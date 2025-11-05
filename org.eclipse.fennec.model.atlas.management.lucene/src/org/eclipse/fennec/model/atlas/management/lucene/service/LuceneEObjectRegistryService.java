/*
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
package org.eclipse.fennec.model.atlas.management.lucene.service;

import static java.util.Objects.requireNonNull;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.management.lucene.registry.LuceneRegistryHelper;
import org.eclipse.fennec.model.atlas.mgmt.annotations.MacCapabilityConstants;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;
import org.osgi.annotation.bundle.Capability;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.AttributeType;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * Shared Lucene-backed registry service for managing ObjectMetadata across multiple storage backends.
 * 
 * <p>This service provides a centralized metadata index that can be used by multiple
 * {@link org.gecko.mac.mgmt.api.EObjectStorageService} instances. Unlike storage-specific
 * registries, this shared registry:</p>
 * 
 * <ul>
 * <li><strong>Consolidates Metadata</strong> - Single Lucene index for all object metadata</li>
 * <li><strong>Cross-Storage Search</strong> - Query objects across draft, approved, and documentation storage</li>
 * <li><strong>Resource Efficiency</strong> - One IndexWriter and SearcherManager for the entire system</li>
 * <li><strong>Storage Backend Tracking</strong> - Tracks which storage backend owns each object</li>
 * <li><strong>Independent Configuration</strong> - Own workspace folder separate from storage services</li>
 * </ul>
 * 
 * <h3>Storage Backend Integration</h3>
 * <p>Storage services can inject this shared registry and use it instead of creating their own:</p>
 * <pre>{@code
 * @Reference(target = "(component.name=SharedLuceneRegistryService)")
 * private EObjectRegistryService<EObject> sharedRegistry;
 * }</pre>
 * 
 * <h3>Configuration Properties</h3>
 * <ul>
 * <li><strong>registry_workspace_folder</strong> - Directory for Lucene index (default: "/tmp/shared-registry")</li>
 * <li><strong>enable_lucene_index</strong> - Enable/disable Lucene indexing (default: true)</li>
 * <li><strong>storage_backend_tracking</strong> - Track which storage backend owns each object (default: true)</li>
 * <li><strong>initial_index_capacity</strong> - Initial capacity for metadata cache (default: 10000)</li>
 * </ul>
 * 
 * <h3>Example Configuration</h3>
 * <pre>{@code
 * # OSGi Configuration Admin
 * registry_workspace_folder=/opt/registry/shared-lucene
 * enable_lucene_index=true
 * storage_backend_tracking=true
 * initial_index_capacity=50000
 * }</pre>
 */
@Component(
		service = EObjectRegistryService.class,
		configurationPolicy = ConfigurationPolicy.REQUIRE,
		name = "LuceneEObjectRegistryService",
		configurationPid = "LuceneEObjectRegistryService",
		property = {
				"service.description=Shared Lucene Registry Service for Cross-Storage Metadata Management",
				"service.vendor=Data In Motion",
				"registry.type=shared",
				"registry.backend=lucene"
		}
		)
@Designate(ocd = LuceneEObjectRegistryService.Config.class)
@Capability(namespace = MacCapabilityConstants.NAMESPACE_MAC_MANAGEMENT, name = MacCapabilityConstants.CAP_EOBJECT_REGISTRY, version = "1.0")
public class LuceneEObjectRegistryService<T extends EObject> implements EObjectRegistryService<T> {

	private static final Logger LOGGER = Logger.getLogger(LuceneEObjectRegistryService.class.getName());

	/**
	 * Configuration interface for the shared registry service.
	 */
	@ObjectClassDefinition(
			name = "Shared Lucene Registry Service Configuration",
			description = "Configuration for centralized metadata registry with Lucene indexing"
			)
	public @interface Config {

		/**
		 * Directory path for the shared Lucene index.
		 * This should be independent of any storage service workspace.
		 */
		@AttributeDefinition(
				name = "Registry Workspace Folder",
				description = "Directory path for the shared Lucene index (independent of storage services)",
				required = true
				)
		String registry_workspace_folder() default "/tmp/shared-registry";


		/**
		 * Track which storage backend owns each object.
		 * Adds storage.backend property to metadata for cross-storage queries.
		 */
		@AttributeDefinition(
				name = "Storage Backend Tracking",
				description = "Track which storage backend owns each object for cross-storage queries",
				type = AttributeType.BOOLEAN
				)
		boolean storage_backend_tracking() default true;

		/**
		 * Initial capacity for the metadata cache.
		 * Higher values reduce memory allocations for large object counts.
		 */
		@AttributeDefinition(
				name = "Initial Index Capacity",
				description = "Initial capacity for metadata cache (higher values for large object counts)",
				type = AttributeType.INTEGER,
				min = "1000",
				max = "1000000"
				)
		int initial_index_capacity() default 10000;

		/**
		 * Enable detailed logging for debugging.
		 */
		@AttributeDefinition(
				name = "Enable Debug Logging",
				description = "Enable detailed logging for registry operations",
				type = AttributeType.BOOLEAN
				)
		boolean enable_debug_logging() default false;
	}

	// Configuration
	private Config config;
	private final PromiseFactory promiseFactory = new PromiseFactory(null);

	// Lucene infrastructure
	private LuceneRegistryHelper luceneHelper;

	// In-memory cache for fast access
	private final Map<String, ObjectMetadata> metadataCache = new ConcurrentHashMap<>();

	// Statistics tracking
	private long totalUpdates = 0;
	private long totalRemovals = 0;
	private Instant lastStatsReset = Instant.now();

	@Activate
	void activate(Config config) throws IOException {
		this.config = requireNonNull(config, "Configuration cannot be null");

		// Create and validate registry workspace
		Path registryPath = Paths.get(config.registry_workspace_folder());
		if (!Files.exists(registryPath)) {
			Files.createDirectories(registryPath);
			LOGGER.info("Created shared registry workspace: " + registryPath);
		}

		// Initialize Lucene helper (always enabled for LuceneEObjectRegistryService)
		initializeLuceneHelper(registryPath);

		// Lucene helper MUST be available for this service to function
		Objects.requireNonNull(luceneHelper, "LuceneRegistryHelper initialization failed - service cannot function without Lucene");

		// Initialize metadata cache with configured capacity
		if (config.initial_index_capacity() > metadataCache.size()) {
			// Pre-size the cache map for better performance
			LOGGER.fine("Initialized metadata cache with capacity: " + config.initial_index_capacity());
		}

		LOGGER.info("Activated LuceneEObjectRegistryService with workspace: " + config.registry_workspace_folder() + 
				", Backend tracking: " + config.storage_backend_tracking());
	}

	@Deactivate
	void deactivate() {
		// Close Lucene helper
		if (luceneHelper != null) {
			try {
				luceneHelper.close();
				LOGGER.info("Closed Lucene helper for shared registry");
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Error closing Lucene helper", e);
			}
		}

		// Clear cache
		metadataCache.clear();

		// Log final statistics
		LOGGER.info("Deactivated LuceneEObjectRegistryService - Total updates: " + totalUpdates + 
				", Total removals: " + totalRemovals);
	}

	@Override
	public Optional<ObjectMetadata> getMetadata(String objectId) {
		requireNonNull(objectId, "Object ID cannot be null");

		// Fast O(1) cache lookup
		ObjectMetadata cached = metadataCache.get(objectId);
		if (cached != null) {
			logDebug("Cache hit for object: " + objectId);
			return Optional.of(cached);
		}

		logDebug("Cache miss for object: " + objectId);
		return Optional.empty();
	}

	@Override
	public void updateCache(ObjectMetadata metadata) {
		requireNonNull(metadata, "Metadata cannot be null");
		requireNonNull(metadata.getObjectId(), "ObjectId in metadata cannot be null");

		String objectId = metadata.getObjectId();
		if (objectId.isEmpty()) {
			throw new IllegalArgumentException("ObjectMetadata must have objectId set (cannot be empty)");
		}

		try {
			// Add storage backend tracking if enabled
			if (config.storage_backend_tracking()) {
				enhanceMetadataWithStorageBackend(metadata);
			}

			// Update in-memory cache using objectId as key
			metadataCache.put(objectId, metadata);
			totalUpdates++;

			// Update Lucene index
			try {
				luceneHelper.updateIndex(objectId, metadata);
				logDebug("Updated Lucene index for object: " + objectId);
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Failed to update Lucene index for object: " + objectId, e);
			}

			logDebug("Updated cache for object: " + objectId + " (total updates: " + totalUpdates + ")");

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error updating shared registry cache for object: " + objectId, e);
			throw new RuntimeException("Failed to update shared registry", e);
		}
	}

	@Override
	public void removeFromCache(String objectId) {
		requireNonNull(objectId, "Object ID cannot be null");

		try {
			// Remove from in-memory cache
			ObjectMetadata removed = metadataCache.remove(objectId);
			if (removed != null) {
				totalRemovals++;
			}

			// Remove from Lucene index
			try {
				luceneHelper.removeFromIndex(objectId);
				logDebug("Removed from Lucene index: " + objectId);
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Failed to remove from Lucene index: " + objectId, e);
			}

			logDebug("Removed from cache: " + objectId + " (total removals: " + totalRemovals + ")");

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error removing from shared registry cache: " + objectId, e);
			throw new RuntimeException("Failed to remove from shared registry", e);
		}
	}

	@Override
	public List<ObjectMetadata> findByStatus(ObjectStatus status) {
		requireNonNull(status, "Status cannot be null");

		try {
			// Use Lucene for efficient status-based search
			String query = "status:" + status.getLiteral();
			List<String> objectIds = luceneHelper.searchObjectIds(query, Integer.MAX_VALUE);
			List<ObjectMetadata> luceneResults = loadMetadataList(objectIds);

			// If Lucene returns results or cache is empty, use Lucene results
			if (!luceneResults.isEmpty() || metadataCache.isEmpty()) {
				return luceneResults;
			}

			// If Lucene returns empty but cache has objects, fall back to cache scan
			logDebug("Lucene search returned empty results, falling back to cache scan for status: " + status);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error in Lucene search by status: " + status, e);
			// Fall back to cache scan
		}

		// Fallback to in-memory cache scan
		return metadataCache.values().stream()
				.filter(metadata -> status.equals(metadata.getStatus()))
				.toList();
	}

	public List<ObjectMetadata> getAllMetadata() {
		return List.copyOf(metadataCache.values());
	}

	// === Enhanced Cross-Storage Query Methods ===

	/**
	 * Find objects by storage backend type (e.g., "file", "minio").
	 * Note: This method searches in custom properties for backward compatibility.
	 * 
	 * @param backend the storage backend to search for
	 * @return list of metadata for objects in the specified backend
	 */
	public List<ObjectMetadata> findByStorageBackend(String backend) {
		requireNonNull(backend, "Storage backend cannot be null");

		try {
			// Search in properties for storage.backend key
			String query = "properties:storage.backend\\:" + backend;
			List<String> objectIds = luceneHelper.searchObjectIds(query, Integer.MAX_VALUE);
			return loadMetadataList(objectIds);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error in Lucene search by storage backend: " + backend, e);
		}
		// Fallback to cache scan
		return metadataCache.values().stream()
				.filter(metadata -> backend.equals(extractStorageBackend(metadata)))
				.toList();
	}

	/**
	 * Find objects by storage role (e.g., "draft", "approved", "documentation").
	 * Uses the ObjectMetadata role field directly.
	 * 
	 * @param role the storage role to search for
	 * @return list of metadata for objects with the specified role
	 */
	public List<ObjectMetadata> findByStorageRole(String role) {
		requireNonNull(role, "Storage role cannot be null");

		// Delegate to the standard findByRole method which uses the ObjectMetadata role field
		return findByRole(role);
	}

	/**
	 * Find objects by both storage backend and role.
	 * 
	 * @param backend the storage backend
	 * @param role the storage role
	 * @return list of metadata for objects matching both criteria
	 */
	public List<ObjectMetadata> findByStorageBackendAndRole(String backend, String role) {
		requireNonNull(backend, "Storage backend cannot be null");
		requireNonNull(role, "Storage role cannot be null");

		try {
			String query = "(properties:storage.backend\\:" + backend + 
					" AND " + LuceneRegistryHelper.FIELD_ROLE + ":" + role + ")";
			List<String> objectIds = luceneHelper.searchObjectIds(query, Integer.MAX_VALUE);
			return loadMetadataList(objectIds);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error in Lucene search by backend and role: " + backend + ", " + role, e);
		}
		// Fallback to cache scan
		return metadataCache.values().stream()
				.filter(metadata -> backend.equals(extractStorageBackend(metadata)) &&
						role.equals(metadata.getRole()))
				.toList();
	}

	/**
	 * Find all draft objects across all storage backends.
	 * 
	 * @return list of all draft objects
	 */
	public List<ObjectMetadata> findAllDrafts() {
		return findByRole("draft");
	}

	/**
	 * Find all approved objects across all storage backends.
	 * 
	 * @return list of all approved objects
	 */
	public List<ObjectMetadata> findAllApproved() {
		return findByRole("approved");
	}

	/**
	 * Find all documentation objects across all storage backends.
	 * 
	 * @return list of all documentation objects
	 */
	public List<ObjectMetadata> findAllDocumentation() {
		return findByRole("documentation");
	}

	/**
	 * Get a summary of objects by storage backend and role.
	 * 
	 * @return map with storage identifiers as keys and object counts as values
	 */
	public Map<String, Long> getStorageDistribution() {
		return metadataCache.values().stream()
				.collect(java.util.stream.Collectors.groupingBy(
						metadata -> createStorageIdentifier(metadata),
						java.util.stream.Collectors.counting()
						));
	}

	@Override
	public Promise<Map<String, Object>> getRegistryStatistics() {
		return promiseFactory.submit(() -> {
			Map<String, Object> stats = new ConcurrentHashMap<>();

			// Basic statistics  
			stats.put("totalObjects", (long) metadataCache.size());
			stats.put("totalUpdates", totalUpdates);
			stats.put("totalRemovals", totalRemovals);
			stats.put("lastStatsReset", lastStatsReset);
			stats.put("registryType", "shared");
			stats.put("storageBackendTracking", config.storage_backend_tracking());
			stats.put("registryWorkspace", config.registry_workspace_folder());

			// Status distribution
			Map<String, Long> statusCounts = metadataCache.values().stream()
					.collect(java.util.stream.Collectors.groupingBy(
							metadata -> metadata.getStatus() != null ? metadata.getStatus().getLiteral() : "unknown",
									java.util.stream.Collectors.counting()
							));
			stats.put("statusDistribution", statusCounts);

			// Storage backend distribution (if tracking enabled)
			if (config.storage_backend_tracking()) {
				Map<String, Long> backendCounts = metadataCache.values().stream()
						.collect(java.util.stream.Collectors.groupingBy(
								metadata -> extractStorageBackend(metadata),
								java.util.stream.Collectors.counting()
								));
				stats.put("storageBackendDistribution", backendCounts);
			}

			// Lucene-specific statistics
			{
				try {
					// Since getIndexStatistics() doesn't exist, provide basic index info
					Map<String, Object> luceneStats = new ConcurrentHashMap<>();
					luceneStats.put("indexEnabled", true);
					luceneStats.put("indexPath", luceneHelper.toString());
					stats.put("luceneStatistics", luceneStats);
				} catch (Exception e) {
					stats.put("luceneStatistics", "Error retrieving Lucene statistics: " + e.getMessage());
				}
			}

			return stats;
		});
	}

	// === Helper Methods ===

	private void initializeLuceneHelper(Path registryPath) {
		try {
			// Create LuceneRegistryHelper with proper constructor
			this.luceneHelper = new LuceneRegistryHelper(registryPath);
			this.luceneHelper.initialize();

			LOGGER.info("Initialized Lucene helper for shared registry at: " + registryPath);

		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Failed to initialize Lucene helper for shared registry", e);
			throw new RuntimeException("Cannot initialize shared registry Lucene support", e);
		}
	}

	private void enhanceMetadataWithStorageBackend(ObjectMetadata metadata) {
		// Check if storage backend information is already present
		String currentBackend = extractStorageBackend(metadata);
		if ("unknown".equals(currentBackend)) {
			// Could be enhanced in the future to auto-detect based on calling context
			// For now, we rely on storage services to explicitly set this information
			logDebug("Storage backend tracking enabled but metadata lacks storage.backend property");
		} else {
			logDebug("Metadata already contains storage backend: " + currentBackend);
		}
	}

	/**
	 * Extract storage backend from metadata properties.
	 * 
	 * @param metadata the object metadata
	 * @return the storage backend or "unknown" if not found
	 */
	private String extractStorageBackend(ObjectMetadata metadata) {
		if (metadata.getProperties() != null) {
			Object backend = metadata.getProperties().get("storage.backend");
			return backend != null ? backend.toString() : "unknown";
		}
		return "unknown";
	}

	/**
	 * Create a storage identifier string for the given metadata.
	 * 
	 * @param metadata the object metadata
	 * @return storage identifier in format "backend:role" or just "role" if no backend
	 */
	private String createStorageIdentifier(ObjectMetadata metadata) {
		String backend = extractStorageBackend(metadata);
		String role = metadata.getRole() != null ? metadata.getRole() : "unknown";

		if ("unknown".equals(backend)) {
			return role;
		} else {
			return backend + ":" + role;
		}
	}

	private List<ObjectMetadata> loadMetadataList(List<String> objectIds) {
		return objectIds.stream()
				.map(metadataCache::get)
				.filter(metadata -> metadata != null)
				.toList();
	}

	// New interface methods for objectName and role queries

	@Override
	public List<ObjectMetadata> findByObjectName(String objectName) {
		requireNonNull(objectName, "Object name cannot be null");

		try {
			// Use Lucene for efficient objectName search
			String query = LuceneRegistryHelper.FIELD_OBJECT_NAME + ":\"" + objectName + "\"";
			List<String> objectIds = luceneHelper.searchObjectIds(query, Integer.MAX_VALUE);
			List<ObjectMetadata> luceneResults = loadMetadataList(objectIds);

			// If Lucene returns results or cache is empty, use Lucene results
			if (!luceneResults.isEmpty() || metadataCache.isEmpty()) {
				return luceneResults;
			}

			// If Lucene returns empty but cache has objects, fall back to cache scan
			logDebug("Lucene search returned empty results, falling back to cache scan for objectName: " + objectName);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error in Lucene search by objectName: " + objectName, e);
			// Fall back to cache scan
		}
		// Fallback to in-memory cache scan
		return metadataCache.values().stream()
				.filter(metadata -> objectName.equals(metadata.getObjectName()))
				.toList();
	}

	@Override
	public Optional<ObjectMetadata> findByObjectNameAndRole(String objectName, String role) {
		requireNonNull(objectName, "Object name cannot be null");
		requireNonNull(role, "Role cannot be null");

		try {
			// Use Lucene for efficient objectName and role search
			String query = "(" + LuceneRegistryHelper.FIELD_OBJECT_NAME + ":\"" + objectName + 
					"\" AND " + LuceneRegistryHelper.FIELD_ROLE + ":" + role + ")";
			List<String> objectIds = luceneHelper.searchObjectIds(query, 1);
			if (!objectIds.isEmpty()) {
				ObjectMetadata metadata = metadataCache.get(objectIds.get(0));
				if (metadata != null) {
					return Optional.of(metadata);
				}
			}

			// If Lucene returns empty and cache has objects, fall back to cache scan
			if (objectIds.isEmpty() && !metadataCache.isEmpty()) {
				logDebug("Lucene search returned empty results, falling back to cache scan for objectName: " + objectName + ", role: " + role);
			} else if (objectIds.isEmpty()) {
				return Optional.empty();
			}
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error in Lucene search by objectName and role: " + objectName + ", " + role, e);
			// Fall back to cache scan
		}
		// Fallback to in-memory cache scan
		return metadataCache.values().stream()
				.filter(metadata -> objectName.equals(metadata.getObjectName()) && role.equals(metadata.getRole()))
				.findFirst();

	}

	@Override
	public List<ObjectMetadata> findByRole(String role) {
		requireNonNull(role, "Role cannot be null");

		try {
			// Use Lucene for efficient role-based search
			String query = LuceneRegistryHelper.FIELD_ROLE + ":" + role;
			List<String> objectIds = luceneHelper.searchObjectIds(query, Integer.MAX_VALUE);
			List<ObjectMetadata> luceneResults = loadMetadataList(objectIds);

			// If Lucene returns results or cache is empty, use Lucene results
			if (!luceneResults.isEmpty() || metadataCache.isEmpty()) {
				return luceneResults;
			}

			// If Lucene returns empty but cache has objects, fall back to cache scan
			logDebug("Lucene search returned empty results, falling back to cache scan for role: " + role);
		} catch (Exception e) {
			LOGGER.log(Level.SEVERE, "Error in Lucene search by role: " + role, e);
			// Fall back to cache scan
		}
		// Fallback to in-memory cache scan
		return metadataCache.values().stream()
				.filter(metadata -> role.equals(metadata.getRole()))
				.toList();
	}

	// Existing interface methods

	@Override
	public List<ObjectMetadata> findPendingApproval() {
		return findByStatus(ObjectStatus.DRAFT);
	}

	@Override
	public List<ObjectMetadata> findByVersion(String version) {
		requireNonNull(version, "Version cannot be null");
		
		// Simple implementation - can be enhanced with Lucene later
		return metadataCache.values().stream()
				.filter(metadata -> version.equals(metadata.getVersion()))
				.toList();
	}

	@Override
	public List<ObjectMetadata> findByVersionPattern(String versionPattern) {
		requireNonNull(versionPattern, "Version pattern cannot be null");
		
		try {
			// Simple pattern matching - can be enhanced later
			String regex = versionPattern.replace("*", ".*");
			return metadataCache.values().stream()
					.filter(metadata -> metadata.getVersion() != null && metadata.getVersion().matches(regex))
					.toList();
		} catch (Exception e) {
			// Handle malformed regex patterns gracefully
			LOGGER.log(Level.WARNING, "Invalid version pattern: " + versionPattern, e);
			return List.of();
		}
	}

	@Override
	public Optional<ObjectMetadata> findByFingerprint(String fingerprint) {
		requireNonNull(fingerprint, "Fingerprint cannot be null");
		
		return metadataCache.values().stream()
				.filter(metadata -> fingerprint.equals(metadata.getGenerationTriggerFingerprint()))
				.findFirst();
	}

	@Override
	public List<ObjectMetadata> findByObjectType(String objectType) {
		requireNonNull(objectType, "Object type cannot be null");
		
		return metadataCache.values().stream()
				.filter(metadata -> objectType.equals(metadata.getObjectType()))
				.toList();
	}

	@Override
	public List<ObjectMetadata> findByStatusAndType(ObjectStatus status, String objectType) {
		requireNonNull(status, "Status cannot be null");
		requireNonNull(objectType, "Object type cannot be null");
		
		return metadataCache.values().stream()
				.filter(metadata -> status.equals(metadata.getStatus()) && objectType.equals(metadata.getObjectType()))
				.toList();
	}

	@Override
	public List<ObjectMetadata> findRecentlyModified(Instant sinceTime, int maxResults) {
		requireNonNull(sinceTime, "Since time cannot be null");
		
		return metadataCache.values().stream()
				.filter(metadata -> metadata.getLastChangeTime() != null && metadata.getLastChangeTime().isAfter(sinceTime))
				.sorted((a, b) -> b.getLastChangeTime().compareTo(a.getLastChangeTime()))
				.limit(maxResults)
				.toList();
	}

	private void logDebug(String message) {
		if (config.enable_debug_logging()) {
			LOGGER.fine(message);
		}
	}
}