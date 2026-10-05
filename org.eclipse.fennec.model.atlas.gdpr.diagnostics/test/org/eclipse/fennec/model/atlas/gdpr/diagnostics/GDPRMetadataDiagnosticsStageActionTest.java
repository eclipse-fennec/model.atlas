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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.action.api.ActionContext;
import org.eclipse.fennec.model.atlas.action.api.StageActionService.ActionEvent;
import org.eclipse.fennec.model.atlas.action.api.StageActionService.ExitReason;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.eclipse.fennec.model.compliance.context.CategoryRef;
import org.eclipse.fennec.model.compliance.context.ContextFactory;
import org.eclipse.fennec.model.compliance.report.ClassifierEvaluation;
import org.eclipse.fennec.model.compliance.report.Evidence;
import org.eclipse.fennec.model.compliance.report.FeatureEvaluation;
import org.eclipse.fennec.model.compliance.report.Finding;
import org.eclipse.fennec.model.compliance.report.ReportFactory;
import org.eclipse.fennec.model.compliance.report.ReportPackage;
import org.eclipse.fennec.model.compliance.report.ComplianceReport;
import org.eclipse.fennec.model.compliance.report.PackageSubject;
import org.eclipse.fennec.model.compliance.report.RelevanceLevel;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.Promises;

/**
 * What the action does with an event: which one address it writes to, and when it writes nothing
 * at all.
 */
class GDPRMetadataDiagnosticsStageActionTest {

	private static final ReportFactory REPORTS = ReportFactory.eINSTANCE;
	private static final String REPORT_TYPE = EcoreUtil.getURI(ReportPackage.Literals.COMPLIANCE_REPORT).toString();
	private static final String FINGERPRINT = "fp1:d92662d3d16860877f59b7b00131d3beb185f35926ab8038cc14aafa389cc37a";

	private final Scope scope = new Scope();

	/* ------------------------------------------------------------------ the contract */

	@Test
	@DisplayName("It answers for a ComplianceReport only, on enter, update and exit, and replays at startup")
	void contract() {
		var action = action();

		assertTrue(action.supportsObjectType(REPORT_TYPE));
		assertFalse(action.supportsObjectType("http://www.eclipse.org/emf/2002/Ecore#//EPackage"),
				"the object type is an EClass URI, and matching the wrong one would deploy and never fire");
		assertEquals(java.util.Set.of("draft", "approved", "release"), action.getTriggerStages());
		assertEquals(java.util.Set.of(ActionEvent.ENTER, ActionEvent.UPDATE, ActionEvent.EXIT),
				action.getTriggerEvents());
		assertTrue(action.requiresReplayOnStartup(), "a review stored while this runtime was down has to land");
		assertFalse(action.requiresReplayOnShutdown(),
				"the shutdown replay sends a DELETED exit for every object; answering it would clear everything");
	}

	/* ------------------------------------------------------------------ where it writes */

	@Test
	@DisplayName("A review writes to its own stage, and leaves the same fingerprint in another stage alone")
	void writesToItsOwnStageOnly() {
		scope.report("gdpr", "approved", "report-1", report(FINGERPRINT));
		scope.reviewed("schema", "approved", "pkg-approved", FINGERPRINT);
		scope.reviewed("schema", "draft", "pkg-draft", FINGERPRINT);

		action().onEnter(context("approved", "report-1"));

		assertEquals(1, scope.writes.size(), "exactly one address, never every hit");
		Write write = scope.writes.get(0);
		assertEquals("schema", write.registry());
		assertEquals("approved", write.stage());
		assertEquals("pkg-approved", write.objectId(),
				"a review describes the stage it was carried out against, not the copy in another stage");
		assertEquals(GdprFindingsToDiagnostics.PRODUCER, write.producer());
		assertEquals(1, write.roots().size(), "one root per producer");
		assertEquals(GdprFindingsToDiagnostics.CODE_REVIEW, write.roots().get(0).getCode());
		assertEquals("//Patient/category", write.roots().get(0).getChildren().get(0).getTarget());
	}

	@Test
	@DisplayName("A fingerprint that is not at the configured address is skipped, not searched for elsewhere")
	void skipsWhenTheFingerprintIsNotThere() {
		scope.report("gdpr", "approved", "report-1", report(FINGERPRINT));
		// the reviewed package sits in another stage; a misconfigured target would write there
		scope.reviewed("schema", "draft", "pkg-draft", FINGERPRINT);

		action().onEnter(context("approved", "report-1"));

		assertTrue(scope.writes.isEmpty(),
				"writing one stage's findings onto another stage's object looks like a result and is not one");
	}

	@Test
	@DisplayName("The startup replay still writes: it is how a review stored while the runtime was down lands")
	void startupReplayWrites() {
		scope.report("gdpr", "approved", "report-1", report(FINGERPRINT));
		scope.reviewed("schema", "approved", "pkg-approved", FINGERPRINT);

		action().onEnter(replayed(context("approved", "report-1")));

		assertEquals(1, scope.writes.size());
	}

	@Test
	@DisplayName("A scope the action does not answer for is left alone")
	void scopeFilter() {
		scope.report("gdpr", "approved", "report-1", report(FINGERPRINT));
		scope.reviewed("schema", "approved", "pkg-approved", FINGERPRINT);
		var action = action(new String[] { "somewhere-else" });

		action.onEnter(context("approved", "report-1"));

		assertTrue(scope.writes.isEmpty());
	}

	@Test
	@DisplayName("A report naming no subject fingerprint belongs to no object")
	void reportWithoutASubjectIsSkipped() {
		ComplianceReport orphan = REPORTS.createComplianceReport();
		orphan.setSubject(REPORTS.createPackageSubject());
		scope.report("gdpr", "approved", "report-1", orphan);
		scope.reviewed("schema", "approved", "pkg-approved", FINGERPRINT);

		action().onEnter(context("approved", "report-1"));

		assertTrue(scope.writes.isEmpty());
	}

	@Test
	@DisplayName("A review that found nothing still writes, so a previous run's findings are withdrawn")
	void aCleanReviewClearsWhatAnEarlierOneSaid() {
		ComplianceReport clean = report(FINGERPRINT);
		((FeatureEvaluation) ((ClassifierEvaluation) clean.getEvaluations().get(0)).getFeatureEvaluations().get(0))
				.getFindings().clear();
		scope.report("gdpr", "approved", "report-1", clean);
		scope.reviewed("schema", "approved", "pkg-approved", FINGERPRINT);

		action().onEnter(context("approved", "report-1"));

		assertEquals(1, scope.writes.size(), "a review that found nothing is a statement, not a reason to skip");
		assertEquals(GdprFindingsToDiagnostics.CODE_REVIEW, onlyRoot(scope.writes.get(0)).getCode());
		assertEquals(DiagnosticSeverity.INFO, onlyRoot(scope.writes.get(0)).getSeverity(),
				"reviewed and clean, which is not the same as not reviewed");
	}

	/* ------------------------------------------------------------------ when a review goes */

	@Test
	@DisplayName("A deleted review takes its own findings with it and leaves another producer's alone")
	void deletedClearsThisProducersRoots() {
		scope.report("gdpr", "approved", "report-1", report(FINGERPRINT));
		scope.reviewed("schema", "approved", "pkg-approved", FINGERPRINT);
		var action = action();
		action.onEnter(context("approved", "report-1"));
		scope.writes.clear();

		// the storage delete has already happened by the time EXIT is dispatched
		scope.deleteReport("gdpr", "approved", "report-1");
		action.onExit(exit(context("approved", "report-1"), ExitReason.DELETED));

		assertEquals(1, scope.writes.size());
		Write write = scope.writes.get(0);
		assertEquals("pkg-approved", write.objectId());
		assertEquals(GdprFindingsToDiagnostics.PRODUCER, write.producer(),
				"the replacement rewrites this producer's roots and only this producer's");
		assertEquals(GdprFindingsToDiagnostics.CODE_NO_REVIEW, onlyRoot(write).getCode());
	}

	@Test
	@DisplayName("A promoted review changes nothing: the metadata, diagnostics included, moves with the object")
	void transitionedChangesNothing() {
		scope.report("gdpr", "approved", "report-1", report(FINGERPRINT));
		scope.reviewed("schema", "approved", "pkg-approved", FINGERPRINT);
		var action = action();
		action.onEnter(context("approved", "report-1"));
		scope.writes.clear();

		action.onExit(exit(context("approved", "report-1"), ExitReason.TRANSITIONED));

		assertTrue(scope.writes.isEmpty());
	}

	@Test
	@DisplayName("A replayed exit changes nothing, because the shutdown replay deletes everything")
	void replayedExitChangesNothing() {
		// requiresReplayOnShutdown() is false, so the workflow does not send this today. The guard
		// is what keeps flipping that flag from silently emptying every finding in the scope on
		// the next shutdown - a failure only a restart would reveal.
		scope.report("gdpr", "approved", "report-1", report(FINGERPRINT));
		scope.reviewed("schema", "approved", "pkg-approved", FINGERPRINT);
		var action = action();
		action.onEnter(context("approved", "report-1"));
		scope.writes.clear();

		action.onExit(replayed(exit(context("approved", "report-1"), ExitReason.DELETED)));

		assertTrue(scope.writes.isEmpty());
	}

	@Test
	@DisplayName("A delete this runtime has no record of is not guessed about")
	void deleteWithoutARecordIsNotGuessedAbout() {
		scope.reviewed("schema", "approved", "pkg-approved", FINGERPRINT);

		action().onExit(exit(context("approved", "report-vanished"), ExitReason.DELETED));

		assertTrue(scope.writes.isEmpty(),
				"a stale diagnostic is better than clearing the findings of a review that still stands");
	}

	@Test
	@Disabled("The deployment reviews in German only, so there is one gdpr.check~de and one producer. "
			+ "Enable this the day a second gdpr.check~<lang> is configured: both languages then review the same "
			+ "fingerprint, both fire this action against the same object, and because updateDiagnostics replaces "
			+ "all roots of a producer the second silently erases the first's - the write succeeds, the event "
			+ "fires, and the finding count simply halves. The fix is a per-language producer (gdpr.review.<lang>) "
			+ "in the same change that enables the language.")
	@DisplayName("An EN and a DE review of one fingerprint both survive")
	void twoLanguagesBothSurvive() {
		scope.report("gdpr", "approved", "report-en", report(FINGERPRINT));
		scope.report("gdpr", "approved", "report-de", report(FINGERPRINT));
		scope.reviewed("schema", "approved", "pkg-approved", FINGERPRINT);
		var action = action();

		action.onEnter(context("approved", "report-en"));
		action.onEnter(context("approved", "report-de"));

		assertEquals(2, scope.writes.size());
		assertFalse(scope.writes.get(0).producer().equals(scope.writes.get(1).producer()),
				"one producer per language, or the second review erases the first");
	}

	/* --------------------------------------------- two reviews of the same revision */

	@Test
	@DisplayName("Two reviews of one revision: the object shows the later one, whichever order they arrive in")
	void theLaterReviewSupersedesTheEarlier() {
		scope.report("gdpr", "approved", "report-ai",
				report(FINGERPRINT, "PERSONAL_DATA", RelevanceLevel.MEDIUM, "2026-09-28T08:00:00Z"));
		scope.report("gdpr", "approved", "report-human",
				report(FINGERPRINT, "QUASI_IDENTIFIER", RelevanceLevel.HIGH, "2026-09-28T09:00:00Z"));
		scope.reviewed("schema", "approved", "pkg-approved", FINGERPRINT);
		var action = action();

		// the startup replay dispatches in listing order, which is not chronological: the older
		// review arriving last must not overwrite the correction that supersedes it
		action.onEnter(replayed(context("approved", "report-human")));
		action.onEnter(replayed(context("approved", "report-ai")));

		assertEquals("gdpr.finding.QUASI_IDENTIFIER.HIGH", lastWrittenChildCode(),
				"whichever report fires, the object shows what the latest review of that revision says");
	}

	@Test
	@DisplayName("Withdrawing the superseded review leaves the one that superseded it standing")
	void deletingTheSupersededReviewKeepsTheOther() {
		scope.report("gdpr", "approved", "report-ai",
				report(FINGERPRINT, "PERSONAL_DATA", RelevanceLevel.MEDIUM, "2026-09-28T08:00:00Z"));
		scope.report("gdpr", "approved", "report-human",
				report(FINGERPRINT, "QUASI_IDENTIFIER", RelevanceLevel.HIGH, "2026-09-28T09:00:00Z"));
		scope.reviewed("schema", "approved", "pkg-approved", FINGERPRINT);
		var action = action();
		action.onEnter(context("approved", "report-ai"));
		action.onEnter(context("approved", "report-human"));

		scope.deleteReport("gdpr", "approved", "report-ai");
		action.onExit(exit(context("approved", "report-ai"), ExitReason.DELETED));

		assertEquals("gdpr.finding.QUASI_IDENTIFIER.HIGH", lastWrittenChildCode(),
				"one review being withdrawn must not erase what another review still says");
	}

	@Test
	@DisplayName("Withdrawing the latest review brings back what the one before it said")
	void deletingTheLatestReviewFallsBackToTheEarlier() {
		scope.report("gdpr", "approved", "report-ai",
				report(FINGERPRINT, "PERSONAL_DATA", RelevanceLevel.MEDIUM, "2026-09-28T08:00:00Z"));
		scope.report("gdpr", "approved", "report-human",
				report(FINGERPRINT, "QUASI_IDENTIFIER", RelevanceLevel.HIGH, "2026-09-28T09:00:00Z"));
		scope.reviewed("schema", "approved", "pkg-approved", FINGERPRINT);
		var action = action();
		action.onEnter(context("approved", "report-ai"));
		action.onEnter(context("approved", "report-human"));
		assertEquals("gdpr.finding.QUASI_IDENTIFIER.HIGH", lastWrittenChildCode(),
				"with both stored the object says what the latest review says");

		scope.deleteReport("gdpr", "approved", "report-human");
		action.onExit(exit(context("approved", "report-human"), ExitReason.DELETED));

		assertEquals("gdpr.finding.PERSONAL_DATA.MEDIUM", lastWrittenChildCode(),
				"the earlier review is the only one left, so its findings are what the object says now");
	}

	@Test
	@DisplayName("Withdrawing the only review of a revision leaves the object recorded as unreviewed")
	void deletingTheOnlyReviewClears() {
		scope.report("gdpr", "approved", "report-1", report(FINGERPRINT));
		scope.reviewed("schema", "approved", "pkg-approved", FINGERPRINT);
		var action = action();
		action.onEnter(context("approved", "report-1"));

		scope.deleteReport("gdpr", "approved", "report-1");
		action.onExit(exit(context("approved", "report-1"), ExitReason.DELETED));

		Diagnostic root = onlyRoot(scope.writes.get(scope.writes.size() - 1));
		assertEquals(GdprFindingsToDiagnostics.CODE_NO_REVIEW, root.getCode(),
				"nothing checked it any more, and silence would read as nobody having checked it yet");
		assertEquals(DiagnosticSeverity.ERROR, root.getSeverity());
	}

	/** The one root of a write: a producer replaces its roots as a set, and this one writes one. */
	private static Diagnostic onlyRoot(Write write) {
		assertEquals(1, write.roots().size(), "one root per producer");
		return write.roots().get(0);
	}

	/** The code of the one claim in the tree written last: root -> element -> claim. */
	private String lastWrittenChildCode() {
		assertFalse(scope.writes.isEmpty(), "nothing was written at all");
		List<Diagnostic> roots = scope.writes.get(scope.writes.size() - 1).roots();
		assertEquals(1, roots.size(), "one root per producer");
		assertEquals(1, roots.get(0).getChildren().size(), "one reviewed element");
		Diagnostic element = roots.get(0).getChildren().get(0);
		assertEquals(1, element.getChildren().size(), "one claim about it");
		return element.getChildren().get(0).getCode();
	}

	/* ------------------------------------------------------------------ fixtures */

	private GDPRMetadataDiagnosticsStageAction action() {
		return action(new String[0]);
	}

	private GDPRMetadataDiagnosticsStageAction action(String[] triggerScopes) {
		@SuppressWarnings("unchecked")
		WritableScopeService<EObject> service = (WritableScopeService<EObject>) Proxy.newProxyInstance(
				getClass().getClassLoader(), new Class<?>[] { WritableScopeService.class }, scope);
		Map<String, Object> values = new LinkedHashMap<>();
		values.put("target_registry", "schema");
		values.put("report_stages", new String[] { "draft", "approved", "release" });
		values.put("trigger_scopes", triggerScopes);
		values.put("scope_target", "(atlas.scope=jena)");
		return new GDPRMetadataDiagnosticsStageAction(service, config(values));
	}

	private static ActionContext context(String stage, String objectId) {
		return new ActionContext("jena", "gdpr", objectId, REPORT_TYPE, stage, null, null, null, "someone",
				Instant.now(), null, false, Map.of());
	}

	private static ActionContext exit(ActionContext ctx, ExitReason reason) {
		return new ActionContext(ctx.scope(), ctx.registry(), ctx.objectId(), ctx.objectType(), ctx.stage(),
				ctx.sourceStage(), ctx.targetStage(), reason, ctx.triggerUser(), ctx.triggerTime(), ctx.notes(),
				ctx.replay(), ctx.metadata());
	}

	private static ActionContext replayed(ActionContext ctx) {
		return new ActionContext(ctx.scope(), ctx.registry(), ctx.objectId(), ctx.objectType(), ctx.stage(),
				ctx.sourceStage(), ctx.targetStage(), ctx.exitReason(), "system", ctx.triggerTime(), ctx.notes(), true,
				ctx.metadata());
	}

	/** A review of one package that found special-category data on one feature. */
	private static ComplianceReport report(String fingerprint) {
		return report(fingerprint, "SPECIAL_CATEGORY", RelevanceLevel.HIGH, null);
	}

	/** A review that reached one category at one relevance, at a stated time. */
	private static ComplianceReport report(String fingerprint, String category, RelevanceLevel relevance,
			String generatedAt) {
		ComplianceReport report = REPORTS.createComplianceReport();
		report.setGeneratedBy("claude-opus-5");
		report.setGeneratedAt(generatedAt);
		PackageSubject subject = REPORTS.createPackageSubject();
		subject.setNsURI("http://test.fennec.eclipse.org/bootstrap/registry/person/1.0.0");
		subject.setSubjectFingerprint(fingerprint);
		report.setSubject(subject);

		ClassifierEvaluation classifier = REPORTS.createClassifierEvaluation();
		classifier.setUriFragment("//Patient");
		FeatureEvaluation feature = REPORTS.createFeatureEvaluation();
		feature.setUriFragment("//Patient/category");
		Finding finding = REPORTS.createFinding();
		finding.setId("F-001");
		finding.getCategories().add(categoryRef(category));
		finding.setRelevanceLevel(relevance);
		finding.setRationale("The literals enumerate religious denominations.");
		Evidence evidence = REPORTS.createEvidence();
		evidence.setCitationId("Art.9(1)");
		evidence.setQuote("...");
		finding.getEvidence().add(evidence);
		feature.getFindings().add(finding);
		classifier.getFeatureEvaluations().add(feature);
		report.getEvaluations().add(classifier);
		return report;
	}

	@SuppressWarnings("unchecked")
	private static GDPRMetadataDiagnosticsStageAction.Config config(Map<String, Object> values) {
		InvocationHandler handler = (p, method, args) -> "annotationType".equals(method.getName())
				? GDPRMetadataDiagnosticsStageAction.Config.class
				: values.get(method.getName());
		return (GDPRMetadataDiagnosticsStageAction.Config) Proxy.newProxyInstance(
				GDPRMetadataDiagnosticsStageActionTest.class.getClassLoader(),
				new Class<?>[] { GDPRMetadataDiagnosticsStageAction.Config.class }, handler);
	}

	/** One call of {@code updateDiagnosticsInStageForRegistry}. */
	private record Write(String registry, String stage, String objectId, String producer, List<Diagnostic> roots) {
	}

	/**
	 * A scope that answers from maps a test lays out by hand and records every diagnostics write.
	 * Anything the action calls beyond the four methods it needs is a change worth failing over,
	 * not worth stubbing for.
	 */
	private static final class Scope implements InvocationHandler {

		private final Map<String, EObject> reports = new LinkedHashMap<>();
		private final Map<String, List<ObjectMetadata>> stages = new LinkedHashMap<>();
		final List<Write> writes = new ArrayList<>();

		/** A report stored at one address of the report registry, listable like a stored object. */
		void report(String registry, String stage, String objectId, EObject report) {
			reports.put(registry + "/" + stage + "/" + objectId, report);
			ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
			metadata.setObjectId(objectId);
			metadata.setUploadTime(Instant.parse("2026-09-28T10:00:00Z"));
			stages.computeIfAbsent(registry + "/" + stage, key -> new ArrayList<>()).add(metadata);
		}

		/** What the storage layer has already done by the time an EXIT/DELETED event is dispatched. */
		void deleteReport(String registry, String stage, String objectId) {
			reports.remove(registry + "/" + stage + "/" + objectId);
			stages.getOrDefault(registry + "/" + stage, new ArrayList<>())
					.removeIf(metadata -> objectId.equals(metadata.getObjectId()));
		}

		/** An object carrying a fingerprint, in one stage of the reviewed registry. */
		void reviewed(String registry, String stage, String objectId, String fingerprint) {
			ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
			metadata.setObjectId(objectId);
			metadata.setFingerprint(fingerprint);
			stages.computeIfAbsent(registry + "/" + stage, key -> new ArrayList<>()).add(metadata);
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args) {
			switch (method.getName()) {
			case "getScopeName":
				return "jena";
			case "getContentFromStageForRegistry":
				return reports.get(args[0] + "/" + args[1] + "/" + args[2]);
			case "listInStageForRegistry":
				return stages.getOrDefault(args[0] + "/" + args[1], List.of());
			case "updateDiagnosticsInStageForRegistry":
				@SuppressWarnings("unchecked")
				List<Diagnostic> roots = (List<Diagnostic>) args[4];
				writes.add(new Write((String) args[0], (String) args[1], (String) args[2], (String) args[3],
						List.copyOf(roots)));
				Promise<ObjectMetadata> written = Promises
						.resolved(ManagementFactory.eINSTANCE.createObjectMetadata());
				return written;
			case "toString":
				return "fake scope";
			case "hashCode":
				return System.identityHashCode(proxy);
			case "equals":
				return proxy == args[0];
			default:
				throw new UnsupportedOperationException(method.getName());
			}
		}
	}

	/**
	 * A data-category reference, the way a review records one: an id in the context's
	 * {@code data-categories} taxonomy. The ids are the names the {@code DataCategory} enum had.
	 */
	static CategoryRef categoryRef(String categoryId) {
		CategoryRef ref = ContextFactory.eINSTANCE.createCategoryRef();
		ref.setContextId("gdpr");
		ref.setTaxonomyId("data-categories");
		ref.setCategoryId(categoryId);
		return ref;
	}

}
