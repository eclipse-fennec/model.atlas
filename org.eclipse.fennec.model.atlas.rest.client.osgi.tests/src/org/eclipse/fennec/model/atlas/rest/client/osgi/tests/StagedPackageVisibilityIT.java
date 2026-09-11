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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.eclipse.emf.ecore.EPackage;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
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
 * Packages at a <em>non-final</em> stage, end to end against a live Atlas (#279, #280, #281).
 *
 * <p>
 * The reported failure: a package published to a non-final stage never became visible to a
 * consuming runtime. The pre-fetch enumerated the final stage alone, and {@code eager.stages} only
 * gave a fetch-on-miss registry — which never fires for a consumer that refuses to dereference an
 * untrusted nsURI, or that addresses by fingerprint and so has no nsURI to miss on.
 * </p>
 *
 * <p>
 * These assert on published {@code EPackage} <b>services</b> rather than on any client internal,
 * because that is the observable that matters: in {@code emf.osgi},
 * {@code MetadataServiceComponent.addEPackage} registers every published EPackage service with the
 * metadata whiteboard, so "published as a service" and "resolvable through {@code MetadataService}
 * by nsURI and by fingerprint" are the same event.
 * </p>
 *
 * <p>
 * {@code jena}'s schema registry stages are {@code draft → approved → release}, only {@code release}
 * final — the shape the report was made against. Skipped when Docker or the locally built
 * {@code jena-snapshot} image is absent, so a normal build stays green; rebuild that image after
 * changing the server, or these run against yesterday's Atlas.
 * </p>
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class StagedPackageVisibilityIT {

	private static final String PID = "org.eclipse.fennec.model.atlas.rest.client";

	private static final String IMAGE = "eclipsefennec/model.atlas:jena-snapshot";
	private static final int HTTP_PORT = 8080;
	private static final String JENA_SCOPE = "jena";
	private static final String FINAL_STAGE = "release";
	private static final String STAGED = "approved";
	private static final String CONFIG_LOAD_DIR = "/opt/modelatlas/runtime/load";
	private static final String STORAGE_ROOT = "/opt/modelatlas/runtime/data";

	private static final long SERVICE_WAIT_MS = 15_000L;
	/** Short enough that the discovery test does not sit through a production poll. */
	private static final long DRIFT_INTERVAL_MS = 250L;

	private static final String ATLAS_STAGE = "atlas.stage";
	private static final String FINGERPRINT = "emf.fingerprint";

	/** Present only at {@code approved} — the case that used to be invisible. */
	private static final String STAGED_NS = "http://atlas.example/test/stagedonly/1.0";
	/** Published at both stages with diverging content — two live model versions. */
	private static final String TWO_VERSION_NS = "http://atlas.example/test/twoversions/1.0";
	/** Published at {@code approved} only after the client is already up. */
	private static final String LATE_NS = "http://atlas.example/test/latestage/1.0";

	private static GenericContainer<?> atlas;
	private static URI baseUri;

	@BeforeAll
	static void startAtlas() throws Exception {
		assumeTrue(DockerClientFactory.instance().isDockerAvailable(),
				"Docker not available — skipping the live-Atlas staged-visibility tests");
		assumeTrue(imageAvailableLocally(), "Image " + IMAGE + " not present locally — build it to run this IT");
		atlas = new GenericContainer<>(IMAGE).withExposedPorts(HTTP_PORT)
				.withEnv("STORAGE_ROOT", STORAGE_ROOT)
				.withEnv("ATLAS_HTTP_PORT", String.valueOf(HTTP_PORT))
				.withFileSystemBind(resolveConfigsDir(), CONFIG_LOAD_DIR, BindMode.READ_ONLY)
				.waitingFor(Wait.forHttp("/atlas/rest/scopes").forPort(HTTP_PORT).forStatusCode(200)
						.forResponsePredicate(body -> body.contains(JENA_SCOPE)))
				.withStartupTimeout(Duration.ofMinutes(2));
		atlas.start();
		baseUri = URI.create("http://" + atlas.getHost() + ":" + atlas.getMappedPort(HTTP_PORT) + "/atlas/rest");

		// Seeded before any client activates, so the pre-fetch has something staged to find.
		assertUploaded(upload(ecore("stagedonly", STAGED_NS, false), STAGED, "StagedOnly"), STAGED_NS, STAGED);
		assertUploaded(upload(ecore("twoversions", TWO_VERSION_NS, false), FINAL_STAGE, "TwoVersionsReleased"),
				TWO_VERSION_NS, FINAL_STAGE);
		assertUploaded(upload(ecore("twoversions", TWO_VERSION_NS, true), STAGED, "TwoVersionsApproved"),
				TWO_VERSION_NS, STAGED);
	}

	@AfterAll
	static void stopAtlas() {
		if (atlas != null) {
			atlas.stop();
			atlas.close();
		}
	}

	/**
	 * #280 — the reported case. A package that exists only at {@code approved} is registered at
	 * activation, without anything provoking a fetch-on-miss first.
	 */
	@Test
	public void aPackageOnlyAtANonFinalStageIsPublished(
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PID,
					name = "staged-visible", location = "?")) Configuration configuration,
			@InjectService(cardinality = 0, filter = "(&(atlas.remote=true)(atlas.stage=approved))") //
			ServiceAware<EPackage> stagedPackages) throws Exception {

		configuration.update(eagerWithStages(STAGED));

		assertNotNull(stagedPackages.waitForService(SERVICE_WAIT_MS),
				"a package present only at a non-final stage must be published, not merely fetchable on a miss");
		assertTrue(nsUrisOf(stagedPackages).contains(STAGED_NS),
				"the staged package should be among the publications, was: " + nsUrisOf(stagedPackages));
	}

	/**
	 * #279 — one nsURI, two live versions. Keying publications by nsURI silently dropped the
	 * second; they must now coexist, distinguishable by stage and by fingerprint.
	 */
	@Test
	public void twoVersionsOfOneNsUriAreBothPublished(
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PID,
					name = "staged-two-versions", location = "?")) Configuration configuration,
			@InjectService(cardinality = 0, filter = "(atlas.remote=true)") //
			ServiceAware<EPackage> remotePackages, @InjectBundleContext BundleContext context) throws Exception {

		configuration.update(eagerWithStages(STAGED, FINAL_STAGE));
		assertNotNull(remotePackages.waitForService(SERVICE_WAIT_MS), "activation should publish something");
		awaitVersions(remotePackages, TWO_VERSION_NS, 2, context);

		List<ServiceReference<EPackage>> versions = referencesFor(remotePackages, TWO_VERSION_NS, context);
		Set<Object> stages = versions.stream().map(ref -> ref.getProperty(ATLAS_STAGE)).collect(Collectors.toSet());
		Set<Object> fingerprints = versions.stream().map(ref -> ref.getProperty(FINGERPRINT))
				.collect(Collectors.toSet());

		assertEquals(2, versions.size(), "both live versions of the nsURI must be published");
		assertTrue(stages.containsAll(Set.of(STAGED, FINAL_STAGE)), "one per stage, was: " + stages);
		assertEquals(2, fingerprints.size(), "diverging content must yield distinct fingerprints: " + fingerprints);
	}

	/**
	 * #281 — a package published to a non-final stage <em>after</em> the client is up. Discovery
	 * used to look for it at the final stage, find nothing, and give up.
	 */
	@Test
	public void aPackageAddedAtANonFinalStageIsDiscovered(
			@InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PID,
					name = "staged-discovery", location = "?")) Configuration configuration,
			@InjectService(cardinality = 0, filter = "(&(atlas.remote=true)(atlas.stage=approved))") //
			ServiceAware<EPackage> stagedPackages) throws Exception {

		Hashtable<String, Object> props = eagerWithStages(STAGED);
		props.put("drift.check.interval.ms", (int) DRIFT_INTERVAL_MS);
		configuration.update(props);
		assertNotNull(stagedPackages.waitForService(SERVICE_WAIT_MS), "the client should be up and pre-fetched");

		// Only now does the package appear on the server, at a non-final stage.
		assertUploaded(upload(ecore("latestage", LATE_NS, false), STAGED, "LateStage"), LATE_NS, STAGED);

		assertTrue(awaitNsUri(stagedPackages, LATE_NS),
				"a package added at a non-final stage must be discovered there, not looked for at the final stage");
	}

	// ---- helpers ------------------------------------------------------------

	private static Hashtable<String, Object> eagerWithStages(String... stages) {
		Hashtable<String, Object> props = new Hashtable<>();
		props.put("base.uri", baseUri.toString());
		props.put("mode", "EAGER");
		props.put("default.scope", JENA_SCOPE);
		props.put("eager.scopes", new String[] { JENA_SCOPE });
		props.put("eager.stages", stages);
		return props;
	}

	/** A minimal schema; {@code extra} adds a second EClass, making it a different model version. */
	private static String ecore(String name, String nsUri, boolean extra) {
		return """
				<?xml version="1.0" encoding="UTF-8"?>
				<ecore:EPackage xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI"
				    xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
				    xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore"
				    name="%s" nsURI="%s" nsPrefix="%s">
				  <eClassifiers xsi:type="ecore:EClass" name="Thing">
				    <eStructuralFeatures xsi:type="ecore:EAttribute" name="name"
				        eType="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EString"/>
				  </eClassifiers>
				  %s
				</ecore:EPackage>
				""".formatted(name, nsUri, name, extra ? "<eClassifiers xsi:type=\"ecore:EClass\" name=\"Extra\"/>" : "");
	}

	private static int upload(String ecore, String stage, String name) throws IOException, InterruptedException {
		HttpResponse<String> response = HttpClient.newHttpClient()
				.send(HttpRequest
						.newBuilder(URI.create(baseUri + "/" + JENA_SCOPE + "/schema/stages/" + stage + "?name=" + name))
						.header("Content-Type", "application/xml").POST(HttpRequest.BodyPublishers.ofString(ecore))
						.build(), HttpResponse.BodyHandlers.ofString());
		return response.statusCode();
	}

	private static void assertUploaded(int status, String nsUri, String stage) {
		assertTrue(status == 201 || status == 200,
				() -> "seeding " + nsUri + " at stage " + stage + " should succeed, got HTTP " + status);
	}

	private static Set<String> nsUrisOf(ServiceAware<EPackage> aware) {
		return aware.getServices().stream().map(EPackage::getNsURI).collect(Collectors.toSet());
	}

	private static List<ServiceReference<EPackage>> referencesFor(ServiceAware<EPackage> aware, String nsUri,
			BundleContext context) {
		Collection<ServiceReference<EPackage>> refs = aware.getServiceReferences();
		return refs.stream().filter(ref -> {
			EPackage ePackage = context.getService(ref);
			try {
				return ePackage != null && nsUri.equals(ePackage.getNsURI());
			} finally {
				context.ungetService(ref);
			}
		}).collect(Collectors.toList());
	}

	/** Publication is asynchronous; poll rather than assume the pre-fetch has finished. */
	private static void awaitVersions(ServiceAware<EPackage> aware, String nsUri, int expected, BundleContext context)
			throws Exception {
		long deadline = System.currentTimeMillis() + SERVICE_WAIT_MS;
		while (System.currentTimeMillis() < deadline && referencesFor(aware, nsUri, context).size() < expected) {
			Thread.sleep(100L);
		}
	}

	private static boolean awaitNsUri(ServiceAware<EPackage> aware, String nsUri) throws Exception {
		long deadline = System.currentTimeMillis() + SERVICE_WAIT_MS;
		while (System.currentTimeMillis() < deadline) {
			if (nsUrisOf(aware).contains(nsUri)) {
				return true;
			}
			Thread.sleep(100L);
		}
		return false;
	}

	/**
	 * Probed with the docker CLI rather than the docker-java API on purpose: touching that API
	 * makes this bundle import {@code com.github.dockerjava.api}, which the test runtime does not
	 * export — and an unresolvable test bundle takes every test in it down, not just this one.
	 */
	private static boolean imageAvailableLocally() {
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
