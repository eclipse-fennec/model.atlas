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
package org.eclipse.fennec.model.atlas.rest.client.osgi.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Collection;
import java.util.Hashtable;

import org.eclipse.emf.ecore.EPackage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * Which stage the scoped fetch-on-miss registries are bound to (issue #205).
 *
 * <p>
 * Stage names are per-scope and user-defined — a scope may stage its registries
 * {@code draft → release}, another {@code released}, another something else
 * entirely — so no stage name is a correct default. Leaving {@code eager.stages}
 * out must therefore give the <em>stage-free</em> bridge, which resolves each
 * scope's own final stage server-side and is correct whatever the stages are
 * called. A default stage name binds the bridge to the stage-explicit endpoint
 * instead, which answers {@code 400} for a stage the scope does not have, so
 * every look-up on it misses and the ConfigAdmin registry generated alongside it
 * can never resolve anything.
 * </p>
 *
 * <p>
 * No server is needed: the bridges are registered during activation, before any
 * request. The base URI is unreachable on purpose, as in
 * {@link AtlasClientOfflineActivationIT}. Each test uses its own scope name so
 * the service look-up cannot pick up a bridge left by another test.
 * </p>
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class EagerStagesDefaultIT {

	/** Factory PID of the client (see {@code AtlasClientComponent.PID}). */
	private static final String PID = "org.eclipse.fennec.model.atlas.rest.client";

	/** Nothing listens on port 1, so no request can succeed. */
	private static final String UNREACHABLE_URI = "http://127.0.0.1:1/atlas/rest";

	private static final String ATLAS_SCOPE = "atlas.scope";
	private static final String ATLAS_STAGE = "atlas.stage";

	/** Long enough for SCR to activate the configuration and register the bridge. */
	private static final long SETTLE_TIMEOUT_MS = 15_000L;

	@Test
	public void withoutEagerStages_theRegistryIsStageFree(
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PID,
					name = "eager-default-stage", location = "?")) Configuration configuration,
			@InjectBundleContext BundleContext context) throws Exception {

		String scope = "stageFreeScope";
		configuration.update(props(scope, null));

		ServiceReference<EPackage.Registry> bridge = awaitBridge(context, scope);

		assertNull(bridge.getProperty(ATLAS_STAGE),
				"With no eager.stages configured the bridge must be stage-free, so that reads resolve this scope's "
						+ "own final stage server-side; any default stage name is wrong for a scope not using it");
	}

	@Test
	public void withExplicitEagerStages_theRegistryIsBoundToThatStage(
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PID,
					name = "eager-explicit-stage", location = "?")) Configuration configuration,
			@InjectBundleContext BundleContext context) throws Exception {

		String scope = "explicitStageScope";
		configuration.update(props(scope, new String[] { "draft" }));

		ServiceReference<EPackage.Registry> bridge = awaitBridge(context, scope);

		assertEquals("draft", bridge.getProperty(ATLAS_STAGE),
				"An explicitly configured stage must still bind the bridge to it — that is the point of the setting");
	}

	private static Hashtable<String, Object> props(String scope, String[] eagerStages) {
		Hashtable<String, Object> props = new Hashtable<>();
		props.put("base.uri", UNREACHABLE_URI);
		// LAZY + non-strict so activation completes without the server; the bridges are
		// registered regardless of mode, from eager.scopes alone.
		props.put("mode", "LAZY");
		props.put("mode.strict", Boolean.FALSE);
		props.put("scope.allow.list", new String[] { scope });
		props.put("eager.scopes", new String[] { scope });
		if (eagerStages != null) {
			props.put("eager.stages", eagerStages);
		}
		return props;
	}

	/** The one fetch-on-miss {@code EPackage.Registry} registered for this scope. */
	private static ServiceReference<EPackage.Registry> awaitBridge(BundleContext context, String scope)
			throws Exception {
		String filter = "(&(atlas.remote=true)(" + ATLAS_SCOPE + "=" + scope + "))";
		long deadline = System.currentTimeMillis() + SETTLE_TIMEOUT_MS;
		while (System.currentTimeMillis() < deadline) {
			Collection<ServiceReference<EPackage.Registry>> references = context
					.getServiceReferences(EPackage.Registry.class, filter);
			if (references.size() == 1) {
				return references.iterator().next();
			}
			if (references.size() > 1) {
				return fail("Expected one fetch-on-miss registry for scope " + scope + ", found " + references.size()
						+ " — one per configured stage: " + references.stream()
								.map(reference -> String.valueOf(reference.getProperty(ATLAS_STAGE))).toList());
			}
			Thread.sleep(100L);
		}
		return fail("No fetch-on-miss EPackage.Registry was registered for scope " + scope);
	}
}
