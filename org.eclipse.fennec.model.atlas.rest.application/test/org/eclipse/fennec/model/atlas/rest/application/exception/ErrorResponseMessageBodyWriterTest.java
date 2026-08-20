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
package org.eclipse.fennec.model.atlas.rest.application.exception;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.rest.model.ErrorResponse;
import org.eclipse.fennec.model.atlas.rest.model.RestFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;

/**
 * Unit tests for {@link ErrorResponseMessageBodyWriter}.
 *
 * <p>The point of this writer is that an {@link ErrorResponse} can be written
 * with nothing but the entity itself — no {@code ResourceSet}, no codec, no
 * scope/stage. These tests exercise exactly that: everything runs plain, off
 * the OSGi runtime.
 */
class ErrorResponseMessageBodyWriterTest {

	private final ErrorResponseMessageBodyWriter writer = new ErrorResponseMessageBodyWriter();

	@Test
	@DisplayName("Writes an ErrorResponse as JSON without any ResourceSet")
	void writesErrorResponseAsJson() throws IOException {
		ErrorResponse errorResponse = RestFactory.eINSTANCE.createErrorResponse();
		errorResponse.setMessage("An internal server error occurred");
		errorResponse.setCode("500");
		errorResponse.setTimestamp(Date.from(
				ZonedDateTime.of(2026, 8, 20, 7, 59, 33, 14_000_000, ZoneId.systemDefault()).toInstant()));

		assertEquals("{\"_type\":\"http://eclipse.org/fennec/model/atlas/rest/1.0#//ErrorResponse\","
				+ "\"message\":\"An internal server error occurred\","
				+ "\"code\":\"500\","
				+ "\"timestamp\":\"" + expectedTimestamp() + "\"}",
				write(errorResponse));
	}

	@Test
	@DisplayName("Claims ErrorResponse entities, not EObjects in general")
	void isWriteableForErrorResponseOnly() {
		assertTrue(writer.isWriteable(RestFactory.eINSTANCE.createErrorResponse().getClass(), null, null,
				MediaType.APPLICATION_JSON_TYPE),
				"The generated implementation class must be claimed, not just the interface");
		assertFalse(writer.isWriteable(EObject.class, null, null, MediaType.APPLICATION_JSON_TYPE),
				"Other EObjects stay with the codec's writer");
	}

	@Test
	@DisplayName("Unset attributes are omitted rather than written as null")
	void omitsUnsetAttributes() throws IOException {
		ErrorResponse errorResponse = RestFactory.eINSTANCE.createErrorResponse();
		errorResponse.setCode("503");

		assertEquals("{\"_type\":\"http://eclipse.org/fennec/model/atlas/rest/1.0#//ErrorResponse\","
				+ "\"code\":\"503\"}",
				write(errorResponse));
	}

	@Test
	@DisplayName("A message with JSON metacharacters stays valid JSON")
	void escapesTheMessage() throws IOException {
		ErrorResponse errorResponse = RestFactory.eINSTANCE.createErrorResponse();
		// Stack traces reach the message when MODELATLAS_DEBUG_STACKTRACE is set,
		// so quotes, backslashes and newlines are not hypothetical.
		errorResponse.setMessage("Scope \"a\\b\" not found\n\tat Foo.java:1");

		assertEquals("{\"_type\":\"http://eclipse.org/fennec/model/atlas/rest/1.0#//ErrorResponse\","
				+ "\"message\":\"Scope \\\"a\\\\b\\\" not found\\n\\tat Foo.java:1\"}",
				write(errorResponse));
	}

	private String write(ErrorResponse errorResponse) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		writer.writeTo(errorResponse, errorResponse.getClass(), null, null, MediaType.APPLICATION_JSON_TYPE,
				new MultivaluedHashMap<>(), out);
		return out.toString(StandardCharsets.UTF_8);
	}

	/** The codec's {@code EDate} format, in the JVM's own zone. */
	private static String expectedTimestamp() {
		return new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ").format(Date.from(
				ZonedDateTime.of(2026, 8, 20, 7, 59, 33, 14_000_000, ZoneId.systemDefault()).toInstant()));
	}
}
