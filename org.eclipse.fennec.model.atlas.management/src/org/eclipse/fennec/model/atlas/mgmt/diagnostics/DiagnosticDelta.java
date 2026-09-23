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

/**
 * One diagnostic's change inside a {@link DiagnosticsChanged} event: its state before and
 * after. A handler that only wants to know "which findings changed" reads the ids; one
 * that reacts to a resolution compares the two statuses; one that mirrors the findings
 * somewhere else applies {@code after} and drops what has no {@code after}.
 */
public class DiagnosticDelta {

    /** The diagnostic appeared. */
    public static final String ADDED = "ADDED";
    /** The diagnostic is still there and looks different. */
    public static final String CHANGED = "CHANGED";
    /** The diagnostic is gone. */
    public static final String REMOVED = "REMOVED";

    /** The diagnostic's stable id; set even when {@link #before} or {@link #after} is not. */
    public String id;
    /** {@link #ADDED}, {@link #CHANGED} or {@link #REMOVED}. */
    public String kind;
    /** The state before the change, or {@code null} when it was added. */
    public DiagnosticState before;
    /** The state after the change, or {@code null} when it was removed. */
    public DiagnosticState after;
}
