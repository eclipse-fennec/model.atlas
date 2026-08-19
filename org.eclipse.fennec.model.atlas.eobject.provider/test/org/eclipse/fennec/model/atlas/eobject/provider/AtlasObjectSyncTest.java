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
package org.eclipse.fennec.model.atlas.eobject.provider;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.emf.osgi.eobject.registry.EObjectRegistryEntry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Plain-JUnit tests of the sync engine against an in-memory scope service and a
 * recording writer - the semantics that were previously only covered indirectly through
 * the Felix IT.
 */
class AtlasObjectSyncTest {

	private static final String NS_URI = "http://fennec.eclipse.org/test/atlas-provider/1.0";

	private FakeScopeService scope;
	private RecordingWriter writer;
	private DeterministicScheduler scheduler;
	private EPackage testPackage;
	private EClass thingClass;
	private AtlasObjectSync sync;

	@BeforeEach
	void setUp() {
		scope = new FakeScopeService("iot");
		writer = new RecordingWriter();
		scheduler = new DeterministicScheduler();
		testPackage = EcoreFactory.eINSTANCE.createEPackage();
		testPackage.setName("test");
		testPackage.setNsPrefix("test");
		testPackage.setNsURI(NS_URI);
		thingClass = EcoreFactory.eINSTANCE.createEClass();
		thingClass.setName("Thing");
		EAttribute mid = EcoreFactory.eINSTANCE.createEAttribute();
		mid.setName("mid");
		mid.setEType(EcorePackage.Literals.ESTRING);
		thingClass.getEStructuralFeatures().add(mid);
		testPackage.getEClassifiers().add(thingClass);
	}

	@AfterEach
	void tearDown() {
		if (sync != null) {
			sync.close();
		}
		EPackage.Registry.INSTANCE.remove(NS_URI);
	}

	private EObject thing(String mid) {
		EObject thing = EcoreUtil.create(thingClass);
		if (mid != null) {
			thing.eSet(thingClass.getEStructuralFeature("mid"), mid);
		}
		return thing;
	}

	private AtlasSyncSettings settings(List<String> registries) {
		return settings(registries, List.of(), "", Set.of());
	}

	private AtlasSyncSettings settings(List<String> registries, List<String> objectIds, String stage,
			Set<String> requiredNsUris) {
		return new AtlasSyncSettings("prov", registries, objectIds, stage, requiredNsUris, 60_000, 30_000, null);
	}

	private void start(AtlasSyncSettings settings) {
		sync = new AtlasObjectSync(scope, settings, AtlasObjectSync.objectIdKeys(), writer, scheduler);
		scheduler.runPending();
	}

	// --- complete passes ---

	@Test
	void completePassSyncsEachRegistryUnderItsOwnSourceTag() {
		scope.put("mappings", "a", thing("a-mid")).put("profiles", "p", thing("p-mid"));

		start(settings(List.of("mappings", "profiles")));

		assertThat(writer.ops).extracting(RecordingWriter.Op::kind)
				.containsExactly(RecordingWriter.Kind.SYNC, RecordingWriter.Kind.SYNC);
		assertThat(writer.ops).extracting(RecordingWriter.Op::source).containsExactly("prov:mappings",
				"prov:profiles");
		assertThat(writer.ops.get(0).entries()).extracting(EObjectRegistryEntry::key).containsExactly("a");
		assertThat(writer.ops.get(1).entries()).extracting(EObjectRegistryEntry::key).containsExactly("p");
	}

	@Test
	void entriesCarryAtlasAndModelProperties() {
		scope.put("mappings", "a", thing("a-mid"));

		start(settings(List.of("mappings")));

		EObjectRegistryEntry entry = writer.ops(RecordingWriter.Kind.SYNC).get(0).entries().get(0);
		assertThat(entry.source()).isEqualTo("prov:mappings");
		assertThat(entry.properties()).containsEntry("atlas.remote", Boolean.TRUE)
				.containsEntry("atlas.scope", "iot").containsEntry("atlas.registry", "mappings")
				.containsEntry(AtlasObjectSync.PROP_OBJECT_ID, "a")
				.containsEntry(AtlasObjectSync.PROP_NS_URI, NS_URI).doesNotContainKey("atlas.stage");
	}

	@Test
	void completePassSchedulesRefreshOnlyWhenConfigured() {
		scope.put("mappings", "a", thing("a-mid"));

		sync = new AtlasObjectSync(scope, new AtlasSyncSettings("prov", List.of("mappings"), null, null, null, 0,
				30_000, null), AtlasObjectSync.objectIdKeys(), writer, scheduler);
		scheduler.runPending();

		assertThat(writer.ops(RecordingWriter.Kind.SYNC)).hasSize(1);
		assertThat(scheduler.scheduled).isEmpty();
	}

	// --- partial passes keep state ---

	@Test
	void fetchFailurePushesFetchedObjectsWithoutRemovals() {
		scope.put("mappings", "a", thing("a-mid")).put("mappings", "b", thing("b-mid"));
		scope.failObjects.add("b");

		start(settings(List.of("mappings")));

		assertThat(writer.ops(RecordingWriter.Kind.SYNC)).isEmpty();
		assertThat(writer.ops(RecordingWriter.Kind.REMOVE)).isEmpty();
		List<RecordingWriter.Op> puts = writer.ops(RecordingWriter.Kind.PUT);
		assertThat(puts).hasSize(1);
		assertThat(puts.get(0).key()).isEqualTo("a");
		assertThat(puts.get(0).source()).isEqualTo("prov:mappings");
		// incomplete pass -> retry scheduled, no refresh yet
		assertThat(scheduler.scheduled).hasSize(1);
		assertThat(scheduler.scheduled.get(0).periodic()).isFalse();
	}

	@Test
	void missingObjectMakesPassIncomplete() {
		scope.put("mappings", "a", thing("a-mid"));
		scope.missingObjects.add("gone");

		start(settings(List.of("mappings"), List.of("a", "gone"), "", Set.of()));

		assertThat(writer.ops(RecordingWriter.Kind.SYNC)).isEmpty();
		assertThat(writer.ops(RecordingWriter.Kind.PUT)).extracting(RecordingWriter.Op::key).containsExactly("a");
		assertThat(scheduler.scheduled).singleElement().satisfies(t -> assertThat(t.periodic()).isFalse());
	}

	@Test
	void listingFailureWritesNothingForThatRegistryButSyncsOthers() {
		scope.put("mappings", "a", thing("a-mid")).put("profiles", "p", thing("p-mid"));
		scope.failListing.add("mappings");

		start(settings(List.of("mappings", "profiles")));

		assertThat(writer.ops).hasSize(1);
		assertThat(writer.ops.get(0).kind()).isEqualTo(RecordingWriter.Kind.SYNC);
		assertThat(writer.ops.get(0).source()).isEqualTo("prov:profiles");
	}

	// --- selection ---

	@Test
	void explicitObjectIdsFetchOnlyThose() {
		scope.put("mappings", "a", thing("a-mid")).put("mappings", "b", thing("b-mid"));

		start(settings(List.of("mappings"), List.of("b"), "", Set.of()));

		List<EObjectRegistryEntry> entries = writer.ops(RecordingWriter.Kind.SYNC).get(0).entries();
		assertThat(entries).extracting(EObjectRegistryEntry::key).containsExactly("b");
	}

	@Test
	void stageSelectsStagedViewAndStampsEntries() {
		scope.put("mappings", "a", thing("a-mid"));

		start(settings(List.of("mappings"), List.of(), "draft", Set.of()));

		assertThat(scope.stagedViewRequests).containsExactly("mappings@draft");
		EObjectRegistryEntry entry = writer.ops(RecordingWriter.Kind.SYNC).get(0).entries().get(0);
		assertThat(entry.properties()).containsEntry("atlas.stage", "draft");
	}

	// --- key derivation ---

	@Test
	void featureKeysDeriveKeysAndSkipObjectsWithout() {
		scope.put("mappings", "a", thing("a-mid")).put("mappings", "b", thing(null));

		sync = new AtlasObjectSync(scope, settings(List.of("mappings")), AtlasObjectSync.featureKeys("mid"), writer,
				scheduler);
		scheduler.runPending();

		// the keyless object is skipped, the pass still counts as complete
		List<RecordingWriter.Op> syncs = writer.ops(RecordingWriter.Kind.SYNC);
		assertThat(syncs).hasSize(1);
		assertThat(syncs.get(0).entries()).extracting(EObjectRegistryEntry::key).containsExactly("a-mid");
		assertThat(scheduler.scheduled).singleElement().satisfies(t -> assertThat(t.periodic()).isTrue());
	}

	@Test
	void keyCollisionLastWins() {
		scope.put("mappings", "first", thing("same")).put("mappings", "second", thing("same"));

		sync = new AtlasObjectSync(scope, settings(List.of("mappings")), AtlasObjectSync.featureKeys("mid"), writer,
				scheduler);
		scheduler.runPending();

		List<EObjectRegistryEntry> entries = writer.ops(RecordingWriter.Kind.SYNC).get(0).entries();
		assertThat(entries).hasSize(1);
		assertThat(entries.get(0).properties()).containsEntry(AtlasObjectSync.PROP_OBJECT_ID, "second");
	}

	// --- required-nsURI gate ---

	@Test
	void requiredNsUriPostponesPassUntilResolvedThenRePins() {
		scope.put("mappings", "a", thing("a-mid"));

		start(settings(List.of("mappings"), List.of(), "", Set.of(NS_URI)));

		// package not registered -> nothing fetched, nothing written, retry scheduled
		assertThat(writer.ops).isEmpty();
		assertThat(scheduler.scheduled).singleElement().satisfies(t -> assertThat(t.periodic()).isFalse());

		// package appears (the model bundle activated) -> retry completes the pass
		EPackage.Registry.INSTANCE.put(NS_URI, testPackage);
		scheduler.runScheduledOnce();
		assertThat(writer.ops(RecordingWriter.Kind.SYNC)).hasSize(1);
		assertThat(scheduler.scheduled).singleElement().satisfies(t -> assertThat(t.periodic()).isTrue());

		// package vanishes (model bundle refresh) -> the next pass re-pins the held instance
		EPackage.Registry.INSTANCE.remove(NS_URI);
		scheduler.runScheduledOnce();
		assertThat(EPackage.Registry.INSTANCE.get(NS_URI)).isSameAs(testPackage);
		assertThat(writer.ops(RecordingWriter.Kind.SYNC)).hasSize(2);
	}

	// --- scheduling transitions ---

	@Test
	void retryTransitionsToRefreshAfterCompletePass() {
		scope.put("mappings", "a", thing("a-mid"));
		scope.failListing.add("mappings");

		start(settings(List.of("mappings")));

		assertThat(scheduler.scheduled).singleElement().satisfies(t -> assertThat(t.periodic()).isFalse());

		// the atlas recovers -> the retry completes and schedules the periodic refresh
		scope.failListing.clear();
		scheduler.runScheduledOnce();
		assertThat(writer.ops(RecordingWriter.Kind.SYNC)).hasSize(1);
		assertThat(scheduler.scheduled).singleElement().satisfies(t -> assertThat(t.periodic()).isTrue());
	}

	@Test
	void refreshPicksUpChangesAndGoneObjects() {
		EObject a = thing("a-mid");
		scope.put("mappings", "a", a).put("mappings", "b", thing("b-mid"));

		start(settings(List.of("mappings")));
		assertThat(writer.ops(RecordingWriter.Kind.SYNC)).hasSize(1);

		// b disappears from the atlas; the refresh pass hands the writer the remainder -
		// removal of gone objects is the writer's per-source sync semantics
		scope.remove("mappings", "b");
		scheduler.runScheduledOnce();
		List<RecordingWriter.Op> syncs = writer.ops(RecordingWriter.Kind.SYNC);
		assertThat(syncs).hasSize(2);
		assertThat(syncs.get(1).entries()).extracting(EObjectRegistryEntry::key).containsExactly("a");
		assertThat(syncs.get(1).entries().get(0).object()).isSameAs(a);
	}

	// --- lifecycle ---

	@Test
	void closeClearsPendingWorkAndWritesNothing() {
		scope.put("mappings", "a", thing("a-mid"));

		sync = new AtlasObjectSync(scope, settings(List.of("mappings")), AtlasObjectSync.objectIdKeys(), writer,
				scheduler);
		sync.close();
		scheduler.runPending();
		scheduler.runScheduledOnce();

		assertThat(writer.ops).isEmpty();
	}

	// --- settings validation ---

	@Test
	void settingsRejectBlankProviderNameAndEmptyRegistries() {
		assertThatIllegalArgumentException().isThrownBy(
				() -> new AtlasSyncSettings(" ", List.of("r"), null, null, null, 0, 0, null));
		assertThatIllegalArgumentException()
				.isThrownBy(() -> new AtlasSyncSettings("prov", List.of(), null, null, null, 0, 0, null));
	}

	@Test
	void settingsDeriveThreadNameAndNormalizeNulls() {
		AtlasSyncSettings settings = new AtlasSyncSettings("prov", List.of("r"), null, null, null, 0, 0, null);
		assertThat(settings.threadName()).isEqualTo("atlas-eobject-provider-prov");
		assertThat(settings.objectIds()).isEmpty();
		assertThat(settings.stage()).isEmpty();
		assertThat(settings.requiredNsUris()).isEmpty();
	}
}
