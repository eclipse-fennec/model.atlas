/**
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
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.workflow;

/**
 * 
 * @author ilenia
 * @since Mar 27, 2026
 */
public interface WorkflowConstants {
	
	public static final String ATLAS_SCOPE_NAME = "atlas";
	
	public static final String ATLAS_SCHEMA_REGISTRY_NAME = "atlas-schema-registry";
	
	public static final String ATLAS_SCHEMA_REGISTRY_STAGE_NAME = "released";
	
	/** ATLAS_EPACKAGE_REGISTRATION_STAGE_PROPERTY 
	 * This is the property used to register EPackages as a service via the EPackageStageActionService
	 * */
	public static final String ATLAS_EPACKAGE_REGISTRATION_STAGE_PROPERTY = "atlas.stage";
}
