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
 * A registry that is configured but unusable.
 *
 * @since 1.0
 */
public class FailedRegistryDTO extends DTO {

    /** The scope it belongs to, where that is known. */
    public String scope;

    /** The registry name, where the configuration gave one. */
    public String name;

    /** One of the constants in {@link FailureReasons}. Always set. */
    public String reason;

    public String pid;

    public long serviceId;

    public String detail;
}
