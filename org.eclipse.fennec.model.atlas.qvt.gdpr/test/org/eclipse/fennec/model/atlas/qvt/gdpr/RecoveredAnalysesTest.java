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

import java.util.List;
import java.util.Map;

import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * What a withdrawn transformation report can still be answered with.
 * <p>
 * When a report is deleted the runtime may have no memory of what it wrote - the record is
 * in-memory and a restart empties it. The metamodels themselves are the durable record: each one
 * this action wrote onto carries a diagnostic whose producer names the transformation, and its own
 * metadata carries the nsURI and fingerprint. That is everything the rewrite needs.
 * </p>
 */
class RecoveredAnalysesTest {

	private static final String PREFIX = "gdpr.transformation/";

	@Test
	@DisplayName("every metamodel a transformation wrote onto is recovered from its producer")
	void analysesAreRecoveredFromTheProducer() {
		List<ObjectMetadata> models = List.of(
				model("https://example.org/clinic/1.0", "fp1:clinic", PREFIX + "clinic.Anonymise"),
				model("https://example.org/record/1.0", "fp1:record", PREFIX + "clinic.Anonymise"));

		Map<String, Map<String, String>> recovered = QvtFlowFindingsStageAction.analysesWrittenOn(models);

		assertEquals(1, recovered.size(), "one transformation wrote onto both models");
		assertEquals(Map.of("https://example.org/clinic/1.0", "fp1:clinic", "https://example.org/record/1.0",
				"fp1:record"), recovered.get("clinic.Anonymise"));
	}

	@Test
	@DisplayName("two transformations writing onto one metamodel stay apart")
	void twoTransformationsAreNotMerged() {
		ObjectMetadata shared = model("https://example.org/clinic/1.0", "fp1:clinic", PREFIX + "clinic.Anonymise");
		shared.getDiagnostics().add(diagnostic(PREFIX + "clinic.Export"));

		Map<String, Map<String, String>> recovered = QvtFlowFindingsStageAction.analysesWrittenOn(List.of(shared));

		assertEquals(2, recovered.size(), "each producer owns its own findings and is rewritten on its own");
		assertEquals(Map.of("https://example.org/clinic/1.0", "fp1:clinic"), recovered.get("clinic.Anonymise"));
		assertEquals(Map.of("https://example.org/clinic/1.0", "fp1:clinic"), recovered.get("clinic.Export"));
	}

	@Test
	@DisplayName("another producer's findings are not claimed")
	void foreignProducersAreIgnored() {
		List<ObjectMetadata> models = List.of(
				model("https://example.org/clinic/1.0", "fp1:clinic", "gdpr.review"),
				model("https://example.org/billing/1.0", "fp1:billing", "QvtTransitionGate"));

		assertTrue(QvtFlowFindingsStageAction.analysesWrittenOn(models).isEmpty(),
				"clearing a producer that is not this action's would delete somebody else's work");
	}

	@Test
	@DisplayName("a metamodel with no nsURI or no fingerprint cannot be written to, so it is not offered")
	void unaddressableModelsAreSkipped() {
		ObjectMetadata noNsUri = ManagementFactory.eINSTANCE.createObjectMetadata();
		noNsUri.setFingerprint("fp1:clinic");
		noNsUri.getDiagnostics().add(diagnostic(PREFIX + "clinic.Anonymise"));
		ObjectMetadata noFingerprint = model("https://example.org/record/1.0", null, PREFIX + "clinic.Anonymise");

		assertTrue(QvtFlowFindingsStageAction.analysesWrittenOn(List.of(noNsUri, noFingerprint)).isEmpty(),
				"the rewrite addresses a model by nsURI and finds it by fingerprint; without both it cannot");
	}

	@Test
	@DisplayName("a producer that is the bare prefix names no transformation")
	void aBarePrefixIsNotATransformation() {
		assertTrue(QvtFlowFindingsStageAction
				.analysesWrittenOn(List.of(model("https://example.org/clinic/1.0", "fp1:clinic", PREFIX))).isEmpty());
	}

	private static ObjectMetadata model(String nsURI, String fingerprint, String producer) {
		ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadata.setObjectId(nsURI);
		metadata.setFingerprint(fingerprint);
		if (nsURI != null) {
			metadata.getProperties().put("nsUri", nsURI);
		}
		metadata.getDiagnostics().add(diagnostic(producer));
		return metadata;
	}

	private static Diagnostic diagnostic(String producer) {
		Diagnostic diagnostic = ManagementFactory.eINSTANCE.createDiagnostic();
		diagnostic.setProducer(producer);
		diagnostic.setCode("gdpr.transformation");
		return diagnostic;
	}
}
