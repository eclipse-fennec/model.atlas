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

import org.osgi.dto.DTO;

/**
 * One stage of a registry.
 *
 * @since 1.0
 */
public class StageRuntimeDTO extends DTO {

    public String name;

    public String description;

    public boolean readable;

    public boolean writable;

    /**
     * Whether this is the stage a read resolves when no stage was asked for.
     *
     * <p>
     * Named {@code finalStage} because {@code final} is a Java keyword. A scope names its own final
     * stage: {@code release}, {@code released} or anything else a deployment chose, so a consumer
     * resolves it from this flag and never from a stage name it expects.
     * </p>
     */
    public boolean finalStage;

    /**
     * How many schema packages are live in this stage.
     *
     * <p>
     * Packages only. Counting every object in a stage means asking the storage backend, and this
     * snapshot does not go to storage — see {@link AtlasRuntime#snapshot()}.
     * </p>
     */
    public int packageCount;
}
