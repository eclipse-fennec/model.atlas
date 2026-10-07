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
package org.eclipse.fennec.model.atlas.gdpr.history.tests;

import org.eclipse.fennec.model.compliance.context.CategoryRef;
import org.eclipse.fennec.model.compliance.context.ContextRef;
import org.eclipse.fennec.model.compliance.context.ContextFactory;
import org.eclipse.fennec.model.compliance.report.ClassifierEvaluation;
import org.eclipse.fennec.model.compliance.report.Confidence;
import org.eclipse.fennec.model.compliance.report.Evidence;
import org.eclipse.fennec.model.compliance.report.FeatureEvaluation;
import org.eclipse.fennec.model.compliance.report.Finding;
import org.eclipse.fennec.model.compliance.report.ReportFactory;
import org.eclipse.fennec.model.compliance.report.ComplianceReport;
import org.eclipse.fennec.model.compliance.report.ReportOrigin;
import org.eclipse.fennec.model.compliance.report.RelevanceLevel;
import org.eclipse.fennec.model.compliance.report.PackageSubject;

/**
 * Two reviews of one model, a run apart: the agent's first pass, then a human raising
 * {@code Patient.diagnosis} from {@code PERSONAL_DATA} to {@code SPECIAL_CATEGORY}, swapping its
 * citation and adding a feature. The pair is the case the document exists for, so it is what both
 * integration tests store.
 */
final class Reports {

	static final ReportFactory FACTORY = ReportFactory.eINSTANCE;

	static final String FINGERPRINT = "fp1:clinic100";

	/** The language every fixture review is carried out in. */
	static final String LANGUAGE = "EN";
	static final String SUBJECT_NS_URI = "https://example.org/clinic/1.0.0";

	private Reports() {
	}

	/** The agent's review. */
	static ComplianceReport agentReview() {
		ComplianceReport report = report("gdpr-clinic-20260922-070000", "2026-09-22T07:00:00Z", "an-agent",
				ReportOrigin.AI_AGENT);
		ClassifierEvaluation patient = classifier(report);
		feature(patient, "Patient.diagnosis", "diagnosis", "PERSONAL_DATA",
				Confidence.REQUIRES_CONFIRMATION, "Free-text clinical notes may contain health data.",
				"Art.9");
		feature(patient, "Patient.postcode", "postcode", "QUASI_IDENTIFIER", Confidence.MEDIUM,
				"A postcode singles out an individual when combined with age.", "Rec.26");
		return report;
	}

	/** The human's correction, for the same model revision. */
	static ComplianceReport humanCorrection() {
		ComplianceReport report = report("gdpr-clinic-20260922-090000", "2026-09-22T09:00:00Z", "a-human",
				ReportOrigin.HUMAN);
		ClassifierEvaluation patient = classifier(report);
		feature(patient, "Patient.diagnosis", "diagnosis", "SPECIAL_CATEGORY", Confidence.HIGH,
				"Confirmed with the data owner: the field holds diagnoses.", "Art.9");
		feature(patient, "Patient.postcode", "postcode", "QUASI_IDENTIFIER", Confidence.MEDIUM,
				"A postcode singles out an individual when combined with age.", "Art.4");
		feature(patient, "Patient.email", "email", "ONLINE_IDENTIFIER", Confidence.HIGH,
				"An e-mail address identifies the data subject directly.", "Rec.30");
		return report;
	}

	/**
	 * The German review of the same model revision. Not a later revision of the English one: it
	 * quotes the German consolidation, so it belongs in a document of its own.
	 */
	static ComplianceReport germanReview() {
		ComplianceReport report = report("gdpr-clinic-de-20260922-080000", "2026-09-22T08:00:00Z", "an-agent",
				ReportOrigin.AI_AGENT);
		report.setLanguage("DE");
		ClassifierEvaluation patient = classifier(report);
		feature(patient, "Patient.diagnosis", "diagnosis", "PERSONAL_DATA",
				Confidence.REQUIRES_CONFIRMATION,
				"Freitext aus der Krankenakte kann Gesundheitsdaten enthalten.", "Art.9");
		return report;
	}

	private static ComplianceReport report(String reportId, String generatedAt, String generatedBy,
			ReportOrigin origin) {
		ComplianceReport report = FACTORY.createComplianceReport();
		report.setReportId(reportId);
		report.setName("GDPR review of clinic 1.0.0");
		report.setGeneratedAt(generatedAt);
		report.setGeneratedBy(generatedBy);
		report.setOrigin(origin);

		PackageSubject subject = FACTORY.createPackageSubject();
		subject.setName("clinic");
		subject.setNsURI(SUBJECT_NS_URI);
		subject.setNsPrefix("clinic");
		subject.setSubjectFingerprint(FINGERPRINT);
		report.setSubject(subject);

		ContextRef context = ContextFactory.eINSTANCE.createContextRef();
		context.setContextId("gdpr");
		context.setContextVersion("20160504");
		report.getContexts().add(context);
		// The language the review was written in: the document is keyed by it, so a fixture
		// without one would land under 'unknown' and not where a real review's document goes.
		report.setLanguage(LANGUAGE);
		return report;
	}

	private static ClassifierEvaluation classifier(ComplianceReport report) {
		ClassifierEvaluation classifier = FACTORY.createClassifierEvaluation();
		classifier.setId("Patient");
		classifier.setName("Patient");
		classifier.setUriFragment("//Patient");
		report.getEvaluations().add(classifier);
		return classifier;
	}

	private static void feature(ClassifierEvaluation classifier, String id, String name, String category,
			Confidence confidence, String rationale, String citation) {
		FeatureEvaluation feature = FACTORY.createFeatureEvaluation();
		feature.setId(id);
		feature.setName(name);
		feature.setUriFragment("//Patient/" + name);
		feature.setTypeName("EString");
		feature.setRelevanceLevel(RelevanceLevel.HIGH);

		Finding finding = FACTORY.createFinding();
		finding.setId("F-" + id);
		finding.getCategories().add(categoryRef(category));
		finding.setRelevanceLevel(RelevanceLevel.HIGH);
		finding.setConfidence(confidence);
		finding.setRationale(rationale);

		Evidence evidence = FACTORY.createEvidence();
		evidence.setCitationId(citation);
		evidence.setQuote("...");
		evidence.setVerbatim(true);
		finding.getEvidence().add(evidence);

		feature.getFindings().add(finding);
		classifier.getFeatureEvaluations().add(feature);
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
