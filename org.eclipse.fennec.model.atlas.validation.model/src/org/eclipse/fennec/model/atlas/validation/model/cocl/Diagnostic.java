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
package org.eclipse.fennec.model.atlas.validation.model.cocl;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Diagnostic</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic#getMessage <em>Message</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic#getSource <em>Source</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic#getType <em>Type</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic#getChildren <em>Children</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic#getExceptionMsg <em>Exception Msg</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic#getData <em>Data</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getDiagnostic()
 * @model
 * @generated
 */
@ProviderType
public interface Diagnostic extends EObject {
	/**
	 * Returns the value of the '<em><b>Message</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Message</em>' attribute.
	 * @see #setMessage(String)
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getDiagnostic_Message()
	 * @model
	 * @generated
	 */
	String getMessage();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic#getMessage <em>Message</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Message</em>' attribute.
	 * @see #getMessage()
	 * @generated
	 */
	void setMessage(String value);

	/**
	 * Returns the value of the '<em><b>Source</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Source</em>' attribute.
	 * @see #setSource(String)
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getDiagnostic_Source()
	 * @model
	 * @generated
	 */
	String getSource();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic#getSource <em>Source</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Source</em>' attribute.
	 * @see #getSource()
	 * @generated
	 */
	void setSource(String value);

	/**
	 * Returns the value of the '<em><b>Type</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.validation.model.cocl.Severity}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Type</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.Severity
	 * @see #setType(Severity)
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getDiagnostic_Type()
	 * @model
	 * @generated
	 */
	Severity getType();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic#getType <em>Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Type</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.Severity
	 * @see #getType()
	 * @generated
	 */
	void setType(Severity value);

	/**
	 * Returns the value of the '<em><b>Children</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Children</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getDiagnostic_Children()
	 * @model containment="true"
	 * @generated
	 */
	EList<Diagnostic> getChildren();

	/**
	 * Returns the value of the '<em><b>Exception Msg</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Exception Msg</em>' attribute.
	 * @see #setExceptionMsg(String)
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getDiagnostic_ExceptionMsg()
	 * @model
	 * @generated
	 */
	String getExceptionMsg();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.Diagnostic#getExceptionMsg <em>Exception Msg</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Exception Msg</em>' attribute.
	 * @see #getExceptionMsg()
	 * @generated
	 */
	void setExceptionMsg(String value);

	/**
	 * Returns the value of the '<em><b>Data</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Data</em>' attribute list.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getDiagnostic_Data()
	 * @model
	 * @generated
	 */
	EList<String> getData();

} // Diagnostic
