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
package org.eclipse.fennec.model.atlas.bootstrap;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * The bootstrap loader derives the version of the models it mounts by the same rule the REST
 * upload uses, which its javadoc has always claimed: declared first, inferred only from a
 * version-shaped last segment (issue #180).
 *
 * @see <a href="https://github.com/eclipse-fennec/model.atlas/issues/180">issue #180</a>
 */
@DisplayName("InitialModelLoader — the version of a bootstrapped model")
class InitialModelLoaderVersionTest {

	@Test
	@DisplayName("a version-shaped last segment is used")
	void takesAVersionShapedLastSegment() {
		assertEquals("1.0.0", InitialModelLoader.extractVersion(ePackage("http://example.org/model/1.0.0", null)));
	}

	@Test
	@DisplayName("a year is not a version")
	void aYearIsNotAVersion() {
		assertNull(InitialModelLoader.extractVersion(ePackage("http://www.eclipse.org/emf/2002/Ecore", null)));
		assertNull(InitialModelLoader.extractVersion(ePackage("http://www.omg.org/spec/UML/20131001", null)));
	}

	@Test
	@DisplayName("a mid-path version or bare major is not read")
	void ignoresMidPathAndBareMajor() {
		assertNull(InitialModelLoader.extractVersion(ePackage("http://example.org/1.0/model", null)));
		assertNull(InitialModelLoader.extractVersion(ePackage("http://example.org/model/1", null)));
	}

	@Test
	@DisplayName("the model's own Version annotation wins")
	void honoursTheVersionAnnotation() {
		assertEquals("2.5.0",
				InitialModelLoader.extractVersion(ePackage("http://example.org/model/9.9.9", "2.5.0")));
	}

	@Test
	@DisplayName("an unparseable declaration is ignored rather than failing the boot")
	void ignoresAnUnparseableDeclaration() {
		// A one-shot loader must not abort start-up over a junk annotation; unlike the REST
		// upload, there is no caller to report a bad request to.
		assertEquals("1.0.0",
				InitialModelLoader.extractVersion(ePackage("http://example.org/model/1.0.0", "not-a-version")));
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
