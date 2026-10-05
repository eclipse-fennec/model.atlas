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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.eclipse.fennec.m2x.model.compiled.CompiledUnit;
import org.eclipse.fennec.m2x.model.compiled.CompiledUnitManifest;
import org.eclipse.fennec.m2x.model.compiled.PackageEntry;
import org.eclipse.fennec.model.atlas.qvt.gdpr.QvtFlowExtractor.Direction;
import org.eclipse.fennec.model.atlas.qvt.gdpr.QvtFlowExtractor.Extraction;
import org.eclipse.fennec.model.atlas.qvt.gdpr.RuleCatalogue.Rule;
import org.eclipse.fennec.model.compliance.context.CategoryRef;
import org.eclipse.fennec.model.compliance.context.ContextFactory;
import org.eclipse.fennec.model.compliance.context.ContextRef;
import org.eclipse.fennec.model.compliance.report.CombinationFinding;
import org.eclipse.fennec.model.compliance.report.DetectionSignal;
import org.eclipse.fennec.model.compliance.report.Evidence;
import org.eclipse.fennec.model.compliance.report.FeatureEvaluation;
import org.eclipse.fennec.model.compliance.report.Finding;
import org.eclipse.fennec.model.compliance.report.FlowEvaluation;
import org.eclipse.fennec.model.compliance.report.FlowKind;
import org.eclipse.fennec.model.compliance.report.ReportFactory;
import org.eclipse.fennec.model.compliance.report.ComplianceReport;
import org.eclipse.fennec.model.compliance.report.ReportOrigin;
import org.eclipse.fennec.model.compliance.report.PackageSubject;
import org.eclipse.fennec.model.compliance.report.RelevanceLevel;
import org.eclipse.fennec.model.compliance.report.TransformationSubject;

/**
 * Derives the GDPR report of a compiled transformation from the reviews of the metamodels it was
 * compiled against (issue #319).
 *
 * <p>
 * A pure function of two stored documents: the {@code CompiledUnit} and the reviews of the exact
 * model revisions its manifest names. No provider, no key, no network, no corpus - and no clock,
 * beyond the timestamp the caller hands in. Running it twice on unchanged inputs produces the same
 * report, byte for byte, which is what lets a revision of a transformation's report document mean
 * that something really changed.
 * </p>
 *
 * <h2>It derives; it does not review</h2>
 * <p>
 * Every category it asserts was asserted by a metamodel review first, and every citation is carried
 * over from the review it came from with its identifier, its quote and its source reference
 * untouched - only a clause saying how the data travels is appended to the citation's relevance.
 * The analyser therefore never looks a provision up and can never invent one, which is the standing
 * failure mode of anything that composes a citation instead of quoting it. The other side of that
 * is that a finding with nothing to carry cannot be raised at all: {@code Finding.evidence} is
 * mandatory, and a compliance record must not hold a claim nobody backed.
 * </p>
 *
 * <h2>Where there is no review, the report says so</h2>
 * <p>
 * A metamodel with no review is listed in the subject like any other, with its fingerprint and with
 * {@code reportId} unset - which is the machine-readable statement that the analysis of the flows
 * through it is incomplete rather than clean. It is not a finding, because there would be no
 * evidence to raise one with; it is a property of the subject, and the diagnostics derived from the
 * report turn it into an error on the compiled unit.
 * </p>
 */
final class FlowAnalysis {

	/**
	 * What the report says it is and is not. A GDPR report is decision support for a data
	 * protection officer; one produced mechanically says a little more than that, because a reader
	 * has to know that its ceiling is its rule table.
	 */
	static final String DISCLAIMER = "This report is derived mechanically from the compiled transformation and "
			+ "from the GDPR reviews of the metamodels it was compiled against. It classifies nothing itself: "
			+ "every category and every citation in it was established by one of those reviews and is carried "
			+ "over unchanged. It states where data travels and what the reviews say about it; it does not "
			+ "assert compliance or non-compliance, and it is not a substitute for a review of this "
			+ "transformation by a person. A rule nobody wrote down never fires, so an absence of findings is "
			+ "not a statement that there is nothing to find.";

	/** The GDPR itself. Not a citation - the corpus a report is against when no review named one. */
	private static final String GDPR_CELEX = "32016R0679";

	/** Where the target fields whose name says the value arrives as prose are recognised. */
	private static final Set<String> FREE_TEXT_NAMES = Set.of("comment", "comments", "note", "notes",
			"description", "remark", "remarks", "memo", "text", "freetext", "details", "info");

	/** The types a classification cannot survive in, because the field holds prose. */
	private static final Set<String> STRING_TYPES = Set.of("EString", "String");

	/**
	 * What joins the parts of a minted id. Anything but {@code #} or whitespace; see
	 * {@link #idOf(Flow)} for why those two are excluded.
	 */
	private static final String ID_SEPARATOR = "|";

	private static final ReportFactory REPORTS = ReportFactory.eINSTANCE;

	private static final ContextFactory CONTEXTS = ContextFactory.eINSTANCE;

	/** The taxonomy a context holds its combination kinds in. */
	private static final String COMBINATION_TAXONOMY = "combination-kinds";

	/** The only combination kind a flow analysis is in a position to assert. */
	private static final String LINKAGE = "LINKAGE";

	private FlowAnalysis() {
	}

	/**
	 * Derives the report.
	 *
	 * @param unit        the compiled unit as the registry stores it
	 * @param reviews     the review of each metamodel, by the {@code fp1:} fingerprint of the exact
	 *                    revision the unit was compiled against. A package with no entry here is
	 *                    one the analysis could not rest on, and the subject says so
	 * @param generatedAt when the analysis ran. It is the one thing that is not derived, and it is
	 *                    deliberately not part of the report's identity
	 * @return the report, whose subject is the transformation. Never {@code null}: a unit with no
	 *         flows and no reviews still produces a report, because "nothing was found" and
	 *         "nothing was looked at" have to be told apart by a reader
	 */
	static ComplianceReport analyse(CompiledUnit unit, Map<String, ComplianceReport> reviews, Instant generatedAt) {
		CompiledUnitManifest manifest = unit.getManifest();
		Extraction extraction = QvtFlowExtractor.extract(unit);
		Map<String, ReviewIndex> byNsURI = reviewsByPackage(manifest, reviews);

		ComplianceReport report = REPORTS.createComplianceReport();
		report.setName("GDPR flow analysis of " + manifest.getQualifiedName());
		report.setGeneratedAt(generatedAt.toString());
		report.setGeneratedBy(RuleCatalogue.VERSION);
		report.setOrigin(ReportOrigin.STATIC_ANALYSIS);
		report.setDisclaimer(DISCLAIMER);
		report.getContexts().addAll(contextsOf(byNsURI));
		report.setLanguage(languageOf(byNsURI));
		TransformationSubject subject = subjectOf(manifest, unit, extraction, reviews);
		report.setSubject(subject);
		report.setReportId(reportIdOf(manifest, subject, report.getContexts(), report.getLanguage()));

		evaluate(extraction, byNsURI, report);
		notPropagated(extraction, byNsURI, report);
		return report;
	}

	/* ------------------------------------------------------------------ the subject */

	/**
	 * What was reviewed and what the review rested on: the unit by its own {@code m2x1:}
	 * fingerprint, and every metamodel of its manifest pinned to the exact revision, split by
	 * whether the transformation reads it or writes it.
	 * <p>
	 * Every package of the manifest is listed, reviewed or not. A package the model parameters do
	 * not mention is listed as a source: the unit was compiled against it, so the analysis rested
	 * on it, and leaving it out would hide an unreviewed metamodel - which is the one thing the
	 * subject exists to make visible.
	 */
	private static TransformationSubject subjectOf(CompiledUnitManifest manifest, CompiledUnit unit,
			Extraction extraction, Map<String, ComplianceReport> reviews) {
		TransformationSubject subject = REPORTS.createTransformationSubject();
		subject.setQualifiedName(manifest.getQualifiedName());
		subject.setLanguage(manifest.getLanguage());
		subject.setSourceFingerprint(manifest.getSourceFingerprint());
		subject.setSubjectFingerprint(manifest.getUnitFingerprint());

		Map<String, String> names = packageNames(unit);
		for (PackageEntry entry : manifest.getPackageEntry()) {
			Direction direction = extraction.directions().getOrDefault(entry.getNsURI(), Direction.IN);
			if (direction != Direction.OUT) {
				subject.getSourcePackages().add(packageSubject(entry, names, reviews));
			}
			if (direction != Direction.IN) {
				subject.getTargetPackages().add(packageSubject(entry, names, reviews));
			}
		}
		return subject;
	}

	/**
	 * One metamodel revision the unit was compiled against.
	 * <p>
	 * {@code reportId} is left unset when no review of that revision exists. That is the report
	 * saying the analysis is incomplete, in the place the model reserved for it - not an omission.
	 */
	private static PackageSubject packageSubject(PackageEntry entry, Map<String, String> names,
			Map<String, ComplianceReport> reviews) {
		PackageSubject subject = REPORTS.createPackageSubject();
		subject.setNsURI(entry.getNsURI());
		subject.setName(names.get(entry.getNsURI()));
		subject.setSubjectFingerprint(entry.getFingerprint());
		ComplianceReport review = reviews.get(entry.getFingerprint());
		if (review != null) {
			subject.setReportId(review.getReportId());
		}
		return subject;
	}

	private static Map<String, String> packageNames(CompiledUnit unit) {
		Map<String, String> names = new LinkedHashMap<>();
		unit.getPackages().stream().filter(p -> p != null && p.getNsURI() != null)
				.forEach(p -> names.putIfAbsent(p.getNsURI(), p.getName()));
		return names;
	}

	/** The review of each metamodel, keyed by nsURI instead of by fingerprint. */
	private static Map<String, ReviewIndex> reviewsByPackage(CompiledUnitManifest manifest,
			Map<String, ComplianceReport> reviews) {
		Map<String, ReviewIndex> byNsURI = new LinkedHashMap<>();
		for (PackageEntry entry : manifest.getPackageEntry()) {
			ComplianceReport review = reviews.get(entry.getFingerprint());
			if (review != null) {
				byNsURI.putIfAbsent(entry.getNsURI(), new ReviewIndex(review));
			}
		}
		return byNsURI;
	}

	/**
	 * The contexts this report is against: the ones the reviews were made against.
	 * <p>
	 * Carried over rather than chosen, because every citation in this report came out of one of
	 * those reviews - a report cannot claim to be against a context it never read. The union of
	 * what the reviews cite, de-duplicated on id and version, so a transformation whose two
	 * metamodels were reviewed against the same regulation names it once.
	 *
	 * @param byNsURI the reviews this analysis rests on
	 * @return the distinct contexts, in the order they were first met; empty when no review carried
	 *         one, which is a report that may not be written - see
	 *         {@link TransformationAnalysis#analyse}
	 */
	private static List<ContextRef> contextsOf(Map<String, ReviewIndex> byNsURI) {
		Map<String, ContextRef> distinct = new LinkedHashMap<>();
		for (ReviewIndex review : byNsURI.values()) {
			for (ContextRef carried : review.contexts()) {
				if (carried.getContextId() == null || carried.getContextId().isBlank()) {
					continue;
				}
				ContextRef ref = CONTEXTS.createContextRef();
				ref.setContextId(carried.getContextId());
				ref.setContextVersion(carried.getContextVersion());
				distinct.putIfAbsent(ref.getContextId() + ID_SEPARATOR + ref.getContextVersion(), ref);
			}
		}
		return new ArrayList<>(distinct.values());
	}

	/**
	 * The language this report writes its prose in: the one the reviews it carried its sentences
	 * from were written in.
	 * <p>
	 * The analyser composes no prose of its own beyond the rule catalogue's templates, and every
	 * rationale it carries over is in the language of the review it came from. Several languages
	 * among the reviews leave it unset rather than picking one - a document that claims a language
	 * it is only half in is worse than one that claims none.
	 */
	private static String languageOf(Map<String, ReviewIndex> byNsURI) {
		Set<String> languages = new LinkedHashSet<>();
		for (ReviewIndex review : byNsURI.values()) {
			if (review.language() != null && !review.language().isBlank()) {
				languages.add(review.language().trim().toUpperCase(Locale.ROOT));
			}
		}
		return languages.size() == 1 ? languages.iterator().next() : null;
	}

	/**
	 * Records a data category on a finding, as a reference into the taxonomy of the contexts the
	 * reviews named. Nothing is recorded when the analysis has no category to state.
	 */
	private static void dataCategory(Finding finding, String categoryId) {
		if (categoryId == null) {
			return;
		}
		finding.getCategories().add(categoryRef(ReviewIndex.TAXONOMY, categoryId));
	}

	/**
	 * Records how a combination combines. Always {@code LINKAGE}: the analyser raises a combination
	 * when several classified source fields reach one target field, which is what makes them
	 * joinable, and it is not in a position to tell that from profiling or from an inference about
	 * a special category - those are a reviewer's readings, and this one looks only at flows.
	 */
	private static void combinationKind(CombinationFinding combination, Map<String, ReviewIndex> byNsURI) {
		combination.getCategories().add(categoryRef(COMBINATION_TAXONOMY, LINKAGE));
	}

	private static CategoryRef categoryRef(String taxonomyId, String categoryId) {
		CategoryRef ref = CONTEXTS.createCategoryRef();
		ref.setTaxonomyId(taxonomyId);
		ref.setCategoryId(categoryId);
		return ref;
	}

	/**
	 * The report's identity: stable while the inputs are, different as soon as one of them moved.
	 * <p>
	 * Stable, because the analyser replays over every stored unit on start-up and an identity that
	 * moved each time would add a revision to the transformation's history document saying nothing.
	 * Different when an input changed, because a corrected metamodel review must not silently
	 * overwrite the analysis that preceded it - the two are successive revisions of a judgement,
	 * and the change sheet between them is the point of the document.
	 * <p>
	 * So it covers the unit's fingerprint, every review it rested on, and the rule catalogue that
	 * wrote it. The generation time is deliberately not in it: re-running the analysis unchanged is
	 * not a new judgement.
	 */
	private static String reportIdOf(CompiledUnitManifest manifest, TransformationSubject subject,
			List<ContextRef> contexts, String reportLanguage) {
		StringBuilder inputs = new StringBuilder(RuleCatalogue.VERSION);
		List<PackageSubject> rested = new ArrayList<>(subject.getSourcePackages());
		rested.addAll(subject.getTargetPackages());
		rested.stream().map(p -> p.getNsURI() + "|" + p.getSubjectFingerprint() + "|" + p.getReportId()).sorted()
				.forEach(entry -> inputs.append('\n').append(entry));
		contexts.stream().map(c -> c.getContextId() + "@" + c.getContextVersion()).sorted()
				.forEach(entry -> inputs.append('\n').append(entry));
		String language = reportLanguage == null || reportLanguage.isBlank() ? "und"
				: reportLanguage.toLowerCase();
		return "gdpr-flow-" + cut(digestOf(manifest.getUnitFingerprint()), 40) + "-"
				+ cut(sha256(inputs.toString()), 16) + "-" + language;
	}

	/**
	 * The digest half of a {@code <scheme>:<digest>} fingerprint.
	 * <p>
	 * The scheme is dropped because the report id already says what kind of thing it identifies,
	 * and a colon in an objectId is one more thing to escape.
	 */
	private static String digestOf(String fingerprint) {
		if (fingerprint == null) {
			return "";
		}
		int scheme = fingerprint.indexOf(':');
		return scheme < 0 ? fingerprint : fingerprint.substring(scheme + 1);
	}

	private static String cut(String value, int length) {
		return value.length() <= length ? value : value.substring(0, length);
	}

	private static String sha256(String value) {
		try {
			byte[] hash = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder(hash.length * 2);
			for (byte b : hash) {
				hex.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
			}
			return hex.toString();
		} catch (NoSuchAlgorithmException e) {
			// Every Java runtime has SHA-256; a runtime without one cannot produce a report whose
			// identity means anything, so this is not something to carry on from.
			throw new IllegalStateException("SHA-256 is not available, so no report identity can be computed", e);
		}
	}

	/* ------------------------------------------------------------------ the rules */

	/**
	 * Turns the flows into evaluations and applies the rules to each target field.
	 * <p>
	 * The unit of judgement is the field a flow ends in rather than the flow itself: aggregation,
	 * structure loss and a disagreeing target review are all statements about what arrives
	 * somewhere, and they are only visible once everything arriving there is together.
	 */
	private static void evaluate(Extraction extraction, Map<String, ReviewIndex> byNsURI, ComplianceReport report) {
		Map<String, List<Flow>> byTarget = new LinkedHashMap<>();
		Map<Flow, FlowEvaluation> evaluations = new LinkedHashMap<>();
		for (Flow flow : extraction.flows()) {
			FlowEvaluation evaluation = evaluationOf(flow, byNsURI);
			evaluations.put(flow, evaluation);
			report.getEvaluations().add(evaluation);
			byTarget.computeIfAbsent(flow.mapping() + "->" + flow.targetKey(), key -> new ArrayList<>()).add(flow);
		}
		for (List<Flow> arriving : byTarget.values()) {
			apply(arriving, evaluations, byNsURI, report);
		}
	}

	/** One flow as the report records it, with what the source review says about its origin. */
	private static FlowEvaluation evaluationOf(Flow flow, Map<String, ReviewIndex> byNsURI) {
		FlowEvaluation evaluation = REPORTS.createFlowEvaluation();
		evaluation.setId(idOf(flow));
		evaluation.setName(flow.sourceFeature() == null ? flow.targetFeature()
				: flow.sourceFeature() + " -> " + flow.targetFeature());
		evaluation.setMapping(flow.mapping());
		evaluation.setSourceNsURI(flow.sourceNsURI());
		evaluation.setSourceFeature(flow.sourceFeature());
		evaluation.setTargetNsURI(flow.targetNsURI());
		evaluation.setTargetFeature(flow.targetFeature());
		evaluation.setFlowKind(flow.kind());
		FeatureEvaluation source = featureOf(flow, byNsURI);
		if (source != null) {
			// Carried over, not decided here: what the source review said about the feature is what
			// travels along the flow.
			evaluation.setRelevanceLevel(source.getRelevanceLevel());
			evaluation.setPurpose(source.getPurpose());
		}
		return evaluation;
	}

	/**
	 * How an evaluation of this report is identified.
	 * <p>
	 * <b>Never a {@code #}.</b> {@code Evaluation.id} is an EMF ID attribute, so a non-containment
	 * reference to one - {@code CombinationFinding.features} is the only one, and every combination
	 * uses it - is serialised as a space-separated list of these very strings. A token containing a
	 * {@code #} is read back as a {@code <resource>#<fragment>} cross-document href rather than as
	 * an id, and the loader then tries to instantiate the declared type to proxy it:
	 * {@code Evaluation}, which is abstract. The report writes, and is unreadable from then on.
	 * <p>
	 * Whitespace is out for the same reason - it is what separates the list - which leaves the
	 * separator below. The feature fragments inside it are safe: only {@code #} and whitespace mean
	 * anything to the reader.
	 */
	private static String idOf(Flow flow) {
		return flow.mapping() + ID_SEPARATOR + (flow.sourceFeature() == null ? "?" : flow.sourceFeature()) + "->"
				+ flow.targetFeature();
	}

	private static FeatureEvaluation featureOf(Flow flow, Map<String, ReviewIndex> byNsURI) {
		ReviewIndex review = flow.sourceNsURI() == null ? null : byNsURI.get(flow.sourceNsURI());
		return review == null ? null : review.feature(flow.sourceFeature());
	}

	/**
	 * Everything the rules have to say about one target field, given everything that arrives in it.
	 */
	private static void apply(List<Flow> arriving, Map<Flow, FlowEvaluation> evaluations,
			Map<String, ReviewIndex> byNsURI, ComplianceReport report) {
		Flow any = arriving.get(0);
		List<Flow> classified = new ArrayList<>();
		String worst = null;
		RelevanceLevel relevance = null;
		for (Flow flow : arriving) {
			String category = ReviewIndex.worstCategory(featureOf(flow, byNsURI));
			if (category != null && ReviewIndex.PERSONAL.contains(category)) {
				classified.add(flow);
				worst = ReviewIndex.stronger(worst, category);
				relevance = ReviewIndex.higher(relevance,
						featureOf(flow, byNsURI).getRelevanceLevel());
			}
			if (flow.kind() == FlowKind.OPAQUE && category != null && ReviewIndex.PERSONAL.contains(category)) {
				add(evaluations.get(flow), finding(Rule.OPAQUE_FLOW, flow, category,
						featureOf(flow, byNsURI).getRelevanceLevel(), byNsURI, List.of(flow)));
			}
		}
		if (classified.isEmpty()) {
			return;
		}

		// PROPAGATION, per flow: the target field holds at least what the source review said, and
		// the target model's own review has no way of knowing that.
		for (Flow flow : classified) {
			FeatureEvaluation source = featureOf(flow, byNsURI);
			add(evaluations.get(flow), finding(Rule.PROPAGATION, flow, ReviewIndex.worstCategory(source),
					source.getRelevanceLevel(), byNsURI, List.of(flow)));
		}

		// AGGREGATION: the motivating case. It is a CombinationFinding and not a finding on any one
		// flow, because it is true of the flows together and of none of them alone - which is also
		// why a reader sees the flows nested under it rather than beside it.
		if (classified.size() > 1) {
			CombinationFinding combination = REPORTS.createCombinationFinding();
			combinationKind(combination, byNsURI);
			classified.forEach(flow -> combination.getFeatures().add(evaluations.get(flow)));
			if (fill(combination, Rule.AGGREGATION, any, worst, relevance, byNsURI, classified)) {
				report.getCombinations().add(combination);
			}
		}

		// STRUCTURE_LOSS: the classification cannot live in the field the data arrives in, so the
		// next review of the target model sees an unremarkable string.
		if (losesStructure(any, classified)) {
			addTargetFinding(Rule.STRUCTURE_LOSS, any, worst, relevance, byNsURI, classified, evaluations, report);
		}

		// TARGET_DISAGREEMENT: two reviews describe one field differently. It fires on any weaker
		// target category and not only on NOT_PERSONAL_DATA - a DIRECT_IDENTIFIER landing in a
		// field the target review calls PERSONAL_DATA is a downgrade and went unseen while the rule
		// was written for the extreme case alone.
		String target = ReviewIndex.worstCategory(targetFeatureOf(any, byNsURI));
		if (target != null && ReviewIndex.weakerThan(target, worst)) {
			addTargetFinding(Rule.TARGET_DISAGREEMENT, any, worst, relevance, byNsURI, classified, evaluations,
					report, Map.of("targetCategory", target));
		}

		// PURPOSE_NOT_CARRIED: a purpose is per processing, and the transformation is a new one.
		FeatureEvaluation targetFeature = targetFeatureOf(any, byNsURI);
		boolean targetStatesPurpose = targetFeature != null && targetFeature.getPurpose() != null
				&& !targetFeature.getPurpose().isBlank();
		if (!targetStatesPurpose) {
			for (Flow flow : classified) {
				FeatureEvaluation source = featureOf(flow, byNsURI);
				if (source.getPurpose() == null || source.getPurpose().isBlank()) {
					continue;
				}
				add(evaluations.get(flow),
						finding(Rule.PURPOSE_NOT_CARRIED, flow, ReviewIndex.worstCategory(source),
								source.getRelevanceLevel(), byNsURI, List.of(flow),
								Map.of("purpose", source.getPurpose())));
			}
		}
	}

	/**
	 * Whether the receiving field can carry the classification of what arrives in it.
	 * <p>
	 * It cannot when the value arrives as prose: a string field whose name says it holds free text,
	 * or any string field several values were concatenated into. Both are the same fact - a value
	 * that was a classified field is now a substring of a sentence - and neither is something the
	 * target metamodel can express.
	 */
	private static boolean losesStructure(Flow target, List<Flow> classified) {
		if (target.targetTypeName() == null || !STRING_TYPES.contains(target.targetTypeName())) {
			return false;
		}
		String name = target.targetFeature() == null ? "" : target.targetFeature();
		name = name.substring(name.lastIndexOf('/') + 1).toLowerCase();
		return FREE_TEXT_NAMES.contains(name)
				|| classified.stream().anyMatch(flow -> flow.kind() == FlowKind.CONCATENATION);
	}

	private static FeatureEvaluation targetFeatureOf(Flow flow, Map<String, ReviewIndex> byNsURI) {
		ReviewIndex review = flow.targetNsURI() == null ? null : byNsURI.get(flow.targetNsURI());
		return review == null ? null : review.feature(flow.targetFeature());
	}

	/**
	 * A statement about the target field: recorded as a combination when several flows made it
	 * true, and on the single flow when only one did.
	 */
	private static void addTargetFinding(Rule rule, Flow target, String worst, RelevanceLevel relevance,
			Map<String, ReviewIndex> byNsURI, List<Flow> classified, Map<Flow, FlowEvaluation> evaluations,
			ComplianceReport report) {
		addTargetFinding(rule, target, worst, relevance, byNsURI, classified, evaluations, report, Map.of());
	}

	private static void addTargetFinding(Rule rule, Flow target, String worst, RelevanceLevel relevance,
			Map<String, ReviewIndex> byNsURI, List<Flow> classified, Map<Flow, FlowEvaluation> evaluations,
			ComplianceReport report, Map<String, String> extra) {
		if (classified.size() == 1) {
			Flow only = classified.get(0);
			add(evaluations.get(only), finding(rule, only, worst, relevance, byNsURI, classified, extra));
			return;
		}
		CombinationFinding combination = REPORTS.createCombinationFinding();
		combinationKind(combination, byNsURI);
		classified.forEach(flow -> combination.getFeatures().add(evaluations.get(flow)));
		if (fill(combination, rule, target, worst, relevance, byNsURI, classified, extra)) {
			report.getCombinations().add(combination);
		}
	}

	/* ------------------------------------------------------------------ not propagated */

	/**
	 * One finding per source metamodel for everything classified that no mapping reads.
	 * <p>
	 * Aggregated deliberately: one finding per unread feature buries a real metamodel under INFO
	 * rows - three classifiers produced eight of them in the probe - and the statement worth making
	 * is about the set, not about each field. It is positive evidence for a data-minimisation
	 * argument rather than a risk, which is what its relevance says.
	 */
	private static void notPropagated(Extraction extraction, Map<String, ReviewIndex> byNsURI, ComplianceReport report) {
		for (Map.Entry<String, ReviewIndex> entry : byNsURI.entrySet()) {
			String nsURI = entry.getKey();
			if (extraction.directions().get(nsURI) == Direction.OUT) {
				// Nothing is read from a pure target model, so nothing being read from it says
				// nothing.
				continue;
			}
			ReviewIndex review = entry.getValue();
			List<String> unread = new ArrayList<>();
			List<FeatureEvaluation> unreadFeatures = new ArrayList<>();
			int classified = 0;
			for (String fragment : review.fragments()) {
				FeatureEvaluation feature = review.feature(fragment);
				String category = ReviewIndex.worstCategory(feature);
				if (category == null || !ReviewIndex.PERSONAL.contains(category)) {
					continue;
				}
				classified++;
				if (!extraction.readFeatures().contains(nsURI + "#" + fragment)) {
					unread.add(fragment);
					unreadFeatures.add(feature);
				}
			}
			if (unread.isEmpty()) {
				continue;
			}
			FlowEvaluation evaluation = REPORTS.createFlowEvaluation();
			evaluation.setId("not-propagated" + ID_SEPARATOR + nsURI);
			evaluation.setName("features of " + nsURI + " that no mapping reads");
			evaluation.setSourceNsURI(nsURI);
			Finding finding = REPORTS.createFinding();
			finding.setId(Rule.NOT_PROPAGATED.code() + ":" + nsURI);
			dataCategory(finding, unreadFeatures.stream().map(ReviewIndex::worstCategory)
					.reduce(null, ReviewIndex::stronger));
			// Not a risk: it is the absence of one, so it is recorded at the lowest level that is
			// still a statement.
			finding.setRelevanceLevel(RelevanceLevel.LOW);
			finding.getDetectedBy().add(DetectionSignal.TRANSFORMATION_FLOW);
			finding.setRationale(RuleCatalogue.rationale(Rule.NOT_PROPAGATED, Map.of(//
					"nSources", String.valueOf(unread.size()), //
					"nClassified", String.valueOf(classified), //
					"nsURI", nsURI, //
					"sourceList", String.join(", ", unread))));
			for (int i = 0; i < unread.size(); i++) {
				String fragment = unread.get(i);
				carry(finding, ReviewIndex.evidenceOf(unreadFeatures.get(i)), Rule.NOT_PROPAGATED,
						Map.of("sourceFeature", fragment));
			}
			if (finding.getEvidence().isEmpty()) {
				continue;
			}
			evaluation.getFindings().add(finding);
			report.getEvaluations().add(evaluation);
		}
	}

	/* ------------------------------------------------------------------ building a finding */

	private static Finding finding(Rule rule, Flow flow, String category, RelevanceLevel relevance,
			Map<String, ReviewIndex> byNsURI, List<Flow> contributing) {
		return finding(rule, flow, category, relevance, byNsURI, contributing, Map.of());
	}

	/**
	 * A finding of one rule, or {@code null} when the reviews it would rest on carry no citation to
	 * carry over. The model requires evidence, and a claim nobody backed does not belong in a
	 * compliance record - so it is dropped rather than invented.
	 */
	private static Finding finding(Rule rule, Flow flow, String category, RelevanceLevel relevance,
			Map<String, ReviewIndex> byNsURI, List<Flow> contributing, Map<String, String> extra) {
		Finding finding = REPORTS.createFinding();
		finding.setId(rule.code() + ":" + idOf(flow));
		return fill(finding, rule, flow, category, relevance, byNsURI, contributing, extra) ? finding : null;
	}

	private static boolean fill(Finding finding, Rule rule, Flow flow, String category,
			RelevanceLevel relevance, Map<String, ReviewIndex> byNsURI, List<Flow> contributing) {
		return fill(finding, rule, flow, category, relevance, byNsURI, contributing, Map.of());
	}

	/**
	 * Writes the rule's sentence and carries its citations over. Answers whether anything could be
	 * carried: without a citation the finding cannot exist.
	 */
	private static boolean fill(Finding finding, Rule rule, Flow flow, String category,
			RelevanceLevel relevance, Map<String, ReviewIndex> byNsURI, List<Flow> contributing,
			Map<String, String> extra) {
		if (finding.getId() == null) {
			finding.setId(rule.code() + ":" + flow.mapping() + ID_SEPARATOR + flow.targetFeature());
		}
		dataCategory(finding, category);
		finding.setRelevanceLevel(relevance);
		// The signal is the transformation itself: the analyser read no name, no type and no
		// documentation, it read where the value goes.
		finding.getDetectedBy().add(DetectionSignal.TRANSFORMATION_FLOW);
		Map<String, String> facts = new LinkedHashMap<>(facts(flow, category, contributing));
		facts.putAll(extra);
		finding.setRationale(RuleCatalogue.rationale(rule, facts));
		for (Flow contributor : contributing) {
			Map<String, String> clause = new LinkedHashMap<>(facts);
			clause.put("sourceFeature", contributor.sourceFeature() == null ? "an unnamed value"
					: contributor.sourceFeature());
			clause.put("nOthers", String.valueOf(contributing.size() - 1));
			carry(finding, ReviewIndex.evidenceOf(featureOf(contributor, byNsURI)), rule, clause);
		}
		return !finding.getEvidence().isEmpty();
	}

	/** The facts every template may ask for about one target field. */
	private static Map<String, String> facts(Flow flow, String category, List<Flow> contributing) {
		Map<String, String> facts = new LinkedHashMap<>();
		facts.put("mapping", flow.mapping() == null ? "an unnamed mapping" : flow.mapping());
		facts.put("sourceFeature", flow.sourceFeature() == null ? "an unnamed value" : flow.sourceFeature());
		facts.put("targetFeature", flow.targetFeature() == null ? "an unresolved field" : flow.targetFeature());
		facts.put("flowKind", flow.kind() == null ? FlowKind.OPAQUE.getName() : flow.kind().getName());
		facts.put("category", category == null ? "NOT_PERSONAL_DATA" : category);
		facts.put("nSources", String.valueOf(contributing.size()));
		facts.put("nOthers", String.valueOf(Math.max(contributing.size() - 1, 0)));
		Set<String> sources = new LinkedHashSet<>();
		contributing.stream().map(Flow::sourceFeature).filter(f -> f != null).forEach(sources::add);
		facts.put("sourceList", sources.isEmpty() ? "an unnamed value" : String.join(", ", sources));
		return facts;
	}

	/**
	 * Carries the citations of one review finding into this one.
	 * <p>
	 * The identifier, the quote and the source reference are copied unchanged - they were checked
	 * when the review was written and they still say exactly what they said. Only {@code relevance}
	 * grows, by the one clause saying how the data this provision is about travels here. Duplicates
	 * are dropped: several review findings on one feature routinely cite the same provision.
	 */
	private static void carry(Finding finding, List<Evidence> inherited, Rule rule, Map<String, String> facts) {
		for (Evidence source : inherited) {
			if (source.getCitationId() == null || source.getQuote() == null) {
				continue;
			}
			boolean already = finding.getEvidence().stream()
					.anyMatch(existing -> source.getCitationId().equals(existing.getCitationId())
							&& source.getQuote().equals(existing.getQuote()));
			if (already) {
				continue;
			}
			Evidence evidence = REPORTS.createEvidence();
			evidence.setCitationId(source.getCitationId());
			evidence.setQuote(source.getQuote());
			evidence.setSourceRef(source.getSourceRef());
			evidence.setVerbatim(source.isVerbatim());
			String clause = RuleCatalogue.relevanceClause(rule, facts);
			evidence.setRelevance(source.getRelevance() == null || source.getRelevance().isBlank() ? clause
					: source.getRelevance().trim() + " " + clause);
			finding.getEvidence().add(evidence);
		}
	}

	private static void add(FlowEvaluation evaluation, Finding finding) {
		if (evaluation != null && finding != null) {
			evaluation.getFindings().add(finding);
		}
	}
}
