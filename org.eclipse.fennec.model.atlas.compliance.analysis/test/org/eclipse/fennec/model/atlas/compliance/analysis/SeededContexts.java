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
package org.eclipse.fennec.model.atlas.compliance.analysis;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcorePackage;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.ecore.xmi.impl.XMIResourceFactoryImpl;
import org.eclipse.fennec.model.compliance.context.ComplianceContext;
import org.eclipse.fennec.model.compliance.context.ContextPackage;
import org.eclipse.fennec.model.compliance.corpus.CorpusPackage;
import org.eclipse.fennec.model.compliance.report.ReportPackage;

/**
 * The compliance contexts this repository seeds into the jena image, loaded as they are stored.
 *
 * <p>
 * These are the real files, not fixtures: {@code gdpr.xmi} is 1.3 MB of consolidated Regulation
 * and {@code bsi-grundschutz-pp.xmi} 6.9 MB of control catalogue. A fixture of a handful of
 * requirements would pass against a resolver that is wrong about the shapes the actual corpora
 * have - which is exactly what went wrong once already, since the issue describing this work
 * recorded that only {@code LegalUnit} carries text, while the catalogue the deployment ships
 * cites 1000 {@code Control}s that carry none.
 * </p>
 */
final class SeededContexts {

	private static final String SEEDED = "docker/dockercompose/initial-models/scopes/dimcity/compliance-context";

	private static final Map<String, ComplianceContext> LOADED = new HashMap<>();

	private SeededContexts() {
	}

	/**
	 * One seeded context by its id, loaded once per JVM.
	 *
	 * @param contextId the file's base name, which is also the context's id
	 * @return the context, never {@code null}
	 */
	static synchronized ComplianceContext get(String contextId) {
		return LOADED.computeIfAbsent(contextId, SeededContexts::load);
	}

	private static ComplianceContext load(String contextId) {
		Path file = directory().resolve(contextId + ".xmi");
		assertTrue(Files.isReadable(file), () -> "seeded context not readable: " + file);
		ResourceSet resourceSet = new ResourceSetImpl();
		resourceSet.getResourceFactoryRegistry().getExtensionToFactoryMap().put("*", new XMIResourceFactoryImpl());
		for (EPackage ePackage : List.of(EcorePackage.eINSTANCE, ContextPackage.eINSTANCE, CorpusPackage.eINSTANCE,
				ReportPackage.eINSTANCE)) {
			resourceSet.getPackageRegistry().put(ePackage.getNsURI(), ePackage);
		}
		Resource resource = resourceSet.getResource(URI.createFileURI(file.toAbsolutePath().toString()), true);
		assertTrue(resource.getContents().get(0) instanceof ComplianceContext,
				() -> "not a ComplianceContext: " + file);
		return (ComplianceContext) resource.getContents().get(0);
	}

	/**
	 * The seeded directory, found by walking up from wherever the tests were started.
	 * <p>
	 * The working directory of a test run is the bundle project under bnd and the workspace root
	 * under some IDE configurations, so neither is assumed. A failure here is loud rather than a
	 * skip: a test that quietly does not run is the one way this suite could stop being true
	 * without anybody noticing.
	 */
	private static Path directory() {
		Path start = Paths.get(System.getProperty("user.dir")).toAbsolutePath();
		for (Path candidate = start; candidate != null; candidate = candidate.getParent()) {
			Path seeded = candidate.resolve(SEEDED);
			if (Files.isDirectory(seeded)) {
				return seeded;
			}
		}
		throw new IllegalStateException("no " + SEEDED + " above " + start);
	}
}
