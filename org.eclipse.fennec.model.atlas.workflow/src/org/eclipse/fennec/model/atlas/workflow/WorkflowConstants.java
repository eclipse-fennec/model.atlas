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

	/** NS_URI_METADATA_PROPERTY
	 * The ObjectMetadata properties key carrying the raw namespace URI of an EPackage.
	 * Written by the schema REST upload path and by the git backend's metadata derivation;
	 * used by the nsUri-based scope-service lookups.
	 * */
	public static final String NS_URI_METADATA_PROPERTY = "nsUri";

	/** DCAT_PUBLISH_METADATA_PROPERTY
	 * The ObjectMetadata properties key asserting that an object may be published to a
	 * DCAT portal. Written by the schema REST upload path from the {@code ?dcat=} query
	 * parameter and read by the DCAT publisher, so the name is API between the two
	 * bundles. The value is stored as a {@link Boolean}; readers should tolerate the
	 * string form too, since {@code properties} is typed {@code String -> EJavaObject}
	 * and both are storable. An absent key means false.
	 * */
	public static final String DCAT_PUBLISH_METADATA_PROPERTY = "dcat";
}
