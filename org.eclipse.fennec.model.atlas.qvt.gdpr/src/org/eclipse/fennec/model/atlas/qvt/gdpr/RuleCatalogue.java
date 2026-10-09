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

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The rules the analyser knows and the sentences it writes them in.
 * <p>
 * Every rationale and every appended relevance clause is a template authored once and filled with
 * facts read off the compiled unit and the metamodel reviews. Nothing here is generated per case,
 * which is what makes the analyser deterministic: the same unit and the same reviews produce
 * byte-identical text, so a difference between two revisions of a transformation report is a real
 * change and not a rewording. No language model could offer that, and a compliance document that
 * is diffed needs it.
 * <p>
 * <b>The catalogue is the compliance artefact.</b> It is reviewed once, versioned, and its
 * {@link #VERSION} goes into every report it produced - so a report can be traced to the exact
 * table of rules that wrote it.
 * <p>
 * <b>Its ceiling is its table.</b> The analyser will never fire a rule nobody wrote down, which is
 * why {@link Rule#OPAQUE_FLOW} and an unreviewed source have to be loud: they are the only honest
 * substitute for a judgement it cannot make.
 * <p>
 * The catalogue deliberately cites nothing of its own. Every citation in a report comes from the
 * review the finding was carried over from, verbatim, so the analyser cannot invent a provision -
 * the standing failure mode when one is composed rather than looked up.
 */
final class RuleCatalogue {

	/** Which table of rules wrote a report. It goes into {@code ComplianceReport.generatedBy}. */
	static final String VERSION = "qvt-flow-analysis/1";

	/**
	 * Which metamodel of a transformation a rule's statement is about, and therefore which one it
	 * is written onto.
	 * <p>
	 * A transformation report is about the transformation, and the compiled unit carries all of it.
	 * But a person maintaining one of the metamodels will never read that report, and half of what
	 * it says is directly about their model - so those halves are written there too, and the split
	 * is the whole point of the enum.
	 */
	enum Reach {

		/** Read by the transformation: a statement about a field whose data goes elsewhere. */
		SOURCE,

		/** Written by the transformation: a statement about a field that receives data. */
		TARGET,

		/** Both ends have something to do about it, and what they would do differs. */
		BOTH;

		boolean touchesSource() {
			return this != TARGET;
		}

		boolean touchesTarget() {
			return this != SOURCE;
		}
	}

	/** What the analyser can conclude from a flow and the reviews of its two ends. */
	enum Rule {

		/**
		 * A classified source feature reaches a target feature. It is a statement for the
		 * <em>receiver</em>: that field holds at least that category whether or not the model it
		 * lives in says so. Not written on the source as well - a model read by several
		 * transformations would collect one of these per flow of each of them, which buries the
		 * findings that ask for a decision.
		 */
		PROPAGATION(Reach.TARGET),

		/**
		 * Two or more classified source features reach one target feature. The motivating case,
		 * and a statement for the receiver: classification, pseudonymisation and erasure all
		 * operate per field, so the combined field cannot be treated as any one of its parts.
		 */
		AGGREGATION(Reach.TARGET),

		/**
		 * Every member of a combination a metamodel review raised reaches one class of the target
		 * model. A statement for the receiver: the set the reviewer described is rebuilt there, in
		 * one record, and the target model's own review has no way of knowing that the fields it
		 * sees separately arrived together.
		 * <p>
		 * It is the counterpart of {@link #AGGREGATION} for a transformation that copies field by
		 * field. Aggregation is about several values meeting in one <em>field</em>, which the
		 * analyser can see for itself; this one is about several values meeting in one
		 * <em>record</em>, which it cannot - that they belong together is the reviewer's
		 * judgement, and it is carried over rather than made here.
		 */
		COMBINATION_CARRIED(Reach.TARGET),

		/**
		 * Classified data lands where the classification cannot be carried. Both ends, because
		 * both have something to do and the two things differ: the target model cannot express
		 * what it now holds, and the source model's field is being flattened into prose somewhere
		 * it has no say over.
		 */
		STRUCTURE_LOSS(Reach.BOTH),

		/**
		 * The target review classifies the receiving field more weakly than what arrives in it.
		 * Two reviews describe one field differently, and it is the target model's review that a
		 * person has to look at again.
		 */
		TARGET_DISAGREEMENT(Reach.TARGET),

		/**
		 * The source states a purpose for the data and the target field states none. The purpose
		 * is the source review's, so the question - is this new processing compatible with it? -
		 * belongs to whoever stated it.
		 */
		PURPOSE_NOT_CARRIED(Reach.SOURCE),

		/**
		 * A classified source feature that no mapping reads. Evidence for a minimisation argument
		 * about the source model, and about nothing else.
		 */
		NOT_PROPAGATED(Reach.SOURCE),

		/**
		 * The analyser could not follow the value, or could not resolve where it lands. It is
		 * written where the data came from, because that is the end that is known: the whole
		 * content of the finding is that where it goes is not.
		 */
		OPAQUE_FLOW(Reach.SOURCE);

		private final Reach reach;

		Rule(Reach reach) {
			this.reach = reach;
		}

		/** The code a finding of this rule carries, and the id it is addressed by. */
		String code() {
			return "gdpr.flow." + name().toLowerCase().replace('_', '-');
		}

		/** Which of a transformation's metamodels this rule's statement is about. */
		Reach reach() {
			return reach;
		}

		/**
		 * The rule a stored finding came from, read off its id, or {@code null} for an id this
		 * catalogue did not write.
		 * <p>
		 * The report model has no field for the rule - a {@code Finding} carries a category and a
		 * rationale, not the reason it was raised - so the id is where the producer and a later
		 * reader have to agree. Every finding this catalogue writes is identified
		 * {@code <code>:<what it is about>}, and that prefix is the contract.
		 * <p>
		 * A finding whose id says nothing is <b>not</b> dropped: a report may have been written by
		 * hand or restored from a backup, and not knowing which end a statement belongs to is no
		 * reason to write it nowhere. It goes to both.
		 */
		static Rule of(String findingId) {
			if (findingId == null) {
				return null;
			}
			int end = findingId.indexOf(':');
			String code = end < 0 ? findingId : findingId.substring(0, end);
			for (Rule rule : values()) {
				if (rule.code().equals(code)) {
					return rule;
				}
			}
			return null;
		}
	}

	private static final Map<Rule, String> RATIONALE = Map.of(//
			Rule.PROPAGATION, //
			"The source review classifies {sourceFeature} as {category}. The mapping {mapping} carries it "
					+ "into {targetFeature} ({flowKind}), so that field holds data of at least that category "
					+ "whether or not its own model says so.",
			Rule.AGGREGATION, //
			"{nSources} classified source features - {sourceList} - are combined into the single field "
					+ "{targetFeature} by the mapping {mapping} ({flowKind}). Classification, "
					+ "pseudonymisation and erasure all operate per field, so a combined field cannot be "
					+ "treated as any one of its parts.",
			Rule.COMBINATION_CARRIED, //
			"The review of {nsURI} raised a combination over {nSources} features - {sourceList} - and the "
					+ "mapping {mapping} carries every one of them into {targetClass}. The set the reviewer "
					+ "described is rebuilt there, in one record of the target model, although no single field "
					+ "of it carries the set's classification.",
			Rule.STRUCTURE_LOSS, //
			"{targetFeature} receives data the source review classifies as {category}, but it cannot carry "
					+ "that classification: the value arrives in it as free text by way of the mapping "
					+ "{mapping} ({flowKind}). The next review of the target model sees an unremarkable "
					+ "string.",
			Rule.TARGET_DISAGREEMENT, //
			"The target review classifies {targetFeature} as {targetCategory}, but the mapping {mapping} "
					+ "carries data the source review classifies as {category} into it. The two reviews "
					+ "describe the same field differently and a person has to decide which holds.",
			Rule.PURPOSE_NOT_CARRIED, //
			"The source review states a purpose for {sourceFeature} - \"{purpose}\" - and the target field "
					+ "{targetFeature} states none. The mapping {mapping} is itself a processing operation, "
					+ "so the purpose of the data in its new place is not recorded anywhere.",
			Rule.NOT_PROPAGATED, //
			"{nSources} of the {nClassified} classified features of {nsURI} are read by no mapping of this "
					+ "transformation: {sourceList}. They do not reach the target model.",
			Rule.OPAQUE_FLOW, //
			"The mapping {mapping} writes {targetFeature} from a value the analyser cannot follow "
					+ "({flowKind}). Whether {sourceFeature} arrives there unchanged, transformed or not at "
					+ "all is not decidable from the compiled unit, so this flow is recorded rather than "
					+ "judged.");

	/**
	 * What is appended to a carried-over citation's {@code relevance}.
	 * <p>
	 * The citation itself - identifier, quote and source reference - is kept byte-identical, so it
	 * still says exactly what the reviewer checked. This clause is the one thing the analyser adds:
	 * how the data the provision is about travels in this transformation.
	 */
	private static final Map<Rule, String> RELEVANCE = Map.of(//
			Rule.PROPAGATION, //
			"Carried over from the review of {sourceFeature}, which this transformation reads and writes "
					+ "into {targetFeature}.",
			Rule.AGGREGATION, //
			"Carried over from the review of {sourceFeature}, which this transformation combines with "
					+ "{nOthers} other classified features into {targetFeature}.",
			Rule.COMBINATION_CARRIED, //
			"Carried over from the review of {nsURI}, which raised this combination over {sourceList}; the "
					+ "mapping {mapping} carries all of them into {targetClass}.",
			Rule.STRUCTURE_LOSS, //
			"Carried over from the review of {sourceFeature}, whose classification the receiving field "
					+ "{targetFeature} cannot express.",
			Rule.TARGET_DISAGREEMENT, //
			"Carried over from the review of {sourceFeature}, which arrives in {targetFeature} despite the "
					+ "target review classifying that field differently.",
			Rule.PURPOSE_NOT_CARRIED, //
			"Carried over from the review of {sourceFeature}, whose stated purpose does not travel with "
					+ "the data into {targetFeature}.",
			Rule.NOT_PROPAGATED, //
			"Carried over from the review of {sourceFeature}, which this transformation does not read.",
			Rule.OPAQUE_FLOW, //
			"Carried over from the review of {sourceFeature}, which this transformation reads into a value "
					+ "the analyser could not follow.");

	private static final Pattern SLOT = Pattern.compile("\\{([a-zA-Z]+)\\}");

	private RuleCatalogue() {
	}

	/** The sentence that says why a finding of this rule exists. */
	static String rationale(Rule rule, Map<String, String> facts) {
		return render(rule, RATIONALE.get(rule), facts);
	}

	/** The clause appended to each citation carried over into a finding of this rule. */
	static String relevanceClause(Rule rule, Map<String, String> facts) {
		return render(rule, RELEVANCE.get(rule), facts);
	}

	/**
	 * Fills a template's slots, and refuses to produce a half-written sentence.
	 * <p>
	 * A slot the caller did not supply is a bug in the analyser, not a gap to paper over with an
	 * empty string: the sentence would reach a compliance document reading "the mapping  carries".
	 * Failing here costs one report and is visible; the alternative is not.
	 */
	private static String render(Rule rule, String template, Map<String, String> facts) {
		if (template == null) {
			throw new IllegalStateException("No template for rule " + rule + " in catalogue " + VERSION);
		}
		Matcher matcher = SLOT.matcher(template);
		StringBuilder rendered = new StringBuilder();
		while (matcher.find()) {
			String slot = matcher.group(1);
			String value = facts.get(slot);
			if (value == null || value.isBlank()) {
				throw new IllegalStateException(String.format(
						"The %s template of catalogue %s has no value for the slot '{%s}'; the sentence would "
								+ "be written with a hole in it.",
						rule, VERSION, slot));
			}
			matcher.appendReplacement(rendered, Matcher.quoteReplacement(value));
		}
		matcher.appendTail(rendered);
		return rendered.toString();
	}
}
