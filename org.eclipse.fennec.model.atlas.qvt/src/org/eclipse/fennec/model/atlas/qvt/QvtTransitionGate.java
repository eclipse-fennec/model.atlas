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

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.m2x.model.compiled.CompiledPackage;
import org.eclipse.fennec.m2x.model.compiled.SourceUnit;
import org.eclipse.fennec.m2x.qvto.api.QvtoEngine;
import org.eclipse.fennec.m2x.qvto.api.QvtoParseException;
import org.eclipse.fennec.model.atlas.action.api.GateContext;
import org.eclipse.fennec.model.atlas.action.api.GateVerdict;
import org.eclipse.fennec.model.atlas.action.api.StageGate;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.workflow.RegistryServiceCollector;
import org.eclipse.fennec.model.atlas.workflow.ResourceSetCollector;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * {@link StageGate} that lets a QVT-O source move to another stage only when it
 * compiles against that stage's view (issue #248).
 *
 * <p>
 * Validity is stage-relative: the target stage has its own package view and its
 * own set of promoted libraries, so a source that compiles in {@code draft} may
 * not compile in {@code release}, typically because a library it imports has
 * not been promoted yet. Before this gate the transition went through anyway
 * and the failure was visible only as the target-stage diagnostics document,
 * to be healed by a later recompile cascade. Now the transition is refused with
 * the compiler's findings, the target stage stays untouched, and the documented
 * rule "transition libraries first" is enforced rather than advised.
 * </p>
 *
 * <p>
 * The gate compiles and discards; storing the unit and the diagnostics in the
 * target stage remains the {@link QvtStageActionService}'s job on the ENTER
 * that follows a passed transition. Both use {@link QvtStageEngines} so they
 * see the same view. Content that is no {@code qvto} source passes; the gate
 * has nothing to say about it.
 * </p>
 */
@Component(name = "QvtTransitionGate", service = StageGate.class, immediate = true)
public class QvtTransitionGate implements StageGate {

    private static final Logger logger = Logger.getLogger(QvtTransitionGate.class.getName());
    private static final String SOURCE_UNIT_TYPE = EcoreUtil.getURI(CompiledPackage.Literals.SOURCE_UNIT).toString();
    /** How many compiler findings the refusal spells out before it counts the rest. */
    private static final int MAX_REPORTED_FINDINGS = 5;

    @Reference
    private RegistryServiceCollector registryCollector;

    @Reference
    private ResourceSetCollector resourceSetCollector;

    private final PromiseFactory promiseFactory = new PromiseFactory(null);

    @Override
    public boolean supportsObjectType(String objectType) {
        return SOURCE_UNIT_TYPE.equals(objectType);
    }

    @Override
    public Promise<GateVerdict> beforeTransition(GateContext ctx) {
        return promiseFactory.submit(() -> decide(ctx));
    }

    private GateVerdict decide(GateContext ctx) {
        RegistryService<EObject> registryService = registryFor(ctx.registry());
        EObject content = registryService.getContentFromStage(ctx.scope(), ctx.sourceStage(), ctx.objectId());
        if (!(content instanceof SourceUnit source)) {
            logger.fine(() -> "Object " + ctx.objectId() + " is no SourceUnit document; nothing to check");
            return GateVerdict.pass();
        }
        String language = source.getLanguage() == null || source.getLanguage().isBlank() ? QvtUnits.LANGUAGE_QVTO
                : source.getLanguage();
        if (!QvtUnits.LANGUAGE_QVTO.equals(language)) {
            logger.fine(() -> "Source " + source.getQualifiedName() + " is '" + language
                    + "'; this gate checks qvto only");
            return GateVerdict.pass();
        }
        // the TARGET stage's view: its unit store resolves the imports, its chain
        // ResourceSet is the package view the source has to compile against
        AtlasUnitStore targetStore = new AtlasUnitStore(registryService, ctx.scope(), ctx.targetStage());
        ComponentServiceObjects<ResourceSet> lease = resourceSetCollector.getResourceSetObjects(ctx.scope(),
                ctx.targetStage());
        ResourceSet resourceSet = lease != null ? lease.getService() : null;
        try {
            QvtoEngine engine = QvtStageEngines.engineFor(targetStore, resourceSet);
            engine.compile(source.getSource(), source.getQualifiedName());
            logger.fine(() -> "Source " + source.getQualifiedName() + " compiles against (" + ctx.scope() + ", "
                    + ctx.targetStage() + "); the transition may proceed");
            return GateVerdict.pass();
        } catch (QvtoParseException e) {
            String reason = describe(source.getQualifiedName(), ctx.targetStage(), e);
            logger.info(() -> "Refusing the transition of " + source.getQualifiedName() + " into (" + ctx.scope()
                    + ", " + ctx.targetStage() + "): " + reason);
            return GateVerdict.refuse(reason);
        } finally {
            if (lease != null && resourceSet != null) {
                lease.ungetService(resourceSet);
            }
        }
    }

    /**
     * Puts the compiler's findings into a sentence the caller can act on. The
     * usual cause of a failure that did not show in the source stage is an
     * import the target stage does not hold yet, so the remedy is named.
     */
    private static String describe(String qualifiedName, String targetStage, QvtoParseException e) {
        List<Resource.Diagnostic> errors = e.getErrors();
        StringBuilder reason = new StringBuilder("source '").append(qualifiedName)
                .append("' does not compile against the '").append(targetStage).append("' stage view");
        if (errors.isEmpty()) {
            reason.append(" (").append(e.getMessage()).append(")");
        } else {
            reason.append(": ").append(errors.stream().limit(MAX_REPORTED_FINDINGS)
                    .map(d -> "line " + d.getLine() + ":" + d.getColumn() + " " + d.getMessage())
                    .collect(Collectors.joining("; ")));
            if (errors.size() > MAX_REPORTED_FINDINGS) {
                reason.append(" and ").append(errors.size() - MAX_REPORTED_FINDINGS).append(" more");
            }
        }
        reason.append(". Transition the libraries it imports first, or fix the source in its current stage.");
        return reason.toString();
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
