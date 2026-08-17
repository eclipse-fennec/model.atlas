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

import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Collects the {@link RegistryService} instances registered in the framework,
 * keyed by their {@code registry.name} service property.
 *
 * <p>
 * Implementations are thread-safe; the tracked set changes with service
 * dynamics, so a registry resolvable now may be gone on the next call.
 * </p>
 *
 * @author ilenia
 * @since Jan 15, 2026
 */
@ProviderType
public interface RegistryServiceCollector {

	/**
	 * Returns the {@link RegistryService} registered for the given registry name,
	 * or {@code null} if none is currently bound.
	 */
	RegistryService<?> getRegistryServiceByRegistryName(String registryName);
}
