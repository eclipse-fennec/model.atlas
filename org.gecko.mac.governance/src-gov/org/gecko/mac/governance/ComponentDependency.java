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

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Component Dependency</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Abstract base class representing operational dependencies between system components. Dependencies define what a component needs (Requirements) or provides (Capabilities) within specific governance namespaces.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.gecko.mac.governance.ComponentDependency#getComponent <em>Component</em>}</li>
 *   <li>{@link org.gecko.mac.governance.ComponentDependency#getName <em>Name</em>}</li>
 *   <li>{@link org.gecko.mac.governance.ComponentDependency#getDescription <em>Description</em>}</li>
 *   <li>{@link org.gecko.mac.governance.ComponentDependency#getReferencedComponent <em>Referenced Component</em>}</li>
 *   <li>{@link org.gecko.mac.governance.ComponentDependency#getGovernanceNamespace <em>Governance Namespace</em>}</li>
 * </ul>
 *
 * @see org.gecko.mac.governance.GovernancePackage#getComponentDependency()
 * @model abstract="true"
 * @generated
 */
@ProviderType
public interface ComponentDependency extends EObject {
	/**
	 * Returns the value of the '<em><b>Component</b></em>' container reference.
	 * It is bidirectional and its opposite is '{@link org.gecko.mac.governance.SystemComponent#getDependencies <em>Dependencies</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Component</em>' container reference.
	 * @see #setComponent(SystemComponent)
	 * @see org.gecko.mac.governance.GovernancePackage#getComponentDependency_Component()
	 * @see org.gecko.mac.governance.SystemComponent#getDependencies
	 * @model opposite="dependencies" keys="componentId" required="true" transient="false"
	 * @generated
	 */
	SystemComponent getComponent();

	/**
	 * Sets the value of the '{@link org.gecko.mac.governance.ComponentDependency#getComponent <em>Component</em>}' container reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Component</em>' container reference.
	 * @see #getComponent()
	 * @generated
	 */
	void setComponent(SystemComponent value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Human-readable name of this dependency (e.g., 'MQTT Data Inbound', 'SSL Security')
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.gecko.mac.governance.GovernancePackage#getComponentDependency_Name()
	 * @model required="true"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.gecko.mac.governance.ComponentDependency#getName <em>Name</em>}' attribute.
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
	 * Detailed description of what this dependency represents
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @see org.gecko.mac.governance.GovernancePackage#getComponentDependency_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.gecko.mac.governance.ComponentDependency#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Referenced Component</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Optional reference to the specific system component this dependency is connected to
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Referenced Component</em>' reference.
	 * @see #setReferencedComponent(SystemComponent)
	 * @see org.gecko.mac.governance.GovernancePackage#getComponentDependency_ReferencedComponent()
	 * @model keys="componentId"
	 * @generated
	 */
	SystemComponent getReferencedComponent();

	/**
	 * Sets the value of the '{@link org.gecko.mac.governance.ComponentDependency#getReferencedComponent <em>Referenced Component</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Referenced Component</em>' reference.
	 * @see #getReferencedComponent()
	 * @generated
	 */
	void setReferencedComponent(SystemComponent value);

	/**
	 * Returns the value of the '<em><b>Governance Namespace</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Reference to the governance namespace this dependency operates within
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Governance Namespace</em>' reference.
	 * @see #setGovernanceNamespace(GovernanceNamespace)
	 * @see org.gecko.mac.governance.GovernancePackage#getComponentDependency_GovernanceNamespace()
	 * @model keys="namespaceId"
	 * @generated
	 */
	GovernanceNamespace getGovernanceNamespace();

	/**
	 * Sets the value of the '{@link org.gecko.mac.governance.ComponentDependency#getGovernanceNamespace <em>Governance Namespace</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Governance Namespace</em>' reference.
	 * @see #getGovernanceNamespace()
	 * @generated
	 */
	void setGovernanceNamespace(GovernanceNamespace value);

} // ComponentDependency
