/*
 */
package org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Create Artifact</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * When you first create an artifact in Apicurio, this is the object you should use.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.CreateArtifact#getFirstVersion <em>First Version</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage#getCreateArtifact()
 * @model
 * @generated
 */
@ProviderType
public interface CreateArtifact extends Artifact {
	/**
	 * Returns the value of the '<em><b>First Version</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>First Version</em>' containment reference.
	 * @see #setFirstVersion(Version)
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage#getCreateArtifact_FirstVersion()
	 * @model containment="true"
	 * @generated
	 */
	Version getFirstVersion();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.CreateArtifact#getFirstVersion <em>First Version</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>First Version</em>' containment reference.
	 * @see #getFirstVersion()
	 * @generated
	 */
	void setFirstVersion(Version value);

} // CreateArtifact
