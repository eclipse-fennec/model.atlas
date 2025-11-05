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
package org.eclipse.fennec.model.atlas.policy.gdpr;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>Legal Basis</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * Enumeration of the legal bases for processing personal data according to Art. 6 GDPR.
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.model.atlas.policy.gdpr.GDPRPackage#getLegalBasis()
 * @model
 * @generated
 */
@ProviderType
public enum LegalBasis implements Enumerator {
	/**
	 * The '<em><b>NOT APPLICABLE</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * No personal data is being processed.
	 * <!-- end-model-doc -->
	 * @see #NOT_APPLICABLE_VALUE
	 * @generated
	 * @ordered
	 */
	NOT_APPLICABLE(0, "NOT_APPLICABLE", "NOT_APPLICABLE"),

	/**
	 * The '<em><b>CONSENT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Art. 6(1)(a): The data subject has given consent.
	 * <!-- end-model-doc -->
	 * @see #CONSENT_VALUE
	 * @generated
	 * @ordered
	 */
	CONSENT(1, "CONSENT", "CONSENT"),

	/**
	 * The '<em><b>CONTRACT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Art. 6(1)(b): Processing is necessary for the performance of a contract.
	 * <!-- end-model-doc -->
	 * @see #CONTRACT_VALUE
	 * @generated
	 * @ordered
	 */
	CONTRACT(2, "CONTRACT", "CONTRACT"),

	/**
	 * The '<em><b>LEGAL OBLIGATION</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Art. 6(1)(c): Processing is necessary for compliance with a legal obligation.
	 * <!-- end-model-doc -->
	 * @see #LEGAL_OBLIGATION_VALUE
	 * @generated
	 * @ordered
	 */
	LEGAL_OBLIGATION(3, "LEGAL_OBLIGATION", "LEGAL_OBLIGATION"),

	/**
	 * The '<em><b>VITAL INTEREST</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Art. 6(1)(d): Processing is necessary to protect the vital interests of the data subject.
	 * <!-- end-model-doc -->
	 * @see #VITAL_INTEREST_VALUE
	 * @generated
	 * @ordered
	 */
	VITAL_INTEREST(4, "VITAL_INTEREST", "VITAL_INTEREST"),

	/**
	 * The '<em><b>PUBLIC TASK</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Art. 6(1)(e): Processing is necessary for the performance of a task carried out in the public interest.
	 * <!-- end-model-doc -->
	 * @see #PUBLIC_TASK_VALUE
	 * @generated
	 * @ordered
	 */
	PUBLIC_TASK(5, "PUBLIC_TASK", "PUBLIC_TASK"),

	/**
	 * The '<em><b>LEGITIMATE INTEREST</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Art. 6(1)(f): Processing is necessary for the purposes of the legitimate interests pursued by the controller.
	 * <!-- end-model-doc -->
	 * @see #LEGITIMATE_INTEREST_VALUE
	 * @generated
	 * @ordered
	 */
	LEGITIMATE_INTEREST(6, "LEGITIMATE_INTEREST", "LEGITIMATE_INTEREST");

	/**
	 * The '<em><b>NOT APPLICABLE</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * No personal data is being processed.
	 * <!-- end-model-doc -->
	 * @see #NOT_APPLICABLE
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int NOT_APPLICABLE_VALUE = 0;

	/**
	 * The '<em><b>CONSENT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Art. 6(1)(a): The data subject has given consent.
	 * <!-- end-model-doc -->
	 * @see #CONSENT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int CONSENT_VALUE = 1;

	/**
	 * The '<em><b>CONTRACT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Art. 6(1)(b): Processing is necessary for the performance of a contract.
	 * <!-- end-model-doc -->
	 * @see #CONTRACT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int CONTRACT_VALUE = 2;

	/**
	 * The '<em><b>LEGAL OBLIGATION</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Art. 6(1)(c): Processing is necessary for compliance with a legal obligation.
	 * <!-- end-model-doc -->
	 * @see #LEGAL_OBLIGATION
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int LEGAL_OBLIGATION_VALUE = 3;

	/**
	 * The '<em><b>VITAL INTEREST</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Art. 6(1)(d): Processing is necessary to protect the vital interests of the data subject.
	 * <!-- end-model-doc -->
	 * @see #VITAL_INTEREST
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int VITAL_INTEREST_VALUE = 4;

	/**
	 * The '<em><b>PUBLIC TASK</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Art. 6(1)(e): Processing is necessary for the performance of a task carried out in the public interest.
	 * <!-- end-model-doc -->
	 * @see #PUBLIC_TASK
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int PUBLIC_TASK_VALUE = 5;

	/**
	 * The '<em><b>LEGITIMATE INTEREST</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Art. 6(1)(f): Processing is necessary for the purposes of the legitimate interests pursued by the controller.
	 * <!-- end-model-doc -->
	 * @see #LEGITIMATE_INTEREST
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int LEGITIMATE_INTEREST_VALUE = 6;

	/**
	 * An array of all the '<em><b>Legal Basis</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final LegalBasis[] VALUES_ARRAY =
		new LegalBasis[] {
			NOT_APPLICABLE,
			CONSENT,
			CONTRACT,
			LEGAL_OBLIGATION,
			VITAL_INTEREST,
			PUBLIC_TASK,
			LEGITIMATE_INTEREST,
		};

	/**
	 * A public read-only list of all the '<em><b>Legal Basis</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<LegalBasis> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Legal Basis</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static LegalBasis get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			LegalBasis result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Legal Basis</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static LegalBasis getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			LegalBasis result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Legal Basis</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static LegalBasis get(int value) {
		switch (value) {
			case NOT_APPLICABLE_VALUE: return NOT_APPLICABLE;
			case CONSENT_VALUE: return CONSENT;
			case CONTRACT_VALUE: return CONTRACT;
			case LEGAL_OBLIGATION_VALUE: return LEGAL_OBLIGATION;
			case VITAL_INTEREST_VALUE: return VITAL_INTEREST;
			case PUBLIC_TASK_VALUE: return PUBLIC_TASK;
			case LEGITIMATE_INTEREST_VALUE: return LEGITIMATE_INTEREST;
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
	private LegalBasis(int value, String name, String literal) {
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
	
} //LegalBasis
