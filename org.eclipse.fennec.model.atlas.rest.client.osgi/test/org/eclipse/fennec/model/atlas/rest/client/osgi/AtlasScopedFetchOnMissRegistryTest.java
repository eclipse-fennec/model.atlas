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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.fennec.model.atlas.rest.client.api.PackageDescriptor;
import org.eclipse.fennec.model.atlas.rest.client.api.RemoteEPackageProvider;
import org.eclipse.fennec.model.atlas.rest.client.api.ResolvedEPackage;
import org.junit.jupiter.api.Test;

/**
 * P6-6 — unit tests for the fetch-on-miss bridge.
 */
class AtlasScopedFetchOnMissRegistryTest {

	private static final String SCOPE = "jena";
	private static final String STAGE = "snapshot";
	private static final String NS = "http://example.org/gateway/1.0";

	private static EPackage pkg(String nsUri) {
		EPackage p = EcoreFactory.eINSTANCE.createEPackage();
		p.setName("p");
		p.setNsPrefix("p");
		p.setNsURI(nsUri);
		return p;
	}

	/** Minimal provider that records calls and serves from an in-memory map. */
	private static final class FakeProvider implements RemoteEPackageProvider {
		final Map<String, EPackage> stageFreePackages = new HashMap<>();
		final Map<String, EPackage> stagedPackages = new HashMap<>();
		final AtomicInteger stageFreeCalls = new AtomicInteger();
		final AtomicInteger stagedCalls = new AtomicInteger();
		String lastStageQueried;
		String lastScopeQueried;

		@Override
		public Optional<EPackage> getEPackage(String nsUri) {
			stageFreeCalls.incrementAndGet();
			return Optional.ofNullable(stageFreePackages.get(nsUri));
		}

		@Override
		public Optional<EPackage> getEPackageAtStage(String nsUri, String scopeName, String stage) {
			stagedCalls.incrementAndGet();
			lastScopeQueried = scopeName;
			lastStageQueried = stage;
			return Optional.ofNullable(stagedPackages.get(nsUri));
		}

		@Override
		public List<String> listNsUris(String scopeName) {
			return List.of();
		}

		@Override
		public Optional<EPackage> ensureAvailable(String nsUri) {
			return getEPackage(nsUri);
		}

		@Override
		public Optional<ResolvedEPackage> resolve(String nsUri) {
			return Optional.empty();
		}

		@Override
		public Optional<EPackage> refresh(String nsUri) {
			return Optional.empty();
		}

		@Override
		public List<PackageDescriptor> listPackagesAtStage(String scopeName, String stage) {
			return List.of();
		}
	}

	// ---- cache miss → stage-aware fetch ------------------------------------

	@Test
	void getEPackage_cacheMiss_withStage_callsGetEPackageAtStage() {
		FakeProvider provider = new FakeProvider();
		EPackage expected = pkg(NS);
		provider.stagedPackages.put(NS, expected);

		AtlasScopedFetchOnMissRegistry registry = new AtlasScopedFetchOnMissRegistry(SCOPE, STAGE, provider,
				new EPackageRegistryImpl());

		EPackage result = registry.getEPackage(NS);

		assertSame(expected, result);
		assertEquals(1, provider.stagedCalls.get());
		assertEquals(0, provider.stageFreeCalls.get());
		assertEquals(SCOPE, provider.lastScopeQueried);
		assertEquals(STAGE, provider.lastStageQueried);
	}

	@Test
	void getEPackage_cacheMiss_noStage_callsGetEPackageStageFree() {
		FakeProvider provider = new FakeProvider();
		EPackage expected = pkg(NS);
		provider.stageFreePackages.put(NS, expected);

		// stage == null → stage-free path
		AtlasScopedFetchOnMissRegistry registry = new AtlasScopedFetchOnMissRegistry(SCOPE, null, provider,
				new EPackageRegistryImpl());

		EPackage result = registry.getEPackage(NS);

		assertSame(expected, result);
		assertEquals(1, provider.stageFreeCalls.get());
		assertEquals(0, provider.stagedCalls.get());
	}

	// ---- caching -----------------------------------------------------------

	@Test
	void getEPackage_secondCall_servesFromCache_noExtraFetch() {
		FakeProvider provider = new FakeProvider();
		provider.stagedPackages.put(NS, pkg(NS));

		AtlasScopedFetchOnMissRegistry registry = new AtlasScopedFetchOnMissRegistry(SCOPE, STAGE, provider,
				new EPackageRegistryImpl());

		registry.getEPackage(NS);
		registry.getEPackage(NS);

		assertEquals(1, provider.stagedCalls.get());
	}

	// ---- parent fallback ---------------------------------------------------

	@Test
	void getEPackage_atlasAndCacheMiss_delegatesToParent() {
		FakeProvider provider = new FakeProvider();
		EPackage parentPkg = pkg(NS);
		EPackageRegistryImpl parent = new EPackageRegistryImpl();
		parent.put(NS, parentPkg);

		AtlasScopedFetchOnMissRegistry registry = new AtlasScopedFetchOnMissRegistry(SCOPE, STAGE, provider, parent);

		EPackage result = registry.getEPackage(NS);

		assertSame(parentPkg, result);
		// Atlas fetch was tried first (cache was empty) but returned empty.
		assertEquals(1, provider.stagedCalls.get());
	}

	@Test
	void getEPackage_null_returnsNull() {
		AtlasScopedFetchOnMissRegistry registry = new AtlasScopedFetchOnMissRegistry(SCOPE, STAGE,
				new FakeProvider(), new EPackageRegistryImpl());
		assertNull(registry.getEPackage(null));
	}

	// ---- drift eviction ----------------------------------------------------

	@Test
	void onPackageChanged_evictsCache_nextCallRefetchesFromAtlas() {
		FakeProvider provider = new FakeProvider();
		provider.stagedPackages.put(NS, pkg(NS));

		AtlasScopedFetchOnMissRegistry registry = new AtlasScopedFetchOnMissRegistry(SCOPE, STAGE, provider,
				new EPackageRegistryImpl());

		registry.getEPackage(NS); // populates cache, 1 fetch
		registry.onPackageChanged(NS, pkg(NS)); // evict
		registry.getEPackage(NS); // re-fetch

		assertEquals(2, provider.stagedCalls.get());
	}

	@Test
	void onPackageRemoved_evictsCache_nextCallRefetchesFromAtlas() {
		FakeProvider provider = new FakeProvider();
		provider.stagedPackages.put(NS, pkg(NS));

		AtlasScopedFetchOnMissRegistry registry = new AtlasScopedFetchOnMissRegistry(SCOPE, STAGE, provider,
				new EPackageRegistryImpl());

		registry.getEPackage(NS); // populates cache
		registry.onPackageRemoved(NS); // evict
		registry.getEPackage(NS); // re-fetch

		assertEquals(2, provider.stagedCalls.get());
	}

	// ---- wrong-stage guard (documents Option A gap) -----------------------

	/**
	 * Package exists only in "snapshot" stage, not yet at the final stage.
	 * <p>
	 * The hybrid bridge (this registry) finds it via the stage-explicit fetch.
	 * A pure Option A fallback through the global stage-free registry would return
	 * null because the stage-free endpoint only resolves the final stage.
	 */
	@Test
	void getEPackage_packageOnlyInConfiguredStage_notInFinalStage_hybridBridgeFindsIt() {
		FakeProvider provider = new FakeProvider();
		EPackage snapshotPkg = pkg(NS);
		provider.stagedPackages.put(NS, snapshotPkg); // exists in "snapshot"
		// provider.stageFreePackages is empty → simulates "not yet at final stage"

		AtlasScopedFetchOnMissRegistry registry = new AtlasScopedFetchOnMissRegistry(SCOPE, STAGE, provider,
				new EPackageRegistryImpl());

		EPackage result = registry.getEPackage(NS);

		assertSame(snapshotPkg, result);

		// Verify Option A's fallback path would have failed: the stage-free provider has nothing.
		assertEquals(0, provider.stageFreeCalls.get());
	}
}
