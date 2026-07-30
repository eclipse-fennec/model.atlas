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

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Reference Gen Config</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Konfiguration fuer die Generierung von Referenz-Zuweisungen.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceGenConfig#getFeatureName <em>Feature Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceGenConfig#getStrategy <em>Strategy</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceGenConfig#getTargetClassFilter <em>Target Class Filter</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceGenConfig#getMinCount <em>Min Count</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceGenConfig#getMaxCount <em>Max Count</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getReferenceGenConfig()
 * @model
 * @generated
 */
@ProviderType
public interface ReferenceGenConfig extends EObject {
	/**
	 * Returns the value of the '<em><b>Feature Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Name der Referenz in der EClass.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Feature Name</em>' attribute.
	 * @see #setFeatureName(String)
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getReferenceGenConfig_FeatureName()
	 * @model required="true"
	 * @generated
	 */
	String getFeatureName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceGenConfig#getFeatureName <em>Feature Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Feature Name</em>' attribute.
	 * @see #getFeatureName()
	 * @generated
	 */
	void setFeatureName(String value);

	/**
	 * Returns the value of the '<em><b>Strategy</b></em>' attribute.
	 * The default value is <code>"RANDOM"</code>.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceStrategy}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Strategie fuer die Auswahl der Referenz-Ziele.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Strategy</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceStrategy
	 * @see #setStrategy(ReferenceStrategy)
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getReferenceGenConfig_Strategy()
	 * @model default="RANDOM" required="true"
	 * @generated
	 */
	ReferenceStrategy getStrategy();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceGenConfig#getStrategy <em>Strategy</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Strategy</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceStrategy
	 * @see #getStrategy()
	 * @generated
	 */
	void setStrategy(ReferenceStrategy value);

	/**
	 * Returns the value of the '<em><b>Target Class Filter</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Optional: Filter auf eine bestimmte Ziel-Klasse.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Target Class Filter</em>' attribute.
	 * @see #setTargetClassFilter(String)
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getReferenceGenConfig_TargetClassFilter()
	 * @model
	 * @generated
	 */
	String getTargetClassFilter();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceGenConfig#getTargetClassFilter <em>Target Class Filter</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Target Class Filter</em>' attribute.
	 * @see #getTargetClassFilter()
	 * @generated
	 */
	void setTargetClassFilter(String value);

	/**
	 * Returns the value of the '<em><b>Min Count</b></em>' attribute.
	 * The default value is <code>"0"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Minimale Anzahl der zugewiesenen Referenzen.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Min Count</em>' attribute.
	 * @see #setMinCount(int)
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getReferenceGenConfig_MinCount()
	 * @model default="0"
	 * @generated
	 */
	int getMinCount();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceGenConfig#getMinCount <em>Min Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Min Count</em>' attribute.
	 * @see #getMinCount()
	 * @generated
	 */
	void setMinCount(int value);

	/**
	 * Returns the value of the '<em><b>Max Count</b></em>' attribute.
	 * The default value is <code>"1"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Maximale Anzahl der zugewiesenen Referenzen.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Max Count</em>' attribute.
	 * @see #setMaxCount(int)
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getReferenceGenConfig_MaxCount()
	 * @model default="1"
	 * @generated
	 */
	int getMaxCount();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.ReferenceGenConfig#getMaxCount <em>Max Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Max Count</em>' attribute.
	 * @see #getMaxCount()
	 * @generated
	 */
	void setMaxCount(int value);

} // ReferenceGenConfig
