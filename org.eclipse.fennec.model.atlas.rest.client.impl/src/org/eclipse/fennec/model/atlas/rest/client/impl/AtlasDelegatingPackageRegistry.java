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

import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.model.atlas.rest.client.api.DriftListener;
import org.eclipse.fennec.model.atlas.rest.client.api.RemoteEPackageProvider;

/**
 * An {@link EPackage.Registry} that falls back to a remote Atlas when an nsURI is
 * unknown locally (P2-8).
 * <p>
 * {@link #getEPackage(String)} resolves in order:
 * <ol>
 * <li>the <b>primary</b> registry (the framework one or {@code EPackage.Registry.INSTANCE})
 * — so a locally shipped package always wins;</li>
 * <li>this registry's own already-fetched entries (direct hit);</li>
 * <li>{@link RemoteEPackageProvider#ensureAvailable(String)} — a hit is cached here
 * so subsequent look-ups are direct.</li>
 * </ol>
 * It is also a {@link DriftListener}: on a package change or removal the cached
 * entry is evicted so the next look-up re-fetches. The own entries are held in a
 * thread-safe map (this class <em>is</em> a {@link ConcurrentHashMap}), since
 * loads and drift eviction can run on different threads.
 * <p>
 * Standalone and reusable: the plain-Java {@code newResourceSet()} installs it,
 * and the Phase-3 OSGi {@code ResourceSetConfigurator} wraps framework
 * ResourceSets with the same delegate.
 */
public class AtlasDelegatingPackageRegistry extends ConcurrentHashMap<String, Object>
		implements EPackage.Registry, DriftListener {

	private static final long serialVersionUID = 1L;

	private final transient EPackage.Registry primary;
	private final transient RemoteEPackageProvider remote;

	public AtlasDelegatingPackageRegistry(EPackage.Registry primary, RemoteEPackageProvider remote) {
		this.primary = Objects.requireNonNull(primary, "primary");
		this.remote = Objects.requireNonNull(remote, "remote");
	}

	@Override
	public EPackage getEPackage(String nsURI) {
		// 1. Primary (local / framework) precedence.
		EPackage local = primary.getEPackage(nsURI);
		if (local != null || nsURI == null) {
			return local;
		}
		// 2. Already fetched here?
		EPackage own = resolveOwn(nsURI);
		if (own != null) {
			return own;
		}
		// 3. Fetch from the Atlas and cache the hit.
		Optional<EPackage> fetched = remote.ensureAvailable(nsURI);
		if (fetched.isPresent()) {
			put(nsURI, fetched.get());
			return fetched.get();
		}
		return null;
	}

	@Override
	public EFactory getEFactory(String nsURI) {
		EFactory localFactory = primary.getEFactory(nsURI);
		if (localFactory != null) {
			return localFactory;
		}
		EPackage ePackage = getEPackage(nsURI);
		return ePackage != null ? ePackage.getEFactoryInstance() : null;
	}

	private EPackage resolveOwn(String nsURI) {
		Object value = get(nsURI);
		if (value instanceof EPackage ePackage) {
			return ePackage;
		}
		if (value instanceof EPackage.Descriptor descriptor) {
			return descriptor.getEPackage();
		}
		return null;
	}

	// ---- DriftListener: evict so the next look-up re-fetches --------------

	@Override
	public java.util.Set<String> heldNsUris() {
		// Our own entries can outlive the provider cache (TTL / size eviction), so
		// the drift watcher must know we still hold them.
		return keySet();
	}

	@Override
	public void onPackageChanged(String nsUri, EPackage newPackage) {
		remove(nsUri);
	}

	@Override
	public void onPackageRemoved(String nsUri) {
		remove(nsUri);
	}
}
