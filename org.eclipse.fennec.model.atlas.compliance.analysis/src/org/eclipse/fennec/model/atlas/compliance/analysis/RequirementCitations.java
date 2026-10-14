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
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.compliance.context.ComplianceContext;
import org.eclipse.fennec.model.compliance.context.Requirement;
import org.eclipse.fennec.model.compliance.context.RequirementGroup;
import org.eclipse.fennec.model.compliance.corpus.Citable;
import org.eclipse.fennec.model.compliance.corpus.Control;
import org.eclipse.fennec.model.compliance.corpus.ControlGroup;
import org.eclipse.fennec.model.compliance.corpus.ControlParameter;
import org.eclipse.fennec.model.compliance.corpus.ControlPart;
import org.eclipse.fennec.model.compliance.corpus.Corpus;
import org.eclipse.fennec.model.compliance.corpus.DocumentClause;
import org.eclipse.fennec.model.compliance.corpus.DocumentSection;
import org.eclipse.fennec.model.compliance.corpus.LegalUnit;
import org.eclipse.fennec.model.compliance.report.Evidence;
import org.eclipse.fennec.model.compliance.report.ReportFactory;

/**
 * The evidence for a statement an analyser makes of its own: a requirement id in, the verbatim text
 * of the provision it cites out.
 *
 * <p>
 * There are two honest ways to fill {@code Finding.evidence}, and a finding uses one or the other.
 * A statement <em>derived</em> from a review - this classified value reaches that field - carries
 * the review's own citation over, which is {@link ReviewIndex#evidenceOf}. A <em>control</em>
 * statement - this column is stored unencrypted, this resource has no retention rule - is made
 * against a requirement rather than copied from a review, and no review of a metamodel ever said
 * it. This class is the second route and only the second route.
 * </p>
 *
 * <p>
 * <b>Nothing is looked up by text and nothing is searched.</b> {@code Requirement.cites} is a
 * non-containment reference and the corpora are containment children of the same
 * {@code ComplianceContext}, so a citation is a same-document fragment that EMF has already
 * resolved by the time the context is in hand - dereferencing it is a field read. Which
 * requirement a situation triggers is decided by a rule catalogue, as a reviewed constant, never
 * here: a resolver that could <em>choose</em> a provision could invent one, and the whole reason
 * this analysis is trustworthy is that it cannot.
 * </p>
 *
 * <p>
 * <b>Three answers, kept apart.</b> {@link Outcome#FOUND} carries at least one {@link Evidence};
 * {@link Outcome#NOT_QUOTABLE} says the requirement exists but nothing it cites can be quoted, so
 * the statement has to be a {@code ControlEvaluation}, which needs only a {@code RequirementRef};
 * {@link Outcome#UNKNOWN_REQUIREMENT} says the catalogue named something this context does not
 * have, which is a fault in the catalogue and not a finding about anything. A caller can never
 * reach an {@code Evidence} with a blank quote, because one is never built.
 * </p>
 *
 * <p>
 * This class is pure: one {@code ComplianceContext} in, no services, no storage, no network. The
 * fetching and the caching of the context are {@link ComplianceContexts}' business.
 * </p>
 */
public final class RequirementCitations {

	/**
	 * The name of the {@link ControlPart} that holds a control's normative sentence.
	 * <p>
	 * OSCAL convention, and the catalogues follow it exactly: of the 1998 parts of the seeded
	 * Grundschutz++ catalogue, 1000 are {@code statement} - one per control - and 998 are
	 * {@code guidance}, which explains the control rather than stating it. Guidance is deliberately
	 * never quoted: it is commentary, and a finding evidenced with commentary would misrepresent
	 * what the corpus requires.
	 */
	private static final String STATEMENT = "statement";

	/**
	 * An OSCAL parameter insertion in prose, e.g. {@code {{ insert: param, dls.2.2-prm1 }}}.
	 *
	 * @see #expandInserts(String, EObject)
	 */
	private static final Pattern INSERT = Pattern.compile("\\{\\{\\s*insert:\\s*param,\\s*([^}]+?)\\s*\\}\\}");

	/** What a lookup answered. */
	public enum Outcome {

		/** The requirement was found and at least one thing it cites could be quoted. */
		FOUND,

		/**
		 * The requirement exists, and nothing it cites carries text - or it cites nothing, or the
		 * context has no corpus at all. {@code iso27001-annex-a} is the whole of the third case:
		 * 93 requirements, no corpora, because its normative text is copyrighted.
		 */
		NOT_QUOTABLE,

		/** No requirement of that id in this context. */
		UNKNOWN_REQUIREMENT,

		/**
		 * No context of that id in the registry. Never answered here - only
		 * {@link ComplianceContexts} can tell, because only it fetches.
		 */
		UNKNOWN_CONTEXT
	}

	/**
	 * One lookup's answer.
	 *
	 * @param outcome           which of the four cases this is
	 * @param contextId         the context asked
	 * @param contextVersion    its version, for the {@code RequirementRef} a caller writes beside
	 *                          the evidence; {@code null} when the context was not found
	 * @param requirementId     the requirement asked
	 * @param requestedLanguage the language asked for, as given
	 * @param language          the language actually quoted, which differs from
	 *                          {@code requestedLanguage} when the context does not hold it
	 * @param evidence          one entry per cited unit that could be quoted, in the order the
	 *                          requirement cites them. Empty unless {@link Outcome#FOUND}. Freshly
	 *                          built on every call and owned by the caller
	 */
	public record Citation(Outcome outcome, String contextId, String contextVersion, String requirementId,
			String requestedLanguage, String language, List<Evidence> evidence) {

		/** Whether a {@code Finding} can be written at all. */
		public boolean isFound() {
			return outcome == Outcome.FOUND;
		}

		/** Whether the quote is in a language other than the one asked for. */
		public boolean isFallbackLanguage() {
			return language != null && requestedLanguage != null && !language.equalsIgnoreCase(requestedLanguage);
		}
	}

	private final ComplianceContext context;
	private final Map<String, Requirement> byId = new LinkedHashMap<>();

	/** Per corpus, its citables by citationId. Built on first use, which is a second language. */
	private final Map<Corpus, Map<String, Citable>> indexed = new HashMap<>();

	/**
	 * @param context the context to answer from, never {@code null}
	 */
	public RequirementCitations(ComplianceContext context) {
		this.context = Objects.requireNonNull(context, "context");
		for (Requirement requirement : allRequirements(context)) {
			if (requirement.getId() != null) {
				byId.putIfAbsent(requirement.getId(), requirement);
			}
		}
	}

	/** The context this answers from. */
	public ComplianceContext context() {
		return context;
	}

	/**
	 * The evidence for one requirement.
	 *
	 * @param requirementId the id a rule catalogue names, never {@code null}
	 * @param language      the language to quote in; {@code null} or blank takes the cited corpus's
	 *                      own language
	 * @return the answer, never {@code null}
	 */
	public Citation resolve(String requirementId, String language) {
		Requirement requirement = requirementId == null ? null : byId.get(requirementId);
		if (requirement == null) {
			return new Citation(Outcome.UNKNOWN_REQUIREMENT, context.getId(), context.getVersion(), requirementId,
					language, null, List.of());
		}
		List<Evidence> evidence = new ArrayList<>();
		String used = null;
		for (Citable cited : requirement.getCites()) {
			Citable inLanguage = translate(cited, language);
			Evidence one = evidenceOf(inLanguage);
			if (one != null) {
				evidence.add(one);
				if (used == null) {
					used = languageOf(inLanguage);
				}
			}
		}
		Outcome outcome = evidence.isEmpty() ? Outcome.NOT_QUOTABLE : Outcome.FOUND;
		return new Citation(outcome, context.getId(), context.getVersion(), requirementId, language, used,
				List.copyOf(evidence));
	}

	/* ------------------------------------------------------------------------------- quoting */

	/**
	 * One cited unit as an {@link Evidence}, or {@code null} when it cannot be quoted.
	 * <p>
	 * A citation id is as mandatory as the quote - {@code Evidence.citationId} is
	 * {@code lowerBound="1"} - so a unit with text and no id is not quotable either. That is not a
	 * theoretical case: a quote nobody can look up is exactly the unverifiable citation this whole
	 * design exists to make impossible.
	 */
	private Evidence evidenceOf(Citable cited) {
		if (cited == null) {
			return null;
		}
		Citable quoted = quotable(cited);
		if (quoted == null) {
			return null;
		}
		String citationId = blankToNull(quoted.getCitationId());
		String quote = blankToNull(expandInserts(ownText(quoted), quoted));
		if (citationId == null || quote == null) {
			return null;
		}
		Corpus corpus = corpusOf(quoted);
		Evidence evidence = ReportFactory.eINSTANCE.createEvidence();
		evidence.setContextId(context.getId());
		if (corpus != null) {
			evidence.setCorpusId(corpus.getId());
		}
		evidence.setCitationId(citationId);
		evidence.setQuote(quote);
		evidence.setVerbatim(true);
		if (quoted instanceof LegalUnit unit && blankToNull(unit.getSourceRef()) != null) {
			evidence.setSourceRef(unit.getSourceRef());
		}
		return evidence;
	}

	/**
	 * The thing actually quoted: the cited unit itself when it carries text, otherwise the one
	 * {@code statement} part beneath it.
	 *
	 * <p>
	 * <b>Why one level down.</b> A {@code Requirement} of a control catalogue cites the
	 * {@link Control}, because that is the thing the catalogue is a catalogue of - but a control is
	 * a container and its normative sentence lives in its {@code statement} {@link ControlPart}.
	 * Refusing to descend would report every one of Grundschutz++'s 1000 requirements as unquotable
	 * while the sentence sits one containment away, which is not a cautious answer but a wrong one.
	 * </p>
	 *
	 * <p>
	 * The descent is one level and it is to a part that is itself {@link Citable}, so the
	 * {@code Evidence} cites {@code KONF.11.8_stm} rather than {@code KONF.11.8}: the id names the
	 * text that was quoted, which is the only id that can be checked against the quote. Exactly one
	 * {@code statement} part or none - two would be an ambiguity, and guessing between two
	 * normative sentences is not something this is allowed to do.
	 * </p>
	 */
	private static Citable quotable(Citable cited) {
		if (ownText(cited) != null) {
			return cited;
		}
		ControlPart statement = null;
		for (ControlPart part : partsOf(cited)) {
			if (STATEMENT.equals(part.getName())) {
				if (statement != null) {
					return null;
				}
				statement = part;
			}
		}
		return statement != null && ownText(statement) != null ? statement : null;
	}

	/**
	 * The text a citable carries itself, or {@code null} where it carries none.
	 * <p>
	 * Not every text-bearing type is a {@link LegalUnit}: a {@link DocumentSection} and a
	 * {@link DocumentClause} of a technical guideline hold their own {@code text}, and a
	 * {@link ControlPart} holds {@code prose}. {@link Control}, {@link ControlGroup},
	 * {@code Division} and {@code AnnexSection} hold none - they are structure, and a heading is
	 * not a provision.
	 */
	private static String ownText(Citable citable) {
		if (citable instanceof LegalUnit unit) {
			return blankToNull(unit.getText());
		}
		if (citable instanceof DocumentSection section) {
			return blankToNull(section.getText());
		}
		if (citable instanceof DocumentClause clause) {
			return blankToNull(clause.getText());
		}
		if (citable instanceof ControlPart part) {
			return blankToNull(part.getProse());
		}
		return null;
	}

	/** The parts directly beneath a citable, for the one descent {@link #quotable} makes. */
	private static List<ControlPart> partsOf(Citable citable) {
		if (citable instanceof Control control) {
			return control.getParts();
		}
		if (citable instanceof ControlGroup group) {
			return group.getParts();
		}
		return List.of();
	}

	/**
	 * Prose with its OSCAL parameter insertions rendered.
	 *
	 * <p>
	 * A resolved OSCAL catalogue still carries {@code {{ insert: param, x }}} in its prose: profile
	 * resolution selects controls and <em>sets</em> parameters, it does not substitute them into
	 * the text. The values are corpus data - {@link ControlParameter}s declared on the enclosing
	 * {@link Control} - so rendering them adds nothing from outside the corpus, which is why the
	 * result is still {@code verbatim}. It is also demonstrably the corpus's own intended
	 * rendering: across the seeded Grundschutz++ catalogue, substituting each insertion with its
	 * parameter's label reproduces the context's {@code Requirement.statement} for all 1000
	 * requirements, byte for byte, with no exceptions.
	 * </p>
	 *
	 * <p>
	 * A parameter that cannot be found leaves its insertion standing rather than blanking it. A
	 * visible {@code {{ insert: ... }}} in a report is a legible fault; a sentence with a hole
	 * where its object should be is a misquotation.
	 * </p>
	 */
	private static String expandInserts(String prose, EObject quoted) {
		if (prose == null || prose.indexOf("{{") < 0) {
			return prose;
		}
		Matcher matcher = INSERT.matcher(prose);
		StringBuilder rendered = new StringBuilder();
		while (matcher.find()) {
			String replacement = parameterValue(quoted, matcher.group(1));
			matcher.appendReplacement(rendered, Matcher.quoteReplacement(replacement == null ? matcher.group() : replacement));
		}
		matcher.appendTail(rendered);
		return rendered.toString();
	}

	/** A parameter's rendering, looked up on the control that encloses the quoted prose. */
	private static String parameterValue(EObject quoted, String parameterId) {
		for (EObject node = quoted; node != null; node = node.eContainer()) {
			if (node instanceof Control control) {
				for (ControlParameter parameter : control.getParameters()) {
					if (parameterId.equals(parameter.getParameterId())) {
						String label = blankToNull(parameter.getLabel());
						if (label != null) {
							return label;
						}
						return parameter.getValues().isEmpty() ? null : blankToNull(parameter.getValues().get(0));
					}
				}
			}
		}
		return null;
	}

	/* ----------------------------------------------------------------------------- languages */

	/**
	 * The same citation in another language, or the cited unit itself when there is no better.
	 *
	 * <p>
	 * {@code cites} points into one corpus. A context that ships two - {@code gdpr} and
	 * {@code ai-act} each carry a DE and an EN corpus of the same {@code workId}, with the same
	 * citation ids in both - can therefore answer in either, and the second language is the same id
	 * located in the sibling. That is the one place a map is built, and it is built per corpus on
	 * first use, because most reports never ask for a second language.
	 * </p>
	 *
	 * <p>
	 * Asked for a language the context does not hold, this falls back to the context's
	 * {@code referenceLanguage} and finally to the cited unit itself. It never answers nothing: a
	 * missing translation is a reason to quote the original, not a reason to lose the citation. The
	 * answer records which language it used, so a report can say so.
	 * </p>
	 */
	private Citable translate(Citable cited, String language) {
		String wanted = blankToNull(language);
		if (wanted == null || cited == null || wanted.equalsIgnoreCase(languageOf(cited))) {
			return cited;
		}
		Citable sibling = inLanguage(cited, wanted);
		if (sibling != null) {
			return sibling;
		}
		String reference = blankToNull(context.getReferenceLanguage());
		if (reference != null && !reference.equalsIgnoreCase(languageOf(cited))) {
			Citable fallback = inLanguage(cited, reference);
			if (fallback != null) {
				return fallback;
			}
		}
		return cited;
	}

	/** The same citation id in the sibling corpus of the same work and the given language. */
	private Citable inLanguage(Citable cited, String language) {
		Corpus origin = corpusOf(cited);
		String citationId = blankToNull(cited.getCitationId());
		if (origin == null || citationId == null) {
			return null;
		}
		for (Corpus candidate : context.getCorpora()) {
			if (candidate == origin || !language.equalsIgnoreCase(candidate.getLanguage())
					|| !sameWork(origin, candidate)) {
				continue;
			}
			Citable found = citables(candidate).get(citationId);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	/**
	 * Two corpora are the same work when they say so. A {@code workId} is what makes a DE and an EN
	 * corpus two renderings of one act rather than two acts, so an absent one is not guessed
	 * around: without it there is nothing to say the citation ids mean the same thing.
	 */
	private static boolean sameWork(Corpus origin, Corpus candidate) {
		String work = blankToNull(origin.getWorkId());
		return work != null && work.equals(blankToNull(candidate.getWorkId()));
	}

	/** One corpus's citables by citation id, memoised. */
	private Map<String, Citable> citables(Corpus corpus) {
		return indexed.computeIfAbsent(corpus, source -> {
			Map<String, Citable> byCitationId = new HashMap<>();
			source.eAllContents().forEachRemaining(content -> {
				if (content instanceof Citable citable && blankToNull(citable.getCitationId()) != null) {
					byCitationId.putIfAbsent(citable.getCitationId(), citable);
				}
			});
			return byCitationId;
		});
	}

	private static String languageOf(Citable citable) {
		Corpus corpus = corpusOf(citable);
		return corpus == null ? null : blankToNull(corpus.getLanguage());
	}

	/* --------------------------------------------------------------------------------- model */

	/** The corpus a citable belongs to, or {@code null} for one that is not under a corpus. */
	private static Corpus corpusOf(EObject node) {
		for (EObject current = node; current != null; current = current.eContainer()) {
			if (current instanceof Corpus corpus) {
				return corpus;
			}
		}
		return null;
	}

	/**
	 * Every requirement of the context, those a group holds included.
	 * <p>
	 * A context states them either way and both are ordinary: {@code gdpr} lists its 25 flat,
	 * {@code bsi-grundschutz-pp} nests all 1000 in 20 groups of groups. Reading only the top level
	 * would answer {@code UNKNOWN_REQUIREMENT} for every requirement of the larger context.
	 */
	private static List<Requirement> allRequirements(ComplianceContext context) {
		List<Requirement> all = new ArrayList<>(context.getRequirements());
		collect(context.getRequirementGroups(), all);
		return all;
	}

	/**
	 * The requirements of a group tree, in declared order. Walked rather than taken from
	 * {@code eAllContents}, which would drag the corpora - 6.7 MB of them for the largest seeded
	 * context - through a scan that only ever wanted the requirements.
	 */
	private static void collect(List<RequirementGroup> groups, List<Requirement> into) {
		for (RequirementGroup group : groups) {
			into.addAll(group.getRequirements());
			collect(group.getGroups(), into);
		}
	}

	private static String blankToNull(String value) {
		return value == null || value.isBlank() ? null : value;
	}
}
