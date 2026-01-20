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
import org.eclipse.fennec.model.atlas.rest.tests.helper.MockTestHelper.MockScopeServiceCollector;
import org.eclipse.fennec.model.atlas.rest.tests.helper.ResourceAware;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestHelper;
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
 * Integration tests for SchemaPackagesResource REST endpoints.
 *
 * <p>Tests cover:</p>
 * <ul>
 * <li>Listing packages in final stage and specific stages</li>
 * <li>Creating packages in stages</li>
 * <li>Retrieving package metadata and content</li>
 * <li>Updating package content</li>
 * <li>Deleting packages</li>
 * <li>Transitioning packages between stages</li>
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
public class SchemaPackagesResourceTest {

    private static final String BASE_URL = "http://localhost:8185/rest";
    private static final String TEST_SCOPE_NAME = "test-scope";
    private static final String TEST_PACKAGE_NSURI = "http://test.example.com/schema/1.1";
    private static final String TEST_PACKAGE_NAME = "TestSchema";
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

    @BeforeEach
    public void setup(@InjectBundleContext BundleContext context) throws Exception {
        // Setup REST client
        restClient = clientBuilder.build();

        // Create and register mock ScopeCollector
        Dictionary<String, Object> serviceProps = new Hashtable<>();
        serviceProps.put("service.ranking", Integer.MAX_VALUE);

        mockScopeCollector = new MockScopeServiceCollector();
        mockScopeCollectorRegistration = context.registerService(
                ScopeServiceCollector.class,
                mockScopeCollector,
                serviceProps);

        // Small delay to allow service registration to propagate
        Thread.sleep(200);

        // Ensure XMI factory is registered
        TestHelper.ensureXMIFactory(resourceSet);

        // Wait for the SchemaPackagesResource to be registered in Jakarta REST runtime
        ResourceAware resourceAware = ResourceAware.create(context, "SchemaPackagesResource");
        boolean resourceReady = resourceAware.waitForResource(15, TimeUnit.SECONDS);

        assertTrue(resourceReady,
                "SchemaPackagesResource should be registered within 15 seconds. " +
                "Check that the resource is properly configured and the Jakarta REST runtime is working.");
    }

    @AfterEach
    public void teardown(@InjectBundleContext BundleContext context) throws Exception {
        if (nonNull(mockScopeCollectorRegistration)) {
            mockScopeCollectorRegistration.unregister();
            mockScopeCollectorRegistration = null;

            // Small delay to allow service unregistration to propagate
            Thread.sleep(200);
        }

        if (nonNull(restClient)) {
            restClient.close();
            restClient = null;
        }
    }

    // ========== List Operations Tests ==========

    @Test
    public void testListReleasedPackages_Success() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .request("application/json")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("metadata"), "Response should contain metadata");
    }

    @Test
    public void testListReleasedPackages_ScopeNotFound() {
        Response response = restClient
                .target(BASE_URL)
                .path("non-existent-scope")
                .path("schema")
                .request("application/json")
                .get();

        assertEquals(400, response.getStatus(), "Should return HTTP 400 Bad Request");
    }

    @Test
    public void testListPackagesInStage_Success() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
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
    public void testListPackagesInStage_WithNsUriFilter() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("nsUri", TEST_PACKAGE_NSURI)
                .request("application/json")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
    }

    @Test
    public void testListPackagesInStage_NotFound() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("nsUri", "http://non-existent.com/schema/1.0")
                .request("application/json")
                .get();

        assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content");
    }

    // ========== Create Package Tests ==========

    @Test
    public void testCreatePackage_Success() throws Exception {
        EPackage testPackage = TestHelper.createTestEPackage("http://non-existent.com/schema/1.0", "new-package", "new");
        String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("nsUri", "http://non-existent.com/schema/1.0")
                .queryParam("name", "new-package")
                .queryParam("version", "1.0.0")
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        System.out.println("DEBUG testCreatePackage_Success - Response status: " + response.getStatus());
        String responseContent = response.readEntity(String.class);
        System.out.println("DEBUG testCreatePackage_Success - Response content: " + responseContent);

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
        assertNotNull(responseContent, "Should return content");
    }

    @Test
    public void testCreatePackage_ScopeNotFound() throws Exception {
        EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, "test");
        String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path("non-existent-scope")
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("nsUri", TEST_PACKAGE_NSURI)
                .queryParam("name", TEST_PACKAGE_NAME)
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(400, response.getStatus(), "Should return HTTP 400 Bad Request");
    }

    // ========== Override Parameter Tests ==========

    @Test
    public void testCreatePackage_Conflict_WithoutOverride() throws Exception {
        EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, "ExistingSchema", "existing");
        String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("nsUri", TEST_PACKAGE_NSURI)
                .queryParam("name", "ExistingSchema")
                .queryParam("override", false)
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        System.out.println("DEBUG testCreatePackage_Conflict_WithoutOverride - Response status: " + response.getStatus());
        String responseContent = response.readEntity(String.class);
        System.out.println("DEBUG testCreatePackage_Conflict_WithoutOverride - Response content: " + responseContent);

        assertEquals(409, response.getStatus(), "Should return HTTP 409 Conflict when override is false and package exists");
    }

    @Test
    public void testCreatePackage_WithOverride_Success() throws Exception {
        EPackage testPackage = TestHelper.createTestEPackage("http://existing.com/schema/1.0", "UpdatedExistingSchema", "existing");
        String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("nsUri", "http://existing.com/schema/1.0")
                .queryParam("name", "UpdatedExistingSchema")
                .queryParam("version", "1.0.0")
                .queryParam("override", true)
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        System.out.println("DEBUG testCreatePackage_WithOverride_Success - Response status: " + response.getStatus());
        String responseContent = response.readEntity(String.class);
        System.out.println("DEBUG testCreatePackage_WithOverride_Success - Response content: " + responseContent);

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK when override is true and package exists and is writable");
        assertNotNull(responseContent, "Should return updated metadata");
        assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
    }

    @Test
    public void testCreatePackage_WithOverride_ReadOnly() throws Exception {
        EPackage testPackage = TestHelper.createTestEPackage("http://readonly.com/schema/1.0", "ReadOnlySchema", "readonly");
        String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("nsUri", "http://readonly.com/schema/1.0")
                .queryParam("name", "ReadOnlySchema")
                .queryParam("version", "1.0.0")
                .queryParam("override", true)
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        System.out.println("DEBUG testCreatePackage_WithOverride_ReadOnly - Response status: " + response.getStatus());
        String responseContent = response.readEntity(String.class);
        System.out.println("DEBUG testCreatePackage_WithOverride_ReadOnly - Response content: " + responseContent);

        assertEquals(403, response.getStatus(), "Should return HTTP 403 Forbidden when override is true but package is read-only");
    }

    @Test
    public void testCreatePackage_WithOverride_NewPackage() throws Exception {
        EPackage testPackage = TestHelper.createTestEPackage("http://new-package.com/schema/1.0", "NewPackage", "new");
        String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("nsUri", "http://new-package.com/schema/1.0")
                .queryParam("name", "NewPackage")
                .queryParam("version", "1.0.0")
                .queryParam("override", true)
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        System.out.println("DEBUG testCreatePackage_WithOverride_NewPackage - Response status: " + response.getStatus());
        String responseContent = response.readEntity(String.class);
        System.out.println("DEBUG testCreatePackage_WithOverride_NewPackage - Response content: " + responseContent);

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK when override is true for new package (behaves like normal create)");
        assertNotNull(responseContent, "Should return created metadata");
    }

    // ========== Get Package Content Tests ==========

    @Test
    public void testGetPackageContent_Success() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .path("content")
                .queryParam("nsUri", TEST_PACKAGE_NSURI)
                .request("application/json")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
    }

    @Test
    public void testGetPackageContent_NotFound() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .path("content")
                .queryParam("nsUri", "http://non-existent.com/schema/1.0")
                .request("application/json")
                .get();

        assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content");
    }

    // ========== Update Package Content Tests ==========

    @Test
    public void testUpdatePackageContent_Success() throws Exception {
        EPackage updatedPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, "UpdatedSchema", "test");
        String xmiContent = TestHelper.serializeToXMI(updatedPackage, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .path("content")
                .queryParam("nsUri", TEST_PACKAGE_NSURI)
                .queryParam("version", "1.1.0")
                .request("application/xmi")
                .put(Entity.entity(xmiContent, "application/xmi"));

        System.out.println("DEBUG testUpdatePackageContent_Success - Response status: " + response.getStatus());
        String responseContent = response.readEntity(String.class);
        System.out.println("DEBUG testUpdatePackageContent_Success - Response content: " + responseContent);

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
    }

    @Test
    public void testUpdatePackageContent_ReadOnlyStage() throws Exception {
        EPackage updatedPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, "UpdatedSchema", "test");
        String xmiContent = TestHelper.serializeToXMI(updatedPackage, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path("readonly-stage")
                .path("content")
                .queryParam("nsUri", TEST_PACKAGE_NSURI)
                .queryParam("version", "1.1.0")
                .request("application/xmi")
                .put(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(403, response.getStatus(), "Should return HTTP 403 Forbidden");
    }

    @Test
    public void testUpdatePackageContent_NotFound() throws Exception {
        EPackage updatedPackage = TestHelper.createTestEPackage("http://non-existent.com/schema/1.0", "NonExistent", "ne");
        String xmiContent = TestHelper.serializeToXMI(updatedPackage, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .path("content")
                .queryParam("nsUri", "http://non-existent.com/schema/1.0")
                .queryParam("version", "1.0.0")
                .request("application/xmi")
                .put(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content");
    }

    // ========== Delete Package Tests ==========

    @Test
    public void testDeletePackage_Success() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("nsUri", TEST_PACKAGE_NSURI)
                .request()
                .delete();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
    }

    @Test
    public void testDeletePackage_ReadOnlyStage() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path("readonly-stage")
                .queryParam("nsUri", TEST_PACKAGE_NSURI)
                .request()
                .delete();

        assertEquals(403, response.getStatus(), "Should return HTTP 403 Forbidden");
    }

    @Test
    public void testDeletePackage_NotFound() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("nsUri", "http://non-existent.com/schema/1.0")
                .request()
                .delete();

        assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content");
    }

    // ========== Transition Tests ==========

    @Test
    public void testTransitionPackage_Success() throws Exception {
    	StageTransitionRequest transition = RestFactory.eINSTANCE.createStageTransitionRequest();
        transition.setObjectId(TEST_PACKAGE_NSURI);
        transition.setTargetStage(TEST_STAGE_APPROVED);

        String xmiContent = TestHelper.serializeToXMI(transition, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .path("actions")
                .path("transition")
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        System.out.println("DEBUG testTransitionPackage_Success - Response status: " + response.getStatus());
        String responseContent = response.readEntity(String.class);
        System.out.println("DEBUG testTransitionPackage_Success - Response content: " + responseContent);

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
    }

    @Test
    public void testTransitionPackage_InvalidTransition() throws Exception {
    	StageTransitionRequest transition = RestFactory.eINSTANCE.createStageTransitionRequest();
        transition.setObjectId(TEST_PACKAGE_NSURI);
        transition.setTargetStage(TEST_STAGE_RELEASE); // Invalid: skipping approved stage

        String xmiContent = TestHelper.serializeToXMI(transition, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .path("actions")
                .path("transition")
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(400, response.getStatus(), "Should return HTTP 400 Bad Request");
    }

    @Test
    public void testTransitionPackage_NotFound() throws Exception {
    	StageTransitionRequest transition = RestFactory.eINSTANCE.createStageTransitionRequest();
        transition.setObjectId("http://non-existent.com/schema/1.0");
        transition.setTargetStage(TEST_STAGE_APPROVED);

        String xmiContent = TestHelper.serializeToXMI(transition, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .path("actions")
                .path("transition")
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content");
    }

    // ========== List Packages By Name Tests ==========

    @Test
    public void testListPackagesInStageByName_ExactMatch() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("name", TEST_PACKAGE_NAME)
                .request("application/json")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
        assertTrue(responseContent.contains(TEST_PACKAGE_NAME), "Response should contain the package name");
    }

    @Test
    public void testListPackagesInStageByName_WildcardMatch() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
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
    public void testListPackagesInStageByName_PartialWildcardMatch() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("name", "TestSchema*")
                .request("application/json")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
    }

    @Test
    public void testListPackagesInStageByName_NotFound() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("name", "NonExistentPackage")
                .request("application/json")
                .get();

        assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content when no packages match");
    }

    @Test
    public void testListPackagesInStageByName_DifferentPackage() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("name", "SensorModel")
                .request("application/json")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("SensorModel"), "Response should contain SensorModel");
    }

    @Test
    public void testListPackagesInStageByName_CombinedWithNsUri() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("nsUri", TEST_PACKAGE_NSURI)
                .queryParam("name", TEST_PACKAGE_NAME)
                .request("application/json")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
    }

    @Test
    public void testListPackagesInStageByName_PrefixWildcard() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("name", "SensorModel*")
                .request("application/json")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("SensorModel"), "Response should contain SensorModel");
    }

    @Test
    public void testListPackagesInStageByName_DifferentStage() {
        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_APPROVED)
                .queryParam("name", TEST_PACKAGE_NAME)
                .request("application/json")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
    }

  

}
