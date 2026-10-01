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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.m2x.model.compiled.CompiledPackage;
import org.eclipse.fennec.m2x.model.compiled.CompiledUnit;
import org.eclipse.fennec.m2x.model.compiled.DependencyEntry;
import org.eclipse.fennec.m2x.model.compiled.SourceUnit;
import org.eclipse.fennec.m2x.qvto.api.QvtoEngine;
import org.eclipse.fennec.m2x.qvto.api.QvtoParseException;
import org.eclipse.fennec.m2x.unit.api.Unit;
import org.eclipse.fennec.m2x.unit.api.UnitKey;
import org.eclipse.fennec.m2x.unit.api.UnitKind;
import org.eclipse.fennec.m2x.unit.api.UnitStoreException;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.action.api.ActionContext;
import org.eclipse.fennec.model.atlas.workflow.RegistryServiceCollector;
import org.eclipse.fennec.model.atlas.workflow.ResourceSetCollector;
import org.eclipse.fennec.model.atlas.action.api.StageActionService;
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
 * <li><b>Invalid source</b> → stays stored, and why it does not compile is
 * recorded as {@link Diagnostic}s on the source's own {@code ObjectMetadata}
 * (issue #327): one {@code qvto.does-not-compile} root with one
 * {@code qvto.compiler-finding} child per compiler message, placed at its line
 * and column. The upload itself never fails.</li>
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
 * (re)derived per stage and never transition themselves. Whether the source
 * may enter that stage at all is decided beforehand by the
 * {@link QvtTransitionGate}, which compiles it against the same view (issue
 * #248); by the time this action runs for a transition, the source is known
 * to compile there.
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

    /**
     * The producer the compile outcome is recorded under.
     * <p>
     * Deliberately not the component name: the workflow records what a stage action <em>made of an
     * event</em> under {@code stage-action/QvtStageActionService}, and those two sitting side by
     * side meaning different things is exactly the confusion to avoid. This one says what the
     * compiler found; that one says whether the action itself ran.
     */
    public static final String PRODUCER = "QvtCompile";

    private static final Logger logger = Logger.getLogger(QvtStageActionService.class.getName());

    /**
     * The root a source carries when it <em>does</em> compile.
     * <p>
     * Written rather than left out, because absence is ambiguous: "it compiled" and "nothing ever
     * ran here" would look identical, and a stage action that binds to no registry fires silently.
     * It is not a content-free tick either - it names the unit the source produced, which is the
     * one thing a caller otherwise has to make a second lookup for.
     */
    public static final String CODE_COMPILES = "qvto.compiles";

    /** How many compiler findings the root spells out before it counts the rest. */
    private static final int MAX_REPORTED_FINDINGS = 5;
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
        return Set.of(ActionEvent.ENTER, ActionEvent.UPDATE, ActionEvent.EXIT);
    }

    @Override
    public Promise<Void> onEnter(ActionContext ctx) {
        return compileAction(ctx);
    }

    @Override
    public Promise<Void> onUpdate(ActionContext ctx) {
        return compileAction(ctx);
    }

    /**
     * Nothing to do on either exit.
     * <p>
     * The compile outcome now lives on the source's own metadata, so a deleted source takes it with
     * it and a transitioned one carries it along - which is what the separate diagnostics document
     * had to be cleaned up by hand for (issue #327). Units stay in the stage they were derived in:
     * they are versioned and possibly pinned by consumers.
     */
    @Override
    public Promise<Void> onExit(ActionContext ctx) {
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
                QvtoEngine engine = QvtStageEngines.engineFor(store, resourceSet);
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
     * Compiles one source and stores the outcome: the unit (startable root), and why it does not
     * compile - if it does not - onto the source's own metadata. Returns the qualified names whose
     * compiled unit changed (input for the dependent cascade), or an empty set.
     */
    private Set<String> compileOne(QvtoEngine engine, AtlasUnitStore store,
            RegistryService<EObject> registryService, ActionContext ctx, String qualifiedName, String sourceText) {
        List<Diagnostic> findings = new ArrayList<>();
        Set<String> changed = new LinkedHashSet<>();
        try {
            CompiledUnit unit = engine.compile(sourceText, qualifiedName);
            // a library's compiled form is what prepare loads for dependents — it is
            // stored just like a startable unit (the double-put the compiled-units
            // guide warns about); only the wording of the log differs
            boolean library = QvtUnits.isLibrary(unit);
            UnitKey key = store.put(unit);
            changed.add(qualifiedName);
            findings.add(compiles(key.fingerprint().orElse(null), library));
            logger.info(() -> (library ? "Stored library " : "Compiled ") + qualifiedName + " -> "
                    + key.fingerprint().orElse("?") + " in (" + ctx.scope() + ", " + ctx.stage() + ")");
        } catch (QvtoParseException e) {
            findings.add(doesNotCompile(summarise(e), compilerFindings(e)));
            logger.info(() -> "Source " + qualifiedName + " in (" + ctx.scope() + ", " + ctx.stage()
                    + ") is invalid; stored as draft with " + e.getErrors().size() + " diagnostics");
        } catch (UnitStoreException e) {
            findings.add(doesNotCompile("compiled, but the unit could not be stored: " + e.getMessage(), List.of()));
            logger.log(Level.WARNING, e,
                    () -> "Unit of " + qualifiedName + " could not be stored in (" + ctx.scope() + ", " + ctx.stage() + ")");
        } catch (RuntimeException e) {
            // an unexpected compiler failure must never leave the PREVIOUS outcome
            // standing as if it described this source
            findings.add(doesNotCompile("internal compiler error: " + e, List.of()));
            logger.log(Level.WARNING, e, () -> "Compiling " + qualifiedName + " in (" + ctx.scope() + ", "
                    + ctx.stage() + ") failed unexpectedly");
        }
        writeDiagnostics(registryService, ctx, qualifiedName, findings);
        return changed;
    }

    /**
     * The root a source carries when it compiled: which unit it produced, and whether that unit is
     * something an engine can start or a library others import. {@code INFO} - it is a statement
     * of fact, not a finding anybody has to act on.
     */
    private static Diagnostic compiles(String fingerprint, boolean library) {
        Diagnostic root = diagnostic(CODE_COMPILES, null,
                String.format("Compiled to %s (%s)", fingerprint == null ? "a unit with no fingerprint" : fingerprint,
                        library ? "a library others import" : "a startable transformation"));
        root.setSeverity(DiagnosticSeverity.INFO);
        return root;
    }

    /**
     * The root: this source does not compile against this stage's package view.
     * <p>
     * Same vocabulary as {@link QvtTransitionGate}, deliberately - a reader meets one set of codes
     * whether the compile happened on upload or while a promotion was being decided. The two keep
     * separate producers, because "does not compile here" and "may not enter there" are different
     * statements that have to be able to stand at the same time.
     */
    private static Diagnostic doesNotCompile(String reason, List<Diagnostic> children) {
        Diagnostic root = diagnostic(QvtTransitionGate.CODE_DOES_NOT_COMPILE, null, reason);
        root.getChildren().addAll(children);
        return root;
    }

    /** One child per compiler message, at {@code line:column}. */
    private static List<Diagnostic> compilerFindings(QvtoParseException e) {
        List<Diagnostic> children = new ArrayList<>();
        Map<String, Integer> seen = new HashMap<>();
        for (Resource.Diagnostic error : e.getErrors()) {
            String at = error.getLine() + ":" + error.getColumn();
            int occurrence = seen.merge(at, 1, Integer::sum);
            if (occurrence > 1) {
                // a diagnostic's id is derived from its code and its target, so two messages at one
                // position would be one node and the second would silently replace the first
                at = at + "#" + occurrence;
            }
            children.add(diagnostic(QvtTransitionGate.CODE_COMPILER_FINDING, at, error.getMessage()));
        }
        return children;
    }

    /** What the root says in one line: the first few findings, then a count of the rest. */
    private static String summarise(QvtoParseException e) {
        List<Resource.Diagnostic> errors = e.getErrors();
        if (errors.isEmpty()) {
            return e.getMessage();
        }
        StringBuilder reason = new StringBuilder(errors.stream().limit(MAX_REPORTED_FINDINGS)
                .map(d -> "line " + d.getLine() + ":" + d.getColumn() + " " + d.getMessage())
                .collect(Collectors.joining("; ")));
        if (errors.size() > MAX_REPORTED_FINDINGS) {
            reason.append(" and ").append(errors.size() - MAX_REPORTED_FINDINGS).append(" more");
        }
        return reason.toString();
    }

    private static Diagnostic diagnostic(String code, String target, String message) {
        Diagnostic diagnostic = ManagementFactory.eINSTANCE.createDiagnostic();
        diagnostic.setCode(code);
        diagnostic.setTarget(target);
        diagnostic.setSeverity(DiagnosticSeverity.ERROR);
        diagnostic.setCategory(QvtTransitionGate.CATEGORY);
        diagnostic.setMessage(message);
        return diagnostic;
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

    /**
     * Records what the compiler made of one source, onto that source's own metadata.
     * <p>
     * Always exactly one root, whichever way the compile went, so the outcome is readable without
     * knowing what the absence of a root would have meant - and writing the new one is what
     * replaces the old, so a source that has been fixed stops claiming it is broken.
     * <p>
     * Addressed by the qualified name rather than by {@code ctx.objectId()}, because the dependent
     * cascade compiles sources the event was not about.
     */
    private void writeDiagnostics(RegistryService<EObject> registryService, ActionContext ctx,
            String qualifiedName, List<Diagnostic> findings) {
        try {
            registryService.updateDiagnostics(ctx.scope(), ctx.stage(), qualifiedName, PRODUCER, findings).getValue();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (Exception e) {
            // the source is stored and the unit is where it belongs; the record of what happened is
            // a courtesy, and failing the upload over it would be worse
            logger.log(Level.WARNING, e, () -> "The compile outcome of " + qualifiedName
                    + " could not be recorded in (" + ctx.scope() + ", " + ctx.stage() + ")");
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
