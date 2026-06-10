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
package org.eclipse.fennec.model.atlas.rest.client.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.model.atlas.rest.client.api.RemoteEPackageProvider;
import org.eclipse.fennec.model.atlas.rest.client.api.ResolvedEPackage;
import org.junit.jupiter.api.Test;

/**
 * P2-8 — the Atlas-aware delegating package registry: primary precedence, remote
 * fallback with caching, drift eviction, and loading an XMI instance that
 * references an otherwise-unknown nsURI.
 */
class AtlasDelegatingPackageRegistryTest {

	private static final String NS = "urn:atlas:test:demo/1.0";

	/** In-memory {@link RemoteEPackageProvider}; counts {@code ensureAvailable} calls. */
	private static final class FakeProvider implements RemoteEPackageProvider {
		final Map<String, EPackage> packages = new HashMap<>();
		final AtomicInteger ensureCalls = new AtomicInteger();

		@Override
		public Optional<EPackage> getEPackage(String nsUri) {
			return ensureAvailable(nsUri);
		}

		@Override
		public List<String> listNsUris(String scopeName) {
			return List.copyOf(packages.keySet());
		}

		@Override
		public Optional<EPackage> ensureAvailable(String nsUri) {
			ensureCalls.incrementAndGet();
			return Optional.ofNullable(packages.get(nsUri));
		}

		@Override
		public Optional<EPackage> refresh(String nsUri) {
			return ensureAvailable(nsUri);
		}

		@Override
		public Optional<ResolvedEPackage> resolve(String nsUri) {
			return ensureAvailable(nsUri)
					.map(pkg -> new ResolvedEPackage(pkg, nsUri, "test", "schema", "released", null));
		}
	}

	private static EPackage demoPackage() {
		EcoreFactory f = EcoreFactory.eINSTANCE;
		EPackage pkg = f.createEPackage();
		pkg.setName("demo");
		pkg.setNsPrefix("demo");
		pkg.setNsURI(NS);
		EClass item = f.createEClass();
		item.setName("Item");
		pkg.getEClassifiers().add(item);
		EAttribute label = f.createEAttribute();
		label.setName("label");
		label.setEType(EcorePackage.eINSTANCE.getEString());
		item.getEStructuralFeatures().add(label);
		return pkg;
	}

	@Test
	void primaryRegistryTakesPrecedence() {
		EPackage primaryPkg = demoPackage();
		EPackageRegistryImpl primary = new EPackageRegistryImpl();
		primary.put(NS, primaryPkg);

		FakeProvider remote = new FakeProvider();
		remote.packages.put(NS, demoPackage()); // a different instance

		AtlasDelegatingPackageRegistry registry = new AtlasDelegatingPackageRegistry(primary, remote);

		assertSame(primaryPkg, registry.getEPackage(NS), "local package must win");
		assertEquals(0, remote.ensureCalls.get(), "remote must not be consulted when local resolves");
	}

	@Test
	void fallsBackToRemote_andCachesForDirectHits() {
		EPackage remotePkg = demoPackage();
		FakeProvider remote = new FakeProvider();
		remote.packages.put(NS, remotePkg);

		AtlasDelegatingPackageRegistry registry = new AtlasDelegatingPackageRegistry(new EPackageRegistryImpl(),
				remote);

		assertSame(remotePkg, registry.getEPackage(NS));
		assertSame(remotePkg, registry.getEPackage(NS));
		assertEquals(1, remote.ensureCalls.get(), "second look-up is a direct hit, no second fetch");
	}

	@Test
	void unknownNsUri_returnsNull() {
		AtlasDelegatingPackageRegistry registry = new AtlasDelegatingPackageRegistry(new EPackageRegistryImpl(),
				new FakeProvider());
		assertNull(registry.getEPackage("urn:atlas:test:nope"));
	}

	@Test
	void getEFactory_fallsBackToFetchedPackage() {
		EPackage remotePkg = demoPackage();
		FakeProvider remote = new FakeProvider();
		remote.packages.put(NS, remotePkg);

		AtlasDelegatingPackageRegistry registry = new AtlasDelegatingPackageRegistry(new EPackageRegistryImpl(),
				remote);

		assertSame(remotePkg.getEFactoryInstance(), registry.getEFactory(NS));
	}

	@Test
	void driftEvicts_soNextLookupRefetches() {
		EPackage remotePkg = demoPackage();
		FakeProvider remote = new FakeProvider();
		remote.packages.put(NS, remotePkg);

		AtlasDelegatingPackageRegistry registry = new AtlasDelegatingPackageRegistry(new EPackageRegistryImpl(),
				remote);

		registry.getEPackage(NS); // fetch + cache (1)
		registry.getEPackage(NS); // direct hit (still 1)
		assertEquals(1, remote.ensureCalls.get());

		registry.onPackageChanged(NS, remotePkg); // drift → evict
		registry.getEPackage(NS); // must re-fetch (2)
		assertEquals(2, remote.ensureCalls.get());

		registry.onPackageRemoved(NS); // drift → evict
		registry.getEPackage(NS); // re-fetch again (3)
		assertEquals(3, remote.ensureCalls.get());
	}

	@Test
	void loadsXmiInstanceReferencingUnknownNsUri() {
		EPackage pkg = demoPackage();
		byte[] instanceXmi = serializeInstance(pkg, "hello");

		FakeProvider remote = new FakeProvider();
		remote.packages.put(NS, pkg); // the Atlas "has" the package

		// A ResourceSet wired exactly like newResourceSet() does, but with a fake provider.
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		resourceSet.setPackageRegistry(new AtlasDelegatingPackageRegistry(EPackage.Registry.INSTANCE, remote));

		Resource resource = resourceSet.createResource(URI.createURI("instance.xmi"));
		try {
			resource.load(new ByteArrayInputStream(instanceXmi), Map.of());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		EObject root = resource.getContents().get(0);
		assertEquals("Item", root.eClass().getName(), "nsURI resolved through the Atlas fallback");
		assertEquals("hello", root.eGet(root.eClass().getEStructuralFeature("label")));
	}

	/** Serialize a single {@code Item} instance of {@code pkg} to XMI bytes. */
	private static byte[] serializeInstance(EPackage pkg, String label) {
		EClass itemClass = (EClass) pkg.getEClassifier("Item");
		EObject item = pkg.getEFactoryInstance().create(itemClass);
		item.eSet(itemClass.getEStructuralFeature("label"), label);

		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		resourceSet.getPackageRegistry().put(pkg.getNsURI(), pkg);
		Resource resource = resourceSet.createResource(URI.createURI("instance.xmi"));
		resource.getContents().add(item);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			resource.save(out, Map.of());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return out.toByteArray();
	}
}
