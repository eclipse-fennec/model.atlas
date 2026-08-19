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
package org.eclipse.fennec.model.atlas.eobject.provider;

import java.util.List;
import java.util.Set;

/**
 * Settings of an {@link AtlasObjectSync}.
 * <p>
 * Adapters map their own configuration onto this record; only the attribute-name
 * <em>convention</em> ({@code registries}, {@code object.ids}, {@code stage},
 * {@code key.feature}, {@code required.nsuris}, {@code refresh.interval.ms},
 * {@code retry.interval.ms}) is shared with the DS component of this bundle.
 *
 * @param providerName      the provider's name; registry entries are written under the
 *                          source tag {@code <providerName>:<atlas-registry>}; must not
 *                          be blank
 * @param registries        atlas registry names to sync from; must not be empty
 * @param objectIds         explicit object ids to load; empty loads every object the
 *                          registries list
 * @param stage             atlas stage to read from; empty reads the final stage
 * @param requiredNsUris    nsURIs whose generated {@code EPackage}s must be resolvable
 *                          before a sync pass runs - guards against fetched objects
 *                          materializing as dynamic EObjects while a model bundle is
 *                          not active yet; empty disables the gate
 * @param refreshIntervalMs interval for re-syncing from the atlas; {@code 0} syncs once
 * @param retryIntervalMs   back-off before retrying while the initial sync is
 *                          incomplete; {@code 0} disables retries
 * @param threadName        name of the private sync thread; {@code null} or blank
 *                          derives {@code atlas-eobject-provider-<providerName>}
 * @since 08/2026
 */
public record AtlasSyncSettings(String providerName, List<String> registries, List<String> objectIds, String stage,
		Set<String> requiredNsUris, long refreshIntervalMs, long retryIntervalMs, String threadName) {

	public AtlasSyncSettings {
		if (providerName == null || providerName.isBlank()) {
			throw new IllegalArgumentException("providerName must not be blank");
		}
		registries = registries == null ? List.of() : List.copyOf(registries);
		if (registries.isEmpty()) {
			throw new IllegalArgumentException("at least one atlas registry is required");
		}
		objectIds = objectIds == null ? List.of() : List.copyOf(objectIds);
		stage = stage == null ? "" : stage;
		requiredNsUris = requiredNsUris == null ? Set.of() : Set.copyOf(requiredNsUris);
		if (threadName == null || threadName.isBlank()) {
			threadName = "atlas-eobject-provider-" + providerName;
		}
	}
}
