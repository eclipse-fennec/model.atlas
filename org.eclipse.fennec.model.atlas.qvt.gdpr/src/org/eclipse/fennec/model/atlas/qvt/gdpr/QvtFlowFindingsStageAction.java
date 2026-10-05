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

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.action.api.ActionContext;
import org.eclipse.fennec.model.atlas.action.api.StageActionService;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.eclipse.fennec.model.compliance.report.ReportPackage;
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
 * Writes what a transformation does to a metamodel onto that metamodel (issue #319, WP3).
 *
 * <p>
 * The report of a compiled transformation lands on the compiled unit, because that is what it is a
 * report of. This closes the other half of the gap: a person maintaining the contacts model will
 * never open a transformation's report, and the one thing they most need to know is only in there -
 * that their {@code comment} field, which their own review calls an ordinary string, receives three
 * classified fields of the clinic model concatenated together.
 * </p>
 *
 * <h2>A reader of stored reports, not a second output of the analyser</h2>
 * <p>
 * It is triggered by a transformation report arriving in the report registry, not by a compiled
 * unit being analysed, and it reads the report back rather than being handed anything. That keeps
 * the rule of the whole feature mechanically true rather than true by convention: <b>every
 * diagnostic anywhere derives from a stored report</b>. A report written by hand, or restored from
 * a backup, produces the same diagnostics as one the analyser has just made.
 * <p>
 * It is a sibling of the instance of {@code GDPRMetadataDiagnosticsStageAction} configured for
 * transformations - that one writes the whole report onto the unit under {@code gdpr.review}, this
 * one writes half of it onto the models under {@code gdpr.transformation/<qualifiedName>}. Neither
 * waits on the other and a deployment may have either without the other.
 *
 * <h2>Which report speaks for a transformation</h2>
 * <p>
 * The <b>latest</b> report about that qualified name in the stage, and not the report that fired.
 * A transformation is recompiled and re-analysed, and a report's id covers the unit revision, so
 * several reports about one transformation is the ordinary case rather than an edge one. Mapping
 * the triggering report alone would make the models show whichever report was written about last -
 * on the start-up replay, whichever the registry happens to list last - and deleting any one
 * revision's report would erase what all the others said, because a producer's roots are replaced
 * as a set. The same reading, and the same reason, as the review findings on a reviewed model.
 *
 * <h2>Where it writes, and where it stops</h2>
 * <p>
 * Onto the object in the configured registry whose {@code ObjectMetadata.fingerprint} equals the
 * {@code fp1:} fingerprint the report's subject pins that metamodel to. By fingerprint and never by
 * nsURI: an analysis is of a revision, and writing it onto a different revision of the same nsURI
 * would look like a result.
 * <p>
 * <b>The metamodel is not assumed to be in the report's stage.</b> Package visibility runs along a
 * ladder - each stage of a schema registry sees the next one - so a transformation in {@code draft}
 * routinely reads a metamodel that lives in {@code approved}, and the findings belong on the model
 * where it actually is. The stages to look in are <b>derived, not configured</b> - the report's own
 * stage and every schema stage declared after it, which is exactly what the transformation could
 * read ({@link StageLadder}) - and the findings are written into the first one that holds the
 * fingerprint.
 * <p>
 * That is also what keeps a transformation from annotating a model it could not itself reach: the
 * ladder runs one way, so a {@code draft} transformation may write onto a released metamodel it
 * read, and a released transformation can never write onto a draft.
 * <p>
 * A fingerprint that is in none of those stages is logged and dropped, never guessed at.
 * <p>
 * <b>Every model the report names is written to on every event, even with nothing to say.</b> An
 * empty list is what clears what a previous revision of the analysis left behind - a transformation
 * that stopped touching a field has to stop saying things about it - and that is why the shrink case
 * needs no bookkeeping.
 *
 * <h2>When a report goes away</h2>
 * <ul>
 * <li>{@code DELETED} - what is left of the transformation's reports is written, which clears the
 * withdrawn one's findings without touching another revision's or another transformation's. The
 * report is already unreadable when {@code EXIT} fires, so the models it named are read back from
 * what this action remembered when it wrote them.</li>
 * <li>{@code TRANSITIONED} - nothing to do. The models move with the reports and the {@code ENTER}
 * in the target stage writes there.</li>
 * </ul>
 * <p>
 * {@code requiresReplayOnShutdown()} is {@code false} and {@link ActionContext#replay()} is checked
 * on exit as well, because a shutdown replay dispatches a delete for every object in the registry
 * and would wipe every transformation finding in the scope - noticed only after a restart.
 */
@Component(name = QvtFlowFindingsStageAction.PID, //
		service = StageActionService.class, //
		configurationPid = QvtFlowFindingsStageAction.PID, //
		configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = QvtFlowFindingsStageAction.Config.class)
public class QvtFlowFindingsStageAction implements StageActionService {

	/**
	 * Configuration pid. REQUIRE, so a runtime that has not asked for transformation findings on
	 * its metamodels does not start writing them by having the bundle installed.
	 */
	static final String PID = "QvtFlowFindingsStageAction";

	private static final Logger LOGGER = Logger.getLogger(QvtFlowFindingsStageAction.class.getName());

	/** What the storage layer writes into {@code ActionContext.objectType()} for a report. */
	private static final String REPORT_TYPE = EcoreUtil.getURI(ReportPackage.Literals.COMPLIANCE_REPORT).toString();

	/** Configuration of this component. */
	@ObjectClassDefinition(name = "QVT Flow Findings Stage Action")
	public @interface Config {

		@AttributeDefinition(name = "Target registry", //
				description = "The registry holding the metamodels the findings are written onto - the schema "
						+ "registry. The stage is never configured: it is the stage the report itself is in, "
						+ "because an analysis rests on reviews that are stage-specific.")
		String target_registry() default "schema";

		@AttributeDefinition(name = "Report stages", //
				description = "The stages of the report registry this action answers for.")
		String[] report_stages() default { "draft", "approved", "release" };

		@AttributeDefinition(name = "Trigger scopes", //
				description = "The scopes this action answers for. Empty means every scope that binds the "
						+ "registry, which is what a single-scope deployment wants.", //
				required = false)
		String[] trigger_scopes() default {};

		@AttributeDefinition(name = "Scope service target", //
				description = "The scope the reports are read from and the findings are written to, as an OSGi "
						+ "target filter.")
		String scope_target() default "(atlas.scope=jena)";
	}

	private final WritableScopeService<EObject> scope;
	private final String targetRegistry;
	private final Set<String> stages;
	private final Set<String> scopes;

	/**
	 * What each report was about and which models it was written onto, so a delete can still say
	 * what to clear.
	 * <p>
	 * Filled on every {@code ENTER} and {@code UPDATE}, the start-up replay included, and read on a
	 * delete - the only event at which the report itself is already gone. A report of a metamodel
	 * review is never remembered, which is also how a delete of one is recognised as not this
	 * action's business.
	 */
	private final Map<ReportAddress, Analysed> remembered = new ConcurrentHashMap<>();

	@Activate
	public QvtFlowFindingsStageAction(@Reference(name = "scope") WritableScopeService<EObject> scope, Config config) {
		this.scope = scope;
		this.targetRegistry = config.target_registry();
		this.stages = toSet(config.report_stages());
		this.scopes = toSet(config.trigger_scopes());

		LOGGER.info(() -> String.format(
				"What a transformation does to a metamodel is written as '%s<qualifiedName>' diagnostics onto the "
						+ "metamodels in registry '%s' of scope '%s', found along the schema registry's stage "
						+ "ladder nearest first, for reports in %s, for %s.",
				FlowFindingsToDiagnostics.PRODUCER_PREFIX, targetRegistry, scope.getScopeName(), stages,
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
	 * Yes: these diagnostics are derived from stored reports, so a runtime that was down while a
	 * transformation was analysed has no other way of noticing. The ids are derived, so a replay
	 * over unchanged reports rewrites what is already there rather than changing anything.
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
		if (ctx.exitReason() != ExitReason.DELETED || ctx.replay()) {
			// A transition carries the report into the next stage, where the ENTER writes there.
			// A replayed delete is the runtime stopping, not a report being withdrawn - clearing
			// here would empty every transformation finding in the scope.
			return Promises.resolved(null);
		}
		ReportAddress address = ReportAddress.of(ctx);
		Analysed analysed = remembered.remove(address);
		if (analysed == null) {
			// Either a metamodel review, which is not this action's business, or a report this
			// runtime never saw. Nothing is guessed: a stale diagnostic is better than clearing
			// the wrong producer's.
			LOGGER.log(Level.FINE, () -> String.format(
					"Report %s was deleted and this runtime has no record of it as a transformation analysis, so "
							+ "no transformation findings were cleared.",
					address));
			return Promises.resolved(null);
		}
		// Recomputed from the reports that are left, not cleared outright: another revision's
		// report may still stand, and the deleted one may have been the superseded one.
		return apply(ctx, analysed.qualifiedName(), analysed.models());
	}

	/* ------------------------------------------------------------------ the work */

	private Promise<Void> write(ActionContext ctx) {
		if (!answersFor(ctx.scope())) {
			return Promises.resolved(null);
		}
		ReportAddress address = ReportAddress.of(ctx);
		EObject content = scope.getContentFromStageForRegistry(ctx.registry(), ctx.stage(), ctx.objectId());
		if (!(content instanceof ComplianceReport report)) {
			LOGGER.log(Level.WARNING, () -> String.format(
					"Report %s is not readable, so nothing was written onto the metamodels it is about.", address));
			return Promises.resolved(null);
		}
		if (!(report.getSubject() instanceof TransformationSubject subject)) {
			// A review of a metamodel. Its findings belong on that metamodel and are written there
			// by the review action, under its own producer.
			return Promises.resolved(null);
		}
		if (blank(subject.getQualifiedName())) {
			LOGGER.log(Level.WARNING, () -> String.format(
					"The transformation report %s names no qualified name, so there is no producer to own its "
							+ "findings on the metamodels.",
					address));
			return Promises.resolved(null);
		}
		Map<String, String> models = fingerprintsOf(subject);
		// Remembered before the write, so a report whose models are not in this stage is still one
		// whose delete can be answered.
		remembered.put(address, new Analysed(subject.getQualifiedName().trim(), models));
		return apply(ctx, subject.getQualifiedName().trim(), models);
	}

	/**
	 * Writes what the transformation's reports currently say onto every metamodel any of them names.
	 *
	 * @param qualifiedName the transformation, which is what the producer is named after
	 * @param also          metamodels to write to even if the surviving report does not name them -
	 *                      the triggering report's own, and on a delete the withdrawn report's, so
	 *                      a model that has dropped out of the analysis is cleared rather than left
	 *                      with what an earlier revision said
	 */
	private Promise<Void> apply(ActionContext ctx, String qualifiedName, Map<String, String> also) {
		String producer = FlowFindingsToDiagnostics.PRODUCER_PREFIX + qualifiedName;
		ComplianceReport current = latestAnalysisOf(ctx, qualifiedName);
		Map<String, List<Diagnostic>> roots = current == null ? Map.of() : FlowFindingsToDiagnostics.map(current);

		Map<String, String> models = new LinkedHashMap<>(also);
		if (current != null && current.getSubject() instanceof TransformationSubject subject) {
			models.putAll(fingerprintsOf(subject));
		}
		try {
			int written = 0;
			for (Map.Entry<String, String> model : models.entrySet()) {
				if (writeOnto(ctx, model.getKey(), model.getValue(), producer,
						roots.getOrDefault(model.getKey(), List.of()))) {
					written++;
				}
			}
			int touched = written;
			LOGGER.log(Level.INFO,
					() -> String.format("'%s' diagnostics of %s updated on %d of %d metamodel(s) in %s/%s/%s.",
							producer, qualifiedName, touched, models.size(), scope.getScopeName(), targetRegistry,
							ctx.stage()));
			return Promises.resolved(null);
		} catch (RuntimeException e) {
			// Failed rather than swallowed: the workflow records it on the report as a
			// 'stage-action.failed' diagnostic, which is the only trace a write that did not
			// happen leaves.
			return Promises.failed(e);
		}
	}

	/**
	 * Writes one model's roots, or says why it did not. Answers whether anything was written.
	 */
	private boolean writeOnto(ActionContext ctx, String nsURI, String fingerprint, String producer,
			List<Diagnostic> roots) {
		if (blank(fingerprint)) {
			LOGGER.log(Level.INFO, () -> String.format(
					"The transformation report pins no revision of '%s', so there is no object of it to write "
							+ "onto - the analysis of the flows through it rested on no review either.",
					nsURI));
			return false;
		}
		Located model = locate(ctx, fingerprint);
		if (model == null) {
			LOGGER.log(Level.INFO, () -> String.format(
					"No object with fingerprint '%s' ('%s') is in %s of registry '%s' in scope '%s', so what the "
							+ "transformation does to it was not written onto it. It lands once that revision is "
							+ "in a stage this action is configured to look in.",
					fingerprint, nsURI, searchStages(ctx), targetRegistry, ctx.scope()));
			return false;
		}
		if (roots.isEmpty() && !holdsDiagnosticsOf(model.metadata(), producer)) {
			// Nothing to write and nothing to clear. Skipped rather than written as an empty
			// replacement, so a metamodel this transformation says nothing about is not touched on
			// every event.
			return false;
		}
		resolve(scope.updateDiagnosticsInStageForRegistry(targetRegistry, model.stage(),
				model.metadata().getObjectId(), producer, roots));
		return true;
	}

	/**
	 * The report that currently speaks for a transformation in one stage: the latest of every
	 * transformation report stored there about that qualified name, or {@code null} when none is
	 * left.
	 * <p>
	 * A scan of the stage, reading each report. What a report is about lives in its content rather
	 * than in its metadata, so there is nothing to index on; the sibling report actions read every
	 * report the same way. Only reports whose subject is a {@code TransformationSubject} count - a
	 * metamodel review has no qualified name and nothing to say here.
	 */
	private ComplianceReport latestAnalysisOf(ActionContext ctx, String qualifiedName) {
		ComplianceReport latest = null;
		Instant latestAt = null;
		String latestId = null;
		for (ObjectMetadata metadata : scope.listInStageForRegistry(ctx.registry(), ctx.stage())) {
			ComplianceReport candidate = reportAt(ctx, metadata.getObjectId());
			if (candidate == null || !(candidate.getSubject() instanceof TransformationSubject subject)
					|| !qualifiedName.equals(trim(subject.getQualifiedName()))) {
				continue;
			}
			Instant at = generatedAt(candidate, metadata);
			// The id breaks a tie, so two reports stamped the same second order the same way on
			// every run rather than by whatever the registry listed first.
			if (latest == null || at.isAfter(latestAt)
					|| (at.equals(latestAt) && metadata.getObjectId().compareTo(latestId) > 0)) {
				latest = candidate;
				latestAt = at;
				latestId = metadata.getObjectId();
			}
		}
		return latest;
	}

	private ComplianceReport reportAt(ActionContext ctx, String objectId) {
		try {
			EObject content = scope.getContentFromStageForRegistry(ctx.registry(), ctx.stage(), objectId);
			return content instanceof ComplianceReport report ? report : null;
		} catch (RuntimeException goneOrUnreadable) {
			// Listed a moment ago and not there now, or not parseable: it cannot speak for the
			// transformation either way, and failing the whole write over it would be worse.
			LOGGER.log(Level.FINE, goneOrUnreadable,
					() -> "Report " + objectId + " could not be read while looking for the latest analysis");
			return null;
		}
	}

	/** A metamodel revision and the stage it was found in. */
	private record Located(String stage, ObjectMetadata metadata) {
	}

	/**
	 * The object carrying the fingerprint, in the first of the searched stages that has it.
	 * <p>
	 * A scan per stage rather than an index lookup, for the reason the review action gives: the
	 * indexed lookup answers across every stage at once - which would lose the priority order this
	 * has to respect - and is not on the scope API, and a stage of a schema registry holds tens of
	 * objects.
	 */
	private Located locate(ActionContext ctx, String fingerprint) {
		for (String stage : searchStages(ctx)) {
			for (ObjectMetadata metadata : scope.listInStageForRegistry(targetRegistry, stage)) {
				if (fingerprint.equals(metadata.getFingerprint())) {
					return new Located(stage, metadata);
				}
			}
		}
		return null;
	}

	/**
	 * The stages a metamodel of this transformation may be in, nearest first: the same set the
	 * transformation could read, read off the scope rather than configured beside it.
	 */
	private List<String> searchStages(ActionContext ctx) {
		return StageLadder.visibleIn(scope.getScope(), targetRegistry, ctx.stage());
	}

	/* ------------------------------------------------------------------ small helpers */

	/** Which revision of each metamodel the report rests on, by nsURI, each once. */
	private static Map<String, String> fingerprintsOf(TransformationSubject subject) {
		Map<String, String> models = new LinkedHashMap<>();
		for (PackageSubject entry : subject.getSourcePackages()) {
			index(models, entry);
		}
		for (PackageSubject entry : subject.getTargetPackages()) {
			// A model declared inout is listed in both, as the same nsURI and the same fingerprint
			index(models, entry);
		}
		return models;
	}

	private static void index(Map<String, String> models, PackageSubject entry) {
		if (!blank(entry.getNsURI())) {
			models.putIfAbsent(entry.getNsURI().trim(), trim(entry.getSubjectFingerprint()));
		}
	}

	private static boolean holdsDiagnosticsOf(ObjectMetadata metadata, String producer) {
		return metadata.getDiagnostics().stream().anyMatch(root -> producer.equals(root.getProducer()));
	}

	/** When the analysis ran: what the report says, else when the Atlas last saw it. */
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
	 * Waits for the write and turns a failure into an exception on this thread. A promise that
	 * failed silently would leave a metamodel claiming nothing is done to it.
	 */
	private static void resolve(Promise<ObjectMetadata> written) {
		try {
			written.getValue();
		} catch (InvocationTargetException e) {
			Throwable cause = e.getCause() == null ? e : e.getCause();
			throw new IllegalStateException(
					"The transformation's findings could not be written onto a metamodel: " + cause.getMessage(),
					cause);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException("Interrupted while writing a transformation's findings.", e);
		}
	}

	/** What a report was about, kept so its delete can be answered once it is unreadable. */
	private record Analysed(String qualifiedName, Map<String, String> models) {
	}

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

	private static String trim(String value) {
		return blank(value) ? null : value.trim();
	}

	private static boolean blank(String value) {
		return value == null || value.isBlank();
	}
}
