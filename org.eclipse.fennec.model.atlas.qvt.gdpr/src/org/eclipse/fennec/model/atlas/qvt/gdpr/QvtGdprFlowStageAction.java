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
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.m2x.model.compiled.CompiledPackage;
import org.eclipse.fennec.m2x.model.compiled.CompiledUnit;
import org.eclipse.fennec.model.atlas.action.api.ActionContext;
import org.eclipse.fennec.model.atlas.action.api.StageActionService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.eclipse.fennec.model.gdprReport.GDPRReportPackage;
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
 * Derives the GDPR report of a compiled transformation when the unit enters a stage, and stores it
 * in the report registry beside the metamodel reviews it was derived from (issue #319).
 *
 * <p>
 * A sibling of {@code QvtStageActionService}, not a second job for it: one action, one output. The
 * compile produces the unit, this produces a report about it, and the registry's default chain runs
 * them with {@code onFailure: continue} - so a failing analysis never costs a compile, and the
 * compiler's own diagnostics are untouched by it.
 * </p>
 *
 * <h2>Why a report and not diagnostics</h2>
 * <p>
 * A report is the artefact: it carries evidence, a disclaimer, the identity of what produced it and
 * a revision history, and it can be read, audited and diffed. A diagnostic is a badge on an object.
 * Producing only badges would throw away the reasoning that makes a finding checkable.
 * <p>
 * The badges follow from the report rather than from here, and that ordering is the point:
 * <b>every diagnostic anywhere derives from a stored report</b>. A transformation report stored in
 * this registry is picked up by machinery that already exists - the instance of
 * {@code GDPRMetadataDiagnosticsStageAction} configured for transformations puts the findings on
 * the compiled unit, and {@code GDPRReportHistoryStageAction} keeps the transformation's document.
 * This action adds nothing to either.
 *
 * <h2>Which reviews it rests on</h2>
 * <p>
 * The compiled unit's manifest pins every metamodel it was compiled against to an exact revision by
 * {@code fp1:} fingerprint, and that value is byte-identical to the one the schema registry
 * computed - so joining a unit to the review of the exact model revision it was built against is a
 * lookup and not a heuristic.
 * <p>
 * <b>The reviews are not assumed to be in the unit's own stage.</b> Package visibility runs along a
 * ladder - each stage of a schema registry sees the next one, and the last sees the parent scope -
 * so a transformation in {@code draft} routinely compiles against a metamodel that lives in
 * {@code approved}. A review is filed in the stage of the model it reviewed, so looking only where
 * the unit is would report every such metamodel as unreviewed, which is not merely incomplete but
 * false: the review of exactly those bytes exists, one stage up.
 * <p>
 * Which stages to look in is <b>derived, not configured</b>: it is the unit's own stage and every
 * schema stage declared after it, which is exactly the set the compiler could resolve the unit's
 * metamodels from. See {@link StageLadder}. They are searched nearest first - <b>the first stage
 * that has a review of a fingerprint is the one that speaks for it</b>, whatever later stages hold.
 * Within one stage the usual rule applies and the latest review wins.
 * <p>
 * Nothing about where a report is <em>stored</em> changes: a review still belongs to the stage of
 * the subject it reviewed. It is only the assumption that that stage is also the transformation's
 * that is being dropped.
 * <p>
 * A metamodel with no review in that stage is not skipped: it is listed in the report's subject with
 * its {@code reportId} unset, which is the report saying the analysis is incomplete rather than
 * clean. The unit then carries that as an error, because the check did not run.
 *
 * <h2>The loop this does not enter</h2>
 * <p>
 * The report is written into a registry that has stage actions of its own, and one of those reads
 * reports. Nothing loops back here, for two independent reasons: this action answers only for a
 * {@code CompiledUnit} object type, and a transformation report's subject is a
 * {@code TransformationSubject} with an {@code m2x1:} fingerprint, which never matches a
 * {@code packageEntry}. The first is the rule; the second is a property of the digests, and is why
 * the reviews this action reads are filtered by subject type outright rather than by their
 * fingerprint's prefix.
 */
@Component(name = QvtGdprFlowStageAction.PID, //
		service = StageActionService.class, //
		configurationPid = QvtGdprFlowStageAction.PID, //
		configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = QvtGdprFlowStageAction.Config.class)
public class QvtGdprFlowStageAction implements StageActionService {

	/**
	 * Configuration pid. REQUIRE, so a runtime that has not asked for transformation reports does
	 * not start writing them by having the bundle installed.
	 */
	static final String PID = "QvtGdprFlowStageAction";

	private static final Logger LOGGER = Logger.getLogger(QvtGdprFlowStageAction.class.getName());

	/** What the storage layer writes into {@code ActionContext.objectType()} for a compiled unit. */
	private static final String UNIT_TYPE = EcoreUtil.getURI(CompiledPackage.Literals.COMPILED_UNIT).toString();

	private static final String REPORT_TYPE = EcoreUtil.getURI(GDPRReportPackage.Literals.GDPR_REPORT).toString();

	/** Configuration of this component. */
	@ObjectClassDefinition(name = "QVT GDPR Flow Analysis Stage Action")
	public @interface Config {

		@AttributeDefinition(name = "Report registry", //
				description = "The registry holding the GDPR reports: where the metamodel reviews this analysis "
						+ "rests on are read from, and where the transformation's own report is written. The "
						+ "stage is never configured - it is the stage the compiled unit is in, because a review "
						+ "describes the stage it was carried out at.")
		String report_registry() default "gdpr";

		@AttributeDefinition(name = "Trigger stages", //
				description = "The stages of the transformation registry this action answers for. Empty means "
						+ "every stage.", //
				required = false)
		String[] trigger_stages() default {};

		@AttributeDefinition(name = "Trigger scopes", //
				description = "The scopes this action answers for. Empty means every scope that binds the "
						+ "registry, which is what a single-scope deployment wants.", //
				required = false)
		String[] trigger_scopes() default {};

		@AttributeDefinition(name = "Scope service target", //
				description = "The scope the compiled units and the reports live in, as an OSGi target filter.")
		String scope_target() default "(atlas.scope=jena)";
	}

	private final WritableScopeService<EObject> scope;
	private final TransformationAnalysis analysis;
	private final String reportRegistry;
	private final Set<String> stages;
	private final Set<String> scopes;

	/**
	 * What report each unit produced, so a deleted unit can take its report with it.
	 * <p>
	 * The report's id is derived from the unit and the reviews, so it could be recomputed - but not
	 * at {@code EXIT}, which is dispatched after the delete and the cache eviction, so the unit is
	 * already unreadable by then. Filled on every analysis, the start-up replay included.
	 */
	private final Map<UnitAddress, String> produced = new ConcurrentHashMap<>();

	@Activate
	public QvtGdprFlowStageAction(@Reference(name = "scope") WritableScopeService<EObject> scope, Config config) {
		this.scope = scope;
		this.reportRegistry = config.report_registry();
		this.analysis = new TransformationAnalysis(scope, reportRegistry);
		this.stages = toSet(config.trigger_stages());
		this.scopes = toSet(config.trigger_scopes());

		LOGGER.info(() -> String.format(
				"GDPR flow analysis is derived for compiled transformations of scope '%s' in %s and stored in "
						+ "registry '%s', in the stage the unit is in, for %s. The metamodels' reviews are looked "
						+ "for along the schema registry's stage ladder, nearest first. Catalogue %s.",
				scope.getScopeName(), stages.isEmpty() ? "every stage" : stages, reportRegistry,
				scopes.isEmpty() ? "every scope" : scopes, RuleCatalogue.VERSION));
	}

	/* ------------------------------------------------------------------ the contract */

	@Override
	public boolean supportsObjectType(String objectType) {
		return UNIT_TYPE.equals(objectType);
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
	 * Yes. A unit stored while this runtime was down, or stored before the analyser existed, has no
	 * report and nothing else would ever notice - and it is the only way a unit that has never been
	 * analysed is found at all, since everything else here works from the reports. The analysis is
	 * deterministic and its report id covers its inputs, so replaying it over unchanged inputs
	 * rewrites the same document rather than adding a revision.
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
		return analyse(ctx);
	}

	@Override
	public Promise<Void> onUpdate(ActionContext ctx) {
		return analyse(ctx);
	}

	@Override
	public Promise<Void> onExit(ActionContext ctx) {
		if (!answersFor(ctx.scope())) {
			return Promises.resolved(null);
		}
		UnitAddress address = UnitAddress.of(ctx);
		if (ctx.exitReason() != ExitReason.DELETED) {
			// A transition carries the unit into the next stage, where the ENTER analyses it
			// against that stage's reviews. The report in this stage still describes the unit that
			// was here, and units are not deleted on transition.
			return Promises.resolved(null);
		}
		if (ctx.replay()) {
			// A replayed delete is the runtime stopping, not a transformation being withdrawn.
			return Promises.resolved(null);
		}
		String reportId = produced.remove(address);
		if (reportId == null) {
			LOGGER.log(Level.INFO, () -> String.format(
					"Compiled unit %s was deleted, but this runtime never analysed it, so no transformation "
							+ "report was removed.",
					address));
			return Promises.resolved(null);
		}
		try {
			scope.deleteFromStageForRegistry(reportRegistry, ctx.stage(), reportId).getValue();
			LOGGER.info(() -> String.format("Removed the GDPR flow report %s of the deleted unit %s.", reportId,
					address));
			return Promises.resolved(null);
		} catch (InvocationTargetException | InterruptedException | RuntimeException e) {
			if (e instanceof InterruptedException) {
				Thread.currentThread().interrupt();
			}
			// A report left behind describes a transformation that is gone. Worth a failure, which
			// the workflow records on the object, rather than a silent stale document.
			return Promises.failed(e);
		}
	}

	/* ------------------------------------------------------------------ the work */

	private Promise<Void> analyse(ActionContext ctx) {
		if (!answersFor(ctx.scope())) {
			return Promises.resolved(null);
		}
		UnitAddress address = UnitAddress.of(ctx);
		EObject content = scope.getContentFromStageForRegistry(ctx.registry(), ctx.stage(), ctx.objectId());
		if (!(content instanceof CompiledUnit unit)) {
			LOGGER.fine(() -> "Object " + address + " is no compiled unit; nothing to analyse");
			return Promises.resolved(null);
		}
		if (unit.getManifest() == null) {
			LOGGER.log(Level.WARNING, () -> String.format(
					"Compiled unit %s carries no manifest, so there is nothing saying which metamodel revisions "
							+ "it was compiled against and no analysis is possible.",
					address));
			return Promises.resolved(null);
		}
		try {
			produced.put(address, analysis.analyse(unit, ctx.stage()).getReportId());
			return Promises.resolved(null);
		} catch (RuntimeException e) {
			// Failed rather than swallowed: the workflow records it on the unit as a
			// 'stage-action.failed' diagnostic, which is the only trace an absent report leaves.
			return Promises.failed(e);
		}
	}

	/* ------------------------------------------------------------------ small helpers */

	/** Which unit, in a runtime where one registry is shared by every scope that binds it. */
	private record UnitAddress(String scope, String registry, String objectId) {

		static UnitAddress of(ActionContext ctx) {
			return new UnitAddress(ctx.scope(), ctx.registry(), ctx.objectId());
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
