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
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.TimeUnit;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.data.atlas.jpa.rest.tests.helper.ResourceAware;
import org.eclipse.fennec.data.atlas.jpa.rest.tests.helper.TestAnnotations;
import org.eclipse.fennec.data.atlas.jpa.rest.tests.helper.TestAnnotations.DataFolderWatcherConfig;
import org.eclipse.fennec.data.atlas.jpa.rest.tests.helper.TestHelper;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig;
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
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.Response;

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
@ExtendWith(ConfigurationExtension.class)
public class JpaConnectionResourceTest {

	private static final String BASE_URL = "http://localhost:8185/rest/jpa";

	private static final String XMI_NO_DATASOURCE = """
			<?xml version="1.0" encoding="UTF-8"?>
			<jpamapping:JpaMappingConfig xmi:version="2.0"
			    xmlns:xmi="http://www.omg.org/XMI"
			    xmlns:jpamapping="http://eclipse.org/fennec/data/atlas/jpamapping/1.0.0"
			    name="no-ds"/>
			""";

	private static final String XMI_UNSUPPORTED_DIALECT = """
			<?xml version="1.0" encoding="UTF-8"?>
			<jpamapping:JpaMappingConfig xmi:version="2.0"
			    xmlns:xmi="http://www.omg.org/XMI"
			    xmlns:jpamapping="http://eclipse.org/fennec/data/atlas/jpamapping/1.0.0"
			    name="postgres-mapping">
			  <dataSource driverClass="org.postgresql.Driver" jdbcUrl="jdbc:postgresql://localhost:5432/mydb" username="user" dialect="POSTGRES"/>
			</jpamapping:JpaMappingConfig>
			""";

	private static final String XMI_WRONG_DRIVER = """
			<?xml version="1.0" encoding="UTF-8"?>
			<jpamapping:JpaMappingConfig xmi:version="2.0"
			    xmlns:xmi="http://www.omg.org/XMI"
			    xmlns:jpamapping="http://eclipse.org/fennec/data/atlas/jpamapping/1.0.0"
			    name="wrong-driver-mapping">
			  <dataSource driverClass="com.mysql.jdbc.Driver" jdbcUrl="jdbc:h2:mem:testdb" username="sa" dialect="H2"/>
			</jpamapping:JpaMappingConfig>
			""";

	private static final String XMI_NO_JDBC_URL = """
			<?xml version="1.0" encoding="UTF-8"?>
			<jpamapping:JpaMappingConfig xmi:version="2.0"
			    xmlns:xmi="http://www.omg.org/XMI"
			    xmlns:jpamapping="http://eclipse.org/fennec/data/atlas/jpamapping/1.0.0"
			    name="no-url-mapping">
			  <dataSource driverClass="org.h2.Driver" username="sa" dialect="H2"/>
			</jpamapping:JpaMappingConfig>
			""";

	private static final String XMI_VALID_H2 = """
			<?xml version="1.0" encoding="UTF-8"?>
			<jpamapping:JpaMappingConfig xmi:version="2.0"
			    xmlns:xmi="http://www.omg.org/XMI"
			    xmlns:jpamapping="http://eclipse.org/fennec/data/atlas/jpamapping/1.0.0"
			    name="valid-h2">
			  <dataSource driverClass="org.h2.Driver" jdbcUrl="jdbc:h2:mem:validtestdb" username="sa" dialect="H2"/>
			</jpamapping:JpaMappingConfig>
			""";

	@InjectService
	ClientBuilder clientBuilder;

	@InjectService
	ResourceSet resourceSet;

	private Client restClient;


	@BeforeEach
	public void before(@InjectBundleContext BundleContext ctx) throws InterruptedException {
		restClient = clientBuilder.build();
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
		ResourceAware resourceAware = ResourceAware.create(context, "JpaConnectionResource");
		boolean resourceReady = resourceAware.waitForResource(15, TimeUnit.SECONDS);
		assertTrue(resourceReady, "JpaConnectionResource should be registered within 15 seconds. "
				+ "Check that the resource is properly configured and the Jakarta REST runtime is working.");
	}

	@Test
	@DataFolderWatcherConfig
	public void testResourceAvailability(@InjectBundleContext BundleContext ctx) throws InterruptedException {
		ensureResourceAvailability(ctx);
	}

	@Test
	public void testPostConnection_missingDataSource(@InjectBundleContext BundleContext ctx) throws InterruptedException {
		ensureResourceAvailability(ctx);

		Response response = restClient.target(BASE_URL + "/test").request()
				.post(Entity.entity(XMI_NO_DATASOURCE, "application/xmi"));

		assertEquals(400, response.getStatus());
		assertEquals("JpaMappingConfig has no DataSourceConfig", response.readEntity(String.class));
	}

	@Test
	public void testPostConnection_unsupportedDialect(@InjectBundleContext BundleContext ctx) throws InterruptedException {
		ensureResourceAvailability(ctx);

		Response response = restClient.target(BASE_URL + "/test").request()
				.post(Entity.entity(XMI_UNSUPPORTED_DIALECT, "application/xmi"));

		assertEquals(400, response.getStatus());
		assertTrue(response.readEntity(String.class).contains("Only H2 is currently supported"));
	}

	@Test
	public void testPostConnection_unsupportedDriverClass(@InjectBundleContext BundleContext ctx) throws InterruptedException {
		ensureResourceAvailability(ctx);

		Response response = restClient.target(BASE_URL + "/test").request()
				.post(Entity.entity(XMI_WRONG_DRIVER, "application/xmi"));

		assertEquals(400, response.getStatus());
		assertTrue(response.readEntity(String.class).contains("Only org.h2.Driver is currently supported"));
	}

	@Test
	public void testPostConnection_missingJdbcUrl(@InjectBundleContext BundleContext ctx) throws InterruptedException {
		ensureResourceAvailability(ctx);

		Response response = restClient.target(BASE_URL + "/test").request()
				.post(Entity.entity(XMI_NO_JDBC_URL, "application/xmi"));

		assertEquals(400, response.getStatus());
		assertEquals("DataSourceConfig has no JDBC URL", response.readEntity(String.class));
	}

	@Test
	public void testPostConnection_validH2(@InjectBundleContext BundleContext ctx) throws InterruptedException {
		ensureResourceAvailability(ctx);

		Response response = restClient.target(BASE_URL + "/test").request()
				.post(Entity.entity(XMI_VALID_H2, "application/xmi"));

		assertEquals(200, response.getStatus());
	}

	@Test
	@DataFolderWatcherConfig
	public void testConnectionByName(
			@InjectBundleContext BundleContext ctx,
			@InjectService(cardinality = 0, filter = "(jpamapping.name=" + TestAnnotations.JPA_MAPPING_NAME + ")")
			ServiceAware<JpaMappingConfig> configAware) throws InterruptedException {
		ensureResourceAvailability(ctx);
		assertNotNull(configAware.waitForService(10_000), "JpaMappingConfig for " + TestAnnotations.JPA_MAPPING_NAME + " should be registered");

		Response response = restClient.target(BASE_URL + "/test/" + TestAnnotations.JPA_MAPPING_NAME).request().get();

		assertEquals(200, response.getStatus());
	}
	
	@Test
	@DataFolderWatcherConfig
	public void testConnectionByName_wrongName_shouldReturnTimeout(
			@InjectBundleContext BundleContext ctx,
			@InjectService(cardinality = 0, filter = "(jpamapping.name=" + TestAnnotations.JPA_MAPPING_NAME + ")")
			ServiceAware<JpaMappingConfig> configAware) throws InterruptedException {
		ensureResourceAvailability(ctx);
		assertNotNull(configAware.waitForService(10_000), "JpaMappingConfig for " + TestAnnotations.JPA_MAPPING_NAME + " should be registered");

		Response response = restClient.target(BASE_URL + "/test/" + "non-existing-data-source").request().get();

		assertEquals(408, response.getStatus());
	}

}
