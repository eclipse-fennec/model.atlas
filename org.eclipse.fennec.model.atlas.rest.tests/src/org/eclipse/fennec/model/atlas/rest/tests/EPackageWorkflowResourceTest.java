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
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.rest.tests;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;
import org.eclipse.fennec.model.atlas.rest.tests.helper.ResourceAware;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestHelper;
import org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService;
import org.gecko.emf.osgi.annotation.require.RequireEMF;
import org.gecko.emf.rest.annotations.RequireEMFMessageBodyReaderWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.service.jakartars.whiteboard.annotations.RequireJakartarsWhiteboard;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.Promises;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;

/**
 * Integration tests for EPackageWorkflowResource REST endpoints.
 *
 * <p>Tests cover:</p>
 * <ul>
 * <li>Draft management operations (upload, list, get, update, delete)</li>
 * <li>Workflow state management (approve, reject, release)</li>
 * <li>Object listing by status (approved, rejected, released)</li>
 * <li>Content retrieval and metadata operations</li>
 * <li>Error handling and validation scenarios</li>
 * <li>XMI serialization of EMF objects via REST</li>
 * </ul>
 *
 * <p>Uses manual XMI serialization/deserialization pattern for reliable EMF object handling
 * and text/plain responses for non-XMI content.</p>
 * 
 * @author Ilenia Salvadori
 * @since 1.0.0
 */
@RequireEMF
@RequireEMFMessageBodyReaderWriter
@RequireJakartarsWhiteboard
@RequireConfigurationAdmin
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class EPackageWorkflowResourceTest {

    private static final String BASE_URL = "http://localhost:8185/rest/packages/workflow";
    private static final String TEST_PACKAGE_NSURI = "http://test.example.com/testpackage/1.0";
    private static final String TEST_UPLOAD_USER = "test.developer@company.com";
    private static final String TEST_REVIEW_USER = "test.steward@company.com";
    private static final String TEST_SOURCE_CHANNEL = "MANUAL_UPLOAD";
//    private static final String TEST_OBJECT_TYPE = "EPackage";

    @InjectService(filter = "(emf.name=workflowapi)")
    ResourceSet resourceSet;

    @InjectService
    ClientBuilder clientBuilder;

    private Client restClient;
    private EObjectWorkflowService<EObject> mockWorkflowService;
    @SuppressWarnings("rawtypes")
	private ServiceRegistration<EObjectWorkflowService> mockServiceRegistration;

    @BeforeEach
    public void setup(@InjectBundleContext BundleContext context) throws Exception {
        // Setup REST client
        restClient = clientBuilder.build();

        // Create and register mock EObjectWorkflowService
        mockWorkflowService = new MockEObjectWorkflowService();
        
        Dictionary<String, Object> serviceProps = new Hashtable<>();
        serviceProps.put("service.ranking", Integer.MAX_VALUE);
        
        mockServiceRegistration = context.registerService(
                EObjectWorkflowService.class,
                mockWorkflowService,
                serviceProps);
        
        // Small delay to allow service registration to propagate
        Thread.sleep(200);
        
        // Ensure XMI factory is registered
     	TestHelper.ensureXMIFactory(resourceSet);
        
        // Wait for the WorkflowResource to be registered in Jakarta REST runtime
        ResourceAware resourceAware = ResourceAware.create(context, "EPackageWorkflowResource");
        boolean resourceReady = resourceAware.waitForResource(15, TimeUnit.SECONDS);
        
        assertTrue(resourceReady, 
                "EPackageWorkflowResource should be registered within 15 seconds. " +
                "Check that the resource is properly configured and the Jakarta REST runtime is working.");
    }

    @AfterEach
    public void teardown(@InjectBundleContext BundleContext context) throws Exception {
    	if (nonNull(mockServiceRegistration)) {
    		mockServiceRegistration.unregister();
    		mockServiceRegistration = null;
    		
    		// Small delay to allow service unregistration to propagate
    		Thread.sleep(200);
    	}
    	
    	if (nonNull(restClient)) {
    		restClient.close();
    		restClient = null;
    	}
    }

    // ========== Draft Management Tests ==========

    @Test
    public void testUploadDraft_Success() throws Exception {
        // Create a test EObject (simple EPackage)
        EPackage testEObject = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, "TestPackage", "test");
        String xmiContent = serializeToXMI(testEObject);

        Response response = restClient
                .target(BASE_URL)
                .path("drafts")
                .queryParam("uploadUser", TEST_UPLOAD_USER)
                .queryParam("sourceChannel", TEST_SOURCE_CHANNEL)
                .queryParam("contentHash", "test-hash-123")
                .queryParam("fileExtension", "ecore")
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        // Debug: Print the actual response status and content
        System.out.println("DEBUG testUploadDraft_Success - Response status: " + response.getStatus());
        String responseContent = response.readEntity(String.class);
        System.out.println("DEBUG testUploadDraft_Success - Response content: " + responseContent);

        assertEquals(201, response.getStatus(), "Should return HTTP 201 Created");
        assertNotNull(responseContent, "Should return content");
        assertEquals(TEST_PACKAGE_NSURI, responseContent.trim(), "Response should be the package nsURI");
    }

    @Test
    public void testUploadDraft_MissingUploadUser() throws Exception {
        EPackage testEObject = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, "TestPackage", "test");
        String xmiContent = TestHelper.serializeToXMI(testEObject, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path("drafts")
                .queryParam("sourceChannel", TEST_SOURCE_CHANNEL)
                .request("text/plain")
                .post(Entity.entity(xmiContent, "application/xmi"));

        // Debug: Print the actual response status and content
        System.out.println("DEBUG - Response status: " + response.getStatus());
        String errorContent = response.readEntity(String.class);
        System.out.println("DEBUG - Error content: " + errorContent);

        assertEquals(400, response.getStatus(), "Should return HTTP 400 Bad Request");
        assertTrue(errorContent.contains("uploadUser"), "Error should mention missing uploadUser");
    }

    @Test
    public void testListDraftObjects_Success() {
        Response response = restClient
                .target(BASE_URL)
                .path("drafts")
                .request("application/xmi")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("ObjectMetadataContainer"), "Response should contain ObjectMetadataContainer");
        assertTrue(responseContent.contains("metadata"), "Response should contain metadata elements");

        // Verify we can handle multiple draft objects (mock returns at least one)
        assertTrue(responseContent.contains(TEST_PACKAGE_NSURI), "Response should contain the test package nsURI");
    }

    @Test
    public void testGetDraft_Success() {
        Response response = restClient
                .target(BASE_URL)
                .path("drafts")
                .path("nsuri")
                .queryParam("packageNsURI", TEST_PACKAGE_NSURI)
                .request("application/xmi")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        // Response should contain ObjectMetadata as XMI
        String xmiContent = response.readEntity(String.class);
        assertNotNull(xmiContent, "Should return XMI content");
        assertTrue(xmiContent.contains("ObjectMetadata"), "Should contain ObjectMetadata element");
        // Note: DRAFT is default enum value (0), so EMF doesn't serialize it - absence indicates DRAFT status
        assertTrue(xmiContent.contains("objectId=\"" + TEST_PACKAGE_NSURI + "\""), "Should contain the test package nsURI");
    }

    @Test
    public void testGetDraft_NotFound() {
        String nonExistentNsURI = "http://non-existent-draft.com/test/1.0";

        Response response = restClient
                .target(BASE_URL)
                .path("drafts")
                .path("nsuri")
                .queryParam("packageNsURI", nonExistentNsURI)
                .request("text/plain")
                .get();

        assertEquals(404, response.getStatus(), "Should return HTTP 404 Not Found");

        String errorContent = response.readEntity(String.class);
        assertTrue(errorContent.contains(nonExistentNsURI), "Error should mention the package nsURI");
    }
    
    @Test
    public void testGetDraftContent_Success() {
        Response response = restClient
                .target(BASE_URL)
                .path("drafts")
                .path("nsuri")
                .path("content")
                .queryParam("packageNsURI", TEST_PACKAGE_NSURI)
                .request("application/xmi")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        // Response should contain EPackage as XMI
        String xmiContent = response.readEntity(String.class);
        assertNotNull(xmiContent, "Should return XMI content");
        assertTrue(xmiContent.contains("EPackage"), "Should contain EPackage element");
    }
    
    @Test
    public void testGetDraftContent_NotFound() {
        String nonExistentNsURI = "http://non-existent-draft.com/test/1.0";

        Response response = restClient
                .target(BASE_URL)
                .path("drafts")
                .path("nsuri")
                .path("content")
                .queryParam("packageNsURI", nonExistentNsURI)
                .request("text/plain")
                .get();

        assertEquals(404, response.getStatus(), "Should return HTTP 404 Not Found");

        String errorContent = response.readEntity(String.class);
        assertTrue(errorContent.contains(nonExistentNsURI), "Error should mention the package nsURI");
    }
    
    @Test
    public void testUpdateDraft_Success() throws Exception {
        // Create a test EObject (simple EPackage)
        EPackage testEObject = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, "TestPackage", "test");
        String xmiContent = serializeToXMI(testEObject);

        Response response = restClient
                .target(BASE_URL)
                .path("drafts")
                .path("nsuri")
                .queryParam("packageNsURI", TEST_PACKAGE_NSURI)
                .request("text/plain")
                .put(Entity.entity(xmiContent, "application/xmi"));

        // Debug: Print the actual response status and content
        System.out.println("DEBUG testUpdateDraft_Success - Response status: " + response.getStatus());
        String responseContent = response.readEntity(String.class);
        System.out.println("DEBUG testUpdateDraft_Success - Response content: " + responseContent);

        assertEquals(200, response.getStatus(), "Should return HTTP 200");
    }
    
    @Test
    public void testUpdateDraft_NotFound() throws Exception {
        // Create a test EObject (simple EPackage)
        EPackage testEObject = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, "TestPackage", "test");
        String xmiContent = serializeToXMI(testEObject);

        Response response = restClient
                .target(BASE_URL)
                .path("drafts")
                .path("nsuri")
                .queryParam("packageNsURI", "http://non-existent-draft.com/test/1.0")
                .request("text/plain")
                .put(Entity.entity(xmiContent, "application/xmi"));

        // Debug: Print the actual response status and content
        System.out.println("DEBUG testUpdateDraft_Success - Response status: " + response.getStatus());
        String responseContent = response.readEntity(String.class);
        System.out.println("DEBUG testUpdateDraft_Success - Response content: " + responseContent);

        assertEquals(404, response.getStatus(), "Should return HTTP 404");
    }

    @Test
    public void testDeleteDraft_Success() {
        Response response = restClient
                .target(BASE_URL)
                .path("drafts")
                .path("nsuri")
                .queryParam("packageNsURI", TEST_PACKAGE_NSURI)
                .request()
                .delete();

        assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content");
    }

    

    // ========== Workflow State Management Tests ==========

    @Test
    public void testApproveObject_Success() {
        Response response = restClient
                .target(BASE_URL)
                .path("state")
                .path("nsuri")
                .path("approve")
                .queryParam("packageNsURI", TEST_PACKAGE_NSURI)
                .queryParam("reviewUser", TEST_REVIEW_USER)
                .queryParam("approvalReason", "All compliance checks passed")
                .request("application/xmi")
                .post(Entity.entity("", "application/xmi"));

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        // Response should contain updated ObjectMetadata as XMI
        String xmiContent = response.readEntity(String.class);
        assertNotNull(xmiContent, "Should return XMI content");
        assertTrue(xmiContent.contains("ObjectMetadata"), "Should contain ObjectMetadata element");
        assertTrue(xmiContent.contains("APPROVED"), "Should contain approved status");
    }

    @Test
    public void testRejectObject_Success() {
        Response response = restClient
                .target(BASE_URL)
                .path("state")
                .path("nsuri")
                .path("reject")
                .queryParam("packageNsURI", TEST_PACKAGE_NSURI)
                .queryParam("reviewUser", TEST_REVIEW_USER)
                .queryParam("rejectionReason", "Compliance issues found")
                .request("application/xmi")
                .post(Entity.entity("", "application/xmi"));

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        // Response should contain updated ObjectMetadata as XMI
        String xmiContent = response.readEntity(String.class);
        assertNotNull(xmiContent, "Should return XMI content");
        assertTrue(xmiContent.contains("ObjectMetadata"), "Should contain ObjectMetadata element");
        assertTrue(xmiContent.contains("REJECTED"), "Should contain rejected status");
    }
    
    @Test
    public void testReleaseObject_Success() {
        Response response = restClient
                .target(BASE_URL)
                .path("state")
                .path("nsuri")
                .path("release")
                .queryParam("packageNsURI", TEST_PACKAGE_NSURI)
                .queryParam("releaseNotes", TEST_REVIEW_USER)
                .request("application/xmi")
                .post(Entity.entity("", "application/xmi"));

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        // Response should contain updated ObjectMetadata as XMI
        String xmiContent = response.readEntity(String.class);
        assertNotNull(xmiContent, "Should return XMI content");
        assertTrue(xmiContent.contains("ObjectMetadata"), "Should contain ObjectMetadata element");
        assertTrue(xmiContent.contains("DEPLOYED"), "Should contain released status");
    }

    // ========== Object Listing Tests ==========

    @Test
    public void testListApprovedObjects_Success() {
        Response response = restClient
                .target(BASE_URL)
                .path("approved")
                .request("application/xmi")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("ObjectMetadataContainer"), "Response should contain ObjectMetadataContainer");
        assertTrue(responseContent.contains("metadata"), "Response should contain metadata elements");

        // Verify we can handle multiple approved objects (mock returns at least one)
        assertTrue(responseContent.contains(TEST_PACKAGE_NSURI), "Response should contain the test package nsURI");
        assertTrue(responseContent.contains("APPROVED"), "Response should contain approved status");
    }

    @Test
    public void testListAllObjects_Success() {
        Response response = restClient
                .target(BASE_URL)
                .path("all")
                .request("application/xmi")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("ObjectMetadataContainer"), "Response should contain ObjectMetadataContainer");
        assertTrue(responseContent.contains("metadata"), "Response should contain metadata elements");

        // Since this endpoint aggregates from all workflow states, it should contain objects from all states
        // The mock service returns one object for each list method, so we should have multiple objects
        assertTrue(responseContent.contains(TEST_PACKAGE_NSURI), "Response should contain the test package nsURI");

        // Verify we get objects from different workflow states
        assertTrue(responseContent.contains("DRAFT") || responseContent.contains("APPROVED") ||
                  responseContent.contains("REJECTED") || responseContent.contains("DEPLOYED"),
                  "Response should contain objects from various workflow states");

        // The response should contain multiple metadata entries since we aggregate from all states
        int metadataCount = (responseContent.split("<metadata").length - 1);
        assertTrue(metadataCount >= 4, "Should contain at least 4 metadata entries (one from each workflow state)");
    }
    
    @Test
    public void testListRejectedObjects_Success() {
        Response response = restClient
                .target(BASE_URL)
                .path("rejected")
                .request("application/xmi")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("ObjectMetadataContainer"), "Response should contain ObjectMetadataContainer");
        assertTrue(responseContent.contains("metadata"), "Response should contain metadata elements");

        // Verify we can handle multiple rejected objects (mock returns at least one)
        assertTrue(responseContent.contains(TEST_PACKAGE_NSURI), "Response should contain the test package nsURI");
        assertTrue(responseContent.contains("REJECTED"), "Response should contain rejected status");
    }
    
    @Test
    public void testListReleasedObjects_Success() {
        Response response = restClient
                .target(BASE_URL)
                .path("released")
                .request("application/xmi")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("ObjectMetadataContainer"), "Response should contain ObjectMetadataContainer");
        assertTrue(responseContent.contains("metadata"), "Response should contain metadata elements");

        // Verify we can handle multiple released objects (mock returns at least one)
        assertTrue(responseContent.contains(TEST_PACKAGE_NSURI), "Response should contain the test package nsURI");
        assertTrue(responseContent.contains("DEPLOYED"), "Response should contain released status");
    }
    
    @Test
    public void testGetObject_Success() {
        Response response = restClient
                .target(BASE_URL)
                .queryParam("packageNsURI", TEST_PACKAGE_NSURI)
                .request("application/xmi")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("ObjectMetadata"), "Response should contain ObjectMetadata");
    }
    
    @Test
    public void testGetObjectContent_Success() {
        Response response = restClient
                .target(BASE_URL)
                .path("content")
                .queryParam("packageNsURI", TEST_PACKAGE_NSURI)
                .request("application/xmi")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("EPackage"), "Response should contain EPackage");
    }
    
    @Test
    public void testUpdateObject_Success() throws IOException {

        // Create a test EObject (simple EPackage)
        EPackage testEObject = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, "TestPackage", "test");
        String xmiContent = TestHelper.serializeToXMI(testEObject, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .queryParam("packageNsURI", TEST_PACKAGE_NSURI)
                .request("text/plain")
                .put(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
    }
    
    @Test
    public void testDeleteObject_Success() {

        Response response = restClient
                .target(BASE_URL)
                .queryParam("packageNsURI", TEST_PACKAGE_NSURI)
                .request("text/plain")
                .delete();

        assertEquals(204, response.getStatus(), "Should return HTTP 204 OK");
    }

    // ========== Helper Methods ==========

    /**
     * Serialize an EMF EObject to XMI string using the injected ResourceSet.
     */
    private String serializeToXMI(EObject eObject) throws IOException {
        Resource resource = resourceSet.createResource(URI.createURI("temp.xmi"));
        resource.getContents().add(eObject);
        
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        resource.save(outputStream, null);
        
        return outputStream.toString("UTF-8");
    }
    

    // ========== Mock Service Implementation ==========

    /**
     * Mock implementation of EObjectWorkflowService for testing.
     */
    public static class MockEObjectWorkflowService implements EObjectWorkflowService<EObject> {

        @Override
        public Promise<String> uploadDraft(EObject eObject, ObjectMetadata metadata) {
            return Promises.resolved(TEST_PACKAGE_NSURI);
        }

        @Override
        public List<ObjectMetadata> listDraftObjects() {
            ObjectMetadata metadata1 = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata1.setObjectId(TEST_PACKAGE_NSURI);
            metadata1.setUploadUser(TEST_UPLOAD_USER);
            metadata1.setStatus(ObjectStatus.DRAFT);
            metadata1.setUploadTime(Instant.now());

            ObjectMetadata metadata2 = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata2.setObjectId(TEST_PACKAGE_NSURI + "-draft-2");
            metadata2.setUploadUser("second.developer@company.com");
            metadata2.setStatus(ObjectStatus.DRAFT);
            metadata2.setUploadTime(Instant.now());

            return List.of(metadata1, metadata2);
        }

        @Override
        public ObjectMetadata getDraft(String packageNsURI) {
            if ("http://non-existent-draft.com/test/1.0".equals(packageNsURI)) {
                return null;
            }

            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setObjectId(packageNsURI);
            metadata.setUploadUser(TEST_UPLOAD_USER);
            metadata.setStatus(ObjectStatus.DRAFT);
            metadata.setUploadTime(Instant.now());

            return metadata;
        }

        @Override
        public EObject getDraftContent(String packageNsURI) {
            if ("http://non-existent-draft.com/test/1.0".equals(packageNsURI)) {
                return null;
            }

            return EcoreFactory.eINSTANCE.createEPackage();
        }

        @Override
        public Promise<Void> updateDraft(String packageNsURI, EObject updatedObject) {
            if ("http://non-existent-draft.com/test/1.0".equals(packageNsURI)) {
                throw new IllegalArgumentException();
            }
            return Promises.resolved(null);
        }

        @Override
        public Promise<Boolean> deleteDraft(String packageNsURI) {
            return Promises.resolved(!"http://non-existent-draft.com/test/1.0".equals(packageNsURI));
        }

       

        @Override
        public ObjectMetadata approveObject(String packageNsURI, String reviewUser, String approvalReason) {
            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setObjectId(packageNsURI);
            metadata.setReviewUser(reviewUser);
            metadata.setStatus(ObjectStatus.APPROVED);
            metadata.setReviewTime(Instant.now());

            return metadata;
        }

        @Override
        public ObjectMetadata rejectObject(String packageNsURI, String reviewUser, String rejectionReason) {
            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setObjectId(packageNsURI);
            metadata.setReviewUser(reviewUser);
            metadata.setStatus(ObjectStatus.REJECTED);
            metadata.setReviewTime(Instant.now());

            return metadata;
        }

        @Override
        public ObjectMetadata releaseObject(String packageNsURI, String releaseNotes, boolean requireComplianceCheck) {
            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setObjectId(packageNsURI);
            metadata.setStatus(ObjectStatus.DEPLOYED);
            metadata.setReviewTime(Instant.now());

            return metadata;
        }

        @Override
        public List<ObjectMetadata> listApprovedObjects() {
            ObjectMetadata metadata1 = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata1.setObjectId(TEST_PACKAGE_NSURI + "-approved-1");
            metadata1.setStatus(ObjectStatus.APPROVED);
            metadata1.setReviewTime(Instant.now());

            ObjectMetadata metadata2 = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata2.setObjectId(TEST_PACKAGE_NSURI + "-approved-2");
            metadata2.setStatus(ObjectStatus.APPROVED);
            metadata2.setReviewTime(Instant.now());

            return List.of(metadata1, metadata2);
        }

        @Override
        public List<ObjectMetadata> listRejectedObjects() {
            ObjectMetadata metadata1 = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata1.setObjectId(TEST_PACKAGE_NSURI + "-rejected-1");
            metadata1.setStatus(ObjectStatus.REJECTED);
            metadata1.setReviewTime(Instant.now());

            ObjectMetadata metadata2 = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata2.setObjectId(TEST_PACKAGE_NSURI + "-rejected-2");
            metadata2.setStatus(ObjectStatus.REJECTED);
            metadata2.setReviewTime(Instant.now());

            return List.of(metadata1, metadata2);
        }

        @Override
        public List<ObjectMetadata> listReleasedObjects() {
            ObjectMetadata metadata1 = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata1.setObjectId(TEST_PACKAGE_NSURI + "-released-1");
            metadata1.setStatus(ObjectStatus.DEPLOYED);
            metadata1.setReviewTime(Instant.now());

            ObjectMetadata metadata2 = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata2.setObjectId(TEST_PACKAGE_NSURI + "-released-2");
            metadata2.setStatus(ObjectStatus.DEPLOYED);
            metadata2.setReviewTime(Instant.now());

            return List.of(metadata1, metadata2);
        }

        @Override
        public ObjectMetadata getObject(String packageNsURI) {
            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setObjectId(packageNsURI);
            metadata.setStatus(ObjectStatus.DRAFT);
            metadata.setUploadTime(Instant.now());

            return metadata;
        }

        @Override
        public Object getObjectContent(String packageNsURI) {
            return EcoreFactory.eINSTANCE.createEPackage();
        }

        @Override
        public Promise<Void> updateObject(String packageNsURI, EObject updatedObject) {
            return Promises.resolved(null);
        }

        @Override
        public Promise<Boolean> deleteObject(String packageNsURI) {
            return Promises.resolved(true);
        }

       

    }
}