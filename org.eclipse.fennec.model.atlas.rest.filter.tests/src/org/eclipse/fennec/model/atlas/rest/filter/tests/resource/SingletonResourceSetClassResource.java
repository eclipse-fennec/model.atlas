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
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;

/**
 * Singleton counterpart to {@code ScopedResourceSetTestResource} used to
 * verify that {@code @Context ResourceSet} is injected as a Jersey proxy
 * (not the real instance) when the consumer outlives the request scope.
 *
 * <p>Because the OSGi component scope is {@link ServiceScope#SINGLETON},
 * the JAX-RS Whiteboard registers a single instance with Jersey; the
 * {@code @Context} field is therefore set once at startup, before any
 * request scope exists, and must be a proxy that resolves per-request.
 */
@Component(service = SingletonResourceSetClassResource.class, scope = ServiceScope.SINGLETON)
@JakartarsResource
@JakartarsName("SingletonResourceSetClassResource")
@Path("/binder-test")
public class SingletonResourceSetClassResource {

	@Context
	private ResourceSet resourceSet;

	@GET
	@Path("/{scopeName}/stages/{stageName}/singleton-class-name")
	@Produces(MediaType.TEXT_PLAIN)
	public String className(@PathParam("scopeName") String scope, @PathParam("stageName") String stage) {
		return resourceSet.getClass().getName();
	}
}
