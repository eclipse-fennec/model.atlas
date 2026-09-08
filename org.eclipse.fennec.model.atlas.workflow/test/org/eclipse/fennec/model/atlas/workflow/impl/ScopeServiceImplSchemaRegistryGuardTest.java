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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.scope.api.RegistryType;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Registry;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;
import org.mockito.junit.jupiter.MockitoSettings;

/**
 * A scope may define at most one registry of type {@link RegistryType#SCHEMA}.
 *
 * <p>
 * Everything that resolves "the schema registry of this scope" by type does so with
 * {@code findFirst()} — the REST layer (issue #179), the registry chain configurator, the
 * validation service. That is only an honest answer while a scope cannot have two, so the
 * scope service refuses the second one instead of letting bind order decide which of them
 * the API speaks for.
 * </p>
 *
 * @see <a href="https://github.com/eclipse-fennec/model.atlas/issues/179">issue #179</a>
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ScopeServiceImpl — one SCHEMA registry per scope")
public class ScopeServiceImplSchemaRegistryGuardTest {

	private static final String SCOPE = "test-scope";

	@Mock
	private ScopeServiceConfig config;

	private ScopeServiceImpl<EObject> scopeService;

	@BeforeEach
	void setUp() {
		when(config.scope_name()).thenReturn(SCOPE);
		when(config.scope_description()).thenReturn("a scope under test");
		when(config.scope_parent()).thenReturn("atlas");
		scopeService = new ScopeServiceImpl<>(config);
	}

	@Test
	@DisplayName("the schema registry may carry any name")
	void bindsASchemaRegistryWhateverItIsCalled() {
		RegistryService<EObject> models = registryService("models", RegistryType.SCHEMA);

		scopeService.bindRegistryService(models, Map.of());

		assertTrue(scopeService.isValidRegistry("models"), "a SCHEMA registry named 'models' must bind");
		verify(models).activate(SCOPE);
	}

	@Test
	@DisplayName("a second SCHEMA registry is refused, the first one stays")
	void refusesASecondSchemaRegistry() {
		RegistryService<EObject> first = registryService("models", RegistryType.SCHEMA);
		RegistryService<EObject> second = registryService("schemata", RegistryType.SCHEMA);

		scopeService.bindRegistryService(first, Map.of());
		scopeService.bindRegistryService(second, Map.of());

		assertTrue(scopeService.isValidRegistry("models"), "the registry that bound first must stay");
		assertFalse(scopeService.isValidRegistry("schemata"),
				"a scope must not end up with two SCHEMA registries — the second one must be refused");
		assertEquals(List.of("models"), scopeService.getAllRegistries());
		assertEquals(1, schemaRegistryCount(), "exactly one SCHEMA registry may be visible on the scope");
		verify(second, never()).activate(any());
	}

	@Test
	@DisplayName("registries of every other type bind alongside it")
	void bindsRegistriesOfOtherTypes() {
		scopeService.bindRegistryService(registryService("models", RegistryType.SCHEMA), Map.of());
		scopeService.bindRegistryService(registryService("person", RegistryType.OTHER), Map.of());
		scopeService.bindRegistryService(registryService("rules", RegistryType.COCL), Map.of());
		scopeService.bindRegistryService(registryService("trafos", RegistryType.TRANSFORMATION), Map.of());

		assertTrue(scopeService.isValidRegistry("person"));
		assertTrue(scopeService.isValidRegistry("rules"));
		assertTrue(scopeService.isValidRegistry("trafos"));
		assertEquals(4, scopeService.getAllRegistries().size(), "only SCHEMA is limited to one per scope");
	}

	@Test
	@DisplayName("rebinding the same schema registry is an update, not a second one")
	void rebindingTheSameNameIsNotASecondSchemaRegistry() {
		// A configuration update unbinds and rebinds the same registry name; with a guard
		// that only counted types, the update would be refused and the scope would lose its
		// schema registry.
		scopeService.bindRegistryService(registryService("models", RegistryType.SCHEMA), Map.of());
		RegistryService<EObject> updated = registryService("models", RegistryType.SCHEMA);

		scopeService.bindRegistryService(updated, Map.of());

		assertTrue(scopeService.isValidRegistry("models"), "the rebound registry must still be there");
		assertEquals(1, schemaRegistryCount());
		verify(updated).activate(SCOPE);
	}

	@Test
	@DisplayName("unbinding the schema registry frees the slot for another one")
	void unbindingFreesTheSlot() {
		RegistryService<EObject> models = registryService("models", RegistryType.SCHEMA);
		scopeService.bindRegistryService(models, Map.of());
		scopeService.unbindRegistryService(models, Map.of());

		scopeService.bindRegistryService(registryService("schemata", RegistryType.SCHEMA), Map.of());

		assertTrue(scopeService.isValidRegistry("schemata"),
				"once the first one is gone, another SCHEMA registry must be able to take the slot");
		assertEquals(1, schemaRegistryCount());
	}

	@Test
	@DisplayName("a refused registry going away does not disturb the one that is held")
	void unbindingARefusedRegistryLeavesTheScopeAlone() {
		RegistryService<EObject> held = registryService("models", RegistryType.SCHEMA);
		RegistryService<EObject> refused = registryService("schemata", RegistryType.SCHEMA);
		scopeService.bindRegistryService(held, Map.of());
		scopeService.bindRegistryService(refused, Map.of());

		scopeService.unbindRegistryService(refused, Map.of());

		assertTrue(scopeService.isValidRegistry("models"), "the bound registry must survive");
		verify(refused, never()).deactivate(any());
	}

	@Test
	@DisplayName("unbinding a replaced instance leaves its replacement in place")
	void unbindingAReplacedInstanceKeepsTheReplacement() {
		// A GREEDY rebind binds the new service before unbinding the old one, both under the
		// same registry name; removing by name alone would have dropped the replacement and
		// left the scope without that registry.
		RegistryService<EObject> old = registryService("models", RegistryType.SCHEMA);
		RegistryService<EObject> replacement = registryService("models", RegistryType.SCHEMA);
		scopeService.bindRegistryService(old, Map.of());
		scopeService.bindRegistryService(replacement, Map.of());

		scopeService.unbindRegistryService(old, Map.of());

		assertTrue(scopeService.isValidRegistry("models"), "the replacement must still serve the scope");
		assertEquals(1, schemaRegistryCount());
	}

	private long schemaRegistryCount() {
		return scopeService.getScopeInfo().getRegistries().stream()
				.filter(registry -> RegistryType.SCHEMA == registry.getType())
				.count();
	}

	private RegistryService<EObject> registryService(String name, RegistryType type) {
		@SuppressWarnings("unchecked")
		RegistryService<EObject> service = org.mockito.Mockito.mock(RegistryService.class);
		Registry registry = WorkflowApiFactory.eINSTANCE.createRegistry();
		registry.setName(name);
		registry.setType(type);
		when(service.getRegistryName()).thenReturn(name);
		when(service.getRegistry()).thenReturn(registry);
		return service;
	}
}
