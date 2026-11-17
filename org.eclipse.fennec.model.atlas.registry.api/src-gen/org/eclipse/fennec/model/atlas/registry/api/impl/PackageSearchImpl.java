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
package org.eclipse.fennec.model.atlas.registry.api.impl;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.fennec.model.atlas.registry.api.PackageSearch;
import org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Package Search</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageSearchImpl#getStage <em>Stage</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageSearchImpl#getNsUri <em>Ns Uri</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageSearchImpl#getNsUriExact <em>Ns Uri Exact</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageSearchImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageSearchImpl#getPrefix <em>Prefix</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageSearchImpl#getClassifier <em>Classifier</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageSearchImpl#getFeatureName <em>Feature Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageSearchImpl#getFeatureType <em>Feature Type</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageSearchImpl#getFeatureNameTypePair <em>Feature Name Type Pair</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageSearchImpl#getLimit <em>Limit</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageSearchImpl#getOffset <em>Offset</em>}</li>
 * </ul>
 *
 * @generated
 */
public class PackageSearchImpl extends MinimalEObjectImpl.Container implements PackageSearch {
	/**
	 * The default value of the '{@link #getStage() <em>Stage</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStage()
	 * @generated
	 * @ordered
	 */
	protected static final String STAGE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getStage() <em>Stage</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStage()
	 * @generated
	 * @ordered
	 */
	protected String stage = STAGE_EDEFAULT;

	/**
	 * The default value of the '{@link #getNsUri() <em>Ns Uri</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNsUri()
	 * @generated
	 * @ordered
	 */
	protected static final String NS_URI_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getNsUri() <em>Ns Uri</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNsUri()
	 * @generated
	 * @ordered
	 */
	protected String nsUri = NS_URI_EDEFAULT;

	/**
	 * The default value of the '{@link #getNsUriExact() <em>Ns Uri Exact</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNsUriExact()
	 * @generated
	 * @ordered
	 */
	protected static final String NS_URI_EXACT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getNsUriExact() <em>Ns Uri Exact</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNsUriExact()
	 * @generated
	 * @ordered
	 */
	protected String nsUriExact = NS_URI_EXACT_EDEFAULT;

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
	 * The default value of the '{@link #getPrefix() <em>Prefix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPrefix()
	 * @generated
	 * @ordered
	 */
	protected static final String PREFIX_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getPrefix() <em>Prefix</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPrefix()
	 * @generated
	 * @ordered
	 */
	protected String prefix = PREFIX_EDEFAULT;

	/**
	 * The default value of the '{@link #getClassifier() <em>Classifier</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getClassifier()
	 * @generated
	 * @ordered
	 */
	protected static final String CLASSIFIER_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getClassifier() <em>Classifier</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getClassifier()
	 * @generated
	 * @ordered
	 */
	protected String classifier = CLASSIFIER_EDEFAULT;

	/**
	 * The default value of the '{@link #getFeatureName() <em>Feature Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFeatureName()
	 * @generated
	 * @ordered
	 */
	protected static final String FEATURE_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFeatureName() <em>Feature Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFeatureName()
	 * @generated
	 * @ordered
	 */
	protected String featureName = FEATURE_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getFeatureType() <em>Feature Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFeatureType()
	 * @generated
	 * @ordered
	 */
	protected static final String FEATURE_TYPE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFeatureType() <em>Feature Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFeatureType()
	 * @generated
	 * @ordered
	 */
	protected String featureType = FEATURE_TYPE_EDEFAULT;

	/**
	 * The default value of the '{@link #getFeatureNameTypePair() <em>Feature Name Type Pair</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFeatureNameTypePair()
	 * @generated
	 * @ordered
	 */
	protected static final String FEATURE_NAME_TYPE_PAIR_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFeatureNameTypePair() <em>Feature Name Type Pair</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFeatureNameTypePair()
	 * @generated
	 * @ordered
	 */
	protected String featureNameTypePair = FEATURE_NAME_TYPE_PAIR_EDEFAULT;

	/**
	 * The default value of the '{@link #getLimit() <em>Limit</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLimit()
	 * @generated
	 * @ordered
	 */
	protected static final int LIMIT_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getLimit() <em>Limit</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLimit()
	 * @generated
	 * @ordered
	 */
	protected int limit = LIMIT_EDEFAULT;

	/**
	 * The default value of the '{@link #getOffset() <em>Offset</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOffset()
	 * @generated
	 * @ordered
	 */
	protected static final int OFFSET_EDEFAULT = 0;

	/**
	 * The cached value of the '{@link #getOffset() <em>Offset</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOffset()
	 * @generated
	 * @ordered
	 */
	protected int offset = OFFSET_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PackageSearchImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RegistryApiPackage.Literals.PACKAGE_SEARCH;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getStage() {
		return stage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStage(String newStage) {
		stage = newStage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getNsUri() {
		return nsUri;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setNsUri(String newNsUri) {
		nsUri = newNsUri;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getNsUriExact() {
		return nsUriExact;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setNsUriExact(String newNsUriExact) {
		nsUriExact = newNsUriExact;
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
		name = newName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getPrefix() {
		return prefix;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPrefix(String newPrefix) {
		prefix = newPrefix;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getClassifier() {
		return classifier;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setClassifier(String newClassifier) {
		classifier = newClassifier;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getFeatureName() {
		return featureName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFeatureName(String newFeatureName) {
		featureName = newFeatureName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getFeatureType() {
		return featureType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFeatureType(String newFeatureType) {
		featureType = newFeatureType;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getFeatureNameTypePair() {
		return featureNameTypePair;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFeatureNameTypePair(String newFeatureNameTypePair) {
		featureNameTypePair = newFeatureNameTypePair;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getLimit() {
		return limit;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLimit(int newLimit) {
		limit = newLimit;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getOffset() {
		return offset;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setOffset(int newOffset) {
		offset = newOffset;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case RegistryApiPackage.PACKAGE_SEARCH__STAGE:
				return getStage();
			case RegistryApiPackage.PACKAGE_SEARCH__NS_URI:
				return getNsUri();
			case RegistryApiPackage.PACKAGE_SEARCH__NS_URI_EXACT:
				return getNsUriExact();
			case RegistryApiPackage.PACKAGE_SEARCH__NAME:
				return getName();
			case RegistryApiPackage.PACKAGE_SEARCH__PREFIX:
				return getPrefix();
			case RegistryApiPackage.PACKAGE_SEARCH__CLASSIFIER:
				return getClassifier();
			case RegistryApiPackage.PACKAGE_SEARCH__FEATURE_NAME:
				return getFeatureName();
			case RegistryApiPackage.PACKAGE_SEARCH__FEATURE_TYPE:
				return getFeatureType();
			case RegistryApiPackage.PACKAGE_SEARCH__FEATURE_NAME_TYPE_PAIR:
				return getFeatureNameTypePair();
			case RegistryApiPackage.PACKAGE_SEARCH__LIMIT:
				return getLimit();
			case RegistryApiPackage.PACKAGE_SEARCH__OFFSET:
				return getOffset();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case RegistryApiPackage.PACKAGE_SEARCH__STAGE:
				setStage((String)newValue);
				return;
			case RegistryApiPackage.PACKAGE_SEARCH__NS_URI:
				setNsUri((String)newValue);
				return;
			case RegistryApiPackage.PACKAGE_SEARCH__NS_URI_EXACT:
				setNsUriExact((String)newValue);
				return;
			case RegistryApiPackage.PACKAGE_SEARCH__NAME:
				setName((String)newValue);
				return;
			case RegistryApiPackage.PACKAGE_SEARCH__PREFIX:
				setPrefix((String)newValue);
				return;
			case RegistryApiPackage.PACKAGE_SEARCH__CLASSIFIER:
				setClassifier((String)newValue);
				return;
			case RegistryApiPackage.PACKAGE_SEARCH__FEATURE_NAME:
				setFeatureName((String)newValue);
				return;
			case RegistryApiPackage.PACKAGE_SEARCH__FEATURE_TYPE:
				setFeatureType((String)newValue);
				return;
			case RegistryApiPackage.PACKAGE_SEARCH__FEATURE_NAME_TYPE_PAIR:
				setFeatureNameTypePair((String)newValue);
				return;
			case RegistryApiPackage.PACKAGE_SEARCH__LIMIT:
				setLimit((Integer)newValue);
				return;
			case RegistryApiPackage.PACKAGE_SEARCH__OFFSET:
				setOffset((Integer)newValue);
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
			case RegistryApiPackage.PACKAGE_SEARCH__STAGE:
				setStage(STAGE_EDEFAULT);
				return;
			case RegistryApiPackage.PACKAGE_SEARCH__NS_URI:
				setNsUri(NS_URI_EDEFAULT);
				return;
			case RegistryApiPackage.PACKAGE_SEARCH__NS_URI_EXACT:
				setNsUriExact(NS_URI_EXACT_EDEFAULT);
				return;
			case RegistryApiPackage.PACKAGE_SEARCH__NAME:
				setName(NAME_EDEFAULT);
				return;
			case RegistryApiPackage.PACKAGE_SEARCH__PREFIX:
				setPrefix(PREFIX_EDEFAULT);
				return;
			case RegistryApiPackage.PACKAGE_SEARCH__CLASSIFIER:
				setClassifier(CLASSIFIER_EDEFAULT);
				return;
			case RegistryApiPackage.PACKAGE_SEARCH__FEATURE_NAME:
				setFeatureName(FEATURE_NAME_EDEFAULT);
				return;
			case RegistryApiPackage.PACKAGE_SEARCH__FEATURE_TYPE:
				setFeatureType(FEATURE_TYPE_EDEFAULT);
				return;
			case RegistryApiPackage.PACKAGE_SEARCH__FEATURE_NAME_TYPE_PAIR:
				setFeatureNameTypePair(FEATURE_NAME_TYPE_PAIR_EDEFAULT);
				return;
			case RegistryApiPackage.PACKAGE_SEARCH__LIMIT:
				setLimit(LIMIT_EDEFAULT);
				return;
			case RegistryApiPackage.PACKAGE_SEARCH__OFFSET:
				setOffset(OFFSET_EDEFAULT);
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
			case RegistryApiPackage.PACKAGE_SEARCH__STAGE:
				return STAGE_EDEFAULT == null ? stage != null : !STAGE_EDEFAULT.equals(stage);
			case RegistryApiPackage.PACKAGE_SEARCH__NS_URI:
				return NS_URI_EDEFAULT == null ? nsUri != null : !NS_URI_EDEFAULT.equals(nsUri);
			case RegistryApiPackage.PACKAGE_SEARCH__NS_URI_EXACT:
				return NS_URI_EXACT_EDEFAULT == null ? nsUriExact != null : !NS_URI_EXACT_EDEFAULT.equals(nsUriExact);
			case RegistryApiPackage.PACKAGE_SEARCH__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case RegistryApiPackage.PACKAGE_SEARCH__PREFIX:
				return PREFIX_EDEFAULT == null ? prefix != null : !PREFIX_EDEFAULT.equals(prefix);
			case RegistryApiPackage.PACKAGE_SEARCH__CLASSIFIER:
				return CLASSIFIER_EDEFAULT == null ? classifier != null : !CLASSIFIER_EDEFAULT.equals(classifier);
			case RegistryApiPackage.PACKAGE_SEARCH__FEATURE_NAME:
				return FEATURE_NAME_EDEFAULT == null ? featureName != null : !FEATURE_NAME_EDEFAULT.equals(featureName);
			case RegistryApiPackage.PACKAGE_SEARCH__FEATURE_TYPE:
				return FEATURE_TYPE_EDEFAULT == null ? featureType != null : !FEATURE_TYPE_EDEFAULT.equals(featureType);
			case RegistryApiPackage.PACKAGE_SEARCH__FEATURE_NAME_TYPE_PAIR:
				return FEATURE_NAME_TYPE_PAIR_EDEFAULT == null ? featureNameTypePair != null : !FEATURE_NAME_TYPE_PAIR_EDEFAULT.equals(featureNameTypePair);
			case RegistryApiPackage.PACKAGE_SEARCH__LIMIT:
				return limit != LIMIT_EDEFAULT;
			case RegistryApiPackage.PACKAGE_SEARCH__OFFSET:
				return offset != OFFSET_EDEFAULT;
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
		result.append(" (stage: ");
		result.append(stage);
		result.append(", nsUri: ");
		result.append(nsUri);
		result.append(", nsUriExact: ");
		result.append(nsUriExact);
		result.append(", name: ");
		result.append(name);
		result.append(", prefix: ");
		result.append(prefix);
		result.append(", classifier: ");
		result.append(classifier);
		result.append(", featureName: ");
		result.append(featureName);
		result.append(", featureType: ");
		result.append(featureType);
		result.append(", featureNameTypePair: ");
		result.append(featureNameTypePair);
		result.append(", limit: ");
		result.append(limit);
		result.append(", offset: ");
		result.append(offset);
		result.append(')');
		return result.toString();
	}

} //PackageSearchImpl
