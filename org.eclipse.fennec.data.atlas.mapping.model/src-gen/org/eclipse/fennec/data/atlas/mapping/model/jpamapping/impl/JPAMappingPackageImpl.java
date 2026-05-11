/**
 */
package org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EEnum;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;

import org.eclipse.emf.ecore.impl.EPackageImpl;

import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.CascadeType;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.ColumnMapping;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.DataSourceConfig;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingFactory;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinMapping;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinType;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.SqlDialect;
import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.TableMapping;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Package</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class JPAMappingPackageImpl extends EPackageImpl implements JPAMappingPackage {
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass jpaMappingConfigEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass dataSourceConfigEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass tableMappingEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass columnMappingEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EClass joinMappingEClass = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum sqlDialectEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum joinTypeEEnum = null;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private EEnum cascadeTypeEEnum = null;

	/**
	 * Creates an instance of the model <b>Package</b>, registered with
	 * {@link org.eclipse.emf.ecore.EPackage.Registry EPackage.Registry} by the package
	 * package URI value.
	 * <p>Note: the correct way to create the package is via the static
	 * factory method {@link #init init()}, which also performs
	 * initialization of the package, or returns the registered package,
	 * if one already exists.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see org.eclipse.emf.ecore.EPackage.Registry
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage#eNS_URI
	 * @see #init()
	 * @generated
	 */
	private JPAMappingPackageImpl() {
		super(eNS_URI, JPAMappingFactory.eINSTANCE);
	}
	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private static boolean isInited = false;

	/**
	 * Creates, registers, and initializes the <b>Package</b> for this model, and for any others upon which it depends.
	 *
	 * <p>This method is used to initialize {@link JPAMappingPackage#eINSTANCE} when that field is accessed.
	 * Clients should not invoke it directly. Instead, they should simply access that field to obtain the package.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #eNS_URI
	 * @see #createPackageContents()
	 * @see #initializePackageContents()
	 * @generated
	 */
	public static JPAMappingPackage init() {
		if (isInited) return (JPAMappingPackage)EPackage.Registry.INSTANCE.getEPackage(JPAMappingPackage.eNS_URI);

		// Obtain or create and register package
		Object registeredJPAMappingPackage = EPackage.Registry.INSTANCE.get(eNS_URI);
		JPAMappingPackageImpl theJPAMappingPackage = registeredJPAMappingPackage instanceof JPAMappingPackageImpl ? (JPAMappingPackageImpl)registeredJPAMappingPackage : new JPAMappingPackageImpl();

		isInited = true;

		// Create package meta-data objects
		theJPAMappingPackage.createPackageContents();

		// Initialize created meta-data
		theJPAMappingPackage.initializePackageContents();

		// Mark meta-data to indicate it can't be changed
		theJPAMappingPackage.freeze();

		// Update the registry and return the package
		EPackage.Registry.INSTANCE.put(JPAMappingPackage.eNS_URI, theJPAMappingPackage);
		return theJPAMappingPackage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getJpaMappingConfig() {
		return jpaMappingConfigEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getJpaMappingConfig_Name() {
		return (EAttribute)jpaMappingConfigEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getJpaMappingConfig_TargetModelNsUri() {
		return (EAttribute)jpaMappingConfigEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getJpaMappingConfig_DataSource() {
		return (EReference)jpaMappingConfigEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getJpaMappingConfig_TableMappings() {
		return (EReference)jpaMappingConfigEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getDataSourceConfig() {
		return dataSourceConfigEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataSourceConfig_DriverClass() {
		return (EAttribute)dataSourceConfigEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataSourceConfig_JdbcUrl() {
		return (EAttribute)dataSourceConfigEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataSourceConfig_Username() {
		return (EAttribute)dataSourceConfigEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataSourceConfig_PasswordRef() {
		return (EAttribute)dataSourceConfigEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataSourceConfig_PoolSize() {
		return (EAttribute)dataSourceConfigEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getDataSourceConfig_Dialect() {
		return (EAttribute)dataSourceConfigEClass.getEStructuralFeatures().get(5);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getTableMapping() {
		return tableMappingEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getTableMapping_ClassName() {
		return (EAttribute)tableMappingEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getTableMapping_TableName() {
		return (EAttribute)tableMappingEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getTableMapping_Schema() {
		return (EAttribute)tableMappingEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getTableMapping_ColumnMappings() {
		return (EReference)tableMappingEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EReference getTableMapping_JoinMappings() {
		return (EReference)tableMappingEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getColumnMapping() {
		return columnMappingEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getColumnMapping_FeatureName() {
		return (EAttribute)columnMappingEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getColumnMapping_ColumnName() {
		return (EAttribute)columnMappingEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getColumnMapping_ColumnType() {
		return (EAttribute)columnMappingEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getColumnMapping_Nullable() {
		return (EAttribute)columnMappingEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getColumnMapping_PrimaryKey() {
		return (EAttribute)columnMappingEClass.getEStructuralFeatures().get(4);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EClass getJoinMapping() {
		return joinMappingEClass;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getJoinMapping_ReferenceName() {
		return (EAttribute)joinMappingEClass.getEStructuralFeatures().get(0);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getJoinMapping_JoinColumn() {
		return (EAttribute)joinMappingEClass.getEStructuralFeatures().get(1);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getJoinMapping_JoinType() {
		return (EAttribute)joinMappingEClass.getEStructuralFeatures().get(2);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EAttribute getJoinMapping_CascadeType() {
		return (EAttribute)joinMappingEClass.getEStructuralFeatures().get(3);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getSqlDialect() {
		return sqlDialectEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getJoinType() {
		return joinTypeEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EEnum getCascadeType() {
		return cascadeTypeEEnum;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public JPAMappingFactory getJPAMappingFactory() {
		return (JPAMappingFactory)getEFactoryInstance();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isCreated = false;

	/**
	 * Creates the meta-model objects for the package.  This method is
	 * guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void createPackageContents() {
		if (isCreated) return;
		isCreated = true;

		// Create classes and their features
		jpaMappingConfigEClass = createEClass(JPA_MAPPING_CONFIG);
		createEAttribute(jpaMappingConfigEClass, JPA_MAPPING_CONFIG__NAME);
		createEAttribute(jpaMappingConfigEClass, JPA_MAPPING_CONFIG__TARGET_MODEL_NS_URI);
		createEReference(jpaMappingConfigEClass, JPA_MAPPING_CONFIG__DATA_SOURCE);
		createEReference(jpaMappingConfigEClass, JPA_MAPPING_CONFIG__TABLE_MAPPINGS);

		dataSourceConfigEClass = createEClass(DATA_SOURCE_CONFIG);
		createEAttribute(dataSourceConfigEClass, DATA_SOURCE_CONFIG__DRIVER_CLASS);
		createEAttribute(dataSourceConfigEClass, DATA_SOURCE_CONFIG__JDBC_URL);
		createEAttribute(dataSourceConfigEClass, DATA_SOURCE_CONFIG__USERNAME);
		createEAttribute(dataSourceConfigEClass, DATA_SOURCE_CONFIG__PASSWORD_REF);
		createEAttribute(dataSourceConfigEClass, DATA_SOURCE_CONFIG__POOL_SIZE);
		createEAttribute(dataSourceConfigEClass, DATA_SOURCE_CONFIG__DIALECT);

		tableMappingEClass = createEClass(TABLE_MAPPING);
		createEAttribute(tableMappingEClass, TABLE_MAPPING__CLASS_NAME);
		createEAttribute(tableMappingEClass, TABLE_MAPPING__TABLE_NAME);
		createEAttribute(tableMappingEClass, TABLE_MAPPING__SCHEMA);
		createEReference(tableMappingEClass, TABLE_MAPPING__COLUMN_MAPPINGS);
		createEReference(tableMappingEClass, TABLE_MAPPING__JOIN_MAPPINGS);

		columnMappingEClass = createEClass(COLUMN_MAPPING);
		createEAttribute(columnMappingEClass, COLUMN_MAPPING__FEATURE_NAME);
		createEAttribute(columnMappingEClass, COLUMN_MAPPING__COLUMN_NAME);
		createEAttribute(columnMappingEClass, COLUMN_MAPPING__COLUMN_TYPE);
		createEAttribute(columnMappingEClass, COLUMN_MAPPING__NULLABLE);
		createEAttribute(columnMappingEClass, COLUMN_MAPPING__PRIMARY_KEY);

		joinMappingEClass = createEClass(JOIN_MAPPING);
		createEAttribute(joinMappingEClass, JOIN_MAPPING__REFERENCE_NAME);
		createEAttribute(joinMappingEClass, JOIN_MAPPING__JOIN_COLUMN);
		createEAttribute(joinMappingEClass, JOIN_MAPPING__JOIN_TYPE);
		createEAttribute(joinMappingEClass, JOIN_MAPPING__CASCADE_TYPE);

		// Create enums
		sqlDialectEEnum = createEEnum(SQL_DIALECT);
		joinTypeEEnum = createEEnum(JOIN_TYPE);
		cascadeTypeEEnum = createEEnum(CASCADE_TYPE);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	private boolean isInitialized = false;

	/**
	 * Complete the initialization of the package and its meta-model.  This
	 * method is guarded to have no affect on any invocation but its first.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public void initializePackageContents() {
		if (isInitialized) return;
		isInitialized = true;

		// Initialize package
		setName(eNAME);
		setNsPrefix(eNS_PREFIX);
		setNsURI(eNS_URI);

		// Create type parameters

		// Set bounds for type parameters

		// Add supertypes to classes

		// Initialize classes, features, and operations; add parameters
		initEClass(jpaMappingConfigEClass, JpaMappingConfig.class, "JpaMappingConfig", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getJpaMappingConfig_Name(), ecorePackage.getEString(), "name", null, 0, 1, JpaMappingConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getJpaMappingConfig_TargetModelNsUri(), ecorePackage.getEString(), "targetModelNsUri", null, 0, 1, JpaMappingConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getJpaMappingConfig_DataSource(), this.getDataSourceConfig(), null, "dataSource", null, 0, 1, JpaMappingConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getJpaMappingConfig_TableMappings(), this.getTableMapping(), null, "tableMappings", null, 0, -1, JpaMappingConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(dataSourceConfigEClass, DataSourceConfig.class, "DataSourceConfig", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getDataSourceConfig_DriverClass(), ecorePackage.getEString(), "driverClass", null, 0, 1, DataSourceConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataSourceConfig_JdbcUrl(), ecorePackage.getEString(), "jdbcUrl", null, 0, 1, DataSourceConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataSourceConfig_Username(), ecorePackage.getEString(), "username", null, 0, 1, DataSourceConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataSourceConfig_PasswordRef(), ecorePackage.getEString(), "passwordRef", null, 0, 1, DataSourceConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataSourceConfig_PoolSize(), ecorePackage.getEInt(), "poolSize", null, 0, 1, DataSourceConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getDataSourceConfig_Dialect(), this.getSqlDialect(), "dialect", null, 0, 1, DataSourceConfig.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(tableMappingEClass, TableMapping.class, "TableMapping", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getTableMapping_ClassName(), ecorePackage.getEString(), "className", null, 0, 1, TableMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTableMapping_TableName(), ecorePackage.getEString(), "tableName", null, 0, 1, TableMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getTableMapping_Schema(), ecorePackage.getEString(), "schema", "PUBLIC", 0, 1, TableMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getTableMapping_ColumnMappings(), this.getColumnMapping(), null, "columnMappings", null, 0, -1, TableMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEReference(getTableMapping_JoinMappings(), this.getJoinMapping(), null, "joinMappings", null, 0, -1, TableMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, IS_COMPOSITE, !IS_RESOLVE_PROXIES, !IS_UNSETTABLE, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(columnMappingEClass, ColumnMapping.class, "ColumnMapping", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getColumnMapping_FeatureName(), ecorePackage.getEString(), "featureName", null, 0, 1, ColumnMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getColumnMapping_ColumnName(), ecorePackage.getEString(), "columnName", null, 0, 1, ColumnMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getColumnMapping_ColumnType(), ecorePackage.getEString(), "columnType", null, 0, 1, ColumnMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getColumnMapping_Nullable(), ecorePackage.getEBoolean(), "nullable", null, 0, 1, ColumnMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getColumnMapping_PrimaryKey(), ecorePackage.getEBoolean(), "primaryKey", null, 0, 1, ColumnMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		initEClass(joinMappingEClass, JoinMapping.class, "JoinMapping", !IS_ABSTRACT, !IS_INTERFACE, IS_GENERATED_INSTANCE_CLASS);
		initEAttribute(getJoinMapping_ReferenceName(), ecorePackage.getEString(), "referenceName", null, 0, 1, JoinMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getJoinMapping_JoinColumn(), ecorePackage.getEString(), "joinColumn", null, 0, 1, JoinMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getJoinMapping_JoinType(), this.getJoinType(), "joinType", null, 0, 1, JoinMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);
		initEAttribute(getJoinMapping_CascadeType(), this.getCascadeType(), "cascadeType", null, 0, -1, JoinMapping.class, !IS_TRANSIENT, !IS_VOLATILE, IS_CHANGEABLE, !IS_UNSETTABLE, !IS_ID, IS_UNIQUE, !IS_DERIVED, IS_ORDERED);

		// Initialize enums and add enum literals
		initEEnum(sqlDialectEEnum, SqlDialect.class, "SqlDialect");
		addEEnumLiteral(sqlDialectEEnum, SqlDialect.H2);
		addEEnumLiteral(sqlDialectEEnum, SqlDialect.POSTGRES);
		addEEnumLiteral(sqlDialectEEnum, SqlDialect.MYSQL);
		addEEnumLiteral(sqlDialectEEnum, SqlDialect.ORACLE);
		addEEnumLiteral(sqlDialectEEnum, SqlDialect.SQLSERVER);
		addEEnumLiteral(sqlDialectEEnum, SqlDialect.OTHER);

		initEEnum(joinTypeEEnum, JoinType.class, "JoinType");
		addEEnumLiteral(joinTypeEEnum, JoinType.FOREIGN_KEY);
		addEEnumLiteral(joinTypeEEnum, JoinType.EMBEDDED);

		initEEnum(cascadeTypeEEnum, CascadeType.class, "CascadeType");
		addEEnumLiteral(cascadeTypeEEnum, CascadeType.ALL);
		addEEnumLiteral(cascadeTypeEEnum, CascadeType.PERSIST);
		addEEnumLiteral(cascadeTypeEEnum, CascadeType.MERGE);
		addEEnumLiteral(cascadeTypeEEnum, CascadeType.REMOVE);
		addEEnumLiteral(cascadeTypeEEnum, CascadeType.REFRESH);
		addEEnumLiteral(cascadeTypeEEnum, CascadeType.DETACH);

		// Create resource
		createResource(eNS_URI);

		// Create annotations
		// Version
		createVersionAnnotations();
		// http://www.eclipse.org/emf/2002/GenModel
		createGenModelAnnotations();
	}

	/**
	 * Initializes the annotations for <b>Version</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createVersionAnnotations() {
		String source = "Version";
		addAnnotation
		  (this,
		   source,
		   new String[] {
			   "value", "1.0"
		   });
	}

	/**
	 * Initializes the annotations for <b>http://www.eclipse.org/emf/2002/GenModel</b>.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected void createGenModelAnnotations() {
		String source = "http://www.eclipse.org/emf/2002/GenModel";
		addAnnotation
		  (this,
		   source,
		   new String[] {
			   "complianceLevel", "17.0",
			   "oSGiCompatible", "true",
			   "basePackage", "org.eclipse.fennec.data.atlas.mapping.model",
			   "resource", "XMI",
			   "documentation", "Root package for the JPA mapping configuration model. Describes how EMF model classes and features are mapped to relational database tables and columns."
		   });
		addAnnotation
		  (jpaMappingConfigEClass,
		   source,
		   new String[] {
			   "documentation", "Top-level configuration that binds a target EMF model (identified by its namespace URI) to a data source and a set of table mappings."
		   });
		addAnnotation
		  (getJpaMappingConfig_Name(),
		   source,
		   new String[] {
			   "documentation", "The configuration name/id."
		   });
		addAnnotation
		  (getJpaMappingConfig_TargetModelNsUri(),
		   source,
		   new String[] {
			   "documentation", "The namespace URI of the EMF EPackage that this mapping configuration targets (e.g. \'http://example.org/mymodel/1.0.0\')."
		   });
		addAnnotation
		  (getJpaMappingConfig_DataSource(),
		   source,
		   new String[] {
			   "documentation", "The JDBC data source configuration used to connect to the relational database."
		   });
		addAnnotation
		  (getJpaMappingConfig_TableMappings(),
		   source,
		   new String[] {
			   "documentation", "The list of table mappings that describe how each EMF class is persisted in the database."
		   });
		addAnnotation
		  (dataSourceConfigEClass,
		   source,
		   new String[] {
			   "documentation", "JDBC data source configuration, including connection URL, credentials, connection pool settings, and the SQL dialect to use."
		   });
		addAnnotation
		  (getDataSourceConfig_DriverClass(),
		   source,
		   new String[] {
			   "documentation", "Fully qualified class name of the JDBC driver (e.g. \'org.postgresql.Driver\')."
		   });
		addAnnotation
		  (getDataSourceConfig_JdbcUrl(),
		   source,
		   new String[] {
			   "documentation", "JDBC connection URL (e.g. \'jdbc:postgresql://localhost:5432/mydb\')."
		   });
		addAnnotation
		  (getDataSourceConfig_Username(),
		   source,
		   new String[] {
			   "documentation", "Database user name used to authenticate the JDBC connection."
		   });
		addAnnotation
		  (getDataSourceConfig_PasswordRef(),
		   source,
		   new String[] {
			   "documentation", "Reference key or environment variable name from which the database password is resolved at runtime, avoiding plain-text secrets in the configuration."
		   });
		addAnnotation
		  (getDataSourceConfig_PoolSize(),
		   source,
		   new String[] {
			   "documentation", "Maximum number of connections maintained in the connection pool."
		   });
		addAnnotation
		  (getDataSourceConfig_Dialect(),
		   source,
		   new String[] {
			   "documentation", "SQL dialect of the target database, used to generate dialect-specific SQL statements."
		   });
		addAnnotation
		  (tableMappingEClass,
		   source,
		   new String[] {
			   "documentation", "Describes the mapping between a single EMF EClass and a relational database table, including its column and join mappings."
		   });
		addAnnotation
		  (getTableMapping_ClassName(),
		   source,
		   new String[] {
			   "documentation", "Full EMF URI of the EClass being mapped, in the form \'nsURI#//ClassName\' (e.g. \'https://dg.de/1.0#//Customer\')."
		   });
		addAnnotation
		  (getTableMapping_TableName(),
		   source,
		   new String[] {
			   "documentation", "Name of the database table to which the EMF class is mapped."
		   });
		addAnnotation
		  (getTableMapping_Schema(),
		   source,
		   new String[] {
			   "documentation", "Optional database schema that qualifies the table name (e.g. \'public\' in PostgreSQL). Leave empty to use the default schema."
		   });
		addAnnotation
		  (getTableMapping_ColumnMappings(),
		   source,
		   new String[] {
			   "documentation", "Mappings from EMF scalar features (EAttributes) to individual database columns."
		   });
		addAnnotation
		  (getTableMapping_JoinMappings(),
		   source,
		   new String[] {
			   "documentation", "Mappings from EMF references (EReferences) to relational join strategies (foreign key or embedded)."
		   });
		addAnnotation
		  (columnMappingEClass,
		   source,
		   new String[] {
			   "documentation", "Describes the mapping between a single EMF EAttribute and a relational database column."
		   });
		addAnnotation
		  (getColumnMapping_FeatureName(),
		   source,
		   new String[] {
			   "documentation", "Name of the EMF EAttribute being mapped (as declared in the EClass)."
		   });
		addAnnotation
		  (getColumnMapping_ColumnName(),
		   source,
		   new String[] {
			   "documentation", "Name of the database column that stores the attribute value."
		   });
		addAnnotation
		  (getColumnMapping_ColumnType(),
		   source,
		   new String[] {
			   "documentation", "SQL data type of the column (e.g. \'VARCHAR(255)\', \'INTEGER\', \'TIMESTAMP\'). If omitted, the type is inferred from the EAttribute type."
		   });
		addAnnotation
		  (getColumnMapping_Nullable(),
		   source,
		   new String[] {
			   "documentation", "Whether the column accepts NULL values. Set to false to enforce a NOT NULL constraint."
		   });
		addAnnotation
		  (getColumnMapping_PrimaryKey(),
		   source,
		   new String[] {
			   "documentation", "Whether this column is the primary key of the table. Exactly one ColumnMapping per TableMapping should have this set to true. The converter will map it to a JPA @Id field in the EORM."
		   });
		addAnnotation
		  (joinMappingEClass,
		   source,
		   new String[] {
			   "documentation", "Describes how an EMF EReference is persisted relationally, either via a foreign key column or by embedding the referenced object\'s columns into the owning table."
		   });
		addAnnotation
		  (getJoinMapping_ReferenceName(),
		   source,
		   new String[] {
			   "documentation", "Name of the EMF EReference being mapped (as declared in the EClass)."
		   });
		addAnnotation
		  (getJoinMapping_JoinColumn(),
		   source,
		   new String[] {
			   "documentation", "Name of the foreign key column in the owning table (for FOREIGN_KEY join type), or the column prefix used when embedding referenced fields (for EMBEDDED join type)."
		   });
		addAnnotation
		  (getJoinMapping_JoinType(),
		   source,
		   new String[] {
			   "documentation", "Strategy used to represent the relationship in the database: FOREIGN_KEY for a standard FK column, or EMBEDDED to inline the referenced object\'s fields."
		   });
		addAnnotation
		  (getJoinMapping_CascadeType(),
		   source,
		   new String[] {
			   "documentation", "JPA cascade operations that are propagated from the owning entity to the associated entity. Multiple values can be combined (e.g. PERSIST + MERGE). Use ALL to enable all operations at once."
		   });
		addAnnotation
		  (sqlDialectEEnum,
		   source,
		   new String[] {
			   "documentation", "Identifies the SQL dialect of the target relational database, allowing the runtime to generate dialect-specific DDL and DML statements."
		   });
		addAnnotation
		  (sqlDialectEEnum.getELiterals().get(0),
		   source,
		   new String[] {
			   "documentation", "H2 in-memory/embedded database (typically used for testing)."
		   });
		addAnnotation
		  (sqlDialectEEnum.getELiterals().get(1),
		   source,
		   new String[] {
			   "documentation", "PostgreSQL relational database."
		   });
		addAnnotation
		  (sqlDialectEEnum.getELiterals().get(2),
		   source,
		   new String[] {
			   "documentation", "MySQL / MariaDB relational database."
		   });
		addAnnotation
		  (sqlDialectEEnum.getELiterals().get(3),
		   source,
		   new String[] {
			   "documentation", "Oracle Database."
		   });
		addAnnotation
		  (sqlDialectEEnum.getELiterals().get(4),
		   source,
		   new String[] {
			   "documentation", "Microsoft SQL Server."
		   });
		addAnnotation
		  (sqlDialectEEnum.getELiterals().get(5),
		   source,
		   new String[] {
			   "documentation", "Any other SQL database not explicitly listed. Generic SQL will be generated."
		   });
		addAnnotation
		  (joinTypeEEnum,
		   source,
		   new String[] {
			   "documentation", "Strategy used to represent an EMF EReference relationship in a relational database table."
		   });
		addAnnotation
		  (joinTypeEEnum.getELiterals().get(0),
		   source,
		   new String[] {
			   "documentation", "The relationship is stored as a foreign key column in the owning table pointing to the primary key of the referenced table."
		   });
		addAnnotation
		  (joinTypeEEnum.getELiterals().get(1),
		   source,
		   new String[] {
			   "documentation", "The referenced object\'s fields are inlined as columns directly into the owning table (JPA @Embedded semantics)."
		   });
		addAnnotation
		  (cascadeTypeEEnum,
		   source,
		   new String[] {
			   "documentation", "Mirrors jakarta.persistence.CascadeType. Defines which JPA entity lifecycle operations are automatically propagated from the owning entity to the associated entity."
		   });
		addAnnotation
		  (cascadeTypeEEnum.getELiterals().get(0),
		   source,
		   new String[] {
			   "documentation", "Shorthand for enabling all cascade operations (PERSIST, MERGE, REMOVE, REFRESH, DETACH)."
		   });
		addAnnotation
		  (cascadeTypeEEnum.getELiterals().get(1),
		   source,
		   new String[] {
			   "documentation", "Cascades EntityManager.persist(): when the owning entity is persisted, the associated entity is also persisted."
		   });
		addAnnotation
		  (cascadeTypeEEnum.getELiterals().get(2),
		   source,
		   new String[] {
			   "documentation", "Cascades EntityManager.merge(): when the owning entity is merged, the associated entity is also merged."
		   });
		addAnnotation
		  (cascadeTypeEEnum.getELiterals().get(3),
		   source,
		   new String[] {
			   "documentation", "Cascades EntityManager.remove(): when the owning entity is deleted, the associated entity is also deleted."
		   });
		addAnnotation
		  (cascadeTypeEEnum.getELiterals().get(4),
		   source,
		   new String[] {
			   "documentation", "Cascades EntityManager.refresh(): when the owning entity is refreshed from the database, the associated entity is also refreshed."
		   });
		addAnnotation
		  (cascadeTypeEEnum.getELiterals().get(5),
		   source,
		   new String[] {
			   "documentation", "Cascades EntityManager.detach(): when the owning entity is detached from the persistence context, the associated entity is also detached."
		   });
	}

} //JPAMappingPackageImpl
