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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.fennec.model.atlas.qvt.gdpr.StagedReviews.Candidate;
import org.eclipse.fennec.model.gdprReport.GDPRReportFactory;
import org.eclipse.fennec.model.gdprReport.GdprReport;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Which review speaks for a metamodel revision when the reviews are spread over several stages.
 * <p>
 * The case this exists for: package visibility runs along a ladder, so a transformation in
 * {@code draft} routinely compiles against a metamodel that lives in {@code approved}. Looking only
 * where the transformation is would report that metamodel as unreviewed - not merely incomplete but
 * false, because the review of exactly those bytes is one stage up.
 */
@DisplayName("The review that speaks for a revision, across stages")
public class StagedReviewsTest {

	private static final List<String> LADDER = List.of("draft", "approved", "release");
	private static final String FP = "fp1:abc";

	private static final Instant EARLY = Instant.parse("2026-09-01T00:00:00Z");
	private static final Instant LATE = Instant.parse("2026-09-30T00:00:00Z");

	@Test
	@DisplayName("a review one stage up is found when the transformation's own stage has none")
	public void aReviewFromAnotherStageIsFound() {
		GdprReport approved = report("in-approved");

		Map<String, GdprReport> resolved = StagedReviews.resolve(LADDER,
				List.of(new Candidate("approved", "r1", EARLY, FP, approved)));

		assertEquals(approved, resolved.get(FP),
				"the metamodel is in approved and so is its review; the unit is in draft");
	}

	@Test
	@DisplayName("stage order decides, and a later stage never overrides an earlier one")
	public void stageOrderBeatsRecency() {
		GdprReport inDraft = report("in-draft");
		GdprReport inApproved = report("in-approved");

		// The approved one is a month newer. It still loses: the list says which stages are
		// authoritative, and recency cannot outrank that.
		Map<String, GdprReport> resolved = StagedReviews.resolve(LADDER,
				List.of(new Candidate("approved", "r2", LATE, FP, inApproved),
						new Candidate("draft", "r1", EARLY, FP, inDraft)));

		assertEquals(inDraft, resolved.get(FP));

		// Reversed, the deployment has said the opposite and gets the opposite.
		assertEquals(inApproved, StagedReviews.resolve(List.of("approved", "draft"),
				List.of(new Candidate("approved", "r2", LATE, FP, inApproved),
						new Candidate("draft", "r1", EARLY, FP, inDraft))).get(FP));
	}

	@Test
	@DisplayName("within one stage the latest review still wins")
	public void recencyDecidesWithinAStage() {
		GdprReport agent = report("agent");
		GdprReport correction = report("human-correction");

		// An agent's review and a person's correction of the same revision, the ordinary case.
		Map<String, GdprReport> resolved = StagedReviews.resolve(LADDER,
				List.of(new Candidate("approved", "r1", EARLY, FP, agent),
						new Candidate("approved", "r2", LATE, FP, correction)));

		assertEquals(correction, resolved.get(FP));
	}

	@Test
	@DisplayName("two reviews stamped the same second order the same way on every run")
	public void theObjectIdBreaksATie() {
		GdprReport first = report("first");
		GdprReport second = report("second");

		assertEquals(second, StagedReviews.resolve(LADDER,
				List.of(new Candidate("approved", "r-a", EARLY, FP, first),
						new Candidate("approved", "r-b", EARLY, FP, second))).get(FP));
		// The same set in the other order answers the same, which a scan's order cannot be trusted
		// to give - the startup replay lists whatever the registry lists.
		assertEquals(second, StagedReviews.resolve(LADDER,
				List.of(new Candidate("approved", "r-b", EARLY, FP, second),
						new Candidate("approved", "r-a", EARLY, FP, first))).get(FP));
	}

	@Test
	@DisplayName("a review in a stage nobody listed does not count")
	public void anUnlistedStageIsIgnored() {
		Map<String, GdprReport> resolved = StagedReviews.resolve(List.of("approved"),
				List.of(new Candidate("draft", "r1", LATE, FP, report("in-draft"))));

		assertTrue(resolved.isEmpty(),
				"the list is also what decides which stages a transformation may rest on at all");
	}

	@Test
	@DisplayName("each revision is answered by its own stage, not by one stage for all of them")
	public void differentRevisionsResolveInDifferentStages() {
		GdprReport hr = report("hr");
		GdprReport payroll = report("payroll");

		Map<String, GdprReport> resolved = StagedReviews.resolve(LADDER,
				List.of(new Candidate("draft", "r1", EARLY, "fp1:hr", hr),
						new Candidate("release", "r2", EARLY, "fp1:payroll", payroll)));

		assertEquals(hr, resolved.get("fp1:hr"));
		assertEquals(payroll, resolved.get("fp1:payroll"),
				"a transformation may read one metamodel from draft and another from release");
	}

	@Test
	@DisplayName("a review naming no revision is not a review of anything")
	public void aReviewWithoutAFingerprintIsDropped() {
		assertTrue(StagedReviews.resolve(LADDER,
				List.of(new Candidate("draft", "r1", EARLY, "  ", report("blank")))).isEmpty());
	}

	private static GdprReport report(String id) {
		GdprReport report = GDPRReportFactory.eINSTANCE.createGdprReport();
		report.setReportId(id);
		return report;
	}
}
