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
package org.eclipse.fennec.model.atlas.registry.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.model.atlas.management.lucene.epackage.EPackageLuceneIndex;
import org.eclipse.fennec.model.atlas.mgmt.management.ManagementFactory;
import org.eclipse.fennec.model.atlas.mgmt.management.ObjectMetadata;
import org.eclipse.fennec.model.atlas.mgmt.registry.RegistryAddress;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

/**
 * Unit tests for {@link SearchIndex}: updates made while no index is bound are applied once one
 * is.
 */
class SearchIndexTest {

	private final SearchIndex searchIndex = new SearchIndex();

	@Test
	void updatesWhileBoundGoStraightToTheIndex() {
		EPackageLuceneIndex index = mock(EPackageLuceneIndex.class);
		searchIndex.bind(index);
		ObjectMetadata metadata = metadata("p1");
		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();

		searchIndex.index(metadata, ePackage);

		verify(index).index(metadata, ePackage);
	}

	@Test
	void updatesWithoutIndexAreAppliedOnBind() {
		ObjectMetadata first = metadata("p1");
		ObjectMetadata second = metadata("p2");
		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		searchIndex.index(first, ePackage);
		searchIndex.index(second, ePackage);

		EPackageLuceneIndex index = mock(EPackageLuceneIndex.class);
		searchIndex.bind(index);

		InOrder order = inOrder(index);
		order.verify(index).index(first, ePackage);
		order.verify(index).index(second, ePackage);
	}

	@Test
	void onlyTheLatestUpdatePerAddressIsApplied() {
		ObjectMetadata metadata = metadata("p1");
		searchIndex.index(metadata, EcoreFactory.eINSTANCE.createEPackage());
		searchIndex.remove(RegistryAddress.of(metadata));

		EPackageLuceneIndex index = mock(EPackageLuceneIndex.class);
		searchIndex.bind(index);

		verify(index, never()).index(any(), any());
		verify(index).remove(RegistryAddress.of(metadata));
	}

	@Test
	void heldUpdatesAreAppliedOnlyOnce() {
		searchIndex.index(metadata("p1"), EcoreFactory.eINSTANCE.createEPackage());
		EPackageLuceneIndex first = mock(EPackageLuceneIndex.class);
		searchIndex.bind(first);
		searchIndex.unbind(first);

		EPackageLuceneIndex second = mock(EPackageLuceneIndex.class);
		searchIndex.bind(second);

		verify(first, times(1)).index(any(), any());
		verifyNoInteractions(second);
	}

	@Test
	void holdsAtMostMaxPendingAddresses() {
		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		for (int i = 0; i <= SearchIndex.MAX_PENDING; i++) {
			searchIndex.index(metadata("p" + i), ePackage);
		}

		EPackageLuceneIndex index = mock(EPackageLuceneIndex.class);
		searchIndex.bind(index);

		verify(index, times(SearchIndex.MAX_PENDING)).index(any(), any());
	}

	private static ObjectMetadata metadata(String objectId) {
		ObjectMetadata metadata = ManagementFactory.eINSTANCE.createObjectMetadata();
		metadata.setScope("scope");
		metadata.setRegistry("schema");
		metadata.setStage("draft");
		metadata.setObjectId(objectId);
		return metadata;
	}
}
