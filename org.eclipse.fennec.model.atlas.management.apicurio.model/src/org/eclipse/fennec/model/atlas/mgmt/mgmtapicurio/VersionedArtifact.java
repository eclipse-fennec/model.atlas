/*
 */
package org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Versioned Artifact</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.VersionedArtifact#getFirstVersion <em>First Version</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage#getVersionedArtifact()
 * @model
 * @generated
 */
@ProviderType
public interface VersionedArtifact extends Artifact {
	/**
	 * Returns the value of the '<em><b>First Version</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>First Version</em>' containment reference.
	 * @see #setFirstVersion(FirstVersion)
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage#getVersionedArtifact_FirstVersion()
	 * @model containment="true"
	 * @generated
	 */
	FirstVersion getFirstVersion();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.VersionedArtifact#getFirstVersion <em>First Version</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>First Version</em>' containment reference.
	 * @see #getFirstVersion()
	 * @generated
	 */
	void setFirstVersion(FirstVersion value);

} // VersionedArtifact
