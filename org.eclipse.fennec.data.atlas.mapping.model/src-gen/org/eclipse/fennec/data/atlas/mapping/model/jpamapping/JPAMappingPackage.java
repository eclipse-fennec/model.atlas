/*
 */
package org.eclipse.fennec.data.atlas.mapping.model.jpamapping;


import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.fennec.emf.osgi.annotation.provide.EPackage;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Package</b> for the model.
 * It contains accessors for the meta objects to represent
 * <ul>
 *   <li>each class,</li>
 *   <li>each feature of each class,</li>
 *   <li>each operation of each class,</li>
 *   <li>each enum,</li>
 *   <li>and each data type</li>
 * </ul>
 * <!-- end-user-doc -->
 * <!-- begin-model-doc -->
 * Root package for the JPA mapping configuration model. Describes how EMF model classes and features are mapped to relational database tables and columns.
 * <!-- end-model-doc -->
 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingFactory
 * @model kind="package"
 *        annotation="Version value='1.0'"
 *        annotation="http://www.eclipse.org/emf/2002/GenModel complianceLevel='17.0' oSGiCompatible='true' basePackage='org.eclipse.fennec.data.atlas.mapping.model' resource='XMI'"
 * @generated
 */
@ProviderType
@EPackage(uri = JPAMappingPackage.eNS_URI, genModel = "/model/jpa-mapping.genmodel", genModelSourceLocations = {"model/jpa-mapping.genmodel","org.eclipse.fennec.data.atlas.mapping.model/model/jpa-mapping.genmodel"}, ecore = "/model/jpa-mapping.ecore", ecoreSourceLocations = "/model/jpa-mapping.ecore")
public interface JPAMappingPackage extends org.eclipse.emf.ecore.EPackage {
	/**
	 * The package name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNAME = "jpamapping";

	/**
	 * The package namespace URI.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_URI = "http://eclipse.org/fennec/data/atlas/jpamapping/1.0.0";

	/**
	 * The package namespace name.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	String eNS_PREFIX = "jpamapping";

	/**
	 * The singleton instance of the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	JPAMappingPackage eINSTANCE = org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JPAMappingPackageImpl.init();

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JpaMappingConfigImpl <em>Jpa Mapping Config</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JpaMappingConfigImpl
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JPAMappingPackageImpl#getJpaMappingConfig()
	 * @generated
	 */
	int JPA_MAPPING_CONFIG = 0;

	/**
	 * The feature id for the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int JPA_MAPPING_CONFIG__NAME = 0;

	/**
	 * The feature id for the '<em><b>Target Model Ns Uri</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int JPA_MAPPING_CONFIG__TARGET_MODEL_NS_URI = 1;

	/**
	 * The feature id for the '<em><b>Data Source</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int JPA_MAPPING_CONFIG__DATA_SOURCE = 2;

	/**
	 * The feature id for the '<em><b>Table Mappings</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int JPA_MAPPING_CONFIG__TABLE_MAPPINGS = 3;

	/**
	 * The number of structural features of the '<em>Jpa Mapping Config</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int JPA_MAPPING_CONFIG_FEATURE_COUNT = 4;

	/**
	 * The number of operations of the '<em>Jpa Mapping Config</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int JPA_MAPPING_CONFIG_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.DataSourceConfigImpl <em>Data Source Config</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.DataSourceConfigImpl
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JPAMappingPackageImpl#getDataSourceConfig()
	 * @generated
	 */
	int DATA_SOURCE_CONFIG = 1;

	/**
	 * The feature id for the '<em><b>Driver Class</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SOURCE_CONFIG__DRIVER_CLASS = 0;

	/**
	 * The feature id for the '<em><b>Jdbc Url</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SOURCE_CONFIG__JDBC_URL = 1;

	/**
	 * The feature id for the '<em><b>Username</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SOURCE_CONFIG__USERNAME = 2;

	/**
	 * The feature id for the '<em><b>Password Ref</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SOURCE_CONFIG__PASSWORD_REF = 3;

	/**
	 * The feature id for the '<em><b>Pool Size</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SOURCE_CONFIG__POOL_SIZE = 4;

	/**
	 * The feature id for the '<em><b>Dialect</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SOURCE_CONFIG__DIALECT = 5;

	/**
	 * The number of structural features of the '<em>Data Source Config</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SOURCE_CONFIG_FEATURE_COUNT = 6;

	/**
	 * The number of operations of the '<em>Data Source Config</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int DATA_SOURCE_CONFIG_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.TableMappingImpl <em>Table Mapping</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.TableMappingImpl
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JPAMappingPackageImpl#getTableMapping()
	 * @generated
	 */
	int TABLE_MAPPING = 2;

	/**
	 * The feature id for the '<em><b>Class Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TABLE_MAPPING__CLASS_NAME = 0;

	/**
	 * The feature id for the '<em><b>Table Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TABLE_MAPPING__TABLE_NAME = 1;

	/**
	 * The feature id for the '<em><b>Schema</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TABLE_MAPPING__SCHEMA = 2;

	/**
	 * The feature id for the '<em><b>Column Mappings</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TABLE_MAPPING__COLUMN_MAPPINGS = 3;

	/**
	 * The feature id for the '<em><b>Join Mappings</b></em>' containment reference list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TABLE_MAPPING__JOIN_MAPPINGS = 4;

	/**
	 * The number of structural features of the '<em>Table Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TABLE_MAPPING_FEATURE_COUNT = 5;

	/**
	 * The number of operations of the '<em>Table Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int TABLE_MAPPING_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.ColumnMappingImpl <em>Column Mapping</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.ColumnMappingImpl
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JPAMappingPackageImpl#getColumnMapping()
	 * @generated
	 */
	int COLUMN_MAPPING = 3;

	/**
	 * The feature id for the '<em><b>Feature Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COLUMN_MAPPING__FEATURE_NAME = 0;

	/**
	 * The feature id for the '<em><b>Column Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COLUMN_MAPPING__COLUMN_NAME = 1;

	/**
	 * The feature id for the '<em><b>Column Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COLUMN_MAPPING__COLUMN_TYPE = 2;

	/**
	 * The feature id for the '<em><b>Nullable</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COLUMN_MAPPING__NULLABLE = 3;

	/**
	 * The feature id for the '<em><b>Primary Key</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COLUMN_MAPPING__PRIMARY_KEY = 4;

	/**
	 * The number of structural features of the '<em>Column Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COLUMN_MAPPING_FEATURE_COUNT = 5;

	/**
	 * The number of operations of the '<em>Column Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int COLUMN_MAPPING_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JoinMappingImpl <em>Join Mapping</em>}' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JoinMappingImpl
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JPAMappingPackageImpl#getJoinMapping()
	 * @generated
	 */
	int JOIN_MAPPING = 4;

	/**
	 * The feature id for the '<em><b>Reference Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int JOIN_MAPPING__REFERENCE_NAME = 0;

	/**
	 * The feature id for the '<em><b>Join Column</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int JOIN_MAPPING__JOIN_COLUMN = 1;

	/**
	 * The feature id for the '<em><b>Join Type</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int JOIN_MAPPING__JOIN_TYPE = 2;

	/**
	 * The feature id for the '<em><b>Cascade Type</b></em>' attribute list.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int JOIN_MAPPING__CASCADE_TYPE = 3;

	/**
	 * The number of structural features of the '<em>Join Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int JOIN_MAPPING_FEATURE_COUNT = 4;

	/**
	 * The number of operations of the '<em>Join Mapping</em>' class.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 * @ordered
	 */
	int JOIN_MAPPING_OPERATION_COUNT = 0;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.SqlDialect <em>Sql Dialect</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.SqlDialect
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JPAMappingPackageImpl#getSqlDialect()
	 * @generated
	 */
	int SQL_DIALECT = 5;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinType <em>Join Type</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinType
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JPAMappingPackageImpl#getJoinType()
	 * @generated
	 */
	int JOIN_TYPE = 6;

	/**
	 * The meta object id for the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.CascadeType <em>Cascade Type</em>}' enum.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.CascadeType
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JPAMappingPackageImpl#getCascadeType()
	 * @generated
	 */
	int CASCADE_TYPE = 7;


	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig <em>Jpa Mapping Config</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Jpa Mapping Config</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig
	 * @generated
	 */
	EClass getJpaMappingConfig();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig#getName <em>Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Name</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig#getName()
	 * @see #getJpaMappingConfig()
	 * @generated
	 */
	EAttribute getJpaMappingConfig_Name();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig#getTargetModelNsUri <em>Target Model Ns Uri</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Target Model Ns Uri</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig#getTargetModelNsUri()
	 * @see #getJpaMappingConfig()
	 * @generated
	 */
	EAttribute getJpaMappingConfig_TargetModelNsUri();

	/**
	 * Returns the meta object for the containment reference '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig#getDataSource <em>Data Source</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference '<em>Data Source</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig#getDataSource()
	 * @see #getJpaMappingConfig()
	 * @generated
	 */
	EReference getJpaMappingConfig_DataSource();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig#getTableMappings <em>Table Mappings</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Table Mappings</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig#getTableMappings()
	 * @see #getJpaMappingConfig()
	 * @generated
	 */
	EReference getJpaMappingConfig_TableMappings();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig <em>Data Source Config</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Data Source Config</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig
	 * @generated
	 */
	EClass getDataSourceConfig();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig#getDriverClass <em>Driver Class</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Driver Class</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig#getDriverClass()
	 * @see #getDataSourceConfig()
	 * @generated
	 */
	EAttribute getDataSourceConfig_DriverClass();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig#getJdbcUrl <em>Jdbc Url</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Jdbc Url</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig#getJdbcUrl()
	 * @see #getDataSourceConfig()
	 * @generated
	 */
	EAttribute getDataSourceConfig_JdbcUrl();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig#getUsername <em>Username</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Username</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig#getUsername()
	 * @see #getDataSourceConfig()
	 * @generated
	 */
	EAttribute getDataSourceConfig_Username();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig#getPasswordRef <em>Password Ref</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Password Ref</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig#getPasswordRef()
	 * @see #getDataSourceConfig()
	 * @generated
	 */
	EAttribute getDataSourceConfig_PasswordRef();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig#getPoolSize <em>Pool Size</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Pool Size</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig#getPoolSize()
	 * @see #getDataSourceConfig()
	 * @generated
	 */
	EAttribute getDataSourceConfig_PoolSize();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig#getDialect <em>Dialect</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Dialect</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig#getDialect()
	 * @see #getDataSourceConfig()
	 * @generated
	 */
	EAttribute getDataSourceConfig_Dialect();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.TableMapping <em>Table Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Table Mapping</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.TableMapping
	 * @generated
	 */
	EClass getTableMapping();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.TableMapping#getClassName <em>Class Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Class Name</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.TableMapping#getClassName()
	 * @see #getTableMapping()
	 * @generated
	 */
	EAttribute getTableMapping_ClassName();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.TableMapping#getTableName <em>Table Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Table Name</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.TableMapping#getTableName()
	 * @see #getTableMapping()
	 * @generated
	 */
	EAttribute getTableMapping_TableName();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.TableMapping#getSchema <em>Schema</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Schema</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.TableMapping#getSchema()
	 * @see #getTableMapping()
	 * @generated
	 */
	EAttribute getTableMapping_Schema();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.TableMapping#getColumnMappings <em>Column Mappings</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Column Mappings</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.TableMapping#getColumnMappings()
	 * @see #getTableMapping()
	 * @generated
	 */
	EReference getTableMapping_ColumnMappings();

	/**
	 * Returns the meta object for the containment reference list '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.TableMapping#getJoinMappings <em>Join Mappings</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the containment reference list '<em>Join Mappings</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.TableMapping#getJoinMappings()
	 * @see #getTableMapping()
	 * @generated
	 */
	EReference getTableMapping_JoinMappings();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.ColumnMapping <em>Column Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Column Mapping</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.ColumnMapping
	 * @generated
	 */
	EClass getColumnMapping();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.ColumnMapping#getFeatureName <em>Feature Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Feature Name</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.ColumnMapping#getFeatureName()
	 * @see #getColumnMapping()
	 * @generated
	 */
	EAttribute getColumnMapping_FeatureName();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.ColumnMapping#getColumnName <em>Column Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Column Name</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.ColumnMapping#getColumnName()
	 * @see #getColumnMapping()
	 * @generated
	 */
	EAttribute getColumnMapping_ColumnName();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.ColumnMapping#getColumnType <em>Column Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Column Type</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.ColumnMapping#getColumnType()
	 * @see #getColumnMapping()
	 * @generated
	 */
	EAttribute getColumnMapping_ColumnType();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.ColumnMapping#isNullable <em>Nullable</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Nullable</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.ColumnMapping#isNullable()
	 * @see #getColumnMapping()
	 * @generated
	 */
	EAttribute getColumnMapping_Nullable();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.ColumnMapping#isPrimaryKey <em>Primary Key</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Primary Key</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.ColumnMapping#isPrimaryKey()
	 * @see #getColumnMapping()
	 * @generated
	 */
	EAttribute getColumnMapping_PrimaryKey();

	/**
	 * Returns the meta object for class '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinMapping <em>Join Mapping</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for class '<em>Join Mapping</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinMapping
	 * @generated
	 */
	EClass getJoinMapping();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinMapping#getReferenceName <em>Reference Name</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Reference Name</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinMapping#getReferenceName()
	 * @see #getJoinMapping()
	 * @generated
	 */
	EAttribute getJoinMapping_ReferenceName();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinMapping#getJoinColumn <em>Join Column</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Join Column</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinMapping#getJoinColumn()
	 * @see #getJoinMapping()
	 * @generated
	 */
	EAttribute getJoinMapping_JoinColumn();

	/**
	 * Returns the meta object for the attribute '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinMapping#getJoinType <em>Join Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute '<em>Join Type</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinMapping#getJoinType()
	 * @see #getJoinMapping()
	 * @generated
	 */
	EAttribute getJoinMapping_JoinType();

	/**
	 * Returns the meta object for the attribute list '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinMapping#getCascadeType <em>Cascade Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for the attribute list '<em>Cascade Type</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinMapping#getCascadeType()
	 * @see #getJoinMapping()
	 * @generated
	 */
	EAttribute getJoinMapping_CascadeType();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.SqlDialect <em>Sql Dialect</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Sql Dialect</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.SqlDialect
	 * @generated
	 */
	EEnum getSqlDialect();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinType <em>Join Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Join Type</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinType
	 * @generated
	 */
	EEnum getJoinType();

	/**
	 * Returns the meta object for enum '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.CascadeType <em>Cascade Type</em>}'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the meta object for enum '<em>Cascade Type</em>'.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.CascadeType
	 * @generated
	 */
	EEnum getCascadeType();

	/**
	 * Returns the factory that creates the instances of the model.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the factory that creates the instances of the model.
	 * @generated
	 */
	JPAMappingFactory getJPAMappingFactory();

	/**
	 * <!-- begin-user-doc -->
	 * Defines literals for the meta objects that represent
	 * <ul>
	 *   <li>each class,</li>
	 *   <li>each feature of each class,</li>
	 *   <li>each operation of each class,</li>
	 *   <li>each enum,</li>
	 *   <li>and each data type</li>
	 * </ul>
	 * <!-- end-user-doc -->
	 * @generated
	 */
	interface Literals {
		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JpaMappingConfigImpl <em>Jpa Mapping Config</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JpaMappingConfigImpl
		 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JPAMappingPackageImpl#getJpaMappingConfig()
		 * @generated
		 */
		EClass JPA_MAPPING_CONFIG = eINSTANCE.getJpaMappingConfig();

		/**
		 * The meta object literal for the '<em><b>Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute JPA_MAPPING_CONFIG__NAME = eINSTANCE.getJpaMappingConfig_Name();

		/**
		 * The meta object literal for the '<em><b>Target Model Ns Uri</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute JPA_MAPPING_CONFIG__TARGET_MODEL_NS_URI = eINSTANCE.getJpaMappingConfig_TargetModelNsUri();

		/**
		 * The meta object literal for the '<em><b>Data Source</b></em>' containment reference feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference JPA_MAPPING_CONFIG__DATA_SOURCE = eINSTANCE.getJpaMappingConfig_DataSource();

		/**
		 * The meta object literal for the '<em><b>Table Mappings</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference JPA_MAPPING_CONFIG__TABLE_MAPPINGS = eINSTANCE.getJpaMappingConfig_TableMappings();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.DataSourceConfigImpl <em>Data Source Config</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.DataSourceConfigImpl
		 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JPAMappingPackageImpl#getDataSourceConfig()
		 * @generated
		 */
		EClass DATA_SOURCE_CONFIG = eINSTANCE.getDataSourceConfig();

		/**
		 * The meta object literal for the '<em><b>Driver Class</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_SOURCE_CONFIG__DRIVER_CLASS = eINSTANCE.getDataSourceConfig_DriverClass();

		/**
		 * The meta object literal for the '<em><b>Jdbc Url</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_SOURCE_CONFIG__JDBC_URL = eINSTANCE.getDataSourceConfig_JdbcUrl();

		/**
		 * The meta object literal for the '<em><b>Username</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_SOURCE_CONFIG__USERNAME = eINSTANCE.getDataSourceConfig_Username();

		/**
		 * The meta object literal for the '<em><b>Password Ref</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_SOURCE_CONFIG__PASSWORD_REF = eINSTANCE.getDataSourceConfig_PasswordRef();

		/**
		 * The meta object literal for the '<em><b>Pool Size</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_SOURCE_CONFIG__POOL_SIZE = eINSTANCE.getDataSourceConfig_PoolSize();

		/**
		 * The meta object literal for the '<em><b>Dialect</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute DATA_SOURCE_CONFIG__DIALECT = eINSTANCE.getDataSourceConfig_Dialect();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.TableMappingImpl <em>Table Mapping</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.TableMappingImpl
		 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JPAMappingPackageImpl#getTableMapping()
		 * @generated
		 */
		EClass TABLE_MAPPING = eINSTANCE.getTableMapping();

		/**
		 * The meta object literal for the '<em><b>Class Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TABLE_MAPPING__CLASS_NAME = eINSTANCE.getTableMapping_ClassName();

		/**
		 * The meta object literal for the '<em><b>Table Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TABLE_MAPPING__TABLE_NAME = eINSTANCE.getTableMapping_TableName();

		/**
		 * The meta object literal for the '<em><b>Schema</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute TABLE_MAPPING__SCHEMA = eINSTANCE.getTableMapping_Schema();

		/**
		 * The meta object literal for the '<em><b>Column Mappings</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TABLE_MAPPING__COLUMN_MAPPINGS = eINSTANCE.getTableMapping_ColumnMappings();

		/**
		 * The meta object literal for the '<em><b>Join Mappings</b></em>' containment reference list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EReference TABLE_MAPPING__JOIN_MAPPINGS = eINSTANCE.getTableMapping_JoinMappings();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.ColumnMappingImpl <em>Column Mapping</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.ColumnMappingImpl
		 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JPAMappingPackageImpl#getColumnMapping()
		 * @generated
		 */
		EClass COLUMN_MAPPING = eINSTANCE.getColumnMapping();

		/**
		 * The meta object literal for the '<em><b>Feature Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute COLUMN_MAPPING__FEATURE_NAME = eINSTANCE.getColumnMapping_FeatureName();

		/**
		 * The meta object literal for the '<em><b>Column Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute COLUMN_MAPPING__COLUMN_NAME = eINSTANCE.getColumnMapping_ColumnName();

		/**
		 * The meta object literal for the '<em><b>Column Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute COLUMN_MAPPING__COLUMN_TYPE = eINSTANCE.getColumnMapping_ColumnType();

		/**
		 * The meta object literal for the '<em><b>Nullable</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute COLUMN_MAPPING__NULLABLE = eINSTANCE.getColumnMapping_Nullable();

		/**
		 * The meta object literal for the '<em><b>Primary Key</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute COLUMN_MAPPING__PRIMARY_KEY = eINSTANCE.getColumnMapping_PrimaryKey();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JoinMappingImpl <em>Join Mapping</em>}' class.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JoinMappingImpl
		 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JPAMappingPackageImpl#getJoinMapping()
		 * @generated
		 */
		EClass JOIN_MAPPING = eINSTANCE.getJoinMapping();

		/**
		 * The meta object literal for the '<em><b>Reference Name</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute JOIN_MAPPING__REFERENCE_NAME = eINSTANCE.getJoinMapping_ReferenceName();

		/**
		 * The meta object literal for the '<em><b>Join Column</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute JOIN_MAPPING__JOIN_COLUMN = eINSTANCE.getJoinMapping_JoinColumn();

		/**
		 * The meta object literal for the '<em><b>Join Type</b></em>' attribute feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute JOIN_MAPPING__JOIN_TYPE = eINSTANCE.getJoinMapping_JoinType();

		/**
		 * The meta object literal for the '<em><b>Cascade Type</b></em>' attribute list feature.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @generated
		 */
		EAttribute JOIN_MAPPING__CASCADE_TYPE = eINSTANCE.getJoinMapping_CascadeType();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.SqlDialect <em>Sql Dialect</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.SqlDialect
		 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JPAMappingPackageImpl#getSqlDialect()
		 * @generated
		 */
		EEnum SQL_DIALECT = eINSTANCE.getSqlDialect();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinType <em>Join Type</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinType
		 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JPAMappingPackageImpl#getJoinType()
		 * @generated
		 */
		EEnum JOIN_TYPE = eINSTANCE.getJoinType();

		/**
		 * The meta object literal for the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.CascadeType <em>Cascade Type</em>}' enum.
		 * <!-- begin-user-doc -->
		 * <!-- end-user-doc -->
		 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.CascadeType
		 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JPAMappingPackageImpl#getCascadeType()
		 * @generated
		 */
		EEnum CASCADE_TYPE = eINSTANCE.getCascadeType();

	}

} //JPAMappingPackage
