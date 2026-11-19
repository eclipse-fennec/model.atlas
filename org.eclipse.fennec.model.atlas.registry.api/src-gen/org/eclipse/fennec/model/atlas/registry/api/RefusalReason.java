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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.registry.api;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>Refusal Reason</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * Why the RegistryWriter refused a request, in terms a front end can translate into its own protocol (the REST layer maps each to a status code).
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getRefusalReason()
 * @model
 * @generated
 */
@ProviderType
public enum RefusalReason implements Enumerator {
	/**
	 * The '<em><b>INVALID</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The request itself is malformed: a missing or mismatching identity, an invalid version.
	 * <!-- end-model-doc -->
	 * @see #INVALID_VALUE
	 * @generated
	 * @ordered
	 */
	INVALID(0, "INVALID", "INVALID"),

	/**
	 * The '<em><b>INCOMPATIBLE TYPE</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The content's type is not one the target registry accepts.
	 * <!-- end-model-doc -->
	 * @see #INCOMPATIBLE_TYPE_VALUE
	 * @generated
	 * @ordered
	 */
	INCOMPATIBLE_TYPE(1, "INCOMPATIBLE_TYPE", "INCOMPATIBLE_TYPE"),

	/**
	 * The '<em><b>DERIVED CONTENT</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The content's type is derived content, which only the Atlas itself writes.
	 * <!-- end-model-doc -->
	 * @see #DERIVED_CONTENT_VALUE
	 * @generated
	 * @ordered
	 */
	DERIVED_CONTENT(2, "DERIVED_CONTENT", "DERIVED_CONTENT"),

	/**
	 * The '<em><b>UNKNOWN SCOPE</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The addressed scope does not exist.
	 * <!-- end-model-doc -->
	 * @see #UNKNOWN_SCOPE_VALUE
	 * @generated
	 * @ordered
	 */
	UNKNOWN_SCOPE(3, "UNKNOWN_SCOPE", "UNKNOWN_SCOPE"),

	/**
	 * The '<em><b>UNKNOWN REGISTRY</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The addressed registry does not exist or is not configured.
	 * <!-- end-model-doc -->
	 * @see #UNKNOWN_REGISTRY_VALUE
	 * @generated
	 * @ordered
	 */
	UNKNOWN_REGISTRY(4, "UNKNOWN_REGISTRY", "UNKNOWN_REGISTRY"),

	/**
	 * The '<em><b>NO SCHEMA REGISTRY</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The addressed scope has no schema registry.
	 * <!-- end-model-doc -->
	 * @see #NO_SCHEMA_REGISTRY_VALUE
	 * @generated
	 * @ordered
	 */
	NO_SCHEMA_REGISTRY(5, "NO_SCHEMA_REGISTRY", "NO_SCHEMA_REGISTRY"),

	/**
	 * The '<em><b>ALREADY EXISTS</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A create found its target taken and the conflict policy says to fail.
	 * <!-- end-model-doc -->
	 * @see #ALREADY_EXISTS_VALUE
	 * @generated
	 * @ordered
	 */
	ALREADY_EXISTS(6, "ALREADY_EXISTS", "ALREADY_EXISTS"),

	/**
	 * The '<em><b>INHERITED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The object is inherited from another scope and must be changed there.
	 * <!-- end-model-doc -->
	 * @see #INHERITED_VALUE
	 * @generated
	 * @ordered
	 */
	INHERITED(7, "INHERITED", "INHERITED"),

	/**
	 * The '<em><b>READ ONLY</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The object is read-only where it was addressed.
	 * <!-- end-model-doc -->
	 * @see #READ_ONLY_VALUE
	 * @generated
	 * @ordered
	 */
	READ_ONLY(8, "READ_ONLY", "READ_ONLY"),

	/**
	 * The '<em><b>PRECONDITION FAILED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The caller's Precondition does not hold for the current object.
	 * <!-- end-model-doc -->
	 * @see #PRECONDITION_FAILED_VALUE
	 * @generated
	 * @ordered
	 */
	PRECONDITION_FAILED(9, "PRECONDITION_FAILED", "PRECONDITION_FAILED"),

	/**
	 * The '<em><b>INTERRUPTED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The calling thread was interrupted while the write was in flight.
	 * <!-- end-model-doc -->
	 * @see #INTERRUPTED_VALUE
	 * @generated
	 * @ordered
	 */
	INTERRUPTED(10, "INTERRUPTED", "INTERRUPTED");

	/**
	 * The '<em><b>INVALID</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The request itself is malformed: a missing or mismatching identity, an invalid version.
	 * <!-- end-model-doc -->
	 * @see #INVALID
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int INVALID_VALUE = 0;

	/**
	 * The '<em><b>INCOMPATIBLE TYPE</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The content's type is not one the target registry accepts.
	 * <!-- end-model-doc -->
	 * @see #INCOMPATIBLE_TYPE
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int INCOMPATIBLE_TYPE_VALUE = 1;

	/**
	 * The '<em><b>DERIVED CONTENT</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The content's type is derived content, which only the Atlas itself writes.
	 * <!-- end-model-doc -->
	 * @see #DERIVED_CONTENT
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int DERIVED_CONTENT_VALUE = 2;

	/**
	 * The '<em><b>UNKNOWN SCOPE</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The addressed scope does not exist.
	 * <!-- end-model-doc -->
	 * @see #UNKNOWN_SCOPE
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int UNKNOWN_SCOPE_VALUE = 3;

	/**
	 * The '<em><b>UNKNOWN REGISTRY</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The addressed registry does not exist or is not configured.
	 * <!-- end-model-doc -->
	 * @see #UNKNOWN_REGISTRY
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int UNKNOWN_REGISTRY_VALUE = 4;

	/**
	 * The '<em><b>NO SCHEMA REGISTRY</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The addressed scope has no schema registry.
	 * <!-- end-model-doc -->
	 * @see #NO_SCHEMA_REGISTRY
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int NO_SCHEMA_REGISTRY_VALUE = 5;

	/**
	 * The '<em><b>ALREADY EXISTS</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A create found its target taken and the conflict policy says to fail.
	 * <!-- end-model-doc -->
	 * @see #ALREADY_EXISTS
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int ALREADY_EXISTS_VALUE = 6;

	/**
	 * The '<em><b>INHERITED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The object is inherited from another scope and must be changed there.
	 * <!-- end-model-doc -->
	 * @see #INHERITED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int INHERITED_VALUE = 7;

	/**
	 * The '<em><b>READ ONLY</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The object is read-only where it was addressed.
	 * <!-- end-model-doc -->
	 * @see #READ_ONLY
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int READ_ONLY_VALUE = 8;

	/**
	 * The '<em><b>PRECONDITION FAILED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The caller's Precondition does not hold for the current object.
	 * <!-- end-model-doc -->
	 * @see #PRECONDITION_FAILED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int PRECONDITION_FAILED_VALUE = 9;

	/**
	 * The '<em><b>INTERRUPTED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The calling thread was interrupted while the write was in flight.
	 * <!-- end-model-doc -->
	 * @see #INTERRUPTED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int INTERRUPTED_VALUE = 10;

	/**
	 * An array of all the '<em><b>Refusal Reason</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final RefusalReason[] VALUES_ARRAY =
		new RefusalReason[] {
			INVALID,
			INCOMPATIBLE_TYPE,
			DERIVED_CONTENT,
			UNKNOWN_SCOPE,
			UNKNOWN_REGISTRY,
			NO_SCHEMA_REGISTRY,
			ALREADY_EXISTS,
			INHERITED,
			READ_ONLY,
			PRECONDITION_FAILED,
			INTERRUPTED,
		};

	/**
	 * A public read-only list of all the '<em><b>Refusal Reason</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<RefusalReason> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Refusal Reason</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static RefusalReason get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			RefusalReason result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Refusal Reason</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static RefusalReason getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			RefusalReason result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Refusal Reason</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static RefusalReason get(int value) {
		switch (value) {
			case INVALID_VALUE: return INVALID;
			case INCOMPATIBLE_TYPE_VALUE: return INCOMPATIBLE_TYPE;
			case DERIVED_CONTENT_VALUE: return DERIVED_CONTENT;
			case UNKNOWN_SCOPE_VALUE: return UNKNOWN_SCOPE;
			case UNKNOWN_REGISTRY_VALUE: return UNKNOWN_REGISTRY;
			case NO_SCHEMA_REGISTRY_VALUE: return NO_SCHEMA_REGISTRY;
			case ALREADY_EXISTS_VALUE: return ALREADY_EXISTS;
			case INHERITED_VALUE: return INHERITED;
			case READ_ONLY_VALUE: return READ_ONLY;
			case PRECONDITION_FAILED_VALUE: return PRECONDITION_FAILED;
			case INTERRUPTED_VALUE: return INTERRUPTED;
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
	private RefusalReason(int value, String name, String literal) {
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
	
} //RefusalReason
