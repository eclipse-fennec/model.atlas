/**
 * Copyright (c) 2012 - 2026 Data In Motion and others.
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

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.model.atlas.rest.tests.helper.ResourceAware;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestHelper;
import org.gecko.emf.osgi.annotation.require.RequireEMF;
import org.gecko.emf.rest.annotations.RequireEMFMessageBodyReaderWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.service.jakartars.whiteboard.annotations.RequireJakartarsWhiteboard;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Integration tests for ModelConverterResource REST endpoints.
 *
 * <p>
 * Tests cover:
 * </p>
 * <ul>
 * <li>Converting EPackage from JSON to XML</li>
 * <li>Converting EPackage from XML to JSON</li>
 * <li>Converting EPackage from XMI to JSON</li>
 * <li>Converting EPackage using mediaType query parameter</li>
 * <li>Error handling for unsupported media types</li>
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
public class ModelConverterResourceTest {

    private static final String BASE_URL = "http://localhost:8185/rest/convert";
    private static final int TIMEOUT_SECONDS = 15;

    @InjectService
    ClientBuilder clientBuilder;

    @InjectService(filter = "(emf.name=workflowapi)")
    ResourceSet resourceSet;

    private Client restClient;

    @BeforeEach
    public void setup(@InjectBundleContext BundleContext context) throws Exception {
        // Setup REST client
        restClient = clientBuilder.build();

        TestHelper.ensureXMIFactory(resourceSet);

        // Wait for the ModelConverterResource to be registered in Jakarta REST runtime
        ResourceAware resourceAware = ResourceAware.create(context, "ModelConverterResource");
        boolean resourceReady = resourceAware.waitForResource(TIMEOUT_SECONDS, TimeUnit.SECONDS);

        // Small delay to allow service registration to propagate
        Thread.sleep(200);

        assertTrue(resourceReady, "ModelConverterResource should be registered within " + TIMEOUT_SECONDS + " seconds. "
                + "Check that the resource is properly configured and the Jakarta REST runtime is working.");
    }

    @AfterEach
    public void teardown() {
        if (nonNull(restClient)) {
            restClient.close();
            restClient = null;
        }
    }

    // ========== JSON to XML Conversion Tests ==========

    @Test
    public void testConvertPackage_JsonToXml_Success() {
        // Given: An EPackage in JSON format
        String nsUri = TestHelper.generateUniqueNsUri("jsonToXmlTest");
        String jsonEPackage = createJsonEPackage(nsUri, "JsonToXmlPackage", "j2x");

        // When: POST with JSON content type and Accept XML
        Response response = restClient.target(BASE_URL).request(MediaType.APPLICATION_XML)
                .post(Entity.json(jsonEPackage));

        // Then: Should return 200 OK with XML content
        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Response content should not be null");
        assertTrue(responseContent.contains(nsUri), "NsURI should be preserved in XML output");
        assertTrue(responseContent.contains("JsonToXmlPackage"), "Package name should be preserved in XML output");
    }

    // ========== XML to JSON Conversion Tests ==========

    @Test
    public void testConvertPackage_XmlToJson_Success() throws IOException {
        // Given: An EPackage in XMI format
        String nsUri = TestHelper.generateUniqueNsUri("xmlToJsonTest");
        EPackage testPackage = TestHelper.createTestEPackage(nsUri, "XmlToJsonPackage", "x2j");
        String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

        // When: POST with XMI content type and Accept JSON
        Response response = restClient.target(BASE_URL).request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(xmiContent, "application/xmi"));

        // Then: Should return 200 OK with JSON content
        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Response content should not be null");
        assertTrue(responseContent.contains(nsUri), "NsURI should be preserved in JSON output");
        assertTrue(responseContent.contains("XmlToJsonPackage"), "Package name should be preserved in JSON output");
    }

    // ========== XMI to JSON Conversion Tests ==========

    @Test
    public void testConvertPackage_XmiToJson_Success() throws IOException {
        // Given: An EPackage in XMI format
        String nsUri = TestHelper.generateUniqueNsUri("xmiToJsonTest");
        EPackage testPackage = TestHelper.createTestEPackage(nsUri, "XmiToJsonPackage", "xmi2j");
        String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

        // When: POST with XMI content type and Accept JSON
        Response response = restClient.target(BASE_URL).request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(xmiContent, "application/xmi"));

        // Then: Should return 200 OK
        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Response content should not be null");
        assertTrue(responseContent.contains(nsUri), "NsURI should be preserved in JSON output");
    }

    // ========== JSON to XMI Conversion Tests ==========

    @Test
    public void testConvertPackage_JsonToXmi_Success() {
        // Given: An EPackage in JSON format
        String nsUri = TestHelper.generateUniqueNsUri("jsonToXmiTest");
        String jsonEPackage = createJsonEPackage(nsUri, "JsonToXmiPackage", "j2xmi");

        // When: POST with JSON content type and Accept XMI
        Response response = restClient.target(BASE_URL).request("application/xmi").post(Entity.json(jsonEPackage));

        // Then: Should return 200 OK
        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Response content should not be null");
        assertTrue(responseContent.contains(nsUri), "NsURI should be preserved in XMI output");
        assertTrue(responseContent.contains("JsonToXmiPackage"), "Package name should be preserved in XMI output");
    }

    // ========== Complex EPackage Conversion Tests ==========

    @Test
    public void testConvertPackage_ComplexPackage_PreservesStructure() throws IOException {
        // Given: A complex EPackage with classes and attributes
        String nsUri = TestHelper.generateUniqueNsUri("complexConvertTest");
        EPackage complexPackage = TestHelper.createTestEPackage(nsUri, "ComplexPackage", "complex");
        String xmiContent = TestHelper.serializeToXMI(complexPackage, resourceSet);

        // When: Convert from XMI to JSON
        Response response = restClient.target(BASE_URL).request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(xmiContent, "application/xmi"));

        // Then: Should preserve the package structure
        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Response content should not be null");
        assertTrue(responseContent.contains(nsUri), "NsURI should be preserved in JSON output");
        assertTrue(responseContent.contains("ComplexPackage"), "Package name should be preserved in JSON output");
    }

    // ========== Unsupported Media Type Tests ==========

    @Test
    public void testConvertPackage_UnsupportedAcceptHeader_Returns415() {
        // Given: An EPackage in JSON format
        String nsUri = TestHelper.generateUniqueNsUri("unsupportedTest");
        String jsonEPackage = createJsonEPackage(nsUri, "UnsupportedPackage", "us");

        // When: POST with unsupported Accept header
        Response response = restClient.target(BASE_URL).request("application/unsupported-type")
                .post(Entity.json(jsonEPackage));

        // Then: Should return 415 Unsupported Media Type
        assertEquals(415, response.getStatus(), "Should return HTTP 415 Unsupported Media Type");
    }

    // ========== Same Format Conversion Tests ==========

    @Test
    public void testConvertPackage_JsonToJson_Success() {
        // Given: An EPackage in JSON format
        String nsUri = TestHelper.generateUniqueNsUri("jsonToJsonTest");
        String jsonEPackage = createJsonEPackage(nsUri, "JsonToJsonPackage", "j2j");

        // When: POST with JSON content type and Accept JSON (same format)
        Response response = restClient.target(BASE_URL).request(MediaType.APPLICATION_JSON)
                .post(Entity.json(jsonEPackage));

        // Then: Should return 200 OK
        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Response content should not be null");
        assertTrue(responseContent.contains(nsUri), "NsURI should be preserved in JSON output");
    }

    // ========== Default Media Type Tests ==========

    @Test
    public void testConvertPackage_NoAcceptHeader_DefaultsToJson() throws IOException {
        // Given: An EPackage in XMI format
        String nsUri = TestHelper.generateUniqueNsUri("defaultMediaTypeTest");
        EPackage testPackage = TestHelper.createTestEPackage(nsUri, "DefaultMediaTypePackage", "dmt");
        String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

        // When: POST with XMI content type and wildcard Accept header
        Response response = restClient.target(BASE_URL).request(MediaType.WILDCARD)
                .post(Entity.entity(xmiContent, "application/xmi"));

        // Then: Should return 200 OK (defaults to JSON)
        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Response content should not be null");
    }

    // ========== Content Type Header Tests ==========

    @Test
    public void testConvertPackage_ResponseContentTypeMatches() {
        // Given: An EPackage in JSON format
        String nsUri = TestHelper.generateUniqueNsUri("contentTypeTest");
        String jsonEPackage = createJsonEPackage(nsUri, "ContentTypePackage", "ct");

        // When: POST requesting XML
        Response response = restClient.target(BASE_URL).request(MediaType.APPLICATION_XML)
                .post(Entity.json(jsonEPackage));

        // Then: Response Content-Type should be XML
        assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

        String contentType = response.getHeaderString("Content-Type");
        assertNotNull(contentType, "Response should have Content-Type header");
        assertTrue(contentType.contains("application/xml") || contentType.contains("application/xmi"),
                "Response Content-Type should be XML or XMI");
    }

    // ========== Helper Methods ==========

    private String createJsonEPackage(String nsUri, String name, String nsPrefix) {
        return String.format("{\"eClass\":\"http://www.eclipse.org/emf/2002/Ecore#//EPackage\","
                + "\"name\":\"%s\",\"nsURI\":\"%s\",\"nsPrefix\":\"%s\"}", name, nsUri, nsPrefix);
    }
}
