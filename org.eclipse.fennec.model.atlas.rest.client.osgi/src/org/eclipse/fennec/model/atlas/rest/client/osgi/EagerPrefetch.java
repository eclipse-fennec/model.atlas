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

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.model.atlas.rest.client.api.ClientConfiguration;
import org.eclipse.fennec.model.atlas.rest.client.api.ModelAtlasClient;
import org.eclipse.fennec.model.atlas.rest.client.api.NotFoundException;
import org.eclipse.fennec.model.atlas.rest.client.api.PackageDescriptor;
import org.eclipse.fennec.model.atlas.rest.client.api.RemoteEPackageProvider;
import org.eclipse.fennec.model.atlas.rest.client.api.ResolvedEPackage;
import org.eclipse.fennec.model.atlas.rest.client.api.TransportException;

/**
 * The activation-time pre-fetch behind the EAGER (P3-4) and HYBRID (P3-6) modes.
 * <p>
 * On activation (driven by {@link AtlasClientComponent}) this fetches EPackages
 * through the shared Phase-2 client and publishes them via the supplied
 * {@link PackagePublication} seam (the {@link RemoteEPackagePublisher}), so the local
 * framework {@code EPackage.Registry} mirrors the Atlas the moment the component is
 * up. Drift detection (the Phase-2 watcher) keeps it fresh afterwards. Two entry
 * points:
 * <ul>
 * <li>{@link #run()} — <strong>EAGER</strong>: lists each configured scope's final-stage
 * packages via {@link RemoteEPackageProvider#listPackages(String)} and publishes each with
 * the owning scope/stage/version that listing already carries (Option A);</li>
 * <li>{@link #prefetchListedNsUris()} — <strong>HYBRID</strong>: publishes only the
 * nsURIs in {@code eager.nsuri.allow.list}, resolving each one's authoritative origin
 * via {@link RemoteEPackageProvider#resolve(String)} (exact scope/stage/version);
 * everything else is left to the LAZY registry.</li>
 * </ul>
 * <p>
 * <strong>Scope resolution</strong> (first non-empty wins): {@code eager.scopes};
 * else {@code scope.allow.list} (the design's "all configured" reading); else every
 * scope the server advertises ({@code GET /scopes}).
 * <p>
 * <strong>Stage.</strong> Reads are stage-free (P5-7): both modes target each scope's final
 * stage, resolved server-side with inheritance. The published {@code atlas.stage} is advisory
 * provenance taken from the server metadata — for EAGER, from the listing
 * ({@link RemoteEPackageProvider#listPackages(String)}); for HYBRID, from
 * {@link RemoteEPackageProvider#resolve(String)}. {@code eager.stages} is not an independently
 * reachable knob here.
 * <p>
 * <strong>Reachability.</strong> A {@link TransportException} (server unreachable)
 * is fatal only when {@code mode.strict=true}: it is rethrown so component
 * activation fails with a clear log line. With {@code mode.strict=false} (default)
 * it is logged at {@code WARNING} and the pre-fetch stops gracefully, to be retried
 * on the next configuration update. A {@link NotFoundException} on a single scope or
 * nsURI (the server is reachable, the item simply is not there) is logged and
 * skipped regardless of strictness.
 */
final class EagerPrefetch {

	private static final Logger LOGGER = Logger.getLogger(EagerPrefetch.class.getName());

	private final ModelAtlasClient client;
	private final PackagePublication publication;
	private final ClientConfiguration config;

	EagerPrefetch(ModelAtlasClient client, PackagePublication publication, ClientConfiguration config) {
		this.client = Objects.requireNonNull(client, "client");
		this.publication = Objects.requireNonNull(publication, "publication");
		this.config = Objects.requireNonNull(config, "config");
	}

	/**
	 * Pre-fetch and publish every EPackage of the resolved scopes.
	 *
	 * @return the number of packages newly published
	 * @throws TransportException if the server is unreachable and {@code mode.strict=true}
	 */
	int run() {
		int published = 0;
		List<String> scopes;
		try {
			scopes = resolveScopes();
		} catch (TransportException unreachable) {
			return failOrSkip(unreachable, published);
		}
		for (String scope : scopes) {
			try {
				published += prefetchScope(scope);
			} catch (TransportException unreachable) {
				// The whole server is down — no point trying the remaining scopes.
				return failOrSkip(unreachable, published);
			}
		}
		int total = published;
		LOGGER.log(Level.INFO, () -> "EAGER pre-fetch for " + config.getBaseUri() + " published " + total
				+ " EPackage(s) from " + scopes.size() + " scope(s)");
		return published;
	}

	/**
	 * HYBRID pre-fetch: publish only the nsURIs listed in {@code eager.nsuri.allow.list},
	 * resolving each one's authoritative origin (owning scope/stage/version) via
	 * {@link RemoteEPackageProvider#resolve(String)}. Everything not listed is left to
	 * the LAZY registry to fetch on demand. An nsURI not visible from any allowed scope
	 * (or filtered by the nsURI allow/deny gate) is logged and skipped; reachability is
	 * handled exactly as in {@link #run()}.
	 *
	 * @return the number of packages newly published
	 * @throws TransportException if the server is unreachable and {@code mode.strict=true}
	 */
	int prefetchListedNsUris() {
		RemoteEPackageProvider provider = client.ePackages();
		List<String> nsUris = config.getEagerNsUriAllowList();
		int published = 0;
		for (String nsUri : nsUris) {
			Optional<ResolvedEPackage> resolved;
			try {
				resolved = provider.resolve(nsUri);
			} catch (TransportException unreachable) {
				return failOrSkip(unreachable, published);
			}
			if (resolved.isEmpty()) {
				LOGGER.log(Level.WARNING, () -> "HYBRID pre-fetch: nsURI '" + nsUri
						+ "' from eager.nsuri.allow.list is not visible from any allowed scope; skipping it");
				continue;
			}
			ResolvedEPackage rp = resolved.get();
			if (publication.publish(rp.getEPackage(), rp.getScope(), rp.getStage(), rp.getVersion(), rp.getFingerprint())) {
				published++;
			}
		}
		int total = published;
		LOGGER.log(Level.INFO, () -> "HYBRID pre-fetch for " + config.getBaseUri() + " published " + total + " of "
				+ nsUris.size() + " listed nsURI(s); the rest resolve lazily");
		return published;
	}

	/** {@code eager.scopes} → {@code scope.allow.list} → all advertised scopes. */
	private List<String> resolveScopes() {
		if (!config.getEagerScopes().isEmpty()) {
			return config.getEagerScopes();
		}
		if (!config.getScopeAllowList().isEmpty()) {
			return config.getScopeAllowList();
		}
		return client.listScopeNames();
	}

	private int prefetchScope(String scope) {
		RemoteEPackageProvider provider = client.ePackages();
		List<PackageDescriptor> packages;
		try {
			// The single listing already carries each package's owning scope/stage/version
			// (Option A) — so EAGER publishes the REAL provenance without the per-package
			// metadata round-trip resolve() would add, and no longer stamps stage=null.
			packages = provider.listPackages(scope);
		} catch (NotFoundException missing) {
			LOGGER.log(Level.WARNING, missing,
					() -> "EAGER pre-fetch: scope '" + scope + "' not found on the server; skipping it");
			return 0;
		}
		int published = 0;
		for (PackageDescriptor descriptor : packages) {
			String nsUri = descriptor.nsUri();
			Optional<EPackage> ePackage;
			try {
				ePackage = provider.ensureAvailable(nsUri);
			} catch (NotFoundException missing) {
				LOGGER.log(Level.WARNING, missing, () -> "EAGER pre-fetch: nsURI '" + nsUri + "' listed in scope '"
						+ scope + "' but no longer retrievable; skipping it");
				continue;
			}
			if (ePackage.isEmpty()) {
				LOGGER.log(Level.FINE, () -> "EAGER pre-fetch: nsURI '" + nsUri + "' listed in scope '" + scope
						+ "' but its content was not available (filtered or empty); skipping it");
				continue;
			}
			// Owning scope from the listing (a parent for an inherited package), like HYBRID's
			// resolve()-based provenance; fall back to the queried scope if the server omitted it.
			String originScope = descriptor.scope() != null && !descriptor.scope().isBlank() ? descriptor.scope()
					: scope;
			if (publication.publish(ePackage.get(), originScope, descriptor.stage(), descriptor.version(),
					descriptor.fingerprint())) {
				published++;
			}
		}
		return published;
	}

	private int failOrSkip(TransportException unreachable, int publishedSoFar) {
		if (config.isModeStrict()) {
			LOGGER.log(Level.SEVERE, unreachable, () -> "EAGER pre-fetch failed: the Atlas at " + config.getBaseUri()
					+ " is unreachable and mode.strict=true — aborting activation");
			throw unreachable;
		}
		LOGGER.log(Level.WARNING, unreachable,
				() -> "EAGER pre-fetch incomplete: the Atlas at " + config.getBaseUri()
						+ " is unreachable (mode.strict=false) — published " + publishedSoFar
						+ " package(s) so far; will retry on the next configuration update");
		return publishedSoFar;
	}
}
