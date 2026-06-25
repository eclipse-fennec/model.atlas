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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
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
 * </ul>
 * The swap is per-nsURI atomic (the publisher's per-nsURI lock); consumers going through
 * the delegating registry see the old or the new package, never {@code null} or a mixture.
 * <p>
 * Only nsURIs we currently publish are acted on: a remote that is suppressed by a local
 * (P3-7, parked in the gate) has no service to swap; if its local later disappears the gate
 * re-resolves it fresh. OSGi-free for testability — the publish/unpublish, the
 * "is it ours" check and the resolver are all injected.
 */
final class DriftSubstitution implements DriftListener {

	private static final Logger LOGGER = Logger.getLogger(DriftSubstitution.class.getName());

	private final Predicate<String> isPublished;
	private final Function<String, Optional<ResolvedEPackage>> resolver;
	private final PackagePublication republisher;
	private final Consumer<String> unpublisher;

	DriftSubstitution(Predicate<String> isPublished, Function<String, Optional<ResolvedEPackage>> resolver,
			PackagePublication republisher, Consumer<String> unpublisher) {
		this.isPublished = Objects.requireNonNull(isPublished, "isPublished");
		this.resolver = Objects.requireNonNull(resolver, "resolver");
		this.republisher = Objects.requireNonNull(republisher, "republisher");
		this.unpublisher = Objects.requireNonNull(unpublisher, "unpublisher");
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
			republisher.publish(remote.getEPackage(), remote.getScope(), remote.getStage(), remote.getVersion());
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
