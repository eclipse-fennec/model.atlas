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
package org.eclipse.fennec.model.atlas.compliance.analysis;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.compliance.report.Evidence;
import org.eclipse.fennec.model.compliance.report.Finding;
import org.eclipse.fennec.model.compliance.report.RelevanceLevel;

/**
 * Findings of one report, rendered as the Diagnostic tree a reader of the analysed model sees:
 * one root, one element node per thing the report speaks about, one claim under it per
 * {@code (category, relevance)} pair.
 *
 * <p>
 * The shape is the same whatever produced the findings, which is why it is here and not with an
 * analyser. What differs between analysers is <em>which</em> element a finding belongs on and what
 * the element's code is - the caller decides that and hands over an {@link ElementKey}. What does
 * not differ is that several findings about one thing must fold into one node rather than
 * collide: a diagnostic's identity is {@code digest(parentId, code, target)}, so two nodes built
 * with the same code on the same target are one node to anything that reads them back, and the
 * second one is simply lost.
 * </p>
 *
 * <p>
 * Nothing here composes a sentence. A claim's message is the findings' own rationales, in the
 * order they were added, followed by the citation ids they carried. An analyser that wants a
 * different sentence writes a different rationale.
 * </p>
 */
public final class FindingDiagnostics {

	/** The diagnostic category every compliance finding is filed under. */
	public static final String CATEGORY = "compliance";

	/**
	 * Prefix of a claim's code. The rest is the category and the relevance, so a reader can filter
	 * on the code alone without parsing a message.
	 */
	public static final String CODE_FINDING_PREFIX = "gdpr.finding.";

	/** Stands in for a category on a finding that states none, so the code is always well formed. */
	public static final String UNCLASSIFIED = "unclassified";

	private FindingDiagnostics() {
	}

	/**
	 * What one element node is: one statement about one feature of one model.
	 * <p>
	 * A {@code null} feature addresses the model as a whole, which is right for a statement about
	 * a set rather than a field. It cannot collide with a feature node, because the code is part
	 * of the key.
	 *
	 * @param code    the element node's diagnostic code
	 * @param feature the feature the node is about, or {@code null} for the model as a whole
	 */
	public record ElementKey(String code, String feature) {
	}

	/** The claims gathered on one element, folded per {@code (category, relevance)}. */
	public static final class Element {

		private final Map<String, Claim> claims = new LinkedHashMap<>();

		public void add(Finding finding, String category, RelevanceLevel relevance) {
			claims.computeIfAbsent(findingCode(category, relevance), code -> new Claim(category, relevance))
					.add(finding);
		}

		public Diagnostic toDiagnostic(ElementKey key, String source) {
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

		private final String category;
		private final RelevanceLevel relevance;
		private final List<String> rationales = new ArrayList<>();
		private final Set<String> citations = new LinkedHashSet<>();

		Claim(String category, RelevanceLevel relevance) {
			this.category = category == null ? UNCLASSIFIED : category;
			this.relevance = relevance == null ? RelevanceLevel.NONE : relevance;
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
			return category + " (" + relevance.getName() + ")";
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

	/**
	 * The producer's single root over the elements gathered for one model, with a headline saying
	 * how much it found.
	 *
	 * @param rootCode    the root node's diagnostic code - one per producer
	 * @param subjectName what the headline names as the thing that was analysed
	 * @param elements    the elements for this model; empty answers with an empty list
	 * @param source      the revision the findings came from, for {@code Diagnostic.source}
	 * @return one root, or an empty list when there is nothing to say
	 */
	public static List<Diagnostic> rootsOf(String rootCode, String subjectName, Map<ElementKey, Element> elements,
			String source) {
		if (elements.isEmpty()) {
			// An empty list, not an empty root: it is what clears what an earlier analysis left on
			// the model, and a root with no children would read as an artefact that was examined
			// and found to do nothing.
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
		Diagnostic root = diagnostic(rootCode, null, worst,
				String.format("%s: %d finding%s on %d element%s", subjectName, claims, claims == 1 ? "" : "s",
						nodes.size(), nodes.size() == 1 ? "" : "s"),
				source);
		root.getChildren().addAll(nodes);
		return List.of(root);
	}

	/** The code of one claim: the prefix, the category it is about and the relevance stated. */
	public static String findingCode(String category, RelevanceLevel relevance) {
		return CODE_FINDING_PREFIX + (category == null ? UNCLASSIFIED : category) + "."
				+ (relevance == null ? RelevanceLevel.NONE : relevance).getName();
	}

	/**
	 * A finding is never an error. {@code ERROR} says the check did not run, which is a different
	 * statement from anything a finding makes about the data.
	 */
	public static DiagnosticSeverity severityOf(RelevanceLevel relevance) {
		if (relevance == null) {
			return DiagnosticSeverity.INFO;
		}
		return switch (relevance) {
		case MEDIUM, HIGH -> DiagnosticSeverity.WARNING;
		case LOW, NONE -> DiagnosticSeverity.INFO;
		};
	}

	/** One diagnostic, always in {@link #CATEGORY}. */
	public static Diagnostic diagnostic(String code, String target, DiagnosticSeverity severity, String message,
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

	/** Trimmed, or {@code null} where a model's unset string arrives as blank. */
	public static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value.trim();
	}
}
