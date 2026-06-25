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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.scope.api.RegistryType;
import org.eclipse.fennec.model.atlas.scope.api.StageInfo;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations.EPackageLuceneIndexSetup;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations.RegistryConfiguration;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Registry;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
import org.eclipse.fennec.model.atlas.workflow.tests.support.LuceneAwareTempDirExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * OSGi integration tests for {@link org.eclipse.fennec.model.atlas.workflow.impl.AtlasSchemaRegistryService}.
 *
 * Verifies that the AtlasSchemaRegistryService is properly registered in the OSGi service registry
 * and behaves correctly as a read-only registry for system EPackages.
 *
 * @author ilenia
 * @since Mar 30, 2026
 */
@RequireEMF
@RequireConfigurationAdmin
@ExtendWith(LuceneAwareTempDirExtension.class)
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@DisplayName("AtlasSchemaRegistryService OSGi Integration Tests")
@SuppressWarnings({ "unchecked", "rawtypes", "restriction" })
public class AtlasSchemaRegistryServiceIntegrationTest {

	@Nested
	@DisplayName("Service Registration Tests")
	class ServiceRegistrationTests {

		@Test
		@DisplayName("Should be registered as RegistryService with registry.name=atlas-schema-registry")
		@RegistryConfiguration
		@EPackageLuceneIndexSetup
		void shouldBeRegisteredWithAtlasSchemaRegistryName(
				@InjectService(cardinality = 0, filter = "(registry.name=" + WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME + ")")
				ServiceAware<RegistryService> registryAware)
						throws InterruptedException {

			RegistryService<EPackage> registryService = registryAware.waitForService(5000);
			assertNotNull(registryService, "AtlasSchemaRegistryService should be registered with registry.name=atlas-schema-registry");
		}

		@Test
		@DisplayName("Should be registered with registry.type=SCHEMA")
		@RegistryConfiguration
		@EPackageLuceneIndexSetup
		void shouldBeRegisteredAsSchemaRegistry(
				@InjectService(cardinality = 0, filter = "(&(registry.name=" + WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME + ")(registry.type=SCHEMA))")
				ServiceAware<RegistryService> registryAware)
						throws InterruptedException {

			RegistryService<EPackage> registryService = registryAware.waitForService(5000);
			assertNotNull(registryService, "AtlasSchemaRegistryService should be registered with registry.type=SCHEMA");
		}
	}

	@Nested
	@DisplayName("Registry Object Tests")
	class RegistryObjectTests {

		@Test
		@DisplayName("Should return registry with correct name and description")
		@RegistryConfiguration
		@EPackageLuceneIndexSetup
		void shouldReturnRegistryWithCorrectNameAndDescription(
				@InjectService(cardinality = 0, filter = "(registry.name=" + WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME + ")")
				ServiceAware<RegistryService> registryAware)
						throws InterruptedException {

			RegistryService<EPackage> registryService = registryAware.waitForService(5000);
			assertNotNull(registryService);

			Registry registry = registryService.getRegistry();
			assertNotNull(registry);
			assertEquals(WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME, registry.getName());
			assertEquals(RegistryType.SCHEMA, registry.getType());
		}

		@Test
		@DisplayName("Should have single non-writable final stage named released")
		@RegistryConfiguration
		@EPackageLuceneIndexSetup
		void shouldHaveSingleReleasedStage(
				@InjectService(cardinality = 0, filter = "(registry.name=" + WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME + ")")
				ServiceAware<RegistryService> registryAware)
						throws InterruptedException {


			RegistryService<EPackage> registryService = registryAware.waitForService(5000);
			assertNotNull(registryService);

			Registry registry = registryService.getRegistry();
			assertEquals(1, registry.getStages().size());

			StageInfo stage = registry.getStages().get(0);
			assertEquals(WorkflowConstants.ATLAS_SCHEMA_REGISTRY_STAGE_NAME, stage.getName());
			assertFalse(stage.isWritable());
			assertTrue(stage.isFinal());
		}
	}

	@Nested
	@DisplayName("Read-Only Behavior Tests")
	class ReadOnlyBehaviorTests {

		@Test
		@DisplayName("Should throw UnsupportedOperationException on upload")
		@RegistryConfiguration
		@EPackageLuceneIndexSetup
		void shouldThrowOnUpload(
				@InjectService(cardinality = 0, filter = "(registry.name=" + WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME + ")")
				ServiceAware<RegistryService> registryAware)
						throws InterruptedException {


			RegistryService<EPackage> registryService = registryAware.waitForService(5000);
			assertNotNull(registryService);

			EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
			ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();

			assertThrows(UnsupportedOperationException.class,
					() -> registryService.uploadToStage("atlas", "released", ePackage, metadata));
		}

		@Test
		@DisplayName("Should throw UnsupportedOperationException on update")
		@RegistryConfiguration
		@EPackageLuceneIndexSetup
		void shouldThrowOnUpdate(
				@InjectService(cardinality = 0, filter = "(registry.name=" + WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME + ")")
				ServiceAware<RegistryService> registryAware)
						throws InterruptedException {


			RegistryService<EPackage> registryService = registryAware.waitForService(5000);
			assertNotNull(registryService);

			EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();

			assertThrows(UnsupportedOperationException.class,
					() -> registryService.updateInStage("atlas", "released", ePackage, "id", "1.0"));
		}

		@Test
		@DisplayName("Should throw UnsupportedOperationException on delete")
		@RegistryConfiguration
		@EPackageLuceneIndexSetup
		void shouldThrowOnDelete(
				@InjectService(cardinality = 0, filter = "(registry.name=" + WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME + ")")
				ServiceAware<RegistryService> registryAware)
						throws InterruptedException {


			RegistryService<EPackage> registryService = registryAware.waitForService(5000);
			assertNotNull(registryService);

			assertThrows(UnsupportedOperationException.class,
					() -> registryService.deleteFromStage("atlas", "released", "id"));
		}

		@Test
		@DisplayName("Should throw UnsupportedOperationException on transition")
		@RegistryConfiguration
		@EPackageLuceneIndexSetup
		void shouldThrowOnTransition(
				@InjectService(cardinality = 0, filter = "(registry.name=" + WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME + ")")
				ServiceAware<RegistryService> registryAware)
						throws InterruptedException {


			RegistryService<EPackage> registryService = registryAware.waitForService(5000);
			assertNotNull(registryService);

			assertThrows(UnsupportedOperationException.class,
					() -> registryService.transitionToStage("atlas", "id", "released", "other"));
		}

		@Test
		@DisplayName("Should report all stages as not writable")
		@RegistryConfiguration
		@EPackageLuceneIndexSetup
		void shouldReportAllStagesNotWritable(
				@InjectService(cardinality = 0, filter = "(registry.name=" + WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME + ")")
				ServiceAware<RegistryService> registryAware)
						throws InterruptedException {


			RegistryService<EPackage> registryService = registryAware.waitForService(5000);
			assertNotNull(registryService);

			assertFalse(registryService.isWritableStage("released"));
			assertFalse(registryService.isFinalStageWritable());
		}

		@Test
		@DisplayName("Should not allow any transitions")
		@RegistryConfiguration
		@EPackageLuceneIndexSetup
		void shouldNotAllowAnyTransitions(
				@InjectService(cardinality = 0, filter = "(registry.name=" + WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME + ")")
				ServiceAware<RegistryService> registryAware)
						throws InterruptedException {


			RegistryService<EPackage> registryService = registryAware.waitForService(5000);
			assertNotNull(registryService);

			assertFalse(registryService.isTransitionAllowed("released", "draft"));
			assertFalse(registryService.isTransitionAllowed("draft", "released"));
		}
	}

	@Nested
	@DisplayName("Stage Validation Tests")
	class StageValidationTests {

		@Test
		@DisplayName("Should only validate released as valid stage")
		@RegistryConfiguration
		@EPackageLuceneIndexSetup
		void shouldOnlyValidateReleasedAsValidStage(
				@InjectService(cardinality = 0, filter = "(registry.name=" + WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME + ")")
				ServiceAware<RegistryService> registryAware)
						throws InterruptedException {


			RegistryService<EPackage> registryService = registryAware.waitForService(5000);
			assertNotNull(registryService);

			assertTrue(registryService.isValidStage("released"));
			assertFalse(registryService.isValidStage("draft"));
			assertFalse(registryService.isValidStage("approved"));
		}

		@Test
		@DisplayName("Should throw on operations with invalid stage")
		@RegistryConfiguration
		@EPackageLuceneIndexSetup
		void shouldThrowOnOperationsWithInvalidStage(
				@InjectService(cardinality = 0, filter = "(registry.name=" + WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME + ")")
				ServiceAware<RegistryService> registryAware)
						throws InterruptedException {


			RegistryService<EPackage> registryService = registryAware.waitForService(5000);
			assertNotNull(registryService);

			assertThrows(IllegalArgumentException.class,
					() -> registryService.getMetadataFromStage("atlas", "draft", "id"));
			assertThrows(IllegalArgumentException.class,
					() -> registryService.getContentFromStage("atlas", "draft", "id"));
			assertThrows(IllegalArgumentException.class,
					() -> registryService.listInStage("atlas", "draft"));
		}
	}

	@Nested
	@DisplayName("EClass Compatibility Tests")
	class EClassCompatibilityTests {

		
		@Test
		@DisplayName("Should be compatible with EPackage EClass")
		@RegistryConfiguration
		@EPackageLuceneIndexSetup
		void shouldBeCompatibleWithEPackage(
				@InjectService(cardinality = 0, filter = "(registry.name=" + WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME + ")")
				ServiceAware<RegistryService> registryAware)
						throws InterruptedException {

			RegistryService<EObject> registryService =  registryAware.waitForService(5000);
			assertNotNull(registryService);

			assertTrue(registryService.isEClassCompatibleWithRegistry(EcorePackage.Literals.EPACKAGE));
		}

		@Test
		@DisplayName("Should not be compatible with non-EPackage EClass")
		@RegistryConfiguration
		@EPackageLuceneIndexSetup
		void shouldNotBeCompatibleWithNonEPackage(
				@InjectService(cardinality = 0, filter = "(registry.name=" + WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME + ")")
				ServiceAware<RegistryService> registryAware)
						throws InterruptedException {


			RegistryService<EPackage> registryService = registryAware.waitForService(5000);
			assertNotNull(registryService);

			assertFalse(registryService.isEClassCompatibleWithRegistry(EcorePackage.Literals.ECLASS));
			assertFalse(registryService.isEClassCompatibleWithRegistry(EcorePackage.Literals.EATTRIBUTE));
		}

		@Test
		@DisplayName("Should return EPackage as root EClass")
		@RegistryConfiguration
		@EPackageLuceneIndexSetup
		void shouldReturnEPackageAsRootEClass(
				@InjectService(cardinality = 0, filter = "(registry.name=" + WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME + ")")
				ServiceAware<RegistryService> registryAware)
						throws InterruptedException {


			RegistryService<EPackage> registryService = registryAware.waitForService(5000);
			assertNotNull(registryService);

			assertEquals(EcorePackage.Literals.EPACKAGE, registryService.getRootEClass());
		}
	}

	@Nested
	@DisplayName("Registry Name Tests")
	class RegistryNameTests {

		@Test
		@DisplayName("Should return correct registry name")
		@RegistryConfiguration
		@EPackageLuceneIndexSetup
		void shouldReturnCorrectRegistryName(
				@InjectService(cardinality = 0, filter = "(registry.name=" + WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME + ")")
				ServiceAware<RegistryService> registryAware)
						throws InterruptedException {


			RegistryService<EPackage> registryService = registryAware.waitForService(5000);
			assertNotNull(registryService);

			assertEquals(WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME, registryService.getRegistryName());
		}
	}
}
