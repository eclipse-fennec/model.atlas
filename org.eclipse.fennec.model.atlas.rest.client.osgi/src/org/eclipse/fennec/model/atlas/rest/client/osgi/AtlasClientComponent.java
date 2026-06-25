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
package org.eclipse.fennec.model.atlas.rest.client.osgi;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.fennec.emf.osgi.configurator.ResourceSetConfigurator;
import org.eclipse.fennec.model.atlas.scope.api.AtlasProperties;
import org.eclipse.fennec.model.atlas.rest.client.api.ClientConfiguration;
import org.eclipse.fennec.model.atlas.rest.client.api.ModelAtlasClient;
import org.eclipse.fennec.model.atlas.rest.client.api.ModelAtlasClientFactory;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.metatype.annotations.Designate;

import jakarta.ws.rs.client.ClientBuilder;

/**
 * P3-1 — the OSGi front-end's entry point: a ConfigAdmin-driven factory component
 * that turns a {@link AtlasClientConfig} into a {@link ClientConfiguration} and
 * builds one {@link ModelAtlasClient} (the Phase-2 plain-Java core, reused as-is).
 * <p>
 * Factory PID {@code org.eclipse.fennec.model.atlas.rest.client}: one component
 * instance — and one independent client — per configuration, so several Atlas
 * instances can be connected in parallel. {@link ConfigurationPolicy#REQUIRE} plus
 * the absence of a {@code @Modified} method means a configuration update tears the
 * instance down (closing its client) and re-activates it cleanly.
 * <p>
 * The client is obtained from the {@link ModelAtlasClientFactory} OSGi service
 * (the impl registers it via the bnd {@code @ServiceProvider} / SPI-Fly bridge),
 * so this bundle never calls {@code ServiceLoader} inside the framework and never
 * references the (un-exported) impl classes.
 * <p>
 * P3-2: the Jakarta RS client is built through a {@link WhiteboardJakartaRsClientProvider}
 * wrapping the {@link ClientBuilder} resolved from the OSGi service registry (the
 * Whiteboard), so the runtime's HTTP client and registered providers apply while
 * the P2-10 auth/timeout wiring is reused. The {@code ClientBuilder} reference is
 * mandatory: with no Whiteboard present the component does not activate (fail-fast).
 * <p>
 * P3-3: it also owns a {@link RemoteEPackagePublisher} (bound to the bundle's
 * {@link BundleContext}) that publishes fetched EPackages as OSGi services and is
 * revoked on deactivation.
 * <p>
 * P3-4: when {@code mode=EAGER} the {@link EagerPrefetch} runs at activation,
 * listing the configured scopes and publishing each EPackage immediately.
 * <p>
 * P3-5: it also builds a {@link LazyResolvingPackageRegistry} over the framework
 * {@code EPackage.Registry} (the {@code (default.resourceset.epackage.registry=true)}
 * service that {@code emf.osgi}'s {@code DefaultEPackageRegistryComponent} owns). That
 * registry fetches+publishes an unknown nsURI on demand and blocks until it is visible
 * in the framework registry. P3-10 installs it into framework {@code ResourceSet}s via a
 * {@code ResourceSetConfigurator}.
 * <p>
 * P3-6: when {@code mode=HYBRID} the {@link EagerPrefetch#prefetchListedNsUris()} pre-fetches
 * only the nsURIs in {@code eager.nsuri.allow.list} at activation; everything else is
 * left to the LAZY registry. {@code mode=LAZY} (the default) pre-fetches nothing.
 * <p>
 * P3-7: all three publish paths (EAGER, HYBRID, LAZY) go through a
 * {@link LocalFirstPublicationGate}, so a remote package is suppressed while a local
 * {@code EPackage}/{@code EPackageConfigurator} provides the same nsURI (unless
 * {@code force.remote}); a {@link LocalServiceWatcher} re-publishes it when the local
 * one disappears.
 * <p>
 * P3-8: when {@code force.remote=true} the publisher stamps a high {@code service.ranking}
 * on every remote publication, and a {@link ForceRemoteStartupCheck} runs at activation to
 * supersede any local EPackage the Atlas has a newer version of.
 * <p>
 * P3-9: a {@link DriftSubstitution} drift listener atomically swaps a published trio when its
 * Atlas content changes (per-nsURI lock; the delegating registry serves the old or new package
 * via {@link RemoteEPackagePublisher#publishedEPackage(String)}, never a half-state) and revokes
 * it on removal.
 * <p>
 * P3-10: unless {@code resource.set.fallback=false}, an {@link AtlasResourceSetConfigurator} is
 * registered so every framework-produced {@code ResourceSet} carries the delegating registry —
 * a resource load referencing an unknown nsURI then resolves it from the Atlas.
 * <p>
 * P3-11: when {@code register.in.global.registry=true} the publisher additionally mirrors every
 * published EPackage into {@code EPackage.Registry.INSTANCE} for legacy code reaching the EMF
 * singleton (default {@code false} leaves it untouched).
 */
@Component(name = AtlasClientComponent.PID, configurationPid = AtlasClientComponent.PID, configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = AtlasClientConfig.class, factory = true)
public class AtlasClientComponent {

	/** ConfigAdmin factory PID for the OSGi Atlas client. */
	public static final String PID = "org.eclipse.fennec.model.atlas.rest.client";

	private static final Logger LOGGER = Logger.getLogger(AtlasClientComponent.class.getName());

	/**
	 * Service property published by {@code emf.osgi}'s {@code DefaultEPackageRegistryComponent}
	 * on the global framework {@code EPackage.Registry} that the default {@code ResourceSetFactory}
	 * consumes; we target it so the LAZY registry waits on (and delegates to) the right one.
	 */
	private static final String DEFAULT_FRAMEWORK_REGISTRY_TARGET = "(default.resourceset.epackage.registry=true)";

	/** Debounce before republishing a remote after its local counterpart disappears (P3-7, anti-flap). */
	private static final long LOCAL_DISAPPEAR_DEBOUNCE_MS = 500L;

	/** {@code service.ranking} for forced remote publications (P3-8) — above the local default of 0. */
	private static final int FORCE_REMOTE_SERVICE_RANKING = 1000;

	private final ModelAtlasClient client;
	private final RemoteEPackagePublisher publisher;
	/** P5-4: publishes one {@code ReadableScopeService<EObject>} OSGi service per scope. */
	private final RemoteScopeServicePublisher scopePublisher;
	private final LazyResolvingPackageRegistry lazyRegistry;
	private final LocalServiceWatcher localServiceWatcher;
	private final ScheduledExecutorService debounceExecutor;
	private final AutoCloseable driftSubscription;
	/** P3-10: registered only when {@code resource.set.fallback=true}; {@code null} otherwise. */
	private final ServiceRegistration<ResourceSetConfigurator> resourceSetConfiguratorReg;
	/** P6-6: one fetch-on-miss bridge per (scope, stage) pair; registered as EPackage.Registry services. */
	private final List<ServiceRegistration<EPackage.Registry>> fetchOnMissRegistrations = new ArrayList<>();
	/** P6-6: manages the ConfigAdmin EPackageRegistry + ResourceSetFactory pairs. */
	private final AtlasEPackageRegistryConfigurator registryConfigurator;

	@Activate
	public AtlasClientComponent(@Reference ModelAtlasClientFactory clientFactory,
			@Reference ClientBuilder clientBuilder,
			@Reference(target = DEFAULT_FRAMEWORK_REGISTRY_TARGET) EPackage.Registry frameworkRegistry,
			@Reference ConfigurationAdmin configurationAdmin,
			BundleContext bundleContext, AtlasClientConfig config) {
		ClientConfiguration configuration = toConfiguration(config);
		this.client = clientFactory.builder()
				.configuration(configuration)
				.clientProvider(new WhiteboardJakartaRsClientProvider(clientBuilder))
				.build();
		// P3-8: forced remotes publish with a high service.ranking so direct lookups prefer them.
		int serviceRanking = configuration.isForceRemote() ? FORCE_REMOTE_SERVICE_RANKING : 0;
		// P3-11: opt-in mirroring of published EPackages into the EMF singleton for legacy consumers.
		EPackage.Registry globalRegistry = configuration.isRegisterInGlobalRegistry() ? EPackage.Registry.INSTANCE
				: null;
		this.publisher = new RemoteEPackagePublisher(bundleContext, configuration.getBaseUri().toString(),
				serviceRanking, globalRegistry);
		// P5-4: per-scope ReadableScopeService<EObject> publications (keyed atlas.scope).
		// P6-7: stamp atlas.stage when the client is configured with a primary stage so two
		// front-ends for the same scope can be told apart; null = stage-free (stamp omitted).
		String primaryStage = configuration.getEagerStages().isEmpty() ? null
				: configuration.getEagerStages().get(0);
		this.scopePublisher = new RemoteScopeServicePublisher(bundleContext, configuration.getBaseUri().toString(),
				primaryStage);

		// P3-7: every publish goes through the local-first gate — a remote package is only
		// published when no local EPackage/EPackageConfigurator already provides its nsURI
		// (unless force.remote). The watcher feeds local service lifecycle to the gate so a
		// suppressed remote is (re)published when its local counterpart disappears.
		this.debounceExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
			Thread thread = new Thread(runnable, "atlas-client-local-first");
			thread.setDaemon(true);
			return thread;
		});
		LocalFirstPublicationGate.Scheduler scheduler = (task, delayMs) -> {
			ScheduledFuture<?> future = debounceExecutor.schedule(task, delayMs, TimeUnit.MILLISECONDS);
			return () -> future.cancel(false);
		};
		LocalFirstPublicationGate gate = new LocalFirstPublicationGate(publisher::publish, publisher::unpublish,
				nsUri -> LocalServiceWatcher.hasLocalService(bundleContext, nsUri), configuration.isForceRemote(),
				scheduler, LOCAL_DISAPPEAR_DEBOUNCE_MS);
		this.localServiceWatcher = LocalServiceWatcher.register(bundleContext, gate);

		// P3-5: the on-demand LAZY registry over the framework registry. Used by HYBRID
		// (P3-6) and installed into ResourceSets by P3-10; harmless to hold in any mode.
		// It resolves each package's authoritative origin (scope/stage/version) per-fetch
		// via the provider, so no scope/stage is supplied here. Publishing goes through the gate.
		this.lazyRegistry = new LazyResolvingPackageRegistry(frameworkRegistry, client.ePackages(), gate,
				publisher::publishedEPackage, configuration.getLazyResolveTimeoutMs());

		// P3-9: keep published services in step with the Atlas. On a drift change we re-resolve
		// and atomically swap the trio (per-nsURI lock); on removal we revoke it. Registered
		// before the prefetch so it is live throughout; events for not-yet-published nsURIs are
		// ignored (isPublished gate).
		this.driftSubscription = client.addDriftListener(new DriftSubstitution(publisher::isPublished,
				client.ePackages()::resolve, publisher::republish, publisher::unpublish));

		// P3-10: unless resource.set.fallback=false, register a ResourceSetConfigurator so every
		// framework-produced ResourceSet carries the delegating registry (Atlas fallback on a miss).
		if (configuration.isResourceSetFallback()) {
			// atlas.* props mark it as ours so it is observable via service inspection (P3-12).
			Hashtable<String, Object> configuratorProps = new Hashtable<>();
			configuratorProps.put(AtlasProperties.ATLAS_REMOTE, Boolean.TRUE);
			configuratorProps.put(AtlasProperties.ATLAS_BASE_URI, configuration.getBaseUri().toString());
			this.resourceSetConfiguratorReg = bundleContext.registerService(ResourceSetConfigurator.class,
					new AtlasResourceSetConfigurator(lazyRegistry), configuratorProps);
		} else {
			this.resourceSetConfiguratorReg = null;
		}
		// P6-6: for each (scope, stage) pair from (eager_scopes × eager_stages), register an
		// AtlasScopedFetchOnMissRegistry OSGi service (the fetch-on-miss bridge) and generate
		// the ConfigAdmin EPackageRegistry + ResourceSetFactory pair that targets it.
		this.registryConfigurator = new AtlasEPackageRegistryConfigurator(configurationAdmin);
		registerScopeRegistries(bundleContext, configuration, frameworkRegistry, client.ePackages());
		try {
			// P3-4 EAGER: pre-fetch the configured scopes. P3-6 HYBRID: pre-fetch only
			// eager.nsuri.allow.list; the rest resolves lazily through lazyRegistry. LAZY
			// (P3-5, default): nothing up front. All publish through the local-first gate.
			EagerPrefetch prefetch = new EagerPrefetch(client, gate, configuration);
			switch (configuration.getMode()) {
				case EAGER -> prefetch.run();
				case HYBRID -> prefetch.prefetchListedNsUris();
				case LAZY -> { /* nothing up front */ }
			}
			if (configuration.isForceRemote()) {
				// P3-8: supersede any local EPackage the Atlas has a newer version of, in any mode.
				new ForceRemoteStartupCheck(() -> LocalServiceWatcher.localModels(bundleContext),
						client.ePackages()::resolve, gate).run();
			}
			// P5-4: publish one ReadableScopeService<EObject> per scope (independent of the
			// EPackage resolution mode); a consumer's (atlas.scope=…) lookup then resolves
			// against this client exactly as it does against the in-process server.
			publishScopeServices(configuration);
		} catch (RuntimeException strictFailure) {
			// mode.strict=true + unreachable server: tear down what we built before
			// letting activation fail, so nothing (client + drift scheduler, the drift
			// subscription, the service listener, the debounce executor) leaks —
			// @Deactivate is not called for a component that never activated.
			tearDown();
			throw strictFailure;
		}
		LOGGER.log(Level.INFO, () -> "Model Atlas client activated for " + configuration.getBaseUri());
	}

	@Deactivate
	void deactivate() {
		tearDown();
	}

	/** Release everything in the reverse order of build-up; safe to call from a failed activation. */
	private void tearDown() {
		unregisterQuietly(resourceSetConfiguratorReg); // stop wrapping new ResourceSets first
		// P6-6: delete ConfigAdmin pairs and unregister fetch-on-miss bridge services.
		if (registryConfigurator != null) {
			registryConfigurator.close();
		}
		fetchOnMissRegistrations.forEach(AtlasClientComponent::unregisterQuietly);
		fetchOnMissRegistrations.clear();
		closeQuietly(driftSubscription); // stop drift swaps
		localServiceWatcher.close();
		debounceExecutor.shutdownNow();
		scopePublisher.unpublishAll(); // P5-4: revoke the per-scope ReadableScopeService publications
		publisher.unpublishAll();
		if (client != null) {
			client.close();
		}
	}

	/**
	 * P5-4 — publish a {@code ReadableScopeService<EObject>} for each scope this client
	 * exposes. The scope set is {@code scope.allow.list} when configured (no server call
	 * needed — the per-scope façade fetches lazily), otherwise the scopes the server
	 * advertises via {@code GET /scopes}. In {@code mode.strict}, a failing {@code listScopeNames}
	 * propagates and tears down the activation (same contract as the EAGER prefetch above).
	 */
	private void publishScopeServices(ClientConfiguration configuration) {
		List<String> scopes = configuration.getScopeAllowList().isEmpty() ? client.listScopeNames()
				: configuration.getScopeAllowList();
		for (String scope : scopes) {
			scopePublisher.publish(scope, client.readOnlyScope(scope));
		}
	}

	/**
	 * P6-6 — for each (scope, stage) pair in {@code eager_scopes × eager_stages}: register an
	 * {@link AtlasScopedFetchOnMissRegistry} as an OSGi {@code EPackage.Registry} service (the
	 * fetch-on-miss bridge), add it as a drift listener so its cache stays fresh, and generate
	 * the ConfigAdmin {@code EPackageRegistry} + {@code ResourceSetFactory} pair via
	 * {@link AtlasEPackageRegistryConfigurator}.
	 * <p>
	 * Only runs when at least one scope is configured in {@code eager_scopes}. Failures during
	 * ConfigAdmin config creation are logged but do not abort activation (the bridge service is
	 * already up; the stock registry simply won't be wired until the config is retried or the
	 * component restarts).
	 */
	private void registerScopeRegistries(BundleContext bundleContext, ClientConfiguration configuration,
			EPackage.Registry frameworkRegistry,
			org.eclipse.fennec.model.atlas.rest.client.api.RemoteEPackageProvider provider) {
		List<String> scopes = configuration.getEagerScopes();
		List<String> stages = configuration.getEagerStages();
		if (scopes.isEmpty()) {
			return;
		}
		for (String scope : scopes) {
			if (stages.isEmpty()) {
				registerOneBridge(bundleContext, scope, null, frameworkRegistry, provider);
			} else {
				for (String stage : stages) {
					registerOneBridge(bundleContext, scope, stage, frameworkRegistry, provider);
				}
			}
		}
	}

	private void registerOneBridge(BundleContext bundleContext, String scope, String stage,
			EPackage.Registry frameworkRegistry,
			org.eclipse.fennec.model.atlas.rest.client.api.RemoteEPackageProvider provider) {
		AtlasScopedFetchOnMissRegistry bridge = new AtlasScopedFetchOnMissRegistry(scope, stage, provider,
				frameworkRegistry);
		Hashtable<String, Object> props = new Hashtable<>();
		props.put(AtlasProperties.ATLAS_REMOTE, Boolean.TRUE);
		props.put(AtlasProperties.ATLAS_SCOPE, scope);
		if (stage != null) {
			props.put(AtlasProperties.ATLAS_STAGE, stage);
		}
		props.put(AtlasScopedFetchOnMissRegistry.FETCH_ON_MISS_PROPERTY, Boolean.TRUE);
		ServiceRegistration<EPackage.Registry> reg = bundleContext.registerService(EPackage.Registry.class, bridge,
				props);
		fetchOnMissRegistrations.add(reg);
		// Register as drift listener so cache is evicted when the Atlas signals a package change.
		client.addDriftListener(bridge);
		try {
			registryConfigurator.register(scope, stage);
		} catch (IOException e) {
			LOGGER.log(Level.WARNING,
					"Failed to register ConfigAdmin scope registry for scope='" + scope + "'"
							+ (stage != null ? ", stage='" + stage + "'" : ""),
					e);
		}
	}

	private static void unregisterQuietly(ServiceRegistration<?> registration) {
		if (registration == null) {
			return;
		}
		try {
			registration.unregister();
		} catch (IllegalStateException alreadyGone) {
			LOGGER.log(Level.FINE, "ResourceSetConfigurator already unregistered", alreadyGone);
		}
	}

	private static void closeQuietly(AutoCloseable closeable) {
		if (closeable == null) {
			return;
		}
		try {
			closeable.close();
		} catch (Exception e) {
			LOGGER.log(Level.FINE, "Closing the drift subscription failed", e);
		}
	}

	/** The client built for this configuration (for the components layered on in later tickets). */
	ModelAtlasClient client() {
		return client;
	}

	/** The EPackage publisher for this client (used by the mode/drift components in later tickets). */
	RemoteEPackagePublisher publisher() {
		return publisher;
	}

	/**
	 * Map the typed ConfigAdmin view onto the shared {@link ClientConfiguration}.
	 * Empty optional-string properties (ConfigAdmin has no null) collapse back to
	 * {@code null} / unset so the builder defaults apply.
	 */
	private static ClientConfiguration toConfiguration(AtlasClientConfig c) {
		ClientConfiguration.Builder b = ClientConfiguration.builder()
				.baseUri(URI.create(c.base_uri()))
				.connectTimeoutMs(c.connect_timeout_ms())
				.readTimeoutMs(c.read_timeout_ms())
				.mode(c.mode())
				.eagerScopes(List.of(c.eager_scopes()))
				.eagerStages(List.of(c.eager_stages()))
				.eagerNsUriAllowList(List.of(c.eager_nsuri_allow_list()))
				.modeStrict(c.mode_strict())
				.nsUriAllowList(List.of(c.nsuri_allow_list()))
				.nsUriDenyList(List.of(c.nsuri_deny_list()))
				.forceRemote(c.force_remote())
				.registerInGlobalRegistry(c.register_in_global_registry())
				.driftCheckIntervalMs(c.drift_check_interval_ms())
				.scopeAllowList(List.of(c.scope_allow_list()))
				.cacheMaxEntries(c.cache_max_entries())
				.cacheTtlMs(c.cache_ttl_ms())
				.authType(c.auth_type())
				.lazyResolveTimeoutMs(c.lazy_resolve_timeout_ms())
				.resourceSetFallback(c.resource_set_fallback());

		emptyToNull(c.default_scope(), b::defaultScope);
		emptyToNull(c.cache_disk_dir(), b::cacheDiskDir);
		emptyToNull(c.auth_token_env(), b::authTokenEnv);
		emptyToNull(c.auth_keystore_path(), b::keystorePath);
		emptyToNull(c.auth_keystore_password(), b::keystorePassword);
		emptyToNull(c.auth_keystore_type(), b::keystoreType);
		emptyToNull(c.auth_truststore_path(), b::truststorePath);
		emptyToNull(c.auth_truststore_password(), b::truststorePassword);
		emptyToNull(c.auth_truststore_type(), b::truststoreType);

		return b.build();
	}

	private static void emptyToNull(String value, java.util.function.Consumer<String> setter) {
		if (value != null && !value.isBlank()) {
			setter.accept(value);
		}
	}
}
