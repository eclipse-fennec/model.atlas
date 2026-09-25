/**
 * Copyright (c) 2012 - 2025 Kentyou and others.
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

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A {@code scopes/<name>/} folder is matched to a scope by name, so the file
 * image renaming its tenant scope with {@code MODEL_ATLAS_SCOPE} leaves the
 * folder addressed to a scope nobody configured. The seeding then times out and
 * halts the container, and the message is the only thing that says why — so it
 * has to name the scopes that <em>are</em> there, not just the one that is not.
 */
@DisplayName("InitialModelLoader — the message when a seed folder never finds its scope")
class InitialModelLoaderUnresolvedSeedMessageTest {

	@Test
	@DisplayName("the scopes that did appear are named, so a renamed scope is visible")
	void namesTheScopesThatDidAppear() {
		String message = InitialModelLoader.unresolvedSeedMessage(List.of("scope service 'jena'"),
				List.of("acme", "atlas"), 60);

		assertTrue(message.contains("scope service 'jena'"), "the missing service is named: " + message);
		assertTrue(message.contains("[acme, atlas]"), "the scopes that did appear are named: " + message);
		assertTrue(message.contains("scopes/<name>/"), "the folder-to-scope rule is stated: " + message);
		assertTrue(message.contains("60s"), "the wait that expired is stated: " + message);
	}

	@Test
	@DisplayName("the present scopes are sorted, whatever order they bound in")
	void sortsThePresentScopes() {
		String message = InitialModelLoader.unresolvedSeedMessage(List.of("scope service 'jena'"),
				List.of("zeta", "atlas", "acme"), 60);

		assertTrue(message.contains("[acme, atlas, zeta]"), "present scopes are sorted: " + message);
	}

	@Test
	@DisplayName("no scope at all reads as an empty list rather than as a missing sentence")
	void survivesNoScopesAtAll() {
		String message = InitialModelLoader.unresolvedSeedMessage(List.of("scope service 'jena'"), List.of(), 5);

		assertTrue(message.contains("Scopes present: []"), "an empty list is still printed: " + message);
	}
}
