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

import java.util.List;

import org.eclipse.fennec.emf.util.documentation.generators.apis.EcoreToDocumentationService;
import org.eclipse.fennec.model.atlas.mediatypes.api.MediatypeContributor;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Announces the documentation formats to the REST API's media type list.
 *
 * <p>
 * The request filter rejects an {@code Accept} the list does not hold, before any writer is
 * consulted, so without this the writer in this bundle would never be reached. What is announced is
 * what a bound generator actually claims: a runtime that installs only the Markdown generator
 * offers Markdown and not HTML, and one that installs neither offers nothing - rather than
 * promising a format whose request would then fail.
 * </p>
 */
@Component(name = "DocumentationMediatypes", service = MediatypeContributor.class)
public class DocumentationMediatypes implements MediatypeContributor {

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC)
    volatile List<EcoreToDocumentationService> generators;

    @Override
    public List<String> getMediaTypes() {
        List<EcoreToDocumentationService> bound = generators;
        if (bound == null) {
            return List.of();
        }
        return DocumentationFormats.mediaTypes().stream()
                .filter(mediaType -> bound.stream().anyMatch(generator -> generator.canHandleMediaType(mediaType)))
                .sorted().toList();
    }

}
