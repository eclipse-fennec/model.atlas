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
package org.eclipse.fennec.model.atlas.mediatypes.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceFactoryImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.fennec.model.atlas.mediatypes.api.MediatypeContributor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SupportedMediatypesImplTest {

    private SupportedMediatypesImpl mediatypes;

    @BeforeEach
    void setUp() {
        mediatypes = new SupportedMediatypesImpl();
        mediatypes.bindResourceSet(resourceSetServing("application/json", "text/csv"));
    }

    @Test
    void listsWhatTheResourceSetCanWrite() {
        List<String> supported = mediatypes.getSupportedMediaTypes();

        assertTrue(supported.contains("application/json"));
        assertTrue(supported.contains("text/csv"));
        assertTrue(supported.contains("application/xmi"), "the formats written without a factory");
    }

    @Test
    void takesContributionsAndGivesThemUpAgain() {
        MediatypeContributor documentation = () -> List.of("text/html", "text/markdown");

        mediatypes.bindContributor(documentation);
        assertTrue(mediatypes.getSupportedMediaTypes().contains("text/html"));
        assertTrue(mediatypes.getSupportedMediaTypes().contains("text/markdown"));

        mediatypes.unbindContributor(documentation);
        assertFalse(mediatypes.getSupportedMediaTypes().contains("text/html"));
        assertFalse(mediatypes.getSupportedMediaTypes().contains("text/markdown"));
        assertTrue(mediatypes.getSupportedMediaTypes().contains("application/json"), "and nothing else was lost");
    }

    /** A rebind recomputes the list from the new ResourceSet - without dropping the contributions. */
    @Test
    void keepsContributionsAcrossAResourceSetRebind() {
        mediatypes.bindContributor(() -> List.of("text/html"));
        mediatypes.bindResourceSet(resourceSetServing("application/yaml"));

        List<String> supported = mediatypes.getSupportedMediaTypes();
        assertTrue(supported.contains("application/yaml"));
        assertTrue(supported.contains("text/html"));
        assertFalse(supported.contains("application/json"), "the old ResourceSet's formats are gone");
    }

    @Test
    void listsAMediaTypeOnce() {
        mediatypes.bindContributor(() -> List.of("text/html"));
        mediatypes.bindContributor(() -> List.of("text/html", "application/json"));

        assertEquals(1, mediatypes.getSupportedMediaTypes().stream().filter("text/html"::equals).count());
        assertEquals(1, mediatypes.getSupportedMediaTypes().stream().filter("application/json"::equals).count());
    }

    private static ResourceSet resourceSetServing(String... contentTypes) {
        ResourceSet resourceSet = new ResourceSetImpl();
        for (String contentType : contentTypes) {
            resourceSet.getResourceFactoryRegistry().getContentTypeToFactoryMap().put(contentType,
                    new ResourceFactoryImpl() {
                        @Override
                        public Resource createResource(org.eclipse.emf.common.util.URI uri) {
                            return super.createResource(uri);
                        }
                    });
        }
        return resourceSet;
    }

}
