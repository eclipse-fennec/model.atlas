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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

/**
 * These URLs are the only thing a harvester can actually fetch, so the encoding matters more than
 * it looks: an unescaped nsURI would truncate the query at its own {@code ://}.
 */
class AtlasContentUrlsTest {

    private static final String BASE = "https://opendata.example.de/model-atlas";
    private static final String NS = "http://test.example.com/schema/1.1";

    @Test
    void accessUrlAddressesTheStageContentEndpoint() {
        assertThat(AtlasContentUrls.accessUrl(BASE, "jena", "release", NS))
                .isEqualTo(BASE + "/jena/schema/stages/release/content?nsUri="
                        + "http%3A%2F%2Ftest.example.com%2Fschema%2F1.1");
    }

    @Test
    void downloadUrlPinsTheRepresentation() {
        // A distribution per representation, rather than one URL differing only by Accept: a
        // gateway cache keyed without Vary would otherwise serve XMI to a JSON harvester.
        assertThat(AtlasContentUrls.downloadUrl(BASE, "jena", "release", NS, "application/xmi"))
                .endsWith("&mediaType=application%2Fxmi");
    }

    @Test
    void theBaseIsUsedVerbatimSoAGatewayPrefixSurvives() {
        assertThat(AtlasContentUrls.accessUrl("https://host/rewritten/prefix", "s", "t", "urn:x"))
                .startsWith("https://host/rewritten/prefix/s/schema/stages/t/content");
    }
}
