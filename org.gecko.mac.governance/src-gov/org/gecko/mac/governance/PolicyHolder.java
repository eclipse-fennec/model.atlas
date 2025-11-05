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
package org.gecko.mac.governance;

import org.eclipse.emf.common.util.EList;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Policy Holder</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * An entity that can hold policies
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.gecko.mac.governance.PolicyHolder#getPolicyDefinitions <em>Policy Definitions</em>}</li>
 *   <li>{@link org.gecko.mac.governance.PolicyHolder#getMappings <em>Mappings</em>}</li>
 * </ul>
 *
 * @see org.gecko.mac.governance.GovernancePackage#getPolicyHolder()
 * @model
 * @generated
 */
@ProviderType
public interface PolicyHolder extends SystemHolder {
	/**
	 * Returns the value of the '<em><b>Policy Definitions</b></em>' containment reference list.
	 * The list contents are of type {@link org.gecko.mac.governance.Policy}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Policy Definitions</em>' containment reference list.
	 * @see org.gecko.mac.governance.GovernancePackage#getPolicyHolder_PolicyDefinitions()
	 * @model containment="true" keys="policyId"
	 * @generated
	 */
	EList<Policy> getPolicyDefinitions();

	/**
	 * Returns the value of the '<em><b>Mappings</b></em>' containment reference list.
	 * The list contents are of type {@link org.gecko.mac.governance.FeatureRequirementMapping}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Mappings</em>' containment reference list.
	 * @see org.gecko.mac.governance.GovernancePackage#getPolicyHolder_Mappings()
	 * @model containment="true"
	 * @generated
	 */
	EList<FeatureRequirementMapping> getMappings();

} // PolicyHolder
