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

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Namespace</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Defines a governance namespace that can be referenced by both operational dependencies (Requirements/Capabilities) and policy elements (PolicyFeatures/PolicyRequirements) to eliminate magic strings and provide semantic clarity.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.GovernanceNamespace#getNamespaceId <em>Namespace Id</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.GovernanceNamespace#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.GovernanceNamespace#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.GovernanceNamespace#getVersion <em>Version</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.GovernanceNamespace#getStandardReference <em>Standard Reference</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.GovernanceNamespace#getParentNamespace <em>Parent Namespace</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.GovernanceNamespace#getChildNamespaces <em>Child Namespaces</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getGovernanceNamespace()
 * @model
 * @generated
 */
@ProviderType
public interface GovernanceNamespace extends EObject {
	/**
	 * Returns the value of the '<em><b>Namespace Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Unique identifier for the namespace (e.g., 'transport.mqtt', 'security.ssl', 'compliance.gdpr')
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Namespace Id</em>' attribute.
	 * @see #setNamespaceId(String)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getGovernanceNamespace_NamespaceId()
	 * @model id="true" required="true"
	 * @generated
	 */
	String getNamespaceId();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.GovernanceNamespace#getNamespaceId <em>Namespace Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Namespace Id</em>' attribute.
	 * @see #getNamespaceId()
	 * @generated
	 */
	void setNamespaceId(String value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Human-readable name of the namespace
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getGovernanceNamespace_Name()
	 * @model required="true"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.GovernanceNamespace#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Detailed description of what this namespace represents
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getGovernanceNamespace_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.GovernanceNamespace#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Version of the namespace specification
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Version</em>' attribute.
	 * @see #setVersion(String)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getGovernanceNamespace_Version()
	 * @model
	 * @generated
	 */
	String getVersion();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.GovernanceNamespace#getVersion <em>Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Version</em>' attribute.
	 * @see #getVersion()
	 * @generated
	 */
	void setVersion(String value);

	/**
	 * Returns the value of the '<em><b>Standard Reference</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Reference to relevant standards or specifications (e.g., 'RFC 3986', 'ISO 27001')
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Standard Reference</em>' attribute.
	 * @see #setStandardReference(String)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getGovernanceNamespace_StandardReference()
	 * @model
	 * @generated
	 */
	String getStandardReference();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.GovernanceNamespace#getStandardReference <em>Standard Reference</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Standard Reference</em>' attribute.
	 * @see #getStandardReference()
	 * @generated
	 */
	void setStandardReference(String value);

	/**
	 * Returns the value of the '<em><b>Parent Namespace</b></em>' reference.
	 * It is bidirectional and its opposite is '{@link org.eclipse.fennec.model.atlas.governance.GovernanceNamespace#getChildNamespaces <em>Child Namespaces</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Parent namespace for hierarchical organization (e.g., 'transport' is parent of 'transport.mqtt')
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Parent Namespace</em>' reference.
	 * @see #setParentNamespace(GovernanceNamespace)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getGovernanceNamespace_ParentNamespace()
	 * @see org.eclipse.fennec.model.atlas.governance.GovernanceNamespace#getChildNamespaces
	 * @model opposite="childNamespaces" keys="namespaceId"
	 * @generated
	 */
	GovernanceNamespace getParentNamespace();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.GovernanceNamespace#getParentNamespace <em>Parent Namespace</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parent Namespace</em>' reference.
	 * @see #getParentNamespace()
	 * @generated
	 */
	void setParentNamespace(GovernanceNamespace value);

	/**
	 * Returns the value of the '<em><b>Child Namespaces</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.governance.GovernanceNamespace}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.fennec.model.atlas.governance.GovernanceNamespace#getParentNamespace <em>Parent Namespace</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Child namespaces for hierarchical organization
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Child Namespaces</em>' reference list.
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getGovernanceNamespace_ChildNamespaces()
	 * @see org.eclipse.fennec.model.atlas.governance.GovernanceNamespace#getParentNamespace
	 * @model opposite="parentNamespace" keys="namespaceId"
	 * @generated
	 */
	EList<GovernanceNamespace> getChildNamespaces();

} // GovernanceNamespace
