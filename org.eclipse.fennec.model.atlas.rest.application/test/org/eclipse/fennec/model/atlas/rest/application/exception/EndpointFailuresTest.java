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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

/**
 * Unit tests for {@link EndpointFailures}.
 */
class EndpointFailuresTest {

    @AfterEach
    void clearInterrupt() {
        // Do not leak an interrupt into the next test.
        Thread.interrupted();
    }

    @Test
    @DisplayName("An interrupt restores the thread's interrupt status and becomes a 503")
    void interruptIsRestored() {
        assertFalse(Thread.currentThread().isInterrupted(), "precondition: thread not already interrupted");

        RuntimeException thrown = EndpointFailures.propagate(new InterruptedException("waiting on a promise"));

        assertTrue(Thread.currentThread().isInterrupted(),
                "The interrupt status must be restored, not swallowed as the old catch-alls did");
        assertEquals(Status.SERVICE_UNAVAILABLE.getStatusCode(),
                ((WebApplicationException) thrown).getResponse().getStatus());
    }

    @Test
    @DisplayName("A WebApplicationException keeps the status its raiser chose")
    void webApplicationExceptionPassesThrough() {
        WebApplicationException notFound = new WebApplicationException(Response.status(Status.NOT_FOUND).build());

        assertSame(notFound, EndpointFailures.propagate(notFound), "It is an answer, not a failure");
    }

    @Test
    @DisplayName("A failed promise is unwrapped, so what gets logged is the real failure")
    void invocationTargetExceptionIsUnwrapped() {
        IllegalStateException real = new IllegalStateException("storage is gone");

        RuntimeException thrown = EndpointFailures.propagate(new InvocationTargetException(real));

        assertSame(real, thrown.getCause(), "The reflection wrapper must not be what the mapper logs");
        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                ((WebApplicationException) thrown).getResponse().getStatus());
    }

    @Test
    @DisplayName("A WebApplicationException wrapped by a failed promise still keeps its status")
    void wrappedWebApplicationExceptionPassesThrough() {
        WebApplicationException forbidden = new WebApplicationException(Response.status(Status.FORBIDDEN).build());

        assertSame(forbidden, EndpointFailures.propagate(new InvocationTargetException(forbidden)));
    }

    @Test
    @DisplayName("Anything else becomes a 500 carrying the original failure")
    void otherFailuresBecomeServerErrors() {
        IOException failure = new IOException("/var/atlas/secret-path is unreadable");

        RuntimeException thrown = EndpointFailures.propagate(failure);

        assertEquals(Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                ((WebApplicationException) thrown).getResponse().getStatus());
        assertSame(failure, thrown.getCause(),
                "The mapper logs the cause; it is the client that must not see it");
    }
}
