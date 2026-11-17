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
package org.eclipse.fennec.model.atlas.registry.api;

import org.eclipse.emf.ecore.EPackage;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Package Write</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * A request to store a package. The package's identity is its nsURI.
 * <!-- end-model-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getScope <em>Scope</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getRegistry <em>Registry</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getStage <em>Stage</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getContent <em>Content</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getNsUri <em>Ns Uri</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getVersion <em>Version</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getDcat <em>Dcat</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getOnConflict <em>On Conflict</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getPrecondition <em>Precondition</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#isLenientVersion <em>Lenient Version</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackageWrite()
 * @model
 * @generated
 */
@ProviderType
public interface PackageWrite {
	/**
	 * Returns the value of the '<em><b>Scope</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * the scope name
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Scope</em>' attribute.
	 * @see #setScope(String)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackageWrite_Scope()
	 * @model
	 * @generated
	 */
	String getScope();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getScope <em>Scope</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Scope</em>' attribute.
	 * @see #getScope()
	 * @generated
	 */
	void setScope(String value);

	/**
	 * Returns the value of the '<em><b>Registry</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * the registry to store it in, or null for the scope's schema registry. A registry named explicitly must accept the package's type
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Registry</em>' attribute.
	 * @see #setRegistry(String)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackageWrite_Registry()
	 * @model
	 * @generated
	 */
	String getRegistry();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getRegistry <em>Registry</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Registry</em>' attribute.
	 * @see #getRegistry()
	 * @generated
	 */
	void setRegistry(String value);

	/**
	 * Returns the value of the '<em><b>Stage</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * the stage name
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Stage</em>' attribute.
	 * @see #setStage(String)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackageWrite_Stage()
	 * @model
	 * @generated
	 */
	String getStage();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getStage <em>Stage</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Stage</em>' attribute.
	 * @see #getStage()
	 * @generated
	 */
	void setStage(String value);

	/**
	 * Returns the value of the '<em><b>Content</b></em>' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * the package. Referenced, not contained
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Content</em>' reference.
	 * @see #setContent(EPackage)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackageWrite_Content()
	 * @model resolveProxies="false"
	 * @generated
	 */
	EPackage getContent();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getContent <em>Content</em>}' reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Content</em>' reference.
	 * @see #getContent()
	 * @generated
	 */
	void setContent(EPackage value);

	/**
	 * Returns the value of the '<em><b>Ns Uri</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * the nsURI the caller expects the package to carry, or null to take it from the package; when given it must match
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Ns Uri</em>' attribute.
	 * @see #setNsUri(String)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackageWrite_NsUri()
	 * @model
	 * @generated
	 */
	String getNsUri();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getNsUri <em>Ns Uri</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Ns Uri</em>' attribute.
	 * @see #getNsUri()
	 * @generated
	 */
	void setNsUri(String value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * a human-readable name; null uses the package name
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackageWrite_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * the version to record; null resolves it from the package's Version annotation, then from a version-shaped last nsURI segment
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Version</em>' attribute.
	 * @see #setVersion(String)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackageWrite_Version()
	 * @model
	 * @generated
	 */
	String getVersion();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getVersion <em>Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Version</em>' attribute.
	 * @see #getVersion()
	 * @generated
	 */
	void setVersion(String value);

	/**
	 * Returns the value of the '<em><b>Dcat</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * whether the package may be published to a DCAT portal; null means false on create and leaves the stored flag untouched on overwrite
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Dcat</em>' attribute.
	 * @see #setDcat(Boolean)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackageWrite_Dcat()
	 * @model
	 * @generated
	 */
	Boolean getDcat();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getDcat <em>Dcat</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Dcat</em>' attribute.
	 * @see #getDcat()
	 * @generated
	 */
	void setDcat(Boolean value);

	/**
	 * Returns the value of the '<em><b>On Conflict</b></em>' attribute.
	 * The literals are from the enumeration {@link org.eclipse.fennec.model.atlas.registry.api.ConflictPolicy}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * what a create does when the nsURI is taken in the stage; ignored by updates. Defaults to FAIL
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>On Conflict</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.registry.api.ConflictPolicy
	 * @see #setOnConflict(ConflictPolicy)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackageWrite_OnConflict()
	 * @model
	 * @generated
	 */
	ConflictPolicy getOnConflict();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getOnConflict <em>On Conflict</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>On Conflict</em>' attribute.
	 * @see org.eclipse.fennec.model.atlas.registry.api.ConflictPolicy
	 * @see #getOnConflict()
	 * @generated
	 */
	void setOnConflict(ConflictPolicy value);

	/**
	 * Returns the value of the '<em><b>Precondition</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * checked against the existing package before it is replaced; may be null
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Precondition</em>' attribute.
	 * @see #setPrecondition(Precondition)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackageWrite_Precondition()
	 * @model dataType="org.eclipse.fennec.model.atlas.registry.api.Precondition"
	 * @generated
	 */
	Precondition getPrecondition();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#getPrecondition <em>Precondition</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Precondition</em>' attribute.
	 * @see #getPrecondition()
	 * @generated
	 */
	void setPrecondition(Precondition value);

	/**
	 * Returns the value of the '<em><b>Lenient Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * true to ignore (and log) a Version annotation that is not a valid version instead of refusing the package, for loaders that must not fail over a junk annotation. An explicit version is always checked
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Lenient Version</em>' attribute.
	 * @see #setLenientVersion(boolean)
	 * @see org.eclipse.fennec.model.atlas.registry.api.RegistryApiPackage#getPackageWrite_LenientVersion()
	 * @model
	 * @generated
	 */
	boolean isLenientVersion();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.registry.api.PackageWrite#isLenientVersion <em>Lenient Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Lenient Version</em>' attribute.
	 * @see #isLenientVersion()
	 * @generated
	 */
	void setLenientVersion(boolean value);

} // PackageWrite
