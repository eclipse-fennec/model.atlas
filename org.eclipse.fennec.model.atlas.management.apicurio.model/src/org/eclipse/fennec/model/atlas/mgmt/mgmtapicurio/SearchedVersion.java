/*
 */
package org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Searched Version</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchedVersion#getGlobalId <em>Global Id</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchedVersion#getVersion <em>Version</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchedVersion#getContentId <em>Content Id</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchedVersion#getState <em>State</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage#getSearchedVersion()
 * @model
 * @generated
 */
@ProviderType
public interface SearchedVersion extends Artifact {
	/**
	 * Returns the value of the '<em><b>Global Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Global Id</em>' attribute.
	 * @see #setGlobalId(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage#getSearchedVersion_GlobalId()
	 * @model required="true"
	 * @generated
	 */
	String getGlobalId();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchedVersion#getGlobalId <em>Global Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Global Id</em>' attribute.
	 * @see #getGlobalId()
	 * @generated
	 */
	void setGlobalId(String value);

	/**
	 * Returns the value of the '<em><b>Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Version</em>' attribute.
	 * @see #setVersion(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage#getSearchedVersion_Version()
	 * @model required="true"
	 * @generated
	 */
	String getVersion();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchedVersion#getVersion <em>Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Version</em>' attribute.
	 * @see #getVersion()
	 * @generated
	 */
	void setVersion(String value);

	/**
	 * Returns the value of the '<em><b>Content Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Content Id</em>' attribute.
	 * @see #setContentId(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage#getSearchedVersion_ContentId()
	 * @model required="true"
	 * @generated
	 */
	String getContentId();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchedVersion#getContentId <em>Content Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Content Id</em>' attribute.
	 * @see #getContentId()
	 * @generated
	 */
	void setContentId(String value);

	/**
	 * Returns the value of the '<em><b>State</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.VersionStateType}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>State</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.VersionStateType
	 * @see #setState(VersionStateType)
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage#getSearchedVersion_State()
	 * @model required="true"
	 * @generated
	 */
	VersionStateType getState();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchedVersion#getState <em>State</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>State</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.VersionStateType
	 * @see #getState()
	 * @generated
	 */
	void setState(VersionStateType value);

} // SearchedVersion
