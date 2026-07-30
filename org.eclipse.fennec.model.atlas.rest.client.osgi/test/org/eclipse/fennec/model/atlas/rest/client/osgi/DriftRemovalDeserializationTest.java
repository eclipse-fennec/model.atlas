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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.PackageNotFoundException;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.model.atlas.rest.client.api.RemoteEPackageProvider;
import org.eclipse.fennec.model.atlas.rest.client.api.ResolvedEPackage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * End-to-end consequence of a drift-detected removal: content of a package that the
 * Atlas no longer serves must stop deserializing on the client.
 * <p>
 * The chain under test is {@link DriftSubstitution#onPackageRemoved(String)} →
 * unpublish (the publisher revokes the trio, emf.osgi unbinds the configurator, the
 * package leaves the framework registry and the publisher's current-publication map) →
 * a subsequent load through the {@link LazyResolvingPackageRegistry} misses every
 * layer, legitimately re-asks the Atlas, gets nothing, and EMF fails the load with a
 * {@link PackageNotFoundException}. The individual layers are covered by
 * {@link DriftSubstitutionTest}, {@code DriftWatcherTest} and
 * {@link LazyResolvingPackageRegistryTest}; this test pins the consumer-visible
 * outcome so no stale layer (framework registry, published-lookup, own entries)
 * can keep a removed package deserializable by accident.
 */
class DriftRemovalDeserializationTest {

	private static final String NS = "urn:test:drift.removal";
	private static final long FAST_TIMEOUT_MS = 2_000L;

	private static final String PAYLOAD = """
			<?xml version="1.0" encoding="UTF-8"?>
			<p:Thing xmlns:p="%s" name="hello"/>
			""".formatted(NS);

	private EPackageRegistryImpl framework;
	private RemoteEPackageProvider remote;
	/** The publisher's current publication per nsURI (P3-9 atomic-read seam). */
	private final Map<String, EPackage> currentPublications = new ConcurrentHashMap<>();

	private LazyResolvingPackageRegistry lazyRegistry;
	private DriftSubstitution substitution;

	@BeforeEach
	void setUp() {
		framework = new EPackageRegistryImpl();
		remote = mock(RemoteEPackageProvider.class);

		// Publish seam: like RemoteEPackagePublisher + emf.osgi, a publish makes the
		// package observable in the framework registry and in the publisher's
		// current-publication map; an unpublish removes it from both.
		PackagePublication publication = (ePackage, scope, stage, version) -> {
			currentPublications.put(ePackage.getNsURI(), ePackage);
			framework.put(ePackage.getNsURI(), ePackage);
			return true;
		};
		java.util.function.Consumer<String> unpublisher = nsUri -> {
			currentPublications.remove(nsUri);
			framework.remove(nsUri);
		};

		lazyRegistry = new LazyResolvingPackageRegistry(framework, remote, publication,
				currentPublications::get, FAST_TIMEOUT_MS, 1L, System::currentTimeMillis, Thread::sleep);
		substitution = new DriftSubstitution(currentPublications::containsKey, currentPublications::keySet,
				ns -> remote.resolve(ns), publication, unpublisher);
	}

	/** A minimal dynamic model: EClass "Thing" with a String attribute "name". */
	private static EPackage thingPackage() {
		EAttribute name = EcoreFactory.eINSTANCE.createEAttribute();
		name.setName("name");
		name.setEType(EcorePackage.Literals.ESTRING);
		EClass thing = EcoreFactory.eINSTANCE.createEClass();
		thing.setName("Thing");
		thing.getEStructuralFeatures().add(name);
		EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
		pkg.setName("p");
		pkg.setNsPrefix("p");
		pkg.setNsURI(NS);
		pkg.getEClassifiers().add(thing);
		return pkg;
	}

	/** Deserialize the payload through a fresh ResourceSet backed by the lazy registry. */
	private EObject load() throws IOException {
		ResourceSetImpl resourceSet = new ResourceSetImpl();
		resourceSet.setPackageRegistry(lazyRegistry);
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		Resource resource = resourceSet.createResource(URI.createURI("drift-removal-test.xmi"));
		resource.load(new ByteArrayInputStream(PAYLOAD.getBytes(StandardCharsets.UTF_8)), Map.of());
		return resource.getContents().get(0);
	}

	@Test
	void removalDetectedByDriftCheckMakesContentOfThatPackageUndeserializable() throws IOException {
		EPackage served = thingPackage();
		when(remote.resolve(NS))
				.thenReturn(Optional.of(new ResolvedEPackage(served, NS, "atlas", "schema", "released", "1.0")));

		// Before: the payload deserializes — the lazy registry fetches, publishes, resolves.
		EObject before = load();
		assertEquals("Thing", before.eClass().getName());
		assertEquals("hello", before.eGet(before.eClass().getEStructuralFeature("name")));
		verify(remote, times(1)).resolve(NS);

		// The EPackage is deleted on the server; the next drift check reports it removed.
		when(remote.resolve(NS)).thenReturn(Optional.empty());
		substitution.onPackageRemoved(NS);

		// No layer still serves the package: not the framework registry, not the
		// publisher's current publication, and a re-ask of the Atlas legitimately misses.
		assertNull(framework.getEPackage(NS), "framework registry must no longer serve the removed package");
		assertNull(lazyRegistry.getEPackage(NS), "lazy registry must not resurrect the removed package");

		// After: the exact same payload no longer deserializes.
		IOException failure = assertThrows(IOException.class, this::load);
		assertInstanceOf(PackageNotFoundException.class, failure.getCause(),
				"the load must fail because the package is unknown, not for some other reason");
		assertEquals(NS, ((PackageNotFoundException) failure.getCause()).uri());
	}

	@Test
	void changedThenGoneAlsoMakesContentUndeserializable() throws IOException {
		EPackage served = thingPackage();
		when(remote.resolve(NS))
				.thenReturn(Optional.of(new ResolvedEPackage(served, NS, "atlas", "schema", "released", "1.0")));
		EObject before = load();
		assertEquals("Thing", before.eClass().getName());

		// Drift reports a *change*, but the re-resolve finds the package gone (deleted
		// between the HEAD and the refetch) — the substitution must unpublish, same outcome.
		when(remote.resolve(NS)).thenReturn(Optional.empty());
		substitution.onPackageChanged(NS, served);

		assertNull(lazyRegistry.getEPackage(NS));
		IOException failure = assertThrows(IOException.class, this::load);
		assertInstanceOf(PackageNotFoundException.class, failure.getCause());
	}
}
