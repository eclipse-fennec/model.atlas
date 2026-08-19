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
package org.eclipse.fennec.model.atlas.eobject.provider;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView;
import org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService;
import org.eclipse.fennec.model.atlas.scope.api.ScopeInfo;

/**
 * In-memory {@link ReadableScopeService} with switchable failure modes per registry:
 * {@code failListing} makes listing throw, {@code failObjects} makes individual fetches
 * throw, {@code missingObjects} answers empty.
 */
class FakeScopeService implements ReadableScopeService<EObject> {

	private final String scopeName;
	private final Map<String, Map<String, EObject>> registries = new LinkedHashMap<>();
	final Set<String> failListing = new HashSet<>();
	final Set<String> failObjects = new HashSet<>();
	final Set<String> missingObjects = new HashSet<>();
	final List<String> stagedViewRequests = new ArrayList<>();

	FakeScopeService(String scopeName) {
		this.scopeName = scopeName;
	}

	FakeScopeService put(String registry, String objectId, EObject object) {
		registries.computeIfAbsent(registry, r -> new LinkedHashMap<>()).put(objectId, object);
		return this;
	}

	FakeScopeService remove(String registry, String objectId) {
		registries.getOrDefault(registry, new HashMap<>()).remove(objectId);
		return this;
	}

	@Override
	public String getScopeName() {
		return scopeName;
	}

	@Override
	public boolean isInheritingFromParentScope() {
		return false;
	}

	@Override
	public Optional<EObject> get(String registry, String objectId) {
		return registryView(registry).get(objectId);
	}

	@Override
	public List<String> listObjectIds(String registry) {
		return registryView(registry).listObjectIds();
	}

	@Override
	public List<EObject> listAll(String registry) {
		return registryView(registry).listAll();
	}

	@Override
	public Stream<EObject> stream(String registry) {
		return listAll(registry).stream();
	}

	@Override
	public ScopeInfo getScopeInfo() {
		throw new UnsupportedOperationException("not used by the engine");
	}

	@Override
	public ReadableRegistryView<EObject> registryView(String registry) {
		return view(registry, "");
	}

	@Override
	public ReadableRegistryView<EObject> registryView(String registry, String stage) {
		stagedViewRequests.add(registry + "@" + stage);
		return view(registry, stage);
	}

	private ReadableRegistryView<EObject> view(String registry, String stage) {
		return new ReadableRegistryView<EObject>() {

			@Override
			public String getScopeName() {
				return scopeName;
			}

			@Override
			public String getRegistryName() {
				return registry;
			}

			@Override
			public String getStageName() {
				return stage;
			}

			@Override
			public Optional<EObject> get(String objectId) {
				if (failObjects.contains(objectId)) {
					throw new IllegalStateException("fetch of " + objectId + " fails");
				}
				if (missingObjects.contains(objectId)) {
					return Optional.empty();
				}
				return Optional.ofNullable(registries.getOrDefault(registry, new HashMap<>()).get(objectId));
			}

			@Override
			public List<String> listObjectIds() {
				if (failListing.contains(registry)) {
					throw new IllegalStateException("listing of " + registry + " fails");
				}
				return List.copyOf(registries.getOrDefault(registry, new HashMap<>()).keySet());
			}

			@Override
			public List<EObject> listAll() {
				return listObjectIds().stream().map(id -> get(id).orElseThrow()).toList();
			}

			@Override
			public Stream<EObject> stream() {
				return listAll().stream();
			}
		};
	}
}
