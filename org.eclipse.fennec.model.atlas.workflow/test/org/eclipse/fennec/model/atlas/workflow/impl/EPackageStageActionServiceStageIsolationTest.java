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
package org.eclipse.fennec.model.atlas.workflow.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Map;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.workflow.ActionContext;
import org.eclipse.fennec.model.atlas.workflow.ResourceSetCollector;
import org.eclipse.fennec.model.atlas.workflow.StageActionService.ExitReason;
import org.eclipse.fennec.model.atlas.workflow.registration.DynamicEPackageRegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.osgi.framework.BundleContext;
import org.osgi.util.promise.Promises;

/**
 * Unit tests for the per-stage registration bookkeeping of
 * {@link EPackageStageActionService}.
 *
 * <p>
 * The same {@code objectId} may legitimately live in two stages of one registry
 * at once - a transition copies unless the registry sets
 * {@code delete.after.transition=true}, and a new draft revision of a released
 * model re-uploads the same id (issue #211). The component is configured for
 * all stages at once, so its bookkeeping must be keyed by (scope, stage,
 * objectId), not by objectId alone.
 * </p>
 *
 * @see <a href="https://github.com/eclipse-fennec/model.atlas/issues/252">issue
 *      #252</a>
 */
@DisplayName("EPackageStageActionService - same objectId in two stages")
public class EPackageStageActionServiceStageIsolationTest {

    private static final String SCOPE = "test-scope";
    private static final String REGISTRY = "schema";
    private static final String OBJECT_ID = "obj-1";
    private static final String NS_URI = "http://example.org/test/1.0.0";

    private DynamicEPackageRegistrationService registrationService;
    private EObjectStorageService<EPackage> storageService;
    private EPackageStageActionService actionService;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() throws Exception {
        registrationService = mock(DynamicEPackageRegistrationService.class);
        when(registrationService.registerEPackage(any(), any())).thenReturn(true);
        when(registrationService.unregisterEPackage(any(), any(), any())).thenReturn(true);
        when(registrationService.isRegistered(any(), any(), any())).thenReturn(true);

        storageService = mock(EObjectStorageService.class);

        EPackageStageActionService.Config config = mock(EPackageStageActionService.Config.class);
        when(config.trigger_stages()).thenReturn(new String[] { "draft", "approved", "release" });
        when(config.replay_on_startup()).thenReturn(true);
        when(config.replay_on_shutdown()).thenReturn(true);

        actionService = new EPackageStageActionService(mock(BundleContext.class, RETURNS_DEEP_STUBS), storageService,
                config);
        inject(actionService, "registrationService", registrationService);
        inject(actionService, "resourceSetCollector", mock(ResourceSetCollector.class));
    }

    @Test
    @DisplayName("EXIT of the second stage still unregisters that stage's EPackage")
    void exitOfSecondStageUnregisters() throws Exception {
        stageHolds("draft", NS_URI, "1");
        stageHolds("release", NS_URI, "1");

        enter("draft");
        enter("release");

        exit("draft");
        verify(registrationService).unregisterEPackage(SCOPE, "draft", NS_URI);

        exit("release");
        verify(registrationService).unregisterEPackage(SCOPE, "release", NS_URI);
    }

    @Test
    @DisplayName("EXIT of the first stage still unregisters it after the other stage left")
    void exitOfFirstStageUnregistersAfterSecondLeft() throws Exception {
        stageHolds("draft", NS_URI, "1");
        stageHolds("release", NS_URI, "1");

        enter("draft");
        enter("release");

        exit("release");
        verify(registrationService).unregisterEPackage(SCOPE, "release", NS_URI);

        exit("draft");
        verify(registrationService).unregisterEPackage(SCOPE, "draft", NS_URI);
    }

    @Test
    @DisplayName("A changed nsURI in one stage does not unregister the other stage's nsURI")
    void changedNsUriDoesNotTouchTheOtherStage() throws Exception {
        String otherNsUri = "http://example.org/test/2.0.0";

        stageHolds("release", NS_URI, "1");
        enter("release");

        // the draft copy of the same id carries a newer nsURI
        stageHolds("draft", otherNsUri, "2");
        enter("draft");

        // the release registration is untouched: only draft's own previous
        // registration (there is none) could be unregistered here
        verify(registrationService, never()).unregisterEPackage(SCOPE, "draft", NS_URI);
        verify(registrationService, never()).unregisterEPackage(SCOPE, "release", NS_URI);

        exit("release");
        verify(registrationService).unregisterEPackage(SCOPE, "release", NS_URI);
    }

    @Test
    @DisplayName("A replay of one stage does not skip registration because of the other stage's version")
    void replaySkipIsPerStage() throws Exception {
        stageHolds("release", NS_URI, "sha-1");
        enter("release");

        // The draft copy sits at the very same version. The replay skip must not
        // read the release stage's version through a shared key: this ENTER
        // registers for the draft stage and tracks it there.
        stageHolds("draft", NS_URI, "sha-1");
        enter("draft");

        verify(registrationService, times(2)).registerEPackage(any(), any());

        exit("draft");
        verify(registrationService).unregisterEPackage(SCOPE, "draft", NS_URI);
        exit("release");
        verify(registrationService).unregisterEPackage(SCOPE, "release", NS_URI);
    }

    // === helpers ===

    private void stageHolds(String stage, String nsUri, String version) {
        EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
        ePackage.setName("test");
        ePackage.setNsPrefix("test");
        ePackage.setNsURI(nsUri);

        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectId(OBJECT_ID);
        metadata.setScope(SCOPE);
        metadata.setRegistry(REGISTRY);
        metadata.setStage(stage);
        metadata.setVersion(version);
        metadata.setUploadTime(Instant.now());

        when(storageService.retrieveObject(SCOPE, REGISTRY, stage, OBJECT_ID)).thenReturn(Promises.resolved(ePackage));
        when(storageService.retrieveMetadata(SCOPE, REGISTRY, stage, OBJECT_ID))
                .thenReturn(Promises.resolved(metadata));
    }

    private void enter(String stage) throws Exception {
        actionService.onEnter(context(stage, null)).getValue();
    }

    private void exit(String stage) throws Exception {
        actionService.onExit(context(stage, ExitReason.TRANSITIONED)).getValue();
    }

    private ActionContext context(String stage, ExitReason exitReason) {
        return new ActionContext(SCOPE, REGISTRY, OBJECT_ID, "http://www.eclipse.org/emf/2002/Ecore#//EPackage", stage,
                null, null, exitReason, "tester", Instant.now(), null, true, Map.of());
    }

    private static void inject(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }
}
