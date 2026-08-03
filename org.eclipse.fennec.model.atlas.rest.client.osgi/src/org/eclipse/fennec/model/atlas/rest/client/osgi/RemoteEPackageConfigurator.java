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
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.emf.osgi.configurator.EPackageConfigurator;
import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;
import org.eclipse.fennec.emf.osgi.fingerprint.util.FingerprintHelper;
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

	private static final Logger LOGGER = Logger.getLogger(RemoteEPackageConfigurator.class.getName());

	private final EPackage ePackage;
	private final String scope;
	private final String stage;
	private final String version;
	private final String baseUri;
	private final String fingerprint;

	RemoteEPackageConfigurator(EPackage ePackage, String scope, String stage, String version, String baseUri) {
		this(ePackage, scope, stage, version, baseUri, null);
	}

	RemoteEPackageConfigurator(EPackage ePackage, String scope, String stage, String version, String baseUri,
			String serverFingerprint) {
		this.ePackage = Objects.requireNonNull(ePackage, "ePackage");
		this.scope = Objects.requireNonNull(scope, "scope");
		// P5-7: stage is advisory provenance and may be unknown (stage-free final-stage reads);
		// nullable, and omitted from the service properties below when not set.
		this.stage = stage;
		this.baseUri = Objects.requireNonNull(baseUri, "baseUri");
		this.version = (version == null || version.isBlank()) ? DEFAULT_VERSION : version;
		// Computed locally from the parsed package, never adopted from the server
		// ("computed, never trusted"). A server-reported value is only a cross-check:
		// a mismatch means the package survived transport/parsing with different
		// content than the server holds — exactly the drift the fingerprint exists
		// to catch.
		this.fingerprint = computeFingerprint(ePackage);
		if (serverFingerprint != null && fingerprint != null && !serverFingerprint.equals(fingerprint)) {
			LOGGER.warning(() -> "Fingerprint mismatch for " + ePackage.getNsURI() + ": server reports "
					+ serverFingerprint + " but the locally parsed package computes to " + fingerprint);
		}
	}

	private static String computeFingerprint(EPackage ePackage) {
		try {
			return FingerprintHelper.fingerprint(ePackage);
		} catch (Exception e) {
			LOGGER.log(Level.FINE, e,
					() -> "Fingerprint computation failed for " + ePackage.getNsURI() + ": " + e.getMessage());
			return null;
		}
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
		if (stage != null && !stage.isBlank()) {
			properties.put(AtlasProperties.ATLAS_STAGE, stage); // advisory provenance; omitted when unknown (P5-7)
		}
		if (fingerprint != null) {
			// Locally computed content identity; omitted when it could not be computed,
			// so (emf.fingerprint=*) keeps meaning "this service knows its model version".
			properties.put(EMFNamespaces.EMF_MODEL_FINGERPRINT, fingerprint);
		}
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
