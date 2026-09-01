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

import org.eclipse.emf.ecore.EAnnotation;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;

/**
 * Two packages in the shape the tool actually meets: a base model the runtime
 * already had, and a derived model inferred against it, whose one class extends
 * a class of the other package. The cross-package supertype is the whole point —
 * it is what a naive serialization gets wrong.
 *
 * @author ilenia
 * @since Aug 27, 2026
 */
final class TestModels {

	static final String BASE_NS_URI = "https://eclipse.org/fennec/test/lorawan";
	static final String DERIVED_NS_URI = "https://eclipse.org/fennec/test/inference/em310udl";
	static final String OTHER_NS_URI = "https://elsewhere.example/test/lorawan";

	private TestModels() {
		// fixtures only
	}

	/** A package with one abstract class carrying an EAnnotation. */
	static EPackage basePackage() {
		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		ePackage.setName("lorawan");
		ePackage.setNsPrefix("lorawan");
		ePackage.setNsURI(BASE_NS_URI);

		EClass uplink = EcoreFactory.eINSTANCE.createEClass();
		uplink.setName("UplinkMessage");
		uplink.setAbstract(true);
		EAttribute devEui = EcoreFactory.eINSTANCE.createEAttribute();
		devEui.setName("devEui");
		devEui.setEType(EcorePackage.eINSTANCE.getEString());
		uplink.getEStructuralFeatures().add(devEui);
		EAnnotation annotation = EcoreFactory.eINSTANCE.createEAnnotation();
		annotation.setSource("https://eclipse.org/fennec/test/discriminator");
		annotation.getDetails().put("key", "applicationName");
		uplink.getEAnnotations().add(annotation);
		ePackage.getEClassifiers().add(uplink);
		return ePackage;
	}

	/** A package whose only class extends {@link #basePackage()}'s abstract class. */
	static EPackage derivedPackage(EPackage base) {
		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		ePackage.setName("em310udl");
		ePackage.setNsPrefix("em310udl");
		ePackage.setNsURI(DERIVED_NS_URI);

		EClass derived = EcoreFactory.eINSTANCE.createEClass();
		derived.setName("EM310UDLUplink");
		derived.getESuperTypes().add((EClass) base.getEClassifier("UplinkMessage"));
		EAttribute distance = EcoreFactory.eINSTANCE.createEAttribute();
		distance.setName("distance");
		distance.setEType(EcorePackage.eINSTANCE.getEInt());
		derived.getEStructuralFeatures().add(distance);
		ePackage.getEClassifiers().add(derived);
		return ePackage;
	}

	/** A package with no namespace URI at all. */
	static EPackage namelessPackage() {
		EPackage ePackage = EcoreFactory.eINSTANCE.createEPackage();
		ePackage.setName("nameless");
		return ePackage;
	}
}
