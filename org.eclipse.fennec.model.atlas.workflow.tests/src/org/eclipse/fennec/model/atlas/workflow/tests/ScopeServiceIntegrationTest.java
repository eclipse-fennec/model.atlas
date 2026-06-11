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
package org.eclipse.fennec.model.atlas.workflow.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations.EPackageLuceneIndexSetup;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations.RegistryConfiguration;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.eclipse.fennec.model.atlas.workflow.tests.annotations.TestAnnotations;
import org.eclipse.fennec.model.atlas.workflow.tests.annotations.TestAnnotations.ParentScopeServiceSetup;
import org.eclipse.fennec.model.atlas.workflow.tests.annotations.TestAnnotations.ScopeServiceSetup;
import org.eclipse.fennec.model.atlas.workflow.tests.support.LuceneAwareTempDirExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * OSGi Integration tests for {@link ScopeService}.
 *
 * Tests the orchestration and delegation logic of ScopeService with mocked
 * RegistryService in a real OSGi container environment.
 *
 * @author Claude
 * @since Jan 13, 2026
 */
@RequireEMF
@RequireConfigurationAdmin
@ExtendWith(LuceneAwareTempDirExtension.class)
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@DisplayName("ScopeService OSGi Integration Tests")
public class ScopeServiceIntegrationTest {

	private static final String SCOPE_NAME = "test-scope";
	private static final String REGISTRY_NAME = "test-registry";
	private static final String STAGE_NAME = "draft";
	private static final String OBJECT_ID = "test-object-id";

	@InjectBundleContext
	BundleContext bundleContext;

	@SuppressWarnings("rawtypes")
	private ServiceRegistration<RegistryService> mockRegistryRegistration;
	private RegistryService<EObject> mockRegistryService;
	private PromiseFactory promiseFactory;

	@BeforeEach
	void setUp() {
		promiseFactory = new PromiseFactory(null);

		// Create mock RegistryService
		@SuppressWarnings("unchecked")
		RegistryService<EObject> mock = mock(RegistryService.class);
		mockRegistryService = mock;

		// Configure mock registry service
		when(mockRegistryService.getRegistryName()).thenReturn(REGISTRY_NAME);

		// Register mock as OSGi service with registry.name property
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put("registry.name", REGISTRY_NAME);
		properties.put("service.ranking", Integer.valueOf(1000)); // High ranking

		mockRegistryRegistration = bundleContext.registerService(RegistryService.class, mockRegistryService,
				properties);
	}

	@AfterEach
	void tearDown() {
		if (mockRegistryRegistration != null) {
			try {
				mockRegistryRegistration.unregister();
			} catch (IllegalStateException e) {
				// Already unregistered
			}
		}
	}

	@Nested
	@DisplayName("Basic Delegation Tests")
	class BasicDelegationTests {

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Test
		@DisplayName("Should delegate upload to RegistryService with correct scope")
		@RegistryConfiguration
		@EPackageLuceneIndexSetup
		@WithFactoryConfiguration(factoryPid = "ScopeService", name = "test-scope", location = "?", properties = {
				@Property(key = "scope.name", value = SCOPE_NAME), @Property(key = "scope.parent", value = ""),
				@Property(key = "registryService.target", value = "(registry.name=" + REGISTRY_NAME + ")") })
		void shouldDelegateUploadWithCorrectScope(
				@InjectService(cardinality = 0, filter = "(scope.name=" + SCOPE_NAME
				+ ")") ServiceAware<ScopeService> scopeAware)
						throws InterruptedException, InvocationTargetException {

			// Wait for service
			ScopeService<EObject> scopeService = scopeAware.waitForService(5000);
			assertNotNull(scopeService, "ScopeService should be available");

			// Prepare test data
			EObject testObject = EcoreFactory.eINSTANCE.createEClass();
			ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
			metadata.setObjectId(OBJECT_ID);
			ObjectMetadata resultMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();

			Promise<ObjectMetadata> expectedPromise = promiseFactory.resolved(resultMetadata);
			when(mockRegistryService.uploadToStage(eq(SCOPE_NAME), eq(STAGE_NAME), any(), any()))
			.thenReturn(expectedPromise);

			// Act
			Promise<ObjectMetadata> result = scopeService.uploadToStageForRegistry(REGISTRY_NAME, STAGE_NAME,
					testObject, metadata);

			// Assert
			assertNotNull(result);
			assertEquals(resultMetadata, result.getValue());

			// Verify delegation with correct scope name
			verify(mockRegistryService).uploadToStage(eq(SCOPE_NAME), eq(STAGE_NAME), any(), any());
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Test
		@DisplayName("Should delegate getMetadataFromStage to RegistryService")
		@RegistryConfiguration
		@EPackageLuceneIndexSetup
		@WithFactoryConfiguration(factoryPid = "ScopeService", name = "test-scope", location = "?", properties = {
				@Property(key = "scope.name", value = SCOPE_NAME), @Property(key = "scope.parent", value = ""),
				@Property(key = "registryService.target", value = "(registry.name=" + REGISTRY_NAME + ")") })
		void shouldDelegateGetMetadata(
				@InjectService(cardinality = 0, filter = "(scope.name=" + SCOPE_NAME
				+ ")") ServiceAware<ScopeService> scopeAware)
						throws InterruptedException, InvocationTargetException {

			ScopeService<EObject> scopeService = scopeAware.waitForService(5000);
			assertNotNull(scopeService);

			ObjectMetadata expectedMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
			expectedMetadata.setObjectId(OBJECT_ID);
			when(mockRegistryService.getMetadataFromStage(SCOPE_NAME, STAGE_NAME, OBJECT_ID))
			.thenReturn(expectedMetadata);

			// Act
			ObjectMetadata result = scopeService.getMetadataFromStageForRegistry(REGISTRY_NAME, STAGE_NAME, OBJECT_ID);

			// Assert
			assertEquals(expectedMetadata, result);
			verify(mockRegistryService).getMetadataFromStage(SCOPE_NAME, STAGE_NAME, OBJECT_ID);
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Test
		@DisplayName("Should delegate transition to RegistryService")
		@RegistryConfiguration
		@EPackageLuceneIndexSetup
		@WithFactoryConfiguration(factoryPid = "ScopeService", name = "test-scope", location = "?", properties = {
				@Property(key = "scope.name", value = SCOPE_NAME), @Property(key = "scope.parent", value = ""),
				@Property(key = "registryService.target", value = "(registry.name=" + REGISTRY_NAME + ")") })
		void shouldDelegateTransition(
				@InjectService(cardinality = 0, filter = "(scope.name=" + SCOPE_NAME
				+ ")") ServiceAware<ScopeService> scopeAware)
						throws InterruptedException, InvocationTargetException {

			ScopeService<EObject> scopeService = scopeAware.waitForService(5000);
			assertNotNull(scopeService);

			String fromStage = "draft";
			String toStage = "approved";
			ObjectMetadata resultMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();

			when(mockRegistryService.transitionToStage(SCOPE_NAME, OBJECT_ID, fromStage, toStage))
			.thenReturn(resultMetadata);

			// Act
			ObjectMetadata result = scopeService.transitionToStageForRegistry(REGISTRY_NAME, OBJECT_ID, fromStage,
					toStage);

			// Assert
			assertEquals(resultMetadata, result);
			verify(mockRegistryService).transitionToStage(SCOPE_NAME, OBJECT_ID, fromStage, toStage);
		}
	}

	@Nested
	@DisplayName("Registry Validation Tests")
	class RegistryValidationTests {

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Test
		@DisplayName("Should validate existing registry")
		@RegistryConfiguration
		@EPackageLuceneIndexSetup
		@WithFactoryConfiguration(factoryPid = "ScopeService", name = "test-scope", location = "?", properties = {
				@Property(key = "scope.name", value = SCOPE_NAME), @Property(key = "scope.parent", value = ""),
				@Property(key = "registryService.target", value = "(registry.name=" + REGISTRY_NAME + ")") })
		void shouldValidateExistingRegistry(
				@InjectService(cardinality = 0, filter = "(scope.name=" + SCOPE_NAME
				+ ")") ServiceAware<ScopeService> scopeAware)
						throws InterruptedException, InvocationTargetException {

			ScopeService<EObject> scopeService = scopeAware.waitForService(5000);
			assertNotNull(scopeService);

			assertTrue(scopeService.isValidRegistry(REGISTRY_NAME));
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Test
		@DisplayName("Should return all registry names")
		@ScopeServiceSetup
		void shouldReturnAllRegistryNames(
				@InjectService(cardinality = 0, filter = "(scope.name=" + TestAnnotations.TEST_SCOPE_NAME
				+ ")") ServiceAware<ScopeService> scopeAware)
						throws InterruptedException, InvocationTargetException {

			ScopeService<EObject> scopeService = scopeAware.waitForService(5000);
			assertNotNull(scopeService);

			List<String> registries = scopeService.getAllRegistries();

			assertEquals(1, registries.size());
			assertTrue(registries.contains(TestAnnotations.SCHEMA_REGISTRY_NAME));
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Test
		@DisplayName("Should throw exception for invalid registry")
		@ScopeServiceSetup
		void shouldThrowExceptionForInvalidRegistry(
				@InjectService(cardinality = 0, filter = "(scope.name=" + TestAnnotations.TEST_SCOPE_NAME
				+ ")") ServiceAware<ScopeService> scopeAware)
						throws InterruptedException, InvocationTargetException {

			ScopeService<EObject> scopeService = scopeAware.waitForService(5000);
			assertNotNull(scopeService);

			EObject testObject = EcoreFactory.eINSTANCE.createEClass();
			ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();

			IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
					() -> scopeService.uploadToStageForRegistry("invalid-registry", STAGE_NAME, testObject, metadata));

			assertTrue(exception.getMessage().contains("not a valid registry"));
		}
	}

	@Nested
	@DisplayName("Parent Scope Delegation Tests")
	class ParentScopeDelegationTests {

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Test
		@DisplayName("Should fallback to parent scope when metadata not found")
		@ParentScopeServiceSetup
		void shouldFallbackToParentScope(
				@InjectService(cardinality = 0, filter = "(scope.name=" + TestAnnotations.TEST_SCOPE_NAME
						+ ")") ServiceAware<ScopeService> childScopeAware,
				@InjectService(cardinality = 0, filter = "(scope.name=" + TestAnnotations.TEST_PARENT_SCOPE_NAME
						+ ")") ServiceAware<ScopeService> parentScopeAware)
				throws InterruptedException, InvocationTargetException {

			ScopeService<EPackage> childScope = childScopeAware.waitForService(5000);
			ScopeService<EPackage> parentScope = parentScopeAware.waitForService(5000);
			assertNotNull(childScope);
			assertNotNull(parentScope);

			// Upload an EPackage to the parent's release (final) stage
			EPackage parentPackage = EcoreFactory.eINSTANCE.createEPackage();
			parentPackage.setName("parent-package");
			parentPackage.setNsURI("http://test/parent-package");
			parentPackage.setNsPrefix("pp");

			ObjectMetadata uploadMetadata = ManagementFactory.eINSTANCE.createObjectMetadata();
			uploadMetadata.setObjectId(OBJECT_ID);

			ObjectMetadata uploaded = parentScope.uploadToStageForRegistry(
					CommonTestAnnotations.SCHEMA_REGISTRY_NAME,
					CommonTestAnnotations.STAGE_RELEASE,
					parentPackage,
					uploadMetadata).getValue();
			assertNotNull(uploaded);

			// Child has nothing in its draft stage -> should fall back to parent's final stage
			ObjectMetadata result = childScope.getMetadataFromStageForRegistry(
					CommonTestAnnotations.SCHEMA_REGISTRY_NAME,
					CommonTestAnnotations.STAGE_DRAFT,
					OBJECT_ID);

			assertNotNull(result, "Should fall back to parent's metadata");
			assertEquals(OBJECT_ID, result.getObjectId());
			assertTrue(result.isIsReadOnly(), "Parent metadata should be marked as read-only");
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Test
		@DisplayName("Should include parent metadata in listInFinalStage")
		@ParentScopeServiceSetup
		void shouldIncludeParentMetadataInList(
				@InjectService(cardinality = 0, filter = "(scope.name=" + TestAnnotations.TEST_SCOPE_NAME
						+ ")") ServiceAware<ScopeService> childScopeAware,
				@InjectService(cardinality = 0, filter = "(scope.name=" + TestAnnotations.TEST_PARENT_SCOPE_NAME
						+ ")") ServiceAware<ScopeService> parentScopeAware)
				throws InterruptedException, InvocationTargetException {

			ScopeService<EPackage> childScope = childScopeAware.waitForService(5000);
			ScopeService<EPackage> parentScope = parentScopeAware.waitForService(5000);
			assertNotNull(childScope);
			assertNotNull(parentScope);

			// Upload an EPackage to the child's release (final) stage
			EPackage scopedPackage = EcoreFactory.eINSTANCE.createEPackage();
			scopedPackage.setName("scoped-package");
			scopedPackage.setNsURI("http://test/scoped-package");
			scopedPackage.setNsPrefix("sp");
			ObjectMetadata scopedMeta = ManagementFactory.eINSTANCE.createObjectMetadata();
			scopedMeta.setObjectId("scoped-object");
			childScope.uploadToStageForRegistry(
					CommonTestAnnotations.SCHEMA_REGISTRY_NAME,
					CommonTestAnnotations.STAGE_RELEASE,
					scopedPackage,
					scopedMeta).getValue();

			// Upload an EPackage to the parent's release (final) stage
			EPackage parentPackage = EcoreFactory.eINSTANCE.createEPackage();
			parentPackage.setName("parent-package");
			parentPackage.setNsURI("http://test/parent-package");
			parentPackage.setNsPrefix("pp");
			ObjectMetadata parentMeta = ManagementFactory.eINSTANCE.createObjectMetadata();
			parentMeta.setObjectId("parent-object");
			parentScope.uploadToStageForRegistry(
					CommonTestAnnotations.SCHEMA_REGISTRY_NAME,
					CommonTestAnnotations.STAGE_RELEASE,
					parentPackage,
					parentMeta).getValue();

			List<ObjectMetadata> result = childScope
					.listInFinalStageForRegistry(CommonTestAnnotations.SCHEMA_REGISTRY_NAME);

			assertEquals(2, result.size());
			assertTrue(result.stream().anyMatch(m -> "scoped-object".equals(m.getObjectId())));
			assertTrue(result.stream().anyMatch(m -> "parent-object".equals(m.getObjectId())));
		}

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should include parent metadata in listAllForRegistry")
        @ParentScopeServiceSetup
        void shouldIncludeParentMetadataInListAll(
                @InjectService(cardinality = 0, filter = "(scope.name=" + TestAnnotations.TEST_SCOPE_NAME
                        + ")") ServiceAware<ScopeService> childScopeAware,
                @InjectService(cardinality = 0, filter = "(scope.name=" + TestAnnotations.TEST_PARENT_SCOPE_NAME
                        + ")") ServiceAware<ScopeService> parentScopeAware)
                throws InterruptedException, InvocationTargetException {

            ScopeService<EPackage> childScope = childScopeAware.waitForService(5000);
            ScopeService<EPackage> parentScope = parentScopeAware.waitForService(5000);
            assertNotNull(childScope);
            assertNotNull(parentScope);

            // Upload an EPackage to the child's draft stage
            EPackage scopedPackage = EcoreFactory.eINSTANCE.createEPackage();
            scopedPackage.setName("scoped-package");
            scopedPackage.setNsURI("http://test/scoped-package");
            scopedPackage.setNsPrefix("sp");
            ObjectMetadata scopedMeta = ManagementFactory.eINSTANCE.createObjectMetadata();
            scopedMeta.setObjectId("scoped-object");
            childScope.uploadToStageForRegistry(
                    CommonTestAnnotations.SCHEMA_REGISTRY_NAME,
                    CommonTestAnnotations.STAGE_DRAFT,
                    scopedPackage,
                    scopedMeta).getValue();

            // Upload an EPackage to the parent's release stage
            EPackage parentPackage = EcoreFactory.eINSTANCE.createEPackage();
            parentPackage.setName("parent-package");
            parentPackage.setNsURI("http://test/parent-package");
            parentPackage.setNsPrefix("pp");
            ObjectMetadata parentMeta = ManagementFactory.eINSTANCE.createObjectMetadata();
            parentMeta.setObjectId("parent-object");
            parentScope.uploadToStageForRegistry(
                    CommonTestAnnotations.SCHEMA_REGISTRY_NAME,
                    CommonTestAnnotations.STAGE_RELEASE,
                    parentPackage,
                    parentMeta).getValue();

            List<ObjectMetadata> result = childScope.listAllForRegistry(CommonTestAnnotations.SCHEMA_REGISTRY_NAME);

            assertEquals(2, result.size());
            assertTrue(result.stream().anyMatch(m -> "scoped-object".equals(m.getObjectId())));
            assertTrue(result.stream().anyMatch(m -> "parent-object".equals(m.getObjectId())));
        }
    }

	@Nested
	@DisplayName("List and Delete Operations Tests")
	class ListAndDeleteOperationsTests {

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Test
		@DisplayName("Should delegate listInStage to RegistryService")
		@RegistryConfiguration
		@EPackageLuceneIndexSetup
		@WithFactoryConfiguration(factoryPid = "ScopeService", name = "test-scope", location = "?", properties = {
				@Property(key = "scope.name", value = SCOPE_NAME), @Property(key = "scope.parent", value = ""),
				@Property(key = "registryService.target", value = "(registry.name=" + REGISTRY_NAME + ")") })
		void shouldDelegateListInStage(
				@InjectService(cardinality = 0, filter = "(scope.name=" + SCOPE_NAME
				+ ")") ServiceAware<ScopeService> scopeAware)
						throws InterruptedException, InvocationTargetException {

			ScopeService<EObject> scopeService = scopeAware.waitForService(5000);
			assertNotNull(scopeService);

			ObjectMetadata metadata1 = ManagementFactory.eINSTANCE.createObjectMetadata();
			ObjectMetadata metadata2 = ManagementFactory.eINSTANCE.createObjectMetadata();
			List<ObjectMetadata> expectedList = Arrays.asList(metadata1, metadata2);

			when(mockRegistryService.listInStage(SCOPE_NAME, STAGE_NAME)).thenReturn(expectedList);

			// Act
			List<ObjectMetadata> result = scopeService.listInStageForRegistry(REGISTRY_NAME, STAGE_NAME);

			// Assert
			assertEquals(2, result.size());
			verify(mockRegistryService).listInStage(SCOPE_NAME, STAGE_NAME);
		}

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should delegate listAllForRegistry to RegistryService")
        @RegistryConfiguration
        @EPackageLuceneIndexSetup
        @WithFactoryConfiguration(factoryPid = "ScopeService", name = "test-scope", location = "?", properties = {
                @Property(key = "scope.name", value = SCOPE_NAME), @Property(key = "scope.parent", value = ""),
                @Property(key = "registryService.target", value = "(registry.name=" + REGISTRY_NAME + ")") })
        void shouldDelegateListAll(
                @InjectService(cardinality = 0, filter = "(scope.name=" + SCOPE_NAME
                        + ")") ServiceAware<ScopeService> scopeAware)
                throws InterruptedException, InvocationTargetException {

            ScopeService<EObject> scopeService = scopeAware.waitForService(5000);
            assertNotNull(scopeService);

            ObjectMetadata metadata1 = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata1.setObjectId("obj1");
            metadata1.setStage("draft");
            ObjectMetadata metadata2 = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata2.setObjectId("obj2");
            metadata2.setStage("approved");
            List<ObjectMetadata> expectedList = new ArrayList<>(Arrays.asList(metadata1, metadata2));

            when(mockRegistryService.listAll(SCOPE_NAME)).thenReturn(expectedList);

            // Act
            List<ObjectMetadata> result = scopeService.listAllForRegistry(REGISTRY_NAME);

            // Assert
            assertEquals(2, result.size());
            assertTrue(result.stream().anyMatch(m -> "obj1".equals(m.getObjectId())));
            assertTrue(result.stream().anyMatch(m -> "obj2".equals(m.getObjectId())));
            verify(mockRegistryService).listAll(SCOPE_NAME);
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should throw exception for invalid registry in listAllForRegistry")
        @RegistryConfiguration
        @EPackageLuceneIndexSetup
        @WithFactoryConfiguration(factoryPid = "ScopeService", name = "test-scope", location = "?", properties = {
                @Property(key = "scope.name", value = SCOPE_NAME), @Property(key = "scope.parent", value = ""),
                @Property(key = "registryService.target", value = "(registry.name=" + REGISTRY_NAME + ")") })
        void shouldThrowExceptionForInvalidRegistryInListAll(
                @InjectService(cardinality = 0, filter = "(scope.name=" + SCOPE_NAME
                        + ")") ServiceAware<ScopeService> scopeAware)
                throws InterruptedException, InvocationTargetException {

            ScopeService<EObject> scopeService = scopeAware.waitForService(5000);
            assertNotNull(scopeService);

            IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                    () -> scopeService.listAllForRegistry("invalid-registry"));

            assertTrue(exception.getMessage().contains("not a valid registry"));
        }

        @SuppressWarnings({ "unchecked", "rawtypes" })
        @Test
        @DisplayName("Should delegate delete to RegistryService")
        @RegistryConfiguration
        @EPackageLuceneIndexSetup
        @WithFactoryConfiguration(factoryPid = "ScopeService", name = "test-scope", location = "?", properties = {
                @Property(key = "scope.name", value = SCOPE_NAME), @Property(key = "scope.parent", value = ""),
                @Property(key = "registryService.target", value = "(registry.name=" + REGISTRY_NAME + ")") })
        void shouldDelegateDelete(
                @InjectService(cardinality = 0, filter = "(scope.name=" + SCOPE_NAME
                        + ")") ServiceAware<ScopeService> scopeAware)
                throws InterruptedException, InvocationTargetException {

			ScopeService<EObject> scopeService = scopeAware.waitForService(5000);
			assertNotNull(scopeService);

			Promise<Boolean> expectedPromise = promiseFactory.resolved(true);
			when(mockRegistryService.deleteFromStage(SCOPE_NAME, STAGE_NAME, OBJECT_ID)).thenReturn(expectedPromise);

			// Act
			Promise<Boolean> result = scopeService.deleteFromStageForRegistry(REGISTRY_NAME, STAGE_NAME, OBJECT_ID);

			// Assert
			assertNotNull(result);
			assertTrue(result.getValue());
			verify(mockRegistryService).deleteFromStage(SCOPE_NAME, STAGE_NAME, OBJECT_ID);
		}
	}
}
