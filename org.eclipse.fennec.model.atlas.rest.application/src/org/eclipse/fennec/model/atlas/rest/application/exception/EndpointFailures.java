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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.fennec.model.atlas.scope.api.StagePolicyException;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response.Status;

/**
 * Hands a failure raised inside an endpoint to {@link ModelAtlasExceptionMapper}.
 *
 * <p>
 * Endpoints used to end in {@code catch (Exception e) → 500 with e.getMessage()}. That
 * answered the request themselves, so the central mapper — which logs the throwable and
 * replaces its message with a generic one — never ran, and internal detail (storage
 * paths, parser errors, configuration dumps) went to the client. Endpoints now rethrow
 * through this class instead: catch what you can answer (a bad request, a conflict), and
 * let everything else become the mapper's problem.
 * </p>
 *
 * @author Data In Motion
 * @since Aug 6, 2026
 */
public final class EndpointFailures {

    private EndpointFailures() {
    }

    /**
     * Converts an endpoint failure into the unchecked exception to throw for it.
     *
     * <p>
     * A {@link WebApplicationException} keeps the status whoever raised it chose — it is
     * an answer, not a failure. An {@link InterruptedException} restores the thread's
     * interrupt status, which the old catch-alls silently dropped, and becomes a 503: the
     * request was abandoned, not broken. Anything else becomes a 500 carrying the original
     * throwable, so the mapper logs it in full and tells the client nothing beyond that
     * something went wrong. A {@link InvocationTargetException} — how a failed
     * {@code Promise.getValue()} surfaces — is unwrapped first, so what gets logged is the
     * actual failure rather than the reflection wrapper.
     * </p>
     *
     * <p>
     * An {@link UnsupportedOperationException} becomes a <strong>405</strong>. A registry that
     * refuses an operation outright — the atlas root scope's, whose schemas are the system's own and
     * which allows no upload, update, delete or transition — is answering the request, not failing
     * to serve it, and the distinction is the difference between "you cannot do that here" and "this
     * server is broken". Its message names the registry, so it is safe to pass on: it says nothing
     * about storage layout or configuration.
     * A {@link StagePolicyException} anywhere in that chain becomes a 403 carrying its
     * message: the registry refused the operation, and the client needs to read that as
     * a rule it cannot retry past rather than as a fault.
     * </p>
     *
     * @param failure the exception an endpoint caught; never {@code null}
     * @return the exception to throw; the caller is expected to {@code throw} the result
     *         so the compiler sees the method end there
     */
    public static RuntimeException propagate(Exception failure) {
        if (failure instanceof InterruptedException) {
            Thread.currentThread().interrupt();
            return new WebApplicationException("The request was interrupted", failure, Status.SERVICE_UNAVAILABLE);
        }
        Throwable cause = failure;
        if (failure instanceof InvocationTargetException && failure.getCause() != null) {
            cause = failure.getCause();
        }
        if (cause instanceof WebApplicationException webApplicationException) {
            return webApplicationException;
        }
        if (cause instanceof UnsupportedOperationException refused) {
            return new WebApplicationException(refused.getMessage(), refused, Status.METHOD_NOT_ALLOWED);
        }
        // A stage policy that refused the operation is an answer as well, and every
        // write path raises it inside a Promise — so it arrives here wrapped, where a
        // generic 500 would tell the client to retry something that can never succeed.
        // Answering it centrally keeps every endpoint that routes failures through this
        // class consistent, instead of each one recognising the refusal for itself.
        StagePolicyException refusal = StagePolicyExceptionMapper.findInChain(cause);
        if (refusal != null) {
            return new WebApplicationException(refusal.getMessage(), refusal, Status.FORBIDDEN);
        }
        return new WebApplicationException(cause, Status.INTERNAL_SERVER_ERROR);
    }
}
