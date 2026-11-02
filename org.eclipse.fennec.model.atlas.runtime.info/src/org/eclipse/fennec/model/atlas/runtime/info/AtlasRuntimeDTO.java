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
 * What this Atlas holds, at one moment.
 *
 * <p>
 * A DTO and not the model, for the reason the OSGi DTOs exist: a reader gets a snapshot it cannot
 * reach through, and it does not have to carry EMF to read a number. Everything here is a copy of
 * state that went on living the moment this was built.
 * </p>
 *
 * <p>
 * {@link #changeCount} is the number that also travels as the runtime service's
 * {@code service.changecount} property. A consumer bound with a dynamic reference is told when it
 * changes, which is the whole of the "something happened, ask again" protocol — see
 * {@link AtlasRuntime}. It counts changes rather than describing them: "has anything happened" is a
 * comparison, and "what" is another snapshot.
 * </p>
 *
 * @since 1.0
 */
public class AtlasRuntimeDTO extends DTO {

    /** How this Atlas names itself. */
    public String name;

    /** How often this snapshot has changed since the Atlas started. */
    public long changeCount;

    /** When this snapshot was taken, in epoch milliseconds. */
    public long takenAt;

    /** What this Atlas is, apart from what it holds. */
    public InstanceDTO instance;

    /** Every media type the REST layer can serve, and who announced it. */
    public List<MediaTypeDTO> mediaTypes;

    /** Every scope whose service is live. */
    public List<ScopeRuntimeDTO> scopes;

    /** Scopes the configuration asked for that are not live, and why. */
    public List<FailedScopeDTO> failedScopes;

    /** Registries that are configured but unusable, and why. */
    public List<FailedRegistryDTO> failedRegistries;

    /** Schema packages that did not become live EPackage services, and why. */
    public List<FailedPackageDTO> failedPackages;
}
