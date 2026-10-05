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
import java.util.Optional;

import org.eclipse.fennec.model.compliance.context.CategoryRef;
import org.eclipse.fennec.model.compliance.context.ContextRef;
import org.eclipse.fennec.model.compliance.context.ContextFactory;
import org.eclipse.fennec.model.compliance.report.ClassifierEvaluation;
import org.eclipse.fennec.model.compliance.report.Confidence;
import org.eclipse.fennec.model.compliance.report.Evidence;
import org.eclipse.fennec.model.compliance.report.FeatureEvaluation;
import org.eclipse.fennec.model.compliance.report.Finding;
import org.eclipse.fennec.model.compliance.report.FlowEvaluation;
import org.eclipse.fennec.model.compliance.report.ReportFactory;
import org.eclipse.fennec.model.compliance.report.ComplianceReport;
import org.eclipse.fennec.model.compliance.report.ReportOrigin;
import org.eclipse.fennec.model.compliance.report.RelevanceLevel;
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

	private static ComplianceReport report(String generatedAt, String generatedBy) {
		ComplianceReport report = REPORTS.createComplianceReport();
		report.setGeneratedAt(generatedAt);
		report.setGeneratedBy(generatedBy);

		PackageSubject subject = REPORTS.createPackageSubject();
		subject.setName("clinic");
		subject.setNsURI("https://example.org/clinic/1.0.0");
		subject.setSubjectFingerprint("9f2c1ab7d4e85530");
		report.setSubject(subject);

		ContextRef context = ContextFactory.eINSTANCE.createContextRef();
		context.setContextId("gdpr");
		context.setContextVersion("20160504");
		report.getContexts().add(context);
		return report;
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
						&& featureId.equals(r.getChildId()))
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
