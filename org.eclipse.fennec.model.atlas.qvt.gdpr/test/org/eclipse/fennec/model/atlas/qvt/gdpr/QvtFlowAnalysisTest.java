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
import java.util.ArrayList;
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
import org.eclipse.fennec.model.compliance.context.ContextFactory;
import org.eclipse.fennec.model.compliance.context.ContextRef;
import org.eclipse.fennec.model.compliance.report.ClassifierEvaluation;
import org.eclipse.fennec.model.compliance.report.CombinationFinding;
import org.eclipse.fennec.model.compliance.report.Evaluation;
import org.eclipse.fennec.model.compliance.report.Evidence;
import org.eclipse.fennec.model.compliance.report.FeatureEvaluation;
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

		Finding finding = notPropagated(report);
		assertEquals(RelevanceLevel.LOW, finding.getRelevanceLevel(),
				"it is evidence for a minimisation argument, not a risk");
		assertTrue(finding.getRationale().contains("//Patient/birthDate"), finding.getRationale());
		assertTrue(finding.getRationale().contains("//Physician/name"), finding.getRationale());
		assertFalse(finding.getRationale().contains("//Patient/fullName"),
				"a feature the transformation does read is not among them");
	}

	@Test
	@DisplayName("a combination the review raised is reassembled when every member travels")
	public void aReviewedCombinationThatTravelsIsCarriedOver() {
		ComplianceReport report = analyse();

		// The clinic reviewer raised CF-001 over id, fullName and email. The mapping writes them
		// into reference, displayName and contactEmail - three different fields, so the aggregation
		// rule cannot see it, and one class, which is what reassembles the set in a target record.
		CombinationFinding carried = combination(report, RuleCatalogue.Rule.COMBINATION_CARRIED);
		assertEquals(3, carried.getFeatures().size(), "one flow per member of the reviewed set");
		assertEquals(List.of("//Contact/reference", "//Contact/displayName", "//Contact/contactEmail"),
				carried.getFeatures().stream().map(FlowEvaluation.class::cast)
						.map(FlowEvaluation::getTargetFeature).toList(),
				"and they are the flows that land in //Contact");

		assertEquals(List.of("QUASI_IDENTIFIER_SET"), kindsOf(carried),
				"the reviewer's kind, carried over: the analyser is not in a position to assert one");
		assertEquals(List.of("DIRECT_IDENTIFIER"), ReviewIndex.categoriesOf(carried),
				"and the reviewer's category of the set");
		assertEquals(RelevanceLevel.HIGH, carried.getRelevanceLevel(), "carried over from the review");
		assertTrue(carried.getRationale().contains("//Contact"), carried.getRationale());
		assertTrue(carried.getRationale().contains("//Patient/fullName"), carried.getRationale());

		assertEquals(1, carried.getEvidence().size(), "the combination's own citation, not its members'");
		Evidence evidence = carried.getEvidence().get(0);
		assertEquals("Rec.26", evidence.getCitationId());
		assertEquals("gdpr", evidence.getContextId());
		assertEquals(Reviews.CORPUS_ID, evidence.getCorpusId());
		assertEquals("singling out", evidence.getQuote(), "the quote, byte for byte");
		assertTrue(evidence.getRelevance().startsWith("The reviewer's relevance for CF-001."),
				evidence.getRelevance());
	}

	@Test
	@DisplayName("a combination whose members do not all travel is not reassembled")
	public void aCombinationThatIsBrokenUpIsNotCarriedOver() {
		ComplianceReport clinic = Reviews.clinic();
		// birthDate is read by no mapping, so a set it belongs to is not reassembled anywhere.
		CombinationFinding reviewed = clinic.getCombinations().get(0);
		reviewed.getFeatures().add(featureOf(clinic, "//Patient/birthDate"));

		ComplianceReport report = FlowAnalysis.analyse(unit,
				Map.of(Reviews.CLINIC_FP, clinic, Reviews.CONTACTS_FP, Reviews.contacts()), RAN_AT);

		assertTrue(report.getCombinations().stream()
				.noneMatch(found -> found.getId().startsWith(RuleCatalogue.Rule.COMBINATION_CARRIED.code())),
				"a set the transformation breaks up is not a set the transformation rebuilds");
	}

	@Test
	@DisplayName("pseudonymised data that travels is still data that travels")
	public void aPseudonymIsCarriedLikeAnythingElse() {
		ComplianceReport clinic = Reviews.clinic();
		// Rec. 26 is explicit that pseudonymised data is personal data as long as the additional
		// information exists, and the reviewer said so about this field. A flow of it is a flow.
		FeatureEvaluation email = featureOf(clinic, "//Patient/email");
		email.getFindings().get(0).getCategories().clear();
		email.getFindings().get(0).getCategories().add(Reviews.categoryRef("PSEUDONYMISED"));

		ComplianceReport report = FlowAnalysis.analyse(unit,
				Map.of(Reviews.CLINIC_FP, clinic, Reviews.CONTACTS_FP, Reviews.contacts()), RAN_AT);

		FlowEvaluation flow = flow(report, "//Patient/email", "//Contact/contactEmail");
		assertEquals(RelevanceLevel.HIGH, flow.getRelevanceLevel(), "the review's relevance, as before");
		Finding propagation = finding(flow, RuleCatalogue.Rule.PROPAGATION);
		assertNotNull(propagation, "a HIGH row with no finding on it is a row that says nothing");
		assertEquals(List.of("PSEUDONYMISED"), ReviewIndex.categoriesOf(propagation),
				"and it says what the review said, not something stronger");
	}

	@Test
	@DisplayName("every unread feature is named by a citation of its own, not only the first of them")
	public void whatIsNotPropagatedKeepsEveryReviewersWords() {
		ComplianceReport report = analyse();

		Finding finding = notPropagated(report);
		List<String> relevances = finding.getEvidence().stream().map(Evidence::getRelevance).toList();
		// //Physician/name and //Physician/email cite Art.4(1) with the same quote. Dropping the
		// second as a duplicate loses the one thing a reader needs: that it was reviewed at all.
		assertTrue(relevances.stream().anyMatch(text -> text.contains("//Physician/name")),
				"the first feature behind the citation: " + relevances);
		assertTrue(relevances.stream().anyMatch(text -> text.contains("//Physician/email")),
				"and the second one: " + relevances);
	}

	@Test
	@DisplayName("every category names the context it was claimed against")
	public void everyCategoryNamesItsContext() {
		ComplianceReport report = analyse();

		// CategoryRef extends ContextRef, whose contextId is mandatory. A category minted from the
		// surviving id alone had neither the context nor its version, so every finding this
		// analyser wrote was invalid against the model it was written in.
		List<CategoryRef> refs = new ArrayList<>();
		report.getEvaluations().stream().flatMap(evaluation -> evaluation.getFindings().stream())
				.forEach(finding -> refs.addAll(finding.getCategories()));
		report.getCombinations().forEach(combination -> refs.addAll(combination.getCategories()));
		assertFalse(refs.isEmpty());
		for (CategoryRef ref : refs) {
			assertEquals("gdpr", ref.getContextId(), ref.getTaxonomyId() + "/" + ref.getCategoryId());
			assertEquals(Reviews.CONTEXT_VERSION, ref.getContextVersion(),
					ref.getTaxonomyId() + "/" + ref.getCategoryId());
		}
	}

	@Test
	@DisplayName("carrying a category over does not take it out of the review")
	public void theReviewKeepsItsOwnCategories() {
		ComplianceReport clinic = Reviews.clinic();
		FeatureEvaluation diagnosis = featureOf(clinic, "//Patient/diagnosis");

		FlowAnalysis.analyse(unit, Map.of(Reviews.CLINIC_FP, clinic, Reviews.CONTACTS_FP, Reviews.contacts()),
				RAN_AT);

		// Finding.categories is a containment reference: recording the review's own instance would
		// move it, and the review this analysis rests on would come out of it short of a category.
		assertEquals(List.of("SPECIAL_CATEGORY"), ReviewIndex.categoriesOf(diagnosis.getFindings().get(0)),
				"the review still asserts what it asserted before it was read");
		assertEquals(List.of("QUASI_IDENTIFIER_SET", "DIRECT_IDENTIFIER"),
				clinic.getCombinations().get(0).getCategories().stream().map(CategoryRef::getCategoryId).toList(),
				"and so does the combination the carried-over one was copied from");
	}

	@Test
	@DisplayName("the kind of a combination is stated against the context of the source reviews")
	public void theCombinationKindTakesTheSourceContext() {
		ComplianceReport report = analyse();

		CombinationFinding aggregation = combination(report, RuleCatalogue.Rule.AGGREGATION);
		CategoryRef kind = aggregation.getCategories().stream()
				.filter(ref -> "combination-kinds".equals(ref.getTaxonomyId())).findFirst().orElseThrow();
		// LINKAGE is the analyser's own statement, so there is no reviewer's reference to copy. It
		// belongs to the source side all the same: everything behind the combination - which fields
		// are classified, as what, on what citation - was read from the source review.
		assertEquals("gdpr", kind.getContextId());
		assertEquals(Reviews.CONTEXT_VERSION, kind.getContextVersion());
	}

	@Test
	@DisplayName("a kind nobody can place in one context is left off rather than guessed")
	public void anAmbiguousContextLeavesTheKindOff() {
		ComplianceReport clinic = Reviews.clinic();
		ContextRef second = ContextFactory.eINSTANCE.createContextRef();
		second.setContextId("bdsg");
		second.setContextVersion("20190625");
		clinic.getContexts().add(second);

		ComplianceReport report = FlowAnalysis.analyse(unit,
				Map.of(Reviews.CLINIC_FP, clinic, Reviews.CONTACTS_FP, Reviews.contacts()), RAN_AT);

		CombinationFinding aggregation = combination(report, RuleCatalogue.Rule.AGGREGATION);
		assertTrue(kindsOf(aggregation).isEmpty(),
				"which of the two contexts the kind belongs to is not derivable from a dataflow");
		assertEquals(List.of("SPECIAL_CATEGORY"), ReviewIndex.categoriesOf(aggregation),
				"the data category still stands: it came from a review and carries its own context");
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
		// A citation is only checkable if a reader can find what was cited. One context may hold
		// several corpora, so naming the context without the corpus does not locate the provision.
		assertEquals("gdpr", evidence.getContextId(), "the context whose corpus was quoted");
		assertEquals(Reviews.CORPUS_ID, evidence.getCorpusId(), "and which corpus of it");
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

	/** The one aggregated finding about everything no mapping reads. */
	private static Finding notPropagated(ComplianceReport report) {
		return report.getEvaluations().stream().flatMap(evaluation -> evaluation.getFindings().stream())
				.filter(found -> found.getId().startsWith(RuleCatalogue.Rule.NOT_PROPAGATED.code())).findFirst()
				.orElseThrow(() -> new AssertionError("no not-propagated finding in the report"));
	}

	/** One feature evaluation of a review, by the fragment it is indexed under. */
	private static FeatureEvaluation featureOf(ComplianceReport review, String uriFragment) {
		return review.getEvaluations().stream().filter(ClassifierEvaluation.class::isInstance)
				.map(ClassifierEvaluation.class::cast)
				.flatMap(classifier -> classifier.getFeatureEvaluations().stream())
				.filter(feature -> uriFragment.equals(feature.getUriFragment())).findFirst()
				.orElseThrow(() -> new AssertionError("no " + uriFragment + " in the review"));
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
