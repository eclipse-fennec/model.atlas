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
package org.eclipse.fennec.model.atlas.rest.filter.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.service.jakartars.runtime.JakartarsServiceRuntime;
import org.osgi.service.jakartars.runtime.dto.ApplicationDTO;
import org.osgi.service.jakartars.runtime.dto.ResourceDTO;
import org.osgi.service.jakartars.runtime.dto.RuntimeDTO;
import org.osgi.service.jakartars.whiteboard.annotations.RequireJakartarsWhiteboard;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

/**
 * OSGi integration test for {@code ScopedResourceSetFeature}. Verifies two
 * properties of the HK2-binder-based per-request {@link ResourceSet}
 * injection:
 *
 * <ol>
 *   <li>The {@link ResourceSet} injected via {@code @Context} into a JAX-RS
 *       resource matches the scope/stage-specific instance published by the
 *       {@link TestResourceSetCollector} for that request's path
 *       parameters.</li>
 *   <li>HK2's {@code Factory.dispose()} runs after the response has been
 *       written, so for every request {@code getService()} on the underlying
 *       {@link org.osgi.service.component.ComponentServiceObjects} is matched
 *       by exactly one {@code ungetService()}.</li>
 * </ol>
 */
@RequireEMF
@RequireJakartarsWhiteboard
@RequireConfigurationAdmin
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class ScopedResourceSetIntegrationTest {

	private static final String BASE_URL = "http://localhost:8186/rest";
	private static final String RESOURCE_NAME = "ScopedResourceSetTestResource";
	private static final String SINGLETON_RESOURCE_NAME = "SingletonResourceSetClassResource";

	@InjectService(timeout = 5_000)
	TestResourceSetCollector testCollector;

	@InjectService(timeout = 5_000)
	TestScopeServiceCollector testScopeCollector;

	@InjectService(timeout = 5_000)
	ClientBuilder clientBuilder;

	@InjectService(timeout = 5_000)
	JakartarsServiceRuntime jakartarsRuntime;

	private Client client;
	private CountingCso scopeAStageA;
	private CountingCso scopeBStageB;

	@BeforeEach
	public void setUp() throws InterruptedException {
		client = clientBuilder.build();

		testCollector.clear();
		testScopeCollector.clear();
		testScopeCollector.register("scopeA");
		testScopeCollector.register("scopeB");

		scopeAStageA = new CountingCso(new ResourceSetImpl());

		ResourceSet scopeBResourceSet = new ResourceSetImpl();
		EPackage marker = EcoreFactory.eINSTANCE.createEPackage();
		marker.setName("marker");
		marker.setNsURI("urn:test:marker");
		marker.setNsPrefix("marker");
		scopeBResourceSet.getPackageRegistry().put(marker.getNsURI(), marker);
		scopeBStageB = new CountingCso(scopeBResourceSet);

		testCollector.register("scopeA", "stageA", scopeAStageA);
		testCollector.register("scopeB", "stageB", scopeBStageB);

		waitForTestResource(15, TimeUnit.SECONDS);
	}

	@AfterEach
	public void tearDown() {
		if (client != null) {
			client.close();
			client = null;
		}
		testCollector.clear();
		testScopeCollector.clear();
	}

	@Test
	public void injectsScopeAndStageSpecificResourceSet() {
		String identityA = identityFor("scopeA", "stageA");
		String identityB = identityFor("scopeB", "stageB");

		assertNotEquals(identityA, identityB,
				"Different scope/stage requests must yield different ResourceSet instances");

		// CountingCso always hands out the same backing ResourceSet, so
		// identity must be stable across repeated requests to the same key.
		assertEquals(identityA, identityFor("scopeA", "stageA"),
				"Same scope/stage must yield the same ResourceSet across requests");
	}

	@Test
	public void injectedResourceSetSeesScopeSpecificPackages() {
		assertEquals("0", packageCountFor("scopeA", "stageA"),
				"scopeA/stageA ResourceSet starts empty");
		assertEquals("1", packageCountFor("scopeB", "stageB"),
				"scopeB/stageB ResourceSet contains the marker EPackage we pre-loaded");
	}

	@Test
	public void disposeUngetServiceForEveryRequest() throws InterruptedException {
		int requests = 5;
		for (int i = 0; i < requests; i++) {
			identityFor("scopeA", "stageA");
		}

		// dispose() runs after the response has been written; give it a
		// moment to settle before asserting matched get/unget counts.
		assertTrue(awaitBalanced(scopeAStageA, requests, 2_000),
				String.format("Expected %d unget calls to match %d get calls (currently get=%d, unget=%d)",
						requests, requests, scopeAStageA.getServiceCalls(), scopeAStageA.ungetServiceCalls()));
		assertEquals(0, scopeBStageB.getServiceCalls(),
				"Untouched scope must not have triggered any getService()");
		assertEquals(0, scopeBStageB.ungetServiceCalls(),
				"Untouched scope must not have triggered any ungetService()");
	}

	@Test
	public void parallelRequestsGetExclusiveResourceSetsAndAreCleanedUp() throws Exception {
		PrototypeCountingCso prototype = new PrototypeCountingCso();
		testCollector.register("scopeA", "stageA", prototype);

		int parallel = 5;
		ExecutorService executor = Executors.newFixedThreadPool(parallel);
		try {
			List<Callable<String>> tasks = new ArrayList<>();
			for (int i = 0; i < parallel; i++) {
				tasks.add(() -> restTarget()
						.path("binder-test/scopeA/stages/stageA/identity-slow")
						.request()
						.get(String.class));
			}

			List<Future<String>> futures = executor.invokeAll(tasks, 30, TimeUnit.SECONDS);

			Set<String> identitiesBefore = new HashSet<>();
			Set<String> identitiesAfter = new HashSet<>();
			for (Future<String> future : futures) {
				String pair = future.get();
				String[] parts = pair.split(":");
				assertEquals(2, parts.length, "Expected before:after identity pair, got " + pair);
				assertEquals(parts[0], parts[1],
						"Same request must see the same ResourceSet instance before and after the sleep");
				identitiesBefore.add(parts[0]);
				identitiesAfter.add(parts[1]);
			}

			assertEquals(parallel, identitiesBefore.size(),
					"Each of the " + parallel + " parallel requests must see a distinct ResourceSet instance");
			assertEquals(parallel, identitiesAfter.size(),
					"Identities after the sleep must also be distinct across requests");
		} finally {
			executor.shutdown();
			assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS),
					"Executor did not shut down cleanly");
		}

		assertTrue(awaitOutstandingCleared(prototype, 2_000),
				String.format("Expected all checked-out ResourceSets to be ungotten (get=%d, unget=%d, outstanding=%d)",
						prototype.getServiceCalls(), prototype.ungetServiceCalls(), prototype.outstandingCount()));
		assertEquals(parallel, prototype.getServiceCalls(),
				"Exactly one getService per request");
		assertEquals(parallel, prototype.ungetServiceCalls(),
				"Exactly one ungetService per request");
	}

	@Test
	public void bothPrototypeAndSingletonResourcesReceiveProxyButResolveCorrectly() {
		// Empirically, both an OSGi-PROTOTYPE resource and an OSGi-SINGLETON
		// resource end up with a Jersey proxy for @Context ResourceSet. The
		// PROTOTYPE resource is HK2 PerLookup, which is not the same scope
		// as the binding's RequestScoped, so `.proxyForSameScope(false)` does
		// not skip proxying for it. That is the safe default: the proxy
		// resolves through the per-thread RequestScope on every method call,
		// which is what makes singleton MessageBodyReaders/Writers correct
		// under concurrent requests in the first place.
		String prototypeClass = restTarget()
				.path("binder-test/scopeA/stages/stageA/class-name")
				.request().get(String.class);
		String singletonClass = restTarget()
				.path("binder-test/scopeA/stages/stageA/singleton-class-name")
				.request().get(String.class);

		assertTrue(looksLikeProxy(prototypeClass),
				"Prototype resource is expected to receive a proxy of ResourceSet, got " + prototypeClass);
		assertTrue(looksLikeProxy(singletonClass),
				"Singleton resource is expected to receive a proxy of ResourceSet, got " + singletonClass);

		// The proxy must still resolve to the right per-request ResourceSet:
		// scopeA and scopeB are backed by distinct CountingCso instances, so
		// the package-count endpoint (which dereferences the proxy) must see
		// the marker EPackage only on scopeB.
		assertEquals("0", packageCountFor("scopeA", "stageA"),
				"Proxy on the prototype resource must resolve to scopeA's empty ResourceSet");
		assertEquals("1", packageCountFor("scopeB", "stageB"),
				"Proxy on the prototype resource must resolve to scopeB's marker-loaded ResourceSet");
	}

	private static boolean looksLikeProxy(String className) {
		return className.contains("$$")
				|| className.contains("Proxy")
				|| className.contains("ByteBuddy")
				|| className.contains("$Proxy");
	}

	@Test
	public void unknownStageReturnsBadRequest() {
		// scopeA exists for ModelAtlasRequestFilter, but no ResourceSet is
		// registered for the (scopeA, missing) pair, so the binder must
		// reject the request with 400.
		Response response = restTarget()
				.path("binder-test/scopeA/stages/missing/identity")
				.request()
				.get();
		try {
			int status = response.getStatus();
			String body = response.readEntity(String.class);
			assertEquals(Response.Status.BAD_REQUEST.getStatusCode(), status,
					"Unknown stage for a known scope must return 400. Actual status=" + status
							+ ", body=" + body);
			assertTrue(body.contains("Resource Set for Stage"),
					"Body must come from the binder, not the upstream filter: " + body);
		} finally {
			response.close();
		}
	}

	// ------------------------------------------------------------------
	// helpers
	// ------------------------------------------------------------------

	private String identityFor(String scope, String stage) {
		Response response = restTarget()
				.path("binder-test").path(scope).path("stages").path(stage).path("identity")
				.request()
				.get();
		try {
			if (response.getStatus() != 200) {
				throw new AssertionError("identity GET " + scope + "/" + stage + " returned "
						+ response.getStatus() + " body=" + response.readEntity(String.class));
			}
			return response.readEntity(String.class);
		} finally {
			response.close();
		}
	}

	private String packageCountFor(String scope, String stage) {
		return restTarget()
				.path("binder-test").path(scope).path("stages").path(stage).path("package-count")
				.request()
				.get(String.class);
	}

	private WebTarget restTarget() {
		return client.target(BASE_URL);
	}

	private boolean awaitOutstandingCleared(PrototypeCountingCso cso, long timeoutMillis)
			throws InterruptedException {
		long deadline = System.currentTimeMillis() + timeoutMillis;
		while (System.currentTimeMillis() < deadline) {
			if (cso.outstandingCount() == 0
					&& cso.getServiceCalls() == cso.ungetServiceCalls()) {
				return true;
			}
			Thread.sleep(25);
		}
		return cso.outstandingCount() == 0
				&& cso.getServiceCalls() == cso.ungetServiceCalls();
	}

	private boolean awaitBalanced(CountingCso cso, int expectedGets, long timeoutMillis)
			throws InterruptedException {
		long deadline = System.currentTimeMillis() + timeoutMillis;
		while (System.currentTimeMillis() < deadline) {
			if (cso.getServiceCalls() == expectedGets
					&& cso.ungetServiceCalls() == expectedGets) {
				return true;
			}
			Thread.sleep(25);
		}
		return cso.getServiceCalls() == expectedGets
				&& cso.ungetServiceCalls() == expectedGets;
	}

	private void waitForTestResource(long timeout, TimeUnit unit) throws InterruptedException {
		long deadline = System.nanoTime() + unit.toNanos(timeout);
		while (System.nanoTime() < deadline) {
			if (isTestResourceRegistered()) {
				// Brief settle: give the binder feature time to bind to the
				// high-ranked TestResourceSetCollector.
				Thread.sleep(500);
				return;
			}
			Thread.sleep(100);
		}
		assertTrue(isTestResourceRegistered(),
				RESOURCE_NAME + " was not registered in the Jakarta REST runtime within "
						+ timeout + " " + unit);
	}

	private boolean isTestResourceRegistered() {
		RuntimeDTO dto = jakartarsRuntime.getRuntimeDTO();
		boolean prototypeReady = false;
		boolean singletonReady = false;
		if (dto.defaultApplication != null) {
			prototypeReady |= containsResource(dto.defaultApplication.resourceDTOs, RESOURCE_NAME);
			singletonReady |= containsResource(dto.defaultApplication.resourceDTOs, SINGLETON_RESOURCE_NAME);
		}
		if (dto.applicationDTOs != null) {
			for (ApplicationDTO app : dto.applicationDTOs) {
				prototypeReady |= containsResource(app.resourceDTOs, RESOURCE_NAME);
				singletonReady |= containsResource(app.resourceDTOs, SINGLETON_RESOURCE_NAME);
			}
		}
		return prototypeReady && singletonReady;
	}

	private boolean containsResource(ResourceDTO[] dtos, String name) {
		if (dtos == null) {
			return false;
		}
		for (ResourceDTO dto : dtos) {
			if (name.equals(dto.name)) {
				return true;
			}
		}
		return false;
	}
}
