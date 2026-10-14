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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.fennec.model.atlas.compliance.analysis.RequirementCitations.Citation;
import org.eclipse.fennec.model.atlas.compliance.analysis.RequirementCitations.Outcome;
import org.eclipse.fennec.model.compliance.context.ComplianceContext;
import org.eclipse.fennec.model.compliance.context.ContextFactory;
import org.eclipse.fennec.model.compliance.context.Requirement;
import org.eclipse.fennec.model.compliance.corpus.Control;
import org.eclipse.fennec.model.compliance.corpus.ControlCatalog;
import org.eclipse.fennec.model.compliance.corpus.ControlPart;
import org.eclipse.fennec.model.compliance.corpus.CorpusFactory;
import org.eclipse.fennec.model.compliance.report.Evidence;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Dereferencing a requirement to the text it cites.
 *
 * <p>
 * The statements these back are the ones no review ever made: that a column is stored
 * unencrypted, that a resource has no retention rule. There is nothing to carry a citation over
 * from, so the evidence is the requirement's own — and because {@code Finding.evidence} is
 * mandatory and {@code Evidence} needs both an id and a quote, a resolver that answered vaguely
 * would either block the finding or fill it with something unverifiable. Hence the three answers,
 * and hence the rule that no {@code Evidence} is ever built without both halves.
 * </p>
 *
 * <p>
 * The fixtures are {@link Contexts}, built in code. They carry no third-party text and every
 * shape the resolver has to cope with; see that class for why the deployment's own contexts are
 * not used.
 * </p>
 */
@DisplayName("A requirement's cited text, as evidence")
public class RequirementCitationsTest {

	private static final ContextFactory CONTEXTS = ContextFactory.eINSTANCE;
	private static final CorpusFactory CORPORA = CorpusFactory.eINSTANCE;

	/* ------------------------------------------------------------------ a legal act, quotable */

	@Test
	@DisplayName("a cited provision is quoted verbatim, with the corpus it was read out of")
	public void quotesALegalUnit() {
		Citation citation = new RequirementCitations(Contexts.twoLanguages()).resolve("Art.1(1)(a)", "DE");

		assertEquals(Outcome.FOUND, citation.outcome());
		assertEquals(1, citation.evidence().size());
		Evidence evidence = citation.evidence().get(0);
		assertEquals("demo", evidence.getContextId());
		assertEquals("demo-DE", evidence.getCorpusId());
		assertEquals("Art.1(1)(a)", evidence.getCitationId());
		assertEquals(Contexts.DE_TEXT, evidence.getQuote(), "copied unchanged, to the last character");
		assertEquals("art_1/parag[1]/item[1]", evidence.getSourceRef(),
				"the corpus unit's own sourceRef, so a suspect quote can be traced to the source document");
		assertTrue(evidence.isVerbatim());
	}

	@Test
	@DisplayName("the same citation in the sibling corpus answers in the other language")
	public void quotesTheSiblingCorpus() {
		RequirementCitations citations = new RequirementCitations(Contexts.twoLanguages());

		Citation german = citations.resolve("Art.1(1)(a)", "DE");
		Citation english = citations.resolve("Art.1(1)(a)", "EN");

		assertEquals(Contexts.DE_TEXT, german.evidence().get(0).getQuote());
		assertEquals(Contexts.EN_TEXT, english.evidence().get(0).getQuote());
		assertEquals("Art.1(1)(a)", english.evidence().get(0).getCitationId(),
				"the citation id is the same in both corpora - that is what makes them one work in two languages");
		assertEquals("demo-EN", english.evidence().get(0).getCorpusId());
		assertEquals("EN", english.language());
		assertFalse(english.isFallbackLanguage());
	}

	@Test
	@DisplayName("a language the context does not hold falls back, and says which it used")
	public void fallsBackToTheReferenceLanguage() {
		Citation citation = new RequirementCitations(Contexts.twoLanguages()).resolve("Art.1(1)(a)", "FR");

		assertEquals(Outcome.FOUND, citation.outcome(), "a missing translation loses the language, not the citation");
		assertEquals("DE", citation.language());
		assertEquals("FR", citation.requestedLanguage());
		assertTrue(citation.isFallbackLanguage(),
				"a report that quoted the fallback has to be able to say so rather than implying a French source");
		assertEquals(Contexts.DE_TEXT, citation.evidence().get(0).getQuote());
	}

	@Test
	@DisplayName("every evidence is freshly built, so two callers cannot steal it from each other")
	public void buildsFreshEvidence() {
		RequirementCitations citations = new RequirementCitations(Contexts.twoLanguages());

		Evidence first = citations.resolve("Art.1(1)(a)", "DE").evidence().get(0);
		Evidence second = citations.resolve("Art.1(1)(a)", "DE").evidence().get(0);

		assertNotSame(first, second,
				"an Evidence goes into a Finding's containment list; handing the same instance to two findings "
						+ "would move it out of the first");
		assertEquals(first.getQuote(), second.getQuote());
	}

	/* ------------------------------------------------- a control catalogue, quotable one down */

	@Test
	@DisplayName("a cited control is quoted through its statement part, citing that part")
	public void descendsToTheStatementPart() {
		Citation citation = new RequirementCitations(Contexts.controlCatalogue()).resolve("KONF.1", "DE");

		assertEquals(Outcome.FOUND, citation.outcome(),
				"the control carries no text, but its normative sentence is one containment away");
		Evidence evidence = citation.evidence().get(0);
		assertEquals("KONF.1_stm", evidence.getCitationId(),
				"the id names the text that was quoted, which is the only id that can be checked against it");
		assertEquals("Anwendungen MUESSEN schuetzenswerte Daten bei der Speicherung verschluesseln.",
				evidence.getQuote());
		assertEquals("cat-DE", evidence.getCorpusId());
		assertTrue(evidence.isVerbatim());
		assertNull(evidence.getSourceRef(), "a ControlPart has no sourceRef; inventing one would be a false trace");
	}

	@Test
	@DisplayName("guidance is never quoted, only the statement")
	public void quotesTheStatementNotTheGuidance() {
		Evidence evidence = new RequirementCitations(Contexts.controlCatalogue()).resolve("KONF.1", "DE").evidence()
				.get(0);

		assertFalse(evidence.getQuote().contains("Erlaeuterung"),
				"the guidance part explains the control; a finding evidenced with commentary would misstate "
						+ "what the corpus requires");
	}

	@Test
	@DisplayName("an OSCAL parameter insertion is rendered from the corpus's own parameter")
	public void expandsParameterInsertions() {
		Evidence evidence = new RequirementCitations(Contexts.controlCatalogue()).resolve("KONF.2", "DE").evidence()
				.get(0);

		assertFalse(evidence.getQuote().contains("{{"),
				"a resolved OSCAL catalogue still carries the insertion; a reader of the report must not");
		assertEquals("Anwendungen SOLLTEN den Transport nach einem anerkannten Standard verschluesseln.",
				evidence.getQuote());
		assertTrue(evidence.isVerbatim(),
				"the parameter is corpus data declared on the same control, so nothing from outside the corpus "
						+ "entered the quote");
	}

	@Test
	@DisplayName("the rendered quote is the context's own statement of the requirement")
	public void renderingMatchesTheRequirementStatement() {
		ComplianceContext context = Contexts.controlCatalogue();
		RequirementCitations citations = new RequirementCitations(context);

		for (String id : new String[] { "KONF.1", "KONF.2" }) {
			Requirement requirement = context.getRequirementGroups().get(0).getRequirements().stream()
					.filter(each -> id.equals(each.getId())).findFirst().orElseThrow();
			assertEquals(requirement.getStatement(), citations.resolve(id, "DE").evidence().get(0).getQuote(),
					() -> "the expansion must reproduce the context's own rendering of " + id);
		}
	}

	@Test
	@DisplayName("a requirement nested in a group is found")
	public void findsRequirementsInsideGroups() {
		assertEquals(Outcome.FOUND, new RequirementCitations(Contexts.controlCatalogue()).resolve("KONF.1", "DE")
				.outcome(),
				"a catalogue nests its requirements in groups; reading only the top level would answer "
						+ "UNKNOWN_REQUIREMENT for every one of them");
	}

	/* -------------------------------------------------------------------- the two refusals */

	@Test
	@DisplayName("a context with no corpus is not quotable, and that is not the same as unknown")
	public void aContextWithoutCorpusIsNotQuotable() {
		RequirementCitations citations = new RequirementCitations(Contexts.withoutCorpus());

		Citation known = citations.resolve("A.5.1", "EN");
		Citation unknown = citations.resolve("A.99.99", "EN");

		assertEquals(Outcome.NOT_QUOTABLE, known.outcome(),
				"a context whose normative text is protected ships none of it, so a statement against this "
						+ "requirement has to be a ControlEvaluation");
		assertEquals(Outcome.UNKNOWN_REQUIREMENT, unknown.outcome(),
				"a catalogue naming a requirement that does not exist is a fault in the catalogue, not a "
						+ "requirement nobody can quote");
		assertTrue(known.evidence().isEmpty());
		assertTrue(unknown.evidence().isEmpty());
	}

	@Test
	@DisplayName("a citation landing on a division yields no evidence at all")
	public void aDivisionIsNotQuotable() {
		Citation citation = new RequirementCitations(Contexts.citingADivision()).resolve("R-1", "DE");

		assertEquals(Outcome.NOT_QUOTABLE, citation.outcome(), "a heading is structure, not a provision");
		assertTrue(citation.evidence().isEmpty(), "never an Evidence with a blank quote");
	}

	@Test
	@DisplayName("two statement parts are an ambiguity, not a choice")
	public void twoStatementsAreNotQuotable() {
		ComplianceContext context = CONTEXTS.createComplianceContext();
		context.setId("synthetic");
		ControlCatalog catalog = CORPORA.createControlCatalog();
		catalog.setId("cat-DE");
		catalog.setWorkId("cat");
		catalog.setLanguage("DE");
		Control control = CORPORA.createControl();
		control.setCitationId("C.1");
		control.getParts().add(part("statement", "C.1_a", "Das eine."));
		control.getParts().add(part("statement", "C.1_b", "Das andere."));
		catalog.getControls().add(control);
		context.getCorpora().add(catalog);
		Requirement requirement = CONTEXTS.createRequirement();
		requirement.setId("R-1");
		requirement.getCites().add(control);
		context.getRequirements().add(requirement);

		Citation citation = new RequirementCitations(context).resolve("R-1", "DE");

		assertEquals(Outcome.NOT_QUOTABLE, citation.outcome(),
				"guessing between two normative sentences is not a thing a citation resolver may do");
	}

	@Test
	@DisplayName("an unknown requirement is reported, never resolved to the nearest thing")
	public void anUnknownRequirementIsReported() {
		Citation citation = new RequirementCitations(Contexts.twoLanguages()).resolve("Art.1(1)(z)", "DE");

		assertEquals(Outcome.UNKNOWN_REQUIREMENT, citation.outcome());
		assertFalse(citation.isFound());
		assertEquals("demo", citation.contextId());
		assertEquals("Art.1(1)(z)", citation.requirementId());
	}

	@Test
	@DisplayName("the version travels with the answer, for the RequirementRef beside the evidence")
	public void carriesTheContextVersion() {
		Citation citation = new RequirementCitations(Contexts.twoLanguages()).resolve("Art.1(1)(a)", "DE");

		assertEquals("1.0.0", citation.contextVersion(),
				"a finding has to pin the version of the context it was raised against");
	}

	private static ControlPart part(String name, String citationId, String prose) {
		ControlPart part = CORPORA.createControlPart();
		part.setName(name);
		part.setCitationId(citationId);
		part.setProse(prose);
		return part;
	}
}
