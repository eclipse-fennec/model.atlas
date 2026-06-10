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

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.emf.osgi.configurator.EPackageConfigurator;
import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;
import org.eclipse.fennec.model.atlas.rest.client.api.AtlasProperties;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

/**
 * P3-7 — the OSGi glue for {@link LocalFirstPublicationGate}: watches <em>local</em>
 * {@code EPackage} / {@code EPackageConfigurator} services (i.e. those <strong>not</strong>
 * marked {@code atlas.remote}) and feeds their lifecycle to the gate keyed by nsURI.
 * <p>
 * The {@link ServiceListener} filter matches the two service interfaces and excludes
 * our own remote publications ({@code (!(atlas.remote=true))}), so the gate never reacts
 * to the services it itself registers. {@code REGISTERED}/{@code MODIFIED} →
 * {@link LocalFirstPublicationGate#onLocalAppeared(String)};
 * {@code UNREGISTERING}/{@code MODIFIED_ENDMATCH} →
 * {@link LocalFirstPublicationGate#onLocalDisappeared(String)}.
 * <p>
 * {@link #hasLocalService(BundleContext, String)} answers the gate's point-in-time
 * presence question, querying the registry by interface and matching the nsURI property
 * directly (so no user-supplied value ever goes into an LDAP filter).
 */
final class LocalServiceWatcher implements ServiceListener, AutoCloseable {

	private static final Logger LOGGER = Logger.getLogger(LocalServiceWatcher.class.getName());

	/** Matches local EPackage/EPackageConfigurator services, excluding our own remote ones. */
	private static final String LISTENER_FILTER = "(&(|(objectClass=" + EPackage.class.getName() + ")(objectClass="
			+ EPackageConfigurator.class.getName() + "))(!(" + AtlasProperties.ATLAS_REMOTE + "=true)))";

	private final BundleContext bundleContext;
	private final LocalFirstPublicationGate gate;

	private LocalServiceWatcher(BundleContext bundleContext, LocalFirstPublicationGate gate) {
		this.bundleContext = bundleContext;
		this.gate = gate;
	}

	/** Create the watcher and register its {@link ServiceListener}. */
	static LocalServiceWatcher register(BundleContext bundleContext, LocalFirstPublicationGate gate) {
		Objects.requireNonNull(bundleContext, "bundleContext");
		Objects.requireNonNull(gate, "gate");
		LocalServiceWatcher watcher = new LocalServiceWatcher(bundleContext, gate);
		try {
			bundleContext.addServiceListener(watcher, LISTENER_FILTER);
		} catch (org.osgi.framework.InvalidSyntaxException e) {
			// LISTENER_FILTER is a constant with no user input, so this cannot happen.
			throw new IllegalStateException("Invalid local-first service filter", e);
		}
		return watcher;
	}

	@Override
	public void serviceChanged(ServiceEvent event) {
		ServiceReference<?> ref = event.getServiceReference();
		Object nsUri = ref.getProperty(EMFNamespaces.EMF_MODEL_NSURI);
		if (!(nsUri instanceof String) || ((String) nsUri).isBlank()) {
			return; // not an nsURI-bearing model service we can match
		}
		String ns = (String) nsUri;
		switch (event.getType()) {
			case ServiceEvent.REGISTERED, ServiceEvent.MODIFIED -> gate.onLocalAppeared(ns);
			case ServiceEvent.UNREGISTERING, ServiceEvent.MODIFIED_ENDMATCH -> gate.onLocalDisappeared(ns);
			default -> { /* ignore */ }
		}
	}

	@Override
	public void close() {
		try {
			bundleContext.removeServiceListener(this);
		} catch (IllegalStateException stoppingBundle) {
			// The bundle is already stopping — the listener is gone anyway.
			LOGGER.log(Level.FINE, "Service listener already removed", stoppingBundle);
		}
	}

	/**
	 * Whether a local (non-{@code atlas.remote}) {@code EPackage} or {@code EPackageConfigurator}
	 * service currently provides {@code nsUri}. Queried by interface, matching the nsURI and
	 * {@code atlas.remote} properties in code, so {@code nsUri} never enters an LDAP filter.
	 */
	static boolean hasLocalService(BundleContext bundleContext, String nsUri) {
		return matches(bundleContext, EPackage.class.getName(), nsUri)
				|| matches(bundleContext, EPackageConfigurator.class.getName(), nsUri);
	}

	private static boolean matches(BundleContext bundleContext, String interfaceName, String nsUri) {
		ServiceReference<?>[] refs;
		try {
			refs = bundleContext.getServiceReferences(interfaceName, null);
		} catch (org.osgi.framework.InvalidSyntaxException e) {
			throw new IllegalStateException("Unexpected filter syntax error", e); // null filter
		}
		if (refs == null) {
			return false;
		}
		for (ServiceReference<?> ref : refs) {
			if (nsUri.equals(ref.getProperty(EMFNamespaces.EMF_MODEL_NSURI)) && !isRemote(ref)) {
				return true;
			}
		}
		return false;
	}

	private static boolean isRemote(ServiceReference<?> ref) {
		return Boolean.parseBoolean(String.valueOf(ref.getProperty(AtlasProperties.ATLAS_REMOTE)));
	}

	/** A local model service's identity for the P3-8 startup version check. */
	record LocalModel(String nsUri, String version) {
	}

	/**
	 * Every distinct local (non-{@code atlas.remote}) model nsURI currently registered, with its
	 * {@code emf.version} if present. Used by the {@code force.remote} startup check (P3-8) to decide
	 * which locals the Atlas can supersede. De-duplicated by nsURI (a package may expose both an
	 * {@code EPackage} and an {@code EPackageConfigurator} service).
	 */
	static java.util.Collection<LocalModel> localModels(BundleContext bundleContext) {
		Map<String, LocalModel> byNsUri = new LinkedHashMap<>();
		collectInto(bundleContext, EPackage.class.getName(), byNsUri);
		collectInto(bundleContext, EPackageConfigurator.class.getName(), byNsUri);
		return byNsUri.values();
	}

	private static void collectInto(BundleContext bundleContext, String interfaceName, Map<String, LocalModel> out) {
		ServiceReference<?>[] refs;
		try {
			refs = bundleContext.getServiceReferences(interfaceName, null);
		} catch (org.osgi.framework.InvalidSyntaxException e) {
			throw new IllegalStateException("Unexpected filter syntax error", e); // null filter
		}
		if (refs == null) {
			return;
		}
		for (ServiceReference<?> ref : refs) {
			if (isRemote(ref)) {
				continue;
			}
			Object nsUri = ref.getProperty(EMFNamespaces.EMF_MODEL_NSURI);
			if (nsUri instanceof String ns && !ns.isBlank()) {
				Object version = ref.getProperty(EMFNamespaces.EMF_MODEL_VERSION);
				out.putIfAbsent(ns, new LocalModel(ns, version instanceof String v ? v : null));
			}
		}
	}
}
