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
package org.eclipse.fennec.model.atlas.mgmt.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for AbstractStorageHelper.
 * Tests the shared EMF resource management functionality.
 */
public class AbstractStorageHelperTest {
    
    private TestableStorageHelper helper;
    private ResourceSet resourceSet;
    
    /**
     * Testable implementation of AbstractStorageHelper for unit testing.
     */
    private static class TestableStorageHelper extends AbstractStorageHelper {
        
        // Test data for controlled responses
        private boolean metadataExists = false;
        private String objectPathToReturn = null;
        
        public TestableStorageHelper(ResourceSet resourceSet) {
            super(resourceSet);
        }
        
        @Override
        protected URI createStorageURI(String path) {
            return URI.createURI("test://" + path);
        }
        
        @Override
        protected void persistResource(String path, Resource resource) throws IOException {
            // No-op for testing - we're testing EMF serialization, not persistence
        }
        
        @Override
        protected boolean storageExists(String path) throws IOException {
            // Return true if it's a metadata path and we want to simulate metadata existence
            return path.endsWith(".metadata.xmi") && metadataExists;
        }
        
        @Override
        protected String findObjectPath(String objectId) throws IOException {
            return objectPathToReturn;
        }
        
        @Override
        public boolean deleteObject(String objectId) throws IOException {
            return false; // Simplified for testing
        }
        
        @Override
        public List<String> listObjectIds() throws IOException {
            return List.of(); // Simplified for testing
        }
        
        // Test helper methods
        public void setMetadataExists(boolean exists) {
            this.metadataExists = exists;
        }
        
        public void setObjectPathToReturn(String path) {
            this.objectPathToReturn = path;
        }
    }
    
    @BeforeEach
    public void setup() {
        resourceSet = new ResourceSetImpl();
        
        // Register required EPackages
        resourceSet.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
        resourceSet.getPackageRegistry().put(ManagementPackage.eNS_URI, ManagementPackage.eINSTANCE);
        
        // Register resource factories for file extensions
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
        resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("ecore", new XMIResourceFactoryImpl());
        
        // Register resource factory for test URI scheme
        resourceSet.getResourceFactoryRegistry().getProtocolToFactoryMap().put("test", new XMIResourceFactoryImpl());
        
        helper = new TestableStorageHelper(resourceSet);
    }
    
    @Test
    public void testGetFileExtensionWithCustomExtension() {
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.getProperties().put("file.extension", ".ecore");
        
        String extension = helper.getFileExtension(metadata);
        assertEquals(".ecore", extension);
    }
    
    @Test
    public void testGetFileExtensionWithoutDot() {
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.getProperties().put("file.extension", "ecore");
        
        String extension = helper.getFileExtension(metadata);
        assertEquals(".ecore", extension);
    }
    
    @Test
    public void testGetFileExtensionDefault() {
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        // No file.extension property
        
        String extension = helper.getFileExtension(metadata);
        assertEquals(".xmi", extension);
    }
    
    @Test
    public void testGetContentType() {
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.getProperties().put("content.type", "org.eclipse.emf.ecore");
        
        String contentType = helper.getContentType(metadata);
        assertEquals("org.eclipse.emf.ecore", contentType);
    }
    
    @Test
    public void testGetContentTypeNull() {
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        // No content.type property
        
        String contentType = helper.getContentType(metadata);
        assertEquals(TestableStorageHelper.DEFAULT_CONTENT_TYPE, contentType);
    }
    
    @Test
    public void testCreateResource() {
        URI testUri = URI.createURI("test://example.xmi");
        
        AbstractStorageHelper.ResourceOperation operation = helper.createResource(testUri, null);
        
        assertNotNull(operation);
        assertNotNull(operation.getResource());
        assertEquals(testUri, operation.getResource().getURI());
        
        // Test cleanup
        operation.cleanup();
        assertTrue(resourceSet.getResources().isEmpty(), "ResourceSet should be empty after cleanup");
    }
    
    @Test
    public void testCreateResourceWithContentType() {
        URI testUri = URI.createURI("test://example.ecore");
        
        // Test without content type factory (should fallback to normal creation)
        AbstractStorageHelper.ResourceOperation operation = helper.createResource(testUri, "org.eclipse.emf.ecore");
        
        assertNotNull(operation);
        assertNotNull(operation.getResource());
        
        operation.cleanup();
    }
    
    @Test
    public void testBuildObjectPath() {
        String objectPath = helper.buildObjectPath("test-id", ".ecore");
        assertEquals("test-id.ecore", objectPath);
    }
    
    @Test
    public void testBuildMetadataPath() {
        String metadataPath = helper.buildMetadataPath("test-id");
        assertEquals("test-id.metadata.xmi", metadataPath);
    }
    
    @Test
    public void testResourceOperationCleanup() {
        URI testUri = URI.createURI("test://cleanup-test.xmi");
        
        AbstractStorageHelper.ResourceOperation operation = helper.createResource(testUri, null);
        Resource resource = operation.getResource();
        
        // Verify resource is in ResourceSet
        assertTrue(resourceSet.getResources().contains(resource));
        
        // Cleanup
        operation.cleanup();
        
        // Verify resource is removed from ResourceSet
        assertTrue(resourceSet.getResources().isEmpty(), "ResourceSet should be empty after cleanup");
    }
    
    @Test
    public void testResourceOperationCleanupWithNullResource() {
        // Test that cleanup handles null resources gracefully
        AbstractStorageHelper.ResourceOperation operation = new AbstractStorageHelper.ResourceOperation(null, resourceSet);
        
        // Should not throw exception
        operation.cleanup();
    }
    
    @Test
    public void testObjectExistsWithMetadata() throws IOException {
        // Test when metadata exists
        helper.setMetadataExists(true);
        helper.setObjectPathToReturn(null); // Object file doesn't exist
        
        boolean exists = helper.objectExists("test-object");
        assertTrue(exists, "Should return true when metadata exists");
    }
    
    @Test
    public void testObjectExistsWithObjectFile() throws IOException {
        // Test when only object file exists (no metadata)
        helper.setMetadataExists(false);
        helper.setObjectPathToReturn("test-object.ecore"); // Object file exists
        
        boolean exists = helper.objectExists("test-object");
        assertTrue(exists, "Should return true when object file exists");
    }
    
    @Test
    public void testObjectExistsWithBoth() throws IOException {
        // Test when both metadata and object file exist
        helper.setMetadataExists(true);
        helper.setObjectPathToReturn("test-object.xmi");
        
        boolean exists = helper.objectExists("test-object");
        assertTrue(exists, "Should return true when both metadata and object exist");
    }
    
    @Test
    public void testObjectExistsWithNeither() throws IOException {
        // Test when neither metadata nor object file exist
        helper.setMetadataExists(false);
        helper.setObjectPathToReturn(null);
        
        boolean exists = helper.objectExists("test-object");
        assertFalse(exists, "Should return false when nothing exists");
    }
    
    @Test
    public void testObjectExistsWithNullObjectId() throws IOException {
        // Test null object ID handling
        helper.setMetadataExists(true);
        helper.setObjectPathToReturn("some-path");
        
        boolean exists = helper.objectExists(null);
        assertFalse(exists, "Should return false for null object ID");
    }
    
    @Test
    public void testObjectExistsWithEmptyObjectId() throws IOException {
        // Test empty object ID handling
        helper.setMetadataExists(true);
        helper.setObjectPathToReturn("some-path");
        
        boolean exists = helper.objectExists("");
        assertFalse(exists, "Should return false for empty object ID");
    }
}