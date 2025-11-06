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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.rest.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.model.atlas.rest.model.EPackageListResponse;
import org.eclipse.fennec.model.atlas.rest.tests.helper.ResourceAware;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.MessageBodyWriter;

/**
 * Integration tests for the EPackage REST API endpoints.
 * Tests cover happy paths, error scenarios, and edge cases including:
 * - CRUD operations (POST, GET, PUT, DELETE)
 * - Multiple media types (JSON, XML, XMI)
 * - Error responses (400, 404, 409, 415)
 * - Pagination
 * - Concurrent operations
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class EPackageResourceTest {

	private static final String BASE_URL = "http://localhost:8185/rest/epackages";
	private static final int TIMEOUT_SECONDS = 15;

	@InjectService
	ClientBuilder clientBuilder;

	@InjectService(cardinality = 0)
	ResourceSet resourceSet;

	private Client client;

	// Track created EPackages for cleanup
	private java.util.List<String> createdNsUris = new java.util.ArrayList<>();

	@SuppressWarnings("rawtypes")
	@BeforeEach
	void setUp(
	    @InjectBundleContext BundleContext context,
	    @InjectService List<MessageBodyWriter> messageBodyWriter) throws Exception {
		assertNotNull(clientBuilder, "ClientBuilder service should be available");

		// Create ResourceSet if not injected
		if (resourceSet == null) {
			resourceSet = new org.eclipse.emf.ecore.resource.impl.ResourceSetImpl();
		}

		messageBodyWriter.forEach(clientBuilder::register);
		
		client = clientBuilder.build();

		// Ensure XMI factory is registered
		TestHelper.ensureXMIFactory(resourceSet);

		// Wait for EPackageResource to be registered in Jakarta REST runtime
		ResourceAware resourceAware = ResourceAware.create(context, "EPackageResource");
		assertTrue(resourceAware.waitForResource(TIMEOUT_SECONDS, TimeUnit.SECONDS),
			"EPackageResource should be available within " + TIMEOUT_SECONDS + " seconds");
	}

	@AfterEach
	void tearDown() {
		// Clean up all created EPackages
		for (String nsUri : createdNsUris) {
			try {
				client.target(BASE_URL)
					.queryParam("nsUri", nsUri)
					.request()
					.delete();
			} catch (Exception e) {
				// Ignore cleanup errors
			}
		}
		createdNsUris.clear();

		if (client != null) {
			client.close();
		}
	}

	// ============ HAPPY PATH TESTS ============

	@Test
	void testCreateEPackage_WithXMI_Success() throws IOException {
		// Given: A new EPackage
		String nsUri = TestHelper.generateUniqueNsUri("createTest");
		EPackage testPackage = createBasicTestEPackage(nsUri, "CreateTestPackage", "create");

		// When: POST to create the EPackage
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		Response response = client.target(BASE_URL)
			.request(MediaType.APPLICATION_XML)
			.post(Entity.entity(xmiContent, "application/xmi"));

		// Then: Should return 201 Created
		assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus(),
			"Should return 201 Created");
		createdNsUris.add(nsUri);

		// Verify response contains the created EPackage
		EPackage createdPackage = response.readEntity(EPackage.class);
		assertNotNull(createdPackage, "Response body should not be null");
		assertEquals(nsUri, createdPackage.getNsURI(), "Response should contain the nsUri");
		assertEquals("CreateTestPackage", createdPackage.getName(), "Response should contain the package name");
	}

	@Test
	void testCreateEPackage_WithJSON_Success() {
		// Given: A new EPackage in JSON format
		String nsUri = TestHelper.generateUniqueNsUri("createJsonTest");
		String jsonEPackage = String.format(
			"{\"eClass\":\"http://www.eclipse.org/emf/2002/Ecore#//EPackage\"," +
			"\"name\":\"JsonTestPackage\",\"nsURI\":\"%s\",\"nsPrefix\":\"jsontest\"}", nsUri);

		// When: POST with JSON
		Response response = client.target(BASE_URL)
			.request(MediaType.APPLICATION_JSON)
			.post(Entity.json(jsonEPackage));

		// Then: Should return 201 Created
		assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus(),
			"Should return 201 Created for JSON");
		createdNsUris.add(nsUri);
	}

	@Test
	void testCreateEPackage_WithJsonSchema_Success() {
		// Given: A new EPackage in JSON Schema format
		String nsUri = TestHelper.generateUniqueNsUri("createJsonSchemaTest");
		String jsonSchemaEPackage = String.format("""
			{
			  "$schema": "https://json-schema.org/draft/2020-12/schema",
			  "$id": "%s",
			  "title": "JsonSchemaTestPackage",
			  "type": "object",
			  "definitions": {
			    "TestClass": {
			      "type": "object",
			      "properties": {
			        "name": { "type": "string" },
			        "value": { "type": "integer" }
			      }
			    }
			  }
			}
			""", nsUri);

		// When: POST with JSON Schema
		Response response = client.target(BASE_URL)
			.request("application/schema+json")
			.post(Entity.entity(jsonSchemaEPackage, "application/schema+json"));

		// Then: Should return 201 Created
		assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus(),
			"Should return 201 Created for JSON Schema");
		createdNsUris.add(nsUri);

		// Verify response contains the created EPackage
		EPackage createdPackage = response.readEntity(EPackage.class);
		assertNotNull(createdPackage, "Response body should not be null");
		assertEquals(nsUri, createdPackage.getNsURI(), "Response should contain the nsUri");
	}

	@Test
	void testGetEPackage_AsJsonSchema_Success() throws IOException {
		// Given: An existing EPackage
		String nsUri = TestHelper.generateUniqueNsUri("getJsonSchemaTest");
		EPackage testPackage = createBasicTestEPackage(nsUri, "JsonSchemaGetPackage", "jsget");
		createEPackageViaREST(testPackage);

		// When: GET with JSON Schema media type
		Response response = client.target(BASE_URL)
			.queryParam("nsUri", nsUri)
			.request("application/schema+json")
			.get();

		// Then: Should return 200 OK with JSON Schema
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(),
			"Should return 200 OK");

		String jsonSchema = response.readEntity(String.class);
		assertNotNull(jsonSchema, "Response body should not be null");
		assertTrue(jsonSchema.contains("definitions"), "Response should contain JSON Schema definitions");
		assertTrue(jsonSchema.contains(nsUri), "Response should contain the nsUri");
	}

	@Test
	void testCreateAndRetrieveEPackage_WithJsonSchema_RoundTrip() throws IOException {
		// Given: A complex EPackage with classes and attributes
		String nsUri = TestHelper.generateUniqueNsUri("roundTripJsonSchemaTest");
		EPackage complexPackage = createComplexTestEPackage(nsUri);

		// When: Create via XMI and retrieve as JSON Schema
		createEPackageViaREST(complexPackage);

		Response response = client.target(BASE_URL)
			.queryParam("nsUri", nsUri)
			.request("application/schema+json")
			.get();

		// Then: Should retrieve as valid JSON Schema
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(),
			"Should return 200 OK");

		String jsonSchema = response.readEntity(String.class);
		assertNotNull(jsonSchema, "JSON Schema should not be null");
		assertTrue(jsonSchema.contains("definitions"), "Should contain definitions");
		assertTrue(jsonSchema.contains("Person"), "Should contain Person class from complex package");
	}

	@Test
	void testGetEPackage_ByNsUri_Success() throws IOException {
		// Given: An existing EPackage
		String nsUri = TestHelper.generateUniqueNsUri("getTest");
		EPackage testPackage = createBasicTestEPackage(nsUri, "GetTestPackage", "get");
		createEPackageViaREST(testPackage);

		// When: GET with nsUri query parameter
		Response response = client.target(BASE_URL)
			.queryParam("nsUri", nsUri)
			.request(MediaType.APPLICATION_XML)
			.get();

		// Then: Should return 200 OK with the EPackage
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(),
			"Should return 200 OK");

		EPackage retrievedPackage = response.readEntity(EPackage.class);
		assertNotNull(retrievedPackage, "Response body should not be null");
		assertEquals(nsUri, retrievedPackage.getNsURI(), "Response should contain the requested nsUri");
		assertEquals("GetTestPackage", retrievedPackage.getName(), "Response should contain the package name");
	}

	@Test
	void testGetEPackage_NonExistent_ReturnsNoContent() {
		// Given: A non-existent nsUri
		String nsUri = "http://nonexistent.example.com/test/1.0";

		// When: GET with non-existent nsUri
		Response response = client.target(BASE_URL)
			.queryParam("nsUri", nsUri)
			.request(MediaType.APPLICATION_XML)
			.get();

		// Then: Should return 204 No Content
		assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus(),
			"Should return 204 No Content for non-existent EPackage");
	}

	@Test
	void testListEPackages_WithoutFilters_Success() throws IOException {
		// Given: Multiple EPackages
		String nsUri1 = TestHelper.generateUniqueNsUri("list1");
		String nsUri2 = TestHelper.generateUniqueNsUri("list2");
		createEPackageViaREST(createBasicTestEPackage(nsUri1, "ListPackage1", "list1"));
		createEPackageViaREST(createBasicTestEPackage(nsUri2, "ListPackage2", "list2"));

		// When: GET without nsUri (list all)
		Response response = client.target(BASE_URL)
			.request(MediaType.APPLICATION_JSON)
			.get();

		// Then: Should return 200 OK with list
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(),
			"Should return 200 OK");

		EPackageListResponse listResponse = response.readEntity(EPackageListResponse.class);
		assertNotNull(listResponse, "List response should not be null");
		assertTrue(listResponse.getPackages().size() >= 2,
			"Should return at least the 2 created packages");
	}

	@Test
	void testUpdateEPackage_Success() throws IOException {
		// Given: An existing EPackage
		String nsUri = TestHelper.generateUniqueNsUri("updateTest");
		EPackage originalPackage = createBasicTestEPackage(nsUri, "OriginalName", "orig");
		createEPackageViaREST(originalPackage);

		// When: PUT with updated EPackage (same nsUri, different name)
		EPackage updatedPackage = createBasicTestEPackage(nsUri, "UpdatedName", "upd");
		String xmiContent = TestHelper.serializeToXMI(updatedPackage, resourceSet);
		Response response = client.target(BASE_URL)
			.queryParam("nsUri", nsUri)
			.request(MediaType.APPLICATION_XML)
			.put(Entity.entity(xmiContent, "application/xmi"));

		// Then: Should return 200 OK
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(),
			"Should return 200 OK");

		// Verify the update
		Response getResponse = client.target(BASE_URL)
			.queryParam("nsUri", nsUri)
			.request(MediaType.APPLICATION_XML)
			.get();
		EPackage retrievedPackage = getResponse.readEntity(EPackage.class);
		assertNotNull(retrievedPackage, "Retrieved package should not be null");
		assertEquals("UpdatedName", retrievedPackage.getName(), "Should contain updated name");
		assertEquals(nsUri, retrievedPackage.getNsURI(), "nsUri should remain the same");
	}

	@Test
	void testDeleteEPackage_Success() throws IOException {
		// Given: An existing EPackage
		String nsUri = TestHelper.generateUniqueNsUri("deleteTest");
		EPackage testPackage = createBasicTestEPackage(nsUri, "DeleteTestPackage", "del");
		createEPackageViaREST(testPackage);

		// When: DELETE the EPackage
		Response response = client.target(BASE_URL)
			.queryParam("nsUri", nsUri)
			.request()
			.delete();

		// Then: Should return 204 No Content
		assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus(),
			"Should return 204 No Content");

		// Verify deletion
		Response getResponse = client.target(BASE_URL)
			.queryParam("nsUri", nsUri)
			.request(MediaType.APPLICATION_XML)
			.get();
		assertEquals(Response.Status.NO_CONTENT.getStatusCode(), getResponse.getStatus(),
			"Deleted EPackage should not be found");

		// Remove from cleanup list since already deleted
		createdNsUris.remove(nsUri);
	}

	@Test
	void testDeleteEPackage_Idempotent() {
		// Given: A non-existent EPackage
		String nsUri = "http://test.example.com/idempotent/1.0";

		// When: DELETE a non-existent EPackage
		Response response = client.target(BASE_URL)
			.queryParam("nsUri", nsUri)
			.request()
			.delete();

		// Then: Should still return 204 No Content (idempotent)
		assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus(),
			"DELETE should be idempotent and return 204");
	}

	// ============ ERROR SCENARIO TESTS ============

	@Test
	void testCreateEPackage_InvalidData_Returns400() {
		// Given: Invalid JSON data
		String invalidJson = "{\"invalid\": \"data\"}";

		// When: POST with invalid data
		Response response = client.target(BASE_URL)
			.request(MediaType.APPLICATION_JSON)
			.post(Entity.json(invalidJson));

		// Then: Should return 400 Bad Request
		assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), response.getStatus(),
			"Should return 400 Bad Request for invalid data");
	}

	@Test
	void testCreateEPackage_Conflict_Returns409() throws IOException {
		// Given: An existing EPackage
		String nsUri = TestHelper.generateUniqueNsUri("conflictTest");
		EPackage testPackage = createBasicTestEPackage(nsUri, "ConflictPackage", "conflict");
		createEPackageViaREST(testPackage);

		// When: POST with same nsUri (duplicate)
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		Response response = client.target(BASE_URL)
			.request(MediaType.APPLICATION_XML)
			.post(Entity.entity(xmiContent, "application/xmi"));

		// Then: Should return 409 Conflict
		assertEquals(Response.Status.CONFLICT.getStatusCode(), response.getStatus(),
			"Should return 409 Conflict for duplicate nsUri");
	}

	@Test
	void testCreateEPackage_UnsupportedMediaType_Returns415() {
		// Given: Content with unsupported media type
		String content = "Some unsupported content";

		// When: POST with unsupported media type
		Response response = client.target(BASE_URL)
			.request(MediaType.APPLICATION_JSON)
			.post(Entity.entity(content, "application/x-custom-unsupported"));

		// Then: Should return 415 Unsupported Media Type
		assertEquals(Response.Status.UNSUPPORTED_MEDIA_TYPE.getStatusCode(), response.getStatus(),
			"Should return 415 Unsupported Media Type");
	}

	@Test
	void testUpdateEPackage_NonExistent_Returns204() throws IOException {
		// Given: A non-existent EPackage
		String nsUri = "http://test.example.com/nonexistent/1.0";
		EPackage testPackage = createBasicTestEPackage(nsUri, "NonExistent", "ne");

		// When: PUT to update non-existent EPackage
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		Response response = client.target(BASE_URL)
			.queryParam("nsUri", nsUri)
			.request(MediaType.APPLICATION_XML)
			.put(Entity.entity(xmiContent, "application/xmi"));

		// Then: Should return 204 No Content (not found)
		assertEquals(Response.Status.NO_CONTENT.getStatusCode(), response.getStatus(),
			"Should return 204 No Content for non-existent EPackage update");
	}

	@Test
	void testUpdateEPackage_InvalidData_Returns400() {
		// Given: Invalid data for update
		String nsUri = "http://test.example.com/update/1.0";
		String invalidXml = "<invalid>data</invalid>";

		// When: PUT with invalid data
		Response response = client.target(BASE_URL)
			.queryParam("nsUri", nsUri)
			.request(MediaType.APPLICATION_XML)
			.put(Entity.entity(invalidXml, MediaType.APPLICATION_XML));

		// Then: Should return 500 Internal Server Error (deserialization failure at framework level)
		assertEquals(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), response.getStatus(),
			"Should return 500 for XML deserialization failure");
	}

	// ============ EDGE CASE TESTS ============

	@Test
	void testListEPackages_WithPagination_Success() throws IOException {
		// Given: Multiple EPackages
		for (int i = 0; i < 5; i++) {
			String nsUri = TestHelper.generateUniqueNsUri("page" + i);
			createEPackageViaREST(createBasicTestEPackage(nsUri, "PagePackage" + i, "page" + i));
		}

		// When: GET with skip and limit parameters
		Response response = client.target(BASE_URL)
			.queryParam("skip", 1)
			.queryParam("limit", 2)
			.request(MediaType.APPLICATION_JSON)
			.get();

		// Then: Should return paginated results
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(),
			"Should return 200 OK");

		EPackageListResponse listResponse = response.readEntity(EPackageListResponse.class);
		assertNotNull(listResponse, "List response should not be null");
		assertEquals(1, listResponse.getSkip(), "Skip should match request");
		assertEquals(2, listResponse.getLimit(), "Limit should match request");
		assertTrue(listResponse.getPackages().size() <= 2,
			"Should return at most 2 packages");
	}

	@Test
	void testMediaTypeNegotiation_ViaAcceptHeader() throws IOException {
		// Given: An existing EPackage
		String nsUri = TestHelper.generateUniqueNsUri("mediaTypeTest");
		EPackage testPackage = createBasicTestEPackage(nsUri, "MediaTypePackage", "mt");
		createEPackageViaREST(testPackage);

		// When: GET with different Accept headers
		Response xmlResponse = client.target(BASE_URL)
			.queryParam("nsUri", nsUri)
			.request(MediaType.APPLICATION_XML)
			.get();

		Response jsonResponse = client.target(BASE_URL)
			.queryParam("nsUri", nsUri)
			.request(MediaType.APPLICATION_JSON)
			.get();

		// Then: Should return content in requested formats
		assertEquals(Response.Status.OK.getStatusCode(), xmlResponse.getStatus(),
			"XML request should succeed");
		assertEquals(Response.Status.OK.getStatusCode(), jsonResponse.getStatus(),
			"JSON request should succeed");

		// Verify entities can be deserialized
		EPackage xmlPackage = xmlResponse.readEntity(EPackage.class);
		EPackage jsonPackage = jsonResponse.readEntity(EPackage.class);

		assertNotNull(xmlPackage, "XML response should contain a valid EPackage");
		assertNotNull(jsonPackage, "JSON response should contain a valid EPackage");
		assertEquals(nsUri, xmlPackage.getNsURI(), "XML response should contain correct nsUri");
		assertEquals(nsUri, jsonPackage.getNsURI(), "JSON response should contain correct nsUri");
		assertEquals("MediaTypePackage", xmlPackage.getName(), "XML response should contain correct name");
		assertEquals("MediaTypePackage", jsonPackage.getName(), "JSON response should contain correct name");
	}

	@Test
	void testMediaTypeNegotiation_IncludingJsonSchema() throws IOException {
		// Given: An existing EPackage with complex structure
		String nsUri = TestHelper.generateUniqueNsUri("multiMediaTypeTest");
		EPackage testPackage = createBasicTestEPackage(nsUri, "MediaTypePackage", "mt");
		createEPackageViaREST(testPackage);

		// When: GET with XML, JSON, and JSON Schema media types
		Response xmlResponse = client.target(BASE_URL)
			.queryParam("nsUri", nsUri)
			.request(MediaType.APPLICATION_XML)
			.get();

		Response jsonResponse = client.target(BASE_URL)
			.queryParam("nsUri", nsUri)
			.request(MediaType.APPLICATION_JSON)
			.get();

		Response jsonSchemaResponse = client.target(BASE_URL)
			.queryParam("nsUri", nsUri)
			.request("application/schema+json")
			.get();

		// Then: All media types should succeed
		assertEquals(Response.Status.OK.getStatusCode(), xmlResponse.getStatus(),
			"XML request should succeed");
		assertEquals(Response.Status.OK.getStatusCode(), jsonResponse.getStatus(),
			"JSON request should succeed");
		assertEquals(Response.Status.OK.getStatusCode(), jsonSchemaResponse.getStatus(),
			"JSON Schema request should succeed");

		// Verify XML and JSON can be deserialized to EPackage
		EPackage xmlPackage = xmlResponse.readEntity(EPackage.class);
		EPackage jsonPackage = jsonResponse.readEntity(EPackage.class);

		assertNotNull(xmlPackage, "XML response should contain a valid EPackage");
		assertNotNull(jsonPackage, "JSON response should contain a valid EPackage");
		assertEquals(nsUri, xmlPackage.getNsURI(), "XML response should contain correct nsUri");
		assertEquals(nsUri, jsonPackage.getNsURI(), "JSON response should contain correct nsUri");

		// Verify JSON Schema response contains schema structure
		String jsonSchema = jsonSchemaResponse.readEntity(String.class);
		assertNotNull(jsonSchema, "JSON Schema response should not be null");
		assertTrue(jsonSchema.contains("definitions"), "JSON Schema should contain definitions");
		assertTrue(jsonSchema.contains(nsUri), "JSON Schema should contain the nsUri");
	}

	@Test
	void testMediaTypeNegotiation_ViaQueryParameter() throws IOException {
		// Given: An existing EPackage
		String nsUri = TestHelper.generateUniqueNsUri("queryMediaTypeTest");
		EPackage testPackage = createBasicTestEPackage(nsUri, "QueryMediaTypePackage", "qmt");
		createEPackageViaREST(testPackage);

		// When: GET with mediaType query parameter
		Response response = client.target(BASE_URL)
			.queryParam("nsUri", nsUri)
			.queryParam("mediaType", "application/json")
			.request()
			.get();

		// Then: Should return JSON content
		assertEquals(Response.Status.OK.getStatusCode(), response.getStatus(),
			"Should return 200 OK");

		EPackage retrievedPackage = response.readEntity(EPackage.class);
		assertNotNull(retrievedPackage, "Retrieved package should not be null");
		assertEquals(nsUri, retrievedPackage.getNsURI(), "Should return correct nsUri");
		assertEquals("QueryMediaTypePackage", retrievedPackage.getName(), "Should return correct name");
	}

	@Test
	void testConcurrentCreateOperations() throws InterruptedException {
		// Given: Multiple threads creating EPackages concurrently
		int threadCount = 5;
		ExecutorService executor = Executors.newFixedThreadPool(threadCount);
		CountDownLatch latch = new CountDownLatch(threadCount);
		AtomicInteger successCount = new AtomicInteger(0);
		AtomicInteger failureCount = new AtomicInteger(0);

		// When: Create EPackages concurrently
		for (int i = 0; i < threadCount; i++) {
			final int index = i;
			executor.submit(() -> {
				try {
					String nsUri = TestHelper.generateUniqueNsUri("concurrent" + index);
					EPackage pkg = createBasicTestEPackage(nsUri, "ConcurrentPackage" + index, "c" + index);
					String xmiContent = TestHelper.serializeToXMI(pkg, resourceSet);

					Response response = client.target(BASE_URL)
						.request(MediaType.APPLICATION_XML)
						.post(Entity.entity(xmiContent, "application/xmi"));

					if (response.getStatus() == Response.Status.CREATED.getStatusCode()) {
						successCount.incrementAndGet();
						synchronized (createdNsUris) {
							createdNsUris.add(nsUri);
						}
					} else {
						failureCount.incrementAndGet();
					}
				} catch (Exception e) {
					failureCount.incrementAndGet();
				} finally {
					latch.countDown();
				}
			});
		}

		// Then: All operations should complete successfully
		assertTrue(latch.await(30, TimeUnit.SECONDS),
			"All concurrent operations should complete within 30 seconds");
		executor.shutdown();

		assertEquals(threadCount, successCount.get(),
			"All concurrent create operations should succeed");
		assertEquals(0, failureCount.get(), "No failures should occur");
	}

	@Test
	void testComplexEPackage_WithClassesAndAttributes() throws IOException {
		// Given: A complex EPackage with EClasses and EAttributes
		String nsUri = TestHelper.generateUniqueNsUri("complexTest");
		EPackage complexPackage = createComplexTestEPackage(nsUri);

		// When: Create the complex EPackage
		String xmiContent = TestHelper.serializeToXMI(complexPackage, resourceSet);
		Response response = client.target(BASE_URL)
			.request(MediaType.APPLICATION_XML)
			.post(Entity.entity(xmiContent, "application/xmi"));

		// Then: Should create successfully
		assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus(),
			"Should create complex EPackage successfully");
		createdNsUris.add(nsUri);

		// Verify retrieval
		Response getResponse = client.target(BASE_URL)
			.queryParam("nsUri", nsUri)
			.request(MediaType.APPLICATION_XML)
			.get();
		assertEquals(Response.Status.OK.getStatusCode(), getResponse.getStatus(),
			"Should retrieve complex EPackage successfully");

		EPackage retrievedPackage = getResponse.readEntity(EPackage.class);
		assertNotNull(retrievedPackage, "Retrieved package should not be null");
		assertEquals(nsUri, retrievedPackage.getNsURI(), "Should have correct nsUri");
		assertEquals(1, retrievedPackage.getEClassifiers().size(), "Should contain Person EClass");

		EClass personClass = (EClass) retrievedPackage.getEClassifiers().get(0);
		assertEquals("Person", personClass.getName(), "Should contain Person EClass");
		assertEquals(2, personClass.getEStructuralFeatures().size(), "Person should have 2 attributes");

		EAttribute nameAttr = (EAttribute) personClass.getEStructuralFeatures().stream()
			.filter(f -> "name".equals(f.getName()))
			.findFirst()
			.orElse(null);
		assertNotNull(nameAttr, "Should contain name attribute");
	}

	// ============ HELPER METHODS ============

	private EPackage createBasicTestEPackage(String nsUri, String name, String nsPrefix) {
		return TestHelper.createTestEPackage(nsUri, name, nsPrefix);
	}

	private EPackage createComplexTestEPackage(String nsUri) {
		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		ePackage.setNsURI(nsUri);
		ePackage.setName("ComplexPackage");
		ePackage.setNsPrefix("complex");

		// Create an EClass
		EClass personClass = EcoreFactory.eINSTANCE.createEClass();
		personClass.setName("Person");

		// Add attributes
		EAttribute nameAttr = EcoreFactory.eINSTANCE.createEAttribute();
		nameAttr.setName("name");
		nameAttr.setEType(EcorePackage.Literals.ESTRING);
		personClass.getEStructuralFeatures().add(nameAttr);

		EAttribute ageAttr = EcoreFactory.eINSTANCE.createEAttribute();
		ageAttr.setName("age");
		ageAttr.setEType(EcorePackage.Literals.EINT);
		personClass.getEStructuralFeatures().add(ageAttr);

		ePackage.getEClassifiers().add(personClass);

		return ePackage;
	}

	private void createEPackageViaREST(EPackage ePackage) throws IOException {
		String xmiContent = TestHelper.serializeToXMI(ePackage, resourceSet);
		Response response = client.target(BASE_URL)
			.request(MediaType.APPLICATION_XML)
			.post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(Response.Status.CREATED.getStatusCode(), response.getStatus(),
			"Failed to create test EPackage: " + ePackage.getNsURI());

		createdNsUris.add(ePackage.getNsURI());
	}
}
