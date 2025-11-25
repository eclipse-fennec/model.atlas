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

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.fennec.model.atlas.model.scope.Scope;
import org.eclipse.fennec.model.atlas.scope.ScopeServiceCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
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


	@BeforeEach
	public void before(@InjectBundleContext BundleContext ctx) {

	}

	/**
	 * Test basic scope service tracking with single scope.
	 */
	@WithFactoryConfiguration(factoryPid = "ScopeService", name = "test", location = "?", properties = {
			@Property(key = "name", value = "my-scope"),
			@Property(key = "description", value = "my test scope"),
			@Property(key = "parent.scope", value = "my-parent-scope")
	})
	@Test
	public void testSingleScopeTracking(
			@InjectService(cardinality = 0) ServiceAware<ScopeServiceCollector> serviceCollAware) throws InterruptedException {

	
		ScopeServiceCollector serviceCollector = serviceCollAware.waitForService(2000);
		assertNotNull(serviceCollector);

		Thread.sleep(2000);

		List<Scope> scopes = serviceCollector.getScopes();
		assertEquals(1, scopes.size(), "scope list should have 1 element");

		Scope scope = scopes.get(0);
		assertEquals("my-scope", scope.getName());
		assertEquals("my test scope", scope.getDescription());
		assertEquals("my-parent-scope", scope.getParentScope());
		assertEquals(2, scope.getLinks().size());
		assertTrue(scope.getLinks().containsKey("self"));
		assertEquals("/scopes/my-scope", scope.getLinks().get("self"));
		assertTrue(scope.getLinks().containsKey("schemas"));
		assertEquals("/my-scope/schema", scope.getLinks().get("schemas"));
	}

	/**
	 * Test that the collector tracks multiple scope services.
	 */
	@WithFactoryConfiguration(factoryPid = "ScopeService", name = "scope1", location = "?", properties = {
			@Property(key = "name", value = "tenant-a"),
			@Property(key = "description", value = "Tenant A scope"),
			@Property(key = "parent.scope", value = "atlas")
	})
	@WithFactoryConfiguration(factoryPid = "ScopeService", name = "scope2", location = "?", properties = {
			@Property(key = "name", value = "tenant-b"),
			@Property(key = "description", value = "Tenant B scope"),
			@Property(key = "parent.scope", value = "global")
	})
	@WithFactoryConfiguration(factoryPid = "ScopeService", name = "scope3", location = "?", properties = {
			@Property(key = "name", value = "global"),
			@Property(key = "description", value = "Global corporate scope"),
			@Property(key = "parent.scope", value = "atlas")
	})
	@Test
	public void testMultipleScopeTracking(
			@InjectService(cardinality = 0) ServiceAware<ScopeServiceCollector> serviceCollAware) throws InterruptedException {

		ScopeServiceCollector serviceCollector = serviceCollAware.waitForService(2000);
		assertNotNull(serviceCollector);

		Thread.sleep(2000);

		List<Scope> scopes = serviceCollector.getScopes();
		assertEquals(3, scopes.size(), "scope list should have 3 elements");

		// Verify all scopes are present
		assertTrue(scopes.stream().anyMatch(s -> "tenant-a".equals(s.getName())));
		assertTrue(scopes.stream().anyMatch(s -> "tenant-b".equals(s.getName())));
		assertTrue(scopes.stream().anyMatch(s -> "global".equals(s.getName())));
	}

	/**
	 * Test getScopeByName retrieves the correct scope.
	 */
	@WithFactoryConfiguration(factoryPid = "ScopeService", name = "scope1", location = "?", properties = {
			@Property(key = "name", value = "my-tenant"),
			@Property(key = "description", value = "My tenant scope"),
			@Property(key = "parent.scope", value = "atlas")
	})
	@WithFactoryConfiguration(factoryPid = "ScopeService", name = "scope2", location = "?", properties = {
			@Property(key = "name", value = "other-tenant"),
			@Property(key = "description", value = "Other tenant scope"),
			@Property(key = "parent.scope", value = "atlas")
	})
	@Test
	public void testGetScopeByName(
			@InjectService(cardinality = 0) ServiceAware<ScopeServiceCollector> serviceCollAware) throws InterruptedException {

		ScopeServiceCollector serviceCollector = serviceCollAware.waitForService(2000);
		assertNotNull(serviceCollector);

		Thread.sleep(2000);

		Scope scope = serviceCollector.getScopeByName("my-tenant");
		assertNotNull(scope, "Should find scope by name");
		assertEquals("my-tenant", scope.getName());
		assertEquals("My tenant scope", scope.getDescription());
		assertEquals("atlas", scope.getParentScope());

		Scope otherScope = serviceCollector.getScopeByName("other-tenant");
		assertNotNull(otherScope, "Should find other scope by name");
		assertEquals("other-tenant", otherScope.getName());
	}

	/**
	 * Test getScopeByName returns null for non-existent scope.
	 */
	@WithFactoryConfiguration(factoryPid = "ScopeService", name = "scope1", location = "?", properties = {
			@Property(key = "name", value = "existing-scope"),
			@Property(key = "description", value = "Existing scope"),
			@Property(key = "parent.scope", value = "atlas")
	})
	@Test
	public void testGetScopeByNameNotFound(
			@InjectService(cardinality = 0) ServiceAware<ScopeServiceCollector> serviceCollAware) throws InterruptedException {

		ScopeServiceCollector serviceCollector = serviceCollAware.waitForService(2000);
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
			@InjectService(cardinality = 0) ServiceAware<ScopeServiceCollector> serviceCollAware) throws InterruptedException {

		ScopeServiceCollector serviceCollector = serviceCollAware.waitForService(2000);
		assertNotNull(serviceCollector);

		Thread.sleep(500);

		List<Scope> scopes = serviceCollector.getScopes();
		assertNotNull(scopes, "Scopes list should not be null");
		assertEquals(0, scopes.size(), "Scopes list should be empty");
	}

	/**
	 * Test that scope with default parent scope value.
	 */
	@WithFactoryConfiguration(factoryPid = "ScopeService", name = "defaultParent", location = "?", properties = {
			@Property(key = "name", value = "tenant-with-default-parent"),
			@Property(key = "description", value = "Tenant with default parent")
			// parent.scope not specified - should use default "atlas"
	})
	@Test
	public void testScopeWithDefaultParent(
			@InjectService(cardinality = 0) ServiceAware<ScopeServiceCollector> serviceCollAware) throws InterruptedException {

		ScopeServiceCollector serviceCollector = serviceCollAware.waitForService(2000);
		assertNotNull(serviceCollector);

		Thread.sleep(2000);

		Scope scope = serviceCollector.getScopeByName("tenant-with-default-parent");
		assertNotNull(scope);
		assertEquals("tenant-with-default-parent", scope.getName());
		assertEquals("Tenant with default parent", scope.getDescription());
		// Note: The current implementation doesn't set default, so this might be null
		// Uncomment if default handling is added to ScopeServiceCollector
		 assertEquals("atlas", scope.getParentScope());
	}

	/**
	 * Test that scope with minimal configuration (only name).
	 */
	@WithFactoryConfiguration(factoryPid = "ScopeService", name = "minimal", location = "?", properties = {
			@Property(key = "name", value = "minimal-scope")
			// description and parent.scope not specified
	})
	@Test
	public void testMinimalScopeConfiguration(
			@InjectService(cardinality = 0) ServiceAware<ScopeServiceCollector> serviceCollAware) throws InterruptedException {

		ScopeServiceCollector serviceCollector = serviceCollAware.waitForService(2000);
		assertNotNull(serviceCollector);

		Thread.sleep(2000);

		Scope scope = serviceCollector.getScopeByName("minimal-scope");
		assertNotNull(scope);
		assertEquals("minimal-scope", scope.getName());
		assertEquals(2, scope.getLinks().size());
		assertEquals("/scopes/minimal-scope", scope.getLinks().get("self"));
		assertEquals("/minimal-scope/schema", scope.getLinks().get("schemas"));
	}

	/**
	 * Test that links are correctly generated for scopes.
	 */
	@WithFactoryConfiguration(factoryPid = "ScopeService", name = "linksTest", location = "?", properties = {
			@Property(key = "name", value = "test-links-scope"),
			@Property(key = "description", value = "Testing links generation")
	})
	@Test
	public void testScopeLinksGeneration(
			@InjectService(cardinality = 0) ServiceAware<ScopeServiceCollector> serviceCollAware) throws InterruptedException {

		ScopeServiceCollector serviceCollector = serviceCollAware.waitForService(2000);
		assertNotNull(serviceCollector);

		Thread.sleep(2000);

		Scope scope = serviceCollector.getScopeByName("test-links-scope");
		assertNotNull(scope);

		assertEquals(2, scope.getLinks().size(), "Should have exactly 2 links");

		// Verify self link
		assertTrue(scope.getLinks().containsKey("self"));
		assertEquals("/scopes/test-links-scope", scope.getLinks().get("self"));

		// Verify schemas link
		assertTrue(scope.getLinks().containsKey("schemas"));
		assertEquals("/test-links-scope/schema", scope.getLinks().get("schemas"));
	}

	/**
	 * Test dynamic scope removal when configuration is deleted.
	 */
	@WithFactoryConfiguration(factoryPid = "ScopeService", name = "dynamicScope", location = "?", properties = {
			@Property(key = "name", value = "dynamic-scope"),
			@Property(key = "description", value = "Scope to be removed dynamically")
	})
	@Test
	public void testDynamicScopeRemoval(
			@InjectService(cardinality = 0) ServiceAware<ScopeServiceCollector> serviceCollAware,
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(
					factoryPid = "ScopeService",
					name = "dynamicScope",
					location = "?")) Configuration configuration) throws Exception {

		ScopeServiceCollector serviceCollector = serviceCollAware.waitForService(2000);
		assertNotNull(serviceCollector);

		Thread.sleep(2000);

		// Verify scope is initially present
		Scope scope = serviceCollector.getScopeByName("dynamic-scope");
		assertNotNull(scope, "Scope should be present initially");
		assertEquals(1, serviceCollector.getScopes().size(), "Should have 1 scope");

		// Delete the configuration to trigger unbind
		configuration.delete();
		Thread.sleep(2000);

		// Verify scope has been removed
		Scope removedScope = serviceCollector.getScopeByName("dynamic-scope");
		assertNull(removedScope, "Scope should be removed after configuration deletion");
		assertEquals(0, serviceCollector.getScopes().size(), "Should have 0 scopes after removal");
	}

	/**
	 * Test dynamic scope re-registration after removal.
	 */
	@Test
	public void testDynamicScopeReRegistration(
			@InjectService(cardinality = 0) ServiceAware<ScopeServiceCollector> serviceCollAware,
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(
					factoryPid = "ScopeService",
					name = "reregScope",
					location = "?")) Configuration configuration,
			@InjectService ServiceAware<ConfigurationAdmin> configAdminAware) throws Exception {

		ScopeServiceCollector serviceCollector = serviceCollAware.waitForService(2000);
		assertNotNull(serviceCollector);

		// Initially no scopes
		assertEquals(0, serviceCollector.getScopes().size(), "Should start with 0 scopes");

		// Create a scope configuration
		Dictionary<String, Object> props = new Hashtable<>();
		props.put("name", "rereg-scope");
		props.put("description", "Re-registration test scope");
		props.put("parent.scope", "atlas");
		configuration.update(props);

		Thread.sleep(2000);

		// Verify scope is registered
		Scope scope = serviceCollector.getScopeByName("rereg-scope");
		assertNotNull(scope, "Scope should be registered");
		assertEquals("rereg-scope", scope.getName());
		assertEquals(1, serviceCollector.getScopes().size());

		// Delete configuration
		configuration.delete();
		Thread.sleep(2000);

		// Verify removal
		assertNull(serviceCollector.getScopeByName("rereg-scope"), "Scope should be removed");
		assertEquals(0, serviceCollector.getScopes().size());

		// Re-register with updated properties
		ConfigurationAdmin configAdmin = configAdminAware.waitForService(1000);
		configuration = configAdmin.getFactoryConfiguration("ScopeService", "rereg", "?");
		props.put("description", "Updated description after re-registration");
		configuration.update(props);
		Thread.sleep(2000);

		// Verify re-registration with updated properties
		Scope reregisteredScope = serviceCollector.getScopeByName("rereg-scope");
		assertNotNull(reregisteredScope, "Scope should be re-registered");
		assertEquals("rereg-scope", reregisteredScope.getName());
		assertEquals("Updated description after re-registration", reregisteredScope.getDescription());
		assertEquals(1, serviceCollector.getScopes().size());

		// Cleanup
		configuration.delete();
	}

	/**
	 * Test that scope names with special characters are handled correctly in links.
	 */
	@WithFactoryConfiguration(factoryPid = "ScopeService", name = "specialChars", location = "?", properties = {
			@Property(key = "name", value = "tenant-with-dashes"),
			@Property(key = "description", value = "Scope with special characters in name")
	})
	@Test
	public void testScopeNameWithSpecialCharacters(
			@InjectService(cardinality = 0) ServiceAware<ScopeServiceCollector> serviceCollAware) throws InterruptedException {

		ScopeServiceCollector serviceCollector = serviceCollAware.waitForService(2000);
		assertNotNull(serviceCollector);

		Thread.sleep(2000);

		Scope scope = serviceCollector.getScopeByName("tenant-with-dashes");
		assertNotNull(scope);
		assertEquals("tenant-with-dashes", scope.getName());

		// Verify links are correctly formed with special characters
		assertEquals("/scopes/tenant-with-dashes", scope.getLinks().get("self"));
		assertEquals("/tenant-with-dashes/schema", scope.getLinks().get("schemas"));
	}

	/**
	 * Test scope update when configuration properties are modified.
	 */
	@Test
	public void testScopeConfigurationUpdate(
			@InjectService(cardinality = 0) ServiceAware<ScopeServiceCollector> serviceCollAware,
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(
					factoryPid = "ScopeService",
					name = "updateTest",
					location = "?")) Configuration configuration) throws Exception {

		ScopeServiceCollector serviceCollector = serviceCollAware.waitForService(2000);
		assertNotNull(serviceCollector);

		// Create initial configuration
		Dictionary<String, Object> props = new Hashtable<>();
		props.put("name", "update-test-scope");
		props.put("description", "Initial description");
		props.put("parent.scope", "atlas");
		configuration.update(props);

		Thread.sleep(2000);

		// Verify initial state
		Scope scope = serviceCollector.getScopeByName("update-test-scope");
		assertNotNull(scope);
		assertEquals("Initial description", scope.getDescription());
		assertEquals("atlas", scope.getParentScope());

		// Update configuration (this typically triggers unbind/bind cycle)
		props.put("description", "Updated description");
		props.put("parent.scope", "global");
		configuration.update(props);

		Thread.sleep(2000);

		// Verify updated state
		Scope updatedScope = serviceCollector.getScopeByName("update-test-scope");
		assertNotNull(updatedScope, "Scope should still exist after update");
		assertEquals("update-test-scope", updatedScope.getName());
		assertEquals("Updated description", updatedScope.getDescription());
		assertEquals("global", updatedScope.getParentScope());

		// Cleanup
		configuration.delete();
	}

}
