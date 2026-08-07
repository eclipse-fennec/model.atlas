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
package org.eclipse.fennec.model.atlas.rest.application.resource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.osgi.framework.Version;

import jakarta.ws.rs.WebApplicationException;

/**
 * Unit tests for the version handling extracted out of
 * {@code SchemaPackagesResource}, which could previously only be exercised
 * through an HTTP request against a running framework.
 */
class NsUriVersionsTest {

	@Test
	void testExtractVersionFromLastSegment() {
		assertEquals(new Version("1.0.0"), NsUriVersions.extractVersion("http://www.gme.org/datagen/1.0.0"));
	}

	@Test
	void testExtractVersionTakesTheLastOfSeveral() {
		assertEquals(new Version("2.0"), NsUriVersions.extractVersion("http://example.org/1.0/model/2.0"));
	}

	@Test
	void testExtractVersionWithoutOneReturnsNull() {
		assertNull(NsUriVersions.extractVersion("http://example.org/model"));
	}

	/**
	 * Documents current behaviour, <em>not</em> intent: {@code Version.parseVersion}
	 * accepts a bare number, so a year segment is read as a major version. Every one
	 * of these is a real-world nsURI. See finding F134 — the assertions here should
	 * be inverted when it is fixed.
	 */
	@Test
	void testAYearSegmentIsMisreadAsAVersion() {
		assertEquals(new Version("2002.0.0"), NsUriVersions.extractVersion("http://www.eclipse.org/emf/2002/Ecore"));
		assertEquals(new Version("2001.0.0"), NsUriVersions.extractVersion("http://www.w3.org/2001/XMLSchema"));
		assertEquals(new Version("20131001.0.0"), NsUriVersions.extractVersion("http://www.omg.org/spec/UML/20131001"));
	}

	/**
	 * The consequence of the above: a caller who states the package's real version
	 * is rejected, because the year outranks it. Also current behaviour, not intent.
	 */
	@Test
	void testAYearSegmentRejectsAnHonestVersionParameter() {
		WebApplicationException e = assertThrows(WebApplicationException.class,
				() -> NsUriVersions.resolveAndValidate("1.0.0", "http://www.eclipse.org/emf/2002/Ecore"));
		assertEquals(400, e.getResponse().getStatus());
	}

	@Test
	void testExtractVersionOfNullOrBlank() {
		assertNull(NsUriVersions.extractVersion(null));
		assertNull(NsUriVersions.extractVersion("  "));
	}

	@Test
	void testCompatibleWhenUriVersionIsNewerInTheSameMajor() {
		assertTrue(NsUriVersions.areCompatible(new Version("1.2.0"), new Version("1.3.0")));
		assertTrue(NsUriVersions.areCompatible(new Version("1.2.0"), new Version("1.2.1")));
		assertTrue(NsUriVersions.areCompatible(new Version("1.2.0"), new Version("1.2.0")));
	}

	@Test
	void testIncompatibleAcrossMajorsOrBackwards() {
		assertFalse(NsUriVersions.areCompatible(new Version("1.0.0"), new Version("2.0.0")));
		assertFalse(NsUriVersions.areCompatible(new Version("1.3.0"), new Version("1.2.0")));
		assertFalse(NsUriVersions.areCompatible(new Version("1.2.1"), new Version("1.2.0")));
	}

	@Test
	void testIncompatibleWhenEitherIsNull() {
		assertFalse(NsUriVersions.areCompatible(null, new Version("1.0.0")));
		assertFalse(NsUriVersions.areCompatible(new Version("1.0.0"), null));
	}

	@Test
	void testResolveFallsBackToTheUriVersion() {
		assertEquals("1.0.0", NsUriVersions.resolveAndValidate(null, "http://example.org/model/1.0.0"));
		assertEquals("1.0.0", NsUriVersions.resolveAndValidate("  ", "http://example.org/model/1.0.0"));
	}

	@Test
	void testResolveWithoutAnyVersionIsNull() {
		assertNull(NsUriVersions.resolveAndValidate(null, "http://example.org/model"));
	}

	@Test
	void testResolveKeepsACompatibleParameter() {
		assertEquals("1.2.0", NsUriVersions.resolveAndValidate("1.2.0", "http://example.org/model/1.3.0"));
	}

	@Test
	void testResolveKeepsTheParameterWhenTheUriHasNoVersion() {
		assertEquals("1.2.0", NsUriVersions.resolveAndValidate("1.2.0", "http://example.org/model"));
	}

	@Test
	void testResolveRejectsAnUnparseableParameter() {
		WebApplicationException e = assertThrows(WebApplicationException.class,
				() -> NsUriVersions.resolveAndValidate("not-a-version", "http://example.org/model/1.0.0"));
		assertEquals(400, e.getResponse().getStatus());
	}

	@Test
	void testResolveRejectsAnIncompatibleParameter() {
		WebApplicationException e = assertThrows(WebApplicationException.class,
				() -> NsUriVersions.resolveAndValidate("2.0.0", "http://example.org/model/1.0.0"));
		assertEquals(400, e.getResponse().getStatus());
	}
}
