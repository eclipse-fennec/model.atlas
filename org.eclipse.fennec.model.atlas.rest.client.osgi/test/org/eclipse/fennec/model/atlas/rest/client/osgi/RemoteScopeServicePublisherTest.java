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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.scope.api.AtlasProperties;
import org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;

/**
 * Unit tests for the P5-4 per-scope {@link ReadableScopeService} publication. The
 * {@link BundleContext} is mocked and the property {@link Dictionary} passed to
 * {@code registerService} is captured to assert the {@code atlas.*} contract a consumer
 * filters on.
 */
class RemoteScopeServicePublisherTest {

	private static final String BASE_URI = "http://atlas.test/atlas/rest";

	private final BundleContext bundleContext = mock(BundleContext.class);
	/** Registrations, in registration order (one per publish/republish), so a swap's revocation is verifiable. */
	private final List<ServiceRegistration<?>> registrations = new ArrayList<>();

	@SuppressWarnings({ "unchecked", "rawtypes" })
	RemoteScopeServicePublisherTest() {
		// any(ReadableScopeService.class) for the value disambiguates the (Class,S,Dictionary)
		// overload from the (Class,ServiceFactory,Dictionary) one.
		when(bundleContext.registerService(eq(ReadableScopeService.class), any(ReadableScopeService.class),
				any(Dictionary.class))).thenAnswer(invocation -> {
					ServiceRegistration reg = mock(ServiceRegistration.class);
					registrations.add(reg);
					return reg;
				});
	}

	@SuppressWarnings("unchecked")
	private static ReadableScopeService<EObject> scopeService() {
		return mock(ReadableScopeService.class);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Dictionary<String, Object> lastCapturedProps() {
		ArgumentCaptor<Dictionary> props = ArgumentCaptor.forClass(Dictionary.class);
		verify(bundleContext, org.mockito.Mockito.atLeastOnce()).registerService(eq(ReadableScopeService.class),
				any(ReadableScopeService.class), props.capture());
		return props.getValue();
	}

	@Test
	void publishesWithTheAtlasScopeContract() {
		RemoteScopeServicePublisher publisher = new RemoteScopeServicePublisher(bundleContext, BASE_URI);

		assertTrue(publisher.publish("jena", scopeService()));

		Dictionary<String, Object> props = lastCapturedProps();
		// (atlas.scope=jena) is the collector key consumers target.
		assertEquals("jena", props.get(AtlasProperties.ATLAS_SCOPE));
		assertEquals(Boolean.TRUE, props.get(AtlasProperties.ATLAS_REMOTE));
		assertEquals(BASE_URI, props.get(AtlasProperties.ATLAS_BASE_URI));
		
	}

	@Test
	void publishIsIdempotentPerScope() {
		RemoteScopeServicePublisher publisher = new RemoteScopeServicePublisher(bundleContext, BASE_URI);

		assertTrue(publisher.publish("jena", scopeService()));
		assertFalse(publisher.publish("jena", scopeService()), "second publish of the same scope is a no-op");
		assertEquals(1, registrations.size(), "only one registration for the scope");
		assertTrue(publisher.isPublished("jena"));
		assertEquals(java.util.Set.of("jena"), publisher.publishedScopes());
	}

	@Test
	void republishSwapsTheServiceAndRevokesTheOld() {
		RemoteScopeServicePublisher publisher = new RemoteScopeServicePublisher(bundleContext, BASE_URI);
		publisher.publish("jena", scopeService());

		boolean replaced = publisher.republish("jena", scopeService());

		assertTrue(replaced);
		// The first (old) registration is revoked; the second (new) is not.
		verify(registrations.get(0)).unregister();
		verify(registrations.get(1), never()).unregister();
	}

	@Test
	void republishWithNothingPublishedActsAsAFreshPublish() {
		RemoteScopeServicePublisher publisher = new RemoteScopeServicePublisher(bundleContext, BASE_URI);

		boolean replaced = publisher.republish("fresh", scopeService());

		assertFalse(replaced, "nothing was replaced");
		assertTrue(publisher.isPublished("fresh"));
	}

	@Test
	void unpublishRevokesTheService() {
		RemoteScopeServicePublisher publisher = new RemoteScopeServicePublisher(bundleContext, BASE_URI);
		publisher.publish("jena", scopeService());

		assertTrue(publisher.unpublish("jena"));
		assertFalse(publisher.isPublished("jena"));
		verify(registrations.get(0)).unregister();
		assertFalse(publisher.unpublish("jena"), "second unpublish is a no-op");
	}

	@Test
	void unpublishAllRevokesEveryService() {
		RemoteScopeServicePublisher publisher = new RemoteScopeServicePublisher(bundleContext, BASE_URI);
		publisher.publish("jena", scopeService());
		publisher.publish("cocl", scopeService());

		publisher.unpublishAll();

		assertTrue(publisher.publishedScopes().isEmpty());
		registrations.forEach(reg -> verify(reg).unregister());
	}

	@Test
	void rejectsABlankScopeName() {
		RemoteScopeServicePublisher publisher = new RemoteScopeServicePublisher(bundleContext, BASE_URI);

		assertFalse(publisher.publish("  ", scopeService()));
		assertTrue(registrations.isEmpty(), "nothing registered for a blank scope name");
	}

	// ---- P6-7: atlas.stage disambiguation label --------------------------------

	@Test
	void publishWithStage_stampsAtlasStageProperty() {
		RemoteScopeServicePublisher publisher = new RemoteScopeServicePublisher(bundleContext, BASE_URI, "snapshot");

		publisher.publish("jena", scopeService());

		Dictionary<String, Object> props = lastCapturedProps();
		assertEquals("jena", props.get(AtlasProperties.ATLAS_SCOPE));
		assertEquals("snapshot", props.get(AtlasProperties.ATLAS_STAGE),
				"atlas.stage must be stamped for consumer disambiguation");
		assertEquals(Boolean.TRUE, props.get(AtlasProperties.ATLAS_REMOTE));
	}

	@Test
	void publishWithoutStage_omitsAtlasStageProperty() {
		RemoteScopeServicePublisher publisher = new RemoteScopeServicePublisher(bundleContext, BASE_URI);

		publisher.publish("jena", scopeService());

		Dictionary<String, Object> props = lastCapturedProps();
		assertEquals(null, props.get(AtlasProperties.ATLAS_STAGE),
				"stage-free publisher must not stamp atlas.stage");
	}
}
