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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.fennec.model.compliance.context.CategoryRef;
import org.eclipse.fennec.model.compliance.report.Finding;

/**
 * Which category refs of a finding this producer reads, and which of them it treats as saying the
 * element is clean.
 * <p>
 * <b>Why this is configuration and not a constant.</b> A finding used to carry one
 * {@code DataCategory}, an enum whose literals the code could compare against. It now carries
 * {@link CategoryRef}s into a taxonomy of the {@code ComplianceContext} the review ran against, so
 * the categories are ids and their meaning lives in that context. The GDPR context names its
 * taxonomy {@code data-categories} and gives its categories the eleven ids the old enum had, which
 * is why the defaults here reproduce the previous behaviour exactly - but a review against another
 * context brings its own vocabulary, and this is where a deployment says so.
 * <p>
 * <b>What it deliberately does not do.</b> It never resolves the context. Knowing which ids are
 * benign is a deployment's statement, not a lookup: this bundle has to work where the context is
 * not stored at all, and a diagnostic write that depends on a second object being present and
 * visible is a diagnostic write that silently stops happening.
 */
record CategoryVocabulary(String taxonomyId, String combinationTaxonomyId, Set<String> benign) {

	/** The taxonomy a GDPR context holds its data categories in. */
	static final String DEFAULT_TAXONOMY = "data-categories";

	/** The taxonomy a GDPR context holds its combination kinds in. */
	static final String DEFAULT_COMBINATION_TAXONOMY = "combination-kinds";

	/**
	 * The categories that place the data outside the Regulation, as the GDPR context names them.
	 * They are what the old {@code DataCategory} enum used for "examined, and this is not personal
	 * data" - the other nine all assert something.
	 */
	static final List<String> DEFAULT_BENIGN = List.of("NOT_PERSONAL_DATA", "ANONYMOUS");

	/** The vocabulary of the GDPR context, which is what the previous enum encoded. */
	static CategoryVocabulary gdpr() {
		return new CategoryVocabulary(DEFAULT_TAXONOMY, DEFAULT_COMBINATION_TAXONOMY, DEFAULT_BENIGN);
	}

	/**
	 * @param taxonomyId            the taxonomy whose refs are read as the claim's categories
	 * @param combinationTaxonomyId the taxonomy whose refs name how a combination combines
	 * @param benign                the category ids that say the element is clean
	 */
	CategoryVocabulary(String taxonomyId, String combinationTaxonomyId, Collection<String> benign) {
		this(blankOr(taxonomyId, DEFAULT_TAXONOMY), blankOr(combinationTaxonomyId, DEFAULT_COMBINATION_TAXONOMY),
				Set.copyOf(new LinkedHashSet<>(benign)));
	}

	/**
	 * The ids a finding claims, sorted.
	 * <p>
	 * Sorted and not in the order the review listed them: the ids go into the diagnostic's code,
	 * which is half of its identity, and two reviews that named the same two categories in
	 * different orders have not changed what they ask a person to decide.
	 *
	 * @param finding the finding, never {@code null}
	 * @return the category ids of this vocabulary's taxonomy, in a stable order, possibly empty
	 */
	List<String> categoriesOf(Finding finding) {
		Set<String> ids = new TreeSet<>();
		for (CategoryRef ref : finding.getCategories()) {
			if (taxonomyId.equals(ref.getTaxonomyId()) && notBlank(ref.getCategoryId())) {
				ids.add(ref.getCategoryId().trim());
			}
		}
		return new ArrayList<>(ids);
	}

	/**
	 * How a combination combines, as the review named it, or {@code null} when it did not say.
	 * <p>
	 * The kind used to be a {@code CombinationKind} literal on the finding; the GDPR context holds
	 * the same four names as a taxonomy of its own, so it travels as a ref beside the data
	 * categories and is told from them by which taxonomy it points into.
	 *
	 * @param finding the combination, never {@code null}
	 * @return the kind's category id, or {@code null}
	 */
	String combinationKindOf(Finding finding) {
		for (CategoryRef ref : finding.getCategories()) {
			if (combinationTaxonomyId.equals(ref.getTaxonomyId()) && notBlank(ref.getCategoryId())) {
				return ref.getCategoryId().trim();
			}
		}
		return null;
	}

	/**
	 * Whether every category the finding claims places the data outside the Regulation.
	 * <p>
	 * A finding that claims <em>no</em> category is not clean by this test: it asserts a relevance
	 * without saying of what, and dropping it would turn a reviewer's statement into silence.
	 *
	 * @param categories the ids from {@link #categoriesOf}
	 * @return {@code true} when there is at least one and all of them are benign
	 */
	boolean allBenign(List<String> categories) {
		return !categories.isEmpty() && benign.containsAll(categories);
	}

	private static boolean notBlank(String value) {
		return value != null && !value.isBlank();
	}

	private static String blankOr(String value, String fallback) {
		return notBlank(value) ? value.trim() : fallback;
	}
}
