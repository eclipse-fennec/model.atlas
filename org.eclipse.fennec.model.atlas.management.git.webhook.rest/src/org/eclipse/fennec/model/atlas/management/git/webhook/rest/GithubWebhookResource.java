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
import org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubWebhookPackage;
import org.eclipse.fennec.model.atlas.management.git.webhook.model.gitwebhook.WebhookPayload;
import org.eclipse.fennec.model.atlas.management.git.webhook.utils.WebhookTopics;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsName;
import org.osgi.service.jakartars.whiteboard.propertytypes.JakartarsResource;
import org.osgi.service.typedevent.TypedEventBus;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * GitHub push-webhook adapter.
 *
 * <p>Parses the GitHub push payload into a
 * {@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubPayload}
 * via the Fennec codec ({@link RootElement}) and delivers it as the neutral
 * {@link WebhookPayload} onto the {@code TypedEventBus}, keyed by the
 * repository+branch topic ({@link WebhookTopics}). Authenticity (HMAC-SHA256
 * {@code X-Hub-Signature-256}) and the push-only gate are enforced by the
 * name-bound {@link WebhookSignatureFilter} before this method runs, so the
 * body reaching here is trusted and known to be a push.
 *
 * @author Data In Motion
 * @since 1.0
 */
@JakartarsResource
@JakartarsName("GithubWebhookResource")
@Component(service = GithubWebhookResource.class, scope = ServiceScope.PROTOTYPE)
@Path("/github")
public class GithubWebhookResource {

	@Reference
	TypedEventBus eventBus;

	@POST
	@Path("/webhook")
	@Consumes(MediaType.APPLICATION_JSON)
	@VerifyWebhookSignature
	public Response webhook(
			@RootElement(rootType = GithubWebhookPackage.eNS_URI + "#//GithubPayload") WebhookPayload payload) {
		if (payload == null || payload.getRepositoryFullName() == null) {
			return Response.status(Response.Status.BAD_REQUEST)
					.entity("Push payload without a repository").build();
		}
		eventBus.deliver(WebhookTopics.topicFor(payload), payload);
		return Response.ok().build();
	}
}
