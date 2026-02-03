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
package org.eclipse.fennec.model.atlas.wf.workflowapi;

import java.util.List;

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
 *   <li>{@link org.eclipse.fennec.model.atlas.wf.workflowapi.Scope#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.wf.workflowapi.Scope#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.wf.workflowapi.Scope#getParentScope <em>Parent Scope</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.wf.workflowapi.Scope#getRegistries <em>Registries</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiPackage#getScope()
 * @model
 * @generated
 */
@ProviderType
public interface Scope {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiPackage#getScope_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.Scope#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiPackage#getScope_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.Scope#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Parent Scope</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parent Scope</em>' attribute.
	 * @see #setParentScope(String)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiPackage#getScope_ParentScope()
	 * @model
	 * @generated
	 */
	String getParentScope();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.Scope#getParentScope <em>Parent Scope</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parent Scope</em>' attribute.
	 * @see #getParentScope()
	 * @generated
	 */
	void setParentScope(String value);

	/**
	 * Returns the value of the '<em><b>Registries</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.wf.workflowapi.Registry}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Registries</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiPackage#getScope_Registries()
	 * @model containment="true"
	 * @generated
	 */
	List<Registry> getRegistries();

} // Scope
