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
 * nsURIs whose EPackage was added on the server, those whose EPackage changed,
 * and those that were removed, relative to what the client currently has cached.
 * <p>
 * "Added" are packages the client held nothing under and that the server has now
 * made resolvable — a package published and promoted into a scope's final stage
 * after the client started. They are reported only when the client is configured
 * to discover them (EAGER/HYBRID); a LAZY client fetches on demand and needs no
 * announcement.
 * <p>
 * EObject-level drift (changed/removed objects per scope+registry) is reported
 * through {@link DriftListener} in Phase 5; this value type stays
 * EPackage-centric for Phase 2.
 */
@ProviderType
public final class DriftReport {

	private static final DriftReport EMPTY = new DriftReport(List.of(), List.of(), List.of());

	private final List<String> addedNsUris;
	private final List<String> changedNsUris;
	private final List<String> removedNsUris;

	private DriftReport(List<String> addedNsUris, List<String> changedNsUris, List<String> removedNsUris) {
		this.addedNsUris = addedNsUris;
		this.changedNsUris = changedNsUris;
		this.removedNsUris = removedNsUris;
	}

	/**
	 * A report describing the given changes. Duplicates are collapsed and
	 * iteration order is preserved; {@code null} arguments are treated as empty.
	 */
	public static DriftReport of(Set<String> changedNsUris, Set<String> removedNsUris) {
		return of(null, changedNsUris, removedNsUris);
	}

	/**
	 * A report describing the given changes, including newly discovered packages.
	 * Duplicates are collapsed and iteration order is preserved; {@code null}
	 * arguments are treated as empty.
	 */
	public static DriftReport of(Set<String> addedNsUris, Set<String> changedNsUris, Set<String> removedNsUris) {
		List<String> added = dedup(addedNsUris);
		List<String> changed = dedup(changedNsUris);
		List<String> removed = dedup(removedNsUris);
		if (added.isEmpty() && changed.isEmpty() && removed.isEmpty()) {
			return EMPTY;
		}
		return new DriftReport(added, changed, removed);
	}

	/** A report with no drift. */
	public static DriftReport empty() {
		return EMPTY;
	}

	/**
	 * nsURIs the client held nothing under and that the server has now made
	 * resolvable. Empty unless the client discovers additions (EAGER/HYBRID).
	 */
	public List<String> getAddedNsUris() {
		return addedNsUris;
	}

	/** nsURIs whose EPackage content changed on the server since last seen. */
	public List<String> getChangedNsUris() {
		return changedNsUris;
	}

	/** nsURIs no longer present on the server. */
	public List<String> getRemovedNsUris() {
		return removedNsUris;
	}

	/** {@code true} if anything was added, changed or removed. */
	public boolean hasChanges() {
		return !addedNsUris.isEmpty() || !changedNsUris.isEmpty() || !removedNsUris.isEmpty();
	}

	private static List<String> dedup(Set<String> values) {
		if (values == null || values.isEmpty()) {
			return List.of();
		}
		return Collections.unmodifiableList(List.copyOf(new LinkedHashSet<>(values)));
	}

	@Override
	public String toString() {
		return "DriftReport[added=" + addedNsUris + ", changed=" + changedNsUris + ", removed=" + removedNsUris + "]";
	}
}
