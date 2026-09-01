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
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.model.atlas.rest.client.api.DriftListener;
import org.eclipse.fennec.model.atlas.rest.client.api.ModelAtlasClientException;
import org.eclipse.fennec.model.atlas.rest.client.api.ResolvedEPackage;

/**
 * P3-9 — the OSGi drift listener that keeps published services in step with the Atlas.
 * Subscribed to the Phase-2 drift mechanism (the scope watcher / conditional GETs):
 * <ul>
 * <li>{@link #onPackageChanged(String, EPackage)} — for an nsURI we currently publish,
 * re-resolve its authoritative origin and {@linkplain RemoteEPackagePublisher#republish
 * atomically swap} the trio to the new package (or unpublish it if the Atlas no longer
 * has it);</li>
 * <li>{@link #onPackageRemoved(String)} — revoke the trio.</li>
 * <li>{@link #onPackageAdded(String, EPackage)} — publish a package that appeared on
 * the server after start-up, if this client's mode wants it (issue #228).</li>
 * </ul>
 * The swap is per-nsURI atomic (the publisher's per-nsURI lock); consumers going through
 * the delegating registry see the old or the new package, never {@code null} or a mixture.
 * <p>
 * Change and removal act only on nsURIs we currently publish: a remote that is suppressed
 * by a local (P3-7, parked in the gate) has no service to swap; if its local later
 * disappears the gate re-resolves it fresh. An <em>addition</em> is by definition not
 * published yet, so it goes through {@code adopter} — the local-first gate — rather than
 * the republish path, which would bypass local-first suppression for a package the
 * framework may already provide. Which additions are wanted is the mode's business, so
 * {@code wantsAddition} is injected. OSGi-free for testability — the publish/unpublish, the
 * "is it ours" check and the resolver are all injected.
 */
final class DriftSubstitution implements DriftListener {

	private static final Logger LOGGER = Logger.getLogger(DriftSubstitution.class.getName());

	private final Predicate<String> isPublished;
	private final Supplier<Set<String>> publishedNsUris;
	private final Function<String, Optional<ResolvedEPackage>> resolver;
	private final PackagePublication republisher;
	private final Consumer<String> unpublisher;
	private final Predicate<String> wantsAddition;
	private final PackagePublication adopter;

	DriftSubstitution(Predicate<String> isPublished, Supplier<Set<String>> publishedNsUris,
			Function<String, Optional<ResolvedEPackage>> resolver, PackagePublication republisher,
			Consumer<String> unpublisher) {
		// No adoption: change/removal only, the pre-#228 behaviour.
		this(isPublished, publishedNsUris, resolver, republisher, unpublisher, nsUri -> false, republisher);
	}

	/**
	 * @param wantsAddition whether a newly discovered nsURI should be published by this
	 *                      client — EAGER takes every one from its scopes, HYBRID only
	 *                      those in {@code eager.nsuri.allow.list}, LAZY none
	 * @param adopter       publishes a newly discovered package; the local-first gate, so
	 *                      an addition that a local bundle already provides stays suppressed
	 */
	DriftSubstitution(Predicate<String> isPublished, Supplier<Set<String>> publishedNsUris,
			Function<String, Optional<ResolvedEPackage>> resolver, PackagePublication republisher,
			Consumer<String> unpublisher, Predicate<String> wantsAddition, PackagePublication adopter) {
		this.isPublished = Objects.requireNonNull(isPublished, "isPublished");
		this.publishedNsUris = Objects.requireNonNull(publishedNsUris, "publishedNsUris");
		this.resolver = Objects.requireNonNull(resolver, "resolver");
		this.republisher = Objects.requireNonNull(republisher, "republisher");
		this.unpublisher = Objects.requireNonNull(unpublisher, "unpublisher");
		this.wantsAddition = Objects.requireNonNull(wantsAddition, "wantsAddition");
		this.adopter = Objects.requireNonNull(adopter, "adopter");
	}

	@Override
	public Set<String> heldNsUris() {
		// A published service can outlive its provider-cache entry (TTL / size
		// eviction); reporting the published set keeps such packages drift-visible.
		return publishedNsUris.get();
	}

	@Override
	public void onPackageAdded(String nsUri, EPackage newPackage) {
		if (isPublished.test(nsUri) || !wantsAddition.test(nsUri)) {
			// Already ours (a race with the prefetch), or this mode does not want it.
			return;
		}
		Optional<ResolvedEPackage> resolved;
		try {
			// Re-resolve for the authoritative origin, so the new service carries the same
			// scope/stage/version provenance the EAGER pre-fetch would have stamped on it.
			resolved = resolver.apply(nsUri);
		} catch (ModelAtlasClientException unreachableOrServer) {
			LOGGER.log(Level.WARNING, unreachableOrServer,
					() -> "Drift: could not resolve newly discovered " + nsUri + "; it stays unpublished");
			return;
		}
		if (resolved.isEmpty()) {
			// Added-then-gone between the drift signal and our resolve; nothing to publish.
			return;
		}
		ResolvedEPackage remote = resolved.get();
		if (adopter.publish(remote.getEPackage(), remote.getScope(), remote.getStage(), remote.getVersion(),
				remote.getFingerprint())) {
			LOGGER.log(Level.INFO, () -> "Drift: published newly discovered EPackage " + nsUri + " from scope "
					+ remote.getScope() + " (stage " + remote.getStage() + ")");
		}
	}

	@Override
	public void onPackageChanged(String nsUri, EPackage newPackage) {
		if (!isPublished.test(nsUri)) {
			// We are not publishing this nsURI (suppressed by a local, or never ours) — nothing to swap.
			return;
		}
		Optional<ResolvedEPackage> resolved;
		try {
			// Re-resolve so the swapped service carries the new authoritative origin (version may have bumped).
			resolved = resolver.apply(nsUri);
		} catch (ModelAtlasClientException unreachableOrServer) {
			LOGGER.log(Level.WARNING, unreachableOrServer, () -> "Drift: could not re-resolve " + nsUri
					+ " after a change; leaving the current publication in place");
			return;
		}
		if (resolved.isPresent()) {
			ResolvedEPackage remote = resolved.get();
			republisher.publish(remote.getEPackage(), remote.getScope(), remote.getStage(), remote.getVersion(), remote.getFingerprint());
		} else {
			// Changed-then-gone between the drift signal and our re-resolve.
			unpublisher.accept(nsUri);
		}
	}

	@Override
	public void onPackageRemoved(String nsUri) {
		unpublisher.accept(nsUri);
	}
}
