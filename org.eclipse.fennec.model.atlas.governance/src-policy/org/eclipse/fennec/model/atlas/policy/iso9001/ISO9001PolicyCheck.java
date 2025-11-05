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
package org.eclipse.fennec.model.atlas.policy.iso9001;

import org.eclipse.fennec.model.atlas.governance.ComplianceCheckResult;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Policy Check</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * A specialized compliance check that represents the verification against key requirements of the ISO 9001:2015 standard.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001PolicyCheck#isProcessApproachApplied <em>Process Approach Applied</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001PolicyCheck#isDocumentedInformationMaintained <em>Documented Information Maintained</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001PolicyCheck#isCustomerFocusDemonstrated <em>Customer Focus Demonstrated</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001PolicyCheck#isNonconformityProcessExists <em>Nonconformity Process Exists</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001PolicyCheck#isResourceAndCompetenceVerified <em>Resource And Competence Verified</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001Package#getISO9001PolicyCheck()
 * @model
 * @generated
 */
@ProviderType
public interface ISO9001PolicyCheck extends ComplianceCheckResult {
	/**
	 * Returns the value of the '<em><b>Process Approach Applied</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result (Clause 4.4): Is a process-oriented approach established, implemented, and maintained for the asset or system?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Process Approach Applied</em>' attribute.
	 * @see #setProcessApproachApplied(boolean)
	 * @see org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001Package#getISO9001PolicyCheck_ProcessApproachApplied()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isProcessApproachApplied();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001PolicyCheck#isProcessApproachApplied <em>Process Approach Applied</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Process Approach Applied</em>' attribute.
	 * @see #isProcessApproachApplied()
	 * @generated
	 */
	void setProcessApproachApplied(boolean value);

	/**
	 * Returns the value of the '<em><b>Documented Information Maintained</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result (Clause 7.5): Is the necessary documented information for the effectiveness of the QMS created, updated, and controlled?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Documented Information Maintained</em>' attribute.
	 * @see #setDocumentedInformationMaintained(boolean)
	 * @see org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001Package#getISO9001PolicyCheck_DocumentedInformationMaintained()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isDocumentedInformationMaintained();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001PolicyCheck#isDocumentedInformationMaintained <em>Documented Information Maintained</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Documented Information Maintained</em>' attribute.
	 * @see #isDocumentedInformationMaintained()
	 * @generated
	 */
	void setDocumentedInformationMaintained(boolean value);

	/**
	 * Returns the value of the '<em><b>Customer Focus Demonstrated</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result (Clause 5.1.2): Are customer and applicable statutory and regulatory requirements determined, understood, and consistently met?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Customer Focus Demonstrated</em>' attribute.
	 * @see #setCustomerFocusDemonstrated(boolean)
	 * @see org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001Package#getISO9001PolicyCheck_CustomerFocusDemonstrated()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isCustomerFocusDemonstrated();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001PolicyCheck#isCustomerFocusDemonstrated <em>Customer Focus Demonstrated</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Customer Focus Demonstrated</em>' attribute.
	 * @see #isCustomerFocusDemonstrated()
	 * @generated
	 */
	void setCustomerFocusDemonstrated(boolean value);

	/**
	 * Returns the value of the '<em><b>Nonconformity Process Exists</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result (Clause 10.2): Is there a defined process to react to nonconformities and take corrective action to prevent recurrence?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Nonconformity Process Exists</em>' attribute.
	 * @see #setNonconformityProcessExists(boolean)
	 * @see org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001Package#getISO9001PolicyCheck_NonconformityProcessExists()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isNonconformityProcessExists();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001PolicyCheck#isNonconformityProcessExists <em>Nonconformity Process Exists</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Nonconformity Process Exists</em>' attribute.
	 * @see #isNonconformityProcessExists()
	 * @generated
	 */
	void setNonconformityProcessExists(boolean value);

	/**
	 * Returns the value of the '<em><b>Resource And Competence Verified</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result (Clause 7.1, 7.2): Are the necessary resources provided and is the competence of personnel ensured for the quality management system?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Resource And Competence Verified</em>' attribute.
	 * @see #setResourceAndCompetenceVerified(boolean)
	 * @see org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001Package#getISO9001PolicyCheck_ResourceAndCompetenceVerified()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isResourceAndCompetenceVerified();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001PolicyCheck#isResourceAndCompetenceVerified <em>Resource And Competence Verified</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Resource And Competence Verified</em>' attribute.
	 * @see #isResourceAndCompetenceVerified()
	 * @generated
	 */
	void setResourceAndCompetenceVerified(boolean value);

} // ISO9001PolicyCheck
