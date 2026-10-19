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

import java.io.OutputStream;
import java.util.Set;

import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.emf.util.documentation.generators.apis.EcoreToDocumentationOptions;
import org.eclipse.fennec.emf.util.documentation.generators.apis.EcoreToDocumentationService;

/**
 * A generator that answers from memory, like the real ones, and fails the test if the writer ever
 * takes the file-writing route instead.
 */
class FakeGenerator implements EcoreToDocumentationService {

    private final String mediaType;
    private final String extension;
    private final Set<EcoreToDocumentationOptions> supportedModes;

    FakeGenerator(String mediaType, String extension, Set<EcoreToDocumentationOptions> supportedModes) {
        this.mediaType = mediaType;
        this.extension = extension;
        this.supportedModes = supportedModes;
    }

    static FakeGenerator html() {
        return new FakeGenerator(DocumentationFormats.HTML, ".html",
                Set.of(EcoreToDocumentationOptions.ONLY_HTML_CLASS_OVERVIEW,
                        EcoreToDocumentationOptions.HTML_WITH_MERMAID_CLASS_DIAGRAM));
    }

    static FakeGenerator markdown() {
        return new FakeGenerator(DocumentationFormats.MARKDOWN, ".md",
                Set.of(EcoreToDocumentationOptions.ONLY_MARKDOWN_CLASS_OVERVIEW,
                        EcoreToDocumentationOptions.MARKDOWN_WITH_MERMAID_CLASS_DIAGRAM,
                        EcoreToDocumentationOptions.MARKDOWN_WITH_PLANTUML_CLASS_DIAGRAM));
    }

    @Override
    public boolean canHandleMediaType(String mediaType) {
        return this.mediaType.equals(mediaType);
    }

    @Override
    public String getOutputFileExtension() {
        return extension;
    }

    @Override
    public String getOutputFolder(EcoreToDocumentationOptions mode) {
        return mode.name();
    }

    @Override
    public Set<EcoreToDocumentationOptions> getSupportedModes() {
        return supportedModes;
    }

    @Override
    public CharSequence generateDocumentation(EPackage ePackage, EcoreToDocumentationOptions mode) {
        requireSupported(mode);
        return documentationFor(ePackage.getName(), mode);
    }

    @Override
    public CharSequence generateDocumentation(EClassifier eClassifier, EcoreToDocumentationOptions mode) {
        requireSupported(mode);
        return documentationFor(eClassifier.getName(), mode);
    }

    @Override
    public OutputStream doGenerateDocumentation(EPackage ePackage, EcoreToDocumentationOptions mode,
            String outputFolderRoot) {
        throw new AssertionError("the writer took the file-writing route");
    }

    @Override
    public OutputStream doGenerateDocumentation(EClassifier eClassifier, EcoreToDocumentationOptions mode,
            String outputFolderRoot) {
        throw new AssertionError("the writer took the file-writing route");
    }

    /** Carries a non-ASCII character, so that an encoding slip cannot pass unnoticed. */
    static String documentationFor(String name, EcoreToDocumentationOptions mode) {
        return "# " + name + " (" + mode.name() + ") — Tierärzte";
    }

}
