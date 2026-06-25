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
package org.eclipse.fennec.model.atlas.rest.client.osgi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.model.atlas.rest.client.api.ClientConfiguration;
import org.eclipse.fennec.model.atlas.rest.client.api.ModelAtlasClient;
import org.eclipse.fennec.model.atlas.rest.client.api.NotFoundException;
import org.eclipse.fennec.model.atlas.rest.client.api.PackageDescriptor;
import org.eclipse.fennec.model.atlas.rest.client.api.RemoteEPackageProvider;
import org.eclipse.fennec.model.atlas.rest.client.api.ResolutionMode;
import org.eclipse.fennec.model.atlas.rest.client.api.ResolvedEPackage;
import org.eclipse.fennec.model.atlas.rest.client.api.TransportException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the EAGER (P3-4) and HYBRID (P3-6) pre-fetch. Drives a mocked
 * {@link ModelAtlasClient} / {@link RemoteEPackageProvider} and a recording
 * {@link PackagePublication}, so no OSGi framework or live server is required.
 */
class EagerPrefetchTest {

	/** Records every publish call instead of touching the OSGi registry. */
	private static final class RecordingPublication implements PackagePublication {
		record Published(String nsUri, String scope, String stage, String version) {
		}

		final List<Published> calls = new ArrayList<>();

		@Override
		public boolean publish(EPackage ePackage, String scope, String stage, String version) {
			calls.add(new Published(ePackage.getNsURI(), scope, stage, version));
			return true;
		}
	}

	private ModelAtlasClient client;
	private RemoteEPackageProvider provider;
	private RecordingPublication publication;

	@BeforeEach
	void setUp() {
		client = mock(ModelAtlasClient.class);
		provider = mock(RemoteEPackageProvider.class);
		lenient().when(client.ePackages()).thenReturn(provider);
		publication = new RecordingPublication();
	}

	private static EPackage ePackage(String nsUri) {
		EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
		pkg.setName(nsUri.substring(nsUri.lastIndexOf('/') + 1));
		pkg.setNsPrefix(pkg.getName());
		pkg.setNsURI(nsUri);
		return pkg;
	}

	private static ClientConfiguration config(ResolutionMode mode, boolean strict, List<String> eagerScopes,
			List<String> scopeAllowList) {
		return ClientConfiguration.builder()
				.baseUri(URI.create("http://atlas.test/atlas/rest"))
				.mode(mode)
				.modeStrict(strict)
				.eagerScopes(eagerScopes)
				.scopeAllowList(scopeAllowList)
				.build();
	}

	private EagerPrefetch prefetch(ClientConfiguration config) {
		return new EagerPrefetch(client, publication, config);
	}

	/** A listing entry (Option A): nsUri + owning scope/stage/version. */
	private static PackageDescriptor desc(String nsUri, String scope, String stage, String version) {
		return new PackageDescriptor(nsUri, scope, stage, version);
	}

	@Test
	void publishesEachPackageOfTheConfiguredEagerScopes() {
		when(provider.listPackages("jena"))
				.thenReturn(List.of(desc("urn:a", "jena", "release", "1.0"), desc("urn:b", "jena", "release", "2.0")));
		when(provider.ensureAvailable("urn:a")).thenReturn(Optional.of(ePackage("urn:a")));
		when(provider.ensureAvailable("urn:b")).thenReturn(Optional.of(ePackage("urn:b")));

		int published = prefetch(config(ResolutionMode.EAGER, false, List.of("jena"), List.of())).run();

		assertEquals(2, published);
		assertEquals(2, publication.calls.size());
		// Option A: each package is published with the REAL scope/stage/version the listing carried.
		assertTrue(publication.calls.stream()
				.allMatch(c -> c.scope().equals("jena") && c.stage().equals("release") && c.version() != null));
		assertEquals(List.of("urn:a", "urn:b"),
				publication.calls.stream().map(RecordingPublication.Published::nsUri).toList());
		// listScopeNames must not be consulted when eager.scopes is set.
		verify(client, never()).listScopeNames();
	}

	@Test
	void emptyEagerScopesFallsBackToScopeAllowList() {
		when(provider.listPackages("s1")).thenReturn(List.of(desc("urn:x", "s1", "release", "1.0")));
		when(provider.listPackages("s2")).thenReturn(List.of(desc("urn:y", "s2", "release", "1.0")));
		when(provider.ensureAvailable(anyString()))
				.thenAnswer(i -> Optional.of(ePackage(i.getArgument(0))));

		int published = prefetch(config(ResolutionMode.EAGER, false, List.of(), List.of("s1", "s2"))).run();

		assertEquals(2, published);
		verify(client, never()).listScopeNames();
		verify(provider).listPackages("s1");
		verify(provider).listPackages("s2");
	}

	@Test
	void emptyEagerScopesAndAllowListFallsBackToAllAdvertisedScopes() {
		when(client.listScopeNames()).thenReturn(List.of("only"));
		when(provider.listPackages("only")).thenReturn(List.of(desc("urn:z", "only", "release", "1.0")));
		when(provider.ensureAvailable("urn:z")).thenReturn(Optional.of(ePackage("urn:z")));

		int published = prefetch(config(ResolutionMode.EAGER, false, List.of(), List.of())).run();

		assertEquals(1, published);
		verify(client).listScopeNames();
	}

	@Test
	void unavailableContentIsSkippedNotPublished() {
		when(provider.listPackages("jena")).thenReturn(
				List.of(desc("urn:present", "jena", "release", "1.0"), desc("urn:absent", "jena", "release", "1.0")));
		when(provider.ensureAvailable("urn:present")).thenReturn(Optional.of(ePackage("urn:present")));
		when(provider.ensureAvailable("urn:absent")).thenReturn(Optional.empty());

		int published = prefetch(config(ResolutionMode.EAGER, false, List.of("jena"), List.of())).run();

		assertEquals(1, published);
		assertEquals(List.of("urn:present"),
				publication.calls.stream().map(RecordingPublication.Published::nsUri).toList());
	}

	@Test
	void notFoundScopeIsSkippedAndOtherScopesStillProcessed() {
		when(provider.listPackages("missing")).thenThrow(new NotFoundException("Scope [missing] not found"));
		when(provider.listPackages("good")).thenReturn(List.of(desc("urn:g", "good", "release", "1.0")));
		when(provider.ensureAvailable("urn:g")).thenReturn(Optional.of(ePackage("urn:g")));

		int published = prefetch(config(ResolutionMode.EAGER, false, List.of("missing", "good"), List.of())).run();

		assertEquals(1, published);
		assertEquals(List.of("urn:g"),
				publication.calls.stream().map(RecordingPublication.Published::nsUri).toList());
	}

	@Test
	void strictModeRethrowsOnUnreachableServer() {
		when(provider.listPackages("jena")).thenThrow(new TransportException("connection refused"));

		EagerPrefetch eager = prefetch(config(ResolutionMode.EAGER, true, List.of("jena"), List.of()));

		assertThrows(TransportException.class, eager::run);
		assertTrue(publication.calls.isEmpty());
	}

	@Test
	void nonStrictModeSwallowsUnreachableServerAndReturnsPartialCount() {
		// First scope publishes one package, second scope's listing times out.
		when(provider.listPackages("first")).thenReturn(List.of(desc("urn:first", "first", "release", "1.0")));
		when(provider.ensureAvailable("urn:first")).thenReturn(Optional.of(ePackage("urn:first")));
		when(provider.listPackages("second")).thenThrow(new TransportException("read timed out"));

		int published = prefetch(config(ResolutionMode.EAGER, false, List.of("first", "second"), List.of())).run();

		// The first scope's work survives; the unreachable second scope aborts the rest.
		assertEquals(1, published);
		assertEquals(List.of("urn:first"),
				publication.calls.stream().map(RecordingPublication.Published::nsUri).toList());
	}

	// ---- HYBRID: prefetchListedNsUris -------------------------------------

	private static ClientConfiguration hybridConfig(boolean strict, List<String> eagerNsUris) {
		return ClientConfiguration.builder()
				.baseUri(URI.create("http://atlas.test/atlas/rest"))
				.mode(ResolutionMode.HYBRID)
				.modeStrict(strict)
				.eagerNsUriAllowList(eagerNsUris)
				.build();
	}

	private static ResolvedEPackage resolved(String nsUri, String scope, String stage, String version) {
		return new ResolvedEPackage(ePackage(nsUri), nsUri, scope, "schema", stage, version);
	}

	@Test
	void hybridPublishesOnlyListedNsUrisWithAuthoritativeOrigin() {
		when(provider.resolve("urn:a")).thenReturn(Optional.of(resolved("urn:a", "atlas", "released", "1.0")));
		when(provider.resolve("urn:b")).thenReturn(Optional.of(resolved("urn:b", "jena", "release", "2.1")));

		int published = prefetch(hybridConfig(false, List.of("urn:a", "urn:b"))).prefetchListedNsUris();

		assertEquals(2, published);
		// Each package stamped with the exact origin resolve() reported, not a guess.
		assertEquals(List.of("urn:a", "urn:b"),
				publication.calls.stream().map(RecordingPublication.Published::nsUri).toList());
		assertTrue(publication.calls.stream()
				.anyMatch(c -> c.nsUri().equals("urn:a") && c.scope().equals("atlas") && c.stage().equals("released")
						&& c.version().equals("1.0")));
		assertTrue(publication.calls.stream()
				.anyMatch(c -> c.nsUri().equals("urn:b") && c.scope().equals("jena") && c.stage().equals("release")
						&& c.version().equals("2.1")));
		// Listed-only: discovery (listNsUris/listScopeNames) is never consulted.
		verify(provider, never()).listNsUris(anyString());
		verify(client, never()).listScopeNames();
	}

	@Test
	void hybridSkipsNsUrisNotVisibleFromAnyAllowedScope() {
		when(provider.resolve("urn:here")).thenReturn(Optional.of(resolved("urn:here", "jena", "release", "1.0")));
		when(provider.resolve("urn:gone")).thenReturn(Optional.empty());

		int published = prefetch(hybridConfig(false, List.of("urn:here", "urn:gone"))).prefetchListedNsUris();

		assertEquals(1, published);
		assertEquals(List.of("urn:here"),
				publication.calls.stream().map(RecordingPublication.Published::nsUri).toList());
	}

	@Test
	void hybridStrictModeRethrowsOnUnreachableServer() {
		when(provider.resolve("urn:a")).thenThrow(new TransportException("connection refused"));

		EagerPrefetch hybrid = prefetch(hybridConfig(true, List.of("urn:a")));

		assertThrows(TransportException.class, hybrid::prefetchListedNsUris);
		assertTrue(publication.calls.isEmpty());
	}

	@Test
	void hybridNonStrictModeSwallowsUnreachableServerAndReturnsPartialCount() {
		when(provider.resolve("urn:first")).thenReturn(Optional.of(resolved("urn:first", "atlas", "released", "1.0")));
		when(provider.resolve("urn:second")).thenThrow(new TransportException("read timed out"));

		int published = prefetch(hybridConfig(false, List.of("urn:first", "urn:second"))).prefetchListedNsUris();

		assertEquals(1, published);
		assertEquals(List.of("urn:first"),
				publication.calls.stream().map(RecordingPublication.Published::nsUri).toList());
	}
}
