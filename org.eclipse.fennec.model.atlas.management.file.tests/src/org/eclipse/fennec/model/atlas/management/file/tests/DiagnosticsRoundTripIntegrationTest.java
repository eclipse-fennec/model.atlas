/*
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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.management.file.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticChange;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticStatus;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations.StorageSetup;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * Diagnostics round trip through the file backend (issue #292): a diagnostic tree written
 * with {@code updateDiagnostics} comes back from a fresh {@code retrieveMetadata}, complete
 * with children, history and every attribute; the write leaves content hash, version and
 * {@code lastChangeTime} alone; a later {@code updateMetadata} keeps the diagnostics; and a
 * producer's replace touches only its own roots.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@DisplayName("Diagnostics round trip through the file backend")
public class DiagnosticsRoundTripIntegrationTest {

    private static final String SCOPE = "diag_scope";
    private static final String REGISTRY = "diag_registry";
    private static final String STAGE = "release";
    private static final Instant T0 = Instant.parse("2026-09-01T10:00:00Z");

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        System.setProperty("tempDir", tempDir.toString());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    @StorageSetup
    @DisplayName("A diagnostic tree survives store, reload and a later metadata update")
    void treeRoundTrips(
            @InjectService(cardinality = 0, filter = "(storage.backend=file)") ServiceAware<EObjectStorageService> aware)
            throws Exception {
        EObjectStorageService<EObject> storage = (EObjectStorageService<EObject>) aware.waitForService(5000L);
        assertNotNull(storage);
        String objectId = "diag-" + System.nanoTime();
        ObjectMetadata stored = store(storage, objectId);
        String contentHash = stored.getContentHash();
        Instant lastChange = stored.getLastChangeTime();

        Diagnostic root = diagnostic("compile-failed", null, DiagnosticSeverity.ERROR, "does not compile");
        root.setSource("org.eclipse.fennec.m2x.qvto");
        root.setCategory("compile");
        Diagnostic child = diagnostic("unresolved-import", "line:2:col:8", DiagnosticSeverity.ERROR,
                "text.Case is not in this stage");
        root.getChildren().add(child);
        DiagnosticChange change = ManagementFactory.eINSTANCE.createDiagnosticChange();
        change.setChangeTime(T0);
        change.setChangedBy("qvt");
        change.setOldSeverity(DiagnosticSeverity.WARNING);
        change.setNewSeverity(DiagnosticSeverity.ERROR);
        change.setOldStatus(DiagnosticStatus.OPEN);
        change.setNewStatus(DiagnosticStatus.OPEN);
        change.setReason("escalated after the library was removed");
        root.getHistory().add(change);

        ObjectMetadata written = storage.updateDiagnostics(SCOPE, REGISTRY, STAGE, objectId, "qvt", List.of(root))
                .getValue();
        assertNotNull(written);
        assertEquals(1, written.getDiagnostics().size());

        // a FRESH load, not the instance the write returned
        ObjectMetadata reloaded = storage.retrieveMetadata(SCOPE, REGISTRY, STAGE, objectId).getValue();
        assertEquals(1, reloaded.getDiagnostics().size(), "the root came back");
        Diagnostic r = reloaded.getDiagnostics().get(0);
        assertNotNull(r.getId(), "the storage minted the id");
        assertEquals("qvt", r.getProducer());
        assertEquals("org.eclipse.fennec.m2x.qvto", r.getSource());
        assertEquals("compile-failed", r.getCode());
        assertEquals(DiagnosticSeverity.ERROR, r.getSeverity());
        assertEquals("does not compile", r.getMessage());
        assertEquals("compile", r.getCategory());
        assertEquals(DiagnosticStatus.OPEN, r.getStatus(), "the default status");
        assertNotNull(r.getCreatedTime());
        assertEquals(0L, r.getVersion());
        assertEquals(1, r.getChildren().size(), "the child came back");
        Diagnostic c = r.getChildren().get(0);
        assertEquals("qvt", c.getProducer(), "the child inherited the producer");
        assertEquals("line:2:col:8", c.getTarget());
        assertEquals("text.Case is not in this stage", c.getMessage());
        assertEquals(1, r.getHistory().size(), "the history came back");
        DiagnosticChange h = r.getHistory().get(0);
        assertEquals(T0, h.getChangeTime());
        assertEquals("qvt", h.getChangedBy());
        assertEquals(DiagnosticSeverity.WARNING, h.getOldSeverity());
        assertEquals(DiagnosticSeverity.ERROR, h.getNewSeverity());
        assertEquals("escalated after the library was removed", h.getReason());

        // diagnostics are metadata, not content
        assertEquals(contentHash, reloaded.getContentHash(), "the content hash is untouched");
        assertEquals("1.0.0", reloaded.getVersion(), "the version is untouched");
        assertEquals(lastChange, reloaded.getLastChangeTime(), "lastChangeTime is untouched");

        // a metadata update of other fields keeps the diagnostics
        ObjectMetadata update = ManagementFactory.eINSTANCE.createObjectMetadata();
        update.setStatus(ObjectStatus.APPROVED);
        assertTrue(storage.updateMetadata(SCOPE, REGISTRY, STAGE, objectId, update).getValue());
        ObjectMetadata afterUpdate = storage.retrieveMetadata(SCOPE, REGISTRY, STAGE, objectId).getValue();
        assertEquals(ObjectStatus.APPROVED, afterUpdate.getStatus());
        assertEquals(1, afterUpdate.getDiagnostics().size(), "updateMetadata does not drop the diagnostics");
        assertEquals(1, afterUpdate.getDiagnostics().get(0).getChildren().size());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Test
    @StorageSetup
    @DisplayName("A producer replaces its own roots and leaves another producer's alone; empty clears")
    void replaceIsScopedToTheProducer(
            @InjectService(cardinality = 0, filter = "(storage.backend=file)") ServiceAware<EObjectStorageService> aware)
            throws Exception {
        EObjectStorageService<EObject> storage = (EObjectStorageService<EObject>) aware.waitForService(5000L);
        String objectId = "diag-" + System.nanoTime();
        store(storage, objectId);

        storage.updateDiagnostics(SCOPE, REGISTRY, STAGE, objectId, "gdpr",
                List.of(diagnostic("personal-data", "//Person/birthDate", DiagnosticSeverity.WARNING, "pii")))
                .getValue();
        storage.updateDiagnostics(SCOPE, REGISTRY, STAGE, objectId, "qvt",
                List.of(diagnostic("compile-failed", null, DiagnosticSeverity.ERROR, "v1"),
                        diagnostic("deprecated-import", null, DiagnosticSeverity.INFO, "goes away")))
                .getValue();
        assertEquals(3, storage.retrieveMetadata(SCOPE, REGISTRY, STAGE, objectId).getValue().getDiagnostics().size());

        storage.updateDiagnostics(SCOPE, REGISTRY, STAGE, objectId, "qvt",
                List.of(diagnostic("compile-failed", null, DiagnosticSeverity.ERROR, "v2"))).getValue();
        ObjectMetadata reloaded = storage.retrieveMetadata(SCOPE, REGISTRY, STAGE, objectId).getValue();
        assertEquals(2, reloaded.getDiagnostics().size());
        assertEquals("pii", reloaded.getDiagnostics().stream().filter(d -> "gdpr".equals(d.getProducer())).findFirst()
                .orElseThrow().getMessage(), "the other producer's finding is untouched");
        assertEquals("v2", reloaded.getDiagnostics().stream().filter(d -> "qvt".equals(d.getProducer())).findFirst()
                .orElseThrow().getMessage());

        storage.updateDiagnostics(SCOPE, REGISTRY, STAGE, objectId, "qvt", List.of()).getValue();
        reloaded = storage.retrieveMetadata(SCOPE, REGISTRY, STAGE, objectId).getValue();
        assertEquals(1, reloaded.getDiagnostics().size(), "empty clears the producer's roots");
        assertEquals("gdpr", reloaded.getDiagnostics().get(0).getProducer());

        assertNull(storage.updateDiagnostics(SCOPE, REGISTRY, STAGE, "no-such-object", "qvt", List.of()).getValue(),
                "an unknown object yields null, not an exception");
    }

    private static ObjectMetadata store(EObjectStorageService<EObject> storage, String objectId) throws Exception {
        EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
        pkg.setName("diag");
        pkg.setNsPrefix("diag");
        pkg.setNsURI("http://diag.test/" + objectId);
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectName("diag");
        metadata.setVersion("1.0.0");
        metadata.setUploadUser("test-user");
        metadata.setSourceChannel("INTEGRATION_TEST");
        metadata.setStatus(ObjectStatus.DRAFT);
        metadata.setUploadTime(Instant.now());
        metadata.setLastChangeTime(Instant.now());
        return storage.storeObject(SCOPE, REGISTRY, STAGE, objectId, pkg, metadata).getValue();
    }

    static Diagnostic diagnostic(String code, String target, DiagnosticSeverity severity, String message) {
        Diagnostic diagnostic = ManagementFactory.eINSTANCE.createDiagnostic();
        diagnostic.setCode(code);
        diagnostic.setTarget(target);
        diagnostic.setSeverity(severity);
        diagnostic.setMessage(message);
        return diagnostic;
    }
}
