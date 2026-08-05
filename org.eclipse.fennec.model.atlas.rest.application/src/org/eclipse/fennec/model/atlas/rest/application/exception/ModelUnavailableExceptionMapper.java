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

import org.eclipse.fennec.model.atlas.mgmt.storage.ModelUnavailableException;
import org.eclipse.fennec.model.atlas.rest.model.ErrorResponse;
import org.eclipse.fennec.model.atlas.rest.model.RestFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;
import jakarta.ws.rs.ext.ExceptionMapper;

/**
 * Maps a {@link ModelUnavailableException} to <b>409 Conflict</b> instead of a generic 500.
 *
 * <p>The exception means a stored instance references a model (EPackage) that is not registered —
 * e.g. its schema was removed on a git branch, or a {@code .ecore} was deleted in the file backend.
 * The object itself exists, so this is neither a 404 (not found) nor a 500 (unexpected fault): it
 * is a state conflict. The response body names the object and the missing nsURI (via the exception
 * message) so the client knows which model to restore.
 *
 * <p>This mapper fires only when the exception propagates <em>unwrapped</em>. The storage read path
 * surfaces it <em>wrapped</em> (a failed OSGi {@code Promise} → {@code InvocationTargetException} →
 * {@code RuntimeException}), and resource methods that read content catch broadly — so those call
 * {@link #findInChain(Throwable)} + {@link #conflict(ModelUnavailableException)} directly rather
 * than relying on this mapper.
 */
@Component
@JakartarsExtension
@JakartarsName("ModelUnavailableExceptionMapper")
public class ModelUnavailableExceptionMapper implements ExceptionMapper<ModelUnavailableException> {

	private static final Logger logger = Logger.getLogger(ModelUnavailableExceptionMapper.class.getName());

	@Override
	public Response toResponse(ModelUnavailableException exception) {
		return conflict(exception);
	}

	/** Builds the 409 Conflict response for a model-unavailable condition. */
	public static Response conflict(ModelUnavailableException exception) {
		logger.log(Level.FINE, "Model unavailable for a requested object", exception);

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
	 * Returns the {@link ModelUnavailableException} in {@code t}'s cause chain, or {@code null} if
	 * there is none. The storage read path wraps it (failed {@code Promise}), so callers that catch
	 * broadly must look down the chain rather than test the top-level type.
	 */
	public static ModelUnavailableException findInChain(Throwable t) {
		for (Throwable c = t; c != null; c = c.getCause()) {
			if (c instanceof ModelUnavailableException mue) {
				return mue;
			}
		}
		return null;
	}
}
