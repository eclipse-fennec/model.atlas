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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.workflow.ActionContext;
import org.eclipse.fennec.model.atlas.workflow.StageActionService;
import org.eclipse.fennec.model.atlas.workflow.StageActionService.ActionEvent;
import org.eclipse.fennec.model.atlas.workflow.StageActionService.ExitReason;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.ArgumentCaptor;
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

        mockStorageRegistration = bundleContext.registerService(EObjectStorageService.class, mockStorageService,
                storageProps);

        // Create mock registry service
        @SuppressWarnings("unchecked")
        EObjectRegistryService<EObject> registryMock = mock(EObjectRegistryService.class);
        mockRegistryService = registryMock;

        // Register mock registry service
        Dictionary<String, Object> registryProps = new Hashtable<>();
        registryProps.put("service.ranking", Integer.valueOf(1000));

        mockRegistryServiceRegistration = bundleContext.registerService(EObjectRegistryService.class,
                mockRegistryService, registryProps);

        when(mockStorageService.getStorageType()).thenReturn("mock");
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
                        "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}", }),
                @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:approved",
                        "approved:release" }),
                @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:mock", "approved:mock",
                        "release:mock" }),
                @Property(key = "storageService.target", value = "(storage.type=mock)") })
        void shouldParseSimpleStages(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME
                        + ")") ServiceAware<RegistryService> registryAware)
                throws InterruptedException, InvocationTargetException {

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
                        "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}", }),
                @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:approved",
                        "approved:release" }),
                @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:mock", "approved:mock",
                        "release:mock" }),
                @Property(key = "storageService.target", value = "(storage.type=mock)") })
        void shouldParseWritableFlags(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME
                        + ")") ServiceAware<RegistryService> registryAware)
                throws InterruptedException, InvocationTargetException {

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
                        "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}", }),
                @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
                @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:mock", "release:mock" }),
                @Property(key = "storageService.target", value = "(storage.type=mock)") })
        void shouldParseFinalStageFlag(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME
                        + ")") ServiceAware<RegistryService> registryAware)
                throws InterruptedException, InvocationTargetException {

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
                        "{ \"name\" : \"release\", \"writable\" : true, \"final\": true}", }),
                @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
                @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:mock", "release:mock" }),
                @Property(key = "storageService.target", value = "(storage.type=mock)") })
        void shouldHandleWritableFinalStage(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME
                        + ")") ServiceAware<RegistryService> registryAware)
                throws InterruptedException, InvocationTargetException {

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
                        "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}", }),
                @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:approved",
                        "approved:release" }),
                @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:mock", "approved:mock",
                        "release:mock" }),
                @Property(key = "storageService.target", value = "(storage.type=mock)") })
        void shouldParseWorkflowTransitions(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME
                        + ")") ServiceAware<RegistryService> registryAware)
                throws InterruptedException, InvocationTargetException {

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
                        "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}", }),
                @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:approved", "draft:release", // Allow
                                                                                                                        // skipping
                                                                                                                        // approved
                        "approved:release" }),
                @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:mock", "approved:mock",
                        "release:mock" }),
                @Property(key = "storageService.target", value = "(storage.type=mock)") })
        void shouldSupportNonLinearWorkflows(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME
                        + ")") ServiceAware<RegistryService> registryAware)
                throws InterruptedException, InvocationTargetException {

            RegistryService<EObject> registryService = registryAware.waitForService(5000);
            assertNotNull(registryService);

            // Verify both paths are allowed
            assertTrue(registryService.isTransitionAllowed("draft", "approved"), "Normal path should be allowed");
            assertTrue(registryService.isTransitionAllowed("draft", "release"), "Fast-track path should be allowed");
            assertTrue(registryService.isTransitionAllowed("approved", "release"), "Completion path should be allowed");
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
                        "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}", }),
                @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:approved",
                        "approved:release", "approved:rejected" // Approval can lead to rejection
                }),
                @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:mock", "approved:mock",
                        "rejected:mock", "release:mock" }),
                @Property(key = "storageService.target", value = "(storage.type=mock)") })
        void shouldHandleMultipleTransitionsFromSameStage(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME
                        + ")") ServiceAware<RegistryService> registryAware)
                throws InterruptedException, InvocationTargetException {

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
                        "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}", }),
                @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:rejected",
                        "draft:release" }),
                @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:mock", "rejected:mock",
                        "release:mock" }),
                @Property(key = "storageService.target", value = "(storage.type=mock)") })
        void shouldRejectMultipleFinalStages(
                @InjectService(cardinality = 0, filter = "(registry.name=invalid-registry)", timeout = 2000) ServiceAware<RegistryService> registryAware)
                throws InterruptedException {

            // Service should not become available due to configuration error
            // The activation should fail when trying to parse stages with multiple final
            // stages
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
                        "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}", }),
                @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
                @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:mock", "release:mock" }),
                @Property(key = "storageService.target", value = "(storage.type=mock)") })
        void shouldThrowExceptionForInvalidStage(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME
                        + ")") ServiceAware<RegistryService> registryAware)
                throws InterruptedException, InvocationTargetException {

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
                    assertTrue(e.getMessage().contains("not a valid stage"), "Exception should mention invalid stage");
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
                        "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}", }),
                @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
                @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:mock", "release:mock" }),
                @Property(key = "storageService.target", value = "(storage.type=mock)") })
        void shouldThrowExceptionForNonWritableStage(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME
                        + ")") ServiceAware<RegistryService> registryAware)
                throws InterruptedException, InvocationTargetException {

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
                        "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}", }),
                @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
                @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:mock", "release:mock" }),
                @Property(key = "storageService.target", value = "(storage.type=mock)") })
        void shouldUploadObjectToValidWritableStage(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME
                        + ")") ServiceAware<RegistryService> registryAware)
                throws InterruptedException, InvocationTargetException {

            RegistryService<EObject> registryService = registryAware.waitForService(5000);
            assertNotNull(registryService);

            EObject testObject = EcoreFactory.eINSTANCE.createEClass();
            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setObjectName("test-object");
            metadata.setObjectId("test-id");

            ObjectMetadata resultMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            resultMetadata.setObjectId("test-id");

            Promise<ObjectMetadata> promise = promiseFactory.resolved(resultMetadata);
            when(mockStorageService.storeObject(anyString(), anyString(), anyString(), anyString(), any(), any()))
                    .thenReturn(promise);

            // Should succeed
            Promise<ObjectMetadata> result = registryService.uploadToStage(SCOPE_NAME, "draft", testObject, metadata);

            assertNotNull(result);
            ObjectMetadata uploaded = result.getValue();
            assertNotNull(uploaded);

            // Verify storage service was called
            verify(mockStorageService).storeObject(anyString(), anyString(), anyString(), anyString(), any(), any());
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should retrieve metadata from stage")
        @WithFactoryConfiguration(factoryPid = "RegistryService", name = "test-registry", location = "?", properties = {
                @Property(key = "registry.name", value = REGISTRY_NAME),
                @Property(key = "stages", type = Type.Array, value = {
                        "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                        "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}", }),
                @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
                @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:mock", "release:mock" }),
                @Property(key = "storageService.target", value = "(storage.type=mock)") })
        void shouldRetrieveMetadataFromStage(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME
                        + ")") ServiceAware<RegistryService> registryAware)
                throws InterruptedException, InvocationTargetException {

            RegistryService<EObject> registryService = registryAware.waitForService(5000);
            assertNotNull(registryService);

            ObjectMetadata expectedMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            expectedMetadata.setObjectId("test-id");

            Promise<ObjectMetadata> promise = promiseFactory.resolved(expectedMetadata);
            when(mockStorageService.retrieveMetadata(anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(promise);

            // Retrieve metadata
            ObjectMetadata result = registryService.getMetadataFromStage(SCOPE_NAME, "draft", "test-id");

            assertNotNull(result);
            assertEquals("test-id", result.getObjectId());

            verify(mockStorageService).retrieveMetadata(SCOPE_NAME, REGISTRY_NAME, "draft", "test-id");
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should retrieve content from stage")
        @WithFactoryConfiguration(factoryPid = "RegistryService", name = "test-registry", location = "?", properties = {
                @Property(key = "registry.name", value = REGISTRY_NAME),
                @Property(key = "stages", type = Type.Array, value = {
                        "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                        "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}", }),
                @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
                @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:mock", "release:mock" }),
                @Property(key = "storageService.target", value = "(storage.type=mock)") })
        void shouldRetrieveContentFromStage(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME
                        + ")") ServiceAware<RegistryService> registryAware)
                throws InterruptedException, InvocationTargetException {

            RegistryService<EObject> registryService = registryAware.waitForService(5000);
            assertNotNull(registryService);

            EObject expectedObject = EcoreFactory.eINSTANCE.createEClass();
            Promise<EObject> promise = promiseFactory.resolved(expectedObject);
            when(mockStorageService.retrieveObject(anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(promise);

            // Retrieve content
            EObject result = registryService.getContentFromStage(SCOPE_NAME, "draft", "test-id");

            assertNotNull(result);
            verify(mockStorageService).retrieveObject(SCOPE_NAME, REGISTRY_NAME, "draft", "test-id");
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should update object in writable stage")
        @WithFactoryConfiguration(factoryPid = "RegistryService", name = "test-registry", location = "?", properties = {
                @Property(key = "registry.name", value = REGISTRY_NAME),
                @Property(key = "stages", type = Type.Array, value = {
                        "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                        "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}", }),
                @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
                @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:mock", "release:mock" }),
                @Property(key = "storageService.target", value = "(storage.type=mock)") })
        void shouldUpdateObjectInWritableStage(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME
                        + ")") ServiceAware<RegistryService> registryAware)
                throws InterruptedException, InvocationTargetException {

            RegistryService<EObject> registryService = registryAware.waitForService(5000);
            assertNotNull(registryService);

            EObject updatedObject = EcoreFactory.eINSTANCE.createEClass();

            // Mock retrieveMetadata to get current metadata
            ObjectMetadata currentMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            currentMetadata.setObjectId("test-id");
            currentMetadata.setVersion("1.0");
            Promise<ObjectMetadata> metadataPromise = promiseFactory.resolved(currentMetadata);
            when(mockStorageService.retrieveMetadata(anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(metadataPromise);

            // Mock updateObject for the update
            ObjectMetadata resultMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            resultMetadata.setObjectId("test-id");
            resultMetadata.setVersion("2.0");
            Promise<ObjectMetadata> updatePromise = promiseFactory.resolved(resultMetadata);
            when(mockStorageService.updateObject(anyString(), any(), any())).thenReturn(updatePromise);

            // Update object
            Promise<ObjectMetadata> result = registryService.updateInStage(SCOPE_NAME, "draft", updatedObject,
                    "test-id", "1.0");

            assertNotNull(result);
            ObjectMetadata updated = result.getValue();
            assertNotNull(updated);
            assertEquals("test-id", updated.getObjectId());

            // Verify both calls
            verify(mockStorageService).retrieveMetadata(SCOPE_NAME, REGISTRY_NAME, "draft", "test-id");
            verify(mockStorageService).updateObject(eq("test-id"), any(), any());
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should delete object from writable stage")
        @WithFactoryConfiguration(factoryPid = "RegistryService", name = "test-registry", location = "?", properties = {
                @Property(key = "registry.name", value = REGISTRY_NAME),
                @Property(key = "stages", type = Type.Array, value = {
                        "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                        "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}", }),
                @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
                @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:mock", "release:mock" }),
                @Property(key = "storageService.target", value = "(storage.type=mock)") })
        void shouldDeleteObjectFromWritableStage(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME
                        + ")") ServiceAware<RegistryService> registryAware)
                throws InterruptedException, InvocationTargetException {

            RegistryService<EObject> registryService = registryAware.waitForService(5000);
            assertNotNull(registryService);

            ObjectMetadata expectedMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            expectedMetadata.setObjectId("test-id");

            Promise<ObjectMetadata> promise = promiseFactory.resolved(expectedMetadata);
            when(mockStorageService.retrieveMetadata(anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(promise);

            Promise<Boolean> promise2 = promiseFactory.resolved(Boolean.TRUE);
            when(mockStorageService.deleteObject(anyString(), anyString(), anyString(), anyString()))
                    .thenReturn(promise2);

            // Delete object
            Promise<Boolean> result = registryService.deleteFromStage(SCOPE_NAME, "draft", "test-id");

            assertNotNull(result);
            assertTrue(result.getValue());

            verify(mockStorageService).deleteObject(SCOPE_NAME, REGISTRY_NAME, "draft", "test-id");
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should list objects in stage")
        @WithFactoryConfiguration(factoryPid = "RegistryService", name = "test-registry", location = "?", properties = {
                @Property(key = "registry.name", value = REGISTRY_NAME),
                @Property(key = "stages", type = Type.Array, value = {
                        "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                        "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}", }),
                @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
                @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:mock", "release:mock" }),
                @Property(key = "storageService.target", value = "(storage.type=mock)") })
        void shouldListObjectsInStage(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME
                        + ")") ServiceAware<RegistryService> registryAware)
                throws InterruptedException, InvocationTargetException {

            RegistryService<EObject> registryService = registryAware.waitForService(5000);
            assertNotNull(registryService);

            ObjectMetadata metadata1 = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata1.setObjectId("obj1");
            ObjectMetadata metadata2 = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata2.setObjectId("obj2");

            List<ObjectMetadata> expectedList = java.util.Arrays.asList(metadata1, metadata2);
            // listInStage uses registryService.findByScopeRegistryAndStage
            when(mockRegistryService.findByScopeRegistryAndStage(SCOPE_NAME, REGISTRY_NAME, "draft"))
                    .thenReturn(expectedList);

            // List objects
            List<ObjectMetadata> result = registryService.listInStage(SCOPE_NAME, "draft");

            assertNotNull(result);
            assertEquals(2, result.size());
            assertEquals("obj1", result.get(0).getObjectId());
            assertEquals("obj2", result.get(1).getObjectId());

            verify(mockRegistryService).findByScopeRegistryAndStage(SCOPE_NAME, REGISTRY_NAME, "draft");
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should list objects in stage by name")
        @WithFactoryConfiguration(factoryPid = "RegistryService", name = "test-registry", location = "?", properties = {
                @Property(key = "registry.name", value = REGISTRY_NAME),
                @Property(key = "stages", type = Type.Array, value = {
                        "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                        "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}", }),
                @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
                @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:mock", "release:mock" }),
                @Property(key = "storageService.target", value = "(storage.type=mock)") })
        void shouldListObjectsInStageByName(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME
                        + ")") ServiceAware<RegistryService> registryAware)
                throws InterruptedException, InvocationTargetException {

            RegistryService<EObject> registryService = registryAware.waitForService(5000);
            assertNotNull(registryService);

            ObjectMetadata metadata1 = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata1.setObjectId("obj1");
            metadata1.setObjectName("TestPackage");

            java.util.List<ObjectMetadata> expectedList = java.util.Arrays.asList(metadata1);
            // listInStageByName uses registryService.findByScopeRegistryStageAndName
            when(mockRegistryService.findByScopeRegistryStageAndName(SCOPE_NAME, REGISTRY_NAME, "draft", "TestPackage"))
                    .thenReturn(expectedList);

            // List objects by name
            java.util.List<ObjectMetadata> result = registryService.listInStageByName(SCOPE_NAME, "draft",
                    "TestPackage");

            assertNotNull(result);
            assertEquals(1, result.size());
            assertEquals("TestPackage", result.get(0).getObjectName());

            verify(mockRegistryService).findByScopeRegistryStageAndName(SCOPE_NAME, REGISTRY_NAME, "draft",
                    "TestPackage");
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should list all objects across all stages")
        @WithFactoryConfiguration(factoryPid = "RegistryService", name = "test-registry", location = "?", properties = {
                @Property(key = "registry.name", value = REGISTRY_NAME),
                @Property(key = "stages", type = Type.Array, value = {
                        "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                        "{ \"name\" : \"approved\", \"writable\" : true, \"final\": false}",
                        "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}", }),
                @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:approved",
                        "approved:release" }),
                @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:mock", "approved:mock",
                        "release:mock" }),
                @Property(key = "storageService.target", value = "(storage.type=mock)") })
        void shouldListAllObjectsAcrossAllStages(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME
                        + ")") ServiceAware<RegistryService> registryAware)
                throws InterruptedException, InvocationTargetException {

            RegistryService<EObject> registryService = registryAware.waitForService(5000);
            assertNotNull(registryService);

            ObjectMetadata draftObj = ManagementFactory.eINSTANCE.createObjectMetadata();
            draftObj.setObjectId("draft-obj");
            draftObj.setStage("draft");

            ObjectMetadata approvedObj = ManagementFactory.eINSTANCE.createObjectMetadata();
            approvedObj.setObjectId("approved-obj");
            approvedObj.setStage("approved");

            ObjectMetadata releaseObj = ManagementFactory.eINSTANCE.createObjectMetadata();
            releaseObj.setObjectId("release-obj");
            releaseObj.setStage("release");

            when(mockRegistryService.findByScopeRegistryAndStage(SCOPE_NAME, REGISTRY_NAME, "draft"))
                    .thenReturn(java.util.Arrays.asList(draftObj));
            when(mockRegistryService.findByScopeRegistryAndStage(SCOPE_NAME, REGISTRY_NAME, "approved"))
                    .thenReturn(java.util.Arrays.asList(approvedObj));
            when(mockRegistryService.findByScopeRegistryAndStage(SCOPE_NAME, REGISTRY_NAME, "release"))
                    .thenReturn(java.util.Arrays.asList(releaseObj));

            // List all objects across all stages
            List<ObjectMetadata> result = registryService.listAll(SCOPE_NAME);

            assertNotNull(result);
            assertEquals(3, result.size());
            assertTrue(result.stream().anyMatch(m -> "draft-obj".equals(m.getObjectId())));
            assertTrue(result.stream().anyMatch(m -> "approved-obj".equals(m.getObjectId())));
            assertTrue(result.stream().anyMatch(m -> "release-obj".equals(m.getObjectId())));

            verify(mockRegistryService).findByScopeRegistryAndStage(SCOPE_NAME, REGISTRY_NAME, "draft");
            verify(mockRegistryService).findByScopeRegistryAndStage(SCOPE_NAME, REGISTRY_NAME, "approved");
            verify(mockRegistryService).findByScopeRegistryAndStage(SCOPE_NAME, REGISTRY_NAME, "release");
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should return empty list when no objects in any stage")
        @WithFactoryConfiguration(factoryPid = "RegistryService", name = "test-registry", location = "?", properties = {
                @Property(key = "registry.name", value = REGISTRY_NAME),
                @Property(key = "stages", type = Type.Array, value = {
                        "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                        "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}", }),
                @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
                @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:mock", "release:mock" }),
                @Property(key = "storageService.target", value = "(storage.type=mock)") })
        void shouldReturnEmptyListWhenNoObjectsInAnyStage(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME
                        + ")") ServiceAware<RegistryService> registryAware)
                throws InterruptedException, InvocationTargetException {

            RegistryService<EObject> registryService = registryAware.waitForService(5000);
            assertNotNull(registryService);

            when(mockRegistryService.findByScopeRegistryAndStage(SCOPE_NAME, REGISTRY_NAME, "draft"))
                    .thenReturn(List.of());
            when(mockRegistryService.findByScopeRegistryAndStage(SCOPE_NAME, REGISTRY_NAME, "release"))
                    .thenReturn(List.of());

            List<ObjectMetadata> result = registryService.listAll(SCOPE_NAME);

            assertNotNull(result);
            assertTrue(result.isEmpty());
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should transition object between stages")
        @WithFactoryConfiguration(factoryPid = "RegistryService", name = "test-registry", location = "?", properties = {
                @Property(key = "registry.name", value = REGISTRY_NAME),
                @Property(key = "stages", type = Type.Array, value = {
                        "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                        "{ \"name\" : \"approved\", \"writable\" : true, \"final\": false}",
                        "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}", }),
                @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:approved",
                        "approved:release" }),
                @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:mock", "approved:mock",
                        "release:mock" }),
                @Property(key = "delete.after.transition", value = "true"),
                @Property(key = "storageService.target", value = "(storage.type=mock)") })
        void shouldTransitionObjectBetweenStages(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME
                        + ")") ServiceAware<RegistryService> registryAware)
                throws InterruptedException, InvocationTargetException {

            RegistryService<EObject> registryService = registryAware.waitForService(5000);
            assertNotNull(registryService);

            // Mock retrieving object from draft stage
            EObject testObject = EcoreFactory.eINSTANCE.createEClass();
            ObjectMetadata sourceMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            sourceMetadata.setObjectId("test-id");
            sourceMetadata.setObjectName("TestObject");

            Promise<EObject> objectPromise = promiseFactory.resolved(testObject);
            Promise<ObjectMetadata> metadataPromise = promiseFactory.resolved(sourceMetadata);
            when(mockStorageService.retrieveObject(anyString(), anyString(), eq("draft"), anyString()))
                    .thenReturn(objectPromise);
            when(mockStorageService.retrieveMetadata(anyString(), anyString(), eq("draft"), anyString()))
                    .thenReturn(metadataPromise);

            // Mock storing object to approved stage
            ObjectMetadata targetMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            targetMetadata.setObjectId("test-id");
            targetMetadata.setStage("approved");

            Promise<ObjectMetadata> storePromise = promiseFactory.resolved(targetMetadata);
            when(mockStorageService.storeObject(anyString(), anyString(), eq("approved"), anyString(), any(), any()))
                    .thenReturn(storePromise);

            // Mock deleting from draft stage
            Promise<Boolean> deletePromise = promiseFactory.resolved(Boolean.TRUE);
            when(mockStorageService.deleteObject(anyString(), anyString(), eq("draft"), anyString()))
                    .thenReturn(deletePromise);

            // Transition object
            ObjectMetadata result = registryService.transitionToStage(SCOPE_NAME, "test-id", "draft", "approved");

            assertNotNull(result);
            assertEquals("approved", result.getStage());

            // Verify the sequence of operations
            verify(mockStorageService).retrieveObject(SCOPE_NAME, REGISTRY_NAME, "draft", "test-id");
            verify(mockStorageService).retrieveMetadata(SCOPE_NAME, REGISTRY_NAME, "draft", "test-id");
            verify(mockStorageService).storeObject(eq(SCOPE_NAME), eq(REGISTRY_NAME), eq("approved"), eq("test-id"),
                    any(), any());
            verify(mockStorageService).deleteObject(SCOPE_NAME, REGISTRY_NAME, "draft", "test-id");
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should fail transition when not allowed")
        @WithFactoryConfiguration(factoryPid = "RegistryService", name = "test-registry", location = "?", properties = {
                @Property(key = "registry.name", value = REGISTRY_NAME),
                @Property(key = "stages", type = Type.Array, value = {
                        "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                        "{ \"name\" : \"approved\", \"writable\" : false, \"final\": false}",
                        "{ \"name\" : \"release\", \"writable\" : false, \"final\": true}", }),
                @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:approved",
                        "approved:release" }),
                @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:mock", "approved:mock",
                        "release:mock" }),
                @Property(key = "storageService.target", value = "(storage.type=mock)") })
        void shouldFailTransitionWhenNotAllowed(
                @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY_NAME
                        + ")") ServiceAware<RegistryService> registryAware)
                throws InterruptedException, InvocationTargetException {

            RegistryService<EObject> registryService = registryAware.waitForService(5000);
            assertNotNull(registryService);

            // Try to transition directly from draft to release (not allowed)
            assertThrows(Exception.class, () -> {
                registryService.transitionToStage(SCOPE_NAME, "test-id", "draft", "release");
            });
        }
    }

    @Nested
    @DisplayName("StageActionService Integration Tests")
    class StageActionServiceTests {

        private static final String STAGE_ACTION_REGISTRY_NAME = "stage-action-test-registry";

        private ServiceRegistration<StageActionService> mockStageActionRegistration;
        private StageActionService mockStageActionService;

        @BeforeEach
        void setUpStageActionService() {
            mockStageActionService = mock(StageActionService.class);

            // Subscribe only to the "release" stage so non-trigger dispatches are filtered
            // out upstream by RegistryServiceImpl#dispatch.
            when(mockStageActionService.supportsObjectType(anyString())).thenReturn(true);
            when(mockStageActionService.getTriggerStages()).thenReturn(Set.of("release"));
            when(mockStageActionService.getTriggerEvents())
                    .thenReturn(Set.of(ActionEvent.ENTER, ActionEvent.UPDATE, ActionEvent.EXIT));
            when(mockStageActionService.onEnter(any(ActionContext.class)))
                    .thenReturn(promiseFactory.resolved(null));
            when(mockStageActionService.onUpdate(any(ActionContext.class)))
                    .thenReturn(promiseFactory.resolved(null));
            when(mockStageActionService.onExit(any(ActionContext.class)))
                    .thenReturn(promiseFactory.resolved(null));

            Dictionary<String, Object> props = new Hashtable<>();
            props.put("service.ranking", Integer.valueOf(1000));

            mockStageActionRegistration = bundleContext.registerService(StageActionService.class,
                    mockStageActionService, props);
        }

        @AfterEach
        void tearDownStageActionService() {
            if (mockStageActionRegistration != null) {
                try {
                    mockStageActionRegistration.unregister();
                } catch (IllegalStateException e) {
                    // Already unregistered
                }
            }
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should invoke onEnter when uploading to a trigger stage")
        @WithFactoryConfiguration(factoryPid = "RegistryService", name = "stage-action-test", location = "?", properties = {
                @Property(key = "registry.name", value = STAGE_ACTION_REGISTRY_NAME),
                @Property(key = "stages", type = Type.Array, value = {
                        "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                        "{ \"name\" : \"release\", \"writable\" : true, \"final\": true}", }),
                @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
                @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:mock", "release:mock" }),
                @Property(key = "storageService.target", value = "(storage.type=mock)"),
                @Property(key = "stageActionService.target", value = "(service.ranking=1000)") })
        void shouldInvokeOnEnterOnUploadToTriggerStage(
                @InjectService(cardinality = 0, filter = "(registry.name=" + STAGE_ACTION_REGISTRY_NAME
                        + ")") ServiceAware<RegistryService> registryAware)
                throws InterruptedException, InvocationTargetException {

            RegistryService<EObject> registryService = registryAware.waitForService(5000);
            assertNotNull(registryService, "RegistryService should be available");

            EObject testObject = EcoreFactory.eINSTANCE.createEClass();
            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setObjectName("test-object");
            metadata.setObjectId("test-id");
            metadata.setObjectType("EClass");

            ObjectMetadata resultMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            resultMetadata.setObjectId("test-id");
            resultMetadata.setObjectType("EClass");

            Promise<ObjectMetadata> promise = promiseFactory.resolved(resultMetadata);
            when(mockStorageService.storeObject(anyString(), anyString(), anyString(), anyString(), any(), any()))
                    .thenReturn(promise);

            Promise<ObjectMetadata> result = registryService.uploadToStage(SCOPE_NAME, "release", testObject, metadata);

            assertNotNull(result);
            result.getValue();

            ArgumentCaptor<ActionContext> captor = ArgumentCaptor.forClass(ActionContext.class);
            verify(mockStageActionService).onEnter(captor.capture());
            ActionContext ctx = captor.getValue();
            assertEquals(SCOPE_NAME, ctx.scope());
            assertEquals(STAGE_ACTION_REGISTRY_NAME, ctx.registry());
            assertEquals("release", ctx.stage());
            assertEquals("test-id", ctx.objectId());
            assertEquals("EClass", ctx.objectType());
            assertNull(ctx.sourceStage());
            assertFalse(ctx.replay());

            verify(mockStageActionService, times(1)).onEnter(any(ActionContext.class));
            verify(mockStageActionService, never()).onUpdate(any(ActionContext.class));
            verify(mockStageActionService, never()).onExit(any(ActionContext.class));
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should NOT invoke onEnter when uploading to a non-trigger stage")
        @WithFactoryConfiguration(factoryPid = "RegistryService", name = "stage-action-test", location = "?", properties = {
                @Property(key = "registry.name", value = STAGE_ACTION_REGISTRY_NAME),
                @Property(key = "stages", type = Type.Array, value = {
                        "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                        "{ \"name\" : \"release\", \"writable\" : true, \"final\": true}", }),
                @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
                @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:mock", "release:mock" }),
                @Property(key = "storageService.target", value = "(storage.type=mock)"),
                @Property(key = "stageActionService.target", value = "(service.ranking=1000)") })
        void shouldNotInvokeOnEnterOnUploadToNonTriggerStage(
                @InjectService(cardinality = 0, filter = "(registry.name=" + STAGE_ACTION_REGISTRY_NAME
                        + ")") ServiceAware<RegistryService> registryAware)
                throws InterruptedException, InvocationTargetException {

            RegistryService<EObject> registryService = registryAware.waitForService(5000);
            assertNotNull(registryService, "RegistryService should be available");

            EObject testObject = EcoreFactory.eINSTANCE.createEClass();
            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setObjectName("test-object");
            metadata.setObjectId("test-id");
            metadata.setObjectType("EClass");

            ObjectMetadata resultMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            resultMetadata.setObjectId("test-id");
            resultMetadata.setObjectType("EClass");

            Promise<ObjectMetadata> promise = promiseFactory.resolved(resultMetadata);
            when(mockStorageService.storeObject(anyString(), anyString(), anyString(), anyString(), any(), any()))
                    .thenReturn(promise);

            Promise<ObjectMetadata> result = registryService.uploadToStage(SCOPE_NAME, "draft", testObject, metadata);

            assertNotNull(result);
            result.getValue();

            verify(mockStageActionService, never()).onEnter(any(ActionContext.class));
            verify(mockStageActionService, never()).onUpdate(any(ActionContext.class));
            verify(mockStageActionService, never()).onExit(any(ActionContext.class));
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should invoke onExit when deleting from a trigger stage")
        @WithFactoryConfiguration(factoryPid = "RegistryService", name = "stage-action-test", location = "?", properties = {
                @Property(key = "registry.name", value = STAGE_ACTION_REGISTRY_NAME),
                @Property(key = "stages", type = Type.Array, value = {
                        "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                        "{ \"name\" : \"release\", \"writable\" : true, \"final\": true}", }),
                @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
                @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:mock", "release:mock" }),
                @Property(key = "storageService.target", value = "(storage.type=mock)"),
                @Property(key = "stageActionService.target", value = "(service.ranking=1000)") })
        void shouldInvokeOnExitOnDeleteFromTriggerStage(
                @InjectService(cardinality = 0, filter = "(registry.name=" + STAGE_ACTION_REGISTRY_NAME
                        + ")") ServiceAware<RegistryService> registryAware)
                throws InterruptedException, InvocationTargetException {

            RegistryService<EObject> registryService = registryAware.waitForService(5000);
            assertNotNull(registryService, "RegistryService should be available");

            ObjectMetadata existingMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            existingMetadata.setObjectId("test-id");
            existingMetadata.setObjectType("EClass");

            Promise<ObjectMetadata> metadataPromise = promiseFactory.resolved(existingMetadata);
            when(mockStorageService.retrieveMetadata(anyString(), anyString(), eq("release"), anyString()))
                    .thenReturn(metadataPromise);

            Promise<Boolean> deletePromise = promiseFactory.resolved(Boolean.TRUE);
            when(mockStorageService.deleteObject(anyString(), anyString(), eq("release"), anyString()))
                    .thenReturn(deletePromise);

            Promise<Boolean> result = registryService.deleteFromStage(SCOPE_NAME, "release", "test-id");

            assertNotNull(result);
            assertTrue(result.getValue());

            ArgumentCaptor<ActionContext> captor = ArgumentCaptor.forClass(ActionContext.class);
            verify(mockStageActionService).onExit(captor.capture());
            ActionContext ctx = captor.getValue();
            assertEquals(SCOPE_NAME, ctx.scope());
            assertEquals(STAGE_ACTION_REGISTRY_NAME, ctx.registry());
            assertEquals("release", ctx.stage());
            assertEquals("test-id", ctx.objectId());
            assertEquals("EClass", ctx.objectType());
            assertEquals(ExitReason.DELETED, ctx.exitReason());
            assertNull(ctx.targetStage());

            verify(mockStageActionService, times(1)).onExit(any(ActionContext.class));
            verify(mockStageActionService, never()).onEnter(any(ActionContext.class));
            verify(mockStageActionService, never()).onUpdate(any(ActionContext.class));
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should NOT invoke onExit when deleting from a non-trigger stage")
        @WithFactoryConfiguration(factoryPid = "RegistryService", name = "stage-action-test", location = "?", properties = {
                @Property(key = "registry.name", value = STAGE_ACTION_REGISTRY_NAME),
                @Property(key = "stages", type = Type.Array, value = {
                        "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                        "{ \"name\" : \"release\", \"writable\" : true, \"final\": true}", }),
                @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
                @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:mock", "release:mock" }),
                @Property(key = "storageService.target", value = "(storage.type=mock)"),
                @Property(key = "stageActionService.target", value = "(service.ranking=1000)") })
        void shouldNotInvokeOnExitOnDeleteFromNonTriggerStage(
                @InjectService(cardinality = 0, filter = "(registry.name=" + STAGE_ACTION_REGISTRY_NAME
                        + ")") ServiceAware<RegistryService> registryAware)
                throws InterruptedException, InvocationTargetException {

            RegistryService<EObject> registryService = registryAware.waitForService(5000);
            assertNotNull(registryService, "RegistryService should be available");

            ObjectMetadata existingMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            existingMetadata.setObjectId("test-id");
            existingMetadata.setObjectType("EClass");

            Promise<ObjectMetadata> metadataPromise = promiseFactory.resolved(existingMetadata);
            when(mockStorageService.retrieveMetadata(anyString(), anyString(), eq("draft"), anyString()))
                    .thenReturn(metadataPromise);

            Promise<Boolean> deletePromise = promiseFactory.resolved(Boolean.TRUE);
            when(mockStorageService.deleteObject(anyString(), anyString(), eq("draft"), anyString()))
                    .thenReturn(deletePromise);

            Promise<Boolean> result = registryService.deleteFromStage(SCOPE_NAME, "draft", "test-id");

            assertNotNull(result);
            assertTrue(result.getValue());

            verify(mockStageActionService, never()).onEnter(any(ActionContext.class));
            verify(mockStageActionService, never()).onUpdate(any(ActionContext.class));
            verify(mockStageActionService, never()).onExit(any(ActionContext.class));
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should invoke onEnter when transitioning INTO a trigger stage")
        @WithFactoryConfiguration(factoryPid = "RegistryService", name = "stage-action-test", location = "?", properties = {
                @Property(key = "registry.name", value = STAGE_ACTION_REGISTRY_NAME),
                @Property(key = "stages", type = Type.Array, value = {
                        "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                        "{ \"name\" : \"release\", \"writable\" : true, \"final\": true}", }),
                @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
                @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:mock", "release:mock" }),
                @Property(key = "delete.after.transition", value = "true"),
                @Property(key = "storageService.target", value = "(storage.type=mock)"),
                @Property(key = "stageActionService.target", value = "(service.ranking=1000)") })
        void shouldInvokeOnEnterOnTransitionToTriggerStage(
                @InjectService(cardinality = 0, filter = "(registry.name=" + STAGE_ACTION_REGISTRY_NAME
                        + ")") ServiceAware<RegistryService> registryAware)
                throws InterruptedException, InvocationTargetException {

            RegistryService<EObject> registryService = registryAware.waitForService(5000);
            assertNotNull(registryService, "RegistryService should be available");

            EObject testObject = EcoreFactory.eINSTANCE.createEClass();
            ObjectMetadata sourceMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            sourceMetadata.setObjectId("test-id");
            sourceMetadata.setObjectName("TestObject");
            sourceMetadata.setObjectType("EClass");

            Promise<EObject> objectPromise = promiseFactory.resolved(testObject);
            Promise<ObjectMetadata> metadataPromise = promiseFactory.resolved(sourceMetadata);
            when(mockStorageService.retrieveObject(anyString(), anyString(), eq("draft"), anyString()))
                    .thenReturn(objectPromise);
            when(mockStorageService.retrieveMetadata(anyString(), anyString(), eq("draft"), anyString()))
                    .thenReturn(metadataPromise);

            ObjectMetadata targetMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            targetMetadata.setObjectId("test-id");
            targetMetadata.setObjectType("EClass");
            targetMetadata.setStage("release");

            Promise<ObjectMetadata> storePromise = promiseFactory.resolved(targetMetadata);
            when(mockStorageService.storeObject(anyString(), anyString(), eq("release"), anyString(), any(), any()))
                    .thenReturn(storePromise);

            Promise<Boolean> deletePromise = promiseFactory.resolved(Boolean.TRUE);
            when(mockStorageService.deleteObject(anyString(), anyString(), eq("draft"), anyString()))
                    .thenReturn(deletePromise);

            ObjectMetadata result = registryService.transitionToStage(SCOPE_NAME, "test-id", "draft", "release");

            assertNotNull(result);
            assertEquals("release", result.getStage());

            // ENTER on the target stage (release) is a trigger event
            ArgumentCaptor<ActionContext> captor = ArgumentCaptor.forClass(ActionContext.class);
            verify(mockStageActionService).onEnter(captor.capture());
            ActionContext ctx = captor.getValue();
            assertEquals("release", ctx.stage());
            assertEquals("draft", ctx.sourceStage());
            assertEquals("test-id", ctx.objectId());

            // EXIT fires for the source stage (draft), but draft is not a trigger stage so
            // the dispatcher filters it out.
            verify(mockStageActionService, never()).onExit(any(ActionContext.class));
            verify(mockStageActionService, never()).onUpdate(any(ActionContext.class));
            verify(mockStageActionService, times(1)).onEnter(any(ActionContext.class));
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should NOT invoke any callback when transitioning between non-trigger stages")
        @WithFactoryConfiguration(factoryPid = "RegistryService", name = "stage-action-test", location = "?", properties = {
                @Property(key = "registry.name", value = STAGE_ACTION_REGISTRY_NAME),
                @Property(key = "stages", type = Type.Array, value = {
                        "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                        "{ \"name\" : \"approved\", \"writable\" : true, \"final\": false}",
                        "{ \"name\" : \"release\", \"writable\" : true, \"final\": true}", }),
                @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:approved",
                        "approved:release" }),
                @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:mock", "approved:mock",
                        "release:mock" }),
                @Property(key = "delete.after.transition", value = "true"),
                @Property(key = "storageService.target", value = "(storage.type=mock)"),
                @Property(key = "stageActionService.target", value = "(service.ranking=1000)") })
        void shouldNotInvokeCallbacksOnTransitionBetweenNonTriggerStages(
                @InjectService(cardinality = 0, filter = "(registry.name=" + STAGE_ACTION_REGISTRY_NAME
                        + ")") ServiceAware<RegistryService> registryAware)
                throws InterruptedException, InvocationTargetException {

            RegistryService<EObject> registryService = registryAware.waitForService(5000);
            assertNotNull(registryService, "RegistryService should be available");

            EObject testObject = EcoreFactory.eINSTANCE.createEClass();
            ObjectMetadata sourceMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            sourceMetadata.setObjectId("test-id");
            sourceMetadata.setObjectName("TestObject");
            sourceMetadata.setObjectType("EClass");

            Promise<EObject> objectPromise = promiseFactory.resolved(testObject);
            Promise<ObjectMetadata> metadataPromise = promiseFactory.resolved(sourceMetadata);
            when(mockStorageService.retrieveObject(anyString(), anyString(), eq("draft"), anyString()))
                    .thenReturn(objectPromise);
            when(mockStorageService.retrieveMetadata(anyString(), anyString(), eq("draft"), anyString()))
                    .thenReturn(metadataPromise);

            ObjectMetadata targetMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            targetMetadata.setObjectId("test-id");
            targetMetadata.setObjectType("EClass");
            targetMetadata.setStage("approved");

            Promise<ObjectMetadata> storePromise = promiseFactory.resolved(targetMetadata);
            when(mockStorageService.storeObject(anyString(), anyString(), eq("approved"), anyString(), any(), any()))
                    .thenReturn(storePromise);

            Promise<Boolean> deletePromise = promiseFactory.resolved(Boolean.TRUE);
            when(mockStorageService.deleteObject(anyString(), anyString(), eq("draft"), anyString()))
                    .thenReturn(deletePromise);

            ObjectMetadata result = registryService.transitionToStage(SCOPE_NAME, "test-id", "draft", "approved");

            assertNotNull(result);
            assertEquals("approved", result.getStage());

            verify(mockStageActionService, never()).onEnter(any(ActionContext.class));
            verify(mockStageActionService, never()).onUpdate(any(ActionContext.class));
            verify(mockStageActionService, never()).onExit(any(ActionContext.class));
        }
    }
}
