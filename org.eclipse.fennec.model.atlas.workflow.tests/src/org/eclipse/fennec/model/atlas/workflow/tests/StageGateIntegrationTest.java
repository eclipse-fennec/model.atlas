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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.model.atlas.action.api.Dependent;
import org.eclipse.fennec.model.atlas.action.api.GateContext;
import org.eclipse.fennec.model.atlas.action.api.GateDiagnostic;
import org.eclipse.fennec.model.atlas.action.api.GateTrigger;
import org.eclipse.fennec.model.atlas.action.api.GateVerdict;
import org.eclipse.fennec.model.atlas.action.api.StageGate;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.scope.api.StageGateRefusedException;
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
import org.osgi.util.promise.Promises;

/**
 * OSGi integration tests for the {@link StageGate} wiring of
 * {@code RegistryServiceImpl#transitionToStage} (issue #248).
 *
 * <p>
 * The unit tests prove the decision logic; what this test proves is the
 * whiteboard: a gate registered as a service, selected by the registry's
 * {@code stageGate.target} configuration, is asked before a transition commits,
 * and its refusal reaches the caller as a {@link StageGateRefusedException}
 * while the target stage stays empty.
 * </p>
 */
@RequireEMF
@RequireConfigurationAdmin
@ExtendWith(LuceneAwareTempDirExtension.class)
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@DisplayName("Stage gate OSGi Integration Tests")
@SuppressWarnings({ "unchecked", "rawtypes" })
public class StageGateIntegrationTest {

    static final String REGISTRY = "gated-schema";
    static final String SCOPE = "gate-scope";
    static final String GATE_PROPERTY = "atlas.test.gate";
    static final String GATE_ID = "stage-gate-it";
    private static final String OBJECT_ID = "gated-package";
    private static final String NS_URI = "http://test/gate/a";

    private final List<ServiceRegistration<StageGate>> registrations = new CopyOnWriteArrayList<>();

    @AfterEach
    void unregisterGates() {
        registrations.forEach(ServiceRegistration::unregister);
        registrations.clear();
    }

    /** A schema registry that admits only gates carrying {@code atlas.test.gate=stage-gate-it}. */
    @EPackageLuceneIndexSetup
    @StorageSetup
    @WithFactoryConfiguration(factoryPid = CommonTestAnnotations.PID_REGISTRY_SERVICE, name = REGISTRY, location = "?", properties = {
            @Property(key = "registry.name", value = REGISTRY),
            @Property(key = "registry.type", value = "SCHEMA"),
            @Property(key = "root.eclass.uri", value = "http://www.eclipse.org/emf/2002/Ecore#//EPackage"),
            @Property(key = "resourceSet.target", value = "(emf.name=ecore)"),
            @Property(key = "storageService.target", value = "(storage.type=file)"),
            @Property(key = "registry.target", value = "(registry=main)"),
            @Property(key = "stageGate.target", value = "(" + GATE_PROPERTY + "=" + GATE_ID + ")"),
            @Property(key = "stageGate.cardinality.minimum", scalar = Scalar.Integer, value = "1"),
            @Property(key = "stages", type = Type.Array, value = {
                    "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                    "{ \"name\" : \"release\", \"writable\" : true, \"final\": true}" }),
            @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
            @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:file", "release:file" }) })
    @Test
    @DisplayName("A registered gate is asked and its refusal keeps the target stage empty")
    void gateRefusesThroughTheWhiteboard(@InjectBundleContext BundleContext bundleContext,
            @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY + ")") ServiceAware<RegistryService> registryAware)
            throws Exception {

        List<GateContext> seen = new CopyOnWriteArrayList<>();
        register(bundleContext, new StageGate() {
            @Override
            public boolean supportsObjectType(String objectType) {
                return true;
            }

            @Override
            public org.osgi.util.promise.Promise<GateVerdict> beforeTransition(GateContext ctx) {
                seen.add(ctx);
                return Promises.resolved(GateVerdict.refuse("the test gate says no"));
            }
        });

        // the registry waits for its gate: cardinality.minimum=1
        RegistryService<EPackage> registryService = registryAware.waitForService(10000);
        assertNotNull(registryService, "the registry must come up once its gate is registered");

        upload(registryService, "draft");

        StageGateRefusedException refused = assertThrows(StageGateRefusedException.class,
                () -> registryService.transitionToStage(SCOPE, OBJECT_ID, "draft", "release"));

        assertTrue(refused.getMessage().contains("the test gate says no"), refused.getMessage());
        assertEquals(1, seen.size(), "the gate was asked exactly once");
        assertEquals("draft", seen.get(0).sourceStage());
        assertEquals("release", seen.get(0).targetStage());
        assertEquals(REGISTRY, seen.get(0).registry());
        assertNull(registryService.getMetadataFromStage(SCOPE, "release", OBJECT_ID),
                "a refused transition writes nothing into the target stage");
        assertNotNull(registryService.getMetadataFromStage(SCOPE, "draft", OBJECT_ID),
                "and leaves the source stage as it was");
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
            @Property(key = "stageGate.target", value = "(" + GATE_PROPERTY + "=" + GATE_ID + ")"),
            @Property(key = "stageGate.cardinality.minimum", scalar = Scalar.Integer, value = "1"),
            @Property(key = "stages", type = Type.Array, value = {
                    "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                    "{ \"name\" : \"release\", \"writable\" : true, \"final\": true}" }),
            @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
            @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:file", "release:file" }) })
    @Test
    @DisplayName("A passing gate lets the transition through")
    void gatePassesThroughTheWhiteboard(@InjectBundleContext BundleContext bundleContext,
            @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY + ")") ServiceAware<RegistryService> registryAware)
            throws Exception {

        register(bundleContext, new StageGate() {
            @Override
            public boolean supportsObjectType(String objectType) {
                return true;
            }

            @Override
            public org.osgi.util.promise.Promise<GateVerdict> beforeTransition(GateContext ctx) {
                return Promises.resolved(GateVerdict.pass());
            }
        });

        RegistryService<EPackage> registryService = registryAware.waitForService(10000);
        assertNotNull(registryService);

        upload(registryService, "draft");
        ObjectMetadata promoted = registryService.transitionToStage(SCOPE, OBJECT_ID, "draft", "release");

        assertEquals("release", promoted.getStage());
        assertNotNull(registryService.getMetadataFromStage(SCOPE, "release", OBJECT_ID));
    }

    /**
     * Issue #294: the veto is not only in the response. It is recorded on the object in
     * its source stage as a persisted diagnostic under the gate's producer, handed to the
     * caller in the exception, and cleared again once a later attempt passes.
     */
    @EPackageLuceneIndexSetup
    @StorageSetup
    @WithFactoryConfiguration(factoryPid = CommonTestAnnotations.PID_REGISTRY_SERVICE, name = REGISTRY, location = "?", properties = {
            @Property(key = "registry.name", value = REGISTRY),
            @Property(key = "registry.type", value = "SCHEMA"),
            @Property(key = "root.eclass.uri", value = "http://www.eclipse.org/emf/2002/Ecore#//EPackage"),
            @Property(key = "resourceSet.target", value = "(emf.name=ecore)"),
            @Property(key = "storageService.target", value = "(storage.type=file)"),
            @Property(key = "registry.target", value = "(registry=main)"),
            @Property(key = "stageGate.target", value = "(" + GATE_PROPERTY + "=" + GATE_ID + ")"),
            @Property(key = "stageGate.cardinality.minimum", scalar = Scalar.Integer, value = "1"),
            @Property(key = "stages", type = Type.Array, value = {
                    "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                    "{ \"name\" : \"release\", \"writable\" : true, \"final\": true}" }),
            @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
            @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:file", "release:file" }) })
    @Test
    @DisplayName("A refusal is recorded on the source copy as diagnostics, and a later pass clears it")
    void refusalIsRecordedAsDiagnostics(@InjectBundleContext BundleContext bundleContext,
            @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY + ")") ServiceAware<RegistryService> registryAware)
            throws Exception {

        GateDiagnostic finding = GateDiagnostic.error("no-go", "the test gate says no").inCategory("test")
                .at("//gated").withChild(GateDiagnostic.warning("detail", "and here is why").at("//gated/x"));
        AtomicReference<GateVerdict> answer = new AtomicReference<>(
                GateVerdict.refuse("the test gate says no", List.of(finding)));
        register(bundleContext, new StageGate() {
            @Override
            public boolean supportsObjectType(String objectType) {
                return true;
            }

            @Override
            public String producer() {
                return GATE_ID;
            }

            @Override
            public org.osgi.util.promise.Promise<GateVerdict> beforeTransition(GateContext ctx) {
                return Promises.resolved(answer.get());
            }
        });

        RegistryService<EPackage> registryService = registryAware.waitForService(10000);
        assertNotNull(registryService);
        upload(registryService, "draft", OBJECT_ID, NS_URI);

        StageGateRefusedException refused = assertThrows(StageGateRefusedException.class,
                () -> registryService.transitionToStage(SCOPE, OBJECT_ID, "draft", "release"));

        // the caller gets the findings and where to find them
        assertEquals(GateTrigger.TRANSITION, refused.trigger());
        assertEquals(SCOPE, refused.scope());
        assertEquals(REGISTRY, refused.registry());
        assertEquals("draft", refused.stage());
        assertEquals(OBJECT_ID, refused.objectId());
        assertEquals(List.of(finding), refused.diagnostics());

        // and the source copy carries them, as persisted diagnostics with ids
        ObjectMetadata draft = registryService.getMetadataFromStage(SCOPE, "draft", OBJECT_ID);
        Diagnostic recorded = only(draft, GATE_ID);
        assertNotNull(recorded.getId(), "the id was minted");
        assertEquals("no-go", recorded.getCode());
        assertEquals(DiagnosticSeverity.ERROR, recorded.getSeverity());
        assertEquals("test", recorded.getCategory());
        assertEquals("//gated", recorded.getTarget());
        assertEquals(1, recorded.getChildren().size());
        assertEquals("//gated/x", recorded.getChildren().get(0).getTarget());
        assertNull(registryService.getMetadataFromStage(SCOPE, "release", OBJECT_ID), "nothing moved");

        // the same finding again gets the same id: a client may keep referring to it
        StageGateRefusedException again = assertThrows(StageGateRefusedException.class,
                () -> registryService.transitionToStage(SCOPE, OBJECT_ID, "draft", "release"));
        assertEquals(1, again.diagnostics().size());
        assertEquals(recorded.getId(), only(registryService.getMetadataFromStage(SCOPE, "draft", OBJECT_ID), GATE_ID)
                .getId(), "re-validation does not re-mint the id");

        // now the gate is satisfied: the veto is cleared on the copy that moves and on the one that stays
        answer.set(GateVerdict.pass());
        ObjectMetadata promoted = registryService.transitionToStage(SCOPE, OBJECT_ID, "draft", "release");
        assertTrue(promoted.getDiagnostics().stream().noneMatch(d -> GATE_ID.equals(d.getProducer())),
                "the promoted copy carries no stale veto, got " + promoted.getDiagnostics());
        ObjectMetadata released = registryService.getMetadataFromStage(SCOPE, "release", OBJECT_ID);
        assertTrue(released.getDiagnostics().stream().noneMatch(d -> GATE_ID.equals(d.getProducer())));
        ObjectMetadata draftAfter = registryService.getMetadataFromStage(SCOPE, "draft", OBJECT_ID);
        if (draftAfter != null) {
            assertTrue(draftAfter.getDiagnostics().stream().noneMatch(d -> GATE_ID.equals(d.getProducer())),
                    "the source copy that stayed behind was brought up to date too");
        }
    }

    /**
     * Issue #294: the gates guard a delete as well. A refusal keeps the object and records
     * the veto on it; {@code force} deletes anyway and records what the gate said about the
     * dependents on those dependents.
     */
    @EPackageLuceneIndexSetup
    @StorageSetup
    @WithFactoryConfiguration(factoryPid = CommonTestAnnotations.PID_REGISTRY_SERVICE, name = REGISTRY, location = "?", properties = {
            @Property(key = "registry.name", value = REGISTRY),
            @Property(key = "registry.type", value = "SCHEMA"),
            @Property(key = "root.eclass.uri", value = "http://www.eclipse.org/emf/2002/Ecore#//EPackage"),
            @Property(key = "resourceSet.target", value = "(emf.name=ecore)"),
            @Property(key = "storageService.target", value = "(storage.type=file)"),
            @Property(key = "registry.target", value = "(registry=main)"),
            @Property(key = "stageGate.target", value = "(" + GATE_PROPERTY + "=" + GATE_ID + ")"),
            @Property(key = "stageGate.cardinality.minimum", scalar = Scalar.Integer, value = "1"),
            @Property(key = "stages", type = Type.Array, value = {
                    "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                    "{ \"name\" : \"release\", \"writable\" : true, \"final\": true}" }),
            @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
            @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:file", "release:file" }) })
    @Test
    @DisplayName("A gate refuses a delete; force deletes anyway and records the consequence on the dependent")
    void deleteIsGuardedAndForceRecordsConsequences(@InjectBundleContext BundleContext bundleContext,
            @InjectService(cardinality = 0, filter = "(registry.name=" + REGISTRY + ")") ServiceAware<RegistryService> registryAware)
            throws Exception {

        String dependentId = "gated-instance";
        List<GateContext> seen = new CopyOnWriteArrayList<>();
        register(bundleContext, new StageGate() {
            @Override
            public boolean supportsObjectType(String objectType) {
                return true;
            }

            @Override
            public String producer() {
                return GATE_ID;
            }

            @Override
            public org.osgi.util.promise.Promise<GateVerdict> beforeTransition(GateContext ctx) {
                return Promises.resolved(GateVerdict.pass());
            }

            @Override
            public org.osgi.util.promise.Promise<GateVerdict> beforeDelete(GateContext ctx) {
                seen.add(ctx);
                if (!OBJECT_ID.equals(ctx.objectId())) {
                    return Promises.resolved(GateVerdict.pass());
                }
                return Promises.resolved(GateVerdict.refuse("1 instance depends on this schema", List.of(
                        GateDiagnostic.error("dependents", "1 instance depends on this schema"),
                        GateDiagnostic.warning("schema-gone", "the schema this instance conforms to is gone")
                                .on(Dependent.inSameRegistry("draft", dependentId)))));
            }
        });

        RegistryService<EPackage> registryService = registryAware.waitForService(10000);
        assertNotNull(registryService);
        upload(registryService, "draft", OBJECT_ID, NS_URI);
        upload(registryService, "draft", dependentId, "http://test/gate/instance");

        // refused: the object stays, the veto is on it, the dependent is untouched
        InvocationTargetException failed = assertThrows(InvocationTargetException.class,
                () -> registryService.deleteFromStage(SCOPE, "draft", OBJECT_ID).getValue());
        StageGateRefusedException refused = assertInstanceOf(StageGateRefusedException.class, failed.getCause());
        assertEquals(GateTrigger.DELETE, refused.trigger());
        assertEquals("draft", refused.stage());
        assertTrue(refused.getMessage().contains("1 instance depends on this schema"), refused.getMessage());
        assertEquals(1, refused.diagnostics().size(), "the finding about the object itself");
        assertEquals(1, seen.size());
        assertTrue(seen.get(0).isDelete());
        assertNull(seen.get(0).targetStage());
        assertNotNull(registryService.getMetadataFromStage(SCOPE, "draft", OBJECT_ID), "the object stays");
        assertEquals("dependents", only(registryService.getMetadataFromStage(SCOPE, "draft", OBJECT_ID), GATE_ID)
                .getCode());
        assertTrue(registryService.getMetadataFromStage(SCOPE, "draft", dependentId).getDiagnostics().isEmpty(),
                "nothing happened to the dependent, so nothing was recorded on it");

        // forced: the object goes, the dependent learns about it
        assertTrue(registryService.deleteFromStage(SCOPE, "draft", OBJECT_ID, true).getValue());
        assertNull(registryService.getMetadataFromStage(SCOPE, "draft", OBJECT_ID), "the object is gone");
        Diagnostic consequence = only(registryService.getMetadataFromStage(SCOPE, "draft", dependentId), GATE_ID);
        assertEquals("schema-gone", consequence.getCode());
        assertEquals(DiagnosticSeverity.WARNING, consequence.getSeverity());
        assertNotNull(consequence.getId());
    }

    private static Diagnostic only(ObjectMetadata metadata, String producer) {
        List<Diagnostic> owned = metadata.getDiagnostics().stream().filter(d -> producer.equals(d.getProducer()))
                .toList();
        assertEquals(1, owned.size(), "exactly one root of " + producer + ", got " + metadata.getDiagnostics());
        return owned.get(0);
    }

    private void register(BundleContext bundleContext, StageGate gate) {
        Dictionary<String, Object> props = new Hashtable<>();
        props.put(GATE_PROPERTY, GATE_ID);
        registrations.add(bundleContext.registerService(StageGate.class, gate, props));
    }

    private void upload(RegistryService<EPackage> registryService, String stage)
            throws InvocationTargetException, InterruptedException {
        upload(registryService, stage, OBJECT_ID, NS_URI);
    }

    private void upload(RegistryService<EPackage> registryService, String stage, String objectId, String nsUri)
            throws InvocationTargetException, InterruptedException {
        EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
        pkg.setName(objectId);
        pkg.setNsURI(nsUri);
        pkg.setNsPrefix(objectId);

        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectId(objectId);
        metadata.setObjectName(objectId);
        metadata.setVersion("1");
        metadata.getProperties().put(WorkflowConstants.NS_URI_METADATA_PROPERTY, nsUri);

        registryService.uploadToStage(SCOPE, stage, pkg, metadata).getValue();
    }
}
