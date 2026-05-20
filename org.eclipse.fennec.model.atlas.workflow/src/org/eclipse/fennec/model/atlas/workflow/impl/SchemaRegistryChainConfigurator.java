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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Registry;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryType;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Scope;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Stage;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;

/**
 * Generates {@code EPackageRegistry} and {@code ResourceSetFactory} factory
 * configurations programmatically as {@link ScopeService}s are bound.
 *
 * <p>
 * For each bound non-atlas scope that has a schema registry, one
 * {@code EPackageRegistry} / {@code ResourceSetFactory} pair is created per
 * stage (in stage declaration order). Chain: stage[i] points at stage[i+1];
 * the final stage points at the parent scope's final-stage pair, or at the
 * default EPackage registry when the parent is {@code atlas}.
 * </p>
 *
 * @author ilenia
 */
@Component(immediate = true, name = "SchemaRegistryChainConfigurator")
public class SchemaRegistryChainConfigurator {

    static final String DEFAULT_REGISTRY_TARGET = "(default.resourceset.epackage.registry=true)";

    private static final Logger LOGGER = Logger.getLogger(SchemaRegistryChainConfigurator.class.getName());

    private final ConfigurationAdmin configAdmin;

    private final Map<String, ScopeService<?>> scopesByName = new ConcurrentHashMap<>();
    private final Map<String, List<Configuration>> configsByScope = new ConcurrentHashMap<>();
    private final Object lock = new Object();

    @Activate
    public SchemaRegistryChainConfigurator(@Reference ConfigurationAdmin configAdmin) {
        this.configAdmin = configAdmin;
    }

    @Reference(name = "scopeService", policy = ReferencePolicy.DYNAMIC, cardinality = ReferenceCardinality.MULTIPLE)
    public void bindScopeService(ScopeService<?> scopeService) {
        Scope scope = scopeService.getScope();
        if (scope == null || scope.getName() == null) {
        	LOGGER.severe(String.format("Cannot register dependent services for ScopeService null or without a scope.name property"));
            return;
        }
        String scopeName = scope.getName();
        if (WorkflowConstants.ATLAS_SCOPE_NAME.equals(scopeName)) {
            return;
        }
        synchronized (lock) {
            scopesByName.put(scopeName, scopeService);
            regenerate(scopeName);
            regenerateChildrenOf(scopeName);
        }
    }

    public void unbindScopeService(ScopeService<?> scopeService) {
        Scope scope = scopeService.getScope();
        if (scope == null || scope.getName() == null) {
            return;
        }
        String scopeName = scope.getName();
        if (WorkflowConstants.ATLAS_SCOPE_NAME.equals(scopeName)) {
            return;
        }
        synchronized (lock) {
            scopesByName.remove(scopeName);
            deleteConfigs(scopeName);
            regenerateChildrenOf(scopeName);
        }
    }

    @Deactivate
    public void deactivate() {
        synchronized (lock) {
            new ArrayList<>(configsByScope.keySet()).forEach(this::deleteConfigs);
            scopesByName.clear();
        }
    }

    private void regenerateChildrenOf(String parentScopeName) {
        scopesByName.values().stream()
                .map(ScopeService::getScope)
                .filter(s -> s != null && parentScopeName.equals(s.getParentScope()))
                .map(Scope::getName)
                .forEach(this::regenerate);
    }

    private void regenerate(String scopeName) {
        deleteConfigs(scopeName);

        ScopeService<?> scopeService = scopesByName.get(scopeName);
        if (scopeService == null) {
            return;
        }
        Scope scope = scopeService.getScope();
        if (scope == null) {
            return;
        }
        Registry schemaRegistry = scope.getRegistries().stream()
                .filter(r -> RegistryType.SCHEMA.equals(r.getType()))
                .findFirst()
                .orElse(null);
        if (schemaRegistry == null) {
            return;
        }
        List<Stage> stages = schemaRegistry.getStages();
        if (stages.isEmpty()) {
            return;
        }

        String parentScopeName = scope.getParentScope();
        String finalStageParentTarget = resolveFinalStageParentTarget(parentScopeName);
        if (finalStageParentTarget == null) {
            LOGGER.log(Level.INFO, () -> "Deferring chain generation for scope '" + scopeName
                    + "': parent scope '" + parentScopeName + "' is not yet bound");
            return;
        }

        List<Configuration> created = new ArrayList<>();
        try {
            for (int i = 0; i < stages.size(); i++) {
                String stageName = stages.get(i).getName();
                String rsfName = rsfName(scopeName, stageName);
                String nextTarget = (i < stages.size() - 1)
                        ? rsfNameFilter(scopeName, stages.get(i + 1).getName())
                        : finalStageParentTarget;

                created.add(createEPackageRegistryConfig(scopeName, stageName, rsfName, nextTarget));
                created.add(createResourceSetFactoryConfig(scopeName, stageName, rsfName));
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to create chain configurations for scope " + scopeName, e);
            created.forEach(this::deleteQuietly);
            return;
        }
        configsByScope.put(scopeName, created);
        LOGGER.log(Level.INFO, () -> "Generated " + created.size() + " chain configurations for scope '" + scopeName
                + "' (" + stages.size() + " stages)");
    }

    /**
     * Returns the target filter the final stage of a scope with
     * {@code parentScopeName} should point at, or {@code null} if the parent is
     * not yet bound (so generation should be deferred).
     */
    private String resolveFinalStageParentTarget(String parentScopeName) {
        if (parentScopeName == null || WorkflowConstants.ATLAS_SCOPE_NAME.equals(parentScopeName)) {
            return DEFAULT_REGISTRY_TARGET;
        }
        ScopeService<?> parentSvc = scopesByName.get(parentScopeName);
        if (parentSvc == null) {
            return null;
        }
        Scope parentScope = parentSvc.getScope();
        if (parentScope == null) {
            return null;
        }
        Registry parentSchema = parentScope.getRegistries().stream()
                .filter(r -> RegistryType.SCHEMA.equals(r.getType()))
                .findFirst()
                .orElse(null);
        if (parentSchema == null || parentSchema.getStages().isEmpty()) {
            return null;
        }
        String parentFinalStage = parentSchema.getStages().stream().filter(s -> s.isFinal()).map(s -> s.getName()).findFirst().orElse(null);
        if(parentFinalStage == null) {
        	LOGGER.warning(String.format("No Final Stage found in RegistryService %s", parentSchema.getName()));
        	return null;
        }
        return rsfNameFilter(parentScopeName, parentFinalStage);
    }

    private Configuration createEPackageRegistryConfig(String scopeName, String stageName, String rsfName,
            String parentTarget) throws IOException {
        Configuration config = configAdmin.getFactoryConfiguration(EMFNamespaces.EPACKAGE_REGISTRY_CONFIG_NAME,
                scopeName + "-" + stageName, "?");
        Hashtable<String, Object> props = new Hashtable<>();
        props.put(EMFNamespaces.PROP_RESOURCE_SET_FACTORY_NAME, rsfName);
        props.put(EMFNamespaces.EPACKAGE_TARGET,
                "(&(" + EMFNamespaces.EMF_MODEL_SCOPE + "=" + scopeName + ")(" + WorkflowConstants.ATLAS_EPACKAGE_REGISTRATION_STAGE_PROPERTY
                        + "=" + stageName + "))");
        props.put("parentRegistry.target", parentTarget);
        config.update(props);
        return config;
    }

    private Configuration createResourceSetFactoryConfig(String scopeName, String stageName, String rsfName)
            throws IOException {
        Configuration config = configAdmin.getFactoryConfiguration(EMFNamespaces.RESOURCE_SET_FACTORY_CONFIG_NAME,
                scopeName + "-" + stageName, "?");
        Hashtable<String, Object> props = new Hashtable<>();
        props.put(EMFNamespaces.EPACKAGE_REGISTRY_TARGET, "(" + EMFNamespaces.PROP_RESOURCE_SET_FACTORY_NAME+ "="+rsfName+")");
        props.put("scope.name", scopeName);
        props.put("stage.name", stageName);
        config.update(props);
        return config;
    }

    private void deleteConfigs(String scopeName) {
        List<Configuration> configs = configsByScope.remove(scopeName);
        if (configs == null) {
            return;
        }
        configs.forEach(this::deleteQuietly);
    }

    private void deleteQuietly(Configuration config) {
        try {
            config.delete();
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Failed to delete configuration " + config.getPid(), e);
        }
    }

    private static String rsfName(String scopeName, String stageName) {
        return scopeName + "_" + stageName;
    }

    private static String rsfNameFilter(String scopeName, String stageName) {
        return "(rsf.name=" + rsfName(scopeName, stageName) + ")";
    }
}
