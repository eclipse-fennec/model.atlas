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

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.model.atlas.rest.model.RestFactory;
import org.eclipse.fennec.model.atlas.rest.model.StageTransitionRequest;
import org.eclipse.fennec.model.atlas.rest.tests.helper.MockTestHelper.MockRegistryServiceCollector;
import org.eclipse.fennec.model.atlas.rest.tests.helper.MockTestHelper.MockScopeServiceCollector;
import org.eclipse.fennec.model.atlas.rest.tests.helper.ResourceAware;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestHelper;
import org.eclipse.fennec.model.atlas.workflow.RegistryServiceCollector;
import org.eclipse.fennec.model.atlas.workflow.ScopeServiceCollector;
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

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;

/**
 * Integration tests for ObjectRegistryResource REST endpoints.
 *
 * <p>
 * Tests cover:
 * </p>
 * <ul>
 * <li>Listing objects in final stage and specific stages</li>
 * <li>Creating objects in stages (when implemented)</li>
 * <li>Retrieving object metadata and content</li>
 * <li>Updating object content</li>
 * <li>Deleting objects</li>
 * <li>Transitioning objects between stages</li>
 * <li>Scope-based workflow operations</li>
 * <li>Error handling and validation scenarios</li>
 * </ul>
 *
 * @author Data In Motion
 * @since 1.0.0
 */
@RequireEMF
@RequireEMFMessageBodyReaderWriter
@RequireJakartarsWhiteboard
@RequireConfigurationAdmin
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class ObjectRegistryResourceTest {

    private static final String BASE_URL = "http://localhost:8185/rest";
    private static final String TEST_SCOPE_NAME = "test-scope";
    private static final String TEST_REGISTRY_NAME = "test-registry";
    private static final String TEST_OBJECT_ID = "test-object-123";
    private static final String TEST_OBJECT_NAME = "TestObject";
    private static final String TEST_STAGE_DRAFT = "draft";
    private static final String TEST_STAGE_APPROVED = "approved";
    private static final String TEST_STAGE_RELEASE = "release";

    @InjectService(filter = "(emf.name=workflowapi)")
    ResourceSet resourceSet;

    @InjectService
    ClientBuilder clientBuilder;

    private Client restClient;
    private MockScopeServiceCollector mockScopeCollector;
    private ServiceRegistration<ScopeServiceCollector> mockScopeCollectorRegistration;
    private MockRegistryServiceCollector mockRegistryCollector;
    private ServiceRegistration<RegistryServiceCollector> mockRegistryCollectorRegistration;

    @BeforeEach
    public void setup(@InjectBundleContext BundleContext context) throws Exception {
        // Setup REST client
        restClient = clientBuilder.build();

        // Create and register mock ScopeCollector
        Dictionary<String, Object> scopeProps = new Hashtable<>();
        scopeProps.put("service.ranking", Integer.MAX_VALUE);

        mockScopeCollector = new MockScopeServiceCollector();
        mockScopeCollectorRegistration = context.registerService(ScopeServiceCollector.class, mockScopeCollector,
                scopeProps);

        mockRegistryCollector = new MockRegistryServiceCollector();
        mockRegistryCollectorRegistration = context.registerService(RegistryServiceCollector.class,
                mockRegistryCollector, scopeProps);

        // Small delay to allow service registration to propagate
        Thread.sleep(200);

        // Ensure XMI factory is registered
        TestHelper.ensureXMIFactory(resourceSet);

        // Wait for the ObjectRegistryResource to be registered in Jakarta REST runtime
        ResourceAware resourceAware = ResourceAware.create(context, "ObjectRegistryResource");
        boolean resourceReady = resourceAware.waitForResource(15, TimeUnit.SECONDS);

        assertTrue(resourceReady, "ObjectRegistryResource should be registered within 15 seconds. "
                + "Check that the resource is properly configured and the Jakarta REST runtime is working.");
    }

    @AfterEach
    public void teardown(@InjectBundleContext BundleContext context) throws Exception {
        if (nonNull(mockScopeCollectorRegistration)) {
            mockScopeCollectorRegistration.unregister();
            mockScopeCollectorRegistration = null;
        }

        if (nonNull(mockRegistryCollectorRegistration)) {
            mockRegistryCollectorRegistration.unregister();
            mockRegistryCollectorRegistration = null;
        }

        // Small delay to allow service unregistration to propagate
        Thread.sleep(200);

        if (nonNull(restClient)) {
            restClient.close();
            restClient = null;
        }
    }

    // ========== List Operations Tests ==========

    @Test
    public void testListReleasedObjects_Success() {
        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).request("application/json").get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("metadata"), "Response should contain metadata");
    }

    @Test
    public void testListReleasedObjects_ScopeNotFound() {
        Response response = restClient.target(BASE_URL).path("non-existent-scope").path("registries")
                .path(TEST_REGISTRY_NAME).request("application/json").get();

        assertEquals(400, response.getStatus(), "Should return HTTP 400 Bad Request");
    }

    @Test
    public void testListObjectsInStage_Success() {
        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_DRAFT).request("application/json").get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("metadata"), "Response should contain metadata");
    }

    @Test
    public void testListObjectsInStage_WithObjectIdFilter() {
        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_DRAFT).queryParam("objectId", TEST_OBJECT_ID)
                .request("application/json").get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
    }

    @Test
    public void testListObjectsInStage_WithNameFilter() {
        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_DRAFT).queryParam("name", TEST_OBJECT_NAME)
                .request("application/json").get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
    }

    @Test
    public void testListObjectsInStage_NotFound() {
        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_DRAFT)
                .queryParam("objectId", "non-existent-object").request("application/json").get();

        assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content");
    }

    // ========== Create Object Tests ==========

    @Test
    public void testCreateObject_Success() throws Exception {
        EPackage newObject = TestHelper.createTestEPackage("http://test.com/newobject/1.0", "NewObject", "test");
        String xmiContent = TestHelper.serializeToXMI(newObject, resourceSet);

        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_DRAFT).path("new-object-id")
                .queryParam("name", "NewObject").queryParam("version", "1.0.0").request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(201, response.getStatus(), "Should return HTTP 201 Created");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return metadata");
        assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
    }

    @Test
    public void testCreateObject_IncompatibleEClass() throws Exception {
        // Create an EObject that is NOT an EPackage (e.g., EClass)
        org.eclipse.emf.ecore.EcoreFactory ecoreFactory = org.eclipse.emf.ecore.EcoreFactory.eINSTANCE;
        org.eclipse.emf.ecore.EClass incompatibleObject = ecoreFactory.createEClass();
        incompatibleObject.setName("IncompatibleClass");

        String xmiContent = TestHelper.serializeToXMI(incompatibleObject, resourceSet);

        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_DRAFT).path("incompatible-object-id")
                .queryParam("name", "IncompatibleObject").queryParam("version", "1.0.0").request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(400, response.getStatus(), "Should return HTTP 400 Bad Request for incompatible EClass");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return error message");
        assertTrue(responseContent.contains("not compatible"), "Error message should mention incompatibility");
    }

    @Test
    public void testCreateObject_UnknownRegistry() throws Exception {
        EPackage newObject = TestHelper.createTestEPackage("http://test.com/object/1.0", "TestObject", "test");
        String xmiContent = TestHelper.serializeToXMI(newObject, resourceSet);

        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path("unknown-registry").path("stages").path(TEST_STAGE_DRAFT).path("test-object-id")
                .queryParam("name", "TestObject").queryParam("version", "1.0.0").request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(400, response.getStatus(), "Should return HTTP 400 Bad Request for unknown registry");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return error message");
        assertTrue(responseContent.contains("Unknown") || responseContent.contains("registry"),
                "Error message should mention unknown registry");
    }

    @Test
    public void testCreateObject_Conflict() throws Exception {
        EPackage newObject = TestHelper.createTestEPackage("http://test.com/object/1.0", "TestObject", "test");
        String xmiContent = TestHelper.serializeToXMI(newObject, resourceSet);

        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_DRAFT).path(TEST_OBJECT_ID)
                .queryParam("name", "TestObject").queryParam("version", "1.0.0").request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(409, response.getStatus(), "Should return HTTP 409 Conflict for duplicate object ID");
    }

    @Test
    public void testCreateObject_WithOverrideSuccess() throws Exception {
        // Use an existing object ID with override=true
        EPackage updatedObject = TestHelper.createTestEPackage("http://test.com/object/1.0", "UpdatedTestObject",
                "upd");
        String xmiContent = TestHelper.serializeToXMI(updatedObject, resourceSet);

        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_DRAFT).path(TEST_OBJECT_ID)
                .queryParam("name", "UpdatedTestObject").queryParam("version", "1.1.0").queryParam("override", "true")
                .request("application/xmi").post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK when override is true and object exists");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return updated metadata");
        assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
    }

    @Test
    public void testCreateObject_WithOverrideFalseConflict() throws Exception {
        // Use an existing object ID with override=false
        EPackage testObject = TestHelper.createTestEPackage("http://test.com/object/1.0", "TestObject", "test");
        String xmiContent = TestHelper.serializeToXMI(testObject, resourceSet);

        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_DRAFT).path(TEST_OBJECT_ID)
                .queryParam("name", "TestObject").queryParam("version", "1.0.0").queryParam("override", "false")
                .request("application/xmi").post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(409, response.getStatus(),
                "Should return HTTP 409 Conflict when override is false and object exists");
    }

    @Test
    public void testCreateObject_WithOverrideNewObject() throws Exception {
        // Use a non-existing object ID with override=true (should create new)
        EPackage newObject = TestHelper.createTestEPackage("http://test.com/newobject/1.0", "NewObject", "new");
        String xmiContent = TestHelper.serializeToXMI(newObject, resourceSet);

        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_DRAFT).path("new-object-id")
                .queryParam("name", "NewObject").queryParam("version", "1.0.0").queryParam("override", "true")
                .request("application/xmi").post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(201, response.getStatus(),
                "Should return HTTP 201 Created when override is true and object doesn't exist");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return metadata");
        assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
    }

    // ========== Get Object Content Tests ==========

    @Test
    public void testGetObjectContent_Success() {
        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_DRAFT).path("content")
                .queryParam("objectId", TEST_OBJECT_ID).request("application/json").get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
    }

    @Test
    public void testGetObjectContent_NotFound() {
        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_DRAFT).path("content")
                .queryParam("objectId", "non-existent-object").request("application/json").get();

        assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content");
    }

    // ========== Update Object Content Tests ==========

    @Test
    public void testUpdateObjectContent_Success() throws Exception {
        EPackage updatedObject = TestHelper.createTestEPackage("http://test.com/object/1.0", "UpdatedObject", "test");
        String xmiContent = TestHelper.serializeToXMI(updatedObject, resourceSet);

        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_DRAFT).path("content")
                .queryParam("objectId", TEST_OBJECT_ID).queryParam("version", "1.1.0").request("application/xmi")
                .put(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return updated metadata");
        assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
    }

    @Test
    public void testUpdateObjectContent_ReadOnlyStage() throws Exception {
        EPackage updatedObject = TestHelper.createTestEPackage("http://test.com/object/1.0", "UpdatedObject", "test");
        String xmiContent = TestHelper.serializeToXMI(updatedObject, resourceSet);

        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path("readonly-stage").path("content")
                .queryParam("objectId", TEST_OBJECT_ID).queryParam("version", "1.1.0").request("application/xmi")
                .put(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(403, response.getStatus(), "Should return HTTP 403 Forbidden for read-only stage");
    }

    @Test
    public void testUpdateObjectContent_NotFound() throws Exception {
        EPackage updatedObject = TestHelper.createTestEPackage("http://test.com/object/1.0", "NonExistent", "ne");
        String xmiContent = TestHelper.serializeToXMI(updatedObject, resourceSet);

        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_DRAFT).path("content")
                .queryParam("objectId", "non-existent-object").queryParam("version", "1.0.0").request("application/xmi")
                .put(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content when object not found");
    }

    // ========== Delete Object Tests ==========

    @Test
    public void testDeleteObject_Success() {
        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_DRAFT).queryParam("objectId", TEST_OBJECT_ID)
                .request().delete();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
    }

    @Test
    public void testDeleteObject_ReadOnlyStage() {
        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path("readonly-stage").queryParam("objectId", TEST_OBJECT_ID)
                .request().delete();

        assertEquals(403, response.getStatus(), "Should return HTTP 403 Forbidden");
    }

    @Test
    public void testDeleteObject_NotFound() {
        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_DRAFT)
                .queryParam("objectId", "non-existent-object").request().delete();

        assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content");
    }

    // ========== Transition Tests ==========

    @Test
    public void testTransitionObject_Success() throws Exception {
        StageTransitionRequest transition = RestFactory.eINSTANCE.createStageTransitionRequest();
        transition.setObjectId(TEST_OBJECT_ID);
        transition.setTargetStage(TEST_STAGE_APPROVED);

        String xmiContent = TestHelper.serializeToXMI(transition, resourceSet);

        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_DRAFT).path("actions").path("transition")
                .request("application/xmi").post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return transition metadata");
        assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
    }

    @Test
    public void testTransitionObject_InvalidTransition() throws Exception {
        StageTransitionRequest transition = RestFactory.eINSTANCE.createStageTransitionRequest();
        transition.setObjectId(TEST_OBJECT_ID);
        transition.setTargetStage(TEST_STAGE_RELEASE); // Invalid: skipping approved stage

        String xmiContent = TestHelper.serializeToXMI(transition, resourceSet);

        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_DRAFT).path("actions").path("transition")
                .request("application/xmi").post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(400, response.getStatus(), "Should return HTTP 400 Bad Request");
    }

    @Test
    public void testTransitionObject_NotFound() throws Exception {
        StageTransitionRequest transition = RestFactory.eINSTANCE.createStageTransitionRequest();
        transition.setObjectId("non-existent-object");
        transition.setTargetStage(TEST_STAGE_APPROVED);

        String xmiContent = TestHelper.serializeToXMI(transition, resourceSet);

        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_DRAFT).path("actions").path("transition")
                .request("application/xmi").post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content");
    }

    // ========== List Objects By Name Tests ==========

    @Test
    public void testListObjectsInStageByName_ExactMatch() {
        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_DRAFT).queryParam("name", TEST_OBJECT_NAME)
                .request("application/json").get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
        assertTrue(responseContent.contains(TEST_OBJECT_NAME), "Response should contain the object name");
    }

    @Test
    public void testListObjectsInStageByName_WildcardMatch() {
        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_DRAFT).queryParam("name", "Test*")
                .request("application/json").get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
    }

    @Test
    public void testListObjectsInStageByName_NotFound() {
        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_DRAFT).queryParam("name", "NonExistentObject")
                .request("application/json").get();

        assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content when no objects match");
    }

    @Test
    public void testListObjectsInStageByName_DifferentObject() {
        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_DRAFT).queryParam("name", "SensorData")
                .request("application/json").get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("SensorData"), "Response should contain SensorData");
    }

    @Test
    public void testListObjectsInStageByName_DifferentStage() {
        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_APPROVED).queryParam("name", TEST_OBJECT_NAME)
                .request("application/json").get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
    }

    // ========== MediaType Query Parameter Tests ==========

    @Test
    public void testListReleasedObjects_WithMediaTypeQueryParam() {
        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).queryParam("mediaType", "application/xml").request("application/json").get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
        assertEquals("application/xml", response.getHeaderString("Content-Type"),
                "Content-Type header should be set to mediaType query parameter value");
    }

    @Test
    public void testListReleasedObjects_WithUnsupportedMediaTypeQueryParam() {
        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).queryParam("mediaType", "application/unsupported").request("application/json")
                .get();

        assertEquals(415, response.getStatus(), "Should return HTTP 415 Unsupported Media Type");
    }

    @Test
    public void testListObjectsInStage_WithMediaTypeQueryParam() {
        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_DRAFT)
                .queryParam("mediaType", "application/xml").request("application/json").get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
        assertEquals("application/xml", response.getHeaderString("Content-Type"),
                "Content-Type header should be set to mediaType query parameter value");
    }

    @Test
    public void testGetObjectContent_WithMediaTypeQueryParam() {
        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_DRAFT).path("content")
                .queryParam("objectId", TEST_OBJECT_ID).queryParam("mediaType", "application/xml")
                .request("application/json").get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
        assertEquals("application/xml", response.getHeaderString("Content-Type"),
                "Content-Type header should be set to mediaType query parameter value");
    }

    @Test
    public void testGetObjectContent_WithUnsupportedMediaTypeQueryParam() {
        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_DRAFT).path("content")
                .queryParam("objectId", TEST_OBJECT_ID).queryParam("mediaType", "application/unsupported")
                .request("application/json").get();

        assertEquals(415, response.getStatus(), "Should return HTTP 415 Unsupported Media Type");
    }

    @Test
    public void testCreateObject_WithMediaTypeQueryParam() throws Exception {
        EPackage newObject = TestHelper.createTestEPackage("http://test.com/mediatype/1.0", "MediaTypeObject", "mt");
        String xmiContent = TestHelper.serializeToXMI(newObject, resourceSet);

        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_DRAFT).path("mediatype-object-id")
                .queryParam("name", "MediaTypeObject").queryParam("version", "1.0.0")
                .queryParam("mediaType", "application/xml").request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(201, response.getStatus(), "Should return HTTP 201 Created");
        assertEquals("application/xml", response.getHeaderString("Content-Type"),
                "Content-Type header should be set to mediaType query parameter value");
    }

    @Test
    public void testUpdateObjectContent_WithMediaTypeQueryParam() throws Exception {
        EPackage updatedObject = TestHelper.createTestEPackage("http://test.com/object/1.0", "UpdatedObject", "test");
        String xmiContent = TestHelper.serializeToXMI(updatedObject, resourceSet);

        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_DRAFT).path("content")
                .queryParam("objectId", TEST_OBJECT_ID).queryParam("version", "1.1.0")
                .queryParam("mediaType", "application/xml").request("application/xmi")
                .put(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
        assertEquals("application/xml", response.getHeaderString("Content-Type"),
                "Content-Type header should be set to mediaType query parameter value");
    }

    @Test
    public void testTransitionObject_WithMediaTypeQueryParam() throws Exception {
        StageTransitionRequest transition = RestFactory.eINSTANCE.createStageTransitionRequest();
        transition.setObjectId(TEST_OBJECT_ID);
        transition.setTargetStage(TEST_STAGE_APPROVED);

        String xmiContent = TestHelper.serializeToXMI(transition, resourceSet);

        Response response = restClient.target(BASE_URL).path(TEST_SCOPE_NAME).path("registries")
                .path(TEST_REGISTRY_NAME).path("stages").path(TEST_STAGE_DRAFT).path("actions").path("transition")
                .queryParam("mediaType", "application/xml").request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
        assertEquals("application/xml", response.getHeaderString("Content-Type"),
                "Content-Type header should be set to mediaType query parameter value");
    }
}
