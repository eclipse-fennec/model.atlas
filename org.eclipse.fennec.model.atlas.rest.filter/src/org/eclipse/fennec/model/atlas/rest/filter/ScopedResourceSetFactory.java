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
package org.eclipse.fennec.model.atlas.rest.filter;

import java.util.function.Supplier;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.osgi.service.component.ComponentServiceObjects;

import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;

/**
 * Jersey {@link Supplier} that resolves a per-request {@link ResourceSet}
 * based on the {@code scopeName} and {@code stageName} path parameters of the
 * incoming request.
 *
 * <p>Registered by {@link ScopedResourceSetFeature} as
 * {@code bindFactory(...).in(RequestScoped.class)} so that {@link #get()}
 * runs once per request. The resolved {@link ComponentServiceObjects} and
 * {@link ResourceSet} instance are stashed on the
 * {@link ContainerRequestContext} so that
 * {@link ScopedResourceSetCleanupFilter} can release the prototype-scoped
 * OSGi service after the response has been written. (Jersey's
 * {@code DisposableSupplier.dispose()} is unreliable for proxied bindings,
 * so the cleanup is implemented as a {@code ContainerResponseFilter}
 * instead.)
 *
 * @author Data In Motion
 * @since 1.0
 */
public class ScopedResourceSetFactory implements Supplier<ResourceSet> {

	static final String ACTIVE_CSO_PROPERTY = "scopedResourceSet.activeCSO";
	static final String ACTIVE_RESOURCE_SET_PROPERTY = "scopedResourceSet.activeInstance";

	@Inject
	ScopedResourceSetFeature feature;

	@Context
	Provider<UriInfo> uriInfoProvider;

	@Context
	Provider<ContainerRequestContext> requestContextProvider;

	@Override
	public ResourceSet get() {
		UriInfo uriInfo = uriInfoProvider.get();
		MultivaluedMap<String, String> pathParams = uriInfo.getPathParameters();
		String scopeName = pathParams.getFirst("scopeName");
		String stageName = pathParams.getFirst("stageName");

		ComponentServiceObjects<ResourceSet> cso = feature.resolveCso(scopeName, stageName);
		ResourceSet rs = cso.getService();
		ContainerRequestContext ctx = requestContextProvider.get();
		ctx.setProperty(ACTIVE_CSO_PROPERTY, cso);
		ctx.setProperty(ACTIVE_RESOURCE_SET_PROPERTY, rs);
		return rs;
	}
}
