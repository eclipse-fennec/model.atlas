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
 * Resolves a scope to its Catalog id and to what may be done with it. The one place that knows
 * whether a Catalog may be written.
 *
 * <h2>Ownership comes from configuration, never from the portal</h2>
 *
 * "Did we create this Catalog?" cannot be answered by reading it — nothing in the DCAT graph
 * records authorship — and guessing wrong is destructive in one direction: a {@code PUT} on a
 * Catalog somebody else owns drops every dataset link it holds, other publishers' included.
 * {@code catalog.adopt} is therefore the whole answer, and no inspection supplements it.
 */
final class CatalogResolver {

    private CatalogResolver() {
    }

    /**
     * @param scope    the scope name
     * @param settings its configuration, or {@link CatalogSettings#none()} when it has none
     * @return the resolution; the derived and configured cases are both {@code OWNED} and differ
     *         only in where the Catalog's attributes come from
     */
    static ResolvedCatalog resolve(String scope, CatalogSettings settings) {
        if (settings == null) {
            return ResolvedCatalog.owned(scope, CatalogSettings.none());
        }
        if (!settings.valid()) {
            // A configuration that cannot be honoured refuses its scope. Falling back to the
            // derived case would answer a request to adopt somebody's Catalog by publishing our
            // own under a different id, which is worse than publishing nothing.
            return ResolvedCatalog.refused(settings.invalidReason());
        }
        // Scope names are already URL path segments in the atlas's own API, so an unconfigured id
        // needs no encoding.
        String id = settings.id() == null ? scope : settings.id();
        return settings.adopt() ? ResolvedCatalog.adopted(id, settings) : ResolvedCatalog.owned(id, settings);
    }
}
