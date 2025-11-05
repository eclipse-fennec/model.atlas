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
package org.gecko.mac.mgmt.management;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>Package Status</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * Package lifecycle status
 * <!-- end-model-doc -->
 * @see org.gecko.mac.mgmt.management.ManagementPackage#getPackageStatus()
 * @model
 * @generated
 */
@ProviderType
public enum PackageStatus implements Enumerator {
	/**
	 * The '<em><b>DRAFT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Package is in draft state
	 * <!-- end-model-doc -->
	 * @see #DRAFT_VALUE
	 * @generated
	 * @ordered
	 */
	DRAFT(0, "DRAFT", "DRAFT"),

	/**
	 * The '<em><b>APPROVED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Package has been approved
	 * <!-- end-model-doc -->
	 * @see #APPROVED_VALUE
	 * @generated
	 * @ordered
	 */
	APPROVED(1, "APPROVED", "APPROVED"),

	/**
	 * The '<em><b>DEPLOYED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Package has been deployed
	 * <!-- end-model-doc -->
	 * @see #DEPLOYED_VALUE
	 * @generated
	 * @ordered
	 */
	DEPLOYED(2, "DEPLOYED", "DEPLOYED"),

	/**
	 * The '<em><b>ARCHIVED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Package has been archived
	 * <!-- end-model-doc -->
	 * @see #ARCHIVED_VALUE
	 * @generated
	 * @ordered
	 */
	ARCHIVED(3, "ARCHIVED", "ARCHIVED");

	/**
	 * The '<em><b>DRAFT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Package is in draft state
	 * <!-- end-model-doc -->
	 * @see #DRAFT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int DRAFT_VALUE = 0;

	/**
	 * The '<em><b>APPROVED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Package has been approved
	 * <!-- end-model-doc -->
	 * @see #APPROVED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int APPROVED_VALUE = 1;

	/**
	 * The '<em><b>DEPLOYED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Package has been deployed
	 * <!-- end-model-doc -->
	 * @see #DEPLOYED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int DEPLOYED_VALUE = 2;

	/**
	 * The '<em><b>ARCHIVED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Package has been archived
	 * <!-- end-model-doc -->
	 * @see #ARCHIVED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int ARCHIVED_VALUE = 3;

	/**
	 * An array of all the '<em><b>Package Status</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final PackageStatus[] VALUES_ARRAY =
		new PackageStatus[] {
			DRAFT,
			APPROVED,
			DEPLOYED,
			ARCHIVED,
		};

	/**
	 * A public read-only list of all the '<em><b>Package Status</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<PackageStatus> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Package Status</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static PackageStatus get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			PackageStatus result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Package Status</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static PackageStatus getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			PackageStatus result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Package Status</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static PackageStatus get(int value) {
		switch (value) {
			case DRAFT_VALUE: return DRAFT;
			case APPROVED_VALUE: return APPROVED;
			case DEPLOYED_VALUE: return DEPLOYED;
			case ARCHIVED_VALUE: return ARCHIVED;
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
	private PackageStatus(int value, String name, String literal) {
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
	
} //PackageStatus
