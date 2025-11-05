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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.gecko.mac.management.file.tests.annotations.FileTestAnnotations;
import org.gecko.mac.mgmt.api.EObjectRegistryService;
import org.gecko.mac.mgmt.api.EObjectStorageService;
import org.gecko.mac.mgmt.management.ManagementFactory;
import org.gecko.mac.mgmt.management.ObjectMetadata;
import org.gecko.mac.mgmt.management.ObjectStatus;
import org.gecko.mac.mgmt.management.StorageBackendType;
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
 * Integration tests for multi-role file storage scenarios.
 * 
 * <p>This test class focuses on scenarios involving multiple storage services
 * with different roles running simultaneously. It verifies:</p>
 * 
 * <ul>
 * <li><strong>Role Isolation</strong> - Each storage service operates independently</li>
 * <li><strong>Role Assignment</strong> - Storage services override metadata roles correctly</li>
 * <li><strong>Data Separation</strong> - Objects are stored in role-specific directories</li>
 * <li><strong>Cross-Service Security</strong> - Services cannot access each other's data</li>
 * </ul>
 * 
 * @author Mark Hoffmann
 * @since 1.0.0
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class MultiRoleFileStorageTest {

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        // Set system property for template argument resolution
        System.setProperty(FileTestAnnotations.PROP_TEMP_DIR, tempDir.toString());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    @FileTestAnnotations.MultiRoleFileStorageSetup
    public void testMultiRoleStorageServices(
            @InjectService(cardinality = 0, filter = "(&(storage.backend=file)(storage.role=draft))")
            ServiceAware<EObjectStorageService> draftServiceAware,
            @InjectService(cardinality = 0, filter = "(&(storage.backend=file)(storage.role=approved))")
            ServiceAware<EObjectStorageService> approvedServiceAware,
            @InjectService(cardinality = 0, filter = "(&(storage.backend=file)(storage.role=documentation))")
            ServiceAware<EObjectStorageService> docServiceAware
    ) throws Exception {
        
        // Wait for all three storage services to become available
        EObjectStorageService<EObject> draftService = (EObjectStorageService<EObject>) draftServiceAware.waitForService(5000L);
        assertNotNull(draftService, "Draft storage service should be available");
        
        EObjectStorageService<EObject> approvedService = (EObjectStorageService<EObject>) approvedServiceAware.waitForService(5000L);
        assertNotNull(approvedService, "Approved storage service should be available");
        
        EObjectStorageService<EObject> docService = (EObjectStorageService<EObject>) docServiceAware.waitForService(5000L);
        assertNotNull(docService, "Documentation storage service should be available");

        // Create test packages for each role
        EPackage draftPackage = EcoreFactory.eINSTANCE.createEPackage();
        draftPackage.setName("DraftPackage");
        draftPackage.setNsPrefix("draft");
        draftPackage.setNsURI("http://draft.example.com");

        EPackage approvedPackage = EcoreFactory.eINSTANCE.createEPackage();
        approvedPackage.setName("ApprovedPackage");
        approvedPackage.setNsPrefix("approved");
        approvedPackage.setNsURI("http://approved.example.com");

        EPackage docPackage = EcoreFactory.eINSTANCE.createEPackage();
        docPackage.setName("DocumentationPackage");
        docPackage.setNsPrefix("doc");
        docPackage.setNsURI("http://doc.example.com");

        // Create metadata objects with preset roles that should be overridden
        ObjectMetadata draftMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        draftMetadata.setObjectName("DraftPackage");
        draftMetadata.setVersion("1.0.0");
        draftMetadata.setStatus(ObjectStatus.DRAFT);
        draftMetadata.setUploadUser("draftUser");
        draftMetadata.setUploadTime(Instant.now());
        draftMetadata.setSourceChannel("DRAFT_CHANNEL");
        draftMetadata.setRole("wrong-role"); // Should be overridden to "draft"
        draftMetadata.getProperties().put("file.extension", ".ecore");

        ObjectMetadata approvedMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        approvedMetadata.setObjectName("ApprovedPackage");
        approvedMetadata.setVersion("2.0.0");
        approvedMetadata.setStatus(ObjectStatus.APPROVED);
        approvedMetadata.setUploadUser("approvedUser");
        approvedMetadata.setUploadTime(Instant.now());
        approvedMetadata.setSourceChannel("APPROVED_CHANNEL");
        approvedMetadata.setRole("wrong-role"); // Should be overridden to "approved"
        approvedMetadata.getProperties().put("file.extension", ".ecore");

        ObjectMetadata docMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        docMetadata.setObjectName("DocumentationPackage");
        docMetadata.setVersion("3.0.0");
        docMetadata.setStatus(ObjectStatus.DEPLOYED);
        docMetadata.setUploadUser("docUser");
        docMetadata.setUploadTime(Instant.now());
        docMetadata.setSourceChannel("DOC_CHANNEL");
        docMetadata.setRole("wrong-role"); // Should be overridden to "documentation"
        docMetadata.getProperties().put("file.extension", ".ecore");

        // Store objects in their respective services
        Promise<String> draftStorePromise = draftService.storeObject("multi-draft-test", draftPackage, draftMetadata);
        String draftId = draftStorePromise.getValue();
        assertEquals("multi-draft-test", draftId);

        Promise<String> approvedStorePromise = approvedService.storeObject("multi-approved-test", approvedPackage, approvedMetadata);
        String approvedId = approvedStorePromise.getValue();
        assertEquals("multi-approved-test", approvedId);

        Promise<String> docStorePromise = docService.storeObject("multi-doc-test", docPackage, docMetadata);
        String docId = docStorePromise.getValue();
        assertEquals("multi-doc-test", docId);

        // Verify each service assigned its configured role, overriding the preset "wrong-role"
        assertEquals("draft", draftMetadata.getRole(), "Draft service should assign 'draft' role");
        assertEquals("approved", approvedMetadata.getRole(), "Approved service should assign 'approved' role");
        assertEquals("documentation", docMetadata.getRole(), "Documentation service should assign 'documentation' role");

        // Verify stored metadata has correct roles
        Promise<ObjectMetadata> draftRetrievePromise = draftService.retrieveMetadata("multi-draft-test");
        ObjectMetadata retrievedDraftMetadata = draftRetrievePromise.getValue();
        assertNotNull(retrievedDraftMetadata);
        assertEquals("draft", retrievedDraftMetadata.getRole());

        Promise<ObjectMetadata> approvedRetrievePromise = approvedService.retrieveMetadata("multi-approved-test");
        ObjectMetadata retrievedApprovedMetadata = approvedRetrievePromise.getValue();
        assertNotNull(retrievedApprovedMetadata);
        assertEquals("approved", retrievedApprovedMetadata.getRole());

        Promise<ObjectMetadata> docRetrievePromise = docService.retrieveMetadata("multi-doc-test");
        ObjectMetadata retrievedDocMetadata = docRetrievePromise.getValue();
        assertNotNull(retrievedDocMetadata);
        assertEquals("documentation", retrievedDocMetadata.getRole());

        // Verify storage isolation: each service should only see its own objects
        assertEquals(1, draftService.getObjectCount(), "Draft service should have 1 object");
        assertEquals(1, approvedService.getObjectCount(), "Approved service should have 1 object");
        assertEquals(1, docService.getObjectCount(), "Documentation service should have 1 object");

        // Verify that services can't retrieve each other's objects
        Promise<EObject> crossRetrievePromise1 = draftService.retrieveObject("multi-approved-test");
        assertNull(crossRetrievePromise1.getValue(), "Draft service should not find approved service's object");

        Promise<EObject> crossRetrievePromise2 = approvedService.retrieveObject("multi-doc-test");
        assertNull(crossRetrievePromise2.getValue(), "Approved service should not find documentation service's object");

        Promise<EObject> crossRetrievePromise3 = docService.retrieveObject("multi-draft-test");
        assertNull(crossRetrievePromise3.getValue(), "Documentation service should not find draft service's object");

        // Verify file system isolation (different workspace folders)
        File draftFile = new File(tempDir.toFile(), "draft-storage/multi-draft-test.ecore");
        File approvedFile = new File(tempDir.toFile(), "approved-storage/multi-approved-test.ecore");
        File docFile = new File(tempDir.toFile(), "documentation-storage/multi-doc-test.ecore");
        
        assertTrue(draftFile.exists(), "Draft file should exist in draft-storage folder");
        assertTrue(approvedFile.exists(), "Approved file should exist in approved-storage folder");
        assertTrue(docFile.exists(), "Documentation file should exist in documentation-storage folder");

        // Test cross-service metadata updates should fail
        ObjectMetadata crossUpdateMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        crossUpdateMetadata.setObjectName("CrossUpdate");
        
        Promise<Boolean> crossUpdatePromise = draftService.updateMetadata("multi-approved-test", crossUpdateMetadata);
        assertFalse(crossUpdatePromise.getValue(), "Draft service should not be able to update approved service's metadata");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    @FileTestAnnotations.MultiRoleFileStorageSetup
    public void testRoleBasedObjectListing(
            @InjectService(cardinality = 0, filter = "(&(storage.backend=file)(storage.role=draft))")
            ServiceAware<EObjectStorageService> draftServiceAware,
            @InjectService(cardinality = 0, filter = "(&(storage.backend=file)(storage.role=approved))")
            ServiceAware<EObjectStorageService> approvedServiceAware,
            @InjectService(cardinality = 0, filter = "(&(storage.backend=file)(storage.role=documentation))")
            ServiceAware<EObjectStorageService> docServiceAware
    ) throws Exception {
        
        // Wait for all storage services
        EObjectStorageService<EObject> draftService = (EObjectStorageService<EObject>) draftServiceAware.waitForService(5000L);
        EObjectStorageService<EObject> approvedService = (EObjectStorageService<EObject>) approvedServiceAware.waitForService(5000L);
        EObjectStorageService<EObject> docService = (EObjectStorageService<EObject>) docServiceAware.waitForService(5000L);

        // Store multiple objects in each service
        for (int i = 0; i < 3; i++) {
            // Draft objects
            EPackage draftPkg = EcoreFactory.eINSTANCE.createEPackage();
            draftPkg.setName("DraftPackage" + i);
            ObjectMetadata draftMeta = ManagementFactory.eINSTANCE.createObjectMetadata();
            draftMeta.setUploadUser("draftUser");
            draftMeta.getProperties().put("file.extension", ".ecore");
            draftService.storeObject("draft-obj-" + i, draftPkg, draftMeta).getValue();

            // Approved objects
            EPackage approvedPkg = EcoreFactory.eINSTANCE.createEPackage();
            approvedPkg.setName("ApprovedPackage" + i);
            ObjectMetadata approvedMeta = ManagementFactory.eINSTANCE.createObjectMetadata();
            approvedMeta.setUploadUser("approvedUser");
            approvedMeta.getProperties().put("file.extension", ".ecore");
            approvedService.storeObject("approved-obj-" + i, approvedPkg, approvedMeta).getValue();

            // Documentation objects
            EPackage docPkg = EcoreFactory.eINSTANCE.createEPackage();
            docPkg.setName("DocPackage" + i);
            ObjectMetadata docMeta = ManagementFactory.eINSTANCE.createObjectMetadata();
            docMeta.setUploadUser("docUser");
            docMeta.getProperties().put("file.extension", ".ecore");
            docService.storeObject("doc-obj-" + i, docPkg, docMeta).getValue();
        }

        // Verify each service lists only its own objects
        Promise<List<String>> draftListPromise = draftService.listObjectIds();
        List<String> draftIds = draftListPromise.getValue();
        assertEquals(3, draftIds.size(), "Draft service should list 3 objects");
        assertTrue(draftIds.contains("draft-obj-0"));
        assertTrue(draftIds.contains("draft-obj-1"));
        assertTrue(draftIds.contains("draft-obj-2"));
        assertFalse(draftIds.contains("approved-obj-0"), "Draft service should not list approved objects");
        assertFalse(draftIds.contains("doc-obj-0"), "Draft service should not list documentation objects");

        Promise<List<String>> approvedListPromise = approvedService.listObjectIds();
        List<String> approvedIds = approvedListPromise.getValue();
        assertEquals(3, approvedIds.size(), "Approved service should list 3 objects");
        assertTrue(approvedIds.contains("approved-obj-0"));
        assertTrue(approvedIds.contains("approved-obj-1"));
        assertTrue(approvedIds.contains("approved-obj-2"));
        assertFalse(approvedIds.contains("draft-obj-0"), "Approved service should not list draft objects");

        Promise<List<String>> docListPromise = docService.listObjectIds();
        List<String> docIds = docListPromise.getValue();
        assertEquals(3, docIds.size(), "Documentation service should list 3 objects");
        assertTrue(docIds.contains("doc-obj-0"));
        assertTrue(docIds.contains("doc-obj-1"));
        assertTrue(docIds.contains("doc-obj-2"));
        assertFalse(docIds.contains("draft-obj-0"), "Documentation service should not list draft objects");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    @FileTestAnnotations.MultiRoleFileStorageSetup
    public void testCrossServiceDeletion(
            @InjectService(cardinality = 0, filter = "(&(storage.backend=file)(storage.role=draft))")
            ServiceAware<EObjectStorageService> draftServiceAware,
            @InjectService(cardinality = 0, filter = "(&(storage.backend=file)(storage.role=approved))")
            ServiceAware<EObjectStorageService> approvedServiceAware
    ) throws Exception {
        
        // Wait for storage services
        EObjectStorageService<EObject> draftService = (EObjectStorageService<EObject>) draftServiceAware.waitForService(5000L);
        EObjectStorageService<EObject> approvedService = (EObjectStorageService<EObject>) approvedServiceAware.waitForService(5000L);

        // Store objects in both services
        EPackage draftPkg = EcoreFactory.eINSTANCE.createEPackage();
        draftPkg.setName("DraftPackage");
        ObjectMetadata draftMeta = ManagementFactory.eINSTANCE.createObjectMetadata();
        draftMeta.setUploadUser("draftUser");
        draftMeta.getProperties().put("file.extension", ".ecore");
        draftService.storeObject("cross-delete-test", draftPkg, draftMeta).getValue();

        EPackage approvedPkg = EcoreFactory.eINSTANCE.createEPackage();
        approvedPkg.setName("ApprovedPackage");
        ObjectMetadata approvedMeta = ManagementFactory.eINSTANCE.createObjectMetadata();
        approvedMeta.setUploadUser("approvedUser");
        approvedMeta.getProperties().put("file.extension", ".ecore");
        approvedService.storeObject("cross-delete-test", approvedPkg, approvedMeta).getValue();

        // Verify both objects exist (same ID but different services)
        assertTrue(draftService.exists("cross-delete-test"), "Draft service should have the object");
        assertTrue(approvedService.exists("cross-delete-test"), "Approved service should have the object");

        // Delete from draft service should not affect approved service
        Promise<Boolean> deletePromise = draftService.deleteObject("cross-delete-test");
        assertTrue(deletePromise.getValue(), "Draft service deletion should succeed");

        // Verify isolation after deletion
        assertFalse(draftService.exists("cross-delete-test"), "Draft service should no longer have the object");
        assertTrue(approvedService.exists("cross-delete-test"), "Approved service should still have the object");

        // Verify approved service object is still retrievable
        Promise<EObject> retrievePromise = approvedService.retrieveObject("cross-delete-test");
        EObject retrievedObj = retrievePromise.getValue();
        assertNotNull(retrievedObj, "Approved service object should still be retrievable");
        assertTrue(retrievedObj instanceof EPackage);
        assertEquals("ApprovedPackage", ((EPackage) retrievedObj).getName());
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Test
    @FileTestAnnotations.MultiRoleFileStorageSetup
    public void testSharedRegistryWithIsolatedStorage(
            @InjectService(cardinality = 0, filter = "(&(storage.backend=file)(storage.role=draft))")
            ServiceAware<EObjectStorageService> draftServiceAware,
            @InjectService(cardinality = 0, filter = "(&(storage.backend=file)(storage.role=approved))")
            ServiceAware<EObjectStorageService> approvedServiceAware,
            @InjectService(cardinality = 0, filter = "(&(storage.backend=file)(storage.role=documentation))")
            ServiceAware<EObjectStorageService> docServiceAware,
            @InjectService(cardinality = 0, filter = "(registry.type=shared)")
            ServiceAware<EObjectRegistryService> registryServiceAware
    ) throws Exception {
        
        // Wait for storage services and registry
        EObjectStorageService<EObject> draftService = (EObjectStorageService<EObject>) draftServiceAware.waitForService(5000L);
        EObjectStorageService<EObject> approvedService = (EObjectStorageService<EObject>) approvedServiceAware.waitForService(5000L);
        EObjectStorageService<EObject> docService = (EObjectStorageService<EObject>) docServiceAware.waitForService(5000L);
        EObjectRegistryService<EObject> registryService = (EObjectRegistryService<EObject>) registryServiceAware.waitForService(5000L);

        // === TEST: Verify we have 3 different storage service instances + 1 shared registry ===
        assertNotNull(draftService, "Draft service should be available");
        assertNotNull(approvedService, "Approved service should be available");
        assertNotNull(docService, "Documentation service should be available");
        assertNotNull(registryService, "Registry service should be available");
        
        // Verify storage services are different instances
        assertNotEquals(draftService, approvedService, "Draft and approved services should be different instances");
        assertNotEquals(draftService, docService, "Draft and documentation services should be different instances");
        assertNotEquals(approvedService, docService, "Approved and documentation services should be different instances");
        
        // Verify they have same backend type but different roles
        assertEquals(StorageBackendType.FILE, draftService.getBackendType());
        assertEquals(StorageBackendType.FILE, approvedService.getBackendType());
        assertEquals(StorageBackendType.FILE, docService.getBackendType());

        // Store objects in different services
        EPackage draftPackage = EcoreFactory.eINSTANCE.createEPackage();
        draftPackage.setName("IsolatedStorageDraftPackage");
        ObjectMetadata draftMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        draftMetadata.setObjectName("IsolatedStorageDraftPackage");
        draftMetadata.setVersion("1.0.0");
        draftMetadata.setUploadUser("draftUser");
        draftMetadata.setUploadTime(Instant.now());
        draftMetadata.setSourceChannel("DRAFT_CHANNEL");
        draftMetadata.getProperties().put("file.extension", ".ecore");
        draftService.storeObject("isolated-storage-draft", draftPackage, draftMetadata).getValue();

        EPackage approvedPackage = EcoreFactory.eINSTANCE.createEPackage();
        approvedPackage.setName("IsolatedStorageApprovedPackage");
        ObjectMetadata approvedMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        approvedMetadata.setObjectName("IsolatedStorageApprovedPackage");
        approvedMetadata.setVersion("2.0.0");
        approvedMetadata.setUploadUser("approvedUser");
        approvedMetadata.setUploadTime(Instant.now());
        approvedMetadata.setSourceChannel("APPROVED_CHANNEL");
        approvedMetadata.getProperties().put("file.extension", ".ecore");
        approvedService.storeObject("isolated-storage-approved", approvedPackage, approvedMetadata).getValue();

        // === TEST: Isolated Storage - Each service only sees its own metadata ===
        
        // Draft service can access its own metadata
        Promise<ObjectMetadata> draftFromDraft = draftService.retrieveMetadata("isolated-storage-draft");
        ObjectMetadata draftMeta1 = draftFromDraft.getValue();
        assertNotNull(draftMeta1, "Draft service should retrieve its own metadata");
        assertEquals("IsolatedStorageDraftPackage", draftMeta1.getObjectName());
        assertEquals("draft", draftMeta1.getRole());

        // Approved service CANNOT access draft metadata (isolated storage!)
        Promise<ObjectMetadata> draftFromApproved = approvedService.retrieveMetadata("isolated-storage-draft");
        ObjectMetadata draftMeta2 = draftFromApproved.getValue();
        assertNull(draftMeta2, "Approved service should NOT retrieve draft metadata - isolated storage");

        // Documentation service CANNOT access approved metadata (isolated storage!)
        Promise<ObjectMetadata> approvedFromDoc = docService.retrieveMetadata("isolated-storage-approved");
        ObjectMetadata approvedMeta = approvedFromDoc.getValue();
        assertNull(approvedMeta, "Documentation service should NOT retrieve approved metadata - isolated storage");

        // But approved service CAN access its own metadata
        Promise<ObjectMetadata> approvedFromApproved = approvedService.retrieveMetadata("isolated-storage-approved");
        ObjectMetadata approvedMeta2 = approvedFromApproved.getValue();
        assertNotNull(approvedMeta2, "Approved service should retrieve its own metadata");
        assertEquals("IsolatedStorageApprovedPackage", approvedMeta2.getObjectName());
        assertEquals("approved", approvedMeta2.getRole());

        // === TEST: Shared Registry - Registry sees ALL metadata from all storage services ===
        
        // Registry can see draft metadata (populated when draft service stored object)
        Optional<ObjectMetadata> draftFromRegistry = registryService.getMetadata("isolated-storage-draft");
        assertTrue(draftFromRegistry.isPresent(), "Registry should see draft metadata via shared registry");
        assertEquals("IsolatedStorageDraftPackage", draftFromRegistry.get().getObjectName());
        assertEquals("draft", draftFromRegistry.get().getRole());

        // Registry can see approved metadata (populated when approved service stored object)
        Optional<ObjectMetadata> approvedFromRegistry = registryService.getMetadata("isolated-storage-approved");
        assertTrue(approvedFromRegistry.isPresent(), "Registry should see approved metadata via shared registry");
        assertEquals("IsolatedStorageApprovedPackage", approvedFromRegistry.get().getObjectName());
        assertEquals("approved", approvedFromRegistry.get().getRole());

        // === TEST: Isolated Storage - Only owning service can access objects ===
        
        // Draft service can access its own object
        Promise<EObject> draftObjFromDraft = draftService.retrieveObject("isolated-storage-draft");
        EObject draftObj1 = draftObjFromDraft.getValue();
        assertNotNull(draftObj1, "Draft service should retrieve its own object");
        assertTrue(draftObj1 instanceof EPackage);
        assertEquals("IsolatedStorageDraftPackage", ((EPackage) draftObj1).getName());

        // Approved service CANNOT access draft object (different workspace)
        Promise<EObject> draftObjFromApproved = approvedService.retrieveObject("isolated-storage-draft");
        EObject draftObj2 = draftObjFromApproved.getValue();
        assertNull(draftObj2, "Approved service should NOT retrieve draft object - isolated storage");

        // Documentation service CANNOT access approved object (different workspace)
        Promise<EObject> approvedObjFromDoc = docService.retrieveObject("isolated-storage-approved");
        EObject approvedObj = approvedObjFromDoc.getValue();
        assertNull(approvedObj, "Documentation service should NOT retrieve approved object - isolated storage");

        // But approved service CAN access its own object
        Promise<EObject> approvedObjFromApproved = approvedService.retrieveObject("isolated-storage-approved");
        EObject approvedObj2 = approvedObjFromApproved.getValue();
        assertNotNull(approvedObj2, "Approved service should retrieve its own object");
        assertTrue(approvedObj2 instanceof EPackage);
        assertEquals("IsolatedStorageApprovedPackage", ((EPackage) approvedObj2).getName());

        // === TEST: exists() method behavior ===
        
        // File-based existence (should only work for owning service)
        assertTrue(draftService.exists("isolated-storage-draft"), "Draft service should find its own object");
        assertTrue(approvedService.exists("isolated-storage-approved"), "Approved service should find its own object");
        
        // Cross-service existence should return false (isolated workspaces)
        assertFalse(approvedService.exists("isolated-storage-draft"), "Approved service should not find draft object");
        assertFalse(draftService.exists("isolated-storage-approved"), "Draft service should not find approved object");
        assertFalse(docService.exists("isolated-storage-draft"), "Documentation service should not find draft object");
        assertFalse(docService.exists("isolated-storage-approved"), "Documentation service should not find approved object");
    }
}