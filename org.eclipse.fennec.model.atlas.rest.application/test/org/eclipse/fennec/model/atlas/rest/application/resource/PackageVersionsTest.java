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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.osgi.framework.Version;

import jakarta.ws.rs.WebApplicationException;

/**
 * A package's version is <em>declared</em> — by the caller, or by the model itself — and only
 * read out of the nsURI as a last resort, where it must actually look like a version
 * (issue #180).
 *
 * @see <a href="https://github.com/eclipse-fennec/model.atlas/issues/180">issue #180</a>
 */
@DisplayName("PackageVersions — declared first, inferred last")
class PackageVersionsTest {

	@Nested
	@DisplayName("reading a version out of an nsURI")
	class FromNsUri {

		@Test
		@DisplayName("the last segment, when it looks like a version")
		void takesTheLastSegment() {
			assertEquals(new Version("1.0.0"), PackageVersions.fromNsUri("http://www.gme.org/datagen/1.0.0"));
			assertEquals(new Version("2.0"), PackageVersions.fromNsUri("http://example.org/1.0/model/2.0"));
		}

		@Test
		@DisplayName("a year, date or spec number is not a version")
		void aYearIsNotAVersion() {
			// Every one of these is a real-world nsURI, and each used to yield a major version
			// of 2002, 2001 and 20131001 respectively (finding F134).
			assertNull(PackageVersions.fromNsUri("http://www.eclipse.org/emf/2002/Ecore"));
			assertNull(PackageVersions.fromNsUri("http://www.w3.org/2001/XMLSchema"));
			assertNull(PackageVersions.fromNsUri("http://www.omg.org/spec/UML/20131001"));
		}

		@Test
		@DisplayName("a numeric host is not a version")
		void aNumericAuthorityIsNotAVersion() {
			// The authority used to be parsed after the segments and to overwrite them, so the
			// last octet of an IP outranked a genuine version segment.
			assertEquals(new Version("5.0"), PackageVersions.fromNsUri("http://10.2.3.4/model/5.0"));
			assertNull(PackageVersions.fromNsUri("http://192.168.0.1/model"));
		}

		@Test
		@DisplayName("only the last segment is considered")
		void ignoresMidPathVersions() {
			assertNull(PackageVersions.fromNsUri("http://example.org/1.0/model"));
		}

		@Test
		@DisplayName("a segment has to be version-shaped, not merely parseable")
		void requiresAVersionShape() {
			assertNull(PackageVersions.fromNsUri("http://example.org/model/v1"), "'v1' is not a version");
			assertNull(PackageVersions.fromNsUri("http://example.org/model/1"), "a bare major is not enough");
			assertNull(PackageVersions.fromNsUri("http://example.org/model"));
		}

		@Test
		@DisplayName("null and blank yield nothing")
		void nullOrBlank() {
			assertNull(PackageVersions.fromNsUri(null));
			assertNull(PackageVersions.fromNsUri("  "));
		}
	}

	@Nested
	@DisplayName("the version the model declares")
	class FromAnnotation {

		@Test
		@DisplayName("the Version annotation of the EPackage is honoured")
		void readsTheVersionAnnotation() {
			// The fennec convention that emf.osgi's code generator writes and reads:
			// EAnnotation source "Version", detail "value".
			EPackage ePackage = ePackage("http://example.org/model", "1.2.3");

			assertEquals("1.2.3", PackageVersions.resolve(ePackage, null, ePackage.getNsURI()));
		}

		@Test
		@DisplayName("it outranks anything the nsURI happens to contain")
		void beatsTheNsUri() {
			EPackage ePackage = ePackage("http://example.org/model/9.9.9", "1.2.3");

			assertEquals("1.2.3", PackageVersions.resolve(ePackage, null, ePackage.getNsURI()));
		}

		@Test
		@DisplayName("an absent or blank annotation is not a declaration")
		void absentAnnotationFallsThrough() {
			EPackage bare = ePackage("http://example.org/model/1.0.0", null);
			EPackage blank = ePackage("http://example.org/model/1.0.0", "  ");

			assertEquals("1.0.0", PackageVersions.resolve(bare, null, bare.getNsURI()));
			assertEquals("1.0.0", PackageVersions.resolve(blank, null, blank.getNsURI()));
		}

		@Test
		@DisplayName("an unparseable declaration is refused rather than ignored")
		void unparseableAnnotationIsRejected() {
			EPackage ePackage = ePackage("http://example.org/model/1.0.0", "not-a-version");

			WebApplicationException failure = assertThrows(WebApplicationException.class,
					() -> PackageVersions.resolve(ePackage, null, ePackage.getNsURI()));

			assertEquals(400, failure.getResponse().getStatus());
			assertTrue(String.valueOf(failure.getResponse().getEntity()).contains("not-a-version"),
					"the message must quote what the package declared");
		}
	}

	@Nested
	@DisplayName("what the caller asks for")
	class ExplicitParameter {

		@Test
		@DisplayName("an explicit version is never vetoed")
		void isNeverVetoed() {
			// The loud half of issue #180: a caller stating the package's real version was
			// rejected because a year in the nsURI outranked it.
			EPackage ecore = ePackage("http://www.eclipse.org/emf/2002/Ecore", null);
			assertEquals("1.0.0", PackageVersions.resolve(ecore, "1.0.0", ecore.getNsURI()));

			EPackage other = ePackage("http://example.org/model/1.0.0", null);
			assertEquals("2.0.0", PackageVersions.resolve(other, "2.0.0", other.getNsURI()),
					"a version outside the nsURI's major is the caller's business, not ours");
		}

		@Test
		@DisplayName("it outranks the model's own declaration")
		void beatsTheAnnotation() {
			EPackage ePackage = ePackage("http://example.org/model", "1.2.3");

			assertEquals("4.0.0", PackageVersions.resolve(ePackage, "4.0.0", ePackage.getNsURI()));
		}

		@Test
		@DisplayName("an unparseable parameter is a bad request")
		void unparseableIsRejected() {
			EPackage ePackage = ePackage("http://example.org/model/1.0.0", null);

			WebApplicationException failure = assertThrows(WebApplicationException.class,
					() -> PackageVersions.resolve(ePackage, "not-a-version", ePackage.getNsURI()));

			assertEquals(400, failure.getResponse().getStatus());
		}

		@Test
		@DisplayName("blank counts as absent")
		void blankIsAbsent() {
			EPackage ePackage = ePackage("http://example.org/model/1.0.0", null);

			assertEquals("1.0.0", PackageVersions.resolve(ePackage, "  ", ePackage.getNsURI()));
		}
	}

	@Test
	@DisplayName("nothing declared and nothing inferable is no version at all")
	void noVersionAnywhere() {
		EPackage ePackage = ePackage("http://example.org/model", null);

		assertNull(PackageVersions.resolve(ePackage, null, ePackage.getNsURI()));
	}

	@Test
	@DisplayName("a null package is tolerated — the nsURI still decides")
	void withoutAPackage() {
		assertEquals("1.0.0", PackageVersions.resolve(null, null, "http://example.org/model/1.0.0"));
		assertNull(PackageVersions.resolve(null, null, "http://www.eclipse.org/emf/2002/Ecore"));
	}

	private static EPackage ePackage(String nsUri, String declaredVersion) {
		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		ePackage.setName("model");
		ePackage.setNsURI(nsUri);
		if (declaredVersion != null) {
			EAnnotation annotation = EcoreFactory.eINSTANCE.createEAnnotation();
			annotation.setSource("Version");
			annotation.getDetails().put("value", declaredVersion);
			ePackage.getEAnnotations().add(annotation);
		}
		return ePackage;
	}
}
