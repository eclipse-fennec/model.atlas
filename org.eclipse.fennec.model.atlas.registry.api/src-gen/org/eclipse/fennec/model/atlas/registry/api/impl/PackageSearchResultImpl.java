/*
 * Copyright (c) 2012 - 2026 Data In Motion and others.
 * All rights reserved.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.registry.api.impl;

import java.util.Collection;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.BasicInternalEList;

import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;

import org.eclipse.fennec.model.atlas.registry.api.PackageSearchResult;
import org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Package Search Result</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageSearchResultImpl#getHits <em>Hits</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageSearchResultImpl#getTotalHits <em>Total Hits</em>}</li>
 * </ul>
 *
 * @generated
 */
public class PackageSearchResultImpl extends MinimalEObjectImpl.Container implements PackageSearchResult {
	/**
	 * The cached value of the '{@link #getHits() <em>Hits</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHits()
	 * @generated
	 * @ordered
	 */
	protected EList<ObjectMetadata> hits;

	/**
	 * The default value of the '{@link #getTotalHits() <em>Total Hits</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTotalHits()
	 * @generated
	 * @ordered
	 */
	protected static final long TOTAL_HITS_EDEFAULT = 0L;

	/**
	 * The cached value of the '{@link #getTotalHits() <em>Total Hits</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTotalHits()
	 * @generated
	 * @ordered
	 */
	protected long totalHits = TOTAL_HITS_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PackageSearchResultImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RegistryApiPackage.Literals.PACKAGE_SEARCH_RESULT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<ObjectMetadata> getHits() {
		if (hits == null) {
			hits = new BasicInternalEList<ObjectMetadata>(ObjectMetadata.class);
		}
		return hits;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public long getTotalHits() {
		return totalHits;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTotalHits(long newTotalHits) {
		totalHits = newTotalHits;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case RegistryApiPackage.PACKAGE_SEARCH_RESULT__HITS:
				return getHits();
			case RegistryApiPackage.PACKAGE_SEARCH_RESULT__TOTAL_HITS:
				return getTotalHits();
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
			case RegistryApiPackage.PACKAGE_SEARCH_RESULT__HITS:
				getHits().clear();
				getHits().addAll((Collection<? extends ObjectMetadata>)newValue);
				return;
			case RegistryApiPackage.PACKAGE_SEARCH_RESULT__TOTAL_HITS:
				setTotalHits((Long)newValue);
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
			case RegistryApiPackage.PACKAGE_SEARCH_RESULT__HITS:
				getHits().clear();
				return;
			case RegistryApiPackage.PACKAGE_SEARCH_RESULT__TOTAL_HITS:
				setTotalHits(TOTAL_HITS_EDEFAULT);
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
			case RegistryApiPackage.PACKAGE_SEARCH_RESULT__HITS:
				return hits != null && !hits.isEmpty();
			case RegistryApiPackage.PACKAGE_SEARCH_RESULT__TOTAL_HITS:
				return totalHits != TOTAL_HITS_EDEFAULT;
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
		result.append(" (totalHits: ");
		result.append(totalHits);
		result.append(')');
		return result.toString();
	}

} //PackageSearchResultImpl
