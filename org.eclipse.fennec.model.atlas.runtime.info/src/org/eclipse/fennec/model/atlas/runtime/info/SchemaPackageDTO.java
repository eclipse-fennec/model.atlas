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
 * One schema package, in one stage.
 *
 * <p>
 * Identified by (scope, registry, stage, nsURI). The nsURI alone is not an identity here: several
 * versions of one nsURI are live at once, one per stage, and a consumer that evicts by nsURI alone
 * drops the version it is still holding.
 * </p>
 *
 * @since 1.0
 */
public class SchemaPackageDTO extends DTO {

    /** The namespace URI, as the {@code emf.nsURI} service property carries it. */
    public String nsURI;

    public String name;

    /**
     * The package's declared version — a {@code Version} annotation on the EPackage, else a
     * version-shaped last segment of the nsURI. Not the change count, and not the Atlas's version.
     */
    public String version;

    /**
     * The semantic identity of the model.
     *
     * <p>
     * The only field that tells two packages apart when they share an nsURI and a declared version,
     * which is exactly what a consumer diffing one stage against another has to resolve. Reported as
     * {@code null} when it cannot be determined, never left out.
     * </p>
     */
    public String fingerprint;

    /** The identifier this package has within its registry. */
    public String objectId;

    /** The stage this package is live in. Always set. */
    public String stage;

    /** The {@code service.id} of the registered EPackage service, or {@code -1} if there is none. */
    public long serviceId;

    /**
     * Where this package came from.
     *
     * <p>
     * {@code STORED} — the Atlas registered it out of a scope's registry. {@code GENERATED} — a
     * bundle brought it into a scope. {@code GLOBAL} — it is registered globally rather than into
     * any scope, which is what the built-in {@code atlas} scope's content consists of.
     * </p>
     */
    public String origin;
}
