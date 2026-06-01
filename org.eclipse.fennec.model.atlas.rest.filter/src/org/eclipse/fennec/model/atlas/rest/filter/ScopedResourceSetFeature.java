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
import org.eclipse.fennec.model.atlas.workflow.ResourceSetCollector;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.process.internal.RequestScoped;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.core.Response;

/**
 * JAX-RS {@link Feature} that wires a request-scoped Jersey binding for
 * {@link ResourceSet}, resolving the correct scope/stage-specific instance per
 * incoming request and disposing it cleanly when the request scope ends.
 *
 * <p>Resources and providers obtain the right {@link ResourceSet} simply via
 * {@code @Context ResourceSet} injection: Jersey calls
 * {@link ScopedResourceSetFactory#get()} to resolve and
 * {@link ScopedResourceSetFactory#dispose(ResourceSet)} to release the
 * underlying prototype-scoped OSGi service.
 *
 * <h3>OSGi service binding strategy</h3>
 * <p>This component is registered as a singleton JAX-RS extension via the OSGi
 * Jakarta RS Whiteboard. The whiteboard holds on to the singleton feature
 * instance for the lifetime of the JAX-RS application, so service references
 * use:
 * <ul>
 *   <li>{@code policy = DYNAMIC} &mdash; allows rebinding without
 *       deactivation/reactivation, which is required because the JAX-RS
 *       whiteboard holds on to the singleton instance.</li>
 *   <li>{@code policyOption = GREEDY} &mdash; ensures higher-ranked services
 *       (e.g. test mocks with {@code service.ranking = MAX_VALUE}) replace the
 *       current binding immediately.</li>
 * </ul>
 * <p>References are stored in {@link AtomicReference} fields for thread-safe
 * access. The {@code unbind} callbacks use
 * {@link AtomicReference#compareAndSet} to avoid nulling out a reference that
 * was already replaced by a higher-ranked service during a GREEDY rebind.
 *
 * @author Data In Motion
 * @since 1.0
 */
@Component(service = Feature.class)
@JakartarsExtension
@JakartarsName("ScopedResourceSetFeature")
public class ScopedResourceSetFeature implements Feature {

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
	public boolean configure(FeatureContext context) {
		context.register(new AbstractBinder() {
			@Override
			protected void configure() {
				// Bind this feature instance so the factory can reach the OSGi-side
				// AtomicReferences without fighting with HK2's lifecycle.
				bind(ScopedResourceSetFeature.this).to(ScopedResourceSetFeature.class);

				bindFactory(ScopedResourceSetFactory.class)
						.to(ResourceSet.class)
						.proxy(true)
						.proxyForSameScope(false)
						.in(RequestScoped.class);
			}
		});
		return true;
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
	ComponentServiceObjects<ResourceSet> resolveCso(String scopeName, String stageName) {
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
