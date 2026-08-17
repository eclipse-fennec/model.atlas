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
package org.eclipse.fennec.model.atlas.workflow;

import java.util.List;

import org.eclipse.fennec.model.atlas.wf.workflowapi.Scope;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Collects the {@link ScopeService} instances registered in the framework,
 * keyed by their scope name (the {@code atlas.scope} service property, falling
 * back to {@code scope.name}).
 *
 * <p>
 * Implementations are thread-safe; the tracked set changes with service
 * dynamics, so a scope resolvable now may be gone on the next call.
 * </p>
 *
 * @author ilenia
 * @since Jan 15, 2026
 */
@ProviderType
public interface ScopeServiceCollector {

	/**
	 * Returns the {@link ScopeService} registered for the given scope name, or
	 * {@code null} if none is currently bound.
	 */
	ScopeService<?> getScopeServiceByScopeName(String scopeName);

	/**
	 * Returns the {@link Scope} of the service registered for the given scope
	 * name, or {@code null} if none is currently bound.
	 */
	Scope getScopeByName(String scopeName);

	/** Returns the {@link Scope}s of all currently bound scope services. */
	List<Scope> getAllScopes();
}
