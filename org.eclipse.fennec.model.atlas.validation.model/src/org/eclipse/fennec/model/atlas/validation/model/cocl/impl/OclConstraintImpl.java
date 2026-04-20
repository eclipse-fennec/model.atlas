/*
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *   Data In Motion Consulting - initial implementation
 */
package org.eclipse.fennec.model.atlas.validation.model.cocl.impl;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EDataTypeUniqueEList;

import org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint;
import org.eclipse.fennec.model.atlas.validation.model.cocl.OclRole;
import org.eclipse.fennec.model.atlas.validation.model.cocl.Severity;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Ocl Constraint</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OclConstraintImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OclConstraintImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OclConstraintImpl#getExpression <em>Expression</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OclConstraintImpl#getSeverity <em>Severity</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OclConstraintImpl#getRole <em>Role</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OclConstraintImpl#getContextClass <em>Context Class</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OclConstraintImpl#getFeatureName <em>Feature Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OclConstraintImpl#isActive <em>Active</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OclConstraintImpl#isOverrides <em>Overrides</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.impl.OclConstraintImpl#getTargetURIs <em>Target UR Is</em>}</li>
 * </ul>
 *
 * @generated
 */
public class OclConstraintImpl extends MinimalEObjectImpl.Container implements OclConstraint {
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
	 * The default value of the '{@link #getExpression() <em>Expression</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExpression()
	 * @generated
	 * @ordered
	 */
	protected static final String EXPRESSION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getExpression() <em>Expression</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getExpression()
	 * @generated
	 * @ordered
	 */
	protected String expression = EXPRESSION_EDEFAULT;

	/**
	 * The default value of the '{@link #getSeverity() <em>Severity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSeverity()
	 * @generated
	 * @ordered
	 */
	protected static final Severity SEVERITY_EDEFAULT = Severity.ERROR;

	/**
	 * The cached value of the '{@link #getSeverity() <em>Severity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSeverity()
	 * @generated
	 * @ordered
	 */
	protected Severity severity = SEVERITY_EDEFAULT;

	/**
	 * The default value of the '{@link #getRole() <em>Role</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRole()
	 * @generated
	 * @ordered
	 */
	protected static final OclRole ROLE_EDEFAULT = OclRole.VALIDATION;

	/**
	 * The cached value of the '{@link #getRole() <em>Role</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRole()
	 * @generated
	 * @ordered
	 */
	protected OclRole role = ROLE_EDEFAULT;

	/**
	 * The default value of the '{@link #getContextClass() <em>Context Class</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContextClass()
	 * @generated
	 * @ordered
	 */
	protected static final String CONTEXT_CLASS_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getContextClass() <em>Context Class</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContextClass()
	 * @generated
	 * @ordered
	 */
	protected String contextClass = CONTEXT_CLASS_EDEFAULT;

	/**
	 * The default value of the '{@link #getFeatureName() <em>Feature Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFeatureName()
	 * @generated
	 * @ordered
	 */
	protected static final String FEATURE_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFeatureName() <em>Feature Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFeatureName()
	 * @generated
	 * @ordered
	 */
	protected String featureName = FEATURE_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #isActive() <em>Active</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isActive()
	 * @generated
	 * @ordered
	 */
	protected static final boolean ACTIVE_EDEFAULT = true;

	/**
	 * The cached value of the '{@link #isActive() <em>Active</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isActive()
	 * @generated
	 * @ordered
	 */
	protected boolean active = ACTIVE_EDEFAULT;

	/**
	 * The default value of the '{@link #isOverrides() <em>Overrides</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isOverrides()
	 * @generated
	 * @ordered
	 */
	protected static final boolean OVERRIDES_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isOverrides() <em>Overrides</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isOverrides()
	 * @generated
	 * @ordered
	 */
	protected boolean overrides = OVERRIDES_EDEFAULT;

	/**
	 * The cached value of the '{@link #getTargetURIs() <em>Target UR Is</em>}' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTargetURIs()
	 * @generated
	 * @ordered
	 */
	protected EList<String> targetURIs;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected OclConstraintImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return COCLPackage.Literals.OCL_CONSTRAINT;
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
			eNotify(new ENotificationImpl(this, Notification.SET, COCLPackage.OCL_CONSTRAINT__NAME, oldName, name));
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
			eNotify(new ENotificationImpl(this, Notification.SET, COCLPackage.OCL_CONSTRAINT__DESCRIPTION, oldDescription, description));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getExpression() {
		return expression;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setExpression(String newExpression) {
		String oldExpression = expression;
		expression = newExpression;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, COCLPackage.OCL_CONSTRAINT__EXPRESSION, oldExpression, expression));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Severity getSeverity() {
		return severity;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSeverity(Severity newSeverity) {
		Severity oldSeverity = severity;
		severity = newSeverity == null ? SEVERITY_EDEFAULT : newSeverity;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, COCLPackage.OCL_CONSTRAINT__SEVERITY, oldSeverity, severity));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public OclRole getRole() {
		return role;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setRole(OclRole newRole) {
		OclRole oldRole = role;
		role = newRole == null ? ROLE_EDEFAULT : newRole;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, COCLPackage.OCL_CONSTRAINT__ROLE, oldRole, role));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getContextClass() {
		return contextClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setContextClass(String newContextClass) {
		String oldContextClass = contextClass;
		contextClass = newContextClass;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, COCLPackage.OCL_CONSTRAINT__CONTEXT_CLASS, oldContextClass, contextClass));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getFeatureName() {
		return featureName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFeatureName(String newFeatureName) {
		String oldFeatureName = featureName;
		featureName = newFeatureName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, COCLPackage.OCL_CONSTRAINT__FEATURE_NAME, oldFeatureName, featureName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isActive() {
		return active;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setActive(boolean newActive) {
		boolean oldActive = active;
		active = newActive;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, COCLPackage.OCL_CONSTRAINT__ACTIVE, oldActive, active));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isOverrides() {
		return overrides;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setOverrides(boolean newOverrides) {
		boolean oldOverrides = overrides;
		overrides = newOverrides;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, COCLPackage.OCL_CONSTRAINT__OVERRIDES, oldOverrides, overrides));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<String> getTargetURIs() {
		if (targetURIs == null) {
			targetURIs = new EDataTypeUniqueEList<String>(String.class, this, COCLPackage.OCL_CONSTRAINT__TARGET_UR_IS);
		}
		return targetURIs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case COCLPackage.OCL_CONSTRAINT__NAME:
				return getName();
			case COCLPackage.OCL_CONSTRAINT__DESCRIPTION:
				return getDescription();
			case COCLPackage.OCL_CONSTRAINT__EXPRESSION:
				return getExpression();
			case COCLPackage.OCL_CONSTRAINT__SEVERITY:
				return getSeverity();
			case COCLPackage.OCL_CONSTRAINT__ROLE:
				return getRole();
			case COCLPackage.OCL_CONSTRAINT__CONTEXT_CLASS:
				return getContextClass();
			case COCLPackage.OCL_CONSTRAINT__FEATURE_NAME:
				return getFeatureName();
			case COCLPackage.OCL_CONSTRAINT__ACTIVE:
				return isActive();
			case COCLPackage.OCL_CONSTRAINT__OVERRIDES:
				return isOverrides();
			case COCLPackage.OCL_CONSTRAINT__TARGET_UR_IS:
				return getTargetURIs();
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
			case COCLPackage.OCL_CONSTRAINT__NAME:
				setName((String)newValue);
				return;
			case COCLPackage.OCL_CONSTRAINT__DESCRIPTION:
				setDescription((String)newValue);
				return;
			case COCLPackage.OCL_CONSTRAINT__EXPRESSION:
				setExpression((String)newValue);
				return;
			case COCLPackage.OCL_CONSTRAINT__SEVERITY:
				setSeverity((Severity)newValue);
				return;
			case COCLPackage.OCL_CONSTRAINT__ROLE:
				setRole((OclRole)newValue);
				return;
			case COCLPackage.OCL_CONSTRAINT__CONTEXT_CLASS:
				setContextClass((String)newValue);
				return;
			case COCLPackage.OCL_CONSTRAINT__FEATURE_NAME:
				setFeatureName((String)newValue);
				return;
			case COCLPackage.OCL_CONSTRAINT__ACTIVE:
				setActive((Boolean)newValue);
				return;
			case COCLPackage.OCL_CONSTRAINT__OVERRIDES:
				setOverrides((Boolean)newValue);
				return;
			case COCLPackage.OCL_CONSTRAINT__TARGET_UR_IS:
				getTargetURIs().clear();
				getTargetURIs().addAll((Collection<? extends String>)newValue);
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
			case COCLPackage.OCL_CONSTRAINT__NAME:
				setName(NAME_EDEFAULT);
				return;
			case COCLPackage.OCL_CONSTRAINT__DESCRIPTION:
				setDescription(DESCRIPTION_EDEFAULT);
				return;
			case COCLPackage.OCL_CONSTRAINT__EXPRESSION:
				setExpression(EXPRESSION_EDEFAULT);
				return;
			case COCLPackage.OCL_CONSTRAINT__SEVERITY:
				setSeverity(SEVERITY_EDEFAULT);
				return;
			case COCLPackage.OCL_CONSTRAINT__ROLE:
				setRole(ROLE_EDEFAULT);
				return;
			case COCLPackage.OCL_CONSTRAINT__CONTEXT_CLASS:
				setContextClass(CONTEXT_CLASS_EDEFAULT);
				return;
			case COCLPackage.OCL_CONSTRAINT__FEATURE_NAME:
				setFeatureName(FEATURE_NAME_EDEFAULT);
				return;
			case COCLPackage.OCL_CONSTRAINT__ACTIVE:
				setActive(ACTIVE_EDEFAULT);
				return;
			case COCLPackage.OCL_CONSTRAINT__OVERRIDES:
				setOverrides(OVERRIDES_EDEFAULT);
				return;
			case COCLPackage.OCL_CONSTRAINT__TARGET_UR_IS:
				getTargetURIs().clear();
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
			case COCLPackage.OCL_CONSTRAINT__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case COCLPackage.OCL_CONSTRAINT__DESCRIPTION:
				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
			case COCLPackage.OCL_CONSTRAINT__EXPRESSION:
				return EXPRESSION_EDEFAULT == null ? expression != null : !EXPRESSION_EDEFAULT.equals(expression);
			case COCLPackage.OCL_CONSTRAINT__SEVERITY:
				return severity != SEVERITY_EDEFAULT;
			case COCLPackage.OCL_CONSTRAINT__ROLE:
				return role != ROLE_EDEFAULT;
			case COCLPackage.OCL_CONSTRAINT__CONTEXT_CLASS:
				return CONTEXT_CLASS_EDEFAULT == null ? contextClass != null : !CONTEXT_CLASS_EDEFAULT.equals(contextClass);
			case COCLPackage.OCL_CONSTRAINT__FEATURE_NAME:
				return FEATURE_NAME_EDEFAULT == null ? featureName != null : !FEATURE_NAME_EDEFAULT.equals(featureName);
			case COCLPackage.OCL_CONSTRAINT__ACTIVE:
				return active != ACTIVE_EDEFAULT;
			case COCLPackage.OCL_CONSTRAINT__OVERRIDES:
				return overrides != OVERRIDES_EDEFAULT;
			case COCLPackage.OCL_CONSTRAINT__TARGET_UR_IS:
				return targetURIs != null && !targetURIs.isEmpty();
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
		result.append(", description: ");
		result.append(description);
		result.append(", expression: ");
		result.append(expression);
		result.append(", severity: ");
		result.append(severity);
		result.append(", role: ");
		result.append(role);
		result.append(", contextClass: ");
		result.append(contextClass);
		result.append(", featureName: ");
		result.append(featureName);
		result.append(", active: ");
		result.append(active);
		result.append(", overrides: ");
		result.append(overrides);
		result.append(", targetURIs: ");
		result.append(targetURIs);
		result.append(')');
		return result.toString();
	}

} //OclConstraintImpl
