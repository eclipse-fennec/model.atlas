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
 * A scope the configuration asked for that is not live.
 *
 * <p>
 * Everything but the reason is best effort: a component that never came up may not be introspectable
 * for much else. What is always here is a reason and whatever named the thing that failed.
 * </p>
 *
 * @since 1.0
 */
public class FailedScopeDTO extends DTO {

    /** The scope name, where the configuration gave one. */
    public String name;

    /** One of the constants in {@link FailureReasons}. Always set. */
    public String reason;

    /** The configuration PID this came from, where there is one. */
    public String pid;

    /** The {@code service.id} of the component configuration, or {@code -1}. */
    public long serviceId;

    /** What the reason does not say on its own: the unsatisfied reference, the bad filter. */
    public String detail;
}
