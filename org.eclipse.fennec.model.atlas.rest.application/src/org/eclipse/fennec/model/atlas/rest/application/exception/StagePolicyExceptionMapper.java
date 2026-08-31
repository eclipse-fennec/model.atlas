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
import org.eclipse.fennec.model.atlas.scope.api.StagePolicyException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;

/**
 * Maps a {@link StagePolicyException} to <b>403 Forbidden</b> instead of a generic 500.
 *
 * <p>
 * The exception means a registry's stage policy refused the operation — updating an
 * object that sits in a stage declared {@code final}, for instance. The request was
 * well formed and named an existing object in an existing, writable stage, so this is
 * neither a 400 nor a 500: it is a rule the caller cannot argue with, and one that no
 * retry will get past. The exception's message travels to the client, because it names
 * the stage and registry that refused.
 * </p>
 *
 * <p>
 * Like {@link ModelUnavailableExceptionMapper}, this mapper only fires when the
 * exception propagates <em>unwrapped</em>. The write paths raise it inside an OSGi
 * {@code Promise}, so it reaches a resource method wrapped (→
 * {@code InvocationTargetException}); {@link EndpointFailures#propagate(Exception)}
 * finds it in the chain with {@link #findInChain(Throwable)} and answers with the same
 * status.
 * </p>
 *
 * @author Data In Motion
 * @since Aug 28, 2026
 */
@Component
@JakartarsExtension
@JakartarsName("StagePolicyExceptionMapper")
public class StagePolicyExceptionMapper implements ExceptionMapper<StagePolicyException> {

	private static final Logger logger = Logger.getLogger(StagePolicyExceptionMapper.class.getName());

	@Override
	public Response toResponse(StagePolicyException exception) {
		return forbidden(exception);
	}

	/** Builds the 403 Forbidden response for a stage-policy refusal. */
	public static Response forbidden(StagePolicyException exception) {
		logger.log(Level.FINE, "A stage policy refused an operation", exception);

		ErrorResponse errorResponse = RestFactory.eINSTANCE.createErrorResponse();
		errorResponse.setMessage(exception.getMessage());
		errorResponse.setCode(String.valueOf(Status.FORBIDDEN.getStatusCode()));
		errorResponse.setTimestamp(new Date());

		return Response.status(Status.FORBIDDEN)
				.entity(errorResponse)
				.type(MediaType.APPLICATION_JSON)
				.build();
	}

	/**
	 * Returns the {@link StagePolicyException} in {@code t}'s cause chain, or
	 * {@code null} if there is none. The write paths wrap it (failed {@code Promise}),
	 * so callers that catch broadly must look down the chain rather than test the
	 * top-level type.
	 */
	public static StagePolicyException findInChain(Throwable t) {
		for (Throwable c = t; c != null; c = c.getCause()) {
			if (c instanceof StagePolicyException spe) {
				return spe;
			}
		}
		return null;
	}
}
