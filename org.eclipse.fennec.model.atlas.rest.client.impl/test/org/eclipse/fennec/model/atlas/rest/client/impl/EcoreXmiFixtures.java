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
package org.eclipse.fennec.model.atlas.rest.client.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EReference;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;

/**
 * Builds a representative dynamic {@link EPackage} and serializes it to Ecore
 * XMI the same way the server's {@code EcoreMessageBodyHandler} does — so the
 * P2-4 tests round-trip the exact wire bytes without a running Atlas.
 */
final class EcoreXmiFixtures {

	static final String SAMPLE_NS_URI = "https://example.org/atlas/sample/1.0";
	static final String SAMPLE_CLASS = "Person";

	private EcoreXmiFixtures() {
		// test helper
	}

	/**
	 * A self-contained package with one class, a primitive attribute (a
	 * cross-reference into {@code Ecore} via {@code EString}) and a containment
	 * reference back to the same class.
	 */
	static EPackage samplePackage() {
		EcoreFactory f = EcoreFactory.eINSTANCE;
		EPackage pkg = f.createEPackage();
		pkg.setName("sample");
		pkg.setNsPrefix("sample");
		pkg.setNsURI(SAMPLE_NS_URI);

		EClass person = f.createEClass();
		person.setName(SAMPLE_CLASS);
		pkg.getEClassifiers().add(person);

		EAttribute name = f.createEAttribute();
		name.setName("name");
		name.setEType(EcorePackage.eINSTANCE.getEString());
		person.getEStructuralFeatures().add(name);

		EReference friends = f.createEReference();
		friends.setName("friends");
		friends.setEType(person);
		friends.setUpperBound(-1);
		person.getEStructuralFeatures().add(friends);

		return pkg;
	}

	/** Serialize a package to Ecore XMI bytes (mirrors the server's writer). */
	static byte[] toXmiBytes(EPackage pkg) {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new EcoreResourceFactoryImpl());
		resourceSet.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
		Resource resource = resourceSet.createResource(URI.createURI("sample.ecore"));
		resource.getContents().add(pkg);
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			resource.save(out, Map.of());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return out.toByteArray();
	}

	/** The sample package, already serialized to Ecore XMI bytes. */
	static byte[] sampleXmiBytes() {
		return toXmiBytes(samplePackage());
	}
}
