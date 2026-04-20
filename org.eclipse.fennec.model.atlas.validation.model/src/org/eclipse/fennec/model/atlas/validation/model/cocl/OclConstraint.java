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
 * A representation of the model object '<em><b>Ocl Constraint</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Zentrale Klasse zur Definition eines OCL-Constraints. Enthaelt den OCL-Ausdruck, Metadaten und optionale Einschraenkungen auf Zielobjekte.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getExpression <em>Expression</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getSeverity <em>Severity</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getRole <em>Role</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getContextClass <em>Context Class</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getFeatureName <em>Feature Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#isActive <em>Active</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#isOverrides <em>Overrides</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getTargetURIs <em>Target UR Is</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getOclConstraint()
 * @model
 * @generated
 */
@ProviderType
public interface OclConstraint extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Eindeutiger, sprechender Name des Constraints. Wird in der Log-Tabelle und in Fehlermeldungen angezeigt.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getOclConstraint_Name()
	 * @model required="true"
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Fachliche Beschreibung des Constraints. Erklaert den Zweck und die Auswirkungen bei Verletzung.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getOclConstraint_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Expression</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Der OCL-Ausdruck selbst. Muss syntaktisch korrekt sein und zum Kontexttyp passen.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Expression</em>' attribute.
	 * @see #setExpression(String)
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getOclConstraint_Expression()
	 * @model required="true"
	 * @generated
	 */
	String getExpression();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getExpression <em>Expression</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Expression</em>' attribute.
	 * @see #getExpression()
	 * @generated
	 */
	void setExpression(String value);

	/**
	 * Returns the value of the '<em><b>Severity</b></em>' attribute.
	 * The default value is <code>"ERROR"</code>.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.validation.model.cocl.Severity}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Schweregrad bei Constraint-Verletzung. Bestimmt Darstellung und Verhalten.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Severity</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.Severity
	 * @see #setSeverity(Severity)
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getOclConstraint_Severity()
	 * @model default="ERROR" required="true"
	 * @generated
	 */
	Severity getSeverity();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getSeverity <em>Severity</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Severity</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.Severity
	 * @see #getSeverity()
	 * @generated
	 */
	void setSeverity(Severity value);

	/**
	 * Returns the value of the '<em><b>Role</b></em>' attribute.
	 * The default value is <code>"VALIDATION"</code>.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclRole}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Definiert die Verwendung des OCL-Ausdrucks (Validierung, Derived, Filter).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Role</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclRole
	 * @see #setRole(OclRole)
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getOclConstraint_Role()
	 * @model default="VALIDATION" required="true"
	 * @generated
	 */
	OclRole getRole();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getRole <em>Role</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Role</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.OclRole
	 * @see #getRole()
	 * @generated
	 */
	void setRole(OclRole value);

	/**
	 * Returns the value of the '<em><b>Context Class</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Vollqualifizierter Name der EClass, auf die sich dieser Constraint bezieht. Format: EClass URI
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Context Class</em>' attribute.
	 * @see #setContextClass(String)
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getOclConstraint_ContextClass()
	 * @model required="true"
	 * @generated
	 */
	String getContextClass();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getContextClass <em>Context Class</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Context Class</em>' attribute.
	 * @see #getContextClass()
	 * @generated
	 */
	void setContextClass(String value);

	/**
	 * Returns the value of the '<em><b>Feature Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Optional: Name des Features (Attribut/Referenz), auf das sich der Constraint bezieht. Relevant fuer DERIVED und REFERENCE_FILTER Rollen.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Feature Name</em>' attribute.
	 * @see #setFeatureName(String)
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getOclConstraint_FeatureName()
	 * @model
	 * @generated
	 */
	String getFeatureName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#getFeatureName <em>Feature Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Feature Name</em>' attribute.
	 * @see #getFeatureName()
	 * @generated
	 */
	void setFeatureName(String value);

	/**
	 * Returns the value of the '<em><b>Active</b></em>' attribute.
	 * The default value is <code>"true"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Gibt an, ob der Constraint aktiv ist. Inaktive Constraints werden bei der Validierung uebersprungen.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Active</em>' attribute.
	 * @see #setActive(boolean)
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getOclConstraint_Active()
	 * @model default="true" required="true"
	 * @generated
	 */
	boolean isActive();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#isActive <em>Active</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Active</em>' attribute.
	 * @see #isActive()
	 * @generated
	 */
	void setActive(boolean value);

	/**
	 * Returns the value of the '<em><b>Overrides</b></em>' attribute.
	 * The default value is <code>"false"</code>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Wenn true, ueberschreibt dieser Constraint einen gleichnamigen Constraint aus einer Quelle mit niedrigerer Prioritaet (z.B. Ecore).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Overrides</em>' attribute.
	 * @see #setOverrides(boolean)
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getOclConstraint_Overrides()
	 * @model default="false"
	 * @generated
	 */
	boolean isOverrides();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.validation.model.cocl.OclConstraint#isOverrides <em>Overrides</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Overrides</em>' attribute.
	 * @see #isOverrides()
	 * @generated
	 */
	void setOverrides(boolean value);

	/**
	 * Returns the value of the '<em><b>Target UR Is</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Optionale Liste von URIs konkreter EObjects, auf die sich der Constraint beschraenkt. Wenn leer, gilt der Constraint fuer alle Instanzen der contextClass.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Target UR Is</em>' attribute list.
	 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getOclConstraint_TargetURIs()
	 * @model
	 * @generated
	 */
	EList<String> getTargetURIs();

} // OclConstraint
