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
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.compliance.analysis.RequirementCitations;
import org.eclipse.fennec.model.atlas.compliance.analysis.ReviewIndex;
import org.eclipse.fennec.model.command.Command;
import org.eclipse.fennec.model.command.DeleteCommand;
import org.eclipse.fennec.model.command.UpdateCommand;
import org.eclipse.fennec.model.compliance.report.ComplianceReport;
import org.eclipse.fennec.model.compliance.report.ControlEvaluation;
import org.eclipse.fennec.model.compliance.report.ControlResult;
import org.eclipse.fennec.model.compliance.report.FeatureEvaluation;
import org.eclipse.fennec.model.compliance.report.Finding;
import org.eclipse.fennec.model.compliance.report.FlowEvaluation;
import org.eclipse.fennec.model.compliance.report.RelevanceLevel;
import org.eclipse.fennec.model.expression.Comparison;
import org.eclipse.fennec.model.expression.DurationLiteral;
import org.eclipse.fennec.model.expression.Expression;
import org.eclipse.fennec.model.expression.GuidLiteral;
import org.eclipse.fennec.model.expression.In;
import org.eclipse.fennec.model.expression.Literal;
import org.eclipse.fennec.model.expression.PropertyPath;
import org.eclipse.fennec.model.expression.StringLiteral;
import org.eclipse.fennec.model.expression.StringMatch;
import org.eclipse.fennec.model.expression.StringMatchKind;
import org.eclipse.fennec.model.expression.TemporalLiteral;
import org.eclipse.fennec.model.query.Expand;
import org.eclipse.fennec.model.query.GroupByStage;
import org.eclipse.fennec.model.query.GroupKey;
import org.eclipse.fennec.model.query.Query;
import org.eclipse.fennec.model.query.Stage;
import org.eclipse.fennec.model.query.TopStage;
import org.eclipse.fennec.model.query.analysis.FeatureRole;
import org.eclipse.fennec.model.query.analysis.FeatureUsage;
import org.eclipse.fennec.model.query.analysis.FeatureUsageAnalyzer;
import org.eclipse.fennec.model.stream.ChangeEntry;
import org.eclipse.fennec.model.stream.DeltaKind;

/**
 * A GDPR report for a catalogue of saved queries and commands, derived from the review of the
 * metamodel they are written against.
 *
 * <p>
 * The join is exact and nothing here matches a name: {@code PropertyPath.segments} is a list of
 * resolved {@link EStructuralFeature}s, so the fragment of a segment's URI <em>is</em> the
 * review's {@code FeatureEvaluation.uriFragment}. An update template joins just as exactly, by
 * the other route the models offer - {@code ChangeEntry.featureId} is the EMF feature id within
 * the selector's root type, which {@code ChangeTemplates} already resolves that way before it
 * applies anything.
 * </p>
 *
 * <p>
 * The walk is not written here either. {@code FeatureUsageAnalyzer}
 * ({@code org.eclipse.fennec.model.query.analysis}, issue #292 of fennec-persistence-jpa) reports
 * which features a query touches and in which role, and its {@code exposedFeatures} is documented
 * as <em>"the features whose values leave the store"</em>, explicitly excluding the filter and
 * sort paths because <em>"they decide which values are delivered, they are not delivered
 * themselves"</em>. That distinction was drawn upstream for backend reasons and it is exactly the
 * flow/control split a compliance report needs, so this analyser consumes it rather than
 * recomputing it.
 * </p>
 *
 * <p>
 * <b>One report per stored operation, and one more for the registry.</b> A compliance report is
 * about an artefact, and the artefact here is the single {@code Query} or {@code Command} a
 * catalogue entry holds - there is no object that is "the catalogue", because
 * {@code NamedOperations} stores one EObject per name and a Model Atlas registry keeps one object
 * per entry. {@link #analyse(EObject)} therefore produces a report whose subject names something
 * that exists.
 * </p>
 *
 * <p>
 * {@code ERASURE_PATH_ABSENT} is the exception and is why {@link #coverage} exists: <em>no entry
 * of this registry erases Feedback</em> is a statement about the set, and an absence cannot be
 * seen from inside any one artefact. Its report says so in its own subject, which names the
 * registry rather than an artefact, and in its name, which lists the entries it was derived from.
 * </p>
 */
public final class CatalogAnalysis {

	/** The rule catalogue this report was produced by, versioned into {@code generatedBy}. */
	public static final String CATALOGUE = "query-analysis/1";

	/** Roles whose values leave the store, as {@code FeatureUsage.exposedFeatures} defines them. */
	private static final FeatureRole[] OUTPUT = { FeatureRole.PROJECTION, FeatureRole.GROUP,
			FeatureRole.AGGREGATE, FeatureRole.COMPUTE };

	private final ReviewIndex review;
	private final RequirementCitations citations;
	private final EPackage source;
	private final String fingerprint;

	public CatalogAnalysis(ReviewIndex review, RequirementCitations citations, EPackage source, String fingerprint) {
		this.review = review;
		this.citations = citations;
		this.source = source;
		this.fingerprint = fingerprint;
	}

	/**
	 * The report for one stored operation - one entry of a named-operation catalogue.
	 * <p>
	 * A report with no evaluation at all is a legitimate outcome and is not an error: an erasure
	 * command that addresses its subject by a parameter does nothing this rule table has anything
	 * to say about. The disclaimer carries the caveat that belongs with it - a rule nobody wrote
	 * down never fires, so an absence of findings is not a statement that there is nothing to
	 * find.
	 *
	 * @param operation the stored {@code Query} or {@code Command}
	 */
	public ComplianceReport analyse(EObject operation) {
		String name = nameOf(operation);
		ComplianceReport report = StaticReport.about(CATALOGUE, "GDPR analysis of the saved operation " + name, name,
				"fennec-query", source, fingerprint, review);
		analyse(report, operation);
		return report;
	}

	/**
	 * The report for a whole registry of named operations, carrying only the statements that
	 * cannot be made about a single entry.
	 * <p>
	 * Its subject names the registry and not an artefact, because there is no artefact: the
	 * entries are separate objects and nothing stores them together. That is what the report has
	 * to be read as, so its name lists the entries the coverage was computed from.
	 *
	 * @param registryName the registry whose entries these are
	 * @param operations   every stored operation the registry holds
	 */
	public ComplianceReport coverage(String registryName, List<EObject> operations) {
		List<String> names = operations.stream().map(CatalogAnalysis::nameOf).toList();
		ComplianceReport report = StaticReport.about(CATALOGUE,
				"GDPR erasure coverage of the " + operations.size() + " saved operations held in the "
						+ registryName + " registry: " + String.join(", ", names),
				registryName, "fennec-query", source, fingerprint, review);
		erasureCoverage(report, operations);
		return report;
	}

	private void analyse(ComplianceReport report, EObject operation) {
		String name = nameOf(operation);
		if (hasUnresolvedReference(operation)) {
			opaque(report, name, "it references a metamodel element that does not resolve in the reviewed package");
			return;
		}
		Query query = selectorOf(operation);
		if (query == null || query.getFrom() == null) {
			opaque(report, name, "it carries no query selector, so there is nothing to join to a review");
			return;
		}
		FeatureUsage usage = FeatureUsageAnalyzer.analyze(query);
		boolean isCommand = operation instanceof Command;

		if (!isCommand) {
			// A command selector has no result shape: it matches the rows to delete or to patch,
			// and nothing is handed to a caller. Running the delivery rules over it would report
			// an erasure command as the widest disclosure in the catalogue, which is backwards.
			unnamedDelivery(report, name, query, usage);
			projects(report, name, usage);
			unboundedResult(report, name, query, usage);
			filtersOn(report, name, usage);
		}
		fuzzyIdentitySearch(report, name, query);
		embeddedPersonalLiteral(report, name, query);
		reidentifyingGroup(report, name, query);
		unboundedExpand(report, name, query);
		if (operation instanceof UpdateCommand update) {
			templateWritesIdentifier(report, name, update);
		}
	}

	/* ------------------------------------------------------------------------------- the rules */

	/**
	 * {@code UNNAMED_DELIVERY}: the query names no output, so it hands out every feature of its
	 * root type and of that type's containment closure - including ones it never mentions and
	 * whose classification therefore never crossed the author's mind. The widest flow a query
	 * language has, and the default shape of an object query.
	 */
	private void unnamedDelivery(ComplianceReport report, String name, Query query, FeatureUsage usage) {
		if (!usage.deliversObjects() || !query.getSelect().isEmpty()) {
			return;
		}
		List<EStructuralFeature> classified = classifiedOf(usage.deliveredFeatures());
		if (classified.isEmpty()) {
			return;
		}
		FlowEvaluation evaluation = flow(report, "UNNAMED_DELIVERY:" + name,
				name + " names no output and so delivers " + classified.size() + " classified features");
		for (EStructuralFeature feature : classified) {
			FeatureEvaluation reviewed = review.feature(fragmentOf(feature));
			boolean viaContainment = feature.getEContainingClass() != query.getFrom();
			Finding finding = StaticReport.carried("UNNAMED_DELIVERY:" + name + ":" + fragmentOf(feature),
					String.format(
							"'%s' selects no feature, so every feature of %s is delivered, %s among them, "
									+ "classified %s%s. The query never names it, so nothing in the query says "
									+ "that this value leaves the store.",
							name, query.getFrom().getName(), fragmentOf(feature),
							ReviewIndex.worstCategory(reviewed),
							viaContainment ? " - reached through the containment closure of "
									+ query.getFrom().getName() : ""),
					relevanceOf(reviewed), reviewed, ReviewIndex.worstCategory(reviewed));
			if (finding != null) {
				evaluation.getFindings().add(finding);
			}
		}
		evaluation.setRelevanceLevel(worstOf(evaluation));
	}

	/**
	 * {@code PROJECTS_IDENTIFIER} and {@code PROJECTS_SPECIAL_CATEGORY}: a feature the query
	 * explicitly projects, groups by, aggregates or computes with. The sharpest flow there is -
	 * the value leaves the store because the artefact says so, by name.
	 */
	private void projects(ComplianceReport report, String name, FeatureUsage usage) {
		for (String category : List.of("SPECIAL_CATEGORY", "DIRECT_IDENTIFIER")) {
			List<EStructuralFeature> hits = new ArrayList<>();
			for (EStructuralFeature feature : usage.features(OUTPUT)) {
				if (category.equals(ReviewIndex.worstCategory(review.feature(fragmentOf(feature))))) {
					hits.add(feature);
				}
			}
			if (hits.isEmpty()) {
				continue;
			}
			String rule = "SPECIAL_CATEGORY".equals(category) ? "PROJECTS_SPECIAL_CATEGORY" : "PROJECTS_IDENTIFIER";
			FlowEvaluation evaluation = flow(report, rule + ":" + name,
					name + " projects " + hits.size() + " features classified " + category);
			for (EStructuralFeature feature : hits) {
				FeatureEvaluation reviewed = review.feature(fragmentOf(feature));
				Finding finding = StaticReport.carried(rule + ":" + name + ":" + fragmentOf(feature),
						String.format(
								"'%s' names %s as an output of the query. The review classifies it %s, so this "
										+ "saved operation is a standing channel by which that value leaves the "
										+ "store.",
								name, fragmentOf(feature), category),
						relevanceOf(reviewed), reviewed, category);
				if (finding != null) {
					evaluation.getFindings().add(finding);
				}
			}
			evaluation.setRelevanceLevel(worstOf(evaluation));
		}
	}

	/**
	 * {@code UNBOUNDED_RESULT}: classified values leave the store and nothing limits how many.
	 * An aggregation is left out - its rows are groups, not data subjects, and
	 * {@code REIDENTIFYING_GROUP} is the statement that fits it.
	 */
	private void unboundedResult(ComplianceReport report, String name, Query query, FeatureUsage usage) {
		if (classifiedOf(usage.exposedFeatures()).isEmpty() || groupBy(query) != null) {
			return;
		}
		if (query.getTop() > 0 || query.isCountOnly() || stages(query, TopStage.class).size() > 0) {
			return;
		}
		Finding finding = StaticReport.control("UNBOUNDED_RESULT:" + name,
				String.format(
						"'%s' exposes classified features and sets neither top nor countOnly, and no stage of "
								+ "its pipeline bounds the result. Every row the predicate matches is returned, "
								+ "whatever the data grew to since the query was catalogued.",
						name),
				"Set top, or countOnly where the caller needs a number rather than the records.",
				RelevanceLevel.MEDIUM, citations, "Art.5(1)(c)", review.language(), null);
		if (finding != null) {
			control(report, "UNBOUNDED_RESULT:" + name, name + " returns an unbounded result",
					"Art.5(1)(c)", ControlResult.NOT_SATISFIED).getFindings().add(finding);
		}
	}

	/**
	 * {@code FILTERS_ON_IDENTIFIER} / {@code FILTERS_ON_SPECIAL_CATEGORY}: which data subjects a
	 * caller reaches is decided by a category. A control statement, not a flow - nothing travels,
	 * the selection itself is the question. Command selectors are excluded: an erasure or
	 * housekeeping command has to address its subject, and reporting that would bury the rule in
	 * noise.
	 */
	private void filtersOn(ComplianceReport report, String name, FeatureUsage usage) {
		for (EStructuralFeature feature : usage.features(FeatureRole.FILTER)) {
			FeatureEvaluation reviewed = review.feature(fragmentOf(feature));
			String category = ReviewIndex.worstCategory(reviewed);
			if (!"DIRECT_IDENTIFIER".equals(category) && !"SPECIAL_CATEGORY".equals(category)) {
				continue;
			}
			String rule = "SPECIAL_CATEGORY".equals(category) ? "FILTERS_ON_SPECIAL_CATEGORY"
					: "FILTERS_ON_IDENTIFIER";
			String requirement = "SPECIAL_CATEGORY".equals(category) ? "Art.9(2)" : "Art.5(1)(b)";
			Finding finding = StaticReport.control(rule + ":" + name + ":" + fragmentOf(feature),
					String.format(
							"'%s' selects the rows it returns by %s, classified %s. Which data subjects a "
									+ "caller of this saved query can reach is therefore decided by that category.",
							name, fragmentOf(feature), category),
					"Filter on a surrogate the caller already holds rather than on the identifier itself.",
					RelevanceLevel.MEDIUM, citations, requirement, review.language(),
					ReviewIndex.refFor(reviewed, category));
			if (finding != null) {
				control(report, rule + ":" + name + ":" + fragmentOf(feature),
						name + " selects by " + fragmentOf(feature), requirement, ControlResult.NOT_SATISFIED)
						.getFindings().add(finding);
			}
		}
	}

	/**
	 * {@code FUZZY_IDENTITY_SEARCH}: a substring, wildcard or fuzzy match against a direct
	 * identifier. An exact lookup reaches the subject the caller already knows; a fuzzy one
	 * reaches the subjects they do not.
	 */
	private void fuzzyIdentitySearch(ComplianceReport report, String name, Query query) {
		for (EObject node : walk(query)) {
			if (!(node instanceof StringMatch match) || match.getKind() == StringMatchKind.STARTS_WITH
					|| match.getKind() == StringMatchKind.ENDS_WITH) {
				continue;
			}
			EStructuralFeature feature = addressedBy(match.getSource());
			FeatureEvaluation reviewed = feature == null ? null : review.feature(fragmentOf(feature));
			if (!"DIRECT_IDENTIFIER".equals(ReviewIndex.worstCategory(reviewed))) {
				continue;
			}
			Finding finding = StaticReport.control("FUZZY_IDENTITY_SEARCH:" + name,
					String.format(
							"'%s' matches %s with %s%s. A partial match over a direct identifier returns the "
									+ "records of data subjects the caller did not name, which is a different "
									+ "operation from looking up the one they did.",
							name, fragmentOf(feature), match.getKind(),
							match.isCaseInsensitive() ? ", case insensitively" : ""),
					"Match the identifier exactly, and let the caller disambiguate from what they already hold.",
					RelevanceLevel.HIGH, citations, "Art.5(1)(c)", review.language(),
					ReviewIndex.refFor(reviewed, "DIRECT_IDENTIFIER"));
			if (finding != null) {
				control(report, "FUZZY_IDENTITY_SEARCH:" + name, name + " searches " + fragmentOf(feature)
						+ " by partial match", "Art.5(1)(c)", ControlResult.NOT_SATISFIED).getFindings().add(finding);
			}
		}
	}

	/**
	 * {@code EMBEDDED_PERSONAL_LITERAL}: a saved query compares a classified feature against a
	 * literal rather than against a parameter. The value is then a personal datum stored in a
	 * second place, with its own lifetime, covered by no retention rule and listed in no record of
	 * processing - and the model already has the compliant form.
	 */
	private void embeddedPersonalLiteral(ComplianceReport report, String name, Query query) {
		for (EObject node : walk(query)) {
			List<Expression> operands = operandsOf(node);
			if (operands.isEmpty()) {
				continue;
			}
			EStructuralFeature feature = null;
			Literal literal = null;
			for (Expression operand : operands) {
				if (operand instanceof PropertyPath path) {
					feature = lastOf(path);
				} else if (isTextual(operand)) {
					literal = (Literal) operand;
				}
			}
			FeatureEvaluation reviewed = feature == null ? null : review.feature(fragmentOf(feature));
			String category = ReviewIndex.worstCategory(reviewed);
			if (literal == null || category == null) {
				continue;
			}
			Finding finding = StaticReport.control("EMBEDDED_PERSONAL_LITERAL:" + name + ":" + fragmentOf(feature),
					String.format(
							"'%s' is a stored query and it compares %s, classified %s, against the literal "
									+ "value '%s'. That value is personal data held in a second place: it has its "
									+ "own lifetime, no retention rule covers it, and no record of processing "
									+ "lists it.",
							name, fragmentOf(feature), category, textOf(literal)),
					"Declare a parameter and compare against a ParameterRef, which the query model already has.",
					RelevanceLevel.HIGH, citations, "Art.5(1)(e)", review.language(),
					ReviewIndex.refFor(reviewed, category));
			if (finding != null) {
				control(report, "EMBEDDED_PERSONAL_LITERAL:" + name + ":" + fragmentOf(feature),
						name + " stores a personal datum as a literal", "Art.5(1)(e)", ControlResult.NOT_SATISFIED)
						.getFindings().add(finding);
			}
		}
	}

	/**
	 * {@code REIDENTIFYING_GROUP}: an aggregation grouped by two or more quasi-identifiers. The
	 * result names nobody and a group of one is a person, which is the oldest re-identification
	 * result there is and the one a query language can be read for statically.
	 */
	private void reidentifyingGroup(ComplianceReport report, String name, Query query) {
		GroupByStage group = groupBy(query);
		if (group == null) {
			return;
		}
		List<EStructuralFeature> keys = new ArrayList<>();
		for (PropertyPath path : group.getPaths()) {
			keys.add(lastOf(path));
		}
		for (GroupKey key : group.getKeys()) {
			EStructuralFeature addressed = addressedBy(key.getExpression());
			if (addressed != null) {
				keys.add(addressed);
			}
		}
		List<String> quasi = new ArrayList<>();
		for (EStructuralFeature key : keys) {
			if ("QUASI_IDENTIFIER".equals(ReviewIndex.worstCategory(review.feature(fragmentOf(key))))) {
				quasi.add(fragmentOf(key));
			}
		}
		if (quasi.size() < 2) {
			return;
		}
		Finding finding = StaticReport.control("REIDENTIFYING_GROUP:" + name,
				String.format(
						"'%s' groups by %s, all of them classified QUASI_IDENTIFIER, and reports a count per "
								+ "group. A group of one is one person, and nothing in the query suppresses "
								+ "small groups. The model itself knows the measure - AuslastungFenster and "
								+ "Tagesbesucher both carry an 'unterdrueckt' flag - and this aggregation does "
								+ "not use it.",
						name, String.join(", ", quasi)),
				"Suppress groups below a threshold, or coarsen the keys until no group can be a single person.",
				RelevanceLevel.HIGH, citations, "Art.5(1)(c)", review.language(), null);
		if (finding != null) {
			control(report, "REIDENTIFYING_GROUP:" + name, name + " groups by " + quasi.size() + " quasi-identifiers",
					"Art.5(1)(c)", ControlResult.NOT_SATISFIED).getFindings().add(finding);
		}
	}

	/**
	 * {@code UNBOUNDED_EXPAND}: an expansion into a classified branch with neither a filter nor a
	 * window. Whatever hangs off the matched object comes with it.
	 */
	private void unboundedExpand(ComplianceReport report, String name, Query query) {
		for (EObject node : walk(query)) {
			if (!(node instanceof Expand expand) || expand.getFilter() != null || expand.getTop() > 0) {
				continue;
			}
			EStructuralFeature into = expand.getPath() == null ? null : lastOf(expand.getPath());
			if (into == null || !(into.getEType() instanceof EClass target) || !classifiesAnythingOf(target)) {
				continue;
			}
			Finding finding = StaticReport.control("UNBOUNDED_EXPAND:" + name + ":" + fragmentOf(into),
					String.format(
							"'%s' expands %s into %s without a filter and without a window. Every %s hanging "
									+ "off a matched object is returned, and the review classifies features of "
									+ "that type.",
							name, fragmentOf(into), target.getName(), target.getName()),
					"Filter or window the expansion, or drop it and let the caller fetch what they need.",
					RelevanceLevel.MEDIUM, citations, "Art.5(1)(c)", review.language(), null);
			if (finding != null) {
				control(report, "UNBOUNDED_EXPAND:" + name + ":" + fragmentOf(into),
						name + " expands " + fragmentOf(into) + " unboundedly", "Art.5(1)(c)",
						ControlResult.NOT_SATISFIED).getFindings().add(finding);
			}
		}
	}

	/**
	 * {@code TEMPLATE_WRITES_IDENTIFIER}: an update template that overwrites a direct identifier.
	 * This may be the breach or it may be the measure - overwriting a name with a constant is
	 * pseudonymisation - and which it is depends on an inventory no runtime here holds. So the
	 * entries are named and the result is {@code NOT_ASSESSED}: a check that could not run must
	 * never read as one that passed, and it must never read as a failure either.
	 */
	private void templateWritesIdentifier(ComplianceReport report, String name, UpdateCommand update) {
		if (update.getTemplate() == null || update.getSelector() == null) {
			return;
		}
		EClass type = update.getSelector().getFrom();
		List<String> written = new ArrayList<>();
		for (ChangeEntry entry : update.getTemplate().getEntries()) {
			if (entry.getKind() != DeltaKind.SET && entry.getKind() != DeltaKind.UNSET) {
				continue;
			}
			EStructuralFeature feature = type.getEStructuralFeature(entry.getFeatureId());
			if (feature == null) {
				opaque(report, name, "its template addresses feature id " + entry.getFeatureId() + ", which "
						+ type.getName() + " does not have");
				continue;
			}
			if ("DIRECT_IDENTIFIER".equals(ReviewIndex.worstCategory(review.feature(fragmentOf(feature))))) {
				written.add(fragmentOf(feature));
			}
		}
		if (written.isEmpty()) {
			return;
		}
		ControlEvaluation evaluation = control(report, "TEMPLATE_WRITES_IDENTIFIER:" + name,
				name + " overwrites " + written.size() + " direct identifiers", "Art.32(1)(a)",
				ControlResult.NOT_ASSESSED);
		Finding finding = StaticReport.control("TEMPLATE_WRITES_IDENTIFIER:" + name,
				String.format(
						"'%s' sets %s, which the review classifies DIRECT_IDENTIFIER, on every %s its selector "
								+ "matches. Overwriting an identifier is how pseudonymisation is performed, so "
								+ "this is either the measure or its opposite. Deciding that needs the measure "
								+ "inventory, and no registry in this runtime holds one, so the control is "
								+ "recorded as NOT_ASSESSED rather than guessed at.",
						name, String.join(", ", written), type.getName()),
				"Record this command as a Measure in the inventory, so the control can be answered rather than "
						+ "left open.",
				RelevanceLevel.MEDIUM, citations, "Art.32(1)(a)", review.language(), null);
		if (finding != null) {
			evaluation.getFindings().add(finding);
		}
	}

	/**
	 * {@code ERASURE_PATH_ABSENT}: for every classifier holding classified data, whether the
	 * catalogue contains a delete command that addresses it.
	 * <p>
	 * It is the one rule here that fires on an <em>absence</em>, so it is also the one that must
	 * write when it finds nothing wrong: a satisfied control is recorded as explicitly as an
	 * unsatisfied one, because silence and a pass must never look the same in a compliance record.
	 */
	private void erasureCoverage(ComplianceReport report, List<EObject> operations) {
		Set<EClass> erasable = new LinkedHashSet<>();
		Map<EClass, String> erasedBy = new LinkedHashMap<>();
		for (EObject operation : operations) {
			if (operation instanceof DeleteCommand delete && delete.getSelector() != null
					&& delete.getSelector().getFrom() != null) {
				erasable.add(delete.getSelector().getFrom());
				erasedBy.put(delete.getSelector().getFrom(), nameOf(operation));
			}
		}
		for (EClassifier classifier : source.getEClassifiers()) {
			if (!(classifier instanceof EClass type) || !classifiesAnythingOf(type) || isContained(type)) {
				continue;
			}
			EClass covering = erasable.stream()
					.filter(candidate -> candidate == type || type.getEAllSuperTypes().contains(candidate))
					.findFirst().orElse(null);
			if (covering != null) {
				ControlEvaluation satisfied = control(report, "ERASURE_PATH_ABSENT:" + type.getName(),
						"the catalogue can erase " + type.getName(), "Art.5(1)(e)", ControlResult.SATISFIED);
				satisfied.getMeasureIds().add(erasedBy.get(covering));
				continue;
			}
			Finding finding = StaticReport.control("ERASURE_PATH_ABSENT:" + type.getName(),
					String.format(
							"The review classifies features of %s as personal data, and this catalogue holds no "
									+ "delete command that addresses %s. The catalogue is not the only way to "
									+ "erase, so this is a statement about the catalogue and not about the "
									+ "system - but the operations a deployment keeps are the operations it "
									+ "can be asked to perform.",
							type.getName(), type.getName()),
					"Catalogue a DeleteCommand for " + type.getName() + ", or record where erasure happens instead.",
					RelevanceLevel.HIGH, citations, "Art.5(1)(e)", review.language(), null);
			if (finding != null) {
				control(report, "ERASURE_PATH_ABSENT:" + type.getName(),
						"no catalogued erasure path for " + type.getName(), "Art.5(1)(e)",
						ControlResult.NOT_SATISFIED).getFindings().add(finding);
			}
		}
	}

	/** {@code OPAQUE_REFERENCE}: an ERROR, because silence would read as clean. */
	private void opaque(ComplianceReport report, String name, String why) {
		flow(report, "OPAQUE_REFERENCE:" + name,
				String.format("'%s' could not be analysed: %s. Nothing is asserted about it.", name, why));
	}

	/* ------------------------------------------------------------------------------- the walk */

	/** The query a named operation is, or the selector a command carries. */
	private static Query selectorOf(EObject operation) {
		if (operation instanceof Query query) {
			return query;
		}
		if (operation instanceof DeleteCommand delete) {
			return delete.getSelector();
		}
		if (operation instanceof UpdateCommand update) {
			return update.getSelector();
		}
		return null;
	}

	private static String nameOf(EObject operation) {
		if (operation instanceof Query query) {
			return query.getName();
		}
		if (operation instanceof Command command) {
			return command.getName();
		}
		return operation.eClass().getName();
	}

	/**
	 * A stored query is XMI with nsURI hrefs, so an absent model package turns every segment into
	 * a proxy and every rule into silence. This is the guard that makes that case loud.
	 */
	private static boolean hasUnresolvedReference(EObject operation) {
		for (EObject node : walk(operation)) {
			for (EObject referenced : node.eCrossReferences()) {
				if (referenced.eIsProxy()) {
					return true;
				}
			}
		}
		return false;
	}

	/** The feature an expression addresses, when it is a path at all. */
	private static EStructuralFeature addressedBy(Expression expression) {
		return expression instanceof PropertyPath path ? lastOf(path) : null;
	}

	/** The feature a path addresses: its last segment, the earlier ones being navigation. */
	private static EStructuralFeature lastOf(PropertyPath path) {
		return path.getSegments().isEmpty() ? null : path.getSegments().get(path.getSegments().size() - 1);
	}

	/** The two sides of a construct that compares something to something. */
	private static List<Expression> operandsOf(EObject node) {
		if (node instanceof Comparison comparison) {
			return List.of(comparison.getLeft(), comparison.getRight());
		}
		if (node instanceof StringMatch match) {
			return List.of(match.getSource(), match.getPattern());
		}
		if (node instanceof In in) {
			List<Expression> operands = new ArrayList<>();
			operands.add(in.getSource());
			operands.addAll(in.getValues());
			return operands;
		}
		return List.of();
	}

	/** Literal kinds that can hold a personal datum; a number or a boolean cannot on its own. */
	private static boolean isTextual(Expression expression) {
		return expression instanceof StringLiteral || expression instanceof GuidLiteral
				|| expression instanceof TemporalLiteral || expression instanceof DurationLiteral;
	}

	private static String textOf(Literal literal) {
		if (literal instanceof StringLiteral string) {
			return string.getValue();
		}
		if (literal instanceof GuidLiteral guid) {
			return guid.getValue();
		}
		if (literal instanceof TemporalLiteral temporal) {
			return temporal.getValue();
		}
		return ((DurationLiteral) literal).getIso8601();
	}

	private static GroupByStage groupBy(Query query) {
		List<GroupByStage> found = stages(query, GroupByStage.class);
		return found.isEmpty() ? null : found.get(0);
	}

	private static <S extends Stage> List<S> stages(Query query, Class<S> kind) {
		List<S> found = new ArrayList<>();
		if (query.getApply() != null) {
			for (Stage stage : query.getApply().getStages()) {
				if (kind.isInstance(stage)) {
					found.add(kind.cast(stage));
				}
			}
		}
		return found;
	}

	private static List<EObject> walk(EObject root) {
		List<EObject> all = new ArrayList<>();
		all.add(root);
		root.eAllContents().forEachRemaining(all::add);
		return all;
	}

	/* ----------------------------------------------------------------------------- the review */

	private List<EStructuralFeature> classifiedOf(Set<EStructuralFeature> features) {
		List<EStructuralFeature> classified = new ArrayList<>();
		for (EStructuralFeature feature : features) {
			if (ReviewIndex.worstCategory(review.feature(fragmentOf(feature))) != null) {
				classified.add(feature);
			}
		}
		return classified;
	}

	/**
	 * Whether the review put a data category on any feature of this classifier.
	 * <p>
	 * The test has to be on the category and not on the presence of a {@code FeatureEvaluation}:
	 * a review evaluates <em>every</em> feature of every classifier, and most of those
	 * evaluations say that there is nothing to say. Asking only whether the review mentions the
	 * type would make every classifier in the package look like personal data, and the erasure
	 * coverage would then report gaps for the opening hours and the price list.
	 */
	private boolean classifiesAnythingOf(EClassifier classifier) {
		String prefix = "//" + classifier.getName() + "/";
		return review.fragments().stream().filter(fragment -> fragment.startsWith(prefix))
				.anyMatch(fragment -> ReviewIndex.worstCategory(review.feature(fragment)) != null);
	}

	/**
	 * Whether anything in the package contains this type. A contained type is erased with its
	 * container, so asking for an erasure path of its own would report a gap that is not one.
	 */
	private boolean isContained(EClass type) {
		for (EClassifier classifier : source.getEClassifiers()) {
			if (!(classifier instanceof EClass candidate)) {
				continue;
			}
			for (EReference reference : candidate.getEReferences()) {
				if (reference.isContainment() && reference.getEType() == type) {
					return true;
				}
			}
		}
		return false;
	}

	private RelevanceLevel relevanceOf(FeatureEvaluation reviewed) {
		return reviewed == null || reviewed.getRelevanceLevel() == null ? RelevanceLevel.MEDIUM
				: reviewed.getRelevanceLevel();
	}

	private static RelevanceLevel worstOf(FlowEvaluation evaluation) {
		RelevanceLevel worst = RelevanceLevel.NONE;
		for (Finding finding : evaluation.getFindings()) {
			worst = ReviewIndex.higher(worst, finding.getRelevanceLevel());
		}
		return worst;
	}

	/** The review's own key for a feature. */
	private static String fragmentOf(EStructuralFeature feature) {
		return EcoreUtil.getURI(feature).fragment();
	}

	/* ------------------------------------------------------------------- the report's own shape */

	private static FlowEvaluation flow(ComplianceReport report, String id, String name) {
		return StaticReport.flow(report, id, name);
	}

	/**
	 * A control evaluation that carries an id and a name, which the report model allows on every
	 * evaluation and which is what makes the file readable by a person rather than only by a
	 * consumer that re-reads the rule table.
	 */
	private ControlEvaluation control(ComplianceReport report, String id, String name, String requirementId,
			ControlResult result) {
		ControlEvaluation evaluation = StaticReport.control(report, "gdpr", requirementId, result);
		evaluation.setId(id);
		evaluation.setName(name);
		return evaluation;
	}
}
