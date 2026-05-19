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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.component.annotations.ReferenceScope;

/**
 * Tracks scope/stage-specific {@link ResourceSet} prototype services published
 * by {@code SchemaRegistryChainConfigurator}. Each tracked service carries the
 * {@code scope.name} and {@code stage.name} properties propagated from its
 * {@code ResourceSetFactory} configuration.
 *
 * <p>
 * Callers resolve the {@link ComponentServiceObjects} for a given (scope,
 * stage) pair and are responsible for {@link ComponentServiceObjects#getService()
 * getService()} / {@link ComponentServiceObjects#ungetService(Object)
 * ungetService()} lifecycle.
 * </p>
 *
 * @author ilenia
 * @since Apr 17, 2026
 */
@Component(immediate = true, name = "ResourceSetCollector", service = ResourceSetCollector.class)
public class ResourceSetCollector {

    public static final String SCOPE_NAME_PROPERTY = "scope.name";
    public static final String STAGE_NAME_PROPERTY = "stage.name";

    private static final Logger LOGGER = Logger.getLogger(ResourceSetCollector.class.getName());

    private final Map<Key, ComponentServiceObjects<ResourceSet>> resourceSetsByKey = new ConcurrentHashMap<>();
    private final Map<Key, ResourceSetFactory> resourceSetFactoryByKey = new ConcurrentHashMap<>();

    /**
     * Returns the {@link ComponentServiceObjects} for the {@link ResourceSet}
     * registered for the given (scope, stage) pair, or {@code null} if none is
     * currently bound.
     */
    public ComponentServiceObjects<ResourceSet> getResourceSetObjects(String scopeName, String stageName) {
        if (scopeName == null || stageName == null) {
            return null;
        }
        return resourceSetsByKey.get(new Key(scopeName, stageName));
    }
    
    public ResourceSetFactory getResourceSetFactory(String scopeName, String stageName) {
        if (scopeName == null || stageName == null) {
            return null;
        }
        return resourceSetFactoryByKey.get(new Key(scopeName, stageName));
    }

    @Reference(
            policy = ReferencePolicy.DYNAMIC,
            policyOption = ReferencePolicyOption.GREEDY,
            cardinality = ReferenceCardinality.MULTIPLE,
            scope = ReferenceScope.PROTOTYPE_REQUIRED,
            target = "(&(" + SCOPE_NAME_PROPERTY + "=*)(" + STAGE_NAME_PROPERTY + "=*))")
    public void bindResourceSet(ComponentServiceObjects<ResourceSet> cso, Map<String, Object> properties) {
        String scopeName = (String) properties.get(SCOPE_NAME_PROPERTY);
        String stageName = (String) properties.get(STAGE_NAME_PROPERTY);
        if (scopeName == null || scopeName.isBlank() || stageName == null || stageName.isBlank()) {
            LOGGER.severe(String.format(
                    "Cannot track ResourceSet without both %s and %s properties set", SCOPE_NAME_PROPERTY,
                    STAGE_NAME_PROPERTY));
            return;
        }
        ComponentServiceObjects<ResourceSet> previous = resourceSetsByKey.put(new Key(scopeName, stageName), cso);
        if (previous != null) {
            LOGGER.warning(String.format(
                    "ResourceSet for scope '%s' / stage '%s' already existed. Overriding", scopeName, stageName));
        }
    }

    public void unbindResourceSet(ComponentServiceObjects<ResourceSet> cso, Map<String, Object> properties) {
        String scopeName = (String) properties.get(SCOPE_NAME_PROPERTY);
        String stageName = (String) properties.get(STAGE_NAME_PROPERTY);
        if (scopeName == null || stageName == null) {
            return;
        }
        resourceSetsByKey.remove(new Key(scopeName, stageName), cso);
    }
    
    @Reference(
            policy = ReferencePolicy.DYNAMIC,
            policyOption = ReferencePolicyOption.GREEDY,
            cardinality = ReferenceCardinality.MULTIPLE,
            scope = ReferenceScope.PROTOTYPE_REQUIRED,
            target = "(&(" + SCOPE_NAME_PROPERTY + "=*)(" + STAGE_NAME_PROPERTY + "=*))")
    public void bindResourceSetFactory(ResourceSetFactory cso, Map<String, Object> properties) {
        String scopeName = (String) properties.get(SCOPE_NAME_PROPERTY);
        String stageName = (String) properties.get(STAGE_NAME_PROPERTY);
        if (scopeName == null || scopeName.isBlank() || stageName == null || stageName.isBlank()) {
            LOGGER.severe(String.format(
                    "Cannot track ResourceSet without both %s and %s properties set", SCOPE_NAME_PROPERTY,
                    STAGE_NAME_PROPERTY));
            return;
        }
        ResourceSetFactory previous = resourceSetFactoryByKey.put(new Key(scopeName, stageName), cso);
        if (previous != null) {
            LOGGER.warning(String.format(
                    "ResourceSet for scope '%s' / stage '%s' already existed. Overriding", scopeName, stageName));
        }
    }

    public void unbindResourceSetFactory(ResourceSetFactory cso, Map<String, Object> properties) {
        String scopeName = (String) properties.get(SCOPE_NAME_PROPERTY);
        String stageName = (String) properties.get(STAGE_NAME_PROPERTY);
        if (scopeName == null || stageName == null) {
            return;
        }
        resourceSetFactoryByKey.remove(new Key(scopeName, stageName), cso);
    }

    private record Key(String scopeName, String stageName) {
    }
}
