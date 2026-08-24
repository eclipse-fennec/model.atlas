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
package org.eclipse.fennec.model.atlas.workflow.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.eclipse.fennec.model.atlas.workflow.ResourceSetCollector;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
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
 * End-to-end regression test for issue #190: an EObject instance whose
 * EPackage was uploaded at runtime (like a REST schema upload) can be stored in
 * an object registry AND read back. Before the fix the read-back died with a
 * {@code ModelUnavailableException} (HTTP 409), because the storage backends
 * re-loaded instances only through their management ResourceSet, which never
 * sees dynamically registered EPackages.
 */
@RequireEMF
@RequireConfigurationAdmin
@ExtendWith(LuceneAwareTempDirExtension.class)
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@DisplayName("Dynamic Instance Readback Integration Tests (issue #190)")
public class DynamicInstanceReadbackIntegrationTest {

    static final String SCOPE_NAME = "readback-scope";

    private static final String NS_URI = "http://test.fennec.eclipse.org/readback/person/1.0.0";

    // The configurations live on the METHOD because LuceneAwareTempDirExtension
    // provides the 'tempDir' system property per test method (beforeEach) - a
    // class-level configuration would be templated before the property exists.
    @SuppressWarnings("unchecked")
    @Test
    @TestAnnotations.EPackageStageActionService
    @CommonTestAnnotations.EPackageLuceneIndexSetup
    // schema registry: like CommonTestAnnotations.SchemaRegistryServiceSetup, but with the
    // stage action service targeted AND a minimum cardinality, so the registry never
    // activates before the EPackageStageActionService (see issue #192)
    @WithFactoryConfiguration(factoryPid = "RegistryService", name = CommonTestAnnotations.SCHEMA_REGISTRY_NAME, location = "?", properties = {
        @Property(key = "registry.name", value = CommonTestAnnotations.SCHEMA_REGISTRY_NAME),
        @Property(key = "registry.type", value = "SCHEMA"),
        @Property(key = "schema.uri", value = "http://www.eclipse.org/emf/2002/Ecore"),
        @Property(key = "root.eclass.uri", value = "http://www.eclipse.org/emf/2002/Ecore#//EPackage"),
        @Property(key = "resourceSet.target", value = "(emf.name=ecore)"),
        @Property(key = "storageService.target", value = "(storage.type=file)"),
        @Property(key = "storageService.cardinality.minimum", value = "1", scalar = Scalar.Integer),
        @Property(key = "stageActionService.target", value = "(component.name=EPackageStageActionService)"),
        @Property(key = "stageActionService.cardinality.minimum", value = "1", scalar = Scalar.Integer),
        @Property(key = "registry.target", value = "(registry=main)"),
        @Property(key = "stages", type = Type.Array, value = {
                "{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
                "{ \"name\" : \"release\", \"writable\" : true, \"final\": true}" }),
        @Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
        @Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:file", "release:file" }) })
// object registry rooted at the implicit EObject root (accepts any instance, #188)
@WithFactoryConfiguration(factoryPid = "RegistryService", name = "configurations", location = "?", properties = {
        @Property(key = "registry.name", value = "configurations"),
        @Property(key = "registry.type", value = "OTHER"),
        @Property(key = "schema.uri", value = "http://www.eclipse.org/emf/2002/Ecore"),
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
    @WithFactoryConfiguration(factoryPid = "ScopeService", name = DynamicInstanceReadbackIntegrationTest.SCOPE_NAME, location = "?", properties = {
            @Property(key = "atlas.scope", value = DynamicInstanceReadbackIntegrationTest.SCOPE_NAME),
            @Property(key = "scope.name", value = DynamicInstanceReadbackIntegrationTest.SCOPE_NAME),
            @Property(key = "registryService.target", value = "(|(registry.name=" + CommonTestAnnotations.SCHEMA_REGISTRY_NAME
                    + ")(registry.name=configurations))"),
            @Property(key = "registryService.cardinality.minimum", value = "2", scalar = Scalar.Integer) })
    @DisplayName("An instance of a runtime-uploaded schema can be stored and read back")
    public void instanceOfRuntimeUploadedSchemaCanBeReadBack(
            @InjectService(cardinality = 0, timeout = 30000,
                    filter = "(atlas.scope=" + SCOPE_NAME + ")") ServiceAware<WritableScopeService> scopeAware,
            @InjectService(cardinality = 0, timeout = 30000) ServiceAware<ResourceSetCollector> collectorAware)
            throws Exception {

        WritableScopeService<EObject> scopeService = scopeAware.waitForService(30000);
        assertNotNull(scopeService, "The test scope service should be available");

        // a dynamic EPackage, exactly what a REST schema upload produces
        EPackage personPackage = EcoreFactory.eINSTANCE.createEPackage();
        personPackage.setName("readbackPerson");
        personPackage.setNsPrefix("rperson");
        personPackage.setNsURI(NS_URI);
        EClass personClass = EcoreFactory.eINSTANCE.createEClass();
        personClass.setName("Person");
        EAttribute nameAttribute = EcoreFactory.eINSTANCE.createEAttribute();
        nameAttribute.setName("name");
        nameAttribute.setEType(EcorePackage.Literals.ESTRING);
        personClass.getEStructuralFeatures().add(nameAttribute);
        personPackage.getEClassifiers().add(personClass);

        scopeService.uploadToStageForRegistry(CommonTestAnnotations.SCHEMA_REGISTRY_NAME,
                CommonTestAnnotations.STAGE_DRAFT, personPackage,
                metadata(CommonTestAnnotations.SCHEMA_REGISTRY_NAME, personPackage)).getValue();

        // issue #196: the moment the upload promise resolves, the registration must
        // already be visible in the (scope, stage) chain ResourceSet - the stage
        // action blocks until the SCR-driven registry update is through, so a
        // response serialized against a leased ResourceSet can no longer race it.
        // Deliberately NO polling here.
        ResourceSetCollector collector = collectorAware.waitForService(30000);
        assertNotNull(collector);
        ComponentServiceObjects<ResourceSet> lease = collector.getResourceSetObjects(SCOPE_NAME,
                CommonTestAnnotations.STAGE_DRAFT);
        assertNotNull(lease, "The (scope, stage) chain ResourceSet must exist when the upload returns");
        EPackage registeredPackage;
        ResourceSet chainResourceSet = lease.getService();
        try {
            registeredPackage = chainResourceSet.getPackageRegistry().getEPackage(NS_URI);
        } finally {
            lease.ungetService(chainResourceSet);
        }
        assertNotNull(registeredPackage,
                "The uploaded EPackage must be visible in the chain ResourceSet the moment the upload returns");

        // An instance of the REGISTERED package, like one deserialized from a REST
        // upload: the registered package's resource URI is the nsURI, so the stored
        // instance references it by nsURI - resolvable only where the dynamic
        // registration is visible (the local personPackage would leak a file: URI
        // to the stored schema instead and hide the bug).
        EClass registeredPersonClass = (EClass) registeredPackage.getEClassifier("Person");
        EObject person = registeredPackage.getEFactoryInstance().create(registeredPersonClass);
        person.eSet(registeredPersonClass.getEStructuralFeature("name"), "Grace");
        ObjectMetadata instanceMetadata = metadata("configurations", person);
        String objectId = instanceMetadata.getObjectId();
        scopeService.uploadToStageForRegistry("configurations", CommonTestAnnotations.STAGE_DRAFT, person,
                instanceMetadata).getValue();

        // the read-back previously failed with ModelUnavailableException (HTTP 409)
        EObject loaded = scopeService.getContentFromStageForRegistry("configurations",
                CommonTestAnnotations.STAGE_DRAFT, objectId);
        assertNotNull(loaded, "The stored instance should be readable");
        assertEquals("Person", loaded.eClass().getName());
        assertEquals(NS_URI, loaded.eClass().getEPackage().getNsURI());
        assertEquals("Grace", loaded.eGet(loaded.eClass().getEStructuralFeature("name")));
    }

    private ObjectMetadata metadata(String registry, EObject object) {
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectId(UUID.randomUUID().toString());
        metadata.setObjectName("readback-" + registry);
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
