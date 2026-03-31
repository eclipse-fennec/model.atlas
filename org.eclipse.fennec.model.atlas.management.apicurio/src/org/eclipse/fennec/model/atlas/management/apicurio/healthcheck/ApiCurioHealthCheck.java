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
package org.eclipse.fennec.model.atlas.management.apicurio.healthcheck;

import java.util.List;

import org.apache.felix.hc.api.FormattingResultLog;
import org.apache.felix.hc.api.HealthCheck;
import org.apache.felix.hc.api.Result;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.component.annotations.ServiceScope;

/**
 * Health check that verifies that APICurio is available.
 *
 * @since 1.0
 */
@Component(scope = ServiceScope.PROTOTYPE, property = { HealthCheck.NAME + "=ApiCurio Storage",
        HealthCheck.TAGS + "=atlas,readiness,apicurio" })
public class ApiCurioHealthCheck implements HealthCheck {

    @Reference(cardinality = ReferenceCardinality.MULTIPLE, target = "(storage.backend=apicurio)", policyOption = ReferencePolicyOption.GREEDY)
    private List<EObjectStorageService<EObject>> apiCurioStorages;

    @Override
    public Result execute() {
        FormattingResultLog log = new FormattingResultLog();

        if (apiCurioStorages == null || apiCurioStorages.isEmpty()) {
            log.critical("No ApiCurioStorage available");
        } else {
            apiCurioStorages.forEach(a -> log.info("Storage of type: " + a.getStorageType() + " available"));
        }

        return new Result(log);
    }
}
