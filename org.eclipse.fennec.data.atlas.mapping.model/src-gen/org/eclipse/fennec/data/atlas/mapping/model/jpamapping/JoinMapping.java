/*
 */
package org.eclipse.fennec.data.atlas.mapping.model.jpamapping;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Join Mapping</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * Describes how an EMF EReference is persisted relationally, either via a foreign key column or by embedding the referenced object's columns into the owning table.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinMapping#getReferenceName <em>Reference Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinMapping#getJoinColumn <em>Join Column</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinMapping#getJoinType <em>Join Type</em>}</li>
 *   <li>{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinMapping#getCascadeType <em>Cascade Type</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage#getJoinMapping()
 * @model
 * @generated
 */
@ProviderType
public interface JoinMapping extends EObject {
	/**
	 * Returns the value of the '<em><b>Reference Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Name of the EMF EReference being mapped (as declared in the EClass).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Reference Name</em>' attribute.
	 * @see #setReferenceName(String)
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage#getJoinMapping_ReferenceName()
	 * @model
	 * @generated
	 */
	String getReferenceName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinMapping#getReferenceName <em>Reference Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Reference Name</em>' attribute.
	 * @see #getReferenceName()
	 * @generated
	 */
	void setReferenceName(String value);

	/**
	 * Returns the value of the '<em><b>Join Column</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Name of the foreign key column in the owning table (for FOREIGN_KEY join type), or the column prefix used when embedding referenced fields (for EMBEDDED join type).
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Join Column</em>' attribute.
	 * @see #setJoinColumn(String)
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage#getJoinMapping_JoinColumn()
	 * @model
	 * @generated
	 */
	String getJoinColumn();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinMapping#getJoinColumn <em>Join Column</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Join Column</em>' attribute.
	 * @see #getJoinColumn()
	 * @generated
	 */
	void setJoinColumn(String value);

	/**
	 * Returns the value of the '<em><b>Join Type</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinType}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * Strategy used to represent the relationship in the database: FOREIGN_KEY for a standard FK column, or EMBEDDED to inline the referenced object's fields.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Join Type</em>' attribute.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinType
	 * @see #setJoinType(JoinType)
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage#getJoinMapping_JoinType()
	 * @model
	 * @generated
	 */
	JoinType getJoinType();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinMapping#getJoinType <em>Join Type</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Join Type</em>' attribute.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JoinType
	 * @see #getJoinType()
	 * @generated
	 */
	void setJoinType(JoinType value);

	/**
	 * Returns the value of the '<em><b>Cascade Type</b></em>' attribute list.
	 * The list contents are of type {@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.CascadeType}.
	 * The literals are from the enumeration {@link org.eclipse.fennec.data.atlas.mapping.model.jpamapping.CascadeType}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * JPA cascade operations that are propagated from the owning entity to the associated entity. Multiple values can be combined (e.g. PERSIST + MERGE). Use ALL to enable all operations at once.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Cascade Type</em>' attribute list.
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.CascadeType
	 * @see org.eclipse.fennec.data.atlas.mapping.model.jpamapping.JPAMappingPackage#getJoinMapping_CascadeType()
	 * @model
	 * @generated
	 */
	EList<CascadeType> getCascadeType();

} // JoinMapping
