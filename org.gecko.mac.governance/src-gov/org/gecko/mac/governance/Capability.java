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
 * A representation of the model object '<em><b>Capability</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Represents what a system component provides to other components within a governance namespace. Capabilities fulfill matching Requirements from other components.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.gecko.mac.governance.Capability#getRequirement <em>Requirement</em>}</li>
 * </ul>
 *
 * @see org.gecko.mac.governance.GovernancePackage#getCapability()
 * @model
 * @generated
 */
@ProviderType
public interface Capability extends ComponentDependency {
	/**
	 * Returns the value of the '<em><b>Requirement</b></em>' reference list.
	 * The list contents are of type {@link org.gecko.mac.governance.Requirement}.
	 * It is bidirectional and its opposite is '{@link org.gecko.mac.governance.Requirement#getCapability <em>Capability</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Requirements that this capability fulfills. Automatically matched by governance namespace.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Requirement</em>' reference list.
	 * @see org.gecko.mac.governance.GovernancePackage#getCapability_Requirement()
	 * @see org.gecko.mac.governance.Requirement#getCapability
	 * @model opposite="capability"
	 * @generated
	 */
	EList<Requirement> getRequirement();

} // Capability
