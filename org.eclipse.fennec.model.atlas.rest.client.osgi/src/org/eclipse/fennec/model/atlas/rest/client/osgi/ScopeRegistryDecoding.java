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

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;
import org.eclipse.fennec.model.atlas.rest.client.api.DecodingRegistryProvider;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

/**
 * Decodes the objects this client reads against the registry chain the framework resolves a
 * scope and stage with (issue #347).
 * <p>
 * For every (scope, stage) in {@code eager.scopes × eager.stages} the component generates a
 * stock {@code EPackageRegistry} through ConfigAdmin: it aggregates the packages the client
 * published for that scope and stage and falls back to the fetch-on-miss bridge
 * ({@link AtlasScopedFetchOnMissRegistry}). A framework ResourceSet for the scope and stage
 * resolves through it. Handing the same registry to the client's object reads makes the read
 * objects bind those instances, instead of copies the client fetched for itself.
 * <p>
 * Only pairs the component set up itself are answered; for anything else, and while a
 * generated registry is not (yet) registered, this answers {@code null} and the client
 * decodes against its own registries.
 */
final class ScopeRegistryDecoding implements DecodingRegistryProvider, AutoCloseable {

	private static final Logger LOGGER = Logger.getLogger(ScopeRegistryDecoding.class.getName());

	private static final String GENERATED_REGISTRIES = "(" + EMFNamespaces.PROP_RESOURCE_SET_FACTORY_NAME + "=*)";

	private final BundleContext bundleContext;
	/** {@code rsf.name}s of the registry chains the component generated. */
	private final Set<String> generated = ConcurrentHashMap.newKeySet();
	/** The registries handed out, so their service use can be released on close. */
	private final Map<ServiceReference<EPackage.Registry>, EPackage.Registry> inUse = new ConcurrentHashMap<>();

	ScopeRegistryDecoding(BundleContext bundleContext) {
		this.bundleContext = Objects.requireNonNull(bundleContext, "bundleContext");
	}

	/** The component generated the registry chain of {@code scope}/{@code stage}. */
	void generated(String scope, String stage) {
		generated.add(AtlasEPackageRegistryConfigurator.rsfName(scope, stage));
	}

	@Override
	public EPackage.Registry registryFor(String scope, String stage) {
		String rsfName = AtlasEPackageRegistryConfigurator.rsfName(scope, stage);
		if (!generated.contains(rsfName)) {
			return null;
		}
		try {
			for (ServiceReference<EPackage.Registry> reference : bundleContext
					.getServiceReferences(EPackage.Registry.class, GENERATED_REGISTRIES)) {
				if (rsfName.equals(reference.getProperty(EMFNamespaces.PROP_RESOURCE_SET_FACTORY_NAME))) {
					EPackage.Registry registry = inUse.computeIfAbsent(reference, bundleContext::getService);
					if (registry != null) {
						return registry;
					}
				}
			}
		} catch (InvalidSyntaxException | IllegalStateException unavailable) {
			// A constant filter, or a context that is going away with the component.
			LOGGER.log(Level.FINE, unavailable, () -> "No registry chain for " + rsfName);
		}
		return null;
	}

	@Override
	public void close() {
		generated.clear();
		inUse.keySet().forEach(reference -> {
			try {
				bundleContext.ungetService(reference);
			} catch (IllegalStateException contextGone) {
				// the framework releases it with the bundle
			}
		});
		inUse.clear();
	}
}
