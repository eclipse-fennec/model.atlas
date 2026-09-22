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
package org.eclipse.fennec.model.atlas.gdpr.history.tests;

import static org.junit.jupiter.api.Assertions.fail;

import java.time.Instant;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.eclipse.fennec.model.gdprReport.GdprReport;
import org.eclipse.fennec.model.gdprReportHistory.GdprReportHistory;

/**
 * Storing a review and waiting for the document the action derives from it.
 */
final class Documents {

	/** What {@code GDPRReportHistoryStageAction.documentId} makes of the fixtures' fingerprint. */
	static final String DOCUMENT_ID = "gdpr-history-fp1-clinic100";

	private static final long TIMEOUT_MS = 30_000;
	private static final long POLL_MS = 100;

	private Documents() {
	}

	/** Stores a review in the reports registry, the way the REST resource stores one. */
	static void store(WritableScopeService<EObject> scope, GdprReport report) throws Exception {
		ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadata.setObjectId(report.getReportId());
		metadata.setObjectName(report.getName());
		metadata.setUploadTime(Instant.now());
		metadata.setVersion(Reports.FINGERPRINT);
		metadata.setObjectType(EcoreUtil.getURI(report.eClass()).toString());
		scope.uploadToStageForRegistry(TestAnnotations.REPORT_REGISTRY, "draft", report, metadata).getValue();
	}

	/**
	 * Waits for the document to carry exactly {@code revisions} revisions.
	 * <p>
	 * Polling rather than a latch: the rebuild runs on the action's own executor, so the store call
	 * returns before the document exists - by design, because the workflow must not wait on it.
	 */
	static GdprReportHistory awaitRevisions(WritableScopeService<EObject> scope, int revisions) {
		long deadline = System.currentTimeMillis() + TIMEOUT_MS;
		GdprReportHistory last = null;
		while (System.currentTimeMillis() < deadline) {
			last = read(scope);
			if (last != null && last.getRevisionCount() == revisions) {
				return last;
			}
			try {
				Thread.sleep(POLL_MS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				fail("interrupted while waiting for the GDPR review document");
			}
		}
		return fail(last == null ? "No GDPR review document was written within " + TIMEOUT_MS + "ms"
				: "The GDPR review document stopped at " + last.getRevisionCount() + " revision(s), expected "
						+ revisions);
	}

	/** The document, or null while it is not there yet. */
	static GdprReportHistory read(WritableScopeService<EObject> scope) {
		try {
			EObject stored = scope.getContentFromStageForRegistry(TestAnnotations.DOCUMENT_REGISTRY, "draft",
					DOCUMENT_ID);
			return stored instanceof GdprReportHistory history ? history : null;
		} catch (RuntimeException notThereYet) {
			return null;
		}
	}
}
