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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.runtime.info.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;

import org.eclipse.fennec.model.atlas.runtime.info.AtlasRuntime;
import org.eclipse.fennec.model.atlas.runtime.info.AtlasRuntimeDTO;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * That the runtime service is there, once, and answers.
 *
 * <p>
 * This runtime deliberately contains no scopes, no storage and no REST layer. That is the
 * situation the service exists to describe — an Atlas where the configuration asked for things and
 * none of them came up — and a runtime service that needed any of them in order to register would
 * be absent exactly then.
 * </p>
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
public class AtlasRuntimeRegistrationIT {

    @InjectBundleContext
    BundleContext context;

    private Collection<ServiceReference<AtlasRuntime>> runtimeReferences() throws InvalidSyntaxException {
        return context.getServiceReferences(AtlasRuntime.class, null);
    }

    @Test
    @DisplayName("the service is registered exactly once")
    void registeredExactlyOnce() throws InvalidSyntaxException {
        Collection<ServiceReference<AtlasRuntime>> references = runtimeReferences();

        // Twice is the failure this guards: the component implements AtlasRuntime, so without
        // service = {} on it DS registers a copy of its own beside the one the publisher
        // registers — and only one of the two has a change count that ever moves. A consumer
        // that binds the other is told nothing for the lifetime of the framework.
        assertEquals(1, references.size(), "exactly one AtlasRuntime service, not one per registrar");
    }

    @Test
    @DisplayName("an Atlas where nothing came up still has a runtime service, and it says so")
    void answersWithNothingConfigured() throws InvalidSyntaxException {
        ServiceReference<AtlasRuntime> reference = runtimeReferences().iterator().next();
        AtlasRuntime runtime = context.getService(reference);
        assertNotNull(runtime, "the service must be there when nothing it reports on is");

        AtlasRuntimeDTO snapshot = runtime.snapshot();

        assertNotNull(snapshot);
        assertNotNull(snapshot.scopes, "empty-but-valid, never null");
        assertTrue(snapshot.scopes.isEmpty(), "no scopes in this runtime");
        assertNotNull(snapshot.failedScopes);
        assertNotNull(snapshot.mediaTypes);
        assertNotNull(snapshot.instance);
        assertTrue(snapshot.takenAt > 0, "a snapshot says when it was taken");
    }

    @Test
    @DisplayName("the service carries the standard change count property, and the DTO carries the same number")
    void changeCountIsPublishedAndReported() throws InvalidSyntaxException {
        ServiceReference<AtlasRuntime> reference = runtimeReferences().iterator().next();

        Object published = reference.getProperty(Constants.SERVICE_CHANGECOUNT);
        assertNotNull(published, "a watcher is told through " + Constants.SERVICE_CHANGECOUNT);
        assertTrue(published instanceof Long, "the standard property is a Long");

        AtlasRuntimeDTO snapshot = context.getService(reference).snapshot();
        assertTrue(snapshot.changeCount >= (Long) published,
                "one number with two publications; the field may have moved on since the property was set");
    }
}
