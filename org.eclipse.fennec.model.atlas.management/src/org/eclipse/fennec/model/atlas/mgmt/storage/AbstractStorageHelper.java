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

import java.io.IOException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EDataType.Internal.ConversionDelegate;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.mgmt.conversion.InstantConversionDelegateFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;

/**
 * Abstract base class for EMF object storage operations.
 * Provides shared functionality for resource management, metadata handling, and EMF serialization.
 * Storage-specific implementations should extend this class and implement the abstract methods.
 * 
 * <p>This class implements AutoCloseable to ensure proper resource cleanup. Subclasses should
 * implement {@link #closeStorageResources()} to clean up storage-specific resources.</p>
 */
public abstract class AbstractStorageHelper implements AutoCloseable {
    
    private static final Logger LOGGER = Logger.getLogger(AbstractStorageHelper.class.getName());
    protected static final String DEFAULT_EXTENSION = ".xmi";
    protected static final String DEFAULT_CONTENT_TYPE = "application/xml";
    protected static final String METADATA_EXTENSION = ".metadata.xmi";
    protected static final String FILE_EXTENSION_PROPERTY = "file.extension";
    protected static final String CONTENT_TYPE_PROPERTY = "content.type";
    
    protected final ResourceSet resourceSet;
    
    public AbstractStorageHelper(ResourceSet resourceSet) {
        this.resourceSet = resourceSet;
        setupConversionDelegates();
    }
    
    /**
     * Sets up EMF conversion delegates for custom data types.
     */
    private void setupConversionDelegates() {
        // Register conversion delegate for Instant serialization
        InstantConversionDelegateFactory conversionFactory = new InstantConversionDelegateFactory();
        EcoreUtil.setConversionDelegates(ManagementPackage.eINSTANCE, List.of(Instant.class.getName()));
        ConversionDelegate.Factory.Registry.INSTANCE.put(Instant.class.getName(), conversionFactory);
    }
    
    /**
     * Creates a resource with proper content type handling and cleanup.
     */
    public static class ResourceOperation {
        private final Resource resource;
        private final ResourceSet resourceSet;
        
        public ResourceOperation(Resource resource, ResourceSet resourceSet) {
            this.resource = resource;
            this.resourceSet = resourceSet;
        }
        
        public Resource getResource() {
            return resource;
        }
        
        /**
         * Cleans up the resource from the ResourceSet and unloads it.
         */
        public void cleanup() {
            try {
                if (resource != null) {
                    synchronized (resourceSet) {
                        if (resourceSet.getResources().contains(resource)) {
                            resourceSet.getResources().remove(resource);
                        }
                    }
//                    resource.unload();
                }
            } catch (Exception e) {
                String resourceUri = (resource != null && resource.getURI() != null) ? resource.getURI().toString() : "unknown";
                LOGGER.log(Level.WARNING, "Failed to cleanup resource: " + resourceUri, e);
            }
        }
    }
    
    /**
     * Determines the file extension from metadata properties.
     * Handles both extensions with and without leading dots.
     */
    public String getFileExtension(ObjectMetadata metadata) {
        if (metadata.getProperties() != null && metadata.getProperties().containsKey(FILE_EXTENSION_PROPERTY)) {
            String customExtension = (String) metadata.getProperties().get(FILE_EXTENSION_PROPERTY);
            if (customExtension != null && !customExtension.isEmpty()) {
                return customExtension.startsWith(".") ? customExtension : "." + customExtension;
            }
        }
        return DEFAULT_EXTENSION;
    }
    
    /**
     * Gets the content type from metadata properties.
     */
    public String getContentType(ObjectMetadata metadata) {
        if (metadata.getProperties() != null && metadata.getProperties().containsKey(CONTENT_TYPE_PROPERTY)) {
            return (String) metadata.getProperties().get(CONTENT_TYPE_PROPERTY);
        }
        return DEFAULT_CONTENT_TYPE;
    }
    
    /**
     * Creates a resource with proper content type and factory selection.
     * Handles content type-specific resource factories and fallbacks.
     */
    public ResourceOperation createResource(URI uri, String contentType) {
        Resource resource = null;
        
        try {
            synchronized (resourceSet) {
                if (contentType != null) {
                    // When content type is specified, use it to get the appropriate factory
                    Resource.Factory factory = (Resource.Factory) resourceSet.getResourceFactoryRegistry()
                        .getContentTypeToFactoryMap().get(contentType);
                    if (factory != null) {
                        resource = factory.createResource(uri);
                        if (resource != null && !resourceSet.getResources().contains(resource)) {
                            resourceSet.getResources().add(resource);
                        }
                    } else {
                        // Fall back to normal creation if no factory found for content type
                        resource = resourceSet.createResource(uri);
                    }
                } else {
                    // Let EMF determine the resource type based on extension
                    resource = resourceSet.createResource(uri);
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, "Failed to create resource: " + uri, e);
        }
        
        return new ResourceOperation(resource, resourceSet);
    }
    
    /**
     * Loads a resource and returns a ResourceOperation for cleanup.
     */
    public ResourceOperation loadResource(URI uri) throws IOException {
        Resource resource;
        synchronized (resourceSet) {
            resource = resourceSet.getResource(uri, true);
        }
        return new ResourceOperation(resource, resourceSet);
    }
    
    /**
     * Serializes an EObject to storage using EMF resources.
     * The storage-specific implementation handles the actual writing.
     * Returns the storage id
     */
    public String saveEObject(String objectId, EObject object, ObjectMetadata metadata) throws IOException {
        String fileExtension = getFileExtension(metadata);
        String contentType = getContentType(metadata);
        
        String objectPath = buildObjectPath(objectId, fileExtension);
        URI objectUri = createStorageURI(objectPath);
        
        ResourceOperation objectOp = createResource(objectUri, contentType);
        try {
            objectOp.getResource().getContents().add(object);
            objectOp.getResource().save(Collections.emptyMap());
            
            // Let storage implementation handle the actual persistence
            persistResource(objectPath, objectOp.getResource());
        } finally {
            objectOp.cleanup();
        }
        return objectId;
    }
    
    /**
     * Serializes metadata to storage as XMI.
     */
    public void saveMetadata(String objectId, ObjectMetadata metadata) throws IOException {
        Objects.requireNonNull(objectId, "Cannot save metadata - objectId cannot be null");
        Objects.requireNonNull(metadata, "Cannot save metadata - metadata cannot be null");
        
        if (objectId.isEmpty()) {
            throw new IllegalArgumentException("Cannot save metadata - objectId cannot be empty");
        }
        
        // CRITICAL: Ensure metadata always has objectId before persistence
        if (Objects.isNull(metadata.getObjectId()) || metadata.getObjectId().isEmpty()) {
            metadata.setObjectId(objectId);
            LOGGER.fine("Set objectId in metadata before saving: " + objectId);
        }
        
        // Validate that objectId matches the storage path
        if (!Objects.equals(objectId, metadata.getObjectId())) {
            throw new IllegalStateException("Metadata objectId (" + metadata.getObjectId() + 
                                          ") does not match storage objectId (" + objectId + ")");
        }
        
        String metadataPath = buildMetadataPath(objectId);
        URI metadataUri = createStorageURI(metadataPath);
        
        ResourceOperation metadataOp = createResource(metadataUri, null);
        try {
            metadataOp.getResource().getContents().add(metadata);
            metadataOp.getResource().save(Collections.emptyMap());
            
            // Let storage implementation handle the actual persistence
            persistResource(metadataPath, metadataOp.getResource());
        } finally {
            metadataOp.cleanup();
        }
    }
    
    /**
     * Loads an EObject from storage, automatically detecting the file extension.
     */
    public EObject loadEObject(String objectId) throws IOException {
        String objectPath = findObjectPath(objectId);
        if (objectPath == null) {
            return null;
        }
        
        URI objectUri = createStorageURI(objectPath);
        ResourceOperation operation = loadResource(objectUri);
        try {
            if (operation.getResource().getContents().isEmpty()) {
                return null;
            }
            return operation.getResource().getContents().get(0);
        } finally {
            operation.cleanup();
        }
    }
    
    /**
     * Loads metadata from storage.
     */
    public ObjectMetadata loadMetadata(String objectId) throws IOException {
        Objects.requireNonNull(objectId, "Cannot load metadata - objectId cannot be null");
        
        if (objectId.isEmpty()) {
            throw new IllegalArgumentException("Cannot load metadata - objectId cannot be empty");
        }
        
        String metadataPath = buildMetadataPath(objectId);
        if (!storageExists(metadataPath)) {
            return null;
        }
        
        URI metadataUri = createStorageURI(metadataPath);
        ResourceOperation operation = loadResource(metadataUri);
        try {
            if (operation.getResource().getContents().isEmpty()) {
                return null;
            }
            EObject eObject = operation.getResource().getContents().get(0);
            
            // Resolve all proxies to ensure containment references (like properties EMap) are properly loaded
            ObjectMetadata metadata = (ObjectMetadata) eObject;
            
           checkMetadataConsistency(objectId, metadata);
            
            return metadata;
        } finally {
            operation.cleanup();
        }
    }
    
    protected void checkMetadataConsistency(String objectId, ObjectMetadata metadata) {
    	 // CRITICAL: Validate data integrity - metadata must have objectId
        if (Objects.isNull(metadata.getObjectId()) || metadata.getObjectId().isEmpty()) {
            throw new IllegalStateException("Data integrity violation: loaded metadata for objectId '" + 
                                          objectId + "' has no objectId set. This indicates a fundamental storage error.");
        }
        
        // Validate that the loaded objectId matches the requested objectId
        if (!Objects.equals(objectId, metadata.getObjectId())) {
            throw new IllegalStateException("Data integrity violation: loaded metadata objectId '" + 
                                          metadata.getObjectId() + "' does not match requested objectId '" + objectId + "'");
        }
        
    }
    
    /**
     * Builds the storage path for an object with the given extension.
     */
    protected String buildObjectPath(String objectId, String extension) {
        return objectId + extension;
    }
    
    /**
     * Builds the storage path for metadata.
     */
    protected String buildMetadataPath(String objectId) {
        return objectId + METADATA_EXTENSION;
    }
    
    
    // Abstract methods to be implemented by storage-specific classes
    
    /**
     * Creates a storage-appropriate URI for the given path.
     * File storage uses file:// URIs, S3 storage might use s3:// or custom schemes.
     */
    protected abstract URI createStorageURI(String path);
    
    /**
     * Persists a resource to the storage backend.
     * This is called after EMF serialization to handle storage-specific operations.
     */
    protected abstract void persistResource(String path, Resource resource) throws IOException;
    
    
    
    /**
     * Checks if a resource exists in storage.
     */
    protected abstract boolean storageExists(String path) throws IOException;
    
    /**
     * Finds the storage path for an object, regardless of extension.
     * Returns null if no object found.
     */
    protected abstract String findObjectPath(String objectId) throws IOException;
    
    /**
     * Deletes all storage resources associated with an object ID.
     * Should delete both the object and metadata files.
     */
    public abstract boolean deleteObject(String objectId) throws IOException;
    
    /**
     * Lists all object IDs in storage.
     */
    public abstract List<String> listObjectIds() throws IOException;
    
    /**
     * Checks if an object exists in storage.
     * This method checks for the existence of either the object file or metadata.
     */
    public boolean objectExists(String objectId) throws IOException {
        if (objectId == null || objectId.isEmpty()) {
            return false;
        }
        
        // Check if metadata exists (most reliable indicator)
        String metadataPath = buildMetadataPath(objectId);
        if (storageExists(metadataPath)) {
            return true;
        }
        
        // Fallback: check if object file exists with any extension
        String objectPath = findObjectPath(objectId);
        return objectPath != null;
    }
    
    /**
     * Template method for subclasses to clean up storage-specific resources.
     * Called during close() to allow storage implementations to cleanup
     * their specific resources (connections, indexes, file handles, etc.).
     * 
     * <p>Default implementation does nothing. Override only if storage-specific cleanup is needed.</p>
     * 
     * @throws Exception if cleanup fails
     */
    protected void closeStorageResources() throws Exception {
        // Default implementation - no cleanup needed
    }
    
    /**
     * Closes the storage helper and releases any resources.
     * Implements AutoCloseable contract for proper resource management.
     * 
     * <p>This method:</p>
     * <ul>
     * <li>Calls {@link #closeStorageResources()} for subclass-specific cleanup</li>
     * <li>Unloads and clears all EMF resources from the ResourceSet</li>
     * <li>Logs any cleanup errors without propagating exceptions</li>
     * </ul>
     */
    @Override
    public void close() throws Exception {
        LOGGER.info("Closing storage helper: " + getClass().getSimpleName());
        
        try {
            // First, let subclasses clean up their specific resources
            closeStorageResources();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to close storage-specific resources", e);
            // Continue with ResourceSet cleanup even if storage cleanup fails
        }
        
        try {
            // Clean up EMF ResourceSet
            synchronized (resourceSet) {
                // Unload all resources and clear the resource set
                for (Resource resource : resourceSet.getResources()) {
                    try {
                        if (resource.isLoaded()) {
                            resource.unload();
                        }
                    } catch (Exception e) {
                        LOGGER.log(Level.WARNING, "Failed to unload resource: " + resource.getURI(), e);
                    }
                }
                resourceSet.getResources().clear();
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed to close ResourceSet", e);
            throw e; // Re-throw ResourceSet cleanup failures
        }
    }
}