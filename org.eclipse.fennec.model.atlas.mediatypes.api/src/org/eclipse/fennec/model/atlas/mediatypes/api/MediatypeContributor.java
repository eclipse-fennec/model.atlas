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
package org.eclipse.fennec.model.atlas.mediatypes.api;

import java.util.List;

import org.osgi.annotation.versioning.ConsumerType;

/**
 * <p>
 * Declares media types the REST API can serve which no {@code Resource.Factory} backs.
 * </p>
 *
 * <p>
 * {@link SupportedMediatype} derives its list from the content types registered in the bound
 * {@code ResourceSet}, which is how every codec announces itself. A format produced by something
 * other than an EMF resource - a {@code MessageBodyWriter} that renders documentation, say - has
 * nothing to register there, and a media type missing from that list is rejected by the request
 * filter before the writer is ever consulted. Registering this service is how such a bundle says
 * what it can serve, and withdrawing it is how the media types disappear again when the bundle is
 * not installed.
 * </p>
 *
 * @since Oct 12, 2026
 */
@ConsumerType
public interface MediatypeContributor {

    /**
     * @return the media types this contributor can serve, never {@code null}
     */
    List<String> getMediaTypes();

}
