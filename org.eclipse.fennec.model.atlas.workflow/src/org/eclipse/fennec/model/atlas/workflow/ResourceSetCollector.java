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

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.osgi.annotation.versioning.ProviderType;
import org.osgi.service.component.ComponentServiceObjects;

/**
 * Collects the scope/stage-specific {@link ResourceSet} prototype services and
 * {@link ResourceSetFactory} services published by
 * {@code SchemaRegistryChainConfigurator}. Each tracked service carries the
 * {@link #SCOPE_NAME_PROPERTY} and {@link #STAGE_NAME_PROPERTY} service
 * properties propagated from its {@code ResourceSetFactory} configuration.
 *
 * <p>
 * Callers resolve the {@link ComponentServiceObjects} for a given (scope,
 * stage) pair and are responsible for the
 * {@link ComponentServiceObjects#getService() getService()} /
 * {@link ComponentServiceObjects#ungetService(Object) ungetService()}
 * lifecycle. Implementations are thread-safe; the tracked set changes with
 * service dynamics.
 * </p>
 *
 * @author ilenia
 * @since Apr 17, 2026
 */
@ProviderType
public interface ResourceSetCollector {

	/** Service property carrying the scope name of a tracked service. */
	String SCOPE_NAME_PROPERTY = "scope.name";
	/** Service property carrying the stage name of a tracked service. */
	String STAGE_NAME_PROPERTY = "stage.name";

	/**
	 * Returns the {@link ComponentServiceObjects} for the {@link ResourceSet}
	 * registered for the given (scope, stage) pair, or {@code null} if none is
	 * currently bound or either argument is {@code null}.
	 */
	ComponentServiceObjects<ResourceSet> getResourceSetObjects(String scopeName, String stageName);

	/**
	 * Returns the {@link ResourceSetFactory} registered for the given (scope,
	 * stage) pair, or {@code null} if none is currently bound or either argument
	 * is {@code null}.
	 */
	ResourceSetFactory getResourceSetFactory(String scopeName, String stageName);
}
