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

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.eclipse.fennec.model.gdprReport.GdprReport;
import org.eclipse.fennec.model.gdprReport.PackageSubject;
import org.eclipse.fennec.model.gdprReport.TransformationSubject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * A compiled unit stored in the transformation registry has to produce a GDPR report about the
 * transformation, resting on the reviews of the metamodels its manifest pins - <b>wherever in the
 * stage ladder those metamodels actually are</b> - and that report has to leave findings on them.
 * <p>
 * Nothing here touches the actions directly: they are Private-Package, and a deployment reaches
 * them only through the workflow, so the tests do too.
 */
@ExtendWith(TempDirExtension.class)
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@DisplayName("A GDPR report derived for a compiled transformation")
public class QvtGdprFlowIT {

	private static final String SCOPE_FILTER = "(atlas.scope=" + TestAnnotations.SCOPE_NAME + ")";
	private static final String DRAFT = CommonTestAnnotations.STAGE_DRAFT;
	private static final String APPROVED = CommonTestAnnotations.STAGE_APPROVED;
	private static final String RELEASE = CommonTestAnnotations.STAGE_RELEASE;

	@Test
	@TestAnnotations.QvtGdprSetup
	@DisplayName("a unit finds the review of a metamodel that lives one stage up")
	public void aUnitRestsOnAReviewFromAnotherStage(
			@InjectService(cardinality = 0, timeout = 30000, filter = SCOPE_FILTER) //
			ServiceAware<WritableScopeService> aware) throws Exception {

		WritableScopeService<EObject> scope = scope(aware);
		// The ordinary shape of a managed workflow: the metamodel has been promoted and reviewed,
		// the transformation that reads it is still being worked on.
		String fingerprint = Fixtures.storeModel(scope, APPROVED);
		Fixtures.storeReport(scope, Fixtures.review("review-approved", fingerprint, "2026-10-01T08:00:00Z"),
				APPROVED);

		Fixtures.storeUnit(scope, Fixtures.unit(fingerprint), DRAFT);

		GdprReport report = Fixtures.await(() -> Fixtures.derivedReport(scope, DRAFT), found -> found != null,
				"the transformation report");
		TransformationSubject subject = (TransformationSubject) report.getSubject();
		assertEquals(Fixtures.UNIT_NAME, subject.getQualifiedName());
		assertEquals(Fixtures.UNIT_FINGERPRINT, subject.getSubjectFingerprint());

		PackageSubject rested = only(subject);
		assertEquals(fingerprint, rested.getSubjectFingerprint());
		assertEquals("review-approved", rested.getReportId(),
			"the review is in approved and the unit in draft; looking only in draft would call this "
					+ "metamodel unreviewed, which is false");
	}

	@Test
	@TestAnnotations.QvtGdprSetup
	@DisplayName("the nearest stage answers, even when a later one was reviewed more recently")
	public void theNearestStageWins(
			@InjectService(cardinality = 0, timeout = 30000, filter = SCOPE_FILTER) //
			ServiceAware<WritableScopeService> aware) throws Exception {

		WritableScopeService<EObject> scope = scope(aware);
		String fingerprint = Fixtures.storeModel(scope, DRAFT);
		// Same revision, reviewed in both stages; the approved one is a month newer and still loses.
		Fixtures.storeReport(scope, Fixtures.review("review-draft", fingerprint, "2026-09-01T08:00:00Z"), DRAFT);
		Fixtures.storeReport(scope, Fixtures.review("review-approved", fingerprint, "2026-10-01T08:00:00Z"),
				APPROVED);

		Fixtures.storeUnit(scope, Fixtures.unit(fingerprint), DRAFT);

		GdprReport report = Fixtures.await(() -> Fixtures.derivedReport(scope, DRAFT), found -> found != null,
				"the transformation report");
		assertEquals("review-draft", only((TransformationSubject) report.getSubject()).getReportId(),
				"the ladder is a statement about which stage is authoritative; recency cannot outrank it");
	}

	@Test
	@TestAnnotations.QvtGdprSetup
	@DisplayName("a released unit never rests on a draft review")
	public void visibilityRunsOneWay(
			@InjectService(cardinality = 0, timeout = 30000, filter = SCOPE_FILTER) //
			ServiceAware<WritableScopeService> aware) throws Exception {

		WritableScopeService<EObject> scope = scope(aware);
		String fingerprint = Fixtures.storeModel(scope, DRAFT);
		Fixtures.storeReport(scope, Fixtures.review("review-draft", fingerprint, "2026-10-01T08:00:00Z"), DRAFT);

		Fixtures.storeUnit(scope, Fixtures.unit(fingerprint), RELEASE);

		GdprReport report = Fixtures.await(() -> Fixtures.derivedReport(scope, RELEASE), found -> found != null,
				"the transformation report");
		assertNull(only((TransformationSubject) report.getSubject()).getReportId(),
				"the final stage of the chain points at the parent scope, never back down its own ladder");
	}

	@Test
	@TestAnnotations.QvtGdprSetup
	@DisplayName("the findings land on the metamodel in the stage that holds it")
	public void findingsLandOnTheModelWhereverItIs(
			@InjectService(cardinality = 0, timeout = 30000, filter = SCOPE_FILTER) //
			ServiceAware<WritableScopeService> aware) throws Exception {

		WritableScopeService<EObject> scope = scope(aware);
		String fingerprint = Fixtures.storeModel(scope, APPROVED);

		// A transformation report filed in draft, about a metamodel that lives in approved.
		Fixtures.storeReport(scope, Fixtures.transformationReport("flow-1", fingerprint), DRAFT);

		List<Diagnostic> roots = Fixtures.await(() -> Fixtures.owned(scope, APPROVED), found -> !found.isEmpty(),
				"the transformation's findings on the metamodel");
		assertEquals(1, roots.size(), "one root per producer");
		Diagnostic root = roots.get(0);
		assertEquals("gdpr.transformation", root.getCode());
		assertEquals(Fixtures.UNIT_FINGERPRINT, root.getSource(),
				"the revision is recorded on the node, never in the producer");
		assertTrue(root.getMessage().startsWith(Fixtures.UNIT_NAME + ":"), root.getMessage());

		assertTrue(Fixtures.owned(scope, DRAFT).isEmpty(),
				"and not onto the stage the report happened to be filed in");
	}

	@Test
	@TestAnnotations.QvtGdprSetup
	@DisplayName("a withdrawn transformation report takes its findings with it")
	public void aDeletedReportClearsItsFindings(
			@InjectService(cardinality = 0, timeout = 30000, filter = SCOPE_FILTER) //
			ServiceAware<WritableScopeService> aware) throws Exception {

		WritableScopeService<EObject> scope = scope(aware);
		String fingerprint = Fixtures.storeModel(scope, APPROVED);
		Fixtures.storeReport(scope, Fixtures.transformationReport("flow-1", fingerprint), DRAFT);
		Fixtures.await(() -> Fixtures.owned(scope, APPROVED), found -> !found.isEmpty(), "the findings");

		scope.deleteFromStageForRegistry(TestAnnotations.REPORT_REGISTRY, DRAFT, "flow-1").getValue();

		Fixtures.await(() -> Fixtures.owned(scope, APPROVED), List::isEmpty,
				"the withdrawn report's findings to be cleared");
	}

	@Test
	@TestAnnotations.QvtGdprSetup
	@DisplayName("a metamodel nobody reviewed is listed, with its reportId unset")
	public void anUnreviewedMetamodelIsSaidInTheSubject(
			@InjectService(cardinality = 0, timeout = 30000, filter = SCOPE_FILTER) //
			ServiceAware<WritableScopeService> aware) throws Exception {

		WritableScopeService<EObject> scope = scope(aware);
		String fingerprint = Fixtures.storeModel(scope, DRAFT);

		Fixtures.storeUnit(scope, Fixtures.unit(fingerprint), DRAFT);

		GdprReport report = Fixtures.await(() -> Fixtures.derivedReport(scope, DRAFT), found -> found != null,
				"the transformation report");
		PackageSubject rested = only((TransformationSubject) report.getSubject());
		assertEquals(fingerprint, rested.getSubjectFingerprint(),
				"the package is still listed, with the revision a review would have had to be of");
		assertNull(rested.getReportId(),
				"reportId unset IS the statement that the analysis is incomplete rather than clean");
	}

	/* ------------------------------------------------------------------ helpers */

	private static PackageSubject only(TransformationSubject subject) {
		assertEquals(1, subject.getSourcePackages().size(), "the manifest pins exactly one metamodel");
		return subject.getSourcePackages().get(0);
	}

	@SuppressWarnings("unchecked")
	private static WritableScopeService<EObject> scope(ServiceAware<WritableScopeService> aware) throws Exception {
		WritableScopeService<EObject> scope = aware.waitForService(30000);
		assertNotNull(scope, "the test scope service must come up");
		return scope;
	}
}
