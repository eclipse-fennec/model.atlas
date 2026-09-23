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
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
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
     * replacements are {@link #prepare prepared} first; a replacement whose id matches a
     * removed root keeps that root's {@code createdTime}, because it is the same finding
     * seen again, not a new one. The replacements are copied before they are contained,
     * so the caller's instances stay the caller's.
     * </p>
     *
     * @param metadata     the metadata to change in place
     * @param producer     the producer whose roots are replaced
     * @param replacements the producer's new roots; an empty list clears them
     * @param now          the time stamped on new findings
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
            if (before != null && before.getCreatedTime() != null) {
                root.setCreatedTime(before.getCreatedTime());
            }
            metadata.getDiagnostics().add(root);
        }
        return List.copyOf(previous.keySet());
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
