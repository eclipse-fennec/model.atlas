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
 *      Mark Hoffmann - initial API and implementation
 */
package org.gecko.mac.governance;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>Compliance Status</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * Status of individual compliance checks within governance documentation
 * <!-- end-model-doc -->
 * @see org.gecko.mac.governance.GovernancePackage#getComplianceStatus()
 * @model
 * @generated
 */
@ProviderType
public enum ComplianceStatus implements Enumerator {
	/**
	 * The '<em><b>NOT CHECKED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Compliance check has not been performed yet
	 * <!-- end-model-doc -->
	 * @see #NOT_CHECKED_VALUE
	 * @generated
	 * @ordered
	 */
	NOT_CHECKED(0, "NOT_CHECKED", "NOT_CHECKED"),

	/**
	 * The '<em><b>PENDING</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Compliance check is in progress
	 * <!-- end-model-doc -->
	 * @see #PENDING_VALUE
	 * @generated
	 * @ordered
	 */
	PENDING(1, "PENDING", "PENDING"),

	/**
	 * The '<em><b>PASSED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Compliance check passed successfully
	 * <!-- end-model-doc -->
	 * @see #PASSED_VALUE
	 * @generated
	 * @ordered
	 */
	PASSED(2, "PASSED", "PASSED"),

	/**
	 * The '<em><b>FAILED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Compliance check failed
	 * <!-- end-model-doc -->
	 * @see #FAILED_VALUE
	 * @generated
	 * @ordered
	 */
	FAILED(3, "FAILED", "FAILED"),

	/**
	 * The '<em><b>NOT APPLICABLE</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Compliance check is not applicable for this object type
	 * <!-- end-model-doc -->
	 * @see #NOT_APPLICABLE_VALUE
	 * @generated
	 * @ordered
	 */
	NOT_APPLICABLE(4, "NOT_APPLICABLE", "NOT_APPLICABLE"),

	/**
	 * The '<em><b>PARTIAL</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Some aspects passed, others failed - requires manual review
	 * <!-- end-model-doc -->
	 * @see #PARTIAL_VALUE
	 * @generated
	 * @ordered
	 */
	PARTIAL(5, "PARTIAL", "PARTIAL");

	/**
	 * The '<em><b>NOT CHECKED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Compliance check has not been performed yet
	 * <!-- end-model-doc -->
	 * @see #NOT_CHECKED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int NOT_CHECKED_VALUE = 0;

	/**
	 * The '<em><b>PENDING</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Compliance check is in progress
	 * <!-- end-model-doc -->
	 * @see #PENDING
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int PENDING_VALUE = 1;

	/**
	 * The '<em><b>PASSED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Compliance check passed successfully
	 * <!-- end-model-doc -->
	 * @see #PASSED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int PASSED_VALUE = 2;

	/**
	 * The '<em><b>FAILED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Compliance check failed
	 * <!-- end-model-doc -->
	 * @see #FAILED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int FAILED_VALUE = 3;

	/**
	 * The '<em><b>NOT APPLICABLE</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Compliance check is not applicable for this object type
	 * <!-- end-model-doc -->
	 * @see #NOT_APPLICABLE
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int NOT_APPLICABLE_VALUE = 4;

	/**
	 * The '<em><b>PARTIAL</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Some aspects passed, others failed - requires manual review
	 * <!-- end-model-doc -->
	 * @see #PARTIAL
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int PARTIAL_VALUE = 5;

	/**
	 * An array of all the '<em><b>Compliance Status</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final ComplianceStatus[] VALUES_ARRAY =
		new ComplianceStatus[] {
			NOT_CHECKED,
			PENDING,
			PASSED,
			FAILED,
			NOT_APPLICABLE,
			PARTIAL,
		};

	/**
	 * A public read-only list of all the '<em><b>Compliance Status</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<ComplianceStatus> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Compliance Status</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static ComplianceStatus get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			ComplianceStatus result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Compliance Status</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static ComplianceStatus getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			ComplianceStatus result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Compliance Status</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static ComplianceStatus get(int value) {
		switch (value) {
			case NOT_CHECKED_VALUE: return NOT_CHECKED;
			case PENDING_VALUE: return PENDING;
			case PASSED_VALUE: return PASSED;
			case FAILED_VALUE: return FAILED;
			case NOT_APPLICABLE_VALUE: return NOT_APPLICABLE;
			case PARTIAL_VALUE: return PARTIAL;
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
	private ComplianceStatus(int value, String name, String literal) {
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
	
} //ComplianceStatus
