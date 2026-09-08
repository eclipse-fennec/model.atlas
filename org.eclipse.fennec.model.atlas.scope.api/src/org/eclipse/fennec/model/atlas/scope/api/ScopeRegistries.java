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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Finds a scope's registry of a given {@link RegistryType}.
 *
 * <p>
 * The schema registry is addressed by <em>type</em>, never by name. {@code /{scope}/schema}
 * is a URL literal that says which kind of registry the request is about, and a deployment
 * is free to call its schema registry {@code models} or anything else (issue #179). The
 * wiring layer has always resolved it this way — the registry chain configurator and the
 * validation service both filter on {@link RegistryType#SCHEMA} — and this is the same rule
 * in one place, so the REST layer no longer has to spell the name {@code "schema"} out.
 * </p>
 *
 * <p>
 * A scope may hold at most one SCHEMA registry; {@code ScopeServiceImpl} refuses a second
 * one when it binds. Should one turn up regardless — a {@link ScopeInfo} assembled
 * elsewhere, a remote scope described by an older server — this fails loudly rather than
 * picking one of them, because either choice would be arbitrary and only some callers would
 * make the same one.
 * </p>
 *
 * @see <a href="https://github.com/eclipse-fennec/model.atlas/issues/179">issue #179</a>
 */
public final class ScopeRegistries {

	private ScopeRegistries() {
		// utility
	}

	/**
	 * The scope's registry of type {@link RegistryType#SCHEMA}.
	 *
	 * @param scope the scope to inspect; may be {@code null}
	 * @return the schema registry, or empty when the scope has none (or is {@code null})
	 * @throws IllegalStateException if the scope declares more than one SCHEMA registry
	 */
	public static Optional<RegistryInfo> schemaRegistry(ScopeInfo scope) {
		return ofType(scope, RegistryType.SCHEMA);
	}

	/**
	 * The name of the scope's schema registry — the name every registry-addressed operation
	 * expects, resolved from the type rather than assumed.
	 *
	 * @param scope the scope to inspect; may be {@code null}
	 * @return the registry name, or empty when the scope has no schema registry
	 * @throws IllegalStateException if the scope declares more than one SCHEMA registry
	 */
	public static Optional<String> schemaRegistryName(ScopeInfo scope) {
		return schemaRegistry(scope).map(RegistryInfo::getName).filter(Objects::nonNull);
	}

	/**
	 * The scope's single registry of {@code type}.
	 *
	 * @param scope the scope to inspect; may be {@code null}
	 * @param type  the registry type to look for
	 * @return the registry, or empty when the scope has none of that type
	 * @throws IllegalStateException if the scope declares more than one registry of that type
	 */
	public static Optional<RegistryInfo> ofType(ScopeInfo scope, RegistryType type) {
		Objects.requireNonNull(type, "type");
		if (scope == null || scope.getRegistries() == null) {
			return Optional.empty();
		}
		List<RegistryInfo> matches = scope.getRegistries().stream()
				.filter(registry -> registry != null && type == registry.getType())
				.toList();
		if (matches.size() > 1) {
			throw new IllegalStateException(String.format(
					"Scope [%s] declares %d registries of type %s (%s); it may declare at most one.",
					scope.getName(), matches.size(), type,
					String.join(", ", matches.stream().map(RegistryInfo::getName).toList())));
		}
		return matches.isEmpty() ? Optional.empty() : Optional.of(matches.get(0));
	}
}
