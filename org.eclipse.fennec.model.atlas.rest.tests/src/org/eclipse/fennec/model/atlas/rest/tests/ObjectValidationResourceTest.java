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

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.m2x.ocl.api.OclEngine;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.PrototypeServiceFactory;
import org.osgi.framework.ServiceRegistration;
import org.osgi.test.common.annotation.InjectBundleContext;

import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
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
public class ObjectValidationResourceTest extends AbstractRestTest{

	
	private ServiceRegistration<OclEngine> mockOclEngineRegistration;

	@BeforeEach
	public void setup(@InjectBundleContext BundleContext context) throws Exception {
		
		super.setup(context);
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

		ensureResourceAvailability(context);
	}

	@AfterEach
	public void teardown() throws Exception {
		if (nonNull(mockOclEngineRegistration)) {
			mockOclEngineRegistration.unregister();
			mockOclEngineRegistration = null;
		}

		// Small delay to allow service unregistration to propagate
		Thread.sleep(200);

		super.teardown();
	}

	// ========== Validation Tests ==========

	@Test
	public void testValidate_Success() throws Exception {
		EPackage validPackage = TestHelper.createTestEPackage("http://test.com/valid/1.0", "ValidPackage", "vp");
		String xmiContent = TestHelper.serializeToXMI(validPackage, resourceSet);

		Response response = validateTarget().request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(200, response.getStatus(), "Should return HTTP 200 OK");

		String responseContent = response.readEntity(String.class);
		assertNotNull(responseContent, "Should return diagnostic content");
	}

	@Test
	public void testValidate_ResponseContainsDiagnosticInfo() throws Exception {
		EPackage validPackage = TestHelper.createTestEPackage("http://test.com/diag/1.0", "DiagPackage", "dp");
		String xmiContent = TestHelper.serializeToXMI(validPackage, resourceSet);

		Response response = validateTarget().request("application/json")
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

		Response response = validateTarget()
				.queryParam("mediaType", "application/unsupported").request("application/xmi")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(415, response.getStatus(), "Should return HTTP 415 Unsupported Media Type");
	}

	@Test
	public void testValidate_WithSupportedMediaTypeQueryParam() throws Exception {
		EPackage validPackage = TestHelper.createTestEPackage("http://test.com/mt/1.0", "MtPackage", "mt");
		String xmiContent = TestHelper.serializeToXMI(validPackage, resourceSet);

		Response response = validateTarget()
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

		Response response = validateTarget().request("text/plain")
				.post(Entity.entity(xmiContent, "application/xmi"));

		assertEquals(406, response.getStatus(), "Should return HTTP 406 Not Acceptable for unsupported Accept header");
	}

	/** /validate */
	private WebTarget validateTarget() {
		return baseTarget().path("validate");
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.fennec.model.atlas.rest.tests.AbstractRestTest#getResourceName()
	 */
	@Override
	String getResourceName() {
		return "ObjectValidationResource";
	}
}
