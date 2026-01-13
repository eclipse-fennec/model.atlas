/**
 * Copyright (c) 2012 - 2026 Data In Motion and others.
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
package org.eclipse.fennec.model.atlas.workflow.tests;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.gecko.emf.osgi.annotation.require.RequireEMF;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.Property.Type;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * OSGi Integration tests for {@link RegistryService}.
 *
 * Tests the stage parsing, validation, and storage routing logic of
 * RegistryService with mocked storage in a real OSGi container environment.
 *
 * @author Claude
 * @since Jan 13, 2026
 */
@RequireEMF
@RequireConfigurationAdmin
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@DisplayName("RegistryService OSGi Integration Tests")
public class RegistryServiceIntegrationTest {

    private static final String REGISTRY_NAME = "test-registry";
    private static final String SCOPE_NAME = "test-scope";

    @TempDir
    Path tempDir;

    @InjectBundleContext
    BundleContext bundleContext;

    @SuppressWarnings("rawtypes")
    private ServiceRegistration<EObjectStorageService> mockStorageRegistration;
    private EObjectStorageService<EObject> mockStorageService;

    @SuppressWarnings("rawtypes")
    private ServiceRegistration<EObjectRegistryService> mockRegistryServiceRegistration;
    private EObjectRegistryService<EObject> mockRegistryService;

    private PromiseFactory promiseFactory;

    @BeforeEach
    void setUp() {
        promiseFactory = new PromiseFactory(null);

        // Create mock storage service
        @SuppressWarnings("unchecked")
        EObjectStorageService<EObject> storageMock = mock(EObjectStorageService.class);
        mockStorageService = storageMock;

        // Register mock storage service
        Dictionary<String, Object> storageProps = new Hashtable<>();
        storageProps.put("storage.type", "mock");
        storageProps.put("service.ranking", Integer.valueOf(1000));

        mockStorageRegistration = bundleContext.registerService(
            EObjectStorageService.class,
            mockStorageService,
            storageProps
        );

        // Create mock registry service
        @SuppressWarnings("unchecked")
        EObjectRegistryService<EObject> registryMock = mock(EObjectRegistryService.class);
        mockRegistryService = registryMock;

        // Register mock registry service
        Dictionary<String, Object> registryProps = new Hashtable<>();
        registryProps.put("service.ranking", Integer.valueOf(1000));

        mockRegistryServiceRegistration = bundleContext.registerService(
            EObjectRegistryService.class,
            mockRegistryService,
            registryProps
        );
    }

    @AfterEach
    void tearDown() {
        if (mockStorageRegistration != null) {
            try {
                mockStorageRegistration.unregister();
            } catch (IllegalStateException e) {
                // Already unregistered
            }
        }
        if (mockRegistryServiceRegistration != null) {
            try {
                mockRegistryServiceRegistration.unregister();
            } catch (IllegalStateException e) {
                // Already unregistered
            }
        }
    }

    @Nested
    @DisplayName("Stage Parsing Tests")
    class StageParsingTests {

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should parse simple stages from configuration")
        @WithFactoryConfiguration(factoryPid = "RegistryService", name = "test-registry", location = "?", properties = {
            @Property(key = "registry.name", value = REGISTRY_NAME),
            @Property(key = "stages", type = Type.Array, value = {
                "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                "{ \"name\" : \"approved\", \"writable\" : false, \"final\": false}",
                "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}",
            }),
            @Property(key = "workflow.transitions", type = Type.Array, value = {
                "draft:approved",
                "approved:release"
            }),
            @Property(key = "stage.storage.mappings", type = Type.Array, value = {
                "draft:mock",
                "approved:mock",
                "release:mock"
            }),
            @Property(key = "storageService.target", value = "(storage.type=mock)")
        })
        void shouldParseSimpleStages(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME + ")")
                ServiceAware<RegistryService> registryAware) throws InterruptedException, InvocationTargetException {

            // Wait for service
            RegistryService<EObject> registryService = registryAware.waitForService(5000);
            assertNotNull(registryService, "RegistryService should be available");

            // Verify stages are parsed correctly
            assertTrue(registryService.isValidStage("draft"), "draft should be a valid stage");
            assertTrue(registryService.isValidStage("approved"), "approved should be a valid stage");
            assertTrue(registryService.isValidStage("release"), "release should be a valid stage");
            assertFalse(registryService.isValidStage("invalid"), "invalid should not be a valid stage");

            // Verify registry name
            assertEquals(REGISTRY_NAME, registryService.getRegistryName());
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should parse writable flags correctly")
        @WithFactoryConfiguration(factoryPid = "RegistryService", name = "test-registry", location = "?", properties = {
            @Property(key = "registry.name", value = REGISTRY_NAME),
            @Property(key = "stages", type = Type.Array, value = {
                    "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                    "{ \"name\" : \"approved\", \"writable\" : true, \"final\": false}",
                    "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}",
                }),
            @Property(key = "workflow.transitions", type = Type.Array, value = {
                "draft:approved",
                "approved:release"
            }),
            @Property(key = "stage.storage.mappings", type = Type.Array, value = {
                "draft:mock",
                "approved:mock",
                "release:mock"
            }),
            @Property(key = "storageService.target", value = "(storage.type=mock)")
        })
        void shouldParseWritableFlags(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME + ")")
                ServiceAware<RegistryService> registryAware) throws InterruptedException, InvocationTargetException {

            RegistryService<EObject> registryService = registryAware.waitForService(5000);
            assertNotNull(registryService);

            // Verify writable flags
            assertTrue(registryService.isWritableStage("draft"), "draft should be writable");
            assertTrue(registryService.isWritableStage("approved"), "approved should be writable");
            assertFalse(registryService.isWritableStage("release"), "release should not be writable");
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should parse final stage flag correctly")
        @WithFactoryConfiguration(factoryPid = "RegistryService", name = "test-registry", location = "?", properties = {
            @Property(key = "registry.name", value = REGISTRY_NAME),
            @Property(key = "stages", type = Type.Array, value = {
                    "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                    "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}",
                }),
            @Property(key = "workflow.transitions", type = Type.Array, value = {
                "draft:release"
            }),
            @Property(key = "stage.storage.mappings", type = Type.Array, value = {
                "draft:mock",
                "release:mock"
            }),
            @Property(key = "storageService.target", value = "(storage.type=mock)")
        })
        void shouldParseFinalStageFlag(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME + ")")
                ServiceAware<RegistryService> registryAware) throws InterruptedException, InvocationTargetException {

            RegistryService<EObject> registryService = registryAware.waitForService(5000);
            assertNotNull(registryService);

            // Final stage should not be writable in this config
            assertFalse(registryService.isFinalStageWritable(), "Final stage should not be writable");
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should handle writable final stage")
        @WithFactoryConfiguration(factoryPid = "RegistryService", name = "test-registry", location = "?", properties = {
            @Property(key = "registry.name", value = REGISTRY_NAME),
            @Property(key = "stages", type = Type.Array, value = {
                    "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                    "{ \"name\" : \"release\", \"writable\" : true, \"final\": true}",
                }),
            @Property(key = "workflow.transitions", type = Type.Array, value = {
                "draft:release"
            }),
            @Property(key = "stage.storage.mappings", type = Type.Array, value = {
                "draft:mock",
                "release:mock"
            }),
            @Property(key = "storageService.target", value = "(storage.type=mock)")
        })
        void shouldHandleWritableFinalStage(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME + ")")
                ServiceAware<RegistryService> registryAware) throws InterruptedException, InvocationTargetException {

            RegistryService<EObject> registryService = registryAware.waitForService(5000);
            assertNotNull(registryService);

            // Final stage should be writable in this config
            assertTrue(registryService.isFinalStageWritable(), "Final stage should be writable");
        }
    }

    @Nested
    @DisplayName("Transition Parsing and Validation Tests")
    class TransitionTests {

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should parse workflow transitions correctly")
        @WithFactoryConfiguration(factoryPid = "RegistryService", name = "test-registry", location = "?", properties = {
            @Property(key = "registry.name", value = REGISTRY_NAME),
            @Property(key = "stages", type = Type.Array, value = {
                    "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                    "{ \"name\" : \"approved\", \"writable\" : true, \"final\": false}",
                    "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}",
                }),
            @Property(key = "workflow.transitions", type = Type.Array, value = {
                "draft:approved",
                "approved:release"
            }),
            @Property(key = "stage.storage.mappings", type = Type.Array, value = {
                "draft:mock",
                "approved:mock",
                "release:mock"
            }),
            @Property(key = "storageService.target", value = "(storage.type=mock)")
        })
        void shouldParseWorkflowTransitions(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME + ")")
                ServiceAware<RegistryService> registryAware) throws InterruptedException, InvocationTargetException {

            RegistryService<EObject> registryService = registryAware.waitForService(5000);
            assertNotNull(registryService);

            // Verify allowed transitions
            assertTrue(registryService.isTransitionAllowed("draft", "approved"),
                "Transition draft→approved should be allowed");
            assertTrue(registryService.isTransitionAllowed("approved", "release"),
                "Transition approved→release should be allowed");

            // Verify disallowed transitions
            assertFalse(registryService.isTransitionAllowed("draft", "release"),
                "Transition draft→release should not be allowed (not configured)");
            assertFalse(registryService.isTransitionAllowed("release", "draft"),
                "Backward transition should not be allowed");
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should support non-linear workflows")
        @WithFactoryConfiguration(factoryPid = "RegistryService", name = "test-registry", location = "?", properties = {
            @Property(key = "registry.name", value = REGISTRY_NAME),
            @Property(key = "stages", type = Type.Array, value = {
                    "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                    "{ \"name\" : \"approved\", \"writable\" : true, \"final\": false}",
                    "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}",
                }),
            @Property(key = "workflow.transitions", type = Type.Array, value = {
                "draft:approved",
                "draft:release",  // Allow skipping approved
                "approved:release"
            }),
            @Property(key = "stage.storage.mappings", type = Type.Array, value = {
                "draft:mock",
                "approved:mock",
                "release:mock"
            }),
            @Property(key = "storageService.target", value = "(storage.type=mock)")
        })
        void shouldSupportNonLinearWorkflows(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME + ")")
                ServiceAware<RegistryService> registryAware) throws InterruptedException, InvocationTargetException {

            RegistryService<EObject> registryService = registryAware.waitForService(5000);
            assertNotNull(registryService);

            // Verify both paths are allowed
            assertTrue(registryService.isTransitionAllowed("draft", "approved"),
                "Normal path should be allowed");
            assertTrue(registryService.isTransitionAllowed("draft", "release"),
                "Fast-track path should be allowed");
            assertTrue(registryService.isTransitionAllowed("approved", "release"),
                "Completion path should be allowed");
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should handle multiple transitions from same stage")
        @WithFactoryConfiguration(factoryPid = "RegistryService", name = "test-registry", location = "?", properties = {
            @Property(key = "registry.name", value = REGISTRY_NAME),
            @Property(key = "stages", type = Type.Array, value = {
                    "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                    "{ \"name\" : \"approved\", \"writable\" : true, \"final\": false}",
                    "{ \"name\" : \"rejected\", \"writable\" : true, \"final\": false}",
                    "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}",
                }),
            @Property(key = "workflow.transitions", type = Type.Array, value = {
                "draft:approved",
                "approved:release",
                "approved:rejected"  // Approval can lead to rejection
            }),
            @Property(key = "stage.storage.mappings", type = Type.Array, value = {
                "draft:mock",
                "approved:mock",
                "rejected:mock",
                "release:mock"
            }),
            @Property(key = "storageService.target", value = "(storage.type=mock)")
        })
        void shouldHandleMultipleTransitionsFromSameStage(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME + ")")
                ServiceAware<RegistryService> registryAware) throws InterruptedException, InvocationTargetException {

            RegistryService<EObject> registryService = registryAware.waitForService(5000);
            assertNotNull(registryService);

            // Verify multiple paths from approved
            assertTrue(registryService.isTransitionAllowed("approved", "release"),
                "approved→release should be allowed");
            assertTrue(registryService.isTransitionAllowed("approved", "rejected"),
                "approved→rejected should be allowed");
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should reject configuration with multiple final stages")
        @WithFactoryConfiguration(factoryPid = "RegistryService", name = "test-registry-invalid", location = "?", properties = {
            @Property(key = "registry.name", value = "invalid-registry"),
            @Property(key = "stages", type = Type.Array, value = {
                    "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                    "{ \"name\" : \"rejected\", \"writable\" : false, \"final\": true}",
                    "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}",
                }),
            @Property(key = "workflow.transitions", type = Type.Array, value = {
                "draft:rejected",
                "draft:release"
            }),
            @Property(key = "stage.storage.mappings", type = Type.Array, value = {
                "draft:mock",
                "rejected:mock",
                "release:mock"
            }),
            @Property(key = "storageService.target", value = "(storage.type=mock)")
        })
        void shouldRejectMultipleFinalStages(
                @InjectService(cardinality = 0, filter = "(registry.name=invalid-registry)", timeout = 2000)
                ServiceAware<RegistryService> registryAware) throws InterruptedException {

            // Service should not become available due to configuration error
            // The activation should fail when trying to parse stages with multiple final stages
            RegistryService<EObject> registryService = registryAware.waitForService(2000);
            assertNull(registryService, "RegistryService should not be available with multiple final stages");
        }
    }

    @Nested
    @DisplayName("Stage Validation Tests")
    class StageValidationTests {

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should throw exception for invalid stage in upload")
        @WithFactoryConfiguration(factoryPid = "RegistryService", name = "test-registry", location = "?", properties = {
            @Property(key = "registry.name", value = REGISTRY_NAME),
            @Property(key = "stages", type = Type.Array, value = {
                    "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                    "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}",
                }),
            @Property(key = "workflow.transitions", type = Type.Array, value = {
                "draft:release"
            }),
            @Property(key = "stage.storage.mappings", type = Type.Array, value = {
                "draft:mock",
                "release:mock"
            }),
            @Property(key = "storageService.target", value = "(storage.type=mock)")
        })
        void shouldThrowExceptionForInvalidStage(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME + ")")
                ServiceAware<RegistryService> registryAware) throws InterruptedException, InvocationTargetException {

            RegistryService<EObject> registryService = registryAware.waitForService(5000);
            assertNotNull(registryService);

            EObject testObject = EcoreFactory.eINSTANCE.createEClass();
            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setObjectName("test");

            // Should throw exception for invalid stage
            assertThrows(Exception.class, () -> {
                try {
                    registryService.uploadToStage(SCOPE_NAME, "invalid-stage", testObject, metadata).getValue();
                } catch (Exception e) {
                    assertTrue(e.getMessage().contains("not a valid stage"),
                        "Exception should mention invalid stage");
                    throw e;
                }
            });
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should throw exception for non-writable stage in update")
        @WithFactoryConfiguration(factoryPid = "RegistryService", name = "test-registry", location = "?", properties = {
            @Property(key = "registry.name", value = REGISTRY_NAME),
            @Property(key = "stages", type = Type.Array, value = {
                    "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                    "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}",
                }),
            @Property(key = "workflow.transitions", type = Type.Array, value = {
                "draft:release"
            }),
            @Property(key = "stage.storage.mappings", type = Type.Array, value = {
                "draft:mock",
                "release:mock"
            }),
            @Property(key = "storageService.target", value = "(storage.type=mock)")
        })
        void shouldThrowExceptionForNonWritableStage(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME + ")")
                ServiceAware<RegistryService> registryAware) throws InterruptedException, InvocationTargetException {

            RegistryService<EObject> registryService = registryAware.waitForService(5000);
            assertNotNull(registryService);

            EObject testObject = EcoreFactory.eINSTANCE.createEClass();

            // Should throw exception for non-writable stage
            assertThrows(Exception.class, () -> {
                try {
                    registryService.updateInStage(SCOPE_NAME, "release", testObject, "test-id", "1.0").getValue();
                } catch (Exception e) {
                    assertTrue(e.getMessage().contains("not a writable stage"),
                        "Exception should mention non-writable stage");
                    throw e;
                }
            });
        }
    }

    @Nested
    @DisplayName("Basic CRUD Operations Tests")
    class CRUDOperationsTests {

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should upload object to valid writable stage")
        @WithFactoryConfiguration(factoryPid = "RegistryService", name = "test-registry", location = "?", properties = {
            @Property(key = "registry.name", value = REGISTRY_NAME),
            @Property(key = "stages", type = Type.Array, value = {
                    "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                    "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}",
                }),
            @Property(key = "workflow.transitions", type = Type.Array, value = {
                "draft:release"
            }),
            @Property(key = "stage.storage.mappings", type = Type.Array, value = {
                "draft:mock",
                "release:mock"
            }),
            @Property(key = "storageService.target", value = "(storage.type=mock)")
        })
        void shouldUploadObjectToValidWritableStage(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME + ")")
                ServiceAware<RegistryService> registryAware) throws InterruptedException, InvocationTargetException {

            RegistryService<EObject> registryService = registryAware.waitForService(5000);
            assertNotNull(registryService);

            EObject testObject = EcoreFactory.eINSTANCE.createEClass();
            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setObjectName("test-object");
            metadata.setObjectId("test-id");

            ObjectMetadata resultMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            resultMetadata.setObjectId("test-id");

            Promise<ObjectMetadata> promise = promiseFactory.resolved(resultMetadata);
            when(mockStorageService.storeObject(anyString(), any(), any())).thenReturn(promise);

            // Should succeed
            Promise<ObjectMetadata> result = registryService.uploadToStage(
                SCOPE_NAME, "draft", testObject, metadata
            );

            assertNotNull(result);
            ObjectMetadata uploaded = result.getValue();
            assertNotNull(uploaded);

            // Verify storage service was called
            verify(mockStorageService).storeObject(anyString(), any(), any());
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should retrieve metadata from stage")
        @WithFactoryConfiguration(factoryPid = "RegistryService", name = "test-registry", location = "?", properties = {
            @Property(key = "registry.name", value = REGISTRY_NAME),
            @Property(key = "stages", type = Type.Array, value = {
                    "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                    "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}",
                }),
            @Property(key = "workflow.transitions", type = Type.Array, value = {
                "draft:release"
            }),
            @Property(key = "stage.storage.mappings", type = Type.Array, value = {
                "draft:mock",
                "release:mock"
            }),
            @Property(key = "storageService.target", value = "(storage.type=mock)")
        })
        void shouldRetrieveMetadataFromStage(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME + ")")
                ServiceAware<RegistryService> registryAware) throws InterruptedException, InvocationTargetException {

            RegistryService<EObject> registryService = registryAware.waitForService(5000);
            assertNotNull(registryService);

            ObjectMetadata expectedMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            expectedMetadata.setObjectId("test-id");

            Promise<ObjectMetadata> promise = promiseFactory.resolved(expectedMetadata);
            when(mockStorageService.retrieveMetadata(anyString())).thenReturn(promise);

            // Retrieve metadata
            ObjectMetadata result = registryService.getMetadataFromStage(
                SCOPE_NAME, "draft", "test-id"
            );

            assertNotNull(result);
            assertEquals("test-id", result.getObjectId());

            verify(mockStorageService).retrieveMetadata("test-id");
        }
    }
}
