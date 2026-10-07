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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;

import org.eclipse.fennec.model.atlas.mgmt.diagnostics.Diagnostics;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.compliance.context.CategoryRef;
import org.eclipse.fennec.model.compliance.context.ContextFactory;
import org.eclipse.fennec.model.compliance.report.ClassifierEvaluation;
import org.eclipse.fennec.model.compliance.report.CombinationFinding;
import org.eclipse.fennec.model.compliance.report.Confidence;
import org.eclipse.fennec.model.compliance.report.Evidence;
import org.eclipse.fennec.model.compliance.report.FeatureEvaluation;
import org.eclipse.fennec.model.compliance.report.Finding;
import org.eclipse.fennec.model.compliance.report.FlowEvaluation;
import org.eclipse.fennec.model.compliance.report.ReportFactory;
import org.eclipse.fennec.model.compliance.report.ComplianceReport;
import org.eclipse.fennec.model.compliance.report.RelevanceLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The mapping table of {@link GdprFindingsToDiagnostics}: what a review's findings become on the
 * reviewed object, and above all which of them are <em>the same finding seen again</em>.
 */
class GdprFindingsToDiagnosticsTest {

	private static final Instant NOW = Instant.parse("2026-09-28T10:00:00Z");
	private static final ReportFactory FACTORY = ReportFactory.eINSTANCE;

	private final GdprFindingsToDiagnostics mapper = new GdprFindingsToDiagnostics();

	@Test
	@DisplayName("Everything the producer says hangs under one root, whatever it found")
	void oneRootPerProducer() {
		ComplianceReport report = report();
		FeatureEvaluation one = feature("//Patient/a");
		one.getFindings().add(finding("F-1", "PERSONAL_DATA", RelevanceLevel.LOW, null, "r", null));
		FeatureEvaluation two = feature("//Patient/b");
		two.getFindings().add(finding("F-2", "SPECIAL_CATEGORY", RelevanceLevel.HIGH, null, "r", null));
		ClassifierEvaluation classifier = classifier(report, "//Patient", one, two);
		classifier.getFindings()
				.add(finding("F-3", "PERSONAL_DATA", RelevanceLevel.MEDIUM, null, "r", null));

		List<Diagnostic> roots = mapper.map(report);

		assertEquals(1, roots.size(), "one root per producer, so a reader collapses it to one row");
		Diagnostic root = roots.get(0);
		assertEquals(GdprFindingsToDiagnostics.CODE_REVIEW, root.getCode());
		assertNull(root.getTarget(), "the review is about the object as a whole");
		assertEquals(DiagnosticSeverity.WARNING, root.getSeverity(), "the worst of everything beneath it");
		assertEquals("GDPR review: 3 findings on 3 elements", root.getMessage());
		assertEquals(3, root.getChildren().size(), "one node per element, under the one root");
	}

	@Test
	@DisplayName("A feature with one finding becomes one element node carrying one claim, addressed by the fragment")
	void oneFindingOnOneFeature() {
		ComplianceReport report = report();
		FeatureEvaluation feature = feature("//Patient/category");
		feature.getFindings().add(finding("F-001", "SPECIAL_CATEGORY", RelevanceLevel.HIGH,
				Confidence.HIGH, "The literals enumerate religious denominations.", "Establish an Art.9(2) basis.",
				"Art.9(1)"));
		classifier(report, "//Patient", feature);

		List<Diagnostic> elements = elements(report);

		assertEquals(1, elements.size());
		Diagnostic element = elements.get(0);
		assertEquals(GdprFindingsToDiagnostics.CODE_FEATURE, element.getCode());
		assertEquals("//Patient/category", element.getTarget());
		assertEquals(DiagnosticSeverity.WARNING, element.getSeverity());
		assertEquals("compliance", element.getCategory());
		assertEquals("claude-sonnet-4-6", element.getSource(), "the source is what produced the review");
		assertEquals("SPECIAL_CATEGORY (HIGH)", element.getMessage(),
				"no 'GDPR review' prefix: the root above already says whose finding this is");

		assertEquals(1, element.getChildren().size());
		Diagnostic child = element.getChildren().get(0);
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
		ComplianceReport report = report();
		FeatureEvaluation feature = feature("//Person/firstName");
		feature.getFindings().add(finding("F-001", "PERSONAL_DATA", RelevanceLevel.MEDIUM,
				Confidence.HIGH, "Reached by the feature name.", null, "Art.4(1)"));
		feature.getFindings().add(finding("F-002", "PERSONAL_DATA", RelevanceLevel.MEDIUM,
				Confidence.LOW, "Reached by the owning classifier.", null, "Rec.26"));
		feature.getFindings().add(finding("F-003", "QUASI_IDENTIFIER", RelevanceLevel.MEDIUM,
				Confidence.MEDIUM, "Contributes to singling out.", null, "Rec.26"));
		classifier(report, "//Person", feature);

		List<Diagnostic> elements = elements(report);

		assertEquals(1, elements.size());
		Diagnostic element = elements.get(0);
		assertEquals(2, element.getChildren().size(),
				"same category and relevance fold, a different category does not");
		Diagnostic folded = element.getChildren().get(0);
		assertEquals("gdpr.finding.PERSONAL_DATA.MEDIUM", folded.getCode());
		assertTrue(folded.getMessage().contains("Reached by the feature name."), folded.getMessage());
		assertTrue(folded.getMessage().contains("Reached by the owning classifier."), folded.getMessage());
		assertTrue(folded.getMessage().contains("Evidence: Art.4(1), Rec.26"), folded.getMessage());
		assertTrue(folded.getMessage().contains("Confidence: LOW"),
				"folding never makes a claim surer than the least sure review that reached it: " + folded.getMessage());
		assertEquals("gdpr.finding.QUASI_IDENTIFIER.MEDIUM", element.getChildren().get(1).getCode());
		assertEquals("PERSONAL_DATA (MEDIUM), QUASI_IDENTIFIER (MEDIUM)", element.getMessage());
	}

	@Test
	@DisplayName("A finding that asserts nothing is not written, and an examined-and-clean feature gets no root")
	void cleanFindingsAreNotWritten() {
		ComplianceReport report = report();
		FeatureEvaluation examinedAndClean = feature("//Patient/fullName");
		FeatureEvaluation none = feature("//Patient/roomNumber");
		none.getFindings()
				.add(finding("F-001", "PERSONAL_DATA", RelevanceLevel.NONE, null, "nothing", null));
		FeatureEvaluation notPersonal = feature("//Patient/currency");
		notPersonal.getFindings().add(
				finding("F-002", "NOT_PERSONAL_DATA", RelevanceLevel.MEDIUM, null, "a currency", null));
		FeatureEvaluation anonymous = feature("//Patient/bucket");
		anonymous.getFindings()
				.add(finding("F-003", "ANONYMOUS", RelevanceLevel.LOW, null, "aggregated", null));
		classifier(report, "//Patient", examinedAndClean, none, notPersonal, anonymous);

		Diagnostic clean = review(report);
		assertEquals(DiagnosticSeverity.INFO, clean.getSeverity());
		assertTrue(clean.getChildren().isEmpty(),
				"none of the four says anything, so the review has only its own verdict to give");
		assertTrue(clean.getMessage().contains("nothing of concern found"), clean.getMessage());
	}

	@Test
	@DisplayName("LOW informs, MEDIUM and HIGH warn, and every node carries the worst beneath it")
	void severityMapping() {
		ComplianceReport report = report();
		FeatureEvaluation low = feature("//P/a");
		low.getFindings().add(finding("F-1", "PERSONAL_DATA", RelevanceLevel.LOW, null, "r", null));
		FeatureEvaluation mixed = feature("//P/b");
		mixed.getFindings().add(finding("F-2", "PERSONAL_DATA", RelevanceLevel.LOW, null, "r", null));
		mixed.getFindings()
				.add(finding("F-3", "DIRECT_IDENTIFIER", RelevanceLevel.HIGH, null, "r", null));
		classifier(report, "//P", low, mixed);

		Diagnostic review = review(report);
		List<Diagnostic> elements = review.getChildren();

		assertEquals(DiagnosticSeverity.INFO, elements.get(0).getSeverity());
		assertEquals(DiagnosticSeverity.INFO, elements.get(0).getChildren().get(0).getSeverity());
		assertEquals(DiagnosticSeverity.WARNING, elements.get(1).getSeverity(), "the node wears the worst badge");
		assertEquals(DiagnosticSeverity.INFO, elements.get(1).getChildren().get(0).getSeverity());
		assertEquals(DiagnosticSeverity.WARNING, elements.get(1).getChildren().get(1).getSeverity());
		assertEquals(DiagnosticSeverity.WARNING, review.getSeverity(), "and the root the worst of all of them");
		assertTrue(Diagnostics.flatten(holding(review)).values().stream()
				.noneMatch(node -> node.getSeverity() == DiagnosticSeverity.ERROR),
				"a review never produces an ERROR: that would say the check did not run");
	}

	@Test
	@DisplayName("The same claim reached differently keeps its id; a changed category or relevance is a new one")
	void identityTurnsOnElementCategoryAndRelevance() {
		String first = idOfOnlyChild(oneFinding("//Person/firstName", "PERSONAL_DATA",
				RelevanceLevel.MEDIUM, "reached by name", "F-001"));
		String reworded = idOfOnlyChild(oneFinding("//Person/firstName", "PERSONAL_DATA",
				RelevanceLevel.MEDIUM, "reached by the owning classifier instead", "f1"));
		String recategorised = idOfOnlyChild(oneFinding("//Person/firstName", "SPECIAL_CATEGORY",
				RelevanceLevel.MEDIUM, "reached by name", "F-001"));
		String raised = idOfOnlyChild(oneFinding("//Person/firstName", "PERSONAL_DATA",
				RelevanceLevel.HIGH, "reached by name", "F-001"));
		String elsewhere = idOfOnlyChild(oneFinding("//Person/lastName", "PERSONAL_DATA",
				RelevanceLevel.MEDIUM, "reached by name", "F-001"));

		assertEquals(first, reworded,
				"another rationale, another Finding.id and another route are the same claim, so a person's "
						+ "decision about it stands");
		assertNotEquals(first, recategorised, "another category is another claim, to be looked at again");
		assertNotEquals(first, raised, "a raised relevance is another claim, to be looked at again");
		assertNotEquals(first, elsewhere, "the same claim about another element is another diagnostic");
	}

	@Test
	@DisplayName("A classifier's own findings get their own node, beside its features'")
	void classifierFindingsAreTheirOwnNode() {
		ComplianceReport report = report();
		FeatureEvaluation feature = feature("//Patient/diagnosis");
		feature.getFindings()
				.add(finding("F-2", "SPECIAL_CATEGORY", RelevanceLevel.HIGH, null, "health", null));
		ClassifierEvaluation classifier = classifier(report, "//Patient", feature);
		classifier.getFindings()
				.add(finding("F-1", "PERSONAL_DATA", RelevanceLevel.LOW, null, "a person", null));

		List<Diagnostic> elements = elements(report);

		assertEquals(2, elements.size());
		assertEquals(GdprFindingsToDiagnostics.CODE_CLASSIFIER, elements.get(0).getCode());
		assertEquals("//Patient", elements.get(0).getTarget());
		assertEquals(GdprFindingsToDiagnostics.CODE_FEATURE, elements.get(1).getCode());
		assertEquals("//Patient/diagnosis", elements.get(1).getTarget());
	}

	@Test
	@DisplayName("A combination is addressed by the elements it spans, sorted, and names its kind in the message")
	void combinationSpansItsElements() {
		ComplianceReport report = report();
		FeatureEvaluation street = feature("//Patient/street");
		FeatureEvaluation birthDate = feature("//Patient/birthDate");
		classifier(report, "//Patient", street, birthDate);
		CombinationFinding combination = FACTORY.createCombinationFinding();
		combination.setId("F-003");
		combination.getCategories().add(categoryRef("QUASI_IDENTIFIER"));
		combination.setRelevanceLevel(RelevanceLevel.HIGH);
		combination.getCategories().add(combinationKind("QUASI_IDENTIFIER_SET"));
		combination.setRationale("Together they single out an individual.");
		combination.getEvidence().add(evidence("Rec.26"));
		// listed in the order the analyser happened to walk them
		combination.getFeatures().add(street);
		combination.getFeatures().add(birthDate);
		report.getCombinations().add(combination);

		List<Diagnostic> elements = elements(report);

		assertEquals(1, elements.size(), "the participating features found nothing on their own");
		Diagnostic element = elements.get(0);
		assertEquals(GdprFindingsToDiagnostics.CODE_COMBINATION, element.getCode());
		assertEquals("//Patient/birthDate+//Patient/street", element.getTarget(),
				"sorted, so the id does not turn on the order the analyser listed them in");
		assertEquals("QUASI_IDENTIFIER_SET: QUASI_IDENTIFIER (HIGH)", element.getMessage());
		assertEquals(1, element.getChildren().size());
	}

	@Test
	@DisplayName("A flow is addressed by its mapping and the qualified path it carries the value along")
	void flowAddressing() {
		ComplianceReport report = report();
		FlowEvaluation flow = FACTORY.createFlowEvaluation();
		flow.setId("toContact:Patient.diagnosis->Contact.comment");
		flow.setMapping("toContact");
		flow.setSourceNsURI("http://example.org/clinic/1.0");
		flow.setSourceFeature("//Patient/diagnosis");
		flow.setTargetNsURI("http://example.org/contacts/1.0");
		flow.setTargetFeature("//Contact/comment");
		flow.getFindings()
				.add(finding("F-1", "SPECIAL_CATEGORY", RelevanceLevel.HIGH, null, "health", null));
		report.getEvaluations().add(flow);

		List<Diagnostic> elements = elements(report);

		assertEquals(1, elements.size());
		assertEquals(GdprFindingsToDiagnostics.CODE_FLOW, elements.get(0).getCode());
		assertEquals("toContact:http://example.org/clinic/1.0#//Patient/diagnosis"
				+ "->http://example.org/contacts/1.0#//Contact/comment", elements.get(0).getTarget(),
				"the namespaces are part of it: two source models can carry the same fragment");
	}

	@Test
	@DisplayName("A feature with no uriFragment is addressed by its classifier's fragment and its name")
	void aFeatureWithoutAFragmentFallsBackToItsName() {
		ComplianceReport report = report();
		FeatureEvaluation first = FACTORY.createFeatureEvaluation();
		first.setName("cardNumber");
		first.getFindings().add(finding("F-1", "DIRECT_IDENTIFIER", RelevanceLevel.HIGH, null, "r1", null));
		FeatureEvaluation second = FACTORY.createFeatureEvaluation();
		second.setName("holder");
		second.getFindings().add(finding("F-2", "PERSONAL_DATA", RelevanceLevel.HIGH, null, "r2", null));
		classifier(report, "//SubscriptionCard", first, second);

		// Both are written, and each is addressed in its own right: dropping them loses the
		// findings, and writing both untargeted would mint one id for the two.
		List<Diagnostic> elements = elements(report);
		assertEquals(2, elements.size());
		assertEquals("//SubscriptionCard/cardNumber", elements.get(0).getTarget());
		assertEquals("//SubscriptionCard/holder", elements.get(1).getTarget());
	}

	@Test
	@DisplayName("A classifier with no uriFragment is addressed by its name, and its features hang off that")
	void aClassifierWithoutAFragmentFallsBackToItsName() {
		ComplianceReport report = report();
		FeatureEvaluation feature = FACTORY.createFeatureEvaluation();
		feature.setName("diagnosis");
		feature.getFindings().add(finding("F-1", "SPECIAL_CATEGORY", RelevanceLevel.HIGH, null, "r", null));
		ClassifierEvaluation classifier = classifier(report, null, feature);
		classifier.setName("Patient");
		classifier.getFindings().add(finding("F-0", "PERSONAL_DATA", RelevanceLevel.MEDIUM, null, "r0", null));

		List<Diagnostic> elements = elements(report);
		assertEquals(2, elements.size());
		assertEquals("//Patient", elements.get(0).getTarget(),
				"the fragment a name implies, not the bare name: the target is an address");
		assertEquals("//Patient/diagnosis", elements.get(1).getTarget(),
				"and a feature is addressed under whatever its classifier resolved to");
	}

	@Test
	@DisplayName("An element with findings but nothing to address it by is dropped rather than written untargeted")
	void unaddressableElementIsDropped() {
		ComplianceReport report = report();
		// No uriFragment, no name and no id: there is genuinely nothing to address it by. An id
		// alone would be enough - it is unique, which is all the drop exists to protect - and a
		// name alone would give a fragment under its classifier.
		FeatureEvaluation nameless = FACTORY.createFeatureEvaluation();
		nameless.getFindings()
				.add(finding("F-1", "PERSONAL_DATA", RelevanceLevel.HIGH, null, "r", null));
		classifier(report, "//Patient", nameless);

		// an untargeted diagnostic is about the object as a whole and would collide with every
		// other one, so it is not written - but the review is not clean either, and the root says
		// which of the two happened
		Diagnostic root = review(report);
		assertEquals(DiagnosticSeverity.WARNING, root.getSeverity());
		assertTrue(root.getChildren().isEmpty());
		assertTrue(root.getMessage().contains("names an element"), root.getMessage());
	}

	@Test
	@DisplayName("No report at all maps to nothing; a report that found nothing says so")
	void emptyInputs() {
		// The two have to stay distinguishable on the object: no root means nobody reviewed it,
		// and a model reviewed and cleared must not look the same as one nobody looked at.
		assertEquals(List.of(), mapper.map(null));

		Diagnostic clean = review(report());
		assertEquals(GdprFindingsToDiagnostics.CODE_REVIEW, clean.getCode());
		assertEquals(DiagnosticSeverity.INFO, clean.getSeverity(), "a clean review is not a warning");
		assertTrue(clean.getChildren().isEmpty(),
				"no children: an empty root would read as a review that found something and lost it");
		assertTrue(clean.getMessage().contains("nothing of concern found"), clean.getMessage());
	}

	@Test
	@DisplayName("A clean review says how much it looked at")
	void aCleanReviewNamesWhatItExamined() {
		// "nothing found" after examining nothing and after clearing nineteen features are very
		// different statements, and only the second is reassuring.
		ComplianceReport report = report();
		ClassifierEvaluation classifier = FACTORY.createClassifierEvaluation();
		classifier.setUriFragment("//Person");
		for (String name : List.of("//Person/name", "//Person/age")) {
			FeatureEvaluation feature = FACTORY.createFeatureEvaluation();
			feature.setUriFragment(name);
			classifier.getFeatureEvaluations().add(feature);
		}
		report.getEvaluations().add(classifier);

		assertTrue(review(report).getMessage().contains("3 elements examined"),
				"the classifier and its two features: " + review(report).getMessage());
	}

	@Test
	@DisplayName("No review left on a revision is an error, not silence")
	void noReviewIsAnError() {
		// Withdrawing the last review does not make an object clean, it makes it unchecked - and
		// clearing the producer would leave it looking like one nobody has reviewed yet.
		List<Diagnostic> roots = mapper.noReview("fp1:clinic");
		assertEquals(1, roots.size(), "a producer has exactly one root");
		Diagnostic root = roots.get(0);
		assertEquals(GdprFindingsToDiagnostics.CODE_NO_REVIEW, root.getCode());
		assertEquals(DiagnosticSeverity.ERROR, root.getSeverity());
		assertTrue(root.getMessage().contains("fp1:clinic"),
				"it names the revision nobody reviewed: " + root.getMessage());
		assertTrue(root.getChildren().isEmpty(), "there is no review to carry anything from");
		assertNotEquals(GdprFindingsToDiagnostics.CODE_NO_REVIEW, review(report()).getCode(),
				"a review that found nothing is a different statement from no review at all");
	}

	/* ------------------------------------------------------------------ helpers */

	/** The one root the mapper produces. */
	private Diagnostic review(ComplianceReport report) {
		List<Diagnostic> roots = mapper.map(report);
		assertEquals(1, roots.size(), "a producer has exactly one root");
		return roots.get(0);
	}

	/** The element nodes under that root. */
	private List<Diagnostic> elements(ComplianceReport report) {
		return review(report).getChildren();
	}

	/** Metadata holding one tree, so Diagnostics can walk it. */
	private static ObjectMetadata holding(Diagnostic root) {
		ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadata.getDiagnostics().add(root);
		return metadata;
	}

	/** The id the one claim of a single-finding report would be stored under. */
	private String idOfOnlyChild(ComplianceReport report) {
		List<Diagnostic> roots = mapper.map(report);
		Diagnostics.prepare(GdprFindingsToDiagnostics.PRODUCER, roots, NOW);
		return roots.get(0).getChildren().get(0).getChildren().get(0).getId();
	}

	private static ComplianceReport oneFinding(String fragment, String category, RelevanceLevel relevance,
			String rationale, String findingId) {
		ComplianceReport report = report();
		FeatureEvaluation feature = feature(fragment);
		feature.getFindings().add(finding(findingId, category, relevance, null, rationale, null));
		classifier(report, "//Person", feature);
		return report;
	}

	private static ComplianceReport report() {
		ComplianceReport report = FACTORY.createComplianceReport();
		report.setGeneratedBy("claude-sonnet-4-6");
		return report;
	}

	private static ClassifierEvaluation classifier(ComplianceReport report, String fragment, FeatureEvaluation... features) {
		ClassifierEvaluation classifier = FACTORY.createClassifierEvaluation();
		classifier.setUriFragment(fragment);
		for (FeatureEvaluation feature : features) {
			classifier.getFeatureEvaluations().add(feature);
		}
		report.getEvaluations().add(classifier);
		return classifier;
	}

	private static FeatureEvaluation feature(String fragment) {
		FeatureEvaluation feature = FACTORY.createFeatureEvaluation();
		feature.setUriFragment(fragment);
		return feature;
	}

	private static Finding finding(String id, String category, RelevanceLevel relevance,
			Confidence confidence, String rationale, String recommendation, String... citations) {
		Finding finding = FACTORY.createFinding();
		finding.setId(id);
		finding.getCategories().add(categoryRef(category));
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
