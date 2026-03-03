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
package org.eclipse.fennec.model.atlas.rest.model;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Stage Transition Request</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.StageTransitionRequest#getObjectId <em>Object Id</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.StageTransitionRequest#getTargetStage <em>Target Stage</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.rest.model.RestPackage#getStageTransitionRequest()
 * @model
 * @generated
 */
@ProviderType
public interface StageTransitionRequest extends EObject {
	/**
	 * Returns the value of the '<em><b>Object Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Object Id</em>' attribute.
	 * @see #setObjectId(String)
	 * @see org.eclipse.fennec.model.atlas.rest.model.RestPackage#getStageTransitionRequest_ObjectId()
	 * @model
	 * @generated
	 */
	String getObjectId();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.rest.model.StageTransitionRequest#getObjectId <em>Object Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Object Id</em>' attribute.
	 * @see #getObjectId()
	 * @generated
	 */
	void setObjectId(String value);

	/**
	 * Returns the value of the '<em><b>Target Stage</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Target Stage</em>' attribute.
	 * @see #setTargetStage(String)
	 * @see org.eclipse.fennec.model.atlas.rest.model.RestPackage#getStageTransitionRequest_TargetStage()
	 * @model
	 * @generated
	 */
	String getTargetStage();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.rest.model.StageTransitionRequest#getTargetStage <em>Target Stage</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Target Stage</em>' attribute.
	 * @see #getTargetStage()
	 * @generated
	 */
	void setTargetStage(String value);

} // StageTransitionRequest
