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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.util.EcoreUtil;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.model.atlas.action.api.ActionContext;
import org.eclipse.fennec.model.atlas.action.api.StageActionService.ActionEvent;
import org.eclipse.fennec.model.atlas.publisher.ObjectPublisher;
import org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView;
import org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService;
import org.eclipse.fennec.model.gdprReport.ClassifierEvaluation;
import org.eclipse.fennec.model.gdprReport.GDPRReportFactory;
import org.eclipse.fennec.model.gdprReport.GDPRReportPackage;
import org.eclipse.fennec.model.gdprReport.GdprReport;
import org.eclipse.fennec.model.gdprReport.GdprReportOrigin;
import org.eclipse.fennec.model.gdprReport.SubjectModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * What the action decides, rather than what the builder computes: which events it answers, which
 * scope, and which reviews it gathers. The wiring itself belongs to an OSGi integration test.
 */
class GDPRReportHistoryStageActionTest {

	private static final GDPRReportFactory REPORTS = GDPRReportFactory.eINSTANCE;
	private static final String FINGERPRINT = "fp1:9f2c1ab7d4e85530";
	private static final String OTHER_FINGERPRINT = "fp1:0000deadbeef0000";

	private final Publisher publisher = new Publisher();
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

		assertTrue(publisher.awaitPublish(), "a single-scope deployment needs no configuration");
	}

	@Test
	@DisplayName("a scope it was not configured for is ignored")
	void otherScopesAreNotAnsweredFor() throws Exception {
		var action = action(new String[] { "draft" }, new String[] { "jena" });
		scope.put("draft", "gdpr-a", report("2026-09-15T08:12:00Z", FINGERPRINT));

		action.onEnter(context("someone-elses-scope", "draft", "gdpr-a"));

		assertFalse(publisher.awaitPublish(),
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

		assertTrue(publisher.awaitPublish());
		assertEquals("gdpr-history-fp1-9f2c1ab7d4e85530", publisher.objectId.get(),
				"one document per subject, addressable from the fingerprint alone");
		assertEquals(FINGERPRINT, publisher.version.get());

		String body = publisher.body.get();
		assertTrue(body.contains("gdpr-old") && body.contains("gdpr-new"),
				"both reviews of the subject belong in the document");
		assertFalse(body.contains("gdpr-unrelated"), "another model's review does not");
	}

	@Test
	@DisplayName("a report that is gone is not guessed about")
	void aDeletedReportIsNotGuessedAbout() throws Exception {
		var action = action(new String[] { "draft" }, new String[0]);

		action.onExit(context("jena", "draft", "gdpr-vanished"));

		assertFalse(publisher.awaitPublish(),
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

		assertFalse(publisher.awaitPublish());
	}

	/* ------------------------------------------------------------------ fixtures */

	private GDPRReportHistoryStageAction action(String[] stages, String[] scopes) {
		ResourceSet json = new ResourceSetImpl();
		json.getResourceFactoryRegistry().getExtensionToFactoryMap().put("json", new XMIResourceFactoryImpl());

		GDPRReportHistoryStageAction action = new GDPRReportHistoryStageAction(publisher, scope, json);
		action.activate(config(stages, scopes));
		return action;
	}

	private static GDPRReportHistoryStageAction.Config config(String[] stages, String[] scopes) {
		Map<String, Object> values = new LinkedHashMap<>();
		values.put("reports_registry", "gdpr");
		values.put("report_stages", stages);
		values.put("trigger_scopes", scopes);
		values.put("scope_target", "(atlas.scope=jena)");
		values.put("publisher_target", "");
		return proxy(GDPRReportHistoryStageAction.Config.class, values);
	}

	private static ActionContext context(String scopeName, String stage, String objectId) {
		return new ActionContext(scopeName, "gdpr", objectId,
				EcoreUtil.getURI(GDPRReportPackage.Literals.GDPR_REPORT).toString(), stage, null, null, null,
				"someone", Instant.now(), null, false, Map.of());
	}

	private static GdprReport report(String generatedAt, String fingerprint) {
		GdprReport report = REPORTS.createGdprReport();
		report.setGeneratedAt(generatedAt);
		report.setGeneratedBy("claude-opus-5");
		report.setOrigin(GdprReportOrigin.AI_AGENT);

		SubjectModel subject = REPORTS.createSubjectModel();
		subject.setName("clinic");
		subject.setNsURI("https://example.org/clinic/1.0.0");
		subject.setModelFingerprint(fingerprint);
		report.setSubject(subject);

		ClassifierEvaluation classifier = REPORTS.createClassifierEvaluation();
		classifier.setId("Patient");
		classifier.setName("Patient");
		report.getClassifierEvaluation().add(classifier);
		return report;
	}

	/* ------------------------------------------------------------------ doubles */

	/** Captures one publication and lets a test wait for the rebuild thread. */
	static class Publisher implements ObjectPublisher {
		final AtomicReference<String> objectId = new AtomicReference<>();
		final AtomicReference<String> body = new AtomicReference<>();
		final AtomicReference<String> version = new AtomicReference<>();
		final CountDownLatch published = new CountDownLatch(1);

		@Override
		public Receipt publish(String objectId, String content, String name, String version) {
			this.objectId.set(objectId);
			this.body.set(content);
			this.version.set(version);
			published.countDown();
			return new Receipt("created", objectId, name, version, "jena", "gdprdoc", "draft",
					"application/json", content.length());
		}

		@Override
		public String contentType() {
			return "application/json";
		}

		/** True if a document was written; false after a short wait if none was. */
		boolean awaitPublish() throws InterruptedException {
			return published.await(5, TimeUnit.SECONDS);
		}
	}

	/** A scope whose views answer from a map, so a test can lay out stages by hand. */
	static class Scope implements ReadableScopeService<EObject> {
		private final Map<String, Map<String, EObject>> byStage = new LinkedHashMap<>();

		void put(String stage, String objectId, EObject object) {
			byStage.computeIfAbsent(stage, s -> new LinkedHashMap<>()).put(objectId, object);
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
