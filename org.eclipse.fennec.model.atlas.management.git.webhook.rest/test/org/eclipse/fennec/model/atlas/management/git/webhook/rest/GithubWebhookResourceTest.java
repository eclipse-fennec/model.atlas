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

import org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubPayload;
import org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubWebhookFactory;
import org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.Repository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.osgi.service.typedevent.TypedEventBus;

import jakarta.ws.rs.core.Response;

/**
 * Unit tests for the GitHub adapter's delivery of the neutral payload.
 */
class GithubWebhookResourceTest {

	@Test
	void webhook_deliversSamePayloadInstanceOnDerivedTopic() {
		GithubPayload payload = GithubWebhookFactory.eINSTANCE.createGithubPayload();
		Repository repo = GithubWebhookFactory.eINSTANCE.createRepository();
		repo.setFullName("eclipse-fennec/model.atlas");
		repo.setCloneUrl("https://github.com/eclipse-fennec/model.atlas.git");
		payload.setRepository(repo);
		payload.setRef("refs/heads/main");

		TypedEventBus bus = mock(TypedEventBus.class);
		GithubWebhookResource resource = new GithubWebhookResource();
		resource.eventBus = bus;

		Response response = resource.webhook(payload);

		assertEquals(200, response.getStatus());
		ArgumentCaptor<Object> event = ArgumentCaptor.forClass(Object.class);
		verify(bus).deliver(eq("fennec/git/webhook/eclipse-fennec_model_atlas/main"), event.capture());
		// The neutral payload is forwarded as-is, not copied/transformed.
		assertSame(payload, event.getValue());
	}

	@Test
	void webhook_missingRepository_returns400_andDoesNotDeliver() {
		GithubPayload payload = GithubWebhookFactory.eINSTANCE.createGithubPayload();
		payload.setRef("refs/heads/main"); // no repository -> getRepositoryFullName() == null

		TypedEventBus bus = mock(TypedEventBus.class);
		GithubWebhookResource resource = new GithubWebhookResource();
		resource.eventBus = bus;

		Response response = resource.webhook(payload);

		assertEquals(400, response.getStatus());
		verify(bus, never()).deliver(any(), any());
	}
}
