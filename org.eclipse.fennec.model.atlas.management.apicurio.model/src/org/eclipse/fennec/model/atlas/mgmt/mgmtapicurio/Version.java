/*
 * Copyright (c) 2012 - 2025 Data In Motion and others.
 * All rights reserved.
 * 
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 * 
 * SPDX-License-Identifier: EPL-2.0
 * 
 * Contributors:
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.EMap;

import org.eclipse.emf.ecore.EObject;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Version</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version#getVersion <em>Version</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version#getContent <em>Content</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version#getName <em>Name</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version#getDescription <em>Description</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version#getBranches <em>Branches</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version#isIsDraft <em>Is Draft</em>}</li>
 *   <li>{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version#getLabels <em>Labels</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage#getVersion()
 * @model
 * @generated
 */
@ProviderType
public interface Version extends EObject {
	/**
	 * Returns the value of the '<em><b>Version</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * <!-- begin-model-doc -->
	 * A single version of an artifact. Can be provided by the client when creating a new version, or it can be server-generated. The value can be any string unique to the artifact, but it is recommended to use a simple integer or a semver value.
	 * <!-- end-model-doc -->
	 * @return the value of the '<em>Version</em>' attribute.
	 * @see #setVersion(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage#getVersion_Version()
	 * @model
	 * @generated
	 */
	String getVersion();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version#getVersion <em>Version</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Version</em>' attribute.
	 * @see #getVersion()
	 * @generated
	 */
	void setVersion(String value);

	/**
	 * Returns the value of the '<em><b>Content</b></em>' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Content</em>' containment reference.
	 * @see #setContent(Content)
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage#getVersion_Content()
	 * @model containment="true" required="true"
	 * @generated
	 */
	Content getContent();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version#getContent <em>Content</em>}' containment reference.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Content</em>' containment reference.
	 * @see #getContent()
	 * @generated
	 */
	void setContent(Content value);

	/**
	 * Returns the value of the '<em><b>Name</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Name</em>' attribute.
	 * @see #setName(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage#getVersion_Name()
	 * @model
	 * @generated
	 */
	String getName();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version#getName <em>Name</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Name</em>' attribute.
	 * @see #getName()
	 * @generated
	 */
	void setName(String value);

	/**
	 * Returns the value of the '<em><b>Description</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Description</em>' attribute.
	 * @see #setDescription(String)
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage#getVersion_Description()
	 * @model
	 * @generated
	 */
	String getDescription();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version#getDescription <em>Description</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Description</em>' attribute.
	 * @see #getDescription()
	 * @generated
	 */
	void setDescription(String value);

	/**
	 * Returns the value of the '<em><b>Branches</b></em>' attribute list.
	 * The list contents are of type {@link java.lang.String}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Branches</em>' attribute list.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage#getVersion_Branches()
	 * @model
	 * @generated
	 */
	EList<String> getBranches();

	/**
	 * Returns the value of the '<em><b>Is Draft</b></em>' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Is Draft</em>' attribute.
	 * @see #setIsDraft(boolean)
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage#getVersion_IsDraft()
	 * @model
	 * @generated
	 */
	boolean isIsDraft();

	/**
	 * Sets the value of the '{@link org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Version#isIsDraft <em>Is Draft</em>}' attribute.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @param value the new value of the '<em>Is Draft</em>' attribute.
	 * @see #isIsDraft()
	 * @generated
	 */
	void setIsDraft(boolean value);

	/**
	 * Returns the value of the '<em><b>Labels</b></em>' map.
	 * The key is of type {@link java.lang.String},
	 * and the value is of type {@link java.lang.String},
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Labels</em>' map.
	 * @see org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.MgmtApicurioPackage#getVersion_Labels()
	 * @model mapType="org.eclipse.fennec.model.atlas.mgmt.mgmtapicurio.Labels&lt;org.eclipse.emf.ecore.EString, org.eclipse.emf.ecore.EString&gt;"
	 * @generated
	 */
	EMap<String, String> getLabels();

} // Version
