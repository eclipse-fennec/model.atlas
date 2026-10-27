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
package org.eclipse.fennec.model.atlas.asset.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.action.api.ActionContext;
import org.eclipse.fennec.model.atlas.action.api.StageActionService.ActionEvent;
import org.eclipse.fennec.model.atlas.action.api.StageActionService.ExitReason;
import org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.ModelAtlasAssetPackage;
import org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.SchemaAsset;
import org.eclipse.fennec.model.atlas.asset.model.modelatlasasset.StageableAsset;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.Promises;

/**
 * What the action decides, rather than what the registry does with it: which events it answers,
 * what the asset it mints says, where that asset is addressed, and what a failed write costs. The
 * wiring - a real registry, a real scope service - belongs to the OSGi integration test.
 */
class AssetStageActionServiceTest {

	private static final String EPACKAGE_TYPE = EcoreUtil.getURI(EcorePackage.Literals.EPACKAGE).toString();
	private static final String SOURCE_SCOPE = "jena";
	private static final String SOURCE_REGISTRY = "schema";
	private static final String INVENTORY_REGISTRY = "inventory";
	private static final String INVENTORY_STAGE = "current";
	private static final String OBJECT_ID = "a-package";
	private static final String NS_URI = "https://example.org/clinic/1.0.0";
	private static final String FINGERPRINT = "fp1:9f2c1ab7d4e85530";

	private final Scope scope = new Scope();

	/* ------------------------------------------------------------------ what it answers */

	@Test
	@DisplayName("the object type is matched as an EClass URI, and a null type is no match rather than a crash")
	void supportedObjectTypes() {
		var action = action(new String[] { SOURCE_SCOPE + ":" + SOURCE_REGISTRY + ":draft" },
				new String[] { EPACKAGE_TYPE });

		assertTrue(action.supportsObjectType(EPACKAGE_TYPE));
		assertFalse(action.supportsObjectType("EPackage"),
				"the storage layer writes the EClass URI; matching the simple name would never fire");
		assertFalse(action.supportsObjectType(null),
				"an object whose type the storage never resolved must not blow up the dispatch");
	}

	@Test
	@DisplayName("the trigger stages are the stage parts of the configured tuples")
	void triggerStages() {
		var action = action(new String[] { "jena:schema:draft", "other:schema:release" }, new String[0]);

		assertEquals(Set.of("draft", "release"), action.getTriggerStages());
		assertEquals(Set.of(ActionEvent.ENTER, ActionEvent.UPDATE, ActionEvent.EXIT), action.getTriggerEvents());
	}

	@Test
	@DisplayName("a trigger that leaves the stage open means every stage, not the stages the others name")
	void openStageMeansEveryStage() {
		assertEquals(Set.of(), action(new String[] { "jena:schema:draft", "jena:schema:*" }, new String[0])
				.getTriggerStages());
		assertEquals(Set.of(), action(new String[] { "jena:schema:" }, new String[0]).getTriggerStages());
		assertEquals(Set.of(), action(new String[0], new String[0]).getTriggerStages(),
				"no trigger at all is every stage too");
	}

	@Test
	@DisplayName("a trigger that is no tuple fails the activation instead of quietly dispatching nothing")
	void malformedTriggerFailsActivation() {
		IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class,
				() -> action(new String[] { "jena:draft" }, new String[0]));
		assertTrue(thrown.getMessage().contains("jena:draft"), thrown.getMessage());
	}

	/* ------------------------------------------------------------------ the asset it mints */

	@Test
	@DisplayName("an EPackage becomes a SchemaAsset carrying its fingerprint and nsURI")
	void ePackageBecomesASchemaAsset() {
		var action = action(new String[0], new String[] { EPACKAGE_TYPE });

		action.onEnter(ePackageContext("draft"));

		SchemaAsset asset = assertInstanceOf(SchemaAsset.class, scope.written.get());
		assertEquals(FINGERPRINT, asset.getFingerprint());
		assertEquals(NS_URI, asset.getNsURI());
		assertEquals(EPACKAGE_TYPE, asset.getAssetType());
		assertEquals(SOURCE_SCOPE, asset.getScope());
		assertEquals(SOURCE_REGISTRY, asset.getRegistry());
		assertEquals("draft", asset.getStage());
		assertEquals(OBJECT_ID, asset.getObjectId(), "the asset remembers which object it is about");
	}

	@Test
	@DisplayName("anything else becomes a plain StageableAsset carrying its own type")
	void otherObjectsBecomeStageableAssets() {
		String instanceType = EcoreUtil.getURI(EcorePackage.Literals.EANNOTATION).toString();
		var action = action(new String[0], new String[] { instanceType });

		action.onEnter(context("configuration", "draft", "an-instance", instanceType, Map.of()));

		StageableAsset asset = scope.written.get();
		assertFalse(asset instanceof SchemaAsset, "only an EPackage carries a schema's fields");
		assertEquals(instanceType, asset.getAssetType());
		assertEquals("configuration", asset.getRegistry());
	}

	@Test
	@DisplayName("the asset's own id is the id the registry addresses it by")
	void theAssetIdIsTheObjectId() {
		var action = action(new String[0], new String[] { EPACKAGE_TYPE });

		action.onEnter(ePackageContext("draft"));

		assertEquals(scope.writtenId.get(), scope.written.get().getId());
	}

	/* ------------------------------------------------------------------ the address */

	@Test
	@DisplayName("the asset id carries scope, registry and stage, so one object in two stages is two assets")
	void oneObjectInTwoStagesIsTwoAssets() {
		var action = action(new String[0], new String[] { EPACKAGE_TYPE });

		action.onEnter(ePackageContext("draft"));
		String draft = scope.writtenId.get();
		action.onEnter(ePackageContext("release"));
		String release = scope.writtenId.get();

		assertEquals("asset_jena_schema_draft_a-package", draft);
		assertEquals("asset_jena_schema_release_a-package", release);
		assertNotEquals(draft, release,
				"an objectId is unique per stage, not across them (issue #211) - one asset would clobber the other");
		assertFalse(draft.contains("#"), "EMF reads a # in an ID attribute back as a cross-document href");
	}

	@Test
	@DisplayName("the same objectId in two scopes or two registries is two assets as well")
	void theAddressSeparatesScopesAndRegistries() {
		var action = action(new String[0], new String[] { EPACKAGE_TYPE });

		action.onEnter(context(SOURCE_REGISTRY, "draft", OBJECT_ID, EPACKAGE_TYPE, Map.of()));
		String here = scope.writtenId.get();
		action.onEnter(context("configuration", "draft", OBJECT_ID, EPACKAGE_TYPE, Map.of()));

		assertNotEquals(here, scope.writtenId.get());
	}

	/* ------------------------------------------------------------------ create or replace */

	@Test
	@DisplayName("nothing stored yet means an upload, and the fingerprint is the version")
	void firstWriteIsAnUpload() {
		var action = action(new String[0], new String[] { EPACKAGE_TYPE });

		action.onEnter(ePackageContext("draft"));

		assertEquals("created", scope.outcome.get());
		assertEquals(FINGERPRINT, scope.writtenVersion.get(),
				"a fingerprint names the revision the asset reflects exactly");
		assertEquals(EcoreUtil.getURI(ModelAtlasAssetPackage.Literals.SCHEMA_ASSET).toString(),
				scope.writtenType.get(), "the metadata records what the asset is, not its supertype");
	}

	@Test
	@DisplayName("an asset this scope already holds is updated rather than uploaded again")
	void secondWriteIsAnUpdate() {
		var action = action(new String[0], new String[] { EPACKAGE_TYPE });
		scope.holds("asset_jena_schema_draft_a-package", "the-inventory-scope");

		action.onUpdate(ePackageContext("draft"));

		assertEquals("updated", scope.outcome.get());
	}

	@Test
	@DisplayName("an asset visible only through the parent scope is uploaded here, not updated")
	void inheritedAssetsAreNotUpdatedInPlace() {
		var action = action(new String[0], new String[] { EPACKAGE_TYPE });
		// what getMetadataFromStageForRegistry returns when this stage holds nothing and the
		// parent's final stage does: updateInStage would then read a null metadata locally
		scope.holds("asset_jena_schema_draft_a-package", "a-parent-scope");

		action.onEnter(ePackageContext("draft"));

		assertEquals("created", scope.outcome.get());
	}

	@Test
	@DisplayName("an object with no fingerprint still gets a version")
	void versionWithoutAFingerprint() {
		var action = action(new String[0], new String[] { EPACKAGE_TYPE });

		action.onEnter(context(SOURCE_REGISTRY, "draft", OBJECT_ID, EPACKAGE_TYPE, Map.of()));

		assertEquals("1.0", scope.writtenVersion.get());
	}

	/* ------------------------------------------------------------------ leaving a stage */

	@Test
	@DisplayName("leaving a stage removes that stage's asset, by the same address it was written under")
	void exitRemovesTheAsset() {
		var action = action(new String[0], new String[] { EPACKAGE_TYPE });

		action.onEnter(ePackageContext("draft"));
		String written = scope.writtenId.get();
		action.onExit(exitContext("draft"));

		assertEquals(List.of(INVENTORY_STAGE + "/" + written), scope.deleted);
	}

	/* ------------------------------------------------------------------ the loop guard */

	@Test
	@DisplayName("an event from the inventory it writes into is ignored, so the action cannot feed itself")
	void eventsFromTheInventoryAreIgnored() {
		var action = action(new String[0],
				new String[] { EcoreUtil.getURI(ModelAtlasAssetPackage.Literals.SCHEMA_ASSET).toString() });

		action.onEnter(fromInventory("asset_jena_schema_draft_a-package", null));
		action.onExit(fromInventory("asset_jena_schema_draft_a-package", ExitReason.DELETED));

		assertNull(scope.written.get(), "writing an asset raises ENTER on the inventory registry in turn");
		assertTrue(scope.deleted.isEmpty());
	}

	@Test
	@DisplayName("the same registry name in another scope is not this action's inventory")
	void theGuardIsScopedToo() {
		var action = action(new String[0], new String[] { EPACKAGE_TYPE });

		action.onEnter(context(INVENTORY_REGISTRY, "draft", OBJECT_ID, EPACKAGE_TYPE, Map.of()));

		assertEquals("created", scope.outcome.get(),
				"the event comes from another scope's registry that merely shares the name");
	}

	/* ------------------------------------------------------------------ failure */

	@Test
	@DisplayName("a write that fails fails this action's promise instead of throwing out of the dispatch")
	void aFailedWriteIsAFailedPromise() {
		var action = action(new String[0], new String[] { EPACKAGE_TYPE });
		scope.failWith(new IllegalStateException("storage is down"));

		Promise<Void> result = action.onEnter(ePackageContext("draft"));

		assertTrue(result.isDone());
		Throwable failure = failureOf(result);
		assertInstanceOf(IllegalStateException.class, failure);
		assertTrue(failure.getMessage().contains("storage is down"), failure.getMessage());
	}

	@Test
	@DisplayName("a removal that fails does the same")
	void aFailedRemovalIsAFailedPromise() {
		var action = action(new String[0], new String[] { EPACKAGE_TYPE });
		scope.failRemovalWith(new IllegalStateException("storage is down"));

		Promise<Void> result = action.onExit(exitContext("draft"));

		assertInstanceOf(IllegalStateException.class, failureOf(result));
	}

	private static Throwable failureOf(Promise<Void> promise) {
		try {
			return promise.getFailure();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			throw new IllegalStateException(e);
		}
	}

	/* ------------------------------------------------------------------ fixtures */

	private AssetStageActionService action(String[] triggers, String[] objectTypes) {
		return new AssetStageActionService(scope.service(), config(triggers, objectTypes));
	}

	private static AssetStageActionService.Config config(String[] triggers, String[] objectTypes) {
		Map<String, Object> values = new LinkedHashMap<>();
		values.put("triggers", triggers);
		values.put("supported_object_types", objectTypes);
		values.put("inventory_registry", INVENTORY_REGISTRY);
		values.put("inventory_stage", INVENTORY_STAGE);
		return proxy(AssetStageActionService.Config.class, values);
	}

	/** An EPackage event, the way the workflow raises one: fingerprint and nsUri in the metadata. */
	private static ActionContext ePackageContext(String stage) {
		return context(SOURCE_REGISTRY, stage, OBJECT_ID, EPACKAGE_TYPE,
				Map.of("fingerprint", FINGERPRINT, "nsUri", NS_URI));
	}

	private static ActionContext context(String registry, String stage, String objectId, String objectType,
			Map<String, Object> metadata) {
		return new ActionContext(SOURCE_SCOPE, registry, objectId, objectType, stage, null, null, null, "someone",
				Instant.now(), null, false, metadata);
	}

	/** An event raised by the inventory registry itself, in the scope the action writes into. */
	private static ActionContext fromInventory(String objectId, ExitReason exitReason) {
		return new ActionContext(Scope.SCOPE_NAME, INVENTORY_REGISTRY, objectId,
				EcoreUtil.getURI(ModelAtlasAssetPackage.Literals.SCHEMA_ASSET).toString(), INVENTORY_STAGE, null, null,
				exitReason, "system", Instant.now(), null, false, Map.of());
	}

	private static ActionContext exitContext(String stage) {
		return new ActionContext(SOURCE_SCOPE, SOURCE_REGISTRY, OBJECT_ID, EPACKAGE_TYPE, stage, null, null,
				ExitReason.DELETED, "someone", Instant.now(), null, false, Map.of());
	}

	/* ------------------------------------------------------------------ doubles */

	/**
	 * The inventory side of the world: it records the one asset written and the ids removed, and
	 * can be told to hold an asset already - in this scope or, as an inherited one, in another.
	 *
	 * <p>
	 * Written as a proxy rather than as an implementation of the whole writable contract: the
	 * action uses four of its methods, and anything else being called throws, which is the same
	 * guarantee a hand-written stub would give.
	 * </p>
	 */
	static class Scope {

		static final String SCOPE_NAME = "the-inventory-scope";

		final AtomicReference<StageableAsset> written = new AtomicReference<>();
		final AtomicReference<String> writtenId = new AtomicReference<>();
		final AtomicReference<String> writtenVersion = new AtomicReference<>();
		final AtomicReference<String> writtenType = new AtomicReference<>();
		final AtomicReference<String> outcome = new AtomicReference<>();
		final List<String> deleted = new ArrayList<>();

		private ObjectMetadata existing;
		private RuntimeException writeFailure;
		private RuntimeException removalFailure;

		String getScopeName() {
			return SCOPE_NAME;
		}

		/** Makes the lookup answer as if an asset were stored, in {@code scopeName}. */
		void holds(String objectId, String scopeName) {
			ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
			metadata.setObjectId(objectId);
			metadata.setScope(scopeName);
			existing = metadata;
		}

		void failWith(RuntimeException failure) {
			this.writeFailure = failure;
		}

		void failRemovalWith(RuntimeException failure) {
			this.removalFailure = failure;
		}

		@SuppressWarnings("unchecked")
		WritableScopeService<EObject> service() {
			InvocationHandler handler = (p, method, args) -> switch (method.getName()) {
			case "getScopeName" -> SCOPE_NAME;
			case "getMetadataFromStageForRegistry" -> existing;
			case "uploadToStageForRegistry" -> record("created", (EObject) args[2], (ObjectMetadata) args[3]);
			case "updateInStageForRegistry" -> record("updated", (EObject) args[2], (String) args[3], (String) args[4]);
			case "deleteFromStageForRegistry" -> remove((String) args[1], (String) args[2]);
			case "toString" -> "Scope[" + SCOPE_NAME + "]";
			case "hashCode" -> System.identityHashCode(this);
			case "equals" -> this == args[0];
			default -> throw new UnsupportedOperationException(method.getName());
			};
			return (WritableScopeService<EObject>) Proxy.newProxyInstance(
					AssetStageActionServiceTest.class.getClassLoader(),
					new Class<?>[] { WritableScopeService.class }, handler);
		}

		private Promise<ObjectMetadata> record(String how, EObject object, ObjectMetadata metadata) {
			writtenType.set(metadata.getObjectType());
			return record(how, object, metadata.getObjectId(), metadata.getVersion());
		}

		private Promise<ObjectMetadata> record(String how, EObject object, String objectId, String version) {
			if (writeFailure != null) {
				return Promises.failed(writeFailure);
			}
			outcome.set(how);
			written.set((StageableAsset) object);
			writtenId.set(objectId);
			writtenVersion.set(version);
			return Promises.resolved(ManagementFactory.eINSTANCE.createObjectMetadata());
		}

		private Promise<Boolean> remove(String stage, String objectId) {
			if (removalFailure != null) {
				return Promises.failed(removalFailure);
			}
			deleted.add(stage + "/" + objectId);
			return Promises.resolved(Boolean.TRUE);
		}
	}

	@SuppressWarnings("unchecked")
	private static <T> T proxy(Class<T> type, Map<String, Object> values) {
		InvocationHandler handler = (p, method, args) -> {
			if ("annotationType".equals(method.getName())) {
				return type;
			}
			return values.get(method.getName());
		};
		return (T) Proxy.newProxyInstance(AssetStageActionServiceTest.class.getClassLoader(),
				new Class<?>[] { type }, handler);
	}
}
