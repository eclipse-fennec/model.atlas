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
package org.eclipse.fennec.model.atlas.management.git;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the {@code type.registry.map} parsing — the one piece of the
 * git storage service that is testable without an OSGi runtime. DS wiring
 * (config, GitService binding, helper creation) is covered by the G8 OSGi ITs.
 */
class EObjectGitStorageServiceTest {

	private static final String EPACKAGE_URI = "http://www.eclipse.org/emf/2002/Ecore#//EPackage";
	private static final String EOBJECT_URI = "http://www.eclipse.org/emf/2002/Ecore#//EObject";

	@Test
	void splitsOnLastColon_soUriColonsAreKept() {
		Map<String, String> map = EObjectGitStorageService.parseTypeToRegistry(new String[] {
				EPACKAGE_URI + ":schema",
				EOBJECT_URI + ":object"
		});
		assertEquals(2, map.size());
		assertEquals("schema", map.get(EPACKAGE_URI), "URI (with its own colons) kept intact");
		assertEquals("object", map.get(EOBJECT_URI));
	}

	@Test
	void trimsWhitespace() {
		Map<String, String> map = EObjectGitStorageService.parseTypeToRegistry(new String[] {
				"  " + EPACKAGE_URI + " : schema  "
		});
		assertEquals("schema", map.get(EPACKAGE_URI));
	}

	@Test
	void skipsBlankAndMalformedEntries() {
		Map<String, String> map = EObjectGitStorageService.parseTypeToRegistry(new String[] {
				"",
				"   ",
				"no-colon-here",          // no separator
				EPACKAGE_URI + ":",       // empty registry
				EPACKAGE_URI + ":schema"  // valid
		});
		assertEquals(1, map.size());
		assertEquals("schema", map.get(EPACKAGE_URI));
	}

	@Test
	void nullArray_yieldsEmptyMap() {
		assertTrue(EObjectGitStorageService.parseTypeToRegistry(null).isEmpty());
	}
}
