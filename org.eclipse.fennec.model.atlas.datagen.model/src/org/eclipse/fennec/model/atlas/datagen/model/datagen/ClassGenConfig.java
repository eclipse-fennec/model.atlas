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
package org.eclipse.fennec.model.atlas.datagen.model.datagen;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Class Gen Config</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Konfiguration fuer die Generierung von Instanzen einer bestimmten EClass.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ClassGenConfig#getContextClass <em>Context Class</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ClassGenConfig#getInstanceCount <em>Instance Count</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ClassGenConfig#isEnabled <em>Enabled</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ClassGenConfig#getAttributeGens <em>Attribute Gens</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ClassGenConfig#getReferenceGens <em>Reference Gens</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getClassGenConfig()
 * @model
 * @generated
 */
@ProviderType
public interface ClassGenConfig extends EObject {
	/**
	 * Returns the value of the '<em><b>Context Class</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Vollqualifizierter Name der EClass, also die EClass-URI.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Context Class</em>' attribute.
	 * @see #setContextClass(String)
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getClassGenConfig_ContextClass()
	 * @model required="true"
	 * @generated
	 */
	String getContextClass();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ClassGenConfig#getContextClass <em>Context Class</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Context Class</em>' attribute.
	 * @see #getContextClass()
	 * @generated
	 */
	void setContextClass(String value);

	/**
	 * Returns the value of the '<em><b>Instance Count</b></em>' attribute.
	 * The default value is <code>"10"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Anzahl der zu generierenden Instanzen.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Instance Count</em>' attribute.
	 * @see #setInstanceCount(int)
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getClassGenConfig_InstanceCount()
	 * @model default="10" required="true"
	 * @generated
	 */
	int getInstanceCount();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ClassGenConfig#getInstanceCount <em>Instance Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Instance Count</em>' attribute.
	 * @see #getInstanceCount()
	 * @generated
	 */
	void setInstanceCount(int value);

	/**
	 * Returns the value of the '<em><b>Enabled</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Ob die Generierung fuer diese Klasse aktiviert ist.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Enabled</em>' attribute.
	 * @see #setEnabled(boolean)
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getClassGenConfig_Enabled()
	 * @model default="true"
	 * @generated
	 */
	boolean isEnabled();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ClassGenConfig#isEnabled <em>Enabled</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Enabled</em>' attribute.
	 * @see #isEnabled()
	 * @generated
	 */
	void setEnabled(boolean value);

	/**
	 * Returns the value of the '<em><b>Attribute Gens</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.datagen.model.datagen.AttributeGenConfig}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Generierungs-Konfigurationen fuer Attribute.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Attribute Gens</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getClassGenConfig_AttributeGens()
	 * @model containment="true"
	 * @generated
	 */
	EList<AttributeGenConfig> getAttributeGens();

	/**
	 * Returns the value of the '<em><b>Reference Gens</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceGenConfig}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Generierungs-Konfigurationen fuer Referenzen.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Reference Gens</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getClassGenConfig_ReferenceGens()
	 * @model containment="true"
	 * @generated
	 */
	EList<ReferenceGenConfig> getReferenceGens();

} // ClassGenConfig
