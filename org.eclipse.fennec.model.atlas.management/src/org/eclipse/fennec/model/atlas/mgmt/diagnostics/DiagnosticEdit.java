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

import static java.util.Objects.requireNonNull;

import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticStatus;

/**
 * A change to one diagnostic, as the {@link DiagnosticService} applies it: what to set,
 * who is setting it and why. A {@code null} severity, status or message keeps the current
 * value, so an edit says only what it means to change.
 *
 * @param severity  the new severity, or {@code null} to keep it
 * @param status    the new status, or {@code null} to keep it
 * @param message   the new message, or {@code null} to keep it
 * @param changedBy who makes the change: a user name for a person, a module name for an
 *                  automatic change; recorded in the history and carried by the event
 * @param reason    why, in the words of whoever makes it; may be {@code null}
 */
public record DiagnosticEdit(DiagnosticSeverity severity, DiagnosticStatus status, String message, String changedBy,
        String reason) {

    public DiagnosticEdit {
        requireNonNull(changedBy, "changedBy: every change names who made it");
        if (severity == null && status == null && message == null) {
            throw new IllegalArgumentException("an edit has to change something");
        }
    }

    /** Sets the status, keeps everything else. */
    public static DiagnosticEdit status(DiagnosticStatus status, String changedBy, String reason) {
        return new DiagnosticEdit(null, requireNonNull(status, "status"), null, changedBy, reason);
    }

    /** Sets the severity, keeps everything else. */
    public static DiagnosticEdit severity(DiagnosticSeverity severity, String changedBy, String reason) {
        return new DiagnosticEdit(requireNonNull(severity, "severity"), null, null, changedBy, reason);
    }
}
