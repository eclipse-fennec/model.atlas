/**
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
package org.gecko.mac.governance.util;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

import org.gecko.mac.governance.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see org.gecko.mac.governance.GovernancePackage
 * @generated
 */
public class GovernanceAdapterFactory extends AdapterFactoryImpl {
	/**
	 * The cached model package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static GovernancePackage modelPackage;

	/**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GovernanceAdapterFactory() {
		if (modelPackage == null) {
			modelPackage = GovernancePackage.eINSTANCE;
		}
	}

	/**
	 * Returns whether this factory is applicable for the type of the object.
	 * <!-- begin-user-doc -->
	 * This implementation returns <code>true</code> if the object is either the model's package or is an instance object of the model.
	 * <!-- end-user-doc -->
	 * @return whether this factory is applicable for the type of the object.
	 * @generated
	 */
	@Override
	public boolean isFactoryForType(Object object) {
		if (object == modelPackage) {
			return true;
		}
		if (object instanceof EObject) {
			return ((EObject)object).eClass().getEPackage() == modelPackage;
		}
		return false;
	}

	/**
	 * The switch that delegates to the <code>createXXX</code> methods.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected GovernanceSwitch<Adapter> modelSwitch =
		new GovernanceSwitch<Adapter>() {
			@Override
			public Adapter caseGovernanceDocumentation(GovernanceDocumentation object) {
				return createGovernanceDocumentationAdapter();
			}
			@Override
			public Adapter caseAttributeDefinition(AttributeDefinition object) {
				return createAttributeDefinitionAdapter();
			}
			@Override
			public Adapter caseComplianceCheckResult(ComplianceCheckResult object) {
				return createComplianceCheckResultAdapter();
			}
			@Override
			public Adapter caseFinding(Finding object) {
				return createFindingAdapter();
			}
			@Override
			public Adapter caseSystemComponent(SystemComponent object) {
				return createSystemComponentAdapter();
			}
			@Override
			public Adapter casePolicy(Policy object) {
				return createPolicyAdapter();
			}
			@Override
			public Adapter caseGovernanceSystem(GovernanceSystem object) {
				return createGovernanceSystemAdapter();
			}
			@Override
			public Adapter caseSystemComponentHolder(SystemComponentHolder object) {
				return createSystemComponentHolderAdapter();
			}
			@Override
			public Adapter casePolicyHolder(PolicyHolder object) {
				return createPolicyHolderAdapter();
			}
			@Override
			public Adapter caseSystemHolder(SystemHolder object) {
				return createSystemHolderAdapter();
			}
			@Override
			public Adapter caseHolderSystemComponent(HolderSystemComponent object) {
				return createHolderSystemComponentAdapter();
			}
			@Override
			public Adapter caseComponentDependency(ComponentDependency object) {
				return createComponentDependencyAdapter();
			}
			@Override
			public Adapter caseRequirement(Requirement object) {
				return createRequirementAdapter();
			}
			@Override
			public Adapter caseCapability(Capability object) {
				return createCapabilityAdapter();
			}
			@Override
			public Adapter casePolicyFeature(PolicyFeature object) {
				return createPolicyFeatureAdapter();
			}
			@Override
			public Adapter caseFeatureHolder(FeatureHolder object) {
				return createFeatureHolderAdapter();
			}
			@Override
			public Adapter casePolicyRequirement(PolicyRequirement object) {
				return createPolicyRequirementAdapter();
			}
			@Override
			public Adapter caseFeatureRequirementMapping(FeatureRequirementMapping object) {
				return createFeatureRequirementMappingAdapter();
			}
			@Override
			public Adapter caseGovernanceNamespace(GovernanceNamespace object) {
				return createGovernanceNamespaceAdapter();
			}
			@Override
			public Adapter caseRequirementGroup(RequirementGroup object) {
				return createRequirementGroupAdapter();
			}
			@Override
			public Adapter caseNamespaceHolder(NamespaceHolder object) {
				return createNamespaceHolderAdapter();
			}
			@Override
			public Adapter casePolicyPack(PolicyPack object) {
				return createPolicyPackAdapter();
			}
			@Override
			public Adapter caseGovernanceDocumentationContainer(GovernanceDocumentationContainer object) {
				return createGovernanceDocumentationContainerAdapter();
			}
			@Override
			public Adapter defaultCase(EObject object) {
				return createEObjectAdapter();
			}
		};

	/**
	 * Creates an adapter for the <code>target</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param target the object to adapt.
	 * @return the adapter for the <code>target</code>.
	 * @generated
	 */
	@Override
	public Adapter createAdapter(Notifier target) {
		return modelSwitch.doSwitch((EObject)target);
	}


	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.governance.GovernanceDocumentation <em>Documentation</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.governance.GovernanceDocumentation
	 * @generated
	 */
	public Adapter createGovernanceDocumentationAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.governance.AttributeDefinition <em>Attribute Definition</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.governance.AttributeDefinition
	 * @generated
	 */
	public Adapter createAttributeDefinitionAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.governance.ComplianceCheckResult <em>Compliance Check Result</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.governance.ComplianceCheckResult
	 * @generated
	 */
	public Adapter createComplianceCheckResultAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.governance.Finding <em>Finding</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.governance.Finding
	 * @generated
	 */
	public Adapter createFindingAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.governance.SystemComponent <em>System Component</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.governance.SystemComponent
	 * @generated
	 */
	public Adapter createSystemComponentAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.governance.Policy <em>Policy</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.governance.Policy
	 * @generated
	 */
	public Adapter createPolicyAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.governance.GovernanceSystem <em>System</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.governance.GovernanceSystem
	 * @generated
	 */
	public Adapter createGovernanceSystemAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.governance.SystemComponentHolder <em>System Component Holder</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.governance.SystemComponentHolder
	 * @generated
	 */
	public Adapter createSystemComponentHolderAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.governance.PolicyHolder <em>Policy Holder</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.governance.PolicyHolder
	 * @generated
	 */
	public Adapter createPolicyHolderAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.governance.SystemHolder <em>System Holder</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.governance.SystemHolder
	 * @generated
	 */
	public Adapter createSystemHolderAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.governance.HolderSystemComponent <em>Holder System Component</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.governance.HolderSystemComponent
	 * @generated
	 */
	public Adapter createHolderSystemComponentAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.governance.ComponentDependency <em>Component Dependency</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.governance.ComponentDependency
	 * @generated
	 */
	public Adapter createComponentDependencyAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.governance.Requirement <em>Requirement</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.governance.Requirement
	 * @generated
	 */
	public Adapter createRequirementAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.governance.Capability <em>Capability</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.governance.Capability
	 * @generated
	 */
	public Adapter createCapabilityAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.governance.PolicyFeature <em>Policy Feature</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.governance.PolicyFeature
	 * @generated
	 */
	public Adapter createPolicyFeatureAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.governance.FeatureHolder <em>Feature Holder</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.governance.FeatureHolder
	 * @generated
	 */
	public Adapter createFeatureHolderAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.governance.PolicyRequirement <em>Policy Requirement</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.governance.PolicyRequirement
	 * @generated
	 */
	public Adapter createPolicyRequirementAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.governance.FeatureRequirementMapping <em>Feature Requirement Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.governance.FeatureRequirementMapping
	 * @generated
	 */
	public Adapter createFeatureRequirementMappingAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.governance.GovernanceNamespace <em>Namespace</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.governance.GovernanceNamespace
	 * @generated
	 */
	public Adapter createGovernanceNamespaceAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.governance.RequirementGroup <em>Requirement Group</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.governance.RequirementGroup
	 * @generated
	 */
	public Adapter createRequirementGroupAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.governance.NamespaceHolder <em>Namespace Holder</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.governance.NamespaceHolder
	 * @generated
	 */
	public Adapter createNamespaceHolderAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.governance.PolicyPack <em>Policy Pack</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.governance.PolicyPack
	 * @generated
	 */
	public Adapter createPolicyPackAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.governance.GovernanceDocumentationContainer <em>Documentation Container</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.governance.GovernanceDocumentationContainer
	 * @generated
	 */
	public Adapter createGovernanceDocumentationContainerAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for the default case.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @generated
	 */
	public Adapter createEObjectAdapter() {
		return null;
	}

} //GovernanceAdapterFactory
