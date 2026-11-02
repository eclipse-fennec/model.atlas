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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.runtime.info.internal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.eclipse.fennec.model.atlas.runtime.info.FailureReasons;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Bundle;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.osgi.service.component.runtime.dto.ReferenceDTO;
import org.osgi.service.component.runtime.dto.UnsatisfiedReferenceDTO;
import org.osgi.util.promise.Promise;

@DisplayName("FailureScanner — which inactive components are reported, and as what")
class FailureScannerTest {

    @Test
    @DisplayName("an inactive component that is neither scope nor registry is not reported")
    void unrelatedComponentIsIgnored() {
        ScannedFailures failures = FailureScanner.scan(scr(
                description("SomeRestResource", "jakarta.ws.rs.core.Feature"),
                configuration(ComponentConfigurationDTO.UNSATISFIED_REFERENCE, Map.of(),
                        unsatisfied("resourceSet", null))));

        assertTrue(failures.scopes().isEmpty());
        assertTrue(failures.registries().isEmpty());
    }

    @Test
    @DisplayName("a scope whose registry reference is unsatisfied reports NO_REGISTRIES_BOUND")
    void scopeWithoutRegistries() {
        ScannedFailures failures = FailureScanner.scan(scr(
                description("ScopeService", FailureScanner.SCOPE_SERVICE),
                configuration(ComponentConfigurationDTO.UNSATISFIED_REFERENCE, Map.of("scope.name", "jena"),
                        unsatisfied("registryService", "(scope=jena)"))));

        assertEquals(1, failures.scopes().size());
        assertEquals("jena", failures.scopes().get(0).name);
        assertEquals(FailureReasons.NO_REGISTRIES_BOUND, failures.scopes().get(0).reason);
    }

    @Test
    @DisplayName("a registry is reported as a registry, by storage or otherwise")
    void registryFailuresAreRegistries() {
        ScannedFailures failures = FailureScanner.scan(scr(
                description("RegistryService", FailureScanner.REGISTRY_SERVICE),
                configuration(ComponentConfigurationDTO.UNSATISFIED_REFERENCE,
                        Map.of("registry.name", "schema", "atlas.scope", "jena"),
                        unsatisfied("storageService", "(scope=jena)"), unsatisfied("schemaPackage", null))));

        assertTrue(failures.scopes().isEmpty(), "a registry is not a failed scope");
        assertEquals(2, failures.registries().size());
        assertEquals(FailureReasons.STORAGE_UNAVAILABLE, failures.registries().get(0).reason);
        assertEquals(FailureReasons.REGISTRY_SERVICE_UNSATISFIED, failures.registries().get(1).reason);
        assertEquals("schema", failures.registries().get(1).name);
        assertEquals("jena", failures.registries().get(1).scope);
    }

    @Test
    @DisplayName("a constructor-injected reference is classified by the interface it binds")
    void constructorReferenceIsClassifiedByInterface() {
        ComponentDescriptionDTO description = description("AtlasSchemaRegistryService", FailureScanner.REGISTRY_SERVICE);
        ReferenceDTO declared = new ReferenceDTO();
        declared.name = "$000";
        declared.interfaceName = "org.eclipse.fennec.model.atlas.management.EObjectStorageService";
        description.references = new ReferenceDTO[] { declared };

        ScannedFailures failures = FailureScanner.scan(scr(description,
                configuration(ComponentConfigurationDTO.UNSATISFIED_REFERENCE, Map.of(), unsatisfied("$000", null))));

        assertEquals(FailureReasons.STORAGE_UNAVAILABLE, failures.registries().get(0).reason);
        assertTrue(failures.registries().get(0).detail.contains("EObjectStorageService"),
                failures.registries().get(0).detail);
    }

    @Test
    @DisplayName("a failed activation carries the failure's first line")
    void failedActivationNamesTheFailure() {
        ComponentConfigurationDTO configuration = configuration(ComponentConfigurationDTO.FAILED_ACTIVATION,
                Map.of("registry.name", "schema"));
        configuration.failure = "java.lang.IllegalArgumentException: bad stage\n\tat Foo.bar(Foo.java:1)";

        ScannedFailures failures = FailureScanner.scan(
                scr(description("RegistryService", FailureScanner.REGISTRY_SERVICE), configuration));

        String detail = failures.registries().get(0).detail;
        assertTrue(detail.contains("FAILED_ACTIVATION") && detail.endsWith("bad stage"), detail);
    }

    @Test
    @DisplayName("an invalid target filter is named as such")
    void invalidFilter() {
        ScannedFailures failures = FailureScanner.scan(scr(
                description("ScopeService", FailureScanner.SCOPE_SERVICE),
                configuration(ComponentConfigurationDTO.UNSATISFIED_REFERENCE, Map.of("scope.name", "jena"),
                        unsatisfied("registryService", "(scope=jena"))));

        assertEquals(FailureReasons.INVALID_FILTER, failures.scopes().get(0).reason);
    }

    private static ComponentDescriptionDTO description(String name, String service) {
        ComponentDescriptionDTO description = new ComponentDescriptionDTO();
        description.name = name;
        description.serviceInterfaces = new String[] { service };
        description.configurationPid = new String[] { name };
        return description;
    }

    private static ComponentConfigurationDTO configuration(int state, Map<String, Object> properties,
            UnsatisfiedReferenceDTO... unsatisfied) {
        ComponentConfigurationDTO configuration = new ComponentConfigurationDTO();
        configuration.state = state;
        configuration.id = 42;
        configuration.properties = properties;
        configuration.unsatisfiedReferences = unsatisfied;
        return configuration;
    }

    private static UnsatisfiedReferenceDTO unsatisfied(String name, String target) {
        UnsatisfiedReferenceDTO reference = new UnsatisfiedReferenceDTO();
        reference.name = name;
        reference.target = target;
        return reference;
    }

    private static ServiceComponentRuntime scr(ComponentDescriptionDTO description,
            ComponentConfigurationDTO configuration) {
        return new ServiceComponentRuntime() {

            @Override
            public Collection<ComponentDescriptionDTO> getComponentDescriptionDTOs(Bundle... bundles) {
                return List.of(description);
            }

            @Override
            public ComponentDescriptionDTO getComponentDescriptionDTO(Bundle bundle, String name) {
                return description;
            }

            @Override
            public Collection<ComponentConfigurationDTO> getComponentConfigurationDTOs(
                    ComponentDescriptionDTO ignored) {
                return List.of(configuration);
            }

            @Override
            public boolean isComponentEnabled(ComponentDescriptionDTO ignored) {
                return true;
            }

            @Override
            public Promise<Void> enableComponent(ComponentDescriptionDTO ignored) {
                throw new UnsupportedOperationException();
            }

            @Override
            public Promise<Void> disableComponent(ComponentDescriptionDTO ignored) {
                throw new UnsupportedOperationException();
            }
        };
    }
}
