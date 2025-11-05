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

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>System Holder</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.SystemHolder#getSystem <em>System</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getSystemHolder()
 * @model abstract="true"
 * @generated
 */
@ProviderType
public interface SystemHolder extends EObject {
	/**
	 * Returns the value of the '<em><b>System</b></em>' reference.
	 * It is bidirectional and its opposite is '{@link org.eclipse.fennec.model.atlas.governance.GovernanceSystem#getHolders <em>Holders</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>System</em>' reference.
	 * @see #setSystem(GovernanceSystem)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getSystemHolder_System()
	 * @see org.eclipse.fennec.model.atlas.governance.GovernanceSystem#getHolders
	 * @model opposite="holders" keys="systemId"
	 * @generated
	 */
	GovernanceSystem getSystem();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.SystemHolder#getSystem <em>System</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>System</em>' reference.
	 * @see #getSystem()
	 * @generated
	 */
	void setSystem(GovernanceSystem value);

} // SystemHolder
