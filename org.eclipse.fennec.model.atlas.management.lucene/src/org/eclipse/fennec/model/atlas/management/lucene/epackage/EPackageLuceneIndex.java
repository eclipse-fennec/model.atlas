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
package org.eclipse.fennec.model.atlas.management.lucene.epackage;

import java.util.List;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;

/**
 * Full-text index over the {@link EPackage}s held by a registry, used to answer
 * schema searches that the storage backends cannot express themselves.
 *
 * <p>
 * The index is a derived, rebuildable view: entries are added by
 * {@link #index(ObjectMetadata, EPackage)} as packages are stored or reloaded and
 * dropped by {@link #remove(String)}, keyed by the {@code objectId} of the
 * {@link ObjectMetadata}. A hit therefore carries the (objectId, scope, registry,
 * stage) coordinates needed to load the real object from the storage layer — never
 * the package itself. An index that has fallen behind its backend can be repopulated
 * by re-indexing every object; nothing here is a system of record.
 * </p>
 *
 * <p>
 * Implementations are OSGi services that own their index directory for the lifetime
 * of the component and are safe to call from multiple threads.
 * </p>
 *
 * @author ilenia
 * @since Apr 8, 2026
 */
public interface EPackageLuceneIndex {

	 /**
     * Index or re-index an EPackage.
     * Extracts objectId, scope, and stage from the ObjectMetadata;
     * extracts EPackage-specific fields (nsUri, classifiers, features, etc.) from the EPackage.
     */
    public void index(ObjectMetadata metadata, EPackage ePackage);

    /**
     * Remove an entry from the index.
     */
    public void remove(String objectId);

    /**
     * Search with filtering and pagination.
     * Returns matching objectIds and total hit count.
     */
    public SearchResult search(EPackageSearchQuery query);

    /**
     * Result record for paginated search.
     * Each SearchHit carries the objectId, scope, and stage from the Lucene stored fields,
     * providing enough context to retrieve the corresponding ObjectMetadata from the correct
     * scope/stage in the storage layer.
     */
    public record SearchResult(List<SearchHit> hits, long totalHits) {}

    /**
     * A single search hit with correlation fields.
     */
    public record SearchHit(String objectId, String scope, String registry, String stage) {}
    
}
