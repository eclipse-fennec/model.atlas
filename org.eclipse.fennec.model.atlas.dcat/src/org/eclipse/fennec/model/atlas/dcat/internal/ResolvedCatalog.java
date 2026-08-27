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
package org.eclipse.fennec.model.atlas.dcat.internal;

/**
 * What one scope's Catalog is: which id, and whether this atlas may write it.
 *
 * @param id             the portal id, or {@code null} when the scope is refused
 * @param ownership      what may be done to it
 * @param settings       the configuration behind it; {@link CatalogSettings#none()} for the derived
 *                       case, so callers never null-check
 * @param refusalReason  why the scope is refused, or {@code null}
 */
record ResolvedCatalog(String id, Ownership ownership, CatalogSettings settings, String refusalReason) {

    /** What this atlas may do to a scope's Catalog. */
    enum Ownership {

        /** Ours: written, reconciled, and deletable. Derived and configured both land here. */
        OWNED,

        /**
         * Somebody else's. Only {@code linkDatasetToCatalog} and {@code unlinkDatasetFromCatalog}
         * are permitted: they are additive, where a {@code PUT} replaces and a {@code CASCADE}
         * delete reaches other publishers' Datasets.
         */
        ADOPTED,

        /** Not publishable at all — a configuration error, reported rather than worked around. */
        REFUSED
    }

    static ResolvedCatalog owned(String id, CatalogSettings settings) {
        return new ResolvedCatalog(id, Ownership.OWNED, settings, null);
    }

    static ResolvedCatalog adopted(String id, CatalogSettings settings) {
        return new ResolvedCatalog(id, Ownership.ADOPTED, settings, null);
    }

    static ResolvedCatalog refused(String reason) {
        return new ResolvedCatalog(null, Ownership.REFUSED, CatalogSettings.none(), reason);
    }

    boolean owned() {
        return ownership == Ownership.OWNED;
    }

    boolean adopted() {
        return ownership == Ownership.ADOPTED;
    }

    boolean refused() {
        return ownership == Ownership.REFUSED;
    }
}
