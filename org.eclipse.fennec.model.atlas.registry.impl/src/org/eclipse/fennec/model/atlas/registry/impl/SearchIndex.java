/*
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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.registry.impl;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.model.atlas.management.lucene.epackage.EPackageLuceneIndex;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.registry.RegistryAddress;

/**
 * The package search index as an optional collaborator. The index is a derived view, not a
 * system of record, and it only runs where it is configured; a registry write must not wait for
 * it.
 *
 * <p>
 * While no index is bound, index updates are held back — the latest per address — and applied
 * once an index appears. That covers the start, when the initial model loader may write packages
 * before the index is up. Where no index is configured at all, at most {@link #MAX_PENDING}
 * addresses are held; updates beyond that are dropped, so such a package is missing from search
 * until it is written again.
 * </p>
 */
final class SearchIndex {

	private static final Logger LOGGER = Logger.getLogger(SearchIndex.class.getName());

	/** How many addresses are held back at most while no index is bound. */
	static final int MAX_PENDING = 1000;

	private final AtomicReference<EPackageLuceneIndex> current = new AtomicReference<>();
	private final Map<RegistryAddress, Consumer<EPackageLuceneIndex>> pending = new LinkedHashMap<>();
	private boolean overflowLogged;

	/** Binds an index and applies the updates held back while there was none. */
	void bind(EPackageLuceneIndex index) {
		List<Consumer<EPackageLuceneIndex>> held;
		synchronized (this) {
			current.set(index);
			held = new ArrayList<>(pending.values());
			pending.clear();
			overflowLogged = false;
		}
		for (Consumer<EPackageLuceneIndex> update : held) {
			try {
				update.accept(index);
			} catch (RuntimeException e) {
				LOGGER.log(Level.WARNING, "Could not apply a held-back package index update", e);
			}
		}
	}

	void unbind(EPackageLuceneIndex index) {
		current.compareAndSet(index, null);
	}

	/** @return the index, if one is bound */
	Optional<EPackageLuceneIndex> get() {
		return Optional.ofNullable(current.get());
	}

	boolean isAvailable() {
		return current.get() != null;
	}

	void index(ObjectMetadata metadata, EPackage ePackage) {
		apply(RegistryAddress.of(metadata), index -> index.index(metadata, ePackage));
	}

	void remove(RegistryAddress address) {
		apply(address, index -> index.remove(address));
	}

	private void apply(RegistryAddress address, Consumer<EPackageLuceneIndex> update) {
		EPackageLuceneIndex index = current.get();
		if (index == null) {
			synchronized (this) {
				index = current.get();
				if (index == null) {
					hold(address, update);
					return;
				}
			}
		}
		update.accept(index);
	}

	/** Called with the monitor held. */
	private void hold(RegistryAddress address, Consumer<EPackageLuceneIndex> update) {
		if (pending.size() >= MAX_PENDING && !pending.containsKey(address)) {
			if (!overflowLogged) {
				overflowLogged = true;
				LOGGER.log(Level.WARNING,
						"No package search index is bound and {0} updates are already held back; further packages are not indexed until they are written again",
						MAX_PENDING);
			}
			return;
		}
		// the latest update per address wins, in the order the addresses were first touched
		pending.put(address, update);
	}
}
