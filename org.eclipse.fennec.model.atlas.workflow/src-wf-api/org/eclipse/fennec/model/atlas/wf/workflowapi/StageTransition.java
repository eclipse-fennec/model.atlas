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

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc --> A representation of the model object '<em><b>Stage
 * Transition</b></em>'. <!-- end-user-doc -->
 *
 * <p>
 * The following features are supported:
 * </p>
 * <ul>
 * <li>{@link org.eclipse.fennec.model.atlas.wf.workflowapi.StageTransition#getFromStage
 * <em>From Stage</em>}</li>
 * <li>{@link org.eclipse.fennec.model.atlas.wf.workflowapi.StageTransition#getToStage
 * <em>To Stage</em>}</li>
 * </ul>
 *
 * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiPackage#getStageTransition()
 * @model
 * @generated
 */
@ProviderType
public interface StageTransition {
    /**
     * Returns the value of the '<em><b>From Stage</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of the '<em>From Stage</em>' attribute.
     * @see #setFromStage(String)
     * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiPackage#getStageTransition_FromStage()
     * @model
     * @generated
     */
    String getFromStage();

    /**
     * Sets the value of the
     * '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.StageTransition#getFromStage
     * <em>From Stage</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc
     * -->
     * 
     * @param value the new value of the '<em>From Stage</em>' attribute.
     * @see #getFromStage()
     * @generated
     */
    void setFromStage(String value);

    /**
     * Returns the value of the '<em><b>To Stage</b></em>' attribute. <!--
     * begin-user-doc --> <!-- end-user-doc -->
     * 
     * @return the value of the '<em>To Stage</em>' attribute.
     * @see #setToStage(String)
     * @see org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiPackage#getStageTransition_ToStage()
     * @model
     * @generated
     */
    String getToStage();

    /**
     * Sets the value of the
     * '{@link org.eclipse.fennec.model.atlas.wf.workflowapi.StageTransition#getToStage
     * <em>To Stage</em>}' attribute. <!-- begin-user-doc --> <!-- end-user-doc -->
     * 
     * @param value the new value of the '<em>To Stage</em>' attribute.
     * @see #getToStage()
     * @generated
     */
    void setToStage(String value);

} // StageTransition
