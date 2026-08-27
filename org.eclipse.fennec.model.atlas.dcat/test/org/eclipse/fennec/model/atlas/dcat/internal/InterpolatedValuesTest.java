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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * The guard against publishing an unset environment variable as if it were a value.
 */
public class InterpolatedValuesTest {

    @Test
    public void anUninterpolatedValueIsRefusedAndNamed() {
        // Observed for real: an unset DCAT_PUBLISHER_NAME put this literal into a portal as the
        // Catalog's dct:publisher, with a green health check.
        Map<String, String> values = Map.of("publisher.name", "$[env:DCAT_PUBLISHER_NAME]");

        assertThatThrownBy(() -> InterpolatedValues.requireInterpolated(values))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("publisher.name")
                .hasMessageContaining("DCAT_PUBLISHER_NAME");
    }

    @Test
    public void everyOffenderIsNamedNotJustTheFirst() {
        // An operator fixing one variable at a time per restart is a bad way to learn there were
        // three.
        Map<String, String> values = new LinkedHashMap<>();
        values.put("publisher.name", "$[env:A]");
        values.put("license.uri", "$[env:B]");
        values.put("language", "de");

        assertThatThrownBy(() -> InterpolatedValues.requireInterpolated(values))
                .hasMessageContaining("publisher.name").hasMessageContaining("license.uri");
    }

    @Test
    public void interpolatedConfigurationPasses() {
        Map<String, String> values = new LinkedHashMap<>();
        values.put("publisher.name", "Stadt Jena");
        values.put("license.uri", "http://dcat-ap.de/def/licenses/dl-by-de/2.0");
        values.put("dataset.description.template", "The %s model, %s of %s");

        assertThatCode(() -> InterpolatedValues.requireInterpolated(values)).doesNotThrowAnyException();
    }

    @Test
    public void anAbsentValueIsNotAPlaceholder() {
        // An optional property nobody configured is silence, not a defect: publisher.about and the
        // theme list are both allowed to be unset.
        Map<String, String> values = new LinkedHashMap<>();
        values.put("publisher.about", null);
        values.put("theme[0]", "");

        assertThatCode(() -> InterpolatedValues.requireInterpolated(values)).doesNotThrowAnyException();
    }

    @Test
    public void aPlaceholderAnywhereInTheValueCounts() {
        // Felix substitutes in place, so a partly-resolved template keeps the rest of the text.
        assertThatThrownBy(() -> InterpolatedValues
                .requireInterpolated(Map.of("atlas.public.base.uri", "https://$[env:HOST]/model-atlas")))
                .hasMessageContaining("atlas.public.base.uri");
    }
}
