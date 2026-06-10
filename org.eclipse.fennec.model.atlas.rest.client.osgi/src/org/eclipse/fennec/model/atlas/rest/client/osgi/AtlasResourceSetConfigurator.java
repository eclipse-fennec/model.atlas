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
package org.eclipse.fennec.model.atlas.rest.client.osgi;

import java.util.Objects;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.emf.osgi.configurator.ResourceSetConfigurator;

/**
 * P3-10 — installs the Atlas-aware delegating registry on every {@link ResourceSet} the
 * framework's {@code ResourceSetFactory} builds, so a resource load that references an
 * unknown nsURI falls back to the Atlas (fetch + publish + block, via
 * {@link LazyResolvingPackageRegistry}).
 * <p>
 * {@code emf.osgi}'s default {@code ResourceSetFactory} binds every registered
 * {@link ResourceSetConfigurator} (no target filter) and calls
 * {@link #configureResourceSet(ResourceSet)} on each {@code ResourceSet} it creates.
 * We set the {@code ResourceSet}'s package registry to the shared
 * {@link LazyResolvingPackageRegistry}, whose {@code primary} is the framework's default
 * {@code EPackage.Registry} — the very registry a default-factory {@code ResourceSet}
 * would otherwise carry — so local/framework packages keep first precedence and only a
 * genuine miss reaches the Atlas. The operation is idempotent.
 * <p>
 * Wrapping is enabled only while {@code resource.set.fallback=true}: the component
 * registers this service in that case and unregisters it otherwise / on shutdown, which
 * stops <em>new</em> {@code ResourceSet}s from being wrapped (the interface has no
 * unconfigure hook, so already-created ones keep the registry; after the client closes,
 * its fallback simply returns {@code null} for unknown nsURIs — local resolution is
 * unaffected).
 */
final class AtlasResourceSetConfigurator implements ResourceSetConfigurator {

	private final EPackage.Registry atlasRegistry;

	AtlasResourceSetConfigurator(EPackage.Registry atlasRegistry) {
		this.atlasRegistry = Objects.requireNonNull(atlasRegistry, "atlasRegistry");
	}

	@Override
	public void configureResourceSet(ResourceSet resourceSet) {
		if (resourceSet.getPackageRegistry() == atlasRegistry) {
			return; // already Atlas-aware — idempotent
		}
		resourceSet.setPackageRegistry(atlasRegistry);
	}
}
