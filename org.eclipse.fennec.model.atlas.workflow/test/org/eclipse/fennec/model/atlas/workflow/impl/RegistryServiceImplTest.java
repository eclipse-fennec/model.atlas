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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.util.List;
import java.util.Set;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceImpl;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectRegistryService;
import org.eclipse.fennec.model.atlas.mgmt.api.EObjectStorageService;
import org.eclipse.fennec.model.atlas.mgmt.diagnostics.Diagnostics;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.action.api.ActionContext;
import org.eclipse.fennec.model.atlas.action.api.Dependent;
import org.eclipse.fennec.model.atlas.action.api.GateContext;
import org.eclipse.fennec.model.atlas.action.api.GateDiagnostic;
import org.eclipse.fennec.model.atlas.action.api.GateTrigger;
import org.eclipse.fennec.model.atlas.action.api.GateVerdict;
import org.eclipse.fennec.model.atlas.action.api.StageActionService;
import org.eclipse.fennec.model.atlas.action.api.StageGate;
import org.eclipse.fennec.model.atlas.scope.api.StageGateRefusedException;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.workflow.RegistryServiceCollector;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.Promises;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link RegistryServiceImpl#isEClassCompatibleWithRegistry(EClass)}.
 *
 * @see <a href="https://github.com/eclipse-fennec/model.atlas/issues/188">issue #188</a>
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RegistryServiceImpl Unit Tests")
public class RegistryServiceImplTest {

    private static final String TEST_NS_URI = "http://example.org/test/1.0.0";

    private ResourceSet resourceSet;
    private EClass personClass;
    private EClass employeeClass;
    private EClass unrelatedClass;

    @BeforeEach
    void setUp() {
        EPackage testPackage = EcoreFactory.eINSTANCE.createEPackage();
        testPackage.setName("test");
        testPackage.setNsPrefix("test");
        testPackage.setNsURI(TEST_NS_URI);

        personClass = EcoreFactory.eINSTANCE.createEClass();
        personClass.setName("Person");
        employeeClass = EcoreFactory.eINSTANCE.createEClass();
        employeeClass.setName("Employee");
        employeeClass.getESuperTypes().add(personClass);
        unrelatedClass = EcoreFactory.eINSTANCE.createEClass();
        unrelatedClass.setName("Unrelated");
        testPackage.getEClassifiers().addAll(List.of(personClass, employeeClass, unrelatedClass));

        resourceSet = new ResourceSetImpl();
        Resource resource = new ResourceImpl(URI.createURI(TEST_NS_URI));
        resource.getContents().add(testPackage);
        resourceSet.getResources().add(resource);
    }

    private RegistryServiceImpl<EObject> createService(String... rootEClassUris) {
        return createService(List.of(), rootEClassUris);
    }

    private RegistryServiceImpl<EObject> createService(
            List<EObjectStorageService<EObject>> storageServices, String... rootEClassUris) {
        return createService(storageServices, rootEClassUris, new String[0]);
    }

    private RegistryServiceImpl<EObject> createService(
            List<EObjectStorageService<EObject>> storageServices, String[] rootEClassUris,
            String[] derivedEClassUris) {
        RegistryServiceConfig config = mock(RegistryServiceConfig.class);
        when(config.registry_name()).thenReturn("test-registry");
        when(config.registry_description()).thenReturn("");
        when(config.registry_type()).thenReturn("OTHER");
        when(config.workflow_transitions()).thenReturn(new String[] { "draft:release" });
        when(config.stage_storage_mappings()).thenReturn(new String[] { "draft:file", "release:file" });
        when(config.stages()).thenReturn(new String[] {
                "{\"name\": \"draft\", \"writable\": true, \"final\": false}",
                "{\"name\": \"release\", \"writable\": true, \"final\": true}" });
        when(config.root_eclass_uri()).thenReturn(rootEClassUris);
        // lenient: never read when an earlier config value already fails activation
        org.mockito.Mockito.lenient().when(config.derived_eclass_uri()).thenReturn(derivedEClassUris);
        return new RegistryServiceImpl<>(storageServices, resourceSet, EcorePackage.eINSTANCE, config);
    }

    @Nested
    @DisplayName("Registry rooted at the implicit EObject root")
    class EObjectRootTests {

        private RegistryServiceImpl<EObject> service;

        @BeforeEach
        void setUp() {
            service = createService(EcorePackage.eNS_URI + "#//EObject");
        }

        @Test
        @DisplayName("Should accept an EClass without explicit super types")
        void shouldAcceptEClassWithoutSuperTypes() {
            assertTrue(service.isEClassCompatibleWithRegistry(personClass));
        }

        @Test
        @DisplayName("Should accept an EClass with explicit super types")
        void shouldAcceptEClassWithSuperTypes() {
            assertTrue(service.isEClassCompatibleWithRegistry(employeeClass));
        }

        @Test
        @DisplayName("Should accept the EObject EClass itself")
        void shouldAcceptEObjectItself() {
            assertTrue(service.isEClassCompatibleWithRegistry(EcorePackage.Literals.EOBJECT));
        }
    }

    @Nested
    @DisplayName("Dynamic stage action service binding")
    class StageActionBindingTests {

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("A stage action service bound after scope activation receives the startup replay")
        void lateBoundStageActionServiceReceivesStartupReplay() {
            EObjectStorageService<EObject> storage = mock(EObjectStorageService.class);
            when(storage.getStorageType()).thenReturn("file");
            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setObjectId("object-1");
            metadata.setStage("draft");
            when(storage.queryObjects(any())).thenReturn(Promises.resolved(List.of(metadata)));

            RegistryServiceImpl<EObject> service = createService(List.of(storage), TEST_NS_URI + "#//Person");

            // the scope activates while no stage action service is bound yet - with the
            // former static reference this ordering silently lost all stage actions
            service.activate("test-scope");

            StageActionService stageAction = mock(StageActionService.class);
            when(stageAction.requiresReplayOnStartup()).thenReturn(true);
            when(stageAction.getTriggerStages()).thenReturn(Set.of("draft"));
            when(stageAction.getTriggerEvents()).thenReturn(Set.of());
            when(stageAction.supportsObjectType(any())).thenReturn(true);
            when(stageAction.onEnter(any())).thenReturn(Promises.resolved(null));

            service.addStageActionService(stageAction);

            verify(stageAction).onEnter(any());
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("A stage action service without startup replay is not replayed on late binding")
        void lateBoundStageActionServiceWithoutReplayIsNotReplayed() {
            RegistryServiceImpl<EObject> service = createService(TEST_NS_URI + "#//Person");
            service.activate("test-scope");

            StageActionService stageAction = mock(StageActionService.class);
            when(stageAction.requiresReplayOnStartup()).thenReturn(false);

            service.addStageActionService(stageAction);

            verify(stageAction, never()).onEnter(any());
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("The event carries the fingerprint of the object it is about")
        void actionContextCarriesTheFingerprint() {
            EObjectStorageService<EObject> storage = mock(EObjectStorageService.class);
            when(storage.getStorageType()).thenReturn("file");
            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setObjectId("object-1");
            metadata.setStage("draft");
            metadata.setFingerprint("fp1:abc123");
            when(storage.queryObjects(any())).thenReturn(Promises.resolved(List.of(metadata)));

            RegistryServiceImpl<EObject> service = createService(List.of(storage), TEST_NS_URI + "#//Person");
            service.activate("test-scope");

            StageActionService stageAction = replayingAction();
            service.addStageActionService(stageAction);

            ArgumentCaptor<ActionContext> captor = ArgumentCaptor.forClass(ActionContext.class);
            verify(stageAction).onEnter(captor.capture());
            // an action addressed by revision - a GDPR review, a skip-if-already-done guard -
            // would otherwise re-read metadata the workflow held when it raised the event
            assertEquals("fp1:abc123", captor.getValue().fingerprint());
            assertEquals("fp1:abc123", captor.getValue().metadata().get(ActionContext.FINGERPRINT));
        }

        @SuppressWarnings("unchecked")
        @Test
        @DisplayName("An object without a fingerprint carries no fingerprint entry at all")
        void actionContextOmitsAnAbsentFingerprint() {
            EObjectStorageService<EObject> storage = mock(EObjectStorageService.class);
            when(storage.getStorageType()).thenReturn("file");
            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setObjectId("object-1");
            metadata.setStage("draft");
            when(storage.queryObjects(any())).thenReturn(Promises.resolved(List.of(metadata)));

            RegistryServiceImpl<EObject> service = createService(List.of(storage), TEST_NS_URI + "#//Person");
            service.activate("test-scope");

            StageActionService stageAction = replayingAction();
            service.addStageActionService(stageAction);

            ArgumentCaptor<ActionContext> captor = ArgumentCaptor.forClass(ActionContext.class);
            verify(stageAction).onEnter(captor.capture());
            // absent, not present-and-null, so containsKey means "the workflow knew one"
            assertNull(captor.getValue().fingerprint());
            assertFalse(captor.getValue().metadata().containsKey(ActionContext.FINGERPRINT));
        }

        private StageActionService replayingAction() {
            StageActionService stageAction = mock(StageActionService.class);
            when(stageAction.requiresReplayOnStartup()).thenReturn(true);
            when(stageAction.getTriggerStages()).thenReturn(Set.of("draft"));
            when(stageAction.getTriggerEvents()).thenReturn(Set.of());
            when(stageAction.supportsObjectType(any())).thenReturn(true);
            when(stageAction.onEnter(any())).thenReturn(Promises.resolved(null));
            return stageAction;
        }
    }

    @Nested
    @DisplayName("Stage gates decide a transition before it commits (issue #248)")
    class StageGateTests {

        private EObjectStorageService<EObject> storage;
        private RegistryServiceImpl<EObject> service;
        private StageActionService stageAction;
        private EObject person;

        @SuppressWarnings("unchecked")
        @BeforeEach
        void setUp() {
            person = personClass.getEPackage().getEFactoryInstance().create(personClass);
            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setObjectId("object-1");
            metadata.setObjectType(TEST_NS_URI + "#//Person");
            metadata.setStage("draft");
            metadata.setFingerprint("fp1:abc123");

            storage = mock(EObjectStorageService.class);
            when(storage.getStorageType()).thenReturn("file");
            org.mockito.Mockito.lenient().when(storage.retrieveObject("test-scope", "test-registry", "draft", "object-1"))
                    .thenReturn(Promises.resolved(person));
            org.mockito.Mockito.lenient().when(storage.retrieveMetadata("test-scope", "test-registry", "draft", "object-1"))
                    .thenReturn(Promises.resolved(metadata));
            // the target address is free
            org.mockito.Mockito.lenient().when(storage.retrieveMetadata("test-scope", "test-registry", "release", "object-1"))
                    .thenReturn(Promises.resolved(null));
            org.mockito.Mockito.lenient().when(storage.storeObject(any(), any(), any(), any(), any(), any()))
                    .thenReturn(Promises.resolved(metadata));
            // a refusal is recorded on the source copy since issue #294
            org.mockito.Mockito.lenient().when(storage.updateDiagnostics(any(), any(), any(), any(), any(), any()))
                    .thenReturn(Promises.resolved(metadata));

            service = createService(List.of(storage), TEST_NS_URI + "#//Person");

            // a post-commit action, to prove a refused transition fires none
            stageAction = mock(StageActionService.class);
            org.mockito.Mockito.lenient().when(stageAction.requiresReplayOnStartup()).thenReturn(false);
            org.mockito.Mockito.lenient().when(stageAction.supportsObjectType(any())).thenReturn(true);
            org.mockito.Mockito.lenient().when(stageAction.getTriggerStages()).thenReturn(Set.of());
            org.mockito.Mockito.lenient().when(stageAction.getTriggerEvents()).thenReturn(Set.of());
            org.mockito.Mockito.lenient().when(stageAction.onEnter(any())).thenReturn(Promises.resolved(null));
            service.addStageActionService(stageAction);
        }

        @Test
        @DisplayName("A refusing gate aborts the transition with its reason; nothing is written and no action fires")
        void refusalLeavesTheTargetUntouched() {
            StageGate gate = mock(StageGate.class);
            when(gate.supportsObjectType(any())).thenReturn(true);
            when(gate.beforeTransition(any()))
                    .thenReturn(Promises.resolved(GateVerdict.refuse("the source does not compile in release")));
            service.addStageGate(gate);

            StageGateRefusedException refused = org.junit.jupiter.api.Assertions.assertThrows(
                    StageGateRefusedException.class,
                    () -> service.transitionToStage("test-scope", "object-1", "draft", "release"));

            assertTrue(refused.getMessage().contains("the source does not compile in release"),
                    "the caller reads the gate's reason, got: " + refused.getMessage());
            assertTrue(refused.getMessage().contains("'draft'") && refused.getMessage().contains("'release'"),
                    "the refusal names both stages, got: " + refused.getMessage());
            verify(storage, never()).storeObject(any(), any(), any(), any(), any(), any());
            verify(storage, never()).deleteObject(any(), any(), any(), any());
            verify(stageAction, never()).onEnter(any());
            verify(stageAction, never()).onExit(any());
        }

        @Test
        @DisplayName("A passing gate lets the transition commit as before")
        void passLetsTheTransitionCommit() {
            StageGate gate = mock(StageGate.class);
            when(gate.supportsObjectType(any())).thenReturn(true);
            when(gate.beforeTransition(any())).thenReturn(Promises.resolved(GateVerdict.pass()));
            service.addStageGate(gate);

            ObjectMetadata promoted = service.transitionToStage("test-scope", "object-1", "draft", "release");

            assertEquals("release", promoted.getStage());
            verify(storage).storeObject(any(), any(), any(), any(), any(), any());
            verify(stageAction).onEnter(any());
        }

        @Test
        @DisplayName("The gate sees the pending transition: both stages, the type and the fingerprint")
        void gateContextDescribesThePendingTransition() {
            StageGate gate = mock(StageGate.class);
            when(gate.supportsObjectType(any())).thenReturn(true);
            when(gate.beforeTransition(any())).thenReturn(Promises.resolved(GateVerdict.pass()));
            service.addStageGate(gate);

            service.transitionToStage("test-scope", "object-1", "draft", "release");

            ArgumentCaptor<GateContext> captor = ArgumentCaptor.forClass(GateContext.class);
            verify(gate).beforeTransition(captor.capture());
            GateContext ctx = captor.getValue();
            assertEquals("test-scope", ctx.scope());
            assertEquals("test-registry", ctx.registry());
            assertEquals("object-1", ctx.objectId());
            assertEquals(TEST_NS_URI + "#//Person", ctx.objectType());
            assertEquals("draft", ctx.sourceStage());
            assertEquals("release", ctx.targetStage());
            assertEquals("fp1:abc123", ctx.fingerprint());
        }

        @Test
        @DisplayName("A gate that does not support the object's type is not asked")
        void unsupportedTypeIsNotConsulted() {
            StageGate gate = mock(StageGate.class);
            when(gate.supportsObjectType(any())).thenReturn(false);
            service.addStageGate(gate);

            service.transitionToStage("test-scope", "object-1", "draft", "release");

            verify(gate, never()).beforeTransition(any());
            verify(storage).storeObject(any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("A gate that cannot decide fails the transition closed: nothing is written")
        void undecidedGateFailsClosed() {
            StageGate gate = mock(StageGate.class);
            when(gate.supportsObjectType(any())).thenReturn(true);
            IllegalStateException fault = new IllegalStateException("compiler exploded");
            when(gate.beforeTransition(any())).thenReturn(Promises.failed(fault));
            service.addStageGate(gate);

            IllegalStateException thrown = org.junit.jupiter.api.Assertions.assertThrows(IllegalStateException.class,
                    () -> service.transitionToStage("test-scope", "object-1", "draft", "release"));

            // a fault, not a refusal: the caller must not read it as "fix your object"
            org.junit.jupiter.api.Assertions.assertSame(fault, thrown.getCause());
            verify(storage, never()).storeObject(any(), any(), any(), any(), any(), any());
            verify(stageAction, never()).onEnter(any());
        }

        @Test
        @DisplayName("A gate removed again is no longer asked")
        void removedGateIsNotConsulted() {
            // no stubbing: a removed gate must not be touched at all, and strict Mockito
            // would flag any stub it never reached
            StageGate gate = mock(StageGate.class);
            service.addStageGate(gate);
            service.removeStageGate(gate);

            service.transitionToStage("test-scope", "object-1", "draft", "release");

            verify(gate, never()).beforeTransition(any());
            verify(storage).storeObject(any(), any(), any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("Gate verdicts carry diagnostics, guard deletes and record consequences (issue #294)")
    class GateDiagnosticsTests {

        private static final String PRODUCER = "qvt-gate";
        private static final String DEPENDENT_ID = "instance-7";

        private EObjectStorageService<EObject> storage;
        private RegistryServiceImpl<EObject> service;
        /** The stored draft copy of the object under decision; the storage double writes diagnostics onto it. */
        private ObjectMetadata metadata;
        /** Another draft object of this registry, named as a dependent. */
        private ObjectMetadata dependent;

        @SuppressWarnings("unchecked")
        @BeforeEach
        void setUp() {
            EObject person = personClass.getEPackage().getEFactoryInstance().create(personClass);
            metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setObjectId("object-1");
            metadata.setObjectType(TEST_NS_URI + "#//Person");
            metadata.setStage("draft");
            dependent = ManagementFactory.eINSTANCE.createObjectMetadata();
            dependent.setObjectId(DEPENDENT_ID);
            dependent.setObjectType(TEST_NS_URI + "#//Person");
            dependent.setStage("draft");

            storage = mock(EObjectStorageService.class);
            when(storage.getStorageType()).thenReturn("file");
            lenient().when(storage.retrieveObject("test-scope", "test-registry", "draft", "object-1"))
                    .thenReturn(Promises.resolved(person));
            lenient().when(storage.retrieveMetadata("test-scope", "test-registry", "draft", "object-1"))
                    .thenAnswer(inv -> Promises.resolved(EcoreUtil.copy(metadata)));
            lenient().when(storage.retrieveMetadata("test-scope", "test-registry", "release", "object-1"))
                    .thenReturn(Promises.resolved(null));
            lenient().when(storage.retrieveMetadata("test-scope", "test-registry", "draft", DEPENDENT_ID))
                    .thenAnswer(inv -> Promises.resolved(EcoreUtil.copy(dependent)));
            lenient().when(storage.storeObject(any(), any(), any(), any(), any(), any()))
                    .thenAnswer(inv -> Promises.resolved(inv.getArgument(5)));
            lenient().when(storage.deleteObject(any(), any(), any(), any())).thenReturn(Promises.resolved(true));
            // the storage double does what the real one does: replaces the producer's roots
            lenient().when(storage.updateDiagnostics(eq("test-scope"), eq("test-registry"), eq("draft"), any(),
                    any(), any())).thenAnswer(inv -> {
                        ObjectMetadata target = DEPENDENT_ID.equals(inv.getArgument(3)) ? dependent : metadata;
                        Diagnostics.replaceOwned(target, inv.getArgument(4), inv.getArgument(5), Instant.now());
                        return Promises.resolved(EcoreUtil.copy(target));
                    });

            service = createService(List.of(storage), TEST_NS_URI + "#//Person");
            // a delete evicts the registry cache, which DS injects into a private field
            EObjectRegistryService<EObject> cache = mock(EObjectRegistryService.class);
            try {
                java.lang.reflect.Field field = RegistryServiceImpl.class.getDeclaredField("registryService");
                field.setAccessible(true);
                field.set(service, cache);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("RegistryServiceImpl.registryService moved; adjust the test", e);
            }
        }

        private StageGate gate() {
            StageGate gate = mock(StageGate.class);
            when(gate.supportsObjectType(any())).thenReturn(true);
            lenient().when(gate.producer()).thenReturn(PRODUCER);
            return gate;
        }

        private static GateVerdict dependentsVeto() {
            return GateVerdict.refuse("2 instances depend on this schema", List.of(
                    GateDiagnostic.error("dependents", "2 instances depend on this schema"),
                    GateDiagnostic.warning("schema-gone", "the schema this instance conforms to is gone")
                            .on(Dependent.inSameRegistry("draft", DEPENDENT_ID))));
        }

        private static Diagnostic only(ObjectMetadata m, String producer) {
            List<Diagnostic> owned = m.getDiagnostics().stream().filter(d -> producer.equals(d.getProducer()))
                    .toList();
            assertEquals(1, owned.size(), "exactly one root of " + producer + ", got " + m.getDiagnostics());
            return owned.get(0);
        }

        @Test
        @DisplayName("A refused transition records the veto on the source copy and hands it to the caller")
        void refusedTransitionRecordsTheVetoOnTheSourceCopy() {
            StageGate gate = gate();
            GateDiagnostic finding = GateDiagnostic.error("qvto.does-not-compile", "does not compile")
                    .inCategory("compile").at("Lib.qvto")
                    .withChild(GateDiagnostic.error("qvto.compiler-finding", "unresolved import").at("3:7"));
            when(gate.beforeTransition(any()))
                    .thenReturn(Promises.resolved(GateVerdict.refuse("does not compile", List.of(finding))));
            service.addStageGate(gate);

            StageGateRefusedException refused = assertThrows(StageGateRefusedException.class,
                    () -> service.transitionToStage("test-scope", "object-1", "draft", "release"));

            // the caller gets the findings and where they were recorded
            assertEquals(GateTrigger.TRANSITION, refused.trigger());
            assertEquals("draft", refused.stage());
            assertEquals("object-1", refused.objectId());
            assertEquals("test-registry", refused.registry());
            assertEquals(List.of(finding), refused.diagnostics());
            // the source copy now carries the veto, as a persisted diagnostic with an id
            verify(storage).updateDiagnostics(eq("test-scope"), eq("test-registry"), eq("draft"), eq("object-1"),
                    eq(PRODUCER), any());
            Diagnostic recorded = only(metadata, PRODUCER);
            assertNotNull(recorded.getId(), "the id was minted");
            assertEquals("qvto.does-not-compile", recorded.getCode());
            assertEquals(DiagnosticSeverity.ERROR, recorded.getSeverity());
            assertEquals("Lib.qvto", recorded.getTarget());
            assertEquals("compile", recorded.getCategory());
            assertEquals(1, recorded.getChildren().size());
            assertEquals("3:7", recorded.getChildren().get(0).getTarget());
            // and nothing moved
            verify(storage, never()).storeObject(any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("A pass clears the veto the same gate recorded on an earlier attempt")
        void passingRerunClearsAnEarlierVeto() {
            Diagnostics.replaceOwned(metadata, PRODUCER,
                    List.of(GateDiagnostics.toModel(GateDiagnostic.error("qvto.does-not-compile", "stale veto"))),
                    Instant.now());
            StageGate gate = gate();
            when(gate.beforeTransition(any())).thenReturn(Promises.resolved(GateVerdict.pass()));
            service.addStageGate(gate);

            service.transitionToStage("test-scope", "object-1", "draft", "release");

            ArgumentCaptor<ObjectMetadata> stored = ArgumentCaptor.forClass(ObjectMetadata.class);
            verify(storage).storeObject(any(), any(), eq("release"), eq("object-1"), any(), stored.capture());
            assertTrue(stored.getValue().getDiagnostics().isEmpty(), "the copy that moved carries no stale veto");
            // the source copy stays (delete_after_transition is off) and is brought up to date too
            verify(storage).updateDiagnostics(eq("test-scope"), eq("test-registry"), eq("draft"), eq("object-1"),
                    eq(PRODUCER), eq(List.of()));
            assertTrue(metadata.getDiagnostics().isEmpty());
        }

        @Test
        @DisplayName("A pass with warnings lets the warnings travel with the object")
        void passWithWarningsTravelsWithTheObject() {
            StageGate gate = gate();
            when(gate.beforeTransition(any())).thenReturn(Promises.resolved(
                    GateVerdict.pass(List.of(GateDiagnostic.warning("deprecated-import", "imports a deprecated library")))));
            service.addStageGate(gate);

            service.transitionToStage("test-scope", "object-1", "draft", "release");

            ArgumentCaptor<ObjectMetadata> stored = ArgumentCaptor.forClass(ObjectMetadata.class);
            verify(storage).storeObject(any(), any(), eq("release"), eq("object-1"), any(), stored.capture());
            Diagnostic carried = only(stored.getValue(), PRODUCER);
            assertEquals("deprecated-import", carried.getCode());
            assertEquals(DiagnosticSeverity.WARNING, carried.getSeverity());
            assertNotNull(carried.getId());
        }

        @Test
        @DisplayName("A refused delete keeps the object, records the veto on it and leaves the dependents alone")
        void refusedDeleteKeepsTheObject() {
            StageGate gate = gate();
            when(gate.beforeDelete(any())).thenReturn(Promises.resolved(dependentsVeto()));
            service.addStageGate(gate);

            InvocationTargetException failed = assertThrows(InvocationTargetException.class,
                    () -> service.deleteFromStage("test-scope", "draft", "object-1").getValue());
            StageGateRefusedException refused = assertInstanceOf(StageGateRefusedException.class, failed.getCause());

            assertEquals(GateTrigger.DELETE, refused.trigger());
            assertTrue(refused.getMessage().contains("Cannot delete object object-1"), refused.getMessage());
            assertTrue(refused.getMessage().contains("2 instances depend on this schema"), refused.getMessage());
            assertEquals(1, refused.diagnostics().size(), "only the finding about the object itself is handed over");
            assertEquals("dependents", refused.diagnostics().get(0).code());
            verify(storage, never()).deleteObject(any(), any(), any(), any());
            verify(gate, never()).beforeTransition(any());
            assertEquals("dependents", only(metadata, PRODUCER).getCode());
            assertTrue(dependent.getDiagnostics().isEmpty(), "nothing happened to the dependent, so nothing is recorded");
        }

        @Test
        @DisplayName("A forced delete goes through and records the consequence on the dependents instead")
        void forcedDeleteRecordsTheConsequenceOnDependents() throws Exception {
            StageGate gate = gate();
            when(gate.beforeDelete(any())).thenReturn(Promises.resolved(dependentsVeto()));
            service.addStageGate(gate);

            assertTrue(service.deleteFromStage("test-scope", "draft", "object-1", true).getValue());

            verify(storage).deleteObject("test-scope", "test-registry", "draft", "object-1");
            Diagnostic consequence = only(dependent, PRODUCER);
            assertEquals("schema-gone", consequence.getCode());
            assertEquals(DiagnosticSeverity.WARNING, consequence.getSeverity());
            assertNotNull(consequence.getId());
            // the veto about the deleted object itself is not written anywhere: the object is gone
            verify(storage, never()).updateDiagnostics(any(), any(), any(), eq("object-1"), any(), any());
        }

        @Test
        @DisplayName("Without force the delete stays what it was: no gate, no writes")
        void deleteWithoutGatesIsUnchanged() throws Exception {
            assertTrue(service.deleteFromStage("test-scope", "draft", "object-1").getValue());

            verify(storage).deleteObject("test-scope", "test-registry", "draft", "object-1");
            verify(storage, never()).updateDiagnostics(any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("A gate written against 1.1 passes deletes by default")
        void beforeDeleteDefaultsToPass() throws Exception {
            StageGate legacy = new StageGate() {
                @Override
                public boolean supportsObjectType(String objectType) {
                    return true;
                }

                @Override
                public Promise<GateVerdict> beforeTransition(GateContext ctx) {
                    return Promises.resolved(GateVerdict.refuse("would refuse a transition"));
                }
            };
            service.addStageGate(legacy);

            assertTrue(service.deleteFromStage("test-scope", "draft", "object-1").getValue());
            verify(storage).deleteObject("test-scope", "test-registry", "draft", "object-1");
        }

        @Test
        @DisplayName("The delete context names the trigger, the stage and no target")
        void deleteContextDescribesTheDelete() throws Exception {
            StageGate gate = gate();
            when(gate.beforeDelete(any())).thenReturn(Promises.resolved(GateVerdict.pass()));
            service.addStageGate(gate);

            service.deleteFromStage("test-scope", "draft", "object-1").getValue();

            ArgumentCaptor<GateContext> captor = ArgumentCaptor.forClass(GateContext.class);
            verify(gate).beforeDelete(captor.capture());
            GateContext ctx = captor.getValue();
            assertEquals(GateTrigger.DELETE, ctx.trigger());
            assertTrue(ctx.isDelete());
            assertEquals("draft", ctx.sourceStage());
            assertNull(ctx.targetStage());
            assertEquals("object-1", ctx.objectId());
        }

        @Test
        @DisplayName("A consequence for another registry goes through that registry")
        void consequenceOnAnotherRegistryGoesThroughTheCollector() throws Exception {
            @SuppressWarnings("unchecked")
            RegistryService<EObject> instances = mock(RegistryService.class);
            when(instances.updateDiagnostics(eq("test-scope"), eq("release"), eq("instance-9"), eq(PRODUCER), any()))
                    .thenReturn(Promises.resolved(ManagementFactory.eINSTANCE.createObjectMetadata()));
            RegistryServiceCollector collector = mock(RegistryServiceCollector.class);
            doReturn(instances).when(collector).getRegistryServiceByRegistryName("instances");
            service.bindRegistryCollector(collector);

            StageGate gate = gate();
            when(gate.beforeDelete(any())).thenReturn(Promises.resolved(GateVerdict.pass(List.of(
                    GateDiagnostic.warning("schema-gone", "gone").on(new Dependent("instances", "release", "instance-9"))))));
            service.addStageGate(gate);

            assertTrue(service.deleteFromStage("test-scope", "draft", "object-1").getValue());

            verify(instances).updateDiagnostics(eq("test-scope"), eq("release"), eq("instance-9"), eq(PRODUCER), any());
            verify(storage).deleteObject("test-scope", "test-registry", "draft", "object-1");
        }

        @Test
        @DisplayName("A consequence that cannot be recorded stops the operation before it commits")
        void unreachableDependentFailsClosed() {
            StageGate gate = gate();
            when(gate.beforeDelete(any())).thenReturn(Promises.resolved(GateVerdict.pass(List.of(
                    GateDiagnostic.warning("schema-gone", "gone").on(new Dependent("instances", "release", "instance-9"))))));
            service.addStageGate(gate);

            InvocationTargetException failed = assertThrows(InvocationTargetException.class,
                    () -> service.deleteFromStage("test-scope", "draft", "object-1", true).getValue());

            // no collector is bound: a fault, not a refusal, and nothing was deleted
            assertInstanceOf(IllegalStateException.class, failed.getCause());
            verify(storage, never()).deleteObject(any(), any(), any(), any());
        }
    }

    @Nested
    @DisplayName("Diagnostics are metadata: written in any stage, no action fired (issue #292)")
    class DiagnosticsTests {

        private EObjectStorageService<EObject> storage;
        private RegistryServiceImpl<EObject> service;
        private StageActionService stageAction;
        private ObjectMetadata stored;

        @SuppressWarnings("unchecked")
        @BeforeEach
        void setUp() {
            stored = ManagementFactory.eINSTANCE.createObjectMetadata();
            stored.setObjectId("object-1");
            stored.setStage("frozen");
            storage = mock(EObjectStorageService.class);
            when(storage.getStorageType()).thenReturn("file");
            org.mockito.Mockito.lenient()
                    .when(storage.updateDiagnostics(eq("test-scope"), eq("test-registry"), any(), eq("object-1"),
                            eq("checker"), any()))
                    .thenReturn(Promises.resolved(stored));
            // the read before the write, for the event's "before" state
            org.mockito.Mockito.lenient().when(storage.retrieveMetadata(any(), any(), any(), any()))
                    .thenReturn(Promises.resolved(null));

            // a registry with a stage that is neither writable nor final: nothing but a
            // diagnostics write may reach it
            RegistryServiceConfig config = mock(RegistryServiceConfig.class);
            when(config.registry_name()).thenReturn("test-registry");
            when(config.registry_description()).thenReturn("");
            when(config.registry_type()).thenReturn("OTHER");
            when(config.workflow_transitions()).thenReturn(new String[] { "draft:release" });
            when(config.stage_storage_mappings())
                    .thenReturn(new String[] { "draft:file", "release:file", "frozen:file" });
            when(config.stages()).thenReturn(new String[] {
                    "{\"name\": \"draft\", \"writable\": true, \"final\": false}",
                    "{\"name\": \"release\", \"writable\": true, \"final\": true}",
                    "{\"name\": \"frozen\", \"writable\": false, \"final\": false}" });
            when(config.root_eclass_uri()).thenReturn(new String[] { TEST_NS_URI + "#//Person" });
            org.mockito.Mockito.lenient().when(config.derived_eclass_uri()).thenReturn(new String[0]);
            service = new RegistryServiceImpl<>(List.of(storage), resourceSet, EcorePackage.eINSTANCE, config);

            stageAction = mock(StageActionService.class);
            org.mockito.Mockito.lenient().when(stageAction.requiresReplayOnStartup()).thenReturn(false);
            org.mockito.Mockito.lenient().when(stageAction.supportsObjectType(any())).thenReturn(true);
            org.mockito.Mockito.lenient().when(stageAction.getTriggerStages()).thenReturn(Set.of());
            org.mockito.Mockito.lenient().when(stageAction.getTriggerEvents()).thenReturn(Set.of());
            service.addStageActionService(stageAction);
        }

        @Test
        @DisplayName("A diagnostics write reaches a non-writable stage and fires no stage action")
        void writesIntoANonWritableStage() throws Exception {
            ObjectMetadata result = service
                    .updateDiagnostics("test-scope", "frozen", "object-1", "checker", List.of()).getValue();

            assertNotNull(result);
            assertTrue(result.isIsReadOnly(), "a non-writable stage is reported read-only, as on a read");
            verify(storage).updateDiagnostics("test-scope", "test-registry", "frozen", "object-1", "checker",
                    List.of());
            verify(stageAction, never()).onUpdate(any());
            verify(stageAction, never()).onEnter(any());
        }

        @Test
        @DisplayName("A diagnostics write reaches a final stage where a content update is refused")
        void writesIntoAFinalStage() throws Exception {
            ObjectMetadata result = service
                    .updateDiagnostics("test-scope", "release", "object-1", "checker", List.of()).getValue();

            assertNotNull(result);
            assertFalse(result.isIsReadOnly(), "release is writable, only its content is frozen");
            verify(storage).updateDiagnostics("test-scope", "test-registry", "release", "object-1", "checker",
                    List.of());
        }

        @Test
        @DisplayName("An unknown stage is still refused")
        void unknownStageIsRefused() {
            var promise = service.updateDiagnostics("test-scope", "nowhere", "object-1", "checker", List.of());
            java.lang.reflect.InvocationTargetException failure = org.junit.jupiter.api.Assertions
                    .assertThrows(java.lang.reflect.InvocationTargetException.class, promise::getValue);
            assertTrue(failure.getCause() instanceof IllegalArgumentException, String.valueOf(failure.getCause()));
            verify(storage, never()).updateDiagnostics(any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("A write that changes a finding delivers one typed event naming what changed (issue #293)")
        void changedDiagnosticsAreAnnounced() throws Exception {
            org.osgi.service.typedevent.TypedEventBus bus = mock(org.osgi.service.typedevent.TypedEventBus.class);
            setBus(service, bus);
            ObjectMetadata before = ManagementFactory.eINSTANCE.createObjectMetadata();
            before.setObjectId("object-1");
            org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic old = ManagementFactory.eINSTANCE
                    .createDiagnostic();
            old.setId("d1");
            old.setProducer("checker");
            old.setCode("c");
            old.setMessage("m");
            old.setSeverity(org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity.WARNING);
            before.getDiagnostics().add(old);
            org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic now = org.eclipse.emf.ecore.util.EcoreUtil
                    .copy(old);
            now.setSeverity(org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity.ERROR);
            stored.getDiagnostics().add(now);
            when(storage.retrieveMetadata("test-scope", "test-registry", "draft", "object-1"))
                    .thenReturn(Promises.resolved(before));

            service.updateDiagnostics("test-scope", "draft", "object-1", "checker", List.of()).getValue();

            ArgumentCaptor<org.eclipse.fennec.model.atlas.mgmt.diagnostics.DiagnosticsChanged> captor = ArgumentCaptor
                    .forClass(org.eclipse.fennec.model.atlas.mgmt.diagnostics.DiagnosticsChanged.class);
            verify(bus).deliver(eq(org.eclipse.fennec.model.atlas.mgmt.diagnostics.DiagnosticsChanged.TOPIC),
                    captor.capture());
            var event = captor.getValue();
            assertEquals("test-scope", event.scope);
            assertEquals("test-registry", event.registry);
            assertEquals("draft", event.stage);
            assertEquals("object-1", event.objectId);
            assertEquals("checker", event.producer);
            assertEquals(1, event.changes.size());
            assertEquals("d1", event.changes.get(0).id);
            assertEquals(org.eclipse.fennec.model.atlas.mgmt.diagnostics.DiagnosticDelta.CHANGED,
                    event.changes.get(0).kind);
            assertEquals("WARNING", event.changes.get(0).before.severity);
            assertEquals("ERROR", event.changes.get(0).after.severity);
        }

        @Test
        @DisplayName("A write that changes nothing delivers no event, so reacting modules cannot loop")
        void unchangedDiagnosticsStaySilent() throws Exception {
            org.osgi.service.typedevent.TypedEventBus bus = mock(org.osgi.service.typedevent.TypedEventBus.class);
            setBus(service, bus);
            when(storage.retrieveMetadata("test-scope", "test-registry", "draft", "object-1"))
                    .thenReturn(Promises.resolved(org.eclipse.emf.ecore.util.EcoreUtil.copy(stored)));

            service.updateDiagnostics("test-scope", "draft", "object-1", "checker", List.of()).getValue();

            verify(bus, never()).deliver(any(), any());
        }

        @Test
        @DisplayName("Without a bus the write stands and nobody is told")
        void noBusNoEvent() throws Exception {
            when(storage.retrieveMetadata("test-scope", "test-registry", "draft", "object-1"))
                    .thenReturn(Promises.resolved(null));

            assertNotNull(service.updateDiagnostics("test-scope", "draft", "object-1", "checker", List.of())
                    .getValue());
        }

        private static void setBus(RegistryServiceImpl<EObject> service, org.osgi.service.typedevent.TypedEventBus bus) {
            service.bindTypedEventBus(bus);
        }

        @Test
        @DisplayName("An object the storage does not know yields null, like updateProperties")
        void unknownObjectYieldsNull() throws Exception {
            when(storage.updateDiagnostics(any(), any(), any(), eq("missing"), any(), any()))
                    .thenReturn(Promises.resolved(null));

            assertNull(service.updateDiagnostics("test-scope", "draft", "missing", "checker", List.of()).getValue());
        }
    }

    @Nested
    @DisplayName("Registry rooted at a specific EClass")
    class SpecificRootTests {

        private RegistryServiceImpl<EObject> service;

        @BeforeEach
        void setUp() {
            service = createService(TEST_NS_URI + "#//Person");
        }

        @Test
        @DisplayName("Should accept the root EClass itself")
        void shouldAcceptRootEClass() {
            assertTrue(service.isEClassCompatibleWithRegistry(personClass));
        }

        @Test
        @DisplayName("Should accept a sub class of the root EClass")
        void shouldAcceptSubClass() {
            assertTrue(service.isEClassCompatibleWithRegistry(employeeClass));
        }

        @Test
        @DisplayName("Should reject an unrelated EClass")
        void shouldRejectUnrelatedEClass() {
            assertFalse(service.isEClassCompatibleWithRegistry(unrelatedClass));
        }

        @Test
        @DisplayName("Should reject the EObject EClass")
        void shouldRejectEObject() {
            assertFalse(service.isEClassCompatibleWithRegistry(EcorePackage.Literals.EOBJECT));
        }
    }

    @Nested
    @DisplayName("Registry with multiple unrelated root EClasses (issue #239)")
    class MultiRootTests {

        private RegistryServiceImpl<EObject> service;

        @BeforeEach
        void createMultiRootService() {
            service = createService(TEST_NS_URI + "#//Person", TEST_NS_URI + "#//Unrelated");
        }

        @Test
        @DisplayName("Should accept instances of every configured root")
        void shouldAcceptEveryRoot() {
            assertTrue(service.isEClassCompatibleWithRegistry(personClass));
            assertTrue(service.isEClassCompatibleWithRegistry(unrelatedClass));
        }

        @Test
        @DisplayName("Should accept subclasses of any configured root")
        void shouldAcceptSubclassOfAnyRoot() {
            assertTrue(service.isEClassCompatibleWithRegistry(employeeClass));
        }

        @Test
        @DisplayName("Should reject an EClass matching no configured root")
        void shouldRejectForeignEClass() {
            EClass foreign = EcoreFactory.eINSTANCE.createEClass();
            foreign.setName("Foreign");
            assertFalse(service.isEClassCompatibleWithRegistry(foreign));
        }

        @Test
        @DisplayName("getRootEClass answers the primary root, getRootEClasses all of them")
        void shouldExposeConfiguredRoots() {
            assertTrue(service.getRootEClass() == personClass);
            assertTrue(service.getRootEClasses().equals(List.of(personClass, unrelatedClass)));
        }

        @Test
        @DisplayName("Should fail activation when any configured root does not resolve")
        void shouldFailOnUnresolvableRoot() {
            org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                    () -> createService(TEST_NS_URI + "#//Person", TEST_NS_URI + "#//DoesNotExist"));
        }
    }

    @Nested
    @DisplayName("Derived EClasses (server-written content, issue #239 review)")
    class DerivedTypeTests {

        private EObjectStorageService<EObject> storage;
        private RegistryServiceImpl<EObject> service;

        @SuppressWarnings("unchecked")
        @BeforeEach
        void createDerivedService() {
            storage = mock(EObjectStorageService.class);
            when(storage.getStorageType()).thenReturn("file");
            service = createService(List.of(storage), new String[] { TEST_NS_URI + "#//Person" },
                    new String[] { TEST_NS_URI + "#//Unrelated" });
        }

        @Test
        @DisplayName("A derived EClass is recognized but is no root")
        void derivedIsRecognizedButNoRoot() {
            assertTrue(service.isDerivedEClass(unrelatedClass));
            assertFalse(service.isEClassCompatibleWithRegistry(unrelatedClass),
                    "derived types are not client-writable roots");
            assertTrue(service.getDerivedEClasses().equals(List.of(unrelatedClass)));
        }

        @Test
        @DisplayName("A derived update passes the final-stage bar; a root update does not")
        void derivedUpdatePassesFinalStageBar() throws Exception {
            ObjectMetadata stored = ManagementFactory.eINSTANCE.createObjectMetadata();
            stored.setObjectId("derived-1");
            when(storage.retrieveMetadata(any(), any(), any(), any())).thenReturn(Promises.resolved(stored));
            when(storage.updateObject(any(), any(), any())).thenReturn(Promises.resolved(stored));

            EObject derived = unrelatedClass.getEPackage().getEFactoryInstance().create(unrelatedClass);
            // release is the final stage of the test config — the derived refresh succeeds
            assertNotNull(service.updateInStage("test-scope", "release", derived, "derived-1", null).getValue());

            EObject root = personClass.getEPackage().getEFactoryInstance().create(personClass);
            var promise = service.updateInStage("test-scope", "release", root, "root-1", null);
            java.lang.reflect.InvocationTargetException failure = org.junit.jupiter.api.Assertions
                    .assertThrows(java.lang.reflect.InvocationTargetException.class, promise::getValue);
            assertTrue(failure.getCause() instanceof org.eclipse.fennec.model.atlas.scope.api.StagePolicyException,
                    "a root update in the final stage stays refused, got " + failure.getCause());
        }

        @Test
        @DisplayName("The service API accepts storing a derived object")
        void derivedUploadPassesTheTypeGate() throws Exception {
            when(storage.storeObject(any(), any(), any(), any(), any(), any()))
                    .thenAnswer(invocation -> Promises.resolved(invocation.getArgument(5)));
            EObject derived = unrelatedClass.getEPackage().getEFactoryInstance().create(unrelatedClass);
            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setObjectId("derived-2");
            assertNotNull(service.uploadToStage("test-scope", "draft", derived, metadata).getValue());
        }
    }

    @Nested
    @DisplayName("Registry-side type gate (defense in depth beside the REST check)")
    class TypeGateTests {

        @Test
        @DisplayName("uploadToStage refuses an object no configured root accepts, before storage is touched")
        void uploadRefusesIncompatibleObject() throws Exception {
            @SuppressWarnings("unchecked")
            EObjectStorageService<EObject> storage = mock(EObjectStorageService.class);
            when(storage.getStorageType()).thenReturn("file");
            RegistryServiceImpl<EObject> service = createService(List.of(storage), TEST_NS_URI + "#//Person");

            EObject unrelated = unrelatedClass.getEPackage().getEFactoryInstance().create(unrelatedClass);
            ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
            metadata.setObjectId("forged");

            var promise = service.uploadToStage("test-scope", "draft", unrelated, metadata);
            java.lang.reflect.InvocationTargetException failure = org.junit.jupiter.api.Assertions
                    .assertThrows(java.lang.reflect.InvocationTargetException.class, promise::getValue);
            assertTrue(failure.getCause() instanceof IllegalArgumentException,
                    "a type refusal is an IllegalArgumentException, got " + failure.getCause());
            verify(storage, never()).storeObject(any(), any(), any(), any(), any(), any());
        }

        @Test
        @DisplayName("updateInStage refuses an object no configured root accepts")
        void updateRefusesIncompatibleObject() throws Exception {
            @SuppressWarnings("unchecked")
            EObjectStorageService<EObject> storage = mock(EObjectStorageService.class);
            when(storage.getStorageType()).thenReturn("file");
            RegistryServiceImpl<EObject> service = createService(List.of(storage), TEST_NS_URI + "#//Person");

            EObject unrelated = unrelatedClass.getEPackage().getEFactoryInstance().create(unrelatedClass);
            var promise = service.updateInStage("test-scope", "draft", unrelated, "some-id", null);
            java.lang.reflect.InvocationTargetException failure = org.junit.jupiter.api.Assertions
                    .assertThrows(java.lang.reflect.InvocationTargetException.class, promise::getValue);
            assertTrue(failure.getCause() instanceof IllegalArgumentException,
                    "a type refusal is an IllegalArgumentException, got " + failure.getCause());
        }
    }
}
