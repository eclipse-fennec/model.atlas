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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;
import org.eclipse.fennec.model.atlas.scope.api.AtlasProperties;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

/**
 * P6-6 — generates the ConfigAdmin pair (stock {@code EPackageRegistry} +
 * {@code ResourceSetFactory}) that wires a scope-specific registry chain for a
 * configured Atlas scope (+ optional stage).
 * <p>
 * Mirrors {@code SchemaRegistryChainConfigurator} on the server side, but uses
 * {@code atlas.*} service properties in {@code ePackageConfigurator.target} instead
 * of {@code emf.model.scope}: client publications are tagged {@code atlas.scope} /
 * {@code atlas.stage}, not {@code emf.model.scope}.
 * <p>
 * The {@code parentRegistry.target} of the stock {@code EPackageRegistry} points to
 * the {@link AtlasScopedFetchOnMissRegistry} registered by {@link AtlasClientComponent},
 * which provides stage-aware fetch-on-miss before falling through to the global
 * default registry. This is the hybrid chain:
 * <pre>
 * Stock EPackageRegistry (aggregates prefetched atlas.scope=S[,atlas.stage=ST] packages)
 *   ↓ miss
 * AtlasScopedFetchOnMissRegistry (atlas.fetch.on.miss=true)
 *   ↓ miss
 * Global default registry (default.resourceset.epackage.registry=true)
 * </pre>
 * Not a DS component — lifecycle is managed by {@link AtlasClientComponent}.
 * Call {@link #register(String, String)} for each (scope, stage) pair;
 * call {@link #close()} on deactivation to delete all created configs.
 */
class AtlasEPackageRegistryConfigurator implements AutoCloseable {

	private static final Logger LOGGER = Logger.getLogger(AtlasEPackageRegistryConfigurator.class.getName());

	/** The {@code parentRegistry.target} of the last chain link → the global default registry. */
	static final String DEFAULT_REGISTRY_TARGET = "(default.resourceset.epackage.registry=true)";

	private final ConfigurationAdmin configAdmin;
	private final List<Configuration> configs = new ArrayList<>();

	AtlasEPackageRegistryConfigurator(ConfigurationAdmin configAdmin) {
		this.configAdmin = configAdmin;
	}

	/**
	 * Generate the stock {@code EPackageRegistry} + {@code ResourceSetFactory} ConfigAdmin
	 * factory pair for the given scope and optional stage.
	 *
	 * @param scope the Atlas scope name (required)
	 * @param stage the stage name, or {@code null} for the final/stage-free case
	 * @throws IOException if ConfigAdmin config creation fails
	 */
	void register(String scope, String stage) throws IOException {
		String rsfName = rsfName(scope, stage);
		String instanceName = "atlas-client-" + scope + (stage != null ? "-" + stage : "");
		String fetchOnMissTarget = fetchOnMissTarget(scope, stage);

		Configuration epReg = configAdmin.getFactoryConfiguration(EMFNamespaces.EPACKAGE_REGISTRY_CONFIG_NAME,
				instanceName, "?");
		Hashtable<String, Object> epProps = new Hashtable<>();
		epProps.put(EMFNamespaces.PROP_RESOURCE_SET_FACTORY_NAME, rsfName);
		epProps.put(EMFNamespaces.EPACKAGE_TARGET, ePackageConfiguratorTarget(scope, stage));
		epProps.put("parentRegistry.target", fetchOnMissTarget);
		epReg.update(epProps);
		configs.add(epReg);

		Configuration rsf = configAdmin.getFactoryConfiguration(EMFNamespaces.RESOURCE_SET_FACTORY_CONFIG_NAME,
				instanceName, "?");
		Hashtable<String, Object> rsfProps = new Hashtable<>();
		rsfProps.put(EMFNamespaces.EPACKAGE_REGISTRY_TARGET,
				"(" + EMFNamespaces.PROP_RESOURCE_SET_FACTORY_NAME + "=" + rsfName + ")");
		rsfProps.put("scope.name", scope);
		if (stage != null) {
			rsfProps.put("stage.name", stage);
		}
		rsf.update(rsfProps);
		configs.add(rsf);

		LOGGER.log(Level.INFO, () -> "Registered scope registry chain for scope='" + scope
				+ (stage != null ? "', stage='" + stage : "") + "' (rsf.name=" + rsfName + ")");
	}

	@Override
	public void close() {
		for (Configuration config : configs) {
			try {
				config.delete();
			} catch (IOException e) {
				LOGGER.log(Level.WARNING, "Failed to delete registry config " + config.getPid(), e);
			}
		}
		configs.clear();
	}

	/**
	 * The {@code ePackageConfigurator.target} for the stock registry: filters on
	 * {@code atlas.remote=true} + {@code atlas.scope=S} + {@code atlas.stage=ST} (when
	 * stage is configured). Uses {@code atlas.*} properties — not {@code emf.model.scope}
	 * — because client publications are tagged with Atlas origin, not EMF scope.
	 */
	static String ePackageConfiguratorTarget(String scope, String stage) {
		if (stage != null) {
			return "(&(" + AtlasProperties.ATLAS_REMOTE + "=true)(" + AtlasProperties.ATLAS_SCOPE + "=" + scope + ")("
					+ AtlasProperties.ATLAS_STAGE + "=" + stage + "))";
		}
		return "(&(" + AtlasProperties.ATLAS_REMOTE + "=true)(" + AtlasProperties.ATLAS_SCOPE + "=" + scope
				+ ")(!(" + AtlasProperties.ATLAS_STAGE + "=*)))";
	}

	/**
	 * The {@code parentRegistry.target} pointing to the {@link AtlasScopedFetchOnMissRegistry}
	 * for the given scope+stage.
	 */
	static String fetchOnMissTarget(String scope, String stage) {
		if (stage != null) {
			return "(&(" + AtlasProperties.ATLAS_SCOPE + "=" + scope + ")(" + AtlasProperties.ATLAS_STAGE + "=" + stage
					+ ")(" + AtlasScopedFetchOnMissRegistry.FETCH_ON_MISS_PROPERTY + "=true))";
		}
		return "(&(" + AtlasProperties.ATLAS_SCOPE + "=" + scope + ")("
				+ AtlasScopedFetchOnMissRegistry.FETCH_ON_MISS_PROPERTY + "=true)(!("
				+ AtlasProperties.ATLAS_STAGE + "=*)))";
	}

	static String rsfName(String scope, String stage) {
		return stage != null ? scope + "_" + stage : scope;
	}
}
