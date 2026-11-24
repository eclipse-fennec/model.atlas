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
package org.eclipse.fennec.model.atlas.rest.model;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Batch Item</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * What a batch did with one item: written (outcome and metadata set), refused (refusal and message set), or not applied because another item was refused (neither set).
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.BatchItem#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.BatchItem#getOutcome <em>Outcome</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.BatchItem#getRefusal <em>Refusal</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.BatchItem#getMessage <em>Message</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.rest.model.BatchItem#getMetadata <em>Metadata</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.rest.model.RestPackage#getBatchItem()
 * @model
 * @generated
 */
@ProviderType
public interface BatchItem extends EObject {
	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(String)
	 * @see org.eclipse.fennec.model.atlas.rest.model.RestPackage#getBatchItem_Id()
	 * @model
	 * @generated
	 */
	String getId();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.rest.model.BatchItem#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(String value);

	/**
	 * Returns the value of the '<em><b>Outcome</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Outcome</em>' attribute.
	 * @see #setOutcome(String)
	 * @see org.eclipse.fennec.model.atlas.rest.model.RestPackage#getBatchItem_Outcome()
	 * @model
	 * @generated
	 */
	String getOutcome();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.rest.model.BatchItem#getOutcome <em>Outcome</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Outcome</em>' attribute.
	 * @see #getOutcome()
	 * @generated
	 */
	void setOutcome(String value);

	/**
	 * Returns the value of the '<em><b>Refusal</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Refusal</em>' attribute.
	 * @see #setRefusal(String)
	 * @see org.eclipse.fennec.model.atlas.rest.model.RestPackage#getBatchItem_Refusal()
	 * @model
	 * @generated
	 */
	String getRefusal();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.rest.model.BatchItem#getRefusal <em>Refusal</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Refusal</em>' attribute.
	 * @see #getRefusal()
	 * @generated
	 */
	void setRefusal(String value);

	/**
	 * Returns the value of the '<em><b>Message</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Message</em>' attribute.
	 * @see #setMessage(String)
	 * @see org.eclipse.fennec.model.atlas.rest.model.RestPackage#getBatchItem_Message()
	 * @model
	 * @generated
	 */
	String getMessage();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.rest.model.BatchItem#getMessage <em>Message</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Message</em>' attribute.
	 * @see #getMessage()
	 * @generated
	 */
	void setMessage(String value);

	/**
	 * Returns the value of the '<em><b>Metadata</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Metadata</em>' containment reference.
	 * @see #setMetadata(ObjectMetadata)
	 * @see org.eclipse.fennec.model.atlas.rest.model.RestPackage#getBatchItem_Metadata()
	 * @model containment="true"
	 * @generated
	 */
	ObjectMetadata getMetadata();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.rest.model.BatchItem#getMetadata <em>Metadata</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Metadata</em>' containment reference.
	 * @see #getMetadata()
	 * @generated
	 */
	void setMetadata(ObjectMetadata value);

} // BatchItem
