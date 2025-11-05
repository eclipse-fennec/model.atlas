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
package org.gecko.mac.governance.workflow;

import org.osgi.util.promise.Promise;

/**
 * Service interface for executing post-release actions after successful object release.
 * 
 * <p>This service is responsible for performing actions that should happen after
 * an object has been successfully released to production storage. Common post-release
 * actions include:</p>
 * 
 * <ul>
 * <li>EPackage registration in the OSGi EMF registry</li>
 * <li>Index updates for search functionality</li>
 * <li>Notification to external systems</li>
 * <li>Cache invalidation and warming</li>
 * <li>Audit trail creation</li>
 * </ul>
 * 
 * <p>The service uses asynchronous operations to avoid blocking the release workflow
 * and provides proper error handling for post-release failures.</p>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 */
public interface PostReleaseActionService {
    
    /**
     * Executes post-release actions for a released object.
     * 
     * <p>This method is called after successful object release and should perform
     * all necessary post-release actions asynchronously. The implementation should
     * be resilient to failures and not affect the main release workflow.</p>
     * 
     * @param objectId the ID of the released object
     * @param objectType the type of the released object (e.g., "EPackage", "Route")
     * @param releaseUser the user who released the object
     * @param releaseNotes optional release notes
     * @return Promise that resolves when all post-release actions are complete
     */
    Promise<Void> executePostReleaseActions(String objectId, String objectType, String releaseUser, String releaseNotes);
    
    /**
     * Executes post-unrelease actions when an object is removed from production.
     * 
     * <p>This method should reverse the post-release actions, such as unregistering
     * EPackages from the OSGi EMF registry or removing from external systems.</p>
     * 
     * @param objectId the ID of the object being unreleaseed
     * @param objectType the type of the object
     * @param unreleaseUser the user performing the unrelease action
     * @param unreleaseReason the reason for unreleasing
     * @return Promise that resolves when all post-unrelease actions are complete
     */
    Promise<Void> executePostUnreleaseActions(String objectId, String objectType, String unreleaseUser, String unreleaseReason);
    
    /**
     * Checks if post-release actions are supported for the given object type.
     * 
     * @param objectType the object type to check
     * @return true if post-release actions are supported, false otherwise
     */
    boolean supportsObjectType(String objectType);
    
    /**
     * Returns information about the last post-release action execution for an object.
     * 
     * @param objectId the object ID to check
     * @return information about the last execution, or null if no actions were executed
     */
    PostReleaseActionInfo getLastActionInfo(String objectId);
    
    /**
     * Information about post-release action execution.
     */
    interface PostReleaseActionInfo {
        
        /**
         * @return the object ID
         */
        String getObjectId();
        
        /**
         * @return the timestamp when actions were executed
         */
        java.time.Instant getExecutionTime();
        
        /**
         * @return true if all actions completed successfully
         */
        boolean isSuccessful();
        
        /**
         * @return error message if actions failed, null otherwise
         */
        String getErrorMessage();
        
        /**
         * @return the user who triggered the actions
         */
        String getExecutionUser();
        
        /**
         * @return details about which actions were performed
         */
        String getActionDetails();
    }
}