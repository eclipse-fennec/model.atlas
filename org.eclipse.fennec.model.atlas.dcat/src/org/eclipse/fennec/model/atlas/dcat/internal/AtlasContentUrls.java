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

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * The public URLs a Distribution advertises.
 *
 * <p>
 * The atlas's request filter honours a {@code ?mediaType=} query parameter that wins over
 * {@code Accept} and answers 415 for an unsupported value, so each representation gets a URL of
 * its own instead of one URL that differs only by a request header. That is not merely nicer: a
 * gateway response cache keyed without {@code Vary: Accept} will happily serve XMI it cached to
 * the next harvester asking for JSON, whereas a query parameter is in a cache key by default.
 * </p>
 */
final class AtlasContentUrls {

    private AtlasContentUrls() {
    }

    /**
     * Content negotiated by {@code Accept}.
     *
     * @param base  the validated public prefix, without a trailing slash
     * @param scope the defining scope
     * @param stage the stage
     * @param nsUri the namespace URI
     * @return the access URL
     */
    static String contentUrl(String base, String scope, String stage, String nsUri) {
        return base + "/" + scope + "/schema/stages/" + stage + "/content?nsUri=" + encode(nsUri);
    }

    /**
     * One concrete representation, pinned by {@code ?mediaType=}.
     *
     * @param base      the validated public prefix, without a trailing slash
     * @param scope     the defining scope
     * @param stage     the stage
     * @param nsUri     the namespace URI
     * @param mediaType the representation to pin
     * @return the download URL
     */
    static String downloadUrl(String base, String scope, String stage, String nsUri, String mediaType) {
        return contentUrl(base, scope, stage, nsUri) + "&mediaType=" + encode(mediaType);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
