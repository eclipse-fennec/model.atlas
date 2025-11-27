/**
 * Copyright (c) 2012 - 2025 Data In Motion and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *      Mark Hoffmann - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.mgmt.registry;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;

/**
 * Abstract base class for object metadata registry operations.
 * 
 * <p>This class provides the common interface for different registry implementations
 * that can index and search ObjectMetadata. Implementations can use in-memory caches,
 * Lucene indexes, or other storage mechanisms for fast metadata lookups.</p>
 * 
 * <h3>Separation of Concerns</h3>
 * <p>Registry helpers are separate from storage helpers:</p>
 * <ul>
 * <li><strong>RegistryHelper</strong> - Handles metadata indexing and search operations</li>
 * <li><strong>StorageHelper</strong> - Handles actual object persistence (files, cloud, etc.)</li>
 * </ul>
 * 
 * <h3>Package Organization</h3>
 * <ul>
 * <li><strong>org.gecko.mac.mgmt.registry</strong> - Abstract registry interfaces and base classes</li>
 * <li><strong>org.gecko.mac.management.file.registry</strong> - File-based registry implementations (Lucene)</li>
 * <li><strong>org.gecko.mac.mgmt.storage</strong> - Storage helper interfaces and base classes</li>
 * <li><strong>org.gecko.mac.management.file</strong> - File-based storage implementations</li>
 * </ul>
 * 
 * <h3>Implementations</h3>
 * <ul>
 * <li><strong>LuceneRegistryHelper</strong> - Fast Lucene-based indexing with complex queries</li>
 * <li><strong>BasicRegistryHelper</strong> - Simple in-memory indexing for smaller datasets</li>
 * </ul>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 */
public abstract class AbstractRegistryHelper implements AutoCloseable {
    
    private static final Logger LOGGER = Logger.getLogger(AbstractRegistryHelper.class.getName());
    
    /**
     * Initialize the registry helper.
     * Called during service activation.
     * 
     * @throws IOException if initialization fails
     */
    public abstract void initialize() throws IOException;
    
    /**
     * Update the metadata index for the given object.
     * 
     * @param objectId the object identifier
     * @param metadata the object metadata to index
     * @throws IOException if update fails
     */
    public abstract void updateIndex(String objectId, ObjectMetadata metadata) throws IOException;
    
    /**
     * Remove the object from the metadata index.
     * 
     * @param objectId the object identifier to remove
     * @throws IOException if removal fails
     */
    public abstract void removeFromIndex(String objectId) throws IOException;
    
    /**
     * Search for object IDs matching the given query.
     * 
     * @param query the search query (implementation-specific format)
     * @param maxResults maximum number of results to return
     * @return list of matching object IDs
     * @throws IOException if search fails
     */
    public abstract List<String> searchObjectIds(String query, int maxResults) throws IOException;
    
    /**
     * Find objects by status.
     * 
     * @param status the object status to search for
     * @return list of matching object IDs
     * @throws IOException if search fails
     */
    public abstract List<String> findByStatus(ObjectStatus status) throws IOException;
    
    /**
     * Find objects by object name.
     * 
     * @param objectName the object name to search for
     * @return list of matching object IDs
     * @throws IOException if search fails
     */
    public abstract List<String> findByObjectName(String objectName) throws IOException;
    
    /**
     * Find objects by role.
     * 
     * @param role the role to search for
     * @return list of matching object IDs
     * @throws IOException if search fails
     */
    public abstract List<String> findByRole(String role) throws IOException;
    
    /**
     * Find a single object by object name and role.
     * 
     * @param objectName the object name
     * @param role the role
     * @return the matching object ID, if found
     * @throws IOException if search fails
     */
    public abstract Optional<String> findByObjectNameAndRole(String objectName, String role) throws IOException;
    
    /**
     * Get all object IDs in the registry.
     * 
     * @return list of all object IDs
     * @throws IOException if retrieval fails
     */
    public abstract List<String> getAllObjectIds() throws IOException;
    
    /**
     * Get the total number of objects in the registry.
     * 
     * @return total object count
     * @throws IOException if count retrieval fails
     */
    public abstract long getObjectCount() throws IOException;
    
    /**
     * Check if an object exists in the registry.
     * 
     * @param objectId the object identifier
     * @return true if the object exists in the registry
     * @throws IOException if check fails
     */
    public abstract boolean exists(String objectId) throws IOException;
    
    /**
     * Rebuild the entire index from scratch.
     * This operation can be expensive and should be used sparingly.
     * 
     * @throws IOException if rebuild fails
     */
    public abstract void rebuildIndex() throws IOException;
    
    /**
     * Get registry-specific statistics.
     * 
     * @return registry statistics (implementation-specific format)
     * @throws IOException if statistics retrieval fails
     */
    public abstract Object getRegistryStatistics() throws IOException;
    
    /**
     * Get the registry type identifier.
     * 
     * @return registry type (e.g., "lucene", "basic", "memory")
     */
    public abstract String getRegistryType();
    
    @Override
    public void close() throws Exception {
        LOGGER.info("Closing registry helper: " + getRegistryType());
    }
}