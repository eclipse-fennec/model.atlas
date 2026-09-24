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

import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.fennec.model.atlas.action.api.ActionContext;
import org.eclipse.fennec.model.atlas.action.api.StageActionService;
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
 * Keeps the dependents' <em>unresolved</em> state current as packages come and go (issue
 * #250, decision 5): a {@link StageActionService} on the schema registry.
 *
 * <ul>
 * <li>{@code EXIT} of a package from a stage - a delete, forced or not, or a transition that
 * removes the source copy - writes {@code schema.dependency-missing} to every object that
 * referenced the package in the view that stage served. A delete the {@link SchemaDeleteGate}
 * let through with {@code force} has recorded the same already; the write is idempotent.</li>
 * <li>{@code ENTER} of a package into a stage clears that finding from the dependents in the
 * views the stage now serves - the heal case: a dependency arriving later, or again, repairs
 * them. It runs on the startup replay too, so a restart catches up.</li>
 * </ul>
 *
 * <p>
 * Shutdown replays are ignored: the packages leave because the runtime stops, not because
 * anything changed. Wire it next to the {@code EPackageStageActionService} with
 * {@code stageActionService.target=(|(component.name=EPackageStageActionService)(component.name=SchemaDependencyStageAction))}.
 * </p>
 */
@Component(name = "SchemaDependencyStageAction", service = StageActionService.class, immediate = true)
public class SchemaDependencyStageAction implements StageActionService {

    private static final Logger LOGGER = Logger.getLogger(SchemaDependencyStageAction.class.getName());

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
    public Set<String> getTriggerStages() {
        return Set.of();
    }

    @Override
    public Set<ActionEvent> getTriggerEvents() {
        return Set.of(ActionEvent.ENTER, ActionEvent.EXIT);
    }

    @Override
    public boolean requiresReplayOnStartup() {
        return true;
    }

    @Override
    public boolean requiresReplayOnShutdown() {
        return false;
    }

    @Override
    public Promise<Void> onEnter(ActionContext ctx) {
        return run(ctx, "heal", this::heal);
    }

    @Override
    public Promise<Void> onUpdate(ActionContext ctx) {
        return Promises.resolved(null);
    }

    @Override
    public Promise<Void> onExit(ActionContext ctx) {
        if (ctx.replay()) {
            return Promises.resolved(null);
        }
        return run(ctx, "mark", this::mark);
    }

    private interface Step {
        void apply(String scope, String stage, String nsURI);
    }

    private Promise<Void> run(ActionContext ctx, String what, Step step) {
        String nsURI = SchemaDependencies.nsUriOf(ctx.metadata());
        if (nsURI == null) {
            LOGGER.fine(() -> "Package " + ctx.objectId() + " in stage '" + ctx.stage()
                    + "' carries no nsUri; nothing depends on it by nsURI, nothing to " + what);
            return Promises.resolved(null);
        }
        try {
            step.apply(ctx.scope(), ctx.stage(), nsURI);
            return Promises.resolved(null);
        } catch (RuntimeException e) {
            return Promises.failed(e);
        }
    }

    /** The package left {@code stage}: its dependents there are unresolved now. */
    void mark(String scope, String stage, String nsURI) {
        for (SchemaDependent dependent : dependents.dependentsOf(scope, stage, nsURI)) {
            RegistryService<?> registry = registryOf(dependent);
            if (registry == null) {
                continue;
            }
            ObjectMetadata current = registry.getMetadataFromStage(scope, dependent.stage(), dependent.objectId());
            if (current == null) {
                continue;
            }
            List<Diagnostic> owned = SchemaDependencies.ownedExcept(current, nsURI);
            owned.add(SchemaDependencies.missing(nsURI, stage));
            write(registry, scope, dependent, owned, "mark");
        }
    }

    /** The package is served from {@code stage} again: its dependents there resolve. */
    void heal(String scope, String stage, String nsURI) {
        for (SchemaDependent dependent : dependents.dependentsOf(scope, stage, nsURI)) {
            RegistryService<?> registry = registryOf(dependent);
            if (registry == null) {
                continue;
            }
            ObjectMetadata current = registry.getMetadataFromStage(scope, dependent.stage(), dependent.objectId());
            if (!SchemaDependencies.holdsMissing(current, nsURI)) {
                continue;
            }
            write(registry, scope, dependent, SchemaDependencies.ownedExcept(current, nsURI), "heal");
        }
    }

    private RegistryService<?> registryOf(SchemaDependent dependent) {
        RegistryService<?> registry = registries.getRegistryServiceByRegistryName(dependent.registry());
        if (registry == null) {
            LOGGER.warning(() -> "No registry '" + dependent.registry() + "' is known; the state of " + dependent.address()
                    + " cannot be recorded");
        }
        return registry;
    }

    private static void write(RegistryService<?> registry, String scope, SchemaDependent dependent,
            List<Diagnostic> owned, String what) {
        try {
            registry.updateDiagnostics(scope, dependent.stage(), dependent.objectId(), PRODUCER, owned).getValue();
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e, () -> "Cannot " + what + " " + dependent.address() + ": " + e.getMessage());
        }
    }
}
