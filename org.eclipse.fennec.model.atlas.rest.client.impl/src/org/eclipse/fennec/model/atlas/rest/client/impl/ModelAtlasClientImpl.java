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
import java.util.Objects;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.model.atlas.rest.client.api.ClientConfiguration;
import org.eclipse.fennec.model.atlas.rest.client.api.DriftListener;
import org.eclipse.fennec.model.atlas.rest.client.api.DriftReport;
import org.eclipse.fennec.model.atlas.rest.client.api.ModelAtlasClient;
import org.eclipse.fennec.model.atlas.rest.client.api.RemoteEPackageProvider;

import com.fasterxml.jackson.databind.JsonNode;

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

	ModelAtlasClientImpl(ClientConfiguration configuration, Client client) {
		this(configuration, client, new XmiEPackageDeserializer());
	}

	ModelAtlasClientImpl(ClientConfiguration configuration, Client client, EPackageDeserializer deserializer) {
		this.configuration = Objects.requireNonNull(configuration, "configuration");
		this.client = Objects.requireNonNull(client, "client");
		this.deserializer = Objects.requireNonNull(deserializer, "deserializer");
		this.baseTarget = client.target(configuration.getBaseUri());
		this.driftWatcher = new DriftWatcher(baseTarget, this::scopesToWatch, this::ePackagesImpl,
				configuration.getDriftCheckIntervalMs());
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
		ResourceSetImpl resourceSet = new ResourceSetImpl();
		// Default XMI handling so plain-Java consumers can load instances out of the box.
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		// Atlas-aware registry: local/INSTANCE first, then a remote fetch on a miss.
		AtlasDelegatingPackageRegistry registry = new AtlasDelegatingPackageRegistry(EPackage.Registry.INSTANCE,
				ePackagesImpl());
		resourceSet.setPackageRegistry(registry);
		// Keep the registry fresh: evict on drift so the next look-up re-fetches.
		addDriftListener(registry);
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
