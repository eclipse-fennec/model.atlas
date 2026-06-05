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
package org.eclipse.fennec.model.atlas.rest.client.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URI;
import java.util.List;

import org.eclipse.fennec.model.atlas.rest.client.api.ClientConfiguration;
import org.eclipse.fennec.model.atlas.rest.client.api.ModelAtlasClientException;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;

/**
 * P2-3 — {@code listScopeNames()} REST mapping ({@code GET /scopes}) and the
 * lazily-built, cached {@link RemoteEPackageProviderImpl}.
 */
class ModelAtlasClientRestMappingTest {

	private static final URI BASE = URI.create("https://atlas.example.org/atlas");

	private final Client client = mock(Client.class);
	private final WebTarget target = mock(WebTarget.class);
	private final Invocation.Builder request = mock(Invocation.Builder.class);

	private ModelAtlasClientImpl newClient() {
		when(client.target(BASE)).thenReturn(target);
		when(target.path(anyString())).thenReturn(target);
		when(target.request(anyString())).thenReturn(request);
		ClientConfiguration config = ClientConfiguration.builder().baseUri(BASE).build();
		return new ModelAtlasClientImpl(config, client);
	}

	private static Response jsonOk(String body) {
		Response r = mock(Response.class);
		when(r.getStatus()).thenReturn(200);
		when(r.getStatusInfo()).thenReturn(Response.Status.OK);
		when(r.readEntity(String.class)).thenReturn(body);
		return r;
	}

	@Test
	void listScopeNames_getsScopes_andParsesNames() {
		Response response = jsonOk("{\"scopes\":[{\"name\":\"jena\"},{\"name\":\"default\"}]}");
		when(request.get()).thenReturn(response);

		List<String> scopes = newClient().listScopeNames();

		assertEquals(List.of("jena", "default"), scopes);
		verify(target).path("scopes");
	}

	@Test
	void listScopeNames_errorStatus_throws() {
		Response r = mock(Response.class);
		when(r.getStatus()).thenReturn(500);
		when(r.getStatusInfo()).thenReturn(Response.Status.INTERNAL_SERVER_ERROR);
		when(request.get()).thenReturn(r);

		assertThrows(ModelAtlasClientException.class, () -> newClient().listScopeNames());
	}

	@Test
	void ePackages_isLazyAndCached() {
		ModelAtlasClientImpl atlas = newClient();
		assertSame(atlas.ePackages(), atlas.ePackages(), "ePackages() should return the cached provider");
	}

	@Test
	void newResourceSet_installsAtlasDelegatingRegistry() {
		ModelAtlasClientImpl atlas = newClient();
		assertInstanceOf(AtlasDelegatingPackageRegistry.class, atlas.newResourceSet().getPackageRegistry());
	}
}
