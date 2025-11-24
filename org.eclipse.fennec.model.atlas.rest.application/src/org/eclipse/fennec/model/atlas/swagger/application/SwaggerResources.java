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
package org.eclipse.fennec.model.atlas.swagger.application;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.http.whiteboard.propertytypes.HttpWhiteboardResource;

@HttpWhiteboardResource(pattern =  "to_be_defined_by_configuration", prefix = "/swagger-api")
@Component(name = SwaggerResources.COMPONENT_NAME, service = SwaggerResources.class, configurationPolicy = ConfigurationPolicy.REQUIRE)
public class SwaggerResources {
	
	public static final String COMPONENT_NAME = "SwaggerResources";
}

