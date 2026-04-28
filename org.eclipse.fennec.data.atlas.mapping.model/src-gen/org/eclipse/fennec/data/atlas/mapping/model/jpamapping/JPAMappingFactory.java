/*
 */
package org.eclipse.fennec.data.atlas.mapping.model.jpamapping;

import org.eclipse.emf.ecore.EFactory;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * The <b>Factory</b> for the model.
 * It provides a create method for each non-abstract class of the model.
 * <!-- end-user-doc -->
 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage
 * @generated
 */
@ProviderType
public interface JPAMappingFactory extends EFactory {
	/**
	 * The singleton instance of the factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	JPAMappingFactory eINSTANCE = org.eclipse.fennec.data.atlas.mapping.model.jpamapping.impl.JPAMappingFactoryImpl.init();

	/**
	 * Returns a new object of class '<em>Jpa Mapping Config</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Jpa Mapping Config</em>'.
	 * @generated
	 */
	JpaMappingConfig createJpaMappingConfig();

	/**
	 * Returns a new object of class '<em>Data Source Config</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Data Source Config</em>'.
	 * @generated
	 */
	DataSourceConfig createDataSourceConfig();

	/**
	 * Returns a new object of class '<em>Table Mapping</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Table Mapping</em>'.
	 * @generated
	 */
	TableMapping createTableMapping();

	/**
	 * Returns a new object of class '<em>Column Mapping</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Column Mapping</em>'.
	 * @generated
	 */
	ColumnMapping createColumnMapping();

	/**
	 * Returns a new object of class '<em>Join Mapping</em>'.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return a new object of class '<em>Join Mapping</em>'.
	 * @generated
	 */
	JoinMapping createJoinMapping();

	/**
	 * Returns the package supported by this factory.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the package supported by this factory.
	 * @generated
	 */
	JPAMappingPackage getJPAMappingPackage();

} //JPAMappingFactory
