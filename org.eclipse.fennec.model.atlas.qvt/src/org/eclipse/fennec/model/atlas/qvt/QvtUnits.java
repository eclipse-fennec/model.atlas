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

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.m2x.model.compiled.CompiledUnit;
import org.eclipse.fennec.m2x.model.qvtoperational.Library;
import org.eclipse.fennec.m2x.model.qvtoperational.Module;
import org.eclipse.fennec.m2x.model.qvtoperational.ModuleImport;
import org.eclipse.fennec.m2x.model.qvtoperational.OperationalTransformation;
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

    /**
     * Pseudo-kind segment for the per-source diagnostics document — an Atlas
     * convention outside the m2x {@code UnitKind} scheme (which knows only
     * {@code source} and {@code compiled}); the store contract tolerates
     * foreign entries, and {@link #parseObjectId(String)} answers empty for it.
     */
    public static final String DIAGNOSTICS_KIND = "diagnostics";

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

    /** The Atlas objectId of the diagnostics document for one source. */
    public static String diagnosticsObjectId(String language, String qualifiedName) {
        return encode(language + "/" + DIAGNOSTICS_KIND + "/" + qualifiedName);
    }

    /**
     * The pinned unit key an Atlas objectId denotes, or empty for an id that is
     * no unit entry (a diagnostics document, or foreign content sharing the
     * registry).
     */
    public static Optional<UnitKey> parseObjectId(String objectId) {
        String decoded = decode(objectId);
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
     * Whether a compiled unit is a library rather than a startable
     * transformation. Mirrors the (package-private) unwrap logic of the m2x
     * linker: a standalone library source parses into a synthetic
     * {@code OperationalTransformation} wrapper that has no module class of its
     * own but imports a {@code Library} that has one. Tracked upstream as
     * eclipse-fennec/emf.m2x#224 (asking for a first-class marker); replace this
     * heuristic once that lands.
     */
    public static boolean isLibrary(CompiledUnit unit) {
        EObject root = unit.getUnit();
        if (!(root instanceof OperationalTransformation transformation)) {
            // QVT-O compile always yields an OperationalTransformation root today;
            // anything else is not startable by this action
            return true;
        }
        if (findModuleClassIn(transformation) != null) {
            return false;
        }
        for (ModuleImport moduleImport : transformation.getModuleImport()) {
            if (moduleImport.getImportedModule() instanceof Library library && findModuleClassIn(library) != null) {
                return true;
            }
        }
        return false;
    }

    private static EClass findModuleClassIn(Module module) {
        String name = module.getName();
        if (name == null) {
            return null;
        }
        return module.getEClassifiers().stream()
                .filter(EClass.class::isInstance)
                .map(EClass.class::cast)
                .filter(c -> name.equals(c.getName()))
                .findFirst()
                .orElse(null);
    }
}
