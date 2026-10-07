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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * What a withdrawn review can still be answered with.
 * <p>
 * The runtime remembers which subject a report reviewed only in memory, so a restart leaves it
 * unable to clear anything when that report is later deleted. The reviewed objects are the durable
 * record: each one this action wrote onto carries a diagnostic under its producer, and its own
 * metadata carries the fingerprint the rewrite addresses it by.
 * </p>
 */
class ReviewedObjectsTest {

	@Test
	@DisplayName("every object carrying this producer's findings is offered for a rewrite")
	void reviewedObjectsAreFoundByTheirProducer() {
		List<ObjectMetadata> objects = List.of(reviewed("fp1:clinic"), reviewed("fp1:billing"));

		assertEquals(List.of("fp1:clinic", "fp1:billing"),
				GDPRMetadataDiagnosticsStageAction.reviewedObjectsIn(objects));
	}

	@Test
	@DisplayName("an object nobody reviewed is left out")
	void unreviewedObjectsAreLeftOut() {
		ObjectMetadata plain = ManagementFactory.eINSTANCE.createObjectMetadata();
		plain.setFingerprint("fp1:untouched");

		assertTrue(GDPRMetadataDiagnosticsStageAction.reviewedObjectsIn(List.of(plain)).isEmpty(),
				"writing onto it would record a review that never happened");
	}

	@Test
	@DisplayName("another producer's findings are not claimed")
	void foreignProducersAreIgnored() {
		ObjectMetadata compiled = ManagementFactory.eINSTANCE.createObjectMetadata();
		compiled.setFingerprint("fp1:clinic");
		compiled.getDiagnostics().add(diagnostic("QvtTransitionGate"));
		compiled.getDiagnostics().add(diagnostic("gdpr.transformation/clinic.Anonymise"));

		assertTrue(GDPRMetadataDiagnosticsStageAction.reviewedObjectsIn(List.of(compiled)).isEmpty(),
				"a transformation's findings on a metamodel belong to the action that wrote them");
	}

	@Test
	@DisplayName("an object with several review findings is offered once")
	void anObjectIsOfferedOnce() {
		ObjectMetadata twice = reviewed("fp1:clinic");
		twice.getDiagnostics().add(diagnostic(GdprFindingsToDiagnostics.PRODUCER));

		assertEquals(List.of("fp1:clinic"), GDPRMetadataDiagnosticsStageAction.reviewedObjectsIn(List.of(twice)),
				"the rewrite is per object, not per finding");
	}

	@Test
	@DisplayName("an object with no fingerprint cannot be addressed, so it is not offered")
	void objectsWithoutAFingerprintAreSkipped() {
		ObjectMetadata anonymous = ManagementFactory.eINSTANCE.createObjectMetadata();
		anonymous.getDiagnostics().add(diagnostic(GdprFindingsToDiagnostics.PRODUCER));

		assertTrue(GDPRMetadataDiagnosticsStageAction.reviewedObjectsIn(List.of(anonymous)).isEmpty(),
				"the rewrite finds an object by fingerprint; without one there is nothing to look up");
	}

	private static ObjectMetadata reviewed(String fingerprint) {
		ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadata.setFingerprint(fingerprint);
		metadata.getDiagnostics().add(diagnostic(GdprFindingsToDiagnostics.PRODUCER));
		return metadata;
	}

	private static Diagnostic diagnostic(String producer) {
		Diagnostic diagnostic = ManagementFactory.eINSTANCE.createDiagnostic();
		diagnostic.setProducer(producer);
		diagnostic.setCode("gdpr.review");
		return diagnostic;
	}
}
