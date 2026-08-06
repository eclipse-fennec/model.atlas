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
import jakarta.ws.rs.core.EntityTag;
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
		assertTrue(responseContent.contains("objectId"), "Response should contain objectId | body: " + responseContent);
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
		assertTrue(responseContent.contains("objectId"), "Response should contain objectId | body: " + responseContent);
	}

	@Test
	@ParentScopeServiceSetup
	public void testMetadataJsonKeysTheIdAttributeByItsFeatureName(@InjectBundleContext BundleContext context)
			throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);
		Response response = stageTarget(TestAnnotations.STAGE_DRAFT)
				.queryParam("name", TEST_OBJECT_NAME)
				.request("application/json").get();

		String responseContent = response.readEntity(String.class);

		// objectId is an EMF ID attribute, and the codec keys those as "_id" by default,
		// dropping the feature name. That name is published API - the Atlas REST client
		// reads it, and the OpenAPI schema documents it - so every endpoint pins the id
		// key mode to FEATURE_ONLY. Without it this response says "_id" and no consumer
		// finds the id it was promised.
		assertTrue(responseContent.contains("\"objectId\""),
				"Metadata JSON must key the id by its feature name | body: " + responseContent);
		assertFalse(responseContent.contains("\"_id\""),
				"Metadata JSON must not key the id as _id | body: " + responseContent);
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

	// ========== Get Object Content From Final Stage Tests (P5-0) ==========

	@Test
	@ParentScopeServiceSetup
	public void testGetObjectContentFromFinalStage_Success(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_RELEASE);

		Response response = objectRegistryTarget().path("content")
				.queryParam("objectId", TEST_OBJECT_ID).request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return content");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetObjectContentFromFinalStage_NotFound(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		Response response = objectRegistryTarget().path("content")
				.queryParam("objectId", "non-existent-object").request("application/json").get();

		assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content when object not in final stage");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetObjectContentFromFinalStage_ScopeNotFound(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		Response response = registryTarget("non-existent-scope", TestAnnotations.OBJECT_REGISTRY_NAME)
				.path("content").queryParam("objectId", TEST_OBJECT_ID).request("application/json").get();

		assertEquals(400, response.getStatus(), "Should return HTTP 400 Bad Request for unknown scope");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetObjectContentFromFinalStage_UnassignedRegistry(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		assertUnassignedRegistryRejected(registryTarget(UNASSIGNED_REGISTRY_NAME).path("content")
				.queryParam("objectId", TEST_OBJECT_ID).request("application/json").get());
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetObjectContentFromFinalStage_ConditionalGetNotModified(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_RELEASE);

		Response first = objectRegistryTarget().path("content")
				.queryParam("objectId", TEST_OBJECT_ID).request("application/json").get();
		assertEquals(200, first.getStatus(), "First GET should return HTTP 200 OK");
		String etag = first.getHeaderString("ETag");
		assertNotNull(etag, "Final-stage content GET should emit an ETag");

		Response second = objectRegistryTarget().path("content")
				.queryParam("objectId", TEST_OBJECT_ID).request("application/json")
				.header("If-None-Match", etag).get();
		assertEquals(304, second.getStatus(), "Matching If-None-Match should return HTTP 304 Not Modified");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetObjectContentFromFinalStage_InheritsFromParent(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		// Upload only to the PARENT scope's final stage; the child scope must read through.
		Person person = TestHelper.createTestObject();
		String xmiContent = TestHelper.serializeToXMI(person, resourceSet);
		Response upload = registryTarget(TestAnnotations.TEST_PARENT_SCOPE_NAME, TestAnnotations.OBJECT_REGISTRY_NAME)
				.path("stages").path(TestAnnotations.STAGE_RELEASE).path(TEST_OBJECT_ID)
				.queryParam("name", TEST_OBJECT_NAME).queryParam("mediaType", "application/xml")
				.request("application/xmi").post(Entity.entity(xmiContent, "application/xmi"));
		assertEquals(201, upload.getStatus(), "Upload to parent scope final stage should succeed");

		Response response = objectRegistryTarget().path("content")
				.queryParam("objectId", TEST_OBJECT_ID).request("application/json").get();

		assertEquals(200, response.getStatus(), "Child final-stage content should read through to the parent scope");
		assertNotNull(response.readEntity(String.class), "Should return inherited content");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetObjectContentFromFinalStage_XmiIsStockLoadable(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_RELEASE);

		Response response = objectRegistryTarget().path("content")
				.queryParam("objectId", TEST_OBJECT_ID).request("application/xmi").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
		String body = response.readEntity(String.class);
		// Proof for P5-1: a general EObject served as application/xmi is plain, stock-EMF
		// loadable XMI (no codec needed on the client to reconstruct it).
		org.eclipse.emf.ecore.EObject reloaded = TestHelper.deserializeFromXMI(body, resourceSet);
		assertNotNull(reloaded, "XMI body should reload via stock EMF");
		assertEquals("Person", reloaded.eClass().getName(), "Reloaded object should be the uploaded Person");
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

		assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content");
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

	// ========== ETag / Idempotency Tests ==========

	@Test
	@ParentScopeServiceSetup
	public void testGetObjectContent_ReturnsStrongETagAndLastModified(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);

		Response response = stageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("objectId", TEST_OBJECT_ID).request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
		EntityTag etag = response.getEntityTag();
		assertNotNull(etag, "Content GET should emit an ETag (set by ObjectMetadataResponseFilter)");
		assertFalse(etag.isWeak(), "ETag should be a strong validator");
		assertNotNull(response.getLastModified(), "Content GET should emit a Last-Modified header");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetObjectContent_IfNoneMatchHit_Returns304WithETagNoBody(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);

		Response first = stageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("objectId", TEST_OBJECT_ID).request("application/json").get();
		assertEquals(200, first.getStatus());
		String etag = first.getHeaderString("ETag");
		assertNotNull(etag, "First content GET should carry an ETag");

		Response second = stageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("objectId", TEST_OBJECT_ID).request("application/json")
				.header("If-None-Match", etag).get();
		assertEquals(304, second.getStatus(), "Matching If-None-Match should yield 304");
		assertEquals(etag, second.getHeaderString("ETag"), "304 should still carry the current ETag");
		assertFalse(second.hasEntity(), "304 should have no body");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetObjectContent_IfModifiedSince_NotModified_Returns304(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);

		Response first = stageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("objectId", TEST_OBJECT_ID).request("application/json").get();
		assertEquals(200, first.getStatus());
		Date lastModified = first.getLastModified();
		assertNotNull(lastModified, "First content GET should carry Last-Modified");

		// Echoing the resource's own Last-Modified back means "not modified since" → 304.
		Response second = stageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("objectId", TEST_OBJECT_ID).request("application/json")
				.header("If-Modified-Since", lastModified).get();
		assertEquals(304, second.getStatus(), "If-Modified-Since >= lastChangeTime should yield 304");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetObjectContent_IfModifiedSince_Modified_Returns200(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);

		// A long-past If-Modified-Since means the resource has changed since → 200.
		Response response = stageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("objectId", TEST_OBJECT_ID).request("application/json")
				.header("If-Modified-Since", new Date(0L)).get();
		assertEquals(200, response.getStatus(), "Stale If-Modified-Since should yield 200");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetObjectContent_SetsVaryAccept(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);

		Response response = stageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("objectId", TEST_OBJECT_ID).request("application/json").get();
		assertEquals(200, response.getStatus());
		assertNotNull(response.getHeaderString("Vary"), "Cacheable content GET should carry a Vary header");
		assertTrue(response.getHeaderString("Vary").toLowerCase().contains("accept"),
				"Vary header should mark the response as varying on Accept");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetObjectContent_VaryAcceptPresentOn304(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);

		Response first = stageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("objectId", TEST_OBJECT_ID).request("application/json").get();
		String etag = first.getHeaderString("ETag");
		assertNotNull(etag);

		Response second = stageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("objectId", TEST_OBJECT_ID).request("application/json")
				.header("If-None-Match", etag).get();
		assertEquals(304, second.getStatus());
		assertNotNull(second.getHeaderString("Vary"), "304 should still carry a Vary header");
		assertTrue(second.getHeaderString("Vary").toLowerCase().contains("accept"),
				"304 Vary header should mark the response as varying on Accept");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetObjectContent_DistinctETagPerRepresentation(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);

		// Same object (same content hash), two representations → two distinct ETags.
		Response asXml = stageTarget(TestAnnotations.STAGE_DRAFT).path("content").queryParam("objectId", TEST_OBJECT_ID)
				.queryParam("mediaType", "application/xml").request("application/json").get();
		assertEquals(200, asXml.getStatus());
		String xmlETag = asXml.getHeaderString("ETag");
		assertNotNull(xmlETag, "XML representation should carry an ETag");

		Response asXmi = stageTarget(TestAnnotations.STAGE_DRAFT).path("content").queryParam("objectId", TEST_OBJECT_ID)
				.queryParam("mediaType", "application/xmi").request("application/json").get();
		assertEquals(200, asXmi.getStatus());
		String xmiETag = asXmi.getHeaderString("ETag");
		assertNotNull(xmiETag, "XMI representation should carry an ETag");

		assertNotEquals(xmlETag, xmiETag, "Different representations of the same object must have distinct ETags");
	}

	@Test
	@ParentScopeServiceSetup
	public void testCreateObject_Override_IfMatchSuccess(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);

		// A create-with-override replaces content, so source If-Match from a content GET.
		String etag = stageTarget(TestAnnotations.STAGE_DRAFT).path("content").queryParam("objectId", TEST_OBJECT_ID)
				.request("application/json").get().getHeaderString("ETag");
		assertNotNull(etag, "Content GET should carry an ETag");

		Person updated = TestHelper.createTestObject();
		updated.setFirstName("Jane");
		String xmi = TestHelper.serializeToXMI(updated, resourceSet);

		Response response = stageTarget(TestAnnotations.STAGE_DRAFT).path(TEST_OBJECT_ID)
				.queryParam("name", TEST_OBJECT_NAME).queryParam("override", "true")
				.request("application/xmi").header("If-Match", etag)
				.post(Entity.entity(xmi, "application/xmi"));

		assertEquals(200, response.getStatus(), "Matching If-Match on create-override should yield 200");
		assertNotNull(response.getHeaderString("ETag"), "Override response should carry the new ETag");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetObjectMetadata_IfNoneMatchHit_Returns304(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);

		Response firstResponse = stageTarget(TestAnnotations.STAGE_DRAFT)
				.queryParam("objectId", TEST_OBJECT_ID).request("application/json").get();
		assertEquals(200, firstResponse.getStatus());
		String etag = firstResponse.getHeaderString("ETag");
		assertNotNull(etag, "First response should contain ETag");

		Response secondResponse = stageTarget(TestAnnotations.STAGE_DRAFT)
				.queryParam("objectId", TEST_OBJECT_ID).request("application/json")
				.header("If-None-Match", etag).get();
		assertEquals(304, secondResponse.getStatus(), "Should return HTTP 304 Not Modified when ETag matches");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetObjectMetadata_IfNoneMatchMiss_Returns200(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);

		Response response = stageTarget(TestAnnotations.STAGE_DRAFT)
				.queryParam("objectId", TEST_OBJECT_ID).request("application/json")
				.header("If-None-Match", "\"stale-etag-value\"").get();
		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK when ETag doesn't match");
	}

	@Test
	@ParentScopeServiceSetup
	public void testUpdateObjectContent_IfMatchSuccess(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);

		// A content write validates against the content ETag, so source If-Match from a content GET.
		Response getResponse = stageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("objectId", TEST_OBJECT_ID).request("application/json").get();
		String etag = getResponse.getHeaderString("ETag");
		assertNotNull(etag, "Should have ETag");

		Person updatedPerson = TestHelper.createTestObject();
		updatedPerson.setFirstName("Jane");
		String xmiContent = TestHelper.serializeToXMI(updatedPerson, resourceSet);

		Response response = stageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("objectId", TEST_OBJECT_ID).queryParam("version", "1.1.0")
				.request("application/xmi").header("If-Match", etag)
				.put(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK when If-Match matches");
		assertNotNull(response.getHeaderString("ETag"), "Updated response should contain new ETag");
	}

	@Test
	@ParentScopeServiceSetup
	public void testUpdateObjectContent_IfMatchFail_Returns412(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);

		Person updatedPerson = TestHelper.createTestObject();
		String xmiContent = TestHelper.serializeToXMI(updatedPerson, resourceSet);

		Response response = stageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("objectId", TEST_OBJECT_ID).queryParam("version", "1.1.0")
				.request("application/xmi").header("If-Match", "\"stale-etag-value\"")
				.put(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(412, response.getStatus(), "Should return HTTP 412 Precondition Failed when ETag doesn't match");
	}

	@Test
	@ParentScopeServiceSetup
	public void testUpdateObjectContent_NoIfMatch_StillWorks(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);

		Person updatedPerson = TestHelper.createTestObject();
		updatedPerson.setFirstName("Jane");
		String xmiContent = TestHelper.serializeToXMI(updatedPerson, resourceSet);

		Response response = stageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("objectId", TEST_OBJECT_ID).queryParam("version", "1.1.0")
				.request("application/xmi").put(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK without If-Match (backward compatible)");
	}

	@Test
	@ParentScopeServiceSetup
	public void testUpdateObjectContent_IdenticalContent_SkipsUpdate(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);

		Person samePerson = TestHelper.createTestObject();
		String xmiContent = TestHelper.serializeToXMI(samePerson, resourceSet);

		Response response = stageTarget(TestAnnotations.STAGE_DRAFT).path("content")
				.queryParam("objectId", TEST_OBJECT_ID).queryParam("version", "1.0.0")
				.request("application/xmi").put(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK for identical content");
		assertNotNull(response.getHeaderString("ETag"), "Response should contain ETag");
	}

	@Test
	@ParentScopeServiceSetup
	public void testDeleteObject_AlreadyDeleted_Returns204(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);

		Response response = stageTarget(TestAnnotations.STAGE_DRAFT)
				.queryParam("objectId", "non-existent-object").request().delete();

		assertEquals(204, response.getStatus(), "Should return HTTP 204 No Content for already-deleted resource");
	}

	@Test
	@ParentScopeServiceSetup
	public void testDeleteObject_IfMatchFail_Returns412(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);

		Response response = stageTarget(TestAnnotations.STAGE_DRAFT)
				.queryParam("objectId", TEST_OBJECT_ID).request()
				.header("If-Match", "\"stale-etag-value\"").delete();

		assertEquals(412, response.getStatus(),
				"Should return HTTP 412 Precondition Failed when If-Match doesn't match on delete");
	}

	@Test
	@ParentScopeServiceSetup
	public void testTransitionObject_IfMatchSuccess(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);

		// A transition validates against the metadata ETag, so source If-Match from a metadata GET.
		String etag = stageTarget(TestAnnotations.STAGE_DRAFT).queryParam("objectId", TEST_OBJECT_ID)
				.request("application/json").get().getHeaderString("ETag");
		assertNotNull(etag, "Metadata GET should carry an ETag");

		StageTransitionRequest transition = RestFactory.eINSTANCE.createStageTransitionRequest();
		transition.setObjectId(TEST_OBJECT_ID);
		transition.setTargetStage(TestAnnotations.STAGE_APPROVED);
		String xmiContent = TestHelper.serializeToXMI(transition, resourceSet);

		Response response = stageTarget(TestAnnotations.STAGE_DRAFT).path("actions").path("transition")
				.request("application/xmi").header("If-Match", etag)
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(200, response.getStatus(), "Matching If-Match should let the transition proceed");
		assertNotNull(response.getHeaderString("ETag"), "Transition response should carry the new ETag");
	}

	@Test
	@ParentScopeServiceSetup
	public void testTransitionObject_IfMatchFail_Returns412(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);

		StageTransitionRequest transition = RestFactory.eINSTANCE.createStageTransitionRequest();
		transition.setObjectId(TEST_OBJECT_ID);
		transition.setTargetStage(TestAnnotations.STAGE_APPROVED);
		String xmiContent = TestHelper.serializeToXMI(transition, resourceSet);

		Response response = stageTarget(TestAnnotations.STAGE_DRAFT).path("actions").path("transition")
				.request("application/xmi").header("If-Match", "\"stale-etag-value\"")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(412, response.getStatus(), "Mismatching If-Match should block the transition with 412");
		// No side effect: the object is still in the source stage.
		Response stillInDraft = stageTarget(TestAnnotations.STAGE_DRAFT).queryParam("objectId", TEST_OBJECT_ID)
				.request("application/json").get();
		assertEquals(200, stillInDraft.getStatus(), "Object must remain in the source stage after a 412");
	}

	@Test
	@ParentScopeServiceSetup
	public void testTransitionObject_AlreadyTransitioned_Returns200(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		uploadTestObject(TestAnnotations.STAGE_DRAFT);

		StageTransitionRequest transition = RestFactory.eINSTANCE.createStageTransitionRequest();
		transition.setObjectId(TEST_OBJECT_ID);
		transition.setTargetStage(TestAnnotations.STAGE_APPROVED);
		String xmiContent = TestHelper.serializeToXMI(transition, resourceSet);

		// First transition: draft → approved
		Response firstTransition = stageTarget(TestAnnotations.STAGE_DRAFT).path("actions").path("transition")
				.request("application/xmi").post(Entity.entity(xmiContent, "application/xmi"));
		assertEquals(200, firstTransition.getStatus(), "First transition should succeed");

		// Retry: object is no longer in draft but IS in approved — server returns 200 (idempotent)
		Response retryResponse = stageTarget(TestAnnotations.STAGE_DRAFT).path("actions").path("transition")
				.request("application/xmi").post(Entity.entity(xmiContent, "application/xmi"));
		assertEquals(200, retryResponse.getStatus(),
				"Should return HTTP 200 OK when object is already in target stage");
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
