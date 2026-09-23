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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.model.atlas.mgmt.diagnostics.DiagnosticAddress;
import org.eclipse.fennec.model.atlas.mgmt.diagnostics.DiagnosticDelta;
import org.eclipse.fennec.model.atlas.mgmt.diagnostics.DiagnosticNotFoundException;
import org.eclipse.fennec.model.atlas.mgmt.diagnostics.DiagnosticService;
import org.eclipse.fennec.model.atlas.mgmt.diagnostics.DiagnosticVersionConflictException;
import org.eclipse.fennec.model.atlas.mgmt.diagnostics.DiagnosticsChanged;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticStatus;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations.SchemaRegistryServiceSetup;
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
import org.osgi.service.typedevent.TypedEventConstants;
import org.osgi.service.typedevent.TypedEventHandler;
import org.osgi.test.common.annotation.InjectBundleContext;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * OSGi integration test for the {@link DiagnosticService} and the
 * {@link DiagnosticsChanged} events (issue #293): a person resolves a producer's finding
 * by id, the typed event carries the state before and after, the history records it, a
 * stale version is refused, and a later re-validation by the producer does not revert the
 * resolution.
 */
@RequireEMF
@RequireConfigurationAdmin
@ExtendWith(LuceneAwareTempDirExtension.class)
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@DisplayName("Diagnostic service OSGi Integration Tests")
@SuppressWarnings({ "unchecked", "rawtypes" })
public class DiagnosticServiceIntegrationTest {

    private static final String SCOPE = "diagnostic-service-scope";
    private static final String REGISTRY_FILTER = "(registry.name=" + CommonTestAnnotations.SCHEMA_REGISTRY_NAME + ")";
    private static final String OBJECT_ID = "checked-package";
    private static final String NS_URI = "http://test/diagnostic-service/a";
    private static final String PRODUCER = "reference-check";

    private final BlockingQueue<DiagnosticsChanged> events = new LinkedBlockingQueue<>();
    private ServiceRegistration<?> handler;

    @AfterEach
    void unregister() {
        if (handler != null) {
            handler.unregister();
        }
    }

    @Test
    @DisplayName("Resolve by id: event with before and after, history entry, stale version refused, re-validation keeps it")
    @SchemaRegistryServiceSetup
    void resolveByIdWithEventsHistoryAndConflict(@InjectBundleContext BundleContext bundleContext,
            @InjectService(cardinality = 0, filter = REGISTRY_FILTER) ServiceAware<RegistryService> registryAware,
            @InjectService(cardinality = 0) ServiceAware<DiagnosticService> serviceAware) throws Exception {
        RegistryService<EPackage> registryService = registryAware.waitForService(5000);
        DiagnosticService service = serviceAware.waitForService(5000);
        assertNotNull(registryService);
        assertNotNull(service, "the DiagnosticService component is part of the workflow bundle");
        listen(bundleContext);

        upload(registryService);
        DiagnosticAddress at = new DiagnosticAddress(SCOPE, CommonTestAnnotations.SCHEMA_REGISTRY_NAME,
                CommonTestAnnotations.STAGE_DRAFT, OBJECT_ID);

        // the producer records a finding
        Diagnostic finding = diagnostic("unresolved-reference", "//Person/address", DiagnosticSeverity.WARNING,
                "http://b is not visible");
        ObjectMetadata written = registryService.updateDiagnostics(SCOPE, CommonTestAnnotations.STAGE_DRAFT,
                OBJECT_ID, PRODUCER, List.of(finding)).getValue();
        String id = written.getDiagnostics().get(0).getId();
        DiagnosticsChanged added = events.poll(10, TimeUnit.SECONDS);
        assertNotNull(added, "the producer's write is announced");
        assertEquals(SCOPE, added.scope);
        assertEquals(CommonTestAnnotations.SCHEMA_REGISTRY_NAME, added.registry);
        assertEquals(CommonTestAnnotations.STAGE_DRAFT, added.stage);
        assertEquals(OBJECT_ID, added.objectId);
        assertEquals(PRODUCER, added.producer);
        assertEquals(1, added.changes.size());
        assertEquals(DiagnosticDelta.ADDED, added.changes.get(0).kind);
        assertNull(added.changes.get(0).before);
        assertEquals("WARNING", added.changes.get(0).after.severity);
        assertEquals("OPEN", added.changes.get(0).after.status);

        // a person resolves it by id
        Diagnostic resolved = service.resolve(at, id, 0, "gdpr.officer", "address is pseudonymised downstream")
                .getValue();
        assertEquals(DiagnosticStatus.RESOLVED, resolved.getStatus());
        assertEquals(1, resolved.getVersion(), "an informed change bumps the version");
        assertEquals(1, resolved.getHistory().size(), "and records who and why");
        assertEquals("gdpr.officer", resolved.getHistory().get(0).getChangedBy());
        assertEquals(DiagnosticStatus.OPEN, resolved.getHistory().get(0).getOldStatus());
        assertEquals(DiagnosticStatus.RESOLVED, resolved.getHistory().get(0).getNewStatus());
        assertEquals("address is pseudonymised downstream", resolved.getHistory().get(0).getReason());
        DiagnosticsChanged changed = events.poll(10, TimeUnit.SECONDS);
        assertNotNull(changed, "the resolution is announced");
        assertEquals(1, changed.changes.size());
        DiagnosticDelta delta = changed.changes.get(0);
        assertEquals(DiagnosticDelta.CHANGED, delta.kind);
        assertEquals(id, delta.id);
        assertEquals("OPEN", delta.before.status);
        assertEquals("RESOLVED", delta.after.status);
        assertEquals(0, delta.before.version);
        assertEquals(1, delta.after.version);
        assertEquals("gdpr.officer", delta.after.changedBy, "the event names who decided");

        // the fresh read agrees
        ObjectMetadata reread = registryService.getMetadataFromStage(SCOPE, CommonTestAnnotations.STAGE_DRAFT,
                OBJECT_ID);
        assertEquals(DiagnosticStatus.RESOLVED, reread.getDiagnostics().get(0).getStatus());

        // a second decision about the version that no longer is: refused, nothing written
        InvocationTargetException refused = assertThrows(InvocationTargetException.class,
                () -> service.acknowledge(at, id, 0, "somebody-else", "late").getValue());
        assertTrue(refused.getCause() instanceof DiagnosticVersionConflictException,
                String.valueOf(refused.getCause()));
        DiagnosticVersionConflictException conflict = (DiagnosticVersionConflictException) refused.getCause();
        assertEquals(0, conflict.getExpectedVersion());
        assertEquals(1, conflict.getCurrentVersion());
        assertNull(events.poll(2, TimeUnit.SECONDS), "a refused change announces nothing");

        // the producer runs again, finds the same thing, knows nothing: the resolution stands
        registryService.updateDiagnostics(SCOPE, CommonTestAnnotations.STAGE_DRAFT, OBJECT_ID, PRODUCER,
                List.of(diagnostic("unresolved-reference", "//Person/address", DiagnosticSeverity.WARNING,
                        "http://b is still not visible")))
                .getValue();
        reread = registryService.getMetadataFromStage(SCOPE, CommonTestAnnotations.STAGE_DRAFT, OBJECT_ID);
        Diagnostic again = reread.getDiagnostics().get(0);
        assertEquals(id, again.getId(), "the same finding keeps its id");
        assertEquals(DiagnosticStatus.RESOLVED, again.getStatus(), "the manual resolution is not reverted");
        assertEquals("http://b is still not visible", again.getMessage(), "the producer's text refreshes");
        assertEquals(1, again.getVersion());
        DiagnosticsChanged revalidated = events.poll(10, TimeUnit.SECONDS);
        assertNotNull(revalidated, "the changed message is announced");
        assertEquals("http://b is not visible", revalidated.changes.get(0).before.message);
        assertEquals("http://b is still not visible", revalidated.changes.get(0).after.message);

        // an identical re-validation announces nothing
        registryService.updateDiagnostics(SCOPE, CommonTestAnnotations.STAGE_DRAFT, OBJECT_ID, PRODUCER,
                List.of(diagnostic("unresolved-reference", "//Person/address", DiagnosticSeverity.WARNING,
                        "http://b is still not visible")))
                .getValue();
        assertNull(events.poll(2, TimeUnit.SECONDS), "nothing changed, nothing announced");
    }

    @Test
    @DisplayName("Escalate raises, add adds next to the producer's findings, remove removes; unknown ids are refused")
    @SchemaRegistryServiceSetup
    void escalateAddRemove(@InjectBundleContext BundleContext bundleContext,
            @InjectService(cardinality = 0, filter = REGISTRY_FILTER) ServiceAware<RegistryService> registryAware,
            @InjectService(cardinality = 0) ServiceAware<DiagnosticService> serviceAware) throws Exception {
        RegistryService<EPackage> registryService = registryAware.waitForService(5000);
        DiagnosticService service = serviceAware.waitForService(5000);
        listen(bundleContext);
        upload(registryService);
        DiagnosticAddress at = new DiagnosticAddress(SCOPE, CommonTestAnnotations.SCHEMA_REGISTRY_NAME,
                CommonTestAnnotations.STAGE_DRAFT, OBJECT_ID);
        String id = registryService.updateDiagnostics(SCOPE, CommonTestAnnotations.STAGE_DRAFT, OBJECT_ID, PRODUCER,
                List.of(diagnostic("deprecated-import", null, DiagnosticSeverity.INFO, "old lib"))).getValue()
                .getDiagnostics().get(0).getId();
        assertNotNull(events.poll(10, TimeUnit.SECONDS));

        Diagnostic escalated = service.escalate(at, id, 0, DiagnosticSeverity.ERROR, "operator", "lib removed")
                .getValue();
        assertEquals(DiagnosticSeverity.ERROR, escalated.getSeverity());
        assertEquals(1, escalated.getVersion());
        assertEquals(DiagnosticSeverity.INFO, escalated.getHistory().get(0).getOldSeverity());
        DiagnosticsChanged event = events.poll(10, TimeUnit.SECONDS);
        assertEquals("INFO", event.changes.get(0).before.severity);
        assertEquals("ERROR", event.changes.get(0).after.severity);

        InvocationTargetException notRaising = assertThrows(InvocationTargetException.class,
                () -> service.escalate(at, id, 1, DiagnosticSeverity.WARNING, "operator", "oops").getValue());
        assertTrue(notRaising.getCause() instanceof IllegalArgumentException, String.valueOf(notRaising.getCause()));

        Diagnostic manual = service.add(at, "gdpr.officer",
                diagnostic("personal-data", "//Person/birthDate", DiagnosticSeverity.WARNING, "pii"), "gdpr.officer",
                "seen in review").getValue();
        assertNotNull(manual.getId());
        assertEquals("gdpr.officer", manual.getProducer());
        assertEquals(1, manual.getVersion());
        assertEquals("seen in review", manual.getHistory().get(0).getReason());
        DiagnosticsChanged addedEvent = events.poll(10, TimeUnit.SECONDS);
        assertEquals("gdpr.officer", addedEvent.producer);
        assertEquals(DiagnosticDelta.ADDED, addedEvent.changes.get(0).kind);
        ObjectMetadata both = registryService.getMetadataFromStage(SCOPE, CommonTestAnnotations.STAGE_DRAFT,
                OBJECT_ID);
        assertEquals(2, both.getDiagnostics().size(), "two producers, two roots");

        assertTrue(service.remove(at, id, 1, "operator", "recorded by mistake").getValue());
        DiagnosticsChanged removedEvent = events.poll(10, TimeUnit.SECONDS);
        assertEquals(DiagnosticDelta.REMOVED, removedEvent.changes.get(0).kind);
        assertNull(removedEvent.changes.get(0).after);
        ObjectMetadata one = registryService.getMetadataFromStage(SCOPE, CommonTestAnnotations.STAGE_DRAFT,
                OBJECT_ID);
        assertEquals(1, one.getDiagnostics().size());
        assertEquals("gdpr.officer", one.getDiagnostics().get(0).getProducer(), "the other producer's root stays");

        InvocationTargetException unknown = assertThrows(InvocationTargetException.class,
                () -> service.resolve(at, "no-such-id", 0, "operator", null).getValue());
        assertTrue(unknown.getCause() instanceof DiagnosticNotFoundException, String.valueOf(unknown.getCause()));
        InvocationTargetException noObject = assertThrows(InvocationTargetException.class,
                () -> service.resolve(new DiagnosticAddress(SCOPE, CommonTestAnnotations.SCHEMA_REGISTRY_NAME,
                        CommonTestAnnotations.STAGE_DRAFT, "nobody"), id, 0, "operator", null).getValue());
        assertTrue(noObject.getCause() instanceof DiagnosticNotFoundException, String.valueOf(noObject.getCause()));
    }

    private void listen(BundleContext bundleContext) {
        Dictionary<String, Object> props = new Hashtable<>();
        props.put(TypedEventConstants.TYPED_EVENT_TOPICS, DiagnosticsChanged.TOPIC);
        props.put(TypedEventConstants.TYPED_EVENT_TYPE, DiagnosticsChanged.class.getName());
        TypedEventHandler<DiagnosticsChanged> listener = (topic, event) -> events.add(event);
        handler = bundleContext.registerService(TypedEventHandler.class, listener, props);
    }

    private void upload(RegistryService<EPackage> registryService)
            throws InvocationTargetException, InterruptedException {
        EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
        pkg.setName("checked");
        pkg.setNsURI(NS_URI);
        pkg.setNsPrefix("checked");
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectId(OBJECT_ID);
        metadata.setObjectName("checked");
        metadata.setVersion("1");
        metadata.getProperties().put(WorkflowConstants.NS_URI_METADATA_PROPERTY, NS_URI);
        registryService.uploadToStage(SCOPE, CommonTestAnnotations.STAGE_DRAFT, pkg, metadata).getValue();
    }

    private static Diagnostic diagnostic(String code, String target, DiagnosticSeverity severity, String message) {
        Diagnostic diagnostic = ManagementFactory.eINSTANCE.createDiagnostic();
        diagnostic.setCode(code);
        diagnostic.setTarget(target);
        diagnostic.setSeverity(severity);
        diagnostic.setMessage(message);
        return diagnostic;
    }
}
