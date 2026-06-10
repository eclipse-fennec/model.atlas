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

import java.util.Hashtable;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EFactory;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.emf.osgi.configurator.EPackageConfigurator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;

/**
 * P3-3 — publishes a remotely fetched EPackage to the OSGi service registry as the
 * trio {@code emf.osgi} expects, all with the <em>identical</em> property set from
 * {@link RemoteEPackageConfigurator#getServiceProperties()}:
 * <ul>
 * <li>an {@link EPackageConfigurator} (picked up by {@code emf.osgi}'s
 * {@code DefaultEPackageRegistryComponent}, which populates {@code EPackage.Registry});</li>
 * <li>the {@link EPackage} itself;</li>
 * <li>its {@link EFactory}.</li>
 * </ul>
 * Registrations are tracked per nsURI so they can be revoked individually (drift
 * substitution, P3-9) or all at once on client shutdown. Publication is idempotent
 * and atomic per nsURI.
 * <p>
 * This is the mechanism only; <em>when</em> packages are published (EAGER pre-fetch,
 * LAZY on demand, HYBRID) is decided by P3-4 … P3-6, and local-first suppression by
 * P3-7.
 * <p>
 * P3-8: when {@code force.remote=true} the trio is registered with a high
 * {@code service.ranking} ({@code serviceRanking} &gt; 0), so consumers doing a direct
 * service lookup prefer the remote over a same-nsURI local one. (Honest caveat:
 * {@code emf.osgi}'s {@code DefaultEPackageRegistryComponent} populates
 * {@code EPackage.Registry} in <em>bind order</em>, not by ranking, so registry-level
 * consumers remain bind-order-dependent — see the P3-8 note / design doc.)
 * <p>
 * P3-11: when {@code register.in.global.registry=true} a non-null {@code globalRegistry}
 * (the EMF {@code EPackage.Registry.INSTANCE}) is supplied; every publish/republish also
 * puts the package into it and every unpublish removes it, under the same per-nsURI lock,
 * so legacy code reaching the EMF singleton sees the same package — kept consistent with
 * the OSGi service through drift swaps. Default is {@code null} (the singleton is left
 * untouched).
 */
final class RemoteEPackagePublisher {

	private static final Logger LOGGER = Logger.getLogger(RemoteEPackagePublisher.class.getName());

	private final BundleContext bundleContext;
	private final String baseUri;
	private final int serviceRanking;
	private final transient EPackage.Registry globalRegistry;
	private final Map<String, Registration> published = new ConcurrentHashMap<>();
	/** P3-9: serialises publish/republish/unpublish of the same nsURI (parallel for distinct nsURIs). */
	private final NsUriLocks locks = new NsUriLocks();

	RemoteEPackagePublisher(BundleContext bundleContext, String baseUri) {
		this(bundleContext, baseUri, 0, null);
	}

	RemoteEPackagePublisher(BundleContext bundleContext, String baseUri, int serviceRanking) {
		this(bundleContext, baseUri, serviceRanking, null);
	}

	/**
	 * @param serviceRanking the {@code service.ranking} to stamp on every published service
	 *                       (0 = omit / framework default; a positive value for {@code force.remote})
	 * @param globalRegistry the EMF singleton to mirror published packages into (P3-11), or
	 *                       {@code null} to leave it untouched
	 */
	RemoteEPackagePublisher(BundleContext bundleContext, String baseUri, int serviceRanking,
			EPackage.Registry globalRegistry) {
		this.bundleContext = Objects.requireNonNull(bundleContext, "bundleContext");
		this.baseUri = Objects.requireNonNull(baseUri, "baseUri");
		this.serviceRanking = serviceRanking;
		this.globalRegistry = globalRegistry;
	}

	/**
	 * Publish {@code ePackage} as the EPackageConfigurator/EPackage/EFactory trio.
	 * No-op (returns {@code false}) when the nsURI is blank or already published.
	 *
	 * @param ePackage the fetched package (its {@code eResource} URI should already be its nsURI)
	 * @param scope    the Atlas scope it came from
	 * @param stage    the stage it was fetched from
	 * @param version  the model version, or {@code null} to stamp the default
	 * @return {@code true} if it was newly published
	 */
	boolean publish(EPackage ePackage, String scope, String stage, String version) {
		Objects.requireNonNull(ePackage, "ePackage");
		String nsUri = ePackage.getNsURI();
		if (nsUri == null || nsUri.isBlank()) {
			LOGGER.warning("Cannot publish an EPackage with a null/blank nsURI");
			return false;
		}
		boolean[] created = { false };
		locks.run(nsUri, () -> {
			if (published.containsKey(nsUri)) {
				return; // already published — idempotent
			}
			published.put(nsUri, register(new RemoteEPackageConfigurator(ePackage, scope, stage, version, baseUri)));
			mirrorToGlobal(nsUri, ePackage);
			created[0] = true;
		});
		if (created[0]) {
			LOGGER.log(Level.INFO, () -> "Published remote EPackage " + nsUri + " (scope=" + scope + ", stage=" + stage + ")");
		}
		return created[0];
	}

	/**
	 * P3-9 — atomically swap the published trio for {@code nsUri} to a new package (a drift
	 * substitution). Under the per-nsURI lock: register the new trio, flip the tracked
	 * registration (so {@link #publishedEPackage(String)} transitions old→new in a single
	 * step, never {@code null}), then revoke the old trio. If nothing was published yet it
	 * behaves like {@link #publish}.
	 * <p>
	 * The framework {@code EPackage.Registry} settles by {@code emf.osgi}'s bind order during
	 * the swap (not under our control — the documented aggregator caveat), so the never-null
	 * guarantee is for consumers going through our delegating registry, which reads
	 * {@link #publishedEPackage(String)}.
	 *
	 * @return {@code true} if it replaced an existing publication
	 */
	boolean republish(EPackage ePackage, String scope, String stage, String version) {
		Objects.requireNonNull(ePackage, "ePackage");
		String nsUri = ePackage.getNsURI();
		if (nsUri == null || nsUri.isBlank()) {
			LOGGER.warning("Cannot republish an EPackage with a null/blank nsURI");
			return false;
		}
		boolean[] replaced = { false };
		locks.run(nsUri, () -> {
			Registration old = published.get(nsUri);
			Registration fresh = register(new RemoteEPackageConfigurator(ePackage, scope, stage, version, baseUri));
			published.put(nsUri, fresh);
			mirrorToGlobal(nsUri, ePackage); // replaces the singleton entry in step with the service swap
			if (old != null) {
				old.unregisterAll();
				replaced[0] = true;
			}
		});
		LOGGER.log(Level.INFO, () -> (replaced[0] ? "Re-published" : "Published") + " remote EPackage " + nsUri
				+ " (scope=" + scope + ", stage=" + stage + ")");
		return replaced[0];
	}

	/** Revoke the trio for {@code nsUri}; {@code false} if it was not published. */
	boolean unpublish(String nsUri) {
		if (nsUri == null) {
			return false;
		}
		boolean[] removed = { false };
		locks.run(nsUri, () -> {
			Registration registration = published.remove(nsUri);
			if (registration != null) {
				registration.unregisterAll();
				removeFromGlobal(nsUri);
				removed[0] = true;
			}
		});
		if (removed[0]) {
			LOGGER.log(Level.INFO, () -> "Unpublished remote EPackage " + nsUri);
		}
		return removed[0];
	}

	/**
	 * The EPackage currently published for {@code nsUri}, or {@code null} if none. Read
	 * atomically from the tracking map, so a concurrent {@link #republish} swap is observed
	 * as either the old or the new package, never {@code null} mid-swap — the basis for the
	 * delegating registry's never-null guarantee (P3-9).
	 */
	EPackage publishedEPackage(String nsUri) {
		if (nsUri == null) {
			return null;
		}
		Registration registration = published.get(nsUri);
		return registration == null ? null : registration.ePackage();
	}

	boolean isPublished(String nsUri) {
		return nsUri != null && published.containsKey(nsUri);
	}

	Set<String> publishedNsUris() {
		return Set.copyOf(published.keySet());
	}

	/** Revoke every publication; called on client shutdown. */
	void unpublishAll() {
		published.forEach((nsUri, registration) -> {
			registration.unregisterAll();
			removeFromGlobal(nsUri); // P3-11: don't leak our entries in EPackage.Registry.INSTANCE
		});
		published.clear();
	}

	/** P3-11: mirror a publication into the EMF singleton when one was supplied. */
	private void mirrorToGlobal(String nsUri, EPackage ePackage) {
		if (globalRegistry != null) {
			globalRegistry.put(nsUri, ePackage);
		}
	}

	/** P3-11: drop our mirrored entry from the EMF singleton. */
	private void removeFromGlobal(String nsUri) {
		if (globalRegistry != null) {
			globalRegistry.remove(nsUri);
		}
	}

	private Registration register(RemoteEPackageConfigurator configurator) {
		EPackage ePackage = configurator.getEPackage();
		// One identical, immutable-by-convention property set for all three services.
		Map<String, Object> properties = configurator.getServiceProperties();
		if (serviceRanking != 0) {
			// P3-8 force.remote: outrank a same-nsURI local service for direct lookups.
			properties.put(Constants.SERVICE_RANKING, serviceRanking);
		}

		ServiceRegistration<EPackageConfigurator> configuratorReg = bundleContext
				.registerService(EPackageConfigurator.class, configurator, new Hashtable<>(properties));
		ServiceRegistration<?> ePackageReg = bundleContext.registerService(
				new String[] { ePackage.getClass().getName(), EPackage.class.getName() }, ePackage,
				new Hashtable<>(properties));

		ServiceRegistration<?> eFactoryReg = null;
		EFactory eFactory = ePackage.getEFactoryInstance();
		if (eFactory != null) {
			eFactoryReg = bundleContext.registerService(
					new String[] { eFactory.getClass().getName(), EFactory.class.getName() }, eFactory,
					new Hashtable<>(properties));
		} else {
			LOGGER.warning(() -> "No EFactory for EPackage " + ePackage.getNsURI() + "; publishing without it");
		}
		return new Registration(ePackage, configuratorReg, ePackageReg, eFactoryReg);
	}

	/** The published EPackage and its three service registrations. */
	private static final class Registration {

		private final EPackage ePackage;
		private final ServiceRegistration<EPackageConfigurator> configuratorReg;
		private final ServiceRegistration<?> ePackageReg;
		private final ServiceRegistration<?> eFactoryReg;

		Registration(EPackage ePackage, ServiceRegistration<EPackageConfigurator> configuratorReg,
				ServiceRegistration<?> ePackageReg, ServiceRegistration<?> eFactoryReg) {
			this.ePackage = ePackage;
			this.configuratorReg = configuratorReg;
			this.ePackageReg = ePackageReg;
			this.eFactoryReg = eFactoryReg;
		}

		EPackage ePackage() {
			return ePackage;
		}

		void unregisterAll() {
			// Reverse order of registration; the configurator (which populates the
			// registry) goes last so the package/factory are gone before it is removed.
			unregisterQuietly(eFactoryReg);
			unregisterQuietly(ePackageReg);
			unregisterQuietly(configuratorReg);
		}

		private static void unregisterQuietly(ServiceRegistration<?> registration) {
			if (registration == null) {
				return;
			}
			try {
				registration.unregister();
			} catch (IllegalStateException alreadyGone) {
				// Already unregistered (e.g. during framework shutdown) — nothing to do.
				LOGGER.log(Level.FINE, "Service already unregistered", alreadyGone);
			}
		}
	}
}
