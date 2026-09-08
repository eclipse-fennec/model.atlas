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

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.wiring.BundleCapability;
import org.osgi.framework.wiring.BundleRevision;

/**
 * The nsURIs that bundles in this framework <em>declare</em> they provide as generated
 * EPackages, read from their {@code org.eclipse.emf.ecore.generated_package} capabilities.
 * <p>
 * Local-first suppression ({@link LocalFirstPublicationGate}) asks whether a package is
 * already provided locally. Answering that from live services alone is too late for a
 * generated package: its {@code EPackage} service appears only when its bundle activates,
 * and the damage is done before that. The generated factory's initialiser does
 * </p>
 *
 * <pre>
 * XyzFactory theXyzFactory = (XyzFactory) EPackage.Registry.INSTANCE.getEFactory(eNS_URI);
 * </pre>
 *
 * <p>
 * so a remote package that took the slot first makes it fail with a
 * {@code ClassCastException} - and even where the singleton is not mirrored, a remote
 * publication that is later withdrawn leaves a window in which loads resolve to the remote
 * copy, or in which two versions of one nsURI are registered at once and resolution is
 * ambiguous (issue #254).
 * </p>
 * <p>
 * A declared capability is visible as soon as the bundle is installed - no class loading, no
 * activation, no service - so it closes that window: the client never publishes an nsURI a
 * local bundle is going to provide. What it cannot see are locally <em>dynamic</em> models,
 * which declare nothing; those are still covered by {@link LocalServiceWatcher}, and they
 * carry no generated factory to break.
 * </p>
 * <p>
 * Bundles installed after the client are picked up by the {@link BundleListener}, which
 * reports changes through the {@link DeclarationListener} seam so the gate can withdraw a
 * publication it already made (or reconsider one it parked).
 * </p>
 *
 * @since 2026-09-07
 */
final class LocalGeneratedPackages implements BundleListener, AutoCloseable {

	/** The capability namespace EMF's generated packages are declared in. */
	static final String GENERATED_PACKAGE_NAMESPACE = "org.eclipse.emf.ecore.generated_package";

	/** The capability attribute carrying the package's nsURI. */
	static final String URI_ATTRIBUTE = "uri";

	private static final Logger LOGGER = Logger.getLogger(LocalGeneratedPackages.class.getName());

	/** What each bundle declares, by bundle id - so one bundle's removal cannot drop another's. */
	private final Map<Long, Set<String>> declarationsByBundle = new ConcurrentHashMap<>();

	private volatile BundleContext bundleContext;
	private volatile DeclarationListener listener = DeclarationListener.NONE;

	/** Told when an nsURI starts or stops being declared by some bundle in the framework. */
	@FunctionalInterface
	interface DeclarationListener {

		/** Nobody listening - the initial scan reports nothing to anyone. */
		DeclarationListener NONE = nsUri -> {
			/* no-op */ };

		/** An nsURI is now declared locally (a bundle carrying it was installed). */
		void declared(String nsUri);

		/** An nsURI is no longer declared by any bundle. */
		default void undeclared(String nsUri) {
			/* no-op by default: only the gate cares, and it re-checks presence anyway */ }
	}

	private LocalGeneratedPackages() {
	}

	/**
	 * Reads what every currently installed bundle declares. Reports nothing: this is the
	 * start-of-life picture, taken before anything has been published.
	 *
	 * @param bundleContext the context to read the bundles from
	 * @return the declarations, not yet tracking changes - see {@link #track}
	 */
	static LocalGeneratedPackages scan(BundleContext bundleContext) {
		Objects.requireNonNull(bundleContext, "bundleContext");
		LocalGeneratedPackages declarations = new LocalGeneratedPackages();
		for (Bundle bundle : bundleContext.getBundles()) {
			Set<String> declared = declaredBy(bundle);
			if (!declared.isEmpty()) {
				declarations.declarationsByBundle.put(bundle.getBundleId(), declared);
			}
		}
		LOGGER.log(Level.FINE, () -> "Local generated packages: " + declarations.all().size()
				+ " nsURI(s) declared by bundles in this framework");
		return declarations;
	}

	/**
	 * Starts following bundle lifecycle, reporting changes to {@code listener}.
	 *
	 * @param bundleContext the context to register the listener with
	 * @param listener      told when an nsURI starts or stops being declared
	 */
	void track(BundleContext bundleContext, DeclarationListener listener) {
		this.bundleContext = Objects.requireNonNull(bundleContext, "bundleContext");
		this.listener = Objects.requireNonNull(listener, "listener");
		bundleContext.addBundleListener(this);
	}

	/**
	 * Whether some bundle in this framework declares {@code nsUri} as a generated package.
	 *
	 * @param nsUri the namespace URI to ask about
	 * @return {@code true} if a local bundle provides (or will provide) it
	 */
	boolean declares(String nsUri) {
		if (nsUri == null || nsUri.isBlank()) {
			return false;
		}
		return declarationsByBundle.values().stream().anyMatch(declared -> declared.contains(nsUri));
	}

	@Override
	public void bundleChanged(BundleEvent event) {
		Bundle bundle = event.getBundle();
		switch (event.getType()) {
			case BundleEvent.INSTALLED, BundleEvent.RESOLVED, BundleEvent.UPDATED -> add(bundle);
			case BundleEvent.UNINSTALLED, BundleEvent.UNRESOLVED -> remove(bundle);
			default -> { /* STARTED/STOPPED change no declaration */ }
		}
	}

	private void add(Bundle bundle) {
		Set<String> declared = declaredBy(bundle);
		Set<String> previous = declared.isEmpty() ? declarationsByBundle.remove(bundle.getBundleId())
				: declarationsByBundle.put(bundle.getBundleId(), declared);
		for (String nsUri : declared) {
			if (previous == null || !previous.contains(nsUri)) {
				LOGGER.log(Level.INFO, () -> "Bundle " + bundle.getSymbolicName() + " declares the generated package "
						+ nsUri + "; the Atlas will not publish it");
				listener.declared(nsUri);
			}
		}
	}

	private void remove(Bundle bundle) {
		Set<String> gone = declarationsByBundle.remove(bundle.getBundleId());
		if (gone == null) {
			return;
		}
		for (String nsUri : gone) {
			if (!declares(nsUri)) { // another bundle may declare the same nsURI
				LOGGER.log(Level.INFO, () -> "No bundle declares the generated package " + nsUri + " any more");
				listener.undeclared(nsUri);
			}
		}
	}

	/** Every declared nsURI, for logging. */
	private Set<String> all() {
		Set<String> all = new LinkedHashSet<>();
		declarationsByBundle.values().forEach(all::addAll);
		return all;
	}

	/**
	 * The nsURIs one bundle declares. Read from the bundle's <em>declared</em> capabilities, so
	 * an installed-but-unresolved bundle counts too - what matters is what it will provide, not
	 * whether it is wired yet.
	 */
	private static Set<String> declaredBy(Bundle bundle) {
		BundleRevision revision = bundle.adapt(BundleRevision.class);
		if (revision == null) {
			return Set.of(); // uninstalled, or a bundle without a revision
		}
		Set<String> nsUris = new LinkedHashSet<>();
		for (BundleCapability capability : revision.getDeclaredCapabilities(GENERATED_PACKAGE_NAMESPACE)) {
			Object uri = capability.getAttributes().get(URI_ATTRIBUTE);
			if (uri instanceof String nsUri && !nsUri.isBlank()) {
				nsUris.add(nsUri);
			}
		}
		return nsUris;
	}

	@Override
	public void close() {
		BundleContext context = bundleContext;
		if (context == null) {
			return;
		}
		try {
			context.removeBundleListener(this);
		} catch (IllegalStateException stoppingBundle) {
			// The bundle is already stopping - the listener is gone anyway.
			LOGGER.log(Level.FINE, "Bundle listener already removed", stoppingBundle);
		}
	}
}
