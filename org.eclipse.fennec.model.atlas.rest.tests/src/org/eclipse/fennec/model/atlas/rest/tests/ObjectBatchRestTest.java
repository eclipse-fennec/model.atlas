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

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.XMLResource;
import org.eclipse.fennec.model.atlas.datagen.example.model.dge.Person;
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
 * {@code POST /{scope}/registries/{registry}/stages/{stage}/batch} (issue #386): one resource,
 * one object per root, all written or none.
 */
public class ObjectBatchRestTest extends AbstractRestTest {

	@Test
	@ParentScopeServiceSetup
	public void testBatch_WritesEveryRoot(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);

		Response response = postBatch(batch(person("p1", "Ada"), person("p2", "Grace")), null);

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
		assertStatus(200, postBatch(batch(person("p1", "Ada")), null), "Seeding p1");

		Response response = postBatch(batch(person("p1", "Changed"), person("p3", "Linus")), null);

		assertStatus(409, response, "An id that is taken should refuse the batch under FAIL");
		String body = response.readEntity(String.class);
		assertTrue(body.contains("ALREADY_EXISTS"), "The refusal should be named | body: " + body);
		assertMissing("p3");
	}

	@Test
	@ParentScopeServiceSetup
	public void testBatch_SkipLeavesExistingObjectsAlone(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		assertStatus(200, postBatch(batch(person("p1", "Ada")), null), "Seeding p1");

		Response response = postBatch(batch(person("p1", "Changed"), person("p2", "Grace")), "skip");

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
		assertStatus(200, postBatch(batch(person("p1", "Ada")), null), "Seeding p1");

		Response response = postBatch(batch(person("p1", "Ada"), person("p2", "Grace")), "OVERWRITE");

		assertStatus(200, response, "OVERWRITE should apply the batch");
		String body = response.readEntity(String.class);
		assertTrue(body.contains("UNCHANGED"), "Equal content should not be written again | body: " + body);
	}

	@Test
	@ParentScopeServiceSetup
	public void testBatch_RootWithoutIdRefusesWholeBatch(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);

		Response response = postBatch(batch(person("p1", "Ada"), person(null, "Nobody")), null);

		assertStatus(400, response, "A root without id should refuse the batch");
		String body = response.readEntity(String.class);
		assertTrue(body.contains("has no object id"), "The missing id should be named | body: " + body);
		assertMissing("p1");
	}

	@Test
	@ParentScopeServiceSetup
	public void testBatch_DuplicateIdRefusesWholeBatch(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);

		Response response = postBatch(batch(person("p1", "Ada"), person("p2", "Grace"), person("p1", "Again")),
				null);

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

		Response response = postBatch(batch(person("p1", "Ada")), "merge");

		assertStatus(400, response, "An unknown onConflict value should be refused");
		assertMissing("p1");
	}

	/**
	 * A JSON batch is an array of root objects. The codec keeps an {@code _id} only in an ID
	 * attribute, which {@code Person} lacks, so both roots are read and refused for want of an id.
	 */
	@Test
	@ParentScopeServiceSetup
	public void testBatch_JsonRootsWithoutIdAttributeAreRefused(@InjectBundleContext BundleContext context)
			throws Exception {
		ensureResourceAvailability(context);
		String json = "[{\"_type\":\"https://dg.de/1.0#//Person\",\"_id\":\"j1\",\"firstName\":\"Ada\"},"
				+ "{\"_type\":\"https://dg.de/1.0#//Person\",\"_id\":\"j2\",\"firstName\":\"Grace\"}]";

		Response response = stageTarget().path("batch").request("application/json")
				.post(Entity.entity(json, "application/json"));

		assertStatus(400, response, "Roots without an id should refuse the batch");
		String body = response.readEntity(String.class);
		assertEquals(2, body.split("has no object id", -1).length - 1,
				"Both JSON roots should be read and refused | body: " + body);
		assertMissing("j1");
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

	/** A person carried by the given xmi:id; {@code null} leaves it without one. */
	private static Person person(String xmiId, String firstName) {
		Person person = TestHelper.createTestObject();
		person.setFirstName(firstName);
		person.eAdapters().add(new XmiId(xmiId));
		return person;
	}

	/** One XMI document with the given objects as its roots, each with the xmi:id it carries. */
	private String batch(Person... persons) throws IOException {
		Resource resource = resourceSet.createResource(URI.createURI("temp://batch-" + System.nanoTime() + ".xmi"));
		try {
			for (Person person : persons) {
				resource.getContents().add(person);
				String id = XmiId.of(person);
				if (id != null) {
					((XMLResource) resource).setID(person, id);
				}
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			resource.save(out, Collections.emptyMap());
			return out.toString(StandardCharsets.UTF_8);
		} finally {
			resource.getContents().clear();
			resourceSet.getResources().remove(resource);
		}
	}

	/** Remembers the xmi:id a test person should be written with. */
	private static final class XmiId extends org.eclipse.emf.common.notify.impl.AdapterImpl {

		private final String id;

		XmiId(String id) {
			this.id = id;
		}

		static String of(EObject object) {
			for (Object adapter : object.eAdapters()) {
				if (adapter instanceof XmiId xmiId) {
					return xmiId.id;
				}
			}
			return null;
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
