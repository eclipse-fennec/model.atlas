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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;

/**
 * Verifies the authenticity of an inbound git push webhook and drops
 * non-push deliveries, before the payload is parsed by the resource.
 *
 * <p>GitHub signs the raw request body with an HMAC-SHA256 keyed by a shared
 * secret and sends it in {@code X-Hub-Signature-256}; GitLab instead sends a
 * plain shared-secret token in {@code X-Gitlab-Token}. Because HMAC is computed
 * over the <em>exact</em> bytes, this filter buffers the entity stream, verifies
 * it, and resets the stream so the codec message-body reader can still parse the
 * {@code @RootElement} payload afterwards.
 *
 * <p>The filter is name-bound via {@link VerifyWebhookSignature} so it only runs
 * against the webhook endpoints, never the other resources sharing the Jakarta
 * RS whiteboard.
 *
 * <p>Provider is detected from the event header; only push events proceed
 * (GitHub {@code X-GitHub-Event: push}, GitLab {@code X-Gitlab-Event: Push
 * Hook}). Anything else — notably GitHub's {@code ping} handshake — is
 * acknowledged with {@code 200 OK} and not delivered downstream.
 *
 * @author Data In Motion
 * @since 1.0
 */
@Component(configurationPid = "org.eclipse.fennec.model.atlas.management.git.webhook")
@JakartarsExtension
@JakartarsName("WebhookSignatureFilter")
@VerifyWebhookSignature
public class WebhookSignatureFilter implements ContainerRequestFilter {

	static final String GITHUB_EVENT_HEADER = "X-GitHub-Event";
	static final String GITHUB_SIGNATURE_HEADER = "X-Hub-Signature-256";
	static final String GITLAB_EVENT_HEADER = "X-Gitlab-Event";
	static final String GITLAB_TOKEN_HEADER = "X-Gitlab-Token";

	private static final String GITHUB_PUSH_EVENT = "push";
	private static final String GITLAB_PUSH_EVENT = "Push Hook";
	private static final String HMAC_SHA256 = "HmacSHA256";
	private static final char[] HEX = "0123456789abcdef".toCharArray();

	/**
	 * Configuration for the webhook secrets. Provided via ConfigAdmin under pid
	 * {@code org.eclipse.fennec.model.atlas.management.git.webhook}.
	 */
	@interface Config {
		/** GitHub HMAC-SHA256 shared secret; empty disables GitHub verification. */
		String githubSecret() default "";

		/** GitLab {@code X-Gitlab-Token} shared secret; empty disables GitLab verification. */
		String gitlabToken() default "";

		/**
		 * When {@code true} (default) a request for a provider whose secret is not
		 * configured is rejected with 401 (fail-closed). Set {@code false} only in
		 * trusted setups to accept unverified deliveries.
		 */
		boolean requireSignature() default true;
	}

	private volatile Config config;

	@Activate
	@Modified
	void activate(Config config) {
		this.config = config;
	}

	@Override
	public void filter(ContainerRequestContext ctx) throws IOException {
		String githubEvent = ctx.getHeaderString(GITHUB_EVENT_HEADER);
		String gitlabEvent = ctx.getHeaderString(GITLAB_EVENT_HEADER);

		// Buffer the body once; needed for HMAC and to let the reader re-parse it.
		byte[] body = ctx.getEntityStream().readAllBytes();
		ctx.setEntityStream(new ByteArrayInputStream(body));

		if (githubEvent != null) {
			if (!GITHUB_PUSH_EVENT.equalsIgnoreCase(githubEvent)) {
				acknowledgeNonPush(ctx, githubEvent);
				return;
			}
			verifyGithub(ctx, body);
		} else if (gitlabEvent != null) {
			if (!GITLAB_PUSH_EVENT.equalsIgnoreCase(gitlabEvent)) {
				acknowledgeNonPush(ctx, gitlabEvent);
				return;
			}
			verifyGitlab(ctx);
		} else {
			abort(ctx, Response.Status.BAD_REQUEST, "Unrecognized webhook: no GitHub or GitLab event header");
		}
	}

	private void verifyGithub(ContainerRequestContext ctx, byte[] body) {
		String secret = config.githubSecret();
		if (secret == null || secret.isEmpty()) {
			requireOrSkip(ctx, "GitHub");
			return;
		}
		String provided = ctx.getHeaderString(GITHUB_SIGNATURE_HEADER);
		if (provided == null) {
			abort(ctx, Response.Status.UNAUTHORIZED, "Missing " + GITHUB_SIGNATURE_HEADER);
			return;
		}
		String expected = "sha256=" + hmacSha256Hex(secret, body);
		if (!constantTimeEquals(expected, provided)) {
			abort(ctx, Response.Status.UNAUTHORIZED, "Invalid " + GITHUB_SIGNATURE_HEADER);
		}
	}

	private void verifyGitlab(ContainerRequestContext ctx) {
		String token = config.gitlabToken();
		if (token == null || token.isEmpty()) {
			requireOrSkip(ctx, "GitLab");
			return;
		}
		String provided = ctx.getHeaderString(GITLAB_TOKEN_HEADER);
		if (provided == null) {
			abort(ctx, Response.Status.UNAUTHORIZED, "Missing " + GITLAB_TOKEN_HEADER);
			return;
		}
		if (!constantTimeEquals(token, provided)) {
			abort(ctx, Response.Status.UNAUTHORIZED, "Invalid " + GITLAB_TOKEN_HEADER);
		}
	}

	private void requireOrSkip(ContainerRequestContext ctx, String provider) {
		if (config.requireSignature()) {
			abort(ctx, Response.Status.UNAUTHORIZED,
					provider + " webhook secret is not configured");
		}
		// else: verification intentionally disabled — let the request through.
	}

	private void acknowledgeNonPush(ContainerRequestContext ctx, String event) {
		// Not a push (e.g. GitHub's 'ping' handshake): acknowledge, do not process.
		ctx.abortWith(Response.ok("Ignored non-push event: " + event).build());
	}

	private void abort(ContainerRequestContext ctx, Response.Status status, String message) {
		ctx.abortWith(Response.status(status).entity(message).build());
	}

	private static String hmacSha256Hex(String secret, byte[] body) {
		try {
			Mac mac = Mac.getInstance(HMAC_SHA256);
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), HMAC_SHA256));
			return toHex(mac.doFinal(body));
		} catch (Exception e) {
			// Misconfigured JCE / bad key — never accept the request on failure.
			throw new IllegalStateException("Unable to compute webhook HMAC", e);
		}
	}

	private static String toHex(byte[] bytes) {
		char[] out = new char[bytes.length * 2];
		for (int i = 0; i < bytes.length; i++) {
			int v = bytes[i] & 0xFF;
			out[i * 2] = HEX[v >>> 4];
			out[i * 2 + 1] = HEX[v & 0x0F];
		}
		return new String(out);
	}

	private static boolean constantTimeEquals(String expected, String provided) {
		return MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
				provided.getBytes(StandardCharsets.UTF_8));
	}
}
