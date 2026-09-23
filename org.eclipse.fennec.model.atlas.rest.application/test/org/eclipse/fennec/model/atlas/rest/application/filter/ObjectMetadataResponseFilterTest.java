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
package org.eclipse.fennec.model.atlas.rest.application.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;

/**
 * #273 — the Atlas origin headers. The stage-explicit content endpoints reported no origin,
 * so a client that fetched a package at a stage could not tell which of several live versions
 * of that nsURI it had received. The filter derives the headers from the {@link ObjectMetadata}
 * a resource attaches, which is why they are asserted here against that metadata directly.
 */
class ObjectMetadataResponseFilterTest {

	/**
	 * #292 — a diagnostics write leaves {@code lastChangeTime} alone, so the metadata validator
	 * has to see the diagnostics themselves; otherwise a client holding the metadata ETag is told
	 * {@code 304} while the object's findings changed. The content validator must not move: the
	 * bytes did not change.
	 */
	@Test
	void metadataValidatorFollowsTheDiagnostics() {
		ObjectMetadata metadata = metadata("jena", "release", "1.2.0", "fp1:abc");
		metadata.setContentHash("hash");
		String before = ObjectMetadataResponseFilter.baseValidator(metadata,
				ObjectMetadataResponseFilter.CacheTarget.METADATA);
		String contentBefore = ObjectMetadataResponseFilter.baseValidator(metadata,
				ObjectMetadataResponseFilter.CacheTarget.CONTENT);

		org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic finding = ManagementFactory.eINSTANCE
				.createDiagnostic();
		finding.setId("d1");
		finding.setProducer("checker");
		finding.setCode("unresolved-reference");
		finding.setSeverity(org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity.WARNING);
		finding.setMessage("b is not visible");
		metadata.getDiagnostics().add(finding);
		String withFinding = ObjectMetadataResponseFilter.baseValidator(metadata,
				ObjectMetadataResponseFilter.CacheTarget.METADATA);

		assertFalse(before.equals(withFinding), "a new finding changes the metadata validator");
		assertEquals(contentBefore, ObjectMetadataResponseFilter.baseValidator(metadata,
				ObjectMetadataResponseFilter.CacheTarget.CONTENT), "the content validator does not move");

		finding.setStatus(org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticStatus.RESOLVED);
		String resolved = ObjectMetadataResponseFilter.baseValidator(metadata,
				ObjectMetadataResponseFilter.CacheTarget.METADATA);
		assertFalse(withFinding.equals(resolved), "a status change on the same finding changes it too");

		metadata.getDiagnostics().clear();
		assertEquals(before, ObjectMetadataResponseFilter.baseValidator(metadata,
				ObjectMetadataResponseFilter.CacheTarget.METADATA),
				"an object without diagnostics keeps the validator it had before #292");
	}

	@Test
	void stampsScopeStageVersionAndFingerprint() {
		MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

		ObjectMetadataResponseFilter.stampProvenance(headers, metadata("jena", "draft", "1.2.0", "fp1:abc"));

		assertEquals("jena", headers.getFirst(ObjectMetadataResponseFilter.HEADER_SCOPE));
		assertEquals("draft", headers.getFirst(ObjectMetadataResponseFilter.HEADER_STAGE));
		assertEquals("1.2.0", headers.getFirst(ObjectMetadataResponseFilter.HEADER_VERSION));
		assertEquals("fp1:abc", headers.getFirst(ObjectMetadataResponseFilter.HEADER_FINGERPRINT));
	}

	/**
	 * The fingerprint is the semantic identity of a <em>model</em>, and the server sets it for
	 * EPackages only. An instance response must therefore omit the header rather than carry an
	 * empty one, which a client could mistake for "this version has no fingerprint".
	 */
	@Test
	void omitsFingerprintWhenTheObjectIsNotAPackage() {
		MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

		ObjectMetadataResponseFilter.stampProvenance(headers, metadata("jena", "draft", "1.2.0", null));

		assertEquals("jena", headers.getFirst(ObjectMetadataResponseFilter.HEADER_SCOPE));
		assertFalse(headers.containsKey(ObjectMetadataResponseFilter.HEADER_FINGERPRINT),
				"an instance has no model fingerprint, so the header must be absent");
	}

	@Test
	void blankValuesAreOmitted() {
		MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

		ObjectMetadataResponseFilter.stampProvenance(headers, metadata("jena", "draft", "  ", ""));

		assertFalse(headers.containsKey(ObjectMetadataResponseFilter.HEADER_VERSION));
		assertFalse(headers.containsKey(ObjectMetadataResponseFilter.HEADER_FINGERPRINT));
	}

	/** Same policy as the cache validators: a resource that already stated an origin keeps it. */
	@Test
	void doesNotOverwriteAHeaderTheResourceAlreadySet() {
		MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();
		headers.putSingle(ObjectMetadataResponseFilter.HEADER_STAGE, "already-set");

		ObjectMetadataResponseFilter.stampProvenance(headers, metadata("jena", "draft", "1.2.0", "fp1:abc"));

		assertEquals("already-set", headers.getFirst(ObjectMetadataResponseFilter.HEADER_STAGE));
		assertEquals("jena", headers.getFirst(ObjectMetadataResponseFilter.HEADER_SCOPE));
	}

	@Test
	void nullMetadataIsANoOp() {
		MultivaluedMap<String, Object> headers = new MultivaluedHashMap<>();

		ObjectMetadataResponseFilter.stampProvenance(headers, null);

		assertNull(headers.getFirst(ObjectMetadataResponseFilter.HEADER_SCOPE));
	}

	private static ObjectMetadata metadata(String scope, String stage, String version, String fingerprint) {
		ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadata.setScope(scope);
		metadata.setStage(stage);
		metadata.setVersion(version);
		metadata.setFingerprint(fingerprint);
		return metadata;
	}
}
