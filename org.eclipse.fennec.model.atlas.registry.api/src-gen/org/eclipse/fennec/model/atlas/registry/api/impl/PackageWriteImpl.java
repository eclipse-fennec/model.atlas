/*
 * Copyright (c) 2012 - 2026 Data In Motion and others.
 * All rights reserved.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.registry.api.impl;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;

import org.eclipse.emf.ecore.impl.MinimalEObjectImpl;

import org.eclipse.fennec.model.atlas.registry.api.ConflictPolicy;
import org.eclipse.fennec.model.atlas.registry.api.PackageWrite;
import org.eclipse.fennec.model.atlas.registry.api.Precondition;
import org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage;

/**
 * <!-- begin-user-doc -->
 * An implementation of the model object '<em><b>Package Write</b></em>'.
 * <!-- end-user-doc -->
 * <p>
 * The following features are implemented:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageWriteImpl#getScope <em>Scope</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageWriteImpl#getRegistry <em>Registry</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageWriteImpl#getStage <em>Stage</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageWriteImpl#getContent <em>Content</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageWriteImpl#getNsUri <em>Ns Uri</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageWriteImpl#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageWriteImpl#getVersion <em>Version</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageWriteImpl#getDcat <em>Dcat</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageWriteImpl#getOnConflict <em>On Conflict</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageWriteImpl#getPrecondition <em>Precondition</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.impl.PackageWriteImpl#isLenientVersion <em>Lenient Version</em>}</li>
 * </ul>
 *
 * @generated
 */
public class PackageWriteImpl extends MinimalEObjectImpl.Container implements PackageWrite {
	/**
	 * The default value of the '{@link #getScope() <em>Scope</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getScope()
	 * @generated
	 * @ordered
	 */
	protected static final String SCOPE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getScope() <em>Scope</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getScope()
	 * @generated
	 * @ordered
	 */
	protected String scope = SCOPE_EDEFAULT;

	/**
	 * The default value of the '{@link #getRegistry() <em>Registry</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRegistry()
	 * @generated
	 * @ordered
	 */
	protected static final String REGISTRY_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getRegistry() <em>Registry</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getRegistry()
	 * @generated
	 * @ordered
	 */
	protected String registry = REGISTRY_EDEFAULT;

	/**
	 * The default value of the '{@link #getStage() <em>Stage</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStage()
	 * @generated
	 * @ordered
	 */
	protected static final String STAGE_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getStage() <em>Stage</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getStage()
	 * @generated
	 * @ordered
	 */
	protected String stage = STAGE_EDEFAULT;

	/**
	 * The cached value of the '{@link #getContent() <em>Content</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getContent()
	 * @generated
	 * @ordered
	 */
	protected EPackage content;

	/**
	 * The default value of the '{@link #getNsUri() <em>Ns Uri</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNsUri()
	 * @generated
	 * @ordered
	 */
	protected static final String NS_URI_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getNsUri() <em>Ns Uri</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getNsUri()
	 * @generated
	 * @ordered
	 */
	protected String nsUri = NS_URI_EDEFAULT;

	/**
	 * The default value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected static final String NAME_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getName() <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getName()
	 * @generated
	 * @ordered
	 */
	protected String name = NAME_EDEFAULT;

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
	 * The default value of the '{@link #getDcat() <em>Dcat</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDcat()
	 * @generated
	 * @ordered
	 */
	protected static final Boolean DCAT_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getDcat() <em>Dcat</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getDcat()
	 * @generated
	 * @ordered
	 */
	protected Boolean dcat = DCAT_EDEFAULT;

	/**
	 * The default value of the '{@link #getOnConflict() <em>On Conflict</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOnConflict()
	 * @generated
	 * @ordered
	 */
	protected static final ConflictPolicy ON_CONFLICT_EDEFAULT = ConflictPolicy.FAIL;

	/**
	 * The cached value of the '{@link #getOnConflict() <em>On Conflict</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getOnConflict()
	 * @generated
	 * @ordered
	 */
	protected ConflictPolicy onConflict = ON_CONFLICT_EDEFAULT;

	/**
	 * The default value of the '{@link #getPrecondition() <em>Precondition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPrecondition()
	 * @generated
	 * @ordered
	 */
	protected static final Precondition PRECONDITION_EDEFAULT = null;

	/**
	 * The cached value of the '{@link #getPrecondition() <em>Precondition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #getPrecondition()
	 * @generated
	 * @ordered
	 */
	protected Precondition precondition = PRECONDITION_EDEFAULT;

	/**
	 * The default value of the '{@link #isLenientVersion() <em>Lenient Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isLenientVersion()
	 * @generated
	 * @ordered
	 */
	protected static final boolean LENIENT_VERSION_EDEFAULT = false;

	/**
	 * The cached value of the '{@link #isLenientVersion() <em>Lenient Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @see #isLenientVersion()
	 * @generated
	 * @ordered
	 */
	protected boolean lenientVersion = LENIENT_VERSION_EDEFAULT;

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	protected PackageWriteImpl() {
		super();
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	protected EClass eStaticClass() {
		return RegistryApiPackage.Literals.PACKAGE_WRITE;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getScope() {
		return scope;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setScope(String newScope) {
		scope = newScope;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getRegistry() {
		return registry;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setRegistry(String newRegistry) {
		registry = newRegistry;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getStage() {
		return stage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setStage(String newStage) {
		stage = newStage;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public EPackage getContent() {
		return content;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setContent(EPackage newContent) {
		content = newContent;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getNsUri() {
		return nsUri;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setNsUri(String newNsUri) {
		nsUri = newNsUri;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setName(String newName) {
		name = newName;
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
		version = newVersion;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Boolean getDcat() {
		return dcat;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setDcat(Boolean newDcat) {
		dcat = newDcat;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public ConflictPolicy getOnConflict() {
		return onConflict;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setOnConflict(ConflictPolicy newOnConflict) {
		onConflict = newOnConflict == null ? ON_CONFLICT_EDEFAULT : newOnConflict;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Precondition getPrecondition() {
		return precondition;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setPrecondition(Precondition newPrecondition) {
		precondition = newPrecondition;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public boolean isLenientVersion() {
		return lenientVersion;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public void setLenientVersion(boolean newLenientVersion) {
		lenientVersion = newLenientVersion;
	}

	/**
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @generated
	 */
	@Override
	public Object eGet(int featureID, boolean resolve, boolean coreType) {
		switch (featureID) {
			case RegistryApiPackage.PACKAGE_WRITE__SCOPE:
				return getScope();
			case RegistryApiPackage.PACKAGE_WRITE__REGISTRY:
				return getRegistry();
			case RegistryApiPackage.PACKAGE_WRITE__STAGE:
				return getStage();
			case RegistryApiPackage.PACKAGE_WRITE__CONTENT:
				return getContent();
			case RegistryApiPackage.PACKAGE_WRITE__NS_URI:
				return getNsUri();
			case RegistryApiPackage.PACKAGE_WRITE__NAME:
				return getName();
			case RegistryApiPackage.PACKAGE_WRITE__VERSION:
				return getVersion();
			case RegistryApiPackage.PACKAGE_WRITE__DCAT:
				return getDcat();
			case RegistryApiPackage.PACKAGE_WRITE__ON_CONFLICT:
				return getOnConflict();
			case RegistryApiPackage.PACKAGE_WRITE__PRECONDITION:
				return getPrecondition();
			case RegistryApiPackage.PACKAGE_WRITE__LENIENT_VERSION:
				return isLenientVersion();
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
			case RegistryApiPackage.PACKAGE_WRITE__SCOPE:
				setScope((String)newValue);
				return;
			case RegistryApiPackage.PACKAGE_WRITE__REGISTRY:
				setRegistry((String)newValue);
				return;
			case RegistryApiPackage.PACKAGE_WRITE__STAGE:
				setStage((String)newValue);
				return;
			case RegistryApiPackage.PACKAGE_WRITE__CONTENT:
				setContent((EPackage)newValue);
				return;
			case RegistryApiPackage.PACKAGE_WRITE__NS_URI:
				setNsUri((String)newValue);
				return;
			case RegistryApiPackage.PACKAGE_WRITE__NAME:
				setName((String)newValue);
				return;
			case RegistryApiPackage.PACKAGE_WRITE__VERSION:
				setVersion((String)newValue);
				return;
			case RegistryApiPackage.PACKAGE_WRITE__DCAT:
				setDcat((Boolean)newValue);
				return;
			case RegistryApiPackage.PACKAGE_WRITE__ON_CONFLICT:
				setOnConflict((ConflictPolicy)newValue);
				return;
			case RegistryApiPackage.PACKAGE_WRITE__PRECONDITION:
				setPrecondition((Precondition)newValue);
				return;
			case RegistryApiPackage.PACKAGE_WRITE__LENIENT_VERSION:
				setLenientVersion((Boolean)newValue);
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
			case RegistryApiPackage.PACKAGE_WRITE__SCOPE:
				setScope(SCOPE_EDEFAULT);
				return;
			case RegistryApiPackage.PACKAGE_WRITE__REGISTRY:
				setRegistry(REGISTRY_EDEFAULT);
				return;
			case RegistryApiPackage.PACKAGE_WRITE__STAGE:
				setStage(STAGE_EDEFAULT);
				return;
			case RegistryApiPackage.PACKAGE_WRITE__CONTENT:
				setContent((EPackage)null);
				return;
			case RegistryApiPackage.PACKAGE_WRITE__NS_URI:
				setNsUri(NS_URI_EDEFAULT);
				return;
			case RegistryApiPackage.PACKAGE_WRITE__NAME:
				setName(NAME_EDEFAULT);
				return;
			case RegistryApiPackage.PACKAGE_WRITE__VERSION:
				setVersion(VERSION_EDEFAULT);
				return;
			case RegistryApiPackage.PACKAGE_WRITE__DCAT:
				setDcat(DCAT_EDEFAULT);
				return;
			case RegistryApiPackage.PACKAGE_WRITE__ON_CONFLICT:
				setOnConflict(ON_CONFLICT_EDEFAULT);
				return;
			case RegistryApiPackage.PACKAGE_WRITE__PRECONDITION:
				setPrecondition(PRECONDITION_EDEFAULT);
				return;
			case RegistryApiPackage.PACKAGE_WRITE__LENIENT_VERSION:
				setLenientVersion(LENIENT_VERSION_EDEFAULT);
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
			case RegistryApiPackage.PACKAGE_WRITE__SCOPE:
				return SCOPE_EDEFAULT == null ? scope != null : !SCOPE_EDEFAULT.equals(scope);
			case RegistryApiPackage.PACKAGE_WRITE__REGISTRY:
				return REGISTRY_EDEFAULT == null ? registry != null : !REGISTRY_EDEFAULT.equals(registry);
			case RegistryApiPackage.PACKAGE_WRITE__STAGE:
				return STAGE_EDEFAULT == null ? stage != null : !STAGE_EDEFAULT.equals(stage);
			case RegistryApiPackage.PACKAGE_WRITE__CONTENT:
				return content != null;
			case RegistryApiPackage.PACKAGE_WRITE__NS_URI:
				return NS_URI_EDEFAULT == null ? nsUri != null : !NS_URI_EDEFAULT.equals(nsUri);
			case RegistryApiPackage.PACKAGE_WRITE__NAME:
				return NAME_EDEFAULT == null ? name != null : !NAME_EDEFAULT.equals(name);
			case RegistryApiPackage.PACKAGE_WRITE__VERSION:
				return VERSION_EDEFAULT == null ? version != null : !VERSION_EDEFAULT.equals(version);
			case RegistryApiPackage.PACKAGE_WRITE__DCAT:
				return DCAT_EDEFAULT == null ? dcat != null : !DCAT_EDEFAULT.equals(dcat);
			case RegistryApiPackage.PACKAGE_WRITE__ON_CONFLICT:
				return onConflict != ON_CONFLICT_EDEFAULT;
			case RegistryApiPackage.PACKAGE_WRITE__PRECONDITION:
				return PRECONDITION_EDEFAULT == null ? precondition != null : !PRECONDITION_EDEFAULT.equals(precondition);
			case RegistryApiPackage.PACKAGE_WRITE__LENIENT_VERSION:
				return lenientVersion != LENIENT_VERSION_EDEFAULT;
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
		result.append(" (scope: ");
		result.append(scope);
		result.append(", registry: ");
		result.append(registry);
		result.append(", stage: ");
		result.append(stage);
		result.append(", nsUri: ");
		result.append(nsUri);
		result.append(", name: ");
		result.append(name);
		result.append(", version: ");
		result.append(version);
		result.append(", dcat: ");
		result.append(dcat);
		result.append(", onConflict: ");
		result.append(onConflict);
		result.append(", precondition: ");
		result.append(precondition);
		result.append(", lenientVersion: ");
		result.append(lenientVersion);
		result.append(')');
		return result.toString();
	}

} //PackageWriteImpl
