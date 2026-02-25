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

import org.apache.felix.hc.api.FormattingResultLog;
import org.apache.felix.hc.api.HealthCheck;
import org.apache.felix.hc.api.Result;
import org.eclipse.fennec.model.atlas.mediatypes.api.SupportedMediatype;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.eclipse.fennec.model.atlas.wf.workflowapi.ScopeService;

/**
 * Health check that verifies media type codecs are available.
 *
 * @since 1.0
 */
@Component(service = HealthCheck.class, property = { HealthCheck.NAME + "=Scopes And Registries",
        HealthCheck.TAGS + "=atlas,readiness" })
public class ScopesHealthCheck implements HealthCheck {

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    private ScopeService<?> scopesService;

    @Override
    public Result execute() {
        FormattingResultLog log = new FormattingResultLog();

        if (scopesService == null) {
            log.critical("No ScopeService found");
        } else {
            List<String> registries = scopesService.getAllRegistries();
            if (registries != null && !registries.isEmpty()) {
                registries.forEach(s -> log.info("{} available", s));
            } else {
                log.warn("No Regsitries available");
            }
        }

        return new Result(log);
    }
}
