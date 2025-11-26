/**
 * Copyright (c) 2012 - 2025 Data In Motion and others.
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
package org.eclipse.fennec.model.atlas.scope.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.fennec.model.atlas.model.scope.Scope;
import org.eclipse.fennec.model.atlas.scope.ScopeCollector;
import org.eclipse.fennec.model.atlas.scope.tests.annotations.ScopeCollectorTestAnnotation;
import org.eclipse.fennec.model.atlas.scope.tests.annotations.ScopeCollectorTestAnnotation.ChildWorkflowServiceConfiguration;
import org.eclipse.fennec.model.atlas.scope.tests.annotations.ScopeCollectorTestAnnotation.DraftStorageConfiguration;
import org.eclipse.fennec.model.atlas.scope.tests.annotations.ScopeCollectorTestAnnotation.ParentReleaseStorageConfiguration;
import org.eclipse.fennec.model.atlas.scope.tests.annotations.ScopeCollectorTestAnnotation.ParentWorkflowServiceConfiguration;
import org.eclipse.fennec.model.atlas.scope.tests.annotations.ScopeCollectorTestAnnotation.RegistryConfiguration;
import org.eclipse.fennec.model.atlas.scope.tests.annotations.ScopeCollectorTestAnnotation.ReleaseStorageConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;

/**
 * See documentation here:
 * 	https://github.com/osgi/osgi-test
 * 	https://github.com/osgi/osgi-test/wiki
 * Examples: https://github.com/osgi/osgi-test/tree/main/examples
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class ScopeCollectorTest {
	
	@TempDir
    Path tempDir;


	@BeforeEach
	public void before(@InjectBundleContext BundleContext ctx) {
		System.setProperty(ScopeCollectorTestAnnotation.PROP_TEMP_DIR, tempDir.toString());
	}
	
	@AfterEach
    void tearDown() {
        System.clearProperty(ScopeCollectorTestAnnotation.PROP_TEMP_DIR);
    }

	/**
	 * Test basic scope service tracking with single scope.
	 */
	@RegistryConfiguration
	@ParentReleaseStorageConfiguration
	@ParentWorkflowServiceConfiguration
	@Test
	public void testSingleScopeTracking(
			@InjectService(cardinality = 0) ServiceAware<ScopeCollector> serviceCollAware) throws InterruptedException {

	
		ScopeCollector serviceCollector = serviceCollAware.waitForService(2000);
		assertNotNull(serviceCollector);

		Thread.sleep(2000);

		List<Scope> scopes = serviceCollector.getScopes();
		assertEquals(1, scopes.size(), "scope list should have 1 element");

		Scope scope = scopes.get(0);
		assertEquals("my-parent-tenant", scope.getName());
		assertEquals("my-parent-tenant scope", scope.getDescription());
		assertEquals("atlas", scope.getParentScope());
		assertEquals(2, scope.getLinks().size());
		assertTrue(scope.getLinks().containsKey("self"));
		assertEquals("/scopes/my-parent-tenant", scope.getLinks().get("self"));
		assertTrue(scope.getLinks().containsKey("schemas"));
		assertEquals("/my-parent-tenant/schema", scope.getLinks().get("schemas"));
		assertEquals("release", scope.getFinalStage());
		assertEquals(1, scope.getStages().size());
		assertTrue(scope.getStages().contains("release"));
	}

	/**
	 * Test that the collector tracks multiple scope services.
	 */
	@RegistryConfiguration
	@DraftStorageConfiguration
	@ReleaseStorageConfiguration
	@ParentReleaseStorageConfiguration
	@ParentWorkflowServiceConfiguration
	@ChildWorkflowServiceConfiguration
	@Test
	public void testMultipleScopeTracking(
			@InjectService(cardinality = 0) ServiceAware<ScopeCollector> serviceCollAware) throws InterruptedException {

		ScopeCollector serviceCollector = serviceCollAware.waitForService(2000);
		assertNotNull(serviceCollector);

		Thread.sleep(2000);

		List<Scope> scopes = serviceCollector.getScopes();
		assertEquals(2, scopes.size(), "scope list should have 2 elements");

		// Verify all scopes are present
		assertTrue(scopes.stream().anyMatch(s -> "my-tenant".equals(s.getName())));
		assertTrue(scopes.stream().anyMatch(s -> "my-parent-tenant".equals(s.getName())));
	}

	/**
	 * Test getScopeByName retrieves the correct scope.
	 */
	@RegistryConfiguration
	@DraftStorageConfiguration
	@ReleaseStorageConfiguration
	@ParentReleaseStorageConfiguration
	@ParentWorkflowServiceConfiguration
	@ChildWorkflowServiceConfiguration
	@Test
	public void testGetScopeByName(
			@InjectService(cardinality = 0) ServiceAware<ScopeCollector> serviceCollAware) throws InterruptedException {

		ScopeCollector serviceCollector = serviceCollAware.waitForService(2000);
		assertNotNull(serviceCollector);

		Thread.sleep(2000);

		Scope scope = serviceCollector.getScopeByName("my-tenant");
		assertNotNull(scope, "Should find scope by name");
		assertEquals("my-tenant", scope.getName());
		assertEquals("my-tenant scope", scope.getDescription());
		assertEquals("my-parent-tenant", scope.getParentScope());

		Scope parentScope = serviceCollector.getScopeByName("my-parent-tenant");
		assertNotNull(parentScope, "Should find other scope by name");
		assertEquals("my-parent-tenant", parentScope.getName());
	}

	/**
	 * Test getScopeByName returns null for non-existent scope.
	 */
	@RegistryConfiguration
	@DraftStorageConfiguration
	@ReleaseStorageConfiguration
	@ParentReleaseStorageConfiguration
	@ParentWorkflowServiceConfiguration
	@ChildWorkflowServiceConfiguration
	@Test
	public void testGetScopeByNameNotFound(
			@InjectService(cardinality = 0) ServiceAware<ScopeCollector> serviceCollAware) throws InterruptedException {

		ScopeCollector serviceCollector = serviceCollAware.waitForService(2000);
		assertNotNull(serviceCollector);

		Thread.sleep(2000);

		Scope scope = serviceCollector.getScopeByName("non-existent-scope");
		assertEquals(null, scope, "Should return null for non-existent scope");
	}

	/**
	 * Test that getScopes returns an empty list when no scopes are registered.
	 */
	@Test
	public void testEmptyScopesList(
			@InjectService(cardinality = 0) ServiceAware<ScopeCollector> serviceCollAware) throws InterruptedException {

		ScopeCollector serviceCollector = serviceCollAware.waitForService(2000);
		assertNotNull(serviceCollector);

		Thread.sleep(500);

		List<Scope> scopes = serviceCollector.getScopes();
		assertNotNull(scopes, "Scopes list should not be null");
		assertEquals(0, scopes.size(), "Scopes list should be empty");
	}

	/**
	 * Test that scope with default parent scope value.
	 */
	@RegistryConfiguration
	@DraftStorageConfiguration
	@ReleaseStorageConfiguration
	@ParentReleaseStorageConfiguration
	@ParentWorkflowServiceConfiguration
	@ChildWorkflowServiceConfiguration
	@Test
	public void testScopeWithDefaultParent(
			@InjectService(cardinality = 0) ServiceAware<ScopeCollector> serviceCollAware) throws InterruptedException {

		ScopeCollector serviceCollector = serviceCollAware.waitForService(2000);
		assertNotNull(serviceCollector);

		Thread.sleep(2000);

		Scope scope = serviceCollector.getScopeByName("my-parent-tenant");
		assertNotNull(scope);
		assertEquals("my-parent-tenant", scope.getName());
		assertEquals("my-parent-tenant scope", scope.getDescription());
		assertEquals("atlas", scope.getParentScope());
	}



	/**
	 * Test that links are correctly generated for scopes.
	 */
	@RegistryConfiguration
	@DraftStorageConfiguration
	@ReleaseStorageConfiguration
	@ParentReleaseStorageConfiguration
	@ParentWorkflowServiceConfiguration
	@ChildWorkflowServiceConfiguration
	@Test
	public void testScopeLinksGeneration(
			@InjectService(cardinality = 0) ServiceAware<ScopeCollector> serviceCollAware) throws InterruptedException {

		ScopeCollector serviceCollector = serviceCollAware.waitForService(2000);
		assertNotNull(serviceCollector);

		Thread.sleep(2000);

		Scope scope = serviceCollector.getScopeByName("my-tenant");
		assertNotNull(scope);

		assertEquals(2, scope.getLinks().size(), "Should have exactly 2 links");

		// Verify self link
		assertTrue(scope.getLinks().containsKey("self"));
		assertEquals("/scopes/my-tenant", scope.getLinks().get("self"));

		// Verify schemas link
		assertTrue(scope.getLinks().containsKey("schemas"));
		assertEquals("/my-tenant/schema", scope.getLinks().get("schemas"));
	}

	/**
	 * Test dynamic scope removal when configuration is deleted.
	 */
	@RegistryConfiguration	
	@ParentReleaseStorageConfiguration
	@ParentWorkflowServiceConfiguration
	@Test
	public void testDynamicScopeRemoval(
			@InjectService(cardinality = 0) ServiceAware<ScopeCollector> serviceCollAware,
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(
					factoryPid = "EObjectWorkflowService",
					name = "parent-tenant-workflow",
					location = "?")) Configuration configuration) throws Exception {

		ScopeCollector serviceCollector = serviceCollAware.waitForService(2000);
		assertNotNull(serviceCollector);

		Thread.sleep(2000);

		// Verify scope is initially present
		Scope scope = serviceCollector.getScopeByName("my-parent-tenant");
		assertNotNull(scope, "Scope should be present initially");
		assertEquals(1, serviceCollector.getScopes().size(), "Should have 1 scope");

		// Delete the configuration to trigger unbind
		configuration.delete();
		Thread.sleep(2000);

		// Verify scope has been removed
		Scope removedScope = serviceCollector.getScopeByName("my-parent-tenant");
		assertNull(removedScope, "Scope should be removed after configuration deletion");
		assertEquals(0, serviceCollector.getScopes().size(), "Should have 0 scopes after removal");
	}

	/**
	 * Test dynamic scope re-registration after removal.
	 */
	@RegistryConfiguration	
	@ParentReleaseStorageConfiguration
	@Test
	public void testDynamicScopeReRegistration(
			@InjectService(cardinality = 0) ServiceAware<ScopeCollector> serviceCollAware,
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(
					factoryPid = "EObjectWorkflowService",
					name = "parent-tenant-workflow",
					location = "?")) Configuration configuration,
			@InjectService ServiceAware<ConfigurationAdmin> configAdminAware) throws Exception {

		ScopeCollector serviceCollector = serviceCollAware.waitForService(2000);
		assertNotNull(serviceCollector);

		// Initially no scopes
		assertEquals(0, serviceCollector.getScopes().size(), "Should start with 0 scopes");

		// Create a scope configuration
		Dictionary<String, Object> props = new Hashtable<>();
		props.put("scope", "my-parent-tenant");
		props.put("description", "Re-registration test scope");
		props.put("parent.scope", "atlas");
		configuration.update(props);

		Thread.sleep(2000);

		// Verify scope is registered
		Scope scope = serviceCollector.getScopeByName("my-parent-tenant");
		assertNotNull(scope, "Scope should be registered");
		assertEquals("my-parent-tenant", scope.getName());
		assertEquals(1, serviceCollector.getScopes().size());

		// Delete configuration
		configuration.delete();
		Thread.sleep(2000);

		// Verify removal
		assertNull(serviceCollector.getScopeByName("my-parent-tenant"), "Scope should be removed");
		assertEquals(0, serviceCollector.getScopes().size());

		// Re-register with updated properties
		ConfigurationAdmin configAdmin = configAdminAware.waitForService(1000);
		configuration = configAdmin.getFactoryConfiguration("EObjectWorkflowService", "rereg", "?");
		props.put("description", "Updated description after re-registration");
		configuration.update(props);
		Thread.sleep(2000);

		// Verify re-registration with updated properties
		Scope reregisteredScope = serviceCollector.getScopeByName("my-parent-tenant");
		assertNotNull(reregisteredScope, "Scope should be re-registered");
		assertEquals("my-parent-tenant", reregisteredScope.getName());
		assertEquals("Updated description after re-registration", reregisteredScope.getDescription());
		assertEquals(1, serviceCollector.getScopes().size());

		// Cleanup
		configuration.delete();
	}

	
	/**
	 * Test scope update when configuration properties are modified.
	 */
	@RegistryConfiguration	
	@ParentReleaseStorageConfiguration
	@Test
	public void testScopeConfigurationUpdate(
			@InjectService(cardinality = 0) ServiceAware<ScopeCollector> serviceCollAware,
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(
					factoryPid = "EObjectWorkflowService",
					name = "parent-tenant-workflow",
					location = "?")) Configuration configuration) throws Exception {

		ScopeCollector serviceCollector = serviceCollAware.waitForService(2000);
		assertNotNull(serviceCollector);

		// Create initial configuration
		Dictionary<String, Object> props = new Hashtable<>();
		props.put("scope", "my-parent-tenant");
		props.put("description", "Initial description");
		props.put("parent.scope", "atlas");
		configuration.update(props);

		Thread.sleep(2000);

		// Verify initial state
		Scope scope = serviceCollector.getScopeByName("my-parent-tenant");
		assertNotNull(scope);
		assertEquals("Initial description", scope.getDescription());
		assertEquals("atlas", scope.getParentScope());

		// Update configuration (this typically triggers unbind/bind cycle)
		props.put("description", "Updated description");
		props.put("parent.scope", "global");
		configuration.update(props);

		Thread.sleep(2000);

		// Verify updated state
		Scope updatedScope = serviceCollector.getScopeByName("my-parent-tenant");
		assertNotNull(updatedScope, "Scope should still exist after update");
		assertEquals("my-parent-tenant", updatedScope.getName());
		assertEquals("Updated description", updatedScope.getDescription());
		assertEquals("global", updatedScope.getParentScope());

		// Cleanup
		configuration.delete();
	}

}
