/**
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
 *     Data In Motion - initial API and implementation
 */
/*
 */
package org.eclipse.fennec.model.atlas.datagen.model.datagen.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceGenConfig;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceStrategy;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Reference Gen Config</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.ReferenceGenConfigImpl#getFeatureName <em>Feature Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.ReferenceGenConfigImpl#getStrategy <em>Strategy</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.ReferenceGenConfigImpl#getTargetClassFilter <em>Target Class Filter</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.ReferenceGenConfigImpl#getMinCount <em>Min Count</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.ReferenceGenConfigImpl#getMaxCount <em>Max Count</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ReferenceGenConfigImpl extends MinimalEObjectImpl.Container implements ReferenceGenConfig {
	/**
	 * The default value of the '{@link #getFeatureName() <em>Feature Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFeatureName()
	 * @generated
	 * @ordered
	 */
	protected static final String FEATURE_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFeatureName() <em>Feature Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFeatureName()
	 * @generated
	 * @ordered
	 */
	protected String featureName = FEATURE_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getStrategy() <em>Strategy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStrategy()
	 * @generated
	 * @ordered
	 */
	protected static final ReferenceStrategy STRATEGY_EDEFAULT = ReferenceStrategy.RANDOM;

	/**
	 * The cached value of the '{@link #getStrategy() <em>Strategy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStrategy()
	 * @generated
	 * @ordered
	 */
	protected ReferenceStrategy strategy = STRATEGY_EDEFAULT;

	/**
	 * The default value of the '{@link #getTargetClassFilter() <em>Target Class Filter</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTargetClassFilter()
	 * @generated
	 * @ordered
	 */
	protected static final String TARGET_CLASS_FILTER_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTargetClassFilter() <em>Target Class Filter</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTargetClassFilter()
	 * @generated
	 * @ordered
	 */
	protected String targetClassFilter = TARGET_CLASS_FILTER_EDEFAULT;

	/**
	 * The default value of the '{@link #getMinCount() <em>Min Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMinCount()
	 * @generated
	 * @ordered
	 */
	protected static final int MIN_COUNT_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getMinCount() <em>Min Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMinCount()
	 * @generated
	 * @ordered
	 */
	protected int minCount = MIN_COUNT_EDEFAULT;

	/**
	 * The default value of the '{@link #getMaxCount() <em>Max Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMaxCount()
	 * @generated
	 * @ordered
	 */
	protected static final int MAX_COUNT_EDEFAULT = 1;

	/**
	 * The cached value of the '{@link #getMaxCount() <em>Max Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMaxCount()
	 * @generated
	 * @ordered
	 */
	protected int maxCount = MAX_COUNT_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ReferenceGenConfigImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return DatagenPackage.Literals.REFERENCE_GEN_CONFIG;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getFeatureName() {
		return featureName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFeatureName(String newFeatureName) {
		String oldFeatureName = featureName;
		featureName = newFeatureName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DatagenPackage.REFERENCE_GEN_CONFIG__FEATURE_NAME, oldFeatureName, featureName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ReferenceStrategy getStrategy() {
		return strategy;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStrategy(ReferenceStrategy newStrategy) {
		ReferenceStrategy oldStrategy = strategy;
		strategy = newStrategy == null ? STRATEGY_EDEFAULT : newStrategy;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DatagenPackage.REFERENCE_GEN_CONFIG__STRATEGY, oldStrategy, strategy));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getTargetClassFilter() {
		return targetClassFilter;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTargetClassFilter(String newTargetClassFilter) {
		String oldTargetClassFilter = targetClassFilter;
		targetClassFilter = newTargetClassFilter;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DatagenPackage.REFERENCE_GEN_CONFIG__TARGET_CLASS_FILTER, oldTargetClassFilter, targetClassFilter));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getMinCount() {
		return minCount;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMinCount(int newMinCount) {
		int oldMinCount = minCount;
		minCount = newMinCount;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DatagenPackage.REFERENCE_GEN_CONFIG__MIN_COUNT, oldMinCount, minCount));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getMaxCount() {
		return maxCount;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMaxCount(int newMaxCount) {
		int oldMaxCount = maxCount;
		maxCount = newMaxCount;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DatagenPackage.REFERENCE_GEN_CONFIG__MAX_COUNT, oldMaxCount, maxCount));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case DatagenPackage.REFERENCE_GEN_CONFIG__FEATURE_NAME:
				return getFeatureName();
			case DatagenPackage.REFERENCE_GEN_CONFIG__STRATEGY:
				return getStrategy();
			case DatagenPackage.REFERENCE_GEN_CONFIG__TARGET_CLASS_FILTER:
				return getTargetClassFilter();
			case DatagenPackage.REFERENCE_GEN_CONFIG__MIN_COUNT:
				return getMinCount();
			case DatagenPackage.REFERENCE_GEN_CONFIG__MAX_COUNT:
				return getMaxCount();
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
			case DatagenPackage.REFERENCE_GEN_CONFIG__FEATURE_NAME:
				setFeatureName((String)newValue);
				return;
			case DatagenPackage.REFERENCE_GEN_CONFIG__STRATEGY:
				setStrategy((ReferenceStrategy)newValue);
				return;
			case DatagenPackage.REFERENCE_GEN_CONFIG__TARGET_CLASS_FILTER:
				setTargetClassFilter((String)newValue);
				return;
			case DatagenPackage.REFERENCE_GEN_CONFIG__MIN_COUNT:
				setMinCount((Integer)newValue);
				return;
			case DatagenPackage.REFERENCE_GEN_CONFIG__MAX_COUNT:
				setMaxCount((Integer)newValue);
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
			case DatagenPackage.REFERENCE_GEN_CONFIG__FEATURE_NAME:
				setFeatureName(FEATURE_NAME_EDEFAULT);
				return;
			case DatagenPackage.REFERENCE_GEN_CONFIG__STRATEGY:
				setStrategy(STRATEGY_EDEFAULT);
				return;
			case DatagenPackage.REFERENCE_GEN_CONFIG__TARGET_CLASS_FILTER:
				setTargetClassFilter(TARGET_CLASS_FILTER_EDEFAULT);
				return;
			case DatagenPackage.REFERENCE_GEN_CONFIG__MIN_COUNT:
				setMinCount(MIN_COUNT_EDEFAULT);
				return;
			case DatagenPackage.REFERENCE_GEN_CONFIG__MAX_COUNT:
				setMaxCount(MAX_COUNT_EDEFAULT);
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
			case DatagenPackage.REFERENCE_GEN_CONFIG__FEATURE_NAME:
				return FEATURE_NAME_EDEFAULT == null ? featureName != null : !FEATURE_NAME_EDEFAULT.equals(featureName);
			case DatagenPackage.REFERENCE_GEN_CONFIG__STRATEGY:
				return strategy != STRATEGY_EDEFAULT;
			case DatagenPackage.REFERENCE_GEN_CONFIG__TARGET_CLASS_FILTER:
				return TARGET_CLASS_FILTER_EDEFAULT == null ? targetClassFilter != null : !TARGET_CLASS_FILTER_EDEFAULT.equals(targetClassFilter);
			case DatagenPackage.REFERENCE_GEN_CONFIG__MIN_COUNT:
				return minCount != MIN_COUNT_EDEFAULT;
			case DatagenPackage.REFERENCE_GEN_CONFIG__MAX_COUNT:
				return maxCount != MAX_COUNT_EDEFAULT;
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
		result.append(" (featureName: ");
		result.append(featureName);
		result.append(", strategy: ");
		result.append(strategy);
		result.append(", targetClassFilter: ");
		result.append(targetClassFilter);
		result.append(", minCount: ");
		result.append(minCount);
		result.append(", maxCount: ");
		result.append(maxCount);
		result.append(')');
		return result.toString();
	}

} //ReferenceGenConfigImpl
