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
package org.eclipse.fennec.model.atlas.runtime.info.internal;

import org.eclipse.fennec.model.atlas.scope.api.ScopeInfo;

/**
 * One live scope service, reduced to what the snapshot needs.
 *
 * <p>
 * Taken off the service registry rather than asked of a collector, because a collector can only
 * hold what registered and this service's job includes naming what did not.
 * </p>
 *
 * @param serviceId  the {@code service.id} of the scope service
 * @param info       the scope's own descriptor, with its registries and stages
 * @param inheriting whether reads read through to the parent scope's final stage
 */
record TrackedScope(long serviceId, ScopeInfo info, boolean inheriting) {
}
