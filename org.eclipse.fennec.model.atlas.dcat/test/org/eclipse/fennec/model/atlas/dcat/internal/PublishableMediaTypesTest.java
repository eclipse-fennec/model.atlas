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

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * The catalogue must never advertise a format the server would answer 415 for, which is what the
 * intersection is for.
 */
class PublishableMediaTypesTest {

    @Test
    void keepsOnlyWhatTheRuntimeCanActuallyServe() {
        assertThat(PublishableMediaTypes.resolve(new String[] { "application/xmi", "application/json" },
                List.of("application/xmi", "application/x-odd"))).containsExactly("application/xmi");
    }

    @Test
    void preservesTheAllowlistOrderNotTheRuntimeOrder() {
        assertThat(PublishableMediaTypes.resolve(new String[] { "application/json", "application/xmi" },
                List.of("application/xmi", "application/json")))
                .containsExactly("application/json", "application/xmi");
    }

    @Test
    void isEmptyWhenTheyDoNotOverlap() {
        assertThat(PublishableMediaTypes.resolve(new String[] { "application/json" }, List.of("application/xmi")))
                .isEmpty();
    }

    // ---- keeping an already-published Dataset in step with the allowlist ----

    @Test
    void aDatasetAdvertisingExactlyTheAllowlistIsUpToDate() {
        assertThat(PublishableMediaTypes.distributionsOutOfDate(Set.of("application/xmi"),
                List.of("application/xmi"))).isFalse();
    }

    @Test
    void narrowingOrWideningTheAllowlistMakesItOutOfDate() {
        // The content has not changed, so the fingerprint check would skip the write. This is the
        // only thing that notices.
        assertThat(PublishableMediaTypes.distributionsOutOfDate(Set.of("application/xmi"),
                List.of("application/xmi", "application/json"))).as("narrowed").isTrue();
        assertThat(PublishableMediaTypes.distributionsOutOfDate(Set.of("application/xmi", "application/json"),
                List.of("application/xmi"))).as("widened").isTrue();
        assertThat(PublishableMediaTypes.distributionsOutOfDate(Set.of("application/json"),
                List.of("application/xmi"))).as("swapped").isTrue();
    }

    @Test
    void anEmptyResolutionNeverStripsWhatIsPublished() {
        // Empty means the runtime reports none of the allowed formats, which is what startup looks
        // like while codecs are still coming up. Acting on it would empty every Dataset and leave
        // nothing to put the Distributions back.
        assertThat(PublishableMediaTypes.distributionsOutOfDate(Set.of(), List.of("application/xmi"))).isFalse();
        assertThat(PublishableMediaTypes.distributionsOutOfDate(null, List.of("application/xmi"))).isFalse();
    }

    @Test
    void aDatasetWithNoDistributionsNeedsTheOnesTheAllowlistAllows() {
        assertThat(PublishableMediaTypes.distributionsOutOfDate(Set.of("application/xmi"), List.of())).isTrue();
        assertThat(PublishableMediaTypes.distributionsOutOfDate(Set.of("application/xmi"), null)).isTrue();
    }

    @Test
    void theComparisonIgnoresCasingAndOrder() {
        assertThat(PublishableMediaTypes.distributionsOutOfDate(Set.of("application/xmi", "application/json"),
                List.of("APPLICATION/JSON", " application/xmi "))).isFalse();
    }

    @Test
    void toleratesNullsBlanksAndCasing() {
        assertThat(PublishableMediaTypes.resolve(new String[] { " APPLICATION/XMI ", null, "" },
                List.of("application/xmi"))).containsExactly("application/xmi");
        assertThat(PublishableMediaTypes.resolve(null, List.of("application/xmi"))).isEmpty();
        assertThat(PublishableMediaTypes.resolve(new String[] { "application/xmi" }, null)).isEmpty();
    }
}
