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
package org.gecko.mac.policy.gdpr.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.gecko.mac.governance.impl.ComplianceCheckResultImpl;

import org.gecko.mac.policy.gdpr.GDPRPackage;
import org.gecko.mac.policy.gdpr.GDPRPolicyCheck;
import org.gecko.mac.policy.gdpr.LegalBasis;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Policy Check</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.gecko.mac.policy.gdpr.impl.GDPRPolicyCheckImpl#isContainsPersonalData <em>Contains Personal Data</em>}</li>
 *   <li>{@link org.gecko.mac.policy.gdpr.impl.GDPRPolicyCheckImpl#getLegalBasisForProcessing <em>Legal Basis For Processing</em>}</li>
 *   <li>{@link org.gecko.mac.policy.gdpr.impl.GDPRPolicyCheckImpl#isDataMinimizationApplied <em>Data Minimization Applied</em>}</li>
 *   <li>{@link org.gecko.mac.policy.gdpr.impl.GDPRPolicyCheckImpl#isPurposeLimitationMet <em>Purpose Limitation Met</em>}</li>
 * </ul>
 *
 * @generated
 */
public class GDPRPolicyCheckImpl extends ComplianceCheckResultImpl implements GDPRPolicyCheck {
	/**
	 * The default value of the '{@link #isContainsPersonalData() <em>Contains Personal Data</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isContainsPersonalData()
	 * @generated
	 * @ordered
	 */
	protected static final boolean CONTAINS_PERSONAL_DATA_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isContainsPersonalData() <em>Contains Personal Data</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isContainsPersonalData()
	 * @generated
	 * @ordered
	 */
	protected boolean containsPersonalData = CONTAINS_PERSONAL_DATA_EDEFAULT;

	/**
	 * The default value of the '{@link #getLegalBasisForProcessing() <em>Legal Basis For Processing</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLegalBasisForProcessing()
	 * @generated
	 * @ordered
	 */
	protected static final LegalBasis LEGAL_BASIS_FOR_PROCESSING_EDEFAULT = LegalBasis.NOT_APPLICABLE;

	/**
	 * The cached value of the '{@link #getLegalBasisForProcessing() <em>Legal Basis For Processing</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLegalBasisForProcessing()
	 * @generated
	 * @ordered
	 */
	protected LegalBasis legalBasisForProcessing = LEGAL_BASIS_FOR_PROCESSING_EDEFAULT;

	/**
	 * The default value of the '{@link #isDataMinimizationApplied() <em>Data Minimization Applied</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isDataMinimizationApplied()
	 * @generated
	 * @ordered
	 */
	protected static final boolean DATA_MINIMIZATION_APPLIED_EDEFAULT = true;

	/**
	 * The cached value of the '{@link #isDataMinimizationApplied() <em>Data Minimization Applied</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isDataMinimizationApplied()
	 * @generated
	 * @ordered
	 */
	protected boolean dataMinimizationApplied = DATA_MINIMIZATION_APPLIED_EDEFAULT;

	/**
	 * The default value of the '{@link #isPurposeLimitationMet() <em>Purpose Limitation Met</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isPurposeLimitationMet()
	 * @generated
	 * @ordered
	 */
	protected static final boolean PURPOSE_LIMITATION_MET_EDEFAULT = true;

	/**
	 * The cached value of the '{@link #isPurposeLimitationMet() <em>Purpose Limitation Met</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isPurposeLimitationMet()
	 * @generated
	 * @ordered
	 */
	protected boolean purposeLimitationMet = PURPOSE_LIMITATION_MET_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GDPRPolicyCheckImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GDPRPackage.Literals.GDPR_POLICY_CHECK;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isContainsPersonalData() {
		return containsPersonalData;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setContainsPersonalData(boolean newContainsPersonalData) {
		boolean oldContainsPersonalData = containsPersonalData;
		containsPersonalData = newContainsPersonalData;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GDPRPackage.GDPR_POLICY_CHECK__CONTAINS_PERSONAL_DATA, oldContainsPersonalData, containsPersonalData));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public LegalBasis getLegalBasisForProcessing() {
		return legalBasisForProcessing;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLegalBasisForProcessing(LegalBasis newLegalBasisForProcessing) {
		LegalBasis oldLegalBasisForProcessing = legalBasisForProcessing;
		legalBasisForProcessing = newLegalBasisForProcessing == null ? LEGAL_BASIS_FOR_PROCESSING_EDEFAULT : newLegalBasisForProcessing;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GDPRPackage.GDPR_POLICY_CHECK__LEGAL_BASIS_FOR_PROCESSING, oldLegalBasisForProcessing, legalBasisForProcessing));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isDataMinimizationApplied() {
		return dataMinimizationApplied;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDataMinimizationApplied(boolean newDataMinimizationApplied) {
		boolean oldDataMinimizationApplied = dataMinimizationApplied;
		dataMinimizationApplied = newDataMinimizationApplied;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GDPRPackage.GDPR_POLICY_CHECK__DATA_MINIMIZATION_APPLIED, oldDataMinimizationApplied, dataMinimizationApplied));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isPurposeLimitationMet() {
		return purposeLimitationMet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPurposeLimitationMet(boolean newPurposeLimitationMet) {
		boolean oldPurposeLimitationMet = purposeLimitationMet;
		purposeLimitationMet = newPurposeLimitationMet;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GDPRPackage.GDPR_POLICY_CHECK__PURPOSE_LIMITATION_MET, oldPurposeLimitationMet, purposeLimitationMet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case GDPRPackage.GDPR_POLICY_CHECK__CONTAINS_PERSONAL_DATA:
				return isContainsPersonalData();
			case GDPRPackage.GDPR_POLICY_CHECK__LEGAL_BASIS_FOR_PROCESSING:
				return getLegalBasisForProcessing();
			case GDPRPackage.GDPR_POLICY_CHECK__DATA_MINIMIZATION_APPLIED:
				return isDataMinimizationApplied();
			case GDPRPackage.GDPR_POLICY_CHECK__PURPOSE_LIMITATION_MET:
				return isPurposeLimitationMet();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case GDPRPackage.GDPR_POLICY_CHECK__CONTAINS_PERSONAL_DATA:
				setContainsPersonalData((Boolean)newValue);
				return;
			case GDPRPackage.GDPR_POLICY_CHECK__LEGAL_BASIS_FOR_PROCESSING:
				setLegalBasisForProcessing((LegalBasis)newValue);
				return;
			case GDPRPackage.GDPR_POLICY_CHECK__DATA_MINIMIZATION_APPLIED:
				setDataMinimizationApplied((Boolean)newValue);
				return;
			case GDPRPackage.GDPR_POLICY_CHECK__PURPOSE_LIMITATION_MET:
				setPurposeLimitationMet((Boolean)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case GDPRPackage.GDPR_POLICY_CHECK__CONTAINS_PERSONAL_DATA:
				setContainsPersonalData(CONTAINS_PERSONAL_DATA_EDEFAULT);
				return;
			case GDPRPackage.GDPR_POLICY_CHECK__LEGAL_BASIS_FOR_PROCESSING:
				setLegalBasisForProcessing(LEGAL_BASIS_FOR_PROCESSING_EDEFAULT);
				return;
			case GDPRPackage.GDPR_POLICY_CHECK__DATA_MINIMIZATION_APPLIED:
				setDataMinimizationApplied(DATA_MINIMIZATION_APPLIED_EDEFAULT);
				return;
			case GDPRPackage.GDPR_POLICY_CHECK__PURPOSE_LIMITATION_MET:
				setPurposeLimitationMet(PURPOSE_LIMITATION_MET_EDEFAULT);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case GDPRPackage.GDPR_POLICY_CHECK__CONTAINS_PERSONAL_DATA:
				return containsPersonalData != CONTAINS_PERSONAL_DATA_EDEFAULT;
			case GDPRPackage.GDPR_POLICY_CHECK__LEGAL_BASIS_FOR_PROCESSING:
				return legalBasisForProcessing != LEGAL_BASIS_FOR_PROCESSING_EDEFAULT;
			case GDPRPackage.GDPR_POLICY_CHECK__DATA_MINIMIZATION_APPLIED:
				return dataMinimizationApplied != DATA_MINIMIZATION_APPLIED_EDEFAULT;
			case GDPRPackage.GDPR_POLICY_CHECK__PURPOSE_LIMITATION_MET:
				return purposeLimitationMet != PURPOSE_LIMITATION_MET_EDEFAULT;
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (containsPersonalData: ");
		result.append(containsPersonalData);
		result.append(", legalBasisForProcessing: ");
		result.append(legalBasisForProcessing);
		result.append(", dataMinimizationApplied: ");
		result.append(dataMinimizationApplied);
		result.append(", purposeLimitationMet: ");
		result.append(purposeLimitationMet);
		result.append(')');
		return result.toString();
	}

} //GDPRPolicyCheckImpl
