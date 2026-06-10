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

	@Test
	void doesNotTouchTheGlobalRegistryWhenNotConfigured() {
		EPackageRegistryImpl global = new EPackageRegistryImpl();
		// No global registry supplied → the singleton (here, our spy) must stay untouched.
		new RemoteEPackagePublisher(bundleContext, "http://atlas.test/atlas/rest")
				.publish(ePackage("urn:none"), "jena", "released", "1.0");

		assertNull(global.getEPackage("urn:none"));
	}
}
