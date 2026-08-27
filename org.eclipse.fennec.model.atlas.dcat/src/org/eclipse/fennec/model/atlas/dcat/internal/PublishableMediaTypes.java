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

import java.util.Collection;
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
    /**
     * Whether a published Dataset advertises something other than what the allowlist now resolves
     * to — which is how a narrowed {@code distribution.media.types} reaches an already-published
     * Dataset at all. Its content has not changed, so the fingerprint check would skip the write
     * and the catalogue would go on offering a format the configuration no longer permits.
     *
     * <p>
     * An <strong>empty</strong> {@code resolved} is never out of date. Empty means the runtime
     * currently reports none of the allowed formats, which happens while codecs are still coming up
     * — a runtime condition, not an instruction to strip a catalogue entry. Acting on it would
     * remove every Distribution during startup and leave nothing to put them back.
     * </p>
     *
     * @param resolved  what {@link #resolve} answers now
     * @param published the media types the portal's Distributions carry
     * @return {@code true} when the Dataset has to be rewritten
     */
    static boolean distributionsOutOfDate(Set<String> resolved, Collection<String> published) {
        if (resolved == null || resolved.isEmpty()) {
            return false;
        }
        Set<String> advertised = new LinkedHashSet<>();
        if (published != null) {
            published.stream().filter(t -> t != null).map(t -> t.trim().toLowerCase()).forEach(advertised::add);
        }
        return !advertised.equals(resolved);
    }

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
