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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.mgmt.registry;

import static java.util.Objects.requireNonNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.api.StorageRegistry;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery;
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
    
    private final Map<String, EObjectStorageService<EObject>> storagesByType = new ConcurrentHashMap<>();
    private final Map<EObjectStorageService<EObject>, String> typesByStorage = new ConcurrentHashMap<>();

    @Reference
    ManagementFactory managementFactory;

    @Reference(name = "storage", cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    void addStorageService(EObjectStorageService<EObject> storageService, Map<String, Object> properties) {
        String type = (String) properties.get("storage.type");        
        if (type != null) {
            storagesByType.put(type, storageService);
            typesByStorage.put(storageService, type);
            logger.log(Level.INFO, "Registered storage service for type: {0}", type);
        } else {
            logger.log(Level.WARNING, "Storage service registered without type property, ignoring: {0}", 
                    storageService.getClass().getName());
        }
    }

    void removeStorageService(EObjectStorageService<EObject> storageService) {
        String type = typesByStorage.remove(storageService);
        if (type != null) {
            storagesByType.remove(type);
            logger.log(Level.INFO, "Unregistered storage service for type: {0}", type);
        }
    }

  
    /* 
     * (non-Javadoc)
     * @see org.eclipse.fennec.model.atlas.mgmt.api.StorageRegistry#getAllStorages()
     */
    @Override
    public EList<EObjectStorageService<EObject>> getAllStorages() {
        return new BasicEList<>(storagesByType.values());
    }
    
    /* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.StorageRegistry#getStorageByType(java.lang.String)
	 */
	@Override
	public EObjectStorageService<EObject> getStorageByType(String type) {
		requireNonNull(type, "Type cannot be null!");
		return storagesByType.get(type);
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.StorageRegistry#getAvailableTypes()
	 */
	@Override
	public EList<String> getAvailableTypes() {
		return new BasicEList<>(typesByStorage.values().stream().distinct().toList());
	}

    /* 
     * (non-Javadoc)
     * @see org.eclipse.fennec.model.atlas.mgmt.api.StorageRegistry#searchMetadataAcrossTypes(org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery)
     */
    @Override
    public EList<ObjectMetadata> searchMetadataAcrossTypes(ObjectQuery query) {
        requireNonNull(query, "Query cannot be null");

        List<ObjectMetadata> allResults = storagesByType.values().stream()
                .flatMap(storage -> {
                    try {
                        Promise<List<ObjectMetadata>> promise = storage.queryObjects(query);
                        return promise.getValue().stream();
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Failed to query storage service", e);
                        return Stream.empty();
                    }
                })
                .collect(Collectors.toList());

        return new BasicEList<>(allResults);
    }

    @Override
    public Map<String, Object> getStorageStatistics() {
        Map<String, Object> statistics = new HashMap<>();
        Map<String, Object> typeStatistics = new HashMap<>();
        int totalObjectCount = 0;

        for (Map.Entry<String, EObjectStorageService<EObject>> entry : storagesByType.entrySet()) {
            String type = entry.getKey();
            EObjectStorageService<EObject> storage = entry.getValue();

            try {
                Map<String, Object> typeStats = new HashMap<>();
                long objectCount = storage.getObjectCount();
                typeStats.put("objectCount", objectCount);
                typeStats.put("backendType", storage.getBackendType().toString());
                
                totalObjectCount += objectCount;
                typeStatistics.put(type, typeStats);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to get statistics from storage type " + type, e);
                Map<String, Object> errorStats = new HashMap<>();
                errorStats.put("error", e.getMessage());
                typeStatistics.put(type, errorStats);
            }
        }

        statistics.put("totalObjectCount", totalObjectCount);
        statistics.put("typeCount", storagesByType.size());
        statistics.put("typeStatistics", typeStatistics);
        statistics.put("availableTypes", getAvailableTypes());

        return statistics;
    }
}