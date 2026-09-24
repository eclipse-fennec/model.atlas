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
package org.eclipse.fennec.model.atlas.workflow.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import org.eclipse.fennec.model.atlas.action.api.GateContext;
import org.eclipse.fennec.model.atlas.action.api.GateDiagnostic;
import org.eclipse.fennec.model.atlas.action.api.GateVerdict;
import org.eclipse.fennec.model.atlas.action.api.StageGate;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.workflow.RegistryServiceCollector;
import org.eclipse.fennec.model.atlas.workflow.dependency.SchemaDependent;
import org.eclipse.fennec.model.atlas.workflow.dependency.SchemaDependents;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.Promises;

/**
 * The schema delete guard of issue #250: a {@link StageGate} that refuses deleting a package
 * from a stage while objects in the view that stage serves still depend on it.
 *
 * <p>
 * The verdict carries one {@code schema.has-dependents} finding about the package, with a
 * child per dependent, and one {@code schema.dependency-missing} consequence per dependent.
 * A refusal records the finding on the package and answers {@code 409} over REST with the
 * package's metadata (issues #294, #295). A caller that forces the delete overrides the veto;
 * the registry then writes the consequences to the dependents before the package goes, which
 * puts them into the <em>unresolved</em> state the {@link SchemaDependencyStageAction} keeps
 * current from then on. A consequence re-emits what this producer already recorded on the
 * dependent about other packages, because the write replaces the producer's whole set.
 * </p>
 *
 * <p>
 * Wire it into a schema registry with {@code stageGate.target=(component.name=SchemaDeleteGate)}.
 * Transitions pass: promoting a package copies it, nothing stops resolving.
 * </p>
 */
@Component(name = "SchemaDeleteGate", service = StageGate.class, immediate = true)
public class SchemaDeleteGate implements StageGate {

    private static final Logger LOGGER = Logger.getLogger(SchemaDeleteGate.class.getName());

    public static final String PRODUCER = SchemaDependencies.PRODUCER;

    @Reference
    private SchemaDependents dependents;

    @Reference
    private RegistryServiceCollector registries;

    @Override
    public boolean supportsObjectType(String objectType) {
        return SchemaDependencies.isEPackage(objectType);
    }

    @Override
    public String producer() {
        return PRODUCER;
    }

    @Override
    public Promise<GateVerdict> beforeTransition(GateContext ctx) {
        return Promises.resolved(GateVerdict.pass());
    }

    @Override
    public Promise<GateVerdict> beforeDelete(GateContext ctx) {
        try {
            return Promises.resolved(decide(ctx));
        } catch (RuntimeException e) {
            return Promises.failed(e);
        }
    }

    private GateVerdict decide(GateContext ctx) {
        String nsURI = nsUriOf(ctx);
        if (nsURI == null) {
            LOGGER.fine(() -> "Package " + ctx.objectId() + " in stage '" + ctx.sourceStage()
                    + "' carries no nsUri; nothing can depend on it by nsURI");
            return GateVerdict.pass();
        }
        List<SchemaDependent> found = dependents.dependentsOf(ctx.scope(), ctx.sourceStage(), nsURI);
        if (found.isEmpty()) {
            return GateVerdict.pass();
        }
        List<GateDiagnostic> diagnostics = new ArrayList<>();
        diagnostics.add(SchemaDependencies.hasDependents(nsURI, ctx.sourceStage(), found));
        for (SchemaDependent dependent : found) {
            for (GateDiagnostic kept : alreadyRecorded(ctx.scope(), dependent, nsURI)) {
                diagnostics.add(kept.on(dependent.toDependent()));
            }
            diagnostics.add(SchemaDependencies.missingFinding(nsURI, ctx.sourceStage()).on(dependent.toDependent()));
        }
        return GateVerdict.refuse(SchemaDependencies.hasDependentsMessage(nsURI, ctx.sourceStage(), found.size()),
                diagnostics);
    }

    private String nsUriOf(GateContext ctx) {
        String fromContext = SchemaDependencies.nsUriOf(ctx.metadata());
        if (fromContext != null) {
            return fromContext;
        }
        RegistryService<?> registry = registries.getRegistryServiceByRegistryName(ctx.registry());
        return registry == null ? null
                : SchemaDependencies.nsUriOf(registry.getMetadataFromStage(ctx.scope(), ctx.sourceStage(), ctx.objectId()));
    }

    /** What this producer holds on the dependent about packages other than {@code nsURI}. */
    private List<GateDiagnostic> alreadyRecorded(String scope, SchemaDependent dependent, String nsURI) {
        RegistryService<?> registry = registries.getRegistryServiceByRegistryName(dependent.registry());
        if (registry == null) {
            return List.of();
        }
        ObjectMetadata metadata = registry.getMetadataFromStage(scope, dependent.stage(), dependent.objectId());
        List<GateDiagnostic> kept = new ArrayList<>();
        for (Diagnostic root : SchemaDependencies.ownedExcept(metadata, nsURI)) {
            kept.add(SchemaDependencies.toFinding(root));
        }
        return kept;
    }
}
