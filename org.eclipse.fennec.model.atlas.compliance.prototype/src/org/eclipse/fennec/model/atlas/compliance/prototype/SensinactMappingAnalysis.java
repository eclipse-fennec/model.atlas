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
import org.eclipse.fennec.event.atlas.model.mapping.AdminMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ProviderMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ResourceMapping;
import org.eclipse.fennec.event.atlas.model.mapping.ServiceMapping;
import org.eclipse.fennec.model.atlas.compliance.analysis.RequirementCitations;
import org.eclipse.fennec.model.atlas.compliance.analysis.ReviewIndex;
import org.eclipse.fennec.model.compliance.report.ComplianceReport;
import org.eclipse.fennec.model.compliance.report.ControlEvaluation;
import org.eclipse.fennec.model.compliance.report.ControlResult;
import org.eclipse.fennec.model.compliance.report.FeatureEvaluation;
import org.eclipse.fennec.model.compliance.report.Finding;
import org.eclipse.fennec.model.compliance.report.FlowEvaluation;
import org.eclipse.fennec.model.compliance.report.RelevanceLevel;

/**
 * A GDPR report for a sensiNact {@code ProviderMapping}, derived from the review of the metamodel
 * whose values it publishes.
 *
 * <p>
 * <b>What a mapping does is publish.</b> A mapped resource becomes a sensiNact resource served by
 * the northbound REST and SensorThings surfaces, on deployments where reads are public and only
 * writes are authenticated. A {@code ResourceMapping} is therefore a disclosure decision, and that
 * is the finding that leads this report.
 * </p>
 *
 * <p>
 * The rules that would need the deployment model - whether a historization filter excludes the
 * resource, whether a housekeeping policy covers it - answer {@code NOT_ASSESSED} here, because
 * no registry holds an {@code EventAtlasDeployment}. That is the honest answer and not a gap in
 * the analyser: a retention check that cannot run must never read as one that passed.
 * </p>
 */
public final class SensinactMappingAnalysis {

	public static final String CATALOGUE = "sensinact-mapping-analysis/1";

	private final ReviewIndex review;
	private final RequirementCitations citations;
	private final EPackage source;
	private final String fingerprint;

	public SensinactMappingAnalysis(ReviewIndex review, RequirementCitations citations, EPackage source,
			String fingerprint) {
		this.review = review;
		this.citations = citations;
		this.source = source;
		this.fingerprint = fingerprint;
	}

	public ComplianceReport analyse(ProviderMapping mapping, String name) {
		ComplianceReport report = StaticReport.about(CATALOGUE, "GDPR analysis of " + name, name,
				"sensinact-mapping", source, fingerprint, review);
		for (ServiceMapping service : mapping.getServices()) {
			for (ResourceMapping resource : service.getResources()) {
				resourcePublishesPersonal(report, resource);
				noDeletionRule(report, resource);
				historisation(report, resource);
			}
		}
		locationOfAPerson(report, mapping.getAdmin());
		return report;
	}

	/**
	 * {@code RESOURCE_PUBLISHES_PERSONAL}: a classified value becomes a northbound-readable
	 * resource. The disclosure finding, and the one that should lead the report.
	 */
	private void resourcePublishesPersonal(ComplianceReport report, ResourceMapping resource) {
		for (EStructuralFeature feature : resource.getValueFeature()) {
			FeatureEvaluation reviewed = review.feature(OclConstraintAnalysis.fragmentOf(feature));
			String category = ReviewIndex.worstCategory(reviewed);
			if (category == null) {
				continue;
			}
			Finding finding = StaticReport.control("RESOURCE_PUBLISHES_PERSONAL:" + resource.getMid(),
					String.format(
							"Resource '%s' publishes %s, which the review classifies %s. A mapped resource is "
									+ "served by the northbound REST and SensorThings surfaces; on a deployment "
									+ "where reads are public, this value is readable by anyone.",
							resource.getMid(), OclConstraintAnalysis.fragmentOf(feature), category),
					"Do not map this feature to a resource, or restrict read access to the provider.",
					RelevanceLevel.HIGH, citations, "Art.5(1)(f)", review.language(),
					ReviewIndex.refFor(reviewed, category));
			if (finding != null) {
				StaticReport.control(report, "gdpr", "Art.5(1)(f)", ControlResult.NOT_SATISFIED).getFindings()
						.add(finding);
			}
		}
	}

	/**
	 * {@code LOCATION_OF_A_PERSON}: the provider's position is taken from features the review
	 * classifies. Location tied to a person is personal data and frequently special category by
	 * inference. A flow: the value travels from the model into the provider's admin service.
	 */
	private void locationOfAPerson(ComplianceReport report, AdminMapping admin) {
		if (admin == null) {
			return;
		}
		for (EStructuralFeature feature : concat(admin.getLatitudeRef(), admin.getLongitudeRef())) {
			FeatureEvaluation reviewed = review.feature(OclConstraintAnalysis.fragmentOf(feature));
			String category = ReviewIndex.worstCategory(reviewed);
			if (category == null) {
				continue;
			}
			FlowEvaluation evaluation = StaticReport.flow(report,
					"LOCATION_OF_A_PERSON:" + OclConstraintAnalysis.fragmentOf(feature),
					OclConstraintAnalysis.fragmentOf(feature) + " becomes the provider's position");
			Finding finding = StaticReport.carried(
					"LOCATION_OF_A_PERSON:" + OclConstraintAnalysis.fragmentOf(feature),
					String.format(
							"The provider's position is taken from %s, classified %s. A check-in has a person "
									+ "at one end, so the provider's position is that person's whereabouts at a "
									+ "known time.",
							OclConstraintAnalysis.fragmentOf(feature), category),
					RelevanceLevel.HIGH, reviewed, category);
			if (finding != null) {
				evaluation.getFindings().add(finding);
			}
		}
	}

	/**
	 * {@code NO_DELETION_RULE}: a classified resource with no retention at all.
	 * <p>
	 * Reported, but deliberately <b>not</b> recommended as the remedy: {@code deletionRule} is
	 * inert in the current gateway - nothing consumes it - so writing one would make an
	 * unprotected system look protected. The recommendation names the lever that does work.
	 */
	private void noDeletionRule(ComplianceReport report, ResourceMapping resource) {
		if (resource.getDeletionRule() != null || !publishesClassified(resource)) {
			return;
		}
		Finding finding = StaticReport.control("NO_DELETION_RULE:" + resource.getMid(),
				String.format(
						"Resource '%s' publishes classified data and carries no deletion rule, so every value "
								+ "ever sampled is kept.",
						resource.getMid()),
				"Exclude the resource on a historization filter of the deployment, which is the lever that is "
						+ "enforced. A ResourceMapping.deletionRule is not: nothing in the gateway consumes it, "
						+ "so setting one would record a measure that does not run.",
				RelevanceLevel.MEDIUM, citations, "Art.5(1)(e)", review.language(), null);
		if (finding != null) {
			StaticReport.control(report, "gdpr", "Art.5(1)(e)", ControlResult.NOT_SATISFIED).getFindings()
					.add(finding);
		}
	}

	/**
	 * {@code NOT_EXCLUDED_FROM_HISTORY}, answered {@code NOT_ASSESSED}: it needs the deployment
	 * model, and no registry holds one.
	 */
	private void historisation(ComplianceReport report, ResourceMapping resource) {
		if (!publishesClassified(resource)) {
			return;
		}
		ControlEvaluation evaluation = StaticReport.control(report, "gdpr", "Art.5(1)(c)",
				ControlResult.NOT_ASSESSED);
		evaluation.setName(String.format(
				"Whether '%s' is historised was not assessed: it is decided by the HistorizationFilters of the "
						+ "EventAtlasDeployment, and no registry holds one.",
				resource.getMid()));
	}

	private boolean publishesClassified(ResourceMapping resource) {
		return resource.getValueFeature().stream()
				.anyMatch(feature -> ReviewIndex.worstCategory(
						review.feature(OclConstraintAnalysis.fragmentOf(feature))) != null);
	}

	private static java.util.List<EStructuralFeature> concat(java.util.List<EStructuralFeature> first,
			java.util.List<EStructuralFeature> second) {
		java.util.List<EStructuralFeature> all = new java.util.ArrayList<>(first);
		all.addAll(second);
		return all;
	}
}
