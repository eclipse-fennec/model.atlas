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

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.model.atlas.rest.client.api.DriftListener;
import org.eclipse.fennec.model.atlas.rest.client.api.PackageDrift;
import org.eclipse.fennec.model.atlas.rest.client.api.RemoteEPackageProvider;

/**
 * P6-6 hybrid fetch-on-miss bridge: the middle layer of the scope-specific registry chain.
 * <p>
 * Sits between the stock {@code EPackageRegistry} (which aggregates prefetched packages
 * tagged {@code atlas.scope=S[, atlas.stage=ST]}) and the global parent registry:
 * <ol>
 * <li>Stock {@code EPackageRegistry} — holds prefetched packages, no fetch-on-miss.</li>
 * <li><b>This registry</b> — on a miss, fetches stage-explicitly from the Atlas at the
 * configured scope + stage, caches the result.</li>
 * <li>Parent (global default registry) — final fallback.</li>
 * </ol>
 * This gives stage-aware fetch-on-miss (packages added after startup, or packages that exist
 * only at the configured stage and not yet at the final stage) without forking the stock
 * emf.osgi configurable registry mechanism: the stock registry still does the aggregation
 * and chaining; this bridge is simply its {@code parentRegistry.target}.
 * <p>
 * Registered as an OSGi {@code EPackage.Registry} service by {@link AtlasClientComponent}
 * with properties {@code atlas.scope}, {@code atlas.stage} (when a non-null stage is
 * configured) and {@code atlas.fetch.on.miss=true} — the latter is what the stock
 * registry's {@code parentRegistry.target} filter matches.
 * <p>
 * Also a {@link DriftListener}: on a package change or removal the cached entry is evicted
 * so the next lookup re-fetches from Atlas at the correct stage.
 */
class AtlasScopedFetchOnMissRegistry extends ConcurrentHashMap<String, Object>
		implements EPackage.Registry, DriftListener {

	static final String FETCH_ON_MISS_PROPERTY = "atlas.fetch.on.miss";

	private static final long serialVersionUID = 1L;

	private final String scope;
	/** Null means stage-free (final stage). */
	private final String stage;
	private final transient RemoteEPackageProvider provider;
	private final transient EPackage.Registry parent;
	/** Told what this bridge holds (#277); {@link StagedPackageSink#NONE} when nothing is listening. */
	private final transient StagedPackageSink sink;

	AtlasScopedFetchOnMissRegistry(String scope, String stage, RemoteEPackageProvider provider,
			EPackage.Registry parent) {
		this(scope, stage, provider, parent, StagedPackageSink.NONE);
	}

	AtlasScopedFetchOnMissRegistry(String scope, String stage, RemoteEPackageProvider provider,
			EPackage.Registry parent, StagedPackageSink sink) {
		this.scope = Objects.requireNonNull(scope, "scope");
		this.stage = stage;
		this.provider = Objects.requireNonNull(provider, "provider");
		this.parent = Objects.requireNonNull(parent, "parent");
		this.sink = Objects.requireNonNull(sink, "sink");
	}

	@Override
	public EPackage getEPackage(String nsURI) {
		if (nsURI == null) {
			return null;
		}
		// 1. Own cache (populated by previous miss-fetches).
		EPackage own = resolveOwn(nsURI);
		if (own != null) {
			return own;
		}
		// 2. Stage-aware fetch from Atlas. If a stage is configured, hit the stage-explicit
		// endpoint so a package that exists only at that stage (not yet at final) is found.
		Optional<EPackage> fetched = (stage != null)
				? provider.getEPackageAtStage(nsURI, scope, stage)
				: provider.getEPackage(nsURI);
		if (fetched.isPresent()) {
			EPackage ePackage = fetched.get();
			// Register on the *transition*, not on the fetch. Two concurrent misses on one nsURI
			// both fetch and both arrive here; a plain put would tell the sink twice about one
			// held entry, and a refcounting sink would then never drop it (#277).
			Object previous = putIfAbsent(nsURI, ePackage);
			if (previous == null) {
				sink.registered(ePackage);
				return ePackage;
			}
			// Lost the race: the winner's package is the one this bridge holds.
			EPackage winner = resolveOwn(nsURI);
			return winner != null ? winner : ePackage;
		}
		// 3. Delegate to parent (global/default registry).
		return parent.getEPackage(nsURI);
	}

	@Override
	public EFactory getEFactory(String nsURI) {
		EFactory parentFactory = parent.getEFactory(nsURI);
		if (parentFactory != null) {
			return parentFactory;
		}
		EPackage pkg = getEPackage(nsURI);
		return pkg != null ? pkg.getEFactoryInstance() : null;
	}

	private EPackage resolveOwn(String nsURI) {
		return asEPackage(get(nsURI));
	}

	private static EPackage asEPackage(Object value) {
		if (value instanceof EPackage ePackage) {
			return ePackage;
		}
		if (value instanceof EPackage.Descriptor descriptor) {
			return descriptor.getEPackage();
		}
		return null;
	}

	// ---- DriftListener: evict so the next lookup re-fetches at the correct stage ----

	@Override
	public java.util.Set<String> heldNsUris() {
		// Stage-explicit fetches bypass the provider cache, so the drift watcher can
		// only know about these entries through us.
		return keySet();
	}

	/**
	 * Only changes at this bridge's own location concern it (#276). A bridge holding the
	 * {@code release} version of an nsURI used to evict it because the {@code draft} version moved:
	 * drift was reported as a bare nsURI, which does not identify a version, so the safe response
	 * was to drop a perfectly current package and re-fetch it.
	 */
	@Override
	public boolean acceptsDrift(PackageDrift drift) {
		return drift == null || drift.concerns(scope, stage);
	}

	@Override
	public void onPackageChanged(String nsUri, EPackage newPackage) {
		evict(nsUri);
	}

	@Override
	public void onPackageRemoved(String nsUri) {
		evict(nsUri);
	}

	/**
	 * Drop one entry and tell the sink which package went. {@code remove} hands back the evicted
	 * value, which an {@code EPackage.Registry} may hold as a {@link EPackage.Descriptor}, so it is
	 * unwrapped the same way a lookup unwraps it.
	 */
	private void evict(String nsUri) {
		Object evicted = remove(nsUri);
		EPackage ePackage = asEPackage(evicted);
		if (ePackage != null) {
			sink.evicted(ePackage);
		}
	}

	/** Drop everything, telling the sink about each — the bridge is going away. */
	void dispose() {
		for (String nsUri : keySet().toArray(String[]::new)) {
			evict(nsUri);
		}
	}

	String getScope() {
		return scope;
	}

	String getStage() {
		return stage;
	}
}
