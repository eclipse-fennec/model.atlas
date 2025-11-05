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
package org.gecko.mac.governance.workflow.impl;

import static java.util.Objects.requireNonNull;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.gecko.mac.governance.registration.DynamicEPackageRegistrationService;
import org.gecko.mac.governance.workflow.PostReleaseActionService;
import org.gecko.mac.mgmt.api.EObjectStorageService;
import org.gecko.mac.mgmt.management.ObjectMetadata;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.promise.Deferred;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * Implementation of {@link PostReleaseActionService} specialized for EPackage objects.
 * 
 * <p>This service performs the following post-release actions for EPackages:</p>
 * <ul>
 * <li>Registers the EPackage in the OSGi EMF registry using {@link DynamicEPackageRegistrationService}</li>
 * <li>Makes the EPackage available to other EMF components</li>
 * <li>Tracks registration status for audit purposes</li>
 * </ul>
 * 
 * <p>The service supports reversible operations, allowing EPackages to be unregistered
 * when they are removed from production.</p>
 * 
 * <p><strong>Configuration:</strong></p>
 * <ul>
 * <li>Automatic file extension detection from EPackage metadata</li>
 * <li>Version extraction from ObjectMetadata properties</li>
 * <li>Graceful error handling that doesn't affect release workflow</li>
 * </ul>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 */
@Component(service = PostReleaseActionService.class, immediate = true)
public class EPackagePostReleaseActionService implements PostReleaseActionService {
    
    private static final Logger logger = Logger.getLogger(EPackagePostReleaseActionService.class.getName());
    
    @Reference
    private DynamicEPackageRegistrationService registrationService;
    
    @Reference(target = "(storage.role=release)")
    private EObjectStorageService<EPackage> releaseStorage;
    
    private final PromiseFactory promiseFactory = new PromiseFactory(null);
    private final Map<String, PostReleaseActionInfoImpl> actionHistory = new ConcurrentHashMap<>();
    
    @Override
    public Promise<Void> executePostReleaseActions(String objectId, String objectType, String releaseUser, String releaseNotes) {
        requireNonNull(objectId, "Object ID cannot be null");
        requireNonNull(objectType, "Object type cannot be null");
        requireNonNull(releaseUser, "Release user cannot be null");
        
        // Only handle EPackage objects
        if (!EcorePackage.Literals.EPACKAGE.getName().equals(objectType)) {
            logger.fine("Skipping post-release actions for non-EPackage object: " + objectType);
            return promiseFactory.resolved(null);
        }
        
        logger.info("Executing post-release actions for EPackage: " + objectId);
        
        Deferred<Void> deferred = promiseFactory.deferred();
        
        // Execute asynchronously to avoid blocking the release workflow
        executeAsync(() -> {
            try {
                PostReleaseActionInfoImpl actionInfo = new PostReleaseActionInfoImpl(objectId, releaseUser);
                actionHistory.put(objectId, actionInfo);
                
                // 1. Load the released EPackage
                EPackage ePackage = loadEPackage(objectId);
                if (ePackage == null) {
                    String error = "Failed to load EPackage from release storage: " + objectId;
                    actionInfo.markFailed(error);
                    logger.warning(error);
                    deferred.fail(new IllegalStateException(error));
                    return;
                }
                
                // 2. Extract metadata for registration
                ObjectMetadata metadata = getPromiseValue(releaseStorage.retrieveMetadata(objectId));
                String fileExtension = extractFileExtension(metadata, ePackage);
                String version = extractVersion(metadata, ePackage);
                
                // 3. Register EPackage in OSGi EMF registry
                boolean registered = registrationService.registerEPackage(ePackage, fileExtension, version);
                if (!registered) {
                    String error = "Failed to register EPackage in OSGi EMF registry: " + ePackage.getNsURI();
                    actionInfo.markFailed(error);
                    logger.warning(error);
                    deferred.fail(new IllegalStateException(error));
                    return;
                }
                
                // 4. Mark as successful
                String details = String.format("Registered EPackage: nsURI=%s, name=%s, version=%s, fileExt=%s", 
                                              ePackage.getNsURI(), ePackage.getName(), version, fileExtension);
                actionInfo.markSuccessful(details);
                
                logger.info("Successfully completed post-release actions for EPackage: " + objectId + 
                           " (nsURI: " + ePackage.getNsURI() + ")");
                deferred.resolve(null);
                
            } catch (Exception e) {
                String error = "Post-release actions failed for EPackage: " + objectId;
                logger.log(Level.SEVERE, error, e);
                
                PostReleaseActionInfoImpl actionInfo = actionHistory.get(objectId);
                if (actionInfo != null) {
                    actionInfo.markFailed(error + ": " + e.getMessage());
                }
                
                deferred.fail(e);
            }
        });
        
        return deferred.getPromise();
    }
    
    @Override
    public Promise<Void> executePostUnreleaseActions(String objectId, String objectType, String unreleaseUser, String unreleaseReason) {
        requireNonNull(objectId, "Object ID cannot be null");
        requireNonNull(objectType, "Object type cannot be null");
        requireNonNull(unreleaseUser, "Unrelease user cannot be null");
        
        // Only handle EPackage objects
        if (!"EPackage".equals(objectType)) {
            return promiseFactory.resolved(null);
        }
        
        logger.info("Executing post-unrelease actions for EPackage: " + objectId);
        
        Deferred<Void> deferred = promiseFactory.deferred();
        
        executeAsync(() -> {
            try {
                // Find the EPackage namespace URI from action history or try to load it
                PostReleaseActionInfoImpl actionInfo = actionHistory.get(objectId);
                String namespaceURI = null;
                
                if (actionInfo != null && actionInfo.isSuccessful()) {
                    // Try to extract namespace URI from action details
                    String details = actionInfo.getActionDetails();
                    if (details != null && details.contains("nsURI=")) {
                        int start = details.indexOf("nsURI=") + 6;
                        int end = details.indexOf(",", start);
                        if (end > start) {
                            namespaceURI = details.substring(start, end);
                        }
                    }
                }
                
                // If we couldn't get it from history, try to load the EPackage
                if (namespaceURI == null) {
                    try {
                        EPackage ePackage = loadEPackage(objectId);
                        if (ePackage != null) {
                            namespaceURI = ePackage.getNsURI();
                        }
                    } catch (Exception e) {
                        logger.log(Level.WARNING, "Could not load EPackage for unregistration: " + objectId, e);
                    }
                }
                
                if (namespaceURI == null) {
                    logger.warning("Cannot determine namespace URI for EPackage unregistration: " + objectId);
                    deferred.resolve(null); // Don't fail the unrelease process
                    return;
                }
                
                // Unregister the EPackage
                boolean unregistered = registrationService.unregisterEPackage(namespaceURI);
                if (unregistered) {
                    logger.info("Successfully unregistered EPackage: " + namespaceURI);
                } else {
                    logger.warning("EPackage was not registered or unregistration failed: " + namespaceURI);
                }
                
                // Remove from action history
                actionHistory.remove(objectId);
                
                deferred.resolve(null);
                
            } catch (Exception e) {
                logger.log(Level.WARNING, "Post-unrelease actions failed for EPackage: " + objectId, e);
                deferred.resolve(null); // Don't fail the unrelease process
            }
        });
        
        return deferred.getPromise();
    }
    
    @Override
    public boolean supportsObjectType(String objectType) {
        return "EPackage".equals(objectType);
    }
    
    @Override
    public PostReleaseActionInfo getLastActionInfo(String objectId) {
        return actionHistory.get(objectId);
    }
    
    // Private helper methods
    
    private EPackage loadEPackage(String objectId) {
        try {
            return getPromiseValue(releaseStorage.retrieveObject(objectId));
        } catch (Exception e) {
            logger.log(Level.WARNING, "Failed to load EPackage: " + objectId, e);
            return null;
        }
    }
    
    private String extractFileExtension(ObjectMetadata metadata, EPackage ePackage) {
        // Try to get from metadata properties first
        if (metadata.getProperties() != null) {
            String fileExt = (String) metadata.getProperties().get("file.extension");
            if (fileExt != null && !fileExt.trim().isEmpty()) {
                return fileExt.startsWith(".") ? fileExt.substring(1) : fileExt;
            }
        }
        
        // Fall back to EPackage name or default
        String name = ePackage.getName();
        return (name != null && !name.isEmpty()) ? name.toLowerCase() : "ecore";
    }
    
    private String extractVersion(ObjectMetadata metadata, EPackage ePackage) {
        // Try to get from metadata properties first
        if (metadata.getProperties() != null) {
            String version = (String) metadata.getProperties().get("version");
            if (version != null && !version.trim().isEmpty()) {
                return version;
            }
        }
        
        // Try to extract from EPackage name if it contains version info
        String name = ePackage.getName();
        if (name != null && name.matches(".*[vV]\\d+.*")) {
            // Simple extraction of version pattern
            return "1.0"; // Could be more sophisticated
        }
        
        return "1.0";
    }
    
    private void executeAsync(Runnable task) {
        // Use a simple async execution - in a real implementation you might want
        // to use a proper thread pool or OSGi's async capabilities
        new Thread(task, "PostReleaseAction-" + System.currentTimeMillis()).start();
    }
    
    private static <T> T getPromiseValue(Promise<T> promise) {
        try {
            return promise.getValue();
        } catch (Exception e) {
            throw new RuntimeException("Promise failed", e);
        }
    }
    
    // Implementation of PostReleaseActionInfo
    private static class PostReleaseActionInfoImpl implements PostReleaseActionInfo {
        private final String objectId;
        private final String executionUser;
        private final Instant executionTime;
        private boolean successful;
        private String errorMessage;
        private String actionDetails;
        
        public PostReleaseActionInfoImpl(String objectId, String executionUser) {
            this.objectId = objectId;
            this.executionUser = executionUser;
            this.executionTime = Instant.now();
            this.successful = false;
        }
        
        public void markSuccessful(String details) {
            this.successful = true;
            this.actionDetails = details;
            this.errorMessage = null;
        }
        
        public void markFailed(String error) {
            this.successful = false;
            this.errorMessage = error;
            this.actionDetails = null;
        }
        
        @Override
        public String getObjectId() {
            return objectId;
        }
        
        @Override
        public Instant getExecutionTime() {
            return executionTime;
        }
        
        @Override
        public boolean isSuccessful() {
            return successful;
        }
        
        @Override
        public String getErrorMessage() {
            return errorMessage;
        }
        
        @Override
        public String getExecutionUser() {
            return executionUser;
        }
        
        @Override
        public String getActionDetails() {
            return actionDetails;
        }
    }
}