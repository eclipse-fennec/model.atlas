/*
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
package org.eclipse.fennec.model.atlas.workflow.registration;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.emf.ecore.impl.EPackageRegistryImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Reproduces the cross-stage serialization regression found in the manual git e2e test
 * (2026-07-22): with the same nsURI registered for two stages (= git branches), removing the
 * schema on ONE branch made objects of the OTHER branch unserializable by the REST/codec layer
 * ("[DynamicEObjectImpl] Error serializing outgoing object").
 *
 * <p><b>Mechanism.</b> Every stage registration publishes its own
 * {@link DynamicEPackageConfigurator}; the EMF-OSGi whiteboard applies ALL configurators to
 * shared, nsURI-flat {@link EPackage.Registry}s (e.g. the codec REST ResourceSet — unlike the
 * per-stage chain registries, which are stage-filtered). This test simulates exactly that
 * whiteboard behavior on a single shared registry:
 * {@code configure(draft) → configure(approved) → unconfigure(approved)}.
 *
 * <p>{@code configureEPackage} does a last-wins {@code put(nsURI, pkg)} and
 * {@code unconfigureEPackage} does an unconditional {@code remove(nsURI)} — so the approved
 * EXIT removes the slot even though draft's EPackage is still registered, and the shared
 * registry can no longer resolve the nsURI at all.
 */
public class DynamicEPackageConfiguratorSharedRegistryTest {

	private static final String NS_URI = "http://example.org/person/1.0";
	private static final String SCOPE = "git_scope";

	@Test
	@Disabled("Known-red reproduction of the flat-nsURI shared-registry clobber — same bug class as the "
			+ "MetadataService issue found in the 2026-07-22 git e2e test (see model.atlas#156, fingerprint-join). "
			+ "Enable once DynamicEPackageConfigurator un/configure is made version/instance-aware.")
	@DisplayName("EXIT of one stage must not make the nsURI unresolvable in a shared registry while another stage still has it")
	public void unconfigureOfOneStageLeavesOtherStagesResolvable() {
		EPackage draftPkg = newPersonPackage();
		EPackage approvedPkg = newPersonPackage();

		DynamicEPackageConfigurator draft = new DynamicEPackageConfigurator(draftPkg, "ecore", "1.0", SCOPE, "draft", "fake:draft");
		DynamicEPackageConfigurator approved = new DynamicEPackageConfigurator(approvedPkg, "ecore", "1.0", SCOPE,
				"approved", "fake:approved");

		EPackage.Registry shared = new EPackageRegistryImpl();

		// The whiteboard applies every configurator to the shared registry as its service
		// appears, and unconfigures it when the service goes away:
		draft.configureEPackage(shared); // draft ENTER
		approved.configureEPackage(shared); // approved ENTER (last-wins overwrite of the slot)
		approved.unconfigureEPackage(shared); // approved EXIT (schema removed on that branch)

		EPackage resolved = shared.getEPackage(NS_URI);
		assertNotNull(resolved,
				"the nsURI must still resolve in the shared registry: draft's EPackage is still registered "
						+ "(this is what broke REST/codec serialization of draft objects after the approved removal)");
		assertSame(draftPkg, resolved, "the surviving stage's (draft's) EPackage should occupy the slot");
	}

	private static EPackage newPersonPackage() {
		EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
		pkg.setName("person");
		pkg.setNsPrefix("person");
		pkg.setNsURI(NS_URI);
		return pkg;
	}
}
