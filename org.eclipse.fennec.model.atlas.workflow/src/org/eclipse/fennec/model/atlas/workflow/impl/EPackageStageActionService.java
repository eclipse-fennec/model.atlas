/*
 * Copyright (c) 2012 - 2025 Data In Motion and others.
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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.workflow.ActionContext;
import org.eclipse.fennec.model.atlas.workflow.StageActionService;
import org.eclipse.fennec.model.atlas.workflow.registration.DynamicEPackageRegistrationService;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.PromiseFactory;

/**
 * {@link StageActionService} that registers EPackages as OSGi services when
 * they enter (or are updated in) a configured stage, and unregisters them when
 * they leave it.
 *
 * <p>
 * Behaviour per event:
 * </p>
 * <ul>
 * <li>{@code ENTER} / {@code UPDATE}: load the EPackage from storage, extract
 * file extension and version from metadata, and register through
 * {@link DynamicEPackageRegistrationService}. Any prior registration for the
 * same object id is replaced.</li>
 * <li>{@code EXIT}: look up the {@code nsURI} captured at registration time
 * and unregister. The EPackage itself is no longer retrievable from storage
 * at this point.</li>
 * </ul>
 *
 * <p>
 * A transition {@code A -> B} where both stages are trigger stages resolves
 * to {@code EXIT(A)} (unregister) followed by {@code ENTER(B)} (register).
 * There is a brief window between the two calls where the EPackage is not
 * registered; acceptable for the current use case.
 * </p>
 */
@Component(name = "EPackageStageActionService", //
        service = StageActionService.class, //
        configurationPid = "EPackageStageActionService", //
        configurationPolicy = ConfigurationPolicy.REQUIRE)
public class EPackageStageActionService implements StageActionService {

    @ObjectClassDefinition(name = "EPackage Stage Action Service")
    public @interface Config {

        @AttributeDefinition(name = "Trigger stages", //
                description = "Stages whose ENTER/UPDATE/EXIT events trigger (un)registration.")
        String[] trigger_stages() default { "release" };

        @AttributeDefinition(name = "Replay on startup", //
                description = "Replay ENTER for every EPackage currently in a trigger stage at startup.")
        boolean replay_on_startup() default true;

        @AttributeDefinition(name = "Replay on shutdown", //
                description = "Replay EXIT for every EPackage currently in a trigger stage at shutdown.")
        boolean replay_on_shutdown() default true;
    }

    private static final Logger logger = Logger.getLogger(EPackageStageActionService.class.getName());
    private static final String EPACKAGE_TYPE = EcoreUtil.getURI(EcorePackage.Literals.EPACKAGE).toString();

    @Reference
    private DynamicEPackageRegistrationService registrationService;

    private final EObjectStorageService<EPackage> storageService;
    private final PromiseFactory promiseFactory = new PromiseFactory(null);
    private final Map<String, String> registeredNsURIs = new ConcurrentHashMap<>();

    private final Set<String> triggerStages;
    private final boolean replayOnStartup;
    private final boolean replayOnShutdown;

    @Activate
    public EPackageStageActionService( //
            @Reference(name = "storageService", target = "(scope=no-inject)") EObjectStorageService<EPackage> storageService, //
            Config config) {
        this.storageService = storageService;
        this.triggerStages = Set.copyOf(new HashSet<>(Set.of(config.trigger_stages())));
        this.replayOnStartup = config.replay_on_startup();
        this.replayOnShutdown = config.replay_on_shutdown();
    }

    @Override
    public boolean supportsObjectType(String objectType) {
        return EPACKAGE_TYPE.equals(objectType);
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
        return registerOrUpdate(ctx);
    }

    @Override
    public Promise<Void> onUpdate(ActionContext ctx) {
        return registerOrUpdate(ctx);
    }

    @Override
    public Promise<Void> onExit(ActionContext ctx) {
        return promiseFactory.submit(() -> {
            String nsURI = registeredNsURIs.remove(ctx.objectId());
            if (nsURI == null) {
                logger.fine(() -> "No tracked registration for " + ctx.objectId() + ", nothing to unregister");
                return null;
            }
            if (registrationService.unregisterEPackage(nsURI)) {
                logger.info(() -> "Unregistered EPackage " + nsURI + " (left stage " + ctx.stage() + ")");
            } else {
                logger.warning(() -> "Unregistration reported failure for EPackage " + nsURI);
            }
            return null;
        });
    }

    @Override
    public boolean requiresReplayOnStartup() {
        return replayOnStartup;
    }

    @Override
    public boolean requiresReplayOnShutdown() {
        return replayOnShutdown;
    }

    private Promise<Void> registerOrUpdate(ActionContext ctx) {
        return promiseFactory.submit(() -> {
            EPackage ePackage = storageService.retrieveObject(ctx.scope(), ctx.registry(), ctx.stage(), ctx.objectId())
                    .getValue();
            if (ePackage == null) {
                throw new IllegalStateException("EPackage not found in storage: " + ctx.objectId());
            }
            ObjectMetadata metadata = storageService
                    .retrieveMetadata(ctx.scope(), ctx.registry(), ctx.stage(), ctx.objectId()).getValue();

            String previous = registeredNsURIs.remove(ctx.objectId());
            if (previous != null) {
                registrationService.unregisterEPackage(previous);
            }

            if (!registrationService.registerEPackage(ePackage, metadata)) {
                throw new IllegalStateException("Failed to register EPackage: " + ePackage.getNsURI());
            }
            registeredNsURIs.put(ctx.objectId(), ePackage.getNsURI());
            logger.info(() -> "Registered EPackage nsURI=" + ePackage.getNsURI() + " stage=" + ctx.stage()
                    + " replay=" + ctx.replay());
            return null;
        }).map(v -> null);
    }


}
