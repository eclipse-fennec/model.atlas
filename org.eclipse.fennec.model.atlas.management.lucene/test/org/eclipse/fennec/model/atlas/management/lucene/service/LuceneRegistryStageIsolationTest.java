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
package org.eclipse.fennec.model.atlas.management.lucene.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Path;
import java.time.Instant;
import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for the stage dimension of the shared registry cache and index.
 *
 * <p>
 * One {@code objectId} may legitimately be held by two stages of the same
 * registry at once: a transition copies unless the registry sets
 * {@code delete.after.transition=true}, and a new draft revision of a released
 * model re-uploads the same id (issue #211). The shared registry is a single
 * instance for all stages, so it must address its entries by (scope, registry,
 * stage, objectId) rather than by objectId alone.
 * </p>
 *
 * @see <a href="https://github.com/eclipse-fennec/model.atlas/issues/252">issue
 *      #252</a>
 */
@DisplayName("LuceneEObjectRegistryService - same objectId in two stages")
public class LuceneRegistryStageIsolationTest {

    private static final String SCOPE = "test-scope";
    private static final String REGISTRY = "transformations";
    private static final String OBJECT_ID = "obj-1";

    @TempDir
    Path tempDir;

    private LuceneEObjectRegistryService<EObject> registry;

    @BeforeEach
    void setUp() throws Exception {
        LuceneEObjectRegistryService.Config config = mock(LuceneEObjectRegistryService.Config.class);
        when(config.registry_workspace_folder()).thenReturn(tempDir.toString());
        when(config.storage_backend_tracking()).thenReturn(false);
        when(config.initial_index_capacity()).thenReturn(1000);
        when(config.enable_debug_logging()).thenReturn(false);

        registry = new LuceneEObjectRegistryService<>();
        registry.activate(config);
    }

    @AfterEach
    void tearDown() {
        if (registry != null) {
            registry.deactivate();
        }
    }

    @Test
    @DisplayName("Both stages keep listing their own copy after a copy-transition")
    void bothStagesListTheirOwnCopy() {
        registry.updateCache(metadata("draft"));
        registry.updateCache(metadata("release"));

        List<ObjectMetadata> draft = registry.findByScopeRegistryAndStage(SCOPE, REGISTRY, "draft");
        assertEquals(List.of(OBJECT_ID), draft.stream().map(ObjectMetadata::getObjectId).toList());
        assertEquals(List.of("draft"), stages(draft));

        List<ObjectMetadata> release = registry.findByScopeRegistryAndStage(SCOPE, REGISTRY, "release");
        assertEquals(List.of(OBJECT_ID), release.stream().map(ObjectMetadata::getObjectId).toList());
        assertEquals(List.of("release"), stages(release));
    }

    @Test
    @DisplayName("A scope/stage listing keeps both copies apart")
    void scopeAndStageListingKeepsBothCopiesApart() {
        registry.updateCache(metadata("draft"));
        registry.updateCache(metadata("release"));

        assertEquals(List.of("draft"), stages(registry.findByScopeAndStage(SCOPE, "draft")));
        assertEquals(List.of("release"), stages(registry.findByScopeAndStage(SCOPE, "release")));
    }

    @Test
    @DisplayName("A name listing keeps both copies apart")
    void nameListingKeepsBothCopiesApart() {
        registry.updateCache(metadata("draft"));
        registry.updateCache(metadata("release"));

        assertEquals(List.of("draft"),
                stages(registry.findByScopeRegistryStageAndName(SCOPE, REGISTRY, "draft", "the-object")));
        assertEquals(List.of("release"),
                stages(registry.findByScopeRegistryStageAndName(SCOPE, REGISTRY, "release", "the-object")));
    }

    @Test
    @DisplayName("A stage-free query reports the object once per stage that holds it")
    void stageFreeQueryReportsEveryCopy() {
        registry.updateCache(metadata("draft"));
        registry.updateCache(metadata("release"));

        assertEquals(List.of("draft", "release"), sortedStages(registry.findByStatus(ObjectStatus.DRAFT)));
        assertEquals(2, registry.getAllMetadata().size());
    }

    @Test
    @DisplayName("Deleting one stage's copy leaves the other stage's copy listed")
    void deletingOneStageCopyKeepsTheOther() {
        registry.updateCache(metadata("draft"));
        registry.updateCache(metadata("release"));

        registry.removeFromCache(SCOPE, REGISTRY, "release", OBJECT_ID);

        assertTrue(registry.findByScopeRegistryAndStage(SCOPE, REGISTRY, "release").isEmpty(),
                "the release copy is expected to be gone");
        assertEquals(List.of("draft"), stages(registry.findByScopeRegistryAndStage(SCOPE, REGISTRY, "draft")));
        assertTrue(registry.getMetadata(SCOPE, REGISTRY, "draft", OBJECT_ID).isPresent(),
                "the draft copy is expected to be still addressable");
        assertTrue(registry.getMetadata(SCOPE, REGISTRY, "release", OBJECT_ID).isEmpty(),
                "the release copy is expected to be no longer addressable");
    }

    @Test
    @DisplayName("The stage-free removal drops every copy of the id")
    void stageFreeRemovalDropsEveryCopy() {
        registry.updateCache(metadata("draft"));
        registry.updateCache(metadata("release"));

        registry.removeFromCache(OBJECT_ID);

        assertTrue(registry.getAllMetadata().isEmpty(), "no copy is expected to be left");
        assertTrue(registry.findByScopeRegistryAndStage(SCOPE, REGISTRY, "draft").isEmpty(),
                "the draft copy is expected to be gone");
        assertTrue(registry.findByScopeRegistryAndStage(SCOPE, REGISTRY, "release").isEmpty(),
                "the release copy is expected to be gone");
    }

    private static List<String> stages(List<ObjectMetadata> found) {
        return found.stream().map(ObjectMetadata::getStage).toList();
    }

    private static List<String> sortedStages(List<ObjectMetadata> found) {
        return found.stream().map(ObjectMetadata::getStage).sorted().toList();
    }

    private ObjectMetadata metadata(String stage) {
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectId(OBJECT_ID);
        metadata.setObjectName("the-object");
        metadata.setScope(SCOPE);
        metadata.setRegistry(REGISTRY);
        metadata.setStage(stage);
        metadata.setStatus(ObjectStatus.DRAFT);
        metadata.setObjectType("EPackage");
        metadata.setUploadTime(Instant.now());
        metadata.setUploadUser("tester");
        return metadata;
    }
}
