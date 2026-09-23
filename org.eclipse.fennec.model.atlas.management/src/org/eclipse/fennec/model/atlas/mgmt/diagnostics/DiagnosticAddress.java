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
package org.eclipse.fennec.model.atlas.mgmt.diagnostics;

import static java.util.Objects.requireNonNull;

/**
 * Where an object's diagnostics live: the object's address in the Atlas. Diagnostics are
 * per stage, so the draft and the released copy of an object carry their own.
 *
 * @param scope    the scope
 * @param registry the registry
 * @param stage    the stage
 * @param objectId the object
 */
public record DiagnosticAddress(String scope, String registry, String stage, String objectId) {

    public DiagnosticAddress {
        requireNonNull(scope, "scope");
        requireNonNull(registry, "registry");
        requireNonNull(stage, "stage");
        requireNonNull(objectId, "objectId");
    }

    @Override
    public String toString() {
        return scope + "/" + registry + "/" + stage + "/" + objectId;
    }
}
