/**
 * Copyright (c) 2012 - 2025 Kentyou and others.
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
package org.eclipse.fennec.model.atlas.healthcheck;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringJoiner;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.felix.hc.api.FormattingResultLog;
import org.apache.felix.hc.api.HealthCheck;
import org.apache.felix.hc.api.Result;
import org.eclipse.fennec.model.atlas.runtime.info.AtlasRuntime;
import org.eclipse.fennec.model.atlas.runtime.info.AtlasRuntimeDTO;
import org.eclipse.fennec.model.atlas.runtime.info.FailedPackageDTO;
import org.eclipse.fennec.model.atlas.runtime.info.FailedRegistryDTO;
import org.eclipse.fennec.model.atlas.runtime.info.FailedScopeDTO;
import org.eclipse.fennec.model.atlas.runtime.info.FailureReasons;
import org.eclipse.fennec.model.atlas.runtime.info.RegistryRuntimeDTO;
import org.eclipse.fennec.model.atlas.runtime.info.ScopeRuntimeDTO;
import org.eclipse.fennec.model.atlas.runtime.info.StageRuntimeDTO;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * Readiness of the scopes and registries, read from the {@link AtlasRuntime} snapshot.
 *
 * <p>
 * The live scopes are reported as information. What makes this check fail is the snapshot's
 * failure half: a scope or registry that is configured but did not come up is invisible to anything
 * that only watches registered services, and it is exactly what a readiness probe has to catch.
 * </p>
 *
 * <p>
 * A scope or registry that is configured and not live — unsatisfied, or failed to activate — is
 * {@link Result.Status#CRITICAL CRITICAL}: the Atlas serves less than it was configured to. Findings
 * about a scope that is live but inconsistent (no registries, a duplicate schema registry, an
 * unresolved parent) are {@link Result.Status#WARN WARN}. No live scope at all is critical.
 * </p>
 *
 * @since 1.0
 */
@Component(service = HealthCheck.class, property = { HealthCheck.NAME + "=Scopes And Registries",
        HealthCheck.TAGS + "=atlas", HealthCheck.TAGS + "=readiness" })
public class ScopesHealthCheck implements HealthCheck {

    private final AtomicReference<AtlasRuntime> runtime = new AtomicReference<>();

    /**
     * Optional, so the check is registered — and fails loudly — when the runtime service is missing,
     * rather than being absent from the probe.
     */
    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC,
            policyOption = ReferencePolicyOption.GREEDY)
    void setRuntime(AtlasRuntime atlasRuntime) {
        runtime.set(atlasRuntime);
    }

    void unsetRuntime(AtlasRuntime atlasRuntime) {
        runtime.compareAndSet(atlasRuntime, null);
    }

    @Override
    public Result execute() {
        FormattingResultLog log = new FormattingResultLog();
        AtlasRuntime atlasRuntime = runtime.get();
        if (atlasRuntime == null) {
            log.critical("Atlas runtime service not available, scopes cannot be inspected");
            return new Result(log);
        }
        AtlasRuntimeDTO snapshot = atlasRuntime.snapshot();

        List<ScopeRuntimeDTO> scopes = nullSafe(snapshot.scopes);
        if (scopes.isEmpty()) {
            log.critical("No scopes are live");
        }
        for (ScopeRuntimeDTO scope : scopes) {
            for (RegistryRuntimeDTO registry : nullSafe(scope.registries)) {
                StringJoiner stages = new StringJoiner(", ");
                for (StageRuntimeDTO stage : nullSafe(registry.stages)) {
                    stages.add(stage.name);
                }
                log.info("scope: {} with Registry: {} Description: {} and Stages : {} available", scope.name,
                        registry.name, registry.description, stages);
            }
        }

        Set<String> liveScopes = new HashSet<>();
        scopes.forEach(scope -> liveScopes.add(scope.name));

        for (FailedScopeDTO failed : nullSafe(snapshot.failedScopes)) {
            if (liveScopes.contains(failed.name)) {
                log(log, Result.Status.WARN, "Scope {} is live but not as configured: {} - {}", failed.name,
                        failed.reason, failed.detail);
            } else {
                log(log, Result.Status.CRITICAL, "Scope {} is configured but not live: {} - {} (pid {})",
                        failed.name, failed.reason, failed.detail, failed.pid);
            }
        }
        for (FailedRegistryDTO failed : nullSafe(snapshot.failedRegistries)) {
            if (FailureReasons.DUPLICATE_SCHEMA_REGISTRY.equals(failed.reason)) {
                log(log, Result.Status.WARN, "Registry {} in scope {} is live but not as configured: {} - {}",
                        failed.name, failed.scope, failed.reason, failed.detail);
            } else {
                log(log, Result.Status.CRITICAL, "Registry {} in scope {} is configured but not live: {} - {} (pid {})",
                        failed.name, failed.scope, failed.reason, failed.detail, failed.pid);
            }
        }
        for (FailedPackageDTO failed : nullSafe(snapshot.failedPackages)) {
            log(log, Result.Status.WARN, "Package {} in scope {} is not live: {} - {}", failed.nsURI, failed.scope,
                    failed.reason, failed.detail);
        }
        return new Result(log);
    }

    private static void log(FormattingResultLog log, Result.Status status, String format, Object... args) {
        if (status == Result.Status.CRITICAL) {
            log.critical(format, args);
        } else {
            log.warn(format, args);
        }
    }

    private static <T> List<T> nullSafe(List<T> values) {
        return values == null ? List.of() : values;
    }
}
