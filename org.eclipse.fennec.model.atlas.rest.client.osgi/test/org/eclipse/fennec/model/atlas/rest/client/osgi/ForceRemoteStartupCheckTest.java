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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

import org.eclipse.emf.ecore.EPackage;
import org.eclipse.emf.ecore.EcoreFactory;
import org.eclipse.fennec.model.atlas.rest.client.api.ResolvedEPackage;
import org.eclipse.fennec.model.atlas.rest.client.api.TransportException;
import org.eclipse.fennec.model.atlas.rest.client.osgi.LocalServiceWatcher.LocalModel;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the P3-8 {@code force.remote} startup version check and its version
 * preference rule. Plain Java — local models, the resolver and the publisher are all
 * injected.
 */
class ForceRemoteStartupCheckTest {

	private final List<String> published = new ArrayList<>();

	private static EPackage ePackage(String nsUri) {
		EPackage pkg = EcoreFactory.eINSTANCE.createEPackage();
		pkg.setName("p");
		pkg.setNsPrefix("p");
		pkg.setNsURI(nsUri);
		return pkg;
	}

	private static ResolvedEPackage resolved(String nsUri, String version) {
		return new ResolvedEPackage(ePackage(nsUri), nsUri, "atlas", "schema", "released", version);
	}

	private PackagePublication recorder() {
		return (ePackage, scope, stage, version, serverFingerprint) -> {
			published.add(ePackage.getNsURI());
			return true;
		};
	}

	private ForceRemoteStartupCheck check(Collection<LocalModel> locals,
			Function<String, Optional<ResolvedEPackage>> resolver) {
		return new ForceRemoteStartupCheck(() -> locals, resolver, recorder());
	}

	// ---- version preference rule ------------------------------------------

	@Test
	void versionRule() {
		assertFalse(ForceRemoteStartupCheck.isRemoteNewer("1.0", null), "no remote version → keep local");
		assertFalse(ForceRemoteStartupCheck.isRemoteNewer("1.0", "  "), "blank remote version → keep local");
		assertTrue(ForceRemoteStartupCheck.isRemoteNewer(null, "1.0"), "unknown local → prefer remote");
		assertFalse(ForceRemoteStartupCheck.isRemoteNewer("1.0", "1.0"), "equal → keep local");
		assertTrue(ForceRemoteStartupCheck.isRemoteNewer("1.0", "1.1"), "remote newer → supersede");
		assertTrue(ForceRemoteStartupCheck.isRemoteNewer("1.0.0", "2.0.0"), "remote newer → supersede");
		assertFalse(ForceRemoteStartupCheck.isRemoteNewer("2.0", "1.5"), "remote older → keep local");
		assertTrue(ForceRemoteStartupCheck.isRemoteNewer("rev-A", "rev-B"), "unparseable but different → prefer remote");
	}

	// ---- run --------------------------------------------------------------

	@Test
	void supersedesLocalsWhenAtlasIsNewer() {
		Map<String, ResolvedEPackage> atlas = Map.of("urn:a", resolved("urn:a", "2.0"), "urn:b",
				resolved("urn:b", "1.0"));
		List<LocalModel> locals = List.of(new LocalModel("urn:a", "1.0"), new LocalModel("urn:b", "1.0"));

		int superseded = check(locals, ns -> Optional.ofNullable(atlas.get(ns))).run();

		// urn:a (atlas 2.0 > local 1.0) superseded; urn:b (equal) left alone.
		assertEquals(1, superseded);
		assertEquals(List.of("urn:a"), published);
	}

	@Test
	void ignoresLocalsTheAtlasDoesNotHave() {
		List<LocalModel> locals = List.of(new LocalModel("urn:only-local", "1.0"));

		int superseded = check(locals, ns -> Optional.empty()).run();

		assertEquals(0, superseded);
		assertTrue(published.isEmpty());
	}

	@Test
	void transportFailureSkipsThePackageWithoutFailing() {
		List<LocalModel> locals = List.of(new LocalModel("urn:down", "1.0"), new LocalModel("urn:up", "1.0"));
		Function<String, Optional<ResolvedEPackage>> resolver = ns -> {
			if (ns.equals("urn:down")) {
				throw new TransportException("read timed out");
			}
			return Optional.of(resolved(ns, "2.0"));
		};

		int superseded = check(locals, resolver).run();

		// The unreachable one is skipped; the reachable, newer one is still superseded.
		assertEquals(1, superseded);
		assertEquals(List.of("urn:up"), published);
	}
}
