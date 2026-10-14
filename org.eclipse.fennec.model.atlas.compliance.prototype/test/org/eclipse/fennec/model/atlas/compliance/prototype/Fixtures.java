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
package org.eclipse.fennec.model.atlas.compliance.prototype;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EClass;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.EcoreResourceFactoryImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.model.compliance.context.ContextPackage;
import org.eclipse.fennec.model.compliance.corpus.CorpusPackage;
import org.eclipse.fennec.model.compliance.report.ComplianceReport;
import org.eclipse.fennec.model.compliance.report.ReportPackage;

/**
 * The one metamodel and the one review every artefact in this prototype is analysed against.
 *
 * <p>
 * {@code baeder-source.ecore} and the DSGVO review of it are copied verbatim out of a real
 * fennec-gdpr run. They are the point: a prototype whose review is hand-written proves only that
 * the code agrees with itself, and the fingerprints in {@code qvt.gdpr}'s own tests are literal
 * constants on both sides of the join for exactly that reason. Here the review is a real one and
 * the fingerprint is computed, so the join either holds or it does not.
 * </p>
 *
 * <p>
 * The three artefacts - an {@code OclConstraintSet}, an {@code EntityMappings} and a
 * {@code ProviderMapping} - are written by hand, because no deployment holds one of any of them
 * yet. They are the plausible ones for this domain, not favourable ones: the data they touch is
 * whatever the review already classified.
 * </p>
 */
final class Fixtures {

	/** Loaded once: the review is 77 KB and the package is read by every test. */
	private static EPackage baederSource;
	private static ComplianceReport baederSourceReview;

	private Fixtures() {
	}

	/** The reviewed metamodel: the city baths' source model, 13 classifiers. */
	static synchronized EPackage baederSource() {
		if (baederSource == null) {
			Resource resource = load("data/baeder-source.ecore", new EcoreResourceFactoryImpl());
			baederSource = (EPackage) resource.getContents().get(0);
		}
		return baederSource;
	}

	/** Its review, as fennec-gdpr produced it: 12 classifier evaluations, DE, AI_AGENT. */
	static synchronized ComplianceReport baederSourceReview() {
		if (baederSourceReview == null) {
			Resource resource = load("data/baeder-source-review.xmi", new XMIResourceFactoryImpl());
			baederSourceReview = (ComplianceReport) resource.getContents().get(0);
		}
		return baederSourceReview;
	}

	/** One classifier of the reviewed package, by name. */
	static EClass eClass(String name) {
		EClass found = (EClass) baederSource().getEClassifier(name);
		assertTrue(found != null, () -> "no EClass " + name + " in " + baederSource().getNsURI());
		return found;
	}

	private static Resource load(String path, Resource.Factory factory) {
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", factory);
		for (EPackage ePackage : List.of(EcorePackage.eINSTANCE, ReportPackage.eINSTANCE, ContextPackage.eINSTANCE,
				CorpusPackage.eINSTANCE)) {
			resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
		}
		Resource resource = resourceSet.createResource(URI.createURI(path));
		try (InputStream in = Fixtures.class.getResourceAsStream(path)) {
			assertTrue(in != null, () -> "fixture not on the test classpath: " + path);
			resource.load(in, Map.of());
		} catch (IOException e) {
			throw new IllegalStateException("could not read " + path, e);
		}
		return resource;
	}
}
