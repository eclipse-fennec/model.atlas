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

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;

/**
 * Verifies the authenticity of an inbound GitHub push webhook and drops
 * non-push deliveries, before the payload is parsed by
 * {@link GithubWebhookResource}.
 *
 * <p>GitHub signs the raw request body with an HMAC-SHA256 keyed by a shared
 * secret and sends it in {@code X-Hub-Signature-256}. Because HMAC is computed
 * over the <em>exact</em> bytes, this filter buffers the entity stream,
 * verifies it, and resets the stream so the codec message-body reader can
 * still parse the {@code @RootElement} payload afterwards.
 *
 * <p>The filter is name-bound via {@link VerifyGithubWebhookSignature} so it
 * only runs against the GitHub webhook endpoint, never the other resources
 * sharing the Jakarta RS whiteboard.
 *
 * <p>Only push events proceed ({@code X-GitHub-Event: push}). Anything else —
 * notably GitHub's {@code ping} handshake — is acknowledged with
 * {@code 200 OK} and not delivered downstream.
 *
 * <p>The component requires the {@value #CONFIG_PID} configuration; without it
 * neither this filter nor {@link GithubWebhookResource} activates, so the
 * GitHub endpoint is absent from the whiteboard entirely.
 *
 * @author Data In Motion
 * @since 1.0
 */
@Component(configurationPid = GithubWebhookSignatureFilter.CONFIG_PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
@JakartarsExtension
@JakartarsName(GithubWebhookSignatureFilter.NAME)
@VerifyGithubWebhookSignature
public class GithubWebhookSignatureFilter extends AbstractWebhookSignatureFilter {

	/** Configuration PID gating the GitHub webhook endpoint and its filter. */
	public static final String CONFIG_PID = "org.eclipse.fennec.model.atlas.management.git.webhook.github";
	/** Whiteboard name, targeted by the resource's extension select. */
	public static final String NAME = "GithubWebhookSignatureFilter";

	static final String GITHUB_EVENT_HEADER = "X-GitHub-Event";
	static final String GITHUB_SIGNATURE_HEADER = "X-Hub-Signature-256";

	private static final String GITHUB_PUSH_EVENT = "push";
	private static final String HMAC_SHA256 = "HmacSHA256";
	private static final char[] HEX = "0123456789abcdef".toCharArray();

	/**
	 * Configuration for the GitHub webhook. Provided via ConfigAdmin under pid
	 * {@value #CONFIG_PID}; the configuration's presence enables the endpoint.
	 */
	@interface Config {
		/** GitHub HMAC-SHA256 shared secret; empty disables GitHub verification. */
		String githubSecret() default "";

		/**
		 * When {@code true} (default) a request is rejected with 401 while the
		 * secret is not configured (fail-closed). Set {@code false} only in
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
		String event = ctx.getHeaderString(GITHUB_EVENT_HEADER);
		if (event == null) {
			abort(ctx, Response.Status.BAD_REQUEST, "Missing " + GITHUB_EVENT_HEADER + " header");
			return;
		}
		if (!GITHUB_PUSH_EVENT.equalsIgnoreCase(event)) {
			acknowledgeNonPush(ctx, event);
			return;
		}
		String secret = config.githubSecret();
		if (secret == null || secret.isEmpty()) {
			requireOrSkip(ctx, "GitHub", config.requireSignature());
			return;
		}
		// Buffer the body once; needed for HMAC and to let the reader re-parse it.
		byte[] body = ctx.getEntityStream().readAllBytes();
		ctx.setEntityStream(new ByteArrayInputStream(body));

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
}
