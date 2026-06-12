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
package org.eclipse.fennec.model.atlas.rest.client.osgi;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.emf.osgi.configurator.EPackageConfigurator;
import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;
import org.eclipse.fennec.model.atlas.scope.api.AtlasProperties;

/**
 * P3-3 — the {@link EPackageConfigurator} for a single EPackage fetched from a
 * remote Atlas. {@code emf.osgi}'s {@code DefaultEPackageRegistryComponent} binds
 * it and replays {@link #configureEPackage(EPackage.Registry)} into the framework
 * {@code EPackage.Registry}.
 * <p>
 * {@link #getServiceProperties()} carries both the canonical {@code emf.*}
 * properties (always via {@link EMFNamespaces} constants, never string literals)
 * and the {@code atlas.*} origin properties (from {@link AtlasProperties}, always
 * set).
 * <p>
 * {@code emf.model.scope} is set to {@link EMFNamespaces#EMF_MODEL_SCOPE_RESOURCE_SET}
 * ({@code "resourceset"}) — the EMF-OSGi scope concept, <em>unrelated</em> to the
 * Atlas scope. This is what {@code emf.osgi}'s {@code DefaultEPackageRegistryComponent}
 * (and the default {@code ResourceSetFactory}) bind on: its configurator reference is
 * hardcoded to {@code (emf.model.scope=resourceset)}, so only configurators carrying
 * that value populate the global framework {@code EPackage.Registry}. (The server's own
 * {@code DynamicEPackageConfigurator} stamps the Atlas scope instead because it pairs
 * each scope with its own scoped registry / {@code ResourceSetFactory}; the read-only
 * client has no such scoped registries and wants the global one.) The real Atlas scope
 * is still carried explicitly as {@link AtlasProperties#ATLAS_SCOPE}, alongside
 * {@code atlas.remote}/{@code atlas.stage}/{@code atlas.base.uri}.
 */
final class RemoteEPackageConfigurator implements EPackageConfigurator {

	/** Fetched packages are exchanged as ecore/XMI, so this is their file extension. */
	static final String ECORE_FILE_EXTENSION = "ecore";
	/** Version stamped when the caller could not resolve one. */
	static final String DEFAULT_VERSION = "1.0";

	private final EPackage ePackage;
	private final String scope;
	private final String stage;
	private final String version;
	private final String baseUri;

	RemoteEPackageConfigurator(EPackage ePackage, String scope, String stage, String version, String baseUri) {
		this.ePackage = Objects.requireNonNull(ePackage, "ePackage");
		this.scope = Objects.requireNonNull(scope, "scope");
		this.stage = Objects.requireNonNull(stage, "stage");
		this.baseUri = Objects.requireNonNull(baseUri, "baseUri");
		this.version = (version == null || version.isBlank()) ? DEFAULT_VERSION : version;
	}

	@Override
	public void configureEPackage(EPackage.Registry registry) {
		Objects.requireNonNull(registry, "registry");
		registry.put(ePackage.getNsURI(), ePackage);
	}

	@Override
	public void unconfigureEPackage(EPackage.Registry registry) {
		Objects.requireNonNull(registry, "registry");
		registry.remove(ePackage.getNsURI());
	}

	/** The identical property set registered on the configurator, EPackage and EFactory services. */
	Map<String, Object> getServiceProperties() {
		Map<String, Object> properties = new HashMap<>();
		// Canonical EMF-OSGi properties — constants only, never string literals.
		properties.put(EMFNamespaces.EMF_NAME, ePackage.getName());
		properties.put(EMFNamespaces.EMF_MODEL_NSURI, ePackage.getNsURI());
		properties.put(EMFNamespaces.EMF_MODEL_VERSION, version);
		properties.put(EMFNamespaces.EMF_MODEL_FILE_EXT, ECORE_FILE_EXTENSION);
		properties.put(EMFNamespaces.EMF_MODEL_REGISTRATION, EMFNamespaces.MODEL_REGISTRATION_DYNAMIC);
		// "resourceset" — the EMF-OSGi scope DefaultEPackageRegistryComponent binds on,
		// NOT the Atlas scope (which travels as atlas.scope below).
		properties.put(EMFNamespaces.EMF_MODEL_SCOPE, EMFNamespaces.EMF_MODEL_SCOPE_RESOURCE_SET);
		// Atlas origin properties — always set.
		properties.put(AtlasProperties.ATLAS_REMOTE, Boolean.TRUE);
		properties.put(AtlasProperties.ATLAS_SCOPE, scope);
		properties.put(AtlasProperties.ATLAS_STAGE, stage);
		properties.put(AtlasProperties.ATLAS_BASE_URI, baseUri);
		return properties;
	}

	EPackage getEPackage() {
		return ePackage;
	}

	String getNsURI() {
		return ePackage.getNsURI();
	}
}
