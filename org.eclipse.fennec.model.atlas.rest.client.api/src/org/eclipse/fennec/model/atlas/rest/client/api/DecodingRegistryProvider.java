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
package org.eclipse.fennec.model.atlas.rest.client.api;

import org.eclipse.emf.ecore.EPackage;
import org.osgi.annotation.versioning.ConsumerType;

/**
 * Supplies the package registry objects read from a scope are decoded against, so that
 * they bind the same {@link EPackage} instances the rest of the runtime holds for that
 * scope and stage (issue #347).
 * <p>
 * An object read through a scope's {@code ReadableScopeService} references its metamodel
 * by nsURI. Without a provider the client resolves those references through registries of
 * its own: one per scope and stage, so repeated reads of a stage bind one instance, and a
 * read of the scope's final stage binds the instance the stage-free path holds. A runtime
 * that keeps its own registries per scope and stage - the OSGi front-end does - hands them
 * in here, and the objects it reads then share their packages with everything else that
 * resolves through those registries.
 * <p>
 * The packages the runtime ships itself still win: the client consults its local package
 * registry before the returned one (issue #330).
 */
@ConsumerType
@FunctionalInterface
public interface DecodingRegistryProvider {

	/**
	 * The registry objects read from {@code scope} at {@code stage} are decoded against.
	 *
	 * @param scope the scope the object is read from
	 * @param stage the stage the object is read from, or {@code null} for a stage-free read
	 *              (the scope's final stage)
	 * @return the registry to decode against, or {@code null} to leave it to the client
	 */
	EPackage.Registry registryFor(String scope, String stage);
}
