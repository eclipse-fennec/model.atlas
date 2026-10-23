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

import java.util.List;

import org.eclipse.fennec.model.atlas.runtime.info.FailedPackageDTO;
import org.eclipse.fennec.model.atlas.runtime.info.FailedRegistryDTO;
import org.eclipse.fennec.model.atlas.runtime.info.FailedScopeDTO;

/**
 * What the component runtime knows is configured and not running.
 *
 * @param scopes     scopes whose component is not satisfied
 * @param registries registries that are configured but unusable
 * @param packages   schema packages that did not become live EPackage services
 */
record ScannedFailures(List<FailedScopeDTO> scopes, List<FailedRegistryDTO> registries,
        List<FailedPackageDTO> packages) {

    static ScannedFailures none() {
        return new ScannedFailures(List.of(), List.of(), List.of());
    }
}
