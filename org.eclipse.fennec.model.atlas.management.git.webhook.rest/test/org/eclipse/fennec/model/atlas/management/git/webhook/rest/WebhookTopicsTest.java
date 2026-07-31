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
package org.eclipse.fennec.model.atlas.management.git.webhook.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.eclipse.fennec.model.atlas.management.git.webhook.utils.WebhookTopics;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the provider-neutral topic derivation.
 */
class WebhookTopicsTest {

	@Test
	void branchFromRef_stripsRefsHeads() {
		assertEquals("main", WebhookTopics.branchFromRef("refs/heads/main"));
	}

	@Test
	void branchFromRef_keepsHierarchicalBranch() {
		assertEquals("feature/x", WebhookTopics.branchFromRef("refs/heads/feature/x"));
	}

	@Test
	void branchFromRef_passesThroughNonHeadsRef() {
		assertEquals("refs/tags/v1", WebhookTopics.branchFromRef("refs/tags/v1"));
	}

	@Test
	void branchFromRef_nullBecomesEmpty() {
		assertEquals("", WebhookTopics.branchFromRef(null));
	}

	@Test
	void topicFor_sanitizesRepoAndBranchToSingleSegmentsEach() {
		// '/' and '.' in the repo name and any '/' in the branch collapse to '_'
		// so repo and branch each contribute exactly one topic segment.
		assertEquals("fennec/git/webhook/eclipse-fennec_model_atlas/main",
				WebhookTopics.topicFor("eclipse-fennec/model.atlas", "main"));
	}

	@Test
	void topicFor_hierarchicalBranchStaysOneSegment() {
		assertEquals("fennec/git/webhook/group_repo/feature_x",
				WebhookTopics.topicFor("group/repo", "feature/x"));
	}

	@Test
	void topicFor_nullOrEmptyTokensBecomeUnderscore() {
		assertEquals("fennec/git/webhook/_/_", WebhookTopics.topicFor(null, ""));
	}

	@Test
	void topicFor_keepsAllowedChars() {
		assertEquals("fennec/git/webhook/a-b_c9/x-1", WebhookTopics.topicFor("a-b_c9", "x-1"));
	}
}
