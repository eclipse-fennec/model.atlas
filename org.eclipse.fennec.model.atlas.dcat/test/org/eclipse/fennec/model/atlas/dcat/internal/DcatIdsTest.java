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

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.Test;

/**
 * A published id is permanent, so these are the assertions that stop a future refactor from
 * silently renaming everything a consumer has bookmarked.
 */
class DcatIdsTest {

    private static final String NS = "http://test.example.com/schema/1.1";

    @Test
    void datasetIdCarriesScopeStageAndTheEncodedNsUri() {
        String id = DcatIds.datasetId("jena", "release", NS);

        assertThat(id).startsWith("jena--release--");
        String encoded = id.substring("jena--release--".length());
        assertThat(new String(Base64.getUrlDecoder().decode(encoded), StandardCharsets.UTF_8)).isEqualTo(NS);
    }

    @Test
    void theEncodingIsUrlSafeAndUnpadded() {
        // These land in portal URLs, so '+', '/' and '=' would all have to be escaped again.
        String id = DcatIds.datasetId("s", "t", NS);
        assertThat(id).doesNotContain("+").doesNotContain("/").doesNotContain("=");
    }

    @Test
    void sameNsUriInDifferentStagesGetsDifferentIds() {
        // The whole reason stage is in the id: one nsURI legitimately lives in several stages, and
        // collapsing them would make the portal show one Dataset flickering between stages.
        assertThat(DcatIds.datasetId("jena", "draft", NS)).isNotEqualTo(DcatIds.datasetId("jena", "release", NS));
        assertThat(DcatIds.datasetId("jena", "release", NS)).isNotEqualTo(DcatIds.datasetId("other", "release", NS));
    }

    @Test
    void distributionIdIsAMediaTypeSlug() {
        assertThat(DcatIds.distributionId("application/xmi")).isEqualTo("application-xmi");
        assertThat(DcatIds.distributionId("application/schema+json")).isEqualTo("application-schema-json");
        assertThat(DcatIds.distributionId("APPLICATION/JSON")).isEqualTo("application-json");
    }

    @Test
    void catalogIdIsTheScopeName() {
        assertThat(DcatIds.catalogId("jena")).isEqualTo("jena");
    }
}
