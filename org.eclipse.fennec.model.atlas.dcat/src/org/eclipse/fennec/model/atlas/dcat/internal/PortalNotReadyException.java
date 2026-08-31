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

/**
 * The portal answered that it is not ready, so the work was not attempted.
 *
 * <p>
 * An exception rather than a quiet {@code return}: not-ready is the most ordinary transient
 * condition there is — a portal still starting, or its store briefly unusable — and returning
 * silently dropped the publish until something unrelated happened to trigger another one. Throwing
 * puts it on the same footing as a 503, which is what it is.
 * </p>
 */
class PortalNotReadyException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    PortalNotReadyException(String message) {
        super(message);
    }
}
