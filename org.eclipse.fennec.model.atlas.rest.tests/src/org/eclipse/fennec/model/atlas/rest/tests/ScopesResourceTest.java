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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestAnnotations;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestAnnotations.ParentScopeServiceSetup;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestHelper;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.EntityTag;
import jakarta.ws.rs.core.Response;

/**
 * Integration tests for ScopesResource REST endpoints.
 *
 * <p>
 * Tests cover:
 * </p>
 * <ul>
 * <li>Listing all available scopes</li>
 * <li>Retrieving a specific scope by name</li>
 * <li>Error handling for non-existent scopes</li>
 * </ul>
 *
 * @author Data In Motion
 * @since 1.0.0
 */

public class ScopesResourceTest extends AbstractRestTest{

	// ========== List All Scopes Tests ==========

	@Test
	@ParentScopeServiceSetup
	public void testListScopes_Success(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		Response response = scopesTarget().request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return content");
		assertTrue(responseContent.contains("scopes"), "Response should contain scopes field");
		assertTrue(responseContent.contains(TestAnnotations.TEST_SCOPE_NAME), "Response should contain the test scope name");
		assertTrue(responseContent.contains(TestAnnotations.TEST_PARENT_SCOPE_NAME), "Response should contain the parent test scope name");
		assertTrue(responseContent.contains(WorkflowConstants.ATLAS_SCOPE_NAME), "Response should contain the atlas scope name");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListScopes_ContainsScopeDetails(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		Response response = scopesTarget().request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return content");

		// Verify scope structure contains expected fields
		assertTrue(responseContent.contains("name"), "Response should contain name field");
		assertTrue(responseContent.contains("registries"), "Response should contain registries field");
	}

	@Test
	@ParentScopeServiceSetup
	public void testListScopes_ReturnsNonEmptyList(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		Response response = scopesTarget().request("application/json").get();

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return content");

		// Verify scopes list is not empty (mock returns at least one scope)
		assertFalse(responseContent.contains("\"scopes\":[]"), "Scopes list should not be empty");
	}

	// ========== Get Specific Scope Tests ==========

	@Test
	@ParentScopeServiceSetup
	public void testGetScope_NotFound(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		Response response = scopesTarget().path("non-existent-scope")
				.request("application/json").get();

		assertEquals(404, response.getStatus(), "Should return HTTP 404 Not Found for non-existent scope");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetScope_EmptyName(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		// Note: This tests behavior with an empty path segment
		// The behavior depends on how the REST framework handles empty path params
		Response response = scopesTarget().path("").request("application/json").get();

		// Empty path segment typically results in listing all scopes (same as /scopes/)
		int status = response.getStatus();
		assertTrue(status == 200, "Empty scope name should return 200 (default to the all scope endpoint)");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetScope_WithSpecialCharacters(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		Response response = scopesTarget().path("scope-with-special-chars!@#")
				.request("application/json").get();

		assertEquals(404, response.getStatus(),
				"Should return HTTP 404 Not Found for non-existent scope with special characters");
	}

	// ========== Content Type Tests ==========

	@Test
	@ParentScopeServiceSetup
	public void testListScopes_AcceptsJsonContentType(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		Response response = scopesTarget().request("application/json").get();

		assertEquals(200, response.getStatus(), "Should accept application/json content type");

		String contentType = response.getHeaderString("Content-Type");
		assertNotNull(contentType, "Response should have Content-Type header");
		assertTrue(contentType.contains("application/json"), "Response Content-Type should be application/json");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetScope_AcceptsJsonContentType(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		Response response = scopesTarget().path(TestAnnotations.TEST_SCOPE_NAME).request("application/json")
				.get();

		assertEquals(200, response.getStatus(), "Should accept application/json content type");

		String contentType = response.getHeaderString("Content-Type");
		assertNotNull(contentType, "Response should have Content-Type header");
		assertTrue(contentType.contains("application/json"), "Response Content-Type should be application/json");
	}

	// ========== Case Sensitivity Tests ==========

	@Test
	@ParentScopeServiceSetup
	public void testGetScope_CaseSensitive(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		// Test that scope names are case-sensitive
		Response response = scopesTarget().path("TEST-SCOPE") // Uppercase version of
				// test-scope
				.request("application/json").get();

		assertEquals(404, response.getStatus(), "Scope lookup should be case-sensitive and return 404 for wrong case");
	}

	@Test
	@ParentScopeServiceSetup
	public void testGetScope_MixedCase(@InjectBundleContext BundleContext context) throws IOException, InterruptedException {
		ensureResourceAvailability(context);
		Response response = scopesTarget().path("Test-Scope") // Mixed case
				.request("application/json").get();

		assertEquals(404, response.getStatus(), "Scope lookup should be case-sensitive and return 404 for mixed case");
	}

	// ========== Scope-level HEAD (aggregate validator) Tests ==========

	private static final String SCOPE_HEAD_NSURI_1 = "http://test.example.com/schema/1.1";
	private static final String SCOPE_HEAD_NAME_1 = "TestSchema";
	private static final String SCOPE_HEAD_NSURI_2 = "http://test.example.com/schema2/1.0";
	private static final String SCOPE_HEAD_NAME_2 = "TestSchema2";

	/** /{TEST_SCOPE_NAME}/schema/stages/{draft} */
	private WebTarget schemaDraftTarget() {
		return scopeTarget(TestAnnotations.TEST_SCOPE_NAME).path("schema").path("stages")
				.path(TestAnnotations.STAGE_DRAFT);
	}

	private void createPackage(String nsUri, String name) throws IOException {
		EPackage pkg = TestHelper.createTestEPackage(nsUri, name, name);
		String xmi = TestHelper.serializeToXMI(pkg, resourceSet);
		schemaDraftTarget().queryParam("nsUri", nsUri).queryParam("name", name).request("application/xmi")
				.post(Entity.entity(xmi, "application/xmi"));
	}

	private void deletePackage(String nsUri) {
		schemaDraftTarget().queryParam("nsUri", nsUri).request().delete();
	}

	@Test
	@ParentScopeServiceSetup
	public void testScopeHead_ReturnsDeterministicAggregateETag(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		createPackage(SCOPE_HEAD_NSURI_1, SCOPE_HEAD_NAME_1);

		Response first = scopesTarget(TestAnnotations.TEST_SCOPE_NAME).request().head();
		assertEquals(200, first.getStatus(), "Scope HEAD should return 200");
		EntityTag etag = first.getEntityTag();
		assertNotNull(etag, "Scope HEAD should emit an aggregate ETag");
		assertFalse(etag.isWeak(), "Aggregate ETag should be a strong validator");

		Response second = scopesTarget(TestAnnotations.TEST_SCOPE_NAME).request().head();
		assertEquals(first.getHeaderString("ETag"), second.getHeaderString("ETag"),
				"Aggregate ETag must be deterministic for an unchanged scope");
	}

	@Test
	@ParentScopeServiceSetup
	public void testScopeHead_ETagStableUnderReordering(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		createPackage(SCOPE_HEAD_NSURI_1, SCOPE_HEAD_NAME_1);
		createPackage(SCOPE_HEAD_NSURI_2, SCOPE_HEAD_NAME_2);
		String etagAB = scopesTarget(TestAnnotations.TEST_SCOPE_NAME).request().head().getHeaderString("ETag");

		// Remove both and re-create in the opposite order: same content, different discovery order.
		deletePackage(SCOPE_HEAD_NSURI_1);
		deletePackage(SCOPE_HEAD_NSURI_2);
		createPackage(SCOPE_HEAD_NSURI_2, SCOPE_HEAD_NAME_2);
		createPackage(SCOPE_HEAD_NSURI_1, SCOPE_HEAD_NAME_1);
		String etagBA = scopesTarget(TestAnnotations.TEST_SCOPE_NAME).request().head().getHeaderString("ETag");

		assertNotNull(etagAB);
		assertEquals(etagAB, etagBA, "Aggregate ETag must be stable under reordering of underlying entries");
	}

	@Test
	@ParentScopeServiceSetup
	public void testScopeHead_IfNoneMatchMatch_Returns304(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		createPackage(SCOPE_HEAD_NSURI_1, SCOPE_HEAD_NAME_1);

		String etag = scopesTarget(TestAnnotations.TEST_SCOPE_NAME).request().head().getHeaderString("ETag");
		assertNotNull(etag);

		Response notModified = scopesTarget(TestAnnotations.TEST_SCOPE_NAME).request()
				.header("If-None-Match", etag).head();
		assertEquals(304, notModified.getStatus(), "Matching aggregate If-None-Match should yield 304");
		assertNull(notModified.getHeaderString("Atlas-Changed-NsUris"), "304 must not carry change hints");
		assertNull(notModified.getHeaderString("Atlas-Changed-Objects"), "304 must not carry change hints");
		assertNull(notModified.getHeaderString("Atlas-Baseline-Unknown"),
				"A matching baseline is by definition known");
	}

	@Test
	@ParentScopeServiceSetup
	public void testScopeHead_StaleIfNoneMatch_Returns200WithExactDiff(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		createPackage(SCOPE_HEAD_NSURI_1, SCOPE_HEAD_NAME_1);
		String etag1 = scopesTarget(TestAnnotations.TEST_SCOPE_NAME).request().head().getHeaderString("ETag");
		assertNotNull(etag1);

		// Add a second package; only its nsURI should appear in the diff.
		createPackage(SCOPE_HEAD_NSURI_2, SCOPE_HEAD_NAME_2);

		Response changed = scopesTarget(TestAnnotations.TEST_SCOPE_NAME).request()
				.header("If-None-Match", etag1).head();
		assertEquals(200, changed.getStatus(), "Stale aggregate If-None-Match should yield 200");
		String changedNsUris = changed.getHeaderString("Atlas-Changed-NsUris");
		assertNotNull(changedNsUris, "200 with a known baseline should list the changed nsURIs");
		assertEquals(SCOPE_HEAD_NSURI_2, changedNsUris, "Diff must contain exactly the newly added nsURI");
		assertNull(changed.getHeaderString("Atlas-Baseline-Unknown"),
				"A reconstructable baseline must not be flagged unknown");
	}

	@Test
	@ParentScopeServiceSetup
	public void testScopeHead_UnknownBaseline_Returns200WithoutDiff(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		createPackage(SCOPE_HEAD_NSURI_1, SCOPE_HEAD_NAME_1);

		Response response = scopesTarget(TestAnnotations.TEST_SCOPE_NAME).request()
				.header("If-None-Match", "\"unknown-baseline-etag\"").head();
		assertEquals(200, response.getStatus(), "An unknown baseline should yield 200");
		assertNull(response.getHeaderString("Atlas-Changed-NsUris"),
				"No diff headers when the baseline cannot be reconstructed");
		assertNull(response.getHeaderString("Atlas-Changed-Objects"),
				"No diff headers when the baseline cannot be reconstructed");
		// #238: without this the answer is indistinguishable from "only things you don't
		// track changed", and a client that just stores the new ETag loses the window for
		// good - its next probe matches that ETag and 304s from then on.
		assertEquals("true", response.getHeaderString("Atlas-Baseline-Unknown"),
				"An unreconstructable baseline must be flagged so the client re-discovers the scope");
	}

	@Test
	@ParentScopeServiceSetup
	public void testScopeHead_UnknownScope_Returns404(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		Response response = scopesTarget("non-existent-scope").request().head();
		assertEquals(404, response.getStatus(), "Unknown scope should yield 404");
	}

	@Test
	@ParentScopeServiceSetup
	public void testScopeHead_StaleIfNoneMatch_ReportsChangedObjects(@InjectBundleContext BundleContext context) throws Exception {
		ensureResourceAvailability(context);
		createObject("scope-head-obj-1", "ScopeHeadObj1");
		String etag1 = scopesTarget(TestAnnotations.TEST_SCOPE_NAME).request().head().getHeaderString("ETag");
		assertNotNull(etag1);

		// Add a second registered object (non-schema registry) → reported via Atlas-Changed-Objects.
		createObject("scope-head-obj-2", "ScopeHeadObj2");

		Response changed = scopesTarget(TestAnnotations.TEST_SCOPE_NAME).request()
				.header("If-None-Match", etag1).head();
		assertEquals(200, changed.getStatus(), "Stale aggregate If-None-Match should yield 200");
		String changedObjects = changed.getHeaderString("Atlas-Changed-Objects");
		assertNotNull(changedObjects, "200 with a known baseline should list the changed objects");
		assertEquals(TestAnnotations.OBJECT_REGISTRY_NAME + "/scope-head-obj-2", changedObjects,
				"Diff must contain exactly the newly added registry/objectId");
		assertNull(changed.getHeaderString("Atlas-Changed-NsUris"),
				"No schema package changed, so no nsURI hints");
	}

	/** /{TEST_SCOPE_NAME}/registries/{person}/stages/{draft} */
	private WebTarget personDraftTarget() {
		return scopeTarget(TestAnnotations.TEST_SCOPE_NAME).path("registries")
				.path(TestAnnotations.OBJECT_REGISTRY_NAME).path("stages").path(TestAnnotations.STAGE_DRAFT);
	}

	private void createObject(String objectId, String name) throws IOException {
		var person = TestHelper.createTestObject();
		String xmi = TestHelper.serializeToXMI(person, resourceSet);
		personDraftTarget().path(objectId).queryParam("name", name).queryParam("mediaType", "application/xml")
				.request("application/xmi").post(Entity.entity(xmi, "application/xmi"));
	}

	/** /scopes */
	private WebTarget scopesTarget() {
		return baseTarget().path("scopes");
	}

	/** /scopes/{scope} */
	private WebTarget scopesTarget(String scope) {
		return scopesTarget().path(scope);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.rest.tests.AbstractRestTest#getResourceName()
	 */
	@Override
	String getResourceName() {
		return "ScopesResource";
	}
}
