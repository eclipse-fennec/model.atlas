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

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Ocl Constraint Set</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OclConstraintSetImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OclConstraintSetImpl#getVersion <em>Version</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OclConstraintSetImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OclConstraintSetImpl#getConstraints <em>Constraints</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OclConstraintSetImpl#getTargetModelNsURIs <em>Target Model Ns UR Is</em>}</li>
 * </ul>
 *
 * @generated
 */
public class OclConstraintSetImpl extends MinimalEObjectImpl.Container implements OclConstraintSet {
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
	 * The default value of the '{@link #getVersion() <em>Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVersion()
	 * @generated
	 * @ordered
	 */
	protected static final String VERSION_EDEFAULT = "1.0";

	/**
	 * The cached value of the '{@link #getVersion() <em>Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVersion()
	 * @generated
	 * @ordered
	 */
	protected String version = VERSION_EDEFAULT;

	/**
	 * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected static final String DESCRIPTION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected String description = DESCRIPTION_EDEFAULT;

	/**
	 * The cached value of the '{@link #getConstraints() <em>Constraints</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getConstraints()
	 * @generated
	 * @ordered
	 */
	protected EList<OclConstraint> constraints;

	/**
	 * The cached value of the '{@link #getTargetModelNsURIs() <em>Target Model Ns UR Is</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTargetModelNsURIs()
	 * @generated
	 * @ordered
	 */
	protected EList<String> targetModelNsURIs;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected OclConstraintSetImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return COCLPackage.Literals.OCL_CONSTRAINT_SET;
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
			eNotify(new ENotificationImpl(this, Notification.SET, COCLPackage.OCL_CONSTRAINT_SET__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getVersion() {
		return version;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setVersion(String newVersion) {
		String oldVersion = version;
		version = newVersion;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, COCLPackage.OCL_CONSTRAINT_SET__VERSION, oldVersion, version));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDescription(String newDescription) {
		String oldDescription = description;
		description = newDescription;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, COCLPackage.OCL_CONSTRAINT_SET__DESCRIPTION, oldDescription, description));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<OclConstraint> getConstraints() {
		if (constraints == null) {
			constraints = new EObjectContainmentEList<OclConstraint>(OclConstraint.class, this, COCLPackage.OCL_CONSTRAINT_SET__CONSTRAINTS);
		}
		return constraints;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getTargetModelNsURIs() {
		if (targetModelNsURIs == null) {
			targetModelNsURIs = new EDataTypeUniqueEList<String>(String.class, this, COCLPackage.OCL_CONSTRAINT_SET__TARGET_MODEL_NS_UR_IS);
		}
		return targetModelNsURIs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case COCLPackage.OCL_CONSTRAINT_SET__CONSTRAINTS:
				return ((InternalEList<?>)getConstraints()).basicRemove(otherEnd, msgs);
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
			case COCLPackage.OCL_CONSTRAINT_SET__NAME:
				return getName();
			case COCLPackage.OCL_CONSTRAINT_SET__VERSION:
				return getVersion();
			case COCLPackage.OCL_CONSTRAINT_SET__DESCRIPTION:
				return getDescription();
			case COCLPackage.OCL_CONSTRAINT_SET__CONSTRAINTS:
				return getConstraints();
			case COCLPackage.OCL_CONSTRAINT_SET__TARGET_MODEL_NS_UR_IS:
				return getTargetModelNsURIs();
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
			case COCLPackage.OCL_CONSTRAINT_SET__NAME:
				setName((String)newValue);
				return;
			case COCLPackage.OCL_CONSTRAINT_SET__VERSION:
				setVersion((String)newValue);
				return;
			case COCLPackage.OCL_CONSTRAINT_SET__DESCRIPTION:
				setDescription((String)newValue);
				return;
			case COCLPackage.OCL_CONSTRAINT_SET__CONSTRAINTS:
				getConstraints().clear();
				getConstraints().addAll((Collection<? extends OclConstraint>)newValue);
				return;
			case COCLPackage.OCL_CONSTRAINT_SET__TARGET_MODEL_NS_UR_IS:
				getTargetModelNsURIs().clear();
				getTargetModelNsURIs().addAll((Collection<? extends String>)newValue);
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
			case COCLPackage.OCL_CONSTRAINT_SET__NAME:
				setName(NAME_EDEFAULT);
				return;
			case COCLPackage.OCL_CONSTRAINT_SET__VERSION:
				setVersion(VERSION_EDEFAULT);
				return;
			case COCLPackage.OCL_CONSTRAINT_SET__DESCRIPTION:
				setDescription(DESCRIPTION_EDEFAULT);
				return;
			case COCLPackage.OCL_CONSTRAINT_SET__CONSTRAINTS:
				getConstraints().clear();
				return;
			case COCLPackage.OCL_CONSTRAINT_SET__TARGET_MODEL_NS_UR_IS:
				getTargetModelNsURIs().clear();
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
			case COCLPackage.OCL_CONSTRAINT_SET__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case COCLPackage.OCL_CONSTRAINT_SET__VERSION:
				return VERSION_EDEFAULT == null ? version != null : !VERSION_EDEFAULT.equals(version);
			case COCLPackage.OCL_CONSTRAINT_SET__DESCRIPTION:
				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
			case COCLPackage.OCL_CONSTRAINT_SET__CONSTRAINTS:
				return constraints != null && !constraints.isEmpty();
			case COCLPackage.OCL_CONSTRAINT_SET__TARGET_MODEL_NS_UR_IS:
				return targetModelNsURIs != null && !targetModelNsURIs.isEmpty();
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
		result.append(", version: ");
		result.append(version);
		result.append(", description: ");
		result.append(description);
		result.append(", targetModelNsURIs: ");
		result.append(targetModelNsURIs);
		result.append(')');
		return result.toString();
	}

} //OclConstraintSetImpl
