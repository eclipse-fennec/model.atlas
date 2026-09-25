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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The answer of a {@link StageGate}: the operation may proceed, or it is
 * refused, and either way the gate may explain itself with
 * {@link GateDiagnostic diagnostics}.
 *
 * <p>
 * A refusal always has at least one diagnostic (issue #294): the workflow
 * records the diagnostics of a refused operation on the object in its source
 * stage, so the refusal is visible on the object afterwards and not only in
 * the response, and hands them to the caller in the exception. A gate that
 * refuses with a plain {@link #refuse(String) reason} gets one
 * {@link GateSeverity#ERROR} diagnostic made from it, coded
 * {@link GateDiagnostic#REFUSED}. A gate that passes may still report
 * {@link #pass(List) findings}; on a transition they travel with the object
 * into the target stage.
 * </p>
 *
 * <p>
 * The {@link #reason()} travels to the client unchanged, so it should say what
 * was checked, what failed and, where there is one, what the caller can do
 * about it ("transition the libraries it imports first"). It must not leak
 * internal detail such as storage paths. When a refusal gives diagnostics but
 * no reason, the reason is made from the messages of its error diagnostics.
 * </p>
 *
 * @param refused     {@code true} if the gate vetoes the operation
 * @param reason      why it was refused; {@code null} when it passed
 * @param diagnostics the gate's findings; never {@code null}, never empty for a
 *                    refusal
 * @since 1.1
 */
public record GateVerdict(boolean refused, String reason, List<GateDiagnostic> diagnostics) {

    private static final GateVerdict PASS = new GateVerdict(false, null, List.of());

    public GateVerdict {
        diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
        if (refused) {
            if (reason == null) {
                reason = summarize(diagnostics);
            }
            if (reason == null) {
                throw new IllegalArgumentException("a refusal needs a reason or an error diagnostic");
            }
            if (diagnostics.isEmpty()) {
                diagnostics = List.of(GateDiagnostic.error(GateDiagnostic.REFUSED, reason));
            }
        }
    }

    /**
     * The shape of 1.1: a verdict without diagnostics. A refusal made this way
     * gets its one diagnostic from the reason.
     *
     * @param refused {@code true} if the gate vetoes the operation
     * @param reason  why it was refused; {@code null} when it passed
     */
    public GateVerdict(boolean refused, String reason) {
        this(refused, reason, null);
    }

    /**
     * @return the verdict that lets the operation proceed
     */
    public static GateVerdict pass() {
        return PASS;
    }

    /**
     * @param diagnostics what the gate found without refusing: warnings and
     *                    information about the object, or consequences for its
     *                    {@link Dependent dependents}
     * @return a passing verdict carrying the findings
     * @since 1.2
     */
    public static GateVerdict pass(List<GateDiagnostic> diagnostics) {
        return diagnostics == null || diagnostics.isEmpty() ? PASS : new GateVerdict(false, null, diagnostics);
    }

    /**
     * @param reason why the operation is refused, written for the caller
     * @return a refusing verdict with one diagnostic made from the reason
     */
    public static GateVerdict refuse(String reason) {
        return new GateVerdict(true, Objects.requireNonNull(reason, "a refusal needs a reason"), null);
    }

    /**
     * @param reason      why the operation is refused, written for the caller
     * @param diagnostics the findings behind it
     * @return a refusing verdict
     * @since 1.2
     */
    public static GateVerdict refuse(String reason, List<GateDiagnostic> diagnostics) {
        return new GateVerdict(true, Objects.requireNonNull(reason, "a refusal needs a reason"), diagnostics);
    }

    /**
     * @param diagnostics the findings behind the refusal; at least one of them an
     *                    {@link GateSeverity#ERROR}, whose messages become the
     *                    reason
     * @return a refusing verdict
     * @since 1.2
     */
    public static GateVerdict refuse(List<GateDiagnostic> diagnostics) {
        return new GateVerdict(true, null, diagnostics);
    }

    /**
     * @return {@code true} if the operation may proceed
     */
    public boolean passed() {
        return !refused;
    }

    /**
     * @return the diagnostics about the object under decision, in order
     * @since 1.2
     */
    public List<GateDiagnostic> findings() {
        List<GateDiagnostic> own = new ArrayList<>();
        for (GateDiagnostic diagnostic : diagnostics) {
            if (diagnostic.isAboutObject()) {
                own.add(diagnostic);
            }
        }
        return own;
    }

    /**
     * @return the diagnostics about other objects, grouped by the
     *         {@link Dependent} they are about, in order of first mention
     * @since 1.2
     */
    public Map<Dependent, List<GateDiagnostic>> consequences() {
        Map<Dependent, List<GateDiagnostic>> byDependent = new LinkedHashMap<>();
        for (GateDiagnostic diagnostic : diagnostics) {
            if (!diagnostic.isAboutObject()) {
                byDependent.computeIfAbsent(diagnostic.dependent(), d -> new ArrayList<>()).add(diagnostic);
            }
        }
        return byDependent;
    }

    private static String summarize(List<GateDiagnostic> diagnostics) {
        String summary = diagnostics.stream().filter(GateDiagnostic::isError).map(GateDiagnostic::message)
                .collect(Collectors.joining("; "));
        return summary.isEmpty() ? null : summary;
    }
}
