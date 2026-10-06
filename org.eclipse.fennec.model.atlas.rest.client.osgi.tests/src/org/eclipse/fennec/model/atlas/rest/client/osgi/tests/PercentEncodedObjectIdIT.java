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
package org.eclipse.fennec.model.atlas.rest.client.osgi.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Hashtable;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Response;

/**
 * Issue #340: the client fetches an object whose id contains a percent-encoded character.
 *
 * <p>
 * Compiled QVT-O units are stored under their URL-encoded entry key, so their ids carry
 * a literal {@code %2F} / {@code %3A}. JAX-RS leaves percent-encoded sequences in a
 * query value as they are, so the client sent {@code objectId=qvto%2Fcompiled...}, the
 * server decoded it once to {@code qvto/compiled...} and never found the stored id:
 * the listing named the unit, every fetch of it came back empty.
 * </p>
 *
 * <p>
 * The test serves a minimal Atlas from a Jakarta REST whiteboard it configures in the
 * test framework, so no Atlas container is needed. Like the server, it decodes the query once and answers only for the exact
 * stored id. The client is configured through ConfigAdmin, as a consumer configures it,
 * and read through the {@code ReadableScopeService} it publishes for the scope.
 * </p>
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class PercentEncodedObjectIdIT {

	/** Factory PID of the client (see {@code AtlasClientComponent.PID}). */
	private static final String PID = "org.eclipse.fennec.model.atlas.rest.client";

	/** The port of the HTTP endpoint {@link FakeAtlasWhiteboard} configures. */
	private static final int HTTP_PORT = 8191;
	private static final String BASE_URI = "http://localhost:" + HTTP_PORT + "/rest/fake-atlas";

	private static final String SCOPE = "percentscope";
	private static final String REGISTRY = "transformations";

	/** A compiled unit's id as the server stores and lists it: the URL-encoded entry key. */
	private static final String UNIT_ID = "qvto%2Fcompiled%2FAuslastungToOpenData%2Fm2x1%3A4de7f0";
	/** An id with braces, which a JAX-RS query value would read as a URI template. */
	private static final String BRACED_ID = "unit{draft}";

	private static final String UNIT_XMI = """
			<?xml version="1.0" encoding="UTF-8"?>
			<ecore:EClass xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI"
			    xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore" name="%s"/>
			""";

	private final List<ServiceRegistration<?>> registrations = new CopyOnWriteArrayList<>();

	@AfterEach
	void unregister() {
		registrations.forEach(ServiceRegistration::unregister);
		registrations.clear();
	}

	/** An HTTP endpoint and a Jakarta REST whiteboard on it, serving the fake Atlas. */
	@WithFactoryConfiguration(factoryPid = "org.apache.felix.http", name = "fakeAtlasHttp", location = "?", properties = {
			@Property(key = "org.osgi.service.http.port", value = "" + HTTP_PORT),
			@Property(key = "org.osgi.service.http.host", value = "localhost"),
			@Property(key = "org.apache.felix.http.context_path", value = "/"),
			@Property(key = "org.apache.felix.http.name", value = "Fake Atlas HTTP"),
			@Property(key = "org.apache.felix.http.runtime.init.id", value = "fakeAtlasHttp") })
	@WithFactoryConfiguration(factoryPid = "JakartarsServletWhiteboardRuntimeComponent", name = "fakeAtlasRest", location = "?", properties = {
			@Property(key = "jersey.jakartars.whiteboard.name", value = "Fake Atlas REST"),
			@Property(key = "jersey.context.path", value = "rest"),
			@Property(key = "osgi.http.whiteboard.target", value = "(id=fakeAtlasHttp)") })
	@Retention(RetentionPolicy.RUNTIME)
	@interface FakeAtlasWhiteboard {
	}

	@Test
	@FakeAtlasWhiteboard
	public void anObjectIdWithAPercentEncodedCharacterIsFetchedByTheIdTheListingReturned(
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PID,
					name = "percent-encoded-ids", location = "?")) Configuration configuration,
			@InjectService(cardinality = 0, filter = "(atlas.scope=" + SCOPE + ")") ServiceAware<ReadableScopeService> scopeAware,
			@InjectBundleContext BundleContext context) throws Exception {
		FakeAtlas atlas = new FakeAtlas();
		Hashtable<String, Object> resourceProps = new Hashtable<>();
		resourceProps.put("osgi.jakartars.resource", Boolean.TRUE);
		registrations.add(context.registerService(FakeAtlas.class, atlas, resourceProps));
		awaitFakeAtlas();

		Hashtable<String, Object> clientProps = new Hashtable<>();
		clientProps.put("base.uri", BASE_URI);
		clientProps.put("mode", "LAZY");
		clientProps.put("mode.strict", Boolean.FALSE);
		clientProps.put("scope.allow.list", new String[] { SCOPE });
		configuration.update(clientProps);

		@SuppressWarnings("unchecked")
		ReadableScopeService<EObject> scope = scopeAware.waitForService(TimeUnit.SECONDS.toMillis(15));
		assertNotNull(scope, "The client publishes a ReadableScopeService for scope " + SCOPE);

		List<String> ids = scope.listObjectIds(REGISTRY);
		assertEquals(List.of(UNIT_ID, BRACED_ID), ids, "The listing names both objects by their stored ids");
		for (String id : ids) {
			Optional<EObject> object = scope.get(REGISTRY, id);
			assertTrue(object.isPresent(), "The object '" + id + "' the listing returned must be fetchable by that id; "
					+ "the server received the ids " + atlas.receivedIds);
			assertEquals(nameOf(id), ((EClass) object.get()).getName(), "The fetched object is the one stored as " + id);
		}
	}

	private static String nameOf(String id) {
		return id.equals(UNIT_ID) ? "AuslastungToOpenData" : "BracedUnit";
	}

	/** Waits until the whiteboard serves the fake Atlas. */
	private static void awaitFakeAtlas() throws Exception {
		HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
		HttpRequest probe = HttpRequest.newBuilder(URI.create(BASE_URI + "/scopes/" + SCOPE)).GET().build();
		long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(15);
		while (System.currentTimeMillis() < deadline) {
			try {
				if (http.send(probe, HttpResponse.BodyHandlers.discarding()).statusCode() == 200) {
					return;
				}
			} catch (java.io.IOException notYetListening) {
				// the whiteboard is still coming up
			}
			Thread.sleep(100L);
		}
		fail("The fake Atlas was not served at " + BASE_URI + " within 15 seconds");
	}

	/**
	 * The three Atlas endpoints the client's object read uses. Like the server, the
	 * query is decoded once and only the exact stored id is found.
	 */
	@Path("fake-atlas")
	public static class FakeAtlas {

		final List<String> receivedIds = new CopyOnWriteArrayList<>();

		@GET
		@Path("scopes/{scope}")
		@Produces("application/json")
		public String scope(@PathParam("scope") String scope) {
			return "{\"name\":\"" + scope + "\",\"registries\":[{\"name\":\"" + REGISTRY + "\",\"type\":\"TRANSFORMATION\"}]}";
		}

		@GET
		@Path("{scope}/registries/{registry}")
		@Produces("application/json")
		public String list() {
			return "{\"metadata\":[{\"objectId\":\"" + UNIT_ID + "\"},{\"objectId\":\"" + BRACED_ID + "\"}]}";
		}

		@GET
		@Path("{scope}/registries/{registry}/content")
		@Produces("application/xmi")
		public Response content(@QueryParam("objectId") String objectId) {
			receivedIds.add(objectId);
			if (UNIT_ID.equals(objectId) || BRACED_ID.equals(objectId)) {
				return Response.ok(UNIT_XMI.formatted(nameOf(objectId))).build();
			}
			return Response.noContent().build();
		}
	}
}
