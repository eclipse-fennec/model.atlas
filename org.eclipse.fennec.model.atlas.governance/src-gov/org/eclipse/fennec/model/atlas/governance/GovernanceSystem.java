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
 *      Mark Hoffmann - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.governance;

import org.eclipse.emf.common.util.EList;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>System</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.GovernanceSystem#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.GovernanceSystem#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.GovernanceSystem#getSystemId <em>System Id</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.GovernanceSystem#getSubSystems <em>Sub Systems</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.GovernanceSystem#getReferencedSystems <em>Referenced Systems</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.GovernanceSystem#getComponentHolders <em>Component Holders</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.GovernanceSystem#getPolicyHolders <em>Policy Holders</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.GovernanceSystem#getHolders <em>Holders</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.GovernanceSystem#getPolicies <em>Policies</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getGovernanceSystem()
 * @model
 * @generated
 */
@ProviderType
public interface GovernanceSystem extends SystemComponentHolder, PolicyHolder, FeatureHolder, NamespaceHolder {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getGovernanceSystem_Name()
	 * @model required="true"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.GovernanceSystem#getName <em>Name</em>}' attribute.
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
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getGovernanceSystem_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.GovernanceSystem#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>System Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>System Id</em>' attribute.
	 * @see #setSystemId(String)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getGovernanceSystem_SystemId()
	 * @model id="true" required="true"
	 * @generated
	 */
	String getSystemId();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.GovernanceSystem#getSystemId <em>System Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>System Id</em>' attribute.
	 * @see #getSystemId()
	 * @generated
	 */
	void setSystemId(String value);

	/**
	 * Returns the value of the '<em><b>Sub Systems</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.governance.GovernanceSystem}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sub Systems</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getGovernanceSystem_SubSystems()
	 * @model containment="true" keys="systemId"
	 * @generated
	 */
	EList<GovernanceSystem> getSubSystems();

	/**
	 * Returns the value of the '<em><b>Referenced Systems</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.governance.GovernanceSystem}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Referenced Systems</em>' reference list.
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getGovernanceSystem_ReferencedSystems()
	 * @model keys="systemId"
	 * @generated
	 */
	EList<GovernanceSystem> getReferencedSystems();

	/**
	 * Returns the value of the '<em><b>Component Holders</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.governance.SystemComponentHolder}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Component Holders</em>' reference list.
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getGovernanceSystem_ComponentHolders()
	 * @model transient="true" volatile="true" derived="true"
	 * @generated
	 */
	EList<SystemComponentHolder> getComponentHolders();

	/**
	 * Returns the value of the '<em><b>Policy Holders</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.governance.PolicyHolder}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Policy Holders</em>' reference list.
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getGovernanceSystem_PolicyHolders()
	 * @model transient="true" volatile="true" derived="true"
	 * @generated
	 */
	EList<PolicyHolder> getPolicyHolders();

	/**
	 * Returns the value of the '<em><b>Holders</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.governance.SystemHolder}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.fennec.model.atlas.governance.SystemHolder#getSystem <em>System</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Holders</em>' reference list.
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getGovernanceSystem_Holders()
	 * @see org.eclipse.fennec.model.atlas.governance.SystemHolder#getSystem
	 * @model opposite="system"
	 * @generated
	 */
	EList<SystemHolder> getHolders();

	/**
	 * Returns the value of the '<em><b>Policies</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.governance.Policy}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Policies</em>' reference list.
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getGovernanceSystem_Policies()
	 * @model keys="policyId"
	 * @generated
	 */
	EList<Policy> getPolicies();

} // GovernanceSystem
