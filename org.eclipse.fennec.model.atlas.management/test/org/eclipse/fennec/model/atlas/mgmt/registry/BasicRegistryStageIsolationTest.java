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
package org.eclipse.fennec.model.atlas.mgmt.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectStatus;
import org.eclipse.fennec.model.atlas.mgmt.storage.AbstractStorageHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.util.promise.PromiseFactory;

/**
 * Unit tests for the stage dimension of the in-memory registry cache.
 *
 * <p>
 * One {@code objectId} may legitimately be held by two stages of the same
 * registry at once: a transition copies unless the registry sets
 * {@code delete.after.transition=true}, and a new draft revision of a released
 * model re-uploads the same id (issue #211). The registry is shared across
 * stages, so it must address its entries by (scope, registry, stage, objectId)
 * rather than by objectId alone (issue #252).
 * </p>
 *
 * @see <a href="https://github.com/eclipse-fennec/model.atlas/issues/252">issue
 *      #252</a>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("BasicEObjectRegistryService - same objectId in two stages")
public class BasicRegistryStageIsolationTest {

    private static final String SCOPE = "test-scope";
    private static final String REGISTRY = "transformations";
    private static final String OBJECT_ID = "obj-1";

    @Mock
    private AbstractStorageHelper mockStorageHelper;

    @Mock
    private PromiseFactory mockPromiseFactory;

    private BasicEObjectRegistryService<EObject> registryService;

    @BeforeEach
    public void setUp() throws Exception {
        lenient().when(mockStorageHelper.loadAllMetadata()).thenReturn(new ArrayList<>());
        registryService = new BasicEObjectRegistryService<>(mockStorageHelper, mockPromiseFactory);
    }

    @Test
    @DisplayName("Both stages keep listing their own copy")
    void bothStagesListTheirOwnCopy() {
        registryService.updateCache(metadata("draft"));
        registryService.updateCache(metadata("release"));

        assertEquals(List.of("draft"), stages(registryService.findByScopeRegistryAndStage(SCOPE, REGISTRY, "draft")));
        assertEquals(List.of("release"),
                stages(registryService.findByScopeRegistryAndStage(SCOPE, REGISTRY, "release")));
        assertEquals(List.of("draft"), stages(registryService.findByScopeAndStage(SCOPE, "draft")));
        assertEquals(List.of("release"), stages(registryService.findByScopeStageAndName(SCOPE, "release", "the-object")));
        assertEquals(List.of("draft"),
                stages(registryService.findByScopeRegistryStageAndName(SCOPE, REGISTRY, "draft", "the-object")));
    }

    @Test
    @DisplayName("The addressed lookup tells the two copies apart")
    void addressedLookupTellsCopiesApart() {
        registryService.updateCache(metadata("draft"));
        registryService.updateCache(metadata("release"));

        assertStage("draft", registryService.getMetadata(SCOPE, REGISTRY, "draft", OBJECT_ID));
        assertStage("release", registryService.getMetadata(SCOPE, REGISTRY, "release", OBJECT_ID));
        assertTrue(registryService.getMetadata(SCOPE, REGISTRY, "approved", OBJECT_ID).isEmpty(),
                "no copy is expected in the approved stage");
        assertStage("draft", registryService.findByObjectNameAndStage("the-object", "draft"));
    }

    @Test
    @DisplayName("A stage-free query reports the object once per stage that holds it")
    void stageFreeQueryReportsEveryCopy() {
        registryService.updateCache(metadata("draft"));
        registryService.updateCache(metadata("release"));

        assertEquals(List.of("draft", "release"), sortedStages(registryService.findByStatus(ObjectStatus.DRAFT)));
        assertEquals(List.of("draft", "release"), sortedStages(registryService.findByObjectType("EPackage")));
        assertEquals(List.of("draft", "release"), sortedStages(registryService.findByObjectName("the-object")));
        assertEquals(List.of("draft", "release"), sortedStages(registryService.findByVersion("1.0.0")));
        assertEquals(List.of("draft", "release"), sortedStages(registryService.findByFingerprint("fp1:abc")));
        assertEquals(List.of("draft", "release"),
                sortedStages(registryService.findByStatusAndType(ObjectStatus.DRAFT, "EPackage")));
    }

    @Test
    @DisplayName("Deleting one stage's copy leaves the other stage's copy cached")
    void deletingOneStageCopyKeepsTheOther() {
        registryService.updateCache(metadata("draft"));
        registryService.updateCache(metadata("release"));

        registryService.removeFromCache(SCOPE, REGISTRY, "release", OBJECT_ID);

        assertTrue(registryService.findByScopeRegistryAndStage(SCOPE, REGISTRY, "release").isEmpty(),
                "the release copy is expected to be gone");
        assertEquals(List.of("draft"), stages(registryService.findByScopeRegistryAndStage(SCOPE, REGISTRY, "draft")));
        assertEquals(List.of("draft"), stages(registryService.findByStatus(ObjectStatus.DRAFT)));
    }

    @Test
    @DisplayName("The stage-free removal drops every copy of the id")
    void stageFreeRemovalDropsEveryCopy() {
        registryService.updateCache(metadata("draft"));
        registryService.updateCache(metadata("release"));

        registryService.removeFromCache(OBJECT_ID);

        assertTrue(registryService.findByStatus(ObjectStatus.DRAFT).isEmpty(), "no copy is expected to be left");
        assertTrue(registryService.findByScopeRegistryAndStage(SCOPE, REGISTRY, "draft").isEmpty(),
                "the draft copy is expected to be gone");
        assertTrue(registryService.findByScopeRegistryAndStage(SCOPE, REGISTRY, "release").isEmpty(),
                "the release copy is expected to be gone");
    }

    @Test
    @DisplayName("A cache initialized from storage keeps both stages' copies")
    void cacheInitializationKeepsBothCopies() throws Exception {
        List<ObjectMetadata> stored = List.of(metadata("draft"), metadata("release"));
        lenient().when(mockStorageHelper.loadAllMetadata()).thenReturn(stored);

        registryService = new BasicEObjectRegistryService<>(mockStorageHelper, mockPromiseFactory);

        assertEquals(List.of("draft"), stages(registryService.findByScopeRegistryAndStage(SCOPE, REGISTRY, "draft")));
        assertEquals(List.of("release"),
                stages(registryService.findByScopeRegistryAndStage(SCOPE, REGISTRY, "release")));
    }

    private static List<String> stages(List<ObjectMetadata> found) {
        return found.stream().map(ObjectMetadata::getStage).toList();
    }

    private static List<String> sortedStages(List<ObjectMetadata> found) {
        return found.stream().map(ObjectMetadata::getStage).sorted().toList();
    }

    private static void assertStage(String expectedStage, Optional<ObjectMetadata> found) {
        assertTrue(found.isPresent(), "a copy is expected in the " + expectedStage + " stage");
        assertEquals(expectedStage, found.get().getStage());
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
        metadata.setVersion("1.0.0");
        metadata.setFingerprint("fp1:abc");
        metadata.setUploadTime(Instant.now());
        metadata.setUploadUser("tester");
        return metadata;
    }
}
