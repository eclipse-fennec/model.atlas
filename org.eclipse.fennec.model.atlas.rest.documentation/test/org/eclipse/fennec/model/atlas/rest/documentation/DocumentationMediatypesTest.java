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
package org.eclipse.fennec.model.atlas.rest.documentation;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;

import org.junit.jupiter.api.Test;

class DocumentationMediatypesTest {

    private final DocumentationMediatypes contributor = new DocumentationMediatypes();

    @Test
    void announcesOnlyWhatABoundGeneratorClaims() {
        contributor.generators = List.of(FakeGenerator.markdown());
        assertEquals(List.of(DocumentationFormats.MARKDOWN), contributor.getMediaTypes());

        contributor.generators = List.of(FakeGenerator.html(),
                FakeGenerator.markdown());
        assertEquals(List.of(DocumentationFormats.HTML, DocumentationFormats.MARKDOWN), contributor.getMediaTypes());
    }

    /**
     * A runtime without the generator bundles promises nothing, rather than advertising a format
     * whose request would then fail.
     */
    @Test
    void announcesNothingWithoutAGenerator() {
        contributor.generators = List.of();
        assertEquals(List.of(), contributor.getMediaTypes());

        contributor.generators = null;
        assertEquals(List.of(), contributor.getMediaTypes());
    }

}
