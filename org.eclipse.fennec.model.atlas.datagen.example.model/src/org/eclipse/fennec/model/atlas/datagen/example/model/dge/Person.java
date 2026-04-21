/*
 */
package org.eclipse.fennec.model.atlas.datagen.example.model.dge;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Person</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.Person#getFirstName <em>First Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.Person#getLastName <em>Last Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.Person#getEmail <em>Email</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.Person#getPhone <em>Phone</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.Person#getJobTitle <em>Job Title</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.Person#getAddress <em>Address</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.Person#getCompany <em>Company</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.Person#getFullName <em>Full Name</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.datagen.example.model.dge.DGPackage#getPerson()
 * @model annotation="http://www.eclipse.org/fennec/m2x/ocl/1.0 ValidPhoneNumber='self.phone.matches(\'^\\\\d{10}$\')'"
 *        annotation="http://www.eclipse.org/emf/2002/Ecore constraints='ValidPhoneNumber'"
 * @generated
 */
@ProviderType
public interface Person extends EObject {
	/**
	 * Returns the value of the '<em><b>First Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>First Name</em>' attribute.
	 * @see #setFirstName(String)
	 * @see org.eclipse.fennec.model.atlas.datagen.example.model.dge.DGPackage#getPerson_FirstName()
	 * @model
	 * @generated
	 */
	String getFirstName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.Person#getFirstName <em>First Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>First Name</em>' attribute.
	 * @see #getFirstName()
	 * @generated
	 */
	void setFirstName(String value);

	/**
	 * Returns the value of the '<em><b>Last Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Last Name</em>' attribute.
	 * @see #setLastName(String)
	 * @see org.eclipse.fennec.model.atlas.datagen.example.model.dge.DGPackage#getPerson_LastName()
	 * @model
	 * @generated
	 */
	String getLastName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.Person#getLastName <em>Last Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Last Name</em>' attribute.
	 * @see #getLastName()
	 * @generated
	 */
	void setLastName(String value);

	/**
	 * Returns the value of the '<em><b>Email</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Email</em>' attribute.
	 * @see #setEmail(String)
	 * @see org.eclipse.fennec.model.atlas.datagen.example.model.dge.DGPackage#getPerson_Email()
	 * @model
	 * @generated
	 */
	String getEmail();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.Person#getEmail <em>Email</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Email</em>' attribute.
	 * @see #getEmail()
	 * @generated
	 */
	void setEmail(String value);

	/**
	 * Returns the value of the '<em><b>Phone</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Phone</em>' attribute.
	 * @see #setPhone(String)
	 * @see org.eclipse.fennec.model.atlas.datagen.example.model.dge.DGPackage#getPerson_Phone()
	 * @model
	 * @generated
	 */
	String getPhone();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.Person#getPhone <em>Phone</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Phone</em>' attribute.
	 * @see #getPhone()
	 * @generated
	 */
	void setPhone(String value);

	/**
	 * Returns the value of the '<em><b>Job Title</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Job Title</em>' attribute.
	 * @see #setJobTitle(String)
	 * @see org.eclipse.fennec.model.atlas.datagen.example.model.dge.DGPackage#getPerson_JobTitle()
	 * @model
	 * @generated
	 */
	String getJobTitle();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.Person#getJobTitle <em>Job Title</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Job Title</em>' attribute.
	 * @see #getJobTitle()
	 * @generated
	 */
	void setJobTitle(String value);

	/**
	 * Returns the value of the '<em><b>Address</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Address</em>' reference.
	 * @see #setAddress(Address)
	 * @see org.eclipse.fennec.model.atlas.datagen.example.model.dge.DGPackage#getPerson_Address()
	 * @model
	 * @generated
	 */
	Address getAddress();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.Person#getAddress <em>Address</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Address</em>' reference.
	 * @see #getAddress()
	 * @generated
	 */
	void setAddress(Address value);

	/**
	 * Returns the value of the '<em><b>Company</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Company</em>' reference.
	 * @see #setCompany(Company)
	 * @see org.eclipse.fennec.model.atlas.datagen.example.model.dge.DGPackage#getPerson_Company()
	 * @model
	 * @generated
	 */
	Company getCompany();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.Person#getCompany <em>Company</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Company</em>' reference.
	 * @see #getCompany()
	 * @generated
	 */
	void setCompany(Company value);

	/**
	 * Returns the value of the '<em><b>Full Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Full Name</em>' attribute.
	 * @see #setFullName(String)
	 * @see org.eclipse.fennec.model.atlas.datagen.example.model.dge.DGPackage#getPerson_FullName()
	 * @model derived="true"
	 *        annotation="http://www.eclipse.org/fennec/m2x/ocl/1.0 derivation='self.firstName.concat(\' \').concat(self.lastName)'"
	 * @generated
	 */
	String getFullName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.Person#getFullName <em>Full Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Full Name</em>' attribute.
	 * @see #getFullName()
	 * @generated
	 */
	void setFullName(String value);

} // Person
