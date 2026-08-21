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
 *     Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.workflow.impl;

import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.model.atlas.mgmt.storage.StageResourceSetProvider;
import org.eclipse.fennec.model.atlas.workflow.ResourceSetCollector;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * Exposes the per-(scope, stage) chain {@link ResourceSet}s tracked by the
 * {@link ResourceSetCollector} to the storage backends, so stored instances of
 * dynamically registered EPackages can be read back (issue #190). Lives in the
 * workflow bundle because the storage bundles must not depend on it — they only
 * see the {@link StageResourceSetProvider} interface from the management
 * bundle.
 */
@Component
public class ChainStageResourceSetProvider implements StageResourceSetProvider {

    @Reference
    private ResourceSetCollector resourceSetCollector;

    @Override
    public ComponentServiceObjects<ResourceSet> getResourceSetObjects(String scope, String stage) {
        return resourceSetCollector.getResourceSetObjects(scope, stage);
    }
}
