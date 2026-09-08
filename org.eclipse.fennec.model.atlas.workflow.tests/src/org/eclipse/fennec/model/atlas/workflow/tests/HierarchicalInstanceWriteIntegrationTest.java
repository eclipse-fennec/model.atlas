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
import org.junit.jupiter.api.Disabled;
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
 * Issue #263: an instance stored in a child scope whose EPackages live in the parent scope.
 *
 * <p>
 * The reported deployment is three levels — {@code atlas}, a {@code platform} scope holding
 * the shared metamodels, and a tenant scope parented on it that holds only instances. Storing
 * an instance in the tenant scope answers {@code 500 Error de-serializing incoming data},
 * caused by {@code PackageNotFoundException} for a package that sits in the parent's final
 * stage: the per-(scope, stage) chain ResourceSet the request is served with does not resolve
 * it.
 * </p>
 *
 * <p>
 * The tests stop at that ResourceSet rather than going through HTTP, because that is where the
 * package has to be visible — the codec's reader only asks it for the package.
 * </p>
 *
 * <p>
 * The cause splits in two. <b>This repository's half</b> is that a scope only got a
 * per-(scope, stage) ResourceSet for the stages its <em>schema</em> registry declares, so a
 * tenant scope holding nothing but instances got none at all and every request against it was
 * served with an unrelated one; {@link #childScopeHasAResourceSetForTheStageBeingWrittenTo}
 * covers that. <b>The other half</b> is that a configured EPackage registry never delegates to
 * the parent registry it is configured with, so even with a ResourceSet in place the parent
 * scope's packages stay invisible — filed as eclipse-fennec/emf.osgi#105, and the two tests
 * waiting on it are disabled with that pointer rather than deleted.
 * </p>
 *
 * @see <a href="https://github.com/eclipse-fennec/model.atlas/issues/263">issue #263</a>
 */
@RequireEMF
@RequireConfigurationAdmin
@ExtendWith(LuceneAwareTempDirExtension.class)
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@DisplayName("Hierarchical instance write (issue #263)")
public class HierarchicalInstanceWriteIntegrationTest {

	static final String PARENT_SCOPE = "hier-parent";

	static final String CHILD_SCOPE = "hier-child";

	private static final String OBJECT_REGISTRY = "hier-configurations";

	private static final String NS_URI = "http://test.fennec.eclipse.org/hier/configuration/1.0.0";

	@Test
	@TestAnnotations.EPackageStageActionService
	@CommonTestAnnotations.EPackageLuceneIndexSetup
	@HierarchySetup
	@DisplayName("the child scope has a chain ResourceSet for the stage being written to")
	public void childScopeHasAResourceSetForTheStageBeingWrittenTo(
			@InjectService(cardinality = 0, timeout = 30000,
					filter = "(atlas.scope=" + PARENT_SCOPE + ")") ServiceAware<WritableScopeService> parentAware,
			@InjectService(cardinality = 0, timeout = 30000,
					filter = "(atlas.scope=" + CHILD_SCOPE + ")") ServiceAware<WritableScopeService> childAware,
			@InjectService(cardinality = 0, timeout = 30000) ServiceAware<ResourceSetCollector> collectorAware)
			throws Exception {

		@SuppressWarnings("unchecked")
		WritableScopeService<EObject> parentScope = parentAware.waitForService(30000);
		assertNotNull(parentScope, "the parent scope service should be available");
		assertNotNull(childAware.waitForService(30000), "the child scope service should be available");

		// Step 3 of the report: the metamodel is uploaded to the PARENT's final stage.
		EPackage configurationPackage = configurationPackage();
		parentScope.uploadToStageForRegistry(CommonTestAnnotations.SCHEMA_REGISTRY_NAME,
				CommonTestAnnotations.STAGE_RELEASE, configurationPackage,
				metadata(PARENT_SCOPE, CommonTestAnnotations.SCHEMA_REGISTRY_NAME, configurationPackage)).getValue();

		ResourceSetCollector collector = collectorAware.waitForService(30000);
		assertNotNull(collector);

		// The registration itself works: the package is visible where it was uploaded.
		ComponentServiceObjects<ResourceSet> parentLease = collector.getResourceSetObjects(PARENT_SCOPE,
				CommonTestAnnotations.STAGE_RELEASE);
		assertNotNull(parentLease, "the parent scope must have a chain ResourceSet for its release stage");
		ResourceSet parentResourceSet = parentLease.getService();
		try {
			assertNotNull(parentResourceSet.getPackageRegistry().getEPackage(NS_URI),
					"the uploaded package must be visible in the scope it was uploaded to");
		} finally {
			parentLease.ungetService(parentResourceSet);
		}

		// Step 4: an instance write against the CHILD is served with the child's
		// (scope, stage) ResourceSet. The child holds no schema registry, so before the fix
		// the chain configurator generated nothing for it and the request was deserialized
		// against whichever ResourceSet the provider fell back to.
		ComponentServiceObjects<ResourceSet> lease = collector.getResourceSetObjects(CHILD_SCOPE,
				CommonTestAnnotations.STAGE_RELEASE);
		assertNotNull(lease, "the child scope must have a chain ResourceSet for the stage being written to,"
				+ " even though no schema registry of that scope declares the stage");
		lease.ungetService(lease.getService());
	}

	@Test
	@Disabled("blocked on eclipse-fennec/emf.osgi#105: a configured EPackage registry does not"
			+ " delegate to the parentRegistry it is configured with, so the parent scope's"
			+ " packages cannot be resolved through the child's chain ResourceSet")
	@TestAnnotations.EPackageStageActionService
	@CommonTestAnnotations.EPackageLuceneIndexSetup
	@HierarchySetup
	@DisplayName("the child's chain ResourceSet resolves a package released in the parent scope")
	public void childSeesTheParentsReleasedPackage(
			@InjectService(cardinality = 0, timeout = 30000,
					filter = "(atlas.scope=" + PARENT_SCOPE + ")") ServiceAware<WritableScopeService> parentAware,
			@InjectService(cardinality = 0, timeout = 30000,
					filter = "(atlas.scope=" + CHILD_SCOPE + ")") ServiceAware<WritableScopeService> childAware,
			@InjectService(cardinality = 0, timeout = 30000) ServiceAware<ResourceSetCollector> collectorAware)
			throws Exception {

		@SuppressWarnings("unchecked")
		WritableScopeService<EObject> parentScope = parentAware.waitForService(30000);
		assertNotNull(parentScope);
		assertNotNull(childAware.waitForService(30000));

		EPackage configurationPackage = configurationPackage();
		parentScope.uploadToStageForRegistry(CommonTestAnnotations.SCHEMA_REGISTRY_NAME,
				CommonTestAnnotations.STAGE_RELEASE, configurationPackage,
				metadata(PARENT_SCOPE, CommonTestAnnotations.SCHEMA_REGISTRY_NAME, configurationPackage)).getValue();

		ResourceSetCollector collector = collectorAware.waitForService(30000);
		ComponentServiceObjects<ResourceSet> lease = collector.getResourceSetObjects(CHILD_SCOPE,
				CommonTestAnnotations.STAGE_RELEASE);
		assertNotNull(lease);
		ResourceSet childResourceSet = lease.getService();
		try {
			assertNotNull(childResourceSet.getPackageRegistry().getEPackage(NS_URI),
					"a package released in the parent scope must be visible to the child's chain ResourceSet"
							+ " - this is what fails as 'Package with uri ... not found' during deserialization");
		} finally {
			lease.ungetService(childResourceSet);
		}
	}

	@Test
	@Disabled("blocked on eclipse-fennec/emf.osgi#105, see childSeesTheParentsReleasedPackage")
	@TestAnnotations.EPackageStageActionService
	@CommonTestAnnotations.EPackageLuceneIndexSetup
	@HierarchySetup
	@DisplayName("and the instance itself can be stored in the child and read back")
	public void anInstanceOfTheParentsPackageCanBeStoredInTheChild(
			@InjectService(cardinality = 0, timeout = 30000,
					filter = "(atlas.scope=" + PARENT_SCOPE + ")") ServiceAware<WritableScopeService> parentAware,
			@InjectService(cardinality = 0, timeout = 30000,
					filter = "(atlas.scope=" + CHILD_SCOPE + ")") ServiceAware<WritableScopeService> childAware,
			@InjectService(cardinality = 0, timeout = 30000) ServiceAware<ResourceSetCollector> collectorAware)
			throws Exception {

		@SuppressWarnings("unchecked")
		WritableScopeService<EObject> parentScope = parentAware.waitForService(30000);
		@SuppressWarnings("unchecked")
		WritableScopeService<EObject> childScope = childAware.waitForService(30000);
		assertNotNull(parentScope);
		assertNotNull(childScope);

		EPackage configurationPackage = configurationPackage();
		parentScope.uploadToStageForRegistry(CommonTestAnnotations.SCHEMA_REGISTRY_NAME,
				CommonTestAnnotations.STAGE_RELEASE, configurationPackage,
				metadata(PARENT_SCOPE, CommonTestAnnotations.SCHEMA_REGISTRY_NAME, configurationPackage)).getValue();

		ResourceSetCollector collector = collectorAware.waitForService(30000);
		ComponentServiceObjects<ResourceSet> lease = collector.getResourceSetObjects(CHILD_SCOPE,
				CommonTestAnnotations.STAGE_RELEASE);
		assertNotNull(lease, "the child scope must have a chain ResourceSet for the stage being written to");

		EObject configuration;
		ResourceSet childResourceSet = lease.getService();
		try {
			EPackage visible = childResourceSet.getPackageRegistry().getEPackage(NS_URI);
			assertNotNull(visible, "the parent's package must be visible in the child");
			// An instance of the REGISTERED package, the way one arrives from a request: its
			// type is referenced by nsURI, resolvable only where the registration is visible.
			EClass configurationClass = (EClass) visible.getEClassifier("Configuration");
			configuration = visible.getEFactoryInstance().create(configurationClass);
			configuration.eSet(configurationClass.getEStructuralFeature("name"), "tenant-config");
		} finally {
			lease.ungetService(childResourceSet);
		}

		ObjectMetadata instanceMetadata = metadata(CHILD_SCOPE, OBJECT_REGISTRY, configuration);
		String objectId = instanceMetadata.getObjectId();
		childScope.uploadToStageForRegistry(OBJECT_REGISTRY, CommonTestAnnotations.STAGE_RELEASE, configuration,
				instanceMetadata).getValue();

		EObject loaded = childScope.getContentFromStageForRegistry(OBJECT_REGISTRY,
				CommonTestAnnotations.STAGE_RELEASE, objectId);
		assertNotNull(loaded, "the instance stored in the child scope should be readable");
		assertEquals(NS_URI, loaded.eClass().getEPackage().getNsURI());
		assertEquals("tenant-config", loaded.eGet(loaded.eClass().getEStructuralFeature("name")));
	}

	private static EPackage configurationPackage() {
		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		ePackage.setName("hierConfiguration");
		ePackage.setNsPrefix("hierconf");
		ePackage.setNsURI(NS_URI);
		EClass configurationClass = EcoreFactory.eINSTANCE.createEClass();
		configurationClass.setName("Configuration");
		EAttribute nameAttribute = EcoreFactory.eINSTANCE.createEAttribute();
		nameAttribute.setName("name");
		nameAttribute.setEType(EcorePackage.Literals.ESTRING);
		configurationClass.getEStructuralFeatures().add(nameAttribute);
		ePackage.getEClassifiers().add(configurationClass);
		return ePackage;
	}

	private ObjectMetadata metadata(String scope, String registry, EObject object) {
		ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadata.setObjectId(UUID.randomUUID().toString());
		metadata.setObjectName("hier-" + registry);
		metadata.setUploadTime(Instant.now());
		metadata.setStage(CommonTestAnnotations.STAGE_RELEASE);
		metadata.setScope(scope);
		metadata.setRegistry(registry);
		metadata.setObjectType(EcoreUtil.getURI(object.eClass()).toString());
		if (object instanceof EPackage ePackage) {
			metadata.getProperties().put(WorkflowConstants.NS_URI_METADATA_PROPERTY, ePackage.getNsURI());
		}
		return metadata;
	}

	/**
	 * The reported hierarchy: a parent scope holding the schema registry, and a child scope
	 * parented on it that holds only an object registry — no schemas of its own.
	 */
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
	@WithFactoryConfiguration(factoryPid = "RegistryService", name = OBJECT_REGISTRY, location = "?", properties = {
			@Property(key = "registry.name", value = OBJECT_REGISTRY),
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
	@WithFactoryConfiguration(factoryPid = "ScopeService", name = PARENT_SCOPE, location = "?", properties = {
			@Property(key = "atlas.scope", value = PARENT_SCOPE),
			@Property(key = "scope.name", value = PARENT_SCOPE),
			@Property(key = "registryService.target", value = "(registry.name="
					+ CommonTestAnnotations.SCHEMA_REGISTRY_NAME + ")"),
			@Property(key = "registryService.cardinality.minimum", value = "1", scalar = Scalar.Integer) })
	@WithFactoryConfiguration(factoryPid = "ScopeService", name = CHILD_SCOPE, location = "?", properties = {
			@Property(key = "atlas.scope", value = CHILD_SCOPE),
			@Property(key = "scope.name", value = CHILD_SCOPE),
			@Property(key = "scope.parent", value = PARENT_SCOPE),
			@Property(key = "registryService.target", value = "(registry.name=" + OBJECT_REGISTRY + ")"),
			@Property(key = "registryService.cardinality.minimum", value = "1", scalar = Scalar.Integer) })
	@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
	@interface HierarchySetup {
	}
}
