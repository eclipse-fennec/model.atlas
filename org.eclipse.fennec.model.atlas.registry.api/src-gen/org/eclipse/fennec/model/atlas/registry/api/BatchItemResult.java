/*
 * Copyright (c) 2012 - 2026 Data In Motion and others.
 * All rights reserved.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.registry.api;

import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Batch Item Result</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * What a batch did with one of its items. Exactly one of three cases holds: the item was written (outcome set), it was refused (refusal and message set), or it was not applied because another item of the batch was refused (neither outcome nor refusal set; whatever it had written was undone).
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.BatchItemResult#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.BatchItemResult#getOutcome <em>Outcome</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.BatchItemResult#getMetadata <em>Metadata</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.BatchItemResult#getRefusal <em>Refusal</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.BatchItemResult#getMessage <em>Message</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getBatchItemResult()
 * @model
 * @generated
 */
@ProviderType
public interface BatchItemResult {
	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * the item's identifier: the objectId of an instance, the nsURI of a package; may be null for an instance that has none
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(String)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getBatchItemResult_Id()
	 * @model
	 * @generated
	 */
	String getId();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.BatchItemResult#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(String value);

	/**
	 * Returns the value of the '<em><b>Outcome</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.registry.api.WriteOutcome}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * what was written; unset if the item was not written
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Outcome</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.registry.api.WriteOutcome
	 * @see #isSetOutcome()
	 * @see #unsetOutcome()
	 * @see #setOutcome(WriteOutcome)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getBatchItemResult_Outcome()
	 * @model unsettable="true"
	 * @generated
	 */
	WriteOutcome getOutcome();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.BatchItemResult#getOutcome <em>Outcome</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Outcome</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.registry.api.WriteOutcome
	 * @see #isSetOutcome()
	 * @see #unsetOutcome()
	 * @see #getOutcome()
	 * @generated
	 */
	void setOutcome(WriteOutcome value);

	/**
	 * Unsets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.BatchItemResult#getOutcome <em>Outcome</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetOutcome()
	 * @see #getOutcome()
	 * @see #setOutcome(WriteOutcome)
	 * @generated
	 */
	void unsetOutcome();

	/**
	 * Returns whether the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.BatchItemResult#getOutcome <em>Outcome</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Outcome</em>' attribute is set.
	 * @see #unsetOutcome()
	 * @see #getOutcome()
	 * @see #setOutcome(WriteOutcome)
	 * @generated
	 */
	boolean isSetOutcome();

	/**
	 * Returns the value of the '<em><b>Metadata</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * the stored metadata of a written item, or null
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Metadata</em>' containment reference.
	 * @see #setMetadata(ObjectMetadata)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getBatchItemResult_Metadata()
	 * @model containment="true"
	 * @generated
	 */
	ObjectMetadata getMetadata();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.BatchItemResult#getMetadata <em>Metadata</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Metadata</em>' containment reference.
	 * @see #getMetadata()
	 * @generated
	 */
	void setMetadata(ObjectMetadata value);

	/**
	 * Returns the value of the '<em><b>Refusal</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.registry.api.RefusalReason}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * why the item was refused; unset if it was not refused
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Refusal</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RefusalReason
	 * @see #isSetRefusal()
	 * @see #unsetRefusal()
	 * @see #setRefusal(RefusalReason)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getBatchItemResult_Refusal()
	 * @model unsettable="true"
	 * @generated
	 */
	RefusalReason getRefusal();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.BatchItemResult#getRefusal <em>Refusal</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Refusal</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.registry.api.RefusalReason
	 * @see #isSetRefusal()
	 * @see #unsetRefusal()
	 * @see #getRefusal()
	 * @generated
	 */
	void setRefusal(RefusalReason value);

	/**
	 * Unsets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.BatchItemResult#getRefusal <em>Refusal</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isSetRefusal()
	 * @see #getRefusal()
	 * @see #setRefusal(RefusalReason)
	 * @generated
	 */
	void unsetRefusal();

	/**
	 * Returns whether the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.BatchItemResult#getRefusal <em>Refusal</em>}' attribute is set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return whether the value of the '<em>Refusal</em>' attribute is set.
	 * @see #unsetRefusal()
	 * @see #getRefusal()
	 * @see #setRefusal(RefusalReason)
	 * @generated
	 */
	boolean isSetRefusal();

	/**
	 * Returns the value of the '<em><b>Message</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * the refusal's message, or null
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Message</em>' attribute.
	 * @see #setMessage(String)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getBatchItemResult_Message()
	 * @model
	 * @generated
	 */
	String getMessage();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.BatchItemResult#getMessage <em>Message</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Message</em>' attribute.
	 * @see #getMessage()
	 * @generated
	 */
	void setMessage(String value);

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * @return whether this item was refused
	 * <!-- end-model-doc -->
	 * @model kind="operation"
	 * @generated
	 */
	boolean isRefused();

} // BatchItemResult
