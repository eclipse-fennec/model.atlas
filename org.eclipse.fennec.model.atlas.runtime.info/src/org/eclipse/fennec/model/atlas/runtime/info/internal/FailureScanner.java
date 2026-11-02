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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.eclipse.fennec.model.atlas.runtime.info.FailedRegistryDTO;
import org.eclipse.fennec.model.atlas.runtime.info.FailedScopeDTO;
import org.eclipse.fennec.model.atlas.runtime.info.FailureReasons;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.component.runtime.ServiceComponentRuntime;
import org.osgi.service.component.runtime.dto.ComponentConfigurationDTO;
import org.osgi.service.component.runtime.dto.ComponentDescriptionDTO;
import org.osgi.service.component.runtime.dto.ReferenceDTO;
import org.osgi.service.component.runtime.dto.UnsatisfiedReferenceDTO;

/**
 * Reads the scope and registry components that are configured but not running out of the
 * component runtime.
 *
 * <p>
 * DS leaves an unsatisfiable component unregistered, so it is absent from every service collector;
 * the component runtime is the one place it is still visible. Components are recognised by the
 * service they would provide, not by bundle or name, so only scopes and registries are reported —
 * any other inactive component is not this snapshot's subject.
 * </p>
 *
 * <p>
 * {@code UNRECOGNISED_PROPERTY} and {@code PACKAGE_REGISTRATION_FAILED} have no producer here yet.
 * </p>
 */
final class FailureScanner {

    static final String SCOPE_SERVICE = "org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService";

    static final String REGISTRY_SERVICE = "org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService";

    private FailureScanner() {
    }

    /**
     * @param scr the component runtime, or {@code null} when it is not available — in which case
     *            nothing is reported rather than nothing being wrong
     * @return the scope and registry components that are configured and not running
     */
    static ScannedFailures scan(ServiceComponentRuntime scr) {
        if (scr == null) {
            return ScannedFailures.none();
        }
        List<FailedScopeDTO> scopes = new ArrayList<>();
        List<FailedRegistryDTO> registries = new ArrayList<>();

        for (ComponentDescriptionDTO description : scr.getComponentDescriptionDTOs()) {
            boolean scope = provides(description, SCOPE_SERVICE);
            boolean registry = !scope && provides(description, REGISTRY_SERVICE);
            if (!scope && !registry) {
                continue;
            }
            for (ComponentConfigurationDTO configuration : scr.getComponentConfigurationDTOs(description)) {
                if (configuration.state == ComponentConfigurationDTO.SATISFIED
                        || configuration.state == ComponentConfigurationDTO.ACTIVE) {
                    continue;
                }
                if (scope) {
                    collectScope(description, configuration, scopes);
                } else {
                    collectRegistry(description, configuration, registries);
                }
            }
        }
        return new ScannedFailures(scopes, registries, List.of());
    }

    private static void collectScope(ComponentDescriptionDTO description, ComponentConfigurationDTO configuration,
            List<FailedScopeDTO> scopes) {
        String pid = pidOf(configuration, description);
        String scopeName = scopeNameOf(configuration);
        UnsatisfiedReferenceDTO[] unsatisfied = configuration.unsatisfiedReferences;

        if (unsatisfied == null || unsatisfied.length == 0) {
            scopes.add(failedScope(scopeName, FailureReasons.SCOPE_SERVICE_UNSATISFIED, pid, configuration.id,
                    stateDetail(description, configuration)));
            return;
        }
        for (UnsatisfiedReferenceDTO reference : unsatisfied) {
            String reason;
            String detail;
            if (reference.target != null && !isValidFilter(reference.target)) {
                reason = FailureReasons.INVALID_FILTER;
                detail = "reference '" + reference.name + "' has target " + reference.target
                        + ", which is not a valid filter";
            } else {
                reason = refersTo(description, reference, "registry") ? FailureReasons.NO_REGISTRIES_BOUND
                        : FailureReasons.SCOPE_SERVICE_UNSATISFIED;
                detail = referenceDetail(description, reference);
            }
            scopes.add(failedScope(scopeName, reason, pid, configuration.id, detail));
        }
    }

    private static void collectRegistry(ComponentDescriptionDTO description, ComponentConfigurationDTO configuration,
            List<FailedRegistryDTO> registries) {
        String pid = pidOf(configuration, description);
        UnsatisfiedReferenceDTO[] unsatisfied = configuration.unsatisfiedReferences;

        if (unsatisfied == null || unsatisfied.length == 0) {
            registries.add(failedRegistry(configuration, FailureReasons.REGISTRY_SERVICE_UNSATISFIED, pid,
                    stateDetail(description, configuration)));
            return;
        }
        for (UnsatisfiedReferenceDTO reference : unsatisfied) {
            String reason;
            String detail;
            if (reference.target != null && !isValidFilter(reference.target)) {
                reason = FailureReasons.INVALID_FILTER;
                detail = "reference '" + reference.name + "' has target " + reference.target
                        + ", which is not a valid filter";
            } else {
                reason = refersTo(description, reference, "storage") ? FailureReasons.STORAGE_UNAVAILABLE
                        : FailureReasons.REGISTRY_SERVICE_UNSATISFIED;
                detail = referenceDetail(description, reference);
            }
            registries.add(failedRegistry(configuration, reason, pid, detail));
        }
    }

    private static boolean provides(ComponentDescriptionDTO description, String service) {
        return description.serviceInterfaces != null && Arrays.asList(description.serviceInterfaces).contains(service);
    }

    private static String stateDetail(ComponentDescriptionDTO description, ComponentConfigurationDTO configuration) {
        String detail = "component '" + description.name + "' is in state " + stateName(configuration.state);
        return configuration.failure == null ? detail : detail + ": " + firstLine(configuration.failure);
    }

    private static String referenceDetail(ComponentDescriptionDTO description, UnsatisfiedReferenceDTO reference) {
        String service = interfaceOf(description, reference);
        return "reference '" + reference.name + "'" + (service == null ? "" : " to " + service)
                + (reference.target == null ? " is unsatisfied" : " is unsatisfied with target " + reference.target);
    }

    /**
     * Whether the reference is about the given kind of service, judged by its name and by the
     * interface it binds. Both, because constructor-injected references are named {@code $000},
     * {@code $001}, … and only their interface says what they are.
     */
    private static boolean refersTo(ComponentDescriptionDTO description, UnsatisfiedReferenceDTO reference,
            String kind) {
        return lower(reference.name).contains(kind) || lower(interfaceOf(description, reference)).contains(kind);
    }

    /** The interface the named reference binds, from the component description. */
    private static String interfaceOf(ComponentDescriptionDTO description, UnsatisfiedReferenceDTO reference) {
        if (description.references == null || reference.name == null) {
            return null;
        }
        for (ReferenceDTO declared : description.references) {
            if (reference.name.equals(declared.name)) {
                return declared.interfaceName;
            }
        }
        return null;
    }

    private static String stateName(int state) {
        return switch (state) {
            case ComponentConfigurationDTO.UNSATISFIED_CONFIGURATION -> "UNSATISFIED_CONFIGURATION";
            case ComponentConfigurationDTO.UNSATISFIED_REFERENCE -> "UNSATISFIED_REFERENCE";
            case ComponentConfigurationDTO.FAILED_ACTIVATION -> "FAILED_ACTIVATION";
            default -> String.valueOf(state);
        };
    }

    private static String firstLine(String text) {
        int newline = text.indexOf('\n');
        return (newline < 0 ? text : text.substring(0, newline)).strip();
    }

    private static boolean isValidFilter(String target) {
        try {
            FrameworkUtil.createFilter(target);
            return true;
        } catch (InvalidSyntaxException notAFilter) {
            return false;
        }
    }

    private static String lower(String value) {
        return value == null ? "" : value.toLowerCase(Locale.ROOT);
    }

    private static FailedScopeDTO failedScope(String name, String reason, String pid, long id, String detail) {
        FailedScopeDTO failed = new FailedScopeDTO();
        failed.name = name;
        failed.reason = reason;
        failed.pid = pid;
        failed.serviceId = id;
        failed.detail = detail;
        return failed;
    }

    private static FailedRegistryDTO failedRegistry(ComponentConfigurationDTO configuration, String reason,
            String pid, String detail) {
        FailedRegistryDTO failed = new FailedRegistryDTO();
        failed.scope = scopeNameOf(configuration);
        failed.name = registryNameOf(configuration);
        failed.reason = reason;
        failed.pid = pid;
        failed.serviceId = configuration.id;
        failed.detail = detail;
        return failed;
    }

    private static String pidOf(ComponentConfigurationDTO configuration, ComponentDescriptionDTO description) {
        Object pid = property(configuration, "service.pid");
        if (pid != null) {
            return String.valueOf(pid);
        }
        return description.configurationPid == null || description.configurationPid.length == 0 ? null
                : description.configurationPid[0];
    }

    private static String scopeNameOf(ComponentConfigurationDTO configuration) {
        Object scope = property(configuration, "atlas.scope");
        if (scope == null) {
            scope = property(configuration, "scope.name");
        }
        return scope == null ? null : String.valueOf(scope);
    }

    private static String registryNameOf(ComponentConfigurationDTO configuration) {
        Object registry = property(configuration, "registry.name");
        if (registry == null) {
            registry = property(configuration, "registry");
        }
        return registry == null ? null : String.valueOf(registry);
    }

    private static Object property(ComponentConfigurationDTO configuration, String key) {
        return configuration.properties == null ? null : configuration.properties.get(key);
    }
}
