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
package org.eclipse.fennec.model.atlas.scope.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link ScopeRegistries}.
 *
 * @see <a href="https://github.com/eclipse-fennec/model.atlas/issues/179">issue #179</a>
 */
@DisplayName("ScopeRegistries — resolve a scope's registry by type")
public class ScopeRegistriesTest {

	@Test
	@DisplayName("finds the schema registry under any name")
	void findsTheSchemaRegistryUnderAnyName() {
		ScopeInfo scope = scope("cities", registry("models", RegistryType.SCHEMA),
				registry("person", RegistryType.OTHER), registry("rules", RegistryType.COCL));

		assertEquals(Optional.of("models"), ScopeRegistries.schemaRegistryName(scope));
		assertEquals("models", ScopeRegistries.schemaRegistry(scope).map(RegistryInfo::getName).orElse(null));
	}

	@Test
	@DisplayName("empty when the scope has no schema registry")
	void emptyWithoutASchemaRegistry() {
		ScopeInfo scope = scope("cities", registry("person", RegistryType.OTHER));

		assertTrue(ScopeRegistries.schemaRegistry(scope).isEmpty());
		assertTrue(ScopeRegistries.schemaRegistryName(scope).isEmpty());
	}

	@Test
	@DisplayName("empty for a scope that publishes no information")
	void emptyForANullScope() {
		assertTrue(ScopeRegistries.schemaRegistry(null).isEmpty());
		assertTrue(ScopeRegistries.schemaRegistryName(null).isEmpty());
	}

	@Test
	@DisplayName("a registry without a name is not a usable answer")
	void aNamelessRegistryYieldsNoName() {
		ScopeInfo scope = scope("cities", registry(null, RegistryType.SCHEMA));

		assertTrue(ScopeRegistries.schemaRegistry(scope).isPresent(), "the registry itself is still found");
		assertTrue(ScopeRegistries.schemaRegistryName(scope).isEmpty(), "but it cannot be addressed by name");
	}

	@Test
	@DisplayName("two schema registries fail loudly instead of picking one")
	void twoSchemaRegistriesThrow() {
		// ScopeServiceImpl refuses the second one at bind time; if a ScopeInfo assembled
		// elsewhere still carries two, silently taking the first would mean different
		// callers disagree about which registry the scope's schema endpoint speaks for.
		ScopeInfo scope = scope("cities", registry("models", RegistryType.SCHEMA),
				registry("schemata", RegistryType.SCHEMA));

		IllegalStateException failure = assertThrows(IllegalStateException.class,
				() -> ScopeRegistries.schemaRegistryName(scope));

		assertTrue(failure.getMessage().contains("models") && failure.getMessage().contains("schemata"),
				"the message must name both candidates | was: " + failure.getMessage());
		assertTrue(failure.getMessage().contains("cities"), "and the scope they belong to");
	}

	@Test
	@DisplayName("other types resolve the same way")
	void resolvesAnyType() {
		ScopeInfo scope = scope("cities", registry("models", RegistryType.SCHEMA),
				registry("trafos", RegistryType.TRANSFORMATION));

		assertEquals("trafos", ScopeRegistries.ofType(scope, RegistryType.TRANSFORMATION)
				.map(RegistryInfo::getName).orElse(null));
		assertTrue(ScopeRegistries.ofType(scope, RegistryType.COCL).isEmpty());
	}

	private static ScopeInfo scope(String name, RegistryInfo... registries) {
		ScopeInfo scope = ScopeApiFactory.eINSTANCE.createScopeInfo();
		scope.setName(name);
		for (RegistryInfo registry : registries) {
			scope.getRegistries().add(registry);
		}
		return scope;
	}

	private static RegistryInfo registry(String name, RegistryType type) {
		RegistryInfo registry = ScopeApiFactory.eINSTANCE.createRegistryInfo();
		registry.setName(name);
		registry.setType(type);
		return registry;
	}
}
