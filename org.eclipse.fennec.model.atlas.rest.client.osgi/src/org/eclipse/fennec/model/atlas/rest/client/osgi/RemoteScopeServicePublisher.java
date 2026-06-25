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

import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.scope.api.AtlasProperties;
import org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * P5-4 — publishes one remote {@link ReadableScopeService}{@code <EObject>} to the OSGi
 * service registry per Atlas <b>scope</b>, the EObject-side counterpart of the EPackage
 * trio published by {@link RemoteEPackagePublisher} (P3-3).
 * <p>
 * Each publication carries the property set a consumer filters on, mirroring the
 * in-process server-side {@code ScopeServiceImpl} publication so the same lookup resolves
 * against either side (the Phase-4/5 symmetry payoff):
 * <ul>
 * <li>{@link AtlasProperties#ATLAS_SCOPE} — the scope name; this is the collector key
 * ({@code ReadableScopeCollector}) and what consumers target,
 * e.g. {@code @Reference(target="(atlas.scope=jena)")};</li>
 * <li>{@link AtlasProperties#ATLAS_REMOTE}{@code =true} — marks it as fetched from a
 * remote Atlas (so it is distinguishable from an in-process publication of the same
 * scope);</li>
 * <li>{@link AtlasProperties#ATLAS_BASE_URI} — provenance: the originating Atlas base URI
 * (matches the {@code ResourceSetConfigurator} publication, P3-10);</li>
 * <li>{@link AtlasProperties#ATLAS_STAGE} — P6-7: the client's configured stage, when
 * non-null.  This is a <em>disambiguation label</em>, not a behavioral knob: the service
 * always reads the scope's final stage (P6 D1), but the stamp lets two front-ends (e.g.
 * one final, one snapshot) that publish for the same scope be told apart via
 * {@code (&(atlas.scope=jena)(atlas.stage=snapshot))}.  Omitted when null (stage-free
 * client, or a client whose primary stage equals the Atlas final-stage name).</li>
 * </ul>
 * {@code atlas.view} is deliberately <em>not</em> stamped: it is advisory only (no consumer
 * filters on it; reads always target the final stage, resolved server-side), and the
 * in-process {@code ScopeServiceImpl} does not stamp it on the scope service either.
 * <p>
 * Registrations are tracked per scope so they can be revoked individually or all at once on
 * client shutdown. Publication is idempotent and atomic per scope; {@link #republish} swaps
 * the registration for a scope under the same per-scope lock (P3-9 idea, scope-scoped).
 * <p>
 * Note on drift: the published service is a stable façade over a cache, and EObject content
 * drift is absorbed <em>in place</em> by that cache (the scope-level drift watcher refreshes
 * it — P5-2), so content changes need no re-registration. {@link #republish} is for swapping
 * the backing service of a scope (e.g. a scope-set reconfiguration), not for content drift.
 */
final class RemoteScopeServicePublisher {

	private static final Logger LOGGER = Logger.getLogger(RemoteScopeServicePublisher.class.getName());

	private final BundleContext bundleContext;
	private final String baseUri;
	/** P6-7: client-level stage label; null → stage-free, stamp omitted. */
	private final String stage;
	private final Map<String, ServiceRegistration<?>> published = new ConcurrentHashMap<>();
	/** Serialises publish/republish/unpublish of the same scope (parallel for distinct scopes). */
	private final NsUriLocks locks = new NsUriLocks();

	RemoteScopeServicePublisher(BundleContext bundleContext, String baseUri) {
		this(bundleContext, baseUri, null);
	}

	/**
	 * @param stage the client's primary configured stage to stamp as {@code atlas.stage} on every
	 *              service publication, or {@code null} / blank to omit the property (stage-free)
	 */
	RemoteScopeServicePublisher(BundleContext bundleContext, String baseUri, String stage) {
		this.bundleContext = Objects.requireNonNull(bundleContext, "bundleContext");
		this.baseUri = Objects.requireNonNull(baseUri, "baseUri");
		this.stage = (stage == null || stage.isBlank()) ? null : stage;
	}

	/**
	 * Publish {@code scopeService} for {@code scopeName}. No-op (returns {@code false}) when
	 * the scope name is blank or a service is already published for it (idempotent).
	 *
	 * @return {@code true} if it was newly published
	 */
	boolean publish(String scopeName, ReadableScopeService<EObject> scopeService) {
		Objects.requireNonNull(scopeService, "scopeService");
		if (scopeName == null || scopeName.isBlank()) {
			LOGGER.warning("Cannot publish a ReadableScopeService with a null/blank scope name");
			return false;
		}
		boolean[] created = { false };
		locks.run(scopeName, () -> {
			if (published.containsKey(scopeName)) {
				return; // already published — idempotent
			}
			published.put(scopeName, register(scopeName, scopeService));
			created[0] = true;
		});
		if (created[0]) {
			LOGGER.log(Level.INFO, () -> "Published remote ReadableScopeService for scope " + scopeName);
		}
		return created[0];
	}

	/**
	 * Atomically swap the published service for {@code scopeName}. Under the per-scope lock:
	 * register the new service, flip the tracked registration, then revoke the old one. If
	 * nothing was published yet it behaves like {@link #publish}.
	 *
	 * @return {@code true} if it replaced an existing publication
	 */
	boolean republish(String scopeName, ReadableScopeService<EObject> scopeService) {
		Objects.requireNonNull(scopeService, "scopeService");
		if (scopeName == null || scopeName.isBlank()) {
			LOGGER.warning("Cannot republish a ReadableScopeService with a null/blank scope name");
			return false;
		}
		boolean[] replaced = { false };
		locks.run(scopeName, () -> {
			ServiceRegistration<?> old = published.get(scopeName);
			ServiceRegistration<?> fresh = register(scopeName, scopeService);
			published.put(scopeName, fresh);
			if (old != null) {
				unregisterQuietly(old);
				replaced[0] = true;
			}
		});
		LOGGER.log(Level.INFO, () -> (replaced[0] ? "Re-published" : "Published")
				+ " remote ReadableScopeService for scope " + scopeName);
		return replaced[0];
	}

	/** Revoke the service for {@code scopeName}; {@code false} if none was published. */
	boolean unpublish(String scopeName) {
		if (scopeName == null) {
			return false;
		}
		boolean[] removed = { false };
		locks.run(scopeName, () -> {
			ServiceRegistration<?> registration = published.remove(scopeName);
			if (registration != null) {
				unregisterQuietly(registration);
				removed[0] = true;
			}
		});
		if (removed[0]) {
			LOGGER.log(Level.INFO, () -> "Unpublished remote ReadableScopeService for scope " + scopeName);
		}
		return removed[0];
	}

	boolean isPublished(String scopeName) {
		return scopeName != null && published.containsKey(scopeName);
	}

	Set<String> publishedScopes() {
		return Set.copyOf(published.keySet());
	}

	/** Revoke every publication; called on client shutdown. */
	void unpublishAll() {
		published.values().forEach(RemoteScopeServicePublisher::unregisterQuietly);
		published.clear();
	}

	private ServiceRegistration<?> register(String scopeName, ReadableScopeService<EObject> scopeService) {
		Hashtable<String, Object> properties = new Hashtable<>();
		properties.put(AtlasProperties.ATLAS_SCOPE, scopeName);
		properties.put(AtlasProperties.ATLAS_REMOTE, Boolean.TRUE);
		properties.put(AtlasProperties.ATLAS_BASE_URI, baseUri);
		// P6-7: stamp atlas.stage when the client is configured for a specific stage so that
		// two front-ends for the same scope (e.g. one "released", one "snapshot") can be
		// disambiguated by consumers via (&(atlas.scope=jena)(atlas.stage=snapshot)).
		if (stage != null) {
			properties.put(AtlasProperties.ATLAS_STAGE, stage);
		}
		// Registered under the raw interface (type erasure): a consumer @Reference of
		// ReadableScopeService<EObject> binds it, exactly as the server-side publication does.
		return bundleContext.registerService(ReadableScopeService.class, scopeService, properties);
	}

	private static void unregisterQuietly(ServiceRegistration<?> registration) {
		if (registration == null) {
			return;
		}
		try {
			registration.unregister();
		} catch (IllegalStateException alreadyGone) {
			// Already unregistered (e.g. during framework shutdown) — nothing to do.
			LOGGER.log(Level.FINE, "Service already unregistered", alreadyGone);
		}
	}
}
