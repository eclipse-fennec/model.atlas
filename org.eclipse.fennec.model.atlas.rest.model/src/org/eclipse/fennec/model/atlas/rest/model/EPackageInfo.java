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
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.rest.model;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>EPackage Info</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.EPackageInfo#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.EPackageInfo#getNsURI <em>Ns URI</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.EPackageInfo#getNsPrefix <em>Ns Prefix</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.EPackageInfo#getClassifierCount <em>Classifier Count</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.EPackageInfo#getSubPackageCount <em>Sub Package Count</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.rest.model.RestPackage#getEPackageInfo()
 * @model
 * @generated
 */
@ProviderType
public interface EPackageInfo extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.model.atlas.rest.model.RestPackage#getEPackageInfo_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.rest.model.EPackageInfo#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Ns URI</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ns URI</em>' attribute.
	 * @see #setNsURI(String)
	 * @see org.eclipse.fennec.model.atlas.rest.model.RestPackage#getEPackageInfo_NsURI()
	 * @model
	 * @generated
	 */
	String getNsURI();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.rest.model.EPackageInfo#getNsURI <em>Ns URI</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ns URI</em>' attribute.
	 * @see #getNsURI()
	 * @generated
	 */
	void setNsURI(String value);

	/**
	 * Returns the value of the '<em><b>Ns Prefix</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Ns Prefix</em>' attribute.
	 * @see #setNsPrefix(String)
	 * @see org.eclipse.fennec.model.atlas.rest.model.RestPackage#getEPackageInfo_NsPrefix()
	 * @model
	 * @generated
	 */
	String getNsPrefix();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.rest.model.EPackageInfo#getNsPrefix <em>Ns Prefix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ns Prefix</em>' attribute.
	 * @see #getNsPrefix()
	 * @generated
	 */
	void setNsPrefix(String value);

	/**
	 * Returns the value of the '<em><b>Classifier Count</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Classifier Count</em>' attribute.
	 * @see #setClassifierCount(int)
	 * @see org.eclipse.fennec.model.atlas.rest.model.RestPackage#getEPackageInfo_ClassifierCount()
	 * @model
	 * @generated
	 */
	int getClassifierCount();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.rest.model.EPackageInfo#getClassifierCount <em>Classifier Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Classifier Count</em>' attribute.
	 * @see #getClassifierCount()
	 * @generated
	 */
	void setClassifierCount(int value);

	/**
	 * Returns the value of the '<em><b>Sub Package Count</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Sub Package Count</em>' attribute.
	 * @see #setSubPackageCount(int)
	 * @see org.eclipse.fennec.model.atlas.rest.model.RestPackage#getEPackageInfo_SubPackageCount()
	 * @model
	 * @generated
	 */
	int getSubPackageCount();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.rest.model.EPackageInfo#getSubPackageCount <em>Sub Package Count</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Sub Package Count</em>' attribute.
	 * @see #getSubPackageCount()
	 * @generated
	 */
	void setSubPackageCount(int value);

} // EPackageInfo
