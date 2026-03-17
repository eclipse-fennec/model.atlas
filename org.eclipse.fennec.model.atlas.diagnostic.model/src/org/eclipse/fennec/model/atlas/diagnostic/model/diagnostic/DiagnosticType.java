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
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>Type</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.model.atlas.diagnostic.model.diagnostic.DiagnosticPackage#getDiagnosticType()
 * @model
 * @generated
 */
@ProviderType
public enum DiagnosticType implements Enumerator {
	/**
	 * The '<em><b>OK</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #OK_VALUE
	 * @generated
	 * @ordered
	 */
	OK(0, "OK", "OK"),

	/**
	 * The '<em><b>INFO</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #INFO_VALUE
	 * @generated
	 * @ordered
	 */
	INFO(1, "INFO", "INFO"),

	/**
	 * The '<em><b>WARNING</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #WARNING_VALUE
	 * @generated
	 * @ordered
	 */
	WARNING(2, "WARNING", "WARNING"),

	/**
	 * The '<em><b>ERROR</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #ERROR_VALUE
	 * @generated
	 * @ordered
	 */
	ERROR(3, "ERROR", "ERROR"),

	/**
	 * The '<em><b>CANCEL</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #CANCEL_VALUE
	 * @generated
	 * @ordered
	 */
	CANCEL(4, "CANCEL", "CANCEL");

	/**
	 * The '<em><b>OK</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #OK
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int OK_VALUE = 0;

	/**
	 * The '<em><b>INFO</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #INFO
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int INFO_VALUE = 1;

	/**
	 * The '<em><b>WARNING</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #WARNING
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int WARNING_VALUE = 2;

	/**
	 * The '<em><b>ERROR</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #ERROR
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int ERROR_VALUE = 3;

	/**
	 * The '<em><b>CANCEL</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #CANCEL
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int CANCEL_VALUE = 4;

	/**
	 * An array of all the '<em><b>Type</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final DiagnosticType[] VALUES_ARRAY =
		new DiagnosticType[] {
			OK,
			INFO,
			WARNING,
			ERROR,
			CANCEL,
		};

	/**
	 * A public read-only list of all the '<em><b>Type</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<DiagnosticType> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Type</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static DiagnosticType get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			DiagnosticType result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Type</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static DiagnosticType getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			DiagnosticType result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Type</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static DiagnosticType get(int value) {
		switch (value) {
			case OK_VALUE: return OK;
			case INFO_VALUE: return INFO;
			case WARNING_VALUE: return WARNING;
			case ERROR_VALUE: return ERROR;
			case CANCEL_VALUE: return CANCEL;
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
	private DiagnosticType(int value, String name, String literal) {
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
	
} //DiagnosticType
