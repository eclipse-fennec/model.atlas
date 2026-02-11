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
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.mgmt.collector;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * Collector service that tracks all registered EObjectStorageService instances
 * and provides lookup by type
 *
 * <p>
 * Storage services register with:
 * <ul>
 * <li>"storage.scope" - the scope/tenant this storage belongs to (e.g.,
 * "my-tenant", "global")</li>
 * <li>"storage.role" - the role/stage type (e.g., "draft", "release")</li>
 * </ul>
 * This collector allows dynamic lookup of storage services by (scope, role)
 * tuple.
 * </p>
 *
 * <h3>Usage Example:</h3>
 * 
 * <pre>
 * &#64;Reference
 * private EObjectStorageServiceCollector storageCollector;
 *
 * public void uploadToStage(String scope, String stage, EObject object) {
 *     String role = getStorageRoleForStage(stage); // e.g., "Draft" -> "draft"
 *     EObjectStorageService storage = storageCollector.getStorage(scope, role);
 *     storage.storeObject(objectId, object, metadata);
 * }
 * </pre>
 *
 * @author Data In Motion
 * @since 1.0
 */
@Component(name = "EObjectStorageServiceCollector", immediate = true, service = EObjectStorageServiceCollector.class)
public class EObjectStorageServiceCollector {

    private static final Logger LOGGER = Logger.getLogger(EObjectStorageServiceCollector.class.getName());

    // Map: "backendType:type" -> StorageService
    private final Map<String, EObjectStorageService<?>> storageByKey = new ConcurrentHashMap<>();

    /**
     * @param backendType the backend type of the storage (e.g. apicurio, file, etc)
     * @param type        the type of the storage (e.g. apicurio, file, etc)
     * @return the storage service for the specified backendType/type pair
     */
    public EObjectStorageService<?> getStorage(String backendType, String type) {
        if (backendType == null || backendType.isEmpty()) {
            LOGGER.warning("Attempted to get storage with null or empty backendType");
            return null;
        }
        if (type == null || type.isEmpty()) {
            LOGGER.warning("Attempted to get storage with null or empty type");
            return null;
        }
        String key = buildKey(backendType, type);
        return storageByKey.getOrDefault(key, null);
    }

    /**
     * Check if a storage service exists for the specified backednType/type pair
     *
     * @param backendType the backend type of the storage (e.g. apicurio, file, etc)
     * @param type        the type of the storage (e.g. apicurio, file, etc)
     * @return true if a storage service exists for this backednType/type pair
     */
    public boolean hasStorage(String backendType, String type) {
        if (backendType == null || type == null) {
            return false;
        }
        String key = buildKey(backendType, type);
        return storageByKey.containsKey(key);
    }

    /**
     * Get all registered storage keys in format "scope:registry:role".
     *
     * @return array of registered storage keys
     */
    public String[] getRegisteredKeys() {
        return storageByKey.keySet().toArray(new String[0]);
    }

    /**
     * @param backendType
     * @param type
     * @return the composite key "backendType:type"
     */
    private String buildKey(String backendType, String type) {
        return backendType + ":" + type;
    }

    /**
     * Bind a storage service when it becomes available.
     *
     * @param storageService the storage service to register
     * @param properties     the service properties
     */
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    public void bindStorageService(EObjectStorageService<?> storageService, Map<String, Object> properties) {
        Object storageBackendType = properties.get("storage.backend");
        Object storageType = properties.get("storage.type");

        if (storageBackendType == null || storageBackendType.toString().isEmpty()) {
            LOGGER.warning("EObjectStorageService registered without or empty 'storage.backend' property - ignoring");
            return;
        }
        if (storageType == null || storageType.toString().isEmpty()) {
            LOGGER.warning("EObjectStorageService registered without or empty 'storage.type' property - ignoring");
            return;
        }

        String backendType = storageBackendType.toString();
        String type = storageType.toString();

        // type-specific storage
        String key = buildKey(backendType, type);

        EObjectStorageService<?> existing = storageByKey.put(key, storageService);
        if (existing != null) {
            LOGGER.warning(String.format(
                    "Multiple EObjectStorageService instances registered for backendType '%s', type '%s' - overwriting",
                    backendType, type));
        } else {
            LOGGER.info(String.format("Registered EObjectStorageService for backendType '%s', type '%s'", backendType,
                    type));
        }
    }

    /**
     * Unbind a storage service when it is no longer available.
     *
     * @param storageService the storage service to unregister
     * @param properties     the service properties
     */
    public void unbindStorageService(EObjectStorageService<?> storageService, Map<String, Object> properties) {
        Object storageBackendType = properties.get("storage.backend");
        Object storageType = properties.get("storage.type");

        if (storageBackendType == null || storageType == null) {
            return;
        }

        String backendType = storageBackendType.toString();
        String type = storageType.toString();

        String key = buildKey(backendType, type);

        boolean removed = storageByKey.remove(key, storageService);
        if (removed) {
            LOGGER.info(String.format("Unregistered EObjectStorageService for backendType '%s', type '%s'", backendType,
                    type));
        }
    }

}
