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
 * A representation of the literals of the enumeration '<em><b>Sql Dialect</b></em>',
 * and utility methods for working with them.
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * Identifies the SQL dialect of the target relational database, allowing the runtime to generate dialect-specific DDL and DML statements.
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage#getSqlDialect()
 * @model
 * @generated
 */
@ProviderType
public enum SqlDialect implements Enumerator {
	/**
	 * The '<em><b>H2</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * H2 in-memory/embedded database (typically used for testing).
	 * <!-- end-model-doc -->
	 * @see #H2_VALUE
	 * @generated
	 * @ordered
	 */
	H2(0, "H2", "H2"),

	/**
	 * The '<em><b>POSTGRES</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * PostgreSQL relational database.
	 * <!-- end-model-doc -->
	 * @see #POSTGRES_VALUE
	 * @generated
	 * @ordered
	 */
	POSTGRES(1, "POSTGRES", "POSTGRES"),

	/**
	 * The '<em><b>MYSQL</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * MySQL / MariaDB relational database.
	 * <!-- end-model-doc -->
	 * @see #MYSQL_VALUE
	 * @generated
	 * @ordered
	 */
	MYSQL(2, "MYSQL", "MYSQL"),

	/**
	 * The '<em><b>ORACLE</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Oracle Database.
	 * <!-- end-model-doc -->
	 * @see #ORACLE_VALUE
	 * @generated
	 * @ordered
	 */
	ORACLE(3, "ORACLE", "ORACLE"),

	/**
	 * The '<em><b>SQLSERVER</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Microsoft SQL Server.
	 * <!-- end-model-doc -->
	 * @see #SQLSERVER_VALUE
	 * @generated
	 * @ordered
	 */
	SQLSERVER(4, "SQLSERVER", "SQLSERVER"),

	/**
	 * The '<em><b>OTHER</b></em>' literal object.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Any other SQL database not explicitly listed. Generic SQL will be generated.
	 * <!-- end-model-doc -->
	 * @see #OTHER_VALUE
	 * @generated
	 * @ordered
	 */
	OTHER(5, "OTHER", "OTHER");

	/**
	 * The '<em><b>H2</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * H2 in-memory/embedded database (typically used for testing).
	 * <!-- end-model-doc -->
	 * @see #H2
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int H2_VALUE = 0;

	/**
	 * The '<em><b>POSTGRES</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * PostgreSQL relational database.
	 * <!-- end-model-doc -->
	 * @see #POSTGRES
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int POSTGRES_VALUE = 1;

	/**
	 * The '<em><b>MYSQL</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * MySQL / MariaDB relational database.
	 * <!-- end-model-doc -->
	 * @see #MYSQL
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int MYSQL_VALUE = 2;

	/**
	 * The '<em><b>ORACLE</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Oracle Database.
	 * <!-- end-model-doc -->
	 * @see #ORACLE
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int ORACLE_VALUE = 3;

	/**
	 * The '<em><b>SQLSERVER</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Microsoft SQL Server.
	 * <!-- end-model-doc -->
	 * @see #SQLSERVER
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int SQLSERVER_VALUE = 4;

	/**
	 * The '<em><b>OTHER</b></em>' literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Any other SQL database not explicitly listed. Generic SQL will be generated.
	 * <!-- end-model-doc -->
	 * @see #OTHER
	 * @model
	 * @generated
	 * @ordered
	 */
	public static final int OTHER_VALUE = 5;

	/**
	 * An array of all the '<em><b>Sql Dialect</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static final SqlDialect[] VALUES_ARRAY =
		new SqlDialect[] {
			H2,
			POSTGRES,
			MYSQL,
			ORACLE,
			SQLSERVER,
			OTHER,
		};

	/**
	 * A public read-only list of all the '<em><b>Sql Dialect</b></em>' enumerators.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static final List<SqlDialect> VALUES = Collections.unmodifiableList(Arrays.asList(VALUES_ARRAY));

	/**
	 * Returns the '<em><b>Sql Dialect</b></em>' literal with the specified literal value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param literal the literal.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static SqlDialect get(String literal) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			SqlDialect result = VALUES_ARRAY[i];
			if (result.toString().equals(literal)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Sql Dialect</b></em>' literal with the specified name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param name the name.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static SqlDialect getByName(String name) {
		for (int i = 0; i < VALUES_ARRAY.length; ++i) {
			SqlDialect result = VALUES_ARRAY[i];
			if (result.getName().equals(name)) {
				return result;
			}
		}
		return null;
	}

	/**
	 * Returns the '<em><b>Sql Dialect</b></em>' literal with the specified integer value.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the integer value.
	 * @return the matching enumerator or <code>null</code>.
	 * @generated
	 */
	public static SqlDialect get(int value) {
		switch (value) {
			case H2_VALUE: return H2;
			case POSTGRES_VALUE: return POSTGRES;
			case MYSQL_VALUE: return MYSQL;
			case ORACLE_VALUE: return ORACLE;
			case SQLSERVER_VALUE: return SQLSERVER;
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
	private SqlDialect(int value, String name, String literal) {
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
	
} //SqlDialect
