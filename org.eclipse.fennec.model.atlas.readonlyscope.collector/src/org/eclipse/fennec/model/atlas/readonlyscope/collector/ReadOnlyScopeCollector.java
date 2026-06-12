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
package org.eclipse.fennec.model.atlas.readonlyscope.collector;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.eclipse.fennec.model.atlas.scope.api.AtlasProperties;
import org.eclipse.fennec.model.atlas.scope.api.ReadOnlyScopeService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

@Component(immediate = true, name = "ReadOnlyScopeCollector", service = ReadOnlyScopeCollector.class)
public class ReadOnlyScopeCollector {

	private static final Logger LOGGER = Logger.getLogger(ReadOnlyScopeCollector.class.getName());
	private Map<String, ReadOnlyScopeService<?>> scopeServiceMap = new ConcurrentHashMap<>();

	public ReadOnlyScopeService<?> getScopeServiceByScopeName(String scopeName) {
        return scopeServiceMap.getOrDefault(scopeName, null);
    }

    public List<String> getAllScopeNames() {
        return scopeServiceMap.keySet().stream().toList();
    }
	
	@Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MULTIPLE)
    public void bindScopeService(ReadOnlyScopeService<?> scopeService, Map<String, Object> properties) {
        if (!properties.containsKey(AtlasProperties.ATLAS_SCOPE) || ((String) properties.get(AtlasProperties.ATLAS_SCOPE)).isEmpty()) {
            LOGGER.severe(String.format("Cannot store ReadOnlyScopeService with %s property not set or empty", AtlasProperties.ATLAS_SCOPE));
            return;
        }
        String scopeName = (String) properties.get(AtlasProperties.ATLAS_SCOPE);
        if (scopeServiceMap.containsKey(scopeName)) {
            LOGGER.warning(
                    String.format("ReadOnlyScopeService with name %s already existed. This will override it", scopeName));
        }
        scopeServiceMap.put(scopeName, scopeService);
    }

    public void unbindScopeService(ReadOnlyScopeService<?> scopeService, Map<String, Object> properties) {
        if (!properties.containsKey(AtlasProperties.ATLAS_SCOPE) || ((String) properties.get(AtlasProperties.ATLAS_SCOPE)).isEmpty()) {
            return;
        }
        String scopeName = (String) properties.get(AtlasProperties.ATLAS_SCOPE);
        scopeServiceMap.remove(scopeName);
    }
}
