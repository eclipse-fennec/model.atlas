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
package org.eclipse.fennec.data.atlas.jpa.rest.tests;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.data.atlas.jpa.rest.tests.helper.ResourceAware;
import org.eclipse.fennec.data.atlas.jpa.rest.tests.helper.TestAnnotations;
import org.eclipse.fennec.data.atlas.jpa.rest.tests.helper.TestAnnotations.DataFolderWatcherConfig;
import org.eclipse.fennec.data.atlas.jpa.rest.tests.helper.TestHelper;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.persistence.eorm.EntityMappings;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.service.jakartars.whiteboard.annotations.RequireJakartarsWhiteboard;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import jakarta.persistence.EntityManagerFactory;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.Response;

/**
 * See documentation here:
 * 	https://github.com/osgi/osgi-test
 * 	https://github.com/osgi/osgi-test/wiki
 * Examples: https://github.com/osgi/osgi-test/tree/main/examples
 */
@RequireEMF
@RequireJakartarsWhiteboard
@RequireConfigurationAdmin
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class JpaDataResourceTest {

	private static final String BASE_URL = "http://localhost:8185/rest/jpa/data/data";
	private static final String E_PACKAGE_URI = "http://example.org/jpa/demo/1.0";

	private static final String MAPPINGS_FILTER =
			"(eorm.name=" + TestAnnotations.JPA_MAPPING_NAME + ")";
	private static final String EMF_FILTER = "(osgi.unit.name=*)";

	@InjectService
	ClientBuilder clientBuilder;

	@InjectService
	ResourceSet resourceSet;

	private Client restClient;

	@BeforeEach
	public void before(@InjectBundleContext BundleContext ctx) {
		restClient = clientBuilder.build();
		TestHelper.ensureXMIFactory(resourceSet);
	}

	@AfterEach
	public void teardown() {
		if (nonNull(restClient)) {
			restClient.close();
			restClient = null;
		}
	}

	private void ensureResourceAvailability(BundleContext ctx) throws InterruptedException {
		ResourceAware resourceAware = ResourceAware.create(ctx, "JpaDataResource");
		boolean resourceReady = resourceAware.waitForResource(15, TimeUnit.SECONDS);
		assertTrue(resourceReady, "JpaDataResource should be registered within 15 seconds. "
				+ "Check that the resource is properly configured and the Jakarta REST runtime is working.");
	}

	private void awaitPipeline(
			ServiceAware<EntityMappings> mappingsAware,
			ServiceAware<EntityManagerFactory> emfAware) throws InterruptedException {
		assertNotNull(mappingsAware.waitForService(15_000),
				"EntityMappings for '" + TestAnnotations.JPA_MAPPING_NAME + "' should be registered");
		assertNotNull(emfAware.waitForService(30_000),
				"EntityManagerFactory should be registered (one pipeline per test).");
	}

	@Test
	@DataFolderWatcherConfig
	public void testResourceAvailability(@InjectBundleContext BundleContext ctx) throws InterruptedException {
		ensureResourceAvailability(ctx);
	}

	@Test
	@DataFolderWatcherConfig
	public void testGetAll_withEPackageUri(
			@InjectBundleContext BundleContext ctx,
			@InjectService(cardinality = 0, filter = MAPPINGS_FILTER) ServiceAware<EntityMappings> mappingsAware,
			@InjectService(cardinality = 0, filter = EMF_FILTER) ServiceAware<EntityManagerFactory> emfAware)
			throws InterruptedException {
		ensureResourceAvailability(ctx);
		awaitPipeline(mappingsAware, emfAware);

		Response response = restClient.target(BASE_URL + "/Employee")
				.queryParam("ePackageUri", E_PACKAGE_URI)
				.request()
				.get();

		assertEquals(200, response.getStatus());
	}

	@Test
	@DataFolderWatcherConfig
	public void testGetAll_withEPackageUri_schemaTable(
			@InjectBundleContext BundleContext ctx,
			@InjectService(cardinality = 0, filter = MAPPINGS_FILTER) ServiceAware<EntityMappings> mappingsAware,
			@InjectService(cardinality = 0, filter = EMF_FILTER) ServiceAware<EntityManagerFactory> emfAware)
			throws InterruptedException {
		ensureResourceAvailability(ctx);
		awaitPipeline(mappingsAware, emfAware);

		Response response = restClient.target(BASE_URL + "/Invoice")
				.queryParam("ePackageUri", E_PACKAGE_URI)
				.request()
				.get();

		assertEquals(200, response.getStatus());
	}

	@Test
	@DataFolderWatcherConfig
	public void testGetAll_withoutEPackageUri(
			@InjectBundleContext BundleContext ctx,
			@InjectService(cardinality = 0, filter = MAPPINGS_FILTER) ServiceAware<EntityMappings> mappingsAware,
			@InjectService(cardinality = 0, filter = EMF_FILTER) ServiceAware<EntityManagerFactory> emfAware)
			throws InterruptedException {
		ensureResourceAvailability(ctx);
		awaitPipeline(mappingsAware, emfAware);

		Response response = restClient.target(BASE_URL + "/Employee").request().get();

		assertEquals(200, response.getStatus());
	}

	@Test
	@DataFolderWatcherConfig
	public void testGetAll_withLimit(
			@InjectBundleContext BundleContext ctx,
			@InjectService(cardinality = 0, filter = MAPPINGS_FILTER) ServiceAware<EntityMappings> mappingsAware,
			@InjectService(cardinality = 0, filter = EMF_FILTER) ServiceAware<EntityManagerFactory> emfAware)
			throws InterruptedException {
		ensureResourceAvailability(ctx);
		awaitPipeline(mappingsAware, emfAware);

		Response response = restClient.target(BASE_URL + "/Employee")
				.queryParam("ePackageUri", E_PACKAGE_URI)
				.queryParam("limit", 2)
				.request()
				.get();

		assertEquals(200, response.getStatus());
	}

	@Test
	@DataFolderWatcherConfig
	public void testGetAll_classNotFound_shouldReturn400(
			@InjectBundleContext BundleContext ctx,
			@InjectService(cardinality = 0, filter = MAPPINGS_FILTER) ServiceAware<EntityMappings> mappingsAware,
			@InjectService(cardinality = 0, filter = EMF_FILTER) ServiceAware<EntityManagerFactory> emfAware)
			throws InterruptedException {
		ensureResourceAvailability(ctx);
		awaitPipeline(mappingsAware, emfAware);

		Response response = restClient.target(BASE_URL + "/NonExistent")
				.queryParam("ePackageUri", E_PACKAGE_URI)
				.request()
				.get();

		assertEquals(400, response.getStatus());
	}

	@Test
	@DataFolderWatcherConfig
	public void testGetById_withEPackageUri(
			@InjectBundleContext BundleContext ctx,
			@InjectService(cardinality = 0, filter = MAPPINGS_FILTER) ServiceAware<EntityMappings> mappingsAware,
			@InjectService(cardinality = 0, filter = EMF_FILTER) ServiceAware<EntityManagerFactory> emfAware)
			throws InterruptedException {
		ensureResourceAvailability(ctx);
		awaitPipeline(mappingsAware, emfAware);

		Response response = restClient.target(BASE_URL + "/Employee/1")
				.queryParam("ePackageUri", E_PACKAGE_URI)
				.request()
				.get();

		assertEquals(200, response.getStatus());
	}

	@Test
	@DataFolderWatcherConfig
	public void testGetById_withEPackageUri_schemaTable(
			@InjectBundleContext BundleContext ctx,
			@InjectService(cardinality = 0, filter = MAPPINGS_FILTER) ServiceAware<EntityMappings> mappingsAware,
			@InjectService(cardinality = 0, filter = EMF_FILTER) ServiceAware<EntityManagerFactory> emfAware)
			throws InterruptedException {
		ensureResourceAvailability(ctx);
		awaitPipeline(mappingsAware, emfAware);

		Response response = restClient.target(BASE_URL + "/Invoice/1")
				.queryParam("ePackageUri", E_PACKAGE_URI)
				.request()
				.get();

		assertEquals(200, response.getStatus());
	}

	@Test
	@DataFolderWatcherConfig
	public void testGetById_withoutEPackageUri(
			@InjectBundleContext BundleContext ctx,
			@InjectService(cardinality = 0, filter = MAPPINGS_FILTER) ServiceAware<EntityMappings> mappingsAware,
			@InjectService(cardinality = 0, filter = EMF_FILTER) ServiceAware<EntityManagerFactory> emfAware)
			throws InterruptedException {
		ensureResourceAvailability(ctx);
		awaitPipeline(mappingsAware, emfAware);

		Response response = restClient.target(BASE_URL + "/Employee/1").request().get();

		assertEquals(200, response.getStatus());
	}

	@Test
	@DataFolderWatcherConfig
	public void testGetById_noResult_shouldReturn204(
			@InjectBundleContext BundleContext ctx,
			@InjectService(cardinality = 0, filter = MAPPINGS_FILTER) ServiceAware<EntityMappings> mappingsAware,
			@InjectService(cardinality = 0, filter = EMF_FILTER) ServiceAware<EntityManagerFactory> emfAware)
			throws InterruptedException {
		ensureResourceAvailability(ctx);
		awaitPipeline(mappingsAware, emfAware);

		Response response = restClient.target(BASE_URL + "/Employee/999")
				.queryParam("ePackageUri", E_PACKAGE_URI)
				.request()
				.get();

		assertEquals(204, response.getStatus());
	}

	@Test
	@DataFolderWatcherConfig
	public void testGetById_invalidId_shouldReturn400(
			@InjectBundleContext BundleContext ctx,
			@InjectService(cardinality = 0, filter = MAPPINGS_FILTER) ServiceAware<EntityMappings> mappingsAware,
			@InjectService(cardinality = 0, filter = EMF_FILTER) ServiceAware<EntityManagerFactory> emfAware)
			throws InterruptedException {
		ensureResourceAvailability(ctx);
		awaitPipeline(mappingsAware, emfAware);

		Response response = restClient.target(BASE_URL + "/Employee/notanumber")
				.queryParam("ePackageUri", E_PACKAGE_URI)
				.request()
				.get();

		assertEquals(400, response.getStatus());
		assertTrue(response.readEntity(String.class).contains("Cannot parse id"));
	}

}
