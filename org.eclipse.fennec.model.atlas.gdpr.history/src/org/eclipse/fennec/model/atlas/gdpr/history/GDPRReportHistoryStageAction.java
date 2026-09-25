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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
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
import org.eclipse.fennec.model.gdprReport.LegalCorpusRef;
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
	 * Stands in for the language of a review that does not state one. A visible placeholder rather
	 * than an empty segment, so a document id stays readable and a reader can tell an unlabelled
	 * review from one carried out in a language nobody configured.
	 */
	private static final String UNKNOWN_LANGUAGE = "UNKNOWN";

	/** How much of the flattened identifier the id keeps, for a reader's benefit. */
	private static final int IDENTIFIER_SEGMENT_MAX = 60;
	/** How much of the language the id keeps. */
	private static final int LANGUAGE_SEGMENT_MAX = 16;
	/** Hex characters of the identifier's digest; what makes the id unique. */
	private static final int DIGEST_CHARS = 8;

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
		stages = toSet(config.report_stages());
		scopes = toSet(config.trigger_scopes());

		LOGGER.info(() -> String.format(
				"GDPR review documents are rebuilt from registry '%s' stages %s of scope '%s' into registry '%s', "
						+ "each into the stage its reviews were carried out at, for %s.",
				registry, stages, scope.getScopeName(), documentRegistry,
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
			String identifier = ReportHistoryBuilder.identifierOf(trigger.get().getSubject());
			if (identifier == null) {
				LOGGER.log(Level.WARNING, () -> String.format(
						"GDPR report '%s' names no subject identifier, so there is no document it belongs to.",
						objectId));
				return;
			}

			Map<DocumentKey, List<StoredReport>> groups = groupsOf(identifier);
			if (groups.isEmpty()) {
				LOGGER.log(Level.INFO, () -> String.format(
						"No review of subject '%s' is readable any more, so no document was written.", identifier));
				return;
			}
			// Every group of the subject, not only the one that fired: a rebuild reads all of its
			// reviews anyway, and rebuilding the rest costs one in-memory pass each. It also
			// repairs a document that was missed while nothing was listening.
			Instant rebuiltAt = Instant.now();
			for (Map.Entry<DocumentKey, List<StoredReport>> group : groups.entrySet()) {
				GdprReportHistory history = builder.build(group.getValue(), rebuiltAt);
				store(history, group.getKey(), group.getValue().size());
			}
		} catch (RuntimeException e) {
			// Thrown on a background thread: swallowed here so one bad subject cannot take the
			// executor down and stop every later rebuild.
			LOGGER.log(Level.SEVERE, e, () -> String.format(
					"The GDPR review document could not be rebuilt after '%s' changed in %s/%s.", objectId,
					triggerScope, registry));
		}
	}

	/**
	 * Every review of one subject, split into the documents they belong to.
	 * <p>
	 * <b>A document covers one stage and one language.</b> A review describes the stage it was
	 * carried out against, so merging a draft review with an approved one would diff two different
	 * judgements and report every rationale as rewritten - the same failure two languages in one
	 * document produce. Reviews are therefore <em>not</em> deduplicated across stages: the stage is
	 * part of what the document is about, not an accident of where a copy happens to sit.
	 */
	private Map<DocumentKey, List<StoredReport>> groupsOf(String subjectIdentifier) {
		Map<DocumentKey, List<StoredReport>> groups = new LinkedHashMap<>();
		for (String stage : stages) {
			ReadableRegistryView<EObject> view = scope.registryView(registry, stage);
			for (String objectId : view.listObjectIds()) {
				view.get(objectId).filter(GdprReport.class::isInstance).map(GdprReport.class::cast)
						.filter(report -> isAbout(report, subjectIdentifier))
						.ifPresent(report -> groups
								.computeIfAbsent(new DocumentKey(subjectIdentifier, stage, languageOf(report)),
										key -> new ArrayList<>())
								.add(stored(objectId, report)));
			}
		}
		return groups;
	}

	/**
	 * What one document is: one subject, reviewed at one stage, in one language. Every part is
	 * needed - drop any of them and the change sheet compares revisions that are not successive
	 * revisions of one review.
	 *
	 * @param subjectIdentifier the nsURI of a package or the qualified name of a transformation
	 * @param stage             the stage the reviews were carried out against
	 * @param language          the corpus language, or {@value #UNKNOWN_LANGUAGE}
	 */
	private record DocumentKey(String subjectIdentifier, String stage, String language) {
	}

	/**
	 * The origin is whatever the report says; the builder reads it from the content and only falls
	 * back to what is passed here for a report written before the model carried the field.
	 */
	private static StoredReport stored(String objectId, GdprReport report) {
		return new StoredReport(objectId, report, null, RevisionOrigin.UNKNOWN);
	}

	private static boolean isAbout(GdprReport report, String subjectIdentifier) {
		return subjectIdentifier.equals(ReportHistoryBuilder.identifierOf(report.getSubject()));
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
	private void store(GdprReportHistory history, DocumentKey key, int revisions) {
		String objectId = documentId(key);
		// The stage of the reviews, not a configured one: the document is stage-specific, and two
		// groups of one subject share an objectId, so writing them to one stage would have the
		// second overwrite the first.
		String stage = key.stage();
		ObjectMetadata existing = scope.getMetadataFromStageForRegistry(documentRegistry, stage, objectId);

		String outcome;
		Promise<ObjectMetadata> written;
		if (existing == null) {
			ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
			metadata.setObjectId(objectId);
			metadata.setObjectName(history.getName());
			metadata.setUploadTime(Instant.now());
			metadata.setVersion(key.subjectIdentifier());
			metadata.setObjectType(EcoreUtil.getURI(history.eClass()).toString());
			written = scope.uploadToStageForRegistry(documentRegistry, stage, history, metadata);
			outcome = "created";
		} else {
			written = scope.updateInStageForRegistry(documentRegistry, stage, history, objectId,
					key.subjectIdentifier());
			outcome = "updated";
		}

		resolve(written, objectId);
		LOGGER.log(Level.INFO, () -> String.format("GDPR review document '%s' (%s) %s in %s/%s/%s, %d revision(s).",
				objectId, key.language(), outcome, scope.getScopeName(), documentRegistry, stage, revisions));
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
	 * The id one document is stored under: one subject, at one stage, in one language. A single path
	 * segment, so everything that is not alphanumeric becomes a dash.
	 * <p>
	 * <b>Keyed by the subject identifier</b> - an nsURI or a unit's qualified name - and not by the
	 * fingerprint. A fingerprint names one revision exactly, so a document keyed by it would hold a
	 * single revision and have nothing to diff; the identifier is what stays put while the content
	 * moves. Each revision's fingerprint is a column of the revision sheet instead.
	 * <p>
	 * <b>And by language</b>, because a review quotes one consolidation of one language version from
	 * start to seal; two languages in one document would diff a German revision against an English
	 * one and report every rationale as changed. The suffix is written even where a runtime serves a
	 * single language, because adding it later would rename a document people already hold a link to.
	 * <p>
	 * <b>The stage is deliberately not in the id.</b> A document is stage-specific, but which stage
	 * it is in is <em>where it lives</em>, not part of what it is called: an id naming a stage would
	 * start lying the moment the document moved. The stage of the reviews decides which stage the
	 * document is written to, so the same id names one document per stage - which is what an objectId
	 * already means everywhere else in the Atlas.
	 * <p>
	 * <b>Why the digest.</b> Reducing an nsURI to one path segment is not injective -
	 * {@code http://x.org/a/b} and {@code http://x.org/a-b} both flatten to {@code http---x-org-a-b}
	 * - so two subjects would share a document and one would silently overwrite the other. The
	 * digest is taken over the <em>raw</em> identifier, which distinguishes them. It also bounds the
	 * id: the readable part is truncated, so a long nsURI cannot push the file name past what a file
	 * system accepts (the file backend stores {@code <objectId>.metadata.xmi} as one name, and
	 * answers an unresolvable one with "no such object" rather than an error).
	 * <p>
	 * The id stays <b>computed, not looked up</b>: the action derives it from the report it is
	 * holding, so it never has to search the document registry for its own output.
	 */
	private static String documentId(DocumentKey key) {
		return "gdpr-history-" + shorten(segment(key.subjectIdentifier()), IDENTIFIER_SEGMENT_MAX) + "-"
				+ digest(key.subjectIdentifier()) + "-"
				+ shorten(segment(key.language()), LANGUAGE_SEGMENT_MAX).toLowerCase(Locale.ROOT);
	}

	/** One path segment: everything that is not alphanumeric becomes a dash. */
	private static String segment(String value) {
		return value.replaceAll("[^A-Za-z0-9]", "-");
	}

	/** At most {@code max} characters, so the whole id stays a legal file name. */
	private static String shorten(String value, int max) {
		return value.length() <= max ? value : value.substring(0, max);
	}

	/**
	 * The first {@value #DIGEST_CHARS} hex characters of the SHA-256 of the value, which is what
	 * makes two identifiers that flatten to the same segment two different documents.
	 */
	private static String digest(String value) {
		try {
			byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder(DIGEST_CHARS);
			for (int i = 0; hex.length() < DIGEST_CHARS; i++) {
				hex.append(String.format("%02x", hash[i]));
			}
			return hex.toString();
		} catch (NoSuchAlgorithmException e) {
			// SHA-256 is required of every Java platform; unreachable.
			throw new IllegalStateException("SHA-256 is not available", e);
		}
	}

	/**
	 * The language a review was carried out in, normalised; {@value #UNKNOWN_LANGUAGE} when unstated.
	 * <p>
	 * Read from the report's own corpus, which is where it is recorded: the review quotes one
	 * consolidation of one language version and the tool that starts a report copies that language
	 * from the corpus it used. A review that names none is kept in a group of its own rather than
	 * folded into a named one - it is the honest answer, and it keeps one unlabelled report from
	 * corrupting the diff of a real language.
	 */
	private static String languageOf(GdprReport report) {
		LegalCorpusRef corpus = report.getCorpus();
		String language = corpus == null ? null : corpus.getLanguage();
		return blank(language) ? UNKNOWN_LANGUAGE : language.trim().toUpperCase(Locale.ROOT);
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
