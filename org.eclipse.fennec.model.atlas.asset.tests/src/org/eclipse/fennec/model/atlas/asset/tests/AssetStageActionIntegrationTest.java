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
package org.eclipse.fennec.model.atlas.asset.tests;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.emf.osgi.annotation.require.RequireEMF;
import org.eclipse.fennec.model.atlas.action.api.StageActionService;
import org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.SchemaAsset;
import org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.StageableAsset;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.tests.common.CommonTestAnnotations;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.eclipse.fennec.model.atlas.workflow.WorkflowConstants;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.osgi.service.cm.annotations.RequireConfigurationAdmin;
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
 * The asset stage action in a running framework: one action instance bound to two source
 * registries of a scope, writing into a third, the inventory registry.
 *
 * <p>
 * What the unit test cannot show is that the pieces find each other - that a registry binds the
 * action through its {@code stageActionService.target}, that the action's own scope service
 * resolves, that the inventory registry accepts an {@code Asset} subclass, and that an asset
 * written into it comes back out as the EObject it went in as. The decisions the action makes
 * are covered next to the action itself.
 * </p>
 */
@RequireEMF
@RequireConfigurationAdmin
@ExtendWith(AssetStageActionIntegrationTest.TempDirPropertyExtension.class)
@ExtendWith(BundleContextExtension.class)
@ExtendWith(ServiceExtension.class)
@ExtendWith(ConfigurationExtension.class)
@DisplayName("Asset stage action OSGi Integration Tests")
@SuppressWarnings("rawtypes")
public class AssetStageActionIntegrationTest {

	static final String SCOPE = "asset-scope";
	static final String SCHEMA_REGISTRY = "asset-schema";
	static final String OBJECT_REGISTRY = "asset-configurations";
	static final String INVENTORY_REGISTRY = "asset-inventory";
	static final String INVENTORY_STAGE = "current";

	private static final String EPACKAGE_TYPE = "http://www.eclipse.org/emf/2002/Ecore#//EPackage";
	private static final String EANNOTATION_TYPE = "http://www.eclipse.org/emf/2002/Ecore#//EAnnotation";
	private static final String ASSET_ECLASS = "https://org.eclipse/fennec/validation/inventory/1.0.0#//Asset";

	private static final String NS_URI = "http://test.fennec.eclipse.org/asset/clinic/1.0.0";
	private static final String PACKAGE_ID = "clinic-package";
	private static final String INSTANCE_ID = "a-configuration";

	/* ------------------------------------------------------------------ a schema */

	@AssetSetup
	@Test
	@DisplayName("an EPackage entering a stage becomes a SchemaAsset in the inventory registry")
	public void ePackageBecomesASchemaAsset(@InjectService(cardinality = 0, timeout = 30000,
			filter = "(atlas.scope=" + SCOPE + ")") ServiceAware<WritableScopeService> scopeAware,
			@InjectService(cardinality = 0, timeout = 30000,
			filter = "(component.name=AssetStageActionService)") ServiceAware<StageActionService> actionAware)
			throws Exception {

		WritableScopeService<EObject> scope = scope(scopeAware, actionAware);
		upload(scope, SCHEMA_REGISTRY, "draft", PACKAGE_ID, clinicPackage());

		SchemaAsset asset = assertInstanceOf(SchemaAsset.class,
				asset(scope, assetId(SCHEMA_REGISTRY, "draft", PACKAGE_ID)),
				"an EPackage is the one source that gets the schema-specific fields");
		assertEquals(NS_URI, asset.getNsURI());
		assertEquals(EPACKAGE_TYPE, asset.getAssetType());
		assertEquals(SCOPE, asset.getScope());
		assertEquals(SCHEMA_REGISTRY, asset.getRegistry());
		assertEquals("draft", asset.getStage());
		assertEquals(PACKAGE_ID, asset.getObjectId());
	}

	/* ------------------------------------------------------------------ anything else */

	@AssetSetup
	@Test
	@DisplayName("a general object in another registry becomes a plain StageableAsset")
	public void anyObjectBecomesAStageableAsset(@InjectService(cardinality = 0, timeout = 30000,
			filter = "(atlas.scope=" + SCOPE + ")") ServiceAware<WritableScopeService> scopeAware,
			@InjectService(cardinality = 0, timeout = 30000,
			filter = "(component.name=AssetStageActionService)") ServiceAware<StageActionService> actionAware)
			throws Exception {

		WritableScopeService<EObject> scope = scope(scopeAware, actionAware);
		EAnnotation instance = EcoreFactory.eINSTANCE.createEAnnotation();
		instance.setSource("http://test.fennec.eclipse.org/asset/annotation");
		upload(scope, OBJECT_REGISTRY, "draft", INSTANCE_ID, instance);

		StageableAsset asset = asset(scope, assetId(OBJECT_REGISTRY, "draft", INSTANCE_ID));
		assertNotNull(asset, "one action instance serves every registry that binds it");
		assertFalse(asset instanceof SchemaAsset);
		assertEquals(EANNOTATION_TYPE, asset.getAssetType());
		assertEquals(OBJECT_REGISTRY, asset.getRegistry());
		assertEquals(INSTANCE_ID, asset.getObjectId());
	}

	/* ------------------------------------------------------------------ moving on */

	/**
	 * The schema registry keeps the source copy on a transition ({@code delete.after.transition}
	 * defaults to false), so after a promotion the object really is in both stages - and that is
	 * precisely the case a stage-free asset id could not represent.
	 */
	@AssetSetup
	@Test
	@DisplayName("a promotion that keeps the source copy leaves one asset per stage")
	public void promotionKeepingTheSourceLeavesBothAssets(@InjectService(cardinality = 0, timeout = 30000,
			filter = "(atlas.scope=" + SCOPE + ")") ServiceAware<WritableScopeService> scopeAware,
			@InjectService(cardinality = 0, timeout = 30000,
			filter = "(component.name=AssetStageActionService)") ServiceAware<StageActionService> actionAware)
			throws Exception {

		WritableScopeService<EObject> scope = scope(scopeAware, actionAware);
		upload(scope, SCHEMA_REGISTRY, "draft", PACKAGE_ID, clinicPackage());
		assertNotNull(asset(scope, assetId(SCHEMA_REGISTRY, "draft", PACKAGE_ID)));

		scope.transitionToStageForRegistry(SCHEMA_REGISTRY, PACKAGE_ID, "draft", "release");

		StageableAsset draft = asset(scope, assetId(SCHEMA_REGISTRY, "draft", PACKAGE_ID));
		StageableAsset released = asset(scope, assetId(SCHEMA_REGISTRY, "release", PACKAGE_ID));
		assertNotNull(draft, "the object is still in draft, so its draft asset still describes something");
		assertNotNull(released, "the asset of the stage the object entered");
		assertEquals("draft", draft.getStage());
		assertEquals("release", released.getStage());
	}

	/**
	 * The object registry deletes the source copy, so the object leaves draft for real and the
	 * EXIT takes that stage's asset with it.
	 */
	@AssetSetup
	@Test
	@DisplayName("a promotion that deletes the source copy takes the source stage's asset with it")
	public void promotionDeletingTheSourceRemovesItsAsset(@InjectService(cardinality = 0, timeout = 30000,
			filter = "(atlas.scope=" + SCOPE + ")") ServiceAware<WritableScopeService> scopeAware,
			@InjectService(cardinality = 0, timeout = 30000,
			filter = "(component.name=AssetStageActionService)") ServiceAware<StageActionService> actionAware)
			throws Exception {

		WritableScopeService<EObject> scope = scope(scopeAware, actionAware);
		EAnnotation instance = EcoreFactory.eINSTANCE.createEAnnotation();
		instance.setSource("http://test.fennec.eclipse.org/asset/annotation");
		upload(scope, OBJECT_REGISTRY, "draft", INSTANCE_ID, instance);
		assertNotNull(asset(scope, assetId(OBJECT_REGISTRY, "draft", INSTANCE_ID)));

		scope.transitionToStageForRegistry(OBJECT_REGISTRY, INSTANCE_ID, "draft", "release");

		assertNotNull(asset(scope, assetId(OBJECT_REGISTRY, "release", INSTANCE_ID)));
		assertNull(asset(scope, assetId(OBJECT_REGISTRY, "draft", INSTANCE_ID)),
				"the draft asset would describe a stage the object no longer sits in");
	}

	@AssetSetup
	@Test
	@DisplayName("deleting the object removes its asset")
	public void deletionRemovesTheAsset(@InjectService(cardinality = 0, timeout = 30000,
			filter = "(atlas.scope=" + SCOPE + ")") ServiceAware<WritableScopeService> scopeAware,
			@InjectService(cardinality = 0, timeout = 30000,
			filter = "(component.name=AssetStageActionService)") ServiceAware<StageActionService> actionAware)
			throws Exception {

		WritableScopeService<EObject> scope = scope(scopeAware, actionAware);
		upload(scope, SCHEMA_REGISTRY, "draft", PACKAGE_ID, clinicPackage());
		assertNotNull(asset(scope, assetId(SCHEMA_REGISTRY, "draft", PACKAGE_ID)));

		scope.deleteFromStageForRegistry(SCHEMA_REGISTRY, "draft", PACKAGE_ID).getValue();

		assertNull(asset(scope, assetId(SCHEMA_REGISTRY, "draft", PACKAGE_ID)));
	}

	/* ------------------------------------------------------------------ fixtures */

	/**
	 * The scope, once the action it feeds is really there.
	 *
	 * <p>
	 * The action cannot be a mandatory reference of the registries that trigger it: it takes the
	 * scope service those registries belong to, so requiring it would be a cycle - the action
	 * waits for the scope, the scope waits for its registries, the registries wait for the
	 * action. The registries therefore come up without it and bind it when it arrives, which is
	 * what this waits for.
	 * </p>
	 */
	@SuppressWarnings("unchecked")
	private static WritableScopeService<EObject> scope(ServiceAware<WritableScopeService> aware,
			ServiceAware<StageActionService> actionAware) throws InterruptedException {
		WritableScopeService<EObject> scope = aware.waitForService(30000);
		assertNotNull(scope, "the scope comes up once its registries are configured");
		assertNotNull(actionAware.waitForService(30000), "the action binds to the registries once its scope is up");
		return scope;
	}

	/** The id the action mints: the whole address the asset is about, so two stages are two assets. */
	private static String assetId(String registry, String stage, String objectId) {
		return String.join("_", "asset", SCOPE, registry, stage, objectId);
	}

	private static StageableAsset asset(WritableScopeService<EObject> scope, String assetId) {
		return (StageableAsset) scope.getContentFromStageForRegistry(INVENTORY_REGISTRY, INVENTORY_STAGE, assetId);
	}

	private static void upload(WritableScopeService<EObject> scope, String registry, String stage, String objectId,
			EObject object) throws Exception {
		ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadata.setObjectId(objectId);
		metadata.setObjectType(EcoreUtil.getURI(object.eClass()).toString());
		metadata.setUploadTime(Instant.now());
		if (object instanceof EPackage ePackage) {
			// where the workflow reads it from when it raises the event
			metadata.getProperties().put(WorkflowConstants.NS_URI_METADATA_PROPERTY, ePackage.getNsURI());
		}
		scope.uploadToStageForRegistry(registry, stage, object, metadata).getValue();
	}

	private static EPackage clinicPackage() {
		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		ePackage.setName("clinic");
		ePackage.setNsURI(NS_URI);
		ePackage.setNsPrefix("clinic");
		EClass patient = EcoreFactory.eINSTANCE.createEClass();
		patient.setName("Patient");
		ePackage.getEClassifiers().add(patient);
		return ePackage;
	}

	/**
	 * The temp dir the storage and index configurations are templated with. Registered before the
	 * configuration extension so the property is set by the time a configuration is applied.
	 */
	public static class TempDirPropertyExtension implements BeforeAllCallback, AfterAllCallback {

		private Path tempDir;

		@Override
		public void beforeAll(ExtensionContext context) throws IOException {
			tempDir = Files.createTempDirectory("asset-action-test-");
			System.setProperty(CommonTestAnnotations.PROP_TEMP_DIR, tempDir.toString());
		}

		@Override
		public void afterAll(ExtensionContext context) {
			System.clearProperty(CommonTestAnnotations.PROP_TEMP_DIR);
		}
	}

	/* ------------------------------------------------------------------ the wiring */

	@CommonTestAnnotations.StorageSetup
	@CommonTestAnnotations.EPackageLuceneIndexSetup
	// Two sources. Both bind the one action, which is what "configure it per registry" means
	// here: the action answers for whichever registry names it.
	@WithFactoryConfiguration(factoryPid = CommonTestAnnotations.PID_REGISTRY_SERVICE, name = SCHEMA_REGISTRY, location = "?", properties = {
			@Property(key = "registry.name", value = SCHEMA_REGISTRY),
			@Property(key = "registry.type", value = "SCHEMA"),
			@Property(key = "root.eclass.uri", value = EPACKAGE_TYPE),
			@Property(key = "resourceSet.target", value = "(emf.name=ecore)"),
			@Property(key = "storageService.target", value = "(storage.type=file)"),
			@Property(key = "registry.target", value = "(registry=main)"),
			@Property(key = "stageActionService.target", value = "(component.name=AssetStageActionService)"),
			@Property(key = "stages", type = Type.Array, value = {
					"{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
					"{ \"name\" : \"release\", \"writable\" : true, \"final\": true}" }),
			@Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
			@Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:file", "release:file" }) })
	@WithFactoryConfiguration(factoryPid = CommonTestAnnotations.PID_REGISTRY_SERVICE, name = OBJECT_REGISTRY, location = "?", properties = {
			@Property(key = "registry.name", value = OBJECT_REGISTRY),
			@Property(key = "registry.type", value = "OTHER"),
			@Property(key = "delete.after.transition", scalar = Scalar.Boolean, value = "true"),
			@Property(key = "root.eclass.uri", value = "http://www.eclipse.org/emf/2002/Ecore#//EObject"),
			@Property(key = "resourceSet.target", value = "(emf.name=ecore)"),
			@Property(key = "storageService.target", value = "(storage.type=file)"),
			@Property(key = "registry.target", value = "(registry=main)"),
			@Property(key = "stageActionService.target", value = "(component.name=AssetStageActionService)"),
			@Property(key = "stages", type = Type.Array, value = {
					"{ \"name\" : \"draft\", \"writable\" : true, \"final\": false}",
					"{ \"name\" : \"release\", \"writable\" : true, \"final\": true}" }),
			@Property(key = "workflow.transitions", type = Type.Array, value = { "draft:release" }),
			@Property(key = "stage.storage.mappings", type = Type.Array, value = { "draft:file", "release:file" }) })
	// The inventory. It names no stage action on purpose: a registry that binds none cannot
	// re-trigger the action that writes into it.
	@WithFactoryConfiguration(factoryPid = CommonTestAnnotations.PID_REGISTRY_SERVICE, name = INVENTORY_REGISTRY, location = "?", properties = {
			@Property(key = "registry.name", value = INVENTORY_REGISTRY),
			@Property(key = "registry.type", value = "OTHER"),
			@Property(key = "root.eclass.uri", value = ASSET_ECLASS),
			@Property(key = "resourceSet.target", value = "(emf.name=ecore)"),
			@Property(key = "storageService.target", value = "(storage.type=file)"),
			@Property(key = "registry.target", value = "(registry=main)"),
			// Two stages, although the assets only ever use one: a registry must declare exactly
			// one final stage, and the assets' own stage must not be it - an object in a final
			// stage cannot be updated, and an asset is rewritten on every event about its object.
			@Property(key = "stages", type = Type.Array, value = {
					"{ \"name\" : \"" + INVENTORY_STAGE + "\", \"writable\" : true, \"final\": false}",
					"{ \"name\" : \"retired\", \"writable\" : true, \"final\": true}" }),
			@Property(key = "workflow.transitions", type = Type.Array, value = { INVENTORY_STAGE + ":retired" }),
			@Property(key = "stage.storage.mappings", type = Type.Array, value = { INVENTORY_STAGE + ":file",
					"retired:file" }) })
	@WithFactoryConfiguration(factoryPid = "ScopeService", name = SCOPE, location = "?", properties = {
			@Property(key = "atlas.scope", value = SCOPE),
			@Property(key = "scope.name", value = SCOPE),
			@Property(key = "registryService.target", value = "(|(registry.name=" + SCHEMA_REGISTRY
					+ ")(registry.name=" + OBJECT_REGISTRY + ")(registry.name=" + INVENTORY_REGISTRY + "))"),
			@Property(key = "registryService.cardinality.minimum", scalar = Scalar.Integer, value = "3") })
	@WithFactoryConfiguration(factoryPid = "AssetStageActionService", name = "asset-action", location = "?", properties = {
			@Property(key = "triggers", type = Type.Array, value = {
					SCOPE + ":" + SCHEMA_REGISTRY + ":draft", SCOPE + ":" + SCHEMA_REGISTRY + ":release",
					SCOPE + ":" + OBJECT_REGISTRY + ":draft", SCOPE + ":" + OBJECT_REGISTRY + ":release" }),
			@Property(key = "supported.object.types", type = Type.Array, value = { EPACKAGE_TYPE, EANNOTATION_TYPE }),
			@Property(key = "inventory.registry", value = INVENTORY_REGISTRY),
			@Property(key = "inventory.stage", value = INVENTORY_STAGE),
			@Property(key = "scopeService.target", value = "(atlas.scope=" + SCOPE + ")") })
	@Retention(RetentionPolicy.RUNTIME)
	@interface AssetSetup {
	}
}
