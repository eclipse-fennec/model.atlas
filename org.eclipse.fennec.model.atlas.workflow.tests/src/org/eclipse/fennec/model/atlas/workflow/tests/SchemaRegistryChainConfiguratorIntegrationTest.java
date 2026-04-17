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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Dictionary;

import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
import org.eclipse.fennec.model.atlas.workflow.tests.annotations.TestAnnotations;
import org.eclipse.fennec.model.atlas.workflow.tests.annotations.TestAnnotations.ParentScopeServiceSetup;
import org.eclipse.fennec.model.atlas.workflow.tests.annotations.TestAnnotations.ScopeServiceSetup;
import org.eclipse.fennec.model.atlas.workflow.tests.support.LuceneAwareTempDirExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * OSGi integration tests for
 * {@link org.eclipse.fennec.model.atlas.workflow.impl.SchemaRegistryChainConfigurator}.
 *
 * <p>
 * Verifies that binding a {@link ScopeService} with a schema registry creates
 * the expected {@code EPackageRegistry} and {@code ResourceSetFactory} factory
 * configurations, that stages are chained correctly, and that unbinding cleans
 * up the configurations.
 * </p>
 *
 * @author ilenia
 * @since Apr 17, 2026
 */
@RequireConfigurationAdmin
@ExtendWith(LuceneAwareTempDirExtension.class)
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@DisplayName("SchemaRegistryChainConfigurator OSGi Integration Tests")
@SuppressWarnings({ "rawtypes", "restriction" })
public class SchemaRegistryChainConfiguratorIntegrationTest {

	private static final String EPACKAGE_REGISTRY_FACTORY_PID = "EPackageRegistry";
	private static final String RESOURCE_SET_FACTORY_FACTORY_PID = "ResourceSetFactory";
	private static final String DEFAULT_REGISTRY_TARGET = "(default.resourceset.epackage.registry=true)";

	@InjectService
	ConfigurationAdmin configAdmin;

	@Nested
	@DisplayName("Single Scope Chain Generation Tests")
	class SingleScopeChainTests {

		@Test
		@DisplayName("Should create EPackageRegistry and ResourceSetFactory configs for each stage")
		@ParentScopeServiceSetup
		void shouldCreateConfigsForEachStage(
				@InjectService(cardinality = 0, filter = "(scope.name=" + TestAnnotations.TEST_PARENT_SCOPE_NAME + ")")
				ServiceAware<ScopeService> parentScopeAware) throws Exception {

			ScopeService<?> parentScope = parentScopeAware.waitForService(5000);
			assertNotNull(parentScope, "Parent ScopeService should be available");

			// 3 stages => 3 EPackageRegistry + 3 ResourceSetFactory per scope
			Configuration[] eprConfigs = configAdmin
					.listConfigurations("(service.factoryPid=" + EPACKAGE_REGISTRY_FACTORY_PID + ")");
			Configuration[] rsfConfigs = configAdmin
					.listConfigurations("(service.factoryPid=" + RESOURCE_SET_FACTORY_FACTORY_PID + ")");

			assertNotNull(eprConfigs, "EPackageRegistry configs should exist");
			assertNotNull(rsfConfigs, "ResourceSetFactory configs should exist");

			long eprCount = countConfigsForScope(eprConfigs, TestAnnotations.TEST_PARENT_SCOPE_NAME);
			long rsfCount = countConfigsForScope(rsfConfigs, TestAnnotations.TEST_PARENT_SCOPE_NAME);

			assertEquals(3, eprCount, "Should have one EPackageRegistry per stage");
			assertEquals(3, rsfCount, "Should have one ResourceSetFactory per stage");
		}

		@Test
		@DisplayName("Should chain stages so that the last stage points at default registry for atlas parent")
		@ParentScopeServiceSetup
		void shouldChainLastStageToDefaultRegistry(
				@InjectService(cardinality = 0, filter = "(scope.name=" + TestAnnotations.TEST_PARENT_SCOPE_NAME + ")")
				ServiceAware<ScopeService> parentScopeAware) throws Exception {

			assertNotNull(parentScopeAware.waitForService(5000));

			// The final stage (release) should point at the default registry
			Configuration rsfRelease = findConfiguration(RESOURCE_SET_FACTORY_FACTORY_PID,
					TestAnnotations.TEST_PARENT_SCOPE_NAME + "-" + CommonTestAnnotations.STAGE_RELEASE);
			assertNotNull(rsfRelease, "ResourceSetFactory for release stage should exist");

			String target = (String) rsfRelease.getProperties().get("ePackageRegistry.target");
			assertEquals(DEFAULT_REGISTRY_TARGET, target,
					"Final stage should point at default EPackage registry when parent is atlas");
		}

		@Test
		@DisplayName("Should chain intermediate stages to the next stage")
		@ParentScopeServiceSetup
		void shouldChainIntermediateStagesToNextStage(
				@InjectService(cardinality = 0, filter = "(scope.name=" + TestAnnotations.TEST_PARENT_SCOPE_NAME + ")")
				ServiceAware<ScopeService> parentScopeAware) throws Exception {

			assertNotNull(parentScopeAware.waitForService(5000));

			// Draft should point at approved
			Configuration rsfDraft = findConfiguration(RESOURCE_SET_FACTORY_FACTORY_PID,
					TestAnnotations.TEST_PARENT_SCOPE_NAME + "-" + CommonTestAnnotations.STAGE_DRAFT);
			assertNotNull(rsfDraft, "ResourceSetFactory for draft stage should exist");

			String draftTarget = (String) rsfDraft.getProperties().get("ePackageRegistry.target");
			assertEquals(
					"(rsf.name=" + TestAnnotations.TEST_PARENT_SCOPE_NAME + "_" + CommonTestAnnotations.STAGE_APPROVED
							+ ")",
					draftTarget, "Draft stage should point at approved stage");

			// Approved should point at release
			Configuration rsfApproved = findConfiguration(RESOURCE_SET_FACTORY_FACTORY_PID,
					TestAnnotations.TEST_PARENT_SCOPE_NAME + "-" + CommonTestAnnotations.STAGE_APPROVED);
			assertNotNull(rsfApproved, "ResourceSetFactory for approved stage should exist");

			String approvedTarget = (String) rsfApproved.getProperties().get("ePackageRegistry.target");
			assertEquals(
					"(rsf.name=" + TestAnnotations.TEST_PARENT_SCOPE_NAME + "_" + CommonTestAnnotations.STAGE_RELEASE
							+ ")",
					approvedTarget, "Approved stage should point at release stage");
		}

		@Test
		@DisplayName("Should set correct EPackageRegistry properties")
		@ParentScopeServiceSetup
		void shouldSetCorrectEPackageRegistryProperties(
				@InjectService(cardinality = 0, filter = "(scope.name=" + TestAnnotations.TEST_PARENT_SCOPE_NAME + ")")
				ServiceAware<ScopeService> parentScopeAware) throws Exception {

			assertNotNull(parentScopeAware.waitForService(5000));

			Configuration eprDraft = findConfiguration(EPACKAGE_REGISTRY_FACTORY_PID,
					TestAnnotations.TEST_PARENT_SCOPE_NAME + "-" + CommonTestAnnotations.STAGE_DRAFT);
			assertNotNull(eprDraft, "EPackageRegistry for draft stage should exist");

			Dictionary<String, Object> props = eprDraft.getProperties();
			assertEquals(
					TestAnnotations.TEST_PARENT_SCOPE_NAME + "_" + CommonTestAnnotations.STAGE_DRAFT,
					props.get("rsf.name"),
					"rsf.name should be scopeName_stageName");

			String configuratorTarget = (String) props.get("ePackageConfigurator.target");
			assertNotNull(configuratorTarget);
			assertTrue(
					configuratorTarget.contains("emf.model.scope=" + TestAnnotations.TEST_PARENT_SCOPE_NAME),
					"ePackageConfigurator.target should filter on scope");
			assertTrue(
					configuratorTarget.contains(WorkflowConstants.ATLAS_EPACKAGE_REGISTRATION_STAGE_PROPERTY + "="
							+ CommonTestAnnotations.STAGE_DRAFT),
					"ePackageConfigurator.target should filter on stage");
		}
	}

	@Nested
	@DisplayName("Parent-Child Scope Chain Tests")
	class ParentChildChainTests {

		@Test
		@DisplayName("Should chain child's final stage to parent scope's final stage")
		@ParentScopeServiceSetup
		void shouldChainChildFinalStageToParentFinalStage(
				@InjectService(cardinality = 0, filter = "(scope.name=" + TestAnnotations.TEST_SCOPE_NAME + ")")
				ServiceAware<ScopeService> childScopeAware,
				@InjectService(cardinality = 0, filter = "(scope.name=" + TestAnnotations.TEST_PARENT_SCOPE_NAME + ")")
				ServiceAware<ScopeService> parentScopeAware) throws Exception {

			assertNotNull(parentScopeAware.waitForService(5000), "Parent ScopeService should be available");
			assertNotNull(childScopeAware.waitForService(5000), "Child ScopeService should be available");
			
			Thread.sleep(2000);

			// Child's final stage (release) should point at parent's final stage (release)
			Configuration rsfRelease = findConfiguration(RESOURCE_SET_FACTORY_FACTORY_PID,
					TestAnnotations.TEST_SCOPE_NAME + "-" + CommonTestAnnotations.STAGE_RELEASE);
			assertNotNull(rsfRelease, "ResourceSetFactory for child's release stage should exist");

			String target = (String) rsfRelease.getProperties().get("ePackageRegistry.target");
			assertEquals(
					"(rsf.name=" + TestAnnotations.TEST_PARENT_SCOPE_NAME + "_" + CommonTestAnnotations.STAGE_RELEASE
							+ ")",
					target, "Child's final stage should point at parent's final stage");
		}

		@Test
		@DisplayName("Should create configs for both parent and child scopes")
		@ParentScopeServiceSetup
		void shouldCreateConfigsForBothScopes(
				@InjectService(cardinality = 0, filter = "(scope.name=" + TestAnnotations.TEST_SCOPE_NAME + ")")
				ServiceAware<ScopeService> childScopeAware,
				@InjectService(cardinality = 0, filter = "(scope.name=" + TestAnnotations.TEST_PARENT_SCOPE_NAME + ")")
				ServiceAware<ScopeService> parentScopeAware) throws Exception {

			assertNotNull(parentScopeAware.waitForService(5000));
			assertNotNull(childScopeAware.waitForService(5000));
			
			Thread.sleep(2000);

			Configuration[] eprConfigs = configAdmin
					.listConfigurations("(service.factoryPid=" + EPACKAGE_REGISTRY_FACTORY_PID + ")");
			assertNotNull(eprConfigs, "EPackageRegistry configs should exist");

			long parentCount = countConfigsForScope(eprConfigs, TestAnnotations.TEST_PARENT_SCOPE_NAME);
			long childCount = countConfigsForScope(eprConfigs, TestAnnotations.TEST_SCOPE_NAME);

			assertEquals(3, parentCount, "Parent scope should have 3 EPackageRegistry configs");
			assertEquals(3, childCount, "Child scope should have 3 EPackageRegistry configs");
		}

		@Test
		@DisplayName("Should chain child's intermediate stages correctly")
		@ParentScopeServiceSetup
		void shouldChainChildIntermediateStages(
				@InjectService(cardinality = 0, filter = "(scope.name=" + TestAnnotations.TEST_SCOPE_NAME + ")")
				ServiceAware<ScopeService> childScopeAware,
				@InjectService(cardinality = 0, filter = "(scope.name=" + TestAnnotations.TEST_PARENT_SCOPE_NAME + ")")
				ServiceAware<ScopeService> parentScopeAware) throws Exception {

			assertNotNull(parentScopeAware.waitForService(5000));
			assertNotNull(childScopeAware.waitForService(5000));
			
			Thread.sleep(2000);

			// Child's draft should point at child's approved
			Configuration rsfDraft = findConfiguration(RESOURCE_SET_FACTORY_FACTORY_PID,
					TestAnnotations.TEST_SCOPE_NAME + "-" + CommonTestAnnotations.STAGE_DRAFT);
			assertNotNull(rsfDraft, "ResourceSetFactory for child's draft stage should exist");

			String draftTarget = (String) rsfDraft.getProperties().get("ePackageRegistry.target");
			assertEquals(
					"(rsf.name=" + TestAnnotations.TEST_SCOPE_NAME + "_" + CommonTestAnnotations.STAGE_APPROVED + ")",
					draftTarget, "Child's draft should point at child's approved stage");

			// Child's approved should point at child's release
			Configuration rsfApproved = findConfiguration(RESOURCE_SET_FACTORY_FACTORY_PID,
					TestAnnotations.TEST_SCOPE_NAME + "-" + CommonTestAnnotations.STAGE_APPROVED);
			assertNotNull(rsfApproved, "ResourceSetFactory for child's approved stage should exist");

			String approvedTarget = (String) rsfApproved.getProperties().get("ePackageRegistry.target");
			assertEquals(
					"(rsf.name=" + TestAnnotations.TEST_SCOPE_NAME + "_" + CommonTestAnnotations.STAGE_RELEASE + ")",
					approvedTarget, "Child's approved should point at child's release stage");
		}
	}

	@Nested
	@DisplayName("Deferred Generation Tests")
	class DeferredGenerationTests {

		@Test
		@DisplayName("Should defer child generation when parent is not yet bound")
		@ScopeServiceSetup
		void shouldDeferWhenParentNotBound(
				@InjectService(cardinality = 0, filter = "(scope.name=" + TestAnnotations.TEST_SCOPE_NAME + ")")
				ServiceAware<ScopeService> childScopeAware) throws Exception {

			// The child scope has parent=test-parent-scope, but @ScopeServiceSetup does
			// NOT create the parent ScopeService. The configurator should defer chain
			// generation because the parent is not bound yet.
			// Note: the ScopeService itself will be available (it only needs a RegistryService),
			// but SchemaRegistryChainConfigurator won't find the parent in its scopesByName map.
			assertNotNull(childScopeAware.waitForService(5000), "Child ScopeService should be available");

			Configuration rsfDraft = findConfiguration(RESOURCE_SET_FACTORY_FACTORY_PID,
					TestAnnotations.TEST_SCOPE_NAME + "-" + CommonTestAnnotations.STAGE_DRAFT);
			assertNull(rsfDraft,
					"No chain configs should be created when parent scope is not bound");
		}
	}

	@Nested
	@DisplayName("Edge Case Tests")
	class EdgeCaseTests {

		@Test
		@DisplayName("Atlas scope should not generate chain configs")
		@ParentScopeServiceSetup
		void shouldNotGenerateConfigsForAtlasScope(
				@InjectService(cardinality = 0, filter = "(scope.name=" + TestAnnotations.TEST_PARENT_SCOPE_NAME + ")")
				ServiceAware<ScopeService> parentScopeAware) throws Exception {

			assertNotNull(parentScopeAware.waitForService(5000));

			// The AtlasScopeService is always present but should be skipped
			Configuration config = findConfiguration(EPACKAGE_REGISTRY_FACTORY_PID,
					WorkflowConstants.ATLAS_SCOPE_NAME + "-" + WorkflowConstants.ATLAS_SCHEMA_REGISTRY_STAGE_NAME);
			assertNull(config, "No chain configs should be created for the atlas scope");
		}

		@Test
		@DisplayName("Parent scope with atlas parent should use default registry target")
		@ParentScopeServiceSetup
		void shouldUseDefaultRegistryForAtlasParent(
				@InjectService(cardinality = 0, filter = "(scope.name=" + TestAnnotations.TEST_PARENT_SCOPE_NAME + ")")
				ServiceAware<ScopeService> parentScopeAware) throws Exception {

			assertNotNull(parentScopeAware.waitForService(5000));

			// Parent scope's default parent is "atlas", so its final stage should
			// point at the default EPackage registry
			Configuration eprRelease = findConfiguration(EPACKAGE_REGISTRY_FACTORY_PID,
					TestAnnotations.TEST_PARENT_SCOPE_NAME + "-" + CommonTestAnnotations.STAGE_RELEASE);
			assertNotNull(eprRelease, "EPackageRegistry for parent's release stage should exist");

			String parentTarget = (String) eprRelease.getProperties().get("parentRegistry.target");
			assertEquals(DEFAULT_REGISTRY_TARGET, parentTarget,
					"Parent scope with atlas parent should point at default registry");
		}
	}

	// ---- Helper methods ----

	private Configuration findConfiguration(String factoryPid, String name) throws Exception {
		String filter = "(&(service.factoryPid=" + factoryPid + ")(service.pid=" + factoryPid + "~" + name + "))";
		Configuration[] configs = configAdmin.listConfigurations(filter);
		return configs != null && configs.length > 0 ? configs[0] : null;
	}

	private long countConfigsForScope(Configuration[] configs, String scopeName) {
		long count = 0;
		for (Configuration config : configs) {
			if (config.getPid().contains(scopeName)) {
				count++;
			}
		}
		return count;
	}
}
