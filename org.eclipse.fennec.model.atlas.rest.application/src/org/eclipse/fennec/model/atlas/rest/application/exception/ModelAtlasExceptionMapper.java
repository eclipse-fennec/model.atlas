/**
 * Copyright (c) 2012 - 2026 Data In Motion and others.
 * All rights reserved.
 *
 * This program and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.rest.application.exception;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.fennec.model.atlas.rest.model.ErrorResponse;
import org.eclipse.fennec.model.atlas.rest.model.RestFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;

/**
 * Global exception mapper that prevents leaking internal details in error responses.
 *
 * <p>By default, all unhandled exceptions return a generic error message without
 * exposing stack traces or internal implementation details (TR-03187 W-19 conformance).
 *
 * <p>For debugging purposes, set the environment variable {@code MODELATLAS_DEBUG_STACKTRACE}
 * to {@code true} to include the full stack trace in error responses.
 *
 * @author Juergen Albert
 * @since 10 Mar 2026
 */
@Component
@JakartarsExtension
@JakartarsName("ModelAtlasExceptionMapper")
public class ModelAtlasExceptionMapper implements ExceptionMapper<Throwable> {

	private static final Logger logger = Logger.getLogger(ModelAtlasExceptionMapper.class.getName());

	private static final String DEBUG_STACKTRACE_ENV = "MODELATLAS_DEBUG_STACKTRACE";

	@Override
	public Response toResponse(Throwable exception) {
		if (exception instanceof WebApplicationException wae) {
			return handleWebApplicationException(wae);
		}
		logger.log(Level.SEVERE, "Unhandled exception in REST endpoint", exception);
		return buildErrorResponse(Status.INTERNAL_SERVER_ERROR, "An internal server error occurred", exception);
	}

	private Response handleWebApplicationException(WebApplicationException wae) {
		Response originalResponse = wae.getResponse();
		Status status = Status.fromStatusCode(originalResponse.getStatus());
		if (status == null) {
			status = Status.INTERNAL_SERVER_ERROR;
		}

		String message;
		if (status.getFamily() == Status.Family.SERVER_ERROR) {
			logger.log(Level.SEVERE, "Server error in REST endpoint", wae);
			message = "An internal server error occurred";
		} else {
			message = extractClientMessage(wae, originalResponse);
		}

		return buildErrorResponse(status, message, wae);
	}

	private String extractClientMessage(WebApplicationException wae, Response originalResponse) {
		Object entity = originalResponse.getEntity();
		if (entity instanceof String s && !s.isBlank()) {
			return s;
		}
		String exMessage = wae.getMessage();
		if (exMessage != null && !exMessage.isBlank()) {
			return exMessage;
		}
		Status status = Status.fromStatusCode(originalResponse.getStatus());
		return status != null ? status.getReasonPhrase() : "Request error";
	}

	private Response buildErrorResponse(Status status, String message, Throwable exception) {
		ErrorResponse errorResponse = RestFactory.eINSTANCE.createErrorResponse();
		errorResponse.setMessage(message);
		errorResponse.setCode(String.valueOf(status.getStatusCode()));
		errorResponse.setTimestamp(new Date());

		if (isDebugStacktraceEnabled()) {
			String stackTrace = getStackTraceAsString(exception);
			errorResponse.setMessage(message + "\n\n" + stackTrace);
		}

		return Response.status(status)
				.entity(errorResponse)
				.type(MediaType.APPLICATION_JSON)
				.build();
	}

	private boolean isDebugStacktraceEnabled() {
		return Boolean.parseBoolean(System.getenv(DEBUG_STACKTRACE_ENV));
	}

	private String getStackTraceAsString(Throwable throwable) {
		StringWriter sw = new StringWriter();
		throwable.printStackTrace(new PrintWriter(sw));
		return sw.toString();
	}
}
