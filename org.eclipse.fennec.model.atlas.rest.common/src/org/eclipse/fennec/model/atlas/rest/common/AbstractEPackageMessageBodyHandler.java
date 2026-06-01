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

import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;

/**
 * Base class for {@link EPackage} {@link MessageBodyReader}/
 * {@link MessageBodyWriter} implementations that need a {@link ResourceSet}
 * for de/serialization.
 *
 * <p>The scope/stage-specific {@link ResourceSet} is injected via JAX-RS
 * {@code @Context} by the binder in
 * {@code org.eclipse.fennec.model.atlas.rest.filter.ScopedResourceSetFeature}.
 * Each request resolves the right per-scope/stage instance and the
 * {@code ScopedResourceSetCleanupFilter} releases it back to its OSGi
 * prototype CSO after the response has been written &mdash; subclasses no
 * longer have to manage the {@code getService}/{@code ungetService}
 * lifecycle themselves.</p>
 *
 * <p>Because MBR/MBW components are JAX-RS singletons, the injected
 * reference is a Jersey-generated proxy that resolves to the current
 * request's {@link ResourceSet} on every method call. This is safe under
 * concurrent requests; see {@code ScopedResourceSetIntegrationTest} in the
 * filter tests for the concurrency check.</p>
 */
public abstract class AbstractEPackageMessageBodyHandler
        implements MessageBodyReader<EPackage>, MessageBodyWriter<EPackage> {

    @Context
    protected ResourceSet resourceSet;
}
