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
package org.gecko.mac.governance;

import org.eclipse.emf.common.util.EList;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Feature Holder</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.gecko.mac.governance.FeatureHolder#getFeatures <em>Features</em>}</li>
 * </ul>
 *
 * @see org.gecko.mac.governance.GovernancePackage#getFeatureHolder()
 * @model
 * @generated
 */
@ProviderType
public interface FeatureHolder extends SystemHolder {
	/**
	 * Returns the value of the '<em><b>Features</b></em>' containment reference list.
	 * The list contents are of type {@link org.gecko.mac.governance.PolicyFeature}.
	 * It is bidirectional and its opposite is '{@link org.gecko.mac.governance.PolicyFeature#getParent <em>Parent</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Features</em>' containment reference list.
	 * @see org.gecko.mac.governance.GovernancePackage#getFeatureHolder_Features()
	 * @see org.gecko.mac.governance.PolicyFeature#getParent
	 * @model opposite="parent" containment="true"
	 * @generated
	 */
	EList<PolicyFeature> getFeatures();

} // FeatureHolder
