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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Registry;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Scope;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
import org.eclipse.fennec.model.atlas.workflow.tests.annotations.TestAnnotations.EPackageLuceneIndexSetup;
import org.eclipse.fennec.model.atlas.workflow.tests.annotations.TestAnnotations.RegistryConfiguration;
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
 * OSGi integration tests for {@link org.eclipse.fennec.model.atlas.workflow.impl.AtlasScopeService}.
 *
 * Verifies that the AtlasScopeService is properly registered in the OSGi service registry
 * and behaves correctly as the parent scope for all other scopes.
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
@DisplayName("AtlasScopeService OSGi Integration Tests")
@SuppressWarnings({ "unchecked", "rawtypes", "restriction" })
public class AtlasScopeServiceIntegrationTest {


	@Nested
	@DisplayName("Service Registration Tests")
	class ServiceRegistrationTests {

		@Test
		@DisplayName("Should be registered as ScopeService with scope.name=atlas")
		@RegistryConfiguration
		@EPackageLuceneIndexSetup
		void shouldBeRegisteredWithAtlasScopeName(
				@InjectService(cardinality = 0, filter = "(scope.name=" + WorkflowConstants.ATLAS_SCOPE_NAME + ")")
				ServiceAware<ScopeService> scopeAware)
				throws InterruptedException {

			
			ScopeService<EPackage> scopeService = scopeAware.waitForService(5000);
			assertNotNull(scopeService, "AtlasScopeService should be registered with scope.name=atlas");
		}
	}

	@Nested
	@DisplayName("Scope Object Tests")
	class ScopeObjectTests {

		@Test
		@DisplayName("Should return scope with name atlas")
		@RegistryConfiguration
		@EPackageLuceneIndexSetup
		void shouldReturnScopeWithNameAtlas(
				@InjectService(cardinality = 0, filter = "(scope.name=" + WorkflowConstants.ATLAS_SCOPE_NAME + ")")
				ServiceAware<ScopeService> scopeAware)
				throws InterruptedException {

			
			ScopeService<EPackage> scopeService = scopeAware.waitForService(5000);
			assertNotNull(scopeService);

			Scope scope = scopeService.getScope();
			assertNotNull(scope);
			assertEquals(WorkflowConstants.ATLAS_SCOPE_NAME, scope.getName());
		}

		@Test
		@DisplayName("Should have no parent scope")
		@RegistryConfiguration
		@EPackageLuceneIndexSetup
		void shouldHaveNoParentScope(
				@InjectService(cardinality = 0, filter = "(scope.name=" + WorkflowConstants.ATLAS_SCOPE_NAME + ")")
				ServiceAware<ScopeService> scopeAware)
				throws InterruptedException {

			
			ScopeService<EPackage> scopeService = scopeAware.waitForService(5000);
			assertNotNull(scopeService);

			Scope scope = scopeService.getScope();
			assertNull(scope.getParentScope(), "Atlas scope should have no parent");
		}

		@Test
		@DisplayName("Should have atlas-schema-registry as only registry")
		@RegistryConfiguration
		@EPackageLuceneIndexSetup
		void shouldHaveAtlasSchemaRegistryAsOnlyRegistry(
				@InjectService(cardinality = 0, filter = "(scope.name=" + WorkflowConstants.ATLAS_SCOPE_NAME + ")")
				ServiceAware<ScopeService> scopeAware)
				throws InterruptedException {

			
			ScopeService<EPackage> scopeService = scopeAware.waitForService(5000);
			assertNotNull(scopeService);

			Scope scope = scopeService.getScope();
			assertEquals(1, scope.getRegistries().size());
			Registry registry = scope.getRegistries().get(0);
			assertEquals(WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME, registry.getName());
			assertTrue(registry.isSchemaRegistry());
		}
	}

	@Nested
	@DisplayName("Registry Validation Tests")
	class RegistryValidationTests {

		@Test
		@DisplayName("Should only accept atlas-schema-registry as valid")
		@RegistryConfiguration
		@EPackageLuceneIndexSetup
		void shouldOnlyAcceptAtlasSchemaRegistry(
				@InjectService(cardinality = 0, filter = "(scope.name=" + WorkflowConstants.ATLAS_SCOPE_NAME + ")")
				ServiceAware<ScopeService> scopeAware)
				throws InterruptedException {

			
			ScopeService<EPackage> scopeService = scopeAware.waitForService(5000);
			assertNotNull(scopeService);

			assertTrue(scopeService.isValidRegistry(WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME));
			assertFalse(scopeService.isValidRegistry("other-registry"));
		}

		@Test
		@DisplayName("Should return only atlas-schema-registry in getAllRegistries")
		@RegistryConfiguration
		@EPackageLuceneIndexSetup
		void shouldReturnOnlyAtlasSchemaRegistryInList(
				@InjectService(cardinality = 0, filter = "(scope.name=" + WorkflowConstants.ATLAS_SCOPE_NAME + ")")
				ServiceAware<ScopeService> scopeAware)
				throws InterruptedException {

			
			ScopeService<EPackage> scopeService = scopeAware.waitForService(5000);
			assertNotNull(scopeService);

			List<String> registries = scopeService.getAllRegistries();
			assertEquals(1, registries.size());
			assertEquals(WorkflowConstants.ATLAS_SCHEMA_REGISTRY_NAME, registries.get(0));
		}

		@Test
		@DisplayName("Should throw IllegalArgumentException for invalid registry operations")
		@RegistryConfiguration
		@EPackageLuceneIndexSetup
		void shouldThrowForInvalidRegistryOperations(
				@InjectService(cardinality = 0, filter = "(scope.name=" + WorkflowConstants.ATLAS_SCOPE_NAME + ")")
				ServiceAware<ScopeService> scopeAware)
				throws InterruptedException {

			
			ScopeService<EPackage> scopeService = scopeAware.waitForService(5000);
			assertNotNull(scopeService);

			assertThrows(IllegalArgumentException.class,
					() -> scopeService.getMetadataFromStageForRegistry("invalid-registry", "released", "id"));
			assertThrows(IllegalArgumentException.class,
					() -> scopeService.listInFinalStageForRegistry("invalid-registry"));
		}
	}
}
