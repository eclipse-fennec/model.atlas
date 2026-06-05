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
package org.eclipse.fennec.model.atlas.rest.client.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.fennec.model.atlas.rest.client.api.ClientConfiguration;
import org.eclipse.fennec.model.atlas.rest.client.api.ModelAtlasClientException;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * P2-4 — Ecore XMI deserialization. Round-trips the exact bytes the server
 * emits (see {@link EcoreXmiFixtures}) through {@link XmiEPackageDeserializer},
 * both directly and through the {@link RemoteEPackageProviderImpl} fetch path
 * with a mocked transport — no live Atlas.
 */
class XmiEPackageDeserializerTest {

	private final XmiEPackageDeserializer deserializer = new XmiEPackageDeserializer();

	private EPackage deserialize(byte[] xmi, String nsUri) {
		return deserializer.deserialize(new ByteArrayInputStream(xmi), nsUri, "application/xmi");
	}

	@Test
	void deserializesToFullyResolvedEPackage() {
		EPackage pkg = deserialize(EcoreXmiFixtures.sampleXmiBytes(), EcoreXmiFixtures.SAMPLE_NS_URI);

		assertEquals(EcoreXmiFixtures.SAMPLE_NS_URI, pkg.getNsURI());
		assertEquals("sample", pkg.getName());
		assertNotNull(pkg.getEClassifier(EcoreXmiFixtures.SAMPLE_CLASS), "Person classifier should be present");
	}

	@Test
	void resolvesCrossReferencesIntoEcoreAndSelf() {
		EPackage pkg = deserialize(EcoreXmiFixtures.sampleXmiBytes(), EcoreXmiFixtures.SAMPLE_NS_URI);
		EClass person = (EClass) pkg.getEClassifier(EcoreXmiFixtures.SAMPLE_CLASS);

		EStructuralFeature name = person.getEStructuralFeature("name");
		assertNotNull(name.getEType(), "attribute type (EString) should resolve against Ecore");
		assertEquals("EString", name.getEType().getName());

		EStructuralFeature friends = person.getEStructuralFeature("friends");
		assertEquals(person, friends.getEType(), "self reference should resolve to Person");
	}

	@Test
	void eFactoryCreatesInstances() {
		EPackage pkg = deserialize(EcoreXmiFixtures.sampleXmiBytes(), EcoreXmiFixtures.SAMPLE_NS_URI);
		EClass person = (EClass) pkg.getEClassifier(EcoreXmiFixtures.SAMPLE_CLASS);

		EObject instance = pkg.getEFactoryInstance().create(person);

		assertNotNull(instance);
		assertEquals(person, instance.eClass());
	}

	@Test
	void deserializedPackageIsNotRegisteredGlobally() {
		// The package is fetched from the (mock) server; it must not already be local.
		assertFalse(EPackage.Registry.INSTANCE.containsKey(EcoreXmiFixtures.SAMPLE_NS_URI),
				"sample nsURI must not be in the global registry before the test");

		EPackage pkg = deserialize(EcoreXmiFixtures.sampleXmiBytes(), EcoreXmiFixtures.SAMPLE_NS_URI);

		assertNotNull(pkg);
		assertFalse(EPackage.Registry.INSTANCE.containsKey(EcoreXmiFixtures.SAMPLE_NS_URI),
				"deserialization must not publish into the global registry");
	}

	@Test
	void malformedXmi_throwsModelAtlasClientException() {
		byte[] garbage = "this is not xmi".getBytes(StandardCharsets.UTF_8);
		assertThrows(ModelAtlasClientException.class, () -> deserialize(garbage, "urn:ns:bad"));
	}

	@Test
	void fetchedThroughProvider_returnsResolvedPackage() {
		// Wire the REAL deserializer through the P2-3 fetch path with a mocked transport.
		URI base = URI.create("https://atlas.example.org/atlas");
		WebTarget target = mock(WebTarget.class);
		Invocation.Builder request = mock(Invocation.Builder.class);
		when(target.path(anyString())).thenReturn(target);
		when(target.queryParam(anyString(), org.mockito.ArgumentMatchers.any())).thenReturn(target);
		when(target.getUri()).thenReturn(base);
		when(target.request(anyString())).thenReturn(request);

		Response response = mock(Response.class);
		when(response.getStatus()).thenReturn(200);
		when(response.getStatusInfo()).thenReturn(Response.Status.OK);
		when(response.hasEntity()).thenReturn(true);
		when(response.readEntity(byte[].class)).thenReturn(EcoreXmiFixtures.sampleXmiBytes());
		when(response.getMediaType()).thenReturn(new MediaType("application", "xmi"));
		when(request.get()).thenReturn(response);

		ClientConfiguration config = ClientConfiguration.builder().baseUri(base).scopeAllowList(List.of("jena"))
				.build();
		RemoteEPackageProviderImpl provider = new RemoteEPackageProviderImpl(target, config, deserializer,
				() -> List.of());

		Optional<EPackage> pkg = provider.getEPackage(EcoreXmiFixtures.SAMPLE_NS_URI);

		assertTrue(pkg.isPresent());
		assertEquals(EcoreXmiFixtures.SAMPLE_NS_URI, pkg.get().getNsURI());
		assertNotNull(pkg.get().getEClassifier(EcoreXmiFixtures.SAMPLE_CLASS));
	}
}
