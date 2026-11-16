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

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.model.atlas.management.lucene.epackage.EPackageLuceneIndex;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.registry.RegistryAddress;

/**
 * The package search index as an optional collaborator. The index is a derived view, not a
 * system of record, and it only runs where it is configured; a registry write must not wait for
 * it. While it is absent, index updates are skipped: a package written meanwhile is missing from
 * search until it is written again.
 */
final class SearchIndex {

	private final AtomicReference<EPackageLuceneIndex> current = new AtomicReference<>();

	void bind(EPackageLuceneIndex index) {
		current.set(index);
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
		EPackageLuceneIndex index = current.get();
		if (index != null) {
			index.index(metadata, ePackage);
		}
	}

	void remove(RegistryAddress address) {
		EPackageLuceneIndex index = current.get();
		if (index != null) {
			index.remove(address);
		}
	}
}
