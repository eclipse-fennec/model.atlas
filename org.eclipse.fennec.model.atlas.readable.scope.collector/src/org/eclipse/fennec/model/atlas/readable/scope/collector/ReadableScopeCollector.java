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
package org.eclipse.fennec.model.atlas.readable.scope.collector;

import java.util.List;

import org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Collects the {@link ReadableScopeService} instances registered in the
 * framework, keyed by their scope name (the {@code atlas.scope} service
 * property).
 *
 * <p>
 * Implementations are thread-safe; the tracked set changes with service
 * dynamics, so a scope resolvable now may be gone on the next call.
 * </p>
 */
@ProviderType
public interface ReadableScopeCollector {

	/**
	 * Returns the {@link ReadableScopeService} registered for the given scope
	 * name, or {@code null} if none is currently bound.
	 */
	ReadableScopeService<?> getScopeServiceByScopeName(String scopeName);

	/** Returns the scope names of all currently bound readable scope services. */
	List<String> getAllScopeNames();
}
