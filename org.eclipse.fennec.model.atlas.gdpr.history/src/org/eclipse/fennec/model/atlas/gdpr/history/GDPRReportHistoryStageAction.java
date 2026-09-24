/**
 * Copyright (c) 2012 - 2026 Data In Motion and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.gdpr.history;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.action.api.ActionContext;
import org.eclipse.fennec.model.atlas.action.api.StageActionService;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.eclipse.fennec.model.gdprReport.GDPRReportPackage;
import org.eclipse.fennec.model.gdprReport.GdprReport;
import org.eclipse.fennec.model.gdprReport.Subject;
import org.eclipse.fennec.model.gdprReportHistory.GdprReportHistory;
import org.eclipse.fennec.model.gdprReportHistory.RevisionOrigin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.Promises;

/**
 * Rebuilds the {@link GdprReportHistory} of a subject whenever one of its reviews lands, changes or
 * leaves.
 * <p>
 * <b>The object type is an EClass URI, not a name.</b> {@code ActionContext.objectType()} is set to
 * {@code EcoreUtil.getURI(object.eClass()).toString()} by the storage layer, so matching
 * {@code "GdprReport"} would compile, deploy, resolve and never fire once.
 * <p>
 * <b>It filters by scope, which the SPI does not do for it.</b> The workflow dispatches on object
 * type, stage and event only - a registry instance is shared by every scope that binds it, so an
 * action is called for all of them unless it says otherwise. Left unset the filter is open, which
 * keeps a single-scope deployment working without configuration.
 * <p>
 * <b>It never triggers itself.</b> The document is written to its own registry, which binds no
 * stage action at all, and even in the reports' registry {@link #supportsObjectType(String)} only
 * answers for a {@code GdprReport}.
 * <p>
 * <b>The write is in-process.</b> It goes straight through {@link WritableScopeService}, not through
 * a REST call to this same runtime. A loopback cannot work during the startup replay, which runs
 * while the HTTP connector is still being configured - and the replay is the only thing that closes
 * a gap left by a report written while this runtime was down.
 */
@Component(name = GDPRReportHistoryStageAction.PID, //
		service = StageActionService.class, //
		configurationPid = GDPRReportHistoryStageAction.PID, //
		configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = GDPRReportHistoryStageAction.Config.class)
public class GDPRReportHistoryStageAction implements StageActionService {

	/**
	 * Configuration pid. REQUIRE, so a runtime that has not asked for the document does not start
	 * producing one by having the bundle installed.
	 */
	static final String PID = "GDPRReportHistoryStageAction";

	private static final Logger LOGGER = Logger.getLogger(GDPRReportHistoryStageAction.class.getName());

	/** What the storage layer writes into {@code ActionContext.objectType()} for a report. */
	private static final String REPORT_TYPE = EcoreUtil.getURI(GDPRReportPackage.Literals.GDPR_REPORT).toString();

	/**
	 * Configuration of this component.
	 */
	@ObjectClassDefinition(name = "GDPR Report History Stage Action")
	public @interface Config {

		@AttributeDefinition(name = "Report registry", //
				description = "The object registry the GdprReport objects are stored in.")
		String reports_registry() default "gdpr";

		@AttributeDefinition(name = "Report stages", //
				description = "The stages holding reports. Every one of them is read on a rebuild and every "
						+ "one of them triggers: a registry with delete.after.transition moves a promoted "
						+ "report out of the stage it came from, so reading only the first stage would erase "
						+ "a promoted review from the document.")
		String[] report_stages() default { "draft", "release" };

		@AttributeDefinition(name = "Trigger scopes", //
				description = "The scopes this action answers for. Empty means every scope that binds the "
						+ "registry - which is what a single-scope deployment wants. Name them as soon as a "
						+ "second scope exists, because the workflow itself does not filter by scope.", //
				required = false)
		String[] trigger_scopes() default {};

		@AttributeDefinition(name = "Scope service target", //
				description = "Which scope the reports are read from and the document is written to, as an "
						+ "OSGi target filter.")
		String scope_target() default "(atlas.scope=jena)";

		@AttributeDefinition(name = "Document registry", //
				description = "The object registry the derived GdprReportHistory documents are written to. "
						+ "Keep it separate from the report registry: a registry that binds no stage action "
						+ "cannot re-trigger the action that writes into it.")
		String document_registry() default "gdprdoc";

		@AttributeDefinition(name = "Document stage", //
				description = "The stage the document is written to. It must not be a final stage: the "
						+ "document is derived and is rewritten in full every time one of its reviews "
						+ "changes, and a final stage refuses updates.")
		String document_stage() default "draft";
	}

	private final WritableScopeService<EObject> scope;
	private final ReportHistoryBuilder builder = new ReportHistoryBuilder();

	/**
	 * One thread, so two reports landing together produce two rebuilds in order rather than two
	 * racing writes of which the older may land last.
	 */
	private final ExecutorService rebuilds = Executors.newSingleThreadExecutor(
			runnable -> new Thread(runnable, "gdpr-history-rebuild"));

	private volatile String registry = "gdpr";
	private volatile String documentRegistry = "gdprdoc";
	private volatile String documentStage = "draft";
	private volatile Set<String> stages = Set.of();
	private volatile Set<String> scopes = Set.of();

	/**
	 * @param scope reads the reviews back and writes the document; one service per scope
	 */
	@Activate
	public GDPRReportHistoryStageAction(@Reference(name = "scope") WritableScopeService<EObject> scope) {
		this.scope = scope;
	}

	@Activate
	void activate(Config config) {
		registry = config.reports_registry();
		documentRegistry = config.document_registry();
		documentStage = config.document_stage();
		stages = toSet(config.report_stages());
		scopes = toSet(config.trigger_scopes());

		LOGGER.info(() -> String.format(
				"GDPR review documents are rebuilt from registry '%s' stages %s of scope '%s' into %s/%s, for %s.",
				registry, stages, scope.getScopeName(), documentRegistry, documentStage,
				scopes.isEmpty() ? "every scope" : scopes));
	}

	@Deactivate
	void deactivate() {
		rebuilds.shutdown();
		try {
			if (!rebuilds.awaitTermination(10, TimeUnit.SECONDS)) {
				rebuilds.shutdownNow();
			}
		} catch (InterruptedException e) {
			rebuilds.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	/* ------------------------------------------------------------------ the contract */

	@Override
	public boolean supportsObjectType(String objectType) {
		return REPORT_TYPE.equals(objectType);
	}

	@Override
	public Set<String> getTriggerStages() {
		return stages;
	}

	@Override
	public Set<ActionEvent> getTriggerEvents() {
		return Set.of(ActionEvent.ENTER, ActionEvent.UPDATE, ActionEvent.EXIT);
	}

	@Override
	public Promise<Void> onEnter(ActionContext ctx) {
		return rebuildFor(ctx);
	}

	@Override
	public Promise<Void> onUpdate(ActionContext ctx) {
		return rebuildFor(ctx);
	}

	/**
	 * A review that left has to leave the document too, or a deleted review keeps being quoted.
	 * <p>
	 * A promotion is also an exit, and there the report is still readable in the stage it moved to,
	 * so the rebuild finds it. A real deletion is not readable anywhere, and then the subject it
	 * belonged to cannot be established from the id alone - that case is logged rather than
	 * guessed, and the document catches up when the subject is next reviewed.
	 */
	@Override
	public Promise<Void> onExit(ActionContext ctx) {
		return rebuildFor(ctx);
	}

	/**
	 * Yes: the document is derived, so a runtime that was down while reviews were written has no
	 * other way of noticing. A replay costs one rebuild per report and produces the same bytes.
	 */
	@Override
	public boolean requiresReplayOnStartup() {
		return true;
	}

	@Override
	public boolean requiresReplayOnShutdown() {
		return false;
	}

	/* ------------------------------------------------------------------ the work */

	private Promise<Void> rebuildFor(ActionContext ctx) {
		if (!answersFor(ctx.scope())) {
			return Promises.resolved(null);
		}
		String objectId = ctx.objectId();
		String triggerScope = ctx.scope();
		// Off the dispatch thread, like every other action that does real work: the workflow waits
		// on the promise it is given, and a rebuild reads every review of the subject.
		rebuilds.execute(() -> rebuild(triggerScope, objectId));
		return Promises.resolved(null);
	}

	private void rebuild(String triggerScope, String objectId) {
		try {
			Optional<GdprReport> trigger = find(objectId);
			if (trigger.isEmpty()) {
				// A deleted report: nothing left to say which subject it was about.
				LOGGER.log(Level.INFO, () -> String.format(
						"GDPR report '%s' is no longer readable in %s/%s, so the document it belonged to was not "
								+ "rebuilt. It catches up when that subject is next reviewed.",
						objectId, triggerScope, registry));
				return;
			}
			Subject subject = trigger.get().getSubject();
			if (subject == null || blank(subject.getSubjectFingerprint())) {
				LOGGER.log(Level.WARNING, () -> String.format(
						"GDPR report '%s' names no subject fingerprint, so there is no document it belongs to.",
						objectId));
				return;
			}

			List<StoredReport> reports = reportsOf(subject.getSubjectFingerprint());
			GdprReportHistory history = builder.build(reports, Instant.now());
			store(history, subject.getSubjectFingerprint(), reports.size());
		} catch (RuntimeException e) {
			// Thrown on a background thread: swallowed here so one bad subject cannot take the
			// executor down and stop every later rebuild.
			LOGGER.log(Level.SEVERE, e, () -> String.format(
					"The GDPR review document could not be rebuilt after '%s' changed in %s/%s.", objectId,
					triggerScope, registry));
		}
	}

	/** Every review of one subject, across every configured stage. */
	private List<StoredReport> reportsOf(String modelFingerprint) {
		List<StoredReport> reports = new ArrayList<>();
		Set<String> seen = new LinkedHashSet<>();
		for (String stage : stages) {
			ReadableRegistryView<EObject> view = scope.registryView(registry, stage);
			for (String objectId : view.listObjectIds()) {
				if (!seen.add(objectId)) {
					continue;
				}
				view.get(objectId).filter(GdprReport.class::isInstance).map(GdprReport.class::cast)
						.filter(report -> isAbout(report, modelFingerprint))
						.ifPresent(report -> reports.add(stored(objectId, report)));
			}
		}
		return reports;
	}

	/**
	 * The origin is whatever the report says; the builder reads it from the content and only falls
	 * back to what is passed here for a report written before the model carried the field.
	 */
	private static StoredReport stored(String objectId, GdprReport report) {
		return new StoredReport(objectId, report, null, RevisionOrigin.UNKNOWN);
	}

	private static boolean isAbout(GdprReport report, String modelFingerprint) {
		Subject subject = report.getSubject();
		return subject != null && modelFingerprint.equals(subject.getSubjectFingerprint());
	}

	private Optional<GdprReport> find(String objectId) {
		for (String stage : stages) {
			Optional<GdprReport> found = scope.registryView(registry, stage).get(objectId)
					.filter(GdprReport.class::isInstance).map(GdprReport.class::cast);
			if (found.isPresent()) {
				return found;
			}
		}
		return Optional.empty();
	}

	/**
	 * Writes the document, replacing whatever is stored under the same id.
	 * <p>
	 * In-process, through the scope service, rather than through a REST call to this same runtime.
	 * The startup replay runs while the HTTP connector is still being configured, so a loopback
	 * fails there every time - and the replay is precisely the path that exists to close a gap.
	 * <p>
	 * Create-or-replace is spelled out the same way {@code ObjectRegistryResource} spells it,
	 * because the storage layer has no single call for it: {@code uploadToStage} always dispatches
	 * ENTER and {@code updateInStage} always dispatches UPDATE, and which one is correct depends on
	 * whether the object is already there.
	 */
	private void store(GdprReportHistory history, String modelFingerprint, int revisions) {
		String objectId = documentId(modelFingerprint);
		ObjectMetadata existing = scope.getMetadataFromStageForRegistry(documentRegistry, documentStage, objectId);

		String outcome;
		Promise<ObjectMetadata> written;
		if (existing == null) {
			ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
			metadata.setObjectId(objectId);
			metadata.setObjectName(history.getName());
			metadata.setUploadTime(Instant.now());
			metadata.setVersion(modelFingerprint);
			metadata.setObjectType(EcoreUtil.getURI(history.eClass()).toString());
			written = scope.uploadToStageForRegistry(documentRegistry, documentStage, history, metadata);
			outcome = "created";
		} else {
			written = scope.updateInStageForRegistry(documentRegistry, documentStage, history, objectId,
					modelFingerprint);
			outcome = "updated";
		}

		resolve(written, objectId);
		LOGGER.log(Level.INFO, () -> String.format("GDPR review document '%s' %s in %s/%s/%s, %d revision(s).",
				objectId, outcome, scope.getScopeName(), documentRegistry, documentStage, revisions));
	}

	/**
	 * Waits for the storage write and turns a failure into an exception on this thread, so the
	 * caller's catch logs it. A promise that failed silently would leave a document that is quietly
	 * out of date, which is the one thing a compliance record must not be.
	 */
	private void resolve(Promise<ObjectMetadata> written, String objectId) {
		try {
			written.getValue();
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause() == null ? e : e.getCause();
			throw new IllegalStateException(
					String.format("The GDPR review document '%s' could not be written: %s", objectId,
							cause.getMessage() == null ? cause.getClass().getSimpleName() : cause.getMessage()),
					cause);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(
					String.format("Interrupted while writing the GDPR review document '%s'.", objectId), e);
		}
	}

	/**
	 * The id one subject's document is stored under. A single path segment, so everything that is
	 * not alphanumeric becomes a dash.
	 * <p>
	 * <b>Keyed by fingerprint</b>, which is open decision 1 of the plan: keying by nsURI instead
	 * would make one document span several revisions of the model, and this method plus the filter
	 * in {@link #reportsOf(String)} is the whole of the change.
	 */
	private static String documentId(String modelFingerprint) {
		return "gdpr-history-" + modelFingerprint.replaceAll("[^A-Za-z0-9]", "-");
	}

	/* ------------------------------------------------------------------ small helpers */

	private boolean answersFor(String scopeName) {
		return scopes.isEmpty() || scopes.contains(scopeName);
	}

	private static Set<String> toSet(String[] values) {
		if (values == null) {
			return Set.of();
		}
		Set<String> set = new LinkedHashSet<>();
		for (String value : values) {
			if (!blank(value)) {
				set.add(value.trim());
			}
		}
		return Set.copyOf(set);
	}

	private static boolean blank(String value) {
		return value == null || value.isBlank();
	}
}
