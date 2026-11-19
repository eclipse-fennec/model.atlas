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
package org.eclipse.fennec.model.atlas.mgmt.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.time.Instant;

import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link MetadataHash}. */
class MetadataHashTest {

	@Test
	void updateStoresTheComputedHash() {
		ObjectMetadata metadata = metadata();

		MetadataHash.update(metadata);

		assertNotNull(metadata.getMetadataHash());
		assertEquals(MetadataHash.compute(metadata), metadata.getMetadataHash());
	}

	@Test
	void ofPrefersTheStoredHash() {
		ObjectMetadata metadata = metadata();
		metadata.setMetadataHash("stored");

		assertEquals("stored", MetadataHash.of(metadata));
	}

	/** Metadata stored before the hash existed still has one. */
	@Test
	void ofComputesTheHashOfOldMetadata() {
		ObjectMetadata metadata = metadata();

		assertEquals(MetadataHash.compute(metadata), MetadataHash.of(metadata));
	}

	@Test
	void hashChangesWithTheLastChangeTime() {
		ObjectMetadata metadata = metadata();
		String before = MetadataHash.compute(metadata);

		metadata.setLastChangeTime(Instant.parse("2026-11-18T10:00:00Z"));

		assertNotEquals(before, MetadataHash.compute(metadata));
	}

	@Test
	void hashChangesWithTheDiagnostics() {
		ObjectMetadata metadata = metadata();
		String before = MetadataHash.compute(metadata);
		Diagnostic diagnostic = ManagementFactory.eINSTANCE.createDiagnostic();
		diagnostic.setId("d1");
		diagnostic.setMessage("a finding");

		metadata.getDiagnostics().add(diagnostic);

		assertNotEquals(before, MetadataHash.compute(metadata));
	}

	/** The stored hash is not part of what it covers. */
	@Test
	void hashIgnoresAStoredHash() {
		ObjectMetadata metadata = metadata();
		String before = MetadataHash.compute(metadata);

		metadata.setMetadataHash("anything");

		assertEquals(before, MetadataHash.compute(metadata));
	}

	@Test
	void noMetadataHasNoHash() {
		assertNull(MetadataHash.of(null));
	}

	private static ObjectMetadata metadata() {
		ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadata.setContentHash("abc");
		metadata.setVersion("1.0.0");
		metadata.setLastChangeTime(Instant.parse("2026-11-17T10:00:00Z"));
		return metadata;
	}
}
