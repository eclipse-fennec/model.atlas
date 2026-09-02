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
 * When the server cannot diff against the ETag we sent — it restarted, or the
 * snapshot aged out of its bounded cache — it answers {@code 200} with no diff
 * headers (and, since #238, {@code Atlas-Baseline-Unknown}). That is not "nothing
 * changed": the scope is then re-discovered in full, because silently adopting the
 * new ETag would lose every change in that window for good.
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
	static final String ATLAS_BASELINE_UNKNOWN = "Atlas-Baseline-Unknown";

	private final WebTarget baseTarget;
	private final Supplier<List<String>> scopesSupplier;
	private final Supplier<RemoteEPackageProviderImpl> providerSupplier;
	private final Function<String, RemoteReadableScopeService> scopeServiceLookup;
	private final long intervalMs;
	private final boolean discoverAdditions;

	private final List<DriftListener> listeners = new CopyOnWriteArrayList<>();
	private final Map<String, String> scopeEtags = new ConcurrentHashMap<>();
	private final ScheduledExecutorService scheduler;

	DriftWatcher(WebTarget baseTarget, Supplier<List<String>> scopesSupplier,
			Supplier<RemoteEPackageProviderImpl> providerSupplier,
			Function<String, RemoteReadableScopeService> scopeServiceLookup, long intervalMs) {
		this(baseTarget, scopesSupplier, providerSupplier, scopeServiceLookup, intervalMs, false);
	}

	/**
	 * @param discoverAdditions whether an nsURI the client holds nothing under is
	 *                          fetched and announced as an addition, rather than
	 *                          skipped
	 */
	DriftWatcher(WebTarget baseTarget, Supplier<List<String>> scopesSupplier,
			Supplier<RemoteEPackageProviderImpl> providerSupplier,
			Function<String, RemoteReadableScopeService> scopeServiceLookup, long intervalMs,
			boolean discoverAdditions) {
		this.discoverAdditions = discoverAdditions;
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
		Set<String> added = new LinkedHashSet<>();
		Set<String> changed = new LinkedHashSet<>();
		Set<String> removed = new LinkedHashSet<>();
		RemoteEPackageProviderImpl provider = providerSupplier.get();
		for (String scope : scopesSupplier.get()) {
			try {
				checkScope(scope, provider, added, changed, removed);
			} catch (RuntimeException e) {
				// One scope failing (unreachable, bad payload, listener trouble) must not
				// starve the remaining scopes of their drift events.
				logger.log(Level.WARNING, e, () -> "Drift check failed for scope " + scope);
			}
		}
		return DriftReport.of(added, changed, removed);
	}

	private void checkScope(String scope, RemoteEPackageProviderImpl provider, Set<String> added, Set<String> changed,
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
			if (isBaselineUnknown(response)) {
				resyncScope(scope, provider, added, changed, removed);
				return;
			}
			handleChangedNsUris(response, provider, added, changed, removed);
			handleChangedObjects(scope, response);
		} finally {
			response.close();
		}
	}

	/**
	 * EPackage drift: refresh and notify for each named nsURI.
	 * <p>
	 * An nsURI we hold is a change or a removal, as before. One we do <em>not</em>
	 * hold is a candidate <em>addition</em> — but only when discovery is on, and only
	 * if the server can actually resolve it: the server's diff reports every stage of
	 * a scope, so a package that exists only in a draft stage is named here long
	 * before a stage-free read can serve it. Such an nsURI is skipped silently. It
	 * must never be reported as removed — we never held it, so there is nothing to
	 * evict, and a listener acting on it would revoke a package it does not own.
	 */
	private void handleChangedNsUris(Response response, RemoteEPackageProviderImpl provider, Set<String> added,
			Set<String> changed, Set<String> removed) {
		String header = response.getHeaderString(ATLAS_CHANGED_NSURIS);
		if (header == null || header.isBlank()) {
			return;
		}
		Set<String> held = heldNsUris(provider);
		for (String raw : header.split(",")) {
			String nsUri = raw.trim();
			if (!nsUri.isEmpty()) {
				applyNsUri(nsUri, provider, held, added, changed, removed);
			}
		}
	}

	/** One nsURI: a change or removal if we hold it, a candidate addition if we do not. */
	private void applyNsUri(String nsUri, RemoteEPackageProviderImpl provider, Set<String> held, Set<String> added,
			Set<String> changed, Set<String> removed) {
		if (!held.contains(nsUri)) {
			if (discoverAdditions) {
				discover(nsUri, provider, added);
			}
			return;
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

	/**
	 * Whether the server could not tell us <em>what</em> changed.
	 * <p>
	 * {@code diffSince} answers {@code baselineKnown == false} when it cannot reconstruct
	 * the state our {@code ETag} stood for — after a server restart, or once that snapshot
	 * has aged out of its bounded per-scope cache — and the {@code 200} then carries no
	 * diff headers at all. Storing the new ETag and doing nothing loses every change in
	 * that window <em>permanently</em>: the next probe matches the ETag we just stored and
	 * 304s forever after. The server says so outright with {@code Atlas-Baseline-Unknown};
	 * for one that predates that header it is inferable, because a {@code 200} means the
	 * aggregate differs and a known baseline always names at least one entry in one of the
	 * two diff headers — so neither header present can only be an unknown baseline.
	 */
	private static boolean isBaselineUnknown(Response response) {
		String flag = response.getHeaderString(ATLAS_BASELINE_UNKNOWN);
		if (flag != null && !flag.isBlank()) {
			return Boolean.parseBoolean(flag.trim());
		}
		return isBlank(response.getHeaderString(ATLAS_CHANGED_NSURIS))
				&& isBlank(response.getHeaderString(ATLAS_CHANGED_OBJECTS));
	}

	private static boolean isBlank(String header) {
		return header == null || header.isBlank();
	}

	/**
	 * Full re-discovery of one scope, for when no diff is available: treat everything the
	 * scope currently lists, plus everything we hold, exactly as a diff entry. Held
	 * packages are revalidated (and reported removed if the atlas no longer serves them),
	 * listed ones we do not hold are candidate additions. The listing is the only extra
	 * server call, and it only happens on the rare unknown-baseline answer.
	 */
	private void resyncScope(String scope, RemoteEPackageProviderImpl provider, Set<String> added,
			Set<String> changed, Set<String> removed) {
		logger.log(Level.INFO, () -> "Drift: the server could not diff scope " + scope
				+ " against our last-seen aggregate; re-discovering it in full");
		Set<String> held = heldNsUris(provider);
		Set<String> candidates = new LinkedHashSet<>(held);
		try {
			candidates.addAll(provider.listNsUris(scope));
		} catch (RuntimeException e) {
			// Without the listing we cannot find additions — but what we hold can still be
			// revalidated, which is the half that protects against a stale local package.
			logger.log(Level.WARNING, e,
					() -> "Drift: could not list the packages of scope " + scope + " for a full re-discovery");
		}
		for (String nsUri : candidates) {
			try {
				resyncNsUri(nsUri, provider, held, added, changed, removed);
			} catch (RuntimeException e) {
				logger.log(Level.WARNING, e, () -> "Drift: re-discovery of " + nsUri + " failed");
			}
		}
		resyncObjects(scope);
	}

	/**
	 * One nsURI during a full re-discovery. Same shape as {@link #applyNsUri}, with one
	 * difference that matters: with no server diff to trust, a held package is revalidated
	 * <em>conditionally</em> and only announced when the payload really did change. The
	 * diff-driven path announces whatever the server named — an entry can change in
	 * provenance alone (a promotion whose content is byte-identical) and the publication
	 * has to follow that. Here a {@code 304} means we already hold what the atlas serves,
	 * and swapping every published service for an identical one — right after the restart
	 * that lost the baseline — would be a re-registration storm for nothing.
	 */
	private void resyncNsUri(String nsUri, RemoteEPackageProviderImpl provider, Set<String> held, Set<String> added,
			Set<String> changed, Set<String> removed) {
		if (!held.contains(nsUri)) {
			if (discoverAdditions) {
				discover(nsUri, provider, added);
			}
			return;
		}
		RemoteEPackageProviderImpl.Refreshed refreshed = provider.revalidate(nsUri);
		switch (refreshed.outcome()) {
		case CHANGED -> {
			changed.add(nsUri);
			fireChanged(nsUri, refreshed.ePackage());
		}
		case UNCHANGED -> {
			// We hold the current payload; there is nothing to tell a listener.
		}
		case REMOVED -> {
			removed.add(nsUri);
			fireRemoved(nsUri);
		}
		}
	}

	/** The object half of {@link #resyncScope}: revalidate every view we hold of this scope. */
	private void resyncObjects(String scope) {
		RemoteReadableScopeService service = scopeServiceLookup.apply(scope);
		if (service == null) {
			return; // no read-only view for this scope → nothing cached to act on
		}
		Set<RemoteReadableScopeService.ObjectKey> held = service.cachedObjects();
		Set<String> objects = new LinkedHashSet<>();
		for (RemoteReadableScopeService.ObjectKey key : held) {
			if (scope.equals(key.scope())) {
				objects.add(key.registry() + "/" + key.objectId());
			}
		}
		for (String entry : objects) {
			int slash = entry.indexOf('/');
			revalidateObject(scope, service, held, entry.substring(0, slash), entry.substring(slash + 1));
		}
	}

	/**
	 * Try to fetch an nsURI we hold nothing under. Present ⇒ a genuine addition;
	 * absent ⇒ not (yet) resolvable stage-free, which is the normal state of a
	 * draft-only publish and is not an event of any kind.
	 */
	private void discover(String nsUri, RemoteEPackageProviderImpl provider, Set<String> added) {
		Optional<EPackage> fetched;
		try {
			fetched = provider.refresh(nsUri);
		} catch (RuntimeException e) {
			// Discovery is best-effort: a package we never held failing to fetch must not
			// cost the remaining nsURIs in this header their change/removal events.
			logger.log(Level.WARNING, e, () -> "Drift: could not fetch newly reported nsURI " + nsUri);
			return;
		}
		if (fetched.isEmpty()) {
			logger.log(Level.FINE,
					() -> "Drift: " + nsUri + " was reported changed but is not resolvable at a final stage yet");
			return;
		}
		added.add(nsUri);
		fireAdded(nsUri, fetched.get());
	}

	/**
	 * Every nsURI the client holds anywhere: the provider's cache plus each
	 * listener's {@link DriftListener#heldNsUris()}. Stage-explicit fetches
	 * ({@code getEPackageAtStage}) bypass the provider cache by design, so gating on
	 * the cache alone would leave those packages drift-blind — never evicted, never
	 * unpublished. A misbehaving listener must not kill the check, hence the guard.
	 * <p>
	 * Note that the eventual {@code refresh()} goes through the stage-free
	 * final-stage path: a package existing only at a non-final stage reports as
	 * removed here, the listener evicts it, and its next stage-explicit look-up
	 * re-fetches it — self-healing, at the price of a spurious removal event.
	 */
	private Set<String> heldNsUris(RemoteEPackageProviderImpl provider) {
		Set<String> held = new LinkedHashSet<>(provider.cachedNsUris());
		for (DriftListener listener : listeners) {
			try {
				held.addAll(listener.heldNsUris());
			} catch (RuntimeException e) {
				logger.log(Level.WARNING, e, () -> "DriftListener heldNsUris failed");
			}
		}
		return held;
	}

	/**
	 * EObject drift (P5-2): the {@code Atlas-Changed-Objects} header lists changed
	 * {@code registry/objectId} pairs. For each pair held in this scope's
	 * {@link RemoteReadableScopeService} cache, refresh the entry and fire
	 * {@code onObjectChanged} (still present) or {@code onObjectRemoved} (gone). If the
	 * consumer never asked for this scope's read-only view there is nothing cached to
	 * evict, so the scope is skipped.
	 */
	private void handleChangedObjects(String scope, Response response) {
		String header = response.getHeaderString(ATLAS_CHANGED_OBJECTS);
		if (header == null || header.isBlank()) {
			return;
		}
		RemoteReadableScopeService service = scopeServiceLookup.apply(scope);
		if (service == null) {
			return; // no read-only view for this scope → nothing cached to act on
		}
		Set<RemoteReadableScopeService.ObjectKey> held = service.cachedObjects();
		for (String raw : header.split(",")) {
			String entry = raw.trim();
			int slash = entry.indexOf('/');
			if (slash <= 0 || slash == entry.length() - 1) {
				continue; // not a well-formed registry/objectId pair
			}
			revalidateObject(scope, service, held, entry.substring(0, slash), entry.substring(slash + 1));
		}
	}

	/**
	 * Revalidate one object of one scope and fire at most one event for it.
	 * <p>
	 * Inheritance means a view's requested stage need not be its content's origin stage (a
	 * draft read can be served by the parent's final stage), so we can't narrow by stage:
	 * revalidate EVERY held view of this object, each at its own stage, and let the
	 * per-view conditional GET decide what actually changed (P6-5).
	 */
	private void revalidateObject(String scope, RemoteReadableScopeService service,
			Set<RemoteReadableScopeService.ObjectKey> held, String registry, String objectId) {
		boolean anyHeld = false;
		boolean anyChanged = false;
		boolean allRemoved = true;
		for (RemoteReadableScopeService.ObjectKey k : held) {
			if (!scope.equals(k.scope()) || !registry.equals(k.registry()) || !objectId.equals(k.objectId())) {
				continue; // a different object/scope
			}
			anyHeld = true;
			switch (service.refresh(k.registry(), k.stage(), k.objectId())) {
			case CHANGED -> {
				anyChanged = true;
				allRemoved = false;
			}
			case UNCHANGED -> allRemoved = false;
			case REMOVED -> {
				// this view is gone; another view of the same object may still be present
			}
			}
		}
		if (!anyHeld) {
			return; // we hold no view of this object
		}
		if (anyChanged) {
			fireObjectChanged(scope, registry, objectId);
		} else if (allRemoved) {
			fireObjectRemoved(scope, registry, objectId);
		} // else: only unchanged sibling views → no event
	}

	private void fireAdded(String nsUri, EPackage ePackage) {
		for (DriftListener listener : listeners) {
			try {
				listener.onPackageAdded(nsUri, ePackage);
			} catch (RuntimeException e) {
				logger.log(Level.WARNING, e, () -> "DriftListener onPackageAdded failed for " + nsUri);
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
			// A transient failure must not kill the schedule — and it must not die
			// silently either, or drift protection is off without anyone noticing.
			logger.log(Level.WARNING, e, () -> "Scheduled drift check failed");
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
