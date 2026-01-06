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

import java.time.Instant;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.model.scope.Scope;
import org.eclipse.fennec.model.atlas.model.scope.ScopeFactory;
import org.eclipse.fennec.model.atlas.model.scope.StageTransition;
import org.eclipse.fennec.model.atlas.rest.tests.helper.ResourceAware;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestHelper;
import org.eclipse.fennec.model.atlas.schema.registry.api.SchemaRegistryService;
import org.eclipse.fennec.model.atlas.scope.ScopeCollector;
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
 * Integration tests for ObjectRegistryResource REST endpoints.
 *
 * <p>Tests cover:</p>
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
    private MockScopeCollector mockScopeCollector;
    private ServiceRegistration<ScopeCollector> mockScopeCollectorRegistration;
    private MockSchemaRegistryService mockSchemaRegistryService;
    private ServiceRegistration<SchemaRegistryService> mockSchemaRegistryServiceRegistration;

    @BeforeEach
    public void setup(@InjectBundleContext BundleContext context) throws Exception {
        // Setup REST client
        restClient = clientBuilder.build();

        // Create and register mock ScopeCollector
        mockScopeCollector = new MockScopeCollector();

        Dictionary<String, Object> scopeProps = new Hashtable<>();
        scopeProps.put("service.ranking", Integer.MAX_VALUE);

        mockScopeCollectorRegistration = context.registerService(
                ScopeCollector.class,
                mockScopeCollector,
                scopeProps);

        // Create and register mock SchemaRegistryService
        mockSchemaRegistryService = new MockSchemaRegistryService(resourceSet);

        Dictionary<String, Object> registryProps = new Hashtable<>();
        registryProps.put("registry.name", TEST_REGISTRY_NAME);
        registryProps.put("service.ranking", Integer.MAX_VALUE);

        mockSchemaRegistryServiceRegistration = context.registerService(
                SchemaRegistryService.class,
                mockSchemaRegistryService,
                registryProps);

        // Small delay to allow service registration to propagate
        Thread.sleep(200);

        // Ensure XMI factory is registered
        TestHelper.ensureXMIFactory(resourceSet);

        // Wait for the ObjectRegistryResource to be registered in Jakarta REST runtime
        ResourceAware resourceAware = ResourceAware.create(context, "ObjectRegistryResource");
        boolean resourceReady = resourceAware.waitForResource(15, TimeUnit.SECONDS);

        assertTrue(resourceReady,
                "ObjectRegistryResource should be registered within 15 seconds. " +
                "Check that the resource is properly configured and the Jakarta REST runtime is working.");
    }

    @AfterEach
    public void teardown(@InjectBundleContext BundleContext context) throws Exception {
        if (nonNull(mockScopeCollectorRegistration)) {
            mockScopeCollectorRegistration.unregister();
            mockScopeCollectorRegistration = null;
        }

        if (nonNull(mockSchemaRegistryServiceRegistration)) {
            mockSchemaRegistryServiceRegistration.unregister();
            mockSchemaRegistryServiceRegistration = null;
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
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("registries")
                .path(TEST_REGISTRY_NAME)
                .request("application/json")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("metadata"), "Response should contain metadata");
    }

    @Test
    public void testListReleasedObjects_ScopeNotFound() {
        Response response = restClient
                .target(BASE_URL)
                .path("non-existent-scope")
                .path("registries")
                .path(TEST_REGISTRY_NAME)
                .request("application/json")
                .get();

        assertEquals(404, response.getStatus(), "Should return HTTP 404 No Content");
    }

    @Test
    public void testListObjectsInStage_Success() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("registries")
                .path(TEST_REGISTRY_NAME)
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .request("application/json")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("metadata"), "Response should contain metadata");
    }

    @Test
    public void testListObjectsInStage_WithObjectIdFilter() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("registries")
                .path(TEST_REGISTRY_NAME)
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("objectId", TEST_OBJECT_ID)
                .request("application/json")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
    }

    @Test
    public void testListObjectsInStage_WithNameFilter() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("registries")
                .path(TEST_REGISTRY_NAME)
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("name", TEST_OBJECT_NAME)
                .request("application/json")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
    }

    @Test
    public void testListObjectsInStage_NotFound() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("registries")
                .path(TEST_REGISTRY_NAME)
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("objectId", "non-existent-object")
                .request("application/json")
                .get();

        assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content");
    }

    // ========== Create Object Tests ==========

    @Test
    public void testCreateObject_Success() throws Exception {
        EPackage newObject = TestHelper.createTestEPackage("http://test.com/newobject/1.0", "NewObject", "test");
        String xmiContent = TestHelper.serializeToXMI(newObject, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("registries")
                .path(TEST_REGISTRY_NAME)
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .path("new-object-id")
                .queryParam("name", "NewObject")
                .queryParam("version", "1.0.0")
                .request("application/xmi")
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

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("registries")
                .path(TEST_REGISTRY_NAME)
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .path("incompatible-object-id")
                .queryParam("name", "IncompatibleObject")
                .queryParam("version", "1.0.0")
                .request("application/xmi")
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

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("registries")
                .path("unknown-registry")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .path("test-object-id")
                .queryParam("name", "TestObject")
                .queryParam("version", "1.0.0")
                .request("application/xmi")
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

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("registries")
                .path(TEST_REGISTRY_NAME)
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .path(TEST_OBJECT_ID)
                .queryParam("name", "TestObject")
                .queryParam("version", "1.0.0")
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(409, response.getStatus(), "Should return HTTP 409 Conflict for duplicate object ID");
    }

    @Test
    public void testCreateObject_WithOverrideSuccess() throws Exception {
        // Use an existing object ID with override=true
        EPackage updatedObject = TestHelper.createTestEPackage("http://test.com/object/1.0", "UpdatedTestObject", "upd");
        String xmiContent = TestHelper.serializeToXMI(updatedObject, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("registries")
                .path(TEST_REGISTRY_NAME)
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .path(TEST_OBJECT_ID)
                .queryParam("name", "UpdatedTestObject")
                .queryParam("version", "1.1.0")
                .queryParam("override", "true")
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

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

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("registries")
                .path(TEST_REGISTRY_NAME)
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .path(TEST_OBJECT_ID)
                .queryParam("name", "TestObject")
                .queryParam("version", "1.0.0")
                .queryParam("override", "false")
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(409, response.getStatus(), "Should return HTTP 409 Conflict when override is false and object exists");
    }

    @Test
    public void testCreateObject_WithOverrideNewObject() throws Exception {
        // Use a non-existing object ID with override=true (should create new)
        EPackage newObject = TestHelper.createTestEPackage("http://test.com/newobject/1.0", "NewObject", "new");
        String xmiContent = TestHelper.serializeToXMI(newObject, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("registries")
                .path(TEST_REGISTRY_NAME)
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .path("new-object-id")
                .queryParam("name", "NewObject")
                .queryParam("version", "1.0.0")
                .queryParam("override", "true")
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(201, response.getStatus(), "Should return HTTP 201 Created when override is true and object doesn't exist");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return metadata");
        assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
    }

    // ========== Get Object Content Tests ==========

    @Test
    public void testGetObjectContent_Success() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("registries")
                .path(TEST_REGISTRY_NAME)
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .path("content")
                .queryParam("objectId", TEST_OBJECT_ID)
                .request("application/json")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
    }

    @Test
    public void testGetObjectContent_NotFound() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("registries")
                .path(TEST_REGISTRY_NAME)
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .path("content")
                .queryParam("objectId", "non-existent-object")
                .request("application/json")
                .get();

        assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content");
    }

    // ========== Update Object Content Tests ==========

    @Test
    public void testUpdateObjectContent_Success() throws Exception {
        EPackage updatedObject = TestHelper.createTestEPackage("http://test.com/object/1.0", "UpdatedObject", "test");
        String xmiContent = TestHelper.serializeToXMI(updatedObject, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("registries")
                .path(TEST_REGISTRY_NAME)
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .path("content")
                .queryParam("objectId", TEST_OBJECT_ID)
                .queryParam("version", "1.1.0")
                .request("application/xmi")
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

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("registries")
                .path(TEST_REGISTRY_NAME)
                .path("stages")
                .path("readonly-stage")
                .path("content")
                .queryParam("objectId", TEST_OBJECT_ID)
                .queryParam("version", "1.1.0")
                .request("application/xmi")
                .put(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(403, response.getStatus(), "Should return HTTP 403 Forbidden for read-only stage");
    }

    @Test
    public void testUpdateObjectContent_NotFound() throws Exception {
        EPackage updatedObject = TestHelper.createTestEPackage("http://test.com/object/1.0", "NonExistent", "ne");
        String xmiContent = TestHelper.serializeToXMI(updatedObject, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("registries")
                .path(TEST_REGISTRY_NAME)
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .path("content")
                .queryParam("objectId", "non-existent-object")
                .queryParam("version", "1.0.0")
                .request("application/xmi")
                .put(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content when object not found");
    }

    // ========== Delete Object Tests ==========

    @Test
    public void testDeleteObject_Success() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("registries")
                .path(TEST_REGISTRY_NAME)
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("objectId", TEST_OBJECT_ID)
                .request()
                .delete();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
    }

    @Test
    public void testDeleteObject_ReadOnlyStage() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("registries")
                .path(TEST_REGISTRY_NAME)
                .path("stages")
                .path("readonly-stage")
                .queryParam("objectId", TEST_OBJECT_ID)
                .request()
                .delete();

        assertEquals(403, response.getStatus(), "Should return HTTP 403 Forbidden");
    }

    @Test
    public void testDeleteObject_NotFound() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("registries")
                .path(TEST_REGISTRY_NAME)
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("objectId", "non-existent-object")
                .request()
                .delete();

        assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content");
    }

    // ========== Transition Tests ==========

    @Test
    public void testTransitionObject_Success() throws Exception {
        StageTransition transition = ScopeFactory.eINSTANCE.createStageTransition();
        transition.setObjectId(TEST_OBJECT_ID);
        transition.setTargetStage(TEST_STAGE_APPROVED);

        String xmiContent = TestHelper.serializeToXMI(transition, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("registries")
                .path(TEST_REGISTRY_NAME)
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .path("actions")
                .path("transition")
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return transition metadata");
        assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
    }

    @Test
    public void testTransitionObject_InvalidTransition() throws Exception {
        StageTransition transition = ScopeFactory.eINSTANCE.createStageTransition();
        transition.setObjectId(TEST_OBJECT_ID);
        transition.setTargetStage(TEST_STAGE_RELEASE); // Invalid: skipping approved stage

        String xmiContent = TestHelper.serializeToXMI(transition, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("registries")
                .path(TEST_REGISTRY_NAME)
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .path("actions")
                .path("transition")
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(400, response.getStatus(), "Should return HTTP 400 Bad Request");
    }

    @Test
    public void testTransitionObject_NotFound() throws Exception {
        StageTransition transition = ScopeFactory.eINSTANCE.createStageTransition();
        transition.setObjectId("non-existent-object");
        transition.setTargetStage(TEST_STAGE_APPROVED);

        String xmiContent = TestHelper.serializeToXMI(transition, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("registries")
                .path(TEST_REGISTRY_NAME)
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .path("actions")
                .path("transition")
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content");
    }

    // ========== List Objects By Name Tests ==========

    @Test
    public void testListObjectsInStageByName_ExactMatch() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("registries")
                .path(TEST_REGISTRY_NAME)
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("name", TEST_OBJECT_NAME)
                .request("application/json")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
        assertTrue(responseContent.contains(TEST_OBJECT_NAME), "Response should contain the object name");
    }

    @Test
    public void testListObjectsInStageByName_WildcardMatch() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("registries")
                .path(TEST_REGISTRY_NAME)
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("name", "Test*")
                .request("application/json")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
    }

    @Test
    public void testListObjectsInStageByName_NotFound() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("registries")
                .path(TEST_REGISTRY_NAME)
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("name", "NonExistentObject")
                .request("application/json")
                .get();

        assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content when no objects match");
    }

    @Test
    public void testListObjectsInStageByName_DifferentObject() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("registries")
                .path(TEST_REGISTRY_NAME)
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("name", "SensorData")
                .request("application/json")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("SensorData"), "Response should contain SensorData");
    }

    @Test
    public void testListObjectsInStageByName_DifferentStage() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("registries")
                .path(TEST_REGISTRY_NAME)
                .path("stages")
                .path(TEST_STAGE_APPROVED)
                .queryParam("name", TEST_OBJECT_NAME)
                .request("application/json")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
    }

    // ========== Mock Service Implementation ==========

    /**
     * Mock implementation of ScopeCollector for testing.
     */
    public static class MockScopeCollector extends ScopeCollector {

        private final MockEObjectWorkflowService mockWorkflowService = new MockEObjectWorkflowService();
        private final Scope mockScope = createMockScope();

        @Override
        public EObjectWorkflowService<?> getWorkflowServiceByScope(String scopeName) {
            if (TEST_SCOPE_NAME.equals(scopeName)) {
                return mockWorkflowService;
            }
            return null;
        }

        @Override
        public Scope getWorkflowScopeByName(String name) {
            if (TEST_SCOPE_NAME.equals(name)) {
                return mockScope;
            }
            return null;
        }

        @Override
        public List<Scope> getAllScopes() {
            return List.of(mockScope);
        }

        private Scope createMockScope() {
            Scope scope = ScopeFactory.eINSTANCE.createScope();
            scope.setName(TEST_SCOPE_NAME);
            scope.setFinalStage(TEST_STAGE_RELEASE);
            scope.getStages().addAll(List.of(TEST_STAGE_DRAFT, TEST_STAGE_APPROVED, TEST_STAGE_RELEASE));
            scope.getWritableStages().addAll(List.of(TEST_STAGE_DRAFT, TEST_STAGE_APPROVED));
            scope.getRegistries().add(TEST_REGISTRY_NAME);
            return scope;
        }
    }

    /**
     * Mock implementation of EObjectWorkflowService for testing.
     */
    public static class MockEObjectWorkflowService implements EObjectWorkflowService<EObject> {

        @Override
        public Promise<ObjectMetadata> uploadToStageForRegistry(String stage, String registry, EObject object, ObjectMetadata metadata) {
            metadata.setRole(stage);
            metadata.setRegistry(registry);
            metadata.setUploadTime(Instant.now());
            return Promises.resolved(metadata);
        }

        @Override
        public ObjectMetadata getFromStageForRegistry(String stage, String registry, String objectId) {
            // Return null for non-existent objects (including new objects to be created)
            if (objectId.equals("non-existent-object") ||
                objectId.equals("new-object-id") ||
                objectId.equals("incompatible-object-id")) {
                return null;
            }

            // Only TEST_OBJECT_ID and sensor-object-456 exist
            if (!objectId.equals(TEST_OBJECT_ID) && !objectId.equals("sensor-object-456")) {
                return null;
            }

            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setObjectId(objectId);
            metadata.setRole(stage);
            metadata.setRegistry(registry);
            metadata.setUploadTime(Instant.now());
            metadata.setIsReadOnly(false);
            return metadata;
        }

        @Override
        public ObjectMetadata getFromFinalStageForRegistry(String registry, String objectId) {
            return getFromStageForRegistry(TEST_STAGE_RELEASE, registry, objectId);
        }

        @Override
        public EObject getContentFromStageForRegistry(String stage, String registry, String objectId) {
            // Return null for non-existent objects
            if (objectId.equals("non-existent-object") ||
                objectId.equals("new-object-id") ||
                objectId.equals("incompatible-object-id")) {
                return null;
            }

            // Only TEST_OBJECT_ID and sensor-object-456 have content
            if (!objectId.equals(TEST_OBJECT_ID) && !objectId.equals("sensor-object-456")) {
                return null;
            }

            return TestHelper.createTestEPackage("http://test.com/object/1.0", "TestObject", "test");
        }

        @Override
        public Promise<ObjectMetadata> updateInStageForRegistry(String stage, String registry, EObject updatedObject, String objectId, String updatedVersion) {
            // Only allow updates for existing objects
            if (objectId.equals("non-existent-object") ||
                objectId.equals("new-object-id") ||
                objectId.equals("incompatible-object-id") ||
                (!objectId.equals(TEST_OBJECT_ID) && !objectId.equals("sensor-object-456"))) {
                return Promises.resolved(null);
            }

            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setObjectId(objectId);
            metadata.setRole(stage);
            metadata.setRegistry(registry);
            metadata.setVersion(updatedVersion);
            metadata.setLastChangeTime(Instant.now());
            return Promises.resolved(metadata);
        }

        @Override
        public Promise<Boolean> deleteFromStageForRegistry(String stage, String registry, String objectId) {
            // Only allow deletion of existing objects
            return Promises.resolved(
                objectId.equals(TEST_OBJECT_ID) || objectId.equals("sensor-object-456")
            );
        }

        @Override
        public List<ObjectMetadata> listInStageForRegistry(String stage, String registry) {
            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setObjectId(TEST_OBJECT_ID);
            metadata.setRole(stage);
            metadata.setRegistry(registry);
            metadata.setObjectName(TEST_OBJECT_NAME);
            metadata.setUploadTime(Instant.now());
            return List.of(metadata);
        }

        @Override
        public List<ObjectMetadata> listInFinalStageForRegistry(String registry) {
            return listInStageForRegistry(TEST_STAGE_RELEASE, registry);
        }

        @Override
        public ObjectMetadata transitionToStageForRegistry(String objectId, String fromStage, String toStage, String registry) {
            // Only allow transitions for existing objects
            if (objectId.equals("non-existent-object") ||
                objectId.equals("new-object-id") ||
                objectId.equals("incompatible-object-id") ||
                (!objectId.equals(TEST_OBJECT_ID) && !objectId.equals("sensor-object-456"))) {
                return null;
            }

            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setObjectId(objectId);
            metadata.setRole(toStage);
            metadata.setRegistry(registry);
            metadata.setLastChangeTime(Instant.now());
            return metadata;
        }

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

        @Override
        public List<ObjectMetadata> listInStageForRegistryByName(String stage, String registry, String name) {
            // Support wildcard search with * character (only trailing wildcards like "Prefix*")
            boolean isWildcard = name.contains("*");
            String nameFilter = isWildcard ? name.replace("*", "") : name;

            // Create test metadata that matches the filter
            if (TEST_OBJECT_NAME.equals(name) || (isWildcard && TEST_OBJECT_NAME.startsWith(nameFilter))) {
                ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
                metadata.setObjectId(TEST_OBJECT_ID);
                metadata.setRole(stage);
                metadata.setRegistry(registry);
                metadata.setObjectName(TEST_OBJECT_NAME);
                metadata.setUploadTime(Instant.now());
                return List.of(metadata);
            }

            // Match for Test* wildcard
            if (isWildcard && TEST_OBJECT_NAME.startsWith(nameFilter)) {
                ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
                metadata.setObjectId(TEST_OBJECT_ID);
                metadata.setRole(stage);
                metadata.setRegistry(registry);
                metadata.setObjectName(TEST_OBJECT_NAME);
                metadata.setUploadTime(Instant.now());
                return List.of(metadata);
            }

            // If looking for SensorData, return a matching object
            if ("SensorData".equals(name) || (isWildcard && "SensorData".startsWith(nameFilter))) {
                ObjectMetadata sensorMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
                sensorMetadata.setObjectId("sensor-object-456");
                sensorMetadata.setRole(stage);
                sensorMetadata.setRegistry(registry);
                sensorMetadata.setObjectName("SensorData");
                sensorMetadata.setUploadTime(Instant.now());
                return List.of(sensorMetadata);
            }

            // Return empty list if no match
            return List.of();
        }


    }

    /**
     * Mock implementation of SchemaRegistryService for testing.
     */
    public static class MockSchemaRegistryService implements SchemaRegistryService {

        private final ResourceSet resourceSet;
        private final org.eclipse.emf.ecore.EClass ePackageEClass;

        public MockSchemaRegistryService(ResourceSet resourceSet) {
            this.resourceSet = resourceSet;
            // Get EPackage EClass from Ecore
            this.ePackageEClass = (org.eclipse.emf.ecore.EClass) resourceSet.getEObject(
                org.eclipse.emf.common.util.URI.createURI("http://www.eclipse.org/emf/2002/Ecore#//EPackage"),
                true
            );
        }

        @Override
        public org.eclipse.emf.ecore.EClass getRootEClass() {
            return ePackageEClass;
        }

        @Override
        public String getRegistryName() {
            return TEST_REGISTRY_NAME;
        }

        @Override
        public boolean isCompatible(org.eclipse.emf.ecore.EClass eClass) {
            // EPackage is compatible with itself or any subtype
            return eClass.equals(ePackageEClass) || eClass.getEAllSuperTypes().contains(ePackageEClass);
        }

		/* 
		 * (non-Javadoc)
		 * @see org.eclipse.fennec.model.atlas.schema.registry.api.SchemaRegistryService#getSchemaUri()
		 */
		@Override
		public String getSchemaUri() {
			return "http://www.eclipse.org/emf/2002/Ecore";
		}
    }
}
