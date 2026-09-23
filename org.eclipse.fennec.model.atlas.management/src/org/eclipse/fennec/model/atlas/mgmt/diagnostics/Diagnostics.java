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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticChange;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;

/**
 * The rules shared by everything that writes {@link Diagnostic diagnostics} onto an
 * {@link ObjectMetadata} (issues #291, #292).
 *
 * <p>
 * <b>Stable id.</b> A diagnostic's id is derived, not drawn: from its producer, its code
 * and its target, and for a child from its parent's id, its code and its target. The same
 * finding about the same element therefore has the same id on every validation run, so a
 * reference from outside survives a re-validation, and replacing a producer's diagnostics
 * is a plain replace by id. The id is the hex SHA-256 of the three parts joined by a unit
 * separator, cut to 32 characters: opaque, URL-safe and collision-free for all practical
 * purposes. Two findings of the same kind about the same element collapse into one; a
 * producer that needs to tell them apart puts the distinction into the target or the code.
 * </p>
 *
 * <p>
 * <b>Producer ownership.</b> An object's diagnostics are the union of what its producers
 * have said about it. A producer only ever replaces its own roots; the roots of every other
 * producer stay untouched. Children belong to their root and carry its producer.
 * </p>
 */
public final class Diagnostics {

    /** Joins the parts an id is derived from; it cannot occur in a code or a target by accident. */
    private static final String UNIT_SEPARATOR = "";
    private static final int ID_LENGTH = 32;

    private Diagnostics() {
    }

    /**
     * Derives the id of a root diagnostic.
     *
     * @param producer the producer, never {@code null}
     * @param code     the producer's code for the kind of finding, never {@code null}
     * @param target   the affected element, or {@code null} for the object as a whole
     * @return the stable id
     */
    public static String idOf(String producer, String code, String target) {
        return digest(requireNonNull(producer, "producer"), requireNonNull(code, "code"), target);
    }

    /**
     * Derives the id of a child diagnostic.
     *
     * @param parentId the id of the diagnostic it refines, never {@code null}
     * @param code     the code of the child, never {@code null}
     * @param target   the affected element, or {@code null}
     * @return the stable id
     */
    public static String childIdOf(String parentId, String code, String target) {
        return digest(requireNonNull(parentId, "parentId"), requireNonNull(code, "code"), target);
    }

    /**
     * Makes a producer's diagnostics ready to be stored: stamps the producer onto every
     * node, mints missing ids by the stable id rule and sets {@code createdTime} where it
     * is unset. A node that already carries an id keeps it, so a producer may mint ids by
     * its own scheme as long as they are stable; a node that carries a <em>different</em>
     * producer is refused, because it would let one producer overwrite another's findings.
     *
     * @param producer    the producer that owns the diagnostics
     * @param diagnostics the roots to prepare; modified in place
     * @param now         the time to stamp on nodes without a {@code createdTime}
     * @throws IllegalArgumentException if a node names another producer or lacks a code
     */
    public static void prepare(String producer, List<Diagnostic> diagnostics, Instant now) {
        requireNonNull(producer, "producer");
        for (Diagnostic root : diagnostics) {
            prepareNode(producer, root, null, now);
        }
    }

    private static void prepareNode(String producer, Diagnostic node, String parentId, Instant now) {
        if (node.getProducer() != null && !producer.equals(node.getProducer())) {
            throw new IllegalArgumentException(String.format(
                    "Diagnostic '%s' names producer '%s', but is being written on behalf of producer '%s'",
                    node.getCode(), node.getProducer(), producer));
        }
        if (node.getCode() == null || node.getCode().isBlank()) {
            throw new IllegalArgumentException("A diagnostic needs a code; its id is derived from it");
        }
        node.setProducer(producer);
        if (node.getId() == null || node.getId().isBlank()) {
            node.setId(parentId == null ? idOf(producer, node.getCode(), node.getTarget())
                    : childIdOf(parentId, node.getCode(), node.getTarget()));
        }
        if (node.getCreatedTime() == null) {
            node.setCreatedTime(now);
        }
        for (Diagnostic child : node.getChildren()) {
            prepareNode(producer, child, node.getId(), now);
        }
    }

    /**
     * Replaces the roots one producer holds on the given metadata with the given ones.
     *
     * <p>
     * The producer's current roots are removed, every other producer's roots stay. The
     * replacements are {@link #prepare prepared} first and copied before they are
     * contained, so the caller's instances stay the caller's.
     * </p>
     *
     * <p>
     * <b>The same finding seen again.</b> A replacement whose id matches a removed root is
     * the same finding, not a new one, and the finding has a life the producer does not
     * know about: somebody may have acknowledged or resolved it, and the history says so.
     * Which side wins is decided by the {@code version}, the count of informed changes:
     * </p>
     * <ul>
     * <li>A replacement at a <em>higher</em> version than its predecessor is an informed
     * change, made by somebody who saw the predecessor (the {@link DiagnosticService}
     * works this way). It is taken as it is.</li>
     * <li>Any other replacement is a re-validation: the producer found the same thing
     * again and knows nothing of what happened since. It contributes what it can judge
     * (severity, message, target, category, source, children) and inherits the rest from
     * its predecessor: {@code createdTime}, {@code status}, {@code history} and
     * {@code version}. So a status a person set is never silently reverted by an automatic
     * run (issue #293). A severity that differs from the predecessor's is recorded as a
     * history entry in the producer's name and bumps the version; an unchanged finding
     * leaves no trace.</li>
     * </ul>
     *
     * @param metadata     the metadata to change in place
     * @param producer     the producer whose roots are replaced
     * @param replacements the producer's new roots; an empty list clears them
     * @param now          the time stamped on new findings and on history entries
     * @return the ids of the roots that were removed and did not come back
     */
    public static List<String> replaceOwned(ObjectMetadata metadata, String producer, List<Diagnostic> replacements,
            Instant now) {
        requireNonNull(metadata, "metadata");
        requireNonNull(producer, "producer");
        List<Diagnostic> incoming = new ArrayList<>();
        for (Diagnostic replacement : replacements) {
            incoming.add(EcoreUtil.copy(replacement));
        }
        prepare(producer, incoming, now);

        Map<String, Diagnostic> previous = new HashMap<>();
        metadata.getDiagnostics().removeIf(root -> {
            if (!producer.equals(root.getProducer())) {
                return false;
            }
            previous.put(root.getId(), root);
            return true;
        });
        for (Diagnostic root : incoming) {
            Diagnostic before = previous.remove(root.getId());
            if (before != null) {
                carryOver(before, root, producer, now);
            }
            metadata.getDiagnostics().add(root);
        }
        return List.copyOf(previous.keySet());
    }

    private static void carryOver(Diagnostic before, Diagnostic again, String producer, Instant now) {
        if (before.getCreatedTime() != null) {
            again.setCreatedTime(before.getCreatedTime());
        }
        if (again.getVersion() > before.getVersion()) {
            // an informed change: the caller saw the predecessor and means every field
            return;
        }
        // a re-validation: the finding's life continues, the producer's view refreshes
        DiagnosticSeverity newSeverity = again.getSeverity();
        again.setStatus(before.getStatus());
        again.setVersion(before.getVersion());
        again.setLastChangeTime(before.getLastChangeTime());
        again.getHistory().clear();
        for (DiagnosticChange entry : before.getHistory()) {
            again.getHistory().add(EcoreUtil.copy(entry));
        }
        if (newSeverity != before.getSeverity()) {
            DiagnosticChange change = ManagementFactory.eINSTANCE.createDiagnosticChange();
            change.setChangeTime(now);
            change.setChangedBy(producer);
            change.setOldSeverity(before.getSeverity());
            change.setNewSeverity(newSeverity);
            change.setOldStatus(before.getStatus());
            change.setNewStatus(before.getStatus());
            change.setReason("re-validation");
            again.getHistory().add(change);
            again.setVersion(before.getVersion() + 1);
            again.setLastChangeTime(now);
        }
    }

    /**
     * Finds a diagnostic by id anywhere in the metadata's trees.
     *
     * @return the diagnostic, or {@code null}
     */
    public static Diagnostic find(ObjectMetadata metadata, String id) {
        requireNonNull(id, "id");
        for (Diagnostic root : metadata.getDiagnostics()) {
            Diagnostic found = find(root, id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    private static Diagnostic find(Diagnostic node, String id) {
        if (id.equals(node.getId())) {
            return node;
        }
        for (Diagnostic child : node.getChildren()) {
            Diagnostic found = find(child, id);
            if (found != null) {
                return found;
            }
        }
        return null;
    }

    /**
     * The root a diagnostic belongs to: itself for a root, else the top of its tree.
     */
    public static Diagnostic rootOf(Diagnostic diagnostic) {
        Diagnostic node = diagnostic;
        while (node.eContainer() instanceof Diagnostic parent) {
            node = parent;
        }
        return node;
    }

    /**
     * Every diagnostic in the metadata's trees, roots and descendants, keyed by id.
     */
    public static Map<String, Diagnostic> flatten(ObjectMetadata metadata) {
        Map<String, Diagnostic> all = new LinkedHashMap<>();
        if (metadata != null) {
            for (Diagnostic root : metadata.getDiagnostics()) {
                flatten(root, all);
            }
        }
        return all;
    }

    private static void flatten(Diagnostic node, Map<String, Diagnostic> all) {
        all.put(node.getId(), node);
        for (Diagnostic child : node.getChildren()) {
            flatten(child, all);
        }
    }

    /**
     * What looks different between two states of an object's diagnostics: the deltas a
     * {@link DiagnosticsChanged} event carries. Empty when nothing a reader would notice
     * changed, which is what lets a write that changed nothing deliver no event.
     *
     * @param before the metadata before the write; {@code null} counts as "no diagnostics"
     * @param after  the metadata after the write
     * @return the deltas, in the order of {@code after} then the removals
     */
    public static List<DiagnosticDelta> diff(ObjectMetadata before, ObjectMetadata after) {
        Map<String, Diagnostic> was = flatten(before);
        Map<String, Diagnostic> is = flatten(after);
        List<DiagnosticDelta> deltas = new ArrayList<>();
        for (Map.Entry<String, Diagnostic> entry : is.entrySet()) {
            Diagnostic previous = was.remove(entry.getKey());
            DiagnosticState now = DiagnosticState.of(entry.getValue());
            if (previous == null) {
                deltas.add(delta(entry.getKey(), DiagnosticDelta.ADDED, null, now));
                continue;
            }
            DiagnosticState then = DiagnosticState.of(previous);
            if (DiagnosticState.differ(then, now)) {
                deltas.add(delta(entry.getKey(), DiagnosticDelta.CHANGED, then, now));
            }
        }
        for (Map.Entry<String, Diagnostic> gone : was.entrySet()) {
            deltas.add(delta(gone.getKey(), DiagnosticDelta.REMOVED, DiagnosticState.of(gone.getValue()), null));
        }
        return deltas;
    }

    private static DiagnosticDelta delta(String id, String kind, DiagnosticState before, DiagnosticState after) {
        DiagnosticDelta delta = new DiagnosticDelta();
        delta.id = id;
        delta.kind = kind;
        delta.before = before;
        delta.after = after;
        return delta;
    }

    private static String digest(String first, String second, String third) {
        String raw = first + UNIT_SEPARATOR + second + UNIT_SEPARATOR + Objects.toString(third, "");
        try {
            byte[] bytes = MessageDigest.getInstance("SHA-256").digest(raw.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(bytes).substring(0, ID_LENGTH);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is mandatory in every Java runtime", e);
        }
    }
}
