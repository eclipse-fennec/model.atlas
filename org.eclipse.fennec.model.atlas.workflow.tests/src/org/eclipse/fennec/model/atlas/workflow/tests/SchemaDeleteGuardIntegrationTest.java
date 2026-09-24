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
import java.time.Instant;
import java.util.List;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.model.atlas.action.api.GateTrigger;
import org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic;
import org.eclipse.fennec.model.atlas.mgmt.management.DiagnosticSeverity;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.scope.api.StageGateRefusedException;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.eclipse.fennec.model.atlas.workflow.ResourceSetCollector;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
import org.eclipse.fennec.model.atlas.workflow.dependency.SchemaDependent;
import org.eclipse.fennec.model.atlas.workflow.dependency.SchemaDependents;
import org.eclipse.fennec.model.atlas.workflow.tests.annotations.TestAnnotations;
import org.eclipse.fennec.model.atlas.workflow.tests.support.LuceneAwareTempDirExtension;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
import org.osgi.service.component.ComponentServiceObjects;
import org.osgi.test.common.annotation.InjectService;
import org.osgi.test.common.annotation.Property;
import org.osgi.test.common.annotation.Property.Scalar;
import org.osgi.test.common.annotation.Property.Type;
import org.osgi.test.common.annotation.config.WithFactoryConfiguration;
import org.osgi.test.common.service.ServiceAware;
import org.osgi.test.junit5.cm.ConfigurationExtension;
import org.osgi.test.junit5.context.BundleContextExtension;
import org.osgi.test.junit5.service.ServiceExtension;

/**
 * The regression test of issue #250: deleting a schema from a stage while an instance of it
 * lives in the view that stage serves is refused; {@code force} deletes anyway and leaves the
 * instance listed as <em>unresolved</em> (a {@code schema.dependency-missing} diagnostic under
 * the producer {@code SchemaDependencies}) with a read that reports the missing model; the
 * schema arriving again heals the instance.
 *
 * <p>
 * The schema registry is wired with the bundled {@code SchemaDeleteGate} and
 * {@code SchemaDependencyStageAction} exactly as the runtime configurations wire them.
 * </p>
 */
@RequireEMF
@RequireConfigurationAdmin
@ExtendWith(LuceneAwareTempDirExtension.class)
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@DisplayName("Schema delete guard (issue #250)")
public class SchemaDeleteGuardIntegrationTest {

    static final String SCOPE_NAME = "guard-scope";
    static final String INSTANCES = "guarded-instances";

    private static final String NS_URI = "http://test.fennec.eclipse.org/guard/person/1.0.0";
    private static final String PRODUCER = "SchemaDependencies";
    private static final String CODE_HAS_DEPENDENTS = "schema.has-dependents";
    private static final String CODE_DEPENDENCY_MISSING = "schema.dependency-missing";
    private static final long WAIT = 30000L;

    // The configurations live on the METHOD because LuceneAwareTempDirExtension
    // provides the 'tempDir' system property per test method (beforeEach).
    @SuppressWarnings("unchecked")
    @Test
    @TestAnnotations.EPackageStageActionService
    @CommonTestAnnotations.EPackageLuceneIndexSetup
    @WithFactoryConfiguration(factoryPid = "RegistryService", name = CommonTestAnnotations.SCHEMA_REGISTRY_NAME, location = "?", properties = {
            @Property(key = "registry.name", value = CommonTestAnnotations.SCHEMA_REGISTRY_NAME),
            @Property(key = "registry.type", value = "SCHEMA"),
            @Property(key = "root.eclass.uri", value = "http://www.eclipse.org/emf/2002/Ecore#//EPackage"),
            @Property(key = "resourceSet.target", value = "(emf.name=ecore)"),
            @Property(key = "storageService.target", value = "(storage.type=file)"),
            @Property(key = "storageService.cardinality.minimum", value = "1", scalar = Scalar.Integer),
            @Property(key = "stageActionService.target", value = "(|(component.name=EPackageStageActionService)(component.name=SchemaDependencyStageAction))"),
            @Property(key = "stageActionService.cardinality.minimum", value = "2", scalar = Scalar.Integer),
            @Property(key = "stageGate.target", value = "(component.name=SchemaDeleteGate)"),
            @Property(key = "stageGate.cardinality.minimum", value = "1", scalar = Scalar.Integer),
            @Property(key = "registry.target", value = "(registry=main)"),
            @Property(key = "stages", type = Type.Array, value = {
                    "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                    "{ \"name\" : \"release\", \"writable\" : true, \"final\": true}" }),
            @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
            @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:file", "release:file" }) })
    @WithFactoryConfiguration(factoryPid = "RegistryService", name = INSTANCES, location = "?", properties = {
            @Property(key = "registry.name", value = INSTANCES),
            @Property(key = "registry.type", value = "OTHER"),
            @Property(key = "root.eclass.uri", value = "http://www.eclipse.org/emf/2002/Ecore#//EObject"),
            @Property(key = "resourceSet.target", value = "(emf.name=ecore)"),
            @Property(key = "storageService.target", value = "(storage.type=file)"),
            @Property(key = "storageService.cardinality.minimum", value = "1", scalar = Scalar.Integer),
            @Property(key = "registry.target", value = "(registry=main)"),
            @Property(key = "stages", type = Type.Array, value = {
                    "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                    "{ \"name\" : \"release\", \"writable\" : true, \"final\": true}" }),
            @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
            @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:file", "release:file" }) })
    @WithFactoryConfiguration(factoryPid = "ScopeService", name = SCOPE_NAME, location = "?", properties = {
            @Property(key = "atlas.scope", value = SCOPE_NAME),
            @Property(key = "scope.name", value = SCOPE_NAME),
            @Property(key = "registryService.target", value = "(|(registry.name=" + CommonTestAnnotations.SCHEMA_REGISTRY_NAME
                    + ")(registry.name=" + INSTANCES + "))"),
            @Property(key = "registryService.cardinality.minimum", value = "2", scalar = Scalar.Integer) })
    @DisplayName("A schema with an instance is not deleted; force deletes it and leaves the instance unresolved until the schema returns")
    public void deleteIsRefusedForcedAndHealed(
            @InjectService(cardinality = 0, timeout = WAIT, filter = "(atlas.scope=" + SCOPE_NAME + ")") ServiceAware<WritableScopeService> scopeAware,
            @InjectService(cardinality = 0, timeout = WAIT) ServiceAware<ResourceSetCollector> collectorAware,
            @InjectService(cardinality = 0, timeout = WAIT) ServiceAware<SchemaDependents> dependentsAware)
            throws Exception {

        WritableScopeService<EObject> scopeService = scopeAware.waitForService(WAIT);
        assertNotNull(scopeService, "The scope service should be available");
        SchemaDependents dependents = dependentsAware.waitForService(WAIT);
        assertNotNull(dependents, "The dependents query should be available");

        // 1. a schema in draft and an instance of it in the instance registry's draft
        EPackage personPackage = personPackage();
        ObjectMetadata schemaMetadata = metadata(CommonTestAnnotations.SCHEMA_REGISTRY_NAME, personPackage);
        String schemaId = schemaMetadata.getObjectId();
        scopeService.uploadToStageForRegistry(CommonTestAnnotations.SCHEMA_REGISTRY_NAME,
                CommonTestAnnotations.STAGE_DRAFT, personPackage, schemaMetadata).getValue();
        EPackage registered = registeredPackage(collectorAware.waitForService(WAIT));
        EClass personClass = (EClass) registered.getEClassifier("Person");
        EObject person = registered.getEFactoryInstance().create(personClass);
        person.eSet(personClass.getEStructuralFeature("name"), "Alice");
        ObjectMetadata instanceMetadata = metadata(INSTANCES, person);
        String instanceId = instanceMetadata.getObjectId();
        scopeService.uploadToStageForRegistry(INSTANCES, CommonTestAnnotations.STAGE_DRAFT, person, instanceMetadata)
                .getValue();

        // the query sees the instance as a dependent of the schema
        List<SchemaDependent> found = dependents.dependentsOf(SCOPE_NAME, CommonTestAnnotations.STAGE_DRAFT, NS_URI);
        assertEquals(1, found.size(), "the instance depends on the schema: " + found);
        assertEquals(new SchemaDependent(SchemaDependent.Kind.INSTANCE, INSTANCES, CommonTestAnnotations.STAGE_DRAFT,
                instanceId, EcoreUtil.getURI(personClass).toString()), found.get(0));

        // 2. the delete is refused, the refusal names the instance, nothing changed
        InvocationTargetException failed = assertThrows(InvocationTargetException.class,
                () -> scopeService.deleteFromStageForRegistry(CommonTestAnnotations.SCHEMA_REGISTRY_NAME,
                        CommonTestAnnotations.STAGE_DRAFT, schemaId).getValue());
        StageGateRefusedException refused = assertInstanceOf(StageGateRefusedException.class, failed.getCause());
        assertEquals(GateTrigger.DELETE, refused.trigger());
        assertTrue(refused.getMessage().contains(NS_URI), refused.getMessage());
        assertEquals(1, refused.diagnostics().size(), "the finding about the schema itself");
        assertEquals(CODE_HAS_DEPENDENTS, refused.diagnostics().get(0).code());
        assertEquals(INSTANCES + "/" + CommonTestAnnotations.STAGE_DRAFT + "/" + instanceId,
                refused.diagnostics().get(0).children().get(0).target());

        ObjectMetadata schemaStays = scopeService.getMetadataFromStageForRegistry(
                CommonTestAnnotations.SCHEMA_REGISTRY_NAME, CommonTestAnnotations.STAGE_DRAFT, schemaId);
        assertNotNull(schemaStays, "the schema stays");
        assertEquals(CODE_HAS_DEPENDENTS, only(schemaStays).getCode(), "the veto is recorded on the schema");
        ObjectMetadata instanceUntouched = scopeService.getMetadataFromStageForRegistry(INSTANCES,
                CommonTestAnnotations.STAGE_DRAFT, instanceId);
        assertTrue(instanceUntouched.getDiagnostics().isEmpty(), "nothing happened to the instance");
        assertNotNull(scopeService.getContentFromStageForRegistry(INSTANCES, CommonTestAnnotations.STAGE_DRAFT,
                instanceId), "the instance still reads");

        // 3. forced: the schema goes, the instance is listed as unresolved and no longer reads
        assertTrue(scopeService.deleteFromStageForRegistry(CommonTestAnnotations.SCHEMA_REGISTRY_NAME,
                CommonTestAnnotations.STAGE_DRAFT, schemaId, true).getValue());
        assertNull(scopeService.getMetadataFromStageForRegistry(CommonTestAnnotations.SCHEMA_REGISTRY_NAME,
                CommonTestAnnotations.STAGE_DRAFT, schemaId), "the schema is gone");

        ObjectMetadata listed = scopeService
                .getMetadataFromStageForRegistry(INSTANCES, CommonTestAnnotations.STAGE_DRAFT, instanceId);
        Diagnostic unresolved = only(listed);
        assertEquals(CODE_DEPENDENCY_MISSING, unresolved.getCode());
        assertEquals(DiagnosticSeverity.ERROR, unresolved.getSeverity());
        assertEquals(NS_URI, unresolved.getTarget());
        assertNotNull(unresolved.getId());
        ObjectMetadata inListing = scopeService.listInStageForRegistry(INSTANCES, CommonTestAnnotations.STAGE_DRAFT)
                .stream().filter(m -> instanceId.equals(m.getObjectId())).findFirst().orElse(null);
        assertNotNull(inListing, "the instance is still listed");
        assertEquals(CODE_DEPENDENCY_MISSING, only(inListing).getCode(), "and the listing shows the state");
        assertThrows(Exception.class, () -> scopeService.getContentFromStageForRegistry(INSTANCES,
                CommonTestAnnotations.STAGE_DRAFT, instanceId), "the instance's model is unavailable");

        // 4. the schema comes back: the instance heals
        scopeService.uploadToStageForRegistry(CommonTestAnnotations.SCHEMA_REGISTRY_NAME,
                CommonTestAnnotations.STAGE_DRAFT, personPackage(),
                metadata(CommonTestAnnotations.SCHEMA_REGISTRY_NAME, personPackage())).getValue();
        ObjectMetadata healed = awaitHealed(scopeService, instanceId);
        assertTrue(healed.getDiagnostics().isEmpty(), "the finding about the returned schema is gone: "
                + healed.getDiagnostics());
        assertNotNull(scopeService.getContentFromStageForRegistry(INSTANCES, CommonTestAnnotations.STAGE_DRAFT,
                instanceId), "the instance reads again");
    }

    private static ObjectMetadata awaitHealed(WritableScopeService<EObject> scopeService, String instanceId)
            throws Exception {
        long deadline = System.currentTimeMillis() + WAIT;
        ObjectMetadata last = null;
        while (System.currentTimeMillis() < deadline) {
            last = scopeService.getMetadataFromStageForRegistry(INSTANCES, CommonTestAnnotations.STAGE_DRAFT,
                    instanceId);
            if (last != null && last.getDiagnostics().isEmpty()) {
                return last;
            }
            Thread.sleep(200);
        }
        return last;
    }

    private static Diagnostic only(ObjectMetadata metadata) {
        List<Diagnostic> owned = metadata.getDiagnostics().stream().filter(d -> PRODUCER.equals(d.getProducer()))
                .toList();
        assertEquals(1, owned.size(), "exactly one root of " + PRODUCER + ", got " + metadata.getDiagnostics());
        return owned.get(0);
    }

    private static EPackage registeredPackage(ResourceSetCollector collector) {
        ComponentServiceObjects<ResourceSet> lease = collector.getResourceSetObjects(SCOPE_NAME,
                CommonTestAnnotations.STAGE_DRAFT);
        assertNotNull(lease, "The (scope, stage) chain ResourceSet must exist when the upload returns");
        ResourceSet chainResourceSet = lease.getService();
        try {
            EPackage registered = chainResourceSet.getPackageRegistry().getEPackage(NS_URI);
            assertNotNull(registered, "The uploaded EPackage must be visible in the chain ResourceSet");
            return registered;
        } finally {
            lease.ungetService(chainResourceSet);
        }
    }

    private static EPackage personPackage() {
        EPackage personPackage = EcoreFactory.eINSTANCE.createEPackage();
        personPackage.setName("guardPerson");
        personPackage.setNsPrefix("gperson");
        personPackage.setNsURI(NS_URI);
        EClass personClass = EcoreFactory.eINSTANCE.createEClass();
        personClass.setName("Person");
        EAttribute nameAttribute = EcoreFactory.eINSTANCE.createEAttribute();
        nameAttribute.setName("name");
        nameAttribute.setEType(EcorePackage.Literals.ESTRING);
        personClass.getEStructuralFeatures().add(nameAttribute);
        personPackage.getEClassifiers().add(personClass);
        return personPackage;
    }

    private static ObjectMetadata metadata(String registry, EObject object) {
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectId(object instanceof EPackage ? "guard-person-schema" : "guard-alice");
        metadata.setObjectName("guard-" + registry);
        metadata.setUploadTime(Instant.now());
        metadata.setStage(CommonTestAnnotations.STAGE_DRAFT);
        metadata.setScope(SCOPE_NAME);
        metadata.setRegistry(registry);
        metadata.setObjectType(EcoreUtil.getURI(object.eClass()).toString());
        if (object instanceof EPackage ePackage) {
            metadata.getProperties().put(WorkflowConstants.NS_URI_METADATA_PROPERTY, ePackage.getNsURI());
        }
        return metadata;
    }
}
