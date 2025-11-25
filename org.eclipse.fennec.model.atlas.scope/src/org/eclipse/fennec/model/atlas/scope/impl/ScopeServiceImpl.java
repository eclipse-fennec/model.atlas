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
package org.eclipse.fennec.model.atlas.scope.impl;

import org.eclipse.fennec.model.atlas.scope.ScopeService;
import org.eclipse.fennec.model.atlas.scope.ScopeServiceConfig;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;

/**
 * 
 * @author ilenia
 * @since Nov 25, 2025
 */
@Component(name = "ScopeService", service = ScopeService.class, configurationPid = "ScopeService", configurationPolicy = ConfigurationPolicy.REQUIRE)
public class ScopeServiceImpl implements ScopeService {
	
	private ScopeServiceConfig config;

	@Activate
	public ScopeServiceImpl(ScopeServiceConfig config) {
		this.config = config;		
	}

}
