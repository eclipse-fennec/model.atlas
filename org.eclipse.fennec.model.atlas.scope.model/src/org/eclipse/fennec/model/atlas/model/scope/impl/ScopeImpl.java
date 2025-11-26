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
 *      Mark Hoffmann - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.model.scope.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;
import org.eclipse.emf.ecore.util.EcoreEMap;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.model.atlas.model.scope.Scope;
import org.eclipse.fennec.model.atlas.model.scope.ScopePackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Scope</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.model.scope.impl.ScopeImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.model.scope.impl.ScopeImpl#getParentScope <em>Parent Scope</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.model.scope.impl.ScopeImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.model.scope.impl.ScopeImpl#getLinks <em>Links</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.model.scope.impl.ScopeImpl#getStages <em>Stages</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.model.scope.impl.ScopeImpl#getFinalStage <em>Final Stage</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.model.scope.impl.ScopeImpl#getWritableStages <em>Writable Stages</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ScopeImpl extends MinimalEObjectImpl.Container implements Scope {
	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getParentScope() <em>Parent Scope</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParentScope()
	 * @generated
	 * @ordered
	 */
	protected static final String PARENT_SCOPE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getParentScope() <em>Parent Scope</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getParentScope()
	 * @generated
	 * @ordered
	 */
	protected String parentScope = PARENT_SCOPE_EDEFAULT;

	/**
	 * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected static final String DESCRIPTION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDescription() <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDescription()
	 * @generated
	 * @ordered
	 */
	protected String description = DESCRIPTION_EDEFAULT;

	/**
	 * The cached value of the '{@link #getLinks() <em>Links</em>}' map.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLinks()
	 * @generated
	 * @ordered
	 */
	protected EMap<String, String> links;

	/**
	 * The cached value of the '{@link #getStages() <em>Stages</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStages()
	 * @generated
	 * @ordered
	 */
	protected EList<String> stages;

	/**
	 * The default value of the '{@link #getFinalStage() <em>Final Stage</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFinalStage()
	 * @generated
	 * @ordered
	 */
	protected static final String FINAL_STAGE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFinalStage() <em>Final Stage</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFinalStage()
	 * @generated
	 * @ordered
	 */
	protected String finalStage = FINAL_STAGE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getWritableStages() <em>Writable Stages</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getWritableStages()
	 * @generated
	 * @ordered
	 */
	protected EList<String> writableStages;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ScopeImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ScopePackage.Literals.SCOPE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setName(String newName) {
		String oldName = name;
		name = newName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ScopePackage.SCOPE__NAME, oldName, name));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getParentScope() {
		return parentScope;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setParentScope(String newParentScope) {
		String oldParentScope = parentScope;
		parentScope = newParentScope;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ScopePackage.SCOPE__PARENT_SCOPE, oldParentScope, parentScope));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDescription(String newDescription) {
		String oldDescription = description;
		description = newDescription;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ScopePackage.SCOPE__DESCRIPTION, oldDescription, description));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EMap<String, String> getLinks() {
		if (links == null) {
			links = new EcoreEMap<String,String>(ScopePackage.Literals.LINKS_MAP, LinksMapImpl.class, this, ScopePackage.SCOPE__LINKS);
		}
		return links;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getStages() {
		if (stages == null) {
			stages = new EDataTypeUniqueEList<String>(String.class, this, ScopePackage.SCOPE__STAGES);
		}
		return stages;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getFinalStage() {
		return finalStage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFinalStage(String newFinalStage) {
		String oldFinalStage = finalStage;
		finalStage = newFinalStage;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ScopePackage.SCOPE__FINAL_STAGE, oldFinalStage, finalStage));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getWritableStages() {
		if (writableStages == null) {
			writableStages = new EDataTypeUniqueEList<String>(String.class, this, ScopePackage.SCOPE__WRITABLE_STAGES);
		}
		return writableStages;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ScopePackage.SCOPE__LINKS:
				return ((InternalEList<?>)getLinks()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ScopePackage.SCOPE__NAME:
				return getName();
			case ScopePackage.SCOPE__PARENT_SCOPE:
				return getParentScope();
			case ScopePackage.SCOPE__DESCRIPTION:
				return getDescription();
			case ScopePackage.SCOPE__LINKS:
				if (coreType) return getLinks();
				else return getLinks().map();
			case ScopePackage.SCOPE__STAGES:
				return getStages();
			case ScopePackage.SCOPE__FINAL_STAGE:
				return getFinalStage();
			case ScopePackage.SCOPE__WRITABLE_STAGES:
				return getWritableStages();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case ScopePackage.SCOPE__NAME:
				setName((String)newValue);
				return;
			case ScopePackage.SCOPE__PARENT_SCOPE:
				setParentScope((String)newValue);
				return;
			case ScopePackage.SCOPE__DESCRIPTION:
				setDescription((String)newValue);
				return;
			case ScopePackage.SCOPE__LINKS:
				((EStructuralFeature.Setting)getLinks()).set(newValue);
				return;
			case ScopePackage.SCOPE__STAGES:
				getStages().clear();
				getStages().addAll((Collection<? extends String>)newValue);
				return;
			case ScopePackage.SCOPE__FINAL_STAGE:
				setFinalStage((String)newValue);
				return;
			case ScopePackage.SCOPE__WRITABLE_STAGES:
				getWritableStages().clear();
				getWritableStages().addAll((Collection<? extends String>)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case ScopePackage.SCOPE__NAME:
				setName(NAME_EDEFAULT);
				return;
			case ScopePackage.SCOPE__PARENT_SCOPE:
				setParentScope(PARENT_SCOPE_EDEFAULT);
				return;
			case ScopePackage.SCOPE__DESCRIPTION:
				setDescription(DESCRIPTION_EDEFAULT);
				return;
			case ScopePackage.SCOPE__LINKS:
				getLinks().clear();
				return;
			case ScopePackage.SCOPE__STAGES:
				getStages().clear();
				return;
			case ScopePackage.SCOPE__FINAL_STAGE:
				setFinalStage(FINAL_STAGE_EDEFAULT);
				return;
			case ScopePackage.SCOPE__WRITABLE_STAGES:
				getWritableStages().clear();
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case ScopePackage.SCOPE__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case ScopePackage.SCOPE__PARENT_SCOPE:
				return PARENT_SCOPE_EDEFAULT == null ? parentScope != null : !PARENT_SCOPE_EDEFAULT.equals(parentScope);
			case ScopePackage.SCOPE__DESCRIPTION:
				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
			case ScopePackage.SCOPE__LINKS:
				return links != null && !links.isEmpty();
			case ScopePackage.SCOPE__STAGES:
				return stages != null && !stages.isEmpty();
			case ScopePackage.SCOPE__FINAL_STAGE:
				return FINAL_STAGE_EDEFAULT == null ? finalStage != null : !FINAL_STAGE_EDEFAULT.equals(finalStage);
			case ScopePackage.SCOPE__WRITABLE_STAGES:
				return writableStages != null && !writableStages.isEmpty();
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (name: ");
		result.append(name);
		result.append(", parentScope: ");
		result.append(parentScope);
		result.append(", description: ");
		result.append(description);
		result.append(", stages: ");
		result.append(stages);
		result.append(", finalStage: ");
		result.append(finalStage);
		result.append(", writableStages: ");
		result.append(writableStages);
		result.append(')');
		return result.toString();
	}

} //ScopeImpl
