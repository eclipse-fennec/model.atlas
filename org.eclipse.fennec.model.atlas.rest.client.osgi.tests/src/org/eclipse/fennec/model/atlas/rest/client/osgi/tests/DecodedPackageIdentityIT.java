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
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
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
 * Issue #347: objects read through the client bind the same {@link EPackage} instances on
 * every read, and the ones the framework resolves that scope and stage with.
 *
 * <p>
 * Each read used to decode against a package registry of its own. A stage-free read still
 * ended up in the client's nsURI cache, but a stage-explicit one fetched every referenced
 * package anew, so two reads of the same stage bound two copies, and none of them was the
 * copy the framework held. Type checks between such objects - a QVT-O type filter,
 * {@code isInstance}, {@code oclIsKindOf} - then never match.
 * </p>
 *
 * <p>
 * A minimal Atlas is served from a Jakarta REST whiteboard the test configures, so no Atlas
 * container is needed. Its scopes have a schema registry staged {@code draft → release}
 * and an object registry, and every package request is counted. Each test configures the
 * client for a scope of its own through ConfigAdmin and reads through the
 * {@code ReadableScopeService} it publishes. Every read is of a different object, because a
 * repeated read of one object is served from the client's object cache.
 * </p>
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class DecodedPackageIdentityIT {

	/** Factory PID of the client (see {@code AtlasClientComponent.PID}). */
	private static final String PID = "org.eclipse.fennec.model.atlas.rest.client";

	private static final int HTTP_PORT = 8192;
	private static final String BASE_URI = "http://localhost:" + HTTP_PORT + "/rest/identity-atlas";

	private static final String OBJECTS = "objects";
	private static final String DRAFT = "draft";
	private static final String RELEASE = "release";

	private static final String NS_URI = "http://atlas.example/test/identity/1.0";

	private static final String ECORE = """
			<?xml version="1.0" encoding="UTF-8"?>
			<ecore:EPackage xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI"
			    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			    xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore"
			    name="identity" nsURI="%s" nsPrefix="idt">
			  <eClassifiers xsi:type="ecore:EClass" name="Thing">
			    <eStructuralFeatures xsi:type="ecore:EAttribute" name="name"
			        eType="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString"/>
			  </eClassifiers>
			</ecore:EPackage>
			""".formatted(NS_URI);

	private static final String THING = """
			<?xml version="1.0" encoding="UTF-8"?>
			<idt:Thing xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:idt="%s" name="%s"/>
			""";

	private final List<ServiceRegistration<?>> registrations = new CopyOnWriteArrayList<>();

	@AfterEach
	void unregister() {
		registrations.forEach(ServiceRegistration::unregister);
		registrations.clear();
	}

	/** An HTTP endpoint and a Jakarta REST whiteboard on it, serving the fake Atlas. */
	@WithFactoryConfiguration(factoryPid = "org.apache.felix.http", name = "identityAtlasHttp", location = "?", properties = {
			@Property(key = "org.osgi.service.http.port", value = "" + HTTP_PORT),
			@Property(key = "org.osgi.service.http.host", value = "localhost"),
			@Property(key = "org.apache.felix.http.context_path", value = "/"),
			@Property(key = "org.apache.felix.http.name", value = "Identity Atlas HTTP"),
			@Property(key = "org.apache.felix.http.runtime.init.id", value = "identityAtlasHttp") })
	@WithFactoryConfiguration(factoryPid = "JakartarsServletWhiteboardRuntimeComponent", name = "identityAtlasRest", location = "?", properties = {
			@Property(key = "jersey.jakartars.whiteboard.name", value = "Identity Atlas REST"),
			@Property(key = "jersey.context.path", value = "rest"),
			@Property(key = "osgi.http.whiteboard.target", value = "(id=identityAtlasHttp)") })
	@Retention(RetentionPolicy.RUNTIME)
	@interface FakeAtlasWhiteboard {
	}

	/**
	 * Without a registry chain for the scope the client decodes against registries of its
	 * own: one per stage, and for the final stage the stage-free one.
	 */
	@Test
	@FakeAtlasWhiteboard
	public void readsOfAStageBindOnePackageAndTheFinalStageBindsTheStageFreeOne(
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PID,
					name = "identity-own", location = "?")) Configuration configuration,
			@InjectBundleContext BundleContext context) throws Exception {
		String scope = "identityown";
		FakeAtlas atlas = serveFakeAtlas(context);
		configuration.update(clientProps(scope, null));
		ReadableScopeService<EObject> scopeService = awaitScopeService(context, scope);

		EPackage releaseOne = packageOf(scopeService.registryView(OBJECTS, RELEASE).get("one").orElseThrow());
		EPackage releaseTwo = packageOf(scopeService.registryView(OBJECTS, RELEASE).get("two").orElseThrow());
		EPackage stageFree = packageOf(scopeService.get(OBJECTS, "three").orElseThrow());
		assertSame(releaseOne, releaseTwo, "Two reads of the final stage bind one package");
		assertSame(stageFree, releaseOne,
				"A read naming the final stage binds the package a stage-free read binds");
		assertEquals(0, atlas.stagedFetches(scope, RELEASE),
				"The final stage's packages are the stage-free ones, nothing is fetched for it by name");

		EPackage draftOne = packageOf(scopeService.registryView(OBJECTS, DRAFT).get("four").orElseThrow());
		EPackage draftTwo = packageOf(scopeService.registryView(OBJECTS, DRAFT).get("five").orElseThrow());
		assertSame(draftOne, draftTwo, "Two reads of a stage bind one package, not a fresh copy each");
		assertEquals(1, atlas.stagedFetches(scope, DRAFT), "The draft package is fetched once");
		assertNotSame(stageFree, draftOne, "A non-final stage keeps a package of its own");
	}

	/**
	 * With {@code eager.scopes} / {@code eager.stages} the client generates the registry
	 * chain the framework resolves that scope and stage with; reads of the stage decode
	 * against it and bind the package it holds.
	 */
	@Test
	@FakeAtlasWhiteboard
	public void readsOfAStageWithARegistryChainBindTheChainsPackage(
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PID,
					name = "identity-chain", location = "?")) Configuration configuration,
			@InjectBundleContext BundleContext context) throws Exception {
		String scope = "identitychain";
		FakeAtlas atlas = serveFakeAtlas(context);
		configuration.update(clientProps(scope, DRAFT));
		ReadableScopeService<EObject> scopeService = awaitScopeService(context, scope);
		EPackage.Registry chain = awaitRegistryChain(context, scope + "_" + DRAFT);

		EPackage draftOne = packageOf(scopeService.registryView(OBJECTS, DRAFT).get("one").orElseThrow());
		EPackage draftTwo = packageOf(scopeService.registryView(OBJECTS, DRAFT).get("two").orElseThrow());
		assertSame(draftOne, draftTwo, "Two reads of the stage bind one package");
		assertSame(chain.getEPackage(NS_URI), draftOne,
				"A read of the stage binds the package the framework's registry chain for it holds");
		assertEquals(1, atlas.stagedFetches(scope, DRAFT), "The draft package is fetched once, by the chain");
	}

	private static EPackage packageOf(EObject object) {
		EPackage ePackage = object.eClass().getEPackage();
		assertNotNull(ePackage, "The object's class has a package");
		assertEquals(NS_URI, ePackage.getNsURI());
		return ePackage;
	}

	private static Hashtable<String, Object> clientProps(String scope, String eagerStage) {
		Hashtable<String, Object> props = new Hashtable<>();
		props.put("base.uri", BASE_URI);
		props.put("mode", "LAZY");
		props.put("mode.strict", Boolean.FALSE);
		props.put("scope.allow.list", new String[] { scope });
		props.put("drift.check.interval.ms", 0);
		if (eagerStage != null) {
			props.put("eager.scopes", new String[] { scope });
			props.put("eager.stages", new String[] { eagerStage });
		}
		return props;
	}

	private FakeAtlas serveFakeAtlas(BundleContext context) throws Exception {
		FakeAtlas atlas = new FakeAtlas();
		Hashtable<String, Object> props = new Hashtable<>();
		props.put("osgi.jakartars.resource", Boolean.TRUE);
		registrations.add(context.registerService(FakeAtlas.class, atlas, props));
		awaitFakeAtlas();
		return atlas;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static ReadableScopeService<EObject> awaitScopeService(BundleContext context, String scope)
			throws Exception {
		long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(15);
		while (System.currentTimeMillis() < deadline) {
			var references = context.getServiceReferences(ReadableScopeService.class, "(atlas.scope=" + scope + ")");
			if (!references.isEmpty()) {
				ServiceReference<ReadableScopeService> reference = references.iterator().next();
				return context.getService(reference);
			}
			Thread.sleep(100L);
		}
		return fail("The client published no ReadableScopeService for scope " + scope);
	}

	/** The registry chain emf.osgi generates from the client's ConfigAdmin configuration. */
	private static EPackage.Registry awaitRegistryChain(BundleContext context, String rsfName) throws Exception {
		long deadline = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(15);
		while (System.currentTimeMillis() < deadline) {
			var references = context.getServiceReferences(EPackage.Registry.class, "(rsf.name=" + rsfName + ")");
			if (!references.isEmpty()) {
				return context.getService(references.iterator().next());
			}
			Thread.sleep(100L);
		}
		return fail("No registry chain " + rsfName + " was generated");
	}

	/** Waits until the whiteboard serves the fake Atlas. */
	private static void awaitFakeAtlas() throws Exception {
		HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(2)).build();
		HttpRequest probe = HttpRequest.newBuilder(URI.create(BASE_URI + "/scopes/probe")).GET().build();
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
	 * The endpoints the client's object reads and package look-ups use. Every scope has a
	 * schema registry staged {@code draft → release} holding one package, and an object
	 * registry whose objects are instances of it.
	 */
	@Path("identity-atlas")
	public static class FakeAtlas {

		private final Map<String, AtomicInteger> stagedFetches = new ConcurrentHashMap<>();

		int stagedFetches(String scope, String stage) {
			AtomicInteger count = stagedFetches.get(scope + "/" + stage);
			return count == null ? 0 : count.get();
		}

		@GET
		@Path("scopes/{scope}")
		@Produces("application/json")
		public String scope(@PathParam("scope") String scope) {
			String stages = "[{\"name\":\"" + DRAFT + "\",\"writable\":true},{\"name\":\"" + RELEASE
					+ "\",\"writable\":true,\"final\":true}]";
			return "{\"name\":\"" + scope + "\",\"registries\":[" //
					+ "{\"name\":\"schema\",\"type\":\"SCHEMA\",\"stages\":" + stages + "}," //
					+ "{\"name\":\"" + OBJECTS + "\",\"type\":\"OTHER\",\"stages\":" + stages + "}]}";
		}

		@GET
		@Path("{scope}/schema/content")
		@Produces("application/xmi")
		public Response stageFreePackage(@QueryParam("nsUri") String nsUri) {
			return packageContent(nsUri);
		}

		@GET
		@Path("{scope}/schema/stages/{stage}/content")
		@Produces("application/xmi")
		public Response stagedPackage(@PathParam("scope") String scope, @PathParam("stage") String stage,
				@QueryParam("nsUri") String nsUri) {
			stagedFetches.computeIfAbsent(scope + "/" + stage, key -> new AtomicInteger()).incrementAndGet();
			return packageContent(nsUri);
		}

		@GET
		@Path("{scope}/registries/{registry}/content")
		@Produces("application/xmi")
		public String stageFreeObject(@QueryParam("objectId") String objectId) {
			return THING.formatted(NS_URI, objectId);
		}

		@GET
		@Path("{scope}/registries/{registry}/stages/{stage}/content")
		@Produces("application/xmi")
		public String stagedObject(@QueryParam("objectId") String objectId) {
			return THING.formatted(NS_URI, objectId);
		}

		private static Response packageContent(String nsUri) {
			return NS_URI.equals(nsUri) ? Response.ok(ECORE).build() : Response.noContent().build();
		}
	}
}
