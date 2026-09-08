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

import java.time.Instant;
import java.util.Collection;
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
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;
import org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * Basic implementation of EObjectRegistryService that works with any storage
 * backend.
 * 
 * <p>
 * This implementation provides registry functionality using standard storage
 * helper operations. It performs O(n) operations by scanning through all
 * objects to find matches, making it suitable for smaller datasets or when
 * advanced indexing is not available.
 * </p>
 * 
 * <h3>Features</h3>
 * <ul>
 * <li><strong>In-Memory Cache</strong> - All metadata cached in memory for O(1)
 * access</li>
 * <li><strong>Multiple Indexes</strong> - Pre-computed indexes for status,
 * type, version, and channel</li>
 * <li><strong>Generic Backend Support</strong> - Works with any
 * AbstractStorageHelper implementation</li>
 * <li><strong>Complete Registry API</strong> - Implements all 12
 * EObjectRegistryService methods</li>
 * <li><strong>Thread Safety</strong> - ReadWriteLock ensures safe concurrent
 * access</li>
 * <li><strong>Promise-based Operations</strong> - Async operations with proper
 * error handling</li>
 * <li><strong>Auto-Initialization</strong> - Loads existing objects from
 * storage on startup</li>
 * <li><strong>Cache Synchronization</strong> - Automatically updates when
 * storage changes</li>
 * <li><strong>Pattern Matching</strong> - Supports wildcard version
 * searches</li>
 * <li><strong>Real-time Statistics</strong> - Pre-computed statistics from
 * cache indexes</li>
 * </ul>
 * 
 * <h3>Performance Characteristics</h3>
 * <ul>
 * <li><strong>getMetadata()</strong> - O(1) - direct HashMap lookup</li>
 * <li><strong>findByStatus()</strong> - O(k) - where k is the number of objects
 * with that status</li>
 * <li><strong>findByObjectType()</strong> - O(k) - where k is the number of
 * objects of that type</li>
 * <li><strong>updateCache()</strong> - O(1) - updates in-memory indexes</li>
 * <li><strong>getRegistryStatistics()</strong> - O(1) - uses pre-computed
 * statistics</li>
 * </ul>
 * 
 * <h3>In-Memory Indexes</h3>
 * <p>
 * This implementation maintains several concurrent maps for fast lookups:
 * </p>
 * <ul>
 * <li><strong>Primary Index</strong> - objectId → ObjectMetadata</li>
 * <li><strong>Status Index</strong> - ObjectStatus → Set&lt;objectId&gt;</li>
 * <li><strong>Type Index</strong> - objectType → Set&lt;objectId&gt;</li>
 * <li><strong>Version Index</strong> - version → Set&lt;objectId&gt;</li>
 * <li><strong>Channel Index</strong> - sourceChannel → Set&lt;objectId&gt;</li>
 * </ul>
 * 
 * <h3>Usage</h3>
 * <p>
 * This class is typically instantiated by storage services that want to provide
 * registry capability. It should not be registered as an OSGi component
 * directly, but rather created and registered by storage services.
 * </p>
 * 
 * <pre>
 * {
 *     &#64;code
 * // Created by storage service
 *     public class SomeStorageService extends AbstractEObjectStorageService {
 * 
 *         &#64;Override
 *         protected EObjectRegistryService<EObject> createEObjectRegistry() throws Exception {
 *             BasicEObjectRegistryService<EObject> registry = new BasicEObjectRegistryService<>(storageHelper,
 *                     promiseFactory);
 *             return registry;
 *         }
 * 
 *         // Registry cache is automatically synchronized with storage operations
 *         @Override
 *         public Promise<String> storeObject(String objectId, EObject object, ObjectMetadata metadata) {
 *             Promise<String> result = super.storeObject(objectId, object, metadata);
 *             // Registry cache is updated via updateCache() call in
 *             // AbstractEObjectStorageService
 *             return result;
 *         }
 *     }
 * }
 * </pre>
 * 
 * <h3>Cache Synchronization</h3>
 * <p>
 * The registry automatically synchronizes with storage operations:
 * </p>
 * <ul>
 * <li><strong>Initialization</strong> - Loads all existing metadata on
 * startup</li>
 * <li><strong>Store Operations</strong> - updateCache() called by storage
 * service</li>
 * <li><strong>Update Operations</strong> - Cache updated when metadata
 * changes</li>
 * <li><strong>Delete Operations</strong> - removeFromCache() called by storage
 * service</li>
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

    // In-memory indexes for fast lookups. Entries are addressed by
    // RegistryAddress, not by objectId: the same object id legitimately lives in
    // two stages of one registry at once - a transition copies unless the registry
    // sets delete.after.transition=true, and a new draft revision of a released
    // model re-uploads the same id (issue #211) - so indexes keyed by object id
    // alone would keep only the copy written last (issue #252).
    private final Map<String, Map<RegistryAddress, ObjectMetadata>> copiesById = new ConcurrentHashMap<>();
    private final Map<ObjectStatus, Set<RegistryAddress>> addressesByStatus = new ConcurrentHashMap<>();
    private final Map<String, Set<RegistryAddress>> addressesByType = new ConcurrentHashMap<>();
    private final Map<String, Set<RegistryAddress>> addressesByVersion = new ConcurrentHashMap<>();
    private final Map<String, Set<RegistryAddress>> addressesByChannel = new ConcurrentHashMap<>();
    private final Map<String, RegistryAddress> addressByGenerationTriggerFingerprint = new ConcurrentHashMap<>();
    private final Map<String, Set<RegistryAddress>> addressesByFingerprint = new ConcurrentHashMap<>();

    // Thread safety for index updates
    private final ReadWriteLock cacheLock = new ReentrantReadWriteLock();

    // Cache state tracking
    private volatile boolean cacheInitialized = false;
    private volatile long lastCacheUpdate = 0L;

    /**
     * Creates a new basic registry service.
     * 
     * @param storageHelper  the storage helper to use for accessing metadata, must
     *                       not be null
     * @param promiseFactory the promise factory for async operations, must not be
     *                       null
     */
    public BasicEObjectRegistryService(AbstractStorageHelper storageHelper, PromiseFactory promiseFactory) {
        this.storageHelper = requireNonNull(storageHelper, "Storage helper must not be null");
        this.promiseFactory = requireNonNull(promiseFactory, "Promise factory must not be null");
        LOGGER.info("BasicEObjectRegistryService created for backend: " + storageHelper.getClass().getSimpleName());

        // Kick off cache initialization asynchronously so constructing the service
        // (which happens on the DS activation path of storage services) never
        // blocks on storage I/O. Readers fall back to lazy initialization via
        // ensureCacheInitialized() if it has not completed (or failed) yet.
        promiseFactory.submit(() -> {
            initializeCache();
            return null;
        });
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * An object id does not identify a single object across stages: when two
     * stages hold the same id, this returns an arbitrary one of the copies.
     * Callers that know the location must use
     * {@link #getMetadata(String, String, String, String)}.
     * </p>
     */
    @Override
    public Optional<ObjectMetadata> getMetadata(String objectId) {
        requireNonNull(objectId, "Object ID must not be null");

        cacheLock.readLock().lock();
        try {
            ensureCacheInitialized();
            return copiesOf(objectId).values().stream().findFirst();
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    @Override
    public Optional<ObjectMetadata> getMetadata(String scope, String registry, String stage, String objectId) {
        requireNonNull(objectId, "Object ID must not be null");

        cacheLock.readLock().lock();
        try {
            ensureCacheInitialized();
            return Optional.ofNullable(metadataAt(new RegistryAddress(scope, registry, stage, objectId)));
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
            return resolve(addressesByStatus.getOrDefault(status, Collections.emptySet()));
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
                statistics.put("totalObjects", allMetadata().count());

                // Status distribution (pre-computed from indexes)
                Map<String, Long> statusCounts = new HashMap<>();
                for (Map.Entry<ObjectStatus, Set<RegistryAddress>> entry : addressesByStatus.entrySet()) {
                    statusCounts.put(entry.getKey().toString(), (long) entry.getValue().size());
                }

                // Type distribution (pre-computed from indexes)
                Map<String, Long> objectTypeCounts = new HashMap<>();
                for (Map.Entry<String, Set<RegistryAddress>> entry : addressesByType.entrySet()) {
                    objectTypeCounts.put(entry.getKey(), (long) entry.getValue().size());
                }

                // Channel distribution (pre-computed from indexes)
                Map<String, Long> sourceChannelCounts = new HashMap<>();
                for (Map.Entry<String, Set<RegistryAddress>> entry : addressesByChannel.entrySet()) {
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

                LOGGER.fine("Generated registry statistics from cache: " + allMetadata().count() + " objects");

                return statistics;

            } finally {
                cacheLock.readLock().unlock();
            }
        });
    }

    /**
     * Returns the storage helper used by this registry.
     * 
     * <p>
     * This method is provided for subclasses that may need access to the underlying
     * storage helper for enhanced operations.
     * </p>
     * 
     * @return the storage helper instance
     */
    protected AbstractStorageHelper getStorageHelper() {
        return storageHelper;
    }

    /**
     * Returns the promise factory used by this registry.
     * 
     * <p>
     * This method is provided for subclasses that may need to create additional
     * async operations.
     * </p>
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
            return resolve(addressesByVersion.getOrDefault(version, Collections.emptySet()));
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

            return addressesByVersion.entrySet().stream().filter(entry -> pattern.matcher(entry.getKey()).matches())
                    .flatMap(entry -> entry.getValue().stream()).map(this::metadataAt).filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    @Override
    public List<ObjectMetadata> findByFingerprint(String fingerprint) {
        requireNonNull(fingerprint, "Fingerprint must not be null");

        cacheLock.readLock().lock();
        try {
            ensureCacheInitialized();
            return resolve(addressesByFingerprint.getOrDefault(fingerprint, Collections.emptySet()));
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
            return resolve(addressesByType.getOrDefault(objectType, Collections.emptySet()));
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
            Set<RegistryAddress> statusAddresses = addressesByStatus.getOrDefault(status, Collections.emptySet());
            Set<RegistryAddress> typeAddresses = addressesByType.getOrDefault(objectType, Collections.emptySet());

            // Intersection of both sets
            return resolve(statusAddresses.stream().filter(typeAddresses::contains).collect(Collectors.toList()));
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
            return allMetadata().filter(metadata -> isRecentlyModified(metadata, sinceTime))
                    .sorted(createMostRecentTimeComparator()).limit(maxResults > 0 ? maxResults : Integer.MAX_VALUE)
                    .collect(Collectors.toList());
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    /**
     * Checks if metadata represents an object that was recently modified (uploaded
     * or changed) since the given time.
     * 
     * @param metadata  the object metadata to check
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
     * Creates a comparator that sorts metadata by the most recent time (upload or
     * change) in descending order.
     * 
     * @return comparator for sorting by most recent modification time
     */
    protected Comparator<ObjectMetadata> createMostRecentTimeComparator() {
        return Comparator.comparing((ObjectMetadata metadata) -> getMostRecentTime(metadata),
                Comparator.reverseOrder());
    }

    /**
     * Extracts the most recent time from metadata, considering both upload and last
     * change times.
     * 
     * @param metadata the metadata to analyze
     * @return the most recent time between upload and last change, or Instant.EPOCH
     *         if both are null
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
            cache(metadata);
            lastCacheUpdate = System.currentTimeMillis();

            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Updated cache for object: " + objectId);
            }
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>
     * Only the entry stored at the given address is dropped: were the held copy
     * the one of another stage - the same object id may legitimately be held by
     * two stages of one registry (issue #211) - it stays, rather than
     * disappearing from a stage that still stores it (issue #252).
     * </p>
     */
    @Override
    public void removeFromCache(String scope, String registry, String stage, String objectId) {
        requireNonNull(objectId, "Object ID must not be null");

        RegistryAddress address = new RegistryAddress(scope, registry, stage, objectId);
        cacheLock.writeLock().lock();
        try {
            if (evict(address) != null) {
                lastCacheUpdate = System.currentTimeMillis();

                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Removed object from cache: " + address);
                }
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
            Map<RegistryAddress, ObjectMetadata> copies = copiesById.remove(objectId);
            if (copies != null && !copies.isEmpty()) {
                copies.forEach(this::removeFromIndexes);
                lastCacheUpdate = System.currentTimeMillis();

                if (LOGGER.isLoggable(Level.FINE)) {
                    LOGGER.fine("Removed object from cache: " + objectId + " (" + copies.size() + " copies)");
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

            List<ObjectMetadata> storedMetadata = storageHelper.loadAllMetadata();
            int loadedCount = 0;
            int errorCount = 0;

            cacheLock.writeLock().lock();
            try {
                for (ObjectMetadata metadata : storedMetadata) {
                    try {
                        cache(metadata);
                        loadedCount++;
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING,
                                "Failed to load metadata during cache initialization: " + metadata.getObjectId(), e);
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
     * Returns every copy of the given object id, keyed by the location holding it.
     */
    private Map<RegistryAddress, ObjectMetadata> copiesOf(String objectId) {
        return copiesById.getOrDefault(objectId, Collections.emptyMap());
    }

    /**
     * Returns the metadata stored at one address, or {@code null}.
     */
    private ObjectMetadata metadataAt(RegistryAddress address) {
        return copiesOf(address.objectId()).get(address);
    }

    /** Every cached object, in every location holding it. */
    private Stream<ObjectMetadata> allMetadata() {
        return copiesById.values().stream().flatMap(copies -> copies.values().stream());
    }

    /**
     * Resolves the addresses an index holds to the metadata stored at them.
     */
    private List<ObjectMetadata> resolve(Collection<RegistryAddress> addresses) {
        return addresses.stream().map(this::metadataAt).filter(Objects::nonNull).collect(Collectors.toList());
    }

    /**
     * Caches the metadata at its own address, replacing what that address held.
     * The copies the same object id has in other stages are untouched.
     */
    private void cache(ObjectMetadata metadata) {
        RegistryAddress address = RegistryAddress.of(metadata);
        ObjectMetadata previous = copiesById.computeIfAbsent(address.objectId(), id -> new ConcurrentHashMap<>())
                .put(address, metadata);
        if (previous != null) {
            removeFromIndexes(address, previous);
        }
        addToIndexes(address, metadata);
    }

    /**
     * Drops the copy stored at one address, and the object id itself once its last
     * copy is gone.
     *
     * @return the metadata that was removed, or {@code null} if that address held
     *         nothing
     */
    private ObjectMetadata evict(RegistryAddress address) {
        ObjectMetadata[] removed = new ObjectMetadata[1];
        copiesById.computeIfPresent(address.objectId(), (id, copies) -> {
            removed[0] = copies.remove(address);
            return copies.isEmpty() ? null : copies;
        });
        if (removed[0] != null) {
            removeFromIndexes(address, removed[0]);
        }
        return removed[0];
    }

    /**
     * Adds metadata to all relevant indexes.
     */
    private void addToIndexes(RegistryAddress address, ObjectMetadata metadata) {
        // Status index
        if (metadata.getStatus() != null) {
            addressesByStatus.computeIfAbsent(metadata.getStatus(), k -> ConcurrentHashMap.newKeySet()).add(address);
        }

        // Type index
        if (metadata.getObjectType() != null) {
            addressesByType.computeIfAbsent(metadata.getObjectType(), k -> ConcurrentHashMap.newKeySet()).add(address);
        }

        // Version index
        if (metadata.getVersion() != null) {
            addressesByVersion.computeIfAbsent(metadata.getVersion(), k -> ConcurrentHashMap.newKeySet()).add(address);
        }

        // Channel index
        if (metadata.getSourceChannel() != null) {
            addressesByChannel.computeIfAbsent(metadata.getSourceChannel(), k -> ConcurrentHashMap.newKeySet())
                    .add(address);
        }

        // Generation Fingerprint index
        if (metadata.getGenerationTriggerFingerprint() != null) {
        	addressByGenerationTriggerFingerprint.put(metadata.getGenerationTriggerFingerprint(), address);
        }
        
        // Fingerprint index
        if (metadata.getFingerprint() != null) {
            addressesByFingerprint.computeIfAbsent(metadata.getFingerprint(), k -> ConcurrentHashMap.newKeySet()).add(address);
        }
    }

    /**
     * Removes metadata from all relevant indexes.
     */
    private void removeFromIndexes(RegistryAddress address, ObjectMetadata metadata) {
        // Status index
        if (metadata.getStatus() != null) {
            Set<RegistryAddress> statusSet = addressesByStatus.get(metadata.getStatus());
            if (statusSet != null) {
                statusSet.remove(address);
                if (statusSet.isEmpty()) {
                    addressesByStatus.remove(metadata.getStatus());
                }
            }
        }

        // Type index
        if (metadata.getObjectType() != null) {
            Set<RegistryAddress> typeSet = addressesByType.get(metadata.getObjectType());
            if (typeSet != null) {
                typeSet.remove(address);
                if (typeSet.isEmpty()) {
                    addressesByType.remove(metadata.getObjectType());
                }
            }
        }

        // Version index
        if (metadata.getVersion() != null) {
            Set<RegistryAddress> versionSet = addressesByVersion.get(metadata.getVersion());
            if (versionSet != null) {
                versionSet.remove(address);
                if (versionSet.isEmpty()) {
                    addressesByVersion.remove(metadata.getVersion());
                }
            }
        }

        // Channel index
        if (metadata.getSourceChannel() != null) {
            Set<RegistryAddress> channelSet = addressesByChannel.get(metadata.getSourceChannel());
            if (channelSet != null) {
                channelSet.remove(address);
                if (channelSet.isEmpty()) {
                    addressesByChannel.remove(metadata.getSourceChannel());
                }
            }
        }

        // Generation Fingerprint index
        if (metadata.getGenerationTriggerFingerprint() != null) {
            addressByGenerationTriggerFingerprint.remove(metadata.getGenerationTriggerFingerprint());
        }

        // Fingerprint index
        if (metadata.getFingerprint() != null) {
            Set<RegistryAddress> fingerprintSet = addressesByFingerprint.get(metadata.getFingerprint());
            if (fingerprintSet != null) {
                fingerprintSet.remove(address);
                if (fingerprintSet.isEmpty()) {
                    addressesByFingerprint.remove(metadata.getFingerprint());
                }
            }
        }
    }

    /**
     * Lifecycle method called when the registry is no longer needed.
     * 
     * <p>
     * Subclasses can override this method to perform cleanup operations. The
     * default implementation clears all caches and logs deactivation.
     * </p>
     */
    public void deactivate() {
        cacheLock.writeLock().lock();
        try {
            copiesById.clear();
            addressesByStatus.clear();
            addressesByType.clear();
            addressesByVersion.clear();
            addressesByChannel.clear();
            addressByGenerationTriggerFingerprint.clear();
            addressesByFingerprint.clear();
            cacheInitialized = false;

            LOGGER.info("BasicEObjectRegistryService deactivated and cache cleared");
        } finally {
            cacheLock.writeLock().unlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#
     * findByObjectName(java.lang.String)
     */
    @Override
    public List<ObjectMetadata> findByObjectName(String objectName) {
        requireNonNull(objectName, "Object name must not be null");

        cacheLock.readLock().lock();
        try {
            ensureCacheInitialized();
            return allMetadata().filter(metadata -> objectName.equals(metadata.getObjectName()))
                    .collect(Collectors.toList());
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#
     * findByObjectNameAndStage(java.lang.String, java.lang.String)
     */
    @Override
    public Optional<ObjectMetadata> findByObjectNameAndStage(String objectName, String stage) {
        requireNonNull(objectName, "Object name must not be null");
        requireNonNull(stage, "Stage must not be null");

        cacheLock.readLock().lock();
        try {
            ensureCacheInitialized();
            return allMetadata().filter(
                    metadata -> objectName.equals(metadata.getObjectName()) && stage.equals(metadata.getStage()))
                    .findFirst();
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#
     * findByScopeAndStage(java.lang.String, java.lang.String)
     */
    @Override
    public List<ObjectMetadata> findByScopeAndStage(String scope, String stage) {
        requireNonNull(scope, "Scope must not be null");
        requireNonNull(stage, "Stage must not be null");

        cacheLock.readLock().lock();
        try {
            ensureCacheInitialized();
            return allMetadata()
                    .filter(metadata -> stage.equals(metadata.getStage()) && scope.equals(metadata.getScope()))
                    .collect(Collectors.toList());
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#
     * findByScopeStageAndName(java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public List<ObjectMetadata> findByScopeStageAndName(String scope, String stage, String name) {
        requireNonNull(scope, "Scope must not be null");
        requireNonNull(stage, "Stage must not be null");
        requireNonNull(name, "Name must not be null");

        // name can also contain * for wildcard search
        boolean isExact = !name.contains("*");
        String nameFilter = name.contains("*") ? name.replaceAll("\\*", "") : name;
        cacheLock.readLock().lock();
        try {
            ensureCacheInitialized();
            if (isExact) {
                return allMetadata().filter(metadata -> stage.equals(metadata.getStage())
                                && scope.equals(metadata.getScope()) && name.equals(metadata.getObjectName()))
                        .collect(Collectors.toList());
            } else {
                return allMetadata().filter(metadata -> stage.equals(metadata.getStage())
                                && scope.equals(metadata.getScope()) && metadata.getObjectName().contains(nameFilter))
                        .collect(Collectors.toList());
            }

        } finally {
            cacheLock.readLock().unlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#
     * findByScopeRegistryAndStage(java.lang.String, java.lang.String,
     * java.lang.String)
     */
    @Override
    public List<ObjectMetadata> findByScopeRegistryAndStage(String scope, String registry, String stage) {
        requireNonNull(scope, "Scope must not be null");
        requireNonNull(registry, "Registry must not be null");
        requireNonNull(stage, "Stage must not be null");

        cacheLock.readLock().lock();
        try {
            ensureCacheInitialized();
            return allMetadata().filter(metadata -> stage.equals(metadata.getStage())
                            && scope.equals(metadata.getScope()) && registry.equals(metadata.getRegistry()))
                    .collect(Collectors.toList());
        } finally {
            cacheLock.readLock().unlock();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#
     * findByScopeRegistryStageAndName(java.lang.String, java.lang.String,
     * java.lang.String, java.lang.String)
     */
    @Override
    public List<ObjectMetadata> findByScopeRegistryStageAndName(String scope, String registry, String stage,
            String name) {
        requireNonNull(scope, "Scope must not be null");
        requireNonNull(registry, "Registry must not be null");
        requireNonNull(stage, "Stage must not be null");
        requireNonNull(name, "Name must not be null");

        // name can also contain * for wildcard search
        boolean isExact = !name.contains("*");
        String nameFilter = name.contains("*") ? name.replaceAll("\\*", "") : name;
        cacheLock.readLock().lock();
        try {
            ensureCacheInitialized();
            if (isExact) {
                return allMetadata()
                        .filter(metadata -> stage.equals(metadata.getStage()) && scope.equals(metadata.getScope())
                                && registry.equals(metadata.getRegistry()) && name.equals(metadata.getObjectName()))
                        .collect(Collectors.toList());
            } else {
                return allMetadata()
                        .filter(metadata -> stage.equals(metadata.getStage()) && scope.equals(metadata.getScope())
                                && registry.equals(metadata.getRegistry())
                                && metadata.getObjectName().contains(nameFilter))
                        .collect(Collectors.toList());
            }

        } finally {
            cacheLock.readLock().unlock();
        }
    }

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService#findByGenerationTriggerFingerprint(java.lang.String)
	 */
	@Override
	public Optional<ObjectMetadata> findByGenerationTriggerFingerprint(String fingerprint) {
		requireNonNull(fingerprint, "Fingerprint must not be null");

        cacheLock.readLock().lock();
        try {
            ensureCacheInitialized();
            RegistryAddress address = addressByGenerationTriggerFingerprint.get(fingerprint);
            return address == null ? Optional.empty() : Optional.ofNullable(metadataAt(address));
        } finally {
            cacheLock.readLock().unlock();
        }
	}

}