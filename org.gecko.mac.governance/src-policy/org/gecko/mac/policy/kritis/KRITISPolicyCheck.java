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
package org.gecko.mac.policy.kritis;

import org.gecko.mac.governance.ComplianceCheckResult;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>KRITIS Policy Check</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * A specialized compliance check that represents the verification against regulations for Critical Infrastructures (KRITIS).
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.gecko.mac.policy.kritis.KRITISPolicyCheck#isResilienceVerified <em>Resilience Verified</em>}</li>
 *   <li>{@link org.gecko.mac.policy.kritis.KRITISPolicyCheck#isIncidentResponseReady <em>Incident Response Ready</em>}</li>
 *   <li>{@link org.gecko.mac.policy.kritis.KRITISPolicyCheck#isSupplyChainSecure <em>Supply Chain Secure</em>}</li>
 *   <li>{@link org.gecko.mac.policy.kritis.KRITISPolicyCheck#isSystemHardeningApplied <em>System Hardening Applied</em>}</li>
 * </ul>
 *
 * @see org.gecko.mac.policy.kritis.KritisPackage#getKRITISPolicyCheck()
 * @model
 * @generated
 */
@ProviderType
public interface KRITISPolicyCheck extends ComplianceCheckResult {
	/**
	 * Returns the value of the '<em><b>Resilience Verified</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result: Have resilience and redundancy measures (e.g., failover, backups) for the asset been implemented and verified?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Resilience Verified</em>' attribute.
	 * @see #setResilienceVerified(boolean)
	 * @see org.gecko.mac.policy.kritis.KritisPackage#getKRITISPolicyCheck_ResilienceVerified()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isResilienceVerified();

	/**
	 * Sets the value of the '{@link org.gecko.mac.policy.kritis.KRITISPolicyCheck#isResilienceVerified <em>Resilience Verified</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Resilience Verified</em>' attribute.
	 * @see #isResilienceVerified()
	 * @generated
	 */
	void setResilienceVerified(boolean value);

	/**
	 * Returns the value of the '<em><b>Incident Response Ready</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result: Is there a documented and tested incident response plan in place for this asset?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Incident Response Ready</em>' attribute.
	 * @see #setIncidentResponseReady(boolean)
	 * @see org.gecko.mac.policy.kritis.KritisPackage#getKRITISPolicyCheck_IncidentResponseReady()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isIncidentResponseReady();

	/**
	 * Sets the value of the '{@link org.gecko.mac.policy.kritis.KRITISPolicyCheck#isIncidentResponseReady <em>Incident Response Ready</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Incident Response Ready</em>' attribute.
	 * @see #isIncidentResponseReady()
	 * @generated
	 */
	void setIncidentResponseReady(boolean value);

	/**
	 * Returns the value of the '<em><b>Supply Chain Secure</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result: Has the physical and software supply chain for this asset been verified as secure and originating from trusted sources?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Supply Chain Secure</em>' attribute.
	 * @see #setSupplyChainSecure(boolean)
	 * @see org.gecko.mac.policy.kritis.KritisPackage#getKRITISPolicyCheck_SupplyChainSecure()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isSupplyChainSecure();

	/**
	 * Sets the value of the '{@link org.gecko.mac.policy.kritis.KRITISPolicyCheck#isSupplyChainSecure <em>Supply Chain Secure</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Supply Chain Secure</em>' attribute.
	 * @see #isSupplyChainSecure()
	 * @generated
	 */
	void setSupplyChainSecure(boolean value);

	/**
	 * Returns the value of the '<em><b>System Hardening Applied</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result: Is the asset's configuration hardened according to security best practices to minimize attack surfaces?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>System Hardening Applied</em>' attribute.
	 * @see #setSystemHardeningApplied(boolean)
	 * @see org.gecko.mac.policy.kritis.KritisPackage#getKRITISPolicyCheck_SystemHardeningApplied()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isSystemHardeningApplied();

	/**
	 * Sets the value of the '{@link org.gecko.mac.policy.kritis.KRITISPolicyCheck#isSystemHardeningApplied <em>System Hardening Applied</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>System Hardening Applied</em>' attribute.
	 * @see #isSystemHardeningApplied()
	 * @generated
	 */
	void setSystemHardeningApplied(boolean value);

} // KRITISPolicyCheck
