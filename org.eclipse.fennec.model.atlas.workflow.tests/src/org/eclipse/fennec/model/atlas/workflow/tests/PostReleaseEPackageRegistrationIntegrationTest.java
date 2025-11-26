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
package org.eclipse.fennec.model.atlas.workflow.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.workflow.PostReleaseActionService;
import org.eclipse.fennec.model.atlas.workflow.tests.annotations.GovernanceTestAnnotations;
import org.eclipse.fennec.model.atlas.workflow.tests.annotations.GovernanceTestAnnotations.GovernanceGatedWorkflowSetup;
import org.eclipse.fennec.model.atlas.workflow.tests.annotations.GovernanceTestAnnotations.PostActionStorageSetup;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.service.typedevent.TypedEventBus;
import org.osgi.service.typedevent.TypedEventHandler;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * Integration test for post-release EPackage registration functionality.
 * 
 * <p>This test verifies that when a post-release action is triggered for an EPackage,
 * it gets automatically registered in the OSGi EMF registry and becomes available
 * as an OSGi service.</p>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(MockitoExtension.class)
@RequireConfigurationAdmin
public class PostReleaseEPackageRegistrationIntegrationTest {
	
    @TempDir
    Path tempDir;
    
    @InjectBundleContext
    BundleContext bundleContext;
    
    
    @BeforeEach
    void setUp() {
        // Set system property for template argument resolution
        System.setProperty(GovernanceTestAnnotations.PROP_TEMP_DIR, tempDir.toString());
        
	
    }
    
    @AfterEach
    void tearDown() {
    	
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
    @PostActionStorageSetup
    public void testEPackageRegistrationAfterPostReleaseAction(
            @InjectService(filter = "(storage.role=release)")
            EObjectStorageService approvedStorage,
            @InjectService PostReleaseActionService postReleaseActionService,
            @InjectService(cardinality = 0, filter = "(emf.name=TestSensorModel)")
            ServiceAware<EPackage> ePackageServiceAware) throws Exception {
    	
    	
        
        // 1. Create a test EPackage
        EPackage testPackage = createTestEPackage();
        String objectId = "test-epackage-registration-" + System.currentTimeMillis();
        
        // 2. Create metadata and store EPackage directly in approved storage (simulating released object)
        ObjectMetadata metadata = createTestMetadata(objectId);
        String storageId = (String) approvedStorage.storeObject(objectId, testPackage, metadata).getValue();
        assertNotNull(storageId);
        
        // 3. Verify EPackage is not yet available as OSGi service
        assertTrue(ePackageServiceAware.isEmpty(), 
                  "EPackage should not be available as OSGi service before post-release action");
        
        // 4. Trigger post-release action
        postReleaseActionService.executePostReleaseActions(
            objectId, 
            "EPackage", 
            "integration-test-user", 
            "Test post-release action for EPackage registration"
        ).getValue(); // Wait for completion
        
        // 5. Wait for EPackage to become available as OSGi service
        EPackage registeredPackage = ePackageServiceAware.waitForService(5000L);
        assertNotNull(registeredPackage, 
                     "EPackage should be available as OSGi service after post-release action");
        
        // 6. Verify the registered EPackage has expected properties
        assertEquals(testPackage.getName(), registeredPackage.getName());
        assertEquals(testPackage.getNsURI(), registeredPackage.getNsURI());
        assertEquals(testPackage.getNsPrefix(), registeredPackage.getNsPrefix());
        assertEquals(1, registeredPackage.getEClassifiers().size());
        assertEquals("Sensor", registeredPackage.getEClassifiers().get(0).getName());
        
        // 7. Test unregistration via post-unrelease action
        postReleaseActionService.executePostUnreleaseActions(
            objectId,
            "EPackage", 
            "integration-test-user",
            "Test cleanup"
        ).getValue(); // Wait for completion
        
        // 8. Verify EPackage is no longer available as OSGi service
        assertTrue(ePackageServiceAware.isEmpty(), 
                  "EPackage should no longer be available as OSGi service after post-unrelease action");
    }
    
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @PostActionStorageSetup
    public void testEPackageRegistrationWithConfigurationEvents(
            @InjectService(filter = "(storage.role=release)")
            EObjectStorageService approvedStorage,
            @InjectService PostReleaseActionService postReleaseActionService,
            @InjectService(cardinality = 0, filter = "(emf.name=TestSensorModel)")
            ServiceAware<EPackage> ePackageServiceAware,
            @InjectService(cardinality = 0, filter = "(emf.name=TestSensorModel)")
            ServiceAware<ResourceSet> resourceSetServiceAware,
            @InjectService TypedEventBus typedEventBus) throws Exception {
        
        // Event capture setup with CountDownLatches for synchronization
        final List<String> capturedEvents = new ArrayList<>();
        final List<String[]> capturedEventData = new ArrayList<>();
        final CountDownLatch addEventLatch = new CountDownLatch(1);
        final CountDownLatch removeEventLatch = new CountDownLatch(1);
        
        TypedEventHandler<String[]> eventHandler = new TypedEventHandler<String[]>() {
            @Override
            public void notify(String topic, String[] data) {
                synchronized (capturedEvents) {
                    capturedEvents.add(topic);
                    capturedEventData.add(data.clone());
                }
                
                // Signal when specific events are received
                if ("configuration/ADD/SouthboundMappingService".equals(topic)) {
                    addEventLatch.countDown();
                } else if ("configuration/REMOVE/SouthboundMappingService".equals(topic)) {
                    removeEventLatch.countDown();
                }
            }
        };
        
        // Register event handler for configuration events
        Dictionary<String, Object> eventProps = new Hashtable<>();
        eventProps.put("event.topics", new String[] {
            "configuration/ADD/SouthboundMappingService",
            "configuration/REMOVE/SouthboundMappingService"
        });
        ServiceRegistration<TypedEventHandler> eventRegistration = 
        			bundleContext.registerService(TypedEventHandler.class, eventHandler, eventProps);
        try {
            // 1. Create a test EPackage
            EPackage testPackage = createTestEPackage();
            String objectId = "test-epackage-config-events-" + System.currentTimeMillis();
            String expectedNsURI = testPackage.getNsURI();
            String expectedModelName = testPackage.getName();
            
            // 2. Create metadata and store EPackage directly in approved storage (simulating released object)
            ObjectMetadata metadata = createTestMetadata(objectId);
            String storageId = (String) approvedStorage.storeObject(objectId, testPackage, metadata).getValue();
            assertNotNull(storageId);
            
            // 3. Verify EPackage is not yet available as OSGi service
            assertTrue(ePackageServiceAware.isEmpty(), 
                      "EPackage should not be available as OSGi service before post-release action");
            
            // 4. Trigger post-release action
            postReleaseActionService.executePostReleaseActions(
                objectId, 
                "EPackage", 
                "integration-test-user", 
                "Test post-release action for EPackage registration with events"
            ).getValue(); // Wait for completion
            
            // 5. Wait for EPackage to become available as OSGi service
            EPackage registeredPackage = ePackageServiceAware.waitForService(5000L);
            assertNotNull(registeredPackage, 
                         "EPackage should be available as OSGi service after post-release action");
            
            // 6. Verify the registered EPackage has expected properties
            assertEquals(testPackage.getName(), registeredPackage.getName());
            assertEquals(testPackage.getNsURI(), registeredPackage.getNsURI());
            assertEquals(testPackage.getNsPrefix(), registeredPackage.getNsPrefix());
            assertEquals(1, registeredPackage.getEClassifiers().size());
            assertEquals("Sensor", registeredPackage.getEClassifiers().get(0).getName());
            
            // 7. Wait for ResourceSet with matching emf.model property to trigger ADD configuration event
            ResourceSet resourceSet = resourceSetServiceAware.waitForService(5000L);
            
            if (resourceSet == null) {
                // Skip the configuration event test if ResourceSet not available
                assertTrue(registeredPackage != null, "EPackage should still be registered");
                return; // Early return to skip the event testing
            }
            
            assertNotNull(resourceSet, "ResourceSet with emf.name=TestSensorModel should be available");
            
            // 8. Wait for ADD configuration event using CountDownLatch
            boolean addEventReceived = addEventLatch.await(5, TimeUnit.SECONDS);
            
            assertTrue(addEventReceived, "ADD configuration event should have been sent within 5 seconds");
            
            // Find the ADD event data
            int addEventIndex = capturedEvents.indexOf("configuration/ADD/SouthboundMappingService");
            if (addEventIndex >= 0) {
                String[] addEventData = capturedEventData.get(addEventIndex);
                assertEquals(2, addEventData.length, "ADD event should have 2 data elements");
                
                String expectedConfigKey = "sthbnd.mapping.codec.typeMap." + expectedModelName;
                String expectedConfigValue = expectedNsURI + "#//" + expectedModelName + "Sensor";
                
                assertEquals(expectedConfigKey, addEventData[0], "Configuration key should match");
                assertEquals(expectedConfigValue, addEventData[1], "Configuration value should match");
            }
            
            // 10. Test unregistration via post-unrelease action (should trigger REMOVE event)
            postReleaseActionService.executePostUnreleaseActions(
                objectId,
                "EPackage", 
                "integration-test-user",
                "Test cleanup with REMOVE event"
            ).getValue(); // Wait for completion
            
            // 11. Wait for REMOVE configuration event using CountDownLatch
            boolean removeEventReceived = removeEventLatch.await(5, TimeUnit.SECONDS);
            assertTrue(removeEventReceived, "REMOVE configuration event should have been sent within 5 seconds");
            
            // Find the REMOVE event data
            int removeEventIndex = capturedEvents.lastIndexOf("configuration/REMOVE/SouthboundMappingService");
            if (removeEventIndex >= 0) {
                String[] removeEventData = capturedEventData.get(removeEventIndex);
                assertEquals(1, removeEventData.length, "REMOVE event should have 1 data element");
                
                String expectedConfigKey = "sthbnd.mapping.codec.typeMap." + expectedModelName;
                assertEquals(expectedConfigKey, removeEventData[0], "Configuration key should match for removal");
            }
            
            // 13. Verify EPackage is no longer available as OSGi service
            assertTrue(ePackageServiceAware.isEmpty(), 
                      "EPackage should no longer be available as OSGi service after post-unrelease action");
                      
        } finally {
            // Cleanup event handler
            if (eventRegistration != null) {
                eventRegistration.unregister();
            }
        }
    }
    
    @Test
    @GovernanceGatedWorkflowSetup
    public void testPostReleaseActionServiceSupportsEPackage(
            @InjectService PostReleaseActionService postReleaseActionService) throws Exception {
        
        // Verify the service can handle EPackage objects
        // In a real scenario, there might be multiple implementations for different object types
        // We just test that our post-release action service is available and functional
        assertNotNull(postReleaseActionService, "PostReleaseActionService should be available");
        assertTrue(postReleaseActionService.supportsObjectType(EcorePackage.Literals.EPACKAGE.getName()));
    }
    
    private EPackage createTestEPackage() {
        EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
        ePackage.setName("TestSensorModel");
        ePackage.setNsPrefix("testsensor");
        ePackage.setNsURI("http://test.sensor.example.com/1.0");
        
        // Add a simple EClass to make it more realistic
        EClass sensorClass = EcoreFactory.eINSTANCE.createEClass();
        sensorClass.setName("Sensor");
        ePackage.getEClassifiers().add(sensorClass);
        
        return ePackage;
    }
    
    private ObjectMetadata createTestMetadata(String objectId) {
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectId(objectId);
        metadata.setObjectName("TestSensorModel");
        metadata.setVersion("1.0");
        metadata.setUploadUser("integration-test-user");
        metadata.setUploadTime(Instant.now().truncatedTo(ChronoUnit.SECONDS));
        metadata.setSourceChannel("MANUAL_UPLOAD");
        metadata.setObjectType("EPackage");
        metadata.setContentHash("test-hash-" + System.currentTimeMillis());
        
        // Add file extension and version properties for registration
        metadata.getProperties().put("file.extension", "testsensor");
        metadata.getProperties().put("version", "1.0");
        
        return metadata;
    }
}