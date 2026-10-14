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
package org.eclipse.fennec.model.atlas.compliance.analysis;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.fennec.model.atlas.compliance.analysis.RequirementCitations.Citation;
import org.eclipse.fennec.model.atlas.compliance.analysis.RequirementCitations.Outcome;
import org.eclipse.fennec.model.atlas.scope.api.ReadableRegistryView;
import org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService;
import org.eclipse.fennec.model.atlas.scope.api.ScopeInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Fetching the contexts an analyser cites against, once each.
 *
 * <p>
 * A {@code ComplianceContext} contains its corpora, so a fetch is 1.3 MB for {@code gdpr} and
 * 6.7 MB for {@code bsi-grundschutz-pp}. An analyser raising twenty findings against the same
 * three requirements must not pay that twenty times, which is the whole reason this class exists
 * on top of {@link RequirementCitations}.
 * </p>
 */
@DisplayName("The contexts an analyser cites against")
public class ComplianceContextsTest {

	@Test
	@DisplayName("the context is fetched once however often it is asked")
	public void fetchesOnce() {
		CountingScope scope = new CountingScope();
		ComplianceContexts contexts = new ComplianceContexts(scope, "compliance-context", null);

		for (int i = 0; i < 5; i++) {
			assertEquals(Outcome.FOUND, contexts.resolve("gdpr", "Art.32(1)(a)", "DE").outcome());
		}

		assertEquals(1, scope.gets, "a multi-megabyte containment tree re-read per finding is the one cost here");
		assertTrue(contexts.isCached("gdpr"));
	}

	@Test
	@DisplayName("two contexts are two fetches, and neither evicts the other")
	public void cachesPerContext() {
		CountingScope scope = new CountingScope();
		ComplianceContexts contexts = new ComplianceContexts(scope, "compliance-context", null);

		contexts.resolve("gdpr", "Art.32(1)(a)", "DE");
		contexts.resolve("iso27001-annex-a", "A.5.1", "EN");
		contexts.resolve("gdpr", "Art.5(1)(e)", "DE");

		assertEquals(2, scope.gets);
		assertSame(contexts.citations("gdpr").orElseThrow(), contexts.citations("gdpr").orElseThrow());
	}

	@Test
	@DisplayName("a context the registry does not hold is reported, and asked for again")
	public void missesAreNotCached() {
		CountingScope scope = new CountingScope();
		ComplianceContexts contexts = new ComplianceContexts(scope, "compliance-context", null);

		Citation first = contexts.resolve("nis2", "Art.21", "DE");
		Citation second = contexts.resolve("nis2", "Art.21", "DE");

		assertEquals(Outcome.UNKNOWN_CONTEXT, first.outcome(),
				"a context missing from the registry is a deployment fault; answering NOT_QUOTABLE would file it "
						+ "as a property of the law");
		assertEquals(Outcome.UNKNOWN_CONTEXT, second.outcome());
		assertTrue(first.evidence().isEmpty());
		assertFalse(contexts.isCached("nis2"));
		assertEquals(2, scope.gets, "seeding the context must fix the deployment without a restart");
	}

	@Test
	@DisplayName("a blank stage reads the scope's own view, a named one that stage")
	public void stageSelectsTheView() {
		CountingScope unstaged = new CountingScope();
		new ComplianceContexts(unstaged, "compliance-context", "  ").citations("gdpr");
		assertEquals(List.of("compliance-context"), unstaged.views);

		CountingScope staged = new CountingScope();
		new ComplianceContexts(staged, "compliance-context", "release").citations("gdpr");
		assertEquals(List.of("compliance-context@release"), staged.views);
	}

	@Test
	@DisplayName("a blank registry falls back to the seeded convention")
	public void defaultRegistry() {
		CountingScope scope = new CountingScope();
		new ComplianceContexts(scope, null, null).citations("gdpr");

		assertEquals(List.of(ComplianceContexts.DEFAULT_REGISTRY), scope.views);
	}

	/** A scope that serves the seeded contexts and counts what was asked of it. */
	private static final class CountingScope implements ReadableScopeService<EObject> {

		private final List<String> views = new java.util.ArrayList<>();
		private int gets;

		@Override
		public ReadableRegistryView<EObject> registryView(String registry) {
			views.add(registry);
			return new View(registry, null);
		}

		@Override
		public ReadableRegistryView<EObject> registryView(String registry, String stage) {
			views.add(registry + "@" + stage);
			return new View(registry, stage);
		}

		@Override
		public Optional<EObject> get(String registry, String objectId) {
			return registryView(registry).get(objectId);
		}

		@Override
		public String getScopeName() {
			return "dimcity";
		}

		@Override
		public boolean isInheritingFromParentScope() {
			return false;
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
		public Stream<EObject> stream(String registry) {
			return Stream.empty();
		}

		@Override
		public ScopeInfo getScopeInfo() {
			return null;
		}

		private final class View implements ReadableRegistryView<EObject> {

			private final String registry;
			private final String stage;

			private View(String registry, String stage) {
				this.registry = registry;
				this.stage = stage;
			}

			@Override
			public Optional<EObject> get(String objectId) {
				gets++;
				if (!"gdpr".equals(objectId) && !"iso27001-annex-a".equals(objectId)) {
					return Optional.empty();
				}
				return Optional.of(SeededContexts.get(objectId));
			}

			@Override
			public String getScopeName() {
				return CountingScope.this.getScopeName();
			}

			@Override
			public String getRegistryName() {
				return registry;
			}

			@Override
			public String getStageName() {
				return stage;
			}

			@Override
			public List<String> listObjectIds() {
				return List.of("gdpr", "iso27001-annex-a");
			}

			@Override
			public List<EObject> listAll() {
				return List.of();
			}

			@Override
			public Stream<EObject> stream() {
				return Stream.empty();
			}
		}
	}
}
