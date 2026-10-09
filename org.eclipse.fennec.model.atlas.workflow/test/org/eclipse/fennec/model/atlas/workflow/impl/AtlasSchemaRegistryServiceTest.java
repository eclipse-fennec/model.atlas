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
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.fennec.model.atlas.management.lucene.epackage.EPackageLuceneIndex;
import org.eclipse.fennec.model.atlas.mgmt.registry.RegistryAddress;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.scope.api.RegistryType;
import org.eclipse.fennec.model.atlas.scope.api.StageInfo;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Registry;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
		// What the shared registry already holds about a schema is what decides whether its
		// timestamps move, so every mirror asks. Nothing there is the ordinary case.
		lenient().when(registryService.getMetadata(any(), any(), any(), any())).thenReturn(Optional.empty());
	}

	/** What the one static registry of {@link #mirrored(EPackage)} currently holds. */
	private final List<EPackage> staticContents = new ArrayList<>();

	private EPackage.Registry boundStaticRegistry;

	/**
	 * Puts one package into the static registry and hands back the metadata that reached the
	 * shared registry for it.
	 * <p>
	 * The same registry instance every time: a reconcile is answered only for the registry this
	 * component is bound to, so a second mock would be ignored as a late signal from one it no
	 * longer follows - and the test would pass by never mirroring anything at all.
	 */
	private ObjectMetadata mirrored(EPackage ePackage) {
		staticContents.clear();
		staticContents.add(ePackage);
		if (boundStaticRegistry == null) {
			boundStaticRegistry = mock(EPackage.Registry.class);
			when(boundStaticRegistry.values()).thenAnswer(inv -> new ArrayList<EPackage>(staticContents));
			service.bindStaticEPackageRegistry(boundStaticRegistry);
		} else {
			service.updatedStaticEPackageRegistry(boundStaticRegistry);
		}
		ArgumentCaptor<ObjectMetadata> captor = ArgumentCaptor.forClass(ObjectMetadata.class);
		verify(registryService, atLeastOnce()).updateCache(captor.capture());
		return captor.getValue();
	}

	@Nested
	@DisplayName("System schema metadata (issue #359)")
	class SystemSchemaMetadataTests {

		private static final String COMPILED_NS_URI = "http://www.eclipse.org/fennec/m2x/compiled/1.0";

		@Test
		@DisplayName("Should record the version the model declares")
		void shouldRecordTheDeclaredVersion() {
			EPackage declaring = ePackage("declaring", "http://test/declaring");
			EAnnotation annotation = EcoreFactory.eINSTANCE.createEAnnotation();
			annotation.setSource("Version");
			annotation.getDetails().put("value", "2.1.0");
			declaring.getEAnnotations().add(annotation);

			assertEquals("2.1.0", mirrored(declaring).getVersion(),
					"a model that states its own version is taken at its word");
		}

		@Test
		@DisplayName("Should infer the version from a version-shaped last nsURI segment")
		void shouldInferTheVersionFromTheNsUri() {
			// The case the issue reports: the compiled schema shows no version in gene and xdp-ui,
			// although its nsURI ends in one.
			assertEquals("1.0.0", mirrored(ePackage("compiled", COMPILED_NS_URI)).getVersion());
		}

		@Test
		@DisplayName("Should leave the version unset when a segment only looks like a number")
		void shouldNotReadAYearAsAVersion() {
			assertNull(mirrored(ePackage("ecore", "http://www.eclipse.org/emf/2002/Ecore")).getVersion(),
					"a year is not a version - issue #180");
		}

		@Test
		@DisplayName("Should record a content hash and a fingerprint")
		void shouldRecordAContentHashAndAFingerprint() {
			ObjectMetadata metadata = mirrored(ePackage("compiled", COMPILED_NS_URI));

			assertNotNull(metadata.getContentHash(), "without it a client cannot see that a schema changed");
			assertNotNull(metadata.getFingerprint(), "and cannot match the schema by fingerprint (#156)");
			assertTrue(metadata.getFingerprint().startsWith("fp1:"), metadata.getFingerprint());
			assertNotNull(metadata.getLastChangeTime());
		}

		@Test
		@DisplayName("Should keep the timestamps of a schema whose content did not change")
		void shouldKeepTheTimestampsOfAnUnchangedSchema() {
			ObjectMetadata first = mirrored(ePackage("compiled", COMPILED_NS_URI));
			Instant uploaded = Instant.parse("2026-09-01T10:00:00Z");
			Instant changed = Instant.parse("2026-09-02T11:00:00Z");
			first.setUploadTime(uploaded);
			first.setLastChangeTime(changed);
			when(registryService.getMetadata(any(), any(), any(), any())).thenReturn(Optional.of(first));

			// A restart re-reads the static registry and mirrors the same schema again, as a new
			// instance. uploadTime moving on every start is what the issue reports.
			ObjectMetadata second = mirrored(ePackage("compiled", COMPILED_NS_URI));

			assertEquals(uploaded, second.getUploadTime(), "the schema was not uploaded again");
			assertEquals(changed, second.getLastChangeTime(), "and its content did not change");
		}

		@Test
		@DisplayName("Should move the last change time when the content did change")
		void shouldMoveTheLastChangeTimeOfAChangedSchema() {
			ObjectMetadata first = mirrored(ePackage("compiled", COMPILED_NS_URI));
			Instant uploaded = Instant.parse("2026-09-01T10:00:00Z");
			Instant changed = Instant.parse("2026-09-02T11:00:00Z");
			first.setUploadTime(uploaded);
			first.setLastChangeTime(changed);
			when(registryService.getMetadata(any(), any(), any(), any())).thenReturn(Optional.of(first));

			EPackage grown = ePackage("compiled", COMPILED_NS_URI);
			EClass added = EcoreFactory.eINSTANCE.createEClass();
			added.setName("CompiledUnit");
			grown.getEClassifiers().add(added);
			ObjectMetadata second = mirrored(grown);

			assertEquals(uploaded, second.getUploadTime(), "when it was first seen does not change");
			assertTrue(second.getLastChangeTime().isAfter(changed),
					"but the content did: " + second.getLastChangeTime());
		}
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
			StageInfo stage = registry.getStages().get(0);
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
			when(registryService.getMetadata(WorkflowConstants.ATLAS_SCOPE_NAME, WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME,
					WorkflowConstants.ATLAS_SCHEMA_REGISTRY_STAGE_NAME, objectId)).thenReturn(Optional.of(expectedMetadata));

			ObjectMetadata result = service.getMetadataFromFinalStage("atlas", objectId);

			assertNotNull(result);
			assertEquals(objectId, result.getObjectId());
			verify(registryService).getMetadata(WorkflowConstants.ATLAS_SCOPE_NAME, WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME,
					WorkflowConstants.ATLAS_SCHEMA_REGISTRY_STAGE_NAME, objectId);
		}

		@Test
		@DisplayName("Should return null when metadata not found")
		void shouldReturnNullWhenMetadataNotFound() {
			when(registryService.getMetadata(WorkflowConstants.ATLAS_SCOPE_NAME, WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME,
					WorkflowConstants.ATLAS_SCHEMA_REGISTRY_STAGE_NAME, "missing-id")).thenReturn(Optional.empty());

			ObjectMetadata result = service.getMetadataFromFinalStage("atlas", "missing-id");

			assertNull(result);
		}

		@Test
		@DisplayName("Should delegate getMetadataFromStage to getMetadataFromFinalStage for released stage")
		void shouldDelegateGetMetadataFromStageToFinalStage() {
			String objectId = "test-id";
			ObjectMetadata expectedMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
			when(registryService.getMetadata(WorkflowConstants.ATLAS_SCOPE_NAME, WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME,
					WorkflowConstants.ATLAS_SCHEMA_REGISTRY_STAGE_NAME, objectId)).thenReturn(Optional.of(expectedMetadata));

			ObjectMetadata result = service.getMetadataFromStage("atlas", "released", objectId);

			assertNotNull(result);
			verify(registryService).getMetadata(WorkflowConstants.ATLAS_SCOPE_NAME, WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME,
					WorkflowConstants.ATLAS_SCHEMA_REGISTRY_STAGE_NAME, objectId);
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

			service.bindStaticEPackageRegistry(mockStaticRegistry);
			service.unbindStaticEPackageRegistry(mockStaticRegistry);

			String expectedId = new String(Base64.getUrlEncoder().encode("http://test/package".getBytes()));
			verify(registryService).removeFromCache(WorkflowConstants.ATLAS_SCOPE_NAME, WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME,
					WorkflowConstants.ATLAS_SCHEMA_REGISTRY_STAGE_NAME, expectedId);
			// The search index is addressed too: dropping this stage's entry must not deindex
			// the copies the same id has in other stages (issue #252)
			verify(ePackageIndex).remove(new RegistryAddress(WorkflowConstants.ATLAS_SCOPE_NAME,
					WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME,
					WorkflowConstants.ATLAS_SCHEMA_REGISTRY_STAGE_NAME, expectedId));
		}

		@Test
		@DisplayName("Should ignore an unbind of a registry that is not the bound one")
		void shouldIgnoreUnbindOfForeignRegistry() {
			EPackage testPackage = ePackage("TestPackage", "http://test/package");
			EPackage.Registry bound = registryWith(testPackage);
			// never read: an unbind of a registry that is not the bound one must not touch it
			EPackage.Registry foreign = mock(EPackage.Registry.class);

			service.bindStaticEPackageRegistry(bound);
			service.unbindStaticEPackageRegistry(foreign);

			verify(registryService, never()).removeFromCache(any(), any(), any(), any());
			verify(ePackageIndex, never()).remove(any(RegistryAddress.class));
		}
	}

	/**
	 * The static registry announces content changes by re-publishing its service
	 * properties; DS delivers those as {@code updated} callbacks (issue #282).
	 */
	@Nested
	@DisplayName("Static EPackage Registry Update Tests")
	class StaticEPackageRegistryUpdateTests {

		@Test
		@DisplayName("Should mirror a package that appears after the registry was bound")
		void shouldMirrorPackageAddedAfterBind() {
			EPackage first = ePackage("First", "http://test/first");
			EPackage late = ePackage("Late", "http://test/late");
			Map<String, Object> registryMap = new LinkedHashMap<>();
			registryMap.put(first.getNsURI(), first);
			EPackage.Registry mockStaticRegistry = mock(EPackage.Registry.class);
			when(mockStaticRegistry.values()).thenAnswer(inv -> new ArrayList<>(registryMap.values()));

			service.bindStaticEPackageRegistry(mockStaticRegistry);
			registryMap.put(late.getNsURI(), late);
			service.updatedStaticEPackageRegistry(mockStaticRegistry);

			verify(registryService).updateCache(argThat(md -> objectId(first).equals(md.getObjectId())));
			verify(registryService).updateCache(argThat(md -> objectId(late).equals(md.getObjectId())));
			verify(ePackageIndex).index(argThat(md -> objectId(late).equals(md.getObjectId())), same(late));
			verify(registryService, times(2)).updateCache(any(ObjectMetadata.class));
			verify(registryService, never()).removeFromCache(any(), any(), any(), any());
		}

		@Test
		@DisplayName("Should forget only the package that left the registry")
		void shouldForgetOnlyTheRemovedPackage() {
			EPackage staying = ePackage("Staying", "http://test/staying");
			EPackage leaving = ePackage("Leaving", "http://test/leaving");
			Map<String, Object> registryMap = new LinkedHashMap<>();
			registryMap.put(staying.getNsURI(), staying);
			registryMap.put(leaving.getNsURI(), leaving);
			EPackage.Registry mockStaticRegistry = mock(EPackage.Registry.class);
			when(mockStaticRegistry.values()).thenAnswer(inv -> new ArrayList<>(registryMap.values()));

			service.bindStaticEPackageRegistry(mockStaticRegistry);
			registryMap.remove(leaving.getNsURI());
			service.updatedStaticEPackageRegistry(mockStaticRegistry);

			RegistryAddress leavingAddress = new RegistryAddress(WorkflowConstants.ATLAS_SCOPE_NAME,
					WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME, WorkflowConstants.ATLAS_SCHEMA_REGISTRY_STAGE_NAME,
					objectId(leaving));
			verify(registryService).removeFromCache(WorkflowConstants.ATLAS_SCOPE_NAME,
					WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME, WorkflowConstants.ATLAS_SCHEMA_REGISTRY_STAGE_NAME,
					objectId(leaving));
			verify(ePackageIndex).remove(leavingAddress);
			verify(registryService, times(1)).removeFromCache(any(), any(), any(), any());
			verify(ePackageIndex, times(1)).remove(any(RegistryAddress.class));
			// the survivor was mirrored once at bind and not touched again
			verify(registryService, times(2)).updateCache(any(ObjectMetadata.class));
		}

		@Test
		@DisplayName("Should do nothing on an update that changed no content")
		void shouldNotTouchCacheOnUnchangedUpdate() {
			EPackage testPackage = ePackage("TestPackage", "http://test/package");
			EPackage.Registry mockStaticRegistry = registryWith(testPackage);

			service.bindStaticEPackageRegistry(mockStaticRegistry);
			service.updatedStaticEPackageRegistry(mockStaticRegistry);
			service.updatedStaticEPackageRegistry(mockStaticRegistry);

			verify(registryService, times(1)).updateCache(any(ObjectMetadata.class));
			verify(ePackageIndex, times(1)).index(any(ObjectMetadata.class), any(EPackage.class));
			verify(registryService, never()).removeFromCache(any(), any(), any(), any());
		}

		@Test
		@DisplayName("Should re-index a package whose instance was replaced under the same nsURI")
		void shouldReindexReplacedInstance() {
			EPackage original = ePackage("TestPackage", "http://test/package");
			EPackage replacement = ePackage("TestPackage", "http://test/package");
			Map<String, Object> registryMap = new LinkedHashMap<>();
			registryMap.put(original.getNsURI(), original);
			EPackage.Registry mockStaticRegistry = mock(EPackage.Registry.class);
			when(mockStaticRegistry.values()).thenAnswer(inv -> new ArrayList<>(registryMap.values()));

			service.bindStaticEPackageRegistry(mockStaticRegistry);
			registryMap.put(replacement.getNsURI(), replacement);
			service.updatedStaticEPackageRegistry(mockStaticRegistry);

			verify(ePackageIndex).index(any(ObjectMetadata.class), same(original));
			verify(ePackageIndex).index(any(ObjectMetadata.class), same(replacement));
			verify(registryService, never()).removeFromCache(any(), any(), any(), any());
		}

		@Test
		@DisplayName("Should skip packages the static registry does not announce yet")
		void shouldSkipUnannouncedPackages() {
			EPackage nameless = EcoreFactory.eINSTANCE.createEPackage();
			nameless.setNsURI("http://test/nameless");
			EPackage uriless = EcoreFactory.eINSTANCE.createEPackage();
			uriless.setName("Uriless");
			Map<String, Object> registryMap = new LinkedHashMap<>();
			registryMap.put("http://test/nameless", nameless);
			registryMap.put("http://test/uriless", uriless);
			EPackage.Registry mockStaticRegistry = mock(EPackage.Registry.class);
			when(mockStaticRegistry.values()).thenAnswer(inv -> new ArrayList<>(registryMap.values()));

			service.bindStaticEPackageRegistry(mockStaticRegistry);

			verify(registryService, never()).updateCache(any(ObjectMetadata.class));
		}

		@Test
		@DisplayName("Should ignore an update from a registry that is no longer bound")
		void shouldIgnoreUpdateFromUnboundRegistry() {
			EPackage testPackage = ePackage("TestPackage", "http://test/package");
			EPackage.Registry mockStaticRegistry = registryWith(testPackage);

			service.bindStaticEPackageRegistry(mockStaticRegistry);
			service.unbindStaticEPackageRegistry(mockStaticRegistry);
			service.updatedStaticEPackageRegistry(mockStaticRegistry);

			verify(registryService, times(1)).updateCache(any(ObjectMetadata.class));
		}
	}

	private static EPackage ePackage(String name, String nsUri) {
		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		ePackage.setName(name);
		ePackage.setNsURI(nsUri);
		ePackage.setNsPrefix(name.toLowerCase());
		return ePackage;
	}

	private static EPackage.Registry registryWith(EPackage... ePackages) {
		Map<String, Object> registryMap = new LinkedHashMap<>();
		for (EPackage ePackage : ePackages) {
			registryMap.put(ePackage.getNsURI(), ePackage);
		}
		EPackage.Registry mockStaticRegistry = mock(EPackage.Registry.class);
		when(mockStaticRegistry.values()).thenAnswer(inv -> new ArrayList<>(registryMap.values()));
		return mockStaticRegistry;
	}

	private static String objectId(EPackage ePackage) {
		return Base64.getUrlEncoder().encodeToString(ePackage.getNsURI().getBytes(StandardCharsets.UTF_8));
	}

	@Nested
	@DisplayName("Static EPackage Registry Binding Tests (metadata)")
	class StaticEPackageRegistryMetadataTests {

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

		@Test
		@DisplayName("Should return EPackage as the only accepted root EClass")
		void shouldReturnEPackageAsOnlyRootEClass() {
			assertEquals(java.util.List.of(EcorePackage.Literals.EPACKAGE), service.getRootEClasses());
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
