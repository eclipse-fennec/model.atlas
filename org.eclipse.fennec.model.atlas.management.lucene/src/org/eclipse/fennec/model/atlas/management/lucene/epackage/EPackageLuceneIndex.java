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
    public record SearchHit(String objectId, String scope, String stage) {}
    
}
