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

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.model.atlas.rest.tests.helper.ResourceAware;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestAnnotations;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestHelper;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.service.jakartars.whiteboard.annotations.RequireJakartarsWhiteboard;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

/**
 * 
 * @author ilenia
 * @since Apr 10, 2026
 */
@RequireEMF
@RequireJakartarsWhiteboard
@RequireConfigurationAdmin
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public abstract class AbstractRestTest {
	
	@InjectService(filter = "(emf.name=workflowapi)")
	ResourceSet resourceSet;

	// The scope under test (set up via @ParentScopeServiceSetup on every test).
	// Used to gate requests until the scope stack (and its registries) is up, so
	// ModelAtlasRequestFilter's scope validation no longer races startup.
	// cardinality 0 so field injection does not block before the configuration is
	// applied; the test awaits it in ensureResourceAvailability.
	@InjectService(cardinality = 0, filter = "(scope.name=test-scope)")
	ServiceAware<ScopeService> testScopeService;

	// The per-(scope, stage) ResourceSet the codec's @Context injection resolves
	// via ScopedResourceSetProvider. Published by the registry chain once the
	// scope is up; awaited so requests don't race it (otherwise the provider
	// answers 400). The 'release' stage is configured for every test scope, so
	// this also covers the draft/approved ResourceSets published in the same
	// registry activation.
	@InjectService(cardinality = 0, filter = "(&(scope.name=test-scope)(stage.name=release))")
	ServiceAware<ResourceSet> scopedResourceSet;

	@TempDir
	Path tempDir;

	@InjectService
	ClientBuilder clientBuilder;

	
	public static final String BASE_URL = "http://localhost:8185/rest";
		
	protected Client restClient;
	
	@BeforeEach
	public void setup(@InjectBundleContext BundleContext context) throws Exception {
		// Set system property for template argument resolution
		System.setProperty(TestAnnotations.PROP_TEMP_DIR, tempDir.toString());

		// Setup REST client
		restClient = clientBuilder.build();


		// Ensure XMI factory is registered
		TestHelper.ensureXMIFactory(resourceSet);
	}

	@AfterEach
	public void teardown() throws Exception {
		if (nonNull(restClient)) {
			restClient.close();
			restClient = null;
		}
	}
	
	/** {@code /} - root target at {@link #BASE_URL}. */
	protected WebTarget baseTarget() {
		return restClient.target(BASE_URL);
	}

	/** {@code /{scope}} */
	protected WebTarget scopeTarget(String scope) {
		return baseTarget().path(scope);
	}

	/** {@code /{TEST_SCOPE_NAME}} */
	protected WebTarget scopeTarget() {
		return scopeTarget(TestAnnotations.TEST_SCOPE_NAME);
	}

	public void ensureResourceAvailability(BundleContext context) throws InterruptedException {
		// Wait for the JAX-RS resource to be registered in the Jakarta REST runtime
		ResourceAware resourceAware = ResourceAware.create(context, getResourceName());
		boolean resourceReady = resourceAware.waitForResource(15, TimeUnit.SECONDS);

		assertTrue(resourceReady, getResourceName() + " should be registered within 15 seconds. "
				+ "Check that the resource is properly configured and the Jakarta REST runtime is working.");

		// The JAX-RS resource being registered does not mean the scope stack is up.
		// Deterministically wait for the backing services instead of sleeping:
		//  - the ScopeService so ModelAtlasRequestFilter accepts the scope, and
		//  - the per-(scope, stage) ResourceSet so the codec's @Context ResourceSet
		//    (resolved by ScopedResourceSetProvider) is available; otherwise the
		//    provider answers 400.
		assertNotNull(testScopeService.waitForService(TimeUnit.SECONDS.toMillis(15)),
				"ScopeService for '" + TestAnnotations.TEST_SCOPE_NAME + "' should be available within 15 seconds.");
		assertNotNull(scopedResourceSet.waitForService(TimeUnit.SECONDS.toMillis(15)),
				"ResourceSet for scope '" + TestAnnotations.TEST_SCOPE_NAME
						+ "' / stage 'release' should be available within 15 seconds.");
	}
	
	abstract String getResourceName();

	/**
	 * Asserts the HTTP status of {@code response}, including the response entity
	 * in the failure message so server-side error details (the JAX-RS error body)
	 * are visible when the status is unexpected. The entity is buffered first, so
	 * callers can still read it afterwards.
	 *
	 * @param expected the expected HTTP status code
	 * @param response the JAX-RS client response (entity gets buffered)
	 * @param message  the base assertion message
	 */
	protected static void assertStatus(int expected, Response response, String message) {
		String body;
		try {
			response.bufferEntity();
			body = response.readEntity(String.class);
		} catch (RuntimeException e) {
			body = "<unreadable entity: " + e + ">";
		}
		assertEquals(expected, response.getStatus(), message + " | response body: " + body);
	}
}
