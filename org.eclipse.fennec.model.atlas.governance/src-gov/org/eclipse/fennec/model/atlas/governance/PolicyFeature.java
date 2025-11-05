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
 * A representation of the model object '<em><b>Policy Feature</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.PolicyFeature#getFeatureId <em>Feature Id</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.PolicyFeature#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.PolicyFeature#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.PolicyFeature#getParent <em>Parent</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.PolicyFeature#getComponents <em>Components</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getPolicyFeature()
 * @model
 * @generated
 */
@ProviderType
public interface PolicyFeature extends EObject {
	/**
	 * Returns the value of the '<em><b>Feature Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Feature Id</em>' attribute.
	 * @see #setFeatureId(String)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getPolicyFeature_FeatureId()
	 * @model id="true" required="true"
	 * @generated
	 */
	String getFeatureId();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.PolicyFeature#getFeatureId <em>Feature Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Feature Id</em>' attribute.
	 * @see #getFeatureId()
	 * @generated
	 */
	void setFeatureId(String value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getPolicyFeature_Name()
	 * @model required="true"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.PolicyFeature#getName <em>Name</em>}' attribute.
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
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getPolicyFeature_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.PolicyFeature#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Parent</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link org.eclipse.fennec.model.atlas.governance.FeatureHolder#getFeatures <em>Features</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Parent</em>' container reference.
	 * @see #setParent(FeatureHolder)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getPolicyFeature_Parent()
	 * @see org.eclipse.fennec.model.atlas.governance.FeatureHolder#getFeatures
	 * @model opposite="features" transient="false"
	 * @generated
	 */
	FeatureHolder getParent();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.PolicyFeature#getParent <em>Parent</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parent</em>' container reference.
	 * @see #getParent()
	 * @generated
	 */
	void setParent(FeatureHolder value);

	/**
	 * Returns the value of the '<em><b>Components</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.governance.SystemComponent}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.fennec.model.atlas.governance.SystemComponent#getSupportedFeatures <em>Supported Features</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Components</em>' reference list.
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getPolicyFeature_Components()
	 * @see org.eclipse.fennec.model.atlas.governance.SystemComponent#getSupportedFeatures
	 * @model opposite="supportedFeatures" keys="componentId"
	 * @generated
	 */
	EList<SystemComponent> getComponents();

} // PolicyFeature
