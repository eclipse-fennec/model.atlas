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
 * One live scope: what it is, and what is under it.
 *
 * @since 1.0
 */
public class ScopeRuntimeDTO extends DTO {

    /** The scope name, as the configuration named it. */
    public String name;

    public String description;

    /** The scope this one reads through to, or {@code null} at the root. */
    public String parentScope;

    /** Whether reads read through to the parent scope's final stage. */
    public boolean inheriting;

    /** The {@code service.id} of the scope service. */
    public long serviceId;

    /** The registries this scope exposes. */
    public List<RegistryRuntimeDTO> registries;
}
