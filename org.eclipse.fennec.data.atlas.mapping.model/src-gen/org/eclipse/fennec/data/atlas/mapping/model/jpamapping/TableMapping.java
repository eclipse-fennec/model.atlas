/*
 */
package org.eclipse.fennec.data.atlas.mapping.model.jpamapping;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Table Mapping</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Describes the mapping between a single EMF EClass and a relational database table, including its column and join mappings.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.TableMapping#getClassName <em>Class Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.TableMapping#getTableName <em>Table Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.TableMapping#getSchema <em>Schema</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.TableMapping#getColumnMappings <em>Column Mappings</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.TableMapping#getJoinMappings <em>Join Mappings</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage#getTableMapping()
 * @model
 * @generated
 */
@ProviderType
public interface TableMapping extends EObject {
	/**
	 * Returns the value of the '<em><b>Class Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Full EMF URI of the EClass being mapped, in the form 'nsURI#//ClassName' (e.g. 'https://dg.de/1.0#//Customer').
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Class Name</em>' attribute.
	 * @see #setClassName(String)
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage#getTableMapping_ClassName()
	 * @model
	 * @generated
	 */
	String getClassName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.TableMapping#getClassName <em>Class Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Class Name</em>' attribute.
	 * @see #getClassName()
	 * @generated
	 */
	void setClassName(String value);

	/**
	 * Returns the value of the '<em><b>Table Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Name of the database table to which the EMF class is mapped.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Table Name</em>' attribute.
	 * @see #setTableName(String)
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage#getTableMapping_TableName()
	 * @model
	 * @generated
	 */
	String getTableName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.TableMapping#getTableName <em>Table Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Table Name</em>' attribute.
	 * @see #getTableName()
	 * @generated
	 */
	void setTableName(String value);

	/**
	 * Returns the value of the '<em><b>Schema</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Optional database schema that qualifies the table name (e.g. 'public' in PostgreSQL). Leave empty to use the default schema.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Schema</em>' attribute.
	 * @see #setSchema(String)
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage#getTableMapping_Schema()
	 * @model
	 * @generated
	 */
	String getSchema();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.TableMapping#getSchema <em>Schema</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Schema</em>' attribute.
	 * @see #getSchema()
	 * @generated
	 */
	void setSchema(String value);

	/**
	 * Returns the value of the '<em><b>Column Mappings</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.ColumnMapping}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Mappings from EMF scalar features (EAttributes) to individual database columns.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Column Mappings</em>' containment reference list.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage#getTableMapping_ColumnMappings()
	 * @model containment="true"
	 * @generated
	 */
	EList<ColumnMapping> getColumnMappings();

	/**
	 * Returns the value of the '<em><b>Join Mappings</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinMapping}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Mappings from EMF references (EReferences) to relational join strategies (foreign key or embedded).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Join Mappings</em>' containment reference list.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage#getTableMapping_JoinMappings()
	 * @model containment="true"
	 * @generated
	 */
	EList<JoinMapping> getJoinMappings();

} // TableMapping
