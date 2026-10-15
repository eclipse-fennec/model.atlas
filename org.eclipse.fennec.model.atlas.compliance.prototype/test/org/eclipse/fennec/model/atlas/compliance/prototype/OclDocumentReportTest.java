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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.emf.osgi.fingerprint.util.FingerprintHelper;
import org.eclipse.fennec.m2x.model.compiled.CompiledPackage;
import org.eclipse.fennec.m2x.model.compiled.CompiledUnit;
import org.eclipse.fennec.m2x.model.compiled.PackageEntry;
import org.eclipse.fennec.m2x.model.compiled.UnitNature;
import org.eclipse.fennec.m2x.model.ocl.CompleteOclDocument;
import org.eclipse.fennec.m2x.model.ocl.OclPackage;
import org.eclipse.fennec.m2x.ocl.api.OclEngine;
import org.eclipse.fennec.m2x.ocl.engine.OclEngines;
import org.eclipse.fennec.m2x.ocl.parser.OclParserSupport;
import org.eclipse.fennec.model.atlas.compliance.analysis.RequirementCitations;
import org.eclipse.fennec.model.atlas.compliance.analysis.ReviewIndex;
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
import org.eclipse.fennec.model.compliance.report.TransformationSubject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The fourth artefact of the prototype, and the one the OCL plan now points at: a Complete OCL
 * document, compiled into an m2x {@code CompiledUnit} of language {@code ocl} and analysed from
 * there.
 *
 * <p>
 * The point being tested is not that a report comes out - the constraint-set analyser already shows
 * that - but that the <em>revision is recorded rather than reconstructed</em>. The compiler writes
 * a {@code PackageEntry} with the fingerprint it saw; the reviewer wrote the same value into the
 * review. If those two are not the same function, the whole reading is worthless, so that is
 * asserted first and on its own.
 * </p>
 *
 * <p>
 * The unit is written to XMI and read back before it is analysed, because that is what actually
 * happens in the Atlas: the compile action stores the unit and the analyser reads a stored
 * document. An AST that only joins while it is still in the compiler's memory would prove nothing.
 * </p>
 */
@DisplayName("A compiled Complete OCL document gets a report")
public class OclDocumentReportTest {

	private static final String GDPR_CONTEXT = "docker/dockercompose/initial-models/scopes/dimcity/"
			+ "compliance-context/gdpr.xmi";

	private static final String UNIT_NAME = "baeder.regeln";

	@Test
	@DisplayName("the fingerprint the compiler recorded is the one the reviewer wrote")
	public void theRecordedRevisionIsTheReviewKey() throws Exception {
		CompiledUnit unit = compile();

		assertEquals("ocl", unit.getManifest().getLanguage());
		assertEquals(UnitNature.LIBRARY, unit.getManifest().getNature(),
				"a document is installed into an engine, never executed");
		assertEquals(1, unit.getManifest().getPackageEntry().size(),
				"the document refers to one metamodel; Ecore and the OCL standard library are "
						+ "part of every runtime and are deliberately not recorded");

		PackageEntry entry = unit.getManifest().getPackageEntry().get(0);
		assertEquals(Fixtures.baederSource().getNsURI(), entry.getNsURI());
		assertEquals(FingerprintHelper.fingerprint(Fixtures.baederSource()), entry.getFingerprint(),
				"the compiler and the reviewer must fingerprint a package the same way, or every "
						+ "unit answers SOURCE_UNREVIEWED and the failure reads like a finding");
		assertEquals(Fixtures.baederSourceReview().getSubject().getSubjectFingerprint(), entry.getFingerprint(),
				"and that value is the key the stored review is found by");
	}

	@Test
	@DisplayName("the report is derived from the stored unit, not from the compiler's memory")
	public void aStoredUnitStillJoins() throws Exception {
		Path stored = write("baeder-regeln.unit.xmi", compile());
		CompiledUnit reloaded = reload(stored);

		ComplianceReport report = analyse(reloaded);
		write("ocl-document-analysis.xmi", report);
		summarise(report);

		assertFalse(report.getEvaluations().isEmpty(),
				"the document reads classified features of a reviewed model, so an empty report means the "
						+ "join failed rather than that there is nothing to say");
		TransformationSubject subject = assertInstanceOf(TransformationSubject.class, report.getSubject());
		assertEquals("ocl", subject.getLanguage());
		assertEquals(reloaded.getManifest().getUnitFingerprint(), subject.getSubjectFingerprint(),
				"the subject is the unit, so the metadata action finds the object the report is about");
		assertEquals(reloaded.getManifest().getPackageEntry().get(0).getFingerprint(),
				subject.getSourcePackages().get(0).getSubjectFingerprint(),
				"and the source package is the revision the manifest recorded, not one resolved now");
		assertFalse(report.getContexts().isEmpty(), "a report must name the context its citations came from");
	}

	@Test
	@DisplayName("what the kinds produce: a def discloses, a derive aggregates, an inv does not")
	public void theKindsBecomeStatements() throws Exception {
		ComplianceReport report = analyse(reload(write("baeder-regeln.unit.xmi", compile())));
		List<String> ids = report.getEvaluations().stream().flatMap(e -> findingsOf(e).stream()).map(Finding::getId)
				.toList();

		assertTrue(ids.stream().anyMatch(id -> id.startsWith("DEFINITION_DISCLOSURE:anzeigename")),
				() -> "a def that computes from two direct identifiers is the widest statement this "
						+ "catalogue makes, and it is missing from " + ids);
		assertTrue(ids.stream().anyMatch(id -> id.startsWith("DEFINITION_DISCLOSURE:kartenAuskunft")),
				() -> "an operation def is a definition too - the name is new either way: " + ids);
		assertTrue(ids.stream().anyMatch(id -> id.startsWith("DERIVED_AGGREGATION:")),
				() -> "two classified features reach dauerMinuten; missing from " + ids);
		assertTrue(ids.stream().anyMatch(id -> id.startsWith("BULK_READ:")),
				() -> "the document calls allInstances() over a classified type; missing from " + ids);
		assertTrue(ids.stream().noneMatch(id -> id.contains("feedbackOhnePersonenbezug")),
				() -> "an invariant over a feature that is personal but not special category is not a "
						+ "finding, and this catalogue has no rule that says otherwise: " + ids);
		assertTrue(ids.stream().noneMatch(id -> id.startsWith("OPERATION_DISCLOSURE:")),
				() -> "OPERATION_DISCLOSURE is about a body: constraint, and a body: needs an EOperation "
						+ "the metamodel declares - this one declares none, so the rule cannot fire here: " + ids);
	}

	@Test
	@DisplayName("a navigation the parser could not type is named, not passed over")
	public void anUntypedIteratorVariableIsAnError() throws Exception {
		ComplianceReport report = analyse(reload(write("baeder-regeln.unit.xmi", compile())));

		List<String> errors = report.getEvaluations().stream().map(Evaluation::getId).filter(java.util.Objects::nonNull)
				.filter(id -> id.startsWith("UNRESOLVED_NAVIGATION:")).toList();

		assertFalse(errors.isEmpty(),
				"the document filters with ->select(b | b.bereich = ...), and the parser gives the iterator "
						+ "variable no type, so b.bereich becomes a synthetic feature that joins to no review. "
						+ "An analysis that cannot see a filter's selector must say so, because silence reads "
						+ "as clean");
	}

	@Test
	@DisplayName("every finding carries a real quote, by one of the two routes")
	public void everyFindingIsEvidenced() throws Exception {
		ComplianceReport report = analyse(reload(write("baeder-regeln.unit.xmi", compile())));

		int checked = 0;
		for (Evaluation evaluation : report.getEvaluations()) {
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
		assertTrue(checked > 0, "the report raised no finding at all, so nothing was proved about evidence");
	}

	@Test
	@DisplayName("a package with no review of the recorded revision is an ERROR, not a silence")
	public void anUnreviewedRevisionSaysSo() throws Exception {
		CompiledUnit unit = reload(write("baeder-regeln.unit.xmi", compile()));
		// the review of a revision nobody compiled against: the join must miss, and say so
		ReviewIndex review = new ReviewIndex(Fixtures.baederSourceReview());
		Map<String, ReviewIndex> wrongRevision = Map.of("fp1:0000000000000000000000000000000000000000000000000000000000000000",
				review);

		IllegalStateException refused = org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
				() -> new OclDocumentAnalysis(wrongRevision, citations()).analyse(unit));

		assertTrue(refused.getMessage().contains("has a review of the recorded revision"),
				() -> "the analyser must refuse loudly rather than write an empty report: " + refused.getMessage());
	}

	/* ------------------------------------------------------------------------------- plumbing */

	/** Compiles the document against the reviewed package, exactly as the stage action would. */
	private static CompiledUnit compile() throws Exception {
		EPackage.Registry registry = new EPackageRegistryImpl();
		registry.put(Fixtures.baederSource().getNsURI(), Fixtures.baederSource());
		OclEngine engine = OclEngines.create(new OclParserSupport(registry));
		CompiledUnit unit = engine.compileDocument(UNIT_NAME, document());
		assertInstanceOf(CompleteOclDocument.class, unit.getUnit());
		return unit;
	}

	private static ComplianceReport analyse(CompiledUnit unit) throws IOException {
		ReviewIndex review = new ReviewIndex(Fixtures.baederSourceReview());
		Map<String, ReviewIndex> reviews = Map.of(FingerprintHelper.fingerprint(Fixtures.baederSource()), review);
		return new OclDocumentAnalysis(reviews, citations()).analyse(unit);
	}

	private static String document() throws IOException {
		try (InputStream in = OclDocumentReportTest.class.getResourceAsStream("data/baeder-regeln.ocl")) {
			assertNotNull(in, "fixture not on the test classpath: data/baeder-regeln.ocl");
			return new String(in.readAllBytes(), StandardCharsets.UTF_8);
		}
	}

	private static List<Finding> findingsOf(Evaluation evaluation) {
		if (evaluation instanceof FlowEvaluation flow) {
			return flow.getFindings();
		}
		if (evaluation instanceof ControlEvaluation control) {
			return control.getFindings();
		}
		return List.of();
	}

	private static RequirementCitations citations() throws IOException {
		Path file = repositoryRoot().resolve(GDPR_CONTEXT);
		assertTrue(Files.isReadable(file), () -> "seeded GDPR context not readable: " + file);
		Resource resource = resourceSet().getResource(URI.createFileURI(file.toAbsolutePath().toString()), true);
		return new RequirementCitations((ComplianceContext) resource.getContents().get(0));
	}

	private static ResourceSet resourceSet() {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		for (EPackage ePackage : List.of(EcorePackage.eINSTANCE, ReportPackage.eINSTANCE, ContextPackage.eINSTANCE,
				CorpusPackage.eINSTANCE, CompiledPackage.eINSTANCE, OclPackage.eINSTANCE)) {
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

	/** Writes a document where a person can open it. The prototype's actual deliverable. */
	private static Path write(String name, org.eclipse.emf.ecore.EObject content) throws IOException {
		Path out = Paths.get("generated", "reports");
		Files.createDirectories(out);
		Path file = out.resolve(name);
		Resource resource = resourceSet().createResource(URI.createFileURI(file.toAbsolutePath().toString()));
		resource.getContents().add(content);
		resource.save(Map.of());
		resource.getContents().clear();
		return file;
	}

	/** Reads the unit back, the way the analyser gets it out of the registry. */
	private static CompiledUnit reload(Path file) throws IOException {
		Resource resource = resourceSet().getResource(URI.createFileURI(file.toAbsolutePath().toString()), true);
		return (CompiledUnit) resource.getContents().get(0);
	}

	/** Printed, not asserted: what the report actually says, for a human to judge. */
	private static void summarise(ComplianceReport report) {
		long findings = report.getEvaluations().stream().mapToLong(e -> findingsOf(e).size()).sum();
		System.out.printf("%-34s %2d evaluations, %2d findings%n", report.getGeneratedBy(),
				report.getEvaluations().size(), findings);
		for (Evaluation evaluation : report.getEvaluations()) {
			if (findingsOf(evaluation).isEmpty()) {
				System.out.printf("    %-58s %s%n", evaluation.getId(), "(no finding)");
			}
			for (Finding finding : findingsOf(evaluation)) {
				Evidence first = finding.getEvidence().get(0);
				System.out.printf("    %-58s %-14s %s%n", finding.getId(), finding.getRelevanceLevel(),
						first.getCitationId());
			}
		}
	}
}
