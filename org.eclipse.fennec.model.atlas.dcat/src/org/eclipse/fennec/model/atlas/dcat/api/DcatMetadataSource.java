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
package org.eclipse.fennec.model.atlas.dcat.api;

import java.util.List;
import java.util.Optional;

import org.osgi.annotation.versioning.ConsumerType;

/**
 * Supplies the descriptive metadata a DCAT entity needs but the atlas does not model.
 *
 * <p>
 * Register an implementation as a whiteboard service to override the default; the highest
 * {@code service.ranking} wins. Every method may return an empty {@link Optional} or an empty
 * list, in which case the publisher falls back to its configuration — so an implementation can
 * answer for one field and stay silent about the rest.
 * </p>
 */
@ConsumerType
public interface DcatMetadataSource {

    /**
     * @param target the package being described
     * @return the human-readable title, or empty to fall back
     */
    Optional<String> title(PublicationTarget target);

    /**
     * @param target the package being described
     * @return the description, or empty to fall back
     */
    Optional<String> description(PublicationTarget target);

    /**
     * @param target the package being described
     * @return the publisher's name, or empty to fall back
     */
    Optional<String> publisherName(PublicationTarget target);

    /**
     * @param target the package being described
     * @return the licence IRI, or empty to fall back
     */
    Optional<String> licenseUri(PublicationTarget target);

    /**
     * @param target the package being described
     * @return DCAT-AP.de data-theme IRIs; empty to fall back
     */
    List<String> themes(PublicationTarget target);

    /**
     * @param target the package being described
     * @return keywords to add to those the publisher derives; empty to fall back
     */
    List<String> keywords(PublicationTarget target);
}
