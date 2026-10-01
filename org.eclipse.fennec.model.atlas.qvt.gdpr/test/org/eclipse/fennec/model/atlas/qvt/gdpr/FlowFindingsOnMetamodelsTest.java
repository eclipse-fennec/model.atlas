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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.fennec.m2x.model.compiled.CompiledUnit;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.qvt.gdpr.RuleCatalogue.Rule;
import org.eclipse.fennec.model.gdprReport.DataCategory;
import org.eclipse.fennec.model.gdprReport.GdprReport;
import org.eclipse.fennec.model.gdprReport.RelevanceLevelType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * What a transformation does to a metamodel, written onto that metamodel (WP3).
 * <p>
 * Against the real report the analyser derives from the probe's compiled unit, so the two halves
 * are tested together: a change to a rule that moved a finding to the wrong end of a flow fails
 * here rather than in a runtime.
 */
@DisplayName("A transformation's findings on the metamodels it touches")
public class FlowFindingsOnMetamodelsTest {

	private static final String COMMENT = "//Contact/comment";
	private static final String DIAGNOSIS = "//Patient/diagnosis";
	private static final String UNIT_FINGERPRINT = "m2x1:175c17b6cfc57e09e0881d32283049c357b250b400f8561253c5b5610fe18cc8";

	private static Map<String, List<Diagnostic>> projection;

	@BeforeAll
	static void deriveAndProject() throws Exception {
		CompiledUnit unit = QvtFlowAnalysisTest.readUnit();
		GdprReport report = FlowAnalysis.analyse(unit,
				Map.of(Reviews.CLINIC_FP, Reviews.clinic(), Reviews.CONTACTS_FP, Reviews.contacts()),
				Instant.parse("2026-09-30T09:00:00Z"));
		projection = FlowFindingsToDiagnostics.map(report);
	}

	@Test
	@DisplayName("the receiving model learns that its harmless free-text field holds health data")
	public void theTargetModelLearnsWhatItReceives() {
		Diagnostic root = root(Reviews.CONTACTS_NS);
		assertEquals(FlowFindingsToDiagnostics.CODE_TRANSFORMATION, root.getCode());
		assertTrue(root.getMessage().startsWith("clinic2contacts:"),
				"the producer is per transformation, so the root says which one: " + root.getMessage());
		assertEquals(DiagnosticSeverity.WARNING, root.getSeverity());

		// The case the whole feature exists for, seen from the model that has to live with it.
		Diagnostic aggregation = element(root, Rule.AGGREGATION, COMMENT);
		assertEquals("SPECIAL_CATEGORY (HIGH)", aggregation.getMessage());
		Diagnostic claim = aggregation.getChildren().get(0);
		assertEquals("gdpr.finding.SPECIAL_CATEGORY.HIGH", claim.getCode());
		assertTrue(claim.getMessage().contains(DIAGNOSIS), claim.getMessage());
		assertTrue(claim.getMessage().contains("Evidence: ") && claim.getMessage().contains("Art.9(1)"),
				"the citations of all three combined fields came from the clinic review and travel with the "
						+ "finding: " + claim.getMessage());

		assertNotNull(element(root, Rule.STRUCTURE_LOSS, COMMENT), "comment cannot carry what it receives");
		assertNotNull(element(root, Rule.PROPAGATION, COMMENT), "and it holds at least what arrives");

		// //Patient/id is a DIRECT_IDENTIFIER; the contacts review calls //Contact/reference
		// PERSONAL_DATA. Whoever maintains contacts is the one who decides which holds.
		Diagnostic disagreement = element(root, Rule.TARGET_DISAGREEMENT, "//Contact/reference");
		assertEquals("DIRECT_IDENTIFIER (HIGH)", disagreement.getMessage());
	}

	@Test
	@DisplayName("the source model learns that its classified field is flattened into prose elsewhere")
	public void theSourceModelLearnsWhereItsDataGoes() {
		Diagnostic root = root(Reviews.CLINIC_NS);

		Diagnostic flattened = element(root, Rule.STRUCTURE_LOSS, DIAGNOSIS);
		assertEquals("SPECIAL_CATEGORY (HIGH)", flattened.getMessage());
		assertTrue(flattened.getChildren().get(0).getMessage().contains(COMMENT),
				"and it says where: " + flattened.getChildren().get(0).getMessage());

		// The purpose is the clinic reviewer's, so the question of whether this new processing is
		// compatible with it is theirs too.
		Diagnostic purpose = element(root, Rule.PURPOSE_NOT_CARRIED, DIAGNOSIS);
		assertTrue(purpose.getChildren().get(0).getMessage().contains(Reviews.DIAGNOSIS_PURPOSE),
				purpose.getChildren().get(0).getMessage());

		// One node about the model as a whole: a statement about a set of fields has no single
		// field to hang on, and its code makes it unique under the root all the same.
		Diagnostic notPropagated = element(root, Rule.NOT_PROPAGATED, null);
		assertEquals(DiagnosticSeverity.INFO, notPropagated.getSeverity(),
				"evidence for a minimisation argument is not a risk");
		// LOW and not NONE: the synthetic evaluation this hangs on states no relevance of its own,
		// and an EMF enum nobody set reads as its first literal - which here is the report model's
		// "examined and nothing of concern found".
		assertEquals("gdpr.finding.SPECIAL_CATEGORY.LOW", notPropagated.getChildren().get(0).getCode());
		assertTrue(notPropagated.getChildren().get(0).getMessage().contains("//Patient/birthDate"),
				notPropagated.getChildren().get(0).getMessage());
	}

	@Test
	@DisplayName("a field carries its own classification, never the set's")
	public void theSourceBadgeIsTheFieldsOwnCategory() {
		Diagnostic clinic = root(Reviews.CLINIC_NS);

		// The aggregation and the structure loss are about the SET of three fields, and the set is
		// SPECIAL_CATEGORY because the diagnosis is in it. Projected onto one contributing field,
		// that category would say //Patient/fullName is health data - which the clinic review does
		// not say, and which is the kind of badge that gets escalated. What travels is the field's
		// own classification; what arrives in //Contact/comment is the set's.
		assertEquals("DIRECT_IDENTIFIER (HIGH)", element(clinic, Rule.STRUCTURE_LOSS, "//Patient/fullName")
				.getMessage());
		assertEquals("QUASI_IDENTIFIER (MEDIUM)", element(clinic, Rule.STRUCTURE_LOSS, "//Patient/postcode")
				.getMessage());
		assertEquals("SPECIAL_CATEGORY (HIGH)", element(clinic, Rule.STRUCTURE_LOSS, DIAGNOSIS).getMessage(),
				"and the one that really is special-category keeps it");

		// The receiving field is the other case: what lands in it is everything at once.
		assertEquals("SPECIAL_CATEGORY (HIGH)",
				element(root(Reviews.CONTACTS_NS), Rule.STRUCTURE_LOSS, COMMENT).getMessage());
	}

	@Test
	@DisplayName("a statement for one end is not written on the other")
	public void eachEndGetsOnlyWhatIsAboutIt() {
		Diagnostic clinic = root(Reviews.CLINIC_NS);
		Diagnostic contacts = root(Reviews.CONTACTS_NS);

		// A model read by several transformations would otherwise collect one PROPAGATION per flow
		// of each of them, which buries the findings that ask for a decision.
		assertTrue(codes(clinic).stream().noneMatch(code -> code.equals(Rule.PROPAGATION.code())),
				"propagation is a statement for the receiver");
		assertTrue(codes(clinic).stream().noneMatch(code -> code.equals(Rule.AGGREGATION.code())));
		assertTrue(codes(clinic).stream().noneMatch(code -> code.equals(Rule.TARGET_DISAGREEMENT.code())));

		assertTrue(codes(contacts).stream().noneMatch(code -> code.equals(Rule.PURPOSE_NOT_CARRIED.code())),
				"the purpose belongs to whoever stated it");
		assertTrue(codes(contacts).stream().noneMatch(code -> code.equals(Rule.NOT_PROPAGATED.code())));

		// Structure loss is the one both ends act on, and what each would do differs.
		assertNotNull(element(clinic, Rule.STRUCTURE_LOSS, DIAGNOSIS));
		assertNotNull(element(contacts, Rule.STRUCTURE_LOSS, COMMENT));
	}

	@Test
	@DisplayName("no two nodes under one parent share a code and a target")
	public void nothingSilentlyReplacesAnythingElse() {
		// A diagnostic's id is derived from its code and its target, so a duplicate pair under one
		// parent is not a duplicate row - it is one finding quietly overwriting another.
		for (List<Diagnostic> roots : projection.values()) {
			roots.forEach(FlowFindingsOnMetamodelsTest::assertAddressesAreUnique);
		}
	}

	@Test
	@DisplayName("every node names the compiled revision it came from")
	public void theRevisionIsRecordedWithoutOwningTheFindings() {
		// The producer is keyed by the qualified name, not by the fingerprint: keyed by the
		// fingerprint, every recompile would strand the previous revision's findings on the model
		// with nothing able to clear them. The revision goes here instead.
		for (List<Diagnostic> roots : projection.values()) {
			for (Diagnostic node : flatten(roots)) {
				assertEquals(UNIT_FINGERPRINT, node.getSource(), node.getCode());
			}
		}
	}

	@Test
	@DisplayName("a model the report names but says nothing about is present with an empty list")
	public void aModelWithNothingToSayIsStillAnswered() {
		GdprReport report = FlowAnalysis.analyse(unit(), Map.of(), Instant.parse("2026-09-30T09:00:00Z"));
		Map<String, List<Diagnostic>> nothing = FlowFindingsToDiagnostics.map(report);

		assertEquals(Set.of(Reviews.CLINIC_NS, Reviews.CONTACTS_NS), nothing.keySet(),
				"both models are answered for, or a previous analysis's findings would never be cleared");
		assertTrue(nothing.get(Reviews.CLINIC_NS).isEmpty(),
				"an empty list clears; an empty root would read as a transformation examined and found harmless");
	}

	@Test
	@DisplayName("a finding whose rule nobody recognises is written on both ends, not nowhere")
	public void anUnknownRuleIsNotDropped() {
		// A report may have been written by hand or restored from a backup. Not knowing which end
		// a statement belongs to is no reason to write it nowhere.
		GdprReport report = FlowAnalysis.analyse(unit(),
				Map.of(Reviews.CLINIC_FP, Reviews.clinic(), Reviews.CONTACTS_FP, Reviews.contacts()),
				Instant.parse("2026-09-30T09:00:00Z"));
		report.getEvaluation().stream().flatMap(evaluation -> evaluation.getFindings().stream())
				.filter(finding -> finding.getId().startsWith(Rule.PROPAGATION.code()))
				.forEach(finding -> finding.setId("something-a-person-typed"));

		Map<String, List<Diagnostic>> mapped = FlowFindingsToDiagnostics.map(report);
		assertTrue(codes(mapped.get(Reviews.CLINIC_NS).get(0)).stream()
				.anyMatch(code -> code.contains("something-a-person-typed")),
				"the source end has it now, where a recognised propagation would not have gone");
		assertTrue(codes(mapped.get(Reviews.CONTACTS_NS).get(0)).stream()
				.anyMatch(code -> code.contains("something-a-person-typed")), "and so does the target end");
	}

	@Test
	@DisplayName("a review of a metamodel is not a transformation report and yields nothing")
	public void aPackageReviewIsNotProjected() {
		assertTrue(FlowFindingsToDiagnostics.map(Reviews.clinic()).isEmpty(),
				"only a TransformationSubject says anything about other models");
	}

	@Test
	@DisplayName("a finding that says the field is clean is not written at all")
	public void nothingOfConcernIsNotADiagnostic() {
		GdprReport report = FlowAnalysis.analyse(unit(),
				Map.of(Reviews.CLINIC_FP, Reviews.clinic(), Reviews.CONTACTS_FP, Reviews.contacts()),
				Instant.parse("2026-09-30T09:00:00Z"));
		report.getEvaluation().stream().flatMap(evaluation -> evaluation.getFindings().stream())
				.forEach(finding -> {
					finding.setCategory(DataCategory.NOT_PERSONAL_DATA);
					finding.setRelevanceLevel(RelevanceLevelType.NONE);
				});
		report.getCombinations().forEach(combination -> {
			combination.setCategory(DataCategory.NOT_PERSONAL_DATA);
			combination.setRelevanceLevel(RelevanceLevelType.NONE);
		});

		Map<String, List<Diagnostic>> mapped = FlowFindingsToDiagnostics.map(report);
		assertTrue(mapped.values().stream().allMatch(List::isEmpty),
				"NONE relevance is the report model's 'examined and nothing of concern found'");
	}

	/* ------------------------------------------------------------------ helpers */

	private static CompiledUnit unit() {
		try {
			return QvtFlowAnalysisTest.readUnit();
		} catch (Exception e) {
			throw new AssertionError("the compiled unit fixture has to be readable", e);
		}
	}

	private static Diagnostic root(String nsURI) {
		List<Diagnostic> roots = projection.get(nsURI);
		assertNotNull(roots, "the report names " + nsURI);
		assertEquals(1, roots.size(), "one root per producer, so a reader collapses it to one row");
		return roots.get(0);
	}

	private static Diagnostic element(Diagnostic root, Rule rule, String target) {
		List<Diagnostic> found = root.getChildren().stream()
				.filter(child -> rule.code().equals(child.getCode())
						&& (target == null ? child.getTarget() == null : target.equals(child.getTarget())))
				.toList();
		assertEquals(1, found.size(), "exactly one " + rule.code() + " node at " + target);
		assertFalse(found.get(0).getChildren().isEmpty(), "an element node with no claim says nothing");
		return found.get(0);
	}

	private static List<String> codes(Diagnostic root) {
		return root.getChildren().stream().map(Diagnostic::getCode).toList();
	}

	private static void assertAddressesAreUnique(Diagnostic node) {
		Set<String> seen = new HashSet<>();
		for (Diagnostic child : node.getChildren()) {
			assertTrue(seen.add(child.getCode() + "@" + child.getTarget()),
					"two children of " + node.getCode() + " share code and target: " + child.getCode() + " @ "
							+ child.getTarget());
			assertAddressesAreUnique(child);
		}
	}

	private static List<Diagnostic> flatten(List<Diagnostic> nodes) {
		List<Diagnostic> all = new ArrayList<>();
		for (Diagnostic node : nodes) {
			all.add(node);
			all.addAll(flatten(node.getChildren()));
		}
		return all;
	}
}
