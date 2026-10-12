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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.fennec.emf.util.documentation.generators.apis.EcoreToDocumentationOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.UriInfo;

class DocumentationMessageBodyWriterTest {

    private static final MediaType HTML = new MediaType("text", "html");
    private static final MediaType MARKDOWN = new MediaType("text", "markdown");

    private DocumentationMessageBodyWriter writer;
    private EPackage ePackage;
    private EClass eClass;
    private final MultivaluedMap<String, String> queryParameters = new MultivaluedHashMap<>();

    @BeforeEach
    void setUp() {
        writer = new DocumentationMessageBodyWriter();
        writer.generators = List.of(FakeGenerator.html(), FakeGenerator.markdown());

        UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getQueryParameters()).thenReturn(queryParameters);
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(requestContext.getUriInfo()).thenReturn(uriInfo);
        writer.requestContextProvider = () -> requestContext;

        ePackage = EcoreFactory.eINSTANCE.createEPackage();
        ePackage.setName("zoo");
        ePackage.setNsURI("http://example.org/zoo");
        ePackage.setNsPrefix("zoo");
        eClass = EcoreFactory.eINSTANCE.createEClass();
        eClass.setName("Animal");
        EAttribute name = EcoreFactory.eINSTANCE.createEAttribute();
        name.setName("name");
        name.setEType(EcorePackage.eINSTANCE.getEString());
        eClass.getEStructuralFeatures().add(name);
        ePackage.getEClassifiers().add(eClass);
    }

    @Test
    void writesAPackageInEveryFormatAGeneratorClaims() throws IOException {
        assertTrue(writer.isWriteable(ePackage.getClass(), null, null, HTML));
        assertTrue(writer.isWriteable(ePackage.getClass(), null, null, MARKDOWN));

        assertEquals(FakeGenerator.documentationFor("zoo", EcoreToDocumentationOptions.HTML_WITH_MERMAID_CLASS_DIAGRAM),
                write(ePackage, HTML));
        assertEquals(
                FakeGenerator.documentationFor("zoo", EcoreToDocumentationOptions.MARKDOWN_WITH_MERMAID_CLASS_DIAGRAM),
                write(ePackage, MARKDOWN));
    }

    @Test
    void writesAClassifierToo() throws IOException {
        assertTrue(writer.isWriteable(eClass.getClass(), null, null, HTML));
        assertEquals(
                FakeGenerator.documentationFor("Animal", EcoreToDocumentationOptions.HTML_WITH_MERMAID_CLASS_DIAGRAM),
                write(eClass, HTML));
    }

    /**
     * The bytes are UTF-8 whatever the platform's default encoding is: the generated text is not
     * ASCII, and a response that leaves the charset to chance is a response that arrives mangled.
     */
    @Test
    void writesUtf8() throws IOException {
        ByteArrayOutputStream entityStream = new ByteArrayOutputStream();
        writer.writeTo(ePackage, ePackage.getClass(), null, null, HTML, headers(), entityStream);

        assertTrue(new String(entityStream.toByteArray(), StandardCharsets.UTF_8).contains("Tierärzte"));
        assertEquals(-1, new String(entityStream.toByteArray(), StandardCharsets.ISO_8859_1).indexOf("Tierärzte"));
    }

    @Test
    void refusesWhatItCannotDocument() {
        assertFalse(writer.isWriteable(EcoreFactory.eINSTANCE.createEAnnotation().getClass(), null, null, HTML),
                "an EObject that is neither a package nor a classifier");
        assertFalse(writer.isWriteable(ePackage.getClass(), null, null, MediaType.APPLICATION_JSON_TYPE),
                "a media type this bundle does not serve");
    }

    @Test
    void refusesAFormatNoBoundGeneratorClaims() {
        writer.generators = List.of(FakeGenerator.markdown());
        assertFalse(writer.isWriteable(ePackage.getClass(), null, null, HTML));
        assertTrue(writer.isWriteable(ePackage.getClass(), null, null, MARKDOWN));

        writer.generators = List.of();
        assertFalse(writer.isWriteable(ePackage.getClass(), null, null, MARKDOWN));
    }

    /** isWriteable said yes, then the generator went away before the body was written. */
    @Test
    void answers406WhenTheGeneratorIsGoneByWriteTime() {
        writer.generators = List.of();
        WebApplicationException failure = assertThrows(WebApplicationException.class, () -> writer.writeTo(ePackage,
                ePackage.getClass(), null, null, HTML, headers(), new ByteArrayOutputStream()));
        assertEquals(406, failure.getResponse().getStatus());
    }

    // ======================
    // ?diagram
    // ======================

    @Test
    void embedsMermaidUnlessAskedOtherwise() throws IOException {
        assertEquals(FakeGenerator.documentationFor("zoo", EcoreToDocumentationOptions.HTML_WITH_MERMAID_CLASS_DIAGRAM),
                write(ePackage, HTML));

        diagram("none");
        assertEquals(FakeGenerator.documentationFor("zoo", EcoreToDocumentationOptions.ONLY_HTML_CLASS_OVERVIEW),
                write(ePackage, HTML));

        diagram("mermaid");
        assertEquals(FakeGenerator.documentationFor("zoo", EcoreToDocumentationOptions.HTML_WITH_MERMAID_CLASS_DIAGRAM),
                write(ePackage, HTML));
    }

    @Test
    void embedsPlantumlWhereTheFormatSupportsIt() throws IOException {
        diagram("plantuml");
        assertEquals(
                FakeGenerator.documentationFor("zoo",
                        EcoreToDocumentationOptions.MARKDOWN_WITH_PLANTUML_CLASS_DIAGRAM),
                write(ePackage, MARKDOWN));
    }

    /**
     * HTML has no PlantUML renderer. The caller is told so, and told what it can ask for instead -
     * rather than being handed a document that silently has no diagram in it.
     */
    @Test
    void refusesADiagramTheFormatDoesNotSupport() {
        diagram("plantuml");

        WebApplicationException failure = assertThrows(WebApplicationException.class, () -> writer.writeTo(ePackage,
                ePackage.getClass(), null, null, HTML, headers(), new ByteArrayOutputStream()));

        assertEquals(400, failure.getResponse().getStatus());
        String message = failure.getResponse().getEntity().toString();
        assertTrue(message.contains("text/html"), message);
        assertTrue(message.contains("none, mermaid"), message);
        assertFalse(message.contains("plantuml,"), message);
    }

    @Test
    void refusesADiagramNobodyHasHeardOf() {
        diagram("ascii-art");

        WebApplicationException failure = assertThrows(WebApplicationException.class, () -> writer.writeTo(ePackage,
                ePackage.getClass(), null, null, MARKDOWN, headers(), new ByteArrayOutputStream()));

        assertEquals(400, failure.getResponse().getStatus());
        assertTrue(failure.getResponse().getEntity().toString().contains("none, mermaid, plantuml"));
    }

    /** A writer outside a request - there is no query string to read - still has its default. */
    @Test
    void worksWithoutARequestContext() throws IOException {
        writer.requestContextProvider = null;

        assertEquals(FakeGenerator.documentationFor("zoo", EcoreToDocumentationOptions.HTML_WITH_MERMAID_CLASS_DIAGRAM),
                write(ePackage, HTML));
    }

    private void diagram(String value) {
        queryParameters.putSingle(DocumentationFormats.DIAGRAM_PARAMETER, value);
    }

    private String write(EObject target, MediaType mediaType) throws IOException {
        ByteArrayOutputStream entityStream = new ByteArrayOutputStream();
        writer.writeTo(target, target.getClass(), null, null, mediaType, headers(), entityStream);
        return entityStream.toString(StandardCharsets.UTF_8);
    }

    private static MultivaluedMap<String, Object> headers() {
        return new MultivaluedHashMap<>();
    }

}
