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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.Switch;

import org.gecko.mac.governance.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Switch</b> for the model's inheritance hierarchy.
 * It supports the call {@link #doSwitch(EObject) doSwitch(object)}
 * to invoke the <code>caseXXX</code> method for each class of the model,
 * starting with the actual class of the object
 * and proceeding up the inheritance hierarchy
 * until a non-null result is returned,
 * which is the result of the switch.
 * <!-- end-user-doc -->
 * @see org.gecko.mac.governance.GovernancePackage
 * @generated
 */
public class GovernanceSwitch<T> extends Switch<T> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static GovernancePackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GovernanceSwitch() {
		if (modelPackage == null) {
			modelPackage = GovernancePackage.eINSTANCE;
		}
	}

	/**
	 * Checks whether this is a switch for the given package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param ePackage the package in question.
	 * @return whether this is a switch for the given package.
	 * @generated
	 */
	@Override
	protected boolean isSwitchFor(EPackage ePackage) {
		return ePackage == modelPackage;
	}

	/**
	 * Calls <code>caseXXX</code> for each class of the model until one returns a non null result; it yields that result.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the first non-null result returned by a <code>caseXXX</code> call.
	 * @generated
	 */
	@Override
	protected T doSwitch(int classifierID, EObject theEObject) {
		switch (classifierID) {
			case GovernancePackage.GOVERNANCE_DOCUMENTATION: {
				GovernanceDocumentation governanceDocumentation = (GovernanceDocumentation)theEObject;
				T result = caseGovernanceDocumentation(governanceDocumentation);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case GovernancePackage.ATTRIBUTE_DEFINITION: {
				AttributeDefinition attributeDefinition = (AttributeDefinition)theEObject;
				T result = caseAttributeDefinition(attributeDefinition);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case GovernancePackage.COMPLIANCE_CHECK_RESULT: {
				ComplianceCheckResult complianceCheckResult = (ComplianceCheckResult)theEObject;
				T result = caseComplianceCheckResult(complianceCheckResult);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case GovernancePackage.FINDING: {
				Finding finding = (Finding)theEObject;
				T result = caseFinding(finding);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case GovernancePackage.SYSTEM_COMPONENT: {
				SystemComponent systemComponent = (SystemComponent)theEObject;
				T result = caseSystemComponent(systemComponent);
				if (result == null) result = caseFeatureHolder(systemComponent);
				if (result == null) result = caseSystemHolder(systemComponent);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case GovernancePackage.POLICY: {
				Policy policy = (Policy)theEObject;
				T result = casePolicy(policy);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case GovernancePackage.GOVERNANCE_SYSTEM: {
				GovernanceSystem governanceSystem = (GovernanceSystem)theEObject;
				T result = caseGovernanceSystem(governanceSystem);
				if (result == null) result = caseSystemComponentHolder(governanceSystem);
				if (result == null) result = casePolicyHolder(governanceSystem);
				if (result == null) result = caseFeatureHolder(governanceSystem);
				if (result == null) result = caseNamespaceHolder(governanceSystem);
				if (result == null) result = caseSystemHolder(governanceSystem);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case GovernancePackage.SYSTEM_COMPONENT_HOLDER: {
				SystemComponentHolder systemComponentHolder = (SystemComponentHolder)theEObject;
				T result = caseSystemComponentHolder(systemComponentHolder);
				if (result == null) result = caseSystemHolder(systemComponentHolder);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case GovernancePackage.POLICY_HOLDER: {
				PolicyHolder policyHolder = (PolicyHolder)theEObject;
				T result = casePolicyHolder(policyHolder);
				if (result == null) result = caseSystemHolder(policyHolder);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case GovernancePackage.SYSTEM_HOLDER: {
				SystemHolder systemHolder = (SystemHolder)theEObject;
				T result = caseSystemHolder(systemHolder);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case GovernancePackage.HOLDER_SYSTEM_COMPONENT: {
				HolderSystemComponent holderSystemComponent = (HolderSystemComponent)theEObject;
				T result = caseHolderSystemComponent(holderSystemComponent);
				if (result == null) result = caseSystemComponent(holderSystemComponent);
				if (result == null) result = caseSystemComponentHolder(holderSystemComponent);
				if (result == null) result = casePolicyHolder(holderSystemComponent);
				if (result == null) result = caseFeatureHolder(holderSystemComponent);
				if (result == null) result = caseSystemHolder(holderSystemComponent);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case GovernancePackage.COMPONENT_DEPENDENCY: {
				ComponentDependency componentDependency = (ComponentDependency)theEObject;
				T result = caseComponentDependency(componentDependency);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case GovernancePackage.REQUIREMENT: {
				Requirement requirement = (Requirement)theEObject;
				T result = caseRequirement(requirement);
				if (result == null) result = caseComponentDependency(requirement);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case GovernancePackage.CAPABILITY: {
				Capability capability = (Capability)theEObject;
				T result = caseCapability(capability);
				if (result == null) result = caseComponentDependency(capability);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case GovernancePackage.POLICY_FEATURE: {
				PolicyFeature policyFeature = (PolicyFeature)theEObject;
				T result = casePolicyFeature(policyFeature);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case GovernancePackage.FEATURE_HOLDER: {
				FeatureHolder featureHolder = (FeatureHolder)theEObject;
				T result = caseFeatureHolder(featureHolder);
				if (result == null) result = caseSystemHolder(featureHolder);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case GovernancePackage.POLICY_REQUIREMENT: {
				PolicyRequirement policyRequirement = (PolicyRequirement)theEObject;
				T result = casePolicyRequirement(policyRequirement);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case GovernancePackage.FEATURE_REQUIREMENT_MAPPING: {
				FeatureRequirementMapping featureRequirementMapping = (FeatureRequirementMapping)theEObject;
				T result = caseFeatureRequirementMapping(featureRequirementMapping);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case GovernancePackage.GOVERNANCE_NAMESPACE: {
				GovernanceNamespace governanceNamespace = (GovernanceNamespace)theEObject;
				T result = caseGovernanceNamespace(governanceNamespace);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case GovernancePackage.REQUIREMENT_GROUP: {
				RequirementGroup requirementGroup = (RequirementGroup)theEObject;
				T result = caseRequirementGroup(requirementGroup);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case GovernancePackage.NAMESPACE_HOLDER: {
				NamespaceHolder namespaceHolder = (NamespaceHolder)theEObject;
				T result = caseNamespaceHolder(namespaceHolder);
				if (result == null) result = caseSystemHolder(namespaceHolder);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case GovernancePackage.POLICY_PACK: {
				PolicyPack policyPack = (PolicyPack)theEObject;
				T result = casePolicyPack(policyPack);
				if (result == null) result = caseNamespaceHolder(policyPack);
				if (result == null) result = caseSystemHolder(policyPack);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case GovernancePackage.GOVERNANCE_DOCUMENTATION_CONTAINER: {
				GovernanceDocumentationContainer governanceDocumentationContainer = (GovernanceDocumentationContainer)theEObject;
				T result = caseGovernanceDocumentationContainer(governanceDocumentationContainer);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Documentation</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Documentation</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGovernanceDocumentation(GovernanceDocumentation object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Attribute Definition</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Attribute Definition</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseAttributeDefinition(AttributeDefinition object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Compliance Check Result</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Compliance Check Result</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseComplianceCheckResult(ComplianceCheckResult object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Finding</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Finding</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseFinding(Finding object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>System Component</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>System Component</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSystemComponent(SystemComponent object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Policy</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Policy</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePolicy(Policy object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>System</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>System</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGovernanceSystem(GovernanceSystem object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>System Component Holder</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>System Component Holder</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSystemComponentHolder(SystemComponentHolder object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Policy Holder</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Policy Holder</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePolicyHolder(PolicyHolder object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>System Holder</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>System Holder</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseSystemHolder(SystemHolder object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Holder System Component</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Holder System Component</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseHolderSystemComponent(HolderSystemComponent object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Component Dependency</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Component Dependency</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseComponentDependency(ComponentDependency object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Requirement</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Requirement</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRequirement(Requirement object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Capability</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Capability</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseCapability(Capability object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Policy Feature</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Policy Feature</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePolicyFeature(PolicyFeature object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Feature Holder</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Feature Holder</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseFeatureHolder(FeatureHolder object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Policy Requirement</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Policy Requirement</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePolicyRequirement(PolicyRequirement object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Feature Requirement Mapping</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Feature Requirement Mapping</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseFeatureRequirementMapping(FeatureRequirementMapping object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Namespace</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Namespace</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGovernanceNamespace(GovernanceNamespace object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Requirement Group</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Requirement Group</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseRequirementGroup(RequirementGroup object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Namespace Holder</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Namespace Holder</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseNamespaceHolder(NamespaceHolder object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Policy Pack</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Policy Pack</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T casePolicyPack(PolicyPack object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Documentation Container</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Documentation Container</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T caseGovernanceDocumentationContainer(GovernanceDocumentationContainer object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch, but this is the last case anyway.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>EObject</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject)
	 * @generated
	 */
	@Override
	public T defaultCase(EObject object) {
		return null;
	}

} //GovernanceSwitch
