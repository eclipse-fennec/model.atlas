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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.action.api.ActionContext;
import org.eclipse.fennec.model.atlas.action.api.StageActionService;
import org.eclipse.fennec.model.atlas.publisher.ObjectPublisher;
import org.eclipse.fennec.model.atlas.publisher.PublishException;
import org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView;
import org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService;
import org.eclipse.fennec.model.gdprReport.GDPRReportPackage;
import org.eclipse.fennec.model.gdprReport.GdprReport;
import org.eclipse.fennec.model.gdprReport.SubjectModel;
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
 * <b>It never triggers itself.</b> The document is written to a different registry through the
 * publisher, and even in the same one {@link #supportsObjectType(String)} only answers for a
 * {@code GdprReport}.
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

	private static final String JSON = "application/json";

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
				description = "Which scope to read the reports from, as an OSGi target filter.")
		String scope_target() default "(atlas.scope=jena)";

		@AttributeDefinition(name = "Publisher target", //
				description = "Which ObjectPublisher writes the document, as an OSGi target filter. It must "
						+ "be configured with overwrite=true: the document is derived and is rewritten in "
						+ "full every time one of its reviews changes.")
		String publisher_target() default "";
	}

	private final ObjectPublisher publisher;
	private final ReadableScopeService<EObject> scope;
	private final ResourceSet json;
	private final ReportHistoryBuilder builder = new ReportHistoryBuilder();

	/**
	 * One thread, so two reports landing together produce two rebuilds in order rather than two
	 * racing writes of which the older may land last.
	 */
	private final ExecutorService rebuilds = Executors.newSingleThreadExecutor(
			runnable -> new Thread(runnable, "gdpr-history-rebuild"));

	private volatile String registry = "gdpr";
	private volatile Set<String> stages = Set.of();
	private volatile Set<String> scopes = Set.of();

	/**
	 * @param publisher writes the document to the atlas
	 * @param scope reads the reviews back; published by the atlas client, one per scope
	 * @param json the codec's JSON resource set, the format the publisher is told the body is in
	 */
	@Activate
	public GDPRReportHistoryStageAction(@Reference(name = "publisher") ObjectPublisher publisher,
			@Reference(name = "scope") ReadableScopeService<EObject> scope,
			@Reference(name = "json", target = "(emf.fileExtension=json)") ResourceSet json) {
		this.publisher = publisher;
		this.scope = scope;
		this.json = json;
	}

	@Activate
	void activate(Config config) {
		registry = config.reports_registry();
		stages = toSet(config.report_stages());
		scopes = toSet(config.trigger_scopes());

		String contentType = publisher.contentType();
		if (contentType != null && !JSON.equalsIgnoreCase(contentType)) {
			// A JSON body announced as something else is worse than no document: the atlas either
			// refuses it, or stores it under a media type nothing will parse it as.
			throw new IllegalStateException(String.format(
					"The configured ObjectPublisher sends '%s', and the document is serialized as '%s'. Set the "
							+ "publisher's content.type to '%s' or point %s at a publisher that uses it.",
					contentType, JSON, JSON, PID));
		}
		LOGGER.info(() -> String.format(
				"GDPR review documents are rebuilt from registry '%s' stages %s of scope '%s', for %s.", registry,
				stages, scope.getScopeName(), scopes.isEmpty() ? "every scope" : scopes));
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
			SubjectModel subject = trigger.get().getSubject();
			if (subject == null || blank(subject.getModelFingerprint())) {
				LOGGER.log(Level.WARNING, () -> String.format(
						"GDPR report '%s' names no subject fingerprint, so there is no document it belongs to.",
						objectId));
				return;
			}

			List<StoredReport> reports = reportsOf(subject.getModelFingerprint());
			GdprReportHistory history = builder.build(reports, Instant.now());
			publish(history, subject.getModelFingerprint(), reports.size());
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
		SubjectModel subject = report.getSubject();
		return subject != null && modelFingerprint.equals(subject.getModelFingerprint());
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

	private void publish(GdprReportHistory history, String modelFingerprint, int revisions) {
		String objectId = documentId(modelFingerprint);
		String body = serialize(history, objectId);
		try {
			ObjectPublisher.Receipt receipt = publisher.publish(objectId, body, history.getName(), modelFingerprint);
			LOGGER.log(Level.INFO,
					() -> String.format("GDPR review document '%s' %s in %s/%s/%s, %d revision(s).", objectId,
							receipt.outcome(), receipt.scope(), receipt.registry(), receipt.stage(), revisions));
		} catch (PublishException e) {
			throw new IllegalStateException(String.format(
					"The GDPR review document '%s' could not be written: %s. The document is rewritten in full on "
							+ "every change, so the publisher it uses has to be configured with overwrite=true.",
					objectId, e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()), e);
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

	private String serialize(GdprReportHistory history, String objectId) {
		Resource resource = json.createResource(URI.createURI(UUID.randomUUID() + ".json"));
		try {
			resource.getContents().add(history);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			resource.save(out, Map.of());
			return out.toString(StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new IllegalStateException(
					String.format("The GDPR review document '%s' could not be serialized: %s", objectId,
							e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()),
					e);
		} finally {
			resource.getContents().clear();
			json.getResources().remove(resource);
		}
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
