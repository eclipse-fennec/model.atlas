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
package org.eclipse.fennec.model.atlas.management.git.webhook.utils;

import org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload;

/**
 * Derives the provider-neutral {@code TypedEventBus} topic a push webhook is
 * delivered on, from repository identity and branch.
 *
 * <p>This is the shared contract between the webhook ingest layer (publisher)
 * and the git storage service (subscriber, added in a later phase): both must
 * compute the <em>same</em> topic for a given (repository, branch) pair, so the
 * derivation lives in one place. A git storage service configured for
 * {@code repo} + {@code branch=stage} subscribes to {@link #topicFor(String,
 * String)} with the same inputs.
 *
 * <p>Topic shape: {@code fennec/git/webhook/<repository>/<branch>}. Each token
 * is sanitized to the characters a typed-event topic accepts
 * ({@code [A-Za-z0-9_-]}); any other character (including the {@code /} inside a
 * namespaced repository or a hierarchical branch) becomes {@code _} so the
 * repository and branch each contribute exactly one segment.
 */
public final class WebhookTopics {

	/** Common prefix for every git webhook topic. */
	public static final String TOPIC_PREFIX = "fennec/git/webhook";

	private static final String REFS_HEADS = "refs/heads/";

	private WebhookTopics() {
		// utility
	}

	/**
	 * Topic for the given neutral payload, from its repository full name and ref.
	 *
	 * @param payload the neutral push payload
	 * @return the delivery topic, never {@code null}
	 */
	public static String topicFor(WebhookPayload payload) {
		return topicFor(payload.getRepositoryFullName(), branchFromRef(payload.getRef()));
	}

	/**
	 * Topic for an explicit repository/branch pair. Used by the subscriber side
	 * (storage service config) to compute the topic it listens on.
	 *
	 * @param repositoryFullName namespaced repository id (e.g. {@code owner/repo})
	 * @param branch short branch name (e.g. {@code main})
	 * @return the delivery topic, never {@code null}
	 */
	public static String topicFor(String repositoryFullName, String branch) {
		return TOPIC_PREFIX + "/" + sanitize(repositoryFullName) + "/" + sanitize(branch);
	}

	/**
	 * Reduces a git ref to its short branch name: {@code refs/heads/main -> main}.
	 * A ref that is not under {@code refs/heads/} is returned unchanged.
	 *
	 * @param ref the full git ref, may be {@code null}
	 * @return the short branch name, or {@code ""} if {@code ref} is {@code null}
	 */
	public static String branchFromRef(String ref) {
		if (ref == null) {
			return "";
		}
		return ref.startsWith(REFS_HEADS) ? ref.substring(REFS_HEADS.length()) : ref;
	}

	private static String sanitize(String token) {
		if (token == null || token.isEmpty()) {
			return "_";
		}
		StringBuilder sb = new StringBuilder(token.length());
		for (int i = 0; i < token.length(); i++) {
			char c = token.charAt(i);
			sb.append(isTopicChar(c) ? c : '_');
		}
		return sb.toString();
	}

	private static boolean isTopicChar(char c) {
		return (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
				|| c == '_' || c == '-';
	}
}
