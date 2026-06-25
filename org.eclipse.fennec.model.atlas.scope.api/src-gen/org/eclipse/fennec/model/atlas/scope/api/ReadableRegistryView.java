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
 * A representation of the model object '<em><b>Readable Registry View</b></em>'.
 * <!-- end-user-doc -->
 *
 *
 * @see org.eclipse.fennec.model.atlas.scope.api.ScopeApiPackage#getReadableRegistryView()
 * @model interface="true" abstract="true"
 * @generated
 */
@ProviderType
public interface ReadableRegistryView<T extends EObject> {
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
	 * The registry name
	 * <!-- end-model-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	String getRegistryName();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * the bound stage, or `null` for a
	 * final-stage view (no stage was requested).
	 * <!-- end-model-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	String getStageName();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Resolve a single object by its identifier
	 * @param objectId the object identifier within that registry
	 * @return the resolved object, or empty if not visible (including, when inheriting, the parent scopes')
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.scope.api.Optional&lt;T&gt;" objectIdRequired="true"
	 * @generated
	 */
	Optional<T> get(String objectId);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The identifiers of every object visible in this registry view, without resolving their content.
	 * @return the object ids (possibly empty); never {@code null}
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.scope.api.List&lt;org.eclipse.emf.ecore.EString&gt;" many="false"
	 * @generated
	 */
	List<String> listObjectIds();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Resolve every object visible in this registry view; prefer stream() for large registries.
	 * @return the resolved objects (possibly empty); never {@code null}
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.scope.api.List&lt;T&gt;" many="false"
	 * @generated
	 */
	List<T> listAll();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A lazy stream over the objects in a registry's view, resolving content as it is consumed.
	 * @return a stream of resolved objects; never {@code null}
	 * <!-- end-model-doc -->
	 * @model dataType="org.eclipse.fennec.model.atlas.scope.api.Stream&lt;T&gt;"
	 * @generated
	 */
	Stream<T> stream();

} // ReadableRegistryView
