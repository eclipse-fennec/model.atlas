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
package org.gecko.mac.policy.opendata;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>License</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * Enumeration of common Open Data licenses.
 * <!-- end-model-doc -->
 * @see org.gecko.mac.policy.opendata.OpenDataPackage#getOpenDataLicense()
 * @model
 * @generated
 */
@ProviderType
public enum OpenDataLicense implements Enumerator {
	/**
	 * The '<em><b>NOT APPLICABLE</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The data is not intended for public release.
	 * <!-- end-model-doc -->
	 * @see #NOT_APPLICABLE_VALUE
	 * @generated
	 * @ordered
	 */
	NOT_APPLICABLE(0, "NOT_APPLICABLE", "NOT_APPLICABLE"),

	/**
	 * The '<em><b>PROPRIETARY</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The data is under a proprietary license and is not Open Data.
	 * <!-- end-model-doc -->
	 * @see #PROPRIETARY_VALUE
	 * @generated
	 * @ordered
	 */
	PROPRIETARY(1, "PROPRIETARY", "PROPRIETARY"),

	/**
	 * The '<em><b>CC0</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Creative Commons Zero - Public Domain Dedication.
	 * <!-- end-model-doc -->
	 * @see #CC0_VALUE
	 * @generated
	 * @ordered
	 */
	CC0(2, "CC0", "CC0"),

	/**
	 * The '<em><b>CC BY</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Creative Commons Attribution.
	 * <!-- end-model-doc -->
	 * @see #CC_BY_VALUE
	 * @generated
	 * @ordered
	 */
	CC_BY(3, "CC_BY", "CC_BY"),

	/**
	 * The '<em><b>ODb L</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Open Data Commons Open Database License.
	 * <!-- end-model-doc -->
	 * @see #ODB_L_VALUE
	 * @generated
	 * @ordered
	 */
	ODB_L(4, "ODbL", "ODbL"),

	/**
	 * The '<em><b>PDDL</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Open Data Commons Public Domain Dedication and License.
	 * <!-- end-model-doc -->
	 * @see #PDDL_VALUE
	 * @generated
	 * @ordered
	 */
	PDDL(5, "PDDL", "PDDL");

	/**
	 * The '<em><b>NOT APPLICABLE</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The data is not intended for public release.
	 * <!-- end-model-doc -->
	 * @see #NOT_APPLICABLE
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int NOT_APPLICABLE_VALUE = 0;

	/**
	 * The '<em><b>PROPRIETARY</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The data is under a proprietary license and is not Open Data.
	 * <!-- end-model-doc -->
	 * @see #PROPRIETARY
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int PROPRIETARY_VALUE = 1;

	/**
	 * The '<em><b>CC0</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Creative Commons Zero - Public Domain Dedication.
	 * <!-- end-model-doc -->
	 * @see #CC0
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int CC0_VALUE = 2;

	/**
	 * The '<em><b>CC BY</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Creative Commons Attribution.
	 * <!-- end-model-doc -->
	 * @see #CC_BY
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int CC_BY_VALUE = 3;

	/**
	 * The '<em><b>ODb L</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Open Data Commons Open Database License.
	 * <!-- end-model-doc -->
	 * @see #ODB_L
	 * @model name="ODbL"
	 * @generated
	 * @ordered
	 */
	public static final int ODB_L_VALUE = 4;

	/**
	 * The '<em><b>PDDL</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Open Data Commons Public Domain Dedication and License.
	 * <!-- end-model-doc -->
	 * @see #PDDL
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int PDDL_VALUE = 5;

	/**
	 * An array of all the '<em><b>License</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final OpenDataLicense[] VALUES_ARRAY =
		new OpenDataLicense[] {
			NOT_APPLICABLE,
			PROPRIETARY,
			CC0,
			CC_BY,
			ODB_L,
			PDDL,
		};

	/**
	 * A public read-only list of all the '<em><b>License</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<OpenDataLicense> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>License</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static OpenDataLicense get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			OpenDataLicense result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>License</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static OpenDataLicense getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			OpenDataLicense result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>License</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static OpenDataLicense get(int value) {
		switch (value) {
			case NOT_APPLICABLE_VALUE: return NOT_APPLICABLE;
			case PROPRIETARY_VALUE: return PROPRIETARY;
			case CC0_VALUE: return CC0;
			case CC_BY_VALUE: return CC_BY;
			case ODB_L_VALUE: return ODB_L;
			case PDDL_VALUE: return PDDL;
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
	private OpenDataLicense(int value, String name, String literal) {
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
	
} //OpenDataLicense
