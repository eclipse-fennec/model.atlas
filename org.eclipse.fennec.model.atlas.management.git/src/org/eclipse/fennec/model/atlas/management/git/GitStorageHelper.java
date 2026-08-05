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
import java.io.InputStream;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.HexFormat;
import java.util.function.BiConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.PackageNotFoundException;
import org.eclipse.fennec.emf.osgi.fingerprint.util.FingerprintHelper;
import org.eclipse.fennec.model.atlas.mgmt.storage.ModelUnavailableException;
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
 * EClass URI, {@code registry} = the map lookup on that EClass URI, and
 * {@code contentHash} = SHA-256 of the raw blob bytes (per file — stable across
 * commits that do not touch the file, unlike {@code version}).
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

	/** The Ecore {@code EPackage} eClass URI — identifies a derived entry as a schema. */
	private static final String EPACKAGE_TYPE = EcoreUtil.getURI(EcorePackage.Literals.EPACKAGE).toString();

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

	/**
	 * Invoked (if set) after a {@link #reconcile(String)} that actually moved a branch tip,
	 * with the reconciled {@code stage} and the objectIds of schemas <em>removed</em> by that
	 * reconcile. The owning storage service uses it to request a registry replay (D8 change D)
	 * — a push may have added/changed a schema (ENTER) or removed one (EXIT, D8-3). {@code null}
	 * in unit tests / when no listener is wired.
	 */
	private volatile BiConsumer<String, List<String>> onReconciled;

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

	/**
	 * Re-synchronizes a single branch (= stage) with its remote after an inbound
	 * push (webhook) or a reconcile poll (G7). Fetches the branch and compares the
	 * tip commit against the last observed one; if unchanged this is a no-op, so a
	 * webhook and a poll firing for the same push are idempotent.
	 *
	 * <p>When the tip moved the whole branch is re-derived: {@link GitService#getFiles()}
	 * exposes only paths + tip commit (no per-file SHA), so an in-place modify cannot
	 * be detected cheaply. This stage's derived entries are therefore evicted from the
	 * shared registry cache and re-derived against the new tree — which covers added,
	 * modified and removed files uniformly (removed files simply do not reappear).
	 *
	 * <p><b>Not covered (deferred, see PLAN):</b> a <em>new or modified schema</em>
	 * whose dynamic {@code EPackage} is not yet registered at runtime will not be
	 * (re)registered here — that hits the registry dispatch cycle. Its instances
	 * become derivable only once the package is registered (via {@link #rederive()}).
	 *
	 * @param stage the branch to reconcile
	 * @return {@code true} if the tip moved and the stage was re-derived, {@code false}
	 *         if nothing changed or the branch is unknown
	 */
	public synchronized boolean reconcile(String stage) {
		GitService gs = branchToService.get(stage);
		if (gs == null) {
			LOGGER.fine(() -> "reconcile: no branch bound for stage " + stage + "; ignoring");
			return false;
		}
		String previousCommit = commitIdForStage(stage);
		TreeResult tree;
		try {
			gs.fetch();
			tree = gs.getFiles();
		} catch (Exception e) {
			LOGGER.log(Level.WARNING, "Failed to reconcile git branch " + stage, e);
			return false;
		}
		String newCommit = tree.getCommitId();
		if (newCommit != null && newCommit.equals(previousCommit)) {
			LOGGER.fine(() -> "reconcile: branch " + stage + " already at " + newCommit + "; no change");
			return false;
		}

		// Capture the schemas present in this stage before the evict, so we can tell which
		// ones a removal push deleted (present before, absent after re-derive) and drive EXIT.
		List<String> schemasBefore = schemaObjectIds(stage);

		evictStage(stage);
		if (previousCommit != null) {
			commitToService.remove(previousCommit);
		}
		branchToTree.put(stage, tree);
		commitToService.put(newCommit, gs);

		deriveAll();

		List<String> schemasAfter = schemaObjectIds(stage);
		List<String> removedSchemas = new ArrayList<>();
		for (String id : schemasBefore) {
			if (!schemasAfter.contains(id)) {
				removedSchemas.add(id);
			}
		}

		LOGGER.info("Reconciled git branch " + stage + ": " + previousCommit + " -> " + newCommit
				+ (removedSchemas.isEmpty() ? "" : " (removed schemas: " + removedSchemas + ")"));
		BiConsumer<String, List<String>> listener = onReconciled;
		if (listener != null) {
			listener.accept(stage, removedSchemas);
		}
		return true;
	}

	/** ObjectIds of the derived <em>schemas</em> (EPackages) currently in {@code stage}. */
	private List<String> schemaObjectIds(String stage) {
		List<String> ids = new ArrayList<>();
		for (ObjectMetadata md : derived.values()) {
			if (stage.equals(md.getStage()) && EPACKAGE_TYPE.equals(md.getObjectType())) {
				ids.add(md.getObjectId());
			}
		}
		return ids;
	}

	/**
	 * Sets the callback invoked after a reconcile that moved a branch tip (see
	 * {@link #onReconciled}): {@code (stage, removedSchemaObjectIds)}. Used by the storage
	 * service to publish a registry-resync event; left unset in unit tests.
	 */
	public void setOnReconciled(BiConsumer<String, List<String>> onReconciled) {
		this.onReconciled = onReconciled;
	}

	/**
	 * Reconciles every configured branch with its remote. This is the reconcile
	 * poll entry point (G7 part C): it simply calls {@link #reconcile(String)} for
	 * each branch, which no-ops when the tip has not moved — so a poll and an
	 * inbound webhook for the same push are idempotent. A failure on one branch is
	 * logged and does not stop the others.
	 */
	public void reconcileAll() {
		for (String stage : new ArrayList<>(branchToService.keySet())) {
			try {
				reconcile(stage);
			} catch (Exception e) {
				LOGGER.log(Level.WARNING, "Reconcile poll failed for branch " + stage, e);
			}
		}
	}

	/**
	 * Drops every derived entry of one stage from both the local {@link #derived}
	 * map and the shared registry cache, so a re-derive of the branch's new tip
	 * starts clean (removed files disappear, modified files pick up new content and
	 * commit version).
	 */
	private void evictStage(String stage) {
		Iterator<Map.Entry<String, ObjectMetadata>> it = derived.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<String, ObjectMetadata> entry = it.next();
			if (stage.equals(entry.getValue().getStage())) {
				registryService.removeFromCache(entry.getKey());
				it.remove();
			}
		}
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
	 *
	 * <p>If the object's model is not registered (typically a schema removed on this branch
	 * while the instance file remains — D8-3), the parse fails with EMF's
	 * {@code PackageNotFoundException}; this is translated to a clean
	 * {@link ModelUnavailableException} so callers get a defined "model unavailable" signal
	 * rather than an opaque failure.
	 */
	@Override
	public EObject loadEObject(String scope, String registry, String stage, String objectId) throws IOException {
		String path = findObjectPath(scope, registry, stage, objectId);
		if (path == null) {
			return null;
		}
		URI uri = createStorageURI(scope, registry, stage, path);
		ComponentServiceObjects<ResourceSet> cso = leaseFor(stage);
		LOGGER.fine(() -> "loadEObject " + objectId + " on stage " + stage + " using "
				+ (cso != null ? "per-stage leased ResourceSet" : "management ResourceSet (no per-stage lease)"));
		ResourceSet rs = cso != null ? cso.getService() : resourceSet;
		Resource resource = null;
		try {
			if (cso != null) {
				rs.getURIConverter().getURIHandlers().add(0, new GitURIHandler(commitToService));
			}
			try {
				resource = rs.getResource(uri, true);
			} catch (RuntimeException e) {
				// getResource wraps a load IOException (incl. PackageNotFoundException) in a
				// WrappedException; surface the missing-model case as ModelUnavailableException.
				PackageNotFoundException pnf = findPackageNotFound(e);
				if (pnf != null) {
					throw new ModelUnavailableException(scope, stage, objectId, pnf.uri(), e);
				}
				throw e;
			}
			// EMF may also record the missing package as a resource error rather than throw.
			PackageNotFoundException pnf = resource == null ? null : findPackageNotFoundInErrors(resource);
			if (pnf != null) {
				throw new ModelUnavailableException(scope, stage, objectId, pnf.uri(), pnf);
			}
			if (resource == null || resource.getContents().isEmpty()) {
				return null;
			}
			// Resolve cross-references (e.g. an EReference's eType pointing at an EClass in
			// another .ecore, or an instance referencing another object) WHILE the leased
			// per-stage ResourceSet — and its package registry — are still alive. These are
			// lazy proxies otherwise resolved only on access, which for the caller happens
			// after this method has released the ResourceSet, leaving them permanently
			// unresolvable. Unresolvable proxies (e.g. a referenced package genuinely absent)
			// are left as-is by resolveAll rather than throwing.
			EcoreUtil.resolveAll(resource);
			return resource.getContents().get(0);
		} finally {
			// Detach the resource from the ResourceSet BEFORE releasing a leased (prototype)
			// ResourceSet: ungetService discards that ResourceSet, and a resource still held by
			// it would be disposed along with it, leaving the returned EObject with a null
			// eResource() (which breaks downstream EPackage registration). Removing it first
			// keeps the returned object intact (eResource present, getResourceSet() null) —
			// matching the shared-management-RS path.
			if (resource != null) {
				rs.getResources().remove(resource);
			}
			if (cso != null) {
				cso.ungetService(rs); // discards the prototype ResourceSet
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
			md.setContentHash(computeContentHash(commitId, path));
			// Same property the schema upload path stamps (SchemaPackagesResource):
			// the only nsURI -> objectId mapping a client can get from listings,
			// and the nsUri column of the scope aggregate manifest.
			if (root instanceof EPackage pkg && pkg.getNsURI() != null) {
				md.getProperties().put("nsUri", pkg.getNsURI());
				// Model fingerprint for schemas — same producer contract as the upload
				// path (AbstractEObjectStorageService.storeObject): computed here from
				// the parsed content, never adopted from anywhere. Like contentHash, a
				// failed computation must never fail the derivation.
				try {
					md.setFingerprint(FingerprintHelper.fingerprint(pkg));
				} catch (Exception e) {
					LOGGER.log(Level.FINE, e,
							() -> "Fingerprint computation failed for " + path + ": " + e.getMessage());
				}
			}
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

	/**
	 * SHA-256 (lowercase hex) over the raw blob bytes at {@code commitId/path} —
	 * the stored XMI content itself; this backend has no write-path
	 * re-serialization to hash. Returns {@code null} (logged FINE) when the blob
	 * cannot be read or hashed: a hash must never fail the derivation.
	 */
	private String computeContentHash(String commitId, String path) {
		GitService gs = commitToService.get(commitId);
		if (gs == null) {
			return null;
		}
		try (InputStream in = gs.readFile(commitId, path)) {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			byte[] buffer = new byte[8192];
			int read;
			while ((read = in.read(buffer)) >= 0) {
				digest.update(buffer, 0, read);
			}
			return HexFormat.of().formatHex(digest.digest());
		} catch (Exception e) {
			LOGGER.log(Level.FINE, e,
					() -> "Could not hash " + path + "@" + commitId + ": " + e.getMessage());
			return null;
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
