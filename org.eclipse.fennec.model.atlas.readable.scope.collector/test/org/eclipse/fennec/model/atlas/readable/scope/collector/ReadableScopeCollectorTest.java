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
package org.eclipse.fennec.model.atlas.readable.scope.collector;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.fennec.model.atlas.readable.scope.collector.impl.ReadableScopeCollectorImpl;
import org.eclipse.fennec.model.atlas.scope.api.AtlasProperties;
import org.eclipse.fennec.model.atlas.scope.api.ReadableScopeService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Unit tests for {@link ReadableScopeCollector}.
 *
 * The point of these tests is to pin the collector's one non-obvious invariant:
 * it keys on the {@code atlas.scope} service property ({@link AtlasProperties#ATLAS_SCOPE}),
 * not on the legacy {@code scope.name} property that feeds the workflow-side
 * {@code ScopeServiceCollector}. A {@link ReadableScopeService} that does not advertise
 * {@code atlas.scope} (e.g. a config block that forgot it) must not be collected.
 *
 * @author ilenia
 * @since Jun 11, 2026
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ReadableScopeCollector Unit Tests")
public class ReadableScopeCollectorTest {

	@Mock
	private ReadableScopeService<?> serviceA;

	@Mock
	private ReadableScopeService<?> serviceB;

	private ReadableScopeCollectorImpl collector;

	@BeforeEach
	void setUp() {
		collector = new ReadableScopeCollectorImpl();
	}

	private static Map<String, Object> props(String atlasScope) {
		Map<String, Object> props = new HashMap<>();
		if (atlasScope != null) {
			props.put(AtlasProperties.ATLAS_SCOPE, atlasScope);
		}
		return props;
	}

	/** A {@code scope.name} property without {@code atlas.scope} — the legacy shape. */
	private static Map<String, Object> legacyProps(String scopeName) {
		Map<String, Object> props = new HashMap<>();
		props.put("scope.name", scopeName);
		return props;
	}

	@Nested
	@DisplayName("Bind / Lookup Tests")
	class BindLookupTests {

		@Test
		@DisplayName("Should store and return a service bound with atlas.scope")
		void shouldStoreServiceBoundWithAtlasScope() {
			collector.bindScopeService(serviceA, props("jena"));

			assertSame(serviceA, collector.getScopeServiceByScopeName("jena"));
			assertTrue(collector.getAllScopeNames().contains("jena"));
		}

		@Test
		@DisplayName("Should key strictly on atlas.scope, ignoring scope.name")
		void shouldKeyOnAtlasScopeValue() {
			Map<String, Object> mixed = props("jena");
			mixed.put("scope.name", "something-else");
			collector.bindScopeService(serviceA, mixed);

			assertSame(serviceA, collector.getScopeServiceByScopeName("jena"));
			assertNull(collector.getScopeServiceByScopeName("something-else"));
		}

		@Test
		@DisplayName("Should keep distinct scopes independent")
		void shouldKeepDistinctScopesIndependent() {
			collector.bindScopeService(serviceA, props("atlas"));
			collector.bindScopeService(serviceB, props("jena"));

			assertSame(serviceA, collector.getScopeServiceByScopeName("atlas"));
			assertSame(serviceB, collector.getScopeServiceByScopeName("jena"));
			assertEquals(2, collector.getAllScopeNames().size());
		}

		@Test
		@DisplayName("Should return null when no service bound for a scope")
		void shouldReturnNullWhenNothingBound() {
			assertNull(collector.getScopeServiceByScopeName("jena"));
			assertTrue(collector.getAllScopeNames().isEmpty());
		}

		@Test
		@DisplayName("Should override when rebinding the same scope")
		void shouldOverrideOnRebind() {
			collector.bindScopeService(serviceA, props("jena"));
			collector.bindScopeService(serviceB, props("jena"));

			assertSame(serviceB, collector.getScopeServiceByScopeName("jena"));
			assertEquals(1, collector.getAllScopeNames().size());
		}
	}

	@Nested
	@DisplayName("Bind Validation Tests")
	class BindValidationTests {

		@Test
		@DisplayName("Should ignore bind when atlas.scope is missing")
		void shouldIgnoreBindWhenAtlasScopeMissing() {
			collector.bindScopeService(serviceA, props(null));

			assertTrue(collector.getAllScopeNames().isEmpty());
		}

		@Test
		@DisplayName("Should ignore bind when atlas.scope is empty")
		void shouldIgnoreBindWhenAtlasScopeEmpty() {
			collector.bindScopeService(serviceA, props(""));

			assertTrue(collector.getAllScopeNames().isEmpty());
		}

		@Test
		@DisplayName("Should ignore a legacy publication that only carries scope.name")
		void shouldIgnoreLegacyScopeNameOnlyPublication() {
			collector.bindScopeService(serviceA, legacyProps("jena"));

			assertNull(collector.getScopeServiceByScopeName("jena"));
			assertTrue(collector.getAllScopeNames().isEmpty());
		}
	}

	@Nested
	@DisplayName("Unbind Tests")
	class UnbindTests {

		@Test
		@DisplayName("Should remove the entry when unbinding")
		void shouldRemoveEntryOnUnbind() {
			collector.bindScopeService(serviceA, props("jena"));
			collector.unbindScopeService(serviceA, props("jena"));

			assertNull(collector.getScopeServiceByScopeName("jena"));
			assertTrue(collector.getAllScopeNames().isEmpty());
		}

		@Test
		@DisplayName("Should be a no-op when unbind properties carry no atlas.scope")
		void shouldBeNoOpWhenAtlasScopeMissingOnUnbind() {
			collector.bindScopeService(serviceA, props("jena"));
			collector.unbindScopeService(serviceA, props(null));
			collector.unbindScopeService(serviceA, props(""));

			assertSame(serviceA, collector.getScopeServiceByScopeName("jena"));
		}
	}
}
