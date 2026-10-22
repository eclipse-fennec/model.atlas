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
package inventory;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Source Ref</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Loose reference to a defining or evidencing artifact, without a dependency on its model. A changed fingerprint shows that the source changed.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link inventory.SourceRef#getKind <em>Kind</em>}</li>
 *   <li>{@link inventory.SourceRef#getFormat <em>Format</em>}</li>
 *   <li>{@link inventory.SourceRef#getUri <em>Uri</em>}</li>
 *   <li>{@link inventory.SourceRef#getSourceRevision <em>Source Revision</em>}</li>
 *   <li>{@link inventory.SourceRef#getName <em>Name</em>}</li>
 *   <li>{@link inventory.SourceRef#getObject <em>Object</em>}</li>
 * </ul>
 *
 * @see inventory.InventoryPackage#getSourceRef()
 * @model
 * @generated
 */
@ProviderType
public interface SourceRef extends EObject {
	/**
	 * Returns the value of the '<em><b>Kind</b></em>' attribute.
	 * The literals are from the enumeration {@link inventory.SourceKind}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * What role the artifact plays.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Kind</em>' attribute.
	 * @see inventory.SourceKind
	 * @see #setKind(SourceKind)
	 * @see inventory.InventoryPackage#getSourceRef_Kind()
	 * @model required="true"
	 * @generated
	 */
	SourceKind getKind();

	/**
	 * Sets the value of the '{@link inventory.SourceRef#getKind <em>Kind</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Kind</em>' attribute.
	 * @see inventory.SourceKind
	 * @see #getKind()
	 * @generated
	 */
	void setKind(SourceKind value);

	/**
	 * Returns the value of the '<em><b>Format</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Technical format of the artifact, free text so that the model stays independent of a runtime or tool. Common values: quadlet, compose, kubernetes, helm, terraform, xmi, ecore, json, yaml, pdf.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Format</em>' attribute.
	 * @see #setFormat(String)
	 * @see inventory.InventoryPackage#getSourceRef_Format()
	 * @model
	 * @generated
	 */
	String getFormat();

	/**
	 * Sets the value of the '{@link inventory.SourceRef#getFormat <em>Format</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Format</em>' attribute.
	 * @see #getFormat()
	 * @generated
	 */
	void setFormat(String value);

	/**
	 * Returns the value of the '<em><b>Uri</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * URI of the artifact, e.g. an endpoint where the source is reachable, a file uri when a source is a document
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Uri</em>' attribute.
	 * @see #setUri(String)
	 * @see inventory.InventoryPackage#getSourceRef_Uri()
	 * @model required="true"
	 * @generated
	 */
	String getUri();

	/**
	 * Sets the value of the '{@link inventory.SourceRef#getUri <em>Uri</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Uri</em>' attribute.
	 * @see #getUri()
	 * @generated
	 */
	void setUri(String value);

	/**
	 * Returns the value of the '<em><b>Source Revision</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * an identifier for the source reference. Whenever the source reference changes (e.g. the format changes, the uri changes, or so on), a new revision is set.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Source Revision</em>' attribute.
	 * @see #setSourceRevision(String)
	 * @see inventory.InventoryPackage#getSourceRef_SourceRevision()
	 * @model
	 * @generated
	 */
	String getSourceRevision();

	/**
	 * Sets the value of the '{@link inventory.SourceRef#getSourceRevision <em>Source Revision</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Source Revision</em>' attribute.
	 * @see #getSourceRevision()
	 * @generated
	 */
	void setSourceRevision(String value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Display name.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see inventory.InventoryPackage#getSourceRef_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link inventory.SourceRef#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Object</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * This is an optional reference to the object the URI of the source resolves into.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Object</em>' reference.
	 * @see #setObject(EObject)
	 * @see inventory.InventoryPackage#getSourceRef_Object()
	 * @model
	 * @generated
	 */
	EObject getObject();

	/**
	 * Sets the value of the '{@link inventory.SourceRef#getObject <em>Object</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Object</em>' reference.
	 * @see #getObject()
	 * @generated
	 */
	void setObject(EObject value);

} // SourceRef
