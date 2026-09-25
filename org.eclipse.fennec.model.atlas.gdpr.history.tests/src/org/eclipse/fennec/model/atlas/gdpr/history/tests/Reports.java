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

import org.eclipse.fennec.model.gdprReport.ClassifierEvaluation;
import org.eclipse.fennec.model.gdprReport.ConfidenceType;
import org.eclipse.fennec.model.gdprReport.DataCategory;
import org.eclipse.fennec.model.gdprReport.Evidence;
import org.eclipse.fennec.model.gdprReport.FeatureEvaluation;
import org.eclipse.fennec.model.gdprReport.Finding;
import org.eclipse.fennec.model.gdprReport.GDPRReportFactory;
import org.eclipse.fennec.model.gdprReport.GdprReport;
import org.eclipse.fennec.model.gdprReport.GdprReportOrigin;
import org.eclipse.fennec.model.gdprReport.RelevanceLevelType;
import org.eclipse.fennec.model.gdprReport.PackageSubject;

/**
 * Two reviews of one model, a run apart: the agent's first pass, then a human raising
 * {@code Patient.diagnosis} from {@code PERSONAL_DATA} to {@code SPECIAL_CATEGORY}, swapping its
 * citation and adding a feature. The pair is the case the document exists for, so it is what both
 * integration tests store.
 */
final class Reports {

	static final GDPRReportFactory FACTORY = GDPRReportFactory.eINSTANCE;

	static final String FINGERPRINT = "fp1:clinic100";

	/** The language every fixture review is carried out in. */
	static final String LANGUAGE = "EN";
	static final String SUBJECT_NS_URI = "https://example.org/clinic/1.0.0";

	private Reports() {
	}

	/** The agent's review. */
	static GdprReport agentReview() {
		GdprReport report = report("gdpr-clinic-20260922-070000", "2026-09-22T07:00:00Z", "an-agent",
				GdprReportOrigin.AI_AGENT);
		ClassifierEvaluation patient = classifier(report);
		feature(patient, "Patient.diagnosis", "diagnosis", DataCategory.PERSONAL_DATA,
				ConfidenceType.REQUIRES_PURPOSE_CONFIRMATION, "Free-text clinical notes may contain health data.",
				"Art.9");
		feature(patient, "Patient.postcode", "postcode", DataCategory.QUASI_IDENTIFIER, ConfidenceType.MEDIUM,
				"A postcode singles out an individual when combined with age.", "Rec.26");
		return report;
	}

	/** The human's correction, for the same model revision. */
	static GdprReport humanCorrection() {
		GdprReport report = report("gdpr-clinic-20260922-090000", "2026-09-22T09:00:00Z", "a-human",
				GdprReportOrigin.HUMAN);
		ClassifierEvaluation patient = classifier(report);
		feature(patient, "Patient.diagnosis", "diagnosis", DataCategory.SPECIAL_CATEGORY, ConfidenceType.HIGH,
				"Confirmed with the data owner: the field holds diagnoses.", "Art.9");
		feature(patient, "Patient.postcode", "postcode", DataCategory.QUASI_IDENTIFIER, ConfidenceType.MEDIUM,
				"A postcode singles out an individual when combined with age.", "Art.4");
		feature(patient, "Patient.email", "email", DataCategory.ONLINE_IDENTIFIER, ConfidenceType.HIGH,
				"An e-mail address identifies the data subject directly.", "Rec.30");
		return report;
	}

	/**
	 * The German review of the same model revision. Not a later revision of the English one: it
	 * quotes the German consolidation, so it belongs in a document of its own.
	 */
	static GdprReport germanReview() {
		GdprReport report = report("gdpr-clinic-de-20260922-080000", "2026-09-22T08:00:00Z", "an-agent",
				GdprReportOrigin.AI_AGENT);
		report.getCorpus().setLanguage("DE");
		ClassifierEvaluation patient = classifier(report);
		feature(patient, "Patient.diagnosis", "diagnosis", DataCategory.PERSONAL_DATA,
				ConfidenceType.REQUIRES_PURPOSE_CONFIRMATION,
				"Freitext aus der Krankenakte kann Gesundheitsdaten enthalten.", "Art.9");
		return report;
	}

	private static GdprReport report(String reportId, String generatedAt, String generatedBy,
			GdprReportOrigin origin) {
		GdprReport report = FACTORY.createGdprReport();
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

		report.setCorpus(FACTORY.createLegalCorpusRef());
		report.getCorpus().setCelex("32016R0679");
		// The language the review was carried out in: the document is keyed by it, so a fixture
		// without one would land under 'unknown' and not where a real review's document goes.
		report.getCorpus().setLanguage(LANGUAGE);
		return report;
	}

	private static ClassifierEvaluation classifier(GdprReport report) {
		ClassifierEvaluation classifier = FACTORY.createClassifierEvaluation();
		classifier.setId("Patient");
		classifier.setName("Patient");
		classifier.setUriFragment("//Patient");
		report.getEvaluation().add(classifier);
		return classifier;
	}

	private static void feature(ClassifierEvaluation classifier, String id, String name, DataCategory category,
			ConfidenceType confidence, String rationale, String citation) {
		FeatureEvaluation feature = FACTORY.createFeatureEvaluation();
		feature.setId(id);
		feature.setName(name);
		feature.setUriFragment("//Patient/" + name);
		feature.setTypeName("EString");
		feature.setRelevanceLevel(RelevanceLevelType.HIGH);

		Finding finding = FACTORY.createFinding();
		finding.setId("F-" + id);
		finding.setCategory(category);
		finding.setRelevanceLevel(RelevanceLevelType.HIGH);
		finding.setConfidence(confidence);
		finding.setRationale(rationale);

		Evidence evidence = FACTORY.createEvidence();
		evidence.setCitationId(citation);
		evidence.setQuote("...");
		evidence.setVerbatim(true);
		finding.getEvidence().add(evidence);

		feature.getFindings().add(finding);
		classifier.getFeatureEvaluation().add(feature);
	}
}
