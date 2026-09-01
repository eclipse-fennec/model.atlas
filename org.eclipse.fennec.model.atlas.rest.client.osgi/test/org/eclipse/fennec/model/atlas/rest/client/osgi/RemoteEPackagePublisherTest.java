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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.List;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.fennec.emf.osgi.configurator.EPackageConfigurator;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

/**
 * Unit tests for the P3-8 {@code service.ranking} stamping. The {@link BundleContext}
 * is mocked and the property {@link Dictionary} passed to {@code registerService} is
 * captured to assert the ranking.
 */
class RemoteEPackagePublisherTest {

	private final BundleContext bundleContext = mock(BundleContext.class);
	/** Configurator-service registrations, in registration order (one per publish/republish). */
	private final List<ServiceRegistration<?>> configuratorRegs = new ArrayList<>();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	RemoteEPackagePublisherTest() {
		// any(EPackageConfigurator.class) for the value disambiguates the (Class,S,Dictionary)
		// overload from the (Class,ServiceFactory,Dictionary) one. A fresh registration mock per
		// call lets a swap's revocation of the OLD configurator be verified.
		when(bundleContext.registerService(eq(EPackageConfigurator.class), any(EPackageConfigurator.class),
				any(Dictionary.class))).thenAnswer(invocation -> {
					ServiceRegistration reg = mock(ServiceRegistration.class);
					configuratorRegs.add(reg);
					return reg;
				});
		when(bundleContext.registerService(any(String[].class), any(), any(Dictionary.class)))
				.thenAnswer(invocation -> mock(ServiceRegistration.class));
	}

	private static EPackage ePackage(String nsUri) {
		EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
		pkg.setName("p");
		pkg.setNsPrefix("p");
		pkg.setNsURI(nsUri);
		return pkg;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Dictionary<String, Object> capturedConfiguratorProps() {
		ArgumentCaptor<Dictionary> props = ArgumentCaptor.forClass(Dictionary.class);
		org.mockito.Mockito.verify(bundleContext).registerService(eq(EPackageConfigurator.class),
				any(EPackageConfigurator.class), props.capture());
		return props.getValue();
	}

	@Test
	void stampsServiceRankingWhenForced() {
		new RemoteEPackagePublisher(bundleContext, "http://atlas.test/atlas/rest", 1000)
				.publish(ePackage("urn:x"), "jena", "released", "1.0");

		assertEquals(Integer.valueOf(1000), capturedConfiguratorProps().get(Constants.SERVICE_RANKING));
	}

	@Test
	void noServiceRankingByDefault() {
		new RemoteEPackagePublisher(bundleContext, "http://atlas.test/atlas/rest")
				.publish(ePackage("urn:y"), "jena", "released", "1.0");

		assertNull(capturedConfiguratorProps().get(Constants.SERVICE_RANKING));
	}

	// ---- P3-9 atomic republish / publishedEPackage -----------------------

	@Test
	void publishedEPackageTracksThePublishedPackage() {
		RemoteEPackagePublisher publisher = new RemoteEPackagePublisher(bundleContext, "http://atlas.test/atlas/rest");
		EPackage pkg = ePackage("urn:x");

		assertNull(publisher.publishedEPackage("urn:x"));
		publisher.publish(pkg, "jena", "released", "1.0");
		assertSame(pkg, publisher.publishedEPackage("urn:x"));

		assertTrue(publisher.unpublish("urn:x"));
		assertNull(publisher.publishedEPackage("urn:x"));
	}

	@Test
	void republishSwapsToTheNewPackageAndRevokesTheOld() {
		RemoteEPackagePublisher publisher = new RemoteEPackagePublisher(bundleContext, "http://atlas.test/atlas/rest");
		EPackage oldPkg = ePackage("urn:x");
		EPackage newPkg = ePackage("urn:x"); // same nsURI, drifted content

		publisher.publish(oldPkg, "jena", "released", "1.0");
		assertSame(oldPkg, publisher.publishedEPackage("urn:x"));

		boolean replaced = publisher.republish(newPkg, "jena", "released", "2.0");

		assertTrue(replaced);
		assertSame(newPkg, publisher.publishedEPackage("urn:x"));
		// The first (old) configurator registration is revoked; the second (new) is not.
		verify(configuratorRegs.get(0)).unregister();
		verify(configuratorRegs.get(1), never()).unregister();
	}

	@Test
	void republishWithNothingPublishedActsAsAFreshPublish() {
		RemoteEPackagePublisher publisher = new RemoteEPackagePublisher(bundleContext, "http://atlas.test/atlas/rest");
		EPackage pkg = ePackage("urn:fresh");

		boolean replaced = publisher.republish(pkg, "jena", "released", "1.0");

		assertFalse(replaced, "nothing was replaced");
		assertSame(pkg, publisher.publishedEPackage("urn:fresh"));
	}

	// ---- P3-11 mirroring into the (injected) global registry --------------

	@Test
	void mirrorsPublishedPackageIntoTheGlobalRegistryWhenSupplied() {
		EPackageRegistryImpl global = new EPackageRegistryImpl();
		RemoteEPackagePublisher publisher = new RemoteEPackagePublisher(bundleContext, "http://atlas.test/atlas/rest",
				0, global);
		EPackage pkg = ePackage("urn:g");

		publisher.publish(pkg, "jena", "released", "1.0");
		assertSame(pkg, global.getEPackage("urn:g"));

		// A drift swap replaces the singleton entry in step with the service.
		EPackage swapped = ePackage("urn:g");
		publisher.republish(swapped, "jena", "released", "2.0");
		assertSame(swapped, global.getEPackage("urn:g"));

		// Unpublish removes our entry.
		publisher.unpublish("urn:g");
		assertNull(global.getEPackage("urn:g"));
	}

	@Test
	void unpublishAllClearsMirroredEntries() {
		EPackageRegistryImpl global = new EPackageRegistryImpl();
		RemoteEPackagePublisher publisher = new RemoteEPackagePublisher(bundleContext, "http://atlas.test/atlas/rest",
				0, global);
		publisher.publish(ePackage("urn:a"), "jena", "released", "1.0");
		publisher.publish(ePackage("urn:b"), "jena", "released", "1.0");

		publisher.unpublishAll();

		assertNull(global.getEPackage("urn:a"));
		assertNull(global.getEPackage("urn:b"));
	}

	// ---- #227: mirroring must never clobber someone else's registration ----

	/** A stand-in for a generated package's own factory — the type the generated code casts to. */
	private static class GeneratedFactory extends org.eclipse.emf.ecore.impl.EFactoryImpl {
	}

	/** A generated-style package: carries its own EFactory subclass. */
	private static EPackage generatedPackage(String nsUri) {
		EPackage pkg = ePackage(nsUri);
		pkg.setEFactoryInstance(new GeneratedFactory());
		return pkg;
	}

	@Test
	void doesNotOverwriteAGeneratedRegistrationAlreadyInTheGlobalRegistry() {
		// #227: an EAGER sweep of a scope that inherits the package must not replace the
		// generated EPackage with the dynamic one — generated factory init does
		// (GeneratedFactory) Registry.INSTANCE.getEFactory(eNS_URI) and would then CCE.
		EPackageRegistryImpl global = new EPackageRegistryImpl();
		EPackage generated = generatedPackage("urn:gen");
		global.put("urn:gen", generated);
		RemoteEPackagePublisher publisher = new RemoteEPackagePublisher(bundleContext, "http://atlas.test/atlas/rest",
				0, global);

		publisher.publish(ePackage("urn:gen"), "jena", "released", "1.0");

		assertSame(generated, global.getEPackage("urn:gen"), "the generated package must survive the sweep");
		assertInstanceOf(GeneratedFactory.class, global.getEFactory("urn:gen"),
				"the generated EFactory must still be the one the generated code casts to");
	}

	@Test
	void republishDoesNotOverwriteAForeignRegistrationEither() {
		EPackageRegistryImpl global = new EPackageRegistryImpl();
		EPackage generated = generatedPackage("urn:gen");
		global.put("urn:gen", generated);
		RemoteEPackagePublisher publisher = new RemoteEPackagePublisher(bundleContext, "http://atlas.test/atlas/rest",
				0, global);

		publisher.publish(ePackage("urn:gen"), "jena", "released", "1.0");
		publisher.republish(ePackage("urn:gen"), "jena", "released", "2.0"); // a drift swap

		assertSame(generated, global.getEPackage("urn:gen"));
	}

	@Test
	void unpublishLeavesARegistrationItNeverPlaced() {
		// The symmetric defect: removeFromGlobal used to delete whatever sat under the nsURI,
		// including another bundle's generated registration we had merely tried to overwrite.
		EPackageRegistryImpl global = new EPackageRegistryImpl();
		EPackage generated = generatedPackage("urn:gen");
		global.put("urn:gen", generated);
		RemoteEPackagePublisher publisher = new RemoteEPackagePublisher(bundleContext, "http://atlas.test/atlas/rest",
				0, global);
		publisher.publish(ePackage("urn:gen"), "jena", "released", "1.0");

		publisher.unpublish("urn:gen");

		assertSame(generated, global.getEPackage("urn:gen"), "unpublish must not remove a foreign registration");
	}

	@Test
	void unpublishAllLeavesRegistrationsItNeverPlaced() {
		EPackageRegistryImpl global = new EPackageRegistryImpl();
		EPackage generated = generatedPackage("urn:gen");
		global.put("urn:gen", generated);
		RemoteEPackagePublisher publisher = new RemoteEPackagePublisher(bundleContext, "http://atlas.test/atlas/rest",
				0, global);
		publisher.publish(ePackage("urn:gen"), "jena", "released", "1.0"); // skipped, foreign
		publisher.publish(ePackage("urn:ours"), "jena", "released", "1.0"); // mirrored, ours

		publisher.unpublishAll();

		assertSame(generated, global.getEPackage("urn:gen"), "a foreign registration must survive shutdown");
		assertNull(global.getEPackage("urn:ours"), "our own mirror must still be cleaned up");
	}

	@Test
	void doesNotInitializeALazyDescriptorWhileProbing() {
		// Generated packages are often registered as a Descriptor and initialised on first
		// getEPackage(). Probing for an existing registration must not force that — the
		// eager-init race is exactly what makes #227 intermittent.
		EPackageRegistryImpl global = new EPackageRegistryImpl();
		int[] initialisations = { 0 };
		global.put("urn:lazy", new EPackage.Descriptor() {

			@Override
			public EPackage getEPackage() {
				initialisations[0]++;
				return generatedPackage("urn:lazy");
			}

			@Override
			public org.eclipse.emf.ecore.EFactory getEFactory() {
				initialisations[0]++;
				return new GeneratedFactory();
			}
		});
		RemoteEPackagePublisher publisher = new RemoteEPackagePublisher(bundleContext, "http://atlas.test/atlas/rest",
				0, global);

		publisher.publish(ePackage("urn:lazy"), "jena", "released", "1.0");

		assertEquals(0, initialisations[0], "the descriptor must not be resolved just to check for its presence");
	}

	@Test
	void unpublishLeavesAMirrorThatAGeneratedBundleLaterOverwrote() {
		// The other half of the start-up race: our sweep wins the nsURI first, then the
		// generated bundle's static initialiser puts its own package over ours. That entry is
		// no longer the one we placed, so shutting the client down must leave it alone.
		EPackageRegistryImpl global = new EPackageRegistryImpl();
		RemoteEPackagePublisher publisher = new RemoteEPackagePublisher(bundleContext, "http://atlas.test/atlas/rest",
				0, global);
		publisher.publish(ePackage("urn:race"), "jena", "released", "1.0"); // free → mirrored, ours

		EPackage generated = generatedPackage("urn:race");
		global.put("urn:race", generated); // the generated bundle initialises afterwards

		publisher.unpublish("urn:race");

		assertSame(generated, global.getEPackage("urn:race"),
				"a mirror that was overwritten by its generated owner is no longer ours to remove");
	}

	@Test
	void stillMirrorsWhenTheNsUriIsFree() {
		// The feature itself must keep working: a domain-only package with no local
		// counterpart is still mirrored, which is the point of register.in.global.registry.
		EPackageRegistryImpl global = new EPackageRegistryImpl();
		RemoteEPackagePublisher publisher = new RemoteEPackagePublisher(bundleContext, "http://atlas.test/atlas/rest",
				0, global);
		EPackage remote = ePackage("urn:free");

		publisher.publish(remote, "jena", "released", "1.0");

		assertSame(remote, global.getEPackage("urn:free"));
	}

	@Test
	void doesNotTouchTheGlobalRegistryWhenNotConfigured() {
		EPackageRegistryImpl global = new EPackageRegistryImpl();
		// No global registry supplied → the singleton (here, our spy) must stay untouched.
		new RemoteEPackagePublisher(bundleContext, "http://atlas.test/atlas/rest")
				.publish(ePackage("urn:none"), "jena", "released", "1.0");

		assertNull(global.getEPackage("urn:none"));
	}
}
