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
import org.osgi.service.component.ComponentServiceObjects;

import jakarta.inject.Provider;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;

/**
 * Base class for {@link EPackage} {@link MessageBodyReader}/
 * {@link MessageBodyWriter} implementations that need a {@link ResourceSet} for
 * de/serialization.
 *
 * <p>
 * The scope/stage-specific {@link ResourceSet} is resolved per request via
 * {@link #resolveResourceSetFactory(ContainerRequestContext, ComponentServiceObjects)}:
 * if {@code ModelAtlasRequestFilter} populated the
 * {@link ModelAtlasRestConstants#RESOLVED_RESOURCE_SET_CSO} request property,
 * that CSO is used; otherwise the supplied fallback CSO is used (e.g. for
 * endpoints that do not target a specific scope/stage, or for direct unit
 * tests that invoke the handler outside a JAX-RS request).
 * </p>
 *
 * <p>
 * Subclasses must own the {@code @Reference ComponentServiceObjects<ResourceSet>}
 * (so DS can bind it) and the {@code @Context ContainerRequestContext}
 * (injected by JAX-RS per request), then pass both to
 * {@link #resolveResourceSetFactory(ContainerRequestContext, ComponentServiceObjects)}.
 * Always call {@code factory.getService()} / {@code factory.ungetService(rs)}
 * on the same local factory variable, to ensure the unget happens on the
 * matching CSO.
 * </p>
 */
public abstract class AbstractEPackageMessageBodyHandler
        implements MessageBodyReader<EPackage>, MessageBodyWriter<EPackage> {

    @Context
    private Provider<ContainerRequestContext> requestContextProvider;

    @SuppressWarnings("unchecked")
    protected ComponentServiceObjects<ResourceSet> getResourceSetFactory() {
        return (ComponentServiceObjects<ResourceSet>)
                requestContextProvider.get().getProperty(ModelAtlasRestConstants.RESOLVED_RESOURCE_SET_CSO);
    }
}
