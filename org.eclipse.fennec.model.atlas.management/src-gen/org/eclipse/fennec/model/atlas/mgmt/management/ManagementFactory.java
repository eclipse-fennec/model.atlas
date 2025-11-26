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
 *      Mark Hoffmann - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.mgmt.management;

import org.eclipse.emf.ecore.EFactory;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage
 * @generated
 */
@ProviderType
public interface ManagementFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	ManagementFactory eINSTANCE = org.eclipse.fennec.model.atlas.mgmt.management.impl.ManagementFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Object Metadata</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Object Metadata</em>'.
	 * @generated
	 */
	ObjectMetadata createObjectMetadata();

	/**
	 * Returns a new object of class '<em>Object Query</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Object Query</em>'.
	 * @generated
	 */
	ObjectQuery createObjectQuery();

	/**
	 * Returns a new object of class '<em>Generation Request</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Generation Request</em>'.
	 * @generated
	 */
	GenerationRequest createGenerationRequest();

	/**
	 * Returns a new object of class '<em>Object Metadata Container</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Object Metadata Container</em>'.
	 * @generated
	 */
	ObjectMetadataContainer createObjectMetadataContainer();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	ManagementPackage getManagementPackage();

} //ManagementFactory
