/**
 * Copyright (c) 2012 - 2023 Data In Motion and others.
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
package org.eclipse.fennec.model.atlas.management.apicurio.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;
import org.eclipse.fennec.model.atlas.mgmt.management.StorageBackendType;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations.RegistryConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.osgi.util.promise.Promise;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Integration tests for EObjectApicurioStorageService
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class EObjectApicurioStorageServiceTest {

    @InjectBundleContext
    BundleContext context;

    @TempDir
    Path tempDir;

    private static final String APICURIO_DOCKER_IMAGE = "apicurio/apicurio-registry:latest-release";
    private static final String APICURIO_ENV_DELETION_ARTIFACT = "APICURIO_REST_DELETION_ARTIFACT_ENABLED";
    private static final int APICURIO_EXPOSED_PORT = 8080;
    private static final String APICURIO_BASE_PATH = "/apis/registry/v3/";
    private static final String APICURIO_BASE_URL = "http://%s:%d" + APICURIO_BASE_PATH;
    private static final String TEST_SCOPE = "test_scope";
    private static final String TEST_REGISTRY = "test_registry";
    private static final String TEST_STAGE = "test_stage";

    private GenericContainer<?> container;

    @BeforeEach
    public void before(@InjectBundleContext BundleContext ctx) {
        assertNotNull(context, "BundleContext should not be null");
        assertNotNull(tempDir, "TempDir should not be null");

        // Set system property for template argument resolution of RegistryConfiguration
        System.setProperty("tempDir", tempDir.toString());

        container = new GenericContainer<>(APICURIO_DOCKER_IMAGE);

        configureApiCurioTestContainer(container);
        ;

        container.start();

    }

    @AfterEach
    void tearDown() throws InterruptedException {
        container.stop();
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @RegistryConfiguration
    @Test
    public void testServiceActivation(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = "ApicurioObjectStorage", name = "test", location = "?", properties = {
                    @Property(key = "storage.type", value = "apicurio") })) Configuration configuration,
            @InjectService(cardinality = 0, filter = "(storage.backend=apicurio)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {

        int mappedPort = container.getMappedPort(8080);
        Dictionary<String, Object> serviceProperties = new Hashtable<>();
        serviceProperties.put("base.url", String.format(APICURIO_BASE_URL, container.getHost(), mappedPort));
        configuration.update(serviceProperties);

        Thread.sleep(3000);

        // Storage service should be available (which implies registry is also working)
        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000l);
        assertNotNull(storageService, "Storage service should be available");

        // Verify backend type
        assertEquals(StorageBackendType.APICURIO, storageService.getBackendType());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @RegistryConfiguration
    @Test
    public void testStoreAndRetrieveEPackageInJson(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = "ApicurioObjectStorage", name = "test", location = "?", properties = {
                    @Property(key = "storage.type", value = "apicurio") })) Configuration configuration,
            @InjectService(cardinality = 0, filter = "(storage.backend=apicurio)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {

        int mappedPort = container.getMappedPort(8080);
        Dictionary<String, Object> serviceProperties = new Hashtable<>();
        serviceProperties.put("base.url", String.format(APICURIO_BASE_URL, container.getHost(), mappedPort));
        configuration.update(serviceProperties);

        Thread.sleep(3000);

        // Storage service should be available (which implies registry is also working)
        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000l);
        assertNotNull(storageService, "Storage service should be available");

        // Create test EPackage
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("TestPackage");
        testPackage.setNsPrefix("test");
        testPackage.setNsURI("http://test/1.0");

        // Create test metadata
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setUploadUser("testUser");
        metadata.setUploadTime(Instant.now());
        metadata.setSourceChannel("testChannel");
        metadata.setVersion("1.0.0");
        metadata.setObjectRef(testPackage);
        metadata.setObjectName("test-id-123");

        // Specify .xmi extension for EPackage
        metadata.getProperties().put("file.extension", ".json");
        metadata.getProperties().put("content.type", "application/json");

        // Store the package
        storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, "default:draft:test-id-123.json", testPackage,
                metadata).getValue();
        String storageId = metadata.getObjectId();

        assertNotNull(storageId);
        assertEquals("default:draft:test-id-123.json", storageId);

        // Verify artifacts were created
        //
        // // Retrieve the package
        Promise<EObject> retrievePromise = storageService.retrieveObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                storageId);
        EPackage retrievedPackage = (EPackage) retrievePromise.getValue();

        assertNotEquals(testPackage, retrievedPackage);
        assertNotNull(retrievedPackage);
        assertEquals("TestPackage", retrievedPackage.getName());
        assertEquals("test", retrievedPackage.getNsPrefix());
        assertEquals("http://test/1.0", retrievedPackage.getNsURI());
        //
        // // Retrieve metadata
        Promise<ObjectMetadata> metadataPromise = storageService.retrieveMetadata(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                storageId);
        ObjectMetadata retrievedMetadata = metadataPromise.getValue();

        assertNotNull(retrievedMetadata);
        assertEquals("testUser", retrievedMetadata.getUploadUser());
        assertEquals("testChannel", retrievedMetadata.getSourceChannel());
        assertNotNull(retrievedMetadata.getContentHash());

        // Cleanup
        Promise<Boolean> deletePromise = storageService.deleteObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, storageId);
        Boolean deleted = deletePromise.getValue();
        assertTrue(deleted);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @RegistryConfiguration
    @Test
    public void testExists(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = "ApicurioObjectStorage", name = "test", location = "?", properties = {
                    @Property(key = "storage.type", value = "apicurio") })) Configuration configuration,
            @InjectService(cardinality = 0, filter = "(storage.backend=apicurio)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {

        int mappedPort = container.getMappedPort(8080);
        Dictionary<String, Object> serviceProperties = new Hashtable<>();
        serviceProperties.put("base.url", String.format(APICURIO_BASE_URL, container.getHost(), mappedPort));
        configuration.update(serviceProperties);

        Thread.sleep(3000);

        // Storage service should be available (which implies registry is also working)
        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000l);
        assertNotNull(storageService, "Storage service should be available");

        // Test non-existent object
        Boolean existsBefore = storageService.exists(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, "non-existent-id");
        assertFalse(existsBefore, "Non-existent object should not exist");

        // Store an object
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("ExistsTest");

        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setUploadUser("testUser");
        metadata.setSourceChannel("testChannel");
        metadata.setContentHash("testHash");
        metadata.setObjectType("EPackage");
        metadata.setUploadTime(Instant.now());
        metadata.getProperties().put("file.extension", ".json");

        storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, "exists-test-id", testPackage, metadata)
                .getValue();
        String storageId = metadata.getObjectId();

        // Test existing object
        Boolean existsAfter = storageService.exists(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, storageId);
        assertTrue(existsAfter, "Stored object should exist");

        // Delete the object
        Promise<Boolean> deletePromise = storageService.deleteObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, storageId);
        Boolean deleted = deletePromise.getValue();
        assertTrue(deleted);

        // Test after deletion
        Boolean existsAfterDelete = storageService.exists(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, storageId);
        assertFalse(existsAfterDelete, "Deleted object should not exist");

        // Test with null and empty IDs
        Boolean existsNull = storageService.exists(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, null);
        assertFalse(existsNull, "Null ID should return false");

        Boolean existsEmpty = storageService.exists(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, "");
        assertFalse(existsEmpty, "Empty ID should return false");

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @RegistryConfiguration
    @Test
    public void testDeleteObject(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = "ApicurioObjectStorage", name = "test", location = "?", properties = {
                    @Property(key = "storage.type", value = "apicurio") })) Configuration configuration,
            @InjectService(cardinality = 0, filter = "(storage.backend=apicurio)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {

        int mappedPort = container.getMappedPort(8080);
        Dictionary<String, Object> serviceProperties = new Hashtable<>();
        serviceProperties.put("base.url", String.format(APICURIO_BASE_URL, container.getHost(), mappedPort));
        configuration.update(serviceProperties);

        Thread.sleep(3000);

        // Storage service should be available (which implies registry is also working)
        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000l);
        assertNotNull(storageService, "Storage service should be available");

        // Store a package
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("DeleteTest");

        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setUploadUser("testUser");
        metadata.getProperties().put("file.extension", ".xmi");

        storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, "delete-test-id", testPackage, metadata)
                .getValue();
        String storageId = metadata.getObjectId();

        // Delete the object
        Promise<Boolean> deletePromise = storageService.deleteObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, storageId);
        Boolean deleted = deletePromise.getValue();

        assertTrue(deleted);

        // Try to retrieve deleted object
        Promise<EObject> retrievePromise = storageService.retrieveObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                storageId);
        EObject retrievedPackage = retrievePromise.getValue();
        assertNull(retrievedPackage, "Should not find deleted package");

    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @RegistryConfiguration
    @Test
    public void testListObjectIds(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = "ApicurioObjectStorage", name = "test", location = "?", properties = {
                    @Property(key = "storage.type", value = "apicurio") })) Configuration configuration,
            @InjectService(cardinality = 0, filter = "(storage.backend=apicurio)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {

        int mappedPort = container.getMappedPort(8080);
        Dictionary<String, Object> serviceProperties = new Hashtable<>();
        serviceProperties.put("base.url", String.format(APICURIO_BASE_URL, container.getHost(), mappedPort));
        configuration.update(serviceProperties);

        Thread.sleep(3000);

        // Storage service should be available (which implies registry is also working)
        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000l);
        assertNotNull(storageService, "Storage service should be available");

        // Store multiple packages
        for (int i = 0; i < 3; i++) {
            EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
            pkg.setName("Package" + i);
            pkg.setNsPrefix("pkg" + i);
            pkg.setNsURI("http://test/" + i);

            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setUploadUser("testUser");
            metadata.setUploadTime(Instant.now());
            metadata.getProperties().put("file.extension", ".xmi");

            storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, "test-pkg-" + i, pkg, metadata)
                    .getValue();
        }

        // List all object IDs
        Promise<List<String>> listPromise = storageService.listObjectIds(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE);
        List<String> objectIds = listPromise.getValue();

        assertNotNull(objectIds);
        assertEquals(3, objectIds.size());
        assertTrue(objectIds.contains("test-pkg-0"));
        assertTrue(objectIds.contains("test-pkg-1"));
        assertTrue(objectIds.contains("test-pkg-2"));

        // Cleanup
        Promise<Boolean> deletePromise = storageService.deleteObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                "test-pkg-0");
        Boolean deleted = deletePromise.getValue();
        assertTrue(deleted);

        deletePromise = storageService.deleteObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, "test-pkg-1");
        deleted = deletePromise.getValue();
        assertTrue(deleted);

        deletePromise = storageService.deleteObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, "test-pkg-2");
        deleted = deletePromise.getValue();
        assertTrue(deleted);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @RegistryConfiguration
    @Test
    public void testAutoGeneratedId(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = "ApicurioObjectStorage", name = "test", location = "?", properties = {
                    @Property(key = "storage.type", value = "apicurio") })) Configuration configuration,
            @InjectService(cardinality = 0, filter = "(storage.backend=apicurio)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {

        int mappedPort = container.getMappedPort(8080);
        Dictionary<String, Object> serviceProperties = new Hashtable<>();
        serviceProperties.put("base.url", String.format(APICURIO_BASE_URL, container.getHost(), mappedPort));
        configuration.update(serviceProperties);

        Thread.sleep(3000);

        // Storage service should be available (which implies registry is also working)
        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000l);
        assertNotNull(storageService, "Storage service should be available");

        // Store package without providing ID
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("AutoIdTest");

        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setUploadUser("testUser");
        metadata.getProperties().put("file.extension", ".json");
        metadata.getProperties().put("content.type", "application/json");

        storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, null, testPackage, metadata).getValue();
        String storageId = metadata.getObjectId();

        assertNotNull(storageId);
        assertFalse(storageId.isEmpty());

        // Should be able to retrieve with auto-generated ID
        Promise<EObject> retrievePromise = storageService.retrieveObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                storageId);
        EPackage retrievedPackage = (EPackage) retrievePromise.getValue();

        assertNotNull(retrievedPackage);
        assertEquals("AutoIdTest", retrievedPackage.getName());

        // Cleanup
        Promise<Boolean> deletePromise = storageService.deleteObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, storageId);
        Boolean deleted = deletePromise.getValue();
        assertTrue(deleted);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @RegistryConfiguration
    @Test
    public void testCustomFileExtension(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = "ApicurioObjectStorage", name = "test", location = "?", properties = {
                    @Property(key = "storage.type", value = "apicurio") })) Configuration configuration,
            @InjectService(cardinality = 0, filter = "(storage.backend=apicurio)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {

        int mappedPort = container.getMappedPort(8080);
        Dictionary<String, Object> serviceProperties = new Hashtable<>();
        serviceProperties.put("base.url", String.format(APICURIO_BASE_URL, container.getHost(), mappedPort));
        configuration.update(serviceProperties);

        Thread.sleep(3000);

        // Storage service should be available (which implies registry is also working)
        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000l);
        assertNotNull(storageService, "Storage service should be available");

        // Test with custom extension
        EPackage testPackage1 = EcoreFactory.eINSTANCE.createEPackage();
        testPackage1.setName("CustomExtTest1");

        ObjectMetadata metadata1 = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata1.setUploadUser("testUser");
        metadata1.getProperties().put("file.extension", "json"); // without dot
        metadata1.setObjectRef(testPackage1);

        storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, "custom-ext-1", testPackage1, metadata1)
                .getValue();
        String storageId1 = metadata1.getObjectId();

        // Verify file with .json extension exists
        assertTrue(storageService.exists(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, storageId1), "Artifact should exist");

        // Test with default extension (no property set)
        EPackage testPackage2 = EcoreFactory.eINSTANCE.createEPackage();
        testPackage2.setName("DefaultExtTest");

        ObjectMetadata metadata2 = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata2.setUploadUser("testUser");
        metadata2.setObjectRef(testPackage2);
        // No file.extension property set - should use default .xmi

        storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, "default-ext", testPackage2, metadata2)
                .getValue();
        String storageId2 = metadata2.getObjectId();

        // Verify file with .xmi extension exists
        assertTrue(storageService.exists(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, storageId2), "Artifact should exist");

        // Verify both can be retrieved
        Promise<EObject> retrievePromise1 = storageService.retrieveObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                storageId1);
        EObject retrieved1 = retrievePromise1.getValue();
        assertNotNull(retrieved1);
        assertTrue(retrieved1 instanceof EPackage);
        assertEquals("CustomExtTest1", ((EPackage) retrieved1).getName());

        Promise<EObject> retrievePromise2 = storageService.retrieveObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                storageId2);
        EObject retrieved2 = retrievePromise2.getValue();
        assertNotNull(retrieved2);
        assertTrue(retrieved2 instanceof EPackage);
        assertEquals("DefaultExtTest", ((EPackage) retrieved2).getName());

        // Cleanup
        Promise<Boolean> deletePromise = storageService.deleteObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, storageId1);
        Boolean deleted = deletePromise.getValue();
        assertTrue(deleted);

        deletePromise = storageService.deleteObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, storageId2);
        deleted = deletePromise.getValue();
        assertTrue(deleted);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @RegistryConfiguration
    @Test
    public void testContentType(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = "ApicurioObjectStorage", name = "test", location = "?", properties = {
                    @Property(key = "storage.type", value = "apicurio") })) Configuration configuration,
            @InjectService(cardinality = 0, filter = "(storage.backend=apicurio)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {

        int mappedPort = container.getMappedPort(8080);
        Dictionary<String, Object> serviceProperties = new Hashtable<>();
        serviceProperties.put("base.url", String.format(APICURIO_BASE_URL, container.getHost(), mappedPort));
        configuration.update(serviceProperties);

        Thread.sleep(3000);

        // Storage service should be available (which implies registry is also working)
        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000l);
        assertNotNull(storageService, "Storage service should be available");

        // Test with content type for Ecore
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("ContentTypeTest");
        testPackage.setNsPrefix("cttest");
        testPackage.setNsURI("http://test/contenttype/1.0");

        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setUploadUser("testUser");
        // Set both file extension and content type
        metadata.getProperties().put("file.extension", ".ecore");
        metadata.getProperties().put("content.type", "application/xml");
        metadata.setObjectRef(testPackage);

        storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, "content-type-test", testPackage, metadata)
                .getValue();
        String storageId = metadata.getObjectId();

        // Verify file exists
        assertTrue(storageService.exists(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, storageId));

        // Retrieve and verify
        Promise<EObject> retrievePromise = storageService.retrieveObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                storageId);
        EObject retrieved = retrievePromise.getValue();
        assertNotNull(retrieved);
        assertTrue(retrieved instanceof EPackage);
        assertEquals("ContentTypeTest", ((EPackage) retrieved).getName());

        // Cleanup
        Promise<Boolean> deletePromise = storageService.deleteObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, storageId);
        Boolean deleted = deletePromise.getValue();
        assertTrue(deleted);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @RegistryConfiguration
    @Test
    public void testUpdateMetadata(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = "ApicurioObjectStorage", name = "test", location = "?", properties = {
                    @Property(key = "storage.type", value = "apicurio") })) Configuration configuration,
            @InjectService(cardinality = 0, filter = "(storage.backend=apicurio)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {

        int mappedPort = container.getMappedPort(8080);
        Dictionary<String, Object> serviceProperties = new Hashtable<>();
        serviceProperties.put("base.url", String.format(APICURIO_BASE_URL, container.getHost(), mappedPort));
        configuration.update(serviceProperties);

        Thread.sleep(3000);

        // Storage service should be available (which implies registry is also working)
        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000l);
        assertNotNull(storageService, "Storage service should be available");

        // Store initial object
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("UpdateMetadataTest");

        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setUploadUser("originalUser");
        metadata.setSourceChannel("originalChannel");
        metadata.setContentHash("originalHash");
        metadata.setUploadTime(Instant.now());
        metadata.getProperties().put("file.extension", ".ecore");

        storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, "update-metadata-test", testPackage, metadata)
                .getValue();
        String storageId = metadata.getObjectId();

        // Create updated metadata
        ObjectMetadata updatedMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        updatedMetadata.setUploadUser("updatedUser");
        updatedMetadata.setSourceChannel("updatedChannel");
        updatedMetadata.setUploadTime(Instant.now());
        updatedMetadata.setReviewUser("reviewUser");
        updatedMetadata.setReviewTime(Instant.now());
        updatedMetadata.setReviewReason("Updated for testing");
        updatedMetadata.getProperties().put("file.extension", ".ecore");
        updatedMetadata.getProperties().put("custom.property", "customValue");

        // Update metadata
        Promise<Boolean> updatePromise = storageService.updateMetadata(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, storageId,
                updatedMetadata);
        Boolean updateResult = updatePromise.getValue();
        assertTrue(updateResult, "Metadata update should succeed");

        // Retrieve and verify updated metadata
        Promise<ObjectMetadata> retrievePromise = storageService.retrieveMetadata(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                storageId);
        ObjectMetadata retrievedMetadata = retrievePromise.getValue();

        assertNotNull(retrievedMetadata);
        assertEquals("originalUser", retrievedMetadata.getUploadUser(),
                "Upload user should be immutable and preserved");
        assertEquals("updatedChannel", retrievedMetadata.getSourceChannel());
        assertEquals("reviewUser", retrievedMetadata.getReviewUser());
        assertEquals("Updated for testing", retrievedMetadata.getReviewReason());
        assertEquals("customValue", retrievedMetadata.getProperties().get("custom.property"));

        // Verify object itself is unchanged
        Promise<EObject> objectPromise = storageService.retrieveObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                storageId);
        EPackage retrievedPackage = (EPackage) objectPromise.getValue();
        assertNotNull(retrievedPackage);
        assertEquals("UpdateMetadataTest", retrievedPackage.getName());

        // Cleanup
        Promise<Boolean> deletePromise = storageService.deleteObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, storageId);
        Boolean deleted = deletePromise.getValue();
        assertTrue(deleted);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @RegistryConfiguration
    @Test
    public void testUpdateStatus(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = "ApicurioObjectStorage", name = "test", location = "?", properties = {
                    @Property(key = "storage.type", value = "apicurio") })) Configuration configuration,
            @InjectService(cardinality = 0, filter = "(storage.backend=apicurio)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {

        int mappedPort = container.getMappedPort(8080);
        Dictionary<String, Object> serviceProperties = new Hashtable<>();
        serviceProperties.put("base.url", String.format(APICURIO_BASE_URL, container.getHost(), mappedPort));
        configuration.update(serviceProperties);

        Thread.sleep(3000);

        // Storage service should be available (which implies registry is also working)
        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000l);
        assertNotNull(storageService, "Storage service should be available");

        // Store initial object with DRAFT status
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("UpdateStatusTest");

        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setUploadUser("testUser");
        metadata.setSourceChannel("testChannel");
        metadata.setContentHash("testHash");
        metadata.setObjectType("EPackage");
        metadata.setUploadTime(Instant.now());
        metadata.setStatus(ObjectStatus.DRAFT);
        metadata.getProperties().put("file.extension", ".ecore");
        metadata.setObjectRef(testPackage);

        storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, "update-status-test", testPackage, metadata)
                .getValue();
        String storageId = metadata.getObjectId();

        // Verify initial status
        Promise<ObjectMetadata> initialPromise = storageService.retrieveMetadata(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                storageId);
        ObjectMetadata initialMetadata = initialPromise.getValue();
        assertEquals(ObjectStatus.DRAFT, initialMetadata.getStatus());
        assertNull(initialMetadata.getLastChangeUser());
        assertNull(initialMetadata.getLastChangeTime());

        // Update status to APPROVED with change user
        Instant beforeUpdate = Instant.now();
        Promise<Boolean> updatePromise = storageService.updateStatus(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, storageId,
                ObjectStatus.APPROVED, "approverUser");
        Boolean updateResult = updatePromise.getValue();
        assertTrue(updateResult, "Status update should succeed");
        Instant afterUpdate = Instant.now();

        // Verify status update
        Promise<ObjectMetadata> updatedPromise = storageService.retrieveMetadata(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                storageId);
        ObjectMetadata updatedMetadata = updatedPromise.getValue();

        assertNotNull(updatedMetadata);
        assertEquals(ObjectStatus.APPROVED, updatedMetadata.getStatus());
        assertEquals("approverUser", updatedMetadata.getLastChangeUser());
        assertNotNull(updatedMetadata.getLastChangeTime());
        assertTrue(updatedMetadata.getLastChangeTime().isAfter(beforeUpdate)
                || updatedMetadata.getLastChangeTime().equals(beforeUpdate));
        assertTrue(updatedMetadata.getLastChangeTime().isBefore(afterUpdate)
                || updatedMetadata.getLastChangeTime().equals(afterUpdate));

        // Update status again without change user
        Promise<Boolean> updatePromise2 = storageService.updateStatus(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, storageId,
                ObjectStatus.DEPLOYED, null);
        Boolean updateResult2 = updatePromise2.getValue();
        assertTrue(updateResult2, "Second status update should succeed");

        // Verify second status update
        Promise<ObjectMetadata> finalPromise = storageService.retrieveMetadata(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                storageId);
        ObjectMetadata finalMetadata = finalPromise.getValue();

        assertEquals(ObjectStatus.DEPLOYED, finalMetadata.getStatus());
        // Change user should remain as previous value when null is passed
        assertEquals("approverUser", finalMetadata.getLastChangeUser());
        assertNotNull(finalMetadata.getLastChangeTime());

        // Cleanup
        Promise<Boolean> deletePromise = storageService.deleteObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, storageId);
        Boolean deleted = deletePromise.getValue();
        assertTrue(deleted);
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @RegistryConfiguration
    @Test
    public void testGetObjectCount(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = "ApicurioObjectStorage", name = "test", location = "?", properties = {
                    @Property(key = "storage.type", value = "apicurio") })) Configuration configuration,
            @InjectService(cardinality = 0, filter = "(storage.backend=apicurio)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {

        int mappedPort = container.getMappedPort(8080);
        Dictionary<String, Object> serviceProperties = new Hashtable<>();
        serviceProperties.put("base.url", String.format(APICURIO_BASE_URL, container.getHost(), mappedPort));
        configuration.update(serviceProperties);

        Thread.sleep(3000);

        // Storage service should be available (which implies registry is also working)
        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000l);
        assertNotNull(storageService, "Storage service should be available");

        // Initial count should be 0
        long initialCount = storageService.getObjectCount();
        assertEquals(0, initialCount, "Initial object count should be 0");

        // Store multiple objects
        for (int i = 0; i < 5; i++) {
            EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
            pkg.setName("CountTest" + i);

            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setUploadUser("testUser");
            metadata.setSourceChannel("testChannel");
            metadata.setContentHash("testHash" + i);
            metadata.setObjectType("EPackage");
            metadata.setUploadTime(Instant.now());
            metadata.getProperties().put("file.extension", ".ecore");
            metadata.setObjectRef(pkg);

            storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, "count-test-" + i, pkg, metadata)
                    .getValue();
        }

        // Count after storing 5 objects
        long countAfterStore = storageService.getObjectCount();
        assertEquals(5, countAfterStore, "Object count should be 5 after storing 5 objects");

        // Delete 2 objects
        Promise<Boolean> deletePromise1 = storageService.deleteObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                "count-test-1");
        deletePromise1.getValue();
        Promise<Boolean> deletePromise2 = storageService.deleteObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                "count-test-3");
        deletePromise2.getValue();

        // Verify metadata retrieval fails for deleted objects
        Promise<ObjectMetadata> metadataPromise1 = storageService.retrieveMetadata(TEST_SCOPE, TEST_REGISTRY,
                TEST_STAGE, "count-test-1");
        assertNull(metadataPromise1.getValue(), "Should not retrieve metadata for deleted object 1");
        Promise<ObjectMetadata> metadataPromise3 = storageService.retrieveMetadata(TEST_SCOPE, TEST_REGISTRY,
                TEST_STAGE, "count-test-3");
        assertNull(metadataPromise3.getValue(), "Should not retrieve metadata for deleted object 3");

        // Count after deleting 2 objects
        long countAfterDelete = storageService.getObjectCount();
        assertEquals(3, countAfterDelete, "Object count should be 3 after deleting 2 objects");

        // Verify count matches listObjectIds size
        Promise<List<String>> listPromise = storageService.listObjectIds(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE);
        List<String> objectIds = listPromise.getValue();
        assertEquals(countAfterDelete, objectIds.size(), "Object count should match listObjectIds size");

        // Cleanup
        Promise<Boolean> deletePromise = storageService.deleteObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                "count-test-0");
        Boolean deleted = deletePromise.getValue();
        assertTrue(deleted);

        deletePromise = storageService.deleteObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, "count-test-2");
        deleted = deletePromise.getValue();
        assertTrue(deleted);

        deletePromise = storageService.deleteObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, "count-test-4");
        deleted = deletePromise.getValue();
        assertTrue(deleted);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @RegistryConfiguration
    public void testUpdateMetadataErrorHandling(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = "ApicurioObjectStorage", name = "test", location = "?", properties = {
                    @Property(key = "storage.type", value = "apicurio") })) Configuration configuration,
            @InjectService(cardinality = 0, filter = "(storage.backend=apicurio)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {

        int mappedPort = container.getMappedPort(8080);
        Dictionary<String, Object> serviceProperties = new Hashtable<>();
        serviceProperties.put("base.url", String.format(APICURIO_BASE_URL, container.getHost(), mappedPort));
        configuration.update(serviceProperties);

        Thread.sleep(3000);

        // Storage service should be available (which implies registry is also working)
        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000l);
        assertNotNull(storageService, "Storage service should be available");

        // Test updating metadata for non-existent object
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setUploadUser("testUser");
        metadata.setSourceChannel("testChannel");
        metadata.setContentHash("testHash");
        metadata.setObjectType("EPackage");
        metadata.setUploadTime(Instant.now());

        Promise<Boolean> updatePromise = storageService.updateMetadata(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                "non-existent-id", metadata);
        Boolean updateResult = updatePromise.getValue();
        assertFalse(updateResult, "Updating metadata for non-existent object should return false");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @RegistryConfiguration
    public void testUpdateStatusErrorHandling(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = "ApicurioObjectStorage", name = "test", location = "?", properties = {
                    @Property(key = "storage.type", value = "apicurio") })) Configuration configuration,
            @InjectService(cardinality = 0, filter = "(storage.backend=apicurio)") ServiceAware<EObjectStorageService> serviceAware)
            throws Exception {

        int mappedPort = container.getMappedPort(8080);
        Dictionary<String, Object> serviceProperties = new Hashtable<>();
        serviceProperties.put("base.url", String.format(APICURIO_BASE_URL, container.getHost(), mappedPort));
        configuration.update(serviceProperties);

        Thread.sleep(3000);

        // Storage service should be available (which implies registry is also working)
        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000l);
        assertNotNull(storageService, "Storage service should be available");
        // Test updating status for non-existent object
        Promise<Boolean> updatePromise = storageService.updateStatus(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE,
                "non-existent-id", ObjectStatus.APPROVED, "testUser");
        Boolean updateResult = updatePromise.getValue();
        assertFalse(updateResult, "Updating status for non-existent object should return false");
    }

    /**
     * Integration test for storage-registry interaction.
     * 
     * Tests that objects stored via storage service can be found using registry
     * service findByStatus. This verifies the registry is properly updated when
     * objects are stored.
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    @RegistryConfiguration
    public void testStorageRegistryIntegration(
            @InjectService(cardinality = 0, filter = "(storage.backend=apicurio)") ServiceAware<EObjectStorageService> serviceAware,
            @InjectService(cardinality = 0, filter = "(registry.type=shared)") ServiceAware<EObjectRegistryService> registryAware,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = "ApicurioObjectStorage", name = "test", location = "?", properties = {
                    @Property(key = "storage.type", value = "apicurio") })) Configuration configuration)
            throws Exception {

        int mappedPort = container.getMappedPort(8080);
        Dictionary<String, Object> serviceProperties = new Hashtable<>();
        serviceProperties.put("base.url", String.format(APICURIO_BASE_URL, container.getHost(), mappedPort));
        serviceProperties.put("workspace.folder", tempDir.toString());
        configuration.update(serviceProperties);

        Thread.sleep(3000);

        // Storage service should be available (which implies registry is also working)
        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
                .waitForService(5000l);
        assertNotNull(storageService, "Storage service should be available");

        // Registry service should be available (configured by annotation)
        EObjectRegistryService<EObject> registryService = (EObjectRegistryService<EObject>) registryAware
                .waitForService(5000L);
        assertNotNull(registryService, "Registry service should be available");
        // Create test EPackage
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("TestRegistryPackage");
        testPackage.setNsURI("https://test.registry/1.0");
        testPackage.setNsPrefix("testreg");

        // Create metadata with DRAFT status
        ObjectMetadata draftMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        draftMetadata.setObjectName("Draft Test Object");
        draftMetadata.setObjectType("EPackage");
        draftMetadata.setVersion("1.0.0");
        draftMetadata.setStatus(ObjectStatus.DRAFT);
        draftMetadata.setUploadUser("TestUser");
        draftMetadata.setUploadTime(Instant.now());
        draftMetadata.setSourceChannel("TEST");
        draftMetadata.setObjectRef(testPackage);

        // Store object with DRAFT status
        storageService
                .storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, "registry-test-draft", testPackage, draftMetadata)
                .getValue();
        String draftObjectId = draftMetadata.getObjectId();
        assertNotNull(draftObjectId, "Draft object ID should not be null");

        // Wait a moment for registry update (if async)
        Thread.sleep(100);

        // Verify we can find DRAFT objects using registry
        List<ObjectMetadata> draftObjects = registryService.findByStatus(ObjectStatus.DRAFT);
        assertNotNull(draftObjects, "Draft objects list should not be null");
        assertTrue(draftObjects.stream().anyMatch(obj -> draftObjectId.equals(obj.getObjectId())),
                "Registry should find the stored draft object by status");

        // Update object to APPROVED status
        draftMetadata.setStatus(ObjectStatus.APPROVED);
        draftMetadata.setLastChangeTime(Instant.now());
        // We have to update the version if we want the update to succeed in Apicurio
        draftMetadata.setVersion("1.1.0");
        assertTrue(storageService.updateMetadata(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, draftObjectId, draftMetadata)
                .getValue(), "Metadata should be updated");

        // Wait a moment for registry update (if async)
        Thread.sleep(100);

        // Verify object no longer appears in DRAFT list
        List<ObjectMetadata> updatedDraftObjects = registryService.findByStatus(ObjectStatus.DRAFT);
        assertFalse(updatedDraftObjects.stream().anyMatch(obj -> draftObjectId.equals(obj.getObjectId())),
                "Object should no longer appear in draft list after status update");

        // Verify object appears in APPROVED list
        List<ObjectMetadata> approvedObjects = registryService.findByStatus(ObjectStatus.APPROVED);
        assertNotNull(approvedObjects, "Approved objects list should not be null");
        assertTrue(approvedObjects.stream().anyMatch(obj -> draftObjectId.equals(obj.getObjectId())),
                "Registry should find the object in approved list after status update");

        // Test with REJECTED status
        EPackage rejectedPackage = EcoreFactory.eINSTANCE.createEPackage();
        rejectedPackage.setName("RejectedTestPackage");
        rejectedPackage.setNsURI("https://test.rejected/1.0");
        rejectedPackage.setNsPrefix("testrej");

        ObjectMetadata rejectedMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        rejectedMetadata.setObjectName("Rejected Test Object");
        rejectedMetadata.setObjectType("EPackage");
        rejectedMetadata.setVersion("1.0.0");
        rejectedMetadata.setStatus(ObjectStatus.REJECTED);
        rejectedMetadata.setUploadUser("TestUser");
        rejectedMetadata.setUploadTime(Instant.now());
        rejectedMetadata.setSourceChannel("TEST");
        rejectedMetadata.setObjectRef(rejectedPackage);

        storageService.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, "registry-test-rejected", rejectedPackage,
                rejectedMetadata).getValue();
        String rejectedObjectId = rejectedMetadata.getObjectId();
        assertNotNull(rejectedObjectId, "Rejected object ID should not be null");

        // Wait a moment for registry update (if async)
        Thread.sleep(100);

        // Verify we can find REJECTED objects using registry
        List<ObjectMetadata> rejectedObjects = registryService.findByStatus(ObjectStatus.REJECTED);
        assertNotNull(rejectedObjects, "Rejected objects list should not be null");
        assertTrue(rejectedObjects.stream().anyMatch(obj -> rejectedObjectId.equals(obj.getObjectId())),
                "Registry should find the stored rejected object by status");

        // Clean up
        storageService.deleteObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, draftObjectId).getValue();
        storageService.deleteObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, rejectedObjectId).getValue();
    }

    private void configureApiCurioTestContainer(GenericContainer<?> container) {
        container.withEnv(APICURIO_ENV_DELETION_ARTIFACT, "true").withExposedPorts(APICURIO_EXPOSED_PORT)
                .waitingFor(Wait.forHttp(APICURIO_BASE_PATH));
    }
}
