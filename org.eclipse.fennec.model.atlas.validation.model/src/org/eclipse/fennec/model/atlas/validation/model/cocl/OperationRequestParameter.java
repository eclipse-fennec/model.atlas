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

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EParameter;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Operation Request Parameter</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Operation request parameter object
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OperationRequestParameter#getParameterName <em>Parameter Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OperationRequestParameter#getParameter <em>Parameter</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OperationRequestParameter#isIsNull <em>Is Null</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OperationRequestParameter#getJavaValue <em>Java Value</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OperationRequestParameter#getEValue <em>EValue</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getOperationRequestParameter()
 * @model
 * @generated
 */
@ProviderType
public interface OperationRequestParameter extends EObject {
	/**
	 * Returns the value of the '<em><b>Parameter Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * When requesting an OperationValidation using a C-OCL id, the client has to provide the operation parameter names, which then will be used to filter the right OPERATION Constraint from the C-OCL ConstraintSet.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Parameter Name</em>' attribute.
	 * @see #setParameterName(String)
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getOperationRequestParameter_ParameterName()
	 * @model
	 * @generated
	 */
	String getParameterName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OperationRequestParameter#getParameterName <em>Parameter Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parameter Name</em>' attribute.
	 * @see #getParameterName()
	 * @generated
	 */
	void setParameterName(String value);

	/**
	 * Returns the value of the '<em><b>Parameter</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * EParameter . This has to be provided by the client when no C-OCL id is provided, but an EOperation defined in the model has to be invoked.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Parameter</em>' reference.
	 * @see #setParameter(EParameter)
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getOperationRequestParameter_Parameter()
	 * @model
	 * @generated
	 */
	EParameter getParameter();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OperationRequestParameter#getParameter <em>Parameter</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Parameter</em>' reference.
	 * @see #getParameter()
	 * @generated
	 */
	void setParameter(EParameter value);

	/**
	 * Returns the value of the '<em><b>Is Null</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Is Null</em>' attribute.
	 * @see #setIsNull(boolean)
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getOperationRequestParameter_IsNull()
	 * @model
	 * @generated
	 */
	boolean isIsNull();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OperationRequestParameter#isIsNull <em>Is Null</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Is Null</em>' attribute.
	 * @see #isIsNull()
	 * @generated
	 */
	void setIsNull(boolean value);

	/**
	 * Returns the value of the '<em><b>Java Value</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * We put the String representation here to not have issues via REST. Then we can construct the value back from the parameter type.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Java Value</em>' attribute.
	 * @see #setJavaValue(String)
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getOperationRequestParameter_JavaValue()
	 * @model
	 * @generated
	 */
	String getJavaValue();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OperationRequestParameter#getJavaValue <em>Java Value</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Java Value</em>' attribute.
	 * @see #getJavaValue()
	 * @generated
	 */
	void setJavaValue(String value);

	/**
	 * Returns the value of the '<em><b>EValue</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>EValue</em>' containment reference.
	 * @see #setEValue(EObject)
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getOperationRequestParameter_EValue()
	 * @model containment="true"
	 * @generated
	 */
	EObject getEValue();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OperationRequestParameter#getEValue <em>EValue</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>EValue</em>' containment reference.
	 * @see #getEValue()
	 * @generated
	 */
	void setEValue(EObject value);

} // OperationRequestParameter
