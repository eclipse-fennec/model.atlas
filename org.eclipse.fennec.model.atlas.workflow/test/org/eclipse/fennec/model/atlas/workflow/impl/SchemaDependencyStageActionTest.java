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
package org.eclipse.fennec.model.atlas.workflow.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.fennec.model.atlas.action.api.ActionContext;
import org.eclipse.fennec.model.atlas.action.api.StageActionService.ActionEvent;
import org.eclipse.fennec.model.atlas.action.api.StageActionService.ExitReason;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.workflow.RegistryServiceCollector;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
import org.eclipse.fennec.model.atlas.workflow.dependency.SchemaDependent;
import org.eclipse.fennec.model.atlas.workflow.dependency.SchemaDependent.Kind;
import org.eclipse.fennec.model.atlas.workflow.dependency.SchemaDependents;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.osgi.util.promise.Promises;

/** The stage action that keeps the dependents' unresolved state current (issue #250). */
@DisplayName("SchemaDependencyStageAction")
class SchemaDependencyStageActionTest {

    private static final String SCOPE = "shop";
    private static final String PERSON_NS = "http://example.org/person/1.0";
    private static final String ORDER_NS = "http://example.org/order/1.0";
    private static final SchemaDependent ALICE = new SchemaDependent(Kind.INSTANCE, "objects", "release", "alice",
            PERSON_NS + "#//Person");

    private SchemaDependencyStageAction action;
    private SchemaDependents dependents;
    private RegistryService<?> objects;
    private ObjectMetadata alice;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() throws Exception {
        action = new SchemaDependencyStageAction();
        dependents = mock(SchemaDependents.class);
        objects = mock(RegistryService.class);
        RegistryServiceCollector registries = mock(RegistryServiceCollector.class);
        doReturn(objects).when(registries).getRegistryServiceByRegistryName("objects");
        inject("dependents", dependents);
        inject("registries", registries);

        alice = ManagementFactory.eINSTANCE.createObjectMetadata();
        alice.setObjectId("alice");
        when(objects.getMetadataFromStage(SCOPE, "release", "alice")).thenReturn(alice);
        doReturn(Promises.resolved(alice)).when(objects).updateDiagnostics(anyString(), anyString(), anyString(),
                anyString(), anyList());
        when(dependents.dependentsOf(SCOPE, "release", PERSON_NS)).thenReturn(List.of(ALICE));
    }

    @Test
    @DisplayName("Triggers: packages, every stage, ENTER and EXIT, replayed on startup only")
    void triggers() {
        assertTrue(action.supportsObjectType(SchemaDependencies.EPACKAGE_TYPE));
        assertTrue(action.getTriggerStages().isEmpty(), "every stage");
        assertEquals(java.util.Set.of(ActionEvent.ENTER, ActionEvent.EXIT), action.getTriggerEvents());
        assertTrue(action.requiresReplayOnStartup());
        assertTrue(!action.requiresReplayOnShutdown());
    }

    @Test
    @DisplayName("EXIT marks every dependent unresolved, keeping what is recorded about other packages")
    void exitMarks() throws Exception {
        Diagnostic aboutOrder = owned(SchemaDependencies.missing(ORDER_NS, "release"));
        alice.getDiagnostics().add(aboutOrder);
        alice.getDiagnostics().add(foreign());

        action.onExit(context(ExitReason.DELETED, false)).getValue();

        List<Diagnostic> written = written();
        assertEquals(List.of(ORDER_NS, PERSON_NS), written.stream().map(Diagnostic::getTarget).toList());
        Diagnostic missing = written.get(1);
        assertEquals(SchemaDependencies.CODE_DEPENDENCY_MISSING, missing.getCode());
        assertEquals(DiagnosticSeverity.ERROR, missing.getSeverity());
        assertEquals(SchemaDependencies.CATEGORY, missing.getCategory());
        assertTrue(missing.getMessage().contains(PERSON_NS) && missing.getMessage().contains("release"),
                missing.getMessage());
        assertNull(missing.getProducer(), "the registry stamps the producer");
    }

    @Test
    @DisplayName("ENTER heals: only the finding about the arriving package goes; untouched dependents are not written")
    void enterHeals() throws Exception {
        alice.getDiagnostics().add(owned(SchemaDependencies.missing(ORDER_NS, "release")));
        alice.getDiagnostics().add(owned(SchemaDependencies.missing(PERSON_NS, "release")));

        action.onEnter(context(null, true)).getValue();

        assertEquals(List.of(ORDER_NS), written().stream().map(Diagnostic::getTarget).toList());

        // nothing about person on it any more: no write
        alice.getDiagnostics().clear();
        action.onEnter(context(null, false)).getValue();
        verify(objects).updateDiagnostics(anyString(), anyString(), anyString(), anyString(), anyList());
    }

    @Test
    @DisplayName("A shutdown replay changes nothing; a package without nsUri concerns nobody")
    void replayAndNoNsUri() throws Exception {
        action.onExit(context(ExitReason.DELETED, true)).getValue();
        action.onExit(new ActionContext(SCOPE, "schema", "pkg", SchemaDependencies.EPACKAGE_TYPE, "release", null,
                null, ExitReason.DELETED, "tester", Instant.now(), null, false, Map.of())).getValue();

        verify(dependents, never()).dependentsOf(any(), any(), any());
        verify(objects, never()).updateDiagnostics(anyString(), anyString(), anyString(), anyString(), anyList());
    }

    @Test
    @DisplayName("A dependent whose registry or object is gone is skipped, not fatal")
    void missingDependentIsSkipped() throws Exception {
        when(dependents.dependentsOf(SCOPE, "release", PERSON_NS)).thenReturn(List.of(ALICE,
                new SchemaDependent(Kind.INSTANCE, "elsewhere", "release", "bob", PERSON_NS + "#//Person"),
                new SchemaDependent(Kind.INSTANCE, "objects", "release", "gone", PERSON_NS + "#//Person")));

        action.onExit(context(ExitReason.TRANSITIONED, false)).getValue();

        verify(objects).updateDiagnostics(eq(SCOPE), eq("release"), eq("alice"), eq(SchemaDependencies.PRODUCER),
                anyList());
        verify(objects, never()).updateDiagnostics(eq(SCOPE), eq("release"), eq("gone"), anyString(), anyList());
    }

    private List<Diagnostic> written() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<Diagnostic>> captor = ArgumentCaptor.forClass(List.class);
        verify(objects).updateDiagnostics(eq(SCOPE), eq("release"), eq("alice"), eq(SchemaDependencies.PRODUCER),
                captor.capture());
        return captor.getValue();
    }

    private static Diagnostic owned(Diagnostic diagnostic) {
        diagnostic.setProducer(SchemaDependencies.PRODUCER);
        return diagnostic;
    }

    private static Diagnostic foreign() {
        Diagnostic diagnostic = ManagementFactory.eINSTANCE.createDiagnostic();
        diagnostic.setProducer("other");
        diagnostic.setCode("x");
        diagnostic.setMessage("not ours");
        return diagnostic;
    }

    private static ActionContext context(ExitReason exitReason, boolean replay) {
        return new ActionContext(SCOPE, "schema", "pkg", SchemaDependencies.EPACKAGE_TYPE, "release", null, null,
                exitReason, "tester", Instant.now(), null, replay,
                Map.of(WorkflowConstants.NS_URI_METADATA_PROPERTY, PERSON_NS));
    }

    private void inject(String field, Object value) throws Exception {
        Field f = SchemaDependencyStageAction.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(action, value);
    }
}
