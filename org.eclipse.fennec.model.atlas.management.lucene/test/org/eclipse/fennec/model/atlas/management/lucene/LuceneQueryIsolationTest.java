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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.management.lucene;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import org.eclipse.fennec.model.atlas.management.lucene.registry.LuceneRegistryHelper;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Tests that a scoped registry search stays inside the scope it was given, whatever the
 * searched values happen to contain.
 *
 * <p>
 * The values a search filters on — object names, scopes, stages, registries — are user
 * data, not query syntax. Interpolating them into a Lucene query string turns them into
 * syntax, and an unparseable result used to be answered with a {@code MatchAllDocsQuery}:
 * one bracket in a name and a search scoped to one tenant returned every object of every
 * scope.
 * </p>
 */
class LuceneQueryIsolationTest {

    private static final String SCOPE_A = "scopeA";
    private static final String SCOPE_B = "scopeB";
    private static final String STAGE = "draft";
    private static final String REGISTRY = "schema";

    @TempDir
    Path tempDir;

    private LuceneRegistryHelper helper;

    @BeforeEach
    void setUp() throws IOException {
        helper = new LuceneRegistryHelper(tempDir);
        helper.initialize();
        // Same object name in two scopes: every search below must tell them apart.
        helper.updateIndex("a-obj", metadata(SCOPE_A, REGISTRY, "SensorModel"));
        helper.updateIndex("b-obj", metadata(SCOPE_B, REGISTRY, "SensorModel"));
    }

    @AfterEach
    void tearDown() throws Exception {
        if (helper != null) {
            helper.close();
        }
    }

    @Test
    void searchRejectsAQueryItCannotParseInsteadOfMatchingEverything() {
        // The shape LuceneEObjectRegistryService.findByScopeStageAndName built for a
        // wildcard name: the name was not quoted, so its own punctuation became syntax.
        String query = "(objectName:http://example.com/model* AND stage:" + STAGE + " AND scope:" + SCOPE_A + ")";

        assertThrows(IllegalArgumentException.class, () -> helper.searchObjectIds(query, Integer.MAX_VALUE),
                "A search that cannot honour its filter must say so, not answer with every object it holds");
    }

    @Test
    void findByScopeAndStageKeepsOtherScopesOut() throws IOException {
        assertEquals(List.of("a-obj"), helper.findByScopeAndStage(SCOPE_A, STAGE));
        assertEquals(List.of("b-obj"), helper.findByScopeAndStage(SCOPE_B, STAGE));
        assertTrue(helper.findByScopeAndStage(SCOPE_A, "approved").isEmpty());
    }

    @Test
    void findByScopeRegistryAndStageKeepsOtherScopesOut() throws IOException {
        assertEquals(List.of("a-obj"), helper.findByScopeRegistryAndStage(SCOPE_A, REGISTRY, STAGE));
        assertTrue(helper.findByScopeRegistryAndStage(SCOPE_A, "other-registry", STAGE).isEmpty());
    }

    @Test
    void findByScopeStageAndNameKeepsOtherScopesOut() throws IOException {
        assertEquals(List.of("a-obj"), helper.findByScopeStageAndName(SCOPE_A, STAGE, "SensorModel"));
        assertEquals(List.of("a-obj"), helper.findByScopeStageAndName(SCOPE_A, STAGE, "Sensor*"));
    }

    @Test
    void findByScopeRegistryStageAndNameKeepsOtherScopesOut() throws IOException {
        assertEquals(List.of("a-obj"), helper.findByScopeRegistryStageAndName(SCOPE_A, REGISTRY, STAGE, "SensorModel"));
        assertTrue(helper.findByScopeRegistryStageAndName(SCOPE_A, "other-registry", STAGE, "SensorModel").isEmpty());
    }

    @Test
    void aNameThatLooksLikeQuerySyntaxIsSearchedAsData() throws IOException {
        helper.updateIndex("odd-obj", metadata(SCOPE_A, REGISTRY, "Model (v2) [draft]"));

        assertEquals(List.of("odd-obj"), helper.findByScopeStageAndName(SCOPE_A, STAGE, "Model (v2) [draft]"),
                "Punctuation in a name is data, not query syntax");
    }

    @Test
    void findByStorageBackendAndStageKeepsOtherStagesOut() throws IOException {
        ObjectMetadata onFile = metadata(SCOPE_A, REGISTRY, "FileBacked");
        onFile.getProperties().put("storage.backend", "file");
        helper.updateIndex("file-obj", onFile);

        assertEquals(List.of("file-obj"), helper.findByStorageBackend("file"));
        assertEquals(List.of("file-obj"), helper.findByStorageBackendAndStage("file", STAGE));
        assertTrue(helper.findByStorageBackendAndStage("file", "approved").isEmpty());
    }

    private ObjectMetadata metadata(String scope, String registry, String objectName) {
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectId(objectName + "@" + scope);
        metadata.setObjectName(objectName);
        metadata.setScope(scope);
        metadata.setStage(STAGE);
        metadata.setRegistry(registry);
        metadata.setObjectType("EPackage");
        metadata.setUploadUser("tester");
        metadata.setUploadTime(Instant.now());
        return metadata;
    }
}
