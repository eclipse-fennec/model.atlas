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
import org.eclipse.fennec.model.atlas.wf.workflowapi.Scope;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiPackage;

/**
 * <!-- begin-user-doc --> An implementation of the model object
 * '<em><b>Scope</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 * <li>{@link org.eclipse.fennec.model.atlas.wf.workflowapi.impl.ScopeImpl#getName
 * <em>Name</em>}</li>
 * <li>{@link org.eclipse.fennec.model.atlas.wf.workflowapi.impl.ScopeImpl#getDescription
 * <em>Description</em>}</li>
 * <li>{@link org.eclipse.fennec.model.atlas.wf.workflowapi.impl.ScopeImpl#getParentScope
 * <em>Parent Scope</em>}</li>
 * <li>{@link org.eclipse.fennec.model.atlas.wf.workflowapi.impl.ScopeImpl#getRegistries
 * <em>Registries</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ScopeImpl extends MinimalEObjectImpl.Container implements Scope {
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
     * The default value of the '{@link #getParentScope() <em>Parent Scope</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getParentScope()
     * @generated
     * @ordered
     */
    protected static final String PARENT_SCOPE_EDEFAULT = null;

    /**
     * The cached value of the '{@link #getParentScope() <em>Parent Scope</em>}'
     * attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getParentScope()
     * @generated
     * @ordered
     */
    protected String parentScope = PARENT_SCOPE_EDEFAULT;

    /**
     * The cached value of the '{@link #getRegistries() <em>Registries</em>}'
     * containment reference list. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getRegistries()
     * @generated
     * @ordered
     */
    protected EList<Registry> registries;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    protected ScopeImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return WorkflowApiPackage.Literals.SCOPE;
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
    public String getParentScope() {
        return parentScope;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void setParentScope(String newParentScope) {
        parentScope = newParentScope;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public List<Registry> getRegistries() {
        if (registries == null) {
            registries = new BasicInternalEList<Registry>(Registry.class);
        }
        return registries;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
        case WorkflowApiPackage.SCOPE__REGISTRIES:
            return ((InternalEList<?>) getRegistries()).basicRemove(otherEnd, msgs);
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
        case WorkflowApiPackage.SCOPE__NAME:
            return getName();
        case WorkflowApiPackage.SCOPE__DESCRIPTION:
            return getDescription();
        case WorkflowApiPackage.SCOPE__PARENT_SCOPE:
            return getParentScope();
        case WorkflowApiPackage.SCOPE__REGISTRIES:
            return getRegistries();
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
        case WorkflowApiPackage.SCOPE__NAME:
            setName((String) newValue);
            return;
        case WorkflowApiPackage.SCOPE__DESCRIPTION:
            setDescription((String) newValue);
            return;
        case WorkflowApiPackage.SCOPE__PARENT_SCOPE:
            setParentScope((String) newValue);
            return;
        case WorkflowApiPackage.SCOPE__REGISTRIES:
            getRegistries().clear();
            getRegistries().addAll((Collection<? extends Registry>) newValue);
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
        case WorkflowApiPackage.SCOPE__NAME:
            setName(NAME_EDEFAULT);
            return;
        case WorkflowApiPackage.SCOPE__DESCRIPTION:
            setDescription(DESCRIPTION_EDEFAULT);
            return;
        case WorkflowApiPackage.SCOPE__PARENT_SCOPE:
            setParentScope(PARENT_SCOPE_EDEFAULT);
            return;
        case WorkflowApiPackage.SCOPE__REGISTRIES:
            getRegistries().clear();
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
        case WorkflowApiPackage.SCOPE__NAME:
            return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
        case WorkflowApiPackage.SCOPE__DESCRIPTION:
            return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
        case WorkflowApiPackage.SCOPE__PARENT_SCOPE:
            return PARENT_SCOPE_EDEFAULT == null ? parentScope != null : !PARENT_SCOPE_EDEFAULT.equals(parentScope);
        case WorkflowApiPackage.SCOPE__REGISTRIES:
            return registries != null && !registries.isEmpty();
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
        result.append(", parentScope: ");
        result.append(parentScope);
        result.append(')');
        return result.toString();
    }

} // ScopeImpl
