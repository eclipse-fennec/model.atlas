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
package org.gecko.mac.policy.euaiact.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.gecko.mac.governance.impl.ComplianceCheckResultImpl;

import org.gecko.mac.policy.euaiact.AIActRiskLevel;
import org.gecko.mac.policy.euaiact.EUAIActPolicyCheck;
import org.gecko.mac.policy.euaiact.EUAIPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Act Policy Check</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.gecko.mac.policy.euaiact.impl.EUAIActPolicyCheckImpl#getRiskLevel <em>Risk Level</em>}</li>
 *   <li>{@link org.gecko.mac.policy.euaiact.impl.EUAIActPolicyCheckImpl#isIsHighRiskAnnexIII <em>Is High Risk Annex III</em>}</li>
 *   <li>{@link org.gecko.mac.policy.euaiact.impl.EUAIActPolicyCheckImpl#isThreatensHealthSafetyOrRights <em>Threatens Health Safety Or Rights</em>}</li>
 *   <li>{@link org.gecko.mac.policy.euaiact.impl.EUAIActPolicyCheckImpl#isIsUnacceptableRiskArticle5 <em>Is Unacceptable Risk Article5</em>}</li>
 * </ul>
 *
 * @generated
 */
public class EUAIActPolicyCheckImpl extends ComplianceCheckResultImpl implements EUAIActPolicyCheck {
	/**
	 * The default value of the '{@link #getRiskLevel() <em>Risk Level</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRiskLevel()
	 * @generated
	 * @ordered
	 */
	protected static final AIActRiskLevel RISK_LEVEL_EDEFAULT = AIActRiskLevel.UNKNOWN;

	/**
	 * The cached value of the '{@link #getRiskLevel() <em>Risk Level</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRiskLevel()
	 * @generated
	 * @ordered
	 */
	protected AIActRiskLevel riskLevel = RISK_LEVEL_EDEFAULT;

	/**
	 * The default value of the '{@link #isIsHighRiskAnnexIII() <em>Is High Risk Annex III</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIsHighRiskAnnexIII()
	 * @generated
	 * @ordered
	 */
	protected static final boolean IS_HIGH_RISK_ANNEX_III_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isIsHighRiskAnnexIII() <em>Is High Risk Annex III</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIsHighRiskAnnexIII()
	 * @generated
	 * @ordered
	 */
	protected boolean isHighRiskAnnexIII = IS_HIGH_RISK_ANNEX_III_EDEFAULT;

	/**
	 * The default value of the '{@link #isThreatensHealthSafetyOrRights() <em>Threatens Health Safety Or Rights</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isThreatensHealthSafetyOrRights()
	 * @generated
	 * @ordered
	 */
	protected static final boolean THREATENS_HEALTH_SAFETY_OR_RIGHTS_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isThreatensHealthSafetyOrRights() <em>Threatens Health Safety Or Rights</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isThreatensHealthSafetyOrRights()
	 * @generated
	 * @ordered
	 */
	protected boolean threatensHealthSafetyOrRights = THREATENS_HEALTH_SAFETY_OR_RIGHTS_EDEFAULT;

	/**
	 * The default value of the '{@link #isIsUnacceptableRiskArticle5() <em>Is Unacceptable Risk Article5</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIsUnacceptableRiskArticle5()
	 * @generated
	 * @ordered
	 */
	protected static final boolean IS_UNACCEPTABLE_RISK_ARTICLE5_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isIsUnacceptableRiskArticle5() <em>Is Unacceptable Risk Article5</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIsUnacceptableRiskArticle5()
	 * @generated
	 * @ordered
	 */
	protected boolean isUnacceptableRiskArticle5 = IS_UNACCEPTABLE_RISK_ARTICLE5_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected EUAIActPolicyCheckImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return EUAIPackage.Literals.EUAI_ACT_POLICY_CHECK;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public AIActRiskLevel getRiskLevel() {
		return riskLevel;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setRiskLevel(AIActRiskLevel newRiskLevel) {
		AIActRiskLevel oldRiskLevel = riskLevel;
		riskLevel = newRiskLevel == null ? RISK_LEVEL_EDEFAULT : newRiskLevel;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, EUAIPackage.EUAI_ACT_POLICY_CHECK__RISK_LEVEL, oldRiskLevel, riskLevel));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isIsHighRiskAnnexIII() {
		return isHighRiskAnnexIII;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setIsHighRiskAnnexIII(boolean newIsHighRiskAnnexIII) {
		boolean oldIsHighRiskAnnexIII = isHighRiskAnnexIII;
		isHighRiskAnnexIII = newIsHighRiskAnnexIII;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, EUAIPackage.EUAI_ACT_POLICY_CHECK__IS_HIGH_RISK_ANNEX_III, oldIsHighRiskAnnexIII, isHighRiskAnnexIII));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isThreatensHealthSafetyOrRights() {
		return threatensHealthSafetyOrRights;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setThreatensHealthSafetyOrRights(boolean newThreatensHealthSafetyOrRights) {
		boolean oldThreatensHealthSafetyOrRights = threatensHealthSafetyOrRights;
		threatensHealthSafetyOrRights = newThreatensHealthSafetyOrRights;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, EUAIPackage.EUAI_ACT_POLICY_CHECK__THREATENS_HEALTH_SAFETY_OR_RIGHTS, oldThreatensHealthSafetyOrRights, threatensHealthSafetyOrRights));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isIsUnacceptableRiskArticle5() {
		return isUnacceptableRiskArticle5;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setIsUnacceptableRiskArticle5(boolean newIsUnacceptableRiskArticle5) {
		boolean oldIsUnacceptableRiskArticle5 = isUnacceptableRiskArticle5;
		isUnacceptableRiskArticle5 = newIsUnacceptableRiskArticle5;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, EUAIPackage.EUAI_ACT_POLICY_CHECK__IS_UNACCEPTABLE_RISK_ARTICLE5, oldIsUnacceptableRiskArticle5, isUnacceptableRiskArticle5));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case EUAIPackage.EUAI_ACT_POLICY_CHECK__RISK_LEVEL:
				return getRiskLevel();
			case EUAIPackage.EUAI_ACT_POLICY_CHECK__IS_HIGH_RISK_ANNEX_III:
				return isIsHighRiskAnnexIII();
			case EUAIPackage.EUAI_ACT_POLICY_CHECK__THREATENS_HEALTH_SAFETY_OR_RIGHTS:
				return isThreatensHealthSafetyOrRights();
			case EUAIPackage.EUAI_ACT_POLICY_CHECK__IS_UNACCEPTABLE_RISK_ARTICLE5:
				return isIsUnacceptableRiskArticle5();
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
			case EUAIPackage.EUAI_ACT_POLICY_CHECK__RISK_LEVEL:
				setRiskLevel((AIActRiskLevel)newValue);
				return;
			case EUAIPackage.EUAI_ACT_POLICY_CHECK__IS_HIGH_RISK_ANNEX_III:
				setIsHighRiskAnnexIII((Boolean)newValue);
				return;
			case EUAIPackage.EUAI_ACT_POLICY_CHECK__THREATENS_HEALTH_SAFETY_OR_RIGHTS:
				setThreatensHealthSafetyOrRights((Boolean)newValue);
				return;
			case EUAIPackage.EUAI_ACT_POLICY_CHECK__IS_UNACCEPTABLE_RISK_ARTICLE5:
				setIsUnacceptableRiskArticle5((Boolean)newValue);
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
			case EUAIPackage.EUAI_ACT_POLICY_CHECK__RISK_LEVEL:
				setRiskLevel(RISK_LEVEL_EDEFAULT);
				return;
			case EUAIPackage.EUAI_ACT_POLICY_CHECK__IS_HIGH_RISK_ANNEX_III:
				setIsHighRiskAnnexIII(IS_HIGH_RISK_ANNEX_III_EDEFAULT);
				return;
			case EUAIPackage.EUAI_ACT_POLICY_CHECK__THREATENS_HEALTH_SAFETY_OR_RIGHTS:
				setThreatensHealthSafetyOrRights(THREATENS_HEALTH_SAFETY_OR_RIGHTS_EDEFAULT);
				return;
			case EUAIPackage.EUAI_ACT_POLICY_CHECK__IS_UNACCEPTABLE_RISK_ARTICLE5:
				setIsUnacceptableRiskArticle5(IS_UNACCEPTABLE_RISK_ARTICLE5_EDEFAULT);
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
			case EUAIPackage.EUAI_ACT_POLICY_CHECK__RISK_LEVEL:
				return riskLevel != RISK_LEVEL_EDEFAULT;
			case EUAIPackage.EUAI_ACT_POLICY_CHECK__IS_HIGH_RISK_ANNEX_III:
				return isHighRiskAnnexIII != IS_HIGH_RISK_ANNEX_III_EDEFAULT;
			case EUAIPackage.EUAI_ACT_POLICY_CHECK__THREATENS_HEALTH_SAFETY_OR_RIGHTS:
				return threatensHealthSafetyOrRights != THREATENS_HEALTH_SAFETY_OR_RIGHTS_EDEFAULT;
			case EUAIPackage.EUAI_ACT_POLICY_CHECK__IS_UNACCEPTABLE_RISK_ARTICLE5:
				return isUnacceptableRiskArticle5 != IS_UNACCEPTABLE_RISK_ARTICLE5_EDEFAULT;
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
		result.append(" (riskLevel: ");
		result.append(riskLevel);
		result.append(", isHighRiskAnnexIII: ");
		result.append(isHighRiskAnnexIII);
		result.append(", threatensHealthSafetyOrRights: ");
		result.append(threatensHealthSafetyOrRights);
		result.append(", isUnacceptableRiskArticle5: ");
		result.append(isUnacceptableRiskArticle5);
		result.append(')');
		return result.toString();
	}

} //EUAIActPolicyCheckImpl
