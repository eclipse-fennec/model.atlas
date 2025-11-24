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
package org.eclipse.fennec.model.atlas.mgmt.storage;

import static java.util.Objects.requireNonNull;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectQuery;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;
import org.eclipse.fennec.model.atlas.mgmt.management.StorageBackendType;
import org.osgi.framework.BundleContext;
import org.osgi.util.promise.Deferred;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * Abstract base class for EObjectStorageService implementations.
 * 
 * <p>This class provides a complete implementation of the Promise-based EObjectStorageService API
 * by delegating all storage operations to an {@link AbstractStorageHelper}. This eliminates
 * code duplication across different storage backend implementations and ensures consistent
 * behavior and error handling patterns.</p>
 * 
 * <h3>Implementation Guide</h3>
 * <p>Concrete subclasses only need to:</p>
 * <ol>
 * <li><strong>Implement {@link #createStorageHelper()}</strong> - Create and configure their specific storage helper</li>
 * <li><strong>Implement {@link #getBackendType()}</strong> - Return their storage backend type</li>
 * <li><strong>Add OSGi annotations</strong> - Component registration and configuration policy</li>
 * <li><strong>Handle activation/deactivation</strong> - Call parent lifecycle methods</li>
 * </ol>
 * 
 * <h3>Example Implementation</h3>
 * <pre>{@code
 * @Component(
 *     property = {"storage.backend=example"},
 *     configurationPolicy = ConfigurationPolicy.REQUIRE
 * )
 * public class ExampleStorageService extends AbstractEObjectStorageService {
 *     
 *     @Activate
 *     public void activate(Config config) throws Exception {
 *         // Custom initialization
 *         activateStorageService(); // Call parent
 *     }
 *     
 *     @Override
 *     protected AbstractStorageHelper createStorageHelper() throws Exception {
 *         return new ExampleStorageHelper(resourceSet, config);
 *     }
 *     
 *     @Override
 *     public StorageBackendType getBackendType() {
 *         return StorageBackendType.EXAMPLE;
 *     }
 * }
 * }</pre>
 * 
 * <h3>Features Provided</h3>
 * <ul>
 * <li><strong>Complete API Implementation</strong> - All 6 EObjectStorageService methods implemented</li>
 * <li><strong>Promise-based Error Handling</strong> - Consistent error propagation and logging</li>
 * <li><strong>Null Safety</strong> - Proper null checks and meaningful error messages</li>
 * <li><strong>Query Support</strong> - Basic queryObjects implementation with metadata integration</li>
 * <li><strong>Lifecycle Management</strong> - Proper resource cleanup and initialization</li>
 * <li><strong>Comprehensive Logging</strong> - Debug, info, and error logging for operations</li>
 * </ul>
 * 
 * <h3>Dependencies</h3>
 * <p>This class requires the following OSGi services to be injected:</p>
 * <ul>
 * <li><strong>PromiseFactory</strong> - For creating and managing Promise instances</li>
 * <li><strong>ResourceSet</strong> - EMF ResourceSet with target "(emf.name=management)"</li>
 * </ul>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 * @see AbstractStorageHelper
 * @see EObjectStorageService
 */
public abstract class AbstractEObjectStorageService implements EObjectStorageService<EObject> {

    protected static final Logger LOGGER = Logger.getLogger(AbstractEObjectStorageService.class.getName());

    protected PromiseFactory promiseFactory;
    
    protected AbstractStorageHelper storageHelper;
    
    protected BundleContext bctx;

    // Registry service (always uses shared registry)
    protected EObjectRegistryService<EObject> registryService;
    
    // Storage role for this instance (set during activation)
    protected String storageRole;
    
    /**
     * Called during activation to create the storage helper.
     * 
     * <p>Subclasses implement this method to create and configure their specific storage helper
     * based on their configuration parameters. This method is called once during service
     * activation and the returned helper is used for all subsequent storage operations.</p>
     * 
     * @return the configured storage helper, must not be null
     * @throws Exception if helper creation fails due to configuration issues or resource problems
     */
    protected abstract AbstractStorageHelper createStorageHelper() throws Exception;
    
    /**
	 * Returns the backend type for this storage implementation.
	 * 
	 * <p>This method identifies the type of storage backend this service provides.
	 * The backend type is used for service selection and monitoring purposes.</p>
	 * 
	 * @return the storage backend type, must not be null
	 */
	public abstract StorageBackendType getBackendType();

	/**
	 * Returns the storage role for this storage implementation.
	 * 
	 * <p>The storage role identifies the purpose of this storage instance within
	 * the object lifecycle (draft, approved, documentation, etc.). This role is
	 * automatically set in object metadata to enable role-based queries and workflows.</p>
	 * 
	 * @return the storage role, may be null if no role is configured
	 */
	protected abstract String getStorageRole();

	/**
     * Common activation logic for storage services.
     * 
     * <p>This method should be called from the subclass @Activate method after any
     * custom initialization is complete. It creates the storage helper and prepares
     * the service for operation.</p>
     * 
     * @throws Exception if storage helper creation fails
     * @see #createStorageHelper()
     */
    protected void activateStorageService() throws Exception {
        LOGGER.info("Activating " + getClass().getSimpleName() + " with backend type: " + getBackendType());
        
        // Initialize storage role
        this.storageRole = getStorageRole();
        requireNonNull(storageRole, "Storage role from getStorageRole() must not be null");
        LOGGER.info("Storage role configured: " + storageRole);
        
        String threadFactory = getBackendType() + "-worker-";
        AtomicLong threadCount = new AtomicLong(0);
        this.promiseFactory = new PromiseFactory(Executors.newCachedThreadPool(new ThreadFactory() {
			
			@Override
			public Thread newThread(Runnable r) {
				Thread t = new Thread(r, threadFactory + threadCount.getAndIncrement());
			    // Set thread properties if needed
			    t.setDaemon(false);
			    t.setPriority(Thread.NORM_PRIORITY);
			    return t;
			}
		}));
        this.storageHelper = createStorageHelper();
        
        if (this.storageHelper == null) {
            throw new IllegalStateException("Storage helper creation returned null");
        }
        
        initializeRegistryService();
        
        LOGGER.info("Storage service activated successfully: " + getClass().getSimpleName());
    }
    
    /**
	 * Gets the registry service for centralized metadata management.
	 * 
	 * <p>This abstract method forces implementors to provide access to the shared
	 * {@link EObjectRegistryService} that provides centralized metadata management
	 * across all storage backends. The shared registry enables:</p>
	 * 
	 * <ul>
	 * <li><strong>Cross-Storage Queries</strong> - Query objects across all storage backends</li>
	 * <li><strong>Resource Efficiency</strong> - Single Lucene index for entire system</li>
	 * <li><strong>Centralized Management</strong> - One registry for all metadata</li>
	 * <li><strong>Role-based Organization</strong> - Objects organized by role (draft, approved, documentation)</li>
	 * </ul>
	 * 
	 * <h3>Implementation Guide</h3>
	 * <p>Implementors should inject the SharedLuceneRegistryService via OSGi DS:</p>
	 * <pre>{@code
	 * @Reference(target = "(component.name=SharedLuceneRegistryService)")
	 * private EObjectRegistryService<EObject> sharedRegistry;
	 * 
	 * @Override
	 * protected EObjectRegistryService<EObject> getRegistryService() {
	 *     return sharedRegistry;
	 * }
	 * }</pre>
	 * 
	 * @return the shared registry service, must not be null
	 */
	protected abstract EObjectRegistryService<EObject> getRegistryService();

	/**
	 * Initializes the registry service for this storage implementation.
	 * 
	 * <p>This method gets the shared registry service from the subclass implementation
	 * and prepares it for use. The registry service provides fast lookup capabilities
	 * for object metadata and is automatically synchronized with storage operations.</p>
	 * 
	 * <h3>Automatic Cache Synchronization</h3>
	 * <p>The registry service is automatically kept in sync with storage operations:</p>
	 * <ul>
	 * <li><strong>Store Operations</strong> - updateCache() called after successful storage</li>
	 * <li><strong>Update Operations</strong> - Cache updated when metadata changes</li>
	 * <li><strong>Delete Operations</strong> - removeFromCache() called after deletion</li>
	 * </ul>
	 * 
	 * @throws IllegalStateException if registry service is null
	 * @see #getRegistryService()
	 */
	protected void initializeRegistryService() {
		this.registryService = getRegistryService();
		requireNonNull(registryService, "Registry service from getRegistryService() must not be null");
		LOGGER.info("Initialized shared registry service for " + getClass().getSimpleName());
	}


	/**
     * Common deactivation logic for storage services.
     * 
     * <p>This method should be called from the subclass @Deactivate method.
     * It performs cleanup and releases resources.</p>
     */
    protected void deactivateStorageService() {
        LOGGER.info("Deactivating " + getClass().getSimpleName());
        // No registry unregistration needed since we use shared registry
        this.storageHelper = null;
        this.registryService = null;
        LOGGER.info("Storage service deactivated: " + getClass().getSimpleName());
    }

    @Override
    public Promise<String> storeObject(String objectId, EObject object, ObjectMetadata metadata) {
        return promiseFactory.submit(() -> {
            try {
                // Use provided objectId or generate one
                String storageId = (objectId != null && !objectId.isEmpty()) ? objectId : UUID.randomUUID().toString();
                
                // Ensure the objectId, role, and objectType are set in metadata
                if (metadata != null) {
                    metadata.setObjectId(storageId); // Always set the objectId in the caller's metadata
                    metadata.setRole(storageRole);
                    // Set the objectType if not already set
                    if (metadata.getObjectType() == null && object != null) {
                        metadata.setObjectType(object.eClass().getName());
                    }
                }
                
                // Use helper to save both object and metadata
                storageHelper.saveEObject(storageId, object, metadata);
                storageHelper.saveMetadata(storageId, metadata);
                
                // Update registry cache if available
                if (registryService != null) {
                    ObjectMetadata metadataCopy = EcoreUtil.copy(metadata);
                    metadataCopy.setObjectId(storageId); // Ensure objectId is set
                    // Ensure objectType is set in registry copy as well
                    if (metadataCopy.getObjectType() == null && object != null) {
                        metadataCopy.setObjectType(object.eClass().getName());
                    }
                    registryService.updateCache(metadataCopy);
                }
                
                String fileExtension = storageHelper.getFileExtension(metadata);
                LOGGER.info("Stored EObject with ID: " + storageId + " and extension: " + fileExtension);
                return storageId;
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to store object", e);
                throw new RuntimeException("Failed to store object", e);
            }
        });
    }

    @Override
    public Promise<EObject> retrieveObject(String objectId) {
        return promiseFactory.submit(() -> {
            try {
                return storageHelper.loadEObject(objectId);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to retrieve object: " + objectId, e);
                throw new RuntimeException("Failed to retrieve object", e);
            }
        });
    }

    @Override
    public Promise<ObjectMetadata> retrieveMetadata(String objectId) {
        return promiseFactory.submit(() -> {
            try {
                return storageHelper.loadMetadata(objectId);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to retrieve metadata: " + objectId, e);
                throw new RuntimeException("Failed to retrieve metadata", e);
            }
        });
    }

    @Override
    public Promise<Boolean> deleteObject(String objectId) {
        return promiseFactory.submit(() -> {
            try {
                boolean deleted = storageHelper.deleteObject(objectId);
                
                // Remove from registry cache if deletion was successful
                if (deleted && registryService != null) {
                    registryService.removeFromCache(objectId);
                }
                
                LOGGER.info("Deleted object with ID: " + objectId + " - " + deleted);
                return deleted;
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to delete object: " + objectId, e);
                throw new RuntimeException("Failed to delete object", e);
            }
        });
    }

    @Override
    public Promise<List<String>> listObjectIds() {
        return promiseFactory.submit(() -> {
            try {
                return storageHelper.listObjectIds();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to list object IDs", e);
                throw new RuntimeException("Failed to list object IDs", e);
            }
        });
    }

    @Override
    public Promise<List<ObjectMetadata>> queryObjects(ObjectQuery query) {
        // If registry service is available, delegate to it for fast indexed queries
        if (registryService != null) {
            return delegateQueryToRegistry(query);
        }
        
        // Fall back to direct storage scanning when registry unavailable
        return scanStorageDirectly(query);
    }
    
    /**
     * Delegate query to registry service for fast indexed lookup.
     * 
     * @param query the query criteria
     * @return promise with matching metadata objects
     */
    private Promise<List<ObjectMetadata>> delegateQueryToRegistry(ObjectQuery query) {
        return promiseFactory.submit(() -> {
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.fine("Delegating query to registry service for " + getBackendType());
            }
            
            List<ObjectMetadata> results = new ArrayList<>();
            
            try {
                // Use registry service methods based on query parameters
                if (query.getStatus() != null && query.getObjectType() != null) {
                    // Most specific query - status + type
                    results = registryService.findByStatusAndType(query.getStatus(), query.getObjectType());
                } else if (query.getStatus() != null) {
                    // Query by status only
                    results = registryService.findByStatus(query.getStatus());
                } else if (query.getObjectType() != null) {
                    // Query by object type only
                    results = registryService.findByObjectType(query.getObjectType());
                } else {
                    // Generic query - could add more registry methods as needed
                    LOGGER.fine("Query has no specific criteria, falling back to storage scan");
                    return scanStorageDirectly(query).getValue();
                }
                
                // Filter results by storage role to only return objects from this storage
                if (storageRole != null) {
                    results = results.stream()
                            .filter(metadata -> storageRole.equals(metadata.getRole()))
                            .collect(java.util.stream.Collectors.toList());
                }
                
                LOGGER.info("Registry query completed: " + results.size() + " objects found");
                return results;
                
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Registry query failed, falling back to storage scan", e);
                return scanStorageDirectly(query).getValue();
            }
        });
    }
    
    /**
     * Scan storage directly when registry service is unavailable or query fails.
     * This is the original brute-force implementation.
     * 
     * @param query the query criteria
     * @return promise with matching metadata objects
     */
    private Promise<List<ObjectMetadata>> scanStorageDirectly(ObjectQuery query) {
        Deferred<List<ObjectMetadata>> deferred = promiseFactory.deferred();
        
        if (LOGGER.isLoggable(Level.FINE)) {
            LOGGER.fine("Scanning storage directly using " + getBackendType());
        }
        
        try {
            if (storageHelper == null) {
                throw new IllegalStateException("Storage service not properly initialized");
            }
            
            List<ObjectMetadata> metadataList = new ArrayList<>();
            List<String> objectIds = storageHelper.listObjectIds();
            
            LOGGER.fine("Processing " + objectIds.size() + " objects for query");
            
            int successCount = 0;
            int errorCount = 0;
            
            for (String objectId : objectIds) {
                try {
                    ObjectMetadata metadata = storageHelper.loadMetadata(objectId);
                    if (metadata != null) {
                        // Ensure objectId is set in metadata for consistency
                        if (metadata.getObjectId() == null || metadata.getObjectId().isEmpty()) {
                            metadata.setObjectId(objectId);
                        }
                        
                        // Apply query filters
                        if (matchesQuery(metadata, query)) {
                            metadataList.add(metadata);
                            successCount++;
                        }
                    } else {
                        LOGGER.warning("No metadata found for object ID: " + objectId);
                        errorCount++;
                    }
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Failed to load metadata for object ID: " + objectId, e);
                    errorCount++;
                }
            }
            
            LOGGER.info("Storage scan completed: " + successCount + " successful, " + errorCount + " errors, " + 
                       metadataList.size() + " total metadata objects");
            
            deferred.resolve(metadataList);
            
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to scan storage directly", e);
            deferred.fail(e);
        }
        
        return deferred.getPromise();
    }
    
    /**
     * Check if metadata matches the given query criteria.
     * 
     * @param metadata the metadata to check
     * @param query the query criteria
     * @return true if metadata matches the query
     */
    private boolean matchesQuery(ObjectMetadata metadata, ObjectQuery query) {
        if (query == null) {
            return true; // No query means match all
        }
        
        // Check status filter
        if (query.getStatus() != null && !query.getStatus().equals(metadata.getStatus())) {
            return false;
        }
        
        // Check object type filter
        if (query.getObjectType() != null && !query.getObjectType().equals(metadata.getObjectType())) {
            return false;
        }
        
        // Check upload user filter
        if (query.getUploadUser() != null && !query.getUploadUser().equals(metadata.getUploadUser())) {
            return false;
        }
        
        // Check source channel filter
        if (query.getSourceChannel() != null && !query.getSourceChannel().equals(metadata.getSourceChannel())) {
            return false;
        }
        
        return true; // All specified criteria match
    }
    
	@Override
	public Promise<Boolean> updateMetadata(String objectId, ObjectMetadata metadata) {
        return promiseFactory.submit(() -> {
            try {
                if (objectId == null || objectId.isEmpty()) {
                    throw new IllegalArgumentException("Object ID cannot be null or empty");
                }
                if (metadata == null) {
                    throw new IllegalArgumentException("Metadata cannot be null");
                }
                
                // Check if object exists
                if (!storageHelper.objectExists(objectId)) {
                    LOGGER.warning("Cannot update metadata - object does not exist: " + objectId);
                    return false;
                }
                
                // Load existing metadata to preserve immutable fields
                ObjectMetadata existingMetadata = storageHelper.loadMetadata(objectId);
                if (existingMetadata == null) {
                    LOGGER.warning("Cannot update metadata - no existing metadata found for object: " + objectId);
                    return false;
                }
                
                ObjectMetadata metadataCopy = EcoreUtil.copy(existingMetadata);
                // Merge updates while preserving immutable fields
                mergeMetadataUpdates(metadataCopy, metadata);
                
                // Ensure the role and objectId are set correctly
                metadataCopy.setRole(storageRole);
                metadataCopy.setObjectId(objectId);
                
                // Save merged metadata
                storageHelper.saveMetadata(objectId, metadataCopy);
                
                // Update registry cache if available
                if (registryService != null) {
                    registryService.updateCache(EcoreUtil.copy(metadataCopy));
                }
                
                LOGGER.info("Updated metadata for object: " + objectId);
                return true;
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to update metadata for object: " + objectId, e);
                throw new RuntimeException("Failed to update metadata", e);
            }
        });
	}

	@Override
	public Promise<Boolean> updateStatus(String objectId, ObjectStatus newStatus, String changeUser) {
        return promiseFactory.submit(() -> {
            try {
                if (objectId == null || objectId.isEmpty()) {
                    throw new IllegalArgumentException("Object ID cannot be null or empty");
                }
                if (newStatus == null) {
                    throw new IllegalArgumentException("New status cannot be null");
                }
                
                // Check if object exists and retrieve current metadata
                if (!storageHelper.objectExists(objectId)) {
                    LOGGER.warning("Cannot update status - object does not exist: " + objectId);
                    return false;
                }
                
                ObjectMetadata metadata = storageHelper.loadMetadata(objectId);
                if (metadata == null) {
                    LOGGER.warning("Cannot update status - no metadata found for object: " + objectId);
                    return false;
                }
                
                // Update status and change tracking
                metadata.setStatus(newStatus);
                metadata.setLastChangeTime(java.time.Instant.now());
                if (changeUser != null && !changeUser.isEmpty()) {
                    metadata.setLastChangeUser(changeUser);
                }
                
                // Save updated metadata
                storageHelper.saveMetadata(objectId, metadata);
                LOGGER.info("Updated status to " + newStatus + " for object: " + objectId);
                return true;
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to update status for object: " + objectId, e);
                throw new RuntimeException("Failed to update status", e);
            }
        });
	}

	@Override
	public Boolean exists(String objectId) {
        try {
            if (objectId == null || objectId.isEmpty()) {
                return false;
            }
            
            return storageHelper.objectExists(objectId);
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error checking object existence: " + objectId, e);
            return false;
        }
	}

	@Override
	public long getObjectCount() {
        try {
            List<String> objectIds = storageHelper.listObjectIds();
            return objectIds.size();
            
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Error getting object count", e);
            return 0;
        }
	}

	/**
	 * Merges metadata updates while strictly preserving immutable fields.
	 * 
	 * <p>This method updates the existing metadata with values from the update metadata
	 * while completely ignoring any attempts to change immutable fields. Upload fields
	 * are permanently immutable after the initial object creation.</p>
	 * 
	 * <h3>Strictly Immutable Fields (NEVER updated)</h3>
	 * <ul>
	 * <li><strong>objectId</strong> - Managed by storage layer, never changed</li>
	 * <li><strong>uploadUser</strong> - Original uploader, permanently immutable</li>
	 * <li><strong>uploadTime</strong> - Original upload timestamp, permanently immutable</li>
	 * <li><strong>role</strong> - Set by storage implementation, not from user input</li>
	 * </ul>
	 * 
	 * <h3>Updatable Fields</h3>
	 * <ul>
	 * <li><strong>objectName</strong> - Can be updated</li>
	 * <li><strong>version</strong> - Can be updated</li>
	 * <li><strong>status</strong> - Can be updated</li>
	 * <li><strong>reviewUser</strong> - Can be updated</li>
	 * <li><strong>reviewReason</strong> - Can be updated</li>
	 * <li><strong>reviewTime</strong> - Can be updated</li>
	 * <li><strong>lastChangeTime</strong> - Set automatically to current time</li>
	 * <li><strong>lastChangeUser</strong> - Can be updated</li>
	 * <li><strong>sourceChannel</strong> - Can be updated</li>
	 * <li><strong>contentHash</strong> - Can be updated</li>
	 * <li><strong>governanceDocumentationId</strong> - Can be updated</li>
	 * <li><strong>lastChangeReason</strong> - Can be updated</li>
	 * <li><strong>properties</strong> - Merged (existing properties preserved, new ones added)</li>
	 * </ul>
	 * 
	 * @param existing the existing metadata to update (modified in-place)
	 * @param updates the metadata containing update values
	 */
	private void mergeMetadataUpdates(ObjectMetadata existing, ObjectMetadata updates) {
		// Update only mutable fields from the updates
		// Upload fields (uploadUser, uploadTime) are NEVER touched - they are permanently immutable
		
		if (updates.getObjectName() != null) {
			existing.setObjectName(updates.getObjectName());
		}
		if (updates.getVersion() != null) {
			existing.setVersion(updates.getVersion());
		}
		if (updates.getStatus() != null) {
			existing.setStatus(updates.getStatus());
		}
		if (updates.getReviewUser() != null) {
			existing.setReviewUser(updates.getReviewUser());
		}
		if (updates.getReviewReason() != null) {
			existing.setReviewReason(updates.getReviewReason());
		}
		if (updates.getReviewTime() != null) {
			existing.setReviewTime(updates.getReviewTime());
		}
		if (updates.getLastChangeUser() != null) {
			existing.setLastChangeUser(updates.getLastChangeUser());
		}
		if (updates.getSourceChannel() != null) {
			existing.setSourceChannel(updates.getSourceChannel());
		}
		if (updates.getContentHash() != null) {
			existing.setContentHash(updates.getContentHash());
		}
		if (updates.getGovernanceDocumentationId() != null) {
			existing.setGovernanceDocumentationId(updates.getGovernanceDocumentationId());
		}
		if (updates.getLastChangeReason() != null) {
			existing.setLastChangeReason(updates.getLastChangeReason());
		}
		if (updates.getObjectType() != null) {
			existing.setObjectType(updates.getObjectType());
		}
		
		// Always set lastChangeTime to current time for any update
		existing.setLastChangeTime(Instant.now());
		
		// Merge properties (preserve existing, add new ones)
		if (updates.getProperties() != null && !updates.getProperties().isEmpty()) {
			if (existing.getProperties() == null) {
				// If existing has no properties, copy all from updates
				existing.getProperties().putAll(updates.getProperties());
			} else {
				// Merge properties - existing ones are preserved, new ones are added
				existing.getProperties().putAll(updates.getProperties());
			}
		}
		
		// CRITICAL: Upload fields are NEVER updated regardless of what's in the update metadata:
		// - uploadUser: Original uploader identity, permanently immutable
		// - uploadTime: Original upload timestamp, permanently immutable  
		// - objectId: Managed by storage layer
		// - role: Set by storage implementation
		// 
		// This ensures data integrity and audit trail preservation
	}

}