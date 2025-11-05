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

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>System Component</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Represents a manageable system component within the infrastructure, such as a sensor, gateway, broker, or application instance.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.SystemComponent#getComponentId <em>Component Id</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.SystemComponent#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.SystemComponent#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.SystemComponent#getTrustLevel <em>Trust Level</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.SystemComponent#getAppliedPolicies <em>Applied Policies</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.SystemComponent#getType <em>Type</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.SystemComponent#getChildComponents <em>Child Components</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.SystemComponent#getParentComponents <em>Parent Components</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.SystemComponent#getSupervisorComponent <em>Supervisor Component</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.SystemComponent#getSupervisedComponents <em>Supervised Components</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.SystemComponent#getDependencies <em>Dependencies</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.SystemComponent#getSupportedFeatures <em>Supported Features</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getSystemComponent()
 * @model
 * @generated
 */
@ProviderType
public interface SystemComponent extends FeatureHolder {
	/**
	 * Returns the value of the '<em><b>Component Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Component Id</em>' attribute.
	 * @see #setComponentId(String)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getSystemComponent_ComponentId()
	 * @model id="true" required="true"
	 * @generated
	 */
	String getComponentId();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.SystemComponent#getComponentId <em>Component Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Component Id</em>' attribute.
	 * @see #getComponentId()
	 * @generated
	 */
	void setComponentId(String value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getSystemComponent_Name()
	 * @model required="true"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.SystemComponent#getName <em>Name</em>}' attribute.
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
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getSystemComponent_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.SystemComponent#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Trust Level</b></em>' attribute.
	 * The default value is <code>"UNKNOWN"</code>.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.governance.TrustLevel}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The assigned trust level of the system component, determining how its data and operations are handled.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Trust Level</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.governance.TrustLevel
	 * @see #setTrustLevel(TrustLevel)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getSystemComponent_TrustLevel()
	 * @model default="UNKNOWN"
	 * @generated
	 */
	TrustLevel getTrustLevel();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.SystemComponent#getTrustLevel <em>Trust Level</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Trust Level</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.governance.TrustLevel
	 * @see #getTrustLevel()
	 * @generated
	 */
	void setTrustLevel(TrustLevel value);

	/**
	 * Returns the value of the '<em><b>Applied Policies</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.governance.Policy}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Policies that apply to this system component.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Applied Policies</em>' reference list.
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getSystemComponent_AppliedPolicies()
	 * @model keys="policyId"
	 * @generated
	 */
	EList<Policy> getAppliedPolicies();

	/**
	 * Returns the value of the '<em><b>Type</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.governance.SystemComponentType}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.governance.SystemComponentType
	 * @see #setType(SystemComponentType)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getSystemComponent_Type()
	 * @model
	 * @generated
	 */
	SystemComponentType getType();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.SystemComponent#getType <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.governance.SystemComponentType
	 * @see #getType()
	 * @generated
	 */
	void setType(SystemComponentType value);

	/**
	 * Returns the value of the '<em><b>Child Components</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.governance.SystemComponent}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.fennec.model.atlas.governance.SystemComponent#getParentComponents <em>Parent Components</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * System components that are sub-components of this component.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Child Components</em>' reference list.
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getSystemComponent_ChildComponents()
	 * @see org.eclipse.fennec.model.atlas.governance.SystemComponent#getParentComponents
	 * @model opposite="parentComponents" keys="componentId"
	 * @generated
	 */
	EList<SystemComponent> getChildComponents();

	/**
	 * Returns the value of the '<em><b>Parent Components</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.governance.SystemComponent}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.fennec.model.atlas.governance.SystemComponent#getChildComponents <em>Child Components</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The system components this component is a sub-component of.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Parent Components</em>' reference list.
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getSystemComponent_ParentComponents()
	 * @see org.eclipse.fennec.model.atlas.governance.SystemComponent#getChildComponents
	 * @model opposite="childComponents" keys="componentId"
	 * @generated
	 */
	EList<SystemComponent> getParentComponents();

	/**
	 * Returns the value of the '<em><b>Supervisor Component</b></em>' reference.
	 * It is bidirectional and its opposite is '{@link org.eclipse.fennec.model.atlas.governance.SystemComponent#getSupervisedComponents <em>Supervised Components</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The system component that acts as a trust anchor or gateway for this component (e.g., a LoRaWAN gateway supervising a sensor).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Supervisor Component</em>' reference.
	 * @see #setSupervisorComponent(SystemComponent)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getSystemComponent_SupervisorComponent()
	 * @see org.eclipse.fennec.model.atlas.governance.SystemComponent#getSupervisedComponents
	 * @model opposite="supervisedComponents" keys="componentId"
	 * @generated
	 */
	SystemComponent getSupervisorComponent();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.SystemComponent#getSupervisorComponent <em>Supervisor Component</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Supervisor Component</em>' reference.
	 * @see #getSupervisorComponent()
	 * @generated
	 */
	void setSupervisorComponent(SystemComponent value);

	/**
	 * Returns the value of the '<em><b>Supervised Components</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.governance.SystemComponent}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.fennec.model.atlas.governance.SystemComponent#getSupervisorComponent <em>Supervisor Component</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The system components that are supervised by this component.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Supervised Components</em>' reference list.
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getSystemComponent_SupervisedComponents()
	 * @see org.eclipse.fennec.model.atlas.governance.SystemComponent#getSupervisorComponent
	 * @model opposite="supervisorComponent" keys="componentId"
	 * @generated
	 */
	EList<SystemComponent> getSupervisedComponents();

	/**
	 * Returns the value of the '<em><b>Dependencies</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.governance.ComponentDependency}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.fennec.model.atlas.governance.ComponentDependency#getComponent <em>Component</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Dependencies</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getSystemComponent_Dependencies()
	 * @see org.eclipse.fennec.model.atlas.governance.ComponentDependency#getComponent
	 * @model opposite="component" containment="true"
	 * @generated
	 */
	EList<ComponentDependency> getDependencies();

	/**
	 * Returns the value of the '<em><b>Supported Features</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.governance.PolicyFeature}.
	 * It is bidirectional and its opposite is '{@link org.eclipse.fennec.model.atlas.governance.PolicyFeature#getComponents <em>Components</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Supported Features</em>' reference list.
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getSystemComponent_SupportedFeatures()
	 * @see org.eclipse.fennec.model.atlas.governance.PolicyFeature#getComponents
	 * @model opposite="components" keys="featureId"
	 * @generated
	 */
	EList<PolicyFeature> getSupportedFeatures();

} // SystemComponent
