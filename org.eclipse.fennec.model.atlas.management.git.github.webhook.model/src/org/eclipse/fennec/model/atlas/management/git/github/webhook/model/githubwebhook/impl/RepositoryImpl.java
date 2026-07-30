/*
 */
package org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.impl;

import org.eclipse.emf.common.notify.Notification;

import org.eclipse.emf.ecore.EClass;

import org.eclipse.emf.ecore.impl.ENotificationImpl;
import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.GithubWebhookPackage;
import org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.Repository;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Repository</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.impl.RepositoryImpl#getCloneUrl <em>Clone Url</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.management.git.github.webhook.model.githubwebhook.impl.RepositoryImpl#getFullName <em>Full Name</em>}</li>
 * </ul>
 *
 * @generated
 */
public class RepositoryImpl extends MinimalEObjectImpl.Container implements Repository {
	/**
	 * The default value of the '{@link #getCloneUrl() <em>Clone Url</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCloneUrl()
	 * @generated
	 * @ordered
	 */
	protected static final String CLONE_URL_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getCloneUrl() <em>Clone Url</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getCloneUrl()
	 * @generated
	 * @ordered
	 */
	protected String cloneUrl = CLONE_URL_EDEFAULT;

	/**
	 * The default value of the '{@link #getFullName() <em>Full Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFullName()
	 * @generated
	 * @ordered
	 */
	protected static final String FULL_NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getFullName() <em>Full Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getFullName()
	 * @generated
	 * @ordered
	 */
	protected String fullName = FULL_NAME_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected RepositoryImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return GithubWebhookPackage.Literals.REPOSITORY;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getCloneUrl() {
		return cloneUrl;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setCloneUrl(String newCloneUrl) {
		String oldCloneUrl = cloneUrl;
		cloneUrl = newCloneUrl;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GithubWebhookPackage.REPOSITORY__CLONE_URL, oldCloneUrl, cloneUrl));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getFullName() {
		return fullName;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setFullName(String newFullName) {
		String oldFullName = fullName;
		fullName = newFullName;
		if (eNotificationRequired())
			eNotify(new ENotificationImpl(this, Notification.SET, GithubWebhookPackage.REPOSITORY__FULL_NAME, oldFullName, fullName));
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case GithubWebhookPackage.REPOSITORY__CLONE_URL:
				return getCloneUrl();
			case GithubWebhookPackage.REPOSITORY__FULL_NAME:
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
			case GithubWebhookPackage.REPOSITORY__CLONE_URL:
				setCloneUrl((String)newValue);
				return;
			case GithubWebhookPackage.REPOSITORY__FULL_NAME:
				setFullName((String)newValue);
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
			case GithubWebhookPackage.REPOSITORY__CLONE_URL:
				setCloneUrl(CLONE_URL_EDEFAULT);
				return;
			case GithubWebhookPackage.REPOSITORY__FULL_NAME:
				setFullName(FULL_NAME_EDEFAULT);
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
			case GithubWebhookPackage.REPOSITORY__CLONE_URL:
				return CLONE_URL_EDEFAULT == null ? cloneUrl != null : !CLONE_URL_EDEFAULT.equals(cloneUrl);
			case GithubWebhookPackage.REPOSITORY__FULL_NAME:
				return FULL_NAME_EDEFAULT == null ? fullName != null : !FULL_NAME_EDEFAULT.equals(fullName);
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
		result.append(" (cloneUrl: ");
		result.append(cloneUrl);
		result.append(", fullName: ");
		result.append(fullName);
		result.append(')');
		return result.toString();
	}

} //RepositoryImpl
