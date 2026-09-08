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
package org.eclipse.fennec.model.atlas.rest.application.aggregate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.rest.application.aggregate.ScopeAggregateService.ScopeAggregate;
import org.eclipse.fennec.model.atlas.rest.application.aggregate.ScopeAggregateService.ScopeDiff;
import org.eclipse.fennec.model.atlas.scope.api.RegistryType;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Registry;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Scope;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiFactory;
import org.eclipse.fennec.model.atlas.workflow.ScopeServiceCollector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A scope's schema packages are identified in the aggregate manifest by nsURI rather than by
 * {@code registry/objectId}. That rule follows the registry's <em>type</em>, not the literal
 * name {@code "schema"} — otherwise renaming the registry silently changes how packages are
 * identified to every client diffing against an ETag (issue #179).
 *
 * @see <a href="https://github.com/eclipse-fennec/model.atlas/issues/179">issue #179</a>
 */
@DisplayName("ScopeAggregateService — schema identity follows the registry type")
public class ScopeAggregateServiceTest {

	private static final String SCOPE = "test-scope";
	private static final String NS_URI = "http://example.org/model/1.0.0";

	@Test
	@DisplayName("a schema registry called 'models' still reports its packages as nsURIs")
	void reportsSchemaChangesAsNsUrisWhateverTheRegistryIsCalled() {
		ObjectMetadata thePackage = metadata("models", "cGFja2FnZQ", NS_URI, "hash-1");
		ScopeAggregateService service = serviceFor("models", RegistryType.SCHEMA, thePackage);

		// The manifest keeps copies, so the aggregate below is the baseline even after the
		// content hash moves on.
		ScopeAggregate before = service.computeAggregate(SCOPE);
		thePackage.setContentHash("hash-2");
		ScopeAggregate after = service.computeAggregate(SCOPE);
		ScopeDiff diff = service.diffSince(SCOPE, before.etag(), after);

		assertTrue(diff.baselineKnown(), "the baseline was just computed, so it must be known");
		assertEquals(List.of(NS_URI), diff.changedNsUris(),
				"a package of the scope's SCHEMA registry is reported by nsURI, whatever the registry is named");
		assertEquals(List.of(), diff.changedObjects(),
				"it must not also be reported as a plain registry/objectId change");
	}

	@Test
	@DisplayName("registries of other types keep reporting registry/objectId")
	void reportsOtherRegistriesByObjectId() {
		ObjectMetadata person = metadata("person", "p-1", null, "hash-1");
		ScopeAggregateService service = serviceFor("person", RegistryType.OTHER, person);

		ScopeAggregate before = service.computeAggregate(SCOPE);
		person.setContentHash("hash-2");
		ScopeAggregate after = service.computeAggregate(SCOPE);
		ScopeDiff diff = service.diffSince(SCOPE, before.etag(), after);

		assertEquals(List.of("person/p-1"), diff.changedObjects());
		assertEquals(List.of(), diff.changedNsUris());
	}

	@SuppressWarnings("unchecked")
	private static ScopeAggregateService serviceFor(String registryName, RegistryType type,
			ObjectMetadata... content) {
		ScopeService<EObject> scopeService = mock(ScopeService.class);
		when(scopeService.getAllRegistries()).thenReturn(List.of(registryName));
		when(scopeService.listAllForRegistry(registryName)).thenReturn(Arrays.asList(content));

		Scope scope = WorkflowApiFactory.eINSTANCE.createScope();
		scope.setName(SCOPE);
		Registry registry = WorkflowApiFactory.eINSTANCE.createRegistry();
		registry.setName(registryName);
		registry.setType(type);
		scope.getRegistries().add(registry);
		when(scopeService.getScopeInfo()).thenReturn(scope);

		ScopeServiceCollector collector = mock(ScopeServiceCollector.class);
		doReturn(scopeService).when(collector).getScopeServiceByScopeName(SCOPE);

		ScopeAggregateService service = new ScopeAggregateService();
		service.scopeCollector = collector;
		return service;
	}

	private static ObjectMetadata metadata(String registry, String objectId, String nsUri, String contentHash) {
		ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadata.setRegistry(registry);
		metadata.setObjectId(objectId);
		metadata.setContentHash(contentHash);
		if (nsUri != null) {
			metadata.getProperties().put("nsUri", nsUri);
		}
		return metadata;
	}
}
