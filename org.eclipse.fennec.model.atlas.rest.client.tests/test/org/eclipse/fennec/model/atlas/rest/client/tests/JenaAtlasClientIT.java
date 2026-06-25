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
package org.eclipse.fennec.model.atlas.rest.client.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeFalse;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.fennec.model.atlas.rest.client.api.ClientConfiguration;
import org.eclipse.fennec.model.atlas.rest.client.api.ModelAtlasClient;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * P2-11 — end-to-end tests for the plain-Java {@link ModelAtlasClient} against a
 * live Atlas server started from the {@code jena} docker image (Testcontainers).
 * <p>
 * Skipped automatically when Docker is unavailable, or when the locally-built
 * {@code jena-snapshot} image is absent (e.g. on CI, which has Docker but never
 * builds it), so a normal build stays green; build the image and run locally to
 * exercise the real round-trip. Tests adapt to whatever the {@code jena} scope's
 * released stage actually contains (they skip if it is empty) rather than assuming
 * a specific model.
 * <p>
 * Still TODO (need server-side writes / an auth-enabled server, iterated with a
 * running instance): drift {@code onPackageChanged} after a server mutation, and
 * end-to-end bearer auth.
 */
class JenaAtlasClientIT {

	private static final String IMAGE = "eclipsefennec/model.atlas:jena-snapshot";
	private static final int HTTP_PORT = 8080;
	private static final String JENA_SCOPE = "jena";

	/** Mount target matching {@code docker-compose-jena.yml} (./configs:/opt/modelatlas/runtime/load). */
	private static final String CONFIG_LOAD_DIR = "/opt/modelatlas/runtime/load";
	private static final String STORAGE_ROOT = "/opt/modelatlas/runtime/data";

	private static GenericContainer<?> atlas;
	private static URI baseUri;
	private static ModelAtlasClient client;

	@BeforeAll
	static void startAtlas() {
		assumeTrue(DockerClientFactory.instance().isDockerAvailable(),
				"Docker not available — skipping live-Atlas integration tests");
		// The jena image is built locally (not published), so it is absent on CI runners that
		// have Docker but never build it. Skip rather than fail there — don't try to pull a
		// non-existent remote image. Build the jena docker image locally to exercise this IT.
		assumeTrue(atlasImageAvailableLocally(),
				"Image " + IMAGE + " not present locally — skipping (build it to run this IT)");
		// The bare jena image only exposes the root 'atlas' scope. The 'jena' scope is a
		// ConfigAdmin factory config (ScopeService~jena) that the compose stack supplies by
		// mounting ./configs -> /opt/modelatlas/runtime/load. Replicate that here, otherwise
		// /scopes never lists jena and the readiness probe below times out.
		String configsDir = resolveConfigsDir();
		// Wait until /scopes is up AND the (lazily-configured) jena scope has registered
		// — not just until the REST layer answers, which happens before jena binds.
		atlas = new GenericContainer<>(IMAGE).withExposedPorts(HTTP_PORT)
				.withEnv("STORAGE_ROOT", STORAGE_ROOT)
				.withEnv("ATLAS_HTTP_PORT", String.valueOf(HTTP_PORT))
				.withFileSystemBind(configsDir, CONFIG_LOAD_DIR, BindMode.READ_ONLY)
				.waitingFor(Wait.forHttp("/atlas/rest/scopes").forPort(HTTP_PORT).forStatusCode(200)
						.forResponsePredicate(body -> body.contains(JENA_SCOPE)))
				.withStartupTimeout(Duration.ofMinutes(2));
		atlas.start();
		baseUri = URI.create("http://" + atlas.getHost() + ":" + atlas.getMappedPort(HTTP_PORT) + "/atlas/rest");
		client = ModelAtlasClient.builder().configuration(jenaConfig().build()).build();
	}

	/**
	 * A {@link ClientConfiguration} builder pointed at the running jena Atlas, with {@code jena}
	 * as the default scope so per-nsURI content look-ups go through jena and rely on the server's
	 * hierarchy walk (jena's release stage inherits the parent atlas scope's released packages).
	 * Reads are stage-free (P5-7): no {@code view} is set — the server resolves jena's final stage.
	 */
	private static ClientConfiguration.Builder jenaConfig() {
		return ClientConfiguration.builder().baseUri(baseUri).defaultScope(JENA_SCOPE);
	}

	/**
	 * Whether the {@link #IMAGE} is present in the local Docker image store. Used to skip
	 * (not fail) when the locally-built jena image is absent — e.g. on CI, which has Docker
	 * but never builds it. We deliberately do not pull, since the image is not published.
	 */
	private static boolean atlasImageAvailableLocally() {
		try {
			DockerClientFactory.instance().client().inspectImageCmd(IMAGE).exec();
			return true;
		} catch (RuntimeException notPresent) {
			return false;
		}
	}

	/**
	 * Locate {@code docker/dockercompose/configs} (which carries the jena scope config)
	 * by walking up from the test working directory, so the bind works regardless of
	 * which directory Gradle/bnd runs the test task from.
	 */
	private static String resolveConfigsDir() {
		Path dir = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
		for (Path p = dir; p != null; p = p.getParent()) {
			Path candidate = p.resolve("docker/dockercompose/configs");
			if (Files.isDirectory(candidate)) {
				return candidate.toString();
			}
		}
		throw new IllegalStateException(
				"Could not locate docker/dockercompose/configs starting from " + dir);
	}

	@AfterAll
	static void stopAtlas() {
		if (client != null) {
			client.close();
		}
		if (atlas != null) {
			atlas.stop();
			atlas.close();
		}
	}

	/** First released nsURI in the jena scope, or skip the test if none. */
	private static String firstReleasedNsUri() {
		List<String> nsUris = client.ePackages().listNsUris(JENA_SCOPE);
		assumeFalse(nsUris.isEmpty(), "jena scope has no released packages to exercise");
		return nsUris.get(0);
	}

	@Test
	void listScopeNames_includesJena() {
		List<String> scopes = client.listScopeNames();
		assertTrue(scopes.contains(JENA_SCOPE), () -> "expected '" + JENA_SCOPE + "' among " + scopes);
	}

	@Test
	void getEPackage_roundTrips_andSecondCallIsCached() {
		String nsUri = firstReleasedNsUri();

		Optional<EPackage> pkg = client.ePackages().getEPackage(nsUri);
		assertTrue(pkg.isPresent(), () -> "package " + nsUri + " should be fetched from the Atlas");
		assertEquals(nsUri, pkg.get().getNsURI());
		assertNotNull(pkg.get().getEFactoryInstance(), "fetched package must carry an EFactory");

		// Cache hit: the second look-up returns the very same instance (no re-fetch).
		assertSame(pkg.get(), client.ePackages().getEPackage(nsUri).orElseThrow());
	}

	@Test
	void newResourceSet_resolvesUnknownNsUriThroughAtlas() {
		String nsUri = firstReleasedNsUri();

		// A brand-new client whose ResourceSet does not hold the package locally;
		// loading must resolve the nsURI through the Atlas-aware delegating registry.
		try (ModelAtlasClient fresh = ModelAtlasClient.builder().configuration(jenaConfig().build()).build()) {
			ResourceSet resourceSet = fresh.newResourceSet();
			EPackage resolved = resourceSet.getPackageRegistry().getEPackage(nsUri);
			assertNotNull(resolved, () -> nsUri + " should resolve through the Atlas-aware ResourceSet");
			assertEquals(nsUri, resolved.getNsURI());
		}
	}

	@Test
	void denyList_blocksAnOtherwiseAvailablePackage() {
		String nsUri = firstReleasedNsUri();

		ClientConfiguration denied = jenaConfig().nsUriDenyList(List.of(nsUri)).build();
		try (ModelAtlasClient deniedClient = ModelAtlasClient.builder().configuration(denied).build()) {
			assertTrue(deniedClient.ePackages().getEPackage(nsUri).isEmpty(),
					"a denied nsURI must not resolve even though the server has it");
		}
	}
}
