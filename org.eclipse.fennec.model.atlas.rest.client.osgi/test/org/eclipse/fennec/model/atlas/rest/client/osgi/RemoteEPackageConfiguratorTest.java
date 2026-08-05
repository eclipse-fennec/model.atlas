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
package org.eclipse.fennec.model.atlas.rest.client.osgi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;

import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.emf.osgi.constants.EMFNamespaces;
import org.eclipse.fennec.emf.osgi.fingerprint.util.FingerprintHelper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * F7 — the client-side {@code emf.fingerprint} property: computed locally from
 * the parsed package via {@link FingerprintHelper} (compute-never-trust), the
 * server-reported value acting only as a cross-check.
 */
class RemoteEPackageConfiguratorTest {

	private static final String NS = "urn:test:fingerprint:client";
	private static final String BASE_URI = "http://atlas.example:8080/rest";

	private static EPackage pkg() {
		EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
		pkg.setName("client");
		pkg.setNsPrefix("client");
		pkg.setNsURI(NS);
		EClass eClass = EcoreFactory.eINSTANCE.createEClass();
		eClass.setName("Thing");
		pkg.getEClassifiers().add(eClass);
		return pkg;
	}

	@Test
	@DisplayName("Published properties carry the locally computed emf.fingerprint")
	public void propertiesCarryLocallyComputedFingerprint() {
		EPackage pkg = pkg();
		String expected = FingerprintHelper.fingerprint(pkg);

		Map<String, Object> properties = new RemoteEPackageConfigurator(pkg, "atlas", "released", "1.0", BASE_URI)
				.getServiceProperties();

		assertEquals(expected, properties.get(EMFNamespaces.EMF_MODEL_FINGERPRINT),
				"emf.fingerprint must be the locally computed value");
		assertTrue(expected.startsWith("fp1:"), "fingerprint should use the current scheme tag");
	}

	@Test
	@DisplayName("A mismatching server fingerprint is never adopted — the local value wins")
	public void serverFingerprintNeverAdopted() {
		EPackage pkg = pkg();
		String local = FingerprintHelper.fingerprint(pkg);
		String bogusServerValue = "fp1:0000000000000000000000000000000000000000000000000000000000000000";
		assertNotEquals(bogusServerValue, local);

		Map<String, Object> properties = new RemoteEPackageConfigurator(pkg, "atlas", "released", "1.0", BASE_URI,
				bogusServerValue).getServiceProperties();

		assertEquals(local, properties.get(EMFNamespaces.EMF_MODEL_FINGERPRINT),
				"a server-reported fingerprint is a cross-check, never the property value");
	}

	@Test
	@DisplayName("A matching server fingerprint is confirmed silently")
	public void matchingServerFingerprintAccepted() {
		EPackage pkg = pkg();
		String local = FingerprintHelper.fingerprint(pkg);

		Map<String, Object> properties = new RemoteEPackageConfigurator(pkg, "atlas", "released", "1.0", BASE_URI,
				local).getServiceProperties();

		assertNotNull(properties.get(EMFNamespaces.EMF_MODEL_FINGERPRINT));
		assertEquals(local, properties.get(EMFNamespaces.EMF_MODEL_FINGERPRINT));
	}
}
