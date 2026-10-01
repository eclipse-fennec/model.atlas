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

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.fennec.model.gdprReport.GdprReport;

/**
 * Which review speaks for a metamodel revision, when the reviews may be spread over several stages.
 *
 * <p>
 * Two orderings meet here and which is the outer one is the whole content of this class.
 * </p>
 * <ul>
 * <li><b>Stage order is outer.</b> The stages are searched in the order the deployment configured
 * them, and the first stage that has any review of a fingerprint settles it. A later stage never
 * overrides an earlier one, <em>not even with a more recent report</em> - the list is a statement
 * about which stages are authoritative, and recency cannot outrank that. A deployment that wants
 * the newest review wherever it is says so by listing the stages the other way round.</li>
 * <li><b>Recency is inner.</b> Within one stage the latest review wins, which is the existing rule
 * and exists because one revision is reviewed more than once as a matter of course - an agent's
 * review and a person's correction of it are the ordinary case. Ordered by what the report states,
 * then by what the Atlas recorded, with the objectId breaking a tie so two runs agree.</li>
 * </ul>
 * <p>
 * Nothing here changes where a review is <em>stored</em>: a review still belongs to the stage of the
 * subject it reviewed. What is dropped is only the assumption that that stage is also the stage of
 * the transformation whose analysis rests on it - package visibility runs along a ladder, so a
 * transformation in an early stage routinely reads a metamodel of a later one.
 */
final class StagedReviews {

	/**
	 * One stored review, as a scan found it.
	 *
	 * @param stage       the stage it was found in
	 * @param objectId    its id in that stage, which breaks a tie between two equally recent ones
	 * @param at          when the review was carried out; never {@code null}
	 * @param fingerprint the revision it is about
	 * @param report      the review itself
	 */
	record Candidate(String stage, String objectId, Instant at, String fingerprint, GdprReport report) {
	}

	private StagedReviews() {
	}

	/**
	 * The review that speaks for each fingerprint.
	 *
	 * @param stageOrder the stages in priority order; a candidate from a stage not listed here is
	 *                   ignored, so the list is also what decides which stages count at all
	 * @param candidates every review found, in any order
	 * @return fingerprint to the review that speaks for it
	 */
	static Map<String, GdprReport> resolve(List<String> stageOrder, List<Candidate> candidates) {
		Map<String, Candidate> best = new LinkedHashMap<>();
		for (Candidate candidate : candidates) {
			int rank = stageOrder.indexOf(candidate.stage());
			if (rank < 0 || candidate.fingerprint() == null || candidate.fingerprint().isBlank()) {
				continue;
			}
			Candidate incumbent = best.get(candidate.fingerprint());
			if (incumbent == null || beats(candidate, rank, incumbent, stageOrder)) {
				best.put(candidate.fingerprint(), candidate);
			}
		}
		Map<String, GdprReport> resolved = new LinkedHashMap<>();
		best.forEach((fingerprint, candidate) -> resolved.put(fingerprint, candidate.report()));
		return resolved;
	}

	private static boolean beats(Candidate candidate, int rank, Candidate incumbent, List<String> stageOrder) {
		int incumbentRank = stageOrder.indexOf(incumbent.stage());
		if (rank != incumbentRank) {
			return rank < incumbentRank;
		}
		if (!candidate.at().equals(incumbent.at())) {
			return candidate.at().isAfter(incumbent.at());
		}
		return candidate.objectId().compareTo(incumbent.objectId()) > 0;
	}
}
