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
package org.eclipse.fennec.model.atlas.gdpr.diagnostics.tests;

import static org.junit.jupiter.api.Assertions.fail;

import java.time.Instant;
import java.util.List;
import java.util.function.Predicate;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.eclipse.fennec.model.gdprReport.ClassifierEvaluation;
import org.eclipse.fennec.model.gdprReport.DataCategory;
import org.eclipse.fennec.model.gdprReport.Evidence;
import org.eclipse.fennec.model.gdprReport.FeatureEvaluation;
import org.eclipse.fennec.model.gdprReport.Finding;
import org.eclipse.fennec.model.gdprReport.GDPRReportFactory;
import org.eclipse.fennec.model.gdprReport.GdprReport;
import org.eclipse.fennec.model.gdprReport.PackageSubject;
import org.eclipse.fennec.model.gdprReport.RelevanceLevelType;
import org.eclipse.fennec.model.gdprReport.TransformationSubject;

/**
 * The package under review, the review of it, and waiting for what the action makes of the two.
 */
final class Fixtures {

	static final String PACKAGE_ID = "person-package";
	static final String PACKAGE_NS_URI = "http://test.fennec.eclipse.org/gdpr/person/1.0.0";
	/** The one feature the fixture review finds something on. */
	static final String REVIEWED_FEATURE = "//Person/birthDate";
	/** The producer whose roots the action owns. */
	static final String PRODUCER = "gdpr.review";

	private static final GDPRReportFactory REPORTS = GDPRReportFactory.eINSTANCE;
	private static final long TIMEOUT_MS = 30_000;
	private static final long POLL_MS = 100;

	private Fixtures() {
	}

	/**
	 * Stores the reviewed package and answers the fingerprint the server computed for it. That
	 * value, and not one a test invents, is what the report has to name: the whole join rests on
	 * the two being the same string.
	 */
	static String storePackage(WritableScopeService<EObject> scope, String stage) throws Exception {
		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		ePackage.setName("person");
		ePackage.setNsURI(PACKAGE_NS_URI);
		ePackage.setNsPrefix("person");
		EClass person = EcoreFactory.eINSTANCE.createEClass();
		person.setName("Person");
		EAttribute birthDate = EcoreFactory.eINSTANCE.createEAttribute();
		birthDate.setName("birthDate");
		birthDate.setEType(EcorePackage.Literals.EDATE);
		person.getEStructuralFeatures().add(birthDate);
		ePackage.getEClassifiers().add(person);

		ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadata.setObjectId(PACKAGE_ID);
		metadata.setObjectName("person");
		metadata.setUploadTime(Instant.now());
		metadata.setObjectType(EcoreUtil.getURI(ePackage.eClass()).toString());
		ObjectMetadata written = scope
				.uploadToStageForRegistry(TestAnnotations.SCHEMA_REGISTRY_NAME, stage, ePackage, metadata).getValue();
		if (written.getFingerprint() == null) {
			fail("The stored EPackage carries no fingerprint, so nothing can be joined to it");
		}
		return written.getFingerprint();
	}

	/** A review of one revision that found something on one feature. */
	static GdprReport review(String reportId, String fingerprint, DataCategory category,
			RelevanceLevelType relevance) {
		return review(reportId, fingerprint, category, relevance, "2026-09-28T10:00:00Z");
	}

	/** The same, carried out at a stated time; which review speaks for a revision turns on it. */
	static GdprReport review(String reportId, String fingerprint, DataCategory category,
			RelevanceLevelType relevance, String generatedAt) {
		GdprReport report = REPORTS.createGdprReport();
		report.setReportId(reportId);
		report.setName("GDPR review of person");
		report.setGeneratedAt(generatedAt);
		report.setGeneratedBy("claude-opus-5");

		PackageSubject subject = REPORTS.createPackageSubject();
		subject.setName("person");
		subject.setNsURI(PACKAGE_NS_URI);
		subject.setSubjectFingerprint(fingerprint);
		report.setSubject(subject);

		report.setCorpus(REPORTS.createLegalCorpusRef());
		report.getCorpus().setCelex("32016R0679");
		report.getCorpus().setLanguage("DE");

		ClassifierEvaluation classifier = REPORTS.createClassifierEvaluation();
		classifier.setId("Person");
		classifier.setName("Person");
		classifier.setUriFragment("//Person");
		FeatureEvaluation feature = REPORTS.createFeatureEvaluation();
		feature.setId("Person.birthDate");
		feature.setName("birthDate");
		feature.setUriFragment(REVIEWED_FEATURE);
		feature.setRelevanceLevel(relevance);
		Finding finding = REPORTS.createFinding();
		finding.setId("F-001");
		finding.setCategory(category);
		finding.setRelevanceLevel(relevance);
		finding.setRationale("A date of birth contributes to singling out an individual.");
		Evidence evidence = REPORTS.createEvidence();
		evidence.setCitationId("Rec.26");
		evidence.setQuote("...");
		evidence.setVerbatim(true);
		finding.getEvidence().add(evidence);
		feature.getFindings().add(finding);
		classifier.getFeatureEvaluation().add(feature);
		report.getEvaluation().add(classifier);
		return report;
	}

	/**
	 * The same review, but of a compiled transformation instead of a package - carrying whatever
	 * fingerprint the caller hands it, a package's included.
	 * <p>
	 * That is deliberate and is the point of the fixture: in a real runtime a transformation's
	 * {@code m2x1:} fingerprint never collides with a package's {@code fp1:} one, so a test that
	 * relied on the prefixes would pass whether or not anything checks the subject type. Giving
	 * the transformation report the package's own fingerprint removes that accident, and what is
	 * left is the rule.
	 */
	static GdprReport transformationReview(String reportId, String fingerprint) {
		GdprReport report = review(reportId, fingerprint, DataCategory.SPECIAL_CATEGORY, RelevanceLevelType.HIGH);
		report.setName("GDPR review of clinic2contacts");
		TransformationSubject subject = REPORTS.createTransformationSubject();
		subject.setQualifiedName("clinic2contacts");
		subject.setLanguage("qvto");
		subject.setSubjectFingerprint(fingerprint);
		report.setSubject(subject);
		return report;
	}

	/** Stores a review into one stage of the report registry, the way the REST resource does. */
	static void storeReview(WritableScopeService<EObject> scope, GdprReport report, String stage) throws Exception {
		ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadata.setObjectId(report.getReportId());
		metadata.setObjectName(report.getName());
		metadata.setUploadTime(Instant.now());
		metadata.setObjectType(EcoreUtil.getURI(report.eClass()).toString());
		scope.uploadToStageForRegistry(TestAnnotations.REPORT_REGISTRY, stage, report, metadata).getValue();
	}

	/** The reviewed package's metadata as the registry holds it now. */
	static ObjectMetadata metadata(WritableScopeService<EObject> scope, String stage) {
		return scope.getMetadataFromStageForRegistry(TestAnnotations.SCHEMA_REGISTRY_NAME, stage, PACKAGE_ID);
	}

	/** This producer's roots on the reviewed package, in the order they were written. */
	static List<Diagnostic> owned(WritableScopeService<EObject> scope, String stage) {
		ObjectMetadata metadata = metadata(scope, stage);
		if (metadata == null) {
			return List.of();
		}
		return metadata.getDiagnostics().stream().filter(root -> PRODUCER.equals(root.getProducer())).toList();
	}

	/**
	 * Waits until this producer's roots on the package satisfy the condition.
	 * <p>
	 * Polling, because the workflow dispatches the action after the report is stored and the store
	 * call the test makes has already returned by then.
	 */
	static List<Diagnostic> await(WritableScopeService<EObject> scope, String stage, Predicate<List<Diagnostic>> until,
			String what) {
		long deadline = System.currentTimeMillis() + TIMEOUT_MS;
		List<Diagnostic> last = List.of();
		while (System.currentTimeMillis() < deadline) {
			last = owned(scope, stage);
			if (until.test(last)) {
				return last;
			}
			try {
				Thread.sleep(POLL_MS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				fail("interrupted while waiting for " + what);
			}
		}
		return fail("Timed out after " + TIMEOUT_MS + "ms waiting for " + what + "; the package carries "
				+ last.size() + " '" + PRODUCER + "' root(s)");
	}

	/**
	 * Gives the action time to do nothing. There is no event to wait for when the expected outcome
	 * is that nothing is written, so this is a bounded pause rather than a poll - short, because
	 * the whole dispatch is synchronous with the store that triggered it.
	 */
	static void settle() throws InterruptedException {
		Thread.sleep(1_000);
	}
}
