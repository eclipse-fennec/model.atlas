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

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.model.atlas.rest.tests.helper.MockTestHelper.MockEPackageLuceneIndex;
import org.eclipse.fennec.model.atlas.rest.tests.helper.MockTestHelper.MockScopeServiceCollector;
import org.eclipse.fennec.model.atlas.management.lucene.epackage.EPackageLuceneIndex;
import org.eclipse.fennec.model.atlas.rest.tests.helper.ResourceAware;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestHelper;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.eclipse.fennec.model.atlas.workflow.ScopeServiceCollector;
import org.gecko.emf.rest.annotations.RequireEMFMessageBodyReaderWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.service.jakartars.whiteboard.annotations.RequireJakartarsWhiteboard;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
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
@RequireEMF
@RequireEMFMessageBodyReaderWriter
@RequireJakartarsWhiteboard
@RequireConfigurationAdmin
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class ModelAtlasExceptionMapperTest {

	private static final String BASE_URL = "http://localhost:8185/rest";
	private static final String TEST_SCOPE_NAME = "test-scope";

	@InjectService(filter = "(emf.name=workflowapi)")
	ResourceSet resourceSet;

	@InjectService
	ClientBuilder clientBuilder;

	private Client restClient;
	private ServiceRegistration<ScopeServiceCollector> mockScopeCollectorRegistration;
	private MockEPackageLuceneIndex mockEPackageIndex;
    private ServiceRegistration<EPackageLuceneIndex> mockEPackageIndexRegistration;

	@BeforeEach
	public void setup(@InjectBundleContext BundleContext context) throws Exception {
		restClient = clientBuilder.build();

		Dictionary<String, Object> serviceProps = new Hashtable<>();
		serviceProps.put("service.ranking", Integer.MAX_VALUE);

		ThrowingScopeServiceCollector mockCollector = new ThrowingScopeServiceCollector();
		mockScopeCollectorRegistration = context.registerService(ScopeServiceCollector.class, mockCollector,
				serviceProps);

		 // Register mock EPackageLuceneIndex
        Dictionary<String, Object> indexProps = new Hashtable<>();
        indexProps.put("service.ranking", Integer.MAX_VALUE);
        mockEPackageIndex = new MockEPackageLuceneIndex();
        mockEPackageIndexRegistration = context.registerService(EPackageLuceneIndex.class, mockEPackageIndex,
                indexProps);

        // Small delay to allow service registration to propagate
        Thread.sleep(200);

		TestHelper.ensureXMIFactory(resourceSet);

		ResourceAware resourceAware = ResourceAware.create(context, "SchemaPackagesResource");
		assertTrue(resourceAware.waitForResource(15, TimeUnit.SECONDS),
				"SchemaPackagesResource should be registered within 15 seconds.");
	}

	@AfterEach
	public void teardown() throws Exception {
		if (nonNull(mockScopeCollectorRegistration)) {
			mockScopeCollectorRegistration.unregister();
			mockScopeCollectorRegistration = null;
			Thread.sleep(200);
		}
		if (nonNull(mockEPackageIndexRegistration)) {
            mockEPackageIndexRegistration.unregister();
            mockEPackageIndexRegistration = null;
            Thread.sleep(200);
        }

		if (nonNull(restClient)) {
			restClient.close();
			restClient = null;
		}
	}

	// ========== Structured ErrorResponse Format Tests ==========

	@Test
	public void testErrorResponse_HasStructuredFormat() {
		Response response = restClient.target(BASE_URL)
				.path(ThrowingScopeServiceCollector.THROWING_SCOPE).path("schema")
				.request("application/json")
				.get();

		assertEquals(500, response.getStatus(), "Should return HTTP 400 Bad Request");

		String body = response.readEntity(String.class);
		assertNotNull(body, "Response body should not be null");
		assertTrue(body.contains("message"), "ErrorResponse should contain 'message' field");
		assertTrue(body.contains("code"), "ErrorResponse should contain 'code' field");
		assertTrue(body.contains("timestamp"), "ErrorResponse should contain 'timestamp' field");
	}

	// ========== Client Error (4xx) Tests ==========

	@Test
	public void testClientError_PreservesMessage() {
		Response response = restClient.target(BASE_URL)
				.path("non-existent-scope").path("schema")
				.request("application/json")
				.get();

		assertEquals(400, response.getStatus(), "Should return HTTP 400 Bad Request");

		String body = response.readEntity(String.class);
		assertTrue(body.contains("non-existent-scope"),
				"Client error response should preserve the scope name in the message");
	}

	@Test
	public void testClientError_UnsupportedMediaType() {
		Response response = restClient.target(BASE_URL)
				.path(TEST_SCOPE_NAME).path("schema")
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
	public void testServerError_ReturnsGenericMessage() {
		Response response = restClient.target(BASE_URL)
				.path("throw-runtime-exception").path("schema")
				.request("application/json")
				.get();

		assertEquals(500, response.getStatus(), "Should return HTTP 500 Internal Server Error");

		String body = response.readEntity(String.class);
		assertNotNull(body, "Response body should not be null");
		assertTrue(body.contains("An internal server error occurred"),
				"Server error should return generic message");
	}

	@Test
	public void testServerError_DoesNotLeakExceptionMessage() {
		Response response = restClient.target(BASE_URL)
				.path("throw-runtime-exception").path("schema")
				.request("application/json")
				.get();

		assertEquals(500, response.getStatus());

		String body = response.readEntity(String.class);
		assertFalse(body.contains("Simulated internal failure"),
				"Server error should NOT leak the internal exception message");
	}

	@Test
	public void testServerError_DoesNotLeakStackTrace() {
		Response response = restClient.target(BASE_URL)
				.path("throw-runtime-exception").path("schema")
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
	public void testServerError_HasStructuredFormat() {
		Response response = restClient.target(BASE_URL)
				.path("throw-runtime-exception").path("schema")
				.request("application/json")
				.get();

		assertEquals(500, response.getStatus());

		String body = response.readEntity(String.class);
		assertTrue(body.contains("message"), "ErrorResponse should contain 'message' field");
		assertTrue(body.contains("code"), "ErrorResponse should contain 'code' field");
		assertTrue(body.contains("500"), "ErrorResponse code should be 500");
		assertTrue(body.contains("timestamp"), "ErrorResponse should contain 'timestamp' field");
	}

	// ========== Mock that throws RuntimeException for a specific scope ==========

	/**
	 * Extends MockScopeServiceCollector to throw a RuntimeException for a specific
	 * scope name, simulating an unhandled server error.
	 */
	private static class ThrowingScopeServiceCollector extends MockScopeServiceCollector {

		private static final String THROWING_SCOPE = "throw-runtime-exception";

		@Override
		public ScopeService<?> getScopeServiceByScopeName(String scopeName) {
			if (THROWING_SCOPE.equals(scopeName)) {
				throw new RuntimeException("Simulated internal failure");
			}
			return super.getScopeServiceByScopeName(scopeName);
		}
	}
}
