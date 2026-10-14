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
package org.eclipse.fennec.model.atlas.compliance.prototype;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.emf.osgi.fingerprint.util.FingerprintHelper;
import org.eclipse.fennec.event.atlas.model.mapping.MappingPackage;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.fennec.m2x.ocl.api.OclEngine;
import org.eclipse.fennec.m2x.ocl.engine.OclEngines;
import org.eclipse.fennec.m2x.ocl.parser.OclParserSupport;
import org.eclipse.fennec.model.atlas.compliance.analysis.RequirementCitations;
import org.eclipse.fennec.model.atlas.compliance.analysis.ReviewIndex;
import org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet;
import org.eclipse.fennec.model.compliance.context.ComplianceContext;
import org.eclipse.fennec.model.compliance.context.ContextPackage;
import org.eclipse.fennec.model.compliance.corpus.CorpusPackage;
import org.eclipse.fennec.model.compliance.report.ComplianceReport;
import org.eclipse.fennec.model.compliance.report.ControlEvaluation;
import org.eclipse.fennec.model.compliance.report.Evaluation;
import org.eclipse.fennec.model.compliance.report.Evidence;
import org.eclipse.fennec.model.compliance.report.Finding;
import org.eclipse.fennec.model.compliance.report.FlowEvaluation;
import org.eclipse.fennec.model.compliance.report.ReportPackage;
import org.eclipse.fennec.persistence.eorm.EntityMappings;
import org.eclipse.fennec.persistence.eorm.EORMPackage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The prototype's whole point: three static GDPR reports for three different artefacts, all
 * derived from one review of one metamodel.
 *
 * <p>
 * Each report is written to {@code generated/reports/} so it can be read rather than asserted
 * about. The assertions here only pin the things that would make the reports worthless if they
 * were wrong - that the join found the review, that evidence is real, and that a check which
 * could not run says so instead of passing.
 * </p>
 */
@DisplayName("One review, three static reports")
public class ThreeReportsTest {

	private static final String GDPR_CONTEXT = "docker/dockercompose/initial-models/scopes/dimcity/"
			+ "compliance-context/gdpr.xmi";

	@Test
	@DisplayName("an OCL constraint set, an eorm mapping and a sensiNact mapping each get a report")
	public void threeReportsFromOneReview() throws IOException {
		EPackage source = Fixtures.baederSource();
		String fingerprint = FingerprintHelper.fingerprint(source);
		ReviewIndex review = new ReviewIndex(Fixtures.baederSourceReview());
		RequirementCitations citations = new RequirementCitations(gdprContext());

		OclEngine engine = OclEngines.create(new OclParserSupport());

		ComplianceReport ocl = new OclConstraintAnalysis(engine, review, citations, source, fingerprint)
				.analyse((OclConstraintSet) artefact("data/baeder-constraints.cocl.xmi"));
		ComplianceReport eorm = new EormMappingAnalysis(review, citations, source, fingerprint)
				.analyse((EntityMappings) artefact("data/baeder-persistence.eorm.xmi"));
		ComplianceReport sensinact = new SensinactMappingAnalysis(review, citations, source, fingerprint)
				.analyse((ProviderMapping) artefact("data/baeder-checkin.mapping.xmi"), "baeder-checkin");

		write("ocl-constraint-analysis.xmi", ocl);
		write("eorm-mapping-analysis.xmi", eorm);
		write("sensinact-mapping-analysis.xmi", sensinact);
		summarise(ocl, eorm, sensinact);

		for (ComplianceReport report : List.of(ocl, eorm, sensinact)) {
			assertFalse(report.getEvaluations().isEmpty(),
					() -> report.getName() + " produced nothing, which for a classified model means the join "
							+ "to the review failed rather than that there is nothing to say");
			assertEquals(fingerprint,
					((org.eclipse.fennec.model.compliance.report.TransformationSubject) report.getSubject())
							.getSourcePackages().get(0).getSubjectFingerprint(),
					"every report pins the revision it was derived against");
			assertFalse(report.getContexts().isEmpty(), "a report must name the context its citations came from");
		}
	}

	@Test
	@DisplayName("every finding carries a real quote, by one of the two routes")
	public void everyFindingIsEvidenced() throws IOException {
		EPackage source = Fixtures.baederSource();
		String fingerprint = FingerprintHelper.fingerprint(source);
		ReviewIndex review = new ReviewIndex(Fixtures.baederSourceReview());
		RequirementCitations citations = new RequirementCitations(gdprContext());

		ComplianceReport eorm = new EormMappingAnalysis(review, citations, source, fingerprint)
				.analyse((EntityMappings) artefact("data/baeder-persistence.eorm.xmi"));

		int checked = 0;
		for (Evaluation evaluation : eorm.getEvaluations()) {
			for (Finding finding : findingsOf(evaluation)) {
				assertFalse(finding.getEvidence().isEmpty(),
						() -> finding.getId() + " has no evidence, and Finding.evidence is mandatory");
				for (Evidence evidence : finding.getEvidence()) {
					assertTrue(evidence.getQuote() != null && !evidence.getQuote().isBlank(),
							() -> finding.getId() + " carries an empty quote");
					assertTrue(evidence.getCitationId() != null && !evidence.getCitationId().isBlank(),
							() -> finding.getId() + " carries a quote nobody can look up");
					checked++;
				}
			}
		}
		assertTrue(checked > 0, "the eorm report raised no finding at all, so nothing was proved about evidence");
	}

	@Test
	@DisplayName("a check that could not run says so, instead of passing")
	public void unassessedIsNotSatisfied() throws IOException {
		EPackage source = Fixtures.baederSource();
		ReviewIndex review = new ReviewIndex(Fixtures.baederSourceReview());
		RequirementCitations citations = new RequirementCitations(gdprContext());

		ComplianceReport sensinact = new SensinactMappingAnalysis(review, citations, source,
				FingerprintHelper.fingerprint(source))
				.analyse((ProviderMapping) artefact("data/baeder-checkin.mapping.xmi"), "baeder-checkin");

		boolean notAssessed = sensinact.getEvaluations().stream()
				.filter(ControlEvaluation.class::isInstance).map(ControlEvaluation.class::cast)
				.anyMatch(control -> control.getResult() == org.eclipse.fennec.model.compliance.report.ControlResult.NOT_ASSESSED);

		assertTrue(notAssessed,
				"the historisation rules need an EventAtlasDeployment and no registry holds one; a retention "
						+ "check that cannot run must be NOT_ASSESSED, never absent and never SATISFIED");
	}

	/* ------------------------------------------------------------------------------- plumbing */

	private static List<Finding> findingsOf(Evaluation evaluation) {
		if (evaluation instanceof FlowEvaluation flow) {
			return flow.getFindings();
		}
		if (evaluation instanceof ControlEvaluation control) {
			return control.getFindings();
		}
		return List.of();
	}

	/** One of the three artefacts, loaded off the test classpath. */
	private static org.eclipse.emf.ecore.EObject artefact(String path) throws IOException {
		ResourceSet resourceSet = resourceSet();
		Resource resource = resourceSet.createResource(URI.createURI(path));
		try (InputStream in = ThreeReportsTest.class.getResourceAsStream(path)) {
			assertTrue(in != null, () -> "fixture not on the test classpath: " + path);
			resource.load(in, Map.of());
		}
		// The artefacts reference the reviewed package by nsURI, so it has to be the very EPackage
		// the review is about - not a second copy with the same nsURI and a different identity.
		resourceSet.getPackageRegistry().put(Fixtures.baederSource().getNsURI(), Fixtures.baederSource());
		org.eclipse.emf.ecore.util.EcoreUtil.resolveAll(resourceSet);
		return resource.getContents().get(0);
	}

	/** The seeded GDPR context, 1.3 MB of consolidated Regulation, for the dereferenced route. */
	private static ComplianceContext gdprContext() throws IOException {
		Path file = repositoryRoot().resolve(GDPR_CONTEXT);
		assertTrue(Files.isReadable(file), () -> "seeded GDPR context not readable: " + file);
		Resource resource = resourceSet()
				.getResource(URI.createFileURI(file.toAbsolutePath().toString()), true);
		return (ComplianceContext) resource.getContents().get(0);
	}

	private static ResourceSet resourceSet() {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		for (EPackage ePackage : List.of(EcorePackage.eINSTANCE, ReportPackage.eINSTANCE, ContextPackage.eINSTANCE,
				CorpusPackage.eINSTANCE, COCLPackage.eINSTANCE, EORMPackage.eINSTANCE, MappingPackage.eINSTANCE)) {
			resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
		}
		resourceSet.getPackageRegistry().put(Fixtures.baederSource().getNsURI(), Fixtures.baederSource());
		return resourceSet;
	}

	private static Path repositoryRoot() {
		Path start = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
		for (Path candidate = start; candidate != null; candidate = candidate.getParent()) {
			if (Files.isDirectory(candidate.resolve("docker/dockercompose/initial-models"))) {
				return candidate;
			}
		}
		throw new IllegalStateException("no repository root above " + start);
	}

	/** Writes a report where a person can open it. The prototype's actual deliverable. */
	private static void write(String name, ComplianceReport report) throws IOException {
		Path out = Paths.get("generated", "reports");
		Files.createDirectories(out);
		ResourceSet resourceSet = resourceSet();
		Resource resource = resourceSet
				.createResource(URI.createFileURI(out.resolve(name).toAbsolutePath().toString()));
		resource.getContents().add(report);
		resource.save(Map.of());
	}

	/** Printed, not asserted: what the three reports actually say, for a human to judge. */
	private static void summarise(ComplianceReport... reports) {
		for (ComplianceReport report : reports) {
			long findings = report.getEvaluations().stream().mapToLong(e -> findingsOf(e).size()).sum();
			long notAssessed = report.getEvaluations().stream().filter(ControlEvaluation.class::isInstance)
					.map(ControlEvaluation.class::cast)
					.filter(c -> c.getResult() == org.eclipse.fennec.model.compliance.report.ControlResult.NOT_ASSESSED)
					.count();
			System.out.printf("%-34s %2d evaluations, %2d findings, %d not assessed%n", report.getGeneratedBy(),
					report.getEvaluations().size(), findings, notAssessed);
			for (Evaluation evaluation : report.getEvaluations()) {
				for (Finding finding : findingsOf(evaluation)) {
					Evidence first = finding.getEvidence().get(0);
					System.out.printf("    %-58s %-14s %s%n", finding.getId(), finding.getRelevanceLevel(),
							first.getCitationId());
				}
			}
		}
	}
}
