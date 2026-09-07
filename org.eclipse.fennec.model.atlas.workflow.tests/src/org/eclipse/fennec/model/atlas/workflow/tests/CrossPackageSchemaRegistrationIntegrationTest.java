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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.workflow.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.InternalEObject;
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
 * Regression test for issue #251: a schema whose {@code eType} references cross
 * into another atlas-hosted package must work regardless of registration order.
 *
 * <p>
 * Before the fix, {@code registerEPackage} detached the package's resource from
 * every ResourceSet, so a package registered <em>before</em> its dependency kept
 * its cross-package references as unresolved proxies forever — even after the
 * dependency arrived. On a restart the replay order is undefined, so any such
 * deployment eventually registered the dependent first, and reading an instance
 * of it died dereferencing the proxy (bare {@code NullPointerException},
 * HTTP 500). Registered packages are now anchored per (scope, stage) so their
 * proxies resolve lazily whenever the dependency shows up.
 * </p>
 */
@RequireEMF
@RequireConfigurationAdmin
@ExtendWith(LuceneAwareTempDirExtension.class)
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@DisplayName("Cross-package schema registration (issue #251)")
public class CrossPackageSchemaRegistrationIntegrationTest {

    static final String SCOPE_NAME = "xref-scope";

    private static final String NS_A = "http://test.fennec.eclipse.org/xref/base/1.0.0";
    private static final String NS_B = "http://test.fennec.eclipse.org/xref/dependent/1.0.0";

    @SuppressWarnings("unchecked")
    @Test
    @TestAnnotations.EPackageStageActionService
    @CommonTestAnnotations.EPackageLuceneIndexSetup
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
    @WithFactoryConfiguration(factoryPid = "ScopeService", name = CrossPackageSchemaRegistrationIntegrationTest.SCOPE_NAME, location = "?", properties = {
            @Property(key = "atlas.scope", value = CrossPackageSchemaRegistrationIntegrationTest.SCOPE_NAME),
            @Property(key = "scope.name", value = CrossPackageSchemaRegistrationIntegrationTest.SCOPE_NAME),
            @Property(key = "registryService.target", value = "(|(registry.name=" + CommonTestAnnotations.SCHEMA_REGISTRY_NAME
                    + ")(registry.name=configurations))"),
            @Property(key = "registryService.cardinality.minimum", value = "2", scalar = Scalar.Integer) })
    @DisplayName("A dependent uploaded BEFORE its dependency heals once the dependency arrives")
    public void dependentRegisteredBeforeDependencyResolvesLazily(
            @InjectService(cardinality = 0, timeout = 30000,
                    filter = "(atlas.scope=" + SCOPE_NAME + ")") ServiceAware<WritableScopeService> scopeAware,
            @InjectService(cardinality = 0, timeout = 30000) ServiceAware<ResourceSetCollector> collectorAware)
            throws Exception {

        WritableScopeService<EObject> scopeService = scopeAware.waitForService(30000);
        assertNotNull(scopeService, "The test scope service should be available");
        ResourceSetCollector collector = collectorAware.waitForService(30000);
        assertNotNull(collector);

        // Package B references package A's classifier by nsURI — exactly the proxy a
        // deserialized .ecore with a cross-package eType carries. A is NOT uploaded yet.
        EPackage packageB = EcoreFactory.eINSTANCE.createEPackage();
        packageB.setName("xrefDependent");
        packageB.setNsPrefix("xdep");
        packageB.setNsURI(NS_B);
        EClass thingClass = EcoreFactory.eINSTANCE.createEClass();
        thingClass.setName("Thing");
        EReference baseReference = EcoreFactory.eINSTANCE.createEReference();
        baseReference.setName("base");
        baseReference.setContainment(true);
        EClass baseProxy = EcoreFactory.eINSTANCE.createEClass();
        ((InternalEObject) baseProxy).eSetProxyURI(URI.createURI(NS_A + "#//Base"));
        baseReference.setEType(baseProxy);
        thingClass.getEStructuralFeatures().add(baseReference);
        packageB.getEClassifiers().add(thingClass);

        // 1) upload the DEPENDENT first: it registers while its dependency is unknown
        scopeService.uploadToStageForRegistry(CommonTestAnnotations.SCHEMA_REGISTRY_NAME,
                CommonTestAnnotations.STAGE_DRAFT, packageB,
                metadata(CommonTestAnnotations.SCHEMA_REGISTRY_NAME, packageB)).getValue();

        EPackage registeredB = registeredPackage(collector, NS_B);
        EReference registeredBase = (EReference) ((EClass) registeredB.getEClassifier("Thing"))
                .getEStructuralFeature("base");
        // resolution is attempted but must fail — the dependency is not there yet, so
        // getEType() still answers the unresolved proxy (and does not cache a failure)
        assertTrue(registeredBase.getEType().eIsProxy(),
                "Before the dependency arrives the cross-package reference is an unresolved proxy");

        // 2) upload the DEPENDENCY afterwards
        EPackage packageA = EcoreFactory.eINSTANCE.createEPackage();
        packageA.setName("xrefBase");
        packageA.setNsPrefix("xbase");
        packageA.setNsURI(NS_A);
        EClass baseClass = EcoreFactory.eINSTANCE.createEClass();
        baseClass.setName("Base");
        packageA.getEClassifiers().add(baseClass);
        scopeService.uploadToStageForRegistry(CommonTestAnnotations.SCHEMA_REGISTRY_NAME,
                CommonTestAnnotations.STAGE_DRAFT, packageA,
                metadata(CommonTestAnnotations.SCHEMA_REGISTRY_NAME, packageA)).getValue();

        // 3) the dependent's proxy now resolves lazily — this was frozen before the fix
        EClass resolvedType = (EClass) registeredBase.getEType();
        assertFalse(resolvedType.eIsProxy(),
                "After the dependency registered for the same (scope, stage), the reference must resolve");
        assertEquals("Base", resolvedType.getName());
        assertEquals(NS_A, resolvedType.getEPackage().getNsURI());

        // 4) end to end: an instance of B (containing a Base) survives store + read
        EPackage registeredA = registeredPackage(collector, NS_A);
        EObject base = registeredA.getEFactoryInstance()
                .create((EClass) registeredA.getEClassifier("Base"));
        EClass registeredThingClass = (EClass) registeredB.getEClassifier("Thing");
        EObject thing = registeredB.getEFactoryInstance().create(registeredThingClass);
        thing.eSet(registeredThingClass.getEStructuralFeature("base"), base);

        ObjectMetadata instanceMetadata = metadata("configurations", thing);
        String objectId = instanceMetadata.getObjectId();
        scopeService.uploadToStageForRegistry("configurations", CommonTestAnnotations.STAGE_DRAFT, thing,
                instanceMetadata).getValue();

        EObject loaded = scopeService.getContentFromStageForRegistry("configurations",
                CommonTestAnnotations.STAGE_DRAFT, objectId);
        assertNotNull(loaded, "The stored instance should be readable");
        assertEquals("Thing", loaded.eClass().getName());
        EObject loadedBase = (EObject) loaded.eGet(loaded.eClass().getEStructuralFeature("base"));
        assertNotNull(loadedBase, "The cross-package containment must deserialize");
        assertEquals("Base", loadedBase.eClass().getName());
        assertEquals(NS_A, loadedBase.eClass().getEPackage().getNsURI());
    }

    private EPackage registeredPackage(ResourceSetCollector collector, String nsUri) {
        ComponentServiceObjects<ResourceSet> lease = collector.getResourceSetObjects(SCOPE_NAME,
                CommonTestAnnotations.STAGE_DRAFT);
        assertNotNull(lease, "The (scope, stage) chain ResourceSet must exist");
        ResourceSet chainResourceSet = lease.getService();
        try {
            EPackage registered = chainResourceSet.getPackageRegistry().getEPackage(nsUri);
            assertNotNull(registered, "The uploaded EPackage must be registered: " + nsUri);
            return registered;
        } finally {
            lease.ungetService(chainResourceSet);
        }
    }

    private ObjectMetadata metadata(String registry, EObject object) {
        ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
        metadata.setObjectId(UUID.randomUUID().toString());
        metadata.setObjectName("xref-" + registry + "-" + UUID.randomUUID());
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
