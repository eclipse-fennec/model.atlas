/*
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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.model.scope;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Scope</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.model.scope.Scope#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.model.scope.Scope#getType <em>Type</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.model.scope.Scope#getParentScope <em>Parent Scope</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.model.scope.Scope#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.model.scope.Scope#getLinks <em>Links</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.model.scope.Scope#getStages <em>Stages</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.model.scope.Scope#getFinalStage <em>Final Stage</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.model.scope.Scope#getWritableStages <em>Writable Stages</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.model.scope.ScopePackage#getScope()
 * @model
 * @generated
 */
@ProviderType
public interface Scope extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.model.atlas.model.scope.ScopePackage#getScope_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.model.scope.Scope#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * This allows to distinguish between schema workflow scopes and storage registry scopes
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see #setType(String)
	 * @see org.eclipse.fennec.model.atlas.model.scope.ScopePackage#getScope_Type()
	 * @model
	 * @generated
	 */
	String getType();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.model.scope.Scope#getType <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' attribute.
	 * @see #getType()
	 * @generated
	 */
	void setType(String value);

	/**
	 * Returns the value of the '<em><b>Parent Scope</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parent Scope</em>' attribute.
	 * @see #setParentScope(String)
	 * @see org.eclipse.fennec.model.atlas.model.scope.ScopePackage#getScope_ParentScope()
	 * @model
	 * @generated
	 */
	String getParentScope();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.model.scope.Scope#getParentScope <em>Parent Scope</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parent Scope</em>' attribute.
	 * @see #getParentScope()
	 * @generated
	 */
	void setParentScope(String value);

	/**
	 * Returns the value of the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @see org.eclipse.fennec.model.atlas.model.scope.ScopePackage#getScope_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.model.scope.Scope#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Links</b></em>' map.
	 * The key is of type {@link java.lang.String},
	 * and the value is of type {@link java.lang.String},
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Links</em>' map.
	 * @see org.eclipse.fennec.model.atlas.model.scope.ScopePackage#getScope_Links()
	 * @model mapType="org.eclipse.fennec.model.atlas.model.scope.LinksMap&lt;org.eclipse.emf.ecore.EString, org.eclipse.emf.ecore.EString&gt;"
	 * @generated
	 */
	EMap<String, String> getLinks();

	/**
	 * Returns the value of the '<em><b>Stages</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * These are the stages allowed for this scope (e.g. draft, approved, release)
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Stages</em>' attribute list.
	 * @see org.eclipse.fennec.model.atlas.model.scope.ScopePackage#getScope_Stages()
	 * @model
	 * @generated
	 */
	EList<String> getStages();

	/**
	 * Returns the value of the '<em><b>Final Stage</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * This is the stage that is considered the final one for this scope (e.g. release)
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Final Stage</em>' attribute.
	 * @see #setFinalStage(String)
	 * @see org.eclipse.fennec.model.atlas.model.scope.ScopePackage#getScope_FinalStage()
	 * @model
	 * @generated
	 */
	String getFinalStage();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.model.scope.Scope#getFinalStage <em>Final Stage</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Final Stage</em>' attribute.
	 * @see #getFinalStage()
	 * @generated
	 */
	void setFinalStage(String value);

	/**
	 * Returns the value of the '<em><b>Writable Stages</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * These are the writable stages allowed for this scope (e.g. draft, approved)
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Writable Stages</em>' attribute list.
	 * @see org.eclipse.fennec.model.atlas.model.scope.ScopePackage#getScope_WritableStages()
	 * @model
	 * @generated
	 */
	EList<String> getWritableStages();

} // Scope
