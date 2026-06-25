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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.wf.workflowapi;

import java.util.List;

import org.eclipse.fennec.model.atlas.scope.api.RegistryInfo;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Registry</b></em>'.
 * <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 *   <li>{@link org.eclipse.fennec.model.atlas.wf.workflowapi.Registry#getAllowedTransitions <em>Allowed Transitions</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiPackage#getRegistry()
 * @model
 * @generated
 */
@ProviderType
public interface Registry extends RegistryInfo {
	/**
	 * Returns the value of the '<em><b>Allowed Transitions</b></em>' containment reference list.
	 * The list contents are of type {@link org.eclipse.fennec.model.atlas.wf.workflowapi.StageTransition}.
	 * <!-- begin-user-doc -->
	 * <!-- end-user-doc -->
	 * @return the value of the '<em>Allowed Transitions</em>' containment reference list.
	 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiPackage#getRegistry_AllowedTransitions()
	 * @model containment="true"
	 * @generated
	 */
	List<StageTransition> getAllowedTransitions();

} // Registry
