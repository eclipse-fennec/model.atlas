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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.eclipse.fennec.model.gdprReportHistory.ChangeKind;
import org.eclipse.fennec.model.gdprReportHistory.ChangeRow;
import org.eclipse.fennec.model.gdprReportHistory.GdprReportHistory;
import org.eclipse.fennec.model.gdprReportHistory.ReportRevision;
import org.eclipse.fennec.model.gdprReportHistory.RevisionOrigin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * A review stored in the reports registry has to produce a document in the documents registry, and
 * a second review of the same model has to become a second revision with a diff against the first.
 * <p>
 * Nothing here touches the action directly - it is Private-Package, and a deployment reaches it only
 * through the workflow, so the test does too.
 */
@ExtendWith(TempDirExtension.class)
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@DisplayName("GDPR review document - rebuild")
public class GdprReportHistoryRebuildIT {

	private static final String SCOPE_FILTER = "(atlas.scope=" + TestAnnotations.SCOPE_NAME + ")";

	@Test
	@TestAnnotations.GdprHistorySetup
	@DisplayName("the first review of a model produces its document")
	public void firstReviewProducesTheDocument(
			@InjectService(cardinality = 0, timeout = 30000, filter = SCOPE_FILTER) //
			ServiceAware<WritableScopeService> aware) throws Exception {

		WritableScopeService<EObject> scope = scope(aware);
		Documents.store(scope, Reports.agentReview());

		GdprReportHistory document = Documents.awaitRevisions(scope, 1);
		assertEquals("clinic", document.getSubjectName());
		assertEquals(Reports.SUBJECT_NS_URI, document.getSubjectIdentifier(),
				"the document is filed under the subject's nsURI, not under one revision's fingerprint");
		assertEquals(Reports.FINGERPRINT, document.getRevisions().get(0).getModelFingerprint());

		ReportRevision only = document.getRevisions().get(0);
		assertEquals(1, only.getRevisionNumber());
		assertEquals("gdpr-clinic-20260922-070000", only.getReportId(),
				"the revision has to be traceable to the stored report");
		assertEquals(RevisionOrigin.AI_AGENT, only.getOrigin());
		assertEquals(2, document.getEvaluations().size(), "one row per evaluated feature");
		assertTrue(document.getChanges().isEmpty(), "there is nothing to diff a first review against");
	}

	@Test
	@TestAnnotations.GdprHistorySetup
	@DisplayName("a second review of the same model is a revision, with a field-level diff")
	public void secondReviewBecomesARevision(
			@InjectService(cardinality = 0, timeout = 30000, filter = SCOPE_FILTER) //
			ServiceAware<WritableScopeService> aware) throws Exception {

		WritableScopeService<EObject> scope = scope(aware);
		Documents.store(scope, Reports.agentReview());
		Documents.awaitRevisions(scope, 1);

		// The live failure this covers: the rebuild has to REPLACE the document it already wrote.
		// Writing it as a create gets a 409, and the document freezes at revision 1.
		Documents.store(scope, Reports.humanCorrection());
		GdprReportHistory document = Documents.awaitRevisions(scope, 2);

		assertEquals(List.of("gdpr-clinic-20260922-070000", "gdpr-clinic-20260922-090000"),
				document.getRevisions().stream().map(ReportRevision::getReportId).toList(),
				"revisions are ordered oldest first");
		assertEquals(RevisionOrigin.HUMAN, document.getRevisions().get(1).getOrigin(),
				"a verdict must not be attributed to the wrong author");

		ChangeRow raised = document.getChanges().stream()
				.filter(change -> "Patient.diagnosis".equals(change.getFeatureId()))
				.filter(change -> "category".equals(change.getField())).findFirst().orElse(null);
		assertNotNull(raised, "raising the category of a feature is the change the document exists for");
		assertEquals(ChangeKind.MODIFIED, raised.getChangeKind());
		assertEquals("PERSONAL_DATA", raised.getOldValue());
		assertEquals("SPECIAL_CATEGORY", raised.getNewValue());

		assertTrue(document.getChanges().stream()
				.anyMatch(change -> "Patient.email".equals(change.getFeatureId())
						&& ChangeKind.ADDED == change.getChangeKind()),
				"a feature the second review added is an addition, not a modification");
	}

	@Test
	@TestAnnotations.GdprHistorySetup
	@DisplayName("a review in a second language gets its own document, not a revision of the first")
	public void eachLanguageGetsItsOwnDocument(
			@InjectService(cardinality = 0, timeout = 30000, filter = SCOPE_FILTER) //
			ServiceAware<WritableScopeService> aware) throws Exception {

		WritableScopeService<EObject> scope = scope(aware);
		Documents.store(scope, Reports.agentReview());
		Documents.awaitRevisions(scope, 1);

		// The same model revision, reviewed against the German consolidation. Merged into one
		// document it would read as a second revision and the change sheet would report every
		// rationale as rewritten, which is the whole content of the sheet.
		Documents.store(scope, Reports.germanReview());

		GdprReportHistory german = Documents.awaitDocument(scope, "DE");
		assertEquals("DE", german.getReportLanguage());
		assertEquals(1, german.getRevisionCount(), "the German document holds the German review alone");
		assertEquals(List.of("gdpr-clinic-de-20260922-080000"),
				german.getRevisions().stream().map(ReportRevision::getReportId).toList());
		assertTrue(german.getChanges().isEmpty(), "a first review in a language has nothing to diff against");

		GdprReportHistory english = Documents.read(scope, "EN");
		assertNotNull(english, "the English document must still be there");
		assertEquals("EN", english.getReportLanguage());
		assertEquals(1, english.getRevisionCount(), "the German review is not a revision of the English document");
		assertEquals(Reports.SUBJECT_NS_URI, english.getSubjectIdentifier(),
				"both documents are about the same subject");
	}

	@Test
	@TestAnnotations.GdprHistorySetup
	@DisplayName("a document in the final stage is still rebuilt, because it is a derived object")
	public void theFinalStageIsRebuildable(
			@InjectService(cardinality = 0, timeout = 30000, filter = SCOPE_FILTER) //
			ServiceAware<WritableScopeService> aware) throws Exception {

		WritableScopeService<EObject> scope = scope(aware);
		// 'release' is the document registry's final stage, and a final stage refuses updates. The
		// document is declared a derived EClass precisely so the Atlas may rewrite its own output
		// there: without that, the first review lands and every later one is refused.
		Documents.store(scope, Reports.agentReview(), "release");
		Documents.awaitRevisions(scope, 1, "release");

		Documents.store(scope, Reports.humanCorrection(), "release");
		GdprReportHistory document = Documents.awaitRevisions(scope, 2, "release");

		assertEquals(2, document.getRevisionCount(), "a rebuild in the final stage must not be refused");
		assertEquals(Reports.SUBJECT_NS_URI, document.getSubjectIdentifier());
		assertNull(Documents.read(scope, Reports.LANGUAGE, "draft"),
				"the document belongs to the stage its reviews were carried out at, and to no other");
	}

	@SuppressWarnings("unchecked")
	private static WritableScopeService<EObject> scope(ServiceAware<WritableScopeService> aware) throws Exception {
		WritableScopeService<EObject> scope = aware.waitForService(30000);
		assertNotNull(scope, "the test scope service must come up");
		return scope;
	}
}
