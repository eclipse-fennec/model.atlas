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
package org.eclipse.fennec.model.atlas.governance;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Policy Pack</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.PolicyPack#getPolicy <em>Policy</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getPolicyPack()
 * @model
 * @generated
 */
@ProviderType
public interface PolicyPack extends NamespaceHolder {
	/**
	 * Returns the value of the '<em><b>Policy</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Policy</em>' reference.
	 * @see #setPolicy(Policy)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getPolicyPack_Policy()
	 * @model keys="policyId" required="true"
	 * @generated
	 */
	Policy getPolicy();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.PolicyPack#getPolicy <em>Policy</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Policy</em>' reference.
	 * @see #getPolicy()
	 * @generated
	 */
	void setPolicy(Policy value);

} // PolicyPack
