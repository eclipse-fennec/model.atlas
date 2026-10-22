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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.asset.model.modelatlasasset;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Fingerprintable Asset</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * An asset which has a fingerprint, in the model.atlas sense of the term. An EPackage has a fingerprint, but also a CompiledUnit
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.FingerprintableAsset#getFingerprint <em>Fingerprint</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.ModelAtlasAssetPackage#getFingerprintableAsset()
 * @model
 * @generated
 */
@ProviderType
public interface FingerprintableAsset extends StageableAsset {
	/**
	 * Returns the value of the '<em><b>Fingerprint</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Fingerprint</em>' attribute.
	 * @see #setFingerprint(String)
	 * @see org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.ModelAtlasAssetPackage#getFingerprintableAsset_Fingerprint()
	 * @model
	 * @generated
	 */
	String getFingerprint();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.FingerprintableAsset#getFingerprint <em>Fingerprint</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Fingerprint</em>' attribute.
	 * @see #getFingerprint()
	 * @generated
	 */
	void setFingerprint(String value);

} // FingerprintableAsset
