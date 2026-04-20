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

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>Ocl Role</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * Definiert die Rolle/Verwendung eines OCL-Ausdrucks im System.
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.model.atlas.validation.model.cocl.COCLPackage#getOclRole()
 * @model
 * @generated
 */
@ProviderType
public enum OclRole implements Enumerator {
	/**
	 * The '<em><b>VALIDATION</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * OCL wird zur Validierung von Instanzen verwendet.
	 * <!-- end-model-doc -->
	 * @see #VALIDATION_VALUE
	 * @generated
	 * @ordered
	 */
	VALIDATION(0, "VALIDATION", "VALIDATION"),

	/**
	 * The '<em><b>DERIVED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * OCL wird zur Berechnung von Derived Values verwendet.
	 * <!-- end-model-doc -->
	 * @see #DERIVED_VALUE
	 * @generated
	 * @ordered
	 */
	DERIVED(1, "DERIVED", "DERIVED"),

	/**
	 * The '<em><b>REFERENCE FILTER</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * OCL wird zur Filterung von Referenzzielen verwendet.
	 * <!-- end-model-doc -->
	 * @see #REFERENCE_FILTER_VALUE
	 * @generated
	 * @ordered
	 */
	REFERENCE_FILTER(2, "REFERENCE_FILTER", "REFERENCE_FILTER"),

	/**
	 * The '<em><b>OPERATION</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * OCL to execute an operation.
	 * <!-- end-model-doc -->
	 * @see #OPERATION_VALUE
	 * @generated
	 * @ordered
	 */
	OPERATION(3, "OPERATION", "OPERATION");

	/**
	 * The '<em><b>VALIDATION</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * OCL wird zur Validierung von Instanzen verwendet.
	 * <!-- end-model-doc -->
	 * @see #VALIDATION
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int VALIDATION_VALUE = 0;

	/**
	 * The '<em><b>DERIVED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * OCL wird zur Berechnung von Derived Values verwendet.
	 * <!-- end-model-doc -->
	 * @see #DERIVED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int DERIVED_VALUE = 1;

	/**
	 * The '<em><b>REFERENCE FILTER</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * OCL wird zur Filterung von Referenzzielen verwendet.
	 * <!-- end-model-doc -->
	 * @see #REFERENCE_FILTER
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int REFERENCE_FILTER_VALUE = 2;

	/**
	 * The '<em><b>OPERATION</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * OCL to execute an operation.
	 * <!-- end-model-doc -->
	 * @see #OPERATION
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int OPERATION_VALUE = 3;

	/**
	 * An array of all the '<em><b>Ocl Role</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final OclRole[] VALUES_ARRAY =
		new OclRole[] {
			VALIDATION,
			DERIVED,
			REFERENCE_FILTER,
			OPERATION,
		};

	/**
	 * A public read-only list of all the '<em><b>Ocl Role</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<OclRole> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Ocl Role</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static OclRole get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			OclRole result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Ocl Role</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static OclRole getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			OclRole result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Ocl Role</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static OclRole get(int value) {
		switch (value) {
			case VALIDATION_VALUE: return VALIDATION;
			case DERIVED_VALUE: return DERIVED;
			case REFERENCE_FILTER_VALUE: return REFERENCE_FILTER;
			case OPERATION_VALUE: return OPERATION;
		}
		return null;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final int value;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final String name;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private final String literal;

	/**
	 * Only this class can construct instances.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private OclRole(int value, String name, String literal) {
		this.value = value;
		this.name = name;
		this.literal = literal;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public int getValue() {
	  return value;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getName() {
	  return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getLiteral() {
	  return literal;
	}

	/**
	 * Returns the literal value of the enumerator, which is its string representation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		return literal;
	}
	
} //OclRole
