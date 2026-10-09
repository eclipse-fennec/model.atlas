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
package org.eclipse.fennec.model.atlas.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.junit.jupiter.api.Test;

/**
 * The version rule shared by the upload endpoint, the bootstrap loader and the atlas schema
 * registry (#180, #359).
 */
class EPackageVersionsTest {

	@Test
	void aDeclaredVersionWinsOverTheNsUri() {
		assertEquals("2.5.0", EPackageVersions.of(ePackage("http://example.org/model/9.9.9", "2.5.0"), unexpected()));
	}

	@Test
	void theNsUriIsReadWhenNothingIsDeclared() {
		assertEquals("1.0.0", EPackageVersions.of(ePackage("http://example.org/model/1.0.0", null), unexpected()));
		assertEquals("1.0.0", EPackageVersions.of(ePackage("http://www.eclipse.org/fennec/m2x/compiled/1.0", null),
				unexpected()));
	}

	@Test
	void yearsHostsAndBareNumbersAreNotVersions() {
		assertNull(EPackageVersions.of(ePackage("http://www.eclipse.org/emf/2002/Ecore", null), unexpected()));
		assertNull(EPackageVersions.of(ePackage("http://www.omg.org/spec/UML/20131001", null), unexpected()));
		assertNull(EPackageVersions.of(ePackage("http://example.org/model/1", null), unexpected()));
		assertNull(EPackageVersions.fromNsUri("http://10.2.3.4/model"));
	}

	@Test
	void anInvalidDeclarationIsReportedAndIgnored() {
		List<String> reported = new ArrayList<>();

		assertEquals("1.0.0",
				EPackageVersions.of(ePackage("http://example.org/model/1.0.0", "not-a-version"), reported::add));
		assertEquals(List.of("not-a-version"), reported);
	}

	@Test
	void declaredReturnsTheStatementUnvalidated() {
		assertEquals("not-a-version", EPackageVersions.declared(ePackage("http://example.org/x", " not-a-version ")));
		assertNull(EPackageVersions.declared(ePackage("http://example.org/x", null)));
		assertNull(EPackageVersions.declared(null));
		assertTrue(EPackageVersions.isVersion("1.2.3.qualifier"));
		assertFalse(EPackageVersions.isVersion("not-a-version"));
	}

	private static java.util.function.Consumer<String> unexpected() {
		return declared -> {
			throw new AssertionError("unexpected invalid declaration " + declared);
		};
	}

	private static EPackage ePackage(String nsUri, String declaredVersion) {
		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		ePackage.setName("test");
		ePackage.setNsURI(nsUri);
		if (declaredVersion != null) {
			EAnnotation annotation = EcoreFactory.eINSTANCE.createEAnnotation();
			annotation.setSource(EPackageVersions.VERSION_ANNOTATION_SOURCE);
			annotation.getDetails().put(EPackageVersions.VERSION_ANNOTATION_DETAIL, declaredVersion);
			ePackage.getEAnnotations().add(annotation);
		}
		return ePackage;
	}
}
