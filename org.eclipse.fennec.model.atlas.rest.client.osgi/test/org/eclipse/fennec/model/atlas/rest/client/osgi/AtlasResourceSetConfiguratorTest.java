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

import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.fennec.model.atlas.rest.client.api.RemoteEPackageProvider;
import org.eclipse.fennec.model.atlas.rest.client.api.ResolvedEPackage;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the P3-10 ResourceSet configurator. Uses real {@link ResourceSetImpl} /
 * {@link EPackageRegistryImpl}, so the install and the end-to-end fallback through a
 * configured {@code ResourceSet} are exercised without an OSGi framework.
 */
class AtlasResourceSetConfiguratorTest {

	private static EPackage ePackage(String nsUri) {
		EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
		pkg.setName("p");
		pkg.setNsPrefix("p");
		pkg.setNsURI(nsUri);
		return pkg;
	}

	@Test
	void installsTheAtlasRegistry() {
		EPackage.Registry atlas = new EPackageRegistryImpl();
		ResourceSet resourceSet = new ResourceSetImpl();
		EPackage.Registry original = resourceSet.getPackageRegistry();

		new AtlasResourceSetConfigurator(atlas).configureResourceSet(resourceSet);

		assertSame(atlas, resourceSet.getPackageRegistry());
		assertNotSame(original, resourceSet.getPackageRegistry());
	}

	@Test
	void isIdempotent() {
		EPackage.Registry atlas = new EPackageRegistryImpl();
		ResourceSet resourceSet = new ResourceSetImpl();
		AtlasResourceSetConfigurator configurator = new AtlasResourceSetConfigurator(atlas);

		configurator.configureResourceSet(resourceSet);
		configurator.configureResourceSet(resourceSet); // already Atlas-aware → no-op

		assertSame(atlas, resourceSet.getPackageRegistry());
	}

	@Test
	void configuredResourceSetResolvesAnUnknownNsUriViaTheAtlas() {
		// A real lazy registry: framework miss → fetch from the (mocked) Atlas → publish
		// (mirrored into the framework registry to simulate emf.osgi binding) → visible.
		EPackageRegistryImpl framework = new EPackageRegistryImpl();
		RemoteEPackageProvider remote = mock(RemoteEPackageProvider.class);
		EPackage fetched = ePackage("urn:unknown");
		when(remote.resolve("urn:unknown"))
				.thenReturn(Optional.of(new ResolvedEPackage(fetched, "urn:unknown", "atlas", "schema", "released",
						"1.0")));
		PackagePublication mirror = (ePackage, scope, stage, version) -> {
			framework.put(ePackage.getNsURI(), ePackage);
			return true;
		};
		LazyResolvingPackageRegistry atlasRegistry = new LazyResolvingPackageRegistry(framework, remote, mirror,
				ns -> null, 2_000L, 1L, System::currentTimeMillis, Thread::sleep);

		ResourceSet resourceSet = new ResourceSetImpl();
		new AtlasResourceSetConfigurator(atlasRegistry).configureResourceSet(resourceSet);

		// Resolution through the configured ResourceSet's registry reaches the Atlas fallback.
		assertSame(fetched, resourceSet.getPackageRegistry().getEPackage("urn:unknown"));
	}
}
