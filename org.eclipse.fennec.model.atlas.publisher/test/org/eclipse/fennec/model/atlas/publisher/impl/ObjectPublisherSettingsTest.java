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

import java.util.List;

import org.junit.jupiter.api.Test;

/**
 * Where an object goes, and what an id is allowed to look like on the way there.
 *
 * @author ilenia
 * @since Sep 10, 2026
 */
class ObjectPublisherSettingsTest {

	private static ObjectPublisherSettings settings() {
		return new ObjectPublisherSettings("jena", "default", List.of("draft"), "registries", "application/json",
				false, 1024);
	}

	@Test
	void theObjectPathIsTheRegistryResourcesOwnPath() {
		assertThat(settings().createObjectPath("draft", "device-1"))
				.isEqualTo("jena/registries/default/stages/draft/device-1");
	}

	@Test
	void theStageProbeAddressesTheStageWithoutAnId() {
		assertThat(settings().stagePath("draft")).isEqualTo("jena/registries/default/stages/draft");
	}

	@Test
	void anIdIsEscapedForAPathSegmentRatherThanForAQuery() {
		// A '+' here would be read back as a literal plus, not as a space, so the
		// query encoder's '+' must not survive into the path.
		assertThat(settings().createObjectPath("draft", "device 1")).endsWith("/device%201");
		assertThat(settings().createObjectPath("draft", "a#b")).endsWith("/a%23b");
	}

	@Test
	void aBlankPropertyFailsAtActivationRatherThanAtPublishTime() {
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new ObjectPublisherSettings("", "default", List.of("draft"), "registries",
						"application/json", false, 1024))
				.withMessageContaining("scope");
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new ObjectPublisherSettings("jena", "  ", List.of("draft"), "registries",
						"application/json", false, 1024))
				.withMessageContaining("registry");
	}

	@Test
	void theOneAllowedStageIsUsedWhenTheCallerNamesNone() {
		assertThat(settings().stageFor(null)).isEqualTo("draft");
		assertThat(settings().stageFor("  ")).isEqualTo("draft");
		assertThat(settings().stageFor("draft")).isEqualTo("draft");
	}

	@Test
	void aStageOutsideTheAllowlistIsRefused() {
		// The control that keeps a caller - an MCP tool above all - out of a released stage.
		assertThatIllegalArgumentException().isThrownBy(() -> settings().stageFor("release"))
				.withMessageContaining("release").withMessageContaining("[draft]");
	}

	@Test
	void noStageIsInferredWhenSeveralAreAllowed() {
		ObjectPublisherSettings several = new ObjectPublisherSettings("jena", "gdpr",
				List.of("draft", "approved", "release"), "registries", "application/json", false, 1024);
		assertThat(several.stageFor("approved")).isEqualTo("approved");
		// Guessing among several is how an object lands in a stage that misdescribes it.
		assertThatIllegalArgumentException().isThrownBy(() -> several.stageFor(null))
				.withMessageContaining("nothing to infer");
	}

	@Test
	void anEmptyAllowlistPublishesNowhere() {
		// Same fail-closed rule as publish.nsuri.allowlist on the package publisher.
		ObjectPublisherSettings none = new ObjectPublisherSettings("jena", "default", List.of(), "registries",
				"application/json", false, 1024);
		assertThatIllegalArgumentException().isThrownBy(() -> none.stageFor("draft"))
				.withMessageContaining("allowed.stages");
		assertThatIllegalArgumentException().isThrownBy(() -> none.stageFor(null))
				.withMessageContaining("allowed.stages");
	}

	@Test
	void aNonPositiveBodyCapIsRefused() {
		// Zero would deny every object while reading like "no limit".
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new ObjectPublisherSettings("jena", "default", List.of("draft"), "registries",
						"application/json", false, 0))
				.withMessageContaining("max.body.bytes");
	}
}
