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

import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.model.atlas.management.apicurio.tests.annotations.ApicurioTestAnnotations;
import org.eclipse.fennec.model.atlas.management.apicurio.tests.annotations.ApicurioTestAnnotations.CustomRoleApicurioStorageConfiguration;
import org.eclipse.fennec.model.atlas.management.apicurio.tests.annotations.ApicurioTestAnnotations.DefaultApicurioStorageConfiguration;
import org.eclipse.fennec.model.atlas.management.lucene.tests.annotations.LuceneTestAnnotations.RegistryConfiguration;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;
import org.eclipse.fennec.model.atlas.mgmt.management.StorageBackendType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.osgi.util.promise.Promise;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;
import org.testcontainers.containers.wait.strategy.WaitStrategy;

//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Integration tests for EObjectApicurioStorageService
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class EObjectApicurioStorageServiceTest {
	
	//	@Mock
	//	TestInterface test;

	@InjectBundleContext
	BundleContext context;

	@TempDir
	Path tempDir;

	private static GenericContainer<?> APICURIO_CONTAINER;
	
	@BeforeAll
    static void setup() {
		
		try (GenericContainer<?> container = new GenericContainer<>("apicurio/apicurio-registry:latest-release")) {

			APICURIO_CONTAINER = container;
			APICURIO_CONTAINER.withEnv("APICURIO_REST_DELETION_ARTIFACT_ENABLED", "true")
                    .withExposedPorts(8080);

            WaitStrategy waitStrategy = new HttpWaitStrategy().usingTls().allowInsecure().forPort(8080)
                    .forStatusCodeMatching(response -> response == HTTP_OK || response == HTTP_UNAUTHORIZED)
                    .withReadTimeout(Duration.ofSeconds(30)).withStartupTimeout(Duration.ofMinutes(5));
            APICURIO_CONTAINER.setWaitStrategy(waitStrategy);

            APICURIO_CONTAINER.start();
        }
		
        // Get the dynamically mapped port and host
        String host = APICURIO_CONTAINER.getHost();
        Integer port = APICURIO_CONTAINER.getFirstMappedPort();
        assertNotNull(host, "Apicurio Registry host should be set");
        assertNotNull(port, "Apicurio Registry port should be set");
    }
	
	@AfterEach
	public void afterEach() {
		APICURIO_CONTAINER.stop();
		APICURIO_CONTAINER = null;
	}
	
	@BeforeEach
	public void before(@InjectBundleContext BundleContext ctx) {
		assertNotNull(context, "BundleContext should not be null");
		assertNotNull(tempDir, "TempDir should not be null");

		// Set system property for template argument resolution
		System.setProperty(ApicurioTestAnnotations.PROP_TEMP_DIR, tempDir.toString());
		
		try (GenericContainer<?> container = new GenericContainer<>("apicurio/apicurio-registry:latest-release")) {

			APICURIO_CONTAINER = container;
			APICURIO_CONTAINER.withEnv("APICURIO_REST_DELETION_ARTIFACT_ENABLED", "true")
                    .withExposedPorts(8080);

            WaitStrategy waitStrategy = new HttpWaitStrategy().usingTls().allowInsecure().forPort(8080)
                    .forStatusCodeMatching(response -> response == HTTP_OK || response == HTTP_UNAUTHORIZED)
                    .withReadTimeout(Duration.ofSeconds(30)).withStartupTimeout(Duration.ofMinutes(5));
            APICURIO_CONTAINER.setWaitStrategy(waitStrategy);

            APICURIO_CONTAINER.start();
        }
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	@DefaultApicurioStorageConfiguration
	public void testServiceActivation(
			@InjectService(cardinality = 0, filter = "(storage.backend=apicurio)")
			ServiceAware<EObjectStorageService> serviceAware
			) throws Exception {
		assertNotNull(serviceAware);

		// Storage service should be available (which implies registry is also working)
		EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware.waitForService(5000l);
		assertNotNull(storageService, "Storage service should be available");

		// Verify backend type
		assertEquals(StorageBackendType.APICURIO, storageService.getBackendType());
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	@DefaultApicurioStorageConfiguration
	public void testStoreAndRetrieveEPackageInJson(
			@InjectService(cardinality = 0, filter = "(storage.backend=apicurio)")
			ServiceAware<EObjectStorageService> serviceAware) throws Exception {
		assertNotNull(serviceAware);

		EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware.waitForService(5000L);
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
		metadata.setContentHash("testhash123");
		metadata.setVersion("1.0.0");
		metadata.setObjectRef(testPackage);
		metadata.setObjectName("test-id-123");

		// Specify .xmi extension for EPackage
		metadata.getProperties().put("file.extension", ".json");
		metadata.getProperties().put("content.type", "application/json");

		// Store the package
		Promise<String> storePromise = storageService.storeObject("default:draft:test-id-123.json", testPackage, metadata);
		String storageId = storePromise.getValue();

		assertNotNull(storageId);
		assertEquals("default:draft:test-id-123.json", storageId);

		// Verify artifacts were created
		//
		//		// Retrieve the package
		Promise<EObject> retrievePromise = storageService.retrieveObject(storageId);
		EPackage retrievedPackage = (EPackage) retrievePromise.getValue();

		assertNotEquals(testPackage, retrievedPackage);
		assertNotNull(retrievedPackage);
		assertEquals("TestPackage", retrievedPackage.getName());
		assertEquals("test", retrievedPackage.getNsPrefix());
		assertEquals("http://test/1.0", retrievedPackage.getNsURI());
		//
		//		// Retrieve metadata
		Promise<ObjectMetadata> metadataPromise = storageService.retrieveMetadata(storageId);
		ObjectMetadata retrievedMetadata = metadataPromise.getValue();

		assertNotNull(retrievedMetadata);
		assertEquals("testUser", retrievedMetadata.getUploadUser());
		assertEquals("testChannel", retrievedMetadata.getSourceChannel());
		assertEquals("testhash123", retrievedMetadata.getContentHash());

		//Cleanup
		Promise<Boolean> deletePromise = storageService.deleteObject(storageId);
		Boolean deleted = deletePromise.getValue();
		assertTrue(deleted);

	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	@DefaultApicurioStorageConfiguration
	public void testExists(
			@InjectService(cardinality = 0, filter = "(storage.backend=apicurio)")
			ServiceAware<EObjectStorageService> serviceAware) throws Exception {
		assertNotNull(serviceAware);

		EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware.waitForService(5000L);
		assertNotNull(storageService, "Storage service should be available");

		// Test non-existent object
		Boolean existsBefore = storageService.exists("non-existent-id");
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

		Promise<String> storePromise = storageService.storeObject("exists-test-id", testPackage, metadata);
		String storageId = storePromise.getValue();

		// Test existing object
		Boolean existsAfter = storageService.exists(storageId);
		assertTrue(existsAfter, "Stored object should exist");

		// Delete the object
		Promise<Boolean> deletePromise = storageService.deleteObject(storageId);
		Boolean deleted = deletePromise.getValue();
		assertTrue(deleted);

		// Test after deletion
		Boolean existsAfterDelete = storageService.exists(storageId);
		assertFalse(existsAfterDelete, "Deleted object should not exist");

		// Test with null and empty IDs
		Boolean existsNull = storageService.exists(null);
		assertFalse(existsNull, "Null ID should return false");

		Boolean existsEmpty = storageService.exists("");
		assertFalse(existsEmpty, "Empty ID should return false");

	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	@DefaultApicurioStorageConfiguration
	public void testDeleteObject(
			@InjectService(cardinality = 0, filter = "(storage.backend=apicurio)")
			ServiceAware<EObjectStorageService> serviceAware) throws Exception {
		assertNotNull(serviceAware);

		EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware.waitForService(5000L);
		assertNotNull(storageService, "Storage service should be available");

		// Store a package
		EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
		testPackage.setName("DeleteTest");

		ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadata.setUploadUser("testUser");
		metadata.getProperties().put("file.extension", ".xmi");

		Promise<String> storePromise = storageService.storeObject("delete-test-id", testPackage, metadata);
		String storageId = storePromise.getValue();


		// Delete the object
		Promise<Boolean> deletePromise = storageService.deleteObject(storageId);
		Boolean deleted = deletePromise.getValue();

		assertTrue(deleted);

		// Try to retrieve deleted object
		Promise<EObject> retrievePromise = storageService.retrieveObject(storageId);
		EObject retrievedPackage = retrievePromise.getValue();
		assertNull(retrievedPackage, "Should not find deleted package");

	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	@DefaultApicurioStorageConfiguration
	public void testListObjectIds(
			@InjectService(cardinality = 0, filter = "(storage.backend=apicurio)")
			ServiceAware<EObjectStorageService> serviceAware) throws Exception {
		assertNotNull(serviceAware);

		EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware.waitForService(5000L);
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

			Promise<String> storePromise = storageService.storeObject("test-pkg-" + i, pkg, metadata);
			storePromise.getValue();
		}

		// List all object IDs
		Promise<List<String>> listPromise = storageService.listObjectIds();
		List<String> objectIds = listPromise.getValue();

		assertNotNull(objectIds);
		assertEquals(3, objectIds.size());
		assertTrue(objectIds.contains("test-pkg-0"));
		assertTrue(objectIds.contains("test-pkg-1"));
		assertTrue(objectIds.contains("test-pkg-2"));

		//		Cleanup
		Promise<Boolean> deletePromise = storageService.deleteObject("test-pkg-0");
		Boolean deleted = deletePromise.getValue();
		assertTrue(deleted);

		deletePromise = storageService.deleteObject("test-pkg-1");
		deleted = deletePromise.getValue();
		assertTrue(deleted);

		deletePromise = storageService.deleteObject("test-pkg-2");
		deleted = deletePromise.getValue();
		assertTrue(deleted);
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	@DefaultApicurioStorageConfiguration
	public void testAutoGeneratedId(
			@InjectService(cardinality = 0, filter = "(storage.backend=apicurio)")
			ServiceAware<EObjectStorageService> serviceAware) throws Exception {
		assertNotNull(serviceAware);

		EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware.waitForService(5000L);
		assertNotNull(storageService, "Storage service should be available");

		// Store package without providing ID
		EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
		testPackage.setName("AutoIdTest");

		ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadata.setUploadUser("testUser");
		metadata.getProperties().put("file.extension", ".json");
		metadata.getProperties().put("content.type", "application/json");
		metadata.setObjectRef(testPackage);
		metadata.setObjectType("EPackage");

		Promise<String> storePromise = storageService.storeObject(null, testPackage, metadata);
		String storageId = storePromise.getValue();

		assertNotNull(storageId);
		assertFalse(storageId.isEmpty());

		// Should be able to retrieve with auto-generated ID
		Promise<EObject> retrievePromise = storageService.retrieveObject(storageId);
		EPackage retrievedPackage = (EPackage) retrievePromise.getValue();

		assertNotNull(retrievedPackage);
		assertEquals("AutoIdTest", retrievedPackage.getName());

		//		Cleanup
		Promise<Boolean> deletePromise = storageService.deleteObject(storageId);
		Boolean deleted = deletePromise.getValue();
		assertTrue(deleted);

	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	@DefaultApicurioStorageConfiguration
	public void testCustomFileExtension(
			@InjectService(cardinality = 0, filter = "(storage.backend=apicurio)")
			ServiceAware<EObjectStorageService> serviceAware) throws Exception {
		assertNotNull(serviceAware);

		EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware.waitForService(5000L);
		assertNotNull(storageService, "Storage service should be available");

		// Test with custom extension
		EPackage testPackage1 = EcoreFactory.eINSTANCE.createEPackage();
		testPackage1.setName("CustomExtTest1");

		ObjectMetadata metadata1 = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadata1.setUploadUser("testUser");
		metadata1.getProperties().put("file.extension", "json"); // without dot
		metadata1.setObjectRef(testPackage1);

		Promise<String> storePromise1 = storageService.storeObject("custom-ext-1", testPackage1, metadata1);
		String storageId1 = storePromise1.getValue();

		// Verify file with .json extension exists
		assertTrue(storageService.exists(storageId1), "Artifact should exist");

		// Test with default extension (no property set)
		EPackage testPackage2 = EcoreFactory.eINSTANCE.createEPackage();
		testPackage2.setName("DefaultExtTest");

		ObjectMetadata metadata2 = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadata2.setUploadUser("testUser");
		metadata2.setObjectRef(testPackage2);
		// No file.extension property set - should use default .xmi

		Promise<String> storePromise2 = storageService.storeObject("default-ext", testPackage2, metadata2);
		String storageId2 = storePromise2.getValue();

		// Verify file with .xmi extension exists
		assertTrue(storageService.exists(storageId2), "Artifact should exist");

		// Verify both can be retrieved
		Promise<EObject> retrievePromise1 = storageService.retrieveObject(storageId1);
		EObject retrieved1 = retrievePromise1.getValue();
		assertNotNull(retrieved1);
		assertTrue(retrieved1 instanceof EPackage);
		assertEquals("CustomExtTest1", ((EPackage) retrieved1).getName());

		Promise<EObject> retrievePromise2 = storageService.retrieveObject(storageId2);
		EObject retrieved2 = retrievePromise2.getValue();
		assertNotNull(retrieved2);
		assertTrue(retrieved2 instanceof EPackage);
		assertEquals("DefaultExtTest", ((EPackage) retrieved2).getName());

		//		Cleanup
		Promise<Boolean> deletePromise = storageService.deleteObject(storageId1);
		Boolean deleted = deletePromise.getValue();
		assertTrue(deleted);

		deletePromise = storageService.deleteObject(storageId2);
		deleted = deletePromise.getValue();
		assertTrue(deleted);

	}



	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	@DefaultApicurioStorageConfiguration
	public void testContentType(
			@InjectService(cardinality = 0, filter = "(storage.backend=apicurio)")
			ServiceAware<EObjectStorageService> serviceAware) throws Exception {
		assertNotNull(serviceAware);

		EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware.waitForService(5000L);
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

		Promise<String> storePromise = storageService.storeObject("content-type-test", testPackage, metadata);
		String storageId = storePromise.getValue();

		// Verify file exists
		assertTrue(storageService.exists(storageId));

		// Retrieve and verify
		Promise<EObject> retrievePromise = storageService.retrieveObject(storageId);
		EObject retrieved = retrievePromise.getValue();
		assertNotNull(retrieved);
		assertTrue(retrieved instanceof EPackage);
		assertEquals("ContentTypeTest", ((EPackage) retrieved).getName());

		//Cleanup
		Promise<Boolean> deletePromise = storageService.deleteObject(storageId);
		Boolean deleted = deletePromise.getValue();
		assertTrue(deleted);

	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	@DefaultApicurioStorageConfiguration
	public void testUpdateMetadata(
			@InjectService(cardinality = 0, filter = "(storage.backend=apicurio)")
			ServiceAware<EObjectStorageService> serviceAware) throws Exception {
		assertNotNull(serviceAware);

		EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware.waitForService(5000L);
		assertNotNull(storageService, "Storage service should be available");

		// Store initial object
		EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
		testPackage.setName("UpdateMetadataTest");

		ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadata.setUploadUser("originalUser");
		metadata.setSourceChannel("originalChannel");
		metadata.setContentHash("originalHash");
		metadata.setObjectType("EPackage");
		metadata.setUploadTime(Instant.now());
		metadata.getProperties().put("file.extension", ".ecore");
		metadata.setObjectRef(testPackage);

		Promise<String> storePromise = storageService.storeObject("update-metadata-test", testPackage, metadata);
		String storageId = storePromise.getValue();

		// Create updated metadata
		ObjectMetadata updatedMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
		updatedMetadata.setUploadUser("updatedUser");
		updatedMetadata.setSourceChannel("updatedChannel");
		updatedMetadata.setContentHash("updatedHash");
		updatedMetadata.setObjectType("EPackage");
		updatedMetadata.setUploadTime(Instant.now());
		updatedMetadata.setReviewUser("reviewUser");
		updatedMetadata.setReviewTime(Instant.now());
		updatedMetadata.setReviewReason("Updated for testing");
		updatedMetadata.getProperties().put("file.extension", ".ecore");
		updatedMetadata.getProperties().put("custom.property", "customValue");

		// Update metadata
		Promise<Boolean> updatePromise = storageService.updateMetadata(storageId, updatedMetadata);
		Boolean updateResult = updatePromise.getValue();
		assertTrue(updateResult, "Metadata update should succeed");

		// Retrieve and verify updated metadata
		Promise<ObjectMetadata> retrievePromise = storageService.retrieveMetadata(storageId);
		ObjectMetadata retrievedMetadata = retrievePromise.getValue();

		assertNotNull(retrievedMetadata);
		assertEquals("originalUser", retrievedMetadata.getUploadUser(), "Upload user should be immutable and preserved");
		assertEquals("updatedChannel", retrievedMetadata.getSourceChannel());
		assertEquals("updatedHash", retrievedMetadata.getContentHash());
		assertEquals("reviewUser", retrievedMetadata.getReviewUser());
		assertEquals("Updated for testing", retrievedMetadata.getReviewReason());
		assertEquals("customValue", retrievedMetadata.getProperties().get("custom.property"));

		// Verify object itself is unchanged
		Promise<EObject> objectPromise = storageService.retrieveObject(storageId);
		EPackage retrievedPackage = (EPackage) objectPromise.getValue();
		assertNotNull(retrievedPackage);
		assertEquals("UpdateMetadataTest", retrievedPackage.getName());

		//Cleanup
		Promise<Boolean> deletePromise = storageService.deleteObject(storageId);
		Boolean deleted = deletePromise.getValue();
		assertTrue(deleted);

	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	@DefaultApicurioStorageConfiguration
	public void testUpdateStatus(
			@InjectService(cardinality = 0, filter = "(storage.backend=apicurio)")
			ServiceAware<EObjectStorageService> serviceAware) throws Exception {
		assertNotNull(serviceAware);

		EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware.waitForService(5000L);
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

		Promise<String> storePromise = storageService.storeObject("update-status-test", testPackage, metadata);
		String storageId = storePromise.getValue();

		// Verify initial status
		Promise<ObjectMetadata> initialPromise = storageService.retrieveMetadata(storageId);
		ObjectMetadata initialMetadata = initialPromise.getValue();
		assertEquals(ObjectStatus.DRAFT, initialMetadata.getStatus());
		assertNull(initialMetadata.getLastChangeUser());
		assertNull(initialMetadata.getLastChangeTime());

		// Update status to APPROVED with change user
		Instant beforeUpdate = Instant.now();
		Promise<Boolean> updatePromise = storageService.updateStatus(storageId, ObjectStatus.APPROVED, "approverUser");
		Boolean updateResult = updatePromise.getValue();
		assertTrue(updateResult, "Status update should succeed");
		Instant afterUpdate = Instant.now();

		// Verify status update
		Promise<ObjectMetadata> updatedPromise = storageService.retrieveMetadata(storageId);
		ObjectMetadata updatedMetadata = updatedPromise.getValue();

		assertNotNull(updatedMetadata);
		assertEquals(ObjectStatus.APPROVED, updatedMetadata.getStatus());
		assertEquals("approverUser", updatedMetadata.getLastChangeUser());
		assertNotNull(updatedMetadata.getLastChangeTime());
		assertTrue(updatedMetadata.getLastChangeTime().isAfter(beforeUpdate) || updatedMetadata.getLastChangeTime().equals(beforeUpdate));
		assertTrue(updatedMetadata.getLastChangeTime().isBefore(afterUpdate) || updatedMetadata.getLastChangeTime().equals(afterUpdate));

		// Update status again without change user
		Promise<Boolean> updatePromise2 = storageService.updateStatus(storageId, ObjectStatus.DEPLOYED, null);
		Boolean updateResult2 = updatePromise2.getValue();
		assertTrue(updateResult2, "Second status update should succeed");

		// Verify second status update
		Promise<ObjectMetadata> finalPromise = storageService.retrieveMetadata(storageId);
		ObjectMetadata finalMetadata = finalPromise.getValue();

		assertEquals(ObjectStatus.DEPLOYED, finalMetadata.getStatus());
		// Change user should remain as previous value when null is passed
		assertEquals("approverUser", finalMetadata.getLastChangeUser());
		assertNotNull(finalMetadata.getLastChangeTime());

		//Cleanup
		Promise<Boolean> deletePromise = storageService.deleteObject(storageId);
		Boolean deleted = deletePromise.getValue();
		assertTrue(deleted);


	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	@DefaultApicurioStorageConfiguration
	public void testGetObjectCount(
			@InjectService(cardinality = 0, filter = "(storage.backend=apicurio)")
			ServiceAware<EObjectStorageService> serviceAware) throws Exception {
		assertNotNull(serviceAware);

		EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware.waitForService(5000L);
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

			Promise<String> storePromise = storageService.storeObject("count-test-" + i, pkg, metadata);
			storePromise.getValue();
		}

		// Count after storing 5 objects
		long countAfterStore = storageService.getObjectCount();
		assertEquals(5, countAfterStore, "Object count should be 5 after storing 5 objects");

		// Delete 2 objects
		Promise<Boolean> deletePromise1 = storageService.deleteObject("count-test-1");
		deletePromise1.getValue();
		Promise<Boolean> deletePromise2 = storageService.deleteObject("count-test-3");
		deletePromise2.getValue();


		// Verify metadata retrieval fails for deleted objects
		Promise<ObjectMetadata> metadataPromise1 = storageService.retrieveMetadata("count-test-1");
		assertNull(metadataPromise1.getValue(), "Should not retrieve metadata for deleted object 1");
		Promise<ObjectMetadata> metadataPromise3 = storageService.retrieveMetadata("count-test-3");
		assertNull(metadataPromise3.getValue(), "Should not retrieve metadata for deleted object 3");

		// Count after deleting 2 objects
		long countAfterDelete = storageService.getObjectCount();
		assertEquals(3, countAfterDelete, "Object count should be 3 after deleting 2 objects");

		// Verify count matches listObjectIds size
		Promise<List<String>> listPromise = storageService.listObjectIds();
		List<String> objectIds = listPromise.getValue();
		assertEquals(countAfterDelete, objectIds.size(), "Object count should match listObjectIds size");
		
		//Cleanup
		Promise<Boolean> deletePromise = storageService.deleteObject("count-test-0");
		Boolean deleted = deletePromise.getValue();
		assertTrue(deleted);
		
		deletePromise = storageService.deleteObject("count-test-2");
		deleted = deletePromise.getValue();
		assertTrue(deleted);
		
		deletePromise = storageService.deleteObject("count-test-4");
		deleted = deletePromise.getValue();
		assertTrue(deleted);

	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	@DefaultApicurioStorageConfiguration
	public void testUpdateMetadataErrorHandling(
			@InjectService(cardinality = 0, filter = "(storage.backend=apicurio)")
			ServiceAware<EObjectStorageService> serviceAware) throws Exception {
		assertNotNull(serviceAware);

		EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware.waitForService(5000L);
		assertNotNull(storageService, "Storage service should be available");

		// Test updating metadata for non-existent object
		ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadata.setUploadUser("testUser");
		metadata.setSourceChannel("testChannel");
		metadata.setContentHash("testHash");
		metadata.setObjectType("EPackage");
		metadata.setUploadTime(Instant.now());

		Promise<Boolean> updatePromise = storageService.updateMetadata("non-existent-id", metadata);
		Boolean updateResult = updatePromise.getValue();
		assertFalse(updateResult, "Updating metadata for non-existent object should return false");

	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	@DefaultApicurioStorageConfiguration
	public void testUpdateStatusErrorHandling(
			@InjectService(cardinality = 0, filter = "(storage.backend=apicurio)")
			ServiceAware<EObjectStorageService> serviceAware) throws Exception {
		assertNotNull(serviceAware);

		EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware.waitForService(5000L);
		assertNotNull(storageService, "Storage service should be available");

		// Test updating status for non-existent object
		Promise<Boolean> updatePromise = storageService.updateStatus("non-existent-id", ObjectStatus.APPROVED, "testUser");
		Boolean updateResult = updatePromise.getValue();
		assertFalse(updateResult, "Updating status for non-existent object should return false");

	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	@CustomRoleApicurioStorageConfiguration
	public void testRoleFunctionality(
			@InjectService(cardinality = 0, filter = "(storage.backend=apicurio)")
			ServiceAware<EObjectStorageService> serviceAware) throws Exception {
		assertNotNull(serviceAware);

		EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware.waitForService(5000L);
		assertNotNull(storageService, "Storage service should be available");

		// Create test EPackage
		EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
		testPackage.setName("RoleTestPackage");
		testPackage.setNsPrefix("roletest");
		testPackage.setNsURI("http://roletest.example.com");

		// Create metadata without role set
		ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadata.setObjectName("RoleTestPackage");
		metadata.setVersion("1.0.0");
		metadata.setStatus(ObjectStatus.DRAFT);
		metadata.setUploadUser("roleTestUser");
		metadata.setUploadTime(Instant.now());
		metadata.setSourceChannel("ROLE_TEST");
		// Note: role is intentionally not set to test automatic role setting
		metadata.setObjectRef(testPackage);

		// Test storeObject with automatic role setting
		Promise<String> storePromise = storageService.storeObject("role-test-id", testPackage, metadata);
		assertNull(storePromise.getFailure(), "Store operation should succeed");
		String objectId = storePromise.getValue();
		assertEquals("role-test-id", objectId);

		// Verify role was automatically set based on status
		assertEquals("custom-role", metadata.getRole(), "Role should be automatically set during storeObject");

		// Verify the object was actually stored and can be retrieved
		Promise<EObject> retrievePromise = storageService.retrieveObject("role-test-id");
		assertNull(retrievePromise.getFailure(), "Retrieve operation should succeed");
		EObject retrievedObject = retrievePromise.getValue();
		assertNotNull(retrievedObject);
		assertTrue(retrievedObject instanceof EPackage);
		assertEquals("RoleTestPackage", ((EPackage) retrievedObject).getName());

		// Verify metadata can be retrieved and has the correct role
		Promise<ObjectMetadata> metadataPromise = storageService.retrieveMetadata("role-test-id");
		assertNull(metadataPromise.getFailure(), "Metadata retrieve should succeed");
		ObjectMetadata retrievedMetadata = metadataPromise.getValue();
		assertNotNull(retrievedMetadata);
		assertEquals("custom-role", retrievedMetadata.getRole(), "Retrieved metadata should have the correct role");

		// Test updateMetadata with automatic role setting
		ObjectMetadata updatedMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
		updatedMetadata.setObjectName("UpdatedRoleTestPackage");
		updatedMetadata.setVersion("2.0.0");
		updatedMetadata.setStatus(ObjectStatus.APPROVED);
		updatedMetadata.setUploadUser("roleTestUser");
		updatedMetadata.setUploadTime(Instant.now());
		updatedMetadata.setSourceChannel("ROLE_TEST");
		// Note: role is intentionally not set to test automatic role setting

		Promise<Boolean> updatePromise = storageService.updateMetadata("role-test-id", updatedMetadata);
		assertNull(updatePromise.getFailure(), "Update metadata operation should succeed");
		Boolean updateResult = updatePromise.getValue();
		assertTrue(updateResult, "Update metadata should return true");

		// Verify original metadata object was NOT modified (copy-based approach)
		assertNull(updatedMetadata.getRole(), "Original metadata object should not be modified");

		// Verify the updated metadata was actually saved
		Promise<ObjectMetadata> updatedMetadataPromise = storageService.retrieveMetadata("role-test-id");
		assertNull(updatedMetadataPromise.getFailure(), "Updated metadata retrieve should succeed");
		ObjectMetadata finalMetadata = updatedMetadataPromise.getValue();
		assertNotNull(finalMetadata);
		assertEquals("UpdatedRoleTestPackage", finalMetadata.getObjectName());
		assertEquals("2.0.0", finalMetadata.getVersion());
		assertEquals(ObjectStatus.APPROVED, finalMetadata.getStatus());
		assertEquals("custom-role", finalMetadata.getRole(), "Final metadata should have the correct role");

		// Test role override: storage service role should override metadata role
		EPackage overrideTestPackage = EcoreFactory.eINSTANCE.createEPackage();
		overrideTestPackage.setName("RoleOverrideTest");
		
		ObjectMetadata metadataWithPresetRole = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadataWithPresetRole.setObjectName("RoleOverrideTest");
		metadataWithPresetRole.setVersion("1.0.0");
		metadataWithPresetRole.setStatus(ObjectStatus.DRAFT);
		metadataWithPresetRole.setUploadUser("overrideTestUser");
		metadataWithPresetRole.setUploadTime(Instant.now());
		metadataWithPresetRole.setSourceChannel("OVERRIDE_TEST");
		// Explicitly set a different role that should be overridden
		metadataWithPresetRole.setRole("wrong-role");
		metadataWithPresetRole.setObjectRef(overrideTestPackage);
		
		Promise<String> overrideStorePromise = storageService.storeObject("role-override-test", overrideTestPackage, metadataWithPresetRole);
		assertNull(overrideStorePromise.getFailure(), "Override store operation should succeed");
		String overrideObjectId = overrideStorePromise.getValue();
		assertEquals("role-override-test", overrideObjectId);
		
		// Verify the storage service role overrode the preset role
		assertEquals("custom-role", metadataWithPresetRole.getRole(), "Storage service role should override preset role in metadata");
		
		// Verify the stored metadata also has the correct role
		Promise<ObjectMetadata> overrideMetadataPromise = storageService.retrieveMetadata("role-override-test");
		assertNull(overrideMetadataPromise.getFailure(), "Override metadata retrieve should succeed");
		ObjectMetadata overrideRetrievedMetadata = overrideMetadataPromise.getValue();
		assertNotNull(overrideRetrievedMetadata);
		assertEquals("custom-role", overrideRetrievedMetadata.getRole(), "Retrieved metadata should have storage service role, not preset role");

//		Cleanup
		Promise<Boolean> deletePromise = storageService.deleteObject(objectId);
		Boolean deleted = deletePromise.getValue();
		assertTrue(deleted);
		
		deletePromise = storageService.deleteObject(overrideObjectId);
		deleted = deletePromise.getValue();
		assertTrue(deleted);
		
	}

	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	@RegistryConfiguration
	public void testDifferentStorageRoles(
			@InjectService(cardinality = 0, filter = "(storage.backend=apicurio)")
			ServiceAware<EObjectStorageService> serviceAware,
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(
					factoryPid = "ApicurioObjectStorage",
					location = "?",
					name = "test2"))
			Configuration config
	) throws Exception {
		assertNotNull(serviceAware);
		assertNotNull(config);
		assertTrue(serviceAware.isEmpty());

		Dictionary<String, Object> props = new Hashtable<>();
		props.put("workspace.folder", tempDir.toString());
		props.put("storage.role", "approved");
		config.update(props);

		EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware.waitForService(5000L);
		assertNotNull(storageService, "Storage service should be available");

		// Create test object and metadata
		EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
		testPackage.setName("ApprovedTestPackage");

		ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadata.setObjectName("ApprovedTestPackage");
		metadata.setVersion("1.0.0");
		metadata.setStatus(ObjectStatus.APPROVED);
		metadata.setUploadUser("approvedUser");
		metadata.setUploadTime(Instant.now());
		metadata.setSourceChannel("APPROVED_TEST");
		metadata.setObjectRef(testPackage);

		// Store object
		Promise<String> storePromise = storageService.storeObject("approved-test-id", testPackage, metadata);
		assertNull(storePromise.getFailure(), "Store operation should succeed");
		
		// Verify the configured storage role was set
		assertEquals("approved", metadata.getRole(), "Role should be set to configured storage_role 'approved'");
		
//		Cleanup Apicurio
		Promise<Boolean> deletePromise = storageService.deleteObject("approved-test-id");
		Boolean deleted = deletePromise.getValue();
		assertTrue(deleted);

		// Clean up configuration
		config.delete();
		Thread.sleep(500);
		assertTrue(serviceAware.isEmpty());
		

	}
	
	@SuppressWarnings({"rawtypes", "unchecked"})
	@Test
	@RegistryConfiguration
	public void testServiceActivationWithDefaultRole(
			@InjectService(cardinality = 0, filter = "(storage.backend=apicurio)")
			ServiceAware<EObjectStorageService> serviceAware,
			@InjectService(cardinality = 0, filter = "(registry.type=shared)")
			ServiceAware<EObjectRegistryService> registryAware,
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(
					factoryPid = "ApicurioObjectStorage",
					location = "?",
					name = "defaultRoleTest")) Configuration config
			) throws Exception {
		assertNotNull(serviceAware);
		assertNotNull(registryAware);
		assertNotNull(config);
		assertTrue(serviceAware.isEmpty());

		// Registry service should be available (configured by annotation)
		EObjectRegistryService<EObject> registryService = (EObjectRegistryService<EObject>) registryAware.waitForService(5000L);
		assertNotNull(registryService, "Registry service should be available");

		// Test configuration without explicit storage_role - should use default "draft"
		Dictionary<String, Object> props = new Hashtable<>();
		props.put("workspace.folder", tempDir.toString());
		// Note: storage_role is intentionally omitted to test default value
		config.update(props);

		// Service should activate with default role
		EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware.waitForService(5000L);
		assertNotNull(storageService, "Service should activate with default storage_role");

		// Test that default role is used
		EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
		testPackage.setName("DefaultRoleTest");

		ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadata.setUploadUser("defaultUser");
		metadata.setSourceChannel("DEFAULT_TEST");
		metadata.getProperties().put("file.extension", ".ecore");
		metadata.setObjectRef(testPackage);

		Promise<String> storePromise = storageService.storeObject("default-role-test", testPackage, metadata);
		String objectId = storePromise.getValue();
		assertEquals("default-role-test", objectId);

		// Verify default role "draft" was set
		assertEquals("draft", metadata.getRole(), "Should use default role 'draft' when not explicitly configured");
		
//		Cleanup Apicurio
		Promise<Boolean> deletePromise = storageService.deleteObject(objectId);
		Boolean deleted = deletePromise.getValue();
		assertTrue(deleted);

		// Clean up
		config.delete();
		Thread.sleep(500);
		assertTrue(serviceAware.isEmpty());
	}
	
	/**
	 * Integration test for storage-registry interaction.
	 * 
	 * Tests that objects stored via storage service can be found using registry service findByStatus.
	 * This verifies the registry is properly updated when objects are stored.
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Test
	@RegistryConfiguration
	@DefaultApicurioStorageConfiguration
	public void testStorageRegistryIntegration(
			@InjectService(cardinality = 0, filter = "(storage.backend=apicurio)")
			ServiceAware<EObjectStorageService> serviceAware,
			@InjectService(cardinality = 0, filter = "(registry.type=shared)")
			ServiceAware<EObjectRegistryService> registryAware
	) throws Exception {
		
		EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware.waitForService(5000L);
		assertNotNull(storageService, "Storage service should be available");
		// Registry service should be available (configured by annotation)
		EObjectRegistryService<EObject> registryService = (EObjectRegistryService<EObject>) registryAware.waitForService(5000L);
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
		String draftObjectId = storageService.storeObject("registry-test-draft", testPackage, draftMetadata).getValue();
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
//		We have to update the version if we want the update to succeed in Apicurio
		draftMetadata.setVersion("1.1.0");
		assertTrue(storageService.updateMetadata(draftObjectId, draftMetadata).getValue(), "Metadata should be updated");
		
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
		
		String rejectedObjectId = storageService.storeObject("registry-test-rejected", rejectedPackage, rejectedMetadata).getValue();
		assertNotNull(rejectedObjectId, "Rejected object ID should not be null");
		
		// Wait a moment for registry update (if async)
		Thread.sleep(100);
		
		// Verify we can find REJECTED objects using registry
		List<ObjectMetadata> rejectedObjects = registryService.findByStatus(ObjectStatus.REJECTED);
		assertNotNull(rejectedObjects, "Rejected objects list should not be null");
		assertTrue(rejectedObjects.stream().anyMatch(obj -> rejectedObjectId.equals(obj.getObjectId())), 
				  "Registry should find the stored rejected object by status");
		
		// Clean up
		storageService.deleteObject(draftObjectId).getValue();
		storageService.deleteObject(rejectedObjectId).getValue();
	}
}
