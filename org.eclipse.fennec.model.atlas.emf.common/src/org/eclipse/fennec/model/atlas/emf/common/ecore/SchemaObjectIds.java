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
package org.eclipse.fennec.model.atlas.emf.common.ecore;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/**
 * The objectId a schema package has within a schema registry.
 *
 * <p>
 * The nsURI, URL-safe Base64 encoded. UTF-8 is fixed so ids stay stable across platform default
 * charsets. Shared, so the registry and every reader of it derive the same id.
 * </p>
 */
public final class SchemaObjectIds {

    private SchemaObjectIds() {
    }

    /**
     * @param nsURI the package's namespace URI, may be {@code null}
     * @return the objectId, or {@code null} for a {@code null} nsURI
     */
    public static String fromNsUri(String nsURI) {
        return nsURI == null ? null : Base64.getUrlEncoder().encodeToString(nsURI.getBytes(StandardCharsets.UTF_8));
    }
}
