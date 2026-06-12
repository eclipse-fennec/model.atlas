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
package org.eclipse.fennec.model.atlas.scope.api;

import java.util.List;
import java.util.Optional;

import java.util.stream.Stream;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Read Only Scope Service</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Read-only, final-stage view of the EObjects in a scope, mirroring the workflow API's registry dimension: the registry is a parameter on each operation, and reads resolve that registry's final stage, reading through to parent scopes' final stages when inheriting. It carries no stage parameter (per-stage access is a workflow concern) and no ObjectMetadata (a wire/storage concern), so it stays free of the workflow/management API. This is the contract a pure consumer of published models depends on (the validation service, a remote Atlas client, any downstream reader).
 * <!-- end-model-doc -->
 *
 *
 * @see org.eclipse.fennec.model.atlas.scope.api.ScopeApiPackage#getReadOnlyScopeService()
 * @model interface="true" abstract="true"
 * @generated
 */
@ProviderType
public interface ReadOnlyScopeService<T extends EObject> {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The scope this service reads from. @return the scope name (e.g. {@code jena}); never {@code null}
	 * <!-- end-model-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	String getScopeName();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Whether reads read through to parent scopes' final stages, so listings and look-ups reflect inherited content.
	 * @return {@code true} if the scope inherits from a parent scope
	 * <!-- end-model-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	boolean isInheritingFromParentScope();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Resolve a single object by its identifier from a registry's final-stage view.
	 * @param registry the registry within the scope (e.g. {@code schema})
	 * @param objectId the object identifier within that registry
	 * @return the resolved object, or empty if not visible (including, when inheriting, the parent scopes')
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.scope.api.Optional&lt;T&gt;" registryRequired="true" objectIdRequired="true"
	 * @generated
	 */
	Optional<T> get(String registry, String objectId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The identifiers of every object visible in a registry's final-stage view, without resolving their content.
	 * @param registry the registry within the scope
	 * @return the object ids (possibly empty); never {@code null}
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.scope.api.List&lt;org.eclipse.emf.ecore.EString&gt;" many="false" registryRequired="true"
	 * @generated
	 */
	List<String> listObjectIds(String registry);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Resolve every object visible in a registry's final-stage view; prefer stream() for large registries.
	 * @param registry the registry within the scope
	 * @return the resolved objects (possibly empty); never {@code null}
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.scope.api.List&lt;T&gt;" many="false" registryRequired="true"
	 * @generated
	 */
	List<T> listAll(String registry);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A lazy stream over the objects in a registry's final-stage view, resolving content as it is consumed.
	 * @param registry the registry within the scope
	 * @return a stream of resolved objects; never {@code null}
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.scope.api.Stream&lt;T&gt;" registryRequired="true"
	 * @generated
	 */
	Stream<T> stream(String registry);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A read-only descriptor of the scope this service reads from: its name, description, parent scope, and the registries it exposes (name + type, without the workflow stage/transition detail). @return the scope info; never {@code null}
	 * <!-- end-model-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	ScopeInfo getScopeInfo();

} // ReadOnlyScopeService
