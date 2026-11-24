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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.registry.api;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Package Search</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Criteria for RegistryReader#searchPackages. Every criterion is optional (null does not filter); the given ones must all match.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getStage <em>Stage</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getNsUri <em>Ns Uri</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getNsUriExact <em>Ns Uri Exact</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getPrefix <em>Prefix</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getClassifier <em>Classifier</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getFeatureName <em>Feature Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getFeatureType <em>Feature Type</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getFeatureNameTypePair <em>Feature Name Type Pair</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getLimit <em>Limit</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getOffset <em>Offset</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackageSearch()
 * @model
 * @generated
 */
@ProviderType
public interface PackageSearch {
	/**
	 * Returns the value of the '<em><b>Stage</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * only packages in this stage
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Stage</em>' attribute.
	 * @see #setStage(String)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackageSearch_Stage()
	 * @model
	 * @generated
	 */
	String getStage();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getStage <em>Stage</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Stage</em>' attribute.
	 * @see #getStage()
	 * @generated
	 */
	void setStage(String value);

	/**
	 * Returns the value of the '<em><b>Ns Uri</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * nsURI contains
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Ns Uri</em>' attribute.
	 * @see #setNsUri(String)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackageSearch_NsUri()
	 * @model
	 * @generated
	 */
	String getNsUri();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getNsUri <em>Ns Uri</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ns Uri</em>' attribute.
	 * @see #getNsUri()
	 * @generated
	 */
	void setNsUri(String value);

	/**
	 * Returns the value of the '<em><b>Ns Uri Exact</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * nsURI equals
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Ns Uri Exact</em>' attribute.
	 * @see #setNsUriExact(String)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackageSearch_NsUriExact()
	 * @model
	 * @generated
	 */
	String getNsUriExact();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getNsUriExact <em>Ns Uri Exact</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ns Uri Exact</em>' attribute.
	 * @see #getNsUriExact()
	 * @generated
	 */
	void setNsUriExact(String value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * package name
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackageSearch_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Prefix</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * namespace prefix
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Prefix</em>' attribute.
	 * @see #setPrefix(String)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackageSearch_Prefix()
	 * @model
	 * @generated
	 */
	String getPrefix();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getPrefix <em>Prefix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Prefix</em>' attribute.
	 * @see #getPrefix()
	 * @generated
	 */
	void setPrefix(String value);

	/**
	 * Returns the value of the '<em><b>Classifier</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * name of a classifier the package contains
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Classifier</em>' attribute.
	 * @see #setClassifier(String)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackageSearch_Classifier()
	 * @model
	 * @generated
	 */
	String getClassifier();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getClassifier <em>Classifier</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Classifier</em>' attribute.
	 * @see #getClassifier()
	 * @generated
	 */
	void setClassifier(String value);

	/**
	 * Returns the value of the '<em><b>Feature Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * name of a structural feature the package contains
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Feature Name</em>' attribute.
	 * @see #setFeatureName(String)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackageSearch_FeatureName()
	 * @model
	 * @generated
	 */
	String getFeatureName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getFeatureName <em>Feature Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Feature Name</em>' attribute.
	 * @see #getFeatureName()
	 * @generated
	 */
	void setFeatureName(String value);

	/**
	 * Returns the value of the '<em><b>Feature Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * type of a structural feature the package contains
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Feature Type</em>' attribute.
	 * @see #setFeatureType(String)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackageSearch_FeatureType()
	 * @model
	 * @generated
	 */
	String getFeatureType();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getFeatureType <em>Feature Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Feature Type</em>' attribute.
	 * @see #getFeatureType()
	 * @generated
	 */
	void setFeatureType(String value);

	/**
	 * Returns the value of the '<em><b>Feature Name Type Pair</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * a structural feature by name and type, as name:type
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Feature Name Type Pair</em>' attribute.
	 * @see #setFeatureNameTypePair(String)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackageSearch_FeatureNameTypePair()
	 * @model
	 * @generated
	 */
	String getFeatureNameTypePair();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getFeatureNameTypePair <em>Feature Name Type Pair</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Feature Name Type Pair</em>' attribute.
	 * @see #getFeatureNameTypePair()
	 * @generated
	 */
	void setFeatureNameTypePair(String value);

	/**
	 * Returns the value of the '<em><b>Limit</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * the page size
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Limit</em>' attribute.
	 * @see #setLimit(int)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackageSearch_Limit()
	 * @model
	 * @generated
	 */
	int getLimit();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getLimit <em>Limit</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Limit</em>' attribute.
	 * @see #getLimit()
	 * @generated
	 */
	void setLimit(int value);

	/**
	 * Returns the value of the '<em><b>Offset</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * the number of hits to skip
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Offset</em>' attribute.
	 * @see #setOffset(int)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackageSearch_Offset()
	 * @model
	 * @generated
	 */
	int getOffset();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.PackageSearch#getOffset <em>Offset</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Offset</em>' attribute.
	 * @see #getOffset()
	 * @generated
	 */
	void setOffset(int value);

} // PackageSearch
