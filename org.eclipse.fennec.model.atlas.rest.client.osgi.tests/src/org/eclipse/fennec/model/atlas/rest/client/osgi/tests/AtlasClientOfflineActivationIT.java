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
package org.eclipse.fennec.model.atlas.rest.client.osgi.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collection;
import java.util.Hashtable;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.service.cm.Configuration;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * What the OSGi client does at activation when the Atlas is <em>not</em> reachable.
 *
 * <p>
 * {@code mode.strict} is the documented switch for that situation: {@code true} means an
 * unreachable server aborts activation, {@code false} (the default) means the client comes up
 * anyway and resolves later. LAZY mode — also the default — is not supposed to contact the
 * server during activation at all. Together those defaults are what lets a client bundle start
 * before, or without, its Atlas.
 * </p>
 *
 * <p>
 * These tests need no server by design: an unreachable base URI <em>is</em> the fixture, so
 * unlike {@link AtlasClientOsgiIT} they run in every build rather than only where the jena image
 * exists. They assert on the SCR component state rather than on a registered service, because
 * the component registers its {@code ResourceSetConfigurator} <em>before</em> the network call
 * and tears it back down when activation fails — a service-appearance check passes in both
 * outcomes by catching that transient registration.
 * </p>
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class AtlasClientOfflineActivationIT {

	/** Factory PID and component name of the client (see {@code AtlasClientComponent.PID}). */
	private static final String PID = "org.eclipse.fennec.model.atlas.rest.client";

	/** Nothing listens on port 1, so every request fails immediately with connection refused. */
	private static final String UNREACHABLE_URI = "http://127.0.0.1:1/atlas/rest";

	/** Long enough for SCR to create and activate (or fail) the configuration. */
	private static final long SETTLE_TIMEOUT_MS = 15_000L;

	@Test
	public void lazyModeAndNotStrict_activatesWithoutTheServer(
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PID, name = "offline-lazy",
					location = "?")) Configuration configuration,
			@InjectService ServiceComponentRuntime scr) throws Exception {

		configuration.update(offlineProps(false, null));

		assertEquals(ComponentConfigurationDTO.ACTIVE, awaitSettledState(scr, configuration.getPid()),
				"With mode.strict=false the client must activate against an unreachable Atlas: LAZY does no startup "
						+ "fetching, and a failing scope discovery is exactly what mode.strict=false permits");
	}

	@Test
	public void strictMode_failsActivationWithoutTheServer(
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PID,
					name = "offline-strict", location = "?")) Configuration configuration,
			@InjectService ServiceComponentRuntime scr) throws Exception {

		configuration.update(offlineProps(true, null));

		assertEquals(ComponentConfigurationDTO.FAILED_ACTIVATION, awaitSettledState(scr, configuration.getPid()),
				"With mode.strict=true an unreachable Atlas must abort activation");
	}

	@Test
	public void scopeAllowListAndNotStrict_activatesWithoutTheServer(
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PID, name = "offline-allow",
					location = "?")) Configuration configuration,
			@InjectService ServiceComponentRuntime scr) throws Exception {

		// A configured scope list is the path that needs no scope discovery at all; it must keep
		// working whether or not the discovery call is guarded.
		configuration.update(offlineProps(false, new String[] { "someScope" }));

		assertEquals(ComponentConfigurationDTO.ACTIVE, awaitSettledState(scr, configuration.getPid()),
				"A configured scope.allow.list needs no server call, so activation must succeed");
	}

	private static Hashtable<String, Object> offlineProps(boolean strict, String[] scopeAllowList) {
		Hashtable<String, Object> props = new Hashtable<>();
		props.put("base.uri", UNREACHABLE_URI);
		props.put("mode", "LAZY");
		props.put("mode.strict", Boolean.valueOf(strict));
		if (scopeAllowList != null) {
			props.put("scope.allow.list", scopeAllowList);
		}
		return props;
	}

	/**
	 * Waits until SCR reports a terminal state for this configuration's component and returns it.
	 * SATISFIED is transient here — it is what SCR reports between creating the configuration and
	 * running {@code activate} — so only ACTIVE and FAILED_ACTIVATION end the wait.
	 */
	private static int awaitSettledState(ServiceComponentRuntime scr, String servicePid)
			throws InterruptedException {
		long deadline = System.currentTimeMillis() + SETTLE_TIMEOUT_MS;
		int lastSeen = -1;
		while (System.currentTimeMillis() < deadline) {
			Optional<ComponentConfigurationDTO> dto = findConfiguration(scr, servicePid);
			if (dto.isPresent()) {
				lastSeen = dto.get().state;
				if (lastSeen == ComponentConfigurationDTO.ACTIVE
						|| lastSeen == ComponentConfigurationDTO.FAILED_ACTIVATION) {
					return lastSeen;
				}
			}
			Thread.sleep(100L);
		}
		return fail("Component " + PID + " for pid " + servicePid
				+ " never reached ACTIVE or FAILED_ACTIVATION; last state seen: " + lastSeen);
	}

	/** The one component configuration SCR created for the given ConfigAdmin pid. */
	private static Optional<ComponentConfigurationDTO> findConfiguration(ServiceComponentRuntime scr,
			String servicePid) {
		Collection<ComponentDescriptionDTO> descriptions = scr.getComponentDescriptionDTOs();
		return descriptions.stream().filter(description -> PID.equals(description.name))
				.flatMap(description -> scr.getComponentConfigurationDTOs(description).stream())
				.filter(dto -> servicePid.equals(dto.properties.get("service.pid"))).findFirst();
	}
}
