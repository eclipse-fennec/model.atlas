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
package org.eclipse.fennec.model.atlas.workflow.impl;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * 
 * @author ilenia
 * @since Jan 13, 2026
 */
@ObjectClassDefinition(name = "Registry Service Configuration")
public @interface RegistryServiceConfig {

    @AttributeDefinition(name = "Registry Name", description = "A meaningful name to describe the purpose of the registry (e.g. configurations, sensinact-mappings)", required = true)
    String registry_name();

    @AttributeDefinition(name = "Registry Description", description = "A description for the registry", required = false, defaultValue = "")
    String registry_description() default "";
    
    @AttributeDefinition(name = "Registry Type", description = "The role of this registry: SCHEMA (holds EPackages), COCL (holds OCL constraint sets), TRANSFORMATION (holds transformation sources and compiled units), OTHER (general purpose)", required = false, defaultValue = "OTHER")
    String registry_type() default "OTHER";

    @AttributeDefinition(name = "Stage Storage Mappings", description = "Array of ':'-separated stage→storage mappings (e.g., [draft:mongodb,approved:minio,release:apicurio])", required = true)
    String[] stage_storage_mappings();

    @AttributeDefinition(name = "Workflow Transitions", description = "Array of ':'-separated fromStage→toStage allowed transitions (e.g. [draft:approved, approved:release])", required = true)
    String[] workflow_transitions();

    @AttributeDefinition(name = "Delete After Transition", description = "Whether to remove an object from a stage after a transition to another stage", required = false, defaultValue = "false")
    boolean delete_after_transition() default false;

    @AttributeDefinition(name = "Storage Service Target Filter", description = "The target filter for the storageService injected reference", required = false, defaultValue = "(storage.type=*)")
    String storageService_target() default "(storage.type=*)";

    @AttributeDefinition(name = "Stages", description = "The stages associated with this RegistryService. "
            + "Should be an array of complex objects of type Stage (e.g. {\"name\": \"draft\", \"writable\": true, \"final\": false})", required = true)
    String[] stages();

    @AttributeDefinition(name = "Schema URI", description = "The uri of the EPackage this registry supports objects from", required = false, defaultValue = "http://www.eclipse.org/emf/2002/Ecore")
    String schema_uri() default "http://www.eclipse.org/emf/2002/Ecore";

    @AttributeDefinition(name = "Root EClass URIs", description = "The uris of the EClasses this registry supports objects from; "
            + "an object is accepted if its EClass matches any listed root or has one among its supertypes. "
            + "A single String value keeps working (coerced to a one-element array).", required = false, defaultValue = "http://www.eclipse.org/emf/2002/Ecore#//EPackage")
    String[] root_eclass_uri() default { "http://www.eclipse.org/emf/2002/Ecore#//EPackage" };

    @AttributeDefinition(name = "Derived EClass URIs", description = "The uris of EClasses whose objects the Atlas itself derives from uploaded content "
            + "(e.g. compiled units and their diagnostics). Derived objects are readable like any other content but are refused "
            + "on the REST write path, while the trusted service API may write them — including updates in final stages, "
            + "because a derived write is the consequence of a sanctioned upload or transition.", required = false)
    String[] derived_eclass_uri() default {};

    @AttributeDefinition(name = "ResourceSet Target", description = "The target filter ensuring that the ResourceSet with the required model is actually available", required = true)
    String resourceSet_target();
}
