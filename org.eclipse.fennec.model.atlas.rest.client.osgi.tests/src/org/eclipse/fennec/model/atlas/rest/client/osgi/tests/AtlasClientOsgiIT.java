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

	// ---- helpers ----------------------------------------------------------

	private static int uploadDriftSchema() throws IOException, InterruptedException {
		HttpResponse<String> response = HttpClient.newHttpClient().send(HttpRequest
				.newBuilder(URI.create(baseUri + "/" + JENA_SCOPE + "/schema/stages/" + JENA_VIEW + "?name=DriftRemoval"))
				.header("Content-Type", "application/xml").POST(HttpRequest.BodyPublishers.ofString(DRIFT_ECORE))
				.build(), HttpResponse.BodyHandlers.ofString());
		return response.statusCode();
	}

	private static int deleteDriftSchema() throws IOException, InterruptedException {
		String nsUriParam = URLEncoder.encode(DRIFT_NS, StandardCharsets.UTF_8);
		HttpResponse<String> response = HttpClient.newHttpClient().send(HttpRequest.newBuilder(
				URI.create(baseUri + "/" + JENA_SCOPE + "/schema/stages/" + JENA_VIEW + "?nsUri=" + nsUriParam))
				.DELETE().build(), HttpResponse.BodyHandlers.ofString());
		return response.statusCode();
	}

	/** Deserialize {@link #DRIFT_PAYLOAD} through a fresh Atlas-aware framework ResourceSet. */
	private static EObject deserializeDriftPayload(ResourceSetFactory factory) throws IOException {
		ResourceSet resourceSet = factory.createResourceSet();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("xmi", new XMIResourceFactoryImpl());
		Resource resource = resourceSet
				.createResource(org.eclipse.emf.common.util.URI.createURI("drift-removal.xmi"));
		resource.load(new ByteArrayInputStream(DRIFT_PAYLOAD.getBytes(StandardCharsets.UTF_8)), java.util.Map.of());
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

	/** Poll until the tracked services drain to none, or {@link #SERVICE_WAIT_MS} elapses. */
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
