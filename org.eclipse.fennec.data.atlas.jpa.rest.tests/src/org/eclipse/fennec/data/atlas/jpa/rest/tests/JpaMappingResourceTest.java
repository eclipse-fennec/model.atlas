/**
 * Copyright (c) 2012 - 2023 Data In Motion and others.
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
package org.eclipse.fennec.data.atlas.jpa.rest.tests;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.data.atlas.jpa.rest.tests.helper.ResourceAware;
import org.eclipse.fennec.data.atlas.jpa.rest.tests.helper.TestHelper;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.gecko.emf.rest.annotations.RequireEMFMessageBodyReaderWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.service.jakartars.whiteboard.annotations.RequireJakartarsWhiteboard;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;

//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;

/**
 * See documentation here: 
 * 	https://github.com/osgi/osgi-test
 * 	https://github.com/osgi/osgi-test/wiki
 * Examples: https://github.com/osgi/osgi-test/tree/main/examples
 */
@RequireEMF
@RequireEMFMessageBodyReaderWriter
@RequireJakartarsWhiteboard
@RequireConfigurationAdmin
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class JpaMappingResourceTest {

	@InjectService
	ClientBuilder clientBuilder;

	@InjectService
	ResourceSet resourceSet;

	private Client restClient;


	@BeforeEach
	public void before(@InjectBundleContext BundleContext ctx) throws InterruptedException {
		// Setup REST client
		restClient = clientBuilder.build();

		// Ensure XMI factory is registered
		TestHelper.ensureXMIFactory(resourceSet);
		
		
	}

	@AfterEach
	public void teardown(@InjectBundleContext BundleContext context) throws Exception {

		if (nonNull(restClient)) {
			restClient.close();
			restClient = null;
		}
	}
	
	private void ensureResourceAvailability(BundleContext context) throws InterruptedException {
		ResourceAware resourceAware = ResourceAware.create(context, "JpaMappingResource");
		boolean resourceReady = resourceAware.waitForResource(15, TimeUnit.SECONDS);

		assertTrue(resourceReady, "JpaMappingResource should be registered within 15 seconds. "
				+ "Check that the resource is properly configured and the Jakarta REST runtime is working.");

	}
	
	@Test
	public void test(@InjectBundleContext BundleContext ctx) throws InterruptedException {
		
		ensureResourceAvailability(ctx);
		
	}

}
