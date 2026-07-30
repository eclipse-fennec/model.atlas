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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabPayload;
import org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.GitlabWebhookFactory;
import org.eclipse.fennec.model.atlas.management.git.gitlab.webhook.model.gitlabwebhook.Project;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.osgi.service.typedevent.TypedEventBus;

import jakarta.ws.rs.core.Response;

/**
 * Unit tests for the GitLab adapter's delivery of the neutral payload — it must
 * produce the exact same topic shape as the GitHub adapter for downstream
 * provider-agnosticism.
 */
class GitlabWebhookResourceTest {

	@Test
	void webhook_deliversSamePayloadInstanceOnDerivedTopic() {
		GitlabPayload payload = GitlabWebhookFactory.eINSTANCE.createGitlabPayload();
		Project project = GitlabWebhookFactory.eINSTANCE.createProject();
		project.setPathWithNamespace("eclipse-fennec/model.atlas");
		project.setGitHttpUrl("https://gitlab.com/eclipse-fennec/model.atlas.git");
		payload.setProject(project);
		payload.setRef("refs/heads/main");

		TypedEventBus bus = mock(TypedEventBus.class);
		GitlabWebhookResource resource = new GitlabWebhookResource();
		resource.eventBus = bus;

		Response response = resource.webhook(payload);

		assertEquals(200, response.getStatus());
		ArgumentCaptor<Object> event = ArgumentCaptor.forClass(Object.class);
		verify(bus).deliver(eq("fennec/git/webhook/eclipse-fennec_model_atlas/main"), event.capture());
		assertSame(payload, event.getValue());
	}

	@Test
	void webhook_missingProject_returns400_andDoesNotDeliver() {
		GitlabPayload payload = GitlabWebhookFactory.eINSTANCE.createGitlabPayload();
		payload.setRef("refs/heads/main"); // no project -> getRepositoryFullName() == null

		TypedEventBus bus = mock(TypedEventBus.class);
		GitlabWebhookResource resource = new GitlabWebhookResource();
		resource.eventBus = bus;

		Response response = resource.webhook(payload);

		assertEquals(400, response.getStatus());
		verify(bus, never()).deliver(any(), any());
	}
}
