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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.runtime.info;

import org.osgi.dto.DTO;

/**
 * One media type the REST layer can serve, and who announced it.
 *
 * <p>
 * The list of types on its own is already available; the <em>origin</em> is not, and it is the part
 * worth reporting. A codec announces a type by registering a content type in the resource set, and
 * it can produce that type for anything. A contributor announces one for the whole REST layer
 * whether or not every endpoint can produce it — which is how a documentation format offered
 * globally turns into a 500 on a listing that has no documentation to give.
 * </p>
 *
 * @since 1.0
 */
public class MediaTypeDTO extends DTO {

    /** The media type, as it is announced. */
    public String mediaType;

    /** {@code CODEC} for a resource-set content type, {@code CONTRIBUTOR} for a declared one. */
    public String origin;

    /** The {@code service.id} that contributed it, or {@code -1} when it came from a codec. */
    public long serviceId;

    /**
     * Whether every endpoint can serve it.
     *
     * <p>
     * True for a codec, which can render any EMF content. False for a contributor, whose types are
     * announced for the whole REST layer but produced only where a writer exists.
     * </p>
     */
    public boolean universal;
}
