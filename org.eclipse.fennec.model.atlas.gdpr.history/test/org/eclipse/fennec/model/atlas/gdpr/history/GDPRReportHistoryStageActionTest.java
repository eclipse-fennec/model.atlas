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
package org.eclipse.fennec.model.atlas.gdpr.history;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.fennec.model.atlas.action.api.ActionContext;
import org.eclipse.fennec.model.atlas.action.api.StageActionService.ActionEvent;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView;
import org.eclipse.fennec.model.atlas.wf.workflowapi.WritableScopeService;
import org.eclipse.fennec.model.gdprReport.ClassifierEvaluation;
import org.eclipse.fennec.model.gdprReport.GDPRReportFactory;
import org.eclipse.fennec.model.gdprReport.GDPRReportPackage;
import org.eclipse.fennec.model.gdprReport.GdprReport;
import org.eclipse.fennec.model.gdprReport.GdprReportOrigin;
import org.eclipse.fennec.model.gdprReport.LegalCorpusRef;
import org.eclipse.fennec.model.gdprReport.PackageSubject;
import org.eclipse.fennec.model.gdprReportHistory.GdprReportHistory;
import org.eclipse.fennec.model.gdprReportHistory.ReportRevision;
import org.junit.jupiter.api.DisplayName;
import org.osgi.util.promise.Promise;
import org.osgi.util.promise.Promises;
import org.junit.jupiter.api.Test;

/**
 * What the action decides, rather than what the builder computes: which events it answers, which
 * scope, and which reviews it gathers. The wiring itself belongs to an OSGi integration test.
 */
class GDPRReportHistoryStageActionTest {

	private static final GDPRReportFactory REPORTS = GDPRReportFactory.eINSTANCE;
	private static final String FINGERPRINT = "fp1:9f2c1ab7d4e85530";
	private static final String OTHER_FINGERPRINT = "fp1:0000deadbeef0000";

	private final Scope scope = new Scope();

	/* ------------------------------------------------------------------ the trap */

	@Test
	@DisplayName("the object type is matched as an EClass URI, never as a name")
	void objectTypeIsTheEClassUri() {
		var action = action(new String[] { "draft" }, new String[0]);

		assertTrue(action.supportsObjectType(
				EcoreUtil.getURI(GDPRReportPackage.Literals.GDPR_REPORT).toString()));
		assertFalse(action.supportsObjectType("GdprReport"),
				"the storage layer writes the EClass URI; matching the simple name would never fire");
		assertFalse(action.supportsObjectType("http://www.eclipse.org/emf/2002/Ecore#//EPackage"));
	}

	@Test
	@DisplayName("it answers for enter, update and exit, on the configured stages")
	void triggers() {
		var action = action(new String[] { "draft", "release" }, new String[0]);

		assertEquals(java.util.Set.of("draft", "release"), action.getTriggerStages());
		assertEquals(java.util.Set.of(ActionEvent.ENTER, ActionEvent.UPDATE, ActionEvent.EXIT),
				action.getTriggerEvents());
		assertTrue(action.requiresReplayOnStartup(), "a derived document cannot notice what it missed");
	}

	/* ------------------------------------------------------------------ the scope gate */

	@Test
	@DisplayName("with no scopes named it answers for every scope")
	void openByDefault() throws Exception {
		var action = action(new String[] { "draft" }, new String[0]);
		scope.put("draft", "gdpr-a", report("2026-09-15T08:12:00Z", FINGERPRINT));

		action.onEnter(context("anything", "draft", "gdpr-a"));

		assertTrue(scope.awaitWrite(), "a single-scope deployment needs no configuration");
	}

	@Test
	@DisplayName("a scope it was not configured for is ignored")
	void otherScopesAreNotAnsweredFor() throws Exception {
		var action = action(new String[] { "draft" }, new String[] { "jena" });
		scope.put("draft", "gdpr-a", report("2026-09-15T08:12:00Z", FINGERPRINT));

		action.onEnter(context("someone-elses-scope", "draft", "gdpr-a"));

		assertFalse(scope.awaitWrite(),
				"the workflow does not filter by scope, so the action has to");
	}

	/* ------------------------------------------------------------------ what it gathers */

	@Test
	@DisplayName("it gathers every stage, and only the reviews of the subject that changed")
	void gathersTheSubjectAcrossStages() throws Exception {
		var action = action(new String[] { "draft", "release" }, new String[0]);
		// A promoted review lives in 'release', a newer one in 'draft', and a third is about a
		// different model altogether.
		scope.put("release", "gdpr-old", report("2026-09-15T08:12:00Z", FINGERPRINT));
		scope.put("draft", "gdpr-new", report("2026-09-17T14:20:30Z", FINGERPRINT));
		scope.put("draft", "gdpr-unrelated", report("2026-09-16T09:00:00Z", OTHER_FINGERPRINT));

		action.onEnter(context("jena", "draft", "gdpr-new"));

		assertTrue(scope.awaitWrite());
		assertEquals("gdpr-history-fp1-9f2c1ab7d4e85530-en", scope.writtenId.get(),
				"one document per subject and language, addressable from the fingerprint and the language");
		assertEquals(FINGERPRINT, scope.writtenVersion.get());

		List<String> reviewed = scope.written.get().getRevisions().stream().map(ReportRevision::getReportId).toList();
		assertEquals(List.of("gdpr-old", "gdpr-new"), reviewed,
				"both reviews of the subject belong in the document, oldest first, and no other model's");
	}

	@Test
	@DisplayName("two languages of one subject become two documents, not one mixed revision list")
	void oneDocumentPerLanguage() throws Exception {
		var action = action(new String[] { "draft" }, new String[0]);
		scope.put("draft", "gdpr-en", report("2026-09-15T08:12:00Z", FINGERPRINT, "EN"));
		scope.put("draft", "gdpr-de", report("2026-09-17T14:20:30Z", FINGERPRINT, "DE"));

		action.onEnter(context("jena", "draft", "gdpr-de"));

		assertTrue(scope.awaitWrite());
		await(() -> scope.documents.size() == 2);
		assertEquals(Set.of("gdpr-history-fp1-9f2c1ab7d4e85530-en", "gdpr-history-fp1-9f2c1ab7d4e85530-de"),
				scope.documents.keySet(), "one document per language, both rebuilt although only DE fired");

		GdprReportHistory english = scope.documents.get("gdpr-history-fp1-9f2c1ab7d4e85530-en");
		GdprReportHistory german = scope.documents.get("gdpr-history-fp1-9f2c1ab7d4e85530-de");
		assertEquals("EN", english.getReportLanguage());
		assertEquals("DE", german.getReportLanguage());
		assertEquals(List.of("gdpr-en"),
				english.getRevisions().stream().map(ReportRevision::getReportId).toList(),
				"the German review is not a later revision of the English one");
		assertEquals(List.of("gdpr-de"),
				german.getRevisions().stream().map(ReportRevision::getReportId).toList());
	}

	@Test
	@DisplayName("a review that names no language is kept apart, not folded into a named one")
	void anUnlabelledReviewIsNotGuessedAt() throws Exception {
		var action = action(new String[] { "draft" }, new String[0]);
		scope.put("draft", "gdpr-en", report("2026-09-15T08:12:00Z", FINGERPRINT, "EN"));
		scope.put("draft", "gdpr-silent", report("2026-09-17T14:20:30Z", FINGERPRINT, null));

		action.onEnter(context("jena", "draft", "gdpr-silent"));

		assertTrue(scope.awaitWrite());
		await(() -> scope.documents.size() == 2);
		assertEquals(Set.of("gdpr-history-fp1-9f2c1ab7d4e85530-en", "gdpr-history-fp1-9f2c1ab7d4e85530-unknown"),
				scope.documents.keySet(),
				"an unlabelled review must not corrupt the diff of a language that is stated");
	}

	/** Waits briefly for a condition the action reaches on its background thread. */
	private static void await(java.util.function.BooleanSupplier condition) throws InterruptedException {
		for (int i = 0; i < 100 && !condition.getAsBoolean(); i++) {
			Thread.sleep(50);
		}
		assertTrue(condition.getAsBoolean(), "the action did not reach the expected state in time");
	}

	@Test
	@DisplayName("a report that is gone is not guessed about")
	void aDeletedReportIsNotGuessedAbout() throws Exception {
		var action = action(new String[] { "draft" }, new String[0]);

		action.onExit(context("jena", "draft", "gdpr-vanished"));

		assertFalse(scope.awaitWrite(),
				"nothing left to say which subject it was about, so nothing is rewritten");
	}

	@Test
	@DisplayName("a report with no subject fingerprint belongs to no document")
	void aReportWithoutASubjectIsSkipped() throws Exception {
		var action = action(new String[] { "draft" }, new String[0]);
		GdprReport orphan = REPORTS.createGdprReport();
		orphan.setGeneratedAt("2026-09-15T08:12:00Z");
		scope.put("draft", "gdpr-orphan", orphan);

		action.onEnter(context("jena", "draft", "gdpr-orphan"));

		assertFalse(scope.awaitWrite());
	}

	/* ------------------------------------------------------------------ how it writes */

	@Test
	@DisplayName("the first document is created")
	void firstDocumentIsCreated() throws Exception {
		var action = action(new String[] { "draft" }, new String[0]);
		scope.put("draft", "gdpr-a", report("2026-09-15T08:12:00Z", FINGERPRINT));

		action.onEnter(context("jena", "draft", "gdpr-a"));

		assertTrue(scope.awaitWrite());
		assertEquals("created", scope.outcome.get());
	}

	@Test
	@DisplayName("a document that is already there is replaced, not refused")
	void existingDocumentIsUpdated() throws Exception {
		// The live failure this guards: writing the second review of a subject through a create
		// call gets a 409 (or, in a final stage, a 403), and the document freezes at revision 1.
		scope.documentExists();
		var action = action(new String[] { "draft" }, new String[0]);
		scope.put("draft", "gdpr-a", report("2026-09-15T08:12:00Z", FINGERPRINT));
		scope.put("draft", "gdpr-b", report("2026-09-17T14:20:30Z", FINGERPRINT));

		action.onEnter(context("jena", "draft", "gdpr-b"));

		assertTrue(scope.awaitWrite());
		assertEquals("updated", scope.outcome.get());
		assertEquals(2, scope.written.get().getRevisionCount());
	}

	/* ------------------------------------------------------------------ fixtures */

	private GDPRReportHistoryStageAction action(String[] stages, String[] scopes) {
		GDPRReportHistoryStageAction action = new GDPRReportHistoryStageAction(scope);
		action.activate(config(stages, scopes));
		return action;
	}

	private static GDPRReportHistoryStageAction.Config config(String[] stages, String[] scopes) {
		Map<String, Object> values = new LinkedHashMap<>();
		values.put("reports_registry", "gdpr");
		values.put("report_stages", stages);
		values.put("trigger_scopes", scopes);
		values.put("scope_target", "(atlas.scope=jena)");
		values.put("document_registry", "gdprdoc");
		values.put("document_stage", "draft");
		return proxy(GDPRReportHistoryStageAction.Config.class, values);
	}

	private static ActionContext context(String scopeName, String stage, String objectId) {
		return new ActionContext(scopeName, "gdpr", objectId,
				EcoreUtil.getURI(GDPRReportPackage.Literals.GDPR_REPORT).toString(), stage, null, null, null,
				"someone", Instant.now(), null, false, Map.of());
	}

	private static GdprReport report(String generatedAt, String fingerprint) {
		return report(generatedAt, fingerprint, "EN");
	}

	private static GdprReport report(String generatedAt, String fingerprint, String language) {
		GdprReport report = REPORTS.createGdprReport();
		report.setGeneratedAt(generatedAt);
		report.setGeneratedBy("claude-opus-5");
		report.setOrigin(GdprReportOrigin.AI_AGENT);

		PackageSubject subject = REPORTS.createPackageSubject();
		subject.setName("clinic");
		subject.setNsURI("https://example.org/clinic/1.0.0");
		subject.setSubjectFingerprint(fingerprint);
		report.setSubject(subject);

		LegalCorpusRef corpus = REPORTS.createLegalCorpusRef();
		corpus.setCelex("32016R0679");
		corpus.setLanguage(language);
		report.setCorpus(corpus);

		ClassifierEvaluation classifier = REPORTS.createClassifierEvaluation();
		classifier.setId("Patient");
		classifier.setName("Patient");
		report.getEvaluation().add(classifier);
		return report;
	}

	/* ------------------------------------------------------------------ doubles */

	/**
	 * A scope whose views answer from a map, so a test can lay out stages by hand, and which
	 * records the one document written back.
	 */
	static class Scope implements WritableScopeService<EObject> {
		private final Map<String, Map<String, EObject>> byStage = new LinkedHashMap<>();

		final AtomicReference<String> writtenId = new AtomicReference<>();
		final AtomicReference<String> writtenVersion = new AtomicReference<>();
		final AtomicReference<String> outcome = new AtomicReference<>();
		final AtomicReference<GdprReportHistory> written = new AtomicReference<>();
		/** Every document written, by object id: a subject in two languages is two of them. */
		final Map<String, GdprReportHistory> documents = new ConcurrentHashMap<>();
		private final CountDownLatch stored = new CountDownLatch(1);
		private volatile ObjectMetadata existing;

		void put(String stage, String objectId, EObject object) {
			byStage.computeIfAbsent(stage, s -> new LinkedHashMap<>()).put(objectId, object);
		}

		/** Makes the document registry answer as if a document were already stored. */
		void documentExists() {
			ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
			metadata.setObjectId("gdpr-history-fp1-9f2c1ab7d4e85530-en");
			existing = metadata;
		}

		/** True if a document was written; false after a short wait if none was. */
		boolean awaitWrite() throws InterruptedException {
			return stored.await(5, TimeUnit.SECONDS);
		}

		private Promise<ObjectMetadata> record(String how, String objectId, String version, EObject object,
				ObjectMetadata metadata) {
			outcome.set(how);
			writtenId.set(objectId);
			writtenVersion.set(version);
			written.set((GdprReportHistory) object);
			documents.put(objectId, (GdprReportHistory) object);
			stored.countDown();
			return Promises.resolved(metadata);
		}

		@Override
		public ObjectMetadata getMetadataFromStageForRegistry(String registry, String stage, String objectId) {
			return existing;
		}

		@Override
		public Promise<ObjectMetadata> uploadToStageForRegistry(String registry, String stage, EObject object,
				ObjectMetadata metadata) {
			return record("created", metadata.getObjectId(), metadata.getVersion(), object, metadata);
		}

		@Override
		public Promise<ObjectMetadata> updateInStageForRegistry(String registry, String stage, EObject updatedObject,
				String objectId, String version) {
			return record("updated", objectId, version, updatedObject, existing);
		}

		/*
		 * The rest of the writable contract. The action uses three of its methods; anything else
		 * being called is a change in the action worth failing over, not worth stubbing for.
		 */

		@Override
		public org.eclipse.fennec.model.atlas.wf.workflowapi.Scope getScope() {
			throw new UnsupportedOperationException();
		}

		@Override
		public Promise<ObjectMetadata> updatePropertiesInStageForRegistry(String registry, String stage,
				String objectId, Map<String, Object> properties) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Promise<ObjectMetadata> updateDiagnosticsInStageForRegistry(String registry, String stage,
				String objectId, String producer, List<org.eclipse.fennec.model.atlas.mgmt.management.Diagnostic> diagnostics) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ObjectMetadata getMetadataFromFinalStageForRegistry(String registry, String objectId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<ObjectMetadata> getMetadataByPropertyFromStageForRegistry(String registry, String stage,
				String key, String value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<ObjectMetadata> getMetadataByPropertyFromFinalStageForRegistry(String registry, String key,
				String value) {
			throw new UnsupportedOperationException();
		}

		@Override
		public EObject getContentFromStageForRegistry(String registry, String stage, String objectId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Promise<Boolean> deleteFromStageForRegistry(String registry, String stage, String objectId) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Promise<Boolean> deleteFromStageForRegistry(String registry, String stage, String objectId,
				boolean force) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<ObjectMetadata> listInStageForRegistry(String registry, String stage) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<ObjectMetadata> listInStageForRegistryByName(String registry, String stage, String name) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<ObjectMetadata> listInFinalStageForRegistry(String registry) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<ObjectMetadata> listAllForRegistry(String registry) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ObjectMetadata transitionToStageForRegistry(String registry, String objectId, String fromStage,
				String toStage) {
			throw new UnsupportedOperationException();
		}

		@Override
		public ObjectMetadata transitionToStageForRegistry(String registry, String objectId, String fromStage,
				String toStage, boolean overwrite) {
			throw new UnsupportedOperationException();
		}

		@Override
		public boolean isValidRegistry(String registryName) {
			throw new UnsupportedOperationException();
		}

		@Override
		public List<String> getAllRegistries() {
			throw new UnsupportedOperationException();
		}

		@Override
		public String getScopeName() {
			return "jena";
		}

		@Override
		public boolean isInheritingFromParentScope() {
			return false;
		}

		@Override
		public Optional<EObject> get(String registry, String objectId) {
			return Optional.empty();
		}

		@Override
		public List<String> listObjectIds(String registry) {
			return List.of();
		}

		@Override
		public List<EObject> listAll(String registry) {
			return List.of();
		}

		@Override
		public java.util.stream.Stream<EObject> stream(String registry) {
			return java.util.stream.Stream.empty();
		}

		@Override
		public org.eclipse.fennec.model.atlas.scope.api.ScopeInfo getScopeInfo() {
			return null;
		}

		@Override
		public ReadableRegistryView<EObject> registryView(String registry) {
			return view(Map.of());
		}

		@Override
		public ReadableRegistryView<EObject> registryView(String registry, String stage) {
			return view(byStage.getOrDefault(stage, Map.of()));
		}

		private ReadableRegistryView<EObject> view(Map<String, EObject> objects) {
			Map<String, Object> answers = new LinkedHashMap<>();
			answers.put("getScopeName", "jena");
			answers.put("getRegistryName", "gdpr");
			answers.put("listObjectIds", new ArrayList<>(objects.keySet()));
			answers.put("listAll", new ArrayList<>(objects.values()));
			answers.put("stream", objects.values().stream());
			answers.put("__get", objects);
			return proxyView(answers);
		}
	}

	@SuppressWarnings("unchecked")
	private static ReadableRegistryView<EObject> proxyView(Map<String, Object> answers) {
		InvocationHandler handler = (p, method, args) -> {
			if ("get".equals(method.getName()) && args != null && args.length == 1) {
				return Optional.ofNullable(((Map<String, EObject>) answers.get("__get")).get(args[0]));
			}
			if ("stream".equals(method.getName())) {
				return ((List<EObject>) answers.get("listAll")).stream();
			}
			return answers.get(method.getName());
		};
		return (ReadableRegistryView<EObject>) Proxy.newProxyInstance(
				GDPRReportHistoryStageActionTest.class.getClassLoader(),
				new Class<?>[] { ReadableRegistryView.class }, handler);
	}

	@SuppressWarnings("unchecked")
	private static <T> T proxy(Class<T> type, Map<String, Object> values) {
		InvocationHandler handler = (p, method, args) -> {
			if ("annotationType".equals(method.getName())) {
				return type;
			}
			return values.get(method.getName());
		};
		return (T) Proxy.newProxyInstance(GDPRReportHistoryStageActionTest.class.getClassLoader(),
				new Class<?>[] { type }, handler);
	}
}
