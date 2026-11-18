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

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Registry Reader</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Reads registry content on behalf of an external client (issue #386).
 * 
 * Lookups follow the scope hierarchy the way the workflow defines it: a stage listing or a lookup by id also sees what a parent scope's final stage holds, marked read-only. An unknown scope, or a scope without a schema registry for the package operations, is refused with RegistryAccessException; a malformed stage raises IllegalArgumentException. Absence of an object is not a failure: lookups answer null, listings an empty list.
 * 
 * Every result is self-contained: content is returned as the root of its own containment tree, metadata as objects no other result shares.
 * <!-- end-model-doc -->
 *
 *
 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getRegistryReader()
 * @model interface="true" abstract="true"
 * @generated
 */
@ProviderType
public interface RegistryReader {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * @return the name of the scope's schema registry; refused with NO_SCHEMA_REGISTRY if the scope has none
	 * @param scope the scope name
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	String schemaRegistry(String scope);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * @return the metadata of every object in every stage of the registry, including what parent scopes' final stages hold
	 * @param scope the scope name
	 * @param registry the registry name
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	EList<ObjectMetadata> listAll(String scope, String registry);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * @return the metadata of every object in the registry's final stage, including parent scopes'
	 * @param scope the scope name
	 * @param registry the registry name
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	EList<ObjectMetadata> listFinal(String scope, String registry);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * @return the metadata of every object in the stage
	 * @param scope the scope name
	 * @param registry the registry name
	 * @param stage the stage name
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	EList<ObjectMetadata> list(String scope, String registry, String stage);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * @return the metadata of the matching objects in the stage
	 * @param scope the scope name
	 * @param registry the registry name
	 * @param stage the stage name
	 * @param namePattern a case-insensitive name filter; wildcards are allowed, but not as the first character
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	EList<ObjectMetadata> listByName(String scope, String registry, String stage, String namePattern);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * @return its metadata, hierarchy-aware, or null
	 * @param address the object
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	ObjectMetadata metadata(ObjectAddress address);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * @return the metadata of the object in the final stage, hierarchy-aware, or null
	 * @param scope the scope name
	 * @param registry the registry name
	 * @param objectId the object id
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	ObjectMetadata finalMetadata(String scope, String registry, String objectId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * @return its content, or null
	 * @param address the object
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	EObject content(ObjectAddress address);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * @return the content of the object in the final stage, hierarchy-aware, or null
	 * @param scope the scope name
	 * @param registry the registry name
	 * @param objectId the object id
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	EObject finalContent(String scope, String registry, String objectId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * @return the metadata of the package with the address's nsURI in its stage of the scope's schema registry, hierarchy-aware, or null
	 * @param address the package
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	ObjectMetadata packageMetadata(PackageAddress address);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * @return the metadata of the package in the final stage of the scope's schema registry, hierarchy-aware, or null
	 * @param scope the scope name
	 * @param nsUri the package's namespace URI
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	ObjectMetadata finalPackageMetadata(String scope, String nsUri);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Searches the packages visible from a scope: its own and its ancestors'. Hits from an ancestor scope are marked read-only.
	 * @param scope the scope name
	 * @param search the criteria
	 * @return one page of hits and the total number of hits
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	PackageSearchResult searchPackages(String scope, PackageSearch search);

} // RegistryReader
