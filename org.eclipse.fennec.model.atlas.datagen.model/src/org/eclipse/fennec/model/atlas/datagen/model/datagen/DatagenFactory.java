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

import org.eclipse.emf.ecore.EFactory;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.model.atlas.datagen.model.datagen.DatagenPackage
 * @generated
 */
@ProviderType
public interface DatagenFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	DatagenFactory eINSTANCE = org.eclipse.fennec.model.atlas.datagen.model.datagen.impl.DatagenFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Data Gen Config</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Data Gen Config</em>'.
	 * @generated
	 */
	DataGenConfig createDataGenConfig();

	/**
	 * Returns a new object of class '<em>Class Gen Config</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Class Gen Config</em>'.
	 * @generated
	 */
	ClassGenConfig createClassGenConfig();

	/**
	 * Returns a new object of class '<em>Attribute Gen Config</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Attribute Gen Config</em>'.
	 * @generated
	 */
	AttributeGenConfig createAttributeGenConfig();

	/**
	 * Returns a new object of class '<em>Reference Gen Config</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Reference Gen Config</em>'.
	 * @generated
	 */
	ReferenceGenConfig createReferenceGenConfig();

	/**
	 * Returns a new object of class '<em>Custom Generator Def</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Custom Generator Def</em>'.
	 * @generated
	 */
	CustomGeneratorDef createCustomGeneratorDef();

	/**
	 * Returns a new object of class '<em>Data Gen Result</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Data Gen Result</em>'.
	 * @generated
	 */
	DataGenResult createDataGenResult();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	DatagenPackage getDatagenPackage();

} //DatagenFactory
