/**
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
package org.gecko.mac.management.file.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.gecko.mac.management.file.tests.annotations.FileTestAnnotations.DefaultFileStorageSetup;
import org.gecko.mac.mgmt.api.EObjectStorageService;
import org.gecko.mac.mgmt.management.ManagementFactory;
import org.gecko.mac.mgmt.management.ObjectMetadata;
import org.gecko.mac.mgmt.management.ObjectStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.osgi.util.promise.Promise;

/**
 * Integration tests for data integrity validation by corrupting actual stored files.
 * 
 * <p>This test class validates that the storage system properly detects and handles
 * data corruption scenarios by actually modifying stored metadata files on disk
 * and verifying that appropriate exceptions are thrown.</p>
 * 
 * <h3>Test Scenarios</h3>
 * <ul>
 * <li><strong>ObjectId Mismatch</strong> - Metadata file contains different objectId than filename</li>
 * <li><strong>Missing ObjectId</strong> - Metadata file has null or empty objectId</li>
 * <li><strong>Corrupted File Content</strong> - Invalid XMI content that fails to parse</li>
 * </ul>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class DataIntegrityValidationIntegrationTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // Set system property for template argument resolution
        System.setProperty("tempDir", tempDir.toString());
    }

    /**
     * Tests that loading metadata with corrupted objectId throws appropriate exception.
     * 
     * <p>This test:</p>
     * <ol>
     * <li>Stores a valid object with metadata</li>
     * <li>Manually corrupts the metadata file by changing the objectId</li>
     * <li>Attempts to retrieve the metadata</li>
     * <li>Verifies that data integrity violation is detected and exception thrown</li>
     * </ol>
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
    @DefaultFileStorageSetup
    public void testLoadMetadataWithCorruptedObjectIdThrowsException(
            @InjectService(cardinality = 0, filter = "(storage.backend=file)")
            ServiceAware<EObjectStorageService> serviceAware) throws Exception {
        
        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware.waitForService(5000L);
        assertNotNull(storageService, "Storage service should be available");

        // === PHASE 1: Store valid object ===
        
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("CorruptionTestPackage");
        testPackage.setNsPrefix("corruption");
        testPackage.setNsURI("http://corruption.test/1.0");

        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectName("CorruptionTestPackage");
        metadata.setVersion("1.0.0");
        metadata.setUploadUser("corruption-test-user");
        metadata.setSourceChannel("CORRUPTION_TEST");
        metadata.setStatus(ObjectStatus.DRAFT);
        metadata.setUploadTime(Instant.now());
        
        String objectId = "corruption-test-object";
        Promise<String> storePromise = storageService.storeObject(objectId, testPackage, metadata);
        String storedObjectId = storePromise.getValue();
        
        // Verify object was stored successfully
        assertTrue(storageService.exists(storedObjectId), "Object should exist after storage");
        
        // === PHASE 2: Corrupt the metadata file ===
        
        Path metadataFile = tempDir.resolve(objectId + ".metadata.xmi");
        assertTrue(Files.exists(metadataFile), "Metadata file should exist");
        
        // Read the original metadata file content
        String originalContent = Files.readString(metadataFile);
        
        // Corrupt the content by changing the objectId in the XMI
        String corruptedContent = originalContent.replace(
            "objectId=\"" + objectId + "\"",
            "objectId=\"corrupted-different-id\""
        );
        
        // Verify we actually changed something
        assertTrue(!originalContent.equals(corruptedContent), "Content should be different after corruption");
        
        // Write the corrupted content back to the file
        Files.writeString(metadataFile, corruptedContent);
        
        // === PHASE 3: Attempt to load corrupted metadata ===
        
        Promise<ObjectMetadata> retrievePromise = storageService.retrieveMetadata(objectId);
        
        // Verify the promise failed with data integrity violation
        Throwable failure = retrievePromise.getFailure();
        assertNotNull(failure, "Promise should have failed due to data integrity violation");
        assertTrue(failure instanceof RuntimeException, "Should be RuntimeException wrapper");
        assertTrue(failure.getCause() instanceof IllegalStateException, "Should have IllegalStateException cause");
        assertTrue(failure.getCause().getMessage().contains("Data integrity violation"), 
                  "Should mention data integrity violation");
        assertTrue(failure.getCause().getMessage().contains("does not match requested objectId"), 
                  "Should mention objectId mismatch");
    }

    /**
     * Tests that loading metadata with missing objectId throws appropriate exception.
     * 
     * <p>This test:</p>
     * <ol>
     * <li>Stores a valid object with metadata</li>
     * <li>Manually corrupts the metadata file by removing the objectId attribute</li>
     * <li>Attempts to retrieve the metadata</li>
     * <li>Verifies that data integrity violation is detected and exception thrown</li>
     * </ol>
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @DefaultFileStorageSetup
    public void testLoadMetadataWithMissingObjectIdThrowsException(
            @InjectService(cardinality = 0, filter = "(storage.backend=file)")
            ServiceAware<EObjectStorageService> serviceAware) throws Exception {
        
        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware.waitForService(5000L);
        assertNotNull(storageService, "Storage service should be available");

        // === PHASE 1: Store valid object ===
        
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("MissingIdTestPackage");

        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectName("MissingIdTestPackage");
        metadata.setVersion("1.0.0");
        metadata.setUploadUser("missing-id-test-user");
        metadata.setStatus(ObjectStatus.DRAFT);
        metadata.setUploadTime(Instant.now());
        
        String objectId = "missing-id-test-object";
        Promise<String> storePromise = storageService.storeObject(objectId, testPackage, metadata);
        storePromise.getValue();
        
        // === PHASE 2: Corrupt the metadata file by removing objectId ===
        
        Path metadataFile = tempDir.resolve(objectId + ".metadata.xmi");
        assertTrue(Files.exists(metadataFile), "Metadata file should exist");
        
        // Read and corrupt the content by removing the objectId attribute
        String originalContent = Files.readString(metadataFile);
        String corruptedContent = originalContent.replaceAll("objectId=\"[^\"]*\"\\s*", "");
        
        // Verify we actually changed something
        assertTrue(!originalContent.equals(corruptedContent), "Content should be different after corruption");
        
        // Write the corrupted content back
        Files.writeString(metadataFile, corruptedContent);
        
        // === PHASE 3: Attempt to load corrupted metadata ===
        
        Promise<ObjectMetadata> retrievePromise = storageService.retrieveMetadata(objectId);
        
        // Verify the promise failed with data integrity violation
        Throwable failure = retrievePromise.getFailure();
        assertNotNull(failure, "Promise should have failed due to missing objectId");
        assertTrue(failure instanceof RuntimeException, "Should be RuntimeException wrapper");
        assertTrue(failure.getCause() instanceof IllegalStateException, "Should have IllegalStateException cause");
        assertTrue(failure.getCause().getMessage().contains("Data integrity violation"), 
                  "Should mention data integrity violation");
        assertTrue(failure.getCause().getMessage().contains("has no objectId set"), 
                  "Should mention missing objectId");
    }

    /**
     * Tests that loading completely corrupted metadata file throws appropriate exception.
     * 
     * <p>This test:</p>
     * <ol>
     * <li>Stores a valid object with metadata</li>
     * <li>Completely corrupts the metadata file with invalid XMI content</li>
     * <li>Attempts to retrieve the metadata</li>
     * <li>Verifies that parsing fails gracefully with appropriate exception</li>
     * </ol>
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @DefaultFileStorageSetup
    public void testLoadCompletelyCorruptedMetadataThrowsException(
            @InjectService(cardinality = 0, filter = "(storage.backend=file)")
            ServiceAware<EObjectStorageService> serviceAware) throws Exception {
        
        EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware.waitForService(5000L);
        assertNotNull(storageService, "Storage service should be available");

        // === PHASE 1: Store valid object ===
        
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("CorruptedFileTestPackage");

        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectName("CorruptedFileTestPackage");
        metadata.setVersion("1.0.0");
        metadata.setUploadUser("corrupted-file-test-user");
        metadata.setStatus(ObjectStatus.DRAFT);
        metadata.setUploadTime(Instant.now());
        
        String objectId = "corrupted-file-test-object";
        Promise<String> storePromise = storageService.storeObject(objectId, testPackage, metadata);
        storePromise.getValue();
        
        // === PHASE 2: Completely corrupt the metadata file ===
        
        Path metadataFile = tempDir.resolve(objectId + ".metadata.xmi");
        assertTrue(Files.exists(metadataFile), "Metadata file should exist");
        
        // Write completely invalid content
        String corruptedContent = "This is not valid XMI content at all! <invalid><xml>corrupted</broken>";
        Files.writeString(metadataFile, corruptedContent);
        
        // === PHASE 3: Attempt to load corrupted metadata ===
        
        Promise<ObjectMetadata> retrievePromise = storageService.retrieveMetadata(objectId);
        
        // Verify the promise failed due to parsing error
        Throwable failure = retrievePromise.getFailure();
        assertNotNull(failure, "Promise should have failed due to corrupted file");
        assertTrue(failure instanceof RuntimeException, "Should be RuntimeException wrapper");
        // The specific exception type may vary depending on EMF's parsing behavior
        // but it should indicate a parsing or IO problem
    }
}