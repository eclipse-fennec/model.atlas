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
import java.util.Map;

import org.osgi.annotation.versioning.ProviderType;

/**
 * Changes registry content on behalf of an external client (issue #386).
 *
 * <p>
 * Every operation applies the rules a client is held to, in the order the REST API has always
 * applied them: the target is resolved, the content's type is checked (never derived content,
 * always a root type of the registry), the existing object is looked up, its read-only state and
 * the caller's {@link Precondition} are checked, and only then is the workflow asked to write.
 * Package operations additionally keep the package search index in line with what was written.
 * </p>
 *
 * <p>
 * Operations are synchronous. Refusals raise {@link RegistryAccessException}; the stage
 * machinery's own refusals ({@code StagePolicyException}, {@code StageOccupiedException},
 * {@code StageGateRefusedException}) and a malformed stage ({@link IllegalArgumentException})
 * propagate unwrapped. Anything else is a failure of the server, not of the request.
 * </p>
 */
@ProviderType
public interface RegistryWriter {

	/**
	 * Stores a new instance, or — depending on {@link ObjectWrite#onConflict()} — replaces or
	 * skips one already at the address.
	 *
	 * @param write the request
	 * @return {@link WriteOutcome#CREATED}, {@link WriteOutcome#UPDATED} (overwrite) or
	 *         {@link WriteOutcome#SKIPPED}, with the stored metadata
	 */
	WriteResult createObject(ObjectWrite write);

	/**
	 * Replaces the content of an existing instance. Content equal to the stored content is not
	 * written again.
	 *
	 * @param write the request; {@link ObjectWrite#onConflict()} is ignored
	 * @return {@link WriteOutcome#UPDATED}, {@link WriteOutcome#UNCHANGED} or
	 *         {@link WriteOutcome#NOT_FOUND}
	 */
	WriteResult updateObject(ObjectWrite write);

	/**
	 * Deletes an instance from a stage.
	 *
	 * @param address      the instance
	 * @param force        delete although a stage gate refuses it; the gate's findings are then
	 *                     recorded on the dependents it named
	 * @param precondition checked against the instance before it is deleted; may be {@code null}
	 * @return {@link WriteOutcome#DELETED} or {@link WriteOutcome#NOT_FOUND}
	 */
	WriteResult deleteObject(ObjectAddress address, boolean force, Precondition precondition);

	/**
	 * Moves an instance from the stage of {@code address} to {@code targetStage}.
	 *
	 * @param address      the instance in its source stage
	 * @param targetStage  the stage to move it to
	 * @param overwrite    replace a different object holding the id in the target stage
	 * @param precondition checked against the instance before it moves; may be {@code null}
	 * @return {@link WriteOutcome#TRANSITIONED}, {@link WriteOutcome#ALREADY_IN_TARGET} or
	 *         {@link WriteOutcome#NOT_FOUND}
	 */
	WriteResult transitionObject(ObjectAddress address, String targetStage, boolean overwrite,
			Precondition precondition);

	/**
	 * Stores a new package in a scope's schema registry, or — depending on
	 * {@link PackageWrite#onConflict()} — replaces or skips one with the same nsURI in the stage.
	 *
	 * @param write the request
	 * @return {@link WriteOutcome#CREATED}, {@link WriteOutcome#UPDATED} (overwrite) or
	 *         {@link WriteOutcome#SKIPPED}, with the stored metadata
	 */
	WriteResult createPackage(PackageWrite write);

	/**
	 * Replaces the content of an existing package. Content equal to the stored content is not
	 * written again.
	 *
	 * @param write the request; {@link PackageWrite#onConflict()}, {@link PackageWrite#name()} and
	 *              {@link PackageWrite#dcat()} are ignored
	 * @return {@link WriteOutcome#UPDATED}, {@link WriteOutcome#UNCHANGED} or
	 *         {@link WriteOutcome#NOT_FOUND}
	 */
	WriteResult updatePackage(PackageWrite write);

	/**
	 * Changes metadata properties of a package without re-uploading it. Which properties a client
	 * may edit is the front end's decision; this operation only refuses inherited and read-only
	 * packages.
	 *
	 * @param address      the package
	 * @param properties   the properties to set
	 * @param precondition checked against the package's metadata; may be {@code null}
	 * @return {@link WriteOutcome#UPDATED} or {@link WriteOutcome#NOT_FOUND}
	 */
	WriteResult updatePackageProperties(PackageAddress address, Map<String, String> properties,
			Precondition precondition);

	/**
	 * Deletes a package from a stage.
	 *
	 * @param address      the package
	 * @param force        delete although a stage gate refuses it
	 * @param precondition checked against the package before it is deleted; may be {@code null}
	 * @return {@link WriteOutcome#DELETED} or {@link WriteOutcome#NOT_FOUND}
	 */
	WriteResult deletePackage(PackageAddress address, boolean force, Precondition precondition);

	/**
	 * Moves a package from one stage to another.
	 *
	 * @param scope        the scope name
	 * @param stage        the source stage
	 * @param idOrNsUri    the package's object id, or — for clients that still send it — its nsURI
	 * @param targetStage  the stage to move it to
	 * @param overwrite    replace a different package holding the id in the target stage
	 * @param precondition checked against the package before it moves; may be {@code null}
	 * @return {@link WriteOutcome#TRANSITIONED}, {@link WriteOutcome#ALREADY_IN_TARGET} or
	 *         {@link WriteOutcome#NOT_FOUND}
	 */
	WriteResult transitionPackage(String scope, String stage, String idOrNsUri, String targetStage,
			boolean overwrite, Precondition precondition);

	/**
	 * Writes several instances as one unit: all of them, or none.
	 *
	 * <p>
	 * Every item is first checked as {@link #createObject(ObjectWrite)} would check it; an item
	 * without an objectId, or whose address occurs twice in the batch, is refused as
	 * {@link RegistryAccessException.Reason#INVALID}. If any item is refused, nothing is written
	 * and every refusal is reported. Otherwise the items are written in order. Content an
	 * {@link ConflictPolicy#OVERWRITE} item would replace by equal content is left alone and
	 * reported {@link WriteOutcome#UNCHANGED}. Should a write still be refused, everything the
	 * batch wrote so far is undone — created instances are deleted, replaced ones get their
	 * previous content back — and the batch is reported as not applied. A failure that is not a
	 * refusal is rethrown once the batch has been undone.
	 * </p>
	 *
	 * @param writes the instances, in the order they are written
	 * @return one result per item
	 */
	BatchResult writeObjects(List<ObjectWrite> writes);

	/**
	 * Writes several packages as one unit: all of them, or none. Packages are matched by nsURI;
	 * otherwise the rules of {@link #writeObjects(List)} apply, each item checked as
	 * {@link #createPackage(PackageWrite)} would check it.
	 *
	 * @param writes the packages, in the order they are written
	 * @return one result per item, identified by nsURI
	 */
	BatchResult writePackages(List<PackageWrite> writes);
}
