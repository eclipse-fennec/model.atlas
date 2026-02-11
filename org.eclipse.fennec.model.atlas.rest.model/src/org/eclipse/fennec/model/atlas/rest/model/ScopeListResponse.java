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
package org.eclipse.fennec.model.atlas.rest.model;

import org.eclipse.emf.common.util.EList;

import org.eclipse.emf.ecore.EObject;

import org.eclipse.fennec.model.atlas.wf.workflowapi.Scope;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Scope
 * List Response</b></em>'. <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 * <li>{@link org.eclipse.fennec.model.atlas.rest.model.ScopeListResponse#getScopes
 * <em>Scopes</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.rest.model.RestPackage#getScopeListResponse()
 * @model
 * @generated
 */
@ProviderType
public interface ScopeListResponse extends EObject {
    /**
     * Returns the value of the '<em><b>Scopes</b></em>' containment reference list.
     * The list contents are of type
     * {@link org.eclipse.fennec.model.atlas.wf.workflowapi.Scope}. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of the '<em>Scopes</em>' containment reference list.
     * @see org.eclipse.fennec.model.atlas.rest.model.RestPackage#getScopeListResponse_Scopes()
     * @model containment="true"
     * @generated
     */
    EList<Scope> getScopes();

} // ScopeListResponse
