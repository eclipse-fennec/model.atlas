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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

/**
 * The base URI is the one thing the publisher cannot derive and cannot guess, so every way of
 * getting it wrong should fail loudly at activation rather than quietly in a public catalogue.
 */
class PublicBaseUriTest {

    @Test
    void acceptsAGatewayPrefixAndKeepsIt() {
        // The whole public prefix, not just scheme and host: an APISIX rewrites paths too, so the
        // container's own /atlas/rest is not necessarily what the world sees.
        assertThat(PublicBaseUri.validate("https://opendata.example.de/model-atlas", false))
                .isEqualTo("https://opendata.example.de/model-atlas");
    }

    @Test
    void normalisesTheTrailingSlashAway() {
        assertThat(PublicBaseUri.validate("https://opendata.example.de/model-atlas/", false))
                .isEqualTo("https://opendata.example.de/model-atlas");
    }

    @Test
    void trimsSurroundingWhitespace() {
        assertThat(PublicBaseUri.validate("  https://opendata.example.de/x  ", false))
                .isEqualTo("https://opendata.example.de/x");
    }

    @Test
    void refusesAMissingValue() {
        assertThatThrownBy(() -> PublicBaseUri.validate(null, false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("atlas.public.base.uri is required");
        assertThatThrownBy(() -> PublicBaseUri.validate("   ", false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("atlas.public.base.uri is required");
    }

    @Test
    void refusesARelativeValue() {
        assertThatThrownBy(() -> PublicBaseUri.validate("/atlas/rest", false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be absolute");
    }

    @Test
    void refusesANonHttpScheme() {
        assertThatThrownBy(() -> PublicBaseUri.validate("ftp://example.de/x", false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("must be http or https");
    }

    @Test
    void refusesAQueryOrFragmentBecauseThePublisherAppendsItsOwn() {
        assertThatThrownBy(() -> PublicBaseUri.validate("https://example.de/x?a=b", false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no query or fragment");
        assertThatThrownBy(() -> PublicBaseUri.validate("https://example.de/x#frag", false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("no query or fragment");
    }

    @Test
    void refusesLoopbackByDefaultButAllowsItWhenAskedTo() {
        assertThatThrownBy(() -> PublicBaseUri.validate("http://localhost:8080/atlas/rest", false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("allow.local.base.uri");
        assertThatThrownBy(() -> PublicBaseUri.validate("http://127.0.0.1:8080/atlas/rest", false))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("allow.local.base.uri");

        assertThat(PublicBaseUri.validate("http://localhost:8080/atlas/rest", true))
                .isEqualTo("http://localhost:8080/atlas/rest");
    }
}
