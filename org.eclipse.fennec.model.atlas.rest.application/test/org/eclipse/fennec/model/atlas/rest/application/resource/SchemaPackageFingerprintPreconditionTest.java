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
package org.eclipse.fennec.model.atlas.rest.application.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.core.Response;

/**
 * #274 — the optional {@code fingerprint} read precondition. Addressing stays
 * {@code (scope, [stage], nsUri)}; the fingerprint only asserts which model version the caller
 * expects to find there, so that a transition racing the read cannot hand back a different one.
 */
class SchemaPackageFingerprintPreconditionTest {

	private static final String NS = "https://example.org/demo/1.0";

	/** No pin: the request must behave exactly as it did before #274. */
	@Test
	void noFingerprintGiven_isNoPrecondition() {
		assertNull(SchemaPackagesResource.refuseOnFingerprintMismatch(null, metadata("fp1:abc"), NS));
		assertNull(SchemaPackagesResource.refuseOnFingerprintMismatch("  ", metadata("fp1:abc"), NS));
	}

	@Test
	void matchingFingerprint_passes() {
		assertNull(SchemaPackagesResource.refuseOnFingerprintMismatch("fp1:abc", metadata("fp1:abc"), NS));
	}

	@Test
	void differentVersionAtThatLocation_is412_namingBoth() {
		Response refused = SchemaPackagesResource.refuseOnFingerprintMismatch("fp1:old", metadata("fp1:new"), NS);

		assertNotNull(refused);
		assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), refused.getStatus());
		String message = String.valueOf(refused.getEntity());
		assertTrue(message.contains("fp1:old") && message.contains("fp1:new"),
				"the caller must be told what it asked for and what is actually there: " + message);
	}

	/**
	 * An object stored without a fingerprint cannot satisfy a pin. Serving it anyway would hand
	 * back content whose identity the server cannot vouch for — precisely what the pin rules out.
	 */
	@Test
	void storedObjectWithoutFingerprint_cannotSatisfyAPin() {
		Response refused = SchemaPackagesResource.refuseOnFingerprintMismatch("fp1:old", metadata(null), NS);

		assertNotNull(refused);
		assertEquals(Response.Status.PRECONDITION_FAILED.getStatusCode(), refused.getStatus());
		assertTrue(String.valueOf(refused.getEntity()).contains("no model fingerprint"));
	}

	private static ObjectMetadata metadata(String fingerprint) {
		ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadata.setFingerprint(fingerprint);
		return metadata;
	}
}
