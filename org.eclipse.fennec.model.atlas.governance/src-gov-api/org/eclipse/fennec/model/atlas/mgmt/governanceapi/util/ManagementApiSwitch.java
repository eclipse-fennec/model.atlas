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
package org.eclipse.fennec.model.atlas.mgmt.governanceapi.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.Switch;

import org.eclipse.fennec.model.atlas.mgmt.governanceapi.*;

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
 * @see org.eclipse.fennec.model.atlas.mgmt.governanceapi.ManagementApiPackage
 * @generated
 */
public class ManagementApiSwitch<T1> extends Switch<T1> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static ManagementApiPackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ManagementApiSwitch() {
		if (modelPackage == null) {
			modelPackage = ManagementApiPackage.eINSTANCE;
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
	protected T1 doSwitch(int classifierID, EObject theEObject) {
		switch (classifierID) {
			case ManagementApiPackage.EOBJECT_WORKFLOW_SERVICE: {
				EObjectWorkflowService<?> eObjectWorkflowService = (EObjectWorkflowService<?>)theEObject;
				T1 result = caseEObjectWorkflowService(eObjectWorkflowService);
				if (result == null) result = caseWorkflowDraftProvider(eObjectWorkflowService);
				if (result == null) result = caseWorkflowComplianceProvider(eObjectWorkflowService);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ManagementApiPackage.SYSTEM_COMPONENT_GOVERNANCE_SERVICE: {
				SystemComponentGovernanceService systemComponentGovernanceService = (SystemComponentGovernanceService)theEObject;
				T1 result = caseSystemComponentGovernanceService(systemComponentGovernanceService);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ManagementApiPackage.GOVERNANCE_COMPLIANCE_SERVICE: {
				GovernanceComplianceService governanceComplianceService = (GovernanceComplianceService)theEObject;
				T1 result = caseGovernanceComplianceService(governanceComplianceService);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ManagementApiPackage.GOVERNANCE_SYSTEM_REPOSITORY: {
				GovernanceSystemRepository governanceSystemRepository = (GovernanceSystemRepository)theEObject;
				T1 result = caseGovernanceSystemRepository(governanceSystemRepository);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ManagementApiPackage.SYSTEM_COMPONENT_GOVERNANCE_CONFIG: {
				SystemComponentGovernanceConfig systemComponentGovernanceConfig = (SystemComponentGovernanceConfig)theEObject;
				T1 result = caseSystemComponentGovernanceConfig(systemComponentGovernanceConfig);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ManagementApiPackage.WORKFLOW_DRAFT_PROVIDER: {
				WorkflowDraftProvider<?> workflowDraftProvider = (WorkflowDraftProvider<?>)theEObject;
				T1 result = caseWorkflowDraftProvider(workflowDraftProvider);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ManagementApiPackage.WORKFLOW_COMPLIANCE_PROVIDER: {
				WorkflowComplianceProvider workflowComplianceProvider = (WorkflowComplianceProvider)theEObject;
				T1 result = caseWorkflowComplianceProvider(workflowComplianceProvider);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case ManagementApiPackage.GOVERNANCE_DOCUMENTATION_SERVICE: {
				GovernanceDocumentationService governanceDocumentationService = (GovernanceDocumentationService)theEObject;
				T1 result = caseGovernanceDocumentationService(governanceDocumentationService);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			default: return defaultCase(theEObject);
		}
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>EObject Workflow Service</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>EObject Workflow Service</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <T extends EObject> T1 caseEObjectWorkflowService(EObjectWorkflowService<T> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>System Component Governance Service</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>System Component Governance Service</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseSystemComponentGovernanceService(SystemComponentGovernanceService object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Governance Compliance Service</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Governance Compliance Service</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseGovernanceComplianceService(GovernanceComplianceService object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Governance System Repository</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Governance System Repository</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseGovernanceSystemRepository(GovernanceSystemRepository object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>System Component Governance Config</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>System Component Governance Config</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseSystemComponentGovernanceConfig(SystemComponentGovernanceConfig object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Workflow Draft Provider</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Workflow Draft Provider</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <T extends EObject> T1 caseWorkflowDraftProvider(WorkflowDraftProvider<T> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Workflow Compliance Provider</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Workflow Compliance Provider</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseWorkflowComplianceProvider(WorkflowComplianceProvider object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Governance Documentation Service</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Governance Documentation Service</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseGovernanceDocumentationService(GovernanceDocumentationService object) {
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
	public T1 defaultCase(EObject object) {
		return null;
	}

} //ManagementApiSwitch
