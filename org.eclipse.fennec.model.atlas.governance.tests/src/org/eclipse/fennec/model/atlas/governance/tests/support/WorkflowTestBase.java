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
package org.eclipse.fennec.model.atlas.governance.tests.support;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.fennec.model.atlas.governance.workflow.PostReleaseActionService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.util.promise.PromiseFactory;

/**
 * Base test class that provides common mock services for workflow tests.
 * 
 * <p>This class automatically registers a mock PostReleaseActionService
 * to satisfy the mandatory dependency in EObjectWorkflowService.</p>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 */
public abstract class WorkflowTestBase {
    
    protected PostReleaseActionService mockPostReleaseActionService;
    protected ServiceRegistration<PostReleaseActionService> postReleaseServiceRegistration;
    protected BundleContext bundleContext;
    
    @BeforeEach
    void setUpMockServices() {
        // Get bundle context - this should be injected by the test framework
        bundleContext = getBundleContext();
        
        // Create mock PostReleaseActionService
        mockPostReleaseActionService = mock(PostReleaseActionService.class);
        
        // Configure mock behavior
        PromiseFactory promiseFactory = new PromiseFactory(null);
        when(mockPostReleaseActionService.executePostReleaseActions(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(promiseFactory.resolved(null));
        when(mockPostReleaseActionService.executePostUnreleaseActions(anyString(), anyString(), anyString(), anyString()))
            .thenReturn(promiseFactory.resolved(null));
        when(mockPostReleaseActionService.supportsObjectType(anyString()))
            .thenReturn(true);
        when(mockPostReleaseActionService.getLastActionInfo(anyString()))
            .thenReturn(null);
        
        // Register mock service
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put("service.ranking", Integer.valueOf(1000)); // High ranking to override real service
        properties.put("test.mock", Boolean.TRUE);
        
        postReleaseServiceRegistration = bundleContext.registerService(
            PostReleaseActionService.class, 
            mockPostReleaseActionService, 
            properties
        );
    }
    
    @AfterEach
    void tearDownMockServices() {
        if (postReleaseServiceRegistration != null) {
            try {
                postReleaseServiceRegistration.unregister();
            } catch (IllegalStateException e) {
                // Service already unregistered - ignore
            }
            postReleaseServiceRegistration = null;
        }
        mockPostReleaseActionService = null;
    }
    
    /**
     * Subclasses must provide access to the BundleContext.
     * This is typically injected by the OSGi test framework.
     * 
     * @return the bundle context for service registration
     */
    protected abstract BundleContext getBundleContext();
    
    /**
     * Helper method to verify that post-release actions were called.
     * Subclasses can use this for verification in their tests.
     * 
     * @return the mock service for verification
     */
    protected PostReleaseActionService getPostReleaseActionServiceMock() {
        return mockPostReleaseActionService;
    }
}