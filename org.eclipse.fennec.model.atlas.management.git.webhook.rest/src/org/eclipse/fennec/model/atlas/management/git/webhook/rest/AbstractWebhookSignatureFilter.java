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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;

/**
 * Shared behavior of the per-provider webhook verification filters
 * ({@link GithubWebhookSignatureFilter}, {@link GitlabWebhookSignatureFilter}):
 * request abort/acknowledge helpers, the fail-closed handling of a missing
 * secret, and constant-time secret comparison.
 *
 * <p>Each provider has its own filter component gated on its own configuration
 * PID, so a deployment can expose the GitHub endpoint, the GitLab endpoint,
 * both, or neither — the matching resource is gated on the same PID and
 * additionally requires its filter via {@code osgi.jakartars.extension.select},
 * so an endpoint is never served without its verification filter.
 *
 * @author Data In Motion
 * @since 1.0
 */
abstract class AbstractWebhookSignatureFilter implements ContainerRequestFilter {

	/**
	 * Handles a request whose provider secret is not configured: rejected with
	 * 401 when {@code requireSignature} is set (fail-closed), let through
	 * otherwise (trusted setups only).
	 */
	protected final void requireOrSkip(ContainerRequestContext ctx, String provider, boolean requireSignature) {
		if (requireSignature) {
			abort(ctx, Response.Status.UNAUTHORIZED, provider + " webhook secret is not configured");
		}
		// else: verification intentionally disabled — let the request through.
	}

	protected final void acknowledgeNonPush(ContainerRequestContext ctx, String event) {
		// Not a push (e.g. GitHub's 'ping' handshake): acknowledge, do not process.
		ctx.abortWith(Response.ok("Ignored non-push event: " + event).build());
	}

	protected final void abort(ContainerRequestContext ctx, Response.Status status, String message) {
		ctx.abortWith(Response.status(status).entity(message).build());
	}

	protected static boolean constantTimeEquals(String expected, String provided) {
		return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
				provided.getBytes(StandardCharsets.UTF_8));
	}
}
