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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.fennec.model.compliance.context.CategoryRef;
import org.eclipse.fennec.model.compliance.context.ContextRef;
import org.eclipse.fennec.model.compliance.report.ClassifierEvaluation;
import org.eclipse.fennec.model.compliance.report.Evaluation;
import org.eclipse.fennec.model.compliance.report.Evidence;
import org.eclipse.fennec.model.compliance.report.FeatureEvaluation;
import org.eclipse.fennec.model.compliance.report.Finding;
import org.eclipse.fennec.model.compliance.report.ComplianceReport;
import org.eclipse.fennec.model.compliance.report.RelevanceLevel;

/**
 * One metamodel's review, indexed the way a flow asks about it: by the {@code uriFragment} of the
 * feature.
 * <p>
 * This is the whole of what the analyser knows about data protection. It classifies nothing itself
 * and it reads no corpus: a category, a relevance, a purpose and a citation all come from here, and
 * where this says nothing the analyser says nothing rather than deciding.
 */
final class ReviewIndex {

	/**
	 * The taxonomy a review's data categories point into. Everything else a finding may reference -
	 * a combination kind, a lawful basis - is in a taxonomy of its own and is not a classification
	 * of the data, so it is read past here.
	 */
	static final String TAXONOMY = "data-categories";

	/**
	 * The categories that mean personal data is involved. {@code PSEUDONYMISED} and
	 * {@code ANONYMOUS} are deliberately not among them: they are what a field looks like after the
	 * measure worked, so a flow carrying one of those is not a flow of identifiable data - but they
	 * do rank above {@code NOT_PERSONAL_DATA} in {@link #SEVERITY}, because losing pseudonymisation
	 * along the way is still worth seeing.
	 */
	static final Set<String> PERSONAL = Set.of("PERSONAL_DATA", "DIRECT_IDENTIFIER", "QUASI_IDENTIFIER",
			"ONLINE_IDENTIFIER", "LOCATION_DATA", "SPECIAL_CATEGORY", "CRIMINAL_CONVICTION_DATA", "CHILD_DATA");

	/**
	 * Weakest to strongest, which is the order two classifications are compared in - for the worst
	 * category reaching a field, and for deciding that a target review classifies a field more
	 * weakly than what arrives in it. It is an ordering of how much the category constrains
	 * processing, not a ranking of harm.
	 * <p>
	 * <b>Ids and no longer enum literals.</b> A finding used to carry one {@code DataCategory};
	 * it now carries references into a taxonomy of the context the review ran against. These are
	 * the ids the GDPR context gives its categories, which are the names the enum had - so the
	 * ordering is unchanged and so is every verdict that depends on it. A category this list does
	 * not name ranks below all of them, which is the cautious reading only because such a category
	 * also fails {@link #PERSONAL} and therefore never reaches a comparison.
	 */
	private static final List<String> SEVERITY = List.of("NOT_PERSONAL_DATA", "ANONYMOUS", "PSEUDONYMISED",
			"PERSONAL_DATA", "ONLINE_IDENTIFIER", "LOCATION_DATA", "QUASI_IDENTIFIER", "DIRECT_IDENTIFIER",
			"CHILD_DATA", "CRIMINAL_CONVICTION_DATA", "SPECIAL_CATEGORY");

	private final ComplianceReport report;
	private final Map<String, FeatureEvaluation> byFragment = new LinkedHashMap<>();

	ReviewIndex(ComplianceReport report) {
		this.report = report;
		for (Evaluation evaluation : report.getEvaluations()) {
			if (evaluation instanceof ClassifierEvaluation classifier) {
				for (FeatureEvaluation feature : classifier.getFeatureEvaluations()) {
					index(feature);
				}
			} else if (evaluation instanceof FeatureEvaluation feature) {
				// A review may evaluate a feature without going through its classifier
				index(feature);
			}
		}
	}

	private void index(FeatureEvaluation feature) {
		if (feature.getUriFragment() != null) {
			byFragment.putIfAbsent(feature.getUriFragment(), feature);
		}
	}

	/** Which review this is, so a report can point at the one it carried its findings from. */
	String reportId() {
		return report.getReportId();
	}

	/**
	 * The contexts the review was made against. A transformation report carries them over rather
	 * than naming any of its own: every citation in it came from a review, so the contexts those
	 * quotes were read against are the contexts this report is against.
	 */
	List<ContextRef> contexts() {
		return report.getContexts();
	}

	/** The language the review wrote its prose in, or {@code null} when it did not say. */
	String language() {
		return report.getLanguage();
	}

	/** What the review says about one feature, or {@code null} if it says nothing about it. */
	FeatureEvaluation feature(String uriFragment) {
		return uriFragment == null ? null : byFragment.get(uriFragment);
	}

	/** Every feature the review has an entry for, in the order the review lists them. */
	Collection<String> fragments() {
		return byFragment.keySet();
	}

	/* ------------------------------------------------------------------ reading an evaluation */

	/**
	 * Every data category a finding claims, in this vocabulary's taxonomy.
	 * <p>
	 * A finding may now name several where it could name one, and may name refs of other taxonomies
	 * beside them - the combination kind travels that way. Only this taxonomy's are classifications
	 * of the data.
	 *
	 * @param finding the finding, never {@code null}
	 * @return the category ids, possibly empty
	 */
	static List<String> categoriesOf(Finding finding) {
		List<String> ids = new ArrayList<>();
		for (CategoryRef ref : finding.getCategories()) {
			if (TAXONOMY.equals(ref.getTaxonomyId()) && ref.getCategoryId() != null
					&& !ref.getCategoryId().isBlank()) {
				ids.add(ref.getCategoryId().trim());
			}
		}
		return ids;
	}

	/**
	 * The strongest category anybody claimed about the feature, or {@code null} when the review
	 * made no claim. Several findings on one feature is the ordinary case - a reviewer records each
	 * signal separately - and what travels along a flow is the strongest of them. A single finding
	 * may now claim several categories as well, and they are weighed the same way.
	 */
	static String worstCategory(FeatureEvaluation feature) {
		String worst = null;
		if (feature == null) {
			return null;
		}
		for (Finding finding : feature.getFindings()) {
			for (String candidate : categoriesOf(finding)) {
				if (worst == null || SEVERITY.indexOf(candidate) > SEVERITY.indexOf(worst)) {
					worst = candidate;
				}
			}
		}
		return worst;
	}

	/** Whether the first classification constrains processing less than the second. */
	static boolean weakerThan(String first, String second) {
		if (first == null || second == null) {
			return false;
		}
		return SEVERITY.indexOf(first) < SEVERITY.indexOf(second);
	}

	/** The stronger of two categories, either of which may be absent. */
	static String stronger(String first, String second) {
		if (first == null) {
			return second;
		}
		if (second == null) {
			return first;
		}
		return weakerThan(first, second) ? second : first;
	}

	/** The higher of two relevance levels, either of which may be absent. */
	static RelevanceLevel higher(RelevanceLevel first, RelevanceLevel second) {
		if (first == null) {
			return second;
		}
		if (second == null) {
			return first;
		}
		return first.getValue() >= second.getValue() ? first : second;
	}

	/**
	 * Every citation the review made about one feature.
	 * <p>
	 * These are what a flow finding is evidenced with, and they are the reason the analyser needs
	 * no corpus at all: it carries a citation over from the review that checked it, never composes
	 * one. A review finding whose evidence is absent contributes none - and a flow finding with no
	 * evidence to carry cannot be raised, because {@code Finding.evidence} is mandatory and a
	 * compliance record must not hold a claim nobody backed.
	 */
	static List<Evidence> evidenceOf(FeatureEvaluation feature) {
		List<Evidence> evidence = new ArrayList<>();
		if (feature == null) {
			return evidence;
		}
		for (Finding finding : feature.getFindings()) {
			evidence.addAll(finding.getEvidence());
		}
		return evidence;
	}
}
