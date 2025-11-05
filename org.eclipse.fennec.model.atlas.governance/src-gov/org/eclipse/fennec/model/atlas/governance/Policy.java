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

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Policy</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Represents a specific policy instance that can be applied to system components.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.Policy#getPolicyId <em>Policy Id</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.Policy#getType <em>Type</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.Policy#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.Policy#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.Policy#getRequirementGroups <em>Requirement Groups</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getPolicy()
 * @model
 * @generated
 */
@ProviderType
public interface Policy extends EObject {
	/**
	 * Returns the value of the '<em><b>Policy Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Policy Id</em>' attribute.
	 * @see #setPolicyId(String)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getPolicy_PolicyId()
	 * @model id="true" required="true"
	 * @generated
	 */
	String getPolicyId();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.Policy#getPolicyId <em>Policy Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Policy Id</em>' attribute.
	 * @see #getPolicyId()
	 * @generated
	 */
	void setPolicyId(String value);

	/**
	 * Returns the value of the '<em><b>Type</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.governance.PolicyType}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.governance.PolicyType
	 * @see #setType(PolicyType)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getPolicy_Type()
	 * @model
	 * @generated
	 */
	PolicyType getType();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.Policy#getType <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.governance.PolicyType
	 * @see #getType()
	 * @generated
	 */
	void setType(PolicyType value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getPolicy_Name()
	 * @model required="true"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.Policy#getName <em>Name</em>}' attribute.
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
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getPolicy_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.Policy#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Requirement Groups</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.governance.RequirementGroup}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.fennec.model.atlas.governance.RequirementGroup#getPolicy <em>Policy</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Requirement Groups</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getPolicy_RequirementGroups()
	 * @see org.eclipse.fennec.model.atlas.governance.RequirementGroup#getPolicy
	 * @model opposite="policy" containment="true"
	 * @generated
	 */
	EList<RequirementGroup> getRequirementGroups();

} // Policy
