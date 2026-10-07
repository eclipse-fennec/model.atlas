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

import org.eclipse.fennec.model.compliance.context.CategoryRef;
import org.eclipse.fennec.model.compliance.context.ContextRef;
import org.eclipse.fennec.model.compliance.context.ContextFactory;
import org.eclipse.fennec.model.compliance.report.ClassifierEvaluation;
import org.eclipse.fennec.model.compliance.report.Evidence;
import org.eclipse.fennec.model.compliance.report.FeatureEvaluation;
import org.eclipse.fennec.model.compliance.report.Finding;
import org.eclipse.fennec.model.compliance.report.ReportFactory;
import org.eclipse.fennec.model.compliance.report.ComplianceReport;
import org.eclipse.fennec.model.compliance.report.ReportOrigin;
import org.eclipse.fennec.model.compliance.report.PackageSubject;
import org.eclipse.fennec.model.compliance.report.RelevanceLevel;

/**
 * The two metamodel reviews the compiled unit of {@link QvtFlowAnalysisTest} was compiled against.
 * <p>
 * They are the reviews the probe of 2026-09-23 actually used - the classifications, relevance
 * levels and the one stated purpose are transcribed from
 * {@code report-clinic-human.json} and {@code report-contacts.json}, which were produced by an
 * agent and, for {@code Patient.diagnosis}, corrected by a person the same morning. Those two files
 * are not loaded directly: they are serialised against the report model as it stood before
 * {@code SubjectModel} became {@code PackageSubject}, so what survives of them is what they said,
 * which is the part the analysis depends on.
 * <p>
 * The citations are shortened to an identifier and a short quote. What matters here is not which
 * provision it is but that the analyser carries whatever it finds through unchanged.
 */
final class Reviews {

	static final String CLINIC_NS = "http://example.org/clinic/1.0.0";
	static final String CONTACTS_NS = "http://example.org/contacts/1.0.0";

	/** The fingerprints the unit's manifest names. Both joins turn on these being exact. */
	static final String CLINIC_FP = "fp1:5b87b0c624cead5c040e43d78ec02112c588f1531311354d7ea0cf1c425843cb";
	static final String CONTACTS_FP = "fp1:0bbb2a335cb6fbf49dbabde6b628abbecfe4f92f3675de534284dc7a4f3d84ca";

	/** The purpose a person entered on the diagnosis field, and the reason one rule fires at all. */
	static final String DIAGNOSIS_PURPOSE = "Stored to support the treating physician";

	private static final ReportFactory REPORTS = ReportFactory.eINSTANCE;

	private Reviews() {
	}

	/** The source model's review: a patient record, reviewed in full. */
	static ComplianceReport clinic() {
		ComplianceReport report = report("gdpr-fp1-5b87b0c6-20260923-human", "clinic", CLINIC_NS, CLINIC_FP,
				ReportOrigin.HUMAN);
		ClassifierEvaluation patient = classifier(report, "Patient");
		feature(patient, "//Patient/id", "DIRECT_IDENTIFIER", RelevanceLevel.HIGH, null, "Art.4(1)",
				"any information relating to an identified or identifiable natural person");
		feature(patient, "//Patient/fullName", "DIRECT_IDENTIFIER", RelevanceLevel.HIGH, null,
				"Art.4(1)", "any information relating to an identified or identifiable natural person");
		feature(patient, "//Patient/email", "DIRECT_IDENTIFIER", RelevanceLevel.HIGH, null, "Art.4(1)",
				"any information relating to an identified or identifiable natural person");
		feature(patient, "//Patient/birthDate", "QUASI_IDENTIFIER", RelevanceLevel.MEDIUM, null,
				"Rec.26", "account should be taken of all the means reasonably likely to be used");
		feature(patient, "//Patient/postcode", "QUASI_IDENTIFIER", RelevanceLevel.MEDIUM, null,
				"Rec.26", "account should be taken of all the means reasonably likely to be used");
		feature(patient, "//Patient/diagnosis", "SPECIAL_CATEGORY", RelevanceLevel.HIGH,
				DIAGNOSIS_PURPOSE, "Art.9(1)", "data concerning health");
		feature(patient, "//Patient/appointments", "SPECIAL_CATEGORY", RelevanceLevel.HIGH, null,
				"Art.9(1)", "data concerning health");

		ClassifierEvaluation physician = classifier(report, "Physician");
		feature(physician, "//Physician/name", "DIRECT_IDENTIFIER", RelevanceLevel.MEDIUM, null,
				"Art.4(1)", "any information relating to an identified or identifiable natural person");
		return report;
	}

	/** The target model's review: written for the probe, with one deliberately free-text field. */
	static ComplianceReport contacts() {
		ComplianceReport report = report("gdpr-fp1-0bbb2a33-20260923-121250", "contacts", CONTACTS_NS, CONTACTS_FP,
				ReportOrigin.AI_AGENT);
		ClassifierEvaluation contact = classifier(report, "Contact");
		// Weaker than the DIRECT_IDENTIFIER that arrives in it - the disagreement the probe missed
		feature(contact, "//Contact/reference", "PERSONAL_DATA", RelevanceLevel.HIGH, null, "Art.4(1)",
				"any information relating to an identified or identifiable natural person");
		feature(contact, "//Contact/displayName", "DIRECT_IDENTIFIER", RelevanceLevel.HIGH, null,
				"Art.4(1)", "any information relating to an identified or identifiable natural person");
		feature(contact, "//Contact/contactEmail", "DIRECT_IDENTIFIER", RelevanceLevel.HIGH, null,
				"Art.4(1)", "any information relating to an identified or identifiable natural person");
		feature(contact, "//Contact/comment", "PERSONAL_DATA", RelevanceLevel.MEDIUM, null, "Art.4(1)",
				"any information relating to an identified or identifiable natural person");
		return report;
	}

	/* ------------------------------------------------------------------ building blocks */

	private static ComplianceReport report(String reportId, String name, String nsURI, String fingerprint,
			ReportOrigin origin) {
		ComplianceReport report = REPORTS.createComplianceReport();
		report.setReportId(reportId);
		report.setName("GDPR review of " + name);
		report.setGeneratedAt("2026-09-23T08:51:08Z");
		report.setOrigin(origin);
		PackageSubject subject = REPORTS.createPackageSubject();
		subject.setName(name);
		subject.setNsURI(nsURI);
		subject.setSubjectFingerprint(fingerprint);
		report.setSubject(subject);
		ContextRef context = ContextFactory.eINSTANCE.createContextRef();
		context.setContextId("gdpr");
		context.setContextVersion("20160504");
		report.getContexts().add(context);
		report.setLanguage("EN");
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

	private static void feature(ClassifierEvaluation classifier, String uriFragment, String category,
			RelevanceLevel relevance, String purpose, String citationId, String quote) {
		FeatureEvaluation feature = REPORTS.createFeatureEvaluation();
		feature.setId(uriFragment);
		feature.setName(uriFragment.substring(uriFragment.lastIndexOf('/') + 1));
		feature.setUriFragment(uriFragment);
		feature.setRelevanceLevel(relevance);
		feature.setPurpose(purpose);
		Finding finding = REPORTS.createFinding();
		finding.setId(uriFragment + "-1");
		finding.getCategories().add(categoryRef(category));
		finding.setRelevanceLevel(relevance);
		finding.setRationale("The reviewer's reason for " + uriFragment + ".");
		Evidence evidence = REPORTS.createEvidence();
		evidence.setCitationId(citationId);
		evidence.setQuote(quote);
		evidence.setVerbatim(true);
		evidence.setSourceRef("celex:02016R0679-20160504#" + citationId);
		evidence.setRelevance("The reviewer's relevance for " + uriFragment + ".");
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
