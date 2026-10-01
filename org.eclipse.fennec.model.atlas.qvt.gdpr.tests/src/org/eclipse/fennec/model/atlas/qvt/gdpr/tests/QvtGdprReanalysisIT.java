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
package org.eclipse.fennec.model.atlas.qvt.gdpr.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.eclipse.fennec.model.gdprReport.GdprReport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * A transformation report is only as current as the reviews it was derived from, so a review that
 * arrives, is corrected or is withdrawn has to re-derive every transformation resting on it
 * (issue #319, WP4).
 * <p>
 * The case that matters most is the last one: a transformation resting on a retracted review is
 * worse than one resting on none, because it still looks complete.
 */
@ExtendWith(TempDirExtension.class)
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@DisplayName("Re-analysis when a review a transformation rests on changes")
public class QvtGdprReanalysisIT {

	private static final String SCOPE_FILTER = "(atlas.scope=" + TestAnnotations.SCOPE_NAME + ")";
	private static final String DRAFT = CommonTestAnnotations.STAGE_DRAFT;
	private static final String APPROVED = CommonTestAnnotations.STAGE_APPROVED;

	@Test
	@TestAnnotations.QvtGdprSetup
	@DisplayName("a review arriving after the unit completes an analysis that came back incomplete")
	public void aLateReviewCompletesTheAnalysis(
			@InjectService(cardinality = 0, timeout = 30000, filter = SCOPE_FILTER) //
			ServiceAware<WritableScopeService> aware) throws Exception {

		WritableScopeService<EObject> scope = scope(aware);
		String fingerprint = Fixtures.storeModel(scope, DRAFT);
		Fixtures.storeUnit(scope, Fixtures.unit(fingerprint), DRAFT);

		// Nothing has reviewed the metamodel yet, so the first analysis is honestly incomplete.
		GdprReport first = Fixtures.await(() -> Fixtures.derivedReport(scope, DRAFT), found -> found != null,
				"the first transformation report");
		assertNull(Fixtures.restedOn(first).getReportId(),
				"the entry is listed with its fingerprint and no reportId, which is how a report says so");

		// The review the analysis was waiting for. It is found by exactly the fingerprint the
		// incomplete report listed.
		Fixtures.storeReport(scope, Fixtures.review("review-1", fingerprint, "2026-10-01T08:00:00Z"), DRAFT);

		GdprReport completed = Fixtures.await(() -> Fixtures.derivedReport(scope, DRAFT),
				found -> found != null && Fixtures.restedOn(found).getReportId() != null,
				"the analysis to be derived again now that a review exists");
		assertEquals("review-1", Fixtures.restedOn(completed).getReportId());
		assertTrue(!first.getReportId().equals(completed.getReportId()),
				"a changed input is a new revision of the judgement, not an overwrite of the old one");
	}

	@Test
	@TestAnnotations.QvtGdprSetup
	@DisplayName("a review arriving one stage up reaches a transformation in an earlier stage")
	public void aReviewReachesTransformationsThatCanSeeIt(
			@InjectService(cardinality = 0, timeout = 30000, filter = SCOPE_FILTER) //
			ServiceAware<WritableScopeService> aware) throws Exception {

		WritableScopeService<EObject> scope = scope(aware);
		String fingerprint = Fixtures.storeModel(scope, APPROVED);
		Fixtures.storeUnit(scope, Fixtures.unit(fingerprint), DRAFT);
		Fixtures.await(() -> Fixtures.derivedReport(scope, DRAFT), found -> found != null, "the first report");

		// The review lands in approved; the transformation sits in draft and can see it.
		Fixtures.storeReport(scope, Fixtures.review("review-1", fingerprint, "2026-10-01T08:00:00Z"), APPROVED);

		GdprReport completed = Fixtures.await(() -> Fixtures.derivedReport(scope, DRAFT),
				found -> found != null && Fixtures.restedOn(found).getReportId() != null,
				"the draft transformation to be re-derived from the approved review");
		assertEquals("review-1", Fixtures.restedOn(completed).getReportId());
	}

	@Test
	@TestAnnotations.QvtGdprSetup
	@DisplayName("a withdrawn review leaves no transformation claiming it still holds")
	public void aWithdrawnReviewIsNoticed(
			@InjectService(cardinality = 0, timeout = 30000, filter = SCOPE_FILTER) //
			ServiceAware<WritableScopeService> aware) throws Exception {

		WritableScopeService<EObject> scope = scope(aware);
		String fingerprint = Fixtures.storeModel(scope, DRAFT);
		Fixtures.storeReport(scope, Fixtures.review("review-1", fingerprint, "2026-10-01T08:00:00Z"), DRAFT);
		Fixtures.storeUnit(scope, Fixtures.unit(fingerprint), DRAFT);
		Fixtures.await(() -> Fixtures.derivedReport(scope, DRAFT),
				found -> found != null && Fixtures.restedOn(found).getReportId() != null,
				"an analysis resting on the review");

		scope.deleteFromStageForRegistry(TestAnnotations.REPORT_REGISTRY, DRAFT, "review-1").getValue();

		// Without this the transformation would go on looking complete, which is worse than
		// looking incomplete.
		GdprReport after = Fixtures.await(() -> Fixtures.derivedReport(scope, DRAFT),
				found -> found != null && Fixtures.restedOn(found).getReportId() == null,
				"the analysis to be derived again without the withdrawn review");
		assertNotNull(after);
	}

	@Test
	@TestAnnotations.QvtGdprSetup
	@DisplayName("a review of another revision leaves the transformation alone")
	public void onlyTheRevisionThatChangedIsStale(
			@InjectService(cardinality = 0, timeout = 30000, filter = SCOPE_FILTER) //
			ServiceAware<WritableScopeService> aware) throws Exception {

		WritableScopeService<EObject> scope = scope(aware);
		String fingerprint = Fixtures.storeModel(scope, DRAFT);
		Fixtures.storeUnit(scope, Fixtures.unit(fingerprint), DRAFT);
		GdprReport first = Fixtures.await(() -> Fixtures.derivedReport(scope, DRAFT), found -> found != null,
				"the first report");

		// A review of a different revision of the same metamodel. What a review is of is a
		// revision, so a report that rested on another one is not stale because this one appeared.
		Fixtures.storeReport(scope, Fixtures.review("review-other", "fp1:someotherrevision",
				"2026-10-01T08:00:00Z"), DRAFT);

		Fixtures.settle();
		GdprReport unchanged = Fixtures.reportById(scope, DRAFT, first.getReportId());
		assertNotNull(unchanged, "the report that was there is still the report that is there");
		assertNull(Fixtures.restedOn(unchanged).getReportId());
	}

	@Test
	@TestAnnotations.QvtGdprSetup
	@DisplayName("storing a transformation report does not set the re-analysis going again")
	public void theLoopTerminates(
			@InjectService(cardinality = 0, timeout = 30000, filter = SCOPE_FILTER) //
			ServiceAware<WritableScopeService> aware) throws Exception {

		WritableScopeService<EObject> scope = scope(aware);
		String fingerprint = Fixtures.storeModel(scope, DRAFT);
		Fixtures.storeUnit(scope, Fixtures.unit(fingerprint), DRAFT);
		Fixtures.await(() -> Fixtures.derivedReport(scope, DRAFT), found -> found != null, "the first report");

		// A re-analysis writes a transformation report into the very registry that triggers this
		// action. It stops because the action answers only for a review of a PACKAGE - a rule,
		// not an accident of the fingerprint schemes. If it did not, this would not terminate.
		Fixtures.storeReport(scope, Fixtures.review("review-1", fingerprint, "2026-10-01T08:00:00Z"), DRAFT);
		Fixtures.await(() -> Fixtures.derivedReport(scope, DRAFT),
				found -> found != null && Fixtures.restedOn(found).getReportId() != null, "the re-analysis");

		// Two: the incomplete one and the one derived once the review existed. They are successive
		// revisions of a judgement, which is why a re-analysis writes rather than replaces.
		Fixtures.settle();
		int afterOneRound = Fixtures.transformationReports(scope, DRAFT);
		assertEquals(2, afterOneRound, "one report per revision of the judgement");

		// And it stays there. Each of those writes fired this same action again; it stopped because
		// the action answers only for a review of a package.
		Fixtures.settle();
		assertEquals(afterOneRound, Fixtures.transformationReports(scope, DRAFT),
				"a re-analysis writes into the registry that triggers it, so a loop would keep adding reports");
	}

	/* ------------------------------------------------------------------ helpers */

	@SuppressWarnings("unchecked")
	private static WritableScopeService<EObject> scope(ServiceAware<WritableScopeService> aware) throws Exception {
		WritableScopeService<EObject> scope = aware.waitForService(30000);
		assertNotNull(scope, "the test scope service must come up");
		return scope;
	}
}
