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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.fennec.model.atlas.mgmt.diagnostics.Diagnostics;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.compliance.context.CategoryRef;
import org.eclipse.fennec.model.compliance.context.ContextFactory;
import org.eclipse.fennec.model.compliance.report.CombinationFinding;
import org.eclipse.fennec.model.compliance.report.Evidence;
import org.eclipse.fennec.model.compliance.report.FeatureEvaluation;
import org.eclipse.fennec.model.compliance.report.Finding;
import org.eclipse.fennec.model.compliance.report.FlowEvaluation;
import org.eclipse.fennec.model.compliance.report.ReportFactory;
import org.eclipse.fennec.model.compliance.report.ComplianceReport;
import org.eclipse.fennec.model.compliance.report.RelevanceLevel;
import org.eclipse.fennec.model.compliance.report.TransformationSubject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A derived report says several different things about one field, and each has to survive as its
 * own node (issue #319).
 * <p>
 * A review's findings on one feature are several signals for one verdict, and folding them into a
 * single claim is what this mapper is for. A transformation report is the other case: its findings
 * on one flow come from different rules, and two of them sharing a category and a relevance is
 * ordinary rather than a sign they are the same claim. Folded, the second silently replaced the
 * first - a diagnostic's id is derived from its code and its target, so they minted one id.
 */
@DisplayName("A derived report's several statements about one element")
public class TransformationReportNodesTest {

	private static final String REPORT_ID = "gdpr-fp1-abc-de-20260918-120000";

	private static final ReportFactory REPORTS = ReportFactory.eINSTANCE;

	private static final String CLINIC = "http://example.org/clinic/1.0.0";
	private static final String CONTACTS = "http://example.org/contacts/1.0.0";
	private static final String COMMENT = "//Contact/comment";

	private final GdprFindingsToDiagnostics mapper = new GdprFindingsToDiagnostics();

	@Test
	@DisplayName("two rules about one flow are two nodes, not one paragraph")
	public void findingsOfDifferentRulesDoNotFold() {
		ComplianceReport report = REPORTS.createComplianceReport();
		report.setSubject(subject());
		FlowEvaluation flow = flow("//Patient/id", "//Contact/reference");
		// Both DIRECT_IDENTIFIER at HIGH, and they say completely different things.
		flow.getFindings().add(finding("gdpr.flow.propagation:one", "DIRECT_IDENTIFIER",
				"It reaches //Contact/reference."));
		flow.getFindings().add(finding("gdpr.flow.target-disagreement:one", "DIRECT_IDENTIFIER",
				"The target review calls that field PERSONAL_DATA."));
		report.getEvaluations().add(flow);

		Diagnostic root = mapper.map(report, REPORT_ID).get(0);
		assertEquals(List.of("gdpr.flow.propagation", "gdpr.flow.target-disagreement"), codes(root),
				"the finding's id declares which node it belongs under");
		String propagation = claim(root, "gdpr.flow.propagation").getMessage();
		String disagreement = claim(root, "gdpr.flow.target-disagreement").getMessage();
		assertTrue(propagation.startsWith("It reaches //Contact/reference."), propagation);
		assertTrue(disagreement.startsWith("The target review calls that field PERSONAL_DATA."), disagreement);
		// Folded, the two rationales ran into one another in a single message under a single id.
		assertTrue(!propagation.contains("PERSONAL_DATA"), propagation);
		assertTrue(!disagreement.contains("It reaches"), disagreement);
	}

	@Test
	@DisplayName("three combinations over one set of flows are three nodes with three ids")
	public void combinationsOverOneSetDoNotCollide() {
		ComplianceReport report = REPORTS.createComplianceReport();
		report.setSubject(subject());
		FlowEvaluation first = flow("//Patient/fullName", COMMENT);
		FlowEvaluation second = flow("//Patient/diagnosis", COMMENT);
		report.getEvaluations().add(first);
		report.getEvaluations().add(second);
		for (String rule : List.of("aggregation", "structure-loss", "target-disagreement")) {
			report.getCombinations().add(combination("gdpr.flow." + rule + ":toContact#" + COMMENT, first, second));
		}

		List<Diagnostic> roots = mapper.map(report, REPORT_ID);
		Diagnostic root = roots.get(0);
		assertEquals(List.of("gdpr.flow.aggregation", "gdpr.flow.structure-loss", "gdpr.flow.target-disagreement"),
				codes(root), "one node per kind of statement, all about the same pair of flows");

		Diagnostics.prepare("gdpr.review", roots, Instant.parse("2026-09-30T09:00:00Z"));
		Set<String> ids = new HashSet<>();
		for (Diagnostic node : root.getChildren()) {
			assertTrue(ids.add(node.getId()),
					"two nodes minted one id, so all but one is lost: " + node.getCode() + " -> " + node.getId());
		}
	}

	@Test
	@DisplayName("a review's findings on one feature still fold into one claim, as they always did")
	public void aReviewIsUnchanged() {
		ComplianceReport report = REPORTS.createComplianceReport();
		report.setSubject(REPORTS.createPackageSubject());
		((org.eclipse.fennec.model.compliance.report.PackageSubject) report.getSubject()).setNsURI(CLINIC);
		FeatureEvaluation feature = REPORTS.createFeatureEvaluation();
		feature.setUriFragment("//Patient/postcode");
		// Two signals for one verdict, with ids that declare nothing. This is the case folding was
		// written for and it must keep behaving exactly as before.
		feature.getFindings().add(finding("F-001", "QUASI_IDENTIFIER", "The name says postcode."));
		feature.getFindings().add(finding("F-002", "QUASI_IDENTIFIER", "Its type is a short string."));
		report.getEvaluations().add(feature);

		Diagnostic root = mapper.map(report, REPORT_ID).get(0);
		assertEquals(List.of("gdpr.feature"), codes(root));
		assertEquals(1, root.getChildren().get(0).getChildren().size(), "one claim, both signals in it");
		assertTrue(root.getChildren().get(0).getChildren().get(0).getMessage().contains("The name says postcode."));
		assertTrue(root.getChildren().get(0).getChildren().get(0).getMessage().contains("Its type is a short string."));
	}

	/* ------------------------------------------------------------------ fixtures */

	private static TransformationSubject subject() {
		TransformationSubject subject = REPORTS.createTransformationSubject();
		subject.setQualifiedName("clinic2contacts");
		subject.setSubjectFingerprint("m2x1:unit");
		subject.getSourcePackages().add(packageEntry(CLINIC));
		subject.getTargetPackages().add(packageEntry(CONTACTS));
		return subject;
	}

	private static org.eclipse.fennec.model.compliance.report.PackageSubject packageEntry(String nsURI) {
		org.eclipse.fennec.model.compliance.report.PackageSubject entry = REPORTS.createPackageSubject();
		entry.setNsURI(nsURI);
		entry.setSubjectFingerprint("fp1:" + nsURI);
		entry.setReportId("review-of-" + nsURI);
		return entry;
	}

	private static FlowEvaluation flow(String sourceFeature, String targetFeature) {
		FlowEvaluation flow = REPORTS.createFlowEvaluation();
		flow.setId("toContact#" + sourceFeature + "->" + targetFeature);
		flow.setMapping("toContact");
		flow.setSourceNsURI(CLINIC);
		flow.setSourceFeature(sourceFeature);
		flow.setTargetNsURI(CONTACTS);
		flow.setTargetFeature(targetFeature);
		return flow;
	}

	private static Finding finding(String id, String category, String rationale) {
		Finding finding = REPORTS.createFinding();
		finding.setId(id);
		finding.getCategories().add(categoryRef(category));
		finding.setRelevanceLevel(RelevanceLevel.HIGH);
		finding.setRationale(rationale);
		finding.getEvidence().add(evidence());
		return finding;
	}

	private static CombinationFinding combination(String id, FlowEvaluation... flows) {
		CombinationFinding combination = REPORTS.createCombinationFinding();
		combination.setId(id);
		combination.getCategories().add(categoryRef("SPECIAL_CATEGORY"));
		combination.setRelevanceLevel(RelevanceLevel.HIGH);
		combination.getCategories().add(combinationKind("LINKAGE"));
		combination.setRationale("What " + id + " says.");
		combination.getEvidence().add(evidence());
		for (FlowEvaluation flow : flows) {
			combination.getFeatures().add(flow);
		}
		return combination;
	}

	private static Evidence evidence() {
		Evidence evidence = REPORTS.createEvidence();
		evidence.setCitationId("Art.4(1)");
		evidence.setQuote("any information relating to an identified or identifiable natural person");
		return evidence;
	}

	private static List<String> codes(Diagnostic root) {
		return root.getChildren().stream().map(Diagnostic::getCode).sorted().toList();
	}

	private static Diagnostic claim(Diagnostic root, String code) {
		return root.getChildren().stream().filter(child -> code.equals(child.getCode())).findFirst().orElseThrow()
				.getChildren().get(0);
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


	/**
	 * How a combination combines, as a review records it: a reference into the context's
	 * {@code combination-kinds} taxonomy, whose ids are the names the {@code CombinationKind} enum
	 * had. It rides in the same list as the data categories and is told from them by its taxonomy.
	 */
	static CategoryRef combinationKind(String kindId) {
		CategoryRef ref = ContextFactory.eINSTANCE.createCategoryRef();
		ref.setContextId("gdpr");
		ref.setTaxonomyId("combination-kinds");
		ref.setCategoryId(kindId);
		return ref;
	}

}
