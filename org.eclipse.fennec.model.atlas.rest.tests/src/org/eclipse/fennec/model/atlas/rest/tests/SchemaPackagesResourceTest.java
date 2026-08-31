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
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
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

	private static final String HREF_PACKAGE_NSURI = "http://test.example.com/hreftest/1.0";
	private static final String HREF_PACKAGE_NAME = "hreftest";

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

	// P5-7: stage-free final-stage metadata for a single package via GET /{s}/schema?nsUri=

	@Test
	@ParentScopeServiceSetup
	public void testListReleasedPackages_WithNsUri_ReturnsSingleMetadata(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		schemaStageTarget(TestAnnotations.STAGE_RELEASE).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		Response response = schemaTarget().queryParam("nsUri", TEST_PACKAGE_NSURI).request("application/json").get();

		assertStatus(200, response, "Should return HTTP 200 OK with the single final-stage metadata");
		String body = response.readEntity(String.class);
		assertNotNull(body, "Should return metadata");
		assertNotNull(response.getHeaderString("ETag"), "Final-stage metadata GET should emit an ETag");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListReleasedPackages_WithNsUri_NotFound(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		Response response = schemaTarget().queryParam("nsUri", "http://non-existent.com/schema/1.0")
				.request("application/json").get();

		assertStatus(204, response, "Should return HTTP 204 No Content when the package is not in the final stage");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListReleasedPackages_WithNsUri_InheritsFromParent(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		// Upload only to the PARENT scope's final stage; the child's stage-free listing must read through.
		Response upload = schemaStageTarget(TestAnnotations.TEST_PARENT_SCOPE_NAME, TestAnnotations.STAGE_RELEASE)
				.queryParam("nsUri", TEST_PACKAGE_NSURI).queryParam("name", TEST_PACKAGE_NAME)
				.request("application/xmi").post(Entity.entity(xmiContent, "application/xmi"));
		assertStatus(201, upload, "Upload to parent scope final stage should succeed");

		Response response = schemaTarget().queryParam("nsUri", TEST_PACKAGE_NSURI).request("application/json").get();

		assertStatus(200, response, "Child stage-free metadata should read through to the parent scope (P5-7)");
		assertNotNull(response.readEntity(String.class), "Should return inherited metadata");
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

	@Test
	@ParentScopeServiceSetup
	public void testCreatePackage_ComputesFingerprint(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		// Create: response metadata carries a server-computed, scheme-prefixed fingerprint
		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));
		assertStatus(201, response, "Should return HTTP 201 Created");
		String createdFingerprint = extractFingerprint(response.readEntity(String.class));
		assertNotNull(createdFingerprint, "created metadata must carry the model fingerprint");
		assertTrue(createdFingerprint.startsWith("fp1:"),
				"fingerprint should use the current scheme tag, was: " + createdFingerprint);

		// Overwrite with IDENTICAL content: fingerprint stays the same (reproducible)
		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).queryParam("overwrite", true).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));
		assertStatus(200, response, "Overwrite with identical content should succeed");
		assertEquals(createdFingerprint, extractFingerprint(response.readEntity(String.class)),
				"identical content must keep the identical fingerprint");

		// Overwrite with CHANGED content: fingerprint changes (identifying)
		EPackage changedPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME,
				TEST_PACKAGE_NAME);
		EClass extraClass = EcoreFactory.eINSTANCE.createEClass();
		extraClass.setName("ExtraClass");
		changedPackage.getEClassifiers().add(extraClass);
		String changedXmi = TestHelper.serializeToXMI(changedPackage, resourceSet);

		response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).queryParam("overwrite", true).request("application/xmi")
				.post(Entity.entity(changedXmi, "application/xmi"));
		assertStatus(200, response, "Overwrite with changed content should succeed");
		String changedFingerprint = extractFingerprint(response.readEntity(String.class));
		assertNotNull(changedFingerprint);
		assertNotEquals(createdFingerprint, changedFingerprint, "changed content must change the fingerprint");
	}

	/** Extracts the fingerprint attribute from a metadata XMI response, or null. */
	private static String extractFingerprint(String metadataXmi) {
		java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("fingerprint=\"([^\"]+)\"")
				.matcher(metadataXmi);
		return matcher.find() ? matcher.group(1) : null;
	}

	private static String extractObjectId(String metadataXmi) {
		java.util.regex.Matcher matcher = java.util.regex.Pattern.compile("objectId=\"([^\"]+)\"")
				.matcher(metadataXmi);
		return matcher.find() ? matcher.group(1) : null;
	}

	// ========== objectId Decoupling Tests (F8) ==========

	@Test
	@ParentScopeServiceSetup
	public void testCreatePackage_ObjectIdIsUuid_AndLocationCarriesNsUri(@InjectBundleContext BundleContext context)
			throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));
		assertStatus(201, response, "Should return HTTP 201 Created");

		String objectId = extractObjectId(response.readEntity(String.class));
		assertNotNull(objectId, "created metadata must carry an objectId");
		// Opaque UUID — no meaning derivable from the id shape anymore
		java.util.UUID.fromString(objectId);

		String location = response.getHeaderString("Location");
		assertNotNull(location, "created response must carry a Location header");
		assertTrue(location.contains("/schema/stages/" + TestAnnotations.STAGE_DRAFT),
				"Location should point at the stage listing endpoint, was: " + location);
		assertTrue(location.contains("nsUri=" + java.net.URLEncoder.encode(TEST_PACKAGE_NSURI, java.nio.charset.StandardCharsets.UTF_8)),
				"Location should carry the percent-encoded nsUri query, was: " + location);
	}

	@Test
	@ParentScopeServiceSetup
	public void testCreatePackage_SameNsUriInTwoStages_DistinctObjectIds(@InjectBundleContext BundleContext context)
			throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response draft = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));
		assertStatus(201, draft, "Draft upload should succeed");

		Response approved = schemaStageTarget(TestAnnotations.STAGE_APPROVED).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));
		assertStatus(201, approved, "Approved upload of the same nsUri should succeed");

		String draftId = extractObjectId(draft.readEntity(String.class));
		String approvedId = extractObjectId(approved.readEntity(String.class));
		assertNotNull(draftId);
		assertNotNull(approvedId);
		assertNotEquals(draftId, approvedId,
				"same nsURI in two stages must get distinct objectIds — audit trails must not merge");
	}

	@Test
	@ParentScopeServiceSetup
	public void testTransitionPackage_ByRealObjectId_KeepsObjectIdStable(@InjectBundleContext BundleContext context)
			throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response created = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));
		assertStatus(201, created, "Should return HTTP 201 Created");
		String createdId = extractObjectId(created.readEntity(String.class));
		assertNotNull(createdId);

		// Transition addressed by the REAL objectId (not the legacy nsUri-in-objectId shape)
		StageTransitionRequest transition = RestFactory.eINSTANCE.createStageTransitionRequest();
		transition.setObjectId(createdId);
		transition.setTargetStage(TestAnnotations.STAGE_APPROVED);
		String transitionXmi = TestHelper.serializeToXMI(transition, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("actions").path("transition")
				.request("application/xmi").post(Entity.entity(transitionXmi, "application/xmi"));
		assertStatus(200, response, "Transition by real objectId should succeed");

		String transitionedId = extractObjectId(response.readEntity(String.class));
		assertEquals(createdId, transitionedId,
				"the objectId is the lifecycle audit trail and must stay stable across stage transitions");
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

	// ========== Final Stage Policy Tests ==========

	/**
	 * A writable final stage still accepts a package that is not there yet.
	 */
	@Test
	@ParentScopeServiceSetup
	public void testCreatePackage_NewInFinalStage_Returns201(@InjectBundleContext BundleContext context)
			throws Exception {
		ensureResourceAvailability(context);

		Response response = postPackage(TestAnnotations.STAGE_RELEASE, TEST_PACKAGE_NAME, null);

		assertStatus(201, response, "A new package in a writable final stage should still be created");
	}

	/**
	 * Updating what is already in the final stage is refused by the registry's stage
	 * policy, not broken by it: the client has to be able to tell a rule it violated
	 * from a server that failed, so the answer is 403 and names stage and registry.
	 */
	@Test
	@ParentScopeServiceSetup
	public void testCreatePackage_OverwriteInFinalStage_Returns403(@InjectBundleContext BundleContext context)
			throws Exception {
		ensureResourceAvailability(context);
		assertStatus(201, postPackage(TestAnnotations.STAGE_RELEASE, TEST_PACKAGE_NAME, null),
				"Should return HTTP 201 Created");

		Response response = postPackage(TestAnnotations.STAGE_RELEASE, "UpdatedSchema", true);

		assertStatus(403, response, "Updating a package in a final stage should be refused, not fail");
		String body = response.readEntity(String.class);
		assertTrue(body.contains(TestAnnotations.STAGE_RELEASE),
				"The refusal should name the stage it applies to: " + body);
		assertTrue(body.contains("schema"), "The refusal should name the registry it applies to: " + body);
	}

	/**
	 * Without {@code overwrite} the same second upload is a plain conflict, and stays
	 * one — the stage policy only speaks for requests that actually mean to update.
	 */
	@Test
	@ParentScopeServiceSetup
	public void testCreatePackage_ExistingInFinalStageWithoutOverwrite_Returns409(
			@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		assertStatus(201, postPackage(TestAnnotations.STAGE_RELEASE, TEST_PACKAGE_NAME, null),
				"Should return HTTP 201 Created");

		Response response = postPackage(TestAnnotations.STAGE_RELEASE, TEST_PACKAGE_NAME, false);

		assertStatus(409, response, "An existing package without overwrite should still be a conflict");
	}

	/**
	 * A stage that is not final keeps accepting updates.
	 */
	@Test
	@ParentScopeServiceSetup
	public void testCreatePackage_OverwriteInNonFinalStage_Returns200(@InjectBundleContext BundleContext context)
			throws Exception {
		ensureResourceAvailability(context);
		assertStatus(201, postPackage(TestAnnotations.STAGE_DRAFT, TEST_PACKAGE_NAME, null),
				"Should return HTTP 201 Created");

		Response response = postPackage(TestAnnotations.STAGE_DRAFT, "UpdatedSchema", true);

		assertStatus(200, response, "Updating a package in a non-final stage should succeed");
	}

	/**
	 * Posts {@link #TEST_PACKAGE_NSURI} under {@code packageName} to {@code stage}.
	 *
	 * @param stage       the stage to post to
	 * @param packageName the package's name, so a second post can carry other content
	 * @param overwrite   the {@code overwrite} query parameter, or {@code null} to omit it
	 */
	private Response postPackage(String stage, String packageName, Boolean overwrite) throws IOException {
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, packageName, "test");
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		WebTarget target = schemaStageTarget(stage).queryParam("nsUri", TEST_PACKAGE_NSURI).queryParam("name",
				packageName);
		if (overwrite != null) {
			target = target.queryParam("overwrite", overwrite);
		}
		return target.request("application/xmi").post(Entity.entity(xmiContent, "application/xmi"));
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
	public void testSearchPackages_AfterTransition_StaysSearchableInNewStage(@InjectBundleContext BundleContext context)
			throws Exception {
		ensureResourceAvailability(context);
		// Own nsURI/name so leftovers of other tests cannot satisfy the assertions.
		String nsUri = "http://test.example.com/schema/transitionsearch/1.0";
		String name = "TransitionSearchPackage";
		EPackage testPackage = TestHelper.createTestEPackage(nsUri, name, name);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response upload = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", nsUri)
				.queryParam("name", name).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));
		assertStatus(201, upload, "Should return HTTP 201 Created");
		String objectId = extractObjectId(upload.readEntity(String.class));

		// Control: searchable while it still sits in the stage it was uploaded to.
		Response beforeTransition = schemaTarget().path("search").queryParam("name", name)
				.request("application/json").get();
		assertStatus(200, beforeTransition, "Search should find the uploaded package");
		assertTrue(beforeTransition.readEntity(String.class).contains(name),
				"Search should return the package it just indexed");

		StageTransitionRequest transition = RestFactory.eINSTANCE.createStageTransitionRequest();
		transition.setObjectId(objectId);
		transition.setTargetStage(TestAnnotations.STAGE_APPROVED);
		Response transitioned = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("actions").path("transition")
				.request("application/xmi")
				.post(Entity.entity(TestHelper.serializeToXMI(transition, resourceSet), "application/xmi"));
		assertStatus(200, transitioned, "Transition should succeed");

		// A search hit is resolved against the stage recorded in the index. If the transition
		// leaves that stage stale, the lookup hits the stage the object has left, comes back
		// null and the hit is dropped without a trace — the package vanishes from search.
		Response afterTransition = schemaTarget().path("search").queryParam("name", name)
				.request("application/json").get();
		assertStatus(200, afterTransition, "Search should still answer after a transition");
		String body = afterTransition.readEntity(String.class);
		assertTrue(body.contains(name), "A transitioned package must stay searchable");
		assertTrue(body.contains(TestAnnotations.STAGE_APPROVED),
				"The search result must report the stage the package now lives in");
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

	// ========== DCAT Publication Flag Tests (D0) ==========

	/**
	 * Reads the {@code dcat} entry out of the {@code properties} map of a JSON
	 * ObjectMetadata response. Returns {@code null} when the key is absent, which is
	 * what distinguishes "never written" from "written false".
	 */
	private static Boolean extractDcatFlag(String metadataJson) {
		// The value may arrive as a JSON boolean or as a quoted string, depending on how
		// the codec renders an EJavaObject map value — accept either and normalise.
		java.util.regex.Matcher matcher = java.util.regex.Pattern
				.compile("\"dcat\"\\s*:\\s*\"?(true|false)\"?").matcher(metadataJson);
		return matcher.find() ? Boolean.valueOf(matcher.group(1)) : null;
	}

	private String draftMetadataJson() {
		Response metadata = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.request("application/json").get();
		assertStatus(200, metadata, "Metadata GET should return HTTP 200 OK");
		return metadata.readEntity(String.class);
	}

	@Test
	@ParentScopeServiceSetup
	public void testCreatePackage_DcatTrue_StoredInMetadata(@InjectBundleContext BundleContext context)
			throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).queryParam("dcat", "true").request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));
		assertStatus(201, response, "Should return HTTP 201 Created");

		assertEquals(Boolean.TRUE, extractDcatFlag(draftMetadataJson()),
				"?dcat=true must be recorded in ObjectMetadata.properties — the publisher reads it from there");
	}

	@Test
	@ParentScopeServiceSetup
	public void testCreatePackage_NoDcatParam_RecordsFalse(@InjectBundleContext BundleContext context)
			throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));
		assertStatus(201, response, "Should return HTTP 201 Created");

		assertEquals(Boolean.FALSE, extractDcatFlag(draftMetadataJson()),
				"an upload without ?dcat must record the flag as false, not leave it absent");
	}

	@Test
	@ParentScopeServiceSetup
	public void testUpdatePackageContent_PreservesDcatFlag(@InjectBundleContext BundleContext context)
			throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		assertStatus(201,
				schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
						.queryParam("name", TEST_PACKAGE_NAME).queryParam("dcat", "true").request("application/xmi")
						.post(Entity.entity(xmiContent, "application/xmi")),
				"Setup upload should return HTTP 201 Created");

		EPackage updated = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, "DcatUpdated", "dcatupd");
		Response contentUpdate = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("nsUri", TEST_PACKAGE_NSURI).request("application/xmi")
				.put(Entity.entity(TestHelper.serializeToXMI(updated, resourceSet), "application/xmi"));
		assertStatus(200, contentUpdate, "Content update should return HTTP 200 OK");

		assertEquals(Boolean.TRUE, extractDcatFlag(draftMetadataJson()),
				"a content edit must not silently unpublish the model");
	}

	@Test
	@ParentScopeServiceSetup
	public void testTransitionPackage_PreservesDcatFlag(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		Response created = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).queryParam("dcat", "true").request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));
		assertStatus(201, created, "Setup upload should return HTTP 201 Created");

		StageTransitionRequest transition = RestFactory.eINSTANCE.createStageTransitionRequest();
		transition.setObjectId(extractObjectId(created.readEntity(String.class)));
		transition.setTargetStage(TestAnnotations.STAGE_APPROVED);
		Response transitioned = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("actions").path("transition")
				.request("application/xmi")
				.post(Entity.entity(TestHelper.serializeToXMI(transition, resourceSet), "application/xmi"));
		assertStatus(200, transitioned, "Transition should return HTTP 200 OK");

		Response metadata = schemaStageTarget(TestAnnotations.STAGE_APPROVED).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.request("application/json").get();
		assertStatus(200, metadata, "Metadata GET in the target stage should return HTTP 200 OK");
		assertEquals(Boolean.TRUE, extractDcatFlag(metadata.readEntity(String.class)),
				"promotion must carry the flag into the target stage — that is how a model reaches the portal");
	}

	@Test
	@ParentScopeServiceSetup
	public void testCreatePackage_OverwriteWithoutDcatParam_PreservesFlag(@InjectBundleContext BundleContext context)
			throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		assertStatus(201,
				schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
						.queryParam("name", TEST_PACKAGE_NAME).queryParam("dcat", "true").request("application/xmi")
						.post(Entity.entity(TestHelper.serializeToXMI(testPackage, resourceSet), "application/xmi")),
				"Setup upload should return HTTP 201 Created");

		EPackage updated = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, "DcatOverwrite", "dcatow");
		Response overwrite = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).queryParam("overwrite", "true").request("application/xmi")
				.post(Entity.entity(TestHelper.serializeToXMI(updated, resourceSet), "application/xmi"));
		assertStatus(200, overwrite, "Overwrite should return HTTP 200 OK");

		assertEquals(Boolean.TRUE, extractDcatFlag(draftMetadataJson()),
				"an overwrite that says nothing about dcat must leave the flag alone, not default it to false");
	}

	@Test
	@ParentScopeServiceSetup
	public void testCreatePackage_OverwriteWithDcatFalse_ClearsFlag(@InjectBundleContext BundleContext context)
			throws Exception {
		ensureResourceAvailability(context);
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		assertStatus(201,
				schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
						.queryParam("name", TEST_PACKAGE_NAME).queryParam("dcat", "true").request("application/xmi")
						.post(Entity.entity(TestHelper.serializeToXMI(testPackage, resourceSet), "application/xmi")),
				"Setup upload should return HTTP 201 Created");

		EPackage updated = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, "DcatCleared", "dcatclr");
		Response overwrite = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME).queryParam("overwrite", "true").queryParam("dcat", "false")
				.request("application/xmi")
				.post(Entity.entity(TestHelper.serializeToXMI(updated, resourceSet), "application/xmi"));
		assertStatus(200, overwrite, "Overwrite should return HTTP 200 OK");

		assertEquals(Boolean.FALSE, extractDcatFlag(draftMetadataJson()),
				"an explicit ?dcat=false on overwrite must clear the flag");
	}

	// ========== Metadata PATCH endpoint (7b) ==========

	/**
	 * PATCH through {@code java.net.http}, not the Jakarta RS client: Jersey's default connector
	 * rejects PATCH outright unless a workaround property is set, which would make these tests fail
	 * at the client for a reason that has nothing to do with the endpoint.
	 */
	private static java.net.http.HttpResponse<String> patch(java.net.URI uri) throws Exception {
		java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder(uri)
				.method("PATCH", java.net.http.HttpRequest.BodyPublishers.noBody())
				.header("Accept", "application/json").build();
		return java.net.http.HttpClient.newHttpClient().send(request,
				java.net.http.HttpResponse.BodyHandlers.ofString());
	}

	private java.net.URI metadataUri(String stage, String... params) {
		WebTarget target = schemaStageTarget(stage).path("metadata").queryParam("nsUri", TEST_PACKAGE_NSURI);
		for (int i = 0; i < params.length; i += 2) {
			target = target.queryParam(params[i], params[i + 1]);
		}
		return target.getUri();
	}

	private void uploadDraft(String... extraParams) throws Exception {
		EPackage testPackage = TestHelper.createTestEPackage(TEST_PACKAGE_NSURI, TEST_PACKAGE_NAME, TEST_PACKAGE_NAME);
		String xmiContent = TestHelper.serializeToXMI(testPackage, resourceSet);
		WebTarget target = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", TEST_PACKAGE_NSURI)
				.queryParam("name", TEST_PACKAGE_NAME);
		for (int i = 0; i < extraParams.length; i += 2) {
			target = target.queryParam(extraParams[i], extraParams[i + 1]);
		}
		Response response = target.request("application/xmi").post(Entity.entity(xmiContent, "application/xmi"));
		assertStatus(201, response, "setup: the package should upload");
	}

	@Test
	@ParentScopeServiceSetup
	public void testPatchMetadata_SetsTheDcatFlagWithoutReuploading(@InjectBundleContext BundleContext context)
			throws Exception {
		ensureResourceAvailability(context);
		uploadDraft();
		assertEquals(Boolean.FALSE, extractDcatFlag(draftMetadataJson()), "setup: the flag starts false");

		java.net.http.HttpResponse<String> response = patch(metadataUri(TestAnnotations.STAGE_DRAFT, "dcat", "true"));

		assertEquals(200, response.statusCode(), "PATCH should return HTTP 200 OK, body: " + response.body());
		// The whole point of the endpoint: re-uploading the content was the only way to change this.
		assertEquals(Boolean.TRUE, extractDcatFlag(draftMetadataJson()),
				"PATCH ?dcat=true must record the flag in ObjectMetadata.properties");
	}

	@Test
	@ParentScopeServiceSetup
	public void testPatchMetadata_ClearsTheDcatFlag(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		uploadDraft("dcat", "true");
		assertEquals(Boolean.TRUE, extractDcatFlag(draftMetadataJson()), "setup: the flag starts true");

		assertEquals(200, patch(metadataUri(TestAnnotations.STAGE_DRAFT, "dcat", "false")).statusCode(),
				"PATCH should return HTTP 200 OK");

		assertEquals(Boolean.FALSE, extractDcatFlag(draftMetadataJson()),
				"clearing the flag is what retires a published Dataset, so it has to reach the metadata");
	}

	@Test
	@ParentScopeServiceSetup
	public void testPatchMetadata_RefusesAnIdentityFieldByName(@InjectBundleContext BundleContext context)
			throws Exception {
		ensureResourceAvailability(context);
		uploadDraft();

		java.net.http.HttpResponse<String> response = patch(
				metadataUri(TestAnnotations.STAGE_DRAFT, "dcat", "true", "objectId", "something-else"));

		// Named, never silently ignored: quietly dropping half a request is how somebody comes to
		// believe they changed a publisher when they did not.
		assertEquals(400, response.statusCode(), "an identity field must be refused");
		assertTrue(response.body().contains("objectId"), "the refusal should name the field, was: " + response.body());
		assertEquals(Boolean.FALSE, extractDcatFlag(draftMetadataJson()),
				"a refused request must change nothing at all");
	}

	@Test
	@ParentScopeServiceSetup
	public void testPatchMetadata_RefusesAnUnknownField(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		uploadDraft();

		// A typo that silently changes nothing is the same failure as a refusal that says nothing.
		java.net.http.HttpResponse<String> response = patch(
				metadataUri(TestAnnotations.STAGE_DRAFT, "dcatt", "true"));

		assertEquals(400, response.statusCode(), "an unknown field must be refused");
		assertTrue(response.body().contains("dcatt"), "the refusal should name the field, was: " + response.body());
		// nsUri says which package to edit; it is identity and is never written. A refusal that
		// listed it among the editable fields would invite exactly the write this endpoint forbids.
		assertFalse(response.body().contains("nsUri"),
				"the list of editable fields must not include the selector, was: " + response.body());
	}

	@Test
	@ParentScopeServiceSetup
	public void testPatchMetadata_RequiresSomethingToChange(@InjectBundleContext BundleContext context)
			throws Exception {
		ensureResourceAvailability(context);
		uploadDraft();

		assertEquals(400, patch(metadataUri(TestAnnotations.STAGE_DRAFT)).statusCode(),
				"a PATCH that asks for no change should say so rather than answer 200");
	}

	@Test
	@ParentScopeServiceSetup
	public void testPatchMetadata_UnknownPackageIs404(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);

		java.net.http.HttpResponse<String> response = patch(metadataUri(TestAnnotations.STAGE_DRAFT, "dcat", "true"));

		assertEquals(404, response.statusCode(), "no such package in that stage");
	}
	
	// ========== Intra-Package Reference Serialization Tests ==========

	/**
	 * A package whose classifiers reference each other must be served the way it
	 * was stored: an intra-package reference stays a bare fragment
	 * ({@code #//Inner}), because a document that names its own file in its hrefs
	 * only resolves as long as the caller saves it under exactly that file name.
	 */
	@Test
	@ParentScopeServiceSetup
	public void testGetPackageContent_KeepsIntraPackageReferenceAsFragment(@InjectBundleContext BundleContext context)
			throws Exception {
		ensureResourceAvailability(context);
		String xmiContent = TestHelper.serializeToXMI(createIntraReferencePackage(), resourceSet);

		Response created = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", HREF_PACKAGE_NSURI)
				.queryParam("name", HREF_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));
		assertStatus(201, created, "Should return HTTP 201 Created");

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("nsUri", HREF_PACKAGE_NSURI).request("application/xmi").get();
		assertStatus(200, response, "Should return HTTP 200 OK");

		String served = response.readEntity(String.class);
		assertFalse(served.contains(HREF_PACKAGE_NAME + ".ecore#//Inner"),
				"Intra-package reference must not be rewritten into a file-name-relative href: " + served);
		assertTrue(served.contains("eType=\"#//Inner\""),
				"Intra-package reference should be served as the stored fragment #//Inner: " + served);
		assertTrue(served.contains("http://www.eclipse.org/emf/2002/Ecore#//EInt"),
				"Cross-package reference should stay absolute: " + served);
		assertEquals("attachment; filename=" + HREF_PACKAGE_NAME + ".ecore",
				response.getHeaderString("Content-Disposition"),
				"Content-Disposition should still suggest the package file name");
	}

	/**
	 * The served document has to load under any file name the caller picks, so its
	 * intra-package reference must resolve without the response ever having been
	 * saved as {@code hreftest.ecore}.
	 */
	@Test
	@ParentScopeServiceSetup
	public void testGetPackageContent_ServedDocumentResolvesUnderAnyFileName(@InjectBundleContext BundleContext context)
			throws Exception {
		ensureResourceAvailability(context);
		String xmiContent = TestHelper.serializeToXMI(createIntraReferencePackage(), resourceSet);

		Response created = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", HREF_PACKAGE_NSURI)
				.queryParam("name", HREF_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));
		assertStatus(201, created, "Should return HTTP 201 Created");

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("nsUri", HREF_PACKAGE_NSURI).request("application/xmi").get();
		assertStatus(200, response, "Should return HTTP 200 OK");

		// Loaded under a file name that has nothing to do with the package name.
		EPackage reloaded = (EPackage) TestHelper.deserializeFromXMI(response.readEntity(String.class), resourceSet);
		EClass outer = (EClass) reloaded.getEClassifier("Outer");
		assertNotNull(outer, "Served package should contain the Outer class");
		EClassifier innerType = ((EReference) outer.getEStructuralFeature("inner")).getEType();
		assertNotNull(innerType, "Reference type should be present");
		assertFalse(innerType.eIsProxy(),
				"Intra-package reference should resolve within the served document, not dangle as a proxy");
		assertEquals(reloaded.getEClassifier("Inner"), innerType,
				"Reference should point at the Inner class of the served package");
	}

	/**
	 * The UML representation of the same package must keep the reference between its
	 * two classes inside the served document, rather than routing it through the file
	 * name the download is suggested to be saved as.
	 */
	@Test
	@ParentScopeServiceSetup
	public void testGetPackageContent_UmlKeepsIntraModelReferenceInsideTheDocument(
			@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		String xmiContent = TestHelper.serializeToXMI(createIntraReferencePackage(), resourceSet);

		Response created = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", HREF_PACKAGE_NSURI)
				.queryParam("name", HREF_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));
		assertStatus(201, created, "Should return HTTP 201 Created");

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("nsUri", HREF_PACKAGE_NSURI).request("application/uml").get();
		assertStatus(200, response, "Should return HTTP 200 OK");

		String served = response.readEntity(String.class);
		assertTrue(served.contains("uml:"), "Should be served as a UML document: " + served);
		assertFalse(served.contains(HREF_PACKAGE_NAME + ".uml"),
				"The served model must not reference its own file name: " + served);
	}

	/**
	 * The XSD representation of the same package must be served as a schema document
	 * that keeps the reference between its two types inside itself.
	 */
	@Test
	@ParentScopeServiceSetup
	public void testGetPackageContent_XsdKeepsIntraSchemaReferenceInsideTheDocument(
			@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		String xmiContent = TestHelper.serializeToXMI(createIntraReferencePackage(), resourceSet);

		Response created = schemaStageTarget(TestAnnotations.STAGE_DRAFT).queryParam("nsUri", HREF_PACKAGE_NSURI)
				.queryParam("name", HREF_PACKAGE_NAME).request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));
		assertStatus(201, created, "Should return HTTP 201 Created");

		Response response = schemaStageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("nsUri", HREF_PACKAGE_NSURI).request("application/schema+xml").get();
		assertStatus(200, response, "Should return HTTP 200 OK");

		String served = response.readEntity(String.class);
		// The closing quote matters: the XMI serialization of the XSD metamodel declares
		// "…/XMLSchema-instance", which carries this namespace as a prefix.
		assertTrue(served.contains("\"http://www.w3.org/2001/XMLSchema\""),
				"Should be served as an XML Schema document, not as XMI of the XSD model: " + served);
		assertFalse(served.contains(HREF_PACKAGE_NAME + ".xsd"),
				"The served schema must not reference its own file name: " + served);
	}

	/**
	 * Creates a package holding both reference kinds the serialization has to keep
	 * apart: {@code Outer.inner} points at a classifier of the very same package,
	 * {@code Inner.v} at Ecore's {@code EInt} in another one.
	 */
	private static EPackage createIntraReferencePackage() {
		EPackage ePackage = TestHelper.createTestEPackage(HREF_PACKAGE_NSURI, HREF_PACKAGE_NAME, HREF_PACKAGE_NAME);
		EClass inner = TestHelper.createTestEClass("Inner");
		inner.getEStructuralFeatures().add(TestHelper.createTestEAttribute("v"));
		EClass outer = TestHelper.createTestEClass("Outer");
		EReference reference = EcoreFactory.eINSTANCE.createEReference();
		reference.setName("inner");
		reference.setEType(inner);
		reference.setContainment(true);
		outer.getEStructuralFeatures().add(reference);
		ePackage.getEClassifiers().add(inner);
		ePackage.getEClassifiers().add(outer);
		return ePackage;
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
