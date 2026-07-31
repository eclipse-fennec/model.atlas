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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EClassifier;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.fennec.model.atlas.mgmt.storage.ModelUnavailableException;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations.EPackageLuceneIndexSetup;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations.RegistryConfiguration;
import org.gecko.jgit.api.GitService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.osgi.util.promise.Promise;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Full-chain OSGi integration tests for the git backend: they stand up the <em>entire</em>
 * registry chain — git storage → {@code EPackageStageActionService} →
 * {@code DynamicEPackageRegistrationService} → {@code RegistryService} → {@code ScopeService}
 * (which builds the per-stage {@code ResourceSet}s) — over the real {@code git://} container
 * ({@link GitTestRepository}), with the production {@code org.gecko.jgit.GitServiceImpl} created
 * per branch via {@code GitConfig} factory configurations (no private key → anonymous
 * {@code git://} fetch). Unlike {@link EObjectGitStorageServiceIT}
 * (which primes the shared cache directly), this exercises the schema (un)registration path
 * end-to-end and thus validates:
 *
 * <ul>
 *   <li><b>Cold-start + change A + G6:</b> a schema present on several branches (= stages)
 *       registers <em>once per stage</em> as an OSGi {@code EPackage} service, each tagged with
 *       its own {@code atlas.stage} and carrying its branch's content.</li>
 *   <li><b>Change D (webhook ENTER):</b> a schema pushed after startup is (re)registered when
 *       the reconcile publishes the {@code RegistryResync} event.</li>
 *   <li><b>D8-3 (EXIT on removal):</b> a schema removed on one branch is unregistered for that
 *       stage while the other branch's copy survives.</li>
 *   <li><b>Instance resolution + D8-3 point 2:</b> an instance resolves against its
 *       dynamically-registered schema; once the schema is removed the read fails with a clean
 *       {@link ModelUnavailableException}.</li>
 * </ul>
 *
 * <p>The chain is created at runtime via {@link ConfigurationAdmin} in a fixed order (git
 * storage first, so it primes the cache before the {@code RegistryService}/{@code ScopeService}
 * replay — the G6 ordering requirement), because the git URL is only known once the container
 * has started.
 *
 * <p><b>Timing:</b> registration flows through ConfigAdmin, DS and the whiteboard
 * asynchronously, so assertions use {@code ServiceAware.waitForService}/polling with generous
 * timeouts rather than fixed sleeps.
 */
@RequireConfigurationAdmin
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class GitRegistryChainIT {

	private static final Logger LOG = LoggerFactory.getLogger(GitRegistryChainIT.class);

	private static final String GIT_ID = "testrepo";
	private static final String SCOPE = "git_scope";
	private static final String SCHEMA_REGISTRY = "schema-git";
	private static final String EPACKAGE_TYPE_URI = "http://www.eclipse.org/emf/2002/Ecore#//EPackage";

	private static final long WAIT = 20000L;

	@InjectBundleContext
	BundleContext context;

	@InjectService
	ConfigurationAdmin configAdmin;

	@TempDir
	Path tempDir;

	private GitTestRepository repo;
	private final List<Configuration> gitServiceConfigs = new ArrayList<>();
	private final List<Configuration> configs = new ArrayList<>();

	@BeforeEach
	public void before() throws Exception {
		assertNotNull(context);
		System.setProperty("tempDir", tempDir.toString());
		LOG.info("[git-chain] starting container");
		repo = new GitTestRepository(tempDir);
		repo.start();
	}

	@AfterEach
	public void after() throws Exception {
		// Delete configs newest-first (ScopeService → RegistryService → stage-action → storage).
		// Deleting ScopeService first triggers RegistryService.deactivate → EXIT replay, which
		// (with replay.on.shutdown=true) unregisters this scope's EPackages.
		for (int i = configs.size() - 1; i >= 0; i--) {
			try {
				configs.get(i).delete();
			} catch (Exception ignore) {
				// best-effort teardown
			}
		}
		configs.clear();
		for (Configuration cfg : gitServiceConfigs) {
			try {
				cfg.delete();
			} catch (Exception ignore) {
				// best-effort teardown
			}
		}
		gitServiceConfigs.clear();
		if (repo != null) {
			repo.close();
		}
		// Wait until this scope's dynamic EPackages are actually gone, so the singleton
		// registration service is clean for the next test method (config deletion is async).
		long deadline = System.currentTimeMillis() + WAIT;
		while (System.currentTimeMillis() < deadline
				&& !context.getServiceReferences(EPackage.class, "(emf.model.scope=" + SCOPE + ")").isEmpty()) {
			Thread.sleep(200);
		}
	}

	// ---------------------------------------------------------------------
	// Cold start — change A + G6
	// ---------------------------------------------------------------------

	/**
	 * The {@code person} schema exists on both {@code main} and {@code release} (with different
	 * content). After the chain starts, it must be registered as an OSGi {@code EPackage}
	 * <em>twice</em> — once per stage — each tagged with its own {@code atlas.stage} and carrying
	 * that branch's content (main: 1 attribute, release: 2). This is the end-to-end proof of the
	 * stage-aware registration fix (change A) plus the cache-priming order (G6).
	 */
	@SuppressWarnings("rawtypes")
	@Test
	@RegistryConfiguration
	@EPackageLuceneIndexSetup
	public void coldStart_schemaRegisteredOncePerBranch(
			@InjectService(cardinality = 0, filter = "(storage.backend=git)") ServiceAware<EObjectStorageService> storageAware,
			@InjectService(cardinality = 0, filter = "(&(emf.name=person)(emf.model.scope=" + SCOPE
					+ ")(atlas.stage=main))") ServiceAware<EPackage> mainPkgAware,
			@InjectService(cardinality = 0, filter = "(&(emf.name=person)(emf.model.scope=" + SCOPE
					+ ")(atlas.stage=release))") ServiceAware<EPackage> releasePkgAware)
			throws Exception {

		startChain(0);

		EPackage mainPkg = mainPkgAware.waitForService(WAIT);
		EPackage releasePkg = releasePkgAware.waitForService(WAIT);
		assertNotNull(mainPkg, "person EPackage should be registered for stage main");
		assertNotNull(releasePkg, "person EPackage should be registered for stage release (same nsURI, other stage)");

		assertEquals(GitTestRepository.PERSON_NS_URI, mainPkg.getNsURI());
		assertEquals(GitTestRepository.PERSON_NS_URI, releasePkg.getNsURI());

		EClass mainPerson = (EClass) mainPkg.getEClassifier("Person");
		EClass releasePerson = (EClass) releasePkg.getEClassifier("Person");
		assertEquals(1, mainPerson.getEStructuralFeatures().size(), "main Person has only 'name'");
		assertEquals(2, releasePerson.getEStructuralFeatures().size(), "release Person adds 'email'");
	}

	// ---------------------------------------------------------------------
	// Webhook ENTER — change D
	// ---------------------------------------------------------------------

	/**
	 * A schema pushed <em>after</em> startup (here {@code animal.ecore} on main) must become a
	 * registered OSGi {@code EPackage} once the reconcile publishes the {@code RegistryResync}
	 * event and the workflow handler replays registration for the scope. Driven by the
	 * always-on reconcile poll (no manual webhook), so it also proves the poll → resync path.
	 */
	@SuppressWarnings("rawtypes")
	@Test
	@RegistryConfiguration
	@EPackageLuceneIndexSetup
	public void pushedSchema_getsRegisteredViaResync(
			@InjectService(cardinality = 0, filter = "(storage.backend=git)") ServiceAware<EObjectStorageService> storageAware,
			@InjectService(cardinality = 0, filter = "(&(emf.name=animal)(emf.model.scope=" + SCOPE
					+ ")(atlas.stage=main))") ServiceAware<EPackage> animalAware)
			throws Exception {

		startChain(2); // 2s reconcile poll picks up the push

		assertTrue(animalAware.isEmpty(), "animal schema must not exist before the push");

		repo.commitOnBranch(GitTestRepository.BRANCH_MAIN, GitTestRepository.ANIMAL_ECORE,
				GitTestRepository.ANIMAL_ECORE_CONTENT);

		EPackage animal = animalAware.waitForService(WAIT);
		assertNotNull(animal, "pushed animal schema should be registered as an OSGi EPackage via the resync");
		assertEquals("http://example.org/animal/1.0", animal.getNsURI());
	}

	// ---------------------------------------------------------------------
	// Removal EXIT — D8-3 point 1 + change A (per-stage)
	// ---------------------------------------------------------------------

	/**
	 * Removing {@code person.ecore} from {@code main} (but not {@code release}) must unregister
	 * the {@code main} EPackage while the {@code release} one survives — the per-stage EXIT
	 * (D8-3 point 1), only possible because registration is keyed per {@code (scope,stage)}
	 * (change A).
	 */
	@SuppressWarnings("rawtypes")
	@Test
	@RegistryConfiguration
	@EPackageLuceneIndexSetup
	public void schemaRemovedOnOneBranch_unregistersOnlyThatStage(
			@InjectService(cardinality = 0, filter = "(storage.backend=git)") ServiceAware<EObjectStorageService> storageAware,
			@InjectService(cardinality = 0, filter = "(&(emf.name=person)(emf.model.scope=" + SCOPE
					+ ")(atlas.stage=main))") ServiceAware<EPackage> mainPkgAware,
			@InjectService(cardinality = 0, filter = "(&(emf.name=person)(emf.model.scope=" + SCOPE
					+ ")(atlas.stage=release))") ServiceAware<EPackage> releasePkgAware)
			throws Exception {

		startChain(2);

		assertNotNull(mainPkgAware.waitForService(WAIT), "person@main registered at start");
		assertNotNull(releasePkgAware.waitForService(WAIT), "person@release registered at start");

		// Remove the schema from main only (replace the file with a non-model file so the tree moves).
		repo.removeOnBranch(GitTestRepository.BRANCH_MAIN, GitTestRepository.PERSON_ECORE);

		// main's EPackage must disappear...
		assertTrue(waitUntilEmpty(mainPkgAware, WAIT), "person@main should be unregistered after removal");
		// ...while release's survives.
		assertNotNull(releasePkgAware.getService(), "person@release must survive the main-branch removal");
	}

	/**
	 * Reproduces the manual-e2e regression (2026-07-22): with the same nsURI on two branches
	 * (= stages), removing the schema on ONE branch made objects of the OTHER branch
	 * unserializable in the REST layer ("[DynamicEObjectImpl] Error serializing outgoing
	 * object", fennec codec) while the storage read of the same object kept returning 200.
	 *
	 * <p>This mirrors the codec's write path exactly: {@code ScopedResourceSetProvider} leases
	 * a <b>fresh prototype per-stage ResourceSet per request</b> from the
	 * {@code ResourceSetCollector}, the writer copies the outgoing EObject into a resource of
	 * that ResourceSet and saves it. So after the release-branch removal this test leases a
	 * fresh (scope, stage=main) ResourceSet — like the next incoming request would — and
	 * asserts it can still resolve the person nsURI and serialize a copy of main's alice.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	@RegistryConfiguration
	@EPackageLuceneIndexSetup
	public void schemaRemovedOnOneBranch_survivingStageStillSerializesViaFreshResourceSet(
			@InjectService(cardinality = 0, filter = "(storage.backend=git)") ServiceAware<EObjectStorageService> storageAware,
			@InjectService(cardinality = 0, filter = "(&(emf.name=person)(emf.model.scope=" + SCOPE
					+ ")(atlas.stage=main))") ServiceAware<EPackage> mainPkgAware,
			@InjectService(cardinality = 0, filter = "(&(emf.name=person)(emf.model.scope=" + SCOPE
					+ ")(atlas.stage=release))") ServiceAware<EPackage> releasePkgAware)
			throws Exception {

		startChain(2);
		EObjectStorageService<EObject> storage = (EObjectStorageService<EObject>) storageAware.waitForService(WAIT);
		assertNotNull(mainPkgAware.waitForService(WAIT), "person@main registered at start");
		assertNotNull(releasePkgAware.waitForService(WAIT), "person@release registered at start");

		String aliceId = SCOPE + "/" + GitTestRepository.BRANCH_MAIN + "/" + GitTestRepository.ALICE_XMI;

		// Precondition (request BEFORE the removal): fresh per-stage RS resolves person and
		// serializes alice — this is the manual step that worked.
		serializeViaFreshStageResourceSet(storage, aliceId, "before the release-branch removal");

		// Remove the schema from release only; wait for its EXIT to settle.
		repo.removeOnBranch(GitTestRepository.BRANCH_RELEASE, GitTestRepository.PERSON_ECORE);
		assertTrue(waitUntilEmpty(releasePkgAware, WAIT), "person@release should be unregistered after removal");
		assertNotNull(mainPkgAware.getService(), "person@main must survive the release-branch removal");

		// THE REGRESSION (request AFTER the removal): the storage read still works, but the
		// codec-equivalent serialization via a freshly-leased main-stage ResourceSet broke.
		serializeViaFreshStageResourceSet(storage, aliceId, "after the release-branch removal");
	}

	/**
	 * Performs the codec's write path against a freshly-leased (scope, stage=main) prototype
	 * ResourceSet: retrieve alice from storage, resolve her nsURI in the leased RS's package
	 * registry, copy her into a new resource of that RS and save it.
	 */
	private void serializeViaFreshStageResourceSet(EObjectStorageService<EObject> storage, String aliceId,
			String phase) throws Exception {
		// Poll instead of a one-shot read: right after chain startup (and around branch
		// removals) the read can transiently return null or throw while the git sync /
		// registration replay is still settling — observed on CI and locally.
		EObject alice = awaitRetrieve(storage, GitTestRepository.BRANCH_MAIN, aliceId, java.util.Objects::nonNull,
				WAIT);
		assertNotNull(alice, "main's alice must be retrievable from storage " + phase);

		java.util.Collection<org.osgi.framework.ServiceReference<org.eclipse.emf.ecore.resource.ResourceSet>> refs = context
				.getServiceReferences(org.eclipse.emf.ecore.resource.ResourceSet.class,
						"(&(scope.name=" + SCOPE + ")(stage.name=" + GitTestRepository.BRANCH_MAIN + "))");
		assertFalse(refs.isEmpty(), "per-stage (main) ResourceSet service must be present " + phase);
		org.osgi.framework.ServiceObjects<org.eclipse.emf.ecore.resource.ResourceSet> so = context
				.getServiceObjects(refs.iterator().next());
		org.eclipse.emf.ecore.resource.ResourceSet rs = so.getService();
		try {
			assertNotNull(rs.getPackageRegistry().getEPackage(GitTestRepository.PERSON_NS_URI),
					"person nsURI must resolve in a freshly-leased main-stage ResourceSet " + phase);
			org.eclipse.emf.ecore.resource.Resource out = rs
					.createResource(org.eclipse.emf.common.util.URI.createURI("http://test.test/alice.xmi"));
			assertNotNull(out, "the leased ResourceSet must be able to create a resource " + phase);
			out.getContents().add(org.eclipse.emf.ecore.util.EcoreUtil.copy(alice));
			java.io.ByteArrayOutputStream bout = new java.io.ByteArrayOutputStream();
			out.save(bout, java.util.Map.of());
			assertTrue(bout.size() > 0, "serialized alice must not be empty " + phase);
			rs.getResources().remove(out);
		} finally {
			so.ungetService(rs);
		}
	}

	// ---------------------------------------------------------------------
	// Instance resolution + D8-3 point 2 (model unavailable)
	// ---------------------------------------------------------------------

	/**
	 * An instance ({@code alice.xmi}) resolves to a typed {@code Person} while its schema is
	 * registered; once {@code person.ecore} is removed from the branch the schema is
	 * unregistered and a read of the still-present instance fails with a clean
	 * {@link ModelUnavailableException} (D8-3 point 2) rather than an opaque error.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	@RegistryConfiguration
	@EPackageLuceneIndexSetup
	public void instanceResolves_thenModelUnavailableAfterSchemaRemoval(
			@InjectService(cardinality = 0, filter = "(storage.backend=git)") ServiceAware<EObjectStorageService> storageAware,
			@InjectService(cardinality = 0, filter = "(&(emf.name=person)(emf.model.scope=" + SCOPE
					+ ")(atlas.stage=main))") ServiceAware<EPackage> mainPkgAware,
			@InjectService(cardinality = 0, filter = "(&(scope.name=" + SCOPE
					+ ")(stage.name=main))") ServiceAware<org.eclipse.emf.ecore.resource.ResourceSet> stageRsAware)
			throws Exception {

		startChain(2);
		EObjectStorageService<EObject> storage = (EObjectStorageService<EObject>) storageAware.waitForService(WAIT);
		assertNotNull(mainPkgAware.waitForService(WAIT), "person@main must be registered so the instance resolves");
		// The per-stage ResourceSet (built by SchemaRegistryChainConfigurator) is what carries the
		// dynamically-registered person package; the instance can only resolve once it is up.
		assertNotNull(stageRsAware.waitForService(WAIT),
				"per-stage ResourceSet (scope.name=" + SCOPE + ", stage.name=main) must be published by the chain");

		String aliceId = SCOPE + "/" + GitTestRepository.BRANCH_MAIN + "/" + GitTestRepository.ALICE_XMI;

		// While the schema is present, the instance resolves to a typed Person. Poll: right
		// after chain startup the read can transiently miss while the sync settles.
		EObject alice = awaitRetrieve(storage, GitTestRepository.BRANCH_MAIN, aliceId, java.util.Objects::nonNull,
				WAIT);
		assertNotNull(alice, "alice instance should resolve while person is registered");
		assertEquals("Person", alice.eClass().getName());
		assertEquals("Alice", alice.eGet(alice.eClass().getEStructuralFeature("name")));

		// Remove the schema; wait for its EPackage to be unregistered.
		repo.removeOnBranch(GitTestRepository.BRANCH_MAIN, GitTestRepository.PERSON_ECORE);
		assertTrue(waitUntilEmpty(mainPkgAware, WAIT), "person@main should be unregistered after removal");

		// The instance file still exists, but its model is gone -> ModelUnavailableException.
		Promise<EObject> read = storage.retrieveObject(SCOPE, SCHEMA_REGISTRY, GitTestRepository.BRANCH_MAIN, aliceId)
				.timeout(WAIT);
		Throwable failure = read.getFailure();
		assertNotNull(failure, "reading an instance whose model was removed must fail");
		assertTrue(hasCause(failure, ModelUnavailableException.class),
				"the failure should be a ModelUnavailableException, was: " + failure);
	}

	// ---------------------------------------------------------------------
	// Referential integrity across a RELOAD (D8 — the boss's explicit asks)
	// ---------------------------------------------------------------------

	/**
	 * An instance's {@code eClass()} must still resolve after its backing model is
	 * <em>reloaded</em> (changed content, same nsURI, new commit) — and reflect the new model.
	 * {@code person} on main starts with only {@code name}; after {@code person.ecore} is
	 * re-pushed with an added {@code email}, a fresh read of {@code alice} resolves to the
	 * reloaded {@code Person} (which now has {@code email}). This is the git end-to-end lift of
	 * the G0.5 "fresh read re-parses against the latest registered EPackage" characterization.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	@RegistryConfiguration
	@EPackageLuceneIndexSetup
	public void instanceEClassResolvesAfterModelReload(
			@InjectService(cardinality = 0, filter = "(storage.backend=git)") ServiceAware<EObjectStorageService> storageAware,
			@InjectService(cardinality = 0, filter = "(&(emf.name=person)(emf.model.scope=" + SCOPE
					+ ")(atlas.stage=main))") ServiceAware<EPackage> mainPkgAware,
			@InjectService(cardinality = 0, filter = "(&(scope.name=" + SCOPE
					+ ")(stage.name=main))") ServiceAware<org.eclipse.emf.ecore.resource.ResourceSet> stageRsAware)
			throws Exception {

		startChain(2);
		EObjectStorageService<EObject> storage = (EObjectStorageService<EObject>) storageAware.waitForService(WAIT);
		assertNotNull(mainPkgAware.waitForService(WAIT), "person@main registered at start");
		assertNotNull(stageRsAware.waitForService(WAIT), "per-stage ResourceSet must be up");

		String aliceId = SCOPE + "/" + GitTestRepository.BRANCH_MAIN + "/" + GitTestRepository.ALICE_XMI;

		// Before reload: alice resolves to a Person that has only 'name'.
		// Poll: right after chain startup the read can transiently miss while the sync settles.
		EObject before = awaitRetrieve(storage, GitTestRepository.BRANCH_MAIN, aliceId, java.util.Objects::nonNull,
				WAIT);
		assertEquals("Person", before.eClass().getName());
		assertFalse(before.eClass().eIsProxy(), "eClass must resolve before reload");
		assertNull(before.eClass().getEStructuralFeature("email"), "no email attribute before reload");

		// Reload: push person.ecore with an added 'email' attribute (same nsURI, new commit).
		repo.commitOnBranch(GitTestRepository.BRANCH_MAIN, GitTestRepository.PERSON_ECORE,
				GitTestRepository.PERSON_ECORE_WITH_EMAIL);

		// After reload: a fresh read of alice must still resolve its eClass, now reflecting the
		// reloaded model (email present). Poll, tolerating the brief reload window.
		EObject after = awaitRetrieve(storage, GitTestRepository.BRANCH_MAIN, aliceId,
				o -> o != null && !o.eClass().eIsProxy() && o.eClass().getEStructuralFeature("email") != null, WAIT);
		assertNotNull(after, "alice must resolve after the model reload");
		assertEquals("Person", after.eClass().getName());
		assertNotNull(after.eClass().getEStructuralFeature("name"), "reloaded Person keeps 'name'");
		assertNotNull(after.eClass().getEStructuralFeature("email"), "reloaded Person gained 'email'");
	}

	/**
	 * Cross-ecore referential integrity across a reload: {@code product.ecore} has a
	 * {@code category} reference whose type is {@code Category} in a <em>separate</em>
	 * {@code category.ecore}. After {@code category.ecore} is reloaded (an extra {@code code}
	 * attribute, same nsURI), a fresh read of {@code product} must still resolve the cross-ecore
	 * reference to {@code Category} — now reflecting the reloaded content. This is the git
	 * end-to-end lift of the G0.5 "fresh read of A re-links to the reloaded B" characterization.
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Test
	@RegistryConfiguration
	@EPackageLuceneIndexSetup
	public void crossEcoreReferenceResolvesAfterReferencedModelReload(
			@InjectService(cardinality = 0, filter = "(storage.backend=git)") ServiceAware<EObjectStorageService> storageAware,
			@InjectService(cardinality = 0, filter = "(&(emf.name=category)(emf.model.scope=" + SCOPE
					+ ")(atlas.stage=main))") ServiceAware<EPackage> categoryAware,
			@InjectService(cardinality = 0, filter = "(&(emf.name=product)(emf.model.scope=" + SCOPE
					+ ")(atlas.stage=main))") ServiceAware<EPackage> productAware)
			throws Exception {

		startChain(2);
		EObjectStorageService<EObject> storage = (EObjectStorageService<EObject>) storageAware.waitForService(WAIT);

		// Push the two mutually-referencing schemas onto main.
		repo.commitOnBranch(GitTestRepository.BRANCH_MAIN, GitTestRepository.CATEGORY_ECORE,
				GitTestRepository.CATEGORY_ECORE_CONTENT);
		repo.commitOnBranch(GitTestRepository.BRANCH_MAIN, GitTestRepository.PRODUCT_ECORE,
				GitTestRepository.PRODUCT_ECORE_CONTENT);

		assertNotNull(categoryAware.waitForService(WAIT), "category schema should register");
		assertNotNull(productAware.waitForService(WAIT), "product schema should register");

		String productId = SCOPE + "/" + GitTestRepository.BRANCH_MAIN + "/" + GitTestRepository.PRODUCT_ECORE;

		// Before reload: product.category resolves to Category (no 'code' yet).
		EClassifier catType = awaitProductCategoryType(storage, productId,
				t -> t != null && !t.eIsProxy() && "Category".equals(t.getName()), WAIT);
		assertNotNull(catType, "product.category must resolve to Category before reload");
		assertNull(((EClass) catType).getEStructuralFeature("code"), "Category has no 'code' before reload");

		// Reload the REFERENCED schema (category) with an added 'code' attribute.
		repo.commitOnBranch(GitTestRepository.BRANCH_MAIN, GitTestRepository.CATEGORY_ECORE,
				GitTestRepository.CATEGORY_ECORE_CONTENT_V2);

		// After reload: a fresh read of product must still resolve the cross-ecore reference to
		// Category, now reflecting the reloaded content ('code' present).
		EClassifier reloaded = awaitProductCategoryType(storage, productId,
				t -> t != null && !t.eIsProxy() && t instanceof EClass ec && ec.getEStructuralFeature("code") != null,
				WAIT);
		assertNotNull(reloaded, "product.category must still resolve to the reloaded Category");
		assertEquals("Category", reloaded.getName());
		assertNotNull(((EClass) reloaded).getEStructuralFeature("code"),
				"the reloaded Category (via the cross-ecore reference) must show its new 'code' attribute");
	}

	// ---------------------------------------------------------------------
	// chain wiring
	// ---------------------------------------------------------------------

	/**
	 * Creates the real {@code GitServiceImpl}s (via {@code GitConfig} factory configurations)
	 * and the chain configs in G6-safe order: git storage (primes the cache) →
	 * EPackageStageActionService → RegistryService → ScopeService (whose bind triggers the
	 * per-stage registries + the cold-start replay).
	 */
	private void startChain(int pollSeconds) throws Exception {
		String gitUrl = repo.gitUrl();
		registerGitService(gitUrl, GitTestRepository.BRANCH_MAIN);
		registerGitService(gitUrl, GitTestRepository.BRANCH_RELEASE);
		awaitGitServices(2);

		// 1) git storage — bring it up first so its ctor primes schemas into the shared cache.
		Configuration storage = configAdmin.getFactoryConfiguration("GitObjectStorage", "chain", "?");
		Dictionary<String, Object> sp = new Hashtable<>();
		sp.put("scope", SCOPE);
		sp.put("type.registry.map", new String[] { EPACKAGE_TYPE_URI + ":" + SCHEMA_REGISTRY });
		sp.put("gitservice.target", "(id=" + GIT_ID + ")");
		sp.put("poll.interval.seconds", pollSeconds);
		sp.put("storage.type", "git");
		sp.put("registry.target", "(registry=main)");
		storage.update(sp);
		configs.add(storage);
		awaitStorage();

		// 2) EPackageStageActionService bound to git storage, triggering on the git branches.
		Configuration sas = configAdmin.getFactoryConfiguration("EPackageStageActionService", "chain", "?");
		Dictionary<String, Object> sasp = new Hashtable<>();
		sasp.put("storageService.target", "(storage.type=git)");
		sasp.put("trigger.stages", new String[] { GitTestRepository.BRANCH_MAIN, GitTestRepository.BRANCH_RELEASE });
		sasp.put("replay.on.startup", true);
		// Replay EXIT on shutdown so tearing down the chain unregisters this scope's EPackages,
		// keeping test methods isolated (the DynamicEPackageRegistrationService is a singleton
		// that outlives per-test components).
		sasp.put("replay.on.shutdown", true);
		sas.update(sasp);
		configs.add(sas);

		// 3) RegistryService (SCHEMA) over git storage.
		Configuration reg = configAdmin.getFactoryConfiguration("RegistryService", "chain", "?");
		Dictionary<String, Object> rp = new Hashtable<>();
		rp.put("registry.name", SCHEMA_REGISTRY);
		rp.put("registry.type", "SCHEMA");
		rp.put("stage.storage.mappings", new String[] { GitTestRepository.BRANCH_MAIN + ":git",
				GitTestRepository.BRANCH_RELEASE + ":git" });
		rp.put("workflow.transitions", new String[] { GitTestRepository.BRANCH_MAIN + ":" + GitTestRepository.BRANCH_RELEASE });
		rp.put("storageService.target", "(storage.type=git)");
		rp.put("stageActionService.target", "(component.name=EPackageStageActionService)");
		rp.put("registryService.target", "(registry=main)");
		rp.put("schema.uri", "http://www.eclipse.org/emf/2002/Ecore");
		rp.put("root.eclass.uri", EPACKAGE_TYPE_URI);
		rp.put("resourceSet.target", "(emf.name=ecore)");
		// RegistryService requires exactly one final stage; release is terminal (main -> release).
		rp.put("stages", new String[] {
				"{ \"name\" : \"" + GitTestRepository.BRANCH_MAIN + "\", \"writable\" : false, \"final\": false}",
				"{ \"name\" : \"" + GitTestRepository.BRANCH_RELEASE + "\", \"writable\" : false, \"final\": true}" });
		reg.update(rp);
		configs.add(reg);

		// 4) ScopeService — its bind builds the per-stage registries and replays registration.
		Configuration scope = configAdmin.getFactoryConfiguration("ScopeService", "chain", "?");
		Dictionary<String, Object> scp = new Hashtable<>();
		scp.put("atlas.scope", SCOPE);
		scp.put("scope.name", SCOPE);
		scp.put("registryService.target", "(registry.name=" + SCHEMA_REGISTRY + ")");
		scp.put("registryService.cardinality.minimum", 1);
		scope.update(scp);
		configs.add(scope);

		LOG.info("[git-chain] chain configured for scope {} (poll={}s)", SCOPE, pollSeconds);
	}

	private void awaitStorage() throws Exception {
		long deadline = System.currentTimeMillis() + WAIT;
		while (System.currentTimeMillis() < deadline) {
			if (!context.getServiceReferences(EObjectStorageService.class, "(storage.backend=git)").isEmpty()) {
				LOG.info("[git-chain] git storage service is up");
				return;
			}
			Thread.sleep(200);
		}
		throw new IllegalStateException("git storage service did not come up");
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
	}

	/** Waits until {@code count} real GitServiceImpl instances (id=testrepo) are registered. */
	private void awaitGitServices(int count) throws Exception {
		long deadline = System.currentTimeMillis() + WAIT;
		while (System.currentTimeMillis() < deadline) {
			if (context.getServiceReferences(GitService.class, "(id=" + GIT_ID + ")").size() >= count) {
				return;
			}
			Thread.sleep(100);
		}
		throw new IllegalStateException("expected " + count + " GitService(id=" + GIT_ID + ") services");
	}

	private static boolean waitUntilEmpty(ServiceAware<EPackage> aware, long timeoutMs) throws InterruptedException {
		long deadline = System.currentTimeMillis() + timeoutMs;
		while (System.currentTimeMillis() < deadline) {
			if (aware.isEmpty()) {
				return true;
			}
			Thread.sleep(200);
		}
		return aware.isEmpty();
	}

	/**
	 * Polls {@code retrieveObject} until the returned object satisfies {@code until} (tolerating
	 * transient reload-window failures), or the timeout elapses. Returns the object, or fails.
	 */
	private EObject awaitRetrieve(EObjectStorageService<EObject> storage, String stage, String objectId,
			java.util.function.Predicate<EObject> until, long timeoutMs) throws Exception {
		long deadline = System.currentTimeMillis() + timeoutMs;
		EObject last = null;
		while (System.currentTimeMillis() < deadline) {
			try {
				last = storage.retrieveObject(SCOPE, SCHEMA_REGISTRY, stage, objectId).timeout(5000L).getValue();
				if (until.test(last)) {
					return last;
				}
			} catch (Exception transientDuringReload) {
				// e.g. ModelUnavailableException during the unregister→register window; keep polling.
			}
			Thread.sleep(300);
		}
		fail("Timed out waiting for " + objectId + " to satisfy the condition; last=" + last);
		return null;
	}

	/**
	 * Polls a fresh read of the {@code product} schema, returning the resolved type of its
	 * {@code Product.category} cross-ecore reference once it satisfies {@code until}.
	 */
	private EClassifier awaitProductCategoryType(EObjectStorageService<EObject> storage, String productId,
			java.util.function.Predicate<EClassifier> until, long timeoutMs) throws Exception {
		long deadline = System.currentTimeMillis() + timeoutMs;
		EClassifier last = null;
		while (System.currentTimeMillis() < deadline) {
			try {
				EObject productPkg = storage.retrieveObject(SCOPE, SCHEMA_REGISTRY, GitTestRepository.BRANCH_MAIN,
						productId).timeout(5000L).getValue();
				if (productPkg instanceof EPackage pkg) {
					EClass productClass = (EClass) pkg.getEClassifier("Product");
					if (productClass != null) {
						EReference categoryRef = (EReference) productClass.getEStructuralFeature("category");
						if (categoryRef != null) {
							last = categoryRef.getEType();
							if (until.test(last)) {
								return last;
							}
						}
					}
				}
			} catch (Exception transientDuringReload) {
				// keep polling across the reload window
			}
			Thread.sleep(300);
		}
		fail("Timed out waiting for product.category type to satisfy the condition; last=" + last);
		return null;
	}

	private static boolean hasCause(Throwable t, Class<? extends Throwable> type) {
		for (Throwable c = t; c != null; c = c.getCause()) {
			if (type.isInstance(c)) {
				return true;
			}
		}
		return false;
	}
}
