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
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import org.eclipse.emf.ecore.EReference;
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

	// ---- P5-6: cross-package references + Jürgen's interdependent-package case ----

	private static final String NS_LIB = "urn:atlas:test:lib/1.0";
	private static final String NS_BOOK = "urn:atlas:test:book/1.0";

	/**
	 * Two interdependent EPackages:
	 * <ul>
	 * <li>{@code lib.Library} has a containment {@code entries} (0..*) typed to the
	 * <em>abstract</em> {@code lib.Entry}, and a {@code flagship} reference typed to
	 * {@code book.Book} (lib → book);</li>
	 * <li>{@code book.Book} {@code extends lib.Entry} and has a {@code shelf} reference back
	 * to {@code lib.Library} (book → lib).</li>
	 * </ul>
	 * Because {@code entries} is declared as the base {@code Entry} but holds a concrete
	 * {@code Book}, the serialized instance carries an {@code xsi:type="book:Book"} — so the
	 * loader must resolve {@code NS_BOOK} <em>through the package registry</em> (not via the
	 * in-memory metamodel graph), which is exactly the path Jürgen's unload case exercises.
	 *
	 * @return {@code [libPackage, bookPackage]}
	 */
	private static EPackage[] interdependentPackages() {
		EcoreFactory f = EcoreFactory.eINSTANCE;
		EPackage lib = f.createEPackage();
		lib.setName("lib");
		lib.setNsPrefix("lib");
		lib.setNsURI(NS_LIB);
		EPackage book = f.createEPackage();
		book.setName("book");
		book.setNsPrefix("book");
		book.setNsURI(NS_BOOK);

		EClass entry = f.createEClass();
		entry.setName("Entry");
		entry.setAbstract(true);
		lib.getEClassifiers().add(entry);
		EClass library = f.createEClass();
		library.setName("Library");
		lib.getEClassifiers().add(library);
		EClass bookClass = f.createEClass();
		bookClass.setName("Book");
		bookClass.getESuperTypes().add(entry); // book -> lib (subtype)
		book.getEClassifiers().add(bookClass);

		EAttribute title = f.createEAttribute();
		title.setName("title");
		title.setEType(EcorePackage.eINSTANCE.getEString());
		bookClass.getEStructuralFeatures().add(title);

		EReference entries = f.createEReference();
		entries.setName("entries");
		entries.setContainment(true);
		entries.setUpperBound(-1);
		entries.setEType(entry); // declared as the base type -> forces xsi:type on a Book
		library.getEStructuralFeatures().add(entries);

		EReference flagship = f.createEReference();
		flagship.setName("flagship");
		flagship.setEType(bookClass); // lib -> book (interdependent)
		library.getEStructuralFeatures().add(flagship);

		EReference shelf = f.createEReference();
		shelf.setName("shelf");
		shelf.setEType(library); // book -> lib
		bookClass.getEStructuralFeatures().add(shelf);
		return new EPackage[] { lib, book };
	}

	/**
	 * A {@code Library} containing one {@code Book} (a different package, held under the
	 * base-typed {@code entries}), cross-referenced by {@code flagship} and pointing back via
	 * {@code shelf}; serialized to XMI.
	 */
	private static byte[] libraryWithBookXmi(EPackage lib, EPackage book) {
		EClass library = (EClass) lib.getEClassifier("Library");
		EClass bookClass = (EClass) book.getEClassifier("Book");
		EObject libraryObj = lib.getEFactoryInstance().create(library);
		EObject bookObj = book.getEFactoryInstance().create(bookClass);
		bookObj.eSet(bookClass.getEStructuralFeature("title"), "Dune");
		bookObj.eSet(bookClass.getEStructuralFeature("shelf"), libraryObj); // book -> lib
		@SuppressWarnings("unchecked")
		List<EObject> entries = (List<EObject>) libraryObj.eGet(library.getEStructuralFeature("entries"));
		entries.add(bookObj);
		libraryObj.eSet(library.getEStructuralFeature("flagship"), bookObj); // lib -> book

		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		rs.getPackageRegistry().put(NS_LIB, lib);
		rs.getPackageRegistry().put(NS_BOOK, book);
		Resource resource = rs.createResource(URI.createURI("library.xmi"));
		resource.getContents().add(libraryObj);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			resource.save(out, Map.of());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return out.toByteArray();
	}

	/** An Atlas-aware {@link ResourceSet}, wired exactly as {@code ModelAtlasClientImpl} does. */
	private static ResourceSet atlasResourceSet(AtlasDelegatingPackageRegistry registry) {
		ResourceSet rs = new ResourceSetImpl();
		rs.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		rs.setPackageRegistry(registry);
		return rs;
	}

	private static EObject load(ResourceSet rs, byte[] xmi, String uri) {
		Resource resource = rs.createResource(URI.createURI(uri));
		try {
			resource.load(new ByteArrayInputStream(xmi), Map.of());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return resource.getContents().get(0);
	}

	@SuppressWarnings("unchecked")
	private static EObject firstEntry(EObject library) {
		return ((List<EObject>) library.eGet(library.eClass().getEStructuralFeature("entries"))).get(0);
	}

	@Test
	void loadsInstanceWithCrossPackageReference_resolvingBothPackagesRemotely() {
		EPackage[] pkgs = interdependentPackages();
		byte[] xmi = libraryWithBookXmi(pkgs[0], pkgs[1]);

		FakeProvider remote = new FakeProvider();
		remote.packages.put(NS_LIB, pkgs[0]);
		remote.packages.put(NS_BOOK, pkgs[1]); // the Atlas "has" both packages

		AtlasDelegatingPackageRegistry registry = new AtlasDelegatingPackageRegistry(new EPackageRegistryImpl(), remote);
		EObject library = load(atlasResourceSet(registry), xmi, "library.xmi");

		// Root metamodel resolved remotely.
		assertEquals("Library", library.eClass().getName());
		assertEquals(NS_LIB, library.eClass().getEPackage().getNsURI());

		// The contained object's metamodel is a *different* package, also fetched from the Atlas.
		EObject book = firstEntry(library);
		assertEquals(NS_BOOK, book.eClass().getEPackage().getNsURI());
		assertEquals("Dune", book.eGet(book.eClass().getEStructuralFeature("title")));

		// Cross-references resolve through the Atlas-aware ResourceSet, in both directions:
		// book -> library (other package -> this) and library -> book (this -> other package).
		assertSame(library, book.eGet(book.eClass().getEStructuralFeature("shelf")));
		assertSame(book, library.eGet(library.eClass().getEStructuralFeature("flagship")));
	}

	@Test
	void interdependentPackages_reResolveAfterUnload_viaTheRootedRegistry() {
		EPackage[] pkgs = interdependentPackages();
		byte[] xmi = libraryWithBookXmi(pkgs[0], pkgs[1]);

		FakeProvider remote = new FakeProvider();
		remote.packages.put(NS_LIB, pkgs[0]);
		remote.packages.put(NS_BOOK, pkgs[1]);

		AtlasDelegatingPackageRegistry registry = new AtlasDelegatingPackageRegistry(new EPackageRegistryImpl(), remote);
		ResourceSet rs = atlasResourceSet(registry);

		// First load roots both packages — one fetch each.
		EObject first = load(rs, xmi, "library1.xmi");
		assertEquals(NS_BOOK, firstEntry(first).eClass().getEPackage().getNsURI());
		assertEquals(2, remote.ensureCalls.get());

		// Jürgen's case: the dependent package is unloaded (drift removal).
		registry.onPackageRemoved(NS_BOOK);

		// A subsequent resolution re-fetches NS_BOOK and re-roots it; NS_LIB stays cached.
		EObject second = load(rs, xmi, "library2.xmi");
		EObject book = firstEntry(second);
		assertNotNull(book.eClass().getEPackage());
		assertEquals(NS_BOOK, book.eClass().getEPackage().getNsURI());
		assertEquals(3, remote.ensureCalls.get(), "only NS_BOOK was re-fetched after unload");
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
