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

import java.time.Instant;
import java.util.List;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.compliance.analysis.RequirementCitations;
import org.eclipse.fennec.model.atlas.compliance.analysis.RequirementCitations.Citation;
import org.eclipse.fennec.model.atlas.compliance.analysis.ReviewIndex;
import org.eclipse.fennec.model.compliance.context.CategoryRef;
import org.eclipse.fennec.model.compliance.context.ContextRef;
import org.eclipse.fennec.model.compliance.context.RequirementRef;
import org.eclipse.fennec.model.compliance.report.ComplianceReport;
import org.eclipse.fennec.model.compliance.report.ControlEvaluation;
import org.eclipse.fennec.model.compliance.report.ControlResult;
import org.eclipse.fennec.model.compliance.report.Evidence;
import org.eclipse.fennec.model.compliance.report.FeatureEvaluation;
import org.eclipse.fennec.model.compliance.report.Finding;
import org.eclipse.fennec.model.compliance.report.FlowEvaluation;
import org.eclipse.fennec.model.compliance.report.PackageSubject;
import org.eclipse.fennec.model.compliance.report.RelevanceLevel;
import org.eclipse.fennec.model.compliance.report.ReportFactory;
import org.eclipse.fennec.model.compliance.report.ReportOrigin;
import org.eclipse.fennec.model.compliance.report.TransformationSubject;

/**
 * The parts of a static report that are the same whatever artefact was analysed.
 *
 * <p>
 * Two things are worth looking at here rather than in the three analysers, because they are the
 * claims the whole exercise rests on.
 * </p>
 *
 * <p>
 * <b>Evidence comes by one of two routes and never by a third.</b> {@link #carried} copies the
 * citation the metamodel review already made about a feature - the only honest evidence for a
 * statement that merely restates where a classified value goes. {@link #control} dereferences the
 * requirement the rule is raised under, through {@link RequirementCitations}, because no review of
 * a metamodel ever said that a column is unencrypted. Nothing composes a citation.
 * </p>
 *
 * <p>
 * <b>The subject carries the source package's fingerprint, resolved now.</b> None of the three
 * artefacts records which revision it was authored against, so the report pins the revision the
 * analysis actually read and says so in the disclaimer. That is the one thing a reader must not be
 * misled about.
 * </p>
 */
public final class StaticReport {

	/** What a derived report is and is not, in the words a reader needs. */
	public static final String DISCLAIMER = "This report is derived mechanically from the stored artefact and "
			+ "from the GDPR review of the metamodel it refers to. It classifies nothing itself: every data "
			+ "category in it was established by that review. Where it makes a statement of its own - that a "
			+ "measure the Regulation requires is absent - it cites the provision it is made against and quotes "
			+ "it from the compliance context, never composing a citation. The artefact records no revision of "
			+ "the metamodel, so the fingerprint named here is the revision resolved at analysis time, not the "
			+ "one the author saw: if the model moved since, this report is about the model as it is now. A rule "
			+ "nobody wrote down never fires, so an absence of findings is not a statement that there is nothing "
			+ "to find.";

	private static final ReportFactory REPORTS = ReportFactory.eINSTANCE;

	private StaticReport() {
	}

	/**
	 * An empty report about one artefact, with its subject, its contexts and its disclaimer set.
	 *
	 * @param catalogue   the rule catalogue and version, e.g. {@code ocl-constraint-analysis/1}
	 * @param name        a human readable title
	 * @param artefact    the artefact's qualified name
	 * @param language    what kind of artefact it is: {@code ocl}, {@code eorm},
	 *                    {@code sensinact-mapping}
	 * @param source      the reviewed package the artefact refers to
	 * @param fingerprint that package's fingerprint, computed now
	 * @param review      the review the analysis rests on, for the contexts it was made against
	 */
	public static ComplianceReport about(String catalogue, String name, String artefact, String language,
			EPackage source, String fingerprint, ReviewIndex review) {
		ComplianceReport report = REPORTS.createComplianceReport();
		report.setReportId(catalogue.replace('/', '-') + "-" + artefact);
		report.setName(name);
		report.setGeneratedAt(Instant.now().toString());
		report.setGeneratedBy(catalogue);
		report.setOrigin(ReportOrigin.STATIC_ANALYSIS);
		report.setLanguage(review.language());
		report.setDisclaimer(DISCLAIMER);

		PackageSubject sourcePackage = REPORTS.createPackageSubject();
		sourcePackage.setNsURI(source.getNsURI());
		sourcePackage.setName(source.getName());
		sourcePackage.setSubjectFingerprint(fingerprint);
		sourcePackage.setReportId(review.reportId());

		TransformationSubject subject = REPORTS.createTransformationSubject();
		subject.setQualifiedName(artefact);
		subject.setLanguage(language);
		subject.getSourcePackages().add(sourcePackage);
		report.setSubject(subject);

		// Carried over, not minted: every citation in this report was either read out of the
		// review or dereferenced in a context the review names, so those are its contexts.
		for (ContextRef context : review.contexts()) {
			report.getContexts().add(EcoreUtil.copy(context));
		}
		return report;
	}

	/** One statement about one element of the artefact. */
	public static FlowEvaluation flow(ComplianceReport report, String id, String name) {
		FlowEvaluation evaluation = REPORTS.createFlowEvaluation();
		evaluation.setId(id);
		evaluation.setName(name);
		report.getEvaluations().add(evaluation);
		return evaluation;
	}

	/**
	 * A control check against one requirement, which is what most of these reports are.
	 * <p>
	 * It is the only evaluation that can say a measure is <em>absent</em>, and the only one usable
	 * where the context cannot be quoted at all - it needs a {@code RequirementRef} and nothing
	 * else, which is why a statement against an ISO control is expressible and a {@code Finding}
	 * against one is not.
	 */
	public static ControlEvaluation control(ComplianceReport report, String contextId, String requirementId,
			ControlResult result) {
		ControlEvaluation evaluation = REPORTS.createControlEvaluation();
		evaluation.setRequirement(requirementRef(contextId, requirementId, null));
		evaluation.setResult(result);
		report.getEvaluations().add(evaluation);
		return evaluation;
	}

	/**
	 * A finding whose evidence is the review's own, carried over unchanged.
	 *
	 * @return {@code null} when the review backed its claim with nothing, because
	 *         {@code Finding.evidence} is mandatory and a compliance record must not hold a claim
	 *         nobody backed
	 */
	public static Finding carried(String id, String rationale, RelevanceLevel relevance, FeatureEvaluation reviewed,
			String categoryId) {
		List<Evidence> evidence = ReviewIndex.evidenceOf(reviewed);
		if (evidence.isEmpty()) {
			return null;
		}
		Finding finding = REPORTS.createFinding();
		finding.setId(id);
		finding.setOrigin(ReportOrigin.STATIC_ANALYSIS);
		finding.setRationale(rationale);
		finding.setRelevanceLevel(relevance);
		CategoryRef category = ReviewIndex.refFor(reviewed, categoryId);
		if (category != null) {
			finding.getCategories().add(EcoreUtil.copy(category));
		}
		for (Evidence one : evidence) {
			finding.getEvidence().add(EcoreUtil.copy(one));
		}
		return finding;
	}

	/**
	 * A finding of the analyser's own, evidenced by dereferencing the requirement it is made
	 * under.
	 * <p>
	 * This is the route that did not exist before #366, and the reason control statements could
	 * not be written at all: there is no review of an eorm or a constraint set to inherit a
	 * citation from. Where the requirement cannot be quoted the finding is not raised - the caller
	 * records a {@link ControlEvaluation} instead, which needs no quote.
	 *
	 * @return {@code null} when the requirement is unquotable or unknown
	 */
	public static Finding control(String id, String rationale, String recommendation, RelevanceLevel relevance,
			RequirementCitations citations, String requirementId, String language, CategoryRef about) {
		Citation citation = citations.resolve(requirementId, language);
		if (!citation.isFound()) {
			return null;
		}
		Finding finding = REPORTS.createFinding();
		finding.setId(id);
		finding.setOrigin(ReportOrigin.STATIC_ANALYSIS);
		finding.setRationale(rationale);
		finding.setRecommendation(recommendation);
		finding.setRelevanceLevel(relevance);
		finding.getRequirements().add(requirementRef(citation.contextId(), requirementId, citation.contextVersion()));
		if (about != null) {
			finding.getCategories().add(EcoreUtil.copy(about));
		}
		for (Evidence one : citation.evidence()) {
			finding.getEvidence().add(one);
		}
		return finding;
	}

	private static RequirementRef requirementRef(String contextId, String requirementId, String version) {
		RequirementRef ref = org.eclipse.fennec.model.compliance.context.ContextFactory.eINSTANCE
				.createRequirementRef();
		ref.setContextId(contextId);
		ref.setRequirementId(requirementId);
		if (version != null) {
			ref.setContextVersion(version);
		}
		return ref;
	}
}
