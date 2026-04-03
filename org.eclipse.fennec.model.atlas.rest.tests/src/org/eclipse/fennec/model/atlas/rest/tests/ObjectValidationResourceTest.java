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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Proxy;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.m2x.ocl.api.OclEngine;
import org.eclipse.fennec.model.atlas.rest.tests.helper.ResourceAware;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestHelper;
import org.gecko.emf.rest.annotations.RequireEMFMessageBodyReaderWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.PrototypeServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.service.jakartars.whiteboard.annotations.RequireJakartarsWhiteboard;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;

/**
 * Integration tests for ObjectValidationResource REST endpoint.
 *
 * <p>
 * Tests cover:
 * </p>
 * <ul>
 * <li>Validating a valid EObject returns 200 with diagnostic</li>
 * <li>Unsupported media type returns 415</li>
 * <li>Supported media type query parameter works correctly</li>
 * <li>Response contains diagnostic information</li>
 * </ul>
 *
 * @author ilenia
 * @since Mar 17, 2026
 */
@RequireEMF
@RequireEMFMessageBodyReaderWriter
@RequireJakartarsWhiteboard
@RequireConfigurationAdmin
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class ObjectValidationResourceTest {

	private static final String BASE_URL = "http://localhost:8185/rest";

	@InjectService(filter = "(emf.name=workflowapi)")
	ResourceSet resourceSet;

	@InjectService
	ClientBuilder clientBuilder;

	private Client restClient;
	private ServiceRegistration<OclEngine> mockOclEngineRegistration;

	@BeforeEach
	public void setup(@InjectBundleContext BundleContext context) throws Exception {
		restClient = clientBuilder.build();

		// Register mock OclEngine as PrototypeServiceFactory to satisfy PROTOTYPE_REQUIRED reference
		Dictionary<String, Object> props = new Hashtable<>();
		props.put("service.ranking", Integer.MAX_VALUE);

		mockOclEngineRegistration = context.registerService(OclEngine.class,
				new PrototypeServiceFactory<OclEngine>() {
					@Override
					public OclEngine getService(Bundle bundle, ServiceRegistration<OclEngine> registration) {
						return (OclEngine) Proxy.newProxyInstance(OclEngine.class.getClassLoader(),
								new Class[] { OclEngine.class }, (proxy, method, args) -> null);
					}

					@Override
					public void ungetService(Bundle bundle, ServiceRegistration<OclEngine> registration,
							OclEngine service) {
					}
				}, props);

		// Small delay to allow service registration to propagate
		Thread.sleep(200);

		// Ensure XMI factory is registered
		TestHelper.ensureXMIFactory(resourceSet);

		// Wait for the ObjectValidationResource to be registered in Jakarta REST runtime
		ResourceAware resourceAware = ResourceAware.create(context, "ObjectValidationResource");
		boolean resourceReady = resourceAware.waitForResource(15, TimeUnit.SECONDS);

		assertTrue(resourceReady, "ObjectValidationResource should be registered within 15 seconds. "
				+ "Check that the resource is properly configured and the Jakarta REST runtime is working.");
	}

	@AfterEach
	public void teardown() throws Exception {
		if (nonNull(mockOclEngineRegistration)) {
			mockOclEngineRegistration.unregister();
			mockOclEngineRegistration = null;
		}

		// Small delay to allow service unregistration to propagate
		Thread.sleep(200);

		if (nonNull(restClient)) {
			restClient.close();
			restClient = null;
		}
	}

	// ========== Validation Tests ==========

	@Test
	public void testValidate_Success() throws Exception {
		EPackage validPackage = TestHelper.createTestEPackage("http://test.com/valid/1.0", "ValidPackage", "vp");
		String xmiContent = TestHelper.serializeToXMI(validPackage, resourceSet);

		Response response = restClient.target(BASE_URL).path("validate").request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return diagnostic content");
	}

	@Test
	public void testValidate_ResponseContainsDiagnosticInfo() throws Exception {
		EPackage validPackage = TestHelper.createTestEPackage("http://test.com/diag/1.0", "DiagPackage", "dp");
		String xmiContent = TestHelper.serializeToXMI(validPackage, resourceSet);

		Response response = restClient.target(BASE_URL).path("validate").request("application/json")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return diagnostic content");
		assertTrue(responseContent.contains("type") || responseContent.contains("message"),
				"Response should contain diagnostic information");
	}

	// ========== MediaType Tests ==========

	@Test
	public void testValidate_UnsupportedMediaType() throws Exception {
		EPackage validPackage = TestHelper.createTestEPackage("http://test.com/unsup/1.0", "UnsupPackage", "up");
		String xmiContent = TestHelper.serializeToXMI(validPackage, resourceSet);

		Response response = restClient.target(BASE_URL).path("validate")
				.queryParam("mediaType", "application/unsupported").request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(415, response.getStatus(), "Should return HTTP 415 Unsupported Media Type");
	}

	@Test
	public void testValidate_WithSupportedMediaTypeQueryParam() throws Exception {
		EPackage validPackage = TestHelper.createTestEPackage("http://test.com/mt/1.0", "MtPackage", "mt");
		String xmiContent = TestHelper.serializeToXMI(validPackage, resourceSet);

		Response response = restClient.target(BASE_URL).path("validate")
				.queryParam("mediaType", "application/xml").request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");
		assertEquals("application/xml", response.getHeaderString("Content-Type"),
				"Content-Type header should be set to mediaType query parameter value");
	}

	@Test
	public void testValidate_RejectsUnsupportedAcceptHeader() throws Exception {
		EPackage validPackage = TestHelper.createTestEPackage("http://test.com/def/1.0", "DefPackage", "def");
		String xmiContent = TestHelper.serializeToXMI(validPackage, resourceSet);

		Response response = restClient.target(BASE_URL).path("validate").request("text/plain")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(406, response.getStatus(), "Should return HTTP 406 Not Acceptable for unsupported Accept header");
	}
}
