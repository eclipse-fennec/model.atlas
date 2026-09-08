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
package org.eclipse.fennec.model.atlas.rest.filter;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.mediatypes.api.SupportedMediatype;
import org.eclipse.fennec.model.atlas.scope.api.RegistryType;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Registry;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Scope;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiFactory;
import org.eclipse.fennec.model.atlas.workflow.ScopeServiceCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.core.PathSegment;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;

/**
 * The {@code /{scopeName}/schema} path is a URL literal, not a registry name: the filter has
 * to validate it against whichever registry of the scope is of type
 * {@link RegistryType#SCHEMA}, whatever that registry is called (issue #179).
 *
 * @see <a href="https://github.com/eclipse-fennec/model.atlas/issues/179">issue #179</a>
 */
@DisplayName("ModelAtlasRequestFilter — the schema path resolves by registry type")
public class ModelAtlasRequestFilterSchemaRegistryTest {

	private static final String SCOPE = "test-scope";

	private ModelAtlasRequestFilter filter;
	private ScopeService<EObject> scopeService;
	private ContainerRequestContext requestContext;
	private UriInfo uriInfo;
	private MultivaluedMap<String, String> pathParams;

	@SuppressWarnings("unchecked")
	@BeforeEach
	void setUp() {
		filter = new ModelAtlasRequestFilter();
		scopeService = mock(ScopeService.class);
		ScopeServiceCollector collector = mock(ScopeServiceCollector.class);
		org.mockito.Mockito.doReturn(scopeService).when(collector).getScopeServiceByScopeName(SCOPE);
		filter.bindScopeCollector(collector);

		SupportedMediatype mediatype = mock(SupportedMediatype.class);
		when(mediatype.getSupportedMediaTypes()).thenReturn(List.of(MediaType.APPLICATION_JSON));
		filter.bindSupportedMediatype(mediatype);

		pathParams = new MultivaluedHashMap<>();
		pathParams.putSingle("scopeName", SCOPE);

		uriInfo = mock(UriInfo.class);
		when(uriInfo.getPathParameters()).thenReturn(pathParams);
		when(uriInfo.getQueryParameters()).thenReturn(new MultivaluedHashMap<>());

		requestContext = mock(ContainerRequestContext.class);
		when(requestContext.getUriInfo()).thenReturn(uriInfo);
		when(requestContext.getAcceptableMediaTypes()).thenReturn(List.of(MediaType.WILDCARD_TYPE));
	}

	@Test
	@DisplayName("a schema registry called something else is the one that gets validated")
	void resolvesTheSchemaRegistryByTypeNotByName() throws Exception {
		// The deployment constraint of issue #179: this scope's schema registry is called
		// 'models', so validating the literal name 'schema' rejects every /schema request.
		scopeHasRegistries(registry("models", RegistryType.SCHEMA), registry("person", RegistryType.OTHER));
		when(scopeService.isValidRegistry("models")).thenReturn(true);
		path(SCOPE, "schema", "all");

		assertDoesNotThrow(() -> filter.filter(requestContext),
				"a /schema request must be validated against the scope's SCHEMA registry, whatever its name");
		verify(scopeService).isValidRegistry("models");
	}

	@Test
	@DisplayName("a segment that merely starts with 'schema' is not the schema path")
	void aSegmentThatOnlyStartsWithSchemaIsNotTheSchemaPath() throws Exception {
		// F62: contains("/schema") also matches /{scope}/schemaDraft/... and made it resolve
		// to the schema registry.
		scopeHasRegistries(registry("models", RegistryType.SCHEMA));
		path(SCOPE, "schemaDraft", "something");

		assertDoesNotThrow(() -> filter.filter(requestContext));
		verify(scopeService, never()).isValidRegistry(anyString());
	}

	@Test
	@DisplayName("the schema path 404s when the scope has no schema registry")
	void refusesTheSchemaPathWhenTheScopeHasNoSchemaRegistry() {
		scopeHasRegistries(registry("person", RegistryType.OTHER));
		path(SCOPE, "schema", "all");

		WebApplicationException failure = assertThrows(WebApplicationException.class,
				() -> filter.filter(requestContext));

		assertEquals(Response.Status.NOT_FOUND.getStatusCode(), failure.getResponse().getStatus(),
				"a scope without a SCHEMA registry does not serve /schema — that is a 404, not a bad request");
	}

	@Test
	@DisplayName("an explicitly addressed registry is still validated by its name")
	void anExplicitRegistryNameIsUnaffected() throws Exception {
		pathParams.putSingle("registryName", "person");
		when(scopeService.isValidRegistry("person")).thenReturn(true);
		path(SCOPE, "registries", "person", "stages", "draft");

		assertDoesNotThrow(() -> filter.filter(requestContext));
		verify(scopeService).isValidRegistry("person");
	}

	private void scopeHasRegistries(Registry... registries) {
		Scope scope = WorkflowApiFactory.eINSTANCE.createScope();
		scope.setName(SCOPE);
		scope.getRegistries().addAll(Arrays.asList(registries));
		when(scopeService.getScopeInfo()).thenReturn(scope);
	}

	private static Registry registry(String name, RegistryType type) {
		Registry registry = WorkflowApiFactory.eINSTANCE.createRegistry();
		registry.setName(name);
		registry.setType(type);
		return registry;
	}

	private void path(String... segments) {
		// The PathSegment mocks are built first: stubbing one inside the enclosing
		// when(...) call throws UnfinishedStubbingException.
		List<PathSegment> pathSegments = new ArrayList<>();
		for (String segment : segments) {
			PathSegment pathSegment = mock(PathSegment.class);
			when(pathSegment.getPath()).thenReturn(segment);
			pathSegments.add(pathSegment);
		}
		when(uriInfo.getPath()).thenReturn(String.join("/", segments));
		when(uriInfo.getPathSegments()).thenReturn(pathSegments);
	}
}
