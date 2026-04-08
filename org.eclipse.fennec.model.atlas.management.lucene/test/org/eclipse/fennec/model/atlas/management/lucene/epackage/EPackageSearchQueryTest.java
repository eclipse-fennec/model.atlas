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
package org.eclipse.fennec.model.atlas.management.lucene.epackage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Set;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link EPackageSearchQuery} builder.
 *
 * @author ilenia
 * @since Apr 8, 2026
 */
class EPackageSearchQueryTest {

	@Test
	void testCreateReturnsNewInstance() {
		EPackageSearchQuery query = EPackageSearchQuery.create();
		assertNotNull(query);
	}

	@Test
	void testDefaults() {
		EPackageSearchQuery query = EPackageSearchQuery.create().build();

		assertNull(query.getScopes());
		assertNull(query.getStage());
		assertNull(query.getNsUri());
		assertNull(query.getNsUriExact());
		assertNull(query.getName());
		assertNull(query.getNsPrefix());
		assertNull(query.getClassifier());
		assertNull(query.getFeatureName());
		assertNull(query.getFeatureType());
		assertNull(query.getFeatureNameTypePair());
		assertEquals(50, query.getLimit());
		assertEquals(0, query.getOffset());
	}

	@Test
	void testBuildReturnsSameInstance() {
		EPackageSearchQuery builder = EPackageSearchQuery.create();
		EPackageSearchQuery built = builder.build();
		assertSame(builder, built);
	}

	@Test
	void testFluentChaining() {
		Set<String> scopes = Set.of("tenant-a", "division-x");

		EPackageSearchQuery query = EPackageSearchQuery.create()
				.scopes(scopes)
				.stage("approved")
				.nsUri("sensors")
				.nsUriExact("http://example.com/sensors/1.0")
				.name("SensorModel")
				.nsPrefix("sensors")
				.classifier("Reading")
				.featureName("temperature")
				.featureType("EDouble")
				.featureNameTypePair("temperature:EDouble")
				.limit(20)
				.offset(10)
				.build();

		assertEquals(scopes, query.getScopes());
		assertEquals("approved", query.getStage());
		assertEquals("sensors", query.getNsUri());
		assertEquals("http://example.com/sensors/1.0", query.getNsUriExact());
		assertEquals("SensorModel", query.getName());
		assertEquals("sensors", query.getNsPrefix());
		assertEquals("Reading", query.getClassifier());
		assertEquals("temperature", query.getFeatureName());
		assertEquals("EDouble", query.getFeatureType());
		assertEquals("temperature:EDouble", query.getFeatureNameTypePair());
		assertEquals(20, query.getLimit());
		assertEquals(10, query.getOffset());
	}

	@Test
	void testSettersReturnSameInstance() {
		EPackageSearchQuery query = EPackageSearchQuery.create();

		assertSame(query, query.scopes(Set.of("a")));
		assertSame(query, query.stage("draft"));
		assertSame(query, query.nsUri("uri"));
		assertSame(query, query.nsUriExact("exact"));
		assertSame(query, query.name("name"));
		assertSame(query, query.nsPrefix("prefix"));
		assertSame(query, query.classifier("cls"));
		assertSame(query, query.featureName("feat"));
		assertSame(query, query.featureType("type"));
		assertSame(query, query.featureNameTypePair("feat:type"));
		assertSame(query, query.limit(10));
		assertSame(query, query.offset(5));
	}

	@Test
	void testOverwritePreviousValue() {
		EPackageSearchQuery query = EPackageSearchQuery.create()
				.classifier("First")
				.classifier("Second")
				.build();

		assertEquals("Second", query.getClassifier());
	}

	@Test
	void testScopesWithSingleValue() {
		EPackageSearchQuery query = EPackageSearchQuery.create()
				.scopes(Set.of("tenant-a"))
				.build();

		assertEquals(1, query.getScopes().size());
		assertEquals(true, query.getScopes().contains("tenant-a"));
	}

	@Test
	void testScopesWithMultipleValues() {
		Set<String> scopes = Set.of("tenant-a", "division-x", "atlas");
		EPackageSearchQuery query = EPackageSearchQuery.create()
				.scopes(scopes)
				.build();

		assertEquals(3, query.getScopes().size());
		assertEquals(scopes, query.getScopes());
	}

	@Test
	void testMinimalQuery() {
		EPackageSearchQuery query = EPackageSearchQuery.create()
				.scopes(Set.of("tenant-a"))
				.build();

		assertNotNull(query.getScopes());
		assertNull(query.getStage());
		assertNull(query.getNsUri());
		assertNull(query.getClassifier());
		assertEquals(50, query.getLimit());
		assertEquals(0, query.getOffset());
	}
}
