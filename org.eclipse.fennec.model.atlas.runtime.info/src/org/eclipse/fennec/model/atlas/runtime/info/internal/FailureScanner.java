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
import org.osgi.service.component.runtime.dto.UnsatisfiedReferenceDTO;

/**
 * Reads what is configured and not running out of the component runtime.
 *
 * <p>
 * This is the half of the snapshot a service collector cannot supply. Declarative Services leaves an
 * unsatisfiable component unregistered and says nothing a client can see, so the component is
 * absent from every registry and from every collector that watches one. The component runtime is
 * where it is still visible, with the reference that was never satisfied and the filter that was
 * asked for.
 * </p>
 *
 * <p>
 * Two reasons in {@link FailureReasons} have no producer here yet:
 * {@code UNRECOGNISED_PROPERTY}, which needs each component's declared configuration type, and
 * {@code PACKAGE_REGISTRATION_FAILED}, which needs the registry contents a snapshot does not read.
 * They are declared because they are the shape of the answer, not because something fills them in.
 * </p>
 */
final class FailureScanner {

    /** Only this project's components are reported; the rest of the framework is not our subject. */
    private static final String ATLAS_PREFIX = "org.eclipse.fennec.model.atlas";

    private FailureScanner() {
    }

    /**
     * @param scr the component runtime, or {@code null} when it is not available — in which case
     *            nothing is reported rather than nothing being wrong
     * @return what is configured and not running
     */
    static ScannedFailures scan(ServiceComponentRuntime scr) {
        if (scr == null) {
            return ScannedFailures.none();
        }
        List<FailedScopeDTO> scopes = new ArrayList<>();
        List<FailedRegistryDTO> registries = new ArrayList<>();

        for (ComponentDescriptionDTO description : scr.getComponentDescriptionDTOs()) {
            if (description.bundle == null || description.bundle.symbolicName == null
                    || !description.bundle.symbolicName.startsWith(ATLAS_PREFIX)) {
                continue;
            }
            for (ComponentConfigurationDTO configuration : scr.getComponentConfigurationDTOs(description)) {
                if (configuration.state == ComponentConfigurationDTO.SATISFIED
                        || configuration.state == ComponentConfigurationDTO.ACTIVE) {
                    continue;
                }
                collect(description, configuration, scopes, registries);
            }
        }
        return new ScannedFailures(scopes, registries, List.of());
    }

    private static void collect(ComponentDescriptionDTO description, ComponentConfigurationDTO configuration,
            List<FailedScopeDTO> scopes, List<FailedRegistryDTO> registries) {

        String pid = pidOf(configuration, description);
        UnsatisfiedReferenceDTO[] unsatisfied = configuration.unsatisfiedReferences;

        if (unsatisfied == null || unsatisfied.length == 0) {
            scopes.add(failedScope(scopeNameOf(configuration), FailureReasons.SCOPE_SERVICE_UNSATISFIED, pid,
                    configuration.id, "component '" + description.name + "' is in state " + configuration.state));
            return;
        }

        for (UnsatisfiedReferenceDTO reference : unsatisfied) {
            String target = reference.target;
            String detail = "reference '" + reference.name + "'"
                    + (target == null ? " is unsatisfied" : " is unsatisfied with target " + target);

            if (target != null && !isValidFilter(target)) {
                scopes.add(failedScope(scopeNameOf(configuration), FailureReasons.INVALID_FILTER, pid,
                        configuration.id, "reference '" + reference.name + "' has target " + target
                                + ", which is not a valid filter"));
                continue;
            }

            String name = reference.name == null ? "" : reference.name.toLowerCase(Locale.ROOT);
            if (name.contains("storage")) {
                FailedRegistryDTO failed = new FailedRegistryDTO();
                failed.scope = scopeNameOf(configuration);
                failed.name = registryNameOf(configuration);
                failed.reason = FailureReasons.STORAGE_UNAVAILABLE;
                failed.pid = pid;
                failed.serviceId = configuration.id;
                failed.detail = detail;
                registries.add(failed);
            } else if (name.contains("registry")) {
                scopes.add(failedScope(scopeNameOf(configuration), FailureReasons.NO_REGISTRIES_BOUND, pid,
                        configuration.id, detail));
            } else {
                scopes.add(failedScope(scopeNameOf(configuration), FailureReasons.SCOPE_SERVICE_UNSATISFIED, pid,
                        configuration.id, detail));
            }
        }
    }

    private static boolean isValidFilter(String target) {
        try {
            FrameworkUtil.createFilter(target);
            return true;
        } catch (InvalidSyntaxException notAFilter) {
            return false;
        }
    }

    private static FailedScopeDTO failedScope(String name, String reason, String pid, long serviceId, String detail) {
        FailedScopeDTO failed = new FailedScopeDTO();
        failed.name = name;
        failed.reason = reason;
        failed.pid = pid;
        failed.serviceId = serviceId;
        failed.detail = detail;
        return failed;
    }

    private static String pidOf(ComponentConfigurationDTO configuration, ComponentDescriptionDTO description) {
        Object pid = property(configuration, "service.pid");
        return pid == null ? description.configurationPid == null || description.configurationPid.length == 0 ? null
                : description.configurationPid[0] : String.valueOf(pid);
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
