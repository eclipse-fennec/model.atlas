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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.eclipse.fennec.model.atlas.rest.tests.helper.TestAnnotations;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestAnnotations.ParentScopeServiceSetup;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.test.common.annotation.InjectBundleContext;

import jakarta.ws.rs.client.WebTarget;
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

	/** /scopes */
	private WebTarget scopesTarget() {
		return baseTarget().path("scopes");
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
