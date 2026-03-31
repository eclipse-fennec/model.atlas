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
package org.eclipse.fennec.model.atlas.schema.registry.impl;

import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;

/**
 * 
 * @author ilenia
 * @since Jan 6, 2026
 */
@ObjectClassDefinition(name = "Schema Registry Service Configuration", description = "Configuration for the SchemaRegistryService")
public @interface SchemaRegistryServiceConfig {

    @AttributeDefinition(name = "Registry Name", description = "The name of the registry this service is responsible for (e.g. sensinact-mapping)", required = true)
    String registry_name();

    @AttributeDefinition(name = "Root EClass URI", description = "The URI of the EClass this service will check validation against (e.g. http://eclipse.org/sensinact/mapping#//SensinactResourceMapping)", required = true)
    String root_eclass_uri();

    @AttributeDefinition(name = "ResourceSet Target", description = "The target filter ensuring that the ResourceSet with the required model is actually available", required = true)
    String resourceSet_target();

}
