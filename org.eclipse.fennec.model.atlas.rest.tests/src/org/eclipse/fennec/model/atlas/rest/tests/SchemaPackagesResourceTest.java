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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

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

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
		
		response = schemaTarget().path("all")
				.request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

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

		assertEquals(400, response.getStatus(), "Should return HTTP 400 Bad Request");
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

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
		
		response = schemaTarget().path("all")
				.queryParam("mediaType", "application/xml").request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
		assertEquals("application/xml", response.getHeaderString("Content-Type"),
				"Content-Type header should be set to mediaType query parameter value");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListAllPackages_WithUnsupportedMediaTypeQueryParam(@InjectBundleContext BundleContext context) throws InterruptedException, IOException {

		ensureResourceAvailability(context);
		Response response = schemaTarget().path("all")
				.queryParam("mediaType", "application/unsupported").request("application/json").get();

		assertEquals(415, response.getStatus(), "Should return HTTP 415 Unsupported Media Type");
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
		
		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		response = schemaTarget().request("application/json")
				.get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

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

		assertEquals(400, response.getStatus(), "Should return HTTP 400 Bad Request");
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

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

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

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI).request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

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

		assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content");
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

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
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


		assertEquals(400, response.getStatus(), "Should return HTTP 400 Bad Request");
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

		assertEquals(409, response.getStatus(),
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

		assertEquals(200, response.getStatus(),
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

		assertEquals(200, response.getStatus(),
				"Should return HTTP 200 OK");

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME)
				.queryParam("overwrite", true).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		System.out.println("DEBUG testCreatePackage_WithOverwrite_ReadOnly - Response status: " + response.getStatus());
		String responseContent = response.readEntity(String.class);
		System.out.println("DEBUG testCreatePackage_WithOverwrite_ReadOnly - Response content: " + responseContent);

		assertEquals(403, response.getStatus(),
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

		assertEquals(200, response.getStatus(),
				"Should return HTTP 200 OK when Overwrite is true for new package (behaves like normal create)");
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

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return content");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetPackageContent_NotFound(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content").queryParam("nsUri", "http://non-existent.com/schema/1.0")
				.request("application/json").get();

		assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content");
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

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
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

		assertEquals(403, response.getStatus(), "Should return HTTP 403 Forbidden");
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

		assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content");
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

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI).request().delete();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
	}

	@Disabled("We have to verify the proper behaviour of non writable stages")
	@Test
	@ParentScopeServiceSetup
	public void testDeletePackage_ReadOnlyStage(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		Response response = schemaStageTarget("readonly-stage").queryParam("nsUri", TEST_PACKAGE_NSURI).request().delete();

		assertEquals(403, response.getStatus(), "Should return HTTP 403 Forbidden");
	}

	@Test
	@ParentScopeServiceSetup
	public void testDeletePackage_NotFound(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", "http://non-existent.com/schema/1.0").request().delete();

		assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content");
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

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		StageTransitionRequest transition = RestFactory.eINSTANCE.createStageTransitionRequest();
		transition.setObjectId(TEST_PACKAGE_NSURI);
		transition.setTargetStage(TestAnnotations.STAGE_APPROVED);

		xmiContent = TestHelper.serializeToXMI(transition, resourceSet);

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("actions").path("transition").request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		System.out.println("DEBUG testTransitionPackage_Success - Response status: " + response.getStatus());
		String responseContent = response.readEntity(String.class);
		System.out.println("DEBUG testTransitionPackage_Success - Response content: " + responseContent);

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
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

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		StageTransitionRequest transition = RestFactory.eINSTANCE.createStageTransitionRequest();
		transition.setObjectId(TEST_PACKAGE_NSURI);
		transition.setTargetStage(TestAnnotations.STAGE_RELEASE); // Invalid: skipping approved stage

		xmiContent = TestHelper.serializeToXMI(transition, resourceSet);

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("actions").path("transition").request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(400, response.getStatus(), "Should return HTTP 400 Bad Request");
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

		assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content");
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

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("name", TEST_PACKAGE_NAME).request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

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

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("name", "Test*").request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

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

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("name", "TestSchema*").request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return content");
		assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListPackagesInStageByName_NotFound(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("name", "NonExistentPackage").request("application/json").get();

		assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content when no packages match");
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

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI).queryParam("name", TEST_PACKAGE_NAME)
				.request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

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

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		response = schemaStageTarget(TestAnnotations.STAGE_APPROVED).queryParam("name", TEST_PACKAGE_NAME).request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

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

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
		response = schemaTarget()
				.queryParam("mediaType", "application/xml").request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
		assertEquals("application/xml", response.getHeaderString("Content-Type"),
				"Content-Type header should be set to mediaType query parameter value");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListReleasedPackages_WithUnsupportedMediaTypeQueryParam(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		Response response = schemaTarget()
				.queryParam("mediaType", "application/unsupported").request("application/json").get();

		assertEquals(415, response.getStatus(), "Should return HTTP 415 Unsupported Media Type");
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

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("mediaType", "application/xml").request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
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

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content").queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("mediaType", "application/xml").request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
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

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content").queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("mediaType", "application/unsupported").request("application/json").get();

		assertEquals(415, response.getStatus(), "Should return HTTP 415 Unsupported Media Type");
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

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
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

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		EPackage updatedPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, "UpdatedSchema", "test");
		xmiContent = TestHelper.serializeToXMI(updatedPackage, resourceSet);

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content").queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("version", "1.1.0").queryParam("mediaType", "application/xml").request("application/xmi")
				.put(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
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

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		StageTransitionRequest transition = RestFactory.eINSTANCE.createStageTransitionRequest();
		transition.setObjectId(TEST_PACKAGE_NSURI);
		transition.setTargetStage(TestAnnotations.STAGE_APPROVED);

		xmiContent = TestHelper.serializeToXMI(transition, resourceSet);

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("actions").path("transition").queryParam("mediaType", "application/xml")
				.request("application/xmi").post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
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

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		response = schemaTarget().path("search")
				.queryParam("name", TEST_PACKAGE_NAME)
				.request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

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

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		response = schemaTarget().path("search")
				.queryParam("nsUri", "schema")
				.request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

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

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		response = schemaTarget().path("search")
				.queryParam("classifier", "Sensor")
				.request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
	}

	@Test
	@ParentScopeServiceSetup
	public void testSearchPackages_NoResults(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		Response response = schemaTarget().path("search")
				.queryParam("classifier", "DoesNotExist")
				.request("application/json").get();

		assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content when no matches");
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
			assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
		}

		Response response = schemaTarget().path("search")
				.queryParam("nsUri", "test.example.com")
				.queryParam("limit", 2)
				.queryParam("offset", 0)
				.request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
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
		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		uri = "http://test.example.com/schema-approved/1.1";
		name = "schema-approved";
		testPackage = TestHelper.createTestEPackage(uri, name, name);
		xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		response = schemaStageTarget(TestAnnotations.STAGE_APPROVED).queryParam("nsUri", uri)
				.queryParam("name", name).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));
		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		response = schemaTarget().path("search")
				.queryParam("stage", TestAnnotations.STAGE_DRAFT)
				.request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
		assertEquals("1", response.getHeaderString("X-Total-Count"), "Should find only draft package");
	}

	@Test
	@ParentScopeServiceSetup
	public void testSearchPackages_ScopeNotFound(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		Response response = schemaTarget("non-existent-scope").path("search")
				.queryParam("classifier", "Something")
				.request("application/json").get();

		assertEquals(400, response.getStatus(), "Should return HTTP 400 Bad Request for unknown scope");
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

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");


		response = schemaTarget().path("search")
				.queryParam("featureName", "temperature")
				.request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
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

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		response = schemaTarget().path("search")
				.queryParam("featureType", "EInt")
				.request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
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
