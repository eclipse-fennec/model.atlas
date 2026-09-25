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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.eclipse.fennec.model.atlas.action.api.Dependent;
import org.eclipse.fennec.model.atlas.action.api.GateContext;
import org.eclipse.fennec.model.atlas.action.api.GateDiagnostic;
import org.eclipse.fennec.model.atlas.action.api.GateTrigger;
import org.eclipse.fennec.model.atlas.action.api.GateVerdict;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
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

/** The verdicts of the schema delete guard (issue #250). */
@DisplayName("SchemaDeleteGate")
class SchemaDeleteGateTest {

    private static final String SCOPE = "shop";
    private static final String PERSON_NS = "http://example.org/person/1.0";
    private static final String ORDER_NS = "http://example.org/order/1.0";

    private SchemaDeleteGate gate;
    private SchemaDependents dependents;
    private RegistryService<?> objects;

    @BeforeEach
    void setUp() throws Exception {
        gate = new SchemaDeleteGate();
        dependents = mock(SchemaDependents.class);
        objects = mock(RegistryService.class);
        RegistryServiceCollector registries = mock(RegistryServiceCollector.class);
        doReturn(objects).when(registries).getRegistryServiceByRegistryName("objects");
        inject("dependents", dependents);
        inject("registries", registries);
    }

    @Test
    @DisplayName("Only packages are asked about, and only their delete")
    void scope() throws Exception {
        assertTrue(gate.supportsObjectType(SchemaDependencies.EPACKAGE_TYPE));
        assertFalse(gate.supportsObjectType(PERSON_NS + "#//Person"));
        assertEquals(SchemaDependencies.PRODUCER, gate.producer());
        assertTrue(gate.beforeTransition(context(Map.of())).getValue().passed());
    }

    @Test
    @DisplayName("Nothing depends on the package: the delete passes")
    void passesWithoutDependents() throws Exception {
        when(dependents.dependentsOf(SCOPE, "release", PERSON_NS)).thenReturn(List.of());

        assertTrue(gate.beforeDelete(context(nsUri(PERSON_NS))).getValue().passed());
    }

    @Test
    @DisplayName("Dependents refuse the delete: one finding naming them, one consequence per dependent")
    void refusesWithDependents() throws Exception {
        SchemaDependent alice = new SchemaDependent(Kind.INSTANCE, "objects", "release", "alice", PERSON_NS + "#//Person");
        SchemaDependent order = new SchemaDependent(Kind.SCHEMA, "schema", "release", "order", SchemaDependencies.EPACKAGE_TYPE);
        when(dependents.dependentsOf(SCOPE, "release", PERSON_NS)).thenReturn(List.of(alice, order));
        when(objects.getMetadataFromStage(anyString(), anyString(), anyString())).thenReturn(null);

        GateVerdict verdict = gate.beforeDelete(context(nsUri(PERSON_NS))).getValue();

        assertTrue(verdict.refused());
        assertTrue(verdict.reason().contains("2 objects") && verdict.reason().contains(PERSON_NS), verdict.reason());
        assertEquals(1, verdict.findings().size());
        GateDiagnostic finding = verdict.findings().get(0);
        assertEquals(SchemaDependencies.CODE_HAS_DEPENDENTS, finding.code());
        assertEquals(PERSON_NS, finding.target());
        assertEquals(SchemaDependencies.CATEGORY, finding.category());
        assertEquals(List.of("objects/release/alice", "schema/release/order"),
                finding.children().stream().map(GateDiagnostic::target).toList());
        assertTrue(finding.children().stream().allMatch(c -> SchemaDependencies.CODE_DEPENDENT.equals(c.code())));

        Map<Dependent, List<GateDiagnostic>> consequences = verdict.consequences();
        assertEquals(2, consequences.size());
        for (SchemaDependent dependent : List.of(alice, order)) {
            List<GateDiagnostic> onIt = consequences.get(dependent.toDependent());
            assertEquals(1, onIt.size(), "one consequence on " + dependent);
            assertEquals(SchemaDependencies.CODE_DEPENDENCY_MISSING, onIt.get(0).code());
            assertEquals(PERSON_NS, onIt.get(0).target());
            assertTrue(onIt.get(0).isError());
        }
    }

    @Test
    @DisplayName("What the producer already recorded on a dependent about other packages is re-emitted")
    void keepsWhatIsRecordedAboutOtherPackages() throws Exception {
        SchemaDependent alice = new SchemaDependent(Kind.INSTANCE, "objects", "release", "alice", PERSON_NS + "#//Person");
        when(dependents.dependentsOf(SCOPE, "release", PERSON_NS)).thenReturn(List.of(alice));
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectId("alice");
        Diagnostic aboutOrder = SchemaDependencies.missing(ORDER_NS, "release");
        aboutOrder.setProducer(SchemaDependencies.PRODUCER);
        metadata.getDiagnostics().add(aboutOrder);
        Diagnostic someoneElses = ManagementFactory.eINSTANCE.createDiagnostic();
        someoneElses.setProducer("other");
        someoneElses.setCode("x");
        someoneElses.setMessage("not ours");
        metadata.getDiagnostics().add(someoneElses);
        when(objects.getMetadataFromStage(SCOPE, "release", "alice")).thenReturn(metadata);

        GateVerdict verdict = gate.beforeDelete(context(nsUri(PERSON_NS))).getValue();

        List<GateDiagnostic> onAlice = verdict.consequences().get(alice.toDependent());
        assertEquals(List.of(ORDER_NS, PERSON_NS), onAlice.stream().map(GateDiagnostic::target).toList(),
                "the earlier finding about order stays, the new one about person is added");
    }

    @Test
    @DisplayName("Without an nsUri in the context the schema's metadata is consulted; without any, nothing can depend on it")
    void nsUriFallback() throws Exception {
        RegistryService<?> schema = mock(RegistryService.class);
        RegistryServiceCollector registries = mock(RegistryServiceCollector.class);
        doReturn(schema).when(registries).getRegistryServiceByRegistryName("schema");
        inject("registries", registries);
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.getProperties().put(WorkflowConstants.NS_URI_METADATA_PROPERTY, PERSON_NS);
        when(schema.getMetadataFromStage(SCOPE, "release", "pkg")).thenReturn(metadata);
        when(dependents.dependentsOf(SCOPE, "release", PERSON_NS)).thenReturn(
                List.of(new SchemaDependent(Kind.INSTANCE, "objects", "release", "alice", PERSON_NS + "#//Person")));

        assertTrue(gate.beforeDelete(context(Map.of())).getValue().refused(), "found through the metadata");

        when(schema.getMetadataFromStage(SCOPE, "release", "pkg")).thenReturn(null);
        assertTrue(gate.beforeDelete(context(Map.of())).getValue().passed(), "no nsUri anywhere");
    }

    @Test
    @DisplayName("A failing dependents query fails the verdict; an undecided gate is not a pass")
    void failureIsNotAPass() throws Exception {
        when(dependents.dependentsOf(any(), any(), any())).thenThrow(new IllegalStateException("registry down"));

        assertTrue(gate.beforeDelete(context(nsUri(PERSON_NS))).getFailure() instanceof IllegalStateException);
    }

    private static Map<String, Object> nsUri(String nsURI) {
        return Map.of(WorkflowConstants.NS_URI_METADATA_PROPERTY, nsURI);
    }

    private static GateContext context(Map<String, Object> metadata) {
        return new GateContext(GateTrigger.DELETE, SCOPE, "schema", "pkg", SchemaDependencies.EPACKAGE_TYPE, "release",
                null, "tester", Instant.now(), metadata);
    }

    private void inject(String field, Object value) throws Exception {
        Field f = SchemaDeleteGate.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(gate, value);
    }
}
