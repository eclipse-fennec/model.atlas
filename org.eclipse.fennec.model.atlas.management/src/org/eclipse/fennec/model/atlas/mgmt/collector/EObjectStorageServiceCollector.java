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
 * and provides lookup by storage role.
 *
 * <p>Storage services register with a "storage.role" property that identifies
 * their purpose (e.g., "draft", "release", "archive"). This collector allows
 * dynamic lookup of storage services by role name.</p>
 *
 * <h3>Usage Example:</h3>
 * <pre>
 * &#64;Reference
 * private EObjectStorageServiceCollector storageCollector;
 *
 * public void uploadToStage(String stage, EObject object) {
 *     String role = getStorageRoleForStage(stage); // e.g., "Draft" -> "draft"
 *     EObjectStorageService storage = storageCollector.getStorageByRole(role);
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

    private final Map<String, EObjectStorageService<?>> storageByRole = new ConcurrentHashMap<>();

    /**
     * Get a storage service by its role.
     *
     * @param role the storage role (e.g., "draft", "release", "archive")
     * @return the storage service with the specified role, or null if not found
     */
    public EObjectStorageService<?> getStorageByRole(String role) {
        if (role == null || role.isEmpty()) {
            LOGGER.warning("Attempted to get storage with null or empty role");
            return null;
        }
        return storageByRole.get(role);
    }

    /**
     * Check if a storage service with the specified role exists.
     *
     * @param role the storage role to check
     * @return true if a storage service with this role is registered
     */
    public boolean hasStorageForRole(String role) {
        return role != null && storageByRole.containsKey(role);
    }

    /**
     * Get all registered storage roles.
     *
     * @return array of registered storage role names
     */
    public String[] getRegisteredRoles() {
        return storageByRole.keySet().toArray(new String[0]);
    }

    /**
     * Bind a storage service when it becomes available.
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

        EObjectStorageService<?> existing = storageByRole.put(role, storageService);
        if (existing != null) {
            LOGGER.warning(String.format(
                "Multiple EObjectStorageService instances registered with role '%s' - overwriting it",
                role
            ));
        } else {
            LOGGER.info(String.format("Registered EObjectStorageService with role '%s'", role));
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

        if (roleObj != null) {
            String role = roleObj.toString();
            boolean removed = storageByRole.remove(role, storageService);
            if (removed) {
                LOGGER.info(String.format("Unregistered EObjectStorageService with role '%s'", role));
            }
        }
    }
}
