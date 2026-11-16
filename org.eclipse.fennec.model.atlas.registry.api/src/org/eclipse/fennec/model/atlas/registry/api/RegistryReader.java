/*
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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.registry.api;

import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.osgi.annotation.versioning.ProviderType;

/**
 * Reads registry content on behalf of an external client (issue #386).
 *
 * <p>
 * Lookups follow the scope hierarchy the way the workflow defines it: a stage listing or a
 * lookup by id also sees what a parent scope's final stage holds, marked read-only. An unknown
 * scope, or a scope without a schema registry for the package operations, is refused with
 * {@link RegistryAccessException}; a malformed stage raises {@link IllegalArgumentException}.
 * Absence of an object is not a failure: lookups answer an empty {@link Optional}, listings an
 * empty list.
 * </p>
 */
@ProviderType
public interface RegistryReader {

	/**
	 * @param scope the scope name
	 * @return the name of the scope's schema registry
	 * @throws RegistryAccessException {@link RegistryAccessException.Reason#NO_SCHEMA_REGISTRY} if
	 *                                 the scope has none
	 */
	String schemaRegistry(String scope);

	/**
	 * @param scope    the scope name
	 * @param registry the registry name
	 * @return the metadata of every object in every stage of the registry, including what parent
	 *         scopes' final stages hold
	 */
	List<ObjectMetadata> listAll(String scope, String registry);

	/**
	 * @param scope    the scope name
	 * @param registry the registry name
	 * @return the metadata of every object in the registry's final stage, including parent scopes'
	 */
	List<ObjectMetadata> listFinal(String scope, String registry);

	/**
	 * @param scope    the scope name
	 * @param registry the registry name
	 * @param stage    the stage name
	 * @return the metadata of every object in the stage
	 */
	List<ObjectMetadata> list(String scope, String registry, String stage);

	/**
	 * @param scope       the scope name
	 * @param registry    the registry name
	 * @param stage       the stage name
	 * @param namePattern a case-insensitive name filter; wildcards are allowed, but not as the
	 *                    first character
	 * @return the metadata of the matching objects in the stage
	 */
	List<ObjectMetadata> listByName(String scope, String registry, String stage, String namePattern);

	/**
	 * @param address the object
	 * @return its metadata, hierarchy-aware
	 */
	Optional<ObjectMetadata> metadata(ObjectAddress address);

	/**
	 * @param scope    the scope name
	 * @param registry the registry name
	 * @param objectId the object id
	 * @return the metadata of the object in the final stage, hierarchy-aware
	 */
	Optional<ObjectMetadata> finalMetadata(String scope, String registry, String objectId);

	/**
	 * @param address the object
	 * @return its content
	 */
	Optional<EObject> content(ObjectAddress address);

	/**
	 * @param scope    the scope name
	 * @param registry the registry name
	 * @param objectId the object id
	 * @return the content of the object in the final stage, hierarchy-aware
	 */
	Optional<EObject> finalContent(String scope, String registry, String objectId);

	/**
	 * @param address the package
	 * @return the metadata of the package with the address's nsURI in its stage of the scope's
	 *         schema registry, hierarchy-aware
	 */
	Optional<ObjectMetadata> packageMetadata(PackageAddress address);

	/**
	 * @param scope the scope name
	 * @param nsUri the package's namespace URI
	 * @return the metadata of the package in the final stage of the scope's schema registry,
	 *         hierarchy-aware
	 */
	Optional<ObjectMetadata> finalPackageMetadata(String scope, String nsUri);

	/**
	 * Searches the packages visible from a scope: its own and its ancestors'. Hits from an
	 * ancestor scope are marked read-only.
	 *
	 * @param scope  the scope name
	 * @param search the criteria
	 * @return one page of hits and the total number of hits
	 */
	PackageSearchResult searchPackages(String scope, PackageSearch search);
}
