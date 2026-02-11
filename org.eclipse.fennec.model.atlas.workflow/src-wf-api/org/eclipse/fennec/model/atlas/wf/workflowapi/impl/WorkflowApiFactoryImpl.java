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
package org.eclipse.fennec.model.atlas.wf.workflowapi.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.eclipse.fennec.model.atlas.wf.workflowapi.*;

/**
 * <!-- begin-user-doc --> An implementation of the model <b>Factory</b>. <!--
 * end-user-doc -->
 * 
 * @generated
 */
public class WorkflowApiFactoryImpl extends EFactoryImpl implements WorkflowApiFactory {
    /**
     * Creates the default factory implementation. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @generated
     */
    public static WorkflowApiFactory init() {
        try {
            WorkflowApiFactory theWorkflowApiFactory = (WorkflowApiFactory) EPackage.Registry.INSTANCE
                    .getEFactory(WorkflowApiPackage.eNS_URI);
            if (theWorkflowApiFactory != null) {
                return theWorkflowApiFactory;
            }
        } catch (Exception exception) {
            EcorePlugin.INSTANCE.log(exception);
        }
        return new WorkflowApiFactoryImpl();
    }

    /**
     * Creates an instance of the factory. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @generated
     */
    public WorkflowApiFactoryImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EObject create(EClass eClass) {
        switch (eClass.getClassifierID()) {
        case WorkflowApiPackage.STAGE:
            return (EObject) createStage();
        case WorkflowApiPackage.REGISTRY:
            return (EObject) createRegistry();
        case WorkflowApiPackage.SCOPE:
            return (EObject) createScope();
        case WorkflowApiPackage.STAGE_TRANSITION:
            return (EObject) createStageTransition();
        default:
            throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
        }
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public Stage createStage() {
        StageImpl stage = new StageImpl();
        return stage;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public Registry createRegistry() {
        RegistryImpl registry = new RegistryImpl();
        return registry;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public Scope createScope() {
        ScopeImpl scope = new ScopeImpl();
        return scope;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public StageTransition createStageTransition() {
        StageTransitionImpl stageTransition = new StageTransitionImpl();
        return stageTransition;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public WorkflowApiPackage getWorkflowApiPackage() {
        return (WorkflowApiPackage) getEPackage();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @deprecated
     * @generated
     */
    @Deprecated
    public static WorkflowApiPackage getPackage() {
        return WorkflowApiPackage.eINSTANCE;
    }

} // WorkflowApiFactoryImpl
