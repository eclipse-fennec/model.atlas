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
package org.eclipse.fennec.model.atlas.gdpr.history;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import org.eclipse.fennec.model.compliance.context.CategoryRef;
import org.eclipse.fennec.model.compliance.context.ContextRef;
import org.eclipse.fennec.model.compliance.context.RequirementRef;
import org.eclipse.fennec.model.compliance.context.ContextFactory;
import org.eclipse.fennec.model.compliance.report.ClassifierEvaluation;
import org.eclipse.fennec.model.compliance.report.Confidence;
import org.eclipse.fennec.model.compliance.report.Evidence;
import org.eclipse.fennec.model.compliance.report.FeatureEvaluation;
import org.eclipse.fennec.model.compliance.report.Finding;
import org.eclipse.fennec.model.compliance.report.FindingResolution;
import org.eclipse.fennec.model.compliance.report.FlowEvaluation;
import org.eclipse.fennec.model.compliance.report.ReportFactory;
import org.eclipse.fennec.model.compliance.report.ComplianceReport;
import org.eclipse.fennec.model.compliance.report.ReportOrigin;
import org.eclipse.fennec.model.compliance.report.RelevanceLevel;

import org.eclipse.fennec.model.compliance.report.ReviewStatus;
import org.eclipse.fennec.model.compliance.report.RiskAssessment;
import org.eclipse.fennec.model.compliance.report.RiskTreatment;
import org.eclipse.fennec.model.compliance.report.TransformationSubject;
import org.eclipse.fennec.model.compliance.report.PackageSubject;
import org.eclipse.fennec.model.compliance.history.ChangeKind;
import org.eclipse.fennec.model.compliance.history.ChangeRow;
import org.eclipse.fennec.model.compliance.history.EvaluationRow;
import org.eclipse.fennec.model.compliance.history.ComplianceReportHistory;
import org.eclipse.fennec.model.compliance.history.ReportRevision;
import org.eclipse.fennec.model.compliance.history.RevisionOrigin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The diff is where this bundle can be wrong in a way a reader would believe, so it is where the
 * tests are.
 */
class ReportHistoryBuilderTest {

	private static final ReportFactory REPORTS = ReportFactory.eINSTANCE;

	private final ReportHistoryBuilder builder = new ReportHistoryBuilder();
	private final Instant now = Instant.parse("2026-09-18T12:00:00Z");

	/* ------------------------------------------------------------------ the shape */

	@Test
	@DisplayName("a single review produces one revision and no changes")
	void firstRevisionHasNothingToDifferFrom() {
		ComplianceReport report = report("2026-09-15T08:12:00Z", "claude-opus-5");
		feature(classifier(report, "Patient"), "Patient.dateOfBirth", "PERSONAL_DATA",
				RelevanceLevel.MEDIUM, Confidence.HIGH, "Identifies a person.", "Art.4(1)");

		ComplianceReportHistory history = builder.build(List.of(stored("gdpr-fp-20260915-081200", report)), now);

		assertEquals(1, history.getRevisionCount());
		assertEquals(1, history.getRevisions().size());
		assertEquals(1, history.getEvaluations().size());
		assertTrue(history.getChanges().isEmpty(), "nothing to compare the first revision against");
		assertEquals(ChangeKind.UNCHANGED, history.getEvaluations().get(0).getChangeKind());
		assertEquals(0, history.getRevisions().get(0).getChangeCount());
		assertEquals("2026-09-18T12:00:00Z", history.getRebuiltAt());
	}

	@Test
	@DisplayName("the subject is described from the newest report")
	void subjectComesFromTheNewestReport() {
		ComplianceReport first = report("2026-09-15T08:12:00Z", "claude-opus-5");
		ComplianceReport second = report("2026-09-17T14:20:30Z", "someone");
		((PackageSubject) second.getSubject()).setName("clinic-renamed");

		ComplianceReportHistory history = builder.build(
				List.of(stored("gdpr-fp-20260915-081200", first), stored("gdpr-fp-20260917-142030", second)), now);

		assertEquals("clinic-renamed", history.getSubjectName());
		assertEquals("https://example.org/clinic/1.0.0", history.getSubjectIdentifier(),
				"the document is filed under the nsURI, which a rename does not move");
		assertEquals("9f2c1ab7d4e85530", history.getRevisions().get(1).getSubjectFingerprint(),
				"the fingerprint is a property of the revision now, not of the document");
		assertEquals("GDPR review history of clinic-renamed", history.getName());
	}

	@Test
	@DisplayName("the document names every context its revisions were judged against, once each")
	void contextsAreCarriedOntoTheDocument() {
		ComplianceReport first = report("2026-09-15T08:12:00Z", "claude-opus-5");
		ComplianceReport second = report("2026-09-17T14:20:30Z", "someone");
		// The same context, re-consolidated, plus a second one the later review also cited.
		second.getContexts().get(0).setContextVersion("20180523");
		second.getContexts().add(context("cra", "20241120"));

		ComplianceReportHistory history = builder.build(
				List.of(stored("gdpr-fp-20260915-081200", first), stored("gdpr-fp-20260917-142030", second)), now);

		assertEquals(List.of("gdpr", "cra"), List.copyOf(history.getContextIds()),
				"a reader asking what this subject was ever judged against reads the document, not "
						+ "every revision; a context cited twice is one context");
		assertEquals(List.of("gdpr@20160504"), List.copyOf(history.getRevisions().get(0).getContextVersions()));
		assertEquals(List.of("gdpr@20180523", "cra@20241120"),
				List.copyOf(history.getRevisions().get(1).getContextVersions()),
				"the version stays on the revision, because that is what moved between the two");
	}

	/* ------------------------------------------------------------------ the headline case */

	@Test
	@DisplayName("a raised category is one MODIFIED change, not a removal plus an addition")
	void raisedCategoryIsAModification() {
		ComplianceReport before = report("2026-09-15T08:12:00Z", "claude-opus-5");
		feature(classifier(before, "Patient"), "Patient.diagnosis", "PERSONAL_DATA",
				RelevanceLevel.HIGH, Confidence.REQUIRES_CONFIRMATION, "Free-text notes.",
				"Art.4(15)");

		ComplianceReport after = report("2026-09-17T14:20:30Z", "a.reviewer@example.org");
		feature(classifier(after, "Patient"), "Patient.diagnosis", "SPECIAL_CATEGORY",
				RelevanceLevel.HIGH, Confidence.HIGH, "Confirmed: clinical diagnoses.", "Art.9(1)");

		ComplianceReportHistory history = builder.build(List.of(stored("gdpr-fp-20260915-081200", before),
				human("gdpr-fp-20260917-142030", after, "a.reviewer@example.org")), now);

		assertEquals(ChangeKind.MODIFIED, row(history, 2, "Patient", "Patient.diagnosis").getChangeKind());
		assertEquals("PERSONAL_DATA", change(history, "categories").getOldValue());
		assertEquals("SPECIAL_CATEGORY", change(history, "categories").getNewValue());
		assertEquals(ChangeKind.MODIFIED, change(history, "categories").getChangeKind());
		assertEquals("REQUIRES_CONFIRMATION", change(history, "confidence").getOldValue());
		assertEquals("HIGH", change(history, "confidence").getNewValue());
		assertNotNull(change(history, "rationale"), "a reworded justification is a change worth recording");

		// The citation moved too: one gone, one arrived, as separate rows.
		assertEquals("Art.4(15)", changeOf(history, "evidence", ChangeKind.REMOVED).getOldValue());
		assertEquals("Art.9(1)", changeOf(history, "evidence", ChangeKind.ADDED).getNewValue());

		ReportRevision second = history.getRevisions().get(1);
		assertEquals(RevisionOrigin.HUMAN, second.getOrigin());
		assertEquals("a.reviewer@example.org", second.getGeneratedBy());
		assertEquals(history.getChanges().size(), second.getChangeCount());
	}

	@Test
	@DisplayName("a dropped citation is its own REMOVED row, not a rewritten cell")
	void droppedCitationIsVisible() {
		ComplianceReport before = report("2026-09-15T08:12:00Z", "claude-opus-5");
		feature(classifier(before, "Patient"), "Patient.postcode", "QUASI_IDENTIFIER",
				RelevanceLevel.MEDIUM, Confidence.MEDIUM, "Narrows a population.", "Art.4(1)", "Rec.26");

		ComplianceReport after = report("2026-09-17T14:20:30Z", "claude-opus-5");
		feature(classifier(after, "Patient"), "Patient.postcode", "QUASI_IDENTIFIER",
				RelevanceLevel.MEDIUM, Confidence.MEDIUM, "Narrows a population.", "Art.4(1)");

		ComplianceReportHistory history = builder.build(List.of(stored("gdpr-fp-20260915-081200", before),
				stored("gdpr-fp-20260917-142030", after)), now);

		List<ChangeRow> changes = history.getChanges();
		assertEquals(1, changes.size(), "only the citation changed");
		assertEquals("evidence", changes.get(0).getField());
		assertEquals(ChangeKind.REMOVED, changes.get(0).getChangeKind());
		assertEquals("Rec.26", changes.get(0).getOldValue());
		assertNull(changes.get(0).getNewValue());
	}

	/* ------------------------------------------------------------------ appearing and disappearing */

	@Test
	@DisplayName("a feature that appears is ADDED; one that disappears is REMOVED with no ghost row")
	void appearingAndDisappearingFeatures() {
		ComplianceReport before = report("2026-09-15T08:12:00Z", "claude-opus-5");
		ClassifierEvaluation beforePatient = classifier(before, "Patient");
		feature(beforePatient, "Patient.dateOfBirth", "PERSONAL_DATA", RelevanceLevel.MEDIUM,
				Confidence.HIGH, "Identifies a person.", "Art.4(1)");
		feature(beforePatient, "Patient.postcode", "QUASI_IDENTIFIER", RelevanceLevel.LOW,
				Confidence.MEDIUM, "Narrows a population.", "Rec.26");

		ComplianceReport after = report("2026-09-17T14:20:30Z", "claude-opus-5");
		ClassifierEvaluation afterPatient = classifier(after, "Patient");
		feature(afterPatient, "Patient.dateOfBirth", "PERSONAL_DATA", RelevanceLevel.MEDIUM,
				Confidence.HIGH, "Identifies a person.", "Art.4(1)");
		feature(afterPatient, "Patient.email", "ONLINE_IDENTIFIER", RelevanceLevel.HIGH,
				Confidence.HIGH, "A contact address.", "Art.4(1)");

		ComplianceReportHistory history = builder.build(List.of(stored("gdpr-fp-20260915-081200", before),
				stored("gdpr-fp-20260917-142030", after)), now);

		assertEquals(ChangeKind.ADDED, row(history, 2, "Patient", "Patient.email").getChangeKind());
		assertEquals(ChangeKind.UNCHANGED, row(history, 2, "Patient", "Patient.dateOfBirth").getChangeKind());

		ChangeRow added = changeOf(history, "", ChangeKind.ADDED);
		assertEquals("Patient.email", added.getChildId());
		assertEquals("ONLINE_IDENTIFIER", added.getNewValue());

		ChangeRow removed = changeOf(history, "", ChangeKind.REMOVED);
		assertEquals("Patient.postcode", removed.getChildId());
		assertEquals("QUASI_IDENTIFIER", removed.getOldValue());

		assertTrue(history.getEvaluations().stream()
				.noneMatch(r -> r.getRevisionNumber() == 2 && "Patient.postcode".equals(r.getChildId())),
				"a revision says nothing about a feature it did not evaluate");
	}

	/* ------------------------------------------------------------------ the matching rules */

	@Test
	@DisplayName("reordered findings are not a change, and Finding.id is never matched on")
	void reorderingIsNotAChange() {
		ComplianceReport before = report("2026-09-15T08:12:00Z", "claude-opus-5");
		ClassifierEvaluation beforePatient = classifier(before, "Patient");
		finding(feature(beforePatient, "Patient.dateOfBirth", null, RelevanceLevel.MEDIUM), "F-001",
				"PERSONAL_DATA", RelevanceLevel.MEDIUM, Confidence.HIGH, "Identifies.", "Art.4(1)");
		finding(feature(beforePatient, "Patient.postcode", null, RelevanceLevel.LOW), "F-002",
				"QUASI_IDENTIFIER", RelevanceLevel.LOW, Confidence.MEDIUM, "Narrows.", "Rec.26");

		// Same content, written in the other order, with the F-numbers consequently swapped.
		ComplianceReport after = report("2026-09-17T14:20:30Z", "claude-opus-5");
		ClassifierEvaluation afterPatient = classifier(after, "Patient");
		finding(feature(afterPatient, "Patient.postcode", null, RelevanceLevel.LOW), "F-001",
				"QUASI_IDENTIFIER", RelevanceLevel.LOW, Confidence.MEDIUM, "Narrows.", "Rec.26");
		finding(feature(afterPatient, "Patient.dateOfBirth", null, RelevanceLevel.MEDIUM), "F-002",
				"PERSONAL_DATA", RelevanceLevel.MEDIUM, Confidence.HIGH, "Identifies.", "Art.4(1)");

		ComplianceReportHistory history = builder.build(List.of(stored("gdpr-fp-20260915-081200", before),
				stored("gdpr-fp-20260917-142030", after)), now);

		assertTrue(history.getChanges().isEmpty(),
				"the same assessment written in another order, with reused F-numbers, is not a change");
	}

	@Test
	@DisplayName("two findings on one feature widen the category cell rather than splitting the row")
	void severalFindingsMergeIntoOneRow() {
		ComplianceReport report = report("2026-09-15T08:12:00Z", "claude-opus-5");
		FeatureEvaluation home = feature(classifier(report, "Patient"), "Patient.homeAddress", null,
				RelevanceLevel.LOW);
		finding(home, "F-001", "LOCATION_DATA", RelevanceLevel.MEDIUM, Confidence.HIGH,
				"A place of residence.", "Art.4(1)");
		finding(home, "F-002", "QUASI_IDENTIFIER", RelevanceLevel.HIGH,
				Confidence.REQUIRES_CONFIRMATION, "Narrows a population.", "Rec.26");

		ComplianceReportHistory history = builder.build(List.of(stored("gdpr-fp-20260915-081200", report)), now);

		EvaluationRow row = row(history, 1, "Patient", "Patient.homeAddress");
		assertEquals("LOCATION_DATA, QUASI_IDENTIFIER", row.getCategories());
		assertEquals("HIGH", row.getRelevanceLevel(), "the highest relevance of the findings");
		assertEquals("REQUIRES_CONFIRMATION", row.getConfidence(), "the least confident of the findings");
		assertEquals("Art.4(1), Rec.26", row.getCitations());
	}

	@Test
	@DisplayName("a feature examined and found irrelevant still gets a row")
	void examinedButNotFlaggedIsStillStated() {
		ComplianceReport report = report("2026-09-15T08:12:00Z", "claude-opus-5");
		feature(classifier(report, "Appointment"), "Appointment.staffNotes", null, RelevanceLevel.NONE);

		ComplianceReportHistory history = builder.build(List.of(stored("gdpr-fp-20260915-081200", report)), now);

		EvaluationRow row = row(history, 1, "Appointment", "Appointment.staffNotes");
		assertNull(row.getCategories(), "nothing was found");
		assertEquals("NONE", row.getRelevanceLevel(), "but it was examined, and that is a statement");
	}

	/* ------------------------------------------------------------------ ordering */

	@Test
	@DisplayName("revisions are ordered by generatedAt, whatever order they arrive in")
	void ordersByGeneratedAt() {
		ComplianceReport newest = report("2026-09-17T14:20:30Z", "claude-opus-5");
		ComplianceReport oldest = report("2026-09-15T08:12:00Z", "claude-opus-5");

		ComplianceReportHistory history = builder.build(List.of(stored("gdpr-fp-20260917-142030", newest),
				stored("gdpr-fp-20260915-081200", oldest)), now);

		assertEquals("gdpr-fp-20260915-081200", history.getRevisions().get(0).getReportId());
		assertEquals("gdpr-fp-20260917-142030", history.getRevisions().get(1).getReportId());
		assertEquals(1, history.getRevisions().get(0).getRevisionNumber());
		assertEquals(2, history.getRevisions().get(1).getRevisionNumber());
	}

	@Test
	@DisplayName("a report without a usable generatedAt is ordered by the timestamp in its id")
	void fallsBackToTheTimestampInTheId() {
		ComplianceReport undated = report(null, "claude-opus-5");
		ComplianceReport dated = report("2026-09-15T08:12:00Z", "claude-opus-5");

		ComplianceReportHistory history = builder.build(List.of(stored("gdpr-fp-20260917-142030", undated),
				stored("gdpr-fp-20260915-081200", dated)), now);

		assertEquals("gdpr-fp-20260915-081200", history.getRevisions().get(0).getReportId());
		assertEquals("gdpr-fp-20260917-142030", history.getRevisions().get(1).getReportId(),
				"the id's timestamp puts the undated report second");
	}

	@Test
	@DisplayName("no reports is an empty document, not a failure")
	void emptyInputIsAnEmptyDocument() {
		ComplianceReportHistory history = builder.build(List.of(), now);

		assertEquals(0, history.getRevisionCount());
		assertTrue(history.getRevisions().isEmpty());
		assertTrue(history.getEvaluations().isEmpty());
		assertTrue(history.getChanges().isEmpty());
		assertNull(history.getSubjectName());
		assertNull(history.getSubjectIdentifier());
	}

	@Test
	@DisplayName("a transformation review is flattened by flow, named after its qualified name")
	void transformationReviewIsFlattenedByFlow() {
		ComplianceReport report = REPORTS.createComplianceReport();
		report.setGeneratedAt("2026-09-24T10:00:00Z");
		report.setGeneratedBy("static-analysis");
		report.setOrigin(ReportOrigin.STATIC_ANALYSIS);
		TransformationSubject subject = REPORTS.createTransformationSubject();
		subject.setQualifiedName("clinic.Anonymise");
		subject.setLanguage("qvto");
		subject.setSubjectFingerprint("77aa88bb99cc00dd");
		report.setSubject(subject);
		FlowEvaluation flow = REPORTS.createFlowEvaluation();
		flow.setId("Patient.name->Record.label");
		flow.setName("Patient.name -> Record.label");
		flow.setMapping("patientToRecord");
		flow.setRelevanceLevel(RelevanceLevel.HIGH);
		flow.setPurpose("copies the name verbatim");
		report.getEvaluations().add(flow);
		Finding finding = REPORTS.createFinding();
		finding.setId("F-001");
		finding.getCategories().add(categoryRef("PERSONAL_DATA"));
		finding.setRelevanceLevel(RelevanceLevel.HIGH);
		finding.setRationale("a person's name flows into the target unchanged");
		flow.getFindings().add(finding);

		ComplianceReportHistory history = builder.build(List.of(stored("gdpr-tr-20260924-100000", report)), now);

		assertEquals("clinic.Anonymise", history.getSubjectName());
		// what the transformation is WRITTEN IN; the review's own language is reportLanguage
		assertEquals("qvto", history.getSubjectLanguage());
		assertEquals("clinic.Anonymise", history.getSubjectIdentifier(),
				"a transformation is filed under its qualified name, a package under its nsURI");
		assertEquals("77aa88bb99cc00dd", history.getRevisions().get(0).getSubjectFingerprint());
		assertEquals("GDPR review history of clinic.Anonymise", history.getName());
		assertEquals(RevisionOrigin.STATIC_ANALYSIS, history.getRevisions().get(0).getOrigin());
		assertEquals(1, history.getRevisions().get(0).getFindingCount());
		assertEquals(1, history.getEvaluations().size());
		EvaluationRow row = history.getEvaluations().get(0);
		assertEquals("Patient.name->Record.label", row.getElementId());
		assertEquals("Patient.name -> Record.label", row.getElementName());
		assertEquals("copies the name verbatim", row.getPurpose());
		assertEquals(RelevanceLevel.HIGH.getName(), row.getRelevanceLevel());
		assertTrue(row.getRationale().contains("flows into the target"));
	}

	/* ------------------------------------------------------------------ provenance and purpose */

	@Test
	@DisplayName("a purpose filled in by a human is an ADDED change naming who wrote it")
	void aHumanAnsweringTheOpenQuestionIsRecorded() {
		ComplianceReport before = report("2026-09-15T08:12:00Z", "claude-opus-5");
		before.setOrigin(ReportOrigin.AI_AGENT);
		feature(classifier(before, "Patient"), "Patient.diagnosis", "PERSONAL_DATA",
				RelevanceLevel.HIGH, Confidence.REQUIRES_CONFIRMATION, "Free-text notes.",
				"Art.4(15)");

		ComplianceReport after = report("2026-09-17T14:20:30Z", "claude-opus-5");
		after.setOrigin(ReportOrigin.HUMAN);
		FeatureEvaluation diagnosis = feature(classifier(after, "Patient"), "Patient.diagnosis",
				"PERSONAL_DATA", RelevanceLevel.HIGH, Confidence.HIGH, "Free-text notes.",
				"Art.4(15)");
		diagnosis.setPurpose("Billing and continuity of treatment, Art.9(2)(h).");

		ComplianceReportHistory history = builder.build(List.of(stored("gdpr-fp-20260915-081200", before),
				human("gdpr-fp-20260917-142030", after, "a.reviewer@example.org")), now);

		assertEquals("Billing and continuity of treatment, Art.9(2)(h).",
				row(history, 2, "Patient", "Patient.diagnosis").getPurpose());
		assertNull(row(history, 1, "Patient", "Patient.diagnosis").getPurpose(), "nobody had answered yet");

		ChangeRow purpose = changeOf(history, "purpose", ChangeKind.ADDED);
		assertNull(purpose.getOldValue());
		assertEquals("Billing and continuity of treatment, Art.9(2)(h).", purpose.getNewValue());
		assertEquals("a.reviewer@example.org", purpose.getChangedBy(),
				"the person who answered, not the model that asked");

		// The pair reads as one action: the question was answered and the confidence moved with it.
		assertEquals("REQUIRES_CONFIRMATION", change(history, "confidence").getOldValue());
		assertEquals("HIGH", change(history, "confidence").getNewValue());
		assertEquals(RevisionOrigin.HUMAN, history.getRevisions().get(1).getOrigin());
	}

	@Test
	@DisplayName("a report that never stated an origin is UNKNOWN, not the first literal")
	void anUnstatedOriginIsNotAGuess() {
		ComplianceReport silent = report("2026-09-15T08:12:00Z", "claude-opus-5");

		ComplianceReportHistory history = builder.build(
				List.of(new StoredReport("gdpr-fp-20260915-081200", silent, null, null)), now);

		assertEquals(RevisionOrigin.UNKNOWN, history.getRevisions().get(0).getOrigin(),
				"an unset EMF enum reads as its first literal, which must not become an attribution");
	}

	@Test
	@DisplayName("the origin in the report wins over the one the caller guessed")
	void theReportSpeaksForItself() {
		ComplianceReport stated = report("2026-09-15T08:12:00Z", "someone");
		stated.setOrigin(ReportOrigin.HUMAN);

		ComplianceReportHistory history = builder.build(
				List.of(new StoredReport("gdpr-fp-20260915-081200", stated, null, RevisionOrigin.AI_AGENT)), now);

		assertEquals(RevisionOrigin.HUMAN, history.getRevisions().get(0).getOrigin());
	}

	@Test
	@DisplayName("a report derived by a program is recorded as STATIC_ANALYSIS, not as an agent")
	void aDerivedReviewIsNeitherAgentNorPerson() {
		ComplianceReport derived = report("2026-09-15T08:12:00Z", "qvto-flow-analyser");
		derived.setOrigin(ReportOrigin.STATIC_ANALYSIS);

		ComplianceReportHistory history = builder.build(
				List.of(new StoredReport("gdpr-fp-20260915-081200", derived, null, RevisionOrigin.AI_AGENT)), now);

		assertEquals(RevisionOrigin.STATIC_ANALYSIS, history.getRevisions().get(0).getOrigin(),
				"no agent and no person formed this judgement, and the document must not imply one did");
	}

	@Test
	@DisplayName("a withdrawn purpose is REMOVED, and a reworded one MODIFIED")
	void purposeCanAlsoChangeAndGoAway() {
		ComplianceReport first = report("2026-09-15T08:12:00Z", "a.reviewer@example.org");
		feature(classifier(first, "Patient"), "Patient.diagnosis", "PERSONAL_DATA",
				RelevanceLevel.HIGH, Confidence.HIGH, "Notes.", "Art.4(15)").setPurpose("Billing.");

		ComplianceReport second = report("2026-09-16T09:00:00Z", "a.reviewer@example.org");
		feature(classifier(second, "Patient"), "Patient.diagnosis", "PERSONAL_DATA",
				RelevanceLevel.HIGH, Confidence.HIGH, "Notes.", "Art.4(15)")
						.setPurpose("Billing and continuity of treatment.");

		ComplianceReport third = report("2026-09-17T14:20:30Z", "a.reviewer@example.org");
		feature(classifier(third, "Patient"), "Patient.diagnosis", "PERSONAL_DATA",
				RelevanceLevel.HIGH, Confidence.HIGH, "Notes.", "Art.4(15)");

		ComplianceReportHistory history = builder.build(List.of(stored("gdpr-fp-20260915-081200", first),
				stored("gdpr-fp-20260916-090000", second), stored("gdpr-fp-20260917-142030", third)), now);

		ChangeRow reworded = changeOf(history, "purpose", ChangeKind.MODIFIED);
		assertEquals("Billing.", reworded.getOldValue());
		assertEquals("Billing and continuity of treatment.", reworded.getNewValue());
		assertEquals(2, reworded.getRevisionNumber());

		ChangeRow withdrawn = changeOf(history, "purpose", ChangeKind.REMOVED);
		assertEquals("Billing and continuity of treatment.", withdrawn.getOldValue());
		assertNull(withdrawn.getNewValue());
		assertEquals(3, withdrawn.getRevisionNumber());
	}

	/* ------------------------------------------------------------------ fixtures */

	/* ------------------------------------------------------------------ the human decision */

	@Test
	@DisplayName("a decided finding puts the whole decision on its row")
	void aDecidedFindingCarriesItsResolution() {
		ComplianceReport report = report("2026-09-15T08:12:00Z", "a-human");
		FeatureEvaluation feature = feature(classifier(report, "Visitor"), "Visitor.note", "PERSONAL_DATA",
				RelevanceLevel.LOW, Confidence.HIGH, "Free text.", "Art.4(1)");
		Finding finding = feature.getFindings().get(0);
		finding.setOrigin(ReportOrigin.HUMAN);
		finding.setCorrectionNote("No Art. 9 link after the rebuild.");
		finding.getRequirements().add(requirement("Art.5(1)(c)"));
		finding.setResolution(resolution(ReviewStatus.REVIEWED, RiskTreatment.MITIGATE,
				"Free text is replaced by a picklist.", "i.salvadori@example.org", "2026-09-15T08:30:00Z"));

		ComplianceReportHistory history = builder.build(List.of(stored("gdpr-fp-20260915-081200", report)), now);
		EvaluationRow row = row(history, 1, "Visitor", "Visitor.note");

		assertEquals("REVIEWED", row.getReviewStatus());
		assertEquals("MITIGATE", row.getTreatment());
		assertEquals("Free text is replaced by a picklist.", row.getResolutionJustification());
		assertEquals("i.salvadori@example.org", row.getDecidedBy());
		assertEquals("2026-09-15T08:30:00Z", row.getDecidedAt());
		assertEquals("M-UI-11, M-UI-12", row.getMeasureIds(), "measures are comma separated, as the model says");
		assertEquals("Picklist ships in November.", row.getTreatmentNote());
		assertEquals("the data protection officer", row.getDelegatedTo());
		assertEquals("2026-11-30", row.getDueDate());
		assertEquals("MEDIUM", row.getRiskLevel(), "from the assessment the treatment is based on");
		assertEquals("No Art. 9 link after the rebuild.", row.getCorrectionNote());
		assertEquals("HUMAN", row.getFindingOrigin());
		assertEquals("Art.5(1)(c)", row.getRequirementIds());
		assertEquals("gdpr", row.getContextId());
	}

	@Test
	@DisplayName("an undecided finding leaves the decision cells empty, which is what 'still open' looks like")
	void anUndecidedFindingLeavesTheDecisionEmpty() {
		ComplianceReport report = report("2026-09-15T08:12:00Z", "claude-opus-5");
		feature(classifier(report, "Visitor"), "Visitor.note", "PERSONAL_DATA", RelevanceLevel.LOW,
				Confidence.HIGH, "Free text.", "Art.4(1)");

		ComplianceReportHistory history = builder.build(List.of(stored("gdpr-fp-20260915-081200", report)), now);
		EvaluationRow row = row(history, 1, "Visitor", "Visitor.note");

		assertNull(row.getReviewStatus(), "empty means nobody has decided it yet");
		assertNull(row.getTreatment());
		assertNull(row.getDecidedBy());
		assertNull(row.getDueDate());
	}

	@Test
	@DisplayName("a feature is REVIEWED only when every one of its findings is, and empty until then")
	void aHalfReviewedFeatureIsStillOpen() {
		ComplianceReport report = report("2026-09-15T08:12:00Z", "a-human");
		FeatureEvaluation feature = feature(classifier(report, "Visitor"), "Visitor.note", "PERSONAL_DATA",
				RelevanceLevel.MEDIUM, Confidence.HIGH, "Contact details.", "Art.4(1)");
		Finding decided = feature.getFindings().get(0);
		decided.setResolution(resolution(ReviewStatus.REVIEWED, RiskTreatment.ACCEPT, "Accepted.",
				"i.salvadori@example.org", "2026-09-15T08:30:00Z"));
		finding(feature, "F-002", "SPECIAL_CATEGORY", RelevanceLevel.HIGH, Confidence.REQUIRES_CONFIRMATION,
				"Health data may be typed in here.", "Art.9(1)");

		ComplianceReportHistory history = builder.build(List.of(stored("gdpr-fp-20260915-081200", report)), now);
		EvaluationRow row = row(history, 1, "Visitor", "Visitor.note");

		assertNull(row.getReviewStatus(),
				"one finding on this feature has not been looked at; the model reads an empty status as "
						+ "still open, so writing OPEN would say it twice");
		assertEquals("ACCEPT", row.getTreatment(), "the one decision there is, is still worth stating");
	}

	@Test
	@DisplayName("findings decided differently are joined rather than reduced to one of them")
	void differingTreatmentsAreBothStated() {
		ComplianceReport report = report("2026-09-15T08:12:00Z", "a-human");
		FeatureEvaluation feature = feature(classifier(report, "Visitor"), "Visitor.note", "PERSONAL_DATA",
				RelevanceLevel.MEDIUM, Confidence.HIGH, "Contact details.", "Art.4(1)");
		feature.getFindings().get(0).setResolution(resolution(ReviewStatus.REVIEWED, RiskTreatment.ACCEPT,
				"Accepted.", "i.salvadori@example.org", "2026-09-15T08:30:00Z"));
		Finding second = finding(feature, "F-002", "SPECIAL_CATEGORY", RelevanceLevel.HIGH, Confidence.HIGH,
				"Health data may be typed in here.", "Art.9(1)");
		second.setResolution(resolution(ReviewStatus.REVIEWED, RiskTreatment.MITIGATE, "Picklist.",
				"i.salvadori@example.org", "2026-09-15T08:30:00Z"));

		ComplianceReportHistory history = builder.build(List.of(stored("gdpr-fp-20260915-081200", report)), now);
		EvaluationRow row = row(history, 1, "Visitor", "Visitor.note");

		assertEquals("REVIEWED", row.getReviewStatus());
		assertEquals("ACCEPT, MITIGATE", row.getTreatment(),
				"a row that covers two findings must not report one of their treatments as if it were the row's");
		assertEquals("Accepted., Picklist.", row.getResolutionJustification());
	}

	@Test
	@DisplayName("a classifier states its purpose and lawful bases even when every finding sits on a feature")
	void aClassifierRowCarriesPurposeAndLawfulBases() {
		ComplianceReport report = report("2026-09-15T08:12:00Z", "a-human");
		ClassifierEvaluation visitor = classifier(report, "Visitor");
		visitor.setPurpose("Membership administration and contract performance.");
		visitor.getLawfulBases().add(lawfulBasis("ART6_1_B"));
		visitor.getLawfulBases().add(lawfulBasis("ART6_1_F"));
		// every finding sits on the feature, which is the normal shape of a review
		feature(visitor, "Visitor.note", "PERSONAL_DATA", RelevanceLevel.LOW, Confidence.HIGH, "Free text.",
				"Art.4(1)");

		ComplianceReportHistory history = builder.build(List.of(stored("gdpr-fp-20260915-081200", report)), now);
		EvaluationRow row = row(history, 1, "Visitor", null);

		assertEquals("Membership administration and contract performance.", row.getPurpose());
		assertEquals("ART6_1_B, ART6_1_F", row.getLawfulBases(), "comma separated, as the model says");
	}

	@Test
	@DisplayName("a classifier with nothing to say earns no row")
	void aSilentClassifierHasNoRow() {
		ComplianceReport report = report("2026-09-15T08:12:00Z", "claude-opus-5");
		feature(classifier(report, "Visitor"), "Visitor.note", "PERSONAL_DATA", RelevanceLevel.LOW,
				Confidence.HIGH, "Free text.", "Art.4(1)");

		ComplianceReportHistory history = builder.build(List.of(stored("gdpr-fp-20260915-081200", report)), now);

		assertTrue(history.getEvaluations().stream().noneMatch(r -> r.getChildId() == null),
				"a classifier without findings, purpose or lawful bases states nothing of its own");
	}

	@Test
	@DisplayName("a decision taken between two revisions reaches the change sheet")
	void aDecisionIsDiffed() {
		ComplianceReport first = report("2026-09-15T08:12:00Z", "claude-opus-5");
		feature(classifier(first, "Visitor"), "Visitor.note", "PERSONAL_DATA", RelevanceLevel.LOW,
				Confidence.HIGH, "Free text.", "Art.4(1)");

		ComplianceReport second = report("2026-09-17T14:20:30Z", "a-human");
		FeatureEvaluation reviewed = feature(classifier(second, "Visitor"), "Visitor.note", "PERSONAL_DATA",
				RelevanceLevel.LOW, Confidence.HIGH, "Free text.", "Art.4(1)");
		Finding finding = reviewed.getFindings().get(0);
		finding.setCorrectionNote("Checked with the controller.");
		finding.setResolution(resolution(ReviewStatus.REVIEWED, RiskTreatment.ACCEPT, "Residual risk accepted.",
				"i.salvadori@example.org", "2026-09-17T14:00:00Z"));

		ComplianceReportHistory history = builder.build(
				List.of(stored("gdpr-fp-20260915-081200", first), human("gdpr-fp-20260917-142030", second, "a-human")),
				now);

		ChangeRow status = changeOf(history, "reviewStatus", ChangeKind.ADDED);
		assertEquals("REVIEWED", status.getNewValue());
		assertEquals("a-human", status.getChangedBy());
		assertEquals("ACCEPT", changeOf(history, "treatment", ChangeKind.ADDED).getNewValue());
		assertEquals("Residual risk accepted.",
				changeOf(history, "resolutionJustification", ChangeKind.ADDED).getNewValue());
		assertEquals("Checked with the controller.",
				changeOf(history, "correctionNote", ChangeKind.ADDED).getNewValue());
		assertNull(change(history, "decidedBy"),
				"the model says decidedBy is not diffed on its own - changedBy already names the author");
		assertNull(change(history, "decidedAt"), "nor decidedAt");
	}


	@Test
	@DisplayName("a row that appears already stating a purpose and a lawful basis says so in the change sheet")
	void anAddedRowStatesWhatAPersonPutOnIt() {
		ComplianceReport first = report("2026-09-15T08:12:00Z", "claude-opus-5");
		feature(classifier(first, "Visitor"), "Visitor.note", "PERSONAL_DATA", RelevanceLevel.LOW,
				Confidence.HIGH, "Free text.", "Art.4(1)");

		ComplianceReport second = report("2026-09-17T14:20:30Z", "a-human");
		ClassifierEvaluation visitor = classifier(second, "Visitor");
		visitor.setPurpose("Membership administration.");
		visitor.getLawfulBases().add(lawfulBasis("ART6_1_B"));
		feature(visitor, "Visitor.note", "PERSONAL_DATA", RelevanceLevel.LOW, Confidence.HIGH, "Free text.",
				"Art.4(1)");

		ComplianceReportHistory history = builder.build(
				List.of(stored("gdpr-fp-20260915-081200", first), human("gdpr-fp-20260917-142030", second, "a-human")),
				now);

		assertEquals(ChangeKind.ADDED, row(history, 2, "Visitor", null).getChangeKind(),
				"the classifier had nothing to say in revision 1, so its row is new");
		assertEquals("Membership administration.", changeOf(history, "purpose", ChangeKind.ADDED).getNewValue(),
				"a cell a person filled on a brand new row must still reach the change sheet");
		assertEquals("ART6_1_B", changeOf(history, "lawfulBases", ChangeKind.ADDED).getNewValue());
		assertEquals("a-human", changeOf(history, "lawfulBases", ChangeKind.ADDED).getChangedBy());
	}

	private static ComplianceReport report(String generatedAt, String generatedBy) {
		ComplianceReport report = REPORTS.createComplianceReport();
		report.setGeneratedAt(generatedAt);
		report.setGeneratedBy(generatedBy);

		PackageSubject subject = REPORTS.createPackageSubject();
		subject.setName("clinic");
		subject.setNsURI("https://example.org/clinic/1.0.0");
		subject.setSubjectFingerprint("9f2c1ab7d4e85530");
		report.setSubject(subject);

		report.getContexts().add(context("gdpr", "20160504"));
		return report;
	}

	private static ContextRef context(String id, String version) {
		ContextRef context = ContextFactory.eINSTANCE.createContextRef();
		context.setContextId(id);
		context.setContextVersion(version);
		return context;
	}

	private static ClassifierEvaluation classifier(ComplianceReport report, String name) {
		ClassifierEvaluation classifier = REPORTS.createClassifierEvaluation();
		classifier.setId(name);
		classifier.setName(name);
		classifier.setUriFragment("//" + name);
		report.getEvaluations().add(classifier);
		return classifier;
	}

	private static FeatureEvaluation feature(ClassifierEvaluation classifier, String id, String category,
			RelevanceLevel relevance) {
		FeatureEvaluation feature = REPORTS.createFeatureEvaluation();
		feature.setId(id);
		feature.setName(id.substring(id.indexOf('.') + 1));
		feature.setUriFragment("//" + id.replace('.', '/'));
		feature.setRelevanceLevel(relevance);
		classifier.getFeatureEvaluations().add(feature);
		return feature;
	}

	private static FeatureEvaluation feature(ClassifierEvaluation classifier, String id, String category,
			RelevanceLevel relevance, Confidence confidence, String rationale, String... citations) {
		FeatureEvaluation feature = feature(classifier, id, category, relevance);
		finding(feature, "F-001", category, relevance, confidence, rationale, citations);
		return feature;
	}

	private static Finding finding(FeatureEvaluation feature, String id, String category,
			RelevanceLevel relevance, Confidence confidence, String rationale, String... citations) {
		Finding finding = REPORTS.createFinding();
		finding.setId(id);
		finding.getCategories().add(categoryRef(category));
		finding.setRelevanceLevel(relevance);
		finding.setConfidence(confidence);
		finding.setRationale(rationale);
		for (String citation : citations) {
			Evidence evidence = REPORTS.createEvidence();
			evidence.setCitationId(citation);
			evidence.setQuote("...");
			finding.getEvidence().add(evidence);
		}
		feature.getFindings().add(finding);
		return finding;
	}

	private static FindingResolution resolution(ReviewStatus status, RiskTreatment treatment, String justification,
			String decidedBy, String decidedAt) {
		FindingResolution resolution = REPORTS.createFindingResolution();
		resolution.setStatus(status);
		resolution.setTreatment(treatment);
		resolution.setJustification(justification);
		resolution.setDecidedBy(decidedBy);
		resolution.setDecidedAt(decidedAt);
		resolution.getMeasureIds().add("M-UI-11");
		resolution.getMeasureIds().add("M-UI-12");
		resolution.setTreatmentNote("Picklist ships in November.");
		resolution.setDelegatedTo("the data protection officer");
		resolution.setDueDate("2026-11-30");
		RiskAssessment assessment = REPORTS.createRiskAssessment();
		assessment.setRiskLevel("MEDIUM");
		resolution.setRiskAssessment(assessment);
		return resolution;
	}

	private static RequirementRef requirement(String id) {
		RequirementRef ref = ContextFactory.eINSTANCE.createRequirementRef();
		ref.setContextId("gdpr");
		ref.setRequirementId(id);
		return ref;
	}

	private static CategoryRef lawfulBasis(String categoryId) {
		CategoryRef ref = ContextFactory.eINSTANCE.createCategoryRef();
		ref.setContextId("gdpr");
		ref.setTaxonomyId("lawful-bases");
		ref.setCategoryId(categoryId);
		return ref;
	}

	private static StoredReport stored(String objectId, ComplianceReport report) {
		return new StoredReport(objectId, report, null, RevisionOrigin.AI_AGENT);
	}

	private static StoredReport human(String objectId, ComplianceReport report, String user) {
		return new StoredReport(objectId, report, user, RevisionOrigin.HUMAN);
	}

	/* ------------------------------------------------------------------ lookups */

	private static EvaluationRow row(ComplianceReportHistory history, int revision, String classifierId, String featureId) {
		Optional<EvaluationRow> row = history.getEvaluations().stream()
				.filter(r -> r.getRevisionNumber() == revision && classifierId.equals(r.getElementId())
						&& Objects.equals(featureId, r.getChildId()))
				.findFirst();
		assertTrue(row.isPresent(), "no row for " + featureId + " in revision " + revision);
		return row.get();
	}

	private static ChangeRow change(ComplianceReportHistory history, String field) {
		return history.getChanges().stream().filter(c -> field.equals(c.getField())).findFirst().orElse(null);
	}

	private static ChangeRow changeOf(ComplianceReportHistory history, String field, ChangeKind kind) {
		Optional<ChangeRow> change = history.getChanges().stream()
				.filter(c -> field.equals(c.getField()) && kind == c.getChangeKind()).findFirst();
		assertTrue(change.isPresent(), "no " + kind + " change for field '" + field + "'");
		return change.get();
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
