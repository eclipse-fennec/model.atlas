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

/**
 * The operation a {@link StageGate} is asked about.
 *
 * <p>
 * A gate learns from {@link GateContext#trigger()} what is about to happen,
 * because the same finding means different things for different operations:
 * a schema with dependents may well be promoted, but not deleted (issue #294).
 * </p>
 *
 * @since 1.2
 */
public enum GateTrigger {

    /**
     * The object is about to move from {@link GateContext#sourceStage()} to
     * {@link GateContext#targetStage()}; asked through
     * {@link StageGate#beforeTransition(GateContext)}.
     */
    TRANSITION,

    /**
     * The object is about to be deleted from {@link GateContext#sourceStage()};
     * asked through {@link StageGate#beforeDelete(GateContext)}. There is no
     * target stage.
     */
    DELETE
}
