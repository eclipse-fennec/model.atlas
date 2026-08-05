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

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.model.atlas.datagen.model.datagen.ClassGenConfig;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.CustomGeneratorDef;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig;
import org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Data Gen Config</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DataGenConfigImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DataGenConfigImpl#getVersion <em>Version</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DataGenConfigImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DataGenConfigImpl#getSeed <em>Seed</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DataGenConfigImpl#getLocale <em>Locale</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DataGenConfigImpl#getTargetModelNsURIs <em>Target Model Ns UR Is</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DataGenConfigImpl#getClassConfigs <em>Class Configs</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DataGenConfigImpl#getCustomGenerators <em>Custom Generators</em>}</li>
 * </ul>
 *
 * @generated
 */
public class DataGenConfigImpl extends MinimalEObjectImpl.Container implements DataGenConfig {
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
	 * The default value of the '{@link #getSeed() <em>Seed</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSeed()
	 * @generated
	 * @ordered
	 */
	protected static final int SEED_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getSeed() <em>Seed</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSeed()
	 * @generated
	 * @ordered
	 */
	protected int seed = SEED_EDEFAULT;

	/**
	 * The default value of the '{@link #getLocale() <em>Locale</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocale()
	 * @generated
	 * @ordered
	 */
	protected static final String LOCALE_EDEFAULT = "de";

	/**
	 * The cached value of the '{@link #getLocale() <em>Locale</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLocale()
	 * @generated
	 * @ordered
	 */
	protected String locale = LOCALE_EDEFAULT;

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
	 * The cached value of the '{@link #getClassConfigs() <em>Class Configs</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getClassConfigs()
	 * @generated
	 * @ordered
	 */
	protected EList<ClassGenConfig> classConfigs;

	/**
	 * The cached value of the '{@link #getCustomGenerators() <em>Custom Generators</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCustomGenerators()
	 * @generated
	 * @ordered
	 */
	protected EList<CustomGeneratorDef> customGenerators;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DataGenConfigImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return DatagenPackage.Literals.DATA_GEN_CONFIG;
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
			eNotify(new ENotificationImpl(this, Notification.SET, DatagenPackage.DATA_GEN_CONFIG__NAME, oldName, name));
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
			eNotify(new ENotificationImpl(this, Notification.SET, DatagenPackage.DATA_GEN_CONFIG__VERSION, oldVersion, version));
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
			eNotify(new ENotificationImpl(this, Notification.SET, DatagenPackage.DATA_GEN_CONFIG__DESCRIPTION, oldDescription, description));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getSeed() {
		return seed;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSeed(int newSeed) {
		int oldSeed = seed;
		seed = newSeed;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DatagenPackage.DATA_GEN_CONFIG__SEED, oldSeed, seed));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getLocale() {
		return locale;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLocale(String newLocale) {
		String oldLocale = locale;
		locale = newLocale;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DatagenPackage.DATA_GEN_CONFIG__LOCALE, oldLocale, locale));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getTargetModelNsURIs() {
		if (targetModelNsURIs == null) {
			targetModelNsURIs = new EDataTypeUniqueEList<String>(String.class, this, DatagenPackage.DATA_GEN_CONFIG__TARGET_MODEL_NS_UR_IS);
		}
		return targetModelNsURIs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<ClassGenConfig> getClassConfigs() {
		if (classConfigs == null) {
			classConfigs = new EObjectContainmentEList<ClassGenConfig>(ClassGenConfig.class, this, DatagenPackage.DATA_GEN_CONFIG__CLASS_CONFIGS);
		}
		return classConfigs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<CustomGeneratorDef> getCustomGenerators() {
		if (customGenerators == null) {
			customGenerators = new EObjectContainmentEList<CustomGeneratorDef>(CustomGeneratorDef.class, this, DatagenPackage.DATA_GEN_CONFIG__CUSTOM_GENERATORS);
		}
		return customGenerators;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case DatagenPackage.DATA_GEN_CONFIG__CLASS_CONFIGS:
				return ((InternalEList<?>)getClassConfigs()).basicRemove(otherEnd, msgs);
			case DatagenPackage.DATA_GEN_CONFIG__CUSTOM_GENERATORS:
				return ((InternalEList<?>)getCustomGenerators()).basicRemove(otherEnd, msgs);
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
			case DatagenPackage.DATA_GEN_CONFIG__NAME:
				return getName();
			case DatagenPackage.DATA_GEN_CONFIG__VERSION:
				return getVersion();
			case DatagenPackage.DATA_GEN_CONFIG__DESCRIPTION:
				return getDescription();
			case DatagenPackage.DATA_GEN_CONFIG__SEED:
				return getSeed();
			case DatagenPackage.DATA_GEN_CONFIG__LOCALE:
				return getLocale();
			case DatagenPackage.DATA_GEN_CONFIG__TARGET_MODEL_NS_UR_IS:
				return getTargetModelNsURIs();
			case DatagenPackage.DATA_GEN_CONFIG__CLASS_CONFIGS:
				return getClassConfigs();
			case DatagenPackage.DATA_GEN_CONFIG__CUSTOM_GENERATORS:
				return getCustomGenerators();
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
			case DatagenPackage.DATA_GEN_CONFIG__NAME:
				setName((String)newValue);
				return;
			case DatagenPackage.DATA_GEN_CONFIG__VERSION:
				setVersion((String)newValue);
				return;
			case DatagenPackage.DATA_GEN_CONFIG__DESCRIPTION:
				setDescription((String)newValue);
				return;
			case DatagenPackage.DATA_GEN_CONFIG__SEED:
				setSeed((Integer)newValue);
				return;
			case DatagenPackage.DATA_GEN_CONFIG__LOCALE:
				setLocale((String)newValue);
				return;
			case DatagenPackage.DATA_GEN_CONFIG__TARGET_MODEL_NS_UR_IS:
				getTargetModelNsURIs().clear();
				getTargetModelNsURIs().addAll((Collection<? extends String>)newValue);
				return;
			case DatagenPackage.DATA_GEN_CONFIG__CLASS_CONFIGS:
				getClassConfigs().clear();
				getClassConfigs().addAll((Collection<? extends ClassGenConfig>)newValue);
				return;
			case DatagenPackage.DATA_GEN_CONFIG__CUSTOM_GENERATORS:
				getCustomGenerators().clear();
				getCustomGenerators().addAll((Collection<? extends CustomGeneratorDef>)newValue);
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
			case DatagenPackage.DATA_GEN_CONFIG__NAME:
				setName(NAME_EDEFAULT);
				return;
			case DatagenPackage.DATA_GEN_CONFIG__VERSION:
				setVersion(VERSION_EDEFAULT);
				return;
			case DatagenPackage.DATA_GEN_CONFIG__DESCRIPTION:
				setDescription(DESCRIPTION_EDEFAULT);
				return;
			case DatagenPackage.DATA_GEN_CONFIG__SEED:
				setSeed(SEED_EDEFAULT);
				return;
			case DatagenPackage.DATA_GEN_CONFIG__LOCALE:
				setLocale(LOCALE_EDEFAULT);
				return;
			case DatagenPackage.DATA_GEN_CONFIG__TARGET_MODEL_NS_UR_IS:
				getTargetModelNsURIs().clear();
				return;
			case DatagenPackage.DATA_GEN_CONFIG__CLASS_CONFIGS:
				getClassConfigs().clear();
				return;
			case DatagenPackage.DATA_GEN_CONFIG__CUSTOM_GENERATORS:
				getCustomGenerators().clear();
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
			case DatagenPackage.DATA_GEN_CONFIG__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case DatagenPackage.DATA_GEN_CONFIG__VERSION:
				return VERSION_EDEFAULT == null ? version != null : !VERSION_EDEFAULT.equals(version);
			case DatagenPackage.DATA_GEN_CONFIG__DESCRIPTION:
				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
			case DatagenPackage.DATA_GEN_CONFIG__SEED:
				return seed != SEED_EDEFAULT;
			case DatagenPackage.DATA_GEN_CONFIG__LOCALE:
				return LOCALE_EDEFAULT == null ? locale != null : !LOCALE_EDEFAULT.equals(locale);
			case DatagenPackage.DATA_GEN_CONFIG__TARGET_MODEL_NS_UR_IS:
				return targetModelNsURIs != null && !targetModelNsURIs.isEmpty();
			case DatagenPackage.DATA_GEN_CONFIG__CLASS_CONFIGS:
				return classConfigs != null && !classConfigs.isEmpty();
			case DatagenPackage.DATA_GEN_CONFIG__CUSTOM_GENERATORS:
				return customGenerators != null && !customGenerators.isEmpty();
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
		result.append(", seed: ");
		result.append(seed);
		result.append(", locale: ");
		result.append(locale);
		result.append(", targetModelNsURIs: ");
		result.append(targetModelNsURIs);
		result.append(')');
		return result.toString();
	}

} //DataGenConfigImpl
