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
package org.gecko.mac.governance.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.gecko.mac.governance.Finding;
import org.gecko.mac.governance.FindingSeverity;
import org.gecko.mac.governance.GovernancePackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Finding</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.gecko.mac.governance.impl.FindingImpl#getCode <em>Code</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.FindingImpl#getSeverity <em>Severity</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.FindingImpl#getDescription <em>Description</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.FindingImpl#getComponent <em>Component</em>}</li>
 *   <li>{@link org.gecko.mac.governance.impl.FindingImpl#getRemediation <em>Remediation</em>}</li>
 * </ul>
 *
 * @generated
 */
public class FindingImpl extends MinimalEObjectImpl.Container implements Finding {
	/**
	 * The default value of the '{@link #getCode() <em>Code</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCode()
	 * @generated
	 * @ordered
	 */
	protected static final String CODE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCode() <em>Code</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCode()
	 * @generated
	 * @ordered
	 */
	protected String code = CODE_EDEFAULT;

	/**
	 * The default value of the '{@link #getSeverity() <em>Severity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSeverity()
	 * @generated
	 * @ordered
	 */
	protected static final FindingSeverity SEVERITY_EDEFAULT = FindingSeverity.INFO;

	/**
	 * The cached value of the '{@link #getSeverity() <em>Severity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSeverity()
	 * @generated
	 * @ordered
	 */
	protected FindingSeverity severity = SEVERITY_EDEFAULT;

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
	 * The default value of the '{@link #getComponent() <em>Component</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getComponent()
	 * @generated
	 * @ordered
	 */
	protected static final String COMPONENT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getComponent() <em>Component</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getComponent()
	 * @generated
	 * @ordered
	 */
	protected String component = COMPONENT_EDEFAULT;

	/**
	 * The default value of the '{@link #getRemediation() <em>Remediation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRemediation()
	 * @generated
	 * @ordered
	 */
	protected static final String REMEDIATION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getRemediation() <em>Remediation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRemediation()
	 * @generated
	 * @ordered
	 */
	protected String remediation = REMEDIATION_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected FindingImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GovernancePackage.Literals.FINDING;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getCode() {
		return code;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCode(String newCode) {
		String oldCode = code;
		code = newCode;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.FINDING__CODE, oldCode, code));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FindingSeverity getSeverity() {
		return severity;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSeverity(FindingSeverity newSeverity) {
		FindingSeverity oldSeverity = severity;
		severity = newSeverity == null ? SEVERITY_EDEFAULT : newSeverity;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.FINDING__SEVERITY, oldSeverity, severity));
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
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.FINDING__DESCRIPTION, oldDescription, description));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getComponent() {
		return component;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setComponent(String newComponent) {
		String oldComponent = component;
		component = newComponent;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.FINDING__COMPONENT, oldComponent, component));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getRemediation() {
		return remediation;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setRemediation(String newRemediation) {
		String oldRemediation = remediation;
		remediation = newRemediation;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GovernancePackage.FINDING__REMEDIATION, oldRemediation, remediation));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case GovernancePackage.FINDING__CODE:
				return getCode();
			case GovernancePackage.FINDING__SEVERITY:
				return getSeverity();
			case GovernancePackage.FINDING__DESCRIPTION:
				return getDescription();
			case GovernancePackage.FINDING__COMPONENT:
				return getComponent();
			case GovernancePackage.FINDING__REMEDIATION:
				return getRemediation();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case GovernancePackage.FINDING__CODE:
				setCode((String)newValue);
				return;
			case GovernancePackage.FINDING__SEVERITY:
				setSeverity((FindingSeverity)newValue);
				return;
			case GovernancePackage.FINDING__DESCRIPTION:
				setDescription((String)newValue);
				return;
			case GovernancePackage.FINDING__COMPONENT:
				setComponent((String)newValue);
				return;
			case GovernancePackage.FINDING__REMEDIATION:
				setRemediation((String)newValue);
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
			case GovernancePackage.FINDING__CODE:
				setCode(CODE_EDEFAULT);
				return;
			case GovernancePackage.FINDING__SEVERITY:
				setSeverity(SEVERITY_EDEFAULT);
				return;
			case GovernancePackage.FINDING__DESCRIPTION:
				setDescription(DESCRIPTION_EDEFAULT);
				return;
			case GovernancePackage.FINDING__COMPONENT:
				setComponent(COMPONENT_EDEFAULT);
				return;
			case GovernancePackage.FINDING__REMEDIATION:
				setRemediation(REMEDIATION_EDEFAULT);
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
			case GovernancePackage.FINDING__CODE:
				return CODE_EDEFAULT == null ? code != null : !CODE_EDEFAULT.equals(code);
			case GovernancePackage.FINDING__SEVERITY:
				return severity != SEVERITY_EDEFAULT;
			case GovernancePackage.FINDING__DESCRIPTION:
				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
			case GovernancePackage.FINDING__COMPONENT:
				return COMPONENT_EDEFAULT == null ? component != null : !COMPONENT_EDEFAULT.equals(component);
			case GovernancePackage.FINDING__REMEDIATION:
				return REMEDIATION_EDEFAULT == null ? remediation != null : !REMEDIATION_EDEFAULT.equals(remediation);
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
		result.append(" (code: ");
		result.append(code);
		result.append(", severity: ");
		result.append(severity);
		result.append(", description: ");
		result.append(description);
		result.append(", component: ");
		result.append(component);
		result.append(", remediation: ");
		result.append(remediation);
		result.append(')');
		return result.toString();
	}

} //FindingImpl
