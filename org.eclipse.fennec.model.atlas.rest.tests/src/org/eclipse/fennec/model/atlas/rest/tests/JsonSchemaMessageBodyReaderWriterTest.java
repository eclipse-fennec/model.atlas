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
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;

/**
 * Direct unit tests for the JsonSchemaMessageBodyReaderWriter.
 * Tests both reading (JSON Schema -> EPackage) and writing (EPackage -> JSON Schema)
 * without going through the full REST endpoint.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class JsonSchemaMessageBodyReaderWriterTest {

	private static final String JSON_SCHEMA_MEDIA_TYPE = "application/schema+json";

	@SuppressWarnings("rawtypes")
	@InjectService List<MessageBodyWriter> messageBodyWriter;
	
	MessageBodyReader<EPackage> reader;	
	MessageBodyWriter<EPackage> writer;

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@BeforeEach
	void setUp(
			@InjectBundleContext BundleContext context
	) throws Exception {
		
		Collection<ServiceReference<MessageBodyReader>> readerReferences = context.getServiceReferences(MessageBodyReader.class, "(component.name=JSONSchemaMessagebodyReaderWriter)");
		assertThat(readerReferences).isNotEmpty();
		
		Collection<ServiceReference<MessageBodyWriter>> writerReferences = context.getServiceReferences(MessageBodyWriter.class, "(component.name=JSONSchemaMessagebodyReaderWriter)");
		assertThat(writerReferences).isNotEmpty();
		
		
		reader = (MessageBodyReader<EPackage>) context.getService(readerReferences.iterator().next());
		writer = (MessageBodyWriter<EPackage>) context.getService(writerReferences.iterator().next());
		
		assertNotNull(reader, "JsonSchemaMessageBodyReader service should be available");
		assertNotNull(writer, "JsonSchemaMessageBodyWriter service should be available");
	}

	// ============ WRITER TESTS ============

	@Test
	void testIsWriteable_WithEPackageAndJsonSchemaMediaType_ReturnsTrue() {
		// Given: EPackage class and JSON Schema media type
		MediaType mediaType = MediaType.valueOf(JSON_SCHEMA_MEDIA_TYPE);

		// When: Check if writable
		boolean writable = writer.isWriteable(EPackage.class, EPackage.class, null, mediaType);

		// Then: Should return true
		assertTrue(writable, "Should be writable for EPackage with application/schema+json");
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
	void testIsWriteable_WithWrongClass_ReturnsFalse() {
		// Given: Non-EPackage class
		MediaType mediaType = MediaType.valueOf(JSON_SCHEMA_MEDIA_TYPE);

		// When: Check if writable
		boolean writable = writer.isWriteable(String.class, String.class, null, mediaType);

		// Then: Should return false
		assertFalse(writable, "Should not be writable for non-EPackage class");
	}

	@Test
	void testWriteTo_SimpleEPackage_ProducesValidJsonSchema() throws Exception {
		// Given: A simple EPackage
		EPackage ePackage = createSimpleTestEPackage();

		// When: Write to output stream
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		MediaType mediaType = MediaType.valueOf(JSON_SCHEMA_MEDIA_TYPE);

		writer.writeTo(ePackage, EPackage.class, EPackage.class, null, mediaType, null, outputStream);

		// Then: Should produce valid JSON Schema
		String jsonSchema = outputStream.toString("UTF-8");
		assertNotNull(jsonSchema, "JSON Schema should not be null");
		assertFalse(jsonSchema.isEmpty(), "JSON Schema should not be empty");

		// Verify it contains expected JSON Schema elements
		assertTrue(jsonSchema.contains("\"definitions\""), "JSON Schema should contain definitions");
		assertTrue(jsonSchema.contains("SimpleTestPackage"), "JSON Schema should contain package name");
		assertTrue(jsonSchema.contains("Person"), "JSON Schema should contain Person class");
	}

	@Test
	void testWriteTo_ComplexEPackage_WithAttributes_ProducesJsonSchema() throws Exception {
		// Given: A complex EPackage with classes and attributes
		EPackage ePackage = createComplexTestEPackage();

		// When: Write to output stream
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		MediaType mediaType = MediaType.valueOf(JSON_SCHEMA_MEDIA_TYPE);

		writer.writeTo(ePackage, EPackage.class, EPackage.class, null, mediaType, null, outputStream);

		// Then: Should produce valid JSON Schema
		String jsonSchema = outputStream.toString("UTF-8");
		assertNotNull(jsonSchema, "JSON Schema should not be null");
		assertFalse(jsonSchema.isEmpty(), "JSON Schema should not be empty");

		// Verify it contains the complex structure
		assertTrue(jsonSchema.contains("Company"), "JSON Schema should contain Company class");
		assertTrue(jsonSchema.contains("Employee"), "JSON Schema should contain Employee class");
		assertTrue(jsonSchema.contains("name"), "JSON Schema should contain name attribute");
	}

	// ============ READER TESTS ============

	@Test
	void testIsReadable_WithEPackageAndJsonSchemaMediaType_ReturnsTrue() {
		// Given: EPackage class and JSON Schema media type
		MediaType mediaType = MediaType.valueOf(JSON_SCHEMA_MEDIA_TYPE);

		// When: Check if readable
		boolean readable = reader.isReadable(EPackage.class, EPackage.class, null, mediaType);

		// Then: Should return true
		assertTrue(readable, "Should be readable for EPackage with application/schema+json");
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
	void testReadFrom_SimpleJsonSchema_ProducesEPackage() throws Exception {
		// Given: A simple JSON Schema
		String jsonSchema = createSimpleJsonSchema();
		InputStream inputStream = new ByteArrayInputStream(jsonSchema.getBytes("UTF-8"));
		MediaType mediaType = MediaType.valueOf(JSON_SCHEMA_MEDIA_TYPE);

		// When: Read from input stream
		EPackage ePackage = reader.readFrom(EPackage.class, EPackage.class, null, mediaType, null, inputStream);

		// Then: Should produce valid EPackage
		assertNotNull(ePackage, "EPackage should not be null");
		assertNotNull(ePackage.getName(), "EPackage name should not be null");
		assertFalse(ePackage.getEClassifiers().isEmpty(), "EPackage should have classifiers");
	}

	@Test
	void testReadFrom_ComplexJsonSchemaFile_ProducesEPackage() throws Exception {
		// Given: A complex JSON Schema from file
		String jsonSchema = loadJsonSchemaFromFile("test-data/simple-person.schema.json");
		InputStream inputStream = new ByteArrayInputStream(jsonSchema.getBytes("UTF-8"));
		MediaType mediaType = MediaType.valueOf(JSON_SCHEMA_MEDIA_TYPE);

		// When: Read from input stream
		EPackage ePackage = reader.readFrom(EPackage.class, EPackage.class, null, mediaType, null, inputStream);

		// Then: Should produce valid EPackage
		assertNotNull(ePackage, "EPackage should not be null");
		assertNotNull(ePackage.getName(), "EPackage should have a name");
	}

	// ============ ROUND-TRIP TESTS ============

	@Test
	void testRoundTrip_WriteAndRead_PreservesEPackageStructure() throws Exception {
		// Given: An EPackage
		EPackage originalPackage = createComplexTestEPackage();
		String originalNsUri = originalPackage.getNsURI();
		String originalName = originalPackage.getName();
		int originalClassifierCount = originalPackage.getEClassifiers().size();

		// When: Write to JSON Schema and read back
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		MediaType mediaType = MediaType.valueOf(JSON_SCHEMA_MEDIA_TYPE);

		writer.writeTo(originalPackage, EPackage.class, EPackage.class, null, mediaType, null, outputStream);

		String jsonSchema = outputStream.toString("UTF-8");
		InputStream inputStream = new ByteArrayInputStream(jsonSchema.getBytes("UTF-8"));

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

	private String createSimpleJsonSchema() {
		return """
		{
		  "$schema": "https://json-schema.org/draft/2020-12/schema",
		  "$id": "http://test.eclipse.fennec/simple/1.0",
		  "title": "Simple Test Schema",
		  "type": "object",
		  "definitions": {
		    "Person": {
		      "type": "object",
		      "properties": {
		        "name": { "type": "string" },
		        "age": { "type": "integer" }
		      }
		    }
		  }
		}
		""";
	}

	private String loadJsonSchemaFromFile(String filePath) throws IOException {
		Path path = Paths.get(filePath);
		if (!Files.exists(path)) {
			// If file doesn't exist, return a simple schema
			return createSimpleJsonSchema();
		}
		return Files.readString(path);
	}
}
