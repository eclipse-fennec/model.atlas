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
 *       Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.datagen.example.model.dge.impl;

import java.lang.reflect.InvocationTargetException;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.BasicEList;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.WrappedException;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EOperation;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectResolvingEList;

import org.eclipse.fennec.model.atlas.datagen.example.model.dge.Address;
import org.eclipse.fennec.model.atlas.datagen.example.model.dge.Company;
import org.eclipse.fennec.model.atlas.datagen.example.model.dge.DGPackage;
import org.eclipse.fennec.model.atlas.datagen.example.model.dge.Person;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Company</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.impl.CompanyImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.impl.CompanyImpl#getIndustry <em>Industry</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.impl.CompanyImpl#getUrl <em>Url</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.impl.CompanyImpl#getAddress <em>Address</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.impl.CompanyImpl#getEmployees <em>Employees</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.impl.CompanyImpl#getEmployeesNames <em>Employees Names</em>}</li>
 * </ul>
 *
 * @generated
 */
public class CompanyImpl extends MinimalEObjectImpl.Container implements Company {
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
	 * The default value of the '{@link #getIndustry() <em>Industry</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIndustry()
	 * @generated
	 * @ordered
	 */
	protected static final String INDUSTRY_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getIndustry() <em>Industry</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getIndustry()
	 * @generated
	 * @ordered
	 */
	protected String industry = INDUSTRY_EDEFAULT;

	/**
	 * The default value of the '{@link #getUrl() <em>Url</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUrl()
	 * @generated
	 * @ordered
	 */
	protected static final String URL_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getUrl() <em>Url</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getUrl()
	 * @generated
	 * @ordered
	 */
	protected String url = URL_EDEFAULT;

	/**
	 * The cached value of the '{@link #getAddress() <em>Address</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAddress()
	 * @generated
	 * @ordered
	 */
	protected Address address;

	/**
	 * The cached value of the '{@link #getEmployees() <em>Employees</em>}' reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEmployees()
	 * @generated
	 * @ordered
	 */
	protected EList<Person> employees;

	/**
	 * The cached setting delegate for the '{@link #getEmployeesNames() <em>Employees Names</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEmployeesNames()
	 * @generated
	 * @ordered
	 */
	protected EStructuralFeature.Internal.SettingDelegate EMPLOYEES_NAMES__ESETTING_DELEGATE = ((EStructuralFeature.Internal)DGPackage.Literals.COMPANY__EMPLOYEES_NAMES).getSettingDelegate();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected CompanyImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return DGPackage.Literals.COMPANY;
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
			eNotify(new ENotificationImpl(this, Notification.SET, DGPackage.COMPANY__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getIndustry() {
		return industry;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setIndustry(String newIndustry) {
		String oldIndustry = industry;
		industry = newIndustry;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DGPackage.COMPANY__INDUSTRY, oldIndustry, industry));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getUrl() {
		return url;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setUrl(String newUrl) {
		String oldUrl = url;
		url = newUrl;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DGPackage.COMPANY__URL, oldUrl, url));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Address getAddress() {
		return address;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetAddress(Address newAddress, NotificationChain msgs) {
		Address oldAddress = address;
		address = newAddress;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, DGPackage.COMPANY__ADDRESS, oldAddress, newAddress);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAddress(Address newAddress) {
		if (newAddress != address) {
			NotificationChain msgs = null;
			if (address != null)
				msgs = ((InternalEObject)address).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - DGPackage.COMPANY__ADDRESS, null, msgs);
			if (newAddress != null)
				msgs = ((InternalEObject)newAddress).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - DGPackage.COMPANY__ADDRESS, null, msgs);
			msgs = basicSetAddress(newAddress, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DGPackage.COMPANY__ADDRESS, newAddress, newAddress));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Person> getEmployees() {
		if (employees == null) {
			employees = new EObjectResolvingEList<Person>(Person.class, this, DGPackage.COMPANY__EMPLOYEES);
		}
		return employees;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public EList<String> getEmployeesNames() {
		return (EList<String>)EMPLOYEES_NAMES__ESETTING_DELEGATE.dynamicGet(this, null, 0, true, false);
	}

	/**
	 * The cached invocation delegate for the '{@link #findEmployeesByNamePrefix(java.lang.String) <em>Find Employees By Name Prefix</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #findEmployeesByNamePrefix(java.lang.String)
	 * @generated
	 * @ordered
	 */
	protected static final EOperation.Internal.InvocationDelegate FIND_EMPLOYEES_BY_NAME_PREFIX_STRING__EINVOCATION_DELEGATE = ((EOperation.Internal)DGPackage.Literals.COMPANY___FIND_EMPLOYEES_BY_NAME_PREFIX__STRING).getInvocationDelegate();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public EList<Person> findEmployeesByNamePrefix(String namePrefix) {
		try {
			return (EList<Person>)FIND_EMPLOYEES_BY_NAME_PREFIX_STRING__EINVOCATION_DELEGATE.dynamicInvoke(this, new BasicEList.UnmodifiableEList<Object>(1, new Object[]{namePrefix}));
		}
		catch (InvocationTargetException ite) {
			throw new WrappedException(ite);
		}
	}

	/**
	 * The cached invocation delegate for the '{@link #getTotalEmployees() <em>Get Total Employees</em>}' operation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTotalEmployees()
	 * @generated
	 * @ordered
	 */
	protected static final EOperation.Internal.InvocationDelegate GET_TOTAL_EMPLOYEES__EINVOCATION_DELEGATE = ((EOperation.Internal)DGPackage.Literals.COMPANY___GET_TOTAL_EMPLOYEES).getInvocationDelegate();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getTotalEmployees() {
		try {
			return (Integer)GET_TOTAL_EMPLOYEES__EINVOCATION_DELEGATE.dynamicInvoke(this, null);
		}
		catch (InvocationTargetException ite) {
			throw new WrappedException(ite);
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case DGPackage.COMPANY__ADDRESS:
				return basicSetAddress(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case DGPackage.COMPANY__NAME:
				return getName();
			case DGPackage.COMPANY__INDUSTRY:
				return getIndustry();
			case DGPackage.COMPANY__URL:
				return getUrl();
			case DGPackage.COMPANY__ADDRESS:
				return getAddress();
			case DGPackage.COMPANY__EMPLOYEES:
				return getEmployees();
			case DGPackage.COMPANY__EMPLOYEES_NAMES:
				return getEmployeesNames();
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
			case DGPackage.COMPANY__NAME:
				setName((String)newValue);
				return;
			case DGPackage.COMPANY__INDUSTRY:
				setIndustry((String)newValue);
				return;
			case DGPackage.COMPANY__URL:
				setUrl((String)newValue);
				return;
			case DGPackage.COMPANY__ADDRESS:
				setAddress((Address)newValue);
				return;
			case DGPackage.COMPANY__EMPLOYEES:
				getEmployees().clear();
				getEmployees().addAll((Collection<? extends Person>)newValue);
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
			case DGPackage.COMPANY__NAME:
				setName(NAME_EDEFAULT);
				return;
			case DGPackage.COMPANY__INDUSTRY:
				setIndustry(INDUSTRY_EDEFAULT);
				return;
			case DGPackage.COMPANY__URL:
				setUrl(URL_EDEFAULT);
				return;
			case DGPackage.COMPANY__ADDRESS:
				setAddress((Address)null);
				return;
			case DGPackage.COMPANY__EMPLOYEES:
				getEmployees().clear();
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
			case DGPackage.COMPANY__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case DGPackage.COMPANY__INDUSTRY:
				return INDUSTRY_EDEFAULT == null ? industry != null : !INDUSTRY_EDEFAULT.equals(industry);
			case DGPackage.COMPANY__URL:
				return URL_EDEFAULT == null ? url != null : !URL_EDEFAULT.equals(url);
			case DGPackage.COMPANY__ADDRESS:
				return address != null;
			case DGPackage.COMPANY__EMPLOYEES:
				return employees != null && !employees.isEmpty();
			case DGPackage.COMPANY__EMPLOYEES_NAMES:
				return EMPLOYEES_NAMES__ESETTING_DELEGATE.dynamicIsSet(this, null, 0);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException {
		switch (operationID) {
			case DGPackage.COMPANY___FIND_EMPLOYEES_BY_NAME_PREFIX__STRING:
				return findEmployeesByNamePrefix((String)arguments.get(0));
			case DGPackage.COMPANY___GET_TOTAL_EMPLOYEES:
				return getTotalEmployees();
		}
		return super.eInvoke(operationID, arguments);
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
		result.append(", industry: ");
		result.append(industry);
		result.append(", url: ");
		result.append(url);
		result.append(')');
		return result.toString();
	}

} //CompanyImpl
