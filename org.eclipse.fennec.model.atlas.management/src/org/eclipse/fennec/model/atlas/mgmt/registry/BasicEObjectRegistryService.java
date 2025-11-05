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

import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;
import org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * Basic implementation of EObjectRegistryService that works with any storage backend.
 * 
 * <p>This implementation provides registry functionality using standard storage helper
 * operations. It performs O(n) operations by scanning through all objects to find matches,
 * making it suitable for smaller datasets or when advanced indexing is not available.</p>
 * 
 * <h3>Features</h3>
 * <ul>
 * <li><strong>In-Memory Cache</strong> - All metadata cached in memory for O(1) access</li>
 * <li><strong>Multiple Indexes</strong> - Pre-computed indexes for status, type, version, and channel</li>
 * <li><strong>Generic Backend Support</strong> - Works with any AbstractStorageHelper implementation</li>
 * <li><strong>Complete Registry API</strong> - Implements all 12 EObjectRegistryService methods</li>
 * <li><strong>Thread Safety</strong> - ReadWriteLock ensures safe concurrent access</li>
 * <li><strong>Promise-based Operations</strong> - Async operations with proper error handling</li>
 * <li><strong>Auto-Initialization</strong> - Loads existing objects from storage on startup</li>
 * <li><strong>Cache Synchronization</strong> - Automatically updates when storage changes</li>
 * <li><strong>Pattern Matching</strong> - Supports wildcard version searches</li>
 * <li><strong>Real-time Statistics</strong> - Pre-computed statistics from cache indexes</li>
 * </ul>
 * 
 * <h3>Performance Characteristics</h3>
 * <ul>
 * <li><strong>getMetadata()</strong> - O(1) - direct HashMap lookup</li>
 * <li><strong>findByStatus()</strong> - O(k) - where k is the number of objects with that status</li>
 * <li><strong>findByObjectType()</strong> - O(k) - where k is the number of objects of that type</li>
 * <li><strong>updateCache()</strong> - O(1) - updates in-memory indexes</li>
 * <li><strong>getRegistryStatistics()</strong> - O(1) - uses pre-computed statistics</li>
 * </ul>
 * 
 * <h3>In-Memory Indexes</h3>
 * <p>This implementation maintains several concurrent maps for fast lookups:</p>
 * <ul>
 * <li><strong>Primary Index</strong> - objectId → ObjectMetadata</li>
 * <li><strong>Status Index</strong> - ObjectStatus → Set&lt;objectId&gt;</li>
 * <li><strong>Type Index</strong> - objectType → Set&lt;objectId&gt;</li>
 * <li><strong>Version Index</strong> - version → Set&lt;objectId&gt;</li>
 * <li><strong>Channel Index</strong> - sourceChannel → Set&lt;objectId&gt;</li>
 * </ul>
 * 
 * <h3>Usage</h3>
 * <p>This class is typically instantiated by storage services that want to provide
 * registry capability. It should not be registered as an OSGi component directly,
 * but rather created and registered by storage services.</p>
 * 
 * <pre>{@code
 * // Created by storage service
 * public class SomeStorageService extends AbstractEObjectStorageService {
 *     
 *     @Override
 *     protected EObjectRegistryService<EObject> createEObjectRegistry() throws Exception {
 *         BasicEObjectRegistryService<EObject> registry = 
 *             new BasicEObjectRegistryService<>(storageHelper, promiseFactory);
 *         return registry;
 *     }
 *     
 *     // Registry cache is automatically synchronized with storage operations
 *     @Override
 *     public Promise<String> storeObject(String objectId, EObject object, ObjectMetadata metadata) {
 *         Promise<String> result = super.storeObject(objectId, object, metadata);
 *         // Registry cache is updated via updateCache() call in AbstractEObjectStorageService
 *         return result;
 *     }
 * }
 * }</pre>
 * 
 * <h3>Cache Synchronization</h3>
 * <p>The registry automatically synchronizes with storage operations:</p>
 * <ul>
 * <li><strong>Initialization</strong> - Loads all existing metadata on startup</li>
 * <li><strong>Store Operations</strong> - updateCache() called by storage service</li>
 * <li><strong>Update Operations</strong> - Cache updated when metadata changes</li>
 * <li><strong>Delete Operations</strong> - removeFromCache() called by storage service</li>
 * </ul>
 * 
 * @param <T> the type of EObject this registry manages
 * @author Mark Hoffmann
 * @since 1.0.0
 * @see EObjectRegistryService
 * @see AbstractStorageHelper
 */
public class BasicEObjectRegistryService<T extends EObject> implements EObjectRegistryService<T> {

    private static final Logger LOGGER = Logger.getLogger(BasicEObjectRegistryService.class.getName());

    /**
     * Promise factory for creating async operations
     */
    protected final PromiseFactory promiseFactory;

    /**
     * Storage helper for accessing object metadata
     */
    protected final AbstractStorageHelper storageHelper;
    
    // In-memory indexes for fast lookups
    private final Map<String, ObjectMetadata> metadataById = new ConcurrentHashMap<>();
    private final Map<ObjectStatus, Set<String>> objectIdsByStatus = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> objectIdsByType = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> objectIdsByVersion = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> objectIdsByChannel = new ConcurrentHashMap<>();
    private final Map<String, String> objectIdsByFingerprint = new ConcurrentHashMap<>();
    
    // Thread safety for index updates
    private final ReadWriteLock cacheLock = new ReentrantReadWriteLock();
    
    // Cache state tracking
    private volatile boolean cacheInitialized = false;
    private volatile long lastCacheUpdate = 0L;

    /**
     * Creates a new basic registry service.
     * 
     * @param storageHelper the storage helper to use for accessing metadata, must not be null
     * @param promiseFactory the promise factory for async operations, must not be null
     */
    public BasicEObjectRegistryService(AbstractStorageHelper storageHelper, PromiseFactory promiseFactory) {
        this.storageHelper = requireNonNull(storageHelper, "Storage helper must not be null");
        this.promiseFactory = requireNonNull(promiseFactory, "Promise factory must not be null");
        LOGGER.info("BasicEObjectRegistryService created for backend: " + storageHelper.getClass().getSimpleName());
        
        // Initialize cache asynchronously
        initializeCache();
    }

    @Override
    public Optional<ObjectMetadata> getMetadata(String objectId) {
        requireNonNull(objectId, "Object ID must not be null");
        
        cacheLock.readLock().lock();
        try {
            ensureCacheInitialized();
            return Optional.ofNullable(metadataById.get(objectId));
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    @Override
    public List<ObjectMetadata> findByStatus(ObjectStatus status) {
        requireNonNull(status, "Status must not be null");
        
        cacheLock.readLock().lock();
        try {
            ensureCacheInitialized();
            Set<String> objectIds = objectIdsByStatus.getOrDefault(status, Collections.emptySet());
            return objectIds.stream()
                .map(metadataById::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    @Override
    public Promise<Map<String, Object>> getRegistryStatistics() {
        return promiseFactory.submit(() -> {
            cacheLock.readLock().lock();
            try {
                ensureCacheInitialized();
                
                Map<String, Object> statistics = new HashMap<>();
                
                // Basic counts from cache
                statistics.put("totalObjects", (long) metadataById.size());
                
                // Status distribution (pre-computed from indexes)
                Map<String, Long> statusCounts = new HashMap<>();
                for (Map.Entry<ObjectStatus, Set<String>> entry : objectIdsByStatus.entrySet()) {
                    statusCounts.put(entry.getKey().toString(), (long) entry.getValue().size());
                }
                
                // Type distribution (pre-computed from indexes)
                Map<String, Long> objectTypeCounts = new HashMap<>();
                for (Map.Entry<String, Set<String>> entry : objectIdsByType.entrySet()) {
                    objectTypeCounts.put(entry.getKey(), (long) entry.getValue().size());
                }
                
                // Channel distribution (pre-computed from indexes)
                Map<String, Long> sourceChannelCounts = new HashMap<>();
                for (Map.Entry<String, Set<String>> entry : objectIdsByChannel.entrySet()) {
                    sourceChannelCounts.put(entry.getKey(), (long) entry.getValue().size());
                }
                
                statistics.put("statusCounts", statusCounts);
                statistics.put("objectTypeCounts", objectTypeCounts);
                statistics.put("sourceChannelCounts", sourceChannelCounts);
                
                // Cache metadata
                statistics.put("cacheInitialized", cacheInitialized);
                statistics.put("lastCacheUpdate", lastCacheUpdate);
                statistics.put("generatedAt", Instant.now().toString());
                statistics.put("registryType", "basic-memory");
                
                LOGGER.fine("Generated registry statistics from cache: " + metadataById.size() + " objects");
                
                return statistics;
                
            } finally {
                cacheLock.readLock().unlock();
            }
        });
    }

    /**
     * Returns the storage helper used by this registry.
     * 
     * <p>This method is provided for subclasses that may need access to the underlying
     * storage helper for enhanced operations.</p>
     * 
     * @return the storage helper instance
     */
    protected AbstractStorageHelper getStorageHelper() {
        return storageHelper;
    }

    /**
     * Returns the promise factory used by this registry.
     * 
     * <p>This method is provided for subclasses that may need to create additional
     * async operations.</p>
     * 
     * @return the promise factory instance
     */
    protected PromiseFactory getPromiseFactory() {
        return promiseFactory;
    }

    // ===== Additional Registry Methods Implementation =====
    
    @Override
    public List<ObjectMetadata> findPendingApproval() {
        return findByStatus(ObjectStatus.DRAFT);
    }
    
    @Override
    public List<ObjectMetadata> findByVersion(String version) {
        requireNonNull(version, "Version must not be null");
        
        cacheLock.readLock().lock();
        try {
            ensureCacheInitialized();
            Set<String> objectIds = objectIdsByVersion.getOrDefault(version, Collections.emptySet());
            return objectIds.stream()
                .map(metadataById::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        } finally {
            cacheLock.readLock().unlock();
        }
    }
    
    @Override
    public List<ObjectMetadata> findByVersionPattern(String versionPattern) {
        requireNonNull(versionPattern, "Version pattern must not be null");
        
        cacheLock.readLock().lock();
        try {
            ensureCacheInitialized();
            // Convert wildcard pattern to regex
            String regex = versionPattern.replace("*", ".*").replace("?", ".");
            Pattern pattern = Pattern.compile(regex);
            
            return objectIdsByVersion.entrySet().stream()
                .filter(entry -> pattern.matcher(entry.getKey()).matches())
                .flatMap(entry -> entry.getValue().stream())
                .map(metadataById::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        } finally {
            cacheLock.readLock().unlock();
        }
    }
    
    @Override
    public Optional<ObjectMetadata> findByFingerprint(String fingerprint) {
        requireNonNull(fingerprint, "Fingerprint must not be null");
        
        cacheLock.readLock().lock();
        try {
            ensureCacheInitialized();
            String objectId = objectIdsByFingerprint.get(fingerprint);
            if (objectId != null) {
                return Optional.ofNullable(metadataById.get(objectId));
            }
            return Optional.empty();
        } finally {
            cacheLock.readLock().unlock();
        }
    }
    
    @Override
    public List<ObjectMetadata> findByObjectType(String objectType) {
        requireNonNull(objectType, "Object type must not be null");
        
        cacheLock.readLock().lock();
        try {
            ensureCacheInitialized();
            Set<String> objectIds = objectIdsByType.getOrDefault(objectType, Collections.emptySet());
            return objectIds.stream()
                .map(metadataById::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        } finally {
            cacheLock.readLock().unlock();
        }
    }
    
    @Override
    public List<ObjectMetadata> findByStatusAndType(ObjectStatus status, String objectType) {
        requireNonNull(status, "Status must not be null");
        requireNonNull(objectType, "Object type must not be null");
        
        cacheLock.readLock().lock();
        try {
            ensureCacheInitialized();
            Set<String> statusIds = objectIdsByStatus.getOrDefault(status, Collections.emptySet());
            Set<String> typeIds = objectIdsByType.getOrDefault(objectType, Collections.emptySet());
            
            // Intersection of both sets
            return statusIds.stream()
                .filter(typeIds::contains)
                .map(metadataById::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        } finally {
            cacheLock.readLock().unlock();
        }
    }
    
    @Override
    public List<ObjectMetadata> findRecentlyModified(Instant sinceTime, int maxResults) {
        requireNonNull(sinceTime, "Since time must not be null");
        
        cacheLock.readLock().lock();
        try {
            ensureCacheInitialized();
            return metadataById.values().stream()
                .filter(metadata -> isRecentlyModified(metadata, sinceTime))
                .sorted(createMostRecentTimeComparator())
                .limit(maxResults > 0 ? maxResults : Integer.MAX_VALUE)
                .collect(Collectors.toList());
        } finally {
            cacheLock.readLock().unlock();
        }
    }
    
    /**
     * Checks if metadata represents an object that was recently modified (uploaded or changed) since the given time.
     * 
     * @param metadata the object metadata to check
     * @param sinceTime the time threshold for "recent" modification
     * @return true if the object was uploaded or last changed after sinceTime
     */
    protected boolean isRecentlyModified(ObjectMetadata metadata, Instant sinceTime) {
        Instant uploadTime = metadata.getUploadTime();
        Instant lastChangeTime = metadata.getLastChangeTime();
        
        // Object is recently modified if either:
        // 1. It was uploaded after sinceTime, OR
        // 2. It was last changed after sinceTime
        boolean uploadedRecently = uploadTime != null && uploadTime.isAfter(sinceTime);
        boolean changedRecently = lastChangeTime != null && lastChangeTime.isAfter(sinceTime);
        
        return uploadedRecently || changedRecently;
    }
    
    /**
     * Creates a comparator that sorts metadata by the most recent time (upload or change) in descending order.
     * 
     * @return comparator for sorting by most recent modification time
     */
    protected Comparator<ObjectMetadata> createMostRecentTimeComparator() {
        return Comparator.comparing((ObjectMetadata metadata) -> getMostRecentTime(metadata), Comparator.reverseOrder());
    }
    
    /**
     * Extracts the most recent time from metadata, considering both upload and last change times.
     * 
     * @param metadata the metadata to analyze
     * @return the most recent time between upload and last change, or Instant.EPOCH if both are null
     */
    protected Instant getMostRecentTime(ObjectMetadata metadata) {
        Instant uploadTime = metadata.getUploadTime();
        Instant lastChangeTime = metadata.getLastChangeTime();
        
        if (lastChangeTime != null && uploadTime != null) {
            return lastChangeTime.isAfter(uploadTime) ? lastChangeTime : uploadTime;
        } else if (lastChangeTime != null) {
            return lastChangeTime;
        } else if (uploadTime != null) {
            return uploadTime;
        } else {
            return Instant.EPOCH; // Fallback for null times
        }
    }
    
    @Override
    public void updateCache(ObjectMetadata metadata) {
        requireNonNull(metadata, "Metadata must not be null");
        requireNonNull(metadata.getObjectId(), "ObjectId in metadata must not be null");
        
        String objectId = metadata.getObjectId();
        if (objectId.isEmpty()) {
            throw new IllegalArgumentException("ObjectMetadata must have objectId set (cannot be empty)");
        }
        
        cacheLock.writeLock().lock();
        try {
            // Remove old metadata from indexes if it exists
            ObjectMetadata oldMetadata = metadataById.get(objectId);
            if (oldMetadata != null) {
                removeFromIndexes(objectId, oldMetadata);
            }
            
            // Add new metadata to cache and indexes  
            metadataById.put(objectId, metadata);
            addToIndexes(objectId, metadata);
            
            lastCacheUpdate = System.currentTimeMillis();
            
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Updated cache for object: " + objectId);
            }
        } finally {
            cacheLock.writeLock().unlock();
        }
    }
    
    @Override
    public void removeFromCache(String objectId) {
        requireNonNull(objectId, "Object ID must not be null");
        
        cacheLock.writeLock().lock();
        try {
            ObjectMetadata metadata = metadataById.remove(objectId);
            if (metadata != null) {
                removeFromIndexes(objectId, metadata);
                lastCacheUpdate = System.currentTimeMillis();
                
                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Removed object from cache: " + objectId);
                }
            }
        } finally {
            cacheLock.writeLock().unlock();
        }
    }
    
    // ===== Cache Management Methods =====
    
    /**
     * Initializes the cache by loading all metadata from storage.
     */
    private void initializeCache() {
        try {
            LOGGER.info("Initializing registry cache from storage backend");
            
            List<String> objectIds = storageHelper.listObjectIds();
            int loadedCount = 0;
            int errorCount = 0;
            
            cacheLock.writeLock().lock();
            try {
                for (String objectId : objectIds) {
                    try {
                        ObjectMetadata metadata = storageHelper.loadMetadata(objectId);
                        if (metadata != null) {
                            // Ensure objectId is set
                            if (metadata.getObjectId() == null || metadata.getObjectId().isEmpty()) {
                                metadata.setObjectId(objectId);
                            }
                            
                            metadataById.put(objectId, metadata);
                            addToIndexes(objectId, metadata);
                            loadedCount++;
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Failed to load metadata during cache initialization: " + objectId, e);
                        errorCount++;
                    }
                }
                
                cacheInitialized = true;
                lastCacheUpdate = System.currentTimeMillis();
            } finally {
                cacheLock.writeLock().unlock();
            }
            
            LOGGER.info("Registry cache initialized: " + loadedCount + " objects loaded, " + errorCount + " errors");
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to initialize registry cache", e);
        }
    }
    
    /**
     * Ensures cache is initialized before performing operations.
     */
    private void ensureCacheInitialized() {
        if (!cacheInitialized) {
            // Upgrade to write lock if needed
            cacheLock.readLock().unlock();
            cacheLock.writeLock().lock();
            try {
                if (!cacheInitialized) {
                    // Re-initialize if still not done
                    initializeCache();
                }
                // Downgrade back to read lock
                cacheLock.readLock().lock();
            } finally {
                cacheLock.writeLock().unlock();
            }
        }
    }
    
    /**
     * Adds metadata to all relevant indexes.
     */
    private void addToIndexes(String objectId, ObjectMetadata metadata) {
        // Status index
        if (metadata.getStatus() != null) {
            objectIdsByStatus.computeIfAbsent(metadata.getStatus(), k -> ConcurrentHashMap.newKeySet()).add(objectId);
        }
        
        // Type index
        if (metadata.getObjectType() != null) {
            objectIdsByType.computeIfAbsent(metadata.getObjectType(), k -> ConcurrentHashMap.newKeySet()).add(objectId);
        }
        
        // Version index
        if (metadata.getVersion() != null) {
            objectIdsByVersion.computeIfAbsent(metadata.getVersion(), k -> ConcurrentHashMap.newKeySet()).add(objectId);
        }
        
        // Channel index
        if (metadata.getSourceChannel() != null) {
            objectIdsByChannel.computeIfAbsent(metadata.getSourceChannel(), k -> ConcurrentHashMap.newKeySet()).add(objectId);
        }
        
        // Fingerprint index
        if (metadata.getGenerationTriggerFingerprint() != null) {
            objectIdsByFingerprint.put(metadata.getGenerationTriggerFingerprint(), objectId);
        }
    }
    
    /**
     * Removes metadata from all relevant indexes.
     */
    private void removeFromIndexes(String objectId, ObjectMetadata metadata) {
        // Status index
        if (metadata.getStatus() != null) {
            Set<String> statusSet = objectIdsByStatus.get(metadata.getStatus());
            if (statusSet != null) {
                statusSet.remove(objectId);
                if (statusSet.isEmpty()) {
                    objectIdsByStatus.remove(metadata.getStatus());
                }
            }
        }
        
        // Type index
        if (metadata.getObjectType() != null) {
            Set<String> typeSet = objectIdsByType.get(metadata.getObjectType());
            if (typeSet != null) {
                typeSet.remove(objectId);
                if (typeSet.isEmpty()) {
                    objectIdsByType.remove(metadata.getObjectType());
                }
            }
        }
        
        // Version index
        if (metadata.getVersion() != null) {
            Set<String> versionSet = objectIdsByVersion.get(metadata.getVersion());
            if (versionSet != null) {
                versionSet.remove(objectId);
                if (versionSet.isEmpty()) {
                    objectIdsByVersion.remove(metadata.getVersion());
                }
            }
        }
        
        // Channel index
        if (metadata.getSourceChannel() != null) {
            Set<String> channelSet = objectIdsByChannel.get(metadata.getSourceChannel());
            if (channelSet != null) {
                channelSet.remove(objectId);
                if (channelSet.isEmpty()) {
                    objectIdsByChannel.remove(metadata.getSourceChannel());
                }
            }
        }
        
        // Fingerprint index
        if (metadata.getGenerationTriggerFingerprint() != null) {
            objectIdsByFingerprint.remove(metadata.getGenerationTriggerFingerprint());
        }
    }
    
    /**
     * Lifecycle method called when the registry is no longer needed.
     * 
     * <p>Subclasses can override this method to perform cleanup operations.
     * The default implementation clears all caches and logs deactivation.</p>
     */
    public void deactivate() {
        cacheLock.writeLock().lock();
        try {
            metadataById.clear();
            objectIdsByStatus.clear();
            objectIdsByType.clear();
            objectIdsByVersion.clear();
            objectIdsByChannel.clear();
            objectIdsByFingerprint.clear();
            cacheInitialized = false;
            
            LOGGER.info("BasicEObjectRegistryService deactivated and cache cleared");
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

	@Override
	public List<ObjectMetadata> findByObjectName(String objectName) {
		requireNonNull(objectName, "Object name must not be null");
		
		cacheLock.readLock().lock();
		try {
			return metadataById.values().stream()
				.filter(metadata -> objectName.equals(metadata.getObjectName()))
				.collect(java.util.stream.Collectors.toList());
		} finally {
			cacheLock.readLock().unlock();
		}
	}

	@Override
	public Optional<ObjectMetadata> findByObjectNameAndRole(String objectName, String role) {
		requireNonNull(objectName, "Object name must not be null");
		requireNonNull(role, "Role must not be null");
		
		cacheLock.readLock().lock();
		try {
			return metadataById.values().stream()
				.filter(metadata -> objectName.equals(metadata.getObjectName()) && role.equals(metadata.getRole()))
				.findFirst();
		} finally {
			cacheLock.readLock().unlock();
		}
	}

	@Override
	public List<ObjectMetadata> findByRole(String role) {
		requireNonNull(role, "Role must not be null");
		
		cacheLock.readLock().lock();
		try {
			return metadataById.values().stream()
				.filter(metadata -> role.equals(metadata.getRole()))
				.collect(java.util.stream.Collectors.toList());
		} finally {
			cacheLock.readLock().unlock();
		}
	}

}