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
package org.eclipse.fennec.model.atlas.mgmt.management;

import java.time.Instant;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Diagnostic</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * One finding about an object, modelled on org.eclipse.emf.common.util.Diagnostic:
 * a severity, a message, a source and code that identify the kind of finding, and
 * children that refine it. Unlike the EMF type it is persisted with the object's
 * metadata and lives on: it has a stable identity, a status that is decided
 * separately from its severity, a history of the changes made to it, and a version
 * for optimistic locking (issue #291).
 * 
 * Stable id rule: the id is deterministic, derived from producer, code and target
 * (for a child: from the parent's id, code and target). The same finding about the
 * same element therefore gets the same id on every validation run, so references
 * from outside - a GDPR report item, a UI bookmark - survive re-validation without a
 * matching step, and a producer's replace operation is a plain replace by id. Two
 * findings of the same kind about the same element are one finding; a producer
 * that needs to tell them apart puts the distinction into the target or the code.
 * The API that mints ids is part of the diagnostic service (#293); the rule is
 * fixed here so every producer arrives at the same id.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic#getId <em>Id</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic#getProducer <em>Producer</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic#getSource <em>Source</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic#getCode <em>Code</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic#getSeverity <em>Severity</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic#getMessage <em>Message</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic#getCategory <em>Category</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic#getTarget <em>Target</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic#getStatus <em>Status</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic#getCreatedTime <em>Created Time</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic#getLastChangeTime <em>Last Change Time</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic#getVersion <em>Version</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic#getChildren <em>Children</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic#getHistory <em>History</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getDiagnostic()
 * @model
 * @generated
 */
@ProviderType
public interface Diagnostic extends EObject {
	/**
	 * Returns the value of the '<em><b>Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Unique and stable identifier of this diagnostic, derived from producer, code and
	 * target (see the class documentation). Unique within the object's metadata; external
	 * artifacts address a diagnostic by (scope, registry, stage, objectId, id).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Id</em>' attribute.
	 * @see #setId(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getDiagnostic_Id()
	 * @model id="true" required="true"
	 * @generated
	 */
	String getId();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic#getId <em>Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Id</em>' attribute.
	 * @see #getId()
	 * @generated
	 */
	void setId(String value);

	/**
	 * Returns the value of the '<em><b>Producer</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The action, gate or module that created this diagnostic, for example
	 * `QvtTransitionGate` or `gdpr.review`. Ownership: a producer's replace operation
	 * replaces exactly the roots carrying its own producer value. Set on roots; children
	 * inherit their parent's producer and carry the same value.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Producer</em>' attribute.
	 * @see #setProducer(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getDiagnostic_Producer()
	 * @model required="true"
	 * @generated
	 */
	String getProducer();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic#getProducer <em>Producer</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Producer</em>' attribute.
	 * @see #getProducer()
	 * @generated
	 */
	void setProducer(String value);

	/**
	 * Returns the value of the '<em><b>Source</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Identifies the origin of the finding within the producer, in the sense of
	 * org.eclipse.emf.common.util.Diagnostic#getSource(): typically the validator, rule
	 * set or compiler that raised it, for example `org.eclipse.fennec.m2x.qvto`. Optional;
	 * a producer with a single source may leave it unset.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Source</em>' attribute.
	 * @see #setSource(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getDiagnostic_Source()
	 * @model
	 * @generated
	 */
	String getSource();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic#getSource <em>Source</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Source</em>' attribute.
	 * @see #getSource()
	 * @generated
	 */
	void setSource(String value);

	/**
	 * Returns the value of the '<em><b>Code</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Producer-defined identifier of the kind of finding, stable across releases of the
	 * producer, for example `unresolved-import` or `personal-data-without-purpose`. Part
	 * of the id; a client may switch on it, the message is for people.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Code</em>' attribute.
	 * @see #setCode(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getDiagnostic_Code()
	 * @model required="true"
	 * @generated
	 */
	String getCode();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic#getCode <em>Code</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Code</em>' attribute.
	 * @see #getCode()
	 * @generated
	 */
	void setCode(String value);

	/**
	 * Returns the value of the '<em><b>Severity</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * How serious the finding is. Independent of status: a resolved error is still
	 * recognisable as a former error. A module may raise or lower the severity later
	 * (#293); every such change is recorded in the history.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Severity</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity
	 * @see #setSeverity(DiagnosticSeverity)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getDiagnostic_Severity()
	 * @model required="true"
	 * @generated
	 */
	DiagnosticSeverity getSeverity();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic#getSeverity <em>Severity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Severity</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity
	 * @see #getSeverity()
	 * @generated
	 */
	void setSeverity(DiagnosticSeverity value);

	/**
	 * Returns the value of the '<em><b>Message</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Human-readable description of the finding, written for whoever looks at the
	 * object. It travels to clients unchanged, so it carries no internal detail such as
	 * storage paths.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Message</em>' attribute.
	 * @see #setMessage(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getDiagnostic_Message()
	 * @model required="true"
	 * @generated
	 */
	String getMessage();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic#getMessage <em>Message</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Message</em>' attribute.
	 * @see #getMessage()
	 * @generated
	 */
	void setMessage(String value);

	/**
	 * Returns the value of the '<em><b>Category</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Coarse classification a UI can filter and group by, across producers, for
	 * example `compile`, `reference`, `compliance`. Optional. Not part of the id, so a
	 * producer may recategorise without minting new ids.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Category</em>' attribute.
	 * @see #setCategory(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getDiagnostic_Category()
	 * @model
	 * @generated
	 */
	String getCategory();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic#getCategory <em>Category</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Category</em>' attribute.
	 * @see #getCategory()
	 * @generated
	 */
	void setCategory(String value);

	/**
	 * Returns the value of the '<em><b>Target</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The affected element inside the object, as a URI or URI fragment (for example
	 * `//Person/birthDate` for an EStructuralFeature, or `line:12:col:4` for a text
	 * position a producer defines). Optional; unset means the finding is about the object
	 * as a whole. Part of the id, so the same finding about two elements yields two
	 * diagnostics.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Target</em>' attribute.
	 * @see #setTarget(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getDiagnostic_Target()
	 * @model
	 * @generated
	 */
	String getTarget();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic#getTarget <em>Target</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Target</em>' attribute.
	 * @see #getTarget()
	 * @generated
	 */
	void setTarget(String value);

	/**
	 * Returns the value of the '<em><b>Status</b></em>' attribute.
	 * The default value is <code>"OPEN"</code>.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticStatus}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Where the finding stands in its life: OPEN when produced, ACKNOWLEDGED when
	 * someone has seen it and accepts it for now, RESOLVED when it no longer applies
	 * or was dealt with. Kept separate from severity on purpose. A status set by a
	 * person is not silently reverted by an automatic re-validation (#293); the history
	 * tells who set it.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Status</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticStatus
	 * @see #setStatus(DiagnosticStatus)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getDiagnostic_Status()
	 * @model default="OPEN" required="true"
	 * @generated
	 */
	DiagnosticStatus getStatus();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic#getStatus <em>Status</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Status</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticStatus
	 * @see #getStatus()
	 * @generated
	 */
	void setStatus(DiagnosticStatus value);

	/**
	 * Returns the value of the '<em><b>Created Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * When the diagnostic was first produced. A re-validation that yields the same id
	 * keeps this value; the history records that it was seen again.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Created Time</em>' attribute.
	 * @see #setCreatedTime(Instant)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getDiagnostic_CreatedTime()
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Instant" required="true"
	 * @generated
	 */
	Instant getCreatedTime();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic#getCreatedTime <em>Created Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Created Time</em>' attribute.
	 * @see #getCreatedTime()
	 * @generated
	 */
	void setCreatedTime(Instant value);

	/**
	 * Returns the value of the '<em><b>Last Change Time</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * When severity, status or message last changed; equals the time of the newest
	 * history entry. Unset while the diagnostic is untouched since creation.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Last Change Time</em>' attribute.
	 * @see #setLastChangeTime(Instant)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getDiagnostic_LastChangeTime()
	 * @model dataType="org.eclipse.fennec.model.atlas.mgmt.management.Instant"
	 * @generated
	 */
	Instant getLastChangeTime();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic#getLastChangeTime <em>Last Change Time</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Last Change Time</em>' attribute.
	 * @see #getLastChangeTime()
	 * @generated
	 */
	void setLastChangeTime(Instant value);

	/**
	 * Returns the value of the '<em><b>Version</b></em>' attribute.
	 * The default value is <code>"0"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Optimistic-locking counter, incremented on every change to this diagnostic
	 * (#293). A change that names an older version is refused as a conflict. Starts at 0.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Version</em>' attribute.
	 * @see #setVersion(long)
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getDiagnostic_Version()
	 * @model default="0" required="true"
	 * @generated
	 */
	long getVersion();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic#getVersion <em>Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Version</em>' attribute.
	 * @see #getVersion()
	 * @generated
	 */
	void setVersion(long value);

	/**
	 * Returns the value of the '<em><b>Children</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Findings that refine this one, as in an EMF diagnostic tree: a compile failure
	 * with one child per error, a compliance finding with one child per affected
	 * feature. Children share the parent's producer; their ids derive from the parent's
	 * id.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Children</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getDiagnostic_Children()
	 * @model containment="true" keys="id"
	 * @generated
	 */
	EList<Diagnostic> getChildren();

	/**
	 * Returns the value of the '<em><b>History</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticChange}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Every change made to this diagnostic after it was produced, oldest first: who,
	 * when, what changed and why. Appended by the diagnostic service (#293), never
	 * edited.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>History</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.mgmt.management.ManagementPackage#getDiagnostic_History()
	 * @model containment="true"
	 * @generated
	 */
	EList<DiagnosticChange> getHistory();

} // Diagnostic
