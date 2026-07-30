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

import org.eclipse.fennec.codec.rest.annotations.json.RootElement;
import org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabWebhookPackage;
import org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload;
import org.eclipse.fennec.model.atlas.management.git.webhook.utils.WebhookTopics;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsExtensionSelect;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;
import org.osgi.service.typedevent.TypedEventBus;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * GitLab push-webhook adapter.
 *
 * <p>Parses the GitLab push payload into a
 * {@link org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabPayload}
 * via the Fennec codec ({@link RootElement}) and delivers it as the neutral
 * {@link WebhookPayload} onto the {@code TypedEventBus}, keyed by the
 * repository+branch topic ({@link WebhookTopics}) — the exact same topic the
 * GitHub adapter produces, so downstream stays provider-agnostic. Authenticity
 * ({@code X-Gitlab-Token}) and the push-only gate are enforced by the name-bound
 * {@link GitlabWebhookSignatureFilter} before this method runs.
 *
 * <p>The endpoint only exists when the
 * {@value GitlabWebhookSignatureFilter#CONFIG_PID} configuration is provided:
 * the component requires that PID, and the whiteboard additionally withholds
 * the resource until the signature filter is wired
 * ({@code osgi.jakartars.extension.select}) — a request can never reach this
 * method unverified.
 *
 * @author Data In Motion
 * @since 1.0
 */
@JakartarsResource
@JakartarsName("GitlabWebhookResource")
@JakartarsExtensionSelect("(jakartars.name=" + GitlabWebhookSignatureFilter.NAME + ")")
@Component(service = GitlabWebhookResource.class, scope = ServiceScope.PROTOTYPE,
		configurationPid = GitlabWebhookSignatureFilter.CONFIG_PID,
		configurationPolicy = ConfigurationPolicy.REQUIRE)
@Path("/gitlab")
public class GitlabWebhookResource {

	@Reference
	TypedEventBus eventBus;

	@POST
	@Path("/webhook")
	@Consumes(MediaType.APPLICATION_JSON)
	@VerifyGitlabWebhookSignature
	public Response webhook(
			@RootElement(rootType = GitlabWebhookPackage.eNS_URI + "#//GitlabPayload") WebhookPayload payload) {
		if (payload == null || payload.getRepositoryFullName() == null) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("Push payload without a repository").build();
		}
		eventBus.deliver(WebhookTopics.topicFor(payload), payload);
		return Response.ok().build();
	}
}
