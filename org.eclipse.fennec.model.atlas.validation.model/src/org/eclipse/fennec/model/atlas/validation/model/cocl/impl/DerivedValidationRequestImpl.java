/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *   Data In Motion Consulting - initial implementation
 */
package org.eclipse.fennec.model.atlas.validation.model.cocl.impl;

import java.util.Collection;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;

import org.eclipse.emf.ecore.util.EObjectResolvingEList;

import org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage;
import org.eclipse.fennec.model.atlas.validation.model.cocl.DerivedValidationRequest;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Derived Validation Request</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.DerivedValidationRequestImpl#getDerivedFeature <em>Derived Feature</em>}</li>
 * </ul>
 *
 * @generated
 */
public class DerivedValidationRequestImpl extends ValidationRequestImpl implements DerivedValidationRequest {
	/**
	 * The cached value of the '{@link #getDerivedFeature() <em>Derived Feature</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDerivedFeature()
	 * @generated
	 * @ordered
	 */
	protected EList<EStructuralFeature> derivedFeature;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DerivedValidationRequestImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return COCLPackage.Literals.DERIVED_VALIDATION_REQUEST;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<EStructuralFeature> getDerivedFeature() {
		if (derivedFeature == null) {
			derivedFeature = new EObjectResolvingEList<EStructuralFeature>(EStructuralFeature.class, this, COCLPackage.DERIVED_VALIDATION_REQUEST__DERIVED_FEATURE);
		}
		return derivedFeature;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case COCLPackage.DERIVED_VALIDATION_REQUEST__DERIVED_FEATURE:
				return getDerivedFeature();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case COCLPackage.DERIVED_VALIDATION_REQUEST__DERIVED_FEATURE:
				getDerivedFeature().clear();
				getDerivedFeature().addAll((Collection<? extends EStructuralFeature>)newValue);
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
			case COCLPackage.DERIVED_VALIDATION_REQUEST__DERIVED_FEATURE:
				getDerivedFeature().clear();
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
			case COCLPackage.DERIVED_VALIDATION_REQUEST__DERIVED_FEATURE:
				return derivedFeature != null && !derivedFeature.isEmpty();
		}
		return super.eIsSet(featureID);
	}

} //DerivedValidationRequestImpl
