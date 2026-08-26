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

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Which representations a Dataset advertises.
 *
 * <p>
 * The runtime's own list is everything it <em>can</em> serve, which is more than belongs in a
 * catalogue — it includes whatever content types the bound {@code ResourceSet} happens to carry.
 * So the configured allowlist is intersected with it: the allowlist keeps the catalogue
 * meaningful, and the intersection keeps it honest, because a portal must never advertise a
 * format the server would answer 415 for.
 * </p>
 */
final class PublishableMediaTypes {

    private PublishableMediaTypes() {
    }

    /**
     * @param configured the operator's allowlist, in preference order
     * @param supported  what the runtime reports it can serve
     * @return the intersection, in the allowlist's order; empty if they do not overlap
     */
    static Set<String> resolve(String[] configured, List<String> supported) {
        Set<String> runtime = new LinkedHashSet<>();
        if (supported != null) {
            supported.stream().filter(t -> t != null).map(t -> t.trim().toLowerCase()).forEach(runtime::add);
        }
        Set<String> result = new LinkedHashSet<>();
        if (configured == null) {
            return result;
        }
        for (String candidate : configured) {
            if (candidate == null || candidate.isBlank()) {
                continue;
            }
            String normalised = candidate.trim().toLowerCase();
            if (runtime.contains(normalised)) {
                result.add(normalised);
            }
        }
        return result;
    }
}
