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
    
	@AttributeDefinition(name = "Registry Name")
    String registry_name();

    @AttributeDefinition(name = "Stage Storage Mappings",
        description = "Array of ':'-separated stage→storage mappings (e.g., [draft:mongodb,approved:minio,release:apicurio])")
    String[] stage_storage_mappings();

    @AttributeDefinition(name = "Workflow Transitions",
        description = "Array of ':'-separated fromStage→toStage allowed transitions (e.g. [draft:approved, approved:release])")
    String[] workflow_transitions();
    
    @AttributeDefinition(name = "Delete After Transition", 
    		description = "Whether to remove an object from a stage after a transition to another stage")
    boolean delete_after_transition() default false;
    
    @AttributeDefinition(name = "Storage Service Target Filter", 
    		description = "The tatrget filter for the storageService injected reference")
    String storageService_target() default "(storage.type=*)";
    
    @AttributeDefinition(name = "Stages", 
    		description = "The stages associated with this RegistryService. "
    				+ "Should be an array of complex objects of type Stage (e.g. {\"name\": \"draft\", \"writable\": true, \"final\": false})")
    String[] stages();
}
