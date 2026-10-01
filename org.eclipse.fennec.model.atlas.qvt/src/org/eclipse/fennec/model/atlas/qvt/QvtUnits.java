/**
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
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.qvt;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

import org.eclipse.fennec.m2x.model.compiled.CompiledUnit;
import org.eclipse.fennec.m2x.model.compiled.CompiledUnitManifest;
import org.eclipse.fennec.m2x.model.compiled.UnitNature;
import org.eclipse.fennec.m2x.unit.api.UnitKey;
import org.eclipse.fennec.m2x.unit.api.UnitKind;

/**
 * How units live in the transformation registry: the id scheme and the
 * unit-nature heuristic.
 *
 * <p>
 * The logical address of an entry is the emf.m2x object-medium entry key
 * {@code <language>/<kind>/<qualifiedName>/<fingerprint>} (see
 * {@code RegistryUnitStore}, eclipse-fennec/emf.m2x#213). An Atlas objectId
 * must be a single file-system-safe path segment (the file backend rejects
 * separators and characters like {@code :}), so the stored objectId is the
 * URL-encoded form of that entry key; {@link #objectId(UnitKey)} and
 * {@link #parseObjectId(String)} convert between the two.
 * </p>
 */
public final class QvtUnits {

    /** The m2x language tag for QVT-O units. */
    public static final String LANGUAGE_QVTO = "qvto";

    private QvtUnits() {
    }

    /** The logical entry key of a pinned unit key, per the object-medium scheme. */
    public static String entryKey(UnitKey key) {
        return key.language() + "/" + key.kind().tag() + "/" + key.qualifiedName() + "/"
                + key.fingerprint().orElseThrow(() -> new IllegalArgumentException(
                        "an entry key requires a pinned fingerprint: " + key));
    }

    /** The Atlas objectId of a pinned unit key: the URL-encoded entry key. */
    public static String objectId(UnitKey key) {
        return encode(entryKey(key));
    }

    /**
     * The pinned unit key an Atlas objectId denotes, or empty for an id that is
     * no unit entry - foreign content sharing the registry, or a
     * {@code qvto/diagnostics/<name>} document left behind by a runtime from before issue #327,
     * which this answers empty for without needing to know what it was.
     */
    public static Optional<UnitKey> parseObjectId(String objectId) {
        String decoded;
        try {
            decoded = decode(objectId);
        } catch (IllegalArgumentException e) {
            // a foreign id that merely looks percent-encoded (e.g. "100%") — such an
            // entry is no unit and must not poison scans over the whole registry
            return Optional.empty();
        }
        String[] parts = decoded.split("/");
        if (parts.length < 4) {
            return Optional.empty();
        }
        UnitKind kind;
        if (UnitKind.SOURCE.tag().equals(parts[1])) {
            kind = UnitKind.SOURCE;
        } else if (UnitKind.COMPILED.tag().equals(parts[1])) {
            kind = UnitKind.COMPILED;
        } else {
            return Optional.empty();
        }
        // the fingerprint is the last segment; the qualified name is everything in
        // between (QVT qualified names use '.', never '/', so this is defensive)
        String fingerprint = parts[parts.length - 1];
        String qualifiedName = String.join("/", java.util.Arrays.copyOfRange(parts, 2, parts.length - 1));
        return Optional.of(UnitKey.pinned(parts[0], qualifiedName, kind, fingerprint));
    }

    public static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    public static String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    /**
     * Whether a compiled unit is a library rather than a startable transformation.
     * <p>
     * Read off {@code CompiledUnitManifest.nature}, which the compiling language stamps at package
     * time (eclipse-fennec/emf.m2x#224). This used to mirror the m2x linker's unwrap logic -
     * a standalone library source parses into a synthetic transformation wrapper - and that mirror
     * is what the upstream change exists to remove: the question is answered once, where the
     * compiler knows, and never again by a consumer reproducing the heuristic. It is also now
     * answerable without walking the AST at all.
     */
    public static boolean isLibrary(CompiledUnit unit) {
        CompiledUnitManifest manifest = unit.getManifest();
        return manifest != null && UnitNature.LIBRARY == manifest.getNature();
    }
}
