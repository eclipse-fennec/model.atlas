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
		assertEquals(Reports.SUBJECT_NS_URI, document.getSubjectNsURI());
		assertEquals(Reports.FINGERPRINT, document.getModelFingerprint());

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

	@SuppressWarnings("unchecked")
	private static WritableScopeService<EObject> scope(ServiceAware<WritableScopeService> aware) throws Exception {
		WritableScopeService<EObject> scope = aware.waitForService(30000);
		assertNotNull(scope, "the test scope service must come up");
		return scope;
	}
}
