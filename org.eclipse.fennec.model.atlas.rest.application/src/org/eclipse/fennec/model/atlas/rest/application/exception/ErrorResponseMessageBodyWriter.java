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

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.rest.model.ErrorResponse;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;

/**
 * Writes the {@link ErrorResponse} entities produced by
 * {@link ModelAtlasExceptionMapper} as JSON, without going through the codec.
 *
 * <p>Error responses must be serializable under any circumstances. The generic
 * EObject writer resolves a scope/stage-specific {@code ResourceSet} for the
 * request first, so an error caused by an unresolvable scope/stage used to fail
 * again while writing the mapped error response — Jersey then logs "Error
 * occurred when processing a response created from an already mapped exception"
 * and the client receives a bodyless {@code 500} with the actual cause nowhere
 * to be seen. Being declared for {@link ErrorResponse} rather than for
 * {@code EObject}, this writer is the more specific match and needs no
 * {@code ResourceSet} at all.
 *
 * <p>The emitted JSON is byte-compatible with what the codec produced before:
 * the {@code _type} type discriminator followed by the {@code message},
 * {@code code} and {@code timestamp} attributes in {@code ErrorResponse}
 * declaration order.
 *
 * @author Data In Motion
 * @since 1.0
 */
@Component(name = "ErrorResponseMessageBodyWriter", service = MessageBodyWriter.class)
@JakartarsExtension
@JakartarsName("ErrorResponseMessageBodyWriter")
@Provider
@Produces(MediaType.APPLICATION_JSON)
public class ErrorResponseMessageBodyWriter implements MessageBodyWriter<ErrorResponse> {

	/** The format the codec used for {@code EDate}, e.g. 2026-08-20T07:59:33.014+0200. */
	private static final DateTimeFormatter TIMESTAMP_FORMAT = DateTimeFormatter
			.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSZ");

	@Override
	public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
		return ErrorResponse.class.isAssignableFrom(type);
	}

	@Override
	public void writeTo(ErrorResponse errorResponse, Class<?> type, Type genericType, Annotation[] annotations,
			MediaType mediaType, MultivaluedMap<String, Object> httpHeaders, OutputStream entityStream)
					throws IOException, WebApplicationException {

		Writer writer = new OutputStreamWriter(entityStream, StandardCharsets.UTF_8);
		writer.write('{');
		writer.write(member("_type", EcoreUtil.getURI(errorResponse.eClass()).toString()));
		appendIfSet(writer, "message", errorResponse.getMessage());
		appendIfSet(writer, "code", errorResponse.getCode());
		appendIfSet(writer, "timestamp", format(errorResponse.getTimestamp()));
		writer.write('}');
		// Only flushed, never closed: closing the entity stream is the container's job.
		writer.flush();
	}

	private static void appendIfSet(Writer writer, String name, String value) throws IOException {
		if (value == null) {
			return;
		}
		writer.write(',');
		writer.write(member(name, value));
	}

	private static String member(String name, String value) {
		return '"' + name + "\":\"" + escape(value) + '"';
	}

	private static String format(Date timestamp) {
		if (timestamp == null) {
			return null;
		}
		return TIMESTAMP_FORMAT.format(timestamp.toInstant().atZone(ZoneId.systemDefault()));
	}

	/**
	 * Escapes a JSON string value. Written out by hand so that error responses
	 * stay independent of any JSON library being wired up correctly.
	 */
	private static String escape(String value) {
		StringBuilder escaped = new StringBuilder(value.length() + 16);
		for (int i = 0; i < value.length(); i++) {
			char c = value.charAt(i);
			switch (c) {
			case '"' -> escaped.append("\\\"");
			case '\\' -> escaped.append("\\\\");
			case '\b' -> escaped.append("\\b");
			case '\f' -> escaped.append("\\f");
			case '\n' -> escaped.append("\\n");
			case '\r' -> escaped.append("\\r");
			case '\t' -> escaped.append("\\t");
			default -> {
				if (c < 0x20) {
					escaped.append(String.format("\\u%04x", (int) c));
				} else {
					escaped.append(c);
				}
			}
			}
		}
		return escaped.toString();
	}
}
