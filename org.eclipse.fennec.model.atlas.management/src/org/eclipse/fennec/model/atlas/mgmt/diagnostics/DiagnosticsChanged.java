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
package org.eclipse.fennec.model.atlas.mgmt.diagnostics;

import java.util.List;

/**
 * The event the workflow delivers on the OSGi Typed Event Bus whenever an object's
 * diagnostics changed (issue #293): one event per write, for the (scope, registry, stage,
 * objectId) it happened on, listing every diagnostic that looks different afterwards with
 * its state before and after.
 *
 * <p>
 * Every change goes out this way, whoever made it: a producer's re-validation that adds,
 * drops or escalates findings, a person resolving one through the {@link DiagnosticService},
 * a delete with {@code force} recording its consequence on the dependents. A write that
 * changes nothing delivers nothing, which is what keeps two modules reacting to each other
 * from looping: the second round finds nothing to change and the chain ends.
 * </p>
 *
 * <p>
 * Handlers subscribe as {@code TypedEventHandler<DiagnosticsChanged>} on {@link #TOPIC}. The
 * event is a DTO so the bus can carry it; a handler must not write diagnostics
 * unconditionally in response, only when the event tells it something it did not know.
 * </p>
 */
public class DiagnosticsChanged {

    /** The topic the event is delivered on. */
    public static final String TOPIC = "org/eclipse/fennec/model/atlas/diagnostics/DIAGNOSTICS_CHANGED";

    /** The scope of the object whose diagnostics changed. */
    public String scope;
    /** Its registry. */
    public String registry;
    /** Its stage; diagnostics are per stage. */
    public String stage;
    /** The object. */
    public String objectId;
    /** The producer whose findings were written; every delta belongs to it. */
    public String producer;
    /** When the change was written, epoch milliseconds. */
    public long time;
    /** Every diagnostic that looks different afterwards, with its state before and after. */
    public List<DiagnosticDelta> changes;
}
