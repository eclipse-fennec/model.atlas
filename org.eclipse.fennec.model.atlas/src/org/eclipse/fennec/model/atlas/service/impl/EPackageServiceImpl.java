/**
 * Copyright (c) 2012 - 2025 Data In Motion and others.
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
package org.eclipse.fennec.model.atlas.service.impl;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.model.atlas.emf.common.configurator.DynamicEPackageConfigurator;
import org.eclipse.fennec.model.atlas.emf.common.ecore.EClassResolvingDynamicEFactory;
import org.eclipse.fennec.model.atlas.service.EPackageService;
import org.gecko.emf.osgi.configurator.EPackageConfigurator;
import org.gecko.emf.osgi.constants.EMFNamespaces;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Service implementation for managing EPackage registration and lifecycle.
 *
 * @author Claude Code
 * @since 1.0
 */
@Component(service = EPackageService.class, scope = ServiceScope.SINGLETON)
public class EPackageServiceImpl implements EPackageService {

	private static final Logger LOG = Logger.getLogger(EPackageServiceImpl.class.getName());

	@Reference
	private ResourceSet resourceSet;

	private BundleContext bundleContext;

	// Track OSGi service registrations by nsUri for cleanup
	private final Map<String, List<ServiceRegistration<?>>> serviceRegistrations = new ConcurrentHashMap<>();
	private final Lock lock = new ReentrantLock();

	@Activate
	public void activate(BundleContext bundleContext) {
		this.bundleContext = bundleContext;
		LOG.log(Level.INFO, "EPackageService activated");
	}

	@Deactivate
	public void deactivate() {
		lock.lock();
		try {
			// Unregister all OSGi services on deactivation
			serviceRegistrations.values().forEach(registrations ->
				registrations.forEach(ServiceRegistration::unregister)
			);
			serviceRegistrations.clear();
			LOG.log(Level.INFO, "EPackageService deactivated");
		} finally {
			lock.unlock();
		}
	}

	@Override
	public Optional<EPackage> registerEPackage(EPackage ePackage) {
		validateEPackage(ePackage);

		lock.lock();
		try {
			String nsUri = ePackage.getNsURI();

			// Check if already exists
			if (exists(nsUri)) {
				LOG.log(Level.WARNING, "EPackage with nsUri already exists: {0}", nsUri);
				return Optional.empty();
			}

			// Set dynamic EFactory for runtime instantiation
			ePackage.setEFactoryInstance(new EClassResolvingDynamicEFactory());

			// Register in ResourceSet PackageRegistry
			resourceSet.getPackageRegistry().put(nsUri, ePackage);

			// Register in global registry (needed for QVT and other tools)
			EPackageRegistryImpl.INSTANCE.put(nsUri, ePackage);

			// Register as OSGi services
			List<ServiceRegistration<?>> registrations = new ArrayList<>();

			// Register EPackageConfigurator
			DynamicEPackageConfigurator configurator = new DynamicEPackageConfigurator(ePackage);
			ServiceRegistration<EPackageConfigurator> configuratorReg = bundleContext.registerService(
				EPackageConfigurator.class,
				configurator,
				createServiceProperties(ePackage)
			);
			registrations.add(configuratorReg);

			// Register EPackage itself as service
			ServiceRegistration<EPackage> packageReg = bundleContext.registerService(
				EPackage.class,
				ePackage,
				createServiceProperties(ePackage)
			);
			registrations.add(packageReg);

			// Track registrations for cleanup
			serviceRegistrations.put(nsUri, registrations);

			LOG.log(Level.INFO, "EPackage registered successfully: {0}", nsUri);
			return Optional.of(ePackage);

		} finally {
			lock.unlock();
		}
	}

	@Override
	public Optional<EPackage> getEPackage(String nsUri) {
		if (nsUri == null || nsUri.isEmpty()) {
			return Optional.empty();
		}

		EPackage ePackage = resourceSet.getPackageRegistry().getEPackage(nsUri);
		return Optional.ofNullable(ePackage);
	}

	@Override
	public List<EPackage> listEPackages() {
		return resourceSet.getPackageRegistry().values().stream()
			.filter(EPackage.class::isInstance)
			.map(EPackage.class::cast)
			.toList();
	}

	@Override
	public Optional<EPackage> updateEPackage(String nsUri, EPackage ePackage) {
		validateEPackage(ePackage);

		if (!ePackage.getNsURI().equals(nsUri)) {
			throw new IllegalArgumentException("nsUri parameter does not match EPackage nsUri");
		}

		lock.lock();
		try {
			// Check if exists
			if (!exists(nsUri)) {
				LOG.log(Level.WARNING, "EPackage not found for update: {0}", nsUri);
				return Optional.empty();
			}

			// Unregister the old package
			unregisterInternal(nsUri);

			// Register the new package
			return registerEPackage(ePackage);

		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean unregisterEPackage(String nsUri) {
		if (nsUri == null || nsUri.isEmpty()) {
			return false;
		}

		lock.lock();
		try {
			return unregisterInternal(nsUri);
		} finally {
			lock.unlock();
		}
	}

	@Override
	public boolean exists(String nsUri) {
		if (nsUri == null || nsUri.isEmpty()) {
			return false;
		}
		return resourceSet.getPackageRegistry().containsKey(nsUri);
	}

	/**
	 * Internal method to unregister an EPackage. Must be called within lock.
	 */
	private boolean unregisterInternal(String nsUri) {
		// Check if exists
		if (!exists(nsUri)) {
			return false;
		}

		// Unregister OSGi services
		List<ServiceRegistration<?>> registrations = serviceRegistrations.remove(nsUri);
		if (registrations != null) {
			registrations.forEach(ServiceRegistration::unregister);
		}

		// Remove from registries
		resourceSet.getPackageRegistry().remove(nsUri);
		EPackageRegistryImpl.INSTANCE.remove(nsUri);

		LOG.log(Level.INFO, "EPackage unregistered successfully: {0}", nsUri);
		return true;
	}

	/**
	 * Validate that an EPackage is valid for registration.
	 */
	private void validateEPackage(EPackage ePackage) {
		if (ePackage == null) {
			throw new IllegalArgumentException("EPackage cannot be null");
		}
		if (ePackage.getNsURI() == null || ePackage.getNsURI().isEmpty()) {
			throw new IllegalArgumentException("EPackage must have a non-empty nsUri");
		}
	}

	/**
	 * Create OSGi service properties for an EPackage.
	 */
	private Hashtable<String, String> createServiceProperties(EPackage ePackage) {
		Hashtable<String, String> properties = new Hashtable<>();
		properties.put(EMFNamespaces.EMF_MODEL_NAME, ePackage.getName() != null ? ePackage.getName() : "unknown");
		properties.put(EMFNamespaces.EMF_MODEL_NSURI, ePackage.getNsURI());
		properties.put(EMFNamespaces.EMF_MODEL_REGISTRATION, EMFNamespaces.MODEL_REGISTRATION_DYNAMIC);
		return properties;
	}
}
