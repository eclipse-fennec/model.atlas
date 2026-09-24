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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * One finding of a {@link StageGate}, in the shape the gate hands it over.
 *
 * <p>
 * This is the gate's side of the Atlas diagnostics (issue #294). It carries
 * what a gate knows - how serious the finding is, a code the gate defines, a
 * message for the caller, optionally the element inside the object it is about
 * and finer findings beneath it - and none of what only the persisted record
 * has: no id, no status, no history. The workflow turns it into a persisted
 * diagnostic when it records the verdict, and mints the id from the gate's
 * {@link StageGate#producer() producer}, the {@link #code()} and the
 * {@link #target()}. The same finding therefore gets the same id every time the
 * gate reports it, which is what lets a client keep referring to it across
 * re-validations. Choose the code and the target so that two different
 * findings never share both.
 * </p>
 *
 * <p>
 * A finding is about the object under decision unless it names a
 * {@link #dependent()}: then it is about that other object and is written
 * there, once the operation goes through. Deliberately free of EMF, like the
 * rest of this SPI.
 * </p>
 *
 * @param severity  how serious the finding is; never {@code null}
 * @param code      a code the gate defines, stable across runs; never blank
 * @param message   what was found, written for the caller; never {@code null}
 * @param category  a grouping the gate defines, for example {@code "compile"},
 *                  or {@code null}
 * @param target    the element inside the object the finding is about, in a
 *                  notation the gate defines, or {@code null} for the object as
 *                  a whole
 * @param dependent the other object this finding is about, or {@code null}
 *                  when it is about the object under decision
 * @param children  finer findings beneath this one; never {@code null}, may be
 *                  empty
 * @since 1.2
 */
public record GateDiagnostic(
        GateSeverity severity,
        String code,
        String message,
        String category,
        String target,
        Dependent dependent,
        List<GateDiagnostic> children) {

    /**
     * The code of the diagnostic {@link GateVerdict#refuse(String)} makes from a
     * plain reason, so a gate that has not adopted diagnostics still records its
     * veto.
     */
    public static final String REFUSED = "refused";

    public GateDiagnostic {
        Objects.requireNonNull(severity, "a finding has a severity");
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("a finding needs a code; its id is derived from it");
        }
        Objects.requireNonNull(message, "a finding has a message");
        children = children == null ? List.of() : List.copyOf(children);
    }

    /**
     * @param code    the gate's code for the finding
     * @param message what was found
     * @return an {@link GateSeverity#ERROR} about the object as a whole
     */
    public static GateDiagnostic error(String code, String message) {
        return new GateDiagnostic(GateSeverity.ERROR, code, message, null, null, null, List.of());
    }

    /**
     * @param code    the gate's code for the finding
     * @param message what was found
     * @return a {@link GateSeverity#WARNING} about the object as a whole
     */
    public static GateDiagnostic warning(String code, String message) {
        return new GateDiagnostic(GateSeverity.WARNING, code, message, null, null, null, List.of());
    }

    /**
     * @param code    the gate's code for the finding
     * @param message what was found
     * @return an {@link GateSeverity#INFO} about the object as a whole
     */
    public static GateDiagnostic info(String code, String message) {
        return new GateDiagnostic(GateSeverity.INFO, code, message, null, null, null, List.of());
    }

    /**
     * @param category the grouping the gate files this finding under
     * @return the same finding in that category
     */
    public GateDiagnostic inCategory(String category) {
        return new GateDiagnostic(severity, code, message, category, target, dependent, children);
    }

    /**
     * @param target the element inside the object the finding is about
     * @return the same finding, about that element
     */
    public GateDiagnostic at(String target) {
        return new GateDiagnostic(severity, code, message, category, target, dependent, children);
    }

    /**
     * @param dependent the other object the finding is about
     * @return the same finding, about that object
     */
    public GateDiagnostic on(Dependent dependent) {
        return new GateDiagnostic(severity, code, message, category, target, dependent, children);
    }

    /**
     * @param children finer findings beneath this one
     * @return the same finding with those children
     */
    public GateDiagnostic withChildren(List<GateDiagnostic> children) {
        return new GateDiagnostic(severity, code, message, category, target, dependent, children);
    }

    /**
     * @param child a finer finding beneath this one
     * @return the same finding with the child appended
     */
    public GateDiagnostic withChild(GateDiagnostic child) {
        List<GateDiagnostic> more = new ArrayList<>(children);
        more.add(Objects.requireNonNull(child, "child"));
        return new GateDiagnostic(severity, code, message, category, target, dependent, more);
    }

    /**
     * @return {@code true} if this finding is an {@link GateSeverity#ERROR}
     */
    public boolean isError() {
        return severity == GateSeverity.ERROR;
    }

    /**
     * @return {@code true} if this finding is about the object under decision
     *         rather than about a {@link #dependent()}
     */
    public boolean isAboutObject() {
        return dependent == null;
    }
}
