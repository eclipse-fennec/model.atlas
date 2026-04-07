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
package org.eclipse.fennec.model.atlas.rest.tests.helper;

import java.time.Instant;
import java.util.Base64;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Registry;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Scope;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Stage;
import org.eclipse.fennec.model.atlas.wf.workflowapi.StageTransition;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiFactory;
import org.eclipse.fennec.model.atlas.workflow.RegistryServiceCollector;
import org.eclipse.fennec.model.atlas.workflow.ScopeServiceCollector;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.Promises;

/**
 * 
 * @author ilenia
 * @since Jan 15, 2026
 */
public class MockTestHelper {

    private static final String TEST_SCOPE_NAME = "test-scope";
    private static final String TEST_REGISTRY_NAME = "test-registry";
    public static final String UNASSIGNED_REGISTRY_NAME = "unassigned-registry";
    private static final String TEST_STAGE_DRAFT = "draft";
    private static final String TEST_STAGE_APPROVED = "approved";
    private static final String TEST_STAGE_RELEASE = "release";
    private static final String TEST_OBJECT_ID = "test-object-123";
    private static final String TEST_PACKAGE_NSURI = "http://test.example.com/schema/1.1";
    private static final String TEST_OBJECT_NAME = "TestObject";
    private static final String TEST_PACKAGE_NAME = "TestSchema";

    // ========== Mock Service Implementation ==========

    /**
     * Mock implementation of ScopeCollector for testing.
     */
    public static class MockScopeServiceCollector extends ScopeServiceCollector {

        private final MockScopeService mockScopeService = new MockScopeService();

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.fennec.model.atlas.workflow.ScopeServiceCollector#
         * getScopeServiceByScopeName(java.lang.String)
         */
        @Override
        public ScopeService<?> getScopeServiceByScopeName(String scopeName) {
            if (TEST_SCOPE_NAME.equals(scopeName)) {
                return mockScopeService;
            }
            return null;
        }

        @Override
        public Scope getScopeByName(String name) {
            if (TEST_SCOPE_NAME.equals(name)) {
                return mockScopeService.getScope();
            }
            return null;
        }

        @Override
        public List<Scope> getAllScopes() {
            return List.of(mockScopeService.getScope());
        }
    }

    /**
     * Mock implementation of RegistryServiceCollector for testing.
     */
    public static class MockRegistryServiceCollector extends RegistryServiceCollector {

        private final MockRegistryService mockRegistryService = new MockRegistryService();
        private final MockRegistryService unassignedRegistryService = new MockRegistryService();

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.fennec.model.atlas.workflow.RegistryServiceCollector#
         * getRegistryServiceByRegistryName(java.lang.String)
         */
        @Override
        public RegistryService<?> getRegistryServiceByRegistryName(String registryName) {
            if (TEST_REGISTRY_NAME.equals(registryName))
                return mockRegistryService;
            if (UNASSIGNED_REGISTRY_NAME.equals(registryName))
                return unassignedRegistryService;
            return null;
        }
    }

    public static class MockScopeService implements ScopeService<EObject> {

        private final Scope mockScope = createMockScope();
        private final RegistryService<EObject> mockRegistryService = new MockRegistryService();

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#
         * uploadToStageForRegistry(java.lang.String, java.lang.String,
         * org.eclipse.emf.ecore.EObject,
         * org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata)
         */
        @Override
        public Promise<ObjectMetadata> uploadToStageForRegistry(String registry, String stage, EObject object,
                ObjectMetadata metadata) {
            return mockRegistryService.uploadToStage(TEST_SCOPE_NAME, stage, object, metadata);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#
         * getMetadataFromStageForRegistry(java.lang.String, java.lang.String,
         * java.lang.String)
         */
        @Override
        public ObjectMetadata getMetadataFromStageForRegistry(String registry, String stage, String objectId) {
            return mockRegistryService.getMetadataFromStage(TEST_SCOPE_NAME, stage, objectId);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#
         * getMetadataFromFinalStageForRegistry(java.lang.String, java.lang.String)
         */
        @Override
        public ObjectMetadata getMetadataFromFinalStageForRegistry(String registry, String objectId) {
            return mockRegistryService.getMetadataFromFinalStage(TEST_SCOPE_NAME, objectId);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#
         * getContentFromStageForRegistry(java.lang.String, java.lang.String,
         * java.lang.String)
         */
        @Override
        public EObject getContentFromStageForRegistry(String registry, String stage, String objectId) {
            return mockRegistryService.getContentFromStage(TEST_SCOPE_NAME, stage, objectId);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#
         * updateInStageForRegistry(java.lang.String, java.lang.String,
         * org.eclipse.emf.ecore.EObject, java.lang.String, java.lang.String)
         */
        @Override
        public Promise<ObjectMetadata> updateInStageForRegistry(String registry, String stage, EObject updatedObject,
                String objectId, String version) {
            return mockRegistryService.updateInStage(TEST_SCOPE_NAME, stage, updatedObject, objectId, version);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#
         * deleteFromStageForRegistry(java.lang.String, java.lang.String,
         * java.lang.String)
         */
        @Override
        public Promise<Boolean> deleteFromStageForRegistry(String registry, String stage, String objectId) {
            return mockRegistryService.deleteFromStage(TEST_SCOPE_NAME, stage, objectId);
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#
         * listAllForRegistry(java.lang.String)
         */
        @Override
        public List<ObjectMetadata> listAllForRegistry(String registry) {
            return mockRegistryService.listAll(TEST_SCOPE_NAME);
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#
         * listInStageForRegistry(java.lang.String, java.lang.String)
         */
        @Override
        public List<ObjectMetadata> listInStageForRegistry(String registry, String stage) {
            return mockRegistryService.listInStage(TEST_SCOPE_NAME, stage);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#
         * listInStageForRegistryByName(java.lang.String, java.lang.String,
         * java.lang.String)
         */
        @Override
        public List<ObjectMetadata> listInStageForRegistryByName(String registry, String stage, String name) {
            return mockRegistryService.listInStageByName(TEST_SCOPE_NAME, stage, name);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#
         * listInFinalStageForRegistry(java.lang.String)
         */
        @Override
        public List<ObjectMetadata> listInFinalStageForRegistry(String registry) {
            return mockRegistryService.listInFinalStage(TEST_SCOPE_NAME);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#
         * transitionToStageForRegistry(java.lang.String, java.lang.String,
         * java.lang.String, java.lang.String)
         */
        @Override
        public ObjectMetadata transitionToStageForRegistry(String registry, String objectId, String fromStage,
                String toStage) {
            return mockRegistryService.transitionToStage(TEST_SCOPE_NAME, objectId, fromStage, toStage);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#isValidRegistry(
         * java.lang.String)
         */
        @Override
        public boolean isValidRegistry(String registryName) {
            return TEST_REGISTRY_NAME.equals(registryName) || "schema".equals(registryName);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#getAllRegistries()
         */
        @Override
        public List<String> getAllRegistries() {
            return List.of(TEST_REGISTRY_NAME);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService#getScope()
         */
        @Override
        public Scope getScope() {
            return mockScope;
        }

        private Scope createMockScope() {
            Scope scope = WorkflowApiFactory.eINSTANCE.createScope();
            scope.setName(TEST_SCOPE_NAME);
            scope.getRegistries().add(createMockRegistry());
            return scope;
        }

        private Registry createMockRegistry() {
            Registry registry = WorkflowApiFactory.eINSTANCE.createRegistry();
            registry.setName(TEST_REGISTRY_NAME);
            registry.getStages().add(createMockStage(TEST_STAGE_DRAFT, true, false));
            registry.getStages().add(createMockStage(TEST_STAGE_APPROVED, true, false));
            registry.getStages().add(createMockStage(TEST_STAGE_RELEASE, false, true));
            registry.getAllowedTransitions().add(createMockStageTransition(TEST_STAGE_DRAFT, TEST_STAGE_APPROVED));
            registry.getAllowedTransitions().add(createMockStageTransition(TEST_STAGE_APPROVED, TEST_STAGE_RELEASE));
            return registry;
        }

        private Stage createMockStage(String name, boolean writable, boolean final_) {
            Stage stage = WorkflowApiFactory.eINSTANCE.createStage();
            stage.setName(name);
            stage.setFinal(final_);
            stage.setWritable(writable);
            return stage;
        }

        private StageTransition createMockStageTransition(String fromStage, String toStage) {
            StageTransition transition = WorkflowApiFactory.eINSTANCE.createStageTransition();
            transition.setFromStage(fromStage);
            transition.setToStage(toStage);
            return transition;
        }

    }

    public static class MockRegistryService implements RegistryService<EObject> {

        private final Registry mockRegistry = createMockRegistry();

        private Registry createMockRegistry() {
            Registry registry = WorkflowApiFactory.eINSTANCE.createRegistry();
            registry.setName(TEST_REGISTRY_NAME);
            registry.getStages().add(createMockStage(TEST_STAGE_DRAFT, true, false));
            registry.getStages().add(createMockStage(TEST_STAGE_APPROVED, true, false));
            registry.getStages().add(createMockStage(TEST_STAGE_RELEASE, false, true));
            registry.getAllowedTransitions().add(createMockStageTransition(TEST_STAGE_DRAFT, TEST_STAGE_APPROVED));
            registry.getAllowedTransitions().add(createMockStageTransition(TEST_STAGE_APPROVED, TEST_STAGE_RELEASE));
            return registry;
        }

        private Stage createMockStage(String name, boolean writable, boolean final_) {
            Stage stage = WorkflowApiFactory.eINSTANCE.createStage();
            stage.setName(name);
            stage.setFinal(final_);
            stage.setWritable(writable);
            return stage;
        }

        private StageTransition createMockStageTransition(String fromStage, String toStage) {
            StageTransition transition = WorkflowApiFactory.eINSTANCE.createStageTransition();
            transition.setFromStage(fromStage);
            transition.setToStage(toStage);
            return transition;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#uploadToStage(
         * java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject,
         * org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata)
         */
        @Override
        public Promise<ObjectMetadata> uploadToStage(String scope, String stage, EObject object,
                ObjectMetadata metadata) {
            metadata.setStage(stage);
            metadata.setRegistry(TEST_REGISTRY_NAME);
            metadata.setScope(scope);
            metadata.setUploadTime(Instant.now());
            return Promises.resolved(metadata);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#
         * getMetadataFromStage(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public ObjectMetadata getMetadataFromStage(String scope, String stage, String objectId) {
            // Return null for non-existent objects (including new objects to be created)
            if (objectId.equals("non-existent-object") || objectId.equals("new-object-id")
                    || objectId.equals("incompatible-object-id")) {
                return null;
            }

            // Only TEST_OBJECT_ID and sensor-object-456 and encoded packageURI
            if (!objectId.equals(TEST_OBJECT_ID) && !objectId.equals("sensor-object-456")
                    && !objectId.equals(new String(Base64.getUrlEncoder().encode(TEST_PACKAGE_NSURI.getBytes())))
                    && !objectId.equals(
                            new String(Base64.getUrlEncoder().encode("http://readonly.com/schema/1.0".getBytes())))) {
                return null;
            }

            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setObjectId(objectId);
            metadata.setStage(stage);
            metadata.setRegistry(TEST_REGISTRY_NAME);
            metadata.setScope(scope);
            metadata.setUploadTime(Instant.now());
            if ("readonly-stage".equals(stage) || objectId
                    .equals(new String(Base64.getUrlEncoder().encode("http://readonly.com/schema/1.0".getBytes()))))
                metadata.setIsReadOnly(true);
            return metadata;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#
         * getMetadataFromFinalStage(java.lang.String, java.lang.String)
         */
        @Override
        public ObjectMetadata getMetadataFromFinalStage(String scope, String objectId) {
            return getMetadataFromStage(scope, TEST_STAGE_RELEASE, objectId);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#
         * getContentFromStage(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public EObject getContentFromStage(String scope, String stage, String objectId) {
            // Return null for non-existent objects
            if (objectId.equals("non-existent-object") || objectId.equals("new-object-id")
                    || objectId.equals("incompatible-object-id")) {
                return null;
            }

            // Only TEST_OBJECT_ID and sensor-object-456 have content
            if (!objectId.equals(TEST_OBJECT_ID) && !objectId.equals("sensor-object-456")
                    && !objectId.equals(new String(Base64.getUrlEncoder().encode(TEST_PACKAGE_NSURI.getBytes())))
                    && !objectId.equals(
                            new String(Base64.getUrlEncoder().encode("http://readonly.com/schema/1.0".getBytes())))) {
                return null;
            }

            return TestHelper.createTestEPackage("http://test.com/object/1.0", "TestObject", "test");
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#updateInStage(
         * java.lang.String, java.lang.String, org.eclipse.emf.ecore.EObject,
         * java.lang.String, java.lang.String)
         */
        @Override
        public Promise<ObjectMetadata> updateInStage(String scope, String stage, EObject updatedObject, String objectId,
                String version) {
            // Only allow updates for existing objects
            if (objectId.equals("non-existent-object") || objectId.equals("new-object-id")
                    || objectId.equals("incompatible-object-id")
                    || (!objectId.equals(TEST_OBJECT_ID) && !objectId.equals("sensor-object-456"))) {
                return Promises.resolved(null);
            }

            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setObjectId(objectId);
            metadata.setStage(stage);
            metadata.setRegistry(TEST_REGISTRY_NAME);
            metadata.setScope(scope);
            metadata.setVersion(version);
            metadata.setLastChangeTime(Instant.now());
            return Promises.resolved(metadata);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#deleteFromStage
         * (java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public Promise<Boolean> deleteFromStage(String scope, String stage, String objectId) {
            // Only allow deletion of existing objects
            return Promises.resolved(objectId.equals(TEST_OBJECT_ID) || objectId.equals("sensor-object-456")
                    || objectId.equals(new String(Base64.getUrlEncoder().encode(TEST_PACKAGE_NSURI.getBytes()))));
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#listInStage(
         * java.lang.String, java.lang.String)
         */
        @Override
        public List<ObjectMetadata> listInStage(String scope, String stage) {
            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setObjectId(TEST_OBJECT_ID);
            metadata.setStage(stage);
            metadata.setRegistry(TEST_REGISTRY_NAME);
            metadata.setScope(scope);
            metadata.setObjectName(TEST_OBJECT_NAME);
            metadata.setUploadTime(Instant.now());
            return List.of(metadata);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#
         * listInStageByName(java.lang.String, java.lang.String, java.lang.String)
         */
        @Override
        public List<ObjectMetadata> listInStageByName(String scope, String stage, String name) {
            // Support wildcard search with * character (only trailing wildcards like
            // "Prefix*")
            boolean isWildcard = name.contains("*");
            String nameFilter = isWildcard ? name.replace("*", "") : name;

            // Create test metadata that matches the filter
            if (TEST_OBJECT_NAME.equals(name) || (isWildcard && TEST_OBJECT_NAME.startsWith(nameFilter))) {
                ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
                metadata.setObjectId(TEST_OBJECT_ID);
                metadata.setStage(stage);
                metadata.setScope(TEST_SCOPE_NAME);
                metadata.setRegistry(TEST_REGISTRY_NAME);
                metadata.setObjectName(TEST_OBJECT_NAME);
                metadata.setUploadTime(Instant.now());
                return List.of(metadata);
            }
            // Create test metadata that matches the filter
            if (TEST_PACKAGE_NAME.equals(name) || (isWildcard && TEST_PACKAGE_NAME.startsWith(nameFilter))) {
                ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
                metadata.setObjectId(TEST_PACKAGE_NAME);
                metadata.setStage(stage);
                metadata.setScope(TEST_SCOPE_NAME);
                metadata.setRegistry(TEST_REGISTRY_NAME);
                metadata.setObjectName(TEST_PACKAGE_NAME);
                metadata.setUploadTime(Instant.now());
                return List.of(metadata);
            }

            // Match for Test* wildcard
            if (isWildcard && TEST_OBJECT_NAME.startsWith(nameFilter)) {
                ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
                metadata.setObjectId(TEST_OBJECT_ID);
                metadata.setStage(stage);
                metadata.setScope(TEST_SCOPE_NAME);
                metadata.setRegistry(TEST_REGISTRY_NAME);
                metadata.setObjectName(TEST_OBJECT_NAME);
                metadata.setUploadTime(Instant.now());
                return List.of(metadata);
            }

            // If looking for SensorData, return a matching object
            if ("SensorData".equals(name) || (isWildcard && "SensorData".startsWith(nameFilter))) {
                ObjectMetadata sensorMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
                sensorMetadata.setObjectId("sensor-object-456");
                sensorMetadata.setStage(stage);
                sensorMetadata.setScope(TEST_SCOPE_NAME);
                sensorMetadata.setRegistry(TEST_REGISTRY_NAME);
                sensorMetadata.setObjectName("SensorData");
                sensorMetadata.setUploadTime(Instant.now());
                return List.of(sensorMetadata);
            }

            // If looking for SensorModel, return a matching object
            if ("SensorModel".equals(name) || (isWildcard && "SensorModel".startsWith(nameFilter))) {
                ObjectMetadata sensorMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
                sensorMetadata.setObjectId("sensor-model");
                sensorMetadata.setStage(stage);
                sensorMetadata.setScope(TEST_SCOPE_NAME);
                sensorMetadata.setRegistry(TEST_REGISTRY_NAME);
                sensorMetadata.setObjectName("SensorModel");
                sensorMetadata.setUploadTime(Instant.now());
                return List.of(sensorMetadata);
            }

            // Return empty list if no match
            return List.of();
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#
         * listInFinalStage(java.lang.String)
         */
        @Override
        public List<ObjectMetadata> listInFinalStage(String scope) {
            return listInStage(scope, TEST_STAGE_RELEASE);
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#
         * listAll(java.lang.String)
         */
        @Override
        public List<ObjectMetadata> listAll(String scope) {
            ObjectMetadata draftMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            draftMetadata.setObjectId(TEST_OBJECT_ID);
            draftMetadata.setStage(TEST_STAGE_DRAFT);
            draftMetadata.setRegistry(TEST_REGISTRY_NAME);
            draftMetadata.setScope(scope);
            draftMetadata.setObjectName(TEST_OBJECT_NAME);
            draftMetadata.setUploadTime(Instant.now());

            ObjectMetadata approvedMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            approvedMetadata.setObjectId("sensor-object-456");
            approvedMetadata.setStage(TEST_STAGE_APPROVED);
            approvedMetadata.setRegistry(TEST_REGISTRY_NAME);
            approvedMetadata.setScope(scope);
            approvedMetadata.setObjectName("SensorData");
            approvedMetadata.setUploadTime(Instant.now());

            return List.of(draftMetadata, approvedMetadata);
        }

        /*
         * (non-Javadoc)
         *
         * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#
         * transitionToStage(java.lang.String, java.lang.String, java.lang.String,
         * java.lang.String)
         */
        @Override
        public ObjectMetadata transitionToStage(String scope, String objectId, String fromStage, String toStage) {
            if (!isTransitionAllowed(fromStage, toStage)) {
                throw new IllegalArgumentException(
                        String.format("Transition not allowed from %s to %s", fromStage, toStage));
            }
            // Only allow transitions for existing objects
            if (objectId.equals("non-existent-object") || objectId.equals("new-object-id")
                    || objectId.equals("incompatible-object-id")
                    || (!objectId.equals(TEST_OBJECT_ID) && !objectId.equals("sensor-object-456"))) {
                return null;
            }

            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setObjectId(objectId);
            metadata.setStage(toStage);
            metadata.setRegistry(TEST_REGISTRY_NAME);
            metadata.setScope(scope);
            metadata.setLastChangeTime(Instant.now());
            return metadata;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getRegistryName
         * ()
         */
        @Override
        public String getRegistryName() {
            return TEST_REGISTRY_NAME;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#isValidStage(
         * java.lang.String)
         */
        @Override
        public boolean isValidStage(String stageName) {
            return TEST_STAGE_DRAFT.equals(stageName) || TEST_STAGE_APPROVED.equals(stageName)
                    || TEST_STAGE_RELEASE.equals(stageName);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#isWritableStage
         * (java.lang.String)
         */
        @Override
        public boolean isWritableStage(String stageName) {
            return TEST_STAGE_DRAFT.equals(stageName) || TEST_STAGE_APPROVED.equals(stageName);
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#
         * isFinalStageWritable()
         */
        @Override
        public boolean isFinalStageWritable() {
            return false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#
         * isTransitionAllowed(java.lang.String, java.lang.String)
         */
        @Override
        public boolean isTransitionAllowed(String fromStage, String toStage) {
            // Allow draft -> approved and approved -> release
            if (TEST_STAGE_DRAFT.equals(fromStage) && TEST_STAGE_APPROVED.equals(toStage)) {
                return true;
            }
            if (TEST_STAGE_APPROVED.equals(fromStage) && TEST_STAGE_RELEASE.equals(toStage)) {
                return true;
            }
            // Disallow skipping stages (e.g., draft -> release)
            return false;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getRegistry()
         */
        @Override
        public Registry getRegistry() {
            return mockRegistry;
        }

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#
         * isEClassCompatibleWithRegistry(org.eclipse.emf.ecore.EClass)
         */
        @Override
        public boolean isEClassCompatibleWithRegistry(EClass eClass) {
            return eClass.equals(EcorePackage.Literals.EPACKAGE)
                    || eClass.getEAllSuperTypes().contains(EcorePackage.Literals.EPACKAGE);
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#getRootEClass()
         */
        @Override
        public EClass getRootEClass() {
            return EcorePackage.Literals.EPACKAGE;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#activate(java.
         * lang.String)
         */
        @Override
        public Void activate(String scope) {
            // TODO Auto-generated method stub
            return null;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService#deactivate(java
         * .lang.String)
         */
        @Override
        public Void deactivate(String scope) {
            // TODO Auto-generated method stub
            return null;
        }
    }

}
