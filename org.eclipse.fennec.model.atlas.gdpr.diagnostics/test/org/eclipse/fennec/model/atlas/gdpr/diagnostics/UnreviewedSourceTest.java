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
package org.eclipse.fennec.model.atlas.gdpr.diagnostics;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.compliance.report.ReportFactory;
import org.eclipse.fennec.model.compliance.report.ComplianceReport;
import org.eclipse.fennec.model.compliance.report.PackageSubject;
import org.eclipse.fennec.model.compliance.report.TransformationSubject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A transformation report says which metamodels it rests on and which of them nobody reviewed. The
 * second half has to reach the object as an error, because it is the analysis not having run
 * (issue #319).
 */
@DisplayName("A metamodel the report rests on that nobody reviewed")
public class UnreviewedSourceTest {

	private static final String REPORT_ID = "gdpr-fp1-abc-de-20260918-120000";

	private static final ReportFactory REPORTS = ReportFactory.eINSTANCE;

	private static final String REVIEWED = "http://example.org/clinic/1.0.0";
	private static final String UNREVIEWED = "http://example.org/crm/1.0.0";

	private final GdprFindingsToDiagnostics mapper = new GdprFindingsToDiagnostics();

	@Test
	@DisplayName("is an error on the compiled unit, and says the analysis is incomplete rather than clean")
	public void anUnreviewedSourceIsAnError() {
		ComplianceReport report = transformationReport();

		List<Diagnostic> roots = mapper.map(report, REPORT_ID);
		assertEquals(1, roots.size(), "one root per producer, as for any review");
		Diagnostic root = roots.get(0);
		assertEquals(DiagnosticSeverity.ERROR, root.getSeverity(),
				"ERROR means the check did not run - which here it did not");
		assertTrue(root.getMessage().contains("1 metamodel it rests on has no review"), root.getMessage());

		Diagnostic unreviewed = only(root, GdprFindingsToDiagnostics.CODE_UNREVIEWED_SOURCE);
		assertEquals(UNREVIEWED, unreviewed.getTarget(), "addressed by the nsURI of the model nobody reviewed");
		assertEquals(DiagnosticSeverity.ERROR, unreviewed.getSeverity());
		assertTrue(unreviewed.getMessage().contains("fp1:crm"),
				"and it names the revision a review would have had to be of: " + unreviewed.getMessage());
		assertTrue(unreviewed.getMessage().contains("incomplete, not clean"), unreviewed.getMessage());
		assertTrue(unreviewed.getChildren().isEmpty(),
				"it is derived from the subject, not from a finding - there is no review to carry evidence from");
	}

	@Test
	@DisplayName("is not raised for a metamodel that does carry a review")
	public void aReviewedSourceIsSilent() {
		ComplianceReport report = transformationReport();
		((TransformationSubject) report.getSubject()).getSourcePackages().get(1).setReportId("gdpr-crm-1");

		assertClean(mapper.map(report, REPORT_ID),
				"every metamodel reviewed and no finding made: the report is clean, not incomplete");
	}

	@Test
	@DisplayName("is raised once for a model the transformation both reads and writes")
	public void anInoutModelIsNotReportedTwice() {
		ComplianceReport report = transformationReport();
		TransformationSubject subject = (TransformationSubject) report.getSubject();
		// A model declared inout belongs in both lists, as two entries with the same nsURI and
		// fingerprint - which must not become two errors about one model.
		subject.getTargetPackages().add(packageEntry(UNREVIEWED, "fp1:crm", null));

		Diagnostic root = mapper.map(report, REPORT_ID).get(0);
		assertEquals(1, root.getChildren().stream()
				.filter(child -> GdprFindingsToDiagnostics.CODE_UNREVIEWED_SOURCE.equals(child.getCode())).count());
	}

	@Test
	@DisplayName("is a transformation's business only: a review of a package never raises it")
	public void aPackageReviewIsUnaffected() {
		ComplianceReport review = REPORTS.createComplianceReport();
		PackageSubject subject = REPORTS.createPackageSubject();
		subject.setNsURI(REVIEWED);
		subject.setSubjectFingerprint("fp1:clinic");
		review.setSubject(subject);

		assertClean(mapper.map(review, REPORT_ID),
				"a PackageSubject has no packages it rests on, so there is nothing to say about them");
	}

	/* ------------------------------------------------------------------ helpers */

	/** Asserts the roots are the one INFO root of a review that found nothing of concern. */
	private static void assertClean(List<Diagnostic> roots, String why) {
		assertEquals(1, roots.size(), why);
		assertEquals(GdprFindingsToDiagnostics.CODE_REVIEW, roots.get(0).getCode(), why);
		assertEquals(DiagnosticSeverity.INFO, roots.get(0).getSeverity(), why);
		assertTrue(roots.get(0).getChildren().isEmpty(), why);
	}

	/** A report about a compiled transformation: one reviewed metamodel, one not. */
	private static ComplianceReport transformationReport() {
		ComplianceReport report = REPORTS.createComplianceReport();
		report.setReportId("gdpr-flow-1");
		report.setGeneratedBy("qvt-flow-analysis/1");
		TransformationSubject subject = REPORTS.createTransformationSubject();
		subject.setQualifiedName("crm2contacts");
		subject.setSubjectFingerprint("m2x1:unit");
		subject.getSourcePackages().add(packageEntry(REVIEWED, "fp1:clinic", "gdpr-clinic-1"));
		subject.getSourcePackages().add(packageEntry(UNREVIEWED, "fp1:crm", null));
		report.setSubject(subject);
		return report;
	}

	private static PackageSubject packageEntry(String nsURI, String fingerprint, String reportId) {
		PackageSubject entry = REPORTS.createPackageSubject();
		entry.setNsURI(nsURI);
		entry.setSubjectFingerprint(fingerprint);
		entry.setReportId(reportId);
		return entry;
	}

	private static Diagnostic only(Diagnostic root, String code) {
		List<Diagnostic> found = root.getChildren().stream().filter(child -> code.equals(child.getCode())).toList();
		assertEquals(1, found.size(), "exactly one " + code + " node");
		assertNotNull(found.get(0));
		return found.get(0);
	}
}
