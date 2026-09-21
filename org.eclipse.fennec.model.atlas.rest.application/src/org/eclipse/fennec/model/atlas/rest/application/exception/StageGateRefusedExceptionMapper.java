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
 * Maps a {@link StageGateRefusedException} to <b>409 Conflict</b> instead of a generic 500.
 *
 * <p>
 * The exception means a stage gate refused a transition before it committed: the object
 * does not hold up in the target stage, for instance a QVT source that does not compile
 * against the target stage's package view (issue #248). The request was well formed and
 * broke no stage rule; what stops it is the object's state relative to the target stage,
 * which the caller can change (promote the library first, fix the source). The gate's
 * reason travels to the client, because it says what to do.
 * </p>
 *
 * <p>
 * Like {@link StageOccupiedExceptionMapper}, this mapper only fires when the exception
 * propagates <em>unwrapped</em>; the transition path may raise it inside a failed
 * {@code Promise}, so {@link EndpointFailures#propagate(Exception)} finds it in the chain
 * with {@link #findInChain(Throwable)} and answers with the same status.
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

	/** Builds the 409 Conflict response for a gate's refusal. */
	public static Response conflict(StageGateRefusedException exception) {
		logger.log(Level.FINE, "A stage gate refused a transition", exception);

		ErrorResponse errorResponse = RestFactory.eINSTANCE.createErrorResponse();
		errorResponse.setMessage(exception.getMessage());
		errorResponse.setCode(String.valueOf(Status.CONFLICT.getStatusCode()));
		errorResponse.setTimestamp(new Date());

		return Response.status(Status.CONFLICT)
				.entity(errorResponse)
				.type(MediaType.APPLICATION_JSON)
				.build();
	}

	/**
	 * Returns the {@link StageGateRefusedException} in {@code t}'s cause chain, or
	 * {@code null} if there is none. Callers that catch broadly must look down the chain
	 * rather than test the top-level type, because a failed {@code Promise} wraps it.
	 */
	public static StageGateRefusedException findInChain(Throwable t) {
		for (Throwable c = t; c != null; c = c.getCause()) {
			if (c instanceof StageGateRefusedException sgre) {
				return sgre;
			}
		}
		return null;
	}
}
