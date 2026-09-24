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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.scope.api.RegistryInfo;
import org.eclipse.fennec.model.atlas.scope.api.RegistryType;
import org.eclipse.fennec.model.atlas.scope.api.ScopeApiFactory;
import org.eclipse.fennec.model.atlas.scope.api.StageInfo;
import org.eclipse.fennec.model.atlas.wf.workflowapi.RegistryService;
import org.eclipse.fennec.model.atlas.wf.workflowapi.Scope;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WorkflowApiFactory;
import org.eclipse.fennec.model.atlas.workflow.RegistryServiceCollector;
import org.eclipse.fennec.model.atlas.workflow.ScopeServiceCollector;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
import org.eclipse.fennec.model.atlas.workflow.dependency.SchemaDependent;
import org.eclipse.fennec.model.atlas.workflow.dependency.SchemaDependent.Kind;
import org.eclipse.fennec.model.atlas.workflow.dependency.SchemaDependentsContributor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The dependents query of issue #250 over the three edge types and the view rules.
 */
@DisplayName("SchemaDependents - who depends on a package as served from a stage")
class SchemaDependentsImplTest {

    private static final String SCOPE = "shop";
    private static final String SCHEMA = "schema";
    private static final String OBJECTS = "objects";
    private static final String PERSON_NS = "http://example.org/person/1.0";
    private static final String ORDER_NS = "http://example.org/order/1.0";
    private static final String OTHER_NS = "http://example.org/other/1.0";

    private SchemaDependentsImpl dependents;
    private RegistryService<?> schemaService;
    private RegistryService<?> objectsService;
    private RegistryInfo schemaRegistry;
    private RegistryInfo objectsRegistry;

    @BeforeEach
    void setUp() throws Exception {
        dependents = new SchemaDependentsImpl();

        schemaRegistry = registry(SCHEMA, RegistryType.SCHEMA, "draft", "approved", "release");
        objectsRegistry = registry(OBJECTS, RegistryType.OTHER, "draft", "release", "sandbox");
        Scope scope = WorkflowApiFactory.eINSTANCE.createScope();
        scope.setName(SCOPE);
        scope.getRegistries().add(schemaRegistry);
        scope.getRegistries().add(objectsRegistry);

        ScopeServiceCollector scopes = mock(ScopeServiceCollector.class);
        when(scopes.getScopeByName(SCOPE)).thenReturn(scope);
        inject("scopes", scopes);

        schemaService = mock(RegistryService.class);
        objectsService = mock(RegistryService.class);
        RegistryServiceCollector registries = mock(RegistryServiceCollector.class);
        doReturn(schemaService).when(registries).getRegistryServiceByRegistryName(SCHEMA);
        doReturn(objectsService).when(registries).getRegistryServiceByRegistryName(OBJECTS);
        inject("registries", registries);

        doReturn(List.of()).when(schemaService).listInStage(anyString(), anyString());
        doReturn(List.of()).when(objectsService).listInStage(anyString(), anyString());
    }

    @Test
    @DisplayName("A schema whose types point into the package is a dependent in the same stage")
    void schemaEdge() {
        EPackage person = person();
        EPackage order = orderReferencing(person);
        bind(person, "release");
        bind(order, "release");
        doReturn(List.of(schema("person", PERSON_NS), schema("order", ORDER_NS))).when(schemaService)
                .listInStage(SCOPE, "release");

        List<SchemaDependent> found = dependents.dependentsOf(SCOPE, "release", PERSON_NS);

        assertEquals(List.of(new SchemaDependent(Kind.SCHEMA, SCHEMA, "release", "order",
                SchemaDependencies.EPACKAGE_TYPE)), found);
        assertTrue(dependents.dependentsOf(SCOPE, "release", ORDER_NS).isEmpty(), "person references nothing");
    }

    @Test
    @DisplayName("An instance whose type is a class of the package is a dependent; other types are not")
    void instanceEdge() {
        bind(person(), "release");
        doReturn(List.of(instance("alice", PERSON_NS + "#//Person"), instance("thing", OTHER_NS + "#//Thing")))
                .when(objectsService).listInStage(SCOPE, "release");

        List<SchemaDependent> found = dependents.dependentsOf(SCOPE, "release", PERSON_NS);

        assertEquals(1, found.size(), found.toString());
        assertEquals(new SchemaDependent(Kind.INSTANCE, OBJECTS, "release", "alice", PERSON_NS + "#//Person"),
                found.get(0));
    }

    @Test
    @DisplayName("Earlier stages resolve through this one unless a stage in between holds the package itself")
    void viewsOfEarlierStages() {
        bind(person(), "release");
        doReturn(List.of(instance("draft-alice", PERSON_NS + "#//Person"))).when(objectsService)
                .listInStage(SCOPE, "draft");
        doReturn(List.of(instance("release-alice", PERSON_NS + "#//Person"))).when(objectsService)
                .listInStage(SCOPE, "release");

        // draft has no own copy: its instances see release's package
        assertEquals(List.of("release-alice", "draft-alice"),
                dependents.dependentsOf(SCOPE, "release", PERSON_NS).stream().map(SchemaDependent::objectId).toList());

        // draft holds its own copy now: its instances resolve there and release's delete does not reach them
        bind(person(), "draft");
        assertEquals(List.of("release-alice"),
                dependents.dependentsOf(SCOPE, "release", PERSON_NS).stream().map(SchemaDependent::objectId).toList());
        // ... and deleting draft's copy concerns draft only
        assertEquals(List.of("draft-alice"),
                dependents.dependentsOf(SCOPE, "draft", PERSON_NS).stream().map(SchemaDependent::objectId).toList());
    }

    @Test
    @DisplayName("Stages only other registries have are served from the final stage")
    void instanceOnlyStagesFollowTheFinalStage() {
        bind(person(), "release");
        doReturn(List.of(instance("sandbox-alice", PERSON_NS + "#//Person"))).when(objectsService)
                .listInStage(SCOPE, "sandbox");

        assertEquals(List.of("sandbox-alice"),
                dependents.dependentsOf(SCOPE, "release", PERSON_NS).stream().map(SchemaDependent::objectId).toList());
        assertTrue(dependents.dependentsOf(SCOPE, "approved", PERSON_NS).isEmpty(),
                "approved is not final, so the sandbox does not resolve through it");
        verify(objectsService, never()).listInStage(SCOPE, "approved");
    }

    @Test
    @DisplayName("Contributors are asked per registry and stage of the view; their answers are deduplicated")
    void contributorsAreAskedPerView() {
        bind(person(), "release");
        SchemaDependentsContributor contributor = mock(SchemaDependentsContributor.class);
        SchemaDependent unit = new SchemaDependent(Kind.UNIT, OBJECTS, "release", "unit-1", "x#//CompiledUnit");
        when(contributor.dependentsIn(eq(SCOPE), any(), anyString(), eq(PERSON_NS))).thenReturn(List.of());
        when(contributor.dependentsIn(SCOPE, objectsService, "release", PERSON_NS)).thenReturn(List.of(unit, unit));
        dependents.bindContributor(contributor);

        List<SchemaDependent> found = dependents.dependentsOf(SCOPE, "release", PERSON_NS);

        assertEquals(List.of(unit), found);
        verify(contributor).dependentsIn(SCOPE, objectsService, "release", PERSON_NS);
        verify(contributor).dependentsIn(SCOPE, objectsService, "draft", PERSON_NS);
        verify(contributor).dependentsIn(SCOPE, objectsService, "sandbox", PERSON_NS);
        verify(contributor, never()).dependentsIn(eq(SCOPE), eq(schemaService), anyString(), anyString());

        dependents.unbindContributor(contributor);
        assertTrue(dependents.dependentsOf(SCOPE, "release", PERSON_NS).isEmpty());
    }

    @Test
    @DisplayName("An unknown scope has no registries to look in; a package's own nsURI is not a dependency")
    void edges() {
        assertTrue(dependents.dependentsOf("nowhere", "release", PERSON_NS).isEmpty());
        assertEquals(PERSON_NS, SchemaDependentsImpl.packageOf(PERSON_NS + "#//Person"));
        assertNull(SchemaDependentsImpl.packageOf(null));
        assertNull(SchemaDependentsImpl.packageOf("no-fragment"));
        EPackage order = orderReferencing(person());
        assertEquals(java.util.Set.of(PERSON_NS, EcorePackage.eNS_URI),
                SchemaDependentsImpl.collectReferencedNsURIs(order));
    }

    // --- fixtures ---

    private static RegistryInfo registry(String name, RegistryType type, String... stages) {
        RegistryInfo registry = ScopeApiFactory.eINSTANCE.createRegistryInfo();
        registry.setName(name);
        registry.setType(type);
        for (int i = 0; i < stages.length; i++) {
            StageInfo stage = ScopeApiFactory.eINSTANCE.createStageInfo();
            stage.setName(stages[i]);
            stage.setWritable(true);
            stage.setFinal(type == RegistryType.SCHEMA ? i == stages.length - 1 : "release".equals(stages[i]));
            registry.getStages().add(stage);
        }
        return registry;
    }

    private static EPackage person() {
        EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
        ePackage.setName("person");
        ePackage.setNsPrefix("person");
        ePackage.setNsURI(PERSON_NS);
        EClass person = EcoreFactory.eINSTANCE.createEClass();
        person.setName("Person");
        ePackage.getEClassifiers().add(person);
        return ePackage;
    }

    private static EPackage orderReferencing(EPackage person) {
        EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
        ePackage.setName("order");
        ePackage.setNsPrefix("order");
        ePackage.setNsURI(ORDER_NS);
        EClass order = EcoreFactory.eINSTANCE.createEClass();
        order.setName("Order");
        EReference customer = EcoreFactory.eINSTANCE.createEReference();
        customer.setName("customer");
        customer.setEType(person.getEClassifier("Person"));
        order.getEStructuralFeatures().add(customer);
        org.eclipse.emf.ecore.EAttribute note = EcoreFactory.eINSTANCE.createEAttribute();
        note.setName("note");
        note.setEType(EcorePackage.Literals.ESTRING);
        order.getEStructuralFeatures().add(note);
        ePackage.getEClassifiers().add(order);
        return ePackage;
    }

    private void bind(EPackage ePackage, String stage) {
        Map<String, Object> properties = new HashMap<>();
        properties.put(EMFNamespaces.EMF_MODEL_SCOPE, SCOPE);
        properties.put(WorkflowConstants.ATLAS_EPACKAGE_REGISTRATION_STAGE_PROPERTY, stage);
        properties.put(EMFNamespaces.EMF_MODEL_NSURI, ePackage.getNsURI());
        dependents.bindEPackage(ePackage, properties);
    }

    private static ObjectMetadata schema(String objectId, String nsURI) {
        ObjectMetadata metadata = instance(objectId, SchemaDependencies.EPACKAGE_TYPE);
        metadata.getProperties().put(WorkflowConstants.NS_URI_METADATA_PROPERTY, nsURI);
        return metadata;
    }

    private static ObjectMetadata instance(String objectId, String objectType) {
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectId(objectId);
        metadata.setObjectType(objectType);
        metadata.setScope(SCOPE);
        return metadata;
    }

    private void inject(String field, Object value) throws Exception {
        Field f = SchemaDependentsImpl.class.getDeclaredField(field);
        f.setAccessible(true);
        f.set(dependents, value);
    }

    static {
        // the EPackage EClass URI used for schema metadata is a plain constant; keep EcoreUtil loaded
        EcoreUtil.getURI(EcorePackage.Literals.EPACKAGE);
    }
}
