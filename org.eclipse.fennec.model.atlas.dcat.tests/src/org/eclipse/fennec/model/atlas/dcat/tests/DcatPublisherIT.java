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
import java.util.Collection;
import java.time.Duration;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.ServiceReference;
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

import org.apache.felix.hc.api.HealthCheck;
import org.eclipse.fennec.dcat.atlas.client.api.DcatAtlasClient;

import dcat.Catalog;
import dcat.DcatFactory;
import foaf.Agent;
import foaf.FoafFactory;
import rdf.PlainLiteral;
import rdf.RdfFactory;

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
    private static final String SCOPE_CATALOG_PID = "DcatScopeCatalog";
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

    // ---- D5: retirement, the debounce and the guards -----------------------

    @Test
    public void anUnregisteredPackageIsRetiredFromItsCatalog(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = CLIENT_PID,
                    name = PORTAL + "9", location = "?")) Configuration clientConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PUBLISHER_PID,
                    name = PORTAL + "9", location = "?")) Configuration publisherConfig,
            @InjectBundleContext BundleContext context) throws Exception {

        clientConfig.update(clientProps());
        publisherConfig.update(publisherProps(StubScopeService.SCOPE, false));

        String nsUri = "http://test.example.com/retired/1.0";
        String datasetId = datasetId(nsUri, PublishablePackages.FINAL_STAGE);
        ServiceRegistration<?> registration = PublishablePackages.register(context, nsUri,
                PublishablePackages.FINAL_STAGE, FINGERPRINT, true);
        assertNotNull(await(portalRestBase + "/datasets/" + datasetId), "setup: the Dataset should publish");
        assertTrue(awaitCatalogTerm(StubScopeService.SCOPE, datasetId, true),
                "setup: the Catalog should list the Dataset");

        // The flag cleared, the package deleted, a promotion out of a permitted stage: all three
        // reach the publisher as this one unbind.
        registration.unregister();

        assertTrue(awaitCatalogTerm(StubScopeService.SCOPE, datasetId, false),
                "the retired Dataset must stop being a member of its Catalog");
        // UNLINK is the default: discoverability goes, the resource stays. Nothing this publisher
        // does on an unbind should destroy what a portal-side editor may have added to it.
        assertNotNull(get(portalRestBase + "/datasets/" + datasetId),
                "UNLINK must leave the Dataset resource in place");
    }

    @Test
    public void aReRegisterInsideTheWindowKeepsTheDataset(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = CLIENT_PID,
                    name = PORTAL + "10", location = "?")) Configuration clientConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PUBLISHER_PID,
                    name = PORTAL + "10", location = "?")) Configuration publisherConfig,
            @InjectBundleContext BundleContext context) throws Exception {

        clientConfig.update(clientProps());
        publisherConfig.update(publisherProps(StubScopeService.SCOPE, false));

        String nsUri = "http://test.example.com/updated/1.0";
        String datasetId = datasetId(nsUri, PublishablePackages.FINAL_STAGE);
        ServiceRegistration<?> first = PublishablePackages.register(context, nsUri, PublishablePackages.FINAL_STAGE,
                FINGERPRINT, true);
        assertNotNull(await(portalRestBase + "/datasets/" + datasetId), "setup: the Dataset should publish");
        assertTrue(awaitCatalogTerm(StubScopeService.SCOPE, datasetId, true),
                "setup: the Catalog should list the Dataset");

        // This is what a content update looks like from here: DynamicEPackageRegistrationService
        // replaces a changed package by unregister-then-register. Without the debounce every edit
        // would briefly unpublish its own Dataset.
        first.unregister();
        ServiceRegistration<?> second = PublishablePackages.register(context, nsUri, PublishablePackages.FINAL_STAGE,
                "fp1:" + "cd".repeat(32), true);
        try {
            Thread.sleep(UNPUBLISH_DELAY_SECONDS * 2_000 + 4_000);
            assertNotNull(get(portalRestBase + "/datasets/" + datasetId),
                    "an update must not retire the Dataset it is updating");
            assertTrue(awaitCatalogTerm(StubScopeService.SCOPE, datasetId, true),
                    "an update must leave the Catalog membership standing");
        } finally {
            second.unregister();
        }
    }

    @Test
    public void deleteModeRemovesTheDatasetResource(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = CLIENT_PID,
                    name = PORTAL + "11", location = "?")) Configuration clientConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PUBLISHER_PID,
                    name = PORTAL + "11", location = "?")) Configuration publisherConfig,
            @InjectBundleContext BundleContext context) throws Exception {

        clientConfig.update(clientProps());
        Hashtable<String, Object> props = publisherProps(StubScopeService.SCOPE, false);
        props.put("unpublish.mode", "DELETE");
        publisherConfig.update(props);

        String nsUri = "http://test.example.com/deleted/1.0";
        String datasetId = datasetId(nsUri, PublishablePackages.FINAL_STAGE);
        ServiceRegistration<?> registration = PublishablePackages.register(context, nsUri,
                PublishablePackages.FINAL_STAGE, FINGERPRINT, true);
        assertNotNull(await(portalRestBase + "/datasets/" + datasetId), "setup: the Dataset should publish");
        assertTrue(awaitCatalogTerm(StubScopeService.SCOPE, datasetId, true),
                "setup: the Catalog should list the Dataset");

        registration.unregister();

        // DELETE is SINGLE, so the membership has to go first or the portal refuses the delete
        // while a Catalog still references it. Our own memberships are ours to drop; a foreign
        // referrer is what SINGLE is there to protect.
        assertTrue(awaitGone(portalRestBase + "/datasets/" + datasetId),
                "DELETE mode must remove the Dataset resource");
    }

    @Test
    public void aConfigUpdateRetiresNothing(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = CLIENT_PID,
                    name = PORTAL + "12", location = "?")) Configuration clientConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PUBLISHER_PID,
                    name = PORTAL + "12", location = "?")) Configuration publisherConfig,
            @InjectBundleContext BundleContext context) throws Exception {

        clientConfig.update(clientProps());
        publisherConfig.update(publisherProps(StubScopeService.SCOPE, false));

        String nsUri = "http://test.example.com/reactivated/1.0";
        String datasetId = datasetId(nsUri, PublishablePackages.FINAL_STAGE);
        ServiceRegistration<?> registration = PublishablePackages.register(context, nsUri,
                PublishablePackages.FINAL_STAGE, FINGERPRINT, true);
        try {
            assertNotNull(await(portalRestBase + "/datasets/" + datasetId), "setup: the Dataset should publish");

            // Re-activation unbinds every reference before it rebinds them. Our own @Deactivate is
            // "stop working", not a statement about the models — a redeploy or a config change must
            // not empty the catalogue, and retire.on.shutdown is false by default.
            publisherConfig.update(publisherProps(StubScopeService.SCOPE, false));
            Thread.sleep(UNPUBLISH_DELAY_SECONDS * 2_000 + 4_000);

            assertNotNull(get(portalRestBase + "/datasets/" + datasetId),
                    "a configuration update must retire nothing");
            assertTrue(awaitCatalogTerm(StubScopeService.SCOPE, datasetId, true),
                    "a configuration update must leave the Catalog membership standing");
        } finally {
            registration.unregister();
        }
    }

    // ---- D2a: the hierarchy and the link fan-out ---------------------------

    @Test
    public void aDatasetIsOneResourceInEveryDescendantCatalog(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = CLIENT_PID,
                    name = PORTAL + "13", location = "?")) Configuration clientConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PUBLISHER_PID,
                    name = PORTAL + "13", location = "?")) Configuration publisherConfig,
            @InjectBundleContext BundleContext context) throws Exception {

        clientConfig.update(clientProps());

        // h-root -> h-mid -> h-leaf, the plan's atlas -> jena -> nawerker.
        List<ServiceRegistration<?>> scopes = List.of(StubScopeService.register(context, "h-root", null),
                StubScopeService.register(context, "h-mid", "h-root"),
                StubScopeService.register(context, "h-leaf", "h-mid"));
        Hashtable<String, Object> props = publisherProps("h-root", false);
        props.put("scopes", new String[] { "h-root", "h-mid", "h-leaf" });
        publisherConfig.update(props);

        String nsUri = "http://test.example.com/inherited/1.0";
        String datasetId = datasetId("h-root", nsUri, PublishablePackages.FINAL_STAGE);
        ServiceRegistration<?> pkg = PublishablePackages.register(context, "h-root", nsUri,
                PublishablePackages.FINAL_STAGE, FINGERPRINT, true);
        try {
            assertNotNull(await(portalRestBase + "/datasets/" + datasetId),
                    "the defining scope should publish the Dataset");

            // One resource in three Catalogs, not three Datasets: the id derives from the defining
            // scope, so a harvester sees one model however many catalogues carry it.
            for (String scope : List.of("h-root", "h-mid", "h-leaf")) {
                assertTrue(awaitCatalogTerm(scope, datasetId, true),
                        "Catalog " + scope + " should list the inherited Dataset");
            }
            String list = get(portalRestBase + "/datasets");
            int occurrences = list.split("\"" + datasetId + "\"", -1).length - 1;
            assertTrue(occurrences <= 1, "inheritance must link, not copy; found " + occurrences + " Datasets");
        } finally {
            pkg.unregister();
            scopes.forEach(ServiceRegistration::unregister);
        }
    }

    @Test
    public void aScopeCreatedLaterInheritsItsAncestorsDatasets(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = CLIENT_PID,
                    name = PORTAL + "14", location = "?")) Configuration clientConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PUBLISHER_PID,
                    name = PORTAL + "14", location = "?")) Configuration publisherConfig,
            @InjectBundleContext BundleContext context) throws Exception {

        clientConfig.update(clientProps());

        ServiceRegistration<?> root = StubScopeService.register(context, "l-root", null);
        ServiceRegistration<?> mid = StubScopeService.register(context, "l-mid", "l-root");
        Hashtable<String, Object> props = publisherProps("l-root", false);
        props.put("scopes", new String[] { "l-root", "l-mid", "l-late" });
        publisherConfig.update(props);

        String nsUri = "http://test.example.com/ancestor/1.0";
        String datasetId = datasetId("l-root", nsUri, PublishablePackages.FINAL_STAGE);
        ServiceRegistration<?> pkg = PublishablePackages.register(context, "l-root", nsUri,
                PublishablePackages.FINAL_STAGE, FINGERPRINT, true);
        ServiceRegistration<?> late = null;
        try {
            assertTrue(awaitCatalogTerm("l-mid", datasetId, true), "setup: the existing descendant should have it");

            // The case that is easy to forget: nothing about the package changes when a new scope
            // appears, so the new Catalog has to pull its ancestors' Datasets in for itself.
            late = StubScopeService.register(context, "l-late", "l-mid");

            assertTrue(awaitCatalogTerm("l-late", datasetId, true),
                    "a scope created later must still get every ancestor's Datasets");
        } finally {
            if (late != null) {
                late.unregister();
            }
            pkg.unregister();
            mid.unregister();
            root.unregister();
        }
    }

    @Test
    public void retiringADatasetUnlinksItFromEveryCatalog(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = CLIENT_PID,
                    name = PORTAL + "15", location = "?")) Configuration clientConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PUBLISHER_PID,
                    name = PORTAL + "15", location = "?")) Configuration publisherConfig,
            @InjectBundleContext BundleContext context) throws Exception {

        clientConfig.update(clientProps());

        ServiceRegistration<?> root = StubScopeService.register(context, "r-root", null);
        ServiceRegistration<?> leaf = StubScopeService.register(context, "r-leaf", "r-root");
        Hashtable<String, Object> props = publisherProps("r-root", false);
        props.put("scopes", new String[] { "r-root", "r-leaf" });
        publisherConfig.update(props);

        String nsUri = "http://test.example.com/retired-inherited/1.0";
        String datasetId = datasetId("r-root", nsUri, PublishablePackages.FINAL_STAGE);
        ServiceRegistration<?> pkg = PublishablePackages.register(context, "r-root", nsUri,
                PublishablePackages.FINAL_STAGE, FINGERPRINT, true);
        try {
            assertTrue(awaitCatalogTerm("r-leaf", datasetId, true), "setup: the descendant should list it");

            pkg.unregister();

            // Retirement mirrors publication exactly. A missed descendant leaves a Catalog
            // advertising a Dataset that is gone, which is worse than never having linked it.
            assertTrue(awaitCatalogTerm("r-root", datasetId, false), "the defining scope should drop it");
            assertTrue(awaitCatalogTerm("r-leaf", datasetId, false), "the descendant should drop it too");
        } finally {
            leaf.unregister();
            root.unregister();
        }
    }

    // ---- D1a: adopted, configured, derived ---------------------------------

    @Test
    public void anAdoptedCatalogGetsLinksAndIsNeverRewritten(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = CLIENT_PID,
                    name = PORTAL + "17", location = "?")) Configuration clientConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PUBLISHER_PID,
                    name = PORTAL + "17", location = "?")) Configuration publisherConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = SCOPE_CATALOG_PID,
                    name = PORTAL + "17", location = "?")) Configuration catalogConfig,
            @InjectBundleContext BundleContext context) throws Exception {

        clientConfig.update(clientProps());

        // Somebody else's Catalog, created before this publisher ever sees the scope. Written
        // through the real client, so what the publisher meets is a genuine portal resource.
        String foreignId = "foreign-catalog";
        String foreignTitle = "Somebody Else's Catalogue";
        awaitClient().registerCatalog(foreignId, foreignCatalog(foreignTitle));

        ServiceRegistration<?> scope = StubScopeService.register(context, "a-scope", null);
        Hashtable<String, Object> catalogProps = new Hashtable<>();
        catalogProps.put("scope", "a-scope");
        catalogProps.put("catalog.id", foreignId);
        catalogProps.put("catalog.adopt", Boolean.TRUE);
        catalogConfig.update(catalogProps);

        Hashtable<String, Object> props = publisherProps("a-scope", false);
        props.put("scopes", new String[] { "a-scope" });
        publisherConfig.update(props);

        String nsUri = "http://test.example.com/adopted/1.0";
        String datasetId = datasetId("a-scope", nsUri, PublishablePackages.FINAL_STAGE);
        ServiceRegistration<?> pkg = PublishablePackages.register(context, "a-scope", nsUri,
                PublishablePackages.FINAL_STAGE, FINGERPRINT, true);
        try {
            assertTrue(awaitCatalogTerm(foreignId, datasetId, true),
                    "Datasets must be linked into an adopted Catalog: that operation is additive");

            String catalog = get(portalRestBase + "/catalogs/" + foreignId);
            // A PUT replaces, and dcat:dataset membership lives on the Catalog — so re-registering
            // an adopted Catalog would drop every link it holds, other publishers' included.
            assertTrue(catalog.contains(foreignTitle),
                    "an adopted Catalog must keep its own title, was: " + catalog);
            assertTrue(get(portalRestBase + "/catalogs/a-scope") == null,
                    "adopting a Catalog must not also create one under the scope name");
        } finally {
            pkg.unregister();
            scope.unregister();
        }
    }

    @Test
    public void aMissingAdoptedCatalogRefusesTheScopeInsteadOfCreatingIt(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = CLIENT_PID,
                    name = PORTAL + "18", location = "?")) Configuration clientConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PUBLISHER_PID,
                    name = PORTAL + "18", location = "?")) Configuration publisherConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = SCOPE_CATALOG_PID,
                    name = PORTAL + "18", location = "?")) Configuration catalogConfig,
            @InjectBundleContext BundleContext context) throws Exception {

        clientConfig.update(clientProps());

        ServiceRegistration<?> scope = StubScopeService.register(context, "m-scope", null);
        Hashtable<String, Object> catalogProps = new Hashtable<>();
        catalogProps.put("scope", "m-scope");
        catalogProps.put("catalog.id", "not-in-this-portal");
        catalogProps.put("catalog.adopt", Boolean.TRUE);
        catalogConfig.update(catalogProps);

        Hashtable<String, Object> props = publisherProps("m-scope", false);
        props.put("scopes", new String[] { "m-scope" });
        publisherConfig.update(props);

        String nsUri = "http://test.example.com/orphan/1.0";
        String datasetId = datasetId("m-scope", nsUri, PublishablePackages.FINAL_STAGE);
        ServiceRegistration<?> pkg = PublishablePackages.register(context, "m-scope", nsUri,
                PublishablePackages.FINAL_STAGE, FINGERPRINT, true);
        try {
            Thread.sleep(6_000);

            // The operator asserted that id exists. Minting a Catalog under an id in somebody
            // else's portal is the one failure mode with no clean recovery, so the scope is refused.
            assertTrue(get(portalRestBase + "/catalogs/not-in-this-portal") == null,
                    "a missing adopted Catalog must not be created");
            assertTrue(get(portalRestBase + "/catalogs/m-scope") == null,
                    "nor may the scope fall back to a Catalog of its own");
            assertTrue(get(portalRestBase + "/datasets/" + datasetId) == null,
                    "a refused scope publishes nothing: its Catalog is the precondition for its Datasets");
        } finally {
            pkg.unregister();
            scope.unregister();
        }
    }

    @Test
    public void aConfiguredCatalogCarriesItsOwnTitle(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = CLIENT_PID,
                    name = PORTAL + "19", location = "?")) Configuration clientConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PUBLISHER_PID,
                    name = PORTAL + "19", location = "?")) Configuration publisherConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = SCOPE_CATALOG_PID,
                    name = PORTAL + "19", location = "?")) Configuration catalogConfig,
            @InjectBundleContext BundleContext context) throws Exception {

        clientConfig.update(clientProps());

        ServiceRegistration<?> scope = StubScopeService.register(context, "c-scope", null);
        Hashtable<String, Object> catalogProps = new Hashtable<>();
        catalogProps.put("scope", "c-scope");
        catalogProps.put("catalog.id", "verkehr-jena");
        catalogProps.put("catalog.title", "Verkehrsmodelle Jena");
        catalogProps.put("catalog.description", "Datenmodelle des Verkehrsbetriebs");
        catalogProps.put("catalog.publisher.name", "Verkehrsbetrieb Jena");
        catalogConfig.update(catalogProps);

        Hashtable<String, Object> props = publisherProps("c-scope", false);
        props.put("scopes", new String[] { "c-scope" });
        publisherConfig.update(props);

        try {
            String catalog = await(portalRestBase + "/catalogs/verkehr-jena");
            assertNotNull(catalog, "a configured Catalog is ours to write, under its configured id");
            // Precedence: the configuration beats the scope, which beats the publisher's defaults.
            assertTrue(catalog.contains("Verkehrsmodelle Jena"), "configured title should win: " + catalog);
            assertTrue(catalog.contains("Datenmodelle des Verkehrsbetriebs"),
                    "configured description should win: " + catalog);
            assertTrue(catalog.contains("Verkehrsbetrieb Jena"), "configured publisher should win: " + catalog);
            assertTrue(!catalog.contains(StubScopeService.DESCRIPTION),
                    "the scope's own description must not survive a configured one: " + catalog);
            assertTrue(get(portalRestBase + "/catalogs/c-scope") == null,
                    "the configured id replaces the scope name rather than adding to it");
        } finally {
            scope.unregister();
        }
    }

    // ---- the media-type allowlist ------------------------------------------

    @Test
    public void narrowingTheAllowlistRemovesTheDistributionItDropped(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = CLIENT_PID,
                    name = PORTAL + "20", location = "?")) Configuration clientConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PUBLISHER_PID,
                    name = PORTAL + "20", location = "?")) Configuration publisherConfig,
            @InjectBundleContext BundleContext context) throws Exception {

        clientConfig.update(clientProps());
        Hashtable<String, Object> both = publisherProps(StubScopeService.SCOPE, false);
        both.put("distribution.media.types", new String[] { "application/xmi", "application/json" });
        publisherConfig.update(both);

        String nsUri = "http://test.example.com/formats/1.0";
        String datasetId = datasetId(nsUri, PublishablePackages.FINAL_STAGE);
        ServiceRegistration<?> pkg = PublishablePackages.register(context, nsUri, PublishablePackages.FINAL_STAGE,
                FINGERPRINT, true);
        try {
            String dataset = await(portalRestBase + "/datasets/" + datasetId);
            assertNotNull(dataset, "setup: the Dataset should publish");
            assertTrue(dataset.contains("application/json"),
                    "setup: both allowed formats should be advertised, was: " + dataset);

            // The operator narrows the allowlist. The content did not change, so the fingerprint
            // check would skip the write — and the catalogue would go on advertising a format the
            // configuration no longer permits.
            Hashtable<String, Object> xmiOnly = publisherProps(StubScopeService.SCOPE, false);
            xmiOnly.put("distribution.media.types", new String[] { "application/xmi" });
            publisherConfig.update(xmiOnly);

            assertTrue(awaitDatasetTerm(datasetId, "application/json", false),
                    "narrowing distribution.media.types must remove the Distribution it dropped");
            String narrowed = get(portalRestBase + "/datasets/" + datasetId);
            assertTrue(narrowed.contains("application/xmi"), "the remaining format must stay: " + narrowed);
        } finally {
            pkg.unregister();
        }
    }

    @Test
    public void aCodecComingUpLaterAddsItsDistribution(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = CLIENT_PID,
                    name = PORTAL + "21", location = "?")) Configuration clientConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PUBLISHER_PID,
                    name = PORTAL + "21", location = "?")) Configuration publisherConfig,
            @InjectBundleContext BundleContext context) throws Exception {

        clientConfig.update(clientProps());

        // The runtime can serve only XMI when the publisher starts, as if the JSON codec had not
        // come up yet — while the allowlist permits both.
        try (MutableSupportedMediatype runtime = MutableSupportedMediatype.register(context,
                List.of("application/xmi"))) {
            Hashtable<String, Object> props = publisherProps(StubScopeService.SCOPE, false);
            props.put("distribution.media.types", new String[] { "application/xmi", "application/json" });
            publisherConfig.update(props);

            String nsUri = "http://test.example.com/latecodec/1.0";
            String datasetId = datasetId(nsUri, PublishablePackages.FINAL_STAGE);
            ServiceRegistration<?> pkg = PublishablePackages.register(context, nsUri,
                    PublishablePackages.FINAL_STAGE, FINGERPRINT, true);
            try {
                assertNotNull(await(portalRestBase + "/datasets/" + datasetId), "setup: the Dataset should publish");
                assertTrue(awaitDatasetTerm(datasetId, "application/json", false),
                        "setup: a format the runtime cannot serve must not be advertised");

                // The codec arrives. The service does not rebind — its properties are refreshed —
                // and that is the only signal there is. Nothing about the package changes: no
                // upload, no configuration touch, no restart.
                runtime.report(List.of("application/xmi", "application/json"));

                assertTrue(awaitDatasetTerm(datasetId, "application/json", true),
                        "a format becoming available must reach an already-published Dataset");
                String widened = get(portalRestBase + "/datasets/" + datasetId);
                assertTrue(widened.contains("application/xmi"), "and must not cost the existing one: " + widened);
            } finally {
                pkg.unregister();
            }
        }
    }

    // ---- D6: retry and the health check ------------------------------------

    @Test
    public void anUnreachablePortalIsRetriedAndReported(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = CLIENT_PID,
                    name = PORTAL + "22", location = "?")) Configuration clientConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PUBLISHER_PID,
                    name = PORTAL + "22", location = "?")) Configuration publisherConfig,
            @InjectBundleContext BundleContext context) throws Exception {

        // A portal nothing is listening to. Before D6 this produced one log line per attempt and
        // then silence: the publish was dropped, and the catalogue stayed behind whatever the atlas
        // served until something unrelated triggered another write.
        Hashtable<String, Object> deadPortal = clientProps();
        deadPortal.put("dcat.portal", PORTAL + "22");
        deadPortal.put("base.uri", "http://localhost:1/rest/");
        clientConfig.update(deadPortal);

        String checkName = "DCAT unreachable";
        Hashtable<String, Object> props = publisherProps(StubScopeService.SCOPE, false);
        props.put("dcat.portal.target", "(dcat.portal=" + PORTAL + "22)");
        props.put("retry.initial.delay.seconds", Integer.valueOf(2));
        props.put("retry.max.attempts", Integer.valueOf(20));
        props.put("hc.name", checkName);
        publisherConfig.update(props);

        String report = awaitHealthCheck(context, checkName, "retrying");
        assertNotNull(report, "an unreachable portal should be reported as retrying, not silently dropped");
        assertTrue(report.contains("publishing the Catalog for scope " + StubScopeService.SCOPE),
                "the report should name what is failing, was: " + report);
    }

    @Test
    public void theHealthCheckIsGreenWhenPublishingWorks(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = CLIENT_PID,
                    name = PORTAL + "23", location = "?")) Configuration clientConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PUBLISHER_PID,
                    name = PORTAL + "23", location = "?")) Configuration publisherConfig,
            @InjectBundleContext BundleContext context) throws Exception {

        clientConfig.update(clientProps());
        String checkName = "DCAT healthy";
        Hashtable<String, Object> props = publisherProps(StubScopeService.SCOPE, false);
        props.put("hc.name", checkName);
        publisherConfig.update(props);
        assertNotNull(awaitCatalog(StubScopeService.SCOPE), "setup: the Catalog should publish");

        assertNotNull(awaitHealthCheck(context, checkName, "No failing publications"),
                "a working publisher should report no failures");
        // Not tagged readiness, and OK: a catalogue is not part of what makes this atlas usable.
        assertTrue(healthCheck(context, checkName).execute().isOk(),
                "nothing here should take the atlas out of a load balancer");
    }

    @Test
    public void aPermanentlyUnpublishableDatasetStaysInTheHealthReport(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = CLIENT_PID,
                    name = PORTAL + "40", location = "?")) Configuration clientConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PUBLISHER_PID,
                    name = PORTAL + "40", location = "?")) Configuration publisherConfig,
            @InjectBundleContext BundleContext context) throws Exception {

        // #231: writeDataset used to catch the failure, record it as permanent and return null.
        // The promise then resolved successfully, which called RetryQueue.succeeded() and deleted
        // the entry the failure had just written — so a Dataset that could never be published
        // reported as healthy, which is precisely the case the ledger exists for.
        clientConfig.update(clientProps());
        String checkName = "DCAT permanent";
        Hashtable<String, Object> props = publisherProps(StubScopeService.SCOPE, false);
        // A Distribution cannot be built without a licence: license is a lowerBound=1 containment.
        // The mapper raises IllegalStateException, which no retry can fix — an operator must act.
        props.remove("license.uri");
        props.put("hc.name", checkName);
        publisherConfig.update(props);

        String nsUri = "http://test.example.com/nolicence/1.0";
        ServiceRegistration<?> registration = PublishablePackages.register(context, nsUri,
                PublishablePackages.FINAL_STAGE, FINGERPRINT, true);
        try {
            String report = awaitHealthCheck(context, checkName, "publishing Dataset");
            assertNotNull(report, "a Dataset that can never be published must stay in the health report");
            assertTrue(report.contains("license"),
                    "the report should say what an operator has to fix, was: " + report);
        } finally {
            registration.unregister();
        }
    }

    // ---- a scope going away ------------------------------------------------

    @Test
    public void aVanishedScopeTakesItsCatalogAndItsOwnDatasetsOnly(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = CLIENT_PID,
                    name = PORTAL + "24", location = "?")) Configuration clientConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PUBLISHER_PID,
                    name = PORTAL + "24", location = "?")) Configuration publisherConfig,
            @InjectBundleContext BundleContext context) throws Exception {

        clientConfig.update(clientProps());

        // v-parent -> v-child -> v-grandchild, and v-child is the one that goes away.
        ServiceRegistration<?> parent = StubScopeService.register(context, "v-parent", null);
        ServiceRegistration<?> child = StubScopeService.register(context, "v-child", "v-parent");
        ServiceRegistration<?> grandchild = StubScopeService.register(context, "v-grandchild", "v-child");
        Hashtable<String, Object> props = publisherProps("v-parent", false);
        props.put("scopes", new String[] { "v-parent", "v-child", "v-grandchild" });
        props.put("unpublish.mode", "DELETE");
        publisherConfig.update(props);

        // A is defined by the scope that disappears; B is defined by its parent and only inherited.
        String nsUriA = "http://test.example.com/vanish-own/1.0";
        String nsUriB = "http://test.example.com/vanish-inherited/1.0";
        String datasetA = datasetId("v-child", nsUriA, PublishablePackages.FINAL_STAGE);
        String datasetB = datasetId("v-parent", nsUriB, PublishablePackages.FINAL_STAGE);
        ServiceRegistration<?> packageA = PublishablePackages.register(context, "v-child", nsUriA,
                PublishablePackages.FINAL_STAGE, FINGERPRINT, true);
        ServiceRegistration<?> packageB = PublishablePackages.register(context, "v-parent", nsUriB,
                PublishablePackages.FINAL_STAGE, FINGERPRINT, true);
        try {
            assertNotNull(await(portalRestBase + "/datasets/" + datasetA), "setup: A should publish");
            assertNotNull(await(portalRestBase + "/datasets/" + datasetB), "setup: B should publish");
            assertTrue(awaitCatalogTerm("v-child", datasetA, true), "setup: v-child should list its own Dataset");
            assertTrue(awaitCatalogTerm("v-grandchild", datasetA, true), "setup: the descendant should list A");
            assertTrue(awaitCatalogTerm("v-child", datasetB, true), "setup: v-child should list the inherited B");

            // The scope's configuration is deleted: its scope service and its packages go with it.
            packageA.unregister();
            child.unregister();

            assertTrue(awaitGone(portalRestBase + "/catalogs/v-child"), "the vanished scope's Catalog should go");
            // A was linked into a descendant's Catalog too, so it has to be unlinked there before it
            // can be deleted — and a Catalog must never be left advertising a Dataset that is gone.
            assertTrue(awaitCatalogTerm("v-grandchild", datasetA, false),
                    "the descendant Catalog must stop listing the deleted Dataset");
            assertTrue(awaitGone(portalRestBase + "/datasets/" + datasetA),
                    "a Dataset the vanished scope defined should be deleted");

            // B belongs to the parent scope. dcat:dataset is a non-containment reference, so losing
            // the Catalog that linked it says nothing about the Dataset itself.
            assertNotNull(get(portalRestBase + "/datasets/" + datasetB),
                    "an inherited Dataset must survive the scope that merely listed it");
            assertTrue(awaitCatalogTerm("v-parent", datasetB, true),
                    "and must still be listed by the scope that defines it");
        } finally {
            packageB.unregister();
            grandchild.unregister();
            parent.unregister();
        }
    }

    @Test
    public void unlinkModeEmptiesAVanishedScopeCatalogWithoutDeletingIt(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = CLIENT_PID,
                    name = PORTAL + "25", location = "?")) Configuration clientConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PUBLISHER_PID,
                    name = PORTAL + "25", location = "?")) Configuration publisherConfig,
            @InjectBundleContext BundleContext context) throws Exception {

        clientConfig.update(clientProps());

        ServiceRegistration<?> parent = StubScopeService.register(context, "u-parent", null);
        ServiceRegistration<?> child = StubScopeService.register(context, "u-child", "u-parent");
        Hashtable<String, Object> props = publisherProps("u-parent", false);
        props.put("scopes", new String[] { "u-parent", "u-child" });
        publisherConfig.update(props);

        String nsUri = "http://test.example.com/unlink-inherited/1.0";
        String datasetId = datasetId("u-parent", nsUri, PublishablePackages.FINAL_STAGE);
        ServiceRegistration<?> pkg = PublishablePackages.register(context, "u-parent", nsUri,
                PublishablePackages.FINAL_STAGE, FINGERPRINT, true);
        try {
            assertTrue(awaitCatalogTerm("u-child", datasetId, true), "setup: the child should list the inherited one");

            child.unregister();

            // UNLINK deletes nothing, so the Catalog resource stays — but it must stop advertising
            // models for a scope that no longer exists.
            assertTrue(awaitCatalogTerm("u-child", datasetId, false),
                    "the vanished scope's Catalog must stop listing our Datasets");
            assertNotNull(get(portalRestBase + "/catalogs/u-child"),
                    "UNLINK must leave the Catalog resource in place");
            assertNotNull(get(portalRestBase + "/datasets/" + datasetId),
                    "and must not touch a Dataset another scope defines");
        } finally {
            pkg.unregister();
            parent.unregister();
        }
    }

    // ---- the metadata whiteboard and the configuration guard ----------------

    @Test
    public void aMetadataSourceOverridingOneFieldKeepsTheConfiguredRest(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = CLIENT_PID,
                    name = PORTAL + "26", location = "?")) Configuration clientConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PUBLISHER_PID,
                    name = PORTAL + "26", location = "?")) Configuration publisherConfig,
            @InjectBundleContext BundleContext context) throws Exception {

        clientConfig.update(clientProps());

        // Registered before the publisher activates, which is the ordinary case for a whiteboard.
        ServiceRegistration<?> metadata = TitleOnlyMetadataSource.register(context);
        publisherConfig.update(publisherProps(StubScopeService.SCOPE, false));

        String nsUri = "http://test.example.com/partialmetadata/1.0";
        String datasetId = datasetId(nsUri, PublishablePackages.FINAL_STAGE);
        ServiceRegistration<?> pkg = PublishablePackages.register(context, nsUri, PublishablePackages.FINAL_STAGE,
                FINGERPRINT, true);
        try {
            String dataset = await(portalRestBase + "/datasets/" + datasetId);
            assertNotNull(dataset, "a partial metadata source must not stop the Dataset publishing");
            assertTrue(dataset.contains(TitleOnlyMetadataSource.TITLE),
                    "the registered source should win where it answers, was: " + dataset);
            // The regression: the source says nothing about the licence, and license.uri IS
            // configured — but substituting the source for the configured defaults made
            // toDistribution throw, which left an orphan Dataset with no Distribution in no Catalog
            // and recorded the failure as permanent so nothing retried it.
            assertTrue(dataset.contains("application/xmi"),
                    "the configured licence must still let a Distribution be built, was: " + dataset);
            assertTrue(awaitCatalogTerm(StubScopeService.SCOPE, datasetId, true),
                    "and the Dataset must reach its Catalog");
        } finally {
            pkg.unregister();
            metadata.unregister();
        }
    }

    @Test
    public void anUninterpolatedConfigurationRefusesToPublish(
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = CLIENT_PID,
                    name = PORTAL + "27", location = "?")) Configuration clientConfig,
            @InjectConfiguration(withFactoryConfig = @WithFactoryConfiguration(factoryPid = PUBLISHER_PID,
                    name = PORTAL + "27", location = "?")) Configuration publisherConfig,
            @InjectBundleContext BundleContext context) throws Exception {

        clientConfig.update(clientProps());

        // What an unset environment variable leaves behind: Felix's interpolation plugin does not
        // fail, it leaves the placeholder as the value. Published, that puts "$[env:…]" into a
        // public catalogue as the name of whoever governs the data.
        Hashtable<String, Object> props = publisherProps("placeholder-scope", false);
        props.put("scopes", new String[] { "placeholder-scope" });
        props.put("publisher.name", "$[env:DCAT_PUBLISHER_NAME]");
        publisherConfig.update(props);

        ServiceRegistration<?> scope = StubScopeService.register(context, "placeholder-scope", null);
        try {
            Thread.sleep(4_000);

            // Activation throws, so nothing is published at all — the same refusal shape as a
            // loopback base URI.
            assertTrue(get(portalRestBase + "/catalogs/placeholder-scope") == null,
                    "an uninterpolated configuration must publish nothing");
        } finally {
            scope.unregister();
        }
    }

    // ---- helpers ----------------------------------------------------------

    /** Polls one health check until its report mentions {@code term}, and returns the report. */
    private static String awaitHealthCheck(BundleContext context, String name, String term) throws Exception {
        long deadline = System.currentTimeMillis() + PUBLISH_WAIT_MS;
        while (System.currentTimeMillis() < deadline) {
            HealthCheck check = healthCheck(context, name);
            if (check != null) {
                StringBuilder report = new StringBuilder();
                check.execute().forEach(entry -> report.append(entry.getStatus()).append(' ')
                        .append(entry.getMessage()).append('\n'));
                if (report.indexOf(term) >= 0) {
                    return report.toString();
                }
            }
            Thread.sleep(500);
        }
        return null;
    }

    private static HealthCheck healthCheck(BundleContext context, String name) throws Exception {
        Collection<ServiceReference<HealthCheck>> references = context.getServiceReferences(HealthCheck.class,
                "(hc.name=" + name + ")");
        return references.isEmpty() ? null : context.getService(references.iterator().next());
    }

    /** Waits for the portal client this test configured, and hands it over. */
    private static DcatAtlasClient awaitClient() throws Exception {
        BundleContext context = FrameworkUtil.getBundle(DcatPublisherIT.class).getBundleContext();
        long deadline = System.currentTimeMillis() + PUBLISH_WAIT_MS;
        while (System.currentTimeMillis() < deadline) {
            Collection<ServiceReference<DcatAtlasClient>> references = context
                    .getServiceReferences(DcatAtlasClient.class, "(dcat.portal=" + PORTAL + ")");
            if (!references.isEmpty()) {
                return context.getService(references.iterator().next());
            }
            Thread.sleep(200);
        }
        throw new IllegalStateException("no DcatAtlasClient for portal " + PORTAL);
    }

    /**
     * A Catalog standing in for one another publisher owns. It clears the portal's write floor —
     * title, description and a contained Agent with a name — because otherwise it would not be
     * stored at all, and the test would be adopting nothing.
     */
    private static Catalog foreignCatalog(String title) {
        Catalog catalog = DcatFactory.eINSTANCE.createCatalog();
        catalog.getTitle().add(literal(title));
        catalog.getDescription().add(literal("A catalogue this atlas did not create"));
        Agent publisher = FoafFactory.eINSTANCE.createAgent();
        publisher.getName().add(literal("Another Publisher"));
        catalog.setPublisher(publisher);
        return catalog;
    }

    private static PlainLiteral literal(String value) {
        PlainLiteral literal = RdfFactory.eINSTANCE.createPlainLiteral();
        literal.setValue(value);
        literal.setLang("de");
        return literal;
    }

    /** The debounce window the D5 tests configure, short enough to assert against. */
    private static final int UNPUBLISH_DELAY_SECONDS = 2;

    /**
     * Polls the scope's Catalog until {@code term} is present (or absent, when {@code present} is
     * false). Membership is what retirement changes, and it changes asynchronously.
     */
    private static boolean awaitCatalogTerm(String catalogId, String term, boolean present) throws Exception {
        long deadline = System.currentTimeMillis() + PUBLISH_WAIT_MS;
        while (System.currentTimeMillis() < deadline) {
            String body = get(portalRestBase + "/catalogs/" + catalogId);
            if (body != null && body.contains(term) == present) {
                return true;
            }
            Thread.sleep(500);
        }
        return false;
    }

    /** Polls a Dataset until {@code term} is present, or absent when {@code present} is false. */
    private static boolean awaitDatasetTerm(String datasetId, String term, boolean present) throws Exception {
        long deadline = System.currentTimeMillis() + PUBLISH_WAIT_MS;
        while (System.currentTimeMillis() < deadline) {
            String body = get(portalRestBase + "/datasets/" + datasetId);
            if (body != null && body.contains(term) == present) {
                return true;
            }
            Thread.sleep(500);
        }
        return false;
    }

    /** Polls until the URL stops answering 2xx. */
    private static boolean awaitGone(String url) throws Exception {
        long deadline = System.currentTimeMillis() + PUBLISH_WAIT_MS;
        while (System.currentTimeMillis() < deadline) {
            if (get(url) == null) {
                return true;
            }
            Thread.sleep(500);
        }
        return false;
    }

    /** {@code fp1:} + 64 hex digits, the shape the fingerprint service produces. */
    private static final String FINGERPRINT = "fp1:" + "ab".repeat(32);

    private static String datasetId(String nsUri, String stage) {
        return datasetId(StubScopeService.SCOPE, nsUri, stage);
    }

    /** The id is derived from the scope that <em>defines</em> the package, never from an inheritor. */
    private static String datasetId(String scope, String nsUri, String stage) {
        String encoded = Base64.getUrlEncoder().withoutPadding()
                .encodeToString(nsUri.getBytes(StandardCharsets.UTF_8));
        return scope + "--" + stage + "--" + encoded;
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
        // Short enough to assert against; the shipped default is 30s.
        props.put("unpublish.delay.seconds", UNPUBLISH_DELAY_SECONDS);
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
