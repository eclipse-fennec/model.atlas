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
package org.eclipse.fennec.model.atlas.gdpr.diagnostics.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticStatus;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.eclipse.fennec.model.gdprReport.DataCategory;
import org.eclipse.fennec.model.gdprReport.RelevanceLevelType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * A review stored in the report registry has to leave its findings on the metadata of the package
 * it is about, and take them away again when it is withdrawn.
 * <p>
 * Nothing here touches the action directly - it is Private-Package, and a deployment reaches it
 * only through the workflow, so the test does too.
 */
@ExtendWith(TempDirExtension.class)
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@DisplayName("GDPR review findings on the reviewed object")
public class GdprMetadataDiagnosticsIT {

	private static final String SCOPE_FILTER = "(atlas.scope=" + TestAnnotations.SCOPE_NAME + ")";
	private static final String DRAFT = CommonTestAnnotations.STAGE_DRAFT;
	private static final String APPROVED = CommonTestAnnotations.STAGE_APPROVED;

	@Test
	@TestAnnotations.GdprDiagnosticsSetup
	@DisplayName("a review of a stored package leaves its findings on that package's metadata")
	public void findingsLandOnTheReviewedPackage(
			@InjectService(cardinality = 0, timeout = 30000, filter = SCOPE_FILTER) //
			ServiceAware<WritableScopeService> aware) throws Exception {

		WritableScopeService<EObject> scope = scope(aware);
		String fingerprint = Fixtures.storePackage(scope, DRAFT);

		Fixtures.storeReview(scope,
				Fixtures.review("report-1", fingerprint, DataCategory.QUASI_IDENTIFIER, RelevanceLevelType.MEDIUM),
				DRAFT);

		List<Diagnostic> roots = Fixtures.await(scope, DRAFT, found -> found.size() == 1, "the review's findings");
		Diagnostic root = roots.get(0);
		assertEquals("gdpr.review", root.getCode(), "one root per producer, so a viewer collapses it to one row");
		assertNull(root.getTarget(), "the review is about the object as a whole");
		assertEquals(DiagnosticSeverity.WARNING, root.getSeverity());
		assertEquals("compliance", root.getCategory());
		assertEquals(DiagnosticStatus.OPEN, root.getStatus());
		assertNotNull(root.getCreatedTime());
		assertEquals("GDPR review: 1 finding on 1 element", root.getMessage());

		assertEquals(1, root.getChildren().size());
		Diagnostic element = root.getChildren().get(0);
		assertEquals("gdpr.feature", element.getCode());
		assertEquals(Fixtures.REVIEWED_FEATURE, element.getTarget());
		assertEquals(DiagnosticSeverity.WARNING, element.getSeverity());

		assertEquals(1, element.getChildren().size());
		Diagnostic child = element.getChildren().get(0);
		assertEquals("gdpr.finding.QUASI_IDENTIFIER.MEDIUM", child.getCode());
		assertEquals(Fixtures.PRODUCER, child.getProducer(), "every node carries the root's producer");
		assertTrue(child.getMessage().contains("date of birth"), child.getMessage());
	}

	@Test
	@TestAnnotations.GdprDiagnosticsSetup
	@DisplayName("a review only reaches the stage it was carried out at")
	public void aReviewDoesNotReachAnotherStagesCopy(
			@InjectService(cardinality = 0, timeout = 30000, filter = SCOPE_FILTER) //
			ServiceAware<WritableScopeService> aware) throws Exception {

		WritableScopeService<EObject> scope = scope(aware);
		// the package is in draft; the review is filed against approved, where no copy of it is
		String fingerprint = Fixtures.storePackage(scope, DRAFT);

		Fixtures.storeReview(scope,
				Fixtures.review("report-1", fingerprint, DataCategory.SPECIAL_CATEGORY, RelevanceLevelType.HIGH),
				APPROVED);

		Fixtures.settle();
		assertTrue(Fixtures.owned(scope, DRAFT).isEmpty(),
				"one stage's verdict must not be written onto another stage's object");
		assertTrue(Fixtures.owned(scope, APPROVED).isEmpty(), "and there is nothing in approved to write it onto");
	}

	@Test
	@TestAnnotations.GdprDiagnosticsSetup
	@DisplayName("a withdrawn review takes its own findings with it and leaves another producer's alone")
	public void aDeletedReviewClearsItsOwnFindings(
			@InjectService(cardinality = 0, timeout = 30000, filter = SCOPE_FILTER) //
			ServiceAware<WritableScopeService> aware) throws Exception {

		WritableScopeService<EObject> scope = scope(aware);
		String fingerprint = Fixtures.storePackage(scope, DRAFT);
		Fixtures.storeReview(scope,
				Fixtures.review("report-1", fingerprint, DataCategory.QUASI_IDENTIFIER, RelevanceLevelType.MEDIUM),
				DRAFT);
		Fixtures.await(scope, DRAFT, found -> found.size() == 1, "the review's findings");
		// somebody else has something to say about the same package
		scope.updateDiagnosticsInStageForRegistry(CommonTestAnnotations.SCHEMA_REGISTRY_NAME, DRAFT,
				Fixtures.PACKAGE_ID, "some-other-producer", List.of(other())).getValue();

		scope.deleteFromStageForRegistry(TestAnnotations.REPORT_REGISTRY, DRAFT, "report-1").getValue();

		Fixtures.await(scope, DRAFT, List::isEmpty, "the withdrawn review's findings to be cleared");
		ObjectMetadata metadata = Fixtures.metadata(scope, DRAFT);
		assertEquals(1, metadata.getDiagnostics().size());
		assertEquals("some-other-producer", metadata.getDiagnostics().get(0).getProducer(),
				"a producer only ever clears its own roots");
	}

	@Test
	@TestAnnotations.GdprDiagnosticsSetup
	@DisplayName("the write leaves the object's own metadata untouched")
	public void theWriteIsNotAChangeToTheObject(
			@InjectService(cardinality = 0, timeout = 30000, filter = SCOPE_FILTER) //
			ServiceAware<WritableScopeService> aware) throws Exception {

		WritableScopeService<EObject> scope = scope(aware);
		String fingerprint = Fixtures.storePackage(scope, DRAFT);
		ObjectMetadata before = Fixtures.metadata(scope, DRAFT);
		String contentHash = before.getContentHash();
		String version = before.getVersion();
		java.time.Instant lastChange = before.getLastChangeTime();

		Fixtures.storeReview(scope,
				Fixtures.review("report-1", fingerprint, DataCategory.QUASI_IDENTIFIER, RelevanceLevelType.MEDIUM),
				DRAFT);
		Fixtures.await(scope, DRAFT, found -> found.size() == 1, "the review's findings");

		ObjectMetadata after = Fixtures.metadata(scope, DRAFT);
		// A diagnostic is something the Atlas found out about the object, not a change somebody
		// made to it - and the REST client's drift detection depends on that distinction.
		assertEquals(fingerprint, after.getFingerprint());
		assertEquals(contentHash, after.getContentHash());
		assertEquals(version, after.getVersion());
		assertEquals(lastChange, after.getLastChangeTime());
	}

	@Test
	@TestAnnotations.GdprDiagnosticsSetup
	@DisplayName("a re-review keeps a decision a person made, and a changed category asks again")
	public void aReReviewKeepsAHumanDecision(
			@InjectService(cardinality = 0, timeout = 30000, filter = SCOPE_FILTER) //
			ServiceAware<WritableScopeService> aware) throws Exception {

		WritableScopeService<EObject> scope = scope(aware);
		String fingerprint = Fixtures.storePackage(scope, DRAFT);
		Fixtures.storeReview(scope,
				Fixtures.review("report-1", fingerprint, DataCategory.QUASI_IDENTIFIER, RelevanceLevelType.MEDIUM),
				DRAFT);
		Diagnostic first = Fixtures.await(scope, DRAFT, found -> found.size() == 1, "the first review's findings")
				.get(0);
		String acknowledgedId = first.getChildren().get(0).getChildren().get(0).getId();
		// a data protection officer has seen the claim and accepts it for now
		acknowledge(scope, acknowledgedId);

		// the same claim, reached again and worded differently
		Fixtures.storeReview(scope,
				Fixtures.review("report-2", fingerprint, DataCategory.QUASI_IDENTIFIER, RelevanceLevelType.MEDIUM),
				DRAFT);
		Fixtures.await(scope, DRAFT,
				found -> "gdpr.finding.QUASI_IDENTIFIER.MEDIUM".equals(childCode(found))
						&& DiagnosticStatus.ACKNOWLEDGED == claim(found).getStatus(),
				"the person's decision to survive a re-review of the same claim");

		// a third review of the same feature at another category is another claim
		Fixtures.storeReview(scope,
				Fixtures.review("report-3", fingerprint, DataCategory.SPECIAL_CATEGORY, RelevanceLevelType.HIGH),
				DRAFT);
		Diagnostic child = claim(Fixtures.await(scope, DRAFT,
				found -> "gdpr.finding.SPECIAL_CATEGORY.HIGH".equals(childCode(found)),
				"a changed category to become a new claim"));
		assertEquals(DiagnosticStatus.OPEN, child.getStatus(), "a changed judgement has to be looked at again");
		assertTrue(!acknowledgedId.equals(child.getId()), "and it is a different diagnostic, not the old one reopened");
	}

	@Test
	@TestAnnotations.GdprDiagnosticsSetup
	@DisplayName("two reviews of one revision: the object shows the later one, and withdrawing it falls back")
	public void theLatestReviewSpeaksForTheRevision(
			@InjectService(cardinality = 0, timeout = 30000, filter = SCOPE_FILTER) //
			ServiceAware<WritableScopeService> aware) throws Exception {

		WritableScopeService<EObject> scope = scope(aware);
		String fingerprint = Fixtures.storePackage(scope, DRAFT);
		// an agent's review of a revision, and a person's correction of the same revision - the
		// ordinary case, and the one the review document holds as two revisions
		Fixtures.storeReview(scope, Fixtures.review("report-ai", fingerprint, DataCategory.PERSONAL_DATA,
				RelevanceLevelType.MEDIUM, "2026-09-28T08:00:00Z"), DRAFT);
		Fixtures.await(scope, DRAFT, found -> found.size() == 1, "the agent's findings");

		Fixtures.storeReview(scope, Fixtures.review("report-human", fingerprint, DataCategory.QUASI_IDENTIFIER,
				RelevanceLevelType.HIGH, "2026-09-28T09:00:00Z"), DRAFT);
		Fixtures.await(scope, DRAFT, found -> "gdpr.finding.QUASI_IDENTIFIER.HIGH".equals(childCode(found)),
				"the correction to supersede the agent's review");

		// withdrawing the LATEST review: the agent's is the only one left, so the object goes back
		// to saying what it says
		scope.deleteFromStageForRegistry(TestAnnotations.REPORT_REGISTRY, DRAFT, "report-human").getValue();
		Fixtures.await(scope, DRAFT, found -> "gdpr.finding.PERSONAL_DATA.MEDIUM".equals(childCode(found)),
				"the earlier review to speak for the revision again");

		// withdrawing the last one leaves nothing behind
		scope.deleteFromStageForRegistry(TestAnnotations.REPORT_REGISTRY, DRAFT, "report-ai").getValue();
		Fixtures.await(scope, DRAFT, List::isEmpty, "the last review's findings to go with it");
	}

	@Test
	@TestAnnotations.GdprDiagnosticsSetup
	@DisplayName("withdrawing the superseded review leaves the correction that replaced it standing")
	public void withdrawingTheSupersededReviewChangesNothing(
			@InjectService(cardinality = 0, timeout = 30000, filter = SCOPE_FILTER) //
			ServiceAware<WritableScopeService> aware) throws Exception {

		WritableScopeService<EObject> scope = scope(aware);
		String fingerprint = Fixtures.storePackage(scope, DRAFT);
		Fixtures.storeReview(scope, Fixtures.review("report-ai", fingerprint, DataCategory.PERSONAL_DATA,
				RelevanceLevelType.MEDIUM, "2026-09-28T08:00:00Z"), DRAFT);
		Fixtures.storeReview(scope, Fixtures.review("report-human", fingerprint, DataCategory.QUASI_IDENTIFIER,
				RelevanceLevelType.HIGH, "2026-09-28T09:00:00Z"), DRAFT);
		Fixtures.await(scope, DRAFT, found -> "gdpr.finding.QUASI_IDENTIFIER.HIGH".equals(childCode(found)),
				"the correction to supersede the agent's review");

		scope.deleteFromStageForRegistry(TestAnnotations.REPORT_REGISTRY, DRAFT, "report-ai").getValue();

		Fixtures.settle();
		assertEquals("gdpr.finding.QUASI_IDENTIFIER.HIGH", childCode(Fixtures.owned(scope, DRAFT)),
				"one review being withdrawn must not erase what another review still says");
	}

	/** The code of the one claim in the tree: root -> element -> claim; null if the shape differs. */
	private static String childCode(List<Diagnostic> roots) {
		if (roots.size() != 1 || roots.get(0).getChildren().size() != 1) {
			return null;
		}
		Diagnostic element = roots.get(0).getChildren().get(0);
		return element.getChildren().size() == 1 ? element.getChildren().get(0).getCode() : null;
	}

	/* ------------------------------------------------------------------ helpers */

	/** The one claim in the tree. */
	private static Diagnostic claim(List<Diagnostic> roots) {
		return roots.get(0).getChildren().get(0).getChildren().get(0);
	}

	private static void acknowledge(WritableScopeService<EObject> scope, String diagnosticId) throws Exception {
		ObjectMetadata metadata = Fixtures.metadata(scope, DRAFT);
		Diagnostic root = metadata.getDiagnostics().stream()
				.filter(candidate -> Fixtures.PRODUCER.equals(candidate.getProducer())).findFirst().orElseThrow();
		// Straight on the tree the producer owns, the way the diagnostic service edits one: the
		// version goes up, so the next automatic run reads it as an informed change on that node.
		Diagnostic copy = org.eclipse.emf.ecore.util.EcoreUtil.copy(root);
		setAcknowledged(copy, diagnosticId);
		scope.updateDiagnosticsInStageForRegistry(CommonTestAnnotations.SCHEMA_REGISTRY_NAME, DRAFT,
				Fixtures.PACKAGE_ID, Fixtures.PRODUCER, List.of(copy)).getValue();
	}

	private static void setAcknowledged(Diagnostic node, String diagnosticId) {
		if (diagnosticId.equals(node.getId())) {
			node.setStatus(DiagnosticStatus.ACKNOWLEDGED);
			node.setVersion(node.getVersion() + 1);
			return;
		}
		node.getChildren().forEach(child -> setAcknowledged(child, diagnosticId));
	}

	private static Diagnostic other() {
		Diagnostic diagnostic = ManagementFactory.eINSTANCE.createDiagnostic();
		diagnostic.setCode("something-else");
		diagnostic.setSeverity(DiagnosticSeverity.INFO);
		diagnostic.setMessage("not the review's business");
		return diagnostic;
	}

	@SuppressWarnings("unchecked")
	private static WritableScopeService<EObject> scope(ServiceAware<WritableScopeService> aware) throws Exception {
		WritableScopeService<EObject> scope = aware.waitForService(30000);
		assertNotNull(scope, "the test scope service must come up");
		return scope;
	}
}
