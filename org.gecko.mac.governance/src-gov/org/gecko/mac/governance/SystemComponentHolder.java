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
 * A representation of the model object '<em><b>System Component Holder</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * An entity that can hold system components
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.gecko.mac.governance.SystemComponentHolder#getSystemComponents <em>System Components</em>}</li>
 * </ul>
 *
 * @see org.gecko.mac.governance.GovernancePackage#getSystemComponentHolder()
 * @model
 * @generated
 */
@ProviderType
public interface SystemComponentHolder extends SystemHolder {
	/**
	 * Returns the value of the '<em><b>System Components</b></em>' containment reference list.
	 * The list contents are of type {@link org.gecko.mac.governance.SystemComponent}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>System Components</em>' containment reference list.
	 * @see org.gecko.mac.governance.GovernancePackage#getSystemComponentHolder_SystemComponents()
	 * @model containment="true" keys="componentId"
	 * @generated
	 */
	EList<SystemComponent> getSystemComponents();

} // SystemComponentHolder
