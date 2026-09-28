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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.eclipse.fennec.model.atlas.mgmt.diagnostics.Diagnostics;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.gdprReport.ClassifierEvaluation;
import org.eclipse.fennec.model.gdprReport.CombinationFinding;
import org.eclipse.fennec.model.gdprReport.CombinationKind;
import org.eclipse.fennec.model.gdprReport.ConfidenceType;
import org.eclipse.fennec.model.gdprReport.DataCategory;
import org.eclipse.fennec.model.gdprReport.Evidence;
import org.eclipse.fennec.model.gdprReport.FeatureEvaluation;
import org.eclipse.fennec.model.gdprReport.Finding;
import org.eclipse.fennec.model.gdprReport.FlowEvaluation;
import org.eclipse.fennec.model.gdprReport.GDPRReportFactory;
import org.eclipse.fennec.model.gdprReport.GdprReport;
import org.eclipse.fennec.model.gdprReport.RelevanceLevelType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The mapping table of {@link GdprFindingsToDiagnostics}: what a review's findings become on the
 * reviewed object, and above all which of them are <em>the same finding seen again</em>.
 */
class GdprFindingsToDiagnosticsTest {

	private static final Instant NOW = Instant.parse("2026-09-28T10:00:00Z");
	private static final GDPRReportFactory FACTORY = GDPRReportFactory.eINSTANCE;

	private final GdprFindingsToDiagnostics mapper = new GdprFindingsToDiagnostics();

	@Test
	@DisplayName("A feature with one finding becomes one root carrying one child, addressed by the fragment")
	void oneFindingOnOneFeature() {
		GdprReport report = report();
		FeatureEvaluation feature = feature("//Patient/category");
		feature.getFindings().add(finding("F-001", DataCategory.SPECIAL_CATEGORY, RelevanceLevelType.HIGH,
				ConfidenceType.HIGH, "The literals enumerate religious denominations.", "Establish an Art.9(2) basis.",
				"Art.9(1)"));
		classifier(report, "//Patient", feature);

		List<Diagnostic> roots = mapper.map(report);

		assertEquals(1, roots.size());
		Diagnostic root = roots.get(0);
		assertEquals(GdprFindingsToDiagnostics.CODE_FEATURE, root.getCode());
		assertEquals("//Patient/category", root.getTarget());
		assertEquals(DiagnosticSeverity.WARNING, root.getSeverity());
		assertEquals("compliance", root.getCategory());
		assertEquals("claude-sonnet-4-6", root.getSource(), "the source is what produced the review");
		assertEquals("GDPR review: SPECIAL_CATEGORY (HIGH)", root.getMessage());

		assertEquals(1, root.getChildren().size());
		Diagnostic child = root.getChildren().get(0);
		assertEquals("gdpr.finding.SPECIAL_CATEGORY.HIGH", child.getCode());
		assertEquals("//Patient/category", child.getTarget(), "a child is about the same element as its root");
		assertEquals(DiagnosticSeverity.WARNING, child.getSeverity());
		assertTrue(child.getMessage().startsWith("The literals enumerate religious denominations."),
				child.getMessage());
		assertTrue(child.getMessage().contains("Recommendation: Establish an Art.9(2) basis."), child.getMessage());
		assertTrue(child.getMessage().contains("Evidence: Art.9(1)"), child.getMessage());
		assertTrue(child.getMessage().contains("Confidence: HIGH"), child.getMessage());
	}

	@Test
	@DisplayName("Two findings of one category and relevance on one feature are one claim; two categories are two")
	void findingsFoldByCategoryAndRelevance() {
		GdprReport report = report();
		FeatureEvaluation feature = feature("//Person/firstName");
		feature.getFindings().add(finding("F-001", DataCategory.PERSONAL_DATA, RelevanceLevelType.MEDIUM,
				ConfidenceType.HIGH, "Reached by the feature name.", null, "Art.4(1)"));
		feature.getFindings().add(finding("F-002", DataCategory.PERSONAL_DATA, RelevanceLevelType.MEDIUM,
				ConfidenceType.LOW, "Reached by the owning classifier.", null, "Rec.26"));
		feature.getFindings().add(finding("F-003", DataCategory.QUASI_IDENTIFIER, RelevanceLevelType.MEDIUM,
				ConfidenceType.MEDIUM, "Contributes to singling out.", null, "Rec.26"));
		classifier(report, "//Person", feature);

		List<Diagnostic> roots = mapper.map(report);

		assertEquals(1, roots.size());
		Diagnostic root = roots.get(0);
		assertEquals(2, root.getChildren().size(), "same category and relevance fold, a different category does not");
		Diagnostic folded = root.getChildren().get(0);
		assertEquals("gdpr.finding.PERSONAL_DATA.MEDIUM", folded.getCode());
		assertTrue(folded.getMessage().contains("Reached by the feature name."), folded.getMessage());
		assertTrue(folded.getMessage().contains("Reached by the owning classifier."), folded.getMessage());
		assertTrue(folded.getMessage().contains("Evidence: Art.4(1), Rec.26"), folded.getMessage());
		assertTrue(folded.getMessage().contains("Confidence: LOW"),
				"folding never makes a claim surer than the least sure review that reached it: " + folded.getMessage());
		assertEquals("gdpr.finding.QUASI_IDENTIFIER.MEDIUM", root.getChildren().get(1).getCode());
		assertEquals("GDPR review: PERSONAL_DATA (MEDIUM), QUASI_IDENTIFIER (MEDIUM)", root.getMessage());
	}

	@Test
	@DisplayName("A finding that asserts nothing is not written, and an examined-and-clean feature gets no root")
	void cleanFindingsAreNotWritten() {
		GdprReport report = report();
		FeatureEvaluation examinedAndClean = feature("//Patient/fullName");
		FeatureEvaluation none = feature("//Patient/roomNumber");
		none.getFindings()
				.add(finding("F-001", DataCategory.PERSONAL_DATA, RelevanceLevelType.NONE, null, "nothing", null));
		FeatureEvaluation notPersonal = feature("//Patient/currency");
		notPersonal.getFindings().add(
				finding("F-002", DataCategory.NOT_PERSONAL_DATA, RelevanceLevelType.MEDIUM, null, "a currency", null));
		FeatureEvaluation anonymous = feature("//Patient/bucket");
		anonymous.getFindings()
				.add(finding("F-003", DataCategory.ANONYMOUS, RelevanceLevelType.LOW, null, "aggregated", null));
		classifier(report, "//Patient", examinedAndClean, none, notPersonal, anonymous);

		assertEquals(List.of(), mapper.map(report),
				"no gdpr.review diagnostic on an object is what 'nothing was found' looks like");
	}

	@Test
	@DisplayName("LOW informs, MEDIUM and HIGH warn, and the root carries the worst of its children")
	void severityMapping() {
		GdprReport report = report();
		FeatureEvaluation low = feature("//P/a");
		low.getFindings().add(finding("F-1", DataCategory.PERSONAL_DATA, RelevanceLevelType.LOW, null, "r", null));
		FeatureEvaluation mixed = feature("//P/b");
		mixed.getFindings().add(finding("F-2", DataCategory.PERSONAL_DATA, RelevanceLevelType.LOW, null, "r", null));
		mixed.getFindings()
				.add(finding("F-3", DataCategory.DIRECT_IDENTIFIER, RelevanceLevelType.HIGH, null, "r", null));
		classifier(report, "//P", low, mixed);

		List<Diagnostic> roots = mapper.map(report);

		assertEquals(DiagnosticSeverity.INFO, roots.get(0).getSeverity());
		assertEquals(DiagnosticSeverity.INFO, roots.get(0).getChildren().get(0).getSeverity());
		assertEquals(DiagnosticSeverity.WARNING, roots.get(1).getSeverity(), "the root wears the worst badge");
		assertEquals(DiagnosticSeverity.INFO, roots.get(1).getChildren().get(0).getSeverity());
		assertEquals(DiagnosticSeverity.WARNING, roots.get(1).getChildren().get(1).getSeverity());
		assertTrue(roots.stream().noneMatch(root -> root.getSeverity() == DiagnosticSeverity.ERROR),
				"a review never produces an ERROR: that would say the check did not run");
	}

	@Test
	@DisplayName("The same claim reached differently keeps its id; a changed category or relevance is a new one")
	void identityTurnsOnElementCategoryAndRelevance() {
		String first = idOfOnlyChild(oneFinding("//Person/firstName", DataCategory.PERSONAL_DATA,
				RelevanceLevelType.MEDIUM, "reached by name", "F-001"));
		String reworded = idOfOnlyChild(oneFinding("//Person/firstName", DataCategory.PERSONAL_DATA,
				RelevanceLevelType.MEDIUM, "reached by the owning classifier instead", "f1"));
		String recategorised = idOfOnlyChild(oneFinding("//Person/firstName", DataCategory.SPECIAL_CATEGORY,
				RelevanceLevelType.MEDIUM, "reached by name", "F-001"));
		String raised = idOfOnlyChild(oneFinding("//Person/firstName", DataCategory.PERSONAL_DATA,
				RelevanceLevelType.HIGH, "reached by name", "F-001"));
		String elsewhere = idOfOnlyChild(oneFinding("//Person/lastName", DataCategory.PERSONAL_DATA,
				RelevanceLevelType.MEDIUM, "reached by name", "F-001"));

		assertEquals(first, reworded,
				"another rationale, another Finding.id and another route are the same claim, so a person's "
						+ "decision about it stands");
		assertNotEquals(first, recategorised, "another category is another claim, to be looked at again");
		assertNotEquals(first, raised, "a raised relevance is another claim, to be looked at again");
		assertNotEquals(first, elsewhere, "the same claim about another element is another diagnostic");
	}

	@Test
	@DisplayName("A classifier's own findings get their own root, beside its features'")
	void classifierFindingsAreTheirOwnRoot() {
		GdprReport report = report();
		FeatureEvaluation feature = feature("//Patient/diagnosis");
		feature.getFindings()
				.add(finding("F-2", DataCategory.SPECIAL_CATEGORY, RelevanceLevelType.HIGH, null, "health", null));
		ClassifierEvaluation classifier = classifier(report, "//Patient", feature);
		classifier.getFindings()
				.add(finding("F-1", DataCategory.PERSONAL_DATA, RelevanceLevelType.LOW, null, "a person", null));

		List<Diagnostic> roots = mapper.map(report);

		assertEquals(2, roots.size());
		assertEquals(GdprFindingsToDiagnostics.CODE_CLASSIFIER, roots.get(0).getCode());
		assertEquals("//Patient", roots.get(0).getTarget());
		assertEquals(GdprFindingsToDiagnostics.CODE_FEATURE, roots.get(1).getCode());
		assertEquals("//Patient/diagnosis", roots.get(1).getTarget());
	}

	@Test
	@DisplayName("A combination is addressed by the elements it spans, sorted, and names its kind in the message")
	void combinationSpansItsElements() {
		GdprReport report = report();
		FeatureEvaluation street = feature("//Patient/street");
		FeatureEvaluation birthDate = feature("//Patient/birthDate");
		classifier(report, "//Patient", street, birthDate);
		CombinationFinding combination = FACTORY.createCombinationFinding();
		combination.setId("F-003");
		combination.setCategory(DataCategory.QUASI_IDENTIFIER);
		combination.setRelevanceLevel(RelevanceLevelType.HIGH);
		combination.setCombinationKind(CombinationKind.QUASI_IDENTIFIER_SET);
		combination.setRationale("Together they single out an individual.");
		combination.getEvidence().add(evidence("Rec.26"));
		// listed in the order the analyser happened to walk them
		combination.getFeatures().add(street);
		combination.getFeatures().add(birthDate);
		report.getCombinations().add(combination);

		List<Diagnostic> roots = mapper.map(report);

		assertEquals(1, roots.size(), "the participating features found nothing on their own");
		Diagnostic root = roots.get(0);
		assertEquals(GdprFindingsToDiagnostics.CODE_COMBINATION, root.getCode());
		assertEquals("//Patient/birthDate+//Patient/street", root.getTarget(),
				"sorted, so the id does not turn on the order the analyser listed them in");
		assertEquals("GDPR review, QUASI_IDENTIFIER_SET: QUASI_IDENTIFIER (HIGH)", root.getMessage());
		assertEquals(1, root.getChildren().size());
	}

	@Test
	@DisplayName("A flow is addressed by its mapping and the qualified path it carries the value along")
	void flowAddressing() {
		GdprReport report = report();
		FlowEvaluation flow = FACTORY.createFlowEvaluation();
		flow.setId("toContact:Patient.diagnosis->Contact.comment");
		flow.setMapping("toContact");
		flow.setSourceNsURI("http://example.org/clinic/1.0");
		flow.setSourceFeature("//Patient/diagnosis");
		flow.setTargetNsURI("http://example.org/contacts/1.0");
		flow.setTargetFeature("//Contact/comment");
		flow.getFindings()
				.add(finding("F-1", DataCategory.SPECIAL_CATEGORY, RelevanceLevelType.HIGH, null, "health", null));
		report.getEvaluation().add(flow);

		List<Diagnostic> roots = mapper.map(report);

		assertEquals(1, roots.size());
		assertEquals(GdprFindingsToDiagnostics.CODE_FLOW, roots.get(0).getCode());
		assertEquals("toContact:http://example.org/clinic/1.0#//Patient/diagnosis"
				+ "->http://example.org/contacts/1.0#//Contact/comment", roots.get(0).getTarget(),
				"the namespaces are part of it: two source models can carry the same fragment");
	}

	@Test
	@DisplayName("An element with findings but nothing to address it by is dropped rather than written untargeted")
	void unaddressableElementIsDropped() {
		GdprReport report = report();
		FeatureEvaluation nameless = FACTORY.createFeatureEvaluation();
		nameless.setId("Patient.mystery");
		nameless.getFindings()
				.add(finding("F-1", DataCategory.PERSONAL_DATA, RelevanceLevelType.HIGH, null, "r", null));
		classifier(report, "//Patient", nameless);

		assertEquals(List.of(), mapper.map(report),
				"an untargeted diagnostic is about the object as a whole and would collide with every other one");
	}

	@Test
	@DisplayName("A report with nothing in it maps to nothing, and so does no report at all")
	void emptyInputs() {
		assertEquals(List.of(), mapper.map(null));
		assertEquals(List.of(), mapper.map(report()));
	}

	/* ------------------------------------------------------------------ helpers */

	/** The id the child of a single-finding report would be stored under. */
	private String idOfOnlyChild(GdprReport report) {
		List<Diagnostic> roots = mapper.map(report);
		Diagnostics.prepare(GdprFindingsToDiagnostics.PRODUCER, roots, NOW);
		return roots.get(0).getChildren().get(0).getId();
	}

	private static GdprReport oneFinding(String fragment, DataCategory category, RelevanceLevelType relevance,
			String rationale, String findingId) {
		GdprReport report = report();
		FeatureEvaluation feature = feature(fragment);
		feature.getFindings().add(finding(findingId, category, relevance, null, rationale, null));
		classifier(report, "//Person", feature);
		return report;
	}

	private static GdprReport report() {
		GdprReport report = FACTORY.createGdprReport();
		report.setGeneratedBy("claude-sonnet-4-6");
		return report;
	}

	private static ClassifierEvaluation classifier(GdprReport report, String fragment, FeatureEvaluation... features) {
		ClassifierEvaluation classifier = FACTORY.createClassifierEvaluation();
		classifier.setUriFragment(fragment);
		for (FeatureEvaluation feature : features) {
			classifier.getFeatureEvaluation().add(feature);
		}
		report.getEvaluation().add(classifier);
		return classifier;
	}

	private static FeatureEvaluation feature(String fragment) {
		FeatureEvaluation feature = FACTORY.createFeatureEvaluation();
		feature.setUriFragment(fragment);
		return feature;
	}

	private static Finding finding(String id, DataCategory category, RelevanceLevelType relevance,
			ConfidenceType confidence, String rationale, String recommendation, String... citations) {
		Finding finding = FACTORY.createFinding();
		finding.setId(id);
		finding.setCategory(category);
		finding.setRelevanceLevel(relevance);
		finding.setConfidence(confidence);
		finding.setRationale(rationale);
		finding.setRecommendation(recommendation);
		for (String citation : citations) {
			if (citation != null) {
				finding.getEvidence().add(evidence(citation));
			}
		}
		return finding;
	}

	private static Evidence evidence(String citationId) {
		Evidence evidence = FACTORY.createEvidence();
		evidence.setCitationId(citationId);
		evidence.setQuote("...");
		return evidence;
	}
}
