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

import static java.util.Objects.requireNonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.api.StorageRegistry;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.util.promise.Promise;

/**
 * Basic implementation of StorageRegistry that provides centralized access to all storage services
 * by role and handles governance documentation ID lifecycle management.
 * 
 * <p>This implementation maintains a registry of all EObjectStorageService instances in the system,
 * organized by their storage role configuration. It provides role-based service discovery and
 * handles cross-storage operations like governance documentation ID updates.</p>
 * 
 * <h3>Features</h3>
 * <ul>
 * <li><strong>Role-based Discovery</strong> - Find storage services by role name (draft, approved, release, documentation, archived)</li>
 * <li><strong>Dynamic Service Registration</strong> - Automatically tracks storage services as they come and go</li>
 * <li><strong>Cross-storage Operations</strong> - Update governance documentation IDs across all relevant storage services</li>
 * <li><strong>Aggregated Queries</strong> - Search metadata across all storage services in a single operation</li>
 * <li><strong>Statistics Collection</strong> - Gather comprehensive statistics from all registered storage services</li>
 * <li><strong>Thread Safety</strong> - Concurrent access to storage service registry</li>
 * </ul>
 * 
 * <h3>Storage Role Configuration</h3>
 * <p>Storage services are identified by their role through OSGi service properties. The expected
 * property key is {@code storage.role} with values like:</p>
 * <ul>
 * <li>{@code draft} - Draft storage for objects under development</li>
 * <li>{@code approved} - Approved storage for objects pending release</li>
 * <li>{@code release} - Release/production storage for live objects</li>
 * <li>{@code documentation} - Governance documentation storage</li>
 * <li>{@code archived} - Historical storage for deprecated objects</li>
 * </ul>
 * 
 * @author Mark Hoffmann
 * @since 1.0
 */
@Component(name="BasicStorageRegistry", configurationPolicy = ConfigurationPolicy.REQUIRE)
public class BasicStorageRegistry implements StorageRegistry {

    private static final Logger logger = Logger.getLogger(BasicStorageRegistry.class.getName());
    
    private final Map<String, EObjectStorageService<EObject>> storagesByRole = new ConcurrentHashMap<>();
    private final Map<EObjectStorageService<EObject>, String> rolesByStorage = new ConcurrentHashMap<>();

    @Reference
    ManagementFactory managementFactory;

    @Reference(name = "storage", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    void addStorageService(EObjectStorageService<EObject> storageService, Map<String, Object> properties) {
        String role = (String) properties.get("storage.role");
        if (role != null) {
            storagesByRole.put(role, storageService);
            rolesByStorage.put(storageService, role);
            logger.log(Level.INFO, "Registered storage service for role: {0}", role);
        } else {
            logger.log(Level.WARNING, "Storage service registered without role property, ignoring: {0}", 
                    storageService.getClass().getName());
        }
    }

    void removeStorageService(EObjectStorageService<EObject> storageService) {
        String role = rolesByStorage.remove(storageService);
        if (role != null) {
            storagesByRole.remove(role);
            logger.log(Level.INFO, "Unregistered storage service for role: {0}", role);
        }
    }

    @Override
    public EObjectStorageService<EObject> getStorageByRole(String role) {
        requireNonNull(role, "Role cannot be null");
        return storagesByRole.get(role);
    }

    @Override
    public EList<EObjectStorageService<EObject>> getAllStorages() {
        return new BasicEList<>(storagesByRole.values());
    }

    @Override
    public EList<String> getAvailableRoles() {
        return new BasicEList<>(storagesByRole.keySet());
    }

    @Override
    public int updateGovernanceDocumentationId(String role, String objectName, String newDocumentationId, String reason) {
        requireNonNull(role, "Role cannot be null");
        requireNonNull(objectName, "Object name cannot be null");
        requireNonNull(newDocumentationId, "New documentation ID cannot be null");

        AtomicInteger updateCount = new AtomicInteger(0);
        String logReason = reason != null ? reason : "Governance documentation ID update";

        // Get the specific storage service for the specified role
        EObjectStorageService<EObject> storage = storagesByRole.get(role);
        if (storage == null) {
            logger.log(Level.WARNING, "No storage service found for role: {0}", role);
            return 0;
        }

        try {
            // Query for objects with the specified name in this specific storage
            // We need to scan storage directly to avoid EMF enum default issues with delegation
            List<ObjectMetadata> allObjects = new ArrayList<>();
            Promise<List<String>> objectIdsPromise = storage.listObjectIds();
            List<String> objectIds = objectIdsPromise.getValue();
            
            // Load metadata for each object and filter by objectType
            for (String objectId : objectIds) {
                try {
                    Promise<ObjectMetadata> metadataPromise = storage.retrieveMetadata(objectId);
                    ObjectMetadata metadata = metadataPromise.getValue();
                    if (metadata != null && objectName.equals(metadata.getObjectType())) {
                        allObjects.add(metadata);
                    }
                } catch (Exception e) {
                    logger.log(Level.WARNING, "Failed to load metadata for object " + objectId, e);
                }
            }
            
            List<ObjectMetadata> objects = allObjects;

            for (ObjectMetadata metadata : objects) {
                // Only update active objects (not archived) unless specifically documentation role
                if (shouldUpdateDocumentationId(metadata, role)) {
                    metadata.setGovernanceDocumentationId(newDocumentationId);
                    metadata.setLastChangeTime(java.time.Instant.now());
                    if (reason != null) {
                        metadata.setLastChangeReason(reason);
                    }

                    Promise<Boolean> updatePromise = storage.updateMetadata(metadata.getObjectId(), metadata);
                    Boolean updated = updatePromise.getValue();
                    
                    if (Boolean.TRUE.equals(updated)) {
                        updateCount.incrementAndGet();
                        logger.log(Level.FINE, "Updated governance documentation ID for object {0} in role {1}", 
                                new Object[]{metadata.getObjectId(), role});
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to update governance documentation ID in storage role " + role, e);
        }

        int totalUpdated = updateCount.get();
        logger.log(Level.INFO, "Updated governance documentation ID for {0} objects with name ''{1}'' in role ''{2}'' - reason: {3}", 
                new Object[]{totalUpdated, objectName, role, logReason});
        
        return totalUpdated;
    }

    @Override
    public EList<ObjectMetadata> searchMetadataAcrossRoles(ObjectQuery query) {
        requireNonNull(query, "Query cannot be null");

        List<ObjectMetadata> allResults = storagesByRole.values().stream()
                .flatMap(storage -> {
                    try {
                        Promise<List<ObjectMetadata>> promise = storage.queryObjects(query);
                        return promise.getValue().stream();
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Failed to query storage service", e);
                        return java.util.stream.Stream.empty();
                    }
                })
                .collect(Collectors.toList());

        return new BasicEList<>(allResults);
    }

    @Override
    public Map<String, Object> getStorageStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        Map<String, Object> roleStatistics = new HashMap<>();
        int totalObjectCount = 0;

        for (Map.Entry<String, EObjectStorageService<EObject>> entry : storagesByRole.entrySet()) {
            String role = entry.getKey();
            EObjectStorageService<EObject> storage = entry.getValue();

            try {
                Map<String, Object> roleStats = new HashMap<>();
                long objectCount = storage.getObjectCount();
                roleStats.put("objectCount", objectCount);
                roleStats.put("backendType", storage.getBackendType().toString());
                
                totalObjectCount += objectCount;
                roleStatistics.put(role, roleStats);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to get statistics from storage role " + role, e);
                Map<String, Object> errorStats = new HashMap<>();
                errorStats.put("error", e.getMessage());
                roleStatistics.put(role, errorStats);
            }
        }

        statistics.put("totalObjectCount", totalObjectCount);
        statistics.put("roleCount", storagesByRole.size());
        statistics.put("roleStatistics", roleStatistics);
        statistics.put("availableRoles", getAvailableRoles());

        return statistics;
    }

    private ObjectQuery createObjectNameQuery(String objectName) {
        ObjectQuery query = managementFactory.createObjectQuery();
        // Since ObjectQuery doesn't have objectName property, we use objectType
        // In practice, objectName often corresponds to objectType (e.g., package name)
        query.setObjectType(objectName);
        // Note: We intentionally don't set status here because we want to find objects
        // of any status with the given objectType. The AbstractEObjectStorageService
        // delegation logic will handle this properly.
        return query;
    }

    private boolean shouldUpdateDocumentationId(ObjectMetadata metadata, String role) {
        // Always update documentation storage
        if ("documentation".equals(role)) {
            return true;
        }
        
        // For other roles, only update active objects (not archived)
        if ("archived".equals(role)) {
            return false;
        }
        
        // Update objects that are in active workflow states
        ObjectStatus status = metadata.getStatus();
        return status == ObjectStatus.DRAFT || 
               status == ObjectStatus.APPROVED || 
               status == ObjectStatus.DEPLOYED;
    }
}