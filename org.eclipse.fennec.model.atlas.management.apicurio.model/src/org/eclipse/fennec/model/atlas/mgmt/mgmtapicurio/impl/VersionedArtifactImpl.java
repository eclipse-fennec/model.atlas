/*
 */
package org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl;

import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.common.notify.NotificationChain;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.InternalEObject;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.FirstVersion;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.VersionedArtifact;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Versioned Artifact</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.VersionedArtifactImpl#getFirstVersion <em>First Version</em>}</li>
 * </ul>
 *
 * @generated
 */
public class VersionedArtifactImpl extends ArtifactImpl implements VersionedArtifact {
	/**
	 * The cached value of the '{@link #getFirstVersion() <em>First Version</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFirstVersion()
	 * @generated
	 * @ordered
	 */
	protected FirstVersion firstVersion;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected VersionedArtifactImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MgmtApicurioPackage.Literals.VERSIONED_ARTIFACT;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public FirstVersion getFirstVersion() {
		return firstVersion;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	public NotificationChain basicSetFirstVersion(FirstVersion newFirstVersion, NotificationChain msgs) {
		FirstVersion oldFirstVersion = firstVersion;
		firstVersion = newFirstVersion;
		if (eNotificationRequired()) {
			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, MgmtApicurioPackage.VERSIONED_ARTIFACT__FIRST_VERSION, oldFirstVersion, newFirstVersion);
			if (msgs == null) msgs = notification; else msgs.add(notification);
		}
		return msgs;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFirstVersion(FirstVersion newFirstVersion) {
		if (newFirstVersion != firstVersion) {
			NotificationChain msgs = null;
			if (firstVersion != null)
				msgs = ((InternalEObject)firstVersion).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - MgmtApicurioPackage.VERSIONED_ARTIFACT__FIRST_VERSION, null, msgs);
			if (newFirstVersion != null)
				msgs = ((InternalEObject)newFirstVersion).eInverseAdd(this, EOPPOSITE_FEATURE_BASE - MgmtApicurioPackage.VERSIONED_ARTIFACT__FIRST_VERSION, null, msgs);
			msgs = basicSetFirstVersion(newFirstVersion, msgs);
			if (msgs != null) msgs.dispatch();
		}
		else if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MgmtApicurioPackage.VERSIONED_ARTIFACT__FIRST_VERSION, newFirstVersion, newFirstVersion));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
		switch (featureID) {
			case MgmtApicurioPackage.VERSIONED_ARTIFACT__FIRST_VERSION:
				return basicSetFirstVersion(null, msgs);
		}
		return super.eInverseRemove(otherEnd, featureID, msgs);
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case MgmtApicurioPackage.VERSIONED_ARTIFACT__FIRST_VERSION:
				return getFirstVersion();
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
			case MgmtApicurioPackage.VERSIONED_ARTIFACT__FIRST_VERSION:
				setFirstVersion((FirstVersion)newValue);
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
			case MgmtApicurioPackage.VERSIONED_ARTIFACT__FIRST_VERSION:
				setFirstVersion((FirstVersion)null);
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
			case MgmtApicurioPackage.VERSIONED_ARTIFACT__FIRST_VERSION:
				return firstVersion != null;
		}
		return super.eIsSet(featureID);
	}

} //VersionedArtifactImpl
