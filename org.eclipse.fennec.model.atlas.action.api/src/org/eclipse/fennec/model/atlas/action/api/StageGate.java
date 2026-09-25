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
import org.osgi.util.promise.Promises;

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
 * and an object may only move where every model it references is visible. The
 * second is the delete (issue #294): a schema that instances still refer to is
 * not deleted behind their back. Further triggers are added as {@code default}
 * methods that pass, so an existing gate keeps compiling and keeps its
 * behaviour when the SPI grows.
 * </p>
 *
 * <p>
 * A gate explains itself with {@link GateDiagnostic diagnostics}. Those of a
 * refusal are recorded on the object in its source stage under this gate's
 * {@link #producer() producer} name, replacing what the same gate recorded
 * there before, so a passing re-run clears an earlier veto. Findings a gate
 * makes about a {@link Dependent} are written to that object once the
 * operation goes through - which, for a delete, is how a caller that
 * <em>forces</em> past the veto leaves a trace on the objects that lose their
 * schema, instead of nothing.
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

    /**
     * Decides whether an object may be deleted from
     * {@link GateContext#sourceStage()}.
     *
     * <p>
     * Called after the workflow has found the object and before it is removed.
     * A caller may <em>force</em> the delete: the gate is still asked, but a
     * refusal no longer stops the operation - what remains of it are the
     * diagnostics the gate reported about the {@link Dependent dependents},
     * which the workflow writes to them. A gate whose refusal names the objects
     * that suffer from the delete therefore gives the forcing caller's decision
     * a visible consequence.
     * </p>
     *
     * <p>
     * Passes by default, so a gate written against 1.1 keeps its behaviour.
     * </p>
     *
     * @param ctx the delete about to happen
     * @return a promise resolving to the verdict; a failed promise aborts the
     *         delete as a fault
     * @since 1.2
     */
    default Promise<GateVerdict> beforeDelete(GateContext ctx) {
        return Promises.resolved(GateVerdict.pass());
    }

    /**
     * The name under which the workflow records this gate's diagnostics on an
     * object's metadata.
     *
     * <p>
     * A producer owns the diagnostics it writes: a new verdict replaces what
     * the same producer recorded before and leaves every other producer's
     * findings alone, and a diagnostic's stable id is minted from the producer,
     * the code and the target. The name therefore has to stay the same across
     * runs and versions of the gate, and no two gates asked about the same
     * objects may share it. The default is the implementation's class name;
     * override it when the class may move, or when several instances of one
     * class guard different things.
     * </p>
     *
     * @return the producer name, never {@code null} or blank
     * @since 1.2
     */
    default String producer() {
        return getClass().getName();
    }
}
