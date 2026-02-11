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

import java.util.Collection;
import java.util.List;

import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.BasicInternalEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.model.atlas.wf.workflowapi.Registry;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Stage;
import org.eclipse.fennec.model.atlas.wf.workflowapi.StageTransition;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiPackage;

/**
 * <!-- begin-user-doc --> An implementation of the model object
 * '<em><b>Registry</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 * <li>{@link org.eclipse.fennec.model.atlas.wf.workflowapi.impl.RegistryImpl#getName
 * <em>Name</em>}</li>
 * <li>{@link org.eclipse.fennec.model.atlas.wf.workflowapi.impl.RegistryImpl#getDescription
 * <em>Description</em>}</li>
 * <li>{@link org.eclipse.fennec.model.atlas.wf.workflowapi.impl.RegistryImpl#getStages
 * <em>Stages</em>}</li>
 * <li>{@link org.eclipse.fennec.model.atlas.wf.workflowapi.impl.RegistryImpl#getAllowedTransitions
 * <em>Allowed Transitions</em>}</li>
 * </ul>
 *
 * @generated
 */
public class RegistryImpl extends MinimalEObjectImpl.Container implements Registry {
    /**
     * The default value of the '{@link #getName() <em>Name</em>}' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getName()
     * @generated
     * @ordered
     */
    protected static final String NAME_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getName() <em>Name</em>}' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getName()
     * @generated
     * @ordered
     */
    protected String name = NAME_EDEFAULT;

    /**
     * The default value of the '{@link #getDescription() <em>Description</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getDescription()
     * @generated
     * @ordered
     */
    protected static final String DESCRIPTION_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getDescription() <em>Description</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getDescription()
     * @generated
     * @ordered
     */
    protected String description = DESCRIPTION_EDEFAULT;

    /**
     * The cached value of the '{@link #getStages() <em>Stages</em>}' containment
     * reference list. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getStages()
     * @generated
     * @ordered
     */
    protected EList<Stage> stages;

    /**
     * The cached value of the '{@link #getAllowedTransitions() <em>Allowed
     * Transitions</em>}' containment reference list. <!-- begin-user-doc --> <!--
     * end-user-doc -->
     * 
     * @see #getAllowedTransitions()
     * @generated
     * @ordered
     */
    protected EList<StageTransition> allowedTransitions;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    protected RegistryImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return WorkflowApiPackage.Literals.REGISTRY;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void setName(String newName) {
        name = newName;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public String getDescription() {
        return description;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void setDescription(String newDescription) {
        description = newDescription;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public List<Stage> getStages() {
        if (stages == null) {
            stages = new BasicInternalEList<Stage>(Stage.class);
        }
        return stages;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public List<StageTransition> getAllowedTransitions() {
        if (allowedTransitions == null) {
            allowedTransitions = new BasicInternalEList<StageTransition>(StageTransition.class);
        }
        return allowedTransitions;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
        case WorkflowApiPackage.REGISTRY__STAGES:
            return ((InternalEList<?>) getStages()).basicRemove(otherEnd, msgs);
        case WorkflowApiPackage.REGISTRY__ALLOWED_TRANSITIONS:
            return ((InternalEList<?>) getAllowedTransitions()).basicRemove(otherEnd, msgs);
        }
        return super.eInverseRemove(otherEnd, featureID, msgs);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public Object eGet(int featureID, boolean resolve, boolean coreType) {
        switch (featureID) {
        case WorkflowApiPackage.REGISTRY__NAME:
            return getName();
        case WorkflowApiPackage.REGISTRY__DESCRIPTION:
            return getDescription();
        case WorkflowApiPackage.REGISTRY__STAGES:
            return getStages();
        case WorkflowApiPackage.REGISTRY__ALLOWED_TRANSITIONS:
            return getAllowedTransitions();
        }
        return super.eGet(featureID, resolve, coreType);
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @SuppressWarnings("unchecked")
    @Override
    public void eSet(int featureID, Object newValue) {
        switch (featureID) {
        case WorkflowApiPackage.REGISTRY__NAME:
            setName((String) newValue);
            return;
        case WorkflowApiPackage.REGISTRY__DESCRIPTION:
            setDescription((String) newValue);
            return;
        case WorkflowApiPackage.REGISTRY__STAGES:
            getStages().clear();
            getStages().addAll((Collection<? extends Stage>) newValue);
            return;
        case WorkflowApiPackage.REGISTRY__ALLOWED_TRANSITIONS:
            getAllowedTransitions().clear();
            getAllowedTransitions().addAll((Collection<? extends StageTransition>) newValue);
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
        case WorkflowApiPackage.REGISTRY__NAME:
            setName(NAME_EDEFAULT);
            return;
        case WorkflowApiPackage.REGISTRY__DESCRIPTION:
            setDescription(DESCRIPTION_EDEFAULT);
            return;
        case WorkflowApiPackage.REGISTRY__STAGES:
            getStages().clear();
            return;
        case WorkflowApiPackage.REGISTRY__ALLOWED_TRANSITIONS:
            getAllowedTransitions().clear();
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
        case WorkflowApiPackage.REGISTRY__NAME:
            return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
        case WorkflowApiPackage.REGISTRY__DESCRIPTION:
            return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
        case WorkflowApiPackage.REGISTRY__STAGES:
            return stages != null && !stages.isEmpty();
        case WorkflowApiPackage.REGISTRY__ALLOWED_TRANSITIONS:
            return allowedTransitions != null && !allowedTransitions.isEmpty();
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
        result.append(" (name: ");
        result.append(name);
        result.append(", description: ");
        result.append(description);
        result.append(')');
        return result.toString();
    }

} // RegistryImpl
