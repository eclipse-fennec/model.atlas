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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.m2x.model.ocl.OclExpression;
import org.eclipse.fennec.m2x.model.ocl.OperationCallExp;
import org.eclipse.fennec.m2x.model.ocl.PropertyCallExp;
import org.eclipse.fennec.m2x.model.ocl.StringLiteralExp;
import org.eclipse.fennec.m2x.model.ocl.TypeExp;
import org.eclipse.fennec.m2x.ocl.api.OclEngine;
import org.eclipse.fennec.m2x.ocl.api.OclParseException;
import org.eclipse.fennec.model.atlas.compliance.analysis.RequirementCitations;
import org.eclipse.fennec.model.atlas.compliance.analysis.ReviewIndex;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclRole;
import org.eclipse.fennec.model.compliance.report.ComplianceReport;
import org.eclipse.fennec.model.compliance.report.ControlResult;
import org.eclipse.fennec.model.compliance.report.FeatureEvaluation;
import org.eclipse.fennec.model.compliance.report.Finding;
import org.eclipse.fennec.model.compliance.report.FlowEvaluation;
import org.eclipse.fennec.model.compliance.report.RelevanceLevel;

/**
 * A GDPR report for an {@code OclConstraintSet}, derived from the review of the metamodel its
 * constraints are written against.
 *
 * <p>
 * The join is exact and this is the whole reason OCL was taken first:
 * {@code PropertyCallExp.referredProperty} is a resolved {@code EStructuralFeature}, so the
 * fragment of its URI <em>is</em> the review's {@code FeatureEvaluation.uriFragment}. No name
 * matching, which is the QVT analyser's one fragile joint.
 * </p>
 */
public final class OclConstraintAnalysis {

	/** The rule catalogue this report was produced by, versioned into {@code generatedBy}. */
	public static final String CATALOGUE = "ocl-constraint-analysis/1";

	private final OclEngine engine;
	private final ReviewIndex review;
	private final RequirementCitations citations;
	private final EPackage source;
	private final String fingerprint;

	public OclConstraintAnalysis(OclEngine engine, ReviewIndex review, RequirementCitations citations, EPackage source,
			String fingerprint) {
		this.engine = engine;
		this.review = review;
		this.citations = citations;
		this.source = source;
		this.fingerprint = fingerprint;
	}

	/**
	 * The report for one constraint set.
	 */
	public ComplianceReport analyse(OclConstraintSet set) {
		ComplianceReport report = StaticReport.about(CATALOGUE, "GDPR analysis of " + set.getName(), set.getName(),
				"ocl", source, fingerprint, review);
		for (OclConstraint constraint : set.getConstraints()) {
			analyse(report, constraint);
		}
		return report;
	}

	private void analyse(ComplianceReport report, OclConstraint constraint) {
		EClassifier context = contextOf(constraint);
		if (context == null) {
			opaque(report, constraint, "its contextClass does not resolve in the reviewed package");
			return;
		}
		OclExpression expression;
		try {
			expression = engine.parse(constraint.getExpression(), context);
		} catch (OclParseException unparseable) {
			// Loud, not silent. A rule table is the ceiling of what the analysis can see, and an
			// expression nobody could read is the one case where that ceiling is zero.
			opaque(report, constraint, "it does not parse: " + unparseable.getMessage());
			return;
		}
		List<EStructuralFeature> read = featuresRead(expression);
		List<EStructuralFeature> classified = read.stream().filter(this::isClassified).toList();

		switch (constraint.getRole()) {
		case VALIDATION -> validation(report, constraint, expression, classified);
		case DERIVED -> derived(report, constraint, classified);
		case OPERATION -> operation(report, constraint, classified);
		case REFERENCE_FILTER -> filter(report, constraint, classified);
		}
		bulkRead(report, constraint, expression);
	}

	/* ------------------------------------------------------------------------------- the rules */

	/**
	 * {@code MESSAGE_LEAK}: a classified value concatenated into a string the constraint's
	 * {@code Diagnostic} will carry. The message is stored and logged, so the value leaves the
	 * model and lands somewhere no review covers.
	 */
	private void validation(ComplianceReport report, OclConstraint constraint, OclExpression expression,
			List<EStructuralFeature> classified) {
		if (!constraint.isActive()) {
			inactiveGuard(report, constraint);
			return;
		}
		if (classified.isEmpty() || !concatenatesAString(expression)) {
			return;
		}
		FlowEvaluation evaluation = StaticReport.flow(report, "MESSAGE_LEAK:" + constraint.getName(),
				constraint.getName() + " builds a message from classified data");
		for (EStructuralFeature feature : classified) {
			FeatureEvaluation reviewed = review.feature(fragmentOf(feature));
			Finding finding = StaticReport.carried("MESSAGE_LEAK:" + constraint.getName() + ":" + fragmentOf(feature),
					String.format(
							"The validation constraint '%s' concatenates %s into a string. A failing validation "
									+ "writes that string into a Diagnostic, which is stored and logged, so the "
									+ "value reaches a place the review of the model does not cover.",
							constraint.getName(), fragmentOf(feature)),
					RelevanceLevel.HIGH, reviewed, ReviewIndex.worstCategory(reviewed));
			if (finding != null) {
				evaluation.getFindings().add(finding);
			}
		}
	}

	/**
	 * {@code DERIVED_AGGREGATION}: two or more classified features reach one derived feature. The
	 * derived value cannot be minimised or erased per source field any more - the motivating QVT
	 * case, reproduced inside a single model.
	 */
	private void derived(ComplianceReport report, OclConstraint constraint, List<EStructuralFeature> classified) {
		if (classified.size() < 2 || constraint.getFeatureName() == null) {
			return;
		}
		FlowEvaluation evaluation = StaticReport.flow(report, "DERIVED_AGGREGATION:" + constraint.getName(),
				classified.size() + " classified features reach " + constraint.getFeatureName());
		for (EStructuralFeature feature : classified) {
			FeatureEvaluation reviewed = review.feature(fragmentOf(feature));
			Finding finding = StaticReport.carried(
					"DERIVED_AGGREGATION:" + constraint.getName() + ":" + fragmentOf(feature),
					String.format(
							"'%s' derives %s from %d classified features, %s among them. What the derived value "
									+ "holds can no longer be minimised or erased per source field.",
							constraint.getName(), constraint.getFeatureName(), classified.size(),
							fragmentOf(feature)),
					RelevanceLevel.MEDIUM, reviewed, ReviewIndex.worstCategory(reviewed));
			if (finding != null) {
				evaluation.getFindings().add(finding);
			}
		}
	}

	/**
	 * {@code OPERATION_DISCLOSURE}: an operation's return value is computed from classified
	 * features. The operation is callable over the validation REST surface, which is an access
	 * channel no metamodel review covers.
	 */
	private void operation(ComplianceReport report, OclConstraint constraint, List<EStructuralFeature> classified) {
		if (classified.isEmpty()) {
			return;
		}
		FlowEvaluation evaluation = StaticReport.flow(report, "OPERATION_DISCLOSURE:" + constraint.getName(),
				constraint.getOperationName() + " returns a value computed from classified data");
		for (EStructuralFeature feature : classified) {
			FeatureEvaluation reviewed = review.feature(fragmentOf(feature));
			Finding finding = StaticReport.carried(
					"OPERATION_DISCLOSURE:" + constraint.getName() + ":" + fragmentOf(feature),
					String.format(
							"The operation '%s' computes its return value from %s. It is callable over the "
									+ "validation REST surface, so this is a read channel the review of the model "
									+ "says nothing about.",
							constraint.getOperationName(), fragmentOf(feature)),
					RelevanceLevel.HIGH, reviewed, ReviewIndex.worstCategory(reviewed));
			if (finding != null) {
				evaluation.getFindings().add(finding);
			}
		}
	}

	/**
	 * {@code FILTER_ON_PERSONAL}: which objects a caller may reach is decided by a classified
	 * feature. A control statement, not a flow - nothing travels, but the selection of data
	 * subjects rests on a category.
	 */
	private void filter(ComplianceReport report, OclConstraint constraint, List<EStructuralFeature> classified) {
		for (EStructuralFeature feature : classified) {
			FeatureEvaluation reviewed = review.feature(fragmentOf(feature));
			String category = ReviewIndex.worstCategory(reviewed);
			if (!"DIRECT_IDENTIFIER".equals(category) && !"SPECIAL_CATEGORY".equals(category)) {
				continue;
			}
			Finding finding = StaticReport.control("FILTER_ON_PERSONAL:" + constraint.getName(),
					String.format(
							"The reference filter '%s' selects objects by %s, classified %s. Which data subjects "
									+ "a caller can reach is therefore decided by that category.",
							constraint.getName(), fragmentOf(feature), category),
					"Filter on a surrogate the caller already holds rather than on the identifier itself.",
					RelevanceLevel.MEDIUM, citations, "Art.5(1)(b)", review.language(),
					ReviewIndex.refFor(reviewed, category));
			if (finding != null) {
				StaticReport.control(report, "gdpr", "Art.5(1)(b)", ControlResult.NOT_SATISFIED).getFindings()
						.add(finding);
			}
		}
	}

	/**
	 * {@code BULK_READ}: {@code allInstances()} over a classifier the review classifies. Every
	 * record of every data subject, read in one call.
	 */
	private void bulkRead(ComplianceReport report, OclConstraint constraint, OclExpression expression) {
		for (EClassifier over : allInstancesOver(expression)) {
			if (!classifiesAnythingOf(over)) {
				continue;
			}
			Finding finding = StaticReport.control("BULK_READ:" + constraint.getName() + ":" + over.getName(),
					String.format(
							"'%s' calls allInstances() over %s, whose features the review classifies. The "
									+ "expression reads every record of every data subject in one evaluation.",
							constraint.getName(), over.getName()),
					"Scope the expression to the objects the caller is entitled to, rather than to the type.",
					RelevanceLevel.MEDIUM, citations, "Art.5(1)(c)", review.language(), null);
			if (finding != null) {
				StaticReport.control(report, "gdpr", "Art.5(1)(c)", ControlResult.NOT_SATISFIED).getFindings()
						.add(finding);
			}
		}
	}

	/** {@code INACTIVE_GUARD}: a protective constraint that is switched off. */
	private void inactiveGuard(ComplianceReport report, OclConstraint constraint) {
		Finding finding = StaticReport.control("INACTIVE_GUARD:" + constraint.getName(),
				String.format(
						"'%s' is a validation that would constrain classified data, and it is inactive. A "
								+ "measure that is configured off is not a measure.",
						constraint.getName()),
				"Re-activate the constraint, or record why it was accepted as a residual risk.",
				RelevanceLevel.MEDIUM, citations, "Art.5(2)", review.language(), null);
		if (finding != null) {
			StaticReport.control(report, "gdpr", "Art.5(2)", ControlResult.NOT_SATISFIED).getFindings().add(finding);
		}
	}

	/** {@code OPAQUE_EXPRESSION}: an ERROR, because silence would read as clean. */
	private void opaque(ComplianceReport report, OclConstraint constraint, String why) {
		StaticReport.flow(report, "OPAQUE_EXPRESSION:" + constraint.getName(),
				String.format("'%s' could not be analysed: %s. Nothing is asserted about it.", constraint.getName(),
						why));
	}

	/* ------------------------------------------------------------------------------- the walk */

	/** Every feature the expression reads, in encounter order, without repetition. */
	static List<EStructuralFeature> featuresRead(OclExpression expression) {
		Set<EStructuralFeature> features = new LinkedHashSet<>();
		for (EObject node : walk(expression)) {
			if (node instanceof PropertyCallExp property && property.getReferredProperty() != null) {
				features.add(property.getReferredProperty());
			}
		}
		return new ArrayList<>(features);
	}

	/** The classifiers {@code allInstances()} is called over in this expression. */
	private static List<EClassifier> allInstancesOver(OclExpression expression) {
		List<EClassifier> over = new ArrayList<>();
		for (EObject node : walk(expression)) {
			if (node instanceof OperationCallExp call && "allInstances".equals(call.getName())
					&& call.getOwnedSource() instanceof TypeExp type && type.getReferredType() != null) {
				over.add(type.getReferredType());
			}
		}
		return over;
	}

	/** Whether a string literal is combined with anything, which is what builds a message. */
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

	private boolean isClassified(EStructuralFeature feature) {
		return ReviewIndex.worstCategory(review.feature(fragmentOf(feature))) != null;
	}

	private boolean classifiesAnythingOf(EClassifier classifier) {
		String prefix = "//" + classifier.getName() + "/";
		return review.fragments().stream().anyMatch(fragment -> fragment.startsWith(prefix));
	}

	/**
	 * The classifier a constraint is written against. {@code contextClass} is a full URI - the
	 * validation service resolves it the same way - so the package is derivable from it and no
	 * name lookup is involved.
	 */
	private EClassifier contextOf(OclConstraint constraint) {
		String uri = constraint.getContextClass();
		if (uri == null || !uri.contains("#//")) {
			return null;
		}
		return source.getEClassifier(uri.substring(uri.lastIndexOf("#//") + 3));
	}

	/** The review's own key for a feature. */
	static String fragmentOf(EStructuralFeature feature) {
		return EcoreUtil.getURI(feature).fragment();
	}
}
