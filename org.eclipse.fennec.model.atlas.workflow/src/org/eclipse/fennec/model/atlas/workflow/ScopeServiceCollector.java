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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.eclipse.fennec.model.atlas.scope.api.AtlasProperties;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Scope;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * 
 * @author ilenia
 * @since Jan 15, 2026
 */
@Component(immediate = true, name = "ScopeServiceCollector", service = ScopeServiceCollector.class)
public class ScopeServiceCollector {

	private static final Logger LOGGER = Logger.getLogger(ScopeServiceCollector.class.getName());

	private Map<String, ScopeService<?>> scopeServiceMap = new ConcurrentHashMap<>();

	public ScopeService<?> getScopeServiceByScopeName(String scopeName) {
		return scopeServiceMap.getOrDefault(scopeName, null);
	}

	public Scope getScopeByName(String scopeName) {
		if (scopeServiceMap.containsKey(scopeName)) {
			return scopeServiceMap.get(scopeName).getScope();
		}
		return null;
	}

	public List<Scope> getAllScopes() {
		return scopeServiceMap.values().stream().map(s -> s.getScope()).toList();
	}

	@Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MULTIPLE)
	public void bindScopeService(ScopeService<?> scopeService, Map<String, Object> properties) {
		String scopeName = null;
		if (!properties.containsKey(AtlasProperties.ATLAS_SCOPE) || ((String) properties.get(AtlasProperties.ATLAS_SCOPE)).isEmpty()) {
			LOGGER.warning(String.format("ScopeService without %s property. Fallback to scope.name property", AtlasProperties.ATLAS_SCOPE));
			if (!properties.containsKey("scope.name") || ((String) properties.get("scope.name")).isEmpty()) {
				LOGGER.severe(String.format("Cannot store ScopeService with scope.name property not set or empty"));
				return;
			}
			scopeName = (String) properties.get("scope.name");
		} else {
			scopeName = (String) properties.get(AtlasProperties.ATLAS_SCOPE);
		}
		if (scopeServiceMap.containsKey(scopeName)) {
			LOGGER.warning(
					String.format("ScopeService with name %s already existed. This will override it", scopeName));
		}
		scopeServiceMap.put(scopeName, scopeService);
	}

	public void unbindScopeService(ScopeService<?> scopeService, Map<String, Object> properties) {
		String propertyKey = null;
		if (!properties.containsKey(AtlasProperties.ATLAS_SCOPE) || ((String) properties.get(AtlasProperties.ATLAS_SCOPE)).isEmpty()) {
			if (!properties.containsKey("scope.name") || ((String) properties.get("scope.name")).isEmpty()) {
				return;
			}
			propertyKey = "scope.name";
		} else {
			propertyKey = AtlasProperties.ATLAS_SCOPE;
		}
		
		String scopeName = (String) properties.get(propertyKey);
		scopeServiceMap.remove(scopeName);
	}

}
