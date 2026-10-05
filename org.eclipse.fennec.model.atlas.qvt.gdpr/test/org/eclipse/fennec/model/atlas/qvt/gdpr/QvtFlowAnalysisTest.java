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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.InternalEObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.m2x.model.compiled.CompiledPackage;
import org.eclipse.fennec.m2x.model.compiled.CompiledUnit;
import org.eclipse.fennec.m2x.model.imperativeocl.ImperativeOclPackage;
import org.eclipse.fennec.m2x.model.ocl.OclPackage;
import org.eclipse.fennec.m2x.model.qvtoperational.QvtOperationalPackage;
import org.eclipse.fennec.model.compliance.context.CategoryRef;
import org.eclipse.fennec.model.compliance.report.CombinationFinding;
import org.eclipse.fennec.model.compliance.report.Evaluation;
import org.eclipse.fennec.model.compliance.report.Evaluation;
import org.eclipse.fennec.model.compliance.report.Evidence;
import org.eclipse.fennec.model.compliance.report.Finding;
import org.eclipse.fennec.model.compliance.report.FlowEvaluation;
import org.eclipse.fennec.model.compliance.report.FlowKind;
import org.eclipse.fennec.model.compliance.report.ReportPackage;
import org.eclipse.fennec.model.compliance.report.ComplianceReport;
import org.eclipse.fennec.model.compliance.report.ReportOrigin;
import org.eclipse.fennec.model.compliance.report.PackageSubject;
import org.eclipse.fennec.model.compliance.report.RelevanceLevel;
import org.eclipse.fennec.model.compliance.report.TransformationSubject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The analyser, against the transformation the probe of 2026-09-23 ran end to end.
 * <p>
 * {@code clinic2contacts-unit.xmi} is the compiled unit exactly as the Atlas stored it that day -
 * not a fixture written to suit the analyser. It is the regression suite this work starts with
 * rather than earns, and it is the only thing that pins down the AST shapes the walk depends on:
 * the satellite variable a property assignment is compiled into, the nested {@code +} calls of a
 * concatenation, and the model parameters that say which metamodel is read and which is written.
 * <p>
 * The transformation, in full:
 *
 * <pre>
 * mapping Patient::toContact() : Contact {
 *     reference    := self.id;
 *     displayName  := self.fullName;
 *     contactEmail := self.email;
 *     comment      := 'Patient ' + self.fullName + ' (' + self.postcode + '), diagnosis: '
 *                   + self.diagnosis;
 * }
 * </pre>
 *
 * The last line is the case the whole work package exists for: three fields the clinic review
 * classified, folded into one free-text field that the contacts review calls an ordinary string.
 */
@DisplayName("A GDPR report derived from a compiled transformation")
public class QvtFlowAnalysisTest {

	private static final String CONTACTS_COMMENT = "//Contact/comment";
	private static final Instant RAN_AT = Instant.parse("2026-09-30T09:00:00Z");

	private static CompiledUnit unit;

	@BeforeAll
	static void loadTheCompiledUnit() throws Exception {
		unit = readUnit();
	}

	@Test
	@DisplayName("the subject is the transformation, pinned to the revisions it was compiled against")
	public void theSubjectNamesTheUnitAndItsMetamodels() {
		ComplianceReport report = analyse();

		assertEquals(ReportOrigin.STATIC_ANALYSIS, report.getOrigin(), "nothing here was reviewed by anybody");
		assertEquals(RuleCatalogue.VERSION, report.getGeneratedBy(), "the rule table is what wrote it");
		assertNotNull(report.getDisclaimer());

		TransformationSubject subject = (TransformationSubject) report.getSubject();
		assertEquals("clinic2contacts", subject.getQualifiedName());
		assertEquals("qvto", subject.getLanguage());
		assertEquals("m2x1:175c17b6cfc57e09e0881d32283049c357b250b400f8561253c5b5610fe18cc8",
				subject.getSubjectFingerprint(), "the subject of a transformation report is the compiled unit");

		assertEquals(List.of(Reviews.CLINIC_NS), subject.getSourcePackages().stream().map(PackageSubject::getNsURI)
				.toList(), "the model parameter declares clinic 'in'");
		assertEquals(List.of(Reviews.CONTACTS_NS), subject.getTargetPackages().stream().map(PackageSubject::getNsURI)
				.toList(), "and contacts 'out'");
		assertEquals(Reviews.CLINIC_FP, subject.getSourcePackages().get(0).getSubjectFingerprint(),
				"the manifest pins the exact revision, which is what the review is joined on");
		assertEquals("gdpr-fp1-5b87b0c6-20260923-human", subject.getSourcePackages().get(0).getReportId(),
				"and the entry points at the review whose findings this report carries over");

		// Carried over, never chosen: every citation in the report came out of those reviews
		assertEquals(1, report.getContexts().size(), "both reviews cite the one context, named once");
		assertEquals("gdpr", report.getContexts().get(0).getContextId());
		assertEquals("20160504", report.getContexts().get(0).getContextVersion());
		assertEquals("EN", report.getLanguage(), "and the prose is in the language the reviews were written in");
	}

	@Test
	@DisplayName("every assignment of the mapping becomes a flow, with the shape of the expression")
	public void theFlowsAreReadOffTheCompiledUnit() {
		ComplianceReport report = analyse();

		List<FlowEvaluation> flows = flows(report);
		assertEquals(6, flows.size(), "three direct assignments plus the three features of the concatenation");
		assertTrue(flows.stream().allMatch(flow -> "toContact".equals(flow.getMapping())));

		FlowEvaluation direct = flow(report, "//Patient/id", "//Contact/reference");
		assertEquals(FlowKind.DIRECT, direct.getFlowKind(), "a bare property call is a direct copy");
		assertEquals(Reviews.CLINIC_NS, direct.getSourceNsURI());
		assertEquals(Reviews.CONTACTS_NS, direct.getTargetNsURI());
		assertEquals(RelevanceLevel.HIGH, direct.getRelevanceLevel(), "carried over from the source review");

		for (String source : List.of("//Patient/fullName", "//Patient/postcode", "//Patient/diagnosis")) {
			assertEquals(FlowKind.CONCATENATION, flow(report, source, CONTACTS_COMMENT).getFlowKind(),
					source + " is concatenated into comment, and how it travels is part of the assessment");
		}
	}

	@Test
	@DisplayName("three classified fields folded into one free-text field: the case this exists for")
	public void theConcatenationIntoFreeTextIsFound() {
		ComplianceReport report = analyse();

		CombinationFinding aggregation = combination(report, RuleCatalogue.Rule.AGGREGATION);
		assertEquals(3, aggregation.getFeatures().size(), "fullName, postcode and diagnosis meet in comment");
		assertEquals(List.of("SPECIAL_CATEGORY"), ReviewIndex.categoriesOf(aggregation),
				"what arrives is the strongest of what was combined");
		assertEquals(List.of("LINKAGE"), kindsOf(aggregation), "and the fields are joinable through it");
		assertTrue(aggregation.getRationale().contains("//Patient/diagnosis"), aggregation.getRationale());
		assertTrue(aggregation.getRationale().contains("per field"), aggregation.getRationale());

		CombinationFinding structureLoss = combination(report, RuleCatalogue.Rule.STRUCTURE_LOSS);
		assertEquals(3, structureLoss.getFeatures().size());
		assertTrue(structureLoss.getRationale().contains("free text"), structureLoss.getRationale());

		// The target review calls comment an ordinary PERSONAL_DATA string; what lands in it is
		// health data. Two reviews describing one field differently is a finding, not an error.
		CombinationFinding disagreement = combination(report, RuleCatalogue.Rule.TARGET_DISAGREEMENT);
		assertTrue(disagreement.getRationale().contains("PERSONAL_DATA"), disagreement.getRationale());
		assertTrue(disagreement.getRationale().contains("SPECIAL_CATEGORY"), disagreement.getRationale());
	}

	@Test
	@DisplayName("a weaker target classification is a disagreement, not only NOT_PERSONAL_DATA")
	public void aDowngradeIsFoundToo() {
		ComplianceReport report = analyse();

		// //Patient/id is a DIRECT_IDENTIFIER and lands in //Contact/reference, which the target
		// review calls PERSONAL_DATA. The probe missed this: its rule fired on NOT_PERSONAL_DATA
		// alone, so a downgrade between two personal categories went unseen.
		Finding downgrade = finding(flow(report, "//Patient/id", "//Contact/reference"),
				RuleCatalogue.Rule.TARGET_DISAGREEMENT);
		assertNotNull(downgrade, "a weaker target category is a disagreement whatever the two categories are");
		assertEquals(List.of("DIRECT_IDENTIFIER"), ReviewIndex.categoriesOf(downgrade));
	}

	@Test
	@DisplayName("a purpose stated at the source does not travel with the data")
	public void aStatedPurposeIsNotCarried() {
		ComplianceReport report = analyse();

		Finding purpose = finding(flow(report, "//Patient/diagnosis", CONTACTS_COMMENT),
				RuleCatalogue.Rule.PURPOSE_NOT_CARRIED);
		assertNotNull(purpose, "the person who reviewed the clinic model stated a purpose for diagnosis");
		assertTrue(purpose.getRationale().contains(Reviews.DIAGNOSIS_PURPOSE), purpose.getRationale());

		assertNull(finding(flow(report, "//Patient/fullName", CONTACTS_COMMENT), RuleCatalogue.Rule.PURPOSE_NOT_CARRIED),
				"a feature whose review states no purpose has nothing not to carry");
	}

	@Test
	@DisplayName("classified fields no mapping reads are reported once, not one finding each")
	public void whatIsNotPropagatedIsAggregated() {
		ComplianceReport report = analyse();

		List<Finding> notPropagated = report.getEvaluations().stream()
				.flatMap(evaluation -> evaluation.getFindings().stream())
				.filter(found -> found.getId().startsWith(RuleCatalogue.Rule.NOT_PROPAGATED.code())).toList();
		assertEquals(1, notPropagated.size(),
				"one finding about the set; one per feature buries a real metamodel under INFO rows");

		Finding finding = notPropagated.get(0);
		assertEquals(RelevanceLevel.LOW, finding.getRelevanceLevel(),
				"it is evidence for a minimisation argument, not a risk");
		assertTrue(finding.getRationale().contains("//Patient/birthDate"), finding.getRationale());
		assertTrue(finding.getRationale().contains("//Physician/name"), finding.getRationale());
		assertFalse(finding.getRationale().contains("//Patient/fullName"),
				"a feature the transformation does read is not among them");
	}

	@Test
	@DisplayName("citations are carried over verbatim, with one clause added about the flow")
	public void evidenceIsInheritedAndNeverComposed() {
		ComplianceReport report = analyse();

		Finding propagation = finding(flow(report, "//Patient/diagnosis", CONTACTS_COMMENT),
				RuleCatalogue.Rule.PROPAGATION);
		assertEquals(1, propagation.getEvidence().size());
		Evidence evidence = propagation.getEvidence().get(0);
		assertEquals("Art.9(1)", evidence.getCitationId(), "the identifier the reviewer checked, unchanged");
		assertEquals("data concerning health", evidence.getQuote(), "and the quote, byte for byte");
		assertEquals("celex:02016R0679-20160504#Art.9(1)", evidence.getSourceRef());
		assertTrue(evidence.isVerbatim());
		assertTrue(evidence.getRelevance().startsWith("The reviewer's relevance for //Patient/diagnosis."),
				"what the reviewer wrote comes first: " + evidence.getRelevance());
		assertTrue(evidence.getRelevance().contains("writes into " + CONTACTS_COMMENT),
				"and the analyser adds only how the data travels: " + evidence.getRelevance());
	}

	@Test
	@DisplayName("an unreviewed metamodel leaves the entry's reportId unset and raises no finding")
	public void anUnreviewedSourceIsSaidInTheSubject() {
		ComplianceReport report = FlowAnalysis.analyse(unit, Map.of(), RAN_AT);

		TransformationSubject subject = (TransformationSubject) report.getSubject();
		assertNull(subject.getSourcePackages().get(0).getReportId(),
				"reportId unset IS the statement that the analysis of flows through it is incomplete");
		assertEquals(Reviews.CLINIC_FP, subject.getSourcePackages().get(0).getSubjectFingerprint(),
				"the package is still listed, with the revision the analysis would have needed a review of");

		assertEquals(6, flows(report).size(), "the dataflow is fact and is recorded whether or not anyone reviewed");
		assertTrue(report.getEvaluations().stream().allMatch(evaluation -> evaluation.getFindings().isEmpty()),
				"but nothing classifies what travels, so there is nothing to find and nothing is claimed");
		assertTrue(report.getCombinations().isEmpty());
	}

	@Test
	@DisplayName("the same unit and the same reviews produce the same report")
	public void theAnalysisIsDeterministic() {
		ComplianceReport first = analyse();
		ComplianceReport second = FlowAnalysis.analyse(unit, reviews(), RAN_AT.plusSeconds(86_400));

		assertEquals(first.getReportId(), second.getReportId(),
				"a re-run on unchanged inputs is not a new judgement, so the replay adds no revision");
		assertEquals(rationales(first), rationales(second), "and the text is written from templates, not composed");

		// A corrected source review is a changed input, and the report that rests on it is another
		// revision rather than an overwrite of the one before.
		ComplianceReport corrected = Reviews.clinic();
		corrected.setReportId("gdpr-fp1-5b87b0c6-20260924-human");
		ComplianceReport afterCorrection = FlowAnalysis.analyse(unit,
				Map.of(Reviews.CLINIC_FP, corrected, Reviews.CONTACTS_FP, Reviews.contacts()), RAN_AT);
		assertFalse(first.getReportId().equals(afterCorrection.getReportId()),
				"otherwise the correction would silently overwrite the analysis that preceded it");
	}

	@Test
	@DisplayName("the report survives being written and read back")
	public void theReportCanBeStoredAndLoadedAgain() throws Exception {
		ComplianceReport report = analyse();
		assertFalse(report.getCombinations().isEmpty(), "the case that breaks only exists with a combination");

		ResourceSet set = new ResourceSetImpl();
		set.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		set.getPackageRegistry().put(ReportPackage.eNS_URI, ReportPackage.eINSTANCE);
		Resource written = set.createResource(URI.createURI("report.xmi"));
		written.getContents().add(report);
		ByteArrayOutputStream bytes = new ByteArrayOutputStream();
		written.save(bytes, Map.of());

		// Writing it is not the hard half. CombinationFinding.features is the model's one
		// non-containment reference, and Evaluation.id is an EMF ID attribute - so a combination
		// is serialised as a space-separated list of evaluation ids. An id holding a '#' comes
		// back as a cross-document href, and the loader then tries to instantiate the declared
		// type to proxy it: Evaluation, which is abstract. The report would store happily and be
		// unreadable from then on, which is how a live Atlas found this and no test did.
		Resource read = new ResourceSetImpl() {
			{
				getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
				getPackageRegistry().put(ReportPackage.eNS_URI, ReportPackage.eINSTANCE);
			}
		}.createResource(URI.createURI("report.xmi"));
		read.load(new ByteArrayInputStream(bytes.toByteArray()), Map.of());

		ComplianceReport loaded = (ComplianceReport) read.getContents().get(0);
		assertEquals(report.getReportId(), loaded.getReportId());
		assertEquals(report.getCombinations().size(), loaded.getCombinations().size());
		for (CombinationFinding combination : loaded.getCombinations()) {
			assertFalse(combination.getFeatures().isEmpty(),
					"a combination that lost its flows says nothing about anything");
			for (Evaluation feature : combination.getFeatures()) {
				assertFalse(feature.eIsProxy(),
						"an unresolved flow reference: " + ((InternalEObject) feature).eProxyURI());
			}
		}
	}

	/* ------------------------------------------------------------------ helpers */

	private static ComplianceReport analyse() {
		return FlowAnalysis.analyse(unit, reviews(), RAN_AT);
	}

	private static Map<String, ComplianceReport> reviews() {
		return Map.of(Reviews.CLINIC_FP, Reviews.clinic(), Reviews.CONTACTS_FP, Reviews.contacts());
	}

	private static List<String> rationales(ComplianceReport report) {
		return report.getEvaluations().stream().flatMap(evaluation -> evaluation.getFindings().stream())
				.map(Finding::getRationale).sorted().toList();
	}

	private static List<FlowEvaluation> flows(ComplianceReport report) {
		return report.getEvaluations().stream().filter(FlowEvaluation.class::isInstance)
				.map(FlowEvaluation.class::cast).filter(flow -> flow.getTargetFeature() != null).toList();
	}

	private static FlowEvaluation flow(ComplianceReport report, String source, String target) {
		return flows(report).stream()
				.filter(flow -> source.equals(flow.getSourceFeature()) && target.equals(flow.getTargetFeature()))
				.findFirst().orElseThrow(() -> new AssertionError("no flow " + source + " -> " + target));
	}

	private static Finding finding(Evaluation evaluation, RuleCatalogue.Rule rule) {
		return evaluation.getFindings().stream().filter(found -> found.getId().startsWith(rule.code())).findFirst()
				.orElse(null);
	}

	private static CombinationFinding combination(ComplianceReport report, RuleCatalogue.Rule rule) {
		Optional<CombinationFinding> found = report.getCombinations().stream()
				.filter(combination -> combination.getId().startsWith(rule.code())).findFirst();
		return found.orElseThrow(() -> new AssertionError("no " + rule + " combination in the report"));
	}

	/** The compiled unit the probe stored, read the way any consumer of the registry reads one. */
	static CompiledUnit readUnit() throws Exception {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		for (EPackage ePackage : List.of(EcorePackage.eINSTANCE, CompiledPackage.eINSTANCE, OclPackage.eINSTANCE,
				ImperativeOclPackage.eINSTANCE, QvtOperationalPackage.eINSTANCE)) {
			resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
		}
		Resource resource = resourceSet.createResource(URI.createURI("clinic2contacts-unit.xmi"));
		try (InputStream fixture = QvtFlowAnalysisTest.class.getResourceAsStream("clinic2contacts-unit.xmi")) {
			assertNotNull(fixture, "the compiled unit fixture has to be on the test classpath");
			resource.load(fixture, Map.of());
		}
		return (CompiledUnit) resource.getContents().get(0);
	}

	/** The combination-kind ids a finding carries, told from its data categories by taxonomy. */
	private static List<String> kindsOf(Finding finding) {
		return finding.getCategories().stream().filter(ref -> "combination-kinds".equals(ref.getTaxonomyId()))
				.map(CategoryRef::getCategoryId).toList();
	}

}
