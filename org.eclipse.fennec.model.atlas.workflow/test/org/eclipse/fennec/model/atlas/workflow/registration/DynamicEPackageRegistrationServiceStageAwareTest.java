/*
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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.workflow.registration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;

import java.util.Dictionary;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceImpl;
import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;
import org.eclipse.fennec.emf.osgi.fingerprint.FingerprintService;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.framework.BundleContext;

/**
 * Unit tests for the stage-aware registration (D8 change A) and reload serialization
 * (change C) of {@link DynamicEPackageRegistrationService}.
 *
 * <p>The service is exercised against a mocked {@link BundleContext} (real OSGi service
 * publication is not the subject here). The invariant under test is the <em>dedup key</em>:
 * the same {@code nsURI} must register once per {@code scope+stage}, so a schema present on
 * several git branches (= stages) is not collapsed to a single stage as it was when the key
 * was the nsURI alone.
 */
@ExtendWith(MockitoExtension.class)
public class DynamicEPackageRegistrationServiceStageAwareTest {

	private static final String NS_URI = "http://example.org/person/1.0";
	private static final String SCOPE = "git_scope";

	// RETURNS_MOCKS: every service publication yields a mock ServiceRegistration and
	// getServiceReferences yields an empty collection, so no explicit stubbing is needed
	// (and we avoid the ambiguous registerService(Class,…) overload at the call site).
	@Mock(answer = Answers.RETURNS_MOCKS)
	BundleContext bundleContext;

	private DynamicEPackageRegistrationService service;

	/**
	 * Deterministic, content-sensitive fake: the value changes with the number of
	 * classifiers, so tests can produce "same content" and "changed content"
	 * without depending on the real emf.osgi implementation bundle.
	 */
	private static final FingerprintService FAKE_FINGERPRINTS = new FingerprintService() {

		@Override
		public String fingerprint(EPackage ePackage, String... derivationInputs) {
			return "fake:" + ePackage.getNsURI() + ":" + ePackage.getEClassifiers().size()
					+ (derivationInputs.length == 0 ? "" : ":" + String.join("|", derivationInputs));
		}

		@Override
		public String currentScheme() {
			return "fake";
		}

		@Override
		public Set<String> supportedSchemes() {
			return Set.of("fake");
		}

		@Override
		public String fingerprintInScheme(String scheme, EPackage ePackage, String... derivationInputs) {
			return fingerprint(ePackage, derivationInputs);
		}
	};

	@BeforeEach
	public void setup() {
		service = new DynamicEPackageRegistrationService();
		service.fingerprintService = FAKE_FINGERPRINTS;
		service.activate(bundleContext);
	}

	@Test
	@DisplayName("Same nsURI in two different stages both register (not deduped by nsURI alone)")
	public void sameNsUriDifferentStagesBothRegister() {
		assertTrue(service.registerEPackage(newPersonPackage(), metadata(SCOPE, "draft")),
				"draft registration should succeed");
		assertTrue(service.registerEPackage(newPersonPackage(), metadata(SCOPE, "release")),
				"release registration should succeed even though the nsURI is already registered for draft");

		assertEquals(2, service.getRegisteredCount(), "both stage registrations should be tracked");
		assertTrue(service.isRegistered(NS_URI), "nsURI should be registered in at least one stage");
		assertTrue(service.isRegistered(SCOPE, "draft", NS_URI));
		assertTrue(service.isRegistered(SCOPE, "release", NS_URI));
		assertFalse(service.isRegistered(SCOPE, "approved", NS_URI), "a stage that never registered is not present");
	}

	@Test
	@DisplayName("A genuine duplicate (same scope+stage+nsURI) is still rejected")
	public void exactDuplicateRejected() {
		assertTrue(service.registerEPackage(newPersonPackage(), metadata(SCOPE, "draft")));
		assertFalse(service.registerEPackage(newPersonPackage(), metadata(SCOPE, "draft")),
				"re-registering the exact same scope+stage+nsURI should be a no-op");
		assertEquals(1, service.getRegisteredCount());
	}

	@Test
	@DisplayName("Unregistering one stage leaves the same nsURI registered for the other")
	public void unregisterOneStageLeavesOther() {
		service.registerEPackage(newPersonPackage(), metadata(SCOPE, "draft"));
		service.registerEPackage(newPersonPackage(), metadata(SCOPE, "release"));

		assertTrue(service.unregisterEPackage(SCOPE, "draft", NS_URI), "draft unregistration should succeed");

		assertFalse(service.isRegistered(SCOPE, "draft", NS_URI), "draft must be gone");
		assertTrue(service.isRegistered(SCOPE, "release", NS_URI), "release must survive");
		assertTrue(service.isRegistered(NS_URI), "nsURI still registered (via release)");
		assertEquals(1, service.getRegisteredCount());
	}

	@Test
	@DisplayName("Changed content at the same location replaces the stale registration")
	public void changedContentSameLocationReplaces() {
		assertTrue(service.registerEPackage(newPersonPackage(), metadata(SCOPE, "draft")));
		assertEquals(1, service.getRegisteredCount());

		// Same scope/stage/nsURI but different content -> different fingerprint. Before
		// the fingerprint-aware key this was silently rejected, leaving stale services up.
		assertTrue(service.registerEPackage(newPersonPackageWithClass("Extra"), metadata(SCOPE, "draft")),
				"changed content at the same location must replace, not be rejected");
		assertEquals(1, service.getRegisteredCount(), "replace must not leave two registrations for one location");
		assertTrue(service.isRegistered(SCOPE, "draft", NS_URI));

		// And the replacement is itself idempotent for identical content
		assertFalse(service.registerEPackage(newPersonPackageWithClass("Extra"), metadata(SCOPE, "draft")),
				"re-registering the identical changed content is a no-op");
		assertEquals(1, service.getRegisteredCount());
	}

	@Test
	@DisplayName("Registered services carry the emf.fingerprint service property")
	public void registeredServicesCarryFingerprintProperty() {
		EPackage pkg = newPersonPackage();
		String expected = FAKE_FINGERPRINTS.fingerprint(pkg);
		assertTrue(service.registerEPackage(pkg, metadata(SCOPE, "draft")));

		@SuppressWarnings({ "unchecked", "rawtypes" })
		ArgumentCaptor<Dictionary<String, ?>> props = ArgumentCaptor.forClass((Class) Dictionary.class);
		// EPackage and EFactory services are registered via the String[] overload
		verify(bundleContext, atLeastOnce()).registerService(any(String[].class), any(), props.capture());
		for (Dictionary<String, ?> dict : props.getAllValues()) {
			assertEquals(expected, dict.get(EMFNamespaces.EMF_MODEL_FINGERPRINT),
					"every registration must carry the computed emf.fingerprint property");
		}
	}

	@Test
	@DisplayName("Fingerprint-exact unregistration removes only a matching content version")
	public void unregisterWithFingerprintMatchesExactly() {
		EPackage pkg = newPersonPackage();
		String fp = FAKE_FINGERPRINTS.fingerprint(pkg);
		assertTrue(service.registerEPackage(pkg, metadata(SCOPE, "draft")));

		assertFalse(service.unregisterEPackage(SCOPE, "draft", NS_URI, "fake:wrong"),
				"a non-matching fingerprint must not unregister the location");
		assertTrue(service.isRegistered(SCOPE, "draft", NS_URI));

		assertTrue(service.unregisterEPackage(SCOPE, "draft", NS_URI, fp),
				"the matching fingerprint unregisters the location");
		assertFalse(service.isRegistered(SCOPE, "draft", NS_URI));
	}

	@Test
	@DisplayName("Concurrent registrations of the same nsURI across distinct stages are all retained")
	public void concurrentRegistrationsAcrossStagesAreSerialized() throws Exception {
		int stages = 8;
		ExecutorService pool = Executors.newFixedThreadPool(stages);
		CountDownLatch start = new CountDownLatch(1);
		AtomicInteger succeeded = new AtomicInteger();
		try {
			for (int i = 0; i < stages; i++) {
				String stage = "stage-" + i;
				pool.submit(() -> {
					try {
						start.await();
						if (service.registerEPackage(newPersonPackage(), metadata(SCOPE, stage))) {
							succeeded.incrementAndGet();
						}
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
					}
					return null;
				});
			}
			start.countDown();
			pool.shutdown();
			assertTrue(pool.awaitTermination(10, TimeUnit.SECONDS), "registrations should finish");
		} finally {
			pool.shutdownNow();
		}
		assertEquals(stages, succeeded.get(), "every distinct stage should register exactly once");
		assertEquals(stages, service.getRegisteredCount(), "the lock must not drop any registration");
	}

	// --- helpers ------------------------------------------------------------

	/** A fresh {@code person} EPackage (same nsURI each time) held in its own resource. */
	private static EPackage newPersonPackage() {
		EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
		pkg.setName("person");
		pkg.setNsPrefix("person");
		pkg.setNsURI(NS_URI);
		// The service needs a non-null eResource(); a bare XMIResourceImpl (no ResourceSet)
		// suffices and avoids needing a registered resource factory.
		Resource resource = new XMIResourceImpl(URI.createURI("person.ecore"));
		resource.getContents().add(pkg);
		return pkg;
	}

	/** Same nsURI, but with an extra EClass — "changed content" for the fake fingerprint. */
	private static EPackage newPersonPackageWithClass(String className) {
		EPackage pkg = newPersonPackage();
		EClass eClass = EcoreFactory.eINSTANCE.createEClass();
		eClass.setName(className);
		pkg.getEClassifiers().add(eClass);
		return pkg;
	}

	private static ObjectMetadata metadata(String scope, String stage) {
		ObjectMetadata md = ManagementFactory.eINSTANCE.createObjectMetadata();
		md.setScope(scope);
		md.setStage(stage);
		return md;
	}
}
