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
	 * pair. Falls back to the default {@link ResourceSet} CSO when either
	 * parameter is {@code null} (e.g. paths without scope/stage templates).
	 *
	 * @throws WebApplicationException 503 if the collector is currently
	 *         unavailable, 400 if no ResourceSet is registered for the given
	 *         scope/stage.
	 */
	private ComponentServiceObjects<ResourceSet> resolveCso(String scopeName, String stageName) {
		if (scopeName == null || stageName == null) {
			ComponentServiceObjects<ResourceSet> fallback = defaultCsoRef.get();
			if (fallback == null) {
				throw new WebApplicationException(
						Response.status(Response.Status.SERVICE_UNAVAILABLE)
								.entity("Default ResourceSet not available")
								.build());
			}
			return fallback;
		}
		ResourceSetCollector collector = collectorRef.get();
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
		return cso;
	}
}
