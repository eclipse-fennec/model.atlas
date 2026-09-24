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
package org.eclipse.fennec.model.atlas.action.api;

import java.util.Objects;

/**
 * Another object a {@link GateDiagnostic} is about: one that depends on the
 * object under decision and bears the consequence of the operation.
 *
 * <p>
 * The delete guard is the case this was made for (issues #250, #294): a schema
 * that instances still refer to is not deleted, and when the caller deletes it
 * anyway with {@code force}, each of those instances is told that its schema is
 * gone. The gate names them here; the workflow writes the diagnostics to them
 * once the operation goes through, and does nothing to them when it is refused,
 * because then nothing happened to them.
 * </p>
 *
 * @param registry the registry holding the dependent, or {@code null} for the
 *                 registry of the object under decision
 * @param stage    the stage holding the dependent
 * @param objectId the dependent's identifier
 * @since 1.2
 */
public record Dependent(String registry, String stage, String objectId) {

    public Dependent {
        Objects.requireNonNull(stage, "a dependent is addressed by its stage");
        Objects.requireNonNull(objectId, "a dependent is addressed by its objectId");
    }

    /**
     * @param stage    the stage holding the dependent
     * @param objectId the dependent's identifier
     * @return a dependent in the same registry as the object under decision
     */
    public static Dependent inSameRegistry(String stage, String objectId) {
        return new Dependent(null, stage, objectId);
    }
}
