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
@ObjectClassDefinition(name = "Scope Service Configuration")
public @interface ScopeServiceConfig {

    @AttributeDefinition(name = "Scope Name", description = "A meaningflu name for the scope", required = true)
    String scope_name();

    @AttributeDefinition(name = "Scope Description", description = "A description for the scope", required = false, defaultValue = "")
    String scope_description() default "";

    @AttributeDefinition(name = "Parent Scope Name", description = "The parent scope name. This is used when performing hierarchical lookup of objects", required = false, defaultValue = "atlas")
    String scope_parent() default "atlas";

    @AttributeDefinition(name = "Registry Service Target Filter", description = "The target filter for the injected list of RegistryService. If not provided no RegistryService will be associated with this scope "
            + "and no operation will be possible for the scope.", required = false, defaultValue = "(scope=no-inject)")
    String registryService_target() default "(scope=no-inject)";
    

}
