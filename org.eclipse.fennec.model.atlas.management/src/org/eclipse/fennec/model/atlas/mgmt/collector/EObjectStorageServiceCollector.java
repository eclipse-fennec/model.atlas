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
 * and provides lookup by scope and storage role.
 *
 * <p>Storage services register with:
 * <ul>
 * <li>"storage.scope" - the scope/tenant this storage belongs to (e.g., "my-tenant", "global")</li>
 * <li>"storage.role" - the role/stage type (e.g., "draft", "release")</li>
 * </ul>
 * This collector allows dynamic lookup of storage services by (scope, role) tuple.</p>
 *
 * <h3>Usage Example:</h3>
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
@Component(
    name = "EObjectStorageServiceCollector",
    immediate = true,
    service = EObjectStorageServiceCollector.class
)
public class EObjectStorageServiceCollector {

    private static final Logger LOGGER = Logger.getLogger(EObjectStorageServiceCollector.class.getName());

    // Map: "scope:role" -> StorageService
    private final Map<String, EObjectStorageService<?>> storageByKey = new ConcurrentHashMap<>();

    // Fallback map for shared storage (no scope specified): "role" -> StorageService
    private final Map<String, EObjectStorageService<?>> sharedStorageByRole = new ConcurrentHashMap<>();

    /**
     * Get a storage service by scope and role.
     *
     * <p>Lookup order:
     * <ol>
     * <li>Scope-specific storage: (scope, role)</li>
     * <li>Shared storage: (role)</li>
     * </ol>
     * </p>
     *
     * @param scope the scope/tenant name (e.g., "my-tenant", "global")
     * @param role the storage role (e.g., "draft", "release", "archive")
     * @return the storage service for the specified scope and role, or null if not found
     */
    public EObjectStorageService<?> getStorage(String scope, String role) {
        if (scope == null || scope.isEmpty()) {
            LOGGER.warning("Attempted to get storage with null or empty scope");
            return null;
        }
        if (role == null || role.isEmpty()) {
            LOGGER.warning("Attempted to get storage with null or empty role");
            return null;
        }

        // Try scope-specific storage first
        String key = buildKey(scope, role);
        EObjectStorageService<?> storage = storageByKey.get(key);

        // Fallback to shared storage
        if (storage == null) {
            storage = sharedStorageByRole.get(role);
            if (storage != null) {
                LOGGER.fine(String.format("Using shared storage for scope '%s', role '%s'", scope, role));
            }
        }

        return storage;
    }

    /**
     * Get a storage service by its role (backward compatibility).
     *
     * @param role the storage role (e.g., "draft", "release", "archive")
     * @return the storage service with the specified role, or null if not found
     * @deprecated Use {@link #getStorage(String, String)} instead
     */
    @Deprecated
    public EObjectStorageService<?> getStorageByRole(String role) {
        if (role == null || role.isEmpty()) {
            LOGGER.warning("Attempted to get storage with null or empty role");
            return null;
        }
        return sharedStorageByRole.get(role);
    }

    /**
     * Check if a storage service exists for the specified scope and role.
     *
     * @param scope the scope name
     * @param role the storage role to check
     * @return true if a storage service exists for this scope and role
     */
    public boolean hasStorage(String scope, String role) {
        if (scope == null || role == null) {
            return false;
        }
        String key = buildKey(scope, role);
        return storageByKey.containsKey(key) || sharedStorageByRole.containsKey(role);
    }

    /**
     * Check if a storage service with the specified role exists (backward compatibility).
     *
     * @param role the storage role to check
     * @return true if a storage service with this role is registered
     * @deprecated Use {@link #hasStorage(String, String)} instead
     */
    @Deprecated
    public boolean hasStorageForRole(String role) {
        return role != null && sharedStorageByRole.containsKey(role);
    }

    /**
     * Get all registered storage keys in format "scope:role".
     *
     * @return array of registered storage keys
     */
    public String[] getRegisteredKeys() {
        return storageByKey.keySet().toArray(new String[0]);
    }

    /**
     * Get all registered storage roles (shared storage only).
     *
     * @return array of registered storage role names
     * @deprecated Use {@link #getRegisteredKeys()} instead
     */
    @Deprecated
    public String[] getRegisteredRoles() {
        return sharedStorageByRole.keySet().toArray(new String[0]);
    }

    /**
     * Build a key for storage lookup.
     *
     * @param scope the scope name
     * @param role the role name
     * @return the composite key "scope:role"
     */
    private String buildKey(String scope, String role) {
        return scope + ":" + role;
    }

    /**
     * Bind a storage service when it becomes available.
     *
     * <p>Storage services can register in two ways:
     * <ul>
     * <li>Scope-specific: with both "storage.scope" and "storage.role" properties</li>
     * <li>Shared: with only "storage.role" property (no scope)</li>
     * </ul>
     * </p>
     *
     * @param storageService the storage service to register
     * @param properties the service properties
     */
    @Reference(
        cardinality = ReferenceCardinality.MULTIPLE,
        policy = ReferencePolicy.DYNAMIC,
        policyOption = ReferencePolicyOption.GREEDY
    )
    public void bindStorageService(EObjectStorageService<?> storageService, Map<String, Object> properties) {
        Object roleObj = properties.get("storage.role");

        if (roleObj == null) {
            LOGGER.warning("EObjectStorageService registered without 'storage.role' property - ignoring");
            return;
        }

        String role = roleObj.toString();
        if (role.isEmpty()) {
            LOGGER.warning("EObjectStorageService registered with empty 'storage.role' property - ignoring");
            return;
        }

        Object scopeObj = properties.get("storage.scope");

        if (scopeObj != null && !scopeObj.toString().isEmpty()) {
            // Scope-specific storage
            String scope = scopeObj.toString();
            String key = buildKey(scope, role);

            EObjectStorageService<?> existing = storageByKey.put(key, storageService);
            if (existing != null) {
                LOGGER.warning(String.format(
                    "Multiple EObjectStorageService instances registered for scope '%s', role '%s' - overwriting",
                    scope, role
                ));
            } else {
                LOGGER.info(String.format("Registered EObjectStorageService for scope '%s', role '%s'", scope, role));
            }
        } else {
            // Shared storage (no scope specified)
            EObjectStorageService<?> existing = sharedStorageByRole.put(role, storageService);
            if (existing != null) {
                LOGGER.warning(String.format(
                    "Multiple shared EObjectStorageService instances registered with role '%s' - overwriting",
                    role
                ));
            } else {
                LOGGER.info(String.format("Registered shared EObjectStorageService with role '%s'", role));
            }
        }
    }

    /**
     * Unbind a storage service when it is no longer available.
     *
     * @param storageService the storage service to unregister
     * @param properties the service properties
     */
    public void unbindStorageService(EObjectStorageService<?> storageService, Map<String, Object> properties) {
        Object roleObj = properties.get("storage.role");

        if (roleObj == null) {
            return;
        }

        String role = roleObj.toString();
        Object scopeObj = properties.get("storage.scope");

        if (scopeObj != null && !scopeObj.toString().isEmpty()) {
            // Scope-specific storage
            String scope = scopeObj.toString();
            String key = buildKey(scope, role);

            boolean removed = storageByKey.remove(key, storageService);
            if (removed) {
                LOGGER.info(String.format("Unregistered EObjectStorageService for scope '%s', role '%s'", scope, role));
            }
        } else {
            // Shared storage
            boolean removed = sharedStorageByRole.remove(role, storageService);
            if (removed) {
                LOGGER.info(String.format("Unregistered shared EObjectStorageService with role '%s'", role));
            }
        }
    }
}
