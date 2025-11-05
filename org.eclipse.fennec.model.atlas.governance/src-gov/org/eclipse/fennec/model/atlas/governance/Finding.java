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
package org.eclipse.fennec.model.atlas.governance;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Finding</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Represents a single, structured finding as the result of a compliance check, providing details for analysis and remediation.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.Finding#getCode <em>Code</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.Finding#getSeverity <em>Severity</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.Finding#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.Finding#getComponent <em>Component</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.governance.Finding#getRemediation <em>Remediation</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getFinding()
 * @model
 * @generated
 */
@ProviderType
public interface Finding extends EObject {
	/**
	 * Returns the value of the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A machine-readable code or identifier for the finding type (e.g., 'PII_DETECTED').
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Code</em>' attribute.
	 * @see #setCode(String)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getFinding_Code()
	 * @model
	 * @generated
	 */
	String getCode();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.Finding#getCode <em>Code</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Code</em>' attribute.
	 * @see #getCode()
	 * @generated
	 */
	void setCode(String value);

	/**
	 * Returns the value of the '<em><b>Severity</b></em>' attribute.
	 * The default value is <code>"INFO"</code>.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.governance.FindingSeverity}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The severity of the finding, used for prioritization.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Severity</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.governance.FindingSeverity
	 * @see #setSeverity(FindingSeverity)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getFinding_Severity()
	 * @model default="INFO" required="true"
	 * @generated
	 */
	FindingSeverity getSeverity();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.Finding#getSeverity <em>Severity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Severity</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.governance.FindingSeverity
	 * @see #getSeverity()
	 * @generated
	 */
	void setSeverity(FindingSeverity value);

	/**
	 * Returns the value of the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A human-readable description of the finding, explaining what was detected.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getFinding_Description()
	 * @model required="true"
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.Finding#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Component</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The specific component, attribute, or element within the asset where the finding was located.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Component</em>' attribute.
	 * @see #setComponent(String)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getFinding_Component()
	 * @model
	 * @generated
	 */
	String getComponent();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.Finding#getComponent <em>Component</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Component</em>' attribute.
	 * @see #getComponent()
	 * @generated
	 */
	void setComponent(String value);

	/**
	 * Returns the value of the '<em><b>Remediation</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A suggested action or recommendation to address and resolve the finding.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Remediation</em>' attribute.
	 * @see #setRemediation(String)
	 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getFinding_Remediation()
	 * @model
	 * @generated
	 */
	String getRemediation();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.governance.Finding#getRemediation <em>Remediation</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Remediation</em>' attribute.
	 * @see #getRemediation()
	 * @generated
	 */
	void setRemediation(String value);

} // Finding
