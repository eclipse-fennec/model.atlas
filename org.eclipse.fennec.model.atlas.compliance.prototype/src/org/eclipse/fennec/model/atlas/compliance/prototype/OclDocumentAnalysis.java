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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.fennec.m2x.model.compiled.CompiledUnit;
import org.eclipse.fennec.m2x.model.compiled.CompiledUnitManifest;
import org.eclipse.fennec.m2x.model.compiled.DependencyEntry;
import org.eclipse.fennec.m2x.model.compiled.PackageEntry;
import org.eclipse.fennec.m2x.model.ocl.CompleteOclDocument;
import org.eclipse.fennec.m2x.model.ocl.Constraint;
import org.eclipse.fennec.m2x.model.ocl.IteratorExp;
import org.eclipse.fennec.m2x.model.ocl.LoopExp;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.m2x.model.ocl.OperationCallExp;
import org.eclipse.fennec.m2x.model.ocl.StringLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.TypeExp;
import org.eclipse.fennec.model.atlas.compliance.analysis.RequirementCitations;
import org.eclipse.fennec.model.atlas.compliance.analysis.ReviewIndex;
import org.eclipse.fennec.model.compliance.report.ComplianceReport;
import org.eclipse.fennec.model.compliance.report.ControlResult;
import org.eclipse.fennec.model.compliance.report.FeatureEvaluation;
import org.eclipse.fennec.model.compliance.report.Finding;
import org.eclipse.fennec.model.compliance.report.FlowEvaluation;
import org.eclipse.fennec.model.compliance.report.PackageSubject;
import org.eclipse.fennec.model.compliance.report.RelevanceLevel;
import org.eclipse.fennec.model.compliance.report.ReportFactory;
import org.eclipse.fennec.model.compliance.report.TransformationSubject;

/**
 * A GDPR report for a <em>compiled</em> Complete OCL document - an m2x {@code CompiledUnit} of
 * language {@code ocl} - derived from the reviews of the metamodels its manifest says it was
 * compiled against.
 *
 * <p>
 * The difference to {@link OclConstraintAnalysis}, which reads an {@code OclConstraintSet}, is not
 * the walk but where the revision comes from. A constraint set names its metamodels by nsURI and
 * nothing else, so that analyser has to fingerprint whatever the runtime holds <em>now</em> and the
 * report cannot tell whether the model moved since the constraints were written. A compiled unit
 * carries a {@link PackageEntry} per package its AST actually refers to, each with the fingerprint
 * the compiler saw, computed by the same {@code FingerprintService} the reviewer's value came from.
 * So the join here is a lookup, not a reconstruction, and {@code SOURCE_UNREVIEWED} means what it
 * says: no review of <em>that revision</em>, rather than no review of the current one.
 * </p>
 *
 * <p>
 * The second difference is what a document can say. {@code ConstraintKind} has seven values where
 * {@code OclRole} has four: {@code def} introduces a name no review speaks for, and {@code init}
 * and {@code pre}/{@code post} exist at all. What is lost is everything the constraint set carries
 * around the expression - no message, no {@code severity}, no {@code active} flag - so
 * {@code MESSAGE_LEAK} and {@code INACTIVE_GUARD} have nothing to read here.
 * </p>
 */
public final class OclDocumentAnalysis {

	/** The rule catalogue this report was produced by, versioned into {@code generatedBy}. */
	public static final String CATALOGUE = "ocl-document-analysis/1";

	/** What this report is, for an artefact that records the revision it was built against. */
	static final String DISCLAIMER = StaticReport.DISCLAIMER.replace(
			"The artefact records no revision of the metamodel, so the fingerprint named here is the "
					+ "revision resolved at analysis time, not the one the author saw: if the model moved "
					+ "since, this report is about the model as it is now.",
			"The artefact records the revision it was compiled against, so the fingerprint named here is "
					+ "the one the compiler saw: where no review of that revision exists this report says "
					+ "so instead of silently analysing a later one.");

	private final Map<String, ReviewIndex> reviewsByFingerprint;
	private final RequirementCitations citations;

	/** Per package nsURI, the review of exactly the revision the manifest names. */
	private final Map<String, ReviewIndex> reviewsByNsURI = new LinkedHashMap<>();

	/**
	 * @param reviewsByFingerprint the reviews reachable along the stage ladder, keyed by the
	 *                             fingerprint of the package each is about - what
	 *                             {@code StagedReviews.resolve} answers in the real analyser
	 * @param citations            the compliance context, for findings of the analyser's own
	 */
	public OclDocumentAnalysis(Map<String, ReviewIndex> reviewsByFingerprint, RequirementCitations citations) {
		this.reviewsByFingerprint = reviewsByFingerprint;
		this.citations = citations;
	}

	/**
	 * The report for one compiled document.
	 *
	 * @throws IllegalStateException when no package the unit refers to has a review of the recorded
	 *                               revision. The real analyser writes an ERROR diagnostic onto the
	 *                               unit instead of a report in that case, exactly as
	 *                               {@code TransformationAnalysis} does; a prototype says it loudly.
	 */
	public ComplianceReport analyse(CompiledUnit unit) {
		CompiledUnitManifest manifest = unit.getManifest();
		if (!(unit.getUnit() instanceof CompleteOclDocument document)) {
			throw new IllegalStateException("'" + manifest.getQualifiedName() + "' is no Complete OCL document but a "
					+ unit.getUnit().eClass().getName());
		}
		List<PackageEntry> unreviewed = new ArrayList<>();
		for (PackageEntry entry : manifest.getPackageEntry()) {
			ReviewIndex review = reviewsByFingerprint.get(entry.getFingerprint());
			if (review == null) {
				unreviewed.add(entry);
			} else {
				reviewsByNsURI.put(entry.getNsURI(), review);
			}
		}
		ComplianceReport report = report(unit, manifest);
		for (PackageEntry entry : unreviewed) {
			sourceUnreviewed(report, manifest, entry);
		}
		for (DependencyEntry dependency : manifest.getDependencyEntry()) {
			importsUnreviewedUnit(report, dependency);
		}
		for (Constraint constraint : document.getConstraints()) {
			analyse(report, constraint);
		}
		return report;
	}

	/* ------------------------------------------------------------------------------- the rules */

	private void analyse(ComplianceReport report, Constraint constraint) {
		if (constraint.getContextClassifier() == null || constraint.getSpecification() == null) {
			opaque(report, constraint, "it has no context classifier or no expression");
			return;
		}
		OclExpression specification = constraint.getSpecification();
		List<EStructuralFeature> read = OclConstraintAnalysis.featuresRead(specification);
		for (EStructuralFeature feature : read) {
			if (feature.eIsProxy()) {
				opaque(report, constraint, "a property it reads did not resolve");
				return;
			}
		}
		unresolvedNavigation(report, constraint, read);
		List<EStructuralFeature> classified = read.stream().filter(this::isClassified).toList();

		switch (constraint.getKind()) {
		case DERIVE, INIT -> derived(report, constraint, classified);
		case DEF -> defined(report, constraint, classified);
		case BODY -> operation(report, constraint, classified);
		case INV, PRE, POST -> invariant(report, constraint, classified);
		}
		concatenation(report, constraint, specification, classified);
		selection(report, constraint, specification);
		bulkRead(report, constraint, specification);
	}

	/**
	 * {@code DERIVED_PROPAGATION} and {@code DERIVED_AGGREGATION}: what a {@code derive} or
	 * {@code init} writes is a feature of the model, so the review speaks for both ends and the
	 * comparison is exact. Propagation is the stronger statement - the target is classified more
	 * weakly than the source, or not at all - and aggregation is the one the QVT analyser was built
	 * for, reproduced inside a single model.
	 */
	private void derived(ComplianceReport report, Constraint constraint, List<EStructuralFeature> classified) {
		EStructuralFeature target = constraint.getContextProperty();
		if (target == null) {
			opaque(report, constraint, "a " + constraint.getKind() + " constraint with no context property");
			return;
		}
		String targetCategory = categoryOf(target);
		List<EStructuralFeature> weakened = classified.stream()
				.filter(source -> targetCategory == null || ReviewIndex.weakerThan(targetCategory, categoryOf(source)))
				.toList();
		if (!weakened.isEmpty()) {
			FlowEvaluation evaluation = StaticReport.flow(report, "DERIVED_PROPAGATION:" + nameOf(constraint),
					nameOf(constraint) + " writes classified data into " + OclConstraintAnalysis.fragmentOf(target));
			for (EStructuralFeature source : weakened) {
				FeatureEvaluation reviewed = reviewedFeature(source);
				Finding finding = StaticReport.carried(
						"DERIVED_PROPAGATION:" + nameOf(constraint) + ":" + OclConstraintAnalysis.fragmentOf(source),
						String.format(
								"The %s constraint on %s computes its value from %s, classified %s, and writes it "
										+ "into a feature the review classifies %s. The value arrives where less is "
										+ "asked of whoever processes it.",
								constraint.getKind().getName(), OclConstraintAnalysis.fragmentOf(target),
								OclConstraintAnalysis.fragmentOf(source), categoryOf(source),
								targetCategory == null ? "not at all" : targetCategory),
						RelevanceLevel.HIGH, reviewed, categoryOf(source));
				if (finding != null) {
					evaluation.getFindings().add(finding);
				}
			}
		}
		if (classified.size() < 2) {
			return;
		}
		FlowEvaluation evaluation = StaticReport.flow(report, "DERIVED_AGGREGATION:" + nameOf(constraint),
				classified.size() + " classified features reach " + OclConstraintAnalysis.fragmentOf(target));
		for (EStructuralFeature source : classified) {
			FeatureEvaluation reviewed = reviewedFeature(source);
			Finding finding = StaticReport.carried(
					"DERIVED_AGGREGATION:" + nameOf(constraint) + ":" + OclConstraintAnalysis.fragmentOf(source),
					String.format(
							"%s is derived from %d classified features, %s among them. What the derived value holds "
									+ "can no longer be minimised or erased per source field.",
							OclConstraintAnalysis.fragmentOf(target), classified.size(),
							OclConstraintAnalysis.fragmentOf(source)),
					RelevanceLevel.MEDIUM, reviewed, categoryOf(source));
			if (finding != null) {
				evaluation.getFindings().add(finding);
			}
		}
	}

	/**
	 * {@code DEFINITION_DISCLOSURE}, and {@code OPERATION_DISCLOSURE} where the definition is an
	 * operation. A {@code def} introduces a name that did not exist in the metamodel: it is
	 * readable by everything the document is registered for, and no review classifies it, because
	 * no review has ever seen it. That is the widest statement this catalogue can make.
	 */
	private void defined(ComplianceReport report, Constraint constraint, List<EStructuralFeature> classified) {
		if (classified.isEmpty()) {
			return;
		}
		// An operation def and an attribute def are both DEFINITION_DISCLOSURE: what makes the
		// statement is that the name is new, not whether it takes parentheses. OPERATION_DISCLOSURE
		// stays with body:, where the operation is one the metamodel itself declares.
		boolean operation = constraint.getContextOperation() != null;
		String defined = nameOf(constraint) + (operation ? "()" : "");
		FlowEvaluation evaluation = StaticReport.flow(report, "DEFINITION_DISCLOSURE:" + nameOf(constraint),
				defined + (operation ? " returns" : " holds") + " a value computed from classified data");
		for (EStructuralFeature source : classified) {
			FeatureEvaluation reviewed = reviewedFeature(source);
			Finding finding = StaticReport.carried(
					"DEFINITION_DISCLOSURE:" + nameOf(constraint) + ":" + OclConstraintAnalysis.fragmentOf(source),
					String.format(
							"The document defines %s on %s from %s, classified %s. The defined name is not part of "
									+ "the metamodel, so no review of that model classifies it, and it is readable "
									+ "wherever this document is registered.",
							defined, constraint.getContextClassifier().getName(),
							OclConstraintAnalysis.fragmentOf(source), categoryOf(source)),
					RelevanceLevel.HIGH, reviewed, categoryOf(source));
			if (finding != null) {
				evaluation.getFindings().add(finding);
			}
		}
	}

	/**
	 * {@code OPERATION_DISCLOSURE} for a {@code body} constraint: the return value of an operation
	 * the metamodel declares, computed from classified features.
	 */
	private void operation(ComplianceReport report, Constraint constraint, List<EStructuralFeature> classified) {
		if (classified.isEmpty()) {
			return;
		}
		FlowEvaluation evaluation = StaticReport.flow(report, "OPERATION_DISCLOSURE:" + nameOf(constraint),
				nameOf(constraint) + " returns a value computed from classified data");
		for (EStructuralFeature source : classified) {
			FeatureEvaluation reviewed = reviewedFeature(source);
			Finding finding = StaticReport.carried(
					"OPERATION_DISCLOSURE:" + nameOf(constraint) + ":" + OclConstraintAnalysis.fragmentOf(source),
					String.format(
							"The body of %s computes its return value from %s, classified %s. An operation is a read "
									+ "channel of its own, and the review of the model says nothing about it.",
							nameOf(constraint), OclConstraintAnalysis.fragmentOf(source), categoryOf(source)),
					RelevanceLevel.HIGH, reviewed, categoryOf(source));
			if (finding != null) {
				evaluation.getFindings().add(finding);
			}
		}
	}

	/**
	 * {@code INVARIANT_ON_SPECIAL_CATEGORY}: a rule the model enforces about data of a special
	 * category. A control, not a flow - nothing travels - and the question it raises is whether the
	 * model should hold that data at all.
	 */
	private void invariant(ComplianceReport report, Constraint constraint, List<EStructuralFeature> classified) {
		for (EStructuralFeature feature : classified) {
			if (!"SPECIAL_CATEGORY".equals(categoryOf(feature))) {
				continue;
			}
			Finding finding = StaticReport.control(
					"INVARIANT_ON_SPECIAL_CATEGORY:" + nameOf(constraint) + ":"
							+ OclConstraintAnalysis.fragmentOf(feature),
					String.format(
							"The invariant '%s' constrains %s, which the review classifies SPECIAL_CATEGORY. The "
									+ "model enforces a rule about data whose processing needs its own legal basis.",
							nameOf(constraint), OclConstraintAnalysis.fragmentOf(feature)),
					"Record the basis under Art. 9(2), or establish that the feature need not hold that data.",
					RelevanceLevel.HIGH, citations, "Art.9(2)", language(),
					ReviewIndex.refFor(reviewedFeature(feature), categoryOf(feature)));
			if (finding != null) {
				StaticReport.control(report, "gdpr", "Art.9(2)", ControlResult.NOT_SATISFIED).getFindings()
						.add(finding);
			}
		}
	}

	/**
	 * {@code DEFINED_CONCATENATION}: a {@code def} or {@code derive} that builds a string out of an
	 * identifier. The result is a new value nobody classified, and concatenation is what makes it
	 * unerasable per source field.
	 */
	private void concatenation(ComplianceReport report, Constraint constraint, OclExpression specification,
			List<EStructuralFeature> classified) {
		if (constraint.getKind() == null || !concatenatesAString(specification)) {
			return;
		}
		switch (constraint.getKind()) {
		case DEF, DERIVE, INIT -> {
			for (EStructuralFeature feature : classified) {
				String category = categoryOf(feature);
				if (!"DIRECT_IDENTIFIER".equals(category) && !"SPECIAL_CATEGORY".equals(category)) {
					continue;
				}
				Finding finding = StaticReport.control(
						"DEFINED_CONCATENATION:" + nameOf(constraint) + ":"
								+ OclConstraintAnalysis.fragmentOf(feature),
						String.format(
								"'%s' concatenates %s, classified %s, into a string. The combined value identifies "
										+ "a person as directly as its parts and can no longer be erased or "
										+ "minimised field by field.",
								nameOf(constraint), OclConstraintAnalysis.fragmentOf(feature), category),
						"Build the label from a surrogate, or keep the parts separate and combine them where the "
								+ "purpose is established.",
						RelevanceLevel.MEDIUM, citations, "Art.5(1)(c)", language(),
						ReviewIndex.refFor(reviewedFeature(feature), category));
				if (finding != null) {
					StaticReport.control(report, "gdpr", "Art.5(1)(c)", ControlResult.NOT_SATISFIED).getFindings()
							.add(finding);
				}
			}
		}
		default -> {
			// an inv or a pre/post builds no value anybody keeps
		}
		}
	}

	/**
	 * {@code SELECTION_ON_PERSONAL}: a {@code select} or {@code reject} whose body reads an
	 * identifier. Which data subjects the expression reaches is decided by that category - a
	 * control statement, because nothing travels.
	 */
	private void selection(ComplianceReport report, Constraint constraint, OclExpression specification) {
		for (EObject node : walk(specification)) {
			if (!(node instanceof IteratorExp iterator)
					|| !("select".equals(iterator.getName()) || "reject".equals(iterator.getName()))) {
				continue;
			}
			OclExpression body = ((LoopExp) iterator).getOwnedBody();
			if (body == null) {
				continue;
			}
			for (EStructuralFeature feature : OclConstraintAnalysis.featuresRead(body)) {
				String category = categoryOf(feature);
				if (!"DIRECT_IDENTIFIER".equals(category) && !"SPECIAL_CATEGORY".equals(category)) {
					continue;
				}
				Finding finding = StaticReport.control(
						"SELECTION_ON_PERSONAL:" + nameOf(constraint) + ":"
								+ OclConstraintAnalysis.fragmentOf(feature),
						String.format(
								"'%s' selects objects by %s, classified %s. Which data subjects the expression "
										+ "reaches is therefore decided by that category.",
								nameOf(constraint), OclConstraintAnalysis.fragmentOf(feature), category),
						"Select on a surrogate the caller already holds rather than on the identifier itself.",
						RelevanceLevel.MEDIUM, citations, "Art.5(1)(b)", language(),
						ReviewIndex.refFor(reviewedFeature(feature), category));
				if (finding != null) {
					StaticReport.control(report, "gdpr", "Art.5(1)(b)", ControlResult.NOT_SATISFIED).getFindings()
							.add(finding);
				}
			}
		}
	}

	/**
	 * {@code BULK_READ}: {@code allInstances()} over a classifier the review classifies. Every
	 * record of every data subject, read in one evaluation.
	 */
	private void bulkRead(ComplianceReport report, Constraint constraint, OclExpression specification) {
		for (EClassifier over : allInstancesOver(specification)) {
			if (!classifiesAnythingOf(over)) {
				continue;
			}
			Finding finding = StaticReport.control("BULK_READ:" + nameOf(constraint) + ":" + over.getName(),
					String.format(
							"'%s' calls allInstances() over %s, whose features the review classifies. The "
									+ "expression reads every record of every data subject in one evaluation.",
							nameOf(constraint), over.getName()),
					"Scope the expression to the objects the caller is entitled to, rather than to the type.",
					RelevanceLevel.MEDIUM, citations, "Art.5(1)(c)", language(), null);
			if (finding != null) {
				StaticReport.control(report, "gdpr", "Art.5(1)(c)", ControlResult.NOT_SATISFIED).getFindings()
						.add(finding);
			}
		}
	}

	/**
	 * {@code UNRESOLVED_NAVIGATION}: an ERROR, and the one this prototype did not expect.
	 *
	 * <p>
	 * The parser leaves an iterator variable untyped - {@code ->select(b | b.bereich = ...)} gives
	 * {@code b} no type - so the property read through it does not resolve to a metamodel feature.
	 * It becomes a synthetic {@code EStructuralFeature} that travels with the unit as a satellite,
	 * keeping the written name and nothing else. Such a read joins to no review, which would make
	 * the analysis quietly incomplete: the whole selector of a filter can be invisible while the
	 * report looks finished. So it is named.
	 * </p>
	 */
	private void unresolvedNavigation(ComplianceReport report, Constraint constraint,
			List<EStructuralFeature> read) {
		List<EStructuralFeature> loose = read.stream().filter(feature -> feature.getEContainingClass() == null)
				.toList();
		if (loose.isEmpty()) {
			return;
		}
		StaticReport.flow(report, "UNRESOLVED_NAVIGATION:" + nameOf(constraint), String.format(
				"'%s' reads %s through a variable the parser leaves untyped, so %s does not resolve to a feature of "
						+ "any metamodel and no review speaks for it. What that navigation touches is outside this "
						+ "report.",
				nameOf(constraint),
				loose.stream().map(EStructuralFeature::getName).distinct().collect(java.util.stream.Collectors
						.joining("', '", "'", "'")),
				loose.size() == 1 ? "it" : "they"));
	}

	/**
	 * {@code SOURCE_UNREVIEWED}: an ERROR. The manifest names a revision, so this says that no
	 * review of <em>that</em> revision is reachable - not that the package is unknown.
	 */
	private void sourceUnreviewed(ComplianceReport report, CompiledUnitManifest manifest, PackageEntry entry) {
		StaticReport.flow(report, "SOURCE_UNREVIEWED:" + entry.getNsURI(),
				String.format(
						"'%s' was compiled against %s at %s, and no review of that revision is reachable from here. "
								+ "Nothing is asserted about the features of that package.",
						manifest.getQualifiedName(), entry.getNsURI(), entry.getFingerprint()));
	}

	/** {@code IMPORTS_UNREVIEWED_UNIT}: an ERROR, for the same reason one stage up. */
	private void importsUnreviewedUnit(ComplianceReport report, DependencyEntry dependency) {
		StaticReport.flow(report, "IMPORTS_UNREVIEWED_UNIT:" + dependency.getQualifiedName(),
				String.format(
						"The document imports '%s' (%s), and this prototype analyses one unit at a time. What that "
								+ "unit defines is not covered by this report.",
						dependency.getQualifiedName(),
						dependency.getFingerprint() == null ? "no recorded fingerprint" : dependency.getFingerprint()));
	}

	/** {@code OPAQUE_EXPRESSION}: an ERROR, because silence would read as clean. */
	private void opaque(ComplianceReport report, Constraint constraint, String why) {
		StaticReport.flow(report, "OPAQUE_EXPRESSION:" + nameOf(constraint), String.format(
				"'%s' could not be analysed: %s. Nothing is asserted about it.", nameOf(constraint), why));
	}

	/* ------------------------------------------------------------------------------- the walk */

	/** The classifiers {@code allInstances()} is called over in this expression, without repetition. */
	private static List<EClassifier> allInstancesOver(OclExpression expression) {
		Set<EClassifier> over = new LinkedHashSet<>();
		for (EObject node : walk(expression)) {
			if (node instanceof OperationCallExp call && "allInstances".equals(call.getName())
					&& call.getOwnedSource() instanceof TypeExp type && type.getReferredType() != null) {
				over.add(type.getReferredType());
			}
		}
		return new ArrayList<>(over);
	}

	/** Whether a string literal is combined with anything, which is what builds a label. */
	private static boolean concatenatesAString(OclExpression expression) {
		for (EObject node : walk(expression)) {
			if (node instanceof OperationCallExp call && ("+".equals(call.getName()) || "concat".equals(call.getName()))
					&& (call.getOwnedSource() instanceof StringLiteralExp
							|| call.getOwnedArguments().stream().anyMatch(StringLiteralExp.class::isInstance))) {
				return true;
			}
		}
		return false;
	}

	private static List<EObject> walk(OclExpression expression) {
		List<EObject> all = new ArrayList<>();
		all.add(expression);
		expression.eAllContents().forEachRemaining(all::add);
		return all;
	}

	/* ------------------------------------------------------------------------------- the join */

	/** The review of the package this feature belongs to, or {@code null} when there is none. */
	private ReviewIndex reviewOf(EStructuralFeature feature) {
		EPackage ePackage = feature.getEContainingClass() == null ? null : feature.getEContainingClass().getEPackage();
		return ePackage == null ? null : reviewsByNsURI.get(ePackage.getNsURI());
	}

	private FeatureEvaluation reviewedFeature(EStructuralFeature feature) {
		ReviewIndex review = reviewOf(feature);
		return review == null ? null : review.feature(OclConstraintAnalysis.fragmentOf(feature));
	}

	private String categoryOf(EStructuralFeature feature) {
		return ReviewIndex.worstCategory(reviewedFeature(feature));
	}

	private boolean isClassified(EStructuralFeature feature) {
		return categoryOf(feature) != null;
	}

	/**
	 * Whether the review classifies any feature of this classifier.
	 *
	 * <p>
	 * Deliberately not "the review mentions a fragment under this classifier": a review carries a
	 * {@code FeatureEvaluation} for every feature of every classifier, and most of them say
	 * nothing. Testing for mention would make this fire for the opening hours.
	 * </p>
	 */
	private boolean classifiesAnythingOf(EClassifier classifier) {
		if (!(classifier instanceof EClass eClass)) {
			return false;
		}
		ReviewIndex review = eClass.getEPackage() == null ? null : reviewsByNsURI.get(eClass.getEPackage().getNsURI());
		if (review == null) {
			return false;
		}
		for (EStructuralFeature feature : eClass.getEAllStructuralFeatures()) {
			if (ReviewIndex.worstCategory(review.feature(OclConstraintAnalysis.fragmentOf(feature))) != null) {
				return true;
			}
		}
		return false;
	}

	private String language() {
		return primaryReview().language();
	}

	private ReviewIndex primaryReview() {
		return reviewsByNsURI.values().iterator().next();
	}

	private static String nameOf(Constraint constraint) {
		if (constraint.getName() != null && !constraint.getName().isBlank()) {
			return constraint.getName();
		}
		String context = constraint.getContextProperty() != null ? constraint.getContextProperty().getName()
				: constraint.getContextClassifier() == null ? "?" : constraint.getContextClassifier().getName();
		return (constraint.getKind() == null ? "constraint" : constraint.getKind().getName()) + " on " + context;
	}

	/* ------------------------------------------------------------------------------- the report */

	/**
	 * The report shell. Everything that differs from the other three analysers is here: the subject
	 * carries the <em>unit</em> fingerprint, which is what the metadata action matches on, and one
	 * {@code PackageSubject} per reviewed {@code packageEntry}, which is what the re-analysis
	 * trigger keys on.
	 */
	private ComplianceReport report(CompiledUnit unit, CompiledUnitManifest manifest) {
		if (reviewsByNsURI.isEmpty()) {
			throw new IllegalStateException("no package '" + manifest.getQualifiedName()
					+ "' was compiled against has a review of the recorded revision; the real analyser writes an "
					+ "ERROR diagnostic onto the unit here instead of a report");
		}
		ReviewIndex primary = primaryReview();
		EPackage source = packageOf(unit, firstReviewedNsURI());
		ComplianceReport report = StaticReport.about(CATALOGUE, "GDPR analysis of " + manifest.getQualifiedName(),
				manifest.getQualifiedName(), "ocl", source, fingerprintOf(manifest, source.getNsURI()), primary);
		report.setDisclaimer(DISCLAIMER);
		TransformationSubject subject = (TransformationSubject) report.getSubject();
		subject.setSubjectFingerprint(manifest.getUnitFingerprint());
		for (PackageEntry entry : manifest.getPackageEntry()) {
			ReviewIndex review = reviewsByFingerprint.get(entry.getFingerprint());
			if (review == null || entry.getNsURI().equals(source.getNsURI())) {
				continue;
			}
			PackageSubject further = ReportFactory.eINSTANCE.createPackageSubject();
			further.setNsURI(entry.getNsURI());
			further.setSubjectFingerprint(entry.getFingerprint());
			further.setReportId(review.reportId());
			subject.getSourcePackages().add(further);
		}
		return report;
	}

	private String firstReviewedNsURI() {
		return reviewsByNsURI.keySet().iterator().next();
	}

	private static String fingerprintOf(CompiledUnitManifest manifest, String nsURI) {
		for (PackageEntry entry : manifest.getPackageEntry()) {
			if (nsURI.equals(entry.getNsURI())) {
				return entry.getFingerprint();
			}
		}
		return null;
	}

	/**
	 * The package instance the AST actually refers to. A dynamic package is also carried inside the
	 * unit, but the AST points at the one the compiler had, which is the one whose features the
	 * fragments are taken from.
	 */
	private static EPackage packageOf(CompiledUnit unit, String nsURI) {
		if (unit.getUnit() instanceof CompleteOclDocument document) {
			for (Constraint constraint : document.getConstraints()) {
				EClassifier context = constraint.getContextClassifier();
				if (context != null && context.getEPackage() != null
						&& nsURI.equals(context.getEPackage().getNsURI())) {
					return context.getEPackage();
				}
			}
		}
		for (EPackage carried : unit.getPackages()) {
			if (nsURI.equals(carried.getNsURI())) {
				return carried;
			}
		}
		throw new IllegalStateException("the unit refers to " + nsURI + " but holds no instance of it");
	}
}
