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

import org.osgi.annotation.versioning.ConsumerType;

/**
 * Decides what reaches a DCAT portal.
 *
 * <p>
 * Register an implementation as a whiteboard service to override the default; the highest
 * {@code service.ranking} wins. The verdict is three-level and no single level can grant
 * publication on its own: the deployment's configuration opts a <em>scope</em> in, configuration
 * gates which <em>stages</em> publish, and the package's own metadata decides the package. This
 * interface covers the first two; the per-package flag rides on the {@code EPackage} service
 * properties, so it is settled by the tracker's target filter before a policy is ever consulted.
 * </p>
 */
@ConsumerType
public interface DcatPublicationPolicy {

    /**
     * Whether this scope is published at all.
     *
     * @param scope the scope name
     * @return {@code true} if the scope may have a Catalog and Datasets in the portal
     */
    boolean publishScope(String scope);

    /**
     * Whether this particular package-in-a-stage is published.
     *
     * @param target the candidate
     * @return {@code true} if it should appear in the portal
     */
    boolean publish(PublicationTarget target);
}
