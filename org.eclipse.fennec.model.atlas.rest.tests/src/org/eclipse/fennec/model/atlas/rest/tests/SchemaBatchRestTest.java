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
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
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
 * Integration tests for the package batch endpoint
 * {@code POST /{scope}/schema/stages/{stage}/batch} (issue #386): one resource, one EPackage per
 * root, all written or none.
 */
public class SchemaBatchRestTest extends AbstractRestTest {

	private static final String NS_A = "http://fennec.test/batch/a/1.0";
	private static final String NS_B = "http://fennec.test/batch/b/1.0";

	@Test
	@ParentScopeServiceSetup
	public void testBatch_WritesEveryPackage(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);

		Response response = postBatch(batch(ePackage(NS_A, "a"), ePackage(NS_B, "b")), null);

		assertStatus(200, response, "A valid batch should be applied");
		String body = response.readEntity(String.class);
		assertTrue(body.contains("CREATED") && body.contains(NS_A) && body.contains(NS_B),
				"Every package should be reported as created, by nsURI | body: " + body);
		assertExists(NS_A);
		assertExists(NS_B);
	}

	@Test
	@ParentScopeServiceSetup
	public void testBatch_ExistingPackageRefusesWholeBatch(@InjectBundleContext BundleContext context)
			throws Exception {
		ensureResourceAvailability(context);
		assertStatus(200, postBatch(batch(ePackage(NS_A, "a")), null), "Seeding package a");

		Response response = postBatch(batch(ePackage(NS_A, "a"), ePackage(NS_B, "b")), null);

		assertStatus(409, response, "A taken nsURI should refuse the batch under FAIL");
		assertMissing(NS_B);
	}

	@Test
	@ParentScopeServiceSetup
	public void testBatch_SkipLeavesExistingPackagesAlone(@InjectBundleContext BundleContext context)
			throws Exception {
		ensureResourceAvailability(context);
		assertStatus(200, postBatch(batch(ePackage(NS_A, "a")), null), "Seeding package a");

		Response response = postBatch(batch(ePackage(NS_A, "a"), ePackage(NS_B, "b")), "SKIP");

		assertStatus(200, response, "SKIP should apply the batch");
		String body = response.readEntity(String.class);
		assertTrue(body.contains("SKIPPED") && body.contains("CREATED"),
				"a should be skipped and b created | body: " + body);
		assertExists(NS_B);
	}

	@Test
	@ParentScopeServiceSetup
	public void testBatch_DuplicateNsUriRefusesWholeBatch(@InjectBundleContext BundleContext context)
			throws Exception {
		ensureResourceAvailability(context);

		Response response = postBatch(batch(ePackage(NS_A, "a"), ePackage(NS_B, "b"), ePackage(NS_A, "again")),
				null);

		assertStatus(400, response, "An nsURI used twice should refuse the batch");
		assertMissing(NS_A);
		assertMissing(NS_B);
	}

	@Test
	@ParentScopeServiceSetup
	public void testBatch_NonPackageRootIsRefused(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);

		Response response = postBatch(batch(ePackage(NS_A, "a"), EcoreFactory.eINSTANCE.createEClass()), null);

		assertStatus(400, response, "A root that is not an EPackage should refuse the batch");
		assertMissing(NS_A);
	}

	// ========== Helpers ==========

	private Response postBatch(String xmi, String onConflict) {
		WebTarget target = stageTarget().path("batch");
		if (onConflict != null) {
			target = target.queryParam("onConflict", onConflict);
		}
		return target.request("application/json").post(Entity.entity(xmi, "application/xmi"));
	}

	private void assertExists(String nsUri) {
		Response response = stageTarget().queryParam("nsUri", nsUri).request("application/json").get();
		assertStatus(200, response, "Package " + nsUri + " should exist");
	}

	private void assertMissing(String nsUri) {
		Response response = stageTarget().queryParam("nsUri", nsUri).request("application/json").get();
		assertEquals(204, response.getStatus(), "Package " + nsUri + " should not have been written");
	}

	private static EPackage ePackage(String nsUri, String name) {
		EPackage ePackage = TestHelper.createTestEPackage(nsUri, name, name);
		ePackage.getEClassifiers().add(TestHelper.createTestEClass(Character.toUpperCase(name.charAt(0))
				+ name.substring(1) + "Thing"));
		return ePackage;
	}

	/** One XMI document with the given objects as its roots. */
	private String batch(EObject... roots) throws IOException {
		Resource resource = resourceSet.createResource(URI.createURI("temp://batch-" + System.nanoTime() + ".xmi"));
		try {
			for (EObject root : roots) {
				resource.getContents().add(root);
			}
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			resource.save(out, Collections.emptyMap());
			return out.toString(StandardCharsets.UTF_8);
		} finally {
			resource.getContents().clear();
			resourceSet.getResources().remove(resource);
		}
	}

	/** /{TEST_SCOPE_NAME}/schema/stages/draft */
	private WebTarget stageTarget() {
		return scopeTarget().path("schema").path("stages").path(TestAnnotations.STAGE_DRAFT);
	}

	@Override
	String getResourceName() {
		return "SchemaPackagesResource";
	}
}
