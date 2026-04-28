/**
 */
package org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EDataType;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.EFactoryImpl;

import org.eclipse.emf.ecore.plugin.EcorePlugin;

import org.eclipse.fennec.data.atlas.mapping.model.jpamapping.*;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model <b>Factory</b>.
 * <!-- end-user-doc -->
 * @generated
 */
public class JPAMappingFactoryImpl extends EFactoryImpl implements JPAMappingFactory {
	/**
	 * Creates the default factory implementation.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public static JPAMappingFactory init() {
		try {
			JPAMappingFactory theJPAMappingFactory = (JPAMappingFactory)EPackage.Registry.INSTANCE.getEFactory(JPAMappingPackage.eNS_URI);
			if (theJPAMappingFactory != null) {
				return theJPAMappingFactory;
			}
		}
		catch (Exception exception) {
			EcorePlugin.INSTANCE.log(exception);
		}
		return new JPAMappingFactoryImpl();
	}

	/**
	 * Creates an instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public JPAMappingFactoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EObject create(EClass eClass) {
		switch (eClass.getClassifierID()) {
			case JPAMappingPackage.JPA_MAPPING_CONFIG: return createJpaMappingConfig();
			case JPAMappingPackage.DATA_SOURCE_CONFIG: return createDataSourceConfig();
			case JPAMappingPackage.TABLE_MAPPING: return createTableMapping();
			case JPAMappingPackage.COLUMN_MAPPING: return createColumnMapping();
			case JPAMappingPackage.JOIN_MAPPING: return createJoinMapping();
			default:
				throw new IllegalArgumentException("The class '" + eClass.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object createFromString(EDataType eDataType, String initialValue) {
		switch (eDataType.getClassifierID()) {
			case JPAMappingPackage.SQL_DIALECT:
				return createSqlDialectFromString(eDataType, initialValue);
			case JPAMappingPackage.JOIN_TYPE:
				return createJoinTypeFromString(eDataType, initialValue);
			case JPAMappingPackage.CASCADE_TYPE:
				return createCascadeTypeFromString(eDataType, initialValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String convertToString(EDataType eDataType, Object instanceValue) {
		switch (eDataType.getClassifierID()) {
			case JPAMappingPackage.SQL_DIALECT:
				return convertSqlDialectToString(eDataType, instanceValue);
			case JPAMappingPackage.JOIN_TYPE:
				return convertJoinTypeToString(eDataType, instanceValue);
			case JPAMappingPackage.CASCADE_TYPE:
				return convertCascadeTypeToString(eDataType, instanceValue);
			default:
				throw new IllegalArgumentException("The datatype '" + eDataType.getName() + "' is not a valid classifier");
		}
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public JpaMappingConfig createJpaMappingConfig() {
		JpaMappingConfigImpl jpaMappingConfig = new JpaMappingConfigImpl();
		return jpaMappingConfig;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public DataSourceConfig createDataSourceConfig() {
		DataSourceConfigImpl dataSourceConfig = new DataSourceConfigImpl();
		return dataSourceConfig;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public TableMapping createTableMapping() {
		TableMappingImpl tableMapping = new TableMappingImpl();
		return tableMapping;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ColumnMapping createColumnMapping() {
		ColumnMappingImpl columnMapping = new ColumnMappingImpl();
		return columnMapping;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public JoinMapping createJoinMapping() {
		JoinMappingImpl joinMapping = new JoinMappingImpl();
		return joinMapping;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public SqlDialect createSqlDialectFromString(EDataType eDataType, String initialValue) {
		SqlDialect result = SqlDialect.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertSqlDialectToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public JoinType createJoinTypeFromString(EDataType eDataType, String initialValue) {
		JoinType result = JoinType.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertJoinTypeToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public CascadeType createCascadeTypeFromString(EDataType eDataType, String initialValue) {
		CascadeType result = CascadeType.get(initialValue);
		if (result == null) throw new IllegalArgumentException("The value '" + initialValue + "' is not a valid enumerator of '" + eDataType.getName() + "'");
		return result;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public String convertCascadeTypeToString(EDataType eDataType, Object instanceValue) {
		return instanceValue == null ? null : instanceValue.toString();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public JPAMappingPackage getJPAMappingPackage() {
		return (JPAMappingPackage)getEPackage();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @deprecated
	 * @generated
	 */
	@Deprecated
	public static JPAMappingPackage getPackage() {
		return JPAMappingPackage.eINSTANCE;
	}

} //JPAMappingFactoryImpl
