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
 * A representation of the literals of the enumeration '<em><b>Asset Relation Kind</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * How one asset relates to another; used to inherit protection needs.
 * <!-- end-model-doc -->
 * @see inventory.InventoryPackage#getAssetRelationKind()
 * @model
 * @generated
 */
@ProviderType
public enum AssetRelationKind implements Enumerator {
	/**
	 * The '<em><b>DEPENDS ON</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The asset needs the target to work.
	 * <!-- end-model-doc -->
	 * @see #DEPENDS_ON_VALUE
	 * @generated
	 * @ordered
	 */
	DEPENDS_ON(0, "DEPENDS_ON", "DEPENDS_ON"),

	/**
	 * The '<em><b>RUNS ON</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The asset runs on the target, e.g. a container on a host.
	 * <!-- end-model-doc -->
	 * @see #RUNS_ON_VALUE
	 * @generated
	 * @ordered
	 */
	RUNS_ON(1, "RUNS_ON", "RUNS_ON"),

	/**
	 * The '<em><b>CONNECTS TO</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The asset communicates with the target.
	 * <!-- end-model-doc -->
	 * @see #CONNECTS_TO_VALUE
	 * @generated
	 * @ordered
	 */
	CONNECTS_TO(2, "CONNECTS_TO", "CONNECTS_TO"),

	/**
	 * The '<em><b>PART OF</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The asset is part of the target.
	 * <!-- end-model-doc -->
	 * @see #PART_OF_VALUE
	 * @generated
	 * @ordered
	 */
	PART_OF(3, "PART_OF", "PART_OF");

	/**
	 * The '<em><b>DEPENDS ON</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The asset needs the target to work.
	 * <!-- end-model-doc -->
	 * @see #DEPENDS_ON
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int DEPENDS_ON_VALUE = 0;

	/**
	 * The '<em><b>RUNS ON</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The asset runs on the target, e.g. a container on a host.
	 * <!-- end-model-doc -->
	 * @see #RUNS_ON
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int RUNS_ON_VALUE = 1;

	/**
	 * The '<em><b>CONNECTS TO</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The asset communicates with the target.
	 * <!-- end-model-doc -->
	 * @see #CONNECTS_TO
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int CONNECTS_TO_VALUE = 2;

	/**
	 * The '<em><b>PART OF</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The asset is part of the target.
	 * <!-- end-model-doc -->
	 * @see #PART_OF
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int PART_OF_VALUE = 3;

	/**
	 * An array of all the '<em><b>Asset Relation Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final AssetRelationKind[] VALUES_ARRAY =
		new AssetRelationKind[] {
			DEPENDS_ON,
			RUNS_ON,
			CONNECTS_TO,
			PART_OF,
		};

	/**
	 * A public read-only list of all the '<em><b>Asset Relation Kind</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<AssetRelationKind> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Asset Relation Kind</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static AssetRelationKind get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			AssetRelationKind result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Asset Relation Kind</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static AssetRelationKind getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			AssetRelationKind result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Asset Relation Kind</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static AssetRelationKind get(int value) {
		switch (value) {
			case DEPENDS_ON_VALUE: return DEPENDS_ON;
			case RUNS_ON_VALUE: return RUNS_ON;
			case CONNECTS_TO_VALUE: return CONNECTS_TO;
			case PART_OF_VALUE: return PART_OF;
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
	private AssetRelationKind(int value, String name, String literal) {
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
	
} //AssetRelationKind
