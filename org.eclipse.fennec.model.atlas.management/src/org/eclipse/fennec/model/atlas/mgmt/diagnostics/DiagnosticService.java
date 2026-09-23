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
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticStatus;
import org.osgi.util.promise.Promise;

/**
 * Changes single diagnostics by id (issue #293): what a person or a module does with a
 * finding after its producer recorded it.
 *
 * <p>
 * A producer writes its findings wholesale, through
 * {@code RegistryService.updateDiagnostics}; that path re-validates. This service is the
 * other kind of change: somebody looked at one finding and decided about it. Every method
 * here names <em>who</em> ({@code changedBy}) and <em>which version</em> it decided about
 * ({@code expectedVersion}). The version is optimistic locking: a change that names a
 * version no longer current is refused with a {@link DiagnosticVersionConflictException}
 * and writes nothing, so two people deciding about the same finding cannot silently
 * overwrite each other. Every applied change appends a history entry, increments the
 * version and goes out as a {@link DiagnosticsChanged} event.
 * </p>
 *
 * <p>
 * <b>Who may change what.</b> Any caller may change any diagnostic, whoever produced it:
 * the GDPR officer resolves what the compliance action found, an operator acknowledges what
 * the compile gate reported. What protects the findings is the audit trail, not a fence:
 * {@code changedBy} lands in the history and in the event, and a change made through this
 * service is an <em>informed</em> change, one that saw the version it changes. That is also
 * what protects it from the producer: a later re-validation keeps a status a person set
 * (see {@link Diagnostics#replaceOwned}), because the person's decision carries a higher
 * version than the producer's fresh finding.
 * </p>
 *
 * <p>
 * Changes go both ways. A resolved finding may be reopened, a warning escalated to an
 * error, an error downgraded; the history keeps the trail.
 * </p>
 */
public interface DiagnosticService {

    /**
     * Adds a finding on behalf of a producer, next to what the producer already holds. For a
     * person's manual finding the producer is whatever names the person's role, for example
     * {@code gdpr.officer}; the finding's id is minted by the stable id rule when unset.
     *
     * @param at         the object
     * @param producer   the producer the finding belongs to
     * @param diagnostic the finding; copied, the caller's instance stays the caller's
     * @param changedBy  who adds it
     * @param reason     why; may be {@code null}
     * @return the stored finding, with its id, version and first history entry
     * @throws DiagnosticNotFoundException if the object is not there
     */
    Promise<Diagnostic> add(DiagnosticAddress at, String producer, Diagnostic diagnostic, String changedBy,
            String reason);

    /**
     * Applies an edit to one diagnostic.
     *
     * @param at              the object
     * @param diagnosticId    the diagnostic, root or descendant
     * @param expectedVersion the version the caller decided about
     * @param edit            what to change, who changes it and why
     * @return the diagnostic after the change
     * @throws DiagnosticNotFoundException        if the object or the diagnostic is not
     *                                            there
     * @throws DiagnosticVersionConflictException if the diagnostic is no longer at
     *                                            {@code expectedVersion}
     */
    Promise<Diagnostic> update(DiagnosticAddress at, String diagnosticId, long expectedVersion, DiagnosticEdit edit);

    /**
     * Marks a finding as seen and accepted for now; it still applies.
     *
     * @see #update
     */
    default Promise<Diagnostic> acknowledge(DiagnosticAddress at, String diagnosticId, long expectedVersion,
            String changedBy, String reason) {
        return update(at, diagnosticId, expectedVersion,
                DiagnosticEdit.status(DiagnosticStatus.ACKNOWLEDGED, changedBy, reason));
    }

    /**
     * Marks a finding as dealt with or no longer applying; it stays for its history.
     *
     * @see #update
     */
    default Promise<Diagnostic> resolve(DiagnosticAddress at, String diagnosticId, long expectedVersion,
            String changedBy, String reason) {
        return update(at, diagnosticId, expectedVersion,
                DiagnosticEdit.status(DiagnosticStatus.RESOLVED, changedBy, reason));
    }

    /**
     * Raises a finding's severity. Lowering it is an ordinary {@link #update}; this method
     * refuses a severity that is not higher than the current one, so a caller cannot
     * downgrade by accident under a name that promises the opposite.
     *
     * @throws IllegalArgumentException if {@code severity} is not higher than the current one
     * @see #update
     */
    Promise<Diagnostic> escalate(DiagnosticAddress at, String diagnosticId, long expectedVersion,
            DiagnosticSeverity severity, String changedBy, String reason);

    /**
     * Removes a diagnostic, with its children. Its history goes with it, so this is for a
     * finding that should never have been recorded; a finding that was dealt with is
     * {@link #resolve resolved}, not removed.
     *
     * @return {@code true} once removed
     * @throws DiagnosticNotFoundException        if the object or the diagnostic is not
     *                                            there
     * @throws DiagnosticVersionConflictException if the diagnostic is no longer at
     *                                            {@code expectedVersion}
     */
    Promise<Boolean> remove(DiagnosticAddress at, String diagnosticId, long expectedVersion, String changedBy,
            String reason);
}
