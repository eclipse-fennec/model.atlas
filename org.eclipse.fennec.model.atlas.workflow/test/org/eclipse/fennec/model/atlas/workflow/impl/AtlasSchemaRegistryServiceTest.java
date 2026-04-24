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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.fennec.model.atlas.management.lucene.epackage.EPackageLuceneIndex;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Registry;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryType;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Stage;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link AtlasSchemaRegistryService}.
 *
 * @author ilenia
 * @since Mar 30, 2026
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AtlasSchemaRegistryService Unit Tests")
public class AtlasSchemaRegistryServiceTest {

	@Mock
	private EObjectRegistryService<EObject> registryService;
	
	@Mock
	private EPackageLuceneIndex ePackageIndex;

	private AtlasSchemaRegistryService service;

	@BeforeEach
	void setUp() {
		service = new AtlasSchemaRegistryService(registryService, ePackageIndex);
	}

	@Nested
	@DisplayName("Registry Object Tests")
	class RegistryObjectTests {

		@Test
		@DisplayName("Should create registry with correct name")
		void shouldCreateRegistryWithCorrectName() {
			Registry registry = service.getRegistry();
			assertNotNull(registry);
			assertEquals(WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME, registry.getName());
		}

		@Test
		@DisplayName("Should create registry marked as schema registry")
		void shouldCreateRegistryMarkedAsSchemaRegistry() {
			Registry registry = service.getRegistry();
			assertEquals(RegistryType.SCHEMA, registry.getType());
		}

		@Test
		@DisplayName("Should create registry with single released stage")
		void shouldCreateRegistryWithSingleReleasedStage() {
			Registry registry = service.getRegistry();
			assertEquals(1, registry.getStages().size());
			Stage stage = registry.getStages().get(0);
			assertEquals(WorkflowConstants.ATLAS_SCHEMA_REGISTRY_STAGE_NAME, stage.getName());
			assertFalse(stage.isWritable());
			assertTrue(stage.isFinal());
		}

		@Test
		@DisplayName("Should return correct registry name")
		void shouldReturnCorrectRegistryName() {
			assertEquals(WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME, service.getRegistryName());
		}
	}

	@Nested
	@DisplayName("Read-Only Behavior Tests")
	class ReadOnlyBehaviorTests {

		@Test
		@DisplayName("Should throw UnsupportedOperationException on upload")
		void shouldThrowOnUpload() {
			EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
			ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
			assertThrows(UnsupportedOperationException.class,
					() -> service.uploadToStage("atlas", "released", ePackage, metadata));
		}

		@Test
		@DisplayName("Should throw UnsupportedOperationException on update")
		void shouldThrowOnUpdate() {
			EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
			assertThrows(UnsupportedOperationException.class,
					() -> service.updateInStage("atlas", "released", ePackage, "id", "1.0"));
		}

		@Test
		@DisplayName("Should throw UnsupportedOperationException on delete")
		void shouldThrowOnDelete() {
			assertThrows(UnsupportedOperationException.class,
					() -> service.deleteFromStage("atlas", "released", "id"));
		}

		@Test
		@DisplayName("Should throw UnsupportedOperationException on transition")
		void shouldThrowOnTransition() {
			assertThrows(UnsupportedOperationException.class,
					() -> service.transitionToStage("atlas", "id", "released", "other"));
		}

		@Test
		@DisplayName("Should report no writable stages")
		void shouldReportNoWritableStages() {
			assertFalse(service.isWritableStage("released"));
			assertFalse(service.isWritableStage("draft"));
		}

		@Test
		@DisplayName("Should report final stage not writable")
		void shouldReportFinalStageNotWritable() {
			assertFalse(service.isFinalStageWritable());
		}

		@Test
		@DisplayName("Should not allow any transitions")
		void shouldNotAllowAnyTransitions() {
			assertFalse(service.isTransitionAllowed("released", "draft"));
			assertFalse(service.isTransitionAllowed("draft", "released"));
		}
	}

	@Nested
	@DisplayName("Stage Validation Tests")
	class StageValidationTests {

		@Test
		@DisplayName("Should validate released stage")
		void shouldValidateReleasedStage() {
			assertTrue(service.isValidStage("released"));
		}

		@Test
		@DisplayName("Should reject invalid stage")
		void shouldRejectInvalidStage() {
			assertFalse(service.isValidStage("draft"));
			assertFalse(service.isValidStage("approved"));
			assertFalse(service.isValidStage(null));
		}

		@Test
		@DisplayName("Should throw on getMetadataFromStage with invalid stage")
		void shouldThrowOnGetMetadataWithInvalidStage() {
			assertThrows(IllegalArgumentException.class,
					() -> service.getMetadataFromStage("atlas", "draft", "id"));
		}

		@Test
		@DisplayName("Should throw on getContentFromStage with invalid stage")
		void shouldThrowOnGetContentWithInvalidStage() {
			assertThrows(IllegalArgumentException.class,
					() -> service.getContentFromStage("atlas", "draft", "id"));
		}

		@Test
		@DisplayName("Should throw on listInStage with invalid stage")
		void shouldThrowOnListInStageWithInvalidStage() {
			assertThrows(IllegalArgumentException.class,
					() -> service.listInStage("atlas", "draft"));
		}

		@Test
		@DisplayName("Should throw on listInStageByName with invalid stage")
		void shouldThrowOnListInStageByNameWithInvalidStage() {
			assertThrows(IllegalArgumentException.class,
					() -> service.listInStageByName("atlas", "draft", "name"));
		}
	}

	@Nested
	@DisplayName("Read Operations Tests")
	class ReadOperationsTests {

		@Test
		@DisplayName("Should delegate getMetadataFromFinalStage to registry")
		void shouldDelegateGetMetadataFromFinalStage() {
			String objectId = "test-id";
			ObjectMetadata expectedMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
			expectedMetadata.setObjectId(objectId);
			when(registryService.getMetadata(objectId)).thenReturn(Optional.of(expectedMetadata));

			ObjectMetadata result = service.getMetadataFromFinalStage("atlas", objectId);

			assertNotNull(result);
			assertEquals(objectId, result.getObjectId());
			verify(registryService).getMetadata(objectId);
		}

		@Test
		@DisplayName("Should return null when metadata not found")
		void shouldReturnNullWhenMetadataNotFound() {
			when(registryService.getMetadata("missing-id")).thenReturn(Optional.empty());

			ObjectMetadata result = service.getMetadataFromFinalStage("atlas", "missing-id");

			assertNull(result);
		}

		@Test
		@DisplayName("Should delegate getMetadataFromStage to getMetadataFromFinalStage for released stage")
		void shouldDelegateGetMetadataFromStageToFinalStage() {
			String objectId = "test-id";
			ObjectMetadata expectedMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
			when(registryService.getMetadata(objectId)).thenReturn(Optional.of(expectedMetadata));

			ObjectMetadata result = service.getMetadataFromStage("atlas", "released", objectId);

			assertNotNull(result);
			verify(registryService).getMetadata(objectId);
		}

		@Test
		@DisplayName("Should delegate listInFinalStage to registry")
		void shouldDelegateListInFinalStage() {
			ObjectMetadata metadata1 = ManagementFactory.eINSTANCE.createObjectMetadata();
			ObjectMetadata metadata2 = ManagementFactory.eINSTANCE.createObjectMetadata();
			List<ObjectMetadata> expected = List.of(metadata1, metadata2);
			when(registryService.findByScopeRegistryAndStage("atlas",
					WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME,
					WorkflowConstants.ATLAS_SCHEMA_REGISTRY_STAGE_NAME))
					.thenReturn(expected);

			List<ObjectMetadata> result = service.listInFinalStage("atlas");

			assertEquals(2, result.size());
			verify(registryService).findByScopeRegistryAndStage("atlas",
					WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME,
					WorkflowConstants.ATLAS_SCHEMA_REGISTRY_STAGE_NAME);
		}

		@Test
		@DisplayName("Should delegate listInStage to listInFinalStage for released stage")
		void shouldDelegateListInStageToFinalStage() {
			List<ObjectMetadata> expected = List.of();
			when(registryService.findByScopeRegistryAndStage("atlas",
					WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME,
					WorkflowConstants.ATLAS_SCHEMA_REGISTRY_STAGE_NAME))
					.thenReturn(expected);

			List<ObjectMetadata> result = service.listInStage("atlas", "released");

			assertEquals(0, result.size());
		}

		@Test
		@DisplayName("Should delegate listInStageByName to registry")
		void shouldDelegateListInStageByName() {
			String name = "TestPackage";
			List<ObjectMetadata> expected = List.of(ManagementFactory.eINSTANCE.createObjectMetadata());
			when(registryService.findByScopeRegistryStageAndName("atlas",
					WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME,
					WorkflowConstants.ATLAS_SCHEMA_REGISTRY_STAGE_NAME, name))
					.thenReturn(expected);

			List<ObjectMetadata> result = service.listInStageByName("atlas", "released", name);

			assertEquals(1, result.size());
			verify(registryService).findByScopeRegistryStageAndName("atlas",
					WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME,
					WorkflowConstants.ATLAS_SCHEMA_REGISTRY_STAGE_NAME, name);
		}
	}

	@Nested
	@DisplayName("Static EPackage Registry Binding Tests")
	class StaticEPackageRegistryBindingTests {

		@Test
		@DisplayName("Should update cache when static registry is bound")
		void shouldUpdateCacheWhenStaticRegistryBound() {
			EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
			testPackage.setName("TestPackage");
			testPackage.setNsURI("http://test/package");
			testPackage.setNsPrefix("test");

			Map<String, Object> registryMap = new HashMap<>();
			registryMap.put("http://test/package", testPackage);

			EPackage.Registry mockStaticRegistry = mock(EPackage.Registry.class);
			when(mockStaticRegistry.values()).thenReturn(registryMap.values());

			service.bindStaticEPackageRegistry(mockStaticRegistry);

			verify(registryService, times(1)).updateCache(any(ObjectMetadata.class));
		}

		@Test
		@DisplayName("Should filter out non-EPackage values when binding")
		void shouldFilterNonEPackageValues() {
			Map<String, Object> registryMap = new HashMap<>();
			registryMap.put("http://test/package", "not-an-epackage");

			EPackage.Registry mockStaticRegistry = mock(EPackage.Registry.class);
			when(mockStaticRegistry.values()).thenReturn(registryMap.values());

			service.bindStaticEPackageRegistry(mockStaticRegistry);

			verify(registryService, never()).updateCache(any(ObjectMetadata.class));
		}

		@Test
		@DisplayName("Should remove from cache when static registry is unbound")
		void shouldRemoveFromCacheWhenStaticRegistryUnbound() {
			EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
			testPackage.setName("TestPackage");
			testPackage.setNsURI("http://test/package");
			testPackage.setNsPrefix("test");

			Map<String, Object> registryMap = new HashMap<>();
			registryMap.put("http://test/package", testPackage);

			EPackage.Registry mockStaticRegistry = mock(EPackage.Registry.class);
			when(mockStaticRegistry.values()).thenReturn(registryMap.values());

			service.unbindStaticEPackageRegistry(mockStaticRegistry);

			String expectedId = new String(Base64.getUrlEncoder().encode("http://test/package".getBytes()));
			verify(registryService).removeFromCache(expectedId);
		}

		@Test
		@DisplayName("Should create correct metadata for bound EPackage")
		void shouldCreateCorrectMetadataForBoundEPackage() {
			EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
			testPackage.setName("TestPackage");
			testPackage.setNsURI("http://test/package");
			testPackage.setNsPrefix("test");

			Map<String, Object> registryMap = new HashMap<>();
			registryMap.put("http://test/package", testPackage);

			EPackage.Registry mockStaticRegistry = mock(EPackage.Registry.class);
			when(mockStaticRegistry.values()).thenReturn(registryMap.values());

			service.bindStaticEPackageRegistry(mockStaticRegistry);

			verify(registryService).updateCache(org.mockito.ArgumentMatchers.argThat(metadata -> {
				String expectedId = new String(Base64.getUrlEncoder().encode("http://test/package".getBytes()));
				return expectedId.equals(metadata.getObjectId())
						&& "TestPackage".equals(metadata.getObjectName())
						&& metadata.isIsReadOnly()
						&& WorkflowConstants.ATLAS_SCOPE_NAME.equals(metadata.getScope())
						&& WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME.equals(metadata.getRegistry())
						&& WorkflowConstants.ATLAS_SCHEMA_REGISTRY_STAGE_NAME.equals(metadata.getStage())
						&& "system".equals(metadata.getUploadUser())
						&& metadata.getUploadTime() != null;
			}));
		}
	}

	@Nested
	@DisplayName("Content Retrieval Tests")
	class ContentRetrievalTests {

		@Test
		@DisplayName("Should return null when no static registry is bound")
		void shouldReturnNullWhenNoStaticRegistryBound() {
			EPackage result = service.getContentFromStage("atlas", "released", "some-id");
			assertNull(result);
		}

		@Test
		@DisplayName("Should retrieve EPackage from static registry by decoded ID")
		void shouldRetrieveEPackageFromStaticRegistry() {
			String nsURI = "http://test/package";
			String encodedId = new String(Base64.getUrlEncoder().encode(nsURI.getBytes()));

			EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
			testPackage.setName("TestPackage");
			testPackage.setNsURI(nsURI);

			Map<String, Object> registryMap = new HashMap<>();
			registryMap.put(nsURI, testPackage);

			EPackage.Registry mockStaticRegistry = mock(EPackage.Registry.class);
			when(mockStaticRegistry.values()).thenReturn(registryMap.values());
			when(mockStaticRegistry.getEPackage(any())).thenReturn(testPackage);

			service.bindStaticEPackageRegistry(mockStaticRegistry);

			EPackage result = service.getContentFromStage("atlas", "released", encodedId);

			assertNotNull(result);
			verify(mockStaticRegistry).getEPackage(any());
		}
	}

	@Nested
	@DisplayName("EClass Compatibility Tests")
	class EClassCompatibilityTests {

		@Test
		@DisplayName("Should be compatible with EPackage EClass")
		void shouldBeCompatibleWithEPackage() {
			assertTrue(service.isEClassCompatibleWithRegistry(EcorePackage.Literals.EPACKAGE));
		}

		@Test
		@DisplayName("Should not be compatible with EClass EClass")
		void shouldNotBeCompatibleWithEClass() {
			assertFalse(service.isEClassCompatibleWithRegistry(EcorePackage.Literals.ECLASS));
		}

		@Test
		@DisplayName("Should return EPackage as root EClass")
		void shouldReturnEPackageAsRootEClass() {
			assertEquals(EcorePackage.Literals.EPACKAGE, service.getRootEClass());
		}
	}

	@Nested
	@DisplayName("Activate/Deactivate Tests")
	class ActivateDeactivateTests {

		@Test
		@DisplayName("Should return null on activate")
		void shouldReturnNullOnActivate() {
			assertNull(service.activate("atlas"));
		}

		@Test
		@DisplayName("Should return null on deactivate")
		void shouldReturnNullOnDeactivate() {
			assertNull(service.deactivate("atlas"));
		}
	}
}
