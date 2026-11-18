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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.fennec.model.atlas.datagen.example.model.dge.Person;
import org.eclipse.fennec.model.atlas.registry.api.ObjectWrite;
import org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage;
import org.eclipse.fennec.model.atlas.registry.api.RegistryRequests;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestAnnotations;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestAnnotations.ParentScopeServiceSetup;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestHelper;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

/**
 * Integration tests for the object batch endpoint
 * {@code POST /{scope}/registries/{registry}/stages/{stage}/batch} (issue #386): the body is an
 * {@code ObjectBatch} document, one {@code ObjectWrite} per object, all written or none.
 */
public class ObjectBatchRestTest extends AbstractRestTest {

	@Test
	@ParentScopeServiceSetup
	public void testBatch_WritesEveryObject(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);

		Response response = postBatch(batch(write("p1", "Ada"), write("p2", "Grace")), null);

		assertStatus(200, response, "A valid batch should be applied");
		String body = response.readEntity(String.class);
		assertTrue(body.contains("CREATED"), "Every item should be reported as created | body: " + body);
		assertExists("p1");
		assertExists("p2");
	}

	@Test
	@ParentScopeServiceSetup
	public void testBatch_ExistingIdRefusesWholeBatch(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		assertStatus(200, postBatch(batch(write("p1", "Ada")), null), "Seeding p1");

		Response response = postBatch(batch(write("p1", "Changed"), write("p3", "Linus")), null);

		assertStatus(409, response, "An id that is taken should refuse the batch under FAIL");
		String body = response.readEntity(String.class);
		assertTrue(body.contains("ALREADY_EXISTS"), "The refusal should be named | body: " + body);
		assertMissing("p3");
	}

	@Test
	@ParentScopeServiceSetup
	public void testBatch_SkipLeavesExistingObjectsAlone(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		assertStatus(200, postBatch(batch(write("p1", "Ada")), null), "Seeding p1");

		Response response = postBatch(batch(write("p1", "Changed"), write("p2", "Grace")), "skip");

		assertStatus(200, response, "SKIP should apply the batch");
		String body = response.readEntity(String.class);
		assertTrue(body.contains("SKIPPED") && body.contains("CREATED"),
				"p1 should be skipped and p2 created | body: " + body);
		assertExists("p2");
	}

	@Test
	@ParentScopeServiceSetup
	public void testBatch_OverwriteWithEqualContentIsUnchanged(@InjectBundleContext BundleContext context)
			throws Exception {
		ensureResourceAvailability(context);
		assertStatus(200, postBatch(batch(write("p1", "Ada")), null), "Seeding p1");

		Response response = postBatch(batch(write("p1", "Ada"), write("p2", "Grace")), "OVERWRITE");

		assertStatus(200, response, "OVERWRITE should apply the batch");
		String body = response.readEntity(String.class);
		assertTrue(body.contains("UNCHANGED"), "Equal content should not be written again | body: " + body);
	}

	/** Person has no ID attribute, so a write that names no id cannot be stored. */
	@Test
	@ParentScopeServiceSetup
	public void testBatch_WriteWithoutIdRefusesWholeBatch(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);

		Response response = postBatch(batch(write("p1", "Ada"), write(null, "Nobody")), null);

		assertStatus(400, response, "A write without id should refuse the batch");
		String body = response.readEntity(String.class);
		assertTrue(body.contains("must name its object id"), "The missing id should be named | body: " + body);
		assertMissing("p1");
	}

	@Test
	@ParentScopeServiceSetup
	public void testBatch_DuplicateIdRefusesWholeBatch(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);

		Response response = postBatch(batch(write("p1", "Ada"), write("p2", "Grace"), write("p1", "Again")), null);

		assertStatus(400, response, "An id used twice should refuse the batch");
		String body = response.readEntity(String.class);
		assertTrue(body.contains("more than once"), "The duplicate should be named | body: " + body);
		assertMissing("p1");
		assertMissing("p2");
	}

	@Test
	@ParentScopeServiceSetup
	public void testBatch_UnknownConflictPolicy(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);

		Response response = postBatch(batch(write("p1", "Ada")), "merge");

		assertStatus(400, response, "An unknown onConflict value should be refused");
		assertMissing("p1");
	}

	@Test
	@ParentScopeServiceSetup
	public void testBatch_AddressOutsideThePathIsRefused(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		ObjectWrite elsewhere = write("p1", "Ada");
		elsewhere.getAddress().setStage("release");

		Response response = postBatch(batch(elsewhere), null);

		assertStatus(400, response, "A write addressing another stage should be refused");
		assertMissing("p1");
	}

	@Test
	@ParentScopeServiceSetup
	public void testBatch_BodyThatIsNoObjectBatchIsRefused(@InjectBundleContext BundleContext context)
			throws Exception {
		ensureResourceAvailability(context);

		Response response = postBatch(xmi(List.of(person("Ada"))), null);

		assertStatus(400, response, "A body that is not an ObjectBatch should be refused");
		String body = response.readEntity(String.class);
		assertTrue(body.contains("ObjectBatch"), "The expected document should be named | body: " + body);
	}

	/** The same document as JSON: the ids travel in the writes, not in the objects. */
	@Test
	@ParentScopeServiceSetup
	public void testBatch_Json(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		String person = "\"content\":{\"_type\":\"https://dg.de/1.0#//Person\",\"firstName\":\"%s\"}";
		String json = "{\"_type\":\"" + RegistryApiPackage.eNS_URI + "#//ObjectBatch\",\"writes\":["
				+ "{\"address\":{\"objectId\":\"j1\"}," + String.format(person, "Ada") + "},"
				+ "{\"address\":{\"objectId\":\"j2\"}," + String.format(person, "Grace") + "}]}";

		Response response = stageTarget().path("batch").request("application/json")
				.post(Entity.entity(json, "application/json"));

		String body = response.readEntity(String.class);
		assertEquals(200, response.getStatus(), "A JSON batch should be applied | body: " + body);
		assertExists("j1");
		assertExists("j2");
	}

	// ========== Helpers ==========

	private Response postBatch(String xmi, String onConflict) {
		WebTarget target = stageTarget().path("batch");
		if (onConflict != null) {
			target = target.queryParam("onConflict", onConflict);
		}
		return target.request("application/json").post(Entity.entity(xmi, "application/xmi"));
	}

	private void assertExists(String objectId) {
		Response response = stageTarget().queryParam("objectId", objectId).request("application/json").get();
		assertStatus(200, response, "Object " + objectId + " should exist");
	}

	private void assertMissing(String objectId) {
		Response response = stageTarget().queryParam("objectId", objectId).request("application/json").get();
		assertEquals(204, response.getStatus(), "Object " + objectId + " should not have been written");
	}

	private static Person person(String firstName) {
		Person person = TestHelper.createTestObject();
		person.setFirstName(firstName);
		return person;
	}

	/**
	 * A write of a new person under the given id; {@code null} names no id. Scope, registry and
	 * stage are left out: the endpoint takes them from the path.
	 */
	private static ObjectWrite write(String objectId, String firstName) {
		return RegistryRequests.objectWrite(RegistryRequests.objectAddress(null, null, null, objectId),
				person(firstName), null, null, null, null);
	}

	/** One ObjectBatch document holding the given writes. */
	private String batch(ObjectWrite... writes) throws IOException {
		// the API interfaces do not extend EObject, their implementations are EObjects
		return xmi(List.of((EObject) RegistryRequests.objectBatch(List.of(writes))));
	}

	private String xmi(List<? extends EObject> roots) throws IOException {
		Resource resource = resourceSet.createResource(URI.createURI("temp://batch-" + System.nanoTime() + ".xmi"));
		try {
			resource.getContents().addAll(roots);
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			resource.save(out, Collections.emptyMap());
			return out.toString(StandardCharsets.UTF_8);
		} finally {
			resource.getContents().clear();
			resourceSet.getResources().remove(resource);
		}
	}

	/** /{TEST_SCOPE_NAME}/registries/{OBJECT_REGISTRY_NAME}/stages/draft */
	private WebTarget stageTarget() {
		return scopeTarget().path("registries").path(TestAnnotations.OBJECT_REGISTRY_NAME).path("stages")
				.path(TestAnnotations.STAGE_DRAFT);
	}

	@Override
	String getResourceName() {
		return "ObjectRegistryResource";
	}
}
