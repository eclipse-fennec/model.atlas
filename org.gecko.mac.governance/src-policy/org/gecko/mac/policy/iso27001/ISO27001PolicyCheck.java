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
package org.gecko.mac.policy.iso27001;

import org.gecko.mac.governance.ComplianceCheckResult;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>ISO27001 Policy Check</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * A specialized compliance check that represents the verification against relevant ISO/IEC 27001 controls for a specific asset.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.gecko.mac.policy.iso27001.ISO27001PolicyCheck#isNetworkControlImplemented <em>Network Control Implemented</em>}</li>
 *   <li>{@link org.gecko.mac.policy.iso27001.ISO27001PolicyCheck#isAccessControlVerified <em>Access Control Verified</em>}</li>
 *   <li>{@link org.gecko.mac.policy.iso27001.ISO27001PolicyCheck#isLoggingAndMonitoringActive <em>Logging And Monitoring Active</em>}</li>
 *   <li>{@link org.gecko.mac.policy.iso27001.ISO27001PolicyCheck#isAssetManagementCompliant <em>Asset Management Compliant</em>}</li>
 * </ul>
 *
 * @see org.gecko.mac.policy.iso27001.ISO27Package#getISO27001PolicyCheck()
 * @model
 * @generated
 */
@ProviderType
public interface ISO27001PolicyCheck extends ComplianceCheckResult {
	/**
	 * Returns the value of the '<em><b>Network Control Implemented</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result (Control A.13.1.1): Is communication to and from the asset secured according to network control policies (e.g., via TLS, with proper authentication)?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Network Control Implemented</em>' attribute.
	 * @see #setNetworkControlImplemented(boolean)
	 * @see org.gecko.mac.policy.iso27001.ISO27Package#getISO27001PolicyCheck_NetworkControlImplemented()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isNetworkControlImplemented();

	/**
	 * Sets the value of the '{@link org.gecko.mac.policy.iso27001.ISO27001PolicyCheck#isNetworkControlImplemented <em>Network Control Implemented</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Network Control Implemented</em>' attribute.
	 * @see #isNetworkControlImplemented()
	 * @generated
	 */
	void setNetworkControlImplemented(boolean value);

	/**
	 * Returns the value of the '<em><b>Access Control Verified</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result (Control A.9.4): Are access controls for the asset's interfaces and data properly defined and enforced based on the principle of least privilege?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Access Control Verified</em>' attribute.
	 * @see #setAccessControlVerified(boolean)
	 * @see org.gecko.mac.policy.iso27001.ISO27Package#getISO27001PolicyCheck_AccessControlVerified()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isAccessControlVerified();

	/**
	 * Sets the value of the '{@link org.gecko.mac.policy.iso27001.ISO27001PolicyCheck#isAccessControlVerified <em>Access Control Verified</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Access Control Verified</em>' attribute.
	 * @see #isAccessControlVerified()
	 * @generated
	 */
	void setAccessControlVerified(boolean value);

	/**
	 * Returns the value of the '<em><b>Logging And Monitoring Active</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result (Control A.12.4): Are relevant events, user activities, and exceptions logged and monitored for this asset to detect anomalies?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Logging And Monitoring Active</em>' attribute.
	 * @see #setLoggingAndMonitoringActive(boolean)
	 * @see org.gecko.mac.policy.iso27001.ISO27Package#getISO27001PolicyCheck_LoggingAndMonitoringActive()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isLoggingAndMonitoringActive();

	/**
	 * Sets the value of the '{@link org.gecko.mac.policy.iso27001.ISO27001PolicyCheck#isLoggingAndMonitoringActive <em>Logging And Monitoring Active</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Logging And Monitoring Active</em>' attribute.
	 * @see #isLoggingAndMonitoringActive()
	 * @generated
	 */
	void setLoggingAndMonitoringActive(boolean value);

	/**
	 * Returns the value of the '<em><b>Asset Management Compliant</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result (Control A.8): Is the asset correctly inventoried, classified, and handled according to the information classification scheme?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Asset Management Compliant</em>' attribute.
	 * @see #setAssetManagementCompliant(boolean)
	 * @see org.gecko.mac.policy.iso27001.ISO27Package#getISO27001PolicyCheck_AssetManagementCompliant()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isAssetManagementCompliant();

	/**
	 * Sets the value of the '{@link org.gecko.mac.policy.iso27001.ISO27001PolicyCheck#isAssetManagementCompliant <em>Asset Management Compliant</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Asset Management Compliant</em>' attribute.
	 * @see #isAssetManagementCompliant()
	 * @generated
	 */
	void setAssetManagementCompliant(boolean value);

} // ISO27001PolicyCheck
