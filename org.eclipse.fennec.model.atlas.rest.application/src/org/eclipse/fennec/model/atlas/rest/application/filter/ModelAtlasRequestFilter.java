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

import org.eclipse.fennec.model.atlas.mediatypes.api.SupportedMediatype;
import org.eclipse.fennec.model.atlas.rest.common.ModelAtlasRestConstants;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.eclipse.fennec.model.atlas.workflow.ScopeServiceCollector;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.Response;

/**
 * JAX-RS request filter that validates scope/registry assignment and resolves
 * media types before requests reach the resource methods.
 *
 * <p>Scope/registry validation: ensures that the requested registry is actually
 * configured for the given scope, preventing access to unconfigured registries.
 *
 * <p>MediaType resolution: centralizes the duplicated {@code checkContentType()}
 * logic from scope-based resources. The resolved media type is set as a request
 * property ({@link ModelAtlasRestConstants#RESOLVED_MEDIA_TYPE}) for resources
 * to use in responses. Only applies to requests with a {@code scopeName} path
 * parameter.
 *
 * <p>Paths starting with {@code /scopes/} are excluded from scope/registry
 * validation, as the {@code ScopesResource} handles scope lookup itself.
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
@JakartarsName("ModelAtlasRequestFilter")
public class ModelAtlasRequestFilter implements ContainerRequestFilter {

	private static final String SCHEMA_REGISTRY_NAME = "schema";

	private final AtomicReference<ScopeServiceCollector> scopeCollectorRef = new AtomicReference<>();
	private final AtomicReference<SupportedMediatype> supportedMediatypeRef = new AtomicReference<>();

	@Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	void bindScopeCollector(ScopeServiceCollector collector) {
		scopeCollectorRef.set(collector);
	}

	void unbindScopeCollector(ScopeServiceCollector collector) {
		scopeCollectorRef.compareAndSet(collector, null);
	}

	@Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	void bindSupportedMediatype(SupportedMediatype mediatype) {
		supportedMediatypeRef.set(mediatype);
	}

	void unbindSupportedMediatype(SupportedMediatype mediatype) {
		supportedMediatypeRef.compareAndSet(mediatype, null);
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		System.out.print("Entering filter");
		MultivaluedMap<String, String> pathParams = requestContext.getUriInfo().getPathParameters();
		String scopeName = pathParams.getFirst("scopeName");

		if (scopeName == null || isScopesResourcePath(requestContext)) {
			return;
		}

		ScopeServiceCollector collector = scopeCollectorRef.get();
		if (collector == null) {
			throw new WebApplicationException(
					Response.status(Response.Status.SERVICE_UNAVAILABLE)
							.entity("ScopeServiceCollector not available")
							.build());
		}

		ScopeService<?> scopeService = collector.getScopeServiceByScopeName(scopeName);
		if (scopeService == null) {
			throw new WebApplicationException(
					Response.status(Response.Status.BAD_REQUEST)
							.entity(String.format("Scope [%s] not found.", scopeName))
							.build());
		}

		String registryName = resolveRegistryName(requestContext, pathParams);
		if (registryName != null && !scopeService.isValidRegistry(registryName)) {
			throw new WebApplicationException(
					Response.status(Response.Status.BAD_REQUEST)
							.entity(String.format("Registry [%s] is not available for scope [%s].",
									registryName, scopeName))
							.build());
		}

		resolveMediaType(requestContext);
	}

	/**
	 * Checks if the request targets the {@code /scopes/} resource, which handles
	 * its own scope validation.
	 */
	private boolean isScopesResourcePath(ContainerRequestContext requestContext) {
		List<PathSegment> segments = requestContext.getUriInfo().getPathSegments();
		return !segments.isEmpty() && "scopes".equals(segments.get(0).getPath());
	}

	/**
	 * Determines the registry name from path parameters or path segments.
	 * For {@code /{scopeName}/registries/{registryName}} paths, it comes from
	 * the path parameter. For {@code /{scopeName}/schema} paths, it is the
	 * hardcoded "schema" registry.
	 */
	private String resolveRegistryName(ContainerRequestContext requestContext,
			MultivaluedMap<String, String> pathParams) {
		String registryName = pathParams.getFirst("registryName");
		if (registryName != null) {
			return registryName;
		}
		String path = requestContext.getUriInfo().getPath();
		if (path.contains("/schema")) {
			return SCHEMA_REGISTRY_NAME;
		}
		return null;
	}

	/**
	 * Resolves the media type from the {@code mediaType} query parameter or
	 * the Accept header. Sets the result as a request property for downstream
	 * resources.
	 */
	private void resolveMediaType(ContainerRequestContext requestContext) {
		SupportedMediatype mediatype = supportedMediatypeRef.get();
		if (mediatype == null) {
			throw new WebApplicationException(
					Response.status(Response.Status.SERVICE_UNAVAILABLE)
							.entity("SupportedMediatype service not available")
							.build());
		}

		List<String> supported = mediatype.getSupportedMediaTypes();
		String mediaTypeParam = requestContext.getUriInfo().getQueryParameters().getFirst("mediaType");

		if (mediaTypeParam != null) {
			if (supported.contains(mediaTypeParam)) {
				requestContext.setProperty(ModelAtlasRestConstants.RESOLVED_MEDIA_TYPE, mediaTypeParam);
				return;
			}
			throw new WebApplicationException(Response.Status.UNSUPPORTED_MEDIA_TYPE);
		}

		List<MediaType> acceptableMediaTypes = requestContext.getAcceptableMediaTypes();
		if (acceptableMediaTypes.isEmpty()) {
			requestContext.setProperty(ModelAtlasRestConstants.RESOLVED_MEDIA_TYPE, MediaType.APPLICATION_JSON);
			return;
		}
		for (MediaType acceptedMediaType : acceptableMediaTypes) {
			if (acceptedMediaType.isWildcardType() || acceptedMediaType.isWildcardSubtype()) {
				requestContext.setProperty(ModelAtlasRestConstants.RESOLVED_MEDIA_TYPE, MediaType.APPLICATION_JSON);
				return;
			}
			String accept = acceptedMediaType.getType() + "/" + acceptedMediaType.getSubtype();
			if (supported.contains(accept)) {
				requestContext.setProperty(ModelAtlasRestConstants.RESOLVED_MEDIA_TYPE, accept);
				return;
			}
		}

		throw new WebApplicationException(Response.Status.UNSUPPORTED_MEDIA_TYPE);
	}
}
