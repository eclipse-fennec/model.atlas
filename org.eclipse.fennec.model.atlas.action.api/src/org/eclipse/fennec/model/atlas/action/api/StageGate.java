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

import org.osgi.util.promise.Promise;

/**
 * A veto point the workflow consults <em>before</em> it commits a stage
 * mutation.
 *
 * <p>
 * A {@link StageActionService} reacts to a mutation that has already happened
 * and cannot stop it; its failures are logged and the operation stands. A gate
 * is the other half of that contract: it is asked first, and a
 * {@link GateVerdict#refused() refusal} aborts the operation before any store
 * is touched. The caller learns the {@link GateVerdict#reason() reason} as an
 * error of its own making (over HTTP a {@code 409 Conflict}), not as a server
 * fault.
 * </p>
 *
 * <p>
 * The first trigger is the stage transition (issue #248): a QVT source may
 * only be promoted when it compiles against the target stage's package view,
 * and an object may only move where every model it references is visible.
 * Further triggers are added as {@code default} methods that pass, so an
 * existing gate keeps compiling and keeps its behaviour when the SPI grows.
 * </p>
 *
 * <p>
 * A gate that cannot decide, because its promise fails, does <em>not</em> let
 * the operation through: the workflow treats an undecided gate as a fault of
 * the operation, because a check that silently passes on error is no check. A
 * gate that has nothing to say about an object answers
 * {@link GateVerdict#pass()}.
 * </p>
 *
 * @since 1.1
 */
public interface StageGate {

    /**
     * @param objectType the object type (for example the URI of the
     *                   {@code EPackage} EClass)
     * @return {@code true} if this gate wants to be consulted for objects of the
     *         given type
     */
    boolean supportsObjectType(String objectType);

    /**
     * Decides whether an object may move from {@link GateContext#sourceStage()}
     * to {@link GateContext#targetStage()}.
     *
     * <p>
     * Called after the workflow has validated the transition itself (allowed
     * pair, object present, target address free) and before anything is
     * written or deleted. The object is still readable in its source stage; the
     * target stage does not hold it yet, or holds its previous revision.
     * </p>
     *
     * @param ctx the transition about to happen
     * @return a promise resolving to the verdict; a failed promise aborts the
     *         transition as a fault
     */
    Promise<GateVerdict> beforeTransition(GateContext ctx);
}
