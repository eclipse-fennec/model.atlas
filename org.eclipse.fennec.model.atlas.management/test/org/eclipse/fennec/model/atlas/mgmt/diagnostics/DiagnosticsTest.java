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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticChange;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticStatus;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The stable id rule and the producer-ownership replace of {@link Diagnostics} (issues
 * #291, #292).
 */
class DiagnosticsTest {

    private static final Instant T0 = Instant.parse("2026-09-01T10:00:00Z");
    private static final Instant T1 = Instant.parse("2026-09-23T10:00:00Z");

    @Test
    @DisplayName("The id is derived from producer, code and target, so the same finding gets the same id")
    void idIsDeterministic() {
        String a = Diagnostics.idOf("qvt", "unresolved-import", "line:3");
        String again = Diagnostics.idOf("qvt", "unresolved-import", "line:3");

        assertEquals(a, again);
        assertEquals(32, a.length(), "opaque, URL-safe, fixed length");
        assertTrue(a.matches("[0-9a-f]{32}"), a);
        assertNotEquals(a, Diagnostics.idOf("qvt", "unresolved-import", "line:4"), "another element, another finding");
        assertNotEquals(a, Diagnostics.idOf("qvt", "syntax", "line:3"), "another kind, another finding");
        assertNotEquals(a, Diagnostics.idOf("gdpr", "unresolved-import", "line:3"),
                "two producers can never mint the same id");
        assertEquals(Diagnostics.idOf("p", "c", null), Diagnostics.idOf("p", "c", ""),
                "no target and an empty target both mean the object as a whole");
    }

    @Test
    @DisplayName("A child's id hangs off its parent's id, so the same child under two parents differs")
    void childIdDependsOnParent() {
        String parentA = Diagnostics.idOf("qvt", "compile-failed", null);
        String parentB = Diagnostics.idOf("qvt", "compile-failed", "other");

        assertNotEquals(Diagnostics.childIdOf(parentA, "error", "line:1"),
                Diagnostics.childIdOf(parentB, "error", "line:1"));
        assertNotEquals(Diagnostics.childIdOf(parentA, "error", "line:1"), Diagnostics.idOf("qvt", "error", "line:1"),
                "a child never collides with a root of the same code and target");
    }

    @Test
    @DisplayName("prepare stamps the producer, mints missing ids down the tree and sets createdTime")
    void prepareFillsWhatTheProducerLeftOpen() {
        Diagnostic root = diagnostic("compile-failed", null, DiagnosticSeverity.ERROR, "does not compile");
        Diagnostic child = diagnostic("error", "line:3", DiagnosticSeverity.ERROR, "unresolved import");
        root.getChildren().add(child);

        Diagnostics.prepare("qvt", List.of(root), T0);

        assertEquals("qvt", root.getProducer());
        assertEquals("qvt", child.getProducer(), "children inherit the producer");
        assertEquals(Diagnostics.idOf("qvt", "compile-failed", null), root.getId());
        assertEquals(Diagnostics.childIdOf(root.getId(), "error", "line:3"), child.getId());
        assertEquals(T0, root.getCreatedTime());
        assertEquals(T0, child.getCreatedTime());
    }

    @Test
    @DisplayName("prepare keeps an id and a createdTime the producer already set")
    void prepareRespectsWhatIsThere() {
        Diagnostic root = diagnostic("x", null, DiagnosticSeverity.INFO, "m");
        root.setId("my-own-scheme");
        root.setCreatedTime(T0);

        Diagnostics.prepare("p", List.of(root), T1);

        assertEquals("my-own-scheme", root.getId());
        assertEquals(T0, root.getCreatedTime());
    }

    @Test
    @DisplayName("prepare refuses a node that names another producer, and one without a code")
    void prepareRefusesForeignOrCodelessNodes() {
        Diagnostic foreign = diagnostic("x", null, DiagnosticSeverity.INFO, "m");
        foreign.setProducer("somebody-else");
        assertThrows(IllegalArgumentException.class, () -> Diagnostics.prepare("p", List.of(foreign), T0));

        Diagnostic codeless = diagnostic(null, null, DiagnosticSeverity.INFO, "m");
        assertThrows(IllegalArgumentException.class, () -> Diagnostics.prepare("p", List.of(codeless), T0));
    }

    @Test
    @DisplayName("replaceOwned swaps one producer's roots and leaves every other producer's alone")
    void replaceOwnedIsScopedToTheProducer() {
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        Diagnostic gdpr = diagnostic("personal-data", "//Person/birthDate", DiagnosticSeverity.WARNING, "pii");
        Diagnostics.replaceOwned(metadata, "gdpr", List.of(gdpr), T0);
        Diagnostic qvtOld = diagnostic("compile-failed", null, DiagnosticSeverity.ERROR, "old");
        Diagnostic qvtGone = diagnostic("deprecated-import", null, DiagnosticSeverity.INFO, "gone next time");
        Diagnostics.replaceOwned(metadata, "qvt", List.of(qvtOld, qvtGone), T0);
        assertEquals(3, metadata.getDiagnostics().size());

        Diagnostic qvtNew = diagnostic("compile-failed", null, DiagnosticSeverity.ERROR, "still broken, new text");
        List<String> gone = Diagnostics.replaceOwned(metadata, "qvt", List.of(qvtNew), T1);

        assertEquals(2, metadata.getDiagnostics().size());
        assertEquals(List.of(Diagnostics.idOf("qvt", "deprecated-import", null)), gone,
                "the finding that did not come back is reported");
        Diagnostic keptGdpr = byProducer(metadata, "gdpr");
        assertEquals("pii", keptGdpr.getMessage(), "the other producer's root is untouched");
        Diagnostic replaced = byProducer(metadata, "qvt");
        assertEquals("still broken, new text", replaced.getMessage(), "the same finding carries the new text");
        assertEquals(T0, replaced.getCreatedTime(), "the same finding seen again keeps its createdTime");
        assertNotEquals(qvtNew, replaced, "the caller's instance is copied, not contained");
        assertSame(metadata, replaced.eContainer());
    }

    @Test
    @DisplayName("An empty replacement clears the producer's roots")
    void emptyReplacementClears() {
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        Diagnostics.replaceOwned(metadata, "qvt", List.of(diagnostic("a", null, DiagnosticSeverity.INFO, "m")), T0);
        Diagnostics.replaceOwned(metadata, "gdpr", List.of(diagnostic("b", null, DiagnosticSeverity.INFO, "m")), T0);

        List<String> gone = Diagnostics.replaceOwned(metadata, "qvt", List.of(), T1);

        assertEquals(1, metadata.getDiagnostics().size());
        assertEquals("gdpr", metadata.getDiagnostics().get(0).getProducer());
        assertEquals(1, gone.size());
    }

    @Test
    @DisplayName("A re-validation keeps a status a person set, with the history and version, and refreshes the producer's view")
    void revalidationKeepsAnInformedStatus() {
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        Diagnostics.replaceOwned(metadata, "qvt",
                List.of(diagnostic("compile-failed", null, DiagnosticSeverity.ERROR, "first run")), T0);
        // a person resolved it through the service: status, history entry, version 1
        Diagnostic stored = metadata.getDiagnostics().get(0);
        stored.setStatus(DiagnosticStatus.RESOLVED);
        stored.setVersion(1);
        DiagnosticChange resolution = ManagementFactory.eINSTANCE.createDiagnosticChange();
        resolution.setChangedBy("gdpr.officer");
        resolution.setChangeTime(T1);
        resolution.setOldStatus(DiagnosticStatus.OPEN);
        resolution.setNewStatus(DiagnosticStatus.RESOLVED);
        stored.getHistory().add(resolution);

        // the producer runs again, knows nothing of that, reports the same finding with new text
        Diagnostics.replaceOwned(metadata, "qvt",
                List.of(diagnostic("compile-failed", null, DiagnosticSeverity.ERROR, "second run")), T1);

        Diagnostic again = metadata.getDiagnostics().get(0);
        assertEquals("second run", again.getMessage(), "the producer's view refreshes");
        assertEquals(DiagnosticStatus.RESOLVED, again.getStatus(), "the person's decision stands");
        assertEquals(1, again.getVersion(), "no informed change happened");
        assertEquals(1, again.getHistory().size(), "an unchanged severity leaves no trace");
        assertEquals("gdpr.officer", again.getHistory().get(0).getChangedBy());
        assertEquals(T0, again.getCreatedTime());
    }

    @Test
    @DisplayName("A re-validation that changes the severity records it in the producer's name and bumps the version")
    void revalidationRecordsASeverityChange() {
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        Diagnostics.replaceOwned(metadata, "qvt",
                List.of(diagnostic("deprecated-import", null, DiagnosticSeverity.WARNING, "m")), T0);

        Diagnostics.replaceOwned(metadata, "qvt",
                List.of(diagnostic("deprecated-import", null, DiagnosticSeverity.ERROR, "m")), T1);

        Diagnostic again = metadata.getDiagnostics().get(0);
        assertEquals(DiagnosticSeverity.ERROR, again.getSeverity());
        assertEquals(1, again.getVersion());
        assertEquals(1, again.getHistory().size());
        DiagnosticChange change = again.getHistory().get(0);
        assertEquals("qvt", change.getChangedBy());
        assertEquals(DiagnosticSeverity.WARNING, change.getOldSeverity());
        assertEquals(DiagnosticSeverity.ERROR, change.getNewSeverity());
        assertEquals(T1, change.getChangeTime());
        assertEquals(T1, again.getLastChangeTime());
    }

    @Test
    @DisplayName("An informed change, at a higher version, is taken as it is")
    void informedChangeWins() {
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        Diagnostics.replaceOwned(metadata, "qvt",
                List.of(diagnostic("compile-failed", null, DiagnosticSeverity.ERROR, "m")), T0);
        Diagnostic stored = metadata.getDiagnostics().get(0);
        stored.setStatus(DiagnosticStatus.RESOLVED);
        stored.setVersion(1);

        // the service reopens it: version 2, status OPEN
        Diagnostic edited = diagnostic("compile-failed", null, DiagnosticSeverity.ERROR, "m");
        edited.setStatus(DiagnosticStatus.OPEN);
        edited.setVersion(2);
        Diagnostics.replaceOwned(metadata, "qvt", List.of(edited), T1);

        Diagnostic again = metadata.getDiagnostics().get(0);
        assertEquals(DiagnosticStatus.OPEN, again.getStatus(), "the informed change is not overruled");
        assertEquals(2, again.getVersion());
        assertEquals(T0, again.getCreatedTime(), "createdTime is carried over even so");
    }

    @Test
    @DisplayName("find reaches descendants; diff reports added, changed and removed, and nothing for a no-op")
    void findAndDiff() {
        ObjectMetadata before = ManagementFactory.eINSTANCE.createObjectMetadata();
        Diagnostic root = diagnostic("compile-failed", null, DiagnosticSeverity.ERROR, "m");
        root.getChildren().add(diagnostic("error", "line:1", DiagnosticSeverity.ERROR, "e1"));
        Diagnostic gone = diagnostic("deprecated-import", null, DiagnosticSeverity.INFO, "gone");
        Diagnostics.replaceOwned(before, "qvt", List.of(root, gone), T0);
        String childId = before.getDiagnostics().get(0).getChildren().get(0).getId();
        assertEquals("e1", Diagnostics.find(before, childId).getMessage(), "find reaches a descendant");
        assertEquals(before.getDiagnostics().get(0), Diagnostics.rootOf(Diagnostics.find(before, childId)));

        assertTrue(Diagnostics.diff(before, EcoreUtil.copy(before)).isEmpty(), "a copy differs in nothing");

        ObjectMetadata after = EcoreUtil.copy(before);
        Diagnostics.replaceOwned(after, "qvt", List.of(
                diagnostic("compile-failed", null, DiagnosticSeverity.WARNING, "m"),
                diagnostic("new-finding", null, DiagnosticSeverity.INFO, "new")), T1);
        List<DiagnosticDelta> deltas = Diagnostics.diff(before, after);

        Map<String, String> kinds = deltas.stream().collect(Collectors.toMap(d -> d.id, d -> d.kind));
        assertEquals(DiagnosticDelta.CHANGED, kinds.get(Diagnostics.idOf("qvt", "compile-failed", null)));
        assertEquals(DiagnosticDelta.ADDED, kinds.get(Diagnostics.idOf("qvt", "new-finding", null)));
        assertEquals(DiagnosticDelta.REMOVED, kinds.get(Diagnostics.idOf("qvt", "deprecated-import", null)));
        assertEquals(DiagnosticDelta.REMOVED, kinds.get(childId), "the child went with the producer's new tree");
        assertEquals(4, deltas.size());
        DiagnosticDelta changed = deltas.stream().filter(d -> DiagnosticDelta.CHANGED.equals(d.kind)).findFirst()
                .orElseThrow();
        assertEquals("ERROR", changed.before.severity);
        assertEquals("WARNING", changed.after.severity);
        assertEquals("qvt", changed.after.changedBy, "the re-validation's history entry names the producer");
    }

    private static Diagnostic byProducer(ObjectMetadata metadata, String producer) {
        return metadata.getDiagnostics().stream().filter(d -> producer.equals(d.getProducer())).findFirst()
                .orElseThrow();
    }

    static Diagnostic diagnostic(String code, String target, DiagnosticSeverity severity, String message) {
        Diagnostic diagnostic = ManagementFactory.eINSTANCE.createDiagnostic();
        diagnostic.setCode(code);
        diagnostic.setTarget(target);
        diagnostic.setSeverity(severity);
        diagnostic.setMessage(message);
        return diagnostic;
    }
}
