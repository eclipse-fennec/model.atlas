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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Set;

import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.fennec.model.atlas.management.lucene.epackage.EPackageLuceneIndex.SearchHit;
import org.eclipse.fennec.model.atlas.management.lucene.epackage.EPackageLuceneIndex.SearchResult;
import org.eclipse.fennec.model.atlas.management.lucene.epackage.impl.EPackageLuceneIndexImpl;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Unit tests for {@link EPackageLuceneIndexImpl}.
 *
 * @author ilenia
 * @since Apr 8, 2026
 */
class EPackageLuceneIndexImplTest {

	@TempDir
	Path tempDir;

	private EPackageLuceneIndexImpl index;

	@BeforeEach
	void setUp() throws IOException {
		EPackageLuceneIndexImpl.Config config = mock(EPackageLuceneIndexImpl.Config.class);
		when(config.index_folder()).thenReturn(tempDir.resolve("epackage-index").toString());
		index = new EPackageLuceneIndexImpl(config);
	}

	@AfterEach
	void tearDown() throws Exception {
		if (index != null) {
			index.close();
		}
	}

	// -- index and search basics --

	@Test
	void testIndexAndSearchByNsUri() {
		index.index(
				createMetadata("obj-1", "tenant-a", "draft"),
				createSensorPackage());

		SearchResult result = index.search(EPackageSearchQuery.create()
				.scopes(Set.of("tenant-a"))
				.nsUri("sensors")
				.build());

		assertEquals(1, result.totalHits());
		assertEquals(1, result.hits().size());
		assertEquals("obj-1", result.hits().get(0).objectId());
	}

	@Test
	void testSearchByExactNsUri() {
		index.index(
				createMetadata("obj-1", "tenant-a", "draft"),
				createSensorPackage());

		SearchResult exactMatch = index.search(EPackageSearchQuery.create()
				.scopes(Set.of("tenant-a"))
				.nsUriExact("http://example.com/sensors/1.0")
				.build());
		assertEquals(1, exactMatch.totalHits());

		SearchResult noMatch = index.search(EPackageSearchQuery.create()
				.scopes(Set.of("tenant-a"))
				.nsUriExact("http://example.com/sensors")
				.build());
		assertEquals(0, noMatch.totalHits());
	}

	@Test
	void testSearchByPackageName() {
		index.index(
				createMetadata("obj-1", "tenant-a", "draft"),
				createSensorPackage());

		SearchResult result = index.search(EPackageSearchQuery.create()
				.scopes(Set.of("tenant-a"))
				.name("SensorModel")
				.build());

		assertEquals(1, result.totalHits());
	}

	@Test
	void testSearchByNsPrefix() {
		index.index(
				createMetadata("obj-1", "tenant-a", "draft"),
				createSensorPackage());

		SearchResult result = index.search(EPackageSearchQuery.create()
				.scopes(Set.of("tenant-a"))
				.nsPrefix("sensors")
				.build());

		assertEquals(1, result.totalHits());
	}

	@Test
	void testSearchByClassifierName() {
		index.index(
				createMetadata("obj-1", "tenant-a", "draft"),
				createSensorPackage());

		SearchResult result = index.search(EPackageSearchQuery.create()
				.scopes(Set.of("tenant-a"))
				.classifier("Reading")
				.build());

		assertEquals(1, result.totalHits());

		SearchResult noMatch = index.search(EPackageSearchQuery.create()
				.scopes(Set.of("tenant-a"))
				.classifier("NonExistent")
				.build());
		assertEquals(0, noMatch.totalHits());
	}

	@Test
	void testSearchByFeatureName() {
		index.index(
				createMetadata("obj-1", "tenant-a", "draft"),
				createSensorPackage());

		SearchResult result = index.search(EPackageSearchQuery.create()
				.scopes(Set.of("tenant-a"))
				.featureName("temperature")
				.build());

		assertEquals(1, result.totalHits());
	}

	@Test
	void testSearchByFeatureType() {
		index.index(
				createMetadata("obj-1", "tenant-a", "draft"),
				createSensorPackage());

		SearchResult result = index.search(EPackageSearchQuery.create()
				.scopes(Set.of("tenant-a"))
				.featureType("EDouble")
				.build());

		assertEquals(1, result.totalHits());
	}

	@Test
	void testSearchByFeatureNameTypePair() {
		index.index(
				createMetadata("obj-1", "tenant-a", "draft"),
				createSensorPackage());

		SearchResult result = index.search(EPackageSearchQuery.create()
				.scopes(Set.of("tenant-a"))
				.featureNameTypePair("temperature:EDouble")
				.build());

		assertEquals(1, result.totalHits());
	}

	@Test
	void testSearchByFeatureNameTypePairNoFalsePositive() {
		// SensorModel has temperature:EDouble and name:EString
		// Searching for temperature:EString should NOT match
		index.index(
				createMetadata("obj-1", "tenant-a", "draft"),
				createSensorPackage());

		SearchResult result = index.search(EPackageSearchQuery.create()
				.scopes(Set.of("tenant-a"))
				.featureNameTypePair("temperature:EString")
				.build());

		assertEquals(0, result.totalHits());
	}

	// -- scope and stage filtering --

	@Test
	void testSearchAcrossMultipleScopes() {
		index.index(
				createMetadata("obj-1", "tenant-a", "approved"),
				createSensorPackage());
		index.index(
				createMetadata("obj-2", "division-x", "approved"),
				createPersonPackage());

		SearchResult result = index.search(EPackageSearchQuery.create()
				.scopes(Set.of("tenant-a", "division-x"))
				.build());

		assertEquals(2, result.totalHits());
	}

	@Test
	void testSearchWithStageFilter() {
		index.index(
				createMetadata("obj-1", "tenant-a", "draft"),
				createSensorPackage());
		index.index(
				createMetadata("obj-2", "tenant-a", "approved"),
				createPersonPackage());

		SearchResult draftOnly = index.search(EPackageSearchQuery.create()
				.scopes(Set.of("tenant-a"))
				.stage("draft")
				.build());

		assertEquals(1, draftOnly.totalHits());
		assertEquals("obj-1", draftOnly.hits().get(0).objectId());
	}

	@Test
	void testSearchHitCarriesScopeAndStage() {
		index.index(
				createMetadata("obj-1", "tenant-a", "approved"),
				createSensorPackage());

		SearchResult result = index.search(EPackageSearchQuery.create()
				.scopes(Set.of("tenant-a"))
				.build());

		SearchHit hit = result.hits().get(0);
		assertEquals("obj-1", hit.objectId());
		assertEquals("tenant-a", hit.scope());
		assertEquals("approved", hit.stage());
	}

	// -- pagination --

	@Test
	void testSearchPaginationLimit() {
		for (int i = 0; i < 10; i++) {
			EPackage pkg = createSimplePackage("pkg" + i, "http://example.com/pkg" + i, "pkg" + i);
			index.index(createMetadata("obj-" + i, "tenant-a", "draft"), pkg);
		}

		SearchResult result = index.search(EPackageSearchQuery.create()
				.scopes(Set.of("tenant-a"))
				.limit(3)
				.build());

		assertEquals(3, result.hits().size());
		assertEquals(10, result.totalHits());
	}

	@Test
	void testSearchPaginationOffset() {
		for (int i = 0; i < 10; i++) {
			EPackage pkg = createSimplePackage("pkg" + i, "http://example.com/pkg" + i, "pkg" + i);
			index.index(createMetadata("obj-" + i, "tenant-a", "draft"), pkg);
		}

		SearchResult result = index.search(EPackageSearchQuery.create()
				.scopes(Set.of("tenant-a"))
				.limit(3)
				.offset(8)
				.build());

		assertEquals(2, result.hits().size()); // only 2 left after offset 8
		assertEquals(10, result.totalHits());
	}

	@Test
	void testSearchTotalHitsCount() {
		for (int i = 0; i < 5; i++) {
			EPackage pkg = createSimplePackage("pkg" + i, "http://example.com/pkg" + i, "pkg" + i);
			index.index(createMetadata("obj-" + i, "tenant-a", "draft"), pkg);
		}

		SearchResult result = index.search(EPackageSearchQuery.create()
				.scopes(Set.of("tenant-a"))
				.limit(2)
				.offset(0)
				.build());

		assertEquals(2, result.hits().size());
		assertEquals(5, result.totalHits());
	}

	// -- remove and update --

	@Test
	void testRemoveFromIndex() {
		index.index(
				createMetadata("obj-1", "tenant-a", "draft"),
				createSensorPackage());

		index.remove("obj-1");

		SearchResult result = index.search(EPackageSearchQuery.create()
				.scopes(Set.of("tenant-a"))
				.build());

		assertEquals(0, result.totalHits());
	}

	@Test
	void testUpdateReindexes() {
		index.index(
				createMetadata("obj-1", "tenant-a", "draft"),
				createSensorPackage());

		// Re-index same objectId with different package
		index.index(
				createMetadata("obj-1", "tenant-a", "draft"),
				createPersonPackage());

		// Should find Person, not Sensor
		SearchResult personResult = index.search(EPackageSearchQuery.create()
				.scopes(Set.of("tenant-a"))
				.classifier("Person")
				.build());
		assertEquals(1, personResult.totalHits());

		SearchResult sensorResult = index.search(EPackageSearchQuery.create()
				.scopes(Set.of("tenant-a"))
				.classifier("Reading")
				.build());
		assertEquals(0, sensorResult.totalHits());
	}

	// -- edge cases --

	@Test
	void testSearchNoResults() {
		SearchResult result = index.search(EPackageSearchQuery.create()
				.scopes(Set.of("tenant-a"))
				.classifier("DoesNotExist")
				.build());

		assertEquals(0, result.totalHits());
		assertTrue(result.hits().isEmpty());
	}

	@Test
	void testSearchKeepsAFilterItCannotParseAsAValue() {
		index.index(
				createMetadata("obj-1", "tenant-a", "draft"),
				createSensorPackage());

		// A search criterion is a value, not query syntax. Parsing it as a query string
		// used to drop the whole criterion whenever it was not valid Lucene syntax, so
		// this search answered with every package of the scope instead of none.
		SearchResult result = index.search(EPackageSearchQuery.create()
				.scopes(Set.of("tenant-a"))
				.nsUri("does-not-exist(")
				.build());

		assertEquals(0, result.totalHits());
	}

	@Test
	void testSearchWithNullQuery() {
		SearchResult result = index.search(null);

		assertEquals(0, result.totalHits());
		assertTrue(result.hits().isEmpty());
	}

	@Test
	void testCombinedFilters() {
		index.index(
				createMetadata("obj-1", "tenant-a", "approved"),
				createSensorPackage());
		index.index(
				createMetadata("obj-2", "tenant-a", "approved"),
				createPersonPackage());

		// Only SensorModel has classifier "Reading" AND feature type "EDouble"
		SearchResult result = index.search(EPackageSearchQuery.create()
				.scopes(Set.of("tenant-a"))
				.classifier("Reading")
				.featureType("EDouble")
				.build());

		assertEquals(1, result.totalHits());
		assertEquals("obj-1", result.hits().get(0).objectId());
	}

	@Test
	void testIndexNullMetadataThrows() {
		assertThrows(IllegalArgumentException.class,
				() -> index.index(null, createSensorPackage()));
	}

	@Test
	void testIndexNullEPackageThrows() {
		assertThrows(IllegalArgumentException.class,
				() -> index.index(createMetadata("obj-1", "tenant-a", "draft"), null));
	}

	@Test
	void testRemoveNonExistentIsNoOp() {
		index.remove("does-not-exist");
		// Should not throw
	}

	@Test
	void testSearchByReferenceType() {
		// PersonPackage has a reference "friends" of type Person
		index.index(
				createMetadata("obj-1", "tenant-a", "draft"),
				createPersonPackage());

		// Search by reference type only — should find the package
		SearchResult byType = index.search(EPackageSearchQuery.create()
				.scopes(Set.of("tenant-a"))
				.featureType("Person")
				.build());
		assertEquals(1, byType.totalHits());

		// Search by exact pair "friends:Person"
		SearchResult byPair = index.search(EPackageSearchQuery.create()
				.scopes(Set.of("tenant-a"))
				.featureNameTypePair("friends:Person")
				.build());
		assertEquals(1, byPair.totalHits());
	}

	// -- helper methods --

	private ObjectMetadata createMetadata(String objectId, String scope, String stage) {
		ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadata.setObjectId(objectId);
		metadata.setScope(scope);
		metadata.setStage(stage);
		return metadata;
	}

	/**
	 * Creates a SensorModel EPackage:
	 * <pre>
	 * SensorModel (nsUri: http://example.com/sensors/1.0, prefix: sensors)
	 *   - Reading (EClass)
	 *       - temperature : EDouble
	 *       - name : EString
	 *   - Sensor (EClass)
	 *       - id : EString
	 * </pre>
	 */
	private EPackage createSensorPackage() {
		EcoreFactory f = EcoreFactory.eINSTANCE;

		EPackage pkg = f.createEPackage();
		pkg.setName("SensorModel");
		pkg.setNsURI("http://example.com/sensors/1.0");
		pkg.setNsPrefix("sensors");

		EClass reading = f.createEClass();
		reading.setName("Reading");

		EAttribute temperature = f.createEAttribute();
		temperature.setName("temperature");
		temperature.setEType(EcorePackage.Literals.EDOUBLE);
		reading.getEStructuralFeatures().add(temperature);

		EAttribute readingName = f.createEAttribute();
		readingName.setName("name");
		readingName.setEType(EcorePackage.Literals.ESTRING);
		reading.getEStructuralFeatures().add(readingName);

		EClass sensor = f.createEClass();
		sensor.setName("Sensor");

		EAttribute sensorId = f.createEAttribute();
		sensorId.setName("id");
		sensorId.setEType(EcorePackage.Literals.ESTRING);
		sensor.getEStructuralFeatures().add(sensorId);

		pkg.getEClassifiers().add(reading);
		pkg.getEClassifiers().add(sensor);
		return pkg;
	}

	/**
	 * Creates a PersonModel EPackage:
	 * <pre>
	 * PersonModel (nsUri: http://example.com/person/1.0, prefix: person)
	 *   - Person (EClass)
	 *       - name : EString
	 *       - age : EInt
	 *       - friends : Person [0..*] (EReference)
	 * </pre>
	 */
	private EPackage createPersonPackage() {
		EcoreFactory f = EcoreFactory.eINSTANCE;

		EPackage pkg = f.createEPackage();
		pkg.setName("PersonModel");
		pkg.setNsURI("http://example.com/person/1.0");
		pkg.setNsPrefix("person");

		EClass person = f.createEClass();
		person.setName("Person");

		EAttribute personName = f.createEAttribute();
		personName.setName("name");
		personName.setEType(EcorePackage.Literals.ESTRING);
		person.getEStructuralFeatures().add(personName);

		EAttribute age = f.createEAttribute();
		age.setName("age");
		age.setEType(EcorePackage.Literals.EINT);
		person.getEStructuralFeatures().add(age);

		EReference friends = f.createEReference();
		friends.setName("friends");
		friends.setEType(person);
		friends.setUpperBound(-1);
		person.getEStructuralFeatures().add(friends);

		pkg.getEClassifiers().add(person);
		return pkg;
	}

	private EPackage createSimplePackage(String name, String nsUri, String nsPrefix) {
		EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
		pkg.setName(name);
		pkg.setNsURI(nsUri);
		pkg.setNsPrefix(nsPrefix);
		return pkg;
	}
}
