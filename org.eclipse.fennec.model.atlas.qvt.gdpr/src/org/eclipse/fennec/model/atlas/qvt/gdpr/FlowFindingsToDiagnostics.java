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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.qvt.gdpr.RuleCatalogue.Rule;
import org.eclipse.fennec.model.gdprReport.CombinationFinding;
import org.eclipse.fennec.model.gdprReport.DataCategory;
import org.eclipse.fennec.model.gdprReport.Evaluation;
import org.eclipse.fennec.model.gdprReport.Evidence;
import org.eclipse.fennec.model.gdprReport.Finding;
import org.eclipse.fennec.model.gdprReport.FlowEvaluation;
import org.eclipse.fennec.model.gdprReport.GdprReport;
import org.eclipse.fennec.model.gdprReport.RelevanceLevelType;
import org.eclipse.fennec.model.gdprReport.TransformationSubject;

/**
 * Projects a transformation's report onto the <em>metamodels</em> it is about (issue #319, WP3).
 *
 * <p>
 * The report itself lands on the compiled unit, which is what it is a report of. But nobody
 * maintaining the contacts model will ever open a transformation's report, and the thing they most
 * need to know lives only there: that their harmless {@code comment} field receives three
 * classified fields of somebody else's model concatenated together. So half of what the report says
 * is written onto the models too, and which half goes where is
 * {@link RuleCatalogue.Reach the rule's reach}.
 * </p>
 *
 * <h2>The producer names the transformation</h2>
 * <p>
 * {@code gdpr.transformation/<qualifiedName>}, and not {@code gdpr.review}: a producer's roots are
 * replaced as a set, so writing under {@code gdpr.review} would erase the model's own review - the
 * write succeeds, the event fires, and the findings are simply gone. One producer for all
 * transformations has the same problem one level down, because a metamodel is read by many
 * transformations and the second to be analysed would replace the first one's findings on it.
 * <p>
 * Named per transformation, each owns its roots on every model it touches, and replacing,
 * re-analysing or withdrawing one never touches another's. The <em>revision</em> is deliberately
 * not in the producer but in {@code Diagnostic.source}: keyed by the unit fingerprint, every
 * recompile would strand the previous revision's findings on the model with nothing owning them
 * and nothing able to clear them.
 *
 * <h2>The shape</h2>
 *
 * <pre>
 * gdpr.transformation                                           WARNING   (no target)
 *   clinic2contacts: 4 findings on 3 elements
 * ├── gdpr.flow.aggregation       //Contact/comment             WARNING
 * │     SPECIAL_CATEGORY (HIGH)
 * │   └── gdpr.finding.SPECIAL_CATEGORY.HIGH
 * │         3 classified source features - //Patient/fullName, … - are combined into …
 * ├── gdpr.flow.structure-loss    //Contact/comment             WARNING
 * │     SPECIAL_CATEGORY (HIGH)
 * │   └── gdpr.finding.SPECIAL_CATEGORY.HIGH
 * │         //Contact/comment receives data the source review classifies as …
 * └── gdpr.flow.target-disagreement //Contact/reference         WARNING
 *       DIRECT_IDENTIFIER (HIGH)
 *     └── gdpr.finding.DIRECT_IDENTIFIER.HIGH
 *           The target review classifies //Contact/reference as PERSONAL_DATA, but …
 * </pre>
 *
 * <p>
 * One element node per {@code (rule, feature)} and one child per claim, because a field can be the
 * subject of several different statements and the rule is what a maintainer acts on. The grouping
 * is deliberately <em>not</em> per mapping: "this field of yours is flattened into free text" is
 * one statement however many mappings do it, and the mappings belong in the text rather than in the
 * count of badges. A diagnostic's id is derived from its code and its target, so a second node with
 * the same pair under one parent would silently replace the first - which is exactly why the
 * mapping is not part of the address.
 * </p>
 * <p>
 * Claims are folded per {@code (category, relevance)} within one element, the way a review's are.
 * They are deliberately not folded across elements: two rules reaching the same conclusion about
 * one field are two statements about it, not one said twice.
 * </p>
 */
final class FlowFindingsToDiagnostics {

	/** The producer family. The transformation's qualified name is the variable segment. */
	static final String PRODUCER_PREFIX = "gdpr.transformation/";

	/** Code of the one root per producer: what this transformation does to this model. */
	static final String CODE_TRANSFORMATION = "gdpr.transformation";

	/** Prefix of every claim code, so a client recognises one without knowing the categories. */
	static final String CODE_FINDING_PREFIX = "gdpr.finding.";

	/** The diagnostic category the whole GDPR family writes under. */
	static final String CATEGORY = "compliance";

	private static final Logger LOGGER = Logger.getLogger(FlowFindingsToDiagnostics.class.getName());

	private FlowFindingsToDiagnostics() {
	}

	/** The producer a report about this transformation owns on every model it touches. */
	static String producerOf(TransformationSubject subject) {
		return PRODUCER_PREFIX + subject.getQualifiedName();
	}

	/**
	 * The roots to write onto each metamodel the report is about.
	 *
	 * @param report a report whose subject is a {@code TransformationSubject}
	 * @return nsURI to the roots for that model, in the order the report states things. A model the
	 *         report names but has nothing to say about is present with an <b>empty</b> list, which
	 *         is what clears what a previous analysis of the same transformation left there
	 */
	static Map<String, List<Diagnostic>> map(GdprReport report) {
		if (!(report.getSubject() instanceof TransformationSubject subject)) {
			return Map.of();
		}
		// The revision, so a reader can see which compiled unit produced a finding without that
		// deciding who may replace it.
		String source = blankToNull(subject.getSubjectFingerprint());

		Map<String, Map<ElementKey, Element>> perModel = new LinkedHashMap<>();
		for (String nsURI : modelsOf(subject)) {
			perModel.put(nsURI, new LinkedHashMap<>());
		}
		for (Evaluation evaluation : report.getEvaluation()) {
			if (evaluation instanceof FlowEvaluation flow) {
				for (Finding finding : flow.getFindings()) {
					route(finding, List.of(flow), perModel);
				}
			}
		}
		for (CombinationFinding combination : report.getCombinations()) {
			route(combination, flowsOf(combination), perModel);
		}

		Map<String, List<Diagnostic>> roots = new LinkedHashMap<>();
		for (Map.Entry<String, Map<ElementKey, Element>> entry : perModel.entrySet()) {
			roots.put(entry.getKey(), rootsOf(subject, entry.getValue(), source));
		}
		return roots;
	}

	/* ------------------------------------------------------------------ routing */

	/**
	 * Sends one finding to the end or ends of its flows that it is a statement about.
	 * <p>
	 * A finding whose rule is unknown goes to both: a report may have been written by hand, and not
	 * knowing which end a statement belongs to is no reason to write it nowhere.
	 */
	private static void route(Finding finding, List<FlowEvaluation> flows,
			Map<String, Map<ElementKey, Element>> perModel) {
		if (assertsNothing(finding)) {
			return;
		}
		Rule rule = Rule.of(finding.getId());
		String code = rule == null ? codeOf(finding) : rule.code();
		boolean source = rule == null || rule.reach().touchesSource();
		boolean target = rule == null || rule.reach().touchesTarget();
		for (FlowEvaluation flow : flows) {
			if (source) {
				// What the SOURCE review said about this field, not what the finding concludes
				// about where the data ends up. A combination's category is the category of the
				// set - the strongest thing in it - and badging one contributing field with it
				// would say that field is special-category data when its own review says it is a
				// name. What travels is the field's own classification; what arrives is the set's.
				place(perModel, flow.getSourceNsURI(), code, blankToNull(flow.getSourceFeature()), finding,
						firstOf(categoryOf(flow), finding.getCategory()),
						firstOf(statedRelevanceOf(flow), finding.getRelevanceLevel()));
			}
			if (target) {
				place(perModel, flow.getTargetNsURI(), code, blankToNull(flow.getTargetFeature()), finding,
						finding.getCategory(), finding.getRelevanceLevel());
			}
		}
	}

	/**
	 * The strongest category the report itself records for one flow - which is the source review's
	 * classification of the field it reads, carried onto the flow when the report was derived.
	 * {@code null} when nothing classified it.
	 */
	private static DataCategory categoryOf(FlowEvaluation flow) {
		DataCategory strongest = null;
		for (Finding finding : flow.getFindings()) {
			strongest = ReviewIndex.stronger(strongest, finding.getCategory());
		}
		return strongest;
	}

	/**
	 * The relevance the flow states, or {@code null} where it states none.
	 * <p>
	 * An EMF enum attribute that was never set reads as its first literal rather than as
	 * {@code null}, and the first literal here is {@code NONE} - which is the report model's
	 * "examined and nothing of concern found". Taken at face value it would quietly downgrade a
	 * finding to "nothing to see" on the strength of a field nobody filled in.
	 */
	private static RelevanceLevelType statedRelevanceOf(FlowEvaluation flow) {
		RelevanceLevelType relevance = flow.getRelevanceLevel();
		return relevance == null || relevance == RelevanceLevelType.NONE ? null : relevance;
	}

	private static <T> T firstOf(T preferred, T fallback) {
		return preferred == null ? fallback : preferred;
	}

	/**
	 * The code of a finding this catalogue did not write. Its own id is the only thing that
	 * identifies it, so that is what the element node is keyed by - two hand-written findings with
	 * one id are one statement, which is the same rule the rest of this follows.
	 */
	private static String codeOf(Finding finding) {
		String id = blankToNull(finding.getId());
		return id == null ? CODE_TRANSFORMATION + ".finding" : CODE_TRANSFORMATION + "." + id;
	}

	private static void place(Map<String, Map<ElementKey, Element>> perModel, String nsURI, String code,
			String feature, Finding finding, DataCategory category, RelevanceLevelType relevance) {
		if (nsURI == null) {
			return;
		}
		Map<ElementKey, Element> elements = perModel.get(nsURI);
		if (elements == null) {
			// The report names a flow through a metamodel its own subject does not list. Nothing
			// can be done with it - there is no fingerprint to find that model by - and a report
			// that contradicts itself is worth a line rather than a silent drop.
			LOGGER.log(Level.WARNING, () -> String.format(
					"A flow of this transformation report touches '%s', which the report's subject does not list, "
							+ "so its findings cannot be written onto that model.",
					nsURI));
			return;
		}
		elements.computeIfAbsent(new ElementKey(code, feature), key -> new Element()).add(finding, category,
				relevance);
	}

	/** Every metamodel the subject names, source and target, each once. */
	private static Set<String> modelsOf(TransformationSubject subject) {
		Set<String> models = new LinkedHashSet<>();
		subject.getSourcePackages().stream().map(entry -> blankToNull(entry.getNsURI())).filter(ns -> ns != null)
				.forEach(models::add);
		subject.getTargetPackages().stream().map(entry -> blankToNull(entry.getNsURI())).filter(ns -> ns != null)
				.forEach(models::add);
		return models;
	}

	private static List<FlowEvaluation> flowsOf(CombinationFinding combination) {
		List<FlowEvaluation> flows = new ArrayList<>();
		for (Evaluation evaluation : combination.getFeatures()) {
			if (evaluation instanceof FlowEvaluation flow) {
				flows.add(flow);
			}
		}
		return flows;
	}

	/* ------------------------------------------------------------------ building the tree */

	private static List<Diagnostic> rootsOf(TransformationSubject subject, Map<ElementKey, Element> elements,
			String source) {
		if (elements.isEmpty()) {
			// An empty list, not an empty root: it is what clears what an earlier analysis of this
			// transformation left on the model, and a root with no children would read as a
			// transformation that was examined and found to do nothing.
			return List.of();
		}
		List<Diagnostic> nodes = new ArrayList<>(elements.size());
		DiagnosticSeverity worst = DiagnosticSeverity.INFO;
		int claims = 0;
		for (Map.Entry<ElementKey, Element> entry : elements.entrySet()) {
			Diagnostic node = entry.getValue().toDiagnostic(entry.getKey(), source);
			claims += node.getChildren().size();
			if (node.getSeverity().getValue() > worst.getValue()) {
				worst = node.getSeverity();
			}
			nodes.add(node);
		}
		Diagnostic root = diagnostic(CODE_TRANSFORMATION, null, worst,
				String.format("%s: %d finding%s on %d element%s", subject.getQualifiedName(), claims,
						claims == 1 ? "" : "s", nodes.size(), nodes.size() == 1 ? "" : "s"),
				source);
		root.getChildren().addAll(nodes);
		return List.of(root);
	}

	/**
	 * What one element node is: one rule's statement about one feature of one model.
	 * <p>
	 * A {@code null} feature addresses the model as a whole, which is right for a rule that speaks
	 * about a set rather than a field - {@code NOT_PROPAGATED} is the one. It cannot collide with
	 * anything, because the code is part of the key and each rule contributes at most one such
	 * node per model.
	 */
	private record ElementKey(String code, String feature) {
	}

	/** The claims gathered on one element, folded per {@code (category, relevance)}. */
	private static final class Element {

		private final Map<String, Claim> claims = new LinkedHashMap<>();

		void add(Finding finding, DataCategory category, RelevanceLevelType relevance) {
			claims.computeIfAbsent(findingCode(category, relevance), code -> new Claim(category, relevance))
					.add(finding);
		}

		Diagnostic toDiagnostic(ElementKey key, String source) {
			List<Diagnostic> children = new ArrayList<>(claims.size());
			List<String> headline = new ArrayList<>(claims.size());
			DiagnosticSeverity worst = DiagnosticSeverity.INFO;
			for (Map.Entry<String, Claim> entry : claims.entrySet()) {
				Claim claim = entry.getValue();
				DiagnosticSeverity severity = severityOf(claim.relevance);
				children.add(diagnostic(entry.getKey(), key.feature(), severity, claim.message(), source));
				headline.add(claim.label());
				if (severity.getValue() > worst.getValue()) {
					worst = severity;
				}
			}
			Diagnostic element = diagnostic(key.code(), key.feature(), worst, String.join(", ", headline), source);
			element.getChildren().addAll(children);
			return element;
		}
	}

	/**
	 * What one {@code (category, relevance)} pair on one element amounts to.
	 * <p>
	 * Deliberately not the review mapper's claim: that one folds several signals about one field
	 * into a single verdict and merges confidence and recommendations with it. Here the findings
	 * being folded came from different rules and each states something of its own, so what is kept
	 * is each rule's own sentence, in order, and the citations they carried over.
	 */
	private static final class Claim {

		private final DataCategory category;
		private final RelevanceLevelType relevance;
		private final List<String> rationales = new ArrayList<>();
		private final Set<String> citations = new LinkedHashSet<>();

		Claim(DataCategory category, RelevanceLevelType relevance) {
			this.category = category == null ? DataCategory.NOT_PERSONAL_DATA : category;
			this.relevance = relevance == null ? RelevanceLevelType.NONE : relevance;
		}

		void add(Finding finding) {
			String rationale = blankToNull(finding.getRationale());
			if (rationale != null && !rationales.contains(rationale)) {
				rationales.add(rationale);
			}
			for (Evidence evidence : finding.getEvidence()) {
				String citation = blankToNull(evidence.getCitationId());
				if (citation != null) {
					citations.add(citation);
				}
			}
		}

		String label() {
			return category.getName() + " (" + relevance.getName() + ")";
		}

		/** The report's own words, and what it quoted. Nothing here is composed. */
		String message() {
			StringBuilder message = new StringBuilder(rationales.isEmpty() ? label() : String.join(" ", rationales));
			if (!citations.isEmpty()) {
				message.append(" | Evidence: ").append(String.join(", ", citations));
			}
			return message.toString();
		}
	}

	/* ------------------------------------------------------------------ small helpers */

	static String findingCode(DataCategory category, RelevanceLevelType relevance) {
		return CODE_FINDING_PREFIX + (category == null ? DataCategory.NOT_PERSONAL_DATA : category).getName() + "."
				+ (relevance == null ? RelevanceLevelType.NONE : relevance).getName();
	}

	/**
	 * Whether the finding says there is nothing of concern. The same reading the review mapper
	 * uses: {@code NONE} relevance is the report model's "examined and nothing found", and
	 * {@code NOT_PERSONAL_DATA} and {@code ANONYMOUS} place the data outside the Regulation.
	 */
	private static boolean assertsNothing(Finding finding) {
		DataCategory category = finding.getCategory();
		return finding.getRelevanceLevel() == null || finding.getRelevanceLevel() == RelevanceLevelType.NONE
				|| category == null || category == DataCategory.NOT_PERSONAL_DATA
				|| category == DataCategory.ANONYMOUS;
	}

	/**
	 * How serious it is, in the vocabulary the object's metadata uses. Never {@code ERROR}: that
	 * means the check did not run, and an analysis that ran and found something is not a failure of
	 * the check. What did not run is said on the compiled unit, where the report is.
	 */
	private static DiagnosticSeverity severityOf(RelevanceLevelType relevance) {
		if (relevance == null) {
			return DiagnosticSeverity.INFO;
		}
		return switch (relevance) {
		case MEDIUM, HIGH -> DiagnosticSeverity.WARNING;
		case LOW, NONE -> DiagnosticSeverity.INFO;
		};
	}

	private static Diagnostic diagnostic(String code, String target, DiagnosticSeverity severity, String message,
			String source) {
		Diagnostic diagnostic = ManagementFactory.eINSTANCE.createDiagnostic();
		diagnostic.setCode(code);
		diagnostic.setTarget(target);
		diagnostic.setSeverity(severity);
		diagnostic.setMessage(message);
		diagnostic.setCategory(CATEGORY);
		if (source != null) {
			diagnostic.setSource(source);
		}
		return diagnostic;
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
