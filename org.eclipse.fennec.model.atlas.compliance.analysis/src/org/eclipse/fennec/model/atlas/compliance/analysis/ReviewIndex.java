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
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.fennec.model.compliance.context.CategoryRef;
import org.eclipse.fennec.model.compliance.context.ContextRef;
import org.eclipse.fennec.model.compliance.report.ClassifierEvaluation;
import org.eclipse.fennec.model.compliance.report.CombinationFinding;
import org.eclipse.fennec.model.compliance.report.Evaluation;
import org.eclipse.fennec.model.compliance.report.Evidence;
import org.eclipse.fennec.model.compliance.report.FeatureEvaluation;
import org.eclipse.fennec.model.compliance.report.Finding;
import org.eclipse.fennec.model.compliance.report.ComplianceReport;
import org.eclipse.fennec.model.compliance.report.RelevanceLevel;

/**
 * One metamodel's review, indexed the way an analyser asks about it: by the {@code uriFragment} of
 * the feature.
 * <p>
 * This is the whole of what an analyser knows <em>about the data</em>. It classifies nothing
 * itself: a category, a relevance and a purpose all come from here, and where this says nothing
 * about a feature the analyser says nothing about that feature rather than deciding.
 * <p>
 * What it does not bound is what an analyser may say about the <em>artefact</em>. A derived
 * statement - this classified value reaches that field - rests wholly on the review and is
 * evidenced by carrying the review's own citation over, which is the route {@link #evidenceOf}
 * serves. A control statement - this classified field is stored unencrypted, this resource is
 * published with no retention rule - is a judgement about the artefact that no review of a
 * metamodel ever made, so there is no citation here to carry: it is evidenced against the
 * requirement it is raised under, by dereferencing that requirement's cited text in the compliance
 * context. Which fields such a rule may speak about at all still comes from here.
 */
public final class ReviewIndex {

	/**
	 * The taxonomy a review's data categories point into. Everything else a finding may reference -
	 * a combination kind, a lawful basis - is in a taxonomy of its own and is not a classification
	 * of the data, so it is read past here.
	 */
	public static final String TAXONOMY = "data-categories";

	/**
	 * The categories that mean personal data is involved.
	 * <p>
	 * {@code PSEUDONYMISED} is among them, and {@code ANONYMOUS} is not. The two look alike in a
	 * taxonomy and are opposites here: anonymisation is the measure that took the data out of the
	 * Regulation, while pseudonymisation is a safeguard <em>within</em> it - Rec. 26 says in as
	 * many words that pseudonymised data which could be attributed to a person with additional
	 * information is information about an identifiable person. A reviewer who writes
	 * {@code PSEUDONYMISED} on a field is saying it still holds personal data, so a flow of it is a
	 * flow like any other. Leaving it out cost the pseudonym field of a pseudonymisation
	 * transformation every finding it should have had, which is the one field such a report exists
	 * to talk about.
	 * <p>
	 * It stays below {@code PERSONAL_DATA} in {@link #SEVERITY} all the same: the safeguard is real
	 * and the ordering is of how much the category constrains processing, not of whether the
	 * Regulation applies.
	 */
	public static final Set<String> PERSONAL = Set.of("PERSONAL_DATA", "PSEUDONYMISED", "DIRECT_IDENTIFIER",
			"QUASI_IDENTIFIER", "ONLINE_IDENTIFIER", "LOCATION_DATA", "SPECIAL_CATEGORY",
			"CRIMINAL_CONVICTION_DATA", "CHILD_DATA");

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

	public ReviewIndex(ComplianceReport report) {
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
	public String reportId() {
		return report.getReportId();
	}

	/**
	 * The contexts the review was made against. A derived report carries them over rather than
	 * naming any of its own: every citation it carries came from a review, so the contexts those
	 * quotes were read against are the contexts that report is against. One that also raises
	 * control statements of its own names the context it raised them against beside these.
	 */
	public List<ContextRef> contexts() {
		return report.getContexts();
	}

	/** The language the review wrote its prose in, or {@code null} when it did not say. */
	public String language() {
		return report.getLanguage();
	}

	/** What the review says about one feature, or {@code null} if it says nothing about it. */
	public FeatureEvaluation feature(String uriFragment) {
		return uriFragment == null ? null : byFragment.get(uriFragment);
	}

	/** Every feature the review has an entry for, in the order the review lists them. */
	public Collection<String> fragments() {
		return byFragment.keySet();
	}

	/**
	 * The combinations the reviewer raised over several features of this model.
	 * <p>
	 * They are the one thing in a review that no single feature's entry carries, and the one thing
	 * an analysis of the artefact cannot arrive at on its own: that two fields are joinable is a
	 * judgement about the data, not about what the artefact does with it. The analyser reads them
	 * to find out whether the artefact rebuilds a set somebody already described.
	 */
	public List<CombinationFinding> combinations() {
		return report.getCombinations();
	}

	/**
	 * The features a combination is over, as the fragments an analysis matches on.
	 * <p>
	 * {@code CombinationFinding.features} is a non-containment reference into the same document, so
	 * a member is the review's own {@code FeatureEvaluation} and carries the fragment directly. A
	 * member that is not a feature evaluation, or one left unresolved by a loader, contributes
	 * nothing - and a combination that lost a member is deliberately not completed from what is
	 * left: the set the reviewer described is the whole of it or it is not that set.
	 *
	 * @param combination the reviewed combination, never {@code null}
	 * @return the members' uri fragments in the order the review listed them, possibly empty
	 */
	public static List<String> membersOf(CombinationFinding combination) {
		List<String> members = new ArrayList<>();
		for (Evaluation member : combination.getFeatures()) {
			if (member instanceof FeatureEvaluation feature && feature.getUriFragment() != null) {
				members.add(feature.getUriFragment());
			}
		}
		return members;
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
	public static List<String> categoriesOf(Finding finding) {
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
	 * signal separately - and what a statement about the feature rests on is the strongest of them.
	 * A single finding may now claim several categories as well, and they are weighed the same
	 * way.
	 */
	public static String worstCategory(FeatureEvaluation feature) {
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
	public static boolean weakerThan(String first, String second) {
		if (first == null || second == null) {
			return false;
		}
		return SEVERITY.indexOf(first) < SEVERITY.indexOf(second);
	}

	/** The stronger of two categories, either of which may be absent. */
	public static String stronger(String first, String second) {
		if (first == null) {
			return second;
		}
		if (second == null) {
			return first;
		}
		return weakerThan(first, second) ? second : first;
	}

	/** The higher of two relevance levels, either of which may be absent. */
	public static RelevanceLevel higher(RelevanceLevel first, RelevanceLevel second) {
		if (first == null) {
			return second;
		}
		if (second == null) {
			return first;
		}
		return first.getValue() >= second.getValue() ? first : second;
	}

	/**
	 * The reference a review used for one category of one feature, or {@code null} where it made no
	 * such claim.
	 * <p>
	 * A category travels through this analysis as an id, because that is what the severity ordering
	 * compares - but an id is not the whole of what the reviewer wrote. The reference also names
	 * the context and the version of it the claim was made against, and {@code contextId} is
	 * mandatory on it. So a finding that carries a category over goes back to the reference that
	 * asserted it rather than minting one from the surviving string.
	 *
	 * @param feature    the feature, or {@code null}
	 * @param categoryId the category id, or {@code null}
	 * @return the reference as the review holds it - still contained by the review, so a caller
	 *         that records it has to copy it
	 */
	public static CategoryRef refFor(FeatureEvaluation feature, String categoryId) {
		if (feature == null || categoryId == null) {
			return null;
		}
		for (Finding finding : feature.getFindings()) {
			for (CategoryRef ref : finding.getCategories()) {
				if (TAXONOMY.equals(ref.getTaxonomyId()) && categoryId.equals(ref.getCategoryId())) {
					return ref;
				}
			}
		}
		return null;
	}

	/**
	 * Every citation the review made about one feature.
	 * <p>
	 * These are what a finding derived from the review is evidenced with: it carries a citation
	 * over from the review that checked it, never composes one. A review finding whose evidence is
	 * absent contributes none - and a derived finding with no evidence to carry cannot be raised,
	 * because {@code Finding.evidence} is mandatory and a compliance record must not hold a claim
	 * nobody backed. A control statement the review never made has nothing here to carry and takes
	 * its evidence from its requirement instead; see the class comment.
	 */
	public static List<Evidence> evidenceOf(FeatureEvaluation feature) {
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
