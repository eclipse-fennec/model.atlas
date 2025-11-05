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
package org.eclipse.fennec.model.atlas.management.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for FileStorageHelper
 */
public class FileStorageHelperTest {
    
    @TempDir
    Path tempDir;
    
    private FileStorageHelper helper;
    private ResourceSet resourceSet;
    
    @BeforeEach
    public void setup() {
        resourceSet = new ResourceSetImpl();
        
        // Register required EPackages
        resourceSet.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
        resourceSet.getPackageRegistry().put(ManagementPackage.eNS_URI, ManagementPackage.eINSTANCE);
        
        // Register resource factories for file extensions
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new XMIResourceFactoryImpl());
        
        helper = new FileStorageHelper(resourceSet, tempDir);
    }
    
    @AfterEach
    public void tearDown() throws Exception {
        if (helper != null) {
            helper.close();
        }
    }
    
    // File extension and content type tests are now in AbstractStorageHelperTest
    // These tests focus on file-specific functionality
    
    @Test
    public void testSaveAndLoadEObject() throws Exception {
        // Create test data
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("TestPackage");
        testPackage.setNsPrefix("test");
        testPackage.setNsURI("http://test/1.0");
        
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.getProperties().put("file.extension", ".ecore");
        
        // Save
        helper.saveEObject("test-id", testPackage, metadata);
        
        // Verify file exists
        File ecoreFile = new File(tempDir.toFile(), "test-id.ecore");
        assertTrue(ecoreFile.exists());
        
        // Load
        EPackage loaded = (EPackage) helper.loadEObject("test-id");
        assertNotNull(loaded);
        assertEquals("TestPackage", loaded.getName());
        assertEquals("test", loaded.getNsPrefix());
        assertEquals("http://test/1.0", loaded.getNsURI());
    }
    
    @Test
    public void testSaveAndLoadMetadata() throws Exception {
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setUploadUser("testUser");
        Instant originalTime = Instant.now();
        metadata.setUploadTime(originalTime);
        metadata.setSourceChannel("testChannel");
        metadata.setContentHash("testhash123");
        
        // Save
        helper.saveMetadata("meta-test", metadata);
        
        // Verify file exists
        File metadataFile = new File(tempDir.toFile(), "meta-test.metadata.xmi");
        assertTrue(metadataFile.exists());
        
        // Load
        ObjectMetadata loaded = helper.loadMetadata("meta-test");
        assertNotNull(loaded);
        assertEquals("testUser", loaded.getUploadUser());
        assertEquals("testChannel", loaded.getSourceChannel());
        assertEquals("testhash123", loaded.getContentHash());
        
        // Verify that uploadTime round-trip conversion works correctly
        assertNotNull(loaded.getUploadTime(), "UploadTime should not be null after loading");
        assertEquals(originalTime, loaded.getUploadTime(), "UploadTime should match after round-trip conversion");
    }
    
    @Test
    public void testFileSystemIntegration() throws Exception {
        // Test that files are actually created in the file system
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("FileSystemTest");
        
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.getProperties().put("file.extension", ".ecore");
        
        helper.saveEObject("filesystem-test", testPackage, metadata);
        
        // Verify actual file exists on filesystem
        File ecoreFile = new File(tempDir.toFile(), "filesystem-test.ecore");
        assertTrue(ecoreFile.exists(), "Ecore file should exist on filesystem");
        assertTrue(ecoreFile.length() > 0, "Ecore file should have content");
    }
    
    @Test
    public void testDeleteObject() throws Exception {
        // Create test files
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("DeleteTest");
        
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setUploadUser("testUser");
        metadata.getProperties().put("file.extension", ".ecore");
        
        helper.saveEObject("delete-test", testPackage, metadata);
        helper.saveMetadata("delete-test", metadata);
        
        // Verify files exist
        File ecoreFile = new File(tempDir.toFile(), "delete-test.ecore");
        File metadataFile = new File(tempDir.toFile(), "delete-test.metadata.xmi");
        assertTrue(ecoreFile.exists());
        assertTrue(metadataFile.exists());
        
        // Delete
        boolean deleted = helper.deleteObject("delete-test");
        assertTrue(deleted);
        
        // Verify files are gone
        assertFalse(ecoreFile.exists());
        assertFalse(metadataFile.exists());
    }
    
    @Test
    public void testListObjectIds() throws Exception {
        // Create multiple test objects
        for (int i = 0; i < 3; i++) {
            EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
            pkg.setName("Package" + i);
            
            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setUploadUser("testUser");
            metadata.getProperties().put("file.extension", ".ecore");
            
            helper.saveEObject("test-pkg-" + i, pkg, metadata);
        }
        
        // List object IDs
        List<String> objectIds = helper.listObjectIds();
        
        assertEquals(3, objectIds.size());
        assertTrue(objectIds.contains("test-pkg-0"));
        assertTrue(objectIds.contains("test-pkg-1"));
        assertTrue(objectIds.contains("test-pkg-2"));
    }
    
    @Test
    public void testResourceOperationCleanup() throws Exception {
        // This test verifies that resources are properly cleaned up
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("CleanupTest");
        
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.getProperties().put("file.extension", ".ecore");
        
        // Save object
        helper.saveEObject("cleanup-test", testPackage, metadata);
        
        // ResourceSet should not contain any resources after the operation
        // (they should have been cleaned up)
        assertTrue(resourceSet.getResources().isEmpty(), 
            "ResourceSet should be empty after operation with cleanup");
    }
    
    @Test
    public void testLoadNonExistentObject() throws Exception {
        EPackage loaded = (EPackage) helper.loadEObject("non-existent");
        assertNull(loaded);
    }
    
    @Test
    public void testLoadNonExistentMetadata() throws Exception {
        ObjectMetadata loaded = helper.loadMetadata("non-existent");
        assertNull(loaded);
    }
}