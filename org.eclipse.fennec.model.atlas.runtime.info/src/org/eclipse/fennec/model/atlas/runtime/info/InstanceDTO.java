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
 * What this Atlas is, as opposed to what it holds.
 *
 * @since 1.0
 */
public class InstanceDTO extends DTO {

    /** How this instance is identified, where a deployment says so. */
    public String id;

    /** The base URL(s) this Atlas serves on, as the {@code atlas.endpoint} property carries them. */
    public List<String> endpoints;

    /**
     * The version of the Atlas implementation itself.
     *
     * <p>
     * Not {@link AtlasRuntimeDTO#changeCount}, which counts changes, and not
     * {@link SchemaPackageDTO#version}, which is a model's declared version. Three different things.
     * </p>
     */
    public String implementationVersion;
}
