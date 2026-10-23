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
package org.eclipse.fennec.model.atlas.runtime.info;

import java.util.List;

import org.osgi.dto.DTO;

/**
 * One registry within a scope.
 *
 * @since 1.0
 */
public class RegistryRuntimeDTO extends DTO {

    /**
     * The value of {@link #type} for the registry that holds a scope's schema packages.
     *
     * <p>
     * Named here rather than written out wherever it is needed: a reader deciding whether a
     * registry can hold packages at all is asking about this exact value.
     * </p>
     */
    public static final String TYPE_SCHEMA = "SCHEMA";

    public String name;

    public String description;

    /**
     * What this registry holds: {@code SCHEMA}, {@code COCL}, {@code TRANSFORMATION} or
     * {@code OTHER}, as the workflow API classifies it.
     *
     * <p>
     * A scope has at most one {@code SCHEMA} registry, whatever it is named — which is why a
     * consumer looking for schemas resolves it by this type and never by the name.
     * </p>
     */
    public String type;

    /** The stages this registry moves content through, in order. */
    public List<StageRuntimeDTO> stages;

    /**
     * The schema packages live in this registry, for a {@code SCHEMA} registry; empty otherwise.
     *
     * <p>
     * Each entry names the stage it lives in. The same nsURI legitimately appears more than once —
     * one per stage, in different versions — so an entry is identified by (stage, nsURI) and never
     * by the nsURI alone.
     * </p>
     */
    public List<SchemaPackageDTO> packages;
}
