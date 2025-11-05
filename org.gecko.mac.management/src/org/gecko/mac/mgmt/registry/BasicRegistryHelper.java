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
package org.gecko.mac.mgmt.registry;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.gecko.mac.mgmt.management.ObjectMetadata;
import org.gecko.mac.mgmt.management.ObjectStatus;

/**
 * Basic in-memory registry helper implementation.
 * 
 * <p>This implementation provides simple in-memory indexing for ObjectMetadata
 * without external dependencies. It's suitable for smaller datasets or when
 * advanced indexing features are not required.</p>
 * 
 * <h3>Features</h3>
 * <ul>
 * <li><strong>In-Memory Storage</strong> - All metadata stored in memory maps</li>
 * <li><strong>Thread-Safe</strong> - Uses ConcurrentHashMap for safe concurrent access</li>
 * <li><strong>Fast Access</strong> - O(1) lookups for direct queries</li>
 * <li><strong>Simple Filtering</strong> - Stream-based filtering for complex queries</li>
 * <li><strong>No Dependencies</strong> - Uses only standard Java collections</li>
 * </ul>
 * 
 * <h3>Performance Characteristics</h3>
 * <ul>
 * <li><strong>updateIndex()</strong> - O(1)</li>
 * <li><strong>removeFromIndex()</strong> - O(1)</li>
 * <li><strong>findByStatus()</strong> - O(n) with stream filtering</li>
 * <li><strong>findByObjectName()</strong> - O(n) with stream filtering</li>
 * <li><strong>findByRole()</strong> - O(n) with stream filtering</li>
 * </ul>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 */
public class BasicRegistryHelper extends AbstractRegistryHelper {
    
    private final Map<String, ObjectMetadata> metadataById = new ConcurrentHashMap<>();
    private volatile boolean initialized = false;
    
    @Override
    public void initialize() throws IOException {
        // No special initialization needed for in-memory registry
        initialized = true;
    }
    
    @Override
    public void updateIndex(String objectId, ObjectMetadata metadata) throws IOException {
        Objects.requireNonNull(objectId, "Object ID cannot be null");
        Objects.requireNonNull(metadata, "Metadata cannot be null");
        
        metadataById.put(objectId, metadata);
    }
    
    @Override
    public void removeFromIndex(String objectId) throws IOException {
        Objects.requireNonNull(objectId, "Object ID cannot be null");
        
        metadataById.remove(objectId);
    }
    
    @Override
    public List<String> searchObjectIds(String query, int maxResults) throws IOException {
        // Basic implementation - just return all IDs (query parsing not implemented)
        return getAllObjectIds().stream()
            .limit(maxResults > 0 ? maxResults : Integer.MAX_VALUE)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<String> findByStatus(ObjectStatus status) throws IOException {
        Objects.requireNonNull(status, "Status cannot be null");
        
        return metadataById.entrySet().stream()
            .filter(entry -> status.equals(entry.getValue().getStatus()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<String> findByObjectName(String objectName) throws IOException {
        Objects.requireNonNull(objectName, "Object name cannot be null");
        
        return metadataById.entrySet().stream()
            .filter(entry -> objectName.equals(entry.getValue().getObjectName()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<String> findByRole(String role) throws IOException {
        Objects.requireNonNull(role, "Role cannot be null");
        
        return metadataById.entrySet().stream()
            .filter(entry -> role.equals(entry.getValue().getRole()))
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
    }
    
    @Override
    public Optional<String> findByObjectNameAndRole(String objectName, String role) throws IOException {
        Objects.requireNonNull(objectName, "Object name cannot be null");
        Objects.requireNonNull(role, "Role cannot be null");
        
        return metadataById.entrySet().stream()
            .filter(entry -> objectName.equals(entry.getValue().getObjectName()) && 
                           role.equals(entry.getValue().getRole()))
            .map(Map.Entry::getKey)
            .findFirst();
    }
    
    @Override
    public List<String> getAllObjectIds() throws IOException {
        return List.copyOf(metadataById.keySet());
    }
    
    @Override
    public long getObjectCount() throws IOException {
        return metadataById.size();
    }
    
    @Override
    public boolean exists(String objectId) throws IOException {
        Objects.requireNonNull(objectId, "Object ID cannot be null");
        
        return metadataById.containsKey(objectId);
    }
    
    @Override
    public void rebuildIndex() throws IOException {
        // For in-memory registry, we keep existing data
        // This could be enhanced to reload from a data source if needed
    }
    
    @Override
    public Object getRegistryStatistics() throws IOException {
        Map<String, Object> stats = new HashMap<>();
        
        stats.put("registryType", getRegistryType());
        stats.put("totalObjects", (long) metadataById.size());
        stats.put("initialized", initialized);
        
        // Status distribution
        Map<String, Long> statusCounts = metadataById.values().stream()
            .collect(Collectors.groupingBy(
                metadata -> metadata.getStatus() != null ? metadata.getStatus().getLiteral() : "unknown",
                Collectors.counting()
            ));
        stats.put("statusDistribution", statusCounts);
        
        // Role distribution
        Map<String, Long> roleCounts = metadataById.values().stream()
            .collect(Collectors.groupingBy(
                metadata -> metadata.getRole() != null ? metadata.getRole() : "unknown",
                Collectors.counting()
            ));
        stats.put("roleDistribution", roleCounts);
        
        return stats;
    }
    
    @Override
    public String getRegistryType() {
        return "basic-memory";
    }
    
    @Override
    public void close() throws Exception {
        super.close();
        metadataById.clear();
        initialized = false;
    }
    
    /**
     * Get all metadata objects (for testing and debugging).
     * 
     * @return set of all metadata objects
     */
    public Set<ObjectMetadata> getAllMetadata() {
        return Set.copyOf(metadataById.values());
    }
}