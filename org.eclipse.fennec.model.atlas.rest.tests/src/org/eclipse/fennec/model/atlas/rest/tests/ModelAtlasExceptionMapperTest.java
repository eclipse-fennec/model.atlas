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

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.fennec.model.atlas.rest.tests.helper.TestAnnotations;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestAnnotations.ParentScopeServiceSetup;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.eclipse.fennec.model.atlas.workflow.ScopeServiceCollector;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;

import jakarta.ws.rs.core.Response;

/**
 * Integration tests for the {@code ModelAtlasExceptionMapper}.
 *
 * <p>
 * Verifies that:
 * </p>
 * <ul>
 * <li>Error responses use the structured {@code ErrorResponse} format (message, code, timestamp)</li>
 * <li>Client errors (4xx) preserve the user-facing message</li>
 * <li>Server errors (5xx) return a generic message without internal details</li>
 * <li>No stack traces or class names leak into error responses</li>
 * </ul>
 *
 * @author Data In Motion
 * @since 1.0.0
 */

public class ModelAtlasExceptionMapperTest extends AbstractRestTest {

	private ServiceRegistration<ScopeServiceCollector> mockScopeCollectorRegistration;
	private static final String THROWING_SCOPE = "throw-runtime-exception";
	private ScopeServiceCollector mockCollector;

	@BeforeEach
	public void setup(@InjectBundleContext BundleContext context) throws Exception {

		super.setup(context);

		Dictionary<String, Object> serviceProps = new Hashtable<>();
		serviceProps.put("service.ranking", Integer.MAX_VALUE);

		mockCollector = mock(ScopeServiceCollector.class);
		when(mockCollector.getScopeServiceByScopeName(eq(THROWING_SCOPE)))                                                                                                                                                                                                                               
	      .thenThrow(new RuntimeException("Simulated internal failure"));      
		
		mockScopeCollectorRegistration = context.registerService(ScopeServiceCollector.class, mockCollector,
				serviceProps);
		// The filter's rebind to this higher-ranked mock is awaited deterministically
		// in ensureResourceAvailability (see the THROWING_SCOPE probe below), so no
		// fixed sleep is needed here.
	}


	@AfterEach
	public void teardown() throws Exception {
		if (nonNull(mockScopeCollectorRegistration)) {
			mockScopeCollectorRegistration.unregister();
			mockScopeCollectorRegistration = null;
		}
		super.teardown();
	}

	// ========== Structured ErrorResponse Format Tests ==========

	@Test
	@ParentScopeServiceSetup
	public void testErrorResponse_HasStructuredFormat(@InjectBundleContext BundleContext context) throws InterruptedException, IOException {

		ensureResourceAvailability(context);
		Response response = scopeTarget(THROWING_SCOPE).path("schema")
				.request("application/json")
				.get();

		assertEquals(500, response.getStatus(), "Should return HTTP 500");

		String body = response.readEntity(String.class);
		assertNotNull(body, "Response body should not be null");
		assertTrue(body.contains("message"), "ErrorResponse should contain 'message' field");
		assertTrue(body.contains("code"), "ErrorResponse should contain 'code' field");
		assertTrue(body.contains("timestamp"), "ErrorResponse should contain 'timestamp' field");
	}

	// ========== Client Error (4xx) Tests ==========

	@Test
	@ParentScopeServiceSetup
	public void testClientError_PreservesMessage(@InjectBundleContext BundleContext context) throws InterruptedException, IOException {

		ensureResourceAvailability(context);
		Response response = scopeTarget("non-existent-scope").path("schema")
				.request("application/json")
				.get();

		assertEquals(400, response.getStatus(), "Should return HTTP 400 Bad Request");

		String body = response.readEntity(String.class);
		assertTrue(body.contains("non-existent-scope"),
				"Client error response should preserve the scope name in the message");
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	@ParentScopeServiceSetup
	public void testClientError_UnsupportedMediaType(@InjectBundleContext BundleContext context,
			@InjectService(cardinality = 0, filter = "(scope.name="+ TestAnnotations.TEST_SCOPE_NAME +")") ServiceAware<ScopeService> scopeServiceAware) throws InterruptedException, IOException {

		ensureResourceAvailability(context);
		ScopeService scopeService = scopeServiceAware.waitForService(2000);
		assertNotNull(scopeService);
		
		when(mockCollector.getScopeServiceByScopeName(eq(TestAnnotations.TEST_SCOPE_NAME)))                                                                                                                                                                                                                               
	      .thenReturn(scopeService);    
		Response response = scopeTarget().path("schema")
				.queryParam("mediaType", "application/unsupported")
				.request("application/json")
				.get();

		assertEquals(415, response.getStatus(), "Should return HTTP 415 Unsupported Media Type");

		String body = response.readEntity(String.class);
		assertNotNull(body, "Response body should not be null");
		assertTrue(body.contains("code"), "ErrorResponse should contain 'code' field");
		assertTrue(body.contains("415"), "ErrorResponse code should be 415");
	}

	// ========== Server Error (5xx) Tests ==========

	@Test
	@ParentScopeServiceSetup
	public void testServerError_ReturnsGenericMessage(@InjectBundleContext BundleContext context) throws InterruptedException, IOException {

		ensureResourceAvailability(context);
		Response response = scopeTarget("throw-runtime-exception").path("schema")
				.request("application/json")
				.get();

		assertEquals(500, response.getStatus(), "Should return HTTP 500 Internal Server Error");

		String body = response.readEntity(String.class);
		assertNotNull(body, "Response body should not be null");
		assertTrue(body.contains("An internal server error occurred"),
				"Server error should return generic message");
	}

	@Test
	@ParentScopeServiceSetup
	public void testServerError_DoesNotLeakExceptionMessage(@InjectBundleContext BundleContext context) throws InterruptedException, IOException {

		ensureResourceAvailability(context);
		Response response = scopeTarget("throw-runtime-exception").path("schema")
				.request("application/json")
				.get();

		assertEquals(500, response.getStatus());

		String body = response.readEntity(String.class);
		assertFalse(body.contains("Simulated internal failure"),
				"Server error should NOT leak the internal exception message");
	}

	@Test
	@ParentScopeServiceSetup
	public void testServerError_DoesNotLeakStackTrace(@InjectBundleContext BundleContext context) throws InterruptedException, IOException {

		ensureResourceAvailability(context);
		Response response = scopeTarget("throw-runtime-exception").path("schema")
				.request("application/json")
				.get();

		assertEquals(500, response.getStatus());

		String body = response.readEntity(String.class);
		assertFalse(body.contains("at org.eclipse"),
				"Server error should NOT contain stack trace elements");
		assertFalse(body.contains("RuntimeException"),
				"Server error should NOT contain exception class names");
		assertFalse(body.contains(".java:"),
				"Server error should NOT contain source file references");
	}

	@Test
	@ParentScopeServiceSetup
	public void testServerError_HasStructuredFormat(@InjectBundleContext BundleContext context) throws InterruptedException, IOException {

		ensureResourceAvailability(context);
		Response response = scopeTarget("throw-runtime-exception").path("schema")
				.request("application/json")
				.get();

		assertEquals(500, response.getStatus());

		String body = response.readEntity(String.class);
		assertTrue(body.contains("message"), "ErrorResponse should contain 'message' field");
		assertTrue(body.contains("code"), "ErrorResponse should contain 'code' field");
		assertTrue(body.contains("500"), "ErrorResponse code should be 500");
		assertTrue(body.contains("timestamp"), "ErrorResponse should contain 'timestamp' field");
	}



	/*
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.rest.tests.AbstractRestTest#getResourceName()
	 */
	@Override
	String getResourceName() {
		return "SchemaPackagesResource";
	}

	/**
	 * In addition to the base readiness checks, deterministically wait until the
	 * mock {@link ScopeServiceCollector} registered in {@link #setup} is actually
	 * routed by {@code ModelAtlasRequestFilter}. The filter binds the highest-ranked
	 * collector via a {@code DYNAMIC}/{@code GREEDY} reference, and that rebind is
	 * asynchronous; rather than sleeping, we probe the {@code THROWING_SCOPE}
	 * endpoint until it yields the mock-driven {@code 500} (it returns {@code 400}
	 * "scope not found" while the real collector is still bound).
	 */
	@Override
	public void ensureResourceAvailability(BundleContext context) throws InterruptedException {
		super.ensureResourceAvailability(context);
		long deadline = System.currentTimeMillis() + 15_000;
		int status = -1;
		while (System.currentTimeMillis() < deadline) {
			try (Response probe = scopeTarget(THROWING_SCOPE).path("schema").request("application/json").get()) {
				status = probe.getStatus();
			}
			if (status == 500) {
				return;
			}
			Thread.sleep(100);
		}
		fail("Mock ScopeServiceCollector was not picked up by ModelAtlasRequestFilter within 15s "
				+ "(THROWING_SCOPE returned " + status + ", expected 500).");
	}
}
