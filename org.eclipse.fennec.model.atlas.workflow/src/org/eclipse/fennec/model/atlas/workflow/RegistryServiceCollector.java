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

import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
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
@Component(immediate = true, name = "RegistryServiceCollector", service = RegistryServiceCollector.class)
public class RegistryServiceCollector {

    private static final Logger LOGGER = Logger.getLogger(RegistryServiceCollector.class.getName());

    private Map<String, RegistryService<?>> registryServiceMap = new ConcurrentHashMap<>();

    public RegistryService<?> getRegistryServiceByRegistryName(String registryName) {
        return registryServiceMap.getOrDefault(registryName, null);
    }

    @Reference(policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY, cardinality = ReferenceCardinality.MULTIPLE)
    public void bindRegistryService(RegistryService<?> registryService, Map<String, Object> properties) {
        if (!properties.containsKey("registry.name") || ((String) properties.get("registry.name")).isEmpty()) {
            LOGGER.severe(String.format("Cannot store RegistryService with registry.name property not set or empty"));
            return;
        }
        String registryName = (String) properties.get("registry.name");
        if (registryServiceMap.containsKey(registryName)) {
            LOGGER.warning(
                    String.format("RegistryService with name %s already existed. This will override it", registryName));
        }
        registryServiceMap.put(registryName, registryService);
    }

    public void unbindRegistryService(RegistryService<?> registryService, Map<String, Object> properties) {
        if (!properties.containsKey("registry.name") || ((String) properties.get("registry.name")).isEmpty()) {
            return;
        }
        String registryName = (String) properties.get("registry.name");
        // Two-arg remove so unbinding a replaced service cannot wipe the
        // freshly bound replacement (DS binds the new service first)
        registryServiceMap.remove(registryName, registryService);
    }
}
