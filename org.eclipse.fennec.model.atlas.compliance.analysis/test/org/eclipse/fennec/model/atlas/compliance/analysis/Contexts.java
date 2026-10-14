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

import org.eclipse.fennec.model.compliance.context.ComplianceContext;
import org.eclipse.fennec.model.compliance.context.ContextFactory;
import org.eclipse.fennec.model.compliance.context.Requirement;
import org.eclipse.fennec.model.compliance.context.RequirementGroup;
import org.eclipse.fennec.model.compliance.corpus.Article;
import org.eclipse.fennec.model.compliance.corpus.Control;
import org.eclipse.fennec.model.compliance.corpus.ControlCatalog;
import org.eclipse.fennec.model.compliance.corpus.ControlGroup;
import org.eclipse.fennec.model.compliance.corpus.ControlParameter;
import org.eclipse.fennec.model.compliance.corpus.ControlPart;
import org.eclipse.fennec.model.compliance.corpus.CorpusFactory;
import org.eclipse.fennec.model.compliance.corpus.Division;
import org.eclipse.fennec.model.compliance.corpus.LegalAct;
import org.eclipse.fennec.model.compliance.corpus.Paragraph;
import org.eclipse.fennec.model.compliance.corpus.Point;

/**
 * The contexts these tests run against, built here rather than read from anywhere.
 *
 * <p>
 * <b>Why not the seeded ones.</b> The deployment's contexts under
 * {@code docker/dockercompose/initial-models/} are not in this repository and are not going to
 * be: {@code .gitignore} excludes the whole tree because the Grundschutz++ catalogue is
 * CC BY-SA 4.0 and the ISO 27001 mapping carries identifiers whose text is protected — *"not ours
 * to redistribute"*, in its own words. A test that reads them passes on the machine that has them
 * and fails everywhere else, which is precisely what it did.
 * </p>
 *
 * <p>
 * So these fixtures carry no third-party text. What they do carry is every <em>shape</em> the
 * resolver has to cope with, because the shapes are what the tests are about:
 * </p>
 *
 * <ul>
 * <li>{@link #twoLanguages()} — one work in two corpora with the same citation ids, which is what
 * makes a second language a lookup rather than a missing capability;</li>
 * <li>{@link #controlCatalogue()} — a requirement citing a {@code Control}, which carries no text
 * of its own while its {@code statement} part does, and an OSCAL parameter insertion in that
 * part's prose. Requirements nested in a group, as a catalogue states them;</li>
 * <li>{@link #withoutCorpus()} — requirements and no corpus at all, the unquotable context;</li>
 * <li>{@link #citingADivision()} — a citation landing on structure rather than on a provision.</li>
 * </ul>
 */
final class Contexts {

	private static final ContextFactory CONTEXTS = ContextFactory.eINSTANCE;
	private static final CorpusFactory CORPORA = CorpusFactory.eINSTANCE;

	/** The reference-language text of the one provision the language tests quote. */
	static final String DE_TEXT = "Kopie eines Verfahrens, in deutscher Sprache, als Beleg;";

	/** The same provision in the sibling corpus. */
	static final String EN_TEXT = "a copy of a procedure, in English, as evidence;";

	private Contexts() {
	}

	/**
	 * One act in DE and EN, same {@code workId}, same citation ids in both. Reference language DE,
	 * which is also the corpus the requirement cites.
	 */
	static ComplianceContext twoLanguages() {
		ComplianceContext context = context("demo", "1.0.0", "DE");
		context.getCorpora().add(act("demo-DE", "demo", "DE", DE_TEXT));
		context.getCorpora().add(act("demo-EN", "demo", "EN", EN_TEXT));
		Requirement requirement = requirement("Art.1(1)(a)", "Nachweis");
		requirement.getCites().add(pointOf(context.getCorpora().get(0)));
		context.getRequirements().add(requirement);
		return context;
	}

	/**
	 * A control catalogue: the requirement cites the {@code Control}, and the sentence is one
	 * containment further down in its single {@code statement} part. The prose carries an OSCAL
	 * insertion whose parameter is declared on that same control, and the requirement's own
	 * {@code statement} is the rendered form — which is what the expansion has to reproduce.
	 */
	static ComplianceContext controlCatalogue() {
		ComplianceContext context = context("catalogue", "2026-01-01", "DE");
		ControlCatalog catalogue = CORPORA.createControlCatalog();
		catalogue.setId("cat-DE");
		catalogue.setWorkId("cat");
		catalogue.setLanguage("DE");

		ControlGroup group = CORPORA.createControlGroup();
		group.setTitle("Konfiguration");
		Control plain = control("KONF.1", "Verschluesselung",
				"Anwendungen MUESSEN schuetzenswerte Daten bei der Speicherung verschluesseln.");
		Control parameterised = control("KONF.2", "Transport",
				"Anwendungen SOLLTEN den Transport nach {{ insert: param, konf.2-prm1 }} verschluesseln.");
		ControlParameter parameter = CORPORA.createControlParameter();
		parameter.setParameterId("konf.2-prm1");
		parameter.setLabel("einem anerkannten Standard");
		parameterised.getParameters().add(parameter);
		group.getControls().add(plain);
		group.getControls().add(parameterised);
		catalogue.getGroups().add(group);
		context.getCorpora().add(catalogue);

		// Nested, the way a catalogue states them: reading only the top level would answer
		// UNKNOWN_REQUIREMENT for every one of them.
		RequirementGroup requirements = CONTEXTS.createRequirementGroup();
		requirements.setId("KONF");
		requirements.getRequirements().add(citing("KONF.1", "Verschluesselung",
				"Anwendungen MUESSEN schuetzenswerte Daten bei der Speicherung verschluesseln.", plain));
		requirements.getRequirements().add(citing("KONF.2", "Transport",
				"Anwendungen SOLLTEN den Transport nach einem anerkannten Standard verschluesseln.",
				parameterised));
		context.getRequirementGroups().add(requirements);
		return context;
	}

	/** Requirements, no corpora, no citations: a context that cannot be quoted at all. */
	static ComplianceContext withoutCorpus() {
		ComplianceContext context = context("identifiers-only", "2022", null);
		context.getRequirements().add(requirement("A.5.1", "Policies for information security"));
		return context;
	}

	/** A requirement whose citation lands on a {@code Division}: structure, not a provision. */
	static ComplianceContext citingADivision() {
		ComplianceContext context = context("structural", "1.0.0", "DE");
		LegalAct act = act("act-DE", "act", "DE", DE_TEXT);
		Division chapter = CORPORA.createDivision();
		chapter.setCitationId("Chapter.II");
		chapter.setHeading("Grundsaetze");
		act.getDivisions().add(chapter);
		context.getCorpora().add(act);
		Requirement requirement = requirement("R-1", "Grundsatz");
		requirement.getCites().add(chapter);
		context.getRequirements().add(requirement);
		return context;
	}

	/* ------------------------------------------------------------------------------ building */

	/** An act whose one point carries {@code text}, reachable as {@link #pointOf}. */
	private static LegalAct act(String id, String workId, String language, String text) {
		LegalAct act = CORPORA.createLegalAct();
		act.setId(id);
		act.setWorkId(workId);
		act.setLanguage(language);
		Division division = CORPORA.createDivision();
		Article article = CORPORA.createArticle();
		article.setNumber(1);
		Paragraph paragraph = CORPORA.createParagraph();
		paragraph.setNumber(1);
		Point point = CORPORA.createPoint();
		point.setCitationId("Art.1(1)(a)");
		point.setLabel("a)");
		point.setText(text);
		point.setSourceRef("art_1/parag[1]/item[1]");
		paragraph.getPoints().add(point);
		article.getParagraphs().add(paragraph);
		division.getArticles().add(article);
		act.getDivisions().add(division);
		return act;
	}

	/** The quotable point of an act built by {@link #act}. */
	private static Point pointOf(org.eclipse.fennec.model.compliance.corpus.Corpus corpus) {
		return ((LegalAct) corpus).getDivisions().get(0).getArticles().get(0).getParagraphs().get(0).getPoints()
				.get(0);
	}

	private static Control control(String citationId, String title, String statementProse) {
		Control control = CORPORA.createControl();
		control.setCitationId(citationId);
		control.setTitle(title);
		control.getParts().add(part("statement", citationId + "_stm", statementProse));
		control.getParts().add(part("guidance", citationId + "_gdn",
				"Erlaeuterung zur Anforderung. Dies ist Kommentar und keine Vorschrift."));
		return control;
	}

	private static ControlPart part(String name, String citationId, String prose) {
		ControlPart part = CORPORA.createControlPart();
		part.setName(name);
		part.setCitationId(citationId);
		part.setProse(prose);
		return part;
	}

	private static ComplianceContext context(String id, String version, String referenceLanguage) {
		ComplianceContext context = CONTEXTS.createComplianceContext();
		context.setId(id);
		context.setVersion(version);
		if (referenceLanguage != null) {
			context.setReferenceLanguage(referenceLanguage);
		}
		return context;
	}

	private static Requirement requirement(String id, String title) {
		Requirement requirement = CONTEXTS.createRequirement();
		requirement.setId(id);
		requirement.setTitle(title);
		return requirement;
	}

	private static Requirement citing(String id, String title, String statement,
			org.eclipse.fennec.model.compliance.corpus.Citable cited) {
		Requirement requirement = requirement(id, title);
		requirement.setStatement(statement);
		requirement.getCites().add(cited);
		return requirement;
	}
}
