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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.model.atlas.rest.client.api.DriftListener;
import org.eclipse.fennec.model.atlas.rest.client.api.DriftReport;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.client.WebTarget;

/**
 * Scope-level drift watcher (P2-7).
 * <p>
 * Per configured scope it issues a {@code HEAD /scopes/{scope}} carrying the
 * last-seen aggregate {@code ETag} as {@code If-None-Match} (the P1-7 server
 * aggregate endpoint). On {@code 304} nothing happens. On {@code 200} the
 * {@code Atlas-Changed-NsUris} header lists the nsURIs that differ; for each one
 * the client currently holds in its cache, the entry is refreshed and the
 * registered {@link DriftListener}s are notified — {@code onPackageChanged} if it
 * is still present, {@code onPackageRemoved} if it is gone.
 * <p>
 * Runs on a daemon-threaded schedule every {@code drift.check.interval.ms}
 * ({@code 0} disables the schedule; {@link #check()} can still be invoked
 * manually). EObject-level drift ({@code Atlas-Changed-Objects}) is Phase 5.
 */
class DriftWatcher implements AutoCloseable {

	private static final Logger logger = Logger.getLogger(DriftWatcher.class.getName());

	private static final String SCOPES = "scopes";
	static final String ATLAS_CHANGED_NSURIS = "Atlas-Changed-NsUris";
	static final String ATLAS_CHANGED_OBJECTS = "Atlas-Changed-Objects";

	private final WebTarget baseTarget;
	private final Supplier<List<String>> scopesSupplier;
	private final Supplier<RemoteEPackageProviderImpl> providerSupplier;
	private final Function<String, RemoteReadOnlyScopeService> scopeServiceLookup;
	private final long intervalMs;

	private final List<DriftListener> listeners = new CopyOnWriteArrayList<>();
	private final Map<String, String> scopeEtags = new ConcurrentHashMap<>();
	private final ScheduledExecutorService scheduler;

	DriftWatcher(WebTarget baseTarget, Supplier<List<String>> scopesSupplier,
			Supplier<RemoteEPackageProviderImpl> providerSupplier,
			Function<String, RemoteReadOnlyScopeService> scopeServiceLookup, long intervalMs) {
		this.baseTarget = baseTarget;
		this.scopesSupplier = Objects.requireNonNull(scopesSupplier, "scopesSupplier");
		this.providerSupplier = Objects.requireNonNull(providerSupplier, "providerSupplier");
		this.scopeServiceLookup = Objects.requireNonNull(scopeServiceLookup, "scopeServiceLookup");
		this.intervalMs = intervalMs;
		this.scheduler = intervalMs > 0 ? Executors.newSingleThreadScheduledExecutor(daemonFactory()) : null;
	}

	/** Begin the scheduled checks, if an interval was configured. */
	void start() {
		if (scheduler != null) {
			scheduler.scheduleWithFixedDelay(this::safeCheck, intervalMs, intervalMs, TimeUnit.MILLISECONDS);
		}
	}

	/** Register a drift listener; close the returned handle to unsubscribe. */
	AutoCloseable addListener(DriftListener listener) {
		Objects.requireNonNull(listener, "listener");
		listeners.add(listener);
		return () -> listeners.remove(listener);
	}

	/**
	 * Probe every configured scope once and apply any reported changes to the
	 * cache and listeners. Returns the aggregate of what changed/was removed.
	 */
	synchronized DriftReport check() {
		Set<String> changed = new LinkedHashSet<>();
		Set<String> removed = new LinkedHashSet<>();
		RemoteEPackageProviderImpl provider = providerSupplier.get();
		for (String scope : scopesSupplier.get()) {
			checkScope(scope, provider, changed, removed);
		}
		return DriftReport.of(changed, removed);
	}

	private void checkScope(String scope, RemoteEPackageProviderImpl provider, Set<String> changed,
			Set<String> removed) {
		Response response = RestSupport.head(baseTarget.path(SCOPES).path(scope), scopeEtags.get(scope));
		try {
			if (RestSupport.isNotModified(response) || !RestSupport.isSuccess(response)) {
				return; // 304 (unchanged) or 404/5xx (skip this scope)
			}
			String previousEtag = scopeEtags.get(scope);
			String newEtag = response.getHeaderString(HttpHeaders.ETAG);
			if (newEtag != null) {
				scopeEtags.put(scope, newEtag);
			}
			if (previousEtag == null) {
				return; // first sight of this scope: establish the baseline, emit nothing
			}
			handleChangedNsUris(response, provider, changed, removed);
			handleChangedObjects(scope, response);
		} finally {
			response.close();
		}
	}

	/** EPackage drift: refresh and notify for each changed nsURI we currently hold. */
	private void handleChangedNsUris(Response response, RemoteEPackageProviderImpl provider, Set<String> changed,
			Set<String> removed) {
		String header = response.getHeaderString(ATLAS_CHANGED_NSURIS);
		if (header == null || header.isBlank()) {
			return;
		}
		Set<String> cached = provider.cachedNsUris();
		for (String raw : header.split(",")) {
			String nsUri = raw.trim();
			if (nsUri.isEmpty() || !cached.contains(nsUri)) {
				continue; // only act on entries we actually hold
			}
			Optional<EPackage> refreshed = provider.refresh(nsUri);
			if (refreshed.isPresent()) {
				changed.add(nsUri);
				fireChanged(nsUri, refreshed.get());
			} else {
				removed.add(nsUri);
				fireRemoved(nsUri);
			}
		}
	}

	/**
	 * EObject drift (P5-2): the {@code Atlas-Changed-Objects} header lists changed
	 * {@code registry/objectId} pairs. For each pair held in this scope's
	 * {@link RemoteReadOnlyScopeService} cache, refresh the entry and fire
	 * {@code onObjectChanged} (still present) or {@code onObjectRemoved} (gone). If the
	 * consumer never asked for this scope's read-only view there is nothing cached to
	 * evict, so the scope is skipped.
	 */
	private void handleChangedObjects(String scope, Response response) {
		String header = response.getHeaderString(ATLAS_CHANGED_OBJECTS);
		if (header == null || header.isBlank()) {
			return;
		}
		RemoteReadOnlyScopeService service = scopeServiceLookup.apply(scope);
		if (service == null) {
			return; // no read-only view for this scope → nothing cached to act on
		}
		Set<RemoteReadOnlyScopeService.ObjectKey> held = service.cachedObjects();
		for (String raw : header.split(",")) {
			String entry = raw.trim();
			int slash = entry.indexOf('/');
			if (slash <= 0 || slash == entry.length() - 1) {
				continue; // not a well-formed registry/objectId pair
			}
			String registry = entry.substring(0, slash);
			String objectId = entry.substring(slash + 1);
			if (!held.contains(new RemoteReadOnlyScopeService.ObjectKey(scope, registry, objectId))) {
				continue; // only act on entries we actually hold
			}
			if (service.refresh(registry, objectId).isPresent()) {
				fireObjectChanged(scope, registry, objectId);
			} else {
				fireObjectRemoved(scope, registry, objectId);
			}
		}
	}

	private void fireChanged(String nsUri, EPackage ePackage) {
		for (DriftListener listener : listeners) {
			try {
				listener.onPackageChanged(nsUri, ePackage);
			} catch (RuntimeException e) {
				logger.log(Level.WARNING, e, () -> "DriftListener onPackageChanged failed for " + nsUri);
			}
		}
	}

	private void fireRemoved(String nsUri) {
		for (DriftListener listener : listeners) {
			try {
				listener.onPackageRemoved(nsUri);
			} catch (RuntimeException e) {
				logger.log(Level.WARNING, e, () -> "DriftListener onPackageRemoved failed for " + nsUri);
			}
		}
	}

	private void fireObjectChanged(String scope, String registry, String objectId) {
		for (DriftListener listener : listeners) {
			try {
				listener.onObjectChanged(scope, registry, objectId);
			} catch (RuntimeException e) {
				logger.log(Level.WARNING, e,
						() -> "DriftListener onObjectChanged failed for " + scope + "/" + registry + "/" + objectId);
			}
		}
	}

	private void fireObjectRemoved(String scope, String registry, String objectId) {
		for (DriftListener listener : listeners) {
			try {
				listener.onObjectRemoved(scope, registry, objectId);
			} catch (RuntimeException e) {
				logger.log(Level.WARNING, e,
						() -> "DriftListener onObjectRemoved failed for " + scope + "/" + registry + "/" + objectId);
			}
		}
	}

	private void safeCheck() {
		try {
			check();
		} catch (RuntimeException e) {
			// A transient failure must not kill the schedule.
			logger.log(Level.FINE, e, () -> "Scheduled drift check failed");
		}
	}

	@Override
	public void close() {
		if (scheduler != null) {
			scheduler.shutdownNow();
		}
	}

	private static ThreadFactory daemonFactory() {
		return runnable -> {
			Thread thread = new Thread(runnable, "atlas-drift-watcher");
			thread.setDaemon(true);
			return thread;
		};
	}
}
