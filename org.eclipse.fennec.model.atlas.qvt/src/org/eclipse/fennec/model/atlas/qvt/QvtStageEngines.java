/*
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
package org.eclipse.fennec.model.atlas.qvt;

import java.util.Set;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.m2x.qvto.api.QvtoConfiguration;
import org.eclipse.fennec.m2x.qvto.api.QvtoEngine;
import org.eclipse.fennec.m2x.qvto.engine.QvtoEngines;
import org.eclipse.fennec.m2x.qvto.engine.QvtoStoreUnitResolver;

/**
 * Builds the QVT-O engine that compiles against one (scope, stage) view of the
 * Atlas.
 *
 * <p>
 * The same engine shape serves two callers: the {@link QvtStageActionService},
 * which compiles a source after it entered a stage, and the
 * {@link QvtTransitionGate}, which compiles it against the <em>target</em> stage
 * before the transition commits (issue #248). Both must see exactly the same
 * view, or the gate would pass what the action then fails on.
 * </p>
 */
final class QvtStageEngines {

    private QvtStageEngines() {
    }

    /**
     * Creates an engine whose imports resolve through the given unit store and
     * whose model types resolve through the given ResourceSet's package
     * registry.
     *
     * @param store       the unit store of the (scope, stage) view; imported
     *                    libraries resolve here
     * @param resourceSet the per-(scope, stage) chain ResourceSet, or
     *                    {@code null} when the stage has no package view; its
     *                    registry is the package view the stage compiles against,
     *                    and its fp1 fingerprints enter the unit manifest
     * @return the engine
     */
    static QvtoEngine engineFor(AtlasUnitStore store, ResourceSet resourceSet) {
        QvtoConfiguration.Builder builder = QvtoConfiguration.builder()
                .addUnitResolver(new QvtoStoreUnitResolver(store))
                .unitResolverEnabled(true);
        if (resourceSet != null) {
            EPackage.Registry packages = resourceSet.getPackageRegistry();
            for (String nsURI : Set.copyOf(packages.keySet())) {
                EPackage ePackage = packages.getEPackage(nsURI);
                if (ePackage != null) {
                    builder.registerPackage(ePackage);
                }
            }
        }
        return QvtoEngines.create(builder.build());
    }
}
