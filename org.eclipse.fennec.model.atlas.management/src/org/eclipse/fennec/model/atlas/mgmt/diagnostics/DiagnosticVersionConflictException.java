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
 * Signals that a change named a diagnostic version that is no longer current: somebody
 * else changed the diagnostic in between. The caller re-reads, decides again and retries
 * with the version it then sees; nothing was written.
 *
 * @since Sep 23, 2026
 */
public class DiagnosticVersionConflictException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String diagnosticId;
    private final long expectedVersion;
    private final long currentVersion;

    public DiagnosticVersionConflictException(DiagnosticAddress at, String diagnosticId, long expectedVersion,
            long currentVersion) {
        super(String.format("Diagnostic %s on %s is at version %d, the change expected version %d", diagnosticId, at,
                currentVersion, expectedVersion));
        this.diagnosticId = diagnosticId;
        this.expectedVersion = expectedVersion;
        this.currentVersion = currentVersion;
    }

    public String getDiagnosticId() {
        return diagnosticId;
    }

    public long getExpectedVersion() {
        return expectedVersion;
    }

    public long getCurrentVersion() {
        return currentVersion;
    }
}
