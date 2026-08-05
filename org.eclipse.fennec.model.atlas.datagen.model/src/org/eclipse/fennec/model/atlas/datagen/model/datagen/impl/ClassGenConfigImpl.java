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
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.datagen.model.datagen.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.ClassGenConfig;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceGenConfig;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Class Gen Config</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.ClassGenConfigImpl#getContextClass <em>Context Class</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.ClassGenConfigImpl#getInstanceCount <em>Instance Count</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.ClassGenConfigImpl#isEnabled <em>Enabled</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.ClassGenConfigImpl#getAttributeGens <em>Attribute Gens</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.ClassGenConfigImpl#getReferenceGens <em>Reference Gens</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ClassGenConfigImpl extends MinimalEObjectImpl.Container implements ClassGenConfig {
	/**
	 * The default value of the '{@link #getContextClass() <em>Context Class</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContextClass()
	 * @generated
	 * @ordered
	 */
	protected static final String CONTEXT_CLASS_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getContextClass() <em>Context Class</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContextClass()
	 * @generated
	 * @ordered
	 */
	protected String contextClass = CONTEXT_CLASS_EDEFAULT;

	/**
	 * The default value of the '{@link #getInstanceCount() <em>Instance Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getInstanceCount()
	 * @generated
	 * @ordered
	 */
	protected static final int INSTANCE_COUNT_EDEFAULT = 10;

	/**
	 * The cached value of the '{@link #getInstanceCount() <em>Instance Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getInstanceCount()
	 * @generated
	 * @ordered
	 */
	protected int instanceCount = INSTANCE_COUNT_EDEFAULT;

	/**
	 * The default value of the '{@link #isEnabled() <em>Enabled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isEnabled()
	 * @generated
	 * @ordered
	 */
	protected static final boolean ENABLED_EDEFAULT = true;

	/**
	 * The cached value of the '{@link #isEnabled() <em>Enabled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isEnabled()
	 * @generated
	 * @ordered
	 */
	protected boolean enabled = ENABLED_EDEFAULT;

	/**
	 * The cached value of the '{@link #getAttributeGens() <em>Attribute Gens</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAttributeGens()
	 * @generated
	 * @ordered
	 */
	protected EList<AttributeGenConfig> attributeGens;

	/**
	 * The cached value of the '{@link #getReferenceGens() <em>Reference Gens</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getReferenceGens()
	 * @generated
	 * @ordered
	 */
	protected EList<ReferenceGenConfig> referenceGens;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ClassGenConfigImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return DatagenPackage.Literals.CLASS_GEN_CONFIG;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getContextClass() {
		return contextClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setContextClass(String newContextClass) {
		String oldContextClass = contextClass;
		contextClass = newContextClass;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DatagenPackage.CLASS_GEN_CONFIG__CONTEXT_CLASS, oldContextClass, contextClass));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getInstanceCount() {
		return instanceCount;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setInstanceCount(int newInstanceCount) {
		int oldInstanceCount = instanceCount;
		instanceCount = newInstanceCount;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DatagenPackage.CLASS_GEN_CONFIG__INSTANCE_COUNT, oldInstanceCount, instanceCount));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isEnabled() {
		return enabled;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setEnabled(boolean newEnabled) {
		boolean oldEnabled = enabled;
		enabled = newEnabled;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DatagenPackage.CLASS_GEN_CONFIG__ENABLED, oldEnabled, enabled));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<AttributeGenConfig> getAttributeGens() {
		if (attributeGens == null) {
			attributeGens = new EObjectContainmentEList<AttributeGenConfig>(AttributeGenConfig.class, this, DatagenPackage.CLASS_GEN_CONFIG__ATTRIBUTE_GENS);
		}
		return attributeGens;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<ReferenceGenConfig> getReferenceGens() {
		if (referenceGens == null) {
			referenceGens = new EObjectContainmentEList<ReferenceGenConfig>(ReferenceGenConfig.class, this, DatagenPackage.CLASS_GEN_CONFIG__REFERENCE_GENS);
		}
		return referenceGens;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case DatagenPackage.CLASS_GEN_CONFIG__ATTRIBUTE_GENS:
				return ((InternalEList<?>)getAttributeGens()).basicRemove(otherEnd, msgs);
			case DatagenPackage.CLASS_GEN_CONFIG__REFERENCE_GENS:
				return ((InternalEList<?>)getReferenceGens()).basicRemove(otherEnd, msgs);
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
			case DatagenPackage.CLASS_GEN_CONFIG__CONTEXT_CLASS:
				return getContextClass();
			case DatagenPackage.CLASS_GEN_CONFIG__INSTANCE_COUNT:
				return getInstanceCount();
			case DatagenPackage.CLASS_GEN_CONFIG__ENABLED:
				return isEnabled();
			case DatagenPackage.CLASS_GEN_CONFIG__ATTRIBUTE_GENS:
				return getAttributeGens();
			case DatagenPackage.CLASS_GEN_CONFIG__REFERENCE_GENS:
				return getReferenceGens();
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
			case DatagenPackage.CLASS_GEN_CONFIG__CONTEXT_CLASS:
				setContextClass((String)newValue);
				return;
			case DatagenPackage.CLASS_GEN_CONFIG__INSTANCE_COUNT:
				setInstanceCount((Integer)newValue);
				return;
			case DatagenPackage.CLASS_GEN_CONFIG__ENABLED:
				setEnabled((Boolean)newValue);
				return;
			case DatagenPackage.CLASS_GEN_CONFIG__ATTRIBUTE_GENS:
				getAttributeGens().clear();
				getAttributeGens().addAll((Collection<? extends AttributeGenConfig>)newValue);
				return;
			case DatagenPackage.CLASS_GEN_CONFIG__REFERENCE_GENS:
				getReferenceGens().clear();
				getReferenceGens().addAll((Collection<? extends ReferenceGenConfig>)newValue);
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
			case DatagenPackage.CLASS_GEN_CONFIG__CONTEXT_CLASS:
				setContextClass(CONTEXT_CLASS_EDEFAULT);
				return;
			case DatagenPackage.CLASS_GEN_CONFIG__INSTANCE_COUNT:
				setInstanceCount(INSTANCE_COUNT_EDEFAULT);
				return;
			case DatagenPackage.CLASS_GEN_CONFIG__ENABLED:
				setEnabled(ENABLED_EDEFAULT);
				return;
			case DatagenPackage.CLASS_GEN_CONFIG__ATTRIBUTE_GENS:
				getAttributeGens().clear();
				return;
			case DatagenPackage.CLASS_GEN_CONFIG__REFERENCE_GENS:
				getReferenceGens().clear();
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
			case DatagenPackage.CLASS_GEN_CONFIG__CONTEXT_CLASS:
				return CONTEXT_CLASS_EDEFAULT == null ? contextClass != null : !CONTEXT_CLASS_EDEFAULT.equals(contextClass);
			case DatagenPackage.CLASS_GEN_CONFIG__INSTANCE_COUNT:
				return instanceCount != INSTANCE_COUNT_EDEFAULT;
			case DatagenPackage.CLASS_GEN_CONFIG__ENABLED:
				return enabled != ENABLED_EDEFAULT;
			case DatagenPackage.CLASS_GEN_CONFIG__ATTRIBUTE_GENS:
				return attributeGens != null && !attributeGens.isEmpty();
			case DatagenPackage.CLASS_GEN_CONFIG__REFERENCE_GENS:
				return referenceGens != null && !referenceGens.isEmpty();
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
		result.append(" (contextClass: ");
		result.append(contextClass);
		result.append(", instanceCount: ");
		result.append(instanceCount);
		result.append(", enabled: ");
		result.append(enabled);
		result.append(')');
		return result.toString();
	}

} //ClassGenConfigImpl
