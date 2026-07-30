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
 *      Data In Motion - initial API and implementation
 */
package org.eclipse.fennec.model.atlas.rest.ecore.xmi;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedHashMap;

/**
 * The XMI writer must be non-destructive: serving an EPackage over REST must
 * not re-parent the instance into the throw-away response resource. When the
 * served instance is a shared one (a registered singleton such as
 * {@code EcorePackage.eINSTANCE} reached via the parent-scope fallback, or a
 * storage-loaded package still attached to its {@code file:} resource), the
 * old move-into-response behaviour permanently changed the instance's
 * {@code eResource()} — for Ecore itself to a resource literally named
 * {@code ecore.ecore} — corrupting every href later computed against it
 * (served content then carried
 * {@code eType="ecore:EDataType file:/...storage.../ecore.ecore#//EString"}
 * instead of the canonical Ecore nsURI, making instances undeserializable on
 * every client).
 */
class EcoreMessageBodyHandlerTest {

	private static final MediaType XMI = new MediaType("application", "xmi");

	private ResourceSet responseResourceSet;
	private EcoreMessageBodyHandler handler;

	/** The resource EcorePackage.eINSTANCE was attached to before the test. */
	private Resource ecoreResourceBefore;

	@BeforeEach
	void setUp() {
		responseResourceSet = new ResourceSetImpl();
		responseResourceSet.getResourceFactoryRegistry().getContentTypeToFactoryMap()
				.put(EcorePackage.eCONTENT_TYPE, new XMIResourceFactoryImpl());
		responseResourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap()
				.put(Resource.Factory.Registry.DEFAULT_EXTENSION, new XMIResourceFactoryImpl());
		handler = new EcoreMessageBodyHandler() {
			@Override
			protected ResourceSet getResourceSet() {
				return responseResourceSet;
			}
		};
		ecoreResourceBefore = EcorePackage.eINSTANCE.eResource();
	}

	@AfterEach
	void restoreEcoreSingleton() {
		// A (buggy) writer may have stolen the GLOBAL Ecore singleton into a test
		// resource; put it back so other tests in this JVM see a sane world.
		Resource current = EcorePackage.eINSTANCE.eResource();
		if (current != ecoreResourceBefore) {
			if (current != null) {
				current.getContents().remove(EcorePackage.eINSTANCE);
			}
			if (ecoreResourceBefore != null) {
				ecoreResourceBefore.getContents().add(EcorePackage.eINSTANCE);
			}
		}
	}

	private String write(EPackage ePackage) throws IOException {
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		handler.writeTo(ePackage, EPackage.class, EPackage.class, new java.lang.annotation.Annotation[0], XMI,
				new MultivaluedHashMap<>(), out);
		return out.toString();
	}

	/** A minimal dynamic package with an EString attribute, like an uploaded schema. */
	private static EPackage schemaPackage() {
		EPackage p = EcoreFactory.eINSTANCE.createEPackage();
		p.setName("driftremoval");
		p.setNsPrefix("dr");
		p.setNsURI("http://atlas.example/test/driftremoval/1.0");
		EClass thing = EcoreFactory.eINSTANCE.createEClass();
		thing.setName("Thing");
		EAttribute name = EcoreFactory.eINSTANCE.createEAttribute();
		name.setName("name");
		// resolved (non-proxy) reference into the global Ecore package — the state a
		// server-side package is in after indexing/validation resolved its proxies
		name.setEType(EcorePackage.eINSTANCE.getEString());
		thing.getEStructuralFeatures().add(name);
		p.getEClassifiers().add(thing);
		return p;
	}

	@Test
	void writeTo_doesNotStealThePackageFromItsResource() throws IOException {
		EPackage p = schemaPackage();
		Resource storage = new XMIResourceFactoryImpl()
				.createResource(URI.createFileURI("/opt/modelatlas/runtime/data/jena/schema/release/pkg.xmi"));
		storage.getContents().add(p);

		write(p);

		assertSame(storage, p.eResource(),
				"serving a package must not re-parent it into the response resource");
	}

	@Test
	void writeTo_servingTheEcoreSingleton_doesNotHijackIt() throws IOException {
		// This is what a client prefetching every listed nsURI does: the Ecore package
		// is listed via the parent atlas scope, so its content gets served too.
		write(EcorePackage.eINSTANCE);

		assertSame(ecoreResourceBefore, EcorePackage.eINSTANCE.eResource(),
				"serving Ecore must not move the global singleton into a response resource named ecore.ecore");
	}

	@Test
	void servedContent_keepsCanonicalEcoreHrefs_evenAfterEcoreContentWasServed() throws IOException {
		// 1. a client fetches the Ecore package's content (buggy writer: the steal)
		write(EcorePackage.eINSTANCE);

		// 2. any package with a resolved EString reference is served afterwards
		EPackage p = schemaPackage();
		Resource storage = new XMIResourceFactoryImpl()
				.createResource(URI.createFileURI("/opt/modelatlas/runtime/data/jena/schema/release/pkg.xmi"));
		storage.getContents().add(p);
		String xmi = write(p);

		assertTrue(xmi.contains("http://www.eclipse.org/emf/2002/Ecore#//EString"),
				"EString must be referenced by the canonical Ecore nsURI, got:\n" + xmi);
		assertFalse(xmi.contains("ecore.ecore"),
				"served content must never reference the response resource of an earlier request:\n" + xmi);
	}
}
