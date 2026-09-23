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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.mgmt.management.impl;

import java.time.Instant;

import java.util.Collection;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.emf.ecore.util.EObjectContainmentEList;
import org.eclipse.emf.ecore.util.InternalEList;

import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticChange;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticStatus;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Diagnostic</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.DiagnosticImpl#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.DiagnosticImpl#getProducer <em>Producer</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.DiagnosticImpl#getSource <em>Source</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.DiagnosticImpl#getCode <em>Code</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.DiagnosticImpl#getSeverity <em>Severity</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.DiagnosticImpl#getMessage <em>Message</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.DiagnosticImpl#getCategory <em>Category</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.DiagnosticImpl#getTarget <em>Target</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.DiagnosticImpl#getStatus <em>Status</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.DiagnosticImpl#getCreatedTime <em>Created Time</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.DiagnosticImpl#getLastChangeTime <em>Last Change Time</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.DiagnosticImpl#getVersion <em>Version</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.DiagnosticImpl#getChildren <em>Children</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.impl.DiagnosticImpl#getHistory <em>History</em>}</li>
 * </ul>
 *
 * @generated
 */
public class DiagnosticImpl extends MinimalEObjectImpl.Container implements Diagnostic {
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
	 * The default value of the '{@link #getProducer() <em>Producer</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProducer()
	 * @generated
	 * @ordered
	 */
	protected static final String PRODUCER_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getProducer() <em>Producer</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getProducer()
	 * @generated
	 * @ordered
	 */
	protected String producer = PRODUCER_EDEFAULT;

	/**
	 * The default value of the '{@link #getSource() <em>Source</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSource()
	 * @generated
	 * @ordered
	 */
	protected static final String SOURCE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getSource() <em>Source</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSource()
	 * @generated
	 * @ordered
	 */
	protected String source = SOURCE_EDEFAULT;

	/**
	 * The default value of the '{@link #getCode() <em>Code</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCode()
	 * @generated
	 * @ordered
	 */
	protected static final String CODE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCode() <em>Code</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCode()
	 * @generated
	 * @ordered
	 */
	protected String code = CODE_EDEFAULT;

	/**
	 * The default value of the '{@link #getSeverity() <em>Severity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSeverity()
	 * @generated
	 * @ordered
	 */
	protected static final DiagnosticSeverity SEVERITY_EDEFAULT = DiagnosticSeverity.INFO;

	/**
	 * The cached value of the '{@link #getSeverity() <em>Severity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getSeverity()
	 * @generated
	 * @ordered
	 */
	protected DiagnosticSeverity severity = SEVERITY_EDEFAULT;

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
	 * The default value of the '{@link #getCategory() <em>Category</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCategory()
	 * @generated
	 * @ordered
	 */
	protected static final String CATEGORY_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCategory() <em>Category</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCategory()
	 * @generated
	 * @ordered
	 */
	protected String category = CATEGORY_EDEFAULT;

	/**
	 * The default value of the '{@link #getTarget() <em>Target</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTarget()
	 * @generated
	 * @ordered
	 */
	protected static final String TARGET_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getTarget() <em>Target</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getTarget()
	 * @generated
	 * @ordered
	 */
	protected String target = TARGET_EDEFAULT;

	/**
	 * The default value of the '{@link #getStatus() <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStatus()
	 * @generated
	 * @ordered
	 */
	protected static final DiagnosticStatus STATUS_EDEFAULT = DiagnosticStatus.OPEN;

	/**
	 * The cached value of the '{@link #getStatus() <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStatus()
	 * @generated
	 * @ordered
	 */
	protected DiagnosticStatus status = STATUS_EDEFAULT;

	/**
	 * The default value of the '{@link #getCreatedTime() <em>Created Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCreatedTime()
	 * @generated
	 * @ordered
	 */
	protected static final Instant CREATED_TIME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCreatedTime() <em>Created Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCreatedTime()
	 * @generated
	 * @ordered
	 */
	protected Instant createdTime = CREATED_TIME_EDEFAULT;

	/**
	 * The default value of the '{@link #getLastChangeTime() <em>Last Change Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLastChangeTime()
	 * @generated
	 * @ordered
	 */
	protected static final Instant LAST_CHANGE_TIME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLastChangeTime() <em>Last Change Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLastChangeTime()
	 * @generated
	 * @ordered
	 */
	protected Instant lastChangeTime = LAST_CHANGE_TIME_EDEFAULT;

	/**
	 * The default value of the '{@link #getVersion() <em>Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVersion()
	 * @generated
	 * @ordered
	 */
	protected static final long VERSION_EDEFAULT = 0L;

	/**
	 * The cached value of the '{@link #getVersion() <em>Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVersion()
	 * @generated
	 * @ordered
	 */
	protected long version = VERSION_EDEFAULT;

	/**
	 * The cached value of the '{@link #getChildren() <em>Children</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getChildren()
	 * @generated
	 * @ordered
	 */
	protected EList<Diagnostic> children;

	/**
	 * The cached value of the '{@link #getHistory() <em>History</em>}' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getHistory()
	 * @generated
	 * @ordered
	 */
	protected EList<DiagnosticChange> history;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected DiagnosticImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return ManagementPackage.Literals.DIAGNOSTIC;
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
		String oldId = id;
		id = newId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.DIAGNOSTIC__ID, oldId, id));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getProducer() {
		return producer;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setProducer(String newProducer) {
		String oldProducer = producer;
		producer = newProducer;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.DIAGNOSTIC__PRODUCER, oldProducer, producer));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getSource() {
		return source;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSource(String newSource) {
		String oldSource = source;
		source = newSource;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.DIAGNOSTIC__SOURCE, oldSource, source));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getCode() {
		return code;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCode(String newCode) {
		String oldCode = code;
		code = newCode;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.DIAGNOSTIC__CODE, oldCode, code));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DiagnosticSeverity getSeverity() {
		return severity;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setSeverity(DiagnosticSeverity newSeverity) {
		DiagnosticSeverity oldSeverity = severity;
		severity = newSeverity == null ? SEVERITY_EDEFAULT : newSeverity;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.DIAGNOSTIC__SEVERITY, oldSeverity, severity));
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
		String oldMessage = message;
		message = newMessage;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.DIAGNOSTIC__MESSAGE, oldMessage, message));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getCategory() {
		return category;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCategory(String newCategory) {
		String oldCategory = category;
		category = newCategory;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.DIAGNOSTIC__CATEGORY, oldCategory, category));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getTarget() {
		return target;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setTarget(String newTarget) {
		String oldTarget = target;
		target = newTarget;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.DIAGNOSTIC__TARGET, oldTarget, target));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DiagnosticStatus getStatus() {
		return status;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStatus(DiagnosticStatus newStatus) {
		DiagnosticStatus oldStatus = status;
		status = newStatus == null ? STATUS_EDEFAULT : newStatus;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.DIAGNOSTIC__STATUS, oldStatus, status));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Instant getCreatedTime() {
		return createdTime;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCreatedTime(Instant newCreatedTime) {
		Instant oldCreatedTime = createdTime;
		createdTime = newCreatedTime;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.DIAGNOSTIC__CREATED_TIME, oldCreatedTime, createdTime));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Instant getLastChangeTime() {
		return lastChangeTime;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLastChangeTime(Instant newLastChangeTime) {
		Instant oldLastChangeTime = lastChangeTime;
		lastChangeTime = newLastChangeTime;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.DIAGNOSTIC__LAST_CHANGE_TIME, oldLastChangeTime, lastChangeTime));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public long getVersion() {
		return version;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setVersion(long newVersion) {
		long oldVersion = version;
		version = newVersion;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, ManagementPackage.DIAGNOSTIC__VERSION, oldVersion, version));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<Diagnostic> getChildren() {
		if (children == null) {
			children = new EObjectContainmentEList<Diagnostic>(Diagnostic.class, this, ManagementPackage.DIAGNOSTIC__CHILDREN);
		}
		return children;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EList<DiagnosticChange> getHistory() {
		if (history == null) {
			history = new EObjectContainmentEList<DiagnosticChange>(DiagnosticChange.class, this, ManagementPackage.DIAGNOSTIC__HISTORY);
		}
		return history;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case ManagementPackage.DIAGNOSTIC__CHILDREN:
				return ((InternalEList<?>)getChildren()).basicRemove(otherEnd, msgs);
			case ManagementPackage.DIAGNOSTIC__HISTORY:
				return ((InternalEList<?>)getHistory()).basicRemove(otherEnd, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case ManagementPackage.DIAGNOSTIC__ID:
				return getId();
			case ManagementPackage.DIAGNOSTIC__PRODUCER:
				return getProducer();
			case ManagementPackage.DIAGNOSTIC__SOURCE:
				return getSource();
			case ManagementPackage.DIAGNOSTIC__CODE:
				return getCode();
			case ManagementPackage.DIAGNOSTIC__SEVERITY:
				return getSeverity();
			case ManagementPackage.DIAGNOSTIC__MESSAGE:
				return getMessage();
			case ManagementPackage.DIAGNOSTIC__CATEGORY:
				return getCategory();
			case ManagementPackage.DIAGNOSTIC__TARGET:
				return getTarget();
			case ManagementPackage.DIAGNOSTIC__STATUS:
				return getStatus();
			case ManagementPackage.DIAGNOSTIC__CREATED_TIME:
				return getCreatedTime();
			case ManagementPackage.DIAGNOSTIC__LAST_CHANGE_TIME:
				return getLastChangeTime();
			case ManagementPackage.DIAGNOSTIC__VERSION:
				return getVersion();
			case ManagementPackage.DIAGNOSTIC__CHILDREN:
				return getChildren();
			case ManagementPackage.DIAGNOSTIC__HISTORY:
				return getHistory();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case ManagementPackage.DIAGNOSTIC__ID:
				setId((String)newValue);
				return;
			case ManagementPackage.DIAGNOSTIC__PRODUCER:
				setProducer((String)newValue);
				return;
			case ManagementPackage.DIAGNOSTIC__SOURCE:
				setSource((String)newValue);
				return;
			case ManagementPackage.DIAGNOSTIC__CODE:
				setCode((String)newValue);
				return;
			case ManagementPackage.DIAGNOSTIC__SEVERITY:
				setSeverity((DiagnosticSeverity)newValue);
				return;
			case ManagementPackage.DIAGNOSTIC__MESSAGE:
				setMessage((String)newValue);
				return;
			case ManagementPackage.DIAGNOSTIC__CATEGORY:
				setCategory((String)newValue);
				return;
			case ManagementPackage.DIAGNOSTIC__TARGET:
				setTarget((String)newValue);
				return;
			case ManagementPackage.DIAGNOSTIC__STATUS:
				setStatus((DiagnosticStatus)newValue);
				return;
			case ManagementPackage.DIAGNOSTIC__CREATED_TIME:
				setCreatedTime((Instant)newValue);
				return;
			case ManagementPackage.DIAGNOSTIC__LAST_CHANGE_TIME:
				setLastChangeTime((Instant)newValue);
				return;
			case ManagementPackage.DIAGNOSTIC__VERSION:
				setVersion((Long)newValue);
				return;
			case ManagementPackage.DIAGNOSTIC__CHILDREN:
				getChildren().clear();
				getChildren().addAll((Collection<? extends Diagnostic>)newValue);
				return;
			case ManagementPackage.DIAGNOSTIC__HISTORY:
				getHistory().clear();
				getHistory().addAll((Collection<? extends DiagnosticChange>)newValue);
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
			case ManagementPackage.DIAGNOSTIC__ID:
				setId(ID_EDEFAULT);
				return;
			case ManagementPackage.DIAGNOSTIC__PRODUCER:
				setProducer(PRODUCER_EDEFAULT);
				return;
			case ManagementPackage.DIAGNOSTIC__SOURCE:
				setSource(SOURCE_EDEFAULT);
				return;
			case ManagementPackage.DIAGNOSTIC__CODE:
				setCode(CODE_EDEFAULT);
				return;
			case ManagementPackage.DIAGNOSTIC__SEVERITY:
				setSeverity(SEVERITY_EDEFAULT);
				return;
			case ManagementPackage.DIAGNOSTIC__MESSAGE:
				setMessage(MESSAGE_EDEFAULT);
				return;
			case ManagementPackage.DIAGNOSTIC__CATEGORY:
				setCategory(CATEGORY_EDEFAULT);
				return;
			case ManagementPackage.DIAGNOSTIC__TARGET:
				setTarget(TARGET_EDEFAULT);
				return;
			case ManagementPackage.DIAGNOSTIC__STATUS:
				setStatus(STATUS_EDEFAULT);
				return;
			case ManagementPackage.DIAGNOSTIC__CREATED_TIME:
				setCreatedTime(CREATED_TIME_EDEFAULT);
				return;
			case ManagementPackage.DIAGNOSTIC__LAST_CHANGE_TIME:
				setLastChangeTime(LAST_CHANGE_TIME_EDEFAULT);
				return;
			case ManagementPackage.DIAGNOSTIC__VERSION:
				setVersion(VERSION_EDEFAULT);
				return;
			case ManagementPackage.DIAGNOSTIC__CHILDREN:
				getChildren().clear();
				return;
			case ManagementPackage.DIAGNOSTIC__HISTORY:
				getHistory().clear();
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
			case ManagementPackage.DIAGNOSTIC__ID:
				return ID_EDEFAULT == null ? id != null : !ID_EDEFAULT.equals(id);
			case ManagementPackage.DIAGNOSTIC__PRODUCER:
				return PRODUCER_EDEFAULT == null ? producer != null : !PRODUCER_EDEFAULT.equals(producer);
			case ManagementPackage.DIAGNOSTIC__SOURCE:
				return SOURCE_EDEFAULT == null ? source != null : !SOURCE_EDEFAULT.equals(source);
			case ManagementPackage.DIAGNOSTIC__CODE:
				return CODE_EDEFAULT == null ? code != null : !CODE_EDEFAULT.equals(code);
			case ManagementPackage.DIAGNOSTIC__SEVERITY:
				return severity != SEVERITY_EDEFAULT;
			case ManagementPackage.DIAGNOSTIC__MESSAGE:
				return MESSAGE_EDEFAULT == null ? message != null : !MESSAGE_EDEFAULT.equals(message);
			case ManagementPackage.DIAGNOSTIC__CATEGORY:
				return CATEGORY_EDEFAULT == null ? category != null : !CATEGORY_EDEFAULT.equals(category);
			case ManagementPackage.DIAGNOSTIC__TARGET:
				return TARGET_EDEFAULT == null ? target != null : !TARGET_EDEFAULT.equals(target);
			case ManagementPackage.DIAGNOSTIC__STATUS:
				return status != STATUS_EDEFAULT;
			case ManagementPackage.DIAGNOSTIC__CREATED_TIME:
				return CREATED_TIME_EDEFAULT == null ? createdTime != null : !CREATED_TIME_EDEFAULT.equals(createdTime);
			case ManagementPackage.DIAGNOSTIC__LAST_CHANGE_TIME:
				return LAST_CHANGE_TIME_EDEFAULT == null ? lastChangeTime != null : !LAST_CHANGE_TIME_EDEFAULT.equals(lastChangeTime);
			case ManagementPackage.DIAGNOSTIC__VERSION:
				return version != VERSION_EDEFAULT;
			case ManagementPackage.DIAGNOSTIC__CHILDREN:
				return children != null && !children.isEmpty();
			case ManagementPackage.DIAGNOSTIC__HISTORY:
				return history != null && !history.isEmpty();
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
		result.append(" (id: ");
		result.append(id);
		result.append(", producer: ");
		result.append(producer);
		result.append(", source: ");
		result.append(source);
		result.append(", code: ");
		result.append(code);
		result.append(", severity: ");
		result.append(severity);
		result.append(", message: ");
		result.append(message);
		result.append(", category: ");
		result.append(category);
		result.append(", target: ");
		result.append(target);
		result.append(", status: ");
		result.append(status);
		result.append(", createdTime: ");
		result.append(createdTime);
		result.append(", lastChangeTime: ");
		result.append(lastChangeTime);
		result.append(", version: ");
		result.append(version);
		result.append(')');
		return result.toString();
	}

} //DiagnosticImpl
