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
package org.eclipse.fennec.model.atlas.management.git;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.git.webhook.model.gitwebhook.WebhookPayload;
import org.eclipse.fennec.git.webhook.utils.WebhookTopics;
import org.eclipse.fennec.jgit.api.GitService;
import org.eclipse.fennec.model.atlas.mgmt.annotations.MacCapabilityConstants;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.management.StorageBackendType;
import org.eclipse.fennec.model.atlas.mgmt.storage.AbstractEObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper;
import org.eclipse.fennec.model.atlas.workflow.RegistryResync;
import org.eclipse.fennec.model.atlas.workflow.ResourceSetCollector;
import org.osgi.annotation.bundle.Capability;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.ConfigurationPolicy;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.component.annotations.ReferenceCardinality;
import org.osgi.service.component.annotations.ReferencePolicy;
import org.osgi.service.component.annotations.ReferencePolicyOption;
import org.osgi.service.component.propertytypes.ServiceDescription;
import org.osgi.service.metatype.annotations.AttributeDefinition;
import org.osgi.service.metatype.annotations.Designate;
import org.osgi.service.metatype.annotations.ObjectClassDefinition;
import org.osgi.service.typedevent.TypedEventBus;
import org.osgi.service.typedevent.TypedEventConstants;
import org.osgi.service.typedevent.TypedEventHandler;
import org.osgi.service.typedevent.annotations.RequireTypedEvent;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

/**
 * Read-only, git-backed {@link EObjectStorageService} (D-INT (a)): a thin
 * {@link AbstractEObjectStorageService} that delegates all reads to a
 * {@link GitStorageHelper}. Git stages surface only via {@code ReadableScopeService};
 * every write throws (see {@code GitStorageHelper}).
 *
 * <h3>Branch = stage</h3>
 * <p>The backend binds one {@link GitService} per branch of the repo (each
 * {@code GitService} = one repo + one branch, from a {@code GitConfig} factory).
 * Which services belong to this backend is selected by the configurable
 * {@code gitservice.target} filter. The binding is {@code STATIC}/{@code GREEDY},
 * so the backend re-activates (rebuilding the helper) whenever the set of matching
 * branches changes.
 *
 * <h3>Scope / registry (D5)</h3>
 * <p>Git carries neither; both are supplied here. {@code scope} is a single
 * configured value; {@code type.registry.map} is a list of
 * {@code eClassUri:registryName} entries the helper uses to route objects
 * (schemas via the fixed {@code EPackage} URI, instances via a configured
 * {@code EObject} catch-all — see {@link GitStorageHelper}).
 *
 * <h3>Webhook resync (G7)</h3>
 * <p>This service is also a {@link TypedEventHandler} for the neutral
 * {@link WebhookPayload}: on activation it registers itself on the whiteboard
 * with the set of {@link WebhookTopics#topicFor(String, String) topics} for its
 * configured branches (repository derived from each {@link GitService}'s clone
 * URL, so the subscriber and the webhook ingest layer compute the same topic).
 * An inbound push routes to {@link GitStorageHelper#reconcile(String)} for the
 * pushed branch, off the event-delivery thread. Topics are per-branch runtime
 * values, so the handler is registered manually (not via component properties).
 *
 * <h3>Reconcile poll (G7)</h3>
 * <p>As a safety net for missed or undelivered webhooks, a background poll
 * ({@code poll.interval.seconds}, default 60; non-positive disables it)
 * periodically calls {@link GitStorageHelper#reconcileAll()}, which no-ops for
 * every branch whose tip has not moved — so it is idempotent with the webhook
 * path and only re-derives on a genuine change.
 *
 * <p><b>Deferred:</b> the ENTER/UPDATE/EXIT runtime schema (re)registration
 * dispatch remains outstanding (it hits the registry dispatch cycle).
 */
@RequireEMF
@RequireConfigurationAdmin
@RequireTypedEvent
@Component(name = EObjectGitStorageService.PID, service = EObjectStorageService.class, property = "storage.backend=git", configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = EObjectGitStorageService.Config.class)
@Capability(namespace = MacCapabilityConstants.NAMESPACE_MAC_MANAGEMENT, name = MacCapabilityConstants.CAP_EOBJECT_STORAGE, version = "1.0", attribute = "storage.backend=git")
@ServiceDescription("Read-only git-backed storage implementation for EObject workflow management")
public class EObjectGitStorageService extends AbstractEObjectStorageService
		implements TypedEventHandler<WebhookPayload> {

	public static final String PID = "GitObjectStorage";

	@ObjectClassDefinition(name = "Git Storage Configuration", description = "Configuration for the read-only git-backed EObject storage service")
	public @interface Config {

		@AttributeDefinition(name = "Repository", description = "Base repository URL (informational; branch selection is via gitservice.target)")
		String repo() default "";

		@AttributeDefinition(name = "Scope", description = "The single model.atlas scope this repository is exposed under")
		String scope();

		@AttributeDefinition(name = "Type-to-registry map", description = "Entries 'eClassUri:registryName' routing objects to registries; "
				+ "use the EObject eClass URI as the instance catch-all. Split on the last ':'.")
		String[] type_registry_map();

		@AttributeDefinition(name = "GitService target", description = "OSGi target filter selecting the GitService(s) (one per branch = stage) of this repo")
		String gitservice_target() default "";

		@AttributeDefinition(name = "Reconcile poll interval (seconds)", description = "Interval of the background reconcile poll that re-syncs each branch with its remote "
				+ "(tip-SHA compare, idempotent with inbound webhooks). 0 or negative disables the poll.")
		int poll_interval_seconds() default 60;

		@AttributeDefinition(name = "Storage Type", description = "Type label of this storage service")
		String storage_type() default "git";
	}

	@Reference(target = "(emf.name=management)")
	private ResourceSet resourceSet;

	@Reference
	private EObjectRegistryService<EObject> registry;

	/**
	 * Hands out the per-(scope,stage) ResourceSet that already carries the stage's
	 * dynamic EPackages, so instances parse against their own stage's packages.
	 * It is an always-present leaf singleton (no back-reference to any storage or
	 * registry service), so this reference introduces no activation cycle.
	 */
	@Reference
	private ResourceSetCollector resourceSetCollector;

	/**
	 * Used to publish a {@link RegistryResync} event after a reconcile that moved a branch
	 * tip, so the workflow layer replays registration for this scope and (re)registers
	 * schemas a push added or changed (D8 change D). Publishing (rather than calling the
	 * registry directly) keeps the storage → dispatcher activation cycle broken.
	 */
	@Reference
	private TypedEventBus eventBus;

	/**
	 * One {@link GitService} per branch (= stage). {@code MULTIPLE}/{@code STATIC}/
	 * {@code GREEDY}: the component re-activates when the matching set changes, so
	 * the helper is rebuilt with the current branches. Target is set from config
	 * via {@code gitservice.target}.
	 */
	@Reference(name = "gitservice", service = GitService.class, cardinality = ReferenceCardinality.MULTIPLE, policy = ReferencePolicy.STATIC, policyOption = ReferencePolicyOption.GREEDY)
	private volatile List<GitService> gitServices;

	private String scope;
	private Map<String, String> typeToRegistry;
	private String storageTypeLabel;

	private GitStorageHelper gitHelper;

	/**
	 * Tracks EPackage registrations as the signal to re-derive instances (their
	 * models become parseable only once registered — see {@link GitStorageHelper}).
	 * Bursts are coalesced onto a single worker so a batch of registrations causes
	 * roughly one re-derivation pass.
	 */
	private ServiceTracker<EPackage, EPackage> ePackageTracker;
	private ExecutorService rederiveExecutor;
	private final AtomicBoolean rederiveScheduled = new AtomicBoolean(false);

	/** Whiteboard registration of this service as the webhook {@link TypedEventHandler}. */
	@SuppressWarnings("rawtypes")
	private ServiceRegistration<TypedEventHandler> webhookRegistration;

	/**
	 * Background reconcile poll (G7 part C): periodically re-syncs each branch with
	 * its remote as a safety net for missed/undelivered webhooks. {@code null} when
	 * the configured interval disables it.
	 */
	private ScheduledExecutorService pollExecutor;

	@Activate
	public void activate(BundleContext bundleContext, Config config) throws Exception {
		this.bctx = bundleContext;
		this.scope = config.scope();
		this.storageTypeLabel = config.storage_type();
		this.typeToRegistry = parseTypeToRegistry(config.type_registry_map());
		this.rederiveExecutor = Executors.newSingleThreadExecutor(r -> {
			Thread t = new Thread(r, "git-rederive-" + scope);
			t.setDaemon(true);
			return t;
		});
		try {
			activateStorageService();
		} catch (Exception e) {
			// DS never calls deactivate() when activate() throws — undo ourselves.
			rederiveExecutor.shutdownNow();
			rederiveExecutor = null;
			throw e;
		}
		LOGGER.info("Git storage service activated: scope=" + scope + ", branches="
				+ gitServices.size() + ", type-registry entries=" + typeToRegistry.size());

		// The initial remote fetch + full-repo derivation runs on the re-derive
		// worker, never on the SCR thread: the STATIC/GREEDY gitservice reference
		// re-triggers activation on every branch-set change, and a hung remote must
		// not stall it. Reads simply see an empty store until priming completes.
		GitStorageHelper helper = this.gitHelper;
		rederiveExecutor.execute(() -> {
			try {
				helper.prime();
				// The workflow's cold-start replay may have run while the store was
				// still empty — request an ENTER replay now that content is derived.
				publishResync(null, List.of());
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Initial git refresh/derivation failed for scope " + scope, e);
			}
		});

		this.ePackageTracker = new ServiceTracker<>(bundleContext, EPackage.class,
				new ServiceTrackerCustomizer<>() {
					@Override
					public EPackage addingService(ServiceReference<EPackage> reference) {
						scheduleRederive();
						// Only the notification matters here; returning null keeps the
						// service untracked, so no EPackage in the framework gets
						// instantiated or pinned by a use count on our behalf.
						return null;
					}

					@Override
					public void modifiedService(ServiceReference<EPackage> reference, EPackage service) {
						// no-op
					}

					@Override
					public void removedService(ServiceReference<EPackage> reference, EPackage service) {
						// Unreachable: addingService returns null, so nothing is tracked.
					}
				});
		this.ePackageTracker.open();

		registerWebhookHandler(bundleContext);
		startReconcilePoll(config.poll_interval_seconds());
	}

	@Deactivate
	public void deactivate() {
		if (webhookRegistration != null) {
			try {
				webhookRegistration.unregister();
			} catch (IllegalStateException alreadyUnregistered) {
				// service already gone; nothing to do
			}
			webhookRegistration = null;
		}
		if (pollExecutor != null) {
			pollExecutor.shutdownNow();
		}
		if (ePackageTracker != null) {
			ePackageTracker.close();
		}
		if (rederiveExecutor != null) {
			rederiveExecutor.shutdownNow();
		}
		deactivateStorageService();
	}

	/**
	 * Starts the background reconcile poll: every {@code intervalSeconds} it calls
	 * {@link GitStorageHelper#reconcileAll()}, which re-syncs each branch and no-ops
	 * where the tip has not moved (so it is idempotent with inbound webhooks and
	 * only re-derives on a genuine change). It runs with a <em>fixed delay</em> on a
	 * single daemon thread, so passes never overlap; a concurrent webhook-triggered
	 * reconcile is safe because {@link GitStorageHelper#reconcile(String)} is
	 * synchronized. A non-positive interval disables the poll (webhook-only).
	 */
	private void startReconcilePoll(int intervalSeconds) {
		if (intervalSeconds <= 0) {
			LOGGER.info("Reconcile poll disabled for scope " + scope + " (interval=" + intervalSeconds + ")");
			return;
		}
		this.pollExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
			Thread t = new Thread(r, "git-reconcile-poll-" + scope);
			t.setDaemon(true);
			return t;
		});
		this.pollExecutor.scheduleWithFixedDelay(() -> {
			GitStorageHelper helper = gitHelper;
			if (helper == null) {
				return;
			}
			try {
				helper.reconcileAll();
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Reconcile poll pass failed for scope " + scope, e);
			}
		}, intervalSeconds, intervalSeconds, TimeUnit.SECONDS);
		LOGGER.info("Reconcile poll started for scope " + scope + " every " + intervalSeconds + "s");
	}

	/**
	 * Registers this service as the whiteboard {@link TypedEventHandler} for the
	 * webhook topics of its configured branches. The repository component of each
	 * topic is derived from the {@link GitService}'s clone URL so it matches the
	 * repository the webhook ingest layer reports; both sides run it through
	 * {@link WebhookTopics#topicFor(String, String)} for identical sanitization.
	 * Topics are runtime values (branch set + clone URLs), so the handler is
	 * registered here rather than via component properties.
	 */
	private void registerWebhookHandler(BundleContext bundleContext) {
		String[] topics = gitServices.stream()
				.map(gs -> WebhookTopics.topicFor(repoFullNameFromCloneUrl(gs.getGitUrl()), gs.getBranch()))
				.distinct()
				.toArray(String[]::new);
		if (topics.length == 0) {
			LOGGER.warning("No git branches configured; webhook resync handler not registered for scope " + scope);
			return;
		}
		Dictionary<String, Object> props = new Hashtable<>();
		props.put(TypedEventConstants.TYPED_EVENT_TOPICS, topics);
		this.webhookRegistration = bundleContext.registerService(TypedEventHandler.class, this, props);
		LOGGER.info("Registered git webhook resync handler for scope " + scope + " on topics "
				+ Arrays.toString(topics));
	}

	/**
	 * Handles an inbound push webhook: reconciles the pushed branch with its remote.
	 * Runs on the single re-derive worker (not the event-delivery thread) so a git
	 * fetch does not block event dispatch, and so it never races the EPackage-driven
	 * re-derivation. The tip-commit check inside {@link GitStorageHelper#reconcile}
	 * makes a webhook and a reconcile poll for the same push idempotent.
	 */
	@Override
	public void notify(String topic, WebhookPayload event) {
		ExecutorService executor = rederiveExecutor;
		GitStorageHelper helper = gitHelper;
		if (executor == null || helper == null) {
			return;
		}
		String branch = WebhookTopics.branchFromRef(event.getRef());
		LOGGER.info("Webhook received on topic " + topic + " for branch " + branch + " (scope " + scope + ")");
		executor.execute(() -> {
			try {
				helper.reconcile(branch);
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Reconcile after webhook failed for branch " + branch, e);
			}
		});
	}

	/**
	 * Reduces a git clone URL to its namespaced repository id ({@code owner/repo},
	 * or {@code group/subgroup/repo} for GitLab), matching what the webhook payload
	 * reports as its repository full name. Handles the HTTP(S) form
	 * ({@code https://host/owner/repo.git}, optionally with embedded credentials)
	 * and the SSH scp-like form ({@code git@host:owner/repo.git}); a trailing
	 * {@code .git} and any leading slashes are stripped.
	 */
	static String repoFullNameFromCloneUrl(String cloneUrl) {
		if (cloneUrl == null || cloneUrl.isBlank()) {
			return "";
		}
		String s = cloneUrl.trim();
		if (s.endsWith(".git")) {
			s = s.substring(0, s.length() - ".git".length());
		}
		int scheme = s.indexOf("://");
		if (scheme >= 0) {
			// https://[user[:token]@]host/owner/repo -> strip scheme + authority
			String rest = s.substring(scheme + 3);
			int slash = rest.indexOf('/');
			s = slash >= 0 ? rest.substring(slash + 1) : rest;
		} else {
			// scp-like git@host:owner/repo (or bare host:owner/repo) -> keep after ':'
			int colon = s.indexOf(':');
			if (colon >= 0) {
				s = s.substring(colon + 1);
			}
		}
		while (s.startsWith("/")) {
			s = s.substring(1);
		}
		return s;
	}

	/**
	 * Coalesces a re-derivation onto the single worker: if one is already pending,
	 * this is a no-op, so a burst of EPackage registrations collapses to ~one pass.
	 */
	private void scheduleRederive() {
		ExecutorService executor = rederiveExecutor;
		if (executor == null || gitHelper == null) {
			return;
		}
		if (rederiveScheduled.compareAndSet(false, true)) {
			executor.execute(() -> {
				rederiveScheduled.set(false);
				try {
					gitHelper.rederive();
				} catch (Exception e) {
					LOGGER.log(Level.WARNING, "Git re-derivation pass failed", e);
				}
			});
		}
	}

	/**
	 * Parses {@code eClassUri:registryName} entries. Split on the <em>last</em>
	 * {@code ':'} because eClass URIs themselves contain colons (scheme, {@code #//});
	 * registry names never do. Blank/malformed entries are skipped.
	 */
	static Map<String, String> parseTypeToRegistry(String[] entries) {
		Map<String, String> map = new LinkedHashMap<>();
		if (entries == null) {
			return map;
		}
		for (String entry : entries) {
			if (entry == null || entry.isBlank()) {
				continue;
			}
			int sep = entry.lastIndexOf(':');
			if (sep <= 0 || sep == entry.length() - 1) {
				LOGGER.warning("Ignoring malformed type-to-registry entry (expected 'eClassUri:registryName'): " + entry);
				continue;
			}
			String uri = entry.substring(0, sep).trim();
			String registryName = entry.substring(sep + 1).trim();
			if (!uri.isEmpty() && !registryName.isEmpty()) {
				map.put(uri, registryName);
			}
		}
		return map;
	}

	@Override
	protected AbstractStorageHelper createStorageHelper() throws Exception {
		LOGGER.info("Creating git storage helper for scope " + scope);
		this.gitHelper = new GitStorageHelper(resourceSet, gitServices, scope, typeToRegistry, registry,
				resourceSetCollector);
		// On a reconcile that moved a branch tip, ask the workflow layer to replay
		// registration for this scope so a pushed/changed schema gets (re)registered.
		this.gitHelper.setOnReconciled(this::publishResync);
		return gitHelper;
	}

	/**
	 * Publishes a {@link RegistryResync} event after a reconcile that moved a branch tip —
	 * or, with a {@code null} stage, after the initial priming pass. Carries the scope (for
	 * the ENTER replay that (re)registers present/changed schemas) plus the reconciled
	 * {@code stage} and any {@code removedObjectIds} (schemas the push deleted, for the EXIT
	 * that unregisters them — D8-3). Failures are logged, not propagated (the read path
	 * stays available).
	 */
	private void publishResync(String stage, List<String> removedObjectIds) {
		TypedEventBus bus = eventBus;
		if (bus == null) {
			return;
		}
		try {
			Map<String, Object> payload = new LinkedHashMap<>();
			payload.put(RegistryResync.KEY_SCOPE, scope);
			if (stage != null) {
				payload.put(RegistryResync.KEY_STAGE, stage);
			}
			if (removedObjectIds != null && !removedObjectIds.isEmpty()) {
				payload.put(RegistryResync.KEY_REMOVED_OBJECT_IDS, removedObjectIds.toArray(new String[0]));
			}
			bus.deliverUntyped(RegistryResync.TOPIC, payload);
			LOGGER.info("Published registry resync for scope " + scope
					+ (stage == null ? " (initial priming)" : " stage " + stage)
					+ (removedObjectIds == null || removedObjectIds.isEmpty() ? ""
							: " (removed " + removedObjectIds.size() + " schema(s))"));
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Failed to publish registry resync for scope " + scope, e);
		}
	}

	@Override
	public StorageBackendType getBackendType() {
		return StorageBackendType.GIT;
	}

	@Override
	protected EObjectRegistryService<EObject> getRegistryService() {
		return registry;
	}

	@Override
	public String getStorageType() {
		return storageTypeLabel;
	}
}
