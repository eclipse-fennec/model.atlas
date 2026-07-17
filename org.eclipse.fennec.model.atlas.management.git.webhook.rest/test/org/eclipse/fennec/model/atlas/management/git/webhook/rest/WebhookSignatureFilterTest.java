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

import static org.eclipse.fennec.model.atlas.management.git.webhook.rest.WebhookSignatureFilter.GITHUB_EVENT_HEADER;
import static org.eclipse.fennec.model.atlas.management.git.webhook.rest.WebhookSignatureFilter.GITHUB_SIGNATURE_HEADER;
import static org.eclipse.fennec.model.atlas.management.git.webhook.rest.WebhookSignatureFilter.GITLAB_EVENT_HEADER;
import static org.eclipse.fennec.model.atlas.management.git.webhook.rest.WebhookSignatureFilter.GITLAB_TOKEN_HEADER;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicReference;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;

/**
 * Unit tests for {@link WebhookSignatureFilter}: HMAC/token verification,
 * fail-closed behaviour, push-event gating and entity-stream buffering.
 */
class WebhookSignatureFilterTest {

	private static final byte[] BODY = "{\"ref\":\"refs/heads/main\"}".getBytes(StandardCharsets.UTF_8);
	private static final String SECRET = "s3cr3t";
	private static final String TOKEN = "tok3n";

	// --- GitHub -------------------------------------------------------------

	@Test
	void github_validSignature_passes() throws IOException {
		AtomicReference<InputStream> reset = new AtomicReference<>();
		ContainerRequestContext ctx = githubCtx("push", githubSignature(SECRET, BODY), reset);

		filter(SECRET, "", true).filter(ctx);

		verify(ctx, never()).abortWith(any());
		assertArrayEquals(BODY, reset.get().readAllBytes(), "entity stream must be reset with original body");
	}

	@Test
	void github_invalidSignature_rejected401() throws IOException {
		ContainerRequestContext ctx = githubCtx("push", "sha256=deadbeef", new AtomicReference<>());
		filter(SECRET, "", true).filter(ctx);
		assertEquals(401, capturedStatus(ctx));
	}

	@Test
	void github_missingSignatureHeader_rejected401() throws IOException {
		ContainerRequestContext ctx = githubCtx("push", null, new AtomicReference<>());
		filter(SECRET, "", true).filter(ctx);
		assertEquals(401, capturedStatus(ctx));
	}

	@Test
	void github_secretNotConfigured_failClosed401() throws IOException {
		ContainerRequestContext ctx = githubCtx("push", githubSignature(SECRET, BODY), new AtomicReference<>());
		filter("", "", true).filter(ctx);
		assertEquals(401, capturedStatus(ctx));
	}

	@Test
	void github_secretNotConfigured_requireDisabled_passes() throws IOException {
		ContainerRequestContext ctx = githubCtx("push", null, new AtomicReference<>());
		filter("", "", false).filter(ctx);
		verify(ctx, never()).abortWith(any());
	}

	@Test
	void github_nonPushEvent_acknowledged200_notVerified() throws IOException {
		// A 'ping' handshake must be accepted (200) without needing a signature.
		ContainerRequestContext ctx = githubCtx("ping", null, new AtomicReference<>());
		filter(SECRET, "", true).filter(ctx);
		assertEquals(200, capturedStatus(ctx));
	}

	// --- GitLab -------------------------------------------------------------

	@Test
	void gitlab_validToken_passes() throws IOException {
		ContainerRequestContext ctx = gitlabCtx("Push Hook", TOKEN, new AtomicReference<>());
		filter("", TOKEN, true).filter(ctx);
		verify(ctx, never()).abortWith(any());
	}

	@Test
	void gitlab_invalidToken_rejected401() throws IOException {
		ContainerRequestContext ctx = gitlabCtx("Push Hook", "wrong", new AtomicReference<>());
		filter("", TOKEN, true).filter(ctx);
		assertEquals(401, capturedStatus(ctx));
	}

	@Test
	void gitlab_missingToken_rejected401() throws IOException {
		ContainerRequestContext ctx = gitlabCtx("Push Hook", null, new AtomicReference<>());
		filter("", TOKEN, true).filter(ctx);
		assertEquals(401, capturedStatus(ctx));
	}

	@Test
	void gitlab_nonPushEvent_acknowledged200() throws IOException {
		ContainerRequestContext ctx = gitlabCtx("Tag Push Hook", TOKEN, new AtomicReference<>());
		filter("", TOKEN, true).filter(ctx);
		assertEquals(200, capturedStatus(ctx));
	}

	// --- Neither ------------------------------------------------------------

	@Test
	void noEventHeaders_rejected400() throws IOException {
		ContainerRequestContext ctx = baseCtx(new AtomicReference<>());
		filter(SECRET, TOKEN, true).filter(ctx);
		assertEquals(400, capturedStatus(ctx));
	}

	// --- helpers ------------------------------------------------------------

	private static WebhookSignatureFilter filter(String githubSecret, String gitlabToken, boolean require) {
		WebhookSignatureFilter f = new WebhookSignatureFilter();
		f.activate(config(githubSecret, gitlabToken, require));
		return f;
	}

	private static ContainerRequestContext baseCtx(AtomicReference<InputStream> resetSink) {
		ContainerRequestContext ctx = mock(ContainerRequestContext.class);
		when(ctx.getEntityStream()).thenReturn(new ByteArrayInputStream(BODY));
		doAnswer(inv -> {
			resetSink.set(inv.getArgument(0));
			return null;
		}).when(ctx).setEntityStream(any());
		return ctx;
	}

	private static ContainerRequestContext githubCtx(String event, String signature,
			AtomicReference<InputStream> resetSink) {
		ContainerRequestContext ctx = baseCtx(resetSink);
		when(ctx.getHeaderString(GITHUB_EVENT_HEADER)).thenReturn(event);
		when(ctx.getHeaderString(GITHUB_SIGNATURE_HEADER)).thenReturn(signature);
		return ctx;
	}

	private static ContainerRequestContext gitlabCtx(String event, String token,
			AtomicReference<InputStream> resetSink) {
		ContainerRequestContext ctx = baseCtx(resetSink);
		when(ctx.getHeaderString(GITLAB_EVENT_HEADER)).thenReturn(event);
		when(ctx.getHeaderString(GITLAB_TOKEN_HEADER)).thenReturn(token);
		return ctx;
	}

	private static int capturedStatus(ContainerRequestContext ctx) {
		ArgumentCaptor<Response> captor = ArgumentCaptor.forClass(Response.class);
		verify(ctx).abortWith(captor.capture());
		return captor.getValue().getStatus();
	}

	private static String githubSignature(String secret, byte[] body) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
			byte[] out = mac.doFinal(body);
			StringBuilder sb = new StringBuilder("sha256=");
			for (byte b : out) {
				sb.append(String.format("%02x", b & 0xFF));
			}
			return sb.toString();
		} catch (Exception e) {
			throw new IllegalStateException(e);
		}
	}

	private static WebhookSignatureFilter.Config config(String githubSecret, String gitlabToken, boolean require) {
		return new WebhookSignatureFilter.Config() {
			@Override
			public Class<? extends Annotation> annotationType() {
				return WebhookSignatureFilter.Config.class;
			}

			@Override
			public String githubSecret() {
				return githubSecret;
			}

			@Override
			public String gitlabToken() {
				return gitlabToken;
			}

			@Override
			public boolean requireSignature() {
				return require;
			}
		};
	}
}
