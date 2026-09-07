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
package org.eclipse.fennec.model.atlas.rest.client.osgi.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.xmi.PackageNotFoundException;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.emf.osgi.ResourceSetFactory;
import org.eclipse.fennec.emf.osgi.configurator.ResourceSetConfigurator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.framework.BundleContext;
import org.osgi.framework.wiring.BundleRevision;
import org.osgi.framework.Bundle;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * P3-12 — OSGi integration tests for the {@code rest.client.osgi} front-end against a
 * live Atlas (the locally-built {@code jena} image, Testcontainers), running inside an
 * OSGi framework. The factory component is driven through ConfigAdmin (the
 * {@link #PID factory PID}); the assertions are on the OSGi <em>side effects</em> the
 * component never exposes itself as a service:
 * <ul>
 * <li><b>EAGER</b> — published {@code EPackage} services carrying {@code atlas.remote=true}
 * appear after activation;</li>
 * <li><b>ResourceSetConfigurator</b> — registered and observable via service inspection;</li>
 * <li><b>LAZY</b> — a framework {@code ResourceSet} resolves an unknown nsURI through the
 * Atlas fallback.</li>
 * </ul>
 * Skipped automatically when Docker or the local {@code jena-snapshot} image is absent
 * (so a normal build stays green); build the image and run {@code testOSGi} locally to
 * exercise it. Mirrors the container setup of the Phase-2 {@code JenaAtlasClientIT}.
 * <p>
 * Drift after a server mutation is covered live: the removal test uploads its own
 * schema into jena's writable {@code release} stage, deletes it over REST and asserts
 * the drift watcher revokes the published package and content of that package stops
 * deserializing.
 * <p>
 * Deferred (need a local-bundle setup, like the Phase-2 live-scenario deferrals):
 * {@code force.remote} superseding a <em>local</em> bundle on startup, and HYBRID's
 * per-nsURI list against image-provided models (the bare jena image exposes no fixed
 * nsURI to pin). These are best driven against a seeded server.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class AtlasClientOsgiIT {

	/** Factory PID of the OSGi Atlas client component (see {@code AtlasClientComponent.PID}). */
	private static final String PID = "org.eclipse.fennec.model.atlas.rest.client";

	private static final String IMAGE = "eclipsefennec/model.atlas:jena-snapshot";
	private static final int HTTP_PORT = 8080;
	private static final String JENA_SCOPE = "jena";
	/** jena's schema registry final stage is {@code release}, not the client default {@code released}. */
	private static final String JENA_VIEW = "release";
	private static final String CONFIG_LOAD_DIR = "/opt/modelatlas/runtime/load";
	private static final String STORAGE_ROOT = "/opt/modelatlas/runtime/data";

	private static final long SERVICE_WAIT_MS = 10_000L;

	// ---- drift-removal fixture ---------------------------------------------

	/** nsURI of the schema the drift test uploads and deletes; version derives from the URI. */
	private static final String DRIFT_NS = "http://atlas.example/test/driftremoval/1.0";
	private static final long DRIFT_INTERVAL_MS = 250L;

	/**
	 * Minimal schema: EClass {@code Thing} with a String attribute {@code name}. No
	 * {@code xsi:schemaLocation} — the codec reader rejects relative locations with
	 * fewer than 3 segments (fennec-codec {@code XMLURIHandler}).
	 */
	private static final String DRIFT_ECORE = """
			<?xml version="1.0" encoding="UTF-8"?>
			<ecore:EPackage xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI"
			    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			    xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore"
			    name="driftremoval" nsURI="%s" nsPrefix="dr">
			  <eClassifiers xsi:type="ecore:EClass" name="Thing">
			    <eStructuralFeatures xsi:type="ecore:EAttribute" name="name"
			        eType="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString"/>
			  </eClassifiers>
			</ecore:EPackage>
			""".formatted(DRIFT_NS);

	/** An instance document of that schema, deserialized before and after the removal. */
	private static final String DRIFT_PAYLOAD = """
			<?xml version="1.0" encoding="UTF-8"?>
			<dr:Thing xmlns:dr="%s" name="hello"/>
			""".formatted(DRIFT_NS);

	// ---- drift-discovery fixture (issue #228) ------------------------------

	/** nsURI of the schema the discovery test publishes only AFTER the client is up. */
	private static final String DISCOVERY_NS = "http://atlas.example/test/driftdiscovery/1.0";

	private static final String DISCOVERY_ECORE = """
			<?xml version="1.0" encoding="UTF-8"?>
			<ecore:EPackage xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI"
			    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
			    xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore"
			    name="driftdiscovery" nsURI="%s" nsPrefix="dd">
			  <eClassifiers xsi:type="ecore:EClass" name="Thing">
			    <eStructuralFeatures xsi:type="ecore:EAttribute" name="name"
			        eType="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString"/>
			  </eClassifiers>
			</ecore:EPackage>
			""".formatted(DISCOVERY_NS);

	private static final String DISCOVERY_PAYLOAD = """
			<?xml version="1.0" encoding="UTF-8"?>
			<dd:Thing xmlns:dd="%s" name="world"/>
			""".formatted(DISCOVERY_NS);

	private static GenericContainer<?> atlas;
	private static URI baseUri;

	@BeforeAll
	static void startAtlas() {
		assumeTrue(DockerClientFactory.instance().isDockerAvailable(),
				"Docker not available — skipping OSGi live-Atlas integration tests");
		assumeTrue(atlasImageAvailableLocally(),
				"Image " + IMAGE + " not present locally — skipping (build it to run this IT)");
		atlas = new GenericContainer<>(IMAGE).withExposedPorts(HTTP_PORT)
				.withEnv("STORAGE_ROOT", STORAGE_ROOT)
				.withEnv("ATLAS_HTTP_PORT", String.valueOf(HTTP_PORT))
				.withFileSystemBind(resolveConfigsDir(), CONFIG_LOAD_DIR, BindMode.READ_ONLY)
				.waitingFor(Wait.forHttp("/atlas/rest/scopes").forPort(HTTP_PORT).forStatusCode(200)
						.forResponsePredicate(body -> body.contains(JENA_SCOPE)))
				.withStartupTimeout(Duration.ofMinutes(2));
		atlas.start();
		baseUri = URI.create("http://" + atlas.getHost() + ":" + atlas.getMappedPort(HTTP_PORT) + "/atlas/rest");
	}

	@AfterAll
	static void stopAtlas() {
		if (atlas != null) {
			atlas.stop();
			atlas.close();
		}
	}

	// ---- tests ------------------------------------------------------------

	@Test
	public void eagerMode_publishesRemoteEPackageServices(
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PID, name = "eager",
					location = "?")) Configuration configuration,
			@InjectService(cardinality = 0, filter = "(atlas.remote=true)") ServiceAware<EPackage> remotePackages)
			throws Exception {
		assumeFalse(releasedNsUris().isEmpty(), "jena scope has no released packages to publish");

		Hashtable<String, Object> props = baseProps("EAGER");
		props.put("eager.scopes", new String[] { JENA_SCOPE });
		configuration.update(props);

		EPackage published = remotePackages.waitForService(SERVICE_WAIT_MS);
		assertNotNull(published, "EAGER activation should publish at least one remote EPackage service");

		// F7: client-side publications carry the locally computed content identity,
		// same property name as on the server (emf.fingerprint, fp1: scheme).
		Object fingerprint = remotePackages.getServiceReference().getProperty("emf.fingerprint");
		assertNotNull(fingerprint, "published remote EPackage must carry the emf.fingerprint property");
		assertTrue(fingerprint.toString().startsWith("fp1:"),
				"fingerprint should use the current scheme tag, was: " + fingerprint);
	}

	@Test
	public void resourceSetConfigurator_isRegisteredAndObservable(
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PID, name = "rsc",
					location = "?")) Configuration configuration,
			@InjectService(cardinality = 0,
					filter = "(atlas.remote=true)") ServiceAware<ResourceSetConfigurator> configurators)
			throws Exception {
		configuration.update(baseProps("LAZY")); // resource.set.fallback defaults to true

		ResourceSetConfigurator configurator = configurators.waitForService(SERVICE_WAIT_MS);
		assertNotNull(configurator, "the Atlas ResourceSetConfigurator should be registered and observable");
	}

	@Test
	public void lazyMode_frameworkResourceSetResolvesUnknownNsUriViaAtlas(
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PID, name = "lazy",
					location = "?")) Configuration configuration,
			@InjectService(cardinality = 0,
					filter = "(atlas.remote=true)") ServiceAware<ResourceSetConfigurator> configurators,
			@InjectService ServiceAware<ResourceSetFactory> resourceSetFactories) throws Exception {
		List<String> nsUris = releasedNsUris();
		assumeFalse(nsUris.isEmpty(), "jena scope has no released packages to resolve");
		String nsUri = nsUris.get(0);

		// A configurator left over from the previous test's still-deactivating component
		// would wrap the ResourceSet with a CLOSED client, whose fallback returns null
		// silently (see AtlasResourceSetConfigurator) — wait until all are gone so the
		// waitForService below can only observe the component this test configures.
		assertTrue(awaitEmpty(configurators),
				"configurators of earlier tests' components should be unregistered before this test starts");

		Hashtable<String, Object> props = baseProps("LAZY");
		// The lazy chain (metadata list + content fetch + publish-wait) must fit in this
		// window; the 5 s component default flakes when a full build loads the machine.
		props.put("lazy.resolve.timeout.ms", (int) SERVICE_WAIT_MS * 3);
		configuration.update(props);
		// The configurator must be registered before we ask the factory for a (wrapped) ResourceSet.
		assertNotNull(configurators.waitForService(SERVICE_WAIT_MS),
				"the Atlas ResourceSetConfigurator must be registered first");

		ResourceSetFactory factory = resourceSetFactories.waitForService(SERVICE_WAIT_MS);
		assertNotNull(factory, "a framework ResourceSetFactory must be present");

		// The configurator service being registered does not mean the ResourceSetFactory has
		// bound it yet — registration and factory binding are independent asynchronous
		// whiteboards, and createResourceSet applies only already-bound configurators. An
		// unwrapped ResourceSet answers null instantly, so re-create until the Atlas wrap is
		// effective; a genuine resolution failure still fails when the deadline expires.
		EPackage resolved = null;
		long deadline = System.currentTimeMillis() + SERVICE_WAIT_MS;
		while (resolved == null) {
			resolved = factory.createResourceSet().getPackageRegistry().getEPackage(nsUri);
			if (resolved == null) {
				if (System.currentTimeMillis() >= deadline) {
					break;
				}
				Thread.sleep(50L);
			}
		}
		assertNotNull(resolved, () -> nsUri + " should resolve through the Atlas-aware ResourceSet");
		assertEquals(nsUri, resolved.getNsURI());
	}

	@Test
	public void hybridMode_prefetchesOnlyTheListedNsUris(
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PID, name = "hybrid",
					location = "?")) Configuration configuration,
			@InjectService(cardinality = 0, filter = "(atlas.remote=true)") ServiceAware<EPackage> remotePackages)
			throws Exception {
		List<String> nsUris = releasedNsUris();
		assumeFalse(nsUris.isEmpty(), "jena scope has no released packages to pin");
		String pinned = nsUris.get(0);

		// HYBRID pre-fetches exactly the nsURIs in eager.nsuri.allow.list at activation; the
		// rest is left to the LAZY registry. Nothing in this test queries the registry, so the
		// only package that should ever be published is the pinned one.
		Hashtable<String, Object> props = baseProps("HYBRID");
		props.put("eager.nsuri.allow.list", new String[] { pinned });
		configuration.update(props);

		EPackage published = remotePackages.waitForService(SERVICE_WAIT_MS);
		assertNotNull(published, "HYBRID activation should pre-fetch and publish the pinned nsURI");

		Set<String> publishedNsUris = remotePackages.getServices().stream().map(EPackage::getNsURI)
				.collect(Collectors.toSet());
		assertEquals(Set.of(pinned), publishedNsUris, "HYBRID must publish only the listed nsURI, nothing else");
	}

	@Test
	public void resourceSetFallbackDisabled_doesNotRegisterConfigurator(
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PID, name = "nofallback",
					location = "?")) Configuration configuration,
			@InjectService(cardinality = 0, filter = "(atlas.remote=true)") ServiceAware<EPackage> remotePackages,
			@InjectService(cardinality = 0,
					filter = "(atlas.remote=true)") ServiceAware<ResourceSetConfigurator> configurators)
			throws Exception {
		assumeFalse(releasedNsUris().isEmpty(), "jena scope has no released packages to publish");

		// EAGER gives an unambiguous activation signal (a published EPackage); the component
		// registers the ResourceSetConfigurator BEFORE the eager prefetch, so once a package is
		// visible the fallback decision has already been made. With resource.set.fallback=false
		// no Atlas ResourceSetConfigurator must have been registered.
		Hashtable<String, Object> props = baseProps("EAGER");
		props.put("eager.scopes", new String[] { JENA_SCOPE });
		props.put("resource.set.fallback", Boolean.FALSE);
		configuration.update(props);

		assertNotNull(remotePackages.waitForService(SERVICE_WAIT_MS),
				"EAGER activation should publish at least one remote EPackage (the activation signal)");
		assertEquals(0, configurators.size(),
				"no Atlas ResourceSetConfigurator should be registered when resource.set.fallback=false");
	}

	@Test
	public void deactivation_revokesPublishedServicesAndConfigurator(
			@InjectService ConfigurationAdmin configAdmin,
			@InjectService(cardinality = 0, filter = "(atlas.remote=true)") ServiceAware<EPackage> remotePackages,
			@InjectService(cardinality = 0,
					filter = "(atlas.remote=true)") ServiceAware<ResourceSetConfigurator> configurators)
			throws Exception {
		assumeFalse(releasedNsUris().isEmpty(), "jena scope has no released packages to publish");

		// Drive the lifecycle directly so we control the teardown moment: ConfigurationPolicy.REQUIRE
		// + no @Modified means deleting the configuration cleanly deactivates the component, which
		// must unpublish every remote EPackage trio and unregister the ResourceSetConfigurator.
		Configuration configuration = configAdmin.createFactoryConfiguration(PID, "?");
		try {
			Hashtable<String, Object> props = baseProps("EAGER");
			props.put("eager.scopes", new String[] { JENA_SCOPE });
			configuration.update(props);

			assertNotNull(remotePackages.waitForService(SERVICE_WAIT_MS),
					"EAGER activation should publish at least one remote EPackage");
			assertNotNull(configurators.waitForService(SERVICE_WAIT_MS),
					"activation should register the Atlas ResourceSetConfigurator");
		} finally {
			configuration.delete();
		}

		assertTrue(awaitEmpty(remotePackages),
				"published remote EPackage services should be revoked on deactivation");
		assertTrue(awaitEmpty(configurators),
				"the Atlas ResourceSetConfigurator should be unregistered on deactivation");
	}

	@Test
	public void driftDetectedRemoval_revokesThePackage_andBlocksDeserialization(
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PID, name = "drift",
					location = "?")) Configuration configuration,
			@InjectService(cardinality = 0,
					filter = "(&(atlas.remote=true)(emf.nsURI=" + DRIFT_NS + "))") ServiceAware<EPackage> driftPackages,
			@InjectService ServiceAware<ResourceSetFactory> resourceSetFactories) throws Exception {
		int uploadStatus = uploadDriftSchema();
		assertTrue(uploadStatus == 201 || uploadStatus == 200,
				() -> "uploading the drift-test schema should succeed, got HTTP " + uploadStatus);
		try {
			// HYBRID pinned to exactly our nsURI (deterministic publish), fast drift polling.
			Hashtable<String, Object> props = baseProps("HYBRID");
			props.put("eager.nsuri.allow.list", new String[] { DRIFT_NS });
			props.put("drift.check.interval.ms", (int) DRIFT_INTERVAL_MS);
			configuration.update(props);

			assertNotNull(driftPackages.waitForService(SERVICE_WAIT_MS),
					"activation should pre-fetch and publish the uploaded schema");
			ResourceSetFactory factory = resourceSetFactories.waitForService(SERVICE_WAIT_MS);
			assertNotNull(factory, "a framework ResourceSetFactory must be present");

			// Before: content of the uploaded schema deserializes through a framework ResourceSet.
			// (Guards against the 2026-07-29 serve-time leak: EcoreMessageBodyHandler used to
			// re-parent served singletons, after which the EString eType arrived as a proxy to
			// file:/opt/modelatlas/.../release/ecore.ecore#//EString that no client could resolve.)
			EObject before = deserializeDriftPayload(factory);
			assertEquals("Thing", before.eClass().getName());
			assertEquals("hello", before.eGet(before.eClass().getEStructuralFeature("name")));

			// The watcher's first probe of a scope only records the ETag baseline and emits
			// nothing — give it a few intervals so the baseline predates the delete.
			Thread.sleep(6 * DRIFT_INTERVAL_MS);

			assertEquals(200, deleteDriftSchema(), "deleting the schema on the server should succeed");

			// The next drift check sees the nsURI gone, the refetch misses, and the
			// substitution revokes the published trio.
			assertTrue(awaitEmpty(driftPackages),
					"the drift check should revoke the published package after the server-side delete");
			assertTrue(awaitDriftNsUnresolvable(factory),
					"no registry layer should still resolve the removed nsURI (emf.osgi unbind is asynchronous)");

			// After: the exact same payload no longer deserializes — the lazy fallback re-asks
			// the Atlas, legitimately misses, and EMF reports the package as unknown.
			IOException failure = assertThrows(IOException.class, () -> deserializeDriftPayload(factory));
			assertInstanceOf(PackageNotFoundException.class, failure.getCause(),
					"the load must fail because the package is unknown, not for some other reason");
			assertEquals(DRIFT_NS, ((PackageNotFoundException) failure.getCause()).uri());
		} finally {
			deleteDriftSchema(); // idempotent cleanup (204 when already gone)
		}
	}

	@Test
	public void driftDiscovery_publishesAPackageThatAppearedAfterActivation(
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PID, name = "discovery",
					location = "?")) Configuration configuration,
			@InjectService(cardinality = 0, filter = "(atlas.remote=true)") ServiceAware<EPackage> remotePackages,
			@InjectService(cardinality = 0, filter = "(&(atlas.remote=true)(emf.nsURI=" + DISCOVERY_NS
					+ "))") ServiceAware<EPackage> discovered,
			@InjectService ServiceAware<ResourceSetFactory> resourceSetFactories) throws Exception {
		// issue #228: a package promoted into a scope's final stage AFTER the client started
		// used to stay invisible until restart — the drift feed named it, and the watcher
		// dropped it because nothing was held under that nsURI.
		assumeFalse(releasedNsUris().isEmpty(), "jena scope has no released packages to signal EAGER activation");
		deleteSchema(DISCOVERY_NS); // the whole point is that it is absent when the client comes up

		try {
			// resource.set.fallback=false removes the LAZY Atlas fallback, so a later successful
			// deserialization can ONLY come from the published trio — i.e. from discovery.
			Hashtable<String, Object> props = baseProps("EAGER");
			props.put("eager.scopes", new String[] { JENA_SCOPE });
			props.put("drift.check.interval.ms", (int) DRIFT_INTERVAL_MS);
			props.put("resource.set.fallback", Boolean.FALSE);
			configuration.update(props);

			assertNotNull(remotePackages.waitForService(SERVICE_WAIT_MS),
					"EAGER activation should publish the scope's existing packages");
			assertEquals(0, discovered.size(),
					"the discovery schema is not on the server yet, so the pre-fetch cannot have published it");

			// The watcher's first probe of a scope only records the ETag baseline and emits
			// nothing — give it a few intervals so the baseline predates the upload.
			Thread.sleep(6 * DRIFT_INTERVAL_MS);

			int status = uploadSchema(DISCOVERY_ECORE, "DriftDiscovery");
			assertTrue(status == 201 || status == 200,
					() -> "uploading the discovery schema should succeed, got HTTP " + status);

			// Before the fix this waits out the full timeout and fails: the nsURI is named by
			// Atlas-Changed-NsUris, but nothing is held under it, so it was skipped.
			assertNotNull(discovered.waitForService(SERVICE_WAIT_MS),
					"the drift check should publish a package that appeared after activation");
			assertEquals(DISCOVERY_NS, discovered.getService().getNsURI());

			// And the payload a consumer was failing on now deserializes.
			ResourceSetFactory factory = resourceSetFactories.waitForService(SERVICE_WAIT_MS);
			assertNotNull(factory, "a framework ResourceSetFactory must be present");
			assertTrue(awaitResolvable(factory, DISCOVERY_NS),
					"the discovered package should reach framework ResourceSets (emf.osgi binding is asynchronous)");
			EObject instance = deserializePayload(factory, DISCOVERY_PAYLOAD, "drift-discovery.xmi");
			assertEquals("Thing", instance.eClass().getName());
			assertEquals("world", instance.eGet(instance.eClass().getEStructuralFeature("name")));
		} finally {
			deleteSchema(DISCOVERY_NS); // idempotent cleanup
		}
	}

	// ---- helpers ----------------------------------------------------------

	private static int uploadDriftSchema() throws IOException, InterruptedException {
		return uploadSchema(DRIFT_ECORE, "DriftRemoval");
	}

	private static int deleteDriftSchema() throws IOException, InterruptedException {
		return deleteSchema(DRIFT_NS);
	}

	/** POST an .ecore into jena's writable final stage. */
	private static int uploadSchema(String ecore, String name) throws IOException, InterruptedException {
		HttpResponse<String> response = HttpClient.newHttpClient().send(HttpRequest
				.newBuilder(URI.create(baseUri + "/" + JENA_SCOPE + "/schema/stages/" + JENA_VIEW + "?name=" + name))
				.header("Content-Type", "application/xml").POST(HttpRequest.BodyPublishers.ofString(ecore))
				.build(), HttpResponse.BodyHandlers.ofString());
		return response.statusCode();
	}

	/** DELETE one nsURI from jena's writable final stage; idempotent (204 when already gone). */
	private static int deleteSchema(String nsUri) throws IOException, InterruptedException {
		String nsUriParam = URLEncoder.encode(nsUri, StandardCharsets.UTF_8);
		HttpResponse<String> response = HttpClient.newHttpClient().send(HttpRequest.newBuilder(
				URI.create(baseUri + "/" + JENA_SCOPE + "/schema/stages/" + JENA_VIEW + "?nsUri=" + nsUriParam))
				.DELETE().build(), HttpResponse.BodyHandlers.ofString());
		return response.statusCode();
	}

	/** Deserialize {@link #DRIFT_PAYLOAD} through a fresh Atlas-aware framework ResourceSet. */
	private static EObject deserializeDriftPayload(ResourceSetFactory factory) throws IOException {
		return deserializePayload(factory, DRIFT_PAYLOAD, "drift-removal.xmi");
	}

	/** Deserialize an instance document through a fresh framework ResourceSet. */
	private static EObject deserializePayload(ResourceSetFactory factory, String payload, String name)
			throws IOException {
		ResourceSet resourceSet = factory.createResourceSet();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		Resource resource = resourceSet.createResource(org.eclipse.emf.common.util.URI.createURI(name));
		resource.load(new ByteArrayInputStream(payload.getBytes(StandardCharsets.UTF_8)), java.util.Map.of());
		return resource.getContents().get(0);
	}

	/**
	 * Poll until no registry layer resolves {@link #DRIFT_NS} any more. The unpublish is
	 * observable immediately on the service side, but emf.osgi unbinds the configurator from
	 * the framework registry asynchronously — a fresh ResourceSet may briefly still see the
	 * package through the primary registry.
	 */
	private static boolean awaitDriftNsUnresolvable(ResourceSetFactory factory) throws InterruptedException {
		long deadline = System.currentTimeMillis() + SERVICE_WAIT_MS;
		while (System.currentTimeMillis() < deadline) {
			if (factory.createResourceSet().getPackageRegistry().getEPackage(DRIFT_NS) == null) {
				return true;
			}
			Thread.sleep(100L);
		}
		return false;
	}

	/**
	 * Poll until a fresh framework ResourceSet resolves {@code nsUri}. The EPackage service is
	 * observable immediately, but emf.osgi binds its EPackageConfigurator into the framework
	 * registry asynchronously.
	 */
	private static boolean awaitResolvable(ResourceSetFactory factory, String nsUri) throws InterruptedException {
		long deadline = System.currentTimeMillis() + SERVICE_WAIT_MS;
		while (System.currentTimeMillis() < deadline) {
			if (factory.createResourceSet().getPackageRegistry().getEPackage(nsUri) != null) {
				return true;
			}
			Thread.sleep(100L);
		}
		return false;
	}

	/** Poll until the tracked services drain to none, or {@link #SERVICE_WAIT_MS} elapses. */
	// ---- the atlas scope gate (issue #254) ---------------------------------

	@Test
	public void eagerMode_doesNotPublishThePackagesOfTheAtlasScope(
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PID, name = "noatlas",
					location = "?")) Configuration configuration,
			@InjectService(cardinality = 0, filter = "(atlas.remote=true)") ServiceAware<EPackage> remotePackages)
			throws Exception {
		// Every scope inherits from the atlas scope, so a sweep of jena also lists the
		// server's own metamodels. They must not be published (include.atlas.scope=false).
		assumeFalse(atlasOwnedNsUris().isEmpty(), "the jena scope inherits no atlas-owned packages");
		assertTrue(awaitEmpty(remotePackages), "publications of earlier tests should be gone");

		Hashtable<String, Object> props = baseProps("EAGER");
		props.put("eager.scopes", new String[] { JENA_SCOPE });
		props.put("include.atlas.scope", Boolean.FALSE); // the default; stated because baseProps opts in
		configuration.update(props);

		// Give the sweep the same window the sibling test needs to publish them, then check
		// that none of what it published is atlas-owned. That test is the control: it shows
		// these very packages ARE publishable when the flag says so.
		long deadline = System.currentTimeMillis() + SERVICE_WAIT_MS;
		while (System.currentTimeMillis() < deadline) {
			assertFalse(publishedScopes(remotePackages).contains("atlas"),
					"no package owned by the atlas scope may be published");
			Thread.sleep(100L);
		}
	}

	@Test
	public void eagerMode_publishesTheAtlasScopeWhenAskedTo(
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PID, name = "withatlas",
					location = "?")) Configuration configuration,
			@InjectService(cardinality = 0, filter = "(atlas.remote=true)") ServiceAware<EPackage> remotePackages)
			throws Exception {
		assumeFalse(atlasOwnedNsUris().isEmpty(), "the jena scope inherits no atlas-owned packages");
		assertTrue(awaitEmpty(remotePackages), "publications of earlier tests should be gone");

		Hashtable<String, Object> props = baseProps("EAGER");
		props.put("eager.scopes", new String[] { JENA_SCOPE });
		props.put("include.atlas.scope", Boolean.TRUE);
		configuration.update(props);

		long deadline = System.currentTimeMillis() + SERVICE_WAIT_MS;
		while (!publishedScopes(remotePackages).contains("atlas") && System.currentTimeMillis() < deadline) {
			Thread.sleep(50L);
		}
		assertTrue(publishedScopes(remotePackages).contains("atlas"),
				"include.atlas.scope=true must publish them again");
	}

	@Test
	public void anAtlasScopePackageIsStillResolvableOnDemand(
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PID, name = "ondemand",
					location = "?")) Configuration configuration,
			@InjectService(cardinality = 0,
					filter = "(&(atlas.remote=true)(atlas.fetch.on.miss=true))") ServiceAware<EPackage.Registry> registries)
			throws Exception {
		// The flag governs publication, not retrieval: a model referencing one of the
		// server's metamodels must still load.
		String nsUri = firstAtlasNsUriNotHeldLocally();
		assumeTrue(nsUri != null, "every atlas-scope package is already present locally — nothing to fetch");
		assertTrue(awaitEmpty(registries), "registries of earlier tests' components should be gone");

		Hashtable<String, Object> props = baseProps("LAZY");
		props.put("include.atlas.scope", Boolean.FALSE); // the default; stated because baseProps opts in
		// eager.scopes drives the per-scope fetch-on-miss registries in every mode; in LAZY
		// it does not pre-fetch anything.
		props.put("eager.scopes", new String[] { JENA_SCOPE });
		props.put("lazy.resolve.timeout.ms", (int) SERVICE_WAIT_MS * 3);
		configuration.update(props);

		EPackage.Registry registry = registries.waitForService(SERVICE_WAIT_MS);
		assertNotNull(registry, "the scoped fetch-on-miss registry should be registered");
		assertNotNull(registry.getEPackage(nsUri),
				"an atlas-scope package must still resolve on demand, even though it is never published");
	}

	@Test
	public void doesNotPublishAnNsUriAnInstalledBundleOnlyDeclares(
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PID, name = "declared",
					location = "?")) Configuration configuration,
			@InjectBundleContext BundleContext bundleContext,
			@InjectService(cardinality = 0, filter = "(atlas.remote=true)") ServiceAware<EPackage> remotePackages)
			throws Exception {
		// The window issue #254 is really about: a bundle that GENERATES a package is installed
		// but has not run yet, so it has registered no EPackage service and local-first cannot
		// see it - while its generated factory will read the registry the moment it does run.
		// The bundle installed here is that state made explicit: it declares the capability and
		// nothing else, and is never started.
		String nsUri = anAtlasOwnedNsUriNoBundleProvidesYet(bundleContext);
		assumeTrue(nsUri != null, "every atlas-owned package is already provided by this runtime");
		assertTrue(awaitEmpty(remotePackages), "publications of earlier tests should be gone");

		Bundle declaring = bundleContext.installBundle("atlas-test:declaring-" + System.nanoTime(),
				declaringBundleJar(nsUri));
		try {
			Hashtable<String, Object> props = baseProps("EAGER");
			props.put("eager.scopes", new String[] { JENA_SCOPE });
			// The scope filter would hide the effect - this test is about the local-first gate.
			props.put("include.atlas.scope", Boolean.TRUE);
			configuration.update(props);

			assertNotNull(remotePackages.waitForService(SERVICE_WAIT_MS), "the sweep should publish something");
			long deadline = System.currentTimeMillis() + SERVICE_WAIT_MS;
			while (System.currentTimeMillis() < deadline) {
				assertFalse(publishedNsUris(remotePackages).contains(nsUri),
						"a package an installed bundle declares as generated must not be published: " + nsUri);
				Thread.sleep(100L);
			}
		} finally {
			declaring.uninstall();
		}
	}

	/** The {@code emf.nsURI} property of every currently published remote EPackage. */
	private static List<String> publishedNsUris(ServiceAware<EPackage> remotePackages) {
		return remotePackages.getServiceReferences().stream().map(ref -> ref.getProperty("emf.nsURI"))
				.filter(java.util.Objects::nonNull).map(Object::toString).collect(Collectors.toList());
	}

	/**
	 * An nsURI this Atlas serves from its atlas scope that no bundle of this framework declares
	 * as a generated package, so the test can stage the "declared but not yet realised" state
	 * itself; {@code null} when there is none left.
	 */
	private static String anAtlasOwnedNsUriNoBundleProvidesYet(BundleContext bundleContext) throws Exception {
		Set<String> declaredLocally = new java.util.LinkedHashSet<>();
		for (Bundle bundle : bundleContext.getBundles()) {
			BundleRevision revision = bundle.adapt(BundleRevision.class);
			if (revision == null) {
				continue;
			}
			revision.getDeclaredCapabilities("org.eclipse.emf.ecore.generated_package").forEach(capability -> {
				Object uri = capability.getAttributes().get("uri");
				if (uri instanceof String declared) {
					declaredLocally.add(declared);
				}
			});
		}
		return atlasOwnedNsUris().stream().filter(nsUri -> !declaredLocally.contains(nsUri)).findFirst().orElse(null);
	}

	/**
	 * A bundle that declares one generated package and carries nothing else - no classes, no
	 * components. Installed and left in INSTALLED state, it is exactly what the client must be
	 * able to see before anything of that package exists at runtime.
	 */
	private static java.io.InputStream declaringBundleJar(String nsUri) throws IOException {
		java.util.jar.Manifest manifest = new java.util.jar.Manifest();
		java.util.jar.Attributes main = manifest.getMainAttributes();
		main.put(java.util.jar.Attributes.Name.MANIFEST_VERSION, "1.0");
		main.putValue("Bundle-ManifestVersion", "2");
		main.putValue("Bundle-SymbolicName", "org.eclipse.fennec.model.atlas.test.declaring");
		main.putValue("Bundle-Version", "1.0.0");
		main.putValue("Provide-Capability",
				"org.eclipse.emf.ecore.generated_package;uri=\"" + nsUri + "\";class=\"none.Declared\"");
		java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
		try (java.util.jar.JarOutputStream jar = new java.util.jar.JarOutputStream(bytes, manifest)) {
			// manifest only
		}
		return new java.io.ByteArrayInputStream(bytes.toByteArray());
	}

	/** The {@code atlas.scope} property of every currently published remote EPackage. */
	private static List<String> publishedScopes(ServiceAware<EPackage> remotePackages) {
		return remotePackages.getServiceReferences().stream().map(ref -> ref.getProperty("atlas.scope"))
				.filter(java.util.Objects::nonNull).map(Object::toString).collect(Collectors.toList());
	}

	/**
	 * The nsURIs the jena listing reports as owned by the {@code atlas} scope — the
	 * server's statically registered metamodels, which every scope inherits. Read from the
	 * same listing the EAGER sweep uses, so the test sees exactly what the client sees.
	 * Each entry carries its {@code objectId} (Base64-URL of the nsURI) before its
	 * {@code scope}, so the listing is split on the id and each chunk tested for the scope.
	 */
	private static List<String> atlasOwnedNsUris() throws Exception {
		HttpResponse<String> response = HttpClient.newHttpClient().send(
				HttpRequest.newBuilder(URI.create(baseUri + "/" + JENA_SCOPE + "/schema")).GET().build(),
				HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			return List.of();
		}
		List<String> nsUris = new ArrayList<>();
		String[] chunks = response.body().split("\"objectId\"\\s*:\\s*\"");
		for (int i = 1; i < chunks.length; i++) {
			String chunk = chunks[i];
			String objectId = chunk.substring(0, chunk.indexOf('"'));
			if (chunk.contains("\"scope\":\"atlas\"")) {
				nsUris.add(new String(Base64.getUrlDecoder().decode(objectId), StandardCharsets.UTF_8));
			}
		}
		return nsUris;
	}

	/**
	 * An atlas-scope nsURI this OSGi runtime does not already provide, so resolving it
	 * really has to go to the server; {@code null} when there is none.
	 */
	private static String firstAtlasNsUriNotHeldLocally() throws Exception {
		for (String nsUri : atlasOwnedNsUris()) {
			if (EPackage.Registry.INSTANCE.getEPackage(nsUri) == null) {
				return nsUri;
			}
		}
		return null;
	}

	private static boolean awaitEmpty(ServiceAware<?> aware) throws InterruptedException {
		long deadline = System.currentTimeMillis() + SERVICE_WAIT_MS;
		while (aware.size() != 0 && System.currentTimeMillis() < deadline) {
			Thread.sleep(50L);
		}
		return aware.size() == 0;
	}

	private static Hashtable<String, Object> baseProps(String mode) {
		Hashtable<String, Object> props = new Hashtable<>();
		props.put("base.uri", baseUri.toString());
		props.put("mode", mode);
		props.put("view", JENA_VIEW);
		props.put("default.scope", JENA_SCOPE);
		// The bare jena image ships no models of its own: every package the jena scope shows
		// is inherited from the atlas scope, which is not published by default since issue
		// #254. These tests are about publishing, not about that filter, so they opt the
		// atlas scope back in to have anything to publish at all. The filter itself is
		// covered by the tests above, which set the property explicitly.
		props.put("include.atlas.scope", Boolean.TRUE);
		return props;
	}

	/**
	 * Discover the released nsURIs of the jena scope with a plain HTTP GET to
	 * {@code /jena/schema} (the final-stage alias), decoding each {@code objectId} (Base64-URL
	 * of the nsURI). Keeps the test independent of the client API and of which model the image
	 * happens to carry (tests {@code assumeFalse} empty).
	 */
	private static List<String> releasedNsUris() throws Exception {
		HttpResponse<String> response = HttpClient.newHttpClient().send(
				HttpRequest.newBuilder(URI.create(baseUri + "/" + JENA_SCOPE + "/schema")).GET().build(),
				HttpResponse.BodyHandlers.ofString());
		if (response.statusCode() != 200) {
			return List.of();
		}
		List<String> nsUris = new ArrayList<>();
		Matcher matcher = Pattern.compile("\"objectId\"\\s*:\\s*\"([^\"]+)\"").matcher(response.body());
		while (matcher.find()) {
			nsUris.add(new String(Base64.getUrlDecoder().decode(matcher.group(1)), StandardCharsets.UTF_8));
		}
		return nsUris;
	}

	/**
	 * Whether the {@code jena-snapshot} image is present in the local Docker image store.
	 * Shells out to the {@code docker} CLI ({@code docker image inspect}) on purpose: the
	 * Testcontainers/docker-java {@code DockerClient.inspectImageCmd(..)} would drag the
	 * {@code com.github.dockerjava.api.*} packages into this bundle's imports and break the
	 * OSGi resolver. The CLI call is pure JDK ({@link ProcessBuilder}) and leaks no types.
	 * Returns {@code false} (→ test skipped) if Docker is missing, the image is absent, or
	 * the probe times out.
	 */
	private static boolean atlasImageAvailableLocally() {
		try {
			Process probe = new ProcessBuilder("docker", "image", "inspect", IMAGE)
					.redirectOutput(ProcessBuilder.Redirect.DISCARD)
					.redirectError(ProcessBuilder.Redirect.DISCARD).start();
			if (!probe.waitFor(20, TimeUnit.SECONDS)) {
				probe.destroyForcibly();
				return false;
			}
			return probe.exitValue() == 0;
		} catch (IOException unavailable) {
			return false;
		} catch (InterruptedException interrupted) {
			Thread.currentThread().interrupt();
			return false;
		}
	}

	private static String resolveConfigsDir() {
		Path dir = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
		for (Path p = dir; p != null; p = p.getParent()) {
			Path candidate = p.resolve("docker/dockercompose/configs");
			if (Files.isDirectory(candidate)) {
				return candidate.toString();
			}
		}
		throw new IllegalStateException("Could not locate docker/dockercompose/configs starting from " + dir);
	}
}
