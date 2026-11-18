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

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Registry Writer</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Changes registry content on behalf of an external client (issue #386).
 * 
 * Every operation applies the rules a client is held to, in the order the REST API has always applied them: the target is resolved, the content's type is checked (never derived content, always a root type of the registry), the existing object is looked up, its read-only state and the caller's Precondition are checked, and only then is the workflow asked to write. Package operations additionally keep the package search index in line with what was written.
 * 
 * Every call is atomic, and its requests and results are self-contained: a request contains its content, a result contains its own copies of the metadata. Nothing a call takes or returns refers to objects outside it, so the service can be offered remotely.
 * 
 * Operations are synchronous. Refusals raise RegistryAccessException; the stage machinery's own refusals (StagePolicyException, StageOccupiedException, StageGateRefusedException) and a malformed stage (IllegalArgumentException) propagate unwrapped. Anything else is a failure of the server, not of the request.
 * <!-- end-model-doc -->
 *
 *
 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getRegistryWriter()
 * @model interface="true" abstract="true"
 * @generated
 */
@ProviderType
public interface RegistryWriter {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Stores a new instance, or, depending on ObjectWrite#getOnConflict(), replaces or skips one already at the address.
	 * @param write the request
	 * @return CREATED, UPDATED (overwrite) or SKIPPED, with the stored metadata
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	WriteResult createObject(ObjectWrite write);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Replaces the content of an existing instance. Content equal to the stored content is not written again.
	 * @param write the request; onConflict is ignored
	 * @return UPDATED, UNCHANGED or NOT_FOUND
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	WriteResult updateObject(ObjectWrite write);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Deletes an instance from a stage.
	 * @param address the instance
	 * @param force delete although a stage gate refuses it; the gate's findings are then recorded on the dependents it named
	 * @param precondition checked against the object before the operation; may be null
	 * @return DELETED or NOT_FOUND
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	WriteResult deleteObject(ObjectAddress address, boolean force, Precondition precondition);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Moves an instance from the stage of address to targetStage.
	 * @param address the instance in its source stage
	 * @param targetStage the stage to move it to
	 * @param overwrite replace a different object holding the id in the target stage
	 * @param precondition checked against the object before the operation; may be null
	 * @return TRANSITIONED, ALREADY_IN_TARGET or NOT_FOUND
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	WriteResult transitionObject(ObjectAddress address, String targetStage, boolean overwrite, Precondition precondition);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Stores a new package in a scope's schema registry, or, depending on PackageWrite#getOnConflict(), replaces or skips one with the same nsURI in the stage.
	 * @param write the request
	 * @return CREATED, UPDATED (overwrite) or SKIPPED, with the stored metadata
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	WriteResult createPackage(PackageWrite write);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Replaces the content of an existing package. Content equal to the stored content is not written again.
	 * @param write the request; onConflict, name and dcat are ignored
	 * @return UPDATED, UNCHANGED or NOT_FOUND
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	WriteResult updatePackage(PackageWrite write);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Changes metadata properties of a package without re-uploading it. Which properties a client may edit is the front end's decision; this operation only refuses inherited and read-only packages.
	 * @param update the package, the properties to set and the precondition
	 * @return UPDATED or NOT_FOUND
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	WriteResult updatePackageProperties(PackagePropertiesUpdate update);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Deletes a package from a stage.
	 * @param address the package
	 * @param force delete although a stage gate refuses it
	 * @param precondition checked against the object before the operation; may be null
	 * @return DELETED or NOT_FOUND
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	WriteResult deletePackage(PackageAddress address, boolean force, Precondition precondition);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Moves a package from one stage to another.
	 * @param scope the scope name
	 * @param stage the source stage
	 * @param idOrNsUri the package's object id, or, for clients that still send it, its nsURI
	 * @param targetStage the stage to move it to
	 * @param overwrite replace a different package holding the id in the target stage
	 * @param precondition checked against the object before the operation; may be null
	 * @return TRANSITIONED, ALREADY_IN_TARGET or NOT_FOUND
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	WriteResult transitionPackage(String scope, String stage, String idOrNsUri, String targetStage, boolean overwrite, Precondition precondition);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Writes several instances as one unit: all of them, or none.
	 * 
	 * Every item is first checked as createObject would check it; an item without an objectId (given or from an ID attribute), or whose address occurs twice in the batch, is refused as INVALID. If any item is refused, nothing is written and every refusal is reported. Otherwise the items are written in order. Content an OVERWRITE item would replace by equal content is left alone and reported UNCHANGED. Should a write still be refused, everything the batch wrote so far is undone (created instances are deleted, replaced ones get their previous content back) and the batch is reported as not applied. A failure that is not a refusal is rethrown once the batch has been undone.
	 * @param batch the instances, in the order they are written
	 * @return one result per item
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	BatchResult writeObjects(ObjectBatch batch);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Writes several packages as one unit: all of them, or none. Packages are matched by nsURI; otherwise the rules of writeObjects apply, each item checked as createPackage would check it.
	 * @param batch the packages, in the order they are written
	 * @return one result per item, identified by nsURI
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	BatchResult writePackages(PackageBatch batch);

} // RegistryWriter
