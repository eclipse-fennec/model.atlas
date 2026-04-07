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
 * A representation of the model object '<em><b>Registry</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.wf.workflowapi.Registry#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.wf.workflowapi.Registry#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.wf.workflowapi.Registry#isSchemaRegistry <em>Schema Registry</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.wf.workflowapi.Registry#getStages <em>Stages</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.wf.workflowapi.Registry#getAllowedTransitions <em>Allowed Transitions</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiPackage#getRegistry()
 * @model
 * @generated
 */
@ProviderType
public interface Registry {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiPackage#getRegistry_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.Registry#getName <em>Name</em>}' attribute.
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
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiPackage#getRegistry_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.Registry#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Schema Registry</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Differentiate the schema registries from all the other kind of registries.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Schema Registry</em>' attribute.
	 * @see #setSchemaRegistry(boolean)
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiPackage#getRegistry_SchemaRegistry()
	 * @model
	 * @generated
	 */
	boolean isSchemaRegistry();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.Registry#isSchemaRegistry <em>Schema Registry</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Schema Registry</em>' attribute.
	 * @see #isSchemaRegistry()
	 * @generated
	 */
	void setSchemaRegistry(boolean value);

	/**
	 * Returns the value of the '<em><b>Stages</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.wf.workflowapi.Stage}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Stages</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiPackage#getRegistry_Stages()
	 * @model containment="true"
	 * @generated
	 */
	List<Stage> getStages();

	/**
	 * Returns the value of the '<em><b>Allowed Transitions</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.wf.workflowapi.StageTransition}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Allowed Transitions</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiPackage#getRegistry_AllowedTransitions()
	 * @model containment="true"
	 * @generated
	 */
	List<StageTransition> getAllowedTransitions();

} // Registry
