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
 * A schema package that did not become a live EPackage service.
 *
 * @since 1.0
 */
public class FailedPackageDTO extends DTO {

    public String scope;

    public String registry;

    public String stage;

    /** The namespace URI, where it is known — a package that failed to load may not have one. */
    public String nsURI;

    /** One of the constants in {@link FailureReasons}. Always set. */
    public String reason;

    public String pid;

    public long serviceId;

    public String detail;
}
