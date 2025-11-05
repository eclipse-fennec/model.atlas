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
package org.eclipse.fennec.model.atlas.governance.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fennec.model.atlas.governance.ApprovalStatus;
import org.eclipse.fennec.model.atlas.governance.AttributeDefinition;
import org.eclipse.fennec.model.atlas.governance.Capability;
import org.eclipse.fennec.model.atlas.governance.ComplianceCheckResult;
import org.eclipse.fennec.model.atlas.governance.ComplianceStatus;
import org.eclipse.fennec.model.atlas.governance.ComponentDependency;
import org.eclipse.fennec.model.atlas.governance.FeatureHolder;
import org.eclipse.fennec.model.atlas.governance.FeatureRequirementMapping;
import org.eclipse.fennec.model.atlas.governance.Finding;
import org.eclipse.fennec.model.atlas.governance.FindingSeverity;
import org.eclipse.fennec.model.atlas.governance.GovernanceDocumentation;
import org.eclipse.fennec.model.atlas.governance.GovernanceDocumentationContainer;
import org.eclipse.fennec.model.atlas.governance.GovernanceFactory;
import org.eclipse.fennec.model.atlas.governance.GovernanceNamespace;
import org.eclipse.fennec.model.atlas.governance.GovernancePackage;
import org.eclipse.fennec.model.atlas.governance.GovernanceSystem;
import org.eclipse.fennec.model.atlas.governance.HolderSystemComponent;
import org.eclipse.fennec.model.atlas.governance.NamespaceHolder;
import org.eclipse.fennec.model.atlas.governance.Policy;
import org.eclipse.fennec.model.atlas.governance.PolicyFeature;
import org.eclipse.fennec.model.atlas.governance.PolicyHolder;
import org.eclipse.fennec.model.atlas.governance.PolicyPack;
import org.eclipse.fennec.model.atlas.governance.PolicyRequirement;
import org.eclipse.fennec.model.atlas.governance.PolicyType;
import org.eclipse.fennec.model.atlas.governance.Requirement;
import org.eclipse.fennec.model.atlas.governance.RequirementGroup;
import org.eclipse.fennec.model.atlas.governance.RequirementModality;
import org.eclipse.fennec.model.atlas.governance.SystemComponent;
import org.eclipse.fennec.model.atlas.governance.SystemComponentHolder;
import org.eclipse.fennec.model.atlas.governance.SystemComponentType;
import org.eclipse.fennec.model.atlas.governance.SystemHolder;
import org.eclipse.fennec.model.atlas.governance.TrustLevel;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class GovernancePackageImpl extends EPackageImpl implements GovernancePackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass governanceDocumentationEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass attributeDefinitionEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass complianceCheckResultEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass findingEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass systemComponentEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass policyEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass governanceSystemEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass systemComponentHolderEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass policyHolderEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass systemHolderEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass holderSystemComponentEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass componentDependencyEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass requirementEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass capabilityEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass policyFeatureEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass featureHolderEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass policyRequirementEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass featureRequirementMappingEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass governanceNamespaceEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass requirementGroupEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass namespaceHolderEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass policyPackEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass governanceDocumentationContainerEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum approvalStatusEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum complianceStatusEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum policyTypeEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum systemComponentTypeEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum trustLevelEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum findingSeverityEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum requirementModalityEEnum = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private GovernancePackageImpl() {
		super(eNS_URI, GovernanceFactory.eINSTANCE);
	}
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 *
	 * <p>This method is used to initialize {@link GovernancePackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static GovernancePackage init() {
		if (isInited) return (GovernancePackage)EPackage.Registry.INSTANCE.getEPackage(GovernancePackage.eNS_URI);

		// Obtain or create and register package
		Object registeredGovernancePackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		GovernancePackageImpl theGovernancePackage = registeredGovernancePackage instanceof GovernancePackageImpl ? (GovernancePackageImpl)registeredGovernancePackage : new GovernancePackageImpl();

		isInited = true;

		// Create package meta-data objects
		theGovernancePackage.createPackageContents();

		// Initialize created meta-data
		theGovernancePackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theGovernancePackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(GovernancePackage.eNS_URI, theGovernancePackage);
		return theGovernancePackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGovernanceDocumentation() {
		return governanceDocumentationEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGovernanceDocumentation_ModelName() {
		return (EAttribute)governanceDocumentationEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGovernanceDocumentation_Version() {
		return (EAttribute)governanceDocumentationEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGovernanceDocumentation_Status() {
		return (EAttribute)governanceDocumentationEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGovernanceDocumentation_GenerationTimestamp() {
		return (EAttribute)governanceDocumentationEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGovernanceDocumentation_GeneratedBy() {
		return (EAttribute)governanceDocumentationEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGovernanceDocumentation_ApprovedBy() {
		return (EAttribute)governanceDocumentationEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGovernanceDocumentation_Description() {
		return (EAttribute)governanceDocumentationEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGovernanceDocumentation_Attributes() {
		return (EReference)governanceDocumentationEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGovernanceDocumentation_DataOwner() {
		return (EAttribute)governanceDocumentationEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGovernanceDocumentation_DataClassification() {
		return (EAttribute)governanceDocumentationEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGovernanceDocumentation_ComplianceChecks() {
		return (EReference)governanceDocumentationEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGovernanceDocumentation_SourceSystemComponent() {
		return (EAttribute)governanceDocumentationEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getAttributeDefinition() {
		return attributeDefinitionEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAttributeDefinition_Name() {
		return (EAttribute)attributeDefinitionEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAttributeDefinition_DataType() {
		return (EAttribute)attributeDefinitionEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAttributeDefinition_Unit() {
		return (EAttribute)attributeDefinitionEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getAttributeDefinition_Description() {
		return (EAttribute)attributeDefinitionEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getComplianceCheckResult() {
		return complianceCheckResultEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getComplianceCheckResult_Status() {
		return (EAttribute)complianceCheckResultEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getComplianceCheckResult_CheckTimestamp() {
		return (EAttribute)complianceCheckResultEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getComplianceCheckResult_Summary() {
		return (EAttribute)complianceCheckResultEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getComplianceCheckResult_Findings() {
		return (EReference)complianceCheckResultEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getFinding() {
		return findingEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFinding_Code() {
		return (EAttribute)findingEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFinding_Severity() {
		return (EAttribute)findingEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFinding_Description() {
		return (EAttribute)findingEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFinding_Component() {
		return (EAttribute)findingEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFinding_Remediation() {
		return (EAttribute)findingEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSystemComponent() {
		return systemComponentEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSystemComponent_ComponentId() {
		return (EAttribute)systemComponentEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSystemComponent_Name() {
		return (EAttribute)systemComponentEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSystemComponent_Description() {
		return (EAttribute)systemComponentEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSystemComponent_TrustLevel() {
		return (EAttribute)systemComponentEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSystemComponent_AppliedPolicies() {
		return (EReference)systemComponentEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getSystemComponent_Type() {
		return (EAttribute)systemComponentEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSystemComponent_ChildComponents() {
		return (EReference)systemComponentEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSystemComponent_ParentComponents() {
		return (EReference)systemComponentEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSystemComponent_SupervisorComponent() {
		return (EReference)systemComponentEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSystemComponent_SupervisedComponents() {
		return (EReference)systemComponentEClass.getEStructuralFeatures().get(9);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSystemComponent_Dependencies() {
		return (EReference)systemComponentEClass.getEStructuralFeatures().get(10);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSystemComponent_SupportedFeatures() {
		return (EReference)systemComponentEClass.getEStructuralFeatures().get(11);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getPolicy() {
		return policyEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPolicy_PolicyId() {
		return (EAttribute)policyEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPolicy_Type() {
		return (EAttribute)policyEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPolicy_Name() {
		return (EAttribute)policyEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPolicy_Description() {
		return (EAttribute)policyEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getPolicy_RequirementGroups() {
		return (EReference)policyEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGovernanceSystem() {
		return governanceSystemEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGovernanceSystem_Name() {
		return (EAttribute)governanceSystemEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGovernanceSystem_Description() {
		return (EAttribute)governanceSystemEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGovernanceSystem_SystemId() {
		return (EAttribute)governanceSystemEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGovernanceSystem_SubSystems() {
		return (EReference)governanceSystemEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGovernanceSystem_ReferencedSystems() {
		return (EReference)governanceSystemEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGovernanceSystem_ComponentHolders() {
		return (EReference)governanceSystemEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGovernanceSystem_PolicyHolders() {
		return (EReference)governanceSystemEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGovernanceSystem_Holders() {
		return (EReference)governanceSystemEClass.getEStructuralFeatures().get(7);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGovernanceSystem_Policies() {
		return (EReference)governanceSystemEClass.getEStructuralFeatures().get(8);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSystemComponentHolder() {
		return systemComponentHolderEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSystemComponentHolder_SystemComponents() {
		return (EReference)systemComponentHolderEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getPolicyHolder() {
		return policyHolderEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getPolicyHolder_PolicyDefinitions() {
		return (EReference)policyHolderEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getPolicyHolder_Mappings() {
		return (EReference)policyHolderEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getSystemHolder() {
		return systemHolderEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getSystemHolder_System() {
		return (EReference)systemHolderEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getHolderSystemComponent() {
		return holderSystemComponentEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getComponentDependency() {
		return componentDependencyEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getComponentDependency_Component() {
		return (EReference)componentDependencyEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getComponentDependency_Name() {
		return (EAttribute)componentDependencyEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getComponentDependency_Description() {
		return (EAttribute)componentDependencyEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getComponentDependency_ReferencedComponent() {
		return (EReference)componentDependencyEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getComponentDependency_GovernanceNamespace() {
		return (EReference)componentDependencyEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getRequirement() {
		return requirementEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getRequirement_Capability() {
		return (EReference)requirementEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getCapability() {
		return capabilityEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getCapability_Requirement() {
		return (EReference)capabilityEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getPolicyFeature() {
		return policyFeatureEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPolicyFeature_FeatureId() {
		return (EAttribute)policyFeatureEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPolicyFeature_Name() {
		return (EAttribute)policyFeatureEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPolicyFeature_Description() {
		return (EAttribute)policyFeatureEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getPolicyFeature_Parent() {
		return (EReference)policyFeatureEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getPolicyFeature_Components() {
		return (EReference)policyFeatureEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getFeatureHolder() {
		return featureHolderEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getFeatureHolder_Features() {
		return (EReference)featureHolderEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getPolicyRequirement() {
		return policyRequirementEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPolicyRequirement_RequirementId() {
		return (EAttribute)policyRequirementEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPolicyRequirement_Name() {
		return (EAttribute)policyRequirementEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPolicyRequirement_Description() {
		return (EAttribute)policyRequirementEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPolicyRequirement_DocumentationLink() {
		return (EAttribute)policyRequirementEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getPolicyRequirement_Modality() {
		return (EAttribute)policyRequirementEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getPolicyRequirement_Group() {
		return (EReference)policyRequirementEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getFeatureRequirementMapping() {
		return featureRequirementMappingEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getFeatureRequirementMapping_Justification() {
		return (EAttribute)featureRequirementMappingEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getFeatureRequirementMapping_SatisfiedRequirement() {
		return (EReference)featureRequirementMappingEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getFeatureRequirementMapping_FulfillingFeature() {
		return (EReference)featureRequirementMappingEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGovernanceNamespace() {
		return governanceNamespaceEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGovernanceNamespace_NamespaceId() {
		return (EAttribute)governanceNamespaceEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGovernanceNamespace_Name() {
		return (EAttribute)governanceNamespaceEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGovernanceNamespace_Description() {
		return (EAttribute)governanceNamespaceEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGovernanceNamespace_Version() {
		return (EAttribute)governanceNamespaceEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getGovernanceNamespace_StandardReference() {
		return (EAttribute)governanceNamespaceEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGovernanceNamespace_ParentNamespace() {
		return (EReference)governanceNamespaceEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGovernanceNamespace_ChildNamespaces() {
		return (EReference)governanceNamespaceEClass.getEStructuralFeatures().get(6);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getRequirementGroup() {
		return requirementGroupEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRequirementGroup_Name() {
		return (EAttribute)requirementGroupEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getRequirementGroup_Description() {
		return (EAttribute)requirementGroupEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getRequirementGroup_Requirements() {
		return (EReference)requirementGroupEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getRequirementGroup_SubGroups() {
		return (EReference)requirementGroupEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getRequirementGroup_Group() {
		return (EReference)requirementGroupEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getRequirementGroup_Policy() {
		return (EReference)requirementGroupEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getNamespaceHolder() {
		return namespaceHolderEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getNamespaceHolder_Namespaces() {
		return (EReference)namespaceHolderEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getPolicyPack() {
		return policyPackEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getPolicyPack_Policy() {
		return (EReference)policyPackEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getGovernanceDocumentationContainer() {
		return governanceDocumentationContainerEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getGovernanceDocumentationContainer_Documentation() {
		return (EReference)governanceDocumentationContainerEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getApprovalStatus() {
		return approvalStatusEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getComplianceStatus() {
		return complianceStatusEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getPolicyType() {
		return policyTypeEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getSystemComponentType() {
		return systemComponentTypeEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getTrustLevel() {
		return trustLevelEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getFindingSeverity() {
		return findingSeverityEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getRequirementModality() {
		return requirementModalityEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public GovernanceFactory getGovernanceFactory() {
		return (GovernanceFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		governanceDocumentationEClass = createEClass(GOVERNANCE_DOCUMENTATION);
		createEAttribute(governanceDocumentationEClass, GOVERNANCE_DOCUMENTATION__MODEL_NAME);
		createEAttribute(governanceDocumentationEClass, GOVERNANCE_DOCUMENTATION__VERSION);
		createEAttribute(governanceDocumentationEClass, GOVERNANCE_DOCUMENTATION__STATUS);
		createEAttribute(governanceDocumentationEClass, GOVERNANCE_DOCUMENTATION__GENERATION_TIMESTAMP);
		createEAttribute(governanceDocumentationEClass, GOVERNANCE_DOCUMENTATION__GENERATED_BY);
		createEAttribute(governanceDocumentationEClass, GOVERNANCE_DOCUMENTATION__APPROVED_BY);
		createEAttribute(governanceDocumentationEClass, GOVERNANCE_DOCUMENTATION__DESCRIPTION);
		createEReference(governanceDocumentationEClass, GOVERNANCE_DOCUMENTATION__ATTRIBUTES);
		createEAttribute(governanceDocumentationEClass, GOVERNANCE_DOCUMENTATION__DATA_OWNER);
		createEAttribute(governanceDocumentationEClass, GOVERNANCE_DOCUMENTATION__DATA_CLASSIFICATION);
		createEReference(governanceDocumentationEClass, GOVERNANCE_DOCUMENTATION__COMPLIANCE_CHECKS);
		createEAttribute(governanceDocumentationEClass, GOVERNANCE_DOCUMENTATION__SOURCE_SYSTEM_COMPONENT);

		attributeDefinitionEClass = createEClass(ATTRIBUTE_DEFINITION);
		createEAttribute(attributeDefinitionEClass, ATTRIBUTE_DEFINITION__NAME);
		createEAttribute(attributeDefinitionEClass, ATTRIBUTE_DEFINITION__DATA_TYPE);
		createEAttribute(attributeDefinitionEClass, ATTRIBUTE_DEFINITION__UNIT);
		createEAttribute(attributeDefinitionEClass, ATTRIBUTE_DEFINITION__DESCRIPTION);

		complianceCheckResultEClass = createEClass(COMPLIANCE_CHECK_RESULT);
		createEAttribute(complianceCheckResultEClass, COMPLIANCE_CHECK_RESULT__STATUS);
		createEAttribute(complianceCheckResultEClass, COMPLIANCE_CHECK_RESULT__CHECK_TIMESTAMP);
		createEAttribute(complianceCheckResultEClass, COMPLIANCE_CHECK_RESULT__SUMMARY);
		createEReference(complianceCheckResultEClass, COMPLIANCE_CHECK_RESULT__FINDINGS);

		findingEClass = createEClass(FINDING);
		createEAttribute(findingEClass, FINDING__CODE);
		createEAttribute(findingEClass, FINDING__SEVERITY);
		createEAttribute(findingEClass, FINDING__DESCRIPTION);
		createEAttribute(findingEClass, FINDING__COMPONENT);
		createEAttribute(findingEClass, FINDING__REMEDIATION);

		systemComponentEClass = createEClass(SYSTEM_COMPONENT);
		createEAttribute(systemComponentEClass, SYSTEM_COMPONENT__COMPONENT_ID);
		createEAttribute(systemComponentEClass, SYSTEM_COMPONENT__NAME);
		createEAttribute(systemComponentEClass, SYSTEM_COMPONENT__DESCRIPTION);
		createEAttribute(systemComponentEClass, SYSTEM_COMPONENT__TRUST_LEVEL);
		createEReference(systemComponentEClass, SYSTEM_COMPONENT__APPLIED_POLICIES);
		createEAttribute(systemComponentEClass, SYSTEM_COMPONENT__TYPE);
		createEReference(systemComponentEClass, SYSTEM_COMPONENT__CHILD_COMPONENTS);
		createEReference(systemComponentEClass, SYSTEM_COMPONENT__PARENT_COMPONENTS);
		createEReference(systemComponentEClass, SYSTEM_COMPONENT__SUPERVISOR_COMPONENT);
		createEReference(systemComponentEClass, SYSTEM_COMPONENT__SUPERVISED_COMPONENTS);
		createEReference(systemComponentEClass, SYSTEM_COMPONENT__DEPENDENCIES);
		createEReference(systemComponentEClass, SYSTEM_COMPONENT__SUPPORTED_FEATURES);

		policyEClass = createEClass(POLICY);
		createEAttribute(policyEClass, POLICY__POLICY_ID);
		createEAttribute(policyEClass, POLICY__TYPE);
		createEAttribute(policyEClass, POLICY__NAME);
		createEAttribute(policyEClass, POLICY__DESCRIPTION);
		createEReference(policyEClass, POLICY__REQUIREMENT_GROUPS);

		governanceSystemEClass = createEClass(GOVERNANCE_SYSTEM);
		createEAttribute(governanceSystemEClass, GOVERNANCE_SYSTEM__NAME);
		createEAttribute(governanceSystemEClass, GOVERNANCE_SYSTEM__DESCRIPTION);
		createEAttribute(governanceSystemEClass, GOVERNANCE_SYSTEM__SYSTEM_ID);
		createEReference(governanceSystemEClass, GOVERNANCE_SYSTEM__SUB_SYSTEMS);
		createEReference(governanceSystemEClass, GOVERNANCE_SYSTEM__REFERENCED_SYSTEMS);
		createEReference(governanceSystemEClass, GOVERNANCE_SYSTEM__COMPONENT_HOLDERS);
		createEReference(governanceSystemEClass, GOVERNANCE_SYSTEM__POLICY_HOLDERS);
		createEReference(governanceSystemEClass, GOVERNANCE_SYSTEM__HOLDERS);
		createEReference(governanceSystemEClass, GOVERNANCE_SYSTEM__POLICIES);

		systemComponentHolderEClass = createEClass(SYSTEM_COMPONENT_HOLDER);
		createEReference(systemComponentHolderEClass, SYSTEM_COMPONENT_HOLDER__SYSTEM_COMPONENTS);

		policyHolderEClass = createEClass(POLICY_HOLDER);
		createEReference(policyHolderEClass, POLICY_HOLDER__POLICY_DEFINITIONS);
		createEReference(policyHolderEClass, POLICY_HOLDER__MAPPINGS);

		systemHolderEClass = createEClass(SYSTEM_HOLDER);
		createEReference(systemHolderEClass, SYSTEM_HOLDER__SYSTEM);

		holderSystemComponentEClass = createEClass(HOLDER_SYSTEM_COMPONENT);

		componentDependencyEClass = createEClass(COMPONENT_DEPENDENCY);
		createEReference(componentDependencyEClass, COMPONENT_DEPENDENCY__COMPONENT);
		createEAttribute(componentDependencyEClass, COMPONENT_DEPENDENCY__NAME);
		createEAttribute(componentDependencyEClass, COMPONENT_DEPENDENCY__DESCRIPTION);
		createEReference(componentDependencyEClass, COMPONENT_DEPENDENCY__REFERENCED_COMPONENT);
		createEReference(componentDependencyEClass, COMPONENT_DEPENDENCY__GOVERNANCE_NAMESPACE);

		requirementEClass = createEClass(REQUIREMENT);
		createEReference(requirementEClass, REQUIREMENT__CAPABILITY);

		capabilityEClass = createEClass(CAPABILITY);
		createEReference(capabilityEClass, CAPABILITY__REQUIREMENT);

		policyFeatureEClass = createEClass(POLICY_FEATURE);
		createEAttribute(policyFeatureEClass, POLICY_FEATURE__FEATURE_ID);
		createEAttribute(policyFeatureEClass, POLICY_FEATURE__NAME);
		createEAttribute(policyFeatureEClass, POLICY_FEATURE__DESCRIPTION);
		createEReference(policyFeatureEClass, POLICY_FEATURE__PARENT);
		createEReference(policyFeatureEClass, POLICY_FEATURE__COMPONENTS);

		featureHolderEClass = createEClass(FEATURE_HOLDER);
		createEReference(featureHolderEClass, FEATURE_HOLDER__FEATURES);

		policyRequirementEClass = createEClass(POLICY_REQUIREMENT);
		createEAttribute(policyRequirementEClass, POLICY_REQUIREMENT__REQUIREMENT_ID);
		createEAttribute(policyRequirementEClass, POLICY_REQUIREMENT__NAME);
		createEAttribute(policyRequirementEClass, POLICY_REQUIREMENT__DESCRIPTION);
		createEAttribute(policyRequirementEClass, POLICY_REQUIREMENT__DOCUMENTATION_LINK);
		createEAttribute(policyRequirementEClass, POLICY_REQUIREMENT__MODALITY);
		createEReference(policyRequirementEClass, POLICY_REQUIREMENT__GROUP);

		featureRequirementMappingEClass = createEClass(FEATURE_REQUIREMENT_MAPPING);
		createEAttribute(featureRequirementMappingEClass, FEATURE_REQUIREMENT_MAPPING__JUSTIFICATION);
		createEReference(featureRequirementMappingEClass, FEATURE_REQUIREMENT_MAPPING__SATISFIED_REQUIREMENT);
		createEReference(featureRequirementMappingEClass, FEATURE_REQUIREMENT_MAPPING__FULFILLING_FEATURE);

		governanceNamespaceEClass = createEClass(GOVERNANCE_NAMESPACE);
		createEAttribute(governanceNamespaceEClass, GOVERNANCE_NAMESPACE__NAMESPACE_ID);
		createEAttribute(governanceNamespaceEClass, GOVERNANCE_NAMESPACE__NAME);
		createEAttribute(governanceNamespaceEClass, GOVERNANCE_NAMESPACE__DESCRIPTION);
		createEAttribute(governanceNamespaceEClass, GOVERNANCE_NAMESPACE__VERSION);
		createEAttribute(governanceNamespaceEClass, GOVERNANCE_NAMESPACE__STANDARD_REFERENCE);
		createEReference(governanceNamespaceEClass, GOVERNANCE_NAMESPACE__PARENT_NAMESPACE);
		createEReference(governanceNamespaceEClass, GOVERNANCE_NAMESPACE__CHILD_NAMESPACES);

		requirementGroupEClass = createEClass(REQUIREMENT_GROUP);
		createEAttribute(requirementGroupEClass, REQUIREMENT_GROUP__NAME);
		createEAttribute(requirementGroupEClass, REQUIREMENT_GROUP__DESCRIPTION);
		createEReference(requirementGroupEClass, REQUIREMENT_GROUP__REQUIREMENTS);
		createEReference(requirementGroupEClass, REQUIREMENT_GROUP__SUB_GROUPS);
		createEReference(requirementGroupEClass, REQUIREMENT_GROUP__GROUP);
		createEReference(requirementGroupEClass, REQUIREMENT_GROUP__POLICY);

		namespaceHolderEClass = createEClass(NAMESPACE_HOLDER);
		createEReference(namespaceHolderEClass, NAMESPACE_HOLDER__NAMESPACES);

		policyPackEClass = createEClass(POLICY_PACK);
		createEReference(policyPackEClass, POLICY_PACK__POLICY);

		governanceDocumentationContainerEClass = createEClass(GOVERNANCE_DOCUMENTATION_CONTAINER);
		createEReference(governanceDocumentationContainerEClass, GOVERNANCE_DOCUMENTATION_CONTAINER__DOCUMENTATION);

		// Create enums
		approvalStatusEEnum = createEEnum(APPROVAL_STATUS);
		complianceStatusEEnum = createEEnum(COMPLIANCE_STATUS);
		policyTypeEEnum = createEEnum(POLICY_TYPE);
		systemComponentTypeEEnum = createEEnum(SYSTEM_COMPONENT_TYPE);
		trustLevelEEnum = createEEnum(TRUST_LEVEL);
		findingSeverityEEnum = createEEnum(FINDING_SEVERITY);
		requirementModalityEEnum = createEEnum(REQUIREMENT_MODALITY);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes
		systemComponentEClass.getESuperTypes().add(this.getFeatureHolder());
		governanceSystemEClass.getESuperTypes().add(this.getSystemComponentHolder());
		governanceSystemEClass.getESuperTypes().add(this.getPolicyHolder());
		governanceSystemEClass.getESuperTypes().add(this.getFeatureHolder());
		governanceSystemEClass.getESuperTypes().add(this.getNamespaceHolder());
		systemComponentHolderEClass.getESuperTypes().add(this.getSystemHolder());
		policyHolderEClass.getESuperTypes().add(this.getSystemHolder());
		holderSystemComponentEClass.getESuperTypes().add(this.getSystemComponent());
		holderSystemComponentEClass.getESuperTypes().add(this.getSystemComponentHolder());
		holderSystemComponentEClass.getESuperTypes().add(this.getPolicyHolder());
		holderSystemComponentEClass.getESuperTypes().add(this.getFeatureHolder());
		requirementEClass.getESuperTypes().add(this.getComponentDependency());
		capabilityEClass.getESuperTypes().add(this.getComponentDependency());
		featureHolderEClass.getESuperTypes().add(this.getSystemHolder());
		namespaceHolderEClass.getESuperTypes().add(this.getSystemHolder());
		policyPackEClass.getESuperTypes().add(this.getNamespaceHolder());

		// Initialize classes, features, and operations; add parameters
		initEClass(governanceDocumentationEClass, GovernanceDocumentation.class, "GovernanceDocumentation", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getGovernanceDocumentation_ModelName(), ecorePackage.getEString(), "modelName", null, 0, 1, GovernanceDocumentation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGovernanceDocumentation_Version(), ecorePackage.getEString(), "version", null, 0, 1, GovernanceDocumentation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGovernanceDocumentation_Status(), this.getApprovalStatus(), "status", null, 0, 1, GovernanceDocumentation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGovernanceDocumentation_GenerationTimestamp(), ecorePackage.getEDate(), "generationTimestamp", null, 0, 1, GovernanceDocumentation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGovernanceDocumentation_GeneratedBy(), ecorePackage.getEString(), "generatedBy", null, 0, 1, GovernanceDocumentation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGovernanceDocumentation_ApprovedBy(), ecorePackage.getEString(), "approvedBy", null, 0, 1, GovernanceDocumentation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGovernanceDocumentation_Description(), ecorePackage.getEString(), "description", null, 0, 1, GovernanceDocumentation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getGovernanceDocumentation_Attributes(), this.getAttributeDefinition(), null, "attributes", null, 0, -1, GovernanceDocumentation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGovernanceDocumentation_DataOwner(), ecorePackage.getEString(), "dataOwner", null, 0, 1, GovernanceDocumentation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGovernanceDocumentation_DataClassification(), ecorePackage.getEString(), "dataClassification", null, 0, 1, GovernanceDocumentation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getGovernanceDocumentation_ComplianceChecks(), this.getComplianceCheckResult(), null, "complianceChecks", null, 0, -1, GovernanceDocumentation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGovernanceDocumentation_SourceSystemComponent(), ecorePackage.getEString(), "sourceSystemComponent", null, 0, 1, GovernanceDocumentation.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(attributeDefinitionEClass, AttributeDefinition.class, "AttributeDefinition", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getAttributeDefinition_Name(), ecorePackage.getEString(), "name", null, 0, 1, AttributeDefinition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAttributeDefinition_DataType(), ecorePackage.getEString(), "dataType", null, 0, 1, AttributeDefinition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAttributeDefinition_Unit(), ecorePackage.getEString(), "unit", null, 0, 1, AttributeDefinition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getAttributeDefinition_Description(), ecorePackage.getEString(), "description", null, 0, 1, AttributeDefinition.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(complianceCheckResultEClass, ComplianceCheckResult.class, "ComplianceCheckResult", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getComplianceCheckResult_Status(), this.getComplianceStatus(), "status", null, 0, 1, ComplianceCheckResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getComplianceCheckResult_CheckTimestamp(), ecorePackage.getEDate(), "checkTimestamp", null, 0, 1, ComplianceCheckResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getComplianceCheckResult_Summary(), ecorePackage.getEString(), "summary", "A short summary of the check result.", 0, 1, ComplianceCheckResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getComplianceCheckResult_Findings(), this.getFinding(), null, "findings", null, 0, -1, ComplianceCheckResult.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(findingEClass, Finding.class, "Finding", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getFinding_Code(), ecorePackage.getEString(), "code", null, 0, 1, Finding.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFinding_Severity(), this.getFindingSeverity(), "severity", "INFO", 1, 1, Finding.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFinding_Description(), ecorePackage.getEString(), "description", null, 1, 1, Finding.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFinding_Component(), ecorePackage.getEString(), "component", null, 0, 1, Finding.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getFinding_Remediation(), ecorePackage.getEString(), "remediation", null, 0, 1, Finding.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(systemComponentEClass, SystemComponent.class, "SystemComponent", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getSystemComponent_ComponentId(), ecorePackage.getEString(), "componentId", null, 1, 1, SystemComponent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSystemComponent_Name(), ecorePackage.getEString(), "name", null, 1, 1, SystemComponent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSystemComponent_Description(), ecorePackage.getEString(), "description", null, 0, 1, SystemComponent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getSystemComponent_TrustLevel(), this.getTrustLevel(), "trustLevel", "UNKNOWN", 0, 1, SystemComponent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getSystemComponent_AppliedPolicies(), this.getPolicy(), null, "appliedPolicies", null, 0, -1, SystemComponent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getSystemComponent_AppliedPolicies().getEKeys().add(this.getPolicy_PolicyId());
		initEAttribute(getSystemComponent_Type(), this.getSystemComponentType(), "type", null, 0, 1, SystemComponent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getSystemComponent_ChildComponents(), this.getSystemComponent(), this.getSystemComponent_ParentComponents(), "childComponents", null, 0, -1, SystemComponent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getSystemComponent_ChildComponents().getEKeys().add(this.getSystemComponent_ComponentId());
		initEReference(getSystemComponent_ParentComponents(), this.getSystemComponent(), this.getSystemComponent_ChildComponents(), "parentComponents", null, 0, -1, SystemComponent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getSystemComponent_ParentComponents().getEKeys().add(this.getSystemComponent_ComponentId());
		initEReference(getSystemComponent_SupervisorComponent(), this.getSystemComponent(), this.getSystemComponent_SupervisedComponents(), "supervisorComponent", null, 0, 1, SystemComponent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getSystemComponent_SupervisorComponent().getEKeys().add(this.getSystemComponent_ComponentId());
		initEReference(getSystemComponent_SupervisedComponents(), this.getSystemComponent(), this.getSystemComponent_SupervisorComponent(), "supervisedComponents", null, 0, -1, SystemComponent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getSystemComponent_SupervisedComponents().getEKeys().add(this.getSystemComponent_ComponentId());
		initEReference(getSystemComponent_Dependencies(), this.getComponentDependency(), this.getComponentDependency_Component(), "dependencies", null, 0, -1, SystemComponent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getSystemComponent_SupportedFeatures(), this.getPolicyFeature(), this.getPolicyFeature_Components(), "supportedFeatures", null, 0, -1, SystemComponent.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getSystemComponent_SupportedFeatures().getEKeys().add(this.getPolicyFeature_FeatureId());

		initEClass(policyEClass, Policy.class, "Policy", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getPolicy_PolicyId(), ecorePackage.getEString(), "policyId", null, 1, 1, Policy.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPolicy_Type(), this.getPolicyType(), "type", null, 0, 1, Policy.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPolicy_Name(), ecorePackage.getEString(), "name", null, 1, 1, Policy.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPolicy_Description(), ecorePackage.getEString(), "description", null, 0, 1, Policy.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getPolicy_RequirementGroups(), this.getRequirementGroup(), this.getRequirementGroup_Policy(), "requirementGroups", null, 0, -1, Policy.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(governanceSystemEClass, GovernanceSystem.class, "GovernanceSystem", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getGovernanceSystem_Name(), ecorePackage.getEString(), "name", null, 1, 1, GovernanceSystem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGovernanceSystem_Description(), ecorePackage.getEString(), "description", null, 0, 1, GovernanceSystem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGovernanceSystem_SystemId(), ecorePackage.getEString(), "systemId", null, 1, 1, GovernanceSystem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getGovernanceSystem_SubSystems(), this.getGovernanceSystem(), null, "subSystems", null, 0, -1, GovernanceSystem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getGovernanceSystem_SubSystems().getEKeys().add(this.getGovernanceSystem_SystemId());
		initEReference(getGovernanceSystem_ReferencedSystems(), this.getGovernanceSystem(), null, "referencedSystems", null, 0, -1, GovernanceSystem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getGovernanceSystem_ReferencedSystems().getEKeys().add(this.getGovernanceSystem_SystemId());
		initEReference(getGovernanceSystem_ComponentHolders(), this.getSystemComponentHolder(), null, "componentHolders", null, 0, -1, GovernanceSystem.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
		initEReference(getGovernanceSystem_PolicyHolders(), this.getPolicyHolder(), null, "policyHolders", null, 0, -1, GovernanceSystem.class, IS_TRANSIENT, IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, IS_DERIVED, IS_ORDERED);
		initEReference(getGovernanceSystem_Holders(), this.getSystemHolder(), this.getSystemHolder_System(), "holders", null, 0, -1, GovernanceSystem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getGovernanceSystem_Policies(), this.getPolicy(), null, "policies", null, 0, -1, GovernanceSystem.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getGovernanceSystem_Policies().getEKeys().add(this.getPolicy_PolicyId());

		initEClass(systemComponentHolderEClass, SystemComponentHolder.class, "SystemComponentHolder", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getSystemComponentHolder_SystemComponents(), this.getSystemComponent(), null, "systemComponents", null, 0, -1, SystemComponentHolder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getSystemComponentHolder_SystemComponents().getEKeys().add(this.getSystemComponent_ComponentId());

		initEClass(policyHolderEClass, PolicyHolder.class, "PolicyHolder", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getPolicyHolder_PolicyDefinitions(), this.getPolicy(), null, "policyDefinitions", null, 0, -1, PolicyHolder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getPolicyHolder_PolicyDefinitions().getEKeys().add(this.getPolicy_PolicyId());
		initEReference(getPolicyHolder_Mappings(), this.getFeatureRequirementMapping(), null, "mappings", null, 0, -1, PolicyHolder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(systemHolderEClass, SystemHolder.class, "SystemHolder", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getSystemHolder_System(), this.getGovernanceSystem(), this.getGovernanceSystem_Holders(), "system", null, 0, 1, SystemHolder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getSystemHolder_System().getEKeys().add(this.getGovernanceSystem_SystemId());

		initEClass(holderSystemComponentEClass, HolderSystemComponent.class, "HolderSystemComponent", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);

		initEClass(componentDependencyEClass, ComponentDependency.class, "ComponentDependency", IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getComponentDependency_Component(), this.getSystemComponent(), this.getSystemComponent_Dependencies(), "component", null, 1, 1, ComponentDependency.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getComponentDependency_Component().getEKeys().add(this.getSystemComponent_ComponentId());
		initEAttribute(getComponentDependency_Name(), ecorePackage.getEString(), "name", null, 1, 1, ComponentDependency.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getComponentDependency_Description(), ecorePackage.getEString(), "description", null, 0, 1, ComponentDependency.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getComponentDependency_ReferencedComponent(), this.getSystemComponent(), null, "referencedComponent", null, 0, 1, ComponentDependency.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getComponentDependency_ReferencedComponent().getEKeys().add(this.getSystemComponent_ComponentId());
		initEReference(getComponentDependency_GovernanceNamespace(), this.getGovernanceNamespace(), null, "governanceNamespace", null, 0, 1, ComponentDependency.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getComponentDependency_GovernanceNamespace().getEKeys().add(this.getGovernanceNamespace_NamespaceId());

		initEClass(requirementEClass, Requirement.class, "Requirement", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getRequirement_Capability(), this.getCapability(), this.getCapability_Requirement(), "capability", null, 0, -1, Requirement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(capabilityEClass, Capability.class, "Capability", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getCapability_Requirement(), this.getRequirement(), this.getRequirement_Capability(), "requirement", null, 0, -1, Capability.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(policyFeatureEClass, PolicyFeature.class, "PolicyFeature", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getPolicyFeature_FeatureId(), ecorePackage.getEString(), "featureId", null, 1, 1, PolicyFeature.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPolicyFeature_Name(), ecorePackage.getEString(), "name", null, 1, 1, PolicyFeature.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPolicyFeature_Description(), ecorePackage.getEString(), "description", null, 0, 1, PolicyFeature.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getPolicyFeature_Parent(), this.getFeatureHolder(), this.getFeatureHolder_Features(), "parent", null, 0, 1, PolicyFeature.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getPolicyFeature_Components(), this.getSystemComponent(), this.getSystemComponent_SupportedFeatures(), "components", null, 0, -1, PolicyFeature.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getPolicyFeature_Components().getEKeys().add(this.getSystemComponent_ComponentId());

		initEClass(featureHolderEClass, FeatureHolder.class, "FeatureHolder", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getFeatureHolder_Features(), this.getPolicyFeature(), this.getPolicyFeature_Parent(), "features", null, 0, -1, FeatureHolder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(policyRequirementEClass, PolicyRequirement.class, "PolicyRequirement", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getPolicyRequirement_RequirementId(), ecorePackage.getEString(), "requirementId", null, 1, 1, PolicyRequirement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPolicyRequirement_Name(), ecorePackage.getEString(), "name", null, 1, 1, PolicyRequirement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPolicyRequirement_Description(), ecorePackage.getEString(), "description", null, 0, 1, PolicyRequirement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPolicyRequirement_DocumentationLink(), ecorePackage.getEString(), "documentationLink", null, 0, 1, PolicyRequirement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getPolicyRequirement_Modality(), this.getRequirementModality(), "modality", "MUST", 1, 1, PolicyRequirement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getPolicyRequirement_Group(), this.getRequirementGroup(), this.getRequirementGroup_Requirements(), "group", null, 0, 1, PolicyRequirement.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(featureRequirementMappingEClass, FeatureRequirementMapping.class, "FeatureRequirementMapping", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getFeatureRequirementMapping_Justification(), ecorePackage.getEString(), "justification", null, 0, 1, FeatureRequirementMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getFeatureRequirementMapping_SatisfiedRequirement(), this.getPolicyRequirement(), null, "satisfiedRequirement", null, 1, 1, FeatureRequirementMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getFeatureRequirementMapping_SatisfiedRequirement().getEKeys().add(this.getPolicyRequirement_RequirementId());
		initEReference(getFeatureRequirementMapping_FulfillingFeature(), this.getPolicyFeature(), null, "fulfillingFeature", null, 1, 1, FeatureRequirementMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getFeatureRequirementMapping_FulfillingFeature().getEKeys().add(this.getPolicyFeature_FeatureId());

		initEClass(governanceNamespaceEClass, GovernanceNamespace.class, "GovernanceNamespace", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getGovernanceNamespace_NamespaceId(), ecorePackage.getEString(), "namespaceId", null, 1, 1, GovernanceNamespace.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGovernanceNamespace_Name(), ecorePackage.getEString(), "name", null, 1, 1, GovernanceNamespace.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGovernanceNamespace_Description(), ecorePackage.getEString(), "description", null, 0, 1, GovernanceNamespace.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGovernanceNamespace_Version(), ecorePackage.getEString(), "version", null, 0, 1, GovernanceNamespace.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getGovernanceNamespace_StandardReference(), ecorePackage.getEString(), "standardReference", null, 0, 1, GovernanceNamespace.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getGovernanceNamespace_ParentNamespace(), this.getGovernanceNamespace(), this.getGovernanceNamespace_ChildNamespaces(), "parentNamespace", null, 0, 1, GovernanceNamespace.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getGovernanceNamespace_ParentNamespace().getEKeys().add(this.getGovernanceNamespace_NamespaceId());
		initEReference(getGovernanceNamespace_ChildNamespaces(), this.getGovernanceNamespace(), this.getGovernanceNamespace_ParentNamespace(), "childNamespaces", null, 0, -1, GovernanceNamespace.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getGovernanceNamespace_ChildNamespaces().getEKeys().add(this.getGovernanceNamespace_NamespaceId());

		initEClass(requirementGroupEClass, RequirementGroup.class, "RequirementGroup", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getRequirementGroup_Name(), ecorePackage.getEString(), "name", null, 0, 1, RequirementGroup.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getRequirementGroup_Description(), ecorePackage.getEString(), "description", null, 0, 1, RequirementGroup.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getRequirementGroup_Requirements(), this.getPolicyRequirement(), this.getPolicyRequirement_Group(), "requirements", null, 0, -1, RequirementGroup.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getRequirementGroup_SubGroups(), this.getRequirementGroup(), this.getRequirementGroup_Group(), "subGroups", null, 0, -1, RequirementGroup.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getRequirementGroup_Group(), this.getRequirementGroup(), this.getRequirementGroup_SubGroups(), "group", null, 0, 1, RequirementGroup.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getRequirementGroup_Policy(), this.getPolicy(), this.getPolicy_RequirementGroups(), "policy", null, 0, 1, RequirementGroup.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(namespaceHolderEClass, NamespaceHolder.class, "NamespaceHolder", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getNamespaceHolder_Namespaces(), this.getGovernanceNamespace(), null, "namespaces", null, 0, -1, NamespaceHolder.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getNamespaceHolder_Namespaces().getEKeys().add(this.getGovernanceNamespace_NamespaceId());

		initEClass(policyPackEClass, PolicyPack.class, "PolicyPack", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getPolicyPack_Policy(), this.getPolicy(), null, "policy", null, 1, 1, PolicyPack.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_COMPOSITE, IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		getPolicyPack_Policy().getEKeys().add(this.getPolicy_PolicyId());

		initEClass(governanceDocumentationContainerEClass, GovernanceDocumentationContainer.class, "GovernanceDocumentationContainer", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEReference(getGovernanceDocumentationContainer_Documentation(), this.getGovernanceDocumentation(), null, "documentation", null, 0, -1, GovernanceDocumentationContainer.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(approvalStatusEEnum, ApprovalStatus.class, "ApprovalStatus");
		addEEnumLiteral(approvalStatusEEnum, ApprovalStatus.DRAFT);
		addEEnumLiteral(approvalStatusEEnum, ApprovalStatus.IN_REVIEW);
		addEEnumLiteral(approvalStatusEEnum, ApprovalStatus.APPROVED);
		addEEnumLiteral(approvalStatusEEnum, ApprovalStatus.REJECTED);

		initEEnum(complianceStatusEEnum, ComplianceStatus.class, "ComplianceStatus");
		addEEnumLiteral(complianceStatusEEnum, ComplianceStatus.NOT_CHECKED);
		addEEnumLiteral(complianceStatusEEnum, ComplianceStatus.PENDING);
		addEEnumLiteral(complianceStatusEEnum, ComplianceStatus.PASSED);
		addEEnumLiteral(complianceStatusEEnum, ComplianceStatus.FAILED);
		addEEnumLiteral(complianceStatusEEnum, ComplianceStatus.NOT_APPLICABLE);
		addEEnumLiteral(complianceStatusEEnum, ComplianceStatus.PARTIAL);

		initEEnum(policyTypeEEnum, PolicyType.class, "PolicyType");
		addEEnumLiteral(policyTypeEEnum, PolicyType.GDPR);
		addEEnumLiteral(policyTypeEEnum, PolicyType.EU_AI_ACT);
		addEEnumLiteral(policyTypeEEnum, PolicyType.HIPAA);
		addEEnumLiteral(policyTypeEEnum, PolicyType.ISO_27001);
		addEEnumLiteral(policyTypeEEnum, PolicyType.CRA);
		addEEnumLiteral(policyTypeEEnum, PolicyType.MDR);
		addEEnumLiteral(policyTypeEEnum, PolicyType.ISO_9001);
		addEEnumLiteral(policyTypeEEnum, PolicyType.KRITIS);
		addEEnumLiteral(policyTypeEEnum, PolicyType.DATA_QUALITY);
		addEEnumLiteral(policyTypeEEnum, PolicyType.OPEN_DATA);
		addEEnumLiteral(policyTypeEEnum, PolicyType.DINSPEC91377);

		initEEnum(systemComponentTypeEEnum, SystemComponentType.class, "SystemComponentType");
		addEEnumLiteral(systemComponentTypeEEnum, SystemComponentType.UNKNOWN);
		addEEnumLiteral(systemComponentTypeEEnum, SystemComponentType.SENSOR);
		addEEnumLiteral(systemComponentTypeEEnum, SystemComponentType.GATEWAY);
		addEEnumLiteral(systemComponentTypeEEnum, SystemComponentType.BROKER);
		addEEnumLiteral(systemComponentTypeEEnum, SystemComponentType.DATABASE);
		addEEnumLiteral(systemComponentTypeEEnum, SystemComponentType.INTERFACE);
		addEEnumLiteral(systemComponentTypeEEnum, SystemComponentType.BACKEND);
		addEEnumLiteral(systemComponentTypeEEnum, SystemComponentType.FRONTEND);
		addEEnumLiteral(systemComponentTypeEEnum, SystemComponentType.AI_SYSTEM);
		addEEnumLiteral(systemComponentTypeEEnum, SystemComponentType.FRAMEWORK);
		addEEnumLiteral(systemComponentTypeEEnum, SystemComponentType.ACTOR);

		initEEnum(trustLevelEEnum, TrustLevel.class, "TrustLevel");
		addEEnumLiteral(trustLevelEEnum, TrustLevel.UNKNOWN);
		addEEnumLiteral(trustLevelEEnum, TrustLevel.UNTRUSTED);
		addEEnumLiteral(trustLevelEEnum, TrustLevel.IN_REVIEW);
		addEEnumLiteral(trustLevelEEnum, TrustLevel.TRUSTED);

		initEEnum(findingSeverityEEnum, FindingSeverity.class, "FindingSeverity");
		addEEnumLiteral(findingSeverityEEnum, FindingSeverity.INFO);
		addEEnumLiteral(findingSeverityEEnum, FindingSeverity.WARNING);
		addEEnumLiteral(findingSeverityEEnum, FindingSeverity.ERROR);
		addEEnumLiteral(findingSeverityEEnum, FindingSeverity.CRITICAL);

		initEEnum(requirementModalityEEnum, RequirementModality.class, "RequirementModality");
		addEEnumLiteral(requirementModalityEEnum, RequirementModality.MUST);
		addEEnumLiteral(requirementModalityEEnum, RequirementModality.SHOULD);
		addEEnumLiteral(requirementModalityEEnum, RequirementModality.MAY);
		addEEnumLiteral(requirementModalityEEnum, RequirementModality.MUST_NOT);
		addEEnumLiteral(requirementModalityEEnum, RequirementModality.SHOULD_NOT);

		// Create resource
		createResource(eNS_URI);
	}

} //GovernancePackageImpl
