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
import org.eclipse.fennec.model.compliance.report.CombinationFinding;
import org.eclipse.fennec.model.compliance.report.DetectionSignal;
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

	/** The corpus within that context the citations were read out of. */
	static final String CORPUS_ID = "gdpr-regulation";

	/** The version of the GDPR context both reviews were made against. */
	static final String CONTEXT_VERSION = "20160504";

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
		FeatureEvaluation id = feature(patient, "//Patient/id", "DIRECT_IDENTIFIER", RelevanceLevel.HIGH, null,
				"Art.4(1)", "any information relating to an identified or identifiable natural person");
		FeatureEvaluation fullName = feature(patient, "//Patient/fullName", "DIRECT_IDENTIFIER",
				RelevanceLevel.HIGH, null, "Art.4(1)",
				"any information relating to an identified or identifiable natural person");
		FeatureEvaluation email = feature(patient, "//Patient/email", "DIRECT_IDENTIFIER", RelevanceLevel.HIGH,
				null, "Art.4(1)", "any information relating to an identified or identifiable natural person");
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
		// A second field citing the provision its neighbour cites, in the reviewer's same words, and
		// read by no mapping either. Two unread features behind one identical citation is the
		// ordinary case in a real review - two name fields of one class - and it is what tells
		// whether the aggregated finding still names both of them.
		FeatureEvaluation physicianEmail = feature(physician, "//Physician/email", "DIRECT_IDENTIFIER",
				RelevanceLevel.MEDIUM, null, "Art.4(1)",
				"any information relating to an identified or identifiable natural person");
		physicianEmail.getFindings().get(0).getEvidence().get(0)
				.setRelevance("The reviewer's relevance for //Physician/name.");

		// What the reviewer said about the three fields together, which is a statement neither the
		// contacts review nor any one of those fields can make.
		combination(report, "CF-001", "QUASI_IDENTIFIER_SET", "DIRECT_IDENTIFIER", RelevanceLevel.HIGH,
				"Rec.26", "singling out", id, fullName, email);
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
		context.setContextVersion(CONTEXT_VERSION);
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

	private static FeatureEvaluation feature(ClassifierEvaluation classifier, String uriFragment, String category,
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
		finding.getEvidence().add(evidence(citationId, quote, "The reviewer's relevance for " + uriFragment + "."));
		feature.getFindings().add(finding);
		classifier.getFeatureEvaluations().add(feature);
		return feature;
	}

	/**
	 * A combination the reviewer raised over several features, the way a review records one: the
	 * kind in the {@code combination-kinds} taxonomy, the category of the set beside it, and the
	 * members as references to the feature evaluations rather than copies of them.
	 */
	private static CombinationFinding combination(ComplianceReport report, String id, String kind,
			String category, RelevanceLevel relevance, String citationId, String quote,
			FeatureEvaluation... members) {
		CombinationFinding combination = REPORTS.createCombinationFinding();
		combination.setId(id);
		combination.setRelevanceLevel(relevance);
		combination.getCategories().add(kindRef(kind));
		combination.getCategories().add(categoryRef(category));
		combination.setRationale("The reviewer's reason for " + id + ".");
		combination.getDetectedBy().add(DetectionSignal.FEATURE_COMBINATION);
		combination.getEvidence().add(evidence(citationId, quote, "The reviewer's relevance for " + id + "."));
		for (FeatureEvaluation member : members) {
			combination.getFeatures().add(member);
		}
		report.getCombinations().add(combination);
		return combination;
	}

	/**
	 * One citation as a review records it: the context and the corpus it was read out of as well as
	 * the identifier and the quote. One context may hold several corpora, which is why the corpus is
	 * named and not inferred.
	 */
	private static Evidence evidence(String citationId, String quote, String relevance) {
		Evidence evidence = REPORTS.createEvidence();
		evidence.setContextId("gdpr");
		evidence.setCorpusId(CORPUS_ID);
		evidence.setCitationId(citationId);
		evidence.setQuote(quote);
		evidence.setVerbatim(true);
		evidence.setSourceRef("celex:02016R0679-20160504#" + citationId);
		evidence.setRelevance(relevance);
		return evidence;
	}

	/** A combination-kind reference: the same shape, a different taxonomy of the same context. */
	static CategoryRef kindRef(String kindId) {
		CategoryRef ref = ContextFactory.eINSTANCE.createCategoryRef();
		ref.setContextId("gdpr");
		ref.setContextVersion(CONTEXT_VERSION);
		ref.setTaxonomyId("combination-kinds");
		ref.setCategoryId(kindId);
		return ref;
	}

	/**
	 * A data-category reference, the way a review records one: an id in the context's
	 * {@code data-categories} taxonomy. The ids are the names the {@code DataCategory} enum had.
	 */
	static CategoryRef categoryRef(String categoryId) {
		CategoryRef ref = ContextFactory.eINSTANCE.createCategoryRef();
		ref.setContextId("gdpr");
		ref.setContextVersion(CONTEXT_VERSION);
		ref.setTaxonomyId("data-categories");
		ref.setCategoryId(categoryId);
		return ref;
	}

}
