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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.LongSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.model.atlas.rest.client.api.ModelAtlasClientException;
import org.eclipse.fennec.model.atlas.rest.client.api.RemoteEPackageProvider;
import org.eclipse.fennec.model.atlas.rest.client.api.ResolvedEPackage;

/**
 * P3-5 — the LAZY-mode delegating {@link EPackage.Registry}: an unknown nsURI is
 * fetched from the Atlas, published as OSGi services, and the call blocks until the
 * package is observable through the framework registry, with a timeout.
 * <p>
 * {@link #getEPackage(String)} resolves in order:
 * <ol>
 * <li>the <b>primary</b> (framework) registry — a locally shipped / already-published
 * package always wins, with no network call;</li>
 * <li>this registry's own entries (anything {@code put} directly into it);</li>
 * <li>on a miss, fetch via {@link RemoteEPackageProvider#resolve(String)} (which
 * reports the package's authoritative owning scope/stage/version),
 * {@linkplain PackagePublication#publish publish} the trio stamped with that exact
 * origin, then <b>block</b> until the package becomes visible in the primary registry.</li>
 * </ol>
 * The block is necessary because {@code emf.osgi}'s {@code DefaultEPackageRegistryComponent}
 * binds the freshly registered {@code EPackageConfigurator} <em>asynchronously</em> on
 * its own SCR thread; only after that bind does {@code primary.getEPackage(nsURI)}
 * return the package. We poll the primary registry (a cheap map lookup) on the calling
 * thread until it appears or {@code lazyResolveTimeoutMs} elapses. Polling on the
 * caller's thread — never on the SCR bind thread — is what keeps this deadlock-free:
 * the bind proceeds on its thread while we wait on ours, and the wait is bounded.
 * <p>
 * On timeout the call returns {@code null} and logs a warning; the package is already
 * published, so a subsequent call returns it once the bind has completed.
 * <p>
 * Concurrent calls for the same nsURI <b>deduplicate</b>: the first caller owns the
 * fetch+publish+wait and the others await its result on a shared future, so the server
 * is hit once.
 * <p>
 * It is a {@link ConcurrentHashMap} (so it satisfies the full {@code EPackage.Registry}
 * /{@code Map} contract out of the box, like the Phase-2 {@code AtlasDelegatingPackageRegistry}).
 * Not yet wired into framework {@code ResourceSet}s — that is P3-10's
 * {@code ResourceSetConfigurator}, which installs this registry as a ResourceSet's
 * package registry.
 */
final class LazyResolvingPackageRegistry extends ConcurrentHashMap<String, Object> implements EPackage.Registry {

	private static final long serialVersionUID = 1L;

	/** Default gap between framework-registry visibility polls. */
	static final long DEFAULT_POLL_INTERVAL_MS = 25L;

	private static final Logger LOGGER = Logger.getLogger(LazyResolvingPackageRegistry.class.getName());

	private final transient EPackage.Registry primary;
	private final transient RemoteEPackageProvider remote;
	private final transient PackagePublication publication;
	private final transient Function<String, EPackage> publishedLookup;
	private final long timeoutMs;
	private final long pollIntervalMs;
	private final transient LongSupplier clock;
	private final transient Sleeper sleeper;

	/** One in-flight resolution per nsURI, so concurrent callers share a single fetch+publish+wait. */
	private final transient ConcurrentHashMap<String, CompletableFuture<EPackage>> inFlight = new ConcurrentHashMap<>();

	/** Sleep seam (so tests need not actually sleep). */
	@FunctionalInterface
	interface Sleeper {
		void sleep(long millis) throws InterruptedException;
	}

	LazyResolvingPackageRegistry(EPackage.Registry primary, RemoteEPackageProvider remote,
			PackagePublication publication, Function<String, EPackage> publishedLookup, long timeoutMs) {
		this(primary, remote, publication, publishedLookup, timeoutMs, DEFAULT_POLL_INTERVAL_MS,
				System::currentTimeMillis, Thread::sleep);
	}

	LazyResolvingPackageRegistry(EPackage.Registry primary, RemoteEPackageProvider remote,
			PackagePublication publication, Function<String, EPackage> publishedLookup, long timeoutMs,
			long pollIntervalMs, LongSupplier clock, Sleeper sleeper) {
		this.primary = Objects.requireNonNull(primary, "primary");
		this.remote = Objects.requireNonNull(remote, "remote");
		this.publication = Objects.requireNonNull(publication, "publication");
		this.publishedLookup = Objects.requireNonNull(publishedLookup, "publishedLookup");
		this.timeoutMs = timeoutMs;
		this.pollIntervalMs = Math.max(1L, pollIntervalMs);
		this.clock = clock;
		this.sleeper = sleeper;
	}

	@Override
	public EPackage getEPackage(String nsURI) {
		// 1. Local / already-published precedence — no network call.
		EPackage local = primary.getEPackage(nsURI);
		if (local != null || nsURI == null) {
			return local;
		}
		// 2. P3-9: the package we currently publish for this nsURI, read atomically from the
		// publisher. Bridges emf.osgi's asynchronous (un)binding — during a drift swap, or
		// while a fresh publication is still being bound into the framework registry, this
		// returns the old or the new package, never null and never a half-state.
		EPackage current = publishedLookup.apply(nsURI);
		if (current != null) {
			return current;
		}
		// 3. Anything put directly into this registry.
		EPackage own = resolveOwn(nsURI);
		if (own != null) {
			return own;
		}
		// 4. Fetch from the Atlas, publish, and block until visible in the framework registry.
		return resolveRemote(nsURI);
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

	private EPackage resolveRemote(String nsURI) {
		CompletableFuture<EPackage> mine = new CompletableFuture<>();
		CompletableFuture<EPackage> existing = inFlight.putIfAbsent(nsURI, mine);
		if (existing != null) {
			// Another thread is already resolving this nsURI — wait on its result.
			return awaitExisting(nsURI, existing);
		}
		// We own the resolution.
		EPackage result = null;
		try {
			result = fetchPublishWait(nsURI);
		} catch (RuntimeException unexpected) {
			LOGGER.log(Level.WARNING, unexpected, () -> "LAZY resolve of " + nsURI + " failed unexpectedly");
		} finally {
			// Complete (never exceptionally — getEPackage must not throw) then drop the entry.
			mine.complete(result);
			inFlight.remove(nsURI, mine);
		}
		return result;
	}

	private EPackage awaitExisting(String nsURI, CompletableFuture<EPackage> existing) {
		try {
			return existing.get(timeoutMs, TimeUnit.MILLISECONDS);
		} catch (TimeoutException te) {
			LOGGER.warning(() -> "LAZY resolve: timed out after " + timeoutMs
					+ " ms waiting for a concurrent resolution of " + nsURI + "; returning null");
			return null;
		} catch (InterruptedException ie) {
			Thread.currentThread().interrupt();
			return null;
		} catch (ExecutionException ee) {
			// The owner completes normally (result or null), so this is not expected.
			LOGGER.log(Level.WARNING, ee, () -> "LAZY resolve: concurrent resolution of " + nsURI + " failed");
			return null;
		}
	}

	private EPackage fetchPublishWait(String nsURI) {
		Optional<ResolvedEPackage> resolved;
		try {
			resolved = remote.resolve(nsURI);
		} catch (ModelAtlasClientException transportOrServer) {
			LOGGER.log(Level.WARNING, transportOrServer,
					() -> "LAZY resolve: could not fetch " + nsURI + " from the Atlas");
			return null;
		}
		if (resolved.isEmpty()) {
			// Not visible from any allowed scope — a legitimate miss, no warning.
			return null;
		}
		ResolvedEPackage rp = resolved.get();
		// Stamp the authoritative origin the server reported (owning scope/stage/version),
		// not a guess — the whole point of resolve() over ensureAvailable().
		publication.publish(rp.getEPackage(), rp.getScope(), rp.getStage(), rp.getVersion(), rp.getFingerprint()); // idempotent per nsURI
		if (waitUntilVisible(nsURI)) {
			return primary.getEPackage(nsURI);
		}
		LOGGER.warning(() -> "LAZY resolve: " + nsURI + " was fetched and published but did not become "
				+ "observable in the framework EPackage.Registry within " + timeoutMs
				+ " ms; returning null (it will be available once binding completes)");
		return null;
	}

	/** Poll the primary registry until {@code nsURI} appears or the timeout elapses. */
	private boolean waitUntilVisible(String nsURI) {
		if (primary.getEPackage(nsURI) != null) {
			return true;
		}
		long deadline = clock.getAsLong() + timeoutMs;
		while (clock.getAsLong() < deadline) {
			try {
				sleeper.sleep(pollIntervalMs);
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
				return primary.getEPackage(nsURI) != null;
			}
			if (primary.getEPackage(nsURI) != null) {
				return true;
			}
		}
		return primary.getEPackage(nsURI) != null;
	}
}
