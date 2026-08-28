/**
 * Copyright (c) 2012 - 2025 Data In Motion and others.
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
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.fennec.model.atlas.rest.tests.helper.TestHelper;
import org.eclipse.uml2.uml.resource.UMLResource;
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
 * Direct unit tests for the UMLMessageBodyReaderWriter. Tests both reading (UML
 * -> EPackage) and writing (EPackage -> UML) without going through the full
 * REST endpoint.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class UMLMessageBodyReaderWriterTest {

    private static final String MEDIA_TYPE = "application/uml";

    @SuppressWarnings("rawtypes")
    @InjectService
    List<MessageBodyWriter> messageBodyWriter;

    MessageBodyReader<EPackage> reader;
    MessageBodyWriter<EPackage> writer;

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @BeforeEach
    void setUp(@InjectBundleContext BundleContext context) throws Exception {

        Collection<ServiceReference<MessageBodyReader>> readerReferences = context
                .getServiceReferences(MessageBodyReader.class, "(component.name=UMLMessageBodyReaderWriter)");
        assertThat(readerReferences).isNotEmpty();

        Collection<ServiceReference<MessageBodyWriter>> writerReferences = context
                .getServiceReferences(MessageBodyWriter.class, "(component.name=UMLMessageBodyReaderWriter)");
        assertThat(writerReferences).isNotEmpty();

        reader = (MessageBodyReader<EPackage>) context.getService(readerReferences.iterator().next());
        writer = (MessageBodyWriter<EPackage>) context.getService(writerReferences.iterator().next());

        assertNotNull(reader, "UMLMessageBodyReaderWriter service should be available");
        assertNotNull(writer, "UMLMessageBodyReaderWriter service should be available");

        // @Context fields are only injected by Jersey; inject a real ResourceSet
        // directly so readFrom/writeTo work outside a JAX-RS request scope.
        ResourceSet resourceSet = context.getService(context.getServiceReference(ResourceSet.class));
        TestHelper.injectResourceSet(reader, resourceSet);
        TestHelper.injectResourceSet(writer, resourceSet);
    }

    // ============ WRITER TESTS ============

    @Test
    void testIsWriteable_WithEPackageAndUMLMediaType_ReturnsTrue() {
        // Given: EPackage class and UML media type
        MediaType mediaType = MediaType.valueOf(MEDIA_TYPE);

        // When: Check if writable
        boolean writable = writer.isWriteable(EPackage.class, EPackage.class, null, mediaType);

        // Then: Should return true
        assertTrue(writable, "Should be writable for EPackage with application/uml");
    }

    @Test
    void testIsWriteable_WithWrongMediaType_ReturnsFalse() {
        // Given: EPackage class but wrong media type
        MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;

        // When: Check if writable
        boolean writable = writer.isWriteable(EPackage.class, EPackage.class, null, mediaType);

        // Then: Should return false
        assertFalse(writable, "Should not be writable for EPackage with application/json");
    }

    @Test
    void testIsWriteable_WithParameterizedUMLMediaType_ReturnsTrue() {
        // Given: the handler's media type carrying a charset parameter, as a client
        // that spells out "application/uml;charset=UTF-8" sends it
        MediaType mediaType = MediaType.valueOf(MEDIA_TYPE + ";charset=UTF-8");

        // When: Check if writable
        boolean writable = writer.isWriteable(EPackage.class, EPackage.class, null, mediaType);

        // Then: parameters must not change the decision
        assertTrue(writable, "Should be writable for EPackage with application/uml;charset=UTF-8");
    }

    @Test
    void testIsReadable_WithParameterizedUMLMediaType_ReturnsTrue() {
        MediaType mediaType = MediaType.valueOf(MEDIA_TYPE + ";charset=UTF-8");

        boolean readable = reader.isReadable(EPackage.class, EPackage.class, null, mediaType);

        assertTrue(readable, "Should be readable for EPackage with application/uml;charset=UTF-8");
    }

    @Test
    void testIsWriteable_WithWrongClass_ReturnsFalse() {
        // Given: Non-EPackage class
        MediaType mediaType = MediaType.valueOf(MEDIA_TYPE);

        // When: Check if writable
        boolean writable = writer.isWriteable(String.class, String.class, null, mediaType);

        // Then: Should return false
        assertFalse(writable, "Should not be writable for non-EPackage class");
    }

    @Test
    void testWriteTo_SimpleEPackage_ProducesValidUMLPackage() throws Exception {
        // Given: A simple EPackage
        EPackage ePackage = createSimpleTestEPackage();

        // When: Write to output stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MediaType mediaType = MediaType.valueOf(MEDIA_TYPE);
        MultivaluedHashMap<String, Object> httpHeaders = new MultivaluedHashMap<>();

        writer.writeTo(ePackage, EPackage.class, EPackage.class, null, mediaType, httpHeaders, outputStream);

        // Then: Should produce valid UML XML
        String umlXml = outputStream.toString("UTF-8");
        assertNotNull(umlXml, "UML XML should not be null");
        assertFalse(umlXml.isEmpty(), "UML XML should not be empty");

        // Verify it contains expected UML elements
        assertTrue(umlXml.contains("<?xml"), "UML should be valid XML");
        assertTrue(umlXml.contains("uml:"), "UML should contain UML namespace");
        assertTrue(umlXml.contains("packagedElement"), "UML should contain packaged elements");

        // And offers the model as a named download, like the ecore/XSD siblings do
        assertEquals("attachment; filename=" + ePackage.getName() + ".uml",
                httpHeaders.getFirst(HttpHeaders.CONTENT_DISPOSITION), "Content-Disposition should name the .uml file");
    }

    @Test
    void testWriteTo_ComplexEPackage_WithAttributes_ProducesUML() throws Exception {
        // Given: A complex EPackage with classes and attributes
        EPackage ePackage = createComplexTestEPackage();

        // When: Write to output stream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MediaType mediaType = MediaType.valueOf(MEDIA_TYPE);

        writer.writeTo(ePackage, EPackage.class, EPackage.class, null, mediaType, new MultivaluedHashMap<>(), outputStream);

        // Then: Should produce valid UML XML
        String umlXml = outputStream.toString("UTF-8");
        assertNotNull(umlXml, "UML XML should not be null");
        assertFalse(umlXml.isEmpty(), "UML XML should not be empty");

        // Verify it contains the complex structure
        assertTrue(umlXml.contains("<?xml"), "UML should be valid XML");
        assertTrue(umlXml.contains("uml:"), "UML should contain UML namespace");
    }

    /**
     * Which format this handler serves must not depend on the {@link ResourceSet} it
     * is handed. Given one that knows no {@code .uml} extension, the resource factory
     * lookup falls back to a generic XMI resource, so the model is no longer written
     * as UML — and this handler's own reader is what has to read it back.
     */
    @Test
    void testWriteTo_ResourceSetWithoutUmlExtension_StillProducesAUMLModel() throws Exception {
        TestHelper.injectResourceSet(writer, new ResourceSetImpl());

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MediaType mediaType = MediaType.valueOf(MEDIA_TYPE);
        writer.writeTo(createIntraReferenceTestEPackage(), EPackage.class, EPackage.class, null, mediaType,
                new MultivaluedHashMap<>(), outputStream);

        String umlXml = outputStream.toString("UTF-8");
        assertTrue(umlXml.contains("uml:"), "Must serve a UML document: " + umlXml);

        // And the served model still round-trips through this handler's reader.
        EPackage roundTrip = reader.readFrom(EPackage.class, EPackage.class, null, mediaType, null,
                new ByteArrayInputStream(outputStream.toByteArray()));
        assertNotNull(roundTrip.getEClassifier("Outer"), "Served model should still be readable: " + umlXml);
    }

    // ============ READER TESTS ============

    @Test
    void testIsReadable_WithEPackageAndUMLMediaType_ReturnsTrue() {
        // Given: EPackage class and UML media type
        MediaType mediaType = MediaType.valueOf(MEDIA_TYPE);

        // When: Check if readable
        boolean readable = reader.isReadable(EPackage.class, EPackage.class, null, mediaType);

        // Then: Should return true
        assertTrue(readable, "Should be readable for EPackage with application/uml");
    }

    @Test
    void testIsReadable_WithWrongMediaType_ReturnsFalse() {
        // Given: EPackage class but wrong media type
        MediaType mediaType = MediaType.APPLICATION_JSON_TYPE;

        // When: Check if readable
        boolean readable = reader.isReadable(EPackage.class, EPackage.class, null, mediaType);

        // Then: Should return false
        assertFalse(readable, "Should not be readable for EPackage with application/json");
    }

    @Test
    void testReadFrom_SimpleUML_ProducesEPackage() throws Exception {
        // Given: A simple UML XML
        String umlXml = createSimpleUML();
        InputStream inputStream = new ByteArrayInputStream(umlXml.getBytes("UTF-8"));
        MediaType mediaType = MediaType.valueOf(MEDIA_TYPE);

        // When: Read from input stream
        EPackage ePackage = reader.readFrom(EPackage.class, EPackage.class, null, mediaType, null, inputStream);

        // Then: Should produce valid EPackage
        assertNotNull(ePackage, "EPackage should not be null");
        assertNotNull(ePackage.getName(), "EPackage name should not be null");
        assertFalse(ePackage.getEClassifiers().isEmpty(), "EPackage should have classifiers");
    }

    @Test
    void testReadFrom_ComplexUMLFile_ProducesEPackage() throws Exception {
        // Given: A complex UML from file
        String umlXml = loadUMLFromFile("test-data/simple-model.uml");
        InputStream inputStream = new ByteArrayInputStream(umlXml.getBytes("UTF-8"));
        MediaType mediaType = MediaType.valueOf(MEDIA_TYPE);

        // When: Read from input stream
        EPackage ePackage = reader.readFrom(EPackage.class, EPackage.class, null, mediaType, null, inputStream);

        // Then: Should produce valid EPackage
        assertNotNull(ePackage, "EPackage should not be null");
        assertNotNull(ePackage.getName(), "EPackage should have a name");
    }

    /**
     * A model whose classes reference each other must not be served with those
     * references routed through the file name the download is suggested to be saved
     * as: such a document only resolves as long as the caller keeps that exact name.
     */
    @Test
    void testWriteTo_DoesNotRouteIntraModelReferencesThroughTheFileName() throws Exception {
        EPackage ePackage = createIntraReferenceTestEPackage();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        writer.writeTo(ePackage, EPackage.class, EPackage.class, null, MediaType.valueOf(MEDIA_TYPE),
                new MultivaluedHashMap<>(), outputStream);

        String umlXml = outputStream.toString("UTF-8");
        assertFalse(umlXml.contains(ePackage.getName() + "." + UMLResource.FILE_EXTENSION),
                "The served model must not reference its own file name: " + umlXml);
    }

    /**
     * The served model has to resolve under any file name the caller picks, so a
     * reference between two of its classes must survive the round trip.
     */
    @Test
    void testRoundTrip_PreservesIntraModelReference() throws Exception {
        EPackage ePackage = createIntraReferenceTestEPackage();

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MediaType mediaType = MediaType.valueOf(MEDIA_TYPE);
        writer.writeTo(ePackage, EPackage.class, EPackage.class, null, mediaType, new MultivaluedHashMap<>(),
                outputStream);

        // The reader loads under "temp.uml", a name the served document never saw.
        EPackage roundTrip = reader.readFrom(EPackage.class, EPackage.class, null, mediaType, null,
                new ByteArrayInputStream(outputStream.toByteArray()));

        EClass outer = (EClass) roundTrip.getEClassifier("Outer");
        assertNotNull(outer, "Round-trip package should contain the Outer class");
        EReference reference = (EReference) outer.getEStructuralFeature("inner");
        assertNotNull(reference, "Round-trip Outer should keep its inner reference");
        EClassifier innerType = reference.getEType();
        assertNotNull(innerType, "Reference type should be present");
        assertFalse(innerType.eIsProxy(), "Reference should resolve, not dangle as a proxy");
        assertEquals(roundTrip.getEClassifier("Inner"), innerType,
                "Reference should point at the Inner class of the same package");
    }

    // ============ ROUND-TRIP TESTS ============

    @Test
    void testRoundTrip_WriteAndRead_PreservesEPackageStructure() throws Exception {
        // Given: An EPackage
        EPackage originalPackage = createComplexTestEPackage();
        String originalNsUri = originalPackage.getNsURI();
        String originalName = originalPackage.getName();
        int originalClassifierCount = originalPackage.getEClassifiers().size();

        // When: Write to UML and read back
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        MediaType mediaType = MediaType.valueOf(MEDIA_TYPE);

        writer.writeTo(originalPackage, EPackage.class, EPackage.class, null, mediaType, new MultivaluedHashMap<>(), outputStream);

        String umlXml = outputStream.toString("UTF-8");
        InputStream inputStream = new ByteArrayInputStream(umlXml.getBytes("UTF-8"));

        EPackage roundTripPackage = reader.readFrom(EPackage.class, EPackage.class, null, mediaType, null, inputStream);

        // Then: Should preserve the structure
        assertNotNull(roundTripPackage, "Round-trip EPackage should not be null");
        assertEquals(originalNsUri, roundTripPackage.getNsURI(), "NsURI should be preserved");
        assertEquals(originalName, roundTripPackage.getName(), "Name should be preserved");
        assertEquals(originalClassifierCount, roundTripPackage.getEClassifiers().size(),
                "Number of classifiers should be preserved");
    }

    // ============ HELPER METHODS ============

    private EPackage createSimpleTestEPackage() {
        EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
        ePackage.setNsURI("http://test.eclipse.fennec/simple/1.0");
        ePackage.setName("SimpleTestPackage");
        ePackage.setNsPrefix("simple");

        // Create a simple EClass
        EClass personClass = EcoreFactory.eINSTANCE.createEClass();
        personClass.setName("Person");

        ePackage.getEClassifiers().add(personClass);

        return ePackage;
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

    private EPackage createComplexTestEPackage() {
        EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
        ePackage.setNsURI("http://test.eclipse.fennec/complex/1.0");
        ePackage.setName("ComplexTestPackage");
        ePackage.setNsPrefix("complex");

        // Create Company class
        EClass companyClass = EcoreFactory.eINSTANCE.createEClass();
        companyClass.setName("Company");

        var companyNameAttr = EcoreFactory.eINSTANCE.createEAttribute();
        companyNameAttr.setName("name");
        companyNameAttr.setEType(EcorePackage.Literals.ESTRING);
        companyClass.getEStructuralFeatures().add(companyNameAttr);

        // Create Employee class
        EClass employeeClass = EcoreFactory.eINSTANCE.createEClass();
        employeeClass.setName("Employee");

        var employeeNameAttr = EcoreFactory.eINSTANCE.createEAttribute();
        employeeNameAttr.setName("name");
        employeeNameAttr.setEType(EcorePackage.Literals.ESTRING);
        employeeClass.getEStructuralFeatures().add(employeeNameAttr);

        var employeeIdAttr = EcoreFactory.eINSTANCE.createEAttribute();
        employeeIdAttr.setName("employeeId");
        employeeIdAttr.setEType(EcorePackage.Literals.EINT);
        employeeClass.getEStructuralFeatures().add(employeeIdAttr);

        ePackage.getEClassifiers().add(companyClass);
        ePackage.getEClassifiers().add(employeeClass);

        return ePackage;
    }

    private String createSimpleUML() {
        return """
                <?xml version="1.0" encoding="UTF-8"?>
                <uml:Model xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" xmlns:uml="http://www.eclipse.org/uml2/5.0.0/UML" xmi:id="_test_model" name="SimpleTestPackage">
                  <packagedElement xmi:type="uml:Class" xmi:id="_person_class" name="Person">
                    <ownedAttribute xmi:id="_person_name" name="name">
                      <type xmi:type="uml:PrimitiveType" href="pathmap://UML_LIBRARIES/UMLPrimitiveTypes.library.uml#String"/>
                    </ownedAttribute>
                    <ownedAttribute xmi:id="_person_age" name="age">
                      <type xmi:type="uml:PrimitiveType" href="pathmap://UML_LIBRARIES/UMLPrimitiveTypes.library.uml#Integer"/>
                    </ownedAttribute>
                  </packagedElement>
                </uml:Model>
                """;
    }

    private String loadUMLFromFile(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            // If file doesn't exist, return a simple UML
            return createSimpleUML();
        }
        return Files.readString(path);
    }
}
