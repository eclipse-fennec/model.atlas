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
package org.eclipse.fennec.model.atlas.mgmt.governanceapi;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>System Component Governance Config</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Configuration interface for SystemComponentGovernanceService factory instances
 * <!-- end-model-doc -->
 *
 *
 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.ManagementApiPackage#getSystemComponentGovernanceConfig()
 * @model interface="true" abstract="true"
 * @generated
 */
@ProviderType
public interface SystemComponentGovernanceConfig {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The system ID this service instance manages
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	String systemId();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Optional description of the governance system
	 * <!-- end-model-doc -->
	 * @model
	 * @generated
	 */
	String description();

} // SystemComponentGovernanceConfig
