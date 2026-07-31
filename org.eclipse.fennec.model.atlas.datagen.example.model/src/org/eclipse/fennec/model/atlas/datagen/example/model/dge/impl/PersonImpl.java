/*
 */
package org.eclipse.fennec.model.atlas.datagen.example.model.dge.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EStructuralFeature;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.fennec.model.atlas.datagen.example.model.dge.Address;
import org.eclipse.fennec.model.atlas.datagen.example.model.dge.Company;
import org.eclipse.fennec.model.atlas.datagen.example.model.dge.DGPackage;
import org.eclipse.fennec.model.atlas.datagen.example.model.dge.Person;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Person</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.impl.PersonImpl#getFirstName <em>First Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.impl.PersonImpl#getLastName <em>Last Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.impl.PersonImpl#getEmail <em>Email</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.impl.PersonImpl#getPhone <em>Phone</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.impl.PersonImpl#getJobTitle <em>Job Title</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.impl.PersonImpl#getAddress <em>Address</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.impl.PersonImpl#getCompany <em>Company</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.datagen.example.model.dge.impl.PersonImpl#getFullName <em>Full Name</em>}</li>
 * </ul>
 *
 * @generated
 */
public class PersonImpl extends MinimalEObjectImpl.Container implements Person {
	/**
	 * The default value of the '{@link #getFirstName() <em>First Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFirstName()
	 * @generated
	 * @ordered
	 */
	protected static final String FIRST_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFirstName() <em>First Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFirstName()
	 * @generated
	 * @ordered
	 */
	protected String firstName = FIRST_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getLastName() <em>Last Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLastName()
	 * @generated
	 * @ordered
	 */
	protected static final String LAST_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getLastName() <em>Last Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getLastName()
	 * @generated
	 * @ordered
	 */
	protected String lastName = LAST_NAME_EDEFAULT;

	/**
	 * The default value of the '{@link #getEmail() <em>Email</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEmail()
	 * @generated
	 * @ordered
	 */
	protected static final String EMAIL_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getEmail() <em>Email</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getEmail()
	 * @generated
	 * @ordered
	 */
	protected String email = EMAIL_EDEFAULT;

	/**
	 * The default value of the '{@link #getPhone() <em>Phone</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPhone()
	 * @generated
	 * @ordered
	 */
	protected static final String PHONE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getPhone() <em>Phone</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPhone()
	 * @generated
	 * @ordered
	 */
	protected String phone = PHONE_EDEFAULT;

	/**
	 * The default value of the '{@link #getJobTitle() <em>Job Title</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getJobTitle()
	 * @generated
	 * @ordered
	 */
	protected static final String JOB_TITLE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getJobTitle() <em>Job Title</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getJobTitle()
	 * @generated
	 * @ordered
	 */
	protected String jobTitle = JOB_TITLE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getAddress() <em>Address</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getAddress()
	 * @generated
	 * @ordered
	 */
	protected Address address;

	/**
	 * The cached value of the '{@link #getCompany() <em>Company</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCompany()
	 * @generated
	 * @ordered
	 */
	protected Company company;

	/**
	 * The cached setting delegate for the '{@link #getFullName() <em>Full Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFullName()
	 * @generated
	 * @ordered
	 */
	protected EStructuralFeature.Internal.SettingDelegate FULL_NAME__ESETTING_DELEGATE = ((EStructuralFeature.Internal)DGPackage.Literals.PERSON__FULL_NAME).getSettingDelegate();

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PersonImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return DGPackage.Literals.PERSON;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getFirstName() {
		return firstName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFirstName(String newFirstName) {
		String oldFirstName = firstName;
		firstName = newFirstName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DGPackage.PERSON__FIRST_NAME, oldFirstName, firstName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getLastName() {
		return lastName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLastName(String newLastName) {
		String oldLastName = lastName;
		lastName = newLastName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DGPackage.PERSON__LAST_NAME, oldLastName, lastName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getEmail() {
		return email;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setEmail(String newEmail) {
		String oldEmail = email;
		email = newEmail;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DGPackage.PERSON__EMAIL, oldEmail, email));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getPhone() {
		return phone;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPhone(String newPhone) {
		String oldPhone = phone;
		phone = newPhone;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DGPackage.PERSON__PHONE, oldPhone, phone));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getJobTitle() {
		return jobTitle;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setJobTitle(String newJobTitle) {
		String oldJobTitle = jobTitle;
		jobTitle = newJobTitle;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DGPackage.PERSON__JOB_TITLE, oldJobTitle, jobTitle));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Address getAddress() {
		if (address != null && address.eIsProxy()) {
			InternalEObject oldAddress = (InternalEObject)address;
			address = (Address)eResolveProxy(oldAddress);
			if (address != oldAddress) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, DGPackage.PERSON__ADDRESS, oldAddress, address));
			}
		}
		return address;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Address basicGetAddress() {
		return address;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setAddress(Address newAddress) {
		Address oldAddress = address;
		address = newAddress;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DGPackage.PERSON__ADDRESS, oldAddress, address));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Company getCompany() {
		if (company != null && company.eIsProxy()) {
			InternalEObject oldCompany = (InternalEObject)company;
			company = (Company)eResolveProxy(oldCompany);
			if (company != oldCompany) {
				if (eNotificationRequired())
					eNotify(new ENotificationImpl(this, Notification.RESOLVE, DGPackage.PERSON__COMPANY, oldCompany, company));
			}
		}
		return company;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public Company basicGetCompany() {
		return company;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCompany(Company newCompany) {
		Company oldCompany = company;
		company = newCompany;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, DGPackage.PERSON__COMPANY, oldCompany, company));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getFullName() {
		return (String)FULL_NAME__ESETTING_DELEGATE.dynamicGet(this, null, 0, true, false);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case DGPackage.PERSON__FIRST_NAME:
				return getFirstName();
			case DGPackage.PERSON__LAST_NAME:
				return getLastName();
			case DGPackage.PERSON__EMAIL:
				return getEmail();
			case DGPackage.PERSON__PHONE:
				return getPhone();
			case DGPackage.PERSON__JOB_TITLE:
				return getJobTitle();
			case DGPackage.PERSON__ADDRESS:
				if (resolve) return getAddress();
				return basicGetAddress();
			case DGPackage.PERSON__COMPANY:
				if (resolve) return getCompany();
				return basicGetCompany();
			case DGPackage.PERSON__FULL_NAME:
				return getFullName();
		}
		return super.eGet(featureID, resolve, coreType);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eSet(int featureID, Object newValue) {
		switch (featureID) {
			case DGPackage.PERSON__FIRST_NAME:
				setFirstName((String)newValue);
				return;
			case DGPackage.PERSON__LAST_NAME:
				setLastName((String)newValue);
				return;
			case DGPackage.PERSON__EMAIL:
				setEmail((String)newValue);
				return;
			case DGPackage.PERSON__PHONE:
				setPhone((String)newValue);
				return;
			case DGPackage.PERSON__JOB_TITLE:
				setJobTitle((String)newValue);
				return;
			case DGPackage.PERSON__ADDRESS:
				setAddress((Address)newValue);
				return;
			case DGPackage.PERSON__COMPANY:
				setCompany((Company)newValue);
				return;
		}
		super.eSet(featureID, newValue);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void eUnset(int featureID) {
		switch (featureID) {
			case DGPackage.PERSON__FIRST_NAME:
				setFirstName(FIRST_NAME_EDEFAULT);
				return;
			case DGPackage.PERSON__LAST_NAME:
				setLastName(LAST_NAME_EDEFAULT);
				return;
			case DGPackage.PERSON__EMAIL:
				setEmail(EMAIL_EDEFAULT);
				return;
			case DGPackage.PERSON__PHONE:
				setPhone(PHONE_EDEFAULT);
				return;
			case DGPackage.PERSON__JOB_TITLE:
				setJobTitle(JOB_TITLE_EDEFAULT);
				return;
			case DGPackage.PERSON__ADDRESS:
				setAddress((Address)null);
				return;
			case DGPackage.PERSON__COMPANY:
				setCompany((Company)null);
				return;
		}
		super.eUnset(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean eIsSet(int featureID) {
		switch (featureID) {
			case DGPackage.PERSON__FIRST_NAME:
				return FIRST_NAME_EDEFAULT == null ? firstName != null : !FIRST_NAME_EDEFAULT.equals(firstName);
			case DGPackage.PERSON__LAST_NAME:
				return LAST_NAME_EDEFAULT == null ? lastName != null : !LAST_NAME_EDEFAULT.equals(lastName);
			case DGPackage.PERSON__EMAIL:
				return EMAIL_EDEFAULT == null ? email != null : !EMAIL_EDEFAULT.equals(email);
			case DGPackage.PERSON__PHONE:
				return PHONE_EDEFAULT == null ? phone != null : !PHONE_EDEFAULT.equals(phone);
			case DGPackage.PERSON__JOB_TITLE:
				return JOB_TITLE_EDEFAULT == null ? jobTitle != null : !JOB_TITLE_EDEFAULT.equals(jobTitle);
			case DGPackage.PERSON__ADDRESS:
				return address != null;
			case DGPackage.PERSON__COMPANY:
				return company != null;
			case DGPackage.PERSON__FULL_NAME:
				return FULL_NAME__ESETTING_DELEGATE.dynamicIsSet(this, null, 0);
		}
		return super.eIsSet(featureID);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String toString() {
		if (eIsProxy()) return super.toString();

		StringBuilder result = new StringBuilder(super.toString());
		result.append(" (firstName: ");
		result.append(firstName);
		result.append(", lastName: ");
		result.append(lastName);
		result.append(", email: ");
		result.append(email);
		result.append(", phone: ");
		result.append(phone);
		result.append(", jobTitle: ");
		result.append(jobTitle);
		result.append(')');
		return result.toString();
	}

} //PersonImpl
