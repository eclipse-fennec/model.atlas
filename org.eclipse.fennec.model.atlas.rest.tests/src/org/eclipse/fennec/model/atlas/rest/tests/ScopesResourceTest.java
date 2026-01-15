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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import org.eclipse.fennec.model.atlas.rest.tests.helper.MockTestHelper.MockScopeServiceCollector;
import org.eclipse.fennec.model.atlas.rest.tests.helper.ResourceAware;
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
import jakarta.ws.rs.core.Response;

/**
 * Integration tests for ScopesResource REST endpoints.
 *
 * <p>Tests cover:</p>
 * <ul>
 * <li>Listing all available scopes</li>
 * <li>Retrieving a specific scope by name</li>
 * <li>Error handling for non-existent scopes</li>
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
public class ScopesResourceTest {

    private static final String BASE_URL = "http://localhost:8185/rest";
    private static final String TEST_SCOPE_NAME = "test-scope";

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

        // Wait for the ScopesResource to be registered in Jakarta REST runtime
        ResourceAware resourceAware = ResourceAware.create(context, "ScopesResource");
        boolean resourceReady = resourceAware.waitForResource(15, TimeUnit.SECONDS);

        assertTrue(resourceReady,
                "ScopesResource should be registered within 15 seconds. " +
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

    // ========== List All Scopes Tests ==========

    @Test
    public void testListScopes_Success() {
        Response response = restClient
                .target(BASE_URL)
                .path("scopes")
                .request("application/json")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");
        assertTrue(responseContent.contains("scopes"), "Response should contain scopes field");
        assertTrue(responseContent.contains(TEST_SCOPE_NAME), "Response should contain the test scope name");
    }

    @Test
    public void testListScopes_ContainsScopeDetails() {
        Response response = restClient
                .target(BASE_URL)
                .path("scopes")
                .request("application/json")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");

        // Verify scope structure contains expected fields
        assertTrue(responseContent.contains("name"), "Response should contain name field");
        assertTrue(responseContent.contains("registries"), "Response should contain registries field");
    }

    @Test
    public void testListScopes_ReturnsNonEmptyList() {
        Response response = restClient
                .target(BASE_URL)
                .path("scopes")
                .request("application/json")
                .get();

        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Should return content");

        // Verify scopes list is not empty (mock returns at least one scope)
        assertFalse(responseContent.contains("\"scopes\":[]"), "Scopes list should not be empty");
    }

    // ========== Get Specific Scope Tests ==========

    @Test
    public void testGetScope_NotFound() {
        Response response = restClient
                .target(BASE_URL)
                .path("scopes")
                .path("non-existent-scope")
                .request("application/json")
                .get();

        assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content for non-existent scope");
    }

    @Test
    public void testGetScope_EmptyName() {
        // Note: This tests behavior with an empty path segment
        // The behavior depends on how the REST framework handles empty path params
        Response response = restClient
                .target(BASE_URL)
                .path("scopes")
                .path("")
                .request("application/json")
                .get();

        // Empty path segment typically results in listing all scopes (same as /scopes/)
        int status = response.getStatus();
        assertTrue(status == 200,
                "Empty scope name should return 200 (dwfault to the all scope endpoint)");
    }

    @Test
    public void testGetScope_WithSpecialCharacters() {
        Response response = restClient
                .target(BASE_URL)
                .path("scopes")
                .path("scope-with-special-chars!@#")
                .request("application/json")
                .get();

        assertEquals(204, response.getStatus(),
                "Should return HTTP 204 No Content for non-existent scope with special characters");
    }

    // ========== Content Type Tests ==========

    @Test
    public void testListScopes_AcceptsJsonContentType() {
        Response response = restClient
                .target(BASE_URL)
                .path("scopes")
                .request("application/json")
                .get();

        assertEquals(200, response.getStatus(), "Should accept application/json content type");

        String contentType = response.getHeaderString("Content-Type");
        assertNotNull(contentType, "Response should have Content-Type header");
        assertTrue(contentType.contains("application/json"),
                "Response Content-Type should be application/json");
    }

    @Test
    public void testGetScope_AcceptsJsonContentType() {
        Response response = restClient
                .target(BASE_URL)
                .path("scopes")
                .path(TEST_SCOPE_NAME)
                .request("application/json")
                .get();

        assertEquals(200, response.getStatus(), "Should accept application/json content type");

        String contentType = response.getHeaderString("Content-Type");
        assertNotNull(contentType, "Response should have Content-Type header");
        assertTrue(contentType.contains("application/json"),
                "Response Content-Type should be application/json");
    }

    // ========== Case Sensitivity Tests ==========

    @Test
    public void testGetScope_CaseSensitive() {
        // Test that scope names are case-sensitive
        Response response = restClient
                .target(BASE_URL)
                .path("scopes")
                .path("TEST-SCOPE") // Uppercase version of test-scope
                .request("application/json")
                .get();

        assertEquals(204, response.getStatus(),
                "Scope lookup should be case-sensitive and return 204 for wrong case");
    }

    @Test
    public void testGetScope_MixedCase() {
        Response response = restClient
                .target(BASE_URL)
                .path("scopes")
                .path("Test-Scope") // Mixed case
                .request("application/json")
                .get();

        assertEquals(204, response.getStatus(),
                "Scope lookup should be case-sensitive and return 204 for mixed case");
    }
}
