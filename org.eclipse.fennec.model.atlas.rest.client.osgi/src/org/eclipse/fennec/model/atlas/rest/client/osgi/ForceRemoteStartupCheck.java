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

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.fennec.model.atlas.rest.client.api.ModelAtlasClientException;
import org.eclipse.fennec.model.atlas.rest.client.api.ResolvedEPackage;
import org.eclipse.fennec.model.atlas.rest.client.osgi.LocalServiceWatcher.LocalModel;
import org.osgi.framework.Version;

/**
 * P3-8 — the {@code force.remote=true} startup version check. For every locally
 * registered EPackage, it asks the Atlas (via {@code resolve()}) for the same nsURI
 * and, when the Atlas copy is <em>newer</em>, publishes the remote so it supersedes
 * the local one. (The remote is published with a high {@code service.ranking} by the
 * publisher, so direct service-lookup consumers prefer it.)
 * <p>
 * OSGi-free for testability: it is driven by a supplier of {@link LocalModel}s, a
 * resolver function and a {@link PackagePublication} (the local-first gate, which —
 * under {@code force.remote} — always publishes).
 * <p>
 * Best-effort: a transport failure resolving one nsURI is logged and skipped (the
 * local stays in place); it never fails activation.
 * <p>
 * <strong>Caveat (carried from the design):</strong> {@code emf.osgi}'s
 * {@code DefaultEPackageRegistryComponent} populates {@code EPackage.Registry} in
 * bind order, not by {@code service.ranking}. So a forced remote reliably wins for
 * consumers doing a direct service lookup, but registry-level
 * ({@code EPackage.Registry.getEPackage}) precedence remains bind-order-dependent
 * until the ranking-aware aggregator (tracked as a follow-up against {@code emf.osgi})
 * lands.
 */
final class ForceRemoteStartupCheck {

	private static final Logger LOGGER = Logger.getLogger(ForceRemoteStartupCheck.class.getName());

	private final Supplier<Collection<LocalModel>> localModels;
	private final Function<String, Optional<ResolvedEPackage>> resolver;
	private final PackagePublication publisher;

	ForceRemoteStartupCheck(Supplier<Collection<LocalModel>> localModels,
			Function<String, Optional<ResolvedEPackage>> resolver, PackagePublication publisher) {
		this.localModels = Objects.requireNonNull(localModels, "localModels");
		this.resolver = Objects.requireNonNull(resolver, "resolver");
		this.publisher = Objects.requireNonNull(publisher, "publisher");
	}

	/**
	 * @return the number of local packages superseded by a newer Atlas copy
	 */
	int run() {
		int superseded = 0;
		for (LocalModel local : localModels.get()) {
			Optional<ResolvedEPackage> resolved;
			try {
				resolved = resolver.apply(local.nsUri());
			} catch (ModelAtlasClientException unreachableOrServer) {
				LOGGER.log(Level.WARNING, unreachableOrServer, () -> "force.remote startup check: could not resolve "
						+ local.nsUri() + " against the Atlas; leaving the local registration in place");
				continue;
			}
			if (resolved.isEmpty()) {
				continue; // the Atlas does not have it — nothing to supersede
			}
			ResolvedEPackage remote = resolved.get();
			if (isRemoteNewer(local.version(), remote.getVersion())
					&& publisher.publish(remote.getEPackage(), remote.getScope(), remote.getStage(),
							remote.getVersion(), remote.getFingerprint())) {
				superseded++;
				LOGGER.log(Level.INFO,
						() -> "force.remote: Atlas copy of " + local.nsUri() + " (version " + remote.getVersion()
								+ ") is newer than the local (version " + local.version() + "); superseding it");
			}
		}
		int total = superseded;
		LOGGER.log(Level.INFO, () -> "force.remote startup check superseded " + total + " local package(s)");
		return superseded;
	}

	/**
	 * Whether the Atlas version should supersede the local one. Rule:
	 * <ul>
	 * <li>no remote version → {@code false} (cannot tell; leave the local);</li>
	 * <li>no local version → {@code true} (local unknown; under {@code force.remote} prefer the Atlas);</li>
	 * <li>equal strings → {@code false};</li>
	 * <li>both parse as OSGi {@link Version} → {@code true} iff remote &gt; local;</li>
	 * <li>otherwise (unparseable, but different) → {@code true} (prefer the differing remote).</li>
	 * </ul>
	 */
	static boolean isRemoteNewer(String localVersion, String remoteVersion) {
		if (remoteVersion == null || remoteVersion.isBlank()) {
			return false;
		}
		if (localVersion == null || localVersion.isBlank()) {
			return true;
		}
		if (localVersion.equals(remoteVersion)) {
			return false;
		}
		try {
			return Version.parseVersion(remoteVersion).compareTo(Version.parseVersion(localVersion)) > 0;
		} catch (IllegalArgumentException unparseable) {
			// Can't order them; under force.remote, a differing remote wins.
			return true;
		}
	}
}
