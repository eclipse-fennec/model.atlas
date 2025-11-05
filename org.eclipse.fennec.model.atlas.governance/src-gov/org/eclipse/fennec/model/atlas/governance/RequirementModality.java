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
package org.eclipse.fennec.model.atlas.governance;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>Requirement Modality</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * Defines the modality of a policy requirement, based on RFC 2119.
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getRequirementModality()
 * @model
 * @generated
 */
@ProviderType
public enum RequirementModality implements Enumerator {
	/**
	 * The '<em><b>MUST</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Indicates an absolute requirement.
	 * <!-- end-model-doc -->
	 * @see #MUST_VALUE
	 * @generated
	 * @ordered
	 */
	MUST(0, "MUST", "MUST"),

	/**
	 * The '<em><b>SHOULD</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Indicates a strong recommendation. There may exist valid reasons to ignore it, but the full implications must be understood.
	 * <!-- end-model-doc -->
	 * @see #SHOULD_VALUE
	 * @generated
	 * @ordered
	 */
	SHOULD(1, "SHOULD", "SHOULD"),

	/**
	 * The '<em><b>MAY</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Indicates an optional course of action.
	 * <!-- end-model-doc -->
	 * @see #MAY_VALUE
	 * @generated
	 * @ordered
	 */
	MAY(2, "MAY", "MAY"),

	/**
	 * The '<em><b>MUST NOT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Indicates an absolute prohibition.
	 * <!-- end-model-doc -->
	 * @see #MUST_NOT_VALUE
	 * @generated
	 * @ordered
	 */
	MUST_NOT(3, "MUST_NOT", "MUST_NOT"),

	/**
	 * The '<em><b>SHOULD NOT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Indicates a strong recommendation against a course of action.
	 * <!-- end-model-doc -->
	 * @see #SHOULD_NOT_VALUE
	 * @generated
	 * @ordered
	 */
	SHOULD_NOT(4, "SHOULD_NOT", "SHOULD_NOT");

	/**
	 * The '<em><b>MUST</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Indicates an absolute requirement.
	 * <!-- end-model-doc -->
	 * @see #MUST
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int MUST_VALUE = 0;

	/**
	 * The '<em><b>SHOULD</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Indicates a strong recommendation. There may exist valid reasons to ignore it, but the full implications must be understood.
	 * <!-- end-model-doc -->
	 * @see #SHOULD
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int SHOULD_VALUE = 1;

	/**
	 * The '<em><b>MAY</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Indicates an optional course of action.
	 * <!-- end-model-doc -->
	 * @see #MAY
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int MAY_VALUE = 2;

	/**
	 * The '<em><b>MUST NOT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Indicates an absolute prohibition.
	 * <!-- end-model-doc -->
	 * @see #MUST_NOT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int MUST_NOT_VALUE = 3;

	/**
	 * The '<em><b>SHOULD NOT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Indicates a strong recommendation against a course of action.
	 * <!-- end-model-doc -->
	 * @see #SHOULD_NOT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int SHOULD_NOT_VALUE = 4;

	/**
	 * An array of all the '<em><b>Requirement Modality</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final RequirementModality[] VALUES_ARRAY =
		new RequirementModality[] {
			MUST,
			SHOULD,
			MAY,
			MUST_NOT,
			SHOULD_NOT,
		};

	/**
	 * A public read-only list of all the '<em><b>Requirement Modality</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<RequirementModality> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Requirement Modality</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static RequirementModality get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			RequirementModality result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Requirement Modality</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static RequirementModality getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			RequirementModality result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Requirement Modality</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static RequirementModality get(int value) {
		switch (value) {
			case MUST_VALUE: return MUST;
			case SHOULD_VALUE: return SHOULD;
			case MAY_VALUE: return MAY;
			case MUST_NOT_VALUE: return MUST_NOT;
			case SHOULD_NOT_VALUE: return SHOULD_NOT;
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
	private RequirementModality(int value, String name, String literal) {
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
	
} //RequirementModality
