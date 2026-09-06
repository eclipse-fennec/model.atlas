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

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.fennec.model.atlas.rest.model.ErrorResponse;
import org.eclipse.fennec.model.atlas.rest.model.RestFactory;
import org.eclipse.fennec.model.atlas.scope.api.StageOccupiedException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;

/**
 * Maps a {@link StageOccupiedException} to <b>409 Conflict</b> instead of a generic 500.
 *
 * <p>
 * The exception means the stage the request wanted to write into already holds a
 * <em>different</em> object under that id — the case a promotion must not overwrite
 * silently, since an objectId is unique per stage rather than across stages. The request
 * was well formed and broke no stage rule; it collided with state, which is what a 409
 * says. The exception's message travels to the client, because it names the id, the stage
 * holding it and how the two objects differ, and because the remedy — delete the occupant
 * first if it really is meant to be replaced — is the caller's to take.
 * </p>
 *
 * <p>
 * Like {@link ModelUnavailableExceptionMapper}, this mapper only fires when the
 * exception propagates <em>unwrapped</em>. The transition path raises it inside an OSGi
 * {@code Promise}, so it reaches a resource method wrapped (→
 * {@code InvocationTargetException}); {@link EndpointFailures#propagate(Exception)}
 * finds it in the chain with {@link #findInChain(Throwable)} and answers with the same
 * status.
 * </p>
 *
 * @author Data In Motion
 * @since Sep 6, 2026
 */
@Component
@JakartarsExtension
@JakartarsName("StageOccupiedExceptionMapper")
public class StageOccupiedExceptionMapper implements ExceptionMapper<StageOccupiedException> {

	private static final Logger logger = Logger.getLogger(StageOccupiedExceptionMapper.class.getName());

	@Override
	public Response toResponse(StageOccupiedException exception) {
		return conflict(exception);
	}

	/** Builds the 409 Conflict response for an occupied target address. */
	public static Response conflict(StageOccupiedException exception) {
		logger.log(Level.FINE, "A stage already holds a different object under that id", exception);

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
	 * Returns the {@link StageOccupiedException} in {@code t}'s cause chain, or
	 * {@code null} if there is none. The transition path wraps it (failed
	 * {@code Promise}), so callers that catch broadly must look down the chain rather
	 * than test the top-level type.
	 */
	public static StageOccupiedException findInChain(Throwable t) {
		for (Throwable c = t; c != null; c = c.getCause()) {
			if (c instanceof StageOccupiedException soe) {
				return soe;
			}
		}
		return null;
	}
}
