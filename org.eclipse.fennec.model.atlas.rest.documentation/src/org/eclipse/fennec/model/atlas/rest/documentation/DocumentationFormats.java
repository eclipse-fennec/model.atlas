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
import java.util.Map;

import org.eclipse.fennec.emf.util.documentation.generators.apis.EcoreToDocumentationOptions;

/**
 * Translates what a request asks for - a media type and a class diagram - into the generator mode
 * that produces it.
 *
 * <p>
 * {@link EcoreToDocumentationOptions} spans every format, so this is a table of the combinations
 * the two axes can name. Whether a generator implements the one that was asked for is the
 * generator's answer to give, through {@code getSupportedModes()}, not something decided here.
 * </p>
 */
final class DocumentationFormats {

    static final String HTML = "text/html";

    static final String MARKDOWN = "text/markdown";

    /** The {@code diagram} query parameter, and the default when a request does not send it. */
    static final String DIAGRAM_PARAMETER = "diagram";

    static final String DEFAULT_DIAGRAM = "mermaid";

    private static final Map<String, Map<String, EcoreToDocumentationOptions>> MODES = Map.of(
            HTML, Map.of(
                    "none", EcoreToDocumentationOptions.ONLY_HTML_CLASS_OVERVIEW,
                    "mermaid", EcoreToDocumentationOptions.HTML_WITH_MERMAID_CLASS_DIAGRAM,
                    "plantuml", EcoreToDocumentationOptions.HTML_WITH_PLANTUML_CLASS_DIAGRAM),
            MARKDOWN, Map.of(
                    "none", EcoreToDocumentationOptions.ONLY_MARKDOWN_CLASS_OVERVIEW,
                    "mermaid", EcoreToDocumentationOptions.MARKDOWN_WITH_MERMAID_CLASS_DIAGRAM,
                    "plantuml", EcoreToDocumentationOptions.MARKDOWN_WITH_PLANTUML_CLASS_DIAGRAM));

    private DocumentationFormats() {
    }

    /**
     * @param mediaType a media type, with any parameters already stripped
     * @return whether this bundle serves it at all
     */
    static boolean isDocumentation(String mediaType) {
        return mediaType != null && MODES.containsKey(mediaType);
    }

    static List<String> mediaTypes() {
        return MODES.keySet().stream().sorted().toList();
    }

    /** The diagrams a request may ask for, in the order they are offered to a caller. */
    static List<String> diagrams() {
        return List.of("none", "mermaid", "plantuml");
    }

    /**
     * @param mediaType a media type this bundle serves
     * @param diagram   the value of the {@code diagram} query parameter, or {@code null} for the
     *                  default
     * @return the generator mode for that combination, or {@code null} if {@code diagram} names no
     *         diagram this bundle knows
     */
    static EcoreToDocumentationOptions modeFor(String mediaType, String diagram) {
        Map<String, EcoreToDocumentationOptions> modes = MODES.get(mediaType);
        if (modes == null) {
            return null;
        }
        return modes.get(diagram == null || diagram.isBlank() ? DEFAULT_DIAGRAM : diagram.toLowerCase());
    }

}
