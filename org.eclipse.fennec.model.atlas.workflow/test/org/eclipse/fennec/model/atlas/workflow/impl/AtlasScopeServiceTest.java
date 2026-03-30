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
package org.eclipse.fennec.model.atlas.workflow.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Registry;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Scope;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiFactory;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * Unit tests for {@link AtlasScopeService}.
 *
 * @author ilenia
 * @since Mar 30, 2026
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AtlasScopeService Unit Tests")
public class AtlasScopeServiceTest {

	@Mock
	private RegistryService<EPackage> mockRegistryService;

	private AtlasScopeService service;
	private PromiseFactory promiseFactory;

	@BeforeEach
	void setUp() {
		promiseFactory = new PromiseFactory(null);
		Registry registry = WorkflowApiFactory.eINSTANCE.createRegistry();
		registry.setName(WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME);
		when(mockRegistryService.getRegistry()).thenReturn(registry);
		service = new AtlasScopeService(mockRegistryService);
	}

	@Nested
	@DisplayName("Scope Object Tests")
	class ScopeObjectTests {

		@Test
		@DisplayName("Should create scope with atlas name")
		void shouldCreateScopeWithAtlasName() {
			Scope scope = service.getScope();
			assertNotNull(scope);
			assertEquals(WorkflowConstants.ATLAS_SCOPE_NAME, scope.getName());
		}

		@Test
		@DisplayName("Should create scope with no parent")
		void shouldCreateScopeWithNoParent() {
			Scope scope = service.getScope();
			assertNull(scope.getParentScope());
		}

		@Test
		@DisplayName("Should create scope with atlas-schema-registry")
		void shouldCreateScopeWithRegistry() {
			Scope scope = service.getScope();
			assertEquals(1, scope.getRegistries().size());
			assertEquals(WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME,
					scope.getRegistries().get(0).getName());
		}

		@Test
		@DisplayName("Should have correct scope description")
		void shouldHaveCorrectDescription() {
			Scope scope = service.getScope();
			assertEquals("Atlas Scope. The parent of all other scopes.", scope.getDescription());
		}
	}

	@Nested
	@DisplayName("Registry Validation Tests")
	class RegistryValidationTests {

		@Test
		@DisplayName("Should validate atlas-schema-registry as valid")
		void shouldValidateAtlasSchemaRegistry() {
			assertTrue(service.isValidRegistry(WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME));
		}

		@Test
		@DisplayName("Should reject invalid registry names")
		void shouldRejectInvalidRegistryNames() {
			assertFalse(service.isValidRegistry("other-registry"));
			assertFalse(service.isValidRegistry(""));
			assertFalse(service.isValidRegistry(null));
		}

		@Test
		@DisplayName("Should return single registry in list")
		void shouldReturnSingleRegistryInList() {
			List<String> registries = service.getAllRegistries();
			assertEquals(1, registries.size());
			assertEquals(WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME, registries.get(0));
		}

		@Test
		@DisplayName("Should throw on upload with invalid registry")
		void shouldThrowOnUploadWithInvalidRegistry() {
			EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
			ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();

			IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
					() -> service.uploadToStageForRegistry("invalid", "released", ePackage, metadata));
			assertTrue(ex.getMessage().contains("not a valid registry"));
			assertTrue(ex.getMessage().contains(WorkflowConstants.ATLAS_SCOPE_NAME));
		}

		@Test
		@DisplayName("Should throw on getMetadata with invalid registry")
		void shouldThrowOnGetMetadataWithInvalidRegistry() {
			assertThrows(IllegalArgumentException.class,
					() -> service.getMetadataFromStageForRegistry("invalid", "released", "id"));
		}

		@Test
		@DisplayName("Should throw on getMetadataFromFinalStage with invalid registry")
		void shouldThrowOnGetMetadataFromFinalStageWithInvalidRegistry() {
			assertThrows(IllegalArgumentException.class,
					() -> service.getMetadataFromFinalStageForRegistry("invalid", "id"));
		}

		@Test
		@DisplayName("Should throw on getContent with invalid registry")
		void shouldThrowOnGetContentWithInvalidRegistry() {
			assertThrows(IllegalArgumentException.class,
					() -> service.getContentFromStageForRegistry("invalid", "released", "id"));
		}

		@Test
		@DisplayName("Should throw on update with invalid registry")
		void shouldThrowOnUpdateWithInvalidRegistry() {
			EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
			assertThrows(IllegalArgumentException.class,
					() -> service.updateInStageForRegistry("invalid", "released", ePackage, "id", "1.0"));
		}

		@Test
		@DisplayName("Should throw on delete with invalid registry")
		void shouldThrowOnDeleteWithInvalidRegistry() {
			assertThrows(IllegalArgumentException.class,
					() -> service.deleteFromStageForRegistry("invalid", "released", "id"));
		}

		@Test
		@DisplayName("Should throw on list with invalid registry")
		void shouldThrowOnListWithInvalidRegistry() {
			assertThrows(IllegalArgumentException.class,
					() -> service.listInStageForRegistry("invalid", "released"));
		}

		@Test
		@DisplayName("Should throw on listByName with invalid registry")
		void shouldThrowOnListByNameWithInvalidRegistry() {
			assertThrows(IllegalArgumentException.class,
					() -> service.listInStageForRegistryByName("invalid", "released", "name"));
		}

		@Test
		@DisplayName("Should throw on listInFinalStage with invalid registry")
		void shouldThrowOnListInFinalStageWithInvalidRegistry() {
			assertThrows(IllegalArgumentException.class,
					() -> service.listInFinalStageForRegistry("invalid"));
		}

		@Test
		@DisplayName("Should throw on transition with invalid registry")
		void shouldThrowOnTransitionWithInvalidRegistry() {
			assertThrows(IllegalArgumentException.class,
					() -> service.transitionToStageForRegistry("invalid", "id", "released", "other"));
		}
	}

	@Nested
	@DisplayName("Delegation Tests")
	class DelegationTests {

		private static final String REGISTRY = WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME;
		private static final String SCOPE = WorkflowConstants.ATLAS_SCOPE_NAME;
		private static final String STAGE = "released";
		private static final String OBJECT_ID = "test-id";

		@Test
		@DisplayName("Should delegate upload to registry service with atlas scope")
		void shouldDelegateUpload() {
			EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
			ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
			ObjectMetadata resultMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
			Promise<ObjectMetadata> expected = promiseFactory.resolved(resultMetadata);
			when(mockRegistryService.uploadToStage(eq(SCOPE), eq(STAGE), any(), any())).thenReturn(expected);

			Promise<ObjectMetadata> result = service.uploadToStageForRegistry(REGISTRY, STAGE, ePackage, metadata);

			assertEquals(expected, result);
			verify(mockRegistryService).uploadToStage(eq(SCOPE), eq(STAGE), eq(ePackage), eq(metadata));
		}

		@Test
		@DisplayName("Should delegate getMetadataFromStage with atlas scope")
		void shouldDelegateGetMetadataFromStage() {
			ObjectMetadata expected = ManagementFactory.eINSTANCE.createObjectMetadata();
			when(mockRegistryService.getMetadataFromStage(SCOPE, STAGE, OBJECT_ID)).thenReturn(expected);

			ObjectMetadata result = service.getMetadataFromStageForRegistry(REGISTRY, STAGE, OBJECT_ID);

			assertEquals(expected, result);
			verify(mockRegistryService).getMetadataFromStage(SCOPE, STAGE, OBJECT_ID);
		}

		@Test
		@DisplayName("Should delegate getMetadataFromFinalStage with atlas scope")
		void shouldDelegateGetMetadataFromFinalStage() {
			ObjectMetadata expected = ManagementFactory.eINSTANCE.createObjectMetadata();
			when(mockRegistryService.getMetadataFromFinalStage(SCOPE, OBJECT_ID)).thenReturn(expected);

			ObjectMetadata result = service.getMetadataFromFinalStageForRegistry(REGISTRY, OBJECT_ID);

			assertEquals(expected, result);
			verify(mockRegistryService).getMetadataFromFinalStage(SCOPE, OBJECT_ID);
		}

		@Test
		@DisplayName("Should delegate getContentFromStage with atlas scope")
		void shouldDelegateGetContentFromStage() {
			EPackage expected = EcoreFactory.eINSTANCE.createEPackage();
			when(mockRegistryService.getContentFromStage(SCOPE, STAGE, OBJECT_ID)).thenReturn(expected);

			EPackage result = service.getContentFromStageForRegistry(REGISTRY, STAGE, OBJECT_ID);

			assertEquals(expected, result);
			verify(mockRegistryService).getContentFromStage(SCOPE, STAGE, OBJECT_ID);
		}

		@Test
		@DisplayName("Should delegate update with atlas scope")
		void shouldDelegateUpdate() {
			EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
			ObjectMetadata resultMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
			Promise<ObjectMetadata> expected = promiseFactory.resolved(resultMetadata);
			when(mockRegistryService.updateInStage(eq(SCOPE), eq(STAGE), any(), eq(OBJECT_ID), eq("1.0")))
					.thenReturn(expected);

			Promise<ObjectMetadata> result = service.updateInStageForRegistry(REGISTRY, STAGE, ePackage, OBJECT_ID, "1.0");

			assertEquals(expected, result);
			verify(mockRegistryService).updateInStage(SCOPE, STAGE, ePackage, OBJECT_ID, "1.0");
		}

		@Test
		@DisplayName("Should delegate delete with atlas scope")
		void shouldDelegateDelete() {
			Promise<Boolean> expected = promiseFactory.resolved(true);
			when(mockRegistryService.deleteFromStage(SCOPE, STAGE, OBJECT_ID)).thenReturn(expected);

			Promise<Boolean> result = service.deleteFromStageForRegistry(REGISTRY, STAGE, OBJECT_ID);

			assertEquals(expected, result);
			verify(mockRegistryService).deleteFromStage(SCOPE, STAGE, OBJECT_ID);
		}

		@Test
		@DisplayName("Should delegate listInStage with atlas scope")
		void shouldDelegateListInStage() {
			List<ObjectMetadata> expected = List.of(ManagementFactory.eINSTANCE.createObjectMetadata());
			when(mockRegistryService.listInStage(SCOPE, STAGE)).thenReturn(expected);

			List<ObjectMetadata> result = service.listInStageForRegistry(REGISTRY, STAGE);

			assertEquals(expected, result);
			verify(mockRegistryService).listInStage(SCOPE, STAGE);
		}

		@Test
		@DisplayName("Should delegate listInStageByName with atlas scope")
		void shouldDelegateListInStageByName() {
			String name = "TestPackage";
			List<ObjectMetadata> expected = List.of(ManagementFactory.eINSTANCE.createObjectMetadata());
			when(mockRegistryService.listInStageByName(SCOPE, STAGE, name)).thenReturn(expected);

			List<ObjectMetadata> result = service.listInStageForRegistryByName(REGISTRY, STAGE, name);

			assertEquals(expected, result);
			verify(mockRegistryService).listInStageByName(SCOPE, STAGE, name);
		}

		@Test
		@DisplayName("Should delegate listInFinalStage with atlas scope")
		void shouldDelegateListInFinalStage() {
			List<ObjectMetadata> expected = List.of(ManagementFactory.eINSTANCE.createObjectMetadata());
			when(mockRegistryService.listInFinalStage(SCOPE)).thenReturn(expected);

			List<ObjectMetadata> result = service.listInFinalStageForRegistry(REGISTRY);

			assertEquals(expected, result);
			verify(mockRegistryService).listInFinalStage(SCOPE);
		}

		@Test
		@DisplayName("Should delegate transition with atlas scope")
		void shouldDelegateTransition() {
			ObjectMetadata expected = ManagementFactory.eINSTANCE.createObjectMetadata();
			when(mockRegistryService.transitionToStage(SCOPE, OBJECT_ID, "released", "other")).thenReturn(expected);

			ObjectMetadata result = service.transitionToStageForRegistry(REGISTRY, OBJECT_ID, "released", "other");

			assertEquals(expected, result);
			verify(mockRegistryService).transitionToStage(SCOPE, OBJECT_ID, "released", "other");
		}
	}
}
