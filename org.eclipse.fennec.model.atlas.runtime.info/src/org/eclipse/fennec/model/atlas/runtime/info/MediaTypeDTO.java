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
 * worth reporting. A contributor is a service this runtime binds itself, so a type it declares is
 * named together with the service that declared it. Everything else arrives through
 * {@code SupportedMediatype} as a flat list of strings, and is reported as what it demonstrably is:
 * what the resource set announces.
 * </p>
 *
 * <p>
 * <strong>It does not say whether every endpoint can serve a type</strong>, because this service
 * cannot know. That list holds both the content types a codec registered, which apply to any EMF
 * content, and formats a bundle announces for itself with no {@code Resource.Factory} behind them
 * — {@code application/uml} is written by one JAX-RS provider, for an {@code EPackage} and
 * nothing else. The two are indistinguishable by the time they reach here, so claiming either would
 * be a guess, and the optimistic guess is the one that turns into a 500 on an endpoint that cannot
 * produce the format.
 * </p>
 *
 * @since 1.0
 */
public class MediaTypeDTO extends DTO {

    /** The media type, as it is announced. */
    public String mediaType;

    /**
     * {@code RESOURCE_SET} for a type the resource set announces, {@code CONTRIBUTOR} for one a
     * {@link org.eclipse.fennec.model.atlas.mediatypes.api.MediatypeContributor} declares.
     */
    public String origin;

    /**
     * The {@code service.id} that contributed it, or {@code -1} for a resource-set type.
     *
     * <p>
     * There is no service to name in that case: the type is a key in the resource set's
     * content-type map, or a literal a bundle appended to it, and neither carries the registration
     * it may once have come from.
     * </p>
     */
    public long serviceId;
}
