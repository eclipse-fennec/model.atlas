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

	/**
	 * What {@code GDPRReportHistoryStageAction.documentId} makes of the fixtures' fingerprint and
	 * language. The language suffix is always written, so a single-language runtime has it too.
	 */
	static final String DOCUMENT_ID = documentId("EN");

	private static final long TIMEOUT_MS = 30_000;
	private static final long POLL_MS = 100;

	/** The stage the reviews are carried out against; a document is bound to it. */
	static final String REVIEW_STAGE = "draft";

	private Documents() {
	}

	/** Stores a review in the reports registry, the way the REST resource stores one. */
	static void store(WritableScopeService<EObject> scope, GdprReport report) throws Exception {
		store(scope, report, REVIEW_STAGE);
	}

	/** Stores a review into one stage of the reports registry. */
	static void store(WritableScopeService<EObject> scope, GdprReport report, String stage) throws Exception {
		ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadata.setObjectId(report.getReportId());
		metadata.setObjectName(report.getName());
		metadata.setUploadTime(Instant.now());
		metadata.setVersion(Reports.FINGERPRINT);
		metadata.setObjectType(EcoreUtil.getURI(report.eClass()).toString());
		scope.uploadToStageForRegistry(TestAnnotations.REPORT_REGISTRY, stage, report, metadata).getValue();
	}

	/**
	 * Waits for the document to carry exactly {@code revisions} revisions.
	 * <p>
	 * Polling rather than a latch: the rebuild runs on the action's own executor, so the store call
	 * returns before the document exists - by design, because the workflow must not wait on it.
	 */
	static GdprReportHistory awaitRevisions(WritableScopeService<EObject> scope, int revisions) {
		return awaitRevisions(scope, revisions, REVIEW_STAGE);
	}

	/** Waits for the document of one stage to carry exactly {@code revisions} revisions. */
	static GdprReportHistory awaitRevisions(WritableScopeService<EObject> scope, int revisions, String stage) {
		long deadline = System.currentTimeMillis() + TIMEOUT_MS;
		GdprReportHistory last = null;
		while (System.currentTimeMillis() < deadline) {
			last = read(scope, Reports.LANGUAGE, stage);
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
		return read(scope, Reports.LANGUAGE);
	}

	/** The document of one language, or null while it is not there yet. */
	static GdprReportHistory read(WritableScopeService<EObject> scope, String language) {
		return read(scope, language, REVIEW_STAGE);
	}

	/**
	 * The document of one language, from the stage its reviews were carried out at, or null while it
	 * is not there yet. A document lives in that stage: the id does not name it.
	 */
	static GdprReportHistory read(WritableScopeService<EObject> scope, String language, String stage) {
		try {
			EObject stored = scope.getContentFromStageForRegistry(TestAnnotations.DOCUMENT_REGISTRY, stage,
					documentId(language));
			return stored instanceof GdprReportHistory history ? history : null;
		} catch (RuntimeException notThereYet) {
			return null;
		}
	}

	/** Waits for the document of one language to exist. */
	static GdprReportHistory awaitDocument(WritableScopeService<EObject> scope, String language) {
		long deadline = System.currentTimeMillis() + TIMEOUT_MS;
		while (System.currentTimeMillis() < deadline) {
			GdprReportHistory found = read(scope, language);
			if (found != null) {
				return found;
			}
			try {
				Thread.sleep(POLL_MS);
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				fail("interrupted while waiting for the " + language + " GDPR review document");
			}
		}
		return fail("No " + language + " GDPR review document was written within " + TIMEOUT_MS + "ms");
	}

	/** What the action stores one language's document under. */
	static String documentId(String language) {
		// The id the action mints: the flattened nsURI, a digest of the raw one so two nsURIs that
		// flatten alike stay apart, and the corpus language. Not the stage - the document lives in
		// its stage rather than naming it.
		return "gdpr-history-" + segment(Reports.SUBJECT_NS_URI) + "-" + digest(Reports.SUBJECT_NS_URI) + "-"
				+ segment(language).toLowerCase(java.util.Locale.ROOT);
	}

	/** The first eight hex characters of the SHA-256, as the action mints them. */
	private static String digest(String value) {
		try {
			byte[] hash = java.security.MessageDigest.getInstance("SHA-256")
					.digest(value.getBytes(java.nio.charset.StandardCharsets.UTF_8));
			StringBuilder hex = new StringBuilder(8);
			for (int i = 0; hex.length() < 8; i++) {
				hex.append(String.format("%02x", hash[i]));
			}
			return hex.toString();
		} catch (java.security.NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 is not available", e);
		}
	}

	private static String segment(String value) {
		return value.replaceAll("[^A-Za-z0-9]", "-");
	}
}
