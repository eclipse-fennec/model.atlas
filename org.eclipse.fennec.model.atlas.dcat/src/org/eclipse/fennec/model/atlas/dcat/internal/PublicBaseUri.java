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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

/**
 * Validates and normalises the atlas's public base URI.
 *
 * <p>
 * The publisher has no request to derive a host from — it acts on a DS bind — and an APISIX
 * terminates the public URL, so the configured value is the <em>whole</em> public prefix, up to
 * and including whatever stands in for the container's own {@code /atlas/rest}. A gateway
 * rewrites paths as readily as hosts, so appending a hard-coded prefix to a host-only base would
 * produce URLs that 404 for every harvester.
 * </p>
 *
 * <p>
 * Nothing here reaches the network. Probing the base at activation fails in exactly the normal
 * deployment: a gateway is frequently unreachable from inside the network it fronts, and
 * split-horizon DNS makes the public name resolve elsewhere or nowhere. Publishing must never
 * depend on the atlas being able to reach its own public address.
 * </p>
 */
final class PublicBaseUri {

    private static final Set<String> LOCAL_HOSTS = Set.of("localhost", "127.0.0.1", "::1", "0.0.0.0");

    private PublicBaseUri() {
    }

    /**
     * @param configured the raw configured value
     * @param allowLocal whether a loopback host is tolerated (a dev convenience)
     * @return the normalised base, without a trailing slash
     * @throws IllegalArgumentException with a message naming the specific defect
     */
    static String validate(String configured, boolean allowLocal) {
        if (configured == null || configured.isBlank()) {
            throw new IllegalArgumentException(
                    "atlas.public.base.uri is required: the publisher has no request context to derive "
                            + "the atlas's public address from, and publishing a guess into a portal is worse "
                            + "than not publishing");
        }
        String trimmed = configured.trim();
        URI uri;
        try {
            uri = new URI(trimmed);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException("atlas.public.base.uri is not a valid URI: " + trimmed, e);
        }
        if (!uri.isAbsolute()) {
            throw new IllegalArgumentException("atlas.public.base.uri must be absolute, was: " + trimmed);
        }
        String scheme = uri.getScheme().toLowerCase();
        if (!"http".equals(scheme) && !"https".equals(scheme)) {
            throw new IllegalArgumentException(
                    "atlas.public.base.uri must be http or https, was: " + uri.getScheme());
        }
        if (uri.getQuery() != null || uri.getFragment() != null) {
            throw new IllegalArgumentException(
                    "atlas.public.base.uri must carry no query or fragment — the publisher appends its own, "
                            + "including ?nsUri= and &mediaType=, was: " + trimmed);
        }
        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new IllegalArgumentException("atlas.public.base.uri has no host: " + trimmed);
        }
        if (!allowLocal && LOCAL_HOSTS.contains(host.toLowerCase())) {
            throw new IllegalArgumentException("atlas.public.base.uri points at " + host
                    + ", which no harvester can resolve. A loopback URL in a public catalogue is worse than "
                    + "no catalogue entry — set allow.local.base.uri=true if this is a development runtime");
        }
        // Normalise the trailing slash away so callers can always append "/{scope}/...".
        return trimmed.endsWith("/") ? trimmed.substring(0, trimmed.length() - 1) : trimmed;
    }
}
