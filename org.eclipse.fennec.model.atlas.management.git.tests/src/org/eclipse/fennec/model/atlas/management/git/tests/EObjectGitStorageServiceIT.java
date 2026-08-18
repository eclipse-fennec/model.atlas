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
package org.eclipse.fennec.model.atlas.management.git.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.git.github.webhook.model.githubwebhook.GithubPayload;
import org.eclipse.fennec.git.github.webhook.model.githubwebhook.GithubWebhookFactory;
import org.eclipse.fennec.git.webhook.model.gitwebhook.WebhookPayload;
import org.eclipse.fennec.git.webhook.utils.WebhookTopics;
import org.eclipse.fennec.jgit.api.GitService;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.StorageBackendType;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations.RegistryConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.service.typedevent.TypedEventBus;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OSGi integration tests for the read-only git-backed {@code EObjectStorageService}.
 *
 * <p>A throw-away container ({@link GitTestRepository}) serves a real repository over
 * the anonymous {@code git://} protocol. The {@link GitService}s the backend binds to
 * are the production {@code org.gecko.jgit.GitServiceImpl} (one per branch = stage),
 * created via {@code GitConfig} factory configurations with {@code id=testrepo} and
 * <em>no</em> {@code privateKey} — since gecko.jgit moved to the Apache MINA sshd
 * backend, the SSH session factory is only installed when a key is configured, so
 * anonymous {@code git://} fetches work. The {@code GitObjectStorage} configuration
 * selects the services via {@code gitservice.target=(id=testrepo)}. Configurations are
 * created at runtime once the container's mapped port is known (the git URL is only
 * knowable then).
 *
 * <p>These tests are all D8-independent: they cover DS wiring, activation, schema
 * derivation and read-only reads. The reload / referential-integrity matrix (D8) is
 * deferred until that contract is decided.
 */
@RequireConfigurationAdmin
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class EObjectGitStorageServiceIT {

	private static final String GIT_STORAGE_PID = "GitObjectStorage";

	/** Service property used to select this repo's GitServices via a target filter. */
	private static final String GIT_ID = "testrepo";

	private static final String TEST_SCOPE = "git_scope";
	/** Registry the person schema (an EPackage) is routed to by the type-to-registry map. */
	private static final String SCHEMA_REGISTRY = "schema";
	private static final String EPACKAGE_TYPE_URI = "http://www.eclipse.org/emf/2002/Ecore#//EPackage";

	private static final Logger LOG = LoggerFactory.getLogger(EObjectGitStorageServiceIT.class);

	@InjectBundleContext
	BundleContext context;

	@InjectService
	ConfigurationAdmin configAdmin;

	@InjectService
	TypedEventBus eventBus;

	@TempDir
	Path tempDir;

	private GitTestRepository repo;
	private final List<Configuration> gitServiceConfigs = new ArrayList<>();
	private Configuration gitStorage;

	@BeforeEach
	public void before() throws Exception {
		assertNotNull(context, "BundleContext should not be null");
		// RegistryConfiguration templates its workspace folder from this system property.
		System.setProperty("tempDir", tempDir.toString());

		LOG.info("[git-it] @BeforeEach: setting up git test repository + container");
		repo = new GitTestRepository(tempDir);
		repo.start();
		LOG.info("[git-it] @BeforeEach: repository ready at {}", repo.gitUrl());
	}

	@AfterEach
	public void after() throws Exception {
		deleteQuietly(gitStorage);
		for (Configuration cfg : gitServiceConfigs) {
			deleteQuietly(cfg);
		}
		gitServiceConfigs.clear();
		if (repo != null) {
			repo.close();
		}
	}

	@SuppressWarnings("rawtypes")
	@Test
	@RegistryConfiguration
	public void testServiceActivationAndSchemaDerivation(
			@InjectService(cardinality = 0, filter = "(storage.backend=git)") ServiceAware<EObjectStorageService> serviceAware)
			throws Exception {

		EObjectStorageService<EObject> storageService = awaitStorage(serviceAware);
		assertEquals(StorageBackendType.GIT, storageService.getBackendType());
		assertEquals("git", storageService.getStorageType());

		// The person schema (an EPackage) on branch=main must have been derived at
		// construction (schemas parse against the management ResourceSet) and routed to
		// the "schema" registry, with the D9-qualified objectId scope/stage/repoPath.
		List<String> ids = storageService.listObjectIds(TEST_SCOPE, SCHEMA_REGISTRY, GitTestRepository.BRANCH_MAIN)
				.timeout(15000L).getValue();
		LOG.info("[git-it] listObjectIds(main) -> {}", ids);
		assertNotNull(ids);
		assertTrue(ids.contains(id(GitTestRepository.BRANCH_MAIN)),
				"listObjectIds(main) should contain the derived person schema id, was: " + ids);
	}

	/**
	 * The same repo path resolves to <em>different</em> content on each branch (= stage):
	 * {@code main}'s {@code Person} has one attribute, {@code release}'s has two. Proves
	 * the read path ({@code loadEObject} → {@code GitURIHandler} → {@code readFile})
	 * and per-stage isolation of content.
	 */
	@SuppressWarnings("rawtypes")
	@Test
	@RegistryConfiguration
	public void testSchemaReadPerBranch(
			@InjectService(cardinality = 0, filter = "(storage.backend=git)") ServiceAware<EObjectStorageService> serviceAware)
			throws Exception {

		EObjectStorageService<EObject> storageService = awaitStorage(serviceAware);

		EObject mainObj = storageService
				.retrieveObject(TEST_SCOPE, SCHEMA_REGISTRY, GitTestRepository.BRANCH_MAIN,
						id(GitTestRepository.BRANCH_MAIN))
				.timeout(15000L).getValue();
		EObject releaseObj = storageService
				.retrieveObject(TEST_SCOPE, SCHEMA_REGISTRY, GitTestRepository.BRANCH_RELEASE,
						id(GitTestRepository.BRANCH_RELEASE))
				.timeout(15000L).getValue();

		assertTrue(mainObj instanceof EPackage, "main root should be an EPackage");
		assertTrue(releaseObj instanceof EPackage, "release root should be an EPackage");

		EClass mainPerson = (EClass) ((EPackage) mainObj).getEClassifier("Person");
		EClass releasePerson = (EClass) ((EPackage) releaseObj).getEClassifier("Person");
		assertNotNull(mainPerson, "main Person EClass");
		assertNotNull(releasePerson, "release Person EClass");
		assertEquals(GitTestRepository.PERSON_NS_URI, ((EPackage) mainObj).getNsURI());

		assertEquals(1, mainPerson.getEStructuralFeatures().size(), "main Person has only 'name'");
		assertEquals(2, releasePerson.getEStructuralFeatures().size(), "release Person has 'name' + 'email'");
		assertNotNull(releasePerson.getEStructuralFeature("email"), "release Person should add 'email'");
	}

	/**
	 * Metadata is <em>derived</em> from git facts (D1), not read from a stored
	 * {@code .metadata.xmi}: stage = branch, version = commit SHA, objectType = the root
	 * EClass URI, registry = the type-to-registry lookup, plus the D9-qualified objectId.
	 */
	@SuppressWarnings("rawtypes")
	@Test
	@RegistryConfiguration
	public void testDerivedMetadata(
			@InjectService(cardinality = 0, filter = "(storage.backend=git)") ServiceAware<EObjectStorageService> serviceAware)
			throws Exception {

		EObjectStorageService<EObject> storageService = awaitStorage(serviceAware);

		ObjectMetadata md = storageService
				.retrieveMetadata(TEST_SCOPE, SCHEMA_REGISTRY, GitTestRepository.BRANCH_MAIN,
						id(GitTestRepository.BRANCH_MAIN))
				.timeout(15000L).getValue();
		LOG.info("[git-it] derived metadata(main) -> {}", md);
		assertNotNull(md, "derived metadata should be served");
		assertEquals(id(GitTestRepository.BRANCH_MAIN), md.getObjectId());
		assertEquals(TEST_SCOPE, md.getScope());
		assertEquals(SCHEMA_REGISTRY, md.getRegistry());
		assertEquals(GitTestRepository.BRANCH_MAIN, md.getStage());
		assertEquals(EPACKAGE_TYPE_URI, md.getObjectType());
		assertNotNull(md.getVersion(), "version should be the commit SHA");
		assertFalse(md.getVersion().isBlank(), "version (commit SHA) should not be blank");
	}

	/**
	 * D9: the same repo path lives on both branches at once, so its qualified objectId
	 * ({@code scope/stage/repoPath}) must not collide across stages. Each stage lists only
	 * its own id.
	 */
	@SuppressWarnings("rawtypes")
	@Test
	@RegistryConfiguration
	public void testListObjectIdsPerBranchIsolation(
			@InjectService(cardinality = 0, filter = "(storage.backend=git)") ServiceAware<EObjectStorageService> serviceAware)
			throws Exception {

		EObjectStorageService<EObject> storageService = awaitStorage(serviceAware);

		List<String> mainIds = storageService.listObjectIds(TEST_SCOPE, SCHEMA_REGISTRY, GitTestRepository.BRANCH_MAIN)
				.timeout(15000L).getValue();
		List<String> releaseIds = storageService
				.listObjectIds(TEST_SCOPE, SCHEMA_REGISTRY, GitTestRepository.BRANCH_RELEASE).timeout(15000L).getValue();

		assertTrue(mainIds.contains(id(GitTestRepository.BRANCH_MAIN)), "main list should contain main id");
		assertTrue(releaseIds.contains(id(GitTestRepository.BRANCH_RELEASE)), "release list should contain release id");
		assertFalse(mainIds.contains(id(GitTestRepository.BRANCH_RELEASE)),
				"D9: main stage must not leak the release id");
		assertFalse(releaseIds.contains(id(GitTestRepository.BRANCH_MAIN)),
				"D9: release stage must not leak the main id");
	}

	/**
	 * Git storage is read-only: every write path must fail cleanly. The service wraps the
	 * helper's {@code UnsupportedOperationException} in a failed promise.
	 */
	@SuppressWarnings("rawtypes")
	@Test
	@RegistryConfiguration
	public void testWritesRejected(
			@InjectService(cardinality = 0, filter = "(storage.backend=git)") ServiceAware<EObjectStorageService> serviceAware)
			throws Exception {

		EObjectStorageService<EObject> storageService = awaitStorage(serviceAware);

		EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
		pkg.setName("x");
		pkg.setNsURI("http://example.org/x/1.0");
		pkg.setNsPrefix("x");
		ObjectMetadata md = ManagementFactory.eINSTANCE.createObjectMetadata();

		Throwable storeFailure = storageService
				.storeObject(TEST_SCOPE, SCHEMA_REGISTRY, GitTestRepository.BRANCH_MAIN, "x", pkg, md)
				.timeout(15000L).getFailure();
		assertNotNull(storeFailure, "storeObject must be rejected");
		assertTrue(hasCause(storeFailure, UnsupportedOperationException.class),
				"storeObject should fail with UnsupportedOperationException, was: " + storeFailure);

		Throwable deleteFailure = storageService
				.deleteObject(TEST_SCOPE, SCHEMA_REGISTRY, GitTestRepository.BRANCH_MAIN, id(GitTestRepository.BRANCH_MAIN))
				.timeout(15000L).getFailure();
		assertNotNull(deleteFailure, "deleteObject must be rejected");
		assertTrue(hasCause(deleteFailure, UnsupportedOperationException.class),
				"deleteObject should fail with UnsupportedOperationException, was: " + deleteFailure);
	}

	/**
	 * An inbound push (webhook) triggers a reconcile that re-syncs the pushed branch: a
	 * new schema committed to the served repo becomes visible after the {@link WebhookPayload}
	 * is delivered on the branch's topic. Delivering the same push twice is idempotent
	 * (tip-SHA compare), so the new id appears exactly once.
	 */
	@SuppressWarnings("rawtypes")
	@Test
	@RegistryConfiguration
	public void testWebhookTriggersReconcile(
			@InjectService(cardinality = 0, filter = "(storage.backend=git)") ServiceAware<EObjectStorageService> serviceAware)
			throws Exception {

		EObjectStorageService<EObject> storageService = awaitStorage(serviceAware, 0); // webhook-only

		String animalId = TEST_SCOPE + "/" + GitTestRepository.BRANCH_MAIN + "/" + GitTestRepository.ANIMAL_ECORE;
		assertFalse(listIds(storageService, GitTestRepository.BRANCH_MAIN).contains(animalId),
				"animal schema should not exist before the push");

		// External push: add a new schema on main in the served repo.
		repo.commitOnBranch(GitTestRepository.BRANCH_MAIN, GitTestRepository.ANIMAL_ECORE,
				GitTestRepository.ANIMAL_ECORE_CONTENT);

		// Deliver the webhook twice (idempotency) on the branch's topic.
		deliverPush(GitTestRepository.BRANCH_MAIN);
		deliverPush(GitTestRepository.BRANCH_MAIN);

		awaitContains(storageService, GitTestRepository.BRANCH_MAIN, animalId, 30000L);

		long occurrences = listIds(storageService, GitTestRepository.BRANCH_MAIN).stream()
				.filter(animalId::equals).count();
		assertEquals(1, occurrences, "the pushed schema should be listed exactly once (idempotent reconcile)");
	}

	/**
	 * The always-on reconcile poll is the safety net for a missed/undelivered webhook: with
	 * no webhook at all, the poll re-syncs the branch and the pushed schema still appears.
	 */
	@SuppressWarnings("rawtypes")
	@Test
	@RegistryConfiguration
	public void testReconcilePollCatchesMissedWebhook(
			@InjectService(cardinality = 0, filter = "(storage.backend=git)") ServiceAware<EObjectStorageService> serviceAware)
			throws Exception {

		EObjectStorageService<EObject> storageService = awaitStorage(serviceAware, 2); // poll every 2s

		String animalId = TEST_SCOPE + "/" + GitTestRepository.BRANCH_MAIN + "/" + GitTestRepository.ANIMAL_ECORE;

		// External push with NO webhook delivered — only the poll can catch it.
		repo.commitOnBranch(GitTestRepository.BRANCH_MAIN, GitTestRepository.ANIMAL_ECORE,
				GitTestRepository.ANIMAL_ECORE_CONTENT);

		awaitContains(storageService, GitTestRepository.BRANCH_MAIN, animalId, 30000L);
	}

	// --- config helpers -----------------------------------------------------

	/** The D9-qualified objectId of the fixture person schema on {@code branch}. */
	private static String id(String branch) {
		return TEST_SCOPE + "/" + branch + "/" + GitTestRepository.PERSON_ECORE;
	}

	private List<String> listIds(EObjectStorageService<EObject> storageService, String stage) throws Exception {
		return storageService.listObjectIds(TEST_SCOPE, SCHEMA_REGISTRY, stage).timeout(15000L).getValue();
	}

	/** Delivers a synthetic push {@link WebhookPayload} on {@code branch}'s topic. */
	private void deliverPush(String branch) {
		GithubPayload payload = GithubWebhookFactory.eINSTANCE.createGithubPayload();
		payload.setRef("refs/heads/" + branch);
		String topic = WebhookTopics.topicFor(repoFullNameFromCloneUrl(repo.gitUrl()), branch);
		LOG.info("[git-it] delivering webhook on topic {}", topic);
		eventBus.deliver(topic, payload);
	}

	/**
	 * Polls {@code listObjectIds} until {@code expectedId} appears on {@code stage} or the
	 * timeout elapses, so an asynchronous reconcile (webhook or poll) can complete.
	 */
	private void awaitContains(EObjectStorageService<EObject> storageService, String stage, String expectedId,
			long timeoutMs) throws Exception {
		long deadline = System.currentTimeMillis() + timeoutMs;
		List<String> last = List.of();
		while (System.currentTimeMillis() < deadline) {
			last = listIds(storageService, stage);
			if (last.contains(expectedId)) {
				LOG.info("[git-it] reconcile picked up {} on {}", expectedId, stage);
				return;
			}
			Thread.sleep(300);
		}
		fail("Timed out waiting for " + expectedId + " on stage " + stage + "; last listObjectIds=" + last);
	}

	/**
	 * Mirrors {@code EObjectGitStorageService.repoFullNameFromCloneUrl} (package-private in
	 * the backend bundle) so the webhook topic computed here matches the one the backend
	 * subscribed to. For {@code git://host:port/testrepo.git} this yields {@code testrepo}.
	 */
	private static String repoFullNameFromCloneUrl(String cloneUrl) {
		String s = cloneUrl.trim();
		if (s.endsWith(".git")) {
			s = s.substring(0, s.length() - ".git".length());
		}
		int scheme = s.indexOf("://");
		if (scheme >= 0) {
			String rest = s.substring(scheme + 3);
			int slash = rest.indexOf('/');
			s = slash >= 0 ? rest.substring(slash + 1) : rest;
		} else {
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

	private static boolean hasCause(Throwable t, Class<? extends Throwable> type) {
		for (Throwable c = t; c != null; c = c.getCause()) {
			if (type.isInstance(c)) {
				return true;
			}
		}
		return false;
	}

	/** Creates the git backend (poll disabled) against the running container and waits for it. */
	@SuppressWarnings("rawtypes")
	private EObjectStorageService<EObject> awaitStorage(ServiceAware<EObjectStorageService> serviceAware)
			throws Exception {
		return awaitStorage(serviceAware, 0);
	}

	/** Creates the git backend with the given reconcile-poll interval and waits for its service. */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private EObjectStorageService<EObject> awaitStorage(ServiceAware<EObjectStorageService> serviceAware,
			int pollSeconds) throws Exception {
		LOG.info("[git-it] creating GitConfig services + GitObjectStorage configuration (poll={}s)...", pollSeconds);
		createGitBackend(pollSeconds);
		LOG.info("[git-it] waiting for EObjectStorageService(storage.backend=git)...");
		EObjectStorageService<EObject> storageService = (EObjectStorageService<EObject>) serviceAware
				.waitForService(30000L);
		LOG.info("[git-it] waitForService returned: {}", storageService);
		assertNotNull(storageService, "Git storage service should be available");
		// Priming (initial fetch + derivation) runs on the re-derive worker, off the
		// activation thread — wait until the main-branch schema is derived so tests
		// can assert on served content right away.
		awaitContains(storageService, GitTestRepository.BRANCH_MAIN, id(GitTestRepository.BRANCH_MAIN), 15000L);
		return storageService;
	}

	/**
	 * Creates one real {@code org.gecko.jgit.GitServiceImpl} per branch (= stage) via a
	 * {@code GitConfig} factory configuration with {@code id=testrepo} (and no private key,
	 * so the fetch runs anonymously over {@code git://}), then creates the
	 * {@code GitObjectStorage} backend that binds them via
	 * {@code gitservice.target=(id=testrepo)}. The services are awaited first so the
	 * {@code STATIC}/{@code GREEDY} gitservice reference binds them on activation.
	 *
	 * @param pollSeconds reconcile-poll interval; {@code 0} disables the poll (webhook-only)
	 */
	private void createGitBackend(int pollSeconds) throws Exception {
		String gitUrl = repo.gitUrl();

		registerGitService(gitUrl, GitTestRepository.BRANCH_MAIN);
		registerGitService(gitUrl, GitTestRepository.BRANCH_RELEASE);
		awaitGitServices(2);

		gitStorage = configAdmin.getFactoryConfiguration(GIT_STORAGE_PID, "test", "?");
		Dictionary<String, Object> storageProps = new Hashtable<>();
		storageProps.put("scope", TEST_SCOPE);
		storageProps.put("type.registry.map", new String[] { EPACKAGE_TYPE_URI + ":" + SCHEMA_REGISTRY });
		storageProps.put("gitservice.target", "(id=" + GIT_ID + ")");
		storageProps.put("poll.interval.seconds", pollSeconds);
		storageProps.put("storage.type", "git");
		// Bind the shared Lucene registry created by @RegistryConfiguration.
		storageProps.put("registry.target", "(registry=main)");
		gitStorage.update(storageProps);
	}

	private void registerGitService(String gitUrl, String branch) throws Exception {
		Configuration cfg = configAdmin.getFactoryConfiguration("GitConfig", branch, "?");
		Dictionary<String, Object> props = new Hashtable<>();
		props.put("repo", gitUrl);
		props.put("branch", branch);
		// Extra (non-OCD) property published as a service property for the gitservice.target filter.
		props.put("id", GIT_ID);
		// No privateKey: GitServiceImpl then skips the SSH session factory entirely and the
		// fetch runs anonymously over git://.
		cfg.update(props);
		gitServiceConfigs.add(cfg);
		LOG.info("[git-it] created GitConfig~{} url={}", branch, gitUrl);
	}

	/** Waits until {@code count} real GitServiceImpl instances (id=testrepo) are registered. */
	private void awaitGitServices(int count) throws Exception {
		long deadline = System.currentTimeMillis() + 30_000L;
		while (System.currentTimeMillis() < deadline) {
			if (context.getServiceReferences(GitService.class, "(id=" + GIT_ID + ")").size() >= count) {
				return;
			}
			Thread.sleep(100);
		}
		throw new IllegalStateException("expected " + count + " GitService(id=" + GIT_ID + ") services");
	}

	private static void deleteQuietly(Configuration config) {
		if (config != null) {
			try {
				config.delete();
			} catch (Exception ignore) {
				// best-effort teardown
			}
		}
	}
}
