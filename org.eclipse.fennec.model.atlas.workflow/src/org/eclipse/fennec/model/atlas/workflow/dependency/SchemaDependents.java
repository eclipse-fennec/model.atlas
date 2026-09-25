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

import java.util.List;

import org.osgi.annotation.versioning.ProviderType;

/**
 * The dependents query of issue #250: everything that would stop resolving if a schema
 * package left a stage.
 *
 * <p>
 * References are keyed by nsURI within the <em>view</em> a stage serves. A package in stage
 * {@code S} of the schema registry is what an object sees when it lives in {@code S} itself,
 * in an earlier stage of the schema registry's chain that has no own copy of the package in
 * between (the chain registries delegate upwards: {@code draft} resolves through
 * {@code approved} through {@code release}), or - when {@code S} is the final stage - in a
 * stage other registries have and the schema registry has not, which the chain configurator
 * wires to the final stage. The query walks exactly those stages. Other packages count only
 * in {@code S} itself: a registered package resolves its cross-package references against the
 * packages registered for its own stage.
 * </p>
 *
 * <p>
 * Three edge types are known: another schema whose types point into the package, an instance
 * whose object type is a class of the package, and a compiled transformation unit whose manifest
 * lists the package. The first two are answered here; further kinds arrive through
 * {@link SchemaDependentsContributor} services, so a bundle that knows its own objects'
 * references (the QVT bundle for units) contributes them without the workflow knowing their
 * models.
 * </p>
 */
@ProviderType
public interface SchemaDependents {

    /**
     * The objects that reference the package {@code nsURI} as served from {@code stage} of the
     * scope's schema registry.
     *
     * @param scope the scope
     * @param stage the schema registry stage the package is (or was) in
     * @param nsURI the package's namespace URI
     * @return the dependents, each once, in the order they were found; never {@code null}
     */
    List<SchemaDependent> dependentsOf(String scope, String stage, String nsURI);
}
