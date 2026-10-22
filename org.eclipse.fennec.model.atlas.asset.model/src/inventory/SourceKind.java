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
 *      Data In Motion - initial API and implementation
 */
package inventory;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>Source Kind</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * What a SourceRef points at.
 * <!-- end-model-doc -->
 * @see inventory.InventoryPackage#getSourceKind()
 * @model
 * @generated
 */
@ProviderType
public enum SourceKind implements Enumerator {
	/**
	 * The '<em><b>SERVICE SPEC</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A service specification.
	 * <!-- end-model-doc -->
	 * @see #SERVICE_SPEC_VALUE
	 * @generated
	 * @ordered
	 */
	SERVICE_SPEC(0, "SERVICE_SPEC", "SERVICE_SPEC"),

	/**
	 * The '<em><b>DEPLOYMENT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A deployment or service binding.
	 * <!-- end-model-doc -->
	 * @see #DEPLOYMENT_VALUE
	 * @generated
	 * @ordered
	 */
	DEPLOYMENT(1, "DEPLOYMENT", "DEPLOYMENT"),

	/**
	 * The '<em><b>EPACKAGE</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * An EPackage, by nsURI.
	 * <!-- end-model-doc -->
	 * @see #EPACKAGE_VALUE
	 * @generated
	 * @ordered
	 */
	EPACKAGE(2, "EPACKAGE", "EPACKAGE"),

	/**
	 * The '<em><b>TRANSFORMATION</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A transformation unit.
	 * <!-- end-model-doc -->
	 * @see #TRANSFORMATION_VALUE
	 * @generated
	 * @ordered
	 */
	TRANSFORMATION(3, "TRANSFORMATION", "TRANSFORMATION"),

	/**
	 * The '<em><b>REPORT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A compliance report.
	 * <!-- end-model-doc -->
	 * @see #REPORT_VALUE
	 * @generated
	 * @ordered
	 */
	REPORT(4, "REPORT", "REPORT"),

	/**
	 * The '<em><b>DOCUMENT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A document, e.g. a policy.
	 * <!-- end-model-doc -->
	 * @see #DOCUMENT_VALUE
	 * @generated
	 * @ordered
	 */
	DOCUMENT(5, "DOCUMENT", "DOCUMENT"),

	/**
	 * The '<em><b>DATASET</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A data set, e.g. a DCAT dataset.
	 * <!-- end-model-doc -->
	 * @see #DATASET_VALUE
	 * @generated
	 * @ordered
	 */
	DATASET(6, "DATASET", "DATASET"),

	/**
	 * The '<em><b>OTHER</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Anything else.
	 * <!-- end-model-doc -->
	 * @see #OTHER_VALUE
	 * @generated
	 * @ordered
	 */
	OTHER(7, "OTHER", "OTHER");

	/**
	 * The '<em><b>SERVICE SPEC</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A service specification.
	 * <!-- end-model-doc -->
	 * @see #SERVICE_SPEC
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int SERVICE_SPEC_VALUE = 0;

	/**
	 * The '<em><b>DEPLOYMENT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A deployment or service binding.
	 * <!-- end-model-doc -->
	 * @see #DEPLOYMENT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int DEPLOYMENT_VALUE = 1;

	/**
	 * The '<em><b>EPACKAGE</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * An EPackage, by nsURI.
	 * <!-- end-model-doc -->
	 * @see #EPACKAGE
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int EPACKAGE_VALUE = 2;

	/**
	 * The '<em><b>TRANSFORMATION</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A transformation unit.
	 * <!-- end-model-doc -->
	 * @see #TRANSFORMATION
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int TRANSFORMATION_VALUE = 3;

	/**
	 * The '<em><b>REPORT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A compliance report.
	 * <!-- end-model-doc -->
	 * @see #REPORT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int REPORT_VALUE = 4;

	/**
	 * The '<em><b>DOCUMENT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A document, e.g. a policy.
	 * <!-- end-model-doc -->
	 * @see #DOCUMENT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int DOCUMENT_VALUE = 5;

	/**
	 * The '<em><b>DATASET</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A data set, e.g. a DCAT dataset.
	 * <!-- end-model-doc -->
	 * @see #DATASET
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int DATASET_VALUE = 6;

	/**
	 * The '<em><b>OTHER</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Anything else.
	 * <!-- end-model-doc -->
	 * @see #OTHER
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int OTHER_VALUE = 7;

	/**
	 * An array of all the '<em><b>Source Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final SourceKind[] VALUES_ARRAY =
		new SourceKind[] {
			SERVICE_SPEC,
			DEPLOYMENT,
			EPACKAGE,
			TRANSFORMATION,
			REPORT,
			DOCUMENT,
			DATASET,
			OTHER,
		};

	/**
	 * A public read-only list of all the '<em><b>Source Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<SourceKind> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Source Kind</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static SourceKind get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			SourceKind result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Source Kind</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static SourceKind getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			SourceKind result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Source Kind</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static SourceKind get(int value) {
		switch (value) {
			case SERVICE_SPEC_VALUE: return SERVICE_SPEC;
			case DEPLOYMENT_VALUE: return DEPLOYMENT;
			case EPACKAGE_VALUE: return EPACKAGE;
			case TRANSFORMATION_VALUE: return TRANSFORMATION;
			case REPORT_VALUE: return REPORT;
			case DOCUMENT_VALUE: return DOCUMENT;
			case DATASET_VALUE: return DATASET;
			case OTHER_VALUE: return OTHER;
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
	private SourceKind(int value, String name, String literal) {
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
	
} //SourceKind
