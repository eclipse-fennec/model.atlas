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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.util.Date;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.model.atlas.rest.model.RestFactory;
import org.eclipse.fennec.model.atlas.rest.model.StageTransitionRequest;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestAnnotations;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestAnnotations.ParentScopeServiceSetup;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestHelper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.Response;

/**
 * Integration tests for SchemaPackagesResource REST endpoints.
 *
 * <p>
 * Tests cover:
 * </p>
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

public class SchemaPackagesResourceTest extends AbstractRestTest {

	private static final String TEST_PACKAGE_NSURI = "http://test.example.com/schema/1.1";
	private static final String TEST_PACKAGE_NAME = "TestSchema";

	// ========== List All Packages Tests ==========

	@Test
	@ParentScopeServiceSetup
	public void testListAllPackages_Success(@InjectBundleContext BundleContext context) throws InterruptedException, IOException {

		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(201, response, "Should return HTTP 201 Created");

		response = schemaTarget().path("all")
				.request("application/json").get();

		assertStatus(200, response, "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return content");
		assertTrue(responseContent.contains("metadata"), "Response should contain metadata");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListAllPackages_ScopeNotFound(@InjectBundleContext BundleContext context) throws InterruptedException, IOException {

		ensureResourceAvailability(context);
		Response response = schemaTarget("non-existent-scope").path("all")
				.request("application/json").get();

		assertStatus(400, response, "Should return HTTP 400 Bad Request");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListAllPackages_WithMediaTypeQueryParam(@InjectBundleContext BundleContext context) throws InterruptedException, IOException {

		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(201, response, "Should return HTTP 201 Created");

		response = schemaTarget().path("all")
				.queryParam("mediaType", "application/xml").request("application/json").get();

		assertStatus(200, response, "Should return HTTP 200 OK");
		assertEquals("application/xml", response.getHeaderString("Content-Type"),
				"Content-Type header should be set to mediaType query parameter value");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListAllPackages_WithUnsupportedMediaTypeQueryParam(@InjectBundleContext BundleContext context) throws InterruptedException, IOException {

		ensureResourceAvailability(context);
		Response response = schemaTarget().path("all")
				.queryParam("mediaType", "application/unsupported").request("application/json").get();

		assertStatus(415, response, "Should return HTTP 415 Unsupported Media Type");
	}


	// ========== List Operations Tests ==========

	@Test
	@ParentScopeServiceSetup
	public void testListReleasedPackages_Success(@InjectBundleContext BundleContext context) throws InterruptedException, IOException {

		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		
		

		Response response = schemaStageTarget(TestAnnotations.STAGE_RELEASE).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(201, response, "Should return HTTP 201 Created");

		response = schemaTarget().request("application/json")
				.get();

		assertStatus(200, response, "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return content");
		assertTrue(responseContent.contains("metadata"), "Response should contain metadata");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListReleasedPackages_ScopeNotFound(@InjectBundleContext BundleContext context) throws InterruptedException {
		ensureResourceAvailability(context);
		Response response = schemaTarget("non-existent-scope")
				.request("application/json").get();

		assertStatus(400, response, "Should return HTTP 400 Bad Request");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListPackagesInStage_Success(@InjectBundleContext BundleContext context) throws InterruptedException, IOException {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(201, response, "Should return HTTP 201 Created");

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).request("application/json").get();

		assertStatus(200, response, "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return content");
		assertTrue(responseContent.contains("metadata"), "Response should contain metadata");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListPackagesInStage_WithNsUriFilter(@InjectBundleContext BundleContext context) throws InterruptedException, IOException {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(201, response, "Should return HTTP 201 Created");

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI).request("application/json").get();

		assertStatus(200, response, "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return content");
		assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListPackagesInStage_NotFound(@InjectBundleContext BundleContext context) throws InterruptedException, IOException {
		ensureResourceAvailability(context);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", "http://non-existent.com/schema/1.0")
				.request("application/json").get();

		assertStatus(204, response, "Should return HTTP 204 No Content");
	}

	// ========== Create Package Tests ==========

	@Test
	@ParentScopeServiceSetup
	public void testCreatePackage_Success(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));


		System.out.println("DEBUG testCreatePackage_Success - Response status: " + response.getStatus());
		String responseContent = response.readEntity(String.class);
		System.out.println("DEBUG testCreatePackage_Success - Response content: " + responseContent);

		assertStatus(201, response, "Should return HTTP 201 Created");
		assertNotNull(responseContent, "Should return content");
	}

	@Test
	@ParentScopeServiceSetup
	public void testCreatePackage_ScopeNotFound(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget("non-existing-scope", TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));


		assertStatus(400, response, "Should return HTTP 400 Bad Request");
	}

	// ========== Overwrite Parameter Tests ==========

	@Test
	@ParentScopeServiceSetup
	public void testCreatePackage_Conflict_WithoutOverwrite(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(409, response,
				"Should return HTTP 409 Conflict when Overwrite is false and package exists");
	}

	@Test
	@ParentScopeServiceSetup
	public void testCreatePackage_WithOverwrite_Success(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME)
				.queryParam("overwrite", true).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		System.out.println("DEBUG testCreatePackage_WithOverwrite_Success - Response status: " + response.getStatus());
		String responseContent = response.readEntity(String.class);
		System.out.println("DEBUG testCreatePackage_WithOverwrite_Success - Response content: " + responseContent);

		assertStatus(200, response,
				"Should return HTTP 200 OK when Overwrite is true and package exists and is writable");
		assertNotNull(responseContent, "Should return updated metadata");
		assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
	}

	@Test
	@ParentScopeServiceSetup
	public void testCreatePackage_WithOverwrite_ReadOnly(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.TEST_PARENT_SCOPE_NAME, TestAnnotations.STAGE_RELEASE).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(201, response,
				"Should return HTTP 201 Created");

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME)
				.queryParam("overwrite", true).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		System.out.println("DEBUG testCreatePackage_WithOverwrite_ReadOnly - Response status: " + response.getStatus());
		String responseContent = response.readEntity(String.class);
		System.out.println("DEBUG testCreatePackage_WithOverwrite_ReadOnly - Response content: " + responseContent);

		assertStatus(403, response,
				"Should return HTTP 403 Forbidden when Overwrite is true but package is read-only");
	}

	@Test
	@ParentScopeServiceSetup
	public void testCreatePackage_WithOverwrite_NewPackage(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.TEST_PARENT_SCOPE_NAME, TestAnnotations.STAGE_RELEASE).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).queryParam("overwrite", true).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		System.out
		.println("DEBUG testCreatePackage_WithOverwrite_NewPackage - Response status: " + response.getStatus());
		String responseContent = response.readEntity(String.class);
		System.out.println("DEBUG testCreatePackage_WithOverwrite_NewPackage - Response content: " + responseContent);

		assertStatus(201, response,
				"Should return HTTP 201 Created when Overwrite is true for new package (behaves like normal create)");
		assertNotNull(responseContent, "Should return created metadata");
	}

	// ========== Get Package Content Tests ==========

	@Test
	@ParentScopeServiceSetup
	public void testGetPackageContent_Success(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));


		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content").queryParam("nsUri", TEST_PACKAGE_NSURI)
				.request("application/json").get();

		assertStatus(200, response, "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return content");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetPackageContent_NotFound(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content").queryParam("nsUri", "http://non-existent.com/schema/1.0")
				.request("application/json").get();

		assertStatus(204, response, "Should return HTTP 204 No Content");
	}

	// ========== Get Package Content From Final Stage Tests (P5-0) ==========

	@Test
	@ParentScopeServiceSetup
	public void testGetPackageContentFromFinalStage_Success(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		schemaStageTarget(TestAnnotations.STAGE_RELEASE).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		Response response = schemaTarget().path("content").queryParam("nsUri", TEST_PACKAGE_NSURI)
				.request("application/json").get();

		assertStatus(200, response, "Should return HTTP 200 OK");
		assertNotNull(response.readEntity(String.class), "Should return content");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetPackageContentFromFinalStage_NotFound(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		Response response = schemaTarget().path("content").queryParam("nsUri", "http://non-existent.com/schema/1.0")
				.request("application/json").get();

		assertStatus(204, response, "Should return HTTP 204 No Content when package not in final stage");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetPackageContentFromFinalStage_ScopeNotFound(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		Response response = schemaTarget("non-existent-scope").path("content").queryParam("nsUri", TEST_PACKAGE_NSURI)
				.request("application/json").get();

		assertStatus(400, response, "Should return HTTP 400 Bad Request for unknown scope");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetPackageContentFromFinalStage_ConditionalGetNotModified(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		schemaStageTarget(TestAnnotations.STAGE_RELEASE).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		Response first = schemaTarget().path("content").queryParam("nsUri", TEST_PACKAGE_NSURI)
				.request("application/json").get();
		assertStatus(200, first, "First GET should return HTTP 200 OK");
		String etag = first.getHeaderString("ETag");
		assertNotNull(etag, "Final-stage content GET should emit an ETag");

		Response second = schemaTarget().path("content").queryParam("nsUri", TEST_PACKAGE_NSURI)
				.request("application/json").header("If-None-Match", etag).get();
		assertStatus(304, second, "Matching If-None-Match should return HTTP 304 Not Modified");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetPackageContentFromFinalStage_InheritsFromParent(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		// Upload only to the PARENT scope's final stage; the child scope must read through.
		Response upload = schemaStageTarget(TestAnnotations.TEST_PARENT_SCOPE_NAME, TestAnnotations.STAGE_RELEASE)
				.queryParam("nsUri", TEST_PACKAGE_NSURI).queryParam("name", TEST_PACKAGE_NAME)
				.request("application/xmi").post(Entity.entity(xmiContent, "application/xmi"));
		assertStatus(201, upload, "Upload to parent scope final stage should succeed");

		Response response = schemaTarget().path("content").queryParam("nsUri", TEST_PACKAGE_NSURI)
				.request("application/json").get();

		assertStatus(200, response, "Child final-stage content should read through to the parent scope");
		assertNotNull(response.readEntity(String.class), "Should return inherited content");
	}

	// ========== Update Package Content Tests ==========

	@Test
	@ParentScopeServiceSetup
	public void testUpdatePackageContent_Success(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		EPackage updatedPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, "UpdatedSchema", "test");
		xmiContent = TestHelper.serializeToXMI(updatedPackage, resourceSet);

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content").queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("version", "1.1.0").request("application/xmi")
				.put(Entity.entity(xmiContent, "application/xmi"));

		System.out.println("DEBUG testUpdatePackageContent_Success - Response status: " + response.getStatus());
		String responseContent = response.readEntity(String.class);
		System.out.println("DEBUG testUpdatePackageContent_Success - Response content: " + responseContent);

		assertStatus(200, response, "Should return HTTP 200 OK");
	}

	@Disabled("We have to verify the proper behaviour of non writable stages")
	@Test
	@ParentScopeServiceSetup
	public void testUpdatePackageContent_ReadOnlyStage(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage updatedPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, "UpdatedSchema", "test");
		String xmiContent = TestHelper.serializeToXMI(updatedPackage, resourceSet);

		Response response = schemaStageTarget("readonly-stage").path("content").queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("version", "1.1.0").request("application/xmi")
				.put(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(403, response, "Should return HTTP 403 Forbidden");
	}

	@Test
	@ParentScopeServiceSetup
	public void testUpdatePackageContent_NotFound(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage updatedPackage = TestHelper.createTestEPackage("http://non-existent.com/schema/1.0", "NonExistent",
				"ne");
		String xmiContent = TestHelper.serializeToXMI(updatedPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content").queryParam("nsUri", "http://non-existent.com/schema/1.0")
				.queryParam("version", "1.0.0").request("application/xmi")
				.put(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(204, response, "Should return HTTP 204 No Content");
	}

	// ========== Delete Package Tests ==========

	@Test
	@ParentScopeServiceSetup
	public void testDeletePackage_Success(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(201, response, "Should return HTTP 201 Created");

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI).request().delete();

		assertStatus(200, response, "Should return HTTP 200 OK");
	}

	@Disabled("We have to verify the proper behaviour of non writable stages")
	@Test
	@ParentScopeServiceSetup
	public void testDeletePackage_ReadOnlyStage(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		Response response = schemaStageTarget("readonly-stage").queryParam("nsUri", TEST_PACKAGE_NSURI).request().delete();

		assertStatus(403, response, "Should return HTTP 403 Forbidden");
	}

	@Test
	@ParentScopeServiceSetup
	public void testDeletePackage_NotFound(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", "http://non-existent.com/schema/1.0").request().delete();

		assertStatus(204, response, "Should return HTTP 204 No Content");
	}

	// ========== Transition Tests ==========

	@Test
	@ParentScopeServiceSetup
	public void testTransitionPackage_Success(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(201, response, "Should return HTTP 201 Created");

		StageTransitionRequest transition = RestFactory.eINSTANCE.createStageTransitionRequest();
		transition.setObjectId(TEST_PACKAGE_NSURI);
		transition.setTargetStage(TestAnnotations.STAGE_APPROVED);

		xmiContent = TestHelper.serializeToXMI(transition, resourceSet);

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("actions").path("transition").request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		System.out.println("DEBUG testTransitionPackage_Success - Response status: " + response.getStatus());
		String responseContent = response.readEntity(String.class);
		System.out.println("DEBUG testTransitionPackage_Success - Response content: " + responseContent);

		assertStatus(200, response, "Should return HTTP 200 OK");
	}

	@Test
	@ParentScopeServiceSetup
	public void testTransitionPackage_InvalidTransition(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(201, response, "Should return HTTP 201 Created");

		StageTransitionRequest transition = RestFactory.eINSTANCE.createStageTransitionRequest();
		transition.setObjectId(TEST_PACKAGE_NSURI);
		transition.setTargetStage(TestAnnotations.STAGE_RELEASE); // Invalid: skipping approved stage

		xmiContent = TestHelper.serializeToXMI(transition, resourceSet);

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("actions").path("transition").request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(400, response, "Should return HTTP 400 Bad Request");
	}

	@Test
	@ParentScopeServiceSetup
	public void testTransitionPackage_NotFound(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		StageTransitionRequest transition = RestFactory.eINSTANCE.createStageTransitionRequest();
		transition.setObjectId("http://non-existent.com/schema/1.0");
		transition.setTargetStage(TestAnnotations.STAGE_APPROVED);

		String xmiContent = TestHelper.serializeToXMI(transition, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("actions").path("transition").request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(204, response, "Should return HTTP 204 No Content");
	}

	// ========== List Packages By Name Tests ==========

	@Test
	@ParentScopeServiceSetup
	public void testListPackagesInStageByName_ExactMatch(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(201, response, "Should return HTTP 201 Created");

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("name", TEST_PACKAGE_NAME).request("application/json").get();

		assertStatus(200, response, "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return content");
		assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
		assertTrue(responseContent.contains(TEST_PACKAGE_NAME), "Response should contain the package name");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListPackagesInStageByName_WildcardMatch(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(201, response, "Should return HTTP 201 Created");

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("name", "Test*").request("application/json").get();

		assertStatus(200, response, "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return content");
		assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListPackagesInStageByName_PartialWildcardMatch(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(201, response, "Should return HTTP 201 Created");

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("name", "TestSchema*").request("application/json").get();

		assertStatus(200, response, "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return content");
		assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListPackagesInStageByName_NotFound(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("name", "NonExistentPackage").request("application/json").get();

		assertStatus(204, response, "Should return HTTP 204 No Content when no packages match");
	}



	@Test
	@ParentScopeServiceSetup
	public void testListPackagesInStageByName_CombinedWithNsUri(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(201, response, "Should return HTTP 201 Created");

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI).queryParam("name", TEST_PACKAGE_NAME)
				.request("application/json").get();

		assertStatus(200, response, "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return content");
		assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
	}



	@Test
	@ParentScopeServiceSetup
	public void testListPackagesInStageByName_DifferentStage(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_APPROVED).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(201, response, "Should return HTTP 201 Created");

		response = schemaStageTarget(TestAnnotations.STAGE_APPROVED).queryParam("name", TEST_PACKAGE_NAME).request("application/json").get();

		assertStatus(200, response, "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return content");
		assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
	}

	// ========== MediaType Query Parameter Tests ==========

	@Test
	@ParentScopeServiceSetup
	public void testListReleasedPackages_WithMediaTypeQueryParam(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_RELEASE).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(201, response, "Should return HTTP 201 Created");
		response = schemaTarget()
				.queryParam("mediaType", "application/xml").request("application/json").get();

		assertStatus(200, response, "Should return HTTP 200 OK");
		assertEquals("application/xml", response.getHeaderString("Content-Type"),
				"Content-Type header should be set to mediaType query parameter value");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListReleasedPackages_WithUnsupportedMediaTypeQueryParam(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		Response response = schemaTarget()
				.queryParam("mediaType", "application/unsupported").request("application/json").get();

		assertStatus(415, response, "Should return HTTP 415 Unsupported Media Type");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListPackagesInStage_WithMediaTypeQueryParam(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(201, response, "Should return HTTP 201 Created");

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("mediaType", "application/xml").request("application/json").get();

		assertStatus(200, response, "Should return HTTP 200 OK");
		assertEquals("application/xml", response.getHeaderString("Content-Type"),
				"Content-Type header should be set to mediaType query parameter value");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetPackageContent_WithMediaTypeQueryParam(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(201, response, "Should return HTTP 201 Created");

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content").queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("mediaType", "application/xml").request("application/json").get();

		assertStatus(200, response, "Should return HTTP 200 OK");
		assertEquals("application/xml", response.getHeaderString("Content-Type"),
				"Content-Type header should be set to mediaType query parameter value");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetPackageContent_WithUnsupportedMediaTypeQueryParam(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(201, response, "Should return HTTP 201 Created");

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content").queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("mediaType", "application/unsupported").request("application/json").get();

		assertStatus(415, response, "Should return HTTP 415 Unsupported Media Type");
	}

	@Test
	@ParentScopeServiceSetup
	public void testCreatePackage_WithMediaTypeQueryParam(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", "MediaTypePackage").queryParam("version", "1.0.0")
				.queryParam("mediaType", "application/xml").request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(201, response, "Should return HTTP 201 Created");
		assertEquals("application/xml", response.getHeaderString("Content-Type"),
				"Content-Type header should be set to mediaType query parameter value");
	}

	@Test
	@ParentScopeServiceSetup
	public void testUpdatePackageContent_WithMediaTypeQueryParam(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(201, response, "Should return HTTP 201 Created");

		EPackage updatedPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, "UpdatedSchema", "test");
		xmiContent = TestHelper.serializeToXMI(updatedPackage, resourceSet);

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content").queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("version", "1.1.0").queryParam("mediaType", "application/xml").request("application/xmi")
				.put(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(200, response, "Should return HTTP 200 OK");
		assertEquals("application/xml", response.getHeaderString("Content-Type"),
				"Content-Type header should be set to mediaType query parameter value");
	}

	@Test
	@ParentScopeServiceSetup
	public void testTransitionPackage_WithMediaTypeQueryParam(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(201, response, "Should return HTTP 201 Created");

		StageTransitionRequest transition = RestFactory.eINSTANCE.createStageTransitionRequest();
		transition.setObjectId(TEST_PACKAGE_NSURI);
		transition.setTargetStage(TestAnnotations.STAGE_APPROVED);

		xmiContent = TestHelper.serializeToXMI(transition, resourceSet);

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("actions").path("transition").queryParam("mediaType", "application/xml")
				.request("application/xmi").post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(200, response, "Should return HTTP 200 OK");
		assertEquals("application/xml", response.getHeaderString("Content-Type"),
				"Content-Type header should be set to mediaType query parameter value");
	}

	// ========== Search Endpoint Tests ==========

	@Test
	@ParentScopeServiceSetup
	public void testSearchPackages_ReturnsResults(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(201, response, "Should return HTTP 201 Created");

		response = schemaTarget().path("search")
				.queryParam("name", TEST_PACKAGE_NAME)
				.request("application/json").get();

		assertStatus(200, response, "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return content");
		assertTrue(responseContent.contains("metadata"), "Response should contain metadata");

		assertNotNull(response.getHeaderString("X-Total-Count"), "Should have X-Total-Count header");
		assertNotNull(response.getHeaderString("X-Offset"), "Should have X-Offset header");
		assertNotNull(response.getHeaderString("X-Limit"), "Should have X-Limit header");
	}

	@Test
	@ParentScopeServiceSetup
	public void testSearchPackages_ByNsUriPartialMatch(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(201, response, "Should return HTTP 201 Created");

		response = schemaTarget().path("search")
				.queryParam("nsUri", "schema")
				.request("application/json").get();

		assertStatus(200, response, "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertTrue(responseContent.contains("metadata"), "Response should contain metadata");
	}

	@Test
	@ParentScopeServiceSetup
	public void testSearchPackages_ByClassifier(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		EClass eClass = TestHelper.createTestEClass("Sensor");
		testPackage.getEClassifiers().add(eClass);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(201, response, "Should return HTTP 201 Created");

		response = schemaTarget().path("search")
				.queryParam("classifier", "Sensor")
				.request("application/json").get();

		assertStatus(200, response, "Should return HTTP 200 OK");
	}

	@Test
	@ParentScopeServiceSetup
	public void testSearchPackages_NoResults(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		Response response = schemaTarget().path("search")
				.queryParam("classifier", "DoesNotExist")
				.request("application/json").get();

		assertStatus(204, response, "Should return HTTP 204 No Content when no matches");
	}

	@Test
	@ParentScopeServiceSetup
	public void testSearchPackages_Pagination(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		// Index multiple packages
		for (int i = 0; i < 5; i++) {
			String uri = "http://test.example.com/schema"+i+"/1.1";
			String name = "schema"+1;
			EPackage testPackage = TestHelper.createTestEPackage(uri, name, name);
			String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

			Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", uri)
					.queryParam("name", name).request("application/xmi")
					.post(Entity.entity(xmiContent, "application/xmi"));
			assertStatus(201, response, "Should return HTTP 201 Created");
		}

		Response response = schemaTarget().path("search")
				.queryParam("nsUri", "test.example.com")
				.queryParam("limit", 2)
				.queryParam("offset", 0)
				.request("application/json").get();

		assertStatus(200, response, "Should return HTTP 200 OK");
		assertEquals("5", response.getHeaderString("X-Total-Count"), "Should report total hits");
		assertEquals("0", response.getHeaderString("X-Offset"), "Should report offset");
		assertEquals("2", response.getHeaderString("X-Limit"), "Should report limit");
	}

	@Test
	@ParentScopeServiceSetup
	public void testSearchPackages_WithStageFilter(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		String uri = "http://test.example.com/schema-draft/1.1";
		String name = "schema-draft";
		EPackage testPackage = TestHelper.createTestEPackage(uri, name, name);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", uri)
				.queryParam("name", name).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));
		assertStatus(201, response, "Should return HTTP 201 Created");

		uri = "http://test.example.com/schema-approved/1.1";
		name = "schema-approved";
		testPackage = TestHelper.createTestEPackage(uri, name, name);
		xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		response = schemaStageTarget(TestAnnotations.STAGE_APPROVED).queryParam("nsUri", uri)
				.queryParam("name", name).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));
		assertStatus(201, response, "Should return HTTP 201 Created");

		response = schemaTarget().path("search")
				.queryParam("stage", TestAnnotations.STAGE_DRAFT)
				.request("application/json").get();

		assertStatus(200, response, "Should return HTTP 200 OK");
		assertEquals("1", response.getHeaderString("X-Total-Count"), "Should find only draft package");
	}

	@Test
	@ParentScopeServiceSetup
	public void testSearchPackages_ScopeNotFound(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		Response response = schemaTarget("non-existent-scope").path("search")
				.queryParam("classifier", "Something")
				.request("application/json").get();

		assertStatus(400, response, "Should return HTTP 400 Bad Request for unknown scope");
	}

	@Test
	@ParentScopeServiceSetup
	public void testSearchPackages_ByFeatureName(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		EClass eClass = TestHelper.createTestEClass("Sensor");
		EAttribute eAtt = TestHelper.createTestEAttribute("temperature");
		eClass.getEAttributes().add(eAtt);
		testPackage.getEClassifiers().add(eClass);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(201, response, "Should return HTTP 201 Created");


		response = schemaTarget().path("search")
				.queryParam("featureName", "temperature")
				.request("application/json").get();

		assertStatus(200, response, "Should return HTTP 200 OK");
		assertEquals("1", response.getHeaderString("X-Total-Count"), "Should find the package");
	}

	@Test
	@ParentScopeServiceSetup
	public void testSearchPackages_ByFeatureType(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		EClass eClass = TestHelper.createTestEClass("Sensor");
		EAttribute eAtt = TestHelper.createTestEAttribute("temperature");
		eClass.getEAttributes().add(eAtt);
		testPackage.getEClassifiers().add(eClass);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(201, response, "Should return HTTP 201 Created");

		response = schemaTarget().path("search")
				.queryParam("featureType", "EInt")
				.request("application/json").get();

		assertStatus(200, response, "Should return HTTP 200 OK");
	}

	// ========== ETag / Idempotency Tests ==========

	@Test
	@ParentScopeServiceSetup
	public void testListPackagesInStage_WithNsUri_ReturnsETag(@InjectBundleContext BundleContext context) throws InterruptedException, IOException {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT)
				.queryParam("nsUri", TEST_PACKAGE_NSURI).request("application/json").get();

		assertStatus(200, response, "Should return HTTP 200 OK");
		assertNotNull(response.getHeaderString("ETag"), "Response should contain ETag header");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetPackageContent_ReturnsETag(@InjectBundleContext BundleContext context) throws InterruptedException, IOException {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("nsUri", TEST_PACKAGE_NSURI).request("application/json").get();

		assertStatus(200, response, "Should return HTTP 200 OK");
		assertNotNull(response.getHeaderString("ETag"), "Response should contain ETag header");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetPackageContent_ReturnsStrongETagAndLastModified(@InjectBundleContext BundleContext context) throws InterruptedException, IOException {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("nsUri", TEST_PACKAGE_NSURI).request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
		EntityTag etag = response.getEntityTag();
		assertNotNull(etag, "Content GET should emit an ETag (set by ObjectMetadataResponseFilter)");
		assertFalse(etag.isWeak(), "ETag should be a strong validator");
		assertNotNull(response.getLastModified(), "Content GET should emit a Last-Modified header");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetPackageContent_IfNoneMatchHit_Returns304WithETagNoBody(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		Response first = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("nsUri", TEST_PACKAGE_NSURI).request("application/json").get();
		assertEquals(200, first.getStatus());
		String etag = first.getHeaderString("ETag");
		assertNotNull(etag, "First content GET should carry an ETag");

		Response second = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("nsUri", TEST_PACKAGE_NSURI).request("application/json")
				.header("If-None-Match", etag).get();
		assertEquals(304, second.getStatus(), "Matching If-None-Match should yield 304");
		assertEquals(etag, second.getHeaderString("ETag"), "304 should still carry the current ETag");
		assertFalse(second.hasEntity(), "304 should have no body");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetPackageContent_IfNoneMatchStale_Returns200WithBodyAndETag(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("nsUri", TEST_PACKAGE_NSURI).request("application/json")
				.header("If-None-Match", "\"stale-etag-value\"").get();
		assertEquals(200, response.getStatus(), "Stale If-None-Match should yield 200");
		assertNotNull(response.getEntityTag(), "200 should carry the current ETag");
		assertTrue(response.hasEntity(), "200 should carry a body");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetPackageContent_IfModifiedSince_NotModified_Returns304(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		Response first = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("nsUri", TEST_PACKAGE_NSURI).request("application/json").get();
		assertEquals(200, first.getStatus());
		Date lastModified = first.getLastModified();
		assertNotNull(lastModified, "First content GET should carry Last-Modified");

		// Echoing the resource's own Last-Modified back means "not modified since" → 304.
		Response second = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("nsUri", TEST_PACKAGE_NSURI).request("application/json")
				.header("If-Modified-Since", lastModified).get();
		assertEquals(304, second.getStatus(), "If-Modified-Since >= lastChangeTime should yield 304");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetPackageContent_IfModifiedSince_Modified_Returns200(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		// A long-past If-Modified-Since means the resource has changed since → 200.
		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("nsUri", TEST_PACKAGE_NSURI).request("application/json")
				.header("If-Modified-Since", new Date(0L)).get();
		assertEquals(200, response.getStatus(), "Stale If-Modified-Since should yield 200");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetPackageContent_SetsVaryAccept(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("nsUri", TEST_PACKAGE_NSURI).request("application/json").get();
		assertEquals(200, response.getStatus());
		assertNotNull(response.getHeaderString("Vary"), "Cacheable content GET should carry a Vary header");
		assertTrue(response.getHeaderString("Vary").toLowerCase().contains("accept"),
				"Vary header should mark the response as varying on Accept");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetPackageContent_VaryAcceptPresentOn304(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		Response first = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("nsUri", TEST_PACKAGE_NSURI).request("application/json").get();
		String etag = first.getHeaderString("ETag");
		assertNotNull(etag);

		Response second = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("nsUri", TEST_PACKAGE_NSURI).request("application/json")
				.header("If-None-Match", etag).get();
		assertEquals(304, second.getStatus());
		assertNotNull(second.getHeaderString("Vary"), "304 should still carry a Vary header");
		assertTrue(second.getHeaderString("Vary").toLowerCase().contains("accept"),
				"304 Vary header should mark the response as varying on Accept");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetPackageContent_DistinctETagPerRepresentation(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		// Same nsURI (same content hash), two different representations → two different ETags.
		Response asXml = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("nsUri", TEST_PACKAGE_NSURI).queryParam("mediaType", "application/xml")
				.request("application/json").get();
		assertEquals(200, asXml.getStatus());
		String xmlETag = asXml.getHeaderString("ETag");
		assertNotNull(xmlETag, "XML representation should carry an ETag");

		Response asXmi = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("nsUri", TEST_PACKAGE_NSURI).queryParam("mediaType", "application/xmi")
				.request("application/json").get();
		assertEquals(200, asXmi.getStatus());
		String xmiETag = asXmi.getHeaderString("ETag");
		assertNotNull(xmiETag, "XMI representation should carry an ETag");

		assertNotEquals(xmlETag, xmiETag, "Different representations of the same object must have distinct ETags");
	}

	@Test
	@ParentScopeServiceSetup
	public void testMetadataAndContentHaveDistinctETags(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		// Metadata GET (no /content) and content GET, same nsURI and same Accept.
		Response meta = schemaStageTarget(TestAnnotations.STAGE_DRAFT)
				.queryParam("nsUri", TEST_PACKAGE_NSURI).request("application/json").get();
		assertEquals(200, meta.getStatus());
		String metaETag = meta.getHeaderString("ETag");
		assertNotNull(metaETag, "Metadata GET should emit its own ETag");

		Response content = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("nsUri", TEST_PACKAGE_NSURI).request("application/json").get();
		assertEquals(200, content.getStatus());
		String contentETag = content.getHeaderString("ETag");
		assertNotNull(contentETag, "Content GET should emit its own ETag");

		assertNotEquals(metaETag, contentETag, "Metadata and content responses must have distinct ETags");
	}

	@Test
	@ParentScopeServiceSetup
	public void testStageTransitionChangesMetadataETagNotContentETag(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		String metaBefore = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.request("application/json").get().getHeaderString("ETag");
		String contentBefore = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("nsUri", TEST_PACKAGE_NSURI).request("application/json").get().getHeaderString("ETag");
		assertNotNull(metaBefore);
		assertNotNull(contentBefore);

		// Transition DRAFT -> APPROVED without touching the content bytes.
		StageTransitionRequest transition = RestFactory.eINSTANCE.createStageTransitionRequest();
		transition.setObjectId(TEST_PACKAGE_NSURI);
		transition.setTargetStage(TestAnnotations.STAGE_APPROVED);
		String transitionXmi = TestHelper.serializeToXMI(transition, resourceSet);
		Response transitionResponse = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("actions").path("transition")
				.request("application/xmi").post(Entity.entity(transitionXmi, "application/xmi"));
		assertEquals(200, transitionResponse.getStatus(), "Transition should succeed");

		String metaAfter = schemaStageTarget(TestAnnotations.STAGE_APPROVED).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.request("application/json").get().getHeaderString("ETag");
		String contentAfter = schemaStageTarget(TestAnnotations.STAGE_APPROVED).path("content")
				.queryParam("nsUri", TEST_PACKAGE_NSURI).request("application/json").get().getHeaderString("ETag");
		assertNotNull(metaAfter);
		assertNotNull(contentAfter);

		assertNotEquals(metaBefore, metaAfter, "A stage transition must change the metadata ETag");
		assertEquals(contentBefore, contentAfter, "A stage transition must not change the content ETag");
	}

	@Test
	@ParentScopeServiceSetup
	public void testCreatePackage_ReturnsETag(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage("http://etag-test.com/schema/1.0", "ETagPackage", "etag");
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT)
				.queryParam("nsUri", "http://etag-test.com/schema/1.0")
				.queryParam("name", "ETagPackage").request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(201, response, "Should return HTTP 201 Created");
		assertNotNull(response.getHeaderString("ETag"), "Create response should contain ETag header");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListPackagesInStage_IfNoneMatchHit_Returns304(@InjectBundleContext BundleContext context) throws InterruptedException, IOException {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		Response firstResponse = schemaStageTarget(TestAnnotations.STAGE_DRAFT)
				.queryParam("nsUri", TEST_PACKAGE_NSURI).request("application/json").get();
		assertEquals(200, firstResponse.getStatus());
		String etag = firstResponse.getHeaderString("ETag");
		assertNotNull(etag, "First response should contain ETag");

		Response secondResponse = schemaStageTarget(TestAnnotations.STAGE_DRAFT)
				.queryParam("nsUri", TEST_PACKAGE_NSURI).request("application/json")
				.header("If-None-Match", etag).get();
		assertEquals(304, secondResponse.getStatus(), "Should return HTTP 304 Not Modified when ETag matches");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListPackagesInStage_IfNoneMatchMiss_Returns200(@InjectBundleContext BundleContext context) throws InterruptedException, IOException {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT)
				.queryParam("nsUri", TEST_PACKAGE_NSURI).request("application/json")
				.header("If-None-Match", "\"stale-etag-value\"").get();
		assertStatus(200, response, "Should return HTTP 200 OK when ETag doesn't match");
	}

	@Test
	@ParentScopeServiceSetup
	public void testUpdatePackageContent_IfMatchSuccess(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		// A content write validates against the content ETag, so source If-Match from a content GET.
		Response getResponse = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("nsUri", TEST_PACKAGE_NSURI).request("application/json").get();
		String etag = getResponse.getHeaderString("ETag");
		assertNotNull(etag, "Should have ETag");

		EPackage updatedPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, "IfMatchUpdated", "im");
		xmiContent = TestHelper.serializeToXMI(updatedPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("nsUri", TEST_PACKAGE_NSURI).queryParam("version", "1.1.0")
				.request("application/xmi").header("If-Match", etag)
				.put(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(200, response, "Should return HTTP 200 OK when If-Match matches");
		assertNotNull(response.getHeaderString("ETag"), "Updated response should contain new ETag");
	}

	@Test
	@ParentScopeServiceSetup
	public void testUpdatePackageContent_IfMatchFail_Returns412(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		EPackage updatedPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, "IfMatchFail", "imf");
		xmiContent = TestHelper.serializeToXMI(updatedPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("nsUri", TEST_PACKAGE_NSURI).queryParam("version", "1.1.0")
				.request("application/xmi").header("If-Match", "\"stale-etag-value\"")
				.put(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(412, response, "Should return HTTP 412 Precondition Failed when ETag doesn't match");
	}

	@Test
	@ParentScopeServiceSetup
	public void testUpdatePackageContent_NoIfMatch_StillWorks(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		EPackage updatedPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, "NoIfMatch", "nim");
		xmiContent = TestHelper.serializeToXMI(updatedPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("nsUri", TEST_PACKAGE_NSURI).queryParam("version", "1.1.0")
				.request("application/xmi").put(Entity.entity(xmiContent, "application/xmi"));

		assertStatus(200, response, "Should return HTTP 200 OK without If-Match (backward compatible)");
	}

	@Test
	@ParentScopeServiceSetup
	public void testDeletePackage_AlreadyDeleted_Returns204(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT)
				.queryParam("nsUri", "http://non-existent.com/schema/1.0").request().delete();

		assertStatus(204, response, "Should return HTTP 204 No Content for already-deleted resource");
	}

	@Test
	@ParentScopeServiceSetup
	public void testDeletePackage_IfMatchFail_Returns412(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT)
				.queryParam("nsUri", TEST_PACKAGE_NSURI).request()
				.header("If-Match", "\"stale-etag-value\"").delete();

		assertStatus(412, response,
				"Should return HTTP 412 Precondition Failed when If-Match doesn't match on delete");
	}

	@Test
	@ParentScopeServiceSetup
	public void testTransitionPackage_IfMatchSuccess(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		// A transition validates against the metadata ETag, so source If-Match from a metadata GET.
		String etag = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.request("application/json").get().getHeaderString("ETag");
		assertNotNull(etag, "Metadata GET should carry an ETag");

		StageTransitionRequest transition = RestFactory.eINSTANCE.createStageTransitionRequest();
		transition.setObjectId(TEST_PACKAGE_NSURI);
		transition.setTargetStage(TestAnnotations.STAGE_APPROVED);
		String transitionXmi = TestHelper.serializeToXMI(transition, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("actions").path("transition")
				.request("application/xmi").header("If-Match", etag)
				.post(Entity.entity(transitionXmi, "application/xmi"));

		assertEquals(200, response.getStatus(), "Matching If-Match should let the transition proceed");
		assertNotNull(response.getHeaderString("ETag"), "Transition response should carry the new ETag");
	}

	@Test
	@ParentScopeServiceSetup
	public void testTransitionPackage_IfMatchFail_Returns412(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		StageTransitionRequest transition = RestFactory.eINSTANCE.createStageTransitionRequest();
		transition.setObjectId(TEST_PACKAGE_NSURI);
		transition.setTargetStage(TestAnnotations.STAGE_APPROVED);
		String transitionXmi = TestHelper.serializeToXMI(transition, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("actions").path("transition")
				.request("application/xmi").header("If-Match", "\"stale-etag-value\"")
				.post(Entity.entity(transitionXmi, "application/xmi"));

		assertEquals(412, response.getStatus(), "Mismatching If-Match should block the transition with 412");
		// No side effect: the package is still in the source stage.
		Response stillInDraft = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.request("application/json").get();
		assertEquals(200, stillInDraft.getStatus(), "Package must remain in the source stage after a 412");
	}

	@Test
	@ParentScopeServiceSetup
	public void testCreatePackage_Overwrite_IfMatchSuccess(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		// A create-with-overwrite replaces content, so source If-Match from a content GET.
		String etag = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("nsUri", TEST_PACKAGE_NSURI).request("application/json").get().getHeaderString("ETag");
		assertNotNull(etag, "Content GET should carry an ETag");

		EPackage updated = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, "OverwriteUpdated", "ow");
		String updatedXmi = TestHelper.serializeToXMI(updated, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).queryParam("overwrite", "true")
				.request("application/xmi").header("If-Match", etag)
				.post(Entity.entity(updatedXmi, "application/xmi"));

		assertEquals(200, response.getStatus(), "Matching If-Match on create-overwrite should yield 200");
		assertNotNull(response.getHeaderString("ETag"), "Overwrite response should carry the new ETag");
	}

	@Test
	@ParentScopeServiceSetup
	public void testCreatePackage_Overwrite_IfMatchFail_Returns412(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		EPackage updated = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, "OverwriteFail", "owf");
		String updatedXmi = TestHelper.serializeToXMI(updated, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).queryParam("overwrite", "true")
				.request("application/xmi").header("If-Match", "\"stale-etag-value\"")
				.post(Entity.entity(updatedXmi, "application/xmi"));

		assertEquals(412, response.getStatus(), "Mismatching If-Match on create-overwrite should yield 412");
	}

	/** /{scope}/schema */
	private WebTarget schemaTarget(String scope) {
		return scopeTarget(scope).path("schema");
	}

	/** /{TEST_SCOPE_NAME}/schema */
	private WebTarget schemaTarget() {
		return schemaTarget(TestAnnotations.TEST_SCOPE_NAME);
	}

	/** /{scope}/schema/stages/{stage} */
	private WebTarget schemaStageTarget(String scope, String stage) {
		return schemaTarget(scope).path("stages").path(stage);
	}

	/** /{TEST_SCOPE_NAME}/schema/stages/{stage} */
	private WebTarget schemaStageTarget(String stage) {
		return schemaStageTarget(TestAnnotations.TEST_SCOPE_NAME, stage);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.rest.tests.AbstractRestTest#getResourceName()
	 */
	@Override
	String getResourceName() {
		return "SchemaPackagesResource";
	}

}
