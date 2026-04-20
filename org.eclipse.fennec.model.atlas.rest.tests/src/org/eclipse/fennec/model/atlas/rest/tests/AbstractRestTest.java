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
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.model.atlas.rest.tests.helper.ResourceAware;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestAnnotations;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestHelper;
import org.gecko.emf.rest.annotations.RequireEMFMessageBodyReaderWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.service.jakartars.whiteboard.annotations.RequireJakartarsWhiteboard;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;

/**
 * 
 * @author ilenia
 * @since Apr 10, 2026
 */
@RequireEMF
@RequireEMFMessageBodyReaderWriter
@RequireJakartarsWhiteboard
@RequireConfigurationAdmin
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public abstract class AbstractRestTest {
	
	@InjectService(filter = "(emf.name=workflowapi)")
	ResourceSet resourceSet;

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
		// Wait for the ObjectRegistryResource to be registered in Jakarta REST runtime
		ResourceAware resourceAware = ResourceAware.create(context, getResourceName());
		boolean resourceReady = resourceAware.waitForResource(15, TimeUnit.SECONDS);

		assertTrue(resourceReady, "ObjectRegistryResource should be registered within 15 seconds. "
				+ "Check that the resource is properly configured and the Jakarta REST runtime is working.");
		
		Thread.sleep(2000); //give time to all the services to come up
	}
	
	abstract String getResourceName();
}
