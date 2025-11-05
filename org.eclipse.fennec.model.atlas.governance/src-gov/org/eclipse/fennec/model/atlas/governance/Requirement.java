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
 * A representation of the model object '<em><b>Requirement</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Represents what a system component needs from other components within a governance namespace. Requirements are fulfilled by matching Capabilities from other components.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.Requirement#getCapability <em>Capability</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getRequirement()
 * @model
 * @generated
 */
@ProviderType
public interface Requirement extends ComponentDependency {
	/**
	 * Returns the value of the '<em><b>Capability</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.governance.Capability}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.fennec.model.atlas.governance.Capability#getRequirement <em>Requirement</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Capabilities that fulfill this requirement. Automatically matched by governance namespace.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Capability</em>' reference list.
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getRequirement_Capability()
	 * @see org.eclipse.fennec.model.atlas.governance.Capability#getRequirement
	 * @model opposite="requirement"
	 * @generated
	 */
	EList<Capability> getCapability();

} // Requirement
