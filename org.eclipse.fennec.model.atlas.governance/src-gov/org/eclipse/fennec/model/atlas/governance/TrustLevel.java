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
 * A representation of the literals of the enumeration '<em><b>Trust Level</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * Defines the level of trust assigned to an asset within the system.
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getTrustLevel()
 * @model
 * @generated
 */
@ProviderType
public enum TrustLevel implements Enumerator {
	/**
	 * The '<em><b>UNKNOWN</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The trust level has not yet been evaluated.
	 * <!-- end-model-doc -->
	 * @see #UNKNOWN_VALUE
	 * @generated
	 * @ordered
	 */
	UNKNOWN(0, "UNKNOWN", "UNKNOWN"),

	/**
	 * The '<em><b>UNTRUSTED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The asset is explicitly considered not trustworthy.
	 * <!-- end-model-doc -->
	 * @see #UNTRUSTED_VALUE
	 * @generated
	 * @ordered
	 */
	UNTRUSTED(1, "UNTRUSTED", "UNTRUSTED"),

	/**
	 * The '<em><b>IN REVIEW</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The asset is currently undergoing a review process to determine its trust level.
	 * <!-- end-model-doc -->
	 * @see #IN_REVIEW_VALUE
	 * @generated
	 * @ordered
	 */
	IN_REVIEW(2, "IN_REVIEW", "IN_REVIEW"),

	/**
	 * The '<em><b>TRUSTED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The asset has been verified and is considered trustworthy.
	 * <!-- end-model-doc -->
	 * @see #TRUSTED_VALUE
	 * @generated
	 * @ordered
	 */
	TRUSTED(3, "TRUSTED", "TRUSTED");

	/**
	 * The '<em><b>UNKNOWN</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The trust level has not yet been evaluated.
	 * <!-- end-model-doc -->
	 * @see #UNKNOWN
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int UNKNOWN_VALUE = 0;

	/**
	 * The '<em><b>UNTRUSTED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The asset is explicitly considered not trustworthy.
	 * <!-- end-model-doc -->
	 * @see #UNTRUSTED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int UNTRUSTED_VALUE = 1;

	/**
	 * The '<em><b>IN REVIEW</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The asset is currently undergoing a review process to determine its trust level.
	 * <!-- end-model-doc -->
	 * @see #IN_REVIEW
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int IN_REVIEW_VALUE = 2;

	/**
	 * The '<em><b>TRUSTED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The asset has been verified and is considered trustworthy.
	 * <!-- end-model-doc -->
	 * @see #TRUSTED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int TRUSTED_VALUE = 3;

	/**
	 * An array of all the '<em><b>Trust Level</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final TrustLevel[] VALUES_ARRAY =
		new TrustLevel[] {
			UNKNOWN,
			UNTRUSTED,
			IN_REVIEW,
			TRUSTED,
		};

	/**
	 * A public read-only list of all the '<em><b>Trust Level</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<TrustLevel> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Trust Level</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static TrustLevel get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			TrustLevel result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Trust Level</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static TrustLevel getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			TrustLevel result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Trust Level</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static TrustLevel get(int value) {
		switch (value) {
			case UNKNOWN_VALUE: return UNKNOWN;
			case UNTRUSTED_VALUE: return UNTRUSTED;
			case IN_REVIEW_VALUE: return IN_REVIEW;
			case TRUSTED_VALUE: return TRUSTED;
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
	private TrustLevel(int value, String name, String literal) {
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
	
} //TrustLevel
