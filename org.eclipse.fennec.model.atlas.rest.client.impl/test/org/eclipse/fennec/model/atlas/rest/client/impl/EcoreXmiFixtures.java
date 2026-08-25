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
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
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
		ResourceSet resourceSet = newResourceSet();
		Resource resource = resourceSet.createResource(URI.createURI("sample.ecore"));
		resource.getContents().add(pkg);
		return save(resource);
	}

	/** The sample package, already serialized to Ecore XMI bytes. */
	static byte[] sampleXmiBytes() {
		return toXmiBytes(samplePackage());
	}

	// ---- Cross-package inheritance (issue #203) --------------------------------
	//
	// A shared base package plus extensions of it, each served as its own
	// document — the layered-model shape the Atlas serves in practice. Every
	// package is serialized on a resource named by its own nsURI, so a reference
	// across the package boundary is written as an absolute href into the other
	// namespace, exactly as the server emits it:
	//
	//   <eClassifiers name="VendorUplink"
	//       eSuperTypes="https://example.org/atlas/uplink/1.0#//UplinkMessage"/>

	static final String BASE_NS_URI = "https://example.org/atlas/uplink/1.0";
	static final String BASE_CLASS = "UplinkMessage";
	static final String INHERITED_ATTRIBUTE = "deduplicationId";

	static final String DERIVED_NS_URI = "https://example.org/atlas/uplink/vendor/1.0";
	static final String DERIVED_CLASS = "VendorUplink";
	static final String DERIVED_ATTRIBUTE = "batteryVoltage";

	static final String LEAF_NS_URI = "https://example.org/atlas/uplink/vendor/leaf/1.0";
	static final String LEAF_CLASS = "VendorLeafUplink";

	static final String LEFT_NS_URI = "https://example.org/atlas/mutual/left/1.0";
	static final String LEFT_CLASS = "Left";
	static final String RIGHT_NS_URI = "https://example.org/atlas/mutual/right/1.0";
	static final String RIGHT_CLASS = "Right";
	static final String TO_RIGHT = "right";
	static final String TO_LEFT = "left";

	/**
	 * A three-package inheritance chain, keyed by nsURI:
	 * {@code base <- derived <- leaf}. {@code leaf} reaches
	 * {@link #INHERITED_ATTRIBUTE} only through two hops, so it exercises
	 * transitive resolution.
	 */
	static Map<String, byte[]> inheritanceChainXmi() {
		EPackage base = emptyPackage("uplink", BASE_NS_URI);
		EClass uplink = classNamed(BASE_CLASS);
		uplink.getEStructuralFeatures().add(stringAttribute(INHERITED_ATTRIBUTE));
		base.getEClassifiers().add(uplink);

		EPackage derived = emptyPackage("vendor", DERIVED_NS_URI);
		EClass vendor = classNamed(DERIVED_CLASS);
		vendor.getESuperTypes().add(uplink);
		vendor.getEStructuralFeatures().add(stringAttribute(DERIVED_ATTRIBUTE));
		derived.getEClassifiers().add(vendor);

		EPackage leaf = emptyPackage("leaf", LEAF_NS_URI);
		EClass leafClass = classNamed(LEAF_CLASS);
		leafClass.getESuperTypes().add(vendor);
		leaf.getEClassifiers().add(leafClass);

		return serializeSeparately(List.of(base, derived, leaf));
	}

	/**
	 * Two packages whose classes reference each other across the boundary, keyed
	 * by nsURI — resolving either one has to terminate.
	 */
	static Map<String, byte[]> mutualReferenceXmi() {
		EPackage left = emptyPackage("left", LEFT_NS_URI);
		EClass leftClass = classNamed(LEFT_CLASS);
		left.getEClassifiers().add(leftClass);

		EPackage right = emptyPackage("right", RIGHT_NS_URI);
		EClass rightClass = classNamed(RIGHT_CLASS);
		right.getEClassifiers().add(rightClass);

		leftClass.getEStructuralFeatures().add(reference(TO_RIGHT, rightClass));
		rightClass.getEStructuralFeatures().add(reference(TO_LEFT, leftClass));

		return serializeSeparately(List.of(left, right));
	}

	/**
	 * Serialize each package as its own document, reproducing the two href shapes
	 * the server emits (see the {@code dragino} package it serves):
	 * <ul>
	 * <li>the document being saved is named after the package
	 * ({@code vendor.ecore}), so a reference <em>inside</em> it comes out
	 * document-relative — {@code vendor.ecore#//DecodedObject};</li>
	 * <li>every other package sits on a resource named by its nsURI, which cannot
	 * be relativized against that document name, so a reference <em>across</em>
	 * the package boundary comes out as an absolute cross-namespace href —
	 * {@code eSuperTypes="https://example.org/atlas/uplink/1.0#//UplinkMessage"}.</li>
	 * </ul>
	 * Getting this shape right is the whole point of the fixture: relativized
	 * hrefs would not reproduce the failure.
	 */
	private static Map<String, byte[]> serializeSeparately(List<EPackage> packages) {
		ResourceSet resourceSet = newResourceSet();
		for (EPackage pkg : packages) {
			resourceSet.createResource(URI.createURI(pkg.getNsURI())).getContents().add(pkg);
		}
		Map<String, byte[]> bytes = new LinkedHashMap<>();
		for (EPackage pkg : packages) {
			Resource byNsUri = pkg.eResource();
			Resource document = resourceSet.createResource(URI.createURI(pkg.getName() + ".ecore"));
			document.getContents().add(pkg);
			bytes.put(pkg.getNsURI(), save(document));
			// Put it back, so it is again referenced by nsURI from the next document.
			byNsUri.getContents().add(pkg);
			resourceSet.getResources().remove(document);
		}
		return bytes;
	}

	private static EPackage emptyPackage(String name, String nsUri) {
		EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
		pkg.setName(name);
		pkg.setNsPrefix(name);
		pkg.setNsURI(nsUri);
		return pkg;
	}

	private static EClass classNamed(String name) {
		EClass eClass = EcoreFactory.eINSTANCE.createEClass();
		eClass.setName(name);
		return eClass;
	}

	private static EAttribute stringAttribute(String name) {
		EAttribute attribute = EcoreFactory.eINSTANCE.createEAttribute();
		attribute.setName(name);
		attribute.setEType(EcorePackage.eINSTANCE.getEString());
		return attribute;
	}

	private static EReference reference(String name, EClass type) {
		EReference eReference = EcoreFactory.eINSTANCE.createEReference();
		eReference.setName(name);
		eReference.setEType(type);
		return eReference;
	}

	private static ResourceSet newResourceSet() {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new EcoreResourceFactoryImpl());
		resourceSet.getPackageRegistry().put(EcorePackage.eNS_URI, EcorePackage.eINSTANCE);
		return resourceSet;
	}

	private static byte[] save(Resource resource) {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		try {
			resource.save(out, Map.of());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
		return out.toByteArray();
	}

	static final String SERVER_SHAPED_NS_URI = "https://example.org/atlas/uplink/servershaped/1.0";
	static final String SERVER_SHAPED_CLASS = "ServerShapedUplink";
	static final String CONTAINED_CLASS = "DecodedObject";
	static final String CONTAINMENT_REFERENCE = "object";

	/**
	 * A document in the shape a live Atlas actually serves — kept verbatim rather
	 * than round-tripped through EMF, because EMF would not write the second form
	 * below for a same-document reference and so would not reproduce it:
	 * <ul>
	 * <li>the super type as an absolute href into another namespace
	 * ({@code eSuperTypes="https://…/uplink/1.0#//UplinkMessage"});</li>
	 * <li>a reference to a class in <em>this</em> document, written with the
	 * server's own resource name ({@code eType="… servershaped.ecore#//DecodedObject"}).</li>
	 * </ul>
	 * The second is resolved against whatever URI the client parses the document
	 * under, so it only resolves if the client routes it back to the document.
	 */
	static byte[] serverShapedXmi() {
		String xmi = """
				<?xml version="1.0" encoding="UTF-8"?>
				<ecore:EPackage xmi:version="2.0" xmlns:xmi="http://www.omg.org/XMI" \
				xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" \
				xmlns:ecore="http://www.eclipse.org/emf/2002/Ecore" name="servershaped" \
				nsURI="https://example.org/atlas/uplink/servershaped/1.0" nsPrefix="servershaped">
				  <eClassifiers xsi:type="ecore:EClass" name="DecodedObject">
				    <eStructuralFeatures xsi:type="ecore:EAttribute" name="batV" \
				eType="ecore:EDataType http://www.eclipse.org/emf/2002/Ecore#//EDouble"/>
				  </eClassifiers>
				  <eClassifiers xsi:type="ecore:EClass" name="ServerShapedUplink" \
				eSuperTypes="https://example.org/atlas/uplink/1.0#//UplinkMessage">
				    <eStructuralFeatures xsi:type="ecore:EReference" name="object" \
				eType="ecore:EClass servershaped.ecore#//DecodedObject" containment="true"/>
				  </eClassifiers>
				</ecore:EPackage>
				""";
		return xmi.getBytes(StandardCharsets.UTF_8);
	}
}
