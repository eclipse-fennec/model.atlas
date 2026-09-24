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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.eclipse.fennec.model.atlas.action.api.GateDiagnostic;
import org.eclipse.fennec.model.atlas.action.api.GateTrigger;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.rest.common.ModelAtlasRestConstants;
import org.eclipse.fennec.model.atlas.rest.model.ErrorResponse;
import org.eclipse.fennec.model.atlas.scope.api.StageGateRefusedException;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Unit tests for {@link GateRefusals} (issue #295): a refusal that names its object is
 * answered with that object's source-stage metadata as the 409 body, in the request's media
 * type; one that cannot be re-read keeps the 409 with the plain error body; anything that is
 * no refusal is left to the endpoint.
 */
@DisplayName("GateRefusals")
@SuppressWarnings({ "unchecked", "rawtypes" })
class GateRefusalsTest {

    private ScopeService scopeService;
    private ObjectMetadata metadata;

    @BeforeEach
    void setUp() {
        scopeService = mock(ScopeService.class);
        metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectId("Announce");
        metadata.setStage("draft");
    }

    private static StageGateRefusedException refusal() {
        return new StageGateRefusedException("Cannot transition object Announce from stage 'draft' to stage 'release'",
                GateTrigger.TRANSITION, "s", "transformations", "draft", "Announce",
                List.of(GateDiagnostic.error("qvto.does-not-compile", "does not compile")));
    }

    @Test
    @DisplayName("A refusal with an address answers 409 with the source-stage metadata as body")
    void refusalWithAddressCarriesTheMetadata() {
        when(scopeService.getMetadataFromStageForRegistry("transformations", "draft", "Announce")).thenReturn(metadata);

        Response response = GateRefusals.conflict(refusal(), scopeService, null);

        assertEquals(Status.CONFLICT.getStatusCode(), response.getStatus());
        assertSame(metadata, response.getEntity(), "the body is the object's metadata, not an error document");
        assertEquals(MediaType.APPLICATION_JSON_TYPE, response.getMediaType(), "JSON when the request resolved none");
    }

    @Test
    @DisplayName("A refusal inside a failed promise is found, and the body takes the request's media type")
    void wrappedRefusalFollowsTheRequest() {
        when(scopeService.getMetadataFromStageForRegistry("transformations", "draft", "Announce")).thenReturn(metadata);
        ContainerRequestContext requestContext = mock(ContainerRequestContext.class);
        when(requestContext.getProperty(ModelAtlasRestConstants.RESOLVED_MEDIA_TYPE)).thenReturn("application/xmi");

        Response response = GateRefusals.conflict(new InvocationTargetException(refusal()), scopeService,
                requestContext);

        assertEquals(Status.CONFLICT.getStatusCode(), response.getStatus());
        assertEquals(MediaType.valueOf("application/xmi"), response.getMediaType());
        assertSame(metadata, response.getEntity());
    }

    @Test
    @DisplayName("A refusal whose object cannot be re-read keeps the 409 with the plain error body")
    void unreadableObjectFallsBackToTheErrorBody() {
        when(scopeService.getMetadataFromStageForRegistry(any(), any(), any())).thenReturn(null);

        Response response = GateRefusals.conflict(refusal(), scopeService, null);

        assertEquals(Status.CONFLICT.getStatusCode(), response.getStatus());
        ErrorResponse body = assertInstanceOf(ErrorResponse.class, response.getEntity());
        assertEquals("409", body.getCode());
        assertEquals(refusal().getMessage(), body.getMessage(), "the reason still reaches the client");
    }

    @Test
    @DisplayName("A failing re-read is a fallback, not a 500")
    void failingReadFallsBack() {
        when(scopeService.getMetadataFromStageForRegistry(any(), any(), any()))
                .thenThrow(new IllegalStateException("storage down"));

        Response response = GateRefusals.conflict(refusal(), scopeService, null);

        assertEquals(Status.CONFLICT.getStatusCode(), response.getStatus());
        assertInstanceOf(ErrorResponse.class, response.getEntity());
    }

    @Test
    @DisplayName("A refusal raised without an address is answered with the error body")
    void legacyRefusalKeepsTheErrorBody() {
        Response response = GateRefusals.conflict(new StageGateRefusedException("the gate says no"), scopeService,
                null);

        assertEquals(Status.CONFLICT.getStatusCode(), response.getStatus());
        assertInstanceOf(ErrorResponse.class, response.getEntity());
    }

    @Test
    @DisplayName("Anything that is no refusal is left to the endpoint")
    void otherFailuresAreNotAnswered() {
        assertNull(GateRefusals.conflict(new IllegalStateException("boom"), scopeService, null));
        assertNull(GateRefusals.conflict(new InvocationTargetException(new RuntimeException("boom")), scopeService,
                null));
    }
}
