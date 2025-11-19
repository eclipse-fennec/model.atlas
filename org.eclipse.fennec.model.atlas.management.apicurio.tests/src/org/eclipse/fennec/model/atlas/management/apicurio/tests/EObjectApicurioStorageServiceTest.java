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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.model.atlas.management.apicurio.tests.annotations.ApicurioTestAnnotations;
import org.eclipse.fennec.model.atlas.management.apicurio.tests.annotations.ApicurioTestAnnotations.DefaultApicurioStorageConfiguration;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.StorageBackendType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.osgi.util.promise.Promise;

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
	
	@BeforeEach
	public void before(@InjectBundleContext BundleContext ctx) {
		assertNotNull(context, "BundleContext should not be null");
		assertNotNull(tempDir, "TempDir should not be null");
		
		// Set system property for template argument resolution
		System.setProperty(ApicurioTestAnnotations.PROP_TEMP_DIR, tempDir.toString());
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
	public void testStoreAndRetrieveEPackageInXMI(
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
		metadata.setContentHash("testhash1234");
		metadata.setVersion("1.0.0");
		
		// Specify .xmi extension for EPackage
		metadata.getProperties().put("file.extension", ".json");
		metadata.getProperties().put("content.type", "application/json");

		// Store the package
		Promise<String> storePromise = storageService.storeObject("test-id-1234", testPackage, metadata);
		String storageId = storePromise.getValue();

		assertNotNull(storageId);
		assertEquals("test-id-123", storageId);

		// Verify files were created
//		File ecoreFile = new File(tempDir.toFile(), storageId + ".ecore");
//		File metadataFile = new File(tempDir.toFile(), storageId + ".metadata.xmi");
//		assertTrue(ecoreFile.exists(), "Ecore file should exist");
//		assertTrue(metadataFile.exists(), "Metadata file should exist");
//
//		// Retrieve the package
//		Promise<EObject> retrievePromise = storageService.retrieveObject(storageId);
//		EPackage retrievedPackage = (EPackage) retrievePromise.getValue();
//
//		assertNotEquals(testPackage, retrievedPackage);
//		assertNotNull(retrievedPackage);
//		assertEquals("TestPackage", retrievedPackage.getName());
//		assertEquals("test", retrievedPackage.getNsPrefix());
//		assertEquals("http://test/1.0", retrievedPackage.getNsURI());
//
//		// Retrieve metadata
//		Promise<ObjectMetadata> metadataPromise = storageService.retrieveMetadata(storageId);
//		ObjectMetadata retrievedMetadata = metadataPromise.getValue();
//
//		assertNotNull(retrievedMetadata);
//		assertEquals("testUser", retrievedMetadata.getUploadUser());
//		assertEquals("testChannel", retrievedMetadata.getSourceChannel());
//		assertEquals("testhash123", retrievedMetadata.getContentHash());

	}


}
