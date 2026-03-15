/*
 */
package org.eclipse.fennec.model.atlas.datagen.example.model.dge;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Company</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.Company#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.Company#getIndustry <em>Industry</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.Company#getUrl <em>Url</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.Company#getAddress <em>Address</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.Company#getEmployees <em>Employees</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.datagen.example.model.dge.DGPackage#getCompany()
 * @model
 * @generated
 */
@ProviderType
public interface Company extends EObject {
	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.model.atlas.datagen.example.model.dge.DGPackage#getCompany_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.Company#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Industry</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Industry</em>' attribute.
	 * @see #setIndustry(String)
	 * @see org.eclipse.fennec.model.atlas.datagen.example.model.dge.DGPackage#getCompany_Industry()
	 * @model
	 * @generated
	 */
	String getIndustry();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.Company#getIndustry <em>Industry</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Industry</em>' attribute.
	 * @see #getIndustry()
	 * @generated
	 */
	void setIndustry(String value);

	/**
	 * Returns the value of the '<em><b>Url</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Url</em>' attribute.
	 * @see #setUrl(String)
	 * @see org.eclipse.fennec.model.atlas.datagen.example.model.dge.DGPackage#getCompany_Url()
	 * @model
	 * @generated
	 */
	String getUrl();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.Company#getUrl <em>Url</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Url</em>' attribute.
	 * @see #getUrl()
	 * @generated
	 */
	void setUrl(String value);

	/**
	 * Returns the value of the '<em><b>Address</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Address</em>' containment reference.
	 * @see #setAddress(Address)
	 * @see org.eclipse.fennec.model.atlas.datagen.example.model.dge.DGPackage#getCompany_Address()
	 * @model containment="true"
	 * @generated
	 */
	Address getAddress();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.Company#getAddress <em>Address</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Address</em>' containment reference.
	 * @see #getAddress()
	 * @generated
	 */
	void setAddress(Address value);

	/**
	 * Returns the value of the '<em><b>Employees</b></em>' reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.Person}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Employees</em>' reference list.
	 * @see org.eclipse.fennec.model.atlas.datagen.example.model.dge.DGPackage#getCompany_Employees()
	 * @model
	 * @generated
	 */
	EList<Person> getEmployees();

} // Company
