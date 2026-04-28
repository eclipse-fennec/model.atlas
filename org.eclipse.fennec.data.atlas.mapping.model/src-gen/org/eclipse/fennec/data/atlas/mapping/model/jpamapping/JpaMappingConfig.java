/*
 */
package org.eclipse.fennec.data.atlas.mapping.model.jpamapping;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Jpa Mapping Config</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Top-level configuration that binds a target EMF model (identified by its namespace URI) to a data source and a set of table mappings.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig#getTargetModelNsUri <em>Target Model Ns Uri</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig#getDataSource <em>Data Source</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig#getTableMappings <em>Table Mappings</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage#getJpaMappingConfig()
 * @model
 * @generated
 */
@ProviderType
public interface JpaMappingConfig extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The configuration name/id.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage#getJpaMappingConfig_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Target Model Ns Uri</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The namespace URI of the EMF EPackage that this mapping configuration targets (e.g. 'http://example.org/mymodel/1.0.0').
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Target Model Ns Uri</em>' attribute.
	 * @see #setTargetModelNsUri(String)
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage#getJpaMappingConfig_TargetModelNsUri()
	 * @model
	 * @generated
	 */
	String getTargetModelNsUri();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig#getTargetModelNsUri <em>Target Model Ns Uri</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Target Model Ns Uri</em>' attribute.
	 * @see #getTargetModelNsUri()
	 * @generated
	 */
	void setTargetModelNsUri(String value);

	/**
	 * Returns the value of the '<em><b>Data Source</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The JDBC data source configuration used to connect to the relational database.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Data Source</em>' containment reference.
	 * @see #setDataSource(DataSourceConfig)
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage#getJpaMappingConfig_DataSource()
	 * @model containment="true"
	 * @generated
	 */
	DataSourceConfig getDataSource();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JpaMappingConfig#getDataSource <em>Data Source</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Data Source</em>' containment reference.
	 * @see #getDataSource()
	 * @generated
	 */
	void setDataSource(DataSourceConfig value);

	/**
	 * Returns the value of the '<em><b>Table Mappings</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.TableMapping}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * The list of table mappings that describe how each EMF class is persisted in the database.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Table Mappings</em>' containment reference list.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage#getJpaMappingConfig_TableMappings()
	 * @model containment="true"
	 * @generated
	 */
	EList<TableMapping> getTableMappings();

} // JpaMappingConfig
