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

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Policy Requirement</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Represents a single, atomic requirement derived from a policy, such as a specific article, law, or control.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.PolicyRequirement#getRequirementId <em>Requirement Id</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.PolicyRequirement#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.PolicyRequirement#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.PolicyRequirement#getDocumentationLink <em>Documentation Link</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.PolicyRequirement#getModality <em>Modality</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.PolicyRequirement#getGroup <em>Group</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getPolicyRequirement()
 * @model
 * @generated
 */
@ProviderType
public interface PolicyRequirement extends EObject {
	/**
	 * Returns the value of the '<em><b>Requirement Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A unique identifier for the requirement, e.g., 'gdpr.art32' or 'iso27001.a.13.1.1'.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Requirement Id</em>' attribute.
	 * @see #setRequirementId(String)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getPolicyRequirement_RequirementId()
	 * @model id="true" required="true"
	 * @generated
	 */
	String getRequirementId();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.PolicyRequirement#getRequirementId <em>Requirement Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Requirement Id</em>' attribute.
	 * @see #getRequirementId()
	 * @generated
	 */
	void setRequirementId(String value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getPolicyRequirement_Name()
	 * @model required="true"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.PolicyRequirement#getName <em>Name</em>}' attribute.
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
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getPolicyRequirement_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.PolicyRequirement#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Documentation Link</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A URL pointing to the official text or documentation of the policy requirement.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Documentation Link</em>' attribute.
	 * @see #setDocumentationLink(String)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getPolicyRequirement_DocumentationLink()
	 * @model
	 * @generated
	 */
	String getDocumentationLink();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.PolicyRequirement#getDocumentationLink <em>Documentation Link</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Documentation Link</em>' attribute.
	 * @see #getDocumentationLink()
	 * @generated
	 */
	void setDocumentationLink(String value);

	/**
	 * Returns the value of the '<em><b>Modality</b></em>' attribute.
	 * The default value is <code>"MUST"</code>.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.governance.RequirementModality}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Specifies the level of obligation for this requirement (e.g., MUST, SHOULD, MAY).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Modality</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.governance.RequirementModality
	 * @see #setModality(RequirementModality)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getPolicyRequirement_Modality()
	 * @model default="MUST" required="true"
	 * @generated
	 */
	RequirementModality getModality();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.PolicyRequirement#getModality <em>Modality</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Modality</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.governance.RequirementModality
	 * @see #getModality()
	 * @generated
	 */
	void setModality(RequirementModality value);

	/**
	 * Returns the value of the '<em><b>Group</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link org.eclipse.fennec.model.atlas.governance.RequirementGroup#getRequirements <em>Requirements</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Group</em>' container reference.
	 * @see #setGroup(RequirementGroup)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getPolicyRequirement_Group()
	 * @see org.eclipse.fennec.model.atlas.governance.RequirementGroup#getRequirements
	 * @model opposite="requirements" transient="false"
	 * @generated
	 */
	RequirementGroup getGroup();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.PolicyRequirement#getGroup <em>Group</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Group</em>' container reference.
	 * @see #getGroup()
	 * @generated
	 */
	void setGroup(RequirementGroup value);

} // PolicyRequirement
