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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.workflow.ActionContext;
import org.eclipse.fennec.model.atlas.workflow.ResourceSetCollector;
import org.eclipse.fennec.model.atlas.workflow.StageActionService;
import org.eclipse.fennec.model.atlas.workflow.registration.DynamicEPackageRegistrationService;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
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
    /** How long to wait for a fresh registration to become visible in the chain registry. */
    private static final long VISIBILITY_TIMEOUT_MS = 5_000;

    @Reference
    private DynamicEPackageRegistrationService registrationService;

    @Reference
    private ResourceSetCollector resourceSetCollector;

    private final BundleContext bundleContext;
    private final EObjectStorageService<EPackage> storageService;
    private final PromiseFactory promiseFactory = new PromiseFactory(null);
    private final Map<String, String> registeredNsURIs = new ConcurrentHashMap<>();
    /** objectId -&gt; the version (e.g. git commit SHA) it is currently registered at. */
    private final Map<String, String> registeredVersions = new ConcurrentHashMap<>();

    private final Set<String> triggerStages;
    private final boolean replayOnStartup;
    private final boolean replayOnShutdown;

    @Activate
    public EPackageStageActionService(BundleContext bundleContext, //
            @Reference(name = "storageService", target = "(scope=no-inject)") EObjectStorageService<EPackage> storageService, //
            Config config) {
        this.bundleContext = bundleContext;
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
            registeredVersions.remove(ctx.objectId());
            if (nsURI == null) {
                logger.fine(() -> "No tracked registration for " + ctx.objectId() + ", nothing to unregister");
                return null;
            }
            if (registrationService.unregisterEPackage(ctx.scope(), ctx.stage(), nsURI)) {
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

            // Idempotent replay: on a REPLAY dispatch (startup replay or a git registry resync
            // that re-enters every stage), if this object is already registered for its
            // (scope,stage) at the same version, do nothing — this keeps a broad replay from
            // needlessly unregistering-then-re-registering unchanged EPackages, which would
            // otherwise briefly flap them for consumers. Only replays skip; a genuine
            // upload/UPDATE (replay=false) always re-registers. A null/blank version (backends
            // that do not version) never matches, so their behaviour is unchanged.
            String version = metadata == null ? null : metadata.getVersion();
            String previousVersion = registeredVersions.get(ctx.objectId());
            if (ctx.replay() && version != null && !version.isBlank() && version.equals(previousVersion)
                    && registrationService.isRegistered(ctx.scope(), ctx.stage(), ePackage.getNsURI())) {
                logger.fine(() -> "EPackage " + ePackage.getNsURI() + " already registered for stage " + ctx.stage()
                        + " at version " + version + "; skipping re-registration");
                return null;
            }

            // Only explicitly unregister when the object's nsURI CHANGED between versions.
            // For an unchanged nsURI the registration service handles the update itself:
            // identical content is an idempotent no-op, changed content atomically replaces
            // the stale registration — either way the services do not flap through an
            // unregistered window as they would with unregister-then-register.
            String previous = registeredNsURIs.remove(ctx.objectId());
            if (previous != null && !previous.equals(ePackage.getNsURI())) {
                registrationService.unregisterEPackage(ctx.scope(), ctx.stage(), previous);
            }

            // registerEPackage returns false both for a genuine failure AND for "already
            // registered for this (scope,stage,nsURI)". The latter is benign — the package IS
            // registered (e.g. by a prior replay whose tracking this component instance does not
            // hold) — so treat it as success and ADOPT it into our tracking, so a later EXIT can
            // unregister it. Only a false result with the package genuinely absent is a failure.
            boolean fresh = registrationService.registerEPackage(ePackage, metadata);
            if (!fresh && !registrationService.isRegistered(ctx.scope(), ctx.stage(), ePackage.getNsURI())) {
                throw new IllegalStateException("Failed to register EPackage: " + ePackage.getNsURI());
            }
            registeredNsURIs.put(ctx.objectId(), ePackage.getNsURI());
            if (version != null) {
                registeredVersions.put(ctx.objectId(), version);
            }
            if (!ctx.replay()) {
                awaitRegistryVisibility(ctx.scope(), ctx.stage(), ePackage, fresh);
            }
            logger.info(() -> "Registered EPackage nsURI=" + ePackage.getNsURI() + " stage=" + ctx.stage()
                    + " replay=" + ctx.replay());
            return null;
        }).map(v -> null);
    }

    /**
     * Blocks (bounded) until the (scope, stage) chain ResourceSet's package
     * registry actually resolves the registered EPackage.
     *
     * <p>
     * {@code registerEPackage} publishes the EPackageConfigurator services, but
     * the chain EPackageRegistry binds them ASYNCHRONOUSLY on an SCR thread —
     * i.e. the package-registry mutation of ResourceSets that are already leased
     * out to request processing happens at an arbitrary later moment. An upload
     * response serialized against such a ResourceSet then races the mutation
     * (issue #196, intermittent 500 on schema create). Waiting here gives every
     * caller of the upload chain a happens-before guarantee: when the upload
     * promise resolves, the registry change is done.
     * </p>
     *
     * <p>
     * Event-driven, no periodic polling: the chain registry propagates its model
     * properties onto the (scope, stage) ResourceSet service registrations, so
     * every registry change surfaces as a REGISTERED/MODIFIED service event. A
     * listener on those events re-checks the actual registry content and opens a
     * latch; the content check is the truth, the events are only the wake-up.
     * </p>
     *
     * <p>
     * Startup replays skip the wait ({@code ctx.replay()}): during scope
     * activation the chain ResourceSets may not exist yet, and no response
     * depends on the replay. For a fresh registration the check requires the
     * registered INSTANCE (the chain registry may transiently still resolve the
     * previous instance, or — via its parent chain — a same-named package of
     * another stage); for an adopted pre-existing registration presence
     * suffices. A timeout is logged and does not fail the upload — that is
     * today's behaviour, minus the guarantee.
     * </p>
     */
    private void awaitRegistryVisibility(String scope, String stage, EPackage ePackage, boolean freshRegistration) {
        ComponentServiceObjects<ResourceSet> chain = resourceSetCollector.getResourceSetObjects(scope, stage);
        if (chain == null) {
            // No chain ResourceSet for this (scope, stage) - the dynamic registration
            // has no registry to land in, so there is nothing to synchronize with
            // (and nothing a response could race). Waiting would only stall setups
            // without the chain configurator (e.g. scope-less registries).
            return;
        }
        if (isVisibleInChainResourceSet(scope, stage, ePackage, freshRegistration)) {
            return;
        }
        CountDownLatch visible = new CountDownLatch(1);
        String filter = String.format("(&(objectClass=%s)(%s=%s)(%s=%s))", ResourceSet.class.getName(),
                ResourceSetCollector.SCOPE_NAME_PROPERTY, scope, ResourceSetCollector.STAGE_NAME_PROPERTY, stage);
        ServiceListener listener = event -> {
            int type = event.getType();
            if ((type == ServiceEvent.REGISTERED || type == ServiceEvent.MODIFIED)
                    && isVisibleInChainResourceSet(scope, stage, ePackage, freshRegistration)) {
                visible.countDown();
            }
        };
        try {
            bundleContext.addServiceListener(listener, filter);
            // re-check after installing the listener: the event may have fired in between
            if (isVisibleInChainResourceSet(scope, stage, ePackage, freshRegistration)
                    || visible.await(VISIBILITY_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                return;
            }
            logger.warning(() -> "EPackage " + ePackage.getNsURI() + " was registered for (" + scope + ", " + stage
                    + ") but did not become visible in the chain ResourceSet within " + VISIBILITY_TIMEOUT_MS
                    + "ms - responses may race the registry update");
        } catch (InvalidSyntaxException e) {
            logger.warning(() -> "Invalid chain ResourceSet listener filter " + filter + ": " + e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            bundleContext.removeServiceListener(listener);
        }
    }

    private boolean isVisibleInChainResourceSet(String scope, String stage, EPackage ePackage,
            boolean freshRegistration) {
        ComponentServiceObjects<ResourceSet> cso = resourceSetCollector.getResourceSetObjects(scope, stage);
        if (cso == null) {
            return false;
        }
        ResourceSet resourceSet = cso.getService();
        try {
            EPackage resolved = resourceSet.getPackageRegistry().getEPackage(ePackage.getNsURI());
            return freshRegistration ? resolved == ePackage : resolved != null;
        } finally {
            cso.ungetService(resourceSet);
        }
    }


}
