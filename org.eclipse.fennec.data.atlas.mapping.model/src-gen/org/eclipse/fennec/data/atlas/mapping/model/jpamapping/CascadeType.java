/*
 */
package org.eclipse.fennec.data.atlas.mapping.model.jpamapping;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.emf.common.util.Enumerator;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the literals of the enumeration '<em><b>Cascade Type</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * Mirrors jakarta.persistence.CascadeType. Defines which JPA entity lifecycle operations are automatically propagated from the owning entity to the associated entity.
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage#getCascadeType()
 * @model
 * @generated
 */
@ProviderType
public enum CascadeType implements Enumerator {
	/**
	 * The '<em><b>ALL</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Shorthand for enabling all cascade operations (PERSIST, MERGE, REMOVE, REFRESH, DETACH).
	 * <!-- end-model-doc -->
	 * @see #ALL_VALUE
	 * @generated
	 * @ordered
	 */
	ALL(0, "ALL", "ALL"),

	/**
	 * The '<em><b>PERSIST</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Cascades EntityManager.persist(): when the owning entity is persisted, the associated entity is also persisted.
	 * <!-- end-model-doc -->
	 * @see #PERSIST_VALUE
	 * @generated
	 * @ordered
	 */
	PERSIST(1, "PERSIST", "PERSIST"),

	/**
	 * The '<em><b>MERGE</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Cascades EntityManager.merge(): when the owning entity is merged, the associated entity is also merged.
	 * <!-- end-model-doc -->
	 * @see #MERGE_VALUE
	 * @generated
	 * @ordered
	 */
	MERGE(2, "MERGE", "MERGE"),

	/**
	 * The '<em><b>REMOVE</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Cascades EntityManager.remove(): when the owning entity is deleted, the associated entity is also deleted.
	 * <!-- end-model-doc -->
	 * @see #REMOVE_VALUE
	 * @generated
	 * @ordered
	 */
	REMOVE(3, "REMOVE", "REMOVE"),

	/**
	 * The '<em><b>REFRESH</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Cascades EntityManager.refresh(): when the owning entity is refreshed from the database, the associated entity is also refreshed.
	 * <!-- end-model-doc -->
	 * @see #REFRESH_VALUE
	 * @generated
	 * @ordered
	 */
	REFRESH(4, "REFRESH", "REFRESH"),

	/**
	 * The '<em><b>DETACH</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Cascades EntityManager.detach(): when the owning entity is detached from the persistence context, the associated entity is also detached.
	 * <!-- end-model-doc -->
	 * @see #DETACH_VALUE
	 * @generated
	 * @ordered
	 */
	DETACH(5, "DETACH", "DETACH");

	/**
	 * The '<em><b>ALL</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Shorthand for enabling all cascade operations (PERSIST, MERGE, REMOVE, REFRESH, DETACH).
	 * <!-- end-model-doc -->
	 * @see #ALL
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int ALL_VALUE = 0;

	/**
	 * The '<em><b>PERSIST</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Cascades EntityManager.persist(): when the owning entity is persisted, the associated entity is also persisted.
	 * <!-- end-model-doc -->
	 * @see #PERSIST
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int PERSIST_VALUE = 1;

	/**
	 * The '<em><b>MERGE</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Cascades EntityManager.merge(): when the owning entity is merged, the associated entity is also merged.
	 * <!-- end-model-doc -->
	 * @see #MERGE
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int MERGE_VALUE = 2;

	/**
	 * The '<em><b>REMOVE</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Cascades EntityManager.remove(): when the owning entity is deleted, the associated entity is also deleted.
	 * <!-- end-model-doc -->
	 * @see #REMOVE
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int REMOVE_VALUE = 3;

	/**
	 * The '<em><b>REFRESH</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Cascades EntityManager.refresh(): when the owning entity is refreshed from the database, the associated entity is also refreshed.
	 * <!-- end-model-doc -->
	 * @see #REFRESH
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int REFRESH_VALUE = 4;

	/**
	 * The '<em><b>DETACH</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Cascades EntityManager.detach(): when the owning entity is detached from the persistence context, the associated entity is also detached.
	 * <!-- end-model-doc -->
	 * @see #DETACH
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int DETACH_VALUE = 5;

	/**
	 * An array of all the '<em><b>Cascade Type</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final CascadeType[] VALUES_ARRAY =
		new CascadeType[] {
			ALL,
			PERSIST,
			MERGE,
			REMOVE,
			REFRESH,
			DETACH,
		};

	/**
	 * A public read-only list of all the '<em><b>Cascade Type</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<CascadeType> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Cascade Type</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static CascadeType get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			CascadeType result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Cascade Type</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static CascadeType getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			CascadeType result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Cascade Type</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static CascadeType get(int value) {
		switch (value) {
			case ALL_VALUE: return ALL;
			case PERSIST_VALUE: return PERSIST;
			case MERGE_VALUE: return MERGE;
			case REMOVE_VALUE: return REMOVE;
			case REFRESH_VALUE: return REFRESH;
			case DETACH_VALUE: return DETACH;
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
	private CascadeType(int value, String name, String literal) {
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
	
} //CascadeType
