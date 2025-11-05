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
package org.eclipse.fennec.model.atlas.policy.iso9001.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.fennec.model.atlas.governance.impl.ComplianceCheckResultImpl;

import org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001Package;
import org.eclipse.fennec.model.atlas.policy.iso9001.ISO9001PolicyCheck;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Policy Check</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.iso9001.impl.ISO9001PolicyCheckImpl#isProcessApproachApplied <em>Process Approach Applied</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.iso9001.impl.ISO9001PolicyCheckImpl#isDocumentedInformationMaintained <em>Documented Information Maintained</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.iso9001.impl.ISO9001PolicyCheckImpl#isCustomerFocusDemonstrated <em>Customer Focus Demonstrated</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.iso9001.impl.ISO9001PolicyCheckImpl#isNonconformityProcessExists <em>Nonconformity Process Exists</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.iso9001.impl.ISO9001PolicyCheckImpl#isResourceAndCompetenceVerified <em>Resource And Competence Verified</em>}</li>
 * </ul>
 *
 * @generated
 */
public class ISO9001PolicyCheckImpl extends ComplianceCheckResultImpl implements ISO9001PolicyCheck {
	/**
	 * The default value of the '{@link #isProcessApproachApplied() <em>Process Approach Applied</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isProcessApproachApplied()
	 * @generated
	 * @ordered
	 */
	protected static final boolean PROCESS_APPROACH_APPLIED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isProcessApproachApplied() <em>Process Approach Applied</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isProcessApproachApplied()
	 * @generated
	 * @ordered
	 */
	protected boolean processApproachApplied = PROCESS_APPROACH_APPLIED_EDEFAULT;

	/**
	 * The default value of the '{@link #isDocumentedInformationMaintained() <em>Documented Information Maintained</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isDocumentedInformationMaintained()
	 * @generated
	 * @ordered
	 */
	protected static final boolean DOCUMENTED_INFORMATION_MAINTAINED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isDocumentedInformationMaintained() <em>Documented Information Maintained</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isDocumentedInformationMaintained()
	 * @generated
	 * @ordered
	 */
	protected boolean documentedInformationMaintained = DOCUMENTED_INFORMATION_MAINTAINED_EDEFAULT;

	/**
	 * The default value of the '{@link #isCustomerFocusDemonstrated() <em>Customer Focus Demonstrated</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isCustomerFocusDemonstrated()
	 * @generated
	 * @ordered
	 */
	protected static final boolean CUSTOMER_FOCUS_DEMONSTRATED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isCustomerFocusDemonstrated() <em>Customer Focus Demonstrated</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isCustomerFocusDemonstrated()
	 * @generated
	 * @ordered
	 */
	protected boolean customerFocusDemonstrated = CUSTOMER_FOCUS_DEMONSTRATED_EDEFAULT;

	/**
	 * The default value of the '{@link #isNonconformityProcessExists() <em>Nonconformity Process Exists</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isNonconformityProcessExists()
	 * @generated
	 * @ordered
	 */
	protected static final boolean NONCONFORMITY_PROCESS_EXISTS_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isNonconformityProcessExists() <em>Nonconformity Process Exists</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isNonconformityProcessExists()
	 * @generated
	 * @ordered
	 */
	protected boolean nonconformityProcessExists = NONCONFORMITY_PROCESS_EXISTS_EDEFAULT;

	/**
	 * The default value of the '{@link #isResourceAndCompetenceVerified() <em>Resource And Competence Verified</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isResourceAndCompetenceVerified()
	 * @generated
	 * @ordered
	 */
	protected static final boolean RESOURCE_AND_COMPETENCE_VERIFIED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isResourceAndCompetenceVerified() <em>Resource And Competence Verified</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isResourceAndCompetenceVerified()
	 * @generated
	 * @ordered
	 */
	protected boolean resourceAndCompetenceVerified = RESOURCE_AND_COMPETENCE_VERIFIED_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected ISO9001PolicyCheckImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ISO9001Package.Literals.ISO9001_POLICY_CHECK;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isProcessApproachApplied() {
		return processApproachApplied;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setProcessApproachApplied(boolean newProcessApproachApplied) {
		boolean oldProcessApproachApplied = processApproachApplied;
		processApproachApplied = newProcessApproachApplied;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ISO9001Package.ISO9001_POLICY_CHECK__PROCESS_APPROACH_APPLIED, oldProcessApproachApplied, processApproachApplied));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isDocumentedInformationMaintained() {
		return documentedInformationMaintained;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDocumentedInformationMaintained(boolean newDocumentedInformationMaintained) {
		boolean oldDocumentedInformationMaintained = documentedInformationMaintained;
		documentedInformationMaintained = newDocumentedInformationMaintained;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ISO9001Package.ISO9001_POLICY_CHECK__DOCUMENTED_INFORMATION_MAINTAINED, oldDocumentedInformationMaintained, documentedInformationMaintained));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isCustomerFocusDemonstrated() {
		return customerFocusDemonstrated;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCustomerFocusDemonstrated(boolean newCustomerFocusDemonstrated) {
		boolean oldCustomerFocusDemonstrated = customerFocusDemonstrated;
		customerFocusDemonstrated = newCustomerFocusDemonstrated;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ISO9001Package.ISO9001_POLICY_CHECK__CUSTOMER_FOCUS_DEMONSTRATED, oldCustomerFocusDemonstrated, customerFocusDemonstrated));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isNonconformityProcessExists() {
		return nonconformityProcessExists;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setNonconformityProcessExists(boolean newNonconformityProcessExists) {
		boolean oldNonconformityProcessExists = nonconformityProcessExists;
		nonconformityProcessExists = newNonconformityProcessExists;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ISO9001Package.ISO9001_POLICY_CHECK__NONCONFORMITY_PROCESS_EXISTS, oldNonconformityProcessExists, nonconformityProcessExists));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isResourceAndCompetenceVerified() {
		return resourceAndCompetenceVerified;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setResourceAndCompetenceVerified(boolean newResourceAndCompetenceVerified) {
		boolean oldResourceAndCompetenceVerified = resourceAndCompetenceVerified;
		resourceAndCompetenceVerified = newResourceAndCompetenceVerified;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ISO9001Package.ISO9001_POLICY_CHECK__RESOURCE_AND_COMPETENCE_VERIFIED, oldResourceAndCompetenceVerified, resourceAndCompetenceVerified));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ISO9001Package.ISO9001_POLICY_CHECK__PROCESS_APPROACH_APPLIED:
				return isProcessApproachApplied();
			case ISO9001Package.ISO9001_POLICY_CHECK__DOCUMENTED_INFORMATION_MAINTAINED:
				return isDocumentedInformationMaintained();
			case ISO9001Package.ISO9001_POLICY_CHECK__CUSTOMER_FOCUS_DEMONSTRATED:
				return isCustomerFocusDemonstrated();
			case ISO9001Package.ISO9001_POLICY_CHECK__NONCONFORMITY_PROCESS_EXISTS:
				return isNonconformityProcessExists();
			case ISO9001Package.ISO9001_POLICY_CHECK__RESOURCE_AND_COMPETENCE_VERIFIED:
				return isResourceAndCompetenceVerified();
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
			case ISO9001Package.ISO9001_POLICY_CHECK__PROCESS_APPROACH_APPLIED:
				setProcessApproachApplied((Boolean)newValue);
				return;
			case ISO9001Package.ISO9001_POLICY_CHECK__DOCUMENTED_INFORMATION_MAINTAINED:
				setDocumentedInformationMaintained((Boolean)newValue);
				return;
			case ISO9001Package.ISO9001_POLICY_CHECK__CUSTOMER_FOCUS_DEMONSTRATED:
				setCustomerFocusDemonstrated((Boolean)newValue);
				return;
			case ISO9001Package.ISO9001_POLICY_CHECK__NONCONFORMITY_PROCESS_EXISTS:
				setNonconformityProcessExists((Boolean)newValue);
				return;
			case ISO9001Package.ISO9001_POLICY_CHECK__RESOURCE_AND_COMPETENCE_VERIFIED:
				setResourceAndCompetenceVerified((Boolean)newValue);
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
			case ISO9001Package.ISO9001_POLICY_CHECK__PROCESS_APPROACH_APPLIED:
				setProcessApproachApplied(PROCESS_APPROACH_APPLIED_EDEFAULT);
				return;
			case ISO9001Package.ISO9001_POLICY_CHECK__DOCUMENTED_INFORMATION_MAINTAINED:
				setDocumentedInformationMaintained(DOCUMENTED_INFORMATION_MAINTAINED_EDEFAULT);
				return;
			case ISO9001Package.ISO9001_POLICY_CHECK__CUSTOMER_FOCUS_DEMONSTRATED:
				setCustomerFocusDemonstrated(CUSTOMER_FOCUS_DEMONSTRATED_EDEFAULT);
				return;
			case ISO9001Package.ISO9001_POLICY_CHECK__NONCONFORMITY_PROCESS_EXISTS:
				setNonconformityProcessExists(NONCONFORMITY_PROCESS_EXISTS_EDEFAULT);
				return;
			case ISO9001Package.ISO9001_POLICY_CHECK__RESOURCE_AND_COMPETENCE_VERIFIED:
				setResourceAndCompetenceVerified(RESOURCE_AND_COMPETENCE_VERIFIED_EDEFAULT);
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
			case ISO9001Package.ISO9001_POLICY_CHECK__PROCESS_APPROACH_APPLIED:
				return processApproachApplied != PROCESS_APPROACH_APPLIED_EDEFAULT;
			case ISO9001Package.ISO9001_POLICY_CHECK__DOCUMENTED_INFORMATION_MAINTAINED:
				return documentedInformationMaintained != DOCUMENTED_INFORMATION_MAINTAINED_EDEFAULT;
			case ISO9001Package.ISO9001_POLICY_CHECK__CUSTOMER_FOCUS_DEMONSTRATED:
				return customerFocusDemonstrated != CUSTOMER_FOCUS_DEMONSTRATED_EDEFAULT;
			case ISO9001Package.ISO9001_POLICY_CHECK__NONCONFORMITY_PROCESS_EXISTS:
				return nonconformityProcessExists != NONCONFORMITY_PROCESS_EXISTS_EDEFAULT;
			case ISO9001Package.ISO9001_POLICY_CHECK__RESOURCE_AND_COMPETENCE_VERIFIED:
				return resourceAndCompetenceVerified != RESOURCE_AND_COMPETENCE_VERIFIED_EDEFAULT;
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
		result.append(" (processApproachApplied: ");
		result.append(processApproachApplied);
		result.append(", documentedInformationMaintained: ");
		result.append(documentedInformationMaintained);
		result.append(", customerFocusDemonstrated: ");
		result.append(customerFocusDemonstrated);
		result.append(", nonconformityProcessExists: ");
		result.append(nonconformityProcessExists);
		result.append(", resourceAndCompetenceVerified: ");
		result.append(resourceAndCompetenceVerified);
		result.append(')');
		return result.toString();
	}

} //ISO9001PolicyCheckImpl
