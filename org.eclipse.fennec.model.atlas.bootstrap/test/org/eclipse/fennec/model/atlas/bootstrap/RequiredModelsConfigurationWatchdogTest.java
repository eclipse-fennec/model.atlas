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
package org.eclipse.fennec.model.atlas.bootstrap;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.osgi.service.cm.Configuration;

/**
 * Since issue #362 the gate in front of the {@code InitialModelLoader} is only registered from a
 * configured activation, so a deployment that ships no {@code InitialModelLoaderRequiredModels}
 * gets a loader that never activates — and the watchdog inside the gate never runs either, because
 * it is scheduled from the activation that did not happen.
 *
 * <p>
 * Nothing would then be seeded and nothing would say so: the scope, its registries and the health
 * checks all come up exactly as they should, and only the contents are missing. This message is
 * the one thing standing between that and a week of wondering, so it is asserted rather than
 * trusted.
 * </p>
 */
@DisplayName("InitialModelLoader — the message when the required-models configuration never arrives")
class RequiredModelsConfigurationWatchdogTest {

	@Test
	@DisplayName("no configuration at all is reported, and the report says what to do about it")
	void reportsAMissingConfiguration() {
		String message = RequiredModelsConfigurationWatchdog.configurationMissingMessage(null, 60);

		assertNotNull(message, "a missing configuration must be reported");
		assertTrue(message.contains(RequiredModelsCondition.PID),
				"the PID that has to be shipped is named: " + message);
		assertTrue(message.contains(InitialModelLoader.PID),
				"the component that will not start is named: " + message);
		assertTrue(message.contains("required.models"),
				"the property to ship it with is named: " + message);
		assertTrue(message.contains("60s"), "the wait that expired is stated: " + message);
	}

	@Test
	@DisplayName("an empty result reads as absent, not as present")
	void treatsAnEmptyResultAsMissing() {
		// ConfigurationAdmin.listConfigurations answers null for 'none matched', but a zero-length
		// array is just as empty and must not be read as a configuration that is there.
		String message = RequiredModelsConfigurationWatchdog.configurationMissingMessage(new Configuration[0], 60);

		assertNotNull(message, "an empty result is still a missing configuration");
	}

	@Test
	@DisplayName("a configuration that is there is not reported")
	void staysQuietWhenConfigured() {
		// The element is never dereferenced - only its presence decides.
		String message = RequiredModelsConfigurationWatchdog.configurationMissingMessage(new Configuration[1], 60);

		assertNull(message, "a configured deployment must not be warned at: " + message);
	}
}
