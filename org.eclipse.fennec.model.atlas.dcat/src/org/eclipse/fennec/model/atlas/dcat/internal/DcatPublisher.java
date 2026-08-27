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
package org.eclipse.fennec.model.atlas.dcat.internal;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.fennec.dcat.atlas.client.api.ConflictException;
import org.eclipse.fennec.dcat.atlas.client.api.DcatAtlasClient;
import org.eclipse.fennec.dcat.atlas.client.api.DcatCollection;
import org.eclipse.fennec.dcat.atlas.client.api.DcatModelConstraintException;
import org.eclipse.fennec.dcat.atlas.client.api.DcatShaclException;
import org.eclipse.fennec.dcat.atlas.client.api.DeleteMode;
import org.eclipse.fennec.dcat.atlas.client.api.NotFoundException;
import org.eclipse.fennec.dcat.atlas.client.api.Registration;
import org.eclipse.fennec.dcat.atlas.client.osgi.AsyncDcatAtlasClient;
import org.eclipse.fennec.model.atlas.dcat.api.DcatMetadataSource;
import org.eclipse.fennec.model.atlas.dcat.api.PublicationTarget;
import org.eclipse.fennec.model.atlas.mediatypes.api.SupportedMediatype;
import org.eclipse.fennec.model.atlas.scope.api.AtlasProperties;
import org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService;
import org.eclipse.fennec.model.atlas.scope.api.ScopeInfo;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.metatype.annotations.Designate;

import org.eclipse.emf.ecore.EPackage;

import dcat.Catalog;
import dcat.Dataset;
import dcat.Distribution;

/**
 * Publishes this atlas to one DCAT.Atlas portal.
 *
 * <p>
 * A factory component, one configuration per portal. Nothing is built for multi-portal, but
 * nothing assumes single either: a second portal is a second configuration with its own
 * {@code dcat.portal.target}.
 * </p>
 *
 * <p>
 * Every portal call goes through {@link AsyncDcatAtlasClient#submit}, so a DS bind, a REST upload
 * and a framework shutdown never wait on the network. Writes are idempotent {@code PUT}s keyed by
 * a caller-chosen id, which is what makes the publish sequence the normal path rather than a
 * repair.
 * </p>
 */
@Component(name = DcatPublisher.PID, //
        configurationPid = DcatPublisher.PID, //
        configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = DcatPublisherConfig.class, factory = true)
public class DcatPublisher {

    /** ConfigAdmin factory PID. */
    static final String PID = "DcatPublisher";

    private static final Logger LOGGER = Logger.getLogger(DcatPublisher.class.getName());

    /**
     * How long a shutdown retirement may block. Bounded because the alternative is holding up the
     * framework on a portal that may be down, and a Dataset left listed is recoverable — the next
     * start republishes it.
     */
    private static final long SHUTDOWN_RETIRE_TIMEOUT_MS = 10_000;

    /**
     * Named so the DS target property is {@code dcat.portal.target}, which is also the name the
     * configuration uses — one key, not two spellings of the same thing.
     */
    @Reference(name = "dcat.portal")
    private AsyncDcatAtlasClient client;

    private final Map<String, ScopeInfo> scopes = new ConcurrentHashMap<>();

    /** Catalog configurations by scope; a scope without one is the derived case. */
    private final Map<String, CatalogSettings> scopeCatalogs = new ConcurrentHashMap<>();

    /**
     * Scopes this publisher will not touch, and why: a broken {@code DcatScopeCatalog}, or an
     * adopted Catalog the portal does not have. Kept so the refusal is logged once rather than per
     * event, and so nothing of that scope is published while it stands.
     */
    private final Map<String, String> refusedScopes = new ConcurrentHashMap<>();

    private volatile DcatPublisherConfig config;
    private volatile String baseUri;
    private volatile Set<String> publishedScopes = Set.of();
    private volatile DcatMapper mapper;
    private volatile boolean active;
    private volatile Set<String> mediaTypes = Set.of();
    private volatile Set<String> permittedStages = Set.of();
    private volatile boolean allStages;
    private volatile UnpublishMode unpublishMode = UnpublishMode.UNLINK;
    private volatile long unpublishDelayMillis;
    private volatile boolean retireOnShutdown;
    private volatile BundleContext bundleContext;

    /**
     * Deferred retirements, so a re-register can cancel one. Created at activation and closed at
     * deactivation, because a pending retirement belongs to the configuration that asked for it.
     */
    private volatile RetirementQueue retirements;

    /**
     * Per published Dataset, the fingerprint last written. The startup ENTER replay re-registers
     * every package on every boot, so without this a restart would rewrite the whole catalogue —
     * two git commits per entity on a git-backed portal, for no change.
     */
    private final Map<String, String> publishedFingerprints = new ConcurrentHashMap<>();

    /** The tracked EPackage services, keyed by dataset id. */
    private final Map<String, TrackedPackage> trackedPackages = new ConcurrentHashMap<>();

    /**
     * Datasets this publisher has retired. A retirement drops the Catalog membership (and under
     * {@code DELETE}/{@code CASCADE} the resource), but leaves an unchanged fingerprint behind on
     * the portal — so a later re-publish of the same content would take the "already published"
     * shortcut and never re-assert the link. This set makes the next write unconditional.
     */
    private final Set<String> retiredDatasets = ConcurrentHashMap.newKeySet();

    /** An EPackage service plus the facts read off its properties at bind time. */
    private record TrackedPackage(PublicationTarget target, EPackage ePackage) {
    }

    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC,
            policyOption = ReferencePolicyOption.GREEDY)
    private volatile SupportedMediatype supportedMediatypes;

    /**
     * The metadata source whiteboard: highest {@code service.ranking} wins, and the configured
     * default fills in when nothing is registered.
     */
    @Reference(cardinality = ReferenceCardinality.OPTIONAL, policy = ReferencePolicy.DYNAMIC,
            policyOption = ReferencePolicyOption.GREEDY)
    private volatile DcatMetadataSource metadataSource;

    /**
     * Tracks every scope in the runtime. {@link ReadableScopeService} is the read-side truth: the
     * tracker says a scope exists, the scope API answers what the portal should hold for it.
     *
     * <p>
     * A bind may arrive before {@link #activate}, so the scope is recorded either way and only
     * published once the component is active.
     * </p>
     */
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, //
            policy = ReferencePolicy.DYNAMIC, //
            policyOption = ReferencePolicyOption.GREEDY, //
            target = "(" + AtlasProperties.ATLAS_SCOPE + "=*)")
    void bindScopeService(ReadableScopeService<?> scopeService, Map<String, Object> properties) {
        String scopeName = (String) properties.get(AtlasProperties.ATLAS_SCOPE);
        if (scopeName == null || scopeName.isBlank()) {
            // The target filter guarantees the property is present; an empty value is a
            // misconfiguration worth naming rather than a scope worth publishing.
            LOGGER.warning("Ignoring a scope service whose " + AtlasProperties.ATLAS_SCOPE + " is empty");
            return;
        }
        ScopeInfo info;
        try {
            info = scopeService.getScopeInfo();
        } catch (RuntimeException e) {
            LOGGER.log(Level.WARNING, "Could not read the scope info for " + scopeName + "; not publishing it", e);
            return;
        }
        scopes.put(scopeName, info);
        if (active) {
            publishCatalog(scopeName);
            relinkDescendantCatalogs(scopeName);
        }
    }

    void unbindScopeService(ReadableScopeService<?> scopeService, Map<String, Object> properties) {
        String scopeName = (String) properties.get(AtlasProperties.ATLAS_SCOPE);
        if (scopeName == null) {
            return;
        }
        scopes.remove(scopeName);
        if (!retirementAllowed()) {
            return;
        }
        // The Catalog resource itself is not retired, and that is a decision rather than an
        // omission. Under UNLINK — the default — there is nothing to do: a scope going away takes
        // its EPackage services with it, so each Dataset retires itself through the unbind below
        // and the Catalog ends up listing nothing, which is exactly what UNLINK means. Deleting
        // the Catalog is the only part that differs, and it is the part that needs to know whether
        // the Catalog is ours: for an adopted Catalog, DELETE and CASCADE must be capped at UNLINK
        // (§7a). That ownership answer is D1a, and guessing it would risk deleting a Catalog we
        // were only ever a contributor to.
        LOGGER.info(() -> "Scope " + scopeName + " went away; its Datasets retire themselves and the Catalog "
                + "resource is left in place (deleting it needs the ownership resolution of D1a)");
    }

    /**
     * The Catalog configurations. Dynamic, because adopting a Catalog — or correcting the id of an
     * adopted one — has to take effect without restarting the publisher.
     */
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, //
            policy = ReferencePolicy.DYNAMIC, //
            policyOption = ReferencePolicyOption.GREEDY)
    void bindScopeCatalog(DcatScopeCatalog scopeCatalog) {
        String scopeName = scopeCatalog.scope();
        if (scopeName == null || scopeName.isBlank()) {
            return;
        }
        scopeCatalogs.put(scopeName, scopeCatalog.settings());
        // The previous refusal, if any, was about the previous configuration.
        refusedScopes.remove(scopeName);
        if (active) {
            publishCatalog(scopeName);
        }
    }

    void unbindScopeCatalog(DcatScopeCatalog scopeCatalog) {
        String scopeName = scopeCatalog.scope();
        if (scopeName == null || scopeName.isBlank()) {
            return;
        }
        scopeCatalogs.remove(scopeName);
        refusedScopes.remove(scopeName);
        // Losing the configuration makes the scope derived again, which means a Catalog under the
        // scope's own name. Deliberately not published here: a Catalog id is permanent, and
        // switching ids because a configuration went away is an operator's decision, not a
        // consequence of one. The next activation publishes it.
        LOGGER.info(() -> "Catalog configuration for scope " + scopeName + " went away; the scope is derived again "
                + "from the next activation, and the Catalog it was using is left as it is");
    }

    /**
     * The publication trigger. The {@code (dcat=true)} term is what does the selecting, so a bind
     * <em>is</em> a publish decision: the flag rides on the service properties, and DS re-evaluates
     * a target filter when those change, so clearing the flag unbinds instead of needing a second
     * notification channel.
     *
     * <p>
     * Everything needed is read straight off the service properties — no storage lookup, no
     * metadata lookup, nothing touching the portal on the DS thread.
     * </p>
     */
    @Reference(cardinality = ReferenceCardinality.MULTIPLE, //
            policy = ReferencePolicy.DYNAMIC, //
            policyOption = ReferencePolicyOption.GREEDY, //
            target = "(&(dynamic.registration=true)(dcat=true)(emf.model.scope=*)(atlas.stage=*)(emf.nsURI=*))")
    void bindPublishablePackage(EPackage ePackage, Map<String, Object> properties) {
        PublicationTarget target = toTarget(properties);
        if (target == null) {
            return;
        }
        String datasetId = DcatIds.datasetId(target.scope(), target.stage(), target.nsUri());
        trackedPackages.put(datasetId, new TrackedPackage(target, ePackage));
        RetirementQueue queue = retirements;
        if (queue != null && queue.cancel(datasetId)) {
            // The unbind that preceded this bind was an update, not a removal. Saying so once is
            // worth it: it is the only place the two are told apart.
            LOGGER.fine(() -> "Re-registered " + target.nsUri() + " inside the unpublish window; "
                    + "its retirement is cancelled and this is an update");
        }
        if (active) {
            publishPackage(datasetId);
        }
    }

    /**
     * The retirement trigger, and it stands for four different events: the {@code dcat} flag
     * cleared, the package deleted, a promotion out of a permitted stage, and a content update.
     * Only the last one must not retire anything, and the only thing that distinguishes it is that
     * a re-register follows — hence the delay.
     */
    void unbindPublishablePackage(EPackage ePackage, Map<String, Object> properties) {
        PublicationTarget target = toTarget(properties);
        if (target == null) {
            return;
        }
        String datasetId = DcatIds.datasetId(target.scope(), target.stage(), target.nsUri());
        if (!retirementAllowed()) {
            // Deliberately keeps the entry: on the shutdown path this map is what
            // retire.on.shutdown works from, and on a reactivation the rebind refreshes it anyway.
            LOGGER.fine(() -> "Not retiring " + datasetId + ": this publisher is stopping, not the model");
            return;
        }
        UnpublishMode mode = cappedByOwnership(unpublishMode, target.scope());
        if (mode == UnpublishMode.NONE) {
            // Keep tracking it, deliberately. NONE means the portal goes on advertising the
            // Dataset, and a Catalog PUT replaces — so dropping it from this map would let the
            // next Catalog write quietly remove the membership that NONE promises to keep. The
            // cost is holding the EPackage of a service that is gone, which is what an operator
            // asking for NONE is asking for.
            LOGGER.info(() -> "Package " + target.nsUri() + " (" + target.scope() + "/" + target.stage()
                    + ") stopped being publishable; unpublish.mode is NONE, so the portal keeps it");
            return;
        }
        TrackedPackage tracked = trackedPackages.remove(datasetId);
        if (tracked == null) {
            return;
        }
        RetirementQueue queue = retirements;
        if (queue == null) {
            return;
        }
        // Captured now, while the scope tree is still intact. A retirement runs a window later, and
        // the ordinary reason for one — a scope's configuration deleted — takes the scope services
        // with it, so by then the fan-out would compute nothing and the memberships would dangle in
        // Catalogs the portal still serves.
        Set<String> catalogIds = catalogsListing(target.scope());
        queue.schedule(datasetId, unpublishDelayMillis, () -> retireDataset(datasetId, target, mode, catalogIds));
    }

    /**
     * Caps a mode at {@code UNLINK} where any Catalog listing the Dataset is adopted.
     *
     * <p>
     * Our Datasets are ours to delete, but a {@code DELETE} leaves an adopted Catalog holding a
     * dangling reference and a {@code CASCADE} rewrites it outright — and what somebody else's
     * catalogue looks like afterwards is not ours to decide. The downgrade is announced once at
     * activation rather than per event.
     * </p>
     */
    private UnpublishMode cappedByOwnership(UnpublishMode mode, String definingScope) {
        if (mode != UnpublishMode.DELETE && mode != UnpublishMode.CASCADE) {
            return mode;
        }
        return anyListingCatalogAdopted(definingScope) ? UnpublishMode.UNLINK : mode;
    }

    /**
     * Whether an unbind is a statement about the model or about us.
     *
     * <p>
     * Two cases where it is about us, gated in one place rather than scattered through the
     * handlers: the framework is stopping, and this component is not active — a reactivation for a
     * configuration change, or the bundle being refreshed for an update. Neither says a model is
     * gone, and treating them as if they did would empty a catalogue on every redeploy.
     * </p>
     */
    private boolean retirementAllowed() {
        if (!active) {
            return false;
        }
        BundleContext context = bundleContext;
        if (context == null) {
            return false;
        }
        try {
            // The framework bundle, so this is one question about the whole runtime rather than a
            // guess from our own state. Note it cannot distinguish stopping from stopping-for-
            // update; both are equally not a statement about the models, so it does not matter.
            return context.getBundle(0).getState() != Bundle.STOPPING;
        } catch (IllegalStateException contextGone) {
            return false;
        }
    }

    private static PublicationTarget toTarget(Map<String, Object> properties) {
        String nsUri = string(properties, "emf.nsURI");
        String scope = string(properties, "emf.model.scope");
        String stage = string(properties, "atlas.stage");
        if (nsUri == null || scope == null || stage == null) {
            return null;
        }
        return new PublicationTarget(scope, stage, nsUri, string(properties, "emf.version"),
                string(properties, "emf.fingerprint"));
    }

    private static String string(Map<String, Object> properties, String key) {
        Object value = properties.get(key);
        return value == null ? null : value.toString();
    }

    @Activate
    void activate(DcatPublisherConfig config, BundleContext bundleContext) {
        this.config = config;
        this.bundleContext = bundleContext;
        String target = config.dcat_portal_target();
        if (target == null || target.isBlank()) {
            throw new IllegalArgumentException("dcat.portal.target is required: without it the reference binds "
                    + "whichever portal client happens to be there, which is not a thing to guess about");
        }
        // Throws with a message naming the defect; refusing to activate beats publishing a
        // localhost URL into a public catalogue.
        this.baseUri = PublicBaseUri.validate(config.atlas_public_base_uri(), config.allow_local_base_uri());
        this.publishedScopes = new LinkedHashSet<>(Arrays.asList(config.scopes()));
        this.permittedStages = new LinkedHashSet<>(Arrays.asList(config.publish_stages()));
        // FINAL is the default and is resolved per registry from StageInfo, which the scope API
        // already carries; ALL skips the gate entirely.
        this.allStages = permittedStages.stream().anyMatch(s -> s.equalsIgnoreCase("ALL"));
        this.unpublishMode = config.unpublish_mode() == null ? UnpublishMode.UNLINK : config.unpublish_mode();
        this.unpublishDelayMillis = Math.max(0L, config.unpublish_delay_seconds()) * 1000L;
        this.retireOnShutdown = config.retire_on_shutdown();
        this.retirements = new RetirementQueue("dcat-retirement");
        DcatMetadataSource source = metadataSource;
        this.mapper = new DcatMapper(config, source != null ? source : new ConfiguredMetadataSource(config));
        this.mediaTypes = PublishableMediaTypes.resolve(config.distribution_media_types(),
                supportedMediatypes == null ? List.of() : supportedMediatypes.getSupportedMediaTypes());
        this.active = true;

        if (mediaTypes.isEmpty()) {
            LOGGER.warning("No publishable media types: distribution.media.types does not overlap what this "
                    + "runtime reports it can serve, so Datasets would be advertised with no way to fetch them. "
                    + "Datasets are still published; Distributions are not");
        }

        // A new configuration re-decides every refusal: the reasons are all configuration.
        refusedScopes.clear();

        if (unpublishMode == UnpublishMode.DELETE || unpublishMode == UnpublishMode.CASCADE) {
            scopeCatalogs.entrySet().stream().filter(entry -> entry.getValue().adopt()).map(Map.Entry::getKey)
                    .forEach(scope -> LOGGER.info(() -> "unpublish.mode " + unpublishMode + " is capped at UNLINK for "
                            + "scope " + scope + ", whose Catalog is adopted: what somebody else's catalogue looks "
                            + "like after our Dataset goes is not ours to decide"));
        }

        if (unpublishDelayMillis == 0 && unpublishMode != UnpublishMode.NONE) {
            LOGGER.warning("unpublish.delay.seconds is 0, so a content update will briefly unpublish its own "
                    + "Dataset: a changed package is republished by unregister-then-register, and with no window "
                    + "the unregister is indistinguishable from a removal");
        }

        if (publishedScopes.isEmpty()) {
            LOGGER.warning("DcatPublisher for " + target + " activated with an empty `scopes` list, so it will "
                    + "publish nothing. Scopes are opt-in by design: a package's metadata cannot express which "
                    + "deployment publishes to which portal");
        } else {
            LOGGER.info(() -> "DcatPublisher active for " + target + ", base " + baseUri + ", scopes "
                    + publishedScopes);
        }
        scopes.keySet().forEach(this::publishCatalog);
        trackedPackages.keySet().forEach(this::publishPackage);
    }

    @Deactivate
    void deactivate() {
        active = false;
        RetirementQueue queue = retirements;
        retirements = null;
        if (queue != null) {
            // Abandon what is pending rather than draining it: those retirements were scheduled
            // while we were active, and we are no longer in a position to tell an update from a
            // removal. The unbinds that follow this call are gated by retirementAllowed().
            int abandoned = queue.pendingCount();
            queue.close();
            if (abandoned > 0) {
                LOGGER.info(() -> "Abandoned " + abandoned + " pending retirement(s) on deactivate");
            }
        }
        if (retireOnShutdown) {
            retireEverything();
        } else {
            // A DCAT entry says "this model exists, here is who governs it and where it is served
            // from", and that stays true across a restart. Retiring here would also make every
            // redeploy a full retire plus re-publish, and it cannot deliver the guarantee it
            // appears to: a SIGKILL, an OOM kill or a dead host runs no @Deactivate at all.
            LOGGER.info("DcatPublisher deactivated; the portal keeps what it holds");
        }
        trackedPackages.clear();
        retiredDatasets.clear();
        publishedFingerprints.clear();
    }

    /**
     * The opt-in shutdown path: retire everything this publisher is tracking, forced to
     * {@link UnpublishMode#UNLINK}.
     *
     * <p>
     * Forced, because a restart must never delete: the same runtime is expected back, and a
     * {@code DELETE} here would destroy anything the portal added on its side, with nothing left
     * running to put it back if the restart fails. And blocking, briefly, because the client is
     * asynchronous — a promise handed off during deactivation may never run once the client
     * component follows us down.
     * </p>
     */
    private void retireEverything() {
        if (unpublishMode == UnpublishMode.NONE) {
            LOGGER.info("retire.on.shutdown is set but unpublish.mode is NONE; retiring nothing");
            return;
        }
        Map<String, TrackedPackage> snapshot = Map.copyOf(trackedPackages);
        if (snapshot.isEmpty()) {
            return;
        }
        LOGGER.info(() -> "retire.on.shutdown: unlinking " + snapshot.size() + " Dataset(s)");
        CountDownLatch done = new CountDownLatch(snapshot.size());
        snapshot.forEach((datasetId, tracked) -> {
            Set<String> catalogIds = catalogsListing(tracked.target().scope());
            client.submit(portal -> {
                retire(portal, datasetId, tracked.target(), UnpublishMode.UNLINK, catalogIds);
                return null;
            }).onResolve(done::countDown);
        });
        try {
            if (!done.await(SHUTDOWN_RETIRE_TIMEOUT_MS, TimeUnit.MILLISECONDS)) {
                LOGGER.warning("retire.on.shutdown did not finish within " + SHUTDOWN_RETIRE_TIMEOUT_MS
                        + "ms; the portal may still list some Datasets. They are re-published on the next start");
            }
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Retires one Dataset, asynchronously, in {@code mode}.
     *
     * <p>
     * The fingerprint is dropped and the id remembered, so a re-publish of byte-identical content
     * writes rather than taking the unchanged shortcut. Without that, an unlinked Dataset would
     * come back as a resource nothing links to.
     * </p>
     */
    private void retireDataset(String datasetId, PublicationTarget target, UnpublishMode mode,
            Set<String> catalogsAtSchedule) {
        publishedFingerprints.remove(datasetId);
        retiredDatasets.add(datasetId);
        client.submit(portal -> {
            retire(portal, datasetId, target, mode, catalogsAtSchedule);
            return null;
        }).onFailure(t -> LOGGER.log(Level.WARNING, "Retiring Dataset " + datasetId + " failed", t));
    }

    private void retire(DcatAtlasClient portal, String datasetId, PublicationTarget target, UnpublishMode mode,
            Set<String> catalogsAtSchedule) {
        // The union of what listed it when the retirement was scheduled and what lists it now: the
        // first covers a scope that has since gone away, the second a descendant that appeared
        // inside the window and had the Dataset linked in by its own fan-in.
        Set<String> catalogIds = new LinkedHashSet<>(catalogsAtSchedule);
        catalogIds.addAll(catalogsListing(target.scope()));
        if (mode == UnpublishMode.CASCADE) {
            try {
                // The portal unlinks every referrer in one commit and reports what it rewrote —
                // including links this publisher never created, which is why it is not the default.
                List<String> rewritten = portal.delete(DcatCollection.DATASETS, datasetId, DeleteMode.CASCADE);
                LOGGER.info(() -> "Retired Dataset " + datasetId + " (CASCADE); the portal rewrote " + rewritten);
            } catch (NotFoundException alreadyGone) {
                LOGGER.fine(() -> "Dataset " + datasetId + " was already gone from the portal");
            }
            return;
        }
        // Our memberships are ours to drop, and dropping them is both the whole of UNLINK and the
        // precondition for a SINGLE delete. The unlink mirrors the publish fan-out exactly: a missed
        // descendant leaves a Catalog advertising a Dataset that is gone.
        catalogIds.forEach(catalogId -> unlink(portal, catalogId, datasetId));
        if (mode != UnpublishMode.DELETE) {
            LOGGER.info(() -> "Retired Dataset " + datasetId + " (UNLINK) from " + catalogIds.size()
                    + " catalog(s); the resource stays in the portal");
            return;
        }
        try {
            portal.delete(DcatCollection.DATASETS, datasetId, DeleteMode.SINGLE);
            LOGGER.info(() -> "Retired Dataset " + datasetId + " (DELETE)");
        } catch (NotFoundException alreadyGone) {
            LOGGER.fine(() -> "Dataset " + datasetId + " was already gone from the portal");
        } catch (ConflictException referrers) {
            // Exactly what SINGLE is for: something we did not link still points at it. Leaving it
            // is the safe answer, and the operator can widen the mode to CASCADE if they mean it.
            LOGGER.warning(() -> "Dataset " + datasetId + " is still referenced, so it was unlinked from "
                    + catalogIds + " but not deleted: " + referrers.getMessage());
        }
    }

    /**
     * Drops one membership. A Catalog or Dataset the portal no longer has is not a failure — the id
     * set deliberately includes ones that may already be gone — and one such id must not abandon the
     * rest of the fan-out, nor the delete that follows it.
     */
    private void unlink(DcatAtlasClient portal, String catalogId, String datasetId) {
        try {
            portal.unlinkDatasetFromCatalog(catalogId, datasetId);
        } catch (NotFoundException gone) {
            LOGGER.fine(() -> "Nothing to unlink: " + datasetId + " in " + catalogId);
        }
    }

    /**
     * Publishes one scope's Catalog, if this portal's configuration opts the scope in.
     *
     * <p>
     * Idempotent: the id is derived from the scope name, so a re-publish replaces rather than
     * duplicates.
     * </p>
     */
    private void publishCatalog(String scopeName) {
        if (!publishedScopes.contains(scopeName)) {
            LOGGER.fine(() -> "Scope " + scopeName + " is not in this portal's `scopes`; not publishing it");
            return;
        }
        ScopeInfo info = scopes.get(scopeName);
        if (info == null) {
            return;
        }
        ResolvedCatalog resolved = resolveCatalog(scopeName);
        if (resolved.refused()) {
            refuse(scopeName, resolved.refusalReason());
            return;
        }
        String catalogId = resolved.id();
        client.submit(portal -> {
            if (!portal.ready()) {
                LOGGER.warning(() -> "Portal is not ready; skipping the Catalog for scope " + scopeName
                        + ". It will be published on the next reconcile");
                return null;
            }
            if (resolved.adopted()) {
                adoptCatalog(portal, catalogId, info, resolved);
                return null;
            }
            return writeCatalog(portal, catalogId, info, resolved.settings());
        }).onFailure(t -> LOGGER.log(Level.WARNING, "Publishing the Catalog for scope " + scopeName + " failed", t));
    }

    /**
     * Takes up an existing Catalog: verify it is there, then link our Datasets into it. Never a
     * {@code PUT}.
     *
     * <p>
     * A missing adopted Catalog is a configuration error, not a licence to create one. The operator
     * asserted the id exists; minting a Catalog under an id in somebody else's portal is the one
     * failure mode with no clean recovery, so the scope is refused and reported instead.
     * {@code catalog.create.if.missing} exists for operators who disagree.
     * </p>
     */
    private void adoptCatalog(DcatAtlasClient portal, String catalogId, ScopeInfo info, ResolvedCatalog resolved) {
        String scopeName = info.getName();
        if (portal.catalog(catalogId).isEmpty()) {
            if (!resolved.settings().createIfMissing()) {
                refuse(scopeName, "the adopted Catalog " + catalogId + " is not in the portal. The id was asserted by "
                        + "configuration, so it is not created here; fix catalog.id or set catalog.create.if.missing");
                return;
            }
            LOGGER.warning(() -> "Adopted Catalog " + catalogId + " is missing and catalog.create.if.missing is set, "
                    + "so it is being created from scope " + scopeName);
            writeCatalog(portal, catalogId, info, resolved.settings());
            return;
        }
        refusedScopes.remove(scopeName);
        // Additive only, and no sub-catalog claims: see relinkSubCatalogs.
        relinkDatasets(portal, catalogId, scopeName);
        LOGGER.info(() -> "Adopted Catalog " + catalogId + " for scope " + scopeName
                + "; its Datasets are linked in and it is never written from here");
    }

    /**
     * Publishes one package's Dataset, its Distributions and its Catalog membership.
     *
     * <p>
     * The order is forced by the portal's semantics: a {@code PUT} replaces, so registering the
     * Dataset drops both its contained Distributions and its Catalog membership, and both have to
     * be re-asserted afterwards. Every step is idempotent, which is what makes this the normal
     * path rather than a repair.
     * </p>
     */
    private void publishPackage(String datasetId) {
        TrackedPackage tracked = trackedPackages.get(datasetId);
        if (tracked == null) {
            return;
        }
        PublicationTarget target = tracked.target();

        if (!publishedScopes.contains(target.scope())) {
            LOGGER.fine(() -> "Scope " + target.scope() + " is not in this portal's `scopes`; not publishing "
                    + target.nsUri());
            return;
        }
        if (refusedScopes.containsKey(target.scope()) || resolveCatalog(target.scope()).refused()) {
            // The scope's Catalog is the precondition for its Datasets: publishing one anyway would
            // advertise a model through no catalogue at all.
            LOGGER.fine(() -> "Scope " + target.scope() + " is refused, so " + target.nsUri() + " is not published: "
                    + refusedScopes.get(target.scope()));
            return;
        }
        if (!stagePermitted(target)) {
            LOGGER.fine(() -> "Stage " + target.stage() + " is not permitted by publish.stages; " + target.nsUri()
                    + " records the intent without being published");
            return;
        }

        client.submit(portal -> {
            if (!portal.ready()) {
                LOGGER.warning(() -> "Portal is not ready; skipping Dataset " + datasetId);
                return null;
            }
            // Re-checked here, not only above: whether an adopted Catalog exists is answered on
            // this thread, and the client's executor is single-threaded, so a refusal recorded by
            // the Catalog task queued before this one is visible now and was not when this task
            // was submitted. Without this, a refused scope still publishes its first Dataset —
            // into no Catalog at all, since the fan-out has nothing to link to.
            if (refusedScopes.containsKey(target.scope())) {
                LOGGER.fine(() -> "Scope " + target.scope() + " was refused; not publishing " + datasetId);
                return null;
            }
            // Change detection. On a restart the in-memory map is empty, so ask the portal once
            // rather than rewriting: a git-backed portal pays two commits per entity otherwise.
            if (retiredDatasets.remove(datasetId)) {
                // Retired and back: the content may be byte-identical, but the membership this
                // publisher dropped is not, so the shortcut below would leave the Dataset
                // unreachable through its Catalog.
                LOGGER.fine(() -> "Dataset " + datasetId + " was retired; re-publishing it unconditionally");
                return writeDataset(portal, datasetId, tracked);
            }
            String published = publishedFingerprints.get(datasetId);
            if (published == null) {
                published = portal.dataset(datasetId).map(DcatPublisher::fingerprintOf).orElse(null);
                if (published != null) {
                    publishedFingerprints.put(datasetId, published);
                }
            }
            if (target.fingerprint() != null && target.fingerprint().equals(published)) {
                LOGGER.fine(() -> "Dataset " + datasetId + " is already published at this fingerprint; no write");
                return null;
            }
            return writeDataset(portal, datasetId, tracked);
        }).onFailure(t -> LOGGER.log(Level.WARNING, "Publishing Dataset " + datasetId + " failed", t));
    }

    private Dataset writeDataset(DcatAtlasClient portal, String datasetId, TrackedPackage tracked) {
        PublicationTarget target = tracked.target();
        try {
            Registration<Dataset> registration = portal.registerDataset(datasetId,
                    mapper.toDataset(target, tracked.ePackage()));
            if (!registration.applied()) {
                LOGGER.warning(() -> "Dataset " + datasetId + " was not written: a precondition refused it");
                return null;
            }
            // Re-assert the containment the PUT just dropped.
            for (String mediaType : mediaTypes) {
                Distribution distribution = mapper.toDistribution(target, mediaType, baseUri);
                portal.registerDistribution(datasetId, DcatIds.distributionId(mediaType), distribution);
            }
            // Re-assert the membership the PUT just dropped — in every Catalog that serves this
            // model, which is its own scope's and every descendant's. Additive, so it is safe even
            // on a Catalog we do not own.
            Set<String> catalogIds = catalogsListing(target.scope());
            catalogIds.forEach(catalogId -> portal.linkDatasetToCatalog(catalogId, datasetId));

            if (target.fingerprint() != null) {
                publishedFingerprints.put(datasetId, target.fingerprint());
            }
            LOGGER.info(() -> "Published Dataset " + datasetId + " with " + mediaTypes.size() + " distribution(s) in "
                    + catalogIds.size() + " catalog(s)");
            return registration.entity();
        } catch (DcatModelConstraintException | DcatShaclException e) {
            LOGGER.log(Level.WARNING, "Portal refused Dataset " + datasetId + " as invalid; not retrying", e);
            return null;
        } catch (IllegalStateException misconfigured) {
            // A missing license.uri, for instance: permanent until an operator acts, so saying it
            // once beats retrying forever.
            LOGGER.log(Level.WARNING, "Cannot publish Dataset " + datasetId + ": " + misconfigured.getMessage());
            return null;
        }
    }

    /**
     * A snapshot of the scope tree, from the scope infos currently bound.
     *
     * <p>
     * Rebuilt per operation rather than maintained: it is a walk over a handful of entries, and a
     * fan-out computed against a tree shifting underneath it would link into some descendants and
     * not others, with nothing left to notice.
     * </p>
     */
    private ScopeHierarchy hierarchy() {
        Map<String, String> parents = new LinkedHashMap<>();
        scopes.forEach((name, info) -> parents.put(name, info.getParentScope()));
        return ScopeHierarchy.of(parents);
    }

    /**
     * Every Catalog that must list a Dataset defined by {@code definingScope}: its own scope's and
     * every descendant's, because a descendant serves the model too — O4's inheritance is one
     * Dataset linked into several Catalogs, never a Dataset per inheriting scope.
     *
     * <p>
     * Filtered by this portal's {@code scopes} and by what is actually bound, since a Catalog only
     * exists for a scope this publisher publishes.
     * </p>
     */
    private Set<String> catalogsListing(String definingScope) {
        Set<String> catalogIds = new LinkedHashSet<>();
        if (publishable(definingScope)) {
            catalogIds.add(catalogIdOf(definingScope));
        }
        hierarchy().descendants(definingScope).stream().filter(this::publishable).map(this::catalogIdOf)
                .forEach(catalogIds::add);
        return catalogIds;
    }

    /**
     * Whether any Catalog that lists a Dataset of {@code definingScope} is adopted, which is what
     * caps {@code unpublish.mode}: a {@code CASCADE} delete of the Dataset unlinks its referrers,
     * and rewriting somebody else's Catalog is not ours to do.
     */
    private boolean anyListingCatalogAdopted(String definingScope) {
        if (resolveCatalog(definingScope).adopted()) {
            return true;
        }
        return hierarchy().descendants(definingScope).stream().filter(this::publishable)
                .anyMatch(scope -> resolveCatalog(scope).adopted());
    }

    /** Whether this portal holds a Catalog for {@code scope} at all. */
    private boolean publishable(String scope) {
        return publishedScopes.contains(scope) && scopes.containsKey(scope) && !refusedScopes.containsKey(scope)
                && !resolveCatalog(scope).refused();
    }

    /**
     * What one scope's Catalog is: which id, and whether we may write it. Configuration is the only
     * input — see {@link CatalogResolver}.
     */
    private ResolvedCatalog resolveCatalog(String scope) {
        return CatalogResolver.resolve(scope, scopeCatalogs.get(scope));
    }

    /** The id of a scope's Catalog, or {@code null} when the scope is refused. */
    private String catalogIdOf(String scope) {
        return resolveCatalog(scope).id();
    }

    /**
     * Records a refusal and says so once. A refused scope publishes nothing: its Catalog is the
     * precondition for its Datasets, so advertising them would produce {@code accessURL}s in no
     * catalogue at all.
     */
    private void refuse(String scope, String reason) {
        if (refusedScopes.put(scope, reason) == null) {
            LOGGER.warning(() -> "Refusing scope " + scope + ": " + reason);
        }
    }

    /**
     * Re-asserts this Catalog's place in the scope tree: its child Catalogs below it, and itself
     * below its parent.
     *
     * <p>
     * Both directions, because a {@code dcat:catalog} link lives on the <em>parent</em> resource.
     * Writing this Catalog drops the links to its children, so those are re-asserted here; its own
     * membership in its parent survives this write but has to be asserted somewhere, and a child
     * appearing after its parent is the ordinary case.
     * </p>
     *
     * <p>
     * Asserting a link <em>on</em> another Catalog is a claim about that Catalog's structure, which
     * is only ours to make while every Catalog is derived. When D1a brings adopted Catalogs, this
     * has to skip the ones we do not own.
     * </p>
     */
    private void relinkSubCatalogs(DcatAtlasClient portal, String catalogId, String scope) {
        if (resolveCatalog(scope).adopted()) {
            // Neither direction for an adopted Catalog. Asserting a sub-catalog link on it is a
            // claim about somebody else's catalogue structure, and hanging it under ours claims
            // their catalogue is part of our tree. Datasets still link in, which is additive.
            return;
        }
        ScopeHierarchy tree = hierarchy();
        tree.children(scope).stream().filter(this::publishable).filter(child -> !resolveCatalog(child).adopted())
                .forEach(child -> linkSubCatalog(portal, catalogId, catalogIdOf(child)));
        String parent = tree.parentOf(scope);
        if (parent != null && publishable(parent) && !resolveCatalog(parent).adopted()) {
            linkSubCatalog(portal, catalogIdOf(parent), catalogId);
        }
    }

    private void linkSubCatalog(DcatAtlasClient portal, String parentCatalogId, String childCatalogId) {
        try {
            portal.linkSubCatalog(parentCatalogId, childCatalogId);
        } catch (RuntimeException e) {
            // A Catalog that has not been written yet cannot be linked; whichever of the two is
            // written second asserts it.
            LOGGER.fine(() -> "Could not link sub-catalog " + childCatalogId + " into " + parentCatalogId + ": "
                    + e.getMessage());
        }
    }

    /**
     * Re-asserts the memberships of every Catalog below {@code scopeName}, without rewriting them.
     *
     * <p>
     * Needed because a bind can <em>complete a chain</em>: a leaf bound before its middle scope
     * could see only as far as the parent name it was told, so it never linked its grandparent's
     * Datasets. Once the middle scope is here the chain resolves — and nothing else would revisit
     * the leaf, since no package changed.
     * </p>
     */
    private void relinkDescendantCatalogs(String scopeName) {
        Set<String> descendants = new LinkedHashSet<>(hierarchy().descendants(scopeName));
        descendants.removeIf(scope -> !publishable(scope));
        if (descendants.isEmpty()) {
            return;
        }
        client.submit(portal -> {
            if (!portal.ready()) {
                return null;
            }
            descendants.forEach(scope -> relinkDatasets(portal, catalogIdOf(scope), scope));
            return null;
        }).onFailure(t -> LOGGER.log(Level.WARNING, "Re-linking the Catalogs below " + scopeName + " failed", t));
    }

    /**
     * Re-asserts every Dataset membership this Catalog owes: the scope's own packages and every
     * ancestor's, the latter being what inheritance means here.
     *
     * <p>
     * Idempotent and additive, so it is safe after any Catalog write — and it is also the whole of
     * the fan-in for a scope that appears later, since nothing happens to an ancestor's package when
     * a new scope binds and the new Catalog therefore has to pull those Datasets in itself.
     * </p>
     */
    private void relinkDatasets(DcatAtlasClient portal, String catalogId, String scope) {
        Set<String> inheritedFrom = new LinkedHashSet<>(hierarchy().ancestors(scope));
        trackedPackages.forEach((datasetId, tracked) -> {
            String defining = tracked.target().scope();
            if (!scope.equals(defining) && !inheritedFrom.contains(defining)) {
                return;
            }
            if (!stagePermitted(tracked.target())) {
                return;
            }
            try {
                portal.linkDatasetToCatalog(catalogId, datasetId);
            } catch (RuntimeException e) {
                // A Dataset that is tracked but not yet written has nothing to link; the write
                // that follows will link it. Anything else is worth a line, not an unwind.
                LOGGER.fine(() -> "Could not re-link Dataset " + datasetId + " into " + catalogId + ": "
                        + e.getMessage());
            }
        });
    }

    /**
     * Reads a published Dataset's fingerprint back out of the SPDX checksum on any of its
     * Distributions, so a restart can tell "already published" from "changed".
     */
    private static String fingerprintOf(Dataset dataset) {
        return dataset.getDistribution().stream().map(Distribution::getChecksum).filter(c -> c != null)
                .map(c -> c.getChecksumValue()).filter(v -> v != null && v.length == 32).findFirst()
                .map(DcatPublisher::toFingerprint).orElse(null);
    }

    private static String toFingerprint(byte[] sha256) {
        StringBuilder hex = new StringBuilder("fp1:");
        for (byte b : sha256) {
            hex.append(Character.forDigit((b >> 4) & 0xF, 16)).append(Character.forDigit(b & 0xF, 16));
        }
        return hex.toString();
    }

    /**
     * Whether this target's stage may publish. {@code FINAL} is resolved against the scope's own
     * {@link StageInfo}, never a hardcoded stage name — a scope may call its final stage anything.
     */
    private boolean stagePermitted(PublicationTarget target) {
        if (allStages) {
            return true;
        }
        boolean finalOnly = permittedStages.stream().anyMatch(s -> s.equalsIgnoreCase("FINAL"));
        if (!finalOnly) {
            return permittedStages.contains(target.stage());
        }
        ScopeInfo info = scopes.get(target.scope());
        if (info == null) {
            return false;
        }
        return info.getRegistries().stream().flatMap(r -> r.getStages().stream())
                .anyMatch(stage -> target.stage().equals(stage.getName()) && stage.isFinal())
                || permittedStages.contains(target.stage());
    }

    private Catalog writeCatalog(DcatAtlasClient portal, String catalogId, ScopeInfo info,
            CatalogSettings settings) {
        try {
            Registration<Catalog> registration = portal.registerCatalog(catalogId, mapper.toCatalog(info, settings));
            if (!registration.applied()) {
                // A foreign edit landed between our read and our write. Log and carry on; never
                // unwind, because the next reconcile converges anyway.
                LOGGER.warning(() -> "Catalog " + catalogId + " was not written: a precondition refused it");
                return null;
            }
            // A PUT replaces, so that write just dropped every dcat:dataset membership the Catalog
            // held. The Datasets themselves are skipped when unchanged — correctly, to avoid
            // rewriting the catalogue on every boot — which means nothing else will re-assert
            // these links. Whoever rewrites a Catalog owns its memberships.
            relinkDatasets(portal, catalogId, info.getName());
            relinkSubCatalogs(portal, catalogId, info.getName());
            LOGGER.info(() -> "Published Catalog " + catalogId + " for scope " + info.getName());
            return registration.entity();
        } catch (DcatModelConstraintException | DcatShaclException e) {
            // Permanent for this entity: the portal will refuse the identical payload every time,
            // so retrying is only noise. The report is the actionable part.
            LOGGER.log(Level.WARNING, "Portal refused Catalog " + catalogId + " as invalid; not retrying", e);
            return null;
        }
    }
}
