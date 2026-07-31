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
package org.eclipse.fennec.model.atlas.datagen.model.datagen;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Data Gen Config</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Root-Container fuer eine Data-Generator-Konfiguration. Entspricht einer .datagen Datei.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig#getVersion <em>Version</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig#getSeed <em>Seed</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig#getLocale <em>Locale</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig#getTargetModelNsURIs <em>Target Model Ns UR Is</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig#getClassConfigs <em>Class Configs</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig#getCustomGenerators <em>Custom Generators</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getDataGenConfig()
 * @model
 * @generated
 */
@ProviderType
public interface DataGenConfig extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Name der Generierungs-Konfiguration.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getDataGenConfig_Name()
	 * @model required="true"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Version</b></em>' attribute.
	 * The default value is <code>"1.0"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Versionsnummer der Konfiguration.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Version</em>' attribute.
	 * @see #setVersion(String)
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getDataGenConfig_Version()
	 * @model default="1.0"
	 * @generated
	 */
	String getVersion();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig#getVersion <em>Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Version</em>' attribute.
	 * @see #getVersion()
	 * @generated
	 */
	void setVersion(String value);

	/**
	 * Returns the value of the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Optionale Beschreibung der Konfiguration.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getDataGenConfig_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Seed</b></em>' attribute.
	 * The default value is <code>"0"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Seed fuer den Zufallsgenerator. 0 = zufaelliger Seed.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Seed</em>' attribute.
	 * @see #setSeed(int)
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getDataGenConfig_Seed()
	 * @model default="0"
	 * @generated
	 */
	int getSeed();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig#getSeed <em>Seed</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Seed</em>' attribute.
	 * @see #getSeed()
	 * @generated
	 */
	void setSeed(int value);

	/**
	 * Returns the value of the '<em><b>Locale</b></em>' attribute.
	 * The default value is <code>"de"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Locale fuer FakerJS (z.B. de, en, fr).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Locale</em>' attribute.
	 * @see #setLocale(String)
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getDataGenConfig_Locale()
	 * @model default="de"
	 * @generated
	 */
	String getLocale();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.DataGenConfig#getLocale <em>Locale</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Locale</em>' attribute.
	 * @see #getLocale()
	 * @generated
	 */
	void setLocale(String value);

	/**
	 * Returns the value of the '<em><b>Target Model Ns UR Is</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Namespace URIs der Ziel-Metamodelle fuer die Generierung.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Target Model Ns UR Is</em>' attribute list.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getDataGenConfig_TargetModelNsURIs()
	 * @model
	 * @generated
	 */
	EList<String> getTargetModelNsURIs();

	/**
	 * Returns the value of the '<em><b>Class Configs</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ClassGenConfig}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Generierungs-Konfigurationen pro EClass.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Class Configs</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getDataGenConfig_ClassConfigs()
	 * @model containment="true"
	 * @generated
	 */
	EList<ClassGenConfig> getClassConfigs();

	/**
	 * Returns the value of the '<em><b>Custom Generators</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.datagen.model.datagen.CustomGeneratorDef}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Benutzerdefinierte Generator-Definitionen.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Custom Generators</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getDataGenConfig_CustomGenerators()
	 * @model containment="true"
	 * @generated
	 */
	EList<CustomGeneratorDef> getCustomGenerators();

} // DataGenConfig
