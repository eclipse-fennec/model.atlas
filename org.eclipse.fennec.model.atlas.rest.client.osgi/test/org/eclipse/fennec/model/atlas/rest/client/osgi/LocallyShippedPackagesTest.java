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

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.junit.jupiter.api.Test;

/**
 * #330 — the local package registry the OSGi client hands its core: the framework registry,
 * narrowed to what this runtime ships itself.
 */
class LocallyShippedPackagesTest {

	private static final String NS = "https://example.org/atlas/local/1.0";

	private final EPackageRegistryImpl framework = new EPackageRegistryImpl();
	private final EPackage held = pkg();

	LocallyShippedPackagesTest() {
		framework.put(NS, held);
	}

	@Test
	void aLocallyShippedPackageIsAnswered() {
		LocallyShippedPackages registry = new LocallyShippedPackages(framework, ns -> true, ns -> null);

		assertSame(held, registry.getEPackage(NS));
		assertSame(held.getEFactoryInstance(), registry.getEFactory(NS));
	}

	@Test
	void aNamespaceNoLocalBundleProvidesIsNotLocal() {
		// e.g. force.remote, or a package only the Atlas serves
		LocallyShippedPackages registry = new LocallyShippedPackages(framework, ns -> false, ns -> null);

		assertNull(registry.getEPackage(NS));
		assertNull(registry.getEFactory(NS));
	}

	@Test
	void whatThisClientPublishedIsNotLocal() {
		// The framework holds our own Atlas publication: answering with it would let a
		// stage-pinned fetch bind whichever Atlas version is published.
		LocallyShippedPackages registry = new LocallyShippedPackages(framework, ns -> true, ns -> held);

		assertNull(registry.getEPackage(NS));
	}

	@Test
	void aDeclaredButUnrealisedPackageIsAbsent() {
		LocallyShippedPackages registry = new LocallyShippedPackages(new EPackageRegistryImpl(), ns -> true,
				ns -> null);

		assertNull(registry.getEPackage(NS), "no instance yet (#254): the caller falls back to the Atlas");
	}

	private static EPackage pkg() {
		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		ePackage.setName("local");
		ePackage.setNsURI(NS);
		ePackage.setNsPrefix("local");
		return ePackage;
	}
}
