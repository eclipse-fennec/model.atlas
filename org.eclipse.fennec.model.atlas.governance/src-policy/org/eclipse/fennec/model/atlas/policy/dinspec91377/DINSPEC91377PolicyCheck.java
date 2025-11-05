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
package org.eclipse.fennec.model.atlas.policy.dinspec91377;

import org.eclipse.fennec.model.atlas.governance.ComplianceCheckResult;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>DINSPEC91377 Policy Check</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * A specialized compliance check that represents the verification against key requirements of the DIN SPEC 91377 for Open Urban Platforms (OUP).
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.dinspec91377.DINSPEC91377PolicyCheck#isArchitectureLayersImplemented <em>Architecture Layers Implemented</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.dinspec91377.DINSPEC91377PolicyCheck#isModelCentricDataFlowApplied <em>Model Centric Data Flow Applied</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.dinspec91377.DINSPEC91377PolicyCheck#isInterfaceInteroperabilityEnsured <em>Interface Interoperability Ensured</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.dinspec91377.DINSPEC91377PolicyCheck#isIntegrationApiStandardsMet <em>Integration Api Standards Met</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.dinspec91377.DINSPEC91377PolicyCheck#isManagementServicesProvided <em>Management Services Provided</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.policy.dinspec91377.DINSPEC91377PolicyCheck#isInformationSecurityConceptApplied <em>Information Security Concept Applied</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.policy.dinspec91377.DS91377Package#getDINSPEC91377PolicyCheck()
 * @model
 * @generated
 */
@ProviderType
public interface DINSPEC91377PolicyCheck extends ComplianceCheckResult {
	/**
	 * Returns the value of the '<em><b>Architecture Layers Implemented</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result (Chapter 4.5): Is the OUP core structured according to the specified horizontal (Interfaces, Processing, Integration) and vertical (Management) layers?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Architecture Layers Implemented</em>' attribute.
	 * @see #setArchitectureLayersImplemented(boolean)
	 * @see org.eclipse.fennec.model.atlas.policy.dinspec91377.DS91377Package#getDINSPEC91377PolicyCheck_ArchitectureLayersImplemented()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isArchitectureLayersImplemented();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.policy.dinspec91377.DINSPEC91377PolicyCheck#isArchitectureLayersImplemented <em>Architecture Layers Implemented</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Architecture Layers Implemented</em>' attribute.
	 * @see #isArchitectureLayersImplemented()
	 * @generated
	 */
	void setArchitectureLayersImplemented(boolean value);

	/**
	 * Returns the value of the '<em><b>Model Centric Data Flow Applied</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result (Chapter 4.6): Does the platform follow the model-centric data flow concept, using a Model and Data Flow Management (MDM) component to configure its software modules?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Model Centric Data Flow Applied</em>' attribute.
	 * @see #setModelCentricDataFlowApplied(boolean)
	 * @see org.eclipse.fennec.model.atlas.policy.dinspec91377.DS91377Package#getDINSPEC91377PolicyCheck_ModelCentricDataFlowApplied()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isModelCentricDataFlowApplied();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.policy.dinspec91377.DINSPEC91377PolicyCheck#isModelCentricDataFlowApplied <em>Model Centric Data Flow Applied</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Model Centric Data Flow Applied</em>' attribute.
	 * @see #isModelCentricDataFlowApplied()
	 * @generated
	 */
	void setModelCentricDataFlowApplied(boolean value);

	/**
	 * Returns the value of the '<em><b>Interface Interoperability Ensured</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result (Chapter 5.3): Does the Interfaces Layer utilize standardized connectors and protocols (e.g., OGC API, MQTT) for inbound data?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Interface Interoperability Ensured</em>' attribute.
	 * @see #setInterfaceInteroperabilityEnsured(boolean)
	 * @see org.eclipse.fennec.model.atlas.policy.dinspec91377.DS91377Package#getDINSPEC91377PolicyCheck_InterfaceInteroperabilityEnsured()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isInterfaceInteroperabilityEnsured();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.policy.dinspec91377.DINSPEC91377PolicyCheck#isInterfaceInteroperabilityEnsured <em>Interface Interoperability Ensured</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Interface Interoperability Ensured</em>' attribute.
	 * @see #isInterfaceInteroperabilityEnsured()
	 * @generated
	 */
	void setInterfaceInteroperabilityEnsured(boolean value);

	/**
	 * Returns the value of the '<em><b>Integration Api Standards Met</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result (Chapter 5.5): Does the Integration Layer provide open, standardized APIs (e.g., OGC API Features, SensorThings API) for outbound data?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Integration Api Standards Met</em>' attribute.
	 * @see #setIntegrationApiStandardsMet(boolean)
	 * @see org.eclipse.fennec.model.atlas.policy.dinspec91377.DS91377Package#getDINSPEC91377PolicyCheck_IntegrationApiStandardsMet()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isIntegrationApiStandardsMet();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.policy.dinspec91377.DINSPEC91377PolicyCheck#isIntegrationApiStandardsMet <em>Integration Api Standards Met</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Integration Api Standards Met</em>' attribute.
	 * @see #isIntegrationApiStandardsMet()
	 * @generated
	 */
	void setIntegrationApiStandardsMet(boolean value);

	/**
	 * Returns the value of the '<em><b>Management Services Provided</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result (Chapter 5.6): Does the Management Layer provide the required core services, including IAM (OIDC/SAML), a DCAT-AP compliant Data Catalog, and API Management?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Management Services Provided</em>' attribute.
	 * @see #setManagementServicesProvided(boolean)
	 * @see org.eclipse.fennec.model.atlas.policy.dinspec91377.DS91377Package#getDINSPEC91377PolicyCheck_ManagementServicesProvided()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isManagementServicesProvided();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.policy.dinspec91377.DINSPEC91377PolicyCheck#isManagementServicesProvided <em>Management Services Provided</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Management Services Provided</em>' attribute.
	 * @see #isManagementServicesProvided()
	 * @generated
	 */
	void setManagementServicesProvided(boolean value);

	/**
	 * Returns the value of the '<em><b>Information Security Concept Applied</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Check result (Chapter 6): Is a holistic information security concept applied, covering data security (confidentiality, integrity, availability), IAM, and secure communication (TLS)?
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Information Security Concept Applied</em>' attribute.
	 * @see #setInformationSecurityConceptApplied(boolean)
	 * @see org.eclipse.fennec.model.atlas.policy.dinspec91377.DS91377Package#getDINSPEC91377PolicyCheck_InformationSecurityConceptApplied()
	 * @model default="false" required="true"
	 * @generated
	 */
	boolean isInformationSecurityConceptApplied();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.policy.dinspec91377.DINSPEC91377PolicyCheck#isInformationSecurityConceptApplied <em>Information Security Concept Applied</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Information Security Concept Applied</em>' attribute.
	 * @see #isInformationSecurityConceptApplied()
	 * @generated
	 */
	void setInformationSecurityConceptApplied(boolean value);

} // DINSPEC91377PolicyCheck
