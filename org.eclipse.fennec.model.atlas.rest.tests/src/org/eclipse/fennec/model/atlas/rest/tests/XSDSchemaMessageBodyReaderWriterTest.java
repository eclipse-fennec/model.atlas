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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.rest.tests;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;

/**
 * Direct tests for the XSDSchemaMessageBodyReaderWriter, covering how a package
 * whose classes reference each other is serialized, without going through the
 * full REST endpoint.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class XSDSchemaMessageBodyReaderWriterTest {

    private static final String MEDIA_TYPE = "application/schema+xml";

    private static final String COMPONENT_NAME = "org.eclipse.fennec.model.atlas.rest.xsdschema.XSDSchemaMessageBodyReaderWriter";

    /**
     * Namespace declaration of an XML Schema document — what this handler serves. The
     * closing quote matters: the XMI serialization of the XSD metamodel declares
     * "…/XMLSchema-instance", which carries this namespace as a prefix.
     */
    private static final String XML_SCHEMA_NS = "\"http://www.w3.org/2001/XMLSchema\"";

    /** Namespace of EMF's XSD metamodel — what a fallback to XMI would serve instead. */
    private static final String XSD_METAMODEL_NS = "http://www.eclipse.org/xsd/2002/XSD";

    @SuppressWarnings("rawtypes")
    @InjectService
    List<MessageBodyWriter> messageBodyWriter;

    MessageBodyReader<EPackage> reader;
    MessageBodyWriter<EPackage> writer;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @BeforeEach
    void setUp(@InjectBundleContext BundleContext context) throws Exception {

        Collection<ServiceReference<MessageBodyReader>> readerReferences = context
                .getServiceReferences(MessageBodyReader.class, "(component.name=" + COMPONENT_NAME + ")");
        assertThat(readerReferences).isNotEmpty();

        Collection<ServiceReference<MessageBodyWriter>> writerReferences = context
                .getServiceReferences(MessageBodyWriter.class, "(component.name=" + COMPONENT_NAME + ")");
        assertThat(writerReferences).isNotEmpty();

        reader = (MessageBodyReader<EPackage>) context.getService(readerReferences.iterator().next());
        writer = (MessageBodyWriter<EPackage>) context.getService(writerReferences.iterator().next());

        assertNotNull(reader, "XSDSchemaMessageBodyReaderWriter service should be available");
        assertNotNull(writer, "XSDSchemaMessageBodyReaderWriter service should be available");

        // @Context fields are only injected by Jersey; inject a real ResourceSet
        // directly so readFrom/writeTo work outside a JAX-RS request scope.
        ResourceSet resourceSet = context.getService(context.getServiceReference(ResourceSet.class));
        TestHelper.injectResourceSet(reader, resourceSet);
        TestHelper.injectResourceSet(writer, resourceSet);
    }

    /**
     * A schema whose types reference each other must not be served with those
     * references routed through the file name the download is suggested to be saved
     * as: such a document only resolves as long as the caller keeps that exact name.
     */
    @Test
    void testWriteTo_DoesNotRouteIntraSchemaReferencesThroughTheFileName() throws Exception {
        EPackage ePackage = createIntraReferenceTestEPackage();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MultivaluedHashMap<String, Object> httpHeaders = new MultivaluedHashMap<>();
        writer.writeTo(ePackage, EPackage.class, EPackage.class, null, MediaType.valueOf(MEDIA_TYPE), httpHeaders,
                outputStream);

        String xsd = outputStream.toString("UTF-8");
        assertTrue(xsd.contains(XML_SCHEMA_NS), "Must serve an XML Schema document: " + xsd);
        assertFalse(xsd.contains(ePackage.getName() + ".xsd"),
                "The served schema must not reference its own file name: " + xsd);
        assertEquals("attachment; filename=" + ePackage.getName() + ".xsd",
                httpHeaders.getFirst(HttpHeaders.CONTENT_DISPOSITION), "Content-Disposition should name the .xsd file");
    }

    /**
     * The served schema has to resolve under any file name the caller picks, so a
     * reference between two of its types must survive the round trip.
     */
    @Test
    void testRoundTrip_PreservesIntraSchemaReference() throws Exception {
        EPackage ePackage = createIntraReferenceTestEPackage();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MediaType mediaType = MediaType.valueOf(MEDIA_TYPE);
        writer.writeTo(ePackage, EPackage.class, EPackage.class, null, mediaType, new MultivaluedHashMap<>(),
                outputStream);

        // The reader loads under "temp.xsd", a name the served document never saw.
        String xsd = outputStream.toString("UTF-8");
        EPackage roundTrip;
        try {
            roundTrip = reader.readFrom(EPackage.class, EPackage.class, null, mediaType, null,
                    new ByteArrayInputStream(outputStream.toByteArray()));
        } catch (RuntimeException e) {
            throw new AssertionError(
                    "Served schema should load again under any file name, but was rejected: " + e.getMessage() + "\n"
                            + xsd,
                    e);
        }

        EClass outer = (EClass) roundTrip.getEClassifier("Outer");
        assertNotNull(outer, "Round-trip package should contain the Outer type");
        EReference reference = (EReference) outer.getEStructuralFeature("inner");
        assertNotNull(reference, "Round-trip Outer should keep its inner reference");
        EClassifier innerType = reference.getEType();
        assertNotNull(innerType, "Reference type should be present");
        assertFalse(innerType.eIsProxy(), "Reference should resolve, not dangle as a proxy");
        assertEquals(roundTrip.getEClassifier("Inner"), innerType,
                "Reference should point at the Inner type of the same package");
    }

    /**
     * Which format this handler serves must not depend on the {@link ResourceSet} it
     * is handed. Given one that knows no {@code .xsd} extension, the resource factory
     * lookup falls back to XMI and the body becomes a serialization of the XSD
     * metamodel — offered to the client as {@code application/schema+xml}, with no
     * error, and rejected by this handler's own reader.
     */
    @Test
    void testWriteTo_ResourceSetWithoutXsdExtension_StillProducesASchemaDocument() throws Exception {
        TestHelper.injectResourceSet(writer, new ResourceSetImpl());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        writer.writeTo(createIntraReferenceTestEPackage(), EPackage.class, EPackage.class, null,
                MediaType.valueOf(MEDIA_TYPE), new MultivaluedHashMap<>(), outputStream);

        String xsd = outputStream.toString("UTF-8");
        assertFalse(xsd.contains(XSD_METAMODEL_NS), "Must not serve the XSD metamodel itself: " + xsd);
        assertTrue(xsd.contains(XML_SCHEMA_NS), "Must serve an XML Schema document: " + xsd);
    }

    /**
     * Creates a package whose {@code Outer.inner} reference points at another class
     * of the very same package — the reference kind that must stay inside the served
     * document.
     */
    private EPackage createIntraReferenceTestEPackage() {
        EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
        ePackage.setNsURI("http://test.eclipse.fennec/hreftest/1.0");
        ePackage.setName("HrefTestPackage");
        ePackage.setNsPrefix("href");

        EClass inner = EcoreFactory.eINSTANCE.createEClass();
        inner.setName("Inner");
        var value = EcoreFactory.eINSTANCE.createEAttribute();
        value.setName("v");
        value.setEType(EcorePackage.Literals.EINT);
        inner.getEStructuralFeatures().add(value);

        EClass outer = EcoreFactory.eINSTANCE.createEClass();
        outer.setName("Outer");
        EReference reference = EcoreFactory.eINSTANCE.createEReference();
        reference.setName("inner");
        reference.setEType(inner);
        reference.setContainment(true);
        outer.getEStructuralFeatures().add(reference);

        ePackage.getEClassifiers().add(inner);
        ePackage.getEClassifiers().add(outer);
        return ePackage;
    }
}
