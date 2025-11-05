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
 * A representation of the model object '<em><b>Namespace Holder</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * An entity that can define and manage governance namespaces. Provides the foundational namespace definitions that can be referenced by ComponentDependencies and PolicyFeatures to eliminate magic strings.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.gecko.mac.governance.NamespaceHolder#getNamespaces <em>Namespaces</em>}</li>
 * </ul>
 *
 * @see org.gecko.mac.governance.GovernancePackage#getNamespaceHolder()
 * @model
 * @generated
 */
@ProviderType
public interface NamespaceHolder extends SystemHolder {
	/**
	 * Returns the value of the '<em><b>Namespaces</b></em>' containment reference list.
	 * The list contents are of type {@link org.gecko.mac.governance.GovernanceNamespace}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Governance namespaces defined within this system
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Namespaces</em>' containment reference list.
	 * @see org.gecko.mac.governance.GovernancePackage#getNamespaceHolder_Namespaces()
	 * @model containment="true" keys="namespaceId"
	 * @generated
	 */
	EList<GovernanceNamespace> getNamespaces();

} // NamespaceHolder
