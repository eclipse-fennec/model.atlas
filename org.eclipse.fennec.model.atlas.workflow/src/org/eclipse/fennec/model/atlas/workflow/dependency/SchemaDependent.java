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
package org.eclipse.fennec.model.atlas.workflow.dependency;

import static java.util.Objects.requireNonNull;

import org.eclipse.fennec.model.atlas.action.api.Dependent;

/**
 * One object that references a schema package by its nsURI and therefore stops resolving
 * when that package leaves the stage its view is served from.
 *
 * @param kind       which edge type this is
 * @param registry   the registry holding the dependent
 * @param stage      the stage of that registry holding the dependent
 * @param objectId   the dependent's id in that stage
 * @param objectType the dependent's object type (its EClass URI), may be {@code null}
 */
public record SchemaDependent(Kind kind, String registry, String stage, String objectId, String objectType) {

    /** The three edge types a schema package can be referenced through (issue #250). */
    public enum Kind {
        /** another package whose {@code eSuperTypes} or {@code eType}s point into the package */
        SCHEMA,
        /** an instance whose object type is a class of the package */
        INSTANCE,
        /** a compiled transformation unit whose manifest lists the package */
        UNIT
    }

    public SchemaDependent {
        requireNonNull(kind, "kind");
        requireNonNull(registry, "registry");
        requireNonNull(stage, "stage");
        requireNonNull(objectId, "objectId");
    }

    /** The same object, addressed the way a gate verdict names a consequence. */
    public Dependent toDependent() {
        return new Dependent(registry, stage, objectId);
    }

    /** {@code registry/stage/objectId}, the form a diagnostic's {@code target} uses. */
    public String address() {
        return registry + "/" + stage + "/" + objectId;
    }
}
