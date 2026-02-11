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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.wf.workflowapi.impl;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.fennec.model.atlas.wf.workflowapi.StageTransition;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiPackage;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Stage
 * Transition</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 * <li>{@link org.eclipse.fennec.model.atlas.wf.workflowapi.impl.StageTransitionImpl#getFromStage
 * <em>From Stage</em>}</li>
 * <li>{@link org.eclipse.fennec.model.atlas.wf.workflowapi.impl.StageTransitionImpl#getToStage
 * <em>To Stage</em>}</li>
 * </ul>
 *
 * @generated
 */
public class StageTransitionImpl extends MinimalEObjectImpl.Container implements StageTransition {
    /**
     * The default value of the '{@link #getFromStage() <em>From Stage</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getFromStage()
     * @generated
     * @ordered
     */
    protected static final String FROM_STAGE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getFromStage() <em>From Stage</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getFromStage()
     * @generated
     * @ordered
     */
    protected String fromStage = FROM_STAGE_EDEFAULT;

    /**
     * The default value of the '{@link #getToStage() <em>To Stage</em>}' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getToStage()
     * @generated
     * @ordered
     */
    protected static final String TO_STAGE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getToStage() <em>To Stage</em>}' attribute.
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getToStage()
     * @generated
     * @ordered
     */
    protected String toStage = TO_STAGE_EDEFAULT;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    protected StageTransitionImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return WorkflowApiPackage.Literals.STAGE_TRANSITION;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public String getFromStage() {
        return fromStage;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void setFromStage(String newFromStage) {
        fromStage = newFromStage;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public String getToStage() {
        return toStage;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void setToStage(String newToStage) {
        toStage = newToStage;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
        case WorkflowApiPackage.STAGE_TRANSITION__FROM_STAGE:
            return getFromStage();
        case WorkflowApiPackage.STAGE_TRANSITION__TO_STAGE:
            return getToStage();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
        case WorkflowApiPackage.STAGE_TRANSITION__FROM_STAGE:
            setFromStage((String) newValue);
            return;
        case WorkflowApiPackage.STAGE_TRANSITION__TO_STAGE:
            setToStage((String) newValue);
            return;
        }
        super.eSet(featureID, newValue);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void eUnset(int featureID) {
        switch (featureID) {
        case WorkflowApiPackage.STAGE_TRANSITION__FROM_STAGE:
            setFromStage(FROM_STAGE_EDEFAULT);
            return;
        case WorkflowApiPackage.STAGE_TRANSITION__TO_STAGE:
            setToStage(TO_STAGE_EDEFAULT);
            return;
        }
        super.eUnset(featureID);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public boolean eIsSet(int featureID) {
        switch (featureID) {
        case WorkflowApiPackage.STAGE_TRANSITION__FROM_STAGE:
            return FROM_STAGE_EDEFAULT == null ? fromStage != null : !FROM_STAGE_EDEFAULT.equals(fromStage);
        case WorkflowApiPackage.STAGE_TRANSITION__TO_STAGE:
            return TO_STAGE_EDEFAULT == null ? toStage != null : !TO_STAGE_EDEFAULT.equals(toStage);
        }
        return super.eIsSet(featureID);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public String toString() {
        if (eIsProxy())
            return super.toString();

        StringBuilder result = new StringBuilder(super.toString());
        result.append(" (fromStage: ");
        result.append(fromStage);
        result.append(", toStage: ");
        result.append(toStage);
        result.append(')');
        return result.toString();
    }

} // StageTransitionImpl
