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
package org.eclipse.fennec.model.atlas.rest.common;

/**
 * Shared JAX-RS request property keys used by {@code ModelAtlasRequestFilter}
 * and downstream REST providers.
 */
public final class ModelAtlasRestConstants {

    private ModelAtlasRestConstants() {
    }

    /**
     * Request property carrying the resolved media type string (from the
     * {@code mediaType} query parameter or the Accept header).
     */
    public static final String RESOLVED_MEDIA_TYPE = "resolvedMediaType";
}
