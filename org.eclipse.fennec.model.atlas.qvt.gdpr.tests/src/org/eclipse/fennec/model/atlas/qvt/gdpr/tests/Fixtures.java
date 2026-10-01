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
package org.eclipse.fennec.model.atlas.qvt.gdpr.tests;

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
import org.eclipse.fennec.m2x.model.compiled.CompiledFactory;
import org.eclipse.fennec.m2x.model.compiled.CompiledUnit;
import org.eclipse.fennec.m2x.model.compiled.CompiledUnitManifest;
import org.eclipse.fennec.m2x.model.compiled.PackageEntry;
import org.eclipse.fennec.m2x.model.compiled.PackageRole;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.eclipse.fennec.model.gdprReport.ClassifierEvaluation;
import org.eclipse.fennec.model.gdprReport.DataCategory;
import org.eclipse.fennec.model.gdprReport.Evidence;
import org.eclipse.fennec.model.gdprReport.FeatureEvaluation;
import org.eclipse.fennec.model.gdprReport.Finding;
import org.eclipse.fennec.model.gdprReport.FlowEvaluation;
import org.eclipse.fennec.model.gdprReport.FlowKind;
import org.eclipse.fennec.model.gdprReport.GDPRReportFactory;
import org.eclipse.fennec.model.gdprReport.GdprReport;
import org.eclipse.fennec.model.gdprReport.PackageSubject;
import org.eclipse.fennec.model.gdprReport.RelevanceLevelType;
import org.eclipse.fennec.model.gdprReport.TransformationSubject;

/**
 * The metamodel, its review, and the compiled unit that reads it - plus waiting for what the
 * actions make of them.
 */
final class Fixtures {

	static final String MODEL_ID = "person-package";
	static final String MODEL_NS_URI = "http://test.fennec.eclipse.org/qvt-gdpr/person/1.0.0";
	static final String REVIEWED_FEATURE = "//Person/birthDate";

	static final String UNIT_ID = "clinic2contacts-unit";
	static final String UNIT_NAME = "clinic2contacts";
	static final String UNIT_FINGERPRINT = "m2x1:0f0f0f0f";

	/** The producer the transformation projection owns on every model it touches. */
	static final String PRODUCER = "gdpr.transformation/" + UNIT_NAME;

	private static final GDPRReportFactory REPORTS = GDPRReportFactory.eINSTANCE;
	private static final CompiledFactory UNITS = CompiledFactory.eINSTANCE;
	private static final long TIMEOUT_MS = 30_000;
	private static final long POLL_MS = 100;

	private Fixtures() {
	}

	/* ------------------------------------------------------------------ the metamodel */

	/**
	 * Stores the metamodel in one stage and answers the fingerprint the server computed for it.
	 * That value, and not one a test invents, is what the unit's manifest and the review both have
	 * to name: the whole join rests on the three being the same string.
	 */
	static String storeModel(WritableScopeService<EObject> scope, String stage) throws Exception {
		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		ePackage.setName("person");
		ePackage.setNsURI(MODEL_NS_URI);
		ePackage.setNsPrefix("person");
		EClass person = EcoreFactory.eINSTANCE.createEClass();
		person.setName("Person");
		EAttribute birthDate = EcoreFactory.eINSTANCE.createEAttribute();
		birthDate.setName("birthDate");
		birthDate.setEType(EcorePackage.Literals.EDATE);
		person.getEStructuralFeatures().add(birthDate);
		ePackage.getEClassifiers().add(person);

		ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadata.setObjectId(MODEL_ID);
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

	/* ------------------------------------------------------------------ the compiled unit */

	/**
	 * A compiled unit whose manifest pins one metamodel revision.
	 * <p>
	 * Its {@code unit} is a bare EPackage rather than a parsed QVT module: what these tests are
	 * about is which review the analyser goes looking for and where, and that is decided entirely
	 * by the manifest. A unit with no mappings yields a report with a subject and no flows, which
	 * is exactly the observable they need and keeps the whole QVT compiler out of the runtime.
	 */
	static CompiledUnit unit(String modelFingerprint) {
		CompiledUnit unit = UNITS.createCompiledUnit();
		unit.setId(UNIT_ID);
		CompiledUnitManifest manifest = UNITS.createCompiledUnitManifest();
		manifest.setQualifiedName(UNIT_NAME);
		manifest.setLanguage("qvto");
		manifest.setUnitFingerprint(UNIT_FINGERPRINT);
		manifest.setSourceFingerprint("m2x1:50urce");
		PackageEntry entry = UNITS.createPackageEntry();
		entry.setNsURI(MODEL_NS_URI);
		entry.setFingerprint(modelFingerprint);
		entry.setScheme("fp1");
		entry.setRole(PackageRole.EMBEDDED);
		manifest.getPackageEntry().add(entry);
		unit.setManifest(manifest);

		EPackage module = EcoreFactory.eINSTANCE.createEPackage();
		module.setName(UNIT_NAME);
		module.setNsURI("http://test.fennec.eclipse.org/qvt-gdpr/" + UNIT_NAME);
		module.setNsPrefix(UNIT_NAME);
		unit.setUnit(module);
		return unit;
	}

	/**
	 * Stores the compiled unit into one stage of the transformation registry, the way
	 * {@code AtlasUnitStore.upsert} does.
	 * <p>
	 * The one line that is easy to leave out and changes everything is the <b>fingerprint</b>: a
	 * compiled unit's {@code ObjectMetadata.fingerprint} is the {@code m2x1:} value its own
	 * manifest states, not something the server computed over the bytes. The re-analysis finds a
	 * stale unit by exactly that value, read off the transformation report's subject - so a fixture
	 * that lets the server compute one instead stores a unit nothing can ever find again.
	 */
	static void storeUnit(WritableScopeService<EObject> scope, CompiledUnit unit, String stage) throws Exception {
		ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadata.setObjectId(UNIT_ID);
		metadata.setObjectName(UNIT_NAME);
		metadata.setUploadTime(Instant.now());
		metadata.setObjectType(EcoreUtil.getURI(unit.eClass()).toString());
		metadata.setFingerprint(unit.getManifest().getUnitFingerprint());
		ObjectMetadata written = scope.uploadToStageForRegistry(TestAnnotations.UNIT_REGISTRY, stage, unit, metadata)
				.getValue();
		if (!UNIT_FINGERPRINT.equals(written.getFingerprint())) {
			fail("The registry replaced the unit's own m2x1 fingerprint with " + written.getFingerprint()
					+ "; nothing would find this unit again");
		}
	}

	/* ------------------------------------------------------------------ the reviews */

	/** A review of one metamodel revision, carried out at a stated time. */
	static GdprReport review(String reportId, String fingerprint, String generatedAt) {
		GdprReport report = REPORTS.createGdprReport();
		report.setReportId(reportId);
		report.setName("GDPR review of person");
		report.setGeneratedAt(generatedAt);
		report.setGeneratedBy("claude-opus-5");

		PackageSubject subject = REPORTS.createPackageSubject();
		subject.setName("person");
		subject.setNsURI(MODEL_NS_URI);
		subject.setSubjectFingerprint(fingerprint);
		report.setSubject(subject);
		report.setCorpus(REPORTS.createLegalCorpusRef());
		report.getCorpus().setCelex("32016R0679");
		report.getCorpus().setLanguage("EN");

		ClassifierEvaluation classifier = REPORTS.createClassifierEvaluation();
		classifier.setId("Person");
		classifier.setUriFragment("//Person");
		FeatureEvaluation feature = REPORTS.createFeatureEvaluation();
		feature.setId("Person.birthDate");
		feature.setUriFragment(REVIEWED_FEATURE);
		feature.setRelevanceLevel(RelevanceLevelType.MEDIUM);
		feature.getFindings().add(finding("F-001", DataCategory.QUASI_IDENTIFIER,
				"A date of birth contributes to singling out an individual."));
		classifier.getFeatureEvaluation().add(feature);
		report.getEvaluation().add(classifier);
		return report;
	}

	/**
	 * A report about the transformation, as the analyser would have written it - used where a test
	 * is about what happens to a stored transformation report rather than about deriving one.
	 */
	static GdprReport transformationReport(String reportId, String modelFingerprint) {
		GdprReport report = REPORTS.createGdprReport();
		report.setReportId(reportId);
		report.setName("GDPR flow analysis of " + UNIT_NAME);
		report.setGeneratedAt("2026-10-01T09:00:00Z");
		report.setGeneratedBy("qvt-flow-analysis/1");
		report.setCorpus(REPORTS.createLegalCorpusRef());
		report.getCorpus().setCelex("32016R0679");

		TransformationSubject subject = REPORTS.createTransformationSubject();
		subject.setQualifiedName(UNIT_NAME);
		subject.setLanguage("qvto");
		subject.setSubjectFingerprint(UNIT_FINGERPRINT);
		PackageSubject source = REPORTS.createPackageSubject();
		source.setNsURI(MODEL_NS_URI);
		source.setSubjectFingerprint(modelFingerprint);
		source.setReportId("review-1");
		subject.getSourcePackages().add(source);
		report.setSubject(subject);

		// One flow out of the reviewed feature, so the projection has something to say about it.
		FlowEvaluation flow = REPORTS.createFlowEvaluation();
		flow.setId("toContact#" + REVIEWED_FEATURE + "->//Contact/comment");
		flow.setMapping("toContact");
		flow.setSourceNsURI(MODEL_NS_URI);
		flow.setSourceFeature(REVIEWED_FEATURE);
		flow.setTargetNsURI("http://test.fennec.eclipse.org/qvt-gdpr/contact/1.0.0");
		flow.setTargetFeature("//Contact/comment");
		flow.setFlowKind(FlowKind.CONCATENATION);
		flow.setRelevanceLevel(RelevanceLevelType.MEDIUM);
		// A statement about the SOURCE end, so it lands on the model this test stores.
		flow.getFindings().add(finding("gdpr.flow.structure-loss:toContact#//Contact/comment",
				DataCategory.QUASI_IDENTIFIER, "//Contact/comment cannot carry that classification."));
		report.getEvaluation().add(flow);
		return report;
	}

	private static Finding finding(String id, DataCategory category, String rationale) {
		Finding finding = REPORTS.createFinding();
		finding.setId(id);
		finding.setCategory(category);
		finding.setRelevanceLevel(RelevanceLevelType.MEDIUM);
		finding.setRationale(rationale);
		Evidence evidence = REPORTS.createEvidence();
		evidence.setCitationId("Rec.26");
		evidence.setQuote("all the means reasonably likely to be used");
		evidence.setVerbatim(true);
		finding.getEvidence().add(evidence);
		return finding;
	}

	/** Stores a report into one stage of the report registry, the way the REST resource does. */
	static void storeReport(WritableScopeService<EObject> scope, GdprReport report, String stage) throws Exception {
		ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadata.setObjectId(report.getReportId());
		metadata.setObjectName(report.getName());
		metadata.setUploadTime(Instant.now());
		metadata.setObjectType(EcoreUtil.getURI(report.eClass()).toString());
		scope.uploadToStageForRegistry(TestAnnotations.REPORT_REGISTRY, stage, report, metadata).getValue();
	}

	/* ------------------------------------------------------------------ reading back */

	/**
	 * The transformation report that currently speaks for the transformation in a stage.
	 * <p>
	 * The <b>latest</b> of them, and not whichever the registry lists first. A re-analysis whose
	 * inputs moved writes a <em>new</em> report rather than replacing the old one - that is what
	 * makes two revisions of a judgement comparable, and it is the whole reason the report id
	 * covers its inputs - so a stage legitimately holds several, and the one that speaks is the
	 * most recent. The same reading the projection onto the metamodels uses.
	 */
	static GdprReport derivedReport(WritableScopeService<EObject> scope, String stage) {
		GdprReport latest = null;
		Instant latestAt = null;
		String latestId = null;
		for (ObjectMetadata metadata : scope.listInStageForRegistry(TestAnnotations.REPORT_REGISTRY, stage)) {
			EObject content = scope.getContentFromStageForRegistry(TestAnnotations.REPORT_REGISTRY, stage,
					metadata.getObjectId());
			if (!(content instanceof GdprReport report)
					|| !(report.getSubject() instanceof TransformationSubject)) {
				continue;
			}
			Instant at = Instant.parse(report.getGeneratedAt());
			if (latest == null || at.isAfter(latestAt)
					|| (at.equals(latestAt) && metadata.getObjectId().compareTo(latestId) > 0)) {
				latest = report;
				latestAt = at;
				latestId = metadata.getObjectId();
			}
		}
		return latest;
	}

	/** The transformation report the analyser stored, by its reportId, or {@code null}. */
	static GdprReport reportById(WritableScopeService<EObject> scope, String stage, String reportId) {
		EObject content = scope.getContentFromStageForRegistry(TestAnnotations.REPORT_REGISTRY, stage, reportId);
		return content instanceof GdprReport report ? report : null;
	}

	/** What a derived report says it rested on, for the one metamodel its unit names. */
	static PackageSubject restedOn(GdprReport report) {
		TransformationSubject subject = (TransformationSubject) report.getSubject();
		return subject.getSourcePackages().isEmpty() ? null : subject.getSourcePackages().get(0);
	}

	/** How many transformation reports a stage holds. */
	static int transformationReports(WritableScopeService<EObject> scope, String stage) {
		int count = 0;
		for (ObjectMetadata metadata : scope.listInStageForRegistry(TestAnnotations.REPORT_REGISTRY, stage)) {
			EObject content = scope.getContentFromStageForRegistry(TestAnnotations.REPORT_REGISTRY, stage,
					metadata.getObjectId());
			if (content instanceof GdprReport report && report.getSubject() instanceof TransformationSubject) {
				count++;
			}
		}
		return count;
	}

	/** This producer's roots on the metamodel in one stage. */
	static List<Diagnostic> owned(WritableScopeService<EObject> scope, String stage) {
		ObjectMetadata metadata = scope.getMetadataFromStageForRegistry(TestAnnotations.SCHEMA_REGISTRY_NAME, stage,
				MODEL_ID);
		if (metadata == null) {
			return List.of();
		}
		return metadata.getDiagnostics().stream().filter(root -> PRODUCER.equals(root.getProducer())).toList();
	}

	/* ------------------------------------------------------------------ waiting */

	/**
	 * Waits until the condition holds of whatever {@code read} answers.
	 * <p>
	 * Polling, because the workflow dispatches the actions after the store, and the store call the
	 * test makes has already returned by then.
	 */
	static <T> T await(java.util.function.Supplier<T> read, Predicate<T> until, String what) {
		long deadline = System.currentTimeMillis() + TIMEOUT_MS;
		T last = null;
		while (System.currentTimeMillis() < deadline) {
			last = read.get();
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
		return fail("Timed out after " + TIMEOUT_MS + "ms waiting for " + what + "; last saw " + last);
	}

	/**
	 * Gives the actions time to do nothing. There is no event to wait for when the expected outcome
	 * is that nothing is written, so this is a bounded pause rather than a poll.
	 */
	static void settle() throws InterruptedException {
		Thread.sleep(1_500);
	}
}
