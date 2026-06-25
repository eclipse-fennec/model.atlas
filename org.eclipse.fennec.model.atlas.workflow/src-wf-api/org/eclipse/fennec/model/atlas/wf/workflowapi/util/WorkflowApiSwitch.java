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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.wf.workflowapi.util;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.util.Switch;

import org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService;
import org.eclipse.fennec.model.atlas.scope.api.RegistryInfo;
import org.eclipse.fennec.model.atlas.scope.api.ScopeInfo;

import org.eclipse.fennec.model.atlas.wf.workflowapi.*;

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
 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiPackage
 * @generated
 */
	@SuppressWarnings("deprecation")
public class WorkflowApiSwitch<T1> extends Switch<T1> {
	/**
	 * The cached model package
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static WorkflowApiPackage modelPackage;

	/**
	 * Creates an instance of the switch.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public WorkflowApiSwitch() {
		if (modelPackage == null) {
			modelPackage = WorkflowApiPackage.eINSTANCE;
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
			case WorkflowApiPackage.EOBJECT_WORKFLOW_SERVICE: {
				EObjectWorkflowService<?> eObjectWorkflowService = (EObjectWorkflowService<?>)theEObject;
				T1 result = caseEObjectWorkflowService(eObjectWorkflowService);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case WorkflowApiPackage.REGISTRY_SERVICE: {
				RegistryService<?> registryService = (RegistryService<?>)theEObject;
				T1 result = caseRegistryService(registryService);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case WorkflowApiPackage.SCOPE_SERVICE: {
				ScopeService<?> scopeService = (ScopeService<?>)theEObject;
				T1 result = caseScopeService(scopeService);
				if (result == null) result = caseWritableScopeService(scopeService);
				if (result == null) result = caseReadableScopeService(scopeService);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case WorkflowApiPackage.REGISTRY: {
				Registry registry = (Registry)theEObject;
				T1 result = caseRegistry(registry);
				if (result == null) result = caseRegistryInfo(registry);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case WorkflowApiPackage.SCOPE: {
				Scope scope = (Scope)theEObject;
				T1 result = caseScope(scope);
				if (result == null) result = caseScopeInfo(scope);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case WorkflowApiPackage.STAGE_TRANSITION: {
				StageTransition stageTransition = (StageTransition)theEObject;
				T1 result = caseStageTransition(stageTransition);
				if (result == null) result = defaultCase(theEObject);
				return result;
			}
			case WorkflowApiPackage.WRITABLE_SCOPE_SERVICE: {
				WritableScopeService<?> writableScopeService = (WritableScopeService<?>)theEObject;
				T1 result = caseWritableScopeService(writableScopeService);
				if (result == null) result = caseReadableScopeService(writableScopeService);
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
	 * Returns the result of interpreting the object as an instance of '<em>Registry Service</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Registry Service</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <T extends EObject> T1 caseRegistryService(RegistryService<T> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Scope Service</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Scope Service</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @deprecated See {@link ScopeService model documentation} for details.
	 * @generated
	 */
	@Deprecated
	public <T extends EObject> T1 caseScopeService(ScopeService<T> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Registry</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Registry</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseRegistry(Registry object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Scope</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Scope</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseScope(Scope object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Stage Transition</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Stage Transition</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseStageTransition(StageTransition object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Writable Scope Service</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Writable Scope Service</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <T extends EObject> T1 caseWritableScopeService(WritableScopeService<T> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Readable Scope Service</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Readable Scope Service</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public <T extends EObject> T1 caseReadableScopeService(ReadableScopeService<T> object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Registry Info</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Registry Info</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseRegistryInfo(RegistryInfo object) {
		return null;
	}

	/**
	 * Returns the result of interpreting the object as an instance of '<em>Scope Info</em>'.
	 * <!-- begin-user-doc -->
	 * This implementation returns null;
	 * returning a non-null result will terminate the switch.
	 * <!-- end-user-doc -->
	 * @param object the target of the switch.
	 * @return the result of interpreting the object as an instance of '<em>Scope Info</em>'.
	 * @see #doSwitch(org.eclipse.emf.ecore.EObject) doSwitch(EObject)
	 * @generated
	 */
	public T1 caseScopeInfo(ScopeInfo object) {
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

} //WorkflowApiSwitch
