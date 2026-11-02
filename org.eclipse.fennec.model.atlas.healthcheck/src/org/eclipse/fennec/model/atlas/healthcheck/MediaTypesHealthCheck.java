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
import java.util.concurrent.atomic.AtomicReference;

import org.apache.felix.hc.api.FormattingResultLog;
import org.apache.felix.hc.api.HealthCheck;
import org.apache.felix.hc.api.Result;
import org.eclipse.fennec.model.atlas.runtime.info.AtlasRuntime;
import org.eclipse.fennec.model.atlas.runtime.info.MediaTypeDTO;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;

/**
 * Health check that verifies media types are available, read from the {@link AtlasRuntime}
 * snapshot.
 *
 * <p>
 * The runtime reference is optional so the check is always registered: with a mandatory reference
 * a missing provider made the check itself disappear from the probe instead of failing it.
 * </p>
 *
 * @since 1.0
 */
@Component(service = HealthCheck.class, property = { HealthCheck.NAME + "=Media Types",
        HealthCheck.TAGS + "=atlas", HealthCheck.TAGS + "=readiness" })
public class MediaTypesHealthCheck implements HealthCheck {

    private final AtomicReference<AtlasRuntime> runtime = new AtomicReference<>();

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
            log.critical("Atlas runtime service not available, media types cannot be inspected");
            return new Result(log);
        }
        List<MediaTypeDTO> types = atlasRuntime.snapshot().mediaTypes;
        if (types != null && !types.isEmpty()) {
            log.info("Supporting {} media types", types.size());
        } else {
            log.warn("No media types available");
        }
        return new Result(log);
    }
}
