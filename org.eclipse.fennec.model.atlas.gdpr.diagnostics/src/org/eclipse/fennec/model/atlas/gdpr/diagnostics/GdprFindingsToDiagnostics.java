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
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.compliance.report.ClassifierEvaluation;
import org.eclipse.fennec.model.compliance.report.CombinationFinding;
import org.eclipse.fennec.model.compliance.report.Confidence;
import org.eclipse.fennec.model.compliance.report.Evaluation;
import org.eclipse.fennec.model.compliance.report.Evidence;
import org.eclipse.fennec.model.compliance.report.FeatureEvaluation;
import org.eclipse.fennec.model.compliance.report.Finding;
import org.eclipse.fennec.model.compliance.report.FlowEvaluation;
import org.eclipse.fennec.model.compliance.report.ComplianceReport;
import org.eclipse.fennec.model.compliance.report.PackageSubject;
import org.eclipse.fennec.model.compliance.report.RelevanceLevel;
import org.eclipse.fennec.model.compliance.report.TransformationSubject;

/**
 * Turns the findings of one {@link ComplianceReport} into the {@link Diagnostic} trees that are written
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
 * <h2>Identity: one child per (categories, relevance) on one element</h2>
 * <p>
 * A child's {@code code} is {@link #findingCode(List, RelevanceLevel)} and its {@code target} is
 * the element's, so its id turns exactly on <b>element, categories and relevance</b>. That is the
 * rule the re-review has to honour: a second review that reaches the same categories at the same
 * relevance by another route is the <em>same</em> claim, so it keeps its id and with it whatever a
 * person decided about it. A second review that says {@code SPECIAL_CATEGORY} where the first said
 * {@code PERSONAL_DATA}, or that raises the relevance, is a <em>different</em> claim: it gets a new
 * id, is {@code OPEN}, and the old one disappears when the producer's roots are replaced.
 * <p>
 * A finding may now name <b>several</b> categories. They are joined <em>sorted</em>, so the code
 * does not depend on the order a reviewer happened to list them in, and a finding naming exactly
 * one produces the code it always did.
 * <p>
 * Consequently <b>several findings that share an element, its categories and a relevance fold into
 * one child</b>, their rationales, recommendations and citations merged. They differ in how the review
 * reached the claim, not in what it asserts, and there is only one decision for a person to make
 * about it. {@code Finding.id} deliberately plays no part - the analyser assigns it afresh on every
 * run ({@code F-001} in one review, {@code f1} in the next for the same claim), so keying on it
 * would make every diagnostic look new every time. Neither does {@code detectedBy}, which is
 * optional and many-valued: one extra recorded signal must not move the key.
 *
 * <h2>What is written, and at which severity</h2>
 * <p>
 * A finding that <b>asserts nothing</b> is skipped: {@code relevanceLevel} {@code NONE}, or every
 * one of its data categories in the {@linkplain CategoryVocabulary#benign benign set} - by default
 * the GDPR context's {@code NOT_PERSONAL_DATA} and {@code ANONYMOUS}. Both are the report's way of
 * saying "examined and nothing of concern found", and the report already says that by carrying an
 * evaluation with no findings at all. Note that an EMF enum attribute nobody set reads as its first
 * literal, which for {@code relevanceLevel} is {@code NONE}, and that XMI does not write a value
 * equal to the default - so a finding that <em>stated</em> no relevance and one that left the field
 * empty are indistinguishable once stored, and both are skipped.
 * <p>
 * A finding naming <b>no</b> category at a relevance above {@code NONE} is <b>written</b>, as
 * {@code gdpr.finding.<RELEVANCE>}. The old model could not tell an unset category from "examined
 * and found nothing"; a list can, and a reviewer who states a concern without saying of what has
 * still stated one.
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

	/** Which category refs this mapper reads, and which of them say the element is clean. */
	private final CategoryVocabulary vocabulary;

	/** A mapper reading the vocabulary of the GDPR context, which is the previous behaviour. */
	public GdprFindingsToDiagnostics() {
		this(CategoryVocabulary.gdpr());
	}

	/**
	 * @param vocabulary the taxonomy and benign ids to read findings with, never {@code null}
	 */
	GdprFindingsToDiagnostics(CategoryVocabulary vocabulary) {
		this.vocabulary = vocabulary;
	}

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

	/**
	 * Code of the root written when the object itself has no review left: every review of its
	 * revision was withdrawn. {@code ERROR}, and the counterpart of a clean review's {@code INFO}
	 * root - see {@link #noReview(String)}.
	 */
	public static final String CODE_NO_REVIEW = "gdpr.no-review";

	/** Prefix of every child code, so a client can recognise one without knowing the categories. */
	public static final String CODE_FINDING_PREFIX = "gdpr.finding.";

	/**
	 * A finding whose id reads {@code <code>:<what it is about>}, where the code starts with this
	 * prefix, <b>declares the node it belongs under</b> - see {@link #declaredCode}.
	 */
	private static final String DECLARED_CODE_PREFIX = "gdpr.";

	/** Separates the elements a combination spans in its target. */
	private static final String COMBINATION_SEPARATOR = "+";

	/** Joins the categories of a claim that names more than one. */
	private static final String CATEGORY_SEPARATOR = "+";

	/** What an EMF fragment addressing a classifier of the reviewed package starts with. */
	private static final String FRAGMENT_PREFIX = "//";

	/**
	 * The code of the child carrying one claim: the categories and the relevance, which together
	 * are what makes two claims about one element the same claim or two.
	 * <p>
	 * A claim naming one category - the ordinary case, and the only one the previous report model
	 * could express - produces the code it always did, so a diagnostic written before this change
	 * is replaced by its successor rather than left beside it. Several categories join in sorted
	 * order, and none at all leaves the segment out: a finding that states a relevance without
	 * saying of what still has to be written, and there is nothing to name it by.
	 *
	 * @param categories the claim's category ids, sorted; may be empty
	 * @param relevance  the claim's relevance; {@code null} reads as {@code NONE}
	 * @return the code, never {@code null}
	 */
	public static String findingCode(List<String> categories, RelevanceLevel relevance) {
		String level = (relevance == null ? RelevanceLevel.NONE : relevance).getName();
		return categories.isEmpty() ? CODE_FINDING_PREFIX + level
				: CODE_FINDING_PREFIX + String.join(CATEGORY_SEPARATOR, categories) + "." + level;
	}

	/**
	 * The diagnostics of one review, as roots ready to be handed to
	 * {@code updateDiagnostics(..., PRODUCER, roots)}.
	 *
	 * @param report the review; {@code null} yields an empty list
	 * @return the roots, in the order the report examined things; empty when the review found
	 *         nothing that asserts anything
	 */
	public List<Diagnostic> map(ComplianceReport report) {
		if (report == null) {
			return List.of();
		}
		String source = blankToNull(report.getGeneratedBy());
		List<Diagnostic> elements = new ArrayList<>();
		for (Evaluation evaluation : report.getEvaluations()) {
			collect(evaluation, source, elements);
		}
		for (Map.Entry<NodeKey, List<CombinationFinding>> group : combinationNodes(report).entrySet()) {
			add(elements, element(group.getKey().code(), group.getKey().target(), group.getValue(), source,
					kindOf(group.getValue().get(0))));
		}
		List<Diagnostic> unreviewed = unreviewedSources(report, source);
		elements.addAll(unreviewed);
		if (elements.isEmpty()) {
			// A review that found nothing still says so. Silence cannot carry it: a model nobody
			// ever reviewed and one reviewed and cleared would look identical, and telling those
			// two apart is the whole reason this producer exists. The root carries no children -
			// an *empty* root would read as a review that found something and lost it - and it
			// says in words that the check ran and came back clean.
			return List.of(nothingWritten(report, source));
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

	/**
	 * The roots to write when no review of the object's revision is on record any more.
	 * <p>
	 * An object whose last review was withdrawn is not a clean object: nothing has been checked.
	 * Clearing the producer instead would leave it looking exactly like one nobody has reviewed
	 * yet, so this producer says which of the two it is, and says it at {@code ERROR} - the same
	 * severity an unreviewed metamodel gets under {@link #CODE_UNREVIEWED_SOURCE}, and for the
	 * same reason: the check did not run.
	 *
	 * @param fingerprint the revision no review covers; may be {@code null}
	 * @return the one root, never empty
	 */
	public List<Diagnostic> noReview(String fingerprint) {
		String revision = blankToNull(fingerprint);
		return List.of(diagnostic(CODE_NO_REVIEW, null, DiagnosticSeverity.ERROR,
				revision == null ? "GDPR review: none on record for this revision"
						: String.format("GDPR review: none on record for revision '%s'", revision),
				null));
	}

	/**
	 * The root of a review that wrote no findings.
	 * <p>
	 * {@code INFO}, and it names how much was looked at - a review of nothing and a review of
	 * nineteen features that cleared all of them are both "nothing found", and only the second is
	 * reassuring. The one exception is a review that did assert something and had none of it
	 * written, because every element it spoke about was unaddressable: that is a malformed report,
	 * not a clean model, and it must not read as one.
	 */
	private Diagnostic nothingWritten(ComplianceReport report, String source) {
		if (assertsAnything(report)) {
			return diagnostic(CODE_REVIEW, null, DiagnosticSeverity.WARNING,
					"GDPR review: it asserts something, but none of its findings names an element they could be "
							+ "written onto",
					source);
		}
		int examined = examined(report);
		return diagnostic(CODE_REVIEW, null, DiagnosticSeverity.INFO,
				String.format("GDPR review: nothing of concern found, %d element%s examined", examined,
						examined == 1 ? "" : "s"),
				source);
	}

	/** Whether any finding anywhere in the report says something about the data. */
	private boolean assertsAnything(ComplianceReport report) {
		for (Iterator<EObject> contents = report.eAllContents(); contents.hasNext();) {
			if (contents.next() instanceof Finding finding
					&& !assertsNothing(finding, vocabulary.categoriesOf(finding))) {
				return true;
			}
		}
		return false;
	}

	/** How many things the review looked at: classifiers, their features, and flows. */
	private static int examined(ComplianceReport report) {
		int count = 0;
		for (Evaluation evaluation : report.getEvaluations()) {
			count++;
			if (evaluation instanceof ClassifierEvaluation classifier) {
				count += classifier.getFeatureEvaluations().size();
			}
		}
		return count;
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
	private static List<Diagnostic> unreviewedSources(ComplianceReport report, String source) {
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
			for (FeatureEvaluation feature : classifier.getFeatureEvaluations()) {
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
	private static Map<NodeKey, List<CombinationFinding>> combinationNodes(ComplianceReport report) {
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
	 * Folds an element's findings into one claim per {@code (categories, relevance)}, in the order
	 * the report first states each. Findings that assert nothing are dropped here.
	 */
	private Map<String, Claim> claims(Collection<? extends Finding> findings, String target) {
		Map<String, Claim> claims = new LinkedHashMap<>();
		for (Finding finding : findings) {
			List<String> categories = vocabulary.categoriesOf(finding);
			if (assertsNothing(finding, categories)) {
				LOGGER.log(Level.FINE, () -> String.format(
						"Finding '%s' on '%s' is %s at relevance %s, which the review uses for 'examined and "
								+ "nothing of concern found', so no diagnostic is written for it.",
						finding.getId(), target, categories.isEmpty() ? "unset" : String.join(", ", categories),
						name(finding.getRelevanceLevel())));
				continue;
			}
			claims.computeIfAbsent(findingCode(categories, finding.getRelevanceLevel()),
					code -> new Claim(categories, finding.getRelevanceLevel())).add(finding);
		}
		return claims;
	}

	/**
	 * Whether the finding says the element is clean. {@code NONE} relevance is the report model's
	 * "examined and nothing of concern found"; the categories that place the data outside the
	 * Regulation are the ones {@link CategoryVocabulary#benign()} names. A finding that contradicts
	 * itself - a clean category at a relevance above {@code NONE} - is read on its categories,
	 * because they are what the claim asserts and the relevance only says how serious it would be.
	 * <p>
	 * A finding that names <em>no</em> category of this vocabulary is not clean. It asserts a
	 * relevance without saying of what, and the old model's single unset category could not tell
	 * that apart from "examined and found nothing" - a list can, and silence is never the signal.
	 */
	private boolean assertsNothing(Finding finding, List<String> categories) {
		return finding.getRelevanceLevel() == null || finding.getRelevanceLevel() == RelevanceLevel.NONE
				|| vocabulary.allBenign(categories);
	}

	/**
	 * How serious the finding is, in the vocabulary the object's metadata uses. Never
	 * {@code ERROR}: that means the check did not run, and a review that ran and found something is
	 * not a failure of the check.
	 */
	private static DiagnosticSeverity severityOf(RelevanceLevel relevance) {
		if (relevance == null) {
			return DiagnosticSeverity.INFO;
		}
		return switch (relevance) {
		case MEDIUM, HIGH -> DiagnosticSeverity.WARNING;
		case LOW, NONE -> DiagnosticSeverity.INFO;
		};
	}

	/* ------------------------------------------------------------------ addressing */

	/**
	 * How a classifier is addressed: its {@code uriFragment}, else the fragment its name implies,
	 * else its id.
	 * <p>
	 * <b>The fallback recovers findings that would otherwise be dropped.</b> An element with
	 * nothing to address it by is not written at all - see {@link #element} - because an untargeted
	 * node would mint the same id as every other untargeted node of its kind. So a review that
	 * names its elements only by name, which a producer may legitimately do, loses every finding on
	 * them. There is no reason to: a classifier's fragment <em>is</em> {@code //} and its name.
	 * <p>
	 * The synthesised value is a fragment rather than the bare name on purpose. A target is an
	 * address a reader resolves against the model, and two classifiers sharing a feature name would
	 * otherwise collide back into one node - the very thing the drop exists to prevent.
	 */
	private static String targetOf(ClassifierEvaluation classifier) {
		String fragment = blankToNull(classifier.getUriFragment());
		if (fragment != null) {
			return fragment;
		}
		String name = blankToNull(classifier.getName());
		return name != null ? FRAGMENT_PREFIX + name : blankToNull(classifier.getId());
	}

	/**
	 * How a feature is addressed: its {@code uriFragment}, else its name under whatever its
	 * classifier resolved to, else its id.
	 * <p>
	 * The owner comes from the containment - {@code featureEvaluations} is a containment list - so
	 * this holds wherever a feature is reached from, including the cross-references a
	 * {@link CombinationFinding} holds, where the classifier is not otherwise in hand. A feature
	 * the report carries directly has no classifier and falls through to its id.
	 * <p>
	 * The prefix is only used when the classifier itself resolved to a fragment. Hanging a name off
	 * an id would read as a path and address nothing.
	 *
	 * @see #targetOf(ClassifierEvaluation)
	 */
	private static String targetOf(FeatureEvaluation feature) {
		String fragment = blankToNull(feature.getUriFragment());
		if (fragment != null) {
			return fragment;
		}
		String name = blankToNull(feature.getName());
		if (name != null && feature.eContainer() instanceof ClassifierEvaluation owner) {
			String ownerTarget = targetOf(owner);
			if (ownerTarget != null && ownerTarget.startsWith(FRAGMENT_PREFIX)) {
				return ownerTarget + "/" + name;
			}
		}
		return blankToNull(feature.getId());
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
	private String kindOf(CombinationFinding combination) {
		return vocabulary.combinationKindOf(combination);
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

		private final List<String> categories;
		private final RelevanceLevel relevance;
		private final List<String> rationales = new ArrayList<>();
		private final List<String> recommendations = new ArrayList<>();
		private final Set<String> citations = new LinkedHashSet<>();
		private Confidence confidence;

		Claim(List<String> categories, RelevanceLevel relevance) {
			this.categories = List.copyOf(categories);
			this.relevance = relevance == null ? RelevanceLevel.NONE : relevance;
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

		/**
		 * What the badge says: the claim, in the report's own words.
		 * <p>
		 * The categories are the ids of the context's taxonomy, printed as they are. A reviewer
		 * reads them, so a context whose category ids are opaque makes an unreadable badge - which
		 * is a property of that context, not something this can repair without resolving it.
		 */
		String label() {
			return (categories.isEmpty() ? "unclassified" : String.join(", ", categories)) + " ("
					+ relevance.getName() + ")";
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
		 * than the least sure review that reached it. {@code REQUIRES_CONFIRMATION} is the
		 * most cautious of all: it says the metamodel cannot settle the question.
		 */
		private static Confidence mostCautious(Confidence one, Confidence other) {
			if (one == null) {
				return other;
			}
			if (other == null) {
				return one;
			}
			return cautiousness(one) <= cautiousness(other) ? one : other;
		}

		private static int cautiousness(Confidence confidence) {
			return switch (confidence) {
			case REQUIRES_CONFIRMATION -> 0;
			case LOW -> 1;
			case MEDIUM -> 2;
			case HIGH -> 3;
			};
		}
	}


}
