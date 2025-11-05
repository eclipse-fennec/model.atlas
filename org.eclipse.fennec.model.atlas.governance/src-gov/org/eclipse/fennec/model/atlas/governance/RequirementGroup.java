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
 * A representation of the model object '<em><b>Requirement Group</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * A container for grouping related PolicyRequirements, allowing for hierarchical structuring of policies.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.RequirementGroup#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.RequirementGroup#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.RequirementGroup#getRequirements <em>Requirements</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.RequirementGroup#getSubGroups <em>Sub Groups</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.RequirementGroup#getGroup <em>Group</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.RequirementGroup#getPolicy <em>Policy</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getRequirementGroup()
 * @model
 * @generated
 */
@ProviderType
public interface RequirementGroup extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Human-readable name of the requirement group
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getRequirementGroup_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.RequirementGroup#getName <em>Name</em>}' attribute.
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
	 * <!-- begin-model-doc -->
	 * Detailed description of the requirement group
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getRequirementGroup_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.RequirementGroup#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Requirements</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.governance.PolicyRequirement}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.fennec.model.atlas.governance.PolicyRequirement#getGroup <em>Group</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Requirements</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getRequirementGroup_Requirements()
	 * @see org.eclipse.fennec.model.atlas.governance.PolicyRequirement#getGroup
	 * @model opposite="group" containment="true"
	 * @generated
	 */
	EList<PolicyRequirement> getRequirements();

	/**
	 * Returns the value of the '<em><b>Sub Groups</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.governance.RequirementGroup}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.fennec.model.atlas.governance.RequirementGroup#getGroup <em>Group</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sub Groups</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getRequirementGroup_SubGroups()
	 * @see org.eclipse.fennec.model.atlas.governance.RequirementGroup#getGroup
	 * @model opposite="group" containment="true"
	 * @generated
	 */
	EList<RequirementGroup> getSubGroups();

	/**
	 * Returns the value of the '<em><b>Group</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link org.eclipse.fennec.model.atlas.governance.RequirementGroup#getSubGroups <em>Sub Groups</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Group</em>' container reference.
	 * @see #setGroup(RequirementGroup)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getRequirementGroup_Group()
	 * @see org.eclipse.fennec.model.atlas.governance.RequirementGroup#getSubGroups
	 * @model opposite="subGroups" transient="false"
	 * @generated
	 */
	RequirementGroup getGroup();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.RequirementGroup#getGroup <em>Group</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Group</em>' container reference.
	 * @see #getGroup()
	 * @generated
	 */
	void setGroup(RequirementGroup value);

	/**
	 * Returns the value of the '<em><b>Policy</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link org.eclipse.fennec.model.atlas.governance.Policy#getRequirementGroups <em>Requirement Groups</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Policy</em>' container reference.
	 * @see #setPolicy(Policy)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getRequirementGroup_Policy()
	 * @see org.eclipse.fennec.model.atlas.governance.Policy#getRequirementGroups
	 * @model opposite="requirementGroups" transient="false"
	 * @generated
	 */
	Policy getPolicy();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.RequirementGroup#getPolicy <em>Policy</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Policy</em>' container reference.
	 * @see #getPolicy()
	 * @generated
	 */
	void setPolicy(Policy value);

} // RequirementGroup
