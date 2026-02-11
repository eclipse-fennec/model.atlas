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
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Group;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchGroupResponse;

/**
 * <!-- begin-user-doc --> An implementation of the model object '<em><b>Search
 * Group Response</b></em>'. <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 * <li>{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.SearchGroupResponseImpl#getGroups
 * <em>Groups</em>}</li>
 * <li>{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.SearchGroupResponseImpl#getCount
 * <em>Count</em>}</li>
 * </ul>
 *
 * @generated
 */
public class SearchGroupResponseImpl extends MinimalEObjectImpl.Container implements SearchGroupResponse {
    /**
     * The cached value of the '{@link #getGroups() <em>Groups</em>}' containment
     * reference list. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getGroups()
     * @generated
     * @ordered
     */
    protected EList<Group> groups;

    /**
     * The default value of the '{@link #getCount() <em>Count</em>}' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getCount()
     * @generated
     * @ordered
     */
    protected static final int COUNT_EDEFAULT = 0;

    /**
     * The cached value of the '{@link #getCount() <em>Count</em>}' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @see #getCount()
     * @generated
     * @ordered
     */
    protected int count = COUNT_EDEFAULT;

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    protected SearchGroupResponseImpl() {
        super();
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    protected EClass eStaticClass() {
        return MgmtApicurioPackage.Literals.SEARCH_GROUP_RESPONSE;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public EList<Group> getGroups() {
        if (groups == null) {
            groups = new EObjectContainmentEList<Group>(Group.class, this,
                    MgmtApicurioPackage.SEARCH_GROUP_RESPONSE__GROUPS);
        }
        return groups;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public int getCount() {
        return count;
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public void setCount(int newCount) {
        int oldCount = count;
        count = newCount;
        if (eNotificationRequired())
            eNotify(new ENotificationImpl(this, Notification.SET, MgmtApicurioPackage.SEARCH_GROUP_RESPONSE__COUNT,
                    oldCount, count));
    }

    /**
     * <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @generated
     */
    @Override
    public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
        switch (featureID) {
        case MgmtApicurioPackage.SEARCH_GROUP_RESPONSE__GROUPS:
            return ((InternalEList<?>) getGroups()).basicRemove(otherEnd, msgs);
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
        case MgmtApicurioPackage.SEARCH_GROUP_RESPONSE__GROUPS:
            return getGroups();
        case MgmtApicurioPackage.SEARCH_GROUP_RESPONSE__COUNT:
            return getCount();
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
        case MgmtApicurioPackage.SEARCH_GROUP_RESPONSE__GROUPS:
            getGroups().clear();
            getGroups().addAll((Collection<? extends Group>) newValue);
            return;
        case MgmtApicurioPackage.SEARCH_GROUP_RESPONSE__COUNT:
            setCount((Integer) newValue);
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
        case MgmtApicurioPackage.SEARCH_GROUP_RESPONSE__GROUPS:
            getGroups().clear();
            return;
        case MgmtApicurioPackage.SEARCH_GROUP_RESPONSE__COUNT:
            setCount(COUNT_EDEFAULT);
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
        case MgmtApicurioPackage.SEARCH_GROUP_RESPONSE__GROUPS:
            return groups != null && !groups.isEmpty();
        case MgmtApicurioPackage.SEARCH_GROUP_RESPONSE__COUNT:
            return count != COUNT_EDEFAULT;
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
        result.append(" (count: ");
        result.append(count);
        result.append(')');
        return result.toString();
    }

} // SearchGroupResponseImpl
