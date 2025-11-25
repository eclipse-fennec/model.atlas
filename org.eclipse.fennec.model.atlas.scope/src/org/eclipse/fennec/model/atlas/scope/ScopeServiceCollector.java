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
package org.eclipse.fennec.model.atlas.scope;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import org.eclipse.fennec.model.atlas.model.scope.Scope;
import org.eclipse.fennec.model.atlas.model.scope.ScopeFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * 
 * @author ilenia
 * @since Nov 25, 2025
 */
@Component(name = "ScopeServiceCollector", immediate = true, service = ScopeServiceCollector.class)
public class ScopeServiceCollector {
	
	private static final Logger LOGGER = Logger.getLogger(ScopeServiceCollector.class.getName());
	private Map<String, Scope> scopeMap = new HashMap<>();
	
	public Scope getScopeByName(String name) {
		return scopeMap.getOrDefault(name, null);
	}
	
	public List<Scope> getScopes() {
		return scopeMap.values().stream().toList();
	}
	
	
	@Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MULTIPLE)
	public void bindScopeService(ScopeService scopeService, Map<String, String> properties) {
		if(!properties.containsKey("name") || properties.get("name") == null || properties.get("name").isEmpty()) {
			LOGGER.severe(String.format("Cannot store ScopeService with name property not set or empty"));
			return;
		}
		scopeMap.put(properties.get("name"), createScopeFromConfig(properties));
	}
	
	public void unbindScopeService(ScopeService scopeService, Map<String, String> properties) {
		scopeMap.remove(properties.get("name"));
	}
	
	private Scope createScopeFromConfig(Map<String, String> properties) {
		Scope scope = ScopeFactory.eINSTANCE.createScope();
		scope.setName(properties.get("name"));
		scope.setDescription(properties.get("description"));
		scope.setParentScope(properties.get("parent.scope"));
		scope.getLinks().put("self", "/scopes/"+properties.get("name"));
		scope.getLinks().put("schemas", "/"+properties.get("name")+"/schema");
		return scope;
	}

}
