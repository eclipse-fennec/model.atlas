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
 * A representation of the model object '<em><b>Ocl Constraint Set</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Container fuer eine Sammlung von OCL-Constraints. Entspricht einer *.c-ocl Datei.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet#getVersion <em>Version</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet#getConstraints <em>Constraints</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet#getTargetModelNsURIs <em>Target Model Ns UR Is</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getOclConstraintSet()
 * @model
 * @generated
 */
@ProviderType
public interface OclConstraintSet extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Name des Constraint-Sets. Wird zur Identifikation und im Log verwendet.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getOclConstraintSet_Name()
	 * @model id="true" required="true"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Version</b></em>' attribute.
	 * The default value is <code>"1.0"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Versionsnummer des Constraint-Sets fuer Kompatibilitaetspruefungen.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Version</em>' attribute.
	 * @see #setVersion(String)
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getOclConstraintSet_Version()
	 * @model default="1.0"
	 * @generated
	 */
	String getVersion();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet#getVersion <em>Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Version</em>' attribute.
	 * @see #getVersion()
	 * @generated
	 */
	void setVersion(String value);

	/**
	 * Returns the value of the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Optionale Beschreibung des Constraint-Sets.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getOclConstraintSet_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraintSet#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Constraints</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Liste der enthaltenen OCL-Constraints.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Constraints</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getOclConstraintSet_Constraints()
	 * @model containment="true" keys="name"
	 * @generated
	 */
	EList<OclConstraint> getConstraints();

	/**
	 * Returns the value of the '<em><b>Target Model Ns UR Is</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Liste von Namespace URIs der Zielmodelle, fuer die dieses Constraint-Set gilt.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Target Model Ns UR Is</em>' attribute list.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getOclConstraintSet_TargetModelNsURIs()
	 * @model
	 * @generated
	 */
	EList<String> getTargetModelNsURIs();

} // OclConstraintSet
