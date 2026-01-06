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
import java.util.Base64;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.model.scope.Scope;
import org.eclipse.fennec.model.atlas.model.scope.ScopeFactory;
import org.eclipse.fennec.model.atlas.model.scope.StageTransition;
import org.eclipse.fennec.model.atlas.rest.tests.helper.ResourceAware;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestHelper;
import org.eclipse.fennec.model.atlas.scope.ScopeCollector;
import org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService;
import org.gecko.emf.osgi.annotation.require.RequireEMF;
import org.gecko.emf.rest.annotations.RequireEMFMessageBodyReaderWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
    private MockScopeCollector mockScopeCollector;
    private ServiceRegistration<ScopeCollector> mockScopeCollectorRegistration;

    @BeforeEach
    public void setup(@InjectBundleContext BundleContext context) throws Exception {
        // Setup REST client
        restClient = clientBuilder.build();

        // Create and register mock ScopeCollector
        mockScopeCollector = new MockScopeCollector();

        Dictionary<String, Object> serviceProps = new Hashtable<>();
        serviceProps.put("service.ranking", Integer.MAX_VALUE);

        mockScopeCollectorRegistration = context.registerService(
                ScopeCollector.class,
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

        assertEquals(404, response.getStatus(), "Should return HTTP 404 Not Found");
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

        assertEquals(201, response.getStatus(), "Should return HTTP 201 OK");
        assertNotNull(responseContent, "Should return content");
    }

    @Test
    @Disabled
    public void testCreatePackage_Conflict() throws Exception {
        EPackage testPackage = TestHelper.createTestEPackage("http://existing.com/schema/1.0", "ExistingSchema", "existing");
        String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("nsUri", "http://existing.com/schema/1.0")
                .queryParam("name", "ExistingSchema")
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(409, response.getStatus(), "Should return HTTP 409 Conflict");
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

        assertEquals(404, response.getStatus(), "Should return HTTP 404 Not Found");
    }

    @Test
    public void testCreatePackage_WithOverrideSuccess() throws Exception {
        // Use an existing package URI
        EPackage updatedPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, "UpdatedTestSchema", "upd");
        String xmiContent = TestHelper.serializeToXMI(updatedPackage, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("nsUri", TEST_PACKAGE_NSURI)
                .queryParam("name", "UpdatedTestSchema")
                .queryParam("version", "1.1.0")
                .queryParam("override", true)
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK when override is true and package exists");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return updated metadata");
        assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
    }

    @Test
    public void testCreatePackage_WithOverrideFalseConflict() throws Exception {
        // Use an existing package URI with override=false
        EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, "test");
        String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("nsUri", TEST_PACKAGE_NSURI)
                .queryParam("name", TEST_PACKAGE_NAME)
                .queryParam("version", "1.1.0")
                .queryParam("override", "false")
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(409, response.getStatus(), "Should return HTTP 409 Conflict when override is false and package exists");
    }

    @Test
    public void testCreatePackage_WithOverrideNewPackage() throws Exception {
        // Use a non-existing package URI with override=true (should create new)
        EPackage newPackage = TestHelper.createTestEPackage("http://non-existent.com/schema/1.0", "new-package", "new");
        String xmiContent = TestHelper.serializeToXMI(newPackage, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("nsUri", "http://non-existent.com/schema/1.0")
                .queryParam("name", "new-package")
                .queryParam("version", "1.0.0")
                .queryParam("override", "true")
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(201, response.getStatus(), "Should return HTTP 201 Created when override is true and package doesn't exist");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return metadata");
    }

    // ========== Version Validation Tests ==========

    @Test
    public void testCreatePackage_InvalidVersionFormat() throws Exception {
        EPackage testPackage = TestHelper.createTestEPackage("http://test.com/schema/1.0", "TestPackage", "test");
        String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("nsUri", "http://test.com/schema/1.0")
                .queryParam("name", "TestPackage")
                .queryParam("version", "invalid-version-format")
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(400, response.getStatus(), "Should return HTTP 400 Bad Request for invalid version format");

        String responseContent = response.readEntity(String.class);
        assertTrue(responseContent.contains("Invalid version"), "Error message should mention invalid version");
    }

    @Test
    public void testCreatePackage_VersionIncompatibleMajor() throws Exception {
        // URI has version 2.0, param has version 1.0 (different major)
        EPackage testPackage = TestHelper.createTestEPackage("http://test.com/schema/2.0", "TestPackage", "test");
        String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("nsUri", "http://test.com/schema/2.0")
                .queryParam("name", "TestPackage")
                .queryParam("version", "1.0.0")
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(400, response.getStatus(), "Should return HTTP 400 Bad Request for incompatible major version");

        String responseContent = response.readEntity(String.class);
        assertTrue(responseContent.contains("not compatible"), "Error message should mention version incompatibility");
    }

    @Test
    public void testCreatePackage_VersionIncompatibleLowerUri() throws Exception {
        // URI has version 1.0.0, param has version 1.5.0 (URI version is lower)
        EPackage testPackage = TestHelper.createTestEPackage("http://test.com/schema/1.0.0", "TestPackage", "test");
        String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("nsUri", "http://test.com/schema/1.0.0")
                .queryParam("name", "TestPackage")
                .queryParam("version", "1.5.0")
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(400, response.getStatus(), "Should return HTTP 400 Bad Request when URI version is lower than param");

        String responseContent = response.readEntity(String.class);
        assertTrue(responseContent.contains("not compatible"), "Error message should mention version incompatibility");
    }

    @Test
    public void testCreatePackage_VersionExtractedFromUri() throws Exception {
        // No version param provided, should extract from URI
        EPackage testPackage = TestHelper.createTestEPackage("http://test.com/schema/1.5.3", "TestPackage", "test");
        String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("nsUri", "http://test.com/schema/1.5.3")
                .queryParam("name", "TestPackage")
                // No version parameter
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(201, response.getStatus(), "Should return HTTP 201 Created and extract version from URI");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return metadata");
        // The version should have been extracted from URI
    }

    @Test
    public void testCreatePackage_VersionCompatibleSameMajor() throws Exception {
        // URI has version 1.5.0, param has version 1.2.0 (compatible - same major, URI >= param)
        EPackage testPackage = TestHelper.createTestEPackage("http://test.com/schema/1.5.0", "TestPackage", "test");
        String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("nsUri", "http://test.com/schema/1.5.0")
                .queryParam("name", "TestPackage")
                .queryParam("version", "1.2.0")
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(201, response.getStatus(), "Should return HTTP 201 Created for compatible versions");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return metadata");
    }

    @Test
    public void testCreatePackage_VersionCompatibleExactMatch() throws Exception {
        // URI and param have exact same version
        EPackage testPackage = TestHelper.createTestEPackage("http://test.com/schema/1.2.3", "TestPackage", "test");
        String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("nsUri", "http://test.com/schema/1.2.3")
                .queryParam("name", "TestPackage")
                .queryParam("version", "1.2.3")
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(201, response.getStatus(), "Should return HTTP 201 Created for exact version match");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return metadata");
    }

    // ========== NsUri Validation Tests ==========

    @Test
    public void testCreatePackage_NsUriMismatch() throws Exception {
        // Query param nsUri doesn't match EPackage's actual nsURI
        EPackage testPackage = TestHelper.createTestEPackage("http://actual.com/schema/1.0", "TestPackage", "test");
        String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("nsUri", "http://wrong.com/schema/1.0") // Mismatch!
                .queryParam("name", "TestPackage")
                .queryParam("version", "1.0.0")
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(400, response.getStatus(), "Should return HTTP 400 Bad Request for nsUri mismatch");

        String responseContent = response.readEntity(String.class);
        assertTrue(responseContent.contains("does not match"), "Error message should mention nsUri mismatch");
    }

    @Test
    public void testCreatePackage_NoNsUriInEPackage() throws Exception {
        // EPackage doesn't have nsURI set
        EPackage testPackage = TestHelper.createTestEPackage(null, "TestPackage", "test");
        testPackage.setNsURI(null); // Explicitly set to null
        String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                .queryParam("nsUri", "http://test.com/schema/1.0")
                .queryParam("name", "TestPackage")
                .queryParam("version", "1.0.0")
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(400, response.getStatus(), "Should return HTTP 400 Bad Request when EPackage has no nsURI");

        String responseContent = response.readEntity(String.class);
        assertTrue(responseContent.contains("non-empty nsURI"), "Error message should mention missing nsURI");
    }

    @Test
    public void testCreatePackage_NoNsUriParam() throws Exception {
        // No nsUri query param provided, should use EPackage's nsURI
        EPackage testPackage = TestHelper.createTestEPackage("http://test.com/schema/1.0", "TestPackage", "test");
        String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

        Response response = restClient
                .target(BASE_URL)
                .path(TEST_SCOPE_NAME)
                .path("schema")
                .path("stages")
                .path(TEST_STAGE_DRAFT)
                // No nsUri parameter
                .queryParam("name", "TestPackage")
                .queryParam("version", "1.0.0")
                .request("application/xmi")
                .post(Entity.entity(xmiContent, "application/xmi"));

        assertEquals(201, response.getStatus(), "Should return HTTP 201 Created when nsUri is taken from EPackage");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return metadata");
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
        StageTransition transition = ScopeFactory.eINSTANCE.createStageTransition();
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
        StageTransition transition = ScopeFactory.eINSTANCE.createStageTransition();
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
        StageTransition transition = ScopeFactory.eINSTANCE.createStageTransition();
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
                .queryParam("name", "Sensor*")
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
            return scope;
        }
    }

    /**
     * Mock implementation of EObjectWorkflowService for testing.
     */
    public static class MockEObjectWorkflowService implements EObjectWorkflowService<EPackage> {

        @Override
        public Promise<ObjectMetadata> uploadToStageForRegistry(String stage, String registry, EPackage object, ObjectMetadata metadata) {
            metadata.setRole(stage);
            metadata.setRegistry(registry);
            metadata.setUploadTime(Instant.now());
            return Promises.resolved(metadata);
        }

        @Override
        public ObjectMetadata getFromStageForRegistry(String stage, String registry, String objectId) {
            String decodedId = new String(Base64.getUrlDecoder().decode(objectId));

            // List of existing packages that should return metadata
            if (decodedId.equals(TEST_PACKAGE_NSURI) ||
                decodedId.equals("http://existing.com/schema/1.0") ||
                decodedId.equals("http://sensor.example.com/model/1.0")) {
                ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
                metadata.setObjectId(objectId);
                metadata.setRole(stage);
                metadata.setRegistry(registry);
                metadata.setUploadTime(Instant.now());
                metadata.setIsReadOnly(false);
                return metadata;
            }

            // All other packages don't exist (return null)
            return null;
        }

        @Override
        public ObjectMetadata getFromFinalStageForRegistry(String registry, String objectId) {
            return getFromStageForRegistry(TEST_STAGE_RELEASE, registry, objectId);
        }

        @Override
        public EPackage getContentFromStageForRegistry(String stage, String registry, String objectId) {
            String decodedId = new String(Base64.getUrlDecoder().decode(objectId));

            // Only return content for existing packages
            if (decodedId.equals(TEST_PACKAGE_NSURI)) {
                return TestHelper.createTestEPackage(decodedId, "TestPackage", "test");
            }
            if (decodedId.equals("http://existing.com/schema/1.0")) {
                return TestHelper.createTestEPackage(decodedId, "ExistingPackage", "existing");
            }
            if (decodedId.equals("http://sensor.example.com/model/1.0")) {
                return TestHelper.createTestEPackage(decodedId, "SensorModel", "sensor");
            }

            // All other packages don't exist
            return null;
        }

        @Override
        public Promise<ObjectMetadata> updateInStageForRegistry(String stage, String registry, EPackage updatedObject, String objectId, String updatedVersion) {
            String decodedId = new String(Base64.getUrlDecoder().decode(objectId));

            // Only allow updates for existing packages
            if (decodedId.equals(TEST_PACKAGE_NSURI) ||
                decodedId.equals("http://existing.com/schema/1.0") ||
                decodedId.equals("http://sensor.example.com/model/1.0")) {
                ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
                metadata.setObjectId(objectId);
                metadata.setRole(stage);
                metadata.setRegistry(registry);
                metadata.setVersion(updatedVersion);
                metadata.setLastChangeTime(Instant.now());
                return Promises.resolved(metadata);
            }

            // Package doesn't exist, return null
            return Promises.resolved(null);
        }

        @Override
        public Promise<Boolean> deleteFromStageForRegistry(String stage, String registry, String objectId) {
            String decodedId = new String(Base64.getUrlDecoder().decode(objectId));

            // Only allow deletion of existing packages
            return Promises.resolved(
                decodedId.equals(TEST_PACKAGE_NSURI) ||
                decodedId.equals("http://existing.com/schema/1.0") ||
                decodedId.equals("http://sensor.example.com/model/1.0")
            );
        }

        @Override
        public List<ObjectMetadata> listInStageForRegistry(String stage, String registry) {
            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            String encodedNsUri = Base64.getUrlEncoder().encodeToString(TEST_PACKAGE_NSURI.getBytes());
            metadata.setObjectId(encodedNsUri);
            metadata.setRole(stage);
            metadata.setRegistry(registry);
            metadata.setObjectName(TEST_PACKAGE_NAME);
            metadata.setUploadTime(Instant.now());
            return List.of(metadata);
        }

        @Override
        public List<ObjectMetadata> listInFinalStageForRegistry(String registry) {
            return listInStageForRegistry(TEST_STAGE_RELEASE, registry);
        }

        @Override
        public ObjectMetadata transitionToStageForRegistry(String objectId, String fromStage, String toStage, String registry) {
            String decodedId = new String(Base64.getUrlDecoder().decode(objectId));

            // Only allow transitions for existing packages
            if (decodedId.equals(TEST_PACKAGE_NSURI) ||
                decodedId.equals("http://existing.com/schema/1.0") ||
                decodedId.equals("http://sensor.example.com/model/1.0")) {
                ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
                metadata.setObjectId(objectId);
                metadata.setRole(toStage);
                metadata.setRegistry(registry);
                metadata.setLastChangeTime(Instant.now());
                return metadata;
            }

            // Package doesn't exist
            return null;
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

		/*
		 * (non-Javadoc)
		 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService#listInStageByName(java.lang.String, java.lang.String)
		 */
		@Override
		public List<ObjectMetadata> listInStageForRegistryByName(String stage, String registry, String name) {
			// Support wildcard search with * character (only trailing wildcards like "Prefix*")
			boolean isWildcard = name.contains("*");
			String nameFilter = isWildcard ? name.replace("*", "") : name;

			// Create test metadata that matches the filter
			if ("TestSchema".equals(name) || (isWildcard && TEST_PACKAGE_NAME.startsWith(nameFilter))) {
				ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
				String encodedNsUri = Base64.getUrlEncoder().encodeToString(TEST_PACKAGE_NSURI.getBytes());
				metadata.setObjectId(encodedNsUri);
				metadata.setRole(stage);
				metadata.setRegistry(registry);
				metadata.setObjectName(TEST_PACKAGE_NAME);
				metadata.setUploadTime(Instant.now());
				return List.of(metadata);
			}

			// Match for Test* wildcard
			if (isWildcard && TEST_PACKAGE_NAME.startsWith(nameFilter)) {
				ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
				String encodedNsUri = Base64.getUrlEncoder().encodeToString(TEST_PACKAGE_NSURI.getBytes());
				metadata.setObjectId(encodedNsUri);
				metadata.setRole(stage);
				metadata.setRegistry(registry);
				metadata.setObjectName(TEST_PACKAGE_NAME);
				metadata.setUploadTime(Instant.now());
				return List.of(metadata);
			}

			// If looking for SensorModel, return a matching object
			if ("SensorModel".equals(name) || (isWildcard && "SensorModel".startsWith(nameFilter))) {
				ObjectMetadata sensorMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
				String encodedSensorNsUri = Base64.getUrlEncoder().encodeToString("http://test.example.com/sensor/1.0".getBytes());
				sensorMetadata.setObjectId(encodedSensorNsUri);
				sensorMetadata.setRole(stage);
				sensorMetadata.setRegistry(registry);
				sensorMetadata.setObjectName("SensorModel");
				sensorMetadata.setUploadTime(Instant.now());
				return List.of(sensorMetadata);
			}

			// Return empty list if no match
			return List.of();
		}
    }
}
