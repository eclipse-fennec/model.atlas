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
 * A representation of the literals of the enumeration '<em><b>Write Outcome</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * What a write did. Refusals are not outcomes; they are raised as RegistryAccessException.
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getWriteOutcome()
 * @model
 * @generated
 */
@ProviderType
public enum WriteOutcome implements Enumerator {
	/**
	 * The '<em><b>CREATED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A new object was stored.
	 * <!-- end-model-doc -->
	 * @see #CREATED_VALUE
	 * @generated
	 * @ordered
	 */
	CREATED(0, "CREATED", "CREATED"),

	/**
	 * The '<em><b>UPDATED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * An existing object's content or metadata was replaced.
	 * <!-- end-model-doc -->
	 * @see #UPDATED_VALUE
	 * @generated
	 * @ordered
	 */
	UPDATED(1, "UPDATED", "UPDATED"),

	/**
	 * The '<em><b>UNCHANGED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The content sent equals the stored content; nothing was written.
	 * <!-- end-model-doc -->
	 * @see #UNCHANGED_VALUE
	 * @generated
	 * @ordered
	 */
	UNCHANGED(2, "UNCHANGED", "UNCHANGED"),

	/**
	 * The '<em><b>SKIPPED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The target already held the object and ConflictPolicy#SKIP left it alone.
	 * <!-- end-model-doc -->
	 * @see #SKIPPED_VALUE
	 * @generated
	 * @ordered
	 */
	SKIPPED(3, "SKIPPED", "SKIPPED"),

	/**
	 * The '<em><b>DELETED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The object was deleted from the stage.
	 * <!-- end-model-doc -->
	 * @see #DELETED_VALUE
	 * @generated
	 * @ordered
	 */
	DELETED(4, "DELETED", "DELETED"),

	/**
	 * The '<em><b>TRANSITIONED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The object moved (or was copied) to the target stage.
	 * <!-- end-model-doc -->
	 * @see #TRANSITIONED_VALUE
	 * @generated
	 * @ordered
	 */
	TRANSITIONED(5, "TRANSITIONED", "TRANSITIONED"),

	/**
	 * The '<em><b>ALREADY IN TARGET</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The object was not in the source stage but already is in the target stage: a retried transition. The result carries the target stage's metadata.
	 * <!-- end-model-doc -->
	 * @see #ALREADY_IN_TARGET_VALUE
	 * @generated
	 * @ordered
	 */
	ALREADY_IN_TARGET(6, "ALREADY_IN_TARGET", "ALREADY_IN_TARGET"),

	/**
	 * The '<em><b>NOT FOUND</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The object the operation addressed does not exist; nothing was written.
	 * <!-- end-model-doc -->
	 * @see #NOT_FOUND_VALUE
	 * @generated
	 * @ordered
	 */
	NOT_FOUND(7, "NOT_FOUND", "NOT_FOUND");

	/**
	 * The '<em><b>CREATED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A new object was stored.
	 * <!-- end-model-doc -->
	 * @see #CREATED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int CREATED_VALUE = 0;

	/**
	 * The '<em><b>UPDATED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * An existing object's content or metadata was replaced.
	 * <!-- end-model-doc -->
	 * @see #UPDATED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int UPDATED_VALUE = 1;

	/**
	 * The '<em><b>UNCHANGED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The content sent equals the stored content; nothing was written.
	 * <!-- end-model-doc -->
	 * @see #UNCHANGED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int UNCHANGED_VALUE = 2;

	/**
	 * The '<em><b>SKIPPED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The target already held the object and ConflictPolicy#SKIP left it alone.
	 * <!-- end-model-doc -->
	 * @see #SKIPPED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int SKIPPED_VALUE = 3;

	/**
	 * The '<em><b>DELETED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The object was deleted from the stage.
	 * <!-- end-model-doc -->
	 * @see #DELETED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int DELETED_VALUE = 4;

	/**
	 * The '<em><b>TRANSITIONED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The object moved (or was copied) to the target stage.
	 * <!-- end-model-doc -->
	 * @see #TRANSITIONED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int TRANSITIONED_VALUE = 5;

	/**
	 * The '<em><b>ALREADY IN TARGET</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The object was not in the source stage but already is in the target stage: a retried transition. The result carries the target stage's metadata.
	 * <!-- end-model-doc -->
	 * @see #ALREADY_IN_TARGET
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int ALREADY_IN_TARGET_VALUE = 6;

	/**
	 * The '<em><b>NOT FOUND</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The object the operation addressed does not exist; nothing was written.
	 * <!-- end-model-doc -->
	 * @see #NOT_FOUND
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int NOT_FOUND_VALUE = 7;

	/**
	 * An array of all the '<em><b>Write Outcome</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final WriteOutcome[] VALUES_ARRAY =
		new WriteOutcome[] {
			CREATED,
			UPDATED,
			UNCHANGED,
			SKIPPED,
			DELETED,
			TRANSITIONED,
			ALREADY_IN_TARGET,
			NOT_FOUND,
		};

	/**
	 * A public read-only list of all the '<em><b>Write Outcome</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<WriteOutcome> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Write Outcome</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static WriteOutcome get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			WriteOutcome result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Write Outcome</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static WriteOutcome getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			WriteOutcome result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Write Outcome</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static WriteOutcome get(int value) {
		switch (value) {
			case CREATED_VALUE: return CREATED;
			case UPDATED_VALUE: return UPDATED;
			case UNCHANGED_VALUE: return UNCHANGED;
			case SKIPPED_VALUE: return SKIPPED;
			case DELETED_VALUE: return DELETED;
			case TRANSITIONED_VALUE: return TRANSITIONED;
			case ALREADY_IN_TARGET_VALUE: return ALREADY_IN_TARGET;
			case NOT_FOUND_VALUE: return NOT_FOUND;
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
	private WriteOutcome(int value, String name, String literal) {
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
	
} //WriteOutcome
