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
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Health check that verifies the EMF Registry has EPackages registered.
 *
 * @since 1.0
 */
@Component(scope = ServiceScope.PROTOTYPE, property = { HealthCheck.NAME + "=EMF Registry",
        HealthCheck.TAGS + "=atlas,readiness" })
public class EMFRegistryHealthCheck implements HealthCheck {

    @Reference(cardinality = ReferenceCardinality.OPTIONAL)
    private ResourceSet resourceSet;

    @Override
    public Result execute() {
        FormattingResultLog log = new FormattingResultLog();

        if (resourceSet == null) {
            log.critical("ResourceSet service not available");
        } else {
            int packageCount = resourceSet.getPackageRegistry().size();
            if (packageCount > 0) {
                log.info("EMF Registry contains {} EPackages", packageCount);
            } else {
                log.warn("EMF Registry is empty - no EPackages registered");
            }
        }

        return new Result(log);
    }
}
