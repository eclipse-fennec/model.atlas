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
 *     Kentyou - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.healthcheck;

import org.apache.felix.hc.api.FormattingResultLog;
import org.apache.felix.hc.api.HealthCheck;
import org.apache.felix.hc.api.Result;
import org.osgi.service.component.annotations.Component;

/**
 * Simple liveness health check that confirms the OSGi framework is running.
 *
 * @since 1.0
 */
@Component(service = HealthCheck.class, property = { HealthCheck.NAME + "=Liveness",
        HealthCheck.TAGS + "=atlas", HealthCheck.TAGS + "=liveness" })
public class LivenessHealthCheck implements HealthCheck {

    @Override
    public Result execute() {
        FormattingResultLog log = new FormattingResultLog();
        log.info("Model Atlas is alive");
        return new Result(log);
    }
}
