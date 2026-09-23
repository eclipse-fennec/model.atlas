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
 * Signals that the object, or the diagnostic on it, a change addressed does not exist.
 * The message says which of the two.
 *
 * @since Sep 23, 2026
 */
public class DiagnosticNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DiagnosticNotFoundException(String message) {
        super(message);
    }

    /** The object is not there. */
    public static DiagnosticNotFoundException object(DiagnosticAddress at) {
        return new DiagnosticNotFoundException("No object at " + at);
    }

    /** The object is there, the diagnostic is not. */
    public static DiagnosticNotFoundException diagnostic(DiagnosticAddress at, String diagnosticId) {
        return new DiagnosticNotFoundException("No diagnostic " + diagnosticId + " on " + at);
    }
}
