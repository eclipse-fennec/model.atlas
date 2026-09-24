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
package org.eclipse.fennec.model.atlas.rest.client.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.fennec.model.atlas.rest.client.api.ModelAtlasClientException;
import org.eclipse.fennec.model.atlas.rest.client.api.NotFoundException;
import org.eclipse.fennec.model.atlas.rest.client.api.OperationRefusedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.core.Response;

/**
 * Unit tests for the status mapping in {@link RestSupport}: a {@code 409} whose body is an
 * object's metadata with diagnostics is a gate's refusal and becomes an
 * {@link OperationRefusedException} carrying them (issue #295); every other {@code 409}
 * stays the plain conflict it was.
 */
@DisplayName("RestSupport status mapping")
class RestSupportTest {

    private static Response response(int status, String body) {
        Response r = mock(Response.class);
        when(r.getStatus()).thenReturn(status);
        when(r.hasEntity()).thenReturn(body != null);
        when(r.readEntity(String.class)).thenReturn(body);
        return r;
    }

    @Test
    @DisplayName("A 409 carrying metadata with diagnostics becomes an OperationRefusedException")
    void refusalCarriesTheDiagnostics() {
        String body = """
                {"_type":"ObjectMetadata","objectId":"GateUser","stage":"draft","diagnostics":[
                  {"id":"abc","producer":"QvtTransitionGate","code":"qvto.does-not-compile","severity":"ERROR",
                   "message":"source 'GateUser' does not compile against the 'release' stage view","category":"compile",
                   "target":"GateUser","status":"OPEN",
                   "children":[{"id":"def","code":"qvto.compiler-finding","severity":"ERROR","message":"unresolved import","target":"2:8"}]}
                ]}
                """;

        ModelAtlasClientException error = RestSupport.statusError(response(409, body), "transition GateUser");

        OperationRefusedException refused = assertInstanceOf(OperationRefusedException.class, error);
        assertEquals(1, refused.diagnostics().size());
        assertEquals("qvto.does-not-compile", refused.diagnostics().get(0).code());
        assertEquals("abc", refused.diagnostics().get(0).id());
        assertEquals(1, refused.diagnostics().get(0).children().size());
        assertEquals("2:8", refused.diagnostics().get(0).children().get(0).target());
        assertTrue(refused.getMessage().contains("does not compile"), refused.getMessage());
        assertTrue(refused.getMessage().contains("transition GateUser"), refused.getMessage());
    }

    @Test
    @DisplayName("A 409 with the plain error body stays a ModelAtlasClientException")
    void occupiedIdStaysAPlainConflict() {
        String body = "{\"_type\":\"ErrorResponse\",\"message\":\"stage 'release' holds another object\",\"code\":\"409\"}";

        ModelAtlasClientException error = RestSupport.statusError(response(409, body), "transition X");

        assertFalse(error instanceof OperationRefusedException);
        assertTrue(error.getMessage().contains("409"));
        assertTrue(error.getMessage().contains("holds another object"));
    }

    @Test
    @DisplayName("A 409 with metadata but no diagnostics, or no JSON at all, is a plain conflict")
    void metadataWithoutDiagnosticsIsAPlainConflict() {
        assertFalse(RestSupport.statusError(response(409, "{\"objectId\":\"X\",\"diagnostics\":[]}"),
                "x") instanceof OperationRefusedException);
        assertFalse(RestSupport.statusError(response(409, "<xmi/>"), "x") instanceof OperationRefusedException);
        assertFalse(RestSupport.statusError(response(409, null), "x") instanceof OperationRefusedException);
    }

    @Test
    @DisplayName("The other statuses keep their mapping")
    void otherStatusesUnchanged() {
        assertInstanceOf(NotFoundException.class, RestSupport.statusError(response(404, null), "x"));
        ModelAtlasClientException plain = RestSupport.statusError(response(500, "boom"), "x");
        assertFalse(plain instanceof OperationRefusedException);
        assertTrue(plain.getMessage().contains("boom"));
    }
}
