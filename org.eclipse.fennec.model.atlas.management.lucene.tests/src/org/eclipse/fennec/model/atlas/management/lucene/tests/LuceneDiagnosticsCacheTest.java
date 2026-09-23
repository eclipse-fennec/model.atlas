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
package org.eclipse.fennec.model.atlas.management.lucene.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations.RegistryConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * The Lucene shared registry serves listings from its metadata cache, not from the index
 * (issue #292): a metadata that carries a diagnostic tree must come back with the tree
 * from every lookup path, so a listing shows an object's findings without a storage read.
 */
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@DisplayName("Diagnostics through the Lucene shared registry cache")
public class LuceneDiagnosticsCacheTest {

    @TempDir
    static Path tempDir;

    @BeforeEach
    void setUp() {
        System.setProperty(CommonTestAnnotations.PROP_TEMP_DIR, tempDir.toString());
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @RegistryConfiguration
    @DisplayName("A cached metadata keeps its diagnostic tree on every lookup path")
    void cachedMetadataKeepsItsDiagnostics(
            @InjectService(filter = "(registry.type=shared)") EObjectRegistryService registryService) {
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectId("diag-cached");
        metadata.setObjectName("Diagnosed");
        metadata.setVersion("1.0.0");
        metadata.setStatus(ObjectStatus.DRAFT);
        metadata.setObjectType("EPackage");
        metadata.setUploadTime(Instant.now());
        metadata.setUploadUser("test-user");
        metadata.setSourceChannel("TEST");
        metadata.setLastChangeTime(Instant.now());
        metadata.setScope("diag-scope");
        metadata.setRegistry("schema");
        metadata.setStage("draft");
        Diagnostic root = ManagementFactory.eINSTANCE.createDiagnostic();
        root.setId("root-1");
        root.setProducer("qvt");
        root.setCode("compile-failed");
        root.setSeverity(DiagnosticSeverity.ERROR);
        root.setMessage("does not compile");
        root.setCreatedTime(Instant.now());
        Diagnostic child = ManagementFactory.eINSTANCE.createDiagnostic();
        child.setId("child-1");
        child.setProducer("qvt");
        child.setCode("unresolved-import");
        child.setSeverity(DiagnosticSeverity.ERROR);
        child.setMessage("text.Case");
        child.setCreatedTime(Instant.now());
        root.getChildren().add(child);
        metadata.getDiagnostics().add(root);

        registryService.updateCache(metadata);

        Optional<ObjectMetadata> byId = registryService.getMetadata("diag-cached");
        assertTrue(byId.isPresent());
        assertTree(byId.get());

        List<ObjectMetadata> listed = registryService.findByScopeRegistryAndStage("diag-scope", "schema", "draft");
        assertEquals(1, listed.size(), "the listing path finds it");
        assertTree(listed.get(0));

        List<ObjectMetadata> byStatus = registryService.findByStatus(ObjectStatus.DRAFT);
        assertTrue(byStatus.stream().anyMatch(m -> "diag-cached".equals(m.getObjectId())));
        assertTree(byStatus.stream().filter(m -> "diag-cached".equals(m.getObjectId())).findFirst().orElseThrow());

        // a second updateCache with a replaced tree wins, as a producer's replace would
        metadata.getDiagnostics().clear();
        registryService.updateCache(metadata);
        ObjectMetadata latest = (ObjectMetadata) registryService.getMetadata("diag-cached").orElseThrow();
        assertTrue(latest.getDiagnostics().isEmpty(), "the cache follows the latest metadata");
    }

    private static void assertTree(ObjectMetadata metadata) {
        assertEquals(1, metadata.getDiagnostics().size(), "the root is there");
        Diagnostic root = metadata.getDiagnostics().get(0);
        assertEquals("root-1", root.getId());
        assertEquals(DiagnosticSeverity.ERROR, root.getSeverity());
        assertEquals(1, root.getChildren().size(), "the child is there");
        assertEquals("child-1", root.getChildren().get(0).getId());
    }
}
