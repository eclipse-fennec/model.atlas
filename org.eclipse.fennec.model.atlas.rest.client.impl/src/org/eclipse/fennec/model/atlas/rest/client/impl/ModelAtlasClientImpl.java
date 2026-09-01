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
package org.eclipse.fennec.model.atlas.rest.client.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.model.atlas.rest.client.api.ClientConfiguration;
import org.eclipse.fennec.model.atlas.rest.client.api.ResolutionMode;
import org.eclipse.fennec.model.atlas.rest.client.api.DriftListener;
import org.eclipse.fennec.model.atlas.rest.client.api.DriftReport;
import org.eclipse.fennec.model.atlas.rest.client.api.ModelAtlasClient;
import org.eclipse.fennec.model.atlas.rest.client.api.RemoteEPackageProvider;
import org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService;
import org.eclipse.fennec.model.atlas.scope.api.RegistryInfo;

import tools.jackson.databind.JsonNode;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * Plain-Java {@link ModelAtlasClient}.
 * <p>
 * Holds the {@link Client} produced through the
 * {@link org.eclipse.fennec.model.atlas.rest.client.api.JakartaRsClientProvider}
 * seam (P2-2) and the base {@link WebTarget} rooted at {@code base.uri}.
 * {@link #listScopeNames()} and {@link #ePackages()} are the read-only EPackage
 * REST mapping (P2-3); the package body decode is delegated to an
 * {@link EPackageDeserializer} (P2-4); the cache is P2-5/P2-6.
 * {@link #checkForDrift()} / {@link #addDriftListener} are backed by a
 * {@link DriftWatcher} (P2-7); {@link #newResourceSet()} installs an
 * {@link AtlasDelegatingPackageRegistry} (P2-8).
 */
public class ModelAtlasClientImpl implements ModelAtlasClient {

	private final ClientConfiguration configuration;
	private final Client client;
	private final WebTarget baseTarget;
	private final EPackageDeserializer deserializer;
	private final DriftWatcher driftWatcher;

	private volatile RemoteEPackageProviderImpl ePackages;
	private final Map<String, RemoteReadableScopeService> readOnlyScopes = new ConcurrentHashMap<>();

	ModelAtlasClientImpl(ClientConfiguration configuration, Client client) {
		this(configuration, client, new XmiEPackageDeserializer());
	}

	ModelAtlasClientImpl(ClientConfiguration configuration, Client client, EPackageDeserializer deserializer) {
		this.configuration = Objects.requireNonNull(configuration, "configuration");
		this.client = Objects.requireNonNull(client, "client");
		this.deserializer = Objects.requireNonNull(deserializer, "deserializer");
		this.baseTarget = client.target(configuration.getBaseUri());
		// A client that mirrors the Atlas (EAGER) or pre-fetches a fixed nsURI list
		// (HYBRID) must learn about packages that appear after start-up; a LAZY client
		// fetches on demand and needs no discovery (issue #228).
		boolean discoverAdditions = configuration.getMode() != ResolutionMode.LAZY;
		this.driftWatcher = new DriftWatcher(baseTarget, this::scopesToWatch, this::ePackagesImpl,
				readOnlyScopes::get, configuration.getDriftCheckIntervalMs(), discoverAdditions);
		this.driftWatcher.start();
	}

	/** The effective configuration this client was built from. */
	ClientConfiguration getConfiguration() {
		return configuration;
	}

	/** The base {@link WebTarget} rooted at {@code base.uri}. */
	WebTarget getBaseTarget() {
		return baseTarget;
	}

	@Override
	public List<String> listScopeNames() {
		Response response = RestSupport.get(baseTarget.path("scopes"), MediaType.APPLICATION_JSON);
		try {
			if (!RestSupport.isSuccess(response)) {
				throw RestSupport.statusError(response, "listScopeNames");
			}
			return parseScopeNames(response.readEntity(String.class));
		} finally {
			response.close();
		}
	}

	@Override
	public RemoteEPackageProvider ePackages() {
		return ePackagesImpl();
	}

	/** The concrete provider (lazily built); shared by {@link #ePackages()} and the drift watcher. */
	private RemoteEPackageProviderImpl ePackagesImpl() {
		RemoteEPackageProviderImpl local = ePackages;
		if (local == null) {
			synchronized (this) {
				local = ePackages;
				if (local == null) {
					local = new RemoteEPackageProviderImpl(baseTarget, configuration, deserializer,
							this::listScopeNames);
					ePackages = local;
				}
			}
		}
		return local;
	}

	@Override
	public DriftReport checkForDrift() {
		return driftWatcher.check();
	}

	@Override
	public AutoCloseable addDriftListener(DriftListener listener) {
		return driftWatcher.addListener(listener);
	}

	/** Scopes the drift watcher probes: the configured allow-list, else every scope on the server. */
	private List<String> scopesToWatch() {
		List<String> allowList = configuration.getScopeAllowList();
		return allowList.isEmpty() ? listScopeNames() : allowList;
	}

	@Override
	public ResourceSet newResourceSet() {
		AtlasDelegatingPackageRegistry registry = newAtlasRegistry();
		ResourceSet resourceSet = newAtlasResourceSet(registry);
		// Long-lived consumer set: keep the registry fresh by evicting on drift so the
		// next look-up re-fetches.
		addDriftListener(registry);
		return resourceSet;
	}

	@Override
	public List<String> listRegistries(String scopeName) {
		return readOnlyScope(scopeName).getScopeInfo().getRegistries().stream().map(RegistryInfo::getName)
				.filter(Objects::nonNull).toList();
	}

	@Override
	public ReadableScopeService<EObject> readOnlyScope(String scopeName) {
		Objects.requireNonNull(scopeName, "scopeName");
		// One service (and one cache) per scope; repeated calls return the same instance.
		return readOnlyScopes.computeIfAbsent(scopeName,
				s -> new RemoteReadableScopeService(baseTarget, configuration, s, this::newDecodingResourceSet));
	}

	/**
	 * A transient, Atlas-aware {@link ResourceSet} for decoding a fetched EObject's XMI.
	 * Like {@link #newResourceSet()} but <em>not</em> registered as a drift listener: it
	 * is short-lived (one decode), so registering it would leak a listener on every
	 * {@code get(...)}. Remote package look-ups still go through the shared, drift-aware
	 * EPackage provider.
	 */
	private ResourceSet newDecodingResourceSet() {
		return newAtlasResourceSet(newAtlasRegistry());
	}

	/** A package registry that resolves local/INSTANCE first, then the remote Atlas on a miss. */
	private AtlasDelegatingPackageRegistry newAtlasRegistry() {
		return new AtlasDelegatingPackageRegistry(EPackage.Registry.INSTANCE, ePackagesImpl());
	}

	/** A ResourceSet with default XMI handling and the given Atlas-aware package registry. */
	private static ResourceSet newAtlasResourceSet(AtlasDelegatingPackageRegistry registry) {
		ResourceSetImpl resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		resourceSet.setPackageRegistry(registry);
		return resourceSet;
	}

	@Override
	public void close() {
		driftWatcher.close();
		client.close();
	}

	/** Extract scope names from a {@code ScopeListResponse} JSON body. */
	private static List<String> parseScopeNames(String json) {
		JsonNode scopes = RestSupport.parse(json, "listScopeNames").path("scopes");
		List<String> names = new ArrayList<>();
		for (JsonNode scope : scopes) {
			JsonNode name = scope.get("name");
			if (name != null && !name.isNull()) {
				names.add(name.asText());
			}
		}
		return List.copyOf(names);
	}
}
