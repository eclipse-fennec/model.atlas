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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.fennec.model.atlas.rest.client.api.RemoteEPackageProvider;
import org.eclipse.fennec.model.atlas.rest.client.api.ResolvedEPackage;
import org.eclipse.fennec.model.atlas.rest.client.api.TransportException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the P3-5 LAZY-mode delegating registry. The "framework" registry is
 * a real {@link EPackageRegistryImpl}; the publish seam simulates {@code emf.osgi}
 * binding the configurator by putting the package into that registry (optionally), so
 * the visibility wait, the timeout and the concurrent de-duplication can be exercised
 * without an OSGi framework. The registry resolves origin via
 * {@link RemoteEPackageProvider#resolve(String)}, so the published {@code atlas.*}
 * scope/stage/version are the authoritative values the server reports.
 */
class LazyResolvingPackageRegistryTest {

	private static final String NS = "urn:test:lazy";
	private static final long FAST_TIMEOUT_MS = 2_000L;

	private EPackageRegistryImpl framework;
	private RemoteEPackageProvider remote;

	@BeforeEach
	void setUp() {
		framework = new EPackageRegistryImpl();
		remote = mock(RemoteEPackageProvider.class);
	}

	private static EPackage ePackage(String nsUri) {
		EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
		pkg.setName("p");
		pkg.setNsPrefix("p");
		pkg.setNsURI(nsUri);
		return pkg;
	}

	private static ResolvedEPackage resolved(EPackage pkg, String scope, String stage, String version) {
		return new ResolvedEPackage(pkg, pkg.getNsURI(), scope, "schema", stage, version);
	}

	/** A publish seam that (optionally) mirrors the new package into the framework registry. */
	private final class SimulatingPublication implements PackagePublication {
		private final boolean makeVisible;
		final AtomicInteger publishCount = new AtomicInteger();
		volatile String lastScope;
		volatile String lastStage;
		volatile String lastVersion;

		SimulatingPublication(boolean makeVisible) {
			this.makeVisible = makeVisible;
		}

		@Override
		public boolean publish(EPackage ePackage, String scope, String stage, String version,
				String serverFingerprint) {
			publishCount.incrementAndGet();
			lastScope = scope;
			lastStage = stage;
			lastVersion = version;
			if (makeVisible) {
				framework.put(ePackage.getNsURI(), ePackage);
			}
			return true;
		}
	}

	private LazyResolvingPackageRegistry registry(PackagePublication publication, long timeoutMs) {
		// publishedLookup ns->null: these tests exercise the primary/resolve paths, not the
		// P3-9 published-package fallback. poll every 1 ms, real clock/sleeper — keeps tests fast.
		return new LazyResolvingPackageRegistry(framework, remote, publication, ns -> null, timeoutMs, 1L,
				System::currentTimeMillis, Thread::sleep);
	}

	@Test
	void localPackageWinsWithoutResolving() {
		EPackage local = ePackage(NS);
		framework.put(NS, local);
		SimulatingPublication publication = new SimulatingPublication(true);

		EPackage result = registry(publication, FAST_TIMEOUT_MS).getEPackage(NS);

		assertSame(local, result);
		verify(remote, never()).resolve(NS);
		assertEquals(0, publication.publishCount.get());
	}

	@Test
	void resolvesPublishesWithAuthoritativeOriginAndReturnsOnceVisible() {
		EPackage fetched = ePackage(NS);
		// The package is owned by parent scope "atlas", stage "released" — the exact
		// values the server reports, not a guess from the client's allow-list.
		when(remote.resolve(NS)).thenReturn(Optional.of(resolved(fetched, "atlas", "released", "1.2")));
		SimulatingPublication publication = new SimulatingPublication(true);

		EPackage result = registry(publication, FAST_TIMEOUT_MS).getEPackage(NS);

		assertSame(fetched, result);
		verify(remote, times(1)).resolve(NS);
		assertEquals(1, publication.publishCount.get());
		assertEquals("atlas", publication.lastScope);
		assertEquals("released", publication.lastStage);
		assertEquals("1.2", publication.lastVersion);
	}

	@Test
	void returnsNullOnTimeoutWhenNeverVisible() {
		when(remote.resolve(NS)).thenReturn(Optional.of(resolved(ePackage(NS), "atlas", "released", "1.0")));
		// makeVisible=false: published, but emf.osgi "never binds it" within the timeout.
		SimulatingPublication publication = new SimulatingPublication(false);

		EPackage result = registry(publication, 50L).getEPackage(NS);

		assertNull(result);
		assertEquals(1, publication.publishCount.get());
	}

	@Test
	void returnsNullForNsUriUnknownToTheAtlas() {
		when(remote.resolve(NS)).thenReturn(Optional.empty());
		SimulatingPublication publication = new SimulatingPublication(true);

		EPackage result = registry(publication, FAST_TIMEOUT_MS).getEPackage(NS);

		assertNull(result);
		assertEquals(0, publication.publishCount.get());
	}

	@Test
	void transportErrorReturnsNullAndDoesNotThrow() {
		when(remote.resolve(NS)).thenThrow(new TransportException("connection refused"));
		SimulatingPublication publication = new SimulatingPublication(true);

		EPackage result = registry(publication, FAST_TIMEOUT_MS).getEPackage(NS);

		assertNull(result);
		assertEquals(0, publication.publishCount.get());
	}

	@Test
	void getEFactoryResolvesThroughLazyFetch() {
		EPackage fetched = ePackage(NS);
		when(remote.resolve(NS)).thenReturn(Optional.of(resolved(fetched, "atlas", "released", "1.0")));
		SimulatingPublication publication = new SimulatingPublication(true);

		assertNotNull(registry(publication, FAST_TIMEOUT_MS).getEFactory(NS));
		assertSame(fetched.getEFactoryInstance(), framework.getEFactory(NS));
	}

	@Test
	void concurrentCallsForSameNsUriDeduplicateToOneResolveAndPublish() throws Exception {
		int threads = 8;
		EPackage fetched = ePackage(NS);
		// Gate the owner's resolve until all callers are in, so they pile up on the shared future.
		CountDownLatch release = new CountDownLatch(1);
		when(remote.resolve(NS)).thenAnswer(invocation -> {
			release.await(5, TimeUnit.SECONDS);
			return Optional.of(resolved(fetched, "atlas", "released", "1.0"));
		});
		SimulatingPublication publication = new SimulatingPublication(true);
		LazyResolvingPackageRegistry registry = registry(publication, FAST_TIMEOUT_MS);

		ExecutorService pool = Executors.newFixedThreadPool(threads);
		try {
			CountDownLatch ready = new CountDownLatch(threads);
			@SuppressWarnings("unchecked")
			Future<EPackage>[] results = new Future[threads];
			for (int i = 0; i < threads; i++) {
				results[i] = pool.submit(() -> {
					ready.countDown();
					return registry.getEPackage(NS);
				});
			}
			// All callers have entered; let the single owner's resolve proceed.
			assertTrue(ready.await(5, TimeUnit.SECONDS));
			Thread.sleep(50);
			release.countDown();

			for (Future<EPackage> result : results) {
				assertSame(fetched, result.get(5, TimeUnit.SECONDS));
			}
		} finally {
			pool.shutdownNow();
		}

		verify(remote, times(1)).resolve(NS);
		assertEquals(1, publication.publishCount.get());
	}

	@Test
	void nullNsUriIsDelegatedToPrimary() {
		assertNull(registry(new SimulatingPublication(true), FAST_TIMEOUT_MS).getEPackage(null));
		verifyNoInteractions(remote);
	}
}
