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
package org.gecko.mac.mgmt.governanceapi.util;

import org.eclipse.emf.common.notify.Adapter;
import org.eclipse.emf.common.notify.Notifier;

import org.eclipse.emf.common.notify.impl.AdapterFactoryImpl;

import org.eclipse.emf.ecore.EObject;

import org.gecko.mac.mgmt.governanceapi.*;

/**
 * <!-- begin-user-doc -->
 * The <b>Adapter Factory</b> for the model.
 * It provides an adapter <code>createXXX</code> method for each class of the model.
 * <!-- end-user-doc -->
 * @see org.gecko.mac.mgmt.governanceapi.ManagementApiPackage
 * @generated
 */
public class ManagementApiAdapterFactory extends AdapterFactoryImpl {
	/**
	 * The cached model package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected static ManagementApiPackage modelPackage;

	/**
	 * Creates an instance of the adapter factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public ManagementApiAdapterFactory() {
		if (modelPackage == null) {
			modelPackage = ManagementApiPackage.eINSTANCE;
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
	protected ManagementApiSwitch<Adapter> modelSwitch =
		new ManagementApiSwitch<Adapter>() {
			@Override
			public <T extends EObject> Adapter caseEObjectWorkflowService(EObjectWorkflowService<T> object) {
				return createEObjectWorkflowServiceAdapter();
			}
			@Override
			public Adapter caseSystemComponentGovernanceService(SystemComponentGovernanceService object) {
				return createSystemComponentGovernanceServiceAdapter();
			}
			@Override
			public Adapter caseGovernanceComplianceService(GovernanceComplianceService object) {
				return createGovernanceComplianceServiceAdapter();
			}
			@Override
			public Adapter caseGovernanceSystemRepository(GovernanceSystemRepository object) {
				return createGovernanceSystemRepositoryAdapter();
			}
			@Override
			public Adapter caseSystemComponentGovernanceConfig(SystemComponentGovernanceConfig object) {
				return createSystemComponentGovernanceConfigAdapter();
			}
			@Override
			public <T extends EObject> Adapter caseWorkflowDraftProvider(WorkflowDraftProvider<T> object) {
				return createWorkflowDraftProviderAdapter();
			}
			@Override
			public Adapter caseWorkflowComplianceProvider(WorkflowComplianceProvider object) {
				return createWorkflowComplianceProviderAdapter();
			}
			@Override
			public Adapter caseGovernanceDocumentationService(GovernanceDocumentationService object) {
				return createGovernanceDocumentationServiceAdapter();
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
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.mgmt.governanceapi.EObjectWorkflowService <em>EObject Workflow Service</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.mgmt.governanceapi.EObjectWorkflowService
	 * @generated
	 */
	public Adapter createEObjectWorkflowServiceAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.mgmt.governanceapi.SystemComponentGovernanceService <em>System Component Governance Service</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.mgmt.governanceapi.SystemComponentGovernanceService
	 * @generated
	 */
	public Adapter createSystemComponentGovernanceServiceAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.mgmt.governanceapi.GovernanceComplianceService <em>Governance Compliance Service</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.mgmt.governanceapi.GovernanceComplianceService
	 * @generated
	 */
	public Adapter createGovernanceComplianceServiceAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.mgmt.governanceapi.GovernanceSystemRepository <em>Governance System Repository</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.mgmt.governanceapi.GovernanceSystemRepository
	 * @generated
	 */
	public Adapter createGovernanceSystemRepositoryAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.mgmt.governanceapi.SystemComponentGovernanceConfig <em>System Component Governance Config</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.mgmt.governanceapi.SystemComponentGovernanceConfig
	 * @generated
	 */
	public Adapter createSystemComponentGovernanceConfigAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.mgmt.governanceapi.WorkflowDraftProvider <em>Workflow Draft Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.mgmt.governanceapi.WorkflowDraftProvider
	 * @generated
	 */
	public Adapter createWorkflowDraftProviderAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.mgmt.governanceapi.WorkflowComplianceProvider <em>Workflow Compliance Provider</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.mgmt.governanceapi.WorkflowComplianceProvider
	 * @generated
	 */
	public Adapter createWorkflowComplianceProviderAdapter() {
		return null;
	}

	/**
	 * Creates a new adapter for an object of class '{@link org.gecko.mac.mgmt.governanceapi.GovernanceDocumentationService <em>Governance Documentation Service</em>}'.
	 * <!-- begin-user-doc -->
	 * This default implementation returns null so that we can easily ignore cases;
	 * it's useful to ignore a case when inheritance will catch all the cases anyway.
	 * <!-- end-user-doc -->
	 * @return the new adapter.
	 * @see org.gecko.mac.mgmt.governanceapi.GovernanceDocumentationService
	 * @generated
	 */
	public Adapter createGovernanceDocumentationServiceAdapter() {
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

} //ManagementApiAdapterFactory
