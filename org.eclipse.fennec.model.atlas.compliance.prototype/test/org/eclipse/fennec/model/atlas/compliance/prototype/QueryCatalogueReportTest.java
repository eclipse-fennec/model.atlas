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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.emf.osgi.fingerprint.util.FingerprintHelper;
import org.eclipse.fennec.model.atlas.compliance.analysis.RequirementCitations;
import org.eclipse.fennec.model.atlas.compliance.analysis.ReviewIndex;
import org.eclipse.fennec.model.command.CommandPackage;
import org.eclipse.fennec.model.compliance.context.ComplianceContext;
import org.eclipse.fennec.model.compliance.context.ContextPackage;
import org.eclipse.fennec.model.compliance.corpus.CorpusPackage;
import org.eclipse.fennec.model.compliance.report.ComplianceReport;
import org.eclipse.fennec.model.compliance.report.ControlEvaluation;
import org.eclipse.fennec.model.compliance.report.ControlResult;
import org.eclipse.fennec.model.compliance.report.Evaluation;
import org.eclipse.fennec.model.compliance.report.Evidence;
import org.eclipse.fennec.model.compliance.report.Finding;
import org.eclipse.fennec.model.compliance.report.FlowEvaluation;
import org.eclipse.fennec.model.compliance.report.ReportPackage;
import org.eclipse.fennec.model.compliance.report.TransformationSubject;
import org.eclipse.fennec.model.expression.ExpressionPackage;
import org.eclipse.fennec.model.query.QueryPackage;
import org.eclipse.fennec.model.stream.StreamPackage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The fourth artefact: the saved queries and commands of one registry, reported on from the same
 * review of the same metamodel as the other three.
 *
 * <p>
 * <b>Seven reports, not one.</b> Six of them are about a stored operation and name it as their
 * subject; the seventh is about the registry and says so, because {@code ERASURE_PATH_ABSENT} -
 * <em>no entry here erases Feedback</em> - is a statement about a set that no single artefact can
 * carry. There is no "catalogue" object to report on: {@code NamedOperations} stores one EObject
 * per name, so the entries are separate documents and nothing holds them together.
 * </p>
 *
 * <p>
 * It is the cheapest of the four to extract, because the walk is not written here at all -
 * {@code FeatureUsageAnalyzer}, shipped in {@code org.eclipse.fennec.query.model} for backend
 * reasons, already reports which features a query touches and in which role. What this test
 * proves is that the result joins to a real review: the segments of a stored query resolve to the
 * very {@code EStructuralFeature}s the review classified, and an update template's integer
 * feature ids resolve through its selector's root type.
 * </p>
 */
@DisplayName("The saved queries and commands of a registry get their reports")
public class QueryCatalogueReportTest {

	private static final String GDPR_CONTEXT = "docker/dockercompose/initial-models/scopes/dimcity/"
			+ "compliance-context/gdpr.xmi";

	/** The registry these entries belong to, which only the coverage report is about. */
	private static final String REGISTRY = "baeder";

	/** The entries, in the order a reader should meet them: the queries, then the commands. */
	private static final List<String> ENTRIES = List.of("data/baeder-besuchersuche.query.xmi",
			"data/baeder-feedbackexport.query.xmi", "data/baeder-saunaauslastung.query.xmi",
			"data/baeder-checkinverlauf.query.xmi", "data/baeder-besuchloeschen.command.xmi",
			"data/baeder-feedbackanonymisieren.command.xmi");

	/** The reports of one run: one per stored operation, plus the registry's coverage report. */
	private record Reports(List<ComplianceReport> perOperation, ComplianceReport coverage) {

		List<ComplianceReport> all() {
			List<ComplianceReport> all = new ArrayList<>(perOperation);
			all.add(coverage);
			return all;
		}

		ComplianceReport of(String operation) {
			return perOperation.stream()
					.filter(report -> operation.equals(((TransformationSubject) report.getSubject())
							.getQualifiedName()))
					.findFirst()
					.orElseThrow(() -> new AssertionError("no report for the stored operation " + operation));
		}
	}

	@Test
	@DisplayName("each stored operation gets a report whose subject names it, and the registry gets one more")
	public void everyEntryGetsItsOwnReport() throws IOException {
		String fingerprint = FingerprintHelper.fingerprint(Fixtures.baederSource());
		Reports reports = reports();

		for (ComplianceReport report : reports.perOperation()) {
			String name = ((TransformationSubject) report.getSubject()).getQualifiedName();
			write("query-analysis-" + name + ".xmi", report);
		}
		write("query-analysis-" + REGISTRY + "-coverage.xmi", reports.coverage());
		reports.all().forEach(QueryCatalogueReportTest::summarise);

		assertEquals(ENTRIES.size(), reports.perOperation().size(), "one report per stored operation");
		for (ComplianceReport report : reports.all()) {
			TransformationSubject subject = (TransformationSubject) report.getSubject();
			assertFalse(subject.getQualifiedName() == null || subject.getQualifiedName().isBlank(),
					() -> report.getReportId() + " names no subject at all");
			assertEquals(fingerprint, subject.getSourcePackages().get(0).getSubjectFingerprint(),
					"every report pins the revision it was derived against");
			assertFalse(report.getContexts().isEmpty(), "a report must name the context its citations came from");
		}
		assertEquals(REGISTRY, ((TransformationSubject) reports.coverage().getSubject()).getQualifiedName(),
				"the coverage report is the only one whose subject is a registry rather than an artefact");
	}

	@Test
	@DisplayName("every stored operation resolves against the reviewed package")
	public void nothingIsOpaque() throws IOException {
		List<String> opaque = reports().all().stream().flatMap(report -> report.getEvaluations().stream())
				.map(Evaluation::getId).filter(id -> id != null && id.startsWith("OPAQUE_REFERENCE")).toList();

		assertTrue(opaque.isEmpty(),
				() -> "a stored query is XMI with nsURI hrefs, so an unresolved reference turns every rule "
						+ "into silence rather than into a finding: " + opaque);
	}

	@Test
	@DisplayName("every finding carries a real quote, by one of the two routes")
	public void everyFindingIsEvidenced() throws IOException {
		int checked = 0;
		for (ComplianceReport report : reports().all()) {
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
		}
		assertTrue(checked > 0, "no report raised a finding at all, so nothing was proved about evidence");
	}

	@Test
	@DisplayName("a check that passed, one that failed and one that could not run are all written down")
	public void allThreeOutcomesAreRecorded() throws IOException {
		Reports reports = reports();

		List<ControlEvaluation> coverage = controlsOf(reports.coverage());
		assertTrue(coverage.stream().anyMatch(c -> c.getResult() == ControlResult.SATISFIED),
				"the registry holds a delete command for KarteninhaberBesuch, and a control that passed has "
						+ "to be recorded as explicitly as one that failed - silence and a pass must never "
						+ "look the same");
		assertTrue(coverage.stream().anyMatch(c -> c.getResult() == ControlResult.NOT_SATISFIED),
				"no entry of this registry erases Checkin, SaunaBesuch or Feedback");

		assertTrue(controlsOf(reports.of("feedback-anonymisieren")).stream()
				.anyMatch(c -> c.getResult() == ControlResult.NOT_ASSESSED),
				"whether the anonymisation update is a measure needs an inventory no runtime here holds; a "
						+ "check that could not run must be NOT_ASSESSED, never absent and never SATISFIED");
	}

	@Test
	@DisplayName("the containment closure delivers what the query never names")
	public void unnamedDeliveryReachesTheContainedLocation() throws IOException {
		List<String> delivered = reports().of("checkin-verlauf").getEvaluations().stream()
				.filter(e -> "UNNAMED_DELIVERY:checkin-verlauf".equals(e.getId()))
				.flatMap(e -> findingsOf(e).stream()).map(Finding::getId).toList();

		assertTrue(delivered.stream().anyMatch(id -> id.endsWith("//CheckinStandort/lat")),
				() -> "checkin-verlauf names neither lat nor lon, and both arrive with the Checkin objects it "
						+ "delivers; this is the statement no other check in the stack makes: " + delivered);
	}

	@Test
	@DisplayName("the erasure command's own report is empty, and that is the point")
	public void anErasureCommandSaysNothingAboutItself() throws IOException {
		ComplianceReport report = reports().of("besuch-loeschen");

		assertTrue(report.getEvaluations().isEmpty(),
				() -> "besuch-loeschen addresses its subject by a parameter and hands nothing to a caller, so "
						+ "no rule has anything to say about it; its contribution to the registry is the "
						+ "satisfied erasure control in the coverage report, not a finding of its own: "
						+ report.getEvaluations().stream().map(Evaluation::getId).toList());
		assertFalse(report.getDisclaimer() == null || report.getDisclaimer().isBlank(),
				"an empty report still has to carry the caveat that a rule nobody wrote down never fires");
	}

	/* ------------------------------------------------------------------------------- plumbing */

	private static Reports reports() throws IOException {
		EPackage source = Fixtures.baederSource();
		CatalogAnalysis analysis = new CatalogAnalysis(new ReviewIndex(Fixtures.baederSourceReview()),
				new RequirementCitations(gdprContext()), source, FingerprintHelper.fingerprint(source));

		List<EObject> operations = entries();
		List<ComplianceReport> perOperation = new ArrayList<>();
		for (EObject operation : operations) {
			perOperation.add(analysis.analyse(operation));
		}
		return new Reports(perOperation, analysis.coverage(REGISTRY, operations));
	}

	/** The stored operations, loaded as the registry holds them: one XMI document each. */
	private static List<EObject> entries() throws IOException {
		ResourceSet resourceSet = resourceSet();
		List<EObject> operations = new ArrayList<>();
		for (String path : ENTRIES) {
			Resource resource = resourceSet.createResource(URI.createURI(path));
			try (InputStream in = QueryCatalogueReportTest.class.getResourceAsStream(path)) {
				assertTrue(in != null, () -> "fixture not on the test classpath: " + path);
				resource.load(in, Map.of());
			}
			operations.add(resource.getContents().get(0));
		}
		// The operations reference the reviewed package by nsURI, so it has to be the very EPackage
		// the review is about - not a second copy with the same nsURI and a different identity.
		EcoreUtil.resolveAll(resourceSet);
		return operations;
	}

	private static List<ControlEvaluation> controlsOf(ComplianceReport report) {
		return report.getEvaluations().stream().filter(ControlEvaluation.class::isInstance)
				.map(ControlEvaluation.class::cast).toList();
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

	/** The seeded GDPR context, 1.3 MB of consolidated Regulation, for the dereferenced route. */
	private static ComplianceContext gdprContext() throws IOException {
		Path file = repositoryRoot().resolve(GDPR_CONTEXT);
		assertTrue(Files.isReadable(file), () -> "seeded GDPR context not readable: " + file);
		Resource resource = resourceSet().getResource(URI.createFileURI(file.toAbsolutePath().toString()), true);
		return (ComplianceContext) resource.getContents().get(0);
	}

	private static ResourceSet resourceSet() {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		for (EPackage ePackage : List.of(EcorePackage.eINSTANCE, ReportPackage.eINSTANCE, ContextPackage.eINSTANCE,
				CorpusPackage.eINSTANCE, QueryPackage.eINSTANCE, ExpressionPackage.eINSTANCE,
				CommandPackage.eINSTANCE, StreamPackage.eINSTANCE)) {
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

	/** Printed, not asserted: what each report actually says, for a human to judge. */
	private static void summarise(ComplianceReport report) {
		long findings = report.getEvaluations().stream().mapToLong(e -> findingsOf(e).size()).sum();
		System.out.printf("%-26s %2d evaluations, %2d findings%n",
				((TransformationSubject) report.getSubject()).getQualifiedName(), report.getEvaluations().size(),
				findings);
		for (Evaluation evaluation : report.getEvaluations()) {
			if (evaluation instanceof ControlEvaluation control && control.getFindings().isEmpty()) {
				System.out.printf("    %-58s %-14s %s%n", control.getId(), control.getResult(),
						control.getRequirement() == null ? "" : control.getRequirement().getRequirementId());
			}
			for (Finding finding : findingsOf(evaluation)) {
				Evidence first = finding.getEvidence().get(0);
				System.out.printf("    %-58s %-14s %s%n", finding.getId(), finding.getRelevanceLevel(),
						first.getCitationId());
			}
		}
	}
}
