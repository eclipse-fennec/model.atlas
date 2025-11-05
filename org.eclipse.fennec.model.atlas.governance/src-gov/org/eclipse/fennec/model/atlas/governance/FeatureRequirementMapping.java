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
 * A representation of the model object '<em><b>Feature Requirement Mapping</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Defines the explicit link between a technical PolicyFeature and a regulatory PolicyRequirement, explaining how the feature satisfies the requirement.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.FeatureRequirementMapping#getJustification <em>Justification</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.FeatureRequirementMapping#getSatisfiedRequirement <em>Satisfied Requirement</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.FeatureRequirementMapping#getFulfillingFeature <em>Fulfilling Feature</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getFeatureRequirementMapping()
 * @model
 * @generated
 */
@ProviderType
public interface FeatureRequirementMapping extends EObject {
	/**
	 * Returns the value of the '<em><b>Justification</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A textual explanation of why and how the assigned feature fulfills the policy requirement.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Justification</em>' attribute.
	 * @see #setJustification(String)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getFeatureRequirementMapping_Justification()
	 * @model
	 * @generated
	 */
	String getJustification();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.FeatureRequirementMapping#getJustification <em>Justification</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Justification</em>' attribute.
	 * @see #getJustification()
	 * @generated
	 */
	void setJustification(String value);

	/**
	 * Returns the value of the '<em><b>Satisfied Requirement</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Satisfied Requirement</em>' reference.
	 * @see #setSatisfiedRequirement(PolicyRequirement)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getFeatureRequirementMapping_SatisfiedRequirement()
	 * @model keys="requirementId" required="true"
	 * @generated
	 */
	PolicyRequirement getSatisfiedRequirement();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.FeatureRequirementMapping#getSatisfiedRequirement <em>Satisfied Requirement</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Satisfied Requirement</em>' reference.
	 * @see #getSatisfiedRequirement()
	 * @generated
	 */
	void setSatisfiedRequirement(PolicyRequirement value);

	/**
	 * Returns the value of the '<em><b>Fulfilling Feature</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Fulfilling Feature</em>' reference.
	 * @see #setFulfillingFeature(PolicyFeature)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getFeatureRequirementMapping_FulfillingFeature()
	 * @model keys="featureId" required="true"
	 * @generated
	 */
	PolicyFeature getFulfillingFeature();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.FeatureRequirementMapping#getFulfillingFeature <em>Fulfilling Feature</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Fulfilling Feature</em>' reference.
	 * @see #getFulfillingFeature()
	 * @generated
	 */
	void setFulfillingFeature(PolicyFeature value);

} // FeatureRequirementMapping
