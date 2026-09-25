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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.m2x.model.compiled.CompiledPackage;
import org.eclipse.fennec.m2x.model.compiled.PackageEntry;
import org.eclipse.fennec.m2x.unit.api.Unit;
import org.eclipse.fennec.m2x.unit.api.UnitKey;
import org.eclipse.fennec.m2x.unit.api.UnitKind;
import org.eclipse.fennec.m2x.unit.api.UnitStoreException;
import org.eclipse.fennec.model.atlas.scope.api.RegistryType;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.workflow.dependency.SchemaDependent;
import org.eclipse.fennec.model.atlas.workflow.dependency.SchemaDependent.Kind;
import org.eclipse.fennec.model.atlas.workflow.dependency.SchemaDependentsContributor;
import org.osgi.service.component.annotations.Component;

/**
 * The unit-to-schema edge of the dependents query (issue #250): a compiled transformation
 * unit depends on every package its manifest lists, because that is what it was compiled
 * against. Every stored version of a unit counts - each is an object of the registry that
 * stops resolving when the package leaves the stage.
 */
@Component(name = "QvtUnitDependentsContributor", service = SchemaDependentsContributor.class, immediate = true)
public class QvtUnitDependentsContributor implements SchemaDependentsContributor {

    private static final Logger LOGGER = Logger.getLogger(QvtUnitDependentsContributor.class.getName());

    private static final String COMPILED_UNIT_TYPE = EcoreUtil.getURI(CompiledPackage.Literals.COMPILED_UNIT)
            .toString();

    @SuppressWarnings("unchecked")
    @Override
    public List<SchemaDependent> dependentsIn(String scope, RegistryService<?> registry, String stage, String nsURI) {
        if (registry.getRegistry() == null || registry.getRegistry().getType() != RegistryType.TRANSFORMATION) {
            return List.of();
        }
        AtlasUnitStore store = new AtlasUnitStore((RegistryService<EObject>) registry, scope, stage);
        List<SchemaDependent> found = new ArrayList<>();
        try {
            for (UnitKey key : store.allKeysNewestFirst()) {
                if (key.kind() != UnitKind.COMPILED) {
                    continue;
                }
                Optional<Unit> unit = store.get(key);
                if (unit.isEmpty() || !(unit.get() instanceof Unit.Packaged packaged)) {
                    continue;
                }
                for (PackageEntry entry : packaged.document().getManifest().getPackageEntry()) {
                    if (nsURI.equals(entry.getNsURI())) {
                        found.add(new SchemaDependent(Kind.UNIT, registry.getRegistryName(), stage,
                                QvtUnits.objectId(key), COMPILED_UNIT_TYPE));
                        break;
                    }
                }
            }
        } catch (UnitStoreException e) {
            LOGGER.log(Level.WARNING, e, () -> "Cannot determine which units in (" + scope + ", "
                    + registry.getRegistryName() + ", " + stage + ") depend on " + nsURI);
        }
        return found;
    }
}
