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
package org.eclipse.fennec.model.atlas.rest.client.api;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.osgi.annotation.versioning.ProviderType;

/**
 * The outcome of a {@link ModelAtlasClient#checkForDrift() drift check}: the
 * nsURIs whose EPackage changed on the server and the nsURIs that were removed,
 * relative to what the client currently has cached.
 * <p>
 * EObject-level drift (changed/removed objects per scope+registry) is reported
 * through {@link DriftListener} in Phase 5; this value type stays
 * EPackage-centric for Phase 2.
 */
@ProviderType
public final class DriftReport {

	private static final DriftReport EMPTY = new DriftReport(List.of(), List.of());

	private final List<String> changedNsUris;
	private final List<String> removedNsUris;

	private DriftReport(List<String> changedNsUris, List<String> removedNsUris) {
		this.changedNsUris = changedNsUris;
		this.removedNsUris = removedNsUris;
	}

	/**
	 * A report describing the given changes. Duplicates are collapsed and
	 * iteration order is preserved; {@code null} arguments are treated as empty.
	 */
	public static DriftReport of(Set<String> changedNsUris, Set<String> removedNsUris) {
		List<String> changed = dedup(changedNsUris);
		List<String> removed = dedup(removedNsUris);
		if (changed.isEmpty() && removed.isEmpty()) {
			return EMPTY;
		}
		return new DriftReport(changed, removed);
	}

	/** A report with no drift. */
	public static DriftReport empty() {
		return EMPTY;
	}

	/** nsURIs whose EPackage content changed on the server since last seen. */
	public List<String> getChangedNsUris() {
		return changedNsUris;
	}

	/** nsURIs no longer present on the server. */
	public List<String> getRemovedNsUris() {
		return removedNsUris;
	}

	/** {@code true} if anything changed or was removed. */
	public boolean hasChanges() {
		return !changedNsUris.isEmpty() || !removedNsUris.isEmpty();
	}

	private static List<String> dedup(Set<String> values) {
		if (values == null || values.isEmpty()) {
			return List.of();
		}
		return Collections.unmodifiableList(List.copyOf(new LinkedHashSet<>(values)));
	}

	@Override
	public String toString() {
		return "DriftReport[changed=" + changedNsUris + ", removed=" + removedNsUris + "]";
	}
}
