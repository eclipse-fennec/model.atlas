/*
 */
package org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;

import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.SearchedVersion;
import org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.VersionStateType;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Searched Version</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.SearchedVersionImpl#getGlobalId <em>Global Id</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.SearchedVersionImpl#getVersion <em>Version</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.SearchedVersionImpl#getContentId <em>Content Id</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.impl.SearchedVersionImpl#getState <em>State</em>}</li>
 * </ul>
 *
 * @generated
 */
public class SearchedVersionImpl extends ArtifactImpl implements SearchedVersion {
	/**
	 * The default value of the '{@link #getGlobalId() <em>Global Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGlobalId()
	 * @generated
	 * @ordered
	 */
	protected static final String GLOBAL_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getGlobalId() <em>Global Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getGlobalId()
	 * @generated
	 * @ordered
	 */
	protected String globalId = GLOBAL_ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getVersion() <em>Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVersion()
	 * @generated
	 * @ordered
	 */
	protected static final String VERSION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getVersion() <em>Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getVersion()
	 * @generated
	 * @ordered
	 */
	protected String version = VERSION_EDEFAULT;

	/**
	 * The default value of the '{@link #getContentId() <em>Content Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContentId()
	 * @generated
	 * @ordered
	 */
	protected static final String CONTENT_ID_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getContentId() <em>Content Id</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContentId()
	 * @generated
	 * @ordered
	 */
	protected String contentId = CONTENT_ID_EDEFAULT;

	/**
	 * The default value of the '{@link #getState() <em>State</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getState()
	 * @generated
	 * @ordered
	 */
	protected static final VersionStateType STATE_EDEFAULT = VersionStateType.ENABLED;

	/**
	 * The cached value of the '{@link #getState() <em>State</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getState()
	 * @generated
	 * @ordered
	 */
	protected VersionStateType state = STATE_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected SearchedVersionImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return MgmtApicurioPackage.Literals.SEARCHED_VERSION;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getGlobalId() {
		return globalId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setGlobalId(String newGlobalId) {
		String oldGlobalId = globalId;
		globalId = newGlobalId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MgmtApicurioPackage.SEARCHED_VERSION__GLOBAL_ID, oldGlobalId, globalId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getVersion() {
		return version;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setVersion(String newVersion) {
		String oldVersion = version;
		version = newVersion;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MgmtApicurioPackage.SEARCHED_VERSION__VERSION, oldVersion, version));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getContentId() {
		return contentId;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setContentId(String newContentId) {
		String oldContentId = contentId;
		contentId = newContentId;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MgmtApicurioPackage.SEARCHED_VERSION__CONTENT_ID, oldContentId, contentId));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public VersionStateType getState() {
		return state;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setState(VersionStateType newState) {
		VersionStateType oldState = state;
		state = newState == null ? STATE_EDEFAULT : newState;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, MgmtApicurioPackage.SEARCHED_VERSION__STATE, oldState, state));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case MgmtApicurioPackage.SEARCHED_VERSION__GLOBAL_ID:
				return getGlobalId();
			case MgmtApicurioPackage.SEARCHED_VERSION__VERSION:
				return getVersion();
			case MgmtApicurioPackage.SEARCHED_VERSION__CONTENT_ID:
				return getContentId();
			case MgmtApicurioPackage.SEARCHED_VERSION__STATE:
				return getState();
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
			case MgmtApicurioPackage.SEARCHED_VERSION__GLOBAL_ID:
				setGlobalId((String)newValue);
				return;
			case MgmtApicurioPackage.SEARCHED_VERSION__VERSION:
				setVersion((String)newValue);
				return;
			case MgmtApicurioPackage.SEARCHED_VERSION__CONTENT_ID:
				setContentId((String)newValue);
				return;
			case MgmtApicurioPackage.SEARCHED_VERSION__STATE:
				setState((VersionStateType)newValue);
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
			case MgmtApicurioPackage.SEARCHED_VERSION__GLOBAL_ID:
				setGlobalId(GLOBAL_ID_EDEFAULT);
				return;
			case MgmtApicurioPackage.SEARCHED_VERSION__VERSION:
				setVersion(VERSION_EDEFAULT);
				return;
			case MgmtApicurioPackage.SEARCHED_VERSION__CONTENT_ID:
				setContentId(CONTENT_ID_EDEFAULT);
				return;
			case MgmtApicurioPackage.SEARCHED_VERSION__STATE:
				setState(STATE_EDEFAULT);
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
			case MgmtApicurioPackage.SEARCHED_VERSION__GLOBAL_ID:
				return GLOBAL_ID_EDEFAULT == null ? globalId != null : !GLOBAL_ID_EDEFAULT.equals(globalId);
			case MgmtApicurioPackage.SEARCHED_VERSION__VERSION:
				return VERSION_EDEFAULT == null ? version != null : !VERSION_EDEFAULT.equals(version);
			case MgmtApicurioPackage.SEARCHED_VERSION__CONTENT_ID:
				return CONTENT_ID_EDEFAULT == null ? contentId != null : !CONTENT_ID_EDEFAULT.equals(contentId);
			case MgmtApicurioPackage.SEARCHED_VERSION__STATE:
				return state != STATE_EDEFAULT;
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
		result.append(" (globalId: ");
		result.append(globalId);
		result.append(", version: ");
		result.append(version);
		result.append(", contentId: ");
		result.append(contentId);
		result.append(", state: ");
		result.append(state);
		result.append(')');
		return result.toString();
	}

} //SearchedVersionImpl
