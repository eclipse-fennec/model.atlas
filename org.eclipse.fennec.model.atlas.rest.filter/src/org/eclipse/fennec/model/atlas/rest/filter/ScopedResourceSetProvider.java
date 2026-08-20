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

import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.codec.rest.jakartas.spi.ResourceSetProvider;
import org.eclipse.fennec.model.atlas.workflow.ResourceSetCollector;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.component.propertytypes.ServiceRanking;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

/**
 * Scope/stage-aware {@link ResourceSetProvider} that resolves the per-request
 * {@link ResourceSet} from the {@code scopeName} and {@code stageName} path
 * parameters of the incoming request.
 *
 * <p>This is the Model Atlas override of the codec's default
 * {@code DefaultResourceSetProvider}. The codec's
 * {@code CodecResourceSetFeature} binds the highest-ranked
 * {@link ResourceSetProvider} via a {@code DYNAMIC}/{@code GREEDY} reference;
 * the {@link ServiceRanking} below ensures this scoped provider wins over the
 * codec default whenever the Model Atlas bundle is present.
 *
 * <p>The resolved {@link ResourceSet} is leased from the matching
 * prototype-scoped {@link ComponentServiceObjects} obtained from the
 * {@link ResourceSetCollector}. The lease is stashed on the
 * {@link ContainerRequestContext} so {@link #releaseResourceSet} can
 * {@code ungetService} it once the codec's cleanup filter fires after the
 * response has been written.
 *
 * <p>When no {@link ResourceSet} is registered for the request's scope/stage
 * pair, the default {@link ResourceSet} is handed out instead. See
 * {@link #resolveCso(String, String)} for why this must not fail the request
 * here.
 *
 * <h3>OSGi service binding strategy</h3>
 * <p>References use {@code policy = DYNAMIC} / {@code policyOption = GREEDY}
 * and are held in {@link AtomicReference} fields so a higher-ranked
 * {@link ResourceSetCollector} (e.g. a test mock) replaces the current binding
 * immediately and thread-safely. The {@code unbind} callbacks use
 * {@link AtomicReference#compareAndSet} to avoid nulling out a reference that
 * a GREEDY rebind already replaced.
 *
 * @author Data In Motion
 * @since 1.0
 */
@Component(service = ResourceSetProvider.class)
@ServiceRanking(100)
public class ScopedResourceSetProvider implements ResourceSetProvider {

	/**
	 * Request-context property under which the leased
	 * {@link ComponentServiceObjects} is stashed during
	 * {@link #getResourceSet} so {@link #releaseResourceSet} can return the
	 * {@link ResourceSet} to it.
	 */
	static final String ACTIVE_CSO_PROPERTY = "scopedResourceSet.activeCSO";

	private static final Logger LOGGER = Logger.getLogger(ScopedResourceSetProvider.class.getName());

	private final AtomicReference<ResourceSetCollector> collectorRef = new AtomicReference<>();
	private final AtomicReference<ComponentServiceObjects<ResourceSet>> defaultCsoRef = new AtomicReference<>();

	@Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	void bindResourceSetCollector(ResourceSetCollector collector) {
		collectorRef.set(collector);
	}

	void unbindResourceSetCollector(ResourceSetCollector collector) {
		collectorRef.compareAndSet(collector, null);
	}

	@Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
	void bindDefaultResourceSet(ComponentServiceObjects<ResourceSet> cso) {
		defaultCsoRef.set(cso);
	}

	void unbindDefaultResourceSet(ComponentServiceObjects<ResourceSet> cso) {
		defaultCsoRef.compareAndSet(cso, null);
	}

	@Override
	public ResourceSet getResourceSet(ContainerRequestContext requestContext) {
		UriInfo uriInfo = requestContext.getUriInfo();
		MultivaluedMap<String, String> pathParams = uriInfo.getPathParameters();
		String scopeName = pathParams.getFirst("scopeName");
		String stageName = pathParams.getFirst("stageName");

		ComponentServiceObjects<ResourceSet> cso = resolveCso(scopeName, stageName);
		ResourceSet rs = cso.getService();
		requestContext.setProperty(ACTIVE_CSO_PROPERTY, cso);
		return rs;
	}

	@Override
	public void releaseResourceSet(ResourceSet resourceSet, ContainerRequestContext requestContext) {
		Object property = requestContext.getProperty(ACTIVE_CSO_PROPERTY);
		try {
			if (property instanceof ComponentServiceObjects<?> cso) {
				@SuppressWarnings("unchecked")
				ComponentServiceObjects<ResourceSet> typed = (ComponentServiceObjects<ResourceSet>) cso;
				typed.ungetService(resourceSet);
			}
		} finally {
			requestContext.removeProperty(ACTIVE_CSO_PROPERTY);
		}
	}

	/**
	 * Resolves the {@link ComponentServiceObjects} for the given scope/stage
	 * pair, falling back to the default {@link ResourceSet} CSO when either
	 * parameter is {@code null} (e.g. paths without scope/stage templates) or
	 * when nothing is registered for the pair.
	 *
	 * <p>An unresolvable scope/stage must not raise a
	 * {@link WebApplicationException} here: this provider is reached lazily
	 * through the codec's {@code CodecResourceSetSupplier}, and its first
	 * caller is {@code MessageBodyWriter.isWriteable(...)}. HK2 swallows an
	 * exception thrown at that point into a {@code MultiException}, Jersey
	 * reads {@code isWriteable} as "no writer available" and answers a bodyless
	 * {@code 500} — and writing the mapped error response fails the same way,
	 * so the real cause never reaches the client. Rejecting unknown scope/stage
	 * combinations belongs to the layers that can still produce a response:
	 * {@code ModelAtlasRequestFilter} validates the scope and registry, and the
	 * registry services validate the stage name and answer {@code 400}.
	 *
	 * @throws WebApplicationException 503 if neither a matching nor a default
	 *         {@link ResourceSet} is available.
	 */
	private ComponentServiceObjects<ResourceSet> resolveCso(String scopeName, String stageName) {
		if (scopeName == null || stageName == null) {
			return defaultCso("no scope/stage path parameters on this request");
		}
		ResourceSetCollector collector = collectorRef.get();
		if (collector == null) {
			return defaultCso("ResourceSetCollector not available");
		}
		ComponentServiceObjects<ResourceSet> cso = collector.getResourceSetObjects(scopeName, stageName);
		if (cso == null) {
			return defaultCso(String.format("no ResourceSet registered for scope [%s] / stage [%s]",
					scopeName, stageName));
		}
		return cso;
	}

	/**
	 * Returns the default {@link ResourceSet} CSO, logging why the scoped one
	 * could not be used.
	 *
	 * @throws WebApplicationException 503 if there is no default either — with
	 *         no {@link ResourceSet} at all nothing can be serialized.
	 */
	private ComponentServiceObjects<ResourceSet> defaultCso(String reason) {
		ComponentServiceObjects<ResourceSet> fallback = defaultCsoRef.get();
		if (fallback == null) {
			LOGGER.severe(() -> "No ResourceSet available: " + reason
					+ ", and no default ResourceSet is registered either");
			throw new WebApplicationException(
					Response.status(Response.Status.SERVICE_UNAVAILABLE)
							.entity("Default ResourceSet not available")
							.build());
		}
		LOGGER.fine(() -> "Falling back to the default ResourceSet: " + reason);
		return fallback;
	}
}
