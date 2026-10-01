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

import org.eclipse.fennec.model.gdprReport.ClassifierEvaluation;
import org.eclipse.fennec.model.gdprReport.DataCategory;
import org.eclipse.fennec.model.gdprReport.Evaluation;
import org.eclipse.fennec.model.gdprReport.Evidence;
import org.eclipse.fennec.model.gdprReport.FeatureEvaluation;
import org.eclipse.fennec.model.gdprReport.Finding;
import org.eclipse.fennec.model.gdprReport.GdprReport;
import org.eclipse.fennec.model.gdprReport.LegalCorpusRef;
import org.eclipse.fennec.model.gdprReport.RelevanceLevelType;

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
	 * The categories that mean personal data is involved. {@code PSEUDONYMISED} and
	 * {@code ANONYMOUS} are deliberately not among them: they are what a field looks like after the
	 * measure worked, so a flow carrying one of those is not a flow of identifiable data - but they
	 * do rank above {@code NOT_PERSONAL_DATA} in {@link #SEVERITY}, because losing pseudonymisation
	 * along the way is still worth seeing.
	 */
	static final Set<DataCategory> PERSONAL = Set.of(DataCategory.PERSONAL_DATA, DataCategory.DIRECT_IDENTIFIER,
			DataCategory.QUASI_IDENTIFIER, DataCategory.ONLINE_IDENTIFIER, DataCategory.LOCATION_DATA,
			DataCategory.SPECIAL_CATEGORY, DataCategory.CRIMINAL_CONVICTION_DATA, DataCategory.CHILD_DATA);

	/**
	 * Weakest to strongest, which is the order two classifications are compared in - for the worst
	 * category reaching a field, and for deciding that a target review classifies a field more
	 * weakly than what arrives in it. It is an ordering of how much the category constrains
	 * processing, not a ranking of harm.
	 */
	private static final List<DataCategory> SEVERITY = List.of(DataCategory.NOT_PERSONAL_DATA,
			DataCategory.ANONYMOUS, DataCategory.PSEUDONYMISED, DataCategory.PERSONAL_DATA,
			DataCategory.ONLINE_IDENTIFIER, DataCategory.LOCATION_DATA, DataCategory.QUASI_IDENTIFIER,
			DataCategory.DIRECT_IDENTIFIER, DataCategory.CHILD_DATA, DataCategory.CRIMINAL_CONVICTION_DATA,
			DataCategory.SPECIAL_CATEGORY);

	private final GdprReport report;
	private final Map<String, FeatureEvaluation> byFragment = new LinkedHashMap<>();

	ReviewIndex(GdprReport report) {
		this.report = report;
		for (Evaluation evaluation : report.getEvaluation()) {
			if (evaluation instanceof ClassifierEvaluation classifier) {
				for (FeatureEvaluation feature : classifier.getFeatureEvaluation()) {
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
	 * The corpus the review cited against. A transformation report carries it over rather than
	 * naming one of its own: every citation in it came from a review, so the corpus those quotes
	 * were read from is the corpus this report is against.
	 */
	LegalCorpusRef corpus() {
		return report.getCorpus();
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
	 * The strongest category anybody claimed about the feature, or {@code null} when the review
	 * made no claim. Several findings on one feature is the ordinary case - a reviewer records each
	 * signal separately - and what travels along a flow is the strongest of them.
	 */
	static DataCategory worstCategory(FeatureEvaluation feature) {
		DataCategory worst = null;
		if (feature == null) {
			return null;
		}
		for (Finding finding : feature.getFindings()) {
			DataCategory candidate = finding.getCategory();
			if (candidate != null && (worst == null || SEVERITY.indexOf(candidate) > SEVERITY.indexOf(worst))) {
				worst = candidate;
			}
		}
		return worst;
	}

	/** Whether the first classification constrains processing less than the second. */
	static boolean weakerThan(DataCategory first, DataCategory second) {
		if (first == null || second == null) {
			return false;
		}
		return SEVERITY.indexOf(first) < SEVERITY.indexOf(second);
	}

	/** The stronger of two categories, either of which may be absent. */
	static DataCategory stronger(DataCategory first, DataCategory second) {
		if (first == null) {
			return second;
		}
		if (second == null) {
			return first;
		}
		return weakerThan(first, second) ? second : first;
	}

	/** The higher of two relevance levels, either of which may be absent. */
	static RelevanceLevelType higher(RelevanceLevelType first, RelevanceLevelType second) {
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
