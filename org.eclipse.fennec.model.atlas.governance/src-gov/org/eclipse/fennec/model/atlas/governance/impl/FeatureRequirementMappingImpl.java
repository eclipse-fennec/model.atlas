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
package org.eclipse.fennec.model.atlas.governance.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.fennec.model.atlas.governance.FeatureRequirementMapping;
import org.eclipse.fennec.model.atlas.governance.GovernancePackage;
import org.eclipse.fennec.model.atlas.governance.PolicyFeature;
import org.eclipse.fennec.model.atlas.governance.PolicyRequirement;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Feature Requirement Mapping</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.impl.FeatureRequirementMappingImpl#getJustification <em>Justification</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.impl.FeatureRequirementMappingImpl#getSatisfiedRequirement <em>Satisfied Requirement</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.impl.FeatureRequirementMappingImpl#getFulfillingFeature <em>Fulfilling Feature</em>}</li>
 * </ul>
 *
 * @generated
 */
public class FeatureRequirementMappingImpl extends MinimalEObjectImpl.Container implements FeatureRequirementMapping {
	/**
	 * The default value of the '{@link #getJustification() <em>Justification</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getJustification()
	 * @generated
	 * @ordered
	 */
	protected static final String JUSTIFICATION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getJustification() <em>Justification</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getJustification()
	 * @generated
	 * @ordered
	 */
	protected String justification = JUSTIFICATION_EDEFAULT;

	/**
	 * The cached value of the '{@link #getSatisfiedRequirement() <em>Satisfied Requirement</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSatisfiedRequirement()
	 * @generated
	 * @ordered
	 */
	protected PolicyRequirement satisfiedRequirement;

	/**
	 * The cached value of the '{@link #getFulfillingFeature() <em>Fulfilling Feature</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFulfillingFeature()
	 * @generated
	 * @ordered
	 */
	protected PolicyFeature fulfillingFeature;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected FeatureRequirementMappingImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GovernancePackage.Literals.FEATURE_REQUIREMENT_MAPPING;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getJustification() {
		return justification;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setJustification(String newJustification) {
		String oldJustification = justification;
		justification = newJustification;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.FEATURE_REQUIREMENT_MAPPING__JUSTIFICATION, oldJustification, justification));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public PolicyRequirement getSatisfiedRequirement() {
		if (satisfiedRequirement != null && satisfiedRequirement.eIsProxy()) {
			InternalEObject oldSatisfiedRequirement = (InternalEObject)satisfiedRequirement;
			satisfiedRequirement = (PolicyRequirement)eResolveProxy(oldSatisfiedRequirement);
			if (satisfiedRequirement != oldSatisfiedRequirement) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, GovernancePackage.FEATURE_REQUIREMENT_MAPPING__SATISFIED_REQUIREMENT, oldSatisfiedRequirement, satisfiedRequirement));
			}
		}
		return satisfiedRequirement;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PolicyRequirement basicGetSatisfiedRequirement() {
		return satisfiedRequirement;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSatisfiedRequirement(PolicyRequirement newSatisfiedRequirement) {
		PolicyRequirement oldSatisfiedRequirement = satisfiedRequirement;
		satisfiedRequirement = newSatisfiedRequirement;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.FEATURE_REQUIREMENT_MAPPING__SATISFIED_REQUIREMENT, oldSatisfiedRequirement, satisfiedRequirement));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public PolicyFeature getFulfillingFeature() {
		if (fulfillingFeature != null && fulfillingFeature.eIsProxy()) {
			InternalEObject oldFulfillingFeature = (InternalEObject)fulfillingFeature;
			fulfillingFeature = (PolicyFeature)eResolveProxy(oldFulfillingFeature);
			if (fulfillingFeature != oldFulfillingFeature) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, GovernancePackage.FEATURE_REQUIREMENT_MAPPING__FULFILLING_FEATURE, oldFulfillingFeature, fulfillingFeature));
			}
		}
		return fulfillingFeature;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PolicyFeature basicGetFulfillingFeature() {
		return fulfillingFeature;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFulfillingFeature(PolicyFeature newFulfillingFeature) {
		PolicyFeature oldFulfillingFeature = fulfillingFeature;
		fulfillingFeature = newFulfillingFeature;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.FEATURE_REQUIREMENT_MAPPING__FULFILLING_FEATURE, oldFulfillingFeature, fulfillingFeature));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case GovernancePackage.FEATURE_REQUIREMENT_MAPPING__JUSTIFICATION:
				return getJustification();
			case GovernancePackage.FEATURE_REQUIREMENT_MAPPING__SATISFIED_REQUIREMENT:
				if (resolve) return getSatisfiedRequirement();
				return basicGetSatisfiedRequirement();
			case GovernancePackage.FEATURE_REQUIREMENT_MAPPING__FULFILLING_FEATURE:
				if (resolve) return getFulfillingFeature();
				return basicGetFulfillingFeature();
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
			case GovernancePackage.FEATURE_REQUIREMENT_MAPPING__JUSTIFICATION:
				setJustification((String)newValue);
				return;
			case GovernancePackage.FEATURE_REQUIREMENT_MAPPING__SATISFIED_REQUIREMENT:
				setSatisfiedRequirement((PolicyRequirement)newValue);
				return;
			case GovernancePackage.FEATURE_REQUIREMENT_MAPPING__FULFILLING_FEATURE:
				setFulfillingFeature((PolicyFeature)newValue);
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
			case GovernancePackage.FEATURE_REQUIREMENT_MAPPING__JUSTIFICATION:
				setJustification(JUSTIFICATION_EDEFAULT);
				return;
			case GovernancePackage.FEATURE_REQUIREMENT_MAPPING__SATISFIED_REQUIREMENT:
				setSatisfiedRequirement((PolicyRequirement)null);
				return;
			case GovernancePackage.FEATURE_REQUIREMENT_MAPPING__FULFILLING_FEATURE:
				setFulfillingFeature((PolicyFeature)null);
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
			case GovernancePackage.FEATURE_REQUIREMENT_MAPPING__JUSTIFICATION:
				return JUSTIFICATION_EDEFAULT == null ? justification != null : !JUSTIFICATION_EDEFAULT.equals(justification);
			case GovernancePackage.FEATURE_REQUIREMENT_MAPPING__SATISFIED_REQUIREMENT:
				return satisfiedRequirement != null;
			case GovernancePackage.FEATURE_REQUIREMENT_MAPPING__FULFILLING_FEATURE:
				return fulfillingFeature != null;
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
		result.append(" (justification: ");
		result.append(justification);
		result.append(')');
		return result.toString();
	}

} //FeatureRequirementMappingImpl
