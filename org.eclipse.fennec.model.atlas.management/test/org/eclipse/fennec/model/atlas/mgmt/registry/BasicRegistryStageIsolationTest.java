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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

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

        assertThat(registryService.findByScopeRegistryAndStage(SCOPE, REGISTRY, "draft"))
                .extracting(ObjectMetadata::getStage).containsExactly("draft");
        assertThat(registryService.findByScopeRegistryAndStage(SCOPE, REGISTRY, "release"))
                .extracting(ObjectMetadata::getStage).containsExactly("release");
        assertThat(registryService.findByScopeAndStage(SCOPE, "draft")).extracting(ObjectMetadata::getStage)
                .containsExactly("draft");
        assertThat(registryService.findByScopeStageAndName(SCOPE, "release", "the-object"))
                .extracting(ObjectMetadata::getStage).containsExactly("release");
        assertThat(registryService.findByScopeRegistryStageAndName(SCOPE, REGISTRY, "draft", "the-object"))
                .extracting(ObjectMetadata::getStage).containsExactly("draft");
    }

    @Test
    @DisplayName("The addressed lookup tells the two copies apart")
    void addressedLookupTellsCopiesApart() {
        registryService.updateCache(metadata("draft"));
        registryService.updateCache(metadata("release"));

        assertThat(registryService.getMetadata(SCOPE, REGISTRY, "draft", OBJECT_ID))
                .hasValueSatisfying(metadata -> assertThat(metadata.getStage()).isEqualTo("draft"));
        assertThat(registryService.getMetadata(SCOPE, REGISTRY, "release", OBJECT_ID))
                .hasValueSatisfying(metadata -> assertThat(metadata.getStage()).isEqualTo("release"));
        assertThat(registryService.getMetadata(SCOPE, REGISTRY, "approved", OBJECT_ID)).isEmpty();
        assertThat(registryService.findByObjectNameAndStage("the-object", "draft"))
                .hasValueSatisfying(metadata -> assertThat(metadata.getStage()).isEqualTo("draft"));
    }

    @Test
    @DisplayName("A stage-free query reports the object once per stage that holds it")
    void stageFreeQueryReportsEveryCopy() {
        registryService.updateCache(metadata("draft"));
        registryService.updateCache(metadata("release"));

        assertThat(registryService.findByStatus(ObjectStatus.DRAFT)).extracting(ObjectMetadata::getStage)
                .containsExactlyInAnyOrder("draft", "release");
        assertThat(registryService.findByObjectType("EPackage")).extracting(ObjectMetadata::getStage)
                .containsExactlyInAnyOrder("draft", "release");
        assertThat(registryService.findByObjectName("the-object")).extracting(ObjectMetadata::getStage)
                .containsExactlyInAnyOrder("draft", "release");
        assertThat(registryService.findByVersion("1.0.0")).extracting(ObjectMetadata::getStage)
                .containsExactlyInAnyOrder("draft", "release");
        assertThat(registryService.findByFingerprint("fp1:abc")).extracting(ObjectMetadata::getStage)
                .containsExactlyInAnyOrder("draft", "release");
        assertThat(registryService.findByStatusAndType(ObjectStatus.DRAFT, "EPackage"))
                .extracting(ObjectMetadata::getStage).containsExactlyInAnyOrder("draft", "release");
    }

    @Test
    @DisplayName("Deleting one stage's copy leaves the other stage's copy cached")
    void deletingOneStageCopyKeepsTheOther() {
        registryService.updateCache(metadata("draft"));
        registryService.updateCache(metadata("release"));

        registryService.removeFromCache(SCOPE, REGISTRY, "release", OBJECT_ID);

        assertThat(registryService.findByScopeRegistryAndStage(SCOPE, REGISTRY, "release")).isEmpty();
        assertThat(registryService.findByScopeRegistryAndStage(SCOPE, REGISTRY, "draft"))
                .extracting(ObjectMetadata::getStage).containsExactly("draft");
        assertThat(registryService.findByStatus(ObjectStatus.DRAFT)).extracting(ObjectMetadata::getStage)
                .containsExactly("draft");
    }

    @Test
    @DisplayName("The stage-free removal drops every copy of the id")
    void stageFreeRemovalDropsEveryCopy() {
        registryService.updateCache(metadata("draft"));
        registryService.updateCache(metadata("release"));

        registryService.removeFromCache(OBJECT_ID);

        assertThat(registryService.findByStatus(ObjectStatus.DRAFT)).isEmpty();
        assertThat(registryService.findByScopeRegistryAndStage(SCOPE, REGISTRY, "draft")).isEmpty();
        assertThat(registryService.findByScopeRegistryAndStage(SCOPE, REGISTRY, "release")).isEmpty();
    }

    @Test
    @DisplayName("A cache initialized from storage keeps both stages' copies")
    void cacheInitializationKeepsBothCopies() throws Exception {
        List<ObjectMetadata> stored = List.of(metadata("draft"), metadata("release"));
        lenient().when(mockStorageHelper.loadAllMetadata()).thenReturn(stored);

        registryService = new BasicEObjectRegistryService<>(mockStorageHelper, mockPromiseFactory);

        assertThat(registryService.findByScopeRegistryAndStage(SCOPE, REGISTRY, "draft"))
                .extracting(ObjectMetadata::getStage).containsExactly("draft");
        assertThat(registryService.findByScopeRegistryAndStage(SCOPE, REGISTRY, "release"))
                .extracting(ObjectMetadata::getStage).containsExactly("release");
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
