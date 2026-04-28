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
package org.eclipse.fennec.data.atlas.jpa.rest;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.fennec.model.atlas.mediatypes.api.SupportedMediatype;
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
 * property ({@link #RESOLVED_MEDIA_TYPE}) for resources to use in responses.
 * Only applies to requests with a {@code scopeName} path parameter.
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

	/**
	 * Request property key for the resolved media type.
	 */
	public static final String RESOLVED_MEDIA_TYPE = "resolvedMediaType";


	private final AtomicReference<SupportedMediatype> supportedMediatypeRef = new AtomicReference<>();

	

	@Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	void bindSupportedMediatype(SupportedMediatype mediatype) {
		supportedMediatypeRef.set(mediatype);
	}

	void unbindSupportedMediatype(SupportedMediatype mediatype) {
		supportedMediatypeRef.compareAndSet(mediatype, null);
	}

	@Override
	public void filter(ContainerRequestContext requestContext) throws IOException {
		

		resolveMediaType(requestContext);
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
				requestContext.setProperty(RESOLVED_MEDIA_TYPE, mediaTypeParam);
				return;
			}
			throw new WebApplicationException(Response.Status.UNSUPPORTED_MEDIA_TYPE);
		}

		List<MediaType> acceptableMediaTypes = requestContext.getAcceptableMediaTypes();
		for (MediaType acceptedMediaType : acceptableMediaTypes) {
			if (acceptedMediaType.isWildcardType() || acceptedMediaType.isWildcardSubtype()) {
				requestContext.setProperty(RESOLVED_MEDIA_TYPE, MediaType.APPLICATION_JSON);
				return;
			}
			String accept = acceptedMediaType.getType() + "/" + acceptedMediaType.getSubtype();
			if (supported.contains(accept)) {
				requestContext.setProperty(RESOLVED_MEDIA_TYPE, accept);
				return;
			}
		}

		requestContext.setProperty(RESOLVED_MEDIA_TYPE, MediaType.APPLICATION_JSON);
	}
}
