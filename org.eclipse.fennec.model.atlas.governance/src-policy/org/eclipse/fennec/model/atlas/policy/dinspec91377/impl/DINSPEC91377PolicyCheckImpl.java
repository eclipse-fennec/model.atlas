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
package org.eclipse.fennec.model.atlas.policy.dinspec91377.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.fennec.model.atlas.governance.impl.ComplianceCheckResultImpl;

import org.eclipse.fennec.model.atlas.policy.dinspec91377.DINSPEC91377PolicyCheck;
import org.eclipse.fennec.model.atlas.policy.dinspec91377.DS91377Package;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>DINSPEC91377 Policy Check</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.dinspec91377.impl.DINSPEC91377PolicyCheckImpl#isArchitectureLayersImplemented <em>Architecture Layers Implemented</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.dinspec91377.impl.DINSPEC91377PolicyCheckImpl#isModelCentricDataFlowApplied <em>Model Centric Data Flow Applied</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.dinspec91377.impl.DINSPEC91377PolicyCheckImpl#isInterfaceInteroperabilityEnsured <em>Interface Interoperability Ensured</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.dinspec91377.impl.DINSPEC91377PolicyCheckImpl#isIntegrationApiStandardsMet <em>Integration Api Standards Met</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.dinspec91377.impl.DINSPEC91377PolicyCheckImpl#isManagementServicesProvided <em>Management Services Provided</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.dinspec91377.impl.DINSPEC91377PolicyCheckImpl#isInformationSecurityConceptApplied <em>Information Security Concept Applied</em>}</li>
 * </ul>
 *
 * @generated
 */
public class DINSPEC91377PolicyCheckImpl extends ComplianceCheckResultImpl implements DINSPEC91377PolicyCheck {
	/**
	 * The default value of the '{@link #isArchitectureLayersImplemented() <em>Architecture Layers Implemented</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isArchitectureLayersImplemented()
	 * @generated
	 * @ordered
	 */
	protected static final boolean ARCHITECTURE_LAYERS_IMPLEMENTED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isArchitectureLayersImplemented() <em>Architecture Layers Implemented</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isArchitectureLayersImplemented()
	 * @generated
	 * @ordered
	 */
	protected boolean architectureLayersImplemented = ARCHITECTURE_LAYERS_IMPLEMENTED_EDEFAULT;

	/**
	 * The default value of the '{@link #isModelCentricDataFlowApplied() <em>Model Centric Data Flow Applied</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isModelCentricDataFlowApplied()
	 * @generated
	 * @ordered
	 */
	protected static final boolean MODEL_CENTRIC_DATA_FLOW_APPLIED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isModelCentricDataFlowApplied() <em>Model Centric Data Flow Applied</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isModelCentricDataFlowApplied()
	 * @generated
	 * @ordered
	 */
	protected boolean modelCentricDataFlowApplied = MODEL_CENTRIC_DATA_FLOW_APPLIED_EDEFAULT;

	/**
	 * The default value of the '{@link #isInterfaceInteroperabilityEnsured() <em>Interface Interoperability Ensured</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isInterfaceInteroperabilityEnsured()
	 * @generated
	 * @ordered
	 */
	protected static final boolean INTERFACE_INTEROPERABILITY_ENSURED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isInterfaceInteroperabilityEnsured() <em>Interface Interoperability Ensured</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isInterfaceInteroperabilityEnsured()
	 * @generated
	 * @ordered
	 */
	protected boolean interfaceInteroperabilityEnsured = INTERFACE_INTEROPERABILITY_ENSURED_EDEFAULT;

	/**
	 * The default value of the '{@link #isIntegrationApiStandardsMet() <em>Integration Api Standards Met</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIntegrationApiStandardsMet()
	 * @generated
	 * @ordered
	 */
	protected static final boolean INTEGRATION_API_STANDARDS_MET_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isIntegrationApiStandardsMet() <em>Integration Api Standards Met</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isIntegrationApiStandardsMet()
	 * @generated
	 * @ordered
	 */
	protected boolean integrationApiStandardsMet = INTEGRATION_API_STANDARDS_MET_EDEFAULT;

	/**
	 * The default value of the '{@link #isManagementServicesProvided() <em>Management Services Provided</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isManagementServicesProvided()
	 * @generated
	 * @ordered
	 */
	protected static final boolean MANAGEMENT_SERVICES_PROVIDED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isManagementServicesProvided() <em>Management Services Provided</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isManagementServicesProvided()
	 * @generated
	 * @ordered
	 */
	protected boolean managementServicesProvided = MANAGEMENT_SERVICES_PROVIDED_EDEFAULT;

	/**
	 * The default value of the '{@link #isInformationSecurityConceptApplied() <em>Information Security Concept Applied</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isInformationSecurityConceptApplied()
	 * @generated
	 * @ordered
	 */
	protected static final boolean INFORMATION_SECURITY_CONCEPT_APPLIED_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isInformationSecurityConceptApplied() <em>Information Security Concept Applied</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isInformationSecurityConceptApplied()
	 * @generated
	 * @ordered
	 */
	protected boolean informationSecurityConceptApplied = INFORMATION_SECURITY_CONCEPT_APPLIED_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DINSPEC91377PolicyCheckImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return DS91377Package.Literals.DINSPEC91377_POLICY_CHECK;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isArchitectureLayersImplemented() {
		return architectureLayersImplemented;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setArchitectureLayersImplemented(boolean newArchitectureLayersImplemented) {
		boolean oldArchitectureLayersImplemented = architectureLayersImplemented;
		architectureLayersImplemented = newArchitectureLayersImplemented;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DS91377Package.DINSPEC91377_POLICY_CHECK__ARCHITECTURE_LAYERS_IMPLEMENTED, oldArchitectureLayersImplemented, architectureLayersImplemented));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isModelCentricDataFlowApplied() {
		return modelCentricDataFlowApplied;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setModelCentricDataFlowApplied(boolean newModelCentricDataFlowApplied) {
		boolean oldModelCentricDataFlowApplied = modelCentricDataFlowApplied;
		modelCentricDataFlowApplied = newModelCentricDataFlowApplied;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DS91377Package.DINSPEC91377_POLICY_CHECK__MODEL_CENTRIC_DATA_FLOW_APPLIED, oldModelCentricDataFlowApplied, modelCentricDataFlowApplied));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isInterfaceInteroperabilityEnsured() {
		return interfaceInteroperabilityEnsured;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setInterfaceInteroperabilityEnsured(boolean newInterfaceInteroperabilityEnsured) {
		boolean oldInterfaceInteroperabilityEnsured = interfaceInteroperabilityEnsured;
		interfaceInteroperabilityEnsured = newInterfaceInteroperabilityEnsured;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DS91377Package.DINSPEC91377_POLICY_CHECK__INTERFACE_INTEROPERABILITY_ENSURED, oldInterfaceInteroperabilityEnsured, interfaceInteroperabilityEnsured));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isIntegrationApiStandardsMet() {
		return integrationApiStandardsMet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setIntegrationApiStandardsMet(boolean newIntegrationApiStandardsMet) {
		boolean oldIntegrationApiStandardsMet = integrationApiStandardsMet;
		integrationApiStandardsMet = newIntegrationApiStandardsMet;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DS91377Package.DINSPEC91377_POLICY_CHECK__INTEGRATION_API_STANDARDS_MET, oldIntegrationApiStandardsMet, integrationApiStandardsMet));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isManagementServicesProvided() {
		return managementServicesProvided;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setManagementServicesProvided(boolean newManagementServicesProvided) {
		boolean oldManagementServicesProvided = managementServicesProvided;
		managementServicesProvided = newManagementServicesProvided;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DS91377Package.DINSPEC91377_POLICY_CHECK__MANAGEMENT_SERVICES_PROVIDED, oldManagementServicesProvided, managementServicesProvided));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isInformationSecurityConceptApplied() {
		return informationSecurityConceptApplied;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setInformationSecurityConceptApplied(boolean newInformationSecurityConceptApplied) {
		boolean oldInformationSecurityConceptApplied = informationSecurityConceptApplied;
		informationSecurityConceptApplied = newInformationSecurityConceptApplied;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DS91377Package.DINSPEC91377_POLICY_CHECK__INFORMATION_SECURITY_CONCEPT_APPLIED, oldInformationSecurityConceptApplied, informationSecurityConceptApplied));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case DS91377Package.DINSPEC91377_POLICY_CHECK__ARCHITECTURE_LAYERS_IMPLEMENTED:
				return isArchitectureLayersImplemented();
			case DS91377Package.DINSPEC91377_POLICY_CHECK__MODEL_CENTRIC_DATA_FLOW_APPLIED:
				return isModelCentricDataFlowApplied();
			case DS91377Package.DINSPEC91377_POLICY_CHECK__INTERFACE_INTEROPERABILITY_ENSURED:
				return isInterfaceInteroperabilityEnsured();
			case DS91377Package.DINSPEC91377_POLICY_CHECK__INTEGRATION_API_STANDARDS_MET:
				return isIntegrationApiStandardsMet();
			case DS91377Package.DINSPEC91377_POLICY_CHECK__MANAGEMENT_SERVICES_PROVIDED:
				return isManagementServicesProvided();
			case DS91377Package.DINSPEC91377_POLICY_CHECK__INFORMATION_SECURITY_CONCEPT_APPLIED:
				return isInformationSecurityConceptApplied();
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
			case DS91377Package.DINSPEC91377_POLICY_CHECK__ARCHITECTURE_LAYERS_IMPLEMENTED:
				setArchitectureLayersImplemented((Boolean)newValue);
				return;
			case DS91377Package.DINSPEC91377_POLICY_CHECK__MODEL_CENTRIC_DATA_FLOW_APPLIED:
				setModelCentricDataFlowApplied((Boolean)newValue);
				return;
			case DS91377Package.DINSPEC91377_POLICY_CHECK__INTERFACE_INTEROPERABILITY_ENSURED:
				setInterfaceInteroperabilityEnsured((Boolean)newValue);
				return;
			case DS91377Package.DINSPEC91377_POLICY_CHECK__INTEGRATION_API_STANDARDS_MET:
				setIntegrationApiStandardsMet((Boolean)newValue);
				return;
			case DS91377Package.DINSPEC91377_POLICY_CHECK__MANAGEMENT_SERVICES_PROVIDED:
				setManagementServicesProvided((Boolean)newValue);
				return;
			case DS91377Package.DINSPEC91377_POLICY_CHECK__INFORMATION_SECURITY_CONCEPT_APPLIED:
				setInformationSecurityConceptApplied((Boolean)newValue);
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
			case DS91377Package.DINSPEC91377_POLICY_CHECK__ARCHITECTURE_LAYERS_IMPLEMENTED:
				setArchitectureLayersImplemented(ARCHITECTURE_LAYERS_IMPLEMENTED_EDEFAULT);
				return;
			case DS91377Package.DINSPEC91377_POLICY_CHECK__MODEL_CENTRIC_DATA_FLOW_APPLIED:
				setModelCentricDataFlowApplied(MODEL_CENTRIC_DATA_FLOW_APPLIED_EDEFAULT);
				return;
			case DS91377Package.DINSPEC91377_POLICY_CHECK__INTERFACE_INTEROPERABILITY_ENSURED:
				setInterfaceInteroperabilityEnsured(INTERFACE_INTEROPERABILITY_ENSURED_EDEFAULT);
				return;
			case DS91377Package.DINSPEC91377_POLICY_CHECK__INTEGRATION_API_STANDARDS_MET:
				setIntegrationApiStandardsMet(INTEGRATION_API_STANDARDS_MET_EDEFAULT);
				return;
			case DS91377Package.DINSPEC91377_POLICY_CHECK__MANAGEMENT_SERVICES_PROVIDED:
				setManagementServicesProvided(MANAGEMENT_SERVICES_PROVIDED_EDEFAULT);
				return;
			case DS91377Package.DINSPEC91377_POLICY_CHECK__INFORMATION_SECURITY_CONCEPT_APPLIED:
				setInformationSecurityConceptApplied(INFORMATION_SECURITY_CONCEPT_APPLIED_EDEFAULT);
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
			case DS91377Package.DINSPEC91377_POLICY_CHECK__ARCHITECTURE_LAYERS_IMPLEMENTED:
				return architectureLayersImplemented != ARCHITECTURE_LAYERS_IMPLEMENTED_EDEFAULT;
			case DS91377Package.DINSPEC91377_POLICY_CHECK__MODEL_CENTRIC_DATA_FLOW_APPLIED:
				return modelCentricDataFlowApplied != MODEL_CENTRIC_DATA_FLOW_APPLIED_EDEFAULT;
			case DS91377Package.DINSPEC91377_POLICY_CHECK__INTERFACE_INTEROPERABILITY_ENSURED:
				return interfaceInteroperabilityEnsured != INTERFACE_INTEROPERABILITY_ENSURED_EDEFAULT;
			case DS91377Package.DINSPEC91377_POLICY_CHECK__INTEGRATION_API_STANDARDS_MET:
				return integrationApiStandardsMet != INTEGRATION_API_STANDARDS_MET_EDEFAULT;
			case DS91377Package.DINSPEC91377_POLICY_CHECK__MANAGEMENT_SERVICES_PROVIDED:
				return managementServicesProvided != MANAGEMENT_SERVICES_PROVIDED_EDEFAULT;
			case DS91377Package.DINSPEC91377_POLICY_CHECK__INFORMATION_SECURITY_CONCEPT_APPLIED:
				return informationSecurityConceptApplied != INFORMATION_SECURITY_CONCEPT_APPLIED_EDEFAULT;
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
		result.append(" (architectureLayersImplemented: ");
		result.append(architectureLayersImplemented);
		result.append(", modelCentricDataFlowApplied: ");
		result.append(modelCentricDataFlowApplied);
		result.append(", interfaceInteroperabilityEnsured: ");
		result.append(interfaceInteroperabilityEnsured);
		result.append(", integrationApiStandardsMet: ");
		result.append(integrationApiStandardsMet);
		result.append(", managementServicesProvided: ");
		result.append(managementServicesProvided);
		result.append(", informationSecurityConceptApplied: ");
		result.append(informationSecurityConceptApplied);
		result.append(')');
		return result.toString();
	}

} //DINSPEC91377PolicyCheckImpl
