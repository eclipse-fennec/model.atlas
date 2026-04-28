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
 * A representation of the literals of the enumeration '<em><b>Join Type</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * Strategy used to represent an EMF EReference relationship in a relational database table.
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage#getJoinType()
 * @model
 * @generated
 */
@ProviderType
public enum JoinType implements Enumerator {
	/**
	 * The '<em><b>FOREIGN KEY</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The relationship is stored as a foreign key column in the owning table pointing to the primary key of the referenced table.
	 * <!-- end-model-doc -->
	 * @see #FOREIGN_KEY_VALUE
	 * @generated
	 * @ordered
	 */
	FOREIGN_KEY(0, "FOREIGN_KEY", "FOREIGN_KEY"),

	/**
	 * The '<em><b>EMBEDDED</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The referenced object's fields are inlined as columns directly into the owning table (JPA @Embedded semantics).
	 * <!-- end-model-doc -->
	 * @see #EMBEDDED_VALUE
	 * @generated
	 * @ordered
	 */
	EMBEDDED(1, "EMBEDDED", "EMBEDDED");

	/**
	 * The '<em><b>FOREIGN KEY</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The relationship is stored as a foreign key column in the owning table pointing to the primary key of the referenced table.
	 * <!-- end-model-doc -->
	 * @see #FOREIGN_KEY
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int FOREIGN_KEY_VALUE = 0;

	/**
	 * The '<em><b>EMBEDDED</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The referenced object's fields are inlined as columns directly into the owning table (JPA @Embedded semantics).
	 * <!-- end-model-doc -->
	 * @see #EMBEDDED
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int EMBEDDED_VALUE = 1;

	/**
	 * An array of all the '<em><b>Join Type</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final JoinType[] VALUES_ARRAY =
		new JoinType[] {
			FOREIGN_KEY,
			EMBEDDED,
		};

	/**
	 * A public read-only list of all the '<em><b>Join Type</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<JoinType> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Join Type</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static JoinType get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			JoinType result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Join Type</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static JoinType getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			JoinType result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Join Type</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static JoinType get(int value) {
		switch (value) {
			case FOREIGN_KEY_VALUE: return FOREIGN_KEY;
			case EMBEDDED_VALUE: return EMBEDDED;
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
	private JoinType(int value, String name, String literal) {
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
	
} //JoinType
