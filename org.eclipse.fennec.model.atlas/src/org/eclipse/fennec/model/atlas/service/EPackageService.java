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
package org.eclipse.fennec.model.atlas.service;

import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EPackage;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Service for managing EPackage registration and lifecycle.
 *
 * @author Claude Code
 * @since 1.0
 */
@ProviderType
public interface EPackageService {

	/**
	 * Register a new EPackage in the system.
	 *
	 * @param ePackage the EPackage to register
	 * @return Optional containing the registered package, or empty if nsUri already exists
	 * @throws IllegalArgumentException if EPackage is invalid (null or missing nsUri)
	 */
	Optional<EPackage> registerEPackage(EPackage ePackage);

	/**
	 * Get an EPackage by its namespace URI.
	 *
	 * @param nsUri the namespace URI of the EPackage
	 * @return Optional containing the EPackage, or empty if not found
	 */
	Optional<EPackage> getEPackage(String nsUri);

	/**
	 * List all registered EPackages.
	 *
	 * @return List of all registered EPackages
	 */
	List<EPackage> listEPackages();

	/**
	 * Update an existing EPackage.
	 *
	 * @param nsUri the namespace URI of the EPackage to update
	 * @param ePackage the updated EPackage
	 * @return Optional containing the updated package, or empty if not found
	 * @throws IllegalArgumentException if EPackage is invalid or nsUri mismatch
	 */
	Optional<EPackage> updateEPackage(String nsUri, EPackage ePackage);

	/**
	 * Unregister an EPackage from the system.
	 *
	 * @param nsUri the namespace URI of the EPackage to unregister
	 * @return true if the EPackage was registered and removed, false if it didn't exist
	 */
	boolean unregisterEPackage(String nsUri);

	/**
	 * Check if an EPackage with the given namespace URI exists.
	 *
	 * @param nsUri the namespace URI to check
	 * @return true if an EPackage with this nsUri is registered
	 */
	boolean exists(String nsUri);
}
