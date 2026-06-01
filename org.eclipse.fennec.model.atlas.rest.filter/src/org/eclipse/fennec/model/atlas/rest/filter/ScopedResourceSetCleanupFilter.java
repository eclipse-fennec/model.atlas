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

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerResponseContext;
import jakarta.ws.rs.container.ContainerResponseFilter;

/**
 * Ungets the per-request {@link ResourceSet} from its {@link ComponentServiceObjects}
 * after the response has been generated. Pairs with
 * {@link ScopedResourceSetFactory}, which stashes the CSO under
 * {@link ScopedResourceSetFactory#ACTIVE_CSO_PROPERTY} during
 * {@code get()}.
 *
 * <p>Jersey's {@code DisposableSupplier.dispose()} is not reliably invoked
 * when the produced binding is proxied; this response filter is the
 * deterministic counterpart that guarantees the prototype-scoped OSGi
 * service is released exactly once per request.
 *
 * @author Data In Motion
 * @since 1.0
 */
@Component
@JakartarsExtension
@JakartarsName("ScopedResourceSetCleanupFilter")
public class ScopedResourceSetCleanupFilter implements ContainerResponseFilter {

	@Override
	public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) {
		Object property = requestContext.getProperty(ScopedResourceSetFactory.ACTIVE_CSO_PROPERTY);
		if (!(property instanceof ComponentServiceObjects<?> cso)) {
			return;
		}
		Object instance = requestContext.getProperty(ScopedResourceSetFactory.ACTIVE_RESOURCE_SET_PROPERTY);
		if (instance instanceof ResourceSet rs) {
			@SuppressWarnings("unchecked")
			ComponentServiceObjects<ResourceSet> typed = (ComponentServiceObjects<ResourceSet>) cso;
			typed.ungetService(rs);
		}
		requestContext.removeProperty(ScopedResourceSetFactory.ACTIVE_CSO_PROPERTY);
		requestContext.removeProperty(ScopedResourceSetFactory.ACTIVE_RESOURCE_SET_PROPERTY);
	}
}
