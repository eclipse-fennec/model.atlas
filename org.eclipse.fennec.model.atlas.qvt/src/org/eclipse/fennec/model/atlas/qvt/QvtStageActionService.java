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
package org.eclipse.fennec.model.atlas.qvt;

import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.m2x.model.compiled.CompiledPackage;
import org.eclipse.fennec.m2x.model.compiled.CompiledUnit;
import org.eclipse.fennec.m2x.model.compiled.DependencyEntry;
import org.eclipse.fennec.m2x.model.compiled.SourceUnit;
import org.eclipse.fennec.m2x.qvto.api.QvtoConfiguration;
import org.eclipse.fennec.m2x.qvto.api.QvtoEngine;
import org.eclipse.fennec.m2x.qvto.api.QvtoParseException;
import org.eclipse.fennec.m2x.qvto.engine.QvtoEngines;
import org.eclipse.fennec.m2x.qvto.engine.QvtoStoreUnitResolver;
import org.eclipse.fennec.m2x.unit.api.Unit;
import org.eclipse.fennec.m2x.unit.api.UnitKey;
import org.eclipse.fennec.m2x.unit.api.UnitKind;
import org.eclipse.fennec.m2x.unit.api.UnitStoreException;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.qvt.diagnostics.CompileStatus;
import org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticEntry;
import org.eclipse.fennec.model.atlas.qvt.diagnostics.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.qvt.diagnostics.QvtDiagnosticsFactory;
import org.eclipse.fennec.model.atlas.qvt.diagnostics.SourceDiagnostics;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.workflow.ActionContext;
import org.eclipse.fennec.model.atlas.workflow.RegistryServiceCollector;
import org.eclipse.fennec.model.atlas.workflow.ResourceSetCollector;
import org.eclipse.fennec.model.atlas.workflow.StageActionService;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * {@link StageActionService} that compiles QVT-O sources when they enter (or
 * are updated in) a stage of the transformation registry (issue #239).
 *
 * <p>
 * Behaviour per source upload, decided 2026-09-01 on the issue:
 * </p>
 * <ul>
 * <li><b>Invalid source</b> → stays stored; a {@link SourceDiagnostics}
 * document with per-finding line/column/severity is stored beside it
 * (status {@code INVALID}). The upload itself never fails.</li>
 * <li><b>Valid source with a startable root transformation</b> → compiled
 * (m2x default {@code pin} mode) against this (scope, stage)'s package view
 * and stored as a {@code CompiledUnit} in the same stage — draft units are
 * testable before release.</li>
 * <li><b>Valid library</b> → stored as a source only (status
 * {@code LIBRARY}); other compilations resolve it as a dependency.</li>
 * <li><b>Dependents recompile to the fixpoint</b>: units whose manifest
 * names a changed source recompile, then their dependents, until nothing
 * changes.</li>
 * </ul>
 *
 * <p>
 * A transition fires ENTER in the target stage, so the recompile against the
 * target stage's package view falls out of the same routine; units are
 * (re)derived per stage and never transition themselves.
 * </p>
 */
@Component(name = "QvtStageActionService", //
        service = StageActionService.class, //
        configurationPid = "QvtStageActionService", //
        configurationPolicy = ConfigurationPolicy.REQUIRE)
public class QvtStageActionService implements StageActionService {

    @ObjectClassDefinition(name = "QVT Stage Action Service")
    public @interface Config {

        @AttributeDefinition(name = "Trigger stages", //
                description = "Stages whose source ENTER/UPDATE events trigger compilation. Empty means all stages.")
        String[] trigger_stages() default {};
    }

    private static final Logger logger = Logger.getLogger(QvtStageActionService.class.getName());
    private static final String SOURCE_UNIT_TYPE = EcoreUtil.getURI(CompiledPackage.Literals.SOURCE_UNIT).toString();

    @Reference
    private RegistryServiceCollector registryCollector;

    @Reference
    private ResourceSetCollector resourceSetCollector;

    private final PromiseFactory promiseFactory = new PromiseFactory(null);
    private final Set<String> triggerStages;

    @Activate
    public QvtStageActionService(Config config) {
        this.triggerStages = Set.of(config.trigger_stages());
    }

    @Override
    public boolean supportsObjectType(String objectType) {
        return SOURCE_UNIT_TYPE.equals(objectType);
    }

    @Override
    public Set<String> getTriggerStages() {
        return triggerStages;
    }

    @Override
    public Set<ActionEvent> getTriggerEvents() {
        return Set.of(ActionEvent.ENTER, ActionEvent.UPDATE);
    }

    @Override
    public Promise<Void> onEnter(ActionContext ctx) {
        return compileAction(ctx);
    }

    @Override
    public Promise<Void> onUpdate(ActionContext ctx) {
        return compileAction(ctx);
    }

    @Override
    public Promise<Void> onExit(ActionContext ctx) {
        // units are derived per stage from their source and stay resolvable for
        // pinned consumers; nothing to undo when a source leaves a stage
        return promiseFactory.resolved(null);
    }

    @Override
    public boolean requiresReplayOnStartup() {
        // compile results are persistent documents, not runtime state — nothing
        // is lost over a restart, so a replay would only re-derive what exists
        return false;
    }

    @Override
    public boolean requiresReplayOnShutdown() {
        return false;
    }

    private Promise<Void> compileAction(ActionContext ctx) {
        return promiseFactory.submit(() -> {
            RegistryService<EObject> registryService = registryFor(ctx.registry());
            EObject content = registryService.getContentFromStage(ctx.scope(), ctx.stage(), ctx.objectId());
            if (!(content instanceof SourceUnit source)) {
                logger.fine(() -> "Object " + ctx.objectId() + " is no SourceUnit document; nothing to compile");
                return null;
            }
            String language = source.getLanguage() == null || source.getLanguage().isBlank() ? QvtUnits.LANGUAGE_QVTO
                    : source.getLanguage();
            if (!QvtUnits.LANGUAGE_QVTO.equals(language)) {
                logger.fine(() -> "Source " + source.getQualifiedName() + " is '" + language
                        + "'; this action compiles qvto only");
                return null;
            }
            AtlasUnitStore store = new AtlasUnitStore(registryService, ctx.scope(), ctx.stage());
            ComponentServiceObjects<ResourceSet> lease = resourceSetCollector.getResourceSetObjects(ctx.scope(),
                    ctx.stage());
            ResourceSet resourceSet = lease != null ? lease.getService() : null;
            try {
                QvtoEngine engine = engineFor(store, resourceSet);
                Set<String> changed = compileOne(engine, store, registryService, ctx,
                        source.getQualifiedName(), source.getSource());
                recompileDependentsToFixpoint(engine, store, registryService, ctx, changed);
            } finally {
                if (lease != null && resourceSet != null) {
                    lease.ungetService(resourceSet);
                }
            }
            return null;
        }).map(v -> null);
    }

    /**
     * Compiles one source and stores the outcome: the unit (startable root) and
     * the diagnostics document. Returns the qualified names whose compiled unit
     * changed (input for the dependent cascade), or an empty set.
     */
    private Set<String> compileOne(QvtoEngine engine, AtlasUnitStore store,
            RegistryService<EObject> registryService, ActionContext ctx, String qualifiedName, String sourceText) {
        SourceDiagnostics diagnostics = QvtDiagnosticsFactory.eINSTANCE.createSourceDiagnostics();
        diagnostics.setQualifiedName(qualifiedName);
        Set<String> changed = new LinkedHashSet<>();
        try {
            CompiledUnit unit = engine.compile(sourceText, qualifiedName);
            diagnostics.setSourceFingerprint(unit.getManifest().getSourceFingerprint());
            if (QvtUnits.isLibrary(unit)) {
                diagnostics.setCompileStatus(CompileStatus.LIBRARY);
                // the library's compiled form is what prepare loads for dependents —
                // store it too (the double-put the compiled-units guide warns about)
                UnitKey key = store.put(unit);
                diagnostics.setUnitFingerprint(key.fingerprint().orElse(null));
                changed.add(qualifiedName);
                logger.info(() -> "Stored library " + qualifiedName + " (" + key.fingerprint().orElse("?") + ") in ("
                        + ctx.scope() + ", " + ctx.stage() + ")");
            } else {
                UnitKey key = store.put(unit);
                diagnostics.setCompileStatus(CompileStatus.OK);
                diagnostics.setUnitFingerprint(key.fingerprint().orElse(null));
                changed.add(qualifiedName);
                logger.info(() -> "Compiled " + qualifiedName + " to unit " + key.fingerprint().orElse("?") + " in ("
                        + ctx.scope() + ", " + ctx.stage() + ")");
            }
        } catch (QvtoParseException e) {
            diagnostics.setCompileStatus(CompileStatus.INVALID);
            diagnostics.setMessage(e.getMessage());
            for (Resource.Diagnostic error : e.getErrors()) {
                DiagnosticEntry entry = QvtDiagnosticsFactory.eINSTANCE.createDiagnosticEntry();
                entry.setLine(error.getLine());
                entry.setColumn(error.getColumn());
                entry.setSeverity(DiagnosticSeverity.ERROR);
                entry.setMessage(error.getMessage());
                diagnostics.getEntries().add(entry);
            }
            logger.info(() -> "Source " + qualifiedName + " in (" + ctx.scope() + ", " + ctx.stage()
                    + ") is invalid; stored as draft with " + diagnostics.getEntries().size() + " diagnostics");
        } catch (UnitStoreException e) {
            diagnostics.setCompileStatus(CompileStatus.INVALID);
            diagnostics.setMessage("compiled, but the unit could not be stored: " + e.getMessage());
            logger.log(Level.WARNING, e,
                    () -> "Unit of " + qualifiedName + " could not be stored in (" + ctx.scope() + ", " + ctx.stage() + ")");
        }
        writeDiagnostics(registryService, ctx, diagnostics);
        return changed;
    }

    /**
     * Recompiles every unit whose manifest pins one of the changed sources,
     * then the dependents of everything that changed in that round, until the
     * fixpoint. A visited set keeps our own loop cycle-safe; import cycles
     * themselves are rejected by the m2x compiler under {@code pin}
     * (compiled-units guide §4).
     */
    private void recompileDependentsToFixpoint(QvtoEngine engine, AtlasUnitStore store,
            RegistryService<EObject> registryService, ActionContext ctx, Set<String> initiallyChanged) {
        Set<String> changed = new LinkedHashSet<>(initiallyChanged);
        Set<String> recompiled = new HashSet<>(initiallyChanged);
        while (!changed.isEmpty()) {
            Set<String> next = new LinkedHashSet<>();
            for (String dependent : dependentsOf(store, changed)) {
                if (!recompiled.add(dependent)) {
                    continue;
                }
                Optional<String> sourceText = newestSourceText(store, dependent);
                if (sourceText.isEmpty()) {
                    logger.warning(() -> "Unit " + dependent + " depends on a changed source but has no stored"
                            + " source in (" + ctx.scope() + ", " + ctx.stage() + "); it keeps its pinned versions");
                    continue;
                }
                next.addAll(compileOne(engine, store, registryService, ctx, dependent, sourceText.get()));
            }
            changed = next;
        }
    }

    /** The qualified names of compiled units whose manifest pins any of the given sources. */
    private Set<String> dependentsOf(AtlasUnitStore store, Set<String> changed) {
        Set<String> dependents = new LinkedHashSet<>();
        try {
            Set<String> seen = new HashSet<>();
            for (UnitKey key : allCompiledKeys(store)) {
                if (!seen.add(key.qualifiedName()) || changed.contains(key.qualifiedName())) {
                    continue; // newest version per name decides
                }
                Optional<Unit> unit = store.get(key);
                if (unit.isEmpty() || !(unit.get() instanceof Unit.Packaged packaged)) {
                    continue;
                }
                for (DependencyEntry dependency : packaged.document().getManifest().getDependencyEntry()) {
                    if (changed.contains(dependency.getQualifiedName())) {
                        dependents.add(key.qualifiedName());
                        break;
                    }
                }
            }
        } catch (UnitStoreException e) {
            logger.log(Level.WARNING, e, () -> "Cannot determine dependents of " + changed);
        }
        return dependents;
    }

    /** Every compiled unit key in the stage view, newest version of a name first. */
    private List<UnitKey> allCompiledKeys(AtlasUnitStore store) throws UnitStoreException {
        Set<String> names = new LinkedHashSet<>();
        List<UnitKey> keys = new java.util.ArrayList<>();
        for (UnitKey key : storeVersionsOfEverything(store)) {
            if (key.kind() == UnitKind.COMPILED && names.add(key.qualifiedName())) {
                keys.add(key);
            }
        }
        return keys;
    }

    private List<UnitKey> storeVersionsOfEverything(AtlasUnitStore store) throws UnitStoreException {
        // versions() per name needs the names first; scanning the registry listing
        // once gives both — delegated through the store to keep the id mapping in
        // one place
        return store.allKeysNewestFirst();
    }

    private Optional<String> newestSourceText(AtlasUnitStore store, String qualifiedName) {
        try {
            Optional<Unit> unit = store.get(UnitKey.of(QvtUnits.LANGUAGE_QVTO, qualifiedName, UnitKind.SOURCE));
            if (unit.isPresent() && unit.get() instanceof Unit.Source source) {
                return Optional.of(source.source());
            }
        } catch (UnitStoreException e) {
            logger.log(Level.WARNING, e, () -> "Cannot load the source of " + qualifiedName);
        }
        return Optional.empty();
    }

    private QvtoEngine engineFor(AtlasUnitStore store, ResourceSet resourceSet) {
        QvtoConfiguration.Builder builder = QvtoConfiguration.builder()
                .addUnitResolver(new QvtoStoreUnitResolver(store))
                .unitResolverEnabled(true);
        if (resourceSet != null) {
            // the per-(scope, stage) chain registry is the package view this stage
            // compiles against; its fp1 fingerprints enter the unit manifest
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

    private void writeDiagnostics(RegistryService<EObject> registryService, ActionContext ctx,
            SourceDiagnostics diagnostics) {
        String objectId = QvtUnits.diagnosticsObjectId(QvtUnits.LANGUAGE_QVTO, diagnostics.getQualifiedName());
        try {
            ObjectMetadata existing = registryService.getMetadataFromStage(ctx.scope(), ctx.stage(), objectId);
            if (existing != null) {
                registryService.updateInStage(ctx.scope(), ctx.stage(), diagnostics, objectId, existing.getVersion())
                        .getValue();
                return;
            }
            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setObjectId(objectId);
            metadata.setObjectName(diagnostics.getQualifiedName());
            metadata.setUploadTime(Instant.now());
            metadata.setObjectType(EcoreUtil.getURI(diagnostics.eClass()).toString());
            registryService.uploadToStage(ctx.scope(), ctx.stage(), diagnostics, metadata).getValue();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            logger.log(Level.WARNING, e, () -> "Diagnostics for " + diagnostics.getQualifiedName()
                    + " could not be stored in (" + ctx.scope() + ", " + ctx.stage() + ")");
        }
    }

    @SuppressWarnings("unchecked")
    private RegistryService<EObject> registryFor(String registryName) {
        RegistryService<?> registryService = registryCollector.getRegistryServiceByRegistryName(registryName);
        if (registryService == null) {
            throw new IllegalStateException("No RegistryService for registry " + registryName);
        }
        return (RegistryService<EObject>) registryService;
    }
}
