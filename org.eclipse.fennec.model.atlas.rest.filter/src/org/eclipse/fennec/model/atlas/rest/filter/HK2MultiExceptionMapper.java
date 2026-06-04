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
package org.eclipse.fennec.model.atlas.rest.filter;

import org.glassfish.hk2.api.MultiException;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;

/**
 * Unwraps {@link MultiException}s produced by HK2 when a Jersey injection
 * supplier (such as the codec's request-scoped {@code ResourceSet} binding,
 * which delegates to {@link ScopedResourceSetProvider}) throws a
 * {@link WebApplicationException}: the wrapping otherwise turns deliberate
 * 4xx responses into generic 500 errors.
 *
 * <p>If the {@code MultiException} contains a {@code WebApplicationException},
 * its embedded {@link Response} is returned as-is so the caller sees the
 * original status (e.g. 400 for an unknown scope/stage pair) and entity. If
 * not, the exception is mapped to a 500.
 *
 * @author Data In Motion
 * @since 1.0
 */
@Component
@JakartarsExtension
@JakartarsName("HK2MultiExceptionMapper")
@Provider
public class HK2MultiExceptionMapper implements ExceptionMapper<MultiException> {

	@Override
	public Response toResponse(MultiException exception) {
		for (Throwable cause : exception.getErrors()) {
			if (cause instanceof WebApplicationException wae) {
				return wae.getResponse();
			}
		}
		return Response.serverError().entity(exception.getMessage()).build();
	}
}
