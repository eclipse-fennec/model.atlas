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
package org.eclipse.fennec.model.atlas.registry.api.impl;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;

import org.eclipse.fennec.model.atlas.registry.api.BatchItemResult;
import org.eclipse.fennec.model.atlas.registry.api.RefusalReason;
import org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage;
import org.eclipse.fennec.model.atlas.registry.api.WriteOutcome;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Batch Item Result</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.BatchItemResultImpl#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.BatchItemResultImpl#getOutcome <em>Outcome</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.BatchItemResultImpl#getMetadata <em>Metadata</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.BatchItemResultImpl#getRefusal <em>Refusal</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.BatchItemResultImpl#getMessage <em>Message</em>}</li>
 * </ul>
 *
 * @generated
 */
public class BatchItemResultImpl extends MinimalEObjectImpl.Container implements BatchItemResult {
	/**
	 * The default value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected static final String ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getId() <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getId()
	 * @generated
	 * @ordered
	 */
	protected String id = ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getOutcome() <em>Outcome</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOutcome()
	 * @generated
	 * @ordered
	 */
	protected static final WriteOutcome OUTCOME_EDEFAULT = WriteOutcome.CREATED;

	/**
	 * The cached value of the '{@link #getOutcome() <em>Outcome</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOutcome()
	 * @generated
	 * @ordered
	 */
	protected WriteOutcome outcome = OUTCOME_EDEFAULT;

	/**
	 * This is true if the Outcome attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean outcomeESet;

	/**
	 * The cached value of the '{@link #getMetadata() <em>Metadata</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMetadata()
	 * @generated
	 * @ordered
	 */
	protected ObjectMetadata metadata;

	/**
	 * The default value of the '{@link #getRefusal() <em>Refusal</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRefusal()
	 * @generated
	 * @ordered
	 */
	protected static final RefusalReason REFUSAL_EDEFAULT = RefusalReason.INVALID;

	/**
	 * The cached value of the '{@link #getRefusal() <em>Refusal</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRefusal()
	 * @generated
	 * @ordered
	 */
	protected RefusalReason refusal = REFUSAL_EDEFAULT;

	/**
	 * This is true if the Refusal attribute has been set.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	protected boolean refusalESet;

	/**
	 * The default value of the '{@link #getMessage() <em>Message</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMessage()
	 * @generated
	 * @ordered
	 */
	protected static final String MESSAGE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getMessage() <em>Message</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getMessage()
	 * @generated
	 * @ordered
	 */
	protected String message = MESSAGE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected BatchItemResultImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RegistryApiPackage.Literals.BATCH_ITEM_RESULT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getId() {
		return id;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setId(String newId) {
		id = newId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public WriteOutcome getOutcome() {
		return outcome;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setOutcome(WriteOutcome newOutcome) {
		outcome = newOutcome == null ? OUTCOME_EDEFAULT : newOutcome;
		outcomeESet = true;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetOutcome() {
		outcome = OUTCOME_EDEFAULT;
		outcomeESet = false;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetOutcome() {
		return outcomeESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ObjectMetadata getMetadata() {
		return metadata;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMetadata(ObjectMetadata newMetadata) {
		metadata = newMetadata;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public RefusalReason getRefusal() {
		return refusal;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setRefusal(RefusalReason newRefusal) {
		refusal = newRefusal == null ? REFUSAL_EDEFAULT : newRefusal;
		refusalESet = true;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void unsetRefusal() {
		refusal = REFUSAL_EDEFAULT;
		refusalESet = false;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isSetRefusal() {
		return refusalESet;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getMessage() {
		return message;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setMessage(String newMessage) {
		message = newMessage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isRefused() {
		return isSetRefusal();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case RegistryApiPackage.BATCH_ITEM_RESULT__ID:
				return getId();
			case RegistryApiPackage.BATCH_ITEM_RESULT__OUTCOME:
				return getOutcome();
			case RegistryApiPackage.BATCH_ITEM_RESULT__METADATA:
				return getMetadata();
			case RegistryApiPackage.BATCH_ITEM_RESULT__REFUSAL:
				return getRefusal();
			case RegistryApiPackage.BATCH_ITEM_RESULT__MESSAGE:
				return getMessage();
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
			case RegistryApiPackage.BATCH_ITEM_RESULT__ID:
				setId((String)newValue);
				return;
			case RegistryApiPackage.BATCH_ITEM_RESULT__OUTCOME:
				setOutcome((WriteOutcome)newValue);
				return;
			case RegistryApiPackage.BATCH_ITEM_RESULT__METADATA:
				setMetadata((ObjectMetadata)newValue);
				return;
			case RegistryApiPackage.BATCH_ITEM_RESULT__REFUSAL:
				setRefusal((RefusalReason)newValue);
				return;
			case RegistryApiPackage.BATCH_ITEM_RESULT__MESSAGE:
				setMessage((String)newValue);
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
			case RegistryApiPackage.BATCH_ITEM_RESULT__ID:
				setId(ID_EDEFAULT);
				return;
			case RegistryApiPackage.BATCH_ITEM_RESULT__OUTCOME:
				unsetOutcome();
				return;
			case RegistryApiPackage.BATCH_ITEM_RESULT__METADATA:
				setMetadata((ObjectMetadata)null);
				return;
			case RegistryApiPackage.BATCH_ITEM_RESULT__REFUSAL:
				unsetRefusal();
				return;
			case RegistryApiPackage.BATCH_ITEM_RESULT__MESSAGE:
				setMessage(MESSAGE_EDEFAULT);
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
			case RegistryApiPackage.BATCH_ITEM_RESULT__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
			case RegistryApiPackage.BATCH_ITEM_RESULT__OUTCOME:
				return isSetOutcome();
			case RegistryApiPackage.BATCH_ITEM_RESULT__METADATA:
				return metadata != null;
			case RegistryApiPackage.BATCH_ITEM_RESULT__REFUSAL:
				return isSetRefusal();
			case RegistryApiPackage.BATCH_ITEM_RESULT__MESSAGE:
				return MESSAGE_EDEFAULT == null ? message != null : !MESSAGE_EDEFAULT.equals(message);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException {
		switch (operationID) {
			case RegistryApiPackage.BATCH_ITEM_RESULT___IS_REFUSED:
				return isRefused();
		}
		return super.eInvoke(operationID, arguments);
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
		result.append(" (id: ");
		result.append(id);
		result.append(", outcome: ");
		if (outcomeESet) result.append(outcome); else result.append("<unset>");
		result.append(", refusal: ");
		if (refusalESet) result.append(refusal); else result.append("<unset>");
		result.append(", message: ");
		result.append(message);
		result.append(')');
		return result.toString();
	}

} //BatchItemResultImpl
