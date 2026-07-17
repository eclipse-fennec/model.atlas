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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.model.atlas.mgmt.annotations.MacCapabilityConstants;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.management.StorageBackendType;
import org.eclipse.fennec.model.atlas.mgmt.storage.AbstractEObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper;
import org.eclipse.fennec.model.atlas.workflow.ResourceSetCollector;
import org.gecko.jgit.api.GitService;
import org.osgi.annotation.bundle.Capability;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
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
 * <p><b>Deferred:</b> inbound webhook / reconcile-poll driven resync and the
 * ENTER/UPDATE/EXIT dispatch are added in G5/G7; this phase establishes the
 * read-only storage backend and its startup cache priming.
 */
@RequireEMF
@RequireConfigurationAdmin
@Component(name = EObjectGitStorageService.PID, service = EObjectStorageService.class, property = "storage.backend=git", configurationPolicy = ConfigurationPolicy.REQUIRE)
@Designate(ocd = EObjectGitStorageService.Config.class)
@Capability(namespace = MacCapabilityConstants.NAMESPACE_MAC_MANAGEMENT, name = MacCapabilityConstants.CAP_EOBJECT_STORAGE, version = "1.0", attribute = "storage.backend=git")
@ServiceDescription("Read-only git-backed storage implementation for EObject workflow management")
public class EObjectGitStorageService extends AbstractEObjectStorageService {

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

	@Activate
	public void activate(BundleContext bundleContext, Config config) throws Exception {
		this.bctx = bundleContext;
		this.scope = config.scope();
		this.storageTypeLabel = config.storage_type();
		this.typeToRegistry = parseTypeToRegistry(config.type_registry_map());
		activateStorageService();
		LOGGER.info("Git storage service activated: scope=" + scope + ", branches="
				+ gitServices.size() + ", type-registry entries=" + typeToRegistry.size());

		this.rederiveExecutor = Executors.newSingleThreadExecutor(r -> {
			Thread t = new Thread(r, "git-rederive-" + scope);
			t.setDaemon(true);
			return t;
		});
		this.ePackageTracker = new ServiceTracker<>(bundleContext, EPackage.class,
				new ServiceTrackerCustomizer<>() {
					@Override
					public EPackage addingService(ServiceReference<EPackage> reference) {
						scheduleRederive();
						return bundleContext.getService(reference);
					}

					@Override
					public void modifiedService(ServiceReference<EPackage> reference, EPackage service) {
						// no-op
					}

					@Override
					public void removedService(ServiceReference<EPackage> reference, EPackage service) {
						bundleContext.ungetService(reference);
					}
				});
		this.ePackageTracker.open();
	}

	@Deactivate
	public void deactivate() {
		if (ePackageTracker != null) {
			ePackageTracker.close();
		}
		if (rederiveExecutor != null) {
			rederiveExecutor.shutdownNow();
		}
		deactivateStorageService();
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
					LOGGER.warning("Git re-derivation pass failed: " + e.getMessage());
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
		return gitHelper;
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
