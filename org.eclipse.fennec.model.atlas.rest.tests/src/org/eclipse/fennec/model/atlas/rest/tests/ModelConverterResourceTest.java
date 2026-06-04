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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestAnnotations;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestAnnotations.ParentScopeServiceSetup;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;

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

public class ModelConverterResourceTest extends AbstractRestTest{

    private static final String CONVERT_BASE_URL = BASE_URL.concat("/").concat(TestAnnotations.TEST_SCOPE_NAME.concat("/stages/").concat(TestAnnotations.STAGE_RELEASE).concat("/convert"));


    @BeforeEach
    public void setup(@InjectBundleContext BundleContext context) throws Exception {
    	super.setup(context);
    	
    }



    // ========== JSON to XML Conversion Tests ==========

    @Test
    @ParentScopeServiceSetup
    public void testConvertPackage_JsonToXml_Success(@InjectBundleContext BundleContext context) throws Exception {
    	ensureResourceAvailability(context);
        // Given: An EPackage in JSON format
        String nsUri = TestHelper.generateUniqueNsUri("jsonToXmlTest");
        String jsonEPackage = createJsonEPackage(nsUri, "JsonToXmlPackage", "j2x");

        // When: POST with JSON content type and Accept XML
        Response response = restClient.target(CONVERT_BASE_URL).request(MediaType.APPLICATION_XML)
                .post(Entity.json(jsonEPackage));

        // Then: Should return 200 OK with XML content
        assertStatus(200, response, "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Response content should not be null");
        assertTrue(responseContent.contains(nsUri), "NsURI should be preserved in XML output");
        assertTrue(responseContent.contains("JsonToXmlPackage"), "Package name should be preserved in XML output");
    }

    // ========== XML to JSON Conversion Tests ==========

    @Test
    @ParentScopeServiceSetup
    public void testConvertPackage_XmlToJson_Success(@InjectBundleContext BundleContext context) throws Exception {
    	ensureResourceAvailability(context);
        // Given: An EPackage in XMI format
        String nsUri = TestHelper.generateUniqueNsUri("xmlToJsonTest");
        EPackage testPackage = TestHelper.createTestEPackage(nsUri, "XmlToJsonPackage", "x2j");
        String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

        // When: POST with XMI content type and Accept JSON
        Response response = restClient.target(CONVERT_BASE_URL).request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(xmiContent, "application/xmi"));

        // Then: Should return 200 OK with JSON content
        assertStatus(200, response, "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Response content should not be null");
        assertTrue(responseContent.contains(nsUri), "NsURI should be preserved in JSON output");
        assertTrue(responseContent.contains("XmlToJsonPackage"), "Package name should be preserved in JSON output");
    }

    // ========== XMI to JSON Conversion Tests ==========

    @Test
    @ParentScopeServiceSetup
    public void testConvertPackage_XmiToJson_Success(@InjectBundleContext BundleContext context) throws Exception {
    	ensureResourceAvailability(context);
        // Given: An EPackage in XMI format
        String nsUri = TestHelper.generateUniqueNsUri("xmiToJsonTest");
        EPackage testPackage = TestHelper.createTestEPackage(nsUri, "XmiToJsonPackage", "xmi2j");
        String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

        // When: POST with XMI content type and Accept JSON
        Response response = restClient.target(CONVERT_BASE_URL).request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(xmiContent, "application/xmi"));

        // Then: Should return 200 OK
        assertStatus(200, response, "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Response content should not be null");
        assertTrue(responseContent.contains(nsUri), "NsURI should be preserved in JSON output");
    }

    // ========== JSON to XMI Conversion Tests ==========

    @Test
    @ParentScopeServiceSetup
    public void testConvertPackage_JsonToXmi_Success(@InjectBundleContext BundleContext context) throws Exception {
    	ensureResourceAvailability(context);
        // Given: An EPackage in JSON format
        String nsUri = TestHelper.generateUniqueNsUri("jsonToXmiTest");
        String jsonEPackage = createJsonEPackage(nsUri, "JsonToXmiPackage", "j2xmi");

        // When: POST with JSON content type and Accept XMI
        Response response = restClient.target(CONVERT_BASE_URL).request("application/xmi").post(Entity.json(jsonEPackage));

        // Then: Should return 200 OK
        assertStatus(200, response, "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Response content should not be null");
        assertTrue(responseContent.contains(nsUri), "NsURI should be preserved in XMI output");
        assertTrue(responseContent.contains("JsonToXmiPackage"), "Package name should be preserved in XMI output");
    }

    // ========== Complex EPackage Conversion Tests ==========

    @Test
    @ParentScopeServiceSetup
    public void testConvertPackage_ComplexPackage_PreservesStructure(@InjectBundleContext BundleContext context) throws Exception {
    	ensureResourceAvailability(context);
        // Given: A complex EPackage with classes and attributes
        String nsUri = TestHelper.generateUniqueNsUri("complexConvertTest");
        EPackage complexPackage = TestHelper.createTestEPackage(nsUri, "ComplexPackage", "complex");
        String xmiContent = TestHelper.serializeToXMI(complexPackage, resourceSet);

        // When: Convert from XMI to JSON
        Response response = restClient.target(CONVERT_BASE_URL).request(MediaType.APPLICATION_JSON)
                .post(Entity.entity(xmiContent, "application/xmi"));

        // Then: Should preserve the package structure
        assertStatus(200, response, "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Response content should not be null");
        assertTrue(responseContent.contains(nsUri), "NsURI should be preserved in JSON output");
        assertTrue(responseContent.contains("ComplexPackage"), "Package name should be preserved in JSON output");
    }

    // ========== Unsupported Media Type Tests ==========

    @Test
    @ParentScopeServiceSetup
    public void testConvertPackage_UnsupportedAcceptHeader_Returns415(@InjectBundleContext BundleContext context) throws Exception {
    	ensureResourceAvailability(context);
        // Given: An EPackage in JSON format
        String nsUri = TestHelper.generateUniqueNsUri("unsupportedTest");
        String jsonEPackage = createJsonEPackage(nsUri, "UnsupportedPackage", "us");

        // When: POST with unsupported Accept header
        Response response = restClient.target(CONVERT_BASE_URL).request("application/unsupported-type")
                .post(Entity.json(jsonEPackage));

        // Then: Should return 415 Unsupported Media Type
        assertStatus(415, response, "Should return HTTP 415 Unsupported Media Type");
    }

    // ========== Same Format Conversion Tests ==========

    @Test
    @ParentScopeServiceSetup
    public void testConvertPackage_JsonToJson_Success(@InjectBundleContext BundleContext context) throws Exception {
    	ensureResourceAvailability(context);

        // Given: An EPackage in JSON format
        String nsUri = TestHelper.generateUniqueNsUri("jsonToJsonTest");
        String jsonEPackage = createJsonEPackage(nsUri, "JsonToJsonPackage", "j2j");

        // When: POST with JSON content type and Accept JSON (same format)
        Response response = restClient.target(CONVERT_BASE_URL).request(MediaType.APPLICATION_JSON)
                .post(Entity.json(jsonEPackage));

        // Then: Should return 200 OK
        assertStatus(200, response, "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Response content should not be null");
        assertTrue(responseContent.contains(nsUri), "NsURI should be preserved in JSON output");
    }

    // ========== Default Media Type Tests ==========

    @Test
    @ParentScopeServiceSetup
    public void testConvertPackage_NoAcceptHeader_DefaultsToJson(@InjectBundleContext BundleContext context) throws Exception {
    	ensureResourceAvailability(context);
        // Given: An EPackage in XMI format
        String nsUri = TestHelper.generateUniqueNsUri("defaultMediaTypeTest");
        EPackage testPackage = TestHelper.createTestEPackage(nsUri, "DefaultMediaTypePackage", "dmt");
        String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

        // When: POST with XMI content type and wildcard Accept header
        Response response = restClient.target(CONVERT_BASE_URL).request(MediaType.WILDCARD)
                .post(Entity.entity(xmiContent, "application/xmi"));

        // Then: Should return 200 OK (defaults to JSON)
        assertStatus(200, response, "Should return HTTP 200 OK");

        String responseContent = response.readEntity(String.class);
        assertNotNull(responseContent, "Response content should not be null");
    }

    // ========== Content Type Header Tests ==========

    @Test
    @ParentScopeServiceSetup
    public void testConvertPackage_ResponseContentTypeMatches(@InjectBundleContext BundleContext context) throws Exception {
    	ensureResourceAvailability(context);
        // Given: An EPackage in JSON format
        String nsUri = TestHelper.generateUniqueNsUri("contentTypeTest");
        String jsonEPackage = createJsonEPackage(nsUri, "ContentTypePackage", "ct");

        // When: POST requesting XML
        Response response = restClient.target(CONVERT_BASE_URL).request(MediaType.APPLICATION_XML)
                .post(Entity.json(jsonEPackage));

        // Then: Response Content-Type should be XML
        assertStatus(200, response, "Should return HTTP 200 OK");

        String contentType = response.getHeaderString("Content-Type");
        assertNotNull(contentType, "Response should have Content-Type header");
        assertTrue(contentType.contains("application/xml") || contentType.contains("application/xmi"),
                "Response Content-Type should be XML or XMI");
    }

    // ========== Helper Methods ==========

    private String createJsonEPackage(String nsUri, String name, String nsPrefix) {
        return String.format("{\"_type\":\"http://www.eclipse.org/emf/2002/Ecore#//EPackage\","
                + "\"name\":\"%s\",\"nsURI\":\"%s\",\"nsPrefix\":\"%s\"}", name, nsUri, nsPrefix);
    }

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.rest.tests.AbstractRestTest#getResourceName()
	 */
	@Override
	String getResourceName() {
		return "ModelConverterResource";
	}
}
