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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the shape a {@link GateVerdict} guarantees the workflow
 * (issue #294): a refusal always explains itself with at least one
 * diagnostic, and the findings about the object are told apart from the
 * consequences for its dependents.
 */
@DisplayName("GateVerdict")
class GateVerdictTest {

    @Test
    @DisplayName("A plain refusal gets one error diagnostic made from its reason")
    void plainRefusalCarriesOneDiagnostic() {
        GateVerdict verdict = GateVerdict.refuse("the source does not compile");

        assertTrue(verdict.refused());
        assertEquals("the source does not compile", verdict.reason());
        assertEquals(1, verdict.diagnostics().size());
        GateDiagnostic made = verdict.diagnostics().get(0);
        assertEquals(GateDiagnostic.REFUSED, made.code());
        assertEquals(GateSeverity.ERROR, made.severity());
        assertEquals("the source does not compile", made.message());
        assertTrue(made.isAboutObject());
    }

    @Test
    @DisplayName("The 1.1 constructor still works and behaves like refuse(String)")
    void legacyConstructorKeepsWorking() {
        GateVerdict verdict = new GateVerdict(true, "no");

        assertEquals("no", verdict.reason());
        assertEquals(1, verdict.diagnostics().size());
        assertEquals(GateDiagnostic.REFUSED, verdict.diagnostics().get(0).code());

        GateVerdict passed = new GateVerdict(false, null);
        assertTrue(passed.passed());
        assertTrue(passed.diagnostics().isEmpty());
    }

    @Test
    @DisplayName("A refusal without a reason takes it from its error diagnostics")
    void reasonIsSummarizedFromErrors() {
        GateVerdict verdict = GateVerdict.refuse(List.of(
                GateDiagnostic.error("a", "first problem"),
                GateDiagnostic.warning("b", "not part of the reason"),
                GateDiagnostic.error("c", "second problem")));

        assertEquals("first problem; second problem", verdict.reason());
        assertEquals(3, verdict.diagnostics().size(), "the warning is kept as a finding");
    }

    @Test
    @DisplayName("A refusal with neither reason nor error diagnostic is rejected")
    void refusalNeedsAReasonOrAnError() {
        assertThrows(IllegalArgumentException.class,
                () -> GateVerdict.refuse(List.of(GateDiagnostic.warning("w", "only a warning"))));
        assertThrows(IllegalArgumentException.class, () -> GateVerdict.refuse(List.of()));
    }

    @Test
    @DisplayName("A pass may carry findings, and an empty pass is the shared constant")
    void passWithAndWithoutFindings() {
        assertSame(GateVerdict.pass(), GateVerdict.pass(List.of()));
        assertSame(GateVerdict.pass(), GateVerdict.pass(null));

        GateVerdict warned = GateVerdict.pass(List.of(GateDiagnostic.warning("w", "look at this")));
        assertTrue(warned.passed());
        assertNull(warned.reason());
        assertEquals(1, warned.findings().size());
    }

    @Test
    @DisplayName("Findings about the object and consequences for dependents are told apart")
    void findingsAndConsequencesAreSeparated() {
        Dependent first = Dependent.inSameRegistry("draft", "instance-1");
        Dependent second = new Dependent("instances", "release", "instance-2");
        GateVerdict verdict = GateVerdict.refuse("dependents exist", List.of(
                GateDiagnostic.error("dependents", "2 instances depend on this schema"),
                GateDiagnostic.warning("schema-gone", "your schema is about to go").on(first),
                GateDiagnostic.warning("schema-gone", "your schema is about to go").on(second),
                GateDiagnostic.info("also", "a second note for the first one").on(first)));

        assertEquals(1, verdict.findings().size());
        assertEquals("dependents", verdict.findings().get(0).code());

        Map<Dependent, List<GateDiagnostic>> consequences = verdict.consequences();
        assertEquals(List.of(first, second), List.copyOf(consequences.keySet()), "grouped in order of first mention");
        assertEquals(2, consequences.get(first).size());
        assertEquals(1, consequences.get(second).size());
        assertNull(first.registry(), "same registry is expressed as null");
        assertEquals("instances", second.registry());
    }

    @Test
    @DisplayName("A diagnostic is immutable and its builders return new instances")
    void diagnosticBuilders() {
        GateDiagnostic root = GateDiagnostic.error("compile", "does not compile");
        GateDiagnostic placed = root.at("Lib.qvto").inCategory("compile")
                .withChild(GateDiagnostic.error("finding", "unresolved import").at("3:7"));

        assertNull(root.target(), "the original is untouched");
        assertTrue(root.children().isEmpty());
        assertEquals("Lib.qvto", placed.target());
        assertEquals("compile", placed.category());
        assertEquals(1, placed.children().size());
        assertEquals("3:7", placed.children().get(0).target());
        assertThrows(UnsupportedOperationException.class, () -> placed.children().add(root));
        assertFalse(placed.withChildren(null).children().contains(root));
    }

    @Test
    @DisplayName("A diagnostic needs a severity, a code and a message")
    void diagnosticRequiresItsEssentials() {
        assertThrows(NullPointerException.class,
                () -> new GateDiagnostic(null, "code", "message", null, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> GateDiagnostic.error(" ", "message"));
        assertThrows(NullPointerException.class, () -> GateDiagnostic.error("code", null));
    }
}
