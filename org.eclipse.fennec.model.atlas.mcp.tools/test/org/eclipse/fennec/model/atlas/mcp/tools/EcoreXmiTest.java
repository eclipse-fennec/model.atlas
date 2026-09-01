/*
 * ******************************************************************
 * Copyright (c) 2026 Contributors to the Eclipse Foundation.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *
 * Contributors:
 *   Data In Motion Consulting - initial implementation
 * ******************************************************************
 */
package org.eclipse.fennec.model.atlas.mcp.tools;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.eclipse.emf.ecore.EPackage;
import org.junit.jupiter.api.Test;

/**
 * The bytes that leave the runtime. Asserted on the generated document rather
 * than on EMF internals: what matters is what the server can read back.
 *
 * @author ilenia
 * @since Aug 27, 2026
 */
class EcoreXmiTest {

	@Test
	void aCrossPackageSupertypeLeavesAsAnNsUriHref() {
		EPackage base = TestModels.basePackage();
		String xmi = EcoreXmi.toXmi(TestModels.derivedPackage(base));

		assertThat(xmi).contains("nsURI=\"" + TestModels.DERIVED_NS_URI + "\"");
		// The supertype must carry the base package's namespace URI. A relative
		// segment, or the bare '#//UplinkMessage' form, resolves to nothing on the
		// server, which has no idea what document this came from.
		assertThat(xmi).contains(TestModels.BASE_NS_URI + "#//UplinkMessage");
		// The foreign package is referenced, never inlined.
		assertThat(xmi).doesNotContain("nsURI=\"" + TestModels.BASE_NS_URI + "\"");
	}

	@Test
	void aReferenceIntoTheDocumentItselfStaysAFragment() {
		EPackage base = TestModels.basePackage();
		String xmi = EcoreXmi.toXmi(base);

		assertThat(xmi).contains("UplinkMessage");
		assertThat(xmi).doesNotContain(TestModels.BASE_NS_URI + "#//UplinkMessage");
	}

	@Test
	void annotationsAndAbstractnessSurvive() {
		String xmi = EcoreXmi.toXmi(TestModels.basePackage());

		assertThat(xmi).contains("eAnnotations");
		assertThat(xmi).contains("https://eclipse.org/fennec/test/discriminator");
		assertThat(xmi).contains("applicationName");
		assertThat(xmi).contains("abstract=\"true\"");
	}

	@Test
	void ecoreBuiltInsResolveWithoutBeingInlined() {
		String xmi = EcoreXmi.toXmi(TestModels.basePackage());

		assertThat(xmi).contains("http://www.eclipse.org/emf/2002/Ecore#//EString");
	}

	@Test
	void aPackageWithoutANamespaceUriIsRefused() {
		assertThatThrownBy(() -> EcoreXmi.toXmi(TestModels.namelessPackage()))
				.isInstanceOf(ToolException.class)
				.hasMessageContaining("no namespace URI");
	}
}
