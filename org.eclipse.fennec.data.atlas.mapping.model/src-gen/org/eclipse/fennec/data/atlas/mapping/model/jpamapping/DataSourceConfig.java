/*
 */
package org.eclipse.fennec.data.atlas.mapping.model.jpamapping;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Data Source Config</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * JDBC data source configuration, including connection URL, credentials, connection pool settings, and the SQL dialect to use.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig#getDriverClass <em>Driver Class</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig#getJdbcUrl <em>Jdbc Url</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig#getUsername <em>Username</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig#getPasswordRef <em>Password Ref</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig#getPoolSize <em>Pool Size</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig#getDialect <em>Dialect</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage#getDataSourceConfig()
 * @model
 * @generated
 */
@ProviderType
public interface DataSourceConfig extends EObject {
	/**
	 * Returns the value of the '<em><b>Driver Class</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Fully qualified class name of the JDBC driver (e.g. 'org.postgresql.Driver').
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Driver Class</em>' attribute.
	 * @see #setDriverClass(String)
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage#getDataSourceConfig_DriverClass()
	 * @model
	 * @generated
	 */
	String getDriverClass();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig#getDriverClass <em>Driver Class</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Driver Class</em>' attribute.
	 * @see #getDriverClass()
	 * @generated
	 */
	void setDriverClass(String value);

	/**
	 * Returns the value of the '<em><b>Jdbc Url</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * JDBC connection URL (e.g. 'jdbc:postgresql://localhost:5432/mydb').
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Jdbc Url</em>' attribute.
	 * @see #setJdbcUrl(String)
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage#getDataSourceConfig_JdbcUrl()
	 * @model
	 * @generated
	 */
	String getJdbcUrl();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig#getJdbcUrl <em>Jdbc Url</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Jdbc Url</em>' attribute.
	 * @see #getJdbcUrl()
	 * @generated
	 */
	void setJdbcUrl(String value);

	/**
	 * Returns the value of the '<em><b>Username</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Database user name used to authenticate the JDBC connection.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Username</em>' attribute.
	 * @see #setUsername(String)
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage#getDataSourceConfig_Username()
	 * @model
	 * @generated
	 */
	String getUsername();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig#getUsername <em>Username</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Username</em>' attribute.
	 * @see #getUsername()
	 * @generated
	 */
	void setUsername(String value);

	/**
	 * Returns the value of the '<em><b>Password Ref</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Reference key or environment variable name from which the database password is resolved at runtime, avoiding plain-text secrets in the configuration.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Password Ref</em>' attribute.
	 * @see #setPasswordRef(String)
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage#getDataSourceConfig_PasswordRef()
	 * @model
	 * @generated
	 */
	String getPasswordRef();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig#getPasswordRef <em>Password Ref</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Password Ref</em>' attribute.
	 * @see #getPasswordRef()
	 * @generated
	 */
	void setPasswordRef(String value);

	/**
	 * Returns the value of the '<em><b>Pool Size</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Maximum number of connections maintained in the connection pool.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Pool Size</em>' attribute.
	 * @see #setPoolSize(int)
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage#getDataSourceConfig_PoolSize()
	 * @model
	 * @generated
	 */
	int getPoolSize();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig#getPoolSize <em>Pool Size</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Pool Size</em>' attribute.
	 * @see #getPoolSize()
	 * @generated
	 */
	void setPoolSize(int value);

	/**
	 * Returns the value of the '<em><b>Dialect</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.SqlDialect}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * SQL dialect of the target database, used to generate dialect-specific SQL statements.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Dialect</em>' attribute.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.SqlDialect
	 * @see #setDialect(SqlDialect)
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage#getDataSourceConfig_Dialect()
	 * @model
	 * @generated
	 */
	SqlDialect getDialect();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig#getDialect <em>Dialect</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Dialect</em>' attribute.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.SqlDialect
	 * @see #getDialect()
	 * @generated
	 */
	void setDialect(SqlDialect value);

} // DataSourceConfig
