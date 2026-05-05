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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.model.atlas.datagen.example.model.dge.Person;
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
 * Integration tests for ObjectRegistryResource REST endpoints.
 *
 * <p>
 * Tests cover:
 * </p>
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
public class ObjectRegistryResourceTest extends AbstractRestTest{

	private static final String TEST_OBJECT_ID = "test-object-123";
	private static final String TEST_OBJECT_NAME = "TestObject";
	public static final String UNASSIGNED_REGISTRY_NAME = "unassigned-registry";

	

	// ========== List All Objects Tests ==========

	@Test
	@ParentScopeServiceSetup
	public void testListAllObjects_Success(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);

		Response response = objectRegistryTarget().path("all").request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return content");
		assertTrue(responseContent.contains("metadata"), "Response should contain metadata");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListAllObjects_ScopeNotFound(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		Response response = registryTarget("non-existent-scope", TestAnnotations.OBJECT_REGISTRY_NAME)
				.path("all").request("application/json").get();

		assertEquals(400, response.getStatus(), "Should return HTTP 400 Bad Request");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListAllObjects_UnassignedRegistry(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		assertUnassignedRegistryRejected(
				registryTarget(UNASSIGNED_REGISTRY_NAME).path("all").request("application/json").get());
	}

	@Test
	@ParentScopeServiceSetup
	public void testListAllObjects_WithMediaTypeQueryParam(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);
		Response response = objectRegistryTarget().path("all")
				.queryParam("mediaType", "application/xml")
				.request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
		assertEquals("application/xml", response.getHeaderString("Content-Type"),
				"Content-Type header should be set to mediaType query parameter value");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListAllObjects_WithUnsupportedMediaTypeQueryParam(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		Response response = objectRegistryTarget().path("all")
				.queryParam("mediaType", "application/unsupported")
				.request("application/json").get();

		assertEquals(415, response.getStatus(), "Should return HTTP 415 Unsupported Media Type");
	}

	// ========== List Operations Tests ==========

	@Test
	@ParentScopeServiceSetup
	public void testListReleasedObjects_Success(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_RELEASE);
		Response response = objectRegistryTarget().request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return content");
		assertTrue(responseContent.contains("metadata"), "Response should contain metadata");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListReleasedObjects_ScopeNotFound(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		Response response = registryTarget("non-existent-scope", TestAnnotations.OBJECT_REGISTRY_NAME)
				.request("application/json").get();

		assertEquals(400, response.getStatus(), "Should return HTTP 400 Bad Request");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListObjectsInStage_Success(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);
		Response response = stageTarget(TestAnnotations.STAGE_DRAFT).request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return content");
		assertTrue(responseContent.contains("metadata"), "Response should contain metadata");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListObjectsInStage_WithObjectIdFilter(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);
		Response response = stageTarget(TestAnnotations.STAGE_DRAFT)
				.queryParam("objectId", TEST_OBJECT_ID)
				.request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return content");
		assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListObjectsInStage_WithNameFilter(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);
		Response response = stageTarget(TestAnnotations.STAGE_DRAFT)
				.queryParam("name", TEST_OBJECT_NAME)
				.request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return content");
		assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListObjectsInStage_NotFound(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		Response response = stageTarget(TestAnnotations.STAGE_DRAFT)
				.queryParam("objectId", "non-existent-object").request("application/json").get();

		assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content");
	}

	// ========== Create Object Tests ==========

	@Test
	@ParentScopeServiceSetup
	public void testCreateObject_Success(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);
	}

	@Test
	@ParentScopeServiceSetup
	public void testCreateObject_IncompatibleEClass(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		// Create an EObject that is NOT a Person (e.g., EClass)
		EClass incompatibleObject = EcoreFactory.eINSTANCE.createEClass();
		incompatibleObject.setName("IncompatibleClass");

		String xmiContent = TestHelper.serializeToXMI(incompatibleObject, resourceSet);

		Response response = stageTarget(TestAnnotations.STAGE_DRAFT).path("incompatible-object-id")
				.queryParam("name", "IncompatibleObject").queryParam("version", "1.0.0").request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(400, response.getStatus(), "Should return HTTP 400 Bad Request for incompatible EClass");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return error message");
		assertTrue(responseContent.contains("not compatible"), "Error message should mention incompatibility");
	}

	@Test
	@ParentScopeServiceSetup
	public void testCreateObject_UnknownRegistry(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		Person person = TestHelper.createTestObject();
		String xmiContent = TestHelper.serializeToXMI(person, resourceSet);

		Response response = stageTarget("unknown-registry", TestAnnotations.STAGE_DRAFT).path(TEST_OBJECT_ID)
				.queryParam("name", TEST_OBJECT_NAME).queryParam("version", "1.0.0").request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(400, response.getStatus(), "Should return HTTP 400 Bad Request for unknown registry");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return error message");
		assertTrue(responseContent.contains("Unknown") || responseContent.contains("registry"),
				"Error message should mention unknown registry");
	}

	@Test
	@ParentScopeServiceSetup
	public void testCreateObject_Conflict(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);

		Person person = TestHelper.createTestObject();
		String xmiContent = TestHelper.serializeToXMI(person, resourceSet);
		Response response = stageTarget(TestAnnotations.STAGE_DRAFT).path(TEST_OBJECT_ID)
				.queryParam("name", "TestObject").queryParam("version", "1.0.0").request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(409, response.getStatus(), "Should return HTTP 409 Conflict for duplicate object ID");
	}

	@Test
	@ParentScopeServiceSetup
	public void testCreateObject_WithOverrideSuccess(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);

		Person person = TestHelper.createTestObject();
		String xmiContent = TestHelper.serializeToXMI(person, resourceSet);

		Response response = stageTarget(TestAnnotations.STAGE_DRAFT).path(TEST_OBJECT_ID)
				.queryParam("name", "UpdatedTestObject").queryParam("version", "1.1.0").queryParam("override", "true")
				.request("application/xmi").post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK when override is true and object exists");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return updated metadata");
		assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
	}

	@Test
	@ParentScopeServiceSetup
	public void testCreateObject_WithOverrideFalseConflict(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);

		Person person = TestHelper.createTestObject();
		String xmiContent = TestHelper.serializeToXMI(person, resourceSet);
		// Use an existing object ID with override=false

		Response response = stageTarget(TestAnnotations.STAGE_DRAFT).path(TEST_OBJECT_ID)
				.queryParam("name", "TestObject").queryParam("version", "1.0.0").queryParam("override", "false")
				.request("application/xmi").post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(409, response.getStatus(),
				"Should return HTTP 409 Conflict when override is false and object exists");
	}

	@Test
	@ParentScopeServiceSetup
	public void testCreateObject_WithOverrideNewObject(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);

		Person person = TestHelper.createTestObject();
		String xmiContent = TestHelper.serializeToXMI(person, resourceSet);
		// Use a non-existing object ID with override=true (should create new)
		Response response = stageTarget(TestAnnotations.STAGE_DRAFT).path("new-object-id")
				.queryParam("name", "NewObject").queryParam("version", "1.0.0").queryParam("override", "true")
				.request("application/xmi").post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(201, response.getStatus(),
				"Should return HTTP 201 Created when override is true and object doesn't exist");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return metadata");
		assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
	}

	// ========== Get Object Content Tests ==========

	@Test
	@ParentScopeServiceSetup
	public void testGetObjectContent_Success(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);
		Response response = stageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("objectId", TEST_OBJECT_ID).request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return content");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetObjectContent_NotFound(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		Response response = stageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("objectId", "non-existent-object").request("application/json").get();

		assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content");
	}

	// ========== Update Object Content Tests ==========

	@Test
	@ParentScopeServiceSetup
	public void testUpdateObjectContent_Success(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);

		Person person = TestHelper.createTestObject();
		String xmiContent = TestHelper.serializeToXMI(person, resourceSet);

		Response response = stageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("objectId", TEST_OBJECT_ID).queryParam("version", "1.1.0").request("application/xmi")
				.put(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return updated metadata");
		assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
	}

	@Disabled("We have to fix issue #64 first")
	@Test
	@ParentScopeServiceSetup
	public void testUpdateObjectContent_ReadOnlyStage(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		EPackage updatedObject = TestHelper.createTestEPackage("http://test.com/object/1.0", "UpdatedObject", "test");
		String xmiContent = TestHelper.serializeToXMI(updatedObject, resourceSet);

		Response response = stageTarget("readonly-stage").path("content")
				.queryParam("objectId", TEST_OBJECT_ID).queryParam("version", "1.1.0").request("application/xmi")
				.put(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(403, response.getStatus(), "Should return HTTP 403 Forbidden for read-only stage");
	}

	@Test
	@ParentScopeServiceSetup
	public void testUpdateObjectContent_NotFound(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		Person person = TestHelper.createTestObject();
		String xmiContent = TestHelper.serializeToXMI(person, resourceSet);

		Response response = stageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("objectId", TEST_OBJECT_ID).queryParam("version", "1.0.0").request("application/xmi")
				.put(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content when object not found");
	}

	// ========== Delete Object Tests ==========

	@Test
	@ParentScopeServiceSetup
	public void testDeleteObject_Success(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);
		Response response = stageTarget(TestAnnotations.STAGE_DRAFT)
				.queryParam("objectId", TEST_OBJECT_ID).request().delete();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
	}

	@Disabled("We have to fix issue #64 first")
	@Test
	@ParentScopeServiceSetup
	public void testDeleteObject_ReadOnlyStage(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		Response response = stageTarget("readonly-stage")
				.queryParam("objectId", TEST_OBJECT_ID).request().delete();

		assertEquals(403, response.getStatus(), "Should return HTTP 403 Forbidden");
	}

	@Test
	@ParentScopeServiceSetup
	public void testDeleteObject_NotFound(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		Response response = stageTarget(TestAnnotations.STAGE_DRAFT)
				.queryParam("objectId", TEST_OBJECT_ID).request().delete();

		assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content");
	}

	// ========== Transition Tests ==========

	@Test
	@ParentScopeServiceSetup
	public void testTransitionObject_Success(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);
		StageTransitionRequest transition = RestFactory.eINSTANCE.createStageTransitionRequest();
		transition.setObjectId(TEST_OBJECT_ID);
		transition.setTargetStage(TestAnnotations.STAGE_APPROVED);

		String xmiContent = TestHelper.serializeToXMI(transition, resourceSet);

		Response response = stageTarget(TestAnnotations.STAGE_DRAFT).path("actions").path("transition")
				.request("application/xmi").post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return transition metadata");
		assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
	}

	@Test
	@ParentScopeServiceSetup
	public void testTransitionObject_InvalidTransition(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);
		StageTransitionRequest transition = RestFactory.eINSTANCE.createStageTransitionRequest();
		transition.setObjectId(TEST_OBJECT_ID);
		transition.setTargetStage(TestAnnotations.STAGE_RELEASE); // Invalid: skipping approved stage

		String xmiContent = TestHelper.serializeToXMI(transition, resourceSet);

		Response response = stageTarget(TestAnnotations.STAGE_DRAFT).path("actions").path("transition")
				.request("application/xmi").post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(400, response.getStatus(), "Should return HTTP 400 Bad Request");
	}

	@Test
	@ParentScopeServiceSetup
	public void testTransitionObject_NotFound(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		StageTransitionRequest transition = RestFactory.eINSTANCE.createStageTransitionRequest();
		transition.setObjectId(TEST_OBJECT_ID);
		transition.setTargetStage(TestAnnotations.STAGE_APPROVED);

		String xmiContent = TestHelper.serializeToXMI(transition, resourceSet);

		Response response = stageTarget(TestAnnotations.STAGE_DRAFT).path("actions").path("transition")
				.request("application/xmi").post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content");
	}

	// ========== List Objects By Name Tests ==========

	@Test
	@ParentScopeServiceSetup
	public void testListObjectsInStageByName_ExactMatch(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);
		Response response = stageTarget(TestAnnotations.STAGE_DRAFT)
				.queryParam("name", TEST_OBJECT_NAME)
				.request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return content");
		assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
		assertTrue(responseContent.contains(TEST_OBJECT_NAME), "Response should contain the object name");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListObjectsInStageByName_WildcardMatch(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);
		Response response = stageTarget(TestAnnotations.STAGE_DRAFT)
				.queryParam("name", "Test*")
				.request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return content");
		assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListObjectsInStageByName_NotFound(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);
		Response response = stageTarget(TestAnnotations.STAGE_DRAFT)
				.queryParam("name", "NonExistentObject")
				.request("application/json").get();

		assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content when no objects match");
	}



	@Test
	@ParentScopeServiceSetup
	public void testListObjectsInStageByName_DifferentStage(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_APPROVED);
		Response response = stageTarget(TestAnnotations.STAGE_APPROVED)
				.queryParam("name", TEST_OBJECT_NAME)
				.request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return content");
		assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
	}

	// ========== Unassigned Registry Tests ==========

	/**
	 * Tests that accessing a registry which exists globally but is NOT configured
	 * for the given scope returns HTTP 400. Validated by the
	 * {@code ModelAtlasRequestFilter} via {@code ScopeService#isValidRegistry(String)}.
	 */
	@Test
	@ParentScopeServiceSetup
	public void testListReleasedObjects_UnassignedRegistry(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_RELEASE);
		assertUnassignedRegistryRejected(
				registryTarget(UNASSIGNED_REGISTRY_NAME).request("application/json").get());
	}

	@Test
	@ParentScopeServiceSetup
	public void testListObjectsInStage_UnassignedRegistry(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);
		assertUnassignedRegistryRejected(
				stageTarget(UNASSIGNED_REGISTRY_NAME, TestAnnotations.STAGE_DRAFT)
						.request("application/json").get());
	}

	@Test
	@ParentScopeServiceSetup
	public void testCreateObject_UnassignedRegistry(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		Person person = TestHelper.createTestObject();
		String xmiContent = TestHelper.serializeToXMI(person, resourceSet);

		assertUnassignedRegistryRejected(
				stageTarget(UNASSIGNED_REGISTRY_NAME, TestAnnotations.STAGE_DRAFT).path(TEST_OBJECT_ID)
						.queryParam("name", TEST_OBJECT_NAME).queryParam("version", "1.0.0")
						.request("application/xmi")
						.post(Entity.entity(xmiContent, "application/xmi")));
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetObjectContent_UnassignedRegistry(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);
		assertUnassignedRegistryRejected(
				stageTarget(UNASSIGNED_REGISTRY_NAME, TestAnnotations.STAGE_DRAFT).path("content")
						.queryParam("objectId", TEST_OBJECT_ID).request("application/json").get());
	}

	@Test
	@ParentScopeServiceSetup
	public void testUpdateObjectContent_UnassignedRegistry(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);
		Person person = TestHelper.createTestObject();
		String xmiContent = TestHelper.serializeToXMI(person, resourceSet);

		assertUnassignedRegistryRejected(
				stageTarget(UNASSIGNED_REGISTRY_NAME, TestAnnotations.STAGE_DRAFT).path("content")
						.queryParam("objectId", TEST_OBJECT_ID).queryParam("version", "1.1.0")
						.request("application/xmi")
						.put(Entity.entity(xmiContent, "application/xmi")));
	}

	@Test
	@ParentScopeServiceSetup
	public void testDeleteObject_UnassignedRegistry(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);
		assertUnassignedRegistryRejected(
				stageTarget(UNASSIGNED_REGISTRY_NAME, TestAnnotations.STAGE_DRAFT)
						.queryParam("objectId", TEST_OBJECT_ID).request().delete());
	}

	@Test
	@ParentScopeServiceSetup
	public void testTransitionObject_UnassignedRegistry(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);
		StageTransitionRequest transition = RestFactory.eINSTANCE.createStageTransitionRequest();
		transition.setObjectId(TEST_OBJECT_ID);
		transition.setTargetStage(TestAnnotations.STAGE_APPROVED);

		String xmiContent = TestHelper.serializeToXMI(transition, resourceSet);

		assertUnassignedRegistryRejected(
				stageTarget(UNASSIGNED_REGISTRY_NAME, TestAnnotations.STAGE_DRAFT)
						.path("actions").path("transition")
						.request("application/xmi")
						.post(Entity.entity(xmiContent, "application/xmi")));
	}

	// ========== MediaType Query Parameter Tests ==========

	@Test
	@ParentScopeServiceSetup
	public void testListReleasedObjects_WithMediaTypeQueryParam(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_RELEASE);
		Response response = objectRegistryTarget()
				.queryParam("mediaType", "application/xml")
				.request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
		assertEquals("application/xml", response.getHeaderString("Content-Type"),
				"Content-Type header should be set to mediaType query parameter value");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListReleasedObjects_WithUnsupportedMediaTypeQueryParam(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_RELEASE);
		Response response = objectRegistryTarget()
				.queryParam("mediaType", "application/unsupported")
				.request("application/json").get();

		assertEquals(415, response.getStatus(), "Should return HTTP 415 Unsupported Media Type");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListObjectsInStage_WithMediaTypeQueryParam(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);
		Response response = stageTarget(TestAnnotations.STAGE_DRAFT)
				.queryParam("mediaType", "application/xml")
				.request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
		assertEquals("application/xml", response.getHeaderString("Content-Type"),
				"Content-Type header should be set to mediaType query parameter value");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetObjectContent_WithMediaTypeQueryParam(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);
		Response response = stageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("objectId", TEST_OBJECT_ID).queryParam("mediaType", "application/xml")
				.request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
		assertEquals("application/xml", response.getHeaderString("Content-Type"),
				"Content-Type header should be set to mediaType query parameter value");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetObjectContent_WithUnsupportedMediaTypeQueryParam(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);
		Response response = stageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("objectId", TEST_OBJECT_ID).queryParam("mediaType", "application/unsupported")
				.request("application/json").get();

		assertEquals(415, response.getStatus(), "Should return HTTP 415 Unsupported Media Type");
	}

	@Test
	@ParentScopeServiceSetup
	public void testCreateObject_WithMediaTypeQueryParam(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		Person person = TestHelper.createTestObject();
		String xmiContent = TestHelper.serializeToXMI(person, resourceSet);

		Response response = stageTarget(TestAnnotations.STAGE_DRAFT).path(TEST_OBJECT_ID)
				.queryParam("name", TEST_OBJECT_NAME).queryParam("version", "1.0.0")
				.queryParam("mediaType", "application/xml").request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(201, response.getStatus(), "Should return HTTP 201 Created");
		assertEquals("application/xml", response.getHeaderString("Content-Type"),
				"Content-Type header should be set to mediaType query parameter value");
	}

	@Test
	@ParentScopeServiceSetup
	public void testUpdateObjectContent_WithMediaTypeQueryParam(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);
		Person person = TestHelper.createTestObject();
		String xmiContent = TestHelper.serializeToXMI(person, resourceSet);

		Response response = stageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("objectId", TEST_OBJECT_ID).queryParam("version", "1.1.0")
				.queryParam("mediaType", "application/xml").request("application/xmi")
				.put(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
		assertEquals("application/xml", response.getHeaderString("Content-Type"),
				"Content-Type header should be set to mediaType query parameter value");
	}

	@Test
	@ParentScopeServiceSetup
	public void testTransitionObject_WithMediaTypeQueryParam(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);
		StageTransitionRequest transition = RestFactory.eINSTANCE.createStageTransitionRequest();
		transition.setObjectId(TEST_OBJECT_ID);
		transition.setTargetStage(TestAnnotations.STAGE_APPROVED);

		String xmiContent = TestHelper.serializeToXMI(transition, resourceSet);

		Response response = stageTarget(TestAnnotations.STAGE_DRAFT).path("actions").path("transition")
				.queryParam("mediaType", "application/xml").request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
		assertEquals("application/xml", response.getHeaderString("Content-Type"),
				"Content-Type header should be set to mediaType query parameter value");
	}

	/** /{scope}/registries/{registry} */
	private WebTarget registryTarget(String scope, String registry) {
		return scopeTarget(scope).path("registries").path(registry);
	}

	/** /{TEST_SCOPE_NAME}/registries/{registry} */
	private WebTarget registryTarget(String registry) {
		return registryTarget(TestAnnotations.TEST_SCOPE_NAME, registry);
	}

	/** /{TEST_SCOPE_NAME}/registries/{OBJECT_REGISTRY_NAME} */
	private WebTarget objectRegistryTarget() {
		return registryTarget(TestAnnotations.OBJECT_REGISTRY_NAME);
	}

	/** /{TEST_SCOPE_NAME}/registries/{registry}/stages/{stage} */
	private WebTarget stageTarget(String registry, String stage) {
		return registryTarget(registry).path("stages").path(stage);
	}

	/** /{TEST_SCOPE_NAME}/registries/{OBJECT_REGISTRY_NAME}/stages/{stage} */
	private WebTarget stageTarget(String stage) {
		return stageTarget(TestAnnotations.OBJECT_REGISTRY_NAME, stage);
	}

	private static void assertUnassignedRegistryRejected(Response response) {
		assertEquals(400, response.getStatus(),
				"Should return HTTP 400 Bad Request for a registry not assigned to the scope");
	}

	private void uploadTestObject(String stage) throws IOException {
		Person person = TestHelper.createTestObject();
		String xmiContent = TestHelper.serializeToXMI(person, resourceSet);

		Response response = stageTarget(stage).path(TEST_OBJECT_ID)
				.queryParam("name", TEST_OBJECT_NAME)
				.queryParam("mediaType", "application/xml").request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(201, response.getStatus(), "Should return HTTP 201 OK");
		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return metadata");
		assertTrue(responseContent.contains("objectId"), "Response should contain objectId");
	}

	/* 
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.rest.tests.AbstractRestTest#getResourceName()
	 */
	@Override
	String getResourceName() {
		return "ObjectRegistryResource";
	}

	
}
