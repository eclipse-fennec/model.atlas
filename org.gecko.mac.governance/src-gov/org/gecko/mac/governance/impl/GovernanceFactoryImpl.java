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
package org.gecko.mac.governance.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.gecko.mac.governance.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class GovernanceFactoryImpl extends EFactoryImpl implements GovernanceFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static GovernanceFactory init() {
		try {
			GovernanceFactory theGovernanceFactory = (GovernanceFactory)EPackage.Registry.INSTANCE.getEFactory(GovernancePackage.eNS_URI);
			if (theGovernanceFactory != null) {
				return theGovernanceFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new GovernanceFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public GovernanceFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case GovernancePackage.GOVERNANCE_DOCUMENTATION: return createGovernanceDocumentation();
			case GovernancePackage.ATTRIBUTE_DEFINITION: return createAttributeDefinition();
			case GovernancePackage.FINDING: return createFinding();
			case GovernancePackage.SYSTEM_COMPONENT: return createSystemComponent();
			case GovernancePackage.POLICY: return createPolicy();
			case GovernancePackage.GOVERNANCE_SYSTEM: return createGovernanceSystem();
			case GovernancePackage.SYSTEM_COMPONENT_HOLDER: return createSystemComponentHolder();
			case GovernancePackage.POLICY_HOLDER: return createPolicyHolder();
			case GovernancePackage.HOLDER_SYSTEM_COMPONENT: return createHolderSystemComponent();
			case GovernancePackage.REQUIREMENT: return createRequirement();
			case GovernancePackage.CAPABILITY: return createCapability();
			case GovernancePackage.POLICY_FEATURE: return createPolicyFeature();
			case GovernancePackage.FEATURE_HOLDER: return createFeatureHolder();
			case GovernancePackage.POLICY_REQUIREMENT: return createPolicyRequirement();
			case GovernancePackage.FEATURE_REQUIREMENT_MAPPING: return createFeatureRequirementMapping();
			case GovernancePackage.GOVERNANCE_NAMESPACE: return createGovernanceNamespace();
			case GovernancePackage.REQUIREMENT_GROUP: return createRequirementGroup();
			case GovernancePackage.NAMESPACE_HOLDER: return createNamespaceHolder();
			case GovernancePackage.POLICY_PACK: return createPolicyPack();
			case GovernancePackage.GOVERNANCE_DOCUMENTATION_CONTAINER: return createGovernanceDocumentationContainer();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object createFromString(EDataType eDataType, String initialValue) {
		switch (eDataType.getClassifierID()) {
			case GovernancePackage.APPROVAL_STATUS:
				return createApprovalStatusFromString(eDataType, initialValue);
			case GovernancePackage.COMPLIANCE_STATUS:
				return createComplianceStatusFromString(eDataType, initialValue);
			case GovernancePackage.POLICY_TYPE:
				return createPolicyTypeFromString(eDataType, initialValue);
			case GovernancePackage.SYSTEM_COMPONENT_TYPE:
				return createSystemComponentTypeFromString(eDataType, initialValue);
			case GovernancePackage.TRUST_LEVEL:
				return createTrustLevelFromString(eDataType, initialValue);
			case GovernancePackage.FINDING_SEVERITY:
				return createFindingSeverityFromString(eDataType, initialValue);
			case GovernancePackage.REQUIREMENT_MODALITY:
				return createRequirementModalityFromString(eDataType, initialValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String convertToString(EDataType eDataType, Object instanceValue) {
		switch (eDataType.getClassifierID()) {
			case GovernancePackage.APPROVAL_STATUS:
				return convertApprovalStatusToString(eDataType, instanceValue);
			case GovernancePackage.COMPLIANCE_STATUS:
				return convertComplianceStatusToString(eDataType, instanceValue);
			case GovernancePackage.POLICY_TYPE:
				return convertPolicyTypeToString(eDataType, instanceValue);
			case GovernancePackage.SYSTEM_COMPONENT_TYPE:
				return convertSystemComponentTypeToString(eDataType, instanceValue);
			case GovernancePackage.TRUST_LEVEL:
				return convertTrustLevelToString(eDataType, instanceValue);
			case GovernancePackage.FINDING_SEVERITY:
				return convertFindingSeverityToString(eDataType, instanceValue);
			case GovernancePackage.REQUIREMENT_MODALITY:
				return convertRequirementModalityToString(eDataType, instanceValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GovernanceDocumentation createGovernanceDocumentation() {
		GovernanceDocumentationImpl governanceDocumentation = new GovernanceDocumentationImpl();
		return governanceDocumentation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public AttributeDefinition createAttributeDefinition() {
		AttributeDefinitionImpl attributeDefinition = new AttributeDefinitionImpl();
		return attributeDefinition;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Finding createFinding() {
		FindingImpl finding = new FindingImpl();
		return finding;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SystemComponent createSystemComponent() {
		SystemComponentImpl systemComponent = new SystemComponentImpl();
		return systemComponent;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Policy createPolicy() {
		PolicyImpl policy = new PolicyImpl();
		return policy;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GovernanceSystem createGovernanceSystem() {
		GovernanceSystemImpl governanceSystem = new GovernanceSystemImpl();
		return governanceSystem;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public SystemComponentHolder createSystemComponentHolder() {
		SystemComponentHolderImpl systemComponentHolder = new SystemComponentHolderImpl();
		return systemComponentHolder;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public PolicyHolder createPolicyHolder() {
		PolicyHolderImpl policyHolder = new PolicyHolderImpl();
		return policyHolder;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public HolderSystemComponent createHolderSystemComponent() {
		HolderSystemComponentImpl holderSystemComponent = new HolderSystemComponentImpl();
		return holderSystemComponent;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Requirement createRequirement() {
		RequirementImpl requirement = new RequirementImpl();
		return requirement;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Capability createCapability() {
		CapabilityImpl capability = new CapabilityImpl();
		return capability;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public PolicyFeature createPolicyFeature() {
		PolicyFeatureImpl policyFeature = new PolicyFeatureImpl();
		return policyFeature;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FeatureHolder createFeatureHolder() {
		FeatureHolderImpl featureHolder = new FeatureHolderImpl();
		return featureHolder;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public PolicyRequirement createPolicyRequirement() {
		PolicyRequirementImpl policyRequirement = new PolicyRequirementImpl();
		return policyRequirement;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FeatureRequirementMapping createFeatureRequirementMapping() {
		FeatureRequirementMappingImpl featureRequirementMapping = new FeatureRequirementMappingImpl();
		return featureRequirementMapping;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GovernanceNamespace createGovernanceNamespace() {
		GovernanceNamespaceImpl governanceNamespace = new GovernanceNamespaceImpl();
		return governanceNamespace;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RequirementGroup createRequirementGroup() {
		RequirementGroupImpl requirementGroup = new RequirementGroupImpl();
		return requirementGroup;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NamespaceHolder createNamespaceHolder() {
		NamespaceHolderImpl namespaceHolder = new NamespaceHolderImpl();
		return namespaceHolder;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public PolicyPack createPolicyPack() {
		PolicyPackImpl policyPack = new PolicyPackImpl();
		return policyPack;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GovernanceDocumentationContainer createGovernanceDocumentationContainer() {
		GovernanceDocumentationContainerImpl governanceDocumentationContainer = new GovernanceDocumentationContainerImpl();
		return governanceDocumentationContainer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ApprovalStatus createApprovalStatusFromString(EDataType eDataType, String initialValue) {
		ApprovalStatus result = ApprovalStatus.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertApprovalStatusToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ComplianceStatus createComplianceStatusFromString(EDataType eDataType, String initialValue) {
		ComplianceStatus result = ComplianceStatus.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertComplianceStatusToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public PolicyType createPolicyTypeFromString(EDataType eDataType, String initialValue) {
		PolicyType result = PolicyType.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertPolicyTypeToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SystemComponentType createSystemComponentTypeFromString(EDataType eDataType, String initialValue) {
		SystemComponentType result = SystemComponentType.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertSystemComponentTypeToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public TrustLevel createTrustLevelFromString(EDataType eDataType, String initialValue) {
		TrustLevel result = TrustLevel.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertTrustLevelToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public FindingSeverity createFindingSeverityFromString(EDataType eDataType, String initialValue) {
		FindingSeverity result = FindingSeverity.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertFindingSeverityToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public RequirementModality createRequirementModalityFromString(EDataType eDataType, String initialValue) {
		RequirementModality result = RequirementModality.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertRequirementModalityToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GovernancePackage getGovernancePackage() {
		return (GovernancePackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static GovernancePackage getPackage() {
		return GovernancePackage.eINSTANCE;
	}

} //GovernanceFactoryImpl
