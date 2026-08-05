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

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.model.atlas.workflow.ResourceSetCollector;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.component.annotations.ReferenceScope;

/**
 * Default {@link ResourceSetCollector} implementation.
 *
 * @author ilenia
 * @since Apr 17, 2026
 */
@Component(immediate = true, name = "ResourceSetCollector", service = ResourceSetCollector.class)
public class ResourceSetCollectorImpl implements ResourceSetCollector {

    private static final Logger LOGGER = Logger.getLogger(ResourceSetCollectorImpl.class.getName());

    private final Map<Key, ComponentServiceObjects<ResourceSet>> resourceSetsByKey = new ConcurrentHashMap<>();
    private final Map<Key, ResourceSetFactory> resourceSetFactoryByKey = new ConcurrentHashMap<>();

    @Override
    public ComponentServiceObjects<ResourceSet> getResourceSetObjects(String scopeName, String stageName) {
        if (scopeName == null || stageName == null) {
            return null;
        }
        return resourceSetsByKey.get(new Key(scopeName, stageName));
    }

    @Override
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
