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
 * A representation of the literals of the enumeration '<em><b>Policy Type</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * Enumeration of supported compliance policies and regulations.
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getPolicyType()
 * @model
 * @generated
 */
@ProviderType
public enum PolicyType implements Enumerator {
	/**
	 * The '<em><b>GDPR</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * General Data Protection Regulation (Datenschutz-Grundverordnung). Checks for personal identifiable information (PII).
	 * <!-- end-model-doc -->
	 * @see #GDPR_VALUE
	 * @generated
	 * @ordered
	 */
	GDPR(0, "GDPR", "GDPR"),

	/**
	 * The '<em><b>EU AI ACT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The European Union's Artificial Intelligence Act. Used for risk assessment of AI-generated models and systems.
	 * <!-- end-model-doc -->
	 * @see #EU_AI_ACT_VALUE
	 * @generated
	 * @ordered
	 */
	EU_AI_ACT(1, "EU_AI_ACT", "EU_AI_ACT"),

	/**
	 * The '<em><b>HIPAA</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Health Insurance Portability and Accountability Act. Checks for protected health information (PHI) in the US healthcare context.
	 * <!-- end-model-doc -->
	 * @see #HIPAA_VALUE
	 * @generated
	 * @ordered
	 */
	HIPAA(2, "HIPAA", "HIPAA"),

	/**
	 * The '<em><b>ISO 27001</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * International standard for Information Security Management Systems (ISMS). Checks against security best practices.
	 * <!-- end-model-doc -->
	 * @see #ISO_27001_VALUE
	 * @generated
	 * @ordered
	 */
	ISO_27001(3, "ISO_27001", "ISO_27001"),

	/**
	 * The '<em><b>CRA</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Cyber Resilience Act. Checks for security-by-design principles and vulnerability management for products with digital elements.
	 * <!-- end-model-doc -->
	 * @see #CRA_VALUE
	 * @generated
	 * @ordered
	 */
	CRA(4, "CRA", "CRA"),

	/**
	 * The '<em><b>MDR</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Medical Device Regulation. Checks for compliance with regulations for medical devices, including software as a medical device.
	 * <!-- end-model-doc -->
	 * @see #MDR_VALUE
	 * @generated
	 * @ordered
	 */
	MDR(5, "MDR", "MDR"),

	/**
	 * The '<em><b>ISO 9001</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * International standard for Quality Management Systems (QMS). Checks for process and documentation quality.
	 * <!-- end-model-doc -->
	 * @see #ISO_9001_VALUE
	 * @generated
	 * @ordered
	 */
	ISO_9001(6, "ISO_9001", "ISO_9001"),

	/**
	 * The '<em><b>KRITIS</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Regulation for Critical Infrastructures (Verordnung zur Bestimmung Kritischer Infrastrukturen). Checks for resilience and security requirements.
	 * <!-- end-model-doc -->
	 * @see #KRITIS_VALUE
	 * @generated
	 * @ordered
	 */
	KRITIS(7, "KRITIS", "KRITIS"),

	/**
	 * The '<em><b>DATA QUALITY</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Internal policy for checking data quality, such as format, range, and consistency.
	 * <!-- end-model-doc -->
	 * @see #DATA_QUALITY_VALUE
	 * @generated
	 * @ordered
	 */
	DATA_QUALITY(8, "DATA_QUALITY", "DATA_QUALITY"),

	/**
	 * The '<em><b>OPEN DATA</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Internal policy for checking data for open data requirements.
	 * <!-- end-model-doc -->
	 * @see #OPEN_DATA_VALUE
	 * @generated
	 * @ordered
	 */
	OPEN_DATA(9, "OPEN_DATA", "OPEN_DATA"),

	/**
	 * The '<em><b>DINSPEC91377</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Internal policy for checking data for open data requirements.
	 * <!-- end-model-doc -->
	 * @see #DINSPEC91377_VALUE
	 * @generated
	 * @ordered
	 */
	DINSPEC91377(10, "DINSPEC91377", "DINSPEC91377");

	/**
	 * The '<em><b>GDPR</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * General Data Protection Regulation (Datenschutz-Grundverordnung). Checks for personal identifiable information (PII).
	 * <!-- end-model-doc -->
	 * @see #GDPR
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int GDPR_VALUE = 0;

	/**
	 * The '<em><b>EU AI ACT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The European Union's Artificial Intelligence Act. Used for risk assessment of AI-generated models and systems.
	 * <!-- end-model-doc -->
	 * @see #EU_AI_ACT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int EU_AI_ACT_VALUE = 1;

	/**
	 * The '<em><b>HIPAA</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Health Insurance Portability and Accountability Act. Checks for protected health information (PHI) in the US healthcare context.
	 * <!-- end-model-doc -->
	 * @see #HIPAA
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int HIPAA_VALUE = 2;

	/**
	 * The '<em><b>ISO 27001</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * International standard for Information Security Management Systems (ISMS). Checks against security best practices.
	 * <!-- end-model-doc -->
	 * @see #ISO_27001
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int ISO_27001_VALUE = 3;

	/**
	 * The '<em><b>CRA</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Cyber Resilience Act. Checks for security-by-design principles and vulnerability management for products with digital elements.
	 * <!-- end-model-doc -->
	 * @see #CRA
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int CRA_VALUE = 4;

	/**
	 * The '<em><b>MDR</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Medical Device Regulation. Checks for compliance with regulations for medical devices, including software as a medical device.
	 * <!-- end-model-doc -->
	 * @see #MDR
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int MDR_VALUE = 5;

	/**
	 * The '<em><b>ISO 9001</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * International standard for Quality Management Systems (QMS). Checks for process and documentation quality.
	 * <!-- end-model-doc -->
	 * @see #ISO_9001
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int ISO_9001_VALUE = 6;

	/**
	 * The '<em><b>KRITIS</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Regulation for Critical Infrastructures (Verordnung zur Bestimmung Kritischer Infrastrukturen). Checks for resilience and security requirements.
	 * <!-- end-model-doc -->
	 * @see #KRITIS
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int KRITIS_VALUE = 7;

	/**
	 * The '<em><b>DATA QUALITY</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Internal policy for checking data quality, such as format, range, and consistency.
	 * <!-- end-model-doc -->
	 * @see #DATA_QUALITY
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int DATA_QUALITY_VALUE = 8;

	/**
	 * The '<em><b>OPEN DATA</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Internal policy for checking data for open data requirements.
	 * <!-- end-model-doc -->
	 * @see #OPEN_DATA
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int OPEN_DATA_VALUE = 9;

	/**
	 * The '<em><b>DINSPEC91377</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Internal policy for checking data for open data requirements.
	 * <!-- end-model-doc -->
	 * @see #DINSPEC91377
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int DINSPEC91377_VALUE = 10;

	/**
	 * An array of all the '<em><b>Policy Type</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final PolicyType[] VALUES_ARRAY =
		new PolicyType[] {
			GDPR,
			EU_AI_ACT,
			HIPAA,
			ISO_27001,
			CRA,
			MDR,
			ISO_9001,
			KRITIS,
			DATA_QUALITY,
			OPEN_DATA,
			DINSPEC91377,
		};

	/**
	 * A public read-only list of all the '<em><b>Policy Type</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<PolicyType> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Policy Type</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static PolicyType get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			PolicyType result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Policy Type</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static PolicyType getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			PolicyType result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Policy Type</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static PolicyType get(int value) {
		switch (value) {
			case GDPR_VALUE: return GDPR;
			case EU_AI_ACT_VALUE: return EU_AI_ACT;
			case HIPAA_VALUE: return HIPAA;
			case ISO_27001_VALUE: return ISO_27001;
			case CRA_VALUE: return CRA;
			case MDR_VALUE: return MDR;
			case ISO_9001_VALUE: return ISO_9001;
			case KRITIS_VALUE: return KRITIS;
			case DATA_QUALITY_VALUE: return DATA_QUALITY;
			case OPEN_DATA_VALUE: return OPEN_DATA;
			case DINSPEC91377_VALUE: return DINSPEC91377;
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
	private PolicyType(int value, String name, String literal) {
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
	
} //PolicyType
