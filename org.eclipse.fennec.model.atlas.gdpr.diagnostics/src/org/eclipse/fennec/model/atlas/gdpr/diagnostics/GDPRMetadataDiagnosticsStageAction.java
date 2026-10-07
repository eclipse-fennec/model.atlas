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
package org.eclipse.fennec.model.atlas.gdpr.diagnostics;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.action.api.ActionContext;
import org.eclipse.fennec.model.atlas.action.api.StageActionService;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.eclipse.fennec.model.compliance.report.ReportPackage;
import org.eclipse.fennec.model.compliance.report.ComplianceReport;
import org.eclipse.fennec.model.compliance.report.Subject;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.Promises;

/**
 * Writes the findings of a GDPR review onto the metadata of the model the review is about, so that
 * a package found to hold special-category data no longer looks exactly like one found clean
 * (issue #318).
 * <p>
 * A sibling of {@code GDPRReportHistoryStageAction}, not a second job for it: one action, one
 * output. Both run off the same event, and the registry's default chain runs them with
 * {@code onFailure: continue}, so a failing metadata write never costs a document rebuild or the
 * reverse. They share no state and their order does not matter; do not put them in a chain that
 * stops on failure.
 *
 * <h2>Which object, and in which stage</h2>
 * <p>
 * The report names its subject by fingerprint, which is the identity of a <em>revision</em>: a
 * review is of content, so the same bytes reached by any route are the same subject. The reviewed
 * object is therefore found by matching {@code ObjectMetadata.fingerprint}, never by nsURI and
 * never by objectId - an objectId is opaque and a delete-and-re-upload of the same nsURI yields a
 * new one.
 * <p>
 * <b>Exactly one address is ever written</b>, and its stage is the one the event came from. A
 * review is stage-specific: a model reviewed in {@code approved} gets a report about
 * {@code approved}, and that verdict does not describe the copy in {@code draft}. The gdpr
 * registry's stages mirror the reviewed registry's, so the target stage is simply
 * {@link ActionContext#stage()} - no configuration, and it follows a scope whose stages are named
 * however that scope names them. Only the target <em>registry</em> is configured, because a
 * {@code PackageSubject} resolves into the schema registry and a {@code TransformationSubject} into
 * the transformations one, and the report deliberately does not say which: it is a shared model
 * and knows nothing of Atlas topology. Run one instance per target registry, and name the kind of
 * subject each one answers for.
 * <p>
 * <b>The write is guarded.</b> Configuration naming the wrong registry would no longer write
 * nothing - it would write one stage's findings onto another stage's object, which looks like a
 * result. So the fingerprint has to be present at the address before anything is written, and a
 * miss is logged and dropped rather than searched for elsewhere. A read-only stage is not a reason
 * to skip: the model says writing diagnostics is allowed in stages that are otherwise read-only.
 * <p>
 * <b>Two instances cannot write onto each other's objects</b>, although every report reaches both:
 * an instance answers only for the kind of subject its {@code subject.type} names, and stops at the
 * report otherwise. The fingerprint schemes say the same thing a second time - an {@code fp1:}
 * value matches nothing in the transformations registry and an {@code m2x1:} nothing in the schema
 * one - but that is a property of the digests rather than a rule anybody wrote down, and it is not
 * what the separation rests on. Leaving {@code subject.type} unset answers for every kind, which is
 * what a deployment with a single reviewable registry wants.
 *
 * <h2>When a review goes away</h2>
 * <p>
 * A withdrawn review takes its findings with it, a promoted one does not.
 * <ul>
 * <li>{@code DELETED} - the review is gone, so the diagnostics it produced are cleared. Only this
 * producer's roots go; another producer's findings on the same object are untouched.</li>
 * <li>{@code TRANSITIONED} - nothing to do. A transition carries the same {@code ObjectMetadata}
 * into the target stage, diagnostics included, so a promoted object keeps its findings without
 * anything being rewritten.</li>
 * </ul>
 * <p>
 * <b>The report is already unreadable when {@code EXIT} fires</b>: the event is dispatched after
 * the storage delete and the cache eviction, so the action cannot ask the report which subject it
 * was about. It therefore remembers the subject of every report it has seen enter or update -
 * including on the startup replay, which visits every stored report - and reads it back on the
 * delete. Nothing is guessed: a delete this runtime has no record of is logged and dropped, which
 * leaves a stale diagnostic rather than clearing the wrong one.
 * <p>
 * {@code requiresReplayOnShutdown()} is {@code false}, so the shutdown replay - which dispatches
 * {@code EXIT} with {@code DELETED} for <em>every</em> object in the registry - never reaches this
 * action. {@link ActionContext#replay()} is checked as well, because that flag is one line of
 * configuration away from being flipped and the failure it would cause is silent: every finding in
 * the scope wiped on shutdown, noticed only after a restart.
 */
@Component(name = GDPRMetadataDiagnosticsStageAction.PID, //
		service = StageActionService.class, //
		configurationPid = GDPRMetadataDiagnosticsStageAction.PID, //
		configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = GDPRMetadataDiagnosticsStageAction.Config.class)
public class GDPRMetadataDiagnosticsStageAction implements StageActionService {

	/**
	 * Configuration pid. REQUIRE, so a runtime that has not asked for review findings on its
	 * models does not start writing them by having the bundle installed.
	 */
	static final String PID = "GDPRMetadataDiagnosticsStageAction";

	private static final Logger LOGGER = Logger.getLogger(GDPRMetadataDiagnosticsStageAction.class.getName());

	/** What the storage layer writes into {@code ActionContext.objectType()} for a report. */
	private static final String REPORT_TYPE = EcoreUtil.getURI(ReportPackage.Literals.COMPLIANCE_REPORT).toString();

	/**
	 * Configuration of this component.
	 */
	@ObjectClassDefinition(name = "GDPR Metadata Diagnostics Stage Action")
	public @interface Config {

		@AttributeDefinition(name = "Target registry", //
				description = "The object registry holding the reviewed objects, which is where the diagnostics "
						+ "are written. A review of an EPackage resolves into the schema registry, a review of a "
						+ "compiled transformation into the transformations one; the report says which kind of "
						+ "subject it has but not which registry holds it, so run one instance per registry. The "
						+ "stage is never configured - it is the stage the report itself is in.")
		String target_registry() default "schema";

		@AttributeDefinition(name = "Subject type", //
				description = "The kind of subject this instance answers for, as the EClass name the report "
						+ "model gives it: 'PackageSubject' for a reviewed EPackage, 'TransformationSubject' for a "
						+ "reviewed compiled transformation. It is the counterpart of the target registry - a "
						+ "report of another kind is left to the instance configured for it. Empty answers for "
						+ "every kind, which is right only where one registry holds everything reviewable.", //
				required = false)
		String subject_type() default "";

		@AttributeDefinition(name = "Report stages", //
				description = "The stages of the report registry this action answers for. Each of them writes to "
						+ "the same-named stage of the target registry, which is what makes a review "
						+ "stage-specific.")
		String[] report_stages() default { "draft", "approved", "release" };

		@AttributeDefinition(name = "Trigger scopes", //
				description = "The scopes this action answers for. Empty means every scope that binds the "
						+ "registry, which is what a single-scope deployment wants. Name them as soon as a second "
						+ "scope exists, because the workflow itself does not filter by scope.", //
				required = false)
		String[] trigger_scopes() default {};

		@AttributeDefinition(name = "Scope service target", //
				description = "The scope the reports are read from and the diagnostics are written to, as an OSGi "
						+ "target filter.")
		String scope_target() default "(atlas.scope=jena)";

	}

	private final WritableScopeService<EObject> scope;
	private final GdprFindingsToDiagnostics mapper = new GdprFindingsToDiagnostics();

	/**
	 * What each report was about, so a delete can still say which object to clear.
	 * <p>
	 * Filled on every {@code ENTER} and {@code UPDATE}, the startup replay included, and read on a
	 * delete - the only event at which the report itself is already gone. Keyed by the address of
	 * the report rather than by its id alone, because one registry is shared by every scope that
	 * binds it.
	 * <p>
	 * A report this instance does not answer for is remembered too, with the subject type that says
	 * so. It costs one entry and it keeps the two reasons for doing nothing on a delete apart: a
	 * report of somebody else's kind is not the same thing as a report this runtime never saw, and
	 * only the second is worth a line in the log.
	 */
	private final Map<ReportAddress, ReviewedSubject> subjects = new ConcurrentHashMap<>();

	private final String targetRegistry;
	private final String subjectType;
	private final Set<String> stages;
	private final Set<String> scopes;

	/**
	 * Constructor injection, which DS hands both the reference and the configuration: the
	 * configuration is an activation object like any other, so there is no reason for a second
	 * activate method and the fields it would fill can be final.
	 *
	 * @param scope  reads the report back and writes the diagnostics; one service per scope
	 * @param config which registry the reviewed objects are in, which kind of subject this instance
	 *               answers for, and which events to answer
	 * @throws IllegalArgumentException if {@code subject.type} names no subject the report model
	 *                                  has. An instance answering for a kind of report nobody can
	 *                                  write would do nothing and say nothing about it, so a typo
	 *                                  fails where it can still be seen
	 */
	@Activate
	public GDPRMetadataDiagnosticsStageAction(@Reference(name = "scope") WritableScopeService<EObject> scope,
			Config config) {
		this.scope = scope;
		this.targetRegistry = config.target_registry();
		this.subjectType = validatedSubjectType(config.subject_type());
		this.stages = toSet(config.report_stages());
		this.scopes = toSet(config.trigger_scopes());

		LOGGER.info(() -> String.format(
				"GDPR review findings of %s are written as '%s' diagnostics onto the reviewed objects in registry "
						+ "'%s' of scope '%s', each into the stage its review was carried out at (%s), for %s.",
				subjectType == null ? "every kind of subject" : "a " + subjectType,
				GdprFindingsToDiagnostics.PRODUCER, targetRegistry, scope.getScopeName(), stages,
				scopes.isEmpty() ? "every scope" : scopes));
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

	/**
	 * Yes: the diagnostics are derived from reports, so a runtime that was down while a review was
	 * stored has no other way of noticing it. A replay costs one read and one write per report and
	 * produces the same diagnostics, which - the ids being derived - is not even a change.
	 */
	@Override
	public boolean requiresReplayOnStartup() {
		return true;
	}

	@Override
	public boolean requiresReplayOnShutdown() {
		return false;
	}

	@Override
	public Promise<Void> onEnter(ActionContext ctx) {
		return write(ctx);
	}

	@Override
	public Promise<Void> onUpdate(ActionContext ctx) {
		return write(ctx);
	}

	@Override
	public Promise<Void> onExit(ActionContext ctx) {
		if (!answersFor(ctx.scope())) {
			return Promises.resolved(null);
		}
		ReportAddress address = ReportAddress.of(ctx);
		if (ctx.exitReason() != ExitReason.DELETED) {
			// A transition carries the metadata, diagnostics included, into the target stage, and
			// the ENTER there refreshes what this action wrote. Nothing to do, and nothing to
			// forget: the report is still readable where it moved to.
			return Promises.resolved(null);
		}
		if (ctx.replay()) {
			// A replayed delete is the runtime stopping, not a review being withdrawn. Clearing
			// here would empty every finding in the scope, and only a restart would show it.
			return Promises.resolved(null);
		}
		ReviewedSubject reviewed = subjects.remove(address);
		if (reviewed == null) {
			// Nothing in memory, which after a restart is every report this runtime did not itself
			// write. The reviewed objects still know: read what was written onto them.
			return rewriteFromWhatWasWritten(ctx, address);
		}
		if (!answersFor(reviewed)) {
			// Somebody else's kind of report, and the instance that did write its findings is
			// clearing them from its own record of it.
			return Promises.resolved(null);
		}
		// Recomputed from the reviews that are left, not cleared outright: another review of the
		// same revision may still stand, and the deleted one may have been the superseded one.
		return apply(ctx, reviewed.fingerprint());
	}

	/**
	 * Rewrites the findings of every object in this stage that carries them.
	 * <p>
	 * <b>Why this is not guesswork.</b> A finding of this action sits on the object it is about and
	 * carries this producer, and that object knows its own fingerprint - which is all
	 * {@link #apply} needs, and it survives a restart where the in-memory record does not. Before
	 * this, a report deleted by a runtime that had not written it left its findings standing on an
	 * object whose review had been withdrawn, with nothing to say so.
	 * </p>
	 * <p>
	 * <b>Why every object and not the reviewed one.</b> A delete carries an object id and nothing
	 * else, and the id does not name the subject. Each rewrite recomputes from the reviews that are
	 * still readable - so an object whose review still stands is written what it already had, and
	 * one whose last review has gone is recorded as unreviewed. The sweep cannot invent a finding,
	 * and it repairs whatever was missed while nothing was listening.
	 * </p>
	 */
	private Promise<Void> rewriteFromWhatWasWritten(ActionContext ctx, ReportAddress address) {
		List<String> fingerprints = reviewedObjectsIn(scope.listInStageForRegistry(targetRegistry, ctx.stage()));
		if (fingerprints.isEmpty()) {
			// Somebody else's kind of report, or a stage nothing of this action's was written into.
			LOGGER.log(Level.FINE, () -> String.format(
					"GDPR report %s was deleted and no object in '%s' carries this producer's findings, so none "
							+ "were cleared.",
					address, targetRegistry));
			return Promises.resolved(null);
		}
		LOGGER.log(Level.INFO, () -> String.format(
				"GDPR report %s was deleted and this runtime had no record of it, so the findings on %d reviewed "
						+ "object(s) of '%s' were rewritten from the reviews that are left.",
				address, fingerprints.size(), targetRegistry));
		// apply() does its write before it returns; the first failure is handed back so the
		// workflow records it, and the rest are still attempted.
		Promise<Void> outcome = Promises.resolved(null);
		for (String fingerprint : fingerprints) {
			Promise<Void> rewrite = apply(ctx, fingerprint);
			try {
				if (rewrite.getFailure() != null) {
					outcome = rewrite;
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return Promises.failed(e);
			}
		}
		return outcome;
	}

	/**
	 * The fingerprints of the objects this producer has written findings onto, each once.
	 *
	 * @param objects metadata of a stage of the target registry, diagnostics included
	 * @return the fingerprints, in the order the stage lists them; empty when none of them carries
	 *         a finding of this action's
	 */
	static List<String> reviewedObjectsIn(List<ObjectMetadata> objects) {
		List<String> fingerprints = new ArrayList<>();
		for (ObjectMetadata metadata : objects) {
			String fingerprint = metadata.getFingerprint();
			if (fingerprint == null || fingerprint.isBlank() || fingerprints.contains(fingerprint)) {
				// Without a fingerprint the rewrite has nothing to look the object up by.
				continue;
			}
			for (Diagnostic diagnostic : metadata.getDiagnostics()) {
				if (GdprFindingsToDiagnostics.PRODUCER.equals(diagnostic.getProducer())) {
					fingerprints.add(fingerprint);
					break;
				}
			}
		}
		return fingerprints;
	}

	/* ------------------------------------------------------------------ the work */

	private Promise<Void> write(ActionContext ctx) {
		if (!answersFor(ctx.scope())) {
			return Promises.resolved(null);
		}
		ReportAddress address = ReportAddress.of(ctx);
		ComplianceReport report = read(ctx);
		if (report == null) {
			LOGGER.log(Level.WARNING, () -> String.format(
					"GDPR report %s is not readable, so no findings were written onto the object it reviewed.",
					address));
			return Promises.resolved(null);
		}
		ReviewedSubject reviewed = ReviewedSubject.of(report);
		if (reviewed == null) {
			LOGGER.log(Level.WARNING, () -> String.format(
					"GDPR report %s names no subject fingerprint, so there is no object its findings belong to.",
					address));
			return Promises.resolved(null);
		}
		// Remembered before anything else, so a report whose subject is not in this registry is
		// still one whose delete can be answered by the instance that did write it.
		subjects.put(address, reviewed);
		if (!answersFor(reviewed)) {
			// Not this instance's kind of report. Said at FINE rather than INFO: in a runtime with
			// one instance per reviewable registry this is the ordinary case and happens for every
			// report, and it is not something anybody has to act on.
			LOGGER.log(Level.FINE, () -> String.format(
					"GDPR report %s is about a %s; this instance answers for %s.", address, reviewed.subjectType(),
					subjectType));
			return Promises.resolved(null);
		}
		return apply(ctx, reviewed.fingerprint());
	}

	/**
	 * Resolves the one address the findings belong to and writes what the reviews of that revision
	 * currently say, or logs why it did not.
	 */
	private Promise<Void> apply(ActionContext ctx, String fingerprint) {
		String stage = ctx.stage();
		String registry = targetRegistry;
		ObjectMetadata reviewed = findByFingerprint(registry, stage, fingerprint);
		if (reviewed == null) {
			// Never a search elsewhere: a review describes the stage it was carried out against,
			// so writing it onto another stage's copy would put one stage's verdict on another's
			// object.
			LOGGER.log(Level.INFO, () -> String.format(
					"No object with fingerprint '%s' is in stage '%s' of registry '%s' in scope '%s', so the GDPR "
							+ "review's findings were not written. They land when the reviewed object and its "
							+ "review are in the same stage.",
					fingerprint, stage, registry, ctx.scope()));
			return Promises.resolved(null);
		}
		Latest current = latestReviewOf(ctx, fingerprint);
		// Never an empty list: an object whose last review was withdrawn has not been checked, and
		// clearing the producer would make it indistinguishable from one nobody has reviewed yet.
		List<Diagnostic> roots = current == null ? mapper.noReview(fingerprint)
				: mapper.map(current.report(), current.objectId());
		String what = current == null ? "withdrawn, so the object is recorded as unreviewed"
				: "wrote " + roots.size();
		try {
			resolve(scope.updateDiagnosticsInStageForRegistry(registry, stage, reviewed.getObjectId(),
					GdprFindingsToDiagnostics.PRODUCER, roots));
			LOGGER.log(Level.INFO,
					() -> String.format("GDPR review findings %s on %s/%s/%s/%s (fingerprint '%s').", what,
							scope.getScopeName(), registry, stage, reviewed.getObjectId(), fingerprint));
			return Promises.resolved(null);
		} catch (RuntimeException e) {
			// Failing the promise rather than swallowing it: the workflow records it on the report
			// as a 'stage-action.failed' diagnostic and clears that when the next event succeeds,
			// which is the only trace a compliance record leaves of a write that did not happen.
			return Promises.failed(e);
		}
	}

	/**
	 * The metadata of the object carrying the fingerprint in one stage of one registry, or
	 * {@code null}.
	 * <p>
	 * A scan of the stage rather than an index lookup. {@code findByFingerprint} exists on
	 * {@code EObjectRegistryService}, but it answers across every stage and is not on the scope
	 * API, and the Lucene registry scans for it anyway; a stage of a schema registry holds tens of
	 * objects. An indexed lookup narrowed to one address would be the optimisation, and it belongs
	 * on the scope API rather than here.
	 */
	private ObjectMetadata findByFingerprint(String registry, String stage, String fingerprint) {
		for (ObjectMetadata metadata : scope.listInStageForRegistry(registry, stage)) {
			if (fingerprint.equals(metadata.getFingerprint())) {
				return metadata;
			}
		}
		return null;
	}

	private ComplianceReport read(ActionContext ctx) {
		EObject content = scope.getContentFromStageForRegistry(ctx.registry(), ctx.stage(), ctx.objectId());
		return content instanceof ComplianceReport report ? report : null;
	}

	/**
	 * The review that currently speaks for one revision in one stage: the latest of every report
	 * stored there about that fingerprint, or {@code null} when none is left.
	 *
	 * <p>
	 * <b>Why not simply the report that fired.</b> One revision can be reviewed more than once - an
	 * agent's review and a human's correction of the same revision are the ordinary case, and the
	 * review document exists to hold them as successive revisions. Mapping only the triggering
	 * report would make the object show whichever report was written about last, which on the
	 * startup replay is whichever the registry happens to list last rather than the newest; and it
	 * would make withdrawing any one review erase what all the others said, because a producer's
	 * roots are replaced as a set.
	 * </p>
	 *
	 * <p>
	 * <b>Latest wins</b>, because a report is a snapshot of a judgement about one revision and the
	 * judgement that holds is the most recent one - the same reading under which the review
	 * document treats a later review as a revision superseding an earlier one. Nothing is lost: the
	 * superseded review is still stored, and the document still carries every revision and the diff
	 * between them.
	 * </p>
	 *
	 * <p>
	 * A scan of the stage, reading each report. The subject fingerprint lives in the content rather
	 * than in the metadata, so there is nothing to index on; the sibling document action reads
	 * every review of a subject the same way, across every stage.
	 * </p>
	 */
	/** The review that currently speaks, and the object id it is stored under. */
	private record Latest(ComplianceReport report, String objectId) {
	}

	private Latest latestReviewOf(ActionContext ctx, String fingerprint) {
		ComplianceReport latest = null;
		Instant latestAt = null;
		String latestId = null;
		for (ObjectMetadata metadata : scope.listInStageForRegistry(ctx.registry(), ctx.stage())) {
			ComplianceReport candidate = reportAt(ctx, metadata.getObjectId());
			ReviewedSubject about = candidate == null ? null : ReviewedSubject.of(candidate);
			if (about == null || !fingerprint.equals(about.fingerprint())) {
				continue;
			}
			Instant at = generatedAt(candidate, metadata);
			// The id breaks a tie, so two reviews stamped the same second still order the same way
			// on every run rather than by whatever the registry listed first.
			if (latest == null || at.isAfter(latestAt)
					|| (at.equals(latestAt) && metadata.getObjectId().compareTo(latestId) > 0)) {
				latest = candidate;
				latestAt = at;
				latestId = metadata.getObjectId();
			}
		}
		return latest == null ? null : new Latest(latest, latestId);
	}

	private ComplianceReport reportAt(ActionContext ctx, String objectId) {
		try {
			EObject content = scope.getContentFromStageForRegistry(ctx.registry(), ctx.stage(), objectId);
			return content instanceof ComplianceReport report ? report : null;
		} catch (RuntimeException goneOrUnreadable) {
			// Listed a moment ago and not there now, or not parseable: it cannot speak for the
			// revision either way, and failing the whole write over it would be worse.
			LOGGER.log(Level.FINE, goneOrUnreadable,
					() -> "Report " + objectId + " could not be read while looking for the latest review");
			return null;
		}
	}

	/**
	 * When a review happened: what it says, else when the Atlas last saw it. A report that does not
	 * state its own generation time cannot claim to supersede one that does, which is why the
	 * stated value comes first.
	 */
	private static Instant generatedAt(ComplianceReport report, ObjectMetadata metadata) {
		String stated = report.getGeneratedAt();
		if (stated != null && !stated.isBlank()) {
			try {
				return Instant.parse(stated.trim());
			} catch (DateTimeParseException unreadable) {
				// Not a report to reject, one to order by the next best thing.
			}
		}
		Instant stored = metadata.getLastChangeTime() == null ? metadata.getUploadTime()
				: metadata.getLastChangeTime();
		return stored == null ? Instant.EPOCH : stored;
	}

	/**
	 * What a report says it is about: which kind of artefact, and which revision of it.
	 *
	 * @param fingerprint the revision the review was of - {@code fp1:} for an EPackage,
	 *                    {@code m2x1:} for a compiled transformation. Never blank
	 * @param subjectType the EClass name of the subject, {@code PackageSubject} or
	 *                    {@code TransformationSubject}, which is what decides whether this
	 *                    instance answers for the report at all
	 */
	private record ReviewedSubject(String fingerprint, String subjectType) {

		/** What the report says, or {@code null} when it names no revision to write onto. */
		static ReviewedSubject of(ComplianceReport report) {
			Subject subject = report.getSubject();
			String fingerprint = subject == null ? null : subject.getSubjectFingerprint();
			if (blank(fingerprint)) {
				return null;
			}
			return new ReviewedSubject(fingerprint.trim(), subject.eClass().getName());
		}
	}

	/**
	 * The configured subject type, or {@code null} for every kind.
	 *
	 * @throws IllegalArgumentException if it names no concrete subject of the report model - which
	 *                                  would silently switch this instance off
	 */
	private static String validatedSubjectType(String configured) {
		if (blank(configured)) {
			return null;
		}
		String name = configured.trim();
		if (subjectTypes().contains(name)) {
			return name;
		}
		throw new IllegalArgumentException(String.format(
				"'%s' is not a subject of the GDPR report model, so this action would answer for no report at "
						+ "all. Configure subject.type as one of %s, or leave it unset to answer for every kind.",
				name, subjectTypes()));
	}

	/** The concrete subclasses of {@code Subject} the report model has, by EClass name. */
	private static Set<String> subjectTypes() {
		Set<String> names = new LinkedHashSet<>();
		for (EClassifier classifier : ReportPackage.eINSTANCE.getEClassifiers()) {
			if (classifier instanceof EClass candidate && !candidate.isAbstract()
					&& ReportPackage.Literals.SUBJECT.isSuperTypeOf(candidate)) {
				names.add(candidate.getName());
			}
		}
		return names;
	}

	/**
	 * Waits for the write and turns a failure into an exception on this thread. A promise that
	 * failed silently would leave the object claiming a clean review it never had, which is the one
	 * thing a compliance record must not do.
	 */
	private static void resolve(Promise<ObjectMetadata> written) {
		try {
			written.getValue();
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause() == null ? e : e.getCause();
			throw new IllegalStateException("The GDPR review findings could not be written: " + cause.getMessage(),
					cause);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while writing the GDPR review findings.", e);
		}
	}

	/* ------------------------------------------------------------------ small helpers */

	/**
	 * Which report, in a runtime where one registry is shared by every scope that binds it. The
	 * stage is deliberately not part of it: a promoted report keeps its id, and its ENTER in the
	 * new stage refreshes the entry rather than adding a second one.
	 */
	private record ReportAddress(String scope, String registry, String objectId) {

		static ReportAddress of(ActionContext ctx) {
			return new ReportAddress(ctx.scope(), ctx.registry(), ctx.objectId());
		}

		@Override
		public String toString() {
			return scope + "/" + registry + "/" + objectId;
		}
	}

	private boolean answersFor(String scopeName) {
		return scopes.isEmpty() || scopes.contains(scopeName);
	}

	/** Whether a report about this subject is one this instance writes the findings of. */
	private boolean answersFor(ReviewedSubject reviewed) {
		return subjectType == null || subjectType.equals(reviewed.subjectType());
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
