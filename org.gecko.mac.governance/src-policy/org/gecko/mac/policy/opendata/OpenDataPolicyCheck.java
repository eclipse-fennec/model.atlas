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
package org.gecko.mac.policy.opendata;

import org.gecko.mac.governance.ComplianceCheckResult;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Policy Check</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * A specialized compliance check that represents the verification against criteria for publishing data as Open Data.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.gecko.mac.policy.opendata.OpenDataPolicyCheck#isPiiAnonymizationVerified <em>Pii Anonymization Verified</em>}</li>
 *   <li>{@link org.gecko.mac.policy.opendata.OpenDataPolicyCheck#getLicenseType <em>License Type</em>}</li>
 *   <li>{@link org.gecko.mac.policy.opendata.OpenDataPolicyCheck#isIsMachineReadable <em>Is Machine Readable</em>}</li>
 *   <li>{@link org.gecko.mac.policy.opendata.OpenDataPolicyCheck#isHasPublicEndpoint <em>Has Public Endpoint</em>}</li>
 * </ul>
 *
 * @see org.gecko.mac.policy.opendata.OpenDataPackage#getOpenDataPolicyCheck()
 * @model
 * @generated
 */
@ProviderType
public interface OpenDataPolicyCheck extends ComplianceCheckResult {
	/**
	 * Returns the value of the '<em><b>Pii Anonymization Verified</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Prerequisite Check: Has the data been verified to contain no Personal Identifiable Information (PII) or has it been properly anonymized?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Pii Anonymization Verified</em>' attribute.
	 * @see #setPiiAnonymizationVerified(boolean)
	 * @see org.gecko.mac.policy.opendata.OpenDataPackage#getOpenDataPolicyCheck_PiiAnonymizationVerified()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isPiiAnonymizationVerified();

	/**
	 * Sets the value of the '{@link org.gecko.mac.policy.opendata.OpenDataPolicyCheck#isPiiAnonymizationVerified <em>Pii Anonymization Verified</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Pii Anonymization Verified</em>' attribute.
	 * @see #isPiiAnonymizationVerified()
	 * @generated
	 */
	void setPiiAnonymizationVerified(boolean value);

	/**
	 * Returns the value of the '<em><b>License Type</b></em>' attribute.
	 * The default value is <code>"NOT_APPLICABLE"</code>.
	 * The literals are from the enumeration {@link org.gecko.mac.policy.opendata.OpenDataLicense}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result: Is the data published under a recognized Open Data license?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>License Type</em>' attribute.
	 * @see org.gecko.mac.policy.opendata.OpenDataLicense
	 * @see #setLicenseType(OpenDataLicense)
	 * @see org.gecko.mac.policy.opendata.OpenDataPackage#getOpenDataPolicyCheck_LicenseType()
	 * @model default="NOT_APPLICABLE" required="true"
	 * @generated
	 */
	OpenDataLicense getLicenseType();

	/**
	 * Sets the value of the '{@link org.gecko.mac.policy.opendata.OpenDataPolicyCheck#getLicenseType <em>License Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>License Type</em>' attribute.
	 * @see org.gecko.mac.policy.opendata.OpenDataLicense
	 * @see #getLicenseType()
	 * @generated
	 */
	void setLicenseType(OpenDataLicense value);

	/**
	 * Returns the value of the '<em><b>Is Machine Readable</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result: Is the data provided in a machine-readable, non-proprietary format (e.g., CSV, JSON, GeoJSON)?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Is Machine Readable</em>' attribute.
	 * @see #setIsMachineReadable(boolean)
	 * @see org.gecko.mac.policy.opendata.OpenDataPackage#getOpenDataPolicyCheck_IsMachineReadable()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isIsMachineReadable();

	/**
	 * Sets the value of the '{@link org.gecko.mac.policy.opendata.OpenDataPolicyCheck#isIsMachineReadable <em>Is Machine Readable</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Is Machine Readable</em>' attribute.
	 * @see #isIsMachineReadable()
	 * @generated
	 */
	void setIsMachineReadable(boolean value);

	/**
	 * Returns the value of the '<em><b>Has Public Endpoint</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result: Is the data accessible via a stable, public endpoint (e.g., public API, download link)?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Has Public Endpoint</em>' attribute.
	 * @see #setHasPublicEndpoint(boolean)
	 * @see org.gecko.mac.policy.opendata.OpenDataPackage#getOpenDataPolicyCheck_HasPublicEndpoint()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isHasPublicEndpoint();

	/**
	 * Sets the value of the '{@link org.gecko.mac.policy.opendata.OpenDataPolicyCheck#isHasPublicEndpoint <em>Has Public Endpoint</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Has Public Endpoint</em>' attribute.
	 * @see #isHasPublicEndpoint()
	 * @generated
	 */
	void setHasPublicEndpoint(boolean value);

} // OpenDataPolicyCheck
