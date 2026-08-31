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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Refuses a configuration whose placeholders never interpolated.
 *
 * <h2>Why this is worth a guard of its own</h2>
 *
 * The docker configuration takes its deployment-specific values from the environment
 * (<code>$[env:DCAT_PUBLISHER_NAME]</code> and friends). When a variable is not set, Felix's
 * interpolation plugin leaves the placeholder <em>as the value</em> — it does not fail, and it does
 * not log at a level a production runtime keeps. So an unset variable does not stop the publisher:
 * it publishes the placeholder text.
 *
 * <p>
 * That was observed rather than imagined. With {@code DCAT_PUBLISHER_NAME} unset, a portal stored
 * <code>&lt;name value="$[env:DCAT_PUBLISHER_NAME]"/&gt;</code> as the Catalog's {@code dct:publisher}
 * — the field that says who governs the data, in a catalogue meant for the public — while the health
 * check reported the publisher healthy throughout.
 * </p>
 *
 * <p>
 * {@code atlas.public.base.uri} escaped this only by accident: {@link PublicBaseUri} rejects the
 * placeholder because it is not a valid URI. The lesson is that the check belongs to the
 * configuration as a whole rather than to the one property that happened to be validated, so this
 * runs over every configured string before anything is published.
 * </p>
 */
final class InterpolatedValues {

    /** The shape Felix's interpolation plugin leaves behind when it cannot resolve a value. */
    private static final String PLACEHOLDER_PREFIX = "$[";

    private InterpolatedValues() {
    }

    /**
     * @param values configured values by property name; {@code null} values are ignored
     * @throws IllegalArgumentException naming every property that still carries a placeholder, and
     *         the placeholder itself, so the message says which variable to set
     */
    static void requireInterpolated(Map<String, String> values) {
        Map<String, String> unresolved = new LinkedHashMap<>();
        values.forEach((property, value) -> {
            if (value != null && value.contains(PLACEHOLDER_PREFIX)) {
                unresolved.put(property, value.trim());
            }
        });
        if (unresolved.isEmpty()) {
            return;
        }
        StringBuilder message = new StringBuilder("configuration was not interpolated, so publishing would put "
                + "placeholder text into a catalogue. Set the environment variable(s) behind: ");
        unresolved.forEach((property, value) -> message.append(property).append('=').append(value).append("  "));
        throw new IllegalArgumentException(message.toString().trim());
    }
}
