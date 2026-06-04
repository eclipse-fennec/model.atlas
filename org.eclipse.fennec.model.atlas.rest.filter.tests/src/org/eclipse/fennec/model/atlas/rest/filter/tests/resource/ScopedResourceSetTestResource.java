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
package org.eclipse.fennec.model.atlas.rest.filter.tests.resource;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

/**
 * Test-only JAX-RS resource that exercises the HK2-binder-based per-request
 * {@link ResourceSet} injection from {@code ScopedResourceSetFeature}.
 *
 * <p>The injected {@link ResourceSet} is resolved by HK2 via the scope/stage
 * path parameters. The resource returns the identity hash of the underlying
 * ResourceSet so tests can verify the right instance was supplied for each
 * (scope, stage) combination.
 */
@Component(service = ScopedResourceSetTestResource.class, scope = org.osgi.service.component.annotations.ServiceScope.PROTOTYPE)
@JakartarsResource
@JakartarsName("ScopedResourceSetTestResource")
@Path("/binder-test")
public class ScopedResourceSetTestResource {

	@Context
	private ResourceSet resourceSet;

	@GET
	@Path("/{scopeName}/stages/{stageName}/identity")
	@Produces(MediaType.TEXT_PLAIN)
	public String identity(@PathParam("scopeName") String scope, @PathParam("stageName") String stage) {
		return Integer.toHexString(System.identityHashCode(unwrap(resourceSet)));
	}

	@GET
	@Path("/{scopeName}/stages/{stageName}/package-count")
	@Produces(MediaType.TEXT_PLAIN)
	public String packageCount(@PathParam("scopeName") String scope, @PathParam("stageName") String stage) {
		return Integer.toString(resourceSet.getPackageRegistry().size());
	}

	/**
	 * Returns the runtime class name of the injected ResourceSet. Used to
	 * verify that prototype-scoped resources receive the real instance (not a
	 * proxy) thanks to {@code proxyForSameScope(false)} on the binder.
	 */
	@GET
	@Path("/{scopeName}/stages/{stageName}/class-name")
	@Produces(MediaType.TEXT_PLAIN)
	public String className(@PathParam("scopeName") String scope, @PathParam("stageName") String stage) {
		return resourceSet.getClass().getName();
	}

	/**
	 * Captures the identity of the injected {@link ResourceSet} before and
	 * after a deliberate sleep, so parallel-request tests can force overlap
	 * and verify that each request gets its own exclusive instance.
	 */
	@GET
	@Path("/{scopeName}/stages/{stageName}/identity-slow")
	@Produces(MediaType.TEXT_PLAIN)
	public String identitySlow(@PathParam("scopeName") String scope, @PathParam("stageName") String stage)
			throws InterruptedException {
		String before = Integer.toHexString(System.identityHashCode(unwrap(resourceSet)));
		Thread.sleep(200);
		String after = Integer.toHexString(System.identityHashCode(unwrap(resourceSet)));
		return before + ":" + after;
	}

	/**
	 * Best-effort proxy unwrapping. HK2 returns a proxy when
	 * {@code .proxy(true)} is configured; identity comparisons must look past
	 * the proxy. For ResourceSetImpl + Java proxies the same delegate is
	 * returned by sequential calls, so this also helps when the test wants
	 * stable identity within a single request.
	 */
	private static Object unwrap(ResourceSet rs) {
		// Force the proxy to resolve, then return any stable identity-bearing
		// state. Using the registry instance because ResourceSetImpl's
		// getPackageRegistry returns the same EPackage.Registry that the
		// CSO-managed prototype holds.
		return rs.getPackageRegistry();
	}
}
