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
import org.eclipse.fennec.model.atlas.rest.client.api.DecodingRegistryProvider;
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
	/** The packages this runtime ships itself; they win over an Atlas copy (#330). */
	private final EPackage.Registry localPackages;
	private final DriftWatcher driftWatcher;

	private volatile RemoteEPackageProviderImpl ePackages;
	private final Map<String, RemoteReadableScopeService> readOnlyScopes = new ConcurrentHashMap<>();
	/** Optional (#347): the registries a runtime wants objects decoded against; {@code null} for none. */
	private final DecodingRegistryProvider decodingRegistries;
	/**
	 * The client's own decode registries, one per (scope, stage) (#347): repeated reads of a
	 * stage bind one instance of each package instead of a fresh copy per read.
	 */
	private final Map<DecodingKey, AtlasDelegatingPackageRegistry> ownDecodingRegistries = new ConcurrentHashMap<>();

	ModelAtlasClientImpl(ClientConfiguration configuration, Client client) {
		this(configuration, client, EPackage.Registry.INSTANCE);
	}

	ModelAtlasClientImpl(ClientConfiguration configuration, Client client, EPackage.Registry localPackages) {
		this(configuration, client, localPackages, null);
	}

	ModelAtlasClientImpl(ClientConfiguration configuration, Client client, EPackage.Registry localPackages,
			DecodingRegistryProvider decodingRegistries) {
		this(configuration, client, new XmiEPackageDeserializer(localPackages), localPackages, decodingRegistries);
	}

	ModelAtlasClientImpl(ClientConfiguration configuration, Client client, EPackageDeserializer deserializer) {
		this(configuration, client, deserializer, EPackage.Registry.INSTANCE);
	}

	ModelAtlasClientImpl(ClientConfiguration configuration, Client client, EPackageDeserializer deserializer,
			EPackage.Registry localPackages) {
		this(configuration, client, deserializer, localPackages, null);
	}

	ModelAtlasClientImpl(ClientConfiguration configuration, Client client, EPackageDeserializer deserializer,
			EPackage.Registry localPackages, DecodingRegistryProvider decodingRegistries) {
		this.decodingRegistries = decodingRegistries;
		this.configuration = Objects.requireNonNull(configuration, "configuration");
		this.client = Objects.requireNonNull(client, "client");
		this.deserializer = Objects.requireNonNull(deserializer, "deserializer");
		this.localPackages = Objects.requireNonNull(localPackages, "localPackages");
		this.baseTarget = client.target(configuration.getBaseUri());
		// A client that mirrors the Atlas (EAGER) or pre-fetches a fixed nsURI list
		// (HYBRID) must learn about packages that appear after start-up; a LAZY client
		// fetches on demand and needs no discovery (issue #228).
		boolean discoverAdditions = configuration.getMode() != ResolutionMode.LAZY;
		this.driftWatcher = new DriftWatcher(baseTarget, this::scopesToWatch, this::ePackagesImpl,
				readOnlyScopes::get, configuration.getDriftCheckIntervalMs(), discoverAdditions,
				// Where to look for a package the stage-free read cannot serve, before concluding
				// it was deleted (#286).
				configuration::getEagerStages);
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
				s -> new RemoteReadableScopeService(baseTarget, configuration, s,
						(stage, finalStage) -> newDecodingResourceSet(s, stage, finalStage)));
	}

	/**
	 * A transient {@link ResourceSet} for decoding one fetched EObject's XMI, over the
	 * long-lived package registry of its scope and stage ({@link #decodingRegistry}).
	 */
	private ResourceSet newDecodingResourceSet(String scope, String stage, boolean finalStage) {
		return newAtlasResourceSet(decodingRegistry(scope, stage, finalStage));
	}

	/**
	 * The package registry an object read from {@code scope} at {@code stage} is decoded
	 * against (#347). Every read of a stage has to bind the same package instances, and
	 * preferably the ones the rest of the runtime holds:
	 * <ol>
	 * <li>the registry the {@link DecodingRegistryProvider} supplies for the stage as named;</li>
	 * <li>for the scope's final stage, the one it supplies for the stage-free case - the final
	 * stage <em>is</em> what a stage-free read resolves;</li>
	 * <li>otherwise the client's own registry for the stage, created once. The final stage
	 * resolves stage-free there as well, into the provider's nsURI cache, so a read that names
	 * the final stage binds what a stage-free read binds.</li>
	 * </ol>
	 */
	EPackage.Registry decodingRegistry(String scope, String stage, boolean finalStage) {
		EPackage.Registry shared = supplied(scope, stage);
		if (shared == null && stage != null && finalStage) {
			shared = supplied(scope, null);
		}
		if (shared != null) {
			return new SharedDecodingRegistry(localPackages, shared);
		}
		String packageStage = finalStage ? null : stage;
		return ownDecodingRegistries.computeIfAbsent(new DecodingKey(scope, packageStage), key -> {
			AtlasDelegatingPackageRegistry registry = newAtlasRegistry(key.scope(), key.stage());
			// Long-lived now, so it has to drop what the Atlas changes or removes, like the
			// registry behind newResourceSet().
			addDriftListener(registry);
			return registry;
		});
	}

	private EPackage.Registry supplied(String scope, String stage) {
		return decodingRegistries == null ? null : decodingRegistries.registryFor(scope, stage);
	}

	/** Key of a decode registry; {@code stage == null} is the stage-free (final-stage) one. */
	private record DecodingKey(String scope, String stage) {
	}

	/** A package registry that resolves the local packages first, then the remote Atlas on a miss. */
	private AtlasDelegatingPackageRegistry newAtlasRegistry() {
		return newAtlasRegistry(null, null);
	}

	/**
	 * As {@link #newAtlasRegistry()}, but resolving a remote miss at {@code scope}/{@code stage}
	 * (#272). {@code stage == null} is the stage-free final-stage behaviour.
	 */
	private AtlasDelegatingPackageRegistry newAtlasRegistry(String scope, String stage) {
		return new AtlasDelegatingPackageRegistry(localPackages, ePackagesImpl(), scope, stage);
	}

	/** A ResourceSet with default XMI handling and the given Atlas-aware package registry. */
	private static ResourceSet newAtlasResourceSet(EPackage.Registry registry) {
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
