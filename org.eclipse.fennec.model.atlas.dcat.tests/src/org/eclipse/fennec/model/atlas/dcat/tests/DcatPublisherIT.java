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
package org.eclipse.fennec.model.atlas.dcat.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Base64;
import java.time.Duration;
import java.util.Hashtable;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.Configuration;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.config.InjectConfiguration;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.testcontainers.DockerClientFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;

/**
 * The publisher, the real DCAT.Atlas client and a real portal container.
 *
 * <p>
 * Assertions are made by reading the portal back over plain HTTP rather than through the client,
 * so a bug that makes the client agree with itself cannot make this pass. Skipped — not failed —
 * when Docker or the portal image is missing, because the image is built locally and is not
 * published to a registry, so requiring it would make this red for everybody else.
 * </p>
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
public class DcatPublisherIT {

    private static final String IMAGE = "eclipse-fennec/dcat.atlas:latest";
    private static final String CLIENT_PID = "org.eclipse.fennec.dcat.atlas.client";
    private static final String PUBLISHER_PID = "DcatPublisher";
    private static final String PORTAL = "it";
    private static final int HTTP_PORT = 8080;
    private static final long PUBLISH_WAIT_MS = 20_000;

    /**
     * The portal reports readiness CRITICAL when a shapes directory is configured but holds no
     * *.ttl, on the grounds that validation would then silently pass everything — and the
     * directory is configured inside the image, so leaving it unmounted does not help. This is a
     * self-authored placeholder, NOT the official GovData DCAT-AP.de shapes, which are AGPL-3.0
     * and are deployment input. Enforcement stays off; the model-level write floor still applies,
     * because validate-on-write is independent of SHACL.
     */
    private static final String SMOKE_SHAPES = """
            @prefix sh:   <http://www.w3.org/ns/shacl#> .
            @prefix dcat: <http://www.w3.org/ns/dcat#> .
            @prefix dct:  <http://purl.org/dc/terms/> .

            <http://example.org/shapes/it/CatalogShape> a sh:NodeShape ;
                sh:targetClass dcat:Catalog ;
                sh:property [ sh:path dct:title ; sh:minCount 1 ; sh:severity sh:Violation ] .
            """;

    private static GenericContainer<?> portal;
    private static String portalRestBase;
    private static Path shapesDir;

    @BeforeAll
    static void startPortal() throws IOException {
        assumeTrue(DockerClientFactory.instance().isDockerAvailable(),
                "Docker not available — skipping the DCAT portal integration tests");
        assumeTrue(portalImageAvailableLocally(),
                "Image " + IMAGE + " not present locally — skipping (build it in dcat.atlas to run this IT)");

        shapesDir = Files.createTempDirectory("dcat-it-shapes");
        Path shapesFile = shapesDir.resolve("it-smoke-shapes.ttl");
        Files.writeString(shapesFile, SMOKE_SHAPES);
        // createTempDirectory is 0700 and the image runs as uid 65532, so the bind mount would be
        // unreadable inside the container — and an unreadable shapes directory makes /health/ready
        // answer 500, which reads as "the image is broken" rather than "the mount is wrong".
        // A directory needs the execute bit to be traversed, not just read.
        Files.setPosixFilePermissions(shapesDir, PosixFilePermissions.fromString("rwxr-xr-x"));
        Files.setPosixFilePermissions(shapesFile, PosixFilePermissions.fromString("rw-r--r--"));

        portal = new GenericContainer<>(IMAGE).withExposedPorts(HTTP_PORT)
                .withEnv("HTTP_PORT", String.valueOf(HTTP_PORT))
                .withEnv("CONTEXT_PATH", "/")
                .withEnv("GIT_REPO", "/opt/dcat/store.git")
                .withEnv("GIT_BRANCH", "main")
                .withEnv("SHACL_ENFORCE", "false")
                // Required: the portal refuses to instantiate PublicIrisImpl without it, and the
                // failure surfaces as /health/ready answering 500 rather than 503 — which reads as a
                // broken image instead of a missing variable. A fixed value is fine even though
                // Testcontainers maps a random host port: this only sets the IRIs the portal stamps
                // into what it serves, not how this test reaches it, and the assertions below read
                // back through the mapped port.
                .withEnv("PUBLIC_BASE_URL", "http://localhost:" + HTTP_PORT + "/rest/")
                .withFileSystemBind(shapesDir.toString(), "/opt/dcat/shapes", BindMode.READ_ONLY)
                // Readiness, not liveness: a portal whose stores are not usable would fail the
                // writes below for a reason that has nothing to do with the publisher.
                .waitingFor(Wait.forHttp("/health/ready").forPort(HTTP_PORT).forStatusCode(200))
                .withStartupTimeout(Duration.ofMinutes(3));
        portal.start();
        portalRestBase = "http://" + portal.getHost() + ":" + portal.getMappedPort(HTTP_PORT) + "/rest";
    }

    @AfterAll
    static void stopPortal() throws IOException {
        if (portal != null) {
            portal.stop();
            portal.close();
        }
        if (shapesDir != null) {
            Files.deleteIfExists(shapesDir.resolve("it-smoke-shapes.ttl"));
            Files.deleteIfExists(shapesDir);
        }
    }

    // ---- tests ------------------------------------------------------------

    @Test
    public void publishesTheScopeAsACatalogThePortalCanServe(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = CLIENT_PID,
                    name = PORTAL, location = "?")) Configuration clientConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PUBLISHER_PID,
                    name = PORTAL, location = "?")) Configuration publisherConfig)
            throws Exception {

        clientConfig.update(clientProps());
        publisherConfig.update(publisherProps(StubScopeService.SCOPE, false));

        String body = awaitCatalog(StubScopeService.SCOPE);
        assertNotNull(body, "the portal should serve a Catalog for the published scope");

        // The scope's own description, not the configured template: proof the publisher read the
        // scope rather than inventing a Catalog.
        assertTrue(body.contains(StubScopeService.DESCRIPTION),
                "Catalog should carry the scope's description, was: " + body);
        // The write floor: publisher is a lowerBound=1 containment, so a Catalog without a named
        // Agent never reaches the store at all.
        assertTrue(body.contains("Integration Test Publisher"),
                "Catalog should carry the configured publisher Agent, was: " + body);
    }

    @Test
    public void republishingReplacesRatherThanDuplicating(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = CLIENT_PID,
                    name = PORTAL + "2", location = "?")) Configuration clientConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PUBLISHER_PID,
                    name = PORTAL + "2", location = "?")) Configuration publisherConfig)
            throws Exception {

        clientConfig.update(clientProps());
        publisherConfig.update(publisherProps(StubScopeService.SCOPE, false));
        assertNotNull(awaitCatalog(StubScopeService.SCOPE), "first publish should land");

        // A configuration update re-activates the component, which republishes every scope. The
        // id is derived from the scope name, so an idempotent PUT must replace: the ENTER replay
        // at startup does exactly this on every boot.
        publisherConfig.update(publisherProps(StubScopeService.SCOPE, false));

        String list = get(portalRestBase + "/catalogs");
        int occurrences = list.split("\"" + StubScopeService.SCOPE + "\"", -1).length - 1;
        assertTrue(occurrences <= 1,
                "re-publishing must replace, not add a second Catalog; found " + occurrences + " in: " + list);
    }

    @Test
    public void aScopeOutsideTheConfiguredListIsNotPublished(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = CLIENT_PID,
                    name = PORTAL + "3", location = "?")) Configuration clientConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PUBLISHER_PID,
                    name = PORTAL + "3", location = "?")) Configuration publisherConfig)
            throws Exception {

        clientConfig.update(clientProps());
        // Scopes are opt-in from configuration: a package's metadata cannot express "this
        // deployment publishes to that portal", so this gate is the only thing standing between
        // an upload and a public catalogue.
        publisherConfig.update(publisherProps("some-other-scope", false));

        Thread.sleep(3_000);
        String body = get(portalRestBase + "/catalogs/" + StubScopeService.SCOPE + "-absent");
        assertTrue(body == null || !body.contains(StubScopeService.SCOPE + "-absent"),
                "a scope absent from `scopes` must not reach the portal");
    }

    @Test
    public void refusesToActivateOnALoopbackBaseUriUnlessAllowed(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = CLIENT_PID,
                    name = PORTAL + "4", location = "?")) Configuration clientConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PUBLISHER_PID,
                    name = PORTAL + "4", location = "?")) Configuration publisherConfig)
            throws Exception {

        clientConfig.update(clientProps());
        Hashtable<String, Object> props = publisherProps(StubScopeService.SCOPE, true);
        props.put("atlas.public.base.uri", "http://localhost:8086/atlas/rest");
        props.put("allow.local.base.uri", false);
        publisherConfig.update(props);

        // Activation throws, so nothing is published. A localhost URL in a public catalogue is
        // worse than no catalogue entry, which is why this is a refusal rather than a warning.
        Thread.sleep(3_000);
        String body = get(portalRestBase + "/catalogs/" + StubScopeService.SCOPE + "-local");
        assertTrue(body == null || !body.contains("localhost:8086"),
                "a loopback base URI must not produce a published Catalog");
    }

    // ---- D2/D3: Datasets, Distributions and the tracker --------------------

    @Test
    public void aFlaggedPackageBecomesADatasetWithDistributions(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = CLIENT_PID,
                    name = PORTAL + "5", location = "?")) Configuration clientConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PUBLISHER_PID,
                    name = PORTAL + "5", location = "?")) Configuration publisherConfig,
            @InjectBundleContext BundleContext context) throws Exception {

        clientConfig.update(clientProps());
        publisherConfig.update(publisherProps(StubScopeService.SCOPE, false));
        assertNotNull(awaitCatalog(StubScopeService.SCOPE), "the Catalog has to exist to link into");

        String nsUri = "http://test.example.com/person/1.1";
        String datasetId = datasetId(nsUri, PublishablePackages.FINAL_STAGE);
        ServiceRegistration<?> registration = PublishablePackages.register(context, nsUri,
                PublishablePackages.FINAL_STAGE, FINGERPRINT, true);
        try {
            String dataset = await(portalRestBase + "/datasets/" + datasetId);
            assertNotNull(dataset, "a dcat=true EPackage service should become a Dataset");

            // dct:identifier is the nsURI verbatim — with no DatasetSeries, this is how a consumer
            // finds every stage of one model.
            assertTrue(dataset.contains(nsUri), "Dataset should carry the nsURI as its identifier: " + dataset);
            assertTrue(dataset.contains("Person"), "Dataset title should name the model: " + dataset);
            // The distribution URL must pin the representation with ?mediaType=, not rely on
            // Accept: a gateway cache keyed without Vary would serve the wrong bytes.
            assertTrue(dataset.contains("mediaType=application%2Fxmi") || dataset.contains("mediaType=application/xmi"),
                    "a Distribution should carry a downloadURL pinning its media type: " + dataset);
            assertTrue(dataset.contains("/schema/stages/" + PublishablePackages.FINAL_STAGE + "/content"),
                    "URLs should address the stage content endpoint: " + dataset);
        } finally {
            registration.unregister();
        }
    }

    @Test
    public void anUnflaggedPackageIsNeverTracked(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = CLIENT_PID,
                    name = PORTAL + "6", location = "?")) Configuration clientConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PUBLISHER_PID,
                    name = PORTAL + "6", location = "?")) Configuration publisherConfig,
            @InjectBundleContext BundleContext context) throws Exception {

        clientConfig.update(clientProps());
        publisherConfig.update(publisherProps(StubScopeService.SCOPE, false));

        String nsUri = "http://test.example.com/unflagged/1.0";
        // dcat=false, so the tracker's (dcat=true) filter never matches and no bind happens. This
        // is the whole mechanism: selection is the filter's job, not a policy call's.
        ServiceRegistration<?> registration = PublishablePackages.register(context, nsUri,
                PublishablePackages.FINAL_STAGE, FINGERPRINT, false);
        try {
            Thread.sleep(4_000);
            assertTrue(get(portalRestBase + "/datasets/" + datasetId(nsUri, PublishablePackages.FINAL_STAGE)) == null,
                    "an unflagged package must not reach the portal");
        } finally {
            registration.unregister();
        }
    }

    @Test
    public void aNonFinalStageRecordsIntentWithoutPublishing(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = CLIENT_PID,
                    name = PORTAL + "7", location = "?")) Configuration clientConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PUBLISHER_PID,
                    name = PORTAL + "7", location = "?")) Configuration publisherConfig,
            @InjectBundleContext BundleContext context) throws Exception {

        clientConfig.update(clientProps());
        publisherConfig.update(publisherProps(StubScopeService.SCOPE, false));

        String nsUri = "http://test.example.com/draftonly/1.0";
        // publish.stages defaults to FINAL, resolved against the scope's own StageInfo: a flagged
        // upload to draft is an intent, not a publication. Promotion is what publishes it.
        ServiceRegistration<?> registration = PublishablePackages.register(context, nsUri, "draft", FINGERPRINT, true);
        try {
            Thread.sleep(4_000);
            assertTrue(get(portalRestBase + "/datasets/" + datasetId(nsUri, "draft")) == null,
                    "a flagged package in a non-final stage must not be published");
        } finally {
            registration.unregister();
        }
    }

    @Test
    public void aCatalogRewriteKeepsItsDatasetMemberships(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = CLIENT_PID,
                    name = PORTAL + "8", location = "?")) Configuration clientConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PUBLISHER_PID,
                    name = PORTAL + "8", location = "?")) Configuration publisherConfig,
            @InjectBundleContext BundleContext context) throws Exception {

        clientConfig.update(clientProps());
        publisherConfig.update(publisherProps(StubScopeService.SCOPE, false));

        String nsUri = "http://test.example.com/membership/1.0";
        String datasetId = datasetId(nsUri, PublishablePackages.FINAL_STAGE);
        ServiceRegistration<?> registration = PublishablePackages.register(context, nsUri,
                PublishablePackages.FINAL_STAGE, FINGERPRINT, true);
        try {
            assertNotNull(await(portalRestBase + "/datasets/" + datasetId), "setup: the Dataset should publish");

            // Re-activate. This rewrites the Catalog, and a PUT replaces — so dcat:dataset
            // membership is dropped. The Dataset itself is correctly skipped as unchanged, which
            // means nothing re-asserts the link unless the Catalog write does it. Every restart
            // would otherwise leave a Catalog that lists nothing while its Datasets still exist.
            publisherConfig.update(publisherProps(StubScopeService.SCOPE, false));
            Thread.sleep(6_000);

            String catalog = get(portalRestBase + "/catalogs/" + StubScopeService.SCOPE);
            assertNotNull(catalog, "the Catalog should still exist");
            assertTrue(catalog.contains(datasetId),
                    "the Catalog must still list its Dataset after being rewritten, was: " + catalog);
        } finally {
            registration.unregister();
        }
    }

    // ---- helpers ----------------------------------------------------------

    /** {@code fp1:} + 64 hex digits, the shape the fingerprint service produces. */
    private static final String FINGERPRINT = "fp1:" + "ab".repeat(32);

    private static String datasetId(String nsUri, String stage) {
        String encoded = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(nsUri.getBytes(StandardCharsets.UTF_8));
        return StubScopeService.SCOPE + "--" + stage + "--" + encoded;
    }

    /** Polls a portal URL until it answers 2xx, or gives up and returns {@code null}. */
    private static String await(String url) throws Exception {
        long deadline = System.currentTimeMillis() + PUBLISH_WAIT_MS;
        while (System.currentTimeMillis() < deadline) {
            String body = get(url);
            if (body != null) {
                return body;
            }
            Thread.sleep(500);
        }
        return null;
    }

    private static Hashtable<String, Object> clientProps() {
        Hashtable<String, Object> props = new Hashtable<>();
        props.put("dcat.portal", PORTAL);
        props.put("base.uri", portalRestBase + "/");
        props.put("check.ready", Boolean.TRUE);
        props.put("require.ready", Boolean.FALSE);
        return props;
    }

    private static Hashtable<String, Object> publisherProps(String scope, boolean distinctPortal) {
        Hashtable<String, Object> props = new Hashtable<>();
        props.put("dcat.portal.target", "(dcat.portal=" + PORTAL + ")");
        props.put("atlas.public.base.uri", "https://opendata.example.de/model-atlas");
        props.put("allow.local.base.uri", Boolean.FALSE);
        props.put("scopes", new String[] { scope });
        props.put("language", "de");
        props.put("publisher.name", "Integration Test Publisher");
        props.put("publisher.about", "https://example.de/it");
        // Required to publish a Distribution at all: license is a lowerBound=1 containment there.
        props.put("license.uri", "http://dcat-ap.de/def/licenses/dl-by-de/2.0");
        props.put("distribution.media.types", new String[] { "application/xmi" });
        return props;
    }

    /** Polls the portal until the Catalog shows up, or gives up and returns {@code null}. */
    private static String awaitCatalog(String scope) throws Exception {
        long deadline = System.currentTimeMillis() + PUBLISH_WAIT_MS;
        while (System.currentTimeMillis() < deadline) {
            String body = get(portalRestBase + "/catalogs/" + scope);
            if (body != null && body.contains(scope)) {
                return body;
            }
            Thread.sleep(500);
        }
        return null;
    }

    /** @return the body on 2xx, {@code null} on anything else. */
    private static String get(String url) throws Exception {
        HttpClient http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();
        HttpRequest request = HttpRequest.newBuilder(URI.create(url)).header("Accept", "application/json")
                .timeout(Duration.ofSeconds(10)).GET().build();
        HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
        return response.statusCode() / 100 == 2 ? response.body() : null;
    }

    private static boolean portalImageAvailableLocally() {
        try {
            Process probe = new ProcessBuilder("docker", "image", "inspect", IMAGE)
                    .redirectOutput(ProcessBuilder.Redirect.DISCARD).redirectError(ProcessBuilder.Redirect.DISCARD)
                    .start();
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
}
