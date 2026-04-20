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
package org.eclipse.fennec.model.atlas.rest.application.filter;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.model.atlas.rest.common.ModelAtlasRestConstants;
import org.eclipse.fennec.model.atlas.workflow.ResourceSetCollector;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.Response;

/**
 * JAX-RS request filter that validates scope/stage assignment and resolves
 * resource set before requests reach the resource methods.
 *
 *
 * <h3>OSGi service binding strategy</h3>
 * <p>This filter is registered as a singleton JAX-RS extension via the OSGi
 * Jakarta RS Whiteboard. Unlike PROTOTYPE-scoped resource components (which get
 * fresh instances with up-to-date references per request), this singleton must
 * handle service rebinding at runtime. The references use:
 * <ul>
 *   <li>{@code policy = DYNAMIC} — allows rebinding without component
 *       deactivation/reactivation, which is required because the JAX-RS
 *       whiteboard holds on to the singleton filter instance.</li>
 *   <li>{@code policyOption = GREEDY} — ensures a higher-ranked service
 *       (e.g. a test mock with {@code service.ranking = MAX_VALUE}) replaces
 *       the current binding immediately, rather than sticking with the
 *       initially bound service.</li>
 * </ul>
 * <p>References are stored in {@link AtomicReference} fields to guarantee
 * thread-safe access. The {@code unbind} callbacks use
 * {@link AtomicReference#compareAndSet} to avoid nulling out a reference that
 * was already replaced by a higher-ranked service during a GREEDY rebind
 * (where {@code bind(new)} is called before {@code unbind(old)}).
 *
 * @author Data In Motion
 * @since 1.0
 */
@Component
@JakartarsExtension
@JakartarsName("ResourceSetRequestFilter")
public class ResourceSetRequestFilter implements ContainerRequestFilter {
	
	@Reference
	ComponentServiceObjects<ResourceSet> defaultResSetFactory;

	private final AtomicReference<ResourceSetCollector> resourceSetCollectorRef = new AtomicReference<>();

	@Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	void bindResourceSetCollector(ResourceSetCollector collector) {
		resourceSetCollectorRef.set(collector);
	}

	void unbindResourceSetCollector(ResourceSetCollector collector) {
		resourceSetCollectorRef.compareAndSet(collector, null);
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		resolveResourceSet(requestContext);
	}

	/**
	 * Resolves the scope/stage-specific {@link ResourceSet} via the
	 * {@link ResourceSetCollector} and stores its {@link ComponentServiceObjects}
	 * as a request property. Fails the request with {@code 400 Bad Request} when
	 * the path targets a stage for which no ResourceSet is currently registered,
	 * to avoid exposing content through a ResourceSet the client is not entitled
	 * to.
	 */
	private void resolveResourceSet(ContainerRequestContext requestContext) {
		MultivaluedMap<String, String> pathParams = requestContext.getUriInfo().getPathParameters();
		String scopeName = pathParams.getFirst("scopeName");
		String stageName = pathParams.getFirst("stageName");

		if (scopeName == null || stageName == null || isScopesResourcePath(requestContext)) {
			requestContext.setProperty(ModelAtlasRestConstants.RESOLVED_RESOURCE_SET_CSO, defaultResSetFactory);
			return;
		}
		ResourceSetCollector collector = resourceSetCollectorRef.get();
		if (collector == null) {
			throw new WebApplicationException(
					Response.status(Response.Status.SERVICE_UNAVAILABLE)
							.entity("ResourceSetCollector not available")
							.build());
		}
		ComponentServiceObjects<ResourceSet> cso = collector.getResourceSetObjects(scopeName, stageName);
		if (cso == null) {
			throw new WebApplicationException(
					Response.status(Response.Status.BAD_REQUEST)
							.entity(String.format("Resource Set for Stage [%s] and Scope [%s] not found.",
									stageName, scopeName))
							.build());
		}
		requestContext.setProperty(ModelAtlasRestConstants.RESOLVED_RESOURCE_SET_CSO, cso);
	}

	/**
	 * Checks if the request targets the {@code /scopes/} resource, which handles
	 * its own scope validation.
	 */
	private boolean isScopesResourcePath(ContainerRequestContext requestContext) {
		List<PathSegment> segments = requestContext.getUriInfo().getPathSegments();
		return !segments.isEmpty() && "scopes".equals(segments.get(0).getPath());
	}

	

}
