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

import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticChange;

/**
 * The state of one diagnostic at one moment, as a {@link DiagnosticsChanged} event
 * carries it: what a reader would show, without children (each descendant travels as its
 * own delta) and without the full history. Enumerations are their literals, so a handler
 * built against this event survives a new literal.
 */
public class DiagnosticState {

    /** The diagnostic's stable id. */
    public String id;
    /** The producer that owns it. */
    public String producer;
    /** The producer's code for the kind of finding. */
    public String code;
    /** {@code INFO}, {@code WARNING} or {@code ERROR}. */
    public String severity;
    /** {@code OPEN}, {@code ACKNOWLEDGED} or {@code RESOLVED}. */
    public String status;
    /** The message, written for people. */
    public String message;
    /** The affected element, or {@code null} for the object as a whole. */
    public String target;
    /** The optimistic-locking version at that moment. */
    public long version;
    /** Who made the newest change recorded in the history, or {@code null} when untouched. */
    public String changedBy;
    /** The id of the diagnostic this one refines, or {@code null} for a root. */
    public String parentId;

    /** Captures the state of a diagnostic. */
    public static DiagnosticState of(Diagnostic diagnostic) {
        DiagnosticState state = new DiagnosticState();
        state.id = diagnostic.getId();
        state.producer = diagnostic.getProducer();
        state.code = diagnostic.getCode();
        state.severity = diagnostic.getSeverity() == null ? null : diagnostic.getSeverity().getLiteral();
        state.status = diagnostic.getStatus() == null ? null : diagnostic.getStatus().getLiteral();
        state.message = diagnostic.getMessage();
        state.target = diagnostic.getTarget();
        state.version = diagnostic.getVersion();
        if (!diagnostic.getHistory().isEmpty()) {
            DiagnosticChange newest = diagnostic.getHistory().get(diagnostic.getHistory().size() - 1);
            state.changedBy = newest.getChangedBy();
        }
        if (diagnostic.eContainer() instanceof Diagnostic parent) {
            state.parentId = parent.getId();
        }
        return state;
    }

    /** Tells whether two states would look different to a reader. */
    public static boolean differ(DiagnosticState a, DiagnosticState b) {
        return !java.util.Objects.equals(a.severity, b.severity) || !java.util.Objects.equals(a.status, b.status)
                || !java.util.Objects.equals(a.message, b.message) || !java.util.Objects.equals(a.target, b.target)
                || a.version != b.version || !java.util.Objects.equals(a.parentId, b.parentId);
    }
}
