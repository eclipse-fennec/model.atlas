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

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.fennec.model.atlas.compliance.analysis.RequirementCitations;
import org.eclipse.fennec.model.atlas.compliance.analysis.ReviewIndex;
import org.eclipse.fennec.model.compliance.report.ComplianceReport;
import org.eclipse.fennec.model.compliance.report.ControlEvaluation;
import org.eclipse.fennec.model.compliance.report.ControlResult;
import org.eclipse.fennec.model.compliance.report.FeatureEvaluation;
import org.eclipse.fennec.model.compliance.report.Finding;
import org.eclipse.fennec.model.compliance.report.RelevanceLevel;
import org.eclipse.fennec.persistence.eorm.Base;
import org.eclipse.fennec.persistence.eorm.Basic;
import org.eclipse.fennec.persistence.eorm.EFeatureObject;
import org.eclipse.fennec.persistence.eorm.Entity;
import org.eclipse.fennec.persistence.eorm.EntityMappings;
import org.eclipse.fennec.persistence.eorm.Id;

/**
 * A GDPR report for an {@code EntityMappings}, derived from the review of the metamodel it
 * persists.
 *
 * <p>
 * The simplest join of the three and the one with no parser: a mapping names the
 * {@code EStructuralFeature} outright, through {@code accessibleObject} as an
 * {@link EFeatureObject}, so the review's key is a field read.
 * </p>
 *
 * <p>
 * Almost every statement here is a <b>control</b> statement - *is this column encrypted?*, *is
 * there a retention rule?* - and none of them is something a review of a metamodel ever said. They
 * exist as {@code Finding}s only because the requirement they are raised under can be quoted out
 * of the compliance context.
 * </p>
 */
public final class EormMappingAnalysis {

	public static final String CATALOGUE = "eorm-mapping-analysis/1";

	/** Categories at or above which a column is worth encrypting at rest. */
	private static final java.util.Set<String> AT_REST = java.util.Set.of("PERSONAL_DATA", "DIRECT_IDENTIFIER",
			"QUASI_IDENTIFIER", "ONLINE_IDENTIFIER", "LOCATION_DATA", "SPECIAL_CATEGORY",
			"CRIMINAL_CONVICTION_DATA", "CHILD_DATA");

	private final ReviewIndex review;
	private final RequirementCitations citations;
	private final EPackage source;
	private final String fingerprint;

	public EormMappingAnalysis(ReviewIndex review, RequirementCitations citations, EPackage source,
			String fingerprint) {
		this.review = review;
		this.citations = citations;
		this.source = source;
		this.fingerprint = fingerprint;
	}

	public ComplianceReport analyse(EntityMappings mappings) {
		ComplianceReport report = StaticReport.about(CATALOGUE, "GDPR analysis of " + mappings.getName(),
				mappings.getName(), "eorm", source, fingerprint, review);
		for (Entity entity : mappings.getEntity()) {
			if (entity.getAttributes() == null) {
				continue;
			}
			for (Id id : entity.getAttributes().getId()) {
				identifierAsKey(report, entity, id);
			}
			for (Basic basic : entity.getAttributes().getBasic()) {
				unprotectedAtRest(report, entity, basic);
			}
			noRetentionRule(report, entity);
		}
		return report;
	}

	/**
	 * {@code UNPROTECTED_AT_REST}: a classified column with no {@code convert}. The lever is in
	 * the mapping itself, which is what makes this worth reporting rather than merely noting.
	 */
	private void unprotectedAtRest(ComplianceReport report, Entity entity, Basic basic) {
		FeatureEvaluation reviewed = reviewOf(basic);
		String category = ReviewIndex.worstCategory(reviewed);
		if (category == null || !AT_REST.contains(category) || basic.getConvert() != null) {
			return;
		}
		Finding finding = StaticReport.control("UNPROTECTED_AT_REST:" + entity.getName() + ":" + basic.getName(),
				String.format(
						"Column %s of table %s receives %s, which the review classifies %s, and the mapping "
								+ "binds no converter. The value is stored in clear.",
						column(basic), table(entity), fragment(basic), category),
				"Bind a converter on this basic with the eorm Convert facet, as the mapping already does for "
						+ "another column of this entity.",
				RelevanceLevel.HIGH, citations, "Art.32(1)(a)", review.language(),
				ReviewIndex.refFor(reviewed, category));
		if (finding != null) {
			StaticReport.control(report, "gdpr", "Art.32(1)(a)", ControlResult.NOT_SATISFIED).getFindings()
					.add(finding);
		}
	}

	/**
	 * {@code IDENTIFIER_AS_KEY}: a {@code DIRECT_IDENTIFIER} used as the primary key.
	 * <p>
	 * Encryption is explicitly <em>not</em> the recommendation: it breaks lookups and joins. The
	 * measure is a surrogate key with the identifier kept as an encrypted basic beside it.
	 */
	private void identifierAsKey(ComplianceReport report, Entity entity, Id id) {
		FeatureEvaluation reviewed = reviewOf(id);
		String category = ReviewIndex.worstCategory(reviewed);
		if (!"DIRECT_IDENTIFIER".equals(category)) {
			return;
		}
		Finding finding = StaticReport.control("IDENTIFIER_AS_KEY:" + entity.getName() + ":" + id.getName(),
				String.format(
						"The primary key of %s is %s, classified %s. The identifier is therefore in every index, "
								+ "every foreign key and every log line that names a row.",
						table(entity), fragment(id), category),
				"Introduce a surrogate key and keep the identifier as an encrypted basic. Encrypting the key "
						+ "itself is not the measure - it breaks lookups and joins.",
				RelevanceLevel.HIGH, citations, "Art.25(1)", review.language(),
				ReviewIndex.refFor(reviewed, category));
		if (finding != null) {
			StaticReport.control(report, "gdpr", "Art.25(1)", ControlResult.NOT_SATISFIED).getFindings().add(finding);
		}
	}

	/**
	 * {@code NO_RETENTION_RULE}, answered {@code NOT_ASSESSED}.
	 * <p>
	 * The check needs the inventory registry, which this prototype does not read. Recording it as
	 * not assessed rather than leaving it out is the point: an absent check and a passed check
	 * must never look the same in a compliance record.
	 */
	private void noRetentionRule(ComplianceReport report, Entity entity) {
		boolean anyClassified = entity.getAttributes().getBasic().stream()
				.anyMatch(basic -> ReviewIndex.worstCategory(reviewOf(basic)) != null);
		if (!anyClassified) {
			return;
		}
		ControlEvaluation evaluation = StaticReport.control(report, "gdpr", "Art.5(1)(e)",
				ControlResult.NOT_ASSESSED);
		evaluation.setName(String.format(
				"Retention of %s was not assessed: no RetentionRule of the compliance inventory was read.",
				table(entity)));
	}

	/* ------------------------------------------------------------------------------ the join */

	/** What the review says about the feature a mapping element persists. */
	private FeatureEvaluation reviewOf(Base base) {
		EStructuralFeature feature = featureOf(base);
		return feature == null ? null : review.feature(OclConstraintAnalysis.fragmentOf(feature));
	}

	/**
	 * The feature behind a mapping element. The current eorm model holds it on
	 * {@code accessibleObject} as an {@link EFeatureObject} - not directly on {@code Base}, which
	 * carries only the column.
	 */
	private static EStructuralFeature featureOf(Base base) {
		return base.getAccessibleObject() instanceof EFeatureObject accessor ? accessor.getFeature() : null;
	}

	private static String fragment(Base base) {
		EStructuralFeature feature = featureOf(base);
		return feature == null ? "(no feature)" : OclConstraintAnalysis.fragmentOf(feature);
	}

	private static String column(Base base) {
		return base.getColumn() == null ? base.getName() : base.getColumn().getName();
	}

	private static String table(Entity entity) {
		return entity.getTable() == null ? entity.getName() : entity.getTable().getName();
	}
}
