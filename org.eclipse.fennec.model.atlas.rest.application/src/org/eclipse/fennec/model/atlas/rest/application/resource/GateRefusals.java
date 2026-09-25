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
package org.eclipse.fennec.model.atlas.rest.application.resource;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.rest.application.exception.StageGateRefusedExceptionMapper;
import org.eclipse.fennec.model.atlas.scope.api.StageGateRefusedException;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * The REST contract for a stage gate's refusal (issue #295): <b>409 Conflict</b> whose body is
 * the refused object's unchanged metadata from its source stage, the gate's findings in its
 * {@code diagnostics}.
 *
 * <p>
 * Since issue #294 the workflow records the gates' findings on the object before it raises the
 * {@link StageGateRefusedException}, and the exception says where. An endpoint that catches
 * the failure of a transition or a delete asks here first: if it is a refusal, the object is
 * re-read and <em>returned</em> as the {@code 409} - returned, not thrown, so the codec
 * serialises it with the endpoint's own options ({@code @ResourceOption} on the method) and in
 * the media type the request filter resolved, exactly like the endpoint's {@code 200}. One
 * document for a client to handle, whether the operation went through or not, and the same
 * document a later {@code GET} of the metadata returns; the reasons are the diagnostics'
 * messages.
 * </p>
 *
 * <p>
 * When the object cannot be re-read the answer is still a {@code 409}, with the plain error
 * body and the reasons as its message, so the status never depends on the body.
 * </p>
 */
final class GateRefusals {

    private static final Logger LOGGER = Logger.getLogger(GateRefusals.class.getName());

    private GateRefusals() {
    }

    /**
     * The {@code 409} for a gate's refusal hidden in {@code failure}, or {@code null} when
     * the failure is something else and the endpoint should go on propagating it.
     *
     * @param failure        what the endpoint caught; a refusal may sit anywhere in its
     *                       cause chain, the workflow raises it inside a promise
     * @param scopeService   the scope the request addressed, to re-read the object
     * @param requestContext the request, for the media type it resolved to; may be
     *                       {@code null}
     * @return the response to return, or {@code null}
     */
    static Response conflict(Throwable failure, ScopeService<?> scopeService, ContainerRequestContext requestContext) {
        StageGateRefusedException refusal = StageGateRefusedExceptionMapper.findInChain(failure);
        if (refusal == null) {
            return null;
        }
        ObjectMetadata metadata = reread(scopeService, refusal);
        if (metadata == null) {
            return StageGateRefusedExceptionMapper.conflict(refusal);
        }
        return conflict(refusal, metadata, requestContext == null ? null : ResourceSupport.resolvedMediaType(requestContext));
    }

    /**
     * The {@code 409} carrying {@code metadata} as its body.
     *
     * @param refusal   the refusal, for the log
     * @param metadata  the object's metadata as stored in its source stage
     * @param mediaType the media type to answer in, or {@code null} for JSON
     */
    static Response conflict(StageGateRefusedException refusal, ObjectMetadata metadata, String mediaType) {
        LOGGER.log(Level.FINE, "A stage gate refused an operation; answering with the object's metadata", refusal);
        return Response.status(Status.CONFLICT).entity(metadata)
                .type(mediaType == null || mediaType.isBlank() ? MediaType.APPLICATION_JSON : mediaType).build();
    }

    /**
     * Re-reads the metadata the refusal was recorded on, or {@code null} when the exception
     * names no address or the read does not succeed. A failure here must not turn the
     * refusal into a {@code 500}: the status is the answer, the body a courtesy.
     */
    private static ObjectMetadata reread(ScopeService<?> scopeService, StageGateRefusedException refusal) {
        if (scopeService == null || refusal.registry() == null || refusal.stage() == null
                || refusal.objectId() == null) {
            return null;
        }
        try {
            return scopeService.getMetadataFromStageForRegistry(refusal.registry(), refusal.stage(),
                    refusal.objectId());
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, String.format(
                    "A stage gate refused %s in (%s, %s, %s), but its metadata could not be re-read for the response body",
                    refusal.objectId(), refusal.scope(), refusal.registry(), refusal.stage()), e);
            return null;
        }
    }
}
