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

import java.io.IOException;

import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtension;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;

import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.Response;

/**
 * Verifies the authenticity of an inbound GitLab push webhook and drops
 * non-push deliveries, before the payload is parsed by
 * {@link GitlabWebhookResource}.
 *
 * <p>GitLab sends a plain shared-secret token in {@code X-Gitlab-Token}; unlike
 * GitHub's HMAC there is nothing computed over the body, so the entity stream
 * is left untouched for the codec message-body reader.
 *
 * <p>The filter is name-bound via {@link VerifyGitlabWebhookSignature} so it
 * only runs against the GitLab webhook endpoint, never the other resources
 * sharing the Jakarta RS whiteboard.
 *
 * <p>Only push events proceed ({@code X-Gitlab-Event: Push Hook}); anything
 * else is acknowledged with {@code 200 OK} and not delivered downstream.
 *
 * <p>The component requires the {@value #CONFIG_PID} configuration; without it
 * neither this filter nor {@link GitlabWebhookResource} activates, so the
 * GitLab endpoint is absent from the whiteboard entirely.
 *
 * @author Data In Motion
 * @since 1.0
 */
@Component(configurationPid = GitlabWebhookSignatureFilter.CONFIG_PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
@JakartarsExtension
@JakartarsName(GitlabWebhookSignatureFilter.NAME)
@VerifyGitlabWebhookSignature
public class GitlabWebhookSignatureFilter extends AbstractWebhookSignatureFilter {

	/** Configuration PID gating the GitLab webhook endpoint and its filter. */
	public static final String CONFIG_PID = "org.eclipse.fennec.model.atlas.management.git.webhook.gitlab";
	/** Whiteboard name, targeted by the resource's extension select. */
	public static final String NAME = "GitlabWebhookSignatureFilter";

	static final String GITLAB_EVENT_HEADER = "X-Gitlab-Event";
	static final String GITLAB_TOKEN_HEADER = "X-Gitlab-Token";

	private static final String GITLAB_PUSH_EVENT = "Push Hook";

	/**
	 * Configuration for the GitLab webhook. Provided via ConfigAdmin under pid
	 * {@value #CONFIG_PID}; the configuration's presence enables the endpoint.
	 */
	@interface Config {
		/** GitLab {@code X-Gitlab-Token} shared secret; empty disables GitLab verification. */
		String gitlabToken() default "";

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
		String event = ctx.getHeaderString(GITLAB_EVENT_HEADER);
		if (event == null) {
			abort(ctx, Response.Status.BAD_REQUEST, "Missing " + GITLAB_EVENT_HEADER + " header");
			return;
		}
		if (!GITLAB_PUSH_EVENT.equalsIgnoreCase(event)) {
			acknowledgeNonPush(ctx, event);
			return;
		}
		String token = config.gitlabToken();
		if (token == null || token.isEmpty()) {
			requireOrSkip(ctx, "GitLab", config.requireSignature());
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
}
