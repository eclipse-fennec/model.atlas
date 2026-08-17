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

import java.util.List;
import java.util.StringJoiner;

import org.apache.felix.hc.api.FormattingResultLog;
import org.apache.felix.hc.api.HealthCheck;
import org.apache.felix.hc.api.Result;
import org.eclipse.fennec.model.atlas.scope.api.RegistryInfo;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Registry;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Scope;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;
import org.eclipse.fennec.model.atlas.scope.api.StageInfo;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * Health check that verifies available scopes.
 *
 * @since 1.0
 */
@Component(service = HealthCheck.class, property = { HealthCheck.NAME + "=Scopes And Registries",
        HealthCheck.TAGS + "=atlas,readiness" })
public class ScopesHealthCheck implements HealthCheck {

    /**
     * Scopes are configuration-driven, so ScopeServices appear and disappear while
     * this check is active. The reference must therefore be DYNAMIC and GREEDY: on
     * the DS defaults (static, reluctant) a ScopeService published after activation
     * is never bound, and readiness keeps answering from the set seen at activation.
     */
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.DYNAMIC, policyOption = ReferencePolicyOption.GREEDY)
    private volatile List<ScopeService<?>> scopesServices;

    @Override
    public Result execute() {
        FormattingResultLog log = new FormattingResultLog();

        if (scopesServices == null || scopesServices.isEmpty()) {
            log.critical("No ScopeServices found");
        } else {
            for (ScopeService<?> scopeService : scopesServices) {
                Scope scope = scopeService.getScope();
                List<RegistryInfo> registries = scope.getRegistries();
                if (registries != null && !registries.isEmpty()) {
                    registries.stream().filter(r -> r instanceof Registry).map(r -> (Registry) r).forEach(r -> {
                	StringJoiner stages = new StringJoiner(", ");
                	r.getStages().stream().map(StageInfo::getName).forEach(stages::add);
                	log.info("scope: {} with Registry: {} Description: {} and Stages : {} available", scope.getName(), r.getName(), r.getDescription(), stages);
                    });
                } else {
                    log.warn("No Registries available");
                }
            }
        }

        return new Result(log);
    }
}
