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
package org.eclipse.fennec.model.atlas.qvt.gdpr;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.m2x.model.compiled.CompiledUnit;
import org.eclipse.fennec.model.atlas.action.api.ActionContext;
import org.eclipse.fennec.model.atlas.action.api.StageActionService;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.eclipse.fennec.model.compliance.report.ComplianceReport;
import org.eclipse.fennec.model.compliance.report.PackageSubject;
import org.eclipse.fennec.model.compliance.report.TransformationSubject;
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
 * Re-derives a transformation's GDPR report when a review it rests on changes (issue #319, WP4).
 *
 * <p>
 * A transformation report is only as current as the metamodel reviews it was derived from. A review
 * that is <b>corrected</b> changes the analysis as much as one that arrives, and a review that is
 * <b>withdrawn</b> changes it more: a transformation resting on a retracted review is worse than
 * one resting on none, because it still looks complete. So the trigger is every change to a review,
 * not an arrival - {@code ENTER}, {@code UPDATE} and {@code EXIT} on the report registry, which is
 * arrival including by transition, correction in place, and deletion.
 * </p>
 *
 * <h2>Which transformations are stale is answered by the reports, not by the units</h2>
 * <p>
 * A transformation report records what it rested on: each entry of
 * {@code TransformationSubject.sourcePackages} and {@code targetPackages} carries an nsURI
 * <em>and</em> the {@code subjectFingerprint} of the revision whose review was carried over. So the
 * changed review's fingerprint is looked for in the stored transformation reports, and every hit
 * names a transformation whose analysis is now out of date.
 * <p>
 * That beats scanning the compiled units' manifests three times over. It stays in one registry, so
 * finding the work needs no reach into the transformations registry and no reading of unit content.
 * It is the stale set <em>exactly</em> - a unit whose analysis never rested on that revision does
 * not need re-running, and a manifest scan cannot tell the difference. And it answers with what the
 * re-analysis needs: the hit's own subject gives the unit's {@code m2x1:} fingerprint, so the
 * compiled unit is then a lookup rather than a computed objectId.
 * <p>
 * A metamodel that had <b>no review at the time</b> is still listed, with its fingerprint and
 * {@code reportId} unset - so a report that came back incomplete is found by exactly the review it
 * was waiting for, which is the case this feature most needs to catch.
 * <p>
 * The one thing it misses is a unit that has <b>never been analysed</b>: no report, so nothing to
 * match. That is covered from the other side, by the analyser's replay on start-up.
 *
 * <h2>Which stages</h2>
 * <p>
 * Package visibility runs one way along a ladder, so a review landing in {@code approved} makes
 * stale the transformations in {@code draft} and in {@code approved}, and none in {@code release}.
 * That set is {@link StageLadder#stagesSeeing the inverse of the ladder} and is computed from it
 * rather than restated. A transformation report lives in its unit's stage, so report and unit are
 * looked for in the same stage - the one that could see the review.
 *
 * <h2>Why this does not loop</h2>
 * <p>
 * A re-analysis stores a transformation report into the very registry that triggers this action.
 * It terminates because <b>this action answers only for a review of a package</b>: a report whose
 * subject is a {@code TransformationSubject} is this action's own output and is ignored outright.
 * That a transformation's {@code m2x1:} subject fingerprint could never match a
 * {@code packageEntry} is true as well and would also stop it, but that is a property of the
 * digests rather than a rule anybody wrote, and the termination of a loop is not something to leave
 * resting on one.
 */
@Component(name = QvtGdprReanalysisStageAction.PID, //
		service = StageActionService.class, //
		configurationPid = QvtGdprReanalysisStageAction.PID, //
		configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = QvtGdprReanalysisStageAction.Config.class)
public class QvtGdprReanalysisStageAction implements StageActionService {

	/**
	 * Configuration pid. REQUIRE, so a runtime that has not asked for transformation reports does
	 * not start re-deriving them by having the bundle installed.
	 */
	static final String PID = "QvtGdprReanalysisStageAction";

	private static final Logger LOGGER = Logger.getLogger(QvtGdprReanalysisStageAction.class.getName());

	/** Configuration of this component. */
	@ObjectClassDefinition(name = "QVT GDPR Re-analysis Stage Action")
	public @interface Config {

		@AttributeDefinition(name = "Unit registry", //
				description = "The registry holding the compiled transformations whose analysis may have gone "
						+ "stale. Their reports are read from, and written back into, the registry this action is "
						+ "triggered by.")
		String unit_registry() default "transformations";

		@AttributeDefinition(name = "Report stages", //
				description = "The stages of the report registry this action answers for.")
		String[] report_stages() default { "draft", "approved", "release" };

		@AttributeDefinition(name = "Trigger scopes", //
				description = "The scopes this action answers for. Empty means every scope that binds the "
						+ "registry, which is what a single-scope deployment wants.", //
				required = false)
		String[] trigger_scopes() default {};

		@AttributeDefinition(name = "Scope service target", //
				description = "The scope the reviews, the reports and the units live in, as an OSGi target "
						+ "filter.")
		String scope_target() default "(atlas.scope=jena)";
	}

	private final WritableScopeService<EObject> scope;
	private final String unitRegistry;
	private final Set<String> stages;
	private final Set<String> scopes;

	/**
	 * Which revision each review was about, so a delete can still say what went stale.
	 * <p>
	 * Filled on every {@code ENTER} and {@code UPDATE}, and read on a delete - the only event at
	 * which the review itself is already gone, because it is dispatched after the storage delete
	 * and the cache eviction.
	 */
	private final Map<ReportAddress, String> reviewed = new ConcurrentHashMap<>();

	@Activate
	public QvtGdprReanalysisStageAction(@Reference(name = "scope") WritableScopeService<EObject> scope,
			Config config) {
		this.scope = scope;
		this.unitRegistry = config.unit_registry();
		this.stages = toSet(config.report_stages());
		this.scopes = toSet(config.trigger_scopes());

		LOGGER.info(() -> String.format(
				"A changed GDPR review re-derives the reports of the transformations resting on it, in registry "
						+ "'%s' of scope '%s', for reports in %s, for %s.",
				unitRegistry, scope.getScopeName(), stages, scopes.isEmpty() ? "every scope" : scopes));
	}

	/* ------------------------------------------------------------------ the contract */

	@Override
	public boolean supportsObjectType(String objectType) {
		return TransformationAnalysis.REPORT_TYPE.equals(objectType);
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
	 * No. The analyser replays over every stored compiled unit on start-up and re-derives each
	 * report from the reviews as they stand then, so everything this action exists to notice has
	 * already been noticed by the time it would run. Replaying here as well would do the same work
	 * once per review.
	 */
	@Override
	public boolean requiresReplayOnStartup() {
		return false;
	}

	@Override
	public boolean requiresReplayOnShutdown() {
		return false;
	}

	@Override
	public Promise<Void> onEnter(ActionContext ctx) {
		return reviewChanged(ctx);
	}

	@Override
	public Promise<Void> onUpdate(ActionContext ctx) {
		return reviewChanged(ctx);
	}

	@Override
	public Promise<Void> onExit(ActionContext ctx) {
		if (!answersFor(ctx.scope())) {
			return Promises.resolved(null);
		}
		ReportAddress address = ReportAddress.of(ctx);
		if (ctx.exitReason() != ExitReason.DELETED) {
			// A transition is not a withdrawal: the review fires ENTER in the stage it moved to,
			// and re-analysing from there is right, because the analysis is per stage like
			// everything else here.
			return Promises.resolved(null);
		}
		if (ctx.replay()) {
			// A replayed delete is the runtime stopping, not a review being withdrawn.
			return Promises.resolved(null);
		}
		String fingerprint = reviewed.remove(address);
		if (fingerprint == null) {
			// Either a transformation report - this action's own output - or a review this runtime
			// never saw. Nothing is guessed.
			LOGGER.log(Level.FINE, () -> String.format(
					"Report %s was deleted and this runtime has no record of it as a review of a package, so "
							+ "nothing was re-analysed.",
					address));
			return Promises.resolved(null);
		}
		// A withdrawn review is the case that matters most: the transformations resting on it still
		// look complete until they are derived again without it.
		return reanalyse(ctx, fingerprint, "withdrawn");
	}

	/* ------------------------------------------------------------------ the work */

	private Promise<Void> reviewChanged(ActionContext ctx) {
		if (!answersFor(ctx.scope())) {
			return Promises.resolved(null);
		}
		ReportAddress address = ReportAddress.of(ctx);
		ComplianceReport report = reports(ctx).reportAt(ctx.stage(), ctx.objectId());
		if (report == null) {
			LOGGER.log(Level.WARNING, () -> String.format(
					"Report %s is not readable, so nothing could be said about what rests on it.", address));
			return Promises.resolved(null);
		}
		if (!(report.getSubject() instanceof PackageSubject subject)) {
			// This action's own output. Returning here is what stops the loop, and it is a rule
			// rather than an accident of the fingerprint schemes.
			return Promises.resolved(null);
		}
		String fingerprint = subject.getSubjectFingerprint();
		if (fingerprint == null || fingerprint.isBlank()) {
			LOGGER.log(Level.WARNING, () -> String.format(
					"The review %s names no subject fingerprint, so there is nothing to find transformations by.",
					address));
			return Promises.resolved(null);
		}
		reviewed.put(address, fingerprint);
		return reanalyse(ctx, fingerprint, "changed");
	}

	/**
	 * Re-derives every transformation whose stored report says it rested on this revision.
	 *
	 * @param ctx         the event, whose stage says which stages could have read the review
	 * @param fingerprint the revision whose review changed
	 * @param what        what happened to it, for the log
	 */
	private Promise<Void> reanalyse(ActionContext ctx, String fingerprint, String what) {
		try {
			TransformationAnalysis analysis = reports(ctx);
			Set<String> done = new LinkedHashSet<>();
			for (String stage : StageLadder.stagesSeeingIn(scope.getScope(), ctx.registry(), ctx.stage())) {
				for (ComplianceReport stale : restingOn(analysis, ctx.registry(), stage, fingerprint)) {
					TransformationSubject subject = (TransformationSubject) stale.getSubject();
					if (!done.add(stage + "/" + subject.getSubjectFingerprint())) {
						// Two revisions of one transformation's report can name the same unit; it
						// is re-derived once.
						continue;
					}
					rederive(analysis, stage, subject, fingerprint, what);
				}
			}
			return Promises.resolved(null);
		} catch (RuntimeException e) {
			// Failed rather than swallowed: the workflow records it on the review as a
			// 'stage-action.failed' diagnostic, which is the only trace a transformation left
			// resting on a review that moved would otherwise leave.
			return Promises.failed(e);
		}
	}

	/** The stored transformation reports in one stage that rested on this revision. */
	private List<ComplianceReport> restingOn(TransformationAnalysis analysis, String registry, String stage,
			String fingerprint) {
		List<ComplianceReport> stale = new ArrayList<>();
		for (ObjectMetadata metadata : scope.listInStageForRegistry(registry, stage)) {
			ComplianceReport report = analysis.reportAt(stage, metadata.getObjectId());
			if (report == null || !(report.getSubject() instanceof TransformationSubject subject)) {
				continue;
			}
			if (restsOn(subject, fingerprint)) {
				stale.add(report);
			}
		}
		return stale;
	}

	/**
	 * Whether the report says it rested on this revision. The entry is matched by fingerprint and
	 * not by nsURI: what a review is of is a revision, and a report that rested on another revision
	 * of the same metamodel is not stale because this one changed.
	 */
	private static boolean restsOn(TransformationSubject subject, String fingerprint) {
		return Stream.concat(subject.getSourcePackages().stream(), subject.getTargetPackages().stream())
				.anyMatch(entry -> fingerprint.equals(entry.getSubjectFingerprint()));
	}

	/** Finds the compiled unit the stale report is about and derives its report again. */
	private void rederive(TransformationAnalysis analysis, String stage, TransformationSubject subject,
			String fingerprint, String what) {
		String unitFingerprint = subject.getSubjectFingerprint();
		CompiledUnit unit = unitAt(stage, unitFingerprint);
		if (unit == null) {
			// The report outlived the unit it is about. Nothing to re-derive, and nothing to
			// invent: whoever deleted the unit owns its report.
			LOGGER.log(Level.INFO, () -> String.format(
					"The review of '%s' %s and '%s' rested on it, but no unit with fingerprint '%s' is in stage "
							+ "'%s' of registry '%s', so its report was left as it stands.",
					fingerprint, what, subject.getQualifiedName(), unitFingerprint, stage, unitRegistry));
			return;
		}
		LOGGER.info(() -> String.format("The review of '%s' %s; re-deriving the report of '%s' in stage '%s'.",
				fingerprint, what, subject.getQualifiedName(), stage));
		analysis.analyse(unit, stage);
	}

	/**
	 * The analysis, over the registry this event came from. That registry is where the reviews and
	 * the transformation reports both live, and it is the one a re-derived report goes back into -
	 * so it is read off the event rather than configured a second time.
	 */
	private TransformationAnalysis reports(ActionContext ctx) {
		return new TransformationAnalysis(scope, ctx.registry());
	}

	/**
	 * The compiled unit carrying this {@code m2x1:} fingerprint in one stage, or {@code null}.
	 * <p>
	 * A lookup and not a computed objectId, which only works because a compiled unit's
	 * {@code ObjectMetadata.fingerprint} is the value its own manifest states.
	 */
	private CompiledUnit unitAt(String stage, String fingerprint) {
		if (fingerprint == null || fingerprint.isBlank()) {
			return null;
		}
		for (ObjectMetadata metadata : scope.listInStageForRegistry(unitRegistry, stage)) {
			if (!fingerprint.equals(metadata.getFingerprint())) {
				continue;
			}
			EObject content = scope.getContentFromStageForRegistry(unitRegistry, stage, metadata.getObjectId());
			if (content instanceof CompiledUnit unit && unit.getManifest() != null) {
				return unit;
			}
		}
		return null;
	}

	/* ------------------------------------------------------------------ small helpers */

	/** Which report, in a runtime where one registry is shared by every scope that binds it. */
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

	private static Set<String> toSet(String[] values) {
		if (values == null) {
			return Set.of();
		}
		Set<String> set = new LinkedHashSet<>();
		for (String value : values) {
			if (value != null && !value.isBlank()) {
				set.add(value.trim());
			}
		}
		return Set.copyOf(set);
	}
}
