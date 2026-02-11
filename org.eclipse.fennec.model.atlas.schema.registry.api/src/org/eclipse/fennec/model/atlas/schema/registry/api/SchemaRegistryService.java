/**
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
package org.eclipse.fennec.model.atlas.schema.registry.api;

import org.eclipse.emf.ecore.EClass;
import org.osgi.annotation.versioning.ProviderType;

@ProviderType
public interface SchemaRegistryService {

	String getRegistryName();
	
	String getSchemaUri();
	
	EClass getRootEClass();
	
	boolean isCompatible(EClass eClass);

}
