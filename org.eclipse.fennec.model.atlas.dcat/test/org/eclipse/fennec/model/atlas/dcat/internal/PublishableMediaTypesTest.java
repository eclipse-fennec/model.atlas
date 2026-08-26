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

    @Test
    void toleratesNullsBlanksAndCasing() {
        assertThat(PublishableMediaTypes.resolve(new String[] { " APPLICATION/XMI ", null, "" },
                List.of("application/xmi"))).containsExactly("application/xmi");
        assertThat(PublishableMediaTypes.resolve(null, List.of("application/xmi"))).isEmpty();
        assertThat(PublishableMediaTypes.resolve(new String[] { "application/xmi" }, null)).isEmpty();
    }
}
