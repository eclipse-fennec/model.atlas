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
package org.eclipse.fennec.model.atlas.gdpr.diagnostics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.gdprReport.ClassifierEvaluation;
import org.eclipse.fennec.model.gdprReport.CombinationFinding;
import org.eclipse.fennec.model.gdprReport.ConfidenceType;
import org.eclipse.fennec.model.gdprReport.DataCategory;
import org.eclipse.fennec.model.gdprReport.Evaluation;
import org.eclipse.fennec.model.gdprReport.Evidence;
import org.eclipse.fennec.model.gdprReport.FeatureEvaluation;
import org.eclipse.fennec.model.gdprReport.Finding;
import org.eclipse.fennec.model.gdprReport.FlowEvaluation;
import org.eclipse.fennec.model.gdprReport.GdprReport;
import org.eclipse.fennec.model.gdprReport.PackageSubject;
import org.eclipse.fennec.model.gdprReport.RelevanceLevelType;
import org.eclipse.fennec.model.gdprReport.TransformationSubject;

/**
 * Turns the findings of one {@link GdprReport} into the {@link Diagnostic} trees that are written
 * onto the metadata of the model the review is about.
 * <p>
 * A pure function of the report, with no OSGi and no storage, so the rules below are unit-testable
 * on their own. It <b>decides nothing the review did not</b>: every diagnostic is a projection of a
 * {@code Finding} that already exists, and the mapping is a table rather than a second opinion.
 *
 * <h2>Shape: one root for the producer, one node per element, one leaf per claim</h2>
 *
 * <pre>
 * gdpr.review                  the review of this object      &lt;- the only root
 *   gdpr.feature //Person/dob  what was found on that element
 *     gdpr.finding.QUASI_IDENTIFIER.HIGH   one claim about it
 * </pre>
 * <p>
 * <b>One root, because an object has several producers.</b> A reader looking at an object's
 * diagnostics - in a browser, a tree, a listing - wants one collapsed row per producer and the
 * choice of which to open. Spreading a producer's findings over many roots buries the other
 * producers among them. So everything this producer has to say hangs under a single node, whose
 * severity is the worst anywhere beneath it and whose message says how much there is.
 * <p>
 * Each node under it stands for an element the review examined and found something on - a
 * classifier, a feature, a flow, or the set of evaluations a {@code CombinationFinding} ties
 * together - and each leaf under that is one claim about that element. An element the review
 * examined and found nothing on contributes nothing, and a review that found nothing produces no
 * root at all: the absence of a {@code gdpr.review} root is what says nothing was found.
 * <p>
 * The producer is deliberately not set here: {@code Diagnostics.prepare} stamps {@link #PRODUCER}
 * onto every node of the tree and mints the ids from it, so this class never has to know the id
 * rule. Ids are derived, never drawn, which is what lets a report's reader compute the id of a
 * finding without it being stored back on the report.
 *
 * <h2>Identity: one child per (category, relevance) on one element</h2>
 * <p>
 * A child's {@code code} is {@link #findingCode(DataCategory, RelevanceLevelType)} and its
 * {@code target} is the element's, so its id turns exactly on <b>element, category and
 * relevance</b>. That is the rule the re-review has to honour: a second review that reaches the
 * same category at the same relevance by another route is the <em>same</em> claim, so it keeps its
 * id and with it whatever a person decided about it. A second review that says
 * {@code SPECIAL_CATEGORY} where the first said {@code PERSONAL_DATA}, or that raises the relevance,
 * is a <em>different</em> claim: it gets a new id, is {@code OPEN}, and the old one disappears when
 * the producer's roots are replaced.
 * <p>
 * Consequently <b>several findings that share an element, a category and a relevance fold into one
 * child</b>, their rationales, recommendations and citations merged. They differ in how the review
 * reached the claim, not in what it asserts, and there is only one decision for a person to make
 * about it. {@code Finding.id} deliberately plays no part - the analyser assigns it afresh on every
 * run ({@code F-001} in one review, {@code f1} in the next for the same claim), so keying on it
 * would make every diagnostic look new every time. Neither does {@code detectedBy}, which is
 * optional and many-valued: one extra recorded signal must not move the key.
 *
 * <h2>What is written, and at which severity</h2>
 * <p>
 * A finding that <b>asserts nothing</b> is skipped: {@code relevanceLevel} {@code NONE}, or a
 * category of {@code NOT_PERSONAL_DATA} or {@code ANONYMOUS}. Both are the report model's way of
 * saying "examined and nothing of concern found", and the report already says that by carrying an
 * evaluation with no findings at all. Note that an EMF enum attribute nobody set reads as its first
 * literal, which is {@code NOT_PERSONAL_DATA} and {@code NONE} respectively, and that XMI does not
 * write a value equal to the default - so a finding that <em>stated</em> it is clean and one that
 * left both fields empty are indistinguishable once stored, and both are skipped.
 * <p>
 * The severity vocabulary is {@code INFO}, {@code WARNING}, {@code ERROR}, and <b>a review never
 * produces {@code ERROR}</b>: the report's own disclaimer says it flags features needing human
 * review and must not assert compliance or non-compliance, so an {@code ERROR} - which means the
 * check did not run - would misstate what it is. {@code LOW} is information, {@code MEDIUM} and
 * {@code HIGH} both warrant a person looking and are {@code WARNING}. Nothing is lost by that
 * collapse, because the relevance is part of the child's code and therefore of its identity.
 */
public class GdprFindingsToDiagnostics {

	private static final Logger LOGGER = Logger.getLogger(GdprFindingsToDiagnostics.class.getName());

	/**
	 * The producer these diagnostics are written under.
	 * <p>
	 * Plainly {@code gdpr.review} and not the {@code stage-action/<name>} form the runtime uses for
	 * its own bookkeeping: these are findings about the object, not a record of what an action did.
	 * <p>
	 * <b>One producer means one language.</b> A producer's roots are replaced as a set, so if a
	 * second {@code gdpr.check~<lang>} is ever configured, both languages review the same revision,
	 * both fire this action against the same object, and the second silently erases the first's
	 * diagnostics - the write succeeds, the event fires, and the finding count simply halves.
	 * Whoever enables a second language has to move to a per-language producer in the same change.
	 */
	public static final String PRODUCER = "gdpr.review";

	/** The coarse classification a UI filters by, shared with every other compliance producer. */
	public static final String CATEGORY = "compliance";

	/**
	 * Code of the one root: the review of this object, as a whole. Everything else this producer
	 * has to say hangs under it, so a reader with several producers on one object collapses each
	 * to a single row and opens the one they care about.
	 */
	public static final String CODE_REVIEW = "gdpr.review";

	/** Code for the findings about one classifier. */
	public static final String CODE_CLASSIFIER = "gdpr.classifier";
	/** Code for the findings about one structural feature. */
	public static final String CODE_FEATURE = "gdpr.feature";
	/** Code for the findings about one source-to-target path of a transformation. */
	public static final String CODE_FLOW = "gdpr.flow";
	/** Code for a finding that only arises from several evaluations together. */
	public static final String CODE_COMBINATION = "gdpr.combination";

	/**
	 * Code for a metamodel the report rests on that nobody reviewed. It is the one node this
	 * producer writes at {@code ERROR}, and the one derived from the report's subject rather than
	 * from its findings - see {@link #unreviewedSources}.
	 */
	public static final String CODE_UNREVIEWED_SOURCE = "gdpr.unreviewed-source";

	/** Prefix of every child code, so a client can recognise one without knowing the categories. */
	public static final String CODE_FINDING_PREFIX = "gdpr.finding.";

	/**
	 * A finding whose id reads {@code <code>:<what it is about>}, where the code starts with this
	 * prefix, <b>declares the node it belongs under</b> - see {@link #declaredCode}.
	 */
	private static final String DECLARED_CODE_PREFIX = "gdpr.";

	/** Separates the elements a combination spans in its target. */
	private static final String COMBINATION_SEPARATOR = "+";

	/**
	 * The code of the child carrying one claim: the category and the relevance, which together are
	 * what makes two claims about one element the same claim or two.
	 *
	 * @param category  the claim's category; {@code null} reads as {@code NOT_PERSONAL_DATA}
	 * @param relevance the claim's relevance; {@code null} reads as {@code NONE}
	 * @return the code, never {@code null}
	 */
	public static String findingCode(DataCategory category, RelevanceLevelType relevance) {
		return CODE_FINDING_PREFIX + (category == null ? DataCategory.NOT_PERSONAL_DATA : category).getName() + "."
				+ (relevance == null ? RelevanceLevelType.NONE : relevance).getName();
	}

	/**
	 * The diagnostics of one review, as roots ready to be handed to
	 * {@code updateDiagnostics(..., PRODUCER, roots)}.
	 *
	 * @param report the review; {@code null} yields an empty list
	 * @return the roots, in the order the report examined things; empty when the review found
	 *         nothing that asserts anything
	 */
	public List<Diagnostic> map(GdprReport report) {
		if (report == null) {
			return List.of();
		}
		String source = blankToNull(report.getGeneratedBy());
		List<Diagnostic> elements = new ArrayList<>();
		for (Evaluation evaluation : report.getEvaluation()) {
			collect(evaluation, source, elements);
		}
		for (Map.Entry<NodeKey, List<CombinationFinding>> group : combinationNodes(report).entrySet()) {
			add(elements, element(group.getKey().code(), group.getKey().target(), group.getValue(), source,
					kindOf(group.getValue().get(0))));
		}
		List<Diagnostic> unreviewed = unreviewedSources(report, source);
		elements.addAll(unreviewed);
		if (elements.isEmpty()) {
			// No root at all rather than an empty one: the absence of a gdpr.review root is what
			// says the review found nothing, and an empty root would read as a review that did.
			return List.of();
		}

		DiagnosticSeverity worst = DiagnosticSeverity.INFO;
		int claims = 0;
		for (Diagnostic element : elements) {
			claims += element.getChildren().size();
			if (element.getSeverity().getValue() > worst.getValue()) {
				worst = element.getSeverity();
			}
		}
		Diagnostic review = diagnostic(CODE_REVIEW, null, worst, summary(claims, elements.size(), unreviewed.size()),
				source);
		review.getChildren().addAll(elements);
		return List.of(review);
	}

	private static String summary(int claims, int elements, int unreviewed) {
		String summary = String.format("GDPR review: %d finding%s on %d element%s", claims, claims == 1 ? "" : "s",
				elements, elements == 1 ? "" : "s");
		if (unreviewed == 0) {
			return summary;
		}
		// Said in the one line a reader sees collapsed, because "0 findings" on its own is exactly
		// the reading this has to prevent.
		return summary + String.format("; %d metamodel%s it rests on %s no review", unreviewed,
				unreviewed == 1 ? "" : "s", unreviewed == 1 ? "has" : "have");
	}

	/**
	 * One node per metamodel the report rests on that nobody reviewed (issue #319).
	 * <p>
	 * A transformation is not reviewed, it is derived from the reviews of the metamodels it was
	 * compiled against - so a metamodel with no review means the flows through it are still fact
	 * while nothing classifies what travels along them. That is the check not having run, and it
	 * must never be read as the transformation being clean.
	 * <p>
	 * <b>Derived from the subject, not from a finding.</b> The report model says to list such a
	 * package and to leave its {@code reportId} unset, and that unset value <em>is</em> the
	 * machine-readable statement. It could not be a {@code Finding} even if one wanted it to be:
	 * {@code Finding.evidence} is mandatory, an analyser cites only by carrying a citation over
	 * from a review, and for an unreviewed source there is no review to carry one from.
	 * <p>
	 * <b>{@code ERROR}, which is not an exception to the rule that a review never produces one.</b>
	 * That rule exists because {@code ERROR} means the check did not run rather than that the model
	 * is bad - and here the check genuinely did not run. The two agree: a finding about the data is
	 * at most a {@code WARNING}; a statement that there is no finding to make is an {@code ERROR}.
	 * <p>
	 * It lands on the object the report is about - the compiled unit - and never on the unreviewed
	 * metamodel itself. "You have no GDPR review" is a statement about a metamodel, but it is not
	 * this transformation's business to write it there, and one would appear per transformation
	 * that reads the model.
	 */
	private static List<Diagnostic> unreviewedSources(GdprReport report, String source) {
		if (!(report.getSubject() instanceof TransformationSubject subject)) {
			return List.of();
		}
		List<PackageSubject> rested = new ArrayList<>(subject.getSourcePackages());
		rested.addAll(subject.getTargetPackages());
		List<Diagnostic> nodes = new ArrayList<>();
		Set<String> seen = new LinkedHashSet<>();
		for (PackageSubject entry : rested) {
			// A model declared inout is listed twice, as the same package in both directions
			if (blankToNull(entry.getReportId()) != null || entry.getNsURI() == null || !seen.add(entry.getNsURI())) {
				continue;
			}
			nodes.add(diagnostic(CODE_UNREVIEWED_SOURCE, entry.getNsURI(), DiagnosticSeverity.ERROR,
					String.format(
							"%s has no GDPR review of the revision this transformation was compiled against (%s), "
									+ "so nothing classifies the data that travels through it. The analysis is "
									+ "incomplete, not clean.",
							entry.getNsURI(), entry.getSubjectFingerprint()),
					source));
		}
		return nodes;
	}

	private void collect(Evaluation evaluation, String source, List<Diagnostic> elements) {
		if (evaluation instanceof ClassifierEvaluation classifier) {
			elements.addAll(nodesOf(CODE_CLASSIFIER, targetOf(classifier), classifier.getFindings(), source));
			for (FeatureEvaluation feature : classifier.getFeatureEvaluation()) {
				elements.addAll(nodesOf(CODE_FEATURE, targetOf(feature), feature.getFindings(), source));
			}
		} else if (evaluation instanceof FeatureEvaluation feature) {
			// A feature the report held directly rather than under its classifier. It carries its
			// own uriFragment, so it is addressed exactly the same way.
			elements.addAll(nodesOf(CODE_FEATURE, targetOf(feature), feature.getFindings(), source));
		} else if (evaluation instanceof FlowEvaluation flow) {
			elements.addAll(nodesOf(CODE_FLOW, targetOf(flow), flow.getFindings(), source));
		} else {
			// A kind of evaluation added to the report model after this was written. Say so: a
			// silently dropped evaluation reads as a review that found nothing.
			LOGGER.log(Level.WARNING, () -> String.format(
					"The GDPR review holds a %s, which this mapper cannot address; its findings are not written "
							+ "onto the reviewed object. It carries classifiers, features, flows and combinations.",
					evaluation.eClass().getName()));
		}
	}

	/**
	 * Which node a group of findings becomes: its code and the element it is about.
	 * <p>
	 * A diagnostic's id is derived from its code and its target, so two nodes sharing both under
	 * one parent are not two rows - the second silently replaces the first. This pair is therefore
	 * the thing that has to be unique, and it is what the findings are grouped by.
	 */
	private record NodeKey(String code, String target) {
	}

	/**
	 * The nodes one element's findings become: normally one, and one per declared code where the
	 * findings say they are different kinds of statement.
	 *
	 * @see #declaredCode
	 */
	private List<Diagnostic> nodesOf(String defaultCode, String target, Collection<? extends Finding> findings,
			String source) {
		Map<String, List<Finding>> byCode = new LinkedHashMap<>();
		for (Finding finding : findings) {
			byCode.computeIfAbsent(declaredCode(finding, defaultCode), code -> new ArrayList<>()).add(finding);
		}
		List<Diagnostic> nodes = new ArrayList<>(byCode.size());
		for (Map.Entry<String, List<Finding>> group : byCode.entrySet()) {
			add(nodes, element(group.getKey(), target, group.getValue(), source, null));
		}
		return nodes;
	}

	/**
	 * The combinations grouped into the nodes they become, by code and target.
	 * <p>
	 * A report may hold several combinations over the <em>same</em> set of evaluations - a
	 * transformation report says three different things about one concatenation - and one node per
	 * combination would mint one id for all of them. Grouped by what the node is addressed by, each
	 * kind of statement gets its own node and combinations that really are the same statement fold
	 * into one.
	 */
	private static Map<NodeKey, List<CombinationFinding>> combinationNodes(GdprReport report) {
		Map<NodeKey, List<CombinationFinding>> grouped = new LinkedHashMap<>();
		for (CombinationFinding combination : report.getCombinations()) {
			NodeKey key = new NodeKey(declaredCode(combination, CODE_COMBINATION), combinationTarget(combination));
			grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(combination);
		}
		return grouped;
	}

	/**
	 * The node code a finding declares for itself, or {@code fallback}.
	 * <p>
	 * A review's findings declare nothing: several findings on one feature are several signals for
	 * one verdict, and folding them into one node under {@code gdpr.feature} is the whole point.
	 * A <em>derived</em> report is the other case. Its findings on one flow come from different
	 * rules and each states something of its own - that a classified value reaches a field, and
	 * separately that the purpose stated for it does not travel with it - so folding them would
	 * run two statements into one paragraph and, where they happen to share a category and a
	 * relevance, mint one id for both.
	 * <p>
	 * The report model has no field for the kind of a finding, so the id is where a producer says
	 * it: {@code <code>:<what it is about>} with a code under {@code gdpr.}. Anything else - an id
	 * like {@code F-001}, or no id at all - declares nothing and behaves exactly as before.
	 */
	private static String declaredCode(Finding finding, String fallback) {
		String id = blankToNull(finding.getId());
		if (id == null) {
			return fallback;
		}
		int separator = id.indexOf(':');
		if (separator <= 0) {
			return fallback;
		}
		String code = id.substring(0, separator);
		return code.startsWith(DECLARED_CODE_PREFIX) ? code : fallback;
	}

	/**
	 * One node for one element, with one child per claim, or {@code null} when the element has
	 * nothing to say. It hangs under the producer's single root.
	 */
	private Diagnostic element(String code, String target, Collection<? extends Finding> findings, String source,
			String note) {
		Map<String, Claim> claims = claims(findings, target);
		if (claims.isEmpty()) {
			return null;
		}
		if (target == null) {
			// An unset target means "the object as a whole", so every unaddressable element would
			// mint the same id and all but one of them would be lost without a trace.
			LOGGER.log(Level.WARNING, () -> String.format(
					"A reviewed element of kind '%s' carries %d finding(s) but nothing to address it by, so they "
							+ "cannot be written onto the object; a node without a target would collide with every "
							+ "other one of its kind under the same root.",
					code, claims.size()));
			return null;
		}

		List<Diagnostic> children = new ArrayList<>(claims.size());
		List<String> headline = new ArrayList<>(claims.size());
		DiagnosticSeverity worst = DiagnosticSeverity.INFO;
		for (Map.Entry<String, Claim> entry : claims.entrySet()) {
			Claim claim = entry.getValue();
			DiagnosticSeverity severity = severityOf(claim.relevance);
			children.add(diagnostic(entry.getKey(), target, severity, claim.message(), source));
			headline.add(claim.label());
			if (severity.getValue() > worst.getValue()) {
				worst = severity;
			}
		}

		// No "GDPR review" prefix: the producer's root above already says whose finding this is,
		// and the target says which element, so the message is the claim and nothing else.
		Diagnostic element = diagnostic(code, target, worst,
				(note == null ? "" : note + ": ") + String.join(", ", headline), source);
		element.getChildren().addAll(children);
		return element;
	}

	/**
	 * Folds an element's findings into one claim per {@code (category, relevance)}, in the order
	 * the report first states each. Findings that assert nothing are dropped here.
	 */
	private Map<String, Claim> claims(Collection<? extends Finding> findings, String target) {
		Map<String, Claim> claims = new LinkedHashMap<>();
		for (Finding finding : findings) {
			if (assertsNothing(finding)) {
				LOGGER.log(Level.FINE, () -> String.format(
						"Finding '%s' on '%s' is %s at relevance %s, which the review uses for 'examined and "
								+ "nothing of concern found', so no diagnostic is written for it.",
						finding.getId(), target, name(finding.getCategory()), name(finding.getRelevanceLevel())));
				continue;
			}
			claims.computeIfAbsent(findingCode(finding.getCategory(), finding.getRelevanceLevel()),
					code -> new Claim(finding.getCategory(), finding.getRelevanceLevel())).add(finding);
		}
		return claims;
	}

	/**
	 * Whether the finding says the element is clean. {@code NONE} relevance is the report model's
	 * "examined and nothing of concern found"; {@code NOT_PERSONAL_DATA} and {@code ANONYMOUS} are
	 * the categories that place the data outside the Regulation. A finding that contradicts itself
	 * - a clean category at a relevance above {@code NONE} - is read on its category, because the
	 * category is what the claim asserts and the relevance only says how serious it would be.
	 */
	private static boolean assertsNothing(Finding finding) {
		DataCategory category = finding.getCategory();
		return finding.getRelevanceLevel() == null || finding.getRelevanceLevel() == RelevanceLevelType.NONE
				|| category == null || category == DataCategory.NOT_PERSONAL_DATA || category == DataCategory.ANONYMOUS;
	}

	/**
	 * How serious the finding is, in the vocabulary the object's metadata uses. Never
	 * {@code ERROR}: that means the check did not run, and a review that ran and found something is
	 * not a failure of the check.
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

	/* ------------------------------------------------------------------ addressing */

	private static String targetOf(ClassifierEvaluation classifier) {
		return blankToNull(classifier.getUriFragment());
	}

	private static String targetOf(FeatureEvaluation feature) {
		return blankToNull(feature.getUriFragment());
	}

	/**
	 * A flow has no fragment of its own, so it is addressed the way the report model says a flow is
	 * identified: the mapping it sits in, and the path from the source feature to the target one.
	 * The namespaces are part of it because two source models can carry the same fragment, which
	 * the model's own documentation warns about.
	 */
	private static String targetOf(FlowEvaluation flow) {
		String mapping = blankToNull(flow.getMapping());
		String from = qualified(flow.getSourceNsURI(), flow.getSourceFeature());
		String to = qualified(flow.getTargetNsURI(), flow.getTargetFeature());
		if (mapping == null && from == null && to == null) {
			// Nothing derivable; the report's own id for the evaluation is documented as stable
			// across reruns, so it is the honest last resort.
			return blankToNull(flow.getId());
		}
		return (mapping == null ? "" : mapping + ":") + (from == null ? "?" : from) + "->" + (to == null ? "?" : to);
	}

	private static String qualified(String nsURI, String fragment) {
		String feature = blankToNull(fragment);
		if (feature == null) {
			return null;
		}
		String namespace = blankToNull(nsURI);
		return namespace == null ? feature : namespace + "#" + feature;
	}

	/**
	 * A combination is about a set of elements rather than one, so it is addressed by all of them,
	 * sorted so that the id does not turn on the order the analyser happened to list them in.
	 */
	private static String combinationTarget(CombinationFinding combination) {
		Set<String> parts = new TreeSet<>();
		for (Evaluation evaluation : combination.getFeatures()) {
			String target = targetOfAny(evaluation);
			if (target != null) {
				parts.add(target);
			}
		}
		return parts.isEmpty() ? null : String.join(COMBINATION_SEPARATOR, parts);
	}

	private static String targetOfAny(Evaluation evaluation) {
		if (evaluation instanceof FeatureEvaluation feature) {
			return targetOf(feature);
		}
		if (evaluation instanceof ClassifierEvaluation classifier) {
			return targetOf(classifier);
		}
		if (evaluation instanceof FlowEvaluation flow) {
			return targetOf(flow);
		}
		return blankToNull(evaluation.getId());
	}

	/**
	 * How the elements interact, for the root's message. Deliberately <em>not</em> part of the id:
	 * what makes two claims the same claim is the elements, the category and the relevance, and a
	 * review that reads the same risk as profiling rather than as a quasi-identifier set has not
	 * changed what it asks a person to decide.
	 */
	private static String kindOf(CombinationFinding combination) {
		return combination.getCombinationKind() == null ? null : combination.getCombinationKind().getName();
	}

	/* ------------------------------------------------------------------ small helpers */

	private static void add(List<Diagnostic> elements, Diagnostic element) {
		if (element != null) {
			elements.add(element);
		}
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

	private static String name(Enum<?> value) {
		return value == null ? "unset" : value.name();
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}

	/**
	 * What one {@code (category, relevance)} pair on one element amounts to, gathered from every
	 * finding that reached it.
	 */
	private static final class Claim {

		private final DataCategory category;
		private final RelevanceLevelType relevance;
		private final List<String> rationales = new ArrayList<>();
		private final List<String> recommendations = new ArrayList<>();
		private final Set<String> citations = new LinkedHashSet<>();
		private ConfidenceType confidence;

		Claim(DataCategory category, RelevanceLevelType relevance) {
			this.category = category == null ? DataCategory.NOT_PERSONAL_DATA : category;
			this.relevance = relevance == null ? RelevanceLevelType.NONE : relevance;
		}

		void add(Finding finding) {
			addIfPresent(rationales, finding.getRationale());
			addIfPresent(recommendations, finding.getRecommendation());
			for (Evidence evidence : finding.getEvidence()) {
				String citation = blankToNull(evidence.getCitationId());
				if (citation != null) {
					citations.add(citation);
				}
			}
			confidence = mostCautious(confidence, finding.getConfidence());
		}

		/** What the badge says: the claim, in the report's own words. */
		String label() {
			return category.getName() + " (" + relevance.getName() + ")";
		}

		/**
		 * The claim as a person reads it: the review's own reasoning first, then what it suggests
		 * doing, what it quoted and how sure it was. Everything here is the report's, verbatim.
		 */
		String message() {
			StringBuilder message = new StringBuilder(rationales.isEmpty() ? label() : String.join(" ", rationales));
			if (!recommendations.isEmpty()) {
				message.append(" | Recommendation: ").append(String.join(" ", recommendations));
			}
			if (!citations.isEmpty()) {
				message.append(" | Evidence: ").append(String.join(", ", citations));
			}
			if (confidence != null) {
				message.append(" | Confidence: ").append(confidence.getName());
			}
			return message.toString();
		}

		private static void addIfPresent(List<String> values, String value) {
			String trimmed = blankToNull(value);
			if (trimmed != null && !values.contains(trimmed)) {
				values.add(trimmed);
			}
		}

		/**
		 * The least certain of two confidences, so folding findings never makes a claim look surer
		 * than the least sure review that reached it. {@code REQUIRES_PURPOSE_CONFIRMATION} is the
		 * most cautious of all: it says the metamodel cannot settle the question.
		 */
		private static ConfidenceType mostCautious(ConfidenceType one, ConfidenceType other) {
			if (one == null) {
				return other;
			}
			if (other == null) {
				return one;
			}
			return cautiousness(one) <= cautiousness(other) ? one : other;
		}

		private static int cautiousness(ConfidenceType confidence) {
			return switch (confidence) {
			case REQUIRES_PURPOSE_CONFIRMATION -> 0;
			case LOW -> 1;
			case MEDIUM -> 2;
			case HIGH -> 3;
			};
		}
	}
}
