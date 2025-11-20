/*
 */
package org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Searched Artifact</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchedArtifact#getCreatedOn <em>Created On</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchedArtifact#getOwner <em>Owner</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchedArtifact#getModifiedOn <em>Modified On</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchedArtifact#getModifiedBy <em>Modified By</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchedArtifact#getGroupId <em>Group Id</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage#getSearchedArtifact()
 * @model
 * @generated
 */
@ProviderType
public interface SearchedArtifact extends Artifact {
	/**
	 * Returns the value of the '<em><b>Created On</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Created On</em>' attribute.
	 * @see #setCreatedOn(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage#getSearchedArtifact_CreatedOn()
	 * @model required="true"
	 * @generated
	 */
	String getCreatedOn();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchedArtifact#getCreatedOn <em>Created On</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Created On</em>' attribute.
	 * @see #getCreatedOn()
	 * @generated
	 */
	void setCreatedOn(String value);

	/**
	 * Returns the value of the '<em><b>Owner</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Owner</em>' attribute.
	 * @see #setOwner(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage#getSearchedArtifact_Owner()
	 * @model required="true"
	 * @generated
	 */
	String getOwner();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchedArtifact#getOwner <em>Owner</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Owner</em>' attribute.
	 * @see #getOwner()
	 * @generated
	 */
	void setOwner(String value);

	/**
	 * Returns the value of the '<em><b>Modified On</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Modified On</em>' attribute.
	 * @see #setModifiedOn(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage#getSearchedArtifact_ModifiedOn()
	 * @model
	 * @generated
	 */
	String getModifiedOn();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchedArtifact#getModifiedOn <em>Modified On</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Modified On</em>' attribute.
	 * @see #getModifiedOn()
	 * @generated
	 */
	void setModifiedOn(String value);

	/**
	 * Returns the value of the '<em><b>Modified By</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Modified By</em>' attribute.
	 * @see #setModifiedBy(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage#getSearchedArtifact_ModifiedBy()
	 * @model
	 * @generated
	 */
	String getModifiedBy();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchedArtifact#getModifiedBy <em>Modified By</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Modified By</em>' attribute.
	 * @see #getModifiedBy()
	 * @generated
	 */
	void setModifiedBy(String value);

	/**
	 * Returns the value of the '<em><b>Group Id</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Group Id</em>' attribute.
	 * @see #setGroupId(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage#getSearchedArtifact_GroupId()
	 * @model
	 * @generated
	 */
	String getGroupId();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchedArtifact#getGroupId <em>Group Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Group Id</em>' attribute.
	 * @see #getGroupId()
	 * @generated
	 */
	void setGroupId(String value);

} // SearchedArtifact
