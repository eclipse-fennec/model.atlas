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

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Custom Generator Def</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Benutzerdefinierte Generator-Definition mit JS-Expression.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.CustomGeneratorDef#getKey <em>Key</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.CustomGeneratorDef#getLabel <em>Label</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.CustomGeneratorDef#getExpression <em>Expression</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.CustomGeneratorDef#getCategory <em>Category</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getCustomGeneratorDef()
 * @model
 * @generated
 */
@ProviderType
public interface CustomGeneratorDef extends EObject {
	/**
	 * Returns the value of the '<em><b>Key</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Eindeutiger Schluessel des Generators (z.B. custom.fullAddress).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Key</em>' attribute.
	 * @see #setKey(String)
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getCustomGeneratorDef_Key()
	 * @model required="true"
	 * @generated
	 */
	String getKey();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.CustomGeneratorDef#getKey <em>Key</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Key</em>' attribute.
	 * @see #getKey()
	 * @generated
	 */
	void setKey(String value);

	/**
	 * Returns the value of the '<em><b>Label</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Anzeigename des Generators.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Label</em>' attribute.
	 * @see #setLabel(String)
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getCustomGeneratorDef_Label()
	 * @model required="true"
	 * @generated
	 */
	String getLabel();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.CustomGeneratorDef#getLabel <em>Label</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Label</em>' attribute.
	 * @see #getLabel()
	 * @generated
	 */
	void setLabel(String value);

	/**
	 * Returns the value of the '<em><b>Expression</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * JavaScript-Expression. Erhaelt faker und index als Variablen.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Expression</em>' attribute.
	 * @see #setExpression(String)
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getCustomGeneratorDef_Expression()
	 * @model required="true"
	 * @generated
	 */
	String getExpression();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.CustomGeneratorDef#getExpression <em>Expression</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Expression</em>' attribute.
	 * @see #getExpression()
	 * @generated
	 */
	void setExpression(String value);

	/**
	 * Returns the value of the '<em><b>Category</b></em>' attribute.
	 * The default value is <code>"Custom"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Kategorie fuer die Anzeige im Generator-Picker.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Category</em>' attribute.
	 * @see #setCategory(String)
	 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage#getCustomGeneratorDef_Category()
	 * @model default="Custom"
	 * @generated
	 */
	String getCategory();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.model.datagen.CustomGeneratorDef#getCategory <em>Category</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Category</em>' attribute.
	 * @see #getCategory()
	 * @generated
	 */
	void setCategory(String value);

} // CustomGeneratorDef
