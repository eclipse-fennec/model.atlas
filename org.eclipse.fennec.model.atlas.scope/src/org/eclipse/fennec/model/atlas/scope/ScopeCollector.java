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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.eclipse.fennec.model.atlas.model.scope.Scope;
import org.eclipse.fennec.model.atlas.model.scope.ScopeFactory;
import org.eclipse.fennec.model.atlas.wf.workflowapi.EObjectWorkflowService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * This service is responsible to collect the EObjectWorkflowServices and construct the corresponding Scope objects that can then be queried by the API
 * @author ilenia
 * @since Nov 25, 2025
 */
@Component(name = "ScopeCollector", immediate = true, service = ScopeCollector.class)
public class ScopeCollector {
	
	private static final Logger LOGGER = Logger.getLogger(ScopeCollector.class.getName());
	private Map<String, Scope> scopeMap = new ConcurrentHashMap<>();
	private Map<String, EObjectWorkflowService<?>> workflowServicesMap = new ConcurrentHashMap<>();
	
	public Scope getScopeByName(String name) {
		return scopeMap.getOrDefault(name, null);
	}
	
	public List<Scope> getScopes() {
		return scopeMap.values().stream().toList();
	}
	
	public EObjectWorkflowService<?> getWorkflowServiceByScope(String scopeName) {
		return workflowServicesMap.getOrDefault(scopeName, null);
	}
	
	
	@Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MULTIPLE)
	public void bindWorkflowService(EObjectWorkflowService<?> workflowService, Map<String, Object> properties) {
		if(!properties.containsKey("scope") || properties.get("scope") == null || ((String) properties.get("scope")).isEmpty()) {
			LOGGER.severe(String.format("Cannot store EObjectWorkflowService with scope property not set or empty"));
			return;
		}
		scopeMap.put((String) properties.get("scope"), createScopeFromConfig(properties));
		workflowServicesMap.put((String) properties.get("scope"), workflowService);
	}
	
	public void unbindWorkflowService(EObjectWorkflowService<?> workflowService, Map<String, Object> properties) {
		scopeMap.remove(properties.get("scope"));
		workflowServicesMap.remove(properties.get("scope"));
	}
	
	private Scope createScopeFromConfig(Map<String, Object> properties) {
		Scope scope = ScopeFactory.eINSTANCE.createScope();
		scope.setName((String) properties.get("scope"));
		scope.setDescription((String) properties.get("description"));
		scope.setParentScope((String) properties.get("parent.scope"));
		scope.getLinks().put("self", "/scopes/"+properties.get("scope"));
		scope.getLinks().put("schemas", "/"+properties.get("scope")+"/schema");
		scope.setFinalStage((String) properties.get("final.stage"));
		scope.getStages().addAll(List.of((String[]) properties.get("stages")));
		return scope;
	}
	
	
}
