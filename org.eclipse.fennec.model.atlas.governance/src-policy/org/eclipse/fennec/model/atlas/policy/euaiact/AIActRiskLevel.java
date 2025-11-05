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
package org.eclipse.fennec.model.atlas.policy.euaiact;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>AI Act Risk Level</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * Enumeration of risk levels as defined by the EU AI Act.
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.model.atlas.policy.euaiact.EUAIPackage#getAIActRiskLevel()
 * @model
 * @generated
 */
@ProviderType
public enum AIActRiskLevel implements Enumerator {
	/**
	 * The '<em><b>UNKNOWN</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The risk level has not yet been determined.
	 * <!-- end-model-doc -->
	 * @see #UNKNOWN_VALUE
	 * @generated
	 * @ordered
	 */
	UNKNOWN(0, "UNKNOWN", "UNKNOWN"),

	/**
	 * The '<em><b>MINIMAL OR NO RISK</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The AI system does not pose a significant risk.
	 * <!-- end-model-doc -->
	 * @see #MINIMAL_OR_NO_RISK_VALUE
	 * @generated
	 * @ordered
	 */
	MINIMAL_OR_NO_RISK(1, "MINIMAL_OR_NO_RISK", "MINIMAL_OR_NO_RISK"),

	/**
	 * The '<em><b>LIMITED RISK</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The AI system is subject to transparency obligations.
	 * <!-- end-model-doc -->
	 * @see #LIMITED_RISK_VALUE
	 * @generated
	 * @ordered
	 */
	LIMITED_RISK(2, "LIMITED_RISK", "LIMITED_RISK"),

	/**
	 * The '<em><b>HIGH RISK</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The AI system is classified as high-risk and is subject to strict requirements.
	 * <!-- end-model-doc -->
	 * @see #HIGH_RISK_VALUE
	 * @generated
	 * @ordered
	 */
	HIGH_RISK(3, "HIGH_RISK", "HIGH_RISK"),

	/**
	 * The '<em><b>UNACCEPTABLE RISK</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The AI system is prohibited.
	 * <!-- end-model-doc -->
	 * @see #UNACCEPTABLE_RISK_VALUE
	 * @generated
	 * @ordered
	 */
	UNACCEPTABLE_RISK(4, "UNACCEPTABLE_RISK", "UNACCEPTABLE_RISK");

	/**
	 * The '<em><b>UNKNOWN</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The risk level has not yet been determined.
	 * <!-- end-model-doc -->
	 * @see #UNKNOWN
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int UNKNOWN_VALUE = 0;

	/**
	 * The '<em><b>MINIMAL OR NO RISK</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The AI system does not pose a significant risk.
	 * <!-- end-model-doc -->
	 * @see #MINIMAL_OR_NO_RISK
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int MINIMAL_OR_NO_RISK_VALUE = 1;

	/**
	 * The '<em><b>LIMITED RISK</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The AI system is subject to transparency obligations.
	 * <!-- end-model-doc -->
	 * @see #LIMITED_RISK
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int LIMITED_RISK_VALUE = 2;

	/**
	 * The '<em><b>HIGH RISK</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The AI system is classified as high-risk and is subject to strict requirements.
	 * <!-- end-model-doc -->
	 * @see #HIGH_RISK
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int HIGH_RISK_VALUE = 3;

	/**
	 * The '<em><b>UNACCEPTABLE RISK</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The AI system is prohibited.
	 * <!-- end-model-doc -->
	 * @see #UNACCEPTABLE_RISK
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int UNACCEPTABLE_RISK_VALUE = 4;

	/**
	 * An array of all the '<em><b>AI Act Risk Level</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final AIActRiskLevel[] VALUES_ARRAY =
		new AIActRiskLevel[] {
			UNKNOWN,
			MINIMAL_OR_NO_RISK,
			LIMITED_RISK,
			HIGH_RISK,
			UNACCEPTABLE_RISK,
		};

	/**
	 * A public read-only list of all the '<em><b>AI Act Risk Level</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<AIActRiskLevel> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>AI Act Risk Level</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static AIActRiskLevel get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			AIActRiskLevel result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>AI Act Risk Level</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static AIActRiskLevel getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			AIActRiskLevel result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>AI Act Risk Level</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static AIActRiskLevel get(int value) {
		switch (value) {
			case UNKNOWN_VALUE: return UNKNOWN;
			case MINIMAL_OR_NO_RISK_VALUE: return MINIMAL_OR_NO_RISK;
			case LIMITED_RISK_VALUE: return LIMITED_RISK;
			case HIGH_RISK_VALUE: return HIGH_RISK;
			case UNACCEPTABLE_RISK_VALUE: return UNACCEPTABLE_RISK;
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
	private AIActRiskLevel(int value, String name, String literal) {
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
	
} //AIActRiskLevel
