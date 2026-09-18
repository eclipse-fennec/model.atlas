/*
 * ******************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Data In Motion Consulting - initial implementation
 * ******************************************************************
 */
package org.eclipse.fennec.model.atlas.publisher.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

/**
 * Where an object goes, and what an id is allowed to look like on the way there.
 *
 * @author ilenia
 * @since Sep 10, 2026
 */
class ObjectPublisherSettingsTest {

	private static ObjectPublisherSettings settings() {
		return new ObjectPublisherSettings("jena", "default", "draft", "registries", "application/json", false,
				1024);
	}

	@Test
	void theObjectPathIsTheRegistryResourcesOwnPath() {
		assertThat(settings().createObjectPath("device-1"))
				.isEqualTo("jena/registries/default/stages/draft/device-1");
	}

	@Test
	void theStageProbeAddressesTheStageWithoutAnId() {
		assertThat(settings().stagePath()).isEqualTo("jena/registries/default/stages/draft");
	}

	@Test
	void anIdIsEscapedForAPathSegmentRatherThanForAQuery() {
		// A '+' here would be read back as a literal plus, not as a space, so the
		// query encoder's '+' must not survive into the path.
		assertThat(settings().createObjectPath("device 1")).endsWith("/device%201");
		assertThat(settings().createObjectPath("a#b")).endsWith("/a%23b");
	}

	@Test
	void aBlankPropertyFailsAtActivationRatherThanAtPublishTime() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new ObjectPublisherSettings("", "default", "draft", "registries",
						"application/json", false, 1024))
				.withMessageContaining("scope");
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new ObjectPublisherSettings("jena", "  ", "draft", "registries",
						"application/json", false, 1024))
				.withMessageContaining("registry");
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new ObjectPublisherSettings("jena", "default", "", "registries",
						"application/json", false, 1024))
				.withMessageContaining("stage");
	}

	@Test
	void aNonPositiveBodyCapIsRefused() {
		// Zero would deny every object while reading like "no limit".
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new ObjectPublisherSettings("jena", "default", "draft", "registries",
						"application/json", false, 0))
				.withMessageContaining("max.body.bytes");
	}
}
