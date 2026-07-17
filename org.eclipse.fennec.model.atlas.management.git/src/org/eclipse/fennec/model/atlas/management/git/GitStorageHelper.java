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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper;
import org.eclipse.fennec.model.atlas.workflow.ResourceSetCollector;
import org.gecko.jgit.api.GitService;
import org.gecko.jgit.api.TreeResult;
import org.osgi.service.component.ComponentServiceObjects;

/**
 * Read-only, git-backed {@link AbstractStorageHelper}.
 *
 * <p>Content is streamed straight out of the per-branch local clones maintained
 * by {@link GitService} (via {@link GitURIHandler}, {@code git://{commitId}/{path}},
 * no working-tree checkout). Each configured branch is a <em>stage</em>. Scope
 * and registry are model.atlas overlays that git does not carry, so they are
 * supplied by configuration (D5 refinement): a single {@code scope} plus a map
 * {@code eClassUri -> registryName}. Metadata is <em>derived</em> from git facts
 * (D1) rather than read from a stored {@code .metadata.xmi}: {@code stage} =
 * branch, {@code version} = commit SHA, {@code objectType} = the root object's
 * EClass URI, and {@code registry} = the map lookup on that EClass URI.
 *
 * <h3>objectId = {@code scope/stage/repoPath} (D9 workaround)</h3>
 * <p>The shared registry cache is keyed by {@code objectId} alone (one entry per
 * id, globally, across scopes/backends). File/apicurio are safe because an object
 * lives in one stage at a time, but git has the same repo path on several branches
 * (= stages) at once, so a bare repo-path objectId would collide across branches
 * (and across scopes sharing a stage name + path). The decided workaround is to
 * qualify the git objectId with {@code scope + "/" + stage + "/" + repoPath}. The
 * {@code scope} and {@code stage} are also passed as method parameters, so reads
 * strip the prefix back to the repo path; a caller may pass either the fully
 * qualified id or a bare repo path (the strip is lenient).
 *
 * <h3>Which ResourceSet parses what (G5)</h3>
 * <p>Determining {@code objectType} — and reading an instance — requires parsing,
 * which needs the object's EClass resolvable. A schema ({@code .ecore}) parses
 * against Ecore alone, so the injected management ResourceSet suffices. An
 * <em>instance</em> needs its (dynamic) EPackage, which lives in the
 * <em>per-(scope,stage)</em> ResourceSet produced by the registry chain. So for
 * every parse this helper prefers a fresh lease of that per-stage ResourceSet
 * from {@link ResourceSetCollector} (adding the {@code git://} handler to the
 * lease), falling back to the management ResourceSet when no per-stage
 * ResourceSet is available yet. Per-stage isolation is also required for
 * correctness: the same {@code nsURI} may carry different content on different
 * branches, so instances must resolve against their own stage's packages.
 *
 * <h3>Cold-start ordering (G5)</h3>
 * <p>At construction the repo's dynamic EPackages are not registered yet, so the
 * initial sweep derives schemas and skips instances. An external signal (an
 * {@code EPackage} ServiceTracker in {@code EObjectGitStorageService}) then calls
 * {@link #rederive()} as packages register; each pass re-attempts only the files
 * not yet derived, leasing a fresh per-stage ResourceSet — which also absorbs the
 * async propagation race (a not-yet-propagated package just fails this pass and
 * is retried on the next). No coupling to the registry/workflow services beyond
 * the leaf {@link ResourceSetCollector}, so there is no activation cycle.
 *
 * <p><b>Read-only:</b> {@code persistResource}/{@code deleteObject} throw.
 */
public class GitStorageHelper extends AbstractStorageHelper {

	private static final Logger LOGGER = Logger.getLogger(GitStorageHelper.class.getName());

	private static final String READ_ONLY_MESSAGE =
			"Git storage is read-only; writes happen externally on the git host";

	/** The Ecore {@code EObject} eClass URI — the configurable instance catch-all key. */
	private static final String EOBJECT_TYPE = EcoreUtil.getURI(EcorePackage.Literals.EOBJECT).toString();

	private final String scope;
	private final Map<String, String> eClassUriToRegistry;
	private final EObjectRegistryService<EObject> registryService;
	/** May be {@code null} (e.g. in unit tests) — then reads fall back to the management ResourceSet. */
	private final ResourceSetCollector resourceSetCollector;

	/** branch (= stage) -> its GitService. */
	private final Map<String, GitService> branchToService = new LinkedHashMap<>();
	/** commit id -> owning GitService; shared live with the {@link GitURIHandler}. */
	private final Map<String, GitService> commitToService = new ConcurrentHashMap<>();
	/** branch (= stage) -> last observed tree (files + tip commit id). */
	private final Map<String, TreeResult> branchToTree = new ConcurrentHashMap<>();

	/** Derived metadata, keyed by objectId ({@code scope/stage/repoPath}). */
	private final Map<String, ObjectMetadata> derived = new ConcurrentHashMap<>();

	private final GitURIHandler gitUriHandler;

	public GitStorageHelper(ResourceSet resourceSet, Collection<GitService> gitServices, String scope,
			Map<String, String> eClassUriToRegistry, EObjectRegistryService<EObject> registryService,
			ResourceSetCollector resourceSetCollector) {
		super(resourceSet);
		this.scope = scope;
		this.eClassUriToRegistry = Map.copyOf(eClassUriToRegistry);
		this.registryService = registryService;
		this.resourceSetCollector = resourceSetCollector;
		for (GitService gs : gitServices) {
			branchToService.put(gs.getBranch(), gs);
		}
		this.gitUriHandler = new GitURIHandler(commitToService);
		resourceSet.getURIConverter().getURIHandlers().add(0, gitUriHandler);

		refresh();
		deriveAll();
	}

	/**
	 * Fetches each branch and refreshes the cached tree + commit routing. Called
	 * at construction; later phases (G7 sync + reconcile poll) re-invoke it.
	 */
	public final synchronized void refresh() {
		for (Map.Entry<String, GitService> entry : branchToService.entrySet()) {
			GitService gs = entry.getValue();
			try {
				gs.fetch();
				TreeResult tree = gs.getFiles();
				branchToTree.put(entry.getKey(), tree);
				commitToService.put(tree.getCommitId(), gs);
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Failed to refresh git branch " + entry.getKey(), e);
			}
		}
	}

	/**
	 * Re-attempts derivation of any files not yet derived, across all branches,
	 * leasing a fresh per-stage ResourceSet so newly-registered EPackages are
	 * seen. Idempotent and cheap once everything is derived. Invoked by the
	 * EPackage ServiceTracker as packages register.
	 */
	public void rederive() {
		deriveAll();
	}

	private String commitIdForStage(String stage) {
		TreeResult tree = branchToTree.get(stage);
		return tree == null ? null : tree.getCommitId();
	}

	private String qualifiedId(String stage, String repoPath) {
		return scope + "/" + stage + "/" + repoPath;
	}

	private static String repoPathOf(String scope, String stage, String objectId) {
		String prefix = scope + "/" + stage + "/";
		return objectId.startsWith(prefix) ? objectId.substring(prefix.length()) : objectId;
	}

	private ComponentServiceObjects<ResourceSet> leaseFor(String stage) {
		return resourceSetCollector == null ? null : resourceSetCollector.getResourceSetObjects(scope, stage);
	}

	// --- read path ----------------------------------------------------------

	@Override
	protected URI createStorageURI(String scope, String registry, String stage, String path) {
		String commitId = commitIdForStage(stage);
		if (commitId == null) {
			return GitEMFHelper.createGitURI("unknown", path);
		}
		return GitEMFHelper.createGitURI(commitId, path);
	}

	@Override
	protected boolean storageExists(String scope, String registry, String stage, String path) throws IOException {
		TreeResult tree = branchToTree.get(stage);
		return tree != null && tree.getFiles().contains(path);
	}

	/**
	 * For git the {@code objectId} <em>is</em> the repo-relative path including the
	 * extension (D2), so there is no extension probing as in the file backend.
	 */
	@Override
	protected String findObjectPath(String scope, String registry, String stage, String objectId) throws IOException {
		String repoPath = repoPathOf(scope, stage, objectId);
		return storageExists(scope, registry, stage, repoPath) ? repoPath : null;
	}

	@Override
	public List<String> listObjectIds(String scope, String registry, String stage) throws IOException {
		List<String> ids = new ArrayList<>();
		for (ObjectMetadata md : derived.values()) {
			if (stage.equals(md.getStage()) && registry.equals(md.getRegistry())) {
				ids.add(md.getObjectId());
			}
		}
		return ids;
	}

	/**
	 * Metadata is derived, not stored, so this serves the derived entry (a copy)
	 * from the cache rather than reading a {@code .metadata.xmi}.
	 */
	@Override
	public ObjectMetadata loadMetadata(String scope, String registry, String stage, String objectId)
			throws IOException {
		ObjectMetadata md = derived.get(objectId);
		return md == null ? null : EcoreUtil.copy(md);
	}

	@Override
	public boolean objectExists(String scope, String registry, String stage, String objectId) throws IOException {
		return findObjectPath(scope, registry, stage, objectId) != null;
	}

	/**
	 * Reads an object against the per-(scope,stage) ResourceSet (so an instance's
	 * dynamic EPackage resolves), falling back to the management ResourceSet.
	 */
	@Override
	public EObject loadEObject(String scope, String registry, String stage, String objectId) throws IOException {
		String path = findObjectPath(scope, registry, stage, objectId);
		if (path == null) {
			return null;
		}
		URI uri = createStorageURI(scope, registry, stage, path);
		ComponentServiceObjects<ResourceSet> cso = leaseFor(stage);
		ResourceSet rs = cso != null ? cso.getService() : resourceSet;
		Resource resource = null;
		try {
			if (cso != null) {
				rs.getURIConverter().getURIHandlers().add(0, new GitURIHandler(commitToService));
			}
			resource = rs.getResource(uri, true);
			if (resource == null || resource.getContents().isEmpty()) {
				return null;
			}
			return resource.getContents().get(0);
		} finally {
			if (cso != null) {
				cso.ungetService(rs); // discards the prototype ResourceSet
			} else if (resource != null) {
				rs.getResources().remove(resource); // keep the shared management RS clean
			}
		}
	}

	@Override
	protected List<ObjectMetadata> loadAllStoredMetadata() throws IOException {
		deriveAll();
		return new ArrayList<>(derived.values());
	}

	/**
	 * One derivation sweep: for each branch, parse the files not yet derived
	 * against a leased per-stage ResourceSet (or the management ResourceSet when
	 * none is available), routing each to its registry and priming the cache.
	 */
	private synchronized void deriveAll() {
		for (Map.Entry<String, TreeResult> entry : branchToTree.entrySet()) {
			String stage = entry.getKey();
			TreeResult tree = entry.getValue();
			String commitId = tree.getCommitId();

			List<String> pending = new ArrayList<>();
			for (String path : tree.getFiles()) {
				if (hasRegisteredFactory(path) && !derived.containsKey(qualifiedId(stage, path))) {
					pending.add(path);
				}
			}
			if (pending.isEmpty()) {
				continue;
			}

			ComponentServiceObjects<ResourceSet> cso = leaseFor(stage);
			ResourceSet rs = cso != null ? cso.getService() : resourceSet;
			try {
				if (cso != null) {
					rs.getURIConverter().getURIHandlers().add(0, new GitURIHandler(commitToService));
				}
				for (String path : pending) {
					ObjectMetadata md = deriveOne(stage, commitId, path, rs);
					if (md != null) {
						derived.put(md.getObjectId(), md);
						registryService.updateCache(md);
					}
				}
			} finally {
				if (cso != null) {
					cso.ungetService(rs);
				}
			}
		}
	}

	/**
	 * Derives metadata for one repo file by parsing it (against {@code rs}) to
	 * obtain the root object's EClass URI, mapping it to a registry. Returns
	 * {@code null} (and logs) when the file cannot be parsed yet (typically an
	 * instance whose EPackage is not registered in {@code rs} — retried on a later
	 * {@link #rederive()}) or when its EClass URI is not routed to any registry.
	 */
	private ObjectMetadata deriveOne(String stage, String commitId, String path, ResourceSet rs) {
		URI uri = GitEMFHelper.createGitURI(commitId, path);
		Resource resource = null;
		try {
			resource = rs.getResource(uri, true);
			if (resource == null || resource.getContents().isEmpty()) {
				return null;
			}
			EObject root = resource.getContents().get(0);
			// Minimal genuine-object guard: only a resolved model object is routed.
			// (Unrecognized extensions are already filtered upstream; this rejects a
			// registered-extension file that parsed into an unresolved/proxy root.)
			if (root.eClass() == null || root.eClass().eIsProxy()) {
				LOGGER.fine(() -> "Unresolved root for " + path + "; ignoring");
				return null;
			}
			String objectType = EcoreUtil.getURI(root.eClass()).toString();
			// Exact eClass URI first (EPackages match their fixed constant), then the
			// EObject-URI catch-all IF the configurer declared one. No hardcoded
			// default: an unmapped type with no catch-all configured is ignored.
			String registry = eClassUriToRegistry.get(objectType);
			if (registry == null) {
				registry = eClassUriToRegistry.get(EOBJECT_TYPE);
			}
			if (registry == null) {
				LOGGER.fine(() -> "No registry mapped for type " + objectType + " (" + path + "); ignoring");
				return null;
			}
			ObjectMetadata md = ManagementFactory.eINSTANCE.createObjectMetadata();
			md.setObjectId(qualifiedId(stage, path));
			md.setScope(scope);
			md.setRegistry(registry);
			md.setStage(stage);
			md.setObjectType(objectType);
			md.setVersion(commitId);
			return md;
		} catch (Exception e) {
			LOGGER.log(Level.FINE, e,
					() -> "Skipping " + path + " on " + stage + " (not parseable yet): " + e.getMessage());
			return null;
		} finally {
			if (resource != null) {
				rs.getResources().remove(resource);
			}
		}
	}

	private boolean hasRegisteredFactory(String path) {
		int dot = path.lastIndexOf('.');
		if (dot < 0) {
			return false;
		}
		String ext = path.substring(dot + 1);
		return resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().containsKey(ext);
	}

	// --- write path (read-only backend) -------------------------------------

	@Override
	protected void persistResource(String path, Resource resource) throws IOException {
		throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
	}

	@Override
	public boolean deleteObject(String scope, String registry, String stage, String objectId) throws IOException {
		throw new UnsupportedOperationException(READ_ONLY_MESSAGE);
	}

	// --- lifecycle ----------------------------------------------------------

	@Override
	protected void closeStorageResources() throws Exception {
		resourceSet.getURIConverter().getURIHandlers().remove(gitUriHandler);
		branchToService.clear();
		branchToTree.clear();
		commitToService.clear();
		derived.clear();
	}
}
