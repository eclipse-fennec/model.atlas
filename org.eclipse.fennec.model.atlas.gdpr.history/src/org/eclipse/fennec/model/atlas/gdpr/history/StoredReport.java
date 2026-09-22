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
package org.eclipse.fennec.model.atlas.gdpr.history;

import java.util.Objects;

import org.eclipse.fennec.model.gdprReport.GdprReport;
import org.eclipse.fennec.model.gdprReportHistory.RevisionOrigin;

/**
 * One stored review, as the history builder receives it: the report together with the two things
 * only the storage layer knows.
 * <p>
 * <b>Why the id is passed in rather than read from the report.</b> A {@code GdprReport} carries no
 * report id of its own, so the Atlas object id is the only thing that ties a revision back to the
 * sealed report someone can go and open. Should the report model ever gain such an attribute, this
 * stays the addressing id and the one in the content becomes a claim to check against it.
 * <p>
 * <b>Why the origin is passed in.</b> Whether a revision came from an agent or from a person is not
 * in the report - {@code generatedBy} holds a model identifier in one case and whatever the uploader
 * put there in the other. The caller has the {@code ObjectMetadata} and can decide; the builder
 * refuses to guess, because attributing a verdict to the wrong author is worse than recording that
 * the answer is not known.
 *
 * @param objectId  the Atlas object id the report is stored under, never {@code null}
 * @param report    the report itself, never {@code null}
 * @param changedBy who produced this revision - an agent identity or a user - may be {@code null}
 * @param origin    where the revision came from; {@code null} is read as
 *                  {@link RevisionOrigin#UNKNOWN}
 */
public record StoredReport(String objectId, GdprReport report, String changedBy, RevisionOrigin origin) {

	/**
	 * @param objectId  the Atlas object id, required
	 * @param report    the report, required
	 * @param changedBy the author, optional
	 * @param origin    the origin, {@code null} becomes {@link RevisionOrigin#UNKNOWN}
	 */
	public StoredReport {
		Objects.requireNonNull(objectId, "objectId");
		Objects.requireNonNull(report, "report");
		if (origin == null) {
			origin = RevisionOrigin.UNKNOWN;
		}
	}

	/**
	 * Convenience for a report whose author and origin are not known.
	 *
	 * @param objectId the Atlas object id
	 * @param report   the report
	 * @return the stored report, with {@link RevisionOrigin#UNKNOWN}
	 */
	public static StoredReport of(String objectId, GdprReport report) {
		return new StoredReport(objectId, report, null, RevisionOrigin.UNKNOWN);
	}
}
