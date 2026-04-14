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
package org.eclipse.fennec.model.atlas.workflow.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.workflow.ActionContext;
import org.eclipse.fennec.model.atlas.workflow.StageActionService;
import org.eclipse.fennec.model.atlas.workflow.StageActionService.ActionEvent;
import org.eclipse.fennec.model.atlas.workflow.StageActionService.ExitReason;
import org.eclipse.fennec.model.atlas.workflow.tests.annotations.TestAnnotations.EPackageStageActionService;
import org.eclipse.fennec.model.atlas.workflow.tests.support.LuceneAwareTempDirExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * Integration tests for {@link StageActionService} implemented by the
 * EPackage stage action service.
 *
 * <p>
 * These tests verify that ENTER / UPDATE / EXIT callbacks correctly register
 * and unregister EPackages as OSGi services and that workflow metadata
 * (scope, stage) is propagated to the service properties.
 * </p>
 */
@ExtendWith(LuceneAwareTempDirExtension.class)
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@ExtendWith(MockitoExtension.class)
@RequireConfigurationAdmin
public class EPackageStageActionServiceIntegrationTest {

    private static final String TEST_SCOPE = "test-scope";
    private static final String TEST_REGISTRY = "test-registry";
    private static final String TEST_STAGE = "release";
    private static final String EPACKAGE_TYPE = EcoreUtil.getURI(EcorePackage.Literals.EPACKAGE).toString();

    @Test
    @EPackageStageActionService
    public void testSupportsObjectType(
            @InjectService StageActionService stageActionService) {

        assertNotNull(stageActionService, "StageActionService should be available");
        assertTrue(stageActionService.supportsObjectType(EPACKAGE_TYPE),
                "Service should support the EPackage URI type");
        assertFalse(stageActionService.supportsObjectType("SomeOtherType"),
                "Service should reject unrelated object types");
    }

    @Test
    @EPackageStageActionService
    public void testTriggerStagesAndEvents(
            @InjectService StageActionService stageActionService) {

        Set<String> stages = stageActionService.getTriggerStages();
        assertEquals(Set.of("draft", "approved", "release"), stages,
                "Trigger stages should match the configured set");

        Set<ActionEvent> events = stageActionService.getTriggerEvents();
        assertEquals(Set.of(ActionEvent.ENTER, ActionEvent.UPDATE, ActionEvent.EXIT), events,
                "Service should subscribe to ENTER, UPDATE and EXIT events");
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @EPackageStageActionService
    public void testOnEnterRegistersEPackage(
            @InjectService(filter = "(storage.type=file)") EObjectStorageService storage,
            @InjectService StageActionService stageActionService,
            @InjectService(cardinality = 0, filter = "(emf.name=TestSensorModel)") ServiceAware<EPackage> ePackageServiceAware)
            throws Exception {

        EPackage testPackage = createTestEPackage();
        String objectId = "test-enter-" + System.currentTimeMillis();
        ObjectMetadata metadata = createTestMetadata(objectId, TEST_STAGE);
        storage.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, objectId, testPackage, metadata).getValue();

        assertTrue(ePackageServiceAware.isEmpty(),
                "EPackage should not be registered before onEnter is invoked");

        ActionContext ctx = enterContext(objectId, TEST_STAGE);
        stageActionService.onEnter(ctx).getValue();

        EPackage registered = ePackageServiceAware.waitForService(5000L);
        assertNotNull(registered, "EPackage should be registered after onEnter");
        assertEquals(testPackage.getNsURI(), registered.getNsURI());
        assertEquals(testPackage.getName(), registered.getName());
        assertEquals(1, registered.getEClassifiers().size());
        assertEquals("Sensor", registered.getEClassifiers().get(0).getName());
        
        removeEPackage(objectId, stageActionService);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @EPackageStageActionService
    public void testRegistrationCarriesWorkflowProperties(
            @InjectService(filter = "(storage.type=file)") EObjectStorageService storage,
            @InjectService StageActionService stageActionService,
            @InjectService(cardinality = 0, filter = "(&(emf.name=TestSensorModel)(workflow.scope=test-scope)(workflow.stage=release))") ServiceAware<EPackage> ePackageServiceAware)
            throws Exception {

        EPackage testPackage = createTestEPackage();
        String objectId = "test-props-" + System.currentTimeMillis();
        ObjectMetadata metadata = createTestMetadata(objectId, TEST_STAGE);
        storage.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, objectId, testPackage, metadata).getValue();

        stageActionService.onEnter(enterContext(objectId, TEST_STAGE)).getValue();

        EPackage registered = ePackageServiceAware.waitForService(5000L);
        assertNotNull(registered,
                "EPackage should be registered with workflow.scope and workflow.stage properties");
        
        removeEPackage(objectId, stageActionService);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @EPackageStageActionService
    public void testOnUpdateKeepsRegistration(
            @InjectService(filter = "(storage.type=file)") EObjectStorageService storage,
            @InjectService StageActionService stageActionService,
            @InjectService(cardinality = 0, filter = "(emf.name=TestSensorModel)") ServiceAware<EPackage> ePackageServiceAware)
            throws Exception {

        EPackage testPackage = createTestEPackage();
        String objectId = "test-update-" + System.currentTimeMillis();
        ObjectMetadata metadata = createTestMetadata(objectId, TEST_STAGE);
        storage.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, objectId, testPackage, metadata).getValue();

        stageActionService.onEnter(enterContext(objectId, TEST_STAGE)).getValue();
        assertNotNull(ePackageServiceAware.waitForService(5000L));

        stageActionService.onUpdate(updateContext(objectId, TEST_STAGE)).getValue();

        EPackage stillRegistered = ePackageServiceAware.waitForService(5000L);
        assertNotNull(stillRegistered, "EPackage should remain registered after onUpdate");
        assertEquals(testPackage.getNsURI(), stillRegistered.getNsURI());
        
        removeEPackage(objectId, stageActionService);
    }
    
    private void removeEPackage(String objectId, StageActionService stageActionService) throws InvocationTargetException, InterruptedException {
    	stageActionService.onExit(exitContext(objectId, TEST_STAGE, ExitReason.DELETED, null)).getValue();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Test
    @EPackageStageActionService
    public void testOnExitUnregistersEPackage(
            @InjectService(filter = "(storage.type=file)") EObjectStorageService storage,
            @InjectService StageActionService stageActionService,
            @InjectService(cardinality = 0, filter = "(emf.name=TestSensorModel)") ServiceAware<EPackage> ePackageServiceAware)
            throws Exception {

        EPackage testPackage = createTestEPackage();
        String objectId = "object-id";
        ObjectMetadata metadata = createTestMetadata(objectId, TEST_STAGE);
        storage.storeObject(TEST_SCOPE, TEST_REGISTRY, TEST_STAGE, objectId, testPackage, metadata).getValue();

        stageActionService.onEnter(enterContext(objectId, TEST_STAGE)).getValue();
        assertNotNull(ePackageServiceAware.waitForService(5000L));

        stageActionService.onExit(exitContext(objectId, TEST_STAGE, ExitReason.DELETED, null)).getValue();
        
        assertTrue(ePackageServiceAware.isEmpty(),
                "EPackage should be unregistered after onExit");
    }

    @Test
    @EPackageStageActionService
    public void testOnExitWithoutPriorEnterIsNoOp(
            @InjectService StageActionService stageActionService) throws Exception {

        // No prior onEnter / onUpdate — nothing is tracked for this objectId.
        String objectId = "test-exit-unknown-" + System.currentTimeMillis();

        // Should resolve successfully and simply log that there is nothing to do.
        stageActionService.onExit(exitContext(objectId, TEST_STAGE, ExitReason.DELETED, null)).getValue();
    }

    private ActionContext enterContext(String objectId, String stage) {
        return new ActionContext(TEST_SCOPE, TEST_REGISTRY, objectId, EPACKAGE_TYPE,
                stage, null, null, null,
                "integration-test-user", Instant.now(), "enter", false,
                java.util.Map.of());
    }

    private ActionContext updateContext(String objectId, String stage) {
        return new ActionContext(TEST_SCOPE, TEST_REGISTRY, objectId, EPACKAGE_TYPE,
                stage, null, null, null,
                "integration-test-user", Instant.now(), "update", false,
                java.util.Map.of());
    }

    private ActionContext exitContext(String objectId, String stage, ExitReason reason, String targetStage) {
        return new ActionContext(TEST_SCOPE, TEST_REGISTRY, objectId, EPACKAGE_TYPE,
                stage, null, targetStage, reason,
                "integration-test-user", Instant.now(), "exit", false,
                java.util.Map.of());
    }

    private EPackage createTestEPackage() {
        EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
        ePackage.setName("TestSensorModel");
        ePackage.setNsPrefix("testsensor");
        ePackage.setNsURI("http://test.sensor.example.com/1.0");

        EClass sensorClass = EcoreFactory.eINSTANCE.createEClass();
        sensorClass.setName("Sensor");
        ePackage.getEClassifiers().add(sensorClass);

        return ePackage;
    }

    private ObjectMetadata createTestMetadata(String objectId, String stage) {
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectId(objectId);
        metadata.setObjectName("TestSensorModel");
        metadata.setVersion("1.0");
        metadata.setUploadUser("integration-test-user");
        metadata.setUploadTime(Instant.now().truncatedTo(ChronoUnit.SECONDS));
        metadata.setSourceChannel("MANUAL_UPLOAD");
        metadata.setObjectType("EPackage");
        metadata.setContentHash("test-hash-" + System.currentTimeMillis());
        metadata.setScope(TEST_SCOPE);
        metadata.setRegistry(TEST_REGISTRY);
        metadata.setStage(stage);

        metadata.getProperties().put("file.extension", "testsensor");
        metadata.getProperties().put("version", "1.0");

        return metadata;
    }
}
