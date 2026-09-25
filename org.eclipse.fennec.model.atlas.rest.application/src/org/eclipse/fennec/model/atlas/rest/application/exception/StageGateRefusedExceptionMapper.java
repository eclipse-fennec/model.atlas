/*
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
package org.eclipse.fennec.model.atlas.rest.application.exception;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.fennec.model.atlas.rest.model.ErrorResponse;
import org.eclipse.fennec.model.atlas.rest.model.RestFactory;
import org.eclipse.fennec.model.atlas.scope.api.StageGateRefusedException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;

/**
 * Maps a {@link StageGateRefusedException} that reaches the container to <b>409 Conflict</b>
 * instead of a generic 500.
 *
 * <p>
 * The exception means a stage gate refused a transition or a delete before it committed
 * (issues #248, #294): the object does not hold up in the target stage, or others still
 * depend on it. The request was well formed and broke no stage rule; what stops it is the
 * object's state relative to the operation, which the caller can change. That is a
 * {@code 409}.
 * </p>
 *
 * <p>
 * The <em>contract</em> for a refusal (issue #295) is a {@code 409} whose body is the refused
 * object's metadata from its source stage, diagnostics included. That body is built where
 * the request is known - in the endpoint, through
 * {@code org.eclipse.fennec.model.atlas.rest.application.resource.GateRefusals} - so the
 * codec serialises it with the endpoint's own options and in the media type the request
 * resolved to. This mapper is the safety net behind it: a refusal that escapes an endpoint
 * unwrapped still answers {@code 409}, with the plain error body and the gates' reasons as
 * its message, so the status never depends on the body. {@link EndpointFailures#propagate}
 * hands a refusal on unwrapped for exactly this reason.
 * </p>
 *
 * @author Data In Motion
 * @since Sep 21, 2026
 */
@Component
@JakartarsExtension
@JakartarsName("StageGateRefusedExceptionMapper")
public class StageGateRefusedExceptionMapper implements ExceptionMapper<StageGateRefusedException> {

	private static final Logger logger = Logger.getLogger(StageGateRefusedExceptionMapper.class.getName());

	@Override
	public Response toResponse(StageGateRefusedException exception) {
		return conflict(exception);
	}

	/** Builds the 409 Conflict response for a gate's refusal with the plain error body. */
	public static Response conflict(StageGateRefusedException exception) {
		logger.log(Level.FINE, "A stage gate refused an operation", exception);

		ErrorResponse errorResponse = RestFactory.eINSTANCE.createErrorResponse();
		errorResponse.setMessage(exception.getMessage());
		errorResponse.setCode(String.valueOf(Status.CONFLICT.getStatusCode()));
		errorResponse.setTimestamp(new Date());

		return Response.status(Status.CONFLICT)
				.entity(errorResponse)
				.type(MediaType.APPLICATION_JSON)
				.build();
	}

	public static StageGateRefusedException findInChain(Throwable t) {
		for (Throwable c = t; c != null; c = c.getCause()) {
			if (c instanceof StageGateRefusedException sgre) {
				return sgre;
			}
		}
		return null;
	}
}
