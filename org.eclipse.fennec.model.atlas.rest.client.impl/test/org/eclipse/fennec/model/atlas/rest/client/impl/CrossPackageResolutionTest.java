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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTimeoutPreemptively;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.fennec.model.atlas.rest.client.api.ClientConfiguration;
import org.junit.jupiter.api.Test;
import org.mockito.invocation.InvocationOnMock;

import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Cross-package reference resolution on the fetch path (issue #203).
 * <p>
 * A fetched package is only usable once the references it makes into
 * <em>other</em> namespaces are resolved. When package B extends a type from
 * package A, B's XMI carries the super type as an absolute href into A; loading
 * B alone leaves that an unresolved proxy, whose {@code getEPackage()} is
 * {@code null} and which contributes no features — so every feature the
 * subclass inherits silently disappears, and only surfaces much later as
 * "the feature 'x' is not a valid feature" when an instance is deserialized.
 * <p>
 * These tests wire the real {@link XmiEPackageDeserializer} through the fetch
 * path with a mocked transport that serves each package's own document, so
 * fetching one package must pull in the packages it references.
 */
class CrossPackageResolutionTest {

	private static final URI BASE = URI.create("https://atlas.example.org/atlas");

	private final WebTarget target = mock(WebTarget.class);
	/** Per-nsURI documents the fake server serves. */
	private final Map<String, byte[]> documents = new HashMap<>();
	/** Content GETs per nsURI, so a test can assert what was actually fetched. */
	private final Map<String, AtomicInteger> fetches = new ConcurrentHashMap<>();

	CrossPackageResolutionTest() {
		when(target.path(anyString())).thenReturn(target);
		when(target.getUri()).thenReturn(BASE);
		// The nsURI query parameter selects which document the fake server returns.
		when(target.queryParam(eq("nsUri"), any())).thenAnswer(invocation -> targetFor(nsUriArgument(invocation)));
	}

	private RemoteEPackageProviderImpl provider() {
		ClientConfiguration config = ClientConfiguration.builder().baseUri(BASE).scopeAllowList(List.of("jena"))
				.build();
		return new RemoteEPackageProviderImpl(target, config, new XmiEPackageDeserializer(), () -> List.of());
	}

	@Test
	void inheritedFeaturesAreVisibleOnAFetchedSubclass() {
		documents.putAll(EcoreXmiFixtures.inheritanceChainXmi());

		EPackage derived = provider().getEPackage(EcoreXmiFixtures.DERIVED_NS_URI).orElseThrow();

		EClass vendor = (EClass) derived.getEClassifier(EcoreXmiFixtures.DERIVED_CLASS);
		assertNotNull(vendor.getEStructuralFeature(EcoreXmiFixtures.DERIVED_ATTRIBUTE),
				"the subclass's own feature is unaffected");
		assertEquals(1, vendor.getESuperTypes().size(), "the super type reference is carried in the XMI");

		EClass superType = vendor.getESuperTypes().get(0);
		assertFalse(superType.eIsProxy(),
				"the eSuperTypes proxy into " + EcoreXmiFixtures.BASE_NS_URI + " must be resolved by the fetch");
		assertNotNull(superType.getEPackage(),
				"EClass.getEPackage() must not be null — the dynamic EFactory is reached through it");
		assertNotNull(vendor.getEStructuralFeature(EcoreXmiFixtures.INHERITED_ATTRIBUTE),
				"a feature inherited from " + EcoreXmiFixtures.BASE_CLASS + " must be visible on the subclass");
		assertEquals(1, fetchCount(EcoreXmiFixtures.BASE_NS_URI),
				"the referenced package must be fetched once, through the same cache-fronted path");
	}

	@Test
	void inheritanceIsFollowedTransitively() {
		documents.putAll(EcoreXmiFixtures.inheritanceChainXmi());

		EPackage leaf = provider().getEPackage(EcoreXmiFixtures.LEAF_NS_URI).orElseThrow();

		EClass leafClass = (EClass) leaf.getEClassifier(EcoreXmiFixtures.LEAF_CLASS);
		assertNotNull(leafClass.getEStructuralFeature(EcoreXmiFixtures.DERIVED_ATTRIBUTE),
				"the feature from the package one hop up must be visible");
		assertNotNull(leafClass.getEStructuralFeature(EcoreXmiFixtures.INHERITED_ATTRIBUTE),
				"the feature from the package two hops up must be visible — resolution has to recurse");
	}

	@Test
	void sameDocumentReferencesResolveAlongsideCrossPackageOnes() {
		documents.putAll(EcoreXmiFixtures.inheritanceChainXmi());
		documents.put(EcoreXmiFixtures.SERVER_SHAPED_NS_URI, EcoreXmiFixtures.serverShapedXmi());

		EPackage pkg = provider().getEPackage(EcoreXmiFixtures.SERVER_SHAPED_NS_URI).orElseThrow();
		EClass uplink = (EClass) pkg.getEClassifier(EcoreXmiFixtures.SERVER_SHAPED_CLASS);

		assertNotNull(uplink.getEStructuralFeature(EcoreXmiFixtures.INHERITED_ATTRIBUTE),
				"the inherited feature must be visible");
		// Reading the cross-package href makes EMF demand-create a placeholder resource
		// for it, so the document is no longer the only resource in the set — which must
		// not stop a reference written with the server's own resource name from resolving.
		EStructuralFeature object = uplink.getEStructuralFeature(EcoreXmiFixtures.CONTAINMENT_REFERENCE);
		assertNotNull(object.getEType(), "the same-document reference must have a type");
		assertFalse(object.getEType().eIsProxy(),
				"a reference written with the server's resource name must resolve against this document");
		assertSame(pkg.getEClassifier(EcoreXmiFixtures.CONTAINED_CLASS), object.getEType(),
				"it must resolve to the class in this very document");
		assertEquals(0, fetchCount("atlas-client://epackage.ecore/servershaped.ecore"),
				"a document-relative reference names no namespace and must not be fetched");
	}

	@Test
	void mutuallyReferencingPackagesTerminate() {
		documents.putAll(EcoreXmiFixtures.mutualReferenceXmi());

		// A reference cycle across the package boundary must not recurse forever.
		EPackage left = assertTimeoutPreemptively(Duration.ofSeconds(30),
				() -> provider().getEPackage(EcoreXmiFixtures.LEFT_NS_URI).orElseThrow());

		EClass leftClass = (EClass) left.getEClassifier(EcoreXmiFixtures.LEFT_CLASS);
		EStructuralFeature toRight = leftClass.getEStructuralFeature(EcoreXmiFixtures.TO_RIGHT);
		assertNotNull(toRight.getEType(), "the reference into the other package must have a type");
		assertFalse(toRight.getEType().eIsProxy(), "the cross-package reference type must be resolved");
		assertEquals(EcoreXmiFixtures.RIGHT_CLASS, toRight.getEType().getName());
	}

	// ---- fake transport ---------------------------------------------------

	/** The nsURI passed to {@code queryParam("nsUri", …)} — a varargs parameter. */
	private static String nsUriArgument(InvocationOnMock invocation) {
		Object value = invocation.getArgument(1, Object.class);
		if (value instanceof Object[] values && values.length > 0) {
			value = values[0];
		}
		return Objects.toString(value, null);
	}

	/** A target bound to one nsURI, answering with that package's document. */
	private WebTarget targetFor(String nsUri) {
		WebTarget bound = mock(WebTarget.class);
		Invocation.Builder request = mock(Invocation.Builder.class);
		when(bound.request(anyString())).thenReturn(request);
		when(request.header(anyString(), any())).thenReturn(request);
		when(request.get()).thenAnswer(invocation -> {
			fetches.computeIfAbsent(nsUri, key -> new AtomicInteger()).incrementAndGet();
			byte[] document = documents.get(nsUri);
			return document != null ? contentOk(document) : noContent();
		});
		return bound;
	}

	private int fetchCount(String nsUri) {
		AtomicInteger count = fetches.get(nsUri);
		return count == null ? 0 : count.get();
	}

	private static Response contentOk(byte[] body) {
		Response response = mock(Response.class);
		when(response.getStatus()).thenReturn(200);
		when(response.getStatusInfo()).thenReturn(Response.Status.OK);
		when(response.hasEntity()).thenReturn(true);
		when(response.readEntity(byte[].class)).thenReturn(body);
		// new MediaType(...) avoids RuntimeDelegate (no JAX-RS runtime on the test classpath).
		when(response.getMediaType()).thenReturn(new MediaType("application", "xmi"));
		return response;
	}

	private static Response noContent() {
		Response response = mock(Response.class);
		when(response.getStatus()).thenReturn(204);
		when(response.getStatusInfo()).thenReturn(Response.Status.NO_CONTENT);
		when(response.hasEntity()).thenReturn(false);
		return response;
	}
}
