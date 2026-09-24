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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.model.atlas.action.api.ActionContext;
import org.eclipse.fennec.model.atlas.action.api.StageActionService;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations.EPackageLuceneIndexSetup;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations.StorageSetup;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
import org.eclipse.fennec.model.atlas.workflow.tests.support.LuceneAwareTempDirExtension;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.Property.Scalar;
import org.osgi.test.common.annotation.Property.Type;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.Promises;

/**
 * OSGi integration tests for the ordered stage action chains (issue #296): two actions
 * registered as whiteboard services run in ranking order without a chain and in the
 * configured order with one; a chain that stops on failure skips what follows the failing
 * action, and both the failure and the skip are recorded on the object as diagnostics.
 */
@RequireEMF
@RequireConfigurationAdmin
@ExtendWith(LuceneAwareTempDirExtension.class)
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@DisplayName("Stage action chain OSGi Integration Tests")
@SuppressWarnings({ "unchecked", "rawtypes" })
public class StageActionChainIntegrationTest {

    static final String REGISTRY = "chained-schema";
    static final String SCOPE = "chain-scope";
    static final String ACTION_PROPERTY = "atlas.test.action";
    static final String ACTION_ID = "chain-it";
    private static final String OBJECT_ID = "chained-package";
    private static final String NS_URI = "http://test/chain/a";

    private final List<ServiceRegistration<StageActionService>> registrations = new CopyOnWriteArrayList<>();
    private final List<String> ran = new CopyOnWriteArrayList<>();

    @AfterEach
    void unregisterActions() {
        registrations.forEach(ServiceRegistration::unregister);
        registrations.clear();
    }

    @EPackageLuceneIndexSetup
    @StorageSetup
    @WithFactoryConfiguration(factoryPid = CommonTestAnnotations.PID_REGISTRY_SERVICE, name = REGISTRY, location = "?", properties = {
            @Property(key = "registry.name", value = REGISTRY),
            @Property(key = "registry.type", value = "SCHEMA"),
            @Property(key = "root.eclass.uri", value = "http://www.eclipse.org/emf/2002/Ecore#//EPackage"),
            @Property(key = "resourceSet.target", value = "(emf.name=ecore)"),
            @Property(key = "storageService.target", value = "(storage.type=file)"),
            @Property(key = "registry.target", value = "(registry=main)"),
            @Property(key = "stageActionService.target", value = "(" + ACTION_PROPERTY + "=" + ACTION_ID + ")"),
            @Property(key = "stageActionService.cardinality.minimum", scalar = Scalar.Integer, value = "2"),
            @Property(key = "stages", type = Type.Array, value = {
                    "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                    "{ \"name\" : \"release\", \"writable\" : true, \"final\": true}" }),
            @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
            @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:file", "release:file" }) })
    @Test
    @DisplayName("Without a chain the actions run in ranking order, and a failure is recorded without stopping the others")
    void rankingOrdersAndFailureIsRecorded(@InjectBundleContext BundleContext bundleContext,
            @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY + ")") ServiceAware<RegistryService> registryAware)
            throws Exception {
        register(bundleContext, "low", 1, Promises.failed(new IllegalStateException("low is broken")));
        register(bundleContext, "high", 10, Promises.resolved(null));

        RegistryService<EPackage> registryService = registryAware.waitForService(10000);
        assertNotNull(registryService, "the registry comes up once both actions are bound");

        ObjectMetadata returned = upload(registryService);

        assertEquals(List.of("high", "low"), ran, "highest ranking first");
        Diagnostic failed = only(registryService.getMetadataFromStage(SCOPE, "draft", OBJECT_ID), "stage-action/low");
        assertEquals("stage-action.failed", failed.getCode());
        assertEquals(DiagnosticSeverity.ERROR, failed.getSeverity());
        assertTrue(failed.getMessage().contains("low is broken"), failed.getMessage());
        assertEquals(failed.getId(), only(returned, "stage-action/low").getId(),
                "the caller's copy carries the same record");
        assertTrue(returned.getDiagnostics().stream().noneMatch(d -> "stage-action/high".equals(d.getProducer())),
                "a success with nothing to clear records nothing");
    }

    @EPackageLuceneIndexSetup
    @StorageSetup
    @WithFactoryConfiguration(factoryPid = CommonTestAnnotations.PID_REGISTRY_SERVICE, name = REGISTRY, location = "?", properties = {
            @Property(key = "registry.name", value = REGISTRY),
            @Property(key = "registry.type", value = "SCHEMA"),
            @Property(key = "root.eclass.uri", value = "http://www.eclipse.org/emf/2002/Ecore#//EPackage"),
            @Property(key = "resourceSet.target", value = "(emf.name=ecore)"),
            @Property(key = "storageService.target", value = "(storage.type=file)"),
            @Property(key = "registry.target", value = "(registry=main)"),
            @Property(key = "stageActionService.target", value = "(" + ACTION_PROPERTY + "=" + ACTION_ID + ")"),
            @Property(key = "stageActionService.cardinality.minimum", scalar = Scalar.Integer, value = "2"),
            @Property(key = "stage.action.chains", type = Type.Array, value = {
                    "{ \"stage\": \"draft\", \"actions\": [\"low\", \"high\"], \"onFailure\": \"stop\" }" }),
            @Property(key = "stages", type = Type.Array, value = {
                    "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                    "{ \"name\" : \"release\", \"writable\" : true, \"final\": true}" }),
            @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
            @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:file", "release:file" }) })
    @Test
    @DisplayName("A configured chain overrides the ranking, and stopping on failure skips the rest with a record")
    void chainOrdersAndStopsOnFailure(@InjectBundleContext BundleContext bundleContext,
            @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY + ")") ServiceAware<RegistryService> registryAware)
            throws Exception {
        register(bundleContext, "low", 1, Promises.failed(new IllegalStateException("low is broken")));
        register(bundleContext, "high", 10, Promises.resolved(null));

        RegistryService<EPackage> registryService = registryAware.waitForService(10000);
        assertNotNull(registryService);

        upload(registryService);

        assertEquals(List.of("low"), ran, "the chain runs low first and stops when it fails");
        ObjectMetadata draft = registryService.getMetadataFromStage(SCOPE, "draft", OBJECT_ID);
        assertEquals("stage-action.failed", only(draft, "stage-action/low").getCode());
        Diagnostic skipped = only(draft, "stage-action/high");
        assertEquals("stage-action.skipped", skipped.getCode());
        assertEquals(DiagnosticSeverity.WARNING, skipped.getSeverity());
        assertTrue(skipped.getMessage().contains("low"), skipped.getMessage());
    }

    private void register(BundleContext bundleContext, String name, int ranking, Promise<Void> answer) {
        StageActionService action = new StageActionService() {
            @Override
            public boolean supportsObjectType(String objectType) {
                return true;
            }

            @Override
            public Set<String> getTriggerStages() {
                return Set.of();
            }

            @Override
            public Set<ActionEvent> getTriggerEvents() {
                return Set.of(ActionEvent.ENTER);
            }

            @Override
            public Promise<Void> onEnter(ActionContext ctx) {
                ran.add(name);
                return answer;
            }

            @Override
            public Promise<Void> onUpdate(ActionContext ctx) {
                return Promises.resolved(null);
            }

            @Override
            public Promise<Void> onExit(ActionContext ctx) {
                return Promises.resolved(null);
            }

            @Override
            public boolean requiresReplayOnStartup() {
                return false;
            }

            @Override
            public boolean requiresReplayOnShutdown() {
                return false;
            }
        };
        Dictionary<String, Object> props = new Hashtable<>();
        props.put(ACTION_PROPERTY, ACTION_ID);
        props.put("stage.action.name", name);
        props.put(Constants.SERVICE_RANKING, ranking);
        registrations.add(bundleContext.registerService(StageActionService.class, action, props));
    }

    private ObjectMetadata upload(RegistryService<EPackage> registryService) throws Exception {
        EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
        pkg.setName("chained");
        pkg.setNsURI(NS_URI);
        pkg.setNsPrefix("chained");

        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectId(OBJECT_ID);
        metadata.setObjectName("chained");
        metadata.setVersion("1");
        metadata.getProperties().put(WorkflowConstants.NS_URI_METADATA_PROPERTY, NS_URI);

        return registryService.uploadToStage(SCOPE, "draft", pkg, metadata).getValue();
    }

    private static Diagnostic only(ObjectMetadata metadata, String producer) {
        List<Diagnostic> owned = metadata.getDiagnostics().stream().filter(d -> producer.equals(d.getProducer()))
                .toList();
        assertEquals(1, owned.size(), "exactly one root of " + producer + ", got " + metadata.getDiagnostics());
        return owned.get(0);
    }
}
