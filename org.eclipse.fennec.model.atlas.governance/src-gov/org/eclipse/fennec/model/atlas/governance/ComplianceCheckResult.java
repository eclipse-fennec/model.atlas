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

import java.util.Date;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Compliance Check Result</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.ComplianceCheckResult#getStatus <em>Status</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.ComplianceCheckResult#getCheckTimestamp <em>Check Timestamp</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.ComplianceCheckResult#getSummary <em>Summary</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.ComplianceCheckResult#getFindings <em>Findings</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getComplianceCheckResult()
 * @model abstract="true"
 * @generated
 */
@ProviderType
public interface ComplianceCheckResult extends EObject {
	/**
	 * Returns the value of the '<em><b>Status</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.governance.ComplianceStatus}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Status</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.governance.ComplianceStatus
	 * @see #setStatus(ComplianceStatus)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getComplianceCheckResult_Status()
	 * @model
	 * @generated
	 */
	ComplianceStatus getStatus();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.ComplianceCheckResult#getStatus <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Status</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.governance.ComplianceStatus
	 * @see #getStatus()
	 * @generated
	 */
	void setStatus(ComplianceStatus value);

	/**
	 * Returns the value of the '<em><b>Check Timestamp</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Check Timestamp</em>' attribute.
	 * @see #setCheckTimestamp(Date)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getComplianceCheckResult_CheckTimestamp()
	 * @model
	 * @generated
	 */
	Date getCheckTimestamp();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.ComplianceCheckResult#getCheckTimestamp <em>Check Timestamp</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Check Timestamp</em>' attribute.
	 * @see #getCheckTimestamp()
	 * @generated
	 */
	void setCheckTimestamp(Date value);

	/**
	 * Returns the value of the '<em><b>Summary</b></em>' attribute.
	 * The default value is <code>"A short summary of the check result."</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Summary</em>' attribute.
	 * @see #setSummary(String)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getComplianceCheckResult_Summary()
	 * @model default="A short summary of the check result."
	 * @generated
	 */
	String getSummary();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.ComplianceCheckResult#getSummary <em>Summary</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Summary</em>' attribute.
	 * @see #getSummary()
	 * @generated
	 */
	void setSummary(String value);

	/**
	 * Returns the value of the '<em><b>Findings</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.governance.Finding}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Findings</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getComplianceCheckResult_Findings()
	 * @model containment="true"
	 * @generated
	 */
	EList<Finding> getFindings();

} // ComplianceCheckResult
