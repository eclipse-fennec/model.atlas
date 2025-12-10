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

	// Map: "scope:registry:role" -> StorageService
	private final Map<String, EObjectStorageService<?>> storageByKey = new ConcurrentHashMap<>();


	/**
	 * Get a storage service by scope, registry and role.
	 *
	 * @param scope the scope/tenant name (e.g., "my-tenant", "global")
	 * @param registry the registry name (e.g., "schema", "configuration")
	 * @param role the storage role (e.g., "draft", "release", "archive")
	 * @return the storage service for the specified scope, registry and role, or null if not found
	 */
	public EObjectStorageService<?> getStorage(String scope, String registry, String role) {
		if (scope == null || scope.isEmpty()) {
			LOGGER.warning("Attempted to get storage with null or empty scope");
			return null;
		}
		if (registry == null || registry.isEmpty()) {
			LOGGER.warning("Attempted to get storage with null or empty registry");
			return null;
		}
		if (role == null || role.isEmpty()) {
			LOGGER.warning("Attempted to get storage with null or empty role");
			return null;
		}
		String key = buildKey(scope, registry, role);
		return storageByKey.getOrDefault(key, null);
	}



	/**
	 * Check if a storage service exists for the specified scope, registry and role.
	 *
	 * @param scope the scope name
	 * @param registry the registry name
	 * @param role the storage role to check
	 * @return true if a storage service exists for this scope, registry and role
	 */
	public boolean hasStorage(String scope, String registry, String role) {
		if (scope == null || registry == null || role == null) {
			return false;
		}
		String key = buildKey(scope, registry, role);
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
	 * Build a key for storage lookup.
	 *
	 * @param scope the scope name
	 * @param registry the registry name
	 * @param role the role name
	 * @return the composite key "scope:registry:role"
	 */
	private String buildKey(String scope, String registry, String role) {
		return scope + ":" + registry + ":" + role;
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
		Object scopeObj = properties.get("storage.scope");
		Object registryObj = properties.get("storage.registry");

		if (roleObj == null || roleObj.toString().isEmpty()) {
			LOGGER.warning("EObjectStorageService registered without or empty 'storage.role' property - ignoring");
			return;
		}
		if (scopeObj == null || scopeObj.toString().isEmpty()) {
			LOGGER.warning("EObjectStorageService registered without or empty 'storage.scope' property - ignoring");
			return;
		}
		if (registryObj == null || registryObj.toString().isEmpty()) {
			LOGGER.warning("EObjectStorageService registered without or empty 'storage.registry' property - ignoring");
			return;
		}

		String role = roleObj.toString();
		String scope = scopeObj.toString();
		String registry = registryObj.toString();

		// Scope-specific storage
		String key = buildKey(scope, registry, role);

		EObjectStorageService<?> existing = storageByKey.put(key, storageService);
		if (existing != null) {
			LOGGER.warning(String.format(
					"Multiple EObjectStorageService instances registered for scope '%s', role '%s' - overwriting",
					scope, role
					));
		} else {
			LOGGER.info(String.format("Registered EObjectStorageService for scope '%s', registry '%s' and role '%s'", scope, registry, role));
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
		Object scopeObj = properties.get("storage.scope");
		Object registryObj = properties.get("storage.registry");

		if (roleObj == null || scopeObj == null || registryObj == null) {
			return;
		}

		String role = roleObj.toString();
		String scope = scopeObj.toString();
		String registry = registryObj.toString();

		String key = buildKey(scope, registry, role);

		boolean removed = storageByKey.remove(key, storageService);
		if (removed) {
			LOGGER.info(String.format("Unregistered EObjectStorageService for scope '%s', registry '%s' and role '%s'", scope, registry, role));
		}
	}

}
