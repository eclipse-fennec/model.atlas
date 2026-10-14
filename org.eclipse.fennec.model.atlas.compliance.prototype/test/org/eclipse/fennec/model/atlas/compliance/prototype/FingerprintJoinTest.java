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
package org.eclipse.fennec.model.atlas.compliance.prototype;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.emf.osgi.fingerprint.util.FingerprintHelper;
import org.eclipse.fennec.model.compliance.report.ComplianceReport;
import org.eclipse.fennec.model.compliance.report.PackageSubject;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The one thing the whole prototype rests on: that a fingerprint computed here is the fingerprint
 * the reviewer recorded.
 *
 * <p>
 * None of the three artefacts records which revision of a metamodel it was authored against, so an
 * analyser has to compute the key it looks a review up by (OCL plan §8). If the computation does
 * not reproduce what the review carries, every artefact resolves to no review and every analyser
 * answers {@code SOURCE_UNREVIEWED} — a total failure that is indistinguishable, in the report,
 * from a genuine finding about an unreviewed model. So it is proved first, against a review this
 * project's own agent actually produced, and before any analyser exists to depend on it.
 * </p>
 */
@DisplayName("A computed fingerprint matches the one the review recorded")
public class FingerprintJoinTest {

	@Test
	@DisplayName("the stored review of baeder-source is found by computing the package's fingerprint")
	public void computedFingerprintMatchesTheReview() {
		EPackage ePackage = Fixtures.baederSource();
		ComplianceReport review = Fixtures.baederSourceReview();

		String recorded = ((PackageSubject) review.getSubject()).getSubjectFingerprint();
		String computed = FingerprintHelper.fingerprint(ePackage);

		assertNotNull(computed, "a package the analyser cannot fingerprint can be joined to no review at all");
		assertEquals(recorded, computed,
				"the review was produced by fennec-gdpr against this very .ecore; if these differ, the OCL "
						+ "plan's compute-the-fingerprint decision is wrong and the analysers must read the "
						+ "stored ObjectMetadata.fingerprint instead");
	}

	@Test
	@DisplayName("the review is about the package the artefacts name")
	public void reviewIsAboutThisPackage() {
		assertEquals(Fixtures.baederSource().getNsURI(),
				((PackageSubject) Fixtures.baederSourceReview().getSubject()).getNsURI());
	}
}
