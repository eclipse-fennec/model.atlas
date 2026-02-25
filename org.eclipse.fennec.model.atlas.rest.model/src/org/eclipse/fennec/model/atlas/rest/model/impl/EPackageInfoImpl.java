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
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.rest.model.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.fennec.model.atlas.rest.model.EPackageInfo;
import org.eclipse.fennec.model.atlas.rest.model.RestPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>EPackage Info</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.impl.EPackageInfoImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.impl.EPackageInfoImpl#getNsURI <em>Ns URI</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.impl.EPackageInfoImpl#getNsPrefix <em>Ns Prefix</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.impl.EPackageInfoImpl#getClassifierCount <em>Classifier Count</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.impl.EPackageInfoImpl#getSubPackageCount <em>Sub Package Count</em>}</li>
 * </ul>
 *
 * @generated
 */
public class EPackageInfoImpl extends MinimalEObjectImpl.Container implements EPackageInfo {
	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getNsURI() <em>Ns URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNsURI()
	 * @generated
	 * @ordered
	 */
	protected static final String NS_URI_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getNsURI() <em>Ns URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNsURI()
	 * @generated
	 * @ordered
	 */
	protected String nsURI = NS_URI_EDEFAULT;

	/**
	 * The default value of the '{@link #getNsPrefix() <em>Ns Prefix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNsPrefix()
	 * @generated
	 * @ordered
	 */
	protected static final String NS_PREFIX_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getNsPrefix() <em>Ns Prefix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNsPrefix()
	 * @generated
	 * @ordered
	 */
	protected String nsPrefix = NS_PREFIX_EDEFAULT;

	/**
	 * The default value of the '{@link #getClassifierCount() <em>Classifier Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getClassifierCount()
	 * @generated
	 * @ordered
	 */
	protected static final int CLASSIFIER_COUNT_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getClassifierCount() <em>Classifier Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getClassifierCount()
	 * @generated
	 * @ordered
	 */
	protected int classifierCount = CLASSIFIER_COUNT_EDEFAULT;

	/**
	 * The default value of the '{@link #getSubPackageCount() <em>Sub Package Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSubPackageCount()
	 * @generated
	 * @ordered
	 */
	protected static final int SUB_PACKAGE_COUNT_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getSubPackageCount() <em>Sub Package Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSubPackageCount()
	 * @generated
	 * @ordered
	 */
	protected int subPackageCount = SUB_PACKAGE_COUNT_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected EPackageInfoImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RestPackage.Literals.EPACKAGE_INFO;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RestPackage.EPACKAGE_INFO__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getNsURI() {
		return nsURI;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setNsURI(String newNsURI) {
		String oldNsURI = nsURI;
		nsURI = newNsURI;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RestPackage.EPACKAGE_INFO__NS_URI, oldNsURI, nsURI));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getNsPrefix() {
		return nsPrefix;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setNsPrefix(String newNsPrefix) {
		String oldNsPrefix = nsPrefix;
		nsPrefix = newNsPrefix;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RestPackage.EPACKAGE_INFO__NS_PREFIX, oldNsPrefix, nsPrefix));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getClassifierCount() {
		return classifierCount;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setClassifierCount(int newClassifierCount) {
		int oldClassifierCount = classifierCount;
		classifierCount = newClassifierCount;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RestPackage.EPACKAGE_INFO__CLASSIFIER_COUNT, oldClassifierCount, classifierCount));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getSubPackageCount() {
		return subPackageCount;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSubPackageCount(int newSubPackageCount) {
		int oldSubPackageCount = subPackageCount;
		subPackageCount = newSubPackageCount;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, RestPackage.EPACKAGE_INFO__SUB_PACKAGE_COUNT, oldSubPackageCount, subPackageCount));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case RestPackage.EPACKAGE_INFO__NAME:
				return getName();
			case RestPackage.EPACKAGE_INFO__NS_URI:
				return getNsURI();
			case RestPackage.EPACKAGE_INFO__NS_PREFIX:
				return getNsPrefix();
			case RestPackage.EPACKAGE_INFO__CLASSIFIER_COUNT:
				return getClassifierCount();
			case RestPackage.EPACKAGE_INFO__SUB_PACKAGE_COUNT:
				return getSubPackageCount();
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
			case RestPackage.EPACKAGE_INFO__NAME:
				setName((String)newValue);
				return;
			case RestPackage.EPACKAGE_INFO__NS_URI:
				setNsURI((String)newValue);
				return;
			case RestPackage.EPACKAGE_INFO__NS_PREFIX:
				setNsPrefix((String)newValue);
				return;
			case RestPackage.EPACKAGE_INFO__CLASSIFIER_COUNT:
				setClassifierCount((Integer)newValue);
				return;
			case RestPackage.EPACKAGE_INFO__SUB_PACKAGE_COUNT:
				setSubPackageCount((Integer)newValue);
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
			case RestPackage.EPACKAGE_INFO__NAME:
				setName(NAME_EDEFAULT);
				return;
			case RestPackage.EPACKAGE_INFO__NS_URI:
				setNsURI(NS_URI_EDEFAULT);
				return;
			case RestPackage.EPACKAGE_INFO__NS_PREFIX:
				setNsPrefix(NS_PREFIX_EDEFAULT);
				return;
			case RestPackage.EPACKAGE_INFO__CLASSIFIER_COUNT:
				setClassifierCount(CLASSIFIER_COUNT_EDEFAULT);
				return;
			case RestPackage.EPACKAGE_INFO__SUB_PACKAGE_COUNT:
				setSubPackageCount(SUB_PACKAGE_COUNT_EDEFAULT);
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
			case RestPackage.EPACKAGE_INFO__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case RestPackage.EPACKAGE_INFO__NS_URI:
				return NS_URI_EDEFAULT == null ? nsURI != null : !NS_URI_EDEFAULT.equals(nsURI);
			case RestPackage.EPACKAGE_INFO__NS_PREFIX:
				return NS_PREFIX_EDEFAULT == null ? nsPrefix != null : !NS_PREFIX_EDEFAULT.equals(nsPrefix);
			case RestPackage.EPACKAGE_INFO__CLASSIFIER_COUNT:
				return classifierCount != CLASSIFIER_COUNT_EDEFAULT;
			case RestPackage.EPACKAGE_INFO__SUB_PACKAGE_COUNT:
				return subPackageCount != SUB_PACKAGE_COUNT_EDEFAULT;
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
		result.append(" (name: ");
		result.append(name);
		result.append(", nsURI: ");
		result.append(nsURI);
		result.append(", nsPrefix: ");
		result.append(nsPrefix);
		result.append(", classifierCount: ");
		result.append(classifierCount);
		result.append(", subPackageCount: ");
		result.append(subPackageCount);
		result.append(')');
		return result.toString();
	}

} //EPackageInfoImpl
