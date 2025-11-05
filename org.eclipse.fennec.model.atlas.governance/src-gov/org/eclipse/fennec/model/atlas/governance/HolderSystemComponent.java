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
 *      Mark Hoffmann - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.governance;

import org.osgi.annotation.versioning.ProviderType;

/**
 * <!-- begin-user-doc -->
 * A representation of the model object '<em><b>Holder System Component</b></em>'.
 * <!-- end-user-doc -->
 *
 * <!-- begin-model-doc -->
 * A system component that can contain other components, policies, and features. Used for complex components like gateways that supervise other components and manage their own sub-components, policies, and features.
 * <!-- end-model-doc -->
 *
 *
 * @see org.eclipse.fennec.model.atlas.governance.GovernancePackage#getHolderSystemComponent()
 * @model
 * @generated
 */
@ProviderType
public interface HolderSystemComponent extends SystemComponent, SystemComponentHolder, PolicyHolder, FeatureHolder {
} // HolderSystemComponent
