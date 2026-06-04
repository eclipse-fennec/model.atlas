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
package org.eclipse.fennec.model.atlas.rest.common;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;

import jakarta.inject.Provider;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;

/**
 * Base class for {@link EPackage} {@link MessageBodyReader}/
 * {@link MessageBodyWriter} implementations that need a {@link ResourceSet}
 * for de/serialization.
 *
 * <p>The scope/stage-specific {@link ResourceSet} is resolved per request by
 * the codec's {@code CodecResourceSetFeature}, which binds {@code ResourceSet}
 * in the JAX-RS request scope via the highest-ranked {@code ResourceSetProvider}
 * (the Model Atlas {@code ScopedResourceSetProvider}). The codec's
 * {@code CodecResourceSetCleanupFilter} releases it back to its OSGi prototype
 * CSO after the response has been written.</p>
 *
 * <p>Because MBR/MBW components are JAX-RS providers (effectively singletons),
 * a request-scoped {@link ResourceSet} cannot be injected into a plain field —
 * it would be {@code null}. Instead a {@link Provider} is injected and
 * {@link #getResourceSet()} resolves the current request's instance on every
 * call. This is safe under concurrent requests.</p>
 */
public abstract class AbstractEPackageMessageBodyHandler
        implements MessageBodyReader<EPackage>, MessageBodyWriter<EPackage> {

    @Context
    private Provider<ResourceSet> resourceSetProvider;

    /**
     * Resolves the {@link ResourceSet} for the current request. Within a single
     * request the same instance is returned on repeated calls (request scope).
     *
     * @return the per-request {@link ResourceSet}; never {@code null} in a
     *         correctly wired runtime
     */
    protected ResourceSet getResourceSet() {
        return resourceSetProvider.get();
    }
}
